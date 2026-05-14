---
description: Phase 0 盲區規則 — 來自 D0-D8 退件經驗萃取
globs:
  - "app/src/main/**/*.kt"
---

# Phase 0 Lessons (Rule 19-23)

**KAIROS #4 產出 (FIX-P0-SESSION-A, 2026-05-13)**

19. **Decommission 全鏈路紀律**：任何 decommission / debug-overlay 類票據在關閉前，必須同時清空 4 層殘留：①UI Composable 呼叫點、②Settings 開關 UI、③持久化 key（DataStore/SharedPreferences）、④ViewModel/Repository 暴露面。只停用呼叫點而不清除其餘三層，不算完成；`/sv` 必須對這 4 層逐一 grep 驗證。觸發來源：FIX-P0-SESSION-A（DetectionOverlay 殘留 4 層，Traycer 退回）。

---

### KAIROS #5 產出 (FIX-P0-SESSION-A-FINAL-SYNC + FIX-GSTACK-SSOT-LINK, 2026-05-13)

20. **PROGRESS.md 三處狀態一致性規則**：PROGRESS.md 對同一驗收 scope 不得同時出現 "SV done" 與 "待 /sv"；Header / Sprint Health / Next Actions 必須三者一致。任何 Ticket 完成後，必須同步更新三個區塊，禁止留下互相矛盾的狀態描述。觸發來源：FIX-P0-SESSION-A-FINAL-SYNC（Traycer 指出 Header 標 DONE 但 Sprint Health 仍標「待 /sv」）。

21. **SSOT 遷移紀律**：活躍的治理文件（PROGRESS.md、CLAUDE.md）絕不能指向已刪除的 SSOT/Spec 產物。每一次 SSOT 遷移必須在同一閉環步驟中同時修復所有連結指針，並以 `find` 或 `grep` 確認舊路徑 0 命中後方可提交。`/sv` 必須對 `.claude/` 目錄的活躍文件執行過期路徑掃描。觸發來源：FIX-GSTACK-SSOT-LINK（PROGRESS.md 指向已刪除的 v3.3/v3.4.1 文件）。

---

**FIX-P0-SESSION-B 產出 (2026-05-14)**

22. **狀態機時序紀律**：任何 mode-entry cooldown（模式切入冷卻）必須綁定**切換時間戳（transition timestamp）**，而非前次動作時間戳（prior action timestamp）。切換至目標模式的當下必須立即更新 `modeEntryTimeMs = timeProvider()`，並以此為基準計算冷卻期。使用前次操作（如上次拍照時間 `lastCaptureTimeMs`）作為冷卻起點是語意錯誤，會導致模式剛切入時冷卻期已部分或全部消耗完畢。觸發來源：FIX-P0-SESSION-B（PTF 1s entry lockout 改用 `modeEntryTimeMs`，與 Rule 16 capture-to-capture cooldown 分層）。

---

**FIX-P0-SESSION-B-POLISH 產出 (2026-05-14)**

23. **Overlay System Insets 紀律**：相機取景框上的浮動控制列（Top Bar、Bottom Bar）必須明確綁定系統安全區，禁止用 `weight(1f)` 等相對佈局模擬系統安全區高度。規範：①Top Bar 必須套用 `Modifier.statusBarsPadding()` 確保 Y 軸起始點避開狀態列；②Bottom Bar 必須以 `Modifier.navigationBarsPadding()` 作為底部緩衝，確保三大金剛鍵與全螢幕手勢下 UI 均不被遮蔽；③預留未來動態 UI 槽位（Reserved Slot），確保後續功能插入不造成已核准控制項的橫向位移。觸發來源：FIX-P0-SESSION-B-POLISH（Top Bar 缺少 statusBarsPadding + Lens Smudge 槽位、Bottom Bar weight(1f) 模擬系統 Insets）。
