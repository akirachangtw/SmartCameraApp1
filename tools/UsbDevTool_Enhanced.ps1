# UsbDevTool_Enhanced.ps1
# 美化版 ADB USB 偵錯 / 螢幕常亮 控制工具 (PowerShell + WinForms)

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

#-------------------------
# 色彩配置
#-------------------------
$Colors = @{
    Primary      = [System.Drawing.Color]::FromArgb(41, 128, 185)   # 藍色
    PrimaryDark  = [System.Drawing.Color]::FromArgb(30, 96, 145)    # 深藍
    Success      = [System.Drawing.Color]::FromArgb(39, 174, 96)    # 綠色
    Warning      = [System.Drawing.Color]::FromArgb(243, 156, 18)   # 橙色
    Danger       = [System.Drawing.Color]::FromArgb(231, 76, 60)    # 紅色
    Background   = [System.Drawing.Color]::FromArgb(236, 240, 241)  # 淺灰
    CardBg       = [System.Drawing.Color]::White                     # 白色
    TextDark     = [System.Drawing.Color]::FromArgb(44, 62, 80)     # 深灰文字
    TextLight    = [System.Drawing.Color]::White                     # 白色文字
    Border       = [System.Drawing.Color]::FromArgb(189, 195, 199)  # 邊框灰
}

#-------------------------
# 共用：執行 adb 並回傳輸出
#-------------------------
function Invoke-Adb {
    param(
        [string]$Arguments,
        [string]$Serial
    )

    $adbPath = "adb"   # 如需指定完整路徑，可改為: "C:\Android\platform-tools\adb.exe"

    if ($Serial -and $Serial.Trim() -ne "") {
        $Arguments = "-s $Serial $Arguments"
    }

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $adbPath
    $psi.Arguments = $Arguments
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError  = $true
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true

    try {
        $proc = [System.Diagnostics.Process]::Start($psi)
        $stdout = $proc.StandardOutput.ReadToEnd()
        $stderr = $proc.StandardError.ReadToEnd()
        $proc.WaitForExit()

        if ($stderr -and $stderr.Trim() -ne "") {
            return @{
                Success = $false
                Output = $stdout
                Error = $stderr
            }
        } else {
            return @{
                Success = $true
                Output = $stdout
                Error = ""
            }
        }
    }
    catch {
        return @{
            Success = $false
            Output = ""
            Error = "無法執行 adb：$($_.Exception.Message)"
        }
    }
}

#-------------------------
# 輔助函數：寫入彩色日誌
#-------------------------
function Write-ColorLog {
    param(
        [string]$Message,
        [string]$Type = "Info"  # Info, Success, Warning, Error
    )

    $timestamp = Get-Date -Format "HH:mm:ss"
    $prefix = switch ($Type) {
        "Success" { "[✓]" }
        "Warning" { "[!]" }
        "Error"   { "[✗]" }
        default   { "[i]" }
    }

    $logMessage = "[$timestamp] $prefix $Message`r`n"
    $textLog.AppendText($logMessage)
    $textLog.SelectionStart = $textLog.Text.Length
    $textLog.ScrollToCaret()
}

#-------------------------
# 主視窗
#-------------------------
$form = New-Object System.Windows.Forms.Form
$form.Text = "ADB 裝置管理工具"
$form.StartPosition = "CenterScreen"
$form.Size = New-Object System.Drawing.Size(800, 600)
$form.MaximizeBox = $false
$form.BackColor = $Colors.Background
$form.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9)

#-------------------------
# 標題列
#-------------------------
$panelHeader = New-Object System.Windows.Forms.Panel
$panelHeader.Location = New-Object System.Drawing.Point(0, 0)
$panelHeader.Size = New-Object System.Drawing.Size(800, 60)
$panelHeader.BackColor = $Colors.Primary

$labelTitle = New-Object System.Windows.Forms.Label
$labelTitle.Text = "ADB 裝置管理工具"
$labelTitle.Location = New-Object System.Drawing.Point(20, 15)
$labelTitle.Size = New-Object System.Drawing.Size(400, 30)
$labelTitle.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 16, [System.Drawing.FontStyle]::Bold)
$labelTitle.ForeColor = $Colors.TextLight
$labelTitle.AutoSize = $true

$panelHeader.Controls.Add($labelTitle)

#-------------------------
# 裝置選擇區域（卡片樣式）
#-------------------------
$groupDevice = New-Object System.Windows.Forms.GroupBox
$groupDevice.Text = " 裝置連線 "
$groupDevice.Location = New-Object System.Drawing.Point(20, 75)
$groupDevice.Size = New-Object System.Drawing.Size(750, 90)
$groupDevice.BackColor = $Colors.CardBg
$groupDevice.ForeColor = $Colors.TextDark
$groupDevice.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

# 裝置標籤
$labelDevices = New-Object System.Windows.Forms.Label
$labelDevices.Text = "已連線裝置："
$labelDevices.Location = New-Object System.Drawing.Point(15, 30)
$labelDevices.AutoSize = $true
$labelDevices.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9)

# 裝置下拉選單
$comboDevices = New-Object System.Windows.Forms.ComboBox
$comboDevices.Location = New-Object System.Drawing.Point(110, 27)
$comboDevices.Size = New-Object System.Drawing.Size(300, 25)
$comboDevices.DropDownStyle = "DropDownList"
$comboDevices.Font = New-Object System.Drawing.Font("Consolas", 9)

# 重新整理裝置按鈕
$btnRefreshDevices = New-Object System.Windows.Forms.Button
$btnRefreshDevices.Text = "🔄 重新整理"
$btnRefreshDevices.Location = New-Object System.Drawing.Point(425, 25)
$btnRefreshDevices.Size = New-Object System.Drawing.Size(110, 30)
$btnRefreshDevices.BackColor = $Colors.Primary
$btnRefreshDevices.ForeColor = $Colors.TextLight
$btnRefreshDevices.FlatStyle = "Flat"
$btnRefreshDevices.FlatAppearance.BorderSize = 0
$btnRefreshDevices.Cursor = [System.Windows.Forms.Cursors]::Hand

# 重啟 ADB 按鈕
$btnRestartAdb = New-Object System.Windows.Forms.Button
$btnRestartAdb.Text = "🔧 重啟 ADB"
$btnRestartAdb.Location = New-Object System.Drawing.Point(545, 25)
$btnRestartAdb.Size = New-Object System.Drawing.Size(110, 30)
$btnRestartAdb.BackColor = $Colors.Warning
$btnRestartAdb.ForeColor = $Colors.TextLight
$btnRestartAdb.FlatStyle = "Flat"
$btnRestartAdb.FlatAppearance.BorderSize = 0
$btnRestartAdb.Cursor = [System.Windows.Forms.Cursors]::Hand

# 裝置資訊標籤
$labelDeviceInfo = New-Object System.Windows.Forms.Label
$labelDeviceInfo.Text = "裝置資訊：未選擇裝置"
$labelDeviceInfo.Location = New-Object System.Drawing.Point(15, 60)
$labelDeviceInfo.Size = New-Object System.Drawing.Size(700, 20)
$labelDeviceInfo.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 8)
$labelDeviceInfo.ForeColor = [System.Drawing.Color]::Gray

$groupDevice.Controls.AddRange(@(
    $labelDevices,
    $comboDevices,
    $btnRefreshDevices,
    $btnRestartAdb,
    $labelDeviceInfo
))

#-------------------------
# 螢幕控制區域（卡片樣式）
#-------------------------
$groupControl = New-Object System.Windows.Forms.GroupBox
$groupControl.Text = " 螢幕控制 "
$groupControl.Location = New-Object System.Drawing.Point(20, 175)
$groupControl.Size = New-Object System.Drawing.Size(750, 80)
$groupControl.BackColor = $Colors.CardBg
$groupControl.ForeColor = $Colors.TextDark
$groupControl.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

$labelControlInfo = New-Object System.Windows.Forms.Label
$labelControlInfo.Text = "控制手機螢幕常亮設定與休眠（需先啟用 USB 偵錯）"
$labelControlInfo.Location = New-Object System.Drawing.Point(15, 25)
$labelControlInfo.Size = New-Object System.Drawing.Size(720, 20)
$labelControlInfo.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 8)
$labelControlInfo.ForeColor = [System.Drawing.Color]::Gray

# 啟用常亮按鈕
$btnDevOn = New-Object System.Windows.Forms.Button
$btnDevOn.Text = "✓ 啟用常亮"
$btnDevOn.Location = New-Object System.Drawing.Point(150, 45)
$btnDevOn.Size = New-Object System.Drawing.Size(130, 25)
$btnDevOn.BackColor = $Colors.Success
$btnDevOn.ForeColor = $Colors.TextLight
$btnDevOn.FlatStyle = "Flat"
$btnDevOn.FlatAppearance.BorderSize = 0
$btnDevOn.Cursor = [System.Windows.Forms.Cursors]::Hand
$btnDevOn.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

# 停用常亮按鈕
$btnDevOff = New-Object System.Windows.Forms.Button
$btnDevOff.Text = "✗ 停用常亮"
$btnDevOff.Location = New-Object System.Drawing.Point(310, 45)
$btnDevOff.Size = New-Object System.Drawing.Size(130, 25)
$btnDevOff.BackColor = $Colors.Danger
$btnDevOff.ForeColor = $Colors.TextLight
$btnDevOff.FlatStyle = "Flat"
$btnDevOff.FlatAppearance.BorderSize = 0
$btnDevOff.Cursor = [System.Windows.Forms.Cursors]::Hand
$btnDevOff.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

# 螢幕休眠按鈕
$btnSleepScreen = New-Object System.Windows.Forms.Button
$btnSleepScreen.Text = "💤 螢幕休眠"
$btnSleepScreen.Location = New-Object System.Drawing.Point(470, 45)
$btnSleepScreen.Size = New-Object System.Drawing.Size(130, 25)
$btnSleepScreen.BackColor = $Colors.Warning
$btnSleepScreen.ForeColor = $Colors.TextDark
$btnSleepScreen.FlatStyle = "Flat"
$btnSleepScreen.FlatAppearance.BorderSize = 0
$btnSleepScreen.Cursor = [System.Windows.Forms.Cursors]::Hand
$btnSleepScreen.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

$groupControl.Controls.AddRange(@(
    $labelControlInfo,
    $btnDevOn,
    $btnDevOff,
    $btnSleepScreen
))

#-------------------------
# 應用程式權限控制區域（卡片樣式）
#-------------------------
$groupAppPerms = New-Object System.Windows.Forms.GroupBox
$groupAppPerms.Text = " 應用程式權限控制 "
$groupAppPerms.Location = New-Object System.Drawing.Point(20, 265)
$groupAppPerms.Size = New-Object System.Drawing.Size(750, 80)
$groupAppPerms.BackColor = $Colors.CardBg
$groupAppPerms.ForeColor = $Colors.TextDark
$groupAppPerms.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

$labelAppPermsInfo = New-Object System.Windows.Forms.Label
$labelAppPermsInfo.Text = "授予特定應用程式的系統權限（需先啟用 USB 偵錯）"
$labelAppPermsInfo.Location = New-Object System.Drawing.Point(15, 25)
$labelAppPermsInfo.Size = New-Object System.Drawing.Size(720, 20)
$labelAppPermsInfo.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 8)
$labelAppPermsInfo.ForeColor = [System.Drawing.Color]::Gray

# TeamViewer 媒體權限按鈕
$btnTeamViewerMedia = New-Object System.Windows.Forms.Button
$btnTeamViewerMedia.Text = "📱 授予 TeamViewer 媒體權限"
$btnTeamViewerMedia.Location = New-Object System.Drawing.Point(250, 45)
$btnTeamViewerMedia.Size = New-Object System.Drawing.Size(250, 25)
$btnTeamViewerMedia.BackColor = $Colors.Primary
$btnTeamViewerMedia.ForeColor = $Colors.TextLight
$btnTeamViewerMedia.FlatStyle = "Flat"
$btnTeamViewerMedia.FlatAppearance.BorderSize = 0
$btnTeamViewerMedia.Cursor = [System.Windows.Forms.Cursors]::Hand
$btnTeamViewerMedia.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

$groupAppPerms.Controls.AddRange(@(
    $labelAppPermsInfo,
    $btnTeamViewerMedia
))

#-------------------------
# 日誌區域
#-------------------------
$groupLog = New-Object System.Windows.Forms.GroupBox
$groupLog.Text = " 操作日誌 "
$groupLog.Location = New-Object System.Drawing.Point(20, 355)
$groupLog.Size = New-Object System.Drawing.Size(750, 145)
$groupLog.BackColor = $Colors.CardBg
$groupLog.ForeColor = $Colors.TextDark
$groupLog.Font = New-Object System.Drawing.Font("Microsoft JhengHei UI", 9, [System.Drawing.FontStyle]::Bold)

$textLog = New-Object System.Windows.Forms.TextBox
$textLog.Location = New-Object System.Drawing.Point(10, 20)
$textLog.Size = New-Object System.Drawing.Size(730, 115)
$textLog.Multiline = $true
$textLog.ScrollBars = "Vertical"
$textLog.ReadOnly = $true
$textLog.BackColor = [System.Drawing.Color]::FromArgb(250, 250, 250)
$textLog.Font = New-Object System.Drawing.Font("Consolas", 9)
$textLog.BorderStyle = "FixedSingle"

$groupLog.Controls.Add($textLog)

#-------------------------
# 底部按鈕
#-------------------------
$btnClearLog = New-Object System.Windows.Forms.Button
$btnClearLog.Text = "清除日誌"
$btnClearLog.Location = New-Object System.Drawing.Point(570, 515)
$btnClearLog.Size = New-Object System.Drawing.Size(100, 35)
$btnClearLog.BackColor = $Colors.Border
$btnClearLog.ForeColor = $Colors.TextDark
$btnClearLog.FlatStyle = "Flat"
$btnClearLog.Cursor = [System.Windows.Forms.Cursors]::Hand

$btnExit = New-Object System.Windows.Forms.Button
$btnExit.Text = "關閉"
$btnExit.Location = New-Object System.Drawing.Point(680, 515)
$btnExit.Size = New-Object System.Drawing.Size(90, 35)
$btnExit.BackColor = $Colors.Danger
$btnExit.ForeColor = $Colors.TextLight
$btnExit.FlatStyle = "Flat"
$btnExit.FlatAppearance.BorderSize = 0
$btnExit.Cursor = [System.Windows.Forms.Cursors]::Hand

# 狀態列
$statusStrip = New-Object System.Windows.Forms.StatusStrip
$statusLabel = New-Object System.Windows.Forms.ToolStripStatusLabel
$statusLabel.Text = "就緒"
$statusStrip.Items.Add($statusLabel) | Out-Null
$statusStrip.BackColor = $Colors.Background

#-------------------------
# 將所有控制項加入表單
#-------------------------
$form.Controls.AddRange(@(
    $panelHeader,
    $groupDevice,
    $groupControl,
    $groupAppPerms,
    $groupLog,
    $btnClearLog,
    $btnExit,
    $statusStrip
))

#-------------------------
# 功能：更新裝置列表
#-------------------------
function Update-DeviceList {
    Write-ColorLog "重新整理裝置列表..." "Info"
    $statusLabel.Text = "正在掃描裝置..."

    $result = Invoke-Adb -Arguments "devices"

    $comboDevices.Items.Clear()
    $labelDeviceInfo.Text = "裝置資訊：未選擇裝置"

    if (-not $result.Success) {
        Write-ColorLog "錯誤：$($result.Error)" "Error"
        $statusLabel.Text = "裝置掃描失敗"
        return
    }

    # 解析裝置列表
    $lines = $result.Output -split "`r?`n"
    $deviceCount = 0

    foreach ($line in $lines) {
        if ($line -match "device$" -and $line -notmatch "List of devices attached") {
            $serial = $line.Split("`t")[0].Trim()
            if (-not [string]::IsNullOrWhiteSpace($serial)) {
                [void]$comboDevices.Items.Add($serial)
                $deviceCount++
            }
        }
    }

    if ($comboDevices.Items.Count -gt 0) {
        $comboDevices.SelectedIndex = 0
        Write-ColorLog "找到 $deviceCount 台裝置" "Success"
        $statusLabel.Text = "已連線 $deviceCount 台裝置"
    } else {
        Write-ColorLog "未找到任何裝置，請確認 USB 偵錯已啟用" "Warning"
        $statusLabel.Text = "未找到裝置"
    }
}

#-------------------------
# 功能：取得裝置資訊
#-------------------------
function Get-DeviceInfo {
    param([string]$Serial)

    if ([string]::IsNullOrWhiteSpace($Serial)) {
        return
    }

    $statusLabel.Text = "正在取得裝置資訊..."

    # 取得裝置型號
    $modelResult = Invoke-Adb -Arguments "shell getprop ro.product.model" -Serial $Serial
    $model = if ($modelResult.Success) { $modelResult.Output.Trim() } else { "未知" }

    # 取得 Android 版本
    $versionResult = Invoke-Adb -Arguments "shell getprop ro.build.version.release" -Serial $Serial
    $version = if ($versionResult.Success) { $versionResult.Output.Trim() } else { "未知" }

    $labelDeviceInfo.Text = "裝置資訊：$model | Android $version | 序號：$Serial"
    Write-ColorLog "裝置：$model (Android $version)" "Info"

    $statusLabel.Text = "就緒"
}

#-------------------------
# 事件綁定
#-------------------------

# 裝置選擇改變
$comboDevices.Add_SelectedIndexChanged({
    if ($comboDevices.SelectedItem -ne $null) {
        Get-DeviceInfo -Serial $comboDevices.SelectedItem.ToString()
    }
})

# 重新整理裝置
$btnRefreshDevices.Add_Click({
    Update-DeviceList
})

# 重啟 ADB server
$btnRestartAdb.Add_Click({
    Write-ColorLog "正在重啟 ADB 伺服器..." "Info"
    $statusLabel.Text = "重啟 ADB 伺服器中..."

    $result1 = Invoke-Adb -Arguments "kill-server"
    Start-Sleep -Milliseconds 500
    $result2 = Invoke-Adb -Arguments "start-server"

    if ($result2.Success) {
        Write-ColorLog "ADB 伺服器重啟成功" "Success"
    } else {
        Write-ColorLog "ADB 伺服器重啟失敗：$($result2.Error)" "Error"
    }

    Update-DeviceList
})

# 啟用：USB 常亮
$btnDevOn.Add_Click({
    if ($comboDevices.SelectedItem -eq $null) {
        [System.Windows.Forms.MessageBox]::Show(
            "請先選擇一台裝置。",
            "提示",
            [System.Windows.Forms.MessageBoxButtons]::OK,
            [System.Windows.Forms.MessageBoxIcon]::Information
        ) | Out-Null
        return
    }

    $serial = $comboDevices.SelectedItem.ToString()
    Write-ColorLog "正在啟用 USB 充電時螢幕常亮..." "Info"
    $statusLabel.Text = "執行中..."

    $result = Invoke-Adb -Arguments "shell svc power stayon usb" -Serial $serial

    if ($result.Success) {
        Write-ColorLog "已成功啟用螢幕常亮功能" "Success"
        $statusLabel.Text = "螢幕常亮已啟用"
    } else {
        Write-ColorLog "啟用失敗：$($result.Error)" "Error"
        $statusLabel.Text = "操作失敗"
    }
})

# 停用：恢復預設
$btnDevOff.Add_Click({
    if ($comboDevices.SelectedItem -eq $null) {
        [System.Windows.Forms.MessageBox]::Show(
            "請先選擇一台裝置。",
            "提示",
            [System.Windows.Forms.MessageBoxButtons]::OK,
            [System.Windows.Forms.MessageBoxIcon]::Information
        ) | Out-Null
        return
    }

    $serial = $comboDevices.SelectedItem.ToString()
    Write-ColorLog "正在停用螢幕常亮功能..." "Info"
    $statusLabel.Text = "執行中..."

    $result = Invoke-Adb -Arguments "shell svc power stayon false" -Serial $serial

    if ($result.Success) {
        Write-ColorLog "已成功停用螢幕常亮功能" "Success"
        $statusLabel.Text = "螢幕常亮已停用"
    } else {
        Write-ColorLog "停用失敗：$($result.Error)" "Error"
        $statusLabel.Text = "操作失敗"
    }
})

# 螢幕休眠
$btnSleepScreen.Add_Click({
    if ($comboDevices.SelectedItem -eq $null) {
        [System.Windows.Forms.MessageBox]::Show(
            "請先選擇一台裝置。",
            "提示",
            [System.Windows.Forms.MessageBoxButtons]::OK,
            [System.Windows.Forms.MessageBoxIcon]::Information
        ) | Out-Null
        return
    }

    $serial = $comboDevices.SelectedItem.ToString()
    Write-ColorLog "正在執行螢幕休眠..." "Info"
    $statusLabel.Text = "執行中..."

    # 使用 keyevent 26 (KEYCODE_POWER) 模擬電源鍵
    $result = Invoke-Adb -Arguments "shell input keyevent 26" -Serial $serial

    if ($result.Success) {
        Write-ColorLog "已成功執行螢幕休眠（模擬電源鍵）" "Success"
        $statusLabel.Text = "螢幕已休眠"
    } else {
        Write-ColorLog "休眠失敗：$($result.Error)" "Error"
        $statusLabel.Text = "操作失敗"
    }
})

# 授予 TeamViewer 媒體權限
$btnTeamViewerMedia.Add_Click({
    if ($comboDevices.SelectedItem -eq $null) {
        [System.Windows.Forms.MessageBox]::Show(
            "請先選擇一台裝置。",
            "提示",
            [System.Windows.Forms.MessageBoxButtons]::OK,
            [System.Windows.Forms.MessageBoxIcon]::Information
        ) | Out-Null
        return
    }

    $serial = $comboDevices.SelectedItem.ToString()
    Write-ColorLog "正在授予 TeamViewer 媒體投影權限..." "Info"
    $statusLabel.Text = "執行中..."

    $result = Invoke-Adb -Arguments "shell appops set com.teamviewer.host.market PROJECT_MEDIA allow" -Serial $serial

    if ($result.Success) {
        Write-ColorLog "已成功授予 TeamViewer 媒體投影權限 (PROJECT_MEDIA)" "Success"
        $statusLabel.Text = "權限已授予"
    } else {
        Write-ColorLog "授予權限失敗：$($result.Error)" "Error"
        $statusLabel.Text = "操作失敗"
    }
})

# 清除日誌
$btnClearLog.Add_Click({
    $textLog.Clear()
    Write-ColorLog "日誌已清除" "Info"
})

# 關閉視窗
$btnExit.Add_Click({
    $form.Close()
})

# 載入表單時初始化
$form.Add_Shown({
    Write-ColorLog "ADB 裝置管理工具已啟動" "Success"
    Write-ColorLog "提示：請確保手機已開啟 USB 偵錯功能" "Info"
    Update-DeviceList
})

# 顯示表單
[void]$form.ShowDialog()
