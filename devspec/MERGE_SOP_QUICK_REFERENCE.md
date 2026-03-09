# Git Merge 快速參考指南

> **目的**: 快速參考分支合併流程，確保不遺漏關鍵步驟
> **完整文檔**: `devspec/MERGE_SOP.md`

---

## ⚡ 快速流程（5 步驟）

```
1️⃣ 確認來源     → git fetch origin --tags
                → 檢查 BRANCH_RELEASES.md
                → 確認使用 "-final" tag

2️⃣ 文檔同步     → 執行 DOCS_SYNC_CHECKLIST.md
                → git checkout <tag> -- docs/features/<version>/
                → git checkout <tag> -- docs/test_results/<version>/

3️⃣ 代碼合併     → git merge <tag> --no-edit
                → 解決衝突（參考 feature_record_Backlog.md）

4️⃣ 驗證        → python tools/check_merge_readiness.py
                → ./gradlew assembleEmulatorDebug
                → bash docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh <apk>

5️⃣ 記錄        → 更新 BRANCH_RELEASES.md
                → git commit -m "[Merge] ..."
```

---

## 🔍 執行前檢查（必讀）

> ⚠️ **前提條件**：`docs/test_results/<version>/HANDOVER_SUMMARY.md` 必須存在且結果為 **PASS**。
> 測試結果透過雙機交接工作流產出，參考：`devspec/99-test_handoff.md`

### Step 1: 檢查來源分支

```bash
# 1. 更新遠端信息
git fetch origin --tags

# 2. 查看 BRANCH_RELEASES.md
cat devspec/BRANCH_RELEASES.md | grep -A 20 "<版本號>"

# 3. 確認 tag 指向正確的 commit
git log <tag> --oneline -5
```

**必須確認**:
- [ ] 使用 `-final` tag（最終修正版）
- [ ] 狀態為「✅ 穩定，可安全合併」
- [ ] 已閱讀「已知問題」
- [ ] 已閱讀「關鍵提交」清單

---

## 📄 文檔同步（Phase 2）

### 自動檢查

```bash
# 執行智能檢查工具
python tools/check_merge_readiness.py
```

**預期輸出**: `✅ READY TO MERGE`

### 手動同步（如檢查失敗）

```bash
# 同步功能文檔
git checkout <tag> -- docs/features/v<版本>/

# 同步測試報告
git checkout <tag> -- docs/test_results/v<版本>/

# 提交同步
git add docs/features/ docs/test_results/
git commit -m "[Docs] Sync v<版本> documentation"
```

---

## 🔄 代碼合併（Phase 3）

### 執行合併

```bash
# 方法 1: 使用 tag（推薦）
git merge v<版本>-final --no-edit

# 方法 2: 使用遠端分支
git merge origin/<branch-name> --no-edit
```

### 解決衝突（如有）

**常見衝突檔案**:
1. `CameraScreen.kt` - 保留兩邊的功能，整合 UI
2. `feature_record_Backlog.md` - 保留兩邊的決議，按時間排序

**衝突解決策略**:
```bash
# 1. 查看衝突檔案
git status

# 2. 閱讀決議文檔（Critical Warnings）
grep -A 20 "Critical Warning" devspec/feature_record_Backlog.md

# 3. 手動編輯衝突檔案
# 4. 標記為已解決
git add <衝突檔案>

# 5. 完成合併
git commit --no-edit
```

---

## ✅ 驗證（Phase 4）

### 最小驗證（必須）

```bash
# 1. Smart Check
python tools/check_merge_readiness.py

# 2. Clean Build
./gradlew clean
./gradlew assembleEmulatorDebug

# 3. 16KB 對齊檢查
bash docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh \
  app/build/outputs/apk/emulator/debug/app-emulator-debug.apk
```

**必須通過**:
- [ ] Smart Check: ✅ READY TO MERGE
- [ ] Build 成功
- [ ] 16KB: `ELF Verification Successful`

### 完整驗證（推薦）

參考 `devspec/stage_verification_checklist.md` Phase 1-5

---

## 📝 記錄（Phase 5）

### 更新 BRANCH_RELEASES.md

```bash
# 編輯 devspec/BRANCH_RELEASES.md
# 在「合併到」區塊加入合併記錄

**合併到**:
- ✅ Merged: `feature/<target-branch>` → v<版本> (YYYY-MM-DD, commit <hash>)
```

### 提交合併記錄

```bash
git add devspec/BRANCH_RELEASES.md
git commit -m "[Merge] v<來源版本> into v<目標版本>: <簡短說明>

詳細變更:
- 功能 1
- 功能 2
- Bug 修復 1

參考: devspec/MERGE_SOP.md

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## ⚠️ 常見錯誤與預防

### ❌ 錯誤 1: 合併了本地分支（過時版本）

```bash
# ❌ 錯誤做法
git merge feature/branch-name

# ✅ 正確做法
git fetch origin --tags
git merge origin/feature/branch-name
# 或
git merge v<版本>-final
```

**預防**: 總是使用 `origin/<branch>` 或 `tag`

---

### ❌ 錯誤 2: 跳過文檔同步（Phase 2）

**症狀**: 代碼已合併，但文檔遺失

**預防**: 執行 `python tools/check_merge_readiness.py`

---

### ❌ 錯誤 3: 衝突解決時刪除了關鍵修復

**症狀**: 重新引入已修復的 Bug

**預防**:
1. 閱讀 `feature_record_Backlog.md` 的 Critical Warnings
2. 檢查 `BRANCH_RELEASES.md` 的「關鍵提交」清單
3. 保留最新的修復代碼

---

## 🆘 緊急情況

### 合併後發現錯誤

```bash
# 如果尚未推送，撤銷合併
git reset --hard HEAD~1

# 如果已推送，創建新的 revert commit
git revert -m 1 HEAD
```

### Smart Check 誤報

```bash
# 使用 --bypass 繞過檢查（風險自負）
python tools/check_merge_readiness.py --bypass
```

⚠️ **警告**: `--bypass` 應僅用於緊急修復

---

## 📖 相關文檔

- **完整流程**: `devspec/MERGE_SOP.md`
- **文檔同步**: `devspec/DOCS_SYNC_CHECKLIST.md`
- **驗證流程**: `devspec/stage_verification_checklist.md`
- **分支記錄**: `devspec/BRANCH_RELEASES.md`
- **決議文檔**: `devspec/feature_record_Backlog.md`
- **Git 規範**: `.claude/CLAUDE.md` (Git Merge 規範)
- **測試交接流程**: `devspec/99-test_handoff.md` (Home NB ↔ Work NB 四步驟 SOP)

---

## 💡 最佳實踐

1. **合併前**: 總是執行 `python tools/check_merge_readiness.py`
2. **來源確認**: 總是使用 `origin/<branch>` 或 `-final` tag
3. **文檔優先**: 先同步文檔，再合併代碼
4. **衝突謹慎**: 參考決議文檔，保留關鍵修復
5. **驗證完整**: Build + 16KB + Testing 全部通過
6. **記錄清晰**: 更新 BRANCH_RELEASES.md 記錄合併資訊

---

**最後更新**: 2026-02-15
**維護者**: Claude Sonnet 4.5 + Akira Chang
