# 分支合併標準作業程序 (SOP)

> **目的**: 提供完整的分支合併流程，防止合併錯誤的分支或遺漏重要修復
> **適用場景**: 所有分支合併操作（feature → feature, feature → develop, develop → main）

---

## 🎯 快速決策樹

```
需要合併分支？
  ↓
步驟 1: 確認來源分支
  ├─ 檢查 devspec/BRANCH_RELEASES.md
  ├─ 確認使用 "-final" tag（最終修正版）
  └─ 使用 origin/<branch> 而非本地分支
  ↓
步驟 2: 文檔同步
  ├─ 執行 devspec/DOCS_SYNC_CHECKLIST.md
  └─ 保留所有決議和測試報告
  ↓
步驟 3: 代碼合併
  ├─ 解決衝突（優先保留最新修復）
  └─ 檢查 feature_record_Backlog.md 的 Critical Warnings
  ↓
步驟 4: 驗證
  ├─ Phase 1-3: Build + 16KB
  └─ Phase 4-5: Testing
  ↓
步驟 5: 記錄
  └─ 更新 BRANCH_RELEASES.md
```

---

## 📋 完整流程：v2.10.2-final → v2.11.0

### Phase 1: 準備階段 (15 分鐘)

#### 1.1 確認來源分支的最終版本

```bash
# 1. 更新遠端信息和標籤
git fetch origin --tags

# 2. 查看所有相關 tags
git tag -l "v2.10*"

# 3. 查看最終版本的詳細信息
git show v2.10.2-final

# 4. 確認 tag 指向的 commit
git log v2.10.2-final --oneline -5
```

**檢查點**:
- [ ] Tag 存在且名稱正確（`v2.10.2-final`）
- [ ] Tag 指向遠端分支（`origin/feature/aspect-ratio-usecasegroup-v2.10.0`）
- [ ] Tag 包含關鍵修復（如 `6501ac2` for v2.10.2 Bug fixes）

#### 1.2 檢查分支版本記錄

```bash
# 閱讀分支版本記錄
cat devspec/BRANCH_RELEASES.md
```

**確認**:
- [ ] v2.10.2-final 的狀態為「✅ 穩定，可安全合併」
- [ ] 已閱讀「包含功能」清單
- [ ] 已閱讀「已知問題」（應為「無」）
- [ ] 已閱讀「關鍵提交」清單

#### 1.3 閱讀決議文檔

```bash
# 閱讀 feature_record_Backlog.md 的相關章節
# - Preview / Resolution / Aspect Ratio (v2.10.0)
# - Photo Capture Feedback (v2.9.8 → v2.10.2)

# 特別注意 Critical Warnings
grep -A 20 "Critical Warning" devspec/feature_record_Backlog.md
```

**確認**:
- [ ] 已閱讀 v2.10.0 的 Aspect Ratio 功能說明
- [ ] 已閱讀 v2.10.2 的 Photo Capture Feedback 修復
- [ ] ⚠️ **已理解** "DO NOT re-add Snackbar" 警告
- [ ] ⚠️ **已理解** "DO NOT add photo info button" 警告

#### 1.4 閱讀測試報告

```bash
# 閱讀 v2.10.2 的測試報告
cat docs/test_results/v2.10.2/PIXEL9A_TEST_REPORT.md
```

**確認**:
- [ ] 所有測試通過（100% PASSED）
- [ ] 無阻塞性 Bug
- [ ] 實機測試已完成（Pixel 9a, Sony XQ-BT52）

---

### Phase 2: 文檔同步 (20 分鐘)

#### 2.1 執行文檔同步檢查清單

```bash
# 參考文檔同步清單
cat devspec/DOCS_SYNC_CHECKLIST.md
```

#### 2.2 同步 devspec/ 文檔

```bash
# 注意：feature_record_Backlog.md 需要在 merge 後手動合併
# 其他文檔檢查是否有更新
git diff HEAD v2.10.2-final -- devspec/
```

#### 2.3 同步 docs/plan/ 文檔

```bash
# 複製 v2.10.0 相關的計劃文檔
git checkout v2.10.2-final -- docs/plan/v2.10.0_*
git checkout v2.10.2-final -- docs/plan/V2.10.0_*

# 確認文檔已複製
ls docs/plan/v2.10.0_* docs/plan/V2.10.0_*
```

**檢查點**:
- [ ] 所有 v2.10.0 相關計劃文檔已複製
- [ ] 無遺漏的決議文檔

#### 2.4 同步 docs/test_results/ 文檔

```bash
# 複製測試報告
git checkout v2.10.2-final -- docs/test_results/v2.10.0/
git checkout v2.10.2-final -- docs/test_results/v2.10.0-fix/
git checkout v2.10.2-final -- docs/test_results/v2.10.2/

# 確認測試報告已複製
ls docs/test_results/v2.10.0/
ls docs/test_results/v2.10.2/
```

**檢查點**:
- [ ] v2.10.0 測試報告已複製
- [ ] v2.10.2 測試報告已複製
- [ ] Logcat 日誌已複製

#### 2.5 創建文檔同步記錄

```bash
# 創建同步記錄
cat > devspec/DOCS_SYNC_LOG_$(date +%Y%m%d_%H%M%S).md <<EOF
# 文檔同步記錄

- **日期**: $(date '+%Y-%m-%d %H:%M:%S')
- **來源**: v2.10.2-final
- **目標**: feature/camera-core-v2.11.0
- **執行者**: [Your Name]

## 已同步文檔
- docs/plan/v2.10.0_* (全部)
- docs/test_results/v2.10.0/ (全部)
- docs/test_results/v2.10.2/ (全部)

## 待合併文檔（merge 時處理）
- devspec/feature_record_Backlog.md

## 驗證
- [ ] 所有計劃文檔已複製
- [ ] 所有測試報告已複製
- [ ] 無遺漏的決議文檔
EOF

# 提交文檔同步
git add docs/plan/ docs/test_results/ devspec/DOCS_SYNC_LOG_*
git commit -m "[Docs] Sync v2.10.2-final documentation to v2.11.0

同步 v2.10.0/v2.10.2 的決議文檔和測試報告

- docs/plan/v2.10.0_* (全部計劃文檔)
- docs/test_results/v2.10.0/ (測試報告)
- docs/test_results/v2.10.2/ (測試報告 + Logcat)

參考: devspec/DOCS_SYNC_CHECKLIST.md"
```

---

### Phase 3: 代碼合併 (30 分鐘)

#### 3.1 撤銷錯誤的合併（如果有）

```bash
# 檢查當前狀態
git log --oneline -5

# 如果有錯誤的合併（合併了本地分支而非遠端分支），撤銷
git reset --hard HEAD~1

# 確認已撤銷
git log --oneline -5
```

**檢查點**:
- [ ] 錯誤的合併已撤銷
- [ ] 當前分支狀態正確

#### 3.2 更新遠端信息

```bash
# 更新所有遠端分支的信息
git fetch origin --tags

# 確認遠端分支最新
git log origin/feature/aspect-ratio-usecasegroup-v2.10.0 --oneline -10
```

**檢查點**:
- [ ] 遠端分支包含 `6501ac2` (v2.10.2 修復)
- [ ] 遠端分支包含 `d7db1e5` (v2.10.0 EXIF)

#### 3.3 執行合併

```bash
# 方法 1: 使用 tag（推薦）
git merge v2.10.2-final --no-edit

# 方法 2: 使用遠端分支
# git merge origin/feature/aspect-ratio-usecasegroup-v2.10.0 --no-edit
```

**如果有衝突**，參考下一步。

#### 3.4 解決衝突

**常見衝突檔案**:
1. `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`
2. `devspec/feature_record_Backlog.md`

**解決策略**:

**CameraScreen.kt**:
```bash
# 檢查衝突內容
git diff CameraScreen.kt

# 策略：保留 StiffnessHigh（v2.10.2 的優化）
# 使用 v2.10.2 的版本
git checkout --theirs app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt
git add app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt
```

**feature_record_Backlog.md**:
```bash
# 策略：保留兩邊的內容（v2.11.0 + v2.10.2）
# 使用當前分支（v2.11.0）的版本（包含所有章節）
git checkout --ours devspec/feature_record_Backlog.md
git add devspec/feature_record_Backlog.md
```

**檢查點**:
- [ ] 所有衝突已解決
- [ ] StiffnessHigh 已保留（CameraScreen.kt）
- [ ] feature_record_Backlog.md 包含所有章節

#### 3.5 完成合併

```bash
# 提交合併
git commit -m "$(cat <<'EOF'
Merge tag 'v2.10.2-final' into feature/camera-core-v2.11.0

整合 v2.10.0 + v2.10.2 的完整功能和修復到 v2.11.0

## 整合內容

### v2.10.0 功能
- ✅ Aspect Ratio Toggle (4:3/16:9/1:1) + UseCaseGroup + ViewPort
- ✅ EXIF 資訊顯示 (GPS, 曝光參數, 裝置資訊)
- ✅ Settings UI 現代化 (可摺疊卡片, 位置權限管理)

### v2.10.2 修復
- ✅ Permission UX 改善（啟動時一次性請求）
- ✅ Aspect Ratio 切換立即生效修復
- ✅ Toast Regression 修正（移除 Snackbar）

### v2.11.0 功能（保留）
- ✅ Eye-AF (眼睛優先自動對焦)
- ✅ Smart Subject Focus (智能主體對焦)
- ✅ Rectangle + Corner Brackets 視覺設計

## 衝突解決
1. CameraScreen.kt: 採用 StiffnessHigh (v2.10.2 優化)
   - 理由: v2.11.0 的 Eye-AF 需要快速響應
2. feature_record_Backlog.md: 保留所有章節
   - 合併 v2.10.0/v2.10.2 + v2.11.0 的完整記錄

## 文檔同步
- ✅ docs/plan/v2.10.0_* (決議文檔)
- ✅ docs/test_results/v2.10.0/ (測試報告)
- ✅ docs/test_results/v2.10.2/ (測試報告)

來源: v2.10.2-final (tag)
參考: devspec/MERGE_SOP.md

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

---

### Phase 4: 驗證 (45 分鐘)

#### 4.1 Phase 2: Build 驗證

```bash
# Clean build
./gradlew clean

# Build Device APK
./gradlew assembleDeviceDebug
```

**檢查點**:
- [ ] Build 成功
- [ ] 無 Build ERROR
- [ ] Build 時間: ______ 秒

#### 4.2 Phase 3: 16KB Compatibility

```bash
# 執行 16KB 對齊檢查
bash docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh \
  app/build/outputs/apk/device/debug/app-device-debug.apk
```

**檢查點**:
- [ ] `ELF Verification Successful`

#### 4.3 Phase 5: Device Testing

```bash
# 卸載舊版本
adb -s 59131JEBF17244 uninstall com.akirachang.ai.photographer

# 安裝新版本
adb -s 59131JEBF17244 install app/build/outputs/apk/device/debug/app-device-debug.apk

# 驗證版本號
adb -s 59131JEBF17244 shell dumpsys package com.akirachang.ai.photographer | grep version
```

**功能驗證**:
- [ ] **v2.11.0 功能**: Eye-AF, Smart Subject Focus, Rectangle UI
- [ ] **v2.10.0 功能**: Aspect Ratio Toggle (4:3/16:9/1:1) ⭐ **重要**
- [ ] **v2.10.2 修復**: Permission UX, Toast (無 Snackbar)

---

### Phase 5: 記錄 (10 分鐘)

#### 5.1 更新分支版本記錄

編輯 `devspec/BRANCH_RELEASES.md`：

```markdown
### v2.11.0 (2026-02-13) ✅ 穩定
- **Status**: ✅ 穩定，包含 v2.10.2-final 的所有功能
- **已整合**: v2.10.2-final (2026-02-13)
- 測試狀態:
  - ✅ Pixel 9a: PASS
```

#### 5.2 推送到遠端

```bash
# 推送分支
git push origin feature/camera-core-v2.11.0

# 推送 tag（如果創建了新 tag）
git push origin --tags
```

---

## ⚠️ 重要提醒

### 永遠使用遠端分支

```bash
# ✅ 正確
git merge origin/feature-name
git merge v2.10.2-final  # 使用 tag

# ❌ 錯誤
git merge feature-name  # 可能是舊的本地分支
```

### 檢查 Critical Warnings

合併前**必須閱讀** `devspec/feature_record_Backlog.md` 的所有 Critical Warnings。

### 保留測試報告

所有測試報告都是功能驗證的證據，必須完整保留。

---

**最後更新**: 2026-02-13
**維護者**: Claude Sonnet 4.5 + Akira Chang
