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

## Level 2 可行性確認

**此 PR 是否已完成 Level 2 評估?**
- ☐ 是 → Issue #______ 中的 Level 2 結論為「可行」
- ☐ 否 → 說明為何跳過: ___________

---

## 對應 Issue
- Closes #______
- Related to #______
