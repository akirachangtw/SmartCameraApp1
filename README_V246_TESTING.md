# v2.4.6 模擬器測試 - 快速參考

**版本**: v2.4.6
**環境**: 模擬器（Emulator First）
**狀態**: 🟢 可開始測試（86% 測試通過）

---

## 🚀 5分鐘快速開始

### 方法 1: 一鍵測試（最快）
```batch
test-v246-emulator.bat quick
```

### 方法 2: VSCode
1. `Ctrl+Shift+B` → 選擇 `🧪 Run Unit Tests`

### 方法 3: 手動
```bash
./gradlew.bat testEmulatorDebugUnitTest --tests "*AnalyzeCompositionUseCaseTest"
```

---

## 📊 當前測試狀態

```
✅ 模擬器環境配置: 100% 完成
✅ 單元測試執行: 86% 通過 (19/22)
✅ v2.4.6 核心功能: 已驗證
⏰ 邊緣距離測試: 3個失敗（待修復）
```

---

## 📁 關鍵檔案

### 測試相關
- [測試計畫](V2_4_6_EMULATOR_TEST_PLAN.md) - 完整測試規劃
- [快速指南](V2_4_6_QUICK_TEST_GUIDE.md) - 操作指南
- [測試總結](V2_4_6_TEST_SUMMARY.md) - 執行結果
- [測試腳本](test-v246-emulator.bat) - 自動化腳本

### 開發相關
- [模擬器開發指南](EMULATOR_DEVELOPMENT_GUIDE.md) - 完整開發流程
- [快速開始](QUICK_START.md) - 環境配置
- [配置總結](chat_logs/handoff_20251214_summary.md) - 技術細節

### 測試代碼
- [AnalyzeCompositionUseCaseTest.kt](app/src/test/java/local/mylab/smartcameraapp1/domain/usecase/AnalyzeCompositionUseCaseTest.kt)

### 測試資源
- [測試圖片目錄](app/src/debug/assets/test_images/)

---

## 🎯 v2.4.6 測試重點

### ✅ 已驗證
- 眼睛位置權重降為 18% ✅
- 權重總和 = 1.0 ✅
- 眼睛位置分析 ✅
- 頭部空間檢查 ✅
- 前鏡頭鏡像處理 ✅
- 多主體偵測 ✅

### ⚠️ 待修復
- 邊緣距離判斷 (3/22 失敗)

---

## 🔧 常用命令

### 測試
```bash
# 完整測試
./gradlew.bat testEmulatorDebugUnitTest

# 快速測試
./gradlew.bat testEmulatorDebugUnitTest --tests "*AnalyzeCompositionUseCaseTest"

# 清理重測
./gradlew.bat clean testEmulatorDebugUnitTest
```

### 構建與安裝
```bash
# 構建
./gradlew.bat assembleEmulatorDebug

# 安裝（需模擬器）
./gradlew.bat installEmulatorDebug

# 一鍵構建安裝
./gradlew.bat installEmulatorDebug
```

### 查看結果
```bash
# 測試報告
start app/build/reports/tests/testEmulatorDebugUnitTest/index.html

# APK 位置
explorer app\build\outputs\apk\emulator\debug
```

### 日誌
```bash
# 收集日誌
adb logcat | grep -E "AnalyzeComposition|CameraModule|FakeCameraDataSource"

# 清除日誌
adb logcat -c
```

---

## 📋 測試清單

### 單元測試
- [x] 構圖分析 UseCase (19/22 通過)
- [ ] 光影分析 UseCase (待建立)
- [ ] ViewModel 整合測試 (待建立)

### UI 測試（模擬器）
- [ ] 構圖引導框顯示
- [ ] 格線系統
- [ ] 構圖提示
- [ ] 前後鏡頭切換

### 整合測試
- [ ] CameraViewModel + CameraDataSource
- [ ] 完整預覽流程
- [ ] 日誌驗證

---

## 🐛 故障排除

### 問題 1: 測試失敗
```bash
# 查看詳細報告
start app/build/reports/tests/testEmulatorDebugUnitTest/index.html

# 重新構建
./gradlew.bat clean build
```

### 問題 2: 模擬器未連接
```bash
# 檢查設備
adb devices

# 啟動模擬器
emulator -list-avds
emulator -avd <emulator_name>
```

### 問題 3: 測試圖片缺失
→ 沒關係！系統會自動生成備用圖片

---

## 📈 下一步

### 立即行動
1. **查看測試報告** - 了解失敗原因
2. **修復失敗測試** - 達到 100% 通過率
3. **在模擬器上驗證** - UI/UX 確認

### 短期目標
1. 整合 CameraViewModel
2. 添加更多測試
3. 提高測試覆蓋率

### 中期目標
1. 完整模擬器驗證
2. 準備實機測試
3. 性能優化

---

## 💡 提示

### 最佳實踐
- 🎯 **模擬器優先** - 80% 功能可在模擬器驗證
- 🧪 **測試驅動** - 先寫測試，再實現
- 📝 **文檔同步** - 保持文檔更新
- 🔄 **持續整合** - 頻繁測試

### 效率技巧
- 使用 `test-v246-emulator.bat quick` 快速測試
- VSCode 按 `Ctrl+Shift+B` 一鍵構建
- 使用 `--tests "*TestName"` 僅測試特定類

---

## 📞 需要幫助？

### 文檔
- [完整測試計畫](V2_4_6_EMULATOR_TEST_PLAN.md)
- [開發指南](EMULATOR_DEVELOPMENT_GUIDE.md)

### 測試報告
- HTML: `app/build/reports/tests/testEmulatorDebugUnitTest/index.html`

### 日誌
- 執行 `test-v246-emulator.bat` 會自動收集日誌

---

**建立**: 2025-12-14
**版本**: v2.4.6
**狀態**: ✅ 可用
**測試通過率**: 86%
