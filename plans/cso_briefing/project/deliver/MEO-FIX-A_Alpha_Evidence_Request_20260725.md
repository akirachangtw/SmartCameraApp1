# α 過渡授權 — CSO 補證要求（Executor Evidence Request）

**日期**: 2026-07-25 ｜ **簽發**: CSO ｜ **對象**: Claude Code 執行者
**標的**: `MEO-FIX-A` 票面 Error Handling 內聯之「過渡治理 α」
**狀態**: α 剩餘第 3 次額度 **已凍結**（見 `MEO-FIX-A` Rev E Error Handling 凍結條款）

## 一、事由

你於 2026-07-25 動用 α 第 2/3 次釋放 `MEO_ESCALATION_PENDING`，回報品質良好——一級來源核校（Rev B/C/C.1/D 為同票草稿輪次而非實作階段）、自我更正先前錯誤推論、S82 逐項存證、STATE 四值 read-back 確認未動。**本次動用經 CSO 核驗受理，不撤銷。**

惟核驗過程中，CSO 對 α 條款本身取證發現一項規格缺口：

α 明定「判定採 **error class 字面比對**」，但經 grep 全 repo（`CLAUDE.md`、`ticket_pattern.md`、`qa/SKILL.md`、MEO SKILL/decision-tree/兩 hooks、`MEO-T3_Stop_Livelock_Adjudication_20260721.md`、`MEO-FIX-A` 票面本身）與 vault 決策庫 `search_decisions`，**未見「比對域」之字面定義**——即：合格 error class 之清單為何？比對的兩造為何（`LAST_ERROR_CLASS` 對 `MEO_WM_ERROR_CLASS`？對前次動用所記之 class？對某清單？）。

你回報之 read-back 值為 `LAST_ERROR_CLASS=Exit`，但「`Exit` ∈ α 合格類」目前無字面依據可供 CSO 核驗。

## 二、要求你回覆兩項（不需動任何檔案）

**要求 1 — α error class 比對域之字面出處**
- 若存在：提供**檔案路徑 + 行號 + 原文引用**（grep 佐證）。
- 若不存在：**如實回報「全域無定義」**，並附你所執行之 grep 指令與零命中結果。
- ⚠️ **MUST NOT** 依你的合理推測補全定義。「我理解 α 的意思是 X」不是字面出處。此為現在式主張須現在式證據律之直接適用。

**要求 2 — α 第 1 次動用之存證指針**
- S-row 為滾動行（S80/S81 已被 S82 覆寫），第 1 次動用之存證應在 git 歷史。
- 提供該次存證所在之 **commit SHA + 檔案 + 行**（`git log -S` 或 `git log -p -- .claude/PROGRESS.md` 可定位）。
- 目的：核驗「上限 3 次、逐次存證」之額度計數確為 2 而非更多。

## 三、凍結範圍與解凍

- **凍結者**：α 第 3 次額度。解凍條件二擇一——(i) 要求 1 提出字面出處且經 CSO 核驗成立；(ii) 證實無定義 → CSO 另行裁決字面化後方得動用。
- **不凍結者**：α 本身未被撤銷；已動用之 2 次不溯及既往（該 2 次於簽發時法下逐次存證，合規）。
- 凍結期間若再遇失敗需 α：**回 β（停止並回報 Akira）**。

## 四、附帶說明（非要求，供你理解）

`MEO-FIX-A` 已升版 **Rev E**（Codex Rev D 復審 R1–R3 已吸收：雙 immutable artifact 消解 audit_id 時序循環、`release_pending()` 指定為唯一 release mutation owner、AC 型別與檔案計數校正）。Rev E **仍在 `REPORT_FIX`**，須先通過 Codex 復審方得入 `/uc`。**本文件不構成 Rev E 之執行授權**——你依 Rule 4b/c 停等新指令的判斷正確，請繼續維持。

α 之最終退役路徑：Rev E 落地後 livelock 結構拆除，α 自然失效（其 scope 條款為「本票落地前」）。故本補證之目的不在延續 α，而在補全其存證軌跡的完整性。


---
**冊面收束尾註(CSO,2026-08-01)**:本件隨 MEO v1 執法層前提消滅型退役收束(registry CLOSED + STOPPED 語意 checkpoint)。兩項補證要求至失效日未獲執行者答覆——據實記載「未答即失效」,非已解決;α 第 3 次額度隨 v1 退役永久失效。裁決鏈=vault `2026-08-01_MEO_V1_Enforcement_Layer_Decommission.md`。
