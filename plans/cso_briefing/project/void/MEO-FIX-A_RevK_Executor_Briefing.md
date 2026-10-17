# MEO-FIX-A Rev K — 回修輪派工 Briefing(Remediation Dispatch Briefing)

**日期**: 2026-07-30 ｜ **簽發**: CSO ｜ **配套票**: `Pending/MEO-FIX-A_Escalation_Gate_Relocation.md`(Rev J,票面零變動,SHA 錨 `D823D165...` 仍有效)
**授權**: Codex 實作 cross-review 🔴(2026-07-30,三 findings);Akira 同日裁示:必修 1 **駁回**(甲案,fail-open 維持)/必修 2 收案/必修 3 改 deviation 登錄/**FIX-B 派工暫緩令**(序列化:本回修+Codex 重審 PASS → FIX-B 開工)。
**紀律**: 票面為 SSOT;驗收期禁修票面。本 Briefing 綁定回修範圍,範圍外零觸碰。

---

## 〇、凍結令豁免窗(派工訊息明示後生效)

凍結令(2026-07-28,含 MEO 字面路徑 git 操作)對**本回修 commit 單次**開豁免窗,條件三項全備:①開工 re-probe `MEO_ESCALATION_PENDING=false`;②高風險呼叫前診斷先行(真實時戳,禁佔位符);③每一 `git add` 前 `git check-ignore -v` 前置。**本豁免窗須 Akira 於派工訊息中明示「豁免窗生效」方為有效**;豁免不及於 `meo/adjudications/*`(BF 遞延不變)。

## 一、開工檢核(四項全過方得續行)

1. `MEO_ESCALATION_PENDING=false` re-probe;true → 停止回報。
2. 水位 re-probe:基線 `STREAK=8`/`WM_STREAK=7`/delta=1 → **零 deny 預算,首筆非預期 deny/非零退出立即停止回報,禁補救**。
3. 審查方行號集 live probe:SKILL.md L28/99/136/153-154/195/199-201 逐行取現值(禁憑本 Briefing 或審查報告之摘錄逕改)。
4. **gate 檔零觸碰確認**:`meo_pretooluse_gate.sh` 本輪不在修改範圍(必修 1 已駁回;L125 fail-open 為 FIX-B HC4 鎖定之現行法)。

## 二、回修範圍(僅此三項)

**R-1 SKILL.md 舊敘述清理(必修 2)**:標的=上列行號集。改寫方向:①裁決主體改為 PreToolUse gate 機械判準(pending gate/字面閘/`release_pending()` ID 匹配消費),刪除「orchestrator 裁決/送 orchestrator/orchestrator review 判定」語義裁決敘述;②hook 契約改為 `hookSpecificOutput.permissionDecision`(刪 top-level `{decision, reason}` 舊契約);③`stop_orchestrator.sh` 僅得描述為 review gate pass/fail 承載體(D5),不得描述為 escalation 裁決三態主體。
**⚠️ 淨行數硬約束**:計帳域現值 332=`MAX_LINES` 精確邊界,本輪 `added−deleted ≤ 0`(替換不增行/純刪);做不到 → STOP 回報,禁自行上調。

**R-2 一致性複查(唯讀)**:`decision-tree.yaml`、`gate.conf` 對 hook 真實語意逐項核對。發現不一致 → **列清單停止回報,禁順手修**(範圍擴張須 CSO 裁決)。

**R-3 Deviation 登錄(必修 3 改裁)**:於 `/qa` 報告明文段落登錄:「`.claude/PROGRESS.md` 出現於 `fdd6375..e065d20` 為未授權改動,收案為 documented deviation;拆離駁回理由=歷史重寫將使 `e065d20` 失效,Rev J 法錨(票面 SHA、conf 字面 refs、332 邊界證據)連鎖崩潰;防再犯由 RevJ Briefing 紅線 6 承載」。

## 三、conf 再生(Rev K,回修 commit 後執行)

整塊再生禁逐鍵:`REV=K`;`DIFF_REF=fdd6375` 維持;`DIFF_END=<回修 commit 後 git rev-parse --short HEAD 字面回填>`;`MAX_LINES=332` 不變(R-1 淨行數約束保證)。commit 前綴 `[MEO-FIX-A]`,禁夾帶 PROGRESS.md(紅線 6)。

## 四、重送 Codex 之隨附釋義(必修 1 駁回,原文引用)

> 必修 1 不採納,依據:①票面 L227「STOP 回報(見 Error Handling)」為執行者面語義(hook 運行時無回報能力),非運行時 deny 指令;②MEO-FIX-B HC4(2026-07-28)字面鎖定「PROJECT_ROOT 不可判定時仍 emit allow,現行行為不得改」,為現行後法;③root 不可判定時治理面路徑匹配(均依 `$PROJECT_ROOT` 前綴)已失效,fail-closed 僅能全量 deny,將封死 session 一切工具且解封需治理面寫入而復被封,構成自舉死鎖,違 gate 自舉不變量。安全暴露窗已知悉;收緊之唯一合法通道為對 HC4 之 supersede 確認決議,不在本回修輪。

## 五、Purpose Statement

**目標**:完成 R-1/R-2/R-3,conf Rev K 再生,重送 Codex 實作 cross-review(隨附 §四釋義)。
**預期帶回**:(a) SKILL.md 行號集逐處 before/after 對照+淨行數證明(`git diff --numstat` 該檔 added≤deleted);(b) 一致性複查清單(不一致項或「零不一致」明載);(c) /qa 報告含 deviation 登錄段;(d) conf Rev K 四鍵回執;(e) Codex 重審回執;(f) 零 deny 全程自證(開工/收工水位對照)。
