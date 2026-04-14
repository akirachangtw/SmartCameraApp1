# Cueshot 工作交接 — 20260414_1359

## 本次完成的變更

| 檔案 | 函數/區塊 | 變更內容 |
|------|----------|---------|
| `domain/composition/rules/EyeLineRule.kt` | 全新建立 | 眼線水平度規則；讀取 `leftEyeNormalized`/`rightEyeNormalized`；計算右→左眼向量 roll 角；fallback=30f（無 landmark 時）；`weight=0.15f`、`confidenceFactor=0.75f` |
| `domain/composition/rules/GazeRule.kt` | 全新建立 | 注視方向規則；讀取 `headEulerAngleY`；`abs(yaw)` 線性衰減至 MAX_YAW_DEG=45°；`weight=0.15f`、`confidenceFactor=0.70f` |
| `di/CompositionModule.kt` | `bindEyeLineRule()` / `bindGazeRule()` | 加入 `@Binds @IntoSet` 綁定 EyeLineRule 和 GazeRule |
| `test/.../EyeLineRuleTest.kt` | 全新建立 | 14 個測試：空 faces、像素座標、負值、無 landmark fallback、完美水平、小角度、15°/30°、多臉選最大、IMU 無關 |
| `test/.../GazeRuleTest.kt` | 全新建立 | 14 個測試：空 faces、像素座標、負值、yaw=0 滿分、±5°、±45°、90°、對稱性、多臉選最大、IMU 無關 |

**前一 Session 的 3 個 commit（D6-B1-T1）本對話已完成**：
- `a09c503` feat(domain): introduce FaceLandmarkData DTO and evolve CompositionRule interface (D6-B1-T1)
- `3aff940` test(domain): migrate rule tests to FaceLandmarkData interface (D6-B1-T1)
- `61e2f3d` docs(progress): D6-B1-T1 DONE, add D6 tickets table

**⚠️ 未 commit 的變更（本對話新增）**：
- `EyeLineRule.kt`（新建）
- `GazeRule.kt`（新建）
- `CompositionModule.kt`（修改）
- `EyeLineRuleTest.kt`（新建）
- `GazeRuleTest.kt`（新建）
- 需遵循鐵律 2：Commit 1（原始碼）、Commit 2（Test）、Commit 3（Docs/PROGRESS）

## 當前阻塞點

無

## 下一步待辦

1. **[立即] 執行 `/qa`** 驗證 EyeLineRule + GazeRule 編譯與測試（預期總測試數約 161t）

2. **[立即] git commit — 原始碼（鐵律 2 Commit 1）**：
   ```
   git add app/src/main/java/.../EyeLineRule.kt \
           app/src/main/java/.../GazeRule.kt \
           app/src/main/java/.../di/CompositionModule.kt
   git commit -m "feat(domain): add EyeLineRule and GazeRule with DI wiring (D6-B1-T2/T3)"
   ```

3. **[立即] git commit — Test（鐵律 2 Commit 2）**：
   ```
   git add app/src/test/.../EyeLineRuleTest.kt \
           app/src/test/.../GazeRuleTest.kt
   git commit -m "test(domain): add EyeLineRuleTest and GazeRuleTest (D6-B1-T2/T3)"
   ```

4. **[立即] git commit — Docs（鐵律 2 Commit 3）**：
   ```
   git add .claude/PROGRESS.md
   git commit -m "docs(progress): D6-B1-T2/T3 DONE"
   ```

5. **[D6-B2] 實作 HaloGuidanceLayer 視覺回饋層**：
   - 根據 PROGRESS.md D6-B2 Ticket 規格實作
   - 參考 SSOT §視覺設計：Halo 統一圓形、琥珀金 `#D4A030`（未對齊）/ 綠色 `#50C864`（對齊）

6. **[D6 KAIROS #2] 執行 `/dream`（日期：2026-04-16）**：
   - 觀測 D6_PROBE Logcat 數據（在 Pixel 9a 上對準人臉，記錄 eulerY/eye landmark 輸出值）
   - 處理 `D5-TECH-DEBT-01`（Compose Test 基礎設施升級）
   - 處理 `SSOT-TYPE-DRIFT-01`（RuleScore vs RuleResult 命名統一）

## 關鍵決策 (本次對話中做出的)

| 決策 | 理由 | 影響範圍 |
|------|------|---------|
| EyeLineRule 無 landmark 回傳 fallback score=30f（非 0f） | 避免無 landmark 情況被誤判為「嚴重違規」而推高優先級；30f 表示「資訊不足，輕微警示」 | `EyeLineRule.kt`、`EyeLineRuleTest.kt` |
| EyeLineRule MAX_TILT_DEG=15f | 15° 為攝影實務上明顯傾斜的閾值；超過即建議校正 | `EyeLineRule.kt` |
| GazeRule MAX_YAW_DEG=45f | 45° 為 ML Kit headEulerAngleY 的合理側臉閾值；超過即屬「側身」構圖 | `GazeRule.kt` |

## 需帶入新對話的 Context

- `CLAUDE.md`（規則 #10 Bash safety、KAIROS #11/#12）
- `PROGRESS.md`（D6 Tickets：B1-T2/T3 DONE 待更新，B2 TODO）
- `domain/composition/rules/EyeLineRule.kt`（剛建立，供 /qa 參考）
- `domain/composition/rules/GazeRule.kt`（剛建立，供 /qa 參考）
- `di/CompositionModule.kt`（已加入 4 個 Rule 綁定）
- `domain/model/FaceLandmarkData.kt`（EyeLineRule/GazeRule 的輸入型別）

## 不需帶入的 Context

- D6-B1-T1 實作過程（FaceLandmarkData、ThirdsRule/HeadroomRule 機械修復）——已 commit 封板
- 3 個 D6-B1-T1 commit 的詳細過程
- SESSION_HANDOFF_20260414_1345.md 的落差分析討論
- 所有 `/uc` / `/normal` 模式切換討論
- 交接文件準確性審查討論
