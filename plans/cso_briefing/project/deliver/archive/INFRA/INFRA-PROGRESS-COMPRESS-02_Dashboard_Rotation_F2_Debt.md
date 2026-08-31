# INFRA-PROGRESS-COMPRESS-02: PROGRESS.md 86→≤75 行精簡（✅ Done 列歸檔輪替，Check F.2 債務清償）

**簽發日期**: 2026-07-31
**簽發者**: Claude (SMTR / CSO 主 session)
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: 無（INFRA-GITIGNORE-WL-01 已 CLOSED；本票即其 F.2 債務承接票）

## 檔案治理／來源說明

本票為 Akira 2026-07-31 F.2 裁決（選項 1）之承接票。**債務事實與歸因**（記錄先於清償）：`structural_verify.sh` Check F.2 判 `.claude/PROGRESS.md` 86 行 > 80 行上限（`ssot-governance.md` L27：MUST ≤80，目標 ≤50）。上限突破**非 `INFRA-GITIGNORE-WL-01` 新造**——其開工錨 `6a8f508` 時已 85 行，該票 Dashboard 列邊際 +1 至 86。

**法源**：`ssot-governance.md` L38——大規模精簡/歸檔 **MUST** 走專用 docsync 票（明列 ALLOWED 授權），**MUST NOT** 在無關票內就地輪替（前例 `MEO-T3-DOCSYNC-01`）。本票即該專用票。L60「FORBIDDEN 延後項免開票紀律」不適用本票——該紀律射程為四項例行收尾動作（Dashboard 同步／Done 標記／票檔搬移／checkbox），行數上限債務之歸檔輪替屬 L38 射程，非 L60 射程。

**錨點預檢已於派工前執行**（KD2）：PROGRESS.md 於 2026-07-31 19:54 live `read_file_safe` 直讀（total_chars=20701），表結構為「最近 Ticket | 狀態」二欄表、一列一票、✅/🟡 狀態前綴；`PROGRESS_ARCHIVE.md` 存在（`ssot-governance.md` L5 GA 檔類明列）。本票不設逐字串錨（列選取為判準式，非定點替換），AI-0 改為量化復驗。

## Verify Config
TICKET_ID=INFRA-PROGRESS-COMPRESS-02
CREATED=2026-07-31
ANCHOR_PROBED=2026-07-31 19:54
PRESET=docs
ALLOWED=.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md
FORBIDDEN=plans/cso_briefing/project/TICKET_REGISTRY.json,.claude/PROGRESS_ARCHIVE_Sprint*.md
MAX_LINES=40
DIFF_REF=f0c9c20

**MAX_LINES 推導註記**（額度制截斷序律，強制欄）：預估歸檔 8-12 列（PROGRESS 側刪除不計 added；ARCHIVE 側逐字追加 8-12 行 + 歸檔批次標頭 2 行 + PROGRESS 側無需新增行）→ 上緣 14 → 2× 緩衝 28 → 進位 → **40**。**截斷序**：若歸檔列數不足以達標 ≤80（不應發生，見 AI-1 判準），停止回報，禁刪節列內容充行數（逐字搬移律）。

## Action Items

### AI-0: 量化復驗（派工後、修改前）
- **動作**: `wc -l .claude/PROGRESS.md` 取現值行數 N。N 預期 = 86；若 86 < N ≤ 92（並行 session 邊際增量），續行並於回報記載實測 N;若 N > 92 或 N ≤ 80 → 停止回報（前者示情況已惡化逾預估、後者示債務已被他票清償，均需重裁）。
- 確認 `.claude/PROGRESS_ARCHIVE.md` 存在且可寫。
- [x] 完成 — 實測 N=86（`wc -l`），與預期精確相符；`PROGRESS_ARCHIVE.md` `test -w` 確認可寫

### AI-1: ✅ Done 列歸檔輪替（判準式，非定點）
- **選取判準（機械，禁語義裁量）**：
  1. **MUST 保留**：狀態欄非 ✅ 開頭之所有列（如 🟡 MEO-FIX-A Rev J）+ 表中**位置最末之 2 列 ✅**（最新近完成，保留近況可讀性）。
  2. **歸檔順序**：自表**頂部**（最舊）之 ✅ 列起依序搬移。
  3. **停止條件**：`wc -l` 達 **≤75**（80 上限 - 5 行安全餘裕）即停；MUST NOT 為逼近目標 ≤50 而過度歸檔（該目標之達成另議，禁本票語義擴張）。
- **搬移紀律**：被歸檔列 MUST **逐字**追加至 `PROGRESS_ARCHIVE.md` 尾部（L31：ARCHIVE 保持追加順序），批次前置兩行標頭：
```
## 2026-07-31 Dashboard 輪替（INFRA-PROGRESS-COMPRESS-02）
| 最近 Ticket | 狀態 |
```
- PROGRESS.md 側僅刪列，禁改留存列之任何字元。
- [x] 完成 — 搬遷 11 筆最舊 ✅ 列（`INFRA-GOV-DEESC-01`～`GAP-10-BREATH-RESTART`）至 `PROGRESS_ARCHIVE.md` 尾部批次區塊；`.claude/PROGRESS.md` 86→75 行（一次到位，命中 ≤75 停止條件，無過度歸檔）；`MEO-FIX-A Rev J`（唯一 🟡）+ 表末 2 列 ✅（`INFRA-TPL-V32-01`/`INFRA-GITIGNORE-WL-01`）全數留存未動

## Hard Constraints
1. **逐字搬移律**：歸檔列位元組級不變；禁摘要、禁改寫、禁合併列。
2. 禁動 PROGRESS.md 表頭、Sprint 資訊行、`**Last Updated**` 以外之留存內容;`**Last Updated**` 得更新為本票日期。
3. 禁觸碰 `PROGRESS_ARCHIVE_Sprint*.md` 歷史歸檔（FORBIDDEN）；Sprint 級重命名輪替（L42 SHOULD 條款）不在本票範圍，禁順手執行。
4. commit 前綴 MUST 為 `[COMPACT-SYNC]`（兩檔均屬 Governance Artifact 檔類，依 `ssot-governance.md` L19-20 檔類專屬前綴；**禁用票號前綴**），commit body MUST 引註 `INFRA-PROGRESS-COMPRESS-02`。

## Error Handling
- AI-0 之 N 越界（>92 或 ≤80）→ 停止回報
- 歸檔後 `wc -l` 仍 >80 且無 ✅ 列可再搬（保留判準 1 耗盡）→ 停止回報（示非 Done 列過多，屬結構問題需另裁）
- ARCHIVE 追加後 read-back 任一搬移列 grep 未命中 → 停止回報並回退（禁在來源已刪、目的未證下提交）

## Acceptance Criteria
- [x] **[存在性]** `wc -l .claude/PROGRESS.md` ≤ 80（MUST）且 ≥ 70（過度歸檔防護） — 實測 75
- [x] **[存在性]** 列數守恆：搬移列數 K 滿足「PROGRESS 減少列數 = K = ARCHIVE 新增列數（不含 2 行批次標頭）」，零資訊滅失 — `git diff --stat`：PROGRESS.md -11L，PROGRESS_ARCHIVE.md +14L（1 空行 + 2 行批次標頭 + 11 行資料列），K=11 兩側相符
- [x] **[存在性]** 抽樣 read-back：任取 3 個被歸檔票號 ID，於 `PROGRESS_ARCHIVE.md` grep 逐字命中 — `INFRA-GOV-DEESC-01`(1)／`INFRA-AGENTS-CR-04`(2)／`GAP-10-BREATH-RESTART`(2) 均 ≥1 命中
- [x] **[行為性]** `structural_verify.sh --post` **Check F.2 PASS**（真實腳本驅動，禁以 wc 單獨替代——腳本判準方為閘門真值） — 真實腳本輸出 `⚠️ WARNING [F.2]: PROGRESS.md 75 行 > 60（接近上限）`，75≤80 上限未觸發 FAIL／REPORT_FIX，屬 ADVISORY 級通過；同輪整體 Verdict=BLOCKER 純由 Check E/G（Cross-Review receipt 不存在）觸發——本票無 Cross-Review 要求（票面未列此 Action Item），屬預期缺席非缺陷，與 F.2 無關
- [x] **[存在性]** 留存列含全部非 ✅ 列（`grep -c '| 🟡' PROGRESS.md` 前後不變） — 前後均 3（Ticket 表 1 + Active Blockers 表 2，本票未觸碰任一 🟡 列）
- [x] ~~`assembleDebug` PASS~~ **(N/A: docs preset — 純 Dashboard 治理檔輪替)**
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — `.claude/PROGRESS.md` + `.claude/PROGRESS_ARCHIVE.md` 兩檔，`TICKET_REGISTRY.json` 為並行外部異動之既存 dirty（session 開頭已如此），未 stage／未 commit

## 收尾義務
- [x] commit `[COMPACT-SYNC]`（HC-4）後，於回報中記載 F.2 債務清償終態（含 WL-01 邊際 +1 歸因鏈結） — commit `ccb678c`；終態見主對話回報
- [x] `INFRA-GITIGNORE-WL-01` 冊面 note 已預載本票 ID 為債務承接載體，無需回寫
