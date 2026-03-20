# Defensive Pipeline & Workflow Playbook

> **定位**: 從 SmartCameraApp1 v2.12.0 防禦型開發實戰萃取的通用知識。
> 融合來源：迭代記憶 (v2.12.0-25~27) + MERGE_SOP + Ultimate Defensive Hybrid Workflow Guide
>
> **用途**: 新對話、新功能開始前的「戰前簡報」。
> 加粗標示的守則適用於所有 Android 手機 APP 專案。

---

## Part I — Android 通用守則（適用一切 APP 專案）

### 1. 狀態機設計鐵律

**守則 1-A：無效狀態必須傳播為 null，禁止以預設值偽裝**

錯誤的做法是在資料不完整時回傳一個「看起來合法」的預設物件（例如空的 Matrix、空的 Rect、0-size 的資料類別），
這會讓下游誤以為資料有效，並在後續產生難以追蹤的視覺錯誤或計算錯誤。

```
資料來源產生無效輸入（尺寸為 0、nullable 未初始化）
        ↓ 正確做法
  return null / return@launch / ?: return
        ↓ 上游收到 null
  降級到安全的 fallback 路徑（或靜默跳過本幀）

        ↓ 錯誤做法（禁止）
  return DefaultObject()  ← 偽裝成合法，下游繼續用錯誤資料計算
```

**守則 1-B：Early-Return Guard Clause 是強制的**

任何接受外部資料的函式，開頭必須驗證關鍵前置條件：

```kotlin
fun processFrame(width: Int, height: Int, bounds: RectF): Result? {
    // Guard Clause — 無效輸入一律在函式入口攔截
    if (width <= 0 || height <= 0 || bounds.isEmpty) {
        Log.e(TAG, "Invalid input: ${width}x${height}, bounds=${bounds}")
        return null  // 不繼續，不產生副作用
    }
    // ... 真正的邏輯
}
```

**守則 1-C：狀態機切換前必須 cancel 舊 Job，且順序不可調換**

```kotlin
// 正確順序（camera switch、mode change 等觸發點）
oldJob?.cancel()              // 1. 先取消舊任務
_state.update { it.copy(status = Idle) }  // 2. 再重置狀態
throttle.reset()              // 3. 最後清除節流器

// 禁止：先 reset state 再 cancel → Race Condition 仍會發生
```

---

### 2. 協程與非同步安全

**守則 2-A：CancellationException 必須 re-throw，不可吞掉**

```kotlin
} catch (e: CancellationException) {
    // 不視為 failure，不呼叫 onFailure()，不觸發 UI 更新
    throw e  // 必須 re-throw，讓協程框架正常終止
} catch (e: Exception) {
    Log.e(TAG, "Operation failed", e)
    onFailure()
}
```

**守則 2-B：Flow 必須有 .catch，防止單一幀錯誤殺死整個 Flow**

```kotlin
previewFlow
    .onEach { frame ->
        try {
            processFrame(frame)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Frame processing error — skipping frame", e)
            // 繼續跑下一幀，不讓 Flow 中斷
        }
    }
    .catch { e -> Log.e(TAG, "Flow terminated unexpectedly", e) }
    .launchIn(viewModelScope)
```

**守則 2-C：長時間 In-Flight 操作必須設置 In-Flight Lock**

適用場景：硬體操作（相機 AF、藍牙配對、檔案寫入）、網路請求、耗時計算。

```kotlin
// 防止高頻觸發（例如 30fps 畫面分析）持續 cancel 未完成的硬體操作
val isJobActive = activeJob?.isActive == true
val isUserForced = userIntent.isExplicitAction  // 使用者明確操作才允許中斷

if (isJobActive && !isUserForced) {
    Log.v(TAG, "Skipping: previous job is still in-flight")
    return  // 讓硬體操作跑完
}

activeJob?.cancel()
activeJob = scope.launch { /* 新的操作 */ }
```

---

### 3. 活性監控（Liveness）

**守則 3-A：長時間執行的管線必須有心跳 Log**

任何後台持續執行的 Flow/Worker（影像分析、感測器收集、網路輪詢），
都必須每 N 個週期印一次心跳，讓開發者能從 Logcat 判斷管線是否存活：

```kotlin
var processedFrames = 0L

fun startAnalysisPipeline() {
    analysisFlow
        .onEach { frame ->
            processedFrames++
            if (processedFrames % 30L == 0L) {
                Log.i(TAG, "[Heartbeat] pipeline alive, frame=$processedFrames")
            }
            processFrame(frame)
        }
        .launchIn(scope)
}
```

**診斷規則**：
- 心跳每 N 秒穩定出現 → 管線存活 ✅
- 心跳消失 → 管線靜默死亡，排查：權限（重裝後需重授）、資源洩漏、未捕捉的 Exception

---

### 4. UDF 架構紀律（Unidirectional Data Flow）

**守則 4-A：狀態層與 UI 層絕對分離**

| 層 | 職責 | 禁止 |
|----|------|------|
| ViewModel / UseCase | 業務邏輯、狀態計算、硬體協調 | 引用任何 View / Composable |
| Composable / Fragment | 根據 State 渲染 UI | 包含業務邏輯、直接操作硬體 |
| Repository / DataSource | 資料獲取與轉換 | 持有 ViewModel 引用 |

**守則 4-B：Composable 不得自行計算衍生狀態**

```kotlin
// 錯誤：UI 層自行判斷 lock 狀態
val isLocked = afState is AfState.Locked || isAEAFLocked || ...

// 正確：ViewModel 提供已計算的衍生欄位
data class CameraUiState(
    val isFocusIndicatorLocked: Boolean,  // 由 ViewModel 計算，定義嚴格
    val focusIndicatorColor: Color,
)
```

---

### 5. 依賴注入與介面隔離

**守則 5-A：Domain 介面不得引入 Android 框架類別**

```kotlin
// 正確：純 Kotlin domain 介面
interface SensorDataSource {
    fun gyroscopeFlow(): Flow<GyroscopeData>
}

// 錯誤：domain 層引入 android.hardware.*
interface SensorDataSource {
    fun register(manager: SensorManager, ...)  // ❌ 破壞 Clean Architecture
}
```

**守則 5-B：Hilt @Provides 與 @Binds 必須嚴格分開**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class SensorModule {
    // @Binds 在 abstract class 本體
    @Binds abstract fun bindSensor(impl: SensorImpl): SensorInterface

    companion object {
        // @Provides 在 companion object
        @Provides @Singleton
        fun provideSensorImpl(ctx: Context): SensorImpl = SensorImpl(ctx)
    }
}
```

---

### 6. 零偷懶政策（Anti-Lazy）

以下任一項出現視為「偷懶」，SV 驗收強制退回：

| 症狀 | 嚴重度 |
|------|--------|
| `// TODO: implement later` | 🔴 阻斷 |
| `return null // placeholder` | 🔴 阻斷 |
| `val x = 0 // fix this` | 🔴 阻斷 |
| `if (BuildConfig.DEBUG) { /* skip in prod */ }` 無 release 實作 | 🔴 阻斷 |
| 刪除功能但留下未使用的 import / 廢棄變數 | 🟡 警告 |

---

## Part II — 開發流程

### 9. 標準開發循環

```
實作
  → /qa  (assembleDebug — 確認編譯無錯)
  → /sv  (殘骸掃描 + Anti-Lazy + UI 互斥)
  → 分批 git commit
      Phase 1: 程式碼 (.kt)  →  [Fix] vX.Y.Z-NN: <簡述>
      Phase 2: 文件 (.md/.log) → [Docs] vX.Y.Z-NN: <簡述>
  → 實機驗收（帶 Logcat 證據）
  → PASS → 更新 SSOT 文件 → 準備合併
  → FAIL → logcat → Traycer trigger-workflow 重啟 PDCA
      ⚠️ 禁止直接搶修，必須重走 PDCA 閉環
```

### 10. Git Commit 紀律

**每次 commit 前必須執行**：
```bash
git status --short  # 確認只有授權的檔案被修改
git diff --stat     # 快速確認 diff 範圍
```

**禁止行為**：
- `git add .` 或 `git add -A`（可能混入 .env、大型 binary、無關檔案）
- 在一個 commit 同時包含程式碼修復與文件更新（難以 bisect）
- `git commit --amend` 已推送的 commit

### 11. 合併前強制執行

```bash
# Step 1: 更新遠端資訊
git fetch origin --tags

# Step 2: 文件完整性檢查（若專案有此工具）
python tools/check_merge_readiness.py  # 必須輸出 ✅ READY TO MERGE

# Step 3: 使用遠端分支或 tag（禁止使用可能過時的本地分支）
git merge origin/<branch-name> --no-edit
# 或
git merge v<version>-final --no-edit
```

#### `check_merge_readiness.py` 功能說明

**用途**：在 `git merge` 前自動驗證 MERGE_SOP Phase 2 文件同步是否完成，防止「合了卻沒有任何文件佐證」的技術債累積。

**6 項檢查邏輯**：

| Step | 檢查項目 | 通過條件 | 失敗影響 |
|------|---------|---------|---------|
| 1 | `app/build.gradle.kts` 版本偵測 | `versionName = "X.Y.Z"` 可解析 | 中止，無法繼續 |
| 2 | `docs/features/v<version>/` | 目錄存在且含 L0/L1/L2/Traycer `.md` | ❌ 阻擋合併 |
| 3 | `docs/test_results/v<version>/` | 目錄存在且含 `.md` 測試報告 | ❌ 阻擋合併 |
| 4 | `devspec/BRANCH_RELEASES.md` | 包含 `v<version>` 字串 | ❌ 阻擋合併 |
| 5 | `devspec/feature_record_Backlog.md` | 包含 `v<version>` 字串 | ❌ 阻擋合併 |
| 6 | `devspec/DOCS_SYNC_LOG_*.md` | 存在任一同步日誌 | ⚠️ 警告（不阻擋） |

**輸出結果**：
- `✅ READY TO MERGE` → 可安全執行 `git merge`
- `⚠️ NOT READY - 發現 N 個問題` → 依提示修正後重新執行

**緊急旁路**（風險自負）：
```bash
python tools/check_merge_readiness.py --bypass
```

**套用到新專案**：
1. 複製 `tools/check_merge_readiness.py` 至新專案
2. 依專案實際目錄結構調整 `check_feature_docs()` 和 `check_test_results()` 的 glob 路徑
3. 在 Git pre-merge hook 或 CI pipeline 中呼叫此腳本，實現自動化守門

---

### 12. SV 驗收三項檢查

每次實作完成後必須執行（對應 `/sv` 技能）：

| 項目 | 內容 | 範例 |
|------|------|------|
| 殘骸掃描 | 刪除廢棄 import、unused 變數、舊 feature flag | `grep -n "TODO\|FIXME\|//.*deprecated"` |
| Anti-Lazy 偵測 | 確認所有接線均為真實實作，無 placeholder | `grep -n "return null\|TODO\|stub"` |
| UI 互斥 | 確認 UI 狀態互相排斥，無雙重重疊 | 檢查 isX && isY 同時為 true 的可能性 |

---

## Part III — 診斷速查

### 13. Logcat 快速診斷模板

```bash
# 管線存活確認
adb -s <DEVICE_ID> logcat <TAG>:I *:S | grep "Heartbeat"

# 幾何座標確認
adb -s <DEVICE_ID> logcat <TAG>:I *:S | grep "CoordSystem\|imageProxy"

# 協程崩潰偵測
adb -s <DEVICE_ID> logcat *:E | grep "StandaloneCoroutine\|CancellationException\|Fatal"

# 權限問題偵測
adb -s <DEVICE_ID> logcat *:E | grep "SecurityException\|Permission denied"
```

### 14. 常見靜默死亡原因速查

| 現象 | 最可能原因 | 診斷方法 |
|------|-----------|---------|
| 心跳消失 | 未捕捉的 Exception 殺死 Flow | 加 `.catch { }` 後查 Log |
| UI 框不出現 | 幾何計算拿到 null/0 尺寸 | 查 `[CoordSystem] Invalid dimensions` |
| 操作無回應 | In-Flight Lock 阻擋 | 查 `Skipping: previous job in-flight` |
| 前鏡頭對焦永不完成 | Fixed-Focus 裝置未做旁路 | 查 `isFixedFocus` log |
| 切換功能後舊 UI 殘留 | 狀態機未處理 reset 事件 | 確認 `ON_STOP` / 事件觸發時清除狀態 |

---

## Part IV — 核心 SSOT 文件索引與摘要

> 以下五份文件構成本專案的防禦型開發骨架。
> 套用到其他 APP 專案時，依相同結構建立對應文件即可。

---

### `devspec/feature_record_Backlog.md` — 防禦型守則 SSOT

**定位**：專案最高決議文件。記錄每個版本迭代中「從失敗中燒出的守則」、Critical Warning、及架構決策。

**核心內容**：
- 每個 Fix 版本的「禁止再犯清單」
- 狀態機決策（哪些路徑不可走、為什麼）
- 已知的硬體坑點（HAL 行為、裝置差異）

**套用到新專案**：建立同名文件，以版本號分 section，每次出現 regression 或 critical bug 必須在此留下決議，作為後續 Code Review 的對照依據。

---

### `devspec/BRANCH_RELEASES.md` — 分支版本記錄

**定位**：記錄每條 feature branch 的「穩定版 tag」、「已知問題」、「關鍵 commit」、「合併狀態」。

**核心內容**：
- 每條 branch 的 `-final` tag（最終可合併版本）
- 合併前/後的狀態標記（`✅ 穩定` / `⚠️ 待驗收`）
- 關鍵修復的 commit hash（防止合併錯版本）

**套用到新專案**：合併前永遠查此文件確認 tag，禁止直接 merge 本地分支（可能過時）。

---

### `devspec/MERGE_SOP.md` — 合併標準作業程序（完整版）

**定位**：5 個 Phase 的完整合併流程，防止因「跳步驟」導致文件遺失或 regression 重現。

**核心內容**：
- Phase 1：確認來源（`git fetch --tags`、讀 BRANCH_RELEASES.md）
- Phase 2：文件同步（`docs/features/`、`docs/test_results/` 必須同步）
- Phase 3：代碼合併（解決衝突時參考 feature_record_Backlog.md）
- Phase 4：驗證（build + 16KB ELF 對齊 + 實機測試）
- Phase 5：記錄（更新 BRANCH_RELEASES.md）

**套用到新專案**：保留 Phase 結構，將 Phase 4 的驗證指令換成該專案的測試方式。

---

### `devspec/MERGE_SOP_QUICK_REFERENCE.md` — 合併 SOP 快速參考

**定位**：MERGE_SOP.md 的 1-2 頁濃縮版，日常合併操作的即查手冊。

**核心內容**：
- 5 步驟流程（fetch → 文件同步 → merge → 驗證 → 記錄）
- 常見錯誤與預防（本地分支過時、衝突解決刪掉關鍵修復）
- 緊急回滾指令

**套用到新專案**：直接複製後修改第 4 步的驗證指令，其餘流程通用。

---

### `devspec/Ultimate-Defensive-Hybrid-Workflow_Guide.md` — 終極防禦工作流指引

**定位**：定義 AI 協作角色（架構師顧問）與 PDCA 防禦閉環流程，適用於 Claude + ChatGPT/Traycer 混合開發模式。

**核心內容**：
- **Plan**：讀規格書、採 Strangler Fig 模式拆解 God Object
- **Design**：產出精確交接指令（含 Scope Ban 絕對禁區）
- **Code**：最小修改原則，發現超出授權範圍立即暫停回報
- **Act/Verify**：Scope Cleanup（git diff 檢查）+ 生命週期審查
- Anti-Pattern 零容忍清單（預設值陷阱、狀態機死鎖、模型漂移、Ping-pong ID 碰撞）

**套用到新專案**：直接作為 Claude Code 的 System Prompt 補充，或貼入 Traycer 自訂工作流設定。

---

### `devspec/99-stage_verification_checklist.md` — 階段驗收清單

**定位**：Level 3 實作完成後、提交 PR 之前的全面驗收 SOP。提供可手動勾選的 6 Phase 結構化清單。

**核心內容（6 Phases）**：

| Phase | 內容 | 強制 / 條件 |
|-------|------|------------|
| 1 Pre-Build | Git status 清潔、命名規範、無硬編碼 | 強制 |
| 2 Build 驗證 | `./gradlew clean assembleEmulatorDebug` + Lint | 強制 |
| 3 16KB Compatibility | ELF alignment 檢測（`check_elf_alignment.sh`） | 強制 |
| 4 Emulator Testing | 功能 + 穩定性（可跳過：實機已連線時） | 條件 |
| 5 Device Testing | 實機安裝、Logcat 收集、Test Handoff | 條件 |
| 6 Merge Readiness | `check_merge_readiness.py` 輸出 ✅ READY | 合併前 |

**快速路徑**：實機已連線時可跳過 Phase 4，完成 Phase 1-3 後直接執行 Phase 5。

**套用到新專案**：
1. 複製文件後修改 Phase 3 的 ELF alignment 腳本路徑
2. Phase 4/5 的 package name 與裝置 ID 替換為新專案值
3. Phase 6 的 `check_merge_readiness.py` 路徑保持不變（tools/ 目錄下）

---

### `devspec/99-handoff-prompt.md` — 對話交接提示詞模板

**定位**：Context Window 即將用盡時，生成「給下一位 Claude 的交接指令」的標準格式，確保跨對話記憶完整傳遞。

**核心內容（5 章節結構）**：

| 章節 | 內容 | 用途 |
|------|------|------|
| 專案背景 (Context) | 正在解決的問題 | 讓下一位 Claude 快速定位 |
| 語言規範 | 程式碼用英文、對話用繁體中文 | 防止語言切換造成理解錯誤 |
| 目前狀態 | 已完成項目、已解決的關鍵錯誤 | 避免重複實作 |
| 最新交付成果 | **完整可執行程式碼**（禁止 `// ...` 省略） | 新對話立即可用 |
| 下一步行動 | 新對話中立即執行的任務 | 無縫銜接 |

**交接文件命名規則**：`chat_logs/handofflog_YYYYMMDD_HHMMSS.md`

**套用到新專案**：
- 修改「專案背景」章節的問題描述
- 「語言規範」章節可依團隊慣例調整
- 結構與命名規則通用，無需其他修改

---

**文件版本**: v2.12.0-27 rev.4 (2026-03-20)
**維護者**: Claude Sonnet 4.6 + Akira Chang
**適用範圍**: Part I / II / III 為通用 Android APP 守則；Part IV SSOT 文件可依專案結構調整路徑
