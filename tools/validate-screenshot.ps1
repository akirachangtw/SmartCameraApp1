# validate-screenshot.ps1 - 模擬器截圖驗證工具
# 用途: 在上傳給 Claude API 前驗證圖片是否可用
# 使用: .\validate-screenshot.ps1 -ImagePath "截圖路徑"

[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)]
    [string]$ImagePath,

    [Parameter(Mandatory=$false)]
    [switch]$AutoFix = $false,

    [Parameter(Mandatory=$false)]
    [int]$MaxSizeKB = 5000  # Claude API 建議限制 5MB
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  📸 截圖驗證工具 v1.0" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 檢查檔案是否存在
if (-not (Test-Path $ImagePath)) {
    Write-Host "❌ 錯誤: 找不到圖片檔案" -ForegroundColor Red
    Write-Host "   路徑: $ImagePath" -ForegroundColor Gray
    exit 1
}

$file = Get-Item $ImagePath
Write-Host "📁 檔案資訊:" -ForegroundColor Yellow
Write-Host "   名稱: $($file.Name)" -ForegroundColor White
Write-Host "   路徑: $($file.FullName)" -ForegroundColor Gray
Write-Host "   大小: $([math]::Round($file.Length/1KB, 2)) KB" -ForegroundColor White
Write-Host "   修改時間: $($file.LastWriteTime)" -ForegroundColor Gray
Write-Host ""

$issues = @()
$warnings = @()

# 驗證 1: 檔案大小
Write-Host "🔍 驗證 1: 檢查檔案大小..." -ForegroundColor Cyan
$fileSizeKB = [math]::Round($file.Length/1KB, 2)
if ($fileSizeKB -gt $MaxSizeKB) {
    $issues += "檔案過大: $fileSizeKB KB (限制: $MaxSizeKB KB)"
    Write-Host "   ❌ 檔案過大: $fileSizeKB KB (超過 $MaxSizeKB KB)" -ForegroundColor Red
} elseif ($fileSizeKB -gt ($MaxSizeKB * 0.8)) {
    $warnings += "檔案接近大小限制: $fileSizeKB KB"
    Write-Host "   ⚠️  檔案接近限制: $fileSizeKB KB" -ForegroundColor Yellow
} else {
    Write-Host "   ✅ 大小正常: $fileSizeKB KB" -ForegroundColor Green
}

# 驗證 2: 檔案不為空
Write-Host "🔍 驗證 2: 檢查檔案內容..." -ForegroundColor Cyan
if ($file.Length -lt 1000) {
    $issues += "檔案過小或空白 (<1 KB)"
    Write-Host "   ❌ 檔案過小: $($file.Length) bytes" -ForegroundColor Red
} else {
    Write-Host "   ✅ 檔案非空" -ForegroundColor Green
}

# 驗證 3: 圖片格式
Write-Host "🔍 驗證 3: 檢查圖片格式..." -ForegroundColor Cyan
$extension = $file.Extension.ToLower()
$validExtensions = @(".png", ".jpg", ".jpeg", ".webp")
if ($extension -notin $validExtensions) {
    $issues += "不支援的圖片格式: $extension"
    Write-Host "   ❌ 不支援的格式: $extension" -ForegroundColor Red
    Write-Host "   支援格式: PNG, JPG, JPEG, WebP" -ForegroundColor Gray
} else {
    Write-Host "   ✅ 格式支援: $extension" -ForegroundColor Green
}

# 驗證 4: 使用 .NET 讀取圖片（檢查是否損壞）
Write-Host "🔍 驗證 4: 檢查圖片完整性..." -ForegroundColor Cyan
try {
    Add-Type -AssemblyName System.Drawing
    $img = [System.Drawing.Image]::FromFile($file.FullName)

    Write-Host "   ✅ 圖片可讀取" -ForegroundColor Green
    Write-Host "   尺寸: $($img.Width) x $($img.Height) px" -ForegroundColor White
    Write-Host "   格式: $($img.RawFormat.Guid)" -ForegroundColor Gray

    # 檢查尺寸是否合理
    if ($img.Width -lt 100 -or $img.Height -lt 100) {
        $warnings += "圖片尺寸過小: $($img.Width) x $($img.Height)"
        Write-Host "   ⚠️  尺寸過小（可能不完整）" -ForegroundColor Yellow
    }

    if ($img.Width -gt 4096 -or $img.Height -gt 4096) {
        $warnings += "圖片尺寸過大: $($img.Width) x $($img.Height)"
        Write-Host "   ⚠️  尺寸過大（建議壓縮）" -ForegroundColor Yellow
    }

    $img.Dispose()
} catch {
    $issues += "圖片損壞或無法讀取: $($_.Exception.Message)"
    Write-Host "   ❌ 圖片損壞: $($_.Exception.Message)" -ForegroundColor Red
}

# 驗證 5: 檔案路徑檢查（中文字元可能導致問題）
Write-Host "🔍 驗證 5: 檢查路徑相容性..." -ForegroundColor Cyan
if ($file.FullName -match '[^\x00-\x7F]') {
    $warnings += "路徑包含非 ASCII 字元（中文等），可能影響某些 API"
    Write-Host "   ⚠️  路徑包含非 ASCII 字元（中文檔名）" -ForegroundColor Yellow
    Write-Host "   建議使用英文檔名以確保最佳相容性" -ForegroundColor Gray
} else {
    Write-Host "   ✅ 路徑僅含 ASCII 字元" -ForegroundColor Green
}

# 驗證 6: ADB 截圖特定問題檢查
Write-Host "🔍 驗證 6: ADB 截圖完整性檢查..." -ForegroundColor Cyan
if ($file.Name -match "Screenshot|screencap") {
    # 檢查 PNG 標頭（前 8 bytes）
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName) | Select-Object -First 8
        $pngHeader = @(137, 80, 78, 71, 13, 10, 26, 10)
        $jpegHeader = @(255, 216, 255)

        $isPng = $true
        for ($i = 0; $i -lt 8; $i++) {
            if ($bytes[$i] -ne $pngHeader[$i]) {
                $isPng = $false
                break
            }
        }

        $isJpeg = ($bytes[0] -eq 255 -and $bytes[1] -eq 216 -and $bytes[2] -eq 255)

        if (-not $isPng -and -not $isJpeg) {
            $issues += "檔案標頭不符合 PNG/JPEG 格式（可能被截斷）"
            Write-Host "   ❌ 檔案標頭異常" -ForegroundColor Red
        } else {
            Write-Host "   ✅ 檔案標頭正常" -ForegroundColor Green
        }
    } catch {
        $warnings += "無法讀取檔案位元組: $($_.Exception.Message)"
        Write-Host "   ⚠️  無法驗證標頭" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⏭️  非 ADB 截圖，跳過此驗證" -ForegroundColor Gray
}

# 總結報告
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  📊 驗證總結" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ($issues.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "✅ 所有驗證通過！此圖片可安全上傳。" -ForegroundColor Green
    exit 0
}

if ($issues.Count -gt 0) {
    Write-Host "❌ 發現 $($issues.Count) 個嚴重問題:" -ForegroundColor Red
    foreach ($issue in $issues) {
        Write-Host "   - $issue" -ForegroundColor Red
    }
    Write-Host ""
}

if ($warnings.Count -gt 0) {
    Write-Host "⚠️  發現 $($warnings.Count) 個警告:" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "   - $warning" -ForegroundColor Yellow
    }
    Write-Host ""
}

# 自動修復建議
if ($AutoFix -and $issues.Count -gt 0) {
    Write-Host "🔧 嘗試自動修復..." -ForegroundColor Cyan

    # 修復 1: 圖片過大 → 壓縮
    if ($fileSizeKB -gt $MaxSizeKB) {
        Write-Host "   → 執行壓縮..." -ForegroundColor Yellow

        $outputPath = $file.DirectoryName + "\validated_" + $file.Name
        if ($extension -eq ".png") {
            # PNG → JPEG 轉換
            $outputPath = $outputPath -replace "\.png$", ".jpg"

            try {
                Add-Type -AssemblyName System.Drawing
                $img = [System.Drawing.Image]::FromFile($file.FullName)

                # 壓縮品質設定
                $encoder = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() |
                    Where-Object { $_.MimeType -eq 'image/jpeg' }
                $encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
                $encoderParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter(
                    [System.Drawing.Imaging.Encoder]::Quality, 85)

                $img.Save($outputPath, $encoder, $encoderParams)
                $img.Dispose()

                $newSize = [math]::Round((Get-Item $outputPath).Length/1KB, 2)
                Write-Host "   ✅ 壓縮完成: $newSize KB" -ForegroundColor Green
                Write-Host "   新檔案: $outputPath" -ForegroundColor Cyan
            } catch {
                Write-Host "   ❌ 壓縮失敗: $($_.Exception.Message)" -ForegroundColor Red
            }
        } else {
            Write-Host "   ⚠️  自動修復僅支援 PNG → JPEG" -ForegroundColor Yellow
        }
    }
}

# 修復建議
if (-not $AutoFix -and $issues.Count -gt 0) {
    Write-Host "💡 修復建議:" -ForegroundColor Cyan

    if ($fileSizeKB -gt $MaxSizeKB) {
        Write-Host "   → 執行壓縮工具:" -ForegroundColor White
        Write-Host "      .\tools\compress-screenshots.ps1 -InputDir `"$($file.DirectoryName)`"" -ForegroundColor Gray
    }

    if ($file.FullName -match '[^\x00-\x7F]') {
        Write-Host "   → 重新命名為英文檔名" -ForegroundColor White
    }

    if ($issues -match "損壞") {
        Write-Host "   → 重新捕獲截圖:" -ForegroundColor White
        Write-Host "      adb exec-out screencap -p > new_screenshot.png" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "   或使用 -AutoFix 參數自動修復:" -ForegroundColor Cyan
    Write-Host "   .\validate-screenshot.ps1 -ImagePath `"$ImagePath`" -AutoFix" -ForegroundColor Gray
}

Write-Host ""

if ($issues.Count -gt 0) {
    exit 1
} else {
    exit 0
}
