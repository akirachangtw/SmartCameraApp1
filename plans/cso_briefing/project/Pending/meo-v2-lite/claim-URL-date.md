<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

## 證據表

| Claim | URL | Date | 證據力 | 備註 |
| :-- | :-- | --: | :-- | :-- |
| Claude Code hooks 將 `SessionStart` 與 `SessionEnd` 視為每個 session 一次，`Stop` 與 `StopFailure` 視為每個 turn 一次。 | [code.claude.com/docs/en/hooks](https://code.claude.com/docs/en/hooks) | 2026-07-01 | 官方文件 | 這是 lifecycle 語義的核心來源。 |
| `SessionStart` 在 `--resume` 時會再次執行，且 `source` 會標示為 `resume`，用途是刷新上下文。 | [code.claude.com/docs/en/hooks](https://code.claude.com/docs/en/hooks) | 2026-07-01 | 官方文件 | 可用來支持「跨 session 重新注入上下文」而非依賴 mutable state。 |
| `Stop` hooks 會接收 `stop_hook_active`，避免 stop hook 自我循環；當 stop hook 的結果讓 Claude Code 繼續執行時，該旗標反映目前狀態。 | [code.claude.com/docs/en/hooks](https://code.claude.com/docs/en/hooks) | 2026-07-01 | 官方文件 | 可用來判定 stop hook 的 re-entry / recursion 防護語義。 |
| 中文版 hooks 文檔說明 `Stop`、`SubagentStop`、`SessionStart`、`SessionEnd` 的生命週期分工，且 `SessionEnd` 用於會話結束清理與狀態保存。 | [code.claude.com/docs/zh-TW/hooks](https://code.claude.com/docs/zh-TW/hooks) | 2026-07-01 | 官方文件 | 中文版可作為同源佐證。 |
| 中文版 hooks 文檔指出 `SessionEnd` 預設 timeout 為 1.5 秒，並適用於會話退出、`/clear`、以及互動式 `/resume` 切換會話。 | [code.claude.com/docs/zh-TW/hooks](https://code.claude.com/docs/zh-TW/hooks) | 2026-07-01 | 官方文件 | 有助於理解 session 終止與恢復語義。 |
| 日文版 hooks 文檔指出，若停止是由使用者中斷引起，`Stop` 不會執行，而是發出 `StopFailure`。 | [code.claude.com/docs/ja/hooks](https://code.claude.com/docs/ja/hooks) | 2026-07-01 | 官方文件 | 可用於區分正常 stop 與中斷失敗。 |
| GitHub issue `#4318` 顯示 `SessionStart` / `SessionEnd` 生命週期 hook 在 2025 年仍屬公開討論功能需求。 | [github.com/anthropics/claude-code/issues/4318](https://github.com/anthropics/claude-code/issues/4318) | 2025-07-23 | issue 討論 | 不是 bug 證據，但可作為功能演進線索。 |
| Claude Code hooks 的社群解說文章將 hooks 分成 session / turn / tool 三種 cadence，並描述 `SessionStart` 為 session begins or resumes。 | [blakecrosley.com/blog/claude-code-hooks-explained](https://blakecrosley.com/blog/claude-code-hooks-explained) | 2026-06-30 | 工程實務文章 | 可作為官方文件的補充解釋。 |
| 社群實作文章提到 `stop_hook_active` 可用於避免無限執行循環。 | [morphllm.com/claude-code-hooks](https://www.morphllm.com/claude-code-hooks) | 2026-06-17 | 工程實務文章 | 非官方，但可作為實作參考。 |
| write-ahead log 的核心是把每次 state change 當成命令順序追加到 append-only log，以提供 durability。 | [oreilly.com/library/view/patterns-of-distributed/9780138222246/ch03.xhtml](https://www.oreilly.com/library/view/patterns-of-distributed/9780138222246/ch03.xhtml) | 2023-04-07 | 經典基礎資料 | 較舊，但屬經典前例；可降權使用。 |
| JSONL 可作為 AI agent runtime 的原生 observability 格式，採 append-only events 搭配 snapshot checkpoints 做 replay 與 recovery。 | [zylos.ai/research/2026-06-06-jsonl-agent-observability-data-format/](https://zylos.ai/research/2026-06-06-jsonl-agent-observability-data-format/) | 2026-06-05 | 工程實務文章 | 與跨 session coordinator 最直接相關。 |
| JSONL append-only logs 適合 record / replay mode，並可用快照跳過已完成步驟。 | [devbytes.co.in/news/this-jsonl-logging-pattern-enables-near-instant-fault-recovery](https://devbytes.co.in/news/this-jsonl-logging-pattern-enables-near-instant-fault-recovery) | 2026-07-18 | 工程實務文章 | 有助於支持 deterministic fold / replay。 |
| Append-only log 是 immutable ordered sequence，可作為 event sourcing 的基礎。 | [agentica.wiki/articles/append-only-log](https://agentica.wiki/articles/append-only-log) | 2026-03-27 | 工程實務文章 | 可作為 event-sourcing 前例。 |
| `SessionStart` hooks 在社群實作裡常被用來「注入前一段脈絡」，而不是當作可靠的共享 mutable state。 | [docs.claude-mem.ai/architecture/hooks](https://docs.claude-mem.ai/architecture/hooks) | 2026-06-xx | 工程實務文章 | 日期未明確，證據力略低於有明確日期者。 |
| 事件驅動 hooks 文章主張 `SessionStart` 載入上次進度、`SessionEnd` 存檔，顯示跨 session 狀態宜外部化。 | [ganhua.wang/claude-code-event-driven-hooks](https://ganhua.wang/claude-code-event-driven-hooks) | 2026-01-23 | 工程實務文章 | 對「session state 外部化」有直接支持。 |
| 多 session 並行的 Claude Code 實務中，常見做法是把 session 進度分離，避免互相覆蓋。 | [jonroosevelt.com/blog/claude-code-session-isolation-hooks](https://jonroosevelt.com/blog/claude-code-session-isolation-hooks/) | 2026-02-15 | 工程實務文章 | 可支持 session isolation 的設計方向。 |
| TOCTOU 的核心防禦是把 check 與 use 合併成原子操作，或在 check-through-use 期間保持鎖定。 | [dipsylala.github.io/FlawFixingGuidance/CWE-367/](https://dipsylala.github.io/FlawFixingGuidance/CWE-367/) | 2026-?? | 安全指南 | 日期不完整，適合作為原則引用，不宜當最強證據。 |
| 經典 secure programming 指南建議使用 `O_CREAT | O_EXCL`，避免以非原子 check/use 模式建立 lock 或臨時檔。 | [dwheeler.com/secure-programs/Secure-Programs-HOWTO/avoid-race.html](https://dwheeler.com/secure-programs/Secure-Programs-HOWTO/avoid-race.html) | 未標示 | 經典基礎資料 | 較舊，證據力應低於 2025–2026 新資料。 |
| 分散式鎖的 TTL 並不可靠，因 clock skew、GC pause、partition 仍可能造成 split-brain。 | [computingseries.com/s/book3/ch23_distributed_locks](https://computingseries.com/s/book3/ch23_distributed_locks) | 未標示 | 工程說明 | 可作為 lock file 只能當協調工具的佐證。 |
| JSONL 單行損壞不會使整份檔案失效，讀取端可跳過 malformed lines。 | [jsonic.io/guides/jsonl-format](https://jsonic.io/guides/jsonl-format) | 2026-05-11 | 工程實務文章 | 支持 partial-line recovery。 |
| JSONL/JSONL logging 實務文章指出，partial writes 可能造成最後一行截斷，應依賴 atomic writes 或 append-safe storage。 | [aiopsschool.com/blog/jsonl/](https://aiopsschool.com/blog/jsonl/) | 2026-02-15 | 工程實務文章 | 可用來支持 torn-write 風險說明。 |
| JSONL 作為結構化 logging 格式的原因之一，是 append-only 與 streaming 能力，且壞行不會拖垮整份資料。 | [superjson.ai/blog/2025-09-07-jsonl-vs-json-data-processing/](https://superjson.ai/blog/2025-09-07-jsonl-vs-json-data-processing/) | 2025-09-06 | 工程實務文章 | 2025 來源，可保留但次於 2026 來源。 |

## 可直接引用的結論句

- Claude Code hooks 的官方語義支持把 `SessionStart` / `SessionEnd` 視為 session 級事件，把 `Stop` 視為 turn 級事件，且 `SessionStart` 在 `--resume` 時會重新執行。[code.claude.com/docs/en/hooks]
- 目前可得證據更支持 **append-only events + deterministic reducer**，而不是讓 agent 自寫 mutable state 直接驅動 runtime hard-deny。[zylos.ai/research/2026-06-06-jsonl-agent-observability-data-format/][oreilly.com/library/view/patterns-of-distributed/9780138222246/ch03.xhtml]
- 以 lock file 做跨 session 協調時，應把它視為協調工具而非絕對保證，並注意 TOCTOU 與 TTL split-brain 風險。[dipsylala.github.io/FlawFixingGuidance/CWE-367/][computingseries.com/s/book3/ch23_distributed_locks]