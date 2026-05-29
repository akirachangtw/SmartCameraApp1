# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D11 → D10-T1 ✅ · D10-T2 ✅ · HF1~HF4 ✅ · V16-CR2 ✅ · D11-T1 ✅ · **D11-T2 🟡 code DONE，待 /sv**
**Last Updated**: 2026-05-29

---

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | Stage0 W1 ✅ — P0-FPS ✅ — P0-FOCUS ✅ — P05-T1 ✅ — P05-T2 ✅ — P05-T5 ✅ — P05-T6 ✅ — **P05-T3 🟡 code DONE，待 Traycer** — P05-T7 🔧 — **D10-T1 ✅ · D10-T2 ✅ · HF1~HF4 ✅ · V16-CR2 ✅ SV PASS 待 Traycer** — **D11-T1 ✅ SV PASS 待 Traycer** — **D11-T2 🟡 /qa PASS 待 /sv** — **4 Bugs 待 Ticket（GridOverlay/GazeAF/PitchDot5/Pixel9aLayout）** |
| Compile | 0 errors ✅ (assembleDebug 20s) |
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

- **SSOT v4.0**: `plans/architecture/SSOT_v4_0_2.md` ← 唯一真理文件
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
> **下一步：D10 KAIROS /dream 回顧（議程見 § D10 KAIROS #3）或開 GridOverlay Bug Ticket（alpha/strokeWidth/Z-order 三層根因已定位）**

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

- SSOT: `plans/architecture/SSOT_v4_0_2.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_3.md`
- Full History: `PROGRESS_ARCHIVE.md`
