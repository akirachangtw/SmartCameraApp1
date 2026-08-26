# INFRA-TRUNK-SYNC-01: main 分支 fast-forward 追平 feature/cueshot-mvp，並同步遠端 GitHub main，作為 Claude Worktree 平行開發模式的穩定基線切換點

**簽發日期**: 2026-08-25
**簽發者**: Claude
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=INFRA-TRUNK-SYNC-01
CREATED=2026-08-25
PRESET=docs
ALLOWED=N/A（純 git ref 操作，不涉及任何 tracked 檔案內容異動）
FORBIDDEN=所有 tracked 檔案（本票禁止產生任何工作目錄檔案異動；唯一允許的變更是 `main` 與 `origin/main` 兩個 ref 指標前移）
MAX_LINES=N/A（零檔案異動之 ref-only 操作，diff insertions+deletions 量尺不適用）
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-08-25 13:35

**背景與依據**：本 session 已完成唯讀評估（`git merge-base --is-ancestor main feature/cueshot-mvp` 回傳 true），確認 `main`（本地 `9e26631`，遠端 `origin/main` `206ba82`）為 `feature/cueshot-mvp`（`dcfd863`，領先 852 commits）之純祖先，零衝突風險。使用者裁決採 `--ff-only` 語意（能前移才前移，前移不了就停止，不偷改為一般 merge），目的是將 `main` 建立為後續 Claude Worktree 平行開發模式的穩定基線。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **動作**: 逐字重新執行以下五條指令，確認現值與票面所載相符：
  1. `git merge-base --is-ancestor main feature/cueshot-mvp`（預期 exit 0）
  2. `git rev-parse main`（預期 `9e26631...`）
  3. `git rev-parse origin/main`（預期 `206ba82...`）
  4. `git fetch origin main` 後執行 `git merge-base --is-ancestor origin/main feature/cueshot-mvp`（預期 exit 0）——remote-tracking ref 之新鮮度止於上次 fetch，本條將「遠端已被他端推進而分歧」之發現點自 AI-2 執行期提前至復驗期（Rev A / F1）
  5. `git worktree list` 確認**無任何 linked worktree 檢出 `main`**——若有，`git fetch . feature/cueshot-mvp:main` 將被 git 以「ref 已在他處 checkout」拒絕；本條同時留存 worktree 清單快照供 AI-3 第 4 條比對（Rev A / F2）
- **任一項不符 → 停止回報，禁自行推定等價狀態，升報 CSO 重新評估。**
- [x] 完成 — 五項全數相符（`is-ancestor` exit 0 ×2、`main`=9e26631、`origin/main`=206ba82、`worktree list` 快照單一 worktree 於 `feature/cueshot-mvp`）

### AI-1: 本地 main ref 前移（fast-forward-only，不切換目前 checkout）
- **檔案**: N/A（git ref 操作，非檔案異動）
- **現值**: `main` = `9e26631`（`feature/cueshot-mvp` 之純祖先，0 個 main 獨有 commit）
- **目標值**: `main` = `dcfd863`（`feature/cueshot-mvp` 現在 tip）
- **動作**: `git fetch . feature/cueshot-mvp:main`
  （此語法對本地 ref 的更新預設即 fast-forward-only——非 fast-forward 時 git 會拒絕，除非以 `+` 前綴強制；本票禁用 `+` 前綴。選用此語法而非 `git checkout main && git merge --ff-only` 之理由：不切換目前工作目錄的 checkout 分支，避免中斷 `feature/cueshot-mvp` 上的既有工作狀態。）
- [x] 完成 — `9e26631..dcfd863` fast-forward 成功，未使用 `+` 強制前綴

### AI-2: 遠端 origin/main 同步推送
- **檔案**: N/A（git ref 操作，非檔案異動）
- **現值**: `origin/main` = `206ba82`
- **目標值**: `origin/main` = `dcfd863`（與本地 main 一致）
- **動作**: `git push origin main`
- [x] 完成 — `206ba82..dcfd863` 推送成功，未使用 `--force`

### AI-3: 收尾驗證
- **動作**: 確認以下四項：
  1. `git rev-parse main` == `git rev-parse origin/main` == `git rev-parse feature/cueshot-mvp`
  2. `git branch --show-current` 回報 `feature/cueshot-mvp`（確認 checkout 未被切換）
  3. `git status --short` 於工作目錄回報空（確認未產生任何 tracked 檔案異動）
  4. `git worktree list` 與 AI-0 第 5 條快照逐字一致（本票全程未新增、移除任何 worktree，亦未改變任何 worktree 之 checkout 分支）（Rev A 改寫：原「未產生非預期新 worktree」為空檢查——ref 操作不可能產生 worktree，有意義的斷言是與前置快照比對）
- [x] 完成 — 1/2/4 三項相符；**第 3 項不符**（`git status --short` 非空，但為開工前既存外部 dirty，非本票 fetch/push 造成，見收尾揭露段落）

## Hard Constraints
1. 全程禁止 `git checkout main` / `git switch main`——本票以 `git fetch . feature/cueshot-mvp:main` 達成 ref 前移，MUST NOT 切換目前工作目錄之 checkout 分支。
2. 禁止使用 `git merge`（無 `--ff-only`）、`git merge --no-ff`、`git rebase`，或任何會產生新 merge commit／改寫既有 852 個 commit 線性歷史的操作。
3. 禁止對 `origin/main` 使用 `--force` 或 `--force-with-lease` 推送。
4. AI-0 錨點復驗或 AI-1 前置的祖先關係檢查若不成立，MUST 停止回報並升報 CSO，禁止自行改採其他合併策略續行。

## Error Handling
- `git merge-base --is-ancestor main feature/cueshot-mvp` 於執行當下回傳非 0（可能因並行 session 已對 main 或 feature/cueshot-mvp 產生新分歧 commit）→ 停止回報，升報 CSO 重新評估合併策略。
- `git fetch . feature/cueshot-mvp:main` 因非 fast-forward 遭拒 → 停止回報，禁止改用 `+` 前綴強制。
- `git fetch . feature/cueshot-mvp:main` 因 `main` 已被某 linked worktree 檢出而遭拒 → 停止回報，禁止於該 worktree 內以任何方式繞過（如先 detach 該 worktree），升報 CSO 裁決 worktree 處置序（Rev A / F2）
- `git push origin main` 遭遠端拒絕（non-fast-forward，代表 `origin/main` 於本操作期間被他人推進）→ 停止回報，禁止 `--force` 續推，升報 CSO。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報（gitignore 死結律）。本票無實體檔案標的，此項標記 N/A。

## Acceptance Criteria
- [x] **[存在性]** `git rev-parse main` 等於執行完成時 `feature/cueshot-mvp` 之 tip SHA — PASS（三者均 `dcfd863...`）
- [x] **[存在性]** `git rev-parse origin/main` 等於 `git rev-parse main`（本地與遠端一致）— PASS
- [x] **[行為性]** `git branch --show-current` 於操作完成後仍回報 `feature/cueshot-mvp` — PASS
- [ ] **[行為性]** `git status --short` 於 `feature/cueshot-mvp` 工作目錄回報空（本操作未產生任何 tracked 檔案異動）— **不符**：工作目錄存在開工前既有外部異動（`TICKET_REGISTRY.json`）與票檔自身，均與本票 `fetch`/`push` 操作無因果關係；Codex Cross-Review 已核驗此揭露不構成 BLOCKER/REPORT_FIX（ADVISORY，REACHABILITY: NONE），依撤警正向取證律保留未勾
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，本票不涉及原始碼異動；本輪仍執行並 PASS)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（ALLOWED=N/A；已依 `.claude/rules/decommission.md` `ZERO-DIFF-DECLARED` 協議顯式宣告零 diff，授權依據：本票 AC 本行 + `DIFF_REF==HEAD==dcfd863` 機械前提成立）

## 收尾揭露（2026-08-25）

- 實質成果：`main`／`origin/main`／`feature/cueshot-mvp` 三者已對齊 `dcfd863`，GitHub 遠端 `main` 同步完成，作為後續 Claude Worktree 平行開發模式的穩定基線。
- `/qa` 全 PASS（`assembleDebug` 47s、`testDeviceDebugUnitTest` 209/209、0 new lint）；Step 4／4.5（PROGRESS.md 更新／Auto Commit）依票面 FORBIDDEN=「所有 tracked 檔案」命中，SKIPPED — DEFERRED-TO-DOCSYNC，同步債已登記待下輪標準 SOP 收尾。
- `/sv` 階段 A-C 全 PASS（Compile 1s 快取命中、Lint 0 new warnings、16KB ELF ALIGNED、語意審查／架構合規 N/A—零程式碼異動）。
- `/sv` 階段 D Codex Cross-Review：**ADVISORY**（exit 0，REACHABILITY: NONE）——指出 AC「`git status --short` 回報空」未達成，但依 BLOCKER 白名單不構成 BLOCKER／REPORT_FIX，僅記錄觀察，流程繼續。
- `reviews.codex` 記為 `PASS`（沿用既有先例：ADVISORY 為非阻斷終局判定，如 `CUE-SNAP-01-FIX-A` 同型記法），非 `N_A`——Codex 確實已執行審查並產出判定，非未審查。
- `lifecycle=REVIEW`、票檔搬 `deliver/`、`TICKET_REGISTRY.json` 轉態均依 Akira 2026-08-25 對話直接指示執行，非 Desktop 治理 session 常態寫入。


---

## 收束覆核（SMTR，2026-08-25，REVIEW→CLOSED）

獨立 live git 驗證（不採信執行者回報）：`main..origin/main` 與 `origin/main..main` 雙向 range 皆空（本地與遠端已對齊 `dcfd863`）；`feature..main` 空（main 仍為純祖先）；`main..feature` 僅 1 筆 `44be7db`（執行後之 [GOVERNANCE-SYNC] REVIEW 轉態 commit，預期時序——AC「main == feature tip」於執行完成當刻成立）。checkpoint 所載「`git status --short` 非空」偏差覆核成立：該異動（TICKET_REGISTRY.json、票檔自身）為開工前既有之外部異動，與 fetch/push ref 操作零因果，ZERO-DIFF 精神未破，Codex 已核驗非 BLOCKER。lifecycle 轉 CLOSED 依 Akira 2026-08-25 對話明示確認。冊面收束註記因平台 isolated project 隔離（`REGISTRY_CROSS_PROJECT_REF` 拒收 append_note）改載於本檔，upsert 通道已同步 lifecycle=CLOSED 與 deliver 路徑。
