# Hybrid Flow with Claude Code - Quick Reference

## 概述

Hybrid Flow 工具已整合到 Claude Code，可直接在對話中執行 L1/L2/PR 文檔生成。

## 設定狀態

✅ **已完成的設定**:
- Python 3.14.0 已安裝
- `hybrid_flow.py` 存在於 `tools/` 目錄
- Claude Code 權限已配置 (`.claude/settings.local.json`)
- VSCode Tasks 已整合 (`.vscode/tasks.json`) ⭐ NEW
- 支援的命令已加入 allow list:
  - `python tools/hybrid_flow.py --level L1`
  - `python tools/hybrid_flow.py --level L2`
  - `python tools/hybrid_flow.py --level PR`

---

## 使用方式

### 🔹 方法 1: 在 Claude Code 對話中直接執行

你可以在與 Claude Code 的對話中直接要求執行 hybrid_flow 命令：

#### Level 1 - Planning (規劃)

```bash
python tools/hybrid_flow.py --level L1 --topic "ui_ux_redesign" --input "重新調整 CameraScreen 排版，優化 GuidelineOverlay 視覺層次" --out "docs/plan" --repo "."
```

#### Level 2 - Feasibility (可行性)

```bash
python tools/hybrid_flow.py --level L2 --topic "ui_ux_redesign" --issue "docs/plan/ui_ux_redesign_L1.md" --input "評估 Sprint 3 後進行 UI/UX 調整的技術風險" --out "docs/plan" --repo "."
```

#### PR Notes (實作記錄)

```bash
python tools/hybrid_flow.py --level PR --topic "ui_ux_redesign" --input "完成 GuidelineOverlay 顏色與透明度調整" --out "docs/plan" --repo "."
```

---

### 🔹 方法 2: 使用 VSCode Tasks (GUI 方式) ⭐ UPDATED

在 VSCode 中:
1. 按 `Ctrl+Shift+P`
2. 輸入 "Tasks: Run Task"
3. 選擇以下任一 Task (已整合到 `.vscode/tasks.json`):
   - `📋 Hybrid: L1 Plan (inline input)`
   - `📋 Hybrid: L1 Plan (input_file)`
   - `📋 Hybrid: L2 Feasibility (inline input)`
   - `📋 Hybrid: L2 Feasibility (input_file)`
   - `📋 Hybrid: PR Notes (inline input)`

**優點**:
- ✅ GUI 介面，無需記憶命令
- ✅ 自動提示輸入參數
- ✅ 與其他專案 tasks (Build, Test, Lint) 整合在一起
- ✅ 輸出顯示在專用 panel

---

## 參數說明

| 參數 | 必填 | 說明 | 範例 |
|------|------|------|------|
| `--level` | ✅ | 文檔類型 | `L1`, `L2`, `PR` |
| `--topic` | ✅ | 主題名稱 (用於檔名) | `ui_ux_redesign` |
| `--input` | ❌ | 內嵌描述 | `"重新調整 UI 排版"` |
| `--input_file` | ❌ | 外部檔案路徑 ⭐ 支援 Windows 路徑 | `logs/logcat.txt` 或 `logs\logcat.txt` |
| `--issue` | ❌ | Issue/L1 檔案路徑 (L2 建議填) ⭐ 支援 Windows 路徑 | `docs/plan/ui_ux_redesign_L1.md` 或 `docs\plan\ui_ux_redesign_L1.md` |
| `--out` | ❌ | 輸出目錄 | `docs/plan` (預設) |
| `--repo` | ❌ | Repository 根目錄 | `.` (預設) |
| `--stdin` | ❌ | 從 STDIN 讀取 | 用於 pipe logcat |
| `--no_git` | ❌ | 停用 git 上下文收集 | - |

### 📝 路徑格式說明 ⭐ NEW

**Windows 路徑支援**:
- ✅ 支援 Unix 格式: `docs/plan/file.md`
- ✅ 支援 Windows 格式: `docs\plan\file.md`
- ✅ 自動正規化路徑分隔符
- ✅ 支援相對路徑（相對於 repository 根目錄）
- ✅ 支援絕對路徑

**範例**:
```bash
# 以下三種寫法都可以
python tools/hybrid_flow.py --level L2 --issue "docs/plan/ui_L1.md"
python tools/hybrid_flow.py --level L2 --issue "docs\plan\ui_L1.md"
python tools/hybrid_flow.py --level L2 --issue "D:\Projects\SmartCameraApp1\docs\plan\ui_L1.md"
```

---

## 使用情境範例

### 情境 1: UI/UX 重新設計規劃 (Level 1)

**對話範例**:
```
User: 我想重新調整 CameraScreen 的 UI 排版，請幫我建立 L1 規劃文件

Claude: 好的，我來幫你執行 L1 規劃生成
```

**執行命令**:
```bash
python tools/hybrid_flow.py \
  --level L1 \
  --topic "camera_screen_ui_redesign" \
  --input "重新調整 CameraScreen 排版，優化 GuidelineOverlay 與 DetectionOverlay 的視覺層次，調整控制按鈕位置" \
  --out "docs/plan" \
  --repo "."
```

**產出檔案**:
- `docs/plan/camera_screen_ui_redesign_L1.md`

---

### 情境 2: 評估 Sprint 3 後進行 UI 調整的可行性 (Level 2)

**對話範例**:
```
User: 基於剛才的 L1 規劃，評估在 Sprint 3 完成後進行 UI/UX 調整的可行性

Claude: 我來執行 L2 可行性評估
```

**執行命令**:
```bash
python tools/hybrid_flow.py \
  --level L2 \
  --topic "camera_screen_ui_redesign" \
  --issue "docs/plan/camera_screen_ui_redesign_L1.md" \
  --input "評估在 Sprint 3 (GuidelineOverlay 動態顯示完成後) 進行 UI/UX 調整的技術風險、工期預估、與現有架構的相容性" \
  --out "docs/plan" \
  --repo "."
```

**產出檔案**:
- `docs/plan/camera_screen_ui_redesign_L2_FEASIBILITY.md`

---

### 情境 3: 記錄實作完成 (PR Notes)

**對話範例**:
```
User: UI/UX 調整已完成，請幫我生成 PR 筆記

Claude: 我來生成 PR 實作記錄
```

**執行命令**:
```bash
python tools/hybrid_flow.py \
  --level PR \
  --topic "camera_screen_ui_redesign" \
  --input "完成 GuidelineOverlay 顏色調整 (白色→黃色)、透明度優化 (0.5→0.7)、線條粗細調整 (1.5dp→2dp)，調整控制按鈕位置" \
  --out "docs/plan" \
  --repo "."
```

**產出檔案**:
- `docs/plan/camera_screen_ui_redesign_PR_NOTES.md`

---

## 與 v2.7.0 開發計劃整合

### 建議時機點

| Sprint | 何時使用 Hybrid Flow | 用途 |
|--------|---------------------|------|
| **Sprint 1-2** | 不需要 | 功能開發中，暫無 UI 調整 |
| **Sprint 3 完成後** | ✅ L1 規劃 | 規劃 UI/UX 調整方案 |
| **UI 調整開工前** | ✅ L2 可行性 | 評估技術風險與工期 |
| **UI 調整完成後** | ✅ PR Notes | 記錄實作變更 |
| **Sprint 4 驗證** | ✅ PR Notes | 記錄跨平台驗證結果 |

---

## 輸出檔案位置

所有生成的文檔預設儲存在:
```
docs/plan/
├── <topic>_L1.md                    # Level 1 規劃
├── <topic>_L2_FEASIBILITY.md        # Level 2 可行性評估
└── <topic>_PR_NOTES.md              # PR 實作記錄
```

---

## 疑難排解

### 問題 1: 找不到 python 命令

**解決**:
```bash
# 檢查 Python 版本
python --version

# 或使用 python3
python3 tools/hybrid_flow.py --help
```

### 問題 2: 權限被拒絕

**原因**: Claude Code 沒有執行該命令的權限

**解決**: 檢查 `.claude/settings.local.json` 是否包含:
```json
"Bash(python tools/hybrid_flow.py --level L1:*)",
"Bash(python tools/hybrid_flow.py --level L2:*)",
"Bash(python tools/hybrid_flow.py --level PR:*)"
```

### 問題 3: 缺少 Google API Key

**錯誤訊息**:
```
Error: GOOGLE_API_KEY environment variable not set
```

**解決**: 設定環境變數 (如需使用 Gemini API)
```bash
# Windows PowerShell
$env:GOOGLE_API_KEY = "your-api-key"

# Linux/Mac
export GOOGLE_API_KEY="your-api-key"
```

---

## 快速命令模板

### L1 規劃
```bash
python tools/hybrid_flow.py --level L1 --topic "YOUR_TOPIC" --input "YOUR_DESCRIPTION" --out "docs/plan" --repo "."
```

### L2 可行性
```bash
python tools/hybrid_flow.py --level L2 --topic "YOUR_TOPIC" --issue "docs/plan/YOUR_TOPIC_L1.md" --input "YOUR_DESCRIPTION" --out "docs/plan" --repo "."
```

### PR 筆記
```bash
python tools/hybrid_flow.py --level PR --topic "YOUR_TOPIC" --input "YOUR_DESCRIPTION" --out "docs/plan" --repo "."
```

---

## 相關檔案

- **腳本**: `tools/hybrid_flow.py`
- **VSCode Tasks (Active)**: `.vscode/tasks.json` ⭐ 主要設定檔
- **VSCode Tasks (Reference)**: `tools/hybrid_flow-tasks.json` (已整合到 .vscode/tasks.json)
- **Claude 權限**: `.claude/settings.local.json`
- **開發計劃**: `docs/plan/v2.7.0_phase2_DEVELOPMENT_PLAN.md`

---

## 📝 整合狀態

### ✅ 完成項目
1. **Claude Code 整合** (2026-01-04)
   - 權限設定完成
   - 可在對話中直接執行

2. **VSCode Tasks 整合** (2026-01-04) ⭐ NEW
   - 已整合到 `.vscode/tasks.json`
   - 與 Build/Test tasks 統一管理
   - GUI 介面可用

### 📍 使用建議
- **CLI 偏好**: 使用方法 1 (直接執行命令)
- **GUI 偏好**: 使用方法 2 (VSCode Tasks)
- **Claude Code**: 直接在對話中要求執行

---

**最後更新**: 2026-01-04 23:50
**整合完成**: ✅ Claude Code + VSCode Tasks 雙重整合
