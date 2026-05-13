# **獨立開發者：低成本高防禦 gstack 工作流 (Cost-Optimized AI Workflow)**

**版本**: v1.5 (Cueshot MVP Edition)
**變更**: v1.4 → v1.5：四大天王角色矩陣重定義（GPT-5.5 升級為 L1-L2 審查官+拆票、Gemini 升級為架構研究+執行協調）、跨模型審查協議（同家族互審無效）、付費機制與雲地端支援明文化、Execution Sequence §0 Delta 整合

**適用對象**: 資源受限、需嚴格控管 API 成本與每日/每月模型配額的獨立開發者。

**核心理念**: 借鑑 Garry Tan 的 gstack 架構，透過「本地角色化指令 (Slash Commands)」與「免費大腦前置過濾」，將昂貴的 AI 呼叫次數降至最低。**「零重工 (Zero Rework) 就是最大的省錢。」**

---

## **⏳ 雙週配額生存指南 (Quota Survival Guide)**

**【當前約束現實】**

假設 Traycer 帳單重置日為下個月 3 號，若在月中僅剩 $20 美金（約 80 次 $0.25 執行），代表**每日僅有 4.4 次**的 Traycer 執行扣打。同時必須兼顧 Claude Pro / ChatGPT Plus 每 3~5 小時的對話次數限制。

**【v1.5 配額優化策略】**

Codex CLI 複合審查（Layer 1 架構重審 + Layer 2 SV Report 驗證）在 Traycer 之前攔截 SSOT drift 和 Scope Creep，預期將 Traycer 一次通過率從 ~65-70% 提升至 85%+。每次攔截 = 省下 $0.25 Traycer 費用 + 修復迴圈時間。

**【極限配速策略 (Pacing Strategy)】**

1. **Gemini Advanced / CLI (防波堤+架構研究+執行協調)**：巨量 Token 與最寬鬆配額（1M context）。報錯、Log 分析、幾千行 Code 追蹤、SSOT 撰寫、Prompt 壓縮、架構研究全部丟給 Gemini。**Cross-Review fallback**（Codex 不可用時備援）。
2. **Codex CLI / ChatGPT Plus (Cross-Reviewer + 拆票 + 精密計算)**：cross_review.sh 複合審查（L1 架構重審 + L2 SV Report 驗證）+ Ticket 拆票。純邏輯與數學（座標矩陣轉換）。**活化原閒置的訂閱沉沒成本**。
3. **Claude Pro (Opus/Sonnet) / Code (無情打字機)**：利用 `.claude/skills/` 進行本地實作。**啟用 /uc 臥底模式**省下每輪 200-400 tokens。嚴格限制 Context。
4. **Traycer (最終法官)**：**每天只准呼叫 2~4 次**。架構級終審（L3 行為級 + L4 系統級）。禁止修編譯錯誤。

---

## **💰 成本路由策略 v1.5 (Cost Routing Strategy)**

### **四大天王付費機制與雲地端支援**

| AI | 訂閱模式 | 部署環境 | CLI 支援 | 備註 |
|----|---------|---------|---------|------|
| **Gemini** | Pro 訂閱制 | 雲端 / 本地端 | ✅ 支援 CLI 使用訂閱 | 最高配額，1M context |
| **ChatGPT (Codex)** | Plus 訂閱制 | 雲端 / 本地端 | ✅ 支援 CLI 使用訂閱 | Codex CLI 可用訂閱額度 |
| **Claude (Claude Code)** | Pro 訂閱制 | 雲端 / 本地端 | ✅ 支援 CLI 使用訂閱 | Session rate limit 3-5hr |
| **Traycer** | Pro 訂閱制 | 本地端 | ❌ 不支援 CLI | 每次 $0.25，每日 2-4 次 |

### **角色矩陣 v1.5**

| 角色 | AI | 核心職責 | 禁區 |
|------|-----|---------|------|
| CSO（戰略） | Claude Opus (claude.ai) | 戰略判斷、票審查、商業決策 | 實作 |
| 執行參謀長 | Gemini Advanced (Web/CLI) | 架構研究、執行協調、SSOT 撰寫、Prompt 壓縮、**Cross-Review fallback** | 戰略決策、寫 code |
| 實作引擎 | Claude Code (CLI) | /uc 實作、/qa、/sv | 審閱自己的產出 |
| Cross-Reviewer + 拆票 | Codex CLI / GPT-5.5 | L1 架構重審 + L2 SV Report 驗證 + Ticket 拆票 | 修改程式碼 |
| 終審法官 | Traycer ($0.25/run) | L3 行為級 + L4 系統級終審 | 修 Bug、Shell 操作 |

### **v1.4 → v1.5 角色變更說明**

| 變更 | 理由 |
|------|------|
| GPT-5.5 升級：精密計算 → Cross-Reviewer + 拆票 | Codex CLI 有移轉的 Traycer Agent Skills（架構重審），活化為主力審查官 |
| Gemini 升級：防波堤 → 架構研究 + 執行協調 | 1M context 優勢用於架構研究；Cross-Review 讓位 GPT-5.5，改為 fallback |
| 新增跨模型審查協議 | 同家族模型互審無效（如 Claude Code 不可審閱自己的產出） |
| 付費機制 + 雲地端明文化 | 消除認知資訊落差，明確每個工具的部署邊界與 CLI 可用性 |
| Traycer 明確標注不支援 CLI | Traycer 為本地端 IDE 整合，無法在 Shell 自動化流程中呼叫 |

### **跨模型審查協議 (Cross-Model Review Protocol)**

> **核心規則**：同家族模型互審無效。

| 產出方 | 有效審查方 | 無效審查方（同家族） |
|--------|-----------|-----------------|
| Claude Code（Claude 家族） | GPT-5.5 / Gemini | Claude Pro / Claude Opus |
| GPT-5.5（OpenAI 家族） | Claude Code / Gemini | ChatGPT Plus / Codex |
| Gemini（Google 家族） | Claude Code / GPT-5.5 | Gemini Advanced |

### **Codex/GPT vs Traycer 審查面向分工**

| 審查面向 | Codex/GPT-5.5 (Step 4.5) | Traycer (Step 5) |
|---------|--------------------------|-----------------|
| SSOT drift（型別/命名） | ✅ L1 主要負責 | ✅ L4 最終確認 |
| Scope creep（越權修改） | ✅ L1 主要負責 | ✅ L4 最終確認 |
| 依賴方向（Clean Arch） | ✅ L1 主要負責 | L4 二次檢查 |
| SV Report 宣稱驗證 | ✅ L2 主要負責 | ✅ L4 最終確認 |
| Ticket 拆票 | ✅ 新增職責 | ⚠️ 有限備援 |
| Thread safety / lifecycle | ❌ | ✅ L3 主要負責 |
| 效能回歸 | ❌ | ✅ L3 主要負責 |
| 商業邏輯正確性 | ❌ | ✅ L4 主要負責 |
| Orphan code / Anti-mock | ✅ L2 主要負責 | ✅ L4 最終確認 |

---

## **🛠️ gstack 本地化防禦矩陣設定 (Local Defense Matrix Setup)**

### **完整指令集 + 腳本 (v1.5) — Agent Skills Layer 3 規範**

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
│   │       └── cross_review.sh      # Layer 3 程式碼: Codex CLI 複合審查（v1.5）
│   ├── qa/SKILL.md                  # 自動化 QA（Build + Test + PROGRESS 更新）
│   ├── uc/SKILL.md                  # 臥底模式（零廢話純 Diff + Intent Header）
│   ├── normal/SKILL.md              # 退出臥底模式
│   ├── dream/SKILL.md               # KAIROS 階段回顧（含 Archive 觸發回讀）
│   └── compact/SKILL.md             # Context 壓縮交接（v1.4 重寫版）
CLAUDE.md                            # 專案最高憲法
PROGRESS.md                          # 跨 Session 進度（Dashboard ≤50 行 + Archive 分離）
PROGRESS_ARCHIVE.md                  # 已完成 Ticket 歸檔
```

### **指令功能矩陣 (v1.5)**

| 指令 | 觸發 | Token 消耗 | 產出 | 頻率 |
|------|------|-----------|------|------|
| `/uc` | 進入密集 Coding | 極低（純 Diff + Intent Header） | 代碼修改 | 每次 Coding 開始 |
| `/normal` | 需要討論架構 | 正常 | 恢復對話 | 按需 |
| `/qa` | 代碼完成後 | 中（執行腳本） | QA Report + PROGRESS 更新 | 每次實作後 |
| `/sv` | QA 通過後 | 中 | SV Report + **Cross-Review 提示** | 每次提交前 |
| `/compact` | 對話過長 | 低 | 交接文件（Dashboard 格式） | 按需 |
| `/dream` | Sprint 節點 | 高（全面回顧 + Archive 回讀） | KAIROS Report + CLAUDE.md 更新 | D2/D6/D10/D13/D15 |

---

## **🔄 標準作業流程 (SOP) v1.5**

```
0. [Resume]      先讀 PROGRESS.md 確認上次進度
1. [Plan]        將需求貼給 Gemini（架構研究+執行協調），取得壓縮後的 Prompt
2. [Code]        Claude Code 實作（/uc 臥底模式 + Intent Header）
3. [QA]          /qa → 編譯 + 測試 + PROGRESS 自動更新
4. [SV]          /sv → 階段 A-C 語意審查 → 階段 D 自動觸發 Codex Cross-Review
5. [Gate]        Cross PASS → Traycer 終審（每日 2-4 次）
6. [Log]         當日結束 → /compact 壓縮 or 更新 PROGRESS.md
7. [KAIROS]      Sprint 節點 → /dream 回顧（D2/D6/D10/D13/D15）
```

### **SOP 流程圖 (v1.5)**

```
CSO / Gemini (Plan + 架構研究)
    ↓ 壓縮 Prompt
Claude Code (/uc + Intent Header → Code → /normal)
    ↓
/qa (Build + Test + PROGRESS)
    ↓ PASS
/sv 階段 A-C (Orphan + Lint + Scope + SSOT → sv_report.md)
    ↓ PASS → 自動觸發階段 D
/sv 階段 D → cross_review.sh → GPT-5.5 / Codex CLI 複合審查   ◄── 自動路由（跨家族審查）
    ├─ L1: Architectural Review (SSOT drift + Scope + 依賴方向 + Ticket 拆票)
    └─ L2: SV Report Cross-Review (宣稱驗證 + Orphan + Anti-Mock)
    ↓ PASS
Traycer (strict_verification) L3+L4              ◄── 預期一次通過率 85%+
    ↓ PASS
Git Commit → /compact (if needed)
    ↓ Sprint Node?
/dream (KAIROS + Archive 回讀 → CLAUDE.md update)
```

### **Cross-Review 使用方式**

```bash
# 基本用法（預設 diff HEAD~1..HEAD + .claude/skills/sv/ssot_context.md）
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md

# 指定 diff 範圍
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md HEAD~3..HEAD

# 指定 SSOT context 路徑
bash .claude/skills/sv/scripts/cross_review.sh ./sv_report.md HEAD~1..HEAD .claude/skills/sv/ssot_context.md
```

**退出碼**:
- `exit 0`: PASS → 進 Traycer 終審
- `exit 1`: FAIL → 回 Step 2 修正（附 GPT-5.5 問題清單）
- `exit 2`: 環境錯誤（codex 未安裝、非 Git repo、SV Report 不存在）

### **Cross-Review Fallback 策略**

| 狀況 | 動作 |
|------|------|
| Codex CLI 不可用（credit 耗盡/服務中斷） | 改用 Gemini CLI headless 模式（執行參謀長 fallback） |
| SSOT context 不存在 | 降級為純 SV Report 驗證（L2 only） |
| GPT-5.5 輸出格式異常 | 視為 FAIL，人工審查完整輸出 |

---

## **📊 配額約束一覽 (Quota Constraints)**

| AI | 配額類型 | 限制 | 策略 |
|----|---------|------|------|
| Traycer | 金錢（$0.25/run） | 每日 2-4 次 | 僅 L3+L4 終審 |
| Claude Code / Pro | Rate limit | 每 3-5hr | /uc 省 Token，/compact 換 Session |
| Codex / ChatGPT Plus | Plus 訂閱額度 | 月訂閱（活化閒置） | L1-L2 Cross-Review + 拆票 |
| Gemini | Pro 訂閱制 | 最寬鬆（1M context） | 架構研究、大量 Token、fallback |

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
| 1. Plan | — (Gemini 外部) | Gemini Advanced | 架構研究 + 執行協調 + Prompt 壓縮 |
| 2. Code | execute | Claude Code (/uc) | Handoff 交付 + Defensive Warning |
| 3. QA | — (Claude Code 內部) | Claude Code (/qa) | Build + Test + PROGRESS |
| 4. SV | execute_review (對應) | Claude Code (/sv) | 五級分類 + 四項反向掃描 |
| **4.5 Cross** | **architectural_review (備援)** | **GPT-5.5 / Codex CLI** | **L1 架構重審 + L2 SV 驗證 + 拆票** |
| 5. Gate | strict_verification | Traycer (Reviewer) | L3 行為級 + L4 系統級終審 |
| 6. Log | strict_verification | /compact | PROGRESS sync |
| 7. KAIROS | strict_verification | /dream | KAIROS trigger + Learnings + Archive 回讀 |

### **Codex 備援 Traycer 場景映射**

| Traycer 階段 | Codex/GPT-5.5 備援能力 | 備援觸發條件 |
|-------------|----------------------|-------------|
| trigger_workflow | ❌ 不備援 | — |
| **architectural_review** | **✅ 主力用途（Step 4.5 常態使用）** | 每次 /sv 後自動觸發 |
| ticket_breakdown | **✅ GPT-5.5 拆票（v1.5 新增）** | Traycer 配額耗盡時 |
| execute | ❌ 不備援（避免改 code） | — |
| execute_review | ✅ 部分備援（L2：Orphan + Anti-Mock） | Traycer 配額耗盡時 |
| strict_verification | ⚠️ 有限備援（文本級+結構級，缺 L3+L4） | Traycer 配額耗盡時 |

---

## **💡 v1.5 Token 節省估算**

| 項目 | v1.4 | v1.5 | 節省/收益 |
|------|------|------|----------|
| /uc 每輪客套話 | ~200-400 tokens | 0 (+ Intent Header ~20 tokens) | 180-380 |
| PROGRESS.md 載入 | ~500-2000 tokens | ≤200 tokens (Dashboard) | 300-1800 |
| Traycer FAIL 重工 | ~35% 重做率 × $0.25 | 預期 ~15% 重做率 × $0.25 | ~$0.05/次 |
| ChatGPT/Codex 沉沒成本 | 訂閱閒置浪費 | 活化為 L1-L2 Cross-Reviewer + 拆票 | 訂閱回收 |
| 同家族互審無效攔截 | 無規範 | 跨模型審查協議強制執行 | 審查品質提升 |
| 15 天 Sprint 累計 | — | — | 預估省 $3-5 Traycer + 訂閱沉沒成本回收 |

---

## **文件元資訊**

- **版本**: gstack v1.5
- **建立日期**: 2026-05-13
- **變更鏈**: v1.0 → v1.1 → v1.2 → v1.3 → v1.4 → **v1.5**
- **核心變更**: 四大天王角色重定義 + 跨模型審查協議 + 付費機制/雲地端明文化 + GPT-5.5 拆票職責 + Gemini 執行協調升級
- **前置文件**: gstack v1.4, Traycer Ultimate Defensive Workflow v2.0.1, Execution Sequence §0
- **下游影響**: CLAUDE.md §多 AI 分工策略需同步更新角色矩陣

---

**End of gstack v1.5**
