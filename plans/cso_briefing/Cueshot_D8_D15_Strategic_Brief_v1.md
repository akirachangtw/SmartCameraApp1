# Cueshot D8-D15 戰略作戰摘要 v1

**文件版本**: D8-D15 Brief v1.0-20260416
**性質**: 瘦身壓縮文件 — 取代 SSOT v3.3 / Tech Deep Dive v3 / Roadmap v1 的 D8-D15 必要段落
**適用階段**: D8 PTF 收尾 → D15 MVP 完工
**使用方式**: CSO 對話唯一載入文件；需細節時再按需查原版

---

## PART A：產品 DNA（不可動搖的決策前提）

### A.1 一句話定位
> **它不只告訴你怎麼拍對,它還懂得什麼時候不該干預。**

### A.2 非對稱差異化四支柱
1. **Android-first** vs Pixel/WayShot 的 iOS/Pixel 獨佔
2. **離線 <100ms** vs Pixel Camera Coach 的雲端依賴
3. **Ambient UI 無文字** vs 所有競品的文字指導
4. **美食+遞手機** vs WayShot 的人像+語音

### A.3 北極星指標
- **IFTR (Instruction Follow-Through Rate)**: 引導後 3 秒內姿態改變 ≥ 3° 的比例
- **目標**: > 70%
- **MVP 追蹤優先級**: IFTR (P0) > 模式觸發率 (P1) > D7 回訪率 (P2)

### A.4 Freemium 切分哲學（反「7天畢業效應」核心）

| 層級 | 功能 | 邏輯 |
|------|------|------|
| **免費 (Hook — 可學會)** | 角度引導/單軸水平/三分網格/Ambient UI 外觀/鏡頭髒汙/每日 5 張評分/每月 3 次 PTF | 3 次學會 → 建立信任 |
| **付費 $2.99/月 (Retention — 不可學會)** | 構圖即時診斷/人像六規則/美食 FoodCompositionRule/光線分析/減法留白/無限 PTF/拍後 DOF/評分無限 | 每次場景不同,持續需要 |

### A.5 Paywall 視覺策略
拍後教學卡片:免費版**構圖/光線/診斷文字全部 blur(3dp)**,只看得到角度/水平分數。底部 "Unlock full diagnosis" CTA。

---

## PART B：D8 當前作戰地圖

### B.1 D8 三個 Ticket

| Ticket | 內容 | 模式 | CSO 優先級 |
|--------|------|------|-----------|
| D8-T1 | PTF 規則引擎(FramingRule 居中 + AngleRule 粗檢 + LevelRule 粗檢) | /uc | **必做** |
| D8-T2 | PTF Halo 牽引(人臉→畫面中心引力場)+ 綠色色系 | /uc | **必做** |
| D8-T3 | 首次使用教學動畫(3 秒循環) | /uc | **可砍** |

### B.2 PTF 三規則戰略順位(若時間不夠的取捨)

**CSO 建議保留順序**:
1. **FramingRule 居中** — 朋友幫拍最容易失誤:主體沒居中
2. **LevelRule 水平** — 朋友幫拍第二失誤:手機歪斜
3. **AngleRule 粗檢** — 可犧牲 — PTF 是社交場景,不是美食場景,朋友不會為角度死磕

### B.3 PTF 核心技術規格速查

| 項目 | 規格 |
|------|------|
| Tab 位置 | 底部全寬 36dp,`navigationBarsPadding()` |
| Tab 行為 | toggle(再點退出) |
| 首次 Bottom Sheet | 20%,`max(screenHeight * 0.20, 96.dp)` |
| 切換動畫 | 300ms,純 UI,零 CameraX rebind |
| 快門冷卻 | 進入 PTF 後 1 秒內不可按 |
| Exit 雙路徑 | Tab 再點 / ✕ 純圖示按鈕(快門左側) |
| Halo 顏色 | 金色(未對齊)→ 綠色(對齊) |
| Edge Warmth 顏色 | 金色 → 綠色 |
| PTF Halo 目標 | `targetPosition = 畫面中心`(非三分交叉點) |
| 規則引擎 | 六規則降為三規則,< 0.1ms |

### B.4 PTF 零硬體切換鐵律(絕對不可違反)
- 不重新 bind CameraX
- 不 close/reopen ML Kit Face Detection(PTF 和 Portrait 都用 Face Detection)
- 不重新校準 IMU
- 不重編 GPU shader
- **全部改動僅限 Compose UI state + 變數賦值**

### B.5 D8 KAIROS #2 歸檔檢查
D6 結束的 KAIROS #2 結論是否已寫入 `CLAUDE.md` 的「⚙️ KAIROS 產出的架構守則」?
如果沒有,D8-D10 會重複踩 ML Kit 互斥切換的坑。

---

## PART C：D9-D15 戰略路徑

### C.1 剩餘 Sprint 日曆

| Day | 日期 | 交付 | KAIROS | 戰略重點 |
|-----|------|------|--------|---------|
| D9 | 04-17(五) | 美食構圖(ML Kit Object Detection FOOD) | — | 主戰場入口 |
| D10 | 04-20(一) | FoodCompositionRule + MinimalistRule + 雜亂框 | **#3** | 減法留白是產品哲學核心體現 |
| D11 | 04-21(二) | 鏡頭髒汙偵測(純 Kotlin Laplacian) | — | 信任建立器 |
| D12 | 04-22(三) | 光線直方圖 + 呼吸韻律 + 三級裝置降級 | — | 效能防禦 |
| D13 | 04-23(四) | **Paywall + 評分 + EXIF + 拍後卡片 + DOF UI 骨架** | **#4** | **商業層關鍵日** |
| D14 | 04-24(五) | IFTR 埋點 + Settings + i18n (en/zh-TW/ja) | — | 上架準備 |
| D15 | 04-27(一) | 整合測試 + 餐廳實測 + 參數微調 | **#5 Final** | PMF 首驗證 |

### C.2 D13 商業層關鍵決策清單(最重要的一天)

| 決策項 | CSO 建議 |
|--------|---------|
| 定價 $2.99/月 | **錨定策略**:WayShot $69.99/年 ≈ $5.83/月 → Cueshot 主打 "half the price, Android-first" |
| 免費限制觸發時機 | 觸發式(不在 onboarding 說),第一次拍照完成 → 卡片底部一行小字 |
| 每日 5 張限制文案 | "5 free scored photos today · Upgrade for unlimited" |
| PTF 次數用完文案 | "PTF limit reached. Upgrade to Pro." |
| Blur 程度 | `blur(3dp)` — 隱約可見有分數、有顏色,看不清具體數字 |
| DOF 按鈕 | MVP 反灰(Phase 1.5 啟用),避免用戶失望 |

### C.3 KAIROS 節點預擬問題

**KAIROS #3 (D10 結束)**:
- ML Kit Object Detection FOOD 實測信心度閾值
- bgCV 雜亂背景閾值 (目前 0.35) 是否需調整
- MinimalistRule Path A / Path B 覆蓋率
- 雜亂框 UI (三角形) 在實際場景是否會干擾

**KAIROS #4 (D13 結束)**:
- 評分公式權重 (角度 0.40 / 水平 0.25 / 構圖 0.20 / 光線 0.15) 是否需調整
- Blur 3dp 的 UX 感受 — 用戶是否能抓到 "有東西但看不清" 的訊號
- Paywall 觸發文案 A/B 候選
- DOF 反灰按鈕的用戶反應

**KAIROS #5 (D15 Final)**:
- IFTR 初步數據 (是否達 70%)
- 三級裝置降級實測 (FULL / LIMITED / LEGACY)
- i18n 品質(英日中三語)
- 上架 Readiness 評估

---

## PART D：Ambient UI 核心規格速查

### D.1 雙通道引導體系

| 通道 | 觸發 | 感測器 | 刷新率 | 視覺 |
|------|------|--------|--------|------|
| Edge Warmth | 手機旋轉/傾斜 | IMU | 60fps | 邊緣漸層暖色光暈 |
| Halo | 手機平移/取景 | ML Kit | 10fps + EMA | 主體周圍柔和光環 |

### D.2 核心顏色

| 用途 | 顏色 | Alpha |
|------|------|-------|
| Edge Warmth 琥珀金 | `#D4A030` | 最大 0.20 |
| Halo 未對齊 | `#D4A030`(金) | 0.25 |
| Halo 對齊 | `#50C864`(綠) | 0.35 |
| 對焦 Focusing | 白色 | 0.9 |
| 對焦 Focused | 綠色 | 0.8 |
| 對焦 Failed | 琥珀 | 0.6 |
| 雜亂三角警示 | `#FFD040` | 0.4 |

### D.3 呼吸韻律公式
```
allConditionsMet → alpha 0.10 ↔ 0.18, 2000ms 週期, FastOutSlowInEasing
```

### D.4 場景判斷邏輯(D8 仍適用)
```
PASS_TO_FRIEND (手動) > pitch > 35° (Food) > face detected (Portrait) > 其他 (General)
切換 hysteresis: 500ms 防跳動
```

---

## PART E：效能防禦要點(D12 接入)

### E.1 分幀計算策略
```
Frame 1: ML Kit + Rules + Priority → 65ms
Frame 2: Histogram + Minimalist + Clutter → 13ms
Frame 3: ML Kit + Rules + Priority → 65ms
Frame 4: Histogram + Minimalist + Clutter → 13ms
Frame 5: Lens Smudge → 12ms
```

### E.2 三級裝置降級

| 等級 | 核心 | 輔助 | 關閉 |
|------|------|------|------|
| FULL | EW 60fps + Halo 10fps + 所有規則 | 髒汙 2fps + 雜亂框 + 減法留白 | — |
| LIMITED | EW 60fps + Halo 8fps + 核心 4 規則 | 髒汙 1fps + 減法留白 | 雜亂框 |
| LEGACY | EW 60fps + Halo 5fps + 2 規則 | — | 髒汙 + 減法留白 + 雜亂框 + 光線 |

**鐵律**: Edge Warmth 永遠 60fps — 任何裝置都能跑。

---

## PART F：風險警戒清單(D8-D15 相關)

| 風險 | 階段 | 對沖 |
|------|------|------|
| ML Kit Object Detection 美食偵測不穩 | D9-D10 | 降級為 IMU 模式(角度+水平+網格) |
| 減法留白偵測誤判 | D10 | Path A/B 雙路徑 + confidence 閾值 |
| 鏡頭髒汙誤報 | D11 | Settings 開關 + 白名單文案 |
| 低階機 Edge Warmth 卡頓 | D12 | 三級降級,EW 保底 60fps |
| Paywall 太早出現引用戶流失 | D13 | 觸發式,不在 onboarding 提限制 |
| IFTR < 70% | D15 | KAIROS #5 微調 Edge Warmth alpha 與 Halo 靈敏度 |
| PTF 朋友誤觸快門 | D7-D8 | **已處理**:1 秒冷卻 |
| PTF 退出路徑不明 | D7-D8 | **已處理**:Tab + ✕ 雙路徑 |

---

## PART G：必要時回查原版的指引

以下情境才需回查完整文件:

| 情境 | 查哪份 | 查哪段 |
|------|--------|--------|
| MinimalistRule Path A/B 完整邏輯 | SSOT v3.3 | §4.6 |
| Edge Warmth 完整實作碼 | SSOT v3.3 | §2.3 |
| PTF Tab Composable 完整碼 | Tech Deep Dive v3 | §4.4 |
| 拍後 DOF 五步管線 | SSOT v3.3 | §4.7 |
| EXIF JSON 結構 | SSOT v3.3 | §7.2 |
| 評分公式細節 | SSOT v3.3 | §7.1 |
| i18n 字串表 | Tech Deep Dive v3 | §7.2 |
| D0 絞殺者手術細節 | Roadmap v1 | 第二部分 |

---

## PART H：CSO 常用拷問模板

接下來的 CSO 對話,若需戰略判斷可以用這幾個角度切入:

1. **投入產出比**: 這個決策花幾天?產出對 MVP 上架有直接價值嗎?
2. **機會成本**: 同樣時間投別處的戰略收益是?
3. **時機判斷**: 這是 MVP 階段該做的,還是 Phase 1.5/Phase 2 的?
4. **IFTR 相關性**: 這個決策會影響 IFTR 嗎?怎麼影響?
5. **競品差異化**: 這個決策強化還是稀釋了四支柱(Android/離線/Ambient/美食+PTF)?
6. **Freemium 分層**: 這屬於可學會的 hook 還是不可學會的 retention?

---

## 文件元資訊

- **版本**: D8-D15 Brief v1.0
- **建立日期**: 2026-04-16 (D8 實作階段)
- **取代目標**: SSOT v3.3 / Tech Deep Dive v3 / Roadmap v1 的 D8-D15 必要段落
- **適用範圍**: D8 開工 → D15 MVP 完工 → KAIROS #5 Final
- **預期效果**: 載入後 Context 從 52% 降至 ~35-40% (綠燈區)
- **回查原版觸發條件**: 見 PART G 指引

---

**End of Strategic Brief**
