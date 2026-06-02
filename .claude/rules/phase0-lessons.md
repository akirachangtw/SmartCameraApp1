---
description: Phase 0 盲區規則 — 來自 D0-D8 退件經驗萃取
globs:
  - "app/src/main/**/*.kt"
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Phase 0 Lessons (Rule 19-23)

**KAIROS #4 產出 (FIX-P0-SESSION-A, 2026-05-13)**

19. **Decommission 全鏈路紀律**：任何 decommission / debug-overlay 類票據在關閉前，**MUST** 同時清空 4 層殘留：①UI Composable 呼叫點、②Settings 開關 UI、③持久化 key（DataStore/SharedPreferences）、④ViewModel/Repository 暴露面。只停用呼叫點而不清除其餘三層，不算完成；`/sv` **MUST** 對這 4 層逐一 grep 驗證。
    **追加（K3-4）**：Decommission 票閉環前，**MUST** 執行 `find app/src -name "*被刪除模組名*"` 驗證零命中。/sv **MUST** 將此 glob 驗證結果寫入 sv_report.md。
    觸發來源：FIX-P0-SESSION-A + FIX-INFRA-02-D。

---

### KAIROS #5 產出 (FIX-P0-SESSION-A-FINAL-SYNC + FIX-GSTACK-SSOT-LINK, 2026-05-13)

20. **PROGRESS.md 三處狀態一致性規則**：PROGRESS.md **MUST** 不得對同一驗收 scope 同時出現 "SV done" 與 "待 /sv"；Header / Sprint Health / Next Actions **MUST** 三者一致。任何 Ticket 完成後，**MUST** 同步更新三個區塊，**MUST** 禁止留下互相矛盾的狀態描述。觸發來源：FIX-P0-SESSION-A-FINAL-SYNC（Traycer 指出 Header 標 DONE 但 Sprint Health 仍標「待 /sv」）。

21. **SSOT 遷移紀律**：活躍的治理文件（PROGRESS.md、CLAUDE.md）**MUST** 絕不能指向已刪除的 SSOT/Spec 產物。每一次 SSOT 遷移 **MUST** 在同一閉環步驟中同時修復所有連結指針，並以 `find` 或 `grep` 確認舊路徑 0 命中後方可提交。`/sv` **MUST** 對 `.claude/` 目錄的活躍文件執行過期路徑掃描。觸發來源：FIX-GSTACK-SSOT-LINK（PROGRESS.md 指向已刪除的 v3.3/v3.4.1 文件）。

---

**FIX-P0-SESSION-B 產出 (2026-05-14)**

22. **狀態機時序紀律**：任何 mode-entry cooldown（模式切入冷卻）**MUST** 綁定**切換時間戳（transition timestamp）**，而非前次動作時間戳（prior action timestamp）。切換至目標模式的當下 **MUST** 立即更新 `modeEntryTimeMs = timeProvider()`，並以此為基準計算冷卻期。使用前次操作（如上次拍照時間 `lastCaptureTimeMs`）作為冷卻起點是語意錯誤，會導致模式剛切入時冷卻期已部分或全部消耗完畢。觸發來源：FIX-P0-SESSION-B（PTF 1s entry lockout 改用 `modeEntryTimeMs`，與 Rule 16 capture-to-capture cooldown 分層）。

---

**FIX-P0-SESSION-B-POLISH 產出 (2026-05-14)**

23. **Overlay System Insets 紀律**：相機取景框上的浮動控制列（Top Bar、Bottom Bar）**MUST** 明確綁定系統安全區，**MUST** 禁止用 `weight(1f)` 等相對佈局模擬系統安全區高度。規範：①Top Bar **MUST** 套用 `Modifier.statusBarsPadding()` 確保 Y 軸起始點避開狀態列；②Bottom Bar **MUST** 以 `Modifier.navigationBarsPadding()` 作為底部緩衝，確保三大金剛鍵與全螢幕手勢下 UI 均不被遮蔽；③預留未來動態 UI 槽位（Reserved Slot），確保後續功能插入不造成已核准控制項的橫向位移。觸發來源：FIX-P0-SESSION-B-POLISH（Top Bar 缺少 statusBarsPadding + Lens Smudge 槽位、Bottom Bar weight(1f) 模擬系統 Insets）。

---

**FIX-P0-SESSION-C-FINAL 產出 (2026-05-14)**

24. **Debug 功能剝離紀律**：Debug-only 效能覆蓋層（FPS、記憶體、幀率監控等）**MUST** 從四層同時剝離：①UI 調用點（`if (BuildConfig.DEBUG) { FPSOverlay(...) }`）、②ViewModel 暴露面（**MUST** 不得暴露 DEBUG 專用 StateFlow 至正式介面）、③Repository / InputSource 介面合約（`CameraRepository`、`CameraInputSource` **MUST** 不得包含 DEBUG 專用方法）、④資料層計算（`if (BuildConfig.DEBUG) _currentFps.value = fps`）。僅依賴 `visible = BuildConfig.DEBUG` 做 UI 隱藏不足以達到 Release 剝離標準，R8/ProGuard 無法消除未被呼叫路徑遮蔽的介面實作。
    **追加（K3-1）**：任何在 CameraScreen/Preview 可視區域內渲染的 debug 文字（FPS、座標、偵測結果等），無論是否受 BuildConfig.DEBUG 保護，一律視為 /sv **MUST** 退件項。Release build 的用戶預覽不允許任何非 SSOT 定義的視覺元素。
    觸發來源：FIX-P0-SESSION-C-FINAL + D8 FIX-01~03。

---

**KAIROS #3 產出 (Session V, 2026-06-01)**

27. **SSOT 優先裁決**：Ticket 驗收清單與 SSOT 規格發生衝突時，**MUST** 以 SSOT 為準。Claude Code **MUST** 在 sv_report.md 中記錄衝突內容、SSOT 條款引用、以及「以 SSOT 為準」的裁決。**MUST** 嚴禁靜默選擇 Ticket 版本而忽略 SSOT 約束。觸發來源：TICKET-01（驗收清單與 SSOT 指定的合併規格源衝突）。
