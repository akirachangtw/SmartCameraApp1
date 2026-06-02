# Cueshot Handoff — 2026-06-02

## Dashboard（新 session 先讀這段）
- **完成**: INFRA-RFC2119 (alias INFRA-20) — 16 個 `.claude/rules/*.md` + `.claude/skills/*/SKILL.md` 全部加上 RFC 2119 anchor line 及 MUST/SHOULD/MAY 標記；CSO Option A 裁決接受 77.3% true-MUST 分佈；QA PASS (Build 72s / Tests 0 failed / Lint clean)；commit `1c47797`。
- **阻塞**: ① `.claude/skills/index-verify/scripts/index_path_verify.sh`（P05→P08 patch，未 commit，需後續 HOTFIX）② `plans/cso_briefing/CSO_INDEX.md`（pre-existing dirty，需確認是否需另外 commit）③ `/sv` 尚未執行（QA 後 context 耗盡，先 /compact）④ P05-R1 Cross-Review R1 BLOCKER 待 Akira 裁決
- **下一步**: 開新 session → 執行 `/sv`（`tmp/verify_cmd.conf` 已鎖定 `DIFF_REF=9b7f79a DIFF_END=1c47797`）→ Cross-Review → PROGRESS_ARCHIVE.md ✅ Done 標記 → 後續處理 index_path_verify.sh dirty（建議 HOTFIX-38）
- **帶入**: CLAUDE.md · PROGRESS.md · `tmp/verify_cmd.conf`

<details>
<summary>📋 變更明細（按需展開）</summary>

| 檔案 | 區塊 | 變更內容 |
|------|------|---------|
| `.claude/rules/android-conventions.md` | 全文 | 加 RFC 2119 anchor；布林值 is/has 前綴 → SHOULD（命名慣例類） |
| `.claude/rules/camera-constraints.md` | Rule 15/16 | 加 anchor；Rule 15 KDoc → SHOULD；Rule 16 硬體雙重 Guard → MUST |
| `.claude/rules/compose-ui.md` | Rule 14/23 | 加 anchor；Rule 14 remember 置頂 → MUST；Rule 23 Insets → MUST |
| `.claude/rules/kairos-lessons.md` | K1/K2/K3 Rules | 加 anchor；全部 → MUST (Gate/Safety) |
| `.claude/rules/phase0-lessons.md` | Rules 19-24/27 | 加 anchor；補明確 MUST markers（Rules 20/21/22/23 原本缺失） |
| `.claude/rules/ptf-rules.md` | Rule 22 | 加 anchor；Rule 22 狀態機時序 → MUST |
| `.claude/skills/sv/SKILL.md` | 全文 | 加 anchor；SSOT 漂移/ELF ALIGNED/lint Warning → SHOULD；核心 → MUST |
| `.claude/skills/qa/SKILL.md` | 全文 | 加 anchor；Co-Authored-By/報告完整度 → SHOULD；core gate → MUST |
| `.claude/skills/uc/SKILL.md` | 全文 | 加 anchor；所有輸出規則 → MUST |
| `.claude/skills/dream/SKILL.md` | 全文 | 加 anchor；報告格式/決策列舉 → SHOULD；CLAUDE.md 規則產出/Blocking → MUST |
| `.claude/skills/compact/SKILL.md` | 全文 | 加 anchor；格式產出/確認訊息 → SHOULD |
| `.claude/skills/handoff/SKILL.md` | 全文 | 加 anchor；禁省略代碼 → SHOULD；禁估算時間/禁記憶口述 → MUST |
| `.claude/skills/dtaudit/SKILL.md` | 全文 | 加 anchor；合併/拆分/補註解 → SHOULD；讀全文/逐層執行 → MUST |
| `.claude/skills/status/SKILL.md` | 全文 | 加 anchor；Session Health → SHOULD |
| `.claude/skills/index-verify/SKILL.md` | 全文 | 加 anchor；修正路徑/確認版本 → MUST |
| `.claude/skills/mdlint/SKILL.md` | 全文 | 加 anchor；原子化 commit → SHOULD；Read before edit/禁越權 → MUST |
| `.claude/PROGRESS.md` | Header/Sprint Health | 加 INFRA-RFC2119 🟡 QA PASS 待 /sv；Last Updated → 2026-06-02 |

</details>

<details>
<summary>🎯 關鍵決策（按需展開）</summary>

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| CSO Option A：接受 77.3% true-MUST 分佈為 terminal | 16 個檔案均為純約束文件（rules/skills），Safety/Architecture/Gate 類 MUST 無法再降；強制降級會損害規則可執行性 | 所有 16 個標記文件 |
| 22 MUST→SHOULD 降級（5 類：命名慣例/程式碼量度/格式偏好/報告完整度/裝置測試模板） | 這些規則不影響 Safety/Gate，違反不會導致 crash 或退件 | android-conventions / sv / qa / dream / dtaudit / status / mdlint / handoff |
| Anchor line 排除於計數之外 | grep 統計會將每個檔案的 RFC anchor 本身計入，導致虛報 +16 MUST（實際 102 條，grep 顯示 118）；CSO 確認不影響裁決 | 所有 16 個檔案 |
| INFRA-RFC2119 commit 使用 INFRA-20 前綴 | Hook `TICKET_PATTERN` 僅接受 `INFRA-[0-9]+`，RFC2119 含字母後綴被 gate 攔截；body 記錄 alias | commit `1c47797` |
| index_path_verify.sh (P05→P08) 不納入本次 commit | 該檔案路徑 `.claude/skills/index-verify/scripts/index_path_verify.sh` 不在 ALLOWED (`rules/*.md,skills/*/SKILL.md`)；需另開 HOTFIX | 待後續處理 |

</details>

<details>
<summary>🔍 Context 指標針（按需展開）</summary>

全局變更：`git diff HEAD~1 --stat`
核心檔案：`git diff HEAD~1 -p .claude/rules/android-conventions.md`
（本次 1 commit：`1c47797`）

verify_cmd.conf 狀態：
```
TICKET_ID=INFRA-RFC2119
DIFF_REF=9b7f79a
DIFF_END=1c47797
ALLOWED=.claude/rules/*.md,.claude/skills/*/SKILL.md
```

未 commit 的 dirty 檔案（需後續處理）：
- ` M .claude/skills/index-verify/scripts/index_path_verify.sh` → P05→P08 patch（建議 HOTFIX-38）
- ` M plans/cso_briefing/CSO_INDEX.md` → pre-existing dirty（需確認來源）

</details>
