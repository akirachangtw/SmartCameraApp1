# 分支合併標準作業程序 (SOP)

> **目的**: 提供完整的分支合併流程，防止合併錯誤的分支或遺漏重要修復
> **適用場景**: 所有分支合併操作（feature → feature, feature → develop, develop → main）

---

## 🎯 快速決策樹

```
需要合併分支？
  ↓
步驟 1: 確認來源分支
  ├─ 檢查 devspec/BRANCH_RELEASES.md
  ├─ 確認使用 "-final" tag（最終修正版）
  └─ 使用 origin/<branch> 而非本地分支
  ↓
步驟 2: 文檔同步
  ├─ 執行 devspec/DOCS_SYNC_CHECKLIST.md
  └─ 保留所有決議和測試報告
  ↓
步驟 3: 代碼合併
  ├─ 解決衝突（優先保留最新修復）
  └─ 檢查 feature_record_Backlog.md 的 Critical Warnings
  ↓
步驟 4: 驗證
  ├─ Phase 1-3: Build + 16KB
  └─ Phase 4-5: Testing
  ↓
步驟 5: 記錄
  └─ 更新 BRANCH_RELEASES.md
```

---

## 📋 完整流程：v2.12.0 → main

### Phase 1: 準備階段 (15 分鐘)

#### 1.1 確認來源分支的最終版本

```bash
# 1. 更新遠端信息和標籤
git fetch origin --tags

# 2. 查看所有相關 tags
git tag -l "v2.12*"

# 3. 查看最終版本的詳細信息（以最新 stable tag 為準）
git show v2.12.0

# 4. 確認 tag 指向的 commit
git log origin/feature/camera-core-v2.12.0 --oneline -8
```

**檢查點**:
- [ ] Tag 存在且名稱正確（`v2.12.0` 或 `v2.12.0-final`）
- [ ] Tag 指向遠端分支（`origin/feature/camera-core-v2.12.0`）
- [ ] 最新 commits 包含 v2.12.0-01 ～ v2.12.0-04 的所有功能

#### 1.2 檢查分支版本記錄

```bash
# 閱讀分支版本記錄
cat devspec/BRANCH_RELEASES.md
```

**確認**:
- [ ] v2.12.0 的狀態為「✅ 穩定，可安全合併」
- [ ] 已閱讀「包含功能」清單（v2.12.0-01 ~ -04）
- [ ] 已閱讀「已知問題」（應為「無」）
- [ ] 已閱讀「關鍵提交」清單

#### 1.3 閱讀決議文檔

```bash
# 閱讀 feature_record_Backlog.md 的相關章節
# - v2.12.0 Sony HAL Quirk / analysisSelector cap
# - v2.12.0 Adaptive EWMA throttle
# - v2.12.0 Focus Indicator Dynamic Resize

# 特別注意 Critical Warnings
grep -A 20 "Critical Warning" devspec/feature_record_Backlog.md
```

**確認**:
- [ ] 已閱讀 v2.12.0-01/02 的 analysisSelector + Sony 16:9 HAL 說明
- [ ] 已閱讀 v2.12.0-03 的 EWMA throttle 設計（conflate 陷阱）
- [ ] 已閱讀 v2.12.0-04 的 FocusIndicator 動態縮放說明
- [ ] ⚠️ **已理解** Optical Flow Alpha=0.4 的設計（不要添加 guard）
- [ ] ⚠️ **已理解** Focus Indicator 4 種 Visual Language（禁止新增）

#### 1.4 閱讀測試報告

```bash
# 閱讀 v2.12.0 的測試報告
ls docs/test_results/v2.12.0/
cat docs/test_results/v2.12.0/TEST_REPORT_v2.12.0-02.md
```

**確認**:
- [ ] 所有測試通過（PASSED）
- [ ] 無阻塞性 Bug
- [ ] 實機測試已完成（Pixel 9a: 59131JEBF17244, Sony XQ-BT52: HQ616R2713）

---

### Phase 2: 文檔同步 (20 分鐘)

#### 2.1 執行文檔同步檢查清單

```bash
# 參考文檔同步清單
cat devspec/DOCS_SYNC_CHECKLIST.md
```

#### 2.2 同步 devspec/ 文檔

```bash
# 注意：feature_record_Backlog.md 需要在 merge 後手動合併
# 其他文檔檢查是否有更新
git diff HEAD v2.10.2-final -- devspec/
```

#### 2.3 同步 docs/features/ 文檔

```bash
# ⚠️ v2.11.0+ 起，功能文檔改存放於 docs/features/<版本>/（非舊的 docs/plan/）
# 命名格式：{version}_{topic}-L0.md / -L1.md / -L2.md（prompt_generator_gui 生成）

# 確認 v2.12.0 功能文檔存在
ls docs/features/v2.12.0/

# 預期看到（至少）：
# v2.12.0-01_dynamic_mlkit_fps_throttle_adj-L0/L1/L2.md
# v2.12.0-04_focus_indicator_dynamic_resize-L0/L1/L2.md
# v2.12.0_sony_16_9_camera_low_fps-L0/L1/L2.md
```

**檢查點**:
- [ ] `docs/features/v2.12.0/` 存在且有 L0/L1/L2 文檔
- [ ] check_merge_readiness.py 驗證通過（含 L0 計數）
- [ ] 無遺漏的決議文檔

#### 2.4 同步 docs/test_results/ 文檔

```bash
# 確認測試報告存在
ls docs/test_results/v2.12.0/

# 預期看到：
# TEST_REPORT_v2.12.0-02.md（模擬器 + Sony 驗測報告）
```

**檢查點**:
- [ ] v2.12.0 測試報告存在
- [ ] 含 Pixel 9a 與 Sony XQ-BT52 驗測記錄
- [ ] Logcat 日誌（frameSizePx 驗證記錄）已存

#### 2.5 驗證 tools/ 目錄關鍵工具

合併後確認 `tools/` 的關鍵工具完整：

```bash
# Tier 1 — 必須存在，缺一不可
ls tools/hybrid_flow.py \
   tools/hybrid_flow-tasks.json \
   tools/check_merge_readiness.py \
   tools/test_handoff_gui.pyw \
   tools/prompt_generator_gui.pyw

# 查看 feature branch 是否新增了 tools/ 中的工具
git log --oneline --diff-filter=A -- tools/

# 如有 merge conflict，解決方式
# - hybrid_flow.py / hybrid_flow-tasks.json → 保留 feature branch 版本
# - check_merge_readiness.py → 保留 main 版本（檢查更嚴格）
# - 其他新增工具 → 一律保留（不丟棄 feature branch 新增的工具）

# 驗證 VSCode Tasks 配置（.vscode/tasks.json 被強制加入 Git）
grep -c "label" .vscode/tasks.json  # 應 >= 10 個 task
grep "Workflow Prompt Generator" .vscode/tasks.json  # 🤖 Workflow Prompt Generator task
grep "Test Handoff Manager" .vscode/tasks.json       # 🚀 Test Handoff Manager task
grep "Hybrid: L1" .vscode/tasks.json                 # 📋 Hybrid L1/L2/PR tasks
```

**詳細清單**: `tools/.gitkeep_critical_files`（三級分類：Tier 1 / 2 / 3）

**VSCode Tasks 保護清單** (`.vscode/tasks.json`):
| Task | 用途 |
|------|------|
| 🚀 Test Handoff Manager | Home↔Work NB 測試交接 GUI |
| 🧪 Stage Verification GUI | 階段驗證 GUI |
| 🤖 Workflow Prompt Generator | 終極防禦型混合工作流提示詞生成器（Custom Workflow 版：trigger-workflow→Gemini L2→ticket_breakdown/execute→Claude+strict_verification） |
| 📋 Hybrid: L1/L2/PR (inline + file) | L1/L2/PR 計劃文檔生成 |

> 📖 **工作流規範**: `devspec/ultimate_defensive_hybrid_workflow.md`（2026-03-09 升級至 Custom Workflow 版）
> Claude Code 在此工作流中扮演 **The Finisher**，僅依 Traycer `execute` 的 Handoff Instructions 執行單一 Ticket，完成後必須暫停等待 Traycer `strict_verification` 驗收。

**檢查點**:
- [ ] Tier 1 工具（5 個）全部存在
- [ ] feature branch 新增的工具未因 merge 遺失
- [ ] 無 tools/ 相關 merge conflict 遺留
- [ ] `.vscode/tasks.json` 包含 Workflow Prompt Generator + Hybrid Tasks
- [ ] `devspec/ultimate_defensive_hybrid_workflow.md` 存在（終極防禦工作流規範）

---

#### 2.6 創建文檔同步記錄

```bash
# 創建同步記錄
cat > devspec/DOCS_SYNC_LOG_$(date +%Y%m%d_%H%M%S).md <<EOF
# 文檔同步記錄

- **日期**: $(date '+%Y-%m-%d %H:%M:%S')
- **來源**: v2.10.2-final
- **目標**: feature/camera-core-v2.11.0
- **執行者**: [Your Name]

## 已同步文檔
- docs/plan/v2.10.0_* (全部)
- docs/test_results/v2.10.0/ (全部)
- docs/test_results/v2.10.2/ (全部)

## 待合併文檔（merge 時處理）
- devspec/feature_record_Backlog.md

## 驗證
- [ ] 所有計劃文檔已複製
- [ ] 所有測試報告已複製
- [ ] 無遺漏的決議文檔
EOF

# 提交文檔同步
git add docs/plan/ docs/test_results/ devspec/DOCS_SYNC_LOG_*
git commit -m "[Docs] Sync v2.10.2-final documentation to v2.11.0

同步 v2.10.0/v2.10.2 的決議文檔和測試報告

- docs/plan/v2.10.0_* (全部計劃文檔)
- docs/test_results/v2.10.0/ (測試報告)
- docs/test_results/v2.10.2/ (測試報告 + Logcat)

參考: devspec/DOCS_SYNC_CHECKLIST.md"
```

---

### Phase 3: 代碼合併 (30 分鐘)

#### 3.1 撤銷錯誤的合併（如果有）

```bash
# 檢查當前狀態
git log --oneline -5

# 如果有錯誤的合併（合併了本地分支而非遠端分支），撤銷
git reset --hard HEAD~1

# 確認已撤銷
git log --oneline -5
```

**檢查點**:
- [ ] 錯誤的合併已撤銷
- [ ] 當前分支狀態正確

#### 3.2 更新遠端信息

```bash
# 更新所有遠端分支的信息
git fetch origin --tags

# 確認遠端分支包含所有 v2.12.0 commits
git log origin/feature/camera-core-v2.12.0 --oneline -10
```

**檢查點**:
- [ ] 遠端分支包含 v2.12.0-04 的最新 commits（`22b0d02`, `610d143`）
- [ ] 遠端分支包含 v2.12.0-03 EWMA throttle commit（`bdc65c0`）

#### 3.3 執行合併

```bash
# 方法 1: 使用 tag（推薦，待打 tag 後）
git merge v2.12.0-final --no-edit

# 方法 2: 使用遠端分支
git merge origin/feature/camera-core-v2.12.0 --no-edit
```

**如果有衝突**，參考下一步。

#### 3.4 解決衝突

**常見衝突檔案**:
1. `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt`
2. `app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraState.kt`
3. `devspec/feature_record_Backlog.md`

**解決策略**:

**CameraScreen.kt / CameraState.kt / CameraViewModel.kt**:
```bash
# 策略：保留 feature/camera-core-v2.12.0 版本（包含最新修復）
git checkout --theirs app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt
git checkout --theirs app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraState.kt
git checkout --theirs app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt
git add app/src/main/java/com/akirachang/ai/photographer/presentation/camera/
```

**feature_record_Backlog.md**:
```bash
# 策略：保留兩邊的內容（main + v2.12.0）
# 使用當前分支（v2.12.0）的版本（包含所有章節）
git checkout --ours devspec/feature_record_Backlog.md
git add devspec/feature_record_Backlog.md
```

**檢查點**:
- [ ] 所有衝突已解決
- [ ] `StiffnessHigh` 保留於 position tracking（CameraScreen.kt）
- [ ] `focusFrameSizePx` 欄位存在（CameraState.kt）
- [ ] `VISUAL_PADDING_SCALE = 1.5f` 保留（CameraViewModel.kt）
- [ ] feature_record_Backlog.md 包含所有章節

#### 3.5 完成合併

```bash
# 提交合併
git commit -m "$(cat <<'EOF'
Merge origin/feature/camera-core-v2.12.0 into main

整合 v2.12.0-01 ~ v2.12.0-04 的完整功能到 main

## 整合內容

### v2.12.0-01
- ✅ Remove 1:1 Aspect Ratio + analysisSelector cap ≤640×480

### v2.12.0-02
- ✅ Remove analysisSelector setAspectRatioStrategy (Sony HAL quirk fix)

### v2.12.0-03
- ✅ Adaptive ML Kit throttle (EWMA) in CameraViewModel device mode
- ✅ .sample() replaces conflate() to avoid BitmapPool reuse issue

### v2.12.0-04
- ✅ FocusIndicator adaptive frame 48-120dp from ML Kit bounding box
- ✅ VISUAL_PADDING_SCALE=1.5 for normal-distance visibility
- ✅ Spring animation (DampingRatioMediumBouncy + StiffnessMediumLow)

## 文檔同步
- ✅ docs/features/v2.12.0/ (L0/L1/L2 文檔共 9 個)
- ✅ docs/test_results/v2.12.0/ (測試報告)

來源: origin/feature/camera-core-v2.12.0
參考: devspec/MERGE_SOP.md

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Phase 4: 驗證 (45 分鐘)

#### 4.1 Phase 2: Build 驗證

```bash
# Clean build
./gradlew clean

# Build Device APK
./gradlew assembleDeviceDebug
```

**檢查點**:
- [ ] Build 成功
- [ ] 無 Build ERROR
- [ ] Build 時間: ______ 秒

#### 4.2 Phase 3: 16KB Compatibility

```bash
# 執行 16KB 對齊檢查
bash docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh \
  app/build/outputs/apk/device/debug/app-device-debug.apk
```

**檢查點**:
- [ ] `ELF Verification Successful`

#### 4.2b Phase 4: Emulator Testing（正確操作流程）

> ⚠️ **經驗記錄（2026-03-01）**：以下為實際測試驗證的正確指令，避免重踩錯誤操作。

##### Step 1：Build Emulator APK

```bash
# Emulator 與 Device 使用不同 flavor，必須分別 Build
./gradlew assembleEmulatorDebug

# APK 路徑（與 Device 不同）
# app/build/outputs/apk/emulator/debug/app-emulator-debug.apk
```

##### Step 2：啟動模擬器（Bash 正確方式）

```bash
# ✅ 正確：使用 & 背景執行（不佔用 terminal）
emulator -avd Pixel_5 -gpu host &

# ❌ 錯誤：不要用以下方式（會造成 terminal 卡住或被 user 拒絕）
# cmd /c "start /min emulator..."          ← Windows cmd，在 bash 環境不一致
# Start-Process emulator ... -NoNewWindow  ← PowerShell 語法，bash 不適用
```

> 可用 AVD 清單：`emulator -list-avds`
> 目前專案可用：`MPhone_A11`, `Medium_Phone_API_36.1`, `Pixel9a-16KB`, `Pixel9a-virtual`, `Pixel_5`

##### Step 3：等待開機完成

```bash
# 輪詢 boot_completed（不要用 sleep 死等）
for i in 1 2 3 4 5 6 7 8 9 10; do
  BOOT=$(adb -s emulator-5554 shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  echo "  boot_completed=$BOOT attempt $i/10"
  [ "$BOOT" = "1" ] && echo "✅ Boot completed!" && break
  sleep 5
done

# 確認裝置上線
adb devices
# 預期: emulator-5554    device
```

##### Step 4：安裝 APK 並授予權限

```bash
# 安裝（注意：Emulator APK 路徑）
adb -s emulator-5554 install -r app/build/outputs/apk/emulator/debug/app-emulator-debug.apk

# ⚠️ Emulator package 名稱與 Device 不同！
# Device:   com.akirachang.ai.photographer
# Emulator: com.akirachang.ai.photographer.emulator

# 一次性授予所有必要權限（避免每次點選彈窗）
adb -s emulator-5554 shell pm grant com.akirachang.ai.photographer.emulator android.permission.CAMERA
adb -s emulator-5554 shell pm grant com.akirachang.ai.photographer.emulator android.permission.READ_MEDIA_IMAGES 2>/dev/null
adb -s emulator-5554 shell pm grant com.akirachang.ai.photographer.emulator android.permission.READ_EXTERNAL_STORAGE 2>/dev/null
```

##### Step 5：啟動 App

```bash
# ✅ 正確：使用 monkey（自動找 LAUNCHER activity，不需知道完整 class 名稱）
adb -s emulator-5554 shell "monkey -p com.akirachang.ai.photographer.emulator -c android.intent.category.LAUNCHER 1"

# ❌ 錯誤：不要用 am start -n，Emulator flavor 的 Activity class 路徑與 Device 不同
# adb -s emulator-5554 shell am start -n com.akirachang.ai.photographer.emulator/.MainActivity
# → Error type 3: Activity class does not exist
```

##### Step 6：截圖（正確方法）

```bash
# ⚠️ 遵守 CLAUDE.md ADB 截圖規範（禁止直接 > 重導向）
adb -s emulator-5554 shell "screencap -p > /sdcard/Download/screenshot.png"
adb -s emulator-5554 pull //sdcard//Download//screenshot.png tmp/screenshot.png
adb -s emulator-5554 shell "rm /sdcard/Download/screenshot.png"
```

**檢查點**:
- [ ] Emulator APK Build 成功（`assembleEmulatorDebug`）
- [ ] 模擬器以 `emulator -avd <name> -gpu host &` 背景啟動
- [ ] `sys.boot_completed=1` 確認開機完成
- [ ] Package 名稱使用 `com.akirachang.ai.photographer.emulator`
- [ ] App 以 `monkey` 指令啟動（非 `am start -n`）
- [ ] App 功能正常、無 Crash

---

#### 4.3 Phase 5: Device Testing

```bash
# 安裝新版本（Pixel 9a）
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 安裝新版本（Sony XQ-BT52）
adb -s HQ616R2713 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 驗證版本號
adb -s 59131JEBF17244 shell dumpsys package com.akirachang.ai.photographer | grep version
# 預期：versionName=2.12.0-04  versionCode=85
```

**功能驗證**:
- [ ] **v2.12.0-01/02**: analysisSelector cap 生效，Sony 16:9 低 FPS 修復
- [ ] **v2.12.0-03**: EWMA throttle log 可見（`[v2.12.0-03] throttle=Xms`）
- [ ] **v2.12.0-04**: FocusIndicator 框隨人臉距離縮放（48-120dp）
- [ ] **Eye-AF**: 對焦點落在眼睛位置（非臉部中心）
- [ ] **Smart Subject Focus**: `frameSizePx` log 值正確（正常距離 >130px）
- [ ] **FocusClamp**: 機械夾緊動畫 3-phase 正常（shrink→expand→settle）

---

### Phase 5: 記錄 (10 分鐘)

#### 5.1 更新分支版本記錄

編輯 `devspec/BRANCH_RELEASES.md`：

```markdown
### v2.12.0 (2026-03-XX) ✅ 穩定
- **Status**: ✅ 穩定，v2.12.0-01 ~ v2.12.0-04 全部完成
- **版本號**: 2.12.0-04 (versionCode=85)
- **包含功能**:
  - v2.12.0-01: Remove 1:1 AR + analysisSelector cap ≤640×480
  - v2.12.0-02: Remove analysisSelector setAspectRatioStrategy (Sony HAL)
  - v2.12.0-03: Adaptive EWMA throttle (device mode)
  - v2.12.0-04: FocusIndicator adaptive 48-120dp (VISUAL_PADDING_SCALE=1.5)
- 測試狀態:
  - ✅ Emulator (Pixel_5): ELF 16KB PASS, 無 crash
  - ✅ Sony XQ-BT52 (HQ616R2713): frameSizePx 驗測 PASS
  - ✅ Pixel 9a (59131JEBF17244): PASS
```

#### 5.2 推送到遠端

```bash
# 打 final tag
git tag v2.12.0-final

# 推送分支
git push origin feature/camera-core-v2.12.0

# 推送 tag
git push origin --tags
```

---

## ⚠️ 重要提醒

### 永遠使用遠端分支

```bash
# ✅ 正確
git merge origin/feature-name
git merge v2.10.2-final  # 使用 tag

# ❌ 錯誤
git merge feature-name  # 可能是舊的本地分支
```

### 檢查 Critical Warnings

合併前**必須閱讀** `devspec/feature_record_Backlog.md` 的所有 Critical Warnings。

### 保留測試報告

所有測試報告都是功能驗證的證據，必須完整保留。

---

**最後更新**: 2026-03-07（Phase 2.5 更新：Workflow Prompt Generator 升級至 v4 終極防禦閉環版）
**維護者**: Claude Sonnet 4.6 + Akira Chang
