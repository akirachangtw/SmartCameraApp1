# MEO-FIX-A Rev K 補件令(Supplement Briefing)

**日期**: 2026-08-01 ｜ **簽發**: CSO ｜ **母件**: `Pending/MEO-FIX-A_RevK_Executor_Briefing.md`(範圍與紀律全數沿用,本令僅補落檔)
**授權**: Akira 2026-08-01 裁示 1-a(發回補件)。
**Rev A(2026-08-01,CSO)**:S-1 前置快照由靜態票載改為 **live 直取制**。事件:槽位於 07-31 14:41(INFRA-TPL-V32-01)後復於 19:54 遭 INFRA-PROGRESS-COMPRESS-02 覆寫,執行者依 §三 正確 STOP;靜態快照 5 小時內失效兩次,證明追逐式改名不可持續。live 直取制下 backup 恆保全任意佔用者,佔用者身分不再構成 STOP 事由。
**背景**: Rev K R-1 已獨立驗收 PASS(commit `dd8de87` 僅 SKILL.md、8+/8−淨行數 0、三改寫方向落實、水位 live re-probe 8/7/false 無變動)。惟回報之三項產出經 CSO 全 repo `modified_after` 掃描**查無耐久載體**,依 1-a 發回補件。**本令範圍限落檔動作,零 git 觸碰、豁免窗不啟用。**

---

## 一、補件範圍(僅此四項)

### S-1 conf Rev K 再生(遲交)

**⚠️ 前置(record-before-clear,MUST 先行,Rev A live 直取制)**:`tmp/verify_cmd.conf` 為多票共用單槽位,佔用者隨時可能更替(CSO 08-01 實測現值=INFRA-PROGRESS-COMPRESS-02/`ANCHOR_PROBED=2026-07-31 19:54`;**此值僅供參考,禁作執行依據**)。覆寫前 MUST 依序:①live read 該檔;②自檔內字面提取 `TICKET_ID` 值為 `LIVE_ID`;③落 backup:
```
LIVE_ID=$(sed -n 's/^TICKET_ID=//p' tmp/verify_cmd.conf)
cp tmp/verify_cmd.conf "tmp/verify_cmd.conf.${LIVE_ID}.bak"
```
backup 失敗 → STOP 回報,禁續行。

**再生(整塊禁逐鍵)**:依票面 Verify Config(`MEO-FIX-A_Escalation_Gate_Relocation.md` L105–110)整塊取值重建,惟四鍵更新為:
```
REV=K
MAX_LINES=332
DIFF_REF=fdd6375
DIFF_END=dd8de87
```
`DIFF_END` 為**字面直取**;**禁自現 HEAD 重導出**(現 HEAD=`06a377f` 屬 INFRA-TICKET-TPL-01 commit,非本票)。

### S-2 R-3 deviation 登錄落檔

依母件 §二 R-3 原文段落,寫入**本輪新建** `/qa` 報告檔(`tmp/sv_report_<timestamp>.md`,timestamp 為真實時戳、禁佔位符),段落 MUST 含字面:「documented deviation」、`fdd6375..e065d20`、「Rev J 法錨連鎖崩潰」、「RevJ Briefing 紅線 6 承載」。

### S-3 R-2 一致性複查清單落檔

已知落差(自回報訊息轉錄,防對話層遺失):**decision-tree.yaml L207 + stop_orchestrator.sh L3 檔頭契約敘述與實際 fail-open 行為有落差**。
MUST 將完整清單寫入 S-2 同一報告檔之「R-2 一致性複查」段,逐項格式:檔案/行號/現值文字/hook 真實語意/落差描述。**禁順手修**(範圍擴張須 CSO 裁決,另立票候裁)。

### S-4 未歸因 STREAK +1 取證(前欠清償)

隨附 hook log 相關段落於 S-2 報告檔,或明文「hook log unavailable」聲明;**禁沉默略過**。

## 二、Hard Constraints

1. **零 git 觸碰**:全部標的(`tmp/`、`plans/cso_briefing/`)均非 git-tracked;豁免窗不啟用、不消耗。
2. `STATE.md` 零觸碰;gate 檔(`meo_pretooluse_gate.sh`)零觸碰;SKILL.md 零觸碰(R-1 已結)。
3. 零 deny 預算延續:首筆非預期 deny/非零退出立即停止回報,禁補救。
4. `verify_cmd.conf` 覆寫前 S-1 backup 為絕對前置。

## 三、Error Handling

- S-1 backup 失敗、任一路徑不可寫、或 live read 之 `TICKET_ID` 鍵缺失/`LIVE_ID` 空值 → **STOP 回報**,禁改道、禁自裁。(Rev A:佔用者身分比對條款廢止——live 直取制下 backup 恆保全任意佔用者,無比對必要。)

## 四、Acceptance Criteria(CSO 逐項 re-probe,禁信回報)

- [ ] **[存在性]** `tmp/verify_cmd.conf.<LIVE_ID>.bak` 存在(`LIVE_ID`=覆寫當下自檔內提取之 `TICKET_ID` 字面),內容與覆寫前原值逐字節一致
- [ ] **[存在性]** `tmp/verify_cmd.conf` 四鍵=`REV=K`/`DIFF_REF=fdd6375`/`DIFF_END=dd8de87`/`MAX_LINES=332`
- [ ] **[存在性]** 新 `tmp/sv_report_<ts>.md` 含 deviation 段(S-2 字面四要素)、R-2 段(含 `decision-tree.yaml` L207 與 `stop_orchestrator.sh` L3 逐項對照)、S-4 hook log 段或 unavailable 聲明
- [ ] **[行為性]** 回報訊息 MUST 附三個確切檔案路徑(backup/conf/報告)供 CSO re-probe;缺路徑即退件

## 五、後續序列

補件 AC 全過 → 重送 Codex 實作 cross-review(隨附母件 §四釋義)→ PASS → FIX-B 暫緩令解除派工。
