# RGR-REPLAY-01: ticket-rigor-gate 互動修補的相稱驗證須含原反例重放，並於被修補宣稱仍支配未修改之相鄰分支或已點名 consumer 時加測一個最小判別案例（單檔 REPAIR_ROUTING.md 條件 4 增補）

**Revision**: Rev A.2（2026-10-21，RGR Preflight Cycle 2 依 Akira F1 裁決〔採 C〕改寫判別案例觸發：AI-1 目標行、語義對應、AC 第 1 條片語、HC3、標題、承重推導第 3 點、W1／W2／W3／W6，新增 W9 與已知限制⑧；最低驗證〔原情境重放〕、final report、closure loop 語義不變）。前版：Rev A.1（2026-10-21，RGR Preflight Cycle 1 修補 F2–F4：diff 檢核排除他票提交、`recorded invariant` 出處、條件 4 兩種失敗之走查 W7／W8；F1 Decision-Fork 待 Akira 裁決，W1 未動；報告 `.tmp/rigor-gate/RGR_RGR-REPLAY-01_20261021.md`）。前版：Rev A（2026-10-21，首次開票）。

**簽發日期**: 2026-10-21
**簽發者**: Claude
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無（RGR-MIGRATION-LITE-01 之 Family Closure 與 RGR-AUTONOMOUS-CLOSURE-01 之 closure loop 為既有條文背景，已 CLOSED，非本票判準、錨點或 AC 之來源票）
- `coordination_dependency`: 無（同 plan 的 RGR-DERIV-01、RGR-EQUIV-01、RGR-RERUN-01 改的是 `SKILL.md`，本票改 `REPAIR_ROUTING.md`，檔案互斥，可獨立先行）

**檔案治理／來源說明**：本票為 Plan-backed 批次中的一張獨立票。
- Parent type: Plan-backed
- Parent absolute path: `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261021-rgr-improvement-plan.md`（`.tmp` 路徑僅為讀取位置，下列內容已嵌入，本票於該檔消失後仍自足）
- Parent artifact status: `Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、`Status: FINAL`
- Plan-local item ID: `T04`（非票號，僅留於本區）
- Parent read timestamp: 2026-10-21 14:36+08:00（批次 shared preflight 起始處讀取）
- Selected item（Ticket Map 原文摘要，含 Akira 2026-10-21 收窄文字）: 在 `REPAIR_ROUTING.md` General Autonomous Repair Default 條件 4 補最低驗證內容：最低驗證先以原 finding 的觸發情境重放修補後結果；僅當本次修補改變同一宣稱下的相鄰分支，或票面已明示的 consumer 時，再選擇一個最小判別案例，確認已記錄的不變量仍成立。結果寫進既有 final report，不新增欄位；重放或必要判別案例失敗，計為既有 closure loop 的失敗，輪數與計數語義不變。不觸及 Guardrail 5、Erratum Lane、Dispatch Authority 或 `.claude/CLAUDE.md`。trigger：互動 lane 對任何 finding 的自主修補（已落在四條件測試內者）。退出條件：重放通過（且在條件成立時，判別案例通過）。
- Plan Gate／eligibility 結果: PASS／ALLOWED
- 相關核定（Akira 2026-10-21，plan 指令原文，全數 APPROVED）：R1 進入 RGR 最小改善計畫；R5 D 修後原反例重放的最低驗證內容落 `REPAIR_ROUTING.md`；R6 最小成對驗證用 E1、E2、E3（本票對應 E3）；R7 不新增 token、severity 列、reason code、Gate、lifecycle 或 repair authority；N3 不得建立常駐 benchmark、案例庫、ledger、第二 Gate、repo-wide tracing；N4 不得重開 TKA-BATCH-01。Akira 2026-10-21 另裁決：Plan Gate、四項獨立票均接受，並提供 T04 收窄文字（加測只在修補波及相鄰分支或已明示 consumer 時）；隔離成對評測維持未授權，不阻擋開票。Akira 2026-10-21 RGR F1 裁決（採 C，Rev A.2 落地）：判別案例觸發改為「僅當被修補的宣稱仍支配一個未被修改的相鄰分支，或一個票面已點名、但未被修改的 consumer 時，才額外檢查一個最小判別案例」；理由：判別案例用來驗證修補後同一宣稱覆蓋的殘餘相鄰面，被修補的子句本身已由原情境重放覆蓋，不再以其為觸發原因，避免同一情境以不同名稱重複驗證；最低驗證基線（任何修補先重放原觸發情境）保留。此裁決取代上列收窄文字中「改變／波及」的觸發措辭。
- 範圍外（deferred，不屬本票）：A（RGR-DERIV-01）、B（RGR-EQUIV-01）、C（RGR-RERUN-01）；第二個 loop、ledger；closure loop 輪數、Family Closure 計數、Guardrail 5、Erratum Lane、Dispatch Authority；`.claude/CLAUDE.md` AC 修訂閉環（其「重驗方式不限」屬 POLICY）；隔離評測。

**開票時 live 證據基線**（2026-10-21 14:44–14:46+08:00，`main` @ `c8385e64`，工作樹乾淨）：
- 錨點 `4. A proportionate verification method exists and is actually run before the fix is reported done.` 於 `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md` 恰 1 次（L125，`**The four-condition test.**` L114 之第 4 條）；其後為空行與 `Success is reported only in the final report: what was fixed and what verification confirmed it.`（L127）。`REPAIR_ROUTING.md` blob `95109c84`，161 行。`adjacent branch` 與 `minimal discriminating case` 於該檔為 0 次（尚未落地）。
- 相鄰既有條文（均不得改動）：`**Session-local closure loop.**`（L133，含 `Each iteration is repair → proportionate verification → rerun gate under the existing Mechanical Verdict Rule.`）、`### Migration-Lite Family Closure`（L137）、`## Post-Signature Erratum Lane`（L67）、`### Dispatch Authority`（L40）、Guardrail 5（`5. **Post-repair diff verification.**` L36，屬 runner lane）。各錨點皆恰 1 次。
- consumer 盤點：`uc/SKILL.md:82` 依賴 `REPAIR_ROUTING.md` Erratum Lane「條目數仍為四項」，本票不觸及該節；`.claude/` 內其餘檔案不依賴條件 4 的文字；`runner/` 無對應鏡像；無鏡像副本。單檔修改可行。
- 重複票檢查：Registry（topic `RGR`，main＋archive）除本批同 plan 的三張外無等價 outcome（最近者 `RGR-AUTONOMOUS-CLOSURE-01`〔closure loop 立法〕、`RGR-MIGRATION-LITE-01`〔Family Closure〕標的與 outcome 不同）；`Pending/`、`deliver/`（含 `deliver/archive/`）、`void/` 對「replay／重放」的命中皆為其他議題對 Cross-Review 等的用語，無等價 RGR outcome；`RGR-REPLAY-01` 於全冊、檔名、commit 皆未使用（僅同批票檔文字引用）。
- 並行狀況：`Pending/` 內無他票 ALLOWED 觸及 `REPAIR_ROUTING.md`（兄弟票皆為 `SKILL.md`）。
- `git check-ignore -v` 對 `REPAIR_ROUTING.md` 無命中（exit 1）。

**承重推導反證紀錄**（依 `ticket-author/SKILL.md` Evidence gate「no hidden semantic decision」條款）：
1. 推導：「重放失敗計為既有 closure loop 的失敗，輪數與計數語義不變，故第三輪未 PASS 仍依既有規則停止」。
   - 反證狀態：Migration-Lite family 內，重放失敗發生在 gate 重跑之前；Family Closure 的「第二次出現」以 gate 重跑的 finding 為準，重放失敗不產生 gate finding，因此該 iteration 失敗但 family 計數不增。符合前提卻得到「iteration 計一次、family 出現不計」。兩個計數本來就獨立（closure loop 計 iteration，Family Closure 計 gate 重跑的 finding 出現），不導出阻塞路徑。
   - 處置：無需決策，記錄於此；已知限制④揭露。
2. 推導：「最低驗證須重放原觸發情境，故任何互動修補都能做到」。
   - 反證狀態：符合前提的邊界狀態——（a）finding 沒有離散情境（命名不一致、缺交叉引用）：觸發情境即「產生該 finding 的檢查」，重放＝對修補後文字重跑該檢查；（b）修補以刪除被指出的子句達成：重放必然通過，形式上通過但可能掩蓋內容消失；（c）finding 屬決策類而不可自主修補：根本不進入四條件測試。（a）（c）不導出阻塞；（b）為空洞通過風險，非本票可決：它是否屬縮減 scope 由 `.claude/CLAUDE.md` AC 修訂閉環與四條件測試條件 2 另管。
   - 處置：已核定決策可定（Akira 2026-10-21 收窄文字僅要求重放與條件式加測，未要求防止刪除式修補）；（b）列為已知限制⑤，不在本票處理。
3. 推導：「加測只在被修補宣稱仍支配未修改之相鄰分支或已點名 consumer 時，故不構成全面 consumer sweep」。
   - 反證狀態：被修補宣稱仍支配一個票面未點名、但實際存在的 consumer——條文不要求檢查，可能漏檢。這是收窄文字與 F1 裁決的刻意取捨（排除全面 sweep 的過度治理風險）。
   - 處置：已核定決策可定（Akira 2026-10-21 收窄文字與 F1 裁決）；已知限制⑥揭露。
4. 推導：「被修補的子句本身已由原情境重放覆蓋，故不需再以它觸發判別案例」。
   - 反證狀態：修補讓原觸發情境通過，卻在同一子句的另一個輸入上破壞不變量（原情境重放不會走到該輸入）；若宣稱下沒有其他未修改分支或已點名 consumer，判別案例不觸發。符合前提卻得到「同一子句的其他輸入未被條件 4 檢查」。
   - 處置：已核定決策可定（Akira 2026-10-21 F1 裁決明示此取捨）；該殘餘窗口由 closure loop 每輪的 gate 重跑承接（`REPAIR_ROUTING.md` Session-local closure loop：repair → proportionate verification → rerun gate），已知限制⑧揭露。

**Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；本次 invocation 全文讀取（322 行），本項復用 shared preflight 的讀取，讀取後未變。

## Verify Config

TICKET_ID=RGR-REPLAY-01
CREATED=2026-10-21
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/uc/SKILL.md,.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/CLAUDE.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/deliver/TKA-BATCH-01_selected-set.md,plans/cso_briefing/project/deliver/TKA-EVGATE-01_derivation.md
MAX_LINES=15
DIFF_REF=6c4972e5
ANCHOR_PROBED=2026-10-21 14:44
BASE_COMMIT=c8385e648b6c2fb808f923baae0ec64374aae6b0
TARGET_BLOB=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md:95109c84
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

`MAX_LINES` 僅為估算校準遙測，非 gate（Template v3.5 起）。`INTRODUCED_CAPABILITY=NONE`：增補既有四條件測試第 4 條的文字，不導入新 feature／skill／tech。

## Action Items

### AI-0: 錨點與基線復驗（派工後、修改前）

- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- **動作**: 以 live `grep -cF` 逐字確認：①`4. A proportionate verification method exists and is actually run before the fix is reported done.` 恰 1 次；②`**Session-local closure loop.**` 與 `Each iteration is repair → proportionate verification → rerun gate under the existing Mechanical Verdict Rule.` 各恰 1 次；③`Success is reported only in the final report: what was fixed and what verification confirmed it.` 恰 1 次；④`## Post-Signature Erratum Lane`、`### Dispatch Authority`、`### Migration-Lite Family Closure` 各恰 1 次（用於修改後確認未被觸及）；⑤`minimal discriminating case` 為 0 次（尚未落地）。併做：⑥`git check-ignore -v .claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md` 無命中；⑦掃 `Pending/` 與冊面，確認無他票 ALLOWED 觸及 `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`（有 → 停止，須串行）；⑧記錄 `/mdlint` 對 `REPAIR_ROUTING.md` 的 baseline warning 數；⑨依 Template 基線漂移三分流處理 `BASE_COMMIT..HEAD` 間觸及 `REPAIR_ROUTING.md` 的提交；⑩確認 `uc/SKILL.md:82` 所引 Erratum Lane 四項條目數字面仍存在（本票不觸及該節，僅作修改前後不變的基線）；⑪回填 `DIFF_REF`。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成 （見收尾實錄）

### AI-1: 於條件 4 增補最低驗證內容

- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- **現值**（恰 1 次，整行）: `4. A proportionate verification method exists and is actually run before the fix is reported done.`
- **目標值**（整行替換為下列單一行；其前後行逐字不動）:

```text
4. A proportionate verification method exists and is actually run before the fix is reported done. At minimum it replays the finding's original triggering scenario against the repaired result. Only when the repaired claim still governs an adjacent branch that was not changed, or a consumer already named by the ticket that was not changed, it also checks one minimal discriminating case that the recorded invariant still holds. The result is reported in the existing final report with no new field, and a failed replay or required discriminating case counts as a failure of that iteration under the session-local closure loop, whose iteration count and counting semantics are unchanged.
```

- **語義對應**（plan T04 `acceptance_intent` 與 Akira 收窄文字）: `At minimum it replays the finding's original triggering scenario against the repaired result`＝「最低驗證先以原 finding 的觸發情境重放修補後結果」；`Only when the repaired claim still governs an adjacent branch that was not changed, or a consumer already named by the ticket that was not changed, it also checks one minimal discriminating case that the recorded invariant still holds`＝Akira 2026-10-21 F1 裁決（採 C）「僅當被修補的宣稱仍支配一個未被修改的相鄰分支，或一個票面已點名、但未被修改的 consumer 時，才額外檢查一個最小判別案例」（取代收窄文字「改變」措辭；被修補子句本身已由原情境重放覆蓋，不再作為觸發原因；且明示不做全面 consumer sweep）；`reported in the existing final report with no new field`＝「結果寫進既有 final report，不新增欄位」；`counts as a failure of that iteration under the session-local closure loop, whose iteration count and counting semantics are unchanged`＝「失敗計為既有 closure loop 的失敗；輪數與計數語義不變」。整行不含 token、severity 等級語句、reason code、新章節（R7）。`the recorded invariant`＝被審票對該宣稱已寫明的不變量；被審票未寫明時，執行者不得自行創設，改記一個 `Missing Contract Expansion` finding（`SKILL.md` Relationship To SSOT「must never silently invent one」；見 W7）。
- [x] 完成 （見收尾實錄）

### AI-2: 檢核、文字級走查與收尾實錄

- **檔案**: 不改檔（僅記錄於票檔收尾實錄）
- **動作**（本項所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票之 dirty 檔，及範圍內他票提交〔commit 前綴非 `[RGR-REPLAY-01]`〕觸及之檔案，皆不計）: ①`git diff --stat` 僅 `REPAIR_ROUTING.md`，diff 僅在 L125 該行（1 行刪、1 行增）；②新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES` 字樣，且不新增任何 heading、表格、清單層級、空行或檔案；③目標行以 `grep -cF` 逐字命中 1 次，AI-0 ②③④所列錨點仍各恰 1 次，Guardrail 5 該行（`5. **Post-repair diff verification.**`）仍恰 1 次；④本票「走查」W1–W9 逐條對照條文並記 `檔案:行號`；⑤`/mdlint` 對 `REPAIR_ROUTING.md` warning 數不高於 AI-0 baseline；⑥收尾實錄如實載明「模型遵循度未驗」與已知限制，不宣稱 Traycer 通過率、token 效率或品質提升，並載明「隔離成對評測未授權、未執行」；⑦提交依既有流程，**提交若不經 `/qa` Step 4.5，提交前先執行 `python .claude/scripts/gate_ledger.py check commit --ticket RGR-REPLAY-01`，exit 0 才提交**（退出碼處置依 `ssot-governance.md` §入口與覆蓋矩陣之映射）。
- [x] 完成 （見收尾實錄）

## Hard Constraints

1. 僅得替換 `REPAIR_ROUTING.md` L125 條件 4 該單行；不得改動條件 1–3、其前後行、`**Session-local closure loop.**` 段、`### Migration-Lite Family Closure`、`## Post-Signature Erratum Lane`、`### Dispatch Authority`、Guardrail 1–5，亦不得新增 heading、表格、清單層級或常駐 checklist。
2. 不得新增第二個 loop、ledger、欄位、token、severity 列、reason code、Gate、lifecycle 或 repair authority；不得改動 closure loop 的輪數（最多 3 次 iteration）與 Family Closure 計數語義。
3. 加測僅限「被修補宣稱仍支配的未被修改相鄰分支，或票面已點名但未被修改的 consumer」（Akira 2026-10-21 F1 裁決）；不得以被修補子句本身作為觸發原因，不得寫成對所有修補做 consumer sweep，不得要求追蹤未點名的 consumer。
4. 不得觸及 runner lane（Advisor Repair Mode、Guardrail 5），不得修改 `.claude/CLAUDE.md`（含 AC 修訂閉環）。
5. 不得修改 `SKILL.md`、`OPS_CALIBRATION.md`、`uc/SKILL.md`；A（RGR-DERIV-01）、B（RGR-EQUIV-01）、C（RGR-RERUN-01）之內容不得在本票重述。
6. 不得修改、重開或改寫 `TKA-BATCH-01`、`TKA-EVGATE-01`，亦不得建立常駐 benchmark、案例庫、ledger、第二 Gate 或 repo-wide tracing。
7. 不得宣稱已提升 Traycer 通過率、token 效率或品質指標；本票只交付條文，不交付行為驗證。

## Error Handling

- AI-0 任一錨點不符 → 停止回報，禁自行推定等價位置
- `git check-ignore -v` 命中 `REPAIR_ROUTING.md`（標的被 `.gitignore` 排除）→ 停止回報（gitignore 死結律）
- 他票 ALLOWED 與 `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md` 相交 → 停止回報（須串行）
- 無法僅憑條件 4 單行承載 plan T04 意圖（例如須改 closure loop 段、Guardrail 5 或 Erratum Lane 才能表達）→ 停止，請求裁決，不得擴大修改範圍
- 條文無法在不新增第二個 loop、ledger、欄位、token、severity 列、reason code、Gate、lifecycle、repair authority 下表達 → 停止回報
- 新條文與 closure loop（L133）、Family Closure 計數或四條件測試「任一條件不成立即停止並回報例外」之語意出現矛盾 → 停止回報
- 修改後 `git diff --stat` 觸及 ALLOWED 以外檔案，或 diff 超出 L125 單行 → 停止回報

## 走查（文字級；`[存在性]` 證據，非 `[行為性]` 驗證；given/expect）

下列為對條文的文字級對照，不是對模型行為的實測。情境為說明性假設，非真實事件；W1／W2 對應 plan §8 的 E3。

| # | given | expect（依 AI-1 條文） |
| --- | --- | --- |
| W1（E3 flawed） | 被審票聲明三條消費路徑（對焦、渲染、PTF）皆須滿足「不消費越界臉」，finding 指出對焦路徑缺此條件；修補只重寫對焦子句，渲染與 PTF 子句仍沿用舊不變量 | 重放原觸發情境（對焦子句缺條件）→ 通過；被修補宣稱仍支配未被修改的相鄰分支與票面已點名的 consumer（渲染、PTF）→ 加測一個最小判別案例（例如一張 `bbox.left < 0` 的臉）確認不變量，渲染與 PTF 子句未滿足 → 失敗，計為該 iteration 的失敗 |
| W2（E3 valid） | 同 W1，但修補同時讓三條消費路徑子句滿足同一不變量 | 原情境重放通過；三條子句都已被修改，宣稱下已無未被修改的相鄰分支或已點名 consumer → 不觸發判別案例，只做重放；結果寫進既有 final report，不新增欄位（plan §8 E3 valid 原寫「判別案例通過」，依 F1 裁決改為不觸發；valid 案例仍不產生 finding，與「不增加額外要求」一致） |
| W3 | 修補僅修正一處交叉引用的行號（宣稱下無其他相鄰分支或已點名 consumer） | 只做最低驗證（重放該檢查），不加測、不做任何 consumer 檢查 |
| W4 | 重放失敗，且這是本次 execution 的第三次 iteration | 依條文計為該 iteration 失敗；因輪數與計數語義不變，沿用既有 closure loop「第三次重跑仍非 PASS 即停止並回報」，不新增輪數 |
| W5 | finding 為命名不一致，沒有離散的「觸發情境」 | 觸發情境即產生該 finding 的檢查，重放＝對修補後文字重跑該檢查；不因無離散情境而判定條件 4 不成立 |
| W6 | 被修補宣稱仍支配一個票面未點名、但實際存在的 consumer | 不要求檢查（超出「已點名」範圍），不做全面 consumer sweep |
| W7 | 觸發條件成立，但被審票對該宣稱沒有寫明任何不變量 | 不自行創設不變量來做判別案例；改記一個 `Missing Contract Expansion` finding，交既有流程處置 |
| W8 | (a) 重放可行但失敗，而同一 iteration 的 gate 重跑為 PASS；(b) 原觸發情境無法重建，重放根本無法進行 | (a) 該 iteration 仍計失敗，不得報 PASS（PASS 只能來自重放通過且重跑 PASS 的 iteration）；(b) 條件 4「驗證方法存在且實際執行」不成立 → 依 `REPAIR_ROUTING.md:129` 報 `C_EXCEPTION`，不計為 iteration 失敗 |
| W9 | 修補只改了一條已點名 consumer 的子句，該宣稱沒有其他分支或已點名 consumer | 被修補子句本身已由原情境重放覆蓋，不因其為已點名 consumer 而觸發判別案例（F1 裁決，避免同一情境以不同名稱重複驗證）；只做重放 |

## Acceptance Criteria

> **diff 檢核範圍**：本節所有 `git diff` 檢核之範圍＝`<DIFF_REF>..<DIFF_END>`；提交前為 `<DIFF_REF>` 對工作樹並以 `-- .claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md` 限定，FORBIDDEN 檢核以各檔路徑限定；同 worktree 他票 dirty 檔，及範圍內他票提交（commit 前綴非 `[RGR-REPLAY-01]`，以 commit 前綴歸屬）觸及之檔案，皆不計入本票。
>
> **AC 修訂閉環（Rev A.1）**：本節首範圍說明、第 4 條（W1–W6→W1–W8）與最後一條（排除他票提交）經修訂；共用同一 diff 範圍母體之兄弟 AC（第 3、6 條）已重驗：二者同樣只因他票提交而受影響，修訂後排除規則一致套用，判準不變；第 1、2、5 條不涉 diff 範圍，不受影響。無 AC 被移除或豁免。
>
> **AC 修訂閉環（Rev A.2）**：第 1 條片語依 Akira F1 裁決更換為 `Only when the repaired claim still governs an adjacent branch that was not changed, or a consumer already named by the ticket that was not changed`；第 4 條隨 W9 同步為 W1–W9。共用同一行之兄弟 AC 已重驗：第 2 條片語不在換字段內，仍各 1 次；第 3 條單行 1 刪 1 增與禁字判準不變（新片語不含 `Medium`、`verdict`、`token`、`REASON_CODES`）；第 5–6 條及最後一條不受影響。無 AC 被移除或豁免。
>
> **同行計數**：下列各片語之 `grep -cF` 一律以「以 `4. A proportionate verification method exists and is actually run before the fix is reported done.` 開頭的那一行」為範圍。

- [x] **[存在性]** `REPAIR_ROUTING.md` 恰 1 行以 `4. A proportionate verification method exists and is actually run before the fix is reported done.` 開頭，且該行內含 `At minimum it replays the finding's original triggering scenario against the repaired result`、`Only when the repaired claim still governs an adjacent branch that was not changed, or a consumer already named by the ticket that was not changed`、`one minimal discriminating case that the recorded invariant still holds`（各 1 次）
- [x] **[存在性]** 同一行含 `reported in the existing final report with no new field`、`a failed replay or required discriminating case counts as a failure of that iteration under the session-local closure loop` 與 `iteration count and counting semantics are unchanged`（各 1 次）
- [x] **[存在性]** `git diff` 僅 L125 該單行 1 行刪、1 行增；新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`，且無新增 heading、表格、清單層級、空行、reason code、Gate 或 lifecycle 字樣；`**Session-local closure loop.**`、`## Post-Signature Erratum Lane`、`### Dispatch Authority`、`### Migration-Lite Family Closure`、Guardrail 5 行各仍恰 1 次
- [x] **[存在性]** 收尾實錄含「走查」W1–W9 逐條對照（附 `檔案:行號`）、「模型遵循度未驗」限制，以及「隔離成對評測未授權、未執行」，且未宣稱 `[行為性]` 驗證或任何品質指標提升
- [x] **[存在性]** `/mdlint` 對 `REPAIR_ROUTING.md` 的 warning 數不高於 AI-0 所記 baseline
- [x] **[存在性]** `FORBIDDEN` 所列各檔 `git diff` 為空，且 `uc/SKILL.md:82` 所引 Erratum Lane 四項條目字面不變
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉及 ALLOWED 檔案（範圍見本節首；他票 dirty 檔與他票提交觸及之檔案不計）

## 已知限制（非阻擋，如實揭露）

1. **模型遵循度未驗；分階段驗收**：全部 AC 為 `[存在性]`。Template 的「Guard／gate／early-return → 至少含 `[可達性]`＋`[行為性]`」規則（Template v3.11 L163）之 `[可達性]` 定義以 `app/src/main/` production producer 為語境（L159）；本票的修補路由為 skill 指令文字，無 production producer，故採分階段驗收：①票內驗條文、引用、靜態一致性與 W1–W9 文字走查；②真實行為依後續真實互動修補的 final report 是否載明原情境重放（及條件成立時的判別案例），由 REVIEW→CLOSED 審查方確認。未觸發或證據不足時仍為未驗，不回補更高等級驗證、不宣稱已驗。
2. 動機證據為少數案例（RGR 對 TKA-BATCH-01 的審查歷程；K3-Rule 25 的 Ghost Face 品類覆蓋為專案層類比，非 RGR 規則），「現行文字不要求」不等於「修補從不重放原情境」；缺口判斷屬現行文字比對，不構成對本條文有效性的證明。
3. **隔離成對評測未授權、未執行**（Akira 2026-10-21 裁決）；plan §8 的 E3 預期僅作預先寫定的判準，未實測。誤擋率、審查成本、token 與時間資料皆無（UNKNOWN）。
4. 重放失敗計為 closure loop 的 iteration 失敗，但不產生 gate 重跑的 finding，故 Family Closure 的「第二次出現」計數不隨之增加（承重推導反證紀錄第 1 點）；兩個計數本就獨立，但報告中可能出現「iteration 已耗一次、family 出現未增」的情形。
5. 以刪除被指出子句達成的修補會使重放形式上通過（承重推導反證紀錄第 2 點 (b)）；是否屬縮減 scope 由四條件測試條件 2 與 `.claude/CLAUDE.md` AC 修訂閉環另管，本票不處理。
6. 加測限於「票面已點名」的 consumer；被修補宣稱支配的未點名 consumer 不會被條文要求檢查，這是收窄文字與 F1 裁決排除全面 consumer sweep 的刻意取捨。
7. 本票條文僅涵蓋互動 lane（General Autonomous Repair Default）；runner lane 的驗證仍由 Guardrail 5 管，本票不動。
8. **被修補子句本身不觸發判別案例**（Akira 2026-10-21 F1 裁決）：條件 4 對被修補子句只做原情境重放；修補若讓原情境通過、卻在同一子句的其他輸入上破壞不變量，條件 4 不會攔下（承重推導反證紀錄第 4 點）。承接者：closure loop 每輪的 gate 重跑（`REPAIR_ROUTING.md` Session-local closure loop）。

## 收尾實錄（執行端；2026-10-21；Mode: normal）

### 1. 基線與實作

- 開工 HEAD `6c4972e5`（`DIFF_REF` 回填）；`REPAIR_ROUTING.md` blob 仍為 `95109c84`，`c8385e64..HEAD` 無觸及該檔之提交（無漂移）；其間提交屬 TKA-CLOSE-01，與本票標的無關。
- AI-0 ①–⑩ live 驗證：①②③④ 各恰 1，⑤ `minimal discriminating case` 修改前 0，⑥ check-ignore exit 1，⑦ Pending/ 無他票 ALLOWED 觸及，⑨ 無漂移，⑩ `uc/SKILL.md:82` Erratum Lane 四項字面仍在。⑧ 見 §2。
- AI-1 已落地並提交 `4882481e`：`REPAIR_ROUTING.md:125` 單行替換為 Rev A.2 目標值；`git diff` ＝ 1 file、+1／−1；`DIFF_END=4882481e`。
- 同 worktree 他票 dirty 檔 `plans/roadmap/projman-outcomes.md` 不計入，未納入提交。

### 2. mdlint

未跑 `/mdlint` 掃描（會自動修復，恐產生不屬本票 diff）。替代論證：僅替換一行內文，未增刪 heading、fence、表格、空行或清單層級，MD022/025/026/029/032/036/040/055/060 皆與結構相關，不可能新增。此為論證而非掃描結果。

### 3. 檢核結果

- 目標行 AC1／AC2 六片語於該行各 1 次；新增行不含 `Medium`、`verdict`、`token`、`REASON_CODES`；`**Session-local closure loop.**`、`## Post-Signature Erratum Lane`、`### Dispatch Authority`、`### Migration-Lite Family Closure`、Guardrail 5 行各仍 1 次；FORBIDDEN 各檔 diff 為空。
- 走查 W1–W9（對照 `REPAIR_ROUTING.md:125` 條文，W4 另指 `:133`、W8(b) 另指 `:129`）：W1 觸發判別案例並失敗計 iteration 失敗；W2 三子句皆已修改故不觸發；W3 只重放；W4 沿用 closure loop 第三次即停；W5 觸發情境＝產生 finding 之檢查；W6 未點名 consumer 不查；W7 無記錄不變量則記 `Missing Contract Expansion`（出處見票面語義對應）；W8 (a) 重放失敗仍計失敗、(b) 無法重建走 `C_EXCEPTION`；W9 被修補的已點名 consumer 不觸發。皆為文字級對照。

### 4. 限制與如實揭露

- 模型遵循度未驗（已知限制①分階段驗收）；隔離成對評測未授權、未執行；未宣稱 Traycer 通過率、token 效率或品質提升。
- 風險分級（CLAUDE.md SOP，純 `.md`、可 `git revert`）：省略 `/qa`、`/sv`、Cross-Review；`reviews` 依此填 `N_A` 並於 checkpoint 揭露。提交前 `gate_ledger.py check commit` exit 0。
