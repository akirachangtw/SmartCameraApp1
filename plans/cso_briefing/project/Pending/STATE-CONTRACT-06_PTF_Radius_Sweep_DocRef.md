# STATE-CONTRACT-06: PTF 半徑筆誤增殖清除（×0.6f ×3 → 複用 HALO_RADIUS_SCALE）+ DOC-REF 註解修正

**簽發日期**: 2026-07-13
**簽發者**: Claude (CSO)
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: 免送審（X1 類：值域已由 SC-05 Path A 定錨，本票為同一筆誤之增殖清除；/qa + /sv 本地閘即可）
**前置依賴**: SC-04/SC-05 已關票（Traycer R3 Path A · 錨 `2ba2b83`）；**時窗：D16+ 或實測後均可**（PTF 不在 Track B 採樣清單）

## 檔案治理/來源說明
D.3 定位案（`archive/Cueshot_D8_D15_Strategic_Brief_v1.md` §D.3，aud_20260713_972b66cba2）：D.3 為全域呼吸韻律通則，**查無 PTF 半徑條款** → Postponed #7 翻盤條件觸發，`HaloGuidanceLayer.kt:264/276/291` 之 ×0.6f 判定為 `HALO_RADIUS_SCALE` 同源轉錄筆誤增殖（SSOT §2.5.4 明禁 0.6）。正解 = 回歸同一常數，**不開新常數**（免 K4-η 空位驗證，且消除 inline magic number）。DOC-REF-01 併入本票（Akira 核准 2026-07-13）。

## Verify Config
TICKET_ID=STATE-CONTRACT-06
CREATED=2026-07-13
PRESET=bugfix
ALLOWED=app/src/main/java/com/akirachang/ai/photographer/presentation/camera/components/HaloGuidanceLayer.kt
FORBIDDEN=app/src/main/java/com/akirachang/ai/photographer/domain/config/GuidanceStateConstants.kt,app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt,plans/architecture/**,.claude/PROGRESS.md
MAX_LINES=40
DIFF_REF=[SPAWN_FILL]

## Action Items

### AI-1: PTF 半徑三處改引常數
- **檔案**: `HaloGuidanceLayer.kt`
- **現值**: L264 / L276 / L291 各含硬編碼 `* 0.6f`（PTF 半徑公式）
- **目標值**: 三處一律改為 `* GuidanceStateConstants.HALO_RADIUS_SCALE`（= 0.5f，SC-05 已錨）
- [ ] 完成

### AI-2: 註解引用修正（DOC-REF-01 併入）
- **檔案**: `HaloGuidanceLayer.kt`
- **現值**: L96 附近 KDoc「PTF breathing animation spec per Strategic Brief Part D.3」
- **目標值**: 改為「per SSOT v4.0.7 §2.5.4 呼吸帶規格；歷史通則 Strategic Brief D.3（archive/）已被超越，血統見 Postponed #7 定性」。**`PTF_BREATH_ALPHA_MIN/MAX` 值零觸碰**（0.10/0.18 維持，D15 觀察後定值）
- [ ] 完成

### AI-3: K4-η 終態複掃（唯讀）
- **檔案**: `app/src/main/` 全域
- **現值**: 首輪 13 命中（2026-07-12 裁決表）
- **目標值**: 複掃 `0.6f`；預期終態 = 白名單 gradient 色標 ×1 + 灰七處（語義域外，不動）；`HaloGuidanceLayer.kt` 歸零。新增命中 → STOP
- [ ] 完成

## Hard Constraints
1. 單次觸碰：僅 PTF 半徑 ×3 + L96 註解；PTF alpha 常數、Normal 路徑、hysteresis 零 byte 觸碰
2. 禁開新常數；禁改 `GuidanceStateConstants.kt`（FORBIDDEN 強制）
3. 註解修正禁改動任何可執行碼行

## Error Handling
- AI-1 若發現三處公式與 Normal 路徑結構差異大到無法直接替換常數 → STOP 回報（可能存在未知 PTF 縮放語義）
- AI-3 新增命中 → STOP 回 CSO

## Acceptance Criteria
- [ ] **[存在性]** `HaloGuidanceLayer.kt` 全檔零 `0.6f` 字面量
- [ ] **[可達性]** L264/L276/L291 半徑鏈經 `GuidanceStateConstants.HALO_RADIUS_SCALE`（證據欄：修後行號引文）
- [ ] **[行為性]** `testDeviceDebugUnitTest` 全綠零回歸（含 C-ρ1 · 9/9）
- [ ] `assembleDeviceDebug` PASS
- [ ] `git diff --stat` 僅 `HaloGuidanceLayer.kt`

## 來源
Postponed #7 定性（2026-07-13 翻盤條件觸發）；D.3 glob 定位案；SC-05 Train② STOP 首輪裁決；Akira 核准 DOC-REF-01 併入。
