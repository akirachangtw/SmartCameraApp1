# Cueshot Handoff — 2026-05-13 22:33

## Dashboard（新 session 先讀這段）
- **完成**: FIX-P0-SESSION-A 全鏈閉環（DetectionOverlay 焦土 4 層、真 1px 分隔線、CLAUDE.md Rule 19-21、ssot_context v4.0）+ FIX-P0-SESSION-A-FINAL-SYNC（L11 命名淨化、PROGRESS 三處一致）+ FIX-GSTACK-SSOT-LINK（治理文件指針全清零，舊路徑 0 命中）；QA ✅ / SV ✅ / Cross-Review ✅ / 9 commits 封存
- **阻塞**: 4 個 P0/P1 Bug 仍阻塞 T02 Hard Gate 視覺驗收（見 PROGRESS.md Active Blockers）；FIX-P0-SESSION-A 系列待 Traycer Path A 終審
- **下一步**: Traycer 終審 → Session B 啟動 `BUG-MLKIT-DUAL-EMIT`：`CameraViewModel.kt` 切換鏡頭時 `_state.update { copy(detectionResult = null) }` 或改用 `flatMapLatest`
- **帶入**: CLAUDE.md · PROGRESS.md · `CameraViewModel.kt` · `CameraScreen.kt` · `sv_report.md`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `overlay/DetectionOverlay.kt` | 全檔 | 焦土刪除（-225L）— 第①層 |
| `OverlayPreferences.kt` | `KEY_DETECTION_OVERLAY_ENABLED` / Flow / setter | 移除持久化 key（-28L）— 第③層 |
| `CameraViewModel.kt` | `isDetectionOverlayEnabled` | 移除 Flow 暴露面（-1L）— 第④層 |
| `SettingsViewModel.kt` | `isDetectionOverlayEnabled` / `setDetectionOverlayEnabled` | 移除 Flow + suspend fun（-12L）— 第④層 |
| `SettingsScreen.kt` | Detection Boxes toggle 區塊 | 移除 Settings 開關 UI（-16L）— 第②層 |
| `CameraScreen.kt` | L485-487 separator | 分隔線 `height(1.dp)` → 真 1px `(1f / LocalDensity.current.density).dp` |
| `.claude/CLAUDE.md` | L11 / L89-90 / Rule 19-21 / gstack / SSOT 路徑 | 命名淨化全清零；Rule 19（Decommission）、Rule 20（PROGRESS 一致性）、Rule 21（SSOT 遷移紀律）寫入；gstack v1.3→v1.5；SSOT 路徑更新 4 處 |
| `.claude/skills/sv/ssot_context.md` | 架構層映射 / 核心型別 / 版本 | v1.0→v1.1；SSOT v3.3→v4.0；EdgeWarmthLayer→PitchTractionLayer |
| `.claude/PROGRESS.md` | Source Documents / Quick Links / Header / Sprint Health / Next Actions | 移除 5 個失效文件引用；三處狀態統一 DONE；新增 SSOT v4.0 + Layout Spec v2.2 指針 |
| `sv_report.md` | 全檔 | FIX-P0-SESSION-A + FINAL-SYNC + GSTACK-SSOT-LINK 三段驗收結果合併 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| DetectionOverlay 4 層全焦土 | Traycer 退回：只停用呼叫點不算完成，Rule 19 明文化此紀律 | 5 檔 -282L |
| 分隔線改真 1px | Traycer 驗收規格：物理像素而非 dp 值 | CameraScreen.kt |
| CLAUDE.md L11 授權覆蓋歷史反例 | 使用者明確授權 Option A，EdgeWarmthLayer→PitchTractionLayer 全局清零 | CLAUDE.md 5 處 |
| Rule 20/21 從 CANDIDATE 升正式 | FIX-GSTACK-SSOT-LINK 閉環確認，兩條規則均有真實觸發案例 | CLAUDE.md |
| PROGRESS.md 失效連結全清零 | FIX-GSTACK-SSOT-LINK：grep 確認舊路徑 0 命中後方提交（Rule 21 首次執行） | PROGRESS.md |
| Cross-Review baseline 固定為 commit hash | `a91fde0` 作為 FIX-GSTACK-SSOT-LINK baseline，避免 HEAD~N 偏移 | Cross-Review 流程 |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更（本 session，10 commits）：
```
git diff bed9091~1..HEAD --stat
```

核心程式碼變更（FIX-P0-SESSION-A，1 commit）：
```
git diff bed9091~1..bed9091 -p
```

治理文件變更（FINAL-SYNC + GSTACK-SSOT-LINK，5 commits）：
```
git diff c7fcd20~1..HEAD -p .claude/CLAUDE.md
git diff a91fde0~1..HEAD -p .claude/PROGRESS.md
```

本次 session commits（共 10）：
```
b192477 [Docs] Add Session Handoff 20260513 — FIX-P0-SESSION-A 閉環交接
977e133 [Docs] FIX-GSTACK-SSOT-LINK — PROGRESS 指針清理 + SV Report 閉環
4d30183 [Docs] FIX-GSTACK-SSOT-LINK — CLAUDE.md Rule 20/21 正式寫入 + 路徑同步
a91fde0 [Docs] FIX-P0-SESSION-A-FINAL-SYNC — PROGRESS + SV Report 閉環
c7fcd20 [Docs] FIX-P0-SESSION-A-FINAL-SYNC — CLAUDE.md 治理同步
833474c [Docs] FIX-P0-SESSION-A sv_report — 補入 ssot_context 授權連帶說明
1b91aaa [Docs] Update ssot_context v1.1 — SSOT v4.0 同步
a197c18 [Docs] FIX-P0-SESSION-A sv_report — scope stat 補入
3d9bf06 [Docs] FIX-P0-SESSION-A — CLAUDE.md 命名淨化 + Rule 19 + SV Report 重建
bed9091 [Fix] FIX-P0-SESSION-A — DetectionOverlay 全鏈清理 + 真 1px 分隔線
```

Active Blockers（Session B 目標）：
- `BUG-MLKIT-DUAL-EMIT`（🔴 P0）：切換鏡頭後舊管線 DetectionResult 殘留 → Halo 閃爍
- `BUG-SONY-ROTATION-CONST`（🔴 P0）：Sony XQ-BT52 `rotationDegrees` 恆為 0
- `BUG-PTF-HALO-SIZE`（🔴 P0）：PTF Halo 渲染過大且不跟隨
- `BUG-I7-FOCUS-RING-PERSIST`（🔴 P1）：`EyeAfController.kt:process()` 需加 Guard 7

Cross-Review baseline：`a91fde0`（FIX-GSTACK-SSOT-LINK）

</details>
