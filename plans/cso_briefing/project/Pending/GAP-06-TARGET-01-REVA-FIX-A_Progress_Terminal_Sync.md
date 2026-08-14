# GAP-06-TARGET-01-REVA-FIX-A: PROGRESS 終態同步（strict_verification Path A 關票前置修正）

**檔案治理/來源說明**：本票依 Traycer strict_verification 報告（`plans/cso_briefing/reports/Traycer_StrictVerification_GAP-06-TARGET-01-REVA_20260718.md`）之 FIX 指令簽發：PROGRESS.md GAP-06 兩處狀態落後實況（仍載「待 R0/Codex 快審 → Traycer T2 回審」），依關票紀律 MUST 先同步方可落定終態。CSO 歸檔：`2026-07-18_GAP06_RevA_Final_Acceptance_Closure.md`。

**簽發日期**: 2026-07-18
**簽發者**: Claude（CSO）
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（ROI v2.0 X2 純治理同步票，禁投 Traycer）
**前置依賴**: GAP-06-TARGET-01-REVA（strict_verification Path A 已判）

## Verify Config
TICKET_ID=GAP-06-TARGET-01-REVA-FIX-A
CREATED=2026-07-18
PRESET=hotfix
ALLOWED=.claude/PROGRESS.md
FORBIDDEN=app/**,plans/cso_briefing/project/deliver/GAP-06-TARGET-01_RevA_SingleSource_Geometry_Wiring.md,plans/cso_briefing/reports/**,.claude/CLAUDE.md,.claude/settings.json,tools/**
MAX_LINES=20
DIFF_REF=<動手前最後一刻活錨回填（A6-4 定式）＋read-back 義務>

## Action Items

### AI-1: Active Blockers GAP-06 列狀態更新
- **檔案**: `.claude/PROGRESS.md`
- **現值**: GAP-06 列狀態欄載「待 R0/Codex 快審 → Traycer T2 回審」（或其後續變體）
- **目標值**: 「Traycer strict_verification Path A APPROVE（碼域，錨 bc40d17）· 剩 D19 三項 [實機] AC（A7-1 映射表承接）+ Next Action #9 Akira 證據核驗」
- [ ] 完成

### AI-2: PROVISIONAL Ledger GAP-06-TARGET 條目同步
- **檔案**: `.claude/PROGRESS.md`
- **現值**: GAP-06-TARGET 條目狀態停於回審前
- **目標值**: 「strict_verification Path A（碼域，錨 bc40d17）· D19 [實機]×3 懸掛（A7-1）」
- [ ] 完成

## Hard Constraints
1. 單一 commit、docs-only；依 CR-04 事件確立之慣例為獨立 artifact commit，**不進** R0/cross_review pipeline。
2. PROGRESS.md 總行數 MUST ≤80（現 79）；若更新需增行，同 commit 內先執行既有 Sprint 歸檔輪替（PROGRESS_ARCHIVE.md），不得超限提交。
3. 僅改 GAP-06 相關兩處；其他票列（含 BUG-P08-F4-REAL-DEVICE、BUG-LCSOURCE-HARDCODED-ROTATION 等）零觸碰。
4. 目標字串中之錨 `bc40d17` 為 strict_verification 審查錨，非本 commit HEAD——**不得**「順手更新」為提交當下之新 HEAD。

## Error Handling
- 兩處目標列任一無法唯一定位（字面已被他票異動）→ 停止回報附現況引文
- 更新後行數 >80 且無法安全輪替 → 停止回報

## Acceptance Criteria
- [ ] **[存在性]** read-back：Active Blockers GAP-06 列命中「strict_verification Path A APPROVE（碼域，錨 bc40d17）」
- [ ] **[存在性]** read-back：PROVISIONAL Ledger GAP-06-TARGET 條目命中「D19」與「[實機]×3」
- [ ] **[存在性]** `wc -l .claude/PROGRESS.md` ≤ 80
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
