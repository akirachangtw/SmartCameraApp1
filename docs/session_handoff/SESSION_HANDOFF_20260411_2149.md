# Cueshot 工作交接 — 20260411_2149

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `.claude/CLAUDE.md` | 零容忍執行紀律 | 新增規則 #10 Bash 指令安全格式（禁止 `cd &&`、強制相對路徑）；i18n 調整為 #9、Bash safety 調整為 #10；KAIROS 規則重編號 #11/#12 |
| `.claude/settings.local.json` | permissions.allow | 新增 `"Edit(*)"` 至白名單 |
| `presentation/camera/CameraViewModel.kt` | bitmap analysis pipeline (~L1627) | FIX-01：移除 `when(deviceCapabilityTier)` 三路分支及 `600f` LEGACY 硬編碼；改為單一 hardware-first `run{}` 路徑；擴充 fail-safe KDoc（幾何意義 + 0f 哨兵說明） |
| `data/source/camera/LiveCameraSource.kt` | `unbind()` + rebind 前 unbindAll 區塊 | FIX-02：`unbindAll()` 成功後重置 `focalLengthFactor = null`；兩處各補 `[D3-T3]` log |
| `domain/camera/ImuCompensator.kt` | KDoc Usage 範例 (~L60) | FIX-03：刪除 `focalLengthPixels = 800f` 舊範例；替換為 hardware-first / fail-safe 雙路說明 |
| `domain/repository/CameraRepository.kt` | `getFocalLengthFactor()` KDoc | FIX-03：拆分 null 語義為 Temporary（未 bind）vs Permanent（硬體不支援）兩種情況 |

## 當前阻塞點

無

## 下一步待辦

1. **[PROGRESS.md 更新]**：將 SV-D3-T3-FIX-01/02/03 全數標記為 ✅ DONE；D3 整體狀態改為 ✅ CLOSED。

2. **[Traycer 終審]**：提交 commits `d348e8f`（FIX-01/02）和 `d96366f`（FIX-03）以 `strict_verification` 模式請求終審。

3. **[焦土] 清除 `CameraScreen.kt:106` `GuidelineOverlay` dead import**：確認無 runtime 使用後刪除該 import，執行 `/qa`。

4. **[Remote Push]**：執行 `git push origin feature/cueshot-mvp`（本 Session commits 尚未 push）。

5. **[D4-T1] Edge Warmth 第二層（浮動光點）**：依 SSOT v3.3 §EdgeWarmthLayer 實作浮動光點 Overlay，接線至 `CameraState.deviceRoll`。開工前先確認 SSOT 段落。

6. **[D4-T2] 呼吸韻律初版**：`#50C864` 綠色脈動動畫，接線至 `CameraState.compositionResult.isAllAligned`。

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| FIX-01 使用 `run{}` 取代 `when(deviceCapabilityTier)` | 所有 tier 皆走 hardware-first；LEGACY 的 `600f` 是 SSOT 未授權的魔術數字，無幾何依據 | `CameraViewModel.kt` |
| fail-safe 確立為 `previewWidth / 2f`（幾何意義：hFov≈90°, tan45°=1） | 有數學依據；`previewWidth=0` 時結果 `0f`，ImuCompensator 安全靜默 | `CameraViewModel.kt` |
| FIX-02 歸零點在 `unbindAll()` **成功後**，不在 catch 中 | 失敗的 unbind 不應清除 factor；stale 汙染只發生在成功解綁時 | `LiveCameraSource.kt` |
| CLAUDE.md Bash safety 規則插入為第 #10 條，i18n 維持 #9 | Bash 安全規範屬核心行為紀律，次序在 i18n 之後 | `CLAUDE.md` |

## 需帶入新對話的 Context

- `CLAUDE.md`（已更新：規則 #10 Bash safety，KAIROS #11/#12 重編號）
- `PROGRESS.md`（需更新：FIX-01/02/03 → ✅ DONE，D3 → CLOSED）
- `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`（D4 EdgeWarmth 規格來源）
- `presentation/camera/CameraScreen.kt`（dead import 清理 + D4 overlay 插入點）
- `presentation/camera/CameraState.kt`（含 deviceRoll、compositionResult 欄位）
- `devspec/feature_record_Backlog.md`（ADR + 技術債清單）

## 不需帶入的 Context

- D3-T3-FIX-01/02/03 的所有討論（已完成，commits d348e8f + d96366f）
- `600f` / `800f` 魔術數字的所有討論（已清零，grep = 0 hits）
- `when(deviceCapabilityTier)` 分支結構的討論（已拆除）
- CLAUDE.md 規則新增與編號調整的討論（已提交 commit 7ff366b）
- settings.local.json `Edit(*)` 新增的討論（已提交）
- SV 報告細節（結論：全部 ✅ PASS，Lint pre-existing 豁免有效）
