# Cueshot Handoff — 2026-05-13 21:13

## Dashboard（新 session 先讀這段）
- **完成**: FIX-P0-SESSION-A 完整落地 — DetectionOverlay 全鏈 4 層焦土清理、CameraScreen 分隔線真 1px、CLAUDE.md Edge Warmth→Pitch Traction 命名淨化（4處）+ Rule 19 新增、ssot_context v1.1 同步至 SSOT v4.0；QA ✅ / SV ✅ / Cross-Review ✅ / 5 commits 封存；另同步產出 gstack v1.5 + devspec 文件遷移
- **阻塞**: 4 個 P0/P1 Bug 仍阻塞 T02 Hard Gate 視覺驗收（見 PROGRESS.md Active Blockers）；FIX-P0-SESSION-A 待 Traycer 終審（每日限 2-4 次）
- **下一步**: Traycer 終審 FIX-P0-SESSION-A（`bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md a7f6417..HEAD` 已 PASS）→ Session B 啟動 `BUG-MLKIT-DUAL-EMIT`：`CameraViewModel.kt` 切換鏡頭時 `_state.update { copy(detectionResult = null) }` 或改用 `flatMapLatest`
- **帶入**: CLAUDE.md · PROGRESS.md · `CameraViewModel.kt` · `CameraScreen.kt` · `sv_report.md`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `overlay/DetectionOverlay.kt` | 全檔 | 焦土刪除（-225L）— 4 層殘留第①層 |
| `OverlayPreferences.kt` | `KEY_DETECTION_OVERLAY_ENABLED` / `isDetectionOverlayEnabled` / `setDetectionOverlayEnabled` | 移除持久化 key + Flow + setter（-28L）— 第③層 |
| `CameraViewModel.kt` | L181 `isDetectionOverlayEnabled` | 移除 Flow 暴露面（-1L）— 第④層 |
| `SettingsViewModel.kt` | `isDetectionOverlayEnabled` / `setDetectionOverlayEnabled` | 移除 Flow + suspend fun（-12L）— 第④層 |
| `SettingsScreen.kt` | Detection Boxes toggle 區塊 | 移除 Settings 開關 UI（-16L）— 第②層 |
| `CameraScreen.kt` | L112 comment / L485-487 separator | 移除殘留 comment；分隔線 `height(1.dp)` → `height((1f / LocalDensity.current.density).dp)` |
| `.claude/CLAUDE.md` | L164 / L215-217 / L219 / L302 | Edge Warmth → Pitch Traction（4 處）；追加 Rule 19 Decommission 全鏈路紀律 |
| `.claude/skills/sv/ssot_context.md` | 架構層映射 / 核心型別 / 硬體約束 / 顏色表 / 版本 | v1.0→v1.1；SSOT v3.3→v4.0；EdgeWarmthLayer→PitchTractionLayer（授權連帶） |
| `devspec/gstack-lowcost-highdefense-workflow_v1_5.md` | 全檔（新建） | gstack v1.5：四大天王角色重定義 + 跨模型審查協議 + 付費機制明文化 |
| `plans/architecture/SSOT_v4_0.md` | 全檔（新建） | SSOT v4.0 規格基準 |
| `plans/architecture/Cueshot_Layout_Spec_v2_2.md` | 全檔（新建） | 佈局規格 v2.2 |
| `plans/cso_briefing/Phase0_UI_Realignment_Contract_v1.md` | 全檔（新建） | Phase0 UI 重對齊合約 |
| 舊 plans 6 檔 | 全檔 | 焦土刪除（v3.3/v3.4.1 superseded） |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| DetectionOverlay 4 層全焦土 | Traycer 退回：只停用呼叫點不算完成，Rule 19 明文化此紀律 | 5 檔 -282L |
| 分隔線改真 1px | Traycer 驗收規格：物理像素而非 dp 值 | CameraScreen.kt |
| CLAUDE.md Edge Warmth→Pitch Traction | FIX-P0N-01 授權；L11 歷史反例保留不動 | CLAUDE.md 4 處 |
| ssot_context.md 同步至 v4.0 | Cross-Review 發現 ssot_context 過期導致誤判；用戶明確授權 | ssot_context.md |
| cross_review.sh 指定 `a7f6417..HEAD` 固定 baseline | 每次補 commit 後 HEAD~N 自動偏移，改用 commit hash 固定範圍 | Cross-Review 流程 |
| gstack v1.5 新建（不覆蓋 v1.4） | 版本迭代保留歷史 | devspec/ |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更（本 session FIX-P0-SESSION-A，5 commits）：
```
git diff a7f6417..HEAD --stat
```

核心檔案快速查：
```
git diff a7f6417..HEAD -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt
git diff a7f6417..HEAD -p .claude/CLAUDE.md
git diff a7f6417..HEAD -p .claude/skills/sv/ssot_context.md
```

本次 session commits（a7f6417 之後）：
```
833474c [Docs] FIX-P0-SESSION-A sv_report — 補入 ssot_context 授權連帶說明
1b91aaa [Docs] Update ssot_context v1.1 — SSOT v4.0 同步
a197c18 [Docs] FIX-P0-SESSION-A sv_report — scope stat 補入連帶說明
3d9bf06 [Docs] FIX-P0-SESSION-A — CLAUDE.md 命名淨化 + Rule 19 + SV Report 重建
bed9091 [Fix] FIX-P0-SESSION-A — DetectionOverlay 全鏈清理 + 真 1px 分隔線
a7f6417 [Docs] Add gstack v1.5
2f8b4ba [Docs] Phase0 SSOT migration
b38fdfe [Docs] Add SSOT v4.0 + Layout Spec v2.2
```

Active Blockers（Session B 目標）：
- `BUG-MLKIT-DUAL-EMIT`（🔴 P0）：切換鏡頭後舊管線 DetectionResult 殘留 → Halo 閃爍
- `BUG-SONY-ROTATION-CONST`（🔴 P0）：Sony XQ-BT52 `rotationDegrees` 恆為 0
- `BUG-PTF-HALO-SIZE`（🔴 P0）：PTF Halo 渲染過大且不跟隨
- `BUG-I7-FOCUS-RING-PERSIST`（🔴 P1）：`EyeAfController.kt:process()` 需加 Guard 7

Cross-Review baseline：`a7f6417..HEAD`（固定，勿改為 HEAD~N）

</details>
