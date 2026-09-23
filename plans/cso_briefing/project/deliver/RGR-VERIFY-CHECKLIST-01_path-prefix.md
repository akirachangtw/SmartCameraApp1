# RGR-VERIFY-CHECKLIST-01: 使 verify_checklist.sh 對目錄型 ALLOWED token 以 repo 相對路徑前綴判定，消除巢狀檔誤判

**簽發日期**: 2026-09-23
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-AUTO-CLEANUP-01（CLOSED；其 Step 0 誤判為本票之正向事件證據）
- `coordination_dependency`: 無

> **檔案治理/來源說明**：裁決來源＝Akira 2026-09-23（採甲，CLEANUP-01 收尾後開立；範圍限 path-prefix 判定，禁擴張為 allowlist parser 重寫）。開票期一手取證（Desktop 唯讀）：
> - 缺陷位置＝`.claude/skills/qa/scripts/verify_checklist.sh` 之 `token_matches_file()`（L~100–115）。含 `/` 之 token 走 `[[ "$file" == $token ]]` glob 比對；目錄型 token 以 `/` 結尾，對 `…/rgr-auto/cases/c1.json` 恆不匹配 → 落入 L451 `SCOPE VIOLATION`。
> - 非 basename-only 缺陷（先前 CLEANUP-01 冊面所記「basename-only」為表象描述）：basename 分支僅適用於不含 `/` 之 token。**真因＝目錄型 token 未被識別為前綴**。
> - 既有 self-test 於 L169／L177–179 已覆蓋 path-glob 與 basename 兩類，**無**目錄型 token 案例 → 本票補之。
> - 票面寫入權：SKAD Desktop 唯一；執行端僅勾選＋附 receipt。

## Verify Config
TICKET_ID=RGR-VERIFY-CHECKLIST-01
CREATED=2026-09-23
PRESET=bugfix
ALLOWED=.claude/skills/qa/scripts/verify_checklist.sh
FORBIDDEN=.claude/skills/ticket-rigor-gate/,.claude/PROGRESS.md
MAX_LINES=N/A
DIFF_REF=552fd28
ANCHOR_PROBED=2026-09-23（Desktop 開票期）
BASE_COMMIT=552fd28
TARGET_BLOB=N/A（Desktop 無 blob-hash producer；AI-0 逐檔讀現值）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

MAX_LINES=N/A 註：本票 AI 觸及 scope 守門判定（安全不變量），依 TPL v3.5 R3 標 N/A。

## Action Items

### AI-0: 錨點復驗
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- 確認 `token_matches_file()` 內 `if [[ "$token" == *"/"* || "$token" == *"\\"* ]]; then` 與其下 `[[ "$file" == $token ]]` 各恰 1 次。
- 確認 self-test 區（`_expect` 呼叫群）存在且可執行。
- 任一不符 → 停止回報，禁自行推定等價位置。
- [x] 完成

### AI-1: 目錄型 token 改前綴判定
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh` → `token_matches_file()`
- **現值**: 含 `/` 之 token 一律走 `[[ "$file" == $token ]]`。
- **目標值**: 於該分支內先判 token 是否以 `/` 結尾；是 → `[[ "$file" == "$token"* ]]`（repo 相對路徑前綴，token 原樣不去斜線）；否 → 維持既有 glob 比對一字不動。
- 不得改動 basename 分支、`INFRA-VERIFY-GLOB-01` 註解所述之未加引號 RHS 行為、或 FORBIDDEN／EXCLUDED_PAYLOAD 之取用方式（同函式共用，行為變更一體適用屬預期）。
- [x] 完成

### AI-2: 補 self-test 正負例
- **檔案**: 同上，self-test 區
- 新增正例：token `.claude/skills/ticket-rigor-gate/runner/fixtures/rgr-auto/`、file `.claude/skills/ticket-rigor-gate/runner/fixtures/rgr-auto/cases/c1.json` → hit。
- 新增負例：同 token、file `.claude/skills/ticket-rigor-gate/runner/fixtures/rgr-auto-sibling/x.json` → miss（前綴相鄰不得誤命中）。
- 既有 `_expect` 案例一項不得刪改。
- [x] 完成

### AI-3: 驗收與提交
- 執行 self-test，全數 PASS 且既有案例零回歸。
- `git diff --name-only HEAD` ⊆ ALLOWED → commit `[RGR-VERIFY-CHECKLIST-01] fix(qa): directory-form ALLOWED token matches by path prefix`。
- 本票單獨 commit，禁與 RGR-FIXTURE-REPORT-PATH-01 併批。
- [x] 完成 — commit `c73e6ba`；self-test 20 passed, 0 failed（18 既有 + 2 新增）

## Hard Constraints
1. 禁改動 `token_matches_file()` 以外之判定邏輯；禁重寫 allowlist parser。
2. 禁觸碰 `.claude/skills/ticket-rigor-gate/` 任何檔案（屬另票範圍）。
3. 禁 `git reset --hard`、`clean -fd`、改寫歷史。
4. 禁與另票合併實作或共用 commit。

## Error Handling
- self-test 任一既有案例回歸 FAIL → 停止回報，禁 commit。
- 需改動 basename 分支或 glob 分支方能成立 → 停止回報（判定為範圍不足，改由 SKAD 改票）。
- `git diff --name-only` 超出 ALLOWED → 停止回報。
- **[標配]** `git check-ignore -v .claude/skills/qa/scripts/verify_checklist.sh` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** `token_matches_file()` 內含以 `/` 結尾 token 之前綴分支，且 basename 分支字面不變。
- [x] **[行為性]** given token `…/rgr-auto/` 與 file `…/rgr-auto/cases/c1.json`，expect self-test hit；given file `…/rgr-auto-sibling/x.json`，expect miss。
- [x] **[行為性]** self-test 既有案例全 PASS（附完整輸出 receipt）— receipt 見下方
- [x] ~~assembleDebug PASS~~ (N/A: 無 app/ 變更)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — `git diff --name-only HEAD` 僅 `.claude/skills/qa/scripts/verify_checklist.sh`

## Receipt: self-test 完整輸出（commit c73e6ba）
```
── token_matches_file() self-test ──
  ✅ [hit] path-glob 正 · ALLOWED 深層路徑命中
  ✅ [hit] path-glob 正 · FORBIDDEN 萬用字元攔截
  ✅ [miss] path-glob 負 · 不同根目錄不得命中
  ✅ [miss] path-glob 負 · 後綴不符不得命中
  ✅ [hit] path 字面 正 · 無萬用字元 token 精確命中
  ✅ [miss] path 字面 負 · 同目錄他檔不得命中
  ✅ [hit] basename 正 · DEFAULT_FORBIDDEN 品類
  ✅ [miss] basename 負 · 前綴相同不得誤命中
  ✅ [hit] pipeline-artifact 正 · PROGRESS_ARCHIVE 豁免命中
  ✅ [miss] pipeline-artifact 負 · 豁免不得外溢至衍生檔名
  ✅ [hit] 目錄型 token 正 · 巢狀檔以路徑前綴命中
  ✅ [miss] 目錄型 token 負 · 前綴相鄰目錄不得誤命中
  ✅ [0] pa_exemption_applies 正 · 小規模同步 delta=12 → 豁免成立
  ✅ [1] pa_exemption_applies 負 · 大規模覆寫 delta=200 → 豁免失效
  ✅ [0] pa_exemption_applies 邊界 · delta=40（=MAX_LINES）→ 豁免成立
  ✅ [1] pa_exemption_applies 邊界 · delta=41（=MAX_LINES+1）→ 豁免失效
  ✅ [NA] maxlines_verdict · MAX_LINES=N/A → NA（安全票，行數閘不適用）
  ✅ [MALFORMED] maxlines_verdict · 非法格式 '10O' → MALFORMED
  ✅ [OVER] maxlines_verdict · 實測 653 > 估算 100 → OVER（非退件）
  ✅ [OK] maxlines_verdict · 實測 653 ≤ 估算 800 → OK
── self-test: 20 passed, 0 failed ──
```
