# SmartCameraApp v2.4.6 - 模擬器截圖自動化腳本
# 用途: 自動捕獲模擬器截圖並組織分類
# 使用: .\capture-emulator-screenshots.ps1 [-Scenario <scenario>] [-AutoTest]

param(
    [string]$Scenario = "manual",  # manual, auto, composition, lighting, overlay
    [switch]$AutoTest = $false,
    [string]$OutputDir = "screenshots\emulator-testing"
)

# 設定
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$baseDir = "d:\MyDocuments\DevSources\AndroidStudioProjects\SmartCameraApp1"
$outputPath = Join-Path $baseDir $OutputDir
$sessionPath = Join-Path $outputPath "session_$timestamp"

# 顏色輸出函數
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

# 建立目錄結構
function Initialize-Directories {
    Write-ColorOutput "📁 建立截圖目錄結構..." "Cyan"

    $dirs = @(
        $sessionPath,
        "$sessionPath\composition",
        "$sessionPath\lighting",
        "$sessionPath\overlay",
        "$sessionPath\camera-switch",
        "$sessionPath\ui-elements"
    )

    foreach ($dir in $dirs) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }

    Write-ColorOutput "✅ 目錄結構建立完成: $sessionPath" "Green"
}

# 檢查模擬器連接
function Test-EmulatorConnection {
    Write-ColorOutput "🔍 檢查模擬器連接..." "Cyan"

    $devices = adb devices | Select-String "emulator"

    if ($devices) {
        Write-ColorOutput "✅ 模擬器已連接" "Green"
        return $true
    } else {
        Write-ColorOutput "❌ 未偵測到模擬器！請先啟動模擬器" "Red"
        Write-ColorOutput "   提示: emulator -avd <your_emulator_name>" "Yellow"
        return $false
    }
}

# 捕獲截圖
function Capture-Screenshot {
    param(
        [string]$Name,
        [string]$Category = "general",
        [int]$DelaySeconds = 0
    )

    if ($DelaySeconds -gt 0) {
        Write-ColorOutput "⏱️  等待 $DelaySeconds 秒..." "Yellow"
        Start-Sleep -Seconds $DelaySeconds
    }

    $filename = "${Name}_${timestamp}.png"
    $categoryPath = Join-Path $sessionPath $Category
    $filepath = Join-Path $categoryPath $filename

    Write-ColorOutput "📸 捕獲截圖: $Name" "Cyan"

    # 使用 adb screencap
    adb exec-out screencap -p > $filepath

    if (Test-Path $filepath) {
        $fileSize = (Get-Item $filepath).Length
        if ($fileSize -gt 1000) {
            Write-ColorOutput "✅ 截圖成功: $filename ($([math]::Round($fileSize/1KB, 2)) KB)" "Green"
            return $filepath
        } else {
            Write-ColorOutput "❌ 截圖失敗: 檔案過小" "Red"
            Remove-Item $filepath -ErrorAction SilentlyContinue
            return $null
        }
    } else {
        Write-ColorOutput "❌ 截圖失敗" "Red"
        return $null
    }
}

# 手動測試模式
function Start-ManualTest {
    Write-ColorOutput "`n========================================" "Magenta"
    Write-ColorOutput "  📱 手動截圖測試模式" "Magenta"
    Write-ColorOutput "========================================`n" "Magenta"

    Write-ColorOutput "請在模擬器上操作應用，每個測試場景後按 Enter 捕獲截圖" "Yellow"
    Write-ColorOutput "輸入 'q' 結束測試`n" "Yellow"

    $testScenarios = @(
        @{Name="初始畫面"; Category="ui-elements"; Description="應用啟動後的初始畫面"},
        @{Name="權限請求"; Category="ui-elements"; Description="相機權限請求對話框"},
        @{Name="相機預覽_無主體"; Category="composition"; Description="無人臉時的畫面"},
        @{Name="格線顯示"; Category="overlay"; Description="三分法格線"},
        @{Name="人臉偵測_單人"; Category="composition"; Description="偵測單一人臉"},
        @{Name="構圖引導框_良好"; Category="composition"; Description="良好構圖的引導框（綠色）"},
        @{Name="構圖引導框_需調整"; Category="composition"; Description="需調整的引導框（黃色）"},
        @{Name="構圖提示_眼睛位置"; Category="overlay"; Description="眼睛位置提示"},
        @{Name="構圖提示_頭部空間"; Category="overlay"; Description="頭部空間提示"},
        @{Name="方向指示器"; Category="overlay"; Description="方向指示器顯示"},
        @{Name="水平儀"; Category="overlay"; Description="水平儀指示器"},
        @{Name="前鏡頭"; Category="camera-switch"; Description="切換到前鏡頭"},
        @{Name="前鏡頭_人臉偵測"; Category="camera-switch"; Description="前鏡頭人臉偵測（鏡像）"},
        @{Name="後鏡頭"; Category="camera-switch"; Description="切換回後鏡頭"},
        @{Name="逆光場景"; Category="lighting"; Description="逆光偵測和HDR建議"},
        @{Name="低光場景"; Category="lighting"; Description="低光環境"},
        @{Name="多人臉"; Category="composition"; Description="多人臉偵測"},
        @{Name="設定畫面"; Category="ui-elements"; Description="設定頁面"}
    )

    $index = 1
    foreach ($scenario in $testScenarios) {
        Write-ColorOutput "`n[$index/$($testScenarios.Count)] $($scenario.Name)" "Cyan"
        Write-ColorOutput "    說明: $($scenario.Description)" "Gray"
        Write-ColorOutput "    請在模擬器上準備此場景，然後按 Enter 捕獲截圖..." "Yellow"

        $input = Read-Host

        if ($input -eq 'q') {
            Write-ColorOutput "⏹️  使用者中斷測試" "Yellow"
            break
        }

        Capture-Screenshot -Name $scenario.Name -Category $scenario.Category
        $index++
    }
}

# 自動測試模式（使用 UI Automator 或 adb 命令）
function Start-AutoTest {
    Write-ColorOutput "`n========================================" "Magenta"
    Write-ColorOutput "  🤖 自動截圖測試模式" "Magenta"
    Write-ColorOutput "========================================`n" "Magenta"

    # 1. 啟動應用
    Write-ColorOutput "🚀 啟動應用..." "Cyan"
    adb shell am start -n com.akirachang.ai.photographer.emulator/.MainActivity
    Start-Sleep -Seconds 3

    Capture-Screenshot -Name "01_應用啟動" -Category "ui-elements"

    # 2. 等待相機初始化
    Write-ColorOutput "⏱️  等待相機初始化..." "Cyan"
    Start-Sleep -Seconds 5

    Capture-Screenshot -Name "02_相機預覽" -Category "ui-elements"

    # 3. 捕獲 Overlay
    Write-ColorOutput "📸 捕獲 Overlay 元素..." "Cyan"
    Start-Sleep -Seconds 2

    Capture-Screenshot -Name "03_格線_Overlay" -Category "overlay"

    # 4. 模擬點擊切換相機（需要座標）
    Write-ColorOutput "🔄 切換相機..." "Cyan"
    # 注意: 需要根據實際 UI 調整座標
    adb shell input tap 960 100  # 假設切換按鈕在右上角
    Start-Sleep -Seconds 2

    Capture-Screenshot -Name "04_前鏡頭" -Category "camera-switch"

    # 5. 切換回後鏡頭
    adb shell input tap 960 100
    Start-Sleep -Seconds 2

    Capture-Screenshot -Name "05_後鏡頭" -Category "camera-switch"

    Write-ColorOutput "✅ 自動測試完成" "Green"
}

# 生成 HTML 報告
function Generate-HtmlReport {
    Write-ColorOutput "`n📊 生成 HTML 報告..." "Cyan"

    $reportPath = Join-Path $sessionPath "index.html"

    $html = @"
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartCameraApp v2.4.6 - 模擬器截圖報告</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            padding: 40px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        h1 {
            color: #333;
            margin-bottom: 10px;
            font-size: 2.5em;
        }
        .meta {
            color: #666;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #eee;
        }
        .category {
            margin-bottom: 40px;
        }
        .category-title {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            font-size: 1.5em;
            margin-bottom: 20px;
        }
        .screenshot-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
        }
        .screenshot-item {
            border: 2px solid #eee;
            border-radius: 10px;
            overflow: hidden;
            transition: transform 0.3s, box-shadow 0.3s;
            background: white;
        }
        .screenshot-item:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }
        .screenshot-item img {
            width: 100%;
            height: auto;
            display: block;
            cursor: pointer;
        }
        .screenshot-info {
            padding: 15px;
            background: #f8f9fa;
        }
        .screenshot-name {
            font-weight: bold;
            color: #333;
            margin-bottom: 5px;
        }
        .screenshot-meta {
            font-size: 0.9em;
            color: #666;
        }
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.9);
            justify-content: center;
            align-items: center;
        }
        .modal-content {
            max-width: 90%;
            max-height: 90%;
        }
        .close {
            position: absolute;
            top: 20px;
            right: 40px;
            color: white;
            font-size: 40px;
            font-weight: bold;
            cursor: pointer;
        }
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        .stat-number {
            font-size: 2.5em;
            font-weight: bold;
        }
        .stat-label {
            font-size: 0.9em;
            opacity: 0.9;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📱 SmartCameraApp v2.4.6</h1>
        <div class="meta">
            <strong>模擬器截圖測試報告</strong><br>
            測試時間: $timestamp<br>
            測試模式: $Scenario
        </div>

        <div class="stats" id="stats"></div>

        <div id="categories"></div>
    </div>

    <div id="modal" class="modal" onclick="closeModal()">
        <span class="close">&times;</span>
        <img class="modal-content" id="modal-img">
    </div>

    <script>
        const categories = {};

        function loadScreenshots() {
            // 此處由 PowerShell 動態生成
            SCREENSHOTS_DATA
        }

        function renderStats() {
            const statsDiv = document.getElementById('stats');
            const totalCount = Object.values(categories).reduce((sum, imgs) => sum + imgs.length, 0);
            const categoryCount = Object.keys(categories).length;

            statsDiv.innerHTML = \`
                <div class="stat-card">
                    <div class="stat-number">\${totalCount}</div>
                    <div class="stat-label">總截圖數</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">\${categoryCount}</div>
                    <div class="stat-label">測試類別</div>
                </div>
            \`;
        }

        function renderCategories() {
            const categoriesDiv = document.getElementById('categories');
            let html = '';

            const categoryNames = {
                'composition': '📐 構圖分析',
                'lighting': '💡 光影分析',
                'overlay': '🎨 Overlay 元素',
                'camera-switch': '🔄 相機切換',
                'ui-elements': '🖼️ UI 元素'
            };

            for (const [category, images] of Object.entries(categories)) {
                html += \`
                    <div class="category">
                        <div class="category-title">\${categoryNames[category] || category} (\${images.length})</div>
                        <div class="screenshot-grid">
                \`;

                images.forEach(img => {
                    html += \`
                        <div class="screenshot-item">
                            <img src="\${img.path}" alt="\${img.name}" onclick="openModal(this.src)">
                            <div class="screenshot-info">
                                <div class="screenshot-name">\${img.name}</div>
                                <div class="screenshot-meta">\${img.size}</div>
                            </div>
                        </div>
                    \`;
                });

                html += \`
                        </div>
                    </div>
                \`;
            }

            categoriesDiv.innerHTML = html;
        }

        function openModal(src) {
            document.getElementById('modal').style.display = 'flex';
            document.getElementById('modal-img').src = src;
        }

        function closeModal() {
            document.getElementById('modal').style.display = 'none';
        }

        loadScreenshots();
        renderStats();
        renderCategories();
    </script>
</body>
</html>
"@

    # 收集所有截圖
    $categories = @("composition", "lighting", "overlay", "camera-switch", "ui-elements")
    $screenshotsData = @{}

    foreach ($category in $categories) {
        $categoryPath = Join-Path $sessionPath $category
        if (Test-Path $categoryPath) {
            $images = Get-ChildItem -Path $categoryPath -Filter "*.png"
            if ($images.Count -gt 0) {
                $imageList = @()
                foreach ($img in $images) {
                    $imageList += @{
                        name = $img.BaseName
                        path = "$category/$($img.Name)"
                        size = "$([math]::Round($img.Length/1KB, 2)) KB"
                    }
                }
                $screenshotsData[$category] = $imageList
            }
        }
    }

    # 生成 JavaScript 資料
    $jsData = $screenshotsData | ConvertTo-Json -Depth 3
    $html = $html -replace "SCREENSHOTS_DATA", "categories = $jsData;"

    # 寫入檔案
    $html | Out-File -FilePath $reportPath -Encoding UTF8

    Write-ColorOutput "✅ HTML 報告已生成: $reportPath" "Green"

    # 自動開啟
    Start-Process $reportPath
}

# 生成 Markdown 對比文檔
function Generate-ComparisonDoc {
    Write-ColorOutput "`n📝 生成對比文檔..." "Cyan"

    $mdPath = Join-Path $sessionPath "VISUAL_COMPARISON.md"

    $markdown = @"
# v2.4.6 模擬器截圖 - 視覺對比文檔

**測試時間**: $timestamp
**測試模式**: $Scenario

---

## 📊 測試統計

"@

    # 統計各類別截圖數
    $categories = @("composition", "lighting", "overlay", "camera-switch", "ui-elements")
    foreach ($category in $categories) {
        $categoryPath = Join-Path $sessionPath $category
        if (Test-Path $categoryPath) {
            $count = (Get-ChildItem -Path $categoryPath -Filter "*.png").Count
            $markdown += "- **$category**: $count 張截圖`n"
        }
    }

    $markdown += @"

---

## 🎯 測試場景清單

### 1. 構圖分析 (Composition)

#### 1.1 無主體
- [ ] 顯示「未偵測到主體」提示
- [ ] 格線正常顯示
- [ ] 無構圖引導框

#### 1.2 單一人臉 - 良好構圖
- [ ] 綠色構圖引導框顯示在理想位置
- [ ] 引導框位於上三分線附近
- [ ] 無調整建議或僅有「構圖良好」訊息

#### 1.3 單一人臉 - 需調整
- [ ] 黃色構圖引導框
- [ ] 顯示具體調整建議（中文）
- [ ] 方向指示器指向正確方向

#### 1.4 多人臉
- [ ] 所有人臉皆有偵測框
- [ ] 主要人臉有構圖引導
- [ ] 多主體提示（如果有）

---

### 2. Overlay 元素

#### 2.1 格線系統
- [ ] 三分法格線位置準確（33%, 67%）
- [ ] 格線顏色清晰可見
- [ ] 格線可開關

#### 2.2 構圖提示
- [ ] 提示文字清晰（中文）
- [ ] 提示位置適當（不遮主體）
- [ ] 提示內容準確
- [ ] 背景半透明確保可讀性

#### 2.3 方向指示器
- [ ] 箭頭方向正確
- [ ] 顏色清晰
- [ ] 位置適當

#### 2.4 水平儀
- [ ] 水平線顯示
- [ ] 傾斜角度指示
- [ ] 顏色變化（平衡/傾斜）

---

### 3. 相機切換

#### 3.1 前鏡頭
- [ ] 預覽正確鏡像
- [ ] 人臉偵測正常
- [ ] 構圖分析正確（座標已轉換）
- [ ] Overlay 元素正確顯示

#### 3.2 後鏡頭
- [ ] 預覽正常（非鏡像）
- [ ] 人臉偵測正常
- [ ] 構圖分析正確
- [ ] 切換流暢無閃爍

---

### 4. 光影分析

#### 4.1 逆光場景
- [ ] 顯示「逆光」標籤
- [ ] HDR 建議圖示
- [ ] 建議文字說明

#### 4.2 低光場景
- [ ] 顯示「低光」標籤
- [ ] 相應的拍攝建議

---

### 5. UI 元素

#### 5.1 初始畫面
- [ ] 相機權限請求（首次）
- [ ] UI 元素完整顯示

#### 5.2 設定畫面
- [ ] 所有開關可見
- [ ] 設定項目完整
- [ ] 描述文字清晰

---

## 🔍 已知問題追蹤

### 視覺問題
- [ ] 問題 1: [描述]
- [ ] 問題 2: [描述]

### 功能問題
- [ ] 問題 1: [描述]
- [ ] 問題 2: [描述]

---

## 📸 截圖索引

### 構圖分析
"@

    # 列出所有截圖
    foreach ($category in $categories) {
        $categoryPath = Join-Path $sessionPath $category
        if (Test-Path $categoryPath) {
            $images = Get-ChildItem -Path $categoryPath -Filter "*.png"
            if ($images.Count -gt 0) {
                $markdown += "`n### $category`n`n"
                foreach ($img in $images) {
                    $relativePath = "$category/$($img.Name)"
                    $markdown += "- ![$($img.BaseName)]($relativePath)`n"
                    $markdown += "  **$($img.BaseName)** ($([math]::Round($img.Length/1KB, 2)) KB)`n`n"
                }
            }
        }
    }

    $markdown += @"

---

## 📋 對比基準

### 參考截圖（v2.4.6-Testing）

可對比以下實機截圖作為基準：

- \`others/v2.4.6-Testing/optimized/前鏡頭_20251213-170234.jpg\`
- \`others/v2.4.6-Testing/optimized/前鏡頭_20251213-170257.jpg\`
- \`others/v2.4.6-Testing/optimized/後鏡頭Screenshot_20251213-170034.jpg\`
- \`others/v2.4.6-Testing/optimized/後鏡頭Screenshot_20251213-170145.jpg\`

### 對比重點

1. **構圖引導框位置** - 模擬器 vs 實機應一致
2. **Overlay 元素顯示** - 格線、提示、指示器
3. **前後鏡頭差異** - 鏡像處理是否正確
4. **UI 佈局** - 按鈕位置、文字大小

---

**建立者**: PowerShell 自動化腳本
**測試環境**: Android Emulator
**應用版本**: v2.4.6-emulator
"@

    $markdown | Out-File -FilePath $mdPath -Encoding UTF8

    Write-ColorOutput "✅ 對比文檔已生成: $mdPath" "Green"
}

# ============ 主程序 ============

Write-ColorOutput "`n========================================" "Magenta"
Write-ColorOutput "  SmartCameraApp v2.4.6" "Magenta"
Write-ColorOutput "  模擬器截圖自動化工具" "Magenta"
Write-ColorOutput "========================================`n" "Magenta"

# 檢查模擬器
if (-not (Test-EmulatorConnection)) {
    exit 1
}

# 建立目錄
Initialize-Directories

# 執行測試
if ($AutoTest) {
    Start-AutoTest
} else {
    Start-ManualTest
}

# 生成報告
Generate-HtmlReport
Generate-ComparisonDoc

# 總結
Write-ColorOutput "`n========================================" "Green"
Write-ColorOutput "  ✅ 截圖捕獲完成！" "Green"
Write-ColorOutput "========================================" "Green"
Write-ColorOutput "📁 輸出位置: $sessionPath" "Cyan"
Write-ColorOutput "📊 HTML 報告: $sessionPath\index.html" "Cyan"
Write-ColorOutput "📝 對比文檔: $sessionPath\VISUAL_COMPARISON.md" "Cyan"
Write-ColorOutput "`n" "White"
