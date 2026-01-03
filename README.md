# Smart Camera App - AI 攝影助手

[![Version](https://img.shields.io/badge/version-2.6.0-blue.svg)](CHANGELOG.md)
[![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-Proprietary-red.svg)]()

智能相機應用程式，結合 AI 技術提供即時構圖建議、光線分析與 HDR 警告，協助使用者拍攝出專業級照片。

---

## 📱 功能特色

### v2.6.0 - 光線分析與 HDR 警告系統 (最新)

#### 🌟 智能光線分析
- **5 種光線場景自動偵測**
  - 🌅 **背光場景 (Backlight)**: 偵測主體逆光情況
  - 🌈 **高對比場景 (High Contrast)**: 識別明暗反差強烈場景
  - ☀️ **過曝場景 (Overexposed)**: 檢測高光溢出問題
  - 🌙 **低光場景 (Underexposed)**: 偵測光線不足環境
  - ✅ **正常光線 (Normal)**: 理想曝光狀態

#### 🎨 HDR 智能警告 Overlay
- **顏色編碼視覺警告**
  - 深橙色 (#FF5722): 背光/高對比場景
  - 紅色 (#F44336): 過曝警告
  - 黃色 (#FFC107): 低光警告
- **即時中文建議訊息**
  - "逆光場景 - 建議使用 HDR 或開啟補光"
  - "高對比場景 - 建議使用 HDR 模式"
  - "曝光過度 - 建議降低曝光或使用 HDR"
  - "光線不足 - 建議增加光源或調高曝光"
- **Auto HDR 智能啟用**: 背光和高對比場景自動開啟 HDR

#### 📐 填充框架構圖增強
- **5 級主體面積評分系統**
  - < 30%: 太小 - "建議靠近拍攝"
  - 30-40%: 可接受低
  - 40-70%: 理想
  - 70-85%: 可接受高
  - \> 85%: 太大 - "建議退後拍攝"
- **智能拍攝距離建議**: 根據主體大小提供精確指引

---

### 核心功能

#### 🎯 AI 構圖分析
- **規則三分法 (Rule of Thirds)**: 自動評估主體位置是否符合三分法則
- **頭部空間 (Headroom)**: 人像攝影時檢測頭部上方留白
- **眼睛位置 (Eye Level)**: 分析眼睛是否在理想水平線
- **邊緣距離 (Edge Distance)**: 檢測主體是否過於靠近畫面邊緣
- **填充框架 (Fill-the-Frame)**: 評估主體占畫面比例

#### 👤 AI 主體偵測
- **人臉偵測**: Google ML Kit 人臉辨識，支援多人場景
- **物體偵測**: 辨識主要拍攝物體，自動框選
- **即時追蹤**: 動態追蹤主體移動

#### 📊 即時反饋系統
- **構圖評分**: 0.0-1.0 即時評分顯示
- **視覺提示**: 綠色 (良好) / 黃色 (可改進) / 紅色 (需調整)
- **文字建議**: 繁體中文構圖改善建議
- **參考線網格**: 可選三分線 / 黃金比例線

#### 📷 相機控制
- **HDR 模式**: AUTO / ON / OFF 三段式切換
- **前後鏡頭切換**: 一鍵切換前後相機
- **水平儀**: 即時顯示相機傾斜角度
- **縮放控制**: 支援雙指縮放調整焦距

---

## 🏗️ 技術架構

### Clean Architecture
```
📦 SmartCameraApp
├── 🎨 Presentation Layer (Jetpack Compose)
│   ├── CameraScreen - 主相機介面
│   ├── CameraViewModel - 狀態管理
│   └── Overlays - 視覺提示元件
│       ├── HDRWarningOverlay - HDR 警告
│       ├── CompositionGuideOverlay - 構圖指引
│       ├── GridOverlay - 參考線網格
│       └── LevelIndicator - 水平儀
├── 🧠 Domain Layer
│   ├── UseCases
│   │   ├── AnalyzeLightingUseCase - 光線分析
│   │   ├── AnalyzeCompositionUseCase - 構圖分析
│   │   ├── DetectFacesUseCase - 人臉偵測
│   │   └── DetectObjectsUseCase - 物體偵測
│   └── Models
│       ├── LightingState - 光線狀態模型
│       ├── CompositionState - 構圖狀態模型
│       └── DetectionResult - 偵測結果模型
└── 💾 Data Layer
    ├── Repository - CameraRepository
    ├── DataSources
    │   ├── LiveCameraSource - CameraX 整合
    │   └── Analysis
    │       ├── ExposureDetector - 曝光檢測
    │       ├── HistogramAnalyzer - 直方圖分析
    │       └── CompositionAnalyzer - 構圖分析
    └── ML Kit Integration - Google ML Kit
```

### 核心技術棧
- **UI**: Jetpack Compose (Material 3)
- **相機**: CameraX (androidx.camera)
- **AI/ML**: Google ML Kit (Face Detection, Object Detection)
- **依賴注入**: Hilt (Dagger)
- **非同步**: Kotlin Coroutines + Flow
- **架構**: MVVM + Clean Architecture
- **測試**: JUnit 5, Robolectric, Mockito

---

## 📋 系統需求

- **最低 Android 版本**: Android 7.0 (API 24)
- **目標 Android 版本**: Android 14 (API 36)
- **相機權限**: 必須
- **儲存權限**: 拍照儲存需要
- **最小記憶體**: 2 GB RAM
- **推薦記憶體**: 4 GB RAM 以上

---

## 🚀 建置指南

### 環境需求
- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17 或更新版本
- Gradle 8.11.1
- Kotlin 1.9+

### 建置步驟

1. **Clone 專案**
   ```bash
   git clone <repository-url>
   cd SmartCameraApp1
   ```

2. **開啟專案**
   - 使用 Android Studio 開啟專案
   - 等待 Gradle 同步完成

3. **建置 APK**

   **模擬器版本** (使用測試圖片):
   ```bash
   ./gradlew.bat assembleEmulatorDebug
   ```

   **實機版本** (使用真實相機):
   ```bash
   ./gradlew.bat assembleDeviceDebug
   ```

   **Release 版本**:
   ```bash
   ./gradlew.bat assembleRelease
   ```

4. **安裝到裝置**
   ```bash
   # 模擬器
   adb -s emulator-5554 install -r app/build/outputs/apk/emulator/debug/app-emulator-debug.apk

   # 實機 (例: Sony XQ-BT52)
   adb -s HQ616R2713 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
   ```

---

## 🧪 測試

### 執行單元測試
```bash
# 所有單元測試
./gradlew.bat test

# 特定測試類別
./gradlew.bat testDeviceDebugUnitTest --tests com.akirachang.ai.photographer.domain.usecase.AnalyzeLightingUseCaseTest
```

### 測試覆蓋率
- **v2.6.0 單元測試**: 72/72 tests (100% 通過率)
  - AnalyzeLightingUseCaseTest: 20/20 tests
  - AnalyzeCompositionUseCaseTest: 52/52 tests

### 測試報告
- 單元測試報告: `app/build/reports/tests/testDeviceDebugUnitTest/index.html`
- 詳細測試文檔: [PHASE1_TEST_REPORT.md](PHASE1_TEST_REPORT.md)
- 實機測試指引: [REAL_DEVICE_TEST_SESSION.md](REAL_DEVICE_TEST_SESSION.md)

---

## 📊 性能指標

### v2.6.0 性能目標
- **FPS**: ≥ 30 FPS (所有 AI 功能啟用)
- **記憶體**: ≤ 200 MB
- **CPU 使用率**: ≤ 40%
- **相機啟動時間**: < 2 秒
- **分析延遲**: < 100ms

### 性能監控
詳見 [PERFORMANCE_MONITORING_GUIDE.md](chat_logs/PERFORMANCE_MONITORING_GUIDE.md)

---

## 🗺️ 開發路線圖

### ✅ Phase 1: 光線分析與 HDR 警告 (v2.6.0) - 已完成
- 光線場景自動分類
- HDR 智能警告 UI
- 填充框架構圖增強

### 🚧 Phase 2: OpenCV 與引導線檢測 (v2.7.0) - 規劃中
- OpenCV 整合
- 引導線自動偵測（水平線、垂直線、對角線）
- 對稱性分析
- 透視修正建議

### 📅 Phase 3: 進階構圖與美學評分 (v2.8.0) - 規劃中
- 黃金比例分析
- 色彩和諧度評估
- 視覺權重分析
- 整體美學評分系統

詳見完整開發計劃: [V2.8.0_development_plan.md](chat_logs/V2.8.0_development_plan.md)

---

## 📖 文檔

- [CHANGELOG.md](CHANGELOG.md) - 版本更新記錄
- [V2.6.0_DEVELOPMENT_STATUS.md](V2.6.0_DEVELOPMENT_STATUS.md) - v2.6.0 開發狀態
- [PHASE1_TEST_REPORT.md](PHASE1_TEST_REPORT.md) - Phase 1 測試報告
- [REAL_DEVICE_TEST_SESSION.md](REAL_DEVICE_TEST_SESSION.md) - 實機測試手冊
- [PERFORMANCE_MONITORING_GUIDE.md](chat_logs/PERFORMANCE_MONITORING_GUIDE.md) - 性能監控指南

---

## 🔧 疑難排解

### 常見問題

**Q: 相機預覽黑屏**
- 檢查相機權限是否已授予
- 確認裝置支援 CameraX
- 重啟應用程式

**Q: ML Kit 偵測失敗**
- 確認網路連線（首次使用需下載模型）
- 檢查 Google Play Services 版本
- 清除應用程式快取

**Q: HDR 警告未顯示**
- 確認 HDR 模式設定為 "AUTO"
- 檢查光線場景是否符合警告條件
- 查看 logcat 確認光線分析狀態

**Q: 模擬器建置與實機建置差異**
- 模擬器使用測試圖片 (emulatorDebug)
- 實機使用真實相機 (deviceDebug)
- 確認使用正確的 build variant

---

## 👨‍💻 開發團隊

- **Lead Developer**: Akira Chang
- **AI Assistant**: Claude Code (Anthropic)

---

## 📄 授權

Copyright © 2024-2025 Akira Chang. All rights reserved.

此專案為私有專案，未經授權不得複製、分發或使用。

---

## 📞 聯絡方式

如有問題或建議，請透過以下方式聯繫：
- Email: [Your Email]
- GitHub Issues: [Repository Issues URL]

---

## 🙏 致謝

- Google ML Kit 團隊 - 提供優秀的 AI 偵測功能
- Android CameraX 團隊 - 簡化相機開發
- Jetpack Compose 團隊 - 現代化 UI 框架
- Anthropic Claude - AI 開發協助

---

**最後更新**: 2025-12-30
**版本**: v2.6.0
