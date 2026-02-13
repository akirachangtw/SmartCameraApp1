# 分支發布記錄

> **目的**: 記錄各功能分支的最終修正版，確保合併時使用正確的版本
> **更新頻率**: 每次分支達到穩定狀態時更新

---

## v2.10.0 系列

### v2.10.2-final (2026-02-09) ✅ 最終修正版
- **Tag**: `v2.10.2-final`
- **Commit**: `189cc85` (origin/feature/aspect-ratio-usecasegroup-v2.10.0)
- **Branch**: `feature/aspect-ratio-usecasegroup-v2.10.0`
- **Status**: ✅ **穩定，可安全合併**

**包含功能**:
- ✅ Aspect Ratio Toggle (4:3/16:9/1:1) + UseCaseGroup + ViewPort
- ✅ EXIF 資訊顯示 (GPS, 曝光參數, 裝置資訊)
- ✅ Settings UI 現代化 (可摺疊卡片, 位置權限管理)
- ✅ v2.10.2 Bug 修復 (Permission UX + Aspect Ratio + Toast Regression)

**關鍵提交**:
- `6501ac2`: [Fix] v2.10.2: Bug fixes - Permission UX + Aspect Ratio + Toast Regression
- `d7db1e5`: [Feature] v2.10.0: Add EXIF info + Photo info sheet + Location permission
- `7948186`: [Feature] v2.10.0: Aspect ratio WYSIWYG fix + cross-device validation
- `9c412ca`: [Feature] v2.10.0: UseCaseGroup + ViewPort aspect ratio implementation

**測試結果**:
- ✅ Pixel 9a: PASS
- ✅ Sony XQ-BT52: PASS
- 測試報告: `docs/test_results/v2.10.2/PIXEL9A_TEST_REPORT.md`

**決議文檔**:
- `devspec/feature_record_Backlog.md` (Preview / Resolution / Aspect Ratio 章節)
- `devspec/feature_record_Backlog.md` (Photo Capture Feedback 章節)
- `docs/test_results/v2.10.2/PIXEL9A_TEST_REPORT.md`
- `docs/test_results/v2.10.0-fix/CROSS_DEVICE_TEST_REPORT.md`

**已知問題**: 無

**合併到**:
- ✅ Merged: `feature/camera-core-v2.11.0` → v2.11.1 (2026-02-13, commit 70e1d67)

---

### v2.10.0 (2026-02-08) ⚠️ 已棄用
- **Tag**: `v2.10.0`
- **Commit**: `0440242` (feature/aspect-ratio-usecasegroup-v2.10.0, 本地分支)
- **Status**: ⚠️ **已棄用 - 缺少 v2.10.2 修復，請使用 v2.10.2-final**

**為什麼棄用**:
- 缺少 Permission UX 修復
- 缺少 Aspect Ratio 切換修復
- 缺少 Toast Regression 修復

**替代版本**: 使用 `v2.10.2-final`

---

## v2.11.0 系列

### v2.11.1 (2026-02-13) ✅ 穩定
- **Branch**: `feature/camera-core-v2.11.0`
- **Commit**: `3830fb2` (feature/camera-core-v2.11.0)
- **Build Code**: 74
- **Status**: ✅ **穩定 - 已整合 v2.10.2-final**

**包含功能**:
- ✅ **v2.11.0 功能**:
  - Eye-AF (眼睛優先自動對焦) - ML Kit Face Detection
  - Smart Subject Focus (智能主體對焦) - Saliency-based
  - Rectangle + Corner Brackets 視覺設計 (Cyan #00E5FF)
  - Neon Glow 雙層輝光效果
- ✅ **v2.10.2 功能**:
  - Aspect Ratio Toggle (4:3/16:9/1:1) + UseCaseGroup + ViewPort
  - EXIF 資訊顯示 (GPS, 曝光參數, 裝置資訊)
  - Settings UI 現代化 (可摺疊卡片, 位置權限管理)
  - v2.10.2 Bug 修復 (Permission UX + Aspect Ratio + Toast Regression)

**關鍵提交**:
- `70e1d67`: Merge v2.10.2-final into v2.11.0: Combine Eye-AF + EXIF features
- `3830fb2`: [Fix] Restore 16KB ELF alignment check script
- `3aac584`: [Feature] v2.11.0: Eye-AF + Smart Subject Focus + Rectangle UI
- `6501ac2`: [Fix] v2.10.2: Bug fixes - Permission UX + Aspect Ratio + Toast Regression

**測試結果**:
- ✅ Stage Verification Phase 1-4: PASS
- ✅ Emulator Testing: PASS (無 FATAL/ERROR, Optical Flow 穩定)
- ⏳ Pixel 9a 實機測試: Pending
- 測試報告: `tmp/test_outputs/v2.11.1-emulator/EMULATOR_TEST_REPORT.md`

**決議文檔**:
- `devspec/feature_record_Backlog.md` (Camera Core Features, Preview, Photo Capture 章節)
- `docs/test_results/v2.11.0-Phase5/HANDOVER_SUMMARY.md`
- `docs/test_results/v2.11.0-Phase5/PIXEL9A_TEST_GUIDE.md`
- `docs/test_results/v2.10.2/PIXEL9A_TEST_REPORT.md`

**已知問題**: 無

**待辦事項**:
- [ ] 執行 Pixel 9a 實機測試 (Phase 5)

---

## 版本命名規則

### Tag 命名格式
- **功能版本**: `v<major>.<minor>.<patch>` (例如: v2.10.0)
- **修正版本**: `v<major>.<minor>.<patch>-final` (例如: v2.10.2-final)
- **候選版本**: `v<major>.<minor>.<patch>-rc<number>` (例如: v2.11.0-rc1)

### 狀態標示
- ✅ **穩定，可安全合併**: 已通過所有測試，可直接合併到其他分支
- 🚧 **開發中**: 功能未完成或未完整測試
- ⚠️ **已棄用**: 不應使用，已有更新的版本替代
- 🔴 **有已知問題**: 存在 Bug 或問題，需修復後才能合併

---

## 合併檢查清單

在合併任何分支前，請確認：
1. [ ] 確認使用的是**最終修正版**（`-final` tag）
2. [ ] 閱讀 `devspec/feature_record_Backlog.md` 相關章節
3. [ ] 檢查測試報告，確認所有測試通過
4. [ ] 閱讀「已知問題」，評估影響
5. [ ] 執行 `devspec/MERGE_SOP.md` 的完整流程

---

**最後更新**: 2026-02-13
**維護者**: Claude Sonnet 4.5 + Akira Chang
