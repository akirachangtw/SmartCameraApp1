# TKA-TRUST-01: TKA close 的成功收束只能由「完整、逐 AC 由 reviewer 回覆支持、與實作者宣稱相符、且綁定同一份 decision snapshot」的驗收決策產生，最終結果只讀該 snapshot 並如實披露非核心 FAIL

**簽發日期**: 2026-10-22（Rev A.1：同日依 Owner 修正裁決修訂，尚未實作）
**簽發者**: Claude（`/ticket-author ticket`，Plan-backed）
**來源議題**: TKAuthor
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: TKA-WIRE-01 —— 本票所修之 `close_helper.py` `decide`／`finalize`／`reviewer` 與 `SKILL.md` `Mode: close` 現值來源（CLOSED，`deliver/TKA-WIRE-01_dual-path.md`；本票不重開、不改其雙路徑 Skill 接線）；TKA-CLOSE-01 —— `close_helper.py` 原始實作與 decision／binding 結構（CLOSED，`deliver/TKA-CLOSE-01_autonomous.md`）
- `coordination_dependency`: 無

## 檔案治理／來源說明

- Parent 類型＝Plan-backed。Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261022-tka-close-trust-plan.md`（`.tmp` 暫存，七天內可能消失；本節即獨立可讀摘要）。Parent artifact 狀態＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`。Plan-local item＝`T01`（非正式票號）。Parent 讀取時間＝2026-10-22 13:5x。原始來源整理（審查回報／Owner 裁決／本地事實三分）＝`.tmp/ticket-author/20261022-tka-close-trust-source.md`。
- 選定項目 T01：同一條「驗收決策 → `decision.json` → 最終結果」資料流上的 F1–F5 信任缺口一次修正（單票；耦合依據：F2/F3 決定決策能否判 `CLOSE`，F5 決定判決者資格〔`decide` 以 tier 驗 reviewer〕，F1 決定 `CLOSE` 後最終結果取自決策或可變輸入，F4 決定最終結果是否如實反映決策；拆開會留下中間態）。
- **Owner 裁決（2026-10-22，本票適用）**：F1 最終結果使用完整且已綁定的 decision snapshot，缺 verdicts 或空物件不得生成成功，不新增永久 hash ledger／receipt；F2 移除自由文字 override 放行核心 FAIL／UNVERIFIED，新證據交 reviewer 定點回驗，Luna 能力不足升 Sol、不換 reviewer 找 PASS；F3 reviewer 逐 AC 確認核心性，分歧未解前按核心處理，不改 Template schema、不回填歷史票；F4 最終結果含非核心 FAIL 數量、AC 編號與短失敗事實，計數涵蓋所有 AC；F5 承重語義強制 Sol、不確定時 Sol，僅明確簡單項目可 Luna，保留簡短 tier basis，不建關鍵字分類平台。驗收：每項 finding 至少一個原反例回歸；另驗正常 CLOSED、核心失敗不放行、有效非核心 FAIL 可 CLOSED 且披露、部分成功補寫、七天暫存消失不虛報成功；機械測試驗決策與副作用，不以 mock 模型判讀品質，真實 reviewer 驗語義。不重開 TKA-WIRE-01；不新增 Gate／永久帳本／daemon／人工接力；過程資料只留 `.tmp` 七天。
- **Owner 修正裁決（2026-10-22，Rev A.1，取代上列與之衝突的語句）**：①F1 為 snapshot-only：有效 `CLOSE` snapshot 存在時，`verdicts.json` 缺失、為空或被替換，`finalize` 仍只由 snapshot 產生相同結果；僅 snapshot／decision 缺失、無效或 binding 不符才拒寫，**不得以「缺 verdicts 必須拒寫」作 AC**；snapshot 的 reviewer 欄位只有單一 producer，`inline`／`independent_call` 檢查沿用同一函式。②F5：結構簡單＋非空 simple-basis **不是** light 的充分條件；已知承重語義或無法判定時一律 Sol，basis 不得推翻；承重文件帶任意 basis 不得僅憑字串進 Luna；Luna 發現承重／tier 不符時升 Sol；不建分類平台。③F2／F3：core 標記之 parser、prompt、測試格式單一；core 標記不算 evidence，仍須實際非空證據；分歧先按核心，經 reviewer 定點確認與目標比對後可同步更新宣稱，不由 Agent 單方降級、不永久凍結舊分類；點重審只更新其有效涵蓋之 AC，並核對同一交付版本。④F4：所有非核心 FAIL 之數量與編號皆保留，緊湊短句披露、不存過程報告，必要失敗事實優先於固定 20 行上限、不得靜默截斷。⑤測試保護行為而非錯誤字面：舊字面與正確新契約衝突時同票更新測試，不還原舊規則。
- **Template 讀取紀錄**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件日期 2026-05-29，effective revision date 2026-09-14（v3.11）；2026-10-22 全文讀取（322 行）。

## 證據分級（2026-10-22 13:5x live 直讀；`main` @ `e89fc6fe`，工作樹乾淨）

- **CONFIRMED（本地原始碼／實際執行）**：
  1. **F1**：`cmd_finalize` 以 `verdicts, _ = load_json(a.verdicts)` 取輸入、失敗即 `{}`；本輪實際執行 `build_result_section(pre, reg, {}, …)` 產出「核心 AC 0 條皆為 PASS 或有效 N/A；非核心 PASS 0／N/A 0／UNVERIFIED 0」。`decision.json` 現僅存 `next`／`binding_ref`／`coverage` 計數／`overrides` 編號，無逐 AC 狀態快照；`verify_binding` 只驗 ticket／ticket_file／binding_ref 與祖先關係。
  2. **F2**：`cmd_decide` 於「宣稱≠reply」且 `override_evidence` 為非空字串時僅 `overrides.append(n)`，狀態保留實作者宣稱值；`test_reply_vs_claim_mismatch` 現以 `CLOSE` 斷言此行為（讀碼＋既有測試字面）。
  3. **F3**：`core` 僅取自 `verdicts.json`（實作者寫入）；`AC_REPLY_RE` 回覆格式不含核心性；`test_non_core_fail_is_not_a_blocker` 斷言非核心 FAIL 不阻擋，故實作者把 FAIL AC 標 `core:false` 即可繞過（讀碼）。
  4. **F4**：`build_result_section` 之 `nc` 僅計 `PASS`／`N/A`／`UNVERIFIED`；本輪實際執行 2 條非核心 FAIL，最終結論列為「非核心 PASS 0／N/A 0／UNVERIFIED 0」且無披露。
  5. **F5**：本輪實際執行 `classify(["plans/architecture/SSOT_v4_0_7.md"])`＝`light`、`classify(["docs/x.md"])`＝`light`，僅 `.claude/` 下判 `heavy`；`SKILL.md` 步驟 3 文字稱「會改變執行／授權／驗收／關票行為的 Skill 或規則文字為 heavy」，與 helper 行為落差。
  6. 基線：`python -X utf8 -m unittest discover -s .claude/skills/ticket-author/tests -p "test_*.py"` ＝ 96 tests OK（全綠不證明上列反例不存在，既有測試未涵蓋、F2 現行為甚至被斷言）。
- **UNKNOWN（以 AI-0 後之回歸測試／實跑取證，不當作已確認）**：七天暫存消失情境下 `finalize` 實跑結果（讀碼推定 `a.verdicts` 缺失即 `{}`，未實跑）；真實 reviewer 對 `core=Y|N` 標記之遵守度（尚無此欄位；由 AC 之真實 reviewer 驗收取證）。
- **承重推導與反例處置（Evidence gate）**：
  - D-a「有效核心性＝每次 `decide` 由當前宣稱與最新有效 reviewer 回覆重算：宣稱為核心 或 reviewer 未明確標 `core=N` 即核心」。滿足前提卻結果不同的狀態：（i）舊格式回覆（無 `core=`）＋真非核心 FAIL → 按核心阻擋，此為「未解前按核心」之 fail-closed 結果；解除路徑＝定點重審取得 `core=N`，Agent 同步把宣稱更新為非核心後，宣稱與回覆一致、分歧消失（不永久凍結舊分類）。（ii）過期狀態：點重審回覆檔早於當前 `preflight.json`（交付版本已變）→ 不採用，要求同版重審；點重審只覆蓋其回覆行涵蓋之 AC。（iii）實作者 core=true、reviewer `core=N` 且 AC 為 FAIL → 仍按核心，待宣稱同步或補證。（iv）「回覆檔晚於 `preflight.json`」前提成立卻交付版本已變的狀態：實作者在 `preflight` 後又提交 ALLOWED 內程式碼、未重跑 `preflight`、再取得新回覆 → 回覆較新而 `binding_ref` 仍為舊值；處置＝`decide` 以 `range_evidence` 重算當前 `binding_ref`，與 `preflight.json` 記錄不符即回 `SUPPLEMENT`（`reason=preflight_stale`，要求重跑 `preflight` 並同版重審），不採用任何回覆。
  - D-b「`decision.json` snapshot 為 `finalize` 唯一輸入」。反例狀態：`verdicts.json` 缺失／空／被替換 → 不影響結果（snapshot-only，AC 驗輸出逐字相同）；`CLOSE` 決策後票面 AC 條數變動（preflight AC 數≠snapshot 數）、後續 `decide` 重跑覆寫為非 `CLOSE`、snapshot 缺失或無效 → `finalize` 必拒寫；`decision.json` 隨 `.tmp` 七天清理消失 → 既有「`CLOSED` 無最終結果且無匹配決策＝不猜補」路徑，`finalize` 同樣拒寫並回報，不虛報。
  - D-c「承重語義或無法判定一律 Sol；Luna 僅為機械上未見承重、且 Agent 明示簡單 basis 之簡單項目，並隨時受 Luna 自身升級回報約束」。反例狀態：承重文件（`.claude/**`、`plans/architecture/**`、`SSOT*`／`*Contract*` 檔名等已知承重位置）帶任意 basis → 仍 Sol，basis 不得推翻；未列於已知承重集合卻實際承重的文件 + 有 basis → 機械上無法分辨，helper 不宣稱能分辨：處置＝Luna 回覆 `TIER: UPGRADE` 則 `ESCALATE_REVIEWER` 升 Sol（與既有「Luna 不能判定承重語義即升 Sol」一致），basis 僅記錄不作授權。已知限制：helper 不證明簡單性，該判定落在 Agent 聲明＋Luna 升級回報，不新增關鍵字／分類平台。
- **條件狀態矩陣（finalize／re-entry）**：

| decision.json | snapshot | verdicts.json | 票面最終結果段 | registry | 結果 |
| --- | --- | --- | --- | --- | --- |
| 無（七天已清） | — | 任 | 無 | CLOSED | `ALREADY_CLOSED`／不寫入，如實回報；`finalize` 拒寫 |
| `next≠CLOSE` 或 binding 不符 | — | 任 | 任 | 任 | `finalize` 拒寫（binding） |
| `CLOSE` | 缺失／AC 條數≠preflight／狀態非法／核心含 FAIL·UNVERIFIED／reviewer 欄位不合格 | 任 | 任 | CLOSED | `finalize` 拒寫 `decision_snapshot_invalid`，不產生「0 條核心全通過」 |
| `CLOSE` | 有效 | 缺失／空／被替換 | 無 | CLOSED | `finalize` 仍寫入，內容與 verdicts 完整時逐字相同（snapshot-only） |
| `CLOSE` | 有效 | 完整 | 無 | CLOSED | `WRITE_FINAL_ONLY`→`finalize` 寫入（部分成功補寫） |
| `CLOSE` | 有效 | 任 | 有 | CLOSED | `ALREADY_CLOSED`；重跑 `finalize` 僅更新本段 |

## Verify Config

TICKET_ID=TKA-TRUST-01
CREATED=2026-10-22
PRESET=bugfix
ALLOWED=.claude/skills/ticket-author/scripts/close_helper.py,.claude/skills/ticket-author/tests/test_close.py,.claude/skills/ticket-author/SKILL.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/settings.local.json,.claude/scripts/gate_ledger.py,.claude/scripts/route.py,.claude/hooks/gate_stop_guard.py,.claude/rules/ssot-governance.md,.claude/skills/qa/**,.claude/skills/sv/**,.claude/skills/uc/**,.claude/skills/normal/**,.claude/skills/projman/**,.claude/skills/compact/**,.claude/skills/ticket-author/references/**,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,plans/cso_briefing/project/deliver/TKA-WIRE-01_dual-path.md,plans/cso_briefing/project/deliver/TKA-CLOSE-01_autonomous.md
MAX_LINES=N/A
DIFF_REF=ccae4dff
ANCHOR_PROBED=2026-10-22 13:51
BASE_COMMIT=e89fc6fe0d99d38032dda33746bac4358f9af098
TARGET_BLOB=.claude/skills/ticket-author/scripts/close_helper.py:777dee69;.claude/skills/ticket-author/tests/test_close.py:64b01485;.claude/skills/ticket-author/SKILL.md:70a0311e
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

（`MAX_LINES=N/A`：本票含 fail-closed 守門／授權判定類安全不變量，失敗形狀為審查發現式，不可先驗估算；`N/A` 非算術比較。）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`、`.claude/skills/ticket-author/tests/test_close.py`、`.claude/skills/ticket-author/SKILL.md`
- **動作**: 逐字確認下列現值各恰 1 次（`ANCHOR_PROBED` 2026-10-22 13:51 已實測，皆為 1）：
  - `close_helper.py`：`ev = e.get("override_evidence")`；`def build_result_section(pre, reg_after, verdicts, summary_lines, commits, now, na_fields, dec=None):`；`verdicts, _ = load_json(a.verdicts)`；`def classify(allowed):`；`tier = "heavy" if force_heavy else classify(allowed)`；`tier = classify(pre.get("verify", {}).get("allowed", []))`；`core = {n: e for n, e in seen.items() if e.get("core") is True and e.get("status") in AC_STATUS}`；`AC_REPLY_RE = re.compile(`；`def verify_binding(repo, tid, pre):`；`def parse_reviewer_replies(paths, n_acs):`；`nc = {"PASS": 0, "N/A": 0, "UNVERIFIED": 0}`；`rec["overrides"] = overrides`；`def cmd_finalize(a):`；`--force-heavy` 恰 2 次。
  - `SKILL.md`：`record \`override_evidence\` for that AC in \`verdicts.json\``；`free-text model output never overrides a direct fact`；`(light = every ALLOWED file is \`.md\`/\`.txt\`/\`.rst\` and none lives under \`.claude/\`)`；`AC<n>: PASS|FAIL|N/A|UNVERIFIED | <short evidence>`；`and run \`finalize\`, which repeats the read-back check`；dispatch 區塊 `finalize --ticket <ID> --registry <dir>/registry_after.json --verdicts <dir>/verdicts.json --summary`。
  - `test_close.py`：`def test_reply_vs_claim_mismatch(self):`；`def test_non_core_fail_is_not_a_blocker(self):`；`def test_light_classification_is_a_superset_of_the_low_risk_condition(self):`；`class TestFinalResult(Base):`。
  - 基線：`unittest discover` 96 tests OK；`git diff --name-only e89fc6fe..HEAD -- <ALLOWED>` 為空則漂移未及標的；非空依 v3.11 三分流記錄歸因後以現值續作（資訊性，不單獨停工）。
- **任一項不符 → 依三分流處理，禁自行推定等價位置。**
- [x] 完成（2026-10-22 開工：close_helper 14／SKILL 4／test 4 組錨點命中次數皆與票面相符，`--force-heavy` 2 次；`e89fc6fe..HEAD` 對 ALLOWED 無提交 → 漂移未及標的；`DIFF_REF` 已回填 `e89fc6fe`；基線 `unittest discover` 96 OK〔開票時實測〕；`/normal TKA-TRUST-01` `ACTIVATED`，lifecycle 已 `IN_PROGRESS`）

### AI-1: F2＋F3 `decide`：移除 override 放行，核心性由 reviewer 確認並可同步
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **現值**: `decide` 以 `override_evidence` 非空字串放行「宣稱≠reply」；`core` 僅取自 `verdicts.json`；`parse_reviewer_replies` 只回傳狀態。
- **目標值**: ①刪除 `override_evidence` 路徑與 `overrides` 變數／`rec["overrides"]`／`finish`／`base` 內全部衍生（孤兒一併移除）：宣稱≠reply 一律以 reply 狀態為準並記 `mism`（既有 `reviewer_mismatch` 補證流程）。②**單一回覆行格式**（parser、`SKILL.md` prompt、測試共用同一字面）：`AC<n>: PASS|FAIL|N/A|UNVERIFIED | core=Y|N | <evidence>`；`core=` 標記由 `parse_reviewer_replies` 解析並自 evidence 剝除，**標記本身不算 evidence**——剝除後 evidence 為空即視同既有 `no_evidence`（該 AC 未涵蓋）；無標記之行（既有格式）仍可解析，視為「核心性未確認」。③有效核心性每次 `decide` 由「當前宣稱」與「最新有效 reviewer 回覆」重算：宣稱為核心 或 回覆未明確標 `core=N` 即核心；宣稱與回覆不一致且 AC 為 FAIL／UNVERIFIED＝分歧：`rnd<2` 回 `SUPPLEMENT`（`reason=core_disagreement`，列 AC），達上限仍按核心處理（FAIL→`BLOCKED`、UNVERIFIED→既有 `core_unverified` 路徑）。經 reviewer 定點重審（`reviewer_reply2.txt`，prompt 引述本票一句話目標與該 AC 全文作目標比對）得 `core=N` 後，Agent 把 `verdicts.json` 該 AC 宣稱同步改為非核心，兩者一致即消除分歧；Agent 不得僅憑自己改宣稱而降級（回覆未標 `core=N` 時仍按核心），舊分類亦不凍結（每輪重算）。④點重審**只更新其回覆行有效涵蓋之 AC**；回覆檔 mtime 早於當前 `preflight.json`（交付版本已變）者視為過期不採用（`reviewer_reply_stale`），須同版重審。⑤核心 FAIL／UNVERIFIED 判定改用有效核心性。⑥`decide` 於 REVIEW 路徑開頭以 `range_evidence`（沿用 `preflight.json` 已記錄之 cfg／ALLOWED／FORBIDDEN）重算當前 `binding_ref`，與 `preflight.json` 不符 → `SUPPLEMENT`（`reason=preflight_stale`）。
- **核心性真值表（AC 狀態為 FAIL／UNVERIFIED 時；PASS／N/A 不觸發分歧處置，僅影響計數且有效核心性算法相同）**：

  | 宣稱 core | 回覆標記 | 有效核心性 | `rnd<2` | `rnd≥2`（上限） |
  | --- | --- | --- | --- | --- |
  | T | `Y` | 核心 | 無分歧：FAIL→`BLOCKED`；UNVERIFIED→`core_unverified` 路徑 | 同左 |
  | T | 無標記 | 核心 | 無分歧（保守一致）：同上 | 同左 |
  | T | `N` | 核心 | `SUPPLEMENT` `core_disagreement`（reviewer 說非核心而宣稱為核心，定點重審後 Agent 同步宣稱） | 維持核心：FAIL→`BLOCKED`；UNVERIFIED→`NOT_CLOSED` |
  | F | `Y` | 核心 | `SUPPLEMENT` `core_disagreement` | 維持核心，同上 |
  | F | 無標記 | 核心（未確認） | `SUPPLEMENT` `core_disagreement`（請 reviewer 補標記） | 維持核心，同上 |
  | F | `N`＋非空 evidence | 非核心 | FAIL／UNVERIFIED 皆不阻擋 | 同左 |

  （回覆標記取「最新有效回覆」：`reviewer_reply2.txt` 只覆蓋其涵蓋之 AC；過期或 `preflight_stale` 之回覆不採用。）
- [x] 完成（AI-1：`decide` 移除 override／`overrides` 路徑；`parse_reviewer_replies` 解析 `core=Y|N`（不算 evidence）；有效核心性每輪重算＋`core_disagreement` SUPPLEMENT；過期回覆 `reviewer_reply_stale`；`live_binding_ref` 重算 → `preflight_stale`（close_helper.py））

### AI-2: F1 `decide` 寫入 decision snapshot（reviewer 欄位單一 producer）
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **現值**: `finish()` 僅在 `CLOSE` 存 `reviews_patch`／`reviews_na_fields`／`coverage`／`overrides`。
- **目標值**: `next=="CLOSE"` 時 `decision.json` 增 `snapshot`：涵蓋 1..N 全部 AC 的 `{n,status,core(有效),ev(短失敗/佐證事實)}`（status 為經 reply 校正與 N/A 降級後之最終值）、`reviewer`、`dependency_status`、`coverage`。**`snapshot.reviewer` 由單一函式自已通過 `validate_reviewer_meta` 的 `verdicts.reviewer` 組出**（family／actual_model／tier／fallback_used／fallback_reason／independent_call／inline／limitation／tier_basis），不另寫第二套欄位；不新增永久 ledger／receipt；寫入失敗沿用既有 `decision_saved:false`。
- [x] 完成（AI-2：`finish()` 於 CLOSE 寫入 `snapshot`（`build_snapshot`／單一 `reviewer_record`））

### AI-3: F1 `finalize`／`verify_binding` snapshot-only
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **現值**: `verdicts, _ = load_json(a.verdicts)`（缺失即 `{}`）→ `build_result_section(pre, after, verdicts, …)`。
- **目標值**: `finalize` **完全不讀 `--verdicts`**（參數保留為可省略且被忽略，相容既有呼叫）；`build_result_section` 只吃 snapshot，故 `verdicts.json` 缺失、為空或被替換不影響輸出。`verify_binding` 增 snapshot 檢查，`finalize` 與 `decide` 之 `WRITE_FINAL_ONLY` 判定共用：`snapshot` 缺失、AC 條數≠`preflight.json` 條數、編號非 1..N 唯一、status／core 非法、核心 AC 為 FAIL／UNVERIFIED、`reviewer` 欄位不能通過與 AI-2 **同一個** `validate_reviewer_meta`（含 `inline` 為 false、`independent_call` 為 true、model／tier 合格）→ `Refuse({"refused":"decision_snapshot_invalid", …})`（`finalize`）或不判 `WRITE_FINAL_ONLY`（`decide`）。`decision.json` 缺失（暫存已清）或 binding 不符同樣拒寫並如實回報，不虛報成功。
- [x] 完成（AI-3：`finalize` 不讀 `--verdicts`；`verify_binding` 併入 `snapshot_ok`（同一 `validate_reviewer_meta`）；`decision_snapshot_invalid`）

### AI-4: F4 最終結果揭露（不靜默截斷）
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **現值**: `nc = {"PASS": 0, "N/A": 0, "UNVERIFIED": 0}`，無非核心 FAIL 計數與披露；結果段固定 `lines[:20]`、披露 `disc[:5]`。
- **目標值**: 非核心計數含 `FAIL`，結論列註明 AC 總數且各狀態合計＝總數；所有非核心 FAIL 之**數量與全部 AC 編號**必留，每條附緊湊短句事實（snapshot `ev`，單條 ≤40 字）；FAIL 披露**不受**固定行數／條數上限截斷（其他次要披露才受既有上限，被擠出者須以「另 N 項」計數揭露，不靜默丟棄）；不存過程報告或 reviewer 原文。
- [x] 完成（AI-4：`build_result_section` 只吃 snapshot，非核心 FAIL 計數＋全部編號＋短事實（≤40 字），無固定行數截斷）

### AI-5: F5 tier：承重或不確定一律 Sol，basis 不得推翻
- **檔案**: `.claude/skills/ticket-author/scripts/close_helper.py`
- **現值**: `tier = "heavy" if force_heavy else classify(allowed)`；`decide` 之 `tier = classify(…)`；`classify` 僅以副檔名／`.claude/` 位置判 light。
- **目標值**: `classify` 保持為結構性必要條件並新增一個小型常數「已知承重位置集合」（`.claude/**`、`plans/architecture/**`、`plans/cso_briefing/project/file/**`、檔名含 `SSOT`／`Contract`；僅固定路徑／檔名，不含內容關鍵字、不建分類平台）：命中即 heavy。`reviewer --simple-basis TEXT` 僅在「結構合格且未命中已知承重集合」時才可使 tier 為 `light`，basis 為空或命中承重集合一律 `heavy`——**basis 永不推翻 heavy**；輸出簡短 `tier_basis`（basis 前 80 字，或 `default heavy: <原因>`）並寫入 `verdicts.reviewer.tier_basis`；`decide` 同規則。Luna 回覆含單行 `TIER: UPGRADE | <原因>`（prompt 要求 Luna 發現承重／tier 不符時如此回報）→ `decide` 回 `ESCALATE_REVIEWER`（升 `gpt-6-sol`，不切換家族、不另找 PASS）；heavy 票收 Luna 回覆仍 `reviewer_tier_mismatch`。`TIER: UPGRADE` 受理條件（窮盡）：僅當 `verdicts.reviewer.actual_model`＝`gpt-6-luna` 且 `--fallback-tried 0` 時受理並回 `ESCALATE_REVIEWER`（優先於 `SUPPLEMENT`／`core_disagreement` 等同輪處置）；`--fallback-tried 1`（已升 Sol）或 reviewer 非 Luna 時該行忽略，AC 判定以 `reviewer_reply2.txt`（Sol）為準；只受理來自 Luna 之回覆檔，不受理 Agent 自寫。
- [x] 完成（AI-5：`LOAD_BEARING_DIRS`／`LOAD_BEARING_NAME_RE`、`select_reviewer(simple_basis)`、`reviewer --simple-basis`、`TIER: UPGRADE`（Luna 且 fb=0））

### AI-6: `SKILL.md` Mode: close 文字同步（替換衝突舊文，不留平行規則）
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值**: 見 AI-0 `SKILL.md` 清單。
- **目標值**: ①步驟 4 回覆行格式改為 AI-1 之單一字面 `AC<n>: PASS|FAIL|N/A|UNVERIFIED | core=Y|N | <short evidence>`，並註明核心性標記不算 evidence、點重審 prompt 須引述本票一句話目標與該 AC 全文、回覆須晚於同版 `preflight.json`；②刪除 `override_evidence` 與「evidence wins 並記 override」句，改為：與直接機械證據矛盾者交 reviewer 定點回驗，自由文字不放行；③步驟 3 的 light 判準改為「預設 Sol；承重或無法判定一律 Sol；僅結構簡單、未命中已知承重位置且明示 `--simple-basis` 者可 Luna，basis 不得推翻 heavy；Luna 發現承重／tier 不符以 `TIER: UPGRADE` 回報並升 Sol」；④步驟 6 增 `core_disagreement`（補證或定點複核、上限同既有 2 輪、未解按核心、確認後同步宣稱）；⑤dispatch 區塊 `finalize` 去除 `--verdicts`，步驟 8 註明最終結果只讀 `decision.json` snapshot；⑥舊字面與新契約衝突者同票更新對應測試；仍成立之被測字面（`EVERY AC`、`reviewer_reply`、`gpt-6-sol`、`gpt-6-luna`、`--codex-fault`、`ESCALATE_REVIEWER`、`never the implementing Agent answering inline`、`--reply <dir>/reviewer_reply.txt`、`Cross-Review verdict history`）保留，不含 `ask the other reviewer`。
- [x] 完成（AI-6：SKILL.md Mode: close 步驟 3/4/6/8 與 dispatch 區塊同步，已無 `override_evidence`／`--verdicts`）

### AI-7: 測試（保護行為；原反例回歸＋正常/失敗/披露/補寫/暫存消失）
- **檔案**: `.claude/skills/ticket-author/tests/test_close.py`
- **現值**: `test_reply_vs_claim_mismatch` 以 override 斷言 `CLOSE`；`test_non_core_fail_is_not_a_blocker` 以宣稱 `core:false` 斷言可行；`test_codex_is_primary_for_every_ticket_type` 之 light 列不帶 basis；`test_light_classification_is_a_superset_of_the_low_risk_condition` 以位置／副檔名斷言 light；若干 finalize 測試依賴 `verdicts.json`。
- **目標值**: 以真實 `decide`／`finalize`／`reviewer` 子命令（機械測試，不 mock 模型品質，不保護錯誤舊字面；衝突舊斷言於同票改為新契約斷言，**不還原舊規則**）：F1 `verdicts.json` 缺失／`{}`／被替換為相反內容 → `finalize` 輸出與完整 verdicts 時**逐字相同**；snapshot 缺失／AC 條數被改／狀態非法／reviewer 欄位 `inline:true`／`independent_call:false`／`decision.json` 缺失／binding 不符 → exit 3 且票檔 bytes 不變，且絕不出現「核心 AC 0 條」；F2 原 override 反例改斷言不放行；F3 宣稱 `core:false` 之 FAIL：回覆無標記或 `core=Y` → 分歧補證、上限後阻擋；`core=N` 且 Agent 同步宣稱 → 可 `CLOSE`；僅 Agent 改宣稱而回覆未標 `core=N` → 仍阻擋；`core=N` 單獨無 evidence → 視為未涵蓋；點重審只改其涵蓋之 AC；過期回覆（早於 `preflight.json`）不採用；F4 多條非核心 FAIL 之數量／全部編號／短事實全保留，披露不被行數上限截斷，各狀態合計＝總數，核心 FAIL 不進 `CLOSED`；F5 承重文件（`plans/architecture/x.md`、`.claude/rules/r.md`、`SSOT_*.md`）帶**任意** basis 仍 `heavy`/`gpt-6-sol`，非承重簡單 docs 無 basis → `heavy`、有 basis → `light`，Luna 回覆 `TIER: UPGRADE` → `ESCALATE_REVIEWER`，heavy 票收 Luna 回覆被拒；另含正常 `CLOSE`→`finalize`、部分成功補寫（`WRITE_FINAL_ONLY`）、`.tmp` 清空不虛報；`binding_ref` 重算反例（`preflight` 後新增 ALLOWED 內提交、未重跑 `preflight`、回覆較新）→ `SUPPLEMENT` `preflight_stale`。舊形狀 fixture（無 `snapshot` 之 `decision.json`、無 `core=` 標記之回覆行）一律在正規化 writer 之下以直接寫檔種入，並於操作前先斷言其起始形狀（無 `snapshot` 鍵／無標記），再驗證處置為「不判 `WRITE_FINAL_ONLY`／視為核心性未確認」。
- [x] 完成（AI-7：新增 `TestTrustBoundary` 18 測試＋既有 8 測試改為新契約行為斷言；`unittest discover` 114 OK；18 項新測試對 base 版 helper 皆失敗（`.tmp/ticket-author/trust/red_vs_base_helper.txt`））

### AI-8: 真實 reviewer 驗證與收尾
- **檔案**: `.claude/skills/ticket-author/SKILL.md`、`.claude/skills/ticket-author/scripts/close_helper.py`（僅於驗證發現語義缺口時修正）
- **目標值**: 本票自身依正常路徑 closeout 至 `REVIEW` 後，由 `Skill(ticket-author)` `close TKA-TRUST-01` 以真實 `codex exec`（heavy→`gpt-6-sol`）取得逐 AC 回覆；檢查原始回覆行符合單一格式（含 `core=Y|N` 與非空 evidence）、`decide` 解析覆蓋 N/N、最終結果取自 snapshot。發現 reviewer 不依格式回覆等語義缺口時，於本票內修正 prompt／解析後重驗；任一不成立如實記未達成與最小修補，不以手動 close 補寫為已驗證。
- [ ] 完成

## Hard Constraints
1. 不重開 TKA-WIRE-01／TKA-CLOSE-01；不改雙路徑上游接線（`sv`／`uc`／`ssot-governance.md`／`gate_stop_guard.py`）。
2. 不新增永久 ledger／receipt／manifest／hash 帳本／Gate／daemon／人工接力；snapshot 僅存 `.tmp` 七天內之 `decision.json`；正式票面只留既有最小最終結果段。
3. 不改 Ticket Template schema，不回填歷史票；歷史 `CLOSED` 票無最終結果段且無匹配決策者維持「不猜補」。
4. 核心 FAIL／UNVERIFIED 不得因自由文字、Agent 單方宣稱降級或 snapshot 無效而放行；未解的核心性分歧按核心處理；有效 snapshot 存在時 `verdicts.json` 不影響 `finalize` 結果。
5. 不以內容關鍵字或可擴張清單建立承重分類平台；已知承重集合僅為固定路徑／檔名常數；basis 僅記錄、不得推翻 heavy；helper 不宣稱能證明簡單性。
6. 測試不 mock 模型判讀品質；機械測試驗決策與副作用，語義由真實 reviewer 驗；測試保護行為，不保護被新契約取代的舊字面。
7. 修改產生之孤兒（`overrides` 等）一併移除；不移除修改前即存在之無關 dead code。
8. 實機義務：N/A（豁免理由：本票為 Skill／腳本之決策閘邏輯，無裝置、感測器或相機路徑；以「真實 reviewer 調用」〔AI-8／最後一條 `[行為性]` AC〕取代實機驗證）。機械測試不得被表述為語義或實機驗證。

## Error Handling
- 任一 AI-0 現值不符 → 依 v3.11 三分流處理（範圍不相交→記錄；他票前綴→記錄歸因後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE`→停止），禁推定等價位置。
- 真實 reviewer 不可用或權限被拒 → 如實記 `[行為性]` 驗證未達成與最小需求，不手動模仿、不以 mock 當 reviewer 驗收。
- 舊字面測試（含 `TestUpstreamWiring`）與新契約衝突 → 同票更新測試並於收尾載明；僅當更新會削弱對行為的保護時才停止回報。
- `unittest` 失敗無法在 ALLOWED 內修復 → 停止回報。
- **[標配]** `git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（開票時三標的實測皆無命中）。

## Acceptance Criteria
- [x] **[存在性]** `close_helper.py` 不再含 `override_evidence`／`overrides` 放行路徑；`SKILL.md` 不再含 ``record `override_evidence` ``；回覆行格式在 parser／`SKILL.md`／測試三處為同一字面 `core=Y|N | <evidence>`；dispatch 區塊 `finalize` 不含 `--verdicts`；Luna 條款含「basis 不得推翻 heavy」與 `TIER: UPGRADE`（證據：`test_contract_text_has_one_reply_format_and_no_override_path` 斷言（override_evidence 零命中、單一回覆行格式、finalize dispatch 無 --verdicts、TIER: UPGRADE／--simple-basis））
- [x] **[存在性]** 位置清單：snapshot producer＝`decide` 於 `CLOSE` 寫入之 `decision.json`（reviewer 欄位僅由單一函式組出）、consumer＝`finalize`／`WRITE_FINAL_ONLY`（同一 `validate_reviewer_meta` 檢查）；核心性標記 producer＝`SKILL.md` 步驟 4 要求之 reviewer 回覆行、parser＝`parse_reviewer_replies`；tier basis producer＝`reviewer --simple-basis`、consumer＝`decide`（`檔案:行號` 於執行後回填；僅證位置，不證可達）（證據：位置：`close_helper.py` `build_snapshot:659`/`reviewer_record:650`/`snapshot_ok:666`/`verify_binding:695`/`parse_reviewer_replies:796`/`select_reviewer:597`；僅證位置）
- [x] **[可達性]** given 以真實 CLI 依序執行 `preflight→reviewer→decide(CLOSE)→finalize`（registry 轉態以 fixture 模擬）expect：①`decide` 寫出之 `decision.json.snapshot` 含全部 AC 與合格 reviewer 欄位；②改動 snapshot 中某 AC 的 `status`／`ev` 後重跑 `finalize`，最終結果段隨之改變，而改動 `verdicts.json` 不改變輸出（trace 顯示 snapshot 為 `finalize` 唯一來源）；③回覆行 `core=N` 經 `parse_reviewer_replies`→有效核心性→snapshot `core`→最終結果之核心／非核心計數逐段可見（證據：`test_f1_finalize_is_snapshot_only`／`test_f1_snapshot_content_and_single_reviewer_producer` 真實鏈 trace）
- [x] **[行為性]** F1 given 有效 `CLOSE` snapshot expect `verdicts.json` 缺失／`{}`／被替換時 `finalize` 輸出與完整 verdicts 時逐字相同；given snapshot／decision 缺失、無效（AC 數被改、狀態非法、`inline:true`、`independent_call:false`）或 binding 不符 expect `finalize` 拒寫（exit 3）並回 `decision_snapshot_invalid`／binding 原因、票檔 bytes 不變（合法的「全非核心」snapshot 仍可正常產出含「核心 AC 0 條」之結果，故不以該字串作拒寫判準）（證據：`test_f1_finalize_is_snapshot_only`／`..._invalid_snapshot_or_binding_refuses...`／`..._legacy_decision_without_snapshot...`）
- [x] **[行為性]** F2 given 核心 AC 宣稱 PASS 但 reply 為 FAIL／UNVERIFIED 並附任意 `override_evidence` expect 不得 `CLOSE`（FAIL→`BLOCKED`；UNVERIFIED→補證或 `NOT_CLOSED`）；新證據經 `reviewer_reply2.txt` 定點回驗，只更新其涵蓋之 AC；過期回覆（早於當前 `preflight.json`）不採用；`preflight` 後新增 ALLOWED 內提交且未重跑 `preflight`（回覆反而較新）expect `SUPPLEMENT` `preflight_stale`，不採用任何回覆（證據：`test_f2_override_evidence_cannot_release...`／`test_f2_point_recheck...`／`test_f2_preflight_stale...`）
- [x] **[行為性]** F3 given 實作者標 `core:false` 之 FAIL AC：回覆無標記或 `core=Y` expect 分歧補證、至上限仍阻擋；回覆 `core=N`＋非空證據且宣稱同步為非核心 expect 可 `CLOSE`；僅 Agent 單方改宣稱而回覆未標 `core=N` expect 仍阻擋；僅有 `core=N` 而無證據 expect 該 AC 視同未涵蓋（證據：`test_f3_*` 三測試）
- [x] **[行為性]** F4 given 多條非核心 FAIL expect 最終結果保留數量與全部 AC 編號及緊湊短事實、不被行數上限截斷、各狀態合計＝AC 總數；given 核心 FAIL expect 不進入 `CLOSED`（證據：`test_f4_*` 兩測試（2 條與 10 條非核心 FAIL））
- [x] **[行為性]** F5 given 承重文件（`plans/architecture/*.md`、`.claude/rules/*.md`、`SSOT*.md`）帶任意 `--simple-basis` expect 仍 `heavy`／`gpt-6-sol`；given 非承重簡單 docs 無 basis expect `heavy`、有 basis expect `light`；Luna 回覆 `TIER: UPGRADE` expect `ESCALATE_REVIEWER`；heavy 票收 Luna 回覆 expect `reviewer_tier_mismatch`（證據：`test_f5_*` 四測試）
- [x] **[行為性]** 正常 `CLOSE`→`finalize` 寫入；`CLOSED` 且無最終結果段 + 有效 snapshot expect `WRITE_FINAL_ONLY` 補寫成功；`decision.json` 清空（模擬七天）expect 不寫入、不虛報（證據：`to_close`→`finalize` 既有測試＋`test_f1_partial_success_resumes_from_the_snapshot_even_without_verdicts`＋`decision.json` 清空測試）
- [x] **[行為性]** 每項 F1–F5 至少一個原反例回歸測試，且收尾實錄（觀察者）逐項列出「測試名＋該測試對 base 版 `close_helper.py`（以 `git show <BASE_COMMIT>:.claude/skills/ticket-author/scripts/close_helper.py` 取出置於暫存目錄執行）失敗之輸出摘要＋對修改後版本通過」；缺任一項的回歸不計入；既有 `unittest discover` 全綠，與新契約衝突之舊斷言已同票更新為行為斷言（非還原舊規則）（證據：18 項新測試對 base 版 helper 皆失敗（`.tmp/ticket-author/trust/red_vs_base_helper.txt`）、對新版 114 OK；舊斷言已更新為行為斷言（見 sv_report Stage A）；收尾實錄逐項列於「收尾實錄」）
- [ ] **[行為性]** 真實 reviewer：本票自身 close 之 `codex exec`（`gpt-6-sol`）原始回覆存於 `.tmp/ticket-author/close/TKA-TRUST-01/`，逐 AC 行符合單一格式（含 `core=` 與非空 evidence），`decide` 解析覆蓋 N/N，最終結果取自 snapshot；不成立則如實記未達成與最小修補
- [ ] **[存在性]** 收尾實錄如實分層（機械測試／真實 reviewer／未驗項），不宣稱跨票型長期遵守度已驗證
- ~~`assembleDebug` PASS~~ (N/A: 非 Android 編譯產物之 Skill／腳本票；以 `unittest discover` 取代)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（證據：`git diff --name-only ccae4dff..0aa98afa` ＝ ALLOWED 三檔）

## 收尾實錄（執行端；2026-10-22；Mode: normal；Rev A.1）

- 啟動：`/normal TKA-TRUST-01`（`ACTIVATED`）；RGR 重跑 PASS（Critical 0／Major 0，`SAFE_TO_IMPLEMENT`；首跑 PARTIAL 的 5 Major＋3 Medium 已於 `TICKET_RIGOR` 同回合修補，報告 `.tmp/rigor-gate/RGR_TKA-TRUST-01_20261022.md`，七天暫存）。
- 實作提交：`0aa98afa`（主體，3 檔 +566／−81）＋`26440e8a`（Cross-Review R1 REPORT_FIX 修正，`TIER: UPGRADE` 改為任一新鮮回覆檔皆辨識）。
- `DIFF_REF` 由 `e89fc6fe` 重定基線至 `ccae4dff`：ADV-ENTRY-01 另一 session 在共用工作樹提交 `bef6a500`／`ccae4dff`，`e89fc6fe..ccae4dff` 對本票 ALLOWED 零觸及；`BASE_COMMIT` 不變。此外 `bdc4595d`／`71a40d05`［ADV-ENTRY-01］與 `b1fb3fce`［GOVERNANCE-SYNC］落在 `0aa98afa` 與 `26440e8a` 之間，已於 SV 報告 `## Excluded Payload` 如實列出並排除出審查 payload（援引 `concurrency.md` 待開票 #5 之 Akira Option B 逐檔排除先例；本票不主張其他授權），本票未修改這些檔案。

### 驗收分層（各自證明什麼）

1. **A 機械測試（`unittest discover` 114 OK；開工基線 96）**：新增 `TestTrustBoundary` 18 測試。證明 helper 的決策與副作用（snapshot-only finalize、拒寫條件、override 不放行、核心性真值表、`preflight_stale`／`reviewer_reply_stale`、非核心 FAIL 披露、tier 與 `TIER: UPGRADE`、部分成功補寫、暫存消失不虛報、舊形狀 fixture）。**不證明** 真實 reviewer 的語義判讀品質。
2. **原反例回歸（AC 觀察者）**：18 項新測試對 `e89fc6fe` 版 `close_helper.py`（`git show` 取出置暫存 skill 樹）全數失敗（`failures=8, errors=10`，輸出 `.tmp/ticket-author/trust/red_vs_base_helper.txt`），對新版全數通過。原反例現象：F1 `verdicts` 缺失時最終結果出現「核心 AC 0 條皆為 PASS」；F2 以 `override_evidence` 把核心 UNVERIFIED 判 `CLOSE`；F3 宣稱 `core:false` 的 FAIL 判 `CLOSE`；F4 兩條非核心 FAIL 未揭露；F5 `plans/architecture/*.md` 判 Luna。R1 修正的「僅 reply2 帶 `TIER: UPGRADE`」斷言對 `0aa98afa` 版 helper 實跑 ERROR、對新版通過。
3. **舊斷言更新（行為斷言取代舊字面，未還原舊規則）**：`test_reply_vs_claim_mismatch`、`test_unverified_sequence`、`test_heavy_ticket_cannot_close_on_luna_and_light_may_escalate`、`TestReviewer` 三測試、`test_full_flow_from_skill_block`；測試夾具的假 reviewer 回覆加 `core=Y|N`。
4. **QA／SV**：`/qa` Step 0 PASS（重定基線後）、Step 1–3 N/A（無 gradle 標的）、Step 3.5 僅 `app/` 既有警示、Step 4 Dashboard 同步延至 `/compact`；`/sv` Cross-Review（`cross_review.sh`，Codex）R1 `REPORT_FIX`（`TIER: UPGRADE` 第二回覆盲區，屬實，已修）→ 重跑 `PASS`；`structural_verify --post` PASS [ADVISORY]。
5. **C 真實 reviewer（AI-8）**：由本票自身之 `Skill(ticket-author)` `close TKA-TRUST-01` 取得——真實 `codex exec`（heavy→`gpt-6-sol`）逐 AC 回覆須含 `core=` 與非空 evidence、`decide` 解析覆蓋 N/N、最終結果取自 snapshot。**結果見 close 最終結果區段與下方 close 後補記；在此之前此項標「未驗」。**

### 尚未證明／限定

- 真實 Codex／Luna 是否遵守 `core=Y|N` 與 `TIER: UPGRADE` 格式（C 項待取證；Luna 路徑本票不會走到，故 `TIER: UPGRADE` 的真實回報行為仍未驗）。
- 同版核對僅為 mtime＋`binding_ref` 重算，不證明 reviewer 實際做了目標比對（以 prompt 引述一句話目標與 AC 全文約束）。
- helper 不證明「簡單性」；未列入已知承重集合卻實際承重者依賴 Luna `TIER: UPGRADE` 與升級規則。
- 跨票型、長期、跨 session 遵守度未驗；`gradle`／Android 層未跑（N/A）。
