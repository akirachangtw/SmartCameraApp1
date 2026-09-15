---
name: sv
description: 執行本地端無情法官驗收：語意審查 + 確定性編譯/Lint 驗證 + 檔案邊界防禦 + Codex Cross-Review 路由。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Prompt
你現在是本專案的「本地 Reviewer」。請依序執行以下 **四階段驗收**，任一階段失敗則停止並回報：

## 前置檢查：Ticket 已 Commit Gate

執行 `git diff --name-only HEAD`，若 `tmp/verify_cmd.conf` 存在，讀取 `ALLOWED` 後比對：
- 若結果含有任何 ALLOWED 中的實作檔案（即 Ticket 代碼尚未 commit）→ **MUST** 停止整個 SV 流程，回報：
  ```
  ⛔ SV BLOCKED: Ticket not committed after /qa
  提示：請先完成 /qa Step 4.5 Auto Commit，再執行 /sv
  ```
- 若 `tmp/verify_cmd.conf` 不存在 → ⚪ 跳過此檢查，繼續階段 A
- 若 `git diff --name-only HEAD` 為空（working tree clean）→ ✅ 通過，繼續階段 A

## 階段 A：確定性驗證 (Deterministic Gate)

1. **編譯驗證**：執行 `./gradlew assembleDebug 2>&1 | tail -30`
   - **MUST** 0 error。若失敗，擷取錯誤 Log 並**立即修復**，然後重新編譯。
   - 最多重試 2 次，第 3 次仍失敗則回報 `🔴 SV FAIL: Compile error` 並停止。

2. **Lint 檢查**：執行 `./gradlew lint 2>&1 | tail -20`
   - **MUST** 無新增 Warning（與上次基線比對）。
   - 新增的 Warning **SHOULD** 立即修復或在 `lint-baseline.xml` 中有明確豁免理由。

3. **檔案邊界檢查**：執行 `git diff --name-only HEAD`
   - 比對本次 Ticket 的授權修改範圍（參考 PROGRESS.md 的 Today's Tickets）。
   - 若修改了**未授權檔案**，**MUST** 立刻回報 `🚫 Scope Violation: [檔案名]`。
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
   - **MUST** 搜尋 `// TODO`、`// FIXME`、`// HACK`、`return null`——零容忍。

5. **Mock 偵測 (Anti-Lazy Check)**：
   - 檢查是否有塞入無意義的預設值（如 `Size(36.dp)` 硬編碼、`Color.Gray` 佔位）。
   - 所有 None/Empty 狀態**MUST** 是 **透明/隱藏**，不可有預設可見物。

6. **UI 互斥 (State Exclusive)**：
   - 檢查 UI 渲染是否有雙重重疊風險。
   - `when` 分支 **MUST** exhaustive，`return` 防線是否穩固。
   - Compose `if/else` 層次是否正確處理 null 狀態。

7. **Guard Input Reachability（INFRA-REACH-01 / INFRA-REACH-02 機械分級）**：
   對本 diff 新增的任何 Guard / gate / early-return，逐一確認依賴的 input：
   - 在 `app/src/main/` 是否存在 production 寫入點（非預設初始化、非 reset-to-Idle 語意）？
   - 若任一 input 無 non-idle production producer → 標記 **⚠️ UNREACHABLE GUARD**
   - **機械分級（INFRA-REACH-02）**：UNREACHABLE GUARD **且** 該 guard/欄位名稱出現於本 Ticket AC 文字中 → 判定 **MUST** 升級為 **📋 REPORT_FIX 機械級**（非可自選 WARNING/PARTIAL）；對應 AC **MUST** 不得標 PASS/PARTIAL 自選，**MUST** 停用 .tmp/sv_report.md 修報告即放行。豁免 **MUST** 僅得引用 Ticket 內**具名白名單**條款（比照 K4-39/K4-48 具名排除格式），**MUST** 禁止 context-gated 理由文（如「本情境下可接受」等臨場豁免）。
   - UNREACHABLE GUARD 但欄位未出現於本票 AC 文字 → 沿用既有語意豁免：對應 AC **MUST** 不得標 PASS，改為 **⚠️ PARTIAL**，並開後續 follow-up Ticket
   - 結果（含「已確認可達」「UNREACHABLE-PARTIAL」或「UNREACHABLE-REPORT_FIX」判定）**MUST** 寫入 `.tmp/sv_report.md`
   - 輔助工具：Step 3.5.5 Dead-State 掃描輸出；參考 K3-Rule 26（多消費路徑掃描）

8. **Integration Test 存在閘（INFRA-REACH-02 G-5）**：
   凡本 Ticket Acceptance Criteria 含 `[可達性]` 或 `[行為性]` 類型標記 → **MUST** 確認對應 integration/ViewModel 層測試存在且綠：
   - 於 `app/src/test/` 或 `app/src/androidTest/` 中搜尋以驅動 production 路徑（非直注 fixture state）方式覆蓋該 AC 的測試檔案/方法。
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
   - `## Files Changed (payload)` — **MUST** 列出 `git diff --name-only DIFF_REF..HEAD` 的**全部**檔案（完整 payload）
     - `.claude/PROGRESS.md` 如果出現在 diff → **MUST** 列出，並在行尾標註 `← Pipeline Artifact`
   - `## Implementation Scope` — 只列 ALLOWED 實作檔（`tmp/verify_cmd.conf` 中的 `ALLOWED`）
   - `## Excluded Payload`（選填）— 已授權排除出 Codex diff 的跨票檔案，格式：
     ```
     ## Excluded Payload
     - AmbientConstants.kt — pre-approved constant; reviewed R1; CSO auth 2026-05-19
     ```
     每個 excluded 檔案 **MUST** 仍出現在 `## Files Changed`，否則 `structural_verify.sh` Check C FAIL
   - **MUST** 禁止只列 ALLOWED 而省略 payload 其他檔案，否則 `structural_verify.sh` Check B FAIL

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
      - `exit 3` (REPORT_FIX) → 📋 修 .tmp/sv_report.md 後重新執行 cross_review（不需重跑 /qa）
      - `exit 2` (ENV / FAIL BUDGET) → ⛔ 人工介入，檢查 `.tmp/cross_review_state/<ticket>`
    - **Fail Budget**: `.tmp/cross_review_state/<TICKET_ID>` 記錄 Round 與 Last Verdict
      - R2 BLOCKER → ⚠️ Checkpoint 警告，等待裁決
      - R3 BLOCKER → ⛔ 強制停止 (exit 2)，需人工審查
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
      - **機械輪次**：`.tmp/cross_review_state/<TICKET_ID>` 之 `CURRENT_ROUND`（腳本自增，producer 明確）
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

SV＋Codex PASS（或 RAPID MODE 豁免）後 **MUST AUTO-CHAIN**，依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 與 §Ticket Closure 紀律執行 Agent closeout 至 `REVIEW`。本節為引用，**MUST NOT** 複製四要件、GO predicate 或 Traycer 判定表全文。

1. **入口冪等**：先 `registry_list` 讀現值，命中 §Ticket Runtime Transition Table「最小冪等回傳」條件即 `MUST RETURN`。
2. **敘事寫入**：走 `registry_set_checkpoint`（`detail_content` 承載收尾實錄敘事、`checkpoint_data` 承載結構化欄），**MUST NOT** 用 `registry_append_note`；且 **MUST** 置於 `registry_set_status` **之後**（`set_status` 每次呼叫清空 checkpoint）。寫後 **MUST** read-back 驗證。

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
→ 🚫 SV 未通過。已自動修復 X 項，仍有 Y 項需人工介入。
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
