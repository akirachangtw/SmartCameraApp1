# INFRA-STATUS-SYNC-01: status skill 單向同步（PLT→APP 位元組級部署，含 TELEMETRY-01 [relayed] 正向補證）

**簽發日期**: 2026-08-21
**簽發者**: Claude（SMTR 主 session；Akira 2026-08-21 裁決「同意以上裁決……單向 MCP 同步 APP」）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（純 .claude/ 文本部署票，比照 INFRA-TPL-V32-01／INFRA-GITIGNORE-WL-01 免 Traycer 先例；Codex 依 docs 型免審先例 N_A，CSO 獨立取證替代）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: INFRA-STATUS-CONTRACT-01、INFRA-STATUS-TELEMETRY-01（均 CLOSED 於 mcp-ticket-platform 冊，票面即部署內容之規格書）；ENV_DIFF_MATRIX_claude_dir_v1（`D:/DevSources/Skill-Advisor/deliver/`）
- `coordination_dependency`: INFRA-ENVSYNC-AUDIT-01（本票為 K-1 單點先行部署；全量同步待該票盤點回流，二者 ALLOWED 不相交可並行）

## 檔案治理／來源說明

ENV_DIFF_MATRIX v1.0 §4 K-1：兩端 `settings.json` 之 statusLine 註冊字面相同（`python .claude/skills/status/scripts/statusline.py`），但 skill 內容已分叉——PLT 端已落 CONTRACT-01（資料權威分層／佔比定位／已知取證缺口節／docstring 分母敘事更正）與 TELEMETRY-01（快取齡／checkpoint jsonl／Δtoken）；APP 端為舊版（SKILL.md 3427 bytes，2026-08-21 實測）。

**同步原則（Akira 裁決）**：單向 PLT→APP，位元組級複製，禁在地改編。矩陣 §8 對照已於簽發時完成（見「矩陣對照」節）。

**附帶價值**：本票行為性 AC 即 TELEMETRY-01 收束時登記之 [relayed] 保留項（checkpoint jsonl 證據檔 NOT_FOUND）之天然正向補證通道——APP 端首次 /status 實跑成功即同時消解該保留。

**源端字面釘死**：
- 源 A：`D:/DevSources/workflow/mcp-ticket-platform/.claude/skills/status/SKILL.md`（末次功能異動 commit `aa3e749`）
- 源 B：`D:/DevSources/workflow/mcp-ticket-platform/.claude/skills/status/scripts/statusline.py`（末次功能異動 commit `b787bd2`）
- 靶 A：`.claude/skills/status/SKILL.md`（APP，覆寫）
- 靶 B：`.claude/skills/status/scripts/statusline.py`（APP，覆寫）

## Verify Config
TICKET_ID=INFRA-STATUS-SYNC-01
CREATED=2026-08-21
PRESET=docs
ALLOWED=.claude/skills/status/SKILL.md,.claude/skills/status/scripts/statusline.py
FORBIDDEN=.claude/settings.json,.claude/CLAUDE.md,.claude/hooks/**,.claude/rules/**,app/**
MAX_LINES=N/A（整檔覆寫部署，diff 行數＝兩端分叉幅度之被動結果，非估算對象；估算校準遙測不適用）
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-21（源端：本 session CONTRACT/TELEMETRY 審查全程 live 直讀＋git_show；靶端：list_directory_with_sizes 實測 SKILL.md 3427B 在盤）

## 矩陣對照（ENV_DIFF_MATRIX §8-1 強制節）

| 矩陣項 | 判定 | 依據 |
|--------|------|------|
| C-1 RAPID_MODE | 不受影響 | status skill 內容零 RAPID 依賴；本票流程門檻依 APP 全程序（不沿用 PLT 豁免） |
| C-2/C-3 | 不受影響 | skill 內容不含架構 SSOT 引用與 commit 混檔字面 |
| S-1 statusLine 註冊 | 不受影響 | 兩端註冊字面已同一，免動 settings.json |
| S-2 端專屬 hooks | 不受影響 | 本票零 hooks 搬運 |
| S-3 權限差集 | 不受影響（已逐指令核） | skill 內 bash 用字＝cat/date/stat/echo/tail/python，全在 APP allow 白名單 |
| §5 worktree | 不受影響 | skill 零 worktree 依賴；執行 MUST NOT 於 worktree cwd 進行（TELEMETRY-01 [relayed] 成因疑點，本票 AC 需在主工作樹留證） |

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **動作 A（源端同一性）**：`git -C D:/DevSources/workflow/mcp-ticket-platform log -1 --format=%H -- .claude/skills/status/SKILL.md` 與 `-- .claude/skills/status/scripts/statusline.py`，確認二檔自 `aa3e749`／`b787bd2` 後**零新 commit**；有新 commit → 停止回報（源端已再演進，票面 SHA 錨過期）。
- **動作 B（源端內容抽驗）**：源 A grep `Step 0：資料權威分層`、`claude_status_checkpoint.jsonl`、`json.dumps` 各 ≥1 命中；源 B grep `#63447` ≥1、`Opus 4.8 reports 1M` ＝0。
- **動作 C（靶端現況）**：靶 A、靶 B 存在且為舊版（靶 A grep `Step 0：資料權威分層` ＝0 命中）。
- **任一項不符 → 停止回報，禁自行推定。**
- [ ] 完成

### AI-1: 靶 A 位元組級覆寫
- **動作**: `cp` 源 A → 靶 A；隨後 `git hash-object` 源靶二檔，**hash MUST 相等**（等值即位元組同一之機械證明）。
- [ ] 完成

### AI-2: 靶 B 位元組級覆寫
- **動作**: 同 AI-1 手法，源 B → 靶 B，hash 相等。
- [ ] 完成

### AI-3: 分二筆 commit 落版（[INFRA-STATUS-SYNC-01] 前綴）
- **動作**: 靶 B（.py）與靶 A（.md）分二筆 commit（比照 PLT 端 b787bd2／78899d0 分檔先例；APP Rule 3 混檔禁令保守適用）。
- [ ] 完成

## Hard Constraints
1. **MUST NOT** 對源內容作任何在地改編（含路徑、措辭、格式）；發現「不改編即無法在 APP 運作」之情形 → 停止回報升裁（此即矩陣對照失準之訊號，禁現場繞道）。
2. **MUST NOT** 觸碰 `.claude/settings.json`（註冊已同構）。
3. 行為性 AC 之實跑 **MUST** 於 APP 主工作樹 cwd 執行，且完成回報 MUST 附 `tmp/claude_status_checkpoint.jsonl` 之 `cat` 實測輸出（禁僅宣告「已生成」）。

## Error Handling
- AI-0 任一動作不符 → 停止回報
- hash 不相等 → 停止回報（複製通道損毀，禁手工修補湊 hash）
- **[標配]** `git check-ignore -v .claude/skills/status/SKILL.md .claude/skills/status/scripts/statusline.py` 命中 → 停止回報（gitignore 死結律，候選 #20）

## Acceptance Criteria
- [ ] **[存在性]** 靶 A grep：`Step 0：資料權威分層`、`已知取證缺口`、`快取齡`、`基線重設`、`OPEN_PARAMETER`（≥2）全數命中；`fresh`／`stale`／`compact_candidate`／`clear_candidate` 四詞命中 0（大小寫敏感，`STALE_AFTER_SECONDS` 不計）
- [ ] **[存在性]** 源靶四檔 `git hash-object` 兩兩相等（AI-1/AI-2 證據轉錄於此）
- [ ] **[可達性]** checkpoint jsonl 之 producer＝靶 A Step 1.5 條款（證據欄：APP 端 SKILL.md 行號，執行後回填）；session_id 之 producer＝靶 B `write_cache()`（證據欄：行號，執行後回填）
- [ ] **[行為性]** APP 主工作樹連續二次 `/status` 實跑：輸出含 `(~[P]%)`、快取齡秒值、首次「基線重設」＋次次 Δtoken 數值、零判定詞；`tmp/claude_status_checkpoint.jsonl` cat 實測附於回報（同時消解 TELEMETRY-01 [relayed] 保留，收束時 MUST 於 mcp-ticket-platform 冊該票 append_note 補登正向補證）
- [ ] ~~`assembleDebug` PASS~~（N/A 顯式劃記：docs preset，.claude/ 文本不入 build 面）
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

**End of INFRA-STATUS-SYNC-01**
