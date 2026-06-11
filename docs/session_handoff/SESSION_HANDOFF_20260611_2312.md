# Cueshot Handoff — 2026-06-11

## Dashboard（新 session 先讀這段）

- **完成**: HOTFIX-47 GhostFace_GuidanceGate 全流程閉環 — SV R2 PASS + Cross-Review R2 PASS + 實機驗收 GF-01~03（Pixel 9a + Sony XQ-BT52 雙裝置全通過）+ Traycer 終審 PASS + K4-Rule 28 歸檔 + squash merge 至 feature/cueshot-mvp
- **阻塞**: BUG-MLKIT-DUAL-EMIT（P0）/ BUG-PTF-HALO-SIZE（P0）/ BUG-SONY-ROTATION-CONST（P0）/ I7 BUG-I7-FOCUS-RING-PERSIST（P1）— 均待下一 Ticket
- **下一步**: 開新 Ticket — `BUG-MLKIT-DUAL-EMIT`（切換鏡頭清空 `detectionResult`，`CameraViewModel.kt` `flatMapLatest` 或切換時 `_state.update { copy(detectionResult = null) }`）或 `BUG-PTF-HALO-SIZE`（`HaloGuidanceLayer.kt` PTF Halo bbox→圓半徑轉換）
- **帶入**: CLAUDE.md · PROGRESS.md · CameraViewModel.kt · CameraRepositoryImpl.kt

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `CameraRepositoryImpl.kt` | `faceDetector` init | `CLASSIFICATION_MODE_ALL` 正式化，SPIKE-01 probe log 替換為 HOTFIX-47 |
| `MLKitDetector.kt` | class header | LEGACY 標記（非活躍偵測路徑，生產 SSOT 為 CameraRepositoryImpl.faceDetector） |
| `CameraViewModel.kt` | companion object | `MIN_EYE_OPEN_PROB_FOR_GUIDANCE = 0.9f` 常數新增 |
| `CameraViewModel.kt` | `observeCompositionAdvice` | Branch A guidance gate inline lambda — `guidanceEligibleFaces = det.faces.filter { minOf(r,l) > 0.9f }`，`landmarkFaces` 改用 guidanceEligibleFaces |
| `CameraViewModel.kt` | `observePtfAlignment` | Branch A guidance gate inline lambda — `firstFace = det.faces.firstOrNull { minOf(r,l) > 0.9f }` |
| `CameraViewModel.kt` | `publishSanitizedResult` | SPIKE-01 per-face probe log 替換為節流 summary（每 30 幀）+ eligibleCount 計算 |
| `.claude/rules/kairos-lessons.md` | K4-Rule 28 | 新增 Guidance Gate Hotfix 三項架構邊界（Branch 分離 / Branch B 存活 / Magic Number 禁止） |
| `.claude/PROGRESS.md` | Dashboard | HOTFIX-47 Traycer PASS 標記，Next Actions 更新 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `MIN_EYE_OPEN_PROB_FOR_GUIDANCE = 0.9f` 門檻 | SPIKE-GF Phase C 數據：K380 0/91 pass（null eye probs），Pixel 9a 真人 328/341 (96%) pass | CameraViewModel.kt Branch A 所有消費點 |
| inline lambda × 3（不加 named function） | SSOT §3 Scope Ban：CameraViewModel.kt 禁止新增任何 named fun（2457 行，8.2× 限制）；Cross-Review R1 BLOCKER 修正為 v1.1 | CameraViewModel.kt 三個 call site |
| Branch A/B 分離：shared raw state 不刪臉 | Branch B（buildFocusTargetsFromDetection）依賴完整 `_state.detectionResult.faces`；AF 需在 guidance=0 時仍存活 | K4-Rule 28 架構邊界 |
| MLKitDetector 標記 LEGACY 不修配置 | 確認非活躍路徑（ImageAnalyzer.kt 只有 dead import），修配置會產生 DI 衝突風險 | MLKitDetector.kt |
| Sony GF-02 實際 100%（非預期 47%） | BUG-SONY-ROTATION-CONST 只影響特定鏡頭方向，正面場景 eye-open 分類正常 | Sony 47% 覆蓋率估計為特定旋轉場景，待 F4 Discovery |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

本次 commits（HEAD~2）：

```
0cfd9da [HOTFIX-47] PROGRESS + K4-Rule 28 + INFRA-34~42 squash merge (spike branch)
454edd6 [HOTFIX-47] Ghost Face guidance-scoped eye-open gate + SPIKE-01 cleanup (GF-01~03 PASS)
```

全局變更：`git diff HEAD~2 --stat`

代碼 diff：`git diff HEAD~2 -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt`

驗收數據（Logcat 實測）：

- GF-01 Pixel 9a：`Guidance gate: 0/1 eligible`, `0/3 eligible`（K380 圓鍵偵測，全攔截）
- GF-01 Sony：`Guidance gate: 0/1 eligible`（穩定 15+ 秒）
- GF-02 Pixel 9a：`1/1 eligible` × 50+ 幀（100%）
- GF-02 Sony：`1/1 eligible` × 全程 29 秒（100%）
- GF-03 兩裝置：K380 場景點擊，綠色對焦圓環正常出現（AF 獨立確認）

</details>
