# MEO-FIX-B — 執行者派工 Briefing(Executor Dispatch Briefing)

> ⛔ **派工暫緩令(Akira 2026-07-30)**:FIX-A Codex 實作 cross-review 🔴 後裁定序列化——本票暫緩開工,待 FIX-A Rev K 回修 + Codex 重審 PASS 後,由 Akira 明示解除方得派工。解除前執行者不得依本 Briefing 開工。(理由:兩票同觸 `SKILL.md`,同檔雙票禁並行。)

**日期**: 2026-07-30 ｜ **簽發**: CSO ｜ **配套票**: `Pending/MEO-FIX-B_PreToolUse_Branch_Order_Fix.md`(2026-07-28 簽發版)
**授權**: Akira 2026-07-29 核准派工。缺陷定性 SSOT=`MEO_PreToolUse_Diagnosis_Livelock_Residue`;裁決層 SSOT=`MEO_Escalation_SelfExciting_Loop_Governance`;證據層 SSOT=`MEO_Escalation_SelfExciting_Loop_Release_Evidence`(vault)。
**路由**: 開工檢核 → conf 原子再生 → `/uc` 實作 → `/qa` → `/sv` → Codex 實作 cross-review → Traycer `strict_verification`
**紀律**: 票面為 SSOT;本 Briefing 為導讀與取證規格補充,不替代票面。兩者衝突 → 停止回報。

---

## 一、開工檢核(第一動,四項全過方得續行)

1. **PENDING=false 活性檢核**:`grep -n '^MEO_ESCALATION_PENDING=' STATE.md` MUST 回 `false`。CSO 於 2026-07-30 已 probe(=false),但**該值不得沿用**——執行者 MUST 於開工當下 re-probe(指令前提活性覆核律)。回 `true` → 停止回報,禁自行 release(票面 Error Handling 第 1 條)。
2. **水位活性檢核**:re-probe `MEO_SAME_ERROR_CLASS_STREAK` 與 `MEO_WM_STREAK`,重算 delta=STREAK−WM_STREAK。CSO 2026-07-30 基線:STREAK=8、WM_STREAK=7、**delta=1**。若開工實測 delta≥2 已成立 → 停止回報。
3. **行號錨定核對**:票面所載 `meo_pretooluse_gate.sh` 之 L128 / L131-135 / L140-142 / L147 / L149-152 / L156-160 逐一 grep 核對現行檔。任一不符 → 停止回報(票面 Error Handling 第 4 條),禁自行重錨。(注意:STATE.md 行號會隨診斷寫入漂移,票面錨定僅及 gate 檔,STATE.md 鍵一律以 grep 取現行行號。)
4. **conf 原子再生**:自票面 Verify Config **整塊取值**重建,`DIFF_REF` 以開工當下 `git rev-parse --short HEAD` 回填,禁逐鍵挑選(FIX-A Rev J 殘值覆蓋教訓,同族缺陷禁復現)。

## 二、熔斷紀律(本票最高風險項;2026-07-30 基線修訂)

開工基線:`STREAK=8`、`WM_STREAK=7`、**delta=1,觸發公式 delta≥2 即開 Episode ID=4——本票 deny/非零退出預算為零筆**:

- **首筆**非預期 deny 或非零退出 → **立即停止回報**,禁任何補救動作(補救動作本身即自激燃料,Episode ID=3 實證)。原「第 2 筆前停止」條款作廢。
- **禁逐次 release-and-retry**(自激迴路已裁,`meo/adjudications/3.md`);新 episode 開啟 → 停止回報,等外部裁決。
- **A 類燃料源斷絕**:任何 `git add` 前 MUST 以 `git check-ignore -v <path>` 判定排除狀態,禁憑記憶推定。已證排除域:`meo/adjudications/*`、`plans/cso_briefing/reports/**`(`.gitignore:71`)。BF 遞延至 T3 AI-5 後維持不變。(注:`plans/cso_briefing/project/TICKET_REGISTRY.json` 為 tracked,不在禁列——d4ace1b/4c727b9 先例。)
- **B 類燃料源斷絕**:高風險呼叫前 MUST 先完成 STATE.md 診斷寫入(bootstrap 豁免通道,PENDING=false 下 allow);禁裸奔 high-risk Bash。
- 禁清零 `STREAK`、禁動閾值、禁以任何 payload 寫入 `MEO_ESCALATION_*`/`MEO_WM_*` 鍵(HC3)。

## 三、Q1–Q4 四象限取證規格(AC [行為性] 第 1 項之執行細則)

**受測體**:未修改與已修改之 `meo_pretooluse_gate.sh` 本體(production hook 路徑)。**驅動方式**:`PROJECT_ROOT` 指向 scratch 目錄(內置真實 schema 之 STATE.md 複本,PENDING 值按象限設定),stdin 餵入真實 hook JSON(`tool_name`+`tool_input` 完整結構)。**禁 fixture 直注 emit、禁為測試修改 gate 檔、禁對 live STATE.md 寫入 PENDING=true**(HC3 間接路徑禁令;scratch 複本寫入不在此禁)。

| 象限 | scratch STATE.md | 驅動 payload | 預期 | 修復前對照 |
|---|---|---|---|---|
| Q1 | PENDING=false | Edit STATE.md,診斷內容(無控制鍵) | allow | allow(回歸保護) |
| Q2 | PENDING=true | Edit STATE.md,診斷內容(無控制鍵) | **allow** | deny(**本票核心修復,MUST 附修復前 deny 實錄**) |
| Q3 | PENDING=true | Edit STATE.md,payload 含 `MEO_ESCALATION_PENDING=` | deny | deny |
| Q4 | PENDING=true | Write `meo/adjudications/x.md`(scratch) | deny(HC2) | deny |

**逐象限證據欄(四項缺一即該象限不成立)**:① 完整 stdin JSON;② hook stdout 之 `hookSpecificOutput.permissionDecision` 與 message 字面;③ exit code;④ 命中分支行號(修復後檔)。Q2 另附修復前後對照雙實錄。

**release 消費用例**:scratch 佈署裁決檔+sidecar 雙檔 → 單次呼叫內完成 release 且 stdout 無 `retry this call` 字樣;六鍵 before/after 快照與裁決檔逐鍵一致。**episode-2/3 回放**:以 `meo/adjudications/2.md`、`3.md` 為素材(唯讀),於 scratch 重演同一動作序列,證據=全序列 deny 計數為零、無新 episode 條件成立。

## 四、執行紅線(零觸碰清單)

1. FORBIDDEN 六項零觸碰:`meo_signal_update.sh`(信號域屬 MEO-T3 AI-5)、`gate.conf`、`.claude/settings.json`、`stop.sh`、`ticket_pattern.md`、`cross-review.md`。
2. HC2 反偽造防線:`meo/adjudications/*` 於 PENDING=true 之 deny **無任何例外分支**,AI-4 改動不得波及。
3. `MAX_LINES=120` 計帳域=ALLOWED 三檔;逼近上限時 STOP 回報,禁自行上調(閾值變更須 CSO 裁決)。
4. commit 前綴 `[MEO-FIX-B]`;禁混 `.kt`/`.xml`;`.claude/PROGRESS.md` 滾動屬 session 代謝,禁夾帶入本票 commit。
5. 票面驗收期禁修;發現票面缺陷 → 停止回報,禁就地修票續跑。

## 五、附帶事項(非 AC,順手項)

- **未歸因增量取證(開工報告義務)**:`STREAK` 於 2026-07-29/30 間發生 7→8 之未歸因 +1(發生窗口=CSO probe 後至前輪 Edit 前)。開工報告 MUST 附前輪 hook log/deny 記錄以完成逐筆歸因(E-5 紀律);**log 不可得時明文申報「不可得」即可**,禁以推測補位——屆時由 CSO 於 vault 證據檔補記懸置(Akira 2026-07-30 裁示 (a) 退 (b))。
- **STATE.md 診斷 token 殘值回填**:現值 `MEO_LAST_DIAGNOSIS_TS=pending-bash-fill-r4`(2026-07-30 probe @L2;r3 於前輪被換版為 r4,佔位符仍未回填)。開工診斷寫入時以 `bash date -u` 取**真實時戳**一併回填,**禁再寫入任何 `pending-bash-fill-r*` 佔位符**(vault 正本 TODO 7 併入條款)。此為本票 ALLOWED 含 STATE.md 之用途,**僅限診斷 token 行,禁及控制鍵**。

## 六、Purpose Statement

**本次派工目標**:完成 AI-1–AI-5 實作(雙類治理模型/pending gate 收窄/state 類字面閘/release 即放行/SKILL.md 同步),以 §三 規格產出 Q1–Q4 全象限證據與 release/回放雙用例,經 `/qa`→`/sv` 送 Codex 實作 cross-review。
**預期帶回**:(a) Q1–Q4 逐象限四項證據(Q2 含修復前後對照);(b) release 單呼叫消費實錄+六鍵快照;(c) episode-2/3 回放零 deny 實錄;(d) `assembleDebug` PASS + `git diff --stat` 限 ALLOWED;(e) SV 報告與 Codex cross-review 回執;(f) 診斷 token 真實時戳回填確認;(g) +1 未歸因增量之 hook log 取證結果(或「不可得」申報)。
