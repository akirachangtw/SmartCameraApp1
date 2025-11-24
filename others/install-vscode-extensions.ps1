# VSCode 混合工作流模式 - 外掛安裝腳本
# 執行方式: 在 VSCode 終端中運行 .\install-vscode-extensions.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VSCode 混合工作流外掛安裝器" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 檢查 code 命令是否可用
Write-Host "正在檢查 VSCode CLI 工具..." -ForegroundColor Yellow

# 嘗試不同的可能路徑
$codeCmd = $null
$possiblePaths = @(
    "code",
    "$env:LOCALAPPDATA\Programs\Microsoft VS Code\bin\code.cmd",
    "$env:ProgramFiles\Microsoft VS Code\bin\code.cmd",
    "${env:ProgramFiles(x86)}\Microsoft VS Code\bin\code.cmd"
)

foreach ($path in $possiblePaths) {
    try {
        $testResult = & $path --version 2>$null
        if ($?) {
            $codeCmd = $path
            Write-Host "✓ 找到 VSCode CLI: $path" -ForegroundColor Green
            break
        }
    } catch {
        continue
    }
}

if (-not $codeCmd) {
    Write-Host "✗ 無法找到 'code' 命令！" -ForegroundColor Red
    Write-Host ""
    Write-Host "請按照以下步驟操作：" -ForegroundColor Yellow
    Write-Host "1. 打開 VSCode" -ForegroundColor White
    Write-Host "2. 按 Ctrl+Shift+P 打開命令面板" -ForegroundColor White
    Write-Host "3. 輸入 'Shell Command: Install code command in PATH'" -ForegroundColor White
    Write-Host "4. 重啟終端後再次運行此腳本" -ForegroundColor White
    Write-Host ""
    Write-Host "或者，您可以在 VSCode 的整合終端中直接運行以下命令：" -ForegroundColor Yellow
    Write-Host ""

    $extensions = @(
        "mathiasfrohlich.Kotlin",
        "vscjava.vscode-java-pack",
        "vscjava.vscode-gradle",
        "eamodio.gitlens",
        "usernamehw.errorlens",
        "Gruntfuggly.todo-tree",
        "PKief.material-icon-theme",
        "streetsidesoftware.code-spell-checker"
    )

    foreach ($ext in $extensions) {
        Write-Host "code --install-extension $ext" -ForegroundColor Cyan
    }

    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "開始安裝外掛..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定義外掛列表
$coreExtensions = @(
    @{
        id = "mathiasfrohlich.Kotlin"
        name = "Kotlin Language"
        category = "必裝"
    },
    @{
        id = "vscjava.vscode-java-pack"
        name = "Extension Pack for Java"
        category = "必裝"
    },
    @{
        id = "vscjava.vscode-gradle"
        name = "Gradle for Java"
        category = "必裝"
    }
)

$recommendedExtensions = @(
    @{
        id = "eamodio.gitlens"
        name = "GitLens"
        category = "強烈推薦"
    },
    @{
        id = "usernamehw.errorlens"
        name = "Error Lens"
        category = "強烈推薦"
    },
    @{
        id = "Gruntfuggly.todo-tree"
        name = "TODO Tree"
        category = "強烈推薦"
    },
    @{
        id = "PKief.material-icon-theme"
        name = "Material Icon Theme"
        category = "強烈推薦"
    },
    @{
        id = "streetsidesoftware.code-spell-checker"
        name = "Code Spell Checker"
        category = "強烈推薦"
    }
)

# 合併所有外掛
$allExtensions = $coreExtensions + $recommendedExtensions

# 計數器
$total = $allExtensions.Count
$current = 0
$success = 0
$failed = 0
$skipped = 0

# 安裝外掛
foreach ($ext in $allExtensions) {
    $current++

    Write-Host "[$current/$total] " -NoNewline -ForegroundColor Cyan
    Write-Host "[$($ext.category)] " -NoNewline -ForegroundColor Yellow
    Write-Host "$($ext.name) " -NoNewline -ForegroundColor White
    Write-Host "($($ext.id))" -ForegroundColor Gray

    try {
        # 檢查是否已安裝
        $installed = & $codeCmd --list-extensions 2>$null | Where-Object { $_ -eq $ext.id }

        if ($installed) {
            Write-Host "  → 已安裝，跳過" -ForegroundColor DarkGray
            $skipped++
        } else {
            # 安裝外掛
            $output = & $codeCmd --install-extension $ext.id 2>&1

            if ($LASTEXITCODE -eq 0) {
                Write-Host "  → 安裝成功 ✓" -ForegroundColor Green
                $success++
            } else {
                Write-Host "  → 安裝失敗 ✗" -ForegroundColor Red
                Write-Host "     錯誤: $output" -ForegroundColor Red
                $failed++
            }
        }
    } catch {
        Write-Host "  → 安裝失敗 ✗" -ForegroundColor Red
        Write-Host "     異常: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }

    Write-Host ""
}

# 顯示安裝總結
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "安裝總結" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "總計: $total 個外掛" -ForegroundColor White
Write-Host "成功: $success 個" -ForegroundColor Green
Write-Host "跳過: $skipped 個（已安裝）" -ForegroundColor DarkGray
Write-Host "失敗: $failed 個" -ForegroundColor Red
Write-Host ""

if ($failed -eq 0) {
    Write-Host "✓ 所有外掛安裝完成！" -ForegroundColor Green
} else {
    Write-Host "⚠ 部分外掛安裝失敗，請檢查網絡連接" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "後續步驟" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "1. 重新載入 VSCode 視窗 (Ctrl+Shift+P → Reload Window)" -ForegroundColor White
Write-Host "2. 配置 settings.json (參考 VSCODE_EXTENSIONS_HYBRID_MODE.md)" -ForegroundColor White
Write-Host "3. 開始使用混合工作流！" -ForegroundColor White
Write-Host ""

# 詢問是否立即查看已安裝的外掛
Write-Host "是否查看已安裝的所有外掛？(Y/N): " -ForegroundColor Yellow -NoNewline
$response = Read-Host

if ($response -eq "Y" -or $response -eq "y") {
    Write-Host ""
    Write-Host "已安裝的外掛列表：" -ForegroundColor Cyan
    Write-Host ""
    & $codeCmd --list-extensions --show-versions | Sort-Object
}

Write-Host ""
Write-Host "完成！" -ForegroundColor Green
