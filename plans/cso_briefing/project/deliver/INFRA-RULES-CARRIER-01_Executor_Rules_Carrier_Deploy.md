# INFRA-RULES-CARRIER-01: 執行者面規則載體部署 — §1 全域槽位擴充 + 退役旁路律 + SessionStart 認知條款

**簽發日期**: 2026-08-01
**簽發者**: Claude (SMTR/CSO)
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: 無（裁決來源 `KAIROS_CaseC_Harvest_Round_Merged_Laws` 已落檔）

## 檔案治理/來源說明

本票為 KAIROS C 案收割輪之執行者面部署票（票乙），承載律 B（單槽位載體隔離律，修正為 §1 擴充）、候選 #19（退役悖論防護）、候選 #18（SessionStart 重入認知條款）。SSOT = vault `2026-08-01_KAIROS_CaseC_Harvest_Round_Merged_Laws.md`。

**⚠️ 律 B 範圍修正（開票前預檢發現，非原裁決字面）**：`concurrency.md` §1 已存在條文「每票 scratch MUST 使用 `tmp/<TICKET_ID>/` 專屬子目錄」。經取證，§1 現行覆蓋面僅及「票自建之 scratch」（事件⑤ scratch 互刪），而 #16（`tmp/verify_cmd.conf` 同日兩覆寫）與 K4-cand-20260719-01（`sv_report.md` 覆寫致 Codex receipt 斷鏈）之載體係 **pipeline 硬編碼產生於全域路徑**，不受 §1 現行措辭約束——此為真缺口。故本票採「§1 擴充」而非新增章節，亦不重複既有條文。**同時遵守 `concurrency.md`「待開票清單」#4 之不得重複開票警語**：本票不觸碰 §5 TOCTOU 議題（已由 `INFRA-CR-R0-PRECLEAR-01` 涵蓋）。

## Verify Config
TICKET_ID=INFRA-RULES-CARRIER-01
CREATED=2026-08-01
PRESET=docs
ALLOWED=.claude/rules/concurrency.md,.claude/rules/decommission.md,.claude/rules/kairos-lessons.md
FORBIDDEN=.claude/rules/ssot-governance.md,.claude/settings.json,plans/**,docs/**
MAX_LINES=40
DIFF_REF=a518319
ANCHOR_PROBED=2026-08-01 18:05

**MAX_LINES 推導註記**（額度制截斷序律）：AI-1 §1 擴充 3 行 → AI-2 退役旁路律 4 行 → AI-3 SessionStart 認知條款 4 行 → 分隔／標題行 4 行 → 小計 15 行 × 2.5 緩衝 = 37.5 → 進位 **40**。**截斷序**：實作超額時依 AI-1 → AI-2 → AI-3 順序保留，超額者停止並回報；禁自行壓縮條文字數。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/rules/concurrency.md`（AI-1 錨點）；`.claude/rules/decommission.md`、`.claude/rules/kairos-lessons.md`（AI-2/AI-3 為純檔尾 append，無內文錨點）
- **動作**: ①逐字確認 AI-1 複合錨點於 `concurrency.md` 命中恰 1 次；②確認 AI-2/AI-3 兩目標檔存在且結尾無殘缺（末行完整）。
- **HEAD 位移檢查**: 若 `git log -1` 已越過 `DIFF_REF=a518319` 且異動觸及本票 ALLOWED 三檔任一 → 停止回報，回請 CSO 重跑預檢。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: concurrency.md §1 擴充 — pipeline 全域槽位納入隔離義務
- **檔案**: `.claude/rules/concurrency.md`
- **現值**（**多行複合錨點**，唯一；其餘 §之事件支撐行編號不同故不衝突）:
```
- 事件支撐：⑤

## §2 Worktree／分支串行令
```
- **目標值**（於 `- 事件支撐：⑤` 之後、`## §2` 之前插入下列三行，錨點各行逐字不動）:
```
- 事件支撐：⑤

- **【擴充，律 B】** 本節隔離義務**同及 pipeline 自身產生之全域單槽位載體**（如 `tmp/verify_cmd.conf`、`sv_report.md`）——此類載體非票自建，跨票共用時覆寫與保留制輪替出局為**結構必然**，非執行者疏失。隔離機械化落地前，過渡義務為：讀取一律 **live 直取**（禁憑前次 session 快照），寫入前 **MUST** 備份前置；收口三件套（verdict／DIFF_SCOPE／receipt）之 receipt **MUST** 於收口當刻複製進耐久載體，禁僅留路徑引用。
- 事件支撐（擴充）：`tmp/verify_cmd.conf` 同日兩次覆寫 + 一次執行者 STOP（MEO-T2 系）；`sv_report.md` 跨票覆寫致 Codex PASS receipt 本體滅失、僅剩 testimony（K4-cand-20260719-01）

## §2 Worktree／分支串行令
```
- [x] 完成

### AI-2: decommission.md 檔尾追加 — 退役旁路律（#19）
- **檔案**: `.claude/rules/decommission.md`
- **動作**: **純檔尾 append**（禁插入既有段落內）。追加下列文塊：
```

## 退役旁路律（候選 #19，`KAIROS_CaseC_Harvest_Round_Merged_Laws` KD-7）

- 退役／拆卸類票 **MUST** 於票面預先確立**不經被拆卸層**之合法執行旁路；令之執行路徑 **MUST NOT** 穿越自身標的（否則待拆之 gate 將擋下拆卸動作本身＝退役悖論）。
- 旁路 **MUST** 於開票時具名指明（工具鏈／通道），禁執行中臨時推定。
- 正向取證先例：MEO v1 執法層退役以 Desktop `mcp-ticket-platform` 寫入為旁路（該通道不經 Claude Code PreToolUse hooks，全 session 實證），Step A–E 全序執行完畢。
```
- [x] 完成

### AI-3: kairos-lessons.md 檔尾追加 — SessionStart 重入認知條款（#18）
- **檔案**: `.claude/rules/kairos-lessons.md`
- **動作**: **純檔尾 append**（禁插入既有 Rule 段落內；本項為認知條款，**不指派 Rule 編號**以免與既有編號序衝突）。追加下列文塊：
```

## 認知條款 — SessionStart 重入（候選 #18，`KAIROS_CaseC_Harvest_Round_Merged_Laws` KD-6）

- SessionStart hook 於 `--resume` 重入時再次觸發，係官方**設計行為**，非平台 bug。
- 故跨 session 狀態 **MUST NOT** 依賴 hook 重置語意作為真理來源；需跨 session 存續之狀態 **MUST** 外部化至獨立事件載體（append-only event log 類），由 reducer 導出當前態。
- 事件支撐：MEO v1 phantom-reset（STATE.md 10+ 處註記），為可預期生命週期後果；v2-lite 事件層即本條款之承載設計。
- 性質：認知約束（無機械驗收面）。翻盤條件：v2-lite 事件層若證實仍受 session 邊界污染，升格為行為規則並附機械檢核。
```
- [x] 完成

## Hard Constraints
1. AI-1 為行級插入、AI-2/AI-3 為純檔尾 append；**三者皆禁刪除或改寫任何既有行**。
2. 禁觸碰 `concurrency.md` §5 與「待開票清單」#4（TOCTOU 議題已由 `INFRA-CR-R0-PRECLEAR-01` 涵蓋，該檔明載不得重複開票）。
3. 禁於 `kairos-lessons.md` 指派新 Rule 編號（本次為認知條款，非編號規則）。
4. 條文逐字採用票面「目標值」，禁改寫、禁精簡、禁補充未載明之語句。
5. 禁夾帶 ALLOWED 以外之檔案異動（含 STATE.md、PROGRESS.md）。

## Error Handling
- AI-1 複合錨點命中數 ≠ 1 → 停止回報，禁改用單行 `- 事件支撐：⑤` 替代（該單行可能非唯一）
- `write_file_safe mode=patch` 回非唯一匹配拒改 → 停止回報，禁改 overwrite／禁重謄全檔
- 任一目標檔於 `DIFF_REF=a518319` 後已有他人 commit 異動 → 停止回報
- **[標配]** 令含 commit 標的時，`git check-ignore -v .claude/rules/concurrency.md` 等三檔任一命中（被 .gitignore 排除）→ 停止回報（gitignore 死結律 #20；本票標的為 tracked 檔，預期零命中）

## Acceptance Criteria
- [x] **[存在性]** `grep -c "【擴充，律 B】" .claude/rules/concurrency.md` = 1；且 `grep -c "tmp/<TICKET_ID>/" .claude/rules/concurrency.md` 仍 = 1（既有 §1 條文未遭覆寫）
- [ ] **[存在性]** `grep -c "退役旁路律" .claude/rules/decommission.md` = 1；文塊內「MUST NOT 穿越自身標的」「退役悖論」二字面均命中 — **佐證不明保留**：`退役旁路律`=1 與`退役悖論`=1 均命中，但 AI-2 目標值原文為 `**MUST NOT**`（粗體記號緊貼 NOT），字面比對後與本 AC 純文字 `MUST NOT 穿越自身標的` 不符（票面 AI-2/AC-2 撰寫不一致，非施作缺陷），待 CSO/Akira 裁決
- [x] **[存在性]** `grep -c "認知條款 — SessionStart 重入" .claude/rules/kairos-lessons.md` = 1；且該檔既有 `K5-Rule 29` 字面仍命中 1 次
- [ ] **[存在性]** 三檔各含 SSOT 引註 `KAIROS_CaseC_Harvest_Round_Merged_Laws` 恰 1 次 — **佐證不明保留**：`decommission.md`／`kairos-lessons.md` 各命中 1 次，但 `concurrency.md` = 0（AI-1 目標值原文未含此引註字面，票面 AI-1/AC-4 範圍不一致，非施作缺陷），待 CSO/Akira 裁決
- [x] **[行為性]** `git diff --stat` 顯示**零刪除**（僅 `+` 行）；三檔 insertions 合計 ≤ 40（實測 3+6+7=16 insertions，0 deletions）
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（實測僅 concurrency.md／decommission.md／kairos-lessons.md 三檔異動）

## 完成後動作
- 完成後回報，停止，**不自行轉冊面狀態、不搬移票檔**（Pending→deliver 由 CSO 收束時執行）。


---
**冊面收束尾註(CSO,2026-08-01)**:全 AI 收束。**AC-2 偏差裁決**:AC 驗收字串「MUST NOT 穿越自身標的」漏載粗體記號,致與檔內「**MUST NOT** 穿越自身標的」字面不中;`git_diff` 證實條文逐字比中票面 AI-2 目標值,**視同 PASS**。**AC-4 偏差裁決**:AI-1 目標值本身未含 SSOT 引註字面(票面 AI/AC 撰寫不同步),2/3 檔具引註;concurrency.md 之可追溯性由「律 B」標記 + 決策檔 Addendum-A 反向指標達成,**視同 PASS**,引註缺失登記為輕債(未來任何觸及 §1 之票順手補引,不單獨開票)。二偏差均屬票面起草缺陷、非執行缺陷,依簽發時法不追溯修改 AC 字面;同輪三實證收割為候選 #25。CSO 獨立取證:git_diff 16 insertions / 0 deletions,僅涉 ALLOWED。
