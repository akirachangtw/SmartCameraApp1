# start-claude-channels.ps1
# Windows 11 PowerShell + psmux
# 啟動 Claude Code 並掛載 Telegram Channel

$ErrorActionPreference = "Stop"

# ===== Config =====
$SessionName = "claude-channels"
$ProjectDir = "D:\DevSources\AndroidProj\SmartCameraApp1"
$Channel = "plugin:telegram@claude-plugins-official"
$SessionName_yyyyMMdd = "{0}_{1}" -f $SessionName, (Get-Date -Format "yyyyMMdd")
${cc-session_name} = $SessionName_yyyyMMdd

# ===== Tool checks =====
if (-not (Get-Command psmux -ErrorAction SilentlyContinue)) {
    throw "psmux not found. Please install psmux and make sure it is in PATH."
}

if (-not (Get-Command claude -ErrorAction SilentlyContinue)) {
    throw "claude not found. Please install Claude Code and make sure it is in PATH."
}

if (-not (Test-Path -LiteralPath $ProjectDir)) {
    throw "Project directory not found: $ProjectDir"
}

# ===== Clean existing session =====
# 若 session 不存在，忽略錯誤
& psmux kill-session -t $SessionName *> $null
Start-Sleep -Seconds 2

# ===== Start detached psmux session =====
# psmux new-session 沒有 tmux -c，所以在 PowerShell 裡 Set-Location
$StartupCommand = @"
Set-Location -LiteralPath '$ProjectDir'
Remove-Item Env:\CLAUDECODE -ErrorAction SilentlyContinue
claude --channels $Channel -n "${cc-session_name}"
"@

& psmux new-session -d -s $SessionName -- powershell.exe -NoLogo -NoProfile -NoExit -Command $StartupCommand

# ===== Wait for Claude startup =====
Start-Sleep -Seconds 20

# ===== Tell Claude to monitor channel messages =====
& psmux send-keys -t $SessionName "Resume monitoring. Check channels for any messages." Enter

Write-Host "Claude Code Telegram channel session started in psmux: $SessionName"
Write-Host "Claude session name: ${cc-session_name}"
Write-Host "Attach with: psmux attach -t $SessionName"
