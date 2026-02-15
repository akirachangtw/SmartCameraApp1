# Git 分支分析指南 - Home NB 使用

**日期**: 2026-02-13
**目的**: 在 Home NB 上分析 v2.11.0 和 v2.10.2-final 的分支關係，確定正確的合併策略

---

## 🎯 當前狀況摘要

### 問題
- Work NB 測試發現 v2.11.0 功能（Eye-AF, Rectangle UI）遺失
- 對焦定點功能損壞
- 原因：之前的合併使用了 `git checkout --theirs`，完全覆蓋了 v2.11.0 代碼

### 已執行動作
1. ✅ 回退到 v2.11.0 原始狀態 (commit 3aac584)
2. ✅ 創建分支版本管理文檔系統
3. ✅ 推送到遠端 (commit d371f2f)
4. ⏸️ **暫停合併**，等待 Home NB 分析

---

## 📊 分支關係分析

### 關鍵發現

**v2.11.0 和 v2.10.0 是平行開發的兩條分支**

```
共同祖先: 01441aa [Docs] v2.x.x Phase 7: Work NB → Home NB 工作交接
```

### 分支 1: feature/aspect-ratio-usecasegroup-v2.10.0

```bash
# 在 Home NB 上執行
git log origin/feature/aspect-ratio-usecasegroup-v2.10.0 --oneline -20
```

**重要提交**：
- `0440242` - v2.10.0 功能完成
- `6501ac2` - v2.10.2 修復（Permission UX + Aspect Ratio + Toast Regression）
- `189cc85` - v2.10.2-final (tag)

### 分支 2: feature/camera-core-v2.11.0

```bash
# 在 Home NB 上執行
git log origin/feature/camera-core-v2.11.0 --oneline -10
```

**重要提交**：
- `3aac584` - v2.11.0 核心功能（Eye-AF + Smart Subject Focus + Rectangle UI）
- `d371f2f` - 分支版本管理系統（當前位置）

---

## 🔍 Home NB 上需要分析的事項

### 1. 確認分支分岔點

```bash
git merge-base feature/camera-core-v2.11.0 origin/feature/aspect-ratio-usecasegroup-v2.10.0
# 預期輸出: 01441aa
```

### 2. 檢視 v2.10.2 修復的具體內容

```bash
git show 6501ac2 --stat
```

**修復的檔案**：
- `MainActivity.kt` - 權限請求 UX 改善
- `CameraScreen.kt` - Toast Regression 修正
- `CameraViewModel.kt` - Aspect Ratio 切換立即生效
- `feature_record_Backlog.md` - Critical Warnings

### 3. 檢視 v2.11.0 的具體內容

```bash
git show 3aac584 --stat
```

**新增的檔案**：
- 檢查 `CameraScreen.kt` 是否有 Rectangle UI 代碼
- 檢查 `CameraViewModel.kt` 是否有 Eye-AF 邏輯

### 4. 比較兩個分支的 CameraScreen.kt

```bash
# 提取兩個版本到臨時目錄
git show 3aac584:app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt > /tmp/v2.11.0_CameraScreen.kt
git show v2.10.2-final:app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt > /tmp/v2.10.2_CameraScreen.kt

# 使用 diff 工具比較
diff /tmp/v2.11.0_CameraScreen.kt /tmp/v2.10.2_CameraScreen.kt | head -100
```

### 5. 檢查衝突檔案

```bash
# 模擬合併（不實際執行）
git merge v2.10.2-final --no-commit --no-ff

# 查看會有哪些衝突
git status

# 取消模擬合併
git merge --abort
```

---

## 📋 需要決策的問題

### 問題 1: CameraScreen.kt 衝突如何解決？

**v2.11.0 的內容**：
- Rectangle Focus Indicator
- Corner Brackets
- Eye-AF tracking
- Neon Glow effects

**v2.10.2 的內容**：
- StiffnessHigh (900f) 優化
- Alpha 動畫順序修正（isLocked → isFading → isTracking）
- Toast Regression 修正

**合併策略**：
- [ ] 選項 A: 保留 v2.11.0 的 UI 代碼 + 手動應用 v2.10.2 的修復
- [ ] 選項 B: 使用 v2.10.2 的代碼 + 手動添加 v2.11.0 的 UI
- [ ] 選項 C: 逐行比對，手動合併所有差異

### 問題 2: 其他衝突檔案如何處理？

預期會衝突的檔案：
- `app/build.gradle.kts` - 版本號（保留 v2.11.0: 73, "2.11.0"）
- `devspec/feature_record_Backlog.md` - 功能記錄（合併兩邊內容）

---

## 🎯 建議的合併策略（待 Home NB 確認）

### 階段 1: 文檔同步（已完成）
- ✅ 已創建 BRANCH_RELEASES.md
- ✅ 已創建 MERGE_SOP.md
- ✅ 已創建 DOCS_SYNC_CHECKLIST.md

### 階段 2: 代碼合併（待執行）

#### 2.1 同步 v2.10.0 的新檔案
直接採用（無衝突）：
- `ExifInfo.kt` - 新增
- `PhotoInfoBottomSheet.kt` - 新增
- `AspectRatioMode.kt` - 新增
- `LocationPermissionHandler.kt` - 新增
- `ExpandableSettingsCard.kt` - 新增

#### 2.2 手動合併 CameraScreen.kt

**策略 A（推薦）**：
1. 使用 v2.11.0 的 `FocusIndicator` composable（完整保留 Rectangle UI）
2. 在 `animatedX/Y` 的 Spring 動畫中應用 v2.10.2 的 `StiffnessHigh`
3. 在 `alpha` 動畫中應用 v2.10.2 的優先順序順序
4. 保留 v2.10.2 的 PhotoInfoBottomSheet 相關代碼

#### 2.3 其他檔案

**MainActivity.kt**：
- 採用 v2.10.2-final（權限請求改善）

**CameraViewModel.kt**：
- 合併兩邊的修改
- v2.11.0: Eye-AF tracking 邏輯
- v2.10.2: Aspect Ratio 監聽修復

**SettingsScreen.kt**：
- 採用 v2.10.2-final（Aspect Ratio toggle）

---

## 🚀 Home NB 上的後續步驟

### 步驟 1: 分析 Git 歷史

```bash
# 切換到項目目錄
cd d:\MyWorking\DevSources\AndroidStudioProjects\SmartCameraApp1

# 更新遠端信息
git fetch origin --tags

# 查看分支關係圖
git log --oneline --graph --all -30
```

### 步驟 2: 比對關鍵檔案

使用 VSCode 或 Beyond Compare 比對：
- `CameraScreen.kt` (v2.11.0 vs v2.10.2-final)
- `CameraViewModel.kt` (v2.11.0 vs v2.10.2-final)
- `feature_record_Backlog.md` (v2.11.0 vs v2.10.2-final)

### 步驟 3: 決定合併策略

基於分析結果，確定：
1. CameraScreen.kt 的合併方式（選項 A/B/C）
2. 其他衝突檔案的處理方式
3. 是否需要建立合併輔助腳本

### 步驟 4: 回報 Work NB

將分析結果和決策回報給 Work NB，由 Work NB 執行合併。

---

## 📝 重要提醒

1. **不要在 Home NB 上執行合併**
   - 主要開發在 Home NB，但 Work NB 已經建立了詳細的 MERGE_SOP
   - 應該在 Work NB 執行合併，Home NB 負責分析和決策

2. **檢查文檔系統**
   - 確認 `devspec/BRANCH_RELEASES.md` 內容正確
   - 確認 `devspec/MERGE_SOP.md` 流程可行
   - 確認 `devspec/DOCS_SYNC_CHECKLIST.md` 完整

3. **版本號管理**
   - v2.11.0 應該保留 versionCode 73
   - 最終版本名稱應為 "2.11.0"（包含 v2.10.2 + v2.11.0）

---

## 📞 需要 Work NB 回答的問題

1. **CameraScreen.kt 合併策略**：選擇 A/B/C？
2. **是否需要額外的安全措施**？（例如：建立測試分支先實驗）
3. **合併後的測試範圍**？（哪些功能必須驗證）

---

**最後更新**: 2026-02-13 (Work NB)
**下次更新**: Home NB 分析後回報
