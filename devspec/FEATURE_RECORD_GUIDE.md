# Feature Record 使用指南

> **目的**: 說明如何使用 `feature_record_Backlog.md` 作為專案的功能規範 SSOT（Single Source of Truth）
> **對象**: 全體開發者、審查者、新成員
> **最後更新**: 2026-02-09

---

## 📚 什麼是 Feature Record？

`devspec/feature_record_Backlog.md` 是專案的**功能規範唯一真實來源（SSOT）**，記錄：

1. **✅ Confirmed** - 已測試驗證的功能行為
2. **🔍 Code Locations** - 關鍵代碼位置
3. **⚠️ Critical Warning** - 不應該做的事情（預防 Regression）
4. **📋 Backlog** - 待完成或規劃中的功能

**為什麼重要**：
- 避免 Regression（如 v2.9.8 Toast 訊息修正 → v2.10.1 regression → v2.10.2 再修正）
- 統一功能理解（不需要問「這個為什麼這樣實作」）
- 快速查閱代碼位置（行號索引）
- 記錄設計決策（為什麼不採用其他方案）

---

## 🎯 使用場景

### 場景 1：開發新功能前

**步驟**：
1. 查閱 feature_record_Backlog.md，確認是否已有相關功能記錄
2. 閱讀 Confirmed 區段，理解現有行為
3. 檢查 ⚠️ Critical Warning，避免違反已驗證的行為
4. 查看 Backlog，確認是否有計劃中的改進

**範例**：
```markdown
# 任務：添加照片保存後的通知功能

## Step 1: 查閱 feature_record_Backlog.md
搜尋關鍵字：「照片」、「保存」、「Toast」、「Snackbar」

## Step 2: 發現相關記錄
→ Photo Capture Feedback (v2.9.8 → v2.10.2)

### ⚠️ Critical Warning
**DO NOT re-add Snackbar for "照片已儲存"**
- v2.9.8 already removed it due to repeated display issue
- Use UI visual feedback instead (button disable + progress indicator)

## Step 3: 決策
❌ 不能使用 Snackbar（違反 Critical Warning）
✅ 應該使用其他方案（UI 視覺反饋、NotificationManager 等）
```

---

### 場景 2：修復 Bug 時

**步驟**：
1. 搜尋相關功能的歷史修正記錄（git log）
2. 查閱 feature_record_Backlog.md 的 Confirmed 行為
3. 確認修復不會違反已驗證的行為
4. 記錄修復原因和 Regression 歷史

**範例**：
```markdown
# Bug：Settings 中切換長寬比例後，預覽畫面沒有更新

## Step 1: 搜尋歷史
git log --grep="aspect ratio\|長寬比例" --oneline --all

## Step 2: 查閱 feature_record_Backlog.md
→ Preview / Resolution / Aspect Ratio (v2.10.0)

### ✅ Confirmed
- 長寬比例 Toggle (4:3/16:9/1:1) — UseCaseGroup + ViewPort 架構

### 🔍 Code Locations
- ViewPort.FIT: `LiveCameraSource.kt:932-940`
- Toggle logic: `CameraViewModel.kt` (aspect ratio state)

## Step 3: 發現問題
CameraViewModel 使用 `.first()` 只讀取一次設定，沒有持續監聽

## Step 4: 修復
改用 `.collect()` 持續監聽 aspectRatioMode 變更

## Step 5: 記錄修復（更新 feature_record_Backlog.md）
添加到 Confirmed:
- Settings 切換即時生效 — `.collect()` 持續監聽 | Verified: 2026-02-09 (v2.10.2)
```

---

### 場景 3：Code Review 時

**步驟**：
1. 查閱 feature_record_Backlog.md 的 ⚠️ Critical Warning
2. 確認 PR 不違反已知的警告
3. 檢查是否有更新文檔（如有重大修正）

**範例**：
```markdown
# PR Review：添加照片資訊按鈕

## Step 1: 查閱 Critical Warning
→ Photo Capture Feedback (v2.9.8 → v2.10.2)

**DO NOT add photo info button**
- User feedback: 「刪除照片資訊按鈕->非用戶使用習慣」| 2026-02-09
- Keep UI minimal - only shutter button with visual feedback

## Step 2: 審查結果
❌ 此 PR 違反 Critical Warning，請求修改
建議：使用長按快門按鈕或在 Settings 中查看 EXIF
```

---

### 場景 4：新成員 Onboarding

**步驟**：
1. 閱讀 feature_record_Backlog.md 的整體結構
2. 理解每個功能的 Confirmed 行為
3. 記住 ⚠️ Critical Warning（最重要）
4. 查看 Backlog 了解未來規劃

**新成員檢查清單**：
- [ ] 理解 SSOT 概念（唯一真實來源）
- [ ] 知道如何查閱 Confirmed 行為
- [ ] 記住 Critical Warning（至少讀過一次）
- [ ] 知道如何搜尋歷史 commit
- [ ] 理解 Regression 預防的重要性

---

## 📖 文檔結構說明

### 標準功能記錄結構

```markdown
## Feature Name (vX.X.X → vY.Y.Y)

### ✅ Confirmed
- 功能行為描述 — 技術細節 | Verified: YYYY-MM-DD (vX.X.X)
  - **Why**: 為什麼這樣做
  - **Alternative solution**: 替代方案
  - **User feedback**: 用戶反饋
  - **Test result**: 測試結果

### 🔍 Code Locations
- 關鍵位置 1: `File.kt:100-150`
- 關鍵位置 2: `File.kt:200`

### ⚠️ Critical Warning
**DO NOT ...**（紅色警告）
- 原因說明
- Regression 歷史
- 正確做法

### 📋 Backlog
- [ ] 待完成功能 — pending 條件
- [x] 已完成功能 ✅ (vX.X.X, YYYY-MM-DD)
```

---

### 各區段說明

#### ✅ Confirmed（已確認行為）

**用途**: 記錄已測試驗證的功能行為，修改前必須有新測試依據

**格式**:
```markdown
- 行為描述 — 技術細節 | Verified: YYYY-MM-DD (vX.X.X)
  - **Why**: 為什麼這樣做
  - **Alternative solution**: 替代方案
  - **User feedback**: 用戶原始反饋
  - **Test result**: 測試結果
```

**範例**:
```markdown
- 照片保存反饋機制 — 移除 Snackbar，改用 UI 視覺反饋 | Verified: 2026-01-30 (v2.9.8)
  - **Why removed**: Snackbar 重複顯示問題（每次進入 CameraScreen 都觸發）
  - **Alternative solution**: 按鈕禁用 + CircularProgressIndicator（60ms 反饋）
  - **User feedback**: 「拍照中/完成皆以快門按鈕動畫為主, 皆以視覺為主」
  - **Test result**: 100% 通過，無用戶抱怨
```

**更新時機**：
- 完成功能開發並測試通過後
- 修復重要 bug 後
- 用戶反饋導致設計變更後

---

#### 🔍 Code Locations（代碼位置）

**用途**: 快速定位關鍵代碼位置，避免全專案搜尋

**格式**:
```markdown
- 功能描述: `File.kt:100-150` (行號範圍或單行)
```

**範例**:
```markdown
- ViewPort.FIT: `LiveCameraSource.kt:932-940`
- Toggle logic: `CameraViewModel.kt` (aspect ratio state)
- 快門反饋: `CameraScreen.kt:151-154` (v2.10.2 移除 Snackbar)
```

**維護提示**：
- 行號變動時需要更新（使用 IDE 的 Find Usages 幫助定位）
- 可以使用版本號標註（如 `v2.10.2` 表示該版本的行號）
- 不需要非常精確，範圍即可

---

#### ⚠️ Critical Warning（關鍵警告）

**用途**: 預防 Regression，記錄「不應該做的事情」

**格式**:
```markdown
**DO NOT ...**（明確的禁止項）
- 原因說明（為什麼不能這樣做）
- Regression 歷史（如有）
- 正確做法（應該怎麼做）
```

**範例**:
```markdown
**DO NOT re-add Snackbar for "照片已儲存"**
- v2.9.8 already removed it due to repeated display issue
- lastCapturedImageUri state persists without clear mechanism
- LaunchedEffect monitoring this state will trigger on every CameraScreen re-entry
- Use UI visual feedback instead (button disable + progress indicator)
- **Regression history**: v2.10.1 (commit d7db1e5) re-added Snackbar → fixed in v2.10.2
```

**何時添加**：
- 發生過 Regression
- 用戶明確反對某個設計
- 技術方案經過驗證不可行
- 有陷阱或容易犯錯的地方

**記住**：Critical Warning 是最重要的區段！修改前必讀！

---

#### 📋 Backlog（待辦事項）

**用途**: 記錄待完成或規劃中的功能

**格式**:
```markdown
- [ ] 待完成功能 — pending 條件 | 詳細: `docs/issues/<issue-file>.md`
- [x] 已完成功能 ✅ (vX.X.X, YYYY-MM-DD)
```

**範例**:
```markdown
- [ ] [9B] maxDistancePerFrame 25f — pending Pixel 9a 實機驗證
- [x] 照片保存 Snackbar 移除 — ✅ 完成 (v2.9.8, 2026-01-30)
- [ ] (Optional) EXIF 資訊訪問方式重新設計（長按快門按鈕或在 Settings 中查看）
```

**狀態標記**：
- `[ ]` - 待完成
- `[x]` - 已完成（移至 Confirmed 或刪除）
- `(Optional)` - 可選，優先級低

---

## 🔧 如何更新 Feature Record

### 新增功能記錄

**步驟**：
1. 在文件底部添加新的功能區段
2. 使用標準結構（Confirmed + Code Locations + Backlog）
3. 記錄測試驗證結果
4. Commit 訊息標註 [Docs]

**範例**:
```markdown
## New Feature Name (vX.X.X)

### ✅ Confirmed
- 功能行為描述 | Verified: YYYY-MM-DD (vX.X.X)
  - **Why**: 原因
  - **Test result**: 測試結果

### 🔍 Code Locations
- 關鍵位置: `File.kt:100`

### 📋 Backlog
- [x] 功能開發 ✅ (vX.X.X, YYYY-MM-DD)
- [ ] 後續改進 — pending 評估
```

---

### 更新已有記錄

**步驟**：
1. 找到相關功能區段
2. 更新 Confirmed 行為（如有變更）
3. 更新 Code Locations（如行號變動）
4. 添加 Critical Warning（如發生 Regression）
5. 移動 Backlog 項目（完成 → Confirmed）

**範例**:
```markdown
# 原記錄
## Photo Capture Feedback (v2.9.8)

### ✅ Confirmed
- 照片保存反饋機制 — 移除 Snackbar | Verified: 2026-01-30 (v2.9.8)

# 更新後（v2.10.2 發生 regression）
## Photo Capture Feedback (v2.9.8 → v2.10.2)

### ✅ Confirmed
- 照片保存反饋機制 — 移除 Snackbar | Verified: 2026-01-30 (v2.9.8)
  - **Final decision**: 保持簡潔 UI | Verified: 2026-02-09 (v2.10.2)

### ⚠️ Critical Warning  ← 新增
**DO NOT re-add Snackbar for "照片已儲存"**
- **Regression history**: v2.10.1 → v2.10.2  ← 記錄 regression
```

---

### 添加 Critical Warning

**何時添加**：
- 發生 Regression 後
- 用戶明確反對某個設計
- 技術驗證不可行的方案

**格式**：
```markdown
### ⚠️ Critical Warning

**DO NOT [明確的禁止項]**
- 原因 1
- 原因 2
- **Regression history**: vA.B.C → vX.Y.Z（如有）
- 正確做法: ...
```

**範例**：
```markdown
**DO NOT add photo info button**
- User feedback: 「刪除照片資訊按鈕->非用戶使用習慣」| 2026-02-09
- Keep UI minimal - only shutter button with visual feedback
- EXIF info access via PhotoInfoBottomSheet (if needed) can be triggered by long-press or Settings
```

---

## 🔍 查閱與搜尋技巧

### 使用 IDE 搜尋

**VSCode**:
```
Ctrl+F → 搜尋 feature_record_Backlog.md 內容
Ctrl+Shift+F → 全專案搜尋（包含文檔）
```

**搜尋關鍵字建議**：
- 功能名稱：`Toast`, `Snackbar`, `Aspect Ratio`, `Permission`
- 警告標記：`DO NOT`, `Critical Warning`, `Regression`
- 檔案名稱：`CameraScreen.kt`, `MainActivity.kt`
- 版本號：`v2.9.8`, `v2.10.2`

---

### 使用 Git 搜尋

**搜尋 commit 訊息**：
```bash
git log --grep="Toast" --oneline --all
git log --grep="Snackbar\|照片已儲存" --oneline --all
```

**搜尋代碼變更**：
```bash
git log -S "showSnackbar" --oneline
```

**查看檔案修改歷史**：
```bash
git log --oneline -- devspec/feature_record_Backlog.md
```

---

### 組合搜尋策略

**情境：我要修改 CameraScreen.kt，想知道有什麼需要注意的**

**Step 1**: 查閱 feature_record_Backlog.md
```
搜尋：CameraScreen
找到：
- Photo Capture Feedback → Critical Warning → DO NOT re-add Snackbar
- Code Locations → 快門反饋: CameraScreen.kt:151-154
```

**Step 2**: 搜尋歷史 commit
```bash
git log --grep="CameraScreen\|Toast\|Snackbar" --oneline --all
```

**Step 3**: 確認行號位置
```kotlin
// 打開 CameraScreen.kt，跳到 Line 151-154
// 確認當前代碼與文檔描述一致
```

---

## 💡 最佳實踐

### 1. 修改前先查閱

**流程**：
```
準備修改代碼
    ↓
查閱 feature_record_Backlog.md
    ↓
確認沒有 Critical Warning
    ↓
搜尋歷史 commit
    ↓
開始修改
```

---

### 2. 完成後立即記錄

**流程**：
```
完成功能開發
    ↓
測試通過
    ↓
更新 feature_record_Backlog.md
    ↓
一起 commit（代碼 + 文檔）
```

**不要**：
- ❌ 等到「有空時」再更新文檔
- ❌ 假設「其他人會更新」
- ❌ 認為「只是小改不用記錄」

---

### 3. 發生 Regression 後必須記錄

**流程**：
```
發現 Regression
    ↓
修復 bug
    ↓
添加 Critical Warning
    ↓
記錄 Regression 歷史
    ↓
提供正確做法
```

**範例**：v2.10.2 Toast 訊息 Regression
- v2.9.8 修正 → v2.10.1 regression → v2.10.2 再修正
- 添加 Critical Warning: **DO NOT re-add Snackbar**
- 記錄原因和正確做法

---

### 4. Code Review 時引用

**審查者**：
```
發現可疑的代碼變更
    ↓
查閱 feature_record_Backlog.md
    ↓
發現違反 Critical Warning
    ↓
請求修改 + 引用文檔說明
```

**Comment 範例**：
```
⚠️ 此修改違反 feature_record_Backlog.md 的 Critical Warning：

**DO NOT re-add Snackbar for "照片已儲存"**
- v2.9.8 already removed it due to repeated display issue
- Regression history: v2.10.1 → v2.10.2

請改用 UI 視覺反饋（按鈕禁用 + 進度指示器）
```

---

## 📚 相關文檔

| 文檔 | 路徑 | 用途 |
|------|------|------|
| **Feature Record (SSOT)** | `devspec/feature_record_Backlog.md` | 功能規範唯一真實來源 |
| **Pre-Commit Checklist** | `devspec/pre_commit_checklist.md` | 提交前檢查清單 |
| **PR Template** | `devspec/pull_request_template.md` | PR 模板（含 Regression Check） |
| **CHANGELOG** | `CHANGELOG.md` | 版本變更歷史 |
| **Version Management** | `docs/VERSION_CLARIFICATION.md` | 版本號管理原則 |
| **Coding Rules** | `devspec/.clinerules` | 代碼規範 |

---

## ❓ 常見問題（FAQ）

### Q1: 什麼樣的修改需要更新 feature_record_Backlog.md？

**A**: 符合以下條件之一：
- 修復重要 bug（影響用戶體驗）
- 新增功能（用戶可見的變更）
- 設計決策變更（用戶反饋導致）
- 發生 Regression（需添加 Critical Warning）
- 行號變動（更新 Code Locations）

**不需要**：
- 純 UI 調整（顏色、間距等）
- 代碼重構（不改變行為）
- 註解更新
- 測試代碼變更

---

### Q2: Critical Warning 太多會不會很亂？

**A**: 不會，Critical Warning 是最重要的預防機制
- 保持簡潔明確（一個警告 2-4 點說明）
- 使用明確的標題（**DO NOT ...**）
- 記錄 Regression 歷史作為教訓
- 定期審查，移除過時的警告

**提示**：如果一個功能有多個 Critical Warning，可能需要重構。

---

### Q3: 行號變動很頻繁，Code Locations 怎麼維護？

**A**: 策略：
- 使用版本號標註（`v2.10.2`）表示該版本的行號
- 使用行號範圍而非精確行號（`100-150` 而非 `105`）
- 優先記錄關鍵位置（不需要記錄每個細節）
- 重大重構後統一更新一次

**範例**：
```markdown
- 快門反饋: `CameraScreen.kt:151-154` (v2.10.2 移除 Snackbar)
- ViewPort.FIT: `LiveCameraSource.kt:932-940`
```

---

### Q4: 新成員應該如何學習 Feature Record？

**A**: 建議流程：
1. **閱讀此指南**（FEATURE_RECORD_GUIDE.md）
2. **完整閱讀一次** feature_record_Backlog.md
3. **記住所有 Critical Warning**（最重要）
4. **實際操作**：修改代碼前先查閱
5. **練習更新**：完成功能後嘗試更新文檔

**檢查清單**：
- [ ] 理解 SSOT 概念
- [ ] 知道如何查閱 Confirmed 行為
- [ ] 記住所有 Critical Warning
- [ ] 知道如何搜尋歷史 commit
- [ ] 能夠獨立更新 Feature Record

---

### Q5: 如果忘記更新文檔怎麼辦？

**A**: Code Review 時會檢查
- PR 模板中有 Regression Check 區段
- 審查者會確認重大修正是否有更新文檔
- 如果遺漏，會在 Review Comments 中要求補充

**補救措施**：
- 立即補充更新文檔
- 創建 follow-up commit: `[Docs] Update feature_record_Backlog.md for <feature>`

---

## 🎓 實戰練習

### 練習 1：查閱 Critical Warning

**任務**: 你要修改 CameraScreen.kt 添加照片保存的提示訊息

**步驟**：
1. 打開 feature_record_Backlog.md
2. 搜尋 "CameraScreen" 或 "照片" 或 "Toast"
3. 找到 Critical Warning 區段
4. 閱讀警告內容
5. 決定是否可以使用 Snackbar

**答案**：
- ❌ 不能使用 Snackbar（違反 Critical Warning）
- ✅ 應該使用 UI 視覺反饋或 NotificationManager

---

### 練習 2：更新文檔

**任務**: 你剛修復了一個 bug - Settings 中切換長寬比例無效

**步驟**：
1. 找到 "Preview / Resolution / Aspect Ratio" 區段
2. 在 Confirmed 區段添加新的驗證記錄
3. 更新 Code Locations（如有變動）
4. 記錄修復方案

**範例答案**：
```markdown
### ✅ Confirmed
- Settings 切換即時生效 — `.collect()` 持續監聽 | Verified: 2026-02-09 (v2.10.2)
  - **Why fixed**: 之前使用 `.first()` 只讀取一次設定
  - **Test result**: Logcat 驗證切換成功（1:1 → 4:3 → 16:9）

### 🔍 Code Locations
- 長寬比例監聽: `CameraViewModel.kt:186-205` (v2.10.2 改用 .collect())
```

---

### 練習 3：添加 Critical Warning

**任務**: 你發現有人又把照片資訊按鈕加回來了，但用戶反饋「非使用習慣」

**步驟**：
1. 修復 bug（移除按鈕）
2. 在 feature_record_Backlog.md 添加 Critical Warning
3. 記錄用戶反饋
4. 提供替代方案

**範例答案**：
```markdown
### ⚠️ Critical Warning

**DO NOT add photo info button**
- User feedback: 「刪除照片資訊按鈕->非用戶使用習慣」| 2026-02-09
- Keep UI minimal - only shutter button with visual feedback
- EXIF info access via PhotoInfoBottomSheet (if needed) can be triggered by long-press or Settings
```

---

## 📞 獲取幫助

### 問題排查流程

```
遇到問題
    ├─ 查閱 feature_record_Backlog.md
    │   ├─ 有相關記錄 → 閱讀 Confirmed 和 Critical Warning
    │   └─ 無相關記錄 → 搜尋歷史 commit
    │
    ├─ 搜尋歷史 commit
    │   ├─ git log --grep="<keyword>"
    │   └─ git log -- path/to/file
    │
    ├─ 查看測試報告
    │   └─ docs/test_results/<version>/
    │
    └─ 仍無法解決
        ├─ 使用 git blame 找到原作者
        ├─ 向團隊成員詢問
        └─ 使用 Claude Code 協助搜尋
```

### 聯絡方式

- **文檔維護者**: 全體開發者
- **問題回報**: 創建 Issue 或直接修改文檔
- **建議改進**: 提交 PR 更新此指南

---

## 版本記錄

- **2026-02-09**: 初版創建（v2.10.2 Regression 預防機制 Phase 1）
- **維護者**: 全體開發者
- **更新頻率**: 發現新的使用模式或問題時更新

---

**記住**：Feature Record 是我們的共享知識庫，每個人都有責任維護它。
