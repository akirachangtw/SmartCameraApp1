# 🚀 Pixel 9a 快速測試卡 (3 分鐘)

**APK**: `app/build/outputs/apk/device/debug/app-device-debug.apk` (246 MB)
**裝置**: Pixel 9a (59131JEBF17244)
**目標**: 確認無 PageSizeMismatchDialog

---

## ⚡ 快速測試（複製貼上即可）

```bash
# 1. 安裝 APK
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# 2. 清除 logcat
adb -s 59131JEBF17244 logcat -c

# 3. 啟動應用程式
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/.MainActivity

# 4. 等待 8 秒後檢查關鍵指標
sleep 8
adb -s 59131JEBF17244 logcat -d | grep -i "pagesize"
```

---

## ✅ 成功標準

**預期輸出**: `[空白 - 無結果]` 或只有 OpenCV 編譯資訊

**如果看到這個 = 失敗**:
```
W AppWarnings: Showing PageSizeMismatchDialog
```

---

## 📊 結果

- [ ] ✅ **通過** - 無警告，更新版本到 2.9.6-phase2.6.1
- [ ] ❌ **失敗** - 有警告，執行 Phase 2.6.2 (NDK 升級)

---

## 📄 詳細指南

參考: `HANDOFF_TO_WORK_NB.md`

**信心等級**: 🟢 99%
