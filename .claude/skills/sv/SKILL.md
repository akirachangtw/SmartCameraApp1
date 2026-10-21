---
name: sv
description: 執行本地端無情法官驗收：語意審查 + 確定性編譯/Lint 驗證 + 檔案邊界防禦 + Codex Cross-Review 路由。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Prompt
你現在是本專案的「本地 Reviewer」。請依序執行以下 **四階段驗收**，任一階段失敗則該階段後續不續行並回報，終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置：

## 前置檢查：Ticket 已 Commit Gate

若 `tmp/verify_cmd.conf` 存在，先執行 `python .claude/scripts/gate_ledger.py check sv --ticket <TICKET_ID>`（`<TICKET_ID>` 取自該 conf，`grep/head/cut`，禁 `source`）；exit 0 續行，非 0 **不進階段 A**，退出碼映射見 `ssot-governance.md` 入口落點句；續行則執行 `git diff --name-only HEAD`，讀取 `ALLOWED` 後比對：
- 若結果含有任何 ALLOWED 中的實作檔案（即 Ticket 代碼尚未 commit）→ **不進階段 A**（可自修：回 `/qa` Step 4.5 完成 commit），終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置，回報：
  ```
  ⛔ SV BLOCKED: Ticket not committed after /qa
  提示：請先完成 /qa Step 4.5 Auto Commit，再執行 /sv
  ```
- 若 `tmp/verify_cmd.conf` 不存在 → ⚪ 跳過此檢查，繼續階段 A
- 若 `git diff --name-only HEAD` 為空（working tree clean）→ ✅ 通過，繼續階段 A

## 階段 A：確定性驗證 (Deterministic Gate)

1. **編譯驗證**：執行 `./gradlew assembleDebug 2>&1 | tail -30`
   - **MUST** 0 error。若失敗，擷取錯誤 Log 並**立即修復**，然後重新編譯。
   - 最多重試 2 次，第 3 次仍失敗則回報 `🔴 SV FAIL: Compile error` 並停止（該路徑終止），記 `step_limit`，終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置。

2. **Lint 檢查**：執行 `./gradlew lint 2>&1 | tail -20`
   - **MUST** 無新增 Warning（與上次基線比對）。
   - 新增的 Warning **SHOULD** 立即修復或在 `lint-baseline.xml` 中有明確豁免理由。

3. **檔案邊界檢查**：執行 `git diff --name-only HEAD`
   - 比對本次 Ticket 的授權修改範圍（參考 PROGRESS.md 的 Today's Tickets）。
   - 若修改了**未授權檔案**，**MUST** 立刻回報 `🚫 Scope Violation: [檔案名]`；終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置（還原本票越權變更屬可自修；需擴 scope 才 `HUMAN_REQUIRED(SCOPE)`）。
   - 若該修改是合理連帶（如 import 更新），說明理由。否則還原。

## 階段 A.5：Android 16KB ELF 對齊驗證 (16KB Page Size Gate)

> Android 15+ 強制要求 arm64-v8a / x86_64 的 .so 以 16KB 對齊。
> 此步驟為 🟡 警告級（不阻斷 SV 流程，但必須在 Release 前清零）。

3.5. **16KB ELF 對齊檢查**：
   - 前置確認：`app/build/outputs/apk/device/debug/app-device-debug.apk` 存在（需先執行 Step 1 assembleDebug）
   - 執行：`bash tools/check_elf_alignment.sh app/build/outputs/apk/device/debug/app-device-debug.apk 2>&1`
   - 判讀規則：
     - 所有 `lib/arm64-v8a/*.so` 與 `lib/x86_64/*.so` **SHOULD** 顯示 `ALIGNED`
     - `UNALIGNED` 項目 → 記錄至 SV Report 為 🟡 WARNING，列出具體 .so 名稱
     - `zipalign` 區段若顯示 `NOTICE`（build-tools < 35.0.0-rc3）→ 記為 ⚪ SKIP，不扣分
     - `ELF Verification Successful` → ✅ PASS
   - 若 `objdump` 或 `bash` 不在 PATH → 記為 ⚪ SKIP（工具不可用，不影響 SV 結論）

## 階段 B：語意審查 (Semantic Review)

4. **殘骸掃描 (Orphan Check)**：
   - 確認舊邏輯、隱藏的 feature flag、廢棄 import 是否已徹底刪除。
   - **MUST** 於**本 diff 新增行**搜尋 `// TODO`、`// FIXME`、`// HACK` 與佔位語意的 `return null`（如 `return null // implement later`）——零容忍；有明確契約語意的 `return null`（如 `rules/android-conventions.md` 明令的無效幾何尺寸）不在此列；既有行不屬本項，發現時可另報 ADVISORY。

5. **Mock 偵測 (Anti-Lazy Check)**：
   - 檢查是否有塞入無意義的預設值（如 `Size(36.dp)` 硬編碼、`Color.Gray` 佔位）。
   - 所有 None/Empty 狀態**MUST** 是 **透明/隱藏**，不可有預設可見物。

6. **UI 互斥 (State Exclusive)**：
   - 檢查 UI 渲染是否有雙重重疊風險。
   - `when` 分支 **MUST** exhaustive，`return` 防線是否穩固。
   - Compose `if/else` 層次是否正確處理 null 狀態。

7. **Guard Input Reachability（INFRA-REACH-01 / INFRA-REACH-02，依實際風險）**：
   對本 diff 新增的任何 Guard / gate / early-return（含 runtime hook／helper 的判定分支），只檢查該判定**實際讀取且影響結果**的 input（不列全 schema、不向 producer 上游無限追溯，一跳為止）：
   - 該 input 是否存在 production 寫入點（`檔案:行號`，可位於任何 repo 或 runtime 路徑；非預設初始化、非 reset-to-Idle 語意）？舉證以指令輸出（grep／read）為準。
   - 若任一被讀取的 input 無 non-idle production producer → 標記 **⚠️ UNREACHABLE GUARD**：對應 AC **MUST** 不得標 PASS，如實標為 **⚠️ PARTIAL**。是否檢查與是否升級依實際可達性風險，**不**以 AC 文字是否出現某欄位名稱或標記為準。
   - 報告宣稱「可達」而未附 producer 舉證 → 決策相關主張缺證據，Cross-Review 判 REPORT_FIX。補上有效舉證（`檔案:行號` 與指令輸出）可解除該缺證據 finding；改標 PARTIAL 僅更正報告主張，**不**自動滿足原 AC、**不**豁免必要品質、**不**放行整票，後續依原票與 Gate 處置（本項不自動強制另開 follow-up Ticket；`ssot-governance.md` Ticket Closure 紀律對「母票存在未閉環 AC」之修補票要求於收尾時獨立適用，不因本句免除）。豁免 **MUST** 僅得引用 Ticket 內**具名白名單**條款（比照 K4-39/K4-48 具名排除格式），**MUST** 禁止 context-gated 理由文（如「本情境下可接受」等臨場豁免）。
   - 結果（含「已確認可達」「UNREACHABLE-PARTIAL」判定）**MUST** 寫入 `.tmp/sv_report.md`
   - 輔助工具：Step 3.5.5 Dead-State 掃描輸出；參考 K3-Rule 26（多消費路徑掃描）

8. **Integration Test 存在閘（INFRA-REACH-02 G-5）**：
   凡本 diff 新增依賴外部輸入的行為（Guard／gate／early-return、reducer 分支、依 runtime state 的 fallback），或本 Ticket Acceptance Criteria 含 `[可達性]` 或 `[行為性]` 類型標記 → **MUST** 確認對應 integration／整合層測試存在且綠（標記是票面額外要求，不是唯一觸發條件）：
   - 於票面 ALLOWED 內的測試檔或本 diff 實際新增／修改的測試檔中搜尋，以驅動 production 路徑（非直注 fixture state）方式覆蓋該行為的測試檔案／方法；測試位置不固定於特定目錄（如 `app/src/test/`、`app/src/androidTest/`、`.claude/**/tests/` 皆可）。
   - 找不到對應測試 → **MUST** 標記 **⚠️ MISSING-INTEGRATION-TEST**，對應 AC **MUST** 不得標 PASS。
   - 本項為存在性檢查起步（確認測試「存在且綠」），測試斷言的行為深度由個別 Ticket 自行以更嚴格 AC 承載（不在本項強制範圍）。
   - 結果 **MUST** 寫入 `.tmp/sv_report.md`。

## 階段 C：架構合規 (Architecture Compliance)

7. **UDF 合規**：
   - 狀態只從 ViewModel → UI 單向流動，UI **MUST** 不直接修改 State。
   - **MUST** 沒有在 Composable 中直接呼叫 Repository 或 UseCase。

8. **SSOT 漂移檢查**：
   - 新增的類別名稱、變數名稱是否與 v3.3 SSOT 一致？
   - 如有偏離（如 SSOT 定義 `EdgeWarmthLayer` 但實作叫 `WarmGradientOverlay`），**SHOULD** 回報漂移。

## 階段 D：Cross-Review 路由 (v1.4 新增)

> 此階段在 SV Report 產出後**自動觸發**。
> 完整 cross-review 指南見 [CROSS_REVIEW.md](CROSS_REVIEW.md)（Layer 3 資源）。

9. **SV Report 輸出**：刪除舊資料的 `.tmp/sv_report.md` 後，將以上審查結果寫入 `.tmp/sv_report.md`（工作目錄根）。

   **SV Report payload disclosure 規則**（不可省略）：
   - `## Files Changed (payload)` — **MUST** 列出 `git diff --name-only DIFF_REF..DIFF_END` 的**全部**檔案（完整 payload；`tmp/verify_cmd.conf` 無 `DIFF_END` 時才以 `DIFF_REF..HEAD`）
     - `.claude/PROGRESS.md` 如果出現在 diff → **MUST** 列出，並在行尾標註 `← Pipeline Artifact`
   - `## Implementation Scope` — 只列 ALLOWED 實作檔（`tmp/verify_cmd.conf` 中的 `ALLOWED`）
   - `## Excluded Payload`（選填）— 已授權排除出 Codex diff 的跨票檔案，格式：
     ```
     ## Excluded Payload
     - AmbientConstants.kt — pre-approved constant; reviewed R1; CSO auth 2026-05-19
     ```
     每個 excluded 檔案 **MUST** 仍出現在 `## Files Changed`，否則 `structural_verify.sh` Check C FAIL
   - **MUST** 禁止只列 ALLOWED 而省略 payload 其他檔案，否則 `structural_verify.sh` Check B FAIL
   - **必備標題（字面，與 `structural_verify.sh` Check D 相同，於 Codex 前由 SV_REPORT_PREFLIGHT 檢查）**：`## Files Changed`、`## Implementation Scope`、`## Stage A`、`## Stage B`、`## Stage C`；缺任一 → REPORT_FIX（`cross_review.sh` exit 3，不呼叫 Codex）

10. **Cross-Review 自動路由**：
    - 檢查 `codex` CLI 是否可用：`command -v codex`
    - 檢查 SSOT context 是否存在：`.claude/skills/sv/ssot_context.md`
    - **若兩者皆備**：自動執行（優先使用 `DIFF_REF..DIFF_END` 鎖定範圍；缺 `DIFF_END` 且 HEAD 含非 ALLOWED 變更則停止）
      ```
      # 讀取 DIFF_REF / DIFF_END（禁止 source，使用 grep/cut）
      _DIFF_REF=$(grep "^DIFF_REF=" tmp/verify_cmd.conf 2>/dev/null | head -1 | cut -d'=' -f2-)
      _DIFF_END=$(grep "^DIFF_END=" tmp/verify_cmd.conf 2>/dev/null | head -1 | cut -d'=' -f2-)
      if [[ -n "$_DIFF_END" ]]; then
        _SCOPE="${_DIFF_REF}..${_DIFF_END}"
      elif [[ -n "$_DIFF_REF" ]]; then
        # DIFF_END 缺失：檢查 HEAD 是否含非 ALLOWED 變更（FUTURE_CONTAMINATION 風險）
        _ALLOWED=$(grep "^ALLOWED=" tmp/verify_cmd.conf 2>/dev/null | head -1 | cut -d'=' -f2-)
        _CONTAMINATED=0
        while IFS= read -r _f; do
          [[ -z "$_f" || "$_f" == ".claude/PROGRESS.md" ]] && continue
          echo "$_ALLOWED" | grep -qF "$(basename "$_f")" || { _CONTAMINATED=1; break; }
        done < <(git diff --name-only "${_DIFF_REF}..HEAD" 2>/dev/null || true)
        if [[ $_CONTAMINATED -eq 1 ]]; then
          echo "⛔ SV BLOCKED: DIFF_END 未設定，但 DIFF_REF..HEAD 含非 ALLOWED 變更（FUTURE_CONTAMINATION 風險）"
          echo "   修正: 在 tmp/verify_cmd.conf 設定 DIFF_END=<ticket-last-commit> 後重新執行 /sv"
          echo "   提示: 若剛完成 /qa Step 4.5，DIFF_END 應已自動寫入；確認 verify_cmd.conf 是否更新"
          exit 1
        fi
        _SCOPE="${_DIFF_REF}..HEAD"
        echo "⚠️ WARNING: DIFF_END 未設定，使用 DIFF_REF..HEAD（HEAD-moving scope）"
      fi
      if [[ -n "$_DIFF_REF" ]]; then
        bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md "${_SCOPE}"
      else
        bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md
      fi
      ```
      並將 Codex 回傳結果附加至 SV Report 末尾。
    - **契約落地反向提問（INFRA-REACH-01）**：Cross-Review prompt **MUST** 包含以下固定問句，強制審查跳出 diff 邊界：
      > 本 Ticket 是否新增任何依賴外部 state 為前提的 Guard / gate / 邏輯？
      > 該 state 是否在 diff 範圍之外有對應 production producer？
      > 若無，標記 missing-wiring 風險。

      結果須在 Cross-Review 報告中明確列出（即使無新 Guard 也須明確回答「無」）。
    - **退出碼對應行動**：
      - `exit 0` (PASS/ADVISORY) → ✅ 繼續 Traycer 終審
      - `exit 1` (BLOCKER) → 🔴 修程式碼後重新 /qa → /sv
      - `exit 3` (REPORT_FIX，未達閉環上限) → 📋 符合 report-only 閉環條件者依下方「REPORT_FIX report-only 閉環」處理；不符者修 `.tmp/sv_report.md` 後停止回報（不需重跑 /qa）
      - `exit 2` (ENV / FAIL BUDGET / `CHECKPOINT_REPORT_FIX_LIMIT` / `REPORT_FIX_ESCALATE`) → 步驟級事件：記 `step_limit`，檢查 `.tmp/cross_review_state/<ticket>`，終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置；`CHECKPOINT_REPORT_FIX_LIMIT` 為步驟級上限（非 BLOCKER、非程式碼修正重試），不得自動重跑
    - **Fail Budget**: `.tmp/cross_review_state/<TICKET_ID>` 記錄 Round 與 Last Verdict
      - R2 BLOCKER → ⚠️ Checkpoint 警告（腳本步驟級預算），終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置
      - R3 BLOCKER → ⛔ 強制停止 (exit 2)，步驟級上限，記 `step_limit`，終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置
      - `REPORT_FIX` 不遞增 `ROUND`（不扣 blocker budget）、遞增 `REPORT_FIX_COUNT`（上限 2）；`REPORT_FIX → REPORT_FIX → BLOCKER` 的第三次 exit 1 為一般 BLOCKER R1，**不是 REPORT_FIX 升級**
    - **REPORT_FIX report-only 閉環**（七條件 predicate 之**唯一來源**為 `ssot-governance.md` Transition Table，此處**不複製**）：①讀 exit 3 輸出之 `REPORT_FIX_SOURCE=`——`structural_preflight` 才可進入閉環；codex 來源不得使用 `REPORT_FIX_CLOSURE=1`，亦不進入下列 structural_preflight 專用閉環步驟；其修正、重審與停止依 `ssot-governance.md` §Autonomous Engineering Gate 及既有步驟預算處置；②見 `CHECKPOINT_REPORT_FIX_LIMIT` 或 `REPORT_FIX_ESCALATE`（exit 2）即停止閉環重跑，記 `step_limit`，終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置；③僅修改 `.tmp/sv_report.md`（僅限既有內容重排、必要標題補齊、既有事實之欄位格式校正；不得新增測試結果、commit、命令輸出、證據引用、完成宣稱等新事實）；④單跑 `bash .claude/skills/qa/scripts/structural_verify.sh .tmp/sv_report.md "<DIFF_REF>..<DIFF_END>"` 至 exit 0；⑤以 `REPORT_FIX_CLOSURE=1 bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md "<DIFF_REF>..<DIFF_END>"` 重跑；⑥同票同一 state 週期（PASS／ADVISORY 清 state 前）最多自動閉環 2 次，第 3 次由腳本輸出 `CHECKPOINT_REPORT_FIX_LIMIT`
    - **若 codex 不可用**：在 SV Report 末尾附加提示：
      ```
      ⚠️ Codex CLI 不可用 — 請手動執行 cross-review 或直接提交 Traycer 終審。
      手動指令：bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md
      ```
    - **若 ssot_context.md 不存在**：降級為純 SV Report 驗證（Layer 2 only），在 Report 中標記：
      ```
      ⚠️ ssot_context.md 不存在 — Codex cross-review 降級為純報告驗證（無架構重審）。
      ```
    - **輪次二元帳**：Cross-Review 存在兩套獨立輪次計數，回件時 **MUST** 同時標示：
      - **機械輪次**：`.tmp/cross_review_state/<TICKET_ID>` 之 `CURRENT_ROUND`（BLOCKER 輪次，REPORT_FIX 不遞增；腳本自增，producer 明確）
      - **邏輯輪次**：人工判例帳，SSOT 在 vault（非本檔、非狀態檔）
      - 回件格式：`R{機械}/L{邏輯}`（例：`R2/L1`）
      - 二帳不一致時**不自動對齊**，以 vault 邏輯帳為準並於回件註明差異

## Step 10: Ticket Done 標記

Cross-Review PASS（exit 0）後，標記對應 Ticket 為 ✅ Done：

1. 讀取 `tmp/verify_cmd.conf`，取得 `TICKET_ID`
2. 執行 bash 標記（不載入全文至 context）：

   ```bash
   TICKET_ID=$(grep "^TICKET_ID=" tmp/verify_cmd.conf | head -1 | cut -d= -f2-)
   sed -i "s/| ${TICKET_ID} /| ${TICKET_ID} ✅ Done /" .claude/PROGRESS_ARCHIVE.md
   ```

3. 同步更新 `PROGRESS.md` Header 與 Sprint Health 中的 Ticket 狀態
4. 回報：`✅ PROGRESS_ARCHIVE.md: [TICKET_ID] marked Done`

若 `tmp/verify_cmd.conf` 不存在或 `TICKET_ID` 為空 → `⚠️ Done 標記跳過：無 TICKET_ID`

## Step 10.5: Session Note 清理

Cross-Review PASS 且 Done 標記完成後：

1. 讀取 `PROGRESS.md` `## Next Actions` 段落
2. 計算 `>` 開頭的 Session completion notes 總數
3. 若超過 5 筆 → 將最舊的 N-3 筆移至 `PROGRESS_ARCHIVE.md` 底部
4. 回報：`🧹 PROGRESS.md: [N] old session notes archived`

若 `## Next Actions` 段落不存在或 `>` 行數 ≤ 5 → `⚪ Session Note 清理跳過：[N] 筆 ≤ 5`

## Step 11: Closeout Auto-Chain

下列任一成立後 **MUST AUTO-CHAIN**（①SV 完成：Cross-Review 為 PASS／ADVISORY／SKIPPED，或 REPORT_FIX 觸 `CHECKPOINT_REPORT_FIX_LIMIT` 而無終局 verdict——`reviews.codex` 填 `N_A`、實際 verdict 史與未決 finding 如實落 checkpoint，不寫 PASS；②低風險免 QA／SV 票〔判準唯一依 `ssot-governance.md` Transition Table「低風險免 QA／SV」列〕於 Ticket DONE 且票面必要驗證完成、並已先執行 `python .claude/scripts/gate_ledger.py check commit --ticket <TICKET_ID>`（exit 0 才續行）再以 `[<TICKET_ID>]` 前綴提交實作之後——僅 Read 並執行本節第 1–5 項，**MUST NOT** 呼叫 `Skill(sv)`／`Skill(qa)` 或假填 QA／SV PASS；已確認 BLOCKER 不在此列，見第 6 項），依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 與 §Ticket Closure 紀律執行 Agent closeout 至 `REVIEW`，並於其後 `MUST AUTO-CHAIN` 以 `Skill(ticket-author)`、args `close <TICKET_ID>` 實際調用 TKA close（第 4 項；該表 close 列）。本節為引用，**MUST NOT** 複製四要件、GO predicate 或 Traycer 判定表全文。

1. **入口冪等**：先 `registry_list` 讀現值，命中 §Ticket Runtime Transition Table「最小冪等回傳」條件即 `MUST RETURN`（此路徑**不**呼叫 `check`）。
   - 入口冪等判定之後、第一個 registry／票檔寫入之前，**MUST** 先執行 `python .claude/scripts/gate_ledger.py check closeout --ticket <TICKET_ID>`（`<TICKET_ID>` 取自目前正式票上下文，並與票檔 `TICKET_ID`、Registry ID 核對；`tmp/verify_cmd.conf` 可重用，但不得為低風險路徑必要依賴，其 `TICKET_ID` 與目前票不一致時 **MUST NOT** 採用）；exit 0 續行，非 0 **不寫任何 registry／不搬移票檔**，退出碼映射見 `ssot-governance.md` 入口落點句。票在 `deliver/` 而冊面 `file` 仍指 `Pending/` 者不 RETURN、**不呼叫** `check`，交既有 Closure Gate 與「矛盾停止」，不新增恢復流程。
2. **敘事寫入**：走 `registry_set_checkpoint`（`detail_content` 承載收尾實錄敘事、`checkpoint_data` 承載結構化欄），**MUST NOT** 用 `registry_append_note`；且 **MUST** 置於 `registry_set_status` **之後**（`set_status` 每次呼叫清空 checkpoint）。寫後 **MUST** read-back 驗證。
3. **收束後成果校正**：第 1 項未命中 `MUST RETURN`、且第 2 項寫入與 read-back 完成（本次呼叫實際完成收束）後，是否呼叫 `Skill(projman)` align 唯一依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 之「Agent closeout 於本次呼叫實際完成」列；判準、授權與保存規則以該列為唯一來源，此處**不複製**，亦不複製成果文件定位邏輯。
4. **自主收束**：第 3 項之後（無論 align 是否觸發）**MUST AUTO-CHAIN** 以 Skill 工具調用 `Skill(ticket-author)`，args 為 `close <TICKET_ID>`（`<TICKET_ID>` 同第 1 項，一次）；第 1 項命中「已收束重入」且 `lifecycle = REVIEW` 者亦同。**MUST NOT** 以 Bash／Registry 工具手動模仿 close 步驟、自填 verdicts 後直接寫入 `CLOSED`、inline 自答 reviewer 或自行宣稱驗收完成；Skill 調用失敗或不可達 → 如實回報 close 未完成（票保留 `REVIEW`），不假報收束。close 的取證、reviewer、轉態與 read-back 由 `ticket-author` 的 `Mode: close` 規範，此處**不複製**。
5. **最終覆蓋確認**：第 4 項的 close 結果已知後，是否為本輪最後一張票並呼叫 `Skill(projman)` 做最終覆蓋確認，唯一依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 之「本輪最後一張票的 `/ticket-author close` 結果已知後」列；判準、授權與保存規則以該列及 `projman` Skill 為唯一來源，此處**不複製**。
6. **BLOCKER 交接**：Cross-Review 或 SV 判已確認 BLOCKER 時，停止不安全操作與錯誤交付，**不**寫 `REVIEW`、不偽填 PASS；仍 **MUST AUTO-CHAIN** 以 `Skill(ticket-author)`、args `close <TICKET_ID>` 將 blocker 與證據交 TKA 作唯讀判定（TKA 回 `NOT_CLOSED`／blocker，lifecycle 不變、零寫入）。

## 回報格式

```
=== SV Report ===
📋 Ticket: [Ticket ID]
🔨 Compile: ✅ PASS (0 errors, X warnings)
🔍 Lint: ✅ PASS (0 new warnings)
📁 Scope: ✅ PASS (modified: [file list])
📦 16KB ELF: ✅ ALIGNED / 🟡 [N] UNALIGNED .so / ⚪ SKIP (tool unavailable)
🧹 Orphans: ✅ CLEAN
🎭 Mock: ✅ CLEAN
🏗️ UDF: ✅ COMPLIANT
📐 SSOT: ✅ ALIGNED
🔍 Cross-Review: ✅ PASS / 💡 ADVISORY / 📋 REPORT_FIX / 🔴 BLOCKER / ⚠️ SKIPPED (codex unavailable)

→ MUST AUTO-CHAIN: Step 11 Closeout
📊 Status: ~[X]K used | [🟢 繼續 / 🟡 留意 / 🔴 建議 /compact]（健康度依 Turns 判定）
Turns: [N] | Code: ~[M] lines
Suggestion: [建議]
Token 估算基準
```

若有任何失敗項：
```
=== SV Report ===
📋 Ticket: [Ticket ID]
🔨 Compile: 🔴 FAIL — [錯誤摘要]
📦 16KB ELF: 🟡 WARNING — [UNALIGNED .so 清單] (Release 前必須清零)
🔍 Cross-Review: 🔴 BLOCKER — [問題清單] / 📋 REPORT_FIX — [sv_report 段落]
...
→ 🚫 SV 未通過。已自動修復 X 項，仍有 Y 項未通過，終點 → 依 `ssot-governance.md` §Autonomous Engineering Gate 處置（續作或 `HUMAN_REQUIRED`）。
📊 Status: ~[X]K used | [狀態 emoji]（健康度依 Turns 判定）
Turns: [N] | Code: ~[M] lines
Suggestion: [建議]
Token 估算基準
```

**未勾項全盤點區塊（強制）**：回報 **MUST** 於上述模板末尾附加本區塊，AC 與 Action Item **雙軌**逐項列舉；全部已勾時**仍須輸出本區塊**並寫「無未勾項」——空區塊之缺席與「忘了寫」無法區辨。每項去向 **MUST** 為以下封閉三值之一，**MUST NOT** 增設「其他」或自由文字選項：

```
## 未勾項全盤點
- AC: 無未勾項
- Action Item: 無未勾項
```

或有未勾項時：

```
## 未勾項全盤點
- AC [項目名稱]: 轉票<票號> / 豁免<理由字面> / 裁決中<待裁者>
- Action Item [項目名稱]: 轉票<票號> / 豁免<理由字面> / 裁決中<待裁者>
```

## Session 健康度

參照 CLAUDE.md §Session Health 規則（以 Turns 判定，非 Context 佔比）。在 Report 末尾附上上述📊摘要。
📊 只顯示已用 token 絕對值，不顯示佔比：1M 模型佔比因 upstream bug #63447 不可靠（詳見 `status/SKILL.md`）。
