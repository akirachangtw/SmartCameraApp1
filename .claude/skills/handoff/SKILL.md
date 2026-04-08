---
disable-model-invocation: false
---
# Description
生成對話交接包 (Handoff Package)，儲存至 `docs/handoff/`，讓下一個 Claude Code 會話能立即重載 Context，無需重讀歷史對話。

# Prompt
我們需要切換到新的 Claude Code 對話視窗以重置 Context Window。

請立即執行以下步驟，生成一份「交接提示詞 (Handoff Prompt)」並自動儲存：

### 儲存路徑
```
docs/handoff/HANDOFF_YYYYMMDD_HHMM.md
```
（請自動帶入當下的日期與時間，格式例如：`SESSION_HANDOFF_20260320_1430.md`）

---

### 必要章節結構

```markdown
## 1. 專案背景 (Context)
[一段話描述：這個專案是什麼、我們正在解決的核心問題]

## 2. 語言規範 (Language Rules)
- **程式碼 / 腳本**：英文（變數名稱、函式名稱、Log 訊息、程式碼註解）
- **對話溝通**：繁體中文（說明、討論、問題回報）

## 3. 目前狀態 (Current Status)
✅ 已完成（僅列出經 `./gradlew assembleDebug` 或實機驗證的事實）：
- [已完成項目]

🔧 本次解決的關鍵問題：
- [問題描述與修復方式]

⚠️ 已知但尚未處理的問題（已記錄至 Backlog）：
- [問題描述]

## 4. 最新交付成果 (Latest Deliverable)
> 優先記錄修改過的檔案路徑與關鍵變更摘要。
> 若單一檔案不超過 50 行，可附上完整程式碼。
> **禁止**使用 `// ... rest of code` 省略。

### 修改的檔案清單
| 檔案路徑 | 變更摘要 |
|----------|---------|
| `app/src/.../Foo.kt` | [新增 / 修改 / 刪除了什麼] |

### 關鍵程式碼片段（僅限必要）
[若有需要交接的核心邏輯，貼出完整片段]

## 5. 下一步行動 (Next Steps)
新對話中立即執行的具體任務（含目標檔案路徑與指令）：
1. [具體任務描述]
2. [...]
```

---

### 使用說明（給下一個 Claude Code 會話）

**交接前必確認**（避免填錯資訊）：
```bash
git branch --show-current   # 確認目前 branch 名稱
git log --oneline -1        # 確認最後一個 commit hash 與說明
```

新對話開頭請執行：
1. 直接貼上 `docs/handoff/HANDOFF_YYYYMMDD_HHMM.md` 的全文
2. 同時附上 `memory/MEMORY.md` 與相關 `devspec/feature_record_Backlog.md` section
3. 禁止從記憶口述——以當前倉庫檔案為依據重載 Context

---

**現在請生成交接包並寫入 `docs/session_handoff/` 目錄。**
