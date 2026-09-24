# RGR-SCOPE-BIND-01: 使 runner 能機械證明 payload target 位於簽發票宣告之 scope 內

檔案治理／來源說明：本票依據 `RGR-P0A-02` AI-0（2026-09-19T08:11:34Z）之一手回流、`repair_runner.sh` 現值（L310／L367–410／L394／L671）、`ticket-repair-payload.v1.json` 全檔、`REPAIR_ROUTING.md` L44–49 建立，依 `Unified_Ticket_Template_v3.md` v3.11 產出。**本票為草案，未派工**；其 AI-0 為調查性質，解法尚未選定。

**簽發日期**: 2026-09-19
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `RGR-DISPATCH-AUTH-01`（`REPAIR_ROUTING.md` L44–49 條件②之法條字面）；`RGR-P0A-02` AI-0 回流（缺口之一手確認與 fixture-only 限制之理由）
- `coordination_dependency`: `RGR-P0A-02` —— **二票不互鎖**。P0A-02 於 fixture 路徑內證明預授權閉環；本票證明真實票面授權與 target 之機械綁定。本票之交付與驗收**不自動解除** P0A-02 之 fixture-only 限制（P0A-02 HC 9）；解除須另開明示之 follow-up 票。

## 問題陳述（CONFIRMED，`RGR-P0A-02` AI-0 一手確認）

`REPAIR_ROUTING.md` `Dispatch Authority` 條件②要求「target 落於票面宣告 scope」，但 runner 端**無對應機械 enforcement**：

| 現值 | 位置 | 說明 |
|---|---|---|
| `IRREVERSIBLE_TARGET_PATTERNS=("app/src/*" "*.gradle*" "gradle/*" ".github/*" "*migration*/*")` | L310 | 與簽發票**無關**之通用黑名單 |
| `target_file="$(jq -r '.target.file_path' "$payload")"` ＋ pattern 迴圈 | L393–394 | 目前**唯一**的 `target.file_path` gate |
| `blocker_preflight()` | L367–410 | 全函式無任何逐票 scope 比對 |
| payload schema | 全檔 | **無**承載簽發票 `ALLOWED` scope 之欄位（`constraints.allow_scope_expansion` 僅為布林，傳入 prompt 供模型參考，非機械 gate） |

後果：現行 `addendum_only` 流程技術上可寫入任何非黑名單 `.md`，包含**另一張已簽發票之本文**。此為既有缺口，非 `RGR-P0A-02` 引入之退化；P0A-02 已以 fixture-only boundary 使新能力之 blast radius 為零，但條件②本身仍無把關。

## 本票之目標

使 runner 能**機械證明**：此 payload 之 target 位於「發出預授權之 ticket」明確宣告之 allowed scope 內。在此之前，預授權自主 apply 不得於非 fixture 路徑啟用。

## 解法尚未選定（AI-0 之首要職責）

**本票 MUST NOT 預設解法。**「payload 加 `issuing_ticket_id` ＋ `declared_scope`」是合理假說，非已驗證之最小解。AI-0 MUST 先回答下列六問，再由 Akira 裁決實作方向：

1. registry、`deliver/` 票檔，或其他本地 artifact 中，何者可作**唯一且可驗證**之 authorization snapshot？
2. 該 snapshot 是否能由 stable ticket ID **機械定位**？
3. 票面 `ALLOWED` 可否被**穩定且無歧義**地編譯／解析為 path matcher？（逗號切分、相對路徑正規化、目錄前綴 vs 檔案、glob 語意）
4. ticket revision 變更、registry `path` 更新、`deliver/` 歸位之後，**哪一份**才是授權時點之 immutable 表示？
5. runner 能否**只讀本 repo、版本控制下、可重現**之資料源完成 scope binding？（不引入跨 repo 讀取、網路、外部服務）
6. 若不能，最小之 immutable authorization snapshot 要**在何時、由誰**產生，以**何種可驗證 fingerprint** 與該次 dispatch 綁定？

**判定原則**：若答案顯示需 runner 解析 free-form ticket Markdown、讀取 registry 以外之外部服務，或依賴非版控環境 → 應考慮改為「由簽發程序產生、具可驗證 fingerprint 之 immutable authorization snapshot」置於 payload；但此僅能在 AI-0 evidence 支持後決定，**不得於草案階段寫死**。

## Verify Config
TICKET_ID=RGR-SCOPE-BIND-01
CREATED=2026-09-19
PRESET=feature
ALLOWED=（待 AI-0 回流後依所選解法填定；草案階段 MUST NOT 預填）
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json,.claude/skills/ticket-rigor-gate/runner/prompts/,.claude/skills/ticket-rigor-gate/runner/profiles/,.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/rules/ssot-governance.md,.claude/PROGRESS.md,app/,plans/cso_briefing/project/deliver/
MAX_LINES=N/A
DIFF_REF=（待派工時以 live HEAD 填定）
ANCHOR_PROBED=（待 AI-0 live read 回填）
BASE_COMMIT=（待派工時以 live HEAD 填定）
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=（待 AI-0 定解法後判定；預期為 feature: token，MUST NOT 預填 NONE）
DEPENDENCY_DELTA=（待 AI-0 定解法後判定）

**草案階段欄位留空之依據**：TPL v3.11 要求 `ALLOWED` 與 capability 由實際標的決定。本票之標的取決於六問之答案（可能落在 payload schema、runner、或簽發側工具），於 AI-0 之前預填即為憑推測開票。**派工前 MUST 補齊全部留空欄位**，未補齊者禁派工。

## 一手 anchors（已固定，AI-0 MUST 逐項復驗）
- `repair_runner.sh` L310：`IRREVERSIBLE_TARGET_PATTERNS` 現值（五項 pattern，逐字如上表）
- `repair_runner.sh` L367–410：`blocker_preflight()` 全函式，無逐票 scope 比對
- `repair_runner.sh` L393–394：目前唯一之 `target.file_path` gate
- `repair_runner.sh` **L686**：`TARGET_FILE` 自 payload 取值，無 scope 驗證。（草案載 L671；anchor 漂移已於 2026-09-20 校正，歸因 `b172441` 於 `validate_decision_leakage()` 新增 25 行。分類 `EVIDENCE_GAP`：未改 ALLOWED／HC／AC expected outcome／任何實作決策，依 O4 不開 Rev。）
- `ticket-repair-payload.v1.json`：無 issuing-ticket scope 承載欄位
- `REPAIR_ROUTING.md` L44–49：`Dispatch Authority` 條件②之法條字面
- `RGR-P0A-02` AI-2 第 6 點與 HC 9：fixture-only 暫時邊界之位置與理由

## Action Items

### AI-0: 六問調查與可行性取證（唯讀，草案階段之唯一 AI）
- **動作 A**: 逐項復驗上節七處 anchor 現值；任一不符 → 停止回報。
- **動作 B**: 逐條回答六問，每答附可定位證據（檔案路徑＋行號，或 registry API 欄位）。禁以「應該可以」作答；無法取證者標 `UNKNOWN`。
- **動作 C**: 就每個可行候選解法，列出：所需 ALLOWED 檔案、是否引入新 capability／dependency、對既有 `addendum_only` 流程之相容性、以及 fail-closed 失敗形狀。
- **動作 D**: `git rev-parse HEAD`、`git status --short`、候選 ALLOWED 檔之 `git check-ignore -v`。
- **停止條件**: 六問中任一之答案顯示須解析 free-form Markdown、讀取外部服務或依賴非版控環境 → 停止回報並標明該問，**不得**自行選定替代解法。
- [x] 完成（2026-09-20 唯讀取證；回流見下節）

#### AI-0 回流（2026-09-20，唯讀）

**動作 A — 七處 anchor 復驗**：六處現值相符（L310 五項 pattern 逐字、`blocker_preflight()` L367、唯一 gate L393-394、payload schema 無 scope 欄、`REPAIR_ROUTING.md` 條件② L45 含「never the body of another already-signed ticket」、P0A-02 HC 9 在位）；一處漂移 L671→L686，已於本票 anchors 節校正。

**動作 B — 六問逐條**：
1. issuing ticket ID 唯一且 repo-readable 之 durable source：**部分**（CONFIRMED）。ID 唯一性成立（registry `id`、payload schema L7 `ticket_id` required minLength 1），但載體 `TICKET_REGISTRY.json` 已退出 git 追蹤——`82840aad`（2026-08-27 INFRA-TRACKSCOPE-01）；`.gitignore` L81-84 僅保留 `deliver/`／`void/`／`file/`。
2. 結構化可機械讀取之 scope 表示：**否**（CONFIRMED）。registry 結構化欄位無 `allowed`／`scope`；grep `ALLOWED` 於 registry 僅命中自由文字 note。唯一表示為票面 Verify Config 之 `ALLOWED=` 逗號字串。
3. 簽發時 immutable authorization snapshot：**不存在**（CONFIRMED）。registry 記錄可變（2026-09-20 實證：P0A-02 revision 412→414，且 `set_status(CLOSED)` 清空 `checkpoint` 整欄）；`Pending/` 票檔可變；`deliver/` 凍結版產生於收尾，晚於授權時點。
4. runner 只讀 repo-root、版控、可重現資料源完成綁定：**否**（CONFIRMED）。派工當下票檔在 `Pending/`（gitignore）、registry 亦 gitignore；唯一版控之 `deliver/` 在授權時點尚無該票。→ **停止條件命中**。
5. scope matcher 安全性：**單 root 可行**（CONFIRMED）／**多 root 與 glob UNKNOWN**。`realpath -m --` ＋ 帶末端分隔符前綴（runner L617-620），sibling-prefix 由 P0A-02 Case 5b 正向證明，CWD 歧義由 `RGR-REPORT-ANCHOR-01` L108 fail-closed 封住。未覆蓋：逗號多 root、目錄前綴 vs 單檔、glob 語意。matcher 非本票 blocker。
6. `expected_old_text_hash` 缺失時 live authorization 仍可能有效：**是，實質缺口**（CONFIRMED）。schema `target.required=["file_path","write_mode"]`，hash 為 optional 無 minLength（L26-31）；runner 以 `if [[ -n "$expected_hash" ]]` 包住整段 RTR-008（L563-568），缺欄即完全跳過 preimage 驗證。`REPAIR_ROUTING.md` L46 條件③要求「preimage matches the **declared** value」，未宣告時該條件空洞成立。

**停止條件命中（第 4 問）**：現有 runtime 讀取路徑三者——解析 `Pending/` free-form Markdown（撞 HC 6）、讀 gitignored 且可變之 registry（撞 HC 5）、依賴授權時點尚不存在之 `deliver/` artifact——均不符本票對 tracked／reproducible／non-free-form authorization source 之約束。依 HC 4 fail-closed，現階段不得以任何 runtime 讀取實作 scope binding。AI-0 不自行選定替代路徑。

**動作 C — 候選路徑（僅列舉，未取證比較，未選定）**：A 簽發側 snapshot 入 payload（需新 producer＋`ALLOWED` 編譯語意＋snapshot lifecycle＋fingerprint model＋schema 新欄）／B registry 重納版控（翻 `INFRA-TRACKSCOPE-01`，且不解 Q2、Q3）／C registry 新增 `allowed` 欄（解格式、不解信任與重現性）／D 維持 fixture-only。

**Akira 裁決（2026-09-20）**：採 **D**。A 延後不實作（其實為新能力，非本票小補丁），B／C 不批准。`expected_old_text_hash` 不作全域 v1 schema／runner retrofit——列為未來 live authorization contract 之不可省略 gate，與 issuance-time 設計一併裁定。lifecycle 改 `BLOCKED`（非 `VOID`：P0A-02 closure note 之承重指標仍指向本票，`VOID` 會誤導為缺口已消失）。

**殘餘風險（不得省略）**：fixture-only 僅使 **P0A-02 新增之 preauthorization override** 對 non-fixture target 之 blast radius 為零；**不**等於既有 `addendum_only` 路徑已有逐票 scope 綁定。該既有路徑現由 RTR-009 target 一致性（L555）、不可逆目標黑名單（L310）、write mode 約束與寫入前像備援（L770）constrain，但四者均非「已簽發票 → target」之 immutable 綁定；且 `Pending/` 與 registry 為 gitignore，誤寫無 git 回退，回復成本較高。

**重開觸發（可指認事件，非語義判準）**：出現第一個 target 不在 `fixtures/**`、且要求使用 `preauthorized_autonomous_apply` 之實際 payload 需求。該事件出現前，禁為未來可能性預建 issuance pipeline；事件出現後，本票自 `BLOCKED` 回 review／planning，依六問重新評估當時之 tracked source、scope representation、snapshot 與 preimage contract，**不得**直接沿用候選 A。

### AI-1～AI-n: 待定
**草案階段刻意留空。** 實作項目由 Akira 依 AI-0 回流一次性裁定後補入。**MUST NOT** 於此之前撰寫或執行任何實作 AI。

## Hard Constraints
1. 本票 **MUST NOT** 解除 `RGR-P0A-02` 之 fixture-only 限制。解除須另開明示之 follow-up 票（P0A-02 HC 9）。
2. **MUST NOT** 修改 `REPAIR_ROUTING.md`、`OPS_CALIBRATION.md` 或 `SKILL.md`——條件②之法條已由 `RGR-DISPATCH-AUTH-01` 立法，本票只補機械 enforcement，不改法。
3. **MUST NOT** 修改 CB-1～CB-4、`RTR_RETRYABLE`、RTR-002／005／007／009／012 之既有判定，亦不得擴充 write mode。
4. scope binding **MUST** fail-closed：無法取得或驗證 authorization snapshot 時，一律視為不在 scope 內並拒絕，**MUST NOT** 放行。
5. **MUST NOT** 引入跨 repo 讀取、網路呼叫、外部服務或非版控資料源作為 scope 判定之 producer。
6. **MUST NOT** 令 runner 解析 free-form ticket Markdown 作為授權依據——若唯一可行路徑如此，停止回報並升裁。
7. 草案階段 **MUST NOT** 撰寫實作 AI、**MUST NOT** 預填 `ALLOWED`／`INTRODUCED_CAPABILITY`／`BASE_COMMIT`。

## Error Handling
- 六問任一答案為 `UNKNOWN` 且該問為解法選擇之必要輸入 → 停止回報，不得以假設補足
- AI-0 發現既有 anchor 現值已漂移（他票已改動 `blocker_preflight()` 或 payload schema）→ 停止回報，重新取證
- 候選解法需修改 FORBIDDEN 路徑方能成立 → 停止回報
- **[標配]** `git check-ignore -v` 命中任一候選 ALLOWED 檔 → 停止回報

## Acceptance Criteria（草案；實作 AC 待解法定案後補入）
- [x] **[存在性]** AI-0 六問逐條作答，每答附可定位證據或明標 `UNKNOWN`（回填證據定位 `§AI-0 回流（2026-09-20）動作 B`）
- [ ] **[存在性]** 候選解法清單含各自之 ALLOWED、capability／dependency delta、相容性影響與 fail-closed 失敗形狀
- [x] **[存在性]** 七處 anchor 現值逐項復驗，與票面相符或差異已明列（回填證據定位 `§AI-0 回流（2026-09-20）動作 A`；一處漂移 L671→L686 已校正）
- [ ] （實作 AC 待補——解法定案前 MUST NOT 填寫）
