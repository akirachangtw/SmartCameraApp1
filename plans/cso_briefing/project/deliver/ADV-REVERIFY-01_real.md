# ADV-REVERIFY-01: 於現版 `advisor.claude-cli` 實作（≥`9a4fc213`）以至多兩次真實訂閱 CLI 呼叫重驗 Advisor 成果表 P1-A①②④⑤ 與 P1-C⑤（有票全鏈＋無票；「已知可做 vs 無法判定」對照併入，不另叫第三次）

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（`ADV-CLAUDECLI-01` 已 CLOSED，為基線）
- `evidence_dependency`: ADV-CLAUDECLI-01 —— 通道實作 `route.py`／`route_table.json` 現版、隔離案例建置法與證據嵌入慣例（〈證據附錄〉、〈AC12 原始事件嵌入〉）；`plans/roadmap/advisor-outcomes.md`（P1-A／P1-C 之約定、驗收方式與「待重驗」標記）
- `coordination_dependency`: 無（成果表之回填只經 `Skill(projman)`，本票不寫成果表）

## 來源（Parent）
- Parent type：Plan-backed；路徑 `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261024-autonomy-closeloop-plan.md`（Revision R3；Fixture false／Plan Gate PASS／Ticket mode eligibility ALLOWED）；plan-local item `T07`；讀取時間 2026-10-24。
- Selected item：ADV 現版真實重驗與 P1-A⑤ 對照（diag C10、C11）。
- 承重決策（Akira 2026-10-24 裁決，D5）：真實驗證**最多兩次**訂閱 CLI 呼叫；「按需取得」之對照**合併**於該兩次內，**不另叫第三次**。成本屬使用者已授權之上限：用盡即止，不得補額度、不得改走 API／計費通道。
- 票號歸屬：本票由「工程師自主閉環」議題（源自 ADV-CLAUDECLI-01 收束與 Advisor 成果表 P1 缺口）發起，依 Akira 2026-10-24 裁決與模板 v3.12「發起議題律」掛 `ADV` 前綴。

## 證據（開票時 live，2026-10-24）
- 成果表現況（`plans/roadmap/advisor-outcomes.md`，`f41c08ce`→`dd0f653f`）：P1-A 已驗收 1/5（③ PPSS）；①②④ 之真實呼叫發生於修復前實作 `781dcd49`（其後 `22770829`、`8e4088b4`、`5abbd211`、`9a4fc213` 改動判定與 cwd 路徑）→「待重驗」不計入；⑤ 缺「已知可做 vs 無法判定」對照。P1-C 已驗收 4/5；⑤ 之真實全鏈同屬修復前實作→待重驗。P1-B④（真實快取命中）**不屬本票**（不可製造）。
- 現版入口：`route.py` `advisor.claude-cli`（`route_table.json` 之 `cli` 區塊：bin `claude`、model `claude-opus-5-5`、effort `high`、timeout 180s）；有票用 `consult --incident <INC> --source advisor.claude-cli [--ticket]`，無票用 `--no-ticket --payload <PATH>`（`ssot-governance.md` Advisor 條）。
- 預算語義：每次真實呼叫＝一次 `claude -p` 子行程啟動；入口於 M1／M4／M5 等前置未過者**零啟動**（不計預算）；入口執行記錄寫 git-ignored `tmp/<ticket>/advisor_cli/`（非耐久，故證據須逐字嵌入票檔，沿用 ADV-EVEMBED-01 之慣例與 ADV-CLAUDECLI-01 先例）。
- 兩次呼叫之覆蓋設計（對照併入，不增次數）：**呼叫 A（有票）**＝隔離案例 repo 內於 incident 下對「無法判定之工程問題」諮詢，覆蓋 P1-A①④、P1-C⑤（取得→消費→修驗→返回全鏈）；**呼叫 B（無票）**＝同一案例之票前性質問題（plan／diag 類）以 `--no-ticket` 諮詢，覆蓋 P1-A②④。**P1-A⑤ 對照**由同一案例內之「已知可做缺陷」提供：executor 直接修復、transcript 顯示**零諮詢**，與呼叫 A／B 之「無法判定」並列對照，無需額外呼叫。P1-A④ 之失敗回報沿用既有「明標為模擬之不可用案例」（離線 stub，不計預算、不製造真實失敗）。
- 反例檢查：①若呼叫 A 因環境因素（未登入、逾時）失敗，該次仍計入兩次預算；剩餘一次不得用於「補跑 A」以外之目的，且不得因而湊到第三次——兩次用盡即如實以「未驗」結束（成果表「不為驗收製造失敗」）。②受控案例為 executor 預先設計，「顧問提供 executor 未知之資訊」不可證——沿用 Mechanism Bypass 揭露，不得升格為自然事故證據。③`apiKeySource=none` 只證未使用 API key 來源，不證訂閱帳務；effort 實際生效值 CLI 不回報。

## Verify Config
TICKET_ID=ADV-REVERIFY-01
CREATED=2026-10-24
PRESET=docs
ALLOWED=plans/cso_briefing/project/deliver/ADV-REVERIFY-01_real.md
FORBIDDEN=.claude/**,app/**,plans/roadmap/**,plans/architecture/**,plans/cso_briefing/project/TICKET_REGISTRY.json,.gitignore,CLAUDE.md,CLAUDE.local.md,tools/**
MAX_LINES=N/A（證據票：無 repo 實作 diff，行數非判準）
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 16:20
BASE_COMMIT=306b5276
TARGET_BLOB=N/A（無實作標的；AI-0 逐項現值錨點替代）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點與前置復驗（派工後、任何真實呼叫前；全部零模型呼叫）
- **動作**: ①`git rev-parse HEAD` 並確認 `git diff 9a4fc213..HEAD --stat -- .claude/scripts/route.py .claude/scripts/route_table.json` 為空，或逐檔列出差異並確認不改變 `advisor.claude-cli` 之判定／cwd 路徑（否則記錄現版 blob sha 作為「被驗實作」，不得沿用 `9a4fc213` 稱呼）；記 `route.py`／`route_table.json` 之 `git hash-object`；②`claude --version`（唯讀，非模型呼叫）；③`python -I .claude/scripts/route.py table check` 回 `ok:true` 且 `advisor.claude-cli` 為 callable；④確認 `python -I .claude/scripts/tests/test_route.py` 全數 OK（離線，為被驗實作之離線基線）；⑤確認本機 `claude` 為已登入訂閱（以不發模型請求之方式，例如 `claude auth status` 類唯讀指令；若無不發請求之確認法，標「未預驗」並接受呼叫 A 可能因未登入失敗而耗用一次預算）；⑥建立隔離案例 repo（沿用 ADV-CLAUDECLI-01〈證據附錄〉之建置法：隔離 repo 內放 HEAD 版腳本副本與最小 gate 狀態，**原 repo** 之 `git status`、`tmp/verify/*`、`.git/gate` 雜湊、`~/.claude/settings.json` 於呼叫前後逐項比對相同；已知項 `~/.claude.json`、`~/.claude/projects` 之變動照實列）。
- **任一項不符 → 停止回報，不進入真實呼叫。**
- [x] 完成（2026-10-24：①HEAD `c13e69cf`，route.py／route_table.json 對 `9a4fc213` 無差異，blob `e2215d2f`／`6140a413`；②`claude` 2.1.304；③`table check` ok、`advisor.claude-cli` callable；④`test_route.py` 87 OK；⑤`claude auth status` loggedIn／max（不發模型請求）；⑥隔離案例 `revcase/` 建置，原 repo 前後快照比對見證據附錄 E1／E6；零模型呼叫）

### AI-1: 設計受控案例（明標）
- **動作**: 於隔離 repo 建一個小案例，含兩個缺陷與其失敗測試：**缺陷 K（已知可做）**——解法可由本地證據直接判定（例如單點邊界錯誤，失敗輸出已指明）；**缺陷 U（無法判定）**——存在兩個以上本地測試無法區分之合理修法，須外部判斷（例如跨平台語義取捨）。另備一個**票前性質**之設計／診斷問題（供呼叫 B）。案例逐字內容與預先設計之缺陷於票檔〈證據附錄〉明標「受控代表案例、非自然事故、同一 executor 設計與執行」。
- [x] 完成（缺陷 K＝`last_n` off-by-one，缺陷 U＝`cache_key` 跨平台語義取捨；票前問題＝快取鍵是否保留原大小寫；逐字見證據附錄 E2，明標受控代表案例）

### AI-2: 呼叫 A（有票，incident 路徑）——至多一次真實啟動
- **動作**: executor 先**直接修復缺陷 K**（記錄：未呼叫 `route.py`、transcript 顯示零諮詢）；於缺陷 U 上本地取證後，於隔離 repo 開 incident，經 `route.py consult --incident <INC> --source advisor.claude-cli`（有票）交付最小必要上下文；取回答後**消費**（核對前提與附帶說法，採納或具理由不採納）→ 局部驗證測試由失敗轉通過 → `round end`／返回原工作流程。
- **證據**: 入口 envelope（`channel`／`model`／`effort_flag`／`api_key_source`／`session_id`／`disposition`）、ledger 事件增量（恰 +1 筆 `consult --kind advisor`，note 含實際來源 `advisor.claude-cli`）、入口執行記錄原件相關行（逐字、`L<n>`、sha256，`sk-ant-` 遮蔽）、transcript `tool_use` 序列尾 8 碼。
- [x] 完成（呼叫 A：K 直接修復零諮詢→`route.py consult` 有票→RECORDED→消費→`check_u.py` 由失敗轉 `ok-u`→`round end pass`；證據附錄 E3／E4）

### AI-3: 呼叫 B（無票）——至多一次真實啟動
- **動作**: 經 `route.py consult --no-ticket --payload <PATH> --source advisor.claude-cli` 對票前性質問題諮詢；取回答後消費；核對案例 ledger 事件數**不變**（零 ledger 事件）、Gate 語義不變。證據項同 AI-2（無 incident／ledger note 項）。
- [x] 完成（呼叫 B：`--no-ticket`→ANSWERED、案例 ledger 4→4 不變、回答消費；證據附錄 E5）

### AI-4: 預算核對與誠實收束
- **動作**: 以入口執行記錄檔數量與 `claude` 子行程啟動證據核對真實啟動總數 **≤ 2**；任一呼叫失敗仍計入並如實記錄（失敗原因、是否零啟動）；兩次用盡即止，**不補跑、不另叫第三次**。逐約定列出：P1-A①②④⑤、P1-C⑤ 各「已有現版真實證據／仍未驗」及其證明範圍與限制；**不寫成果表**（回填由 `Skill(projman)` align 依本票收束後之 Transition Table 處理）。證據逐字嵌入票檔〈證據附錄〉（reviewer 不能讀 scratch 原件）。
- [x] 完成（真實啟動總數 2，無失敗、無第三次；逐約定現版證據／仍未驗見 E8；成果表未寫；證據逐字嵌入 E1–E9）

## Hard Constraints
1. 真實 `claude -p` 啟動總數 **MUST NOT** 超過 2；**MUST NOT** 為取得更佳結果、補對照或補證而叫第三次；失敗亦計入。
2. **MUST NOT** 修改 `.claude/**`、`app/**` 或成果表；**MUST NOT** 改用 API／計費通道或其他備援；**MUST NOT** 製造失敗以滿足驗收。
3. 受控案例 **MUST** 明標（非自然事故、預先設計、同一 executor 自評、Mechanism Bypass 揭露）；對照結論不得升格為「顧問提供 executor 未知資訊」之證據。
4. 不得以舊實作（`781dcd49`）之呼叫、別名來源或歷史隔離案例冒充現版證據。
5. 隔離 repo 之 ledger 與原 repo 之共用 ledger／`tmp/verify/*` 完全分離；原 repo 呼叫前後狀態比對須相同（已知項照實列）。

## Error Handling
- AI-0 任一項失敗（含 `table check` 非 ok、離線測試未全過、`claude` 不可用）→ 停止回報，**零真實呼叫**。
- 呼叫 A 失敗（未登入、模型不可用、逾時等）→ 記錄為已耗用一次；是否用剩餘一次重做 A 由票內判斷（僅限「環境因素且已修復」之情形，並於票檔揭露），否則呼叫 B 照常；任何情況不超過兩次。
- 兩次預算用盡而約定仍未驗 → 如實標「未驗」，結束；不續叫。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v plans/cso_briefing/project/deliver/ADV-REVERIFY-01_real.md` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** 票檔〈證據附錄〉含：被驗實作之 HEAD 與 `route.py`／`route_table.json` blob sha、離線基線結果、隔離案例逐字內容與明標、兩次呼叫之 envelope／執行記錄原件相關行（逐字、行號、sha256、遮蔽）、ledger 事件增量、transcript `tool_use` 序列尾 8 碼、原 repo 前後比對結果與預算核對表。（證據附錄 E1–E9 逐項具備：HEAD／blob、離線基線、案例逐字、兩次 envelope 與記錄 stream 行〔L5／L13、L5／L16〕、sha256、遮蔽規則、ledger 增量、transcript 序列、前後比對、預算表）
- [x] **[行為性]** given 隔離案例與現版 `route.py`：(a) 呼叫 A（有票）→ expect envelope `disposition` 為 RECORDED、`channel` 標 Claude 訂閱 CLI、`model` 為 `claude-opus-5-5`、`effort_flag`＝`high`、`api_key_source` 照實記錄、ledger 恰 +1 筆 `consult --kind advisor` 且 note 含 `source=advisor.claude-cli`、回答被 executor 消費、缺陷 U 之失敗測試由失敗轉通過、返回原工作流程（對應 P1-A①④、P1-C⑤）；(b) 呼叫 B（無票）→ expect 回答取得、案例 ledger 事件數前後相同、Gate 語義不變（對應 P1-A②④）；(c) 缺陷 K → expect transcript 顯示直接修復且該段**零** `route.py consult` 呼叫，與 U、B 之諮詢並列（對應 P1-A⑤ 對照）。任一子項未成立則該子項標「未驗」或 FAIL，不降級敘述。（(a)(b)(c) 皆成立，證明範圍限受控案例 N=1、同一 executor；P1-A④ 失敗回報半部分仍僅 stub 層，見 E8）
- [x] **[行為性]** 預算：真實 `claude -p` 啟動總數（入口執行記錄檔數＋任何失敗但已啟動者）≤ 2，核對表附於票檔；無第三次啟動證據。（E7：記錄檔 2 個＝啟動 2 次；無失敗啟動、無第三次）
- [x] **[存在性]** 對每一約定（P1-A①②④⑤、P1-C⑤）逐一載明「現版真實證據／仍未驗」、證明範圍與限制；P1-B④ 明載不在本票範圍。（E8 六列：P1-A①②⑤、P1-C⑤ 現版真實／受控證據；P1-A④ 部分〔來源／模型欄位真實、失敗回報仍僅 stub〕；P1-B④ 不在本票）
- [x] **[存在性]** 原 repo 於呼叫前後之 `git status`、`tmp/verify/*`、`.git/gate` 各檔雜湊、`~/.claude/settings.json` 相同，已知項（`~/.claude.json`、`~/.claude/projects`）照實列；`git diff --stat` 僅涉及 ALLOWED 檔案（本票零實作 diff 時於 sv_report 以 `ZERO-DIFF-DECLARED` 並引本票〈來源〉段授權依據宣告）。（E6：原 repo 前後相同；已知項 `~/.claude.json` 與 `~/.claude/projects` 照實列；本票零實作 diff，低風險 docs 路徑不經 sv_report／`ZERO-DIFF-DECLARED`，`git diff --stat` 僅本票檔）
- [x] ~~assembleDebug PASS~~ (N/A: docs preset／無 app/ 變更)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（僅 `deliver/ADV-REVERIFY-01_real.md`）

## 證據附錄（ADV-REVERIFY-01，2026-10-24；逐字原件，reviewer 不能讀 scratch，故嵌入）

> 受控代表案例、非自然事故、由同一 executor 設計與執行；Mechanism Bypass 揭露：缺陷 K／U 為 executor 預先設計，「顧問提供 executor 未知之資訊」不可由本證據證明；`apiKeySource=none` 只證未使用 API key 來源，不證訂閱帳務；effort 實際生效值 CLI 不回報。原件位於 session scratchpad `revcase/`（非耐久）、`rev_*` 檔；sha256 見各節。

### E1 被驗實作與離線基線（AI-0，零模型呼叫）

- HEAD＝`c13e69cf67684584a19325f44cd1845f8a2212ef`；`git diff 9a4fc213..HEAD --stat -- .claude/scripts/route.py .claude/scripts/route_table.json` 輸出為空（exit 0）→ 現版 `advisor.claude-cli` 與 `9a4fc213` 在該二檔無差異。
- `git hash-object`：`.claude/scripts/route.py`＝`e2215d2f9017bc3323ff283b81c0417ff92e6fbb`；`.claude/scripts/route_table.json`＝`6140a413bd1083e237da073b594c6e2d614459a5`。
- `claude --version`＝`2.1.304 (Claude Code)`；`claude auth status`（唯讀，不發模型請求；email／orgId 於此省略）：`loggedIn: true`、`authMethod: claude.ai`、`apiProvider: firstParty`、`subscriptionType: max`。
- `python -I .claude/scripts/route.py table check` 逐字：
```json
{"ok": true, "verb": "table check", "via": "route.py", "disposition": "OK", "detail": "table valid", "sources": ["advisor.api", "advisor.claude-cli", "advisor.native", "advisor.rigor-repair", "research.ppss", "research.rigor-fallback"], "callable": ["advisor.api", "advisor.claude-cli", "advisor.native", "research.ppss"], "enabled": ["advisor.api", "advisor.claude-cli", "advisor.native", "research.ppss"]}
```

- `python -I .claude/scripts/tests/test_route.py`：`Ran 87 tests in 208.642s`／`OK`（離線基線；失敗類別案例僅為離線 stub，不計預算）。

### E2 隔離案例（AI-1；明標：受控代表案例）

案例 repo＝`revcase/`（git init；`.gitignore`＝`plans/ tmp/ .tmp/ __pycache__/`；初始提交 `3c9ba6e`）；案例票 `CASE-REV-01`（`plans/cso_briefing/project/Pending/CASE-REV-01_case.md`）；incident `INC-001`（subject `case:cache-key-semantics`）由 `gate_ledger.py --ticket CASE-REV-01 incident open` 開立（呼叫前）。`CLI` cwd 為入口自管之 `cueshot_advisor_cli_cwd`（repo 外）。另：第一次建置腳本之反斜線被工具層吃掉致案例檔語法錯誤，於**零模型呼叫**階段發現並棄用該目錄（`rev1case/`，其 ledger 含一個未用之 `INC-001`、無任何 advisor 記錄檔），改以無反斜線字面（`chr(92)`）重建為 `revcase/`；兩次建置皆零模型呼叫。

初始案例檔（逐字）：
```python
# lastn.py (defect K — known-doable)
def last_n(items, n):
    return items[-n - 1:]

# check_k.py
from lastn import last_n

assert last_n([1, 2, 3, 4], 2) == [3, 4], last_n([1, 2, 3, 4], 2)
print('ok-k')

# ckey.py (defect U — undecidable)
BS = chr(92)


def cache_key(path):
    return path.replace(BS, '/')

# check_u.py
from ckey import cache_key, BS

win = 'A' + BS + 'b.TXT'
posix = 'a/B.txt'
assert cache_key(win) == cache_key(posix), (cache_key(win), cache_key(posix))
print('ok-u')
```

初始失敗輸出：K：`AssertionError: [2, 3, 4]`（`check_k.py` 預期 `[3, 4]`，實得 `[2, 3, 4]`）；U：`AssertionError: ('A/b.TXT', 'a/B.txt')`（sha256 `76334878f0a60f5640fec1d15125c237199a940556381636c1373f633f361c32`）。
**缺陷 U 之性質**：候選修法 (1) `path.replace(BS, '/').lower()` 與 (2) `os.path.normcase(path)` 在本機（Windows）皆使 `check_u.py` 通過，現有測試無法區分；(2) 於 POSIX 為 no-op，須由外部判斷快取鍵應以「檔案系統語意」或「跨平台一致」為準。

### E3 缺陷 K 之直接修復（P1-A⑤ 對照：已知可做 → 零諮詢）

transcript（session `d7f48063-6006-4d6e-a8e3-2f7fa366bc43`）`tool_use` 序列（UTC，id 尾 8 碼）：`x3yPojtg` 07:47:07 建置案例＋incident＋兩案初始失敗輸出 → `8TJnLwMC` 07:47:14 `Read lastn.py` → `wpkhXSxh` 07:47:17 `Edit lastn.py`（K 修復）→ `ZwB4zpBf` 07:47:24 `check_k.py`→`ok-k`＋呼叫 A。K 之偵測、讀檔、修復**三步之間零 `route.py consult`**；首個 `route.py consult` 為 `ZwB4zpBf`（呼叫 A，K 已修畢之後）。
```diff
--- a/lastn.py
+++ b/lastn.py
@@ -1,2 +1,2 @@
 def last_n(items, n):
-    return items[-n - 1:]
+    return items[-n:]
```

### E4 呼叫 A（有票，incident 路徑）— 真實啟動 #1

命令（cwd＝`revcase/`）：`python -I D:/DevSources/AndroidProj/SmartCameraApp1/.claude/scripts/route.py --ticket CASE-REV-01 consult --source advisor.claude-cli --incident INC-001 --payload rev_payload.md --evidence rev_evidence.txt`（exit 0）。transcript：`ZwB4zpBf`（07:47:24 發出，執行記錄時間戳 `20261024T074741Z`）。

payload（`rev_payload.md`，sha256 `e6787d5880352d94e8dd06b2ce09274f2f15ae480a2d20711e6911e5722b57e1`，逐字）：
```markdown
## 目標
`ckey.cache_key()` 要讓同一檔案的不同寫法得到同一個快取鍵（`check_u.py` 要求 Windows 寫法 `A<反斜線>b.TXT` 與 POSIX 寫法 `a/B.txt` 得到相同的鍵）；請判斷採哪個修法。incident=INC-001

## 失敗證據
failure_sha256=76334878f0a60f5640fec1d15125c237199a940556381636c1373f633f361c32

```
Traceback (most recent call last):
  File "C:\Users\ADMINI~1\AppData\Local\Temp\claude\D--DevSources-AndroidProj-SmartCameraApp1\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\scratchpad\revcase\check_u.py", line 5, in <module>
    assert cache_key(win) == cache_key(posix), (cache_key(win), cache_key(posix))
           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
AssertionError: ('A/b.TXT', 'a/B.txt')
```

## 相關內容
- 檔案：`ckey.py`（目前只把反斜線換成斜線；`BS = chr(92)`）、`check_u.py`。
- 我已本地取證：候選修法 (1) `path.replace(BS, '/').lower()`；(2) `os.path.normcase(path)`。兩者在本機（Windows）皆使 `check_u.py` 通過，現有測試無法區分；(2) 在 POSIX 上不處理反斜線與大小寫，(1) 與平台無關。
- 此快取鍵會讓 Windows 與 Linux 共用同一個共享快取目錄，不確定該以『檔案系統語意』還是『跨平台一致』為準。
- 限制：不新增依賴、不改 check_u.py、只改 ckey.py。

## 已試策略與結果
兩個候選修法皆在本機通過 check_u.py；尚未決定採用哪個。

## 既有授權
僅可修改 ckey.py；不可改測試。

## 要求輸出
最多 5 行：建議採哪個修法與理由，以及一個我套用前應先驗證的前提。不要寫完整程式。
```

envelope（`rev_realA_out.json`，sha256 `6284c4702e077efabc8c9618b81a539181a826085901fd0c453951141ceed770`，逐字）：
```json
{"ok": true, "verb": "consult", "via": "route.py", "mode": "ticketed", "channel": "claude-subscription-cli", "requested_model": "claude-opus-5-5", "models_used": {"claude-opus-5-5": {"inputTokens": 2, "outputTokens": 701, "cacheReadInputTokens": 1559, "cacheCreationInputTokens": 4593}}, "effort_flag": "high", "api_key_source": "none", "session_id": "3f4c9e5c-33e4-4e01-ab50-0a406b429a3b", "usage": {"input_tokens": 2, "cache_creation_input_tokens": 4593, "cache_read_input_tokens": 1559, "output_tokens": 701, "output_tokens_details": {"thinking_tokens": 235}, "server_tool_use": {"web_search_requests": 0, "web_fetch_requests": 0}, "service_tier": "standard", "cache_creation": {"ephemeral_1h_input_tokens": 4593, "ephemeral_5m_input_tokens": 0}, "inference_geo": "global", "iterations": [{"input_tokens": 2, "output_tokens": 701, "cache_read_input_tokens": 1559, "cache_creation_input_tokens": 4593, "cache_creation": {"ephemeral_5m_input_tokens": 0, "ephemeral_1h_input_tokens": 4593}, "type": "message"}], "speed": "standard", "fallback_credit": null}, "cost_reported_usd": 0.0510838, "cost_note": "CLI list-price figure, not billing", "duration_ms": 16126, "cli_internal_retries": 0, "hooks": [{"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}, {"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}], "warnings": [], "record": "tmp/CASE-REV-01/advisor_cli/advisor_cli_20261024T074741Z_c93a3003.json", "model": "claude-opus-5-5", "answer": "1. **建議採 (1) `path.replace(BS, '/').lower()`。** 這個鍵會被 Windows 和 Linux 共用，所以應以「跨平台一致」為準，不該以單一主機的檔案系統語意為準。\n2. (2) `os.path.normcase` 在 POSIX 上什麼都不做：Linux 端產生的鍵會保留大小寫與反斜線，Windows 端產生的鍵則不會。結果是同一檔案在兩個平台得到不同的鍵，共享快取失效；這個問題只在 Linux 上出現，本機測試看不到。\n3. (1) 的取捨：在 Linux 上，只差大小寫的兩個不同檔案（如 `a/B.txt` 與 `a/b.txt`）會撞成同一個鍵，可能讀到錯誤的快取。\n4. **套用前請先驗證這個前提：** 被快取的路徑空間（專案原始碼、資源檔等）在 Linux 上不會有只差大小寫的不同檔案。可以在 Linux 端掃描是否有小寫化後重複的路徑。若確認存在這種檔案，(1) 就不安全，要另議。\n5. 附帶：鍵的格式變了，共享目錄裡的舊快取會全部失效，部署時要預期一次冷啟動，或主動清掉舊快取。", "disposition": "RECORDED", "ledger_seq": 2, "detail": "evidence=failure", "source": "advisor.claude-cli"}
```

案例 ledger（`revcase/.git/gate/CASE-REV-01.jsonl` 於呼叫 A 完成時；seq 1 為呼叫前 `incident open`，seq 2 為**入口於呼叫內記入之 `consult --kind advisor`**〔恰 +1〕）：
```json
{"seq": 1, "ts": "2026-10-24T07:47:08+00:00", "ticket": "CASE-REV-01", "type": "finding", "incident": "INC-001", "new": true, "subjects": ["case:cache-key-semantics"], "gate_point": null, "verdict": null, "same_root_cause": null, "independent_evidence": null, "attach_reason": null}
{"seq": 2, "ts": "2026-10-24T07:47:42+00:00", "ticket": "CASE-REV-01", "type": "consult", "incident": "INC-001", "kind": "advisor", "note": "source=advisor.claude-cli via=route.py channel=claude-subscription-cli model=claude-opus-5-5 effort_flag=high api_key_source=none session_id=3f4c9e5c-33e4-4e01-ab50-0a406b429a3b payload_sha256=e6787d5880352d94e8dd06b2ce09274f2f15ae480a2d20711e6911e5722b57e1 incident=INC-001 evidence=failure:76334878f0a60f5640fec1d15125c237199a940556381636c1373f633f361c32 record=tmp/CASE-REV-01/advisor_cli/advisor_cli_20261024T074741Z_c93a3003.json"}
```

入口執行記錄原件（`revcase/tmp/CASE-REV-01/advisor_cli/advisor_cli_20261024T074741Z_c93a3003.json`，20070 bytes，sha256 `2ca97de23c7570e8cd75affe5dcb0ec9500663c0685f3074176134c357090d61`；欄位 `answer/envelope/note/payload_sha256/returncode/source/stderr/stream/verdict`，`returncode=0`、`stderr=""`、`verdict` 見原件；`sk-ant-` 命中數 0，遮蔽規則：`sk-ant-[A-Za-z0-9_-]+`→`sk-ant-***`）之 stream 相關行（`stream` 共 14 行，L5＝`system/init`、L13＝`result`；逐字，`L<n>` 為 stream 內行號）：
```
L5: {"type":"system","subtype":"init","cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","session_id":"3f4c9e5c-33e4-4e01-ab50-0a406b429a3b","tools":[],"mcp_servers":[],"model":"claude-opus-5-5","permissionMode":"auto","slash_commands":[],"apiKeySource":"none","claude_code_version":"2.1.304","output_style":"default","agents":["claude","claude-code-guide","Explore","general-purpose","Plan","statusline-setup","understand-anything:architecture-analyzer","understand-anything:article-analyzer","understand-anything:assemble-reviewer","understand-anything:domain-analyzer","understand-anything:file-analyzer","understand-anything:graph-reviewer","understand-anything:knowledge-graph-guide","understand-anything:project-scanner","understand-anything:tour-builder"],"skills":[],"plugins":[{"name":"context-bar","path":"D:\\DevSources\\claude-mods\\context-bar","source":"context-bar@inline","version":"0.1.0"},{"name":"understand-anything","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\understand-anything\\understand-anything\\2.8.1","source":"understand-anything@understand-anything","version":"2.8.1"},{"name":"frontend-design","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\claude-plugins-official\\frontend-design\\58267944f921","source":"frontend-design@claude-plugins-official"},{"name":"cowork-plugin-management","path":"C:\\Users\\Administrator\\.claude\\plugins\\synced\\dcd7b68f-604c-422c-bf55-e2fb149f77bb_42153965-a23d-4904-83d9-b6fa38cdaff3\\cowork-plugin-management~g2","source":"cowork-plugin-management@synced","version":"0.2.2"},{"name":"cc-plugin-agents-md","path":"builtin","source":"cc-plugin-agents-md@builtin"},{"name":"cc-plugin-telemetry","path":"builtin","source":"cc-plugin-telemetry@builtin"},{"name":"cc-plugin-plugin-authoring","path":"builtin","source":"cc-plugin-plugin-authoring@builtin"},{"name":"cc-plugin-you-should-know","path":"builtin","source":"cc-plugin-you-should-know@builtin"}],"capabilities":["interrupt_receipt_v1","interrupt_cancel_queued_v1","interrupt_send_now_v1","msg_lifecycle_v1","request_marker_lists_v1","sdk_mcp_tools_list_changed","sdk_mcp_manifests","mcp_read_resource_v1","mcp_tool_ui_meta_v1","ui_surface_v1","prompts_screened_v1","mcp_call_result_not_for_model_v1"],"analytics_disabled":false,"product_feedback_disabled":false,"uuid":"22b72da4-0e1e-4ce8-98c8-6e802bf630d1","memory_paths":{"auto":"C:\\Users\\Administrator\\.claude\\projects\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\memory\\"},"scratchpad_path":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\claude\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\3f4c9e5c-33e4-4e01-ab50-0a406b429a3b\\scratchpad","original_cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","additional_directories":["C:\\Users\\Administrator\\.claude\\channels\\telegram","D:\\DevSources\\AndroidProj\\SmartCameraApp1\\.claude\\skills\\ticket-rigor-gate\\runner"],"messaging_socket_path":"\\\\.\\pipe\\LOCAL\\cc-msg-1e3c25df8c0f6b08f7400b6fee642995","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","per_turn_effort_active":true,"view_mode":"default","powershell_path":"C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WindowsApps\\pwsh.exe","startup_timing":{"entrypoint":"claude-desktop","warm_spare_claimed":false,"plugin_full_loads":0,"user_context_builds":2,"sdk_mcp_dial_runs":0,"sdk_mcp_servers_dialed":0,"flag_fetch_kick":"not_eligible","flag_fetch_kick_judged_at_ms":206,"hooks_init_source":"startup","phases":{"node_boot_ms":140,"settings_load_ms":0,"pre_action_mdm_keychain_ms":2,"pre_action_init_ms":61,"pre_action_client_data_url_ms":0,"pre_action_migrations_ms":0,"pre_action_managed_settings_ms":3,"pre_action_ms":115,"permission_context_ms":251,"setup_import_ms":1,"setup_uds_dir_ms":0,"setup_uds_listen_ms":1,"setup_uds_publish_ms":36,"setup_uds_messaging_ms":54,"setup_hooks_snapshot_ms":0,"setup_file_watcher_ms":0,"setup_release_notes_ms":11,"setup_ms":59,"skills_load_ms":250,"tools_commands_load_ms":147,"model_catalog_decision_ms":63,"mcp_connect_ms":5,"print_import_ms":50,"headless_prologue_ms":488,"hooks_init_ms":900,"initial_messages_load_ms":323,"headless_post_load_ms":9,"input_ready_ms":1901,"first_message_read_ms":1918,"sdk_mcp_update_ms":3,"first_message_handle_ms":3,"first_message_handle_cpu_ms":0,"first_message_handle_loop_lag_ms":0,"mcp_prewait_ms":0,"qe_system_prompt_ms":29,"prompt_submit_hooks_ms":332,"qe_plugin_skills_load_ms":0,"init_emit_ms":2},"time_origin_ms":1792828045774.726,"phase_start_ms":{"node_boot_ms":0,"settings_load_ms":140,"pre_action_mdm_keychain_ms":143,"pre_action_init_ms":145,"pre_action_client_data_url_ms":205,"pre_action_migrations_ms":207,"pre_action_managed_settings_ms":207,"pre_action_ms":143,"permission_context_ms":270,"setup_import_ms":607,"setup_uds_dir_ms":626,"setup_uds_listen_ms":626,"setup_uds_publish_ms":627,"setup_uds_messaging_ms":613,"setup_hooks_snapshot_ms":670,"setup_file_watcher_ms":670,"setup_release_notes_ms":673,"setup_ms":624,"skills_load_ms":617,"tools_commands_load_ms":740,"model_catalog_decision_ms":889,"mcp_connect_ms":995,"print_import_ms":1002,"headless_prologue_ms":1057,"hooks_init_ms":969,"initial_messages_load_ms":1546,"headless_post_load_ms":1869,"input_ready_ms":0,"first_message_read_ms":0,"sdk_mcp_update_ms":1918,"first_message_handle_ms":1918,"mcp_prewait_ms":2003,"qe_system_prompt_ms":2015,"prompt_submit_hooks_ms":2054,"qe_plugin_skills_load_ms":2388,"init_emit_ms":2394}}}
L13: {"duration_api_ms":9891,"stop_reason":"end_turn","session_id":"3f4c9e5c-33e4-4e01-ab50-0a406b429a3b","total_cost_usd":0.0510838,"usage":{"input_tokens":2,"cache_creation_input_tokens":4593,"cache_read_input_tokens":1559,"output_tokens":701,"output_tokens_details":{"thinking_tokens":235},"server_tool_use":{"web_search_requests":0,"web_fetch_requests":0},"service_tier":"standard","cache_creation":{"ephemeral_1h_input_tokens":4593,"ephemeral_5m_input_tokens":0},"inference_geo":"global","iterations":[{"input_tokens":2,"output_tokens":701,"cache_read_input_tokens":1559,"cache_creation_input_tokens":4593,"cache_creation":{"ephemeral_5m_input_tokens":0,"ephemeral_1h_input_tokens":4593},"type":"message"}],"speed":"standard","fallback_credit":null},"modelUsage":{"claude-opus-5-5":{"inputTokens":2,"outputTokens":701,"cacheReadInputTokens":1559,"cacheCreationInputTokens":4593,"webSearchRequests":0,"costUSD":0.0510838,"contextWindow":1000000,"maxOutputTokens":128000,"thinkingTokens":235,"canonicalModel":"claude-opus-5-5","provider":"firstParty","costBasis":"list"}},"permission_denials":[],"terminal_reason":"completed","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","subagent_stats":{"spawned":0,"requested":{"background":0,"foreground":0,"unset":0},"started_in_background":0,"max_depth":0,"spawned_by_subagents":0,"completed":0,"failed":0,"killed":{"parent":0,"user":0,"system":0},"refused":{"depth_limit":0,"concurrency_limit":0,"budget":0},"by_type":{}},"safety_stops":0,"is_error":false,"num_turns":1,"subtype":"success","api_error_status":null,"result":"1. **建議採 (1) `path.replace(BS, '/').lower()`。** 這個鍵會被 Windows 和 Linux 共用，所以應以「跨平台一致」為準，不該以單一主機的檔案系統語意為準。\n2. (2) `os.path.normcase` 在 POSIX 上什麼都不做：Linux 端產生的鍵會保留大小寫與反斜線，Windows 端產生的鍵則不會。結果是同一檔案在兩個平台得到不同的鍵，共享快取失效；這個問題只在 Linux 上出現，本機測試看不到。\n3. (1) 的取捨：在 Linux 上，只差大小寫的兩個不同檔案（如 `a/B.txt` 與 `a/b.txt`）會撞成同一個鍵，可能讀到錯誤的快取。\n4. **套用前請先驗證這個前提：** 被快取的路徑空間（專案原始碼、資源檔等）在 Linux 上不會有只差大小寫的不同檔案。可以在 Linux 端掃描是否有小寫化後重複的路徑。若確認存在這種檔案，(1) 就不安全，要另議。\n5. 附帶：鍵的格式變了，共享目錄裡的舊快取會全部失效，部署時要預期一次冷啟動，或主動清掉舊快取。","ttft_ms":5650,"type":"result","duration_ms":13491,"uuid":"4ba491b7-17fc-4903-81d0-e3d5563fda3f","ttft_stream_ms":3104,"time_to_request_ms":630,"time_to_request_phases_ms":{"other":36,"system_prompt":30,"resume_other":0,"process_user_input":4,"input_attachments":6,"input_hooks":332,"tool_pool_refresh":1,"init_message":7,"engine_pickup":3,"message_prep":2,"model_config_wait":1,"autocompact":2,"query_setup":50,"auto_mode_context_wait":141,"model_gates":1,"tool_schema_build":1,"message_normalization":2,"client_creation":4,"request_build":7},"turn_start_resume_kind":"none","process_turn_index":0,"time_to_request_loop_lag_ms":52,"time_to_request_cpu_ms":407,"time_to_request_major_faults":5139,"input_attachments_detail":{"slowest_producer":"queued_commands","slowest_producer_ms":4,"cpu_ms":0,"major_faults":213},"first_content_frame_ms":3104,"queued_turn_count":0,"result_index":0}
```

**消費（executor）**：回答建議 (1)，並指出 (2) 在 POSIX 為 no-op、(1) 之代價為 Linux 上只差大小寫的不同檔案會撞鍵，要求套用前驗證「被快取路徑空間在 Linux 上不會有只差大小寫的不同檔案」，並附帶「舊快取全部失效」。本地驗證（`pisD2WmZ`）：`posixpath.normcase('A/b.TXT')`＝`'A/b.TXT'`（no-op 屬實）、`ntpath.normcase`＝`'a\b.txt'`（Windows 上 (2) 亦通過，故測試不可區分）、`'a/B.txt'.lower()==...` 撞鍵成立；案例 repo 內 `git ls-files` 小寫後重複數＝0（前提在案例範圍內成立，**案例僅 5 個檔，不代表真實路徑空間**）。採納 (1)，不採納 (2)；附帶說法「舊快取失效」未在案例中驗證（案例無既有快取）。套用 `Edit ckey.py`（`we9uTnPX`，07:48:06）→ `check_u.py`→`ok-u`、`check_k.py`→`ok-k`（`DhQu7Di1`，07:48:12）。
```diff
--- a/ckey.py
+++ b/ckey.py
@@ -2,4 +2,4 @@ BS = chr(92)
 
 
 def cache_key(path):
-    return path.replace(BS, '/')
+    return path.replace(BS, '/').lower()
```

原工作流程返回：`gate_ledger.py --ticket CASE-REV-01 round start --incident INC-001 --expect 0 --layer advisor --strategy ... --authority ...` → `round 1 seq 3`；`round end --result pass` → `seq 4`；`status`：`INC-001 closed rounds=1 advisor=1/3(failed 0) pending_human=[]`（`zxbkipyV`，07:48:20）。案例 ledger 終態 4 行：
```json
{"seq": 1, "ts": "2026-10-24T07:47:08+00:00", "ticket": "CASE-REV-01", "type": "finding", "incident": "INC-001", "new": true, "subjects": ["case:cache-key-semantics"], "gate_point": null, "verdict": null, "same_root_cause": null, "independent_evidence": null, "attach_reason": null}
{"seq": 2, "ts": "2026-10-24T07:47:42+00:00", "ticket": "CASE-REV-01", "type": "consult", "incident": "INC-001", "kind": "advisor", "note": "source=advisor.claude-cli via=route.py channel=claude-subscription-cli model=claude-opus-5-5 effort_flag=high api_key_source=none session_id=3f4c9e5c-33e4-4e01-ab50-0a406b429a3b payload_sha256=e6787d5880352d94e8dd06b2ce09274f2f15ae480a2d20711e6911e5722b57e1 incident=INC-001 evidence=failure:76334878f0a60f5640fec1d15125c237199a940556381636c1373f633f361c32 record=tmp/CASE-REV-01/advisor_cli/advisor_cli_20261024T074741Z_c93a3003.json"}
{"seq": 3, "ts": "2026-10-24T07:48:21+00:00", "ticket": "CASE-REV-01", "type": "round_start", "incident": "INC-001", "round": 1, "layer": "advisor", "strategy": "adopt advisor option (1): replace(BS,'/').lower() in ckey.py", "strategy_norm": "adopt advisor option (1): replace(bs,'/').lower() in ckey.py", "authority": "case ticket CASE-REV-01; only ckey.py editable"}
{"seq": 4, "ts": "2026-10-24T07:48:21+00:00", "ticket": "CASE-REV-01", "type": "round_end", "incident": "INC-001", "round": 1, "result": "pass"}
```

### E5 呼叫 B（無票）— 真實啟動 #2

命令（cwd＝`revcase/`）：`python -I D:/DevSources/AndroidProj/SmartCameraApp1/.claude/scripts/route.py consult --no-ticket --source advisor.claude-cli --payload rev_payload_tl.md`（exit 0）。transcript：`zYVwxWzB`（07:48:27 發出，記錄時間戳 `20261024T074846Z`）。

payload（`rev_payload_tl.md`，sha256 `6ab24dcc2e9210112f0cb3a9c6cc43645d76b2893a214568804dd954e0f36169`，逐字）：
```markdown
## 目標
（票前性質的設計問題）一個小型工具要把檔案路徑轉成快取鍵並寫入磁碟快取檔名；請給取捨。

## 相關內容
- 快取會在 Windows 與 Linux 共用同一個共享目錄。
- 問題：快取鍵是否需要保留原始大小寫資訊（例如另存一份原路徑於 metadata），還是只要正規化後的鍵即可？
- 限制：只用標準庫。

## 要求輸出
最多 4 行：取捨結論與一個套用前應驗證的前提。不要寫完整程式。
```

envelope（`rev_realB_out.json`，sha256 `e9c304e89867e7f3daadfb6c5b84fccc5b8ac5ea8953dc8f1e4c7d9cebb75b87`，逐字）：
```json
{"ok": true, "verb": "consult", "via": "route.py", "mode": "ticketless", "channel": "claude-subscription-cli", "requested_model": "claude-opus-5-5", "models_used": {"claude-opus-5-5": {"inputTokens": 2, "outputTokens": 970, "cacheReadInputTokens": 3189, "cacheCreationInputTokens": 2379}}, "effort_flag": "high", "api_key_source": "none", "session_id": "671fc5a1-bc20-45c8-bd22-6f77f0361a08", "usage": {"input_tokens": 2, "cache_creation_input_tokens": 2379, "cache_read_input_tokens": 3189, "output_tokens": 970, "output_tokens_details": {"thinking_tokens": 544}, "server_tool_use": {"web_search_requests": 0, "web_fetch_requests": 0}, "service_tier": "standard", "cache_creation": {"ephemeral_1h_input_tokens": 2379, "ephemeral_5m_input_tokens": 0}, "inference_geo": "global", "iterations": [{"input_tokens": 2, "output_tokens": 970, "cache_read_input_tokens": 3189, "cache_creation_input_tokens": 2379, "cache_creation": {"ephemeral_5m_input_tokens": 0, "ephemeral_1h_input_tokens": 2379}, "type": "message"}], "speed": "standard", "fallback_credit": null}, "cost_reported_usd": 0.0390778, "cost_note": "CLI list-price figure, not billing", "duration_ms": 17273, "cli_internal_retries": 0, "hooks": [{"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}, {"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}], "warnings": [], "record": "tmp/_advisor_cli_ticketless/advisor_cli_20261024T074846Z_853a8676.json", "model": "claude-opus-5-5", "answer": "**結論**：檔名用正規化路徑的 SHA-256 hex（小寫 hex 字元），另在 metadata 存原始路徑。原始大小寫要保留，但存成 metadata，不放進檔名。\n**理由**：共享目錄掛在 Windows/SMB 時通常不分大小寫，所以檔名不能靠大小寫區分；用 hex 雜湊可以避開大小寫、非法字元和長度上限的問題。存原路徑幾乎沒有成本，還能偵測碰撞、方便除錯。\n**取捨**：只有 Windows 來源的路徑才做 casefold。Linux 路徑若一律轉小寫，`/a/Foo` 和 `/a/foo` 會共用同一個鍵，讀到錯的快取。這種錯不會報錯，比多存一份快取更難發現。\n**套用前要驗證**：Linux 端的實際資料裡是否真的有只差大小寫的路徑，以及同一個檔案是否需要從兩個 OS 命中同一筆快取。如果需要跨 OS 命中，就得先把 `C:\\…` 和 `/mnt/…` 對應到同一個根路徑再做雜湊，單靠大小寫正規化不夠。", "disposition": "ANSWERED", "detail": "ticketless: no ledger event was written", "source": "advisor.claude-cli"}
```

入口執行記錄原件（`revcase/tmp/_advisor_cli_ticketless/advisor_cli_20261024T074846Z_853a8676.json`，21676 bytes，sha256 `d548c1be03ae9f6a8c8ceea6071124034797dbda33dd622b88d706998cd36902`；`sk-ant-` 命中數 0）之 stream 相關行（`stream` 共 16 行，L5＝`system/init`、L16＝`result`）：
```
L5: {"type":"system","subtype":"init","cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","session_id":"671fc5a1-bc20-45c8-bd22-6f77f0361a08","tools":[],"mcp_servers":[],"model":"claude-opus-5-5","permissionMode":"auto","slash_commands":[],"apiKeySource":"none","claude_code_version":"2.1.304","output_style":"default","agents":["claude","claude-code-guide","Explore","general-purpose","Plan","statusline-setup","understand-anything:architecture-analyzer","understand-anything:article-analyzer","understand-anything:assemble-reviewer","understand-anything:domain-analyzer","understand-anything:file-analyzer","understand-anything:graph-reviewer","understand-anything:knowledge-graph-guide","understand-anything:project-scanner","understand-anything:tour-builder"],"skills":[],"plugins":[{"name":"context-bar","path":"D:\\DevSources\\claude-mods\\context-bar","source":"context-bar@inline","version":"0.1.0"},{"name":"understand-anything","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\understand-anything\\understand-anything\\2.8.1","source":"understand-anything@understand-anything","version":"2.8.1"},{"name":"frontend-design","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\claude-plugins-official\\frontend-design\\58267944f921","source":"frontend-design@claude-plugins-official"},{"name":"cowork-plugin-management","path":"C:\\Users\\Administrator\\.claude\\plugins\\synced\\dcd7b68f-604c-422c-bf55-e2fb149f77bb_42153965-a23d-4904-83d9-b6fa38cdaff3\\cowork-plugin-management~g2","source":"cowork-plugin-management@synced","version":"0.2.2"},{"name":"cc-plugin-agents-md","path":"builtin","source":"cc-plugin-agents-md@builtin"},{"name":"cc-plugin-telemetry","path":"builtin","source":"cc-plugin-telemetry@builtin"},{"name":"cc-plugin-plugin-authoring","path":"builtin","source":"cc-plugin-plugin-authoring@builtin"},{"name":"cc-plugin-you-should-know","path":"builtin","source":"cc-plugin-you-should-know@builtin"}],"capabilities":["interrupt_receipt_v1","interrupt_cancel_queued_v1","interrupt_send_now_v1","msg_lifecycle_v1","request_marker_lists_v1","sdk_mcp_tools_list_changed","sdk_mcp_manifests","mcp_read_resource_v1","mcp_tool_ui_meta_v1","ui_surface_v1","prompts_screened_v1","mcp_call_result_not_for_model_v1"],"analytics_disabled":false,"product_feedback_disabled":false,"uuid":"4d7d8927-6923-4a59-91e5-57b72402b9ac","memory_paths":{"auto":"C:\\Users\\Administrator\\.claude\\projects\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\memory\\"},"scratchpad_path":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\claude\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\671fc5a1-bc20-45c8-bd22-6f77f0361a08\\scratchpad","original_cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","additional_directories":["C:\\Users\\Administrator\\.claude\\channels\\telegram","D:\\DevSources\\AndroidProj\\SmartCameraApp1\\.claude\\skills\\ticket-rigor-gate\\runner"],"messaging_socket_path":"\\\\.\\pipe\\LOCAL\\cc-msg-2e288b1ba71036e6be24585017b79731","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","per_turn_effort_active":true,"view_mode":"default","powershell_path":"C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WindowsApps\\pwsh.exe","startup_timing":{"entrypoint":"claude-desktop","warm_spare_claimed":false,"plugin_full_loads":0,"user_context_builds":2,"sdk_mcp_dial_runs":0,"sdk_mcp_servers_dialed":0,"flag_fetch_kick":"not_eligible","flag_fetch_kick_judged_at_ms":219,"hooks_init_source":"startup","phases":{"node_boot_ms":154,"settings_load_ms":0,"pre_action_mdm_keychain_ms":2,"pre_action_init_ms":59,"pre_action_client_data_url_ms":0,"pre_action_migrations_ms":0,"pre_action_managed_settings_ms":3,"pre_action_ms":73,"permission_context_ms":270,"setup_import_ms":0,"setup_uds_dir_ms":0,"setup_uds_listen_ms":0,"setup_uds_publish_ms":12,"setup_uds_messaging_ms":18,"setup_hooks_snapshot_ms":0,"setup_file_watcher_ms":0,"setup_release_notes_ms":4,"setup_ms":22,"skills_load_ms":132,"tools_commands_load_ms":72,"model_catalog_decision_ms":21,"mcp_connect_ms":1,"print_import_ms":27,"headless_prologue_ms":457,"hooks_init_ms":768,"initial_messages_load_ms":263,"headless_post_load_ms":4,"input_ready_ms":1498,"first_message_read_ms":1507,"sdk_mcp_update_ms":1,"first_message_handle_ms":1,"first_message_handle_cpu_ms":0,"first_message_handle_loop_lag_ms":0,"mcp_prewait_ms":1,"qe_system_prompt_ms":88,"prompt_submit_hooks_ms":488,"qe_plugin_skills_load_ms":0,"init_emit_ms":2},"time_origin_ms":1792828108844.8108,"phase_start_ms":{"node_boot_ms":0,"settings_load_ms":155,"pre_action_mdm_keychain_ms":157,"pre_action_init_ms":159,"pre_action_client_data_url_ms":218,"pre_action_migrations_ms":220,"pre_action_managed_settings_ms":220,"pre_action_ms":157,"permission_context_ms":233,"setup_import_ms":531,"setup_uds_dir_ms":537,"setup_uds_listen_ms":537,"setup_uds_publish_ms":538,"setup_uds_messaging_ms":533,"setup_hooks_snapshot_ms":553,"setup_file_watcher_ms":553,"setup_release_notes_ms":554,"setup_ms":537,"skills_load_ms":536,"tools_commands_load_ms":609,"model_catalog_decision_ms":681,"mcp_connect_ms":718,"print_import_ms":731,"headless_prologue_ms":760,"hooks_init_ms":713,"initial_messages_load_ms":1218,"headless_post_load_ms":1481,"input_ready_ms":0,"first_message_read_ms":0,"sdk_mcp_update_ms":1507,"first_message_handle_ms":1507,"mcp_prewait_ms":1578,"qe_system_prompt_ms":1587,"prompt_submit_hooks_ms":1690,"qe_plugin_skills_load_ms":2182,"init_emit_ms":2189}}}
L16: {"duration_api_ms":12551,"stop_reason":"end_turn","session_id":"671fc5a1-bc20-45c8-bd22-6f77f0361a08","total_cost_usd":0.0390778,"usage":{"input_tokens":2,"cache_creation_input_tokens":2379,"cache_read_input_tokens":3189,"output_tokens":970,"output_tokens_details":{"thinking_tokens":544},"server_tool_use":{"web_search_requests":0,"web_fetch_requests":0},"service_tier":"standard","cache_creation":{"ephemeral_1h_input_tokens":2379,"ephemeral_5m_input_tokens":0},"inference_geo":"global","iterations":[{"input_tokens":2,"output_tokens":970,"cache_read_input_tokens":3189,"cache_creation_input_tokens":2379,"cache_creation":{"ephemeral_5m_input_tokens":0,"ephemeral_1h_input_tokens":2379},"type":"message"}],"speed":"standard","fallback_credit":null},"modelUsage":{"claude-opus-5-5":{"inputTokens":2,"outputTokens":970,"cacheReadInputTokens":3189,"cacheCreationInputTokens":2379,"webSearchRequests":0,"costUSD":0.0390778,"contextWindow":1000000,"maxOutputTokens":128000,"thinkingTokens":544,"canonicalModel":"claude-opus-5-5","provider":"firstParty","costBasis":"list"}},"permission_denials":[],"terminal_reason":"completed","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","subagent_stats":{"spawned":0,"requested":{"background":0,"foreground":0,"unset":0},"started_in_background":0,"max_depth":0,"spawned_by_subagents":0,"completed":0,"failed":0,"killed":{"parent":0,"user":0,"system":0},"refused":{"depth_limit":0,"concurrency_limit":0,"budget":0},"by_type":{}},"safety_stops":0,"is_error":false,"num_turns":1,"subtype":"success","api_error_status":null,"result":"**結論**：檔名用正規化路徑的 SHA-256 hex（小寫 hex 字元），另在 metadata 存原始路徑。原始大小寫要保留，但存成 metadata，不放進檔名。\n**理由**：共享目錄掛在 Windows/SMB 時通常不分大小寫，所以檔名不能靠大小寫區分；用 hex 雜湊可以避開大小寫、非法字元和長度上限的問題。存原路徑幾乎沒有成本，還能偵測碰撞、方便除錯。\n**取捨**：只有 Windows 來源的路徑才做 casefold。Linux 路徑若一律轉小寫，`/a/Foo` 和 `/a/foo` 會共用同一個鍵，讀到錯的快取。這種錯不會報錯，比多存一份快取更難發現。\n**套用前要驗證**：Linux 端的實際資料裡是否真的有只差大小寫的路徑，以及同一個檔案是否需要從兩個 OS 命中同一筆快取。如果需要跨 OS 命中，就得先把 `C:\\…` 和 `/mnt/…` 對應到同一個根路徑再做雜湊，單靠大小寫正規化不夠。","ttft_ms":8178,"type":"result","duration_ms":15259,"uuid":"70b347f7-96d9-4aef-a5a3-707046c4425f","ttft_stream_ms":1869,"time_to_request_ms":796,"time_to_request_phases_ms":{"other":31,"system_prompt":89,"resume_other":0,"process_user_input":4,"input_attachments":9,"input_hooks":488,"tool_pool_refresh":3,"init_message":10,"engine_pickup":1,"message_prep":2,"model_config_wait":0,"autocompact":3,"query_setup":47,"auto_mode_context_wait":99,"model_gates":0,"tool_schema_build":1,"message_normalization":1,"client_creation":3,"request_build":5},"turn_start_resume_kind":"none","process_turn_index":0,"time_to_request_loop_lag_ms":58,"time_to_request_cpu_ms":547,"time_to_request_major_faults":4518,"input_attachments_detail":{"slowest_producer":"queued_commands","slowest_producer_ms":5,"cpu_ms":0,"major_faults":128},"first_content_frame_ms":1869,"queued_turn_count":0,"result_index":0}
```

**零 ledger 事件**：呼叫 B 前後案例 ledger 皆 4 行、雜湊 `2619df235c233ab7` 不變（見 E6 `before2`／`after2`）；僅新增 `tmp/_advisor_cli_ticketless/` 內 1 個執行記錄檔。**消費（executor）**：回答建議「檔名用正規化路徑的 SHA-256 hex、原路徑存 metadata；僅 Windows 來源做 casefold；套用前驗證 Linux 資料是否有只差大小寫路徑，且是否需跨 OS 命中同一筆快取」。本地驗證（`bf7F9EdR`，07:48:57）：sha256 hex 為小寫（64 字元）、`'/a/Foo'.lower()==...` 撞鍵屬實、原路徑 sha256 可區分兩者。處置：採納「原路徑存 metadata、檔名用 hash」之取捨結論；其前提「跨 OS 命中同一筆快取」為設計問題之開放項，於此案不可由本地驗證判定，**不採納為已解**（票前問題，無程式變更）。

### E6 副作用快照（`snap.py` 改版，唯讀；`rev_snap_before1`／`after1`／`before2`／`after2`）

檔案 sha256：before1 `4231f993c3245ef63fa2bf1d19d28b631520fa55b61bedb587d9db99d1d22709`；after1 `c9019db50cd37aab50027d5164850b1dae355d93eac8b60e301650bf4a720150`；before2 `92f6667d4d88dae81591be419185b914ba1302edff5f05add0f7f1a768ce618e`；after2 `3d67208d6ab981219027c491c6a37978f3d5c37b85fa55fc3a5327f7fc020999`。
- **原 repo**（`git status --short`、`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.git/gate` 各檔雜湊、`~/.claude/settings.json`、`advisor_cli*` 目錄）：呼叫 A 前後相同＝True、呼叫 B 前後相同＝True、before1 對 after2 相同＝True。逐字（before1 之 `orig` 區塊中非 ledger 項）：`git_status=[]`、`verify_cmd_conf=28a7671fc14d8481`、`active_json=fda74b68b627730d`、`settings_json=061d178b73d911b0`、`advisor_cli_dirs=[]`、`.git/gate` 共 32 檔雜湊兩端一致（其中 `ADV-STOPWAIT-01.jsonl=eae30ede3d2410e4`；本票 `ADV-REVERIFY-01` 無 ledger 檔）。
- **隔離案例 repo**（逐字，`case` 區塊）：
```
before1(呼叫A前，incident open 後): {"git_gate": {"CASE-REV-01.jsonl": "eaa10b9bed7aee88"}, "ledger_lines": 1, "tmp_files": []}
after1(呼叫A後):                    {"git_gate": {"CASE-REV-01.jsonl": "83b97b63d78b3dfa"}, "ledger_lines": 2, "tmp_files": ["tmp\\CASE-REV-01\\advisor_cli\\advisor_cli_20261024T074741Z_c93a3003.json"]}
before2(round start/end 後、呼叫B前): {"git_gate": {"CASE-REV-01.jsonl": "2619df235c233ab7"}, "ledger_lines": 4, "tmp_files": ["tmp\\CASE-REV-01\\advisor_cli\\advisor_cli_20261024T074741Z_c93a3003.json"]}
after2(呼叫B後):                    {"git_gate": {"CASE-REV-01.jsonl": "2619df235c233ab7"}, "ledger_lines": 4, "tmp_files": ["tmp\\CASE-REV-01\\advisor_cli\\advisor_cli_20261024T074741Z_c93a3003.json", "tmp\\_advisor_cli_ticketless\\advisor_cli_20261024T074846Z_853a8676.json"]}
```

- 預期符合：A 使 ledger 1→2 行並新增 1 個執行記錄檔（consult +1）；B 使 ledger 4→4 行（零事件）並新增 `tmp/_advisor_cli_ticketless/` 1 個記錄檔。
- **已知項**：`~/.claude.json` 位元組數 104067→104126（呼叫 A）→104126→104126（呼叫 B 無增，CLI 自行更新）；`~/.claude/projects` 子目錄數 18→18（無新增；共用同一 CLI cwd 目錄）。

### E7 預算核對表（AC 預算；HC1）

| # | 呼叫 | 入口執行記錄檔 | 啟動結果 | 計入預算 |
| --- | --- | --- | --- | --- |
| 1 | A（有票） | `revcase/tmp/CASE-REV-01/advisor_cli/advisor_cli_20261024T074741Z_c93a3003.json` | 成功（exit 0、RECORDED） | 是 |
| 2 | B（無票） | `revcase/tmp/_advisor_cli_ticketless/advisor_cli_20261024T074846Z_853a8676.json` | 成功（exit 0、ANSWERED） | 是 |

核對：`advisor_cli*.json` 於 `revcase/`（案例）共 2 檔（`find -name 'advisor_cli_*.json' -newer prep_rev2.py` 於 scratchpad 全域亦為 2 檔）；原 repo `advisor_cli_dirs=[]` 不變；舊隔離案例 `rev1case/`、歷史 `clicase/` 無新增記錄檔；`claude --version`／`claude auth status` 不發模型請求、不計入。**真實 `claude -p` 啟動總數＝2，無第三次，無失敗啟動。**

### E8 約定逐項（現版真實證據／仍未驗；證明範圍與限制）

| 約定 | 狀態 | 證據 | 證明範圍與限制 |
| --- | --- | --- | --- |
| P1-A① 有票諮詢 | **現版真實證據（受控代表案例）** | E4：`--ticket CASE-REV-01` 於 `Pending/` 票＋open incident，`disposition=RECORDED`、`channel=claude-subscription-cli`、`model=claude-opus-5-5`、`effort_flag=high`、`api_key_source=none`；ledger 恰 +1 筆 `consult --kind advisor`，note 含 `source=advisor.claude-cli via=route.py`；回答抵達 executor 並被消費 | 受控案例、同一 executor 設計；`apiKeySource=none` 不證訂閱帳務；effort 實際生效值未回報；hooks／plugins 仍啟動；N=1 |
| P1-A② 無票諮詢 | **現版真實證據（受控代表案例）** | E5：`--no-ticket`，`disposition=ANSWERED`、`mode=ticketless`、同模型／旗標；案例 ledger 4→4、雜湊不變；僅新增 1 個記錄檔 | 受控案例；問題為 executor 設計之票前性質；N=1；「Gate 語義不變」僅以 ledger 零增量與入口 detail `ticketless: no ledger event was written` 佐證 |
| P1-A④ 來源／模型／錯誤回報 | **部分：來源與模型欄位現版真實證據；失敗回報仍僅 stub 層** | E4／E5 envelope 與記錄 `init` 標明通道、實際模型、`effort_flag`、`api_key_source`；E1 `test_route.py` 87 OK 含明標模擬之不可用案例（離線 stub） | 本票**不製造真實失敗**（HC2），失敗各類（未登入、模型不可用、逾時、被拒、不可解析）無現版真實證據，僅離線 stub；故該約定之「失敗明確回報」半部分仍為**未驗（僅 stub 層）** |
| P1-A⑤ 已知可做 vs 無法判定對照 | **現版證據（受控對照）** | E3：缺陷 K 由 executor 直接修復，transcript 序列在 K 之偵測→修復間**零** `route.py consult`；E4／E5：缺陷 U 與票前設計問題經諮詢取得後消費 | 對照由同一 executor 預先設計（Mechanism Bypass），K／U 各 N=1；僅證「已知可做不諮詢、無法判定才諮詢」之行為可被觀察，不證判斷準則之普遍性、不證顧問提供 executor 未知之資訊 |
| P1-C⑤ 經 Routing 以 Claude 訂閱 CLI 之真實全鏈 | **現版真實證據（受控代表案例）** | E4：取得（RECORDED）→消費（本地驗證前提與附帶說法後採納 (1)、不採納 (2)）→修驗（`check_u.py` 由 `AssertionError` 轉 `ok-u`）→返回（`round end --result pass`、incident closed） | 受控案例、同一 executor；消費可追溯至回答具體建議（`.lower()` 一行）；不證回答為解題所必需；N=1 |
| P1-B④ 真實快取命中 | **不在本票範圍** | — | 不可製造；本票未處理、未觸碰 |

**本票不寫成果表**：成果表回填由 `Skill(projman)` align 依本票收束後之 Transition Table 處理。

### E9 transcript `tool_use` 序列（UTC，id 尾 8 碼；本票真實呼叫段）

`RrtcV5K6`（07:41:48 AI-0 ①–⑤ 批次：HEAD／diff／hash／`claude --version`／`table check`／`test_route.py`／`claude auth status`）→ `ndAy5r1w`、`MfczqyEo`、`FFnjr84r`（首次建置 `rev1case/` 與其檢查、棄用過程，皆零模型呼叫；其間一次修正嘗試被權限層拒絕、未執行）→ `VD4bPeGy`（Write `prep_rev2.py`）→ `x3yPojtg`（建置 `revcase/`＋incident＋前快照）→ `8TJnLwMC`（Read `lastn.py`）→ `wpkhXSxh`（Edit K 修復）→ **`ZwB4zpBf`（呼叫 A）** → `mBXYqaQw`／`v6LZ6q6D`（讀取 envelope／ledger）→ `pisD2WmZ`（本地驗證回答前提）→ `jF2Z9XhA`／`we9uTnPX`（Read／Edit 套用 U 修復）→ `DhQu7Di1`（`check_u`／`check_k` 綠、`after1` 快照）→ `zxbkipyV`（round start／end、ledger status）→ **`zYVwxWzB`（呼叫 B，含 `before2` 快照）** → `bf7F9EdR`（B 本地驗證＋`after2` 快照＋原 repo 比對）→ 本附錄建置。

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）
- **交付**：以現版 `advisor.claude-cli`（`route.py` blob `e2215d2f`、`route_table.json` blob `6140a413`，HEAD `c13e69cf`）至多兩次真實訂閱 CLI 呼叫（A 有票、B 無票，皆成功，啟動總數 2）重驗 P1-A①②④⑤ 與 P1-C⑤；證據逐字見〈證據附錄〉。
- **範圍與限制（如實）**：受控代表案例、同一 executor 設計與執行（Mechanism Bypass）；各 N=1；P1-A④ 僅來源／模型欄位為現版真實證據，**失敗回報仍僅 stub 層**（HC2 不製造真實失敗）；顧問提供 executor 未知資訊不可證；`apiKeySource=none` 不證訂閱帳務；effort 實際生效值未回報。
- **QA／SV／Cross-Review**：依風險分級（ALLOWED 全為 `.md`、無 `app/`／腳本／hook、無不可逆操作、可 `git revert`）省略；`reviews` 四欄 `N_A`。
- **偏差**：首次案例建置因工具層吃掉反斜線致案例檔語法錯誤，於零模型呼叫階段發現並棄用（`rev1case/`），重建為 `revcase/`；其間一次修正命令被權限層拒絕、未改道重試同一動作。
- **成果表**：未寫入；P1-A／P1-C 之回填由 `Skill(projman)` align 處理。PROGRESS Dashboard 同步屬 FORBIDDEN 延後項，待 `/compact`。
