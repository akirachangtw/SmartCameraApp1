# 構圖引導視覺設計參考

此資料夾包含 DirectionalIndicator 重新設計的視覺參考檔案。

## 📁 檔案說明

### 視覺設計選項

1. **option1_target_circle.html** - 選項 1：目標圓圈（簡潔版）
   - 僅顯示最近的三分點目標圓圈
   - 脈衝動畫引導使用者移動主體
   - 簡潔清晰，推薦使用

2. **option2_with_guideline.html** - 選項 2：目標圓圈 + 引導線
   - 在選項 1 基礎上增加虛線引導
   - 從主體中心連接到目標位置
   - 更明確的視覺引導方向

3. **option3_all_points.html** - 選項 3：顯示全部四個三分點
   - 顯示所有四個三分點交叉位置
   - 最近的點會高亮顯示並脈衝
   - 其他點以半透明靜態圓圈顯示
   - 適合進階使用者

4. **animation_timeline.html** - 動畫時間軸展示
   - 顯示脈衝動畫的三個關鍵幀
   - 包含完整的技術規格和 Compose 實作範例
   - 展示 Alpha 和 Scale 變化過程

## 🚀 使用方法

### 在瀏覽器中檢視

直接在瀏覽器中開啟任何 HTML 檔案即可檢視動畫效果：

```bash
# Windows
start option1_target_circle.html

# macOS
open option1_target_circle.html

# Linux
xdg-open option1_target_circle.html
```

### 推薦檢視順序

1. 先看 `option1_target_circle.html` 了解基礎設計
2. 對比 `option2_with_guideline.html` 看引導線效果
3. 查看 `option3_all_points.html` 了解多點顯示
4. 最後看 `animation_timeline.html` 了解動畫技術細節

## 🎨 設計規格

### 顏色系統

- **目標圓圈**: `#4CAF50` (Material Green 500)
- **外圈 Alpha**: `0.6` (60% 不透明度)
- **內圈 Alpha**: `0.4 → 1.0` (動畫範圍)
- **引導線**: `rgba(76, 175, 80, 0.5)`
- **九宮格線**: `rgba(255, 255, 255, 0.2)` (降低干擾)

### 尺寸規格

- **外圈半徑**: 40dp (靜態)
- **內圈基礎半徑**: 20dp
- **內圈縮放範圍**: 1.0 → 1.3 (動畫)
- **中心點半徑**: 4dp
- **線寬**: 3dp (外圈)、2dp (引導線)

### 動畫參數

- **循環週期**: 1000ms (1 秒)
- **動畫模式**: `RepeatMode.Reverse`
- **緩動函數**: `tween(1000)` - 線性插值
- **Alpha 變化**: 0.4 ↔ 1.0
- **Scale 變化**: 1.0 ↔ 1.3

## 🔧 實作注意事項

### Compose 實作範例

```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "target_pulse")

val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000),
        repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
)

val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.3f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000),
        repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
)
```

### 目標位置計算

目標位置應為主體中心到最近的三分點交叉位置：

```kotlin
// 四個三分點交叉位置
val thirdPoints = listOf(
    Point(imageWidth / 3f, imageHeight / 3f),       // 左上
    Point(imageWidth * 2f / 3f, imageHeight / 3f),  // 右上
    Point(imageWidth / 3f, imageHeight * 2f / 3f),  // 左下
    Point(imageWidth * 2f / 3f, imageHeight * 2f / 3f) // 右下
)

// 找出最近的點
val nearestPoint = thirdPoints.minByOrNull { point ->
    val dx = point.x - faceCenterX
    val dy = point.y - faceCenterY
    sqrt(dx * dx + dy * dy)
}
```

## 📊 設計決策

### 為什麼推薦選項 1？

1. **視覺簡潔**: 不會造成畫面混亂
2. **意圖明確**: 使用者直覺理解要將主體移動到圓圈位置
3. **效能最佳**: 只需繪製一個目標圓圈
4. **適合初學者**: 不需要理解九宮格原理

### 選項 2 的優勢

- 明確顯示移動方向（虛線引導）
- 適合需要精確引導的場景
- 稍微增加視覺複雜度

### 選項 3 的使用場景

- 進階使用者想自己選擇構圖點
- 展示所有可能的構圖位置
- 適合攝影教學模式

## 🎯 下一步

選擇偏好的設計選項後，需要：

1. 修改 `AnalyzeCompositionUseCase.kt` 添加目標位置計算
2. 重寫 `DirectionalIndicator.kt` 實作新的視覺設計
3. 更新 `CompositionState.kt` 添加目標座標資料
4. 測試並調整顏色、尺寸、動畫速度

---

**建立日期**: 2025-12-01
**版本**: 2.1.0-preview
**作者**: Claude Code
