# CSO 裁決簡報 — BHA-01 Hard Gate 驗測報告
**日期**: 2026-05-11  
**測試裝置**: Sony XQ-BT52  
**Build**: feature/cueshot-mvp @ ea8d5e7  
**呈報人**: Local Engineer

---

## 一、執行摘要

BHA-01-FIX（rotation-aware 歸一化修復）**數學邏輯已驗證正確**，但實機視覺驗收發現 3 個前置 Bug，導致 T02 Hard Gate 視覺部分 FAIL。

| 項目 | 結果 |
|------|------|
| 歸一化數學（Logcat Gate） | ✅ PASS — 全場景 normBbox [0,1] 無溢位 |
| 視覺 Hard Gate | 🔴 FAIL — 3 Bug 阻塞 |
| Traycer 終審條件 | 🔴 未達標，需先修復 3 Bug |

---

## 二、四場景驗測結果

### 場景一：直立拍攝（Portrait）
- **Logcat**: rotation=0，normBbox x=0.31 y=0.44 w=0.29 h=0.22 ✅
- **視覺**: Halo 跟隨人臉左側/中央，無偏移 ✅
- **判定**: ✅ PASS

### 場景二：橫向拍攝（Landscape）
- **Logcat**: rotation=0（Sony 恆為 0），normBbox 全部 [0,1] ✅
- **視覺**: 🔴 Halo 忽大忽小
- **根因**: `BUG-MLKIT-DUAL-EMIT` — 切換方向後雙管線交替輸出
- **判定**: Logcat ✅ / 視覺 🔴 FAIL

### 場景三：PTF 模式切換
- **Logcat**: `ptf normBbox x=0.41 y=0.48 w=0.33 h=0.25` ✅
- **視覺**: 🔴 PTF 圓環過大且完全不跟隨人臉
- **根因**: `BUG-PTF-HALO-SIZE` — 渲染層 bbox→圓半徑轉換錯誤
- **判定**: Logcat ✅ / 視覺 🔴 FAIL

### 場景四：前鏡頭（Front Camera Mirroring）
- **Logcat**: rotation=0，normBbox [0,1] ✅；但同毫秒兩筆不同 bbox 交替
- **視覺**: 🔴 Halo 重複出現又消失（每幀閃爍）
- **根因**: `BUG-MLKIT-DUAL-EMIT` — 後鏡頭 DetectionResult 未清除，與前鏡頭交替發射
- **判定**: Logcat ✅ / 視覺 🔴 FAIL

---

## 三、發現的前置 Bug（非 BHA-01-FIX 引入）

### BUG-MLKIT-DUAL-EMIT（P0）
- **現象**: 切換鏡頭或方向後，Halo 忽大忽小 / 重複出現消失
- **Logcat 證據**:
  ```
  20:52:51.431 face[0] x=0.308 w=0.294  ← 後鏡頭殘留（固定值）
  20:52:51.432 face[0] x=0.102 w=0.550  ← 前鏡頭真實偵測
  ```
  同一毫秒內兩筆不同 bbox 交替，觸發規則引擎不一致 → Halo 閃爍
- **根因**: 切換鏡頭時舊鏡頭 DetectionResult Flow 未取消訂閱
- **修復方向**: `CameraViewModel` 切換時執行 `_state.update { copy(detectionResult = null) }`，或將訂閱改為 `flatMapLatest`
- **影響場景**: 場景二（橫向）、場景四（前鏡頭）

### BUG-SONY-ROTATION-CONST（P0）
- **現象**: Sony XQ-BT52 直立/橫向皆回報 `rotationDegrees=0`
- **Logcat 證據**:
  ```
  直立: raw=480x640 rotation=0 effective=480x640
  橫向: raw=480x640 rotation=0 effective=480x640  ← 應為 rotation=90
  ```
- **根因**: 裝置 sensor 為 portrait-native，CameraX ImageAnalysis 未注入正確 rotation
- **影響**: BHA-01-FIX 的 rotation swap 邏輯在此裝置永遠不觸發（rotation≠90/270）
- **修復方向**: 改用 `imageInfo.rotationDegrees`（若已是此用法需查 CameraX 設定），或改用 `display.rotation` 轉換為角度
- **注意**: 需在 Pixel 9a 確認 rotation 行為，判斷是否僅 Sony 特殊

### BUG-PTF-HALO-SIZE（P0）
- **現象**: PTF 模式下圓環過大，且不跟隨人臉位置移動
- **Logcat 確認**: PTF pipeline 座標正確（`ptf normBbox` 全部 [0,1]）
- **根因**: 渲染層（`HaloGuidanceLayer` 或 PTF overlay）將 bbox w/h 誤用為圓半徑，而非計算 bbox 對角線或中心點半徑
- **修復方向**: 追蹤 PTF Halo 渲染函數，修正半徑計算公式

---

## 四、CSO 裁決請求

| 項目 | 建議 | 請 CSO 確認 |
|------|------|------------|
| BHA-01 歸一化修復 | ✅ 數學正確，維持現有 commit | □ 確認 |
| BUG-MLKIT-DUAL-EMIT | 優先修復，解除場景二/四阻塞 | □ 授權 T-MLKIT-FIX |
| BUG-SONY-ROTATION-CONST | 需 Pixel 9a 對照測試後決定修復範圍 | □ 授權測試 / □ 延後 |
| BUG-PTF-HALO-SIZE | PTF 核心功能阻塞，需修復 | □ 授權 T-PTF-RENDER-FIX |
| T02 Hard Gate 重驗 | 3 Bug 修復完成後重跑四場景 | □ 同意流程 |

---

## 五、建議執行順序

```
1. BUG-MLKIT-DUAL-EMIT 修復（CameraViewModel 鏡頭切換清空邏輯）
   → 場景二、四視覺重驗
2. BUG-SONY-ROTATION-CONST 調查（Pixel 9a 對照 + CameraX rotation 來源）
   → 決定是否需要裝置適配層
3. BUG-PTF-HALO-SIZE 修復（渲染層半徑公式）
   → 場景三 PTF 視覺重驗
4. T02 Hard Gate 全場景重驗 → PASS 後解除 T03 blocked
```
