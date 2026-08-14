# INFRA-GITIGNORE-WL-02: `.gitignore` 建立 Pending/ 與 deliver/ 目錄級白名單

**簽發日期**: 2026-08-02
**簽發者**: GPT (Codex)
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: INFRA-GITIGNORE-WL-01 ✅ Done

## 檔案治理／來源說明

本票承接 Akira 2026-08-02 直接裁決：「優先以獨立 INFRA 治理票修改 `.gitignore`，建立 `Pending/`、`deliver/` 目錄級白名單」。現況 `.gitignore` 以 `plans/cso_briefing/project/*` 排除 project 直接子項，只反選 `TICKET_REGISTRY.json` 與 `project/file/`；但 `.claude/rules/ssot-governance.md` 已將 `Pending/` → `deliver/` 定義為票務單向生命週期。故本票只解除兩目錄之 ignore，使票面可被 Git 看見、審查與後續納管。

**邊界聲明**：本票不將既有票檔加入 index，也不宣稱「Git 追蹤完成」。開票時 live 盤點為 `Pending/` 29 檔、`deliver/` 27 檔（**盤點口徑（2026-08-03 補正）**：`Pending/` 29 = 直屬 24 + `meo-v2-lite/` 5；**未計** `meo-v2-lite/examples/` 次層。AI-2 巢狀 probe 之驗收不受此口徑限制）；白名單生效後它們預期顯示為 untracked。內容分類、敏感資料清查與 baseline `git add` 必須由後續獨立 migration ticket 承接。

**序號機械取號證據**：依 TICKET_TPL v3.3 對 `TICKET_REGISTRY.json`、`deliver/`、`reports/` 全冊 grep `INFRA-GITIGNORE-WL-[0-9]+`，最大號為 `01`，故本票取 `02`；禁只掃 `Pending/`。

## Verify Config
TICKET_ID=INFRA-GITIGNORE-WL-02
CREATED=2026-08-02
PRESET=docs
ALLOWED=.gitignore
FORBIDDEN=app/**,.claude/**,plans/architecture/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/file/**,plans/cso_briefing/project/Handoff/**,plans/cso_briefing/archive/**,plans/uita_project/**
MAX_LINES=30
DIFF_REF=1479f58
ANCHOR_PROBED=2026-08-02 20:04

**DIFF_REF 時序註記（2026-08-03 補正）**：`ANCHOR_PROBED`（08-02 20:04）早於 `1479f58`（08-02 20:13:43），惟該 commit 僅異動 `plans/cso_briefing/project/TICKET_REGISTRY.json`（本票與 INFRA-TPL-V33-02 之 intake），**未觸 `.gitignore`**，故 AI-0 錨點仍有效，無須重跑預檢。獨立復驗（CSO 2026-08-03，非採審查回報）：①`git_show 1479f58` 確認異動檔僅 registry；②`git_log --paths .gitignore` 顯示該檔最後異動為 `f0c9c20`（2026-07-31，WL-01），**自 ANCHOR_PROBED 起至今無任何 commit 觸及目標檔**。

**派工時順延預告**：`1479f58` 之後已有後續治理 commit（app repo），派工當下若 HEAD 已前進，`DIFF_REF` 應順延至派工前最新治理 commit，定性為 **Verify Config 機械基線調整、非 AC 修訂**（比照 TKR-02-FIX-G、TKR-05-A 先例）；否則固定 AC「`git diff --stat` 僅涉及 ALLOWED 檔案」將因區間納入無關治理 commit 而機械失敗。順延時 MUST 同批更新票面與 `tmp/verify_cmd.conf` 兩處，並復驗 `.gitignore` 於新區間內仍零異動。

**MAX_LINES 推導註記**：AI-1 淨增 3 行（註解 1 + 白名單 2）→ docs preset 下界與錨點漂移緩衝 → **30**。**截斷序**：persistent diff 超過 30 行或出現 `.gitignore` 以外檔案時停止回報，禁縮減驗收或順手納管既有票檔。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.gitignore`
- **現值**: 下列連續區塊於檔內命中恰 1 次，且順序一致：
```gitignore
# CSO / UITA project knowledge (local AI agent context, not committed)
# Exception: TICKET_REGISTRY.json is a Governance Artifact (project-scoped status
# dashboard, tracked by CSO ruling 2026-07-28) — Manifest (cross-project) stays
# untracked; Registry (this repo's own state) is version-controlled.
plans/cso_briefing/*
!plans/cso_briefing/project/
plans/cso_briefing/project/*
!plans/cso_briefing/project/TICKET_REGISTRY.json
# INFRA-GITIGNORE-WL-01: 治理文檔目錄白名單（消除同目錄新檔靜默 untracked 盲區）
!plans/cso_briefing/project/file/
```
- **動作**: 同時確認 `git check-ignore -v .gitignore` 無命中；若 HEAD 已越過 `ANCHOR_PROBED` 後的目標檔 commit，重跑錨點預檢並更新證據。
- [ ] 完成

### AI-1: 插入票務生命週期目錄白名單
- **檔案**: `.gitignore`
- **現值**:
```gitignore
# INFRA-GITIGNORE-WL-01: 治理文檔目錄白名單（消除同目錄新檔靜默 untracked 盲區）
!plans/cso_briefing/project/file/
```
- **目標值**:
```gitignore
# INFRA-GITIGNORE-WL-01: 治理文檔目錄白名單（消除同目錄新檔靜默 untracked 盲區）
!plans/cso_briefing/project/file/
# INFRA-GITIGNORE-WL-02: 票務生命週期目錄白名單（僅解除忽略；內容 baseline 另票）
!plans/cso_briefing/project/Pending/
!plans/cso_briefing/project/deliver/
```
- [ ] 完成

### AI-2: 真實 Git 可見性探測
- **檔案**: `.gitignore`
- **動作**: 僅暫建 `Pending/wl_probe_pending.md`、`Pending/wl_probe_nested/wl_probe_nested.md`、`deliver/wl_probe_deliver.md`；以 `git status --short --untracked-files=all -- <path>` 驗證三者皆顯示 `??`，再逐一刪除並 read-back 確認零殘留。禁只以 `git check-ignore` 代替使用者面可見性驗證。
- [ ] 完成

### AI-3: 負向邊界與 index 汙染檢查
- **檔案**: `.gitignore`
- **動作**: 驗證 `project/Handoff/` 與 `plans/cso_briefing/archive/` 仍被 ignore；`TICKET_REGISTRY.json` 與 `project/file/` 既有追蹤語義不變；`git diff --cached --name-only` 不得新增任何 `Pending/` 或 `deliver/` 內容。
- [ ] 完成

## Hard Constraints
1. Persistent diff 僅得修改 `.gitignore`，且只可在 `!plans/cso_briefing/project/file/` 後插入 AI-1 三行；禁改寫 AI-0 字面塊所列之既有反選集。**行號僅供定位、以字面塊為準**（live 取證實際位置為 L74-L79，非原稿所載 L70-L79；行號未經取證即書寫為本票起草缺陷，2026-08-03 補正）。
2. `Pending/`、`deliver/` 既有內容全部唯讀；除 AI-2 三個具名 probe 外，禁止新增、修改、刪除、搬移、stage 或 commit 任何票檔。
3. 本票終值只能宣告「兩目錄不再被 ignore、內容已 Git-visible」；禁止宣告既有內容已 tracked、已完成 baseline 或已解決冊面漂移。
4. 禁使用 `git add -f`；禁解除 `Handoff/`、`archive/`、`reports/` 或整個 `plans/cso_briefing/` 的忽略。
5. 實作 commit 只含 `.gitignore`，commit 前綴使用 `[INFRA-GITIGNORE-WL-02]`；Registry intake／closure 走獨立 `[GOVERNANCE-SYNC]` commit，不得混入實作 payload。
6. Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

## Error Handling
- AI-0 連續錨點命中數不等於 1、順序不符或目標檔已有未裁決異動 → 停止回報，禁自行找等價位置。
- `git check-ignore -v .gitignore` 命中 → 停止回報（gitignore 死結律）；`Pending/`、`deliver/` 在修改前被 ignore 是本票待修現值，不構成此標配停點。
- 任一白名單已由並行 session 寫入 → 停止回報，改走 no-op／冊面對帳裁決，禁重複插入。
- AI-2 任一 probe 未顯示 `??`、刪除後仍殘留，或 AI-3 負向路徑變為可見 → 停止回報，禁止 commit。
- `git diff --cached --name-only` 含任何 `Pending/`、`deliver/` 檔案，或 persistent diff 超出 ALLOWED → 停止回報並列出汙染來源，禁自行吸收 baseline 工作。

## Acceptance Criteria
- [ ] **[存在性]** `!plans/cso_briefing/project/Pending/` 與 `!plans/cso_briefing/project/deliver/` 各命中恰 1 次，WL-01 既有兩行逐字不變。
- [ ] **[行為性]** AI-2 三個真實 probe（Pending 根、Pending 巢狀、deliver 根）均由 `git status --short --untracked-files=all` 顯示 `??`，驗畢零殘留。
- [ ] **[行為性]** 至少各取一個既有 Pending／deliver 票檔驗證不再顯示 `!!`，且不得被 stage；證據記錄精確路徑。
- [ ] **[行為性]** `project/Handoff/` 與 `plans/cso_briefing/archive/` 仍命中 ignore；`TICKET_REGISTRY.json` 仍由 `git ls-files` 命中，`project/file/` 可見性不回歸。
  - **達成機制註記（2026-08-03 補正）**：`.gitignore` 內**無** `plans/cso_briefing/archive/` 專行，其 ignore 係經 L74 `plans/cso_briefing/*` 概括排除達成；驗收以 `git check-ignore -v` 之命中規則為準，禁以「找不到專行」判為未 ignore。另 L81 之 `plans/archive/` 係**不同路徑**，勿混淆。
- [ ] **[存在性]** `git diff --check` PASS，且 `.gitignore` 僅新增 AI-1 三行、零刪除。
- [ ] ~~`assembleDebug` PASS~~ **(N/A: docs preset — `.gitignore` 無編譯產物語義)**
- [ ] ~~**[可達性]**~~ **(N/A: `.gitignore` 為宣告式配置，無呼叫鏈／引用鏈可達性語義；使用者面可見性驗證已由上列 [行為性] AC 承載)**
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案。

## 後續承接（不在本票範圍）

- 另開 lifecycle baseline/migration ticket：分類既有 Pending/deliver 內容、執行 secrets／個資掃描、修正冊面錯置，再以明確 manifest 納入 index。該票 PASS 前不得宣告「票務資料已完成 Git 追蹤」。
