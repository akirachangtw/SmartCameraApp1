# Cueshot D1-D8 實機功能驗測清單 v1.1

**文件版本**: D1-D8-Verification-v1.0-20260418
**觸發原因**: 實機截圖顯示 MVP 預期與實際表現差異顯著，暫停 D9 開發
**驗測設備**: Sony XQ-BT52 (HQ616R2713) + Pixel 9a (59131JEBF17244)
**驗測模式**: 每項標註 PASS / FAIL / N/A，FAIL 項附帶截圖或日誌

---

## 🔴 截圖已確認的問題（優先修復）

### 問題 P1：「未偵測到主體」文字標籤違反 Ambient UI 無文字原則
- **嚴重度**: 🔴 Critical — 直接違反四支柱第三項
- **SSOT 依據**: Strategic Brief §A.2「Ambient UI 無文字 vs 所有競品的文字指導」
- **預期行為**: 拍攝畫面上零文字指令，所有引導透過 Edge Warmth + Halo 視覺通道傳遞
- **實際行為**: 畫面頂部顯示灰底白字「未偵測到主體」
- **可能來源**: D1-D2 早期開發階段的除錯 UI 或舊版 CompositionResult 文字顯示邏輯殘留
- **排查方向**: `CameraScreen.kt` 搜尋「未偵測到主體」或相關 Text Composable；檢查 `compositionResult` 是否有 text overlay 渲染路徑

### 問題 P2：角度數值「-3°」直接顯示在拍攝畫面
- **嚴重度**: 🔴 Critical — 違反無文字原則 + 評分數據外洩
- **SSOT 依據**: SSOT「評分寫入 EXIF UserComment，不印在照片上」；Ambient UI 無文字
- **預期行為**: 角度資訊透過 Edge Warmth 漸層傳遞，數值不直接顯示
- **實際行為**: 畫面底部黃色文字顯示「-3°」
- **可能來源**: 角度引導的除錯 overlay 或舊版 AngleIndicator 元件殘留
- **排查方向**: `CameraScreen.kt` 搜尋角度顯示相關 Composable；檢查 IMU pitch/roll 值是否有 Text() 渲染

### 問題 P3：人臉可見但無 Halo / Edge Warmth 引導
- **嚴重度**: 🟡 High — Ambient UI 雙通道引導未生效
- **SSOT 依據**: Strategic Brief §D.1「Edge Warmth 60fps + Halo 10fps+EMA」
- **預期行為**: 偵測到人臉 → Portrait 模式觸發 → Halo 圍繞人臉 + Edge Warmth 邊緣暖光
- **實際行為**: 三分網格可見，但無 Halo 圓環、無 Edge Warmth 漸層
- **可能原因**:
  - 場景判斷未切換到 Portrait（可能卡在 General 模式）
  - Edge Warmth / Halo alpha 值過低在實機上不可見
  - `compositionResult` 或 `detectionResult` 管線未正確連接到 UI
- **排查方向**: Logcat 過濾 `[D2]` `[D8-B1-T3]` 標籤，確認 compositionAdvice 和 ptfAlignmentResult 是否有輸出

---

### 問題 P4：底部「拍照」使用中文文字標籤而非圖示
- **嚴重度**: 🔴 Critical — 違反圖示優先 UI 原則 + 語言硬編碼
- **SSOT 依據**: Cueshot 設計原則「icon-first UI with zero text labels during shooting」；D14 規劃 i18n (en/zh-TW/ja)，MVP 預設語言應為英文
- **預期行為**: 底部快門區域使用純圖示，無文字標籤；若有文字應為英文
- **實際行為**: 綠色按鈕上顯示中文「拍照」文字
- **排查方向**: `CameraScreen.kt` 底部 Tab / Button 區域的 Text Composable；確認是否有 `stringResource` 或硬編碼中文

### 問題 P5：底部「遞手機」使用中文文字標籤
- **嚴重度**: 🟡 High — 同 P4，文字標籤 + 語言問題
- **預期行為**: PTF Tab 使用純圖示（如手機遞交圖標），或英文標籤「PTF」
- **實際行為**: 右下角顯示中文「遞手機」文字
- **排查方向**: PTF Tab Composable 內的 Text 元件

### 問題 P6：缺少圖片瀏覽入口
- **嚴重度**: 🟡 High — 基本相機 App 必備功能缺失
- **預期行為**: 快門按鈕旁（左下角）應有最近拍攝照片的縮圖 thumbnail，點擊可進入圖片瀏覽/相簿
- **實際行為**: 左下角僅有前後鏡頭切換按鈕，無圖片瀏覽入口
- **排查方向**: 確認是否有 Gallery/PhotoPreview Composable 但被隱藏，或根本未實作

### 問題 P7：頂部工具列圖示功能不明
- **嚴重度**: 🟡 Medium — UX 可理解性
- **預期行為**: 頂部圖示應清晰可辨識：設定齒輪（左）、閃光燈模式（中）、[待確認]（右）
- **實際行為**: 左側齒輪 OK，中間閃光燈有「A」標記（Auto？），右側星形圖示用途不明
- **排查方向**: 確認右上角圖示的功能（可能是 AI 功能開關？特效？需對照 SSOT 定義）

---

## 基本 UI 介面驗測（全局性）

### UI-A：圖示優先原則（Icon-First）
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| A.1 | 拍攝畫面零文字標籤 | 所有按鈕、Tab、指引均為純圖示 | ☐ 🔴 |
| A.2 | 快門按鈕為圖示 | 圓形快門圖示，無「拍照」文字 | ☐ 🔴 |
| A.3 | PTF Tab 為圖示 | 手機遞交圖標或抽象圖示，無「遞手機」文字 | ☐ 🔴 |
| A.4 | 設定入口為齒輪圖示 | 純圖示，無「設定」文字 | ☐ |
| A.5 | 閃光燈切換為圖示 | 閃電圖示 + 狀態指示（Auto/On/Off），無文字 | ☐ |
| A.6 | 所有 Toast/Snackbar 可為英文 | MVP 預設英文，i18n 在 D14 補 | ☐ |

### UI-B：語言與在地化
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| B.1 | App 預設語言為英文 | 所有 UI 文字（若有）為英文 | ☐ 🔴 |
| B.2 | 無硬編碼中文字串 | 所有文字走 `strings.xml` / `stringResource` | ☐ |
| B.3 | Settings 頁面語言 | 英文（D14 加 zh-TW / ja） | ☐ |

### UI-C：底部操作列佈局
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| C.1 | 快門按鈕居中 | 圓形快門，視覺焦點 | ☐ |
| C.2 | 左側：圖片瀏覽 thumbnail | 最近拍攝照片縮圖，點擊進相簿 | ☐ 🔴 |
| C.3 | 右側：PTF Tab | 底部全寬 36dp（Strategic Brief §B.3） | ☐ |
| C.4 | PTF ✕ 退出按鈕位置 | 快門左側，純圖示 | ☐ |
| C.5 | 前後鏡頭切換位置 | 左下角或右下角，圖示按鈕 | ☐ |
| C.6 | 底部 `navigationBarsPadding()` | 不被系統導航列遮擋 | ☐ |

### UI-D：頂部工具列佈局
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| D.1 | 設定齒輪（左上） | 點擊進入 Settings 頁面 | ☐ |
| D.2 | 閃光燈模式切換（頂部中間） | 循環切換 Auto / On / Off | ☐ |
| D.3 | 右上角圖示功能定義 | 需對照 SSOT 確認用途（AI 開關？） | ☐ |
| D.4 | 頂部不顯示文字指令 | 無「未偵測到主體」類文字 | ☐ 🔴 |

### UI-E：相機預覽區域
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| E.1 | 預覽畫面填滿可用區域 | 無多餘黑邊（可有 aspect ratio 差異的 letterbox） | ☐ |
| E.2 | 三分網格線 | 可見但不搶眼，透明度適中 | ☐ |
| E.3 | 對焦指示器 | 點擊對焦時出現圓形指示器（白→綠→消失） | ☐ |
| E.4 | 無除錯文字殘留 | 無 FPS 計數器、無座標數值、無 ML Kit 標籤 | ☐ |
| E.5 | 螢幕方向鎖定 | 相機畫面鎖定為 Portrait | ☐ |

### UI-F：圖片瀏覽功能（Gallery/Photo Review）
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| F.1 | 拍照後可查看照片 | 拍攝完成後提供查看入口 | ☐ |
| F.2 | 最近照片 thumbnail | 底部左下角顯示最近拍攝照片 | ☐ 🔴 |
| F.3 | 點擊 thumbnail 進入全屏瀏覽 | 可左右滑動瀏覽歷史照片 | ☐ |
| F.4 | 分享/刪除功能 | 基本照片管理操作 | ☐ |

---

## D1-D8 逐日功能驗測清單

### D0：分支切割（Branch Cutover）
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| 0.1 | `git branch` 確認在 `feature/cueshot-mvp` | ✅ | ☐ |
| 0.2 | `git log --oneline -1` 確認 base commit | `bad1400` 或其後裔 | ☐ |

### D1-D2：Core Pipeline（CameraX + ML Kit + IMU）
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| 1.1 | App 冷啟動不 crash | 正常進入相機畫面 | ☐ |
| 1.2 | 相機預覽畫面流暢 | 60fps，無卡頓 | ☐ |
| 1.3 | 三分網格線顯示 | 可見的九宮格引導線 | ☐ |
| 1.4 | **拍攝畫面無任何文字標籤** | 零 Text overlay | ☐ 🔴 |
| 1.5 | **拍攝畫面無角度數值顯示** | 角度僅透過 Edge Warmth 傳遞 | ☐ 🔴 |
| 1.6 | 快門按鈕可拍照 | 點擊後保存照片 | ☐ |
| 1.7 | 前後鏡頭切換 | 點擊切換按鈕正常切換 | ☐ |
| 1.8 | 權限請求（Camera + Storage） | 首次啟動時請求，GPS 不在此時請求 | ☐ |

### D3-D4：Edge Warmth + 角度引導
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| 3.1 | Edge Warmth 邊緣暖光可見 | 手機傾斜時邊緣出現琥珀金 `#D4A030` 漸層，alpha ≤ 0.20 | ☐ |
| 3.2 | Edge Warmth 隨 IMU 即時響應 | 60fps 跟隨手機旋轉，延遲 < 16ms | ☐ |
| 3.3 | 手機水平放置時 Edge Warmth 消失 | pitch ≈ 0°, roll ≈ 0° 時無暖光 | ☐ |
| 3.4 | 手機傾斜 45° 時 Edge Warmth 明顯 | 底部邊緣暖光最強 | ☐ |
| 3.5 | **無角度數字顯示** | 僅 Edge Warmth 視覺引導 | ☐ 🔴 |

### D5-D6：Halo 系統 + Portrait 模式
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| 5.1 | 偵測到人臉時 Halo 出現 | 琥珀金 `#D4A030` 圓環圍繞人臉，alpha 0.25 | ☐ |
| 5.2 | 人臉移動時 Halo 跟隨 | 平滑跟隨（EMA α=0.3 平滑），無明顯抖動 | ☐ |
| 5.3 | 構圖對齊後 Halo 變綠 | `#50C864` + 呼吸韻律（alpha 0.10↔0.18, 2000ms） | ☐ |
| 5.4 | 無人臉時 Halo 消失 | 平滑淡出 | ☐ |
| 5.5 | 場景自動判斷：人臉 → Portrait | Logcat 確認 CameraMode 切換為 Portrait/Normal | ☐ |
| 5.6 | 場景自動判斷：pitch > 35° → Food | Logcat 確認（D9 前此模式可能無完整 UI） | ☐ |
| 5.7 | **「未偵測到主體」文字不應出現** | 無文字，僅 Halo 消失表示無主體 | ☐ 🔴 |

### D7：PTF 模式 UI
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| 7.1 | 「遞手機」Tab 可見於底部 | 底部全寬 36dp | ☐ |
| 7.2 | 點擊「遞手機」進入 PTF 模式 | 模式切換，300ms 動畫，零 CameraX rebind | ☐ |
| 7.3 | PTF 模式下快門 1 秒冷卻 | 進入後 1 秒內快門不可按 | ☐ |
| 7.4 | 再點 Tab 退出 PTF | toggle 行為 | ☐ |
| 7.5 | ✕ 按鈕退出 PTF | 快門左側純圖示按鈕 | ☐ |
| 7.6 | 首次進入 Bottom Sheet 提示 | 20% 高度，`max(screenHeight * 0.20, 96.dp)` | ☐ |
| 7.7 | PTF 模式零硬體切換 | 不 rebind CameraX，不 close ML Kit，不校準 IMU | ☐ |

### D8：PTF 規則引擎 + Halo 牽引
| # | 驗測項 | 預期 | 結果 |
|---|--------|------|------|
| 8.1 | PTF 模式偵測到人臉 → 金色 Halo | `#D4A030` alpha 0.25，跟隨 face bbox | ☐ |
| 8.2 | PTF Halo 引力場偏亮弧 | 偏亮側指向人臉需移動的方向，3dp 位移 | ☐ |
| 8.3 | PTF 三規則全對齊 → 綠色 Halo | `#50C864` alpha 0.35 + 呼吸韻律 | ☐ |
| 8.4 | PTF 金 → 綠轉場 | 300ms 平滑過渡，無閃爍 | ☐ |
| 8.5 | PTF 無人臉 → Halo 不顯示 | NO_FACE sentinel，Halo 隱藏 | ☐ |
| 8.6 | PTF 退出後 Normal Halo 恢復 | 回到 Normal 模式的琥珀金 Halo 行為 | ☐ |
| 8.7 | EMA Lifecycle Reset | App 背景→前景後 Halo 無閃跳 | ☐ |
| 8.8 | Logcat `[D8-B1-T3]` 輸出 | PTF alignment pipeline 有 isAligned/offset/failedRules 日誌 | ☐ |

---

## 排查優先序（CSO 建議）

### 第一波：Critical 修復（阻塞所有後續開發）
1. **P1 + P2：移除所有文字 overlay** — 這是產品 DNA 級問題，不修就不是 Cueshot
2. **P4 + P5：底部按鈕改為純圖示** — 「拍照」「遞手機」文字標籤違反 icon-first 原則
3. **P3：確認 Halo / Edge Warmth 管線是否連通** — 如果 Ambient UI 雙通道都沒生效，D3-D8 的工作等於沒上線

### 第二波：功能補齊
4. **P6：圖片瀏覽入口** — 確認是未實作還是被隱藏，MVP 需要基本的拍後查看能力
5. **P7：頂部工具列圖示釐清** — 確認每個圖示的功能定義
6. **UI-B：語言預設改英文** — 清除所有硬編碼中文，走 strings.xml

### 第三波：逐日功能驗證
7. 按 D1 → D8 順序逐項驗測
8. 每個 FAIL 項記錄截圖 + Logcat 片段

### 第三波：修復票分派
5. 根據 FAIL 項的性質分類：
   - **UI 殘留清理** → 15 min 快票，直接 /uc
   - **管線斷接** → 60 min 票，需 /normal 架構對齊
   - **邏輯錯誤** → 視複雜度分票

---

## 給 Gemini / Claude Code 的指示

### 給 Claude Code（立即執行）
```
第一步排查指令（在現有 codebase 上跑）：

=== P1-P2：文字 overlay 殘留 ===
1. grep -rn "未偵測" app/src/main/java/
2. grep -rn "主體" app/src/main/java/
3. grep -rn "Text(" app/src/main/java/**/CameraScreen.kt | head -40
4. grep -rn "°\|degree\|角度\|angle" app/src/main/java/**/CameraScreen.kt

=== P3：Ambient UI 管線 ===
5. grep -n "EdgeWarmth\|edgeWarmth\|edge_warmth" app/src/main/java/**/CameraScreen.kt
6. grep -n "HaloGuidanceLayer" app/src/main/java/**/CameraScreen.kt
7. grep -n "CameraMode\|cameraMode\|sceneMode" app/src/main/java/**/CameraViewModel.kt | head -20

=== P4-P5：中文文字標籤 + Icon-First ===
8. grep -rn "拍照\|遞手機\|設定" app/src/main/java/
9. grep -rn "拍照\|遞手機\|設定" app/src/main/res/values*/strings.xml
10. grep -rn "Text(" app/src/main/java/**/CameraScreen.kt | grep -v "//" | head -50

=== P6：圖片瀏覽入口 ===
11. grep -rn "Gallery\|gallery\|Thumbnail\|thumbnail\|PhotoPreview\|photoPreview\|LastPhoto\|lastPhoto" app/src/main/java/
12. grep -rn "MediaStore\|ContentResolver\|lastCapturedUri" app/src/main/java/**/CameraViewModel.kt

=== UI 全局排查 ===
13. grep -rn "stringResource\|getString\|R.string" app/src/main/java/**/CameraScreen.kt
14. find app/src/main/res/values* -name "strings.xml" -exec cat {} \;
15. grep -rn "hardcoded\|TODO\|FIXME" app/src/main/java/**/CameraScreen.kt
```

### 給 Gemini（待 Claude Code 排查結果出來後）
根據排查結果產出修復交接單。修復票優先級：
1. 文字 overlay 清除（P1 + P2）
2. Ambient UI 管線修通（P3）
3. 其他驗測 FAIL 項

---

## 文件元資訊

- **文件版本**: D1-D8-Verification-v1.0-20260418
- **建立日期**: 2026-04-18
- **作者**: CSO 策略長 (Claude Opus 4.6)
- **觸發條件**: 實機截圖顯示 MVP 預期差異
- **下游動作**: Akira 逐項驗測 → Claude Code 排查 → Gemini 產修復票 → 修復後重驗
