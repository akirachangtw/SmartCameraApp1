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

    def decide(self, rnd=0, fb=0, extra=()):
        return self.h("decide", "--ticket", TID, "--registry", self.dir / "registry.json",
                      "--verdicts", self.dir / "verdicts.json", "--round", rnd, "--fallback-tried", fb, *extra)

    def set_dependents(self, tickets=None):
        self.write_json("dependents.json", {"tickets": tickets if tickets is not None else [{"id": TID}]})

    @staticmethod
    def verdicts(statuses=("PASS", "PASS", "N/A"), cores=(True, True, False), **kw):
        v = {"reviewer": {"primary": "sonnet", "actual": "sonnet", "fallback_used": False, "limitation": ""},
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
               "Agent 最高可寫 lifecycle 為", "並於 checkpoint 揭露豁免依據"]
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
        self.assertEqual(len(cmds), 5)
        self.set_dependents()
        head_before = self.git("rev-parse", "HEAD")
        rc, pre = self.exec(cmds[0])
        self.assertEqual((rc, pre["location"], pre["scope"]["status"], pre["ac_count"]), (0, "deliver", "PASS", 3))
        rc, rv = self.exec(cmds[1])
        self.assertEqual((rc, rv["primary"], rv["actual"]), (0, "sonnet", "sonnet"))
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
        fb = self.d(v, rnd=2, fb=0, extra=("--codex-available", 1))
        self.assertEqual((fb["next"], fb["reviewer"]), ("FALLBACK_REVIEWER", "codex"))
        self.assertEqual(self.d(v, rnd=2, fb=1)["next"], "NOT_CLOSED")
        self.assertEqual(self.d(v, rnd=2, fb=0, extra=("--codex-available", 0))["next"], "NOT_CLOSED")
        self.assertEqual(self.d(v, rnd=2, fb=1)["reason"], "core_unverified")

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
        self.assertEqual(self.run_decide(v, 2)["next"], "FALLBACK_REVIEWER")

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

    def test_classification_by_extension_not_preset(self):
        d = self.reviewer("docs/a.md,docs/n.txt")
        self.assertEqual((d["class"], d["primary"], d["actual"]), ("docs", "sonnet", "sonnet"))
        d = self.reviewer("docs/a.md,scripts/x.py")
        self.assertEqual((d["class"], d["primary"]), ("code", "codex"))

    def test_fallbacks(self):
        d = self.reviewer("docs/a.md", "--sonnet-available", "0", "--codex-available", "1")
        self.assertEqual((d["actual"], d["fallback_used"]), ("codex", True))
        self.assertTrue(d["limitation"])
        d = self.reviewer("x.py", "--codex-available", "0", "--sonnet-available", "1")
        self.assertEqual((d["actual"], d["fallback_used"]), ("sonnet", True))
        d = self.reviewer("x.py", "--codex-available", "0", "--sonnet-available", "0")
        self.assertEqual(d["actual"], "none")

    def test_escalate_names_other_reviewer_and_acs(self):
        d = self.reviewer("docs/a.md", "--codex-available", "1", escalate="2,3")
        self.assertEqual(d["escalation"], {"reviewer": "codex", "acs": [2, 3]})
        d = self.reviewer("docs/a.md", "--codex-available", "0", escalate="2")
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
        self.assertEqual((d["primary"], d["actual"]), ("codex", "codex"))


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

    def test_no_report_file_enters_tracked_paths(self):
        self.make_repo()
        self.write_summary()
        self.to_close()
        self.after()
        self.finalize()
        tracked_dirty = self.git("status", "--porcelain").splitlines()
        self.assertEqual(tracked_dirty, [" M " + self.ticket_rel])


if __name__ == "__main__":
    unittest.main()
