# Logcat 日誌檔案為空（找不到 HDR 訊息）— 分析與修正建議

## 問題結論
目前「HDR 相關 logcat 日誌檔案為空/幾乎為空」並不代表 App 沒有輸出 HDR 日誌，更可能是 **收集方式在 Windows/PowerShell 下被編碼與管線效能問題干擾**，再加上 logcat 未在來源端過濾導致輸出量過大，引發 `Unexpected EOF`，最後檔案只剩極少數系統行或空檔。

本文件根據 `LOGCAT_ISSUE_HANDOFF.md` 彙整：提供可重現的診斷流程與「能穩定收集 HDR 日誌」的修正方案。

---

## 觀察到的現象（來自 `LOGCAT_ISSUE_HANDOFF.md`）
- `adb -s HQ616R2713 logcat -d > test_log.txt` 只得到 2 行（`beginning of system` + 1 行系統日誌），沒有任何 App/HDR 訊息。
- 即時監聽時常見：
  - `logcat: Unexpected EOF!`
- 部分輸出檔案為 UTF-16（每字元帶 `\u0000`），導致 `findstr/grep` 搜不到。
- 以 `findstr` / PowerShell `Select-String` 篩選後結果為 0。

---

## 高機率根因

### 1) PowerShell 重導向/輸出編碼導致 UTF-16 檔案
- 在 PowerShell 使用 `>` / `Out-File` 預設會產生 UTF-16（Unicode LE）文字檔。
- 這會讓 `findstr`、部分工具與既有批次流程「看似抓不到」關鍵字。

### 2) 在 PowerShell 管線端做大量過濾與逐行寫檔，導致 logcat 讀取跟不上（引發 EOF）
- 像 `adb logcat ... | Where-Object {...} | ForEach-Object { ...; Out-File -Append ... }` 會把大量輸出丟給 PowerShell 逐行處理與 I/O。
- 若未先在 logcat 端縮小輸出（例如未用 `--pid` 或 `-s ... *:S`），很容易觸發：
  - `unable to read log messages as quickly as they were being produced` → `Unexpected EOF`
- 結果：輸出被中斷，檔案只剩少量行數甚至空檔。

### 3) 未在「來源端」過濾，導致輸出量過大
- 目前多數流程是先抓全量 logcat，再在 PC 端用 `findstr/Where-Object` 過濾。
- 正確作法：**把過濾條件交給 logcat 本身**（tag filter 或 `--pid`），減少輸出量與後處理負擔。

### 4) 可能存在「主機端 logcat filter 環境變數」誤設
- 若主機環境有 `ANDROID_LOG_TAGS=* :S` 類型設定，可能導致幾乎沒有輸出（只剩 `beginning of ...`）。

---

## 修正方案（建議採用順序）

### P0：先用一條指令驗證「logcat 是否能被你抓到」
在裝置上手動打一條 log，再立刻用 logcat 抓取：

```bash
adb -s HQ616R2713 shell "log -t HDR_TEST 'ping'"
adb -s HQ616R2713 logcat -d -v time -s HDR_TEST:I *:S
```

- 若這都抓不到：問題在 adb/logcat 連線或主機環境設定（優先查 `ANDROID_LOG_TAGS`、USB、adb 版本）。
- 若抓得到：logcat 功能正常，問題在「HDR 測試收集流程」。

---

### P0：用 `--pid` + tag filter，在 logcat 端縮小輸出（推薦 CLI 流程）
1) 清 buffer
```bash
adb -s HQ616R2713 logcat -c
```

2) 取得 App PID
```bash
adb -s HQ616R2713 shell pidof -s com.akirachang.ai.photographer
```

3) 只抓該 PID 的 log（同時只留 HDR 相關 tag，其他全關）
```bash
adb -s HQ616R2713 logcat --pid <PID> -v time CameraRepositoryImpl:D LiveCameraSource:D CameraViewModel:D *:S
```

4) **不要在 PowerShell 用 `Where-Object` 過濾全量 logcat**。若要存檔：
- PowerShell（UTF-8）：
```powershell
adb -s HQ616R2713 logcat --pid <PID> -v time CameraRepositoryImpl:D LiveCameraSource:D CameraViewModel:D *:S |
  Tee-Object -FilePath .\hdr_log_utf8.txt -Encoding utf8
```
- 或用 cmd（避免 PowerShell UTF-16 重導向）：
```cmd
cmd /c "adb -s HQ616R2713 logcat --pid <PID> -v time CameraRepositoryImpl:D LiveCameraSource:D CameraViewModel:D *:S > hdr_log.txt"
```

---

### P0：直接在裝置端輸出到檔案，再 `adb pull`（避開主機端編碼/EOF）
```bash
adb -s HQ616R2713 shell "logcat -c"
adb -s HQ616R2713 shell "logcat -v time -f /sdcard/hdr_logcat.txt CameraRepositoryImpl:D LiveCameraSource:D CameraViewModel:D *:S"
```

- 執行 HDR 測試（ON→OFF→AUTO），然後停止 logcat（可拔線或在第二個終端 `adb shell pkill -f logcat` 視裝置支援）。
- 下載檔案：
```bash
adb -s HQ616R2713 pull /sdcard/hdr_logcat.txt .
```

---

### P0：Android Studio Logcat（最省事的驗證路徑）
- Filter：`package:com.akirachang.ai.photographer level:debug`
- 搜尋：`HDR`, `CameraRepositoryImpl`, `LiveCameraSource`, `CameraViewModel`

---

## 主機端快速檢查清單

### 1) 檢查是否有 `ANDROID_LOG_TAGS`
PowerShell：
```powershell
Get-ChildItem Env:ANDROID_LOG_TAGS
```
若存在且過濾過嚴，先清掉（只對目前視窗）：
```powershell
Remove-Item Env:ANDROID_LOG_TAGS
```

### 2) 確認 adb 版本
```bash
adb version
```
若過舊，更新 Android SDK platform-tools。

### 3) 避免 PowerShell `>` 產生 UTF-16
- 要存檔請用 `Out-File -Encoding utf8` 或 `Tee-Object -Encoding utf8`。

---

## 對既有腳本的修正建議（摘要）
- `collect_hdr_logcat.ps1`：
  - 改用 `--pid` + `* :S` 在 logcat 端過濾
  - 用 `Tee-Object -Encoding utf8` 取代逐行 `Out-File -Append`
  - 避免先抓全量再 `Where-Object`
- `get_hdr_log.bat`：
  - 改抓 `adb logcat --pid <PID> ...`（或至少加上 `-s ... *:S`）
- `capture_live_hdr.bat`：
  - 目前的 `timeout ... & adb ...` 並不會「監聽 30 秒後自動停止」，建議改用 PowerShell 控制時間或用 `logcat -t <count>` 取樣。

---

## 驗證成功標準（HDR）
收集到的 log 應包含（示例關鍵字）：
- `Delegating HDR`（Repository/ViewModel 委派）
- `Exposure compensation set to: 1` / `... 0`（LiveCameraSource 真的套用到 CameraControl）
- 不應再出現：`Cannot enable/disable HDR: CameraControl not available`

---

## 下一步
若你希望我直接把上述「腳本修正」套用到 repo（更新 `collect_hdr_logcat.ps1` / `get_hdr_log.bat` 等），我可以依這份建議做最小 diff 修改。
