# **Pixel 9 攝影機制：對焦追蹤 vs. 鎖定對焦與曝光 (AE/AF Lock)**

在 Pixel 9 的專業相機系統中，對焦與曝光的處理分為兩個核心階段：自動追蹤與強制鎖定。以下是兩者的簡述與深度差異分析。

## **1\. 功能簡述**

### **A. 對焦追蹤 (Focus Tracking / Motion Focus)**

* **定義**：當使用者在預覽畫面中點擊一個特定物體（如移動中的寵物、行進中的車輛）時，相機會識別該物體的特徵並在其移動時持續調整焦點。  
* **運作機制**：利用 **Optical Flow (光流法)** 與 AI 視覺模型，即時計算物體在二維平面上的位移，並連動鏡頭音圈馬達（VMC）進行連續對焦。  
* **視覺表現**：對焦框（通常為白色或青色圓圈/方框）會隨著主體在畫面上「黏著」移動。

### **B. 鎖定功能 (AE/AF Lock & Controls)**

* **定義**：使用者透過「長按主體」或「點擊鎖定圖示」來固定目前的對焦距離與曝光參數。  
* **運作機制**：  
  * **AF Lock**：將對焦馬達固定在當前距離，不再隨畫面變化。  
  * **AE Lock**：固定感光度（ISO）與快門速度，防止環境光線變化導致畫面忽明忽暗。  
* **視覺表現**：對焦框通常會變色（如變為黃色），並出現一個**鎖頭圖示**，同時側邊會彈出亮度與陰影的調節滑桿（即所謂的「對話/控制介面」）。

## **2\. 核心差異對照表**

| 比較維度 | 對焦追蹤 (Tracking) | 鎖定功能 (AE/AF Lock) |
| :---- | :---- | :---- |
| **主體行為** | 主體是動態的，位置不固定。 | 主體與背景相對靜止，或需固定特定氛圍。 |
| **參數狀態** | **動態更新**：隨距離改變焦點。 | **靜態固定**：參數被強制鎖死。 |
| **觸發方式** | 單擊主體。 | 長按主體 或 點擊控制條上的鎖頭。 |
| **視覺符號** | 隨動的框線、呼吸感特效。 | 固定位置的黃色框線、鎖頭圖示、調節滑桿。 |
| **光影表現** | 隨場景自動測光。 | 使用者手動干預亮度/對比。 |
| **中斷機制** | 主體移出畫面或點擊背景。 | 再次點擊解鎖 或 按下快門後釋放。 |

## **3\. 總結**

* **對焦追蹤**是為了「捕捉」，解決的是**動態準確度**問題。  
* **鎖定對話**是為了「創作」，解決的是**光影主導權**問題。

在 Pixel 9 的體驗中，兩者相輔相成。追蹤確保主體清晰，鎖定則讓攝影師能精確控制照片的調性（如低調照片或剪影效果）。

---

## **4\. SmartCameraApp 實作對照與行為驗證（v2.11.4-01, 2026-02-22）**

### **4.1 對焦追蹤 (Tap → Focus Tracking)**

| 項目 | Pixel 9 原生行為 | SmartCameraApp 現況 | 一致性 |
| :---- | :---- | :---- | :---- |
| 觸發 | 單擊主體 | 單擊主體 → `focusOnPoint()` | ✅ |
| 追蹤機制 | Optical Flow + AI | ML Kit Face/Object Detection + OF | ✅ |
| 視覺表現 | 框跟隨主體移動 | Cyan 框跟隨（StiffnessHigh spring） | ✅ |
| 框尺寸 | 隨主體 bounding box 縮放 | **固定尺寸**（不隨 bbox 改變） | ❌ 設計差距 |
| 多目標 | 多框共存 | **單一框**（最高 saliency 候選者） | ❌ 設計差距 |

### **4.2 AE/AF Lock (Long Press)**

| 項目 | Pixel 9 原生行為 | SmartCameraApp 現況 | 一致性 |
| :---- | :---- | :---- | :---- |
| 觸發 | 長按主體 | 長按主體 → `lockAEAF()` | ✅ |
| AF Lock | 對焦馬達固定在當前距離 | CameraX `FLAG_AF` 持久鎖定（數秒後自動退回 PASSIVE_FOCUSED） | ⚠️ 部分 |
| AE Lock | 固定 ISO + 快門速度 | **未生效**：`FLAG_AE` 只設 metering region，`AE_LOCK` 全程 false | ❌ Bug |
| 不追蹤物件 | 鎖定後不追蹤定點及物件 | 停止 OF + ML Kit tracking，框固定 | ✅ |
| 視覺表現 | 黃色框 + 鎖頭圖示 | 黃色框（無鎖頭圖示） | ⚠️ 部分 |
| 解鎖 | 再次點擊 / 快門後釋放 | 再次點擊 → `unlockAEAF()` | ✅ |
| 無 auto-cancel | 持續鎖定直到手動解鎖 | `FocusMeteringAction` 不設 auto-cancel | ✅ |

### **4.3 CameraX API 實作確認**

```
LiveCameraSource.lockAEAF():
- FocusMeteringAction.Builder(lockPoint, FLAG_AF | FLAG_AE)
- 不呼叫 setAutoCancelDuration() → 持久鎖定
- isFocusSuccessful 僅表示 AF 收斂品質，不影響 lock 是否生效 (v2.11.2 fix)
```

### **4.4 Logcat 驗證指令**

```bash
# AE/AF Lock 完整流程驗證
adb -s 59131JEBF17244 logcat -v time CameraViewModel:I LiveCameraSource:I *:S

# 預期 log 序列：
# 1. "Locking AE/AF at (x, y)"
# 2. "Optical flow tracking stopped for AE/AF lock"  (如果之前有追蹤)
# 3. "[v2.11.2] AE/AF lock applied at (x, y) - isFocusSuccessful=true/false"
# 4. "AE/AF locked successfully at (x, y)"
```

### **4.5 Logcat 實測結論（2026-02-22 Camera2 CaptureResult 驗證）**

| 驗證項目 | 預期 | 實際 | 結論 |
| :---- | :---- | :---- | :---- |
| `AE_LOCK=true` | 全程 true | **全程 false** | ❌ AE 未鎖定 |
| `AE_STATE=LOCKED` | 全程 LOCKED | SEARCHING / CONVERGED（持續調光） | ❌ 曝光自由變化 |
| `AF_STATE=FOCUSED_LOCKED` | 全程 | 數秒後退化為 PASSIVE_FOCUSED → PASSIVE_SCAN | ⚠️ AF 短暫後釋放 |
| ISO 不變 | 固定值 | 29 → 669（+2207%） | ❌ 未鎖 |
| exposure 不變 | 固定值 | 4ms → 60ms（+1400%） | ❌ 未鎖 |

**根本原因**：`FocusMeteringAction.FLAG_AE` ≠ Camera2 `CONTROL_AE_LOCK=true`。`FLAG_AE` 只設定測光區域，不凍結曝光參數。

**修正計畫**：見 [`v2.11.4_multi_focus_coexistence_L1.md`](v2.11.4_multi_focus_coexistence_L1.md) — Feature D（v2.11.4-02）。

修正方式：`FocusMeteringAction` 成功後，額外呼叫 `Camera2CameraControl.addCaptureRequestOptions(CONTROL_AE_LOCK=true)`。