' UsbDevTool_Enhanced_Launcher.vbs
' 用於隱藏 PowerShell 主控台視窗的啟動器

Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

' 取得當前腳本所在目錄
strScriptPath = objFSO.GetParentFolderName(WScript.ScriptFullName)
strPSScript = strScriptPath & "\UsbDevTool_Enhanced.ps1"

' 檢查 PowerShell 腳本是否存在
If Not objFSO.FileExists(strPSScript) Then
    MsgBox "找不到 UsbDevTool_Enhanced.ps1" & vbCrLf & _
           "路徑: " & strPSScript, vbCritical, "錯誤"
    WScript.Quit
End If

' 啟動 PowerShell（視窗隱藏，不等待結束）
' -ExecutionPolicy Bypass: 允許執行未簽署的腳本
' -NoProfile: 不載入 PowerShell 設定檔（加快啟動速度）
' -WindowStyle Hidden: 隱藏 PowerShell 視窗
strCommand = "powershell.exe -ExecutionPolicy Bypass -NoProfile -WindowStyle Hidden -File """ & strPSScript & """"

' 執行命令（0 = 隱藏視窗, False = 不等待）
objShell.Run strCommand, 0, False

Set objShell = Nothing
Set objFSO = Nothing
