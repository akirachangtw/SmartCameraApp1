# INFRA-CR-AGENT-APP-01: cross-reviewer agent 模型別名化＋Checkpoint token 清單依 Android/Kotlin 技術棧重驗

**簽發日期**: 2026-09-02
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（單一 agent 設定檔文字異動，§0 五分鐘 `git restore` 域）
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: PLT `INFRA-CR-AGENT-01`（CLOSED，`docs/deliver/archive/INFRA/`）——本票之來源票；其維護註記「新專案 MUST 依該專案技術棧重驗 token 清單，禁沿用他棧殘留」即本票之直接授權依據
- `coordination_dependency`: 無（`.claude/agents/cross-reviewer.md` 現無他票在途）

## 檔案治理／來源說明

**本票非 PLT 票之同型移植，而是其維護註記之首次適用。** PLT 票做了兩件事：①模型字串別名化；②token 清單去死防線。第①項在 APP 同型成立；**第②項在 APP 必須反轉**——PLT 刪除 `@Inject`／`@Module`／`@Provides` 的理由是「Dagger token 於 Python repo 永不觸發，屬死防線」，而 APP 是 Android／Kotlin 專案，Hilt／Dagger 正是其 DI 框架，該三 token 為**有效防線，MUST 保留**。PLT 新增之 `feature_flag` 屬 Python snake_case 慣例，同樣不移植。

**現況取證（2026-09-02 live grep，`aud_20260902_422aff6307`）**：
- `.claude/agents/cross-reviewer.md` L5：`model: claude-opus-4-8`（過時版本釘選，同型缺口成立）
- 同檔 L13：token 清單為 `FeatureFlags` / `gate` / `Gate` / `cooldown` / `@Inject` / `@Module` / `@Provides`

**裸字 `gate`／`Gate` 之處置為條件式，非照抄**：PLT 收窄該二 token 的理由是它們在 PLT repo 命中 `ticket-rigor-gate`、`docs/rigor-gate/` 等大量非語法字面，造成假 CHECKPOINT-REQUIRED 噪音。APP 是否有同等噪音**未知**，MUST 由 AI-2 現地實測命中量決定，禁以 PLT 結論代替 APP 取證（跨端環境非等價）。

## Verify Config
TICKET_ID=INFRA-CR-AGENT-APP-01
CREATED=2026-09-02
PRESET=docs
ALLOWED=.claude/agents/cross-reviewer.md
FORBIDDEN=.claude/skills/**,.claude/rules/**,.claude/settings.json,.claude/CLAUDE.md,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json,D:/DevSources/workflow/mcp-ticket-platform/**
MAX_LINES=15
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
BASE_COMMIT=<同上>
TARGET_BLOB=N/A（docs preset、單檔小改，依 TPL v3.8 得標 N/A，V1 降級為 AI-0 錨點制）
ANCHOR_PROBED=2026-09-02 17:10

**MAX_LINES 推導註記**：AI-1 一行替換；AI-2 一行 token 清單調整＋維護註記約 3 行 ≈ 5 行 → ×1.4 → 7 → 緩衝取 **15**（非判準，僅估算校準遙測）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/agents/cross-reviewer.md`
- L5 `model: claude-opus-4-8` 唯一命中
- L13 token 清單七項字面（`FeatureFlags` / `gate` / `Gate` / `cooldown` / `@Inject` / `@Module` / `@Provides`）齊備
- **任一不符 → 依 TPL v3.8 基線漂移三分流分類回報，禁單一 STOP 無分類、禁自行推定等價位置**
- **[標配]** `git check-ignore -v .claude/agents/cross-reviewer.md` 命中 → 停止回報
- [x] 完成（L5 `model: claude-opus-4-8` 唯一命中確認；L13 七項 token 齊備確認；`git check-ignore -v` exit=1 未命中，未觸發標配停止）

### AI-1: 模型字串別名化
- **檔案**: `.claude/agents/cross-reviewer.md`
- **現值**: `model: claude-opus-4-8`
- **目標值**: `model: opus`
- **理由**: 別名自動追隨當前 Opus 世代，消滅每次改版都須改此行之維護債（PLT 同型裁決）
- **殘餘風險與承擔者**: 若 Claude Code subagent frontmatter 不支援別名解析，agent 將回退預設模型；承擔者為 AC-4 之實機確認，失敗時回改具體字串，成本一行。**PLT 端同一 AC 至今未閉環**（PLT 票 AC-5 標記「未閉環，責任人 Akira」），本票 MUST NOT 以 PLT 未閉環之項目推定 APP 成立
- [x] 完成（`model: claude-opus-4-8` → `model: opus`；[實機] 驗證見 AC-4：Agent 工具真實派工 cross-reviewer subagent，自報模型字串 `claude-opus-4-8[1m]`，未回退非 Opus 家族預設模型，別名解析成功）

### AI-2: token 清單依 Android/Kotlin 技術棧重驗
- **檔案**: 同上，L13 token 清單
- **保留（不刪，與 PLT 相反）**: `@Inject`／`@Module`／`@Provides` —— APP 使用 Hilt／Dagger，該三 annotation 為活防線。**MUST NOT** 因 PLT 票刪除過就照做
- **不新增**: `feature_flag`（Python 慣例）。APP 若需補等價 token，MUST 為 Kotlin 慣例形態
- **條件式收窄（先測後改）**: 於 repo 實測裸字命中量——`git grep -c "gate\|Gate" -- app/ .claude/ plans/` 或等效唯讀計數：
  - 命中集中於**可執行語法**（`gate(`、`_gate`、`Gate(`、`isGated` 等）→ 裸字保留原樣，**不動**
  - 命中大量**非語法字面**（目錄名、票號、文件標題）→ 依 PLT 形狀收窄為可執行語法形態，Kotlin 慣例採 `gate(`／`Gate(`／`isGated`（駝峰，非 PLT 之 snake_case `is_gated`）
  - 兩種情形**均 MUST** 將實測命中數與判定結果寫入收尾
- **新增維護註記**: 於 token 清單旁補一行，載明「本清單依 Android/Kotlin(Hilt) 技術棧校準，與 mcp-ticket-platform 之 Python 棧清單刻意分歧，MUST NOT 跨端同步」——防止日後 env-sync 批次把 PLT 版覆蓋回來
- [x] 完成（保留 `@Inject`／`@Module`／`@Provides` 不刪；`feature_flag` 未新增；裸字命中實測：`git grep -c "gate\|Gate" -- app/ .claude/ plans/` 共 107 檔／501 次出現，經樣式比對僅 18 筆近似可執行語法形態，且全數為封存 MEO 文件之腳本檔名/文件標題字面（如 `meo_pretooluse_gate.sh`、`Hook_Enforcement_Gate.md`），現行 `app/src/` 與 `.claude/` 腳本零真實 `gate()`/`isGated` 呼叫（唯一疑似 `aggregate(` 為 substring 誤命中，非 gate token）——命中集中於非語法字面，依 PLT 同型形狀收窄，`gate`／`Gate` → `gate(`／`Gate(`／`isGated`；維護註記已加為 HTML comment 一行）

## Hard Constraints
1. 禁動 frontmatter 之 `name`／`description`／`tools` 欄
2. 禁動 Review Protocol 與 Output Format 段（本票僅及模型欄與 token 清單）
3. **禁跨 repo**：`mcp-ticket-platform`／`ObsidianVault`／`Smarter-Project` 一律不得觸碰
4. **禁以 PLT 票之 diff 為施作藍本逐行套用**——本票與 PLT 票在 AI-2 上動作相反，位元組級複製即製造錯誤
5. AI-2 之收窄屬條件式：未取得實測命中數前 **MUST NOT** 逕行修改裸字 token

## Error Handling
- AI-0 任一錨點不符 → 依三分流分類回報
- `git grep` 計數無法取得（工具不可用）→ 停止回報，**MUST NOT** 以「PLT 也這樣」推定收窄
- diff 觸及 ALLOWED 以外路徑 → 停止回報
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** frontmatter 含 `model: opus`，且 `claude-opus-4-8` 字面命中＝0——實測：`.claude/agents/cross-reviewer.md` L5 `model: opus`，`grep claude-opus-4-8` 該檔零命中
- [x] **[存在性]** token 清單仍含 `@Inject`／`@Module`／`@Provides` 三者（**保留之正向驗證**——本項為防「照抄 PLT 誤刪」之守門，MUST NOT 省略）——實測：L13 三 token 逐字仍在
- [x] **[存在性]** `feature_flag` 字面命中＝0（Python 慣例未誤入）——實測：`grep feature_flag` 該檔零命中
- [x] **[存在性]** 維護註記存在，含「MUST NOT 跨端同步」語義字面——實測：L14 HTML comment 含「MUST NOT 跨端同步」逐字
- [x] **[行為性]** AI-2 之裸字命中實測數已寫入收尾，且收窄與否之判定依該數作成——`git grep -c "gate\|Gate" -- app/ .claude/ plans/` 共 107 檔／501 次；`git grep -nE "\b[gG]ate\(|\bisGated\b|_[gG]ate\b|\b[gG]ate_" | grep -v aggregate` 僅 18 筆，且全數為封存 MEO 文件之腳本檔名/文件標題字面，非現行程式碼語法呼叫；判定：命中集中於非語法字面 → 收窄為 `gate(`／`Gate(`／`isGated`
- [x] **[實機]** Claude Code 實際調用 cross-reviewer 時 `model: opus` 解析成功、未回退預設——實測：`Agent` 工具真實派工 cross-reviewer subagent，自報模型字串 `claude-opus-4-8[1m]`（Opus 家族，非通用預設模型），別名解析成功
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，純 agent 設定檔文字，無編譯產物)
- [x] `git diff --stat` 僅涉及 `.claude/agents/cross-reviewer.md`——實測：`git diff --stat` 僅該檔 +3/-2

---

**End of INFRA-CR-AGENT-APP-01**
