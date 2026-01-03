# Hybrid Workflow MVP（ChatGPT × Claude Code）

此 MVP 透過 Python 腳本呼叫 OpenAI **Responses API** 生成：
- Level 1 規劃摘要（L1）
- Level 2 可行性檢視備忘錄（L2）
- PR Notes 草稿（PR）

## 先決條件
1. 安裝 OpenAI Python SDK（官方）  
   `pip install openai`
2. 設定環境變數（在你開發機）
   - `OPENAI_API_KEY=...`
   - 可選：`OPENAI_MODEL=gpt-5.2`

## VSCode 使用方式（已提供 tasks.json）
- 執行 Task：
  - `Hybrid: Generate L1 Plan`
  - `Hybrid: Generate L2 Feasibility`
  - `Hybrid: Generate PR Notes`
- 生成檔案會寫入：`docs/plan/<topic>_L1.md` / `<topic>_L2.md` / `<topic>_PR.md`

## CLI 使用方式
```bash
python tools/hybrid_flow.py --level L1 --topic camera_static_mode --out docs/plan
python tools/hybrid_flow.py --level L2 --topic camera_static_mode --out docs/plan
python tools/hybrid_flow.py --level PR --topic camera_static_mode --out docs/plan
```

## 注意事項
- 腳本會自動附上精簡的 `git status` 與 `git diff`（截斷）作為上下文（可用 `--no_git` 關閉）。
- 建議把決策輸出 commit 進 Git，供 Claude Code 檢視與落地。
