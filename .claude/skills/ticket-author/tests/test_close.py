#!/usr/bin/env python3
"""Isolated tests for close_helper.py (TKA-CLOSE-01 AI-2).

Every case runs the real helper CLI in a scratch git repo under a temp dir. The Agent role is played by
the test (it writes registry / verdicts / summary JSON files itself), the Registry is a fake adapter
(plain JSON files) and reviewers are stubs. A pass here proves helper decisions, side effects and that the
`close-dispatch` block in SKILL.md is executable; it does NOT prove real Sonnet/Codex judgement quality or
that a real Agent follows the Skill.

Run: python -m unittest discover -s .claude/skills/ticket-author/tests -p "test_*.py"
"""
import ast
import hashlib
import json
import os
import re
import shlex
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

HERE = Path(__file__).resolve()
SKILL_DIR = HERE.parents[1]
REAL_REPO = HERE.parents[4]
HELPER = SKILL_DIR / "scripts" / "close_helper.py"
SKILL_MD = SKILL_DIR / "SKILL.md"
PROJECT = "plans/cso_briefing/project"
DAY = 86400
TID = "TST-01"

DEFAULT_ACS = (
    "- [ ] **[存在性]** first criterion\n"
    "- [ ] **[行為性]** second criterion " + ("long-text-" * 60) + "\n"
    "  continuation line one\n"
    "\n"
    "  continuation line two\n"
    "- [ ] ~~`assembleDebug` PASS~~ (N/A: docs only)\n"
)


def put(path, text):
    Path(path).write_bytes(text.encode("utf-8"))


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def run_helper(repo, *args, now=None, env=None):
    cmd = [sys.executable, "-X", "utf8", str(HELPER), "--repo", str(repo)]
    if now is not None:
        cmd += ["--now", str(now)]
    cmd += [str(a) for a in args]
    r = subprocess.run(cmd, capture_output=True, env=env)
    out = r.stdout.decode("utf-8", "replace").strip()
    data = None
    if out:
        try:
            data = json.loads(out.splitlines()[-1])
        except ValueError:
            data = None
    return r.returncode, data, r.stderr.decode("utf-8", "replace")


class Base(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(ignore_cleanup_errors=True)
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)

    # ---- git
    def git(self, *a):
        r = subprocess.run(["git", "-C", str(self.repo), "-c", "user.name=t", "-c", "user.email=t@t",
                            "-c", "core.autocrlf=false", *a], capture_output=True)
        if r.returncode != 0:
            raise AssertionError("git %s failed: %s" % (a, r.stderr.decode("utf-8", "replace")))
        return r.stdout.decode("utf-8", "replace").rstrip("\r\n")

    def commit(self, msg, *paths):
        self.git("add", "--", *paths)
        self.git("commit", "-q", "-m", msg)
        return self.git("rev-parse", "HEAD")

    # ---- scratch repo
    def ticket_text(self, allowed, forbidden="", base="", diff_ref="<開工時 git rev-parse --short HEAD 回填>",
                    acs=DEFAULT_ACS, marker=False, header_extra=""):
        t = ["# TST-01 sample", "", "execution_dependency: none", header_extra, "", "## Verify Config", "",
             "TICKET_ID=TST-01", "PRESET=feature", "ALLOWED=" + allowed, "FORBIDDEN=" + forbidden,
             "DIFF_REF=" + diff_ref, "BASE_COMMIT=" + base, "", "## Action Items", "", "- [x] done", ""]
        if acs is not None:
            t += ["## Acceptance Criteria", "", "> intro quote is not an AC", "", acs.rstrip("\n"), ""]
        t += ["## 已知限制", "", "1. none", ""]
        text = "\n".join(t) + "\n"
        if marker:
            text += "<!-- close-result:begin -->\n## 收束結果（close）\n- old\n<!-- close-result:end -->\n"
        return text

    def make_repo(self, allowed="docs/a.md", forbidden="", ticket_dir="deliver", name="TST-01_x.md", impl=True,
                  lifecycle="REVIEW", pointer=None, acs=DEFAULT_ACS, marker=False, with_base=True,
                  diff_ref="<開工時 git rev-parse --short HEAD 回填>", reviews=None, checkpoint=None):
        self.repo = self.root / "repo"
        self.repo.mkdir()
        subprocess.run(["git", "init", "-q", "-b", "main", str(self.repo)], check=True)
        put((self.repo / ".gitignore"), ".tmp/\n")
        put((self.repo / "README.md"), "init\n")
        self.c0 = self.commit("init", ".gitignore", "README.md")
        self.impl = None
        if impl:
            (self.repo / "docs").mkdir()
            put((self.repo / "docs" / "a.md"), "alpha\n")
            self.impl = self.commit("[TST-01] impl", "docs/a.md")
        self.name = name
        self.ticket_rel = "%s/%s/%s" % (PROJECT, ticket_dir, name)
        tp = self.repo / self.ticket_rel
        tp.parent.mkdir(parents=True, exist_ok=True)
        tp.write_text(self.ticket_text(allowed, forbidden, self.c0 if with_base else "", diff_ref, acs, marker),
                      encoding="utf-8", newline="")
        self.commit("[TST-01] ticket record", self.ticket_rel)
        self.dir = self.repo / ".tmp" / "ticket-author" / "close" / TID
        self.dir.mkdir(parents=True)
        self.reg = {"id": TID, "lifecycle": lifecycle, "file": pointer or "%s/%s" % (ticket_dir, name),
                    "reviews": reviews if reviews is not None else {"qa": "PASS", "sv": "PASS", "codex": "PASS", "traycer": "N_A"},
                    "checkpoint": checkpoint}
        self.write_json("registry.json", self.reg)

    def write_json(self, name, obj):
        put((self.dir / name), json.dumps(obj, ensure_ascii=False))
        if name == "verdicts.json" and isinstance(obj, dict) and not obj.get("_no_reply"):
            # the real flow saves the reviewer's raw reply; tests derive an equivalent reply from the claimed statuses
            lines = ["AC%d: %s | core=%s | e" % (e["n"], e["status"], "Y" if e.get("core") else "N")
                     for e in obj.get("acs", [])
                     if isinstance(e, dict) and isinstance(e.get("n"), int) and e.get("status")]
            put(self.dir / "reviewer_reply.txt", "\n".join(lines) + "\n")

    def read_json(self, name):
        return json.loads((self.dir / name).read_text(encoding="utf-8"))

    def ticket_path(self):
        return self.repo / self.ticket_rel

    # ---- helper calls
    def h(self, *args, **kw):
        return run_helper(self.repo, *args, **kw)

    def preflight(self, dependents=True):
        args = ["preflight", "--ticket", TID, "--registry", self.dir / "registry.json"]
        if dependents:
            args += ["--dependents", self.dir / "dependents.json"]
        return self.h(*args)

    def decide(self, rnd=0, fb=0, extra=(), replies=None):
        reply_args = []
        for r in (replies if replies is not None else ["reviewer_reply.txt"]):
            reply_args += ["--reply", self.dir / r]
        return self.h("decide", "--ticket", TID, "--registry", self.dir / "registry.json",
                      "--verdicts", self.dir / "verdicts.json", *reply_args,
                      "--round", rnd, "--fallback-tried", fb, *extra)

    def set_dependents(self, tickets=None):
        self.write_json("dependents.json", {"tickets": tickets if tickets is not None else [{"id": TID}]})

    @staticmethod
    def verdicts(statuses=("PASS", "PASS", "N/A"), cores=(True, True, False), **kw):
        v = {"reviewer": {"family": "codex", "actual_model": "gpt-6-sol", "tier": "heavy", "fallback_used": False,
                          "fallback_reason": "", "independent_call": True, "inline": False, "limitation": ""},
             "acs": [{"n": i + 1, "status": s, "core": c, "evidence": "e", "note": ""}
                     for i, (s, c) in enumerate(zip(statuses, cores))],
             "security_issue": False, "irreversible_risk": False,
             "dependency_contract": {"status": "NONE_FOUND", "detail": ""},
             "checkpoint_matter_resolved": None}
        v.update(kw)
        return v

    def after(self, lifecycle="CLOSED", reviews=None, file=None):
        a = dict(self.reg)
        a["lifecycle"] = lifecycle
        a["checkpoint"] = None
        if reviews is not None:
            a["reviews"] = reviews
        if file is not None:
            a["file"] = file
        self.write_json("registry_after.json", a)
        return a

    def to_close(self, verdicts=None):
        """preflight + decide(CLOSE) with default verdicts."""
        self.set_dependents()
        self.assertEqual(self.preflight()[0], 0)
        self.write_json("verdicts.json", verdicts or self.verdicts())
        rc, d, _ = self.decide()
        self.assertEqual((rc, d["next"]), (0, "CLOSE"), d)
        return d

    def write_summary(self):
        put((self.dir / "summary.txt"), "line one\nline two\nline three\nline four\n")

    def finalize(self, commits=None):
        return self.h("finalize", "--ticket", TID, "--registry", self.dir / "registry_after.json",
                      "--verdicts", self.dir / "verdicts.json", "--summary", self.dir / "summary.txt",
                      "--commits", commits or (self.impl or ""))


# =============================================================================== 0. static
class TestStatic(unittest.TestCase):
    def test_files_exist_and_only_stdlib(self):
        std = sys.stdlib_module_names
        for p in (HELPER, HERE):
            self.assertTrue(p.is_file(), p)
            tree = ast.parse(p.read_text(encoding="utf-8"))
            for n in ast.walk(tree):
                mods = []
                if isinstance(n, ast.Import):
                    mods = [a.name.split(".")[0] for a in n.names]
                elif isinstance(n, ast.ImportFrom) and n.level == 0:
                    mods = [n.module.split(".")[0]]
                for m in mods:
                    self.assertIn(m, std, "%s imports non-stdlib %s" % (p.name, m))
            subprocess.run([sys.executable, "-m", "py_compile", str(p)], check=True)

    def test_dispatch_subcommands_exist_in_help(self):
        text = SKILL_MD.read_text(encoding="utf-8")
        m = re.search(r"```close-dispatch\n(.*?)```", text, re.S)
        self.assertIsNotNone(m)
        r = subprocess.run([sys.executable, str(HELPER), "--help"], capture_output=True)
        helptext = r.stdout.decode("utf-8", "replace")
        for line in m.group(1).strip().splitlines():
            sub = shlex.split(line)[2]
            self.assertIn(sub, helptext)
        for needle in ("registry.json", "dependents.json", "verdicts.json", "registry_after.json", "summary.txt"):
            self.assertIn(needle, text)

    def test_residual_old_phrases_absent(self):
        old = ["限審查方 SMTR/CSO", "Traycer T2 另由 Akira", "等待最終 T2／CSO 複審", "等待人工 Traycer 複審與終審方轉",
               "HUMAN_GATE（Traycer T2／", "於 PROGRESS.md 誠實揭露（K5-Rule 29）", "停止勾選該項並列入修補票追蹤",
               "Agent 最高可寫 lifecycle 為", "並於 checkpoint 揭露豁免依據",
               "RAPID MODE", "ask the other reviewer", "FALLBACK_REVIEWER", "Manual modes: diag, plan, ticket, close",
               "[supplement | fallback reviewer]*", "呼叫 `/ticket-author close"]
        files = [REAL_REPO / ".claude/rules/ssot-governance.md", REAL_REPO / ".claude/skills/sv/SKILL.md"]
        files += sorted(SKILL_DIR.glob("*.md")) + sorted((SKILL_DIR / "references").glob("*.md"))
        for f in files:
            text = f.read_text(encoding="utf-8")
            for phrase in old:
                self.assertNotIn(phrase, text, "%s still has %r" % (f.name, phrase))


# =============================================================================== 1. dispatch integration
class TestDispatchIntegration(Base):
    def dispatch_commands(self):
        text = SKILL_MD.read_text(encoding="utf-8")
        m = re.search(r"```close-dispatch\n(.*?)```", text, re.S)
        self.assertIsNotNone(m, "close-dispatch block missing")
        return [ln for ln in m.group(1).strip().splitlines() if ln.strip()]

    def argv(self, line, **subs):
        line = line.replace("<ID>", TID).replace("<dir>", self.dir.as_posix())
        for k, v in subs.items():
            line = line.replace(k, str(v))
        parts = shlex.split(line)
        self.assertEqual(parts[0], "python")
        self.assertTrue(parts[1].endswith("close_helper.py"))
        return [sys.executable, "-X", "utf8", str(HELPER), "--repo", str(self.repo)] + parts[2:]

    def exec(self, line, **subs):
        r = subprocess.run(self.argv(line, **subs), capture_output=True)
        out = r.stdout.decode("utf-8", "replace").strip()
        return r.returncode, json.loads(out.splitlines()[-1]) if out else None

    def test_full_flow_from_skill_block(self):
        self.make_repo()
        cmds = self.dispatch_commands()
        self.assertEqual(len(cmds), 7)
        self.assertEqual([shlex.split(c)[2] for c in cmds],
                         ["preflight", "reviewer", "decide", "readback", "finalize", "persist", "persist"])
        self.assertNotIn("--recover", cmds[5])
        self.assertIn("--recover", cmds[6])
        self.set_dependents()
        head_before = self.git("rev-parse", "HEAD")
        rc, pre = self.exec(cmds[0])
        self.assertEqual((rc, pre["location"], pre["scope"]["status"], pre["ac_count"]), (0, "deliver", "PASS", 3))
        rc, rv = self.exec(cmds[1])
        self.assertEqual((rc, rv["class"], rv["actual"]["model"]), (0, "heavy", "gpt-6-sol"))  # no explicit simple basis: Sol
        self.write_json("verdicts.json", self.verdicts())
        rc, dec = self.exec(cmds[2], **{"<N>": 0, "<0|1>": 0})
        self.assertEqual((rc, dec["next"]), (0, "CLOSE"))
        self.after()
        rc, rb = self.exec(cmds[3])
        self.assertTrue(rb["ok"], rb)
        self.write_summary()
        rc, fin = self.exec(cmds[4], **{"<h1,h2>": self.impl})
        self.assertEqual((rc, fin["written"]), (0, True))
        text = self.ticket_path().read_text(encoding="utf-8")
        self.assertEqual(text.count("<!-- close-result:begin -->"), 1)
        self.assertEqual(text.count("<!-- close-result:end -->"), 1)
        self.assertEqual(self.git("rev-parse", "HEAD"), head_before)
        self.assertEqual(self.git("status", "--porcelain"), " M " + self.ticket_rel)
        # integration reachability: the documented last dispatch step saves the Ticket record
        rc, per = self.exec(cmds[5])
        self.assertEqual((rc, per["saved"], per["verification"]), (0, True, "full"), per)
        self.assertNotEqual(self.git("rev-parse", "HEAD"), head_before)
        self.assertEqual(self.git("status", "--porcelain"), "")
        self.assertEqual(self.git("diff", "--name-only", head_before, "HEAD"), self.ticket_rel)
        self.assertTrue(self.git("log", "-1", "--format=%s").startswith("[TST-01] "))


# =============================================================================== 1b. persist
class TestPersist(Base):
    """ADV-SAVE-01: deterministic save of the Ticket record, honest failures, scratch-free recovery."""

    def closed_repo(self, finalize=True, **kw):
        self.make_repo(**kw)
        self.to_close()
        self.after()
        if finalize:
            self.write_summary()
            self.assertEqual(self.finalize()[0], 0)

    def persist(self, recover=False, registry="registry_after.json"):
        args = ["persist", "--ticket", TID, "--registry", self.dir / registry]
        if recover:
            args.append("--recover")
        return self.h(*args)

    def untrack_ticket(self):
        keep = self.ticket_path().read_bytes()
        self.git("rm", "-q", "--cached", "--", self.ticket_rel)
        self.git("commit", "-q", "-m", "untrack ticket")
        self.assertEqual(self.ticket_path().read_bytes(), keep)

    def sweep_scratch(self):
        shutil.rmtree(self.dir)
        self.dir.mkdir(parents=True)

    def head(self):
        return self.git("rev-parse", "HEAD")

    def status(self):
        return self.git("status", "--porcelain", "--untracked-files=all")

    # ---- full verification
    def test_untracked_new_file_commits_only_that_file(self):
        self.closed_repo()
        self.untrack_ticket()
        put(self.repo / "other.md", "someone else's dirty file\n")
        put(self.repo / "README.md", "init changed\n")
        before = self.head()
        self.git("add", "--", "README.md")  # a foreign staged change must stay staged
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["verification"]), (0, True, "full"), d)
        self.assertEqual(self.git("rev-list", "--count", before + "..HEAD"), "1")
        self.assertEqual(self.git("log", "-1", "--format=%s"), "[TST-01] docs(ticket): persist closeout record (TKA close persist)")
        self.assertEqual(self.git("diff", "--name-only", before, "HEAD"), self.ticket_rel)
        self.assertIn("?? other.md", self.status())
        self.assertIn("M  README.md", self.status())

    def test_tracked_modified_commits_one_file(self):
        self.closed_repo()
        put(self.repo / "other.md", "dirty\n")
        before = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"]), (0, True), d)
        self.assertEqual(self.git("diff", "--name-only", before, "HEAD"), self.ticket_rel)
        self.assertEqual(d["commit"], self.head())
        self.assertIn("?? other.md", self.status())

    def test_tracked_no_diff_is_saved_without_commit_and_reentry_adds_nothing(self):
        self.closed_repo()
        self.assertEqual(self.persist()[0], 0)
        head = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["reason"]), (0, True, "no_diff"), d)
        self.assertEqual(self.head(), head)

    def test_non_closed_registry_is_refused_with_zero_writes(self):
        self.closed_repo()
        self.untrack_ticket()
        self.after(lifecycle="REVIEW")
        head = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["refused"]), (3, False, "readback_mismatch"), d)
        self.assertEqual(self.head(), head)
        self.assertEqual(self.status(), "?? " + self.ticket_rel)  # not even `git add`

    def test_ticket_not_under_deliver_is_refused_with_zero_writes(self):
        self.closed_repo()
        moved = "%s/Pending/%s" % (PROJECT, self.name)
        (self.repo / PROJECT / "Pending").mkdir(parents=True, exist_ok=True)
        shutil.move(str(self.ticket_path()), str(self.repo / moved))
        self.after(file="Pending/" + self.name)
        head = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["refused"]), (3, False, "readback_mismatch"), d)
        self.assertEqual(self.head(), head)
        self.assertEqual(self.git("diff", "--cached", "--name-only"), "")

    def test_ignored_ticket_file_is_not_saved_and_not_forced(self):
        self.closed_repo()
        self.git("rm", "-q", "--cached", "--", self.ticket_rel)
        put(self.repo / ".gitignore", ".tmp/\n" + self.ticket_rel + "\n")
        self.git("add", "--", ".gitignore")
        self.git("commit", "-q", "-m", "ignore ticket")
        head = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["reason"]), (3, False, "ignored"), d)
        self.assertEqual(self.head(), head)
        self.assertEqual(self.git("ls-files", "--", self.ticket_rel), "")

    def test_commit_hook_failure_is_reported_not_saved_and_leaves_nothing_staged(self):
        self.closed_repo()
        self.untrack_ticket()
        hook = self.repo / ".git" / "hooks" / "commit-msg"
        put(hook, "#!/bin/sh\necho blocked >&2\nexit 1\n")
        os.chmod(hook, 0o755)
        head = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["reason"]), (3, False, "commit_failed"), d)
        self.assertNotIn('"saved": true', json.dumps(d))
        self.assertEqual(self.head(), head)
        self.assertEqual(self.status(), "?? " + self.ticket_rel)

    def test_add_failure_is_reported_not_saved(self):
        self.closed_repo()
        self.untrack_ticket()
        lock = self.repo / ".git" / "index.lock"
        put(lock, "")
        self.addCleanup(lambda: lock.exists() and lock.unlink())
        head = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["reason"]), (3, False, "add_failed"), d)
        self.assertEqual(self.head(), head)

    # ---- scratch swept: explicit recovery
    def test_scratch_missing_requires_explicit_recover_and_writes_nothing(self):
        self.closed_repo()
        self.untrack_ticket()
        reg_after = (self.dir / "registry_after.json").read_bytes()
        self.sweep_scratch()
        put(self.dir / "registry_after.json", reg_after.decode("utf-8"))
        head = self.head()
        rc, d, _ = self.persist()
        self.assertEqual((rc, d["saved"], d["refused"]), (3, False, "scratch_missing_use_recover"), d)
        self.assertEqual(self.head(), head)
        self.assertEqual(self.status(), "?? " + self.ticket_rel)

    def test_recover_commits_after_scratch_sweep(self):
        self.closed_repo()
        self.untrack_ticket()
        reg_after = (self.dir / "registry_after.json").read_bytes()
        self.sweep_scratch()
        put(self.dir / "registry_after.json", reg_after.decode("utf-8"))
        before = self.head()
        rc, d, _ = self.persist(recover=True)
        self.assertEqual((rc, d["saved"], d["verification"]), (0, True, "recover"), d)
        self.assertEqual(self.git("diff", "--name-only", before, "HEAD"), self.ticket_rel)

    def recover_refusal(self, mutate):
        self.closed_repo()
        self.untrack_ticket()
        reg_after = json.loads((self.dir / "registry_after.json").read_text(encoding="utf-8"))
        self.sweep_scratch()
        mutate(reg_after)
        put(self.dir / "registry_after.json", json.dumps(reg_after, ensure_ascii=False))
        head = self.head()
        rc, d, _ = self.persist(recover=True)
        self.assertEqual((rc, d["saved"], d["refused"]), (3, False, "readback_mismatch"), d)
        self.assertEqual(self.head(), head)
        self.assertEqual(self.git("diff", "--cached", "--name-only"), "")
        return d

    def test_recover_refuses_without_close_result_section(self):
        self.closed_repo(finalize=False)
        self.untrack_ticket()
        reg_after = (self.dir / "registry_after.json").read_bytes()
        self.sweep_scratch()
        put(self.dir / "registry_after.json", reg_after.decode("utf-8"))
        rc, d, _ = self.persist(recover=True)
        self.assertEqual((rc, d["saved"]), (3, False))
        self.assertTrue(any("close-result section" in m for m in d["mismatches"]), d)

    def test_recover_refuses_non_closed_registry(self):
        d = self.recover_refusal(lambda r: r.update(lifecycle="REVIEW"))
        self.assertTrue(any("lifecycle" in m for m in d["mismatches"]), d)

    def test_recover_refuses_ticket_outside_deliver(self):
        self.closed_repo()
        moved = "%s/Pending/%s" % (PROJECT, self.name)
        (self.repo / PROJECT / "Pending").mkdir(parents=True, exist_ok=True)
        shutil.move(str(self.ticket_path()), str(self.repo / moved))
        reg_after = json.loads((self.dir / "registry_after.json").read_text(encoding="utf-8"))
        reg_after["file"] = "Pending/" + self.name
        self.sweep_scratch()
        put(self.dir / "registry_after.json", json.dumps(reg_after, ensure_ascii=False))
        head = self.head()
        rc, d, _ = self.persist(recover=True)
        self.assertEqual((rc, d["saved"]), (3, False))
        self.assertTrue(any("location" in m or "resolvable" in m for m in d["mismatches"]), d)
        self.assertEqual(self.head(), head)
        self.assertEqual(self.git("diff", "--cached", "--name-only"), "")

    def test_recover_refuses_missing_or_invalid_reviews(self):
        for bad in ({"qa": "PASS", "sv": "PASS", "codex": "PASS"}, {"qa": "PASS", "sv": "PASS", "codex": "PASS", "traycer": "MAYBE"}):
            with self.subTest(reviews=bad):
                self.root = Path(tempfile.mkdtemp(dir=self.tmp.name))
                d = self.recover_refusal(lambda r, b=bad: r.update(reviews=b))
                self.assertTrue(any("reviews." in m for m in d["mismatches"]), d)


    # ---- SKILL wording that Agents follow (documented rules, checked by text)
    def test_skill_documents_honest_failure_and_recovery_rules(self):
        text = SKILL_MD.read_text(encoding="utf-8")
        for needle in ("persist: saved <hash> | already_saved | NOT SAVED(<reason>) | not_run(<why>)",
                       "when it is false never write saved or committed",
                       "report the close as partial success",
                       "persist --recover", "not_run(no_final_result_section)",
                       "Nothing is persisted for `NOT_CLOSED`, `PRECONDITION_FAILED`, `BLOCKED` or any `STOP_*`",
                       "no `check commit`, no `-A`, no `--allow-empty`"):
            self.assertIn(needle, text)


# =============================================================================== 2. AC completeness
class TestAcCompleteness(Base):
    def setUp(self):
        super().setUp()
        self.make_repo()
        self.set_dependents()

    def next_for(self, verdicts, rnd=0):
        self.preflight()
        self.write_json("verdicts.json", verdicts)
        rc, d, _ = self.decide(rnd)
        self.assertEqual(rc, 0, d)
        return d

    def test_missing_duplicate_unknown_empty_never_close(self):
        v = self.verdicts()
        missing = dict(v, acs=v["acs"][:2])
        self.assertEqual(self.next_for(missing)["next"], "SUPPLEMENT")
        self.assertEqual(self.next_for(missing)["missing"], [3])
        dup = dict(v, acs=v["acs"] + [dict(v["acs"][0])])
        d = self.next_for(dup)
        self.assertEqual((d["next"], d["duplicate"]), ("SUPPLEMENT", [1]))
        unk = dict(v, acs=v["acs"] + [{"n": 9, "status": "PASS", "core": True, "evidence": "e", "note": ""}])
        d = self.next_for(unk)
        self.assertEqual((d["next"], d["unknown"]), ("SUPPLEMENT", [9]))
        empty = dict(v, acs=[])
        self.assertEqual(self.next_for(empty)["next"], "SUPPLEMENT")
        self.assertEqual(self.next_for(empty, rnd=2)["next"], "NOT_CLOSED")
        self.assertEqual(self.next_for(v)["next"], "CLOSE")

    def test_invalid_status_or_core_value(self):
        v = self.verdicts()
        v["acs"][0]["status"] = "MAYBE"
        self.assertEqual(self.next_for(v)["next"], "SUPPLEMENT")
        v = self.verdicts()
        v["acs"][1]["core"] = "yes"
        self.assertEqual(self.next_for(v)["next"], "SUPPLEMENT")

    def test_ac_list_unavailable(self):
        self.root = self.root / "second"
        self.root.mkdir()
        self.make_repo(acs=None)
        self.set_dependents()
        rc, pre, _ = self.preflight()
        self.assertEqual(pre["acs_status"], "UNAVAILABLE")
        self.write_json("verdicts.json", self.verdicts())
        rc, d, _ = self.decide()
        self.assertEqual(d["next"], "NOT_CLOSED")
        self.assertEqual(d["reason"], "ac_list_unavailable")

    def test_full_text_kept_preview_truncated(self):
        rc, pre, _ = self.preflight()
        self.assertEqual(pre["ac_count"], 3)
        full = self.read_json("preflight.json")["acs"]
        self.assertGreater(len(full[1]["text"]), 400)
        self.assertIn("continuation line one", full[1]["text"])
        self.assertIn("continuation line two", full[1]["text"])
        self.assertTrue(full[2]["struck"])
        self.assertNotIn("intro quote", json.dumps(full))
        self.assertLessEqual(len(pre["acs"][1]["text_preview"]), 400)
        self.assertEqual(full[0]["types"], ["存在性"])


# =============================================================================== 3. decision table
class TestDecisionTable(Base):
    def setUp(self):
        super().setUp()
        self.make_repo()
        self.set_dependents()
        self.preflight()

    def d(self, verdicts, rnd=0, fb=0, extra=()):
        self.write_json("verdicts.json", verdicts)
        rc, data, err = self.decide(rnd, fb, extra)
        self.assertEqual(rc, 0, err)
        return data

    def test_blockers_precede_everything(self):
        v = self.verdicts(statuses=("FAIL", "PASS", "N/A"))
        self.assertEqual(self.d(v)["kind"], "core_ac_fail")
        v = self.verdicts(statuses=("FAIL", "UNVERIFIED", "N/A"))
        v["acs"] = v["acs"][:2]  # incomplete + core fail: blocker still wins
        self.assertEqual(self.d(v)["next"], "BLOCKED")
        self.assertEqual(self.d(self.verdicts(security_issue=True))["kind"], "security")
        self.assertEqual(self.d(self.verdicts(irreversible_risk=True))["kind"], "irreversible")
        v = self.verdicts(dependency_contract={"status": "MISSING", "detail": "x"})
        self.assertEqual(self.d(v)["kind"], "dependency_contract")

    def test_non_core_fail_is_not_a_blocker(self):
        v = self.verdicts(statuses=("PASS", "PASS", "N/A"), cores=(True, True, False))
        v["acs"][2] = {"n": 3, "status": "FAIL", "core": False, "evidence": "e", "note": ""}
        self.assertEqual(self.d(v)["next"], "CLOSE")

    def test_unverified_sequence(self):
        v = self.verdicts(statuses=("PASS", "UNVERIFIED", "N/A"))
        self.assertEqual(self.d(v, rnd=0)["next"], "SUPPLEMENT")
        self.assertEqual(self.d(v, rnd=1)["next"], "SUPPLEMENT")
        # the default reviewer is already the strongest (gpt-6-sol): nothing to escalate to
        self.assertEqual(self.d(v, rnd=2, fb=0)["next"], "NOT_CLOSED")
        self.assertEqual(self.d(v, rnd=2, fb=1)["reason"], "core_unverified")
        luna = dict(v, reviewer=dict(v["reviewer"], actual_model="gpt-6-luna", tier="light", tier_basis="plain wording fix"))
        esc = self.d(luna, rnd=2, fb=0)
        self.assertEqual((esc["next"], esc["reviewer"]), ("ESCALATE_REVIEWER", "gpt-6-sol"))
        self.assertEqual(self.d(luna, rnd=2, fb=1)["next"], "NOT_CLOSED")

    def test_non_core_unverified_and_valid_na_do_not_block(self):
        v = self.verdicts(statuses=("PASS", "PASS", "UNVERIFIED"))
        self.assertEqual(self.d(v)["next"], "CLOSE")

    def test_na_without_strike_is_downgraded(self):
        v = self.verdicts(statuses=("PASS", "N/A", "N/A"))  # AC 2 core N/A but not struck
        d = self.d(v)
        self.assertEqual(d["next"], "SUPPLEMENT")
        self.assertIn(2, d["downgraded_na"])
        self.assertEqual(self.d(self.verdicts(statuses=("PASS", "PASS", "N/A")))["downgraded_na"], [])

    def test_required_params_exit_2(self):
        self.write_json("verdicts.json", self.verdicts())
        base = ["decide", "--ticket", TID, "--registry", self.dir / "registry.json", "--verdicts", self.dir / "verdicts.json"]
        self.assertEqual(self.h(*base, "--fallback-tried", 0)[0], 2)
        self.assertEqual(self.h(*base, "--round", 0)[0], 2)

    def test_checkpoint_rows(self):
        self.reg["checkpoint"] = {"state": "OPEN", "summary": "waiting for manual close"}
        self.write_json("registry.json", self.reg)
        self.preflight()
        self.assertEqual(self.d(self.verdicts())["reason"], "checkpoint_unresolved")
        d = self.d(self.verdicts(checkpoint_matter_resolved=True))
        self.assertEqual((d["next"], d["checkpoint_action"]), ("CLOSE", "resolve"))
        self.reg["checkpoint"] = {"state": "RESOLVED", "summary": "x"}
        self.write_json("registry.json", self.reg)
        self.preflight()
        self.assertEqual(self.d(self.verdicts())["checkpoint_action"], "none")

    def test_dependency_lead_rows(self):
        v = self.verdicts(dependency_contract={"status": "LEAD", "detail": "x"})
        self.assertEqual(self.d(v, rnd=0)["reason"], "dependency_lead")
        self.assertEqual(self.d(v, rnd=2)["reason"], "dependency_lead_unresolved")
        v = self.verdicts(dependency_contract={"status": "UNKNOWN", "detail": ""})
        self.assertEqual(self.d(v)["next"], "CLOSE")

    def test_decision_file_only_for_review_and_bound(self):
        self.assertEqual(self.d(self.verdicts())["next"], "CLOSE")
        dec = self.read_json("decision.json")
        self.assertEqual((dec["ticket"], dec["ticket_file"], dec["next"]), (TID, self.ticket_rel, "CLOSE"))
        self.assertEqual(dec["binding_ref"], self.impl)
        self.assertIn("reviews_patch", dec)


# =============================================================================== 3b. X rows (scope)
class TestScopeRows(Base):
    def run_decide(self, verdicts, rnd):
        self.set_dependents()
        self.preflight()
        self.write_json("verdicts.json", verdicts)
        rc, d, _ = self.decide(rnd)
        self.assertEqual(rc, 0)
        return d

    def test_x1_x2_scope_fail(self):
        self.make_repo(allowed="docs/a.md", forbidden="docs/b.md")
        put((self.repo / "docs" / "b.md"), "b\n")
        self.commit("[TST-01] touches forbidden", "docs/b.md")
        v = self.verdicts()
        d = self.run_decide(v, 0)
        self.assertEqual((d["next"], d["scope"]), ("RECONCILE", "FAIL"))
        self.assertEqual(self.run_decide(v, 2)["reason"], "reconcile_unresolved")

    def test_x3_scope_unknown_then_cap(self):
        self.make_repo(impl=False)
        v = self.verdicts()
        d = self.run_decide(v, 0)
        self.assertEqual((d["next"], d["reason"]), ("SUPPLEMENT", "scope_unknown"))
        self.assertEqual(self.run_decide(v, 2)["next"], "CLOSE")  # cap reached: no extra terminal state
        v = self.verdicts(statuses=("PASS", "UNVERIFIED", "N/A"))
        d = self.run_decide(v, 2)
        self.assertEqual((d["next"], d["reason"]), ("NOT_CLOSED", "core_unverified"))  # strongest reviewer already used

    def test_whitespace_failure_reconciles(self):
        self.make_repo()
        put((self.repo / "docs" / "a.md"), "alpha  \nbeta\n")
        self.commit("[TST-01] trailing ws", "docs/a.md")
        d = self.run_decide(self.verdicts(), 0)
        self.assertEqual((d["next"], d["diff_check"]), ("RECONCILE", "FAIL"))


# =============================================================================== 4. dependency
class TestDependency(Base):
    def setUp(self):
        super().setUp()
        self.make_repo()

    def decide_with(self, dep_status, dependents_ok, rnd=0):
        if dependents_ok:
            self.set_dependents([{"id": TID}, {"id": "DEP-01", "depends_on": [TID], "lifecycle": "PENDING"}])
        else:
            (self.dir / "dependents.json").unlink(missing_ok=True)
        rc, pre, _ = self.preflight()
        self.write_json("verdicts.json", self.verdicts(dependency_contract={"status": dep_status, "detail": "x"}))
        return pre, self.decide(rnd)[1]

    def test_direct_dependents_listed_and_checked(self):
        pre, d = self.decide_with("NONE_FOUND", True)
        self.assertEqual((pre["dependents"]["status"], pre["dependents"]["count"]), ("CHECKED", 1))
        self.assertEqual((d["next"], d["dependency_status"]), ("CLOSE", "NONE_FOUND"))

    def test_none_found_downgraded_when_list_unavailable(self):
        pre, d = self.decide_with("NONE_FOUND", False)
        self.assertEqual(pre["dependents"]["status"], "UNKNOWN")
        self.assertEqual((d["next"], d["dependency_status"]), ("CLOSE", "UNKNOWN"))

    def test_lead_and_missing_not_downgraded(self):
        _, d = self.decide_with("LEAD", False)
        self.assertEqual((d["next"], d["reason"]), ("SUPPLEMENT", "dependency_lead"))
        _, d = self.decide_with("MISSING", False)
        self.assertEqual((d["next"], d["kind"]), ("BLOCKED", "dependency_contract"))

    def test_header_reference_found_without_recursion(self):
        dep = self.repo / PROJECT / "Pending" / "DEP-01_y.md"
        dep.parent.mkdir(parents=True)
        put(dep, "# DEP-01\nevidence_dependency: TST-01 REVIEW\nunrelated line\n")
        self.set_dependents([{"id": TID}, {"id": "DEP-01", "file": "Pending/DEP-01_y.md", "lifecycle": "PENDING"},
                             {"id": "OTHER-01", "file": "Pending/none.md"}])
        rc, pre, _ = self.preflight()
        self.assertEqual(pre["dependents"]["status"], "UNKNOWN")  # OTHER-01 file unreadable but not referencing -> unreadable listed
        self.set_dependents([{"id": TID}, {"id": "DEP-01", "file": "Pending/DEP-01_y.md", "lifecycle": "PENDING"}])
        rc, pre, _ = self.preflight()
        self.assertEqual((pre["dependents"]["status"], pre["dependents"]["count"]), ("CHECKED", 1))
        self.assertIn("ticket_header", self.read_json("preflight.json")["dependents"]["direct"][0]["via"])


# =============================================================================== 5. reviewer
class TestReviewer(Base):
    def reviewer(self, allowed, *flags, escalate=None):
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo(allowed=allowed)
        self.set_dependents()
        self.preflight()
        extra = list(flags) + (["--escalate", escalate] if escalate else [])
        rc, d, _ = self.h("reviewer", "--ticket", TID, *extra)
        self.assertEqual(rc, 0)
        return d

    def test_codex_is_primary_for_every_ticket_type(self):
        basis = ("--simple-basis", "plain wording fix")
        for allowed, tier, model, flags in (("plans/x/note.md,docs/n.txt", "light", "gpt-6-luna", basis),
                                            ("plans/x/note.md,docs/n.txt", "heavy", "gpt-6-sol", ()),  # uncertain: Sol
                                            ("docs/a.md,scripts/x.py", "heavy", "gpt-6-sol", basis),
                                            ("app/src/Main.kt", "heavy", "gpt-6-sol", basis),
                                            (".claude/skills/x/SKILL.md", "heavy", "gpt-6-sol", basis),
                                            (".claude/rules/r.md,plans/p.md", "heavy", "gpt-6-sol", basis)):
            d = self.reviewer(allowed, "--codex-available", "1", *flags)
            self.assertEqual((d["class"], d["actual"]["family"], d["actual"]["model"], d["fallback_used"]),
                             (tier, "codex", model, False), allowed)

    def test_force_heavy_raises_but_nothing_lowers(self):
        d = self.reviewer("plans/p.md", "--codex-available", "1", "--force-heavy")
        self.assertEqual((d["class"], d["actual"]["model"]), ("heavy", "gpt-6-sol"))

    def test_claude_only_on_codex_fault(self):
        d = self.reviewer("x.py", "--codex-available", "1")
        self.assertFalse(d["fallback_used"])  # no fault: never switches family
        d = self.reviewer("x.py", "--codex-fault", "quota")
        self.assertEqual((d["actual"], d["fallback_used"], d["fallback_reason"]),
                         ({"family": "claude", "model": "opus"}, True, "quota"))
        self.assertIn("not Codex", d["limitation"])
        d = self.reviewer("plans/p.md", "--codex-fault", "auth", "--simple-basis", "plain wording fix")
        self.assertEqual(d["actual"], {"family": "claude", "model": "sonnet"})
        d = self.reviewer("x.py", "--codex-available", "0")  # codex missing is a fault
        self.assertEqual((d["actual"]["family"], d["fallback_reason"]), ("claude", "unavailable"))
        d = self.reviewer("x.py", "--codex-fault", "network", "--claude-available", "0")
        self.assertEqual(d["actual"]["family"], "none")
        rc, bad, _ = self.h("reviewer", "--ticket", TID, "--codex-fault", "report_fix")
        self.assertEqual(rc, 2)  # REPORT_FIX / finding / FAIL / LIMIT are not Codex faults

    def test_escalate_luna_to_sol_only(self):
        d = self.reviewer("plans/p.md", "--codex-available", "1", "--simple-basis", "plain wording fix", escalate="2,3")
        self.assertEqual(d["escalation"], {"reviewer": "gpt-6-sol", "acs": [2, 3]})
        d = self.reviewer("x.py", "--codex-available", "1", escalate="2")
        self.assertEqual(d["escalation"]["reviewer"], "none")

    def test_codex_stub_on_path_detected(self):
        stub_dir = self.root / "stub"
        stub_dir.mkdir()
        if os.name == "nt":
            put((stub_dir / "codex.cmd"), "@echo off\r\necho stub\r\n")
        else:
            p = stub_dir / "codex"
            put(p, "#!/bin/sh\necho stub\n")
            p.chmod(0o755)
        self.make_repo(allowed="x.py")
        self.set_dependents()
        self.preflight()
        env = dict(os.environ, PATH=str(stub_dir) + os.pathsep + os.environ.get("PATH", ""))
        rc, d, _ = self.h("reviewer", "--ticket", TID, env=env)
        self.assertEqual((d["actual"]["family"], d["fallback_used"]), ("codex", False))


# =============================================================================== 6. diff scope
class TestDiffScope(Base):
    def pre(self):
        self.set_dependents()
        return self.preflight()[1]

    def test_other_ticket_commit_not_a_violation(self):
        self.make_repo()
        put((self.repo / "docs" / "a.md"), "alpha\nother\n")
        self.commit("[OTHER-01] edit shared file", "docs/a.md")
        put((self.repo / "docs" / "c.md"), "c\n")
        self.commit("[OTHER-01] outside allowed", "docs/c.md")
        pre = self.pre()
        self.assertEqual(pre["scope"]["status"], "PASS")
        self.assertEqual({o["ticket"] for o in self.read_json("preflight.json")["range"]["attribution"]["other"]}, {"OTHER-01"})

    def test_unprefixed_commit_touching_allowed_is_unknown(self):
        self.make_repo()
        put((self.repo / "docs" / "a.md"), "alpha\nmore\n")
        self.commit("tweak without prefix", "docs/a.md")
        pre = self.pre()
        self.assertEqual(pre["scope"]["status"], "UNKNOWN")
        self.assertIn("unprefixed", pre["scope"]["reason"])

    def test_own_commit_outside_allowed_fails(self):
        self.make_repo()
        put((self.repo / "docs" / "z.md"), "z\n")
        self.commit("[TST-01] strays", "docs/z.md")
        put((self.repo / "docs" / "a.md"), "alpha\nmore\n")
        self.commit("[TST-01] allowed again", "docs/a.md")
        pre = self.pre()
        self.assertEqual(pre["scope"]["status"], "FAIL")
        self.assertEqual(pre["scope"]["violations"], ["docs/z.md"])

    def test_record_commit_only_means_unknown(self):
        self.make_repo(impl=False)
        pre = self.pre()
        self.assertEqual(pre["scope"]["status"], "UNKNOWN")
        self.assertEqual(self.read_json("preflight.json")["range"]["commits"], [])

    def test_placeholder_diff_ref_uses_base_commit_and_both_invalid_is_unknown(self):
        self.make_repo()
        pre = self.pre()
        self.assertEqual(pre["base"], self.c0)
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo(with_base=False)
        pre = self.pre()
        self.assertEqual((pre["scope"]["status"], pre["scope"]["reason"]), ("UNKNOWN", "no_base"))

    def test_valid_diff_ref_is_lower_bound(self):
        self.make_repo(diff_ref="HEAD")
        # DIFF_REF=HEAD is not hex -> invalid; switch to a real short sha of the init commit
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo()
        text = self.ticket_path().read_text(encoding="utf-8").replace(
            "DIFF_REF=<開工時 git rev-parse --short HEAD 回填>", "DIFF_REF=" + self.impl[:8])
        put(self.ticket_path(), text)
        self.commit("[TST-01] fill diff ref", self.ticket_rel)
        pre = self.pre()
        self.assertEqual(pre["base"], self.impl)
        self.assertEqual(pre["scope"]["status"], "UNKNOWN")  # no own implementation commit after DIFF_REF

    def test_diff_check_only_for_own_commits(self):
        self.make_repo()
        put((self.repo / "docs" / "c.md"), "trailing   \n")
        self.commit("[OTHER-01] sloppy", "docs/c.md")
        self.assertEqual(self.pre()["diff_check"]["status"], "PASS")
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo()
        put((self.repo / "docs" / "a.md"), "alpha  \n")
        self.commit("[TST-01] sloppy", "docs/a.md")
        self.assertEqual(self.pre()["diff_check"]["status"], "FAIL")


# =============================================================================== 7. readback / finalize / reviews_patch
class TestReadbackFinalize(Base):
    def setUp(self):
        super().setUp()
        self.make_repo()
        self.write_summary()

    def readback(self):
        return self.h("readback", "--ticket", TID, "--registry", self.dir / "registry_after.json")

    def test_mismatches_block_finalize_and_leave_ticket_untouched(self):
        self.to_close()
        before = sha(self.ticket_path())
        cases = {
            "lifecycle": self.after(lifecycle="REVIEW"),
            "file": self.after(file="deliver/does-not-exist.md"),
            "reviews": self.after(reviews={"qa": "PASS", "sv": "FAIL", "codex": "PASS", "traycer": "N_A"}),
        }
        for name, _ in cases.items():
            if name == "lifecycle":
                self.after(lifecycle="REVIEW")
            elif name == "file":
                self.after(file="deliver/does-not-exist.md")
            else:
                self.after(reviews={"qa": "PASS", "sv": "FAIL", "codex": "PASS", "traycer": "N_A"})
            rc, rb, _ = self.readback()
            self.assertEqual((rc, rb["ok"]), (0, False), name)
            self.assertTrue(rb["mismatches"], name)
            rc, fin, _ = self.finalize()
            self.assertEqual(rc, 3, name)
            self.assertEqual(fin["refused"], "readback_mismatch")
            self.assertEqual(sha(self.ticket_path()), before, name)
            self.assertNotIn("讀回一致", self.ticket_path().read_text(encoding="utf-8"))

    def test_clean_flow_writes_once_and_is_idempotent(self):
        self.to_close()
        self.after()
        self.assertTrue(self.readback()[1]["ok"])
        self.assertEqual(self.finalize()[0], 0)
        first = self.ticket_path().read_text(encoding="utf-8")
        self.assertEqual(self.finalize()[0], 0)
        self.assertEqual(self.ticket_path().read_text(encoding="utf-8"), first)
        self.assertEqual(first.count("<!-- close-result:begin -->"), 1)

    def test_reviews_patch_only_fills_absent_fields(self):
        self.reg["reviews"] = {"qa": "PASS", "sv": "PASS"}
        self.write_json("registry.json", self.reg)
        v = self.verdicts(reviews_observed={"codex": {"value": "PASS", "evidence": "cross_review exit 0"},
                                            "traycer": {"value": "PASS", "evidence": ""}})
        d = self.to_close(v)
        self.assertEqual(d["reviews_patch"], {"codex": "PASS", "traycer": "N_A"})
        self.assertEqual(d["reviews_na_fields"], ["traycer"])
        self.after(reviews={"qa": "PASS", "sv": "PASS", "codex": "PASS", "traycer": "N_A"})
        self.assertTrue(self.readback()[1]["ok"])
        self.assertEqual(self.finalize()[0], 0)
        text = self.ticket_path().read_text(encoding="utf-8")
        self.assertIn("traycer", text.split("<!-- close-result:begin -->")[1])
        # a faked PASS for the optional review is a mismatch
        self.after(reviews={"qa": "PASS", "sv": "PASS", "codex": "PASS", "traycer": "PASS"})
        self.assertFalse(self.readback()[1]["ok"])

    def test_existing_values_are_never_changed(self):
        d = self.to_close(self.verdicts(reviews_observed={"qa": {"value": "FAIL", "evidence": "x"}}))
        self.assertEqual(d["reviews_patch"], {})

    def test_write_only_when_closed_and_bound(self):
        before = sha(self.ticket_path())
        self.after()  # CLOSED in registry but no decision at all
        self.assertEqual(self.finalize()[0], 3)
        self.assertEqual(sha(self.ticket_path()), before)


# =============================================================================== 8. binding / partial success
class TestBindingAndPartialSuccess(Base):
    def setUp(self):
        super().setUp()
        self.make_repo()
        self.write_summary()

    def reenter(self):
        self.reg["lifecycle"] = "CLOSED"
        self.write_json("registry.json", self.reg)
        self.preflight()
        return self.decide()[1]

    def test_partial_success_resumes_with_matching_binding(self):
        self.to_close()
        self.after()
        d = self.reenter()
        self.assertEqual(d["next"], "WRITE_FINAL_ONLY")
        self.after()
        self.assertEqual(self.finalize()[0], 0)
        self.assertEqual(self.reenter()["next"], "ALREADY_CLOSED")
        self.assertEqual(self.read_json("decision.json")["next"], "CLOSE")  # not overwritten by the CLOSED path

    def test_any_binding_item_mismatch_blocks_write(self):
        self.to_close()
        self.after()
        original = self.read_json("decision.json")
        for field, bad in (("ticket", "OTHER-99"), ("ticket_file", "plans/x.md"), ("binding_ref", self.c0)):
            self.write_json("decision.json", dict(original, **{field: bad}))
            before = sha(self.ticket_path())
            d = self.reenter()
            self.assertEqual(d["next"], "ALREADY_CLOSED", field)
            self.assertIn("historical", d["reason"])
            rc, fin, _ = self.finalize()
            self.assertEqual((rc, fin["refused"]), (3, "binding_check_failed"), field)
            self.assertEqual(sha(self.ticket_path()), before, field)

    def test_historical_closed_without_decision_untouched(self):
        self.reg["lifecycle"] = "CLOSED"
        self.write_json("registry.json", self.reg)
        before = sha(self.ticket_path())
        self.preflight()
        d = self.decide()[1]
        self.assertEqual(d["next"], "ALREADY_CLOSED")
        self.assertFalse((self.dir / "decision.json").exists())
        self.assertEqual(sha(self.ticket_path()), before)

    def test_closed_with_final_result_present(self):
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo(marker=True, lifecycle="CLOSED")
        self.preflight()
        d = self.decide()[1]
        self.assertEqual((d["next"], d["reason"]), ("ALREADY_CLOSED", "final_result_present"))

    def test_review_decision_recomputed_from_verdicts(self):
        self.to_close()
        self.write_json("verdicts.json", self.verdicts(statuses=("FAIL", "PASS", "N/A")))
        rc, d, _ = self.decide()
        self.assertEqual(d["next"], "BLOCKED")
        self.assertEqual(self.read_json("decision.json")["next"], "BLOCKED")


# =============================================================================== 9. locate
class TestLocate(Base):
    def test_stale_pointer_found_in_archive(self):
        self.make_repo(ticket_dir="deliver/archive", pointer="deliver/TST-01_x.md", lifecycle="CLOSED", marker=True)
        self.set_dependents()
        rc, pre, _ = self.preflight()
        self.assertEqual((pre["location"], pre["file_field_stale"], pre["consistency"]["status"]),
                         ("deliver/archive", True, "PASS"))
        self.assertEqual(self.decide()[1]["next"], "ALREADY_CLOSED")

    def test_missing_ambiguous_and_contradiction(self):
        self.make_repo(pointer="deliver/gone/TST-01_x.md")
        dup = self.repo / PROJECT / "Pending" / "TST-01_x.md"
        dup.parent.mkdir(parents=True)
        put(dup, "dup\n")
        rc, pre, _ = self.preflight(dependents=False)
        self.assertEqual(pre["locate_status"], "ambiguous")
        self.write_json("verdicts.json", self.verdicts())
        d = self.decide()[1]
        self.assertEqual((d["next"], d["stop"]), ("PRECONDITION_FAILED", "STOP_EVIDENCE_CONFLICT"))
        dup.unlink()
        (self.repo / self.ticket_rel).unlink()
        rc, pre, _ = self.preflight(dependents=False)
        self.assertEqual(pre["locate_status"], "missing")
        self.assertEqual(self.decide()[1]["reason"], "ticket_file_missing")

    def test_lifecycle_location_contradiction(self):
        self.make_repo(ticket_dir="Pending", pointer="Pending/TST-01_x.md")
        self.preflight(dependents=False)
        self.write_json("verdicts.json", self.verdicts())
        d = self.decide()[1]
        self.assertEqual((d["next"], d["stop"]), ("PRECONDITION_FAILED", "STOP_EVIDENCE_CONFLICT"))
        self.assertFalse((self.dir / "decision.json").exists())

    def test_not_review_or_closed_is_precondition_failed(self):
        self.make_repo(ticket_dir="Pending", pointer="Pending/TST-01_x.md", lifecycle="IN_PROGRESS")
        self.preflight(dependents=False)
        self.write_json("verdicts.json", self.verdicts())
        d = self.decide()[1]
        self.assertEqual(d["next"], "PRECONDITION_FAILED")
        self.assertIn("IN_PROGRESS", d["reason"])


# =============================================================================== 10. scratch & sweep
class TestSweep(Base):
    NOW = 2_000_000_000

    def mk(self, root, name, age_days, nested=True):
        d = root / name
        d.mkdir(parents=True)
        files = [d]
        if nested:
            (d / "sub").mkdir()
            put((d / "f.txt"), "x")
            put((d / "sub" / "g.txt"), "y")
            files += [d / "sub", d / "f.txt", d / "sub" / "g.txt"]
        t = self.NOW - age_days * DAY
        for f in reversed(files):
            os.utime(f, (t, t))
        return d

    def sweep(self, root, *extra):
        rc, d, err = run_helper(self.root, "sweep", "--root", root, *extra, now=self.NOW)
        self.assertEqual(rc, 0, err)
        return d

    def test_expired_removed_recent_kept_boundary_strict(self):
        root = self.root / "close"
        root.mkdir()
        old = self.mk(root, "OLD-01", 8)
        edge = self.mk(root, "EDGE-01", 7)
        just = self.mk(root, "JUST-01", 7)
        os.utime(just, (self.NOW - 7 * DAY - 1,) * 2)
        for f in just.rglob("*"):
            os.utime(f, (self.NOW - 7 * DAY - 1,) * 2)
        os.utime(just, (self.NOW - 7 * DAY - 1,) * 2)
        fresh = self.mk(root, "NEW-01", 1)
        d = self.sweep(root)
        self.assertFalse(old.exists())
        self.assertFalse(just.exists())
        self.assertTrue(edge.exists())
        self.assertTrue(fresh.exists())
        self.assertEqual(sorted(d["removed"]), ["JUST-01", "OLD-01"])

    def test_newest_inner_file_keeps_candidate(self):
        root = self.root / "close"
        root.mkdir()
        d = self.mk(root, "BUSY-01", 30)
        os.utime(d / "sub" / "g.txt", (self.NOW - DAY,) * 2)
        self.sweep(root)
        self.assertTrue(d.exists())

    def test_exclude_pattern_and_non_directories_untouched(self):
        root = self.root / "close"
        root.mkdir()
        excl = self.mk(root, "KEEP-01", 30)
        odd = self.mk(root, "notaticket", 30)
        low = self.mk(root, "abc-01", 30)
        stray = root / "STRAY-01"
        put(stray, "file named like a ticket")
        os.utime(stray, (self.NOW - 30 * DAY,) * 2)
        self.sweep(root, "--exclude", excl)
        for p in (excl, odd, low, stray):
            self.assertTrue(p.exists(), p)

    def test_max_bounds_work(self):
        root = self.root / "close"
        root.mkdir()
        for n in ("AAA-01", "BBB-02", "CCC-03"):
            self.mk(root, n, 30)
        d = self.sweep(root, "--max", "2")
        self.assertEqual(len(d["removed"]), 2)
        self.assertTrue(any("over_max" in s for s in d["skipped"]))

    def make_link(self, link, target):
        try:
            os.symlink(target, link, target_is_directory=True)
            return True
        except (OSError, NotImplementedError):
            pass
        if os.name == "nt":
            r = subprocess.run(["cmd", "/c", "mklink", "/J", str(link), str(target)], capture_output=True)
            return r.returncode == 0
        return False

    def test_links_removed_but_never_followed(self):
        root = self.root / "close"
        root.mkdir()
        outside = self.root / "outside"
        outside.mkdir()
        put((outside / "precious.txt"), "keep")
        cand = self.mk(root, "LINK-01", 30)
        if not self.make_link(cand / "lnk", outside):
            self.skipTest("cannot create symlink/junction in this environment (privilege or filesystem)")
        t = self.NOW - 30 * DAY
        os.utime(cand, (t, t))
        d = self.sweep(root)
        self.assertFalse(cand.exists(), d)
        self.assertTrue((outside / "precious.txt").is_file())

    def test_candidate_that_is_a_link_is_skipped(self):
        root = self.root / "close"
        root.mkdir()
        outside = self.root / "outside2"
        outside.mkdir()
        put((outside / "keep.txt"), "k")
        if not self.make_link(root / "SELF-01", outside):
            self.skipTest("cannot create symlink/junction in this environment (privilege or filesystem)")
        self.sweep(root)
        self.assertTrue((outside / "keep.txt").is_file())

    @unittest.skipUnless(os.name == "nt", "open-handle deletion failure is only reproducible on Windows")
    def test_deletion_failure_never_fails_the_command(self):
        root = self.root / "close"
        root.mkdir()
        cand = self.mk(root, "LOCK-01", 30)
        with open(cand / "f.txt", "rb"):
            d = self.sweep(root)  # asserts rc == 0 inside
            self.assertTrue(d["errors"])
            self.assertTrue(cand.exists())

    def test_missing_root_is_harmless(self):
        d = self.sweep(self.root / "nope")
        self.assertEqual(d["removed"], [])

    def test_preflight_writes_only_scratch_and_creates_no_commit(self):
        self.make_repo()
        self.set_dependents()
        head = self.git("rev-parse", "HEAD")
        self.assertEqual(self.preflight()[0], 0)
        self.assertEqual(self.git("rev-parse", "HEAD"), head)
        self.assertEqual(self.git("status", "--porcelain"), "")
        self.assertTrue((self.dir / "preflight.json").is_file())


# =============================================================================== 11. final result
class TestFinalResult(Base):
    def test_section_is_single_small_and_self_contained(self):
        self.make_repo()
        self.write_summary()
        self.to_close()
        self.after()
        self.assertEqual(self.finalize()[0], 0)
        text = self.ticket_path().read_text(encoding="utf-8")
        section = text.split("<!-- close-result:begin -->")[1].split("<!-- close-result:end -->")[0]
        self.assertLessEqual(len(section.strip().splitlines()) + 2, 20)
        self.assertNotIn(".tmp", section)
        self.assertNotIn("line four", section)  # only the first 3 summary lines
        self.assertIn(self.impl, section)
        self.assertEqual(text.count("收束結果（close）"), 1)

    def test_not_closed_paths_never_write_the_ticket(self):
        self.make_repo()
        self.set_dependents()
        self.preflight()
        before = sha(self.ticket_path())
        for v in (self.verdicts(statuses=("PASS", "UNVERIFIED", "N/A")), self.verdicts(security_issue=True),
                  self.verdicts(statuses=("PASS",), cores=(True,))):
            self.write_json("verdicts.json", v)
            self.decide(2, 1)
            self.assertEqual(sha(self.ticket_path()), before)

    def test_disclosure_lists_unverified_non_core_and_unknown_dependency(self):
        self.make_repo()
        self.write_summary()
        v = self.verdicts(statuses=("PASS", "PASS", "UNVERIFIED"),
                          dependency_contract={"status": "UNKNOWN", "detail": ""})
        self.to_close(v)
        self.after()
        self.assertEqual(self.finalize()[0], 0)
        section = self.ticket_path().read_text(encoding="utf-8").split("<!-- close-result:begin -->")[1]
        self.assertIn("UNVERIFIED", section)
        self.assertIn("dependency", section)

    def test_unknown_scope_and_diff_check_are_disclosed(self):
        self.make_repo(impl=False)  # no own implementation commit -> scope and diff_check UNKNOWN
        self.write_summary()
        self.to_close_at_cap()
        self.after()
        self.assertEqual(self.finalize(commits="")[0], 0)
        section = self.ticket_path().read_text(encoding="utf-8").split("<!-- close-result:begin -->")[1]
        self.assertIn("實作 diff 範圍為 UNKNOWN", section)
        self.assertIn("git diff --check 為 UNKNOWN", section)

    def test_inline_marker_mentions_are_not_a_result_section_and_crlf_is_kept(self):
        self.make_repo()
        self.write_summary()
        text = self.ticket_path().read_text(encoding="utf-8")
        text = text.replace("- [x] done", "- [x] done (spec quotes `<!-- close-result:begin -->` and `<!-- close-result:end -->` inline)")
        put(self.ticket_path(), text.replace("\n", "\r\n"))
        self.to_close()
        self.assertFalse(self.read_json("preflight.json")["final_result_present"])
        self.after()
        self.assertEqual(self.finalize()[0], 0)
        data = self.ticket_path().read_bytes().decode("utf-8")
        self.assertIn("spec quotes `<!-- close-result:begin -->`", data)  # inline text untouched
        self.assertEqual(data.count("\n"), data.count("\r\n"))  # no bare LF introduced
        self.assertEqual(self.finalize()[0], 0)  # idempotent: replaces its own section only
        again = self.ticket_path().read_bytes().decode("utf-8")
        self.assertEqual(again, data)
        self.assertEqual(again.count("收束結果（close）"), 1)

    def to_close_at_cap(self):
        self.set_dependents()
        self.assertEqual(self.preflight()[0], 0)
        self.write_json("verdicts.json", self.verdicts())
        rc, d, _ = self.decide(2, 0)
        self.assertEqual((rc, d["next"]), (0, "CLOSE"))

    def test_no_report_file_enters_tracked_paths(self):
        self.make_repo()
        self.write_summary()
        self.to_close()
        self.after()
        self.finalize()
        tracked_dirty = self.git("status", "--porcelain").splitlines()
        self.assertEqual(tracked_dirty, [" M " + self.ticket_rel])


def _is_human_text(rec):
    """A message the human typed: not a tool_result carrier, not meta, not hook/system injected text."""
    if rec.get("type") != "user" or rec.get("isMeta"):
        return None
    c = (rec.get("message") or {}).get("content")
    texts = []
    if isinstance(c, str):
        texts = [c]
    elif isinstance(c, list):
        for b in c:
            if not isinstance(b, dict):
                continue
            if b.get("type") == "tool_result":
                return None
            if b.get("type") == "text":
                texts.append(b.get("text") or "")
    t = "\n".join(x for x in texts if x and x.strip()).strip()
    if not t:
        return None
    for pre in ("Stop hook", "<system-reminder>", "<task-notification>", "Base directory for this skill:",
                "<local-command", "(Re-invocation"):
        if t.startswith(pre):
            return None
    return t


def scan_skill_calls(path):
    """Structured scan of a transcript/stream jsonl: every Skill tool_use with the human message before it."""
    out, last = [], None
    with open(path, encoding="utf-8") as f:
        for ln, line in enumerate(f, 1):
            try:
                rec = json.loads(line)
            except ValueError:
                continue
            h = _is_human_text(rec)
            if h is not None:
                last = {"line": ln, "text": h}
                continue
            if rec.get("type") != "assistant":
                continue
            c = (rec.get("message") or {}).get("content")
            if not isinstance(c, list):
                continue
            for b in c:
                if isinstance(b, dict) and b.get("type") == "tool_use" and b.get("name") == "Skill":
                    inp = b.get("input") or {}
                    out.append({"line": ln, "skill": inp.get("skill"), "args": inp.get("args"), "prev_human": last})
    return out


def human_asked_for_close(call, tid):
    t = ((call.get("prev_human") or {}).get("text") or "")
    return bool(re.search(r"close\s+" + re.escape(tid), t) or "/ticket-author close" in t)


class TestSkillCallParser(unittest.TestCase):
    def jsonl(self, recs):
        d = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, d, True)
        p = Path(d) / "t.jsonl"
        p.write_text("\n".join(json.dumps(r, ensure_ascii=False) for r in recs) + "\n", encoding="utf-8")
        return p

    @staticmethod
    def user(text):
        return {"type": "user", "message": {"role": "user", "content": text}}

    @staticmethod
    def tool_result():
        return {"type": "user", "message": {"role": "user", "content": [
            {"type": "tool_result", "tool_use_id": "x", "content": "close TKA-X-01 mentioned in a tool output"}]}}

    @staticmethod
    def skill_call(inp):
        return {"type": "assistant", "message": {"role": "assistant", "content": [
            {"type": "tool_use", "id": "t1", "name": "Skill", "input": inp}]}}

    def test_key_order_does_not_matter_and_fields_are_structured(self):
        p = self.jsonl([self.user("please finish"), self.skill_call({"args": "close TKA-X-01", "skill": "ticket-author"})])
        calls = scan_skill_calls(p)
        self.assertEqual((calls[0]["skill"], calls[0]["args"]), ("ticket-author", "close TKA-X-01"))
        self.assertFalse(human_asked_for_close(calls[0], "TKA-X-01"))

    def test_tool_result_user_records_are_not_human_messages(self):
        p = self.jsonl([self.user("go on"), self.tool_result(), self.skill_call({"skill": "ticket-author", "args": "close TKA-X-01"})])
        c = scan_skill_calls(p)[0]
        self.assertEqual(c["prev_human"]["text"], "go on")  # the tool_result mentioning close is not the prompt
        self.assertFalse(human_asked_for_close(c, "TKA-X-01"))

    def test_human_who_typed_close_is_detected(self):
        p = self.jsonl([self.user("/ticket-author close TKA-X-01"), self.skill_call({"skill": "ticket-author", "args": "close TKA-X-01"})])
        self.assertTrue(human_asked_for_close(scan_skill_calls(p)[0], "TKA-X-01"))

    def test_hook_and_system_injections_are_not_human_prompts(self):
        for inj in ("Stop hook blocking error from command: close TKA-X-01", "<system-reminder>close TKA-X-01</system-reminder>",
                    "<task-notification>close TKA-X-01</task-notification>"):
            p = self.jsonl([self.user("work on the ticket"), self.user(inj),
                            self.skill_call({"skill": "ticket-author", "args": "close TKA-X-01"})])
            c = scan_skill_calls(p)[0]
            self.assertEqual(c["prev_human"]["text"], "work on the ticket", inj)
            self.assertFalse(human_asked_for_close(c, "TKA-X-01"), inj)

    def test_missing_args_other_tools_and_bad_lines(self):
        p = self.jsonl([self.user("x"), {"type": "assistant", "message": {"content": [
            {"type": "tool_use", "name": "Bash", "input": {"command": "close TKA-X-01"}}]}},
            self.skill_call({"skill": "qa"})])
        with open(p, "a", encoding="utf-8") as f:
            f.write("not json\n")
        calls = scan_skill_calls(p)
        self.assertEqual([(c["skill"], c["args"]) for c in calls], [("qa", None)])


class TestReviewerReplyCoverage(Base):
    def setUp(self):
        super().setUp()
        self.make_repo()
        self.set_dependents()
        self.preflight()

    def d(self, verdicts, rnd=0, fb=0, reply_text=None, replies=None):
        self.write_json("verdicts.json", verdicts)
        if reply_text is not None:
            put(self.dir / "reviewer_reply.txt", reply_text)
        rc, data, err = self.decide(rnd, fb, replies=replies)
        self.assertEqual(rc, 0, err)
        return data

    def test_reply_must_cover_every_ac_including_na_and_non_core(self):
        v = self.verdicts()
        d = self.d(v, reply_text="AC1: PASS | a\nAC2: PASS | b\n")  # AC3 (struck N/A, non-core) not judged
        self.assertEqual((d["next"], d["reason"], d["missing"]), ("SUPPLEMENT", "reviewer_coverage", [3]))
        d = self.d(v, rnd=2, reply_text="AC1: PASS | a\nAC2: PASS | b\n")
        self.assertEqual((d["next"], d["reason"]), ("NOT_CLOSED", "reviewer_coverage_incomplete"))
        self.assertEqual(self.d(v, reply_text="AC1: PASS | e\nAC2: PASS | e\nAC3: N/A | e\n")["next"], "CLOSE")

    def test_duplicate_unknown_and_malformed_reply_lines(self):
        v = self.verdicts()
        d = self.d(v, reply_text="AC1: PASS | e\nAC1: PASS | e\nAC2: PASS | e\nAC3: N/A | e\n")
        self.assertEqual((d["next"], d["duplicate"]), ("SUPPLEMENT", [1]))
        d = self.d(v, reply_text="AC1: PASS | e\nAC2: PASS | e\nAC3: N/A | e\nAC9: PASS | e\n")
        self.assertEqual((d["next"], d["unknown"]), ("SUPPLEMENT", [9]))
        d = self.d(v, reply_text="all good, everything passes\n")
        self.assertEqual((d["next"], d["missing"]), ("SUPPLEMENT", [1, 2, 3]))
        d = self.d(v, reply_text="- **AC1**: PASS | e\n* AC2：PASS | e\nAC3: n/a | e\n")  # bold, bullets, full-width colon, case
        self.assertEqual(d["next"], "CLOSE")

    def test_no_reply_file_at_all_never_closes(self):
        v = self.verdicts()
        v["_no_reply"] = True
        (self.dir / "reviewer_reply.txt").unlink(missing_ok=True)
        self.write_json("verdicts.json", v)
        rc, d, _ = self.decide()
        self.assertEqual((d["next"], d["reason"]), ("SUPPLEMENT", "reviewer_coverage"))
        self.assertTrue(d["unreadable"])
        rc, d, _ = self.decide(2, 0)
        self.assertEqual(d["next"], "NOT_CLOSED")

    def test_reply_vs_claim_mismatch(self):
        v = self.verdicts()
        d = self.d(v, reply_text="AC1: FAIL | broke\nAC2: PASS | e\nAC3: N/A | e\n")
        self.assertEqual((d["next"], d["kind"]), ("BLOCKED", "core_ac_fail"))  # judged on the reviewer's reply
        d = self.d(v, reply_text="AC1: PASS | e\nAC2: UNVERIFIED | unsure\nAC3: N/A | e\n")
        self.assertEqual((d["next"], d["reason"], d["acs"]), ("SUPPLEMENT", "reviewer_mismatch", [2]))
        self.assertEqual(self.d(v, rnd=2, reply_text="AC1: PASS | e\nAC2: UNVERIFIED | e\nAC3: N/A | e\n")["next"], "NOT_CLOSED")
        v2 = self.verdicts()
        v2["acs"][1]["override_evidence"] = "git grep shows the string exists (mechanical)"
        d = self.d(v2, reply_text="AC1: PASS | e\nAC2: UNVERIFIED | unsure\nAC3: N/A | e\n")
        # TKA-TRUST-01 F2: free-text override_evidence carries no authority; only a reviewer re-check can change the status
        self.assertEqual((d["next"], d["reason"], d["acs"]), ("SUPPLEMENT", "reviewer_mismatch", [2]))
        self.assertNotIn("overrides", d)

    def test_reviewer_must_be_a_real_second_call(self):
        for patch, code in (({"inline": True}, "reviewer_not_independent"),
                            ({"independent_call": False}, "reviewer_not_independent"),
                            ({"inline": None}, "reviewer_meta_invalid"),
                            ({"actual_model": ""}, "reviewer_meta_invalid"),
                            ({"actual_model": "gpt-9"}, "reviewer_model_unknown"),
                            ({"family": "claude", "actual_model": "opus"}, "reviewer_fallback_unjustified"),
                            ({"family": "claude", "actual_model": "opus", "fallback_used": True, "fallback_reason": "report_fix"},
                             "reviewer_fallback_unjustified")):
            v = self.verdicts()
            v["reviewer"].update(patch)
            d = self.d(v)
            self.assertEqual((d["next"], d["reason"], d["detail"]), ("SUPPLEMENT", "reviewer_invalid", code), patch)
            self.assertEqual(self.d(v, rnd=2)["next"], "NOT_CLOSED", patch)
        v = self.verdicts()
        v["reviewer"] = None
        self.assertEqual(self.d(v)["detail"], "reviewer_meta_missing")
        v = self.verdicts()
        v["reviewer"] = {"family": "claude", "actual_model": "opus", "tier": "heavy", "fallback_used": True,
                         "fallback_reason": "quota", "independent_call": True, "inline": False}
        self.assertEqual(self.d(v)["next"], "CLOSE")  # justified Claude fallback is a valid, separately recorded reviewer

    def test_heavy_ticket_cannot_close_on_luna_and_light_may_escalate(self):
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo(allowed="x.py")
        self.set_dependents()
        self.preflight()
        v = self.verdicts()
        v["reviewer"].update(actual_model="gpt-6-luna", tier="light", tier_basis="plain wording fix")
        d = self.d(v)
        self.assertEqual((d["next"], d["detail"]), ("SUPPLEMENT", "reviewer_tier_mismatch"))
        # the escalation is not shadowed by the coverage cap on a light ticket
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo()
        self.set_dependents()
        self.preflight()
        v = self.verdicts()
        v["reviewer"].update(actual_model="gpt-6-luna", tier="light", tier_basis="plain wording fix")
        d = self.d(v, rnd=2, fb=0, reply_text="AC1: PASS | e\nAC2: UNVERIFIED | e\nAC3: N/A | e\n")
        self.assertEqual((d["next"], d["reviewer"]), ("ESCALATE_REVIEWER", "gpt-6-sol"))
        d = self.d(v, rnd=2, fb=1, reply_text="AC1: PASS | e\nAC2: UNVERIFIED | e\nAC3: N/A | e\n")
        self.assertEqual(d["next"], "NOT_CLOSED")

    def test_status_without_evidence_is_not_a_judgement(self):
        v = self.verdicts()
        d = self.d(v, reply_text="AC1: PASS\nAC2: PASS | \nAC3: N/A | **\n")
        self.assertEqual((d["next"], d["reason"], d["no_evidence"]), ("SUPPLEMENT", "reviewer_coverage", [1, 2, 3]))
        self.assertEqual(self.d(v, rnd=2, reply_text="AC1: PASS\nAC2: PASS\nAC3: N/A\n")["next"], "NOT_CLOSED")

    def test_claude_fallback_model_must_match_the_tier(self):
        v = self.verdicts()
        v["reviewer"] = {"family": "claude", "actual_model": "sonnet", "tier": "heavy", "fallback_used": True,
                         "fallback_reason": "quota", "independent_call": True, "inline": False}
        d = self.d(v)
        self.assertEqual((d["next"], d["detail"]), ("SUPPLEMENT", "reviewer_model_unknown"))
        v["reviewer"]["actual_model"] = "opus"
        self.assertEqual(self.d(v)["next"], "CLOSE")

    def test_later_reply_overrides_earlier_for_the_acs_it_covers(self):
        v = self.verdicts()
        self.write_json("verdicts.json", v)
        put(self.dir / "reviewer_reply.txt", "AC1: PASS | e\nAC2: UNVERIFIED | e\nAC3: N/A | e\n")
        put(self.dir / "reviewer_reply2.txt", "AC2: PASS | escalated\n")
        rc, d, _ = self.decide(0, 1, replies=["reviewer_reply.txt", "reviewer_reply2.txt"])
        self.assertEqual((rc, d["next"]), (0, "CLOSE"))
        self.assertEqual(self.read_json("decision.json")["coverage"], {"covered": 3, "total": 3})

    def test_old_shaped_verdicts_without_reply_or_reviewer_fields_do_not_close(self):
        old = {"reviewer": {"primary": "sonnet", "actual": "sonnet", "fallback_used": False, "limitation": ""},
               "acs": self.verdicts()["acs"], "security_issue": False, "irreversible_risk": False,
               "dependency_contract": {"status": "NONE_FOUND", "detail": ""}, "checkpoint_matter_resolved": None,
               "_no_reply": True}
        (self.dir / "reviewer_reply.txt").unlink(missing_ok=True)
        self.write_json("verdicts.json", old)
        rc, d, _ = self.decide()
        self.assertNotEqual(d["next"], "CLOSE")

    def test_finalize_discloses_model_tier_coverage_and_fallback(self):
        self.write_summary()
        v = self.verdicts()
        v["reviewer"] = {"family": "claude", "actual_model": "opus", "tier": "heavy", "fallback_used": True,
                         "fallback_reason": "quota", "independent_call": True, "inline": False, "limitation": "x"}
        self.write_json("verdicts.json", v)
        self.assertEqual(self.decide()[1]["next"], "CLOSE")
        self.after()
        self.assertEqual(self.finalize()[0], 0)
        sec = self.ticket_path().read_text(encoding="utf-8").split("<!-- close-result:begin -->")[1]
        for needle in ("claude/opus", "tier=heavy", "fallback:quota", "覆蓋 AC 3／3（全部）", "independent_call=True"):
            self.assertIn(needle, sec)

    def test_non_review_lifecycle_is_a_read_only_judgement(self):
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo(ticket_dir="Pending", pointer="Pending/TST-01_x.md", lifecycle="IN_PROGRESS")
        self.preflight(dependents=False)
        before = sha(self.ticket_path())
        self.write_json("verdicts.json", self.verdicts())
        rc, d, _ = self.decide()
        self.assertEqual((d["next"], d["read_only"]), ("PRECONDITION_FAILED", True))
        self.assertFalse((self.dir / "decision.json").exists())
        self.assertEqual(sha(self.ticket_path()), before)


class TestUpstreamWiring(unittest.TestCase):
    """Contract layer only: text/grep checks of the real repo files. Not evidence of real Agent behaviour."""

    @staticmethod
    def text(rel):
        return (REAL_REPO / rel).read_text(encoding="utf-8")

    def test_skill_tool_form_and_no_slash_form(self):
        sv, ssot = self.text(".claude/skills/sv/SKILL.md"), self.text(".claude/rules/ssot-governance.md")
        for t in (sv, ssot):
            self.assertIn("`Skill(ticket-author)`", t)
            self.assertIn("close <", t)
            self.assertNotIn("呼叫 `/ticket-author close", t)
        step11 = sv.split("## Step 11")[1]
        self.assertIn("Skill(ticket-author)", step11.split("4. **自主收束**")[1].split("5. **最終覆蓋確認**")[0])

    def test_rapid_is_gone_and_low_risk_is_named_consistently(self):
        for rel in (".claude/rules/ssot-governance.md", ".claude/skills/sv/SKILL.md", ".claude/skills/uc/SKILL.md",
                    ".claude/hooks/gate_stop_guard.py"):
            self.assertNotIn("RAPID", self.text(rel), rel)
        ssot = self.text(".claude/rules/ssot-governance.md")
        self.assertIn("低風險免 QA／SV 票", ssot)
        self.assertIn("一般票（不符上列低風險條件）", ssot)
        self.assertNotIn("低風險免審", ssot)
        row = [ln for ln in ssot.splitlines() if ln.startswith("| 低風險免 QA／SV 票")][0]
        for cond in ("①票面 `ALLOWED` 全為 `.md`", "②無 `app/` production code", "③票面無不可逆操作", "④可 `git revert`",
                     "check commit", "Skill(qa)", "Skill(sv)", "Step 11 第 1–5 項"):
            self.assertIn(cond, row)

    def test_low_risk_condition_text_matches_claude_md_source(self):
        claude = self.text(".claude/CLAUDE.md")
        self.assertIn("低風險 docs／治理文本票（僅 `.md`、可 git revert）", claude)
        self.assertIn("觸及 `app/` production code、腳本／pipeline 或不可逆操作之票維持全程序", claude)

    def test_blocker_and_limit_rows_hand_over_to_tka(self):
        ssot = self.text(".claude/rules/ssot-governance.md")
        blocker = [ln for ln in ssot.splitlines() if ln.startswith("| SV 或 Codex 判 BLOCKER")][0]
        for needle in ("Skill(ticket-author)", "唯讀", "不假轉 `REVIEW`", "lifecycle 不變"):
            self.assertIn(needle, blocker)
        rf = [ln for ln in ssot.splitlines() if ln.startswith("| SV／Codex 判 REPORT_FIX")][0]
        self.assertIn("LIMIT 本身不是永久停車條件", rf)
        self.assertIn("`reviews.codex` 填 `N_A` 不寫 `PASS`", rf)
        self.assertNotIn("人工裁決 checkpoint", rf)
        self.assertIn("6. **BLOCKER 交接**", self.text(".claude/skills/sv/SKILL.md"))

    def test_uc_rule8_has_the_low_risk_branch(self):
        uc = self.text(".claude/skills/uc/SKILL.md")
        self.assertIn("低風險免 QA／SV 票", uc)
        self.assertIn("Step 11 第 1–5 項", uc)
        self.assertIn("Skill(ticket-author)", uc)

    def test_ticket_id_source_is_not_only_verify_cmd_conf(self):
        step11 = self.text(".claude/skills/sv/SKILL.md").split("## Step 11")[1]
        self.assertIn("目前正式票上下文", step11)
        self.assertIn("MUST NOT", step11.split("目前正式票上下文")[1][:300])
        self.assertNotIn("（`<TICKET_ID>` 取自 `tmp/verify_cmd.conf`）；exit 0 續行", step11)

    def test_skill_close_mode_contract(self):
        sk = self.text(".claude/skills/ticket-author/SKILL.md")
        head = sk.split("---", 2)[1]
        self.assertNotIn("Manual modes: diag, plan, ticket, close", head)
        mode = sk.split("## Mode: close")[1].split("## Conditional state matrix")[0]
        for needle in ("EVERY AC", "reviewer_reply", "gpt-6-sol", "gpt-6-luna", "--codex-fault",
                       "ESCALATE_REVIEWER", "never the implementing Agent answering inline", "`skill` = `ticket-author`",
                       "read-only judgement", "Cross-Review verdict history"):
            self.assertIn(needle, mode)
        self.assertNotIn("ask the other reviewer", mode)
        self.assertIn("--reply <dir>/reviewer_reply.txt", mode)

    def test_light_classification_is_a_superset_of_the_low_risk_condition(self):
        from importlib.util import module_from_spec, spec_from_file_location
        spec = spec_from_file_location("close_helper_under_test", str(HELPER))
        ch = module_from_spec(spec)
        spec.loader.exec_module(ch)
        self.assertEqual(ch.DOC_EXT, {".md", ".txt", ".rst"})
        self.assertEqual(ch.classify(["plans/a.md"]), "light")
        self.assertEqual(ch.classify(["docs/n.txt"]), "light")  # reviewer-light, but not low-risk (".md" only)
        self.assertEqual(ch.classify([".claude/skills/x/SKILL.md"]), "heavy")
        self.assertEqual((ch.CODEX_HEAVY, ch.CODEX_LIGHT), ("gpt-6-sol", "gpt-6-luna"))

    def test_guard_prompt_no_longer_allows_stopping_before_close(self):
        g = self.text(".claude/hooks/gate_stop_guard.py")
        self.assertIn("Skill(ticket-author)", g)
        self.assertNotIn("可停止", g)

    def test_state_transitions_names_escalated_and_fault_reviewers(self):
        st = self.text(".claude/skills/ticket-author/references/state-transitions.md")
        self.assertIn("escalated reviewer", st)
        self.assertNotIn("fallback reviewer", st)


class TestRegistrySubjectBinding(Base):
    """A Registry JSON that belongs to another Ticket must never be bound to the CLI ticket id."""

    def setUp(self):
        super().setUp()
        self.make_repo()
        self.set_dependents()

    def test_preflight_refuses_foreign_registry_object(self):
        self.write_json("registry.json", dict(self.reg, id="OTHER-99"))
        rc, d, _ = self.preflight()
        self.assertEqual((rc, d["refused"]), (3, "registry_id_mismatch"))
        self.assertFalse((self.dir / "preflight.json").exists())

    def test_decide_rejects_foreign_registry_object(self):
        self.assertEqual(self.preflight()[0], 0)
        self.write_json("registry.json", dict(self.reg, id="OTHER-99"))
        self.write_json("verdicts.json", self.verdicts())
        rc, d, _ = self.decide()
        self.assertEqual((d["next"], d["reason"], d["stop"]),
                         ("PRECONDITION_FAILED", "registry_id_mismatch", "STOP_EVIDENCE_CONFLICT"))
        self.assertFalse((self.dir / "decision.json").exists())

    def test_registry_file_pointing_to_another_tickets_file_is_a_conflict(self):
        text = self.ticket_path().read_text(encoding="utf-8").replace("TICKET_ID=TST-01", "TICKET_ID=OTHER-99")
        put(self.ticket_path(), text)
        rc, pre, _ = self.preflight()
        self.assertEqual((rc, pre["consistency"]["status"]), (0, "FAIL"))
        self.assertIn("ticket_file_id=OTHER-99", pre["consistency"]["detail"])
        self.write_json("verdicts.json", self.verdicts())
        rc, d, _ = self.decide()
        self.assertEqual((d["next"], d["stop"]), ("PRECONDITION_FAILED", "STOP_EVIDENCE_CONFLICT"))
        self.assertFalse((self.dir / "decision.json").exists())

    def test_readback_and_finalize_reject_foreign_registry_object(self):
        self.to_close()
        self.write_summary()
        self.after()
        after = self.read_json("registry_after.json")
        self.write_json("registry_after.json", dict(after, id="OTHER-99"))
        before = sha(self.ticket_path())
        rc, rb, _ = self.h("readback", "--ticket", TID, "--registry", self.dir / "registry_after.json")
        self.assertEqual((rc, rb["ok"]), (0, False))  # readback reports via `ok`, exit code stays 0
        self.assertTrue(any(m.startswith("id=") for m in rb["mismatches"]))
        rc, fin, _ = self.finalize()
        self.assertEqual((rc, fin["refused"]), (3, "readback_mismatch"))
        self.assertEqual(sha(self.ticket_path()), before)


# =============================================================================== 12. trust boundary (TKA-TRUST-01)
NOW = 1_800_000_000


def L(n, st, core=None, ev="e"):
    """One reviewer reply line in the single contract format: `AC<n>: PASS|FAIL|N/A|UNVERIFIED | core=Y|N | <short evidence>`."""
    if core is None:
        return "AC%d: %s | %s\n" % (n, st, ev)
    return "AC%d: %s | core=%s | %s\n" % (n, st, core, ev)


class TestTrustBoundary(Base):
    """Original counterexamples of F1-F5 plus the normal/recovery regressions. Mechanical tests: they prove helper
    decisions and side effects, not the semantic quality of a real reviewer."""

    def setup_ticket(self, acs=DEFAULT_ACS, allowed="docs/a.md"):
        self.make_repo(allowed=allowed, acs=acs)
        self.set_dependents()
        self.assertEqual(self.preflight()[0], 0)

    def dec(self, v, reply=None, rnd=0, fb=0, reply2=None):
        """decide with an explicit raw reply (reply=None: the harness derives a faithful reply from the claims)."""
        if reply is None:
            self.write_json("verdicts.json", v)
            replies = ["reviewer_reply.txt"]
        else:
            self.write_json("verdicts.json", dict(v, _no_reply=True))
            put(self.dir / "reviewer_reply.txt", reply)
            replies = ["reviewer_reply.txt"]
        if reply2 is not None:
            put(self.dir / "reviewer_reply2.txt", reply2)
            replies.append("reviewer_reply2.txt")
        rc, d, err = self.decide(rnd, fb, replies=replies)
        self.assertEqual(rc, 0, err)
        return d

    def after_and_summary(self):
        self.after()
        self.write_summary()

    def fin(self):
        return self.h("finalize", "--ticket", TID, "--registry", self.dir / "registry_after.json",
                      "--verdicts", self.dir / "verdicts.json", "--summary", self.dir / "summary.txt",
                      "--commits", self.impl or "", now=NOW)

    def section(self):
        text = self.ticket_path().read_text(encoding="utf-8")
        a, b = text.index("<!-- close-result:begin -->"), text.index("<!-- close-result:end -->")
        return text[a:b]

    def test_contract_text_has_one_reply_format_and_no_override_path(self):
        sk = SKILL_MD.read_text(encoding="utf-8")
        mode = sk.split("## Mode: close")[1].split("## Conditional state matrix")[0]
        for needle in ("AC<n>: PASS|FAIL|N/A|UNVERIFIED | core=Y|N | <short evidence>", "TIER: UPGRADE", "--simple-basis",
                       "snapshot", "core_disagreement", "preflight_stale", "never counted as evidence"):
            self.assertIn(needle, mode)
        self.assertNotIn("override_evidence", mode)
        fmt = "AC<n>: PASS|FAIL|N/A|UNVERIFIED | core=Y|N | <short evidence>"
        self.assertIn(fmt, HELPER.read_text(encoding="utf-8"))  # parser docstring
        self.assertIn(fmt, HERE.read_text(encoding="utf-8"))  # the test helper that builds the reply lines
        self.assertNotIn("override_evidence", HELPER.read_text(encoding="utf-8"))
        finalize = [ln for ln in mode.splitlines() if "close_helper.py finalize" in ln][0]
        self.assertNotIn("--verdicts", finalize)
        sample = "AC7: FAIL | core=N | regression in the secondary path\n"  # the documented form parses to status+core+evidence
        put(self.root / "r.txt", sample)
        spec = __import__("importlib.util").util.spec_from_file_location("close_helper_fmt", str(HELPER))
        ch = __import__("importlib.util").util.module_from_spec(spec)
        spec.loader.exec_module(ch)
        st, prob, info = ch.parse_reviewer_replies([self.root / "r.txt"], 9)
        self.assertEqual((st[7], info["core"][7], info["ev"][7]), ("FAIL", "N", "regression in the secondary path"))

    # ---------------------------------------------------------------- F1
    def test_f1_finalize_is_snapshot_only(self):
        self.setup_ticket()
        self.to_close()
        self.after_and_summary()
        before = self.ticket_path().read_bytes()
        rc, fin, _ = self.fin()
        self.assertEqual(rc, 0, fin)
        expected = self.ticket_path().read_bytes()
        self.assertNotEqual(expected, before)
        swapped = self.verdicts(statuses=("FAIL", "FAIL", "FAIL"), cores=(True, True, True))
        swapped["reviewer"]["limitation"] = "swapped-in text that must not appear"
        for variant in ("missing", "empty", "swapped"):
            self.ticket_path().write_bytes(before)
            vp = self.dir / "verdicts.json"
            if variant == "missing":
                vp.unlink()
            elif variant == "empty":
                put(vp, "{}")
            else:
                put(vp, json.dumps(swapped))
            rc, fin, _ = self.fin()
            self.assertEqual(rc, 0, (variant, fin))
            self.assertEqual(self.ticket_path().read_bytes(), expected, variant)
        self.assertNotIn("swapped-in", self.ticket_path().read_text(encoding="utf-8"))

    def test_f1_result_follows_the_snapshot_values(self):
        """Reachability trace: decide's snapshot is finalize's only source - editing it moves the result, editing verdicts does not."""
        self.setup_ticket()
        self.to_close()
        self.after_and_summary()
        pristine = self.ticket_path().read_bytes()
        snap_path = self.dir / "decision.json"
        orig = json.loads(snap_path.read_text(encoding="utf-8"))
        rc, fin, _ = self.fin()
        self.assertEqual(rc, 0, fin)
        base_section = self.section()
        self.assertIn("核心 AC 2 條皆為 PASS", base_section)
        self.assertIn("非核心 PASS 0／N/A 1／UNVERIFIED 0／FAIL 0（共 3 條 AC）", base_section)
        edits = {
            "status": lambda d: d["snapshot"]["acs"][2].update(status="FAIL", ev="snapshot-only secondary-path fact"),
            "core": lambda d: d["snapshot"]["acs"][2].update(core=True, status="PASS"),
        }
        for name, edit in edits.items():
            dec = json.loads(json.dumps(orig))
            edit(dec)
            put(snap_path, json.dumps(dec))
            self.ticket_path().write_bytes(pristine)
            rc, fin, _ = self.fin()
            self.assertEqual(rc, 0, (name, fin))
            sec = self.section()
            self.assertNotEqual(sec, base_section, name)
            if name == "status":
                self.assertIn("非核心 FAIL 1 條: AC3 snapshot-only secondary-path fact", sec)
                self.assertIn("FAIL 1（共 3 條 AC）", sec)
            else:
                self.assertIn("核心 AC 3 條皆為 PASS", sec)
        # verdicts.json edits are invisible to the result
        put(snap_path, json.dumps(orig))
        self.ticket_path().write_bytes(pristine)
        put(self.dir / "verdicts.json", json.dumps(self.verdicts(statuses=("FAIL", "FAIL", "FAIL"), cores=(True, True, True))))
        rc, fin, _ = self.fin()
        self.assertEqual(rc, 0, fin)
        self.assertEqual(self.section(), base_section)

    def test_f1_invalid_snapshot_or_binding_refuses_and_leaves_ticket_untouched(self):
        self.setup_ticket()
        self.to_close()
        self.after_and_summary()
        orig = (self.dir / "decision.json").read_text(encoding="utf-8")
        cases = {
            "no_snapshot": lambda d: d.pop("snapshot"),
            "ac_dropped": lambda d: d["snapshot"]["acs"].pop(),
            "ac_duplicated": lambda d: d["snapshot"]["acs"].append(dict(d["snapshot"]["acs"][0])),
            "bad_status": lambda d: d["snapshot"]["acs"][0].__setitem__("status", "MAYBE"),
            "core_fail": lambda d: d["snapshot"]["acs"][0].update(status="FAIL", core=True),
            "core_not_bool": lambda d: d["snapshot"]["acs"][0].__setitem__("core", "yes"),
            "inline": lambda d: d["snapshot"]["reviewer"].__setitem__("inline", True),
            "not_independent": lambda d: d["snapshot"]["reviewer"].__setitem__("independent_call", False),
            "no_reviewer": lambda d: d["snapshot"].pop("reviewer"),
            "not_close": lambda d: d.__setitem__("next", "SUPPLEMENT"),
            "binding": lambda d: d.__setitem__("binding_ref", "0" * 40),
        }
        for name, fn in cases.items():
            dec = json.loads(orig)
            fn(dec)
            put(self.dir / "decision.json", json.dumps(dec))
            before = sha(self.ticket_path())
            rc, fin, _ = self.fin()
            self.assertEqual(rc, 3, (name, fin))
            self.assertEqual(sha(self.ticket_path()), before, name)
        (self.dir / "decision.json").unlink()  # the 7-day scratch is gone: never claim success
        before = sha(self.ticket_path())
        rc, fin, _ = self.fin()
        self.assertEqual(rc, 3, fin)
        self.assertEqual(sha(self.ticket_path()), before)
        self.assertNotIn("close-result:begin", self.ticket_path().read_text(encoding="utf-8"))

    def test_f1_snapshot_content_and_single_reviewer_producer(self):
        self.setup_ticket()
        v = self.verdicts()
        v["reviewer"]["tier_basis"] = "n/a for heavy"
        d = self.dec(v, L(1, "PASS", "Y", "first fact") + L(2, "PASS", "Y", "second fact") + L(3, "N/A", "N", "struck"))
        self.assertEqual(d["next"], "CLOSE", d)
        snap = self.read_json("decision.json")["snapshot"]
        self.assertEqual([a["n"] for a in snap["acs"]], [1, 2, 3])
        self.assertEqual([(a["status"], a["core"]) for a in snap["acs"]], [("PASS", True), ("PASS", True), ("N/A", False)])
        self.assertEqual(snap["acs"][0]["ev"], "first fact")
        rev = snap["reviewer"]
        for key in ("family", "actual_model", "tier", "fallback_used", "fallback_reason", "independent_call", "inline",
                    "limitation", "tier_basis"):
            self.assertIn(key, rev)
        self.assertEqual((rev["actual_model"], rev["inline"], rev["independent_call"]), ("gpt-6-sol", False, True))
        self.assertEqual(snap["dependency_status"], "NONE_FOUND")

    def test_f1_legacy_decision_without_snapshot_is_not_resumed(self):
        self.setup_ticket()
        self.to_close()
        self.after_and_summary()
        dec = self.read_json("decision.json")
        dec.pop("snapshot")
        put(self.dir / "decision.json", json.dumps(dec))
        self.assertNotIn("snapshot", self.read_json("decision.json"))  # starting shape: legacy, below the normalized writer
        self.reg["lifecycle"] = "CLOSED"
        self.write_json("registry.json", self.reg)
        self.preflight()
        d = self.decide()[1]
        self.assertEqual(d["next"], "ALREADY_CLOSED", d)
        self.assertIn("snapshot", d["binding_check"])
        before = sha(self.ticket_path())
        rc, fin, _ = self.fin()
        self.assertEqual(rc, 3, fin)
        self.assertEqual(sha(self.ticket_path()), before)

    def test_f1_partial_success_resumes_from_the_snapshot_even_without_verdicts(self):
        self.setup_ticket()
        self.to_close()
        self.after_and_summary()
        self.reg["lifecycle"] = "CLOSED"
        self.write_json("registry.json", self.reg)
        self.preflight()
        self.assertEqual(self.decide()[1]["next"], "WRITE_FINAL_ONLY")
        (self.dir / "verdicts.json").unlink()
        rc, fin, _ = self.fin()
        self.assertEqual((rc, fin["written"]), (0, True), fin)
        self.assertIn("核心 AC 2 條皆為 PASS", self.section())

    # ---------------------------------------------------------------- F2
    def test_f2_override_evidence_cannot_release_core_fail_or_unverified(self):
        self.setup_ticket()
        for st in ("FAIL", "UNVERIFIED"):
            v = self.verdicts()  # claims AC2 PASS (core)
            v["acs"][1]["override_evidence"] = "git grep shows the string exists (free text)"
            d = self.dec(v, L(1, "PASS", "Y") + L(2, st, "Y") + L(3, "N/A", "N"))
            self.assertNotEqual(d["next"], "CLOSE", st)
            if st == "FAIL":
                self.assertEqual((d["next"], d["kind"], d["acs"]), ("BLOCKED", "core_ac_fail", [2]))
            else:
                self.assertEqual((d["next"], d["reason"], d["acs"]), ("SUPPLEMENT", "reviewer_mismatch", [2]))
                self.assertNotIn("overrides", d)

    def test_f2_point_recheck_updates_only_covered_ac_and_rejects_stale_replies(self):
        self.setup_ticket()
        v = self.verdicts()
        r1 = L(1, "PASS", "Y") + L(2, "UNVERIFIED", "Y") + L(3, "N/A", "N")
        self.assertEqual(self.dec(v, r1)["reason"], "reviewer_mismatch")
        r2 = L(2, "PASS", "Y", "re-verified against the new evidence")
        d = self.dec(v, r1, reply2=r2)
        self.assertEqual(d["next"], "CLOSE", d)  # AC1/AC3 stay as judged in the first reply
        pre_mtime = (self.dir / "preflight.json").stat().st_mtime
        old = pre_mtime - 100
        os.utime(self.dir / "reviewer_reply2.txt", (old, old))
        rc, d, _ = self.decide(replies=["reviewer_reply.txt", "reviewer_reply2.txt"])
        self.assertEqual((d["next"], d["reason"]), ("SUPPLEMENT", "reviewer_reply_stale"), d)

    def test_f2_preflight_stale_when_delivery_changed_after_preflight(self):
        self.setup_ticket()
        put(self.repo / "docs" / "a.md", "alpha changed after preflight\n")
        self.commit("[TST-01] more", "docs/a.md")
        d = self.dec(self.verdicts())  # replies are newer than preflight.json, but the delivery moved on
        self.assertEqual((d["next"], d["reason"]), ("SUPPLEMENT", "preflight_stale"), d)
        d = self.dec(self.verdicts(), rnd=2)
        self.assertEqual((d["next"], d["reason"]), ("NOT_CLOSED", "preflight_stale"), d)
        self.assertEqual(self.preflight()[0], 0)
        d = self.dec(self.verdicts())  # fresh preflight + fresh replies
        self.assertEqual(d["next"], "CLOSE", d)

    # ---------------------------------------------------------------- F3
    def test_f3_core_downgrade_requires_reviewer_confirmation(self):
        self.setup_ticket()
        v = self.verdicts(statuses=("PASS", "FAIL", "N/A"), cores=(True, False, False))  # agent says AC2 is non-core
        for mark in (None, "Y"):
            r = L(1, "PASS", "Y") + L(2, "FAIL", mark, "regression found") + L(3, "N/A", "N")
            d = self.dec(v, r)
            self.assertEqual((d["next"], d["reason"], d["acs"]), ("SUPPLEMENT", "core_disagreement", [2]), mark)
            d = self.dec(v, r, rnd=2)
            self.assertEqual((d["next"], d["kind"], d["acs"]), ("BLOCKED", "core_ac_fail", [2]), mark)
        r = L(1, "PASS", "Y") + L(2, "FAIL", "N", "cosmetic path only, not the delivery goal") + L(3, "N/A", "N")
        d = self.dec(v, r)
        self.assertEqual(d["next"], "CLOSE", d)  # reviewer-confirmed non-core FAIL does not block
        snap = self.read_json("decision.json")["snapshot"]
        self.assertEqual((snap["acs"][1]["status"], snap["acs"][1]["core"]), ("FAIL", False))

    def test_f3_reviewer_noncore_vs_agent_core_is_a_disagreement_until_synced(self):
        self.setup_ticket()
        v = self.verdicts(statuses=("PASS", "FAIL", "N/A"), cores=(True, True, False))
        r = L(1, "PASS", "Y") + L(2, "FAIL", "N", "not the delivery goal") + L(3, "N/A", "N")
        d = self.dec(v, r)
        self.assertEqual((d["next"], d["reason"], d["acs"]), ("SUPPLEMENT", "core_disagreement", [2]))
        d = self.dec(v, r, rnd=2)
        self.assertEqual((d["next"], d["kind"]), ("BLOCKED", "core_ac_fail"))  # unresolved: treated as core
        v["acs"][1]["core"] = False  # synchronised after the reviewer's pointed confirmation
        self.assertEqual(self.dec(v, r)["next"], "CLOSE")
        v["acs"][1]["core"] = False  # the Agent flips alone while the reviewer says core=Y: no downgrade
        r_y = L(1, "PASS", "Y") + L(2, "FAIL", "Y", "goal not met") + L(3, "N/A", "N")
        self.assertEqual(self.dec(v, r_y)["reason"], "core_disagreement")

    def test_f3_core_marker_is_not_evidence(self):
        self.setup_ticket()
        v = self.verdicts(statuses=("PASS", "FAIL", "N/A"), cores=(True, False, False))
        r = L(1, "PASS", "Y") + "AC2: FAIL | core=N\n" + L(3, "N/A", "N")
        d = self.dec(v, r)
        self.assertEqual((d["next"], d["reason"], d["missing"]), ("SUPPLEMENT", "reviewer_coverage", [2]), d)
        r_legacy = "AC1: PASS | e\nAC2: PASS | e\nAC3: N/A | e\n"  # legacy lines without marker still parse
        self.assertEqual(self.dec(self.verdicts(), r_legacy)["next"], "CLOSE")

    # ---------------------------------------------------------------- F4
    @staticmethod
    def acs_n(n_noncore):
        lines = ["- [ ] **[存在性]** core one\n"]
        lines += ["- [ ] **[行為性]** non-core %d\n" % (i + 2) for i in range(n_noncore)]
        lines.append("- [ ] ~~`assembleDebug` PASS~~ (N/A: docs only)\n")
        return "".join(lines)

    def close_with_noncore_fails(self, n_noncore):
        self.setup_ticket(acs=self.acs_n(n_noncore))
        statuses = ("PASS",) + ("FAIL",) * n_noncore + ("N/A",)
        cores = (True,) + (False,) * n_noncore + (False,)
        reply = L(1, "PASS", "Y")
        for i in range(n_noncore):
            reply += L(i + 2, "FAIL", "N", "does not handle empty input case number %d in the secondary path" % (i + 2))
        reply += L(n_noncore + 2, "N/A", "N")
        d = self.dec(self.verdicts(statuses=statuses, cores=cores), reply)
        self.assertEqual(d["next"], "CLOSE", d)
        self.after_and_summary()
        rc, fin, _ = self.fin()
        self.assertEqual(rc, 0, fin)
        return self.section()

    def test_f4_noncore_fail_count_numbers_and_short_facts_are_disclosed(self):
        sec = self.close_with_noncore_fails(2)
        self.assertIn("非核心 FAIL 2", sec)
        self.assertIn("AC2", sec)
        self.assertIn("AC3", sec)
        self.assertIn("FAIL 2", sec)
        self.assertIn("共 4 條 AC", sec)
        self.assertIn("核心 AC 1 條", sec)
        self.assertNotIn("does not handle empty input case number 2 in the secondary path", sec)  # compact, not verbatim

    def test_f4_many_noncore_fails_are_never_silently_truncated(self):
        sec = self.close_with_noncore_fails(10)
        for n in range(2, 12):
            self.assertIn("AC%d" % n, sec, n)
        self.assertIn("非核心 FAIL 10", sec)
        self.assertIn("共 12 條 AC", sec)

    # ---------------------------------------------------------------- F5
    def reviewer_for(self, allowed, *flags):
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.make_repo(allowed=allowed)
        self.set_dependents()
        self.preflight()
        rc, d, err = self.h("reviewer", "--ticket", TID, "--codex-available", "1", *flags)
        self.assertEqual(rc, 0, err)
        return d

    def test_f5_load_bearing_documents_stay_on_sol_whatever_the_basis(self):
        for allowed in ("plans/architecture/x.md", ".claude/rules/r.md", "docs/SSOT_v4.md", "docs/api_Contract.md",
                        "plans/cso_briefing/project/file/T.md"):
            d = self.reviewer_for(allowed, "--simple-basis", "trivial typo fix, nothing load-bearing")
            self.assertEqual((d["class"], d["actual"]["model"]), ("heavy", "gpt-6-sol"), allowed)

    def test_f5_light_needs_structural_simplicity_and_an_explicit_basis(self):
        d = self.reviewer_for("plans/x/note.md")
        self.assertEqual((d["class"], d["actual"]["model"]), ("heavy", "gpt-6-sol"))  # uncertain: Sol
        d = self.reviewer_for("plans/x/note.md", "--simple-basis", "   ")
        self.assertEqual(d["class"], "heavy")
        d = self.reviewer_for("plans/x/note.md", "--simple-basis", "single typo in prose")
        self.assertEqual((d["class"], d["actual"]["model"]), ("light", "gpt-6-luna"))
        self.assertIn("single typo", d["tier_basis"])
        d = self.reviewer_for("plans/x/note.md", "--simple-basis", "single typo", "--force-heavy")
        self.assertEqual(d["class"], "heavy")  # nothing lowers a raised tier

    def test_f5_decide_rejects_luna_without_a_basis_or_on_load_bearing_documents(self):
        self.setup_ticket(allowed="docs/a.md")
        v = self.verdicts()
        v["reviewer"].update(actual_model="gpt-6-luna", tier="light")
        d = self.dec(v)
        self.assertEqual((d["next"], d["reason"], d["detail"]), ("SUPPLEMENT", "reviewer_invalid", "reviewer_tier_mismatch"), d)
        v["reviewer"]["tier_basis"] = "plain wording fix"
        self.assertEqual(self.dec(v)["next"], "CLOSE")
        self.root = Path(tempfile.mkdtemp(dir=str(self.root)))
        self.setup_ticket(allowed="plans/architecture/x.md")
        d = self.dec(v)  # any basis string, load-bearing document
        self.assertEqual((d["next"], d["detail"]), ("SUPPLEMENT", "reviewer_tier_mismatch"), d)

    def test_f5_luna_tier_upgrade_report_escalates_to_sol_only_in_the_valid_state(self):
        self.setup_ticket(allowed="docs/a.md")
        v = self.verdicts()
        v["reviewer"].update(actual_model="gpt-6-luna", tier="light", tier_basis="plain wording fix")
        faithful = L(1, "PASS", "Y") + L(2, "PASS", "Y") + L(3, "N/A", "N")
        up = "TIER: UPGRADE | the ticket changes acceptance-gate semantics\n" + faithful
        d = self.dec(v, up)
        self.assertEqual((d["next"], d["reviewer"]), ("ESCALATE_REVIEWER", "gpt-6-sol"), d)
        d = self.dec(v, up, fb=1, reply2=faithful)  # the stronger reviewer already judged: the line is ignored
        self.assertEqual(d["next"], "CLOSE", d)
        d = self.dec(v, faithful, reply2=up)  # a Luna point re-check that only carries the line in reply 2 still escalates
        self.assertEqual((d["next"], d["reviewer"]), ("ESCALATE_REVIEWER", "gpt-6-sol"), d)
        sol = self.verdicts()
        d = self.dec(sol, up)  # not a Luna reply: ignored
        self.assertEqual(d["next"], "CLOSE", d)


if __name__ == "__main__":
    unittest.main()
