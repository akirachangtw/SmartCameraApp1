# INFRA-GOV-DEESC-01 Rev A — 執行者交接摘要（Executor Briefing）

**日期**: 2026-07-25 ｜ **簽發**: CSO ｜ **配套票**: `Pending/INFRA-GOV-DEESC-01_Adjudication_Noise_Deescalation.md`（Rev A）
**用途**: 你（Claude Code 執行者）於上輪依 Error Handling 條款 1 正確停止回報（四條款錨點全數失效）。CSO 已裁決並產出 Rev A。本摘要說明裁決結果與執行要點，避免重蹈錨點錯位。票面為 SSOT，本摘要僅為導讀；兩者若有出入，以票面為準並停止回報。

## 一、你的停止回報 → CSO 裁決對照

| 你回報的問題 | CSO 裁決 | Rev A 落點 |
|---|---|---|
| AI-1 §1/§2 規則本文只在 CLAUDE.md（FORBIDDEN） | **選 B**：擴 ALLOWED 納入 `.claude/CLAUDE.md`，兩檔同步、SSOT 定於 Rule 4a | AI-1a（改 Rule 4a）+ AI-1b（ticket_pattern.md 僅指針，禁複寫規則本文） |
| AI-1 §3 六欄表無相容錨點 | **選 A**：新增獨立區塊 | AI-1b `## Family Provenance 補登紀錄`；AI-1c 補 `MEO-` 行（補登日期改 **2026-07-25**） |
| AI-2 §1 無票據模板章節 | **選 A**：新建 SSOT 落點 | AI-2a 新增 `## 票據模板規範` 章節 |
| AI-2 §2 qa/SKILL.md 無 Boot Sequence | **選 A**（採你的判讀）：寫入 Step 0 Scope Gate | AI-2b，不強求「Boot Sequence」字面 |
| AI-2 §3 Anchor Drift 全 repo 無定義 | **選 A**：框定「首次成文」 | AI-2c，MUST 字面註明「尚無 hook 機械強制點」 |

## 二、執行紅線（票面 Hard Constraints 之導讀）

1. **CLAUDE.md 僅限 Rule 4a**（HC-4）——兩分支改寫不得波及其他 Rule、Boot Sequence 或任何段落；Ticket ID regex 行位元不動。
2. **SSOT 單寫紀律**：兩分支規則本文只活在 CLAUDE.md Rule 4a；ticket_pattern.md 變更紀錄表尾只放指針句。若你發現自己在兩檔寫入相同規則文字 → 違反設計，停止。
3. **既有表結構不動**：ticket_pattern.md 兩張 3 欄表一列不改，六欄表為全新獨立區塊。
4. doc-only：`.sh`/`.py`/引擎/regex 一律不碰。

## 三、開工程序（依 AI-2b 之三原子動作，本票自舉適用）

1. `git rev-parse --short HEAD` → 回填本票 `DIFF_REF`。
2. 再生 `tmp/verify_cmd.conf`（REV=A、DIFF_REF=回填值）。
3. 回填值與 conf 摘要記入證據欄。三動作缺一即停。

## 四、AC 驗證要點

- **負向保留驗證（行為性 AC）**：紙上推演「commit 訊息憑空出現 `XYZ-` family、Pending/與 deliver/ 皆無票檔」→ 必須落 STOP 分支。推演過程逐字記入證據欄，禁只寫結論。
- `git diff` 佐證三件事分開列：① Rule 4a 兩分支落地且 regex 行未動；② ticket_pattern.md 僅新增區塊/敘述行；③ 總變更 ≤ MAX_LINES=120。
- commit 帶 `[INFRA-GOV-DEESC-01]` 前綴。

## 五、停止條款提醒（Rev A 新增第三條）

- 三檔任一 patch 錨點模糊 → 停止回報。
- 現行文字與票面「現值」不符 → 停止回報，禁強行套用。
- **CLAUDE.md Rule 4a 現值與 AI-1a 描述不符（Rule 編號漂移）→ 停止回報，禁猜測對應。**

## 六、下游依賴（供你理解排程壓力，非執行內容）

本票完成＋收案後，`MEO-FIX-A` Rev E 方可回填 DIFF_REF、重建 conf、送 Codex 復審。本票是全線唯一阻塞點，但**寧可停止回報，不可為排程搶進度**——DEFENSIVE WARNING 依然有效。
