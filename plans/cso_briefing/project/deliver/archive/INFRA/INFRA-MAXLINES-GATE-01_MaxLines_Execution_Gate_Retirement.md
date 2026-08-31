# INFRA-MAXLINES-GATE-01: verify_checklist.sh MAX_LINES execution gate 退役（量尺校正 + 四類值一律不阻斷）

**簽發日期**: 2026-08-13
**簽發者**: CSO（Akira 2026-08-13 對話裁決授權簽發）
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（治理／腳本票，依 governance-ticket-traycer-exempt 先例）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-TPL-V33-02` —— TPL v3.5「MAX_LINES 量尺定義與性質」為本票判準來源（量尺＝insertions＋deletions、範圍＝production-only、四類值不阻斷、交付阻斷權唯五類）
- `coordination_dependency`: `INFRA-MAXLINES-EVIDENCE-01` —— 量測 evidence record 綁定層承接本票之遙測輸出形狀

## Verify Config
TICKET_ID=INFRA-MAXLINES-GATE-01
CREATED=2026-08-13
PRESET=refactor
ALLOWED=verify_checklist.sh
FORBIDDEN=Unified_Ticket_Template_v3.md,TICKET_REGISTRY.json,PROGRESS.md,SKILL.md
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-08-13 17:17

**MAX_LINES=N/A 附註**（R3）：本票 AI 直接改動 `/qa` Step 0 之守門判定路徑（fail-closed gate 之 flag 設定與 exit code），失敗形狀枚舉為審查發現式，不可先驗估算。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **動作**: 逐字確認下列現值命中數與票面所載相符：
  - `if [[ "$ADDED" -gt "$MAX_LINES" ]]; then` —— 唯一（1 次）
  - `⛔ BLOAT: +${ADDED} 行（上限: ${MAX_LINES}）` —— 唯一（1 次）
  - `✅ +${ADDED} 行（上限: ${MAX_LINES}）` —— 唯一（1 次）
  - `s+=$1} END {print s+0}` —— **4 次**（L406/409 ALLOWED 迴圈、L416/418 全量分支；L406 與 L409 為同字面重複行，**MUST** 以 `CONF_MODE` 分支上下文區辨，禁憑單行字面 str_replace）
  - `s+=$1+$2} END {print s+0}` —— **2 次**（L65/67，`pa_delta` 既有實作，**FORBIDDEN 觸碰**，僅作風格對照）
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [ ] 完成

### AI-1: 量尺校正為 insertions ＋ deletions（R1）
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **現值**: `[0.3] 新增行數` 區四處 awk 累加皆為 `s+=$1`（僅 insertions）——ALLOWED 迴圈 L406/409、全量分支 L416/418
- **目標值**: 四處一律改為 `s+=$1+$2`（insertions ＋ deletions），與既有 `pa_delta`（L65/67）同一 idiom
- **註**: `--numstat` 對 binary 檔輸出 `-  -`，awk 對非數值 coerce 為 0，已實測混列（`-/-` ＋ `12/5`）得 17，不需額外 binary 分支
- [ ] 完成

### AI-2: 閘門退役——四類值一律不阻斷（終局裁決 2026-08-13）
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **現值**:
  ```bash
  if [[ "$ADDED" -gt "$MAX_LINES" ]]; then
    echo "  ⛔ BLOAT: +${ADDED} 行（上限: ${MAX_LINES}）"
    FAIL=1
  else
    echo "  ✅ +${ADDED} 行（上限: ${MAX_LINES}）"
  fi
  ```
- **目標值**: 四分支遙測輸出，**任一分支 MUST NOT 設 `FAIL=1`**：
  ```bash
  if [[ "$MAX_LINES" == "N/A" ]]; then
    echo "  ⚪ MAX_LINES=N/A（安全票，行數閘不適用）— 實測 ${ADDED} 行（估算校準用）"
  elif [[ ! "$MAX_LINES" =~ ^[0-9]+$ ]]; then
    echo "  ⚠️ MAX_LINES 值非法（'${MAX_LINES}'）— 遙測跳過，票面格式待 ticket lint 修；實測 ${ADDED} 行"
  elif [[ "$ADDED" -gt "$MAX_LINES" ]]; then
    echo "  ⚠️ 實測 ${ADDED} 行 > 估算 ${MAX_LINES}（超出 $((ADDED - MAX_LINES)) 行）— 非退件，供估算校準"
  else
    echo "  ✅ 實測 ${ADDED} 行（估算 ${MAX_LINES}）"
  fi
  ```
- **關鍵**: `N/A` **MUST** 走字串比對顯式跳過，**MUST NOT** 落入算術比較（現況 `[[ 653 -gt "N/A" ]]` → `division by 0` 後 fail-open 假 PASS，已實測）
- [ ] 完成

### AI-3: self-test 補負向覆蓋
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`
- **現值**: `self_test()` 內 `_expect_num` 四例僅覆蓋 `pa_exemption_applies`（L112–115），MAX_LINES 四分支零覆蓋
- **目標值**: 將 AI-2 之四分支判定抽為可測函數（如 `maxlines_verdict <max_lines> <added>` 回傳 `NA|MALFORMED|OVER|OK`），並於 `self_test()` 補 ≥4 例：`N/A` → `NA`、`10O` → `MALFORMED`、`653/100` → `OVER`、`653/800` → `OK`；四例 **MUST** 各自斷言**不設 FAIL**
- [ ] 完成

## Hard Constraints
1. **MUST NOT** 觸碰 `PIPELINE_ARTIFACT_MAX_LINES`（L24）及其判定 `pa_exemption_applies`（L59）——同名不同物，屬 Pipeline Artifact 豁免界線，非本票標的。
2. **MUST NOT** 移除或弱化其他 `FAIL=1` 來源（`GENUINE_VIOLATION`／`STALE`／`FUTURE_CONTAMINATION`／FORBIDDEN 命中）——交付阻斷權五類不受本票影響。
3. **MUST NOT** 刪除 MAX_LINES 之讀取與量測（L128 預設值、L147–148 conf 讀取、`[0.3]` 量測）——退役的是 gate，不是遙測。
4. **MUST NOT** 於同一 commit 混入 `.md` 文件變更（CLAUDE.md 鐵律 3）。

## Error Handling
- AI-0 任一命中數不符 → 停止回報
- `s+=$1` 四處中任一處所在 `CONF_MODE` 分支結構與票面所載不符 → 停止回報，禁憑單行字面盲改
- 修改後 `bash verify_checklist.sh --self-test` 非零退出 → 停止回報
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）

## Acceptance Criteria
- [ ] **[存在性]** `grep -c 's+=$1+$2' verify_checklist.sh` ≥ 6（原 L65/67 兩處 ＋ 本票四處）
- [ ] **[存在性]** `grep -c 'FAIL=1' verify_checklist.sh` 較 DIFF_REF 基線**減 1**（BLOAT 分支之 `FAIL=1` 消失，其餘來源不變）
- [ ] **[行為性]** `MAX_LINES=N/A` 時 stderr **無** `division by 0`，輸出含 `⚪`，且 exit code 不因此為非零
- [ ] **[行為性]** 合法超額（實測 > 估算）時輸出含 `⚠️`、**不含** `⛔ BLOAT`，且 exit code 不因此為非零
- [ ] **[行為性]** 非法格式（如 `10O`）與空值時輸出含 `⚠️`，且 exit code 不因此為非零
- [ ] **[行為性]** `bash verify_checklist.sh --self-test` PASS，且含 AI-3 新增 ≥4 例
- [ ] ~~`assembleDebug` PASS~~ (N/A: 無 .kt/.xml 變更)
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案

## 完成後動作
- **MUST NOT** 自行搬移票檔（Pending→deliver 由 CSO 收束時執行）
- **MUST NOT** 自行寫入 `TICKET_REGISTRY.json`（單一寫入者為 Desktop 治理 session，concurrency.md §5）
