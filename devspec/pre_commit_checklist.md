# Pre-Commit Checklist

> **目的**: 提交代碼前的必要檢查清單，預防 Regression 和確保代碼品質
> **使用時機**: 每次執行 `git commit` 之前
> **維護者**: 全體開發者

---

## 🔍 代碼變更前必查（Regression 預防）

### 1. 搜尋歷史修正

**為什麼重要**: 避免重新引入已解決的 bug（如 v2.9.8 Toast 訊息 → v2.10.1 regression → v2.10.2 再修正）

**檢查步驟**:
- [ ] 搜尋相關功能的歷史 commit
  ```bash
  # 搜尋關鍵字（例如：Toast、Snackbar、Permission）
  git log --grep="<keyword>" --oneline --all

  # 搜尋修改過特定檔案的 commit
  git log --oneline -- path/to/file.kt

  # 搜尋包含特定代碼變更的 commit
  git log -S "code snippet" --oneline
  ```

- [ ] 查閱 `devspec/feature_record_Backlog.md` 的 ⚠️ Critical Warning 區段
  - 搜尋相關功能名稱
  - 檢查是否有 "DO NOT" 警告
  - 閱讀 Regression 歷史記錄

- [ ] 查閱相關的測試報告 `docs/test_results/<version>/`
  - 確認功能的驗證行為
  - 理解為什麼要這樣實作

---

### 2. 確認不引入 Regression

**檢查清單**:
- [ ] 確認修正**不會覆蓋**之前的 bug fix
  - 檢查是否有 Critical Warning 提到相關功能
  - 確認修改不違反已驗證的行為（feature_record_Backlog.md 中的 Confirmed）

- [ ] 檢查是否重新引入已解決的問題
  - 查看 CHANGELOG.md 中的 Fixed 區段
  - 確認沒有回到舊的實作方式

- [ ] 如有疑問，與團隊討論或查閱文檔
  - 不確定時，不要假設「應該沒問題」
  - 使用 git blame 找到原作者詢問

---

### 3. 記錄重要修正

**如果你的修正很重要（影響用戶體驗或修復 regression）**:
- [ ] 更新 `devspec/feature_record_Backlog.md`
  - 在相關功能的 Confirmed 區段添加驗證記錄
  - 更新 Code Locations（如行號變動）
  - 添加 ⚠️ Critical Warning（如有必要）

- [ ] 提供 Alternative Solution（如有替代方案）
  - 說明為什麼不採用其他方案
  - 提供未來可能的改進方向

- [ ] 記錄 Regression 歷史（如適用）
  - 格式：`v2.9.8 修正 → v2.10.1 regression → v2.10.2 再修正`
  - 說明為什麼會發生 regression
  - 提供預防措施

---

## ❌ 高風險修改警告

**如果你要修改以下功能，請先查閱 `feature_record_Backlog.md`：**

### 🚨 Critical Files（已知有 Regression 歷史）

- [ ] **CameraScreen.kt** - 照片保存反饋（Snackbar、Toast 訊息）
  - ⚠️ DO NOT re-add Snackbar for "照片已儲存"（v2.9.8 已移除）
  - ⚠️ DO NOT add photo info button（v2.10.2 已移除，用戶反饋非使用習慣）
  - 參考：`feature_record_Backlog.md` → Photo Capture Feedback

- [ ] **CameraViewModel.kt** - 長寬比例設定
  - ⚠️ 使用 `.collect()` 持續監聽，不要用 `.first()`（v2.10.2 修正）
  - ⚠️ Optical Flow fade-out guard（參考 MEMORY.md）
  - 參考：`feature_record_Backlog.md` → Preview / Resolution / Aspect Ratio

- [ ] **MainActivity.kt** - 權限請求
  - ⚠️ 啟動時一次性請求所有權限（v2.10.2 用戶期望）
  - ⚠️ 根據 Android 版本動態請求檔案權限
  - 參考：CLAUDE.md → 權限規範

- [ ] **LiveCameraSource.kt** - 相機設定
  - ⚠️ ViewPort.setScaleType(FIT) 必須存在（v2.10.0-fix）
  - ⚠️ 1:1 模式使用 ResolutionFilter，不要 fallback 到 4:3
  - 參考：`feature_record_Backlog.md` → Preview / Resolution / Aspect Ratio

### 🔥 High-Risk Operations（需特別小心）

- [ ] **Snackbar / Toast 訊息**
  - 確認不會重複顯示（LaunchedEffect + 持久狀態）
  - 優先使用 UI 視覺反饋（按鈕禁用 + 進度指示器）

- [ ] **權限請求**
  - 確認不會在每次進入畫面時都請求
  - 考慮 Android 版本差異（API 33+ vs 29-32 vs 28-）

- [ ] **長寬比例 / ViewPort**
  - 確認 WYSIWYG（預覽 = 拍攝結果）
  - 測試所有模式（4:3, 16:9, 1:1）

- [ ] **Optical Flow / ML Kit**
  - 確認淡出動畫行為（參考 MEMORY.md 的 Known Pitfall）
  - 不要添加會阻止正常淡出的 guard

---

## 📝 代碼品質檢查

### 變數命名規範
- [ ] 變數名稱**至少 3 個字元**
- [ ] 使用有意義的命名（避免 `a`, `b`, `x`, `temp`）
- [ ] Boolean 變數使用 `is`, `has`, `should` 開頭
- [ ] 常數使用 `UPPER_SNAKE_CASE`

### 字串與顏色
- [ ] 避免硬編碼字串（使用 `strings.xml`）
- [ ] 避免硬編碼顏色（使用 `colors.xml` 或 Material Theme）
- [ ] UI 文字使用繁體中文（除非是技術術語）

### 註解與文檔
- [ ] 關鍵邏輯有註解說明
- [ ] 修正 bug 時註解說明原因和參考
- [ ] 複雜算法有演算法說明
- [ ] Public API 有 KDoc/JavaDoc

### 日誌與除錯
- [ ] 移除 `println()` 或 `System.out.println()`
- [ ] 使用適當的 Log 級別（`Log.d`, `Log.w`, `Log.e`）
- [ ] 移除測試用的 `Log.v` 或過多的 debug log
- [ ] 敏感資訊（密碼、Token）不記錄到 Log

---

## 🧪 測試檢查

### 單元測試（如有修改核心邏輯）
- [ ] 是否需要編寫/更新單元測試？
- [ ] 測試覆蓋率是否下降？
- [ ] 測試命名清楚（使用 backtick 格式）

### 驗測準備
- [ ] 確認修改範圍明確
- [ ] 準備驗證場景（至少 3 個）
- [ ] 確認有回滾方案
- [ ] 紀錄預期行為（Expected vs Actual）

---

## ✅ Commit 準備

### Commit 訊息格式
- [ ] 使用標準格式：`[Type] Short description`
- [ ] Type 選擇正確（Feature, Fix, Refactor, UI, Perf, Docs, Test, Build）
- [ ] 簡短描述清楚（50 字元內）
- [ ] 詳細內容使用 bullet points
- [ ] 包含 Co-Authored-By（如適用）

### Commit 內容檢查
- [ ] 只包含相關的變更（避免混入不相關的修改）
- [ ] 沒有遺留的 TODO 或 FIXME（除非有計劃處理）
- [ ] 沒有註解掉的代碼（應刪除而非註解）
- [ ] 沒有個人測試檔案或暫存檔案

---

## 📚 快速參考

### 重要文檔位置

| 文檔 | 路徑 | 用途 |
|------|------|------|
| **Feature Record (SSOT)** | `devspec/feature_record_Backlog.md` | 功能規範唯一真實來源 |
| **CHANGELOG** | `CHANGELOG.md` | 版本變更歷史 |
| **Version Management** | `docs/VERSION_CLARIFICATION.md` | 版本號管理原則 |
| **Coding Rules** | `devspec/.clinerules` | 代碼規範 |
| **CLAUDE.md** | `.claude/CLAUDE.md` | 專案開發規範 |
| **PR Template** | `devspec/pull_request_template.md` | PR 模板 |
| **Memory (Auto)** | `C:\Users\Administrator\.claude\projects\<project>\memory\MEMORY.md` | Claude Code 自動記憶 |

### 常用指令

```bash
# 搜尋歷史 commit
git log --grep="keyword" --oneline --all

# 查看檔案修改歷史
git log --oneline -- path/to/file.kt

# 搜尋代碼變更
git log -S "code snippet" --oneline

# 查看某行代碼的作者（blame）
git blame path/to/file.kt -L 100,150

# 查看當前變更
git status
git diff

# 查看 staged 變更
git diff --cached
```

---

## 🎯 快速決策樹

```
準備 commit 前
    ├─ 修改了 Critical Files？（CameraScreen, CameraViewModel, MainActivity, LiveCameraSource）
    │   ├─ 是 → 查閱 feature_record_Backlog.md ⚠️ Critical Warning
    │   │       ├─ 有 DO NOT 警告？
    │   │       │   ├─ 是 → 停止！重新思考方案
    │   │       │   └─ 否 → 繼續
    │   │       └─ 搜尋歷史 commit，確認沒有 regression
    │   └─ 否 → 繼續
    │
    ├─ 修改了核心功能？（權限、長寬比例、Snackbar、Optical Flow）
    │   ├─ 是 → 查閱相關測試報告，確認行為一致
    │   └─ 否 → 繼續
    │
    ├─ 代碼品質檢查
    │   ├─ 變數命名 ≥ 3 字元？
    │   ├─ 無硬編碼字串/顏色？
    │   ├─ 註解完整？
    │   └─ 無 debug log？
    │
    ├─ Commit 訊息格式正確？
    │   └─ [Type] Short description
    │
    └─ 執行 git commit
```

---

## 💡 最佳實踐

### 1. 小步提交（Atomic Commits）
- 每個 commit 只做一件事
- 便於 review 和 revert
- 便於理解變更歷史

### 2. 測試驅動（Test Before Commit）
- 修改功能 → 先寫測試 → 再修改代碼
- 修復 bug → 先寫 regression test → 再修復
- 確保修改不破壞現有功能

### 3. 文檔同步（Document As You Go）
- 修改核心功能 → 同時更新 feature_record_Backlog.md
- 修復 regression → 記錄 Regression 歷史
- 添加 Critical Warning → 避免未來重複錯誤

### 4. Code Review 準備
- 自己先 review 一次代碼
- 確保 commit 訊息清楚
- 準備好測試結果和截圖

---

## 📞 需要幫助？

### 遇到問題時

1. **查閱文檔**
   - `feature_record_Backlog.md` - 功能規範
   - `CHANGELOG.md` - 變更歷史
   - `docs/test_results/` - 測試報告

2. **搜尋歷史**
   - `git log --grep="keyword"`
   - `git blame` 找到原作者

3. **詢問團隊**
   - 不確定時，不要假設
   - 尋求 code review 幫助

4. **使用 Claude Code**
   - 詢問 feature_record_Backlog.md 的內容
   - 請求搜尋相關歷史
   - 請求檢查 regression 風險

---

## 版本記錄

- **2026-02-09**: 初版創建（v2.10.2 Regression 預防機制 Phase 1）
- **維護者**: 全體開發者
- **更新頻率**: 發現新的 regression pattern 時更新

---

**記住**：花 5 分鐘檢查，可以避免 5 小時的 debug 和 regression 修復。
