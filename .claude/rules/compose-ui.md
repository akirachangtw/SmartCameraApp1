---
description: Compose UI + Ambient UI 規則
globs:
  - "**/*Screen*.kt"
  - "**/*Overlay*.kt"
  - "**/*Composable*.kt"
---

# Compose UI Rules

### Ambient UI 色彩語言

| 顏色 | 用途 |
|------|------|
| `#D4A030` 琥珀金 | Pitch Traction 引導中、Halo 未對齊、Normal 模式 |
| `#50C864` 綠色 | 對齊完成、PTF 模式、呼吸韻律 |
| 所有 Halo 統一圓形 | 手動對焦 / 構圖引導 / PTF / 減法留白確認 |

14. **Composable 狀態記憶置頂規則**：所有 Composable 函數中的 `remember`/`LaunchedEffect`/`rememberCoroutineScope` 呼叫，必須置於函數體的最頂部（任何 `if`/`when`/`return` 語句之前），無論哪些路徑邏輯上需要使用它們。違反此規則會導致 Compose Slot-Table 不一致，在條件狀態切換時觸發 IllegalStateException 或首幀遺失（視 Compose 版本而定）。觸發來源：D6-B2-T2.1（`HaloGuidanceLayer` `LaunchedEffect` 首次錯置於 `isAllAligned` early return之後，Traycer 要求修復）。

23. **Overlay System Insets 紀律**：相機取景框上的浮動控制列（Top Bar、Bottom Bar）必須明確綁定系統安全區，禁止用 `weight(1f)` 等相對佈局模擬系統安全區高度。規範：①Top Bar 必須套用 `Modifier.statusBarsPadding()` 確保 Y 軸起始點避開狀態列；②Bottom Bar 必須以 `Modifier.navigationBarsPadding()` 作為底部緩衝，確保三大金剛鍵與全螢幕手勢下 UI 均不被遮蔽；③預留未來動態 UI 槽位（Reserved Slot），確保後續功能插入不造成已核准控制項的橫向位移。觸發來源：FIX-P0-SESSION-B-POLISH（Top Bar 缺少 statusBarsPadding + Lens Smudge 槽位、Bottom Bar weight(1f) 模擬系統 Insets）。
