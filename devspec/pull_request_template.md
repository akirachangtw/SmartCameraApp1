# Pull Request

> **工作流級別**: Level 3 (實作與驗證)
>
> ⚠️ 提交前請確認已完成 Level 2 可行性評估，且所有驗證項目已勾選

---

## 變更摘要（What changed）
- ☐ 功能新增
- ☐ Bug 修正
- ☐ 重構
- ☐ 效能改善
- ☐ 其他：

請簡述本次 PR 的目的與核心變更。

---

## 影響範圍（Impact scope）
- ☐ CameraX / Preview / ImageAnalysis
- ☐ 模擬器 vs 實體裝置
- ☐ FPS / Memory / CPU / GPU
- ☐ Overlay / UI
- ☐ AI / ML Kit / TFLite
- ☐ 僅局部低風險模組

---

## 驗證方式（How verified）

### ✅ 必要驗證 (全部必須勾選)
- ☐ **階段驗測已完成** ✨ NEW
  - 驗測報告: `docs/test_results/<version>/VERIFICATION_REPORT.md`
  - ☐ Phase 1-3: Pre-Build + Build + 16KB ✅
  - ☐ Phase 4: Emulator Testing ✅
  - ☐ Phase 5: Device Testing ✅ / ⏭️ SKIPPED
- ☐ Build 成功 (`./gradlew assembleDebug` 或 `./gradlew assembleEmulatorDebug`)
- ☐ Lint 檢查通過
- ☐ 無新增 Logcat 錯誤

### 🔬 功能驗證
- ☐ 單元測試通過（如適用）
  - 測試指令: `./gradlew test` 或 `./gradlew testDebugUnitTest`
  - 覆蓋率: ______%
- ☐ 模擬器驗證
  - API Level: ______
  - 驗證場景: _______________
- ☐ **實體裝置驗證** ⭐ **高風險變更必須**
  - 型號: _______________
  - Android 版本: _______________
  - 驗證場景: _______________

### 📊 效能驗證 (如涉及效能)
- ☐ FPS 監控: 變更前 ______ fps / 變更後 ______ fps
- ☐ Memory 監控: 變更前 ______ MB / 變更後 ______ MB
- ☐ 無明顯卡頓或延遲

### 📝 驗證詳情
<!-- 請說明驗證指令、步驟與結果 -->

### 📊 驗測報告連結
- 驗測報告: `docs/test_results/<version>/VERIFICATION_REPORT.md`
- Logcat (Emulator): `docs/test_results/<version>/emulator_test_logcat.log`
- 16KB Check: `docs/test_results/<version>/16kb_check_emulator.log`

---

## 風險與回滾（Risk & rollback）

### 潛在風險
1.
2.
3.

### 回滾方案
- ☐ 可直接 revert commit: `git revert <commit-sha>`
- ☐ 需 feature flag 控制: flag 名稱 `___________`
- ☐ 需配置檔案調整: 檔案 `___________`
- ☐ 其他: ___________

### 影響評估
- 影響使用者數量: ☐ 全部 ☐ 部分 (說明: _______)
- 是否可降級: ☐ 是 ☐ 否
- 回滾時間預估: ______ 分鐘

---

## 🔍 Regression Check（審查者必查）

> **目的**: 預防重新引入已解決的 bug（如 v2.9.8 Toast → v2.10.1 regression → v2.10.2 修正）

### 歷史修正搜尋
- ☐ 已搜尋相關功能的歷史修正記錄
  - Git log: `git log --grep="<keyword>" --oneline --all`
  - Feature Record: 查閱 `devspec/feature_record_Backlog.md`
  - Test Reports: 查閱 `docs/test_results/`
  - 搜尋關鍵字: _______________
  - 發現相關 commit: _______________

### Regression 風險確認
- ☐ 確認不會引入已解決的 bug
  - ☐ 檢查 `feature_record_Backlog.md` 的 ⚠️ Critical Warning 區段
  - ☐ 確認修改不違反已驗證的行為（Confirmed 區段）
  - ☐ 查閱 CHANGELOG.md 中的 Fixed 區段

### Critical Files 檢查（如有修改）
- ☐ **CameraScreen.kt** - 照片保存反饋
  - ⚠️ DO NOT re-add Snackbar for "照片已儲存"（v2.9.8 已移除）
  - ⚠️ DO NOT add photo info button（v2.10.2 已移除）
- ☐ **CameraViewModel.kt** - 長寬比例設定
  - ⚠️ 使用 `.collect()` 持續監聽，不要用 `.first()`（v2.10.2 修正）
  - ⚠️ Optical Flow fade-out guard（參考 MEMORY.md）
- ☐ **MainActivity.kt** - 權限請求
  - ⚠️ 啟動時一次性請求所有權限（v2.10.2 用戶期望）
- ☐ **LiveCameraSource.kt** - 相機設定
  - ⚠️ ViewPort.setScaleType(FIT) 必須存在（v2.10.0-fix）
  - ⚠️ 1:1 模式使用 ResolutionFilter

### 文檔更新（如有重大修正）
- ☐ 更新 `devspec/feature_record_Backlog.md`（如有新 Confirmed 行為）
  - ☐ 添加 Confirmed 行為描述
  - ☐ 更新 Code Locations（如行號變動）
  - ☐ 添加 Critical Warning（如有必要）
  - ☐ 記錄 Regression 歷史（如適用）
- ☐ 更新 `CHANGELOG.md`（用戶可見變更）
- ☐ 創建測試報告 `docs/test_results/<version>/`（如有測試）

---

## Level 2 可行性確認

**此 PR 是否已完成 Level 2 評估?**
- ☐ 是 → Issue #______ 中的 Level 2 結論為「可行」
- ☐ 否 → 說明為何跳過: ___________

---

## 對應 Issue
- Closes #______
- Related to #______
