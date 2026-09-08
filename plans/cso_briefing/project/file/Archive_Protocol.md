# Archive_Protocol.md — 終極歸檔協議 v3.3(M1:記憶圖譜指標原子雙寫)

**版本**: v3.3
**Revision**: v3.3（2026-09-08，SMTR 抽檔）— 自 `Instructions_for_Claude.md` v2.8.8 抽離為按需載入檔；內容逐字保真，僅刪除一句與歸檔無關且與「檔案存取速查」重複之工具說明（消融測試：刪除後行為不變）。常駐端僅留觸發器、強制載入令與恆時圖譜邊界。
**載入時機**: Akira 明示「確認決議」/「歸檔」/「建檔」時，MUST 先載入本檔再執行；未載入禁呼叫 `archive_decision`。
**適用角色**: CSO / TRA / UITA / SMTR / SKAD

---

## 核心意圖(你的唯一目標)

> **歸檔的唯一目的:讓三個月後、零上下文的 Claude 讀完這份文件後,能獨立做出與當初一致的判斷,並回答任何衍生的歧義問題。**

達成這個意圖需要下列條件:
1. **結論可定位**:TL;DR + 決策結論,讓搜尋命中後 2 秒內知道「這份在講什麼」
2. **推理可重建**:保留完整的因果鏈,讓未來 Claude 能回答「為什麼選 A 不選 B?在什麼條件下 A 會失效?」
3. **字面精準度**:嚴格依據字面共識提煉,禁止自行腦補對話中未提及的實作細節。
4. **議題原子化**:不同議題須原子化歸檔以提升未來搜尋命中率及閱讀專注力。

---

## 歸檔執行步驟

### Step 1:提煉 — 剔除雜訊,保留信號

**剔除**:寒暄、致謝、重複論述、被否決且無參考價值的分支、未達共識的模糊提議。
**保留**:最終結論、推理轉折點、量化依據、否決方案及其具體理由、前置依賴、跨決策引用。
**修訂檢核(強制)**:凡本次裁決修訂既有裁決者,Key Decisions 或 Context MUST 字面寫出被修訂決策之 topic 全名——此為 6.3 建 supersedes 邊之唯一合法字面依據,漏寫即事後無合法建邊路徑。

### Step 2:填充結構化模板

```markdown
> **TL;DR**: [一句話結論,30 ~ 50 字]

## Context(背景)
[觸發痛點或約束,2-3 句]

### Done
- [成果列表]

### Current State
| 項目 | 狀態 | 備註 |
|------|------|------|

### Key Decisions - [內容須具備事後歧義校對或參閱的依據]
| # | 決策 | 理由 | 否決的替代方案 | 翻盤條件 |
|---|------|------|---------------|---------|

### 教訓收割(待 KAIROS) - [無教訓收割可空白,內容須具備事後歧義校對或參閱的依據]
| # | 教訓 | 事件支撐 | 防護措施 | 收錄狀態 |
|---|------|---------|---------|---------|

### 產出文件清單 - [本 Session 的產出物是決策歸檔本身]
| # | 文件 | 用途 | 部署/儲存位置 | 狀態 |
|---|------|------|-------------|------|

### TODO(按優先序)
| # | 動作 | 執行者 | 前置依賴 |
```

### Step 3:品質自檢(寫入前強制)

用一個問題自測:**「如果我刪掉 [Key Decisions/KAIROS],這份文件是否退化為一份無法消解歧義的空殼?」** 答案必須為「是」——如果刪掉 [Key Decision/KAIROS] 後文件品質沒有明顯下降,代表你的 [Key Decisions/KAIROS] 寫得太薄,回去補。

### Step 4:強制調用工具

你必須主動呼叫 `archive_decision` 工具:
- `role`: 本次對話的角色名稱(`CSO` / `TRA` / `UITA` / `SMTR` / `SKAD`)
- `topic`: 英文短句,格式為 `Domain_Decision`(例:`Obsidian_Archive_Pipeline`、`PMF_Gate_Design`)。禁止空格與特殊字元。
- `content`: Step 2 模板的完整 Markdown 輸出
- `tags`: 至少包含 `[角色名, 系統模組名]`,可追加 `Decision` / `Risk` / `Architecture` / `Workflow` / `PMF` 等語意標籤

### Step 5:確認寫入

在工具成功回報寫入完成**之前**,你不可單方面宣告任務結束。寫入成功後,回報檔案路徑與 index 更新狀態。

### Step 6:圖譜指標雙寫 Memory MCP(archive_decision 成功後強制執行)

前置:Step 5 已回報 vault 檔案路徑與 index 更新成功。vault 為決策內容唯一 SSOT;圖譜僅存指標與關係。**圖譜寫入失敗不得回滾或否定 Step 5 之歸檔成功**,僅標記「圖譜待補」入 TODO 續辦。

- **6.1 查重判定**:先 `search_nodes(query=<topic>)`。`search_nodes` 為子字串匹配,「已存在」以回傳清單中 **name 完全一致**者為唯一判準。
  - decision 型完全一致命中 → **異常,停止本步並回報 Akira**(decision 實體禁重複、禁 add_observations——與 6.2 禁增條款一致)。
  - 非 decision 端點完全一致命中 → 跳過建立,直接進 6.3。
- **6.2 Decision 指標實體**(每筆決策檔一個):
  - `name`:與 vault topic 及檔名完全一致(命名紀律為子字串搜尋命中率唯一槓桿)
  - `entityType`:`decision`
  - `observations`(僅此二則,禁增):`vault: <相對路徑>`、`TL;DR: <30–50字>`
  - **TL;DR 來源紀律**:自 vault 檔內字面提取,可裁剪、不可改寫語意;index 層 TL;DR 缺失時以 `read_file_safe` 小額探測(max_chars 500–2000)取檔內原文;**禁憑檔名臆造**。
- **6.3 關係寫入**:`create_relations` 依裁決內容連結,relationType 限受控字典(見「圖譜邊界規則 §B」),且僅建**檔案內字面明載**之關係(D-I.3)。**`supersedes` 為強制檢查項**:本決策若推翻既有決策(含條件觸發型翻盤與**前提消滅型退役**),必建 `<新topic> supersedes <舊topic>`;無翻案則免。建 supersedes 邊時,新檔內文 MUST 字面記載**退役型態**(條件觸發型翻盤/前提消滅型退役/**翻盤條件未成立之明示 supersede**——第三型限 L2 明示裁決,檔內 MUST 引裁決字面)。
  **檔級指標先例(E-a)**:supersedes 邊端點為檔級 topic;部分翻盤(僅及舊檔個別 Key Decision)仍建邊,**翻盤範圍以新檔內文為準**,遍歷者查證必回 vault。
- **6.4 關係端點實體**:端點不存在時可順建,observations ≤2 則且僅穩定事實。端點型別含 `decision`——僅限**既存 vault 決策之指標回填**(BF-3 惰性回填常設,依 `Memory_Graph_Backfill_Strategy`),實體規格依 6.2(含 TL;DR 來源紀律)。
- **6.5 Read-back**:以 `open_nodes([<topic>])` 精確驗證實體;本次有建邊者,`open_nodes` 須帶**雙端點**驗證關係回傳。命中方可回報完成;`search_nodes` 子字串命中不構成精確驗證。逾時依 userPreferences read-back 驗證規範處理,不可單方宣告成功。

---

## 圖譜邊界規則(完整版;常駐端僅留摘要)

- **A. 單寫者約定**:圖譜寫入僅限 Desktop 主 session,且限兩種觸發:①「確認決議」(本協議 Step 6);② **圖譜管理作業**(BF-4 種子/BF-3 回填/修復),須 Akira 明示授權並依對應決策檔規格執行。Claude Code/子 session 一律唯讀(`search_nodes`/`open_nodes`)。**⚠️ 本則對 Cueshot-memory 局部失效**:該圖譜三端共用,單寫者由 `Cueshot_Memory_Concurrent_Write_Model` D-F 宣告式串行取代(同時僅一宣告寫入端,切換須明確交接、禁隱式接管)。Desktop 任 Cueshot 宣告寫入端時,**首次 mutation 前 MUST 依 D-F.2 執行時戳快照**(同目錄 `.bak`、保留 5 份);禁以「寫入量小應不衝突」略過(D-F.2 禁語義化推定)。Claude Code 端由 CUE-SNAP-01 hook 機械強制。
- **B. 受控字典**:entityType 六種 `project/repo/workflow/decision/policy/component`;relationType 六種 `contains/depends-on/implements/requires/applies-to/supersedes`。擴充字典須經確認決議。Cueshot-memory 僅開放 `component/repo/workflow` 三型(D-B),`decision/policy` 禁入。
- **C. 禁寫**:secrets/個資/token、未驗證推測、原始 log/堆疊、高頻變動值、檔案內容或大片段、**決策內文**(僅指標)。
- **D. `read_graph` 日常禁用**(無界讀取),對兩圖譜**均無例外**(D-G,含體積尚小者);僅限圖譜管理/修復 session 且需 Akira 明示。
- **E. 新裁決之 decision/policy 實體永久僅經本協議「確認決議」觸發**,禁止 agent 於任何其他流程自動寫回新裁決。既存 vault 決策之**指標回填**不在此禁,依 A-② 通道與 `Memory_Graph_Backfill_Strategy` 規格執行。

---

**End of Archive_Protocol.md v3.3**
