# 檔名版號慣例（Naming & Version Convention）

**版本**: v1.0
**建立**: 2026-07-01
**來源**: CSO 裁決 R-T12
**性質**: 活文件（本檔名無版號 = 示範 N-1）

## N-1 角色分流

| 檔案本體 | 慣例 | 例 |
|---------|------|----|
| 快照型（immutable：新版=新檔、舊版保留）| **版號入檔名** | *_Prototype_v3_2.html / Prompt_v2 / 凍結 Contract / dated report |
| 活文件型（mutated-in-place：同份持續改）| **穩定 slug + 內文 `版本` metadata 權威 + changelog** | SSOT / Layout Spec / active Contract |

## N-2 權威來源

活文件的權威版本 = 內文 `版本` metadata。檔名僅穩定識別碼；檔名中的版號數字（若有）視為 opaque 裝飾字，**非版本宣稱**。故「檔名 vN vs 內文 vM」不構成 mismatch。

## N-3 遷移政策

- 新活文件即刻遵 N-1（clean slug 無版號）。
- 既有版號入檔名的活文件（SSOT、Layout）：檔名就地凍結（數字轉 opaque），內文 metadata 接管權威。
- clean-slug rename（去數字）= 延後 + 選配，僅於乾淨封板點各自獨立 GOV 票 + 完整 downstream sweep。**禁 mid-flight rename**（會 churn 在飛票 ALLOWED 路徑 + 觸禁區引用）。

## 現有檔案合規矩陣

| 檔案 | 本體 | 慣例狀態 | 動作 |
|------|------|---------|------|
| Cueshot_Layout_Spec_v2_5_1.md | 活文件 | ✅ 已凍結 slug（GOV-02）| 無 |
| SSOT_v4_0_6.md | 活文件 | ⚠️ 版號入檔名 | 凍結 opaque；clean-slug 遷移 = GOV-04 候選（封板點評估）|
| Cueshot_State_1-6_Prototype_v3_2.html | 快照 | ✅ 合規（版號入檔名）| 無 |
| Portrait_MVP_Kotlin_Contract_v0_3/v0_4.md | 快照（被取代版本）| ✅ 合規 | 無 |
| Prototyper Prompt v2 / Background Image Prompts v2 | 快照 | ✅ 合規 | 無 |

## 翻盤條件

- SSOT clean-slug 遷移 ROI 於封板點評估為正（一次 sweep 成本 < 未來 N 次 bump-sweep 累計）→ 開 GOV-04 執行。
