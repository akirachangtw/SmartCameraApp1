# CodeGraph Query Patterns & Fallback Notes

**文件範圍**: Pilot 階段 CodeGraph 檢索工具 (`codegraph_explore`) 的標準使用範本與 Fallback 處理規範。
**限制聲明**: 本文件僅描述 `codegraph_explore` 工具 (MCP/CLI) 現有的可用參數 (`query`, `maxFiles`, `projectPath`)，不假設或預留任何尚未實作的欄位 (如 `mode`, `target`)。

## 1. 查詢範本 (Query Patterns)

以下定義三種在本 Repo 中進行 CodeGraph 檢索時的核心模式：

### Pattern 1: Symbol Lookup (符號尋找)
- **說明**: 給定 function / class / method 名稱，找出其定義位置與主要使用點。
- **適用情境**:
  - Bug trace 前，先確認核心元件的定義與屬性。
  - 想快速理解這個 Symbol 所在的 Module 或 Package。
- **使用工具**: `codegraph_explore` (MCP / CLI)
- **使用範例**:
  - **MCP**: `codegraph_explore(query="CameraController definition", maxFiles=5)`
  - **CLI**: `codegraph explore "CameraController definition"`
- **預期輸出**: 包含目標檔案的路徑、行號區間、Symbol 的型別 (Class/Interface/Function 等) 以及可能的基礎結構資訊。

### Pattern 2: Impact Analysis (影響範圍分析)
- **說明**: 針對特定檔案或 Function 的變更，分析可能被波及的呼叫鏈與依賴模組。
- **適用情境**:
  - 準備對核心模組進行 Refactor 或重構。
  - 評估修改特定 API 可能產生的 Blast Radius (影響半徑)。
- **使用工具**: `codegraph_explore` (MCP / CLI)
- **使用範例**:
  - **MCP**: `codegraph_explore(query="src/camera/ImageCapture.java takePicture impact callers dependents", maxFiles=8)`
  - **CLI**: `codegraph explore "src/camera/ImageCapture.java takePicture impact callers dependents"`
- **預期輸出**: 受影響的檔案清單、關鍵的呼叫鏈路，以及依賴此模組的其他元件清單。

### Pattern 3: Caller / Callee Trace (調用鏈追蹤)
- **說明**: 追蹤某個 Symbol 的上游呼叫者 (Callers) 與下游被呼叫對象 (Callees)。
- **適用情境**:
  - Cross-file (跨檔案) 的 Bug Trace。
  - 需釐清 Request 或 Event 從觸發到處理的完整流向。
- **使用工具**: `codegraph_explore` (MCP / CLI)
- **使用範例**:
  - **MCP**: `codegraph_explore(query="CameraService.openCamera callers callees request flow", maxFiles=8)`
  - **CLI**: `codegraph explore "CameraService.openCamera callers callees request flow"`
- **預期輸出**: Callers 與 Callees 清單、跨檔案調用路徑、以及可疑的資料傳遞中間層。

---

## 2. 何時不用 CodeGraph (Anti-Patterns)

為避免增加不必要的檢索 Overhead，**不建議**在以下情境呼叫 CodeGraph：
1. **純自然語言討論或架構設計**：沒有特定檔案或 Symbol 需要定位的廣泛探討。
2. **單檔小修**：已開啟該檔案，且變更範圍僅限於局部的 Bug / Typo 修正，無需跨檔分析。
3. **明確知道路徑**：開發者 (或 Agent) 已清楚知道目標檔案路徑與位置，直接使用 IDE 內建跳轉或 `view_file` / `read_file` 即可。
4. **超小型專案或極小變更**：當肉眼即可看完全局時，CodeGraph 可能只會增加冗餘的分析時間。

---

## 3. Fallback 紀錄規範

由於目前 Sidecar 與 Schema 仍在實作準備階段，Agent 遇到檢索降級或失效時，應以**手動紀錄格式**寫入 Extension Log 或對話日誌中。

### 定義的 Fallback Reasons
- `none`: 查詢成功且回傳結果準確可接受。
- `timeout`: CodeGraph 查詢執行超過預期時間，或被外部機制強制中斷。
- `no_results_or_low_quality`: 檢索回傳結果為空，或是與查詢意圖高度不相關 (例如誤中不相干的 Helper 函數)。
- `stale_index`: 明確知道索引 (Index) 未更新，或與目前 Repo 現狀差異過大。
- `tool_unavailable`: MCP 工具或 CLI 指令不可用 (例如啟動失敗、環境變數遺失等)。

### 手動記錄格式範例
當執行了一次 CodeGraph 查詢後，若遇到不佳的結果，請輸出以下文字區塊以供後續追蹤：

```text
[retrieval]
task_id=req_042
used_codegraph=true
pattern=symbol_lookup
fallback_reason=no_results_or_low_quality
note=Searched for 'FocusManager' but index returned obsolete cache logic. Switched to ripgrep.
```
