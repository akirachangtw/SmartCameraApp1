"""Tests for gate_ledger.py (ADV-GATE-02). Standard library unittest only.

Every git operation happens inside a tempfile repository. The real repository's
.git, the real second worktree, Git configuration and the default TARGETS of
tools/cleanup_expired_tmp.py are never touched. PPSS cases are offline: no
research call, no network.
"""
from __future__ import annotations

import ast
import contextlib
import importlib.util
import io
import json
import os
import shutil
import stat
import subprocess
import sys
import tempfile
import time
import unittest
from pathlib import Path
from unittest import mock

SCRIPTS = Path(__file__).resolve().parent.parent
REPO_ROOT = SCRIPTS.parent.parent
SCRIPT = SCRIPTS / "gate_ledger.py"
sys.path.insert(0, str(SCRIPTS))
import gate_ledger as gl  # noqa: E402

PROJECT = ("plans", "cso_briefing", "project")
REAL_ARTIFACT = Path(r"D:\DevSources\workflow\mcp-ticket-platform\data\research\2026-10\rsch-20261011-66f685d4c6.json")


def _rm_readonly(func, path, _exc):
    os.chmod(path, stat.S_IWRITE)
    func(path)


def rmtree(path):
    shutil.rmtree(path, onerror=_rm_readonly)


def git(cwd, *args):
    res = subprocess.run(["git", "-c", "user.name=t", "-c", "user.email=t@example.invalid",
                          "-c", "commit.gpgsign=false", *args],
                         cwd=cwd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if res.returncode != 0:
        raise RuntimeError(f"git {args} failed: {res.stderr}")
    return res.stdout.strip()


class RepoCase(unittest.TestCase):
    """Temporary repository with a pending ticket PEND-01 helper."""

    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp(prefix="gl_test_")).resolve()
        self.addCleanup(rmtree, self.tmp)
        self.repo = self.tmp / "repo"
        self.repo.mkdir()
        git(self.repo, "init", "-q")
        (self.repo / "README.md").write_text("x", encoding="utf-8")
        (self.repo / ".gitignore").write_text("plans/\ntmp/\n.tmp/\ntools/\n.claude/\n", encoding="utf-8")
        git(self.repo, "add", "README.md", ".gitignore")
        git(self.repo, "commit", "-q", "-m", "init")
        for sub in ("Pending", "deliver", "void"):
            self.repo.joinpath(*PROJECT, sub).mkdir(parents=True)
        self.ticket("ADV-TEST-01")

    def ticket(self, tid, where="Pending", name=None):
        path = self.repo.joinpath(*PROJECT, where, name or f"{tid}_x.md")
        path.write_text("# t\n", encoding="utf-8")
        return path

    def active(self, tid):
        d = self.repo / "tmp" / "verify"
        d.mkdir(parents=True, exist_ok=True)
        (d / "active.json").write_text(json.dumps({"ticket_id": tid}), encoding="utf-8")

    def env(self):
        env = {k: v for k, v in os.environ.items() if k not in ("PYTHONIOENCODING", "PYTHONUTF8")}
        return env

    def run_cli(self, *args, cwd=None, ticket="ADV-TEST-01"):
        argv = [sys.executable, str(SCRIPT)]
        if ticket:
            argv += ["--ticket", ticket]
        argv += list(args)
        res = subprocess.run(argv, cwd=cwd or self.repo, capture_output=True, env=self.env())
        res.out = res.stdout.decode("utf-8", errors="replace")
        res.err = res.stderr.decode("utf-8", errors="replace")
        return res

    def ledger_path(self, tid="ADV-TEST-01"):
        return self.repo / ".git" / "gate" / f"{tid}.jsonl"

    def events(self, tid="ADV-TEST-01"):
        p = self.ledger_path(tid)
        if not p.exists():
            return []
        return [json.loads(x) for x in p.read_text(encoding="utf-8").splitlines() if x.strip()]

    def status(self, **kw):
        res = self.run_cli("status", "--json", **kw)
        self.assertEqual(res.returncode, 0, res.err)
        return json.loads(res.out)

    def open_incident(self, subject="test:a", **extra):
        args = ["incident", "open", "--subject", subject]
        for k, v in extra.items():
            args += [f"--{k.replace('_', '-')}", v]
        res = self.run_cli(*args)
        self.assertIn(res.returncode, (0, 5), res.err)
        return json.loads(res.out)

    def round(self, inc, expect, strategy, result=None, authority="ALLOWED"):
        res = self.run_cli("round", "start", "--incident", inc, "--expect", str(expect),
                           "--strategy", strategy, "--authority", authority)
        if res.returncode == 0 and result:
            end = self.run_cli("round", "end", "--incident", inc, "--result", result)
            self.assertEqual(end.returncode, 0, end.err)
        return res


class RoundsAndHuman(RepoCase):
    def test_round_definition(self):
        inc = self.open_incident()["incident"]
        for _ in range(2):
            self.assertEqual(self.run_cli("step-limit", "--incident", inc, "--source", "X").returncode, 0)
        self.assertEqual(self.status()["incidents"][0]["rounds_started"], 0)
        before = len(self.events())
        self.assertEqual(self.round(inc, 0, "").returncode, 5)
        bad = self.run_cli("round", "start", "--incident", inc, "--expect", "0", "--strategy", "s1")
        self.assertEqual(bad.returncode, 5)
        self.assertEqual(self.round(inc, 0, "S one", "fail").returncode, 0)
        self.assertEqual(self.round(inc, 1, "  s ONE ").returncode, 5)  # same strategy after normalisation
        self.assertEqual(len(self.events()), before + 2)

    def _fail_rounds(self, inc, n):
        for i in range(n):
            self.assertEqual(self.round(inc, i, f"strategy {i}", "fail").returncode, 0)

    def test_cross_gate_point_and_exhausted(self):
        a = self.open_incident("test:a", gate_point="qa", verdict="REPORT_FIX")["incident"]
        b = self.open_incident("test:a", gate_point="sv", verdict="BLOCKER")
        self.assertEqual((b["incident"], b["action"]), (a, "attached"))
        self._fail_rounds(a, 3)
        res = self.run_cli("human", "--category", "EXHAUSTED", "--incident", a, "--basis", "rounds_3")
        self.assertEqual(res.returncode, 0, res.err)
        lines = [x for x in res.out.splitlines() if x.strip()]
        self.assertEqual(lines, [f"HUMAN_REQUIRED: EXHAUSTED | ADV-TEST-01 | {a} | round=3/3 | rounds_3"])
        self.assertEqual(self.round(a, 3, "strategy 4").returncode, 5)

    def test_independent_evidence(self):
        a = self.open_incident("test:a")["incident"]
        r = self.open_incident("test:b")
        self.assertEqual((r["action"], r["incident"]), ("attached_instead_of_new", a))
        self.assertEqual(len(self.status()["incidents"]), 1)
        b = self.open_incident("test:c", independent_evidence="out.txt")
        self.assertEqual(b["action"], "new")
        self._fail_rounds(a, 2)
        self.assertEqual(self.round(b["incident"], 0, "fresh", "fail").returncode, 0)
        st = {i["id"]: i for i in self.status()["incidents"]}
        self.assertEqual((st[a]["rounds_started"], st[b["incident"]]["rounds_started"]), (2, 1))

    def test_third_round_not_truncated_and_pass_closes(self):
        a = self.open_incident()["incident"]
        self._fail_rounds(a, 2)
        self.run_cli("step-limit", "--incident", a, "--source", "CHECKPOINT_REPORT_FIX_LIMIT")
        self.assertEqual(self.round(a, 2, "third strategy", "pass").returncode, 0)
        inc = self.status()["incidents"][0]
        self.assertEqual((inc["status"], inc["rounds_started"]), ("closed", 3))
        b = self.open_incident("test:z")["incident"]
        self.assertEqual(self.round(b, 0, "only", "pass").returncode, 0)
        self.assertEqual({i["id"]: i["status"] for i in self.status()["incidents"]}[b], "closed")

    def test_exhausted_only_after_three(self):
        a = self.open_incident()["incident"]
        self.run_cli("consult", "--incident", a, "--kind", "local")
        self.run_cli("consult", "--incident", a, "--kind", "advisor")
        self.run_cli("strategy-scan", "--incident", a, "--item", "x::none authorised")
        self._fail_rounds(a, 2)
        for basis in ("rounds_3", "rounds_2"):
            res = self.run_cli("human", "--category", "EXHAUSTED", "--incident", a, "--basis", basis)
            self.assertEqual(res.returncode, 5)
        ok = self.run_cli("human", "--category", "PERMISSION", "--incident", a, "--basis", "denied")
        self.assertEqual(ok.returncode, 0)
        res = self.run_cli("human", "resolve", "--ref", "akira")
        self.assertEqual(res.returncode, 0, res.err)
        self._fail_rounds_third = self.round(a, 2, "third", "fail")
        self.run_cli("human", "--category", "EXHAUSTED", "--incident", a, "--basis", "rounds_3")
        res = self.run_cli("human", "resolve", "--ref", "akira")
        self.assertEqual(res.returncode, 5)
        self.assertEqual(self.round(a, 3, "fourth").returncode, 5)

    def test_no_authorized_strategy(self):
        a = self.open_incident()["incident"]
        self._fail_rounds(a, 2)
        cat = ("human", "--category", "NO_AUTHORIZED_STRATEGY", "--incident", a, "--basis", "none left")
        self.assertEqual(self.run_cli(*cat).returncode, 5)
        self.run_cli("consult", "--incident", a, "--kind", "local")
        self.assertEqual(self.run_cli(*cat).returncode, 5)
        self.run_cli("consult", "--incident", a, "--kind", "advisor")
        self.assertEqual(self.run_cli(*cat).returncode, 5)
        self.assertEqual(self.run_cli("strategy-scan", "--incident", a, "--item", "x::").returncode, 5)
        self.assertEqual(self.run_cli("strategy-scan", "--incident", a, "--item", "::y").returncode, 5)
        self.assertEqual(self.run_cli("strategy-scan", "--incident", a, "--item", "retry::already tried").returncode, 0)
        self.assertEqual(self.run_cli(*cat).returncode, 0)
        src = SCRIPT.read_text(encoding="utf-8")
        self.assertNotIn("def search_strategy", src)

    def test_check_and_receipt(self):
        res = self.run_cli("check", "qa")
        self.assertEqual(res.returncode, 0, res.err)
        late = self.run_cli("check", "done", "--late")
        self.assertEqual(late.returncode, 0)
        a = self.open_incident()["incident"]
        self.run_cli("human", "--category", "PERMISSION", "--incident", a, "--basis", "denied")
        blocked = self.run_cli("check", "sv")
        self.assertEqual(blocked.returncode, 1)
        self.assertIn("PENDING_HUMAN: PERMISSION", blocked.out)
        self.assertEqual(self.run_cli("check", "bogus").returncode, 2)
        receipts = [e for e in self.events() if e["type"] == "check"]
        self.assertEqual([r["gate_point"] for r in receipts], ["qa", "done", "sv"])
        self.assertEqual([r["late"] for r in receipts], [False, True, False])
        self.assertEqual({r["ticket_source"] for r in receipts}, {"explicit"})
        head = git(self.repo, "rev-parse", "HEAD")
        self.assertEqual(receipts[0]["head"], head)
        self.assertEqual(set(receipts[0]) - {"seq", "ts", "type", "ticket", "gate_point", "ticket_source",
                                             "head", "exit", "late"}, set())

    def test_blocks_and_missed_check_do_not_change_rounds(self):
        a = self.open_incident()["incident"]
        self.run_cli("blocks", "--note", "n")
        self.run_cli("missed-check", "--gate-point", "qa")
        st = self.status()
        self.assertEqual((st["incidents"][0]["rounds_started"], st["blocks"], st["missed_checks"]), (0, 1, 1))
        self.assertEqual(self.round(a, 0, "s", "fail").returncode, 0)

    def test_utf8_output_under_legacy_codepage(self):
        a = self.open_incident("test:中文")["incident"]
        res = self.round(a, 0, "修補策略 一", "fail")
        self.assertEqual(res.returncode, 0, res.err)
        self.assertEqual(self.run_cli("strategy-scan", "--incident", a, "--item", "策略::無授權").returncode, 0)
        st = self.run_cli("status")
        self.assertEqual(st.returncode, 0, st.err)
        self.ledger_path().read_text(encoding="utf-8")  # valid UTF-8
        self.assertIn("修補策略 一", self.status()["incidents"][0]["strategies"][0])


class Identity(RepoCase):
    def test_explicit_over_active_and_no_activation(self):
        self.ticket("ADV-TEST-02")
        self.active("ADV-TEST-01")
        conf = self.repo / "tmp" / "verify_cmd.conf"
        conf.write_text("TICKET_ID=ADV-TEST-01\n", encoding="utf-8")
        before = ((self.repo / "tmp" / "verify" / "active.json").read_bytes(), conf.read_bytes())
        res = self.run_cli("incident", "open", "--subject", "s", ticket="ADV-TEST-02")
        self.assertEqual(res.returncode, 0, res.err)
        self.assertTrue(self.ledger_path("ADV-TEST-02").exists())
        self.assertFalse(self.ledger_path("ADV-TEST-01").exists())
        self.run_cli("check", "qa", ticket="ADV-TEST-02")
        self.assertEqual(self.events("ADV-TEST-02")[-1]["ticket_source"], "explicit")
        after = ((self.repo / "tmp" / "verify" / "active.json").read_bytes(), conf.read_bytes())
        self.assertEqual(before, after)
        res = self.run_cli("check", "qa", ticket=None)
        self.assertEqual(res.returncode, 0, res.err)
        self.assertEqual(self.events("ADV-TEST-01")[-1]["ticket_source"], "active_json")

    def test_no_ticket_and_invalid_ids(self):
        res = self.run_cli("check", "qa", ticket=None)
        self.assertEqual(res.returncode, 3)
        self.assertFalse((self.repo / ".git" / "gate").exists())
        for bad in ("../x", "a/b", "adv-test-01", "ADV-TEST", "ADV-TEST-01\\x", ""):
            res = self.run_cli("check", "qa", ticket=bad or None)
            self.assertEqual(res.returncode, 3, bad)
        self.assertFalse((self.repo / ".git" / "gate").exists())
        self.assertFalse(any("unknown" in p.name for p in (self.repo / ".git").iterdir()))

    def test_locations(self):
        self.ticket("ADV-TEST-03", "deliver")
        self.ticket("ADV-TEST-04", "void")
        self.ticket("ADV-TEST-05", "Pending")
        self.ticket("ADV-TEST-05", "deliver")
        for tid, loc in (("ADV-TEST-03", "loc:deliver"), ("ADV-TEST-04", "loc:void"), ("ADV-TEST-05", "loc:multi")):
            for cmd in (("check", "qa"), ("incident", "open", "--subject", "s"), ("blocks",)):
                res = self.run_cli(*cmd, ticket=tid)
                self.assertEqual(res.returncode, 4, (tid, cmd, res.err))
            self.assertFalse(self.ledger_path(tid).exists())
        res = self.run_cli("status", "--json", ticket="ADV-TEST-03")  # file exists, no ledger: read-only, exit 0
        self.assertEqual(res.returncode, 0, res.err)
        self.assertEqual(json.loads(res.out)["ledger_exists"], False)
        self.assertFalse(self.ledger_path("ADV-TEST-03").exists())
        # history stays readable after the ticket leaves Pending
        self.assertEqual(self.run_cli("incident", "open", "--subject", "s").returncode, 0)
        moved = self.repo.joinpath(*PROJECT, "Pending", "ADV-TEST-01_x.md")
        shutil.move(str(moved), str(self.repo.joinpath(*PROJECT, "deliver", "ADV-TEST-01_x.md")))
        st = self.status()
        self.assertEqual(st["ticket_location"], "loc:deliver")
        self.assertEqual(len(st["incidents"]), 1)
        self.assertEqual(self.run_cli("round", "start", "--incident", "INC-001", "--expect", "0",
                                      "--strategy", "s", "--authority", "a").returncode, 4)
        self.assertEqual(self.run_cli("status", ticket="ADV-NOFILE-01").returncode, 4)  # loc:none, no ledger

    def test_stale_active_json(self):
        self.ticket("ADV-TEST-06", "deliver")
        self.active("ADV-TEST-06")
        self.assertEqual(self.run_cli("check", "qa", ticket=None).returncode, 4)
        self.assertTrue(self.run_cli("status", ticket=None).returncode in (0, 4))
        # give it a ledger so status can show the stale marker
        (self.repo / ".git" / "gate").mkdir(exist_ok=True)
        self.ledger_path("ADV-TEST-06").write_text(
            json.dumps({"seq": 1, "ts": "t", "ticket": "ADV-TEST-06", "type": "blocks"}) + "\n", encoding="utf-8")
        res = self.run_cli("status", "--json", ticket=None)
        self.assertEqual(res.returncode, 0, res.err)
        out = json.loads(res.out)
        self.assertTrue(out["stale"])
        self.assertEqual(out["ticket_location"], "loc:deliver")

    def test_worktree_shares_ledger(self):
        wt = self.tmp / "wt"
        git(self.repo, "worktree", "add", "-q", "--detach", str(wt))
        a = self.run_cli("incident", "open", "--subject", "s")
        self.assertEqual(a.returncode, 0, a.err)
        b = self.run_cli("blocks", "--note", "from-wt", cwd=wt)
        self.assertEqual(b.returncode, 0, b.err)
        c = self.run_cli("check", "qa", cwd=wt)
        self.assertEqual(c.returncode, 0, c.err)
        self.assertEqual([e["seq"] for e in self.events()], [1, 2, 3])
        common_main = gl.get_common_dir(self.repo)
        common_wt = gl.get_common_dir(wt)
        self.assertEqual(common_main, common_wt)
        self.assertTrue(common_wt.is_absolute())
        self.assertFalse((wt / ".git" / "gate").exists())
        self.assertEqual(git(self.repo, "status", "--short"), "")
        self.assertEqual(git(wt, "status", "--short"), "")
        self.assertEqual(self.run_cli("status", cwd=wt).returncode, 0)


class Concurrency(RepoCase):
    def test_eight_processes_one_round_winner(self):
        inc = self.open_incident()["incident"]
        procs = []
        for i in range(8):
            argv = [sys.executable, str(SCRIPT), "--ticket", "ADV-TEST-01", "round", "start", "--incident", inc,
                    "--expect", "0", "--strategy", f"s{i}", "--authority", "a"]
            procs.append(subprocess.Popen(argv, cwd=self.repo, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                          env=self.env()))
            procs.append(subprocess.Popen([sys.executable, str(SCRIPT), "--ticket", "ADV-TEST-01", "blocks",
                                           "--note", str(i)], cwd=self.repo, stdout=subprocess.PIPE,
                                          stderr=subprocess.PIPE, env=self.env()))
        codes = [p.wait(timeout=120) for p in procs]
        for p in procs:
            p.stdout.close()
            p.stderr.close()
        self.assertEqual(sorted(codes[0::2]), [0] + [5] * 7)
        self.assertEqual(codes[1::2], [0] * 8)
        ev = self.events()
        self.assertEqual([e["seq"] for e in ev], list(range(1, len(ev) + 1)))
        st = self.status()
        self.assertEqual((st["incidents"][0]["rounds_started"], st["blocks"]), (1, 8))
        self.assertFalse((self.repo / ".git" / "gate" / "ADV-TEST-01.jsonl.lock").exists())


class LedgerTail(RepoCase):
    def test_damaged_tail_refuses_mutation_and_leaves_file_unchanged(self):
        self.assertEqual(self.run_cli("incident", "open", "--subject", "s").returncode, 0)
        led = self.ledger_path()
        original = led.read_bytes()
        good_json_no_newline = json.dumps({"seq": 2, "ts": "t", "ticket": "ADV-TEST-01",
                                           "type": "blocks", "note": None}).encode("utf-8")
        for tail in (b'{"seq": 2, "ty', good_json_no_newline):
            led.write_bytes(original + tail)
            before = led.read_bytes()
            for cmd in (("blocks", "--note", "x"), ("check", "qa"),
                        ("round", "start", "--incident", "INC-001", "--expect", "0",
                         "--strategy", "s2", "--authority", "a")):
                res = self.run_cli(*cmd)
                self.assertEqual(res.returncode, 6, (tail, cmd, res.err))
                self.assertIn("unterminated tail", res.err)
                self.assertEqual(led.read_bytes(), before, (tail, cmd))
                self.assertFalse(led.with_name(led.name + ".lock").exists())
            # read-only status keeps working (partial tail ignored)
            self.assertEqual(self.run_cli("status", "--json").returncode, 0)
            self.assertEqual(led.read_bytes(), before)

    def test_corrupt_middle_line_also_refuses_and_keeps_bytes(self):
        self.assertEqual(self.run_cli("incident", "open", "--subject", "s").returncode, 0)
        led = self.ledger_path()
        led.write_bytes(b'not json\n' + led.read_bytes())
        before = led.read_bytes()
        res = self.run_cli("blocks", "--note", "x")
        self.assertEqual(res.returncode, 6, res.err)
        self.assertEqual(led.read_bytes(), before)


class CarrierSurvival(RepoCase):
    def test_reset_inject_and_cleanup_leave_ledger(self):
        self.run_cli("incident", "open", "--subject", "s")
        self.run_cli("blocks", "--note", "n")
        led = self.ledger_path()
        before = led.read_bytes()
        self.active("ADV-TEST-01")
        conf = self.repo / "tmp" / "verify_cmd.conf"
        conf.write_text("x", encoding="utf-8")
        hooks = self.repo / ".claude" / "hooks"
        hooks.mkdir(parents=True)
        shutil.copy(REPO_ROOT / ".claude" / "hooks" / "session_start.sh", hooks / "session_start.sh")
        bash = shutil.which("bash")
        usable = bash and not any(x in bash.lower() for x in ("windowsapps", "system32"))
        if usable:
            for mode in ("reset", "inject"):
                subprocess.run([bash, ".claude/hooks/session_start.sh", mode], cwd=self.repo,
                               capture_output=True, timeout=60)
        else:
            conf.unlink()
        self.assertFalse(conf.exists())
        self.assertTrue((self.repo / "tmp" / "verify" / "active.json").exists())
        # cleanup_expired_tmp.py copy, TARGETS limited to the temp repo only
        tools = self.repo / "tools"
        tools.mkdir()
        shutil.copy(REPO_ROOT / "tools" / "cleanup_expired_tmp.py", tools / "cleanup_expired_tmp.py")
        spec = importlib.util.spec_from_file_location("cleanup_copy", tools / "cleanup_expired_tmp.py")
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        old = self.repo / "tmp" / "old.txt"
        old.write_text("old", encoding="utf-8")
        past = time.time() - 9 * 86400
        os.utime(old, (past, past))
        hidden = self.repo / ".tmp"
        hidden.mkdir()
        mod.TARGETS = {"smartcamera": self.repo / "tmp", "smartcamera-.tmp": hidden}
        for name in mod.TARGETS:
            with contextlib.redirect_stdout(io.StringIO()):
                self.assertEqual(mod.clean_target(name, False), 0)
        self.assertFalse(old.exists())
        self.assertEqual(led.read_bytes(), before)
        self.assertEqual(self.status()["blocks"], 1)
        for target in mod.TARGETS.values():
            self.assertNotIn(self.repo / ".git", target.parents)


class LockCases(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp(prefix="gl_lock_")).resolve()
        self.addCleanup(rmtree, self.tmp)
        self.lock = self.tmp / "T.jsonl.lock"
        self.children = []
        self.addCleanup(self._reap)

    def _reap(self):
        for c in self.children:
            if c.poll() is None:
                c.kill()
            c.wait()

    def sleeper(self):
        p = subprocess.Popen([sys.executable, "-c", "import time; time.sleep(60)"])
        self.children.append(p)
        return p

    def write_lock(self, pid, age):
        self.lock.write_text(json.dumps({"pid": pid, "ts": time.time() - age}), encoding="utf-8")
        past = time.time() - age
        os.utime(self.lock, (past, past))

    def test_1_transient_permission_error_is_retried(self):
        real = os.open
        calls = {"n": 0}

        def flaky(path, flags, *a, **k):
            if str(path) == str(self.lock) and calls["n"] < 3:
                calls["n"] += 1
                raise PermissionError(13, "delete pending")
            return real(path, flags, *a, **k)

        with mock.patch.object(gl.os, "open", flaky):
            gl.acquire_lock(self.lock, timeout=5, stale=30)
        self.assertEqual(calls["n"], 3)
        self.assertTrue(self.lock.exists())
        gl.release_lock(self.lock)

    def test_2_persistent_permission_error_is_bounded_and_keeps_original(self):
        def denied(path, flags, *a, **k):
            raise PermissionError(13, "Access is denied")

        t0 = time.monotonic()
        with mock.patch.object(gl.os, "open", denied):
            with self.assertRaises(gl.LockTimeout) as cm:
                gl.acquire_lock(self.lock, timeout=0.5, stale=30)
        self.assertLess(time.monotonic() - t0, 5)
        self.assertIsInstance(cm.exception.__cause__, PermissionError)
        self.assertIn("PermissionError", str(cm.exception))
        self.assertIn("errno=13", str(cm.exception))
        self.assertEqual(cm.exception.code, gl.EXIT_IO)
        self.assertFalse(self.lock.exists())

    def test_2b_permission_error_outside_acquire_is_not_retried(self):
        led = gl.Ledger(self.tmp, "ADV-TEST-01", lock_timeout=2, stale_seconds=30)
        calls = {"n": 0}

        def boom(*a, **k):
            calls["n"] += 1
            raise PermissionError(13, "denied")

        with mock.patch.object(gl.os, "makedirs", boom):
            with self.assertRaises(gl.GateError) as cm:
                with led.locked():
                    pass
        self.assertEqual((calls["n"], cm.exception.code), (1, gl.EXIT_IO))
        led2 = gl.Ledger(self.tmp, "ADV-TEST-02", lock_timeout=2, stale_seconds=30)
        with mock.patch.object(led2, "_write_line", side_effect=PermissionError(13, "denied")) as w:
            with self.assertRaises(PermissionError):
                with led2.locked():
                    led2._write_line("x")
        self.assertEqual(w.call_count, 1)
        self.assertFalse(led2.lock_path.exists())

    def test_3_live_owner_is_not_reclaimed(self):
        child = self.sleeper()
        self.write_lock(child.pid, age=120)
        raw = self.lock.read_bytes()
        with self.assertRaises(gl.LockTimeout) as cm:
            gl.acquire_lock(self.lock, timeout=0.6, stale=0.1)
        self.assertEqual(self.lock.read_bytes(), raw)
        self.assertIn("not reclaimed", str(cm.exception))

    def test_3b_fresh_lock_of_live_owner_is_not_reclaimed(self):
        self.write_lock(os.getpid(), age=0)
        with self.assertRaises(gl.LockTimeout):
            gl.acquire_lock(self.lock, timeout=0.4, stale=30)
        self.assertTrue(self.lock.exists())

    def test_4_dead_owner_is_reclaimed(self):
        child = subprocess.Popen([sys.executable, "-c", "pass"])
        child.wait()
        self.write_lock(child.pid, age=120)
        gl.acquire_lock(self.lock, timeout=5, stale=0.1)
        self.assertEqual(json.loads(self.lock.read_text(encoding="utf-8"))["pid"], os.getpid())

    def test_ownerless_old_lock_is_reclaimed(self):
        self.lock.write_text("", encoding="utf-8")
        past = time.time() - 120
        os.utime(self.lock, (past, past))
        gl.acquire_lock(self.lock, timeout=5, stale=0.1)
        self.assertTrue(self.lock.exists())

    # ---- G1: invalid owner pids are never probed -------------------------
    def test_g1_invalid_pid_is_never_probed_and_expired_lock_is_reclaimed(self):
        for bad in (0, -1, -4242):
            self.write_lock(bad, age=120)
            with mock.patch.object(gl, "_pid_state") as probe:
                gl.acquire_lock(self.lock, timeout=5, stale=0.1)
            probe.assert_not_called()
            self.assertEqual(json.loads(self.lock.read_text(encoding="utf-8"))["pid"], os.getpid())
            gl.release_lock(self.lock)

    def test_g1_invalid_pid_unexpired_lock_is_not_reclaimed(self):
        self.write_lock(0, age=0)
        with mock.patch.object(gl, "_pid_state") as probe:
            with self.assertRaises(gl.LockTimeout):
                gl.acquire_lock(self.lock, timeout=0.4, stale=30)
        probe.assert_not_called()
        self.assertTrue(self.lock.exists())

    def test_g1_pid_state_rejects_non_positive_without_any_api_call(self):
        with mock.patch.object(gl, "_kernel32") as k32, mock.patch.object(gl.os, "kill") as kill:
            for bad in (0, -1):
                self.assertIsNone(gl._pid_state(bad)[0])
        k32.assert_not_called()
        kill.assert_not_called()

    # ---- G2: OpenProcess NULL handle mapping ------------------------------
    @unittest.skipUnless(os.name == "nt", "Win32 only")
    def test_g2_openprocess_null_error_mapping_and_no_close_handle(self):
        import ctypes

        class Fake:
            def __init__(self, null):
                self.null, self.closed, self.queried = null, [], []

            def OpenProcess(self, access, inherit, pid):
                return self.null

            def GetExitCodeProcess(self, handle, ref):
                self.queried.append(handle)
                return 1

            def CloseHandle(self, handle):
                self.closed.append(handle)
                return 1

        for null in (0, None):
            for err, expected in ((87, False), (5, None), (1314, None)):
                fake = Fake(null)
                with mock.patch.object(gl, "_kernel32", return_value=fake), \
                        mock.patch.object(ctypes, "get_last_error", return_value=err):
                    state, detail = gl._win_pid_state(4321)
                self.assertIs(state, expected, (null, err))
                self.assertEqual((fake.closed, fake.queried), ([], []), (null, err))
                self.assertIn(f"last_error={err}", detail)

    # ---- E1: Win32 liveness API contract -------------------------------
    def test_e1_live_and_dead_pid_states(self):
        alive, _ = gl._pid_state(os.getpid())
        self.assertIs(alive, True)
        child = subprocess.Popen([sys.executable, "-c", "pass"])
        child.wait()
        dead, _ = gl._pid_state(child.pid)
        self.assertIs(dead, False)

    @unittest.skipUnless(os.name == "nt", "Win32 only")
    def test_e1_ctypes_signatures_declared(self):
        k32 = gl._kernel32()
        for name in ("OpenProcess", "GetExitCodeProcess", "CloseHandle"):
            fn = getattr(k32, name)
            self.assertIsNotNone(fn.argtypes, name)
            self.assertIsNotNone(fn.restype, name)

    @unittest.skipUnless(os.name == "nt", "Win32 only")
    def test_e1_exit_code_decision_and_handle_closed(self):
        import ctypes

        class Fake:
            def __init__(self, ok, code):
                self.ok, self.code, self.closed, self.opened = ok, code, [], []

            def OpenProcess(self, access, inherit, pid):
                self.opened.append(pid)
                return 1234

            def GetExitCodeProcess(self, handle, ref):
                if self.ok:
                    ref._obj.value = self.code
                return 1 if self.ok else 0

            def CloseHandle(self, handle):
                self.closed.append(handle)
                return 1

        for ok, code, expected in ((True, 259, True), (True, 0, False), (True, 3, False), (False, 259, None)):
            fake = Fake(ok, code)
            with mock.patch.object(gl, "_kernel32", return_value=fake), \
                    mock.patch.object(ctypes, "get_last_error", return_value=6):
                state, detail = gl._win_pid_state(4321)
            self.assertIs(state, expected, (ok, code))
            self.assertEqual(fake.closed, [1234])  # handle closed on every path
            if ok is False:
                self.assertIn("last_error=6", detail)

    @unittest.skipUnless(os.name == "nt", "Win32 only")
    def test_e1_zero_return_is_undecidable_and_not_reclaimed(self):
        self.write_lock(999999, age=120)
        raw = self.lock.read_bytes()
        with mock.patch.object(gl, "_win_pid_state", return_value=(None, "GetExitCodeProcess returned 0 (last_error=5)")):
            with self.assertRaises(gl.LockTimeout) as cm:
                gl.acquire_lock(self.lock, timeout=0.4, stale=0.1)
        self.assertEqual(self.lock.read_bytes(), raw)
        self.assertIn("last_error=5", str(cm.exception))


class Ppss(RepoCase):
    def artifact(self, rid="rsch-1", urls=("https://docs.example.org/a",), name="art.json"):
        path = self.tmp / name
        path.write_text(json.dumps({"request_id": rid, "captured_at": "t", "queries": ["q"],
                                    "results": [{"title": "t", "url": u, "snippet": "s", "date": None,
                                                 "last_updated": None} for u in urls]}), encoding="utf-8")
        return path

    def source(self, text="The helper must keep the original error.", name="src.txt"):
        path = self.tmp / name
        path.write_text(text, encoding="utf-8")
        return path

    def record(self, inc, art, **kw):
        args = ["ppss", "record", "--incident", inc, "--artifact", str(art)]
        for k, v in kw.items():
            args += [f"--{k.replace('_', '-')}", v]
        return self.run_cli(*args)

    def read(self, inc, rid, url, src, supports, quote=None):
        args = ["ppss", "read", "--incident", inc, "--request-id", rid, "--url", url,
                "--source-file", str(src), "--supports", supports]
        if quote:
            args += ["--quote", quote]
        return self.run_cli(*args)

    def test_window_and_states(self):
        inc = self.open_incident()["incident"]
        art = self.artifact()
        res = self.record(inc, art, domains="example.org")
        out = json.loads(res.out)
        self.assertEqual((out["state"], out["evidence_ok"]), ("RETRIEVED", False))
        self.assertEqual(self.record(inc, art).returncode, 5)
        self.assertEqual(self.record(inc, art, new_query_strategy="other domain").returncode, 5)
        self.assertEqual(self.record(inc, self.artifact("rsch-2"), new_query_strategy="other domain",
                                     reason="first query off-topic").returncode, 0)
        self.assertEqual(self.status()["incidents"][0]["rounds_started"], 0)

    def test_evidence_matrix(self):
        inc = self.open_incident()["incident"]
        art = self.artifact()
        self.record(inc, art)
        url = "https://docs.example.org/a"
        states = {p["request_id"]: p for p in self.status()["ppss"]}
        self.assertFalse(states["rsch-1"]["evidence_ok"])
        self.assertEqual(states["rsch-1"]["state"], "RETRIEVED")
        src = self.source()
        off = json.loads(self.read(inc, "rsch-1", url, src, "none").out)
        self.assertEqual((off["state"], off["evidence_ok"]), ("SOURCE_READ", False))  # read but off-topic
        self.assertEqual(self.read(inc, "rsch-1", url, src, "claim-1").returncode, 5)  # no quote
        self.assertEqual(self.read(inc, "rsch-1", url, src, "claim-1", "not in source").returncode, 5)
        self.assertEqual(self.read(inc, "rsch-1", "https://elsewhere.example/x", src, "claim-1", "helper").returncode, 5)
        good = json.loads(self.read(inc, "rsch-1", url, src, "claim-1", "keep the original error").out)
        self.assertTrue(good["evidence_ok"])
        rec = {p["request_id"]: p for p in self.status()["ppss"]}["rsch-1"]
        self.assertTrue(rec["evidence_ok"])
        self.assertEqual(len(rec["reads"]), 2)
        self.assertEqual(rec["reads"][0]["sha256"], rec["reads"][1]["sha256"])

    def test_invalid_cases(self):
        inc = self.open_incident()["incident"]
        empty = self.tmp / "empty.json"
        empty.write_text(json.dumps({"request_id": "rsch-e", "results": []}), encoding="utf-8")
        for art, kw in ((empty, {}), (self.tmp / "missing.json", {}), (self.artifact("rsch-d"), {"domains": "other.org"})):
            out = json.loads(self.record(inc, art, new_query_strategy="n", reason="r", **kw).out)
            self.assertEqual(out["state"], "INVALID", art)
        # an INVALID request cannot be read
        self.assertEqual(self.read(inc, "rsch-d", "https://docs.example.org/a", self.source(), "none").returncode, 5)

    def test_off_topic_negative_samples(self):
        inc = self.open_incident()["incident"]
        if REAL_ARTIFACT.is_file():
            self.assertEqual(self.record(inc, REAL_ARTIFACT).returncode, 0)
            rid = json.loads(REAL_ARTIFACT.read_text(encoding="utf-8"))["request_id"]
            rec = {p["request_id"]: p for p in self.status()["ppss"]}[rid]
            self.assertEqual((rec["state"], rec["evidence_ok"]), ("RETRIEVED", False))
        art = self.artifact("rsch-neg", ("https://code.example.org/hooks",), "neg.json")
        self.record(inc, art, new_query_strategy="n", reason="r") if REAL_ARTIFACT.is_file() else self.record(inc, art)
        self.read(inc, "rsch-neg", "https://code.example.org/hooks", self.source("unrelated hooks page"), "none")
        rec = {p["request_id"]: p for p in self.status()["ppss"]}["rsch-neg"]
        self.assertEqual((rec["state"], rec["evidence_ok"]), ("SOURCE_READ", False))


class StaticChecks(unittest.TestCase):
    def test_stdlib_only_and_no_forbidden_names(self):
        tree = ast.parse(SCRIPT.read_text(encoding="utf-8"))
        stdlib = set(sys.stdlib_module_names)
        banned = ("registry", "urllib", "requests", "http", "socket")
        for node in ast.walk(tree):
            if isinstance(node, ast.Import):
                mods = [a.name for a in node.names]
            elif isinstance(node, ast.ImportFrom):
                mods = [node.module or ""]
            else:
                mods = []
            for m in mods:
                self.assertIn(m.split(".")[0], stdlib, m)
                self.assertFalse(any(b in m.lower() for b in banned), m)
            names = []
            if isinstance(node, ast.Name):
                names = [node.id]
            elif isinstance(node, ast.Attribute):
                names = [node.attr]
            elif isinstance(node, (ast.FunctionDef, ast.ClassDef)):
                names = [node.name]
            for n in names:
                self.assertFalse(any(b in n.lower() for b in banned), n)
            if isinstance(node, ast.Constant) and isinstance(node.value, str):
                self.assertNotIn("TICKET_REGISTRY", node.value)

    def test_usage_lists_every_subcommand_and_exit_code(self):
        doc = ast.get_docstring(ast.parse(SCRIPT.read_text(encoding="utf-8")))
        for word in ("status", "incident", "round", "step-limit", "consult", "strategy-scan", "ppss",
                     "human", "blocks", "missed-check", "check"):
            self.assertIn(word, doc)
        for code in "0123456":
            self.assertIn(code, doc)


if __name__ == "__main__":
    unittest.main()
