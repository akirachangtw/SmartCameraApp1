---
name: wktree-close
description: 完成議題／合併 worktree／查 worktree 狀態。當使用者說「收尾這個 worktree」「合併這個 topic」「finish worktree」「worktree 狀態如何」時觸發。
---

# wktree-close

收口 `git worktree` — 對帳狀態、合併回主分支、清理。**MUST NOT** 承擔任何 worktree 建立職能（見 D1/D2，建立全交官方 `claude --worktree <topic>`）。

## 查狀態

```
bash .claude/skills/wktree-close/scripts/wktree-close.sh status
```

以 `git worktree list --porcelain` 為底對帳 `.claude/wktrdata/PROGRESS.md`（非版控，衍生視圖）。三層模型：`.claude/PROGRESS.md` 為人維護 sprint dashboard（受版控），腳本**永不寫入**；`.claude/wktrdata/`（gitignore 排除）承載全部機器對帳與交接資料。`worktrees/` 為原廠 worktree 本體目錄，本專案**零寫入**；一切治理資料落 `wktrdata/`，二者 **MUST NOT** 混寫。
Path 欄顯示 `-` 表示該 topic 之 worktree 已被觀測到消失；同名重建（含官方預設同路徑）後再跑 `status`，該列即復活為 `active` 並回填 live 值。

## 收尾（finish）

1. 先不帶 `--yes` 執行 `wktree-close.sh finish <topic>`，看預覽（log/diff stat/VERIFY 狀態）。
2. **MUST** 在對話中向使用者確認後，才可帶 `--yes` 執行：
   ```
   bash .claude/skills/wktree-close/scripts/wktree-close.sh finish <topic> --yes
   ```
3. **MUST** 先確認 `.claude/wktrdata/<topic>/handoffs/` 內存在非空之最新時戳檔——缺 → 先觸發 `wktree-compact` Skill 產出交接檔，**MUST NOT** 跳過（腳本層⑤閘亦會機械擋下，此為 SKILL 層提前提示）。
4. D5：`finish` 僅能於**主 checkout session** 執行；強制力來源＝腳本閘①（`git rev-parse --git-common-dir` 比對），非 Claude Code sandbox——sandbox 阻擋主張已於 2026-08-14 現地證偽（AI-0 動作 B），**MUST NOT** 宣稱「會被 sandbox 擋下」，正確措辭為「腳本閘①會拒絕並指引回主 checkout」。若於 worktree session 內誤呼叫，依此指引改到主 checkout 執行，**MUST NOT** 繞道。
5. 目標分支機械鎖定（閘⓪，置於全部閘之首）：`finish` 只允許整併到解析出的目標主分支（`WKTR_MAIN_BRANCH` env → `origin/HEAD` → 字面 `main` 三層 fallback），當前分支不符即 die；**MUST NOT** 隱式以「當前所在分支」代入整併目標。

## 失敗處置

- `merge_blocked`：腳本已自動 `git merge --abort` 並標記狀態，衝突檔清單見輸出；**MUST NOT** 盲目重試，需人工解衝突後再收尾。
- 任何前置閘失敗：依腳本輸出訊息處理，**MUST NOT** 繞過閘門手動執行 `git merge`。
