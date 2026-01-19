# ADB 截圖異常問題與解決方案

## 問題描述
在 Windows 環境下使用 PowerShell 或 CMD 配合 `>` 重導向符號來導出 `adb shell screencap` 的資料時，會造成生成的 PNG 圖片損壞無法開啟。

### 發生原因
1. **換行符號轉換 (Newlines Conversion)**：`adb shell` 會建立一個偽終端機 (PTY)，自動將 Unix 換行符號 (`0x0A` LF) 轉換為 Windows 換行符號 (`0x0D 0x0A` CRLF)。這會破壞 PNG 的二進位資料結構。
2. **PowerShell 編碼**：PowerShell 的 `>` 重導向通常會嘗試將輸出的二進位資料視為文字進行編碼處理 (例如 UTF-16)，導致二進位內容完全損壞。

## 解決方案 (推薦)

最穩定且保險的做法是**先將截圖存入手機暫存空間，再透過 `pull` 指令拉回電腦**。這樣可以完全避免 Windows Shell 的編碼與換行轉換干擾。

### 操作步驟 (PowerShell 腳本)

您可以直接在 PowerShell 中執行以下指令：

```powershell
# 1. 產生帶時間戳記的檔案名稱
$filename = "tmp/sony_screen_$(Get-Date -Format 'yyyyMMdd_HHmmss').png"

# 2. 在手機上截圖並存檔 (不走 pipe，直接存入 sdcard)
# 注意：這裡使用 -s HQ616R2713 指定特定裝置，請視情況調整
adb -s HQ616R2713 shell screencap -p /sdcard/temp_screen.png

# 3. 將圖片拉回電腦
adb -s HQ616R2713 pull /sdcard/temp_screen.png $filename

# 4. 刪除手機上的暫存檔
adb -s HQ616R2713 shell rm /sdcard/temp_screen.png

Write-Host "截圖已儲存至: $filename"
```

這種方法雖然多了一個步驟，但在跨平台 (Windows/Mac/Linux) 與跨 Shell (Cmd/PowerShell/Bash) 的環境下都能保證 100% 的可靠性。
