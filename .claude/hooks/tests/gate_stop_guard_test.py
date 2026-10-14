#!/usr/bin/env python3
"""Deterministic tests for gate_stop_guard.py (ADV-GATE-04).

Each case builds a throw-away git repo (tempfile, never this repo), copies the REAL
gate_ledger.py into it, drives incidents/receipts through the real public CLI, and runs
the hook as a subprocess (stdin payload -> stdout). No helper behaviour is mocked.
Run: python -I .claude/hooks/tests/gate_stop_guard_test.py
"""
from __future__ import annotations

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
from datetime import datetime, timedelta, timezone
from pathlib import Path

HERE = Path(__file__).resolve().parent
REPO = HERE.parents[2]
HOOK = REPO / ".claude" / "hooks" / "gate_stop_guard.py"
LEDGER = REPO / ".claude" / "scripts" / "gate_ledger.py"
TICKET = "TST-GATE-01"
OK_TICKET = "## Action Items\n\n### AI-1: x\n- [x] 完成\n\n## Acceptance Criteria\n- [x] a\n"


def iso(ts: float) -> str:
    return datetime.fromtimestamp(ts, timezone.utc).isoformat().replace("+00:00", "Z")


class Env:
    def __init__(self, lifecycle="IN_PROGRESS", location="Pending", ticket_text=OK_TICKET):
        self.dir = Path(tempfile.mkdtemp(prefix="gsg_"))
        self.root = self.dir
        self.git("init", "-q")
        (self.root / ".claude" / "scripts").mkdir(parents=True)
        shutil.copy2(LEDGER, self.root / ".claude" / "scripts" / "gate_ledger.py")
        base = self.root / "plans" / "cso_briefing" / "project"
        for sub in ("Pending", "deliver", "void"):
            (base / sub).mkdir(parents=True, exist_ok=True)
        (base / location / f"{TICKET}_x.md").write_text(ticket_text, encoding="utf-8")
        self.set_lifecycle(lifecycle)
        (self.root / "tmp" / "verify").mkdir(parents=True)
        (self.root / "tmp" / "verify" / "active.json").write_text(json.dumps({"ticket_id": TICKET}), encoding="utf-8")
        (self.root / "sub").mkdir()
        self.n = 0

    def close(self):
        for path in self.dir.rglob("*"):
            try:
                os.chmod(path, stat.S_IWRITE | stat.S_IREAD)
            except OSError:
                pass
        shutil.rmtree(self.dir, ignore_errors=True)

    def git(self, *args):
        return subprocess.run(["git", "-C", str(self.root), *args], capture_output=True, text=True)

    def set_lifecycle(self, lifecycle):
        reg = self.root / "plans" / "cso_briefing" / "project" / "TICKET_REGISTRY.json"
        reg.write_text(json.dumps({"tickets": [{"id": TICKET, "lifecycle": lifecycle}]}), encoding="utf-8")

    def ledger(self, *args, ok=True):
        res = subprocess.run([sys.executable, str(self.root / ".claude" / "scripts" / "gate_ledger.py"), *args,
                              "--ticket", TICKET], cwd=str(self.root), capture_output=True, text=True, encoding="utf-8")
        if ok:
            assert res.returncode == 0, (args, res.returncode, res.stderr, res.stdout)
        return res

    def status(self):
        return json.loads(self.ledger("status", "--json").stdout)

    def incident(self, subject, extra=()):
        out = json.loads(self.ledger("incident", "open", "--subject", subject, *extra).stdout)
        return out["incident"]

    def fail_round(self, inc, n, strategy, layer=None):
        args = ["round", "start", "--incident", inc, "--expect", str(n), "--strategy", strategy, "--authority", "ticket"]
        if layer:
            args += ["--layer", layer]
        self.ledger(*args)
        self.ledger("round", "end", "--incident", inc, "--result", "fail")

    def consult(self, inc, kind):
        self.ledger("consult", "--incident", inc, "--kind", kind)

    def exhaust_advisor(self, inc, start=0):
        self.consult(inc, "advisor")
        for k in range(3):
            self.fail_round(inc, start + k, f"adv-{inc}-{k}", layer="advisor")

    def write_events(self, events):
        path = self.root / ".git" / "gate" / f"{TICKET}.jsonl"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text("".join(json.dumps(e, ensure_ascii=False) + "\n" for e in events), encoding="utf-8")

    def transcript(self, prompt, text, ts=None):
        path = self.root / "tmp" / f"transcript_{prompt}.jsonl"
        line = {"type": "user", "promptId": prompt, "timestamp": iso(ts if ts is not None else time.time() - 120),
                "message": {"role": "user", "content": text}}
        path.write_text(json.dumps(line, ensure_ascii=False) + "\n", encoding="utf-8")
        return path

    def payload(self, prompt="p1", text="繼續處理", cwd=None, transcript=True, **extra):
        data = {"session_id": "s1", "prompt_id": prompt, "cwd": str(cwd or self.root), "hook_event_name": "Stop",
                "stop_hook_active": False}
        if transcript:
            data["transcript_path"] = str(self.transcript(prompt, text))
        data.update(extra)
        return data

    def hook(self, payload):
        res = subprocess.run([sys.executable, "-I", str(HOOK)], input=json.dumps(payload), capture_output=True,
                             text=True, encoding="utf-8", cwd=str(self.root), timeout=60)
        assert res.returncode == 0, res.stderr
        out = res.stdout.strip()
        return json.loads(out) if out else None

    def marker(self, suffix, session="s1", prompt="p1"):
        return self.root / "tmp" / ".stop_guard" / f"{session}_{prompt}.{suffix}"


class Base(unittest.TestCase):
    def setUp(self):
        self.env = Env()
        self.addCleanup(self.env.close)

    def assertAllow(self, payload, env=None):
        self.assertIsNone((env or self.env).hook(payload))

    def block(self, payload, env=None):
        out = (env or self.env).hook(payload)
        self.assertIsNotNone(out, "expected block")
        self.assertEqual(out["decision"], "block")
        return out["reason"]


class AllowConditions(Base):
    def test_no_active_ticket(self):
        (self.env.root / "tmp" / "verify" / "active.json").unlink()
        self.env.incident("s-a")
        self.assertAllow(self.env.payload())

    def test_terminal_and_blocked_lifecycles_allow(self):
        self.env.incident("s-a")
        for lifecycle in ("REVIEW", "CLOSED", "VOID", "BLOCKED"):
            self.env.set_lifecycle(lifecycle)
            self.assertAllow(self.env.payload())

    def test_registry_missing_ticket_allows(self):
        self.env.incident("s-a")
        (self.env.root / "plans/cso_briefing/project/TICKET_REGISTRY.json").write_text('{"tickets": []}', encoding="utf-8")
        self.assertAllow(self.env.payload())

    def test_location_not_pending_allows(self):
        self.env.incident("s-a")
        base = self.env.root / "plans/cso_briefing/project"
        shutil.move(str(base / "Pending" / f"{TICKET}_x.md"), str(base / "deliver" / f"{TICKET}_x.md"))
        self.assertAllow(self.env.payload())

    def test_missing_payload_fields_allow(self):
        self.env.incident("s-a")
        for key in ("prompt_id", "session_id", "cwd"):
            data = self.env.payload()
            data.pop(key)
            self.assertAllow(data)

    def test_cancel_allows_without_ledger_event(self):
        self.env.incident("s-a")
        before = self.env.status()["last_seq"]
        for text in ("停止", "取消", "Stop", "請先停吧", "算了。"):
            self.assertAllow(self.env.payload(text=text))
        self.assertEqual(self.env.status()["last_seq"], before)

    def test_negated_or_quoted_stop_is_not_cancel(self):
        self.env.incident("s-a")
        for n, text in enumerate(("不要 stop", "請把「stop」改成 abort 並繼續", "stop the build and fix the failing test")):
            self.block(self.env.payload(prompt=f"q{n}", text=text))

    def test_discussion_allows(self):
        self.env.incident("s-a")
        self.assertAllow(self.env.payload(text="這個 hook 為什麼要 fail-open？"))
        self.assertAllow(self.env.payload(prompt="p2", text="請說明 K 值的選擇"))

    def test_explicit_tool_restriction_allows_and_keeps_incident(self):
        inc = self.env.incident("s-a")
        before = self.env.status()
        for n, text in enumerate(("只回覆 OK 兩個字，不要使用任何工具。", "請勿使用工具，直接回答", "不用工具",
                                  "Do not use any tools, just answer.", "please don't use tools", "answer with no tools")):
            self.assertAllow(self.env.payload(prompt=f"t{n}", text=text))
        after = self.env.status()
        self.assertEqual(after["last_seq"], before["last_seq"])            # no ledger event written
        self.assertEqual([i["status"] for i in after["incidents"]], ["open"])   # incident kept
        self.assertEqual(after["incidents"][0]["id"], inc)

    def test_tool_restriction_does_not_override_other_prompts(self):
        self.env.incident("s-a")
        for n, text in enumerate(("請使用工具修復並跑測試", "工具鏈壞了，請修", "我不是說不要修，請繼續",
                                  "use the tools to fix it", "tools are fine, keep going")):
            self.block(self.env.payload(prompt=f"u{n}", text=text))

    def test_tool_restriction_does_not_touch_blocked_or_permission_state(self):
        self.env.incident("s-a")
        self.env.ledger("human", "--category", "PERMISSION", "--basis", "denied write")
        self.assertAllow(self.env.payload(text="不要使用任何工具"))
        self.assertEqual([p["category"] for p in self.env.status()["pending_human"]], ["PERMISSION"])

    def test_unknown_user_turn_is_neither_cancel_nor_discussion(self):
        self.env.incident("s-a")
        self.block(self.env.payload(transcript=False))

    def test_garbage_stdin_allows(self):
        res = subprocess.run([sys.executable, "-I", str(HOOK)], input="not json", capture_output=True, text=True)
        self.assertEqual((res.returncode, res.stdout.strip()), (0, ""))

    def test_internal_deadline_allows(self):
        spec = importlib.util.spec_from_file_location("gsg_mod", HOOK)
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        mod.DEADLINE_SECONDS = 0.01
        self.env.incident("s-a")
        buf, old_in, old_out = io.StringIO(), sys.stdin, sys.stdout
        sys.stdin = type("S", (), {"buffer": io.BytesIO(json.dumps(self.env.payload()).encode("utf-8"))})()
        sys.stdout = buf
        try:
            rc = mod.main()
        finally:
            sys.stdin, sys.stdout = old_in, old_out
        self.assertEqual((rc, buf.getvalue()), (0, ""))

    def test_subdirectory_cwd_still_locates_root(self):
        self.env.incident("s-a")
        reason = self.block(self.env.payload(cwd=self.env.root / "sub"))
        self.assertIn("INC-001", reason)

    def test_source_has_no_notification_or_internal_import(self):
        text = HOOK.read_text(encoding="utf-8").lower()
        for banned in ("notify", "telegram", "import gate_ledger", "from gate_ledger"):
            self.assertNotIn(banned, text)


class OpenIncidents(Base):
    def test_open_incident_below_three_rounds_blocks_with_router(self):
        inc = self.env.incident("s-a")
        reason = self.block(self.env.payload())
        for token in ("R1", "R3", "R4", "R6", "consult --kind advisor", inc, "Advisor 已用 0", "PPSS 已用 0",
                      "executor 0/3（失敗 0）", "permissions"):
            self.assertIn(token, reason)
        for banned in ("PPSS 已用 0／1", "初判", "reconcile", "僅允許升報"):
            self.assertNotIn(banned, reason)
        self.assertEqual(self.env.status()["blocks"], 1)

    def test_three_failed_rounds_only_escalation_and_other_incident_not_misblocked(self):
        i1 = self.env.incident("s-a")
        for n, strat in enumerate(("a", "b", "c")):
            self.env.fail_round(i1, n, strat)
        i2 = self.env.incident("s-b", ("--independent-evidence", "separate failing output"))
        reason = self.block(self.env.payload())
        self.assertNotIn("僅允許升報", reason)             # executor exhaustion alone is not an escalation
        self.assertIn("executor 層已耗盡", reason)
        self.assertIn("consult --kind advisor", reason)
        self.assertIn(i2, reason)
        self.env.exhaust_advisor(i1, start=3)
        reason = self.block(self.env.payload(prompt="p2"))
        self.assertIn("僅允許升報", reason)
        self.assertIn("EXHAUSTED", reason)
        self.assertIn("layer_advisor_3", reason)
        self.assertIn(i2, reason)
        router_part = reason.split("僅允許升報")[1]
        self.assertIn("以下 incident 仍有未結義務", router_part)

    def test_exhausted_pending_local_allows_when_nothing_else_owed(self):
        i1 = self.env.incident("s-a")
        for n, strat in enumerate(("a", "b", "c")):
            self.env.fail_round(i1, n, strat)
        self.env.exhaust_advisor(i1, start=3)
        self.env.ledger("human", "--category", "EXHAUSTED", "--incident", i1, "--basis", "layer_advisor_3")
        self.assertAllow(self.env.payload())

    def test_local_pending_does_not_excuse_other_open_incident(self):
        i1 = self.env.incident("s-a")
        for n, strat in enumerate(("a", "b", "c")):
            self.env.fail_round(i1, n, strat)
        self.env.exhaust_advisor(i1, start=3)
        self.env.ledger("human", "--category", "EXHAUSTED", "--incident", i1, "--basis", "layer_advisor_3")
        i2 = self.env.incident("s-b", ("--independent-evidence", "separate failing output"))
        reason = self.block(self.env.payload())
        self.assertIn(i2, reason)
        self.assertNotIn("僅允許升報", reason)

    def test_whole_ticket_pending_allows_despite_open_incident(self):
        self.env.incident("s-a")
        self.env.ledger("human", "--category", "PERMISSION", "--basis", "denied write to settings")
        self.assertAllow(self.env.payload())

    def test_incident_scoped_permission_is_local_not_whole_ticket(self):
        i1 = self.env.incident("s-a")
        self.env.ledger("human", "--category", "PERMISSION", "--incident", i1, "--basis", "denied")
        i2 = self.env.incident("s-b", ("--independent-evidence", "separate failing output"))
        reason = self.block(self.env.payload())
        self.assertIn(i2, reason)


class LayeredRouting(Base):
    """ADV-LAYER-01: routing by layer state (bounded probe, stub level; real helper, real hook subprocess)."""

    def test_advisor_layer_below_three_failures_is_router_not_escalation(self):
        inc = self.env.incident("s-a")
        self.env.consult(inc, "advisor")
        for k in range(2):
            self.env.fail_round(inc, k, f"adv {k}", layer="advisor")
        reason = self.block(self.env.payload())
        self.assertNotIn("僅允許升報", reason)
        self.assertIn("advisor 2/3（失敗 2）", reason)

    def test_early_escalation_with_advisor_round_in_progress_is_router_and_shows_layer_state(self):
        inc = self.env.incident("s-a")
        self.env.fail_round(inc, 0, "ex one")
        self.env.consult(inc, "advisor")
        self.env.ledger("round", "start", "--incident", inc, "--expect", "1", "--strategy", "adv one",
                        "--authority", "ticket", "--layer", "advisor")
        reason = self.block(self.env.payload())
        self.assertNotIn("僅允許升報", reason)
        self.assertNotIn("executor 層已耗盡", reason)
        self.assertIn("advisor 1/3（失敗 0）", reason)       # production path: CLI -> ledger -> status -> hook reason
        self.assertIn("executor 1/3（失敗 1）", reason)

    def test_legacy_events_without_layer_count_as_executor_and_route_to_advisor(self):
        events = [{"seq": 1, "ts": "2026-10-14T00:00:00+00:00", "ticket": TICKET, "type": "finding",
                   "incident": "INC-001", "new": True, "subjects": ["s-a"], "gate_point": None, "verdict": None,
                   "same_root_cause": None, "independent_evidence": None, "attach_reason": None}]
        seq = 2
        for k in range(3):
            events.append({"seq": seq, "ts": "2026-10-14T00:00:00+00:00", "ticket": TICKET, "type": "round_start",
                           "incident": "INC-001", "round": k + 1, "strategy": f"old {k}", "strategy_norm": f"old {k}",
                           "authority": "ticket"})
            events.append({"seq": seq + 1, "ts": "2026-10-14T00:00:00+00:00", "ticket": TICKET, "type": "round_end",
                           "incident": "INC-001", "round": k + 1, "result": "fail"})
            seq += 2
        self.assertFalse(any("layer" in e for e in events))
        self.env.write_events(events)
        reason = self.block(self.env.payload())
        self.assertNotIn("僅允許升報", reason)
        self.assertIn("executor 3/3（失敗 3）", reason)
        self.assertIn("executor 層已耗盡", reason)

    def test_r0_exhausted_points_to_advisor_without_rerunning_r0_or_escalating(self):
        inc = self.env.incident("s-a")
        self.env.consult(inc, "r0")
        for k in range(3):
            self.env.fail_round(inc, k, f"r0 {k}", layer="r0")
        reason = self.block(self.env.payload())
        self.assertIn("r0 層已耗盡", reason)
        self.assertIn("consult --kind advisor", reason)
        self.assertNotIn("僅允許升報", reason)
        self.assertNotIn("--layer r0", reason)               # never points back at the spent R0 layer
        self.assertNotIn("EXHAUSTED", reason)
        self.env.consult(inc, "advisor")
        self.env.fail_round(inc, 3, "adv one", layer="advisor")
        reason = self.block(self.env.payload(prompt="p2"))
        self.assertNotIn("僅允許升報", reason)
        self.assertNotIn("r0 層已耗盡", reason)              # Advisor engaged: no more pointing
        self.assertIn("advisor 1/3（失敗 1）", reason)

    def test_r0_and_executor_both_exhausted_uses_r0_branch(self):
        inc = self.env.incident("s-a")
        for k in range(3):
            self.env.fail_round(inc, k, f"ex {k}")
        self.env.consult(inc, "r0")
        for k in range(3):
            self.env.fail_round(inc, 3 + k, f"r0 {k}", layer="r0")
        reason = self.block(self.env.payload())
        self.assertIn("r0 層已耗盡", reason)
        self.assertNotIn("executor 層已耗盡", reason)
        self.assertNotIn("僅允許升報", reason)

    def test_executor_pass_closes_incident_and_stop_is_allowed(self):
        inc = self.env.incident("s-a")
        self.env.ledger("round", "start", "--incident", inc, "--expect", "0", "--strategy", "fix it",
                        "--authority", "ticket")
        self.env.ledger("round", "end", "--incident", inc, "--result", "pass")
        self.assertAllow(self.env.payload())


class SingleCheck(Base):
    def test_unchecked_item_blocks_once_per_prompt(self):
        env = Env(ticket_text="## Action Items\n\n### AI-1: x\n- [ ] 完成\n\n## Acceptance Criteria\n- [ ] a 條件\n")
        self.addCleanup(env.close)
        reason = self.block(env.payload(prompt="p1"), env)
        self.assertIn("只出現一次", reason)
        self.assertIn("AI-1", reason)
        self.assertTrue(env.marker("check").is_file())
        self.assertIsNone(env.hook(env.payload(prompt="p1")))
        self.block(env.payload(prompt="p2"), env)
        self.assertEqual(env.status()["blocks"], 0)       # single check never writes a ledger event

    def test_human_marked_unchecked_lines_are_not_clues(self):
        env = Env(ticket_text="## Acceptance Criteria\n- [ ] **[實機]** 真機 Logcat\n- [ ] DEFERRED-TO-DOCSYNC 搬移\n- [ ] 等 Akira 驗收\n")
        self.addCleanup(env.close)
        self.assertAllow(env.payload(), env)

    def test_qa_receipt_without_sv_is_a_check_not_a_command(self):
        self.env.ledger("check", "qa")
        reason = self.block(self.env.payload())
        self.assertIn("核對 `/qa` 最終結果", reason)
        self.assertIn("QA PASS 且本票要求 SV 則續接", reason)
        self.assertNotIn("請進入 /sv", reason)

    def test_sv_receipt_after_qa_clears_clue(self):
        self.env.ledger("check", "qa")
        self.env.ledger("check", "sv")
        self.assertAllow(self.env.payload())

    def test_clue_under_local_pending_is_single_check_not_ignored(self):
        env = Env(ticket_text="## Action Items\n\n### AI-1: x\n- [ ] 完成\n")
        self.addCleanup(env.close)
        i1 = env.incident("s-a")
        env.ledger("human", "--category", "PERMISSION", "--incident", i1, "--basis", "denied")
        reason = self.block(env.payload(), env)
        self.assertIn("已升報待人工", reason)


class MissedCheck(Base):
    def _evidence(self):
        ev = self.env.root / "tmp" / TICKET / "evidence"
        ev.mkdir(parents=True, exist_ok=True)
        (ev / "step0_abc123.txt").write_text("step0", encoding="utf-8")

    def test_evidence_without_earlier_receipt_blocks_and_writes_missed_check_once(self):
        self._evidence()
        reason = self.block(self.env.payload())
        self.assertIn("qa", reason)
        self.assertEqual(self.env.status()["missed_checks"], 1)
        self.block(self.env.payload(prompt="p1"))
        self.assertEqual(self.env.status()["missed_checks"], 1)          # one write per prompt x gate point

    def test_late_receipt_resolves_but_missed_check_remains(self):
        self._evidence()
        self.block(self.env.payload())
        time.sleep(1.1)
        self.env.ledger("check", "qa", "--late")
        self.env.ledger("check", "sv")                                      # keep the QA->SV clue out of the way
        self.assertAllow(self.env.payload())
        self.assertEqual(self.env.status()["missed_checks"], 1)

    def test_earlier_receipt_means_no_miss(self):
        self.env.ledger("check", "qa")
        self.env.ledger("check", "sv")
        time.sleep(1.1)
        self._evidence()
        self.assertAllow(self.env.payload())
        self.assertEqual(self.env.status()["missed_checks"], 0)

    def test_unknown_turn_start_never_writes_missed_check(self):
        self._evidence()
        self.assertAllow(self.env.payload(transcript=False))
        self.assertEqual(self.env.status()["missed_checks"], 0)

    def test_saturated_receipt_window_is_unknown(self):
        for _ in range(5):
            self.env.ledger("check", "done")
        self._evidence()
        self.assertAllow(self.env.payload())
        self.assertEqual(self.env.status()["missed_checks"], 0)

    def test_ticket_prefixed_commit_is_commit_evidence(self):
        self.env.ledger("check", "qa")
        self.env.ledger("check", "sv")
        self.env.git("-c", "user.name=t", "-c", "user.email=t@t", "commit", "-q", "--allow-empty", "-m", f"[{TICKET}] work")
        reason = self.block(self.env.payload())
        self.assertIn("commit", reason)
        self.assertEqual(self.env.status()["missed_checks"], 1)


class ProgressAndFailures(Base):
    def _seq(self):
        return json.loads(self.env.marker("seq").read_text(encoding="utf-8"))

    def test_sequence_counts_unchanged_blocks_and_ignores_noise(self):
        inc = self.env.incident("s-a")
        for expected in (1, 2):
            self.block(self.env.payload())
            self.assertEqual(self._seq()["count"], expected)
        self.env.ledger("check", "done")                                    # repeated check: not progress
        self.env.ledger("incident", "open", "--subject", "s-a", "--same-root-cause", inc)   # repeated finding
        self.env.ledger("consult", "--incident", inc, "--kind", "local", "--note", "x")
        self.env.ledger("incident", "open", "--subject", "s-z", "--independent-evidence", "another")   # more incidents
        self.block(self.env.payload())
        self.assertEqual(self._seq()["count"], 3)

    def test_round_end_is_progress_and_resets_sequence(self):
        inc = self.env.incident("s-a")
        self.block(self.env.payload())
        self.block(self.env.payload())
        self.env.fail_round(inc, 0, "first")
        self.block(self.env.payload())
        self.assertEqual(self._seq()["count"], 0)

    def test_new_prompt_starts_new_sequence(self):
        self.env.incident("s-a")
        self.block(self.env.payload(prompt="p1"))
        self.block(self.env.payload(prompt="p1"))
        self.block(self.env.payload(prompt="p2"))
        seq2 = json.loads(self.env.marker("seq", prompt="p2").read_text(encoding="utf-8"))
        self.assertEqual(seq2["count"], 1)

    def test_k_without_progress_writes_no_progress_and_allows(self):
        self.env.incident("s-a")
        for _ in range(5):
            self.block(self.env.payload())
        self.assertAllow(self.env.payload())                                # 6th evaluation escalates
        st = self.env.status()
        self.assertEqual([p["category"] for p in st["pending_human"]], ["NO_PROGRESS"])
        self.assertAllow(self.env.payload(prompt="p9"))                     # whole-ticket pending stays trusted

    def test_progress_vector_components(self):
        spec = importlib.util.spec_from_file_location("gsg_mod2", HOOK)
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        st = {"incidents": [{"status": "closed", "rounds_started": 1, "round_open": False},
                            {"status": "open", "rounds_started": 2, "round_open": True}],
              "ppss": [{"reads": [{"sha256": "a"}, {"sha256": "a"}]}, {"reads": [{"sha256": "b"}]}]}
        self.assertEqual(mod.progress_vector(st, 3), [1, 2, 3, 2])
        st["ppss"].append({"reads": [{"sha256": "b"}]})                     # same data again: no change
        self.assertEqual(mod.progress_vector(st, 3), [1, 2, 3, 2])

    def test_write_exit_6_blocks_once_then_allows_without_retry(self):
        self.env.incident("s-a")
        ledger_file = next((self.env.root / ".git" / "gate").glob("*.jsonl"))
        os.chmod(ledger_file, stat.S_IREAD)
        reason = self.block(self.env.payload())
        self.assertIn("ledger 讀寫失敗", reason)
        self.assertIn("exit", reason)
        self.assertAllow(self.env.payload())
        self.assertTrue(self.env.marker("ledgerfail").is_file())
        os.chmod(ledger_file, stat.S_IWRITE | stat.S_IREAD)
        self.assertEqual(self.env.status()["blocks"], 0)                    # nothing fabricated

    def test_write_exit_5_blocks_once_then_allows_without_retry(self):
        self.env.incident("s-a")
        self.env.marker("seq").parent.mkdir(parents=True, exist_ok=True)
        self.env.marker("seq").write_text(json.dumps({"count": 5, "hw": [0, 0, 2, 0]}), encoding="utf-8")   # 2 = boxes already ticked in OK_TICKET
        reason = self.block(self.env.payload())       # `human NO_PROGRESS` refused: no blocks event yet -> exit 5
        self.assertIn("exit 5", reason)
        self.assertAllow(self.env.payload())
        self.assertEqual(self.env.status()["pending_human"], [])

    def test_status_failure_blocks_once_then_allows(self):
        self.env.incident("s-a")
        ledger_file = next((self.env.root / ".git" / "gate").glob("*.jsonl"))
        ledger_file.write_text("{broken json\n{}\n", encoding="utf-8")   # corrupt line that is not the tail -> status exit 6
        reason = self.block(self.env.payload())
        self.assertIn("ledger 讀寫失敗", reason)
        self.assertAllow(self.env.payload())

    def test_marker_unwritable_allows(self):
        self.env.incident("s-a")
        ledger_file = next((self.env.root / ".git" / "gate").glob("*.jsonl"))
        os.chmod(ledger_file, stat.S_IREAD)
        (self.env.root / "tmp" / ".stop_guard").write_text("a file, not a directory", encoding="utf-8")
        self.assertAllow(self.env.payload())


if __name__ == "__main__":
    unittest.main(verbosity=2)
