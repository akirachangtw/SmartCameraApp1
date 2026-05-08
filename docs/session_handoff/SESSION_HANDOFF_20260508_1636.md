# Cueshot Handoff — 2026-05-08

## Dashboard（新 session 先讀這段）
- **完成**: W1 全部關帳 — INFRA-02（OpenCV 模組徹底移除）+ FIX-INFRA-02（GuidelineAnalysisResult 殭屍合約清除）完成；3 個 commit 成功推入 `feature/cueshot-mvp`
- **阻塞**: 無（I7 對焦圓環永動循環為 Medium，可選先做）
- **下一步**: W2 啟動 — S2(W2) → T02 → T03 → T04，讀 `plans/cso_briefing/Cueshot_Execution_Sequence_v1.md` 確認 ticket 細節
- **帶入**: CLAUDE.md · PROGRESS.md · `plans/cso_briefing/Cueshot_Execution_Sequence_v1.md`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `settings.gradle.kts` | top-level | 移除 `include(":opencv")` |
| `SmartCameraApplication.kt` | `onCreate()` KDoc | 清除 OpenCV 版本歷史 + Log 訊息簡化 |
| `ImageProxyExtensions.kt` | file header | 移除 OpenCV 標頭，保留 `toBitmap()` |
| `FeatureFlags.kt` | file KDoc | 替換為單行 KDoc |
| `CoordinateMapper.kt` | file header | 移除 `opencv.core.Point removed` 引用 |
| `GuidelineAnalyzer.kt` | entire file | **刪除**（unused stub，違反零-TODO 政策）|
| `GuidelineAnalysisResult.kt` | entire file | **刪除**（OpenCV Hough Line 孤兒型別）|
| `CameraRepository.kt` | interface | 移除 `guidelineResults: StateFlow<GuidelineAnalysisResult?>` |
| `CameraRepositoryImpl.kt` | impl | 移除 `_guidelineResults` + `guidelineResults` 實作 |
| `CameraViewModel.kt` | L50, L191-192 | 移除 import；型別改為 `Any?` (compile fix，0 UI consumers 已確認) + orphan TODO 標記 |
| `opencv/` (988 files) | entire module | **刪除**（build.gradle, haarcascades, Java API, native headers）|
| `opencv/etc/licenses/*.md` (9 files) | license docs | **刪除**（隨模組移除）|
| `.claude/PROGRESS.md` | Sprint Health | 更新至 W1 ✅ CLOSED，W2 待啟動 |
| `sv_report.md` | FIX-INFRA-02 report | 歸檔 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `CameraViewModel.guidelineResults` 型別改為 `Any?` | `GuidelineAnalysisResult.kt` 刪除後 compile 失敗；UI 有 0 consumers（grep 確認）；FIX-INFRA-02 scope ban 禁止邏輯變更 | `CameraViewModel.kt:191-192` |
| opencv/ commit 拆為 [Build] + [Docs] 兩個 | pre-commit hook 拒絕混合 `.java/.xml`（code）與 `.md`（docs）在同一 commit | 988 + 9 files |
| INFRA-02 cross-review diff 範圍限縮為 `app/src/ + settings.gradle.kts` | `git diff HEAD` 含 opencv/ 刪除 = ~34MB，超過 Codex stdin 限制；opencv/ 大量刪除另行聲明於 SV Report | sv_report.md |
| `paramsPreset` 保持 `"sprint5_optimized"` 不變 | Codex cross-review 標記為 [severity: high] logic change；INFRA-02 授權為零邏輯變更 | `GuidelineAnalysisResult.kt`（現已刪除）|

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

本次 commits（本 session）：3 個

```
git log --oneline -6
411d851 [Docs] INFRA-02 + FIX-INFRA-02 — W1 關帳歸檔
b835789 [Build] INFRA-02 — Delete opencv/ module (source, native, cascades, configs)
4d41f3b [Fix] INFRA-02 + FIX-INFRA-02 — Remove OpenCV zombie contract from source
```

全局變更：`git diff HEAD~3 --stat`
核心 source 變更：`git diff HEAD~3 -p app/src/`

未提交的 pre-existing 修改（需獨立審查）：
- `.claude/CLAUDE.md` — session health template 新增（4 行，行 55）
- `.claude/skills/sv/scripts/cross_review.sh` — W1 session 修改

</details>
