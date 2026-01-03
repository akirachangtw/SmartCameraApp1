# 每日開發操作 SOP（Hybrid Workflow）
## ChatGPT × VSCode with Claude Code

本文件為「混合工作流」的一頁版每日操作指引，設計用於在 **VSCode with Claude Code / Codex** 下快速查閱與即時套用。

---

## 核心原則（每日只需記住這 4 句）
1. 想改任何東西 → **一定先用 ChatGPT 整理**
2. 要不要動程式碼 → **只問 Claude Code**
3. Claude Code 說不可行 → **不硬改**
4. 所有實作 → **必須可驗證、可回滾**

---

## 每日固定流程（照順序執行）

### Step 1｜啟動規劃（必做）
**觸發時機**
- 新功能構想
- Bug 修正
- 行為異常排查
- 架構 / 模式 / 效能相關調整

**行動（ChatGPT）**
- 說明目前狀況與目標
- 產出：
  - 目標（Goal）
  - 影響範圍（Scope）
  - 潛在風險（Risks）
  - 是否需要可行性檢視（Yes / No）

> 此階段 **不寫任何程式碼**

---

### Step 2｜落盤成文件（必做）
**行動**
- 建立 Issue（使用 Issue Template）
- 或新增 Markdown（`docs/plan/*.md`）

**目的**
- 建立單一事實來源（SSOT）
- 避免決策只存在對話中

---

### Step 3｜可行性檢視（高風險必做）
**自動觸發條件（任一成立）**
- CameraX / Preview / ImageAnalysis
- 模擬器 vs 實體手機
- FPS / Memory / CPU / GPU
- 模式切換（Live / Static / Debug）
- 修改超過 1–2 個檔案

**行動（Claude Code）**
- 讀取 repo 現況
- 判斷是否可行
- 指出高風險模組
- 建議是否進入實作

**輸出只能是**
- 可行
- 不可行（附原因）
- 可行但需調整

---

### Step 4｜實作關卡（嚴格）
**只有同時符合以下條件才可進入**
- Claude Code 判定可行
- 變更範圍明確（最小 diff）
- 驗證方式已定義（build / run / device）

不滿足 → 回到 Step 1 或 Step 3

---

### Step 5｜實作與驗證
**行動（Claude Code）**
- 小步提交（atomic commits）
- Build / 測試
- 必要時實體手機驗證
- 填寫 PR Template：
  - What changed
  - How verified
  - Risk & rollback

---

### Step 6｜結案
- 合併 PR
- Issue 標記完成
- 文件保留以利回溯

---

## 快速速記版（只看這段也能用）
- 想改 → ChatGPT
- 能不能寫 → Claude Code
- 不可行 → 不寫
- 寫了 → 能驗證、能回滾

---

## 建議放置位置
- 專案根目錄：`DAILY_DEVELOPMENT_SOP.md`
- 或 `docs/workflow/DAILY_DEVELOPMENT_SOP.md`

---

本 SOP 目的：
確保每一次修改 **可解釋、可驗證、可回溯**，
避免「模擬器正常、實機翻車」與高風險回歸問題。
