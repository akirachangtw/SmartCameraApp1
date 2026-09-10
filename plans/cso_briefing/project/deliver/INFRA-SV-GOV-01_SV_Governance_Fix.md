# INFRA-SV-GOV-01: SV 治理修正 Rev B（EXCLUDE_ARGS rename 失配＋輪次二元帳明文化＋回件未勾項盤點）

**簽發日期**: 2026-07-21（Rev A 草案）｜ **Rev B**: 2026-09-04（SMTR 依 Akira「甲 拆二」裁決重寫）
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（`.claude/` 工具鏈；APP 全程序但本票無 `.kt` 變更）

## Rev B 修訂摘要（Akira 2026-09-04 裁決「甲 拆二」）

Rev A 草案自 2026-07-21 起草後未派工，期間前提已變。本次重寫四項：

1. **AI-2（structural_verify Check A↔C 規格互斥）剝離出票**。該項自身之 Error Handling 即載明「互斥根因屬需 CSO 裁決之規格衝突 → 停止回報」，等同承認一開工即可能停擺；依 Decision-Fork 紀律，規格衝突 MUST 先以**唯讀分析**產出三案再裁，不得與可直接施作項綁定使整票懸置。剝離後另行辦理（唯讀分析任務，非本票）。
2. **MEO 條款之理由更正**。Rev A 以「MEO-T2 已收案資產」為由禁動 `MEO_VERIFIER_REJECT_COUNT` 接線，並列「與 MEO-T3 並行」為前置。**MEO v1 強制層已退役**，該接線現為**退役殘留**而非活資產；禁動之結論不變，但理由改為「退役處置屬 `rules/decommission.md` 四層退場流程，非本票射程」。`MEO-T3`／`MEO-FIX-A`／`MEO-FIX-B` 仍滯留 `Pending/`，其處置屬 `INFRA-PENDING-HYGIENE-APP-01` 射程。
3. **「開工時定位」條款解消**。Rev A 之 ALLOWED 含兩處「開工時定位」佔位；`structural_verify.sh` 實路徑已由 `cross_review.sh` L176 之 `_SV_SCRIPT` 常數確定，回件模板落點亦已定位於 `sv/SKILL.md`。ALLOWED 全面實路徑化。
4. **骨架升級至 TPL v3.8**（補 `BASE_COMMIT`／`TARGET_BLOB`／`ANCHOR_PROBED`、依賴三分、AI-0 錨點復驗、`git check-ignore` 標配、`assembleDebug` N/A 顯式劃記、`[可達性]` AC、`MAX_LINES` 推導註記）。Rev A 之 `Mode: normal → uc` 語義不明，定為 `uc`。

**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: R15-a 事件（跨票 rename 需獨立 commit 分離規避）；R14–R16 判例（vault ＝ 邏輯帳 SSOT）；R17 教訓 #10（回件無未勾項盤點致 F1/F2）
- `coordination_dependency`: **`INFRA-QA-PERTICKET-CONF-01`（APP 冊 seq 23，PENDING）——語義耦合，非 ALLOWED 交集**。`cross_review.sh` 於 EXCLUDE_ARGS 計算中**直讀 `tmp/verify_cmd.conf` 之 `ALLOWED=` 行**；該票將 config 遷移至 `tmp/verify/<TICKET_ID>.<revision>.conf` 並保留舊檔為 fallback。**兩票順序不拘**，但 AI-1 之修正 **MUST NOT** 硬編碼 `tmp/verify_cmd.conf` 為唯一來源，MUST 保留其為可替換之讀取點（見 AI-1 紀律 3）

## 檔案治理／來源說明

**SSOT**：`.claude/skills/sv/scripts/cross_review.sh`（23,774B）與 `.claude/skills/sv/SKILL.md`。

**AI-1 缺陷機制（2026-09-04 live 讀碼取證，`aud_20260904_ce508613f0`／`c03f06215b`）**——Rev A 僅寫「rename 場景下 pathspec 失配」，本次補完整因果鏈：

`cross_review.sh` 之 payload 範圍計算流程為：①自 `tmp/verify_cmd.conf` 讀 `ALLOWED=`；②對每個 token 執行 `git ls-files -- "$_tok"`，無命中時退化為 basename 比對（`awk -v t="$_tok" -F/ '{if ($NF==t) print}'`）；③`_mcount -eq 1` 時，以 `[[ "$_matches" == "${_gpath}"* ]]` 判定該 token 涵蓋 `.claude/`／`docs/`／`plans/` 之何者；④**未被涵蓋之 gpath 才加入 `EXCLUDE_ARGS` 之 `:!gpath`**；⑤`git diff "$DIFF_SCOPE" -- "${EXCLUDE_ARGS[@]}"`。

**失效點在②③之間**：`git ls-files` 查的是**索引**。rename 場景下 ALLOWED 若記新路徑而 rename 尚未進索引、或記舊路徑而該路徑已於索引中消失 → `_matches` 為空 → `_mcount=0` → 落入 else 分支，該分支對三個 gpath **全部** `_COVERED_GPATHS+=` 並印三次警示 → `EXCLUDE_ARGS` 僅剩 `':!sv_report.md'` → **排除機制整體塌陷，三個治理目錄全量進 payload**。

故本缺陷之實際形狀**不是**「排除範圍過寬」而是「排除範圍歸零」——else 分支之註解自述為「保守判定為涵蓋」，該保守方向對 payload 體積而言是 fail-open。R15-a 之「需獨立 commit 分離規避」正是此塌陷的人工繞道。

**AI-3／AI-4 缺口（2026-09-04 live grep，`aud_20260904_1380adaa6f`）**：`sv/SKILL.md` 全檔對 `輪次`／`未勾`／`回件`／`模板` 字面**零命中**；`Round` 僅一處（L158，Fail Budget 狀態檔記錄）。二元帳規則與未勾項盤點條款均確實不在盤，缺口成立。

## Verify Config
TICKET_ID=INFRA-SV-GOV-01
CREATED=2026-07-21
PRESET=feature
ALLOWED=.claude/skills/sv/scripts/cross_review.sh,.claude/skills/sv/SKILL.md
FORBIDDEN=.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/qa/SKILL.md,.claude/CLAUDE.md,.claude/rules/**,.claude/settings.json,STATE.md,stop_orchestrator.sh,app/**,plans/**
MAX_LINES=60
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
BASE_COMMIT=<同上>
TARGET_BLOB=<開工時逐標的 `git hash-object` 回填，格式 path:sha8，分號分隔>
ANCHOR_PROBED=2026-09-04（SMTR live 讀碼：`cross_review.sh` EXCLUDE_ARGS 區塊與 `_SV_SCRIPT` 常數在盤；`sv/SKILL.md` 三缺口字面零命中。**派工時 MUST 重跑 AI-0 並更新本欄時戳**）

**MAX_LINES 推導註記**：AI-1 rename 解析分支 ~12 行；AI-3 二元帳條文 ~10 行；AI-4 未勾項盤點區塊 ~18 行 ≈ 40 → ×1.4 → 56 → 進位 **60**（估算校準遙測，非判準）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案 `cross_review.sh`**：①`EXCLUDE_ARGS=(':!sv_report.md')` 唯一命中；②`_matches=$(git ls-files -- "$_tok" 2>/dev/null)` 在盤；③`保守判定為涵蓋` 警示字面在盤（else 分支入口）；④`git diff "$DIFF_SCOPE" -- "${EXCLUDE_ARGS[@]}"` 在盤；⑤`_SV_SCRIPT=".claude/skills/qa/scripts/structural_verify.sh"` 在盤（僅確認，本票不動該檔）
- **檔案 `sv/SKILL.md`**：`輪次`／`未勾`／`回件` 字面命中 **0**（負向錨點，證缺口仍在；非零表示他票已先行 → 停止回報）
- **MEO 殘留確認（僅記錄不動）**：`MEO_VERIFIER_REJECT_COUNT` 字面在盤，記錄其行號存收尾（HC-1 之正向取證）
- 任一不符 → 依 TPL v3.8 基線漂移三分流分類回報，禁單一 STOP
- **[標配]** `git check-ignore -v` 對 ALLOWED 兩檔 → 命中即停止回報
- [x] 完成（2026-09-11，五項錨點全數符合，`git check-ignore -v` 兩檔均無命中，`MEO_VERIFIER_REJECT_COUNT` 落於 `cross_review.sh:444`）

### AI-1: EXCLUDE_ARGS rename 解析修正
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`
- **現值**: ALLOWED token 於索引查無時（`_mcount=0`），else 分支將 `.claude/`／`docs/`／`plans/` 三者全標為 covered，導致 `EXCLUDE_ARGS` 塌陷為僅 `:!sv_report.md`
- **目標值**: 於落入 else 分支**之前**，增設 rename 感知之解析層——以 `DIFF_SCOPE` 範圍內之變更檔清單（含 rename 後之 post-image 路徑）作為第三順位解析來源；仍無命中才進 else 分支
- **紀律**：
  1. **MUST NOT** 刪除或弱化既有 else 分支（`_mcount=0` 且新解析層亦無命中時，保守涵蓋行為與其三次警示**原樣保留**）——本 AI 只是讓「真的可解析」的 rename 案例不再走到那裡
  2. **MUST NOT** 放寬既有排除範圍：`_mcount -eq 1` 之既有判定路徑逐字不動
  3. **MUST NOT** 硬編碼 `tmp/verify_cmd.conf` 為唯一 config 來源（`INFRA-QA-PERTICKET-CONF-01` 將遷移落點）；若新增讀取邏輯，MUST 沿用既有 `CONF_ALLOWED` 變數而非另起讀檔
  4. 新解析層 **MUST** 對 `git` 呼叫失敗 fail-safe：任何非零退出即視為無命中並繼續既有流程，**MUST NOT** 使 `cross_review.sh` 因此中斷
- [x] 完成（2026-09-11，commit `07c77a2`；行為性驗證見 sv_report.md Stage B「Integration Test 存在閘」隔離 rename repro）

### AI-3: 輪次二元帳語意明文化
- **檔案**: `.claude/skills/sv/SKILL.md`，`## 階段 D：Cross-Review 路由` 節內、Fail Budget 條目（L158 附近）之後
- **現值**: 機械輪次／邏輯輪次二元帳為 R14–R16 判例（vault ＝ 邏輯帳 SSOT），SKILL.md 全檔零字面
- **目標值**: 增列條文，MUST 含三要素：①二帳定義——機械輪次＝`.tmp/cross_review_state/<TICKET_ID>` 之 `CURRENT_ROUND`（腳本自增，producer 明確）；邏輯輪次＝人工判例帳，SSOT 在 vault；②**回件時 MUST 同時標示兩帳號碼**，格式如 `R{機械}/L{邏輯}`；③二帳不一致時**不自動對齊**，以 vault 邏輯帳為準並於回件註明差異
- **紀律**: **MUST NOT** 在腳本中新增邏輯帳之自動計算或寫入（邏輯帳為人工判例產物，自動化即偽造 producer）；本 AI 為純文件動作，不動 `cross_review.sh`
- [x] 完成（2026-09-11，`sv/SKILL.md` L170-174，`邏輯` 字面於 `cross_review.sh` 命中=0）

### AI-4: 回件模板增列「未勾項全盤點」強制區塊
- **檔案**: `.claude/skills/sv/SKILL.md`，`## 回報格式` 節（L199 起）
- **現值**: 回件無未勾項強制盤點，R17 之 F1/F2（可達性 AC 與 AI-2 漏列於收案語句）即由此漏出
- **目標值**: 於回報格式增列強制區塊，MUST **AC 與 Action Item 雙軌逐項列舉**，每項標示去向三選一：`轉票<票號>`／`豁免<理由字面>`／`裁決中<待裁者>`。**MUST 明載：全部已勾時仍須輸出本區塊並寫「無未勾項」**——空區塊之缺席與「忘了寫」無法區辨，正是 R17 之漏出形狀
- **紀律**: 去向枚舉為封閉三值，**MUST NOT** 增設「其他」或自由文字選項
- [x] 完成（2026-09-11，`sv/SKILL.md` L238-256，含空區塊「無未勾項」要求）

### AI-5: 修訂波及面全檔掃描
- `sv/SKILL.md` 全檔 grep `輪次`／`未勾`／`Round`，確認 AI-3／AI-4 新條文與既有 L158 Fail Budget 敘述無矛盾；`cross_review.sh` 全檔 grep `EXCLUDE_ARGS`／`_COVERED_GPATHS`，確認 AI-1 未在他處留下未同步之同主張
- [x] 完成（2026-09-11，零矛盾，見 sv_report.md「Stage B」AI-5 記錄）

## Hard Constraints
1. **禁動 `MEO_VERIFIER_REJECT_COUNT` 接線**（`cross_review.sh` BLOCKER 分支內）。理由（Rev B 更正）：該接線為 **MEO v1 退役殘留**，其處置屬 `rules/decommission.md` 四層退場流程，非本票射程；**MUST NOT** 以「順手清掉」為由刪除
2. **禁動 `structural_verify.sh`**（Check A↔C 互斥已剝離出票，見 Rev B 修訂摘要第 1 項）
3. AI-1 禁刪既有 else 保守分支、禁放寬 `_mcount -eq 1` 之既有判定
4. AI-3 禁在腳本中自動化邏輯輪次
5. 禁動 `STATE.md`、`stop_orchestrator.sh`、`.claude/rules/**`
6. 完成後 STOP，等待 SMTR 複審，禁自行擴 scope

## Error Handling
- AI-0 任一錨點不符 → 三分流分類回報
- rename 場景無法真實復現 → **停止回報，禁以 fixture 直注充當行為性證據**（Rev A 原條款保留）
- AI-1 修正後既有非 rename 案例之 payload 範圍發生變化 → 停止回報（表示誤改既有判定路徑，違紀律 2）
- 發現 Check A↔C 互斥影響本票任一 AI 之驗收 → 停止回報升裁（該議題已剝離，本票不得順手處理）
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[行為性]** 真實 rename commit 場景（非 fixture）下，ALLOWED token 可被解析且 `EXCLUDE_ARGS` 未塌陷——收尾 MUST 附 `EXCLUDE_ARGS` 實際內容與 `保守判定為涵蓋` 警示**未出現**之輸出（見 sv_report.md Stage B：隔離 git repo `/tmp/rename_test_repo2` 真實 `git mv` rename，`EXCLUDE_ARGS=':!sv_report.md :!docs/ :!plans/'`，`.claude/` 未被排除，警示未出現）
- [x] **[行為性]（回歸守門，與上項互為正反）** 非 rename 之既有案例：ALLOWED token 索引可解析時，`EXCLUDE_ARGS` 內容與修改前**逐字一致**（證紀律 2 未違）；ALLOWED token 真正無法解析時，else 保守分支**仍照常觸發**並印三次警示（證紀律 1 未違）——同一 harness 對照驗證，見 sv_report.md
- [x] **[存在性]** `sv/SKILL.md` 含機械／邏輯二元帳定義與 `R{機械}/L{邏輯}` 格式字面；`cross_review.sh` 內無邏輯帳自動計算（`邏輯` 字面於腳本命中＝0）（`sv/SKILL.md:170-174`）
- [x] **[存在性]** `## 回報格式` 含未勾項全盤點區塊，AC/AI 雙軌與封閉三值去向枚舉字面俱全，且含「無未勾項」之空區塊要求（`sv/SKILL.md:238-256`）
- [x] **[可達性]** 機械輪次之 producer 可指認：`cross_review.sh` 之 `CURRENT_ROUND` 寫入行（既有機制，本票未新增/未修改）＋狀態檔路徑 `.tmp/cross_review_state/<TICKET_ID>`
- [x] **[存在性]** `MEO_VERIFIER_REJECT_COUNT` 字面仍在盤（HC-1 正向取證，禁誤刪；`cross_review.sh:444`）
- [x] **[行為性]** AI-5 全檔掃描完成並回報零矛盾
- [x] ~~`assembleDebug` PASS~~（N/A 顯式劃記：`.claude/` 工具鏈腳本與文件，零 `.kt`／`.xml` 變更；本次仍如常執行並 PASS(15s) 作為附帶佐證）
- [x] `git diff --stat` 僅涉及 ALLOWED 兩檔（commit `07c77a2`；`.claude/PROGRESS.md` 另為 Pipeline Artifact 同步，`c5024e9`）

## 收尾實錄（2026-09-11）

- **碼閉環**：AI-0~AI-5 全數完成，commit `07c77a2`（實作二檔）+ `c5024e9`（PROGRESS.md Done 標記，`[COMPACT-SYNC]`）
- **`/qa`**：Build ✅ PASS(15s) / Tests ✅ PASS（device+emulator flavors）/ Lint ✅ 0 warnings / Step 3.5 ⚠️ 5 項既有域外 `.kt` 警示（非本票觸碰範圍）
- **`/sv`**：Stage A/A.5/B/C 全 PASS，Cross-Review **ADVISORY**（R1/L1，DIFF_SCOPE=`a1169fa..07c77a2`，REACHABILITY: NONE）；`structural_verify.sh --post` **PASS (A/B/C/D/E/F/G) [ADVISORY]**
- **Traycer**：票面 Header 已明文 `N/A`（`.claude/` 工具鏈，本票無 `.kt` 變更），依票面聲明豁免
- **reviews**：`{qa:PASS, sv:PASS, codex:ADVISORY, traycer:N_A}`

---

**End of INFRA-SV-GOV-01 (Rev B)**
