# Cueshot Handoff — 2026-05-14

## Dashboard（新 session 先讀這段）
- **完成**: FIX-P0-SESSION-B-POLISH（Top/Bottom Bar system insets + i18n）+ FIX-INFRA-CROSSREVIEW（sv_report.md untrack + cross_review.sh pathspec exclusion）全流程結案：QA ✅ / SV ✅ / Cross-Review PASS ✅ / 3 commits ✅
- **阻塞**: 無新阻塞；舊有 P1 blockers 見 PROGRESS.md Active Blockers（BUG-I7-FOCUS-RING-PERSIST、BUG-MLKIT-DUAL-EMIT、BUG-PTF-HALO-SIZE、BUG-SONY-ROTATION-CONST）
- **下一步**: Session C 目標 — ①P0-FPS：`CameraViewModel.kt` 調查 `imageInfo.rotationDegrees` vs `Surface.ROTATION_*`（Sony XQ-BT52 恆回報 0）；②P0-FOCUS：`EyeAfController.kt:process()` 加入 Guard 7（`LensFocusState.Focused + drift < 閾值 → skip`）+ `lastTriggerPoint` instance var
- **帶入**: CLAUDE.md · PROGRESS.md · CameraViewModel.kt · EyeAfController.kt

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `app/.../camera/CameraScreen.kt` | Top bar Row modifier | `statusBarsPadding()` + padding 8dp→16dp |
| `app/.../camera/CameraScreen.kt` | HDR Text | `"HDR"` → `stringResource(R.string.hdr_label)` (Rule 9 i18n) |
| `app/.../camera/CameraScreen.kt` | Top bar Row | 新增 `Spacer(Modifier.size(48.dp))` reserved slot（Lens Smudge 預留） |
| `app/.../components/BottomControlBar.kt` | Column 底部 Spacer | `weight(1f)` → `navigationBarsPadding()` (Rule 23) |
| `app/src/main/res/values/strings.xml` | P0-TOPBAR 區塊 | 新增 `<string name="hdr_label">HDR</string>` |
| `.claude/skills/sv/scripts/cross_review.sh` | line 183 git diff | `git diff "$DIFF_SCOPE"` → `git diff "$DIFF_SCOPE" -- ':!sv_report.md'` |
| `.gitignore` | SV workflow 區塊 | 新增 `sv_report.md` entry；`git rm --cached` 永久 untrack |
| `.claude/CLAUDE.md` | KAIROS #5 區塊後 | 追加 Rule 23（Overlay System Insets 紀律） |
| `.claude/PROGRESS.md` | Header + Sprint Health | Current Day + Schedule 同步至 FIX-P0-SESSION-B-POLISH QA ✅ |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| `git diff "$DIFF_SCOPE" -- ':!sv_report.md'` 保留雙引號 | 使用者指示：維持變數雙引號包覆，僅加 pathspec；去掉引號在部分場景可能破壞 range 語法 | cross_review.sh:183 |
| sv_report.md 改為 3-commit 策略而非 2-commit | 使用者要求「以檔案功能及用途原子化」：UI(1+2) / Infra(3) / Docs(4+5) 最終合為 UI / Infra / Docs 三組 | git history |
| cross-review 不重跑（/sv 階段 D）| 相同 diff scope，本 Session 已取得 PASS（session 019e2283），重跑為重複 credit 消耗 | SV flow |
| DIFF_SCOPE="HEAD" 而非預設 HEAD~1..HEAD | 變更在呼叫 cross-review 時仍未提交；commit 後後續 session 應用預設值（不傳第二參數） | cross_review.sh 使用方式 |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~3 --stat`
核心檔案：`git diff HEAD~3 -p app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`
（本次 3 commits：45641e9 / 208e685 / 7a08292）

SV workflow 修復驗證：
`git diff HEAD~3 -p .claude/skills/sv/scripts/cross_review.sh`

</details>
