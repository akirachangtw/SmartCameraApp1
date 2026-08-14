# KAIROS #4 教訓候選彙整 — 完整版（K4-1 ~ K4-55）

**本檔為 KAIROS #4 候選登記唯一真理來源 (Canonical Registry)**。`PROGRESS.md` §D13 候選議程僅作暫存區，合併後即從 PROGRESS 移除。

**彙整日期**: 2026-06-20（最後更新 S17 合併；INFRA-XX 稽核更新 S28 / 2026-07-03；INFRA-XX 第二批併入 S28 同日）
**來源**: CSO Session VI (Turns 1-16) + 00_Handoff S11~S17 追加 + PROGRESS.md D13 議程併入（K4-34/35）+ 散落「教訓收割（待 KAIROS）」表併入（K4-29~33, K4-36~45，R-T2/R-T9~R-T14 系列 session）
**候選數量**: 55 項（K4-1~55 連續；無空號；K4-52~54 = LH-VAC-02 三候選；K4-55 = STATE-CONTRACT-03 Traycer R1 閉環同步律，CSO 裁示收錄 2026-07-10）
**觸發時機**: Phase 0.8 Visual Baseline Gate PASS 後首個 KAIROS 節點（尚未觸發 — S28 現況：D12，GAP-06/GAP-07 仍 🔴 P0 blocked）

---

## 全量候選表

| ID | 教訓 | 事件支撐 | 品類 |
|----|------|---------|------|
| K4-1 | verify_cmd.conf DIFF_REF/DIFF_END 應在 Ticket 閉環時歸檔至 sv_report.md 或 Obsidian | P05-R1 config 被後續 ticket 覆寫後無法還原 → R2 不可執行 | Pipeline |
| K4-2 | Scope Ban 規則需包含「Contract 授權例外」條款，否則機械閘門對合法操作產生 false positive | P05-R1（EyeAfController）+ P08-E1（*Test.kt）— 同構問題兩次發生 | 治理 |
| K4-3 | Option A（sv_report.md 補文字 → 重跑）對 Layer 3.4 機械閘門無效 — 遇 Scope Ban BLOCKER + 有 Contract 授權 → 直接 Option D | P05-R1 Option A → R2 高概率 BLOCK；P08-E1 同構確認 | Pipeline |
| K4-4 | ScaleType 等全局屬性統一變更後，需掃描所有依賴該屬性語意的計算邏輯 | P0-VF 統一 FILL_CENTER 但 calculateContentBounds 仍用 FIT_CENTER 數學 → Grid 43% 覆蓋缺口 | 依賴鏈 |
| K4-5 | 兩個 Bug 可能互為解藥 — 修 A 之前需確認 A 的「錯誤輸出」是否為 B 的「正確輸入」 | contentBounds FIT_CENTER「錯誤」可能為 TapFocus center-crop 映射提供正確值 | 架構 |
| K4-6 | Ghost Face 是 Bug **品類**而非單一 Bug — 修復 scope 必須覆蓋「品類邊界」而非「已知實例」 | P05-R1→F3→HOTFIX-45 三次壁癌，每次修一個子類（AF越界→全路徑越界→形狀） | 品質 |
| K4-7 | ML Kit FAST mode + 全關品質特徵 = 圓形物件偵測器，不是人臉偵測器。品質 gate 必須在應用層補償 | K380 圓鍵 pareidolia — 偵測器回傳 confidence 但無五官驗證能力 | ML Kit |
| K4-8 | filter 條件新增時 MUST 考慮 rotation 維度，否則 rotation 修復 = regression 觸發器 | 缺陷 3：publishSanitizedResult vs observeCompositionAdvice 座標系不一致 | 座標系 |
| K4-9 | normalized 座標相除無幾何意義 — aspect ratio 等幾何量 MUST 在 pixel-space 計算 | normW/normH=1.33 誤判為 landscape，實為 pixel-space 57×57 正方形 | 數據 |
| K4-10 | CSO 裁決依賴的「殺手級數據」MUST 經獨立單位驗算 — 跨模型交叉驗算是必要防線 | GPT Adjudicator 攔截座標錯誤，防止門檻設錯 | 治理 |
| K4-11 | Pre-completed 票審查需錨定原始實作 commit + HEAD 驗證殘留，避免將後續 state-sync commit 誤判為實作 | Traycer F-1/F-2 方法論 | Pipeline |
| K4-12 | 視覺可見性是多因子耦合（alpha + 線寬 + Z-order + 繪製邊界），非單點調參 | F-2 根因：三因子擴展為四因子（contentBounds 第四因子） | UI |
| K4-13 | 命名格式規則必須隨 Phase 升級同步更新 — 「文件慣性」使規則停留在初始版本 | Rule 4 從 Phase 0 沿用至 P08 未更新，合規率 18% | 治理 |
| K4-14 | Phase 入口 Contract 必須 Traycer 重審（本次為正樣本） | Traycer ROI v2 改制 | Pipeline |
| K4-15 | `ssot_context.md` GOVERNANCE_DRIFT 發現：v4.0.2→v4.0.5 未同步 | S14 SSOT Alignment Errata | SSOT 紀律 |
| K4-16 | Guidance gate predicate 只能落在 consumer 層，不得落在 shared raw state | HOTFIX-47 / Portrait Contract v0.3 漏寫 | 架構（已收錄 K4-Rule 28） |
| K4-17 | 驗證腳本的檔案路徑必須與修訂單 Target file 欄位一致 | C-2 腳本路徑錯誤 / S12 Errata | Pipeline |
| K4-18 | FRAMING-class 的「全量清除」需跨 section sweep 確認 | CR-B3/B9 覆蓋 enum+table 但遺漏 package layout | 治理 |
| K4-19 | Bug 描述必須基於源碼審查而非推測 | PTF Halo 三重根因 vs 單一推測描述 | 治理 |
| K4-20 | 魔法數字與 SSOT 對比是有效的 drift 偵測手段 | 0.35 alpha 在 SSOT 不存在 | SSOT 紀律 |
| K4-21 | Scope Ban 措辭必須在行內包含例外條件，不可僅依賴表頭條件式閘門 | Codex 忽略表頭 HOTFIX-40 例外條款 | 治理 |
| K4-22 | `ssot_context.md` 行數等事實數據必須隨 Ticket 閉環自動更新 | CameraViewModel 行數 2457→2344 漂移未被偵測 | SSOT 紀律 |
| K4-23 | `execute_review` PASS ≠ DONE。碰 Camera pipeline MUST 真機驗收 | F4-FIX Traycer PASS 但真機全滅 | Camera 規則 |
| K4-24 | FIX MUST 逐項閉環 Discovery 前提缺陷 | FIX 扶正 Path A 但未修復根因 | Contract 紀律 |
| K4-25 | CSO 選擇扶正路徑時 MUST 含「所有 backend 驗證」前提確認 | CSO Contract 假設 Path A 接線即可用 | CSO 自我歸責 |
| K4-26 | Discovery→FIX 前提閉環規則（K4-24 擴充補強） | 同 K4-24，跨 session 反覆出現 | CLAUDE.md 候選 |
| K4-27 | 座標系選擇（`fromMediaImage` vs `fromBitmap`）必須與下游 `CoordinateTransformer` rotation 假設一致 | Issue #1 源碼審查發現橫向座標陷阱 | Camera 規則 |
| K4-28 | GuidanceState 定義必須基於 SSOT 六態，不得自行發明狀態分類 | Perplexity 提案 6 態與 SSOT §4.4.1 衝突 | SSOT 紀律 |
| K4-34 | 真機渲染缺陷無法被靜態/模擬器捕捉 — Halo 牽引圓點在 SSOT「加亮弧」空實作下，靜態+單元全綠但真機完全不渲染 → 視覺渲染類 AC MUST 真機 gate（K4-23 由「Camera 後端」擴展至「渲染」維度） | STATE-CONTRACT-01 AC-6/8 靜態 PASS 真機 FAIL（GAP-06） | Camera 規則 / UI |
| K4-35 | Guard 依賴的 input 若無 production producer（dead field），單元測試注入該 state 造成 false pass → reachability 維度 MUST 入 /qa+/sv | AC-10 假過 / afState 永 Idle / Guard 7a 死碼（GAP-07） | Contract 紀律（已轉 INFRA-REACH-01 票，本候選即其防護） |
| K4-46 | alpha／感知常數 MUST 由 Fable5 或實機閘定案；禁由黑底瀏覽器 demo 目視直寫 SSOT（黑底可辨 ≠ 實景可辨） | PROTO-02 報告 ①②③ 擬以瀏覽器目視抬 alpha，與 R-T9 C3「原型零視覺真理來源、唯實機能裁」證據法則衝突（R-T9×PROTO02 交叉驗測 2026-07-04） | 感知常數／治理 |
| K4-47 | 綠色 UI 引導元件於 Food 場景與綠色食材（薄荷、羅勒、抹茶類 garnish）同色域 → chromatic camouflage 失效；感知審查 MUST 檢 UI 色相 vs 場景常見食材色相衝突 | FABLE5-AUDIT-01 F-D 量測：State 6 最大綠色連通域實為薄荷葉裝飾非 UI（681px Weber+0.33） | 感知常數／產品 |
| K4-29 | 真機多 Bug 必須先做共因收斂再逐項開票 | S18 Bug #1~#5 共因分析 | Triage |
| K4-30 | 「近似對應」在真機上會震盪——governance 文件須要求 transition guard | Bug #3 前鏡頭閃爍 | 治理 |
| K4-31 | 一票吃三種責任 = scope creep；按驗收標準原子化切割 | UA-01 沿用 vs 三軌辯證 | 治理 |
| K4-32 | doc-only 票據須用 end-state inspection 替代 git diff review | PHASE-B-01 R4 Traycer | Pipeline |
| K4-33 | 主表修正後 summary/recommendation 必須 cross-section 回讀 | PHASE-B-01 RC2 split-brain | 治理 |
| K4-36 | AI「檔案」欄語義範圍須 ⊇「目標值」實際編輯範圍；禁在「檔案=A 節」AI 內用括號附帶「B 節」編輯 | AI-1「檔案:§2.5」括號夾帶「群組統一」(§2.5.2)→alpha 禁區歧義+AC 覆蓋缺口 | 治理 |
| K4-37 | 規則存廢裁決先問「rationale 是否存在」再分流（R-T9 vs R-T5）| R-T9 分類 | 治理 |
| K4-38 | gitignored 票檔的治理修訂（AC/禁區/常數）必須同時具備 (a) 檔內 R-N 修訂註記 + (b) Obsidian 歸檔，因 git 無軌可稽 | FIX-A R-2 初僅檔內註記無 Obsidian（本檔即補齊）| 治理 |
| K4-39 | fossil-scan / 值掃描 gate 禁用 context-gated 同行式（keyword.*value），因值與關鍵字常分屬兩行→false-PASS；須用區段錨定 + 具名排除 | R-T10 負向探針（else 0.25f 漏網） | Pipeline |
| K4-40 | 機械掃描票 Error Handling 禁寫「任何未列舉命中→停止」（與 AC completeness 先天矛盾）；須寫同類併入/異類停止 | R-T11 L120 | Pipeline |
| K4-41 | apply-and-report vs hold-and-report 分界 = 是否需超出 pattern-同一性的判斷 | R-T11 | 治理 |
| K4-42 | 黑底 demo=視覺常數的 dual-backend trap：demo 高對比環境驗證的感知常數對實景是弱證據（等同 static test vs real device）；感知常數 AC 須聲明驗證環境，demo-only 不得封板 | 0.10→0.18 黑底可辨、實景外推失效 | UI |
| K4-43 | downstream sweep 須涵蓋「相對被改錨點校準的衍生值」：改絕對錨時所有以舊錨為基準的相對值/層級比逐一重推導 | R-T2 sweep 漏掃對齊態→S25 同日兩裁決矛盾潛伏 | 依賴鏈 |
| K4-44 | ticket AI 目標檔案必須「已驗證存在」或明確標記「新建」；禁用「依現行 X 位置」類未驗證指涉 | AI-7（R-T14）+ GOV-01 Error Handling（R-T11）兩次 CSO 起草缺陷 | 治理 |
| K4-45 | 票號指派前必須查已執行/已通過票 registry（PROGRESS + reports/），不能只看 Pending/——通過票離開 Pending/，只掃 Pending/ 必然碰撞 | PROTO-01 撞號（新票與已通過票同號） | 治理 |

---

## 品類分群

### A. Pipeline / 工具鏈（K4-1, K4-3, K4-11, K4-14, K4-17）

共同主題：Pipeline 工具的機械行為需要被正確理解和利用。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-1 | ⚠️ 延後 | Pipeline v1.7 升級時考慮 | 自動歸檔 DIFF_REF 需 pipeline 改造，非規則可解 |
| K4-3 | ✅ 已落地（KAIROS-BACKFILL-01）| `.claude/rules/cross-review.md`「Scope Ban BLOCKER 處置 + Payload 一致性」區段 | 比照 K5-Rule 29 雙層驗證原則補寫；併入 K3-6（cross_review.sh payload SV scope↔diff scope 一致性） |
| K4-11 | ⚠️ 延後 | Traycer SKILL 或 KB | 適用面窄（僅 pre-completed 票），不值得 CLAUDE.md 行 |
| K4-14 | ⚠️ 待 KAIROS #4 裁決 | Pipeline SOP | Phase 入口 Contract Traycer 重審流程化 |
| K4-17 | ⚠️ 待 KAIROS #4 裁決 | `/sv` 驗收 SOP | 驗證腳本路徑 ↔ Target file 一致性可加入 /sv checklist |

### B. 治理 / Scope Ban（K4-2, K4-10, K4-13, K4-18, K4-19, K4-21）

共同主題：治理文件需主動維護，不能「寫完即凍結」。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-2 | ✅ 已執行 | HOTFIX-40（ssot_context.md 例外條款） | 已轉為具體修補票，不需再收規則 |
| K4-10 | ✅ 收錄 | CSO KB v3 或 CLAUDE.md | 跨模型驗算防線 — 高頻適用（每次 CSO 基於數據裁決時） |
| K4-13 | ✅ 已執行 | INFRA-21 AI-5（Rule 4 格式更新） | 已轉為具體修補，不需再收規則 |
| K4-18 | ⚠️ 待 KAIROS #4 裁決 | `/sv` gate 擴充 | 全量清除的 grep 覆蓋率檢查可系統化 |
| K4-19 | ⚠️ 待 KAIROS #4 裁決 | CLAUDE.md 或 PROGRESS 規範 | Bug 描述品質門：源碼確認 vs 推測需標示 |
| K4-21 | ✅ 已收錄 | `PROGRESS.md` D13 KAIROS #4 候選議程 | Scope Ban 行內例外條件規則（rules/scope-ban-governance.md） |

### C. 依賴鏈 / 架構（K4-4, K4-5, K4-16）

共同主題：修改一個屬性時需理解全部下游依賴。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-4 | ⚠️ 合併 | 與 K3-Rule 26 合併 | 本質是 Rule 26（多消費路徑掃描）的子案例：「屬性語意變更 = 隱性 API 變更」 |
| K4-5 | ✅ 收錄 | CSO KB v3 | 戰略層教訓（修 A 前確認 A 是否為 B 的解藥），適合 KB 不適合 CLAUDE.md |
| K4-16 | ✅ 已收錄 | `.claude/rules/kairos-lessons.md` K4-Rule 28 | Guidance gate 架構邊界已成文（HOTFIX-47 閉環） |

### D. Ghost Face / ML Kit（K4-6, K4-7, K4-8）

共同主題：ML Kit FAST mode 的品質缺口需系統性應對。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-6 | ✅ 已落地（KAIROS-BACKFILL-01）| `.claude/rules/kairos-lessons.md` K3-Rule 25「Bug 品類覆蓋」子彈點 | 補寫完成，非新增規則號，append 至既有 Rule 25 |
| K4-7 | ⚠️ 延後 | CSO KB v3 | 知識性（ML Kit 配置語意），非行為規則 |
| K4-8 | ✅ 已落地（KAIROS-BACKFILL-01）| `.claude/rules/camera-constraints.md` Rule 30 | Filter Rotation 維度規則已成文 |

### E. 數據完整性（K4-9）

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-9 | ✅ 已落地（KAIROS-BACKFILL-01）| `.claude/rules/camera-constraints.md` Rule 31 | 幾何量 Pixel-Space 計算規則已成文 |

### F. UI（K4-12）

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-12 | ⚠️ 延後 | CSO KB v3 | 知識性觀察，非行為規則 |

### G. SSOT 紀律（K4-15, K4-20, K4-22, K4-28）

共同主題：SSOT 是唯一真理來源，靜態數字與狀態定義均需動態對齊。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-15 | ⚠️ 待 KAIROS #4 裁決 | SSOT 治理 SOP | GOVERNANCE_DRIFT 偵測機制可系統化 |
| K4-20 | ⚠️ 待 KAIROS #4 裁決 | `/sv` gate 擴充 | 魔法數字來源追溯可加入 /sv checklist |
| K4-22 | ✅ 已收錄 | `PROGRESS.md` D13 KAIROS #4 候選議程 | ssot_context.md 行數自動更新機制 |
| K4-28 | ⚠️ 待 KAIROS #4 裁決 | SSOT §4.4.1 治理 | GuidanceState 六態必須以 SSOT 為基底，任何 State 提案需先校驗 |

### H. Camera Pipeline / Contract 紀律（K4-23, K4-24, K4-25, K4-26, K4-27）

共同主題：Camera 硬體路徑的驗收不可依賴靜態工具，Discovery 與 FIX 需形成閉環。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-23 | ✅ 已收錄 | `PROGRESS.md` D13 KAIROS #4 / `.claude/rules/camera-constraints.md` | execute_review PASS ≠ DONE（Camera pipeline 真機驗收強制） |
| K4-24 | ✅ 已收錄 | `PROGRESS.md` D13 KAIROS #4 | FIX 逐項閉環 Discovery 前提缺陷 |
| K4-25 | ⚠️ 待 KAIROS #4 裁決 | CSO 行為規範 | CSO 扶正路徑選擇必須含 backend 驗證前提 |
| K4-26 | ⚠️ 待 KAIROS #4 裁決 | CLAUDE.md 候選（合併 K4-24） | K4-24 的 session 跨度補強，可合併為單一規則 |
| K4-27 | ✅ 已落地（KAIROS-BACKFILL-01 併同補寫）| `.claude/rules/camera-constraints.md` Rule 32 | fromMediaImage vs fromBitmap 座標系選擇必須與 CoordinateTransformer 一致；同族收束 Camera 座標系風險群（含 GAP-07 前置認知） |

### I. 渲染 / Reachability 驗收（K4-34, K4-35，STATE-CONTRACT-01/HOTFIX-47 後續，S28 併入）

共同主題：靜態工具（單元測試/模擬器）對真機渲染缺陷與 dead-field 假通過無感知，驗收需擴充可達性維度。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-34 | ⚠️ 待 KAIROS #4 裁決 | `.claude/rules/camera-constraints.md`（K4-23 擴展） | 視覺渲染類 AC 真機 gate 範圍需明確延伸至「渲染」維度，非僅 Camera 後端。〔併入 K3-3：KAIROS 節點 real-device screenshot 要求由本項統一承載〕 |
| K4-35 | ⚠️ 待 KAIROS #4 裁決 | `/sv` + `/qa` gate 擴充 | Guard input reachability 已轉具體修補票 INFRA-REACH-01，本候選是否需額外收錄為通用規則待裁決 |

### J. 真機 Bug Triage / Scope（K4-29, K4-30, K4-31，S18/UA-01 系列，INFRA-XX 第二批併入）

共同主題：真機多缺陷需先做共因收斂，避免逐項開票造成 scope creep。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-29 | ⚠️ 待 KAIROS #4 裁決 | 開票 SOP | 多 Bug 共因收斂流程化，避免逐項開票放大工作量 |
| K4-30 | ⚠️ 待 KAIROS #4 裁決 | `.claude/rules/state-machine.md` | 「近似對應」需 transition guard，防止真機震盪（與 Rule 22 mode-entry cooldown 同族） |
| K4-31 | ⚠️ 待 KAIROS #4 裁決 | CLAUDE.md Rule 4a 擴充 | 一票吃三種責任視為 scope creep，需按驗收標準原子化切割 |

### K. Ticket 起草治理 / AI 欄位語義（K4-32, K4-33, K4-36, K4-44, K4-45，PHASE-B-01/R-T14 系列，INFRA-XX 第二批併入）

共同主題：Ticket 起草與審查流程本身的語義完整性需要規則約束，防止 CSO 自身起草缺陷。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-32 | ⚠️ 待 KAIROS #4 裁決 | Traycer 審查 SOP | doc-only 票據需 end-state inspection 取代 git diff review |
| K4-33 | ⚠️ 待 KAIROS #4 裁決 | Review SOP | 主表修正後 summary/recommendation 需 cross-section 回讀，防 split-brain |
| K4-36 | ⚠️ 待 KAIROS #4 裁決 | 起草 checklist（ticket lint） | AI「檔案」欄語義範圍需 ⊇ 目標值實際編輯範圍 |
| K4-44 | ⚠️ 待 KAIROS #4 裁決 | 起草 checklist（ticket lint） | AI 目標檔案需已驗證存在或明確標記新建 |
| K4-45 | ⚠️ 待 KAIROS #4 裁決 | 開票 SOP | 票號指派前需查 registry（PROGRESS + reports/），不能只掃 Pending/ |

### L. Pipeline Gate 撰寫紀律（K4-37, K4-38, K4-39, K4-40, K4-41，R-T9~R-T11 系列，INFRA-XX 第二批併入）

共同主題：機械掃描 gate 與規則存廢裁決的撰寫方式本身需符合防漏網、防矛盾的紀律。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-37 | ⚠️ 待 KAIROS #4 裁決 | 規則存廢 SOP | 存廢裁決先問 rationale 是否存在再分流 |
| K4-38 | ⚠️ 待 KAIROS #4 裁決 | 票檔修訂 checklist | gitignored 票檔治理修訂需同時具備檔內 R-N 註記 + Obsidian 歸檔 |
| K4-39 | ⚠️ 待 KAIROS #4 裁決 | `/sv` gate 撰寫指引 | fossil-scan 禁用 context-gated 同行式，須用區段錨定 + 具名排除 |
| K4-40 | ⚠️ 待 KAIROS #4 裁決 | ticket 起草 lint | 機械掃描票 Error Handling 禁寫「任何未列舉命中→停止」 |
| K4-41 | ⚠️ 待 KAIROS #4 裁決 | CLAUDE.md 候選 | apply-and-report vs hold-and-report 分界規則化 |

### M. 感知常數 / Sweep 完整性（K4-42, K4-43，R-T2 系列，INFRA-XX 第二批併入）

共同主題：感知常數與衍生值需明確驗證環境與完整重推導範圍。

| ID | 收錄建議 | 目標位置 | 理由 |
|----|---------|---------|------|
| K4-42 | ⚠️ 待 KAIROS #4 裁決 | AC 撰寫規範（環境聲明欄） | demo 高對比環境驗證的感知常數對實景是弱證據，demo-only 不得封板 |
| K4-43 | ⚠️ 待 KAIROS #4 裁決 | sweep checklist | downstream sweep 須涵蓋相對被改錨點校準的衍生值重推導 |

---

## 收錄決策摘要

> **S28 稽核與補寫紀錄**：原「✅ 收錄為規則」5 項中，K4-16 於 INFRA-XX 稽核（`kairos-lessons.md` K4-Rule 28 條文核實存在）確認已落地；K4-3、K4-6、K4-8、K4-9 經 KAIROS-BACKFILL-01（依上游決策 #118）補寫落地（`cross-review.md` 新增區段 + `kairos-lessons.md` K3-Rule 25 append + `camera-constraints.md` Rule 30/31），K4-27 同族併同補寫（Rule 32）。K4-10 目標為 repo 外部 CSO KB，本 repo 無法核實，列「⚠️ 無法稽核」。

| 處置 | IDs | 數量 |
|------|-----|------|
| ✅ 收錄為規則（稽核通過） | K4-16（→ kairos-lessons.md K4-Rule 28） | 1 |
| ✅ 已落地（KAIROS-BACKFILL-01 補寫） | K4-3（→ cross-review.md）, K4-6（→ kairos-lessons.md K3-Rule 25）, K4-8（→ camera-constraints.md Rule 30）, K4-9（→ camera-constraints.md Rule 31）, K4-27（→ camera-constraints.md Rule 32） | 5 |
| ⚠️ 無法稽核（目標為 repo 外部 CSO KB） | K4-5, K4-7, K4-10, K4-12 | 4 |
| ✅ 已轉為修補票 | K4-2（HOTFIX-40）, K4-13（INFRA-21 AI-5） | 2 |
| ✅ 已收錄（PROGRESS 候選議程原文併入本表） | K4-21, K4-22, K4-23, K4-24 | 4 |
| ⚠️ 合併至既有規則 | K4-4（→ Rule 26 擴展） | 1 |
| ⚠️ 延後（需 pipeline 改造） | K4-1, K4-11 | 2 |
| ⏳ 待 KAIROS #4 裁決 | K4-14, K4-15, K4-17, K4-18, K4-19, K4-20, K4-25, K4-26, K4-28, K4-29, K4-30, K4-31, K4-32, K4-33, K4-34, K4-35, K4-36, K4-37, K4-38, K4-39, K4-40, K4-41, K4-42, K4-43, K4-44, K4-45, K4-46, K4-47, K4-48, K4-49, K4-50 | 31 |

---

## 建議收錄的規則草稿

### Rule 28 候選（cross-review.md）

```text
遇 Scope Ban BLOCKER + 有 Contract 明文授權 → 直接 Option D（CSO 誤判申訴），
不走 Option A（sv_report 補文字）。Layer 3.4 不讀推理文字，歷史實證兩次確認。
```

### Rule 25 擴展（kairos-lessons.md）

```text
Rule 25 追加子彈點：
- Bug 品類覆蓋：修復 scope 必須覆蓋品類邊界而非已知實例。
  反例：Ghost Face 三次壁癌（AF越界→全路徑越界→形狀），每次只堵一個子類。
```

### Rule 29 候選（camera-constraints.md）

```text
filter 條件新增時 MUST 考慮 rotation 維度。
rotation=0 下正確的 filter 在 rotation=90/270 時可能誤殺或漏放。
```

### Rule 30 候選（camera-constraints.md）

```text
bbox 長寬比等幾何量 MUST 在 pixel-space 計算。
normalized 座標對不同分母（width vs height）的除法無幾何意義。
```

### Rule 31 候選（camera-constraints.md，K4-27）

```text
座標系選擇 (`fromMediaImage` vs `fromBitmap`) MUST 與下游
CoordinateTransformer 的 rotation 補償假設一致。
橫向拍攝時 HAL rotation=0，fromMediaImage 不旋轉但 fromBitmap 已含旋轉 →
兩者混用導致 ±90° 座標偏移。
```

### CSO KB v3 追加

```text
CSO 裁決依賴的「殺手級數據」MUST 經獨立驗算（跨模型或人工）。
一個座標單位誤讀差點設錯整個過濾門檻。GPT Adjudicator 角色確認有效。
```

### K4-48 候選（cross-review.md，K-c，extends K4-39）
<!-- 遷號註記：原誤植 K4-46（2026-07-05 CSO tail 局部讀取取號），與表列 K4-46（Fable5 感知常數，07-04）撞號；2026-07-05 遷 K4-48 修復。撞號事件本身收割為 K4-49。 -->

```text
票面 mandate changelog / 重錨註記時，對應 fossil-scan AC MUST 在開票時
就把該敘述行登記為「具名排除白名單」，禁用 context-gated 語意排除
（「歷史語境除外」）。否則 cross-family reviewer 的 context-blind grep
必然命中票面強制產物 → 定義性 false-positive。
事件支撐：TRACTION-SSOT-04 R1 Codex substance review，S-2 grep
(0\.10.*0\.18) 命中 L481——即票面 AI-2 強制之 D-3 重錨敘述行
（「由 0.10→0.18 重錨為 0.25→0.40」）→ 誤判 fossil → R-T18 裁定
L481 升具名排除（比照 subjectRatio）。
防護：開票 SOP 增列——凡 AC 含 fossil-scan 且票面另要求「附註記舊值 /
重錨敘述」→ 該行同票登記具名排除（延伸 K4-39：區段錨定 + 具名排除）。
```

### K4-49 候選（registry 治理，K-d，extends K4-45）

```text
KAIROS / R-T 等序號登記 MUST 以全檔 grep max-ID 機械取號
（grep -oE "K4-[0-9]+" 全檔 → max+1），禁用 tail / 局部讀取驗證
「目前最大號」。多 agent 並行寫入下，局部讀取必然漏看他處已登記
條目 → 撞號。
事件支撐：2026-07-05 CSO 以 tail 45 行驗證「現至 K4-45」後將 K-c
登記為 K4-46，漏看檔中段表格既有 K4-46（Fable5 感知常數，07-04）/
K4-47（chromatic camouflage）→ 雙 K4-46 撞號——K4-45 教訓在教訓
登記簿上自我重演（本次遷號 K4-48 修復）。
防護：取號 SOP 機械化；R-T 序號同理（R-T18 已有 stamp 前對帳 AC
前例）；本檔各計數行（總項數/待裁決數）隨登記同步校正。
```

### K4-50 候選（AC 語義折損律，K-e，extends K4-46/K4-48）

```text
AC 自然語言語義（「落地」「生效」「可達」）在機械驗收拆解時必然折損為
存在性檢查，除非開票時以 AC 類型顯式編碼（存在性/可達性/行為性/實機）
＋producer 證據欄，否則審查階段語意補撈（如 B-7 類 Guard Reachability
語意豁免）只是次優防線——審查器 context-blind，豁免走語意裁量，無法
保證一致機械執行。
事件支撐：INFRA-REACH-01 AC-10 四層防線（unit test / static scan /
sv B-7 / cross-review）全數綠燈，仍放行一個 production 無 producer
的 dead field（afState）—— 四層共享隱含公理「存在＋綠燈＝落地」。
防護：Unified Ticket Template v3.1 於開票層強制 AC 類型標記
（INFRA-REACH-02 AI-1）；Guard/gate 類 AC 之 UNREACHABLE 判定於
/sv 審查層機械分級為 REPORT_FIX（AI-3）；Cross-Review 增列
REACHABILITY: 機械 token（AI-4）；`[可達性]`/`[行為性]` AC 強制
integration test 存在閘（AI-5）——四點合圍將防線由「審查層語意補撈」
前移至「開票層顯式編碼＋機械驗收」。
```

---

### K4-51 候選（錨移全檔同值 sweep 律，K-f，extends K4-44）

```text
數值錨移裁決（如 R-T2 alpha 0.25→0.8）之 downstream sweep 若僅掃代碼
區/常數定義區，狀態表、敘述性條文中的同值殘留即成化石；化石對下游審
查者呈現為合法 SSOT 錨點，誤引後產出之推導（關係式/敘事）與 live 真
理矛盾，且引用者無從察覺——化石殺傷力不在殘留本身，在其「可被引用性」。
事件支撐：R-T2 錨移漏掃 §4.4.1 L901（GUIDANCE_ACTIVE | Halo 琥珀
α0.25；同表 ALIGNED 列已隨 R-T13 更新，唯此列失修）→ UITA 鎖定態數
值定稿引 L901 為前提，導出「LOCKED α0.35 > 付費琥珀 α0.25」關係式，
方向與 live 真理（0.8）完全顛倒，幾乎入檔 v4.0.7（CSO 終審 grep 對決
攔截，2026-07-07，見 CSO_Verdict_UITA_LockedHalo_Numeric_20260707）。
防護：數值錨移類裁決之 downstream sweep 清單 MUST 由「全檔舊值 grep」
產生（含狀態表/敘述區/註解），非僅代碼與常數區；sweep 勾除以 grep 舊
值零命中為機械驗收，禁人工列舉。
```

### K4-52 候選（UITA 版本一致性斷言，LH-VAC-02 K-a）

```text
審計方自身版本漂移：審計基準檔版本落後 INDEX 宣告版本，全 session 在
過期副本上審計。
事件支撐：Handoff 載明 SSOT v4.0.6，UITA_INDEX v1.2 已宣告 v4.0.7；
UITA 依 Handoff 拉檔未比對 INDEX，於 v4.0.6 狀態表化石列取值——該列正
是 v4.0.7 AI-7 sweep 修正對象（現 L954），致 vaccine v0.1 倒置關係式
（LH-VAC-02 翻案源）。
防護：Session 開場 Checklist 增加斷言「工作區副本版本 == 角色 INDEX
宣告版本」，不符即重拉方可開審；落點 UITA_Session_Startup_SOP，CSO/
TRA 開場同構適用。
```

### K4-53 候選（渲染參數權威鏈，LH-VAC-02 K-b，extends K4-51）

```text
渲染參數權威鏈：§2.5 live 代碼塊 > 下游狀態表（§4/§8 為投影層）；引
用投影層取值即引用二手快照。K4-51 為 producer 側（sweep 完整性），本
條為 consumer 側（引用紀律）——雙側防護同一化石病。
事件支撐：vaccine v0.1 前提「付費琥珀 α0.25」取自狀態表投影，未對
§2.5 live 塊（targetValue = if (isAligned) 0.40f else 0.8f，現 L399）
驗證，致關係式方向顛倒。
防護：vaccine/redline 任何渲染參數引用一律以 §2.5 live 代碼塊字串錨
為源；引用投影層須附「已對 live 塊驗證」標記。
```

### K4-54 候選（關係式優先於裸數字，LH-VAC-02 K-c，extends K4-43/K4-51）

```text
關係式優先於裸數字：跨條目引用的數值一旦寫成裸數字快照，錨移日即化石
誕生日；應寫成對 live 具名常數的相對關係，數字僅作當前代入值註記。與
「字串錨點優先於行號」同構（引用會漂移的快照 → 改引穩定的關係）。
事件支撐：v0.1 關係式「LOCKED > 付費琥珀」以裸數字 0.25 為前提，R-T2
錨移（0.25→0.8）後入檔首日即違反；v0.2 改 C1-C4 相對式（LH-VAC-02，
CSO 裁示附修正條件 A：主詞常數明標定義處、關係式右項綁具名常數或 live
塊字串錨），未來錨移由關係式自動裁決違約與否。
防護：SSOT 條目間數值引用強制以關係式表達（<、≥、= 某具名常數）；裸
數字僅允許出現於常數定義處與「現值代入」括註。
```

### K4-55 候選（送審即同步律，Traycer R1 STATE-CONTRACT-03，extends K4-δ/K4-50）

```text
送審即同步律：[實機] AC 一旦在送審包宣稱 PASS，PROGRESS.md 與所有被引用
證據路徑 MUST 在同一 closure commit 內同步至終態；送審文件引用路徑一
律全路徑，禁 session 縮寫。禁止「報告已過、ledger 仍 pending、附件路
徑解析落空」三真理並存。
事件支撐：D13 STATE-CONTRACT-03 Traycer R1 判 Path B，非 code defect
而是 closure defect：PROGRESS 停在煙測 pending，且送審包用 `reports/`
縮寫（實指 plans/cso_briefing/reports/）致 Traycer 字面解析落空、存在
之證據檔被判缺失（假陰性）。
防護：送審前 checklist — ①ledger 終態同步 ②引用路徑全路徑化且逐一
get_file_info 存在性驗證 ③事實記帳（煙測 PASS）與閉環宣告（關票）措
辭分離。
範圍：commit 同步義務僅及 tracked ledger（如 PROGRESS.md）；untracked
治理域證據檔（.gitignore 政策排除，如 plans/cso_briefing/）改以存在性
驗證（get_file_info）+ 全路徑引用替代。
來源：Traycer R1 candidate rule + CSO 追加全路徑條款 + 範圍條款修訂
（gitignore 衝突裁決 A2，2026-07-10）。
```

### K4-56 候選（共享視覺常數家族 sweep-or-register 律，正式收編 K4-η，extends K4-51/K4-54）

```text
共享視覺常數家族 sweep-or-register 律：任何觸碰共享視覺常數（顏色/
alpha/半徑/座標比例）家族之票，開工前 MUST 對該數值字面量執行雙法
grep 全庫掃描（K4-ε）；清單外命中 MUST 二選一分流——sweep-in（納入
本票 ALLOWED 一併重錨）或 register-out（STOP 回 CSO 裁域外，登記
Postponed + 翻盤條件）。禁止「視而不見放行」。
事件支撐：SC-04 首次觸碰 alpha family，K4-η sweep 攔截清單外命中 ×2
（HaloGuidanceLayer.kt:96-97 PTF_BREATH、CameraScreen.kt:580-581 快門
環）→ CSO 域外裁決 Postponed #7/#8；SC-05 觸碰 radius family 重演同構
分流（AI-3 白名單機制）。二次同構已達收編門檻（CLAUDE.md Rule 4a
family provenance 精神：模式重複 ≥2 次即收編，勿待第三次）。
防護：K4-51 錨移全檔同值 sweep 律之視覺常數特化版；未來任何常數家族
單次觸碰票 MUST 內建此 sweep-or-register 步驟為 Action Item，不得省略。
```

### K4-57 候選（雙真理源消費者掃描完備性律，正式收編 K4-ζ，extends K3-26）

```text
雙真理源消費者掃描完備性律：任何新建/取代「真理源」（single source of
truth）之票，開工前除 K3-26（消費者是否在 ALLOWED 範圍）外，MUST 額外
判定「新真理源是否完整取代舊真理源，或兩者將長期並存」；若判定並存，
MUST 在 PROGRESS.md Active Debt 或 SSOT 明確登記雙真理源邊界與遷移
路徑，禁止留待後續票意外撞見。
事件支撐：STATE-CONTRACT-01 落地 GuidanceState 六態契約時未同步掃描
legacy `compositionResult.isAllAligned` 消費鏈，致專案並存兩套 alignment
真理源；SC-04 K4-η sweep 才第二次揭露此裂縫（CameraScreen 快門環），
現以 SC02-5 Amendment A1 + Postponed #8 追蹤遷移。
防護：K3-26 之特化——K3-26 掃描「消費者是否漏掃」，K4-ζ 掃描「是否存
在並行真理源之結構性風險」，兩者互補、缺一不可。
```

### K4-58 候選（雙法複證律，正式收編 K4-ε，extends 靜態掃描既有紀律）

```text
雙法複證律：任何「零命中」宣稱（如常數字面量掃描、消費鏈可達性掃描）
之驗收，MUST 以第二種獨立語法（不同 grep pattern / 不同工具）複證，
單一語法零命中不得直接採信為證據。
事件支撐：SC-05 AI-3 K4-η 消費點對沖明訂「零命中須第二獨立語法複
證」；SC-04/SC-05 兩票之 K4-η sweep 附件（雙法 grep 全文）已依此紀律
產出並隨送審包附呈。
防護：/sv、/qa 之靜態掃描步驟涉及「零命中即 PASS」判準時，MUST 標註
是否已雙法複證；未複證之零命中僅可標 ADVISORY，不得逕列 PASS。
```

### K4-59 候選（預簽決策樹保底出口律，extends K4-η STOP 案例）

```text
預簽決策樹保底出口律：高頻重演之域外裁決模式（≥2 次同構命中），CSO
MAY 預先簽核「決策樹保底出口」條款（觸發特徵 + 自動判定 + 翻盤條
件），登記於 rules/ 或 Obsidian ruling index；後續同構命中援引條款
即可直接落 Postponed，無需逐案回 CSO 走 Checkpoint 往返。
事件支撐：SC-04 STOP 案兩次域外命中（PTF_BREATH、快門環）皆走人工
STOP→CSO 逐案裁決循環，各耗一輪 Checkpoint；若預簽，SC-05 同構命中
可省略往返。
範圍：僅適用「域外零觸碰」此類低風險、高頻重演模式；涉核心邏輯或
CLAUDE.md/SSOT 之 Blocker 仍 MUST 逐案 Checkpoint，不得預簽繞過。
```

### K4-60 候選（gate-integrity vs adjacent-scope blocker 分類律）

```text
gate-integrity vs adjacent-scope blocker 分類律：Traycer/CSO 收到
Blocker 時 MUST 先分類：①gate-integrity blocker（本票 diff 直接導
致 AC 失敗或回歸）→ 本票內修復；②adjacent-scope blocker（既存但與
本票 diff 無因果、超出本票 ALLOWED 邊界）→ MUST spawn 獨立 Ticket，
本票以「已發現並登記」滿足揭露義務，不得要求本票擴大 scope 修復。
分類依據：修復是否需碰觸本票 FORBIDDEN 或 ALLOWED 外檔案。
事件支撐：Traycer SC-04 R1 Blocker 1（`HALO_RADIUS_SCALE=0.6f` vs
SSOT `0.5f`）正確分類為 adjacent-scope（非 SC-04 diff 造成、非 SC-04
AC 範圍）→ spawn STATE-CONTRACT-05 獨立票，未在 SC-04 內強行擴大修復。
```

---

## KAIROS #4 執行時機

**觸發條件**：Phase 0.8 Visual Baseline Gate PASS 後首個 KAIROS 節點（D13）

**輸入**：本文件（50 項）+ CSO KB v3 + CLAUDE.md 現行規則

**產出**：CLAUDE.md 規則更新 + path-scoped rules 更新 + KB v3.1

**約束**：CLAUDE.md 維持 ≤150 行 / 12 核心規則（新規則走 path-scoped）

**優先裁決清單**（⏳ 32 項，依「影響範圍 × 已發生頻率」排序，INFRA-XX 第二批併入 S28 排序；KAIROS-BACKFILL-01 後 K4-27 已落地移出）：

| 優先層 | IDs | 排序依據 |
|--------|-----|---------|
| P0（與現行 P0 blocker 同構或已二次以上重複發生） | K4-39, K4-44, K4-45, K4-48, K4-49, K4-50 | K4-48=K-c（判準機械化，extends K4-39）；K4-49=取號機械化（extends K4-45，撞號二次實證：PROTO-01 + 本檔 K4-46 雙登）；K4-50=AC 語義折損律（extends K4-46/48，事件支撐 AC-10 四層全綠假過，INFRA-REACH-02 已落地四點防護）；K4-39 已有 R-T10 負向探針實證；K4-44 為兩次 CSO 起草缺陷（AI-7+GOV-01）；K4-45 已實際撞號（PROTO-01）。K4-27 原列本層，已於 KAIROS-BACKFILL-01 補寫落地（camera-constraints.md Rule 32），移出待裁決清單 |
| P1（機制性防護，高頻適用於日常 Pipeline/治理操作） | K4-38, K4-40, K4-41, K4-36, K4-31, K4-33, K4-19, K4-17, K4-18, K4-51 | 涉及 ticket 起草/審查/gate 撰寫的日常高頻路徑，一旦缺失即反覆觸發同類缺陷 |
| P2（架構/知識性，中頻但有明確觸發場景） | K4-15, K4-20, K4-25, K4-26, K4-28, K4-30, K4-32, K4-37, K4-42, K4-43 | SSOT drift／狀態機／感知常數等場景性強，觸發頻率低於 P1 但影響面不小 |
| P3（延後或知識庫向，適用面窄） | K4-14, K4-29, K4-34, K4-35 | K4-14 適用面窄（僅 Phase 入口 Contract）；K4-29 為流程建議非硬規則；K4-34/35 已各自轉入具體修補票（渲染 gate / INFRA-REACH-01），本候選僅評估是否需額外通用化 |

**補寫清單狀態**：已清空。歷史待補寫項已轉入上方「收錄決策摘要」✅ 已落地（KAIROS-BACKFILL-01 補寫）列。


---

## 追加候選（2026-07-14 · GAP-06 否證案 + GAP-11 簽發案收割 · 暫定 ID 待 KAIROS 節點全域消歧取號〔K4-49 防撞〕）

### K4-cand-20260714-01：Ledger 鏈路血統律
```
PROVISIONAL Ledger（及一切治理帳本）記載 producer/consumer 鏈路時，
MUST 附呼叫者血統證據（`檔案:行號` + 呼叫者存在確認），
無證據之鏈路 MUST 顯式標記 `[推定]`。
開票引用 Ledger 鏈路描述時，`[推定]` 項 MUST 先偵察後起草 AI。
事件支撐：GAP-06-TARGET Ledger 條目之 producer 描述
（CompositionAdvice.Adjust → guidance 管線）為推定入帳——
唯一 producer CompositionGuidelineEngine 係 v2.13.0 孤兒（零呼叫者），
化石寄生治理帳本，經 CSO 開票二次放大，AI-1 偵察方否證。
性質：教訓 #3（僥倖裁決/推定前提）升級型——帳本層級。
附帶實證：Error Handling 保底出口第三次實戰有效（執行端零改碼正確 STOP）。
```

### K4-cand-20260714-02：SSOT 覆蓋率盲區（已規格未實作偵測）
```
SSOT 條款落地狀態缺乏機械追蹤——「已規格未實作」缺口僅靠實測撞見。
二例成形：①Food Halo 三層斷鏈（SSOT L134/L259/§2.5.1，D15-16 §2）
②Portrait EMA 0.7/0.3（SSOT :536-541，D17 診斷，PORTRAIT_EMA 全專案零命中）。
防護候選：Sprint 邊界排 SSOT-to-code coverage 掃描票
（SSOT 常數/公式條款 → grep 對應實作，產出缺口清單入 Ledger）。
窗 A（Fable5 預算窗）內不執行，Sprint 邊界裁決。
```


---

## 追加候選（2026-07-14 · GAP-11 審查鏈收割 · 暫定 ID 待正式取號〔K4-49 防撞〕· cand-05 經 Akira 裁示撤銷不錄）

### K4-cand-20260714-03：兩閘獨立律（ALLOWED ≠ Scope Ban 豁免）
```
票面 ALLOWED（scope gate）與 ssot_context Scope Ban（結構 gate）為兩道獨立閘門，
通過前者不構成後者豁免。CSO 修補條款指名實作落點時 MUST 對照 Scope Ban 清單。
事件支撐：GAP-11 A1 指名 companion object 落 CameraViewModel → Codex R1 BLOCKER
（禁止淨新增函數，例外僅 1-for-1 replacement）。extends K4-44 CSO 起草缺陷家族。
```

### K4-cand-20260714-04：constructor 呼叫點窮舉律
```
票面起草觸及類別 constructor 簽名時，MUST grep 全樹直接建構呼叫點納入 ALLOWED 預算。
事件支撐：GAP-11 同根二發——A1（AC-2 測試檔漏列）+ A3（CameraViewModelTestGazeAfWiring
直接建構呼叫編譯連帶，票面未列）。extends K4-44 家族。
```

### K4-cand-20260714-06：閘門對撞升層律
```
兩機械閘門對撞且無碼級合法出路時，解決層級 MUST 上移規則層：
誠實修規則（目標態 vs 現況態分離）+ 最小豁免白名單（恰列名品類，禁 grandfather 全豁免）
+ 債務登記入 Postponed。禁碼級硬拗、禁報告補字（K4-3）、禁「誤判申訴」框架
（審查方字面正確時，申訴 = 語義壓機械）。
事件支撐：GAP-11 R2——A2 指定落點（逃 Scope Ban）vs ssot_context §1 domain 純度
正面對撞；A 路觸 K4-Rule 28 shared face model 紅線，B 路以新矛盾解舊矛盾；
裁決以 §1 現況化修訂 + RectF/PointF+Log 恰兩品類白名單 + INFRA-DOMAIN-PURITY-01
債務登記收束（2026-07-14）。
```

### K4-cand-20260719-01：全域 Scratch 覆寫致回執斷鏈律（呼應 concurrency.md §1，R1/R2 案例第三例）
```
非 per-ticket 隔離之全域 scratch 檔案（`sv_report.md`、`.tmp/cross_review_result_*.md`）
在跨票/跨 session 連續執行下，會被後續 Ticket 的 SV 執行覆寫、或依「最近 N 筆」保留制
輪替出局，致「Codex PASS 回執三件套（verdict/DIFF_SCOPE/receipt 路徑）」中的 receipt
路徑本體不可回溯——即使三件套已依裁決要求寫入治理記錄。
事件支撐：INFRA-COMPACT-AUTOCOMMIT-01 Codex R2 PASS 產出
`.tmp/cross_review_result_20260719_173726.md`，回執三件套寫入 `sv_report.md`
「Cross-Review 回執」段落（同日 17:37）；同日稍晚（19:24、21:00-21:05）另一票
`INFRA-CONCURRENCY-01` 之 SV 執行覆寫 `sv_report.md` 全域內容，且該時段 `.tmp/` 與
`tmp/sv_report_*.md` 之 5 筆保留制輪替，致原始 receipt 檔與其 sv_report.md 引用段落
均不可回讀——僅剩 git-tracked 的 PROGRESS.md/commit message 摘要（testimony），
receipt 檔本體（primary artifact）已遺失。Verdict 與 DIFF_SCOPE 因分別有 git commit
（`62311fc`/`7ceb010`）與 git-derivable commit hash 佐證，仍可獨立驗證；唯 receipt
路徑無對應保存機制。Akira 裁決 A+B 合裁：現狀不重開票（A），本候選登記供未來
concurrency.md §1 待開票（per-ticket scratch 隔離）立法引用（B）。
防護：呼應 `concurrency.md` §1 待開票清單——per-ticket scratch 隔離
（`tmp/<TICKET_ID>/`）與 receipt 保留策略需將「尚有未收口三件套義務的 receipt」
排除於自動輪替之外，或於 Ticket 收口當下即把 receipt 內容複製進 git-tracked 產物
（而非僅留路徑引用）。本候選為 `concurrency.md` 九例對照表 R1/R2 之第三個同構實例
（暫記 R3，正式併表待 concurrency.md SSOT owner 裁決）。
```

### K4-cand-20260721-01：票面 Rev bump 未強制再生 verify_cmd.conf 契約分歧律
```
Ticket 開工時 `tmp/verify_cmd.conf` 依 Boot Sequence 由票面 `## Verify Config`
一次性複製生成；票面若於開工後再經 Rev bump（如 CSO 追加裁決補登 ALLOWED 項），
`tmp/verify_cmd.conf` 不會隨之再生，兩份契約即可靜默分歧——runtime 依舊只認舊版
ALLOWED，`/qa` Step 4.5 Scope 汙染檢查因而對「票面已授權、但 conf 未同步」之檔案
誤判為汙染。此非 Auto-Chain 誤攔（攔截行為忠實於現行 conf，機制運作正確），而是
「票面即時裁決」與「runtime 契約快照」之間缺乏強制再生鏈路。
事件支撐：MEO-T2——Rev B.1（R11，2026-07-21）補登 `stop_orchestrator.sh`、
Rev B.2（R12，同日）補登 `cross_review.sh`+`STATE.md` 均已寫入票面 ALLOWED，
但 `tmp/verify_cmd.conf` 仍停留於 Rev B 原始 5 項，`/qa` Step 4.5 因而攔截三檔
為 Scope 汙染。Akira 裁決 D（結構化混合案）：純度驗證確認一筆無關 staged rename
（MEO-T1 路徑修正）獨立成 T1 commit（`fa1ec18`）與 conf 再生分離處理，`tmp/verify_cmd.conf`
經 CSO 側直接同步至票面現況後，Step 4.5 重跑零汙染，T2 commit（`8044a0d`）順利完成。
防護：待開票——`tmp/verify_cmd.conf` 再生鏈路機械化（票面 Rev bump 偵測 → 提示或
自動觸發 conf 重新複製），或 Step 0 Scope Gate 增「票面簽發日期 Rev 註記 vs
conf CREATED」新鮮度比對分支，避免下次 Rev bump 事件重演同一誤判-裁決循環。
```

### K4-cand-20260722-01：機械閾值 headroom 推導值須實證回校律（PIPELINE_ARTIFACT_MAX_LINES=40）
```
以 headroom margin 推導（而非實證分布擬合）設定之機械閾值，於立法當下僅為
「合理猜測」，MUST 登記假陽性率觀測義務並於累積足量樣本後回校；否則閾值將
以「已裁決常數」之姿固化，假陽性由下游票以個案 ALLOWED 明列吸收，根因
（閾值失準）永不浮現。
事件支撐：INFRA-VERIFY-PA-BOUND-01（2026-07-22）設
`PIPELINE_ARTIFACT_MAX_LINES=40`，依據為近期合法 Step-4 同步 per-file delta
實測 4–16 行（n 小）之最大值 × 2.5 headroom；CSO 審查 F3 併案指出 40 為推導值
非實證值，且 ARCHIVE 輪替（3-session 規則觸發、與票面意圖無關）之 delta 分布
未納入樣本。已於票面 AI-2 條款 4 增設「輪替延後至專用 docsync 票」常設指引
消除主要假陽性向量，殘餘假陽性率待觀測。
防護：累積 2–3 個 sprint 之 PA 檔（PROGRESS.md / PROGRESS_ARCHIVE.md）
per-file delta 分布後回校 40 值；觀測項＝「PA 檔因超界 fall-through 落
SCOPE VIOLATION 且事後裁定為合法同步」之次數。回校屬翻盤條件明確之常數調參，
經確認決議即可，免重開根因票。
```

### K4-cand-20260722-02：真實狀態不可回退律（truthful-resync 常設化候選）
```
scope cleanup 可抵銷越界 payload，但 MUST NOT 回退已成立的真實狀態；
Dashboard／治理記錄之修正 MUST 以 truthful-resync 方式完成（保留狀態日期、
當前治理判定、block 條件、待派工項之自洽敘述），禁止「為清 diff 而犧牲
current truth」。驗收 PROGRESS.md 類修補票時，不能只看 diff 是否乾淨，
還必須看 current truth 是否自洽、單一、不可雙重解讀。
事件支撐：MEO-T3-DOCSYNC-01 第四次審查（2026-07-22，✅ Well Implemented）——
前三輪之 blocking finding 即「artifact truth / Dashboard split-brain」：
早前 cleanup 為清 diff 回退了 Dashboard 真實狀態；第四輪以 truthful-resync
補回（Last Updated / Sprint Health S78 / Next Actions #17 改述），S76–S78
敘述自洽後方通過。審查方明示此為有價值之治理規則。
防護：常設化落點候選＝`ssot-governance.md`「PROGRESS.md Dashboard 紀律」段
增補條款（與 INFRA-VERIFY-PA-BOUND-01 之 AI-2 新增小節相鄰但議題獨立，
依原子化原則未併入該票）；採納時建議開獨立小票（doc-only，preset=bugfix）。
```

### K4-cand-20260725-01：閘門前移必附既存狀態遷移律
```
凡收緊/前移 gate 之票（含輸出契約變更、豁免分支順序調整），MUST 於票面
普查該 gate 生效後將立即消費之「既存狀態實例」（舊格式 flag、pending 殘留、
計數器），並明訂遷移路徑（認定過期／補建新格式／人工裁決通道）；缺遷移條款時，
死鎖為結構必然而非意外。
事件支撐：MEO-FIX-A Rev F AI-3（2026-07-25）——pending-gate 前移至 bootstrap
豁免分支之前後立即生效，咬住先於 episode-identity 機制之無主 pending flag
（無 MEO_ESCALATION_ID），連帶咬死舊 diagnosis 刷新 workaround，專案層級全鎖
（pwd 亦 deny）；Rev F 規格完整定義新格式 episode 之產生與釋放，惟未含既存舊
格式狀態之普查與遷移條款。執行者依 STOP 條款正確停止，由 Akira 以 out-of-band
通道人工遷移解鎖（見 PROGRESS.md 項 20）。
防護：TICKET_TPL 層級候選——gate 類票新增強制檢核項「既存狀態普查表」；
或於 decision-tree.yaml pending_release 相鄰段增補 legacy-state census 前置。
落點待 KAIROS #4 收割輪裁決。
```

### K4-cand-20260725-02：ALLOWED token 可匹配性檢核律（死 token 即殭屍條款）
```
開票/修訂時 MUST 逐一檢核 Verify Config ALLOWED 之每一 token 於現行
token_matches_file() 語意下可實際匹配其意圖標的：目錄意圖 MUST 帶 glob
字元（如 dir/*）；token 既非既存字面路徑、又不含 */?/[ 者為「死 token」
——機械上永不匹配任何檔案的殭屍條款，授權範圍虛設。
事件支撐：MEO-FIX-A Rev F /qa Step 0（2026-07-25，commit e065d20）——
票面 ALLOWED 含裸尾斜線 token meo/adjudications/，INFRA-VERIFY-GLOB-01
修正版 token_matches_file() 對不含 glob 字元之 token 只做字面比對，致票面
自身交付物（AI-2 §C 之 SCHEMA.md/.gitkeep）被誤判 SCOPE VIOLATION，
執行者依 Rule 4c STOP。build/tests/lint 全綠，純閘門缺口。K4-2（機械閘門
對合法操作 false positive）第 N 例，但根因層次不同：K4-2 屬缺豁免條款，
本例屬授權條款本身機械不可兌現。
防護：兩層候選——①TICKET_TPL 開票檢核項增「ALLOWED token 可匹配性
逐項驗證」（token 語法 lint：目錄型必帶 glob）；②verify 腳本層增 dead-token
偵測（token 非既存路徑且不含 glob 字元 → 開票期/Boot 期 FAIL-fast，
而非執行期 SCOPE VIOLATION 假陽性）。落點待 KAIROS #4 收割輪裁決。
```

### K4-cand-20260725-03：token 多重消費端語意稽核律
```
修正任何 config token 之語意/拼寫時，MUST 先盤點該 token 之全部 consumer
（scope matcher、行數計算器、任何 awk/grep/sed 消費點）並逐端驗證修正後
語意相容；單一 consumer 驗證通過即宣告修正完成＝未完成修正。此為修訂波及
面全檔掃描律之機器層級同構：文字層掃「同一主張之所有實例」，機器層掃
「同一 token 之所有消費端」。
事件支撐：MEO-FIX-A Rev G（2026-07-25，Codex delta 復審 F1 BLOCKER）——
ALLOWED token 改 meo/adjudications/* 後，scope matcher（verify_checklist.sh
L43–50，Bash pattern matching）正確匹配，但行數計算器（L405–409，awk
$3==token 字面等值）不解讀 wildcard，SCHEMA.md 65 行漏算，/qa 將以
+236≤300 假綠通過；真值 additions=301 已超 MAX_LINES=300。
與 K4-cand-20260725-02 區辨：彼為 token 自身機械不可兌現（死 token），
此為 token 跨消費端語意分歧（同 token 雙義）。
防護：①票面修訂涉 Verify Config token 時，修訂說明 MUST 附 consumer
盤點表（逐端：消費點位置、匹配語意、修正後行為）；②衍生獨立票候選：
verifier 行數計算器 glob-aware 化＋additions-only 與 AC added+deleted
口徑對齊（verify_checklist.sh 屬 FORBIDDEN，依 scope 一致性檢查 defer）。
落點待 KAIROS #4 收割輪裁決。
```

### K4-cand-20260725-04：前置資源恆存律之開票期機械檢核缺口
```
前置資源恆存律（強制條款交付前 MUST 逐條檢核其前置資源之產生通道已明文
授權）已為 userPreferences 常駐條文（D7 事件收割），仍於 MEO-FIX-A
Rev D→F 三輪簽發中未被套用、並連過兩輪 Codex 復審——規則有耐久載體
而無開票期機械檢查點＝同樣失效（KAIROS 原理之升級態：載體存在≠檢核
發生）。
事件支撐：MEO-FIX-A AC §6-8(iii) 要求 git diff --no-index <baseline複本>
行數佐證，而 AI-0 步驟 4 僅授權記錄 baseline SHA-256，複本產生通道從未
授權；2026-07-26 CSO 全樹取證（3,455 檔）確認複本不存在，行數重建機械
不可得，構成本票第三條死條款（前二：死 token K4-cand-20260725-02、
token 雙義 K4-cand-20260725-03；三者合流指向同一開票期缺口家族：
條款之機械可兌現性從未被逐條驗證）。
防護：TICKET_TPL 增強制檢核項「AC 前置資源產生通道對照表」——每條 AC
所需 artifact ↔ 授權其產生之 AI 編號逐一對應，無對應即開票期 FAIL-fast。
落點待 KAIROS #4 收割輪裁決。
```
