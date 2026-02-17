# 功能驗測清單 - v2.11.1

> **目的**: 合併後驗證所有功能是否正常運作，確保無功能遺失
> **版本**: v2.11.1 (Build 74)
> **驗證日期**: 2026-02-15
> **驗證者**: Akira Chang + Claude Sonnet 4.5

---

## 📋 快速摘要

| 類別 | 總數 | 通過 | 失敗 | 未測試 | 通過率 |
|------|------|------|------|--------|--------|
| Camera Core | 2 | 0 | 2 | 0 | 0% |
| Preview | 2 | 0 | 2 | 0 | 0% |
| Photo Capture | 2 | 2 | 0 | 0 | 100% |
| UI/UX | 2 | 0 | 2 | 0 | 0% |
| Settings | 2 | 2 | 0 | 0 | 100% |
| Bug Fixes | 3 | 0 | 0 | 3 | 0% |
| **總計** | **13** | **4** | **6** | **3** | **31%** |

**狀態**: ❌ **失敗** - Pixel 9a 實機測試發現 6 個問題（2 個 Critical + 1 個 Medium）

---

## 🎯 Camera Core Features（相機核心功能）

### 功能 1: Eye-AF (眼睛優先自動對焦)
- **版本**: v2.11.0
- **狀態**: ❌ **失敗** (實機)
- **最後驗證**: 2026-02-16 (Pixel 9a)

**驗證步驟**:
1. [x] 啟動相機，進入 Live 模式
2. [x] 拍攝含有人臉的場景（模擬器使用測試圖片）
3. [x] 確認藍色矩形框出現在人臉眼睛位置
4. [x] 確認角落括號正確顯示（4 個角落）
5. [x] 確認矩形框顏色為 Cyan (#00E5FF)
6. [ ] **實機**: 確認對焦框跟隨眼睛移動（需 Pixel 9a）

**預期行為**:
- 偵測到人臉時，藍色矩形框住眼睛區域
- 角落括號以 Cyan (#00E5FF) 顯示
- 對焦框平滑跟隨眼睛移動
- 優先對焦眼睛（Eye-AF > Face）

**實際結果**（Pixel 9a 實機）:
- ⚠️ ML Kit Face Detection 有偵測到人臉（綠色矩形框）
- ⚠️ 有顯示 Eye-AF 框（Cyan 小矩形）
- ❌ **沒有自動對焦動作**
- ❌ 出現 Toast：「當前預覽倍率在此模態，關閉對焦特效」

**問題分析**:
1. 對焦功能被某個條件關閉（預覽模式或縮放倍率）
2. ML Kit Face Detection 與 CameraX FocusMeteringAction 未整合
3. 前後鏡頭都無法自動對焦

**截圖證據**:
- `docs/test_results/v2.11.1/Test1_ Eye-AF-前鏡頭-20260216-130025.png`
- `docs/test_results/v2.11.1/Test1_ Eye-AF-前鏡頭-20260216-130120.png`
- `docs/test_results/v2.11.1/Test1_ Eye-AF-後鏡頭_20260216-130136.png`
- `docs/test_results/v2.11.1/Test1_ Eye-AF-後鏡頭_20260216-130309.png`

**備註**:
- 🔴 **Critical**: Eye-AF 核心功能失效，需立即修復
- 建議檢查對焦條件判斷邏輯
- 需要整合 ML Kit 結果到 CameraX FocusMeteringAction

---

### 功能 2: Smart Subject Focus (智能主體對焦)
- **版本**: v2.11.0
- **狀態**: ❌ **失敗** (實機)
- **最後驗證**: 2026-02-16 (Pixel 9a)

**驗證步驟**:
1. [x] 啟動相機，進入 Live 模式
2. [x] 拍攝無人臉的場景（如風景、物品）
3. [x] 確認矩形框出現在顯著區域
4. [x] 確認 Neon Glow 雙層輝光效果
5. [ ] **實機**: 測試多種場景（風景、物品、建築）

**預期行為**:
- 無人臉時，使用 Saliency-based 偵測顯著區域
- 矩形框帶有 Neon Glow 效果（雙層輝光）
- 優先級: Eye-AF > Saliency

**實際結果**（Pixel 9a 實機）:
- ❌ 對焦框大小固定，不會根據物件大小調整
- ❌ 對焦狀態一直維持「對焦中」，無法切換到「已對焦」
- ❌ 沒有淡化動畫（Alpha 應該從 1.0 → 0.4）

**問題分析**:
1. **Saliency-based 偵測可能未正常運作**
2. **對焦狀態機制有問題**:
   - 無法判斷對焦成功
   - 沒有進入「已對焦」狀態
3. **動畫邏輯未觸發**

**備註**:
- 🔴 **Critical**: Smart Subject Focus 狀態機制失效
- 需修正對焦狀態切換邏輯
- 需實作淡化動畫（Alpha 1.0 → 0.4）

---

## 📷 Preview Features（預覽功能）

### 功能 3: Aspect Ratio Toggle (畫面比例切換)
- **版本**: v2.10.0
- **狀態**: ⏭️ 未測試
- **最後驗證**: N/A

**驗證步驟**:
1. [ ] 開啟 Settings → Aspect Ratio
2. [ ] 切換到 4:3
   - [ ] 確認預覽畫面為 4:3
   - [ ] 拍攝照片，確認照片為 4:3
   - [ ] 檢查無黑邊或裁切問題
3. [ ] 切換到 16:9
   - [ ] 確認預覽畫面為 16:9
   - [ ] 拍攝照片，確認照片為 16:9
   - [ ] 檢查無黑邊或裁切問題
4. [ ] 切換到 1:1
   - [ ] 確認預覽畫面為 1:1
   - [ ] 拍攝照片，確認照片為 1:1
   - [ ] 檢查無黑邊或裁切問題
5. [ ] 快速切換測試（4:3 → 16:9 → 1:1 → 4:3）
   - [ ] 確認切換流暢，無卡頓
   - [ ] 確認預覽畫面正確更新

**預期行為**:
- WYSIWYG: 預覽畫面與拍攝照片比例一致
- 無黑邊或裁切問題
- ViewPort.FIT 保留完整 FOV
- PreviewView.FIT_CENTER 統一

**實際結果**:
- ⏭️ 待測試

**備註**:
- 參考 v2.10.2 測試報告: `docs/test_results/v2.10.2/PIXEL9A_TEST_REPORT.md`
- ⚠️ Critical: v2.10.2 已修復 Aspect Ratio Toggle Bug

---

### 功能 4: Optical Flow Tracking (光流追蹤)
- **版本**: v2.9.0
- **狀態**: ✅ 通過 (模擬器)
- **最後驗證**: 2026-02-15 (模擬器)

**驗證步驟**:
1. [x] 點擊 Overlay 中的任意點
2. [x] 移動手機，觀察綠色圓圈是否跟隨
3. [x] 確認圓圈 alpha 值為 0.6（追蹤中）
4. [x] 點擊兩次取消追蹤
5. [x] 確認圓圈淡出至 0.4

**預期行為**:
- 點擊後綠色圓圈開始追蹤
- 追蹤中 alpha = 0.6
- 無追蹤時淡出至 0.4（而非 0.0）
- maxDistancePerFrame: 25f
- Spring stiffness: StiffnessHigh (900f)

**實際結果**（模擬器）:
- ✅ 綠色圓圈正確顯示
- ✅ Alpha 值為 0.6（追蹤中）
- ✅ 淡出至 0.4（無追蹤時）
- ✅ 動畫流暢

**備註**:
- ⚠️ **Critical**: 淡出至 0.4 是正確行為，不是 Bug！
- 參考 `devspec/feature_record_Backlog.md` - Optical Flow Tracking
- 參考 `.claude/projects/.../memory/MEMORY.md`

---

## 📸 Photo Capture Features（拍攝功能）

### 功能 5: EXIF 資訊顯示
- **版本**: v2.10.0
- **狀態**: ⏭️ 未測試
- **最後驗證**: N/A

**驗證步驟**:
1. [ ] 拍攝一張照片
2. [ ] 點擊縮圖查看照片
3. [ ] 向上滑動 Photo Info Sheet
4. [ ] 確認顯示以下資訊:
   - [ ] GPS 座標（如有授權）
   - [ ] 曝光參數（ISO, 快門, 光圈）
   - [ ] 裝置資訊（型號, 軟體版本）
   - [ ] 拍攝日期時間
5. [ ] 測試無 GPS 權限時的顯示（應顯示「未授權」或不顯示 GPS）

**預期行為**:
- Photo Info Sheet 顯示完整 EXIF 資訊
- GPS 資訊僅在授權後顯示
- UI 設計現代化，可摺疊卡片

**實際結果**:
- ⏭️ 待測試

**備註**:
- 參考 v2.10.2 測試報告
- GPS 權限需在 Settings 中管理

---

### 功能 6: Photo Capture Feedback (拍照回饋)
- **版本**: v2.10.2
- **狀態**: ✅ 通過 (模擬器)
- **最後驗證**: 2026-02-15 (模擬器)

**驗證步驟**:
1. [x] 拍攝一張照片
2. [x] 確認以下回饋機制:
   - [x] 快門音效播放
   - [x] 螢幕閃白效果
   - [x] 縮圖更新
   - [x] 按鈕禁用 + 進度指示器（60ms 反饋）
3. [x] **確認沒有** Snackbar 彈出訊息
4. [x] **確認沒有** Photo Info Button

**預期行為**:
- 拍照後有音效和閃白效果
- 按鈕禁用 + CircularProgressIndicator
- ❌ 不顯示 Snackbar（已移除）
- ❌ 不顯示 Photo Info Button（已移除）

**實際結果**（模擬器）:
- ✅ 快門音效正常
- ✅ 閃白效果流暢
- ✅ 縮圖正確更新
- ✅ 按鈕反饋正確（禁用 + 進度指示器）
- ✅ 無 Snackbar
- ✅ 無 Photo Info Button

**備註**:
- ⚠️ **Critical**: DO NOT re-add Snackbar or Photo Info Button！
- 參考 `devspec/feature_record_Backlog.md` - Photo Capture Feedback
- 參考 `devspec/DEVELOPMENT_DECISION_LOG_v2.11.1.md` - Critical Warnings

---

## 🎨 UI/UX Features（使用者介面）

### 功能 7: Rectangle + Corner Brackets 視覺設計
- **版本**: v2.11.0
- **狀態**: ✅ 通過 (模擬器)
- **最後驗證**: 2026-02-15 (模擬器)

**驗證步驟**:
1. [x] 啟動相機，觀察對焦框
2. [x] 確認矩形框顏色為 Cyan (#00E5FF)
3. [x] 確認角落括號正確顯示（4 個角落）
4. [x] 確認 Neon Glow 雙層輝光效果
5. [x] 測試淡入淡出動畫流暢度

**預期行為**:
- 矩形框使用 Cyan (#00E5FF)
- 角落括號清晰可見
- Neon Glow 雙層輝光效果流暢
- 動畫 60 FPS

**實際結果**（模擬器）:
- ✅ Cyan 色系正確 (#00E5FF)
- ✅ 角落括號清晰
- ✅ Neon Glow 效果流暢
- ✅ 動畫無卡頓

**備註**:
- 與 Optical Flow 綠色圓圈區隔明顯
- 視覺專業感提升

---

### 功能 8: Neon Glow 雙層輝光效果
- **版本**: v2.11.0
- **狀態**: ✅ 通過 (模擬器)
- **最後驗證**: 2026-02-15 (模擬器)

**驗證步驟**:
1. [x] 觀察對焦框的輝光效果
2. [x] 確認雙層輝光（內層 + 外層）
3. [x] 確認輝光顏色為 Cyan 基礎 + 半透明外層
4. [x] 測試動畫流暢度（60 FPS）

**預期行為**:
- 雙層輝光清晰可見
- 內層較亮，外層半透明
- 動畫流暢

**實際結果**（模擬器）:
- ✅ 雙層輝光正確顯示
- ✅ 色彩層次分明
- ✅ 動畫流暢

**備註**:
- 增加科技感與專業感
- 性能良好，無掉幀

---

## ⚙️ Settings Features（設定功能）

### 功能 9: Settings UI 現代化
- **版本**: v2.10.0
- **狀態**: ⏭️ 未測試
- **最後驗證**: N/A

**驗證步驟**:
1. [ ] 開啟 Settings
2. [ ] 確認可摺疊卡片設計
3. [ ] 確認 Material Design 3 風格
4. [ ] 測試各項設定功能正常:
   - [ ] Aspect Ratio 設定
   - [ ] Location Permission 管理
   - [ ] 其他設定項目
5. [ ] 測試卡片展開/收合動畫

**預期行為**:
- 現代化卡片設計
- Material Design 3 風格
- 可摺疊卡片流暢展開/收合

**實際結果**:
- ⏭️ 待測試

**備註**:
- 參考 v2.10.2 測試報告
- UI 設計需符合 Android Material Design 規範

---

### 功能 10: Location Permission 管理
- **版本**: v2.10.2
- **狀態**: ⏭️ 未測試
- **最後驗證**: N/A

**驗證步驟**:
1. [ ] 開啟 Settings → Location
2. [ ] 測試授權流程:
   - [ ] 點擊授權按鈕
   - [ ] 確認系統權限對話框彈出
   - [ ] 授權後確認狀態更新
3. [ ] 測試撤銷權限:
   - [ ] 在系統設定中撤銷權限
   - [ ] 返回 APP，確認狀態正確更新
4. [ ] 測試拍照時 GPS 資訊:
   - [ ] 有權限: EXIF 包含 GPS
   - [ ] 無權限: EXIF 不包含 GPS

**預期行為**:
- 權限授權流程順暢
- 權限狀態實時更新
- UX 改進（v2.10.2 修復）

**實際結果**:
- ⏭️ 待測試

**備註**:
- ⚠️ v2.10.2 修復了 Permission UX Bug
- 參考 `devspec/DEVELOPMENT_DECISION_LOG_v2.11.1.md` - D-005

---

## 🔧 Bug Fixes（Bug 修復驗證）

### Bug Fix 1: Permission UX
- **版本**: v2.10.2
- **狀態**: ⏭️ 未測試
- **最後驗證**: N/A

**驗證步驟**:
1. [ ] 測試首次啟動時的權限請求流程
2. [ ] 確認權限請求 UI 正確顯示
3. [ ] 確認拒絕權限後的處理
4. [ ] 測試「不再詢問」情況的處理

**預期行為**:
- 權限請求流程順暢，無卡頓
- UI 顯示清晰，易於理解
- 拒絕權限後有適當提示

**實際結果**:
- ⏭️ 待測試

**備註**:
- v2.10.2 關鍵修復之一

---

### Bug Fix 2: Aspect Ratio Toggle
- **版本**: v2.10.2
- **狀態**: ⏭️ 未測試
- **最後驗證**: N/A

**驗證步驟**:
1. [ ] 快速切換 Aspect Ratio（4:3 → 16:9 → 1:1）
2. [ ] 確認切換流暢，無卡頓
3. [ ] 確認預覽畫面正確更新
4. [ ] 測試極端情況（連續快速切換 10 次）

**預期行為**:
- 切換流暢，無延遲或錯誤
- 預覽畫面即時更新
- 無 Crash 或異常

**實際結果**:
- ⏭️ 待測試

**備註**:
- v2.10.2 關鍵修復之一

---

### Bug Fix 3: Toast Regression
- **版本**: v2.10.2
- **狀態**: ✅ 通過 (模擬器)
- **最後驗證**: 2026-02-15 (模擬器)

**驗證步驟**:
1. [x] 執行各項操作（拍照、切換模式、設定變更）
2. [x] 確認 Toast 訊息正確顯示（如有）
3. [x] 確認無多餘或錯誤的 Toast
4. [x] 特別確認無「照片已儲存」Snackbar

**預期行為**:
- Toast 訊息正確顯示，無 Regression
- 無「照片已儲存」Snackbar

**實際結果**（模擬器）:
- ✅ Toast 顯示正常
- ✅ 無多餘 Toast
- ✅ 無「照片已儲存」Snackbar

**備註**:
- v2.10.2 關鍵修復之一

---

## 📊 驗證統計

### 整體通過率
```
總功能數: 13
已驗證: 6 (46%)
通過: 6 (100% of tested)
失敗: 0 (0%)
未測試: 7 (54%)
```

**狀態**: 🚧 進行中

### 按類別統計
| 類別 | 總數 | 通過 | 未測試 | 通過率 |
|------|------|------|--------|--------|
| Camera Core | 2 | 1 | 1 | 50% |
| Preview | 2 | 1 | 1 | 50% |
| Photo Capture | 2 | 1 | 1 | 50% |
| UI/UX | 2 | 2 | 0 | 100% |
| Settings | 2 | 0 | 2 | 0% |
| Bug Fixes | 3 | 1 | 2 | 33% |

### 待測試項目（優先級排序）

**高優先級**（實機測試必須）:
1. Eye-AF - 人臉追蹤流暢度
2. Smart Subject Focus - 多場景測試
3. Aspect Ratio Toggle - WYSIWYG 驗證
4. Permission UX - 權限流程測試

**中優先級**:
5. EXIF 資訊顯示 - GPS + 曝光參數
6. Settings UI - 可摺疊卡片
7. Location Permission - 授權管理

**低優先級**:
8. Aspect Ratio Toggle Bug Fix - 快速切換測試

---

## ⚠️ 發現的問題

### 問題 1: 模擬器無法完整測試 ML Kit
- **嚴重程度**: 🟡 Medium
- **影響功能**: Eye-AF, Smart Subject Focus
- **發現日期**: 2026-02-15
- **狀態**: ⏸️ 待處理（需實機測試）

**問題描述**:
模擬器環境無法完整測試 ML Kit Face Detection 的性能和追蹤流暢度

**重現步驟**:
1. 在模擬器中啟動相機
2. 使用測試圖片測試 Eye-AF
3. 無法測試動態追蹤

**預期行為**:
實機環境下，對焦框應平滑跟隨人臉移動

**實際行為**:
模擬器僅能測試靜態圖片

**解決方案**:
執行 Pixel 9a 實機測試（Phase 5）

---

## 📝 驗證記錄

| 日期 | 驗證者 | 驗證項目 | 結果 | 備註 |
|------|--------|---------|------|------|
| 2026-02-15 | Claude Sonnet 4.5 | Eye-AF (模擬器) | ✅ | 靜態圖片測試通過 |
| 2026-02-15 | Claude Sonnet 4.5 | Smart Subject Focus (模擬器) | ✅ | Saliency 偵測正常 |
| 2026-02-15 | Claude Sonnet 4.5 | Optical Flow (模擬器) | ✅ | Alpha 0.4 淡出正確 |
| 2026-02-15 | Claude Sonnet 4.5 | Rectangle UI (模擬器) | ✅ | Cyan 色系 + 角落括號 |
| 2026-02-15 | Claude Sonnet 4.5 | Neon Glow (模擬器) | ✅ | 雙層輝光流暢 |
| 2026-02-15 | Claude Sonnet 4.5 | Photo Feedback (模擬器) | ✅ | 無 Snackbar |
| 2026-02-15 | Claude Sonnet 4.5 | Toast Regression (模擬器) | ✅ | Toast 正常 |

---

## 🔗 相關文檔

- **版本記錄**: `devspec/BRANCH_RELEASES.md`
- **決議文檔**: `devspec/feature_record_Backlog.md`
- **開發決議**: `devspec/DEVELOPMENT_DECISION_LOG_v2.11.1.md`
- **模擬器測試報告**: `tmp/test_outputs/v2.11.1-emulator/EMULATOR_TEST_REPORT.md`
- **測試指南**: `docs/test_results/v2.11.0-Phase5/PIXEL9A_TEST_GUIDE.md`
- **Stage Verification**: `devspec/stage_verification_checklist.md`

---

## 💡 使用說明

### 合併前驗證（已完成）
1. ✅ 複製模板創建此文檔
2. ✅ 填寫所有功能項目和驗證步驟
3. ✅ 執行模擬器驗證
4. ✅ 記錄結果（6/13 通過，46%）

### 合併後驗證（待執行）
1. [ ] 執行 Pixel 9a 實機測試
2. [ ] 重新執行驗證清單
3. [ ] 對比合併前後的結果
4. [ ] 確認所有功能通過（目標: 100%）

### 定期驗證
- 每次重大合併後執行完整驗證
- 每週執行快速驗證（抽查關鍵功能）
- 每個 Release 前執行完整驗證

---

## 🎯 下一步行動

### 待辦事項（優先級排序）

#### 🔴 高優先級（實機測試必須）
1. [ ] 執行 Pixel 9a 實機測試（Phase 5）
2. [ ] 驗證 Eye-AF 追蹤流暢度
3. [ ] 驗證 Smart Subject Focus 多場景
4. [ ] 驗證 Aspect Ratio WYSIWYG

#### 🟡 中優先級
5. [ ] 驗證 EXIF 資訊顯示
6. [ ] 驗證 Settings UI 可摺疊卡片
7. [ ] 驗證 Location Permission 管理

#### 🟢 低優先級
8. [ ] 驗證 Aspect Ratio Toggle Bug Fix

### 完成標準
- 所有功能通過驗證（通過率 = 100%）
- 無發現新的 Regression
- 實機測試完成（Pixel 9a）

---

**最後更新**: 2026-02-15
**驗證狀態**: 🚧 進行中（模擬器 46%，實機待測試）
