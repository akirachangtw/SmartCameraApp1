# MEO-DECOMM-01: MEO v1 執法層前提消滅型退役(Step A 存證 → B 解武 → C 復通取證 → D 清理 → E 冊面收束)

**簽發日期**: 2026-08-01
**簽發者**: Claude(CSO/SMTR,Desktop)
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴**: vault `2026-08-01_MEO_V1_Enforcement_Layer_Decommission.md`(退役 SSOT)、`2026-08-01_MEO_Decommission_Execution_Path_Authorization.md`(執行序+授權 SSOT)

## 檔案治理/來源說明
本票依 Akira 雙授權(2026-08-01)開立:①settings.json MEO hooks 解武由 Desktop(CSO)經 mcp-ticket-platform 旁路執行;②凍結令對本票清理 commits 之 supersede(型態=前提消滅)。執行路徑=Desktop 旁路(不經 Claude Code PreToolUse hooks),因執行者端全 Bash 被 gate 癱瘓(第四死結=退役悖論)。

## Verify Config
TICKET_ID=MEO-DECOMM-01
CREATED=2026-08-01
PRESET=docs
ALLOWED=.claude/settings.json
FORBIDDEN=.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/hooks/telegram_ask.sh,.claude/hooks/snapshot_cueshot_memory.py
MAX_LINES=70
DIFF_REF=d5fe887
ANCHOR_PROBED=2026-08-01 15:38

**MAX_LINES 推導註記**(額度制截斷序律):settings.json 移除 5 個 MEO hook 註冊區塊(PreToolUse ~10 行、PostToolUse ~12 行、PostToolUseFailure ~12 行、Stop 第三項 ~6 行、SessionStart ~13 行)≈ 53 行 → 緩衝 1.3x ≈ 69 → 進位 70。**截斷序**:任一 patch 區塊 search 非唯一匹配即整批拒改停止回報;禁逐塊改寫繞行。Step A 存證副本與 Step B `.bak` 為新建檔,不計入 MAX_LINES(diff 範圍僅及 ALLOWED)。

## 錨點預檢紀錄(AI-0 依據,live 直取 2026-08-01 15:38)
- settings.json(4,598 B)內 MEO hook 註冊 **5 處**:PreToolUse L102(gate)、PostToolUse L124(signal)、PostToolUseFailure L136(signal)、Stop L155(stop_orchestrator)、SessionStart(session_reset)。
- ⚠️ **live 直查修正 Handoff 存證清單**:`meo_session_reset.sh` + SessionStart 註冊為 Handoff 快照清單未列項,經 live 直查納入本票 scope(仍屬授權①「MEO hooks」字面範圍;揭露於此,豁免停點不豁免可見性)。
- STATE.md K-7 re-probe:`MEO_ESCALATION_PENDING=true`(L447)/`MEO_ESCALATION_ID=4`(L494)/`MEO_SAME_ERROR_CLASS_STREAK=11`(L443)— 與 Handoff 凍存值一致;另 `MEO_WM_STREAK=7`(L496)/`MEO_FASTPATH=false`(L475)。
- 存證標的均實存:STATE.md(36,461 chars)、gate.conf(634 B)、meo_pretooluse_gate.sh(7,080)、meo_signal_update.sh(4,533)、meo_session_reset.sh(503)、stop_orchestrator.sh(repo 根)、meo/adjudications/(SCHEMA.md + 1–3 號 .md/.audit.md 七檔;.gitkeep 0 B 不存證)。
- HEAD=d5fe887(`[GOVERNANCE-SYNC] registry: INFRA-CLOSURE-REGISTRY-GATE-01 intake`)。

## Action Items

### AI-0: 錨點復驗(執行前)
- **檔案**: `.claude/settings.json`、`STATE.md`
- **動作**: 確認上列 5 處 MEO hook 註冊與 3 個 K-7 凍存鍵值仍與錨點預檢紀錄逐字相符;HEAD 未越過 `ANCHOR_PROBED` 時刻。
- **任一項不符 → 停止回報,禁自行推定等價位置。**
- [x] 完成(2026-08-01 CSO 重跑預檢通過;HEAD 位移裁決見 Addendum-1)

### AI-1【Step A 存證】: 退役前證據快照(record-before-clear)
- **執行者**: CSO(Desktop,mcp-ticket-platform)
- **動作**: 逐檔 copy 至 `D:/DevSources/Smarter-Project/deliver/MEO-DECOMM-01_evidence/`(保留原檔名;adjudications 七檔入 `adjudications/` 子目錄):STATE.md、gate.conf、settings.json(解武前態)、meo_pretooluse_gate.sh、meo_signal_update.sh、meo_session_reset.sh、stop_orchestrator.sh、meo/adjudications/{SCHEMA,1,1.audit,2,2.audit,3,3.audit}.md。
- **驗證**: 目的地 list + 逐檔 size 與來源一致。
- [x] 完成(2026-08-01 15:40 CSO/Desktop;MANIFEST.md 14 檔 sha256)

### AI-2【Step B 解武-1】: settings.json backup 前置
- **檔案**: `.claude/settings.json` → `.claude/settings.json.pre-decomm.bak`(create,禁 overwrite)
- [x] 完成(sha256 與 Step A 存證副本逐位一致)

### AI-3【Step B 解武-2】: 移除 5 處 MEO hook 註冊
- **檔案**: `.claude/settings.json`
- **現值**: PreToolUse `Write|Edit|MultiEdit|Bash`→gate 區塊、PostToolUse 全鍵、PostToolUseFailure 全鍵、Stop 陣列第三項(stop_orchestrator.sh)、SessionStart 全鍵(各 search 以 args 絕對路徑行保證唯一)
- **目標值**: 五區塊移除;保留 telegram_ask/cueshot snapshot(PreToolUse)、stop.sh/telegram_stop.sh(Stop);JSON 結構有效(逗號收斂)
- **方法**: `write_file_safe mode=patch`(all-or-nothing)
- [x] 完成(五區塊一次通過,4598→3125 bytes;Akira 以 0b321c2 收 commit,見 Addendum-2)

### AI-4【Step B 解武-3】: read-back + diff 揭露
- **動作**: `read_file_safe` 讀回全檔確認 JSON 有效且 `meo_|stop_orchestrator` grep 零命中;`git_diff` scoped `.claude/settings.json` 揭露,同批確認**零非預期異動**(同批差分律)。
- [x] 完成(grep 六關鍵字零命中;git_diff 純刪除)

### AI-5【Step C 復通取證】: 新 Claude Code session 執行 `git log -1`
- **執行者**: 新 Claude Code session(派工訊息由本票附件供給)
- **判準**: 命令成功回傳=Bash 復通**正向反例**(撤警正向取證律,禁以「未再觸發」為據)。失敗 → 停止回報,禁重試繞行。
- [x] 完成(新 Claude Code session `git log -1` 成功;CSO 旁證見 Addendum-3)

### AI-6【Step D 清理 commits】: hook 腳本歸檔 + STATE.md MEO 區段封存
- **執行者**: 執行者(Claude Code;授權②凍結令 supersede,限本票 commits)
- **動作**: commit 前綴 `[MEO-DECOMM-01]`;細目於 Step C PASS 後由 CSO 出具派工令(本票僅錨定授權範圍)。
- [x] 完成(efb3c3f 歸檔 16 檔至 archive/meo-v1/;STATE.md 單行封存;Commit #2 豁免與註記措辭偏差裁決見 Addendum-5)

### AI-7【Step E 冊面收束】: FIX-A/FIX-B registry 轉 CLOSED+STOPPED 註記 → STOPLOSS-01 解凍派工
- **執行者**: CSO(Desktop);`registry_set_status` 逐票,同批 `git_diff` scoped registry 驗證零非預期異動。
- [x] 完成(FIX-A/FIX-B → CLOSED+N_A+checkpoint STOPPED 語意;票檔收束註記已附;diff 僅二筆目標記錄零損毀;updated_at 未回寫屬平台行為,見 Addendum-5)

## Hard Constraints
1. 禁止修改/刪除任何非 MEO hooks(FORBIDDEN 四檔)與 permissions/statusLine/enabledPlugins/model 鍵。
2. Step A 完成前禁止任何 Step B 寫入(record-before-clear)。
3. Step B/D 之唯一合法依據=授權①②(vault 執行序 SSOT 字面記載);Akira 撤回授權即全票停止。
4. hook 腳本本體於 Step B 不動(僅解除註冊);腳本歸檔屬 Step D 執行者 commits。
5. 全記錄替換型寫入禁令適用:settings.json 僅 patch,禁 overwrite 重謄。

## Error Handling
- 任一 patch search 非唯一/未命中 → 整批拒改,停止回報
- copy 目的地已存在同名檔 → 停止回報(禁 overwrite 存證)
- read-back 發現 JSON 失效或非預期異動 → 以 `.bak` 判定回復方案前停止回報
- Step C `git log -1` 失敗 → 停止回報(禁推定 gate 殘留位置)
- MCP 寫入逾時 → 依 read-back 驗證規範,禁盲重試

## Acceptance Criteria
- [x] **[存在性]** `deliver/MEO-DECOMM-01_evidence/` 含 14 檔且 size 逐檔與來源一致(AI-1)(2026-08-01 覆核:目錄現存 18 檔=原 14 檔存證+4 份 Step C/D/E 派工令/MANIFEST 文件,原始 14 檔核心存證完整)
- [x] **[存在性]** `.claude/settings.json.pre-decomm.bak` 存在且與解武前態一致(AI-2)(2026-08-01 覆核:檔案存在)
- [x] **[存在性]** 解武後 settings.json grep `meo_pretooluse_gate|meo_signal_update|meo_session_reset|stop_orchestrator` 零命中(AI-4)(2026-08-01 覆核:`grep -c` = 0)
- [x] **[行為性]** 新 Claude Code session `git log -1` 成功(Bash 復通正向反例;AI-5,證據=session 輸出回填)(本 session 多次 `git log` 成功執行)
- [x] **[可達性]** 解武後 Stop/PreToolUse 存留 hooks(stop.sh/telegram_stop.sh/telegram_ask/cueshot snapshot)註冊行仍在檔(producer 證據欄:settings.json L91/L101/L112/L116)
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案(存證副本/`.bak` 於 repo 外或 untracked,不入 diff)— ⚠️ 經 Addendum-2 L2 現場修訂:`.bak` 併入 0b321c2(2026-08-01 覆核:`git diff d5fe887 --stat -- .claude/settings.json` 僅 1 檔)

---
## Addendum(2026-08-01 CSO 裁決紀錄,豁免停點不豁免可見性)

**Addendum-1(AI-0 HEAD 位移裁決)**:派工後 HEAD 由 d5fe887 前進至 e9a2930(3 commits)。CSO/Desktop 逐 commit 獨立取證:0b321c2=本票 Step B 落 commit(Akira 親簽,changed files=settings.json+.bak,內容與票面目標值逐字相符);eab534e=STOPLOSS-01 registry intake;e9a2930=PROGRESS.md 同步——後二者 changed files 均不觸碰任何錨點檔。依 TPL v3.2 AI-0 條文救濟路徑(越過 ANCHOR_PROBED 即重跑預檢),CSO 重跑:settings.json 解武後態 grep 零命中、STATE.md K-7 三鍵(L443/L447/L494)與凍存值一致 → **錨點復驗通過**。非字面豁免,係依票面既有救濟條款重跑。

**Addendum-2(.bak 入 commit,L2 現場修訂)**:票面原設 `.bak` 維持 untracked 不入 diff;Akira 於 0b321c2 親自將 `.bak` 併入 commit(訊息明載「退役前原檔快照,供回溯核驗」)。作者=Akira(L2),視為 AC 末條之簽發後 L2 現場修訂,據實記載;blob e340910 與解武前原檔一致,無證據力減損。

**Addendum-3(AI-5 採認依據)**:執行者回報之三 commit hash/訊息/changed files 經 Desktop `git_log`/`git_show`/`git_changed_files` 逐項比中(該等資訊未載於派工令,執行者僅能經 Bash git 取得)→ 構成執行者端 Bash 實際復通之獨立旁證,非單採執行者自證。

**Addendum-4(AI-1 執行者更正)**:執行者核驗表誤載 AI-1 未執行;AI-1 已於 2026-08-01 15:40 由 CSO/Desktop 完成(先於 Step B,record-before-clear 序未破)。

**Addendum-5(Step D/E 收束裁決,2026-08-01)**:
(a) **Commit #2 豁免**:STATE.md 經 `.gitignore:84` 排除版控(既定慣例),Commit #2 客觀不可能;磁碟層單行封存註記(+85 chars,鍵值零動,K-7 三鍵位移 +1 行一致)+ Step A 快照構成完整封存,CSO 裁認豁免 Commit #2。
(b) **註記措辭偏差**:執行者以 `# ARCHIVED 2026-08-01(...)` 取代派工令逐字 HTML 註解;語意等價(票號/日期/凍結/歸檔路徑俱全)且合於 STATE.md 自身 `#` 註解慣例,裁認通過。
(c) **efb3c3f scope 取證**:changed files=16 檔全數為 archive/meo-v1/ 下新位(含 skill 全目錄 SKILL.md/decision-tree/prompt-template 隨目錄整移,屬 D-2 目錄級 mv 預期範圍);原位四標的經 get_file_info 證實 NOT_FOUND;工作樹 clean。
(d) **Step E 平台行為揭露**:registry_set_status/set_checkpoint 未回寫 updated_at(仍 07-30/07-28);屬平台欄位行為,不手工矯正,循 seq 缺陷先例列平台改善項。
(e) **殘留待裁(非本票 scope)**:MEO-FIX-A-EVIDENCE-REQ(PENDING,註記「待裁」)、MEO-FIX-A-REVJ-BRIEF(PENDING,非獨立票)之冊面收束;MEO-DECOMM-01 自身 registry intake;registry dirty 之 [GOVERNANCE-SYNC] commit(Akira 手簽)。
