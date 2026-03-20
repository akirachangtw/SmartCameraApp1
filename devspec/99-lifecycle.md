# 對話生命週期管理 (Conversation Lifecycle)

> **定位**: 本文件定義 Claude Code CLI 對話生命週期管理標準，為本專案的 SSOT (Single Source of Truth)。
> **專案**: AI 時尚穿搭顧問 (ai_stylist) — Flutter + Firebase
> **最後更新**: 2026-03-20

---

## 1. 目的 (Objective)

為避免觸發 Claude Code 的 Session Limit 及 Token 消耗過度，並確保 AI 在長對話中保持邏輯清晰，開發者必須主動管理對話生命週期。**「適時開啟新對話」是保持高效率與準確度的關鍵。**

---

## 2. 重置觸發條件 (Reset Triggers)

遇到以下任一情況，立即執行交接程序並開啟新對話：

| 觸發條件 | 訊號 | 動作 |
|---------|------|------|
| **系統警告** | 介面顯示剩餘訊息提示（如 "10 messages remaining"）| 立即停止新工作，準備交接 |
| **任務轉換** | 一個階段完成，下一階段工作類型不同（開發 → Debug，Debug → 文件，實作 → Review）| 壓縮當前狀態後重啟 |
| **邏輯衰退** | AI 開始忘記前面的守則、與已確定決策矛盾、或產生幻覺 | 停止信任對話記憶，從倉庫真相重建 |
| **大型重構後** | 程式碼大規模變動，舊 Context 已不再準確描述現況 | 以當前檔案為基礎建立新交接 |

> **決策規則**：任一觸發條件成立即執行。不要等到多個症狀同時出現。**早重置比從壞 Context 恢復代價低。**

---

## 3. 交接程序 (Handoff Procedure)

觸發時，執行 `/handoff`，生成交接包並儲存至：

```
docs/session_handoff/SESSION_HANDOFF_YYYYMMDD_HHMM.md
```

**必要章節**：

```markdown
## 1. 專案背景 (Context)
正在解決的問題（一段話）

## 2. 語言規範 (Language Rules)
- 程式碼 / 腳本：英文（變數、註解、Log）
- 對話溝通：繁體中文

## 3. 目前狀態 (Current Status)
- ✅ 已完成項目（僅列出經 flutter analyze / flutter build 驗證的事實）
- 🔧 解決的關鍵錯誤
- ⚠️ 已知但待處理的問題（已記錄至 Backlog）

## 4. 最新交付成果 (Latest Deliverable)
> 優先記錄修改的檔案路徑清單 + 關鍵變更摘要。
> 若單一檔案不超過 50 行，可附上完整程式碼。
> 禁止使用 `// ... rest of code` 省略。

| 檔案路徑 | 變更摘要 |
|----------|---------|
| `lib/xxx/yyy.dart` | [新增 / 修改 / 刪除了什麼] |

## 5. 下一步行動 (Next Steps)
新對話中立即執行的任務（具體指令，含目標檔案路徑）
```

**使用說明**：
- 新對話開頭，直接貼上 `docs/session_handoff/SESSION_HANDOFF_YYYYMMDD_HHMM.md` 全文
- 同時附上 `.claude/CLAUDE.md`，確保 AI 載入最新守則
- 禁止從記憶口述——以當前倉庫檔案為依據重載 Context

---

## 4. 交接原則 (Handoff Rules)

1. **倉庫真相優先於對話記憶**：交接內容以當前實際檔案為依據，不要從記憶中複述可能已過時的狀態。
2. **已驗證 vs 猜測嚴格分開**：只有被 `flutter analyze`、`flutter build`、或實機測試確認的事實才進入「已完成」欄位。
3. **交接包要足夠緊湊**：讓全新的 Claude Code 會話能快速重載 Context，不需要重讀整個歷史。
4. **程式碼以路徑 + 摘要為主**：優先記錄「修改的檔案路徑 + 關鍵變更摘要」；完整程式碼僅在單檔不超過 50 行時附上，避免新會話 Token 過度消耗。

---

## 5. 相關文件

| 文件 | 用途 |
|------|------|
| `devspec/99-handoff-prompt.md` | `/handoff` Skill 的手動參考提示詞 |
| `.claude/skills/handoff/SKILL.md` | Claude Code `/handoff` Skill 定義 |
| `chat_logs/` | 交接記錄儲存目錄（含 `.gitkeep` 確保目錄被 Git 追蹤）|
| `.claude/CLAUDE.md` | AI 代理最高行為準則（新對話必須附上）|
