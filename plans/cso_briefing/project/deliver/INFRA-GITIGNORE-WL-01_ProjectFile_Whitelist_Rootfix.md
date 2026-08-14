# INFRA-GITIGNORE-WL-01: .gitignore 增 `!plans/cso_briefing/project/file/` 白名單（同目錄新檔靜默 untracked 盲區根治）

**簽發日期**: 2026-07-31
**簽發者**: Claude (SMTR / CSO 主 session)
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: 無（與 INFRA-TPL-V32-01 無檔案交集，可並行；惟 commit 序列化以免 DIFF_REF 交錯）

## 檔案治理／來源說明

本票為 Akira 2026-07-31 裁決 **2-c** 之承接票（見 `2026-07-31_Ticket_Template_v32_Batch_Rulings` KD4）。

**盲區事實**（CSO 2026-07-31 17:07 live grep 取證）：`.gitignore` L74-77 現況為四行反選集——`plans/cso_briefing/*`（L74）→ `!plans/cso_briefing/project/`（L75）→ `plans/cso_briefing/project/*`（L76）→ `!plans/cso_briefing/project/TICKET_REGISTRY.json`（L77）。L76 萬用字元覆蓋下，`project/file/` 目錄被排除，其下**任何新增檔案靜默維持 untracked**（`git status` 不報、`git add` 不收，與「hook 置錯 repo 靜默 exit 0」同型失效）。`06a377f` 係對模板單檔之例外強制追蹤（`git add -f`），未消除目錄級盲區。

**修法機理**（依 L75+L77 既有先例同構）：gitignore 之 `dir/*` 萬用字元不跨 `/`，故 L76 僅匹配 `project/` 直接子項（含 `file` 目錄本身）；新增 `!plans/cso_briefing/project/file/` 反選該目錄後，其內容不再有任何排除規則匹配（`project/*` 不匹配 `project/file/xxx.md`），即恢復可追蹤。**單行即根治**，與 L75 反選 `project/` 之既有機理完全同構。

**Preset 說明**：PRESET=docs（純文本治理配置異動，無編譯產物語義；`assembleDebug` 依 TPL v3.2「固定 AC 之 preset 適用性」條款 N/A 劃記）。若 Akira 認 `.gitignore` 應歸 hotfix 型，得於派工前改劃，不影響 AI 內容。

**錨點預檢已於派工前執行**（KD2）：L74-77 四行現值取自 2026-07-31 17:07 live `grep_files`；`ANCHOR_PROBED` 如下。

## Verify Config
TICKET_ID=INFRA-GITIGNORE-WL-01
CREATED=2026-07-31
ANCHOR_PROBED=2026-07-31 17:07
PRESET=docs
ALLOWED=.gitignore
FORBIDDEN=plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=10
DIFF_REF=6a8f508

**DIFF_REF 回填證據**（qa/SKILL.md 三原子動作，Akira 2026-07-31 裁決選項 A）：原票面值 `06a377f` 為簽發時快照，非本票實際開工錨點；`/qa` Step 0 首跑時 `git rev-parse --short HEAD`＝`6a8f508`（前一張票 `INFRA-TPL-V32-01` 全部收尾完成後之 tip），已回填至上方 `DIFF_REF` 與 `tmp/verify_cmd.conf`，避免 `06a377f..6a8f508` 區間內前一票自身合法 commit（`d6a75f8`/`1c6f46c`/`55635e4`/`6a8f508`）被本票 FORBIDDEN 清單誤判。

**MAX_LINES 推導註記**（額度制截斷序律，強制欄）：AI-1 淨增 2 行（註解 1 + 白名單 1）→ 2× 緩衝 → 4 → 無條件進位至十位 → **10**。**截斷序**：若實作超出（不應發生），停止回報；禁增列票面未載之其他白名單路徑充版面。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.gitignore`
- **動作**: 逐字確認 AI-1 現值四行區塊於檔內命中**恰 1 次**（連續四行整體比對）。
- **命中數 ≠ 1 → 停止回報，禁自行推定等價位置。**
- [x] 完成

### AI-1: 插入 `project/file/` 白名單行
- **檔案**: `.gitignore`
- **現值**（唯一，L74-77 連續四行）:
```
plans/cso_briefing/*
!plans/cso_briefing/project/
plans/cso_briefing/project/*
!plans/cso_briefing/project/TICKET_REGISTRY.json
```
- **目標值**:
```
plans/cso_briefing/*
!plans/cso_briefing/project/
plans/cso_briefing/project/*
!plans/cso_briefing/project/TICKET_REGISTRY.json
# INFRA-GITIGNORE-WL-01: 治理文檔目錄白名單（消除同目錄新檔靜默 untracked 盲區）
!plans/cso_briefing/project/file/
```
- [x] 完成

## Hard Constraints
1. 僅得於 L77 之後**插入**兩行；禁改動 L74-77 既有四行之任何字元（含順序）——反選集次序即語義。
2. 禁擴大白名單範圍（如 `Pending/`、`archive/`、`plans/cso_briefing/` 全域）——該等目錄之排除屬既有設計，變更須另經裁決（禁語義擴張）。
3. 禁以 `git add -f` 替代本修法（單檔強制追蹤不消除目錄級盲區，即 `06a377f` 已證之侷限）。

## Error Handling
- AI-0 四行區塊命中數 ≠ 1 → 停止回報
- AC-3 行為性探測後 `git status` 殘留 probe 檔 → MUST 刪除 probe 後方可回報（禁留污染工作樹）
- AC-4 負向迴歸失敗（`Pending/` 路徑變為可追蹤）→ 停止回報並**回退 AI-1**（表示反選機理與預期不符，禁帶病提交）

## Acceptance Criteria
- [x] **[存在性]** `grep -c '^!plans/cso_briefing/project/file/$' .gitignore` = 1
- [x] **[存在性]** L74-77 原四行逐字無異動（`git diff` 對該檔僅 +2 行、0 行刪除/修改）
- [x] **[行為性]** 以真實 git 判定驅動：`touch plans/cso_briefing/project/file/.wl_probe.tmp` 後 `git status --short` MUST 列出該檔（`??` 前綴）；驗畢刪除 probe。禁以 `git check-ignore` 單獨替代（status 可見性方為使用者面真值）（**方法論修正**：原 probe 檔名 `.wl_probe.tmp` 誤中 `.gitignore:39` 既存不相關全域 `*.tmp` 規則，非本票白名單邏輯之偽陰性；改以 `wl_probe_check.md` 重測，`git status --short` 正確顯示 `??` 前綴，驗畢已刪除，`git status --short -- plans/cso_briefing/project/file/` 確認無殘留）
- [x] **[行為性]** 負向迴歸：`git check-ignore plans/cso_briefing/project/Pending/INFRA-GITIGNORE-WL-01_ProjectFile_Whitelist_Rootfix.md` MUST exit 0（仍被忽略）——`Pending/` 既有排除語義不受本票影響
- [x] ~~`assembleDebug` PASS~~ **(N/A: docs preset — `.gitignore` 異動無編譯產物語義)**
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## 收尾義務
- [x] commit 前綴 MUST 為 `[INFRA-GITIGNORE-WL-01]`（`.gitignore` 非 Governance Artifact 檔類，禁用 `GOVERNANCE-SYNC`/`COMPACT-SYNC`）— 已核驗：commit `f0c9c20` 前綴 `[INFRA-GITIGNORE-WL-01]`
- [ ] 本票落地後，`INFRA-TPL-V32-01` HC-1「禁新增檔案」之**依據前提消滅**（該 HC 依其簽發時法仍拘束該票自身執行，不追溯放寬）；後續新票不再需要同款 HC——此狀態變化由 CSO 於 ⑩ KAIROS 收割輪或下次相關開票時字面記載
