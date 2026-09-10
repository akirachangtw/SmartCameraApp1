# INFRA-COMPACT-REVB-APP-01: compact skill Rev B 部署 APP（三模式全採＋Auto Commit 段保留＋長 session 工作流對齊）

**簽發日期**: 2026-09-03
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（skill prompt 文本重構，§0 `git restore` 五分鐘域）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `D:/DevSources/Skill-Advisor/deliver/compact-skill/SKILL_RevB.md`（10,838B，規格 SSOT，2026-09-03 實測在盤）＋同目錄 `spec_delta_revb.md`（8,721B）；PLT `INFRA-COMPACT-REVB-01`（CLOSED，`docs/deliver/archive/INFRA/`）為 POV 驗測成果與四項 Rigor Gate Major 修訂之來源
- `coordination_dependency`: **`INFRA-COMPACT-CLOSURE-GATE-APP-01`（APP 冊 seq 31，PENDING）——ALLOWED 交集硬約束**：同改 `.claude/skills/compact/SKILL.md`。**該票 MUST 先行**；本票為整檔重構，MUST 於 AI-1 逐字保留其交付之「冊面收束一致性閘」節（見 AI-1 動作 D）。該票未落地即派工本票 → 停止回報

## 檔案治理／來源說明

**工作流方向裁決（Akira 2026-09-03 L2，本票之上位前提）**：APP 全面導入**長對話＋原生 context compaction** 工作流。「每完成一張 ticket 就新開 session、產一份交接檔」不再視為正常作業；一條長 session 應可連續處理多張票。**`/compact` 之定位自此為 context 管理與階段性交接，不是每張 ticket 的結案儀式。** 長對話規則應置於 `CLAUDE.md`（官方建議），不依賴對話早期內容。

**此裁決使 Rev B 三模式從「非必要」變為「工作流骨架」**：`brief`（階段性摘要）／`checkpoint`（context 管理落盤）／`closeout`（真結案）恰為新工作流之三個節點；單模式設計預設「每次 compact 都是結案」，正是被裁決否定之舊假設的具現。SMTR 於 2026-09-02 曾建議「只取 delta、不引入三模式」——該建議建立於舊假設（頻繁手動交接），已由本裁決推翻，如實留存於此不隱去。

**現況取證（2026-09-02 live，`aud_20260902_4b494ca91d`）**：APP `.claude/skills/compact/SKILL.md` 3,314B，單模式；`## 輸出格式`（Cueshot Handoff 模板）／`## 最後 Housekeeping`（PowerShell，保留**當天**檔案）／`## Auto Commit: Governance Artifacts`（L88 起）三節。

**三處衝突之裁決（Akira 2026-09-03，甲案）**：
- **R-1 Housekeeping**：採 Rev B —— 依 mtime 保留**最新 2 份**，APP 之「保留當天」規則**廢止**。理由：長 session 下一天可能僅產 1-2 份，日期制退化為無效篩選；mtime 制與 session 密度解耦
- **R-2 Auto Commit 段**：**保留 APP 版，Rev B 不得覆蓋**。該段以 `ssot-governance.md` 表為清單 SSOT、Scope 汙染即停、零 diff SKIPPED、禁 `--allow-empty`，為 APP 端已驗證成果，且與 Rev B「禁 `git add -A`」保守條款相容非衝突
- **R-3 三模式**：**全採**（見上方工作流裁決）

**改名不執行**：skill 資料夾與 frontmatter `name:` 維持 `compact`（PLT 端改名 `session-checkpoint` 案緩議中，屬 breaking semantic change，須先完成全消費端清點）。底本內 `session-checkpoint` 自稱字面一律替換為 `compact`。

## Verify Config
TICKET_ID=INFRA-COMPACT-REVB-APP-01
CREATED=2026-09-03
PRESET=docs
ALLOWED=.claude/skills/compact/SKILL.md
FORBIDDEN=.claude/skills/wktree-compact/**,.claude/skills/status/**,.claude/skills/qa/**,.claude/skills/sv/**,.claude/CLAUDE.md,.claude/rules/**,.claude/settings.json,.claude/hooks/**,plans/cso_briefing/project/TICKET_REGISTRY.json,PROGRESS.md,app/**
MAX_LINES=N/A（整檔重構，diff＝規格差之被動結果；TPL v3.5 R3）
DIFF_REF=84ddbce
BASE_COMMIT=84ddbce
TARGET_BLOB=.claude/skills/compact/SKILL.md:cec95e89
ANCHOR_PROBED=2026-09-10（AI-0 重跑：pre-image sha8=cec95e89 實測；底本 287 行、brief×2／checkpoint×12／closeout×9 命中實測；coordination `INFRA-COMPACT-CLOSURE-GATE-APP-01`=CLOSED 復驗；消費端耦合掃描 15 檔僅指令名提及，無段落結構耦合；`git check-ignore` 未命中）

## Action Items

### AI-0: 前置取證（派工後、修改前）
- **動作 A（pre-image）**：`git hash-object .claude/skills/compact/SKILL.md` 記錄 sha8 存收尾（AC 負向對照用），同值回填 `TARGET_BLOB`；`git rev-parse --short HEAD` 回填 `BASE_COMMIT`；**同時更新 `ANCHOR_PROBED` 時戳**
- **動作 B（底本完整性）**：`SKILL_RevB.md` 存在且非空；grep 命中 `brief`／`checkpoint`／`closeout` 三模式字面各 ≥1
- **動作 C（coordination 復驗）**：`registry_list(project="SmartCameraApp1", ticket_id="INFRA-COMPACT-CLOSURE-GATE-APP-01")` 讀 lifecycle——非 `CLOSED`/`REVIEW` → **停止回報**（違序）。並確認標的檔內「冊面收束一致性閘」節字面在盤
- **動作 D（消費端內容耦合掃描）**：`git grep -l "skills/compact\|/compact" -- .claude/ plans/ docs/` 枚舉清單存收尾——僅枚舉不改動；發現有檔案引用 compact skill 之**內部段落結構**（非僅指令名）→ 停止回報升裁
- **動作 E（三分流）**：任一項不符 MUST 依 TPL v3.8 基線漂移三分流分類回報（`ANCHOR_STALE_SCOPE_INTACT`／`ALREADY_EXECUTED_PENDING_CLOSURE`／`CANDIDATE_SUPERSEDED`／`BLOCKED_UNATTRIBUTED_STATE`），禁單一 STOP 無分類
- **[標配]** `git check-ignore -v .claude/skills/compact/SKILL.md` 命中 → 停止回報
- [x] 完成

### AI-1: 整檔部署 Rev B 內容（四項動作，缺一即未完成）
- **動作 A（底本部署）**：以 `SKILL_RevB.md` 為底本整檔覆寫標的。frontmatter `name:` MUST 維持 `compact`，`description` 依 Rev B。底本內 `session-checkpoint` **自稱**字面（含連字號式 `session-checkpoint` frontmatter 值，與空格式 H1 標題「Session Checkpoint Skill」等變體）一律替換為 `compact`（敘述性提及原生 `/compact` 指令處不動）
- **動作 B（§12 遷移建議刪除）**：底本 §12「遷移建議」為規格作者對「舊版→Rev B」之一次性作者側指引、非 skill 運行時內容，且其第 1 步建議改名與本票「維持 `compact`」直接衝突 → **MUST 刪除**，不併入部署後檔案
- **動作 C（三模式呼叫語法）**：部署內容 MUST 含 `brief`／`checkpoint`／`closeout` 三模式之呼叫語法（`/compact brief` 等 ARGUMENTS 慣例）；**無參數時之預設模式 MUST 明文寫入 SKILL.md 本體為 `checkpoint`**（Rigor Gate F2 Decision-Fork，Akira 2026-09-08 裁決選項 B：`SKILL_RevB.md` §4 全文未定義預設值，`checkpoint` 與票面裁決敘述「compact 之定位自此為 context 管理與階段性交接」字面最貼合，且與現行單模式每次同步 Dashboard 之使用習慣換版落差最小）；依工作流裁決（compact 為 context 管理非結案儀式），預設 **MUST NOT** 為 `closeout`
- **動作 D（R-2 段落保留，本票最高風險項）**：APP 既有 `## Auto Commit: Governance Artifacts` 全段 ＋ `INFRA-COMPACT-CLOSURE-GATE-APP-01` 交付之「冊面收束一致性閘」全節，**MUST 逐字保留並併入重構後版本**，位置關係維持「收束閘節在前、Auto Commit 節在後」。**MUST NOT** 以底本內容覆蓋或改寫此二節之任何 MUST 條款。本動作為整檔覆寫之例外區，執行順序建議：先取出此二節原文 → 部署底本 → 回植 → 逐字比對
- **動作 D-1（呼叫時機鎖定，Rigor Gate F1 修訂）**：此二節之**呼叫路徑**（何時被執行到，非文字本身）**MUST** 限定於 `closeout` 模式。依據：`SKILL_RevB.md` §8.1 明文「brief 與 checkpoint 模式 **MUST NOT** 自動執行 `git add -A`、`git add .`、`git commit`」「任何 Git stage / commit 都 **MUST** 屬於顯式的 closeout 或 delivery 流程」——此為底本已明文之限制，非本票新增裁決。部署後 **MUST NOT** 維持現行單模式檔案「不分模式恆執行 Auto Commit」之呼叫方式；`brief`／`checkpoint` 模式路徑 **MUST** 在到達此二節之前即返回，不得經過其邏輯
- [x] 完成（§4.1/4.2 各附「MUST 在完成上述動作後直接結束，MUST NOT 到達…二節之邏輯」；§4.3 明文「MUST 依序執行…此二節之呼叫路徑僅限於 closeout 模式」；二節逐字比對 diff 為空，見收尾）

### AI-2: 五項承重 delta（PLT 端已驗，逐字要點）
1. **承重來源節**：檔首補「規格來源：`Skill-Advisor/deliver/compact-skill/SKILL_RevB.md` ＋ 部署票 `INFRA-COMPACT-REVB-APP-01`」
2. **session_id 通道互指**：Source Session 欄 MUST 自 `tmp/claude_status_cache.json` 頂層 `session_id` 直取（producer：`.claude/skills/status/scripts/statusline.py` 之 `write_cache()`）；**APP 端 MUST 於 AI-0 確認該 producer 路徑於 APP 存在**——不存在則該欄一律標 `unavailable` 並於收尾載明，**禁弱指紋推定**、禁沿用 PLT 行號
3. **雙 handoff 載體邊界節**：本 skill 之 `docs/session_handoff/` 為 **repo-local、Claude Code 端**交接載體；Desktop 治理端載體（`Skill-Advisor/Handoff/`）職責互斥，**MUST NOT** 交叉寫入
4. **Archive 判準機械化**：`docs/session_handoff/` 歸檔規則改「依 mtime 保留最新 **2** 份、其餘搬 `Archive/` 子目錄」，**APP 既有「保留當天」規則與其 PowerShell 實作一併廢止**（R-1 裁決）；刪除任何「最近工作所需」類語義措辭。**注意（Rigor Gate F6 修訂）**：`SKILL_RevB.md` §7 原文僅陳述「除最近工作所需 handoff 外，其餘 SHOULD 移入 Archive」，**未含 mtime 或數字 2**，且原文本身即含「最近工作所需」字面——本項為**主動改寫**底本 §7 對應段落（以 R-1 量化規則取代其模糊用語），**非**逐字繼承，實作時 MUST 以此裁決文字覆蓋底本 §7 原句，不得直接沿用
5. **Context Mode 機械判定**：欄值由呼叫時**明示參數**決定（`resumed`／`compacted`／`cleared`），無參數一律 `continue`。部署內容 MUST 逐字納入「明示參數決定」與「禁自行語義推定」兩片語
- [x] 完成（五項 grep 逐字命中確認，見收尾）

### AI-3: 行為實測（production 路徑）
- 於 APP 實際 session 各跑一次 `brief` 與 `checkpoint` 模式：`checkpoint` 產出落 `docs/session_handoff/`、`PROGRESS.md` 有對應更新、輸出含 Source Session 欄（UUID 或 `unavailable`）
- `git status` 確認 skill 未執行任何 `git add -A`（保守條款實證）；Auto Commit 段之逐檔提交行為與部署前**逐字一致**
- **`checkpoint` 模式測試 MUST 於執行前先於當次 session 記錄至少一項非 trivial 決策或 blocker**，以驗證其確實承載內容而非產出空殼（PLT Rigor Gate Major-3 修訂）
- **長 session 情境驗證（APP 特有，PLT 無此項）**：於**同一 session 已收束 ≥1 票**之狀態下執行 `brief`，確認其輸出不觸發任何結案語義動作（不搬票檔、不改冊面、不標 CLOSED）——證三模式分工在新工作流下成立
- **`closeout` 模式實跑（Rigor Gate F3 修訂，本票不可省略項）**：於 `.claude/PROGRESS.md` 或任一 Governance Artifact 清單檔案為 dirty 狀態下實跑一次 `closeout`，確認 Auto Commit／冊面收束一致性閘二節確實被觸發（可觀察到對應 `📌 Auto Commit:` 輸出行或明確 SKIPPED/BLOCKED 判定），且未被動作 D-1 新增之 mode-gating 邏輯意外阻斷
- **mode-gating 負向驗證（Rigor Gate F1 修訂）**：於 `.claude/PROGRESS.md` 為 dirty 狀態下分別實跑 `brief` 與 `checkpoint`，以 `git log -1` 確認二者皆**無新 commit 產生**（非僅檢查 `git status` 無 `-A` 痕跡）
- [x] 完成（`brief`／`checkpoint`／`closeout`／裸 `/compact` 四次實跑：`brief` 唯讀＋未達二節；`checkpoint` 更新 PROGRESS.md 且無 commit；`closeout` 依序執行收束閘（待驗清單空，SKIPPED）→ Auto Commit（Scope 汙染 BLOCKED，因 `INFRA-CTAWR-PHASE0-01_activate.md` 尚未提交，已於本輪另行 commit `d00d677`），二者皆正確被觸發非被 mode-gating 意外阻斷；裸 `/compact` 行為與 `checkpoint` 一致；長 session 驗證：本 session 已收束 `INFRA-CTAWR-PHASE0-01` 後執行 `brief` 未觸發任何結案動作；Archive mtime-2 邏輯以 scratchpad 隔離沙盒驗證（3 檔差異 mtime，keep-2 邏輯正確保留最新 2 份、其餘 1 份移入 Archive/，未觸碰真實 `docs/session_handoff/`）；`Skill-Advisor/Handoff/` 目錄 mtime 全程未變動）

## Hard Constraints
1. `name:` MUST 維持 `compact`；禁執行改名
2. **AI-1 動作 D 之二節為整檔覆寫之例外區**，禁以任何理由簡化、改寫或省略；違反即回退本票交付並回退 `INFRA-COMPACT-CLOSURE-GATE-APP-01` 成果
3. 禁動 ALLOWED 外任何檔案——特別是 `CLAUDE.md`（長對話規則另票）、`.claude/rules/**`、`wktree-compact`
4. **【Rev B 改寫，2026-09-10】** 原條「禁引入 PLT 五態字面；APP 為四態制（`ssot-governance.md` L81）」所依之 2026-08-27 四態裁剪，已於 2026-09-06 Akira D-1 裁決明示 supersede（`ssot-governance.md` L153 翻盤紀錄；APP 自此採 PLT 五段語義，`INFRA-LIFECYCLE-SSOT-01`）。改為：五態字面得且僅得出現於 AI-1 動作 D 逐字保留之二節（`## 冊面收束一致性閘`、`## Auto Commit: Governance Artifacts`）及 `SKILL_RevB.md` 底本原有引用處；部署**新增**內容 **MUST NOT** 另造第二套 lifecycle 枚舉或狀態詞（`ssot-governance.md` L109「不新增第二套流程」）
5. **禁以 PLT 端行號、路徑或實測值直接寫入 APP 部署內容**——`statusline.py` 行號、`docs/` 路徑均須 APP 端現地確認
6. 完成後 STOP，等待 SMTR 複審，禁自行擴 scope

## Error Handling
- AI-0 任一項不符 → 依三分流分類回報
- `INFRA-COMPACT-CLOSURE-GATE-APP-01` 未達 `REVIEW`/`CLOSED` → 停止回報（coordination 違序）
- AI-0 動作 D 發現段落級結構耦合 → 停止回報升裁（整檔替換將破壞引用端）
- `tmp/claude_status_cache.json` 之 producer 於 APP 不存在 → **不停止**，依 AI-2 第 2 項標 `unavailable` 並載明
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** frontmatter `name: compact`；`session-checkpoint` 自稱字面命中＝0（grep 逐字確認）
- [x] **[存在性]** 三模式 `brief`／`checkpoint`／`closeout` 呼叫語法字面齊備；預設模式明文寫入為 `checkpoint`（L68：「無參數時之預設模式 MUST 為 `checkpoint`」，正面值直接比對）
- [x] **[行為性]** given 無參數之裸 `/compact` 呼叫，expect 實際行為與明示呼叫 `/compact checkpoint` 一致——本輪已實跑：裸呼叫更新 `.claude/PROGRESS.md`，`git log -1` 無新 commit，與明示 `checkpoint` 一輪行為一致
- [x] **[存在性]** 五項 delta 字面全命中；`最近工作所需` 命中＝0；`明示參數決定`（L151）與`禁自行語義推定`（L151）兩片語均命中（grep 逐字確認，非僅 `continue` 命中）
- [x] **[存在性]** §12「遷移建議」相關字面命中＝0（grep 確認）
- [x] **[行為性]（R-2 保留之正向驗證，本票最關鍵守門）** 二節與部署前版本 `diff` exit 0，零差異（`awk` 擷取「## 冊面收束一致性閘」起至檔尾，對比 `git show HEAD:` 前版對應區段）
- [x] **[行為性]** `git hash-object` 新值 `20c3baee`... ≠ pre-image `cec95e89`...
- [x] **[存在性]（HC4 Rev B 直接驗證）** 三字面命中 L322/L325/L334，逐行核對皆落於「冊面收束一致性閘」節內；節外新增命中＝0
- [x] **[行為性]（Rigor Gate F1 修訂，本票次關鍵守門）** `brief`／`checkpoint` 各實跑一次，`git log -1` 皆顯示 `d00d677`（`INFRA-CTAWR-PHASE0-01` 收尾 commit，先於本輪兩次測試即存在）不變，無新 commit
- [x] **[行為性]（Rigor Gate F6 修訂）** `Skill-Advisor/Handoff/` 目錄 `ls -la` 確認全程 mtime 未變（唯一檔 `HANDOFF_2026-09-09-1600_...md` mtime 停留 Sep 9 19:10，早於本輪測試）
- [x] **[行為性]（Rigor Gate F6 修訂，附誠實揭露）** mtime-2 keep 邏輯已以 3 份差異 mtime 檔驗證：keep-2/move-1 行為正確（保留最新 2 份、最舊 1 份移入 Archive/）——**惟執行於 session scratchpad 隔離沙盒，非真實 `docs/session_handoff/`**（該目錄現僅 2 份根檔，無法在不注入假資料汙染真實交接史的前提下自然湊滿 3 份），驗證的是 §7 邏輯本身之正確性，非對真實目錄之直接實跑；如需逐字滿足「given 真實目錄 3 份」字面，需下次真實累積至 3 份時另行核驗
- [x] **[實機]** `brief`／`checkpoint` 兩模式實跑 PASS；`git status` 全程零 `git add -A` 痕跡（僅見既有 `M` 標記，無 staged 異動）
- [x] **[實機]（Rigor Gate F3 修訂，本票不可省略項）** `closeout` 模式實跑 PASS：收束閘 SKIPPED（待驗清單空）→ Auto Commit BLOCKED（Scope 汙染，因 `INFRA-CTAWR-PHASE0-01_activate.md` 當時未提交）皆確實觸發，未被 mode-gating 邏輯攔下
- [x] **[實機]** 長 session 情境驗證通過：本 session 已收束 `INFRA-CTAWR-PHASE0-01`（commit `d00d677`）後執行 `brief`，未搬票檔、未改冊面、未標 CLOSED
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，純 skill 文本，無編譯產物)
- [x] `git diff --stat` 僅涉及 `.claude/skills/compact/SKILL.md`＋`.claude/PROGRESS.md`（後者為 Governance Artifact，依 `INFRA-GOVSYNC-01` 不計入本票 scope 違規判定；本票 AI-3 dogfood 測試之預期副作用）

---

**End of INFRA-COMPACT-REVB-APP-01**

## Addendum

**Rev B（2026-09-10，SKAD 裁決，Akira 授權「協助檢視分析進行裁決」）**：執行端於 AI-0 停止回報 `⚠️ SSOT 衝突`——HC4／對應 AC「五態字面命中＝0」與 AI-1 動作 D「逐字保留 `## 冊面收束一致性閘` 節」互斥（該節 L99／L102／L111 字面即含五態）。取證：`ssot-governance.md` L153 翻盤紀錄載明 HC4 所引之 2026-08-27 四態裁剪已於 2026-09-06 Akira D-1 supersede；本票簽發於 2026-09-03，票面未同步。分類 `SPEC_DEFECT`（改 HC＋AC）→ 開 Rev B：HC4 改寫為「五態字面限於逐字保留二節與底本原有引用處，新增內容禁另造第二套枚舉」；L99 AC 改為逐行出處對照。HC2、AI-1 動作 D、`INFRA-COMPACT-CLOSURE-GATE-APP-01` 交付物均不受影響。執行端得自 AI-0 續作。O1：`Rev=1 ｜ AI 標題數=3 ｜ AI 深度=1 ｜ 4a/4d/6b 狀態=未成立`。

## 收尾實錄

**四要件④耐久載體（2026-09-10）**：AI-0~AI-3 碼閉環 + AC 15/15 附證據勾選（本檔本體，見上方）。

**Commits**（`git log`，`DIFF_REF=d00d677..DIFF_END=2370834`，另 `84ddbce..d00d677` 為前置獨立票不計入本票 scope）：
- `a661104` — 整檔部署 Rev B（`.claude/skills/compact/SKILL.md`＋`.claude/PROGRESS.md`）
- `2370834` — Codex R1 ADVISORY 修復：L150 `session_id` producer 描述改精確（`write_cache()` 不產生/不引用 `session_id`，僅持久化 hook 原始 payload；`INFRA-STATUS-SYNC-01` S90 已實測記載此區分）
- `de3aaa2` — PROGRESS.md Dashboard 同步：`/sv` PASS 收尾

**審查鏈**：
- `/qa`：✅ PASS（`assembleDebug` 45s、`testDeviceDebugUnitTest` 209/209、lint 584 baseline/0 new、`static_blind_scan.sh` 5 項既有域外 ⚠️，皆 pre-existing 非本輪觸碰）
- `/sv` Stage A-C：✅ PASS（ELF ALIGNED；檔案邊界 `d00d677..2370834` 僅命中 ALLOWED＋Pipeline Artifact）
- Codex Cross-Review：R1 📋 REPORT_FIX（報告措辭未區分「預期阻擋」與「乾淨通過」＋ADVISORY 揪出 L150 已知不精確描述）→ R2 📋 REPORT_FIX（`DIFF_END` 未涵蓋 R1 修復 commit `2370834`）→ R3 ✅ **PASS**（REACHABILITY: NONE）
- `structural_verify.sh --post`：✅ PASS(A/B/C/D/E/F/G) `[ADVISORY]`（唯一告誡：`.claude/PROGRESS.md` 80 行逼近 ≤80 上限，非本票 scope 之既存狀態）
- Traycer：**N_A**（票面 Header L7 開票時即宣告，skill prompt 文本重構屬 §0 `git restore` 五分鐘域，非結構性變更）

**非阻擋項誠實揭露**（皆已於 AC 對應項載明，不另開票）：
- AC 102 Archive mtime-2 測試於 session scratchpad 隔離沙盒驗證邏輯正確性，非對真實 `docs/session_handoff/` 目錄之逐字實跑（該目錄現僅 2 份根檔，無法自然湊滿 3 份）；待該目錄未來自然累積至 3 份時 SHOULD 另行核驗
- Codex R1 抓到的 L150 描述性瑕疵源自票面 AI-2 delta 2 原文本身（非執行端自創），已於 R1 後同步修正 HC/AC 承載文字與部署內容，非僅改報告措辭

**下游影響**：`INFRA-COMPACT-CLOSURE-GATE-APP-01` 交付之「冊面收束一致性閘」節與 APP 既有「Auto Commit: Governance Artifacts」節逐字保留成功（diff=0），二節下游消費（`/compact closeout` 模式）不受影響。
