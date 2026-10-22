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
  reviewer  --ticket ID [--codex-fault KIND] [--codex-available 0|1] [--claude-available 0|1] [--force-heavy] [--simple-basis TEXT] [--escalate n1,n2]
  decide    --ticket ID --registry F --verdicts F --reply F [--reply F2] --round N --fallback-tried 0|1
  readback  --ticket ID --registry F
  finalize  --ticket ID --registry F --summary F --commits h1,h2   (reads only the decision.json snapshot; --verdicts is accepted and ignored)
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
# Known load-bearing locations: fixed paths / file-name tokens only (no content keywords, no classifier).
LOAD_BEARING_DIRS = (".claude/", "plans/architecture/", "plans/cso_briefing/project/file/")
LOAD_BEARING_NAME_RE = re.compile(r"ssot|contract", re.I)
CORE_MARK_RE = re.compile(r"^core[ \t]*=[ \t]*([YyNn])\b[ \t]*(?:\|[ \t]*)?(.*)$", re.S)
TIER_UPGRADE_RE = re.compile(r"^[ \t]*(?:[-*][ \t]+)?\**[ \t]*TIER[ \t]*\**[ \t]*[:\uff1a][ \t]*\**[ \t]*UPGRADE\b", re.I | re.M)
CODEX_HEAVY = "gpt-6-sol"
CODEX_LIGHT = "gpt-6-luna"
CLAUDE_FALLBACK = {"heavy": "opus", "light": "sonnet"}
CODEX_FAULTS = ("auth", "network", "quota", "unavailable", "model_unavailable", "recovery_exhausted")
AC_REPLY_RE = re.compile(
    r"^[ \t]*(?:[-*][ \t]+)?\**[ \t]*AC[ \t]*(\d+)[ \t]*\**[ \t]*[:\uff1a][ \t]*\**[ \t]*(PASS|FAIL|N/A|UNVERIFIED)\b[ \t]*\**[ \t]*(?:\|[ \t]*([^\n]*))?",
    re.I | re.M)
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
    pre["final_result_present"] = bool(text and find_section(text))
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
    """Structural precondition for the light tier only: every ALLOWED file is plain text outside the known
    load-bearing locations. Never sufficient by itself (see select_reviewer: an explicit basis is also required)."""
    if not allowed:
        return "heavy"
    for tok in allowed:
        t = posix(tok)
        if os.path.splitext(t)[1].lower() not in DOC_EXT or t.startswith(".claude/") or "/.claude/" in t:
            return "heavy"
        if any(t.startswith(d) or ("/" + d) in t for d in LOAD_BEARING_DIRS):
            return "heavy"
        if LOAD_BEARING_NAME_RE.search(os.path.basename(t)):
            return "heavy"
    return "light"


def select_reviewer(allowed, codex_fault, codex_ok, claude_ok, force_heavy=False, simple_basis=""):
    basis = (simple_basis or "").strip()
    structural = classify(allowed)
    if force_heavy:
        tier, why = "heavy", "default heavy: force-heavy"
    elif structural != "light":
        tier, why = "heavy", "default heavy: load-bearing or non-text scope"
    elif not basis:
        tier, why = "heavy", "default heavy: no explicit simple basis"
    else:
        tier, why = "light", basis[:80]
    model = CODEX_HEAVY if tier == "heavy" else CODEX_LIGHT
    primary = {"family": "codex", "model": model}
    fault = codex_fault or ("" if codex_ok else "unavailable")
    res = {"class": tier, "tier_basis": why, "primary": primary, "actual": dict(primary), "fallback_used": False,
           "fallback_reason": "", "escalate_to": CODEX_HEAVY if model == CODEX_LIGHT else "none", "limitation": ""}
    if fault:
        res["fallback_reason"] = fault
        cm = CLAUDE_FALLBACK[tier]
        if claude_ok:
            res["actual"] = {"family": "claude", "model": cm}
            res["fallback_used"] = True
            res["limitation"] = ("Codex fault (%s); a separate Claude reviewer (%s) is used; "
                                 "this is not Codex or cross-model verification" % (fault, cm))
        else:
            res["actual"] = {"family": "none", "model": "none"}
            res["limitation"] = "Codex fault (%s) and no Claude reviewer available" % fault
    return res


def cmd_reviewer(a):
    d = close_dir(a.repo, a.ticket)
    pre, err = load_json(d / "preflight.json")
    if err:
        return emit({"error": "preflight_" + err}, 3)
    if a.codex_fault and a.codex_fault not in CODEX_FAULTS:
        return emit({"error": "invalid_codex_fault", "allowed": list(CODEX_FAULTS)}, 2)
    codex_ok = (shutil.which("codex") is not None) if a.codex_available is None else a.codex_available == 1
    claude_ok = True if a.claude_available is None else a.claude_available == 1
    sel = select_reviewer(pre.get("verify", {}).get("allowed", []), a.codex_fault or "", codex_ok, claude_ok,
                          bool(a.force_heavy), a.simple_basis or "")
    if a.escalate:
        nums = [int(x) for x in a.escalate.split(",") if x.strip().isdigit()]
        act = sel["actual"]
        if act["family"] == "codex" and act["model"] == CODEX_LIGHT:
            sel["escalation"] = {"reviewer": CODEX_HEAVY, "acs": nums}
        else:
            sel["escalation"] = {"reviewer": "none", "acs": nums,
                                 "limitation": "no stronger reviewer than the one in use (or none was used)"}
    return emit(sel)


# --------------------------------------------------------------------------- binding / read-back
def reviewer_record(rev, basis):
    """The single producer of the reviewer fields kept in the decision snapshot."""
    rev = rev if isinstance(rev, dict) else {}
    return {"family": rev.get("family"), "actual_model": rev.get("actual_model"), "tier": rev.get("tier"),
            "fallback_used": rev.get("fallback_used"), "fallback_reason": rev.get("fallback_reason") or "",
            "independent_call": rev.get("independent_call"), "inline": rev.get("inline"),
            "limitation": str(rev.get("limitation") or "")[:200], "tier_basis": (basis or "")[:80]}


def build_snapshot(seen, eff, ev, n_acs, rev, dep, coverage):
    basis = str(rev.get("tier_basis") or "").strip() if isinstance(rev, dict) else ""
    acs = [{"n": n, "status": seen[n]["status"], "core": bool(eff.get(n, True)), "ev": (ev.get(n) or "")[:80]}
           for n in range(1, n_acs + 1)]
    return {"acs": acs, "reviewer": reviewer_record(rev, basis), "dependency_status": dep, "coverage": coverage}


def snapshot_ok(dec, n_acs):
    snap = (dec or {}).get("snapshot")
    if not isinstance(snap, dict):
        return False, "snapshot_missing"
    acs = snap.get("acs")
    if not isinstance(acs, list) or not n_acs or len(acs) != n_acs:
        return False, "snapshot_ac_count_mismatch"
    seen = set()
    for e in acs:
        if not isinstance(e, dict):
            return False, "snapshot_ac_invalid"
        n = e.get("n")
        if not isinstance(n, int) or isinstance(n, bool) or n < 1 or n > n_acs or n in seen:
            return False, "snapshot_ac_numbers_invalid"
        seen.add(n)
        if e.get("status") not in AC_STATUS or not isinstance(e.get("core"), bool):
            return False, "snapshot_ac_invalid"
        if e["core"] and e["status"] in ("FAIL", "UNVERIFIED"):
            return False, "snapshot_core_not_passing"
    rev = snap.get("reviewer")
    tier = rev.get("tier") if isinstance(rev, dict) else None
    if tier not in ("heavy", "light"):
        return False, "snapshot_reviewer_invalid"
    bad = validate_reviewer_meta(rev, tier)  # the same check decide applies to the live reviewer record
    if bad:
        return False, "snapshot_" + bad
    return True, ""


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
    return snapshot_ok(dec, len(pre.get("acs") or []))


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


def parse_reviewer_replies(paths, n_acs, fresh_after=None):
    """Parse the reviewer's raw reply files (later files override earlier ones for the ACs they cover).

    One line format: `AC<n>: PASS|FAIL|N/A|UNVERIFIED | core=Y|N | <short evidence>`. The core marker is optional for
    backward compatibility and is never evidence: once it is stripped, an empty evidence leaves the AC uncovered.
    A reply file older than `fresh_after` (the current preflight.json) judged another delivery version: not used.
    Returns (status, problems, info) with info = {core: {n: 'Y'|'N'}, ev: {n: short text}, upgrade: bool}."""
    status, dup, unknown, unreadable, no_evidence, stale = {}, [], [], [], [], []
    info = {"core": {}, "ev": {}, "upgrade": False}
    for p in paths or []:
        try:
            if fresh_after is not None and Path(p).stat().st_mtime < fresh_after:
                stale.append(posix(p))
                continue
            text = read_text(p)
        except OSError:
            unreadable.append(posix(p))
            continue
        if TIER_UPGRADE_RE.search(text):  # any fresh reply file: a Luna point re-check may carry it too (decide gates on fb=0 + Luna)
            info["upgrade"] = True
        here = set()
        for m in AC_REPLY_RE.finditer(text):
            n, st = int(m.group(1)), m.group(2).upper()
            if n < 1 or n > n_acs:
                unknown.append(n)
                continue
            if n in here:
                dup.append(n)
                continue
            here.add(n)
            ev = (m.group(3) or "").strip(" \t*")
            mark = None
            cm = CORE_MARK_RE.match(ev)
            if cm:
                mark, ev = cm.group(1).upper(), cm.group(2).strip(" \t*|")
            if not ev:
                no_evidence.append(n)  # a status or marker without evidence is not a judgement: the AC stays uncovered
                continue
            status[n] = st
            if mark:
                info["core"][n] = mark
            else:
                info["core"].pop(n, None)  # a later reply without a marker supersedes an earlier marker
            info["ev"][n] = re.sub(r"\s+", " ", ev)[:80]
    missing = [n for n in range(1, n_acs + 1) if n not in status]
    return status, {"missing": missing, "duplicate": sorted(set(dup)), "unknown": sorted(set(unknown)),
                    "unreadable": unreadable, "no_evidence": sorted(set(no_evidence)), "stale": stale}, info


def validate_reviewer_meta(rev, tier):
    """Return None when the reviewer record is usable, else a short code."""
    if not isinstance(rev, dict):
        return "reviewer_meta_missing"
    fam, model = rev.get("family"), rev.get("actual_model")
    if fam not in ("codex", "claude") or not isinstance(model, str) or not model.strip():
        return "reviewer_meta_invalid"
    if not isinstance(rev.get("inline"), bool) or not isinstance(rev.get("independent_call"), bool):
        return "reviewer_meta_invalid"
    if rev["inline"] or not rev["independent_call"]:
        return "reviewer_not_independent"
    if fam == "codex":
        if model not in (CODEX_HEAVY, CODEX_LIGHT):
            return "reviewer_model_unknown"
        if tier == "heavy" and model == CODEX_LIGHT:
            return "reviewer_tier_mismatch"
    else:
        if rev.get("fallback_used") is not True or rev.get("fallback_reason") not in CODEX_FAULTS:
            return "reviewer_fallback_unjustified"
        if model != CLAUDE_FALLBACK[tier]:
            return "reviewer_model_unknown"
    return None


def live_binding_ref(repo, tid, pre):
    """Re-derive the delivery version (binding_ref) the way preflight did, from the live repository."""
    ticket_rel = pre.get("ticket_file")
    if not ticket_rel:
        return pre.get("binding_ref")
    try:
        cfg = parse_verify_config(read_text(Path(repo) / ticket_rel))
    except OSError:
        return pre.get("binding_ref")
    v = pre.get("verify", {})
    return range_evidence(repo, tid, cfg, ticket_rel, v.get("allowed", []), v.get("forbidden", []))["binding_ref"]


def cmd_decide(a):
    repo, tid = a.repo, a.ticket
    d = close_dir(repo, tid)
    reg, rerr = load_json(a.registry)
    pre, perr = load_json(d / "preflight.json")
    if rerr or perr or not isinstance(pre, dict):
        return emit({"next": "PRECONDITION_FAILED", "read_only": True, "reason": "registry_or_preflight_unavailable",
                     "detail": [rerr, perr]})
    if not isinstance(reg, dict) or reg.get("id") != tid or pre.get("ticket") != tid:
        return emit({"next": "PRECONDITION_FAILED", "read_only": True, "reason": "registry_id_mismatch",
                     "stop": "STOP_EVIDENCE_CONFLICT"})
    lifecycle = (reg or {}).get("lifecycle")
    loc = pre.get("locate", {})
    # P
    if lifecycle not in ("REVIEW", "CLOSED"):
        return emit({"next": "PRECONDITION_FAILED", "read_only": True, "reason": "lifecycle=%s" % lifecycle})
    if loc.get("status") == "ambiguous":
        return emit({"next": "PRECONDITION_FAILED", "read_only": True, "reason": "ticket_file_ambiguous", "stop": "STOP_EVIDENCE_CONFLICT"})
    if loc.get("status") != "found":
        return emit({"next": "PRECONDITION_FAILED", "read_only": True, "reason": "ticket_file_missing"})
    if pre.get("consistency", {}).get("status") != "PASS":
        return emit({"next": "PRECONDITION_FAILED", "read_only": True, "reason": "location_conflict:" + pre["consistency"]["detail"],
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
    claimed = {n: e.get("status") for n, e in seen.items()}
    try:
        fresh_after = (d / "preflight.json").stat().st_mtime
    except OSError:
        fresh_after = None
    reply_status, rprob, rinfo = parse_reviewer_replies(a.reply, n_acs, fresh_after)
    mism = []
    for n, e in seen.items():
        if n in reply_status and claimed[n] != reply_status[n]:
            mism.append(n)
            e["status"] = reply_status[n]  # judged on the reviewer's reply; free text carries no override authority
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
    rev_in = verdicts.get("reviewer") if isinstance(verdicts.get("reviewer"), dict) else {}
    basis = str(rev_in.get("tier_basis") or "").strip()
    # light needs the structural precondition AND an explicit basis; the Agent may raise the tier, never lower it
    tier = "light" if (classify(pre.get("verify", {}).get("allowed", [])) == "light" and basis) else "heavy"
    if rev_in.get("tier") == "heavy":
        tier = "heavy"
    reviewer_model = rev_in.get("actual_model") if rev_in else None
    meta_bad = validate_reviewer_meta(verdicts.get("reviewer"), tier)
    # effective core, recomputed every round: claimed core OR not explicitly confirmed non-core by the reviewer
    eff, dis = {}, []
    for n, e in seen.items():
        if e.get("status") not in AC_STATUS or not isinstance(e.get("core"), bool):
            continue
        claim, mk = e["core"], rinfo["core"].get(n)
        eff[n] = claim or (n in reply_status and mk != "N")  # an AC the reviewer did not judge is a coverage matter (R)
        if n in reply_status and e["status"] in ("FAIL", "UNVERIFIED") and ((claim and mk == "N") or (not claim and mk != "N")):
            dis.append(n)

    def finish(obj, save=True):
        obj.setdefault("downgraded_na", downgraded)
        if save:
            rec = {"ticket": tid, "ticket_file": pre.get("ticket_file"), "binding_ref": pre.get("binding_ref"),
                   "next": obj["next"], "ts": int(a.now)}
            if obj["next"] == "CLOSE":
                rec["reviews_patch"] = obj.get("reviews_patch", {})
                rec["reviews_na_fields"] = obj.get("reviews_na_fields", [])
                rec["coverage"] = {"covered": len(reply_status), "total": n_acs}
                rec["snapshot"] = build_snapshot(seen, eff, rinfo["ev"], n_acs, verdicts.get("reviewer"), dep, rec["coverage"])
            try:
                d.mkdir(parents=True, exist_ok=True)
                with open(d / "decision.json", "w", encoding="utf-8", newline="\n") as f:
                    json.dump(rec, f, ensure_ascii=False, indent=1)
            except OSError:
                obj["decision_saved"] = False
        return emit(obj)

    core = {n: e for n, e in seen.items() if eff.get(n) and e.get("status") in AC_STATUS}
    # S: the evidence bundle and every reply must describe the current delivery version
    live_ref = live_binding_ref(repo, tid, pre)
    if live_ref != pre.get("binding_ref"):
        if rnd < MAX_ROUNDS:
            return finish({"next": "SUPPLEMENT", "reason": "preflight_stale", "preflight_binding_ref": pre.get("binding_ref"),
                           "current_binding_ref": live_ref})
        return finish({"next": "NOT_CLOSED", "reason": "preflight_stale"})
    # T: a Luna reply that reports a load-bearing / tier mismatch is answered by the stronger reviewer
    if rinfo["upgrade"] and reviewer_model == CODEX_LIGHT and fb == 0 and not meta_bad:
        return finish({"next": "ESCALATE_REVIEWER", "reviewer": CODEX_HEAVY, "reason": "tier_upgrade"})
    # B
    hard = verdicts.get("security_issue") is True or verdicts.get("irreversible_risk") is True or dep == "MISSING"
    if dis and rnd < MAX_ROUNDS and not hard:
        return finish({"next": "SUPPLEMENT", "reason": "core_disagreement", "acs": sorted(dis)})
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
    # R: the reviewer must be a real second call and cover every AC (parsed from its raw reply files)
    cov_bad = bool(rprob["missing"] or rprob["duplicate"] or rprob["unknown"] or rprob["unreadable"] or rprob["stale"])
    if meta_bad or cov_bad or mism:
        if meta_bad:
            reason, term, detail = "reviewer_invalid", "reviewer_invalid", {"detail": meta_bad}
        elif rprob["stale"]:
            reason, term, detail = "reviewer_reply_stale", "reviewer_reply_stale", {"stale": rprob["stale"]}
        elif cov_bad:
            reason, term, detail = "reviewer_coverage", "reviewer_coverage_incomplete", dict(rprob)
        else:
            reason, term, detail = "reviewer_mismatch", "reviewer_mismatch", {"acs": sorted(mism)}
        if rnd < MAX_ROUNDS:
            return finish({"next": "SUPPLEMENT", "reason": reason, **detail})
        if reviewer_model == CODEX_LIGHT and fb == 0 and not meta_bad and not rprob["stale"]:
            return finish({"next": "ESCALATE_REVIEWER", "reviewer": CODEX_HEAVY, "reason": reason, **detail})
        return finish({"next": "NOT_CLOSED", "reason": term, **detail})
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
        if reviewer_model == CODEX_LIGHT and fb == 0:
            return finish({"next": "ESCALATE_REVIEWER", "reviewer": CODEX_HEAVY, "acs": unv})
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
def short_fact(ev, limit=40):
    s = re.sub(r"\s+", " ", str(ev or "")).strip()
    return s[:limit] if s else "未載明"


def build_result_section(pre, reg_after, snap, summary_lines, commits, now, na_fields, dec=None):
    """Only the validated decision snapshot feeds the result: nothing mutable (verdicts.json) is read."""
    accs = {e["n"]: e for e in snap["acs"]}
    core_n = sum(1 for e in accs.values() if e["core"])
    nc = {"PASS": 0, "N/A": 0, "UNVERIFIED": 0, "FAIL": 0}
    unv_nc, fail_nc = [], []
    for n in sorted(accs):
        e = accs[n]
        if e["core"]:
            continue
        nc[e["status"]] += 1
        if e["status"] == "UNVERIFIED":
            unv_nc.append(n)
        elif e["status"] == "FAIL":
            fail_nc.append(n)
    rev = snap.get("reviewer") or {}
    date = datetime.fromtimestamp(now).strftime("%Y-%m-%d")
    lines = [MARK_BEGIN, "## 收束結果（close）"]
    lines.append("- 交付摘要: " + " / ".join(summary_lines[:3]))
    lines.append("- 最終驗收結論: 核心 AC %d 條皆為 PASS 或有效 N/A；非核心 PASS %d／N/A %d／UNVERIFIED %d／FAIL %d（共 %d 條 AC）"
                 % (core_n, nc["PASS"], nc["N/A"], nc["UNVERIFIED"], nc["FAIL"], len(accs)))
    if fail_nc:  # required failure facts are never cut by a line or item cap
        lines.append("- 非核心 FAIL %d 條: " % len(fail_nc)
                     + "；".join("AC%d %s" % (n, short_fact(accs[n].get("ev"))) for n in fail_nc))
    lines.append("- 實作提交: " + (", ".join(c for c in commits if c) or "（無可考本票實作提交）"))
    cov = snap.get("coverage") or {}
    fb_txt = "，fallback:%s" % rev.get("fallback_reason") if rev.get("fallback_used") else ""
    lines.append("- 實際收束結果: %s 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: %s/%s（tier=%s%s）；覆蓋 AC %s／%s（全部），independent_call=%s"
                 % (date, rev.get("family") or "未記錄", rev.get("actual_model") or "未記錄", rev.get("tier") or "未記錄",
                    fb_txt, cov.get("covered", "未記錄"), cov.get("total", "未記錄"), rev.get("independent_call")))
    disc = []
    if unv_nc:
        disc.append("非核心 UNVERIFIED AC: " + ",".join(map(str, sorted(unv_nc))))
    dep = snap.get("dependency_status")
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
        extra = "（另 %d 項未列）" % (len(disc) - 5) if len(disc) > 5 else ""
        lines.append("- 披露: " + "；".join(disc[:5]) + extra)
    lines.append(MARK_END)
    return lines


def find_section(text):
    """Span of the whole-line close-result section, or None. Inline mentions of the marker are ignored."""
    begins = list(re.finditer(r"(?m)^" + re.escape(MARK_BEGIN) + r"[ \t]*\r?$", text))
    if not begins:
        return None
    b = begins[-1]
    e = re.compile(r"(?m)^" + re.escape(MARK_END) + r"[ \t]*\r?$").search(text, b.end())
    if not e:
        return None
    end = e.end()
    if text[end:end + 2] == "\r\n":
        end += 2
    elif text[end:end + 1] == "\n":
        end += 1
    return b.start(), end


def upsert_section(text, lines):
    eol = "\r\n" if "\r\n" in text else "\n"
    block = eol.join(lines) + eol
    span = find_section(text)
    if span:
        return text[:span[0]] + block + text[span[1]:], True
    if not text.endswith("\n"):
        text += eol
    return text + eol + block, False


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
        if why.startswith("snapshot_"):
            raise Refuse({"refused": "decision_snapshot_invalid", "reason": why})
        raise Refuse({"refused": "binding_check_failed", "reason": why})
    try:
        summary_lines = [ln.strip() for ln in read_text(a.summary).splitlines() if ln.strip()][:3]
    except OSError:
        summary_lines = []
    if not summary_lines:
        raise Refuse({"refused": "summary_missing_or_empty"})
    dec, _ = load_json(d / "decision.json")
    na = list((dec or {}).get("reviews_na_fields") or [])
    commits = [c.strip() for c in (a.commits or "").split(",") if c.strip()]
    lines = build_result_section(pre, after, dec["snapshot"], summary_lines, commits, a.now, na, dec)
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
    s.add_argument("--codex-fault")
    s.add_argument("--codex-available", type=int, choices=(0, 1))
    s.add_argument("--claude-available", type=int, choices=(0, 1))
    s.add_argument("--force-heavy", action="store_true")
    s.add_argument("--simple-basis", default="")
    s.add_argument("--escalate")

    s = sub.add_parser("decide")
    s.add_argument("--ticket", required=True)
    s.add_argument("--registry", required=True)
    s.add_argument("--verdicts", required=True)
    s.add_argument("--round", type=int, required=True)
    s.add_argument("--fallback-tried", type=int, choices=(0, 1), required=True)
    s.add_argument("--reply", action="append", default=[])

    s = sub.add_parser("readback")
    s.add_argument("--ticket", required=True)
    s.add_argument("--registry", required=True)

    s = sub.add_parser("finalize")
    s.add_argument("--ticket", required=True)
    s.add_argument("--registry", required=True)
    s.add_argument("--verdicts", default=None)  # accepted for older dispatch lines; finalize reads only the snapshot
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
