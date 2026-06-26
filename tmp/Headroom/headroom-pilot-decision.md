# Headroom / CodeGraph Pilot Repo Decision

本文件定義目前 repo 內對 Headroom sidecar、VS Code dashboard、pilot config 存放結構與 multi-agent governance 邊界的正式決策。其目的不是描述所有未來能力，而是固定目前 pilot 階段的正式落點與責任分界，避免設定檔、runtime adapter 與 governance 層互相污染。

## 決策摘要

本 repo 採用的正式結構是：`AGENTS.md` 保持 repo-level startup authority；`.agents/skills` 保持 cross-agent common source；`.claude/skills` 與 `.codex/skills` 保持 active runtime adapter entrypoints；pilot 專用設定則獨立落在 `.agents/pilot/`。這與既有整併決策一致，因為 `.agents/skills` 不能取代 `AGENTS.md`，而 runtime adapters 也不應在 pilot 階段被直接搬移。

在 sidecar 與可視化部分，Headroom proxy 已視為本地 sidecar，VS Code dashboard 以內建 Webview API 的自製 extension 實作，並輪詢 `/health` 與 `/stats`。VS Code 官方文件將 webview 定位為 extension 可建立的自訂 UI 容器，而 Headroom quickstart 明確提供 `headroom proxy` 與 `/health` 端點作為起步方式。

## 正式目錄結構

目前建議的正式 repo 結構如下：

```text
AGENTS.md
.agents/
  pilot/
    pilot.config.json
    README.md
  skills/
    MANIFEST.md
    scripts/
      scan-common-skills.sh
.claude/
  rules/
    cross-review.md
  skills/
.codex/
  skills/
```

此結構的設計理由如下：

- `AGENTS.md` 仍是 deployment prerequisite 與 startup authority，不與 skill 層混淆。
- `.agents/skills` 僅為 common source layer，不承擔 pilot config 角色。
- `.claude/skills`、`.codex/skills` 仍作為 runtime adapters，因此 pilot 設定不應放進任一 adapter 目錄。
- `.agents/pilot/` 作為 pilot wiring、dashboard wiring、telemetry wiring 的專屬位置，語義最清楚。

## pilot.config.json 決策

`pilot.config.json` 的正式落點為：

```text
.agents/pilot/pilot.config.json
```

此檔只承載 pilot integration wiring，不承載所有應用程式設定。其角色是把 prompt contract、common skill source、runtime adapter roots、Headroom sidecar 端點、dashboard view id 與 telemetry 欄位固定下來，讓 sidecar、dashboard、guardrail 與後續觀測使用同一份 SSOT。

### 初稿內容

```json
{
  "version": 1,
  "prompt_contract_path": "AGENTS.md",
  "skill_source_root": ".agents/skills",
  "runtime_adapters": {
    "claude": ".claude/skills",
    "codex": ".codex/skills"
  },
  "paths": {
    "pilot_root": ".agents/pilot",
    "manifest_path": ".agents/skills/MANIFEST.md",
    "common_scan_script": ".agents/skills/scripts/scan-common-skills.sh",
    "cross_review_rule": ".claude/rules/cross-review.md"
  },
  "headroom": {
    "enabled": true,
    "proxy_url": "http://127.0.0.1:8787",
    "health_path": "/health",
    "stats_path": "/stats",
    "dashboard": {
      "extension_id": "local.vscode-headroom-dashboard",
      "activitybar_container_id": "headroomDashboard",
      "sidebar_view_id": "headroomDashboard.view",
      "poll_interval_ms": 5000,
      "request_timeout_ms": 3000,
      "show_raw_payload": true
    }
  },
  "codegraph": {
    "enabled": false,
    "health_url": "http://127.0.0.1:8788/health",
    "stats_url": "http://127.0.0.1:8788/stats",
    "dashboard_card_id": "codegraphHealth",
    "future_integration": true
  },
  "pilot_flags": {
    "enable_headroom_dashboard": true,
    "enable_headroom_stats_polling": true,
    "enable_codegraph_health": false,
    "enable_cross_review_guard": true,
    "enable_common_skill_scan": true
  },
  "telemetry": {
    "emit_prompt_contract_path": true,
    "emit_skill_source_root": true,
    "emit_runtime_adapter_root": true,
    "emit_dashboard_view_id": true,
    "emit_sidecar_endpoint": true,
    "fields": [
      "prompt_contract_path",
      "skill_source_root",
      "runtime_adapter_root",
      "skill_name",
      "skill_classification",
      "adapter_runtime",
      "dashboard_view_id",
      "sidecar_service",
      "sidecar_endpoint"
    ]
  }
}
```

上述欄位設計對齊了 pilot review 中明確指出的 observability needs：至少應能區分 `prompt_contract_path`、`skill_source_root` 與 `runtime_adapter_root`，以避免 dashboard 在多 agent 共用 common skill 時出現歧義。

## Headroom sidecar 可視化決策

Headroom sidecar 的本地健康資訊以 VS Code sidebar dashboard 呈現，採用官方 Webview API 由自製 extension 輪詢本機 sidecar 端點，而不是透過 Marketplace 發布流程先行綁定。因為目前需求只要求本地 `.vsix` 安裝與 pilot 驗證，並未要求對外發布。

### 可視化顯示欄位

第一版 dashboard 應顯示下列欄位：

| 欄位 | 來源 | 說明 |
|---|---|---|
| `health.status` | `GET /health` | 基本健康判定。  |
| `healthHttpStatus` | `GET /health` | HTTP 成功/失敗狀態。  |
| `statsHttpStatus` | `GET /stats` | 統計端點可達性。  |
| `checkedAt` | extension host | 最後一次輪詢時間。 |
| `endpointBase` | pilot config | 顯示目前 sidecar base URL。 |
| `stats payload` | `GET /stats` | 供 pilot 觀察 raw payload。  |
| `error` | extension host | sidecar 不可達或解析異常。 |

### Sidecar 啟動方式

Headroom sidecar 以 bash 啟動腳本作為預設方式，對齊目前 Windows 11 + Git Bash 的工作環境。腳本責任僅是啟動 `headroom proxy`，extension 則只負責輪詢與顯示，不在第一版把進程生命週期控制綁進 VS Code extension。這符合 pilot 階段低耦合、易回退的原則。

## CodeGraph 預留欄位

CodeGraph indexing 已完成，但目前 dashboard 階段只正式啟用 Headroom health/stats。`pilot.config.json` 先保留 `codegraph.health_url`、`codegraph.stats_url` 與 `dashboard_card_id` 欄位，將 `enabled` 設為 `false`，作為下一階段 dashboard 擴充點，而不在本輪要求實作 CodeGraph 健康卡片。這符合先做最小閉環、再逐步擴充的 pilot 原則。

## Guardrail 與治理關聯

Pilot wiring 必須與現有治理規則保持一致，因此 `pilot.config.json` 會記錄 `MANIFEST.md`、`scan-common-skills.sh` 與 `.claude/rules/cross-review.md` 的正式路徑。這並不是要把這些檔案整併在一起，而是為了讓 dashboard、掃描與後續 telemetry 可以對齊同一份 repo 路徑 SSOT。

此外，若 `.agents/skills/**` 已作為 common source layer 啟用，Cross-Review guard 就必須覆蓋它；這是既有審查結論中的 MAJOR requirement，而 `pilot.config.json` 只負責記錄該 guard 所在位置，不改變其權責分界。

## Repo 內正式採納建議

目前最適合採納的正式 repo 內決策如下：

1. 正式建立 `.agents/pilot/pilot.config.json` 作為 pilot wiring SSOT。
2. 保持 `.agents/skills/MANIFEST.md`、`.agents/skills/scripts/scan-common-skills.sh` 與 `.claude/rules/cross-review.md` 為治理層配套。
3. 保持 Headroom dashboard 以本地 `.vsix` extension 使用，不要求 Marketplace 發布。
4. 先只將 Headroom `/health` 與 `/stats` 納入 dashboard，CodeGraph 只保留預留欄位。
5. 不把 pilot config 混入 `.claude/skills`、`.codex/skills` 或 extension source tree。
