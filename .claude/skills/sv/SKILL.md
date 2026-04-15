---
disable-model-invocation: false
---
# Description
執行本地端無情法官驗收：語意審查 + 確定性編譯/Lint 驗證 + 檔案邊界防禦。

# Prompt
你現在是本專案的「本地 Reviewer」。請依序執行以下 **三階段驗收**，任一階段失敗則停止並回報：

## 階段 A：確定性驗證 (Deterministic Gate)

1. **編譯驗證**：執行 `./gradlew assembleDebug 2>&1 | tail -30`
   - 必須 0 error。若失敗，擷取錯誤 Log 並**立即修復**，然後重新編譯。
   - 最多重試 2 次，第 3 次仍失敗則回報 `🔴 SV FAIL: Compile error` 並停止。

2. **Lint 檢查**：執行 `./gradlew lint 2>&1 | tail -20`
   - 必須無新增 Warning（與上次基線比對）。
   - 新增的 Warning 必須立即修復或在 `lint-baseline.xml` 中有明確豁免理由。

3. **檔案邊界檢查**：執行 `git diff --name-only HEAD`
   - 比對本次 Ticket 的授權修改範圍（參考 PROGRESS.md 的 Today's Tickets）。
   - 若修改了**未授權檔案**，立刻回報 `🚫 Scope Violation: [檔案名]`。
   - 若該修改是合理連帶（如 import 更新），說明理由。否則還原。

## 階段 A.5：Android 16KB ELF 對齊驗證 (16KB Page Size Gate)

> Android 15+ 強制要求 arm64-v8a / x86_64 的 .so 以 16KB 對齊。
> 此步驟為 🟡 警告級（不阻斷 SV 流程，但必須在 Release 前清零）。

3.5. **16KB ELF 對齊檢查**：
   - 前置確認：`app/build/outputs/apk/device/debug/app-device-debug.apk` 存在（需先執行 Step 1 assembleDebug）
   - 執行：`bash tools/check_elf_alignment.sh app/build/outputs/apk/device/debug/app-device-debug.apk 2>&1`
   - 判讀規則：
     - 所有 `lib/arm64-v8a/*.so` 與 `lib/x86_64/*.so` 必須顯示 `ALIGNED`
     - `UNALIGNED` 項目 → 記錄至 SV Report 為 🟡 WARNING，列出具體 .so 名稱
     - `zipalign` 區段若顯示 `NOTICE`（build-tools < 35.0.0-rc3）→ 記為 ⚪ SKIP，不扣分
     - `ELF Verification Successful` → ✅ PASS
   - 若 `objdump` 或 `bash` 不在 PATH → 記為 ⚪ SKIP（工具不可用，不影響 SV 結論）

## 階段 B：語意審查 (Semantic Review)

4. **殘骸掃描 (Orphan Check)**：
   - 確認舊邏輯、隱藏的 feature flag、廢棄 import 是否已徹底刪除。
   - 搜尋 `// TODO`、`// FIXME`、`// HACK`、`return null`——零容忍。

5. **Mock 偵測 (Anti-Lazy Check)**：
   - 檢查是否有塞入無意義的預設值（如 `Size(36.dp)` 硬編碼、`Color.Gray` 佔位）。
   - 所有 None/Empty 狀態必須是 **透明/隱藏**，不可有預設可見物。

6. **UI 互斥 (State Exclusive)**：
   - 檢查 UI 渲染是否有雙重重疊風險。
   - `when` 分支必須 exhaustive，`return` 防線是否穩固。
   - Compose `if/else` 層次是否正確處理 null 狀態。

## 階段 C：架構合規 (Architecture Compliance)

7. **UDF 合規**：
   - 狀態只從 ViewModel → UI 單向流動，UI 不直接修改 State。
   - 沒有在 Composable 中直接呼叫 Repository 或 UseCase。

8. **SSOT 漂移檢查**：
   - 新增的類別名稱、變數名稱是否與 v3.3 SSOT 一致？
   - 如有偏離（如 SSOT 定義 `EdgeWarmthLayer` 但實作叫 `WarmGradientOverlay`），回報漂移。

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

→ 本地 SV 驗收通過，準備提交 Traycer 終審。
📊 Session: ~[N] turns | [🟢 繼續 / 🟡 留意 / 🔴 建議 /compact]
```

若有任何失敗項：
```
=== SV Report ===
📋 Ticket: [Ticket ID]
🔨 Compile: 🔴 FAIL — [錯誤摘要]
📦 16KB ELF: 🟡 WARNING — [UNALIGNED .so 清單] (Release 前必須清零)
...
→ 🚫 SV 未通過。已自動修復 X 項，仍有 Y 項需人工介入。
📊 Session: ~[N] turns | [🟢 繼續 / 🟡 留意 / 🔴 建議 /compact]
```

## Session 健康度判斷規則

在 Report 末尾，根據當前對話狀態估算 Session 健康度：

| 對話輪數 | 狀態 | 建議 |
|---------|------|------|
| ≤ 10 turns | 🟢 | 繼續工作 |
| 11-18 turns | 🟡 | 留意，密集 Coding 可考慮下個 Ticket 前 /compact |
| ≥ 19 turns | 🔴 | 建議立即 /compact 開新 Session |

「turns」= 本次對話中 user 訊息的總數（不含系統 Prompt）。若對話中有大量代碼貼入（單則 >200 行），每則代碼額外計為 +2 turns。
