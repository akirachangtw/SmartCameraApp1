# Index Verify Workflow

When the user invokes `/index-verify`, perform the following steps to validate the INDEX maintenance.

## Step 1: Execute Verification Script
// turbo
Run the following bash script to verify INDEX paths, clean up blank lines, detect stale versions, and update auto-index routing.
```powershell
bash .claude/skills/index-verify/scripts/index_path_verify.sh
```

## Step 2: Handle Exit Code & Warnings
Evaluate the exit code from the script:
- **0 (Pass):** Check the diff to confirm routing and cleanup are as expected.
- **1 (Fail):** There are missing paths or directories. You **MUST** correct the paths in the INDEX/SOP files, or recreate the missing files/directories.
- **2 (Environment Error):** **MUST** confirm execution is from the project root.

Process any warnings (`⚠️`):
- **Expired SSOT/Layout reference (CLAUDE.md / PROGRESS.md):** **MUST** have a Ticket + Cross-Review Checkpoint to fix.
- **Expired reference in INDEX files:** **SHOULD** fix the INDEX document directly.
- **Duplicate cases:** **MUST** confirm the correct version and delete duplicates.
- **🔴 SOP semantic reference deleted:** **MUST** manually check if the SOP load command needs correction.

## Step 3: Output Report
Generate and output the following report to the user based on the script's output:

```text
=== Index Verify Report v2.0 ===
📁 Scope File Inventory: CSO / UITA / Architecture listed
📁 CSO INDEX+SOP: ✅ [N] 通過 / ❌ [N] 失敗 / 🧹 [N] 自動修正 / 📌 [N] 路由新增 / 🧾 [N] fallback 新增
📁 UITA INDEX+SOP: ✅ [N] 通過 / ❌ [N] 失敗 / 🧹 [N] 自動修正 / 📌 [N] 路由新增 / 🧾 [N] fallback 新增
📌 Version: SSOT=[current] / Layout=[current]
⚠️  警告: [N] 項（過期引用 / 大小寫重複 / SOP 語意斷裂）
→ 結論: PASS / FAIL
```
