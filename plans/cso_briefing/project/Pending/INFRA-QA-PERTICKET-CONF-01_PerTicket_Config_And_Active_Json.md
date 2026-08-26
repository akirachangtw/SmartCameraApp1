# INFRA-QA-PERTICKET-CONF-01: per-ticket config 遷移與 active.json 原子指標（P3，單槽位污染根治）

**簽發日期**: 2026-08-26 ｜ **簽發者**: Claude（SMTR；Akira 2026-08-23 開票授權，2026-08-26「繼續上述」續行）
**來源議題**: INFRA ｜ **Mode**: uc ｜ **Effort**: Medium
**Traycer Workflow**: N/A（`.claude/` 工具鏈腳本；APP 無 RAPID，QA／SV／Cross-Review 全程序）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（`INFRA-TPL-V38-01` 已 CLOSED，本票 evidence 前置滿足）
- `evidence_dependency`: `Skill-Advisor/deliver/QA-Session-Aware-Verification.md` Rev B Step 2（P3 規格 SSOT，L218-241）；`INFRA-TPL-V38-01` checkpoint（v3.8 條文）；`concurrency.md` §1 律 B（單槽位覆寫實證）
- `coordination_dependency`: `INFRA-QA-ACTIVATION-01`（P2；本票為其 Step 2 之載體層，**MUST 先於 P2 收束**——P2 之 V0 通過後始得更新 `active.json`，該檔須先存在）

## Verify Config
TICKET_ID=INFRA-QA-PERTICKET-CONF-01
CREATED=2026-08-26
PRESET=refactor
ALLOWED=.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/SKILL.md,.gitignore
FORBIDDEN=.claude/skills/sv/**,.claude/skills/status/**,.claude/CLAUDE.md,.claude/settings.json,app/**
MAX_LINES=N/A
BASE_COMMIT=<開工時 git rev-parse --short HEAD 回填>
TARGET_BLOB=<開工時逐標的 git hash-object 回填，格式 path:sha8，分號分隔>
DIFF_REF=字面佔位待開工回填
ANCHOR_PROBED=2026-08-26（SMTR live grep：verify_checklist.sh evidence 區塊 L457-477 在盤，`tmp/${TICKET_ID}/evidence` 落點字面確認；派工時 MUST 重跑）

## 檔案治理／來源說明

現況缺陷（已有兩筆實證）：`tmp/verify_cmd.conf` 為**全域單槽位**跨票可變檔——`INFRA-ENVSYNC-AUDIT-01` 之值殘留至 `INFRA-MAXLINES-GATE-01` 執行時（2026-08-23 實測）；`concurrency.md` §1 律 B 另載同日兩次覆寫＋一次執行者 STOP。本票依 P3 規格遷移為 per-ticket 目錄制。

**與既有 evidence record 之關係**：`INFRA-MAXLINES-EVIDENCE-01` 已建立 `tmp/<TICKET_ID>/evidence/` 隔離制；本票之 `tmp/verify/` 為**設定層**，二者路徑不相交、職責不同（前者取證、後者啟用態），**MUST NOT** 合併或互相覆寫。

## Action Items

### AI-0: 前置取證
- A：`verify_checklist.sh` grep `verify_cmd.conf` 命中 ≥1（遷移標的在盤）；`tmp/verify/` 字面命中 0（未先行）
- B：`tmp/verify_cmd.conf` 現值 `TICKET_ID=` 之值存收尾（污染現況留證，供 AC 對照）
- C：`.gitignore` 對 `tmp/` 之既有規則枚舉存收尾（新增落點須確認已被忽略，禁令 runtime 態入版控）
- 任一不符 → 依 TPL v3.8 基線漂移三分流分類回報
- [ ] 完成

### AI-1: per-ticket config 讀寫路徑遷移
- `verify_checklist.sh` 之 config 讀取改為 `tmp/verify/<TICKET_ID>.<revision>.conf`（revision 缺省時以 `rev-0` 佔位）；**保留 `tmp/verify_cmd.conf` 之讀取作為 fallback**（過渡期相容，並於 stdout 輸出一行棄用警示）
- **MUST NOT** 刪除舊檔（退役屬另票；本票僅改讀寫優先序）
- [ ] 完成

### AI-2: active.json 原子寫入器
- 六欄位：`ticket_id`／`ticket_revision`／`repo_root`／`branch`／`head_sha`／`activated_at`（UTC ISO-8601）／`activation_source`（枚舉 `resume|new-session|ticket-switch`）
- 寫入 **MUST** 採 temp file ＋ atomic rename；寫入後 **MUST** read-back 驗 `ticket_id`／`ticket_revision`／`repo_root`／`head_sha` 四欄一致，不一致即警示並**保留既有 config 不破壞**
- 本票僅建立寫入器與 read-back，**不接線至 V0 閘**（該接線屬 P2）
- [ ] 完成

### AI-3: 輪替與清理規則（R5，前置資源恆存律）
- `tmp/verify/` 之 `.conf` 依 mtime 保留最新 **10** 份，其餘刪除；`active.json` 永不輪替
- 清理於 config 寫入後執行，失敗僅 `echo >&2` **MUST NOT** 影響 exit code（比照 EVIDENCE-01 HC-1 語義）
- [ ] 完成

### AI-4: SKILL.md 契約段更新
- `/qa` SKILL.md 補：config 落點新舊優先序、`active.json` 六欄位與原子寫入要求、輪替上限 10、與 `tmp/<TICKET_ID>/evidence/` 之職責邊界（設定層 vs 取證層，禁合併）
- [ ] 完成

## Hard Constraints
1. **MUST NOT** 刪除或改寫 `tmp/verify_cmd.conf`（退役另票）。
2. **MUST NOT** 觸碰 `tmp/<TICKET_ID>/evidence/` 路徑或其寫入邏輯（EVIDENCE-01 成果）。
3. config／`active.json` 寫入失敗 **MUST NOT** 成為 Step 0 阻斷理由（同 EVIDENCE-01 HC-1：本票建立能力，阻斷權不回流）。
4. **MUST NOT** 於本票實作 V0／V1／V2 判定或 Write Authority 閘（屬 P2）。
5. `tmp/verify/` **MUST** 落在 `.gitignore` 忽略範圍內；若既有規則未涵蓋，AI-1 得補一行——此為 ALLOWED 含 `.gitignore` 之唯一用途。

## Error Handling
- AI-0 任一不符 → 依 v3.8 三分流分類回報
- read-back 四欄不一致 → 停止回報（禁重試覆寫，可能為並行競態）
- **[標配]** `git check-ignore -v` 對 ALLOWED 三檔 → 命中即停止回報（#20；註：`tmp/verify/` 之命中屬預期目標態，不在此列）

## Acceptance Criteria
- [ ] **[存在性]** `verify_checklist.sh` 含 `tmp/verify/` 與 `active.json` 字面；舊 `verify_cmd.conf` 字面仍在盤（HC-1 正向取證，命中 ≥1）
- [ ] **[存在性]** `.gitignore` 對 `tmp/verify/` 之忽略成立（`git check-ignore` 命中，輸出存收尾）
- [ ] **[行為性]** given 同一 repo 連續以兩個不同 `TICKET_ID` 執行 Step 0，expect 產生兩個獨立 `.conf` 檔且互不覆寫，`active.json` 末次值為後者（單槽位污染根治之直接驗證；兩次輸出存收尾）
- [ ] **[行為性]** given `active.json` 寫入後，expect read-back 四欄與寫入值一致；given 人工竄改 `active.json` 之 `ticket_id` 後重跑，expect 輸出 mismatch 警示且既有 `.conf` 內容未被破壞
- [ ] **[行為性]** given `tmp/verify/` 內置入 12 個 `.conf`，expect 清理後為 10 個且 `active.json` 仍在
- [ ] **[行為性]** given `tmp/verify/` 設為唯讀致寫入失敗，expect Step 0 exit code 不因此非零（HC-3）
- [ ] **[可達性]** config 載體之 producer＝`verify_checklist.sh`（證據欄：行號執行後回填）；消費者為 Step 0 讀取路徑（行號回填）
- [ ] ~~`assembleDebug` PASS~~（N/A 顯式劃記：`.claude/` 工具鏈腳本，零 `.kt`／`.xml` 變更）
- [ ] `git diff --stat` 僅涉 ALLOWED 三檔

**End of INFRA-QA-PERTICKET-CONF-01**
