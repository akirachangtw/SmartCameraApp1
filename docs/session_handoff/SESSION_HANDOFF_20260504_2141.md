# Cueshot Handoff — 2026-05-04

## Dashboard（新 session 先讀這段）
- **完成**: D8-VERIFY-I1-I2 Sony XQ-BT52 實機驗證完畢（I1 Sensor Fallback ✅、I2 FocusIndicator 焦土 ✅、S3 amber dots ✅）；發現新 blocker I7（對焦圓環永動）與 I8（isAllAligned 幾何互斥永遠 false）；所有 Skills frontmatter 補齊符合 Agent Skills 規範；gstack workflow 升版至 v1.4。
- **阻塞**: I8 🔴 Critical — `AnalyzeCompositionUseCase.kt` `isAllAligned` 邏輯需改為加權平均 ≥ 70；I7 🟡 Medium — `EyeAfController.kt` 每 2s 無條件重觸發需加 `lens=Focused && drift<10px → skip` gate
- **下一步**: (1) `AnalyzeCompositionUseCase.kt:51` — 將 `isAllAligned = topInstruction == null` 改為加權平均 ≥ 70 邏輯；(2) `EyeAfController.kt` — 新增 Focused 狀態 + 漂移門控
- **帶入**: CLAUDE.md · PROGRESS.md · `app/src/main/java/.../domain/usecase/AnalyzeCompositionUseCase.kt` · `app/src/main/java/.../domain/eye/EyeAfController.kt`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `.claude/skills/compact/SKILL.md` | frontmatter | 新增 name/description/disable-model-invocation |
| `.claude/skills/dream/SKILL.md` | frontmatter | 新增 name/description/disable-model-invocation |
| `.claude/skills/handoff/SKILL.md` | frontmatter | 新增 name/description/disable-model-invocation |
| `.claude/skills/normal/SKILL.md` | frontmatter | 新增 name/description/disable-model-invocation |
| `.claude/skills/qa/SKILL.md` | frontmatter | 新增 name/description/disable-model-invocation |
| `.claude/skills/status/SKILL.md` | frontmatter | 新增 name/description（已有 disable-model-invocation） |
| `.claude/skills/sv/SKILL.md` | frontmatter | 補齊 |
| `.claude/skills/uc/SKILL.md` | frontmatter | 新增 name/description/disable-model-invocation |
| `.claude/skills/sv/CROSS_REVIEW.md` | 新增 | SV Cross-Review 規範文件 |
| `.claude/skills/sv/ssot_context.md` | 新增 | SV SSOT 脈絡文件 |
| `.claude/skills/sv/scripts/cross_review.sh` | 新增 | Cross-Review 執行腳本 |
| `devspec/gstack-lowcost-highdefense-workflow_v1_4.md` | 新增 | gstack workflow v1.4 |
| `devspec/gstack-lowcost-highdefense-workflow.md` | 刪除 | v1.3 舊版移除 |
| `.gemini/settings.json` | 新增 | Gemini 本機設定 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| I8 根因定性為「幾何互斥」而非實作 Bug | HeadroomRule (top∈[0.1,0.2]) 與 ThirdsRule (face center at 1/3 grid) 在任何現實拍攝距離下無法同時滿足 | `AnalyzeCompositionUseCase.kt` 需改加權邏輯 |
| I7 根因定性為 EyeAfController 設計缺陷 | stableId 每 2s 遞增觸發 AF，未檢查 lens=Focused 狀態，導致 UI 動畫永動 | `EyeAfController.kt` 需加 Focused gate |
| Skills frontmatter 合併為單一 Docs commit | 所有 skill 同屬 Agent Skills spec 補齊，目的一致 | `.claude/skills/*/SKILL.md` × 8 |
| gstack workflow v1.4 獨立 commit | 與 skill frontmatter 用途不同（workflow 規範 vs. skill spec） | `devspec/` |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~2 --stat`
核心檔案：`git diff HEAD~2 -p .claude/skills/sv/SKILL.md`
（N=2，本次工作 commits：82160d8、617d33b）

D8-VERIFY 實機測試結果詳見：`docs/session_handoff/SESSION_HANDOFF_20260424_1338.md`

</details>
