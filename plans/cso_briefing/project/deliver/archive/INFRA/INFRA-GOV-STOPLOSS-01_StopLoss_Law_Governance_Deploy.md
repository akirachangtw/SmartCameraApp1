# INFRA-GOV-STOPLOSS-01: 結構歧義停損律寫入執行者面治理載體(ssot-governance.md)

**簽發日期**: 2026-08-01
**簽發者**: Claude(CSO)
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A(純 .md 治理票)
**前置依賴**: 無(SSOT 已落 vault `2026-08-01_Structural_Ambiguity_Stop_Loss_Law.md`,本票僅部署執行者面摘錄)

**檔案治理/來源說明**: 本票依 CSO 確認決議(2026-08-01)部署結構歧義停損律至執行者常駐規則檔;律之完整推理鏈與翻盤條件以 vault 決策檔為 SSOT,本票寫入者為機械摘錄,兩者歧異時 vault 優先。

## Verify Config
TICKET_ID=INFRA-GOV-STOPLOSS-01
CREATED=2026-08-01
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=plans/cso_briefing/project/TICKET_REGISTRY.json,.claude/PROGRESS.md,STATE.md
MAX_LINES=20
DIFF_REF=d5fe887
ANCHOR_PROBED=2026-08-01 15:10

**MAX_LINES 推導註記**(額度制截斷序律,強制欄):AI-1 單節新增 11 行(標題 1+觸發器 4+範圍/解除 2+SSOT 引註 1+空行緩衝 3)→ ×1.5 緩衝 → 16.5 → 進位 **20**。**截斷序**:實作超額時,先刪空行緩衝,次刪觸發來源引註行;四觸發器+凍結範圍+解除條件行**禁刪**;仍超額 → 停止回報。

## Action Items

### AI-0: 錨點復驗(派工後、修改前)
- **檔案**: `.claude/rules/ssot-governance.md`
- **動作**: 逐字確認「現值」錨點於檔內命中恰 1 次:檔案末段結尾字串 `避免治理疊床架屋)。`(「FORBIDDEN 延後項免開票紀律」bullet 之收尾,後接檔案結尾換行);並確認 `git log -1` HEAD 仍為 `d5fe887`——若 HEAD 已越過 `ANCHOR_PROBED`(2026-08-01 15:10),停止回報請 CSO 重跑預檢。
- **任一項不符 → 停止回報,禁自行推定等價位置。**
- [x] 完成(CSO 收束補勾 2026-08-01:執行者回報錨點恰 1 次+HEAD 位移經解凍派工令明示授權)

### AI-1: 檔尾追加「結構歧義停損律」節
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 檔案以 `避免治理疊床架屋)。` + 換行結尾(AI-0 已復驗)
- **目標值**: 於檔尾追加下列文塊(逐字):

```
- **結構歧義停損律**(2026-08-01 CSO 確認決議):同一子系統下列任一**字面**成立,MUST 停止開立/受理新修補票並升報 CSO:①同一票修訂版次 ≥ 3(票面 Rev 直取);②結構性死結/自指阻塞 ≥ 2 種(決策檔/Handoff 記載筆數直取);③任一 workaround 額度制用量達上限 50%;④修補深度 ≥ 2(FIX 票之 FIX 票)。凍結僅及修補線,不及執行中之非修補工作;解除條件=外審回流+CSO 存廢三案(退役/降級/續修)裁決落檔。禁語義裁量(「此票很重要」不豁免)。SSOT:vault `2026-08-01_Structural_Ambiguity_Stop_Loss_Law.md`;觸發來源:MEO v1 十輪修補無停損(`MEO_V1_Enforcement_Layer_Decommission`)。
```
- [x] 完成(CSO 收束補勾 2026-08-01:commit f4a9c8e,git_show 獨立驗訖純追加 +1/-0、逐字比中)

## Hard Constraints
1. 純追加,既有內文零改動(含空白字元)。
2. 追加文塊逐字採票面目標值,禁改寫、禁摘要、禁補充。
3. 本票不涉及 MEO 字面路徑,凍結令無涉;commit 前綴 `[INFRA-GOV-STOPLOSS-01]`,禁夾帶他檔。

## Error Handling
- AI-0 錨點不符或 HEAD 越過 ANCHOR_PROBED → 停止回報
- 追加後 read-back 未逐字命中 → 停止回報,禁重試覆寫
- git add 前 `git check-ignore -v` 顯示標的被忽略 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** `grep -c "結構歧義停損律" .claude/rules/ssot-governance.md` = 1,且四觸發器①②③④字面逐一命中
- [x] **[存在性]** 追加文塊含 SSOT vault 引註與觸發來源引註各恰 1 次
- [x] **[行為性]** 追加為純尾部 append:`git diff` 顯示僅新增行、零刪除行、零既有行修改
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

> 註:本律之「觸發後行為」屬 CSO/開票方裁決域,本票 AC 不含停損行為之運行時測試([行為性] AC 以 diff 純追加性承載);首次真實觸發案例回填至 vault SSOT 檔(K5-Rule 29 慣例)。
