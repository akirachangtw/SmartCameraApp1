#!/usr/bin/env python3
"""close_helper.py - deterministic helper for `/ticket-author close <TICKET_ID>` (TKA-CLOSE-01).

Python 3, standard library only. The helper never calls the Registry, a model or the
network. The Agent reads and writes the Registry through the Registry tools and hands
observations in as JSON files in the scratch directory; every sub-command prints one JSON
object on stdout whose `next` / `ok` fields tell the Agent what to do.

Scratch : <repo>/.tmp/ticket-author/close/<TICKET_ID>/   (kept 7 days, never committed)
Writes  : only the scratch directory and, in `finalize`, one minimal result section of the
          target Ticket file. Git is used read-only. Nothing else is touched.

Sub-commands
  preflight --ticket ID --registry F [--dependents F]
  reviewer  --ticket ID [--sonnet-available 0|1] [--codex-available 0|1] [--escalate n1,n2]
  decide    --ticket ID --registry F --verdicts F --round N --fallback-tried 0|1
  readback  --ticket ID --registry F
  finalize  --ticket ID --registry F --verdicts F --summary F --commits h1,h2
  sweep     [--root R] [--days 7] [--exclude DIR] [--max 200]
Global      --repo ROOT   --now EPOCH

Exit codes  0 result produced | 2 usage error | 3 refused (precondition not met)
"""
from __future__ import annotations

import argparse
import fnmatch
import json
import os
import re
import shutil
import stat
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path

PROJECT_DIR = "plans/cso_briefing/project"
CLOSE_ROOT = ".tmp/ticket-author/close"
MARK_BEGIN = "<!-- close-result:begin -->"
MARK_END = "<!-- close-result:end -->"
DOC_EXT = {".md", ".txt", ".rst"}
MAX_ROUNDS = 2
MAX_CANDIDATE_ENTRIES = 5000
REVIEW_FIELDS = ("qa", "sv", "codex", "traycer")
REVIEW_ENUM = {"PASS", "FAIL", "N_A", "PENDING"}
AC_STATUS = {"PASS", "FAIL", "N/A", "UNVERIFIED"}
DEP_STATUS = {"NONE_FOUND", "MISSING", "LEAD", "UNKNOWN"}
TICKET_ID_RE = re.compile(r"^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$")
PREFIX_RE = re.compile(r"^\[([A-Z][A-Z0-9]*(?:-[A-Z0-9]+)+)\]")
FILE_ATTRIBUTE_REPARSE_POINT = 0x400


# --------------------------------------------------------------------------- basics
class Refuse(Exception):
    """Raised when a precondition is not met (exit 3)."""

    def __init__(self, payload):
        super().__init__(json.dumps(payload, ensure_ascii=False))
        self.payload = payload


def emit(obj, code=0):
    sys.stdout.write(json.dumps(obj, ensure_ascii=False, indent=None) + "\n")
    sys.stdout.flush()
    return code


def git(repo, *args, timeout=60):
    try:
        r = subprocess.run(["git", "-C", str(repo), *args], capture_output=True, timeout=timeout)
    except (OSError, subprocess.SubprocessError):
        return 127, ""
    return r.returncode, r.stdout.decode("utf-8", "replace")


def load_json(path):
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f), None
    except FileNotFoundError:
        return None, "missing"
    except (OSError, ValueError) as e:
        return None, "invalid:%s" % type(e).__name__


def read_text(path):
    with open(path, encoding="utf-8", newline="") as f:
        return f.read()


def posix(p):
    return str(p).replace("\\", "/")


def close_dir(repo, tid):
    return Path(repo) / CLOSE_ROOT / tid


def rel_to(repo, p):
    try:
        return posix(Path(p).resolve().relative_to(Path(repo).resolve()))
    except ValueError:
        return posix(p)


def split_tokens(value):
    return [t.strip() for t in (value or "").split(",") if t.strip()]


def token_matches(path, token):
    path = posix(path)
    token = posix(token)
    return path == token or os.path.basename(path) == token or fnmatch.fnmatch(path, token)


def matches_any(path, tokens):
    return any(token_matches(path, t) for t in tokens)


# --------------------------------------------------------------------------- ticket file
def location_of(rel):
    rel = posix(rel)
    if rel.startswith(PROJECT_DIR + "/deliver/archive/"):
        return "deliver/archive"
    if rel.startswith(PROJECT_DIR + "/deliver/"):
        return "deliver"
    if rel.startswith(PROJECT_DIR + "/Pending/"):
        return "pending"
    if rel.startswith(PROJECT_DIR + "/void/"):
        return "void"
    return "other"


def locate_ticket(repo, tid, reg):
    """Registry `file` first, then a basename search. Returns a dict."""
    repo = Path(repo)
    pointer = (reg or {}).get("file")
    base = PROJECT_DIR
    result = {"status": "missing", "path": None, "location": None,
              "file_field": pointer, "file_field_stale": False, "pointer_conflict": False,
              "candidates": []}
    if pointer:
        direct = repo / base / pointer
        if direct.is_file():
            rel = rel_to(repo, direct)
            result.update(status="found", path=rel, location=location_of(rel), candidates=[rel])
            return result
    name = os.path.basename(pointer) if pointer else None
    roots = [repo / base / "deliver" / "archive", repo / base / "deliver", repo / base / "void", repo / base / "Pending"]
    found = []
    for i, root in enumerate(roots):
        if not root.is_dir():
            continue
        if i == 0:
            walker = os.walk(root)
        else:
            walker = [(str(root), [], os.listdir(root))]
        for dirpath, _dirs, files in walker:
            for fn in files:
                hit = (fn == name) if name else (fn.startswith(tid + "_") and fn.endswith(".md"))
                if hit:
                    p = Path(dirpath) / fn
                    if p.is_file():
                        found.append(rel_to(repo, p))
    found = sorted(set(found))
    result["candidates"] = found
    if len(found) == 1:
        result.update(status="found", path=found[0], location=location_of(found[0]))
        result["file_field_stale"] = True
        if pointer and pointer.startswith("Pending/") and result["location"] != "pending":
            result["pointer_conflict"] = True
    elif len(found) > 1:
        result["status"] = "ambiguous"
    return result


def parse_verify_config(text):
    cfg = {}
    in_sec = False
    for line in text.splitlines():
        if line.startswith("## "):
            in_sec = line.startswith("## Verify Config")
            continue
        if in_sec:
            m = re.match(r"^([A-Z_]+)=(.*)$", line.rstrip("\r"))
            if m:
                cfg[m.group(1)] = m.group(2).strip()
    return cfg


def parse_acs(text):
    """Top-level `- [ ]` / `- [x]` items of `## Acceptance Criteria`, continuation lines included."""
    lines = text.split("\n")
    start = None
    for i, line in enumerate(lines):
        if line.startswith("## Acceptance Criteria"):
            start = i + 1
            break
    if start is None:
        return []
    end = len(lines)
    in_fence = False
    for j in range(start, len(lines)):
        if re.match(r"^\s*(```|~~~)", lines[j]):
            in_fence = not in_fence
        if not in_fence and lines[j].startswith("## "):
            end = j
            break
    items = []
    cur = None
    for line in lines[start:end]:
        m = re.match(r"^- \[( |x|X)\] ?(.*)$", line)
        if m:
            cur = {"checked": m.group(1) in ("x", "X"), "first": m.group(2), "rest": []}
            items.append(cur)
            continue
        if cur is None:
            continue
        if line.startswith("#") or line.startswith("---") or line.startswith(">"):
            cur = None
            continue
        cur["rest"].append(line)
    acs = []
    for n, it in enumerate(items, 1):
        rest = list(it["rest"])
        while rest and not rest[-1].strip():
            rest.pop()
        full = "\n".join([it["first"]] + rest)
        acs.append({
            "n": n,
            "checked": it["checked"],
            "struck": it["first"].lstrip().startswith("~~"),
            "types": re.findall(r"\[(存在性|可達性|行為性|實機)\]", it["first"]),
            "text": full,
            "text_preview": full.split("\n", 1)[0][:400],
        })
    return acs


# --------------------------------------------------------------------------- git evidence
def resolve_commit(repo, ref):
    if not ref or not re.match(r"^[0-9a-fA-F]{4,40}$", ref):
        return None
    rc, out = git(repo, "rev-parse", "--verify", "--quiet", ref + "^{commit}")
    return out.strip() if rc == 0 and out.strip() else None


def range_evidence(repo, tid, cfg, ticket_rel, allowed, forbidden):
    base = resolve_commit(repo, cfg.get("DIFF_REF", "")) or resolve_commit(repo, cfg.get("BASE_COMMIT", ""))
    ev = {"base": base, "impl_end": None, "binding_ref": None, "commits": [],
          "scope": {"status": "UNKNOWN", "reason": "no_base", "violations": []},
          "diff_check": {"status": "UNKNOWN", "reason": "no_base", "output_head": ""},
          "attribution": {"own": [], "other": [], "unprefixed_touching_allowed": []}}
    if not base:
        return ev
    rc, out = git(repo, "log", "--reverse", "--format=%H%x1f%P%x1f%s", base + "..HEAD")
    if rc != 0:
        ev["scope"]["reason"] = "git_log_failed"
        ev["diff_check"]["reason"] = "git_log_failed"
        return ev
    ticket_name = os.path.basename(ticket_rel or "")
    progress = {".claude/PROGRESS.md", ".claude/PROGRESS_ARCHIVE.md"}

    def exempt(f):
        return f in progress or (posix(f).startswith(PROJECT_DIR + "/") and os.path.basename(f) == ticket_name)

    own_impl = []
    unprefixed_touch = []
    other = []
    own_all = []
    for row in out.splitlines():
        parts = row.split("\x1f")
        if len(parts) < 3:
            continue
        sha, parents, subject = parts[0], parts[1].split(), parts[2]
        if len(parents) > 1:
            continue
        m = PREFIX_RE.match(subject)
        rc2, names = git(repo, "diff-tree", "--no-commit-id", "--name-only", "-r", "--root", sha)
        files = [posix(n) for n in names.splitlines() if n.strip()] if rc2 == 0 else []
        touches_allowed = any(matches_any(f, allowed) for f in files)
        if m and m.group(1) == tid:
            own_all.append(sha)
            if any(not exempt(f) for f in files):
                own_impl.append((sha, files))
        elif m:
            other.append({"commit": sha[:8], "ticket": m.group(1), "touches_allowed": touches_allowed})
        elif touches_allowed:
            unprefixed_touch.append(sha[:8])
    ev["attribution"] = {"own": [c[:8] for c in own_all],
                         "own_implementation": [c[0][:8] for c in own_impl],
                         "other": other, "unprefixed_touching_allowed": unprefixed_touch}
    ev["commits"] = [c[0] for c in own_impl]
    if own_impl:
        ev["impl_end"] = own_impl[-1][0]
    ev["binding_ref"] = ev["impl_end"] or base
    # scope: only files touched by own implementation commits
    touched = sorted({f for _, files in own_impl for f in files})
    violations = [f for f in touched if not exempt(f) and (matches_any(f, forbidden) or not matches_any(f, allowed))]
    if violations:
        ev["scope"] = {"status": "FAIL", "reason": "own_commit_outside_allowed", "violations": violations}
    elif not own_impl:
        ev["scope"] = {"status": "UNKNOWN", "reason": "no_own_implementation_commit", "violations": []}
    elif unprefixed_touch:
        ev["scope"] = {"status": "UNKNOWN", "reason": "unprefixed_commit_touches_allowed:" + ",".join(unprefixed_touch), "violations": []}
    else:
        ev["scope"] = {"status": "PASS", "reason": "", "violations": []}
    # whitespace check on own implementation commits only
    if not own_impl:
        ev["diff_check"] = {"status": "UNKNOWN", "reason": "no_own_implementation_commit", "output_head": ""}
    else:
        bad = []
        for sha, _files in own_impl:
            rc3, o3 = git(repo, "diff", "--check", sha + "^", sha)
            if rc3 != 0 and (o3.strip()):
                bad.append(o3.strip().splitlines()[0][:200])
            elif rc3 not in (0, 2) and rc3 != 0:
                bad.append("git diff --check rc=%d" % rc3)
        ev["diff_check"] = {"status": "FAIL" if bad else "PASS", "reason": "", "output_head": "\n".join(bad[:3])}
    return ev


def ledger_summary(repo, tid):
    script = Path(repo) / ".claude/scripts/gate_ledger.py"
    if not script.is_file():
        return {"status": "UNAVAILABLE", "reason": "script_missing", "receipts": []}
    try:
        r = subprocess.run([sys.executable, str(script), "status", "--ticket", tid, "--json"],
                           cwd=str(repo), capture_output=True, timeout=20)
        data = json.loads(r.stdout.decode("utf-8", "replace") or "{}")
        receipts = [{"gate_point": x.get("gate_point"), "exit": x.get("exit"), "late": x.get("late")}
                    for x in data.get("receipts", [])]
        return {"status": "READ", "location": data.get("ticket_location"), "receipts": receipts}
    except (OSError, ValueError, subprocess.SubprocessError):
        return {"status": "UNAVAILABLE", "reason": "status_failed", "receipts": []}


def dependents_evidence(repo, tid, dep_path):
    if not dep_path:
        return {"status": "UNKNOWN", "reason": "not_provided", "direct": []}
    data, err = load_json(dep_path)
    if err:
        return {"status": "UNKNOWN", "reason": "dependents_" + err, "direct": []}
    tickets = data.get("tickets") if isinstance(data, dict) else data
    if not isinstance(tickets, list):
        return {"status": "UNKNOWN", "reason": "dependents_shape", "direct": []}
    direct = []
    unreadable = []
    for t in tickets:
        if not isinstance(t, dict) or t.get("id") == tid:
            continue
        via = []
        if tid in (t.get("depends_on") or []):
            via.append("depends_on")
        if tid in (t.get("blocked_by") or []):
            via.append("blocked_by")
        snippets = []
        f = t.get("file")
        if f:
            p = Path(repo) / PROJECT_DIR / f
            try:
                head = read_text(p).split("\n## ", 1)[0]
                for line in head.splitlines():
                    if tid in line and ("execution_dependency" in line or "evidence_dependency" in line):
                        via.append("ticket_header")
                        snippets.append(line.strip()[:240])
                    if len(snippets) >= 3:
                        break
            except OSError:
                unreadable.append(t.get("id"))
        if via:
            direct.append({"id": t.get("id"), "lifecycle": t.get("lifecycle"), "via": sorted(set(via)), "snippets": snippets})
    if unreadable:
        return {"status": "UNKNOWN", "reason": "unreadable_ticket_files:" + ",".join(map(str, unreadable)), "direct": direct}
    return {"status": "CHECKED", "reason": "", "direct": direct}


# --------------------------------------------------------------------------- sweep
def is_link(path):
    try:
        st = os.lstat(path)
    except OSError:
        return True
    if stat.S_ISLNK(st.st_mode):
        return True
    return bool(getattr(st, "st_file_attributes", 0) & FILE_ATTRIBUTE_REPARSE_POINT)


def newest_mtime(path, limit=MAX_CANDIDATE_ENTRIES):
    newest = os.lstat(path).st_mtime
    stack = [path]
    count = 0
    while stack:
        cur = stack.pop()
        try:
            with os.scandir(cur) as it:
                for e in it:
                    count += 1
                    if count > limit:
                        return newest, count, True
                    try:
                        st = os.lstat(e.path)
                    except OSError:
                        continue
                    newest = max(newest, st.st_mtime)
                    if e.is_dir(follow_symlinks=False) and not is_link(e.path):
                        stack.append(e.path)
        except OSError:
            continue
    return newest, count, False


def remove_link(path):
    try:
        os.unlink(path)
    except (IsADirectoryError, PermissionError, OSError):
        os.rmdir(path)


def remove_tree(path, errors):
    try:
        entries = list(os.scandir(path))
    except OSError as e:
        errors.append("%s: %s" % (path, type(e).__name__))
        return False
    ok = True
    for e in entries:
        p = e.path
        try:
            if is_link(p):
                remove_link(p)
            elif e.is_dir(follow_symlinks=False):
                if not remove_tree(p, errors):
                    ok = False
            else:
                try:
                    os.unlink(p)
                except PermissionError:
                    os.chmod(p, stat.S_IWRITE)
                    os.unlink(p)
        except OSError as ex:
            errors.append("%s: %s" % (p, type(ex).__name__))
            ok = False
    try:
        os.rmdir(path)
    except OSError as ex:
        errors.append("%s: %s" % (path, type(ex).__name__))
        ok = False
    return ok


def do_sweep(root, days, exclude, max_candidates, now):
    result = {"root": posix(root), "removed": [], "kept": 0, "skipped": [], "errors": [], "max": max_candidates}
    try:
        root = Path(root)
        if not root.is_dir() or is_link(root):
            result["skipped"].append("root_not_a_plain_directory")
            return result
        excl = os.path.normcase(os.path.abspath(exclude)) if exclude else None
        handled = 0
        for e in sorted(os.scandir(root), key=lambda x: x.name):
            if not TICKET_ID_RE.match(e.name):
                continue
            if not e.is_dir(follow_symlinks=False) or is_link(e.path):
                result["skipped"].append(e.name + ":link_or_not_dir")
                continue
            if excl and os.path.normcase(os.path.abspath(e.path)) == excl:
                result["skipped"].append(e.name + ":excluded")
                continue
            if handled >= max_candidates:
                result["skipped"].append(e.name + ":over_max")
                continue
            handled += 1
            newest, _count, exceeded = newest_mtime(e.path)
            if exceeded:
                result["skipped"].append(e.name + ":too_many_entries")
                continue
            if now - newest > days * 86400:
                if remove_tree(e.path, result["errors"]):
                    result["removed"].append(e.name)
            else:
                result["kept"] += 1
    except Exception as ex:  # cleanup must never block the main flow
        result["errors"].append("sweep:%s" % type(ex).__name__)
    return result


# --------------------------------------------------------------------------- preflight
def build_preflight(repo, tid, reg, dep_path, now):
    reg = reg or {}
    loc = locate_ticket(repo, tid, reg)
    lifecycle = reg.get("lifecycle")
    pre = {"ticket": tid, "lifecycle": lifecycle, "ticket_file": loc["path"], "location": loc["location"],
           "locate": loc, "ts": int(now)}
    text = None
    if loc["status"] == "found":
        try:
            text = read_text(Path(repo) / loc["path"])
        except OSError:
            loc["status"] = "missing"
    cfg = parse_verify_config(text) if text else {}
    allowed = split_tokens(cfg.get("ALLOWED"))
    forbidden = split_tokens(cfg.get("FORBIDDEN"))
    pre["verify"] = {"preset": cfg.get("PRESET"), "allowed": allowed, "forbidden": forbidden,
                     "diff_ref": cfg.get("DIFF_REF"), "base_commit": cfg.get("BASE_COMMIT"),
                     "introduced_capability": cfg.get("INTRODUCED_CAPABILITY")}
    acs = parse_acs(text) if text else []
    pre["acs_status"] = "OK" if acs else "UNAVAILABLE"
    pre["acs"] = acs
    pre["final_result_present"] = bool(text and MARK_BEGIN in text)
    file_tid = cfg.get("TICKET_ID")
    id_ok = not file_tid or file_tid == tid
    lc_ok = (lifecycle in ("REVIEW", "CLOSED") and loc["location"] in ("deliver", "deliver/archive")
             and not loc["pointer_conflict"] and id_ok)
    pre["consistency"] = {"status": "PASS" if lc_ok else "FAIL",
                          "detail": "lifecycle=%s location=%s pointer_conflict=%s ticket_file_id=%s"
                                    % (lifecycle, loc["location"], loc["pointer_conflict"], file_tid or "-")}
    ev = range_evidence(repo, tid, cfg, loc["path"], allowed, forbidden) if text else {
        "base": None, "impl_end": None, "binding_ref": None, "commits": [],
        "scope": {"status": "UNKNOWN", "reason": "no_ticket_file", "violations": []},
        "diff_check": {"status": "UNKNOWN", "reason": "no_ticket_file", "output_head": ""},
        "attribution": {}}
    pre.update(range=ev, scope=ev["scope"], diff_check=ev["diff_check"],
               impl_end=ev["impl_end"], base=ev["base"], binding_ref=ev["binding_ref"])
    pre["ledger"] = ledger_summary(repo, tid)
    cp = reg.get("checkpoint")
    pre["checkpoint"] = {"state": cp.get("state") if isinstance(cp, dict) else "none",
                         "summary": (cp.get("summary") if isinstance(cp, dict) else None)}
    pre["dependents"] = dependents_evidence(repo, tid, dep_path)
    return pre


def cmd_preflight(a):
    repo = Path(a.repo)
    tid = a.ticket
    if not TICKET_ID_RE.match(tid):
        return emit({"error": "invalid_ticket_id", "ticket": tid}, 2)
    reg, err = load_json(a.registry)
    if err:
        return emit({"error": "registry_" + err, "ticket": tid}, 2)
    if not isinstance(reg, dict) or reg.get("id") != tid:
        return emit({"refused": "registry_id_mismatch", "ticket": tid,
                     "registry_id": reg.get("id") if isinstance(reg, dict) else None}, 3)
    d = close_dir(repo, tid)
    d.mkdir(parents=True, exist_ok=True)
    pre = build_preflight(repo, tid, reg, a.dependents, a.now)
    with open(d / "preflight.json", "w", encoding="utf-8", newline="\n") as f:
        json.dump(pre, f, ensure_ascii=False, indent=1)
    sweep = do_sweep(repo / CLOSE_ROOT, 7, str(d), 200, a.now)
    summary = {
        "ticket": tid, "lifecycle": pre["lifecycle"], "ticket_file": pre["ticket_file"],
        "location": pre["location"], "file_field_stale": pre["locate"]["file_field_stale"],
        "locate_status": pre["locate"]["status"], "consistency": pre["consistency"],
        "scope": pre["scope"], "diff_check": pre["diff_check"], "base": pre["base"], "impl_end": pre["impl_end"],
        "acs_status": pre["acs_status"], "ac_count": len(pre["acs"]),
        "acs": [{k: x[k] for k in ("n", "checked", "struck", "types", "text_preview")} for x in pre["acs"]],
        "final_result_present": pre["final_result_present"], "checkpoint": pre["checkpoint"],
        "dependents": {"status": pre["dependents"]["status"], "count": len(pre["dependents"]["direct"]),
                       "reason": pre["dependents"]["reason"]},
        "ledger": pre["ledger"]["status"], "bundle": rel_to(repo, d / "preflight.json"),
        "sweep": {"removed": sweep["removed"], "errors": len(sweep["errors"])},
    }
    return emit(summary)


# --------------------------------------------------------------------------- reviewer
def classify(allowed):
    if not allowed:
        return "code"
    for tok in allowed:
        ext = os.path.splitext(tok)[1].lower()
        if ext not in DOC_EXT:
            return "code"
    return "docs"


def select_reviewer(allowed, sonnet_ok, codex_ok):
    cls = classify(allowed)
    primary = "sonnet" if cls == "docs" else "codex"
    fallback = "codex" if primary == "sonnet" else "sonnet"
    avail = {"sonnet": sonnet_ok, "codex": codex_ok}
    actual, used, limitation = primary, False, ""
    if not avail[primary]:
        if avail[fallback]:
            actual, used = fallback, True
            limitation = "primary reviewer %s unavailable; fell back to %s" % (primary, fallback)
        else:
            actual = "none"
            limitation = "no reviewer available (sonnet and codex both unavailable)"
    return {"class": cls, "primary": primary, "fallback": fallback, "actual": actual,
            "fallback_used": used, "limitation": limitation, "available": avail}


def availability(a):
    sonnet = True if a.sonnet_available is None else a.sonnet_available == 1
    codex = (shutil.which("codex") is not None) if a.codex_available is None else a.codex_available == 1
    return sonnet, codex


def cmd_reviewer(a):
    d = close_dir(a.repo, a.ticket)
    pre, err = load_json(d / "preflight.json")
    if err:
        return emit({"error": "preflight_" + err}, 3)
    sonnet, codex = availability(a)
    sel = select_reviewer(pre.get("verify", {}).get("allowed", []), sonnet, codex)
    if a.escalate:
        nums = [int(x) for x in a.escalate.split(",") if x.strip().isdigit()]
        other = {"sonnet": "codex", "codex": "sonnet"}.get(sel["actual"])
        if other and sel["available"][other]:
            sel["escalation"] = {"reviewer": other, "acs": nums}
        else:
            sel["escalation"] = {"reviewer": "none", "acs": nums,
                                 "limitation": "the other reviewer is unavailable or none was used"}
    return emit(sel)


# --------------------------------------------------------------------------- binding / read-back
def verify_binding(repo, tid, pre):
    d = close_dir(repo, tid)
    dec, err = load_json(d / "decision.json")
    if err:
        return False, "decision_" + err
    if dec.get("ticket") != tid:
        return False, "ticket_mismatch"
    if dec.get("next") != "CLOSE":
        return False, "decision_not_close"
    if dec.get("ticket_file") != pre.get("ticket_file") or not pre.get("ticket_file"):
        return False, "ticket_file_mismatch"
    ref = dec.get("binding_ref")
    if not ref or ref != pre.get("binding_ref"):
        return False, "binding_ref_mismatch"
    rc, _ = git(repo, "merge-base", "--is-ancestor", ref, "HEAD")
    if rc != 0:
        return False, "binding_ref_not_ancestor_of_head"
    return True, ""


def expected_reviews(repo, tid):
    d = close_dir(repo, tid)
    pre_reg, _ = load_json(d / "registry.json")
    dec, _ = load_json(d / "decision.json")
    exp = dict((pre_reg or {}).get("reviews") or {})
    patch = (dec or {}).get("reviews_patch") or {}
    for k, v in patch.items():
        if k not in exp or exp.get(k) in (None, ""):
            exp[k] = v
    return exp


def verify_readback(repo, tid, after):
    mism = []
    after = after or {}
    if after.get("id") != tid:
        mism.append("id=%r (expected %r)" % (after.get("id"), tid))
    if after.get("lifecycle") != "CLOSED":
        mism.append("lifecycle=%s (expected CLOSED)" % after.get("lifecycle"))
    loc = locate_ticket(repo, tid, after)
    if loc["status"] != "found":
        mism.append("ticket file not resolvable (%s)" % loc["status"])
    elif loc["location"] not in ("deliver", "deliver/archive"):
        mism.append("ticket file location=%s" % loc["location"])
    exp = expected_reviews(repo, tid)
    got = after.get("reviews") or {}
    for f in REVIEW_FIELDS:
        if f not in exp:
            mism.append("reviews.%s has no expected value" % f)
        elif got.get(f) != exp.get(f):
            mism.append("reviews.%s=%r (expected %r)" % (f, got.get(f), exp.get(f)))
    return (not mism), mism, exp, loc


def cmd_readback(a):
    after, err = load_json(a.registry)
    if err:
        return emit({"ok": False, "mismatches": ["registry_after_" + err]}, 0)
    ok, mism, exp, _loc = verify_readback(a.repo, a.ticket, after)
    return emit({"ok": ok, "mismatches": mism, "expected_reviews": exp})


# --------------------------------------------------------------------------- decide
def reviews_patch(reg, verdicts):
    cur = (reg or {}).get("reviews") or {}
    obs = (verdicts or {}).get("reviews_observed") or {}
    patch, na = {}, []
    for f in REVIEW_FIELDS:
        if cur.get(f) not in (None, ""):
            continue
        o = obs.get(f) if isinstance(obs, dict) else None
        if isinstance(o, dict) and o.get("value") in REVIEW_ENUM and str(o.get("evidence") or "").strip():
            patch[f] = o["value"]
        else:
            patch[f] = "N_A"
            na.append(f)
    return patch, na


def validate_verdicts(verdicts, n_acs):
    entries = (verdicts or {}).get("acs")
    if not isinstance(entries, list):
        entries = []
    seen, dup, unknown, invalid = {}, [], [], []
    for e in entries:
        if not isinstance(e, dict):
            invalid.append(None)
            continue
        n = e.get("n")
        if not isinstance(n, int) or isinstance(n, bool) or n < 1 or n > n_acs:
            unknown.append(n)
            continue
        if e.get("status") not in AC_STATUS or not isinstance(e.get("core"), bool):
            invalid.append(n)
        if n in seen:
            dup.append(n)
        seen[n] = e
    missing = [n for n in range(1, n_acs + 1) if n not in seen]
    return seen, {"missing": missing, "duplicate": sorted(set(dup)), "unknown": unknown, "invalid": invalid}


def cmd_decide(a):
    repo, tid = a.repo, a.ticket
    d = close_dir(repo, tid)
    reg, rerr = load_json(a.registry)
    pre, perr = load_json(d / "preflight.json")
    if rerr or perr or not isinstance(pre, dict):
        return emit({"next": "PRECONDITION_FAILED", "reason": "registry_or_preflight_unavailable",
                     "detail": [rerr, perr]})
    if not isinstance(reg, dict) or reg.get("id") != tid or pre.get("ticket") != tid:
        return emit({"next": "PRECONDITION_FAILED", "reason": "registry_id_mismatch",
                     "stop": "STOP_EVIDENCE_CONFLICT"})
    lifecycle = (reg or {}).get("lifecycle")
    loc = pre.get("locate", {})
    # P
    if lifecycle not in ("REVIEW", "CLOSED"):
        return emit({"next": "PRECONDITION_FAILED", "reason": "lifecycle=%s" % lifecycle})
    if loc.get("status") == "ambiguous":
        return emit({"next": "PRECONDITION_FAILED", "reason": "ticket_file_ambiguous", "stop": "STOP_EVIDENCE_CONFLICT"})
    if loc.get("status") != "found":
        return emit({"next": "PRECONDITION_FAILED", "reason": "ticket_file_missing"})
    if pre.get("consistency", {}).get("status") != "PASS":
        return emit({"next": "PRECONDITION_FAILED", "reason": "location_conflict:" + pre["consistency"]["detail"],
                     "stop": "STOP_EVIDENCE_CONFLICT"})
    # C
    if lifecycle == "CLOSED":
        if pre.get("final_result_present"):
            return emit({"next": "ALREADY_CLOSED", "reason": "final_result_present"})
        ok, why = verify_binding(repo, tid, pre)
        if ok:
            return emit({"next": "WRITE_FINAL_ONLY", "reason": "partial_success_of_this_close"})
        return emit({"next": "ALREADY_CLOSED",
                     "reason": "historical_or_unconfirmed_partial_success_no_final_result_not_modified",
                     "binding_check": why})
    # REVIEW path
    verdicts, _verr = load_json(a.verdicts)
    verdicts = verdicts if isinstance(verdicts, dict) else {}
    n_acs = len(pre.get("acs", []))
    seen, problems = validate_verdicts(verdicts, n_acs)
    struck = {x["n"]: x["struck"] for x in pre.get("acs", [])}
    downgraded = []
    for n, e in seen.items():
        if e.get("status") == "N/A" and not struck.get(n):
            e["status"] = "UNVERIFIED"
            downgraded.append(n)
    rnd, fb = a.round, a.fallback_tried
    dep = (verdicts.get("dependency_contract") or {}).get("status")
    if dep not in DEP_STATUS:
        dep = "UNKNOWN"
    if dep == "NONE_FOUND" and pre.get("dependents", {}).get("status") != "CHECKED":
        dep = "UNKNOWN"
    scope_st = pre.get("scope", {}).get("status")
    dc_st = pre.get("diff_check", {}).get("status")
    reviewer_actual = (verdicts.get("reviewer") or {}).get("actual")
    sonnet, codex = availability(a)

    def finish(obj, save=True):
        obj.setdefault("downgraded_na", downgraded)
        if save:
            rec = {"ticket": tid, "ticket_file": pre.get("ticket_file"), "binding_ref": pre.get("binding_ref"),
                   "next": obj["next"], "ts": int(a.now)}
            if obj["next"] == "CLOSE":
                rec["reviews_patch"] = obj.get("reviews_patch", {})
                rec["reviews_na_fields"] = obj.get("reviews_na_fields", [])
            try:
                d.mkdir(parents=True, exist_ok=True)
                with open(d / "decision.json", "w", encoding="utf-8", newline="\n") as f:
                    json.dump(rec, f, ensure_ascii=False, indent=1)
            except OSError:
                obj["decision_saved"] = False
        return emit(obj)

    core = {n: e for n, e in seen.items() if e.get("core") is True and e.get("status") in AC_STATUS}
    # B
    if any(e["status"] == "FAIL" for e in core.values()):
        return finish({"next": "BLOCKED", "kind": "core_ac_fail",
                       "acs": sorted(n for n, e in core.items() if e["status"] == "FAIL"), "stop": "STOP_DECISION_REQUIRED"})
    if verdicts.get("security_issue") is True:
        return finish({"next": "BLOCKED", "kind": "security", "stop": "STOP_DECISION_REQUIRED"})
    if verdicts.get("irreversible_risk") is True:
        return finish({"next": "BLOCKED", "kind": "irreversible", "stop": "STOP_DECISION_REQUIRED"})
    if dep == "MISSING":
        return finish({"next": "BLOCKED", "kind": "dependency_contract", "stop": "STOP_DECISION_REQUIRED"})
    # V
    if pre.get("acs_status") == "UNAVAILABLE":
        return finish({"next": "NOT_CLOSED", "reason": "ac_list_unavailable"})
    if problems["missing"] or problems["duplicate"] or problems["unknown"] or problems["invalid"] or not verdicts.get("acs"):
        if rnd < MAX_ROUNDS:
            return finish({"next": "SUPPLEMENT", "reason": "verdicts_incomplete", **problems})
        return finish({"next": "NOT_CLOSED", "reason": "verdicts_incomplete", **problems})
    # X
    if scope_st == "FAIL" or dc_st == "FAIL":
        if rnd < MAX_ROUNDS:
            return finish({"next": "RECONCILE", "reason": "mechanical_fail_without_core_fail_verdict",
                           "scope": scope_st, "diff_check": dc_st})
        return finish({"next": "NOT_CLOSED", "reason": "reconcile_unresolved"})
    if (scope_st == "UNKNOWN" or dc_st == "UNKNOWN") and rnd < MAX_ROUNDS:
        return finish({"next": "SUPPLEMENT", "reason": "scope_unknown",
                       "scope": pre.get("scope"), "diff_check": pre.get("diff_check")})
    # U
    unv = sorted(n for n, e in core.items() if e["status"] == "UNVERIFIED")
    if unv:
        if rnd < MAX_ROUNDS:
            return finish({"next": "SUPPLEMENT", "reason": "core_unverified", "acs": unv})
        other = {"sonnet": "codex", "codex": "sonnet"}.get(reviewer_actual)
        other_ok = other is not None and {"sonnet": sonnet, "codex": codex}[other]
        if fb == 0 and other_ok:
            return finish({"next": "FALLBACK_REVIEWER", "reviewer": other, "acs": unv})
        return finish({"next": "NOT_CLOSED", "reason": "core_unverified", "acs": unv})
    # L
    if dep == "LEAD":
        if rnd < MAX_ROUNDS:
            return finish({"next": "SUPPLEMENT", "reason": "dependency_lead"})
        return finish({"next": "NOT_CLOSED", "reason": "dependency_lead_unresolved"})
    # K
    patch, na = reviews_patch(reg, verdicts)
    cp_state = pre.get("checkpoint", {}).get("state")
    base = {"reviews_patch": patch, "reviews_na_fields": na, "dependency_status": dep}
    if cp_state == "OPEN":
        if verdicts.get("checkpoint_matter_resolved") is True:
            return finish({"next": "CLOSE", "checkpoint_action": "resolve", **base})
        return finish({"next": "NOT_CLOSED", "reason": "checkpoint_unresolved"})
    return finish({"next": "CLOSE", "checkpoint_action": "none", **base})


# --------------------------------------------------------------------------- finalize
def build_result_section(pre, reg_after, verdicts, summary_lines, commits, now, na_fields):
    accs = {e["n"]: e for e in (verdicts.get("acs") or []) if isinstance(e, dict) and isinstance(e.get("n"), int)}
    struck = {x["n"]: x["struck"] for x in pre.get("acs", [])}
    core_n = sum(1 for e in accs.values() if e.get("core"))
    nc = {"PASS": 0, "N/A": 0, "UNVERIFIED": 0}
    unv_nc = []
    for n, e in accs.items():
        if e.get("core"):
            continue
        st = e.get("status")
        if st == "N/A" and not struck.get(n):
            st = "UNVERIFIED"
        if st in nc:
            nc[st] += 1
        if st == "UNVERIFIED":
            unv_nc.append(n)
    rev = verdicts.get("reviewer") or {}
    date = datetime.fromtimestamp(now).strftime("%Y-%m-%d")
    lines = [MARK_BEGIN, "## 收束結果（close）"]
    lines.append("- 交付摘要: " + " / ".join(summary_lines[:3]))
    lines.append("- 最終驗收結論: 核心 AC %d 條皆為 PASS 或有效 N/A；非核心 PASS %d／N/A %d／UNVERIFIED %d"
                 % (core_n, nc["PASS"], nc["N/A"], nc["UNVERIFIED"]))
    lines.append("- 實作提交: " + (", ".join(c for c in commits if c) or "（無可考本票實作提交）"))
    lines.append("- 實際收束結果: %s 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: %s%s"
                 % (date, rev.get("actual") or "未記錄", "（fallback）" if rev.get("fallback_used") else ""))
    disc = []
    if unv_nc:
        disc.append("非核心 UNVERIFIED AC: " + ",".join(map(str, sorted(unv_nc))))
    dep = (verdicts.get("dependency_contract") or {}).get("status")
    if dep in ("UNKNOWN", "LEAD") or (dep == "NONE_FOUND" and pre.get("dependents", {}).get("status") != "CHECKED"):
        disc.append("dependency contract: 未知（未宣稱已確認無缺件）")
    if (reg_after.get("reviews") or {}).get("traycer") == "PENDING":
        disc.append("Traycer／T2 可選外審未執行")
    if na_fields:
        disc.append("reviews 缺漏欄位補 N_A（無可考記錄）: " + ",".join(na_fields))
    if rev.get("limitation"):
        disc.append("reviewer limitation: " + str(rev["limitation"])[:120])
    if pre.get("scope", {}).get("status") == "UNKNOWN":
        disc.append("實作 diff 範圍為 UNKNOWN: " + str(pre["scope"].get("reason"))[:100])
    if pre.get("diff_check", {}).get("status") == "UNKNOWN":
        disc.append("git diff --check 為 UNKNOWN: " + str(pre["diff_check"].get("reason"))[:100])
    if disc:
        lines.append("- 披露: " + "；".join(disc[:5]))
    lines.append(MARK_END)
    return lines[:20]


def upsert_section(text, lines):
    block = "\n".join(lines) + "\n"
    i = text.find(MARK_BEGIN)
    j = text.find(MARK_END)
    if i != -1 and j != -1 and j > i:
        end = j + len(MARK_END)
        if end < len(text) and text[end] == "\n":
            end += 1
        return text[:i] + block + text[end:], True
    if not text.endswith("\n"):
        text += "\n"
    return text + "\n" + block, False


def cmd_finalize(a):
    repo, tid = a.repo, a.ticket
    d = close_dir(repo, tid)
    after, err = load_json(a.registry)
    if err:
        raise Refuse({"refused": "registry_after_" + err})
    ok, mism, _exp, _loc = verify_readback(repo, tid, after)
    if not ok:
        raise Refuse({"refused": "readback_mismatch", "mismatches": mism})
    pre, perr = load_json(d / "preflight.json")
    if perr:
        raise Refuse({"refused": "preflight_" + perr})
    bok, why = verify_binding(repo, tid, pre)
    if not bok:
        raise Refuse({"refused": "binding_check_failed", "reason": why})
    verdicts, _ = load_json(a.verdicts)
    verdicts = verdicts if isinstance(verdicts, dict) else {}
    try:
        summary_lines = [ln.strip() for ln in read_text(a.summary).splitlines() if ln.strip()][:3]
    except OSError:
        summary_lines = []
    if not summary_lines:
        raise Refuse({"refused": "summary_missing_or_empty"})
    dec, _ = load_json(d / "decision.json")
    na = list((dec or {}).get("reviews_na_fields") or [])
    commits = [c.strip() for c in (a.commits or "").split(",") if c.strip()]
    lines = build_result_section(pre, after, verdicts, summary_lines, commits, a.now, na)
    path = Path(repo) / pre["ticket_file"]
    text = read_text(path)
    new_text, replaced = upsert_section(text, lines)
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(new_text)
    return emit({"written": True, "replaced": replaced, "lines": len(lines), "ticket_file": pre["ticket_file"]})


# --------------------------------------------------------------------------- sweep command
def cmd_sweep(a):
    root = a.root or str(Path(a.repo) / CLOSE_ROOT)
    return emit(do_sweep(root, a.days, a.exclude, a.max, a.now))


# --------------------------------------------------------------------------- main
def build_parser():
    p = argparse.ArgumentParser(prog="close_helper.py", description=__doc__.split("\n", 1)[0])
    p.add_argument("--repo", default=None)
    p.add_argument("--now", type=float, default=None)
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("preflight")
    s.add_argument("--ticket", required=True)
    s.add_argument("--registry", required=True)
    s.add_argument("--dependents")

    s = sub.add_parser("reviewer")
    s.add_argument("--ticket", required=True)
    s.add_argument("--sonnet-available", type=int, choices=(0, 1))
    s.add_argument("--codex-available", type=int, choices=(0, 1))
    s.add_argument("--escalate")

    s = sub.add_parser("decide")
    s.add_argument("--ticket", required=True)
    s.add_argument("--registry", required=True)
    s.add_argument("--verdicts", required=True)
    s.add_argument("--round", type=int, required=True)
    s.add_argument("--fallback-tried", type=int, choices=(0, 1), required=True)
    s.add_argument("--sonnet-available", type=int, choices=(0, 1))
    s.add_argument("--codex-available", type=int, choices=(0, 1))

    s = sub.add_parser("readback")
    s.add_argument("--ticket", required=True)
    s.add_argument("--registry", required=True)

    s = sub.add_parser("finalize")
    s.add_argument("--ticket", required=True)
    s.add_argument("--registry", required=True)
    s.add_argument("--verdicts", required=True)
    s.add_argument("--summary", required=True)
    s.add_argument("--commits", default="")

    s = sub.add_parser("sweep")
    s.add_argument("--root")
    s.add_argument("--days", type=float, default=7)
    s.add_argument("--exclude")
    s.add_argument("--max", type=int, default=200)
    return p


def main(argv=None):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, ValueError):
        pass
    a = build_parser().parse_args(argv)
    if a.repo is None:
        rc, out = git(os.getcwd(), "rev-parse", "--show-toplevel")
        a.repo = out.strip() if rc == 0 and out.strip() else os.getcwd()
    a.now = a.now if a.now is not None else time.time()
    handler = {"preflight": cmd_preflight, "reviewer": cmd_reviewer, "decide": cmd_decide,
               "readback": cmd_readback, "finalize": cmd_finalize, "sweep": cmd_sweep}[a.cmd]
    try:
        return handler(a)
    except Refuse as r:
        emit(r.payload, 3)
        return 3


if __name__ == "__main__":
    sys.exit(main())
