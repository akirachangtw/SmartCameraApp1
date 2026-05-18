---
description: Android 開發規範 — 錯誤防禦、UDF、i18n、Bash 安全、命名規範、Git Commit
globs:
  - "app/src/main/**/*.kt"
  - "app/src/main/**/*.xml"
  - "*.gradle.kts"
---

# Android Conventions

## 錯誤防禦
- 必須處理可能導致 Crash 的 null 狀態與陣列越界
- 所有 `when` 必須 exhaustive
- Flow / Coroutine 加 `.catch { }` 或 `try-catch` 並印出 `Log.e`
- 無效幾何尺寸（`<= 0`）→ `return null`，絕不回傳 `Matrix()` 偽裝者

## 架構純潔 (UDF)
- 嚴格遵守 Unidirectional Data Flow，狀態層與 UI 層絕對分離
- UI 不直接呼叫 Repository 或 UseCase
- 使用依賴注入（Hilt）、遵循 MVVM 架構模式

## 命名一致 (Anti-Drift)
- 所有類別名稱、變數名稱必須與 SSOT 一致
- 禁止自行發明未經授權的命名
- 變數使用 `camelCase`，常數使用 `UPPER_SNAKE_CASE`
- 布林值以 `is`, `has`, `should` 開頭
- 函數名稱使用動詞開頭，變數名稱至少 3 個字元
- Activity: `XxxActivity` / Fragment: `XxxFragment` / ViewModel: `XxxViewModel`

## i18n 三道防線
1. **UI 層**: 絕對禁止 Hardcode 顯示字串。所有靜態文案必須透過 `stringResource(R.string.xxx)` 渲染
2. **Domain/Data 層**: 禁止持有本地化字串。必須使用純語意代碼 (Sealed Class / Enum)，翻譯責任下放 UI 層
3. **AI 邏輯層**: 串接 LLM 時必須在 Prompt 動態注入 Locale 參數

## Bash 指令安全
- 禁止 `cd [路徑] &&` 開頭（繞過權限白名單匹配）
- 強制使用相對路徑
- ✅ `grep -n "CameraCharacteristics" app/src/...`
- ❌ `cd "D:\..." && grep ...`

## 程式碼品質
- 每個類別不超過 300 行
- 每個函數不超過 50 行
- 避免硬編碼字串（使用 `strings.xml`）
- 避免硬編碼顏色（使用 `colors.xml` 或 Compose Theme）

## Git Commit 格式
```
[類型] 簡短說明
類型：[Feature] / [Fix] / [Refactor] / [UI] / [Perf] / [Docs] / [Test] / [Build]
```

## 版本號: `主版本.次版本.修訂號 (Build號)`
- 主(+1.0.0): 重大功能/架構重構
- 次(+0.1.0): 新增功能
- 修(+0.0.1): Bug 修復
