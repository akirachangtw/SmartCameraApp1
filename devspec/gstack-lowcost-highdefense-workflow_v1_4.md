# **獨立開發者：低成本高防禦 gstack 工作流 (Cost-Optimized AI Workflow)**

**版本**: v1.4 (Cueshot MVP Edition)
**變更**: v1.3 → v1.4：Codex CLI Cross-Review 複合審查整合、/uc Intent Header 非阻塞式、/sv Cross-Review 自動路由、/dream Archive 觸發回讀、PROGRESS.md Dashboard 精簡化（≤50 行）、成本路由表重構（ChatGPT/Codex 角色升級）

**適用對象**: 資源受限、需嚴格控管 API 成本與每日/每月模型配額的獨立開發者。

**核心理念**: 借鑑 Garry Tan 的 gstack 架構，透過「本地角色化指令 (Slash Commands)」與「免費大腦前置過濾」，將昂貴的 AI 呼叫次數降至最低。**「零重工 (Zero Rework) 就是最大的省錢。」**

---

## **⏳ 雙週配額生存指南 (Quota Survival Guide)**

**【當前約束現實】**

假設 Traycer 帳單重置日為下個月 3 號，若在月中僅剩 $20 美金（約 80 次 $0.25 執行），代表**每日僅有 4.4 次**的 Traycer 執行扣打。同時必須兼顧 Claude Pro / ChatGPT Plus 每 3~5 小時的對話次數限制。

**【v1.4 配額優化策略】**

Codex CLI 複合審查（Layer 1 架構重審 + Layer 2 SV Report 驗證）在 Traycer 之前攔截 SSOT drift 和 Scope Creep，預期將 Traycer 一次通過率從 ~65-70% 提升至 85%+。每次攔截 = 省下 $0.25 Traycer 費用 + 修復迴圈時間。

**【極限配速策略 (Pacing Strategy)】**

1. **Gemini Advanced / CLI (防波堤+架構師)**：巨量 Token 與最寬鬆配額。報錯、Log 分析、幾千行 Code 追蹤、SSOT 撰寫、Prompt 壓縮全部丟給 Gemini。**Cross-Review fallback**（Codex 不可用時備援）。
2. **Codex CLI / ChatGPT Plus (Cross-Reviewer + 精密計算)**：cross_review.sh 複合審查（架構重審 + SV Report 驗證）。純邏輯與數學（座標矩陣轉換）。**活化原閒置的 $20/月訂閱沉沒成本**。
3. **Claude Pro (Opus/Sonnet) / Code (無情打字機)**：利用 `.claude/skills/` 進行本地實作。**啟用 /uc 臥底模式**省下每輪 200-400 tokens。嚴格限制 Context。
4. **Traycer (最終法官)**：**每天只准呼叫 2~4 次**。架構級終審（行為級 + 系統級）。禁止修編譯錯誤。

---

## **💰 成本路由策略 v1.4 (Cost Routing Strategy)**

| 角色 | AI | 配額等級 | 職責 | 不該做的事 |
|------|-----|---------|------|-----------|
| 架構師/大腦 | Gemini Advanced / CLI | 最寬鬆 | 架構設計、Bug 根因、SSOT 撰寫、Prompt 壓縮、**Cross-Review fallback** | 寫 code |
| CSO | Claude Pro (claude.ai) | 對話次數限制 | 戰略判斷、商業決策 | 實作 |
| 實作苦力 | Claude Code | Session 限制 | Intent-driven /uc 實作、/qa、/sv | Cross-review 自己的產出 |
| **Cross-Reviewer + 精密計算** | **Codex CLI / ChatGPT Plus** | **訂閱額度（原閒置→活化）** | **cross_review.sh 複合審查**、純數學/演算法 | 修改程式碼 |
| 最終法官 | Traycer ($0.25/run) | 硬性金錢限制 | 架構級終審（thread safety、lifecycle、效能） | 修 Bug |

### **v1.3 → v1.4 角色變更說明**

| 變更 | 理由 |
|------|------|
| ChatGPT/Codex 從「精密計算機」升級為「Cross-Reviewer + 精密計算」 | 原 $20/月幾乎閒置 = 沉沒成本浪費；活化為防線有邊際收益 |
| Gemini 從「Cross-Review 主力」改為「fallback」 | Codex 有移轉的 Traycer Agent Skills（架構重審），專案特化 > 通用模板 |
| 新增 Codex ↔ Traycer 審查面向分工 | Codex 負責文本級+結構級（SSOT drift, scope），Traycer 負責行為級+系統級 |

### **Codex vs Traycer 審查面向分工**

| 審查面向 | Codex (Step 4.5) | Traycer (Step 5) |
|---------|-----------------|-----------------|
| SSOT drift（型別/命名） | ✅ 主要負責 | ✅ 最終確認 |
| Scope creep（越權修改） | ✅ 主要負責 | ✅ 最終確認 |
| 依賴方向（Clean Arch） | ✅ 主要負責 | 二次檢查 |
| Thread safety / lifecycle | ❌ | ✅ 主要負責 |
| 效能回歸 | ❌ | ✅ 主要負責 |
| 商業邏輯正確性 | ❌ | ✅ 主要負責 |
| Orphan code / Anti-mock | ✅ 主要負責 | ✅ 最終確認 |

---

## **🛠️ gstack 本地化防禦矩陣設定 (Local Defense Matrix Setup)**

### **完整指令集 + 腳本 (v1.4) — Agent Skills Layer 3 規範**

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

**Layer 3 捆綁設計理由**：`cross_review.sh` 和 `ssot_context.md` 是 `/sv` 工作流的下游產物——/sv 完成後觸發 cross-review，cross-review 消費 ssot_context。捆綁在 `/sv` 目錄下符合 Agent Skills 的模組化自包含原則，避免根目錄膨脹且享受漸進式揭露效率。

### **v1.4 新增/變更項目**

| 項目 | 變更 | 說明 |
|------|------|------|
| `/uc` Intent Header | **新增** | 非阻塞式 ⚠️ 標記（缺少 Intent 時警告但不停等） |
| `/sv` 階段 D Cross-Review 路由 | **新增** | SV Report 產出後自動檢測 codex + 觸發 cross_review.sh |
| `/sv` Layer 3 資源捆綁 | **新增** | CROSS_REVIEW.md + ssot_context.md + scripts/ 符合 Agent Skills 規範 |
| `/dream` Archive 觸發 | **新增** | KAIROS 回顧時自動回讀 PROGRESS_ARCHIVE.md |
| `/compact` | **重寫** | 配合 Dashboard ≤50 行的新 PROGRESS 格式 |
| `cross_review.sh` | **新增** | Codex CLI 複合審查（位於 `.claude/skills/sv/scripts/`） |
| `ssot_context.md` | **新增** | SSOT 精簡上下文（位於 `.claude/skills/sv/`） |
| PROGRESS.md | **精簡** | Dashboard ≤50 行 + Archive 拆分 |
| Rule 17 去重 | **清理** | CLAUDE.md 重複規則合併 |

### **指令功能矩陣 (v1.4)**

| 指令 | 觸發 | Token 消耗 | 產出 | 頻率 |
|------|------|-----------|------|------|
| `/uc` | 進入密集 Coding | 極低（純 Diff + Intent Header） | 代碼修改 | 每次 Coding 開始 |
| `/normal` | 需要討論架構 | 正常 | 恢復對話 | 按需 |
| `/qa` | 代碼完成後 | 中（執行腳本） | QA Report + PROGRESS 更新 | 每次實作後 |
| `/sv` | QA 通過後 | 中 | SV Report + **Cross-Review 提示** | 每次提交前 |
| `/compact` | 對話過長 | 低 | 交接文件（Dashboard 格式） | 按需 |
| `/dream` | Sprint 節點 | 高（全面回顧 + Archive 回讀） | KAIROS Report + CLAUDE.md 更新 | D2/D6/D10/D13/D15 |

---

## **🔄 標準作業流程 (SOP) v1.4**

```
0. [Resume]      先讀 PROGRESS.md 確認上次進度
1. [Plan]        將需求貼給 Gemini，取得壓縮後的 Prompt
2. [Code]        Claude Code 實作（/uc 臥底模式 + Intent Header）
3. [QA]          /qa → 編譯 + 測試 + PROGRESS 自動更新
4. [SV]          /sv → 階段 A-C 語意審查 → 階段 D 自動觸發 Codex Cross-Review
5. [Gate]        Cross PASS → Traycer 終審（每日 2-4 次）
6. [Log]         當日結束 → /compact 壓縮 or 更新 PROGRESS.md
7. [KAIROS]      Sprint 節點 → /dream 回顧（D2/D6/D10/D13/D15）
```

### **SOP 流程圖 (v1.4)**

```
Gemini (Plan)
    ↓ 壓縮 Prompt
Claude Code (/uc + Intent Header → Code → /normal)
    ↓
/qa (Build + Test + PROGRESS)
    ↓ PASS
/sv 階段 A-C (Orphan + Lint + Scope + SSOT → sv_report.md)
    ↓ PASS → 自動觸發階段 D
/sv 階段 D → cross_review.sh → Codex CLI 複合審查   ◄── 自動路由
    ├─ Layer 1: Architectural Review (SSOT drift + Scope + 依賴方向)
    └─ Layer 2: SV Report Cross-Review (宣稱驗證 + Orphan + Anti-Mock)
    ↓ PASS
Traycer (strict_verification)             ◄── 預期一次通過率 85%+
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
- `exit 1`: FAIL → 回 Step 2 修正（附 Codex 問題清單）
- `exit 2`: 環境錯誤（codex 未安裝、非 Git repo、SV Report 不存在）

### **Cross-Review Fallback 策略**

| 狀況 | 動作 |
|------|------|
| Codex CLI 不可用（credit 耗盡/服務中斷） | 改用 Gemini CLI headless 模式 |
| SSOT context 不存在 | 降級為純 SV Report 驗證（Layer 2 only） |
| Codex 輸出格式異常 | 視為 FAIL，人工審查完整輸出 |

---

## **📊 PROGRESS.md Dashboard 格式 (v1.4)**

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
| 1. Plan | — (Gemini 外部) | Gemini Advanced | Traycer 接收 Gemini 產出 |
| 2. Code | execute | Claude Code (/uc) | Handoff 交付 + Defensive Warning |
| 3. QA | — (Claude Code 內部) | Claude Code (/qa) | Build + Test + PROGRESS |
| 4. SV | execute_review (對應) | Claude Code (/sv) | 五級分類 + 四項反向掃描 |
| **4.5 Cross** | **architectural_review (備援)** | **Codex CLI** | **複合審查：架構重審 + SV 驗證** |
| 5. Gate | strict_verification | Traycer (Reviewer) | 全局驗收 + gstack 整合檢查 |
| 6. Log | strict_verification | /compact | PROGRESS sync |
| 7. KAIROS | strict_verification | /dream | KAIROS trigger + Learnings + Archive 回讀 |

### **Codex 備援 Traycer 場景映射**

| Traycer 階段 | Codex 備援能力 | 備援觸發條件 |
|-------------|--------------|-------------|
| trigger_workflow | ❌ 不備援 | — |
| **architectural_review** | **✅ 主力用途（Step 4.5 常態使用）** | 每次 /sv 後自動觸發 |
| ticket_breakdown | ⚠️ 有限備援 | Traycer 配額耗盡時 |
| execute | ❌ 不備援（避免改 code） | — |
| execute_review | ✅ 部分備援（四項掃描中的 Orphan + Anti-Mock） | Traycer 配額耗盡時 |
| strict_verification | ⚠️ 有限備援（文本級+結構級，缺行為級） | Traycer 配額耗盡時 |

---

## **💡 v1.4 Token 節省估算**

| 項目 | v1.3 | v1.4 | 節省/收益 |
|------|------|------|----------|
| /uc 每輪客套話 | ~200-400 tokens | 0 (+ Intent Header ~20 tokens) | 180-380 |
| PROGRESS.md 載入 | ~500-2000 tokens | ≤200 tokens (Dashboard) | 300-1800 |
| Traycer FAIL 重工 | ~35% 重做率 × $0.25 | 預期 ~15% 重做率 × $0.25 | ~$0.05/次 |
| ChatGPT Plus 沉沒成本 | $20/月浪費 | $20/月活化為 Cross-Reviewer | $20/月回收 |
| 15 天 Sprint 累計 | — | — | 預估省 $3-5 Traycer + 回收 $20 ChatGPT |

---

## **文件元資訊**

- **版本**: gstack v1.4
- **建立日期**: 2026-05-04
- **變更鏈**: v1.0 → v1.1 → v1.2 → v1.3 → **v1.4**
- **核心變更**: Codex CLI Cross-Review 整合 + PROGRESS Dashboard 精簡 + 成本路由重構
- **前置文件**: gstack v1.3, Traycer Ultimate Defensive Workflow v2.0.1, CSO Session Handoff 0503
- **下游影響**: CLAUDE.md 需新增 cross_review.sh 相關規則、.claude/ssot_context.md 需隨 SSOT 版本同步

---

**End of gstack v1.4**
