# 測試圖片資源

此目錄是測試圖片的 **唯一權威來源**。

## 重要說明

- ✅ **Debug build**：自動包含這些測試圖片
- ✅ **Release build**：不包含這些測試圖片（減少 APK 大小約 1.7 MB）
- ✅ **Asset 組織**：遵循 Android 最佳實踐，測試資源僅存放於 `debug/assets`
- ⚠️ **請勿在 `main/assets` 中放置測試圖片**

## 圖片清單

### V1.0 基礎測試圖片（高解析度版本）
- `portrait_good_composition.jpg` (476 KB) - 良好構圖的人像
- `portrait_bad_headroom.jpg` (444 KB) - 頭部空間不足的人像
- `portrait_off_center.jpg` (405 KB) - 主體偏離中心的人像
- `landscape_symmetry.jpg` (235 KB) - 對稱構圖的風景
- `landscape_rule_of_thirds.jpg` (225 KB) - 三分法構圖的風景

**總大小：** 約 1.76 MB（高解析度，適合構圖分析測試）

### V2.0 進階測試圖片（未來添加）
```
composition/
├── leading_lines_road.jpg          # 道路引導線
├── leading_lines_railway.jpg       # 鐵軌引導線
├── symmetry_architecture.jpg       # 建築對稱
├── symmetry_reflection.jpg         # 水面倒影
├── framing_arch.jpg                # 拱門框架
├── framing_window.jpg              # 窗框框架
├── fill_frame_food.jpg             # 填滿構圖（食物）
└── depth_layers.jpg                # 前景/中景/背景

lighting/
├── backlight_severe.jpg            # 嚴重逆光
├── backlight_moderate.jpg          # 中度逆光
├── high_contrast.jpg               # 高對比場景
├── low_light.jpg                   # 低光場景
├── overexposed.jpg                 # 過曝
└── underexposed.jpg                # 欠曝

scenes/
├── portrait_indoor.jpg             # 室內人像
├── portrait_outdoor.jpg            # 室外人像
├── landscape_mountain.jpg          # 山景風景
├── landscape_sea.jpg               # 海景風景
├── architecture_modern.jpg         # 現代建築
├── architecture_classical.jpg      # 古典建築
├── street_day.jpg                  # 白天街拍
├── street_night.jpg                # 夜間街拍
└── food_overhead.jpg               # 俯拍食物
```

## 使用方式

1. 將測試圖片放入對應目錄
2. 圖片建議尺寸：1920x1080 或更高
3. 格式：JPG 或 PNG
4. 檔案大小：建議 < 5MB 以避免載入延遲

## 技術說明

### Android Asset Merging 機制

```
Asset 解析優先順序：
1. Product Flavor specific (emulator/assets)
2. Build Type specific (debug/assets)     ← 測試圖片存放處
3. Main source set (main/assets)

Debug Build 載入順序：
- 如果 debug/assets/test_images/photo.jpg 存在 → 使用它 ✅
- 如果不存在 → fallback 到 main/assets/test_images/photo.jpg

Release Build 載入：
- 只使用 main/assets（不含 debug/assets 內容）
- 因此 release APK 不包含測試圖片 ✅
```

### TestImageProvider 載入路徑

```kotlin
context.assets.open("test_images/$imageName")
// Android 自動解析為 debug/assets/test_images/$imageName
```

## 注意事項

- ✅ 這些圖片僅在 `debug` build 中使用
- ✅ `release` build 完全不包含這些圖片（APK 更小）
- ✅ 如果找不到測試圖片，系統會自動生成備用圖片（灰色背景 + 白色矩形）
- ⚠️ 請確保圖片檔名與 `TestImageProvider.testImages` 列表一致
