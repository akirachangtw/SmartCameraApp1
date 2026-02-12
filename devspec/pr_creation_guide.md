# Pull Request 創建指引

**日期**: 2026-02-10
**版本**: v2.10.2
**分支**: feature/aspect-ratio-usecasegroup-v2.10.0 → main

---

## 📋 快速步驟

### 步驟 1：開啟 PR 創建頁面

點擊以下連結（或複製到瀏覽器）：

```
https://github.com/akirachangtw/SmartCameraApp1/compare/main...feature/aspect-ratio-usecasegroup-v2.10.0
```

### 步驟 2：填寫 PR Title

在 "Title" 欄位輸入：

```
v2.10.2: Aspect Ratio + EXIF Info + Regression Prevention
```

### 步驟 3：填寫 PR Description

複製以下內容到 "Leave a comment" 欄位：

---

## Summary

This PR includes v2.10.0, v2.10.1, v2.10.2 development and regression prevention mechanisms.

### v2.10.0 Features
- ✅ Aspect ratio WYSIWYG fix (UseCaseGroup + ViewPort implementation)
- ✅ EXIF info support (GPS, Camera settings, Image metadata)
- ✅ Photo info bottom sheet UI
- ✅ Location permission handler
- ✅ Settings UI modernization (Expandable cards)

### v2.10.2 Bug Fixes
- ✅ **Permission UX**: Request all permissions at startup (Camera + Location + Storage)
- ✅ **Aspect Ratio**: Fixed Settings changes not taking effect (`.first()` → `.collect()`)
- ✅ **Toast Regression**: Removed v2.10.1 Snackbar "照片已儲存" (restored v2.9.8 solution)
- ✅ **UI Cleanup**: Removed photo info button (user feedback: non-standard UX)

### Regression Prevention Phase 1
- ✅ **Pre-Commit Checklist** (`devspec/pre_commit_checklist.md`)
- ✅ **PR Template** with Regression Check (`devspec/pull_request_template.md`)
- ✅ **Feature Record Guide** (`devspec/FEATURE_RECORD_GUIDE.md`)
- ✅ **CHANGELOG.md** (version tracking)
- ✅ **VERSION_CLARIFICATION.md** (version number management)

## Changed Files
- **Core**: 11 Kotlin files (MainActivity, CameraScreen, CameraViewModel, etc.)
- **New**: AspectRatioMode enum, PhotoInfoBottomSheet, LocationPermissionHandler
- **Docs**: 30+ documentation files (plans, test reports, handoff logs)
- **Tools**: stage_verification_gui.pyw, test_handoff_gui.pyw updates

## Test Results
- ✅ **Pixel 9a**: 100% PASSED (v2.10.2 full validation)
- ✅ **Sony XQ-BT52**: Cross-device compatibility verified
- ✅ **16KB Compatibility Check**: ELF Verification Successful
- ✅ **Build**: 20s incremental compilation

## Regression Check
- [x] Searched historical fixes: `git log --grep="Snackbar"`, `git log --grep="aspect ratio"`
- [x] Checked `devspec/feature_record_Backlog.md` Critical Warnings
- [x] No duplicate fixes detected

### Critical Files Modified
- [x] **CameraScreen.kt** - Snackbar removed (v2.9.8 solution restored)
- [x] **CameraViewModel.kt** - Aspect ratio monitoring (`.collect()` used)
- [x] **MainActivity.kt** - Permission request logic updated

## Documentation Updates
- [x] `CHANGELOG.md` - v2.10.2 entry added
- [x] `devspec/feature_record_Backlog.md` - Critical Warnings added
- [x] `docs/VERSION_CLARIFICATION.md` - Version management guide created
- [x] Test reports for v2.10.0-fix, v2.10.1, v2.10.2

## Breaking Changes
None. All changes are backward compatible.

## Test Plan
- [x] Emulator testing (API 34)
- [x] Pixel 9a real device testing
- [x] Permission flow validation
- [x] Aspect ratio switching (4:3, 16:9, 1:1)
- [x] Photo capture feedback verification

## Deployment Notes
- Version updated: **v2.10.2** (versionCode: 72)
- No database migrations required
- No API changes

🤖 Generated with [Claude Code](https://claude.com/claude-code)

---

### 步驟 4：設定 PR 選項（可選）

- **Reviewers**: 如有團隊成員，可添加 Reviewers
- **Labels**: 可添加標籤（如 `enhancement`, `bug`, `documentation`）
- **Milestone**: 如有 Milestone 設定，可選擇 v2.10.2

### 步驟 5：創建 PR

點擊綠色按鈕 **"Create pull request"**

---

## 📊 PR 統計資訊

- **Commits**: 24 個
- **Files Changed**: 74 個檔案
- **Insertions**: 約 303,235 行（包含測試報告和 logcat）
- **Deletions**: 約 477 行

---

## 🔍 PR Review 建議

### 重點檢查項目

1. **v2.10.2 Bug Fixes**
   - [MainActivity.kt](../app/src/main/java/com/akirachang/ai/photographer/MainActivity.kt#L30-L60) - 權限請求
   - [CameraViewModel.kt](../app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt#L186-L205) - 長寬比例監聽
   - [CameraScreen.kt](../app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt#L151-L154) - Toast 移除

2. **Regression Prevention Phase 1**
   - [devspec/pre_commit_checklist.md](../devspec/pre_commit_checklist.md)
   - [devspec/pull_request_template.md](../devspec/pull_request_template.md)
   - [devspec/FEATURE_RECORD_GUIDE.md](../devspec/FEATURE_RECORD_GUIDE.md)

3. **Documentation**
   - [CHANGELOG.md](../CHANGELOG.md)
   - [docs/VERSION_CLARIFICATION.md](../docs/VERSION_CLARIFICATION.md)
   - [devspec/feature_record_Backlog.md](../devspec/feature_record_Backlog.md)

### Regression Check 清單

- [ ] 查閱 `devspec/feature_record_Backlog.md` Critical Warnings
- [ ] 確認沒有重新引入 Snackbar "照片已儲存"
- [ ] 確認沒有添加 photo info button
- [ ] 確認使用 `.collect()` 而非 `.first()`
- [ ] 確認啟動時一次性請求所有權限

---

## ✅ PR 創建完成後

### 1. 複製 PR URL

PR 創建後，GitHub 會生成一個 URL，例如：
```
https://github.com/akirachangtw/SmartCameraApp1/pull/123
```

請保存此 URL 以便後續追蹤。

### 2. 通知團隊（如適用）

如有團隊成員，可透過以下方式通知：
- GitHub PR 評論
- Slack/Discord 通知
- Email 通知

### 3. 等待 CI/CD（如有設定）

如專案有設定 GitHub Actions，等待自動測試完成。

### 4. 合併 PR

當 Review 完成且所有檢查通過後，點擊 **"Merge pull request"**。

---

## 🆘 遇到問題？

### 問題 1: 無法看到 "Create pull request" 按鈕

**原因**: 分支可能尚未推送到遠端

**解決方法**:
```bash
git push origin feature/aspect-ratio-usecasegroup-v2.10.0
```

### 問題 2: PR 已存在

**解決方法**: 前往現有的 PR 並更新描述即可

### 問題 3: Merge conflicts

**原因**: main 分支有新的 commits

**解決方法**:
```bash
git checkout feature/aspect-ratio-usecasegroup-v2.10.0
git pull origin main
# 解決衝突
git push origin feature/aspect-ratio-usecasegroup-v2.10.0
```

---

## 📞 相關資源

- **PR Template**: `devspec/pull_request_template.md`
- **Feature Record**: `devspec/feature_record_Backlog.md`
- **Pre-Commit Checklist**: `devspec/pre_commit_checklist.md`
- **Regression Prevention Phase 2**: `tmp/PHASE2_IMPLEMENTATION_SUMMARY.md`

---

**準備者**: Claude Sonnet 4.5
**日期**: 2026-02-10
**版本**: v2.10.2
