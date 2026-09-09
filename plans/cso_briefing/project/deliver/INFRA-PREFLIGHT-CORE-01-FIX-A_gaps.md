# INFRA-PREFLIGHT-CORE-01-FIX-A: 收斂母票 INFRA-PREFLIGHT-CORE-01 六項未閉環項目

**簽發日期**: 2026-09-08
**簽發者**: Claude
**來源議題**: INFRA
**Mode**: normal → uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-PREFLIGHT-CORE-01`（母票，`deliver/`；本票六項承接條目之判準、程式碼落點、既有測試皆源自母票 AI-3.1-b/AI-5.1/AI-6/AI-7 之產物）
- `coordination_dependency`: 無

母票 `INFRA-PREFLIGHT-CORE-01` 一次性票級 cross-review（Codex R1，ADVISORY，`5fd5dac..4bf0253`，
receipt `.tmp/cross_review_pass/INFRA-PREFLIGHT-CORE-01`）收尾時，六項未閉環項目依 ssot-governance.md
「修補票紀律」同批開立本票，承接轉 PASS 條件。母票四項為既有誠實未勾 AC（AI-7 已標 pending／planned），
兩項為本輪 Cross-Review 新揭露（`sv_report.md` Cross-Review Result 段落，逐字附加於母票收尾證據）。

## Verify Config
TICKET_ID=INFRA-PREFLIGHT-CORE-01-FIX-A
CREATED=2026-09-08
PRESET=bugfix
ALLOWED=.claude/skills/qa/scripts/preflight_activation.sh,.claude/skills/qa/tests/test_preflight_adapter.sh
FORBIDDEN=.claude/skills/qa/scripts/preflight_core.py,.claude/skills/qa/scripts/preflight_rules.json,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/sv/**,.claude/rules/**,.claude/settings.json,.claude/CLAUDE.md,app/**,tests/**
MAX_LINES=N/A（安全不變量類票：R-7 fail-closed 邊界修補，失敗形狀枚舉為審查發現式）
DIFF_REF=fb107c6
ANCHOR_PROBED=2026-09-08 14:58
BASE_COMMIT=fb107c6
TARGET_BLOB=.claude/skills/qa/scripts/preflight_activation.sh:47a4d120
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/qa/scripts/preflight_activation.sh`
- **動作**: 確認 `invoke_core_decision()` 之 protocol 完備性檢查（現行 L484-489 一帶）逐字命中，
  確認其現值僅檢查 `CORE_FINAL_STATUS`／`CORE_EXIT_CODE`／`CORE_MAY_ACTIVATE_TICKET`／
  `CORE_REPO_ROOT`／`CORE_WORKTREE_PATH`／`CORE_HEAD_SHA` 六欄非空；確認 `test_preflight_adapter.sh`
  之 `_mtime()` 現值為 `stat -c '%Y'`（秒級）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成：`invoke_core_decision()` 於現行 L449-454（非 L484-489，行號漂移屬其餘 AI-6/AI-7 編輯自然位移，內容逐字相符，票面「一帶」措辭已預先容納）逐字命中六欄非空檢查：`CORE_FINAL_STATUS`／`CORE_EXIT_CODE`／`CORE_MAY_ACTIVATE_TICKET`／`CORE_REPO_ROOT`／`CORE_WORKTREE_PATH`／`CORE_HEAD_SHA`；`test_preflight_adapter.sh` L29 `_mtime()` 確認為 `stat -c '%Y'`（秒級）。基線 assertion 總數 36（AI-6 收尾 `5b5d294` 追加後值）

### AI-1: invoke_core_decision() 三欄非空檢查補強
- **檔案**: `.claude/skills/qa/scripts/preflight_activation.sh`
- **現值**: protocol 完備性檢查僅驗證六欄非空（不含 `CORE_WRITE_AUTHORITY`／`CORE_TICKET_ID`／
  `CORE_CONTEXT_ID`）
- **目標值**: 以**單一 required-field 列表**取代散落之 `[[ -n ... ]]` 條件串，`invoke_core_decision()` 依該列表逐一檢查非空：
  ```bash
  readonly CORE_REQUIRED_FIELDS=(
    CORE_FINAL_STATUS CORE_EXIT_CODE CORE_MAY_ACTIVATE_TICKET
    CORE_WRITE_AUTHORITY CORE_TICKET_ID CORE_CONTEXT_ID
    CORE_REPO_ROOT CORE_WORKTREE_PATH CORE_HEAD_SHA
  )
  ```
  任一缺漏 → `CORE_UNAVAILABLE_REASON="CORE_PROTOCOL_INVALID"`，與現行六欄缺漏同一 fail-closed 分支（`return 1`，不設定 `CORE_AVAILABLE=1`）
- **列表為 required-field SSOT**：AI-2 之 omission 測試 MUST 自同一列表衍生，禁手寫第二份欄位集合（Codex R1 之根因即為「欄位被使用卻未納入完備性檢查」之兩處字面漂移）
- **本項為 FIX-A 唯一允許之結構性改動**（依 O5 外審回流預算律）；不得順帶重構其餘任何函式
- [x] 完成：`readonly CORE_REQUIRED_FIELDS=(...)` 於檔案頂層（`PREFLIGHT_CORE_PY` 宣告後）新增，九欄逐字相符；`invoke_core_decision()` 原六欄手寫 `[[ -z ... ]]` 串接判斷改為 `for _core_field in "${CORE_REQUIRED_FIELDS[@]}"` 迴圈 + 間接展開 `${!_core_field}` 逐一檢查非空，缺漏時 `CORE_UNAVAILABLE_REASON="CORE_PROTOCOL_INVALID"`（與六欄缺漏同一 fail-closed 分支）。未觸碰任何其餘函式。`git diff --stat` 僅 `preflight_activation.sh` 一檔異動

### AI-2: Scenario ⑤ — protocol required-field 單欄缺漏回歸（三欄各自獨立子案）
- **檔案**: `.claude/skills/qa/tests/test_preflight_adapter.sh`
- **現值**: Scenario ④ 僅測「四欄各自不符」（identity 欄），無單一 protocol 欄位缺失情境
- **目標值**: 新增 Scenario ⑤。以**同一份有效 core-response fixture** 為基底，對 `CORE_WRITE_AUTHORITY`／`CORE_TICKET_ID`／`CORE_CONTEXT_ID` **各自**做單欄省略；每個子案 MUST 保持其餘欄位（含 identity 四欄）全部合法。
- **MUST 為三個具名子案（`subTest` 或三個命名 shell case），禁合併為單一多欄缺失案**——單欄測試才真正證明「任一必要欄位缺失即 fail-closed」之不變量；只測 `CORE_WRITE_AUTHORITY` 則另二欄日後被移除、拼錯或重構漏掉皆不會被偵測
- **每子案斷言**: `CORE_PROTOCOL_INVALID`／`CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH`／`ADVISORY_ONLY=true`／`exit 1`／**P3 writer sentinel=0**／`active.json` mtime_ns 不變
- **子案清單 MUST 自 AI-1 之 `CORE_REQUIRED_FIELDS` 衍生**（涵蓋全部 required fields，非僅本次揭露之三欄）
- [x] 完成：實作超出票面文字之「三個具名子案」最低要求，改依票面稍後條款「MUST 自 CORE_REQUIRED_FIELDS 衍生，涵蓋全部 required fields」之嚴格版——`mapfile -t _REQUIRED_FIELDS < <(sed -n '/^readonly CORE_REQUIRED_FIELDS=(/,/^)/p' "$SCRIPT" | tail -n +2 | grep -oE 'CORE_[A-Z_]+')` 直接自 `preflight_activation.sh` 陣列宣告萃取（非手寫第二份集合），斷言萃取結果恰為 9 欄；九個具名子案（Scenario R7-4）逐一單欄省略，其餘欄位含 identity 四欄皆合法；每子案斷言 `CORE_PROTOCOL_INVALID`／`CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH`／`ADVISORY_ONLY=true`／exit 1／zero-write（sentinel+mtime_ns 雙證據）全數 PASS（9×6=54 項斷言）

### AI-3: 零寫入取證雙證據化（ns 級 mtime ＋ writer sentinel）
- **檔案**: `.claude/skills/qa/tests/test_preflight_adapter.sh`
- **現值**: `_mtime()` 使用 `stat -c '%Y'`（秒級精度），同秒內寫入無法偵測
- **目標值 (a) ns 級量測**: **禁用 `stat -c '%Y.%N'`**——`%N` 於 GNU/BSD `stat` 實作與檔案系統解析度不一致，非可攜之純奈秒小數。改採 Python 標準庫，且 **MUST 於 PATH 遮蔽前解析絕對路徑**，使 harness 在「`python3` 不可達」情境下仍保有量測能力：
  ```bash
  HOST_PYTHON3="$(command -v python3)" || exit 2
  _mtime_ns() { "$HOST_PYTHON3" -c 'import os,sys; print(os.stat(sys.argv[1]).st_mtime_ns)' "$1"; }
  ```
  故障注入 MUST 僅遮蔽**受測腳本**之 PATH，MUST NOT 使 harness 自身失去量測能力
- **目標值 (b) 雙證據定義**: `zero_write` ≡ (P3 writer call sentinel = 0) **∧** (`active.json` mtime_ns 未變)。mtime 為後驗檔案觀察、sentinel 為 writer 路徑觀察；**單一證據不足**——僅 mtime 會被「同內容寫入」誤判為零寫入，僅 sentinel 會漏掉其他寫入路徑
- **目標值 (c) 精度刺激固定**: 驗證程序 MUST 為——①建立測試專用 `active.json` 副本（temporary directory）②讀 `mtime_ns_before` ③同一 shell process 內立即以已知不同內容覆寫 ④讀 `mtime_ns_after` ⑤斷言兩者不同**且**內容 hash 不同 ⑥還原或棄置 fixture
- **降級揭露**: 若底層檔案系統解析度確實不足 → 結果 MUST 標 `LIMITED_BY_FILESYSTEM_RESOLUTION`，並以內容 hash／inode／writer sentinel 補強；**MUST NOT** 僅留一句註解即宣稱精度已加固
- [x] 完成：(a) `HOST_PYTHON3="$(command -v python3)"` 於檔案頂層、任何 PATH 遮蔽動作前解析；`_mtime_ns()`／`_content_hash()` 皆經 `$HOST_PYTHON3` 絕對路徑呼叫，R7-1（python3 PATH 遮蔽情境）下 harness 自身量測能力實測仍正常（`MTIME-1` 探針獨立於 R7-1 之前執行，且 R7-1 內之 mtime 讀取呼叫亦成功）。(b) `_zero_write()` 雙證據：sentinel＝stderr 是否含 `verify_checklist.sh::write_active_json()`（FORBIDDEN 檔，未修改）唯一於成功寫入後輸出之 `📌 active.json:` 字面標記；file_unchanged＝依 `MTIME_RESOLUTION_OK` 擇一使用 mtime_ns 或 content hash 比對；兩者皆需成立方判零寫入。(c) 精度探針 `MTIME-1`：`tmp/verify/.precision_probe.$$` 臨時目錄建立測試專用檔，同 process 內連續寫入 `aaa`→`bbb`，讀 mtime_ns 前後值＋content hash 前後值，斷言 content hash 必異（不受解析度影響）；**本機實測**：mtime_ns 前後值確實不同（`before=1788930696633470200 after=1788930696954329500`，NTFS 100ns tick 解析度充足），`MTIME_RESOLUTION_OK=1`，未觸發降級分支；惟程式碼已含 `LIMITED_BY_FILESYSTEM_RESOLUTION` 降級路徑（`MTIME_RESOLUTION_OK=0` 時），未執行但邏輯完整可稽（若移至解析度不足環境重跑會自動切換至 content-hash-only 判定，不需改碼）。臨時目錄執行後即 `rm -rf`，不殘留

### AI-4: R-1 四型 adapter 層全流程驗證補齊（真實 collector stimulus）
- **檔案**: `.claude/skills/qa/tests/test_preflight_adapter.sh`
- **現值**: 僅 `MISMATCH` 一型於 adapter 全流程以 `active.json` mtime 不變驗證；`TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE` 三型僅於 Python 核心單元層驗證
- **⛔ 禁以 stub core output 達成**：僅 stub core response 最多證明「adapter 收到 `may_activate_ticket=false` 時不寫」，**繞過最關鍵的 collector 轉譯層**，無法證明原始 Git／ticket 失敗是否真的映射為對應四型、亦無法證明該 facts 正確傳入 core。完整鏈為：原始失敗 → `blob_check` → facts JSON → core decision → adapter side effect
- **目標值（四案，各以真實刺激驅動）**:

| 型別 | 刺激方式 | 應驗證 |
|---|---|---|
| `MISMATCH` | ticket `TARGET_BLOB` 指向格式合法但異於實際 blob 之 SHA | `blob_check=MISMATCH`、blocked、`may_activate_ticket=false`、writer sentinel=0 |
| `TARGET_MISSING` | 格式合法但 path 不存在之 target | `blob_check=TARGET_MISSING`、blocked、writer=0 |
| `COMMAND_FAILED` | 以 PATH shim／wrapper 令 `git hash-object` 對既存 target 非零退出 | `blob_check=COMMAND_FAILED`、blocked、writer=0 |
| `INVALID_TICKET_VALUE` | 格式不合法之 `TARGET_BLOB` 值 | `blob_check=INVALID_TICKET_VALUE`、blocked、writer=0 |

- **⛔ exit code 分組（寫死，禁混用）**: 四案在 V0 PASS 前提下依 R-5 相容性 **exit 0**，**不是 1**。R-7 之 core-unavailable／protocol-invalid／identity-mismatch 為 **exit 1**，屬 R-5 明示例外。二組 MUST 分開斷言。**若對 R-1 四型套用 exit 1 斷言，將與母票 `test_exit0_v0pass_with_blocked_scope_or_target_mismatch` 直接矛盾，且錯的是本票**——該具名測試存在之全部理由即為鎖住此區分
- 零寫入判定依 AI-3 之雙證據定義
- [x] 完成：四案（R1-1..R1-4）皆以真實刺激驅動，非 stub core output——①`R1-1 MISMATCH`：`FIXA_TICKET_FILE` `TARGET_BLOB` 改指既存檔案＋錯誤 sha8（`deadbeef`）；②`R1-2 TARGET_MISSING`：改指格式合法但不存在之路徑；③`R1-3 COMMAND_FAILED`：以 `tmp/verify/.git_shim.$$/git` PATH shim（僅攔截 `hash-object` 子命令固定 exit 1，其餘子命令 `exec` 委派真實 `git`）令既存 target 之 `git hash-object` 非零退出；④`R1-4 INVALID_TICKET_VALUE`：`TARGET_BLOB` 設為無冒號之格式不合法值。雙層驗證：(i) 直接呼叫既有未修改之 `collect_blob_check_fact()`（source 腳本後呼叫，非 stub），斷言精確回傳型別——四案全數 got=expect 精確相符；(ii) 全流程黑箱執行 adapter，斷言 `exit 0`（V0 PASS，R-5 相容，非 R-7 之 exit 1）、zero-write（sentinel+mtime_ns 雙證據確認 writer 未呼叫）、`WRITE_AUTHORITY=NO`。四案 4×4=16 項斷言全 PASS。`FIXA_TICKET_FILE`（`INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` 自身，非母票 deliver/ 凍結檔）之 `TARGET_BLOB` 欄位每案 `sed -i` 覆寫，四案跑畢後以 `cp` 備份還原並 `diff -q` 驗證位元組級一致（額外 1 項斷言，PASS），恢復原值 `47a4d120`

### AI-5: 雙 worktree identity 實機可分辨性驗證
- **檔案**: N/A（實機驗證，無程式碼異動）
- **⚠️ 名稱與範圍已收窄**：本項驗證 **R-4 identity facts 之實機可分辨性**，**不是**跨 worktree 並行安全。母票非目標 §8 僅約束「同一 worktree 同時寫入多票須禁止或序列化」，從未承諾提供跨 worktree 鎖或完整並行隔離
- **W1（必做）identity separability**: 建立第二個 `git worktree`，兩 worktree 各自對**不同** ticket 執行一次 preflight；記錄兩側 `repo_root`／`worktree_path`／`head_sha`／ticket ID 與 run 時間窗；**證明兩筆 `worktree_path` 不同**
- **W2（選作）overlap safety**: 兩側 preflight 在時間上重疊執行，保存兩側 run ID、起訖時間、輸出與 `active.json` observation，檢查有無誤綁或交叉寫入
- **⛔ precondition（W2 前置，MUST 先確認）**: 兩 worktree **不得共用同一 `tmp/verify/active.json`**。若共用，測試本身引入全域 mutable state 競爭，觀測結果不可信 → 停止回報，禁在共用狀態下宣稱並行結論
- **⛔ 結論界線**: 結果僅得表述為「R-4 identity facts 於實機可分辨」；**MUST NOT** 推論為已提供跨 worktree 鎖、無競態保證或全面並行安全
- [x] W1（必做）完成，W2（選作）跳過（票面明示選作，範圍/時間考量不做）：
  - `git worktree add -b fixa-wt-test-tmp ../SmartCameraApp1-wt-test HEAD` 建立第二 worktree（disposable 分支）
  - **前置確認**：新 worktree `tmp/verify/` 建立前為空（`ls` 零輸出），確認與主 worktree 無共用 `active.json`（各 worktree 之 `tmp/` 為未追蹤獨立實體目錄，非符號連結）
  - **實測結果**（兩側對不同 ticket）：
    | worktree | repo_root | branch | head_sha | ticket_id | run 時間窗（UTC） |
    |---|---|---|---|---|---|
    | 主 | `D:/DevSources/AndroidProj/SmartCameraApp1` | `main` | `711f6b92a1e553b802248d69f339608091c6460f` | `INFRA-PREFLIGHT-CORE-01-FIX-A` | `2026-09-09T05:07:47Z`–`05:07:50Z` |
    | 副 | `D:/DevSources/AndroidProj/SmartCameraApp1-wt-test` | `fixa-wt-test-tmp` | `711f6b92a1e553b802248d69f339608091c6460f` | `INFRA-TPL-V310-01` | `2026-09-09T05:08:00Z`–`05:08:03Z` |
  - **`worktree_path` 確認相異**：`D:/DevSources/AndroidProj/SmartCameraApp1` ≠ `D:/DevSources/AndroidProj/SmartCameraApp1-wt-test`（`REPO_ROOT=` stdout 逐字對照）。`head_sha` 兩側相同屬預期（新 worktree 建立自同一 HEAD，非缺陷）
  - **附帶發現（非本項 AC 要求，如實記錄）**：`plans/cso_briefing/project/TICKET_REGISTRY.json` 為 gitignored 檔，`git worktree add` 不會複製未追蹤檔案至新 worktree——新 worktree 初次執行 preflight 時 `lookup_ticket_file()` 因查無該檔而回 `V0_STATUS=BLOCKED_ENV_MISMATCH`；本次測試以 `cp` 手動同步一份快照方使新 worktree 之票務解析可用。此為操作面真實限制（非本票 scope 缺陷），供後續若有跨 worktree 常態使用需求時參考
  - **結論**（依票面結論界線）：R-4 identity facts（`repo_root`／`worktree_path`）於雙 worktree 情境下確認可分辨。**不推論**為已提供跨 worktree 鎖、無競態保證或全面並行安全（W2 未執行，無 overlap 觀測證據）
  - **清理**：`git worktree remove ../SmartCameraApp1-wt-test --force` + `git branch -D fixa-wt-test-tmp`，`git worktree list` 確認僅剩主 worktree，測試未殘留任何常設狀態

## Hard Constraints
1. 禁止修改 `preflight_core.py` 之 `decide()` 判定邏輯本體（AI-1 僅為 adapter 層 protocol 檢查補強，
   非核心判定規則變更）
2. 禁止新增對外 stdout key 以外之副作用（AI-1 修補 MUST NOT 影響 R-7 既有 stdout 契約）
3. AI-5 為實機驗證項，MUST NOT 以「理論上應可區分」代替真實雙 worktree 觀測
4. **禁以 `v2_semantics=FAILED` 或 `v2_provenance=KNOWN` 補 allowlisted producer**。母票 AC（`:480`）**已勾**且斷言為「注入該二值均命中 `UNLISTED_COMBINATION`」（`ProducerClosureTest`）；將其變為合法 producer 會使一條已通過之母票 AC 變成假的，直接違反 D.3-4「不追溯改寫 AI-0～AI-7 完成事實」
5. **禁在本票進行「新增一條規則」可維護性演練**（原 AI-6 已移除，見下）。該演練與本票 P0（R-7 protocol 完備性）無依賴關係，且必然觸及 `preflight_core.py`／`preflight_rules.json`（本票 FORBIDDEN），徒增變更面與審查噪音。若日後仍要驗證可維護性，MUST 另票並改為**唯讀／非持久性**形式：於暫存副本驗證 rule-table loader 對新增合法 row 之 schema／測試同步機制，不得提交、不改正式 `preflight_rules.json`、不建立 `FAILED` producer、不改 `decide()`
6. **本票唯一允許之結構性改動為 AI-1 之 `CORE_REQUIRED_FIELDS` 單一列表**；其餘一律照現行結構最小修改（O5 外審回流預算律）

## Error Handling
- `invoke_core_decision()` 現值與票面描述不符 → 停止回報
- **[已撤除]** ~~`test_preflight_adapter.sh` 現行斷言數與票面「28→36」不符 → 停止回報~~ —— **撤除理由**：本票必然新增 Scenario ⑤、R-1 四案與 mtime 案，斷言數必然不再是 36；以歷史固定計數作機械 STOP gate，會使票因自身新增 coverage 而卡死。**把會隨 coverage 自然變動的計數誤當作不可變安全不變量**，正是「跑跑停停」之典型來源
- **[取代條款]** 基線由 AI-0 記錄**實測 assertion count 與 case ID 清單**；收尾 MUST 列出 case ID／assertion delta／完整測試結果。**若既有 R-7、R-4 case ID 消失、被弱化或無法重跑 → 停止回報**。MUST NOT 以固定歷史 assertion 總數作為驗收或 STOP 判準
- R-1 四案任一以 stub core output 替代真實 collector stimulus → 停止回報
- 任一測試 FAIL → 停止回報，禁改實作或核心讓測試過
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報（gitignore 死結律）

## Acceptance Criteria
- [x] **[存在性]** `invoke_core_decision()` 之 required-field 檢查由單一 `CORE_REQUIRED_FIELDS` 列表驅動，含全部九欄；`grep` 可指認列表為唯一欄位集合來源（無第二份手寫集合）—— `readonly CORE_REQUIRED_FIELDS=(...)` 為唯一宣告點；`test_preflight_adapter.sh` 之 R7-4 子案清單以 `sed`/`grep` 自該陣列動態萃取，非另立第二份
- [x] **[行為性]** given 一份其餘欄位（含 identity 四欄）全部合法之 core-response，expect 省略 `CORE_WRITE_AUTHORITY`／`CORE_TICKET_ID`／`CORE_CONTEXT_ID` **任一單欄**時 → `CORE_PROTOCOL_INVALID`、`CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH`、`ADVISORY_ONLY=true`、exit 1、writer sentinel=0、`active.json` mtime_ns 不變。**三欄各有獨立具名子案**，非合併案 —— 實作擴大為全九欄各自獨立子案（超越票面最低要求，見 AI-2 完成記載），三欄子集包含於其中且全數 PASS
- [x] **[行為性]** given 同一 shell process 內同秒兩次不同內容寫入，expect `mtime_ns_before ≠ mtime_ns_after` 且內容 hash 不同；量測經 `HOST_PYTHON3` 絕對路徑，於受測腳本 PATH 遮蔽情境下仍可運作。解析度不足時結果標 `LIMITED_BY_FILESYSTEM_RESOLUTION` 並附補強證據 —— `MTIME-1` 探針：mtime_ns 前後值相異（本機 NTFS 解析度充足）、content hash 前後值相異；R7-1 情境下 harness 自身量測能力保留（`HOST_PYTHON3` 絕對路徑）
- [x] **[行為性]** R-1 四型皆以**真實 collector stimulus**（非 stub core output）於 adapter 全流程驗證：各案 `blob_check` 為對應值、blocked、writer sentinel=0、mtime_ns 不變、**exit 0**（V0 PASS 前提，R-5 相容）—— R1-1..R1-4 全數 PASS，見 AI-4 完成記載
- [x] **[實機]** 雙 worktree 各對不同 ticket 完成一次 preflight；兩筆 `worktree_path` 不同；記錄兩側 `repo_root`／`worktree_path`／`head_sha`／ticket ID 與 run 時間窗。**結論僅限 identity 可分辨性，不得表述為並行安全** —— 見 AI-5 完成記載，實測表格 + 結論界線遵守
- [x] **[存在性]** 收尾列出 case ID 清單與 assertion delta（對 AI-0 基線）；既有 R-7、R-4 case ID 全數保留且可重跑 —— 見下方「收尾證據」節：case ID 清單、AI-0 基線 36 → 現值 110（delta +74），R7-1/R7-2/R7-3/R4-1..4 七個既有 case ID 全數保留、重跑全 PASS
- [x] `assembleDebug` PASS —— `./gradlew assembleDebug` BUILD SUCCESSFUL（45s），如預期 no-op，票面 scope 二檔零涉入 build 產物
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 —— `git status --short` 僅 `preflight_activation.sh`／`test_preflight_adapter.sh` 二檔異動，皆在 ALLOWED

## 收尾證據：Case ID 清單與 Assertion Delta

**AI-0 基線**：36 assertions（AI-6 收尾 `5b5d294` 值）。**現值**：110 assertions（`bash test_preflight_adapter.sh` 全綠，110 passed, 0 failed）。**Delta**：+74。

**Case ID 全清單**（`═══` 區段標頭直取，各案精確 assertion 數逐項核對實跑輸出）：
- `MTIME-1`（新增，AI-3c）：2
- `R7-1`／`R7-2`／`R7-3`（既有，全數保留、可重跑，各 6）：18
- `R4-1`(ticket_id，6，含額外 2 項 stdout key 斷言)／`R4-2`(repo_root，4)／`R4-3`(worktree_path，4)／`R4-4`(head_sha，4)（既有，全數保留、可重跑）：18
- `R7-4`（新增，AI-2，1 項萃取數量斷言 + 九欄×6）：1 + 54 = 55
- `R1-1..R1-4`（新增，AI-4，各 4 + 收尾還原驗證 1 項）：16 + 1 = 17

總計 2+18+18+55+17 = **110**，與 `bash test_preflight_adapter.sh` 實跑輸出「110 passed, 0 failed」精確相符。

**既有 R-7／R-4 case ID 存活性**：`R7-1`、`R7-2`、`R7-3`、`R4-1`(ticket_id)、`R4-2`(repo_root)、`R4-3`(worktree_path)、`R4-4`(head_sha) 七個既有 case 於本輪重跑逐一比對票面 Error Handling 條款「MUST 列出既有 case ID 是否消失/弱化」——七者斷言內容與強度（zero-write／exit 1／ADVISORY_ONLY／reason_code／必要時之 stdout key）**逐字保留**，未見任何弱化或刪除。

---

## 收尾實錄（耐久載體＝本票檔；依四要件④，轉終態前落檔）

**碼閉環**：2026-09-09 AI-0～AI-5 全數完成，lifecycle→REVIEW；同日家族一次性收束轉 CLOSED。

**Commits**：`0ab9bbd`（AI-0～AI-5 實作）＋`c1a42e4`（`PROGRESS.md` 獨立提交）＋`32ffc19`（票檔搬 `deliver/`）。`BASE_COMMIT=416c711`。

**審查鏈**：`/qa` PASS → `/sv` 階段 A–C PASS → R0 三輪（R1 `CHECKPOINT-REQUIRED`：commit `9aa8922` 混 `PROGRESS.md`＋`.sh` 違 Rule 3，經 Akira 裁決選項 A 拆分為 `0ab9bbd`＋`c1a42e4` → R2 `FAIL`：`sv_report.md` scope 引用未同步 → R3 `FORWARD`）→ Codex Cross-Review 兩輪（R1 `REPORT_FIX`：AC「無第二份手寫集合」措辭過度宣稱 `_JSON_KEY_OF` mapping、scope AC 措辭未揭露 Pipeline Artifact，已修正 `sv_report.md` → R2 **PASS**）→ `structural_verify.sh --post` 一度 `REPORTFIX`（缺英文 Stage A/B/C 標頭字面），修正後 PASS(A–G)[ADVISORY]。R0 記錄三份於 `.tmp/fail-advisor/`，Codex 結果 `.tmp/cross_review_result_20260909_134237.md`。

**交付內容**：母票六項未閉環之收斂——九欄 `CORE_REQUIRED_FIELDS` 單一來源驅動之 protocol 完備性檢查（取代原六欄手寫串接）＋R7-4 九個具名子案（自陣列動態萃取，55 項斷言）；R-1 四型 adapter 全流程驗證；mtime 斷言精度限制揭露。

### Traycer 裁決：`PASS`（2026-09-09）

依據：Traycer 2026-09-09 對 `INFRA-PREFLIGHT-CORE-01` 票族之功能面終審 `execute_review`，裁決 **Path A — Approve**（無 T0、無未併批 T1；審查基準含本票交付票檔 `deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` 與 `preflight_activation.sh`／`test_preflight_adapter.sh` 等實作載體；工作樹無未提交變更，SMTR 一手驗證 `aud_20260909_8c113304ed`）。本票交付物確在該次審查範圍內，故填實測值 `PASS`，非推定。

### 忠實性補記（FIX-C HC-5 義務，已於冊面同步）

本票 AI-4 之 R1-2..R1-4（`TARGET_MISSING`／`COMMAND_FAILED`／`INVALID_TICKET_VALUE`）當時 PASS 之機制，經 `FIX-C` 根因分析證實為 **R-2 unlisted fallthrough 之偶然**，而非 F-2 正確判定：core 之 `_is_f2_blob_failure()` 綁 `v1_scope=NOT_EVALUATED ∧ v1_provenance=NOT_APPLICABLE` 前提，本票 commit 進入 `BASE_COMMIT..HEAD` 區間後 `v1_scope` 變為 `CHANGED_TARGETS`，該組合未列於任何 F 列 → 落入 R-2 fallthrough 恰巧 `BLOCKED`。**本票 AC 字面（blocked＋零寫入）當時觀測屬實，不撤銷**；此僅忠實記載其防護機制為偶然而非設計。根因已由 `FIX-C`（R-1 guard 前置化，commit `7e87014`）修復。

### 禁倒灌

`FIX-C` 之 core R-1 前置 guard 為 `FIX-C` 交付，非本票能力；本票亦未達成母票之多 worktree 並行／四入口實機／規則演練三項，該三項於 `SKILL.md` 已知未達項表維持 `pending`／`planned`。
