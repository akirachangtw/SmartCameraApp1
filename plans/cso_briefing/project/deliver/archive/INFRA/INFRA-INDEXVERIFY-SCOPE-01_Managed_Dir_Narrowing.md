# INFRA-INDEXVERIFY-SCOPE-01: index-verify 檔案異動管理範圍收斂至三個管理夾

**簽發日期**: 2026-07-19
**簽發者**: Claude（Akira 對話授權開票，2026-07-19）
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A（純 .sh/.md 治理票，Codex Cross-Review PASS 即閉環，免 Traycer）
**前置依賴**: 無

## 檔案治理/來源說明

現行 `index_path_verify.sh` 的 ② 目錄覆蓋率 + 智慧路由掃描整棵角色目錄（`plans/cso_briefing/`、`plans/uita_project/`），把 archive/reports/Handoff/Pending/deliver/role 等域外檔案一併納入「檔案異動管理」。Akira 要求：檔案異動管理只針對 `plans/architecture/`、`plans/cso_briefing/project/file/`、`plans/uita_project/file/` 三夾。

## Verify Config
TICKET_ID=INFRA-INDEXVERIFY-SCOPE-01
CREATED=2026-07-19
PRESET=refactor
ALLOWED=.claude/skills/index-verify/scripts/index_path_verify.sh,.claude/skills/index-verify/SKILL.md
FORBIDDEN=plans/cso_briefing/CSO_INDEX.md,plans/uita_project/UITA_INDEX.md,.claude/CLAUDE.md,.claude/rules/*
MAX_LINES=80
DIFF_REF=0cc6258

## Action Items

### AI-1: `smart_auto_index` / `check_role` 引入 scan_dir 參數（與 role_dir 分離）
- **檔案**: `.claude/skills/index-verify/scripts/index_path_verify.sh`
- **現值**: ② 掃描與路由分類共用 `role_dir`（整棵角色目錄）
- **目標值**: 新增 `scan_dir`；② `collect_files` 改用 `scan_dir`；`classify_file_section` / `is_in_role_dir`（① 邊界）維持 `role_dir`
- [x] 完成

### AI-2: Main check_role 傳入 scan_dir + Scope File Inventory 縮至三夾
- **檔案**: `.claude/skills/index-verify/scripts/index_path_verify.sh`
- **目標值**: CSO scan_dir=`plans/cso_briefing/project/file`、UITA scan_dir=`plans/uita_project/file`；`list_files` 縮至三個管理夾（抉擇 A）
- [x] 完成

### AI-3: SKILL.md 文件同步
- **檔案**: `.claude/skills/index-verify/SKILL.md`
- **目標值**: 「驗證範圍」表、「智慧路由規則」表、「執行順序 ④」同步反映 ② 縮小後 scope；`reports/*` 路由規則附註文字改為 HC#2 之永久定案語言（移除「若日後 scope 擴張至 reports/ 即自動生效」暫定措辭）
- [x] 完成（`SKILL.md:44` 現場核對，`reports/*` 附註改為永久定案語言；「驗證範圍」表 + 「執行順序 ④」已由 AI-1/AI-2 實作同步反映縮小後 scope，無需額外變更）

## Hard Constraints
1. `role_dir` 維持整棵樹供 ⓪ 缺檔清理、① 引用驗證 + 跨角色邊界（`is_in_role_dir`）——**MUST NOT** 縮至 scan_dir
2. `reports/` 路由規則**永久定案為「引用登記制」**：路由 pattern 表項（`reports/|審查報告|...`）保留供人工引用登記格式參考，**MUST** 永久禁止 `scan_dir` 再擴張涵蓋 `reports/`；`reports/*` 檔案登記 **MUST** 僅透過人工於 INDEX 手動新增引用行，**MUST NOT** 恢復資料夾掃描（Akira 2026-07-19 對話裁決最終定案，取代原「保留待日後 scope 擴張即自動生效」之暫定語言）
3. `check_architecture` 不動（已正確限定 `plans/architecture`）
4. **MUST NOT** 手動編輯 INDEX 檔案內容（由 script 管理）

## Error Handling
- `classify_file_section` 相對路徑計算依賴 role_dir，若誤改為 scan_dir 導致 `project/file/` route 失配 → 停止回報
- `bash -n` 語法檢查 FAIL → 停止回報

## Acceptance Criteria
- [x] **[存在性]** scan_dir 參數存在於 `smart_auto_index` 與 `check_role`（grep 確認）
- [x] **[行為性]** 實跑 script：② 僅掃三個管理夾；archive/reports/Handoff 不再列 `[UNLISTED]`；INDEX 檔案零 diff（未刪未增，冪等）
- [x] **[行為性]** ① 引用驗證仍覆蓋全 ref（archive/、Handoff/、deliver/ 仍 ✅，跨角色邊界完整）
- [x] **[存在性]** `bash -n` 語法 PASS
- [x] **[存在性]** SKILL.md 三處說明與 script 行為一致（grep 確認，`SKILL.md:44` reports/* 永久定案語言已落地）
- [x] **[可達性]** Codex Cross-Review 終局 PASS（R2 PASS，`0cc6258..9a84b81`，`.tmp/cross_review_result_20260719_192437.md`；R1 REPORT_FIX 為 sv_report.md Compile 秒數與 diff 不符之報告瑕疵，修報告後 R2 轉 PASS）
