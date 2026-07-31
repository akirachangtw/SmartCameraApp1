# Cueshot S12 Executable Amendment — Claude Code 執行指令

**簽發**: UITA (Claude Opus 4.6) + CSO sign-off
**日期**: 2026-06-13
**驗證基礎**: UITA 審查 → Perplexity 雙輪校對（11/11 confirmed）→ CSO 裁決
**執行順序**: §A SSOT Hotfix → §B Contract v0.4 修訂 → §C 驗證

---

# §A SSOT v4.0.6 Hotfix

**File**: `plans/architecture/SSOT_v4_0_6.md`

## HF-A1: C-4 LANDMARK_ALL 引用修正 [CRITICAL]

```
[str_replace]
OLD:
     d. C-4: MLKitDetector mode-aware 重構（D-22, R31）— 含 LANDMARK_ALL 恢復（D-23, R33）+ enableMultipleObjects 移除（D-21, R30）
NEW:
     d. C-4: MLKitDetector mode-aware 重構（D-22, R31）— LANDMARK_NONE（D-27 降級）+ CLASSIFICATION_ALL（D-23/HOTFIX-47）+ enableMultipleObjects 移除（D-21, R30）
```

## HF-A2: 版本引用修正 [MAJOR]

```
[str_replace]
OLD:
  2. Claude Code 按 SSOT v4.0.5 + Layout Spec v2.5.1 實作：
NEW:
  2. Claude Code 按 SSOT v4.0.6 + Layout Spec v2.5.1 實作：
```

## HF-A3: B.14 位置修正 [MEDIUM]

此為剪切-貼上操作，需分兩步：

**Step 1 — 刪除錯位的 B.14 區塊**:

```
[str_replace]
OLD:
### B.14 Face Detection 配置修正 + Portrait 狀態機入 SSOT（D-27）

D-23 修正：LANDMARK_ALL 降為 NONE（MVP）。v2.12.0 "Unknown landmark type" crash 未根治 + production 從未成功部署 = SSOT 與代碼語義分裂。EyeLineRule 延後至 Phase 1.5；AF focus target 降級為 FACE_CENTER。
新增 §4.4.1 Portrait Guidance 六態狀態機（NO_FACE → FACES_DETECTED_NONE_ELIGIBLE → SUBJECT_STABILIZING → GUIDANCE_ACTIVE → ALIGNED → SUPPRESSED）。
新增 §4.4.2 Face Detector Session Lifecycle（建立/暫停/恢復/切換）。

**翻盤條件**：

- LANDMARK_ALL：ML Kit 新版修復 crash + 1000 幀雙裝置零 crash → Phase 1.5 恢復
- 狀態機：六態不足以覆蓋新產品場景 → 在 SSOT 層增態（禁止 Contract 層自行擴展後再升格）

### B.13 Grid 三分網格線規格鎖定（D-24）
NEW:
### B.13 Grid 三分網格線規格鎖定（D-24）
```

**Step 2 — 在 B.13 結尾（`---` 之前）插入 B.14**:

```
[str_replace]
OLD:
**否決方案**: α0.25 過淡、α0.50 過重、不交換 Z-order（邊緣被 aligned 呼吸削弱）。

---

## Viewfinder Overlay 圖層定義
NEW:
**否決方案**: α0.25 過淡、α0.50 過重、不交換 Z-order（邊緣被 aligned 呼吸削弱）。

### B.14 Face Detection 配置修正 + Portrait 狀態機入 SSOT（D-27）

D-23 修正：LANDMARK_ALL 降為 NONE（MVP）。v2.12.0 "Unknown landmark type" crash 未根治 + production 從未成功部署 = SSOT 與代碼語義分裂。EyeLineRule 延後至 Phase 1.5；AF focus target 降級為 FACE_CENTER。
新增 §4.4.1 Portrait Guidance 六態狀態機（NO_FACE → FACES_DETECTED_NONE_ELIGIBLE → SUBJECT_STABILIZING → GUIDANCE_ACTIVE → ALIGNED → SUPPRESSED）。
新增 §4.4.2 Face Detector Session Lifecycle（建立/暫停/恢復/切換）。

**翻盤條件**：

- LANDMARK_ALL：ML Kit 新版修復 crash + 1000 幀雙裝置零 crash → Phase 1.5 恢復
- 狀態機：六態不足以覆蓋新產品場景 → 在 SSOT 層增態（禁止 Contract 層自行擴展後再升格）

---

## Viewfinder Overlay 圖層定義
```

---

# §B Contract v0.3 → v0.4 修訂

**Source file**: `plans/cso_briefing/project/file/Portrait_MVP_Kotlin_Contract_v0_3.md`
**Target file**: `plans/cso_briefing/project/file/Portrait_MVP_Kotlin_Contract_v0_4.md`（先複製一份再修改）

## CR-B8: 文件頭（版本 + Status + Merge Basis）

```
[str_replace]
OLD:
# Portrait MVP Kotlin Contract v0.3

Date: 2026-06-12
Status: D-27 Redline Incorporated — pending CSO final sign-off
Scope: Android / Kotlin / Jetpack Compose / CameraX / ML Kit Face Detection
Merge basis: Kotlin Contract v0.1 (base) + Kotlin Spec v0.1 (three-layer types) + Face AF Reducer Spec v0.1 (AF pipeline) + UITA S11 audit corrections
NEW:
# Portrait MVP Kotlin Contract v0.4

Date: 2026-06-13
Status: UITA S12 audit + Perplexity dual-verification incorporated — CSO signed-off
Scope: Android / Kotlin / Jetpack Compose / CameraX / ML Kit Face Detection
Merge basis: Contract v0.2 (S11 merge: v0.1 base + Spec v0.1 + AF Reducer v0.1 + UITA S11 audit) + D-27 SSOT Amendment redline + UITA S12 state-machine hardening
```

## CR-B6: §6.1 RawDetectedFace landmarks 註解

```
[str_replace]
OLD:
    val landmarks: Map<Int, PointF>?, // ML Kit landmark type -> preview-transformed point
    val timestampMs: Long
)
NEW:
    val landmarks: Map<Int, PointF>?, // ML Kit landmark type -> preview-transformed point; D-27: always null in MVP (LANDMARK_MODE_NONE); populated when LANDMARK_ALL restored in Phase 1.5
    val timestampMs: Long
)
```

## CR-B6 (cont.): §6.3 PublishedGuidanceSubject landmarks 註解

```
[str_replace]
OLD:
    val headEulerAngleZ: Float,
    val landmarks: Map<Int, PointF>?,
    val publishReason: PublishReason,
NEW:
    val headEulerAngleZ: Float,
    val landmarks: Map<Int, PointF>?, // D-27: always null in MVP (LANDMARK_MODE_NONE); populated when LANDMARK_ALL restored in Phase 1.5
    val publishReason: PublishReason,
```

## CR-B12: §6.6 Detector Lifecycle 不可復用禁令

```
[str_replace]
OLD:
The ViewModel is the correct owner for *session intent* (e.g., `isPortraitModeActive`) and *published state* (e.g., `PortraitGuidanceState`), but detector instance management belongs in the camera session layer (e.g., `LiveCameraSource` or equivalent `ImageAnalysis.Analyzer` owner).
NEW:
The ViewModel is the correct owner for *session intent* (e.g., `isPortraitModeActive`) and *published state* (e.g., `PortraitGuidanceState`), but detector instance management belongs in the camera session layer (e.g., `LiveCameraSource` or equivalent `ImageAnalysis.Analyzer` owner).

**Closed-instance prohibition (SSOT §4.4.2)**: A closed `FaceDetector` instance MUST NOT be reused. Calling `process()` on a closed detector is undefined behavior — the native ML Kit state is invalidated upon `close()`, not merely the `trackingId` counters. A new camera session always requires a fresh `FaceDetection.getClient(options)` call.
```

## CR-B3 + CR-B9: §7.1 FRAMING 移除 + TILT/SMILE 預留（CR-B4）

```
[str_replace]
OLD:
enum class PortraitRuleType {
    LEVEL,
    FRAMING,
    THIRDS,
    HEADROOM,
    EYELINE,    // Phase 1.5 — deferred; LANDMARK_ALL disabled per D-27
    GAZE        // MVP P1 — conditional on headEulerAngleY reliability
}
```

TiltRule and SmileRule are Phase 1.5 per SSOT §4.4 and are not included in this contract.
NEW:
enum class PortraitRuleType {
    LEVEL,
    THIRDS,
    HEADROOM,
    EYELINE,    // Phase 1.5 — deferred; LANDMARK_ALL disabled per D-27
    GAZE,       // MVP P1 — conditional on headEulerAngleY reliability
    TILT,       // Phase 1.5 — deferred; headEulerAngleZ > 8° (SSOT §4.4)
    SMILE       // Phase 1.5 — deferred; smilingProbability > 0.4 sustained > 300ms (SSOT §4.4)
}
```

FRAMING removed per CSO S12 ruling: FramingRule is SSOT §4.5 PTF-only (designed for "friends who don't understand photography" centering use case); Portrait mode coverage via ThirdsRule + HeadroomRule is sufficient.
```

## CR-B9: §7.4 MVP Rule Priority Matrix — FRAMING 行移除 + TILT/SMILE 新增

```
[str_replace]
OLD:
| Rule | Input source | Confidence | MVP Priority | SSOT phase |
|------|-------------|------------|-------------|------------|
| LEVEL | IMU roll | 0.95 | P0 | MVP |
| FRAMING | face bbox | 0.80 | P0 | MVP |
| THIRDS | face center | 0.80 | P0 | MVP (§4.4) |
| HEADROOM | face bbox top | 0.80 | P0 | MVP (§4.4) |
| EYELINE | landmarks[LEFT_EYE].y | 0.80 | Phase 1.5 | Deferred — LANDMARK_ALL disabled per D-27 |
| GAZE | headEulerAngleY | 0.80 | P1 conditional | MVP P1 |
NEW:
| Rule | Input source | Confidence | MVP Priority | SSOT phase |
|------|-------------|------------|-------------|------------|
| LEVEL | IMU roll | 0.95 | P0 | MVP |
| THIRDS | face center | 0.80 | P0 | MVP (§4.4) |
| HEADROOM | face bbox top | 0.80 | P0 | MVP (§4.4) |
| GAZE | headEulerAngleY | 0.80 | P1 conditional | MVP P1 |
| EYELINE | landmarks[LEFT_EYE].y | 0.80 | Phase 1.5 | Deferred — LANDMARK_ALL disabled per D-27 |
| TILT | headEulerAngleZ | 0.80 | Phase 1.5 | SSOT §4.4 — headEulerAngleZ > 8° |
| SMILE | smilingProbability | 0.80 | Phase 1.5 | SSOT §4.4 — smilingProbability > 0.4 sustained > 300ms |
```

## CR-B1 + CR-B2 + CR-B11: §8.2 State Transitions 全量替換

```
[str_replace]
OLD:
### 8.2 State Transitions

| From | To | Condition |
|------|-----|-----------|
| NO_FACE | FACES_DETECTED_NONE_ELIGIBLE | rawFaces.isNotEmpty() AND eligible.isEmpty() |
| NO_FACE | SUBJECT_STABILIZING | First eligible face appears |
| FACES_DETECTED_NONE_ELIGIBLE | SUBJECT_STABILIZING | First eligible face appears |
| SUBJECT_STABILIZING | GUIDANCE_ACTIVE | Same subject persists through publishEnterHoldMs |
| SUBJECT_STABILIZING | NO_FACE | All faces lost |
| GUIDANCE_ACTIVE | ALIGNED | All rules canImprove == false, stable for alignmentStableMs |
| GUIDANCE_ACTIVE | SUPPRESSED | Flicker guard triggered or focus-priority override |
| ALIGNED | GUIDANCE_ACTIVE | Any rule canImprove == true |
| ANY | NO_FACE | All faces lost beyond subjectLostTimeoutMs |
| SUPPRESSED | GUIDANCE_ACTIVE | Suppression clears, subject still present |
NEW:
### 8.2 State Transitions

| From | To | Condition |
|------|-----|-----------|
| NO_FACE | FACES_DETECTED_NONE_ELIGIBLE | rawFaces.isNotEmpty() AND eligible.isEmpty() |
| NO_FACE | SUBJECT_STABILIZING | First eligible face appears |
| FACES_DETECTED_NONE_ELIGIBLE | SUBJECT_STABILIZING | First eligible face appears |
| SUBJECT_STABILIZING | GUIDANCE_ACTIVE | Same subject persists through publishEnterHoldMs |
| SUBJECT_STABILIZING | NO_FACE | All faces lost (rawFaces.isEmpty()) |
| SUBJECT_STABILIZING | FACES_DETECTED_NONE_ELIGIBLE | rawFaces.isNotEmpty() AND eligible.isEmpty() |
| SUBJECT_STABILIZING | SUPPRESSED | focusPriorityActive == true (user tap during stabilization) |
| GUIDANCE_ACTIVE | ALIGNED | All rules canImprove == false, stable for alignmentStableMs |
| GUIDANCE_ACTIVE | SUPPRESSED | Flicker guard triggered or focusPriorityActive == true |
| GUIDANCE_ACTIVE | FACES_DETECTED_NONE_ELIGIBLE | publishedSubject == null AND rawFaces.isNotEmpty() AND eligible.isEmpty() — SHOULD be debounced by publishExitHoldMs |
| ALIGNED | GUIDANCE_ACTIVE | Any rule canImprove == true |
| ALIGNED | SUPPRESSED | focusPriorityActive == true OR flicker guard triggered |
| ALIGNED | FACES_DETECTED_NONE_ELIGIBLE | publishedSubject == null AND rawFaces.isNotEmpty() AND eligible.isEmpty() — SHOULD be debounced by publishExitHoldMs |
| SUPPRESSED | GUIDANCE_ACTIVE | Suppression clears AND publishedSubject != null |
| SUPPRESSED | FACES_DETECTED_NONE_ELIGIBLE | Suppression clears AND publishedSubject == null AND rawFaces.isNotEmpty() AND eligible.isEmpty() |
| SUPPRESSED | NO_FACE | Suppression clears AND rawFaces.isEmpty() |
| ANY | NO_FACE | All faces lost beyond subjectLostTimeoutMs (rawFaces.isEmpty() sustained) |

**SUPPRESSED state hardening note (S12)**: SUPPRESSED entry is triggered by AfCoordinator manual tap or flicker guard from any subject-tracking state (GUIDANCE_ACTIVE, ALIGNED, SUBJECT_STABILIZING). SUPPRESSED exit depends on suppression-clear event AND current face/eligibility state: (1) subject still present → GUIDANCE_ACTIVE for rule re-evaluation — no direct SUPPRESSED → ALIGNED jump; (2) subject ineligible, faces present → FACES_DETECTED_NONE_ELIGIBLE; (3) no faces → NO_FACE (immediate, no timeout — suppression-clear is a discrete event).

**Eligibility-drop debounce note (S12)**: Transitions from GUIDANCE_ACTIVE and ALIGNED to FACES_DETECTED_NONE_ELIGIBLE SHOULD use `publishExitHoldMs` (§12.1, default 200ms) to avoid UI flicker when eye-open probability or pose oscillates near the sanitizer threshold boundary. SUBJECT_STABILIZING and SUPPRESSED do not need this debounce (subject not yet published / already suppressed, respectively).
```

## CR-B5: §8.3 PortraitSubjectPipelineResult — 刪除 guidanceState

```
[str_replace]
OLD:
data class PortraitSubjectPipelineResult(
    val rawFaces: List<RawDetectedFace>,
    val eligible: List<EligibleFace>,
    val rejected: List<RejectedFace>,
    val publishedSubject: PublishedGuidanceSubject?,
    val guidanceState: PortraitGuidanceState
)
NEW:
data class PortraitSubjectPipelineResult(
    val rawFaces: List<RawDetectedFace>,
    val eligible: List<EligibleFace>,
    val rejected: List<RejectedFace>,
    val publishedSubject: PublishedGuidanceSubject?
    // guidanceState removed (S12 CR-B5): pipeline produces sanitization/publishing
    // results only; guidance state is computed solely by GuidanceReducer using
    // GuidanceReducerInput.previousState as single source of truth.
)
```

## CR-B13: §12.1 AF Cooldown 語義區分

```
[str_replace]
OLD:
    val afOwnerCooldownMs: Long = 1500L,
NEW:
    val afOwnerCooldownMs: Long = 1500L,      // Consumer: MultiFaceAfOwnerReducer (§9.3). Purpose: prevent rapid owner switching when challenger face briefly exceeds superiority margin. NOT interchangeable with afReacquireCooldownMs.
```

```
[str_replace]
OLD:
    val afReacquireCooldownMs: Long = 1500L,  // [REDLINE D-27] 1500L is provisional lower bound, not locked product value; range 1500–2000ms — lock after real-device testing
NEW:
    val afReacquireCooldownMs: Long = 1500L,  // [REDLINE D-27] Consumer: FaceAfReducer (§9.4). Purpose: prevent redundant focus commands when subject jitters around reacquire threshold. 1500L is provisional lower bound; range 1500–2000ms — lock after real-device testing. NOT interchangeable with afOwnerCooldownMs.
```

## CR-B9 (cont.): §16 Implementation Order — FRAMING 移除

```
[str_replace]
OLD:
5. Implement MVP P0 rule evaluators: LEVEL, FRAMING, THIRDS, HEADROOM
NEW:
5. Implement MVP P0 rule evaluators: LEVEL, THIRDS, HEADROOM
```

---

# §C 驗證

## C-1: SSOT v4.0.6 Hotfix 驗證

```bash
echo "=== HF-A1: C-4 LANDMARK_ALL removed ===" &&
! grep -q "LANDMARK_ALL 恢復" plans/architecture/SSOT_v4_0_6.md && echo "PASS" || echo "FAIL" &&
grep -q "LANDMARK_NONE.*D-27" plans/architecture/SSOT_v4_0_6.md && echo "PASS: D-27 ref present" || echo "FAIL" &&

echo "=== HF-A2: version ref ===" &&
! grep -q "按 SSOT v4.0.5" plans/architecture/SSOT_v4_0_6.md && echo "PASS" || echo "FAIL" &&
grep -q "按 SSOT v4.0.6" plans/architecture/SSOT_v4_0_6.md && echo "PASS" || echo "FAIL" &&

echo "=== HF-A3: B-series order ===" &&
B12=$(grep -n "### B\.12" plans/architecture/SSOT_v4_0_6.md | cut -d: -f1) &&
B13=$(grep -n "### B\.13" plans/architecture/SSOT_v4_0_6.md | cut -d: -f1) &&
B14=$(grep -n "### B\.14" plans/architecture/SSOT_v4_0_6.md | cut -d: -f1) &&
echo "B.12=L$B12, B.13=L$B13, B.14=L$B14" &&
[ "$B12" -lt "$B13" ] && [ "$B13" -lt "$B14" ] && echo "PASS: ascending order" || echo "FAIL: wrong order"
```

## C-2: Contract v0.4 驗證

```bash
FILE="plans/architecture/Portrait_MVP_Kotlin_Contract_v0_4.md"

echo "=== Version ===" &&
head -1 "$FILE" | grep -q "v0.4" && echo "PASS" || echo "FAIL" &&

echo "=== FRAMING removed ===" &&
! grep -q "FRAMING" "$FILE" && echo "PASS: no FRAMING refs" || echo "FAIL: FRAMING still present" &&

echo "=== TILT/SMILE enum added ===" &&
grep -q "TILT.*Phase 1.5" "$FILE" && echo "PASS: TILT" || echo "FAIL" &&
grep -q "SMILE.*Phase 1.5" "$FILE" && echo "PASS: SMILE" || echo "FAIL" &&

echo "=== State transitions count ===" &&
TRANSITIONS=$(grep -c "^|" "$FILE" | head -1)
echo "Transition table rows (expect ~20 with headers): found lines with |" &&

echo "=== SUPPRESSED hardening note ===" &&
grep -q "SUPPRESSED state hardening" "$FILE" && echo "PASS" || echo "FAIL" &&

echo "=== Eligibility-drop transitions ===" &&
grep -q "GUIDANCE_ACTIVE.*FACES_DETECTED_NONE_ELIGIBLE" "$FILE" && echo "PASS" || echo "FAIL" &&
grep -q "ALIGNED.*FACES_DETECTED_NONE_ELIGIBLE" "$FILE" && echo "PASS" || echo "FAIL" &&

echo "=== guidanceState removed from PipelineResult ===" &&
! grep -q "val guidanceState: PortraitGuidanceState" "$FILE" && echo "PASS: removed" ||
(grep -c "val guidanceState" "$FILE" | xargs -I{} echo "Found {} refs — expect only in OverlayModel") &&

echo "=== D-27 landmarks annotation ===" &&
grep -q "D-27: always null in MVP" "$FILE" && echo "PASS" || echo "FAIL" &&

echo "=== Detector prohibition ===" &&
grep -q "MUST NOT be reused" "$FILE" && echo "PASS" || echo "FAIL" &&

echo "=== AF cooldown disambiguation ===" &&
grep -q "NOT interchangeable" "$FILE" && echo "PASS" || echo "FAIL"
```

---

# §D 修改摘要

| ID | 嚴重度 | 操作 | CSO 裁決 |
|----|--------|------|---------|
| **SSOT v4.0.6** | | | |
| HF-A1 | CRITICAL | C-4 LANDMARK_ALL → LANDMARK_NONE | ✅ |
| HF-A2 | MAJOR | v4.0.5 ref → v4.0.6 | ✅ |
| HF-A3 | MEDIUM | B.14 移位至 B.13 後 | ✅ |
| **Contract v0.4** | | | |
| CR-B1 | HIGH | ALIGNED/STABILIZING → SUPPRESSED 入口 | ✅ |
| CR-B2 | HIGH | Eligibility-drop 4 路徑 + debounce | ✅ Perplexity V-1 ✅ |
| CR-B3 | HIGH | FRAMING 移除 | ✅ CSO Option A |
| CR-B4 | MEDIUM | TILT/SMILE Phase 1.5 enum 預留 | ✅ |
| CR-B5 | MEDIUM | PipelineResult.guidanceState 刪除 | ✅ |
| CR-B6 | LOW | landmarks D-27 註解 | ✅ |
| CR-B7 | LOW | Merge basis 更新 | ✅（併入 CR-B8） |
| CR-B8 | LOW | 版本號 + status | ✅ |
| CR-B9 | HIGH | §7.4 + §16 FRAMING 行移除 | ✅ 連動 CR-B3 |
| CR-B11 | MEDIUM | SUPPRESSED 出口 +2 轉換 + 註釋 | ✅ Perplexity V-2 ✅ |
| CR-B12 | LOW | §6.6 不可復用禁令 | ✅ Perplexity V-3 ✅ |
| CR-B13 | LOW | §12.1 cooldown 語義區分 | ✅ Perplexity V-4 ✅ |

**Total**: 3 SSOT + 13 Contract = 16 項修正，全數 CSO signed-off。

---

**End of S12 Executable Amendment**
