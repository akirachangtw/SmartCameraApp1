# TRACTION-PROTO-02: Prototype v3.3 對齊態常數同步（QA 真值身分完整性修復）

**簽發日期**: 2026-07-02
**簽發者**: CSO
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（原型常數同步）｜ Cross-Review: N/A（機械式常數改值，AC 錨定驗證）
**前置依賴**: TRACTION-SSOT-04 PASS（原型跟已裁 spec 走，不得先於 spec 改）

## 檔案治理 / 來源說明
執行 CSO 裁決 **R-T13 V-1~V-5**（2026-07-02）。v3.3 經 R-T7 封為「可執行 QA 真值」，但其對齊態常數仍錨於舊 alpha 政權（0.10→0.18 / Pitch rgba 疊乘 / V 曲線谷底 0.1）且 L244 註解「0.85 刻意保留勿同步」與 QA 真值身分互斥（V-5 採方案 a：改 0.8 同步 SSOT）。**本票為 v3.3 唯讀豁免票**——R-T13 明文授權編輯 v3.3；v3.2 維持唯讀歷史 baseline（設計原值 0.85 由 v3.2 承載，不流失）。溯源：Obsidian #114（R-T13 / topic: Aligned_State_Visibility_ReAnchor）。

## Verify Config
TICKET_ID=TRACTION-PROTO-02
CREATED=2026-07-02
PRESET=refactor
ALLOWED=plans/architecture/Cueshot_State_1-6_Prototype_v3_3.html
FORBIDDEN=app/src/**,.claude/**,plans/architecture/SSOT_v4_0_6.md,plans/architecture/Cueshot_Layout_Spec_v2_5_1.md,plans/cso_briefing/project/file/Cueshot_State_1-6_Prototype_v3_2.html
MAX_LINES=60
DIFF_REF=TBD

## Action Items（行號依 UITA 取樣 + CSO 覆核，執行時以錨定字串定位）

### AI-1: 對齊呼吸 keyframes 抬升（V-1）
- **位置**: L281 `breathe-halo-aligned` + L299 `breathe-ambient`
- **現值**: `opacity: 0.10` ↔ `0.18`（halo 版含 scale 0.98→1.02）
- **目標值**: `opacity: 0.25` ↔ `0.40`（scale 不變）
- [x] 完成

### AI-2: Pitch 對齊態全 alpha 化（V-2）
- **位置**: L323 line `rgba(80, 200, 100, 0.4)` / L335 ring `rgba(...,0.6)` / L349 dot `rgba(...,0.4)`
- **目標值**: 三處一律 `var(--align-green)`（alpha 包絡由呼吸單獨承載）
- [x] 完成

### AI-3: Pitch 對齊圓點強存在（V-3）
- **位置**: Pitch aligned 分支（CSS L348-351 + JS ~L1012 aligned 分支）
- **現值**: 對齊圓點隨 breathe-ambient 近隱形
- **目標值**: 比照 Halo（JS L940-942）：對齊後圓點 opacity 0.85、scale 1.1、退場呼吸僅作用環/線（dot 移除 breathe-ambient animation，改 breathe-dot 或靜態 0.85）
- [x] 完成

### AI-4: V 曲線谷底/增益統一（V-4）
- **位置**: JS L947-948（Halo）、L989/992（Food Halo）、L1016-1017（Pitch）
- **現值**: `0.1 + x × 0.75`（Halo/Food）、`0.1 + x × 0.7 / 0.9`（Pitch amber/green）
- **目標值**: 六處統一 `0.35 + x × 0.5`（峰 0.85）
- [x] 完成

### AI-5: 未對齊 0.85 → 0.8 同步 + 註解改寫（V-5 方案 a）
- **位置**: CSS L244-246（註解 + `opacity: 0.85`）、JS L945 `haloEl.style.opacity = 0.85`、及區段掃描所有未對齊 Halo 0.85
- **現值**: `0.85` + 註解「此差異刻意保留，勿同步」
- **目標值**: `0.8` + 註解改寫：「v3.3+R-T13 V-5：同步 SSOT §2.5 之 0.8（R-T2/R-T9）。設計原值 0.85 之歷史紀錄見 v3.2（唯讀）。QA 真值常數與 SSOT 一致，無容差帶。」
- [x] 完成（含 JS L984 Food Halo 未對齊同型態同步）

### AI-6: 版本註記
- **位置**: L7 v3.3 NOTE
- **目標值**: 追加「+ R-T13 對齊態可見性修正（V-1~V-5）2026-07-02」
- [x] 完成

## Hard Constraints
1. **v3.2 絕對禁止觸碰**（唯讀歷史 baseline，設計原值檔案庫）。
2. 僅改常數/色值/註解——狀態機 JS 邏輯（roll-only、D-9 Food 閘、PTF、快門 gate）禁止觸碰（UITA 四項正面確認區）。
3. 未對齊→0.8 僅限 Halo 未對齊態；對齊態圓點 0.85 強存在（V-3）**保留 0.85**，勿誤掃。

## Error Handling
- 錨定字串定位失敗或現值不符 → 停止回報。
- 發現 0.10/0.18/0.85 出現於 AI 未列舉位置：同機械類（對齊呼吸/未對齊 Halo alpha）→ 併入 sweep 明列回報；異類（如 breathe-dot 的 0.7、按鈕 active 態）→ **勿動**（非本票語境）。

## Acceptance Criteria
- [ ] AI-1~6 全落地；瀏覽器開啟 State 2/3 目視：對齊呼吸明顯可見、Pitch 對齊三元素全綠可辨、圓點歸心強存在（**待 Akira 實機/瀏覽器目視確認**）
- [x] 錨定掃描：`breathe-halo-aligned|breathe-ambient` keyframes 內無 `0\.10|0\.18`
- [x] 錨定掃描：`.pitch-traction.aligned` 區塊內無 `rgba(80, 200, 100, 0\.[46])`
- [x] 錨定掃描：V 曲線六處均為 `0.35 + ` 且增益 `0.5`
- [x] 未對齊 Halo 無 `0.85` 殘留（對齊態圓點 0.85 除外——具名保留，剩餘 3 處均為對齊態）
- [x] `git diff --stat` 僅涉及 ALLOWED（v3.3 單檔，`git status` 確認）
