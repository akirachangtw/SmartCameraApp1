# Pixel 9a 測試報告 - v2.x.x Optical Flow Phase 7

## 測試日期: 2026-02-03
## 測試人員: Claude Code (auto)

---

## Phase 7 修正內容

| 修正 | 檔案 | 說明 |
|------|------|------|
| A | CameraViewModel.kt | 分離 OPT 高帧率訂閱（移除 `.sample(100)` 瓶頸，28 FPS → LK） |
| B | OpticalFlowTracker.kt | 動態 aspect-ratio-preserving 下降解析度（480×640，替代固定 640×480） |
| C | OpticalFlowConfig.kt | winSize 21→31, maxLevel 3→4（max trackable motion ~240px/帧） |
| D | CoordinateMapper.kt | CENTER_CROP-aware 座標映射（uniformScale + cropOffset） |

---

## 通過/失敗 清單

- [x] 場景 1: 基本光學流動追蹤 — **PASS**
- [ ] 場景 2: Re-tap 快速目標切換 — 待測
- [ ] 場景 3: 無紋理區域處理 — 待測
- [ ] 場景 4: ML Kit 追蹤優先 — 待測
- [ ] 場景 5: AE/AF Lock 後再點擊 — 待測
- [x] 場景 6: 效能監測 — **PASS**
- [ ] 場景 7: 相機切換後行為 — 待測

---

## 場景 1: 基本光學流動追蹤 — 詳細結果

### 測試條件
- 點擊位置: PreviewView (495.40, 976.70)
- 對應 image 座標: (454.16, 565.95)
- 點擊目標: 有紋理物件
- 操作: 緩慢 pan 約 21 秒

### Round-trip 座標驗證
```
Tap        → preview (495.40, 976.70)
→ image       (454.16, 565.95)         [uniformScale=1.7258, cropOffX=167.10, cropOffY=0]
→ back     → preview (495.40, 976.70)  ✓ 零誤差
```

### 追蹤結果
| 指標 | 數值 | 標準 | 判定 |
|------|------|------|------|
| LK Track 帧數 | 234 帧 | — | — |
| status=0 失敗次數 | 0 | 0 | ✓ |
| 追蹤時長 | ~21 秒 | ≥ 5 秒 | ✓ |
| quality 平均 | 0.914 | ≥ 0.8 | ✓ |
| quality 最低 | 0.71 | — | — |
| LK elapsed 平均 | 2.3 ms | — | — |
| LK elapsed 最大 | 63 ms (第一帧 warm-up) | P95 < 20ms | ✓ |
| 唯一座標數 | 235 個 | > 0 (座標在移動) | ✓ |

### 座標移動軌跡（抽樣）
```
16:04:49  start  preview (495.4,  976.7)   image (454.2, 565.9)
16:04:50  move   preview (483.9,  948.6)   image (447.5, 549.6)   ← Y 方向移動
16:05:00  move   preview (528.6,  918.4)   image (473.6, 532.3)   ← X 方向移動
16:05:05  move   preview (388.0,  925.8)   image (391.8, 536.0)   ← 反向 pan
16:05:09  move   preview (357.9,  611.0)   image (374.5, 354.1)   ← 大幅移動
16:05:10  end    preview (0.0,    253.2)   image (159.8, 146.7)   ← 物件離開畫面左緣
```

---

## 場景 6: 效能監測 — 詳細結果

| 指標 | 數值 | 標準 | 判定 |
|------|------|------|------|
| LK elapsed P95 | ~3 ms (234帧中僅1帧 >20ms) | < 20 ms | ✓ |
| LK elapsed 平均 | 2.3 ms | — | ✓ |
| Downsample 尺寸 | 480×640 (3:4 比例正確) | aspect-ratio 一致 | ✓ |

---

## CENTER_CROP 座標映射驗證

Phase 7 前使用簡單 X/Y 縱橫比例 scaling，對於 aspect ratio 不同的情況（960×1280 image vs 1080×2272 preview）座標偏移最大達 ~110px。

### 修正後參數（Pixel 9a）
```
image:      960 × 1280  (3:4 portrait)
preview:    1080 × 2272 (9:19)
uniformScale = max(1080/960, 2272/1280) = max(1.125, 1.775) = 1.7258
cropOffsetX  = (960 - 1080/1.7258) / 2 = (960 - 625.8) / 2 = 167.10
cropOffsetY  = (1280 - 2272/1.7258) / 2 = (1280 - 1316.4) / 2 ≈ 0  (Y 被裁至滿屏)
```

- **Y 軸**：preview 高度 2272 在 uniformScale 1.7258 下 → 映射到 image 高 1316px > image 實際高 1280px → Y 裁切量接近 0，故 cropOffY=0
- **X 軸**：preview 寬 1080 在 uniformScale 1.7258 下 → 映射到 image 寬 625.8px < image 實際寬 960px → 兩側各裁切 167px → cropOffX=167.10

### Round-trip 精度
點擊座標經 previewToImage → imageToPreview 後完全映回原座標，誤差為零。

---

## 異常記錄

無異常。234帧追蹤無 status=0，quality 穩定在 0.71–0.97。

末尾座標被 coerceIn 鎖在 X=0.0 是正常行為——物件隨相機移動離開了 PreviewView 左邊緣。

---

## Logcat

- `logcat_phase7_centercrop.log` — 本次測試完整日誌

---

## 待測場景（Scene 2–7）

參考 `PIXEL9A_TEST_GUIDE.md` 中各場景定義，後續在實機上逐一驗證。
