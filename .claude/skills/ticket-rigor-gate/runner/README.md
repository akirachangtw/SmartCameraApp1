# Codex Ticket Repair Runner

## 契約 SSOT

此 runner 依 `Skill-Advisor/deliver/ticket-rigor-gate-skill/ticket-quality-skill-codex-runner-contract.md`
（跨 repo，非本 repo 內檔案）落地實作。本 README 不重述契約全文，僅記載本次落地之版本綁定與偏差。

- Payload schema 版本：`ticket-repair-payload/v1`（`schemas/ticket-repair-payload.v1.json`）
- Response schema 版本：`ticket-repair-response/v1`（`schemas/ticket-repair-response.v1.json`）
- Prompt 模板版本：`ticket-repair-prompt/v1`（`prompts/ticket-repair-prompt.v1.txt`）
- Model profiles 版本：`model-profiles/v1`（`profiles/model-profiles.v1.json`）

## §7.2 Profile 化偏差（KD-6）

契約 §7.2 字面為固定值比對（`run_summary.model` 必須是 `gpt-5.6-sol`）。本落地依裁決
`RigorGate_R2_Terra_Routing_Profile_Switch_And_A5_Family`（2026-08-20）改為比對**本次 run 宣告之
profile** 於 `profiles/model-profiles.v1.json` 所載值；比對值與命令組裝之 `-m`／`-c` 共用同一常數
來源（`repair_runner.sh` 之 `MODEL`／`EFFORT` 變數，皆自 profiles 檔解析，無兩處各自硬編）。

## 執行 Shell

`repair_runner.sh` 為 bash／POSIX 腳本（契約 §3 C-7）。Windows 下以 git-bash 執行；腳本內固定呼叫
`codex.cmd`（禁裸 `codex`，AI-0 動作 B 現地取證要求）。

## 引數規格

```text
bash repair_runner.sh [sol|terra] --payload <payload.json>
```

- 第一參數（可省略，缺省＝`terra`）僅接受字面 `sol` 或 `terra`；任何其他值一律拒收非零退出（exit 2），
  且拒收發生於任何 payload 讀取／Codex 呼叫之前——Codex 呼叫次數恆為 0。
- Profile 切換僅經此引數；**禁**環境變數 override，**禁**第三 profile，**禁** `auto` 選擇
  （Hard Constraint 5a）。
- `--payload <path>` 為必要旗標，指向 `ticket-repair-payload/v1` JSON 檔。

| Profile | Model | Reasoning Effort |
|---|---|---|
| `sol` | `gpt-5.6-sol` | `high` |
| `terra`（預設） | `gpt-5.6-terra` | `medium` |

## 驗證層與斷路器

- §7.1–§7.8 八層驗證（schema／model contract／eligibility／evidence／target hash／diff budget／
  frozen sentence／decision leakage）均由主執行路徑依序呼叫；任一層未過即禁寫入，該 finding 整筆回退。
- §14 斷路器 CB-1～CB-4 於每次 `codex exec` 呼叫前評估；觸發後凍結該 `ticket_id`，quality report 記
  `Apply Status: escalated` 與對應 CB 編號，禁自行重試。

## MVP 邊界（最小實作聲明）

- 僅支援 `target.write_mode = "addendum_only"`；其餘 write_mode 值一律回 `RTR-009`（fail-closed，非猜測
  bounded-section 語意）。
- §7.8 決策洩漏驗證為機械式啟發（`requires_human_decision` 矛盾檢查＋固定字串標記掃描），非語意判定；
  真正的承重防線仍是上游 `SKILL.md` 之 fact-layer eligibility gate。

## Artifacts

每次執行落 `.tmp/rigor-gate/ticket-repair/<ticket_id>/<source_run_id>/`（script-root 定錨，非 cwd 相對；
路徑於 `RGR-ARTIFACTS-01` 自 `artifacts/` 遷移），含 input payload、rendered prompt、
raw response、JSONL 事件流（`--json`）、validated response、patch before/after、report append 結果、
stderr（失敗時）。

每個 `ticket_id` 保留最近 5 個 run，逾額由 `housekeeping.sh` 於 runner 主流程自然終點自動清掃
（mtime 最舊先刪；刪除候選 realpath 逸出 `TELEMETRY_ROOT` 即整體中止、零刪除、非零退出）。
janitor 以子 shell 隔離呼叫，其失敗不汙染 runner 既定 exit code。

## SKILL.md 布線位置

布線區塊插入於 `SKILL.md` L345 段（"Repair routing law" 開頭）之後，見該檔本體。
