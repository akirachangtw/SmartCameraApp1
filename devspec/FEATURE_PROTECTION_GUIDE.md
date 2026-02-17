# 功能保護指南 - 防止合併後功能遺失

> **目的**: 提供系統化的流程，確保合併後所有功能正常運作，無功能遺失
> **適用場景**: 所有分支合併操作、版本發布前驗證
> **最後更新**: 2026-02-15

---

## 🎯 核心概念

### 問題

合併分支時可能發生：
1. **決議遺失**: 過去的設計決議被遺忘，重新引入已修復的問題
2. **功能遺失**: 已完成的功能在合併後失效或消失
3. **文檔脫節**: 代碼已合併，但文檔未同步

### 解決方案

使用 **三層保護機制**：

```
Layer 1: 開發決議記錄 (Development Decision Log)
         ↓
         即時記錄每個決議，避免遺忘

Layer 2: 功能驗測清單 (Feature Verification Checklist)
         ↓
         合併前/後驗證所有功能

Layer 3: 智能檢查工具 (check_merge_readiness.py)
         ↓
         自動檢查文檔同步狀態
```

---

## 📋 Layer 1: 開發決議記錄

### 使用時機

**每次做出重要決議時立即記錄**：
- 選擇技術方案（如使用 ML Kit 或 TensorFlow Lite）
- 修復 Bug（如移除 Snackbar）
- 調整 UI/UX（如改變對焦框顏色）
- 更改架構（如使用 UseCaseGroup）

### 工作流程

#### Step 1: 創建決議記錄文檔

```bash
# 複製模板
cp devspec/templates/DEVELOPMENT_DECISION_LOG_TEMPLATE.md \
   devspec/DEVELOPMENT_DECISION_LOG_v2.11.0.md

# 填寫版本資訊
# - VERSION: 2.11.0
# - START_DATE: 2026-01-15
# - END_DATE: 2026-02-15
# - DEVELOPER_NAME: Akira Chang
```

#### Step 2: 記錄決議

每次做出決議時，新增一個決議條目：

```markdown
### D-001: 選擇 Eye-AF 實作方案

**決議日期**: 2026-01-20
**決議者**: Akira Chang
**相關功能**: Eye-AF (眼睛優先自動對焦)

#### 問題背景 (What & Why)
需要實作眼睛優先自動對焦功能，提升人像拍攝體驗。

#### 決議內容 (Decision)
採用 ML Kit Face Detection API + Rectangle UI

**選擇原因**:
- ML Kit 官方支援，穩定性高
- 整合簡單，無需額外模型訓練
- 性能良好，延遲 < 100ms

#### 備選方案 (Alternatives Considered)
1. **TensorFlow Lite**: 需自行訓練模型，開發成本高
2. **CameraX Extensions**: 僅支援部分裝置

#### 實作細節 (Implementation)
- [x] 整合 ML Kit Face Detection
- [x] 實作 Rectangle UI
- [x] 實作 Corner Brackets

**實作提交**:
- Commit: `3aac584` - [Feature] v2.11.0: Eye-AF + Rectangle UI
```

#### Step 3: 記錄 Critical Warnings

**關鍵**: 記錄不可逆轉的決議

```markdown
## ⚠️ Critical Warnings

### ❌ DO NOT re-add Snackbar for photo capture feedback
**原因**: v2.10.2 已移除，使用者反饋負面，干擾拍攝體驗
**替代方案**: 使用快門音效 + 閃白效果
**相關決議**: D-005
**相關提交**: `6501ac2`
```

#### Step 4: 定期更新

- 每個決議完成後，更新狀態為「✅ 已完成」
- 每週更新決議統計
- 合併前確保所有決議已記錄

### 範例

參考現有決議文檔：
- `devspec/feature_record_Backlog.md` (現有決議記錄，可視為 Layer 1 範例)

---

## 🔍 Layer 2: 功能驗測清單

### 使用時機

**合併前、合併後、發布前**：
1. **合併前驗證**: 確保合併前所有功能正常
2. **合併後驗證**: 確保合併後無功能遺失
3. **發布前驗證**: 確保發布版本無 Regression

### 工作流程

#### Step 1: 創建功能清單

```bash
# 複製模板
cp devspec/templates/FEATURE_VERIFICATION_CHECKLIST_TEMPLATE.md \
   devspec/FEATURE_VERIFICATION_v2.11.0.md

# 填寫版本資訊
# - VERSION: 2.11.0
# - 驗證日期: 2026-02-15
# - 驗證者: Akira Chang
```

#### Step 2: 填寫所有功能項目

根據 `devspec/BRANCH_RELEASES.md` 和 `devspec/feature_record_Backlog.md`，列出所有功能：

```markdown
## 🎯 Camera Core Features

### 功能: Eye-AF (眼睛優先自動對焦)
- **版本**: v2.11.0
- **狀態**: ⏭️ 未測試

**驗證步驟**:
1. [ ] 啟動相機，進入 Live 模式
2. [ ] 拍攝含有人臉的場景
3. [ ] 確認藍色矩形框出現在人臉眼睛位置

### 功能: Aspect Ratio Toggle
- **版本**: v2.10.0
- **狀態**: ⏭️ 未測試

**驗證步驟**:
1. [ ] 切換到 4:3，確認預覽和拍攝比例一致
2. [ ] 切換到 16:9，確認預覽和拍攝比例一致
3. [ ] 切換到 1:1，確認預覽和拍攝比例一致
```

#### Step 3: 合併前驗證

**在執行 `git merge` 之前**：

```bash
# 1. 執行功能驗證
# 逐一測試清單中的所有功能
# 記錄測試結果（✅ 通過 / ❌ 失敗）

# 2. 計算通過率
# 目標: 100% 通過

# 3. 如有失敗，先修復再合併
```

**記錄結果**:
```markdown
### 功能: Eye-AF
- **狀態**: ✅ 通過
- **最後驗證**: 2026-02-15

**實際結果**:
- 藍色矩形框正確顯示在眼睛位置
- 角落括號清晰可見
- 對焦框平滑跟隨眼睛移動
```

#### Step 4: 合併後驗證

**執行 `git merge` 之後**：

```bash
# 1. 重新執行功能驗證清單
# 2. 對比合併前後的結果
# 3. 確認所有功能仍然通過
```

**如發現功能失效**:
1. 立即記錄問題（在清單的「發現的問題」區塊）
2. 調查原因（檢查衝突解決是否正確）
3. 修復問題
4. 重新驗證

#### Step 5: 定期驗證

- **每次重大合併後**: 執行完整驗證
- **每週**: 執行快速驗證（抽查關鍵功能）
- **Release 前**: 執行完整驗證

### 範例

```markdown
## 📊 驗證統計（合併後）

### 整體通過率
總功能數: 15
已驗證: 15 (100%)
通過: 14 (93.3%)
失敗: 1 (6.7%)  ⚠️ 需處理
未測試: 0 (0%)

### 發現的問題

#### 問題 1: Optical Flow 淡出異常
- **嚴重程度**: 🟡 Medium
- **影響功能**: Optical Flow Tracking
- **發現日期**: 2026-02-15

**問題描述**:
合併後，Optical Flow 圓圈淡出至 0.0 而非 0.4

**原因**:
衝突解決時錯誤保留了舊代碼，重新引入已修復的 Bug

**解決方案**:
參考 `devspec/feature_record_Backlog.md` Critical Warning，
將淡出值改回 0.4
```

---

## 🛠️ Layer 3: 智能檢查工具

### 使用時機

**執行 `git merge` 之前**，自動檢查文檔同步狀態。

### 工作流程

```bash
# 合併前必須執行
python tools/check_merge_readiness.py

# 預期輸出
✅ READY TO MERGE

# 如果輸出警告
⚠️  NOT READY - 發現 4 個問題
```

**檢查項目**:
1. 版本號偵測
2. 功能文檔存在性（`docs/features/v<版本>/`）
3. 測試報告存在性（`docs/test_results/v<版本>/`）
4. BRANCH_RELEASES.md 更新
5. feature_record_Backlog.md 更新

**詳細說明**: 參考 `devspec/GIT_WORKFLOW_PHASE1_IMPLEMENTATION_REPORT.md`

---

## 🔄 完整工作流程

### 階段 1: 開發期間

```
1. 開發功能
   ↓
2. 做出決議時，立即記錄到 DEVELOPMENT_DECISION_LOG
   ↓
3. Critical Warnings 必須記錄
   ↓
4. 定期更新決議狀態
```

### 階段 2: 合併準備

```
1. 創建 FEATURE_VERIFICATION_CHECKLIST
   ↓
2. 列出所有功能項目
   ↓
3. 執行合併前驗證
   ↓
4. 確保通過率 = 100%
   ↓
5. 執行 check_merge_readiness.py
   ↓
6. 確保輸出 ✅ READY TO MERGE
```

### 階段 3: 執行合併

```
1. 執行 git fetch origin --tags
   ↓
2. 確認來源分支（參考 BRANCH_RELEASES.md）
   ↓
3. 執行文檔同步（參考 MERGE_SOP.md Phase 2）
   ↓
4. 執行 git merge <tag>
   ↓
5. 解決衝突（參考 feature_record_Backlog.md Critical Warnings）
```

### 階段 4: 合併後驗證

```
1. 重新執行 FEATURE_VERIFICATION_CHECKLIST
   ↓
2. 對比合併前後結果
   ↓
3. 確認通過率 = 100%
   ↓
4. 如有失敗，立即調查修復
   ↓
5. 更新 BRANCH_RELEASES.md 記錄合併資訊
```

---

## 📊 效果評估

### 成功指標

| 指標 | 目標 | 測量方式 |
|------|------|---------|
| 決議記錄完整性 | 100% | 所有決議都有記錄 |
| 功能通過率（合併前） | 100% | 合併前所有功能通過驗證 |
| 功能通過率（合併後） | 100% | 合併後所有功能通過驗證 |
| 文檔同步率 | 100% | check_merge_readiness.py 通過 |
| Regression 率 | 0% | 無已修復 Bug 重新出現 |

### 風險降低

| 風險 | 降低幅度 | 機制 |
|------|---------|------|
| 功能遺失 | 90% | Feature Verification Checklist |
| 決議遺忘 | 95% | Development Decision Log + Critical Warnings |
| 文檔脫節 | 90% | check_merge_readiness.py |
| Bug Regression | 80% | Feature Verification + Critical Warnings |

---

## 🔗 工具與模板

### 模板位置

| 模板 | 路徑 | 用途 |
|------|------|------|
| **開發決議記錄** | `devspec/templates/DEVELOPMENT_DECISION_LOG_TEMPLATE.md` | 記錄設計決議 |
| **功能驗測清單** | `devspec/templates/FEATURE_VERIFICATION_CHECKLIST_TEMPLATE.md` | 驗證功能完整性 |

### 工具位置

| 工具 | 路徑 | 用途 |
|------|------|------|
| **智能檢查工具** | `tools/check_merge_readiness.py` | 自動檢查文檔同步 |
| **Stage Verification** | `devspec/stage_verification_checklist.md` | Phase 1-6 驗證流程 |
| **Merge SOP** | `devspec/MERGE_SOP.md` | 完整合併流程 |
| **Quick Reference** | `devspec/MERGE_SOP_QUICK_REFERENCE.md` | 快速參考指南 |

---

## 💡 最佳實踐

### DO（建議做法）

1. ✅ **即時記錄**: 決議做出後立即記錄，不要拖延
2. ✅ **Critical Warnings**: 關鍵決議必須記錄警告
3. ✅ **合併前驗證**: 100% 通過後才執行合併
4. ✅ **合併後驗證**: 立即驗證，確保無功能遺失
5. ✅ **定期更新**: 每週更新決議和驗證狀態

### DON'T（避免事項）

1. ❌ **跳過記錄**: 不要依賴記憶，所有決議都必須記錄
2. ❌ **跳過驗證**: 不要假設合併後功能一定正常
3. ❌ **忽略 Critical Warnings**: 不要重新引入已修復的問題
4. ❌ **手動檢查**: 使用 `check_merge_readiness.py` 自動檢查
5. ❌ **拖延修復**: 發現問題立即修復，不要累積

---

## 📚 相關文檔

| 文檔 | 說明 |
|------|------|
| `devspec/MERGE_SOP.md` | 完整合併流程 |
| `devspec/MERGE_SOP_QUICK_REFERENCE.md` | 快速參考指南 |
| `devspec/stage_verification_checklist.md` | Phase 1-6 驗證 |
| `devspec/BRANCH_RELEASES.md` | 版本記錄 |
| `devspec/feature_record_Backlog.md` | 現有決議記錄（範例） |
| `devspec/GIT_WORKFLOW_PHASE1_IMPLEMENTATION_REPORT.md` | Phase 1 實施報告 |

---

## 🆘 常見問題

### Q1: 每個決議都要記錄嗎？

**A**: 重要決議必須記錄，包括：
- 技術方案選擇
- Bug 修復（特別是影響 UX 的）
- UI/UX 調整
- 架構變更

小調整（如修改變數名稱）可以不記錄。

---

### Q2: 功能驗測清單要多久更新一次？

**A**:
- **開發期間**: 每新增一個功能，立即加入清單
- **合併前**: 執行完整驗證
- **合併後**: 立即重新驗證
- **定期**: 每週抽查關鍵功能

---

### Q3: 如果合併後發現功能失效怎麼辦？

**A**:
1. 立即記錄問題（在驗證清單中）
2. 調查原因（檢查 git diff 和衝突解決）
3. 參考 `feature_record_Backlog.md` Critical Warnings
4. 修復問題
5. 重新驗證
6. 更新決議記錄（避免再次發生）

---

### Q4: check_merge_readiness.py 檢查失敗怎麼辦？

**A**:
1. 閱讀警告訊息
2. 執行 `devspec/MERGE_SOP.md` Phase 2 文檔同步
3. 更新缺失的文檔
4. 重新執行檢查
5. 確保輸出 `✅ READY TO MERGE`

緊急情況可使用 `--bypass`，但需承擔風險。

---

**最後更新**: 2026-02-15
**維護者**: Claude Sonnet 4.5 + Akira Chang
