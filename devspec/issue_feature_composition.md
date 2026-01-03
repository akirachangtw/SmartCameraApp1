# Feature Request — Composition / Guidance

> **工作流級別**: Level 1 (規劃檢視) → 填寫此 Issue 即完成 Level 1
>
> 如觸發 Level 2 條件(見下方檢查清單)，需進行可行性評估後才能實作

---

## 功能目標（Goal）
請描述此功能想解決的問題。

---

## 使用場景（Scenario）
- ☐ 人像
- ☐ 風景
- ☐ 夜景
- ☐ 街拍 / 建築
- ☐ 其他：

---

## 涉及構圖或引導（Checklist）
- ☐ 三分法
- ☐ 頭部空間
- ☐ 對稱
- ☐ 引導線
- ☐ 填滿構圖
- ☐ AI 提示文字

---

## 影響範圍
- ☐ Overlay UI
- ☐ 分析邏輯
- ☐ 效能（FPS / 延遲）
- ☐ 相機管線

---

## ⚠️ Level 2 升級條件檢查

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

## 驗收條件（Acceptance criteria）
- ☐ UI 提示正確
- ☐ 不影響既有構圖邏輯
- ☐ 實體裝置驗證通過（型號: _______）
- ☐ FPS 無明顯下降（≥ ______ fps）
