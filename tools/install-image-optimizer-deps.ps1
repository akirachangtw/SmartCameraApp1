# Install Image Optimizer Tool Dependencies
# 安裝圖片優化工具所需的 Python 套件

Write-Host "🔧 圖片優化工具 - 依賴安裝腳本" -ForegroundColor Cyan
Write-Host ""

# Check Python installation
Write-Host "檢查 Python 安裝..." -ForegroundColor Yellow
try {
    $pythonVersion = python --version 2>&1
    Write-Host "  ✓ 找到 Python: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ 錯誤: 找不到 Python" -ForegroundColor Red
    Write-Host "  請先安裝 Python 3.8 或更新版本" -ForegroundColor Red
    Write-Host ""
    Read-Host "按 Enter 鍵退出"
    exit 1
}

Write-Host ""

# Check pip
Write-Host "檢查 pip 安裝..." -ForegroundColor Yellow
try {
    $pipVersion = pip --version 2>&1
    Write-Host "  ✓ 找到 pip: $pipVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ 錯誤: 找不到 pip" -ForegroundColor Red
    Write-Host "  請確認 Python 安裝包含 pip" -ForegroundColor Red
    Write-Host ""
    Read-Host "按 Enter 鍵退出"
    exit 1
}

Write-Host ""

# Install Pillow
Write-Host "安裝 Pillow (PIL Fork)..." -ForegroundColor Yellow
Write-Host ""

try {
    pip install Pillow --upgrade
    Write-Host ""
    Write-Host "  ✓ Pillow 安裝成功！" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "  ✗ Pillow 安裝失敗" -ForegroundColor Red
    Write-Host "  請手動執行: pip install Pillow" -ForegroundColor Red
    Write-Host ""
    Read-Host "按 Enter 鍵退出"
    exit 1
}

Write-Host ""
Write-Host "=" * 60 -ForegroundColor Gray
Write-Host ""

# Test import
Write-Host "測試套件匯入..." -ForegroundColor Yellow
$testScript = @"
try:
    from PIL import Image
    print('  ✓ Pillow 匯入成功')
    print(f'  版本: {Image.__version__}')
except ImportError as e:
    print(f'  ✗ 匯入失敗: {e}')
    exit(1)
"@

$testResult = python -c $testScript 2>&1
Write-Host $testResult -ForegroundColor Green

Write-Host ""
Write-Host "=" * 60 -ForegroundColor Gray
Write-Host ""
Write-Host "✅ 所有依賴安裝完成！" -ForegroundColor Green
Write-Host ""
Write-Host "現在可以執行圖片優化工具：" -ForegroundColor Cyan
Write-Host "  雙擊 optimize-test-images.pyw" -ForegroundColor White
Write-Host ""

Read-Host "按 Enter 鍵退出"
