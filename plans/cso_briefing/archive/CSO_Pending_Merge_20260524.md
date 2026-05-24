# CSO 待合併交付物 — 2026-05-24

**簽發**: CSO (Claude Opus, claude.ai)
**來源**: Session 13 — Cross-Review FAIL 事件分析 + ssot_context 全面對齊
**狀態**: 待 Akira 下次 Claude Code session 手動觸發執行
**Obsidian 歸檔**: 2 筆已完成（CrossReview_FAIL_Governance_Rule + SSOT_Context_v14_Alignment）

---

## 交付物 A：Cross-Review FAIL 處置硬規範

**目標位置**: CLAUDE.md §治理規則（最高層，非 .claude/rules/ 子目錄）
**理由**: /uc 模式下子目錄規則加載不可靠，治理紅線必須每次 session 可見

### 規則文字（直接複製貼入 CLAUDE.md）

```markdown
## §Cross-Review FAIL 處置硬規範（不可違反）

### 觸發條件
任何 cross_review.sh 退出碼 ≠ 0 時生效。

### 第一層判定：客觀邊界檢查（無需 LLM 判斷）

**檔案邊界**：FAIL 涉及的修正若觸及以下任一檔案 → **必停 + 回報**：
- PROGRESS.md / PROGRESS_ARCHIVE.md
- 任何 sv_report*.md
- .claude/skills/sv/ssot_context.md
- .claude/skills/ 下任何 SKILL.md
- CLAUDE.md
- SSOT*.md
- 任何 *_Contract*.md / *_Slice*.md

**行為邊界**：FAIL 涉及的修正若包含以下任一 token → **必停 + 回報**：
- FeatureFlags / featureFlags / feature_flag
- gate / Gate / GATE
- cooldown / Cooldown
- threshold（若該常數已存在於 SSOT）
- @Inject 新增/移除
- DI 模組變更（@Module / @Provides / @Binds）

**任一邊界觸發 → 立即輸出 Checkpoint 模板，停止所有自動修復**。

### 第二層判定：純 code 修正的次數限制

通過第一層後，若 FAIL 確實只涉及 ticket primary diff 內的 `.kt` 檔案：
- **第 1 次 FAIL**：可 inline fix，但必須**完成後立即回報「我修了什麼」+ 等 ack 才能繼續**
- **第 2 次 FAIL（同票任何維度）**：**必停**，輸出 Checkpoint 模板
- 不存在「第 3 次 fix」的可能性

### Checkpoint 模板（強制格式）

📍 CROSS_REVIEW CHECKPOINT
============================
Ticket: [票號]
Round: R[N] / 第 [N] 次 FAIL
Codex 判定: [原文摘錄]

我的觀察:
- [觀察 1]
- [觀察 2]

可能的處置選項:
A. [選項 A]
B. [選項 B]
C. [選項 C]
D. 我認為 Codex 判定有誤，理由: [理由]

⏸️ 等待 Akira 裁決

### 退出機制
Akira 裁決後，依裁決結果繼續。**任何情況下 Claude Code 不得在無 Akira ack 的狀況下，連續執行 2 次以上 cross_review.sh**。
```

---

## 交付物 B：ssot_context v1.4 修補指令

**目標檔案**: `.claude/skills/sv/ssot_context.md`
**版本**: v1.3 → v1.4
**禁區**: ⛔ 不得添加 `FOCUS_RETAP_FLASH_MS`（SSOT 未定義，屬自創常數）

### Patch 1：§4 常數表擴充

**位置**: §4 表格最後三行（Focus AF / drift / 長按）替換為完整 11 行

**刪除**:
```
| Focus AF 超時 | `FOCUS_AF_TIMEOUT_MS` = 2000ms | 超時 → Failed 狀態 |
| Focus drift 閾值 | `FOCUS_DRIFT_THRESHOLD_DEG` = 15° | Focused → fadeout → Idle |
| Focus 長按閾值 | `FOCUS_LONG_PRESS_MS` = 500ms | 觸發 AE/AF Lock |
```

**替換為**:
```
| Focus 圓環尺寸 | `FOCUS_RING_SIZE_DP` = 60dp | 圓環外徑（SSOT A.1） |
| Focus 圓環線寬 | `FOCUS_RING_STROKE_DP` = 1.5dp | 邊框寬度（SSOT A.1） |
| Focus AF 超時 | `FOCUS_AF_TIMEOUT_MS` = 2000ms | 超時 → Failed 狀態（SSOT A.2.1） |
| Focus fadeout | `FOCUS_FADEOUT_MS` = 500ms | drift 後淡出（SSOT A.2.2.1） |
| Focus Failed | `FOCUS_FAILED_MS` = 800ms | Failed 振盪時長（SSOT A.2.3） |
| Focus 長按閾值 | `FOCUS_LONG_PRESS_MS` = 500ms | 觸發 AE/AF Lock（SSOT A.2.4） |
| Focus drift 閾值 | `FOCUS_DRIFT_THRESHOLD_DEG` = 15° | Focused → fadeout → Idle（SSOT A.2.2.1） |
| 對齊統一門檻 | `ALIGNED_THRESHOLD_DEG` = 5° | 三通道共用（SSOT §2.0） |
| Level 漸入區間 | `LEVEL_FADE_RANGE_DEG` = 10° | N=10°，T7 核心公式（SSOT §2.0.1） |
| 快門閃爍 | `SHUTTER_FLASH_DURATION_MS` = 300ms | 拍攝確認（SSOT §10.3.1） |
| 標準過渡 | `TRANSITION_FAST_MS` = 300ms | 動畫通用時長（SSOT §2.0） |
```

### Patch 2：§6 Halo 對齊 alpha 修正

**刪除**:
```
| Halo 對齊 | `#50C864` | 0.35 |
```

**替換為**:
```
| Halo 對齊（呼吸峰值） | `#50C864` | 0.18（呼吸 0.10↔0.18） |
```

### Patch 3：§5 新增 §5.5

**位置**: §5.4 場景判斷優先級之後，§6 之前

**新增**:
```markdown
### 5.5 P05 Photo Mode Recovery 活躍範圍

| Ticket | 狀態 | 驗收焦點 |
|--------|------|---------|
| T1 | ✅ Done | Eye-AF 視覺環 flag 關閉，FocusIndicatorOverlay 不渲染 |
| T2 | ✅ Done | TapFocusRing 五態渲染，Canvas ≤1 次 drawCircle |
| T3 | 🔧 Active | TapFocusController 狀態機 SSOT A.3 + CameraX AF 接線 |
| T4 | 🔧 Active | Scrim y=0 α0.22 → y=120px α0 |
| T5 | ✅ Done | 快門色彩硬編碼 B.3.3 |
| T6 | ✅ Done | Grid 預設可見 |
| T7 | 🔧 Active | Level Traction 退場 N=10° 公式 |

**Strangler Fig 架構**：TapFocusRing/Controller 為新建系統，FocusIndicatorOverlay 為 MVP 關閉但保留的舊系統。兩套共存不衝突。
```

### Patch 4：§5.4 補 LowAngleMode

**刪除**:
```
PASS_TO_FRIEND (手動) > pitch > 35° (Food) > face detected (Portrait) > General
```

**替換為**:
```
PASS_TO_FRIEND (手動) > pitch > 35° (Food) > pitch ∈ [-10°,35°] + face (Portrait) > General > pitch < -10° (LowAngle)
```

### 版本元資訊更新

**刪除**:
```
- **版本**: ssot_context v1.3
```
**替換為**:
```
- **版本**: ssot_context v1.4
```

**刪除**:
```
- **更新日期**: 2026-05-19
```
**替換為**:
```
- **更新日期**: 2026-05-24
```

**變更摘要追加**:
```
- **變更摘要 (v1.3 → v1.4)**:
  - §4 FOCUS_* 常數從 3 → 7（補 RING_SIZE, RING_STROKE, FADEOUT, FAILED）
  - §4 新增 ALIGNED_THRESHOLD_DEG, LEVEL_FADE_RANGE_DEG, SHUTTER_FLASH_DURATION_MS, TRANSITION_FAST_MS
  - §5.4 補 LowAngleMode
  - §5.5 新增 P05 活躍範圍
  - §6 Halo 對齊 alpha 0.35 → 0.18（修正 SSOT 代碼對齊）
```

---

## 交付物 C：R2-R5 Revert SOP

**前置**: 進入 Claude Code session 後，先確認 commit 歷史

```bash
# Step 1: 確認 R2-R5 的 commit SHA
git log --oneline -20

# Step 2: 識別以下特徵的 commit（依時序）
# R2: PROGRESS.md 修改
# R3: sv_report.md 修改
# R4: PROGRESS.md 再次修改
# R5: FeatureFlags gate 恢復

# Step 3: 逐一 revert（從最新到最舊）
git revert --no-commit <R5_SHA>
git revert --no-commit <R4_SHA>
git revert --no-commit <R3_SHA>
git revert --no-commit <R2_SHA>

# Step 4: 確認 diff
git diff --cached --stat

# Step 5: 確認 FeatureFlags gate 狀態是否回到 T1 設定
grep -n "isTapFocusReady\|FeatureFlags" app/src/main/java/.../presentation/camera/CameraScreen.kt

# Step 6: 等待 Akira 確認後 commit
# git commit -m "revert: undo R2-R5 unauthorized cross-review auto-fixes (CSO directive)"
```

---

## 執行排序建議

```
Session 開始
━━━━━━━━━━━
1. 交付物 C：R2-R5 Revert（清理非授權修改）
2. 交付物 A：CLAUDE.md 合併硬規範（立即生效）
3. 交付物 B：ssot_context v1.4 Patch 執行
4. 回到 P05 Day 2 正常流程：T3 → T4 → T7
```

---

## 文件元資訊

| 欄位 | 值 |
|------|-----|
| 簽發者 | CSO (Claude Opus, claude.ai) |
| 日期 | 2026-05-24 |
| Session | 13 |
| Obsidian 歸檔 | 2 筆（CrossReview_FAIL_Governance_Rule + SSOT_Context_v14_Alignment） |
| 下次審查 | Phase 0.5 Gate 或 KAIROS |

---

**End of CSO Pending Merge 20260524**
