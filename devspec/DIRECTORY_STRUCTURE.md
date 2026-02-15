# 專案目錄結構規範

> **目的**: 定義專案文檔的分類邏輯與存放路徑，確保文檔組織一致性
> **最後更新**: 2026-02-15
> **維護者**: Claude Sonnet 4.5 + Akira Chang

---

## 📂 目錄結構總覽

```
SmartCameraApp1/
├── plans/              # 開發 APP 專案計畫（整體規劃）
├── docs/               # 項目文檔
│   ├── features/       # 功能實作計劃（L1/L2 Plan）
│   ├── releases/       # 版本發布記錄
│   ├── reports/        # 分析報告
│   ├── git/            # Git 管理文檔
│   ├── guides/         # 操作指南
│   ├── issues/         # Issue 追蹤
│   ├── test_results/   # 測試報告
│   └── session_handoff/ # 工作交接
├── devspec/            # 開發規範與流程
├── tools/              # 開發工具與腳本
└── app/                # Android 應用源碼
```

---

## 🎯 目錄用途定義

### 1. `plans/` - 開發 APP 專案計畫

**用途**: 整體開發規劃、產品路線圖、架構設計

**子目錄結構**:
```
plans/
├── roadmap/        # 產品路線圖
├── architecture/   # 架構設計
├── proposals/      # 提案與可行性評估
└── archive/        # 已完成/棄用計劃
```

**存放內容**:
- ✅ 產品整體開發藍圖
- ✅ 長期技術路線圖（Roadmap）
- ✅ 架構設計文檔
- ✅ 跨版本的大型提案
- ❌ 不存放：具體功能的 L1/L2 計劃（應放 `docs/features/`）

**命名規範**:
```
格式: <編號>-<名稱>_<類型>.md
範例: 02.4-smart_camera_app_roadmap.md
     01-AI_PHOTOGRAPHER_COMPLETE_BLUEPRINT.md
```

**何時使用**:
- 撰寫產品整體發展方向文檔
- 設計跨版本的架構調整
- 提出大型功能集的可行性評估

---

### 2. `docs/features/` - 功能實作計劃（L1/L2 Plan）

**用途**: 具體功能的 L1/L2/Traycer 實作計劃

**子目錄結構**:
```
docs/features/
├── v2.8.5/             # 按版本分類
├── v2.9.0/
├── v2.10.0/
├── v2.11.0/
└── <專題名稱>/          # 跨版本專題（如 optical_flow）
```

**存放內容**:
- ✅ L1 規劃文檔（問題背景、影響範圍、風險）
- ✅ L2 可行性文檔（API 評估、依賴分析、可行性結論）
- ✅ Traycer 實作計劃（詳細步驟、代碼示例）
- ✅ 功能改進提案（FEATURE_IMPROVEMENT_PROPOSAL）
- ❌ 不存放：產品路線圖、整體架構設計

**命名規範**:
```
格式: <版本>_<功能名稱>_<類型>.md
範例: v2.10.0_aspect_ratio_wysiwyg_fix_L1.md
     v2.10.0_aspect_ratio_wysiwyg_fix_L2.md
     v2.10.0_aspect-ratio-wysiwyg-fix_Traycer.md
     optical-flow-tracking_L1.md (跨版本專題)
```

**何時使用**:
- 執行 Hybrid Workflow L1 規劃
- 執行 L2 可行性評估
- 撰寫 Traycer 實作計劃
- 記錄功能開發的決策過程

**⚠️ 重要規則**:
- 同一功能的 L1/L2/Traycer 必須放在同一版本目錄下
- 跨版本的專題（如 Optical Flow）創建獨立目錄
- 文件名必須包含版本號（除非是跨版本專題）

---

### 3. `docs/releases/` - 版本發布記錄

**用途**: 版本發布相關的文檔

**子目錄結構**:
```
docs/releases/
├── v2.7.0/
│   └── RELEASE_NOTES_v2.7.0.md
├── v2.8.0/
│   ├── RELEASE_NOTES_v2.8.0.md
│   └── V2.8.4_PULL_REQUEST.md
└── v2.11.0/
    └── INTEGRATION_COMPLETE_20260104.md
```

**存放內容**:
- ✅ Release Notes（版本發布說明）
- ✅ Pull Request 描述文檔
- ✅ 版本整合完成報告
- ❌ 不存放：測試報告（應放 `docs/test_results/`）

**命名規範**:
```
格式: <文檔類型>_<版本>.md
範例: RELEASE_NOTES_v2.8.0.md
     V2.8.4_PULL_REQUEST.md
     INTEGRATION_COMPLETE_20260104.md
```

---

### 4. `docs/reports/` - 分析報告

**用途**: 技術分析、性能評估、可行性報告

**存放內容**:
- ✅ 技術可行性分析報告
- ✅ 性能評估報告
- ✅ 兼容性分析報告
- ✅ 下一階段開發建議

**命名規範**:
```
格式: <報告主題>_report.md 或 <主題>_analysis.md
範例: 16kb-compatibility-report.md
     NEXT_PHASE_RECOMMENDATIONS.md
```

---

### 5. `docs/git/` - Git 管理文檔

**用途**: Git 工作流程、分支管理、合併指南

**存放內容**:
- ✅ Git 分析指南
- ✅ Git 分析報告
- ✅ 合併衝突處理指南（待新增）

**命名規範**:
```
格式: GIT_<主題>_<類型>.md (全大寫)
範例: GIT_ANALYSIS_GUIDE.md
     GIT_ANALYSIS_REPORT.md
     GIT_MERGE_CONFLICT_GUIDE.md
```

---

### 6. `docs/guides/` - 操作指南

**用途**: 測試指南、工具使用、操作手冊

**存放內容**:
- ✅ 測試操作指南
- ✅ 工具使用說明
- ✅ 工作交接流程
- ✅ 快速檢查清單

**命名規範**:
```
格式: <主題>_<類型>.md (全大寫或首字母大寫)
範例: MANUAL_TEST_HANDOFF.md
     QUICK_TEST_CHECKLIST.md
     adb_screencap_guide.md
```

---

### 7. `docs/issues/` - Issue 追蹤

**用途**: Bug 追蹤、問題分析、Issue 記錄

**存放內容**:
- ✅ Bug 分析文檔
- ✅ 問題追蹤記錄
- ✅ Issue 調查報告

**命名規範**:
```
格式: <問題簡述>.md (小寫+底線)
範例: front_camera_mlkit_mirror.md
     preview_zoom_aspect_ratio.md
```

---

### 8. `docs/test_results/` - 測試報告

**用途**: 版本測試報告、實機測試結果

**子目錄結構**:
```
docs/test_results/
├── v2.10.0-fix/
│   ├── HANDOVER_SUMMARY.md
│   ├── PIXEL9A_TEST_REPORT.md
│   └── *.log
├── v2.10.2/
└── v2.11.0-Phase5/
```

**存放內容**:
- ✅ 測試報告（HANDOVER_SUMMARY, TEST_REPORT）
- ✅ Logcat 日誌（*.log）
- ✅ 測試截圖（*.png）
- ✅ 測試指南（TEST_GUIDE）

**命名規範**:
```
目錄: v<版本>/ 或 v<版本>-<階段>/
文件: <設備>_TEST_REPORT.md
     HANDOVER_SUMMARY.md
     <測試類型>_logcat.log
```

---

### 9. `devspec/` - 開發規範與流程

**用途**: 開發流程、規範文檔、模板、檢查清單

**存放內容**:
- ✅ 開發流程規範（MERGE_SOP, DAILY_DEVELOPMENT_SOP）
- ✅ 分支發布記錄（BRANCH_RELEASES）
- ✅ 功能決議記錄（feature_record_Backlog）
- ✅ 檢查清單（stage_verification_checklist）
- ✅ 模板（pull_request_template, issue templates）
- ✅ 指南（FEATURE_RECORD_GUIDE, pr_creation_guide）

**命名規範**:
```
格式: <主題>_<類型>.md 或 <主題>.md (全大寫或首字母大寫)
範例: MERGE_SOP.md
     BRANCH_RELEASES.md
     feature_record_Backlog.md
     stage_verification_checklist.md
```

---

## 🚨 常見錯誤與正確做法

### 錯誤 1: L1/L2 文檔存放錯誤

❌ **錯誤做法**:
```
plans/v2.10.0_aspect_ratio_fix_L1.md  (放在 plans/)
```

✅ **正確做法**:
```
docs/features/v2.10.0/aspect_ratio_fix_L1.md  (放在 docs/features/)
```

**原因**: `plans/` 用於整體專案計畫，L1/L2 是具體功能實作計劃，應放在 `docs/features/`

---

### 錯誤 2: 版本發布文檔存放錯誤

❌ **錯誤做法**:
```
docs/RELEASE_NOTES_v2.8.0.md  (散落在 docs/ 頂層)
```

✅ **正確做法**:
```
docs/releases/v2.8.0/RELEASE_NOTES_v2.8.0.md  (按版本分類)
```

---

### 錯誤 3: 測試報告與功能計劃混淆

❌ **錯誤做法**:
```
docs/features/v2.10.0/PIXEL9A_TEST_REPORT.md
```

✅ **正確做法**:
```
docs/test_results/v2.10.0/PIXEL9A_TEST_REPORT.md
```

**原因**: 測試報告屬於測試結果，不是功能實作計劃

---

## 📖 快速決策樹

### 我應該把文檔放在哪裡？

```
開始
 │
 ├─ 是整體專案規劃/架構設計？
 │   └─ Yes → plans/ (roadmap/, architecture/, proposals/)
 │
 ├─ 是具體功能的 L1/L2/Traycer 計劃？
 │   └─ Yes → docs/features/<版本>/
 │
 ├─ 是版本發布記錄/Release Notes？
 │   └─ Yes → docs/releases/<版本>/
 │
 ├─ 是測試報告/測試結果？
 │   └─ Yes → docs/test_results/<版本>/
 │
 ├─ 是 Bug 追蹤/Issue 分析？
 │   └─ Yes → docs/issues/
 │
 ├─ 是技術分析報告/可行性評估？
 │   └─ Yes → docs/reports/
 │
 ├─ 是 Git 相關指南/分析？
 │   └─ Yes → docs/git/
 │
 ├─ 是操作指南/工具使用說明？
 │   └─ Yes → docs/guides/
 │
 └─ 是開發流程/規範/模板？
     └─ Yes → devspec/
```

---

## 🔧 自動化檢查工具

使用以下工具驗證文檔是否存放在正確位置：

```bash
# 檢查文檔路徑是否符合規範
tools/check_document_path.sh <file_path>

# 掃描所有文檔並報告路徑錯誤
tools/scan_document_structure.sh
```

---

## 📝 新增文檔時的檢查清單

創建新文檔前，請確認：

- [ ] 確認文檔類型（專案計劃 / 功能計劃 / 測試報告 / 等）
- [ ] 查看「快速決策樹」確定正確目錄
- [ ] 使用正確的命名規範
- [ ] 如果是 L1/L2 文檔，確認存放在 `docs/features/<版本>/`
- [ ] 如果是測試報告，確認存放在 `docs/test_results/<版本>/`
- [ ] 更新相關索引文檔（如有需要）

---

## 🔗 相關文檔

- **開發流程**: `devspec/DAILY_DEVELOPMENT_SOP.md`
- **Hybrid Workflow**: `devspec/hybrid_workflow_checklist.md`
- **功能記錄指南**: `devspec/FEATURE_RECORD_GUIDE.md`
- **合併流程**: `devspec/MERGE_SOP.md`

---

**重要提醒**: 此文檔是**唯一真實來源（SSOT）**，任何目錄結構變更都必須更新此文檔。
