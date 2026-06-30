> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Ticket Pattern — 合法 Ticket ID 格式

**本檔為唯一真理來源 (SSOT)**。`CLAUDE.md` Rule 4a 與 `.git/hooks/commit-msg` 皆引用此規範。
修改 Ticket 格式 **MUST** 在本檔完成後，同步更新 hook 的 `TICKET_PATTERN` regex。

## 合法格式一覽

| 格式 | 說明 | 範例 |
|------|------|------|
| `D[N]-T[N]` | Sprint Day Ticket | `D9-T1` |
| `P0-[NAME]` | Phase 0 Ticket（NAME 全大寫英文） | `P0-FPS` |
| `P0[N]-[CODE]` | Phase 0.x Wave Ticket | `P08-E1`, `P08-F3` |
| `INFRA-[XX]` | 基礎設施 Ticket（英數混合，可多段連字號） | `INFRA-01`, `INFRA-52`, `INFRA-SSOT-TICKET` |
| `HOTFIX-[NN]` | 緊急修復 Ticket（純數字） | `HOTFIX-01`, `HOTFIX-47` |
| `V16-[XX]` | gstack v1.6 升級 Ticket | `V16-A1` |
| `KAIROS-D[N]` | KAIROS 階段回顧 Ticket | `KAIROS-D10` |
| `SPIKE-[CODE]` | 調查 Spike Ticket（英數） | `SPIKE-GF`, `SPIKE-AF` |
| `UA-[NN]` | Understand-Anything 知識圖譜 Ticket | `UA-01`, `UA-60` |
| `PHASE-[A]-[NN](-R[N])?` | 分析 Phase Ticket（可選 Revision 後綴） | `PHASE-B-01`, `PHASE-B-01-R3` |
| `STATE-CONTRACT-[NN][...]` | 狀態合約設計 Ticket（可附後綴） | `STATE-CONTRACT-01`, `STATE-CONTRACT-01-FIX-A` |
| `TRACTION-[TYPE]-[NN]` | Halo/Level Traction 規格 Ticket | `TRACTION-SSOT-01`, `TRACTION-GOV-01` |

## Regex（commit-msg hook 使用）

```
commit-hook=^\[(D[0-9]+-T[0-9]+|P0-[A-Z]+|P0[0-9]+-[A-Z][0-9]+|INFRA-[A-Z0-9-]+|HOTFIX-[0-9]+|V16-[A-Z][0-9]+|KAIROS-D[0-9]+|SPIKE-[A-Z][A-Z0-9]*|UA-[0-9]+|PHASE-[A-Z]-[0-9]+(-R[0-9]+)?|STATE-CONTRACT-[0-9]+[A-Z0-9-]*|TRACTION-[A-Z]+-[0-9]+)\]
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
