# INFRA-SV-GOV-01: SV 治理修正（EXCLUDE_ARGS rename/pathspec + Check A↔C 互斥 + 輪次計數語意）

**簽發日期**: 2026-07-21（R17 收案後起草，待 Akira 核定）
**簽發者**: Claude
**Mode**: normal → uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review
**前置依賴**: 無（與 MEO-T3 並行；ALLOWED 檔案集不相交）

## Verify Config
TICKET_ID=INFRA-SV-GOV-01
CREATED=2026-07-21
PRESET=feature
ALLOWED=.claude/skills/sv/scripts/cross_review.sh,structural_verify 腳本（開工時定位，R12 型定位條款，定位後 Rev 補登實路徑）,sv 回件模板文件（開工時定位）
FORBIDDEN=cross_review.sh 之 MEO_VERIFIER_REJECT_COUNT 接線（MEO-T2 已收案資產）,stop_orchestrator.sh,STATE.md
MAX_LINES=200
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: EXCLUDE_ARGS rename/pathspec 修正
- **現值**: rename 場景下 EXCLUDE_ARGS pathspec 失配（R15-a 事件：跨票 rename 需獨立 commit 分離規避）
- **目標值**: pathspec 於 rename 場景正確生效，排除範圍不因 rename 失效
- [ ] 完成

### AI-2: structural_verify Check A↔C 規格互斥消解
- **檔案**: structural_verify 腳本（開工時定位）
- **現值**: Check A 與 Check C 存在規格互斥（同輸入致必然一方誤報）
- **目標值**: 規格層消解互斥；禁以刪除任一 Check 了事
- [ ] 完成

### AI-3: 輪次計數語意明文化
- **現值**: 機械輪次／邏輯輪次二元帳為 R14–R16 判例（vault=邏輯帳 SSOT），sv 文件未明文
- **目標值**: 二元帳規則入 sv 治理文件；回件須同時標示兩帳號碼
- [ ] 完成

### AI-4: strict_verification 回件模板增補（R17 教訓 #10 落地）
- **現值**: 回件無未勾項強制盤點，致 R17 F1/F2（可達性 AC 與 AI-2 漏列於收案語句）
- **目標值**: 模板增列「未勾項全盤點」強制區塊——AC 與 Action Item 雙軌逐項列舉，含各項去向（轉票／豁免／裁決中）
- [ ] 完成

## Hard Constraints
1. 禁動 MEO 接線（BLOCKER 分支 kv_set 一行為 T2 收案資產）
2. AI-1 僅修 rename 失配，不得放寬既有排除範圍
3. AI-2 消解採規格層修正，禁刪 Check 降維
4. 本票不觸 MEO 訊號語意（訊號源過濾屬 MEO-T3 AI-5）

## Error Handling
- structural_verify 腳本定位失敗 → AI-2 停止回報
- Check A↔C 互斥根因屬需 CSO 裁決之規格衝突 → 停止回報並附衝突分析
- rename 場景無法真實復現 → 停止回報，禁 fixture 直注充當證據

## Acceptance Criteria
- [ ] **[行為性]** 真實 rename commit 場景下 EXCLUDE_ARGS 排除正確生效
- [ ] **[行為性]** Check A 與 Check C 同輸入並行執行無互斥誤報
- [ ] **[存在性]** 輪次二元帳規則明文入 sv 治理文件
- [ ] **[存在性]** 回件模板含未勾項全盤點強制區塊（AC+AI 雙軌）
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案（structural_verify 實路徑以 Rev 補登為準）
