## **現代化專業對焦系統規範 (Focus System Spec)**

為符合現代旗艦手機的科技感與專業操作，對焦指示器（Focus Indicator）需根據偵測主體類型自動調整視覺與動畫。

> **v2.12.0-12 重要更正**：`isFocusIndicatorLocked`（Yellow 框）**只對應 AE/AF Lock（長按）**。
> Eye-AF / 點擊對焦 / 物件對焦成功後 `afState = Locked` 時，顏色仍為 **Cyan**，不切換 Yellow。

### **1.1 後置鏡頭對焦規則**

|功能類型|觸發方式|視覺特效 (Visual)|動畫行為 (Animation)|框體規格 (Sizing)|
|-|-|-|-|-|
|**Eye-AF / 物件對焦**|ML Kit 自動偵測人臉/物件|科技感極光藍輝光 (#00F2FF Cyan Glow)|**機械夾爪夾緊** + 支援**光流追蹤**（框隨主體移動）。對焦成功後：框停留 ~1秒 → 淡出至半透明 (Alpha 0.4) 保持追蹤。|**動態比例**：自動調整長寬比以精確框住人臉或物件邊緣（ML Kit BoundingBox）。|
|**環境/紋理對焦**|點擊螢幕無主體位置|科技感極光藍輝光 (#00F2FF Cyan Glow)|支援**光流追蹤**（框隨座標特徵移動）。|**固定大小**：72dp × 72dp 正方形。|
|**AE/AF 鎖定**|**長按**主體 或 點擊鎖定圖示|經典黃色輝光 (#FFD700 Yellow Glow)|鎖定圖示淡入 + 呼吸感微跳動（**無追蹤**，座標固定）。|**固定大小**：72dp × 72dp 正方形。|

* **AE/AF 鎖定定義與觸發**：

  * **定義**：固定目前的對焦距離與曝光參數，不隨畫面變動或主體移動而重新計算。
  * **觸發方式**：

    1. **長按主體**：直接在預覽畫面上長按目標主體進行鎖定。
    2. **點擊鎖定圖示**：點擊 UI 介面上的專屬鎖定按鈕（Lock Icon）進行鎖定。

  * **⚠️ 關鍵規則**：Yellow 框只在以上兩種觸發方式成立時出現。Eye-AF 對焦成功（`afState = Locked`）的 Cyan 框**不屬於 AE/AF Lock**，不得顯示 Yellow。

* **多重對焦邏輯**：

  * 支援 **Eye-AF 多人臉偵測**：畫面上可同時存在多個半透明 Cyan 框，且皆具備光流追蹤。
  * 支援 **優先級權重**：當使用者點擊螢幕特定位置時，優先執行「點擊對焦」，其餘自動偵測框降為低亮度或消失。

* **視覺生命週期**：對焦成功後，框體停留約 **1 秒** → 平滑淡出至 **半透明 (Alpha 0.4)** 保持追蹤，直至主體消失或失焦。

### **1.2 前置鏡頭對焦規則 (自拍優化)**

* **唯一模式**：僅啟用 **Eye-AF 對焦**（不支援點擊手動對焦）。
* **視覺效果**：與後置鏡頭一致的科技感極光藍輝光 (#00F2FF **Cyan**)。
* **動畫流程**：

  1. 偵測到人臉/眼睛。
  2. 觸發 **「機械夾爪夾緊」** 動畫並開啟**光流追蹤**。
  3. 框體停留 **1 秒**。
  4. 平滑淡出至 **半透明 (Alpha 0.4)** 保持追蹤，以減少對自拍構圖的視覺干擾。

* **不支援 Yellow 框**：前置鏡頭無 AE/AF Lock 功能，不出現 Yellow 顏色。

### **1.3 顏色對照表（SSOT）**

| 顏色 | Hex | 對應狀態 | 鏡頭 |
|------|-----|---------|------|
| Cyan | `#00F2FF` | Eye-AF / 點擊對焦 / 物件對焦（對焦中、成功、追蹤中） | 前置 + 後置 |
| Yellow | `#FFD700` | AE/AF Lock（長按 或 鎖定圖示點擊）| 後置限定 |

---

## **2. 技術實現拆解**

### **2.1 智慧構圖引導 (Smart Composition Guide)**

* **主體偵測**：利用 **ML Kit Object Detection** 與 **Face Detection** 獲取 BoundingBox。
* **追蹤技術**：結合 **光流演算法 (Optical Flow)** 處理主體微調與移動時的框體隨動。
* **動態 UI 繪製**：

  * 使用 **Jetpack Compose Canvas**。
  * `animateFloatAsState` 處理「夾爪」縮小動畫。
  * `animateColorAsState` 處理輝光顏色切換。

### **2.2 光影即時優化 (Real-time Light Optimization)**

* **AE/AF 鎖定機制**：透過 `CameraControl.startFocusAndMetering` 鎖定指定區域的曝光與對焦，並透過 Compose 狀態機切換至「黃色鎖定狀態」（`isAEAFLocked = true`）。此模式下座標固定，不隨主體位移。

### **2.3 isFocusIndicatorLocked 規格（v2.12.0-12 確立）**

```kotlin
// CameraState.kt — 正確定義
// AE/AF long-press lock ONLY. Eye-AF success (afState=Locked) uses Cyan, NOT Yellow.
val isFocusIndicatorLocked: Boolean
    get() = isAEAFLocked
```

* **正確**：`isAEAFLocked`（長按/鎖定圖示 → Yellow）
* **錯誤（已修正）**：`isAEAFLocked || afState is AfState.Locked`（會把 Eye-AF 成功誤判為 Yellow）

### **2.4 FocusIndicator 雙視覺路徑分離（v2.12.0-13 確立）**

v2.12.0-13 將 Cyan 與 Yellow 路徑在 Compose 層完全解耦，引入 `isEyeAfLocked` 獨立參數。

```kotlin
// CameraScreen.kt — 呼叫端
FocusIndicator(
    isLocked       = state.isFocusIndicatorLocked,  // Yellow 路徑（AE/AF Lock）
    isEyeAfLocked  = state.afState is AfState.Locked, // Cyan 路徑（Eye-AF 成功）
    // ...
)
```

**Alpha 優先序（不可更改）**：
```
isLocked(Yellow, 1.0f)
  → isEyeAfLocked(Cyan, 1.0f)
    → isFading(Dwell fade, 0.4f)
      → isTracking(Cyan tracking, 1.0f)
        → else(1.0f)
```

**四條嚴格語義守則**：
1. `isLocked`（Yellow）= `state.isFocusIndicatorLocked` = `isAEAFLocked`，絕不包含 `afState is Locked`
2. `isEyeAfLocked`（Cyan）= `state.afState is AfState.Locked`，嚴格對應 Eye-AF 成功狀態
3. `showCornerBrackets`（機械夾爪）= `focusSuccess == true || isLocked || isEyeAfLocked`
4. UI Composable **禁止**自行寫 `||` 組合 lock 判斷邏輯；所有衍生狀態由 `CameraState` 提供

### **2.5 Release/Debug 幾何對齊要求（v2.12.0-14 確立）**

`app/build.gradle.kts` 的 `USE_PREVIEWVIEW_DISPLAY` 必須在 release 與 debug 均為 `true`。

| Build | 值 | 渲染路徑 | 幾何 |
|-------|---|---------|------|
| debug | `true` | PreviewView + FIT_CENTER | ✅ 正確 |
| release（v2.12.0-14 前）| `false` | Bitmap + ContentScale.Crop | ❌ 幾何錯誤 |
| release（v2.12.0-14 後）| `true` | PreviewView + FIT_CENTER | ✅ 正確 |

---

## **3. 開發藍圖 (Feature Roadmap)**

### **階段一：視覺框架與手勢 (V1.0 - 當前重點)**

* 實現 NeuralFocusRing 的「機械夾爪」動畫。
* 建立「長按」與「圖示點擊」雙觸發鎖定邏輯。
* 建立後置鏡頭與前置鏡頭的狀態區分邏輯。

### **階段二：動態適配與多點追蹤 (V2.0)**

* 根據 ML Kit 回傳的 Rect 動態調整 Canvas 繪製框的長寬比。
* 實作 **光流追蹤邏輯**，確保偵測框在主體移動時保持黏合感。
* 實現多人臉對焦框的層次管理 (Alpha 多階處理)。
