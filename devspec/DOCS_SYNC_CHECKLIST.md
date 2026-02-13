# 分支合併文檔同步清單

> **目的**: 確保合併分支時，所有重要文檔（決議、計劃、測試報告）都正確同步
> **使用時機**: 執行 git merge 前

---

## 合併場景：v2.10.2-final → v2.11.0

### devspec/ 目錄

#### ✅ feature_record_Backlog.md
- **同步章節**:
  - [ ] `## Preview / Resolution / Aspect Ratio (v2.10.0)` (完整章節)
  - [ ] `## Photo Capture Feedback (v2.9.8 → v2.10.2)` (完整章節)
  - [ ] `## Optical Flow Tracking` 的 `✅ Confirmed` 中的 Spring StiffnessHigh 相關記錄
- **合併方式**: 保留兩邊內容，手動合併（使用 `git checkout --ours` 或 `--theirs`）
- **注意事項**:
  - ⚠️ Photo Capture Feedback 章節包含 **Critical Warnings**，必須保留
  - 更新 `Last updated` 日期為合併日期

#### ⏭️ 其他 devspec/ 文檔
- `hybrid_workflow_checklist.md` - 檢查是否有更新
- `pull_request_template.md` - 檢查是否有更新
- `stage_verification_checklist.md` - 無需同步（通用文檔）

---

### docs/plan/ 目錄

#### ✅ v2.10.0 相關計劃文檔
- [ ] `V2.10.0_FEATURE_IMPROVEMENT_PROPOSAL.md`
- [ ] `V2.10.0_SETTINGS_UI_MODERNIZATION.md`
- [ ] `V2.10.0_SETTINGS_UI_MODERNIZATION_Traycer.md`
- [ ] `V2.10.0_SOLUTION_COMPARISON.md`
- [ ] `v2.10.0_aspect-ratio-wysiwyg-fix_Traycer.md`
- [ ] `v2.10.0_aspect_ratio_wysiwyg_fix_L1.md`
- [ ] `v2.10.0_aspect_ratio_wysiwyg_fix_L2.md`
- [ ] `v2.10.0_optimize-app-features_Traycer.md`
- [ ] `v2.10.0_solution_proposal_L2.md`

**合併方式**:
```bash
git checkout v2.10.2-final -- docs/plan/v2.10.0_*
git checkout v2.10.2-final -- docs/plan/V2.10.0_*
```

**注意事項**: 這些文檔是決議和分析的歷史記錄，應完整保留

---

### docs/issues/ 目錄

#### ✅ v2.10.0/v2.10.2 相關 Issue 文檔
- [ ] 檢查是否有 v2.10.0 或 v2.10.2 相關的 issue 文檔
- [ ] 如果有，複製到目標分支

**檢查方式**:
```bash
git show v2.10.2-final:docs/issues/ | ls
```

**合併方式**: 複製到目標分支

---

### docs/test_results/ 目錄

#### ✅ v2.10.0 測試報告
- [ ] `v2.10.0/HANDOVER_SUMMARY.md`
- [ ] `v2.10.0-fix/CROSS_DEVICE_TEST_REPORT.md`
- [ ] `v2.10.0-fix/HANDOVER_SUMMARY.md`
- [ ] `v2.10.0-fix/SONY_XQ-BT52_TEST_REPORT.md`

#### ✅ v2.10.2 測試報告
- [ ] `v2.10.2/PIXEL9A_TEST_REPORT.md`
- [ ] `v2.10.2/emulator_test_logcat.log`
- [ ] `v2.10.2/pixel9a_test_logcat.log`

**合併方式**:
```bash
git checkout v2.10.2-final -- docs/test_results/v2.10.0/
git checkout v2.10.2-final -- docs/test_results/v2.10.0-fix/
git checkout v2.10.2-final -- docs/test_results/v2.10.2/
```

**注意事項**: 測試報告是驗證功能的重要證據，必須完整保留

---

### tools/ 目錄

#### ⏭️ 檢查新增工具
- [ ] 檢查 v2.10.0 是否有新增工具腳本
- [ ] 檢查 `tools/test_handoff_gui.pyw` 是否有更新

**檢查方式**:
```bash
git diff HEAD v2.10.2-final -- tools/
```

**合併方式**: 如果有新增或更新，複製到目標分支

---

### chat_logs/ 目錄

#### ⏭️ v2.10.0/v2.10.2 開發日誌
- [ ] `handofflog_20260203_231928.md`
- [ ] `handofflog_20260204_153930.md`
- [ ] `handofflog_20260204_213447.md`
- [ ] `handofflog_20260208_011800.md`
- [ ] `handofflog_20260208_235900.md`

**合併方式**: 這些是歷史記錄，Git 會自動處理（無需手動干預）

---

## 文檔同步流程

### Step 1: 使用自動化腳本（推薦）
```bash
bash tools/sync_branch_docs.sh v2.10.2-final feature/camera-core-v2.11.0
```

### Step 2: 手動檢查清單
逐項檢查上述所有文檔，確認同步完成

### Step 3: 創建同步記錄
```bash
# 自動生成同步記錄
cat > devspec/DOCS_SYNC_LOG_$(date +%Y%m%d_%H%M%S).md <<EOF
# 文檔同步記錄

- **日期**: $(date +%Y-%m-%d %H:%M:%S)
- **來源**: v2.10.2-final
- **目標**: feature/camera-core-v2.11.0
- **執行者**: [Your Name]

## 已同步文檔
- devspec/feature_record_Backlog.md (部分章節)
- docs/plan/v2.10.0_* (全部)
- docs/test_results/v2.10.0/ (全部)
- docs/test_results/v2.10.2/ (全部)

## 手動合併項目
- [ ] devspec/feature_record_Backlog.md - 合併衝突已解決
- [ ] 其他: ___________

## 驗證
- [ ] 所有文檔已檢查
- [ ] 無遺漏的決議或測試報告
EOF
```

---

## 常見問題

### Q: 如果兩個分支都修改了同一個文檔怎麼辦？
**A**:
1. 優先保留最新的修復（v2.10.2）
2. 如果兩邊都有重要內容，手動合併
3. 參考 `devspec/feature_record_Backlog.md` 的 Critical Warnings

### Q: 測試報告需要全部同步嗎？
**A**:
- ✅ 是的，測試報告是功能驗證的證據
- 保留完整的測試歷史有助於未來的 Regression 檢測

### Q: chat_logs/ 需要同步嗎？
**A**:
- 通常不需要手動同步
- Git 會自動處理這些文件的合併
- 如果有衝突，優先保留最新的日誌

---

**最後更新**: 2026-02-13
**維護者**: Claude Sonnet 4.5 + Akira Chang
