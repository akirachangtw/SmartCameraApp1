# INFRA-SV-ZERODIFF-01: structural_verify.sh Check B 零 diff 三條件宣告制 + 全管線零 diff 相容性掃描 + decommission.md 回填

**簽發日期**: 2026-07-16
**簽發者**: Claude
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review（.sh 行為變更 = 治理票）
**前置依賴**: INFRA-HEADROOM-DECOMMISSION-01 之 CSO 裁決 A+B 並行（本票即 B；A 個案豁免之承接票號 = 本票）

> 檔案治理/來源說明：事件支撐 = INFRA-HEADROOM-DECOMMISSION-01 R1 STOP（2026-07-15）：`DIFF_REF==DIFF_END=c68d69b` 為 CSO 裁決條件 3 顯式授權之合法零 diff，但 `structural_verify.sh` Check B 寫死「REPORTED_FILES 空 → 一律 FAIL」，未為合法空 diff 建模；報告端偽造 bullet 又被 Check A（ghost claim）反咬——證實此為腳本案例類型缺口，非報告缺漏。模式級根因（第三次引爆，同 GAP-11 L1、HEADROOM PROGRESS.md 衝突）：裁決創造管線世界模型外的新狀態類型，下游閘門連環引爆——故本票 MUST 含全管線掃描，不得只修 Check B。

## Verify Config
TICKET_ID=INFRA-SV-ZERODIFF-01
CREATED=2026-07-16
PRESET=refactor
ALLOWED=.claude/skills/qa/scripts/structural_verify.sh,.claude/rules/decommission.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/PROGRESS.md,tools/cross_review.sh,tools/verify_checklist.sh,.claude/agents/cross-reviewer.md,.claude/rules/cross-review.md
MAX_LINES=150
DIFF_REF=<執行前 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: Check B 三條件宣告制
- **檔案**: `.claude/skills/qa/scripts/structural_verify.sh`（Check B，約 :90-110；路徑經 Akira 2026-07-19 裁決 A 修訂，原票面誤植 `tools/`）
- **現值**: `REPORTED_FILES` 解析為空 → 一律 FAIL（missing or empty '## Files Changed'）
- **目標值**: 增零 diff 分支，**三條件缺一即維持 FAIL**：
  1. 機械前提：`DIFF_REF == DIFF_END` **且** `git diff --name-only DIFF_REF..DIFF_END` 實測為空
  2. 顯式宣告：sv_report.md 之 `## Files Changed` 含 token **`ZERO-DIFF-DECLARED`** 且同行附授權依據引用（裁決/票面條款出處）
  3. 三者齊備 → PASS，pre-check 輸出 MUST 標注「零 diff 宣告輪」；宣告 token 存在但 `git diff` 非空 → **FAIL（防反向濫用：宣告不得掩蓋實際變更）**
- **設計原則**: 零 diff 是被授權且被宣告的狀態，不是默認狀態（與 EXCLUDED_PAYLOAD 立法目的同源：豁免的是明示對象，不是免檢權）
- **追加修正**（實作中發現）：零 diff PASS 分支原始實作於 `--post` 模式下會直接 `exit 0`，略過 Check D/E/F/G（sv_report 五段結構／Cross-Review PASS receipt／PROGRESS.md 格式／REACHABILITY token）；已改為與既有非空 diff 路徑一致的 `POST_MODE` fall-through 邏輯，`--post` 呼叫時零 diff PASS 後仍會執行 D/E/F/G，非空 diff 路徑行為零變更（Hard Constraint 1 verified）
- [x] 完成


### AI-2: 全管線零 diff 相容性掃描（唯讀清點，不得夾帶修改）
- **範圍**: `cross_review.sh` payload 生成、Codex 語意審查輸入、Traycer 回審、/qa Step 0–4.5——逐節點清點「diff 必非空」之隱含假設
- **附加問**（Akira 2026-07-19 增列，與零 diff 同屬「管線世界模型外的狀態類型」家族）：docs-only／治理票（純 `.md`/`.sh` 治理、無 `.kt`/`.xml` 觸碰）之 `/sv` 適用性——現行 `/sv` 是否對此類票隱含假設「必有語意可審之產物 diff」，若有即列入同一結論表
- **現值**: 無此清單；零 diff 票、docs-only／治理票每過一道閘之行為均未知
- **目標值**: 產出結論表（節點 | 空 diff／docs-only 行為 | 是否需修 | 承接票建議），覆蓋原四節點 + 本附加問；發現需修改之其他 `.sh` → **僅登記待開票，MUST NOT 本票夾帶**（HC2）
- **結論表已寫入** `.claude/rules/decommission.md`；發現 1 項需修（`/qa` Step 4.5 Auto Commit 對真零 diff 無 `--allow-empty` 處理）→ 已登記待開票（暫擬 `INFRA-QA-COMMIT-EMPTYDIFF-01`），未本票夾帶修復
- [x] 完成

### AI-3: decommission.md 回填「退場票型管線相容性清單」
- **檔案**: `.claude/rules/decommission.md`
- **現值**: 空殼（僅標題，82 bytes）
- **目標值**: 寫入首批內容：(a) AI-2 之四節點掃描結論表；(b) 零 diff 退場票操作要點（ZERO-DIFF-DECLARED 宣告格式 + 授權依據要求）；(c) 退場理由四要件模板（管線零引用 / 結構性失效 / 職能覆蓋 / git 可回復——INFRA-AGENTS-CR-01 先例）
- [x] 完成

## Hard Constraints
1. Check B 既有非空 diff 路徑之行為零變更（含 Check A ghost claim 咬合——回歸測試必驗）
2. 本票 `.sh` 觸碰僅限 `structural_verify.sh` 單檔；掃描發現之其他 `.sh` 缺口一律待開票承接
3. `ZERO-DIFF-DECLARED` 無授權依據引用時 MUST FAIL（裸宣告不豁免）

## Error Handling
- `structural_verify.sh` 實際路徑非 `tools/` → 定位實際檔案；若落於 ALLOWED 之外 → 停止回報附實際路徑（票面修訂屬 CSO 權限）
- Check B 現行邏輯與 :90-110 描述不符（已被異動）→ 停止回報附現況引文
- AI-2 掃描發現 cross_review.sh 對空 diff 直接崩潰（無法生成 payload）→ 記入結論表 + 待開票，本票照常完成（不夾帶修復）

## Acceptance Criteria
- [x] **[行為性]** 以 INFRA-HEADROOM-DECOMMISSION-01 回放（`DIFF_REF==DIFF_END=c68d69b` + sv_report 補 `ZERO-DIFF-DECLARED` 與依據）→ Check B PASS 且輸出標注零 diff 宣告輪
- [x] **[行為性]** 反向測試一：`ZERO-DIFF-DECLARED` 存在但 diff 非空 → FAIL
- [x] **[行為性]** 反向測試二：空 diff 但無宣告 token → 維持 FAIL（默認態不放行）
- [x] **[行為性]** 回歸：任一既有非空 diff 票之 sv_report 重跑 Check A + B → 判定與修改前一致
- [x] **[存在性]** decommission.md 含四節點結論表、宣告格式、退場四要件模板（grep 三段落均命中）
- [x] `assembleDebug` PASS
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案
