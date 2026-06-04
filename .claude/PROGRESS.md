# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12 → D10-T1 ✅ · D10-T2 ✅ · HF1~HF4 ✅ · V16-CR2 ✅ · D11-T1 ✅ · D11-T2 ✅ · D11-T2-HF1 ✅ · **D12-T1 ✅ SV+Cross-Review PASS 待 Traycer** · **INFRA-16 ✅** · **P05-R1 ✅ R2 Skip → Traycer 批次** · **INFRA-17 ✅** · **INFRA-18 ✅ SV+Cross-Review PASS 待 Traycer** · **INFRA-19 ✅** · **HOTFIX-30~32 ✅** · **Session V：Ghost Face 渲染路徑根因定位 + KAIROS K3-8/K3-9 登記 + CSO_INDEX/P05 Contract 更新** · **INFRA-RFC2119 ✅ Traycer Skip** · **HOTFIX-38 ✅** · **HOTFIX-dtaudit ✅** · **INFRA-21 ✅ SV+Cross-Review PASS 待 Traycer** · **P08-E1 ✅ Traycer execute_review PASS** · **P08-F1 ✅ Traycer execute_review PASS** · **P08-F2 ✅ Traycer execute_review PASS（四因子 AI-2 contentBounds R2）** · **P08-F3+HF1 🟡 QA PASS 待 /sv**（single writer local lambda + facesFilteredToEmpty bypass）
**Last Updated**: 2026-06-04

---

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | Stage0 W1 ✅ — P0-FPS ✅ — P0-FOCUS ✅ — P05-T1 ✅ — P05-T2 ✅ — P05-T5 ✅ — P05-T6 ✅ — **P05-T3 🟡 code DONE，待 Traycer** — P05-T7 🔧 — **D10-T1 ✅ · D10-T2 ✅ · HF1~HF4 ✅ · V16-CR2 ✅ SV PASS 待 Traycer** — **D11-T1 ✅ SV PASS 待 Traycer** — **D11-T2 ✅ SV PASS 待 Traycer** — **D11-T2-HF1 ✅ SV+Cross-Review PASS 待 Traycer** — **D12-T1 ✅ SV+Cross-Review PASS 待 Traycer** — **P05-R1 ✅ R2 Skip → Traycer 批次** — **INFRA-17 ✅** — **INFRA-18 ✅ SV+Cross-Review PASS 待 Traycer** — **INFRA-19 ✅** — **HOTFIX-30~32 ✅** — **HOTFIX-38 ✅** — **HOTFIX-dtaudit ✅** — **INFRA-RFC2119 ✅ Traycer Skip** — **4 Bugs 待 Ticket（Ghost Face 渲染路徑/GridOverlay/PitchDot5/Pixel9aLayout）** — **P08-E1 ✅ Traycer execute_review PASS** — **P08-F1 ✅ Traycer execute_review PASS** — **P08-F2 ✅ Traycer execute_review PASS（四因子 AI-2 contentBounds R2）** — **P08-F3+HF1 🟡 QA PASS 待 /sv**（observeDetectionResults 移除 + single write lambda + facesFilteredToEmpty bypass） |
| Compile | 0 errors ✅ (assembleDebug 17s) |
| Tests | passed ✅ (testDeviceDebugUnitTest — 181 passed, 0 failed) |
| Lint | 0 errors ✅ / 0 new warnings (baseline unchanged) |

---

## Stage0 Realignment（2026-05-05 啟動）

### Active Tickets

- **TICKET-00** isAllAligned fix — P0 — Est 30m / Actual 35m (+5m) — DONE
- **TICKET-01** AmbientConstants SSOT — P0 — Est 30m / Actual 25m (-5m) — DONE

### Wave Queue

- **W1**: T00 → T01
- **W2**: T02 → T03 → T04 → T06
- **W3**: T08 → T09 → T05
- **W4**: T07 → T10 → T11 → T12A → T13 → T15
- **W5**: T12B → T14

### Session Plan

- S1(W1): T00→T01
- S2(W2): T02→T03→T04
- S3(W2'): T06
- S4(W3): T08→T09
- S5(W3): T05
- S6(W4): T07→T10
- S7(W4): T11→T12A
- S8(W4): T13+T15
- S9(W5): T12B→T14

### Source Documents (Phase 0 現行基準)

- **SSOT v4.0**: `plans/architecture/SSOT_v4_0_5.md` ← 唯一真理文件
- **Layout Spec v2.2**: `plans/architecture/Cueshot_Layout_Spec_v2_3.md` ← 佈局精確定義
- **Phase0 UI Contract**: `plans/cso_briefing/Phase0_UI_Realignment_Contract_v1.md`

---

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| ~~I8~~ | ~~IFTR 歸零 — isAllAligned 永遠 false~~ | ✅ 已修復 | TICKET-00 — 改為加權平均 ≥ 70 |
| ~~I9~~ | ~~Traycer Path B 裁決阻擋 W2 啟動~~ | ✅ 已解除 | FIX-INFRA-02-FINAL + FIX-INFRA-02-D 完成 OpenCV 全鏈路清除，Path B 唯一 blocker 已修補 |
| I7 / BUG-I7-FOCUS-RING-PERSIST | FocusIndicatorOverlay 雙圓環持續顯示（實機確認） | 🔴 P1 | `EyeAfController` 每 2s 觸發新 `stableId` → `LaunchedEffect` 重置 `alphaTarget=1.0`，dwell fade 永遠被打斷。圓環為 `drawSearchingBase` 設計的 3 層同心圓（非兩 Overlay 疊加）。修復：Guard 7（`LensFocusState.Focused + drift < 閾值 → skip`）+ `lastTriggerPoint` instance var。 |
| BHA-01 | T02 Hard Gate — 歸一化 ✅，視覺 🔴 | 🟡 部分通過 | 歸一化數學 PASS（所有場景 normBbox [0,1]）。視覺 FAIL：4 個前置 Bug 阻塞（見下方）。 |
| BUG-MLKIT-DUAL-EMIT | ML Kit 雙管線交替發射 | 🔴 P0 Blocker | 切換鏡頭/方向後舊鏡頭 DetectionResult 未清除，與新鏡頭結果交替輸出 → Halo 忽大忽小 + 閃爍出現/消失。修復方向：切換時 `_state.update { copy(detectionResult = null) }` 或 `flatMapLatest`。 |
| BUG-SONY-ROTATION-CONST | Sony rotation 恆為 0 | 🔴 P0 | Sony XQ-BT52 直立/橫向皆回報 `rotationDegrees=0`，rotation swap 邏輯無法觸發。需裝置相容性處理或改用 CameraX Surface rotation。 |
| BUG-PTF-HALO-SIZE | PTF 圓環渲染過大且不跟隨 | 🔴 P0 | PTF Halo 渲染層 bbox → 圓半徑轉換錯誤。座標數學已正確，純渲染 bug。 |

## Next Actions

> **Session J 完成（2026-05-24）：RC-1~RC-5 全部修復 — Split-Brain(RC-1) / Await Cancel(RC-2) / FadingOut 中間態(RC-3) / FOCUS_FAILED_MS 常數化(RC-4) / Lock Rollback(RC-5)，QA PASS（181 tests, 0 failed）**
> **Session K 完成（2026-05-25）：RC-3-FOLLOWUP（Failed→Idle render fadeout 補齊）+ RC-4-FOLLOWUP（TapFocusRing magic number 常數治理）QA PASS（181 tests, 0 failed）**
> **Session L 完成（2026-05-26）：INFRA-002~005 — .gitignore session_handoff、/qa Step 0 Scope Gate verify_checklist.sh、CSO briefing 重組、CSO_Session_Startup_SOP 永駐層精簡（19K→2K）**
> **Session M 完成（2026-05-27）：純調查 — GazeAF 琥珀圓環根因定位（Ghost Face bbox + gazeScore=0.0 雙層 Bug）、PitchTraction 第5顆光點 Sony nav bar 遮蔽分析、Pixel 9a 預覽雙層覆蓋三因子分析、UITA SOP v2.1 更新。三項 Bug 等待 CSO 裁決選項後開 Ticket**
> **Session N 完成（2026-05-29）：GridOverlay 不可見三層根因定位（alpha=0.08f/strokeWidth=0.5f/Z-order）、Sony logcat 驗證 contentBounds 正常、INFRA-14/15 git untrack cso_briefing+uita_project**
> **Session O 完成（2026-05-29）：D10-T1 Pipeline Boot 基礎建設（CLAUDE.md Boot Sequence + tmp 清除 + Verify Config 規則）、D10-T2 verify_checklist.sh conf 模式（get_conf / [0.5] 新鮮度 / path 比對修正）、D10-T2-HF1 /qa Step 4.5 Auto Commit Gate + /sv Ticket Commit 前置檢查 + cross_review .claude/ 動態路由 — 3 commits, 181 tests PASS**
> **Session P 完成（2026-05-29）：Pipeline v1.6 全鏈路 — D10-T2 HF2~HF4（DIFF_SCOPE 修正 + Pipeline Artifact 揭露 + annotation stripping）、V16-CR2（cross_review.sh v1.6 4-Verdict + Fail Budget + structural_verify.sh v1.2 Check C + sv/SKILL.md exit code 對應表）— HOTFIX-16~21，SV + Cross-Review R1 ✅ PASS，181 tests PASS**
> **Session Q 完成（2026-05-29）：Pipeline v1.6 全鏈路 — D11-T1（/qa→/sv Auto-Chain + PROGRESS 寫入拆分）、D11-T2（structural_verify.sh v1.3 Check D/E/F + /qa Step 5）、D11-T2-HF1（--post flag 解除 Check E 循環依賴）— HOTFIX-22~24，SV + Cross-Review PASS，181 tests PASS**
> **Session S 完成（2026-05-31）：P05-R1 GazeAF Ghost Face 三層修復（EyeAfController bbox 過濾 + gazeScore≤0.1f + LiveCameraSource coord clamp）QA PASS（181 tests）；INFRA-17 DIFF_REF 四段式合約（verify_checklist.sh v2 STALE_DIFF_REF 分類 + Suggested base + 三腳本協作）— HOTFIX-28 + INFRA-17，3 commits**
> **Session T 完成（2026-05-31）：INFRA-18 SV+Cross-Review PASS — DIFF_END 鎖定 Ticket 審查終點；P05-R1 QA PASS（181 tests）**
> **Session U 完成（2026-06-01）：P05-R1 Sony 實機三項驗收 PASS（IllegalArgumentException/gazeScore/bbox）；INFRA-19 index-verify Skill；HOTFIX-30~32 治理文件維護（過期路徑修正 + CSO_INDEX/SOP 靜動態重組）；P05-R1 Cross-Review R1 BLOCKER 等裁決**
> **Session V 完成（2026-06-01）：Ghost Face 渲染路徑根因定位（observeCompositionAdvice() → detectedFaces → HaloGuidanceLayer 無 bbox [0,1] 過濾）；CSO_INDEX CONTRACT 更新至 P08；P05 Contract Frozen 標記；KAIROS K3-8/K3-9 候選登記**
> **Session W 完成（2026-06-02）：INFRA-RFC2119 — 16 個 rules/skills 文件加 RFC 2119 anchor + MUST/SHOULD/MAY 標記；CSO Option A 裁決（77.3% MUST terminal）；22 降級；QA PASS；commit 1c47797 [INFRA-20]**
> **Session X 完成（2026-06-02）：INFRA-RFC2119 SV+Cross-Review PASS — Build 37s / Lint clean / ELF PASS；Cross-Review R4 ADVISORY（dtaudit/SKILL.md 禁止事項語意反轉，非 BLOCKER，後續 HOTFIX 修正）**
> **Session X 完成（2026-06-02）：HOTFIX-38/39/40 ✅；dtaudit MUST NOT 修正；CLAUDE.md mdlint + Markdown 紀律規則；structural_verify.sh 反引號修正；commit-msg INFRA-[A-Z0-9]+**
> **Session Y 完成（2026-06-02）：INFRA-21 CLAUDE.md 行數瘦身 173→145 行（Cross-Review 外遷至 cross-review.md + Path-Scoped Rules 壓縮）；P08-L1 Contract Amendment 套用（AI-2 CSO Override Skip）；Build/Lint/ELF ✅；Cross-Review R1 PASS**
> **Session Z 完成（2026-06-03）：P08-E1 PitchTractionLayer 清場 — drawFloatingDots + drawPitchTractionGradient + drawAlignedBreathingGradient + PitchTractionLayerTest 全數消滅（-561 lines）；SV 10/10 PASS；Cross-Review CSO Override ADVISORY；HOTFIX-40 ssot_context.md 例外條款；Traycer execute_review ✅ PASS（2026-06-04）**
> **Session AA 完成（2026-06-03）：P08-F1 Pre-completed 確認閉環（D05-T7 fa5afb8 已修正）；QA/SV/Cross-Review/Structural Verify 全 PASS；HOTFIX-41 cross_review.sh Excluded Payload 反引號 Bug 修正；Telegram 觸發條件精簡；Traycer execute_review ✅ PASS（2026-06-04）**
> **Session AB 完成（2026-06-03）：P08-F2 GridOverlay 三因子修復（alpha 0.08f→0.25f / strokeWidth 0.5px→1.5.dp / Z-order layer 2→5）；SV+Cross-Review R1 PASS；Telegram 觸發規則完善（實作完成、Auto-Chain、pipeline 暫停點全覆蓋）**
> **Session AC 完成（2026-06-03）：P08-F2 AI-2 追加修正（CSO 裁決 Option A — GridOverlay contentBounds→null，修復 21:9 格線 43% 缺口；lambda 孤兒清理 `_`）；/qa PASS（23s / 181 tests）；SV+Cross-Review R2 PASS（gpt-5.5 high）；Structural Verify A-F PASS；P08-F3 AI-1 Discovery（HaloGuidanceLayer 不依賴 contentBounds，零 Halo regression）；Traycer execute_review ✅ PASS（2026-06-04）**
> **Session AD 完成（2026-06-04）：P08-F3 AI-1 CSO 三裁決同步 + AI-3+AI-4 實作閉環（Ghost Face bbox 源頭過濾 + HaloGuidanceLayer normBbox guard）；SV+Cross-Review R1 PASS；INFRA-22/23 Telegram Stop Hook 智能路由（黑名單 + SV PASS 富通知）；P08-E1/F1/F2 Traycer execute_review PASS 狀態同步。注：AI-2 在此 session 判定為 skip（管線已是單一），HF1 後追補完整。**
> **Session AE 進行中（2026-06-04）：P08-F3-HF1 — `observeDetectionResults()` 函式刪除（死碼）；`publishSanitizedResult` local lambda（CSO Option A）收斂單寫入點；`facesFilteredToEmpty` bypass `MIN_NO_DETECTIONS_TO_HIDE` gate；QA PASS（181 tests / 20s）；待 /sv + Traycer 終審**
> **下一步：/sv → Cross-Review R3 → Traycer 終審 P08-F3+HF1（CameraViewModel），或 P08-F4 BUG-SONY-ROTATION-CONST Discovery**

1. ~~**🔴 P0-FPS**~~（✅ FIX-P0-SESSION-C-FINAL SV 待 Traycer）：FPS overlay 四層完整剝離
2. ~~**🔴 P0-FOCUS**~~（✅ QA PASS）：Guard 7 stableId preservation + FocusIndicator dwell fade
3. ~~**P05-T1**~~（✅ SV + Cross-Review PASS 2026-05-19）：`FocusIndicatorOverlay` runtime-disabled gate `isTapFocusReady=false`
4. ~~**P05-T2**~~（✅ SV + Cross-Review PASS 2026-05-19）：`TapFocusRing.kt` 新建 — 5態渲染 + 5 @Preview + `AmbientConstants` FOCUS_RING_SIZE_DP/STROKE_DP + Layer 3.1/semantics fix + PreviewCenter fix
4.5. ~~**P05-T6**~~（✅ SV + Cross-Review PASS 2026-05-19）：`CameraScreen.kt` GridOverlay visible 移除 `!contentBounds.isEmpty` 條件，修復啟動時 Grid 消失
4.6. ~~**P05-T5**~~（✅ SV + Cross-Review PASS 2026-05-19）：`CameraScreen.kt` ShutterButton 色彩硬編碼 — 外環 α0.30 / 內圓 White / 進度環 #50C864 α0.80，移除 MaterialTheme 依賴
5. **🔴 Baseline**：
   - `BUG-MLKIT-DUAL-EMIT`：`CameraViewModel.kt` 切換鏡頭清空 `detectionResult` 或改用 `flatMapLatest`
   - `BUG-PTF-HALO-SIZE`：PTF Halo 渲染層 bbox → 圓半徑轉換修正
6. **T03 重啟**（以上通過後）：Pill Toggle 重寫

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）

## D10 KAIROS #3 候選議程（Pre-W1-B 登記）

| ID | 候選項目 | 來源 | 類型 |
| ---- | --------- | ------ | ------ |
| K3-1 | Debug Overlay Zero Tolerance（debug 文字覆蓋零容忍） | D8 FIX-01~03 | CLAUDE.md 規則 |
| K3-2 | Hardcoded Text Scanning（硬編碼文字掃描） | D8 FIX-01~03 | /sv gate 擴充 |
| K3-3 | KAIROS Real-Device Screenshot Requirements | D8 FIX-01~03 | KAIROS 節點流程 |
| K3-4 | Decommission 票刪除驗收必須用 tracked filename glob 驗證 | FIX-INFRA-02-D | CLAUDE.md 規則 |
| K3-5 | Ticket 驗收清單與 SSOT 衝突時，以合併規格源為裁決依據並記錄衝突吸收 | TICKET-01 | CLAUDE.md 規則 |
| K3-6 | `cross_review.sh` payload 組裝時驗證 SV scope ↔ diff scope 一致性 | cross_review forensic | 腳本改進 |
| K3-7 | Edit 操作前強制 Re-view（>200 行目標檔 / /compact 後無條件觸發） | K3-7 Session Boundary Rule 修訂 | CLAUDE.md 規則 ✅ 已寫入 Rule 18 |

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_5.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_3.md`
- Full History: `PROGRESS_ARCHIVE.md`
