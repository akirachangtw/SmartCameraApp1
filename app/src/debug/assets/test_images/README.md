# 測試圖片資源

此目錄用於存放模擬器開發時使用的測試圖片。

## 圖片清單

### V1.0 基礎測試圖片
- `portrait_good_composition.jpg` - 良好構圖的人像
- `portrait_bad_headroom.jpg` - 頭部空間不足的人像
- `portrait_off_center.jpg` - 主體偏離中心的人像
- `landscape_symmetry.jpg` - 對稱構圖的風景
- `landscape_rule_of_thirds.jpg` - 三分法構圖的風景

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

## 注意事項

- 這些圖片僅在 `debug` 和 `emulator` flavor 中使用
- `release` 和 `device` flavor 不會包含這些圖片
- 如果找不到測試圖片，系統會自動生成備用圖片
