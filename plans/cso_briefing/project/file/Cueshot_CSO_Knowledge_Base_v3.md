# Cueshot CSO Knowledge Base v3

**版本**: v3.0.1 | **日期**: 2026-06-14
**性質**: CSO 對話唯一 Project Knowledge 文件
**前版**: v2.0 (2026-05-07) — 本版重大更新：gstack v2.0 角色矩陣、Pipeline v1.6、Phase 0.8 架構、PMF Gate v1、RFC 2119 標記策略
**更新時機**: KAIROS 節點或重大架構變更時

---

## §1 產品 DNA（不可動搖）

**一句話**: 它不只告訴你怎麼拍對，它還懂得什麼時候不該干預。

### 非對稱差異化四支柱
1. **Android-first** vs Pixel/WayShot 的 iOS/Pixel 獨佔
2. **離線 <100ms** vs Pixel Camera Coach 的雲端依賴
3. **Ambient UI 無文字** vs 所有競品的文字指導
4. **美食+遞手機** vs WayShot 的人像+語音

### 北極星
- **IFTR (Instruction Follow-Through Rate)**: 引導後 3 秒內姿態改變 ≥3° 的比例
- MVP 門檻: ≥50%（PMF Gate v1）· Launch 標準: ≥70%
- 優先級: IFTR (P0) > 模式觸發率 (P1) > D7 回訪率 (P2)

### Freemium 切分
| 層級 | 功能 | 邏輯 |
|------|------|------|
| **免費 (Hook — 可學會)** | 角度引導/水平/網格/Ambient UI/鏡頭髒汙/每日 5 張評分/每月 3 次 PTF | 3 次學會→建立信任 |
| **付費 $2.99/月 (Retention — 不可學會)** | 構圖即時診斷/人像六規則/FoodCompositionRule/光線/減法留白/無限 PTF+DOF+評分 | 每次場景不同，持續需要 |

### Paywall 策略
- 觸發式（非 onboarding），第一次拍照完成後卡片底部一行小字
- 付費內容 `blur(3dp)` — 隱約可見有分數，看不清具體數字
- 錨定: WayShot $5.83/月 → Cueshot $2.99 "half the price, Android-first"

---

## §2 gstack v2.0 核心流程

### 角色矩陣

| 角色 | AI | 核心職責 | 禁區 |
|------|-----|---------|------|
| **CSO** | Claude Opus (claude.ai) | 戰略判斷、Contract/Slice/Ticket 產出、規則收割 | 實作、碰代碼 |
| **實作引擎** | Claude Code (CLI) | /uc 實作、/qa、/sv | 審閱自己的產出、修改治理文件 |
| **Cross-Reviewer** | Codex CLI | 機械式 diff 審查（L1-L2）、實作仲裁 | 修改程式碼、戰略決策 |
| **終審法官** | Traycer Pro ($0.25/run) | 行為級+系統級驗證（VS Code Extension, read-only） | 執行 Shell、修改檔案 |
| **反應式仲裁** | GPT/Codex | 實作層級爭議的反應式裁決（僅 Codex 觸發時） | 主動審查、跨家族互審 |

**v2.0 關鍵變更**（vs v1.4/v1.5）：
- Gemini 角色完全移除（原「執行參謀長」→ 拆分為 CSO 自身 + Codex 機械審查）
- CSO 吸收 Contract/Slice 產出（原 Gemini 職責）
- GPT/Codex 降級為「反應式仲裁」（非主動角色）
- 同家族互審禁令不變：ChatGPT/Codex/GPT-5.5 禁止互相 cross-review

### 標準 SOP（Pipeline v1.6）
```
0.Resume → 1.Plan(CSO Contract) → 2.Code(Claude /uc) → 3./qa(verify_checklist.sh)
→ 4./sv → 4.5.cross_review.sh(Codex) → 5.Traycer(高風險票) → 6.Git → 7.KAIROS(節點時)
```

### Pipeline v1.6 基建
| 元件 | 功能 | 部署狀態 |
|------|------|---------|
| `verify_checklist.sh` | /qa Step 0 確定性驗證（零 AI 配額） | ✅ D10 |
| `structural_verify.sh` | diff scope ↔ ALLOWED 一致性驗證 | ✅ D11 |
| `cross_review.sh` v1.2 | 四參數簽名（含 EXTRA_EXCLUDE） | ✅ D10 |
| Auto-Chain | /qa PASS → 自動觸發 /sv | ✅ D11 |
| Stop Hook | ≤2 條停止條件 | ✅ D12 |
| `verify_cmd.conf` | Ticket Verify Config 區塊 → 純文字複製 | ✅ P05 起 |

### Context Firewall 原則
CSO 對話（戰略文件 + Contract）與 Claude Code 對話（實作 context）嚴格隔離。CSO 不觸碰代碼；Claude Code 不接觸戰略推演。中間傳遞物僅為 Contract（文字文件）、Ticket（結構化規格）、Handoff（壓縮狀態）。

### DRA Protocol v1.1
Claude Code 發現治理文件偏差（CLAUDE.md / SSOT / Contract 不一致）時：
- **Discovery**: 可以發現並讀取
- **Report**: 必須回報，不得靜默
- **Approve**: 不得自行修改。等 CSO/Akira 裁決

### 配額約束
| AI | 配額 | 策略 |
|----|------|------|
| Traycer | $0.25/run, 400K context | 養兵千日用在一時 — 僅高風險票 |
| Claude Code | Pro plan, 3-5hr rate limit | /uc 密集模式節省 token |
| Codex | Plus 訂閱 | 機械審查配額充裕 |

---

## §3 技術護欄

### 必防反模式
1. **預設值陷阱**: UI None 狀態必須完全透明/隱藏
2. **狀態機死鎖**: 強制 TTL + 冷卻節流（Rule 22: transition timestamp）
3. **模型漂移**: SSOT 定義 X，實作創造 Y → 零容忍（Rule 27: SSOT 優先裁決）
4. **多目標 ID 碰撞**: 全局唯一 stableId + IoU 黏滯
5. **Partial Fix 盲區**: Bug 修復必須覆蓋所有消費路徑，不得僅修觸發路徑（Rule 25/26）
6. **Ghost Face 壁癌**: ML Kit bbox 越界偽陽性需在 detectionResult 源頭統一過濾

### 相機硬體知識
- VCM 馬達慢（2-3s 冷卻），視覺追蹤 10fps 與馬達呼叫脫鉤
- 前鏡頭 Fixed-Focus: 只追求視覺成功
- ML Kit Face/Object 互斥，切換需 500ms hysteresis
- Sony AF 偏慢，2s 對焦逾時為已知特性（非 regression）
- `ListenableFuture` 必須收斂 Executor，嚴禁 `new Thread()`

### Ambient UI 核心色
| 用途 | 顏色 | Alpha |
|------|------|-------|
| Halo 未對齊 / 琥珀金 | `#D4A030` | 0.25 |
| Halo 對齊 / 綠色（呼吸） | `#50C864` | 0.10↔0.18（週期 2000ms） |

**注意**: SSOT v4.0.6 §2.2 明確定義「三通道均為 IMU 或 ML Kit 驅動，不存在任何名為 Edge Warmth 的渲染元件」。Edge Warmth gradient orphan code 已由 P08-E1 消滅（−561 lines, PitchTractionLayer deletion ✅）。

### CameraViewModel 禁令
CameraViewModel.kt (2,457 行, 15/100 健康分) — 全面禁止新增函數（P08-F3 local lambda refactor ✅ 完成，例外已關閉）。W2 triple-strike 前維持禁令。

---

## §4 當前架構狀態

### Phase 0.8: 清場整備（Active）

| 波次 | 內容 | 票數 | 狀態 |
|------|------|------|------|
| Wave E (Eliminate) | 消滅 orphan code (drawFloatingDots + Edge Warmth) | 1 | ✅ 完成（P08-E1） |
| Wave F (Foundation) | 修正地基（常數/GridOverlay/ML Kit 管線/裝置 Bug） | 6 | 🔧 進行中（F4✅ F5✅ INFRA-45✅；Baseline bugs 待修） |
| Wave L (Legacy) | P05 殘餘閉環 + Scrim 實作 | 3 | 待執行 |
| Gate | Phase 0.5 Visual Baseline（八場景實機截圖） | — | 觸發條件: E+F+L Done |

### PMF Gate v1（Phase 0.8 Gate PASS 後觸發）
- 5 人 beta test（食物攝影創作者，非 Pixel Android）
- IFTR ≥ 50% · ≥2/5 主動第二次使用 → PASS
- 0/5 第二次使用 → 🔴 STOP 重評方向
- Scope Gate（Ticket 開立前 3 問）於 PMF Gate v1 啟動後生效

### Wave 2（Phase 0.8 Done 後）
CameraScreen.kt triple-strike（T02-T04）— 最高風險票，Traycer 主要配額目標。

---

## §5 治理體系

### CLAUDE.md: 12 條核心規則（≤150 行）
核心鐵律 6 條（確認再動手/最小修改/手術刀/越權禁令/寫前先讀/語言鎖定）+ 工程紀律 6 條（Intent-Driven/確定性工作用代碼/測試驗 intent/每步 Checkpoint/衝突不混搭/失敗要大聲）。

**容量紅線**: 12→14 規則曾導致 24 點合規下降。新規則一律走 path-scoped rules（`.claude/rules/`）。

### Path-Scoped Rules（匹配路徑自動載入）
| 文件 | 規則範圍 | 規則數 |
|------|---------|--------|
| `kairos-lessons.md` | K1-K3 實戰萃取（Rule 11-13, 18, 25-26） | 6 |
| `phase0-lessons.md` | Phase 0 退件教訓（Rule 19-24, 27） | 7 |
| `android-conventions.md` | 錯誤防禦/UDF/i18n/Bash/命名/Git | ~15 |
| `camera-constraints.md` | 硬體約束/Rule 15-16 | ~10 |
| `compose-ui.md` | Ambient UI/Rule 14/23 | ~8 |
| `critical-files.md` | 禁刪清單 | 4 |
| `ptf-rules.md` | PTF 場景/Rule 22 | ~5 |

### RFC 2119 強制級別標記
所有 path-scoped rules + SKILL.md 使用 RFC 2119 Keywords：
- **MUST** = 違反即 FAIL（~30%）
- **SHOULD** = 強烈建議，可附理由跳過（~50%）
- **MAY** = 純可選（~20%）
CLAUDE.md 不標記（12 條全為 MUST 級別）。

### Traycer 技術債三級分級
| 嚴重度 | 定義 | MVP 處理 |
|--------|------|---------|
| 🔴 Critical | SSOT drift / 架構違規 / 功能錯誤 | 立即修復 |
| 🟡 Major | 孤兒代碼 / 狀態機時序 / 安全漏洞 | 立即修復 |
| 🟢 Minor | i18n hardcode / touch target / 1.dp | 記入 Debt → Launch 前清 |

---

## §6 CSO 拷問模板

1. **投入產出比**: 花幾天？對 MVP 上架有直接價值嗎？
2. **機會成本**: 同樣時間投別處的收益？
3. **時機判斷**: MVP 該做的，還是 Phase 1.5/2 的？
4. **IFTR 相關性**: 影響北極星指標嗎？怎麼影響？
5. **差異化**: 強化還是稀釋四支柱？
6. **Freemium 分層**: 可學會的 hook 還是不可學會的 retention？
7. **PMF 推進**: 這張 Ticket 直接推進哪個 PMF 指標？（Phase 0.8 後生效）

---

## §7 回查指引

| 需求 | 文件 | Filesystem 路徑 |
|------|------|----------------|
| 完整技術規格 | SSOT v4.0.6 | `plans/architecture/SSOT_v4_0_6.md` |
| UI 佈局規格 | Layout Spec v2.5.1 | `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md` |
| 當前活躍 Contract | P08 Codebase Sanitization | `plans/cso_briefing/project/file/P08_Codebase_Sanitization_Contract.md` |
| Ticket 格式模板 | Unified Ticket Template v3 | `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md` |
| Claude Code 行為契約 | CLAUDE.md (12-Rule) | `.claude/CLAUDE.md` |
| 歷史決策庫 | Obsidian vault (90+ 筆) | `archive_agent_decision` / `read_decision_index` |
| 暫緩議題 | Postponed.md | `plans/cso_briefing/project/Postponed.md` |
| INDEX 導航 | CSO_INDEX.md | `plans/cso_briefing/CSO_INDEX.md` |
| TRA 分析報告 | Playbook + Task Decomposition | Obsidian archive |

---

## §8 關鍵教訓速查（KAIROS 萃取）

| 教訓 | 事件 | 規則 |
|------|------|------|
| 修 Bug 必須覆蓋所有消費路徑 | P05-R1 Ghost Face 壁癌 | Rule 25 |
| 動工前 grep 全部消費者 | 同上，三條路徑僅修一條 | Rule 26 |
| SSOT 與 Ticket 衝突以 SSOT 為準 | TICKET-01 | Rule 27 |
| Decommission 四層 + glob 驗證 | DetectionOverlay 殘留 | Rule 19 |
| Debug 文字 = /sv 即退件 | D8 FIX-01~03 | Rule 24 |
| CLAUDE.md 12 條天花板 | 14→12 合規率回升 24 點 | 容量紅線 |
| 養兵千日用在一時 | Traycer 配額策略 | §2 配額 |
| Context Firewall 嚴格隔離 | 戰略 ↔ 實作 context 汙染 | §2 原則 |

---

**End of CSO Knowledge Base v3**
