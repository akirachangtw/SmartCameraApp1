# SmartCameraApp 下階段開發計劃整合分析

**分析日期**: 2026-02-10
**基於文件**:
- `plans/02.4-smart_camera_app_roadmap.md` - 開發路線圖
- `docs/plan/v2.9.9-LumiFrame-AI-Focusing-light-shallow-deep.md` - LumiFrame AI 計劃
- `devspec/feature_record_Backlog.md` - 功能記錄和待辦清單

---

## 📊 當前專案狀態

### ✅ 已完成版本
- **v2.10.2** ✅ (當前穩定版)
  - 權限 UX 改善
  - 長寬比例設定修復
  - Toast Regression 修復
  - Regression Prevention Phase 1 實施完成

- **v2.9.9** ✅ (相機核心補完)
  - GPS EXIF 標記（非同步處理）
  - 快門視覺反饋（CircularProgressIndicator）
  - 光影分析系統
  - 引導線偵測

### 🟡 進行中 / 🔴 高優先級
- **v2.10.0-v2.10.2** ✅ 已完成
- **v2.9.9 Day 1-2** 🟡 相機核心功能

---

## 🎯 下階段開發項目（Q2 2026）

### 📋 階段 1: v3.0.0 UI/UX 升級 (4 週)

#### 🎨 NeuralLens 色彩系統
**目標**: 科幻風格 UI，提升專業感
- Material 3 進階應用
- 動態色彩增強
- 漸進式動畫效果

**預估工作量**: 2-3 週
**相關檔案**: 待新建

#### 🧭 DirectionalIndicator 方向指示器
**目標**: 實時構圖對齐提示
- 三分法網格動態提示
- 引導線視覺強化
- 對稱線實時反饋

**預估工作量**: 1-2 週
**相關檔案**:
- `CameraScreen.kt` (新增 DirectionalIndicator Overlay)
- `CameraViewModel.kt` (指示器邏輯)

#### ✨ UI 動畫與過渡
**目標**: 流暢的視覺體驗
- 模態過渡動畫
- 按鈕交互反饋
- 狀態變化動畫

**預估工作量**: 1 週

---

### 📋 階段 2: 相機核心功能 (v2.11.0 - 1-2 週，補完 v2.9.9 原規劃)

#### 🔍 音量鍵拍照
**目標**: 提升易用性
- 音量鍵（上/下）映射拍照
- 系統級音量控制保留
- 設定開關（預設開啟）

**預估工作量**: 2-3 天
**相關檔案**:
- `CameraScreen.kt` - Modifier.onKeyEvent
- `CameraViewModel.kt` - 事件處理

#### 🖱️ 雙擊拍照
**目標**: 快速拍照方式
- 預覽區域雙擊觸發拍照
- 防止誤觸（400ms 去抖動）
- 視覺反饋

**預估工作量**: 1-2 天
**相關檔案**:
- `CameraScreen.kt` - 手勢偵測

#### 🔎 Pinch-to-Zoom 優化
**目標**: 平滑縮放控制
- 夾捏手勢縮放
- 滑桿控制輔助
- 焦點位置鎖定

**預估工作量**: 2-3 天
**相關檔案**:
- `LiveCameraSource.kt` - 縮放 API
- `CameraScreen.kt` - 手勢和 UI

#### 📍 對焦點 UI 優化
**目標**: 改善對焦反饋
- 點擊對焦動畫
- 對焦成功/失敗指示
- 自動對焦狀態顯示

**預估工作量**: 2 天
**相關檔案**:
- `CameraScreen.kt` - FocusIndicator Overlay
- `CameraViewModel.kt` - 對焦狀態管理

---

### 📋 階段 3: 進階視覺功能 (v3.5.0 - 2-3 週)

#### 🌙 夜間模式
**目標**: 低光環境適配
- 深色主題適配
- 低光預覽增強
- 感光度動態調整

**預估工作量**: 1-2 週
**複雜度**: MEDIUM
**相關檔案**:
- `CameraScreen.kt` - 夜間模式 UI
- `CameraViewModel.kt` - 模式管理
- `SettingsScreen.kt` - 設定開關

#### 📍 GPS 精度指示器
**目標**: 位置信息可視化
- 精度等級顯示（優秀/良好/一般）
- 定位狀態指示
- 衛星數量顯示

**預估工作量**: 3-5 天
**相關檔案**:
- `CameraScreen.kt` - GPS 指示器 Overlay
- `LocationPermissionHandler.kt` - 精度計算

#### 🗺️ 位置資訊即時顯示
**目標**: EXIF 數據可視化
- 地址反查（可選）
- 座標顯示
- 高度信息

**預估工作量**: 3-5 天
**相關檔案**:
- `CameraScreen.kt` - 位置信息 Overlay
- `CameraRepositoryImpl.kt` - GPS 數據整合

#### 📸 EXIF 查看器升級
**目標**: 專業級信息展示
- 升級為 NeuralLens 科幻風格
- 完整 EXIF 參數顯示
- 地圖集成（可選）

**預估工作量**: 1 週
**複雜度**: MEDIUM
**相關檔案**:
- `PhotoInfoBottomSheet.kt` - 升級設計
- `ExifInfo.kt` - 數據結構

---

## 🚀 中期願景 (v3.1.0 - v4.0.0)

### v3.1.0: 進階視覺設計 (4-6 週)

#### ✨ Glassmorphism 設計系統
**目標**: 現代科幻 UI 風格
- 毛玻璃效果（Blur + Semi-transparent）
- Material You 進階應用
- 動態色彩方案

**預估工作量**: 2-3 週
**複雜度**: HIGH
**技術棧**: Compose RenderEffect API

#### 🔮 Neural Ring 指示器
**目標**: 進階構圖引導
- 環形進度指示
- 即時構圖評分動畫
- 多層級視覺反饋

**預估工作量**: 1-2 週
**複雜度**: MEDIUM

#### 🛡️ Motion Shield 防抖
**目標**: 動態抖動補償
- 實時動作檢測
- 自適應穩定化
- 低光增強

**預估工作量**: 2-3 週
**複雜度**: HIGH
**技術棧**: OpenCV 動作向量分析

---

### v4.0.0: AI 智能補光系統 (3-4 個月)

#### 🤖 AI 智能補光
**目標**: LumiFrame AI 核心功能
- 虛擬光源模擬
- 景深自動計算
- 光影優化算法

**預估工作量**: 4-6 週
**複雜度**: VERY HIGH
**技術棧**:
- ARCore Depth API
- TFLite 模型
- AGSL 著色器

#### 🎯 構圖增強
**目標**: 實時構圖優化
- 黃金螺旋檢測
- 對角線構圖
- 框架構圖識別
- 即時構圖評分（0-100）

**預估工作量**: 3-4 週
**複雜度**: HIGH
**技術棧**: OpenCV + TFLite

#### 🌍 場景識別
**目標**: 場景自適應
- 6 類場景識別：
  1. 人物肖像
  2. 風景
  3. 美食
  4. 文件
  5. 夜景
  6. 微距
- 場景適配參數
- 自動設定調整

**預估工作量**: 2-3 週
**複雜度**: MEDIUM-HIGH
**技術棧**: ML Kit + 自訓練模型

---

## 📊 優先級和時程表

### Q2 2026 (4-8月)
```
第 1-2 週    第 3-6 週    第 7-9 週    第 10 週
├─ v2.11.0  ├─ v3.0.0   ├─ v3.5.0   ├─ 測試完成
│ 相機核心  │ UI/UX     │ 進階視覺   │ 發佈準備
│ (補完     │ 升級      │ EXIF+GPS  │
│ v2.9.9)   │           │           │
└───────────┴───────────┴───────────┴──────────→
```

### Q3 2026 (7-9月)
- v3.1.0: Glassmorphism + Neural Ring (4-6 週)
- 穩定性測試
- 性能優化

### Q4 2026 (10-12月)
- v4.0.0: AI 智能補光系統 (3-4 月)
- LumiFrame AI 完整實現
- 場景識別與自適應

---

## ⚠️ 待辦清單 (Backlog)

### 🔴 Critical Issues
1. **Optical Flow 實機驗證** (pending Pixel 9a)
   - maxDistancePerFrame: 25f
   - Spring Stiffness: StiffnessHigh
   - 參考: `feature_record_Backlog.md:28-30`

2. **ML Kit 前置鏡頭座標鏡像** (pending 驗證)
   - 詳細: `docs/issues/front_camera_mlkit_mirror.md`

### 🟡 Medium Priority
1. **設備解析度資料庫**
   - 1:1 模式不同設備支持
   - Sony vs Pixel 差異處理

2. **ARCore Depth API PoC**
   - LumiFrame AI 前置研究
   - 景深精度評估

3. **AGSL Bokeh Shader**
   - 背景虛化實現
   - 效能優化

### 🔵 Low Priority
1. **微距模式檢測** (v2.11.0+)
   - 硬體相依性極高
   - 需廣泛設備驗證

2. **圖片尺寸設定** (v3.0.0)
   - 整合設定介面重寫

---

## 📋 LumiFrame AI 專案狀態

**參考**: `docs/plan/v2.9.9-LumiFrame-AI-Focusing-light-shallow-deep.md`

### ✅ 已完成
- 產品願景確定
- 技術棧評估
- UI 設計協議定義

### 🟡 進行中 / 📋 規劃中
1. **Phase 1-2**: 基礎建置與視覺協議動畫 (v3.1.0)
2. **Phase 3**: 深度圖 + 景深虛化算法 (v4.0.0 Phase 1)
3. **Phase 4**: 熱控測試與光影聯動 (v4.0.0 Phase 2)

### 技術需求
- ✅ ARCore Depth API (Depth 感測)
- ✅ AGSL 著色器 (高效模糊)
- 🟡 TFLite 模型訓練（景深估計）
- 🟡 場景識別模型

---

## 🔧 開發規範

### 版本號管理
- **APP 功能變更**: 遞增版本號
  - 新增功能 → Minor +1
  - Bug 修復 → Patch +1
  - 重大變更 → Major +1

- **開發工具變更**: 不遞增版本號
  - 使用 `[Docs]` / `[Tools]` prefix
  - 記錄在 commit 訊息

### Commit 規範
```
[Feature] v3.0.0: Add NeuralLens color system

- Implement Glassmorphism design foundation
- Add dynamic color enhancement
- Create progressive animation effects

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### 測試要求
- 每版本至少 80% 代碼覆蓋
- 模擬器測試 (API 34+)
- 實機測試 (Pixel 9a)
- 性能基準驗證

---

## 📈 性能目標

### 框架率 (FPS)
- **目標**: 60 FPS (最小 48 FPS)
- **當前**: 27.2 FPS (v2.8.4)
- **改進計劃**:
  - 動畫分層優化 (v3.1.0)
  - Compose 重組優化 (v3.0.0)
  - 著色器優化 (v4.0.0)

### 記憶體使用
- **目標**: < 250 MB
- **當前**: 212 MB (穩定)
- **預留**: 景深計算 (+30-50 MB)

### 電池消耗
- **目標**: 60 分鐘使用時間
- **當前**: ~45 分鐘
- **改進計劃**:
  - GPS 採樣優化 (v3.5.0)
  - AI 推論降級 (v4.0.0)
  - 熱控機制 (v3.1.0)

---

## ✅ 檢查清單

### 即將開始 (v3.0.0)
- [ ] 設計評審 (NeuralLens 配色)
- [ ] API 確認 (Material 3 進階)
- [ ] 時程確認 (4 週估算)
- [ ] 團隊分配

### 準備開發 (v2.11.0)
- [ ] 功能規格確認
- [ ] 測試計劃制定
- [ ] 實機測試清單

### 中期評估 (v3.1.0)
- [ ] ARCore Depth PoC 驗證
- [ ] AGSL 著色器 PoC
- [ ] 性能基準測試

### 長期規劃 (v4.0.0)
- [ ] TFLite 模型定義
- [ ] 場景識別資料集
- [ ] AI 訓練流程

---

**下一步**: 確認 v3.0.0 設計稿並啟動開發！

**相關文檔**:
- 路線圖: `plans/02.4-smart_camera_app_roadmap.md`
- LumiFrame: `docs/plan/v2.9.9-LumiFrame-AI-Focusing-light-shallow-deep.md`
- 待辦: `devspec/feature_record_Backlog.md`

---

**最後更新**: 2026-02-10
**狀態**: ✅ 分析完成，準備進入 v3.0.0 開發
