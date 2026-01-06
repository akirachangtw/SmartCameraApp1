# VSCode Tasks Integration - Hybrid Flow

**Integration Date**: 2026-01-04 23:50
**Status**: ✅ Successfully Integrated

---

## 📋 Overview

Hybrid Flow tasks have been integrated into the main VSCode tasks configuration (`.vscode/tasks.json`), unifying all development workflows in a single location.

---

## ✅ Integration Summary

### Before Integration
- Hybrid Flow tasks: `tools/hybrid_flow-tasks.json` (separate file)
- Build/Test tasks: `.vscode/tasks.json`
- **Issue**: Users need to manage two task files

### After Integration
- **All tasks**: `.vscode/tasks.json` (unified)
- **Reference**: `tools/hybrid_flow-tasks.json` (kept for documentation)
- **Benefit**: Single source of truth for all VSCode tasks

---

## 🎯 Available Tasks

### Build & Deploy Tasks
1. 🚀 Build Emulator Debug
2. 📱 Install to Emulator
3. 🚀 Build Device Debug
4. 📱 Install to Device
5. 🧹 Clean Build

### Test & Quality Tasks
6. 🧪 Run Unit Tests
7. 🔍 Run Lint
8. 📊 Generate Test Report
9. 🎯 Full Test Cycle

### Hybrid Flow Tasks ⭐ NEW
10. 📋 Hybrid: L1 Plan (inline input)
11. 📋 Hybrid: L1 Plan (input_file)
12. 📋 Hybrid: L2 Feasibility (inline input)
13. 📋 Hybrid: L2 Feasibility (input_file)
14. 📋 Hybrid: PR Notes (inline input)

**Total Tasks**: 14 tasks in unified configuration

---

## 🚀 How to Use

### Method 1: VSCode Command Palette
1. Press `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (Mac)
2. Type: "Tasks: Run Task"
3. Select from the list of available tasks
4. For Hybrid Flow tasks, enter required parameters when prompted

### Method 2: VSCode Task Menu
1. Click Terminal menu → Run Task...
2. Select task from the list

### Method 3: Keyboard Shortcut (Default Build Task)
- Press `Ctrl+Shift+B` to run the default build task (🚀 Build Emulator Debug)

---

## 📝 Task Parameters

### Hybrid Flow Tasks Inputs

All Hybrid Flow tasks use the following input parameters:

#### 1. `hybridTopic`
- **Type**: String
- **Description**: 本次任務 topic（用於檔名）
- **Default**: `camera_control_not_available`
- **Example**: `ui_ux_redesign`, `opencv_integration`, `camera_bug_fix`

#### 2. `hybridInput`
- **Type**: String
- **Description**: 需求/問題描述（可直接貼錯誤訊息、重現條件、裝置差異）
- **Default**: `實體手機切回 Live 模式後呼叫 setZoomRatio 出現 CameraControl not available；模擬器未重現。`
- **Used by**: L1 Plan (inline), L2 Feasibility (inline), PR Notes

#### 3. `hybridInputFile`
- **Type**: String (File Path)
- **Description**: input_file 路徑（例如 logs/logcat.txt 或 notes/issue.md）
- **Default**: `logs/logcat.txt`
- **Used by**: L1 Plan (input_file), L2 Feasibility (input_file)
- **Path Format**: ⭐ 支援 Windows 路徑 (`logs\logcat.txt` 或 `logs/logcat.txt`)

#### 4. `hybridIssuePath`
- **Type**: String (File Path)
- **Description**: Issue/計畫檔路徑（L2 建議填 L1 產出，如 docs/plan/<topic>_L1.md）
- **Default**: `docs/plan/camera_control_not_available_L1.md`
- **Used by**: L2 Feasibility tasks
- **Path Format**: ⭐ 支援 Windows 路徑 (`docs\plan\file.md` 或 `docs/plan/file.md`)

---

## 🔍 Task Details

### 📋 Hybrid: L1 Plan (inline input)

**Purpose**: Generate Level 1 planning document with inline description

**When to use**:
- Starting a new feature or bug fix
- Initial problem analysis
- Defining scope and requirements

**Workflow**:
1. Run task → Enter topic name
2. Enter problem description (inline text)
3. Script generates: `docs/plan/<topic>_L1.md`

**Example**:
```
Topic: ui_ux_redesign
Input: 重新調整 CameraScreen 排版，優化 GuidelineOverlay 視覺層次
Output: docs/plan/ui_ux_redesign_L1.md
```

---

### 📋 Hybrid: L1 Plan (input_file)

**Purpose**: Generate Level 1 planning document from external file

**When to use**:
- Problem description is in a log file
- Issue details are in a markdown file
- Description is too long for inline input

**Workflow**:
1. Run task → Enter topic name
2. Enter file path (e.g., `logs/crash_logcat.txt`)
3. Script reads file and generates: `docs/plan/<topic>_L1.md`

**Example**:
```
Topic: camera_crash_analysis
Input File: tmp/logcat_dumps/camera_crash_20260104.log
Output: docs/plan/camera_crash_analysis_L1.md
```

---

### 📋 Hybrid: L2 Feasibility (inline input)

**Purpose**: Generate Level 2 feasibility assessment

**When to use**:
- After L1 planning is complete
- Need to evaluate technical risks
- Estimating implementation effort

**Workflow**:
1. Run task → Enter topic name
2. Enter issue path (usually the L1.md file)
3. Enter additional context (inline)
4. Script generates: `docs/plan/<topic>_L2_FEASIBILITY.md`

**Example**:
```
Topic: ui_ux_redesign
Issue Path: docs/plan/ui_ux_redesign_L1.md
Input: 評估在 Sprint 3 完成後進行的技術風險
Output: docs/plan/ui_ux_redesign_L2_FEASIBILITY.md
```

---

### 📋 Hybrid: L2 Feasibility (input_file)

**Purpose**: Generate Level 2 feasibility with external context file

**When to use**:
- Additional context is in a separate file
- Performance logs or profiler data available
- Complex analysis requiring multiple data sources

**Workflow**:
1. Run task → Enter topic name
2. Enter issue path (L1.md file)
3. Enter input file path (additional context)
4. Script generates: `docs/plan/<topic>_L2_FEASIBILITY.md`

---

### 📋 Hybrid: PR Notes (inline input)

**Purpose**: Generate PR implementation notes

**When to use**:
- After implementation is complete
- Documenting changes made
- Creating PR description draft

**Workflow**:
1. Run task → Enter topic name
2. Enter implementation summary (inline)
3. Script generates: `docs/plan/<topic>_PR_NOTES.md`

**Example**:
```
Topic: ui_ux_redesign
Input: 完成 GuidelineOverlay 顏色調整、透明度優化、線條粗細調整
Output: docs/plan/ui_ux_redesign_PR_NOTES.md
```

---

## 📂 File Structure

### Configuration Files
```
.vscode/
└── tasks.json                    # Main tasks configuration (ALL tasks)

tools/
└── hybrid_flow-tasks.json        # Reference copy (kept for documentation)
```

### Output Files
```
docs/plan/
├── <topic>_L1.md                 # Level 1 planning
├── <topic>_L2_FEASIBILITY.md     # Level 2 feasibility
└── <topic>_PR_NOTES.md           # PR implementation notes
```

---

## 🔄 Task Workflow Example

### Complete Feature Development Workflow

```
1. Initial Planning
   → Run: 📋 Hybrid: L1 Plan (inline input)
   → Topic: "camera_zoom_control"
   → Output: docs/plan/camera_zoom_control_L1.md

2. Feasibility Assessment
   → Run: 📋 Hybrid: L2 Feasibility (inline input)
   → Topic: "camera_zoom_control"
   → Issue: docs/plan/camera_zoom_control_L1.md
   → Output: docs/plan/camera_zoom_control_L2_FEASIBILITY.md

3. Implementation
   → Code changes...
   → Testing...

4. PR Documentation
   → Run: 📋 Hybrid: PR Notes (inline input)
   → Topic: "camera_zoom_control"
   → Input: "實作 SeekBar zoom 控制，支援 0.5x-10x 範圍"
   → Output: docs/plan/camera_zoom_control_PR_NOTES.md

5. Code Review & Merge
   → Use PR_NOTES.md as PR description
   → Commit changes
```

---

## ⚙️ Technical Details

### Task Configuration

Each Hybrid Flow task is configured with:

```json
{
  "label": "📋 Hybrid: L1 Plan (inline input)",
  "type": "shell",
  "command": "python",
  "args": [
    "tools/hybrid_flow.py",
    "--level", "L1",
    "--topic", "${input:hybridTopic}",
    "--input", "${input:hybridInput}",
    "--out", "docs/plan",
    "--repo", "${workspaceFolder}"
  ],
  "options": {
    "cwd": "${workspaceFolder}"
  },
  "problemMatcher": [],
  "presentation": {
    "reveal": "always",
    "panel": "dedicated"
  }
}
```

### Key Features

1. **Dynamic Inputs**: Uses VSCode input variables (`${input:...}`)
2. **Dedicated Panel**: Output shown in dedicated terminal panel
3. **Working Directory**: Automatically set to workspace folder
4. **Problem Matcher**: Disabled (not needed for document generation)

---

## 🔧 Troubleshooting

### Issue: Task not appearing in list

**Solution**:
1. Reload VSCode window (Ctrl+Shift+P → "Reload Window")
2. Check `.vscode/tasks.json` for syntax errors
3. Verify JSON format is valid

### Issue: Python command not found

**Solution**:
1. Verify Python is installed: `python --version`
2. Check Python is in system PATH
3. Try using `python3` instead of `python` (edit tasks.json)

### Issue: Input prompts not showing

**Solution**:
1. Check `inputs` section exists in tasks.json
2. Verify input IDs match task arguments
3. Restart VSCode

### Issue: Script execution fails

**Solution**:
1. Check working directory is correct
2. Verify `tools/hybrid_flow.py` exists
3. Test script manually: `python tools/hybrid_flow.py --help`

---

## 📊 Benefits of Integration

### Before
- ❌ Separate task files to manage
- ❌ Inconsistent task organization
- ❌ Need to remember which file contains which tasks

### After
- ✅ Single unified task configuration
- ✅ All tasks visible in one list
- ✅ Consistent emoji-based labeling
- ✅ Easier maintenance and updates
- ✅ Better developer experience

---

## 🔗 Related Documentation

- **Hybrid Flow Usage**: `tools/HYBRID_FLOW_CLAUDE_USAGE.md`
- **Critical Files Protection**: `docs/CRITICAL_FILES_PROTECTION.md`
- **Session Summary**: `docs/SESSION_SUMMARY_20260104.md`
- **Development Plan**: `docs/plan/v2.7.0_phase2_DEVELOPMENT_PLAN.md`

---

## 📝 Maintenance Notes

### When to Update

Update `.vscode/tasks.json` when:
1. Adding new Hybrid Flow task variants
2. Changing default input values
3. Modifying task behavior or arguments
4. Adding new workflow automation

### Updating Reference File

When updating `.vscode/tasks.json`, also update `tools/hybrid_flow-tasks.json` to keep reference documentation synchronized.

### Version History

- **v1.0** (2026-01-04): Initial integration of Hybrid Flow tasks
- Future updates will be documented here

---

**Last Updated**: 2026-01-04 23:50
**Integration Status**: ✅ Complete
**Total Tasks**: 14 tasks (9 build/test + 5 hybrid flow)
