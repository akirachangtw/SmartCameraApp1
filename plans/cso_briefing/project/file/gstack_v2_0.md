# **獨立開發者：低成本高防禦 gstack 工作流 (Cost-Optimized AI Workflow)**

**版本**: v2.0 (CSO-Centric Edition)
**變更**: v1.5 → v2.0：消除 Gemini 執行參謀長角色（CSO 吸收 Slice 產出+架構研究+執行協調）、GPT-5.5 收窄為實作裁決官、Context Firewall 修訂、SOP 簡化為 CSO 直出 Slice、Gemini 降格為工具級 fallback

**適用對象**: 資源受限、需嚴格控管 API 成本與每日/每月模型配額的獨立開發者。

**核心理念**: 借鑑 Garry Tan 的 gstack 架構，透過「本地角色化指令 (Slash Commands)」與「免費大腦前置過濾」，將昂貴的 AI 呼叫次數降至最低。**「零重工 (Zero Rework) 就是最大的省錢。」**

---

## **⏳ 雙週配額生存指南 (Quota Survival Guide)**

**【當前約束現實】**

假設 Traycer 帳單重置日為下個月 3 號，若在月中僅剩 $20 美金（約 80 次 $0.25 執行），代表**每日僅有 4.4 次**的 Traycer 執行扣打。同時必須兼顧 Claude Pro / ChatGPT Plus 每 3~5 小時的對話次數限制。

**【v2.0 配額優化策略】**

v1.5 的 Codex CLI Cross-Review 機制繼續運作（L1 架構重審 + L2 SV Report 驗證），維持 Traycer 一次通過率 85%+。v2.0 額外消除了 Gemini Slice 中轉循環的成本——CSO 直接產出 Slice，省下 2-3 輪審查修正循環（每輪 Akira 中轉 + CSO 審查 token）。

**【極限配速策略 (Pacing Strategy)】**

1. **Claude Pro Opus / claude.ai（CSO — 大腦）**: 戰略判斷、Contract + Slice 產出、架構研究、執行協調。每 session 產出 1-2 份 Slice，context ~200K 限制需監控。
2. **Codex CLI / ChatGPT Plus (Cross-Reviewer + 拆票)**：cross_review.sh 複合審查（L1 架構重審 + L2 SV Report 驗證）+ Ticket 拆票。**活化原閒置的訂閱沉沒成本**。
3. **GPT-5.5 / ChatGPT (實作裁決官)**：僅在 Claude Code 執行中遇到分支決策或 bug 時介入裁決。不產出 Slice、不做 Cross-Review。**按需調用，非常態角色。**
4. **Claude Code (CLI) (實作引擎)**：利用 `.claude/skills/` 進行本地實作。**啟用 /uc 臥底模式**省下每輪 200-400 tokens。嚴格限制 Context。
5. **Traycer (最終法官)**：**每天只准呼叫 2~4 次**。架構級終審（L3 行為級 + L4 系統級）。禁止修編譯錯誤。
6. **Gemini Advanced (工具級 fallback)**：1M context 優勢保留用於巨量 Log 分析、幾千行 Code 追蹤、Cross-Review fallback（Codex 不可用時）。**非正式角色，按需工具。**

---

## **💰 成本路由策略 v2.0 (Cost Routing Strategy)**

### **付費機制與雲地端支援**

| AI | 訂閱模式 | 部署環境 | CLI 支援 | 備註 |
|----|---------|---------|---------|------|
| **Claude (CSO + Code)** | Pro 訂閱制 | 雲端 / 本地端 | ✅ 支援 CLI 使用訂閱 | CSO: claude.ai web; Code: CLI; Session rate limit 3-5hr |
| **ChatGPT (Codex + GPT-5.5)** | Plus 訂閱制 | 雲端 / 本地端 | ✅ 支援 CLI 使用訂閱 | Codex: L1-L2 Cross-Review; GPT-5.5: 實作裁決 |
| **Traycer** | Pro 訂閱制 | 本地端 | ❌ 不支援 CLI | 每次 $0.25，每日 2-4 次 |
| **Gemini** | Pro 訂閱制 | 雲端 / 本地端 | ✅ 支援 CLI 使用訂閱 | 工具級 fallback，1M context |

### **角色矩陣 v2.0**

| 角色                      | AI                      | 核心職責                                           | 禁區               |
| ----------------------- | ----------------------- | ---------------------------------------------- | ---------------- |
| **CSO**（戰略+協調）          | Claude Opus (claude.ai) | 戰略判斷、架構研究、執行協調、SSOT 撰寫、**Contract + Slice 產出** | 寫代碼              |
| **實作裁決官**               | GPT-5.5 (ChatGPT)       | 實作中分支裁決、修補票簽發                                  | 戰略決策、產 Slice、寫代碼 |
| **實作引擎**                | Claude Code (CLI)       | /uc 實作、/qa、/sv                                 | 審閱自己的產出          |
| **Cross-Reviewer + 拆票** | Codex CLI               | L1 架構重審 + L2 SV Report 驗證 + Ticket 拆票          | 修改程式碼            |
| **終審法官**                | Traycer ($0.25/run)     | L3 行為級 + L4 系統級終審                              | 修 Bug、Shell 操作   |

### **v1.5 → v2.0 角色變更說明**

| 變更 | 理由 | 事件支撐 |
|------|------|---------|
| **Gemini 執行參謀長：消除** | Gemini/GPT Slice 品質系統性不穩定（LLM 無法可靠區分「約束」vs「背景」），CSO 每次花等效半份 Slice token 審查修正，加上 Akira 中轉成本，ROI 為負 | P05 Day 2 Slice：Gemini 2 CRITICAL + 3 MAJOR（病灶 B 復發），GPT 0 CRITICAL + 3 修正 |
| **CSO 吸收 Slice 產出** | 寫 Contract 的人最清楚什麼是約束 vs 背景，品質一次到位；消除 Akira 人工中轉成本 | CSO 直出的 P05 Day 2 Slice v1.0 零修正通過 |
| **GPT-5.5 收窄為實作裁決官** | GPT-5.5 在有限邊界內技術裁決表現良好（T2 badge reentry fix），但結構化文件產出不穩定 | P05 Day 1 T2 inline fix 授權成功；Day 2 Slice 仍需 3 處修正 |
| **Gemini 降格為工具級 fallback** | 1M context 優勢保留用於巨量分析，但不再承擔正式角色職責 | 「並行」是幻覺——單人開發者實際串行等待 |
| **Context Firewall 修訂** | Slice 是戰略文件的執行轉譯，不是代碼。Firewall 保護「代碼不進 CSO」而非「執行協調不進 CSO」 | CSO 產出 Slice 未引入實作細節污染 |

### **Context Firewall（v2.0 修訂）**

> **CSO 不寫代碼，Claude Code 不做戰略決策。**
>
> Slice 是 Contract 的精確轉譯，屬於戰略文件範疇，由 CSO 產出不違反 Firewall。
> Firewall 保護的邊界是：**代碼實作 context（debug log、stack trace、code diff）不進入 CSO 推理**，
> 不是「執行協調不進入 CSO」。
>
> CSO 可以產出的：Contract、Slice、SSOT、架構決策、審查裁決。
> CSO 不碰的：`.kt` 檔案、`git diff` 內容、編譯錯誤修復、runtime log 分析。

### **跨模型審查協議 (Cross-Model Review Protocol)**

> **核心規則**：同家族模型互審無效。

| 產出方 | 有效審查方 | 無效審查方（同家族） |
|--------|-----------|-----------------|
| Claude Code（Claude 家族） | GPT-5.5 / Codex / Gemini | Claude Pro / Claude Opus |
| GPT-5.5（OpenAI 家族） | Claude Code / Gemini | ChatGPT Plus / Codex |
| Gemini（Google 家族） | Claude Code / GPT-5.5 / Codex | Gemini Advanced |

**v2.0 備註**：CSO 產出的 Slice 不需跨模型審查——Slice 是 Contract 的轉譯，Contract 本身即 CSO 裁決，不存在「自己審自己」的問題（Slice 的驗收由 Claude Code 的 /qa + /sv + Codex Cross-Review 執行鏈自然覆蓋）。

### **Codex vs Traycer 審查面向分工（v2.0 更新）**

| 審查面向 | Codex CLI (Step 4.5) | GPT-5.5 (Step 2.5, 按需) | Traycer (Step 5) |
|---------|---------------------|-------------------------|-----------------|
| SSOT drift（型別/命名） | ✅ L1 主要負責 | — | ✅ L4 最終確認 |
| Scope creep（越權修改） | ✅ L1 主要負責 | — | ✅ L4 最終確認 |
| 依賴方向（Clean Arch） | ✅ L1 主要負責 | — | L4 二次檢查 |
| SV Report 宣稱驗證 | ✅ L2 主要負責 | — | ✅ L4 最終確認 |
| Ticket 拆票 | ✅ 維持職責 | — | ⚠️ 有限備援 |
| 實作分支裁決 | — | ✅ 主要職責 | — |
| 修補票簽發 | — | ✅ 主要職責 | — |
| Thread safety / lifecycle | — | — | ✅ L3 主要負責 |
| 效能回歸 | — | — | ✅ L3 主要負責 |
| 商業邏輯正確性 | — | — | ✅ L4 主要負責 |
| Orphan code / Anti-mock | ✅ L2 主要負責 | — | ✅ L4 最終確認 |

---

## **🛠️ gstack 本地化防禦矩陣設定 (Local Defense Matrix Setup)**

### **完整指令集 + 腳本 (v2.0) — Agent Skills Layer 3 規範**

> 遵循 Anthropic Agent Skills 三層漸進式揭露架構：
> - Layer 1（元資料）：YAML 前置資料，啟動時載入，~100 tokens
> - Layer 2（指令）：SKILL.md 主體，觸發時載入，<5K tokens
> - Layer 3（資源+程式碼）：捆綁檔案，按需載入，不佔 context 直到被引用

```
.claude/
├── skills/
│   ├── sv/                          # /sv 無情法官 Skill
│   │   ├── SKILL.md                 # Layer 2: 語意+編譯+Lint+邊界+SSOT+Cross-Review路由
│   │   ├── CROSS_REVIEW.md          # Layer 3 指令: Cross-Review 完整指南
│   │   ├── ssot_context.md          # Layer 3 資源: SSOT 精簡上下文（~120 行）
│   │   └── scripts/
│   │       └── cross_review.sh      # Layer 3 程式碼: Codex CLI 複合審查（v1.2: per-call EXTRA_EXCLUDE）
│   ├── qa/SKILL.md                  # 自動化 QA（Build + Test + PROGRESS 更新）
│   ├── uc/SKILL.md                  # 臥底模式（零廢話純 Diff + Intent Header）
│   ├── normal/SKILL.md              # 退出臥底模式
│   ├── dream/SKILL.md               # KAIROS 階段回顧（含 Archive 觸發回讀）
│   └── compact/SKILL.md             # Context 壓縮交接（v1.4 重寫版）
CLAUDE.md                            # 專案最高憲法
PROGRESS.md                          # 跨 Session 進度（Dashboard ≤50 行 + Archive 分離）
PROGRESS_ARCHIVE.md                  # 已完成 Ticket 歸檔
```

### **指令功能矩陣 (v2.0)**

| 指令 | 觸發 | Token 消耗 | 產出 | 頻率 |
|------|------|-----------|------|------|
| `/uc` | 進入密集 Coding | 極低（純 Diff + Intent Header） | 代碼修改 | 每次 Coding 開始 |
| `/normal` | 需要討論架構 | 正常 | 恢復對話 | 按需 |
| `/qa` | 代碼完成後 | 中（執行腳本） | QA Report + PROGRESS 更新 | 每次實作後 |
| `/sv` | QA 通過後 | 中 | SV Report + **Cross-Review 提示** | 每次提交前 |
| `/compact` | 對話過長 | 低 | 交接文件（Dashboard 格式） | 按需 |
| `/dream` | Sprint 節點 | 高（全面回顧 + Archive 回讀） | KAIROS Report + CLAUDE.md 更新 | D2/D6/D10/D13/D15 |

---

## **🔄 標準作業流程 (SOP) v2.0**

```
0. [Resume]      先讀 PROGRESS.md 確認上次進度
1. [Plan+Slice]  CSO 產出 Contract + Slice（直接交付 Claude Code）
2. [Code]        Claude Code 實作（/uc 臥底模式 + Intent Header）
  2.5 [裁決]     如遇分支決策 → GPT-5.5 裁決 → 回到 Step 2
3. [QA]          /qa → 編譯 + 測試 + PROGRESS 自動更新
4. [SV]          /sv → 階段 A-C 語意審查 → 階段 D 自動觸發 Codex Cross-Review
5. [Gate]        Cross PASS → Traycer 終審（每日 2-4 次）
6. [Log]         當日結束 → /compact 壓縮 or 更新 PROGRESS.md
7. [KAIROS]      Sprint 節點 → /dream 回顧（D2/D6/D10/D13/D15）
```

### **SOP 流程圖 (v2.0)**

```
CSO (Contract + Slice 直出)
    ↓ Slice 交付
Claude Code (/uc + Intent Header → Code → /normal)
    │
    ├─ [if 分支裁決 needed] → GPT-5.5 裁決 → return
    │
    ↓
/qa (Build + Test + PROGRESS)
    ↓ PASS
/sv 階段 A-C (Orphan + Lint + Scope + SSOT → sv_report.md)
    ↓ PASS → 自動觸發階段 D
/sv 階段 D → cross_review.sh → Codex CLI 複合審查         ◄── 自動路由（跨家族審查）
    ├─ L1: Architectural Review (SSOT drift + Scope + 依賴方向 + Ticket 拆票)
    └─ L2: SV Report Cross-Review (宣稱驗證 + Orphan + Anti-Mock)
    ↓ PASS
Traycer (strict_verification) L3+L4                       ◄── 預期一次通過率 85%+
    ↓ PASS
Git Commit → /compact (if needed)
    ↓ Sprint Node?
/dream (KAIROS + Archive 回讀 → CLAUDE.md update)
```

### **v1.5 → v2.0 SOP 差異**

| Step | v1.5 | v2.0 | 變更理由 |
|------|------|------|---------|
| 1 | Gemini Plan（架構研究+執行協調+Prompt 壓縮） | **CSO Plan+Slice 直出** | 消除 Gemini 中轉，品質一次到位 |
| 2.5 | 不存在 | **GPT-5.5 條件裁決** | 實作中分支決策需要非 Claude 家族的裁決 |
| 4.5 | GPT-5.5 / Codex CLI 共同審查 | **Codex CLI 單獨審查** | GPT-5.5 職責收窄，Cross-Review 由 Codex 獨擔 |

### **Cross-Review 使用方式**

```bash
# 基本用法（預設 diff HEAD~1..HEAD + .claude/skills/sv/ssot_context.md）
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md

# 指定 diff 範圍
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md HEAD~3..HEAD

# 指定 diff 範圍 + per-call 排除（v1.2: INFRA-001）
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md "a9558fc..c65482f" "*/CameraScreen.kt"
```

**退出碼**:
- `exit 0`: PASS → 進 Traycer 終審
- `exit 1`: FAIL → 回 Step 2 修正（附 Codex 問題清單）
- `exit 2`: 環境錯誤（codex 未安裝、非 Git repo、SV Report 不存在）

### **Cross-Review Fallback 策略**

| 狀況 | 動作 |
|------|------|
| Codex CLI 不可用（credit 耗盡/服務中斷） | 改用 Gemini CLI headless 模式（工具級 fallback） |
| SSOT context 不存在 | 降級為純 SV Report 驗證（L2 only） |
| Codex 輸出格式異常 | 視為 FAIL，人工審查完整輸出 |

### **GPT-5.5 實作裁決官使用時機**

| 觸發條件 | 裁決範圍 | 產出 |
|---------|---------|------|
| Claude Code 執行中遇到設計分支（如「用 StateFlow 還是 SharedFlow?」） | 限定在 Slice scope 內的技術選擇 | 裁決結論（一句話） |
| Claude Code 發現 Slice 未覆蓋的邊界 case | 補充 Slice 缺漏的 edge case 處理 | 修補指令 |
| 實作完成後發現非 scope 內的 bug（如 T2 badge reentry） | 評估 inline fix vs 獨立票 | inline fix 授權 或 新票草案 |

**GPT-5.5 禁區**：不做戰略決策（CSO 職責）、不產出 Slice（已證明品質不穩定）、不寫代碼、不做 Cross-Review。

---

## **📊 配額約束一覽 (Quota Constraints)**

| AI | 配額類型 | 限制 | 策略 |
|----|---------|------|------|
| Traycer | 金錢（$0.25/run） | 每日 2-4 次 | 僅 L3+L4 終審 |
| Claude Opus (CSO) | claude.ai session | ~200K context | 每 session 1-2 份 Slice + Contract |
| Claude Code | Rate limit | 每 3-5hr | /uc 省 Token，/compact 換 Session |
| Codex CLI | Plus 訂閱額度 | 月訂閱（活化閒置） | L1-L2 Cross-Review + 拆票 |
| GPT-5.5 | Plus 訂閱額度 | 共享 Codex 額度 | 按需裁決（非常態消耗） |
| Gemini | Pro 訂閱制 | 最寬鬆（1M context） | 工具級 fallback，不佔正式角色 |

---

## **📊 PROGRESS.md Dashboard 格式 (v1.4+)**

```markdown
# Cueshot MVP — PROGRESS Dashboard

## Current Sprint Day: D[X] | Date: YYYY-MM-DD
## Context Health: 🟢/🟡/🔴

### Active Tickets
| Ticket | Status | Blocker |
|--------|--------|---------|
| D8-T1  | 🔧 WIP | — |

### Today's Focus
1. [最重要的一件事]

### Decisions Pending
- [待決事項]

---
歷史歸檔見 PROGRESS_ARCHIVE.md
```

目標：Dashboard ≤ 50 行。已完成 Ticket 移至 `PROGRESS_ARCHIVE.md`。

---

## **🔗 與 Traycer Ultimate Defensive Workflow v2.0.1 對齊表**

| gstack 步驟 | Traycer Command | 執行者 | 對齊方式 |
|-------------|----------------|--------|---------|
| 0. Resume | trigger_workflow | Traycer (Planner) | Gate Entry Summary 含 PROGRESS 確認 |
| **1. Plan+Slice** | — (**CSO 直出**) | **CSO (Claude Opus)** | **Contract + Slice 產出，直接交付 Claude Code** |
| 2. Code | execute | Claude Code (/uc) | Handoff 交付 + Defensive Warning |
| **2.5 裁決** | — (按需) | **GPT-5.5** | **分支裁決 + 修補票簽發** |
| 3. QA | — (Claude Code 內部) | Claude Code (/qa) | Build + Test + PROGRESS |
| 4. SV | execute_review (對應) | Claude Code (/sv) | 五級分類 + 四項反向掃描 |
| **4.5 Cross** | **architectural_review (備援)** | **Codex CLI** | **L1 架構重審 + L2 SV 驗證 + 拆票** |
| 5. Gate | strict_verification | Traycer (Reviewer) | L3 行為級 + L4 系統級終審 |
| 6. Log | strict_verification | /compact | PROGRESS sync |
| 7. KAIROS | strict_verification | /dream | KAIROS trigger + Learnings + Archive 回讀 |

### **Codex 備援 Traycer 場景映射**

| Traycer 階段 | Codex CLI 備援能力 | 備援觸發條件 |
|-------------|-------------------|-------------|
| trigger_workflow | ❌ 不備援 | — |
| **architectural_review** | **✅ 主力用途（Step 4.5 常態使用）** | 每次 /sv 後自動觸發 |
| ticket_breakdown | ✅ Codex 拆票 | Traycer 配額耗盡時 |
| execute | ❌ 不備援（避免改 code） | — |
| execute_review | ✅ 部分備援（L2：Orphan + Anti-Mock） | Traycer 配額耗盡時 |
| strict_verification | ⚠️ 有限備援（文本級+結構級，缺 L3+L4） | Traycer 配額耗盡時 |

---

## **💡 v2.0 Token 節省估算**

| 項目 | v1.5 | v2.0 | 節省/收益 |
|------|------|------|----------|
| /uc 每輪客套話 | 0 (Intent Header) | 0 (不變) | 不變 |
| PROGRESS.md 載入 | ≤200 tokens (Dashboard) | ≤200 tokens (不變) | 不變 |
| Traycer FAIL 重工 | ~15% 重做率 | ~15% 重做率 (不變) | 不變 |
| **Slice 中轉循環** | **2-3 輪 × (Gemini 產出 + CSO 審查 + Akira 中轉)** | **0 輪（CSO 直出）** | **~2000-4000 tokens/Slice + Akira 人工時間** |
| **Slice 品質返工** | **Gemini 2 CRITICAL / GPT 3 修正** | **零修正（CSO 自審通過）** | **品質成本歸零** |
| ChatGPT/Codex 沉沒成本 | 活化為 Cross-Reviewer + 拆票 | 維持（Codex 獨擔） | 不變 |
| GPT-5.5 使用頻率 | 常態（每票 Cross-Review） | **按需（僅分支裁決）** | **GPT-5.5 配額釋放給 Codex** |

---

## **文件元資訊**

- **版本**: gstack v2.0
- **建立日期**: 2026-05-22
- **變更鏈**: v1.0 → v1.1 → v1.2 → v1.3 → v1.4 → v1.5 → **v2.0**
- **核心變更**: 消除 Gemini 執行參謀長 + CSO 吸收 Slice + GPT-5.5 收窄為實作裁決官 + Context Firewall 修訂 + SOP 簡化
- **前置文件**: gstack v1.5, Traycer Ultimate Defensive Workflow v2.0.1, CSO Session 12 決策歸檔
- **觸發事件**: P05 Day 2 Slice 品質問題（Gemini 2 CRITICAL + GPT 3 修正）暴露 LLM Slice 產出的系統性不穩定
- **下游影響**: CLAUDE.md §多 AI 分工策略需同步更新角色矩陣 + CSO Knowledge Base v2 → v3
- **作廢文件**: gstack v1.0~v1.5 及所有前版

---

**End of gstack v2.0**
