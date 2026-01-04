# **Stitch with Google 設計執行導引：AI 智能攝影師專案**

使用 Stitch with Google 進行初步設計，能確保您的 APP 在具備「未來感」的同時，保有 Android 系統原生的流暢度與易用性。以下是將 ui\_ux\_design\_proposals\_zh-TW.md 中的視覺概念轉化為 Stitch 原型的步驟。

## **1\. 基礎架構配置 (System Setup)**

在 Stitch 中啟動專案時，請先設定符合 **Neural Lens** 主題的基礎參數：

* **選用 Material 3 範本**：Stitch 深度整合 M3 規範，這能確保您的按鈕、間距與系統行為符合 Android 最佳實踐。  
* **動態色彩 (Dynamic Color) 設定**：  
  * 將「極光藍 (\#00F2FF)」設定為 **Primary (主色)**。  
  * 將「電子紫 (\#7000FF)」設定為 **Tertiary (三次色)**。  
  * 在 Stitch 的色盤設定中，開啟「高對比度」選項，以符合未來感的視覺張力。

## **2\. 關鍵組件的 Stitch 實作路徑**

### **A. 玻璃擬態層 (Glassmorphism Layers)**

Stitch 支援自定義 Surface 性質。

* **操作**：在 Stitch 的 **Surface Component** 中，將 Opacity 設定為 20%\~40%，並在 Background Blur (背景模糊) 屬性中設定為 16dp 或更高。  
* **建議**：將此層級用於「浮動式數據疊層 (HUD)」，確保其背景能隱約透出相機預覽流。

### **B. 智能快門環 (The Neural Ring)**

利用 Stitch 的 **Interaction States** 進行設計。

* **狀態一 (Default)**：使用 Material 3 的 Filled Floating Action Button 作為基礎，設定為半透明。  
* **狀態二 (Focus/Active)**：在 Stitch 的動態屬性中新增一個環形 Border 動畫，將顏色轉向「金黃色」，代表 AI 已鎖定最佳拍攝點。

### **C. 數據字體與標籤**

* **字體選擇**：Stitch 內建 Google Fonts。請將 Display 字體設為 **Google Sans** (簡潔現代)，將數值顯示字體設為 **Roboto Mono** (科技感)。

## **3\. UX 交互行為預演 (Prototyping)**

在 Stitch 中，您可以預先設定以下關鍵交互：

1. **動態模糊警示 (Motion Shield)**：  
   * 設定一個全螢幕的半透明紅色 Overlay。  
   * 使用 Stitch 的 Transition 工具設定為「快速淡入 (Rapid Fade-in)」，模擬當陀螺儀偵測到劇烈晃動時的視覺反饋。  
2. **磁吸感構圖 (Magnetic Composition)**：  
   * 在 Stitch 畫布上設定 AR 引導線。  
   * 利用 Snap-to-guide 效果模擬使用者將相機對準建議位置時的「磁吸力」視覺回饋。

## **4\. 從 Stitch 到 Android 的產出 (Export & Implement)**

Stitch 最大的優點是能產出開發可用的數據：

* **Design Tokens**：從 Stitch 匯出 JSON 格式的色盤與字體定義。這些數據可以直接對應到 Android Studio 專案中 ui.theme 資料夾下的 Color.kt 與 Type.kt。  
* **Compose 參考碼**：Stitch 生成的佈局間距 (Margin/Padding) 與 Material 3 元件參數，可以直接轉化為 **Jetpack Compose** 的程式碼實作，減少 UI 誤差。

## **5\. 設計檢查清單 (Review Checklist)**

* \[ \] **單手操作性**：快門鈕與模式切換器是否位於手機下三分之一區域？  
* \[ \] **視覺對比度**：在亮色背景（如晴天風景預覽）下，極光藍的 UI 文字是否依然清晰？  
* \[ \] **AR 遮擋**：當 AI 顯示多重物件偵測框時，是否遮擋了畫面中央的主體？（Stitch 建議：使用超細線框）。

透過 Stitch with Google 的輔助，您能在第一天就看到一個具有「Google 風格但帶有未來感」的 APP 雛形，這對於後續開發階段的介面實作將有極大幫助。