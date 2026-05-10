# Cueshot Handoff — 2026-05-11

## Dashboard（新 session 先讀這段）
- **完成**: 本次 session 完成 KAIROS #3 K3-7 規則寫入（Rule 18）、Claude Code statusline 基礎設施提交、T02 Sony 實機驗測（Hard Gate 🔴 FAIL 確認）、T03 誤自動執行後已還原。
- **阻塞**: 🔴 **BHA-01（Bbox Hard Gate）** — T02 Hard Gate 未通過：HaloGuidanceLayer 座標空間錯配，face bbox 2.25× 位移（分析幀 480×640 vs VIEW 1080×1898）。T02 不得標記完成。
- **下一步**: 開立並執行 **BHA-01 診斷票** — 確認 `CameraViewModel.kt` ~1988 行 `boundingBoxNormalized` 歸一化座標空間（VIEW_REFERENCED vs IMAGE_REFERENCED），再決定修復路徑。
- **帶入**: CLAUDE.md · PROGRESS.md · `CameraViewModel.kt` · `HaloGuidanceLayer.kt` · `NormalizedToPixelTransformer.kt` · `PreviewViewportContainer.kt`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `.claude/CLAUDE.md` | KAIROS #3 區塊 | 新增 Rule 18 — Edit 操作前強制 Re-view（K3-7） |
| `.claude/PROGRESS.md` | D10 KAIROS #3 候選議程 | 新增 K3-7 條目，標記 ✅ 已寫入 Rule 18 |
| `.claude/settings.json` | statusLine | 啟用 statusLine command hook → `~/.claude/statusline.sh` |
| `.claude/statusline.sh` | — | 新建：Model / Context / Rate Limit / Cost 顯示腳本 |
| `CameraTabBar.kt` | 全檔 | T03 Stage0 Pill Toggle 重寫（已 revert） |
| `CameraScreen.kt` | CameraTabBar 呼叫處 | T03 移除 weight(1f)（已 revert） |

**注意**：T03 commit `2a75ca3` 已被 `b820f50` revert，兩檔回到 T02 完成狀態。

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| T03 還原 | 未收到明確提交單即自動實作，違反鐵律 4 | CameraTabBar.kt, CameraScreen.kt |
| K3-7 採納為 Rule 18 | 根因正確（顯式 Read > session 記憶），/compact 後強制觸發 | CLAUDE.md |
| BHA-01 新增為 Hard Gate blocker | Sony 實測確認 bbox 明顯偏移，T02 不得完成 | T02 驗收狀態 |
| T02 Hard Gate 🔴 FAIL | contentBounds=[L=6, T=0, W=1068, H=1898]；分析幀 480×640 歸一化後乘以 canvas 1080×1898 → 2.25× 錯位 | HaloGuidanceLayer.kt |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~4 --stat`
核心 bug 位置：`CameraViewModel.kt` ~1982-2014（`boundingBoxNormalized` 計算）
對照：`HaloGuidanceLayer.kt` ~267（`NormalizedToPixelTransformer` 構建）
Logcat 關鍵行：`imageProxy=480x640 preview=1080x1898 contentBounds=[L=6.1875, T=0.0, W=1067.625, H=1898.0]`
確認 commit 數：`git log --oneline -10`

</details>

---

## BHA-01 診斷票（下一步）

**問題**：HaloGuidanceLayer 人臉圓環與實際人臉位置偏移（T02 Hard Gate FAIL）

**診斷目標**：
1. 確認 `CameraViewModel.kt` ~1988 行的 `face.boundingBox` 座標空間
   - 若 **VIEW_REFERENCED**：`box.left` 單位是 view px（0~1080），用 `imgW=480` 歸一化 → 2.25× 放大錯誤
   - 若 **IMAGE_REFERENCED**：`box.left` 單位是分析幀 px（0~480），用 `imgW=480` 歸一化 → 正確，問題在別處
2. 追蹤 `det` 物件來源（SmartCameraAnalyzer / MLKitDetector），確認使用哪種 coordinate reference
3. 比對 V2 pipeline（`sensorToViewMatrix`）正確映射路徑，決定修復方案

**修復候選**：
- A：若 VIEW_REFERENCED → 改用 `viewWidth/viewHeight` 歸一化
- B：若 IMAGE_REFERENCED → 修正 `NormalizedToPixelTransformer` 加入 `contentBounds` offset
