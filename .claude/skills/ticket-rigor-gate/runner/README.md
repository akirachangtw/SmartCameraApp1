# Codex Ticket Repair Runner

## 契約 SSOT

此 runner 依 `Skill-Advisor/deliver/ticket-rigor-gate-skill/ticket-quality-skill-codex-runner-contract.md`
（跨 repo，非本 repo 內檔案）落地實作。本 README 不重述契約全文，僅記載本次落地之版本綁定與偏差。

- Payload schema 版本：`ticket-repair-payload/v1`（`schemas/ticket-repair-payload.v1.json`）
- Response schema 版本：`ticket-repair-response/v1`（`schemas/ticket-repair-response.v1.json`）
- Prompt 模板版本：`ticket-repair-prompt/v1`（`prompts/ticket-repair-prompt.v1.txt`）
- Model profiles 版本：`model-profiles/v1`（`profiles/model-profiles.v1.json`）

## §7.2 Model contract 驗證層——已退役（`RGR-MODEL-CONTRACT-01`，commit `ab23c62`，2026-09-16）

契約 §7.2 字面為固定值比對（`run_summary.model` 必須是 `gpt-5.6-sol`）；本落地曾依裁決
`RigorGate_R2_Terra_Routing_Profile_Switch_And_A5_Family`（2026-08-20，KD-6）改為比對本次 run 宣告之
profile。**該比對已整段移除**：`run_summary.model`／`run_summary.reasoning_effort` 為模型輸出之自述，
runner 自有事件流（`codex exec --json` stdout）無 `turn_context`，故無可信 provider metadata producer，
自述不承載 provider identity 語意。現行 runner 不再存在 `validate_model_contract()` 或 RTR-006 拒絕路徑；
`RTR_RETRYABLE` 表中之 `RTR-006` 條目為未被引用之靜態保留項。

`run_summary` 仍由 response schema 要求存在（結構檢查），但僅為 audit 資料。命令組裝之 `-m`／`-c`
仍自 `profiles/model-profiles.v1.json` 解析（`repair_runner.sh` 之 `MODEL`／`EFFORT`），此部分未變。

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

- §7.1–§7.8 驗證層中，§7.2 model contract 已退役（見上節），現行為**七層**（schema／eligibility／
  evidence／target hash／diff budget／frozen sentence／decision leakage）均由主執行路徑依序呼叫；
  任一層未過即禁寫入，該 finding 整筆回退。`validate_response_structure` 另含 RTR-005 之
  `ticket_id` 一致性與非空 `finding_results` 守門（`RGR-CODEX-TRANSPORT-01` AI-2）。
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

每個 `ticket_id` 保留最近 10 個 run，逾額由 `housekeeping.sh` 於 runner 主流程自然終點自動清掃
（mtime 最舊先刪；刪除候選 realpath 逸出 `TELEMETRY_ROOT` 即整體中止、零刪除、非零退出）。
janitor 以子 shell 隔離呼叫，其失敗不汙染 runner 既定 exit code。

## SKILL.md 布線位置

布線區塊插入於 `SKILL.md` L345 段（"Repair routing law" 開頭）之後，見該檔本體。
