# Cueshot Handoff — 2026-05-11 20:58

## Dashboard（新 session 先讀這段）
- **完成**: BHA-01-FIX 實作並通過 Codex Cross-Review（rotation swap + log enrichment）；Sony XQ-BT52 Hard Gate 完成，歸一化數學全場景 ✅ PASS
- **阻塞**: 3 個前置 Bug 阻塞 T02 視覺 Hard Gate：`BUG-MLKIT-DUAL-EMIT`（P0）/ `BUG-SONY-ROTATION-CONST`（P0）/ `BUG-PTF-HALO-SIZE`（P0）
- **下一步**: 優先修復 `BUG-MLKIT-DUAL-EMIT` — `CameraViewModel.kt` 切換鏡頭時清空 `detectionResult`，或將 DetectionResult 訂閱改為 `flatMapLatest`；完成後重跑場景二/四視覺驗證
- **帶入**: CLAUDE.md · PROGRESS.md · `CameraViewModel.kt` · `sv_report.md`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `CameraViewModel.kt` | `observeCompositionAdvice` (line ~1982) | BHA-01-FIX: rotation swap（imgW/imgH 互換 90°/270°）+ log enrichment（raw/rotation/effective/normBbox） |
| `CameraViewModel.kt` | `observePtfAlignment` (line ~2081) | 同上，對稱修復 PTF pipeline |
| `.claude/settings.json` | `permissions.allow` | 清空，規則全數移至 settings.local.json |
| `.claude/settings.local.json` | `permissions.allow` | 集中化 + 優化：`Bash(bash *)` 取代兩條具體路徑，新增 `Bash(Write-Host *)` |
| `.claude/skills/status/scripts/statusline.sh` | 全檔 | 新建：CACHE_FILE 改相對路徑 `tmp/claude_status_cache.json` |
| `.claude/skills/status/SKILL.md` | Step 1 | 優先讀取 statusline 快取真實數據，fallback 估算 |
| `.claude/settings.json` | `statusLine.command` | 路徑修正：`bash .claude/skills/status/scripts/statusline.sh` |
| `sv_report.md` | Device Hard Gate Report | Sony XQ-BT52 四場景驗證結果 + 3 Bug 登記 |
| `.claude/PROGRESS.md` | Active Blockers / Next Actions | 新增 3 Bug + 修復順序更新 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| BHA-01 rotation swap 修復方案 A | 對齊既有 `updateCoordinateTransformer` line 585 模式，最小風險 | `CameraViewModel.kt` 兩處 |
| Cross-Review 第一輪 FAIL → 先 commit 再重跑 | diff scope `HEAD~1..HEAD` 抓不到未 commit 的 CameraViewModel，需先提交才能驗證 | cross_review.sh 工作流程 |
| Sony rotation=0 定性為裝置 Bug | Sony XQ-BT52 直立/橫向皆回報 `rotationDegrees=0`，非 BHA-01-FIX 問題，需獨立 Ticket | 裝置相容性處理 |
| BUG-MLKIT-DUAL-EMIT 根因 | 切換鏡頭後舊 DetectionResult Flow 未取消，與新鏡頭交替輸出，Logcat 可見同毫秒兩筆不同 bbox | `CameraViewModel.kt` 鏡頭切換邏輯 |
| 允許規則集中至 settings.local.json | 統一管理，`Bash(bash *)` 萬用規則取代多條具體路徑 | `.claude/settings.*` |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~6 --stat`

本次 Session commits（HEAD~6 起）：
```
9a9d178 [Docs] Hard Gate Report
ea8d5e7 [Fix] BHA-01-FIX-VERIFY log enrichment
be1a220 [Infra] 允許規則集中化
c7180fd [Fix] BHA-01-FIX rotation swap
42e1184 [Docs] PROGRESS + SV Report
693b0e2 [Infra] StatusLine hook 整合（含 statusline.sh 新建）
```

BUG-MLKIT-DUAL-EMIT 根因位置：
- `CameraViewModel.kt` — 鏡頭切換時 DetectionResult Flow 訂閱未清除
- Logcat 證據：同毫秒兩筆 `face[0]` bbox 大小不同交替出現
- 修復建議：`flatMapLatest` 或切換時 `_state.update { copy(detectionResult = null) }`

BUG-SONY-ROTATION-CONST：
- Sony XQ-BT52 `det.rotationDegrees` 恆為 0（直立/橫向皆同）
- rotation swap 在此裝置永遠不觸發
- 建議改用 `display.rotation` 或 CameraX `Surface.ROTATION_*`

BUG-PTF-HALO-SIZE：
- PTF Halo 渲染層 bbox → 圓半徑轉換錯誤
- 座標數學已確認正確（`ptf normBbox` 全部 [0,1]）
- 追蹤 `HaloGuidanceLayer` 或 PTF overlay 渲染邏輯

</details>
