# **獨立開發者：低成本高防禦 gstack 工作流 (Cost-Optimized AI Workflow)**

**版本**: v1.3 (Cueshot MVP Edition)
**變更**: v1.2 → v1.3：新增 PROGRESS.md 機制、/uc 臥底模式、/dream KAIROS 回顧、/compact Context 壓縮、/sv /qa 確定性強化

**適用對象**: 資源受限、需嚴格控管 API 成本與每日/每月模型配額的獨立開發者。

**核心理念**: 借鑑 Garry Tan 的 gstack 架構，透過「本地角色化指令 (Slash Commands)」與「免費大腦前置過濾」，將昂貴的 AI 呼叫次數降至最低。**「零重工 (Zero Rework) 就是最大的省錢。」**

## **⏳ 雙週配額生存指南 (Quota Survival Guide)**

**【當前約束現實】**

假設 Traycer 帳單重置日為下個月 3 號，若在月中僅剩 $20 美金（約 80 次 $0.25 執行），代表**每日僅有 4.4 次**的 Traycer 執行扣打。同時必須兼顧 Claude Pro / ChatGPT Plus 每 3~5 小時的對話次數限制。

**【極限配速策略 (Pacing Strategy)】**

1. **Gemini Advanced (防波堤)**：巨量 Token 與最寬鬆配額。**報錯、Log 分析、幾千行 Code 追蹤全部丟給 Gemini。**
2. **ChatGPT Plus (精密計算機)**：純邏輯與數學（座標矩陣轉換）。**一次只問一個精確問題**，拿完就走。
3. **Claude Pro (Opus/Sonnet) / CLI (無情打字機)**：利用 `.claude/commands/` 進行本地實作。**啟用 /uc 臥底模式**省下每輪 200-400 tokens 客套話。嚴格限制 Context 丟入量。
4. **Traycer (最終法官)**：**每天只准呼叫 2~4 次**。架構拆票 + 終審蓋章。禁止修編譯錯誤。

## **💰 成本路由策略 (Cost Routing Strategy)**

| 角色 | AI | 配額等級 | 職責 | 原則 |
|------|-----|---------|------|------|
| 架構師/大腦 | Gemini Advanced | 最寬鬆 | 架構設計、Bug 根因分析、SSOT 撰寫、Prompt 壓縮 | 絕對不讓付費 API 去「猜」需求 |
| CSO + 實作 | Claude Pro | 對話次數限制 | 苦力實作、/sv 自我驗收、/qa 編譯檢查 | /uc 省 Token + 限制 Context |
| 最終法官 | Traycer ($0.25/run) | 硬性金錢限制 | 架構級 Code Review、最終驗收 | 法官不負責修 Bug |
| 精密計算 | ChatGPT Plus | 降級選用 | 純數學/演算法 | 拿完 Function 就走 |

## **🛠️ gstack 本地化防禦矩陣設定 (Local Defense Matrix Setup)**

### **完整指令集 (v1.3)**

```
.claude/
├── skills/
│   ├── sv/SKILL.md       # 無情法官（語意 + 編譯 + Lint + 邊界 + SSOT 漂移）
│   ├── qa/SKILL.md       # 自動化 QA（Build + Test + Warning + PROGRESS 更新）
│   ├── uc/SKILL.md       # 臥底模式（零廢話純 Diff）
│   ├── normal/SKILL.md   # 退出臥底模式
│   ├── dream/SKILL.md    # KAIROS 階段回顧
│   └── compact/SKILL.md  # Context 壓縮交接
CLAUDE.md                  # 專案最高憲法
PROGRESS.md                # 跨 Session 進度狀態
```

### **指令功能矩陣**

| 指令 | 觸發 | Token 消耗 | 產出 | 頻率 |
|------|------|-----------|------|------|
| `/uc` | 進入密集 Coding | 極低（純 Diff） | 代碼修改 | 每次 Coding 開始 |
| `/normal` | 需要討論架構 | 正常 | 恢復對話 | 按需 |
| `/qa` | 代碼完成後 | 中（執行腳本） | QA Report + PROGRESS 更新 | 每次實作後 |
| `/sv` | QA 通過後 | 中 | SV Report | 每次提交前 |
| `/compact` | 對話過長 | 低 | 交接文件 | 按需 |
| `/dream` | Sprint 節點 | 高（全面回顧） | KAIROS Report + CLAUDE.md 更新 | D2/D6/D10/D13/D15 |

## **🔄 標準作業流程 (SOP) v1.3**

```
0. [Resume]      先讀 PROGRESS.md 確認上次進度
1. [Plan]        將需求貼給 Gemini，取得壓縮後的 Prompt
2. [Code]        Claude Code 實作（/uc 臥底模式）
3. [QA]          /qa → 編譯 + 測試 + PROGRESS 自動更新
4. [SV]          /sv → 語意審查 + 確定性驗證
5. [Gate]        SV + QA 都通過 → Traycer 終審（每日 2-4 次）
6. [Log]         當日結束 → /compact 壓縮 or 更新 PROGRESS.md
7. [KAIROS]      Sprint 節點 → /dream 回顧（D2/D6/D10/D13/D15）
```

### **SOP 流程圖**

```
Gemini (Plan)
    ↓ 壓縮 Prompt
Claude Code (/uc → Code → /normal)
    ↓
/qa (Build + Test + PROGRESS)
    ↓ PASS
/sv (Orphan + Lint + Scope + SSOT)
    ↓ PASS
Traycer (strict_verification)
    ↓ PASS
Git Commit → /compact (if needed)
    ↓ Sprint Node?
/dream (KAIROS → CLAUDE.md update)
```

### **臥底模式 Token 節省估算**

| 項目 | Normal 模式 | /uc 模式 | 節省 |
|------|------------|---------|------|
| 每輪客套話 | ~200-400 tokens | 0 | 200-400 |
| 15 天 Sprint (10-20 修改/天) | 30K-120K | 0 | 30K-120K |
| 等效對話空間 | — | — | +2-3 輪完整架構對話 |
