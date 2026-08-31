# MEO-FIX-A Rev J — 執行者重入派工 Briefing(Executor Re-entry Dispatch Briefing)

**日期**: 2026-07-26 ｜ **簽發**: CSO ｜ **配套票**: `Pending/MEO-FIX-A_Escalation_Gate_Relocation.md`(**Rev J**,SHA-256 `D823D165...`)
**授權**: Codex Rev J **delta 復審 PASS**(`reports/MEO-FIX-A_RevJ_Codex_Delta_ReReview_20260726.md`:`REPORT_FIX=CLEARED`／`REENTRY_AUTHORIZED=YES`／`CONF_REGEN=AUTHORIZED`);Akira 2026-07-26 核准派工。
**路由**: conf 再生 → `/qa` 重跑 → `/sv` → Codex 實作 cross-review → Traycer execute_review
**⚠️ 本次非新實作派工**:AI-1–AI-5 已於 Rev F 完成並 commit 至 `e065d20`;**AI-0 不重跑**(Codex Final Routing 明示 `AI-0: DO NOT RERUN`)。本輪任務=**票面治理修訂後之驗收重入**。
**紀律**: 票面為 SSOT;本 Briefing 為導讀,不替代票面。兩者衝突 → 停止回報。

---

## 一、conf 再生(原子組,重入第一動)

**MUST 自票面 Verify Config 整塊取值重建 `tmp/verify_cmd.conf`**,四鍵一次落定:

```text
REV=J
MAX_LINES=332
DIFF_REF=fdd6375
DIFF_END=e065d20
```

- **⚠️ 殘值覆蓋警示(本輪最高風險項)**:現行 conf 為 `REV=F`／**`MAX_LINES=300`**(Codex Rev J §2 實測)。Codex Final Routing 僅字面提及 `REV=J` 與 fixed refs,**未點名 `MAX_LINES`**;若逐鍵挑選式再生,`300` 將殘留於 conf 層——此即 Rev G→H 兩輪 BLOCKER(數值脫鉤／殘值脫鉤)之同族缺陷下沉至 conf。**禁逐鍵挑選,MUST 整塊取值**。
- `DIFF_REF`／`DIFF_END` **字面直取**,禁自現 HEAD 或任何間接來源重導出(票面 Rev J 重入協定 3)。
- **重開條件**:conf 任一鍵與票面 Verify Config 不一致 → 停止回報,禁續跑 `/qa`。

## 二、`/qa` 承接義務(三項,缺一即報告不完備)

**Q1 — AC §6-8(iv) MAX_LINES binding 證據**
- 唯一合法證據=**顯式 8 檔路徑** `git diff --numstat fdd6375 e065d20 -- <逐檔列名>` 之逐檔加總(預期 added=301／deleted=31／合計 **332**)。
- verifier Step 0 因 wildcard 漏算所示之 `+236` 為**已知漏算值**,**MUST NOT** 充當證據;`/qa` 報告 MUST 明文註記此標記。
- **⚠️ 零餘裕警示**:實測 332 = `MAX_LINES` 上限,**PASS 於精確邊界**(Codex Rev J §4.1 `RESULT=PASS_AT_EXACT_BOUNDARY`)。**任何** tracked ALLOWED 檔案之新增行(含 `/qa`／`/sv` 期間之修補)即立刻超額 FAIL。若驗收期發現須改實作 → **STOP 回報,禁自行上調 `MAX_LINES`**(上調屬閾值變更,須經 CSO 裁決與新一輪 delta 復審)。

**Q2 — AC §6-8(iii) 現況結構性稽核(Rev I/J 改制,首次執行)**
- **(B) 機械判準**:以票面原樣 16 鍵判準式對 live `STATE.md` 執行,`grep -nvE ...` **零命中**方為 PASS。16 鍵集合為 13 live hook keys + 3 SSOT/schema keys,**MUST NOT** 於驗收期自行增列以求綠。
- **(C) 註記行分類**:分類域**僅限 `#` 起首行**;16 合法鍵行由 (B) 承接、**不入 (C)**。`#` 行逐塊歸入 (2) 遷移區塊(雙錨 `# Legacy pending migration (2026-07-25,` → `K4-cand-20260725-01 registered.`,區塊內禁現 `KEY=` 行、禁增修一字)與 (3) agent 註記區塊二類。
- **(A) 誠實邊界**:baseline SHA-256 實值不存在、歷史 delta 不可重建。**MUST NOT** 宣稱以 baseline SHA 證明「已變更」,**MUST NOT** 以現行 SHA 回填 before-state。
- **(D) 人工審查明文標記**:第 (3) 類之內容語意適格性屬**人工結構審查**,`/qa` 報告 **MUST** 明文標記為人工審查結論,**MUST NOT** 宣稱 (C) 各類均達鍵級機械白名單。

**Q3 — Codex Advisory 承接(Rev I 提出、Rev J 維持有效)**
- 重入後若產生新 adjudication／audit artifacts:**MUST** 逐檔核對檔名、內容與 git-tracked 狀態,並於 AC §6-8(ii) 逐檔名列舉。
- **MUST NOT** 因固定 `DIFF_END=e065d20` 未涵蓋新產物即推定無新增。
- artifacts **不入行數計帳**(票面 §C.8 Rev I 改制),但 **git-tracked 義務不變**、**MUST NOT** 加入 `.gitignore` 或外部歸檔。

## 三、執行紅線(零觸碰清單)

1. **AI-0 不重跑**——其斷言為 Rev F 開工時態,已驗訖並經 AI-1–AI-5 合法推進(現況:目錄存在、tracked=8、HEAD=`e065d20`)。
2. **AI-1–AI-5 程式碼與 fixture 零變動**;`STATE.md` **零觸碰**(本輪僅唯讀稽核);Error Handling α 條款**字面零變動**。
3. **票面錨即時失效律**:`D823D165...` 為 Rev J 通過內容之識別錨;票面任何字元變動即令其失效,須重算 SHA 並重送 Codex delta 復審後方得續行。**驗收期禁修票面**。
4. 禁清零 `STREAK`／禁動閾值(D1 `RETRY>=2 ∥ STREAK>=2`)／禁增第四 Stop hook／禁碰 FORBIDDEN 七項(含 `ticket_pattern.md`、`verify_checklist.sh`、`.claude/settings.json`)。
5. delta PASS **不等於** `/qa`／`/sv`／implementation cross-review 已通過(Codex Rev J §3 明示未擴張解讀);後續層級責任獨立。
6. commit 前綴 `[MEO-FIX-A]`;禁混 `.kt/.xml`;`.claude/PROGRESS.md` 滾動 S-row 屬 session 代謝,禁夾帶入本票 commit。

## 四、α 過渡授權狀態(現況承接,無變更)

- 比對域維持 Rev F 字面化裁決:`MEO_LAST_ERROR_CLASS` ⟷ 字面值 `Exit` 字面等值,合格清單僅此一項,禁語義擴張／禁正則近似。
- 額度:已動用 **2/3**,第 3 次額度已解凍;動用時 MUST 逐項存證並**明引票面 Error Handling α 條款**(禁引記憶檔 `meo-escalation-transitional-alpha`——該檔為非權威轉錄)。
- α 於本票落地後自然退役,不須另行撤銷。

## 五、Purpose Statement

**本次派工目標**:以 `REV=J` 整塊再生 conf 後完成 `/qa`→`/sv` 重入,產出含 Q1–Q3 三項承接證據之完整驗收欄,並送 Codex 實作 cross-review。
**預期帶回**:(a) 顯式 8 檔 numstat 逐檔加總證據(332 邊界);(b) AC §6-8(iii)(B) 判準式零命中原始輸出＋(C) 註記行分類清單＋(D) 人工審查標記;(c) 新增 artifacts 逐檔核對結果(有則列舉、無則明載「本輪未產生」);(d) SV 報告與 Codex cross-review 回執。


---
**冊面收束尾註(CSO,2026-08-01)**:本 Briefing 隨 MEO v1 執法層前提消滅型退役收束(registry CLOSED + STOPPED 語意 checkpoint)。標的 MEO-FIX-A 已 STOPPED,Rev J 驗收重入不再發生;文首「授權/路由」為簽發時措辭,自本收束日起失效。裁決鏈=vault `2026-08-01_MEO_V1_Enforcement_Layer_Decommission.md`。
