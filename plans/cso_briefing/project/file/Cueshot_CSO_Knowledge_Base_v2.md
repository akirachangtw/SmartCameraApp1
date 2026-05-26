# Cueshot CSO Knowledge Base v2

**版本**: v2.0 | **日期**: 2026-05-07
**性質**: CSO 對話唯一 Project Knowledge 文件（取代 Strategic Brief v1 + gstack v1.4 + Ultimate Defensive Workflow）
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
- **IFTR (Instruction Follow-Through Rate)**: 引導後 3 秒內姿態改變 ≥3° 的比例，目標 >70%
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

## §2 gstack 核心流程 (v1.4 base + v1.5 delta)

### 角色矩陣

| 角色 | AI | 核心職責 | 禁區 |
|------|-----|---------|------|
| CSO | Claude Opus (claude.ai) | 戰略判斷、票審查 | 實作 |
| 執行參謀長 | Gemini Advanced (Web) | 架構研究 + 執行協調 | 戰略決策、寫 code |
| 實作引擎 | Claude Code (CLI) | /uc 實作、/qa、/sv | 審閱自己的產出 |
| Cross-Reviewer | Codex CLI / GPT | 複合審查 (L1-L2) | 修改程式碼 |
| 終審法官 | Traycer ($0.25/run) | L3-L4 行為級+系統級 | 修 Bug、Shell |

### 標準 SOP
```
0.Resume → 1.Plan(Gemini) → 2.Code(Claude /uc) → 3./qa → 4./sv 
→ 4.5.cross_review(Codex) → 5.Traycer → 6.Git → 7.KAIROS(節點時)
```

### 配額約束
- Traycer: 每日 2-4 次，每次 $0.25
- Claude Code/Pro: 每 3-5hr rate limit
- Codex/ChatGPT: Plus 訂閱額度
- Gemini: 最寬鬆（1M context，最高配額）

### v1.5 Delta（Execution Sequence §0 定義）
- 跨模型審查協議: 同家族互審無效
- GPT-5.5 升級為 L1-L2 審查官+拆票
- Gemini 升級為架構研究+執行協調（本文件 §2 角色矩陣已反映）

---

## §3 技術護欄

### 必防反模式
1. **預設值陷阱**: UI None 狀態必須完全透明/隱藏
2. **狀態機死鎖**: 強制 TTL + 冷卻節流
3. **模型漂移**: SSOT 定義 X，實作創造 Y → 零容忍
4. **多目標 ID 碰撞**: 全局唯一 stableId + IoU 黏滯

### 相機硬體知識
- VCM 馬達慢（2-3s 冷卻），視覺追蹤 10fps 與馬達呼叫脫鉤
- 前鏡頭 Fixed-Focus: 只追求視覺成功
- ML Kit Face/Object 互斥，切換需 500ms hysteresis
- `ListenableFuture` 必須收斂 Executor，嚴禁 `new Thread()`

### Ambient UI 核心色
| 用途 | 顏色 | Alpha |
|------|------|-------|
| Edge Warmth 琥珀金 | `#D4A030` | max 0.20 |
| Halo 未對齊 | `#D4A030` | 0.25 |
| Halo 對齊 | `#50C864` | 0.35 |

---

## §4 CSO 拷問模板

1. **投入產出比**: 花幾天？對 MVP 上架有直接價值嗎？
2. **機會成本**: 同樣時間投別處的收益？
3. **時機判斷**: MVP 該做的，還是 Phase 1.5/2 的？
4. **IFTR 相關性**: 影響北極星指標嗎？怎麼影響？
5. **差異化**: 強化還是稀釋四支柱？
6. **Freemium 分層**: 可學會的 hook 還是不可學會的 retention？

---

## §5 回查指引

| 需求 | 查哪份文件 |
|------|-----------|
| 完整票序 + 依賴圖 + Session 分割 | `Cueshot_Execution_Sequence_v1.md` |
| Gemini 參謀長 Protocol | `Cueshot_Chief_of_Staff_Protocol_Gemini_v2.md` |
| Contract 格式模板 | `CoS_Contract_Templates_v2.md` |
| SSOT 完整規格 | `Cueshot_Merged_Spec_v3_4_1_Stage0.md` |
| PTF 完整碼 / DOF 管線 / EXIF / 評分 | SSOT v3.3 對應章節 |
| 審查報告 + 原始票清單 | `full_audit_and_tickets.md` |

---

**End of CSO Knowledge Base v2**
