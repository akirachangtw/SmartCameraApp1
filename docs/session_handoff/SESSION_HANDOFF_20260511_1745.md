# Cueshot Handoff — 2026-05-11 17:45

## Dashboard（新 session 先讀這段）
- **完成**: statusLine hook 整合完成 — `settings.json` 已指向 `.claude/skills/status/scripts/statusline.sh`（修正 typo `scritps→scripts` + 刪除舊 `.claude/statusline.sh`），cache 寫入路徑改為相對路徑 `tmp/claude_status_cache.json`
- **阻塞**: BHA-01-FIX 等待授權（P0）— T02 Hard Gate 仍 FAIL；statusLine hook 需重啟 Claude Code 後方可寫入真實 API 數據
- **下一步**: 授權後執行 BHA-01-FIX：`CameraViewModel.kt` line ~1982 `observeCompositionAdvice` + line ~2081 `observePtfAlignment` 加入 rotation swap（參考 line 585 `updateCoordinateTransformer` 現有模式）
- **帶入**: CLAUDE.md · PROGRESS.md · `.claude/settings.json` · `.claude/skills/status/scripts/statusline.sh` · `.claude/skills/status/SKILL.md`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `.claude/settings.json` | `statusLine.command` | 從 `bash .claude/statusline.sh` → `bash .claude/skills/status/scripts/statusline.sh` |
| `.claude/skills/status/scripts/statusline.sh` | 全檔（新建） | statusLine hook 腳本，CACHE_FILE 改為相對路徑 `tmp/claude_status_cache.json` + `mkdir -p` 保護 |
| `.claude/skills/status/SKILL.md` | Step 1 讀取路徑 | 更新為 `tmp/claude_status_cache.json`；Step 2 fallback 估算 |
| `.claude/statusline.sh` | — | 已刪除（原始錯誤位置，`D` in git） |
| `.claude/skills/status/scritps/` | — | typo 目錄已清除 |
| `sv_report.md` | BHA-01 段落 | 本次 session BHA-01 診斷報告更新 |
| `.claude/bha_report.md` | 全檔（新建） | BHA-01 完整診斷報告 |
| `.claude/bha_cso_report.md` | 全檔（新建） | BHA-01 CSO 簡報版 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| statusline.sh 放在 `.claude/skills/status/scripts/` | 與 `cross_review.sh` 在 `sv/scripts/` 的位置一致，skill 封裝原則 | `settings.json` command path |
| CACHE_FILE 改用相對路徑 | `git rev-parse --show-toplevel` 在 Windows Git Bash 若失敗會 fallback 到 `/tmp/`（根目錄），相對路徑更可靠，hook 永遠從專案根執行 | `statusline.sh` line 4 |
| BHA-01 修復方案 A 確認 | rotation swap 邏輯已存在於 `updateCoordinateTransformer` line 585，複製同一模式到 1982/2081 最小風險 | `CameraViewModel.kt` 2 處 |
| statusLine hook 需重啟才生效 | Claude Code settings watcher 不熱重載 `statusLine.command`，本 session 中修改無法即時套用 | 下一 session 才驗證 🟢 真實數據 |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD --stat`（本 session 變更尚未 commit）

未 commit 的主要變更：
- `.claude/settings.json` — statusLine path 修正
- `.claude/skills/status/SKILL.md` — cache 讀取邏輯
- `sv_report.md` — BHA-01 診斷更新

新建未追蹤：
- `.claude/skills/status/scripts/statusline.sh`
- `.claude/bha_report.md`
- `.claude/bha_cso_report.md`

BHA-01 根因確認位置：
- `git log --oneline -5` → HEAD = `3f659cc`
- `CameraViewModel.kt` line 1982：`observeCompositionAdvice` — rotation swap 缺失
- `CameraViewModel.kt` line 2081：`observePtfAlignment` — rotation swap 缺失
- 參考修復模式：line 585 `updateCoordinateTransformer`

</details>
