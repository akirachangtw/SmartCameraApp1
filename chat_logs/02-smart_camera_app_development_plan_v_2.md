# SmartCameraApp Development Plan v2

（此文件可直接放入 Git 專案作為正式開發藍圖文件）

---

# 📌 文件資訊
**文件名稱**：SmartCameraApp_DevelopmentPlan_v2.md  
**版本**：2.0  
**建立時間**：2025-12-01  
**作者**：SmartCamera AI Camera Project  
**目的**：整合「AI 即時攝影師」專案所有現階段設計、智慧導引場景、構圖法與開發階段，統一成可直接放入 Git 的規格文件。

---

# 目錄
1. [專案願景](#專案願景)
2. [智慧導引支援場景與構圖法矩陣](#智慧導引支援場景與構圖法矩陣)
3. [Overlay UI 設計目標](#overlay-ui-設計目標)
4. [核心技術架構](#核心技術架構)
5. [開發階段計畫（V1.0-V5.0）](#開發階段計畫v10-v50)
6. [功能拆解](#功能拆解)
7. [版本里程碑 (Milestones)](#版本里程碑-milestones)
8. [測試計畫](#測試計畫)
9. [專案目錄結構建議](#專案目錄結構建議)
10. [未來擴充方向](#未來擴充方向)

---

# 專案願景
SmartCameraApp 的核心目標是：

**在使用者按下快門前，透過 AI 即時分析構圖與光影，在預覽畫面直接提供專業級攝影導引與智能補光，產生更佳影像成果。**

實現「即時 AI 攝影師（AI Live Photographer）」的完整體驗：
- 智慧構圖判斷
- 光影分析（高光、陰影、逆光）
- HDR / Tone Mapping 即時預覽
- 智能補光（人像分割）
- 場景化提示（街拍、夜景、旅遊、人像、風景…）

---

# 智慧導引支援場景與構圖法矩陣
此矩陣為整個系統的核心依據，也是各版本開發優先順序的基準。

| 拍攝場景 | 使用構圖法 | AI 偵測後的動作 | UI 即時提示文字（示例） |
|--------------|----------------|-----------------------|-------------------------------|
| **人像** | 三分法（眼睛在 1/3） | 檢查人臉 bounding box 是否接近上三分線 | 「請將相機上移，使眼睛位於上三分線」 |
| | 頭部空間 Headroom | 計算人臉上方空間比例 | 「頭部空間略多，可微微上提手機」 |
| | S 型姿態 | 偵測肩線、體姿角度 | 「可微調姿勢以形成自然 S 型線條」 |
| | 留白 | 分析背景佔比 | 「增加右側留白可提升呼吸感」 |
| **旅遊 / 街拍** | 引導線 | 使用線偵測（例如 Hough）分析道路/欄杆方向 | 「利用道路線條增加深度感」 |
| | 對稱 | 比較左右亮度、形狀區塊是否接近 | 「再向左靠近一些以對齊中心線」 |
| | 框架式 | 判定拱門、窗框等封閉結構區域 | 「移動相機，使主體落在框架中央」 |
| | 對角線 | 檢查主要線條傾斜角度是否接近 45° | 「可向右旋轉讓線條形成對角視覺」 |
| **風景** | 三分法（天/地比例） | 檢測地平線位置與畫面上下比例 | 「地平線偏低，可稍向下壓構圖」 |
| | 黃金比例 | 分析主體中心點與黃金螺旋區域的關係 | 「將焦點放在黃金螺旋端點更吸睛」 |
| | 前景–主體–背景 | 檢查畫面是否缺乏前景層 | 「加入前景可增加畫面層次」 |
| | 留白 | 分析天空與地景佔比 | 「保留更多天空可強化開闊感」 |
| **夜景 Low Light** | 對稱 | 偵測水面或建築兩側亮度與形狀是否對稱 | 「向右平移以獲得完美對稱效果」 |
| | 引導線 | 偵測城市街燈或道路線條延伸方向 | 「利用街燈形成夜間引導線」 |
| | 光軌 | 分析車流光軌方向與延伸感 | 「調整角度讓光軌延伸更順暢」 |
| | 留白 | 分析暗部與亮部比例 | 「多保留暗部使主體更突出」 |
| **食物 / 物品特寫** | 填滿構圖 Fill the Frame | 偵測主體在畫面中的佔比 | 「靠近一點，讓主體填滿畫面更具吸引力」 |
| | 三角形構圖 | 判斷盤子/物件三點是否形成穩定視覺三角形 | 「稍微移動，使三元素形成三角視覺」 |
| | 對稱 | 分析盤面或桌面線條對稱程度 | 「可居中拍攝以突出整體對稱性」 |
| | 留白 | 分析桌面空間留白區域 | 「右側可保留少許留白提升質感」 |

---

# Overlay UI 設計目標
APP 的智慧導引均以 Overlay 呈現在 CameraX 預覽畫面上。

Overlay 需具備：
### **1. 基礎構圖輔助**
- 三分法格線
- 水平儀
- 主體框（Face/Object Detection Box）

### **2. 進階攝影輔助**
- 引導線（Leading Lines）
- 對稱中線（Symmetry Axis）
- 框架式標註（Framing Overlay）
- HDR / 逆光警示圖示
- AI 構圖提示文字泡泡（Suggestion Bubble）

### **3. AI 補光渲染輔助**
- Portrait Segmentation Mask（以 Shader 呈現）
- 人像區域補光 Feathering

---

# 核心技術架構
整體技術架構分為 4 個層級：

```
CameraX → ImageAnalysis → AI 構圖/光影引擎 → Overlay / Shader Rendering
```

### **主要元件**
- CameraX Preview + ImageAnalysis（Strean）
- ML Kit（人臉、主體偵測）
- OpenCV（線條/對稱性 可選）
- TFLite（人像分割）
- OpenGL ES / Vulkan（Tone Mapping、補光 Shader）
- Jetpack Compose（Overlay UI）

---

# 開發階段計畫（V1.0-V5.0）
依據智慧導引支援場景重新制定開發 Roadmap。

---

# **V1.0 — 基礎相機 + 三分法構圖引導（MVP）**
**目標：可拍照、可分析、可給最小構圖建議。**

### **功能清單**
- CameraX 核心 + 圖像分析管線(ImageAnalysis)
- 三分法格線 Overlay
- 水平儀 Overlay
- 人臉偵測 + 主體框
- 構圖引擎 V1：
  - 人像三分法
  - 頭部空間
  - 主體置中
- UI 提示（Move Up / Move Left 氣泡）

### **支援場景**
✔ 人像   ✔ 風景   ✔ 一般街拍

---

# **V2.0 — 進階構圖法 + 場景導引**
**目標：完整支援所有構圖場景矩陣中的主要構圖法。**

### **功能清單**
- 引導線偵測（Leading Lines）
- 對稱性偵測（Symmetry）
- 框架式構圖（Framing）
- 深度構圖分析（Foreground/Middle/Background）
- 填滿構圖（Fill the Frame）
- 光影分析（逆光 / 過曝 / 低光）
- HDR / 光影警示 Overlay
- 構圖引擎 V2 加強：根據場景產生更精準提示

### **支援場景**
✔ 旅拍   ✔ 建築   ✔ 夜景   ✔ 食物 / 小物

---

# **V3.0 — 即時 HDR + Tone Mapping Shader**
**目標：讓 HDR、光影補正直接呈現在預覽畫面中。**

### **功能清單**
- OpenGL ES / Vulkan 渲染管線建立
- HDR Tone Mapping（Reinhard / ACES / Filmic）
- 亮度直方圖數據導入 Shader
- 高光壓縮與陰影提亮
- 夜景/逆光場景自動啟用 Tone Mapping
- HDR 模式圖示 Overlay

### **支援場景**
✔ 夜景   ✔ 高對比風景   ✔ 室內昏暗環境

---

# **V4.0 — AI 智能補光（TFLite Portrait Segmentation）**
**目標：真正實現 AI 逆光補光與自然人像增亮。**

### **功能清單**
- TFLite Portrait Segmentation（人像分割）
- 人像 Mask → GPU 紋理
- Shader：人像區域補光 + 背景保持不變
- 羽化（Feathering）消除邊界感
- 自動補光強度模型（根據逆光嚴重度）
- UI 提示：「AI 補光已啟動」

### **支援場景**
✔ 逆光人像   ✔ 室內自拍   ✔ 夜景人像

---

# **V5.0 — 智慧場景模式（Smart Scene Modes）**
**目標：自動依場景推薦最佳構圖法與光影策略。**

### **功能清單**
- 場景識別（人像 / 旅拍 / 建築 / 夜景 / 食物）
- 依場景自動套用構圖提示：
  - 建築 → 對稱
  - 食物 → 填滿
  - 旅拍 → 框架式
  - 夜景 → HDR 建議
- 自動構圖評分（Score）
- 「按下快門時機」提示

### **支援場景**
✔ 全場景智慧化

---

# 功能拆解
以下為各功能模組的明確定義。

### **1. AI Engine（核心智慧模組）**
- Composition Engine
- Lighting Engine
- Scene Classifier
- Tracking Engine

### **2. Overlay Layer（Compose）**
- GridOverlay (三分法)
- LevelIndicator
- FaceBoxOverlay
- LeadingLinesOverlay
- SymmetryOverlay
- HDRWarningOverlay
- SuggestionBubble
- SegmentationMaskPreview（V4+）

### **3. GPU Layer**
- Base Shader
- ToneMapping Shader
- SmartFillLight Shader
- Mask Blending

---

# 版本里程碑 (Milestones)
| 版本 | 時程 | 交付項目 |
|------|------|-----------|
| **V1.0** | 第 1-2 個月 | 基礎構圖與人像判斷 |
| **V2.0** | 第 3-4 個月 | 引導線、對稱、深度、光影判斷 |
| **V3.0** | 第 5-7 個月 | HDR Shader、Tone Mapping |


---

# 版本里程碑 (Milestones)
| 版本 | 時程 | 交付項目 |
|------|------|-----------|
| **V1.0** | 第 1-2 個月 | 基礎相機、三分法、水平儀、主體框、基本構圖引導 |
| **V2.0** | 第 3-4 個月 | 引導線、對稱、框架式構圖、深度構圖、填滿構圖、光影分析、HDR 警示 |
| **V3.0** | 第 5-7 個月 | HDR/Tone Mapping Shader、曝光推估、自動 HDR 策略 |
| **V4.0** | 第 8-11 個月 | TFLite 人像分割、智能補光 Shader、羽化、逆光補光模型 |
| **V5.0** | 第 12-13 個月 | 場景識別、智慧構圖推薦、構圖評分、最佳拍照時機提示 |

---

# 測試計畫
SmartCameraApp 的測試覆蓋三大類：**功能測試、效能測試、裝置相容性測試**。

## 1. 功能測試 (Feature Testing)
### V1.0
- [ ] 預覽畫面正常顯示
- [ ] 三分法格線準確對齊
- [ ] 臉部偵測框位置正確
- [ ] 构圖提示（上移/下移/置中）能在正確時機觸發

### V2.0
- [ ] 引導線偵測準確（道路、建築、欄杆）
- [ ] 對稱性軸線與建築結構對齊
- [ ] 框架式構圖可辨識窗框、拱門
- [ ] 填滿構圖偵測主體大小正確
- [ ] 光影分析：逆光、高反差、過暗能正確辨識

### V3.0
- [ ] Tone Mapping Shader 切換正常
- [ ] HDR 模式在高反差場景自動啟用
- [ ] Shader 渲染與 CameraX 預覽保持同步

### V4.0
- [ ] TFLite 模型載入成功（GPU Delegate 正常）
- [ ] 模型 Mask 與人像邊界一致
- [ ] 補光 Shader 正確作用在人物區域
- [ ] 羽化邊緣自然無破綻

### V5.0
- [ ] 場景識別準確：人像/旅遊/建築/夜景/食物
- [ ] 構圖推薦與場景一致
- [ ] 拍攝時機提示在畫面最佳時觸發

---

## 2. 效能測試 (Performance Testing)
| 測試項目 | 目標值 |
|----------|--------|
| 預覽 FPS | 高階: 60 FPS / 中階: 30 FPS |
| ML Kit 分析延遲 | < 100 ms |
| HDR Shader 延遲 | < 16 ms (1 frame) |
| TFLite 分割推論 | < 50 ms |
| 記憶體占用 | < 300 MB（高階） / <200 MB（中階） |
| 電力消耗 | 10 分鐘拍攝中不得超過 5% 電量 |

---

## 3. 裝置相容性測試 (Device Compatibility)
需在 3 種等級的手機上測試：
- 高階：Pixel 8 / S24 Ultra
- 中階：Pixel 7a / Samsung A54（主要測試平台）
- 低階：任何 Android 7.0+ 裝置

測試內容：
- CameraX 相機對焦行為
- Shader 是否能正確編譯
- ML Kit / TFLite 是否能在 GPU Delegate 正常運作

---

# 專案目錄結構建議
建議依照 Clean Architecture + 分層 AI Engine 設計：

```
app/src/main/java/local/mylab/smartcameraapp1/
│
├── domain/                      # 業務邏輯
│   ├── model/                   # 架構資料模型
│   ├── usecase/                 # 構圖/光影判斷用例
│   └── repository/              # 定義資料存取介面
│
├── data/                        # 實作層
│   ├── repository/              # Repository 實作
│   └── source/
│       ├── camera/              # CameraX 整合
│       ├── ml/                  # ML Kit 偵測
│       ├── analysis/            # 光影與構圖 AI engine
│       ├── ai/                  # TFLite Segmentation
│       └── rendering/           # Shader / GLES
│
├── presentation/
│   ├── camera/                  # Camera UI
│   └── components/overlay/      # 所有 Overlay UI 元件
│
├── di/                          # Hilt DI module
├── util/                        # 公用工具
└── MainActivity.kt
```

---

# 未來擴充方向
以下為 V5 之後可加入的進階特色：

### **1. AI 构圖評分系統（Composition Scoring AI）**
輸入圖片 → 給予 0-100 分專業構圖分數。

### **2. 進階人像風格化拍攝**
- 美膚程度
- 膚色自動校準
- 瞳孔光補強

### **3. AI Scenic Story Mode（智慧敘事模式）**
自動根據旅遊景點、建築類型提供拍攝風格建議。

### **4. 影片模式（Video Mode）**
- 即時 HDR 影片
- 即時構圖提示
- 影片用分割（肖像影片）

### **5. 雲端擴充**
- 上傳至雲端分析最佳構圖
- 自動照片分類（人像/風景/夜景）

---

# 文件完結
此文件已完成所有內容，可直接加入 Git 專案作為正式開發藍圖。
