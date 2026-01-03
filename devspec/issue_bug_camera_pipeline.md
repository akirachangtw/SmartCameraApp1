# Bug Report — Camera / Preview / Pipeline

> **工作流級別**: Level 1 (規劃檢視) → 填寫此 Issue 即完成 Level 1
>
> 如觸發 Level 2 條件(見下方檢查清單),需進行可行性評估後才能實作

---

## 問題描述（What happened）
請描述實際觀察到的行為。

## 預期行為（What expected）
請描述預期正確行為。

---

## 發生環境（Environment）
- 裝置：
- Android 版本：
- App 版本 / Commit：
- 模式：
  - ☐ Live Camera
  - ☐ Static Image（測試圖片）

---

## 重現步驟（Steps to reproduce）
1.
2.
3.

---

## 初步風險判斷（Checklist）

### 現象分類
- ☐ Preview 無畫面 / 黑畫面
- ☐ ImageAnalysis 無資料
- ☐ 模式切換後異常
- ☐ 僅模擬器正常，實機異常
- ☐ 其他：

### ⚠️ Level 2 升級條件檢查

**如勾選任一項，必須先完成 Level 2 可行性評估**:
- ☐ 涉及 CameraX / Preview / ImageAnalysis
- ☐ 涉及模擬器 vs 實體裝置差異
- ☐ 涉及 FPS / Memory / CPU / GPU
- ☐ 涉及模式切換 (Live / Static / Debug)
- ☐ 修改兩個以上檔案
- ☐ 非純 UI / 文案調整

**Level 2 結論** (可行性評估後填寫):
- ☐ 可行 → 可進入 Level 3 實作
- ☐ 需調整 → 調整方案: ___________
- ☐ 不可行 → 原因: ___________

---

## 補充資料
- Logcat / 截圖 / 影片
