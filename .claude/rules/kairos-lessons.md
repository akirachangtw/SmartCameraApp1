---
description: KAIROS 回顧產出的架構守則 — 經實戰驗證的規則
globs:
  - "app/src/main/**/*.kt"
  - "app/src/test/**/*.kt"
---

# KAIROS Lessons (實戰萃取規則)

## K1-Rule 11 — 骨架 Ticket 未實作強制聲明
D1/骨架 Ticket 中的未實作函數，必須以 `error("Not implemented — will be wired in DX-TY")`
顯式拋出。嚴禁回傳合法型別空值（`null`、`emptyList()`）偽裝完成。
觸發來源：SV-D2-T4-FIX-01

## K1-Rule 12 — Rule 負向測試強制覆蓋
每個 `CompositionRule` 實作的測試套件，必須包含 ≥1 組負向測試
（空 faces 清單 + 座標超界 `[0,1]` 以外），驗證結果為 `score=0` / `isAligned=false`。
觸發來源：SV-D2-T1-FIX-01~03

## K2-Rule 13 — Android SDK 型別 JVM 測試環境規則
使用 `android.graphics.PointF`/`RectF` 的類別，JVM 單元測試必須加上
`@RunWith(RobolectricTestRunner::class)` + `@Config(sdk = [28])`，
或將欄位型別改為純 Kotlin data class。
理由：stub 環境下 PointF/RectF fields 全回傳 0，導致測試假過。
觸發來源：D6-B2-T1, D6-B3

## K3-Rule 18 — Edit 前強制 Re-view
對 >200 行的 .kt/.xml，Edit (str_replace) 前必須先 Read 操作區段（±10 行上下文）。
/compact 後視為強制觸發條件（無論檔案大小）。
（注意：此規則已內嵌至核心鐵律 5「寫之前先讀」中）

## K3-Rule 25 — Partial Fix 驗收紀律
Bug 修復 Ticket 若只覆蓋「觸發路徑」而非「所有消費路徑」，/sv **MUST**：
(a) 明列「已知未覆蓋路徑」於 sv_report.md；
(b) 開後續 Ticket 追蹤未覆蓋路徑；
(c) 不得將僅覆蓋部分路徑的修復標記為「完整修復」。
範例：P05-R1 修 EyeAfController（AF 路徑），但 HaloGuidanceLayer（渲染路徑）
和 observePtfAlignment（PTF 路徑）未覆蓋 → 琥珀圓環持續亂竄（Ghost Face 壁癌）。
觸發來源：Session V — GazeAF Ghost Face 壁癌分析。

## K3-Rule 26 — 多消費路徑掃描
Ticket 動工前（Plan 階段），**MUST** 對 Ticket 要修改的「資料來源」執行：
`grep -rn "[資料來源名]" app/src/main/java --include=*.kt`
列出所有消費者路徑，每條路徑標註「是否在 ALLOWED 範圍」。
若存在消費者不在 ALLOWED 範圍 → 回報 CSO，等待裁決是否擴充 ALLOWED 或開後續 Ticket。
嚴禁「只修觸手可及的路徑，忽略看不見的消費者」。
觸發來源：Session V — GazeAF Ghost Face 壁癌分析（三條消費路徑，P05-R1 僅覆蓋一條）。
