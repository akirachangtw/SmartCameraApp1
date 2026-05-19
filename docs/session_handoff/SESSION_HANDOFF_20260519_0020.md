# Cueshot Handoff — 2026-05-19

## Dashboard（新 session 先讀這段）
- **完成**: P05-T1 (Eye-AF visual ring MVP gate) + P05-T2 (TapFocusRing.kt 5-state rendering) 本地 SV ✅ + Cross-Review ✅ PASS（Round 14，gpt-5.5）。共 5 commits（D05-T1 × 1, D05-T2 × 3, INFRA-10 × 1）。
- **阻塞**: 無（SV/Cross-Review 已通過）。P05-T3 Tap 事件接線尚未開始。
- **下一步**: Traycer 終審（P05-T1 + P05-T2 commits）→ 通過後開 P05-T3（`CameraScreen.kt` tap gesture + `TapFocusRing` wiring + `isTapFocusReady = true`）
- **帶入**: CLAUDE.md · PROGRESS.md · `TapFocusRing.kt` · `CameraScreen.kt` · `sv_report.md`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `presentation/camera/CameraScreen.kt` | FocusIndicatorOverlay callsite | P05-T1: `isTapFocusReady = false` MVP gate wrapping `FocusIndicatorOverlay(...)` — Eye-AF 視覺環暫停，T3 解除 |
| `presentation/camera/components/TapFocusRing.kt` | 全新檔案 | P05-T2: `TapFocusState` sealed class (5態) + `TapFocusRing` Composable (60dp ring, Canvas budget=1 drawCircle) + Locked Badge (18dp, semantics non-interactive) + 5 @Preview |
| `domain/config/AmbientConstants.kt` | Tap Focus Ring section | P05-T2: `FOCUS_RING_SIZE_DP = 60f` + `FOCUS_RING_STROKE_DP = 1.5f` (Chief of Staff Ruling A) |
| `.claude/PROGRESS.md` | Sprint Health + Next Actions | INFRA-10: P05-T2 QA PASS 狀態同步 |
| `.claude/settings.local.json` | qa skill allowlist | INFRA-10: Skill(qa) 框架擴充 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `isTapFocusReady = false` 是條件 gate，非 Layer 3.2 decommission | FocusIndicatorOverlay.kt 未刪除；callsite 仍在 (runtime-disabled)；ViewModel chain 完整保留；無 DataStore/Settings 層需清理 | Cross-Review Round 8-13 反覆討論，Round 14 PASS |
| `TapFocusState` 定義在 presentation layer | 純 rendering state，無 domain 語意；不在 SSOT 型別清單 | TapFocusRing.kt 內聚，T3 wiring 時直接使用 |
| Locked Badge `semantics(mergeDescendants=false){}` | 18dp 低於 48dp touch target，需明確標記為 non-interactive 裝飾性元素 (Layer 3.1) | Cross-Review Round 6 FAIL → Round 7 fix |
| `PreviewCenter = Offset(150f, 150f)` | 原 Offset(400f,800f) 在 300dp canvas 外（y=800px > 300dp≈600px at 2x density） | Cross-Review Round 9 FAIL → Round 10 fix |
| `AmbientConstants.kt` 授權加 UI 常數 | Chief of Staff Ruling A；既有常數 SCRIM_HEIGHT_PX/VIEWFINDER_WEIGHT 同為 UI 常數，屬已建立慣例 | Cross-Review Round 12 FAIL → C2 SSOT Drift 補充說明後 PASS |
| SV Report 合併 P05-T1 + P05-T2 | TapFocusRing.kt 與 CameraScreen.kt 在同一 commit (be4585c)，無法分離 diff scope | 需在 Authorized Scope Declaration 明確聲明雙票 scope |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~5 --stat`
核心檔案：`git diff HEAD~5 -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/TapFocusRing.kt`

本 session commits（HEAD~5..HEAD）：
- `c138154` [D05-T2] TapFocusRing: fix PreviewCenter inside 300dp canvas bounds
- `7a3d5f1` [D05-T2] TapFocusRing: mark Locked badge non-interactive (Layer 3.1)
- `98fe836` [INFRA-10] PROGRESS.md P05-T2 QA PASS + settings.local.json Skill(qa) allowlist
- `8bceb95` [D05-T2] AmbientConstants FOCUS_RING_SIZE_DP + FOCUS_RING_STROKE_DP
- `be4585c` [D05-T1] Disable Eye-AF visual ring for MVP via isTapFocusReady=false gate

Cross-Review 歷程：Round 6→14，問題依序：
1. Touch target (R6) → semantics fix
2. semantics 未 commit 進 diff (R7) → 補 commit
3. SV scope 矛盾 (R8) → 合併 P05-T1+T2 scope 聲明 + Layer 3.2 rebuttal
4. diff scope HEAD~4 vs HEAD~5 (R10) → 統一
5. PreviewCenter 越界 (R9) → Offset(150f,150f)
6. diff scope 頭部殘留 (R10) → 全局統一 HEAD~5..HEAD
7. focusUiStateFlow "live" 用語 (R13) → 改為 "runtime-disabled gate"
8. AmbientConstants 分層責任 (R12) → C2 SSOT Drift 補充

SV Report 位置：`sv_report.md`（root，已進 .gitignore，勿 git add）

</details>
