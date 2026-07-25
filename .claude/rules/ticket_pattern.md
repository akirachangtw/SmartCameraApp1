> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Ticket Pattern — 合法 Ticket ID 格式

**本檔為唯一真理來源 (SSOT)**。`CLAUDE.md` Rule 4a 與 `.git/hooks/commit-msg` 皆引用此規範。
**變更結構文法本身**時 **MUST** 在本檔「## Regex」區塊的 `commit-hook=` 行完成修改（本檔為 SSOT，`.git/hooks/commit-msg` 鏡像本行）；**新增 family 不改 regex**，僅於「## 合法格式一覽」表補登 provenance。

## 合法格式一覽

> 本表為 family provenance 敘述（記錄各 family 由哪張票引入），非 hook 強制來源；enforcement 唯一來源為下方「## Regex」單行結構文法。新增 family 無需改 regex，僅在 CSO 稽核時補登本表。

| 格式 | 說明 | 範例 |
|------|------|------|
| `D[N]-T[N]` | Sprint Day Ticket | `D9-T1` |
| `P0-[NAME]` | Phase 0 Ticket（NAME 全大寫英文） | `P0-FPS` |
| `P0[N]-[CODE]` | Phase 0.x Wave Ticket | `P08-E1`, `P08-F3` |
| `INFRA-[XX]` | 基礎設施 Ticket（英數混合，可多段連字號） | `INFRA-01`, `INFRA-52`, `INFRA-SSOT-TICKET` |
| `HOTFIX-[NN]` | 緊急修復 Ticket（純數字） | `HOTFIX-01`, `HOTFIX-47` |
| `V16-[XX]` | gstack v1.6 升級 Ticket | `V16-A1` |
| `KAIROS-D[N]` | KAIROS 階段回顧 Ticket | `KAIROS-D10` |
| `KAIROS-BACKFILL-[NN][...]` | KAIROS 節點外規則收割/補寫 Ticket（非 D[N] 回顧本體，可附後綴） | `KAIROS-BACKFILL-01`, `KAIROS-BACKFILL-01-FIX-A` |
| `SPIKE-[CODE]` | 調查 Spike Ticket（英數） | `SPIKE-GF`, `SPIKE-AF` |
| `UA-[NN]` | Understand-Anything 知識圖譜 Ticket | `UA-01`, `UA-60` |
| `PHASE-[A]-[NN](-R[N])?` | 分析 Phase Ticket（可選 Revision 後綴） | `PHASE-B-01`, `PHASE-B-01-R3` |
| `STATE-CONTRACT-[NN][...]` | 狀態合約設計 Ticket（可附後綴） | `STATE-CONTRACT-01`, `STATE-CONTRACT-01-FIX-A` |
| `TRACTION-[TYPE]-[NN]` | Halo/Level Traction 規格 Ticket | `TRACTION-SSOT-01`, `TRACTION-GOV-01` |
| `GAP-[NN][...]` | GAP 缺口修復 Ticket（渲染/回授斷裂類，可附後綴） | `GAP-06`, `GAP-07`, `GAP-06-RENDER-DOT-01` |
| `SSOT-V407-[...]` | SSOT v4.0.7 版本升級治理 Ticket（可含主修訂、SWEEP、closure 後綴） | `SSOT-V407-01`, `SSOT-V407-SWEEP-01` |

## Regex（commit-msg hook 使用）

```
commit-hook=^\[[A-Z][A-Z0-9]*(-[A-Z0-9]+)+\]
```

## 變更紀錄

| Ticket | 新增格式 | 說明 |
|--------|---------|------|
| INFRA-28 | `SPIKE-[CODE]` | 調查 Spike 類型 |
| INFRA-52 | `TRACTION-[TYPE]-[NN]` | Halo/Level Traction 規格重寫 |
| INFRA-60 | `UA-[NN]` | Understand-Anything 知識圖譜 |
| INFRA-SSOT-TICKET | `INFRA-[A-Z0-9-]+`（擴充連字號） | INFRA 支援多段式語意化 ID（如 `INFRA-SSOT-TICKET`）|
| 授權補登 | `PHASE-[A]-[NN](-R[N])?` | 分析 Phase，對齊 CSO 裁決單 |
| 授權補登 | `STATE-CONTRACT-[NN][...]` | 狀態合約設計 |
| KAIROS-BACKFILL-01-FIX-A | `KAIROS-BACKFILL-[NN][...]` | KAIROS 節點外規則收割/補寫（非 D[N] 回顧本體）；收斂原 KAIROS-BACKFILL-01 引入的通用 `KAIROS-[CODE]-[NN]` 為專用格式，CSO 裁決治理票授權 |
| INFRA-TICKETID-01 | `^\[[A-Z][A-Z0-9]*(-[A-Z0-9]+)+\]`（結構文法） | family 列舉遷移為單一結構文法；enforcement 收斂、family 表降 provenance；併記 FIX-A 之 ticket_pattern.md 編輯屬 DRA 越界，已由 CSO 立規追認並以本票消除越界誘因 |
| INFRA-M3-AUDIT-01 | （無新格式） | 建立 family provenance 偵測控制 `.claude/scripts/m3_family_audit.sh`（唯讀 + escalate，對帳本表 derive 的 family 白名單）；消除第 6 行死指令（結構文法變更 vs 新增 family 分流）；掛 `/dream` stale-gate |
| GAP-06-RENDER-DOT-01 | `GAP-[NN][...]` | GAP 缺口修復 family 補登 provenance（結構文法既已涵蓋，不改 regex）；CSO（Akira）2026-07-08 授權補登，解 Rule 4a「family 未登記」escalate |
| INFRA-TICKETID-SSOT-V407-01 | `SSOT-V407-[...]` | SSOT v4.0.7 版本升級 family 補登（`SSOT-V407-01` + `SSOT-V407-SWEEP-01` 收斂為單一 family，非兩個單票特例）；CSO（Akira）2026-07-08 裁決授權，解 M3 Family Provenance 稽核（`M3_Family_Audit_20260708.md`）2 筆 escalate |

> family provenance STOP／自動補登分流規則之 SSOT＝`CLAUDE.md` Rule 4a（two-branch，`INFRA-GOV-DEESC-01` Rev A）；本檔僅承載補登資料表，不複寫規則本文。

## Family Provenance 補登紀錄

| family | 首票 TICKET_ID | 代表票 | 來源路徑 | 補登日期 | 追認依據 |
|--------|----------------|--------|----------|----------|----------|
| `MEO-` | `MEO-T2` | `MEO-FIX-A` | `plans/cso_briefing/project/Pending/MEO-FIX-A_Escalation_Gate_Relocation.md` | 2026-07-25 | CSO 簽發並直接指示執行 |

## 票據模板規範

> 新建 SSOT 落點（`INFRA-GOV-DEESC-01` Rev A AI-2a）；非修改既有段落。

票據 `## Verify Config` 區塊之 `DIFF_REF` **MUST** 為字面佔位 `<開工時 git rev-parse --short HEAD 回填>`；起草時 **MUST NOT** 硬編具體雜湊。回填時機與三原子動作見 `.claude/skills/qa/SKILL.md` Step 0（Scope Gate）。
