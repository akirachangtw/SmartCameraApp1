# INFRA-VERIFY-PA-BOUND-01: Pipeline Artifact 豁免有界化 + 雙閘一致化（根因修復）

**檔案治理／來源說明**：依 `MEO-T3-FIX-A`（`deliver/MEO-T3-FIX-A_Scope_Payload_Cleanup.md`）事件之根因分析、Akira 2026-07-22 對話裁決（要求對 scope-bundling 缺陷做根因修復而非再開 cleanup 票）與 live Git 證據起草；本票由 Opus 4.8 協助草擬。`MEO-T3`（`89f41af`）將不在該票 `ALLOWED` 內之 `.claude/PROGRESS.md`、`.claude/PROGRESS_ARCHIVE.md` 變更夾帶進實作 commit，並於 `sv_report.md` 自行標註為「Pipeline Artifact」規避 Scope Gate，最終須開 `MEO-T3-FIX-A` 清理。本票封堵三處根因，使同類 scope-bundling 不再可能。

**簽發日期**: 2026-07-22
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: execute_review
**前置依賴**: `MEO-T3-FIX-A`（`25b77f3` cleanup + `2c883cf` QA sync，已 Codex Cross-Review PASS）

## Verify Config

TICKET_ID=INFRA-VERIFY-PA-BOUND-01
CREATED=2026-07-22
REV=A.1
PRESET=bugfix
ALLOWED=.claude/skills/qa/scripts/verify_checklist.sh,.claude/rules/ssot-governance.md,.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/CLAUDE.md,CameraViewModel.kt,.claude/skills/sv/scripts/cross_review.sh,.claude/skills/qa/scripts/structural_verify.sh,plans/architecture/SSOT_v4_0_7.md
MAX_LINES=100
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

> **Rev A.1 重錨說明（2026-07-22 CSO 裁決）**：原 `DIFF_REF=2c883cf` 為起草時硬編，票在 Pending 停留期間 HEAD 前進至 `d17c62a`，觸發 Anchor Drift 停止條款。免開票唯讀 Git 取證（`git_changed_files 2c883cf..d17c62a` 僅 `.claude/PROGRESS.md` + `.claude/PROGRESS_ARCHIVE.md`；`git_status` 除 untracked `tools/.claude/settings.local.json` 外乾淨）確認**三個 ALLOWED 檔零觸及、無範圍內實作痕跡**，判定為安全重錨。根治方式為改採開工回填形式，**MUST NOT** 再硬編具體雜湊。
>
> ⚠️ **Rev bump 連動（K4-cand-20260721-01 前例）**：本次 Rev A→A.1 後，開工前 **MUST** 確認 `tmp/verify_cmd.conf` 已依新票面重新生成；票面 Rev bump 不會自動觸發 conf 再生，漏做將使 runtime 沿用舊 `DIFF_REF`／舊 ALLOWED 而誤判 Scope 汙染。

> **MAX_LINES 理由**：三檔為「新增小型 helper + 常數 + 閘門分支修改 + self-test 案例」（`verify_checklist.sh`）＋「一小段治理段落」（`ssot-governance.md`）＋「單行改寫」（`qa/SKILL.md`）。預估淨增 40–70 行，設 100 留合理緩衝，避免 `[0.3]` BLOAT 誤攔。
>
> **FORBIDDEN 理由**：`.claude/CLAUDE.md` 觸碰會觸發 `cross-review.md` 強制 Checkpoint，本票不動它；`CameraViewModel.kt` 沿用腳本 `DEFAULT_FORBIDDEN`；其餘三項為高風險同管線鄰檔（cross-review 腳本、結構校驗腳本、策略 SSOT）作防呆。**MUST NOT** 以 `.claude/rules/*.md` glob 表達「只動 ssot-governance.md」——該 glob 會命中本票 ALLOWED 的 `ssot-governance.md`、於 `[0.2]` 自我阻擋；ALLOWED 正向閘已保證未列檔案即 SCOPE VIOLATION。

## 根因摘要（三項發現）

| # | 層 | 檔案 | 缺陷 |
|---|----|------|------|
| 1 | 機械 | `verify_checklist.sh` L22 `PIPELINE_ARTIFACT` | **無條件**豁免清單：`[0.1]` 命中即 `continue`，完全獨立於票面 `ALLOWED`，任何票可無界改寫兩檔而不觸 Scope Gate |
| 2 | 治理 doc | `ssot-governance.md`「PROGRESS.md Dashboard 紀律」 | 只規範內容格式，**未定義** Pipeline Artifact 豁免的合法邊界；「sv_report.md 不得自行以 artifact 標籤擴張 ALLOWED」原則僅存於 `MEO-T3-FIX-A` 單票 Hard Constraint，未升為常設規則 |
| 3 | 雙閘不一致 | `verify_checklist.sh` L22 vs `qa/SKILL.md` L105 | Step 0 豁免兩檔（PROGRESS + ARCHIVE）；Step 4.5 substep 2 文字只豁免 `.claude/PROGRESS.md` 一檔。同管線兩份獨立維護、範圍不同的豁免清單本身即缺陷來源 |

> **因果歸屬（誠實揭露，防 overclaim）**：`MEO-T3`（`89f41af`）對兩檔的實際 per-file delta 為 PROGRESS.md 6+8、PROGRESS_ARCHIVE.md 10+0 行（正常 Step-4 同步規模，**非整檔覆寫**）。真正的越界向量是「小型但不在 `ALLOWED` 內」的變更，靠 sv_report.md 自貼「Pipeline Artifact」標籤過閘——此向量由**發現 2（治理層）機械捕捉**。**發現 1（行數界線）** 封堵的是無界豁免所**潛在允許**的大規模覆寫／回滾向量（deletion-heavy），屬 defense-in-depth；**行數界線不會**攔到 `89f41af` 的小 delta，此殘餘缺口由發現 2 治理層覆蓋（K3-Rule 25 partial-fix 揭露）。

## Action Items

### AI-1: `verify_checklist.sh` — Pipeline Artifact 豁免有界化 + self-test 案例

- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **現值**: `[0.1]`（約 L243–252）對 `PA_MATCH==1` 直接 `echo "📌 Pipeline Artifact"` + `continue`，無任何行數界線；豁免與票面 `ALLOWED` 完全解耦。
- **目標值**:
  1. 新增常數 `PIPELINE_ARTIFACT_MAX_LINES=40`（置於 L22 `PIPELINE_ARTIFACT` 旁）。**MUST** 為明確整數常數，禁「視情況」模糊界線。理由：實測近期合法 Step-4 同步 per-file delta 為 4–16 行（`.claude/PROGRESS.md` 6/14/16、`.claude/PROGRESS_ARCHIVE.md` 4/6/10），40 為最大合法同步之約 2.5× headroom margin（非「為攔 MEO-T3」）。
  2. 新增純函數 `pa_exemption_applies(delta_lines)`：`delta_lines <= PIPELINE_ARTIFACT_MAX_LINES` 回 0（豁免成立），否則回 1（豁免失效）。純算術、不觸 git，供 `self_test()` 直呼。
  3. 新增 per-file 變更行數計數（**MUST** 為 `added + deleted`，即 numstat `$1 + $2`，**非** `[0.3]` 的 added-only `$1`——回滾／整檔覆寫是 deletion-dominant，added-only 會低估而使界線被回滾向量繞過）；**MUST** 鏡像 `[0.3]` 之 `CONF_MODE` vs CLI-mode `git diff` 端點分支（conf 模式用 `${DIFF_REF}..${DIFF_END_RESOLVED}`，CLI 模式用 `$DIFF_REF`），使界線兩模式皆生效。
  4. `[0.1]` 判定 `PA_MATCH==1` 後、`continue` 前：計算該檔 delta，呼叫 `pa_exemption_applies`。豁免成立 → 維持現行 `📌 Pipeline Artifact` + `continue`。豁免失效（超界）→ **MUST fall through 至正常 ALLOWED 檢查**（喪失 auto-exemption，等同一般檔案處理）：該檔若在 `ALLOWED` 內則 PASS，不在則落 `SCOPE VIOLATION`。**MUST NOT** 直接硬判 SCOPE VIOLATION——否則合法大規模 ARCHIVE 輪替將無從授權；正解是需要大改的票把該檔明列進 `ALLOWED` 即可放行。
  5. `self_test()`（L57–86）新增案例：因既有 `_expect` 硬綁 `token_matches_file`，**MUST** 為 `pa_exemption_applies` 另建小型數值斷言 helper（或泛化 `_expect`）。至少四案：①小規模同步（如 delta=12）→ 豁免成立（回 0）；②模擬大規模覆寫（如 delta=200）→ 豁免失效（回 1）；③**邊界對案** delta=40（即 `PIPELINE_ARTIFACT_MAX_LINES` 本值）→ 豁免成立（回 0）；④**邊界對案** delta=41 → 豁免失效（回 1）。③④ **MUST** 存在：僅①②無法區分 `<=40` 與 `<40` 兩種實作，界線語義未被釘死即不算驗到 intent（Rule 9）。**MUST NOT** 更動任何既有 `_expect` 案例的期望值。
- [x] 完成（`--self-test` 14/14 PASS 含新增 4 案；Codex Cross-Review R1 PASS；commit `92653c1`）

### AI-2: `ssot-governance.md` — Pipeline Artifact 豁免合法邊界常設化

- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 「PROGRESS.md Dashboard 紀律」段僅規範內容格式（≤80 行、Session Notes 3-session 內歸檔等），未定義豁免邊界。
- **目標值**: 於該段後新增一小節（標題如 `## Pipeline Artifact 豁免邊界`），內容 **MUST** 涵蓋：
  1. Pipeline Artifact 豁免（`verify_checklist.sh` `PIPELINE_ARTIFACT` + `PIPELINE_ARTIFACT_MAX_LINES`）僅限 `/qa` Step 4 產生的**增量** Dashboard／ARCHIVE 同步；
  2. **任何服務於票面實質目的、不在 `ALLOWED` 內的內容變更（無論大小）MUST 明列進 `ALLOWED`**，不得倚賴 Pipeline Artifact 豁免夾帶（此為捕捉 `MEO-T3` 真實向量的條款，採 size-agnostic 措辭）；
  3. 任何 `sv_report.md` **MUST NOT** 以「Pipeline Artifact」標籤自行擴張 `ALLOWED` 範圍（升格 `MEO-T3-FIX-A` Hard Constraint 4 為常設規則）；
  4. 超過 `PIPELINE_ARTIFACT_MAX_LINES` 的合法大規模輪替 **MUST** 走明列 `ALLOWED` 路徑取得授權，不得靠豁免放行；且 `PROGRESS_ARCHIVE.md` 輪替（3-session 規則）到期時 **MUST** 延後至專用 docsync 票執行（前例 `MEO-T3-DOCSYNC-01`），**MUST NOT** 在無關票內就地輪替——輪替時點由 session 計數觸發、與票面意圖無關，就地執行將對無關票產生假陽性 SCOPE VIOLATION 且無授權路徑。
  依 K5-Rule 29 精神：本票 doc 宣告（AI-2）與機械落地（AI-1）成對交付，不可只改一邊。
- [x] 完成（`ssot-governance.md` L21 新增「Pipeline Artifact 豁免邊界」節，grep 確認 AC5 兩條款命中）

### AI-3: `qa/SKILL.md` Step 4.5 substep 2 — 雙閘豁免範圍統一

- **檔案**: `.claude/skills/qa/SKILL.md`
- **現值**: L105「Scope 汙染確認」：`git status --short`，確認無非 ALLOWED 且非 `.claude/PROGRESS.md` 的 dirty 檔案——只豁免一檔，不含 `.claude/PROGRESS_ARCHIVE.md`，與 `verify_checklist.sh` L22 兩檔範圍不一致。
- **目標值**: 改寫該行豁免範圍，使之與 `verify_checklist.sh` 的 `PIPELINE_ARTIFACT` 常數範圍一致（兩檔 `.claude/PROGRESS.md` + `.claude/PROGRESS_ARCHIVE.md` 皆豁免，且同受 AI-1 行數界線約束）。**MUST** 文字明示 `verify_checklist.sh` 的 `PIPELINE_ARTIFACT` 為**單一真相來源（single source of truth）**，SKILL.md 僅描述性引用而非另立一份清單——避免 K5-Rule 29 doc-only 假閉環。**MUST NOT** 在 SKILL.md 硬編一份與腳本平行維護的清單。
- [x] 完成（`qa/SKILL.md` Step 4.5 substep 2 改寫，grep 確認 AC6 三詞命中）

## Hard Constraints
1. **MUST NOT** 修改 `.claude/CLAUDE.md` 或 `ssot-governance.md` 以外的任何 `.claude/rules/*.md`；本票只動 `ssot-governance.md` 這一份規則檔。
2. **MUST NOT** 放寬或改動 `token_matches_file()` 或 `self_test()` 現有案例的既有期望值（`_expect` 現有斷言 hit/miss 一律不動）；只能**新增**案例。
3. **MUST NOT** 在 `verify_checklist.sh` 新增與本票（PA 豁免有界化）無關的功能；手術刀原則，不順手重構相鄰 `[0.2]`/`[0.3]`/`[0.4]` 區塊。
4. **MUST** 修改後 `bash .claude/skills/qa/scripts/verify_checklist.sh --self-test` 全數通過（含新增案例，`failed=0`）。
5. **MUST NOT** 引入需人工判斷的模糊界線：`PIPELINE_ARTIFACT_MAX_LINES` **MUST** 為明確整數常數，禁「視情況／酌情」表述。
6. **MUST** 行數計數採 `added + deleted`（numstat `$1+$2`），禁複用 `[0.3]` 的 added-only 計數。
7. **MUST NOT** 混合 `.kt/.xml` 原始碼與本票 `.sh/.md` 於同一 commit（本票無 .kt 變更，此為防呆）。commit 訊息 **MUST** 使用 `[INFRA-VERIFY-PA-BOUND-01]`。
8. `Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.`

## Error Handling
- `DIFF_REF` 尚未回填、或 `tmp/verify_cmd.conf` 未依 Rev A.1 重新生成（開工前）→ 停止回報，禁以舊 conf 執行。
- 回填後之 `DIFF_REF` 至 `HEAD` 之間若出現觸及三個 ALLOWED 檔之 commit → 停止回報 Anchor Drift，禁自行重錨（三處行號錨定之 `現值` 描述將失真）。
- self-test 新增案例無法在「未加界線的舊腳本」上如預期 FAIL（即無法證明新案例真正驗到界線邏輯）→ 停止回報，禁標 Done（Rule 9 測試驗 intent）。
- 若發現 `[0.1]` fall-through 改動意外影響 STALE／FUTURE_CONTAMINATION 分類 → 停止回報，禁靜默調整既有分類邏輯。
- 觸及 `FORBIDDEN` 或需改 `.claude/CLAUDE.md` / 其他規則檔 → 停止並走 Cross-Review Checkpoint，禁 inline。

## Acceptance Criteria
- [x] **[發現1・行為性]** 建構一個模擬 diff：某 PA 檔（如 `.claude/PROGRESS_ARCHIVE.md`）delta 超過 `PIPELINE_ARTIFACT_MAX_LINES` 且**不在** conf 的 `ALLOWED` 內 → `verify_checklist.sh` exit 1（`SCOPE VIOLATION`）。（`afbda52..b10ec17`，Δ579/Δ177，exit 1，sv_report.md AC1）
- [x] **[發現1・行為性]** 同一超界 PA 檔若**明列於** `ALLOWED` → exit 0 PASS（驗證 fall-through 保留合法大規模輪替的授權逃生口，非硬阻擋）。該模擬 conf **MUST** 將 `MAX_LINES` 設足夠大（如 `9999`）以隔離 `[0.3]` BLOAT 閘變因——否則測到的是 BLOAT 攔截而非 `[0.1]` fall-through 行為。（同 range，`MAX_LINES=9999`，exit 0，sv_report.md AC2）
- [x] **[發現1・行為性]** 小規模 PA 同步（delta ≤ 界線）且不在 `ALLOWED` → 仍 `📌 Pipeline Artifact` 豁免、不計 SCOPE VIOLATION（零回歸）。（`b79538a..89f41af`，Δ14/Δ10，exit 0，sv_report.md AC3）
- [x] **[發現1・存在性]** `bash .claude/skills/qa/scripts/verify_checklist.sh --self-test` 輸出 `failed=0`，且含新增的 `pa_exemption_applies` 四案（小規模 / 大規模 / 邊界 delta=40 / 邊界 delta=41）。（`14 passed, 0 failed`）
- [x] **[發現2・存在性]** `grep -n "PIPELINE_ARTIFACT\|sv_report\|ALLOWED" .claude/rules/ssot-governance.md` 命中新增小節，含「不得以 Pipeline Artifact 標籤擴張 ALLOWED」與「服務票面實質目的之變更無論大小 MUST 明列 ALLOWED」兩條款。（L21 命中）
- [x] **[發現3・存在性]** `grep -n "PROGRESS_ARCHIVE\|PIPELINE_ARTIFACT" .claude/skills/qa/SKILL.md` 確認 Step 4.5 substep 2 豁免範圍含兩檔且描述性引用 `verify_checklist.sh` 之 `PIPELINE_ARTIFACT`（單一真相來源），不再只列 `.claude/PROGRESS.md` 一檔。（L105 命中三詞）
- [x] **[結構]** `git diff --name-only <DIFF_REF>..HEAD` 精確為三個 ALLOWED 檔案，無夾帶（`<DIFF_REF>` 取 Verify Config 回填值，**MUST NOT** 於本條硬編雜湊）。（`d17c62a..92653c1` = 3 ALLOWED + `.claude/PROGRESS.md`〔PA 豁免範圍內〕，零偷渡）
- [x] **[結構]** `PIPELINE_ARTIFACT_MAX_LINES` 為單一明確整數常數（Hard Constraint 5），無「視情況」措辭。（`=40`，附理由註解）
- [x] **[殘餘揭露・K3-Rule 25]** sv_report.md **MUST** 明列已知未覆蓋路徑：行數界線不機械攔截「小規模但不在 ALLOWED」的越界（如 `89f41af` 品類），該殘餘由發現 2 治理層覆蓋；不得將本票標為「完整封堵所有 scope-bundling」。（sv_report.md AC9 段已揭露）
