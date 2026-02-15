# 版本號澄清與修正說明

**日期**: 2026-02-09
**最終版本**: ✅ **v2.10.1**

---

## 📋 問題背景

在開發過程中出現了版本號管理混亂的問題：

### 原始狀況

| 項目 | 版本號 | 狀態 |
|------|--------|------|
| `app/build.gradle.kts` | v2.9.9-Day1-2 | ❌ 未更新 |
| Git 分支 | `feature/aspect-ratio-usecasegroup-v2.10.0` | ⚠️ 需澄清 |
| Commit 訊息 | `[Feature] v2.10.0` | ⚠️ 需澄清 |
| Traycer Plan | v2.10.0 | ⚠️ 需澄清 |
| dapper-twirling-frost.md | v2.11.0 → v2.11.1 | ❌ 錯誤 |

### 版本歷史發現

檢查測試記錄發現：

| 版本 | 測試狀態 | 功能範圍 | 測試裝置 |
|------|---------|---------|---------|
| **v2.10.0-fix** | ✅ **已完成** | Aspect Ratio WYSIWYG 修復<br>- Fix 1:1 破壞<br>- Fix 16:9 FOV 過度裁切 | Sony XQ-BT52<br>Google Pixel 9a |
| **當前分支** | 🚧 開發中 | v2.10.0-fix 功能 +<br>- ✨ EXIF Info 顯示<br>- ✨ Settings UI 現代化<br>- ✨ GPS 權限管理 | 待測試 |

### 問題分析

1. **版本歷史衝突**：
   - `v2.10.0-fix` 已經完成測試（包含 Aspect Ratio WYSIWYG 修復）
   - 當前分支包含 v2.10.0-fix 的所有修復 + **新增功能**
   - 但版本號仍標記為 v2.10.0，造成版本重疊

2. **計劃文檔版本號衝突**：
   - `docs/plan/v2.10.0_optimize-app-features_Traycer.md` 標記為 **v2.10.0**
   - `C:\Users\Administrator\.claude\plans\dapper-twirling-frost.md` 標記為 **v2.11.1**
   - **但兩者描述的功能範圍基本相同**

3. **當前分支功能範圍**：
   - ✅ Aspect Ratio WYSIWYG (UseCaseGroup + ViewPort) - 來自 v2.10.0-fix
   - ✅ EXIF 資訊顯示 (PhotoInfoBottomSheet) - ✨ 新增
   - ✅ Settings UI 現代化 (ExpandableSettingsCard) - ✨ 新增
   - ✅ GPS 位置權限處理 (LocationPermissionHandler) - ✨ 新增

4. **版本號管理混淆 - 開發工具 vs APP 功能**：
   - dapper-twirling-frost.md 標記為 v2.11.1，但實際包含**開發工具規劃**（Stage Verification GUI）
   - **錯誤**：開發工具不應該佔用 APP 版本號
   - **正確做法**：APP 版本號只追蹤使用者可見的功能變更

---

## 📌 版本號管理原則（重要）

### APP 版本號 vs 開發工具

| 類別 | 版本號策略 | 範例 | 記錄方式 |
|------|-----------|------|---------|
| **APP 功能** | 使用 `app/build.gradle.kts` 版本號 | v2.10.1: EXIF Info + Settings UI | `app/build.gradle.kts` + Git tag |
| **開發工具** | **不使用 APP 版本號** | Stage Verification GUI | Commit 訊息 + `tools/CHANGELOG.md`（可選）|
| **文檔更新** | **不使用 APP 版本號** | 更新 CLAUDE.md、README | Commit 訊息 |

### ✅ 正確原則

1. **APP 版本號只追蹤使用者可見的功能變更**
   - ✅ 新增 EXIF 資訊顯示 → 遞增版本號（v2.10.0 → v2.10.1）
   - ✅ 修復 Aspect Ratio WYSIWYG → 遞增版本號（v2.9.9 → v2.10.0）
   - ✅ Settings UI 現代化 → 遞增版本號

2. **開發工具變更不佔用 APP 版本號**
   - ✅ 新增 Stage Verification GUI → **不遞增版本號**，記錄在 commit
   - ✅ 新增 Test Handoff GUI → **不遞增版本號**，記錄在 commit
   - ✅ 更新 VSCode Tasks → **不遞增版本號**

3. **開發工具變更的記錄方式**
   - **選項 A**：在 commit 訊息中記錄
     ```bash
     git commit -m "[Tools] Add Stage Verification GUI

     - Create tools/stage_verification_gui.pyw
     - Add 5-phase verification workflow
     - Integrate emulator management"
     ```

   - **選項 B**：創建獨立的 `tools/CHANGELOG.md`
     ```markdown
     # Tools Changelog

     ## 2026-02-09 - Stage Verification GUI
     - Added: stage_verification_gui.pyw (2100+ lines)
     - Added: emulator_manager module
     - Added: VSCode Task integration
     ```

   - **選項 C**：在專案 `CHANGELOG.md` 中分類
     ```markdown
     ## [v2.10.1] - 2026-02-09

     ### APP Features (使用者可見)
     - EXIF Info display
     - Settings UI modernization

     ### Developer Tools (不影響版本號)
     - Stage Verification GUI
     - Updated Test Handoff workflow
     ```

### ❌ 錯誤範例（避免）

```
❌ v2.11.1 = Stage Verification GUI 工具
   → 開發工具不應該佔用 APP 版本號

❌ v2.10.2 = 更新 CLAUDE.md 文檔
   → 文檔更新不應該遞增版本號

❌ v2.10.3 = 新增 VSCode Task
   → 開發基礎設施不應該遞增版本號
```

### 📝 版本號遞增規範

| 版本號變更 | 適用場景 | 範例 |
|-----------|---------|------|
| **Major (+1.0.0)** | 重大架構變更、不向後兼容 | v2.0.0 → v3.0.0：CameraX 重構 |
| **Minor (+0.1.0)** | 新增功能、向後兼容 | v2.10.0 → v2.10.1：EXIF Info |
| **Patch (+0.0.1)** | Bug 修復、不改變功能 | v2.10.1 → v2.10.2：修復 GPS 權限 |

**不遞增版本號**：
- 開發工具變更（Stage Verification GUI、Test Handoff GUI）
- 文檔更新（CLAUDE.md、README.md）
- 測試腳本變更（tools/*.py）
- CI/CD 配置變更（.github/workflows/）

---

## ✅ 修正方案

### 決策：統一為 **v2.10.1**

**理由**：
1. ✅ **v2.10.0-fix 已完成測試** - 版本號 v2.10.0 已經被使用
2. ✅ **當前分支包含新增功能** - 應該遞增 Minor 版本號
3. ✅ **符合版本號管理規範** - v2.10.0 → v2.10.1（新增功能，向後兼容）
4. ✅ **避免版本重疊** - 清楚區分 v2.10.0-fix 和當前開發版本

### 版本號定義

| 版本 | 功能範圍 | 狀態 |
|------|---------|------|
| **v2.10.0-fix** | Aspect Ratio WYSIWYG 修復 | ✅ 已完成測試 |
| **v2.10.1** | v2.10.0-fix + EXIF Info + Settings UI 現代化 + GPS 權限 | 🚧 開發中 |

---

## 🔧 修正步驟

### 1. 修改 APP 版本號 ✅

**檔案**: `app/build.gradle.kts`

```kotlin
// 修正前
versionCode = 60
versionName = "2.9.9-Day1-2"

// 第一次修正（錯誤）
versionCode = 70
versionName = "2.10.0"  // ❌ 與 v2.10.0-fix 重疊

// 最終修正（正確）
versionCode = 71
versionName = "2.10.1"  // ✅ 正確版本號
```

**變更理由**：
- versionCode 遞增 11（從 60 → 71）
- versionName 更新為 v2.10.1，避免與 v2.10.0-fix 重疊

---

### 2. 更新測試交接文檔 ✅

**目錄**: `docs/test_results/v2.10.0/` → `docs/test_results/v2.10.1/`

**檔案**: `docs/test_results/v2.10.1/HANDOVER_SUMMARY.md`

**狀態**: ✅ 已更新為 v2.10.1

**內容摘要**:
```markdown
**Version**: v2.10.1
**Branch**: `feature/aspect-ratio-usecasegroup-v2.10.0`

包含功能：
1. Aspect Ratio WYSIWYG 修正（來自 v2.10.0-fix）
2. UseCaseGroup + ViewPort 整合
3. EXIF 資訊顯示（新增）
4. Settings UI 現代化（新增）
5. GPS 位置權限管理（新增）
```

---

### 3. dapper-twirling-frost.md 版本號說明與錯誤分析

**檔案**: `C:\Users\Administrator\.claude\plans\dapper-twirling-frost.md`

**狀態**: ⚠️ 歷史計劃文檔，版本號標記為 v2.11.1（**錯誤**）

**錯誤原因分析**：

1. **混淆了 APP 功能和開發工具**
   - dapper-twirling-frost.md 計劃包含：
     - ✅ APP 功能：EXIF Info、Settings UI 現代化、GPS 權限（應遞增版本號）
     - ❌ 開發工具：Stage Verification GUI（**不應遞增版本號**）
   - 錯誤：將開發工具也納入版本號規劃

2. **版本號重疊**
   - v2.10.0-fix 已完成測試
   - 計劃標記為 v2.11.1 跳過了 v2.10.1-v2.11.0
   - 實際開發時發現版本號不連續

**正確處理方式**：
- ✅ **APP 功能部分** → 標記為 **v2.10.1**（實際實作版本）
- ✅ **開發工具部分**（Stage Verification GUI）→ **不佔用 APP 版本號**，記錄在 commit 訊息中
- ✅ 保持 plan file 不變（作為歷史記錄和教訓）
- ✅ **以當前 APP 版本號為準：v2.10.1**

**經驗教訓**：
- ⚠️ 計劃階段就要區分「APP 功能」和「開發工具」
- ⚠️ 只有 APP 功能變更才應該規劃版本號
- ⚠️ 開發工具變更應該在獨立的文檔中追蹤（如 `tools/CHANGELOG.md`）

---

## 📊 修正後的版本號一致性

### 最終狀態

| 項目 | 版本號 | 狀態 |
|------|--------|------|
| **APP 版本號** | **v2.10.1** | ✅ 已修正 |
| **Git 分支** | `feature/aspect-ratio-usecasegroup-v2.10.0` | ⚠️ 分支名稱未變（保持歷史） |
| **Commit 訊息** | `[Feature] v2.10.0` | ⚠️ 歷史 commit（不追溯修改） |
| **測試交接文檔** | **v2.10.1** | ✅ 已更新 |
| **測試記錄目錄** | `docs/test_results/v2.10.1/` | ✅ 已重新命名 |

**說明**：
- Git 分支名稱保持 `feature/aspect-ratio-usecasegroup-v2.10.0`（歷史原因，避免強制修改）
- APP 實際版本號為 **v2.10.1**，優先以 `app/build.gradle.kts` 為準

---

## 🔗 版本歷史與測試記錄

### v2.10.0-fix（已完成）

**測試記錄**: `docs/test_results/v2.10.0-fix/`

**功能範圍**：
- ✅ Fix 1:1 WYSIWYG 破壞（Preview square but Capture 3:4）
- ✅ Fix 16:9 FOV 過度裁切（Field of view 30-40% narrower）

**關鍵技術變更**：
- ViewPort.FIT（保留完整 FOV）
- Unified PreviewView.FIT_CENTER（顯示一致性）

**測試裝置**：
- Sony XQ-BT52 ✅
- Google Pixel 9a ✅

**狀態**: ✅ **COMPLETED - All Issues Fixed**

---

### v2.10.1（開發中）

**測試記錄**: `docs/test_results/v2.10.1/`

**功能範圍**：
- ✅ v2.10.0-fix 的所有修復（Aspect Ratio WYSIWYG）
- ✨ **新增**: EXIF 資訊顯示（PhotoInfoBottomSheet）
- ✨ **新增**: Settings UI 現代化（ExpandableSettingsCard）
- ✨ **新增**: GPS 位置權限管理（LocationPermissionHandler）

**測試裝置**：
- 待測試：Google Pixel 9a

**狀態**: 🚧 **Pending Device Testing**

---

## ⚠️ 未來避免版本號錯亂的建議

1. **Git 分支命名與 APP 版本號同步**：
   - 創建分支時確定版本號
   - 分支名稱與 APP versionName 保持一致

2. **APP 版本號同步更新**：
   - 創建分支後立即更新 `app/build.gradle.kts` 的 versionName
   - 避免版本號滯後

3. **檢查版本號重疊**：
   - 查看 `docs/test_results/` 目錄，確認版本號未被使用
   - 查看 Git tags，確認版本號未被標記

4. **計劃文檔版本號驗證**：
   - Level 1/2 計劃完成後，確認版本號與測試記錄不重疊
   - 如有衝突，及時調整版本號

5. **版本號遞增規範**：
   - **Minor 版本遞增**（v2.10.0 → v2.10.1）：新增功能，向後兼容
   - **Patch 版本遞增**（v2.10.1 → v2.10.2）：Bug 修復，不改變功能

6. **區分 APP 功能與開發工具** ⭐ **最重要**：
   - **APP 功能變更** → 遞增版本號，記錄在 `app/build.gradle.kts`
   - **開發工具變更** → **不遞增版本號**，記錄在 commit 訊息或 `tools/CHANGELOG.md`
   - **計劃階段即區分**：Level 1 計劃時就要標註哪些是 APP 功能，哪些是開發工具
   - **範例**：
     ```markdown
     ## Level 1 計劃摘要
     **Version**: v2.10.1 (僅 APP 功能)

     ### APP 功能（遞增版本號）
     - EXIF 資訊顯示
     - Settings UI 現代化

     ### 開發工具（不遞增版本號，另外記錄）
     - Stage Verification GUI
     - Test Handoff workflow 改進
     ```

---

## 🎯 快速參考：版本號管理決策樹

```
收到變更需求
    ├─ 是否為 APP 功能？（使用者可見）
    │   ├─ 是 → 規劃版本號（v2.10.x）
    │   │       ├─ 新增功能 → Minor +1（v2.10.0 → v2.10.1）
    │   │       ├─ Bug 修復 → Patch +1（v2.10.1 → v2.10.2）
    │   │       └─ 重大變更 → Major +1（v2.0.0 → v3.0.0）
    │   │
    │   └─ 否 → 不遞增版本號
    │           ├─ 開發工具 → commit 訊息記錄 "[Tools] ..."
    │           ├─ 文檔更新 → commit 訊息記錄 "[Docs] ..."
    │           └─ 測試腳本 → commit 訊息記錄 "[Test] ..."
    │
    └─ 更新 app/build.gradle.kts（僅 APP 功能）
```

**判斷標準**：
- ✅ **使用者可見的功能變更** → 遞增版本號
- ❌ **開發者工具、文檔、測試** → 不遞增版本號

---

## 📚 相關文檔

- **測試交接摘要**: `docs/test_results/v2.10.1/HANDOVER_SUMMARY.md` (SSOT)
- **v2.10.0-fix 測試報告**: `docs/test_results/v2.10.0-fix/HANDOVER_SUMMARY.md`
- **Traycer 計劃**: `docs/plan/v2.10.0_optimize-app-features_Traycer.md`
- **歷史計劃** (版本號參考用): `C:\Users\Administrator\.claude\plans\dapper-twirling-frost.md`

---

**SSOT (Single Source of Truth)**: 此文檔為 v2.10.1 版本號修正的唯一真實來源。
