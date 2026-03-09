# 測試交接管理員 — 雙機測試交接工作流程

> **文件版本**: v1.0
> **適用工具**: `tools/test_handoff_gui.pyw`
> **適用場景**: Home NB 開發完成 → Work NB 實機測試 → 結果回傳 Home NB

---

## 概覽

```
Home NB (開發)          GitHub Remote          Work NB (測試)
    │                        │                       │
    │──Step 1: Push──────────▶│                       │
    │  (commit + push)        │◀──Step 2: Pull────────│
    │                         │                       │ (執行實機測試)
    │                         │◀──Step 3: Push────────│
    │◀──Step 4: Pull──────────│                       │
    │  (開啟報告)              │                       │
```

**原則**：APK 在目標機器上重新編譯，不透過 USB 傳輸。

---

## 前置準備

### 必要目錄結構

```
docs/test_results/<version>/
├── HANDOVER_SUMMARY.md        # 交接摘要（SSOT — 必須存在）
├── <version>_work_nb_execution.md  # 測試執行指南（Work NB 用）
├── PIXEL9A_TEST_REPORT.md     # 實機測試報告（Work NB 產出）
├── *.log                      # Logcat 日誌（Work NB 產出）
└── *.png                      # 截圖（視需要）
```

> ⚠️ `docs/test_results/<version>/` 才會被 Git 追蹤；`tmp/` 已在 `.gitignore`。

### 啟動 GUI

```bash
python tools/test_handoff_gui.pyw
```

---

## Step 1：Home NB 推送（開發機 → GitHub）

**目的**：將測試文件、交接摘要推送到 GitHub，供 Work NB 下載。

### 操作流程

1. 啟動 GUI，切換至「**1. 📤 Home (Push)**」分頁
2. 填寫「任務資訊」區塊：

   | 欄位 | 範例 | 說明 |
   |------|------|------|
   | 任務主題 | `v2.12.0_af_fixes` | 版本或功能代號 |
   | 測試裝置 | `Pixel 9a` | 下拉選擇 |
   | Git 分支 | `feature/camera-core-v2.12.0` | 自動偵測，可手動選 |
   | 測試目標 | 描述本次測試驗證的功能 | 2-3 行 |

3. 點擊「**🔍 檢查 Git 狀態**」確認要提交的檔案
4. 點擊「**🚀 執行推送 (Commit & Push)**」

### GUI 底層執行邏輯

```bash
git add -A
git commit -m "[Docs] Test handoff: {topic}"  \
           -m "Test Device: {device}"          \
           -m "Test Objective: {objective}"
git push github <branch>    # 優先使用 github remote，fallback 到 origin
```

### 推送前人工確認清單

- [ ] `docs/test_results/<version>/HANDOVER_SUMMARY.md` 已撰寫
- [ ] `docs/test_results/<version>/` 執行指南已撰寫
- [ ] `chat_logs/handofflog_<timestamp>.md` 已產出（由交接提示詞工具生成）
- [ ] 無誤刪檔案（先 `git status` 確認）
- [ ] 無 APK / `tmp/` 等大型二進位檔案被納入

### Commit 訊息格式（GUI 自動產出）

```
[Docs] Test handoff: v2.12.0_af_fixes

Test Device: Pixel 9a
Test Objective: Verify focus frame AR fix + OperationCanceledException handling

Co-Authored-By: Claude Sonnet 4.5 (noreply@anthropic.com)
```

---

## Step 2：Work NB 拉取（GitHub → 測試機）

**目的**：Work NB 取得最新測試文件並準備執行測試。

### 操作流程

1. 在 Work NB 啟動 GUI，切換至「**2. 📥 Work (Pull)**」分頁
2. 確認分支與 Home NB 相同
3. 點擊「**⬇️ 拉取更新 (Git Pull)**」

```bash
git pull github <branch>
```

### Pull 成功後

1. 閱讀 `docs/test_results/<version>/HANDOVER_SUMMARY.md`
2. 閱讀測試執行指南
3. 編譯 Device 版本 APK：

```bash
./gradlew assembleDeviceDebug
adb -s <DEVICE_ID> install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

4. 開始執行測試，將 Logcat / 截圖存至 `docs/test_results/<version>/`

---

## Step 3：Work NB 推送（測試結果 → GitHub）

**目的**：測試完成後，將報告、Logcat、截圖推送回 GitHub。

### 操作流程

1. 確認測試產出已存放於 `docs/test_results/<version>/`
2. 切換至「**3. 📤 Work (Push)**」分頁
3. 在「測試結果」下拉選擇：`PASS` / `FAIL` / `PARTIAL`
4. 點擊「**🔍 檢查 Git 狀態**」確認要提交的檔案
5. 點擊「**🚀 推送報告 (Commit & Push)**」

### GUI 底層執行邏輯

```bash
git add -A
git commit -m "[Test] {topic}: Test results - {PASS|FAIL|PARTIAL}"  \
           -m "Device: {device}"                                      \
           -m "Tester: Claude Code (Work NB)"                         \
           -m "Date: {YYYY-MM-DD}"
git push github <branch>
```

### 推送前人工確認清單

- [ ] `HANDOVER_SUMMARY.md` 測試結果表格已填寫
- [ ] Logcat 檔案已命名並存至 `docs/test_results/<version>/`
- [ ] 截圖（如有）已存至 `docs/test_results/<version>/`
- [ ] 若有 FAIL：失敗項目已記錄（TC 編號 + 現象描述）

### 測試結果判斷

| 結果 | 意義 | Home NB 下一步 |
|------|------|---------------|
| `PASS` | 全部 TC 通過 | 繼續下一個迭代版本 |
| `PARTIAL` | 部分通過，發現新 Bug | 帶回 Home NB，Traycer `trigger-workflow` 重啟 PDCA |
| `FAIL` | 主要 TC 失敗 | ⚠️ 架構防禦啟動，帶 Logcat 回 Step 1 |

---

## Step 4：Home NB 拉取並查看報告

**目的**：Home NB 取得測試結果，開啟最新報告。

### 操作流程

1. 切換至「**4. 📥 Home (Pull)**」分頁
2. 點擊「**⬇️ 拉取並開啟報告**」

```bash
git pull github <branch>
# 自動開啟 docs/test_results/ 最新的 .md 檔案（VS Code）
```

---

## 常見問題排除

### Q: Step 1 Push 失敗 — "nothing to commit"

**原因**：所有變更已在上一次 commit 中提交，尚有未推送的 commits。
**處置**：GUI 會自動偵測 `ahead` 狀態並繼續 push，無需手動處理。

### Q: Step 2 Pull 失敗 — "would be overwritten by merge"

**原因**：Work NB 有本地未 commit 的修改與遠端衝突。
**處置**（依嚴重度選擇）：

```bash
# 方法 1（推薦）：暫存本地修改再拉取
git stash && git pull github <branch>

# 方法 2：還原特定衝突檔案
git restore .vscode/tasks.json   # 例如 tasks.json 衝突

# 方法 3（謹慎）：放棄所有本地修改
git reset --hard github/<branch>
```

### Q: Push 到哪個 Remote？

GUI 的 Remote 優先順序：`github` > `origin`（全部步驟一致）。
可在「⚙️ Settings」分頁確認偵測到的 Remote 清單。

---

## 實際案例參考

| 版本 | 測試裝置 | 結果 | 交接文件 |
|------|---------|------|---------|
| v2.10.0-fix | Sony XQ-BT52 + Pixel 9a | ✅ PASS | `docs/test_results/v2.10.0-fix/HANDOVER_SUMMARY.md` |
| v2.12.0-08+09 | Sony XQ-BT52 | ⚠️ PARTIAL | `docs/test_results/v2.12.0/HANDOVER_SUMMARY.md` |
| v2.12.0-10 | Sony XQ-BT52 | ⏳ Pending | `docs/test_results/v2.12.0/HANDOVER_SUMMARY.md` |

---

## 相關文件

| 文件 | 說明 |
|------|------|
| `docs/guides/PYTHON_GUI_GUIDE.md` | GUI 工具詳細使用說明 |
| `devspec/MERGE_SOP.md` | 測試 PASS 後的合併流程 |
| `devspec/ultimate_defensive_hybrid_workflow.md` | PDCA 閉環工作流規範 |
| `CLAUDE.local.md` | 裝置 ID、ADB 快速指令 |
