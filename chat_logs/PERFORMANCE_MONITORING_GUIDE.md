# v2.6.0 性能監控指南

**版本**: v2.6.0
**目標設備**: Sony XQ-BT52 (HQ616R2713)
**測試日期**: 2025-12-30
**監控目標**: FPS ≥30 | Memory ≤200MB | CPU ≤40%

---

## 📊 性能監控概述

### 監控指標

| 指標 | 目標值 | 測量工具 | 監控時機 |
|------|--------|---------|---------|
| **FPS** | ≥ 30 FPS | dumpsys gfxinfo | 相機預覽期間 |
| **記憶體** | ≤ 200 MB | dumpsys meminfo | 連續運行 5 分鐘後 |
| **CPU** | ≤ 40% | top / dumpsys cpuinfo | 光線分析運行時 |
| **幀時間** | ≤ 33.3 ms | dumpsys gfxinfo | 相機預覽期間 |
| **電池溫度** | ≤ 45°C | dumpsys battery | 連續運行 10 分鐘後 |

---

## 🎯 1. FPS 監控

### 方法 A: dumpsys gfxinfo (推薦)

**步驟 1: 清除舊數據**
```bash
adb -s HQ616R2713 shell dumpsys gfxinfo com.akirachang.ai.photographer reset
```

**步驟 2: 運行應用 30 秒**
- 啟動相機預覽
- 移動設備測試不同場景
- 確保光線分析運行

**步驟 3: 獲取 FPS 數據**
```bash
adb -s HQ616R2713 shell dumpsys gfxinfo com.akirachang.ai.photographer framestats
```

**輸出範例**:
```
---PROFILEDATA---
Flags,IntendedVsync,Vsync,OldestInputEvent,NewestInputEvent,HandleInputStart,AnimationStart,PerformTraversalsStart,DrawStart,SyncQueued,SyncStart,IssueDrawCommandsStart,SwapBuffers,FrameCompleted,DequeueBufferDuration,QueueBufferDuration,GpuCompleted
0,1234567890,1234567890,0,0,1234567900,1234567950,1234568000,1234568050,1234568100,1234568150,1234568200,1234568250,1234568300,500000,300000,1234568350
...
```

**分析腳本** (PowerShell):
```powershell
# 儲存為 analyze_fps.ps1
adb -s HQ616R2713 shell dumpsys gfxinfo com.akirachang.ai.photographer framestats |
    Select-String "^0," |
    ForEach-Object {
        $fields = $_.ToString().Split(',')
        $frameTime = ([long]$fields[13] - [long]$fields[1]) / 1000000.0  # Convert to ms
        $frameTime
    } |
    Measure-Object -Average -Maximum -Minimum |
    Select-Object Count, Average, Maximum, Minimum
```

**執行分析**:
```bash
powershell -ExecutionPolicy Bypass -File analyze_fps.ps1
```

**預期輸出**:
```
Count    : 180
Average  : 28.5 ms   (35 FPS) ✅
Maximum  : 45.2 ms
Minimum  : 16.7 ms
```

**驗收標準**:
- ✅ 平均幀時間 ≤ 33.3 ms (30 FPS)
- ✅ Janky frames < 10% (幀時間 > 33.3 ms 的比例)

---

### 方法 B: 實時 FPS 監控 (Android Studio)

**步驟**:
1. 打開 Android Studio
2. View → Tool Windows → Profiler
3. 點擊「+」新增 Session
4. 選擇設備 HQ616R2713 和應用 com.akirachang.ai.photographer
5. 啟用 "CPU" profiler
6. 在 Display 區域查看 FPS 圖表

**優點**: 實時視覺化，易於觀察波動

---

## 💾 2. 記憶體監控

### 方法 A: dumpsys meminfo (命令列)

**獲取記憶體使用**:
```bash
adb -s HQ616R2713 shell dumpsys meminfo com.akirachang.ai.photographer
```

**關鍵指標解讀**:
```
Applications Memory Usage (in Kilobytes):
Uptime: 123456 Realtime: 234567

** MEMINFO in pid 12345 [com.akirachang.ai.photographer] **
                   Pss  Private  Private  SwapPss     Heap     Heap     Heap
                 Total    Dirty    Clean    Dirty     Size    Alloc     Free
                ------   ------   ------   ------   ------   ------   ------
  Native Heap    45678    45234      123       0    65536    50123    15413  ← 主要關注
  Dalvik Heap    12345    12000      234       0    32768    15000    17768  ← 主要關注
 Dalvik Other     5678     5234      123       0
        Stack     1234     1234        0       0
       Ashmem      567      567        0       0
    Other dev      123      100       23       0
     .so mmap    23456      234     2345       0
    .jar mmap      567        0      567       0
    .apk mmap     1234        0     1234       0
    .ttf mmap      123        0      123       0
    .dex mmap     5678       12     5666       0
    .oat mmap     2345        0     1234       0
    .art mmap     3456     3123      234       0
   Other mmap      234       23       34       0
      Unknown     1234     1234        0       0
        TOTAL   103902    69995    10740       0    98304    65123    33181  ← 總計
```

**計算總記憶體 (MB)**:
```bash
# 使用 PowerShell
adb -s HQ616R2713 shell dumpsys meminfo com.akirachang.ai.photographer |
    Select-String "TOTAL" |
    ForEach-Object {
        $fields = $_.ToString() -split '\s+'
        $totalKB = [int]$fields[1]
        $totalMB = [math]::Round($totalKB / 1024, 2)
        Write-Output "Total Memory: $totalMB MB"
    }
```

**監控間隔建議**:
```bash
# 每 10 秒監控一次，持續 5 分鐘
for ($i=1; $i -le 30; $i++) {
    Write-Output "`n=== Sample $i at $(Get-Date) ==="
    adb -s HQ616R2713 shell dumpsys meminfo com.akirachang.ai.photographer |
        Select-String "TOTAL" |
        ForEach-Object {
            $fields = $_.ToString() -split '\s+'
            $totalKB = [int]$fields[1]
            $totalMB = [math]::Round($totalKB / 1024, 2)
            Write-Output "Total Memory: $totalMB MB"
        }
    Start-Sleep -Seconds 10
}
```

**驗收標準**:
- ✅ 總記憶體 (TOTAL PSS) ≤ 200 MB
- ✅ Native Heap + Dalvik Heap ≤ 150 MB
- ✅ 無明顯記憶體洩漏（5 分鐘內增長 < 20%）

---

### 方法 B: Android Studio Memory Profiler (視覺化)

**步驟**:
1. Android Studio → Profiler → Memory
2. 記錄 5 分鐘的記憶體使用
3. 檢查是否有持續增長趨勢（記憶體洩漏）
4. 點擊 "Force GC" 按鈕觸發垃圾回收，觀察是否釋放記憶體

**關鍵觀察點**:
- Java Heap 穩定在 30-50 MB
- Native Heap 穩定在 40-80 MB
- Graphics 使用 < 30 MB
- 無鋸齒狀持續上升（記憶體洩漏徵兆）

---

## ⚙️ 3. CPU 使用監控

### 方法 A: top 命令

**實時監控 CPU**:
```bash
adb -s HQ616R2713 shell top -m 10 | grep photographer
```

**輸出範例**:
```
  PID USER     PR  NI VIRT  RES  SHR S %CPU %MEM     TIME+ COMMAND
12345 u0_a123  20   0 2.3G 180M  80M S 35.2  4.5   0:12.34 com.akirachang.ai.photographer
```

**關鍵指標**:
- `%CPU`: 35.2% ✅ (< 40%)
- `%MEM`: 4.5%

**持續監控腳本**:
```bash
# 監控 60 秒，每秒記錄一次
for ($i=1; $i -le 60; $i++) {
    $timestamp = Get-Date -Format "HH:mm:ss"
    $cpuInfo = adb -s HQ616R2713 shell "top -n 1 -m 10 | grep photographer"
    if ($cpuInfo) {
        Write-Output "$timestamp - $cpuInfo"
    }
    Start-Sleep -Seconds 1
}
```

**驗收標準**:
- ✅ 平均 CPU 使用率 ≤ 40%
- ✅ 峰值 CPU < 60%（短暫尖峰可接受）
- ✅ 相機預覽穩定運行，無卡頓

---

### 方法 B: dumpsys cpuinfo

**獲取詳細 CPU 資訊**:
```bash
adb -s HQ616R2713 shell dumpsys cpuinfo | grep photographer
```

**輸出範例**:
```
38% 12345/com.akirachang.ai.photographer: 25% user + 13% kernel
```

**解讀**:
- `38%`: 總 CPU 使用率 ✅
- `25% user`: 應用程式執行時間
- `13% kernel`: 系統調用時間（相機、ML Kit）

---

## 🔥 4. 電池與溫度監控

**獲取電池狀態**:
```bash
adb -s HQ616R2713 shell dumpsys battery
```

**輸出範例**:
```
Current Battery Service state:
  AC powered: false
  USB powered: true
  Wireless powered: false
  Max charging current: 500000
  Max charging voltage: 5000000
  Charge counter: 2800000
  status: 2
  health: 2
  present: true
  level: 85
  scale: 100
  voltage: 4200
  temperature: 380  ← 38.0°C ✅ (< 45°C)
  technology: Li-ion
```

**溫度監控腳本**:
```bash
# 每分鐘記錄溫度，持續 10 分鐘
for ($i=1; $i -le 10; $i++) {
    $timestamp = Get-Date -Format "HH:mm:ss"
    $temp = adb -s HQ616R2713 shell dumpsys battery | Select-String "temperature"
    Write-Output "$timestamp - $temp"
    Start-Sleep -Seconds 60
}
```

**驗收標準**:
- ✅ 溫度 ≤ 45°C (450)
- ✅ 10 分鐘內溫度上升 < 10°C

---

## 📈 5. 綜合性能測試腳本

**自動化性能測試** (PowerShell):
```powershell
# 儲存為 performance_test.ps1
param(
    [string]$deviceId = "HQ616R2713",
    [int]$durationMinutes = 5
)

$packageName = "com.akirachang.ai.photographer"
$iterations = $durationMinutes * 6  # 每 10 秒一次

Write-Output "=== 性能測試開始 ==="
Write-Output "設備: $deviceId"
Write-Output "持續時間: $durationMinutes 分鐘"
Write-Output "開始時間: $(Get-Date)"
Write-Output ""

# 清除 gfxinfo
adb -s $deviceId shell dumpsys gfxinfo $packageName reset | Out-Null

for ($i=1; $i -le $iterations; $i++) {
    $timestamp = Get-Date -Format "HH:mm:ss"
    Write-Output "`n=== 樣本 $i/$iterations at $timestamp ==="

    # 1. 記憶體
    $memInfo = adb -s $deviceId shell dumpsys meminfo $packageName |
        Select-String "TOTAL" |
        Select-Object -First 1
    if ($memInfo) {
        $fields = $memInfo.ToString() -split '\s+'
        $totalKB = [int]$fields[1]
        $totalMB = [math]::Round($totalKB / 1024, 2)
        Write-Output "記憶體: $totalMB MB"

        if ($totalMB -gt 200) {
            Write-Output "⚠️ 警告: 記憶體超過 200 MB!"
        }
    }

    # 2. CPU
    $cpuInfo = adb -s $deviceId shell "top -n 1 -m 10 | grep photographer"
    if ($cpuInfo) {
        $cpuPercent = ($cpuInfo -split '\s+')[8]
        Write-Output "CPU: $cpuPercent%"

        if ([float]$cpuPercent -gt 40) {
            Write-Output "⚠️ 警告: CPU 使用率超過 40%!"
        }
    }

    # 3. 溫度
    $temp = adb -s $deviceId shell dumpsys battery |
        Select-String "temperature:" |
        ForEach-Object {
            $tempValue = ($_.ToString() -split ':')[1].Trim()
            [float]$tempValue / 10
        }
    Write-Output "溫度: ${temp}°C"

    if ($temp -gt 45) {
        Write-Output "⚠️ 警告: 溫度超過 45°C!"
    }

    Start-Sleep -Seconds 10
}

# 最終 FPS 分析
Write-Output "`n=== FPS 分析 ==="
$fpsData = adb -s $deviceId shell dumpsys gfxinfo $packageName framestats |
    Select-String "^0," |
    ForEach-Object {
        $fields = $_.ToString().Split(',')
        ([long]$fields[13] - [long]$fields[1]) / 1000000.0
    }

if ($fpsData) {
    $stats = $fpsData | Measure-Object -Average -Maximum -Minimum
    $avgFrameTime = [math]::Round($stats.Average, 2)
    $avgFps = [math]::Round(1000 / $avgFrameTime, 1)

    Write-Output "總幀數: $($stats.Count)"
    Write-Output "平均幀時間: $avgFrameTime ms"
    Write-Output "平均 FPS: $avgFps"
    Write-Output "最大幀時間: $([math]::Round($stats.Maximum, 2)) ms"
    Write-Output "最小幀時間: $([math]::Round($stats.Minimum, 2)) ms"

    if ($avgFps -lt 30) {
        Write-Output "⚠️ 警告: 平均 FPS 低於 30!"
    } else {
        Write-Output "✅ FPS 測試通過"
    }
}

Write-Output "`n=== 性能測試結束 ==="
Write-Output "結束時間: $(Get-Date)"
```

**執行測試**:
```bash
powershell -ExecutionPolicy Bypass -File performance_test.ps1 -deviceId HQ616R2713 -durationMinutes 5
```

---

## 🎯 6. 光線分析性能專項測試

**測試光線分析的 CPU 開銷**:

**步驟 1: 啟動應用並開始監控**
```bash
# Terminal 1: 啟動應用
adb -s HQ616R2713 shell am start -n com.akirachang.ai.photographer/.MainActivity

# Terminal 2: 開始 CPU 監控
adb -s HQ616R2713 shell "while true; do top -n 1 -m 1 | grep photographer; sleep 1; done"
```

**步驟 2: 觀察不同模式的 CPU 使用**
1. **無主體場景** (只有背景)
   - 記錄 CPU 使用率

2. **有人臉場景** (觸發光線分析)
   - 記錄 CPU 使用率
   - 對比無主體場景

**步驟 3: 日誌驗證**
```bash
adb -s HQ616R2713 logcat -s AnalyzeLightingUseCase:D | grep "Lighting"
```

**預期輸出**:
```
12-30 12:00:01.123 D AnalyzeLightingUseCase: Lighting analysis from Bitmap: score=1.0, backlit=false, overexp=false, lowlight=false
12-30 12:00:03.456 D AnalyzeLightingUseCase: Lighting with subject (Bitmap): score=0.8, backlit=false, HDR=false
```

**驗收標準**:
- ✅ 光線分析 CPU 開銷 < 10%
- ✅ 分析頻率 ~10 FPS (每 100ms)
- ✅ 無阻塞 UI 線程

---

## 📊 7. 性能報告模板

**建立報告**:
```markdown
# v2.6.0 性能測試報告

## 測試環境
- **設備**: Sony XQ-BT52 (HQ616R2713)
- **Android 版本**: [填寫]
- **APK 版本**: app-device-debug.apk
- **測試時間**: 2025-12-30
- **測試時長**: 5 分鐘

## 測試結果

### FPS
- 平均 FPS: [填寫] FPS
- 平均幀時間: [填寫] ms
- Janky frames: [填寫]%
- **狀態**: ✅ 通過 / ❌ 未通過

### 記憶體
- 初始記憶體: [填寫] MB
- 峰值記憶體: [填寫] MB
- 5 分鐘後記憶體: [填寫] MB
- **狀態**: ✅ 通過 / ❌ 未通過

### CPU
- 平均 CPU: [填寫]%
- 峰值 CPU: [填寫]%
- **狀態**: ✅ 通過 / ❌ 未通過

### 溫度
- 初始溫度: [填寫]°C
- 峰值溫度: [填寫]°C
- **狀態**: ✅ 通過 / ❌ 未通過

## 光線分析性能
- 分析頻率: [填寫] FPS
- CPU 開銷: [填寫]%
- 狀態切換延遲: [填寫] ms

## 結論
[填寫整體評估]
```

---

## 🛠️ 疑難排解

### 問題 1: dumpsys gfxinfo 無數據
**原因**: 應用未啟用 GPU 渲染分析
**解決**:
```bash
adb -s HQ616R2713 shell setprop debug.hwui.profile true
```

### 問題 2: CPU 使用率異常高
**檢查步驟**:
1. 確認是否在背景運行其他應用
2. 檢查 ML Kit 模型是否正確載入
3. 查看日誌是否有異常迴圈

### 問題 3: 記憶體持續增長
**檢查步驟**:
1. 使用 Memory Profiler 錄製 Heap Dump
2. 分析是否有 Bitmap 未釋放
3. 檢查 StateFlow 是否正確管理

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
