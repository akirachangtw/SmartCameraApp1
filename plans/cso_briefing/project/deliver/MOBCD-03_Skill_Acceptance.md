# MOBCD-03 驗收記錄

**票號**: MOBCD-03
**收束日期**: 2026-09-02
**收束方式**: 部分達成收 CLOSED，行為性 AC 移交 MOBCD-04（Akira 2026-09-02 L2 裁定）

---

## §1 達成項

### AI-1 移植（達成）

`SmartCameraApp1/.claude/skills/ticket-run/SKILL.md` 已落地，經 `list_directory` 第一手確認存在於 APP skills 目錄（與 `uc`／`qa`／`sv`／`ticket-rigor-gate` 等既有 skill 並列）。

`tmp/verify_cmd.conf` 載 `TICKET_ID=MOBCD-03`、`DIFF_REF=f4562cf`，證桌機端曾依常規流程開工本票。

### 實機九步（達成，惟標的偏離腳本）

AI-2 原規定標的為本票自身。實際執行時，操作者自清單選取**真票**為標的，九步完整走通，冊面留下正向證據：

| 標的票 | note 字面 | 取證 |
|--------|----------|------|
| `INFRA-SV-GOV-01` | `2026-09-02 16:33 執行起始｜來源：手機 Remote Control（Claude Code @SmartCameraApp1）` | `registry_list(lifecycle="IN_PROGRESS", fields=["note"])` Desktop 第一手 |
| `MEO-T3` | `2026-09-02 16:36 執行起始｜來源：手機 Remote Control（Claude Code @SmartCameraApp1）` | 同上 |

**此二筆證明**：步驟 1（清單）、4（數字選取）、6（確認）、7（`set_status` 成功，含 `expected_revision` CAS 通過）、9（`append_note` 落冊）**全數運作**。且 note 格式與 MOBCD-02 步驟 9 規格**逐字相符**，含專案名已正確改為 `@SmartCameraApp1`（AI-1 三處差異之第三處獲正向驗證）。

**G-3 結案**：APP isolated project 之 registry 寫入通道**完整放行**，由上述兩次 `set_status` 實機成功確認，非僅探針推定。MOBCD-03 票面 HC-5 之要求已滿足。

### 實測污染之還原

上述兩張為真票，實測後掛 `IN_PROGRESS` 但無實際開工。依 Akira 2026-09-02 L2 裁定，兩張均已還原 `PENDING`（rev 130／131），並各追加還原說明 note（rev 132／133）。`reviews` 四欄依原值帶回（`MEO-T3` 之 `traycer=N_A` 保留）；兩票均無 checkpoint，無 KAIROS #26 洗白風險。

---

## §2 未達成項（移交 MOBCD-04）

| AC | 狀態 | 移交理由 |
|----|------|---------|
| N≥2 清單編號連續 | 未逐項記錄 | 清單功能已由兩次成功選取間接證實，惟未留編號字面 |
| 步驟 4 超範圍數字 → 重列 | 未測 | 負向路徑 |
| 步驟 6 非 `y` → 維持 PENDING | 未測，且**將失效** | MOBCD-04 AI-1 將 `y` 改為 `1`/`2`，此 AC 之判準隨之改寫 |
| MISMATCH → 停止 | 未觸發 | 機會性驗收，未自然發生。**MUST NOT 以「邏輯上應會攔截」代替實測** |
| 步驟 5 回 `ok:true` 非 `NOT_FOUND` | **未驗** | 見 §3 |

**移交依據**：MOBCD-04 將改寫同一 SKILL.md 之步驟 5、6 並新增步驟 10。於九步版補測負向路徑，其證據於 MOBCD-04 落地當下即失效。依 §0 首要原則，不為即將作廢之版本補測。MOBCD-04 之 AI-4 涵蓋全部上列項目之十步版等價驗收。

---

## §3 遺留疑點（MUST 於 MOBCD-04 優先確認）

**步驟 5 是否曾真正成功執行，無正向證據。**

- G-1 已證 `read_file_safe` 不接受相對路徑，而步驟 5 之修法（base-path 常數）為 AI-1 三處差異之第二處。
- 冊面證據（`set_status`／`append_note`）僅涵蓋步驟 7、9，**無法反證步驟 5 之成敗**——摘要縱使失敗，流程仍可能被操作者略過而續行至確認。
- 操作者回報「實測結果可以選，選完之後按 Yes」，未提及摘要內容，故摘要是否正確顯示 Goal／ALLOWED **無字面證據**。

**判定**：步驟 5 於本票列為**未驗**，MUST NOT 以「AI-1 已完成」推定其正確。MOBCD-04 AI-2 將以 `grep_files` 定位擷取全面取代該步實作，屆時一併取得首次正向證據。

---

## §4 收束

- MOBCD-03 收 `CLOSED`，`reviews` 四欄 `N_A`（RAPID_MODE）。
- 驗收檔即本檔，落 `plans/cso_briefing/project/deliver/`。
- 後續：MOBCD-04（APP，選項化＋步驟 10＋定位擷取）→ MOBCD-05（PLT 同步，含 G-1 base-path）。

**End of MOBCD-03_Skill_Acceptance.md**
