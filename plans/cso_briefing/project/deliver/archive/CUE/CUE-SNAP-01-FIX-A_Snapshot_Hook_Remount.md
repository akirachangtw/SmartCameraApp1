# CUE-SNAP-01-FIX-A: Cueshot-memory 快照 hook 重掛載至 SmartCameraApp1 並補真實觸發驗收

**簽發日期**: 2026-07-24
**簽發者**: Claude (SMTR / CSO)
**Mode**: normal → uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review
**前置依賴**: 無（原票 CUE-SNAP-01 已全數退回，本票彙整其全部內容，原票不再獨立執行）

## 檔案治理/來源說明

- **原票正本與副本均已滅失**：`Pending/` 正本先前遺失；`docs\patch\` 副本與錯置實作已由 Akira 於 2026-07-25 還原 mcp-ticket-platform 原狀時刪除（正向取證：該 repo `.claude\` 整目錄 NOT_FOUND、`docs\patch\` 21 項清單無 CUE-SNAP-01）。原票 HC／AI／AC 全文已由本票承接，**本票即唯一 SSOT**
- 原錯置實作 `snapshot_cueshot_memory.py`（3,420 bytes）之原始碼全文保全於**附錄 A**（滅失前已於 2026-07-24 session 讀入存證）
- 錯置後果：腳本以 `os.getcwd()/.mcp-memory` 解析目標，於該 repo 指向 17 bytes 樁檔；真實 Cueshot 圖譜（`SmartCameraApp1\.mcp-memory\memory.jsonl`）零覆蓋
- 上游裁決：`Cueshot_Memory_Concurrent_Write_Model` D-F.1 / D-F.2；`Cueshot_Snapshot_Carrier_And_BFC1_Seed_Spec` D-1 / D-2

## Verify Config
TICKET_ID=CUE-SNAP-01-FIX-A
CREATED=2026-07-24
PRESET=bugfix
ALLOWED=.claude/hooks/snapshot_cueshot_memory.py,.claude/settings.json
FORBIDDEN=.mcp-memory/memory.jsonl,.claude/skills/model-effort-orchestrator/hooks/,STATE.md,.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh
MAX_LINES=150
DIFF_REF=619b3b2

## Action Items

### AI-1: 腳本移植至本 repo
- **檔案**: `.claude/hooks/snapshot_cueshot_memory.py`
- **現值**: 本 repo 不存在此檔（實作錯置於 mcp-ticket-platform）
- **目標值**: 以**附錄 A 保全之原始碼**為基底新建於本 repo，並套用 AI-2、AI-5、AI-6 之腳本側修正
- [x] 完成

### AI-2: 目標解析禁用 CWD
- **檔案**: `.claude/hooks/snapshot_cueshot_memory.py`
- **現值**: `memory_dir = os.path.join(os.getcwd(), '.mcp-memory')`；`if not os.path.exists(memory_file): return 0`
- **目標值**: 目標以**絕對路徑**解析（環境變數 `CUESHOT_MEMORY_DIR` 優先，未設時退回 hook 腳本自身位置推導之 repo 根，禁用 CWD）；目標檔不存在時 **MUST** 輸出 stderr 警告後才 exit 0，禁靜默 return
- [x] 完成

### AI-3: 註冊位置更正
- **檔案**: `.claude/settings.json`
- **現值**: `hooks.PreToolUse` 僅含 `AskUserQuestion` 與 `Write|Edit|MultiEdit|Bash` 兩條目
- **目標值**: 新增一條目呼叫 `python .claude/hooks/snapshot_cueshot_memory.py`，不得移除或改寫既有兩條目
- [x] 完成

### AI-4: matcher 加 server 限定
- **檔案**: `.claude/settings.json`
- **現值**: （他 repo 原值）`create_entities|create_relations|add_observations|delete_entities|delete_relations|delete_observations`
- **目標值**: matcher **MUST** 限定至 Cueshot server（形如 `mcp__Cueshot-memory__(create_entities|create_relations|add_observations|delete_entities|delete_relations|delete_observations)`）；實際字面以 Claude Code 匹配語意之正向取證為準，取證結果回填本欄
- **正向取證回填**：本 session 實際掛載之 MCP server 字面為 `cueshot-memory`（小寫連字號，非票面 placeholder 之 `Cueshot-memory`），已採實測工具清單（`mcp__cueshot-memory__create_entities` 等）為準；settings.json 落地 matcher 為 `mcp__cueshot-memory__(create_entities|create_relations|add_observations|delete_entities|delete_relations|delete_observations)`，並經真實 `create_entities`→`add_observations`→`delete_entities` 三連發觸發驗證命中（見 AC3/AC4 回填）
- [x] 完成

### AI-5: fallback 降級可偵測
- **檔案**: `.claude/hooks/snapshot_cueshot_memory.py`
- **現值**: `session_id = f"ppid_{os.getppid()}"`，僅輸出 stderr 警告，產物無標記
- **目標值**: 走 PPID fallback 時，快照檔名或同名 sidecar **MUST** 帶 `fallback` 標記，使降級於事後可由檔案系統辨識（承 D-2 偵測型護欄定位）
- [x] 完成

### AI-6: 輪替名單收窄，保護非 hook 產生之證據檔
- **檔案**: `.claude/hooks/snapshot_cueshot_memory.py`
- **現值**: `if f.startswith('memory.jsonl.') and f.endswith('.bak')`
- **目標值**: 僅刪除**嚴格符合 hook 自身命名樣式**者（`memory.jsonl.<YYYYmmdd_HHMMSS>.bak`，以 regex 全匹配）；任何不符該樣式之 `.bak` 或證據檔一律不納入輪替刪除名單
- [x] 完成

## Hard Constraints

1. 快照為**檔案複製**，禁以任何形式寫入或編輯 `memory.jsonl` 本體
2. session 內**單次 latch** — 首次寫類 mutation 觸發後，同 session 後續 mutation 不重複快照
3. hook 失敗不得阻斷唯讀工具（`search_nodes` / `open_nodes`）
4. 禁觸 MEO 既有 hook 之判定路徑與 latch 狀態
5. **禁以直呼腳本（`echo … | python …`）之輸出充當 `[可達性]` AC 之驗收證據**
6. 所有驗收證據 **MUST** 標明產生於本 repo（SmartCameraApp1）；他 repo 產生之證據一律不採計

## Error Handling

- Claude Code 對 MCP 工具名之 matcher 匹配語意（search / fullmatch）無法取證 → 停止回報
- 絕對路徑注入通道於 Claude Code hook 執行環境不可用 → 停止回報
- `[可達性]` AC 無法於本 session 完成（需另起 session 觸發） → 停止回報，禁以直呼腳本代替
- 修改 `.claude/settings.json` 後 Claude Code 無法啟動 → 立即還原並停止回報

## Acceptance Criteria

- [x] **[存在性]** `.claude/hooks/snapshot_cueshot_memory.py` 存在，且 `.claude/settings.json` 含 server 限定之 PreToolUse matcher
- [x] **[存在性]** 腳本內 `os.getcwd()` 之目標解析已消除（grep 命中數 = 0，實測 exit 1 無匹配）
- [x] **[可達性]** 於本 repo 起 Claude Code session，實際呼叫一次 Cueshot 寫類 MCP 工具後 hook 被觸發。**producer 證據欄**（執行後回填）：觸發時戳＝`20260725_011732`、產生之 `.bak` 檔名＝`memory.jsonl.20260725_011732.bak`（`mcp__cueshot-memory__create_entities` 觸發，session_id=`861c4ac0-8fe3-4f09-84c6-d331e9cc7efa`，非 PPID fallback，`.mcp-memory/.snapshot_latch_861c4ac0-8fe3-4f09-84c6-d331e9cc7efa` 同步生成）
- [x] **[行為性]** 同一 session 連續 3 次寫類 mutation，`.mcp-memory/` 僅新增 **1** 份 `.bak`（本 session 實測：`create_entities`→`add_observations`→`delete_entities` 三連發，`.mcp-memory/` 僅見單一 `.bak` + 單一 latch 檔，讀秒確認）
- [x] **[行為性]** 兩個獨立 session 各一次寫類 mutation，`.mcp-memory/` 共新增 **2** 份 `.bak`（跨 session 隔離；證偽靜默失效）— **2026-08-27 新 session 補測**：session 1（`861c4ac0-...`，2026-07-25）已有 `memory.jsonl.20260725_011732.bak`；本次新 session（`31def4fb-0e2c-4687-a0f3-cc005cefa60e`）呼叫真實 `mcp__cueshot-memory__create_entities`（探測實體，事後 `delete_entities` 撤回）後產生 `.snapshot_latch_31def4fb-...` + `memory.jsonl.20260827_213047.bak`，兩份合計恰 2；同 session 第二次 mutation（`delete_entities`）未再新增第三份（latch 生效）
- [x] **[行為性]** 僅執行 `search_nodes` / `open_nodes` 之 session 不產生任何 `.bak` — **2026-08-27 實測**：本 session 先呼叫 `mcp__cueshot-memory__search_nodes` 並等待 2s，`.mcp-memory/` 零新增檔案；隨後才呼叫 `create_entities` 觸發快照（見上）
- [x] **[行為性]** 目標檔不存在時，stderr 有警告輸出且 exit code = 0（禁靜默無操作）— **2026-08-27 實測**（HC-5 僅限制 [可達性] AC，本項為 [行為性]，scratch 目錄 `tmp/CUE-SNAP-01-FIX-A/`、經 `CUESHOT_MEMORY_DIR` 導向、事後已清除）：`CUESHOT_MEMORY_DIR=<空目錄>` 呼叫腳本，stderr 輸出 `Warning: Cueshot-memory target not found: ...`，exit_code=0
- [x] **[行為性]** 刻意使 hook 腳本失敗，寫類 mutation 仍可完成（fail-open）— **2026-08-27 實測**：令 `memory.jsonl` 為目錄（非檔案）觸發 `shutil.copy2` 內部 `PermissionError`，stderr 輸出 `Warning: Cueshot-memory snapshot failed: [Errno 13] Permission denied: ...`，exit_code=0（fail-open 生效，未阻斷）
- [x] **[行為性]** 快照達 6 份時最舊者被刪、穩定維持 5 份；且不符 hook 命名樣式之證據檔**未被刪除**— **2026-08-27 實測**：scratch 目錄佈置 6 份符合命名之 `.bak`（mtime 08-20~08-25）+ 1 份不符命名之最舊證據檔（mtime 08-01）→ 觸發第 7 次快照 → 結果精確保留最新 5 份符合命名檔（08-22~08-25+新增 08-27），刪除 2 份最舊符合命名檔（08-20/08-21），不符命名檔（雖為全體最舊）**未被觸碰**
- [x] `assembleDebug` PASS
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案

## 後續連動（不在本票範圍）

- **CUE-SNAP-02：取消**。錯置之 hook 腳本、`settings.json` 註冊與票檔副本已由 Akira 於 2026-07-25 手動還原刪除（取證同上「檔案治理/來源說明」），無退役標的
- **latch 檔堆積**：`.snapshot_latch_*` 無清理機制且與圖譜同目錄，本輪未納入 8 點修正範圍，列 TODO 待裁
- `Cueshot_Memory_Concurrent_Write_Model` TODO #2（5 份輪替）由 AI-6 承接結案；TODO #3（三端並用行數對比）仍為獨立待辦

## 附錄 A：原實作原始碼存證（AI-1 之移植基底）

來源：`workflow\mcp-ticket-platform\.claude\hooks\snapshot_cueshot_memory.py`（3,420 bytes，於 2026-07-24 session 滅失前讀入；原檔已刪除，本附錄為唯一存本）。**注意：此為含缺陷之基底**，AI-2（CWD 解析）、AI-5（fallback 標記）、AI-6（輪替收窄）之修正必須套用，禁原樣移植。

```python
import sys
import os
import shutil
import json
from datetime import datetime

def find_session_id(data):
    if isinstance(data, dict):
        if 'session_id' in data:
            return str(data['session_id'])
        for v in data.values():
            res = find_session_id(v)
            if res:
                return res
    elif isinstance(data, list):
        for item in data:
            res = find_session_id(item)
            if res:
                return res
    return None

def main():
    try:
        # Check if memory.jsonl exists
        memory_dir = os.path.join(os.getcwd(), '.mcp-memory')
        memory_file = os.path.join(memory_dir, 'memory.jsonl')
        
        if not os.path.exists(memory_file):
            return 0

        # AI-3: Session Latch Fix
        # True session identity comes from stdin JSON payload provided by Claude Code.
        # We must NOT use a shared fallback like 'default_session'.
        session_id = None
        
        # Read JSON from stdin
        if not sys.stdin.isatty():
            try:
                stdin_data = sys.stdin.read()
                if stdin_data.strip():
                    payload = json.loads(stdin_data)
                    session_id = find_session_id(payload)
            except Exception as e:
                print(f"Warning: Failed to parse PreToolUse stdin JSON: {e}", file=sys.stderr)

        if not session_id:
            # Fallback to process ID if session_id is genuinely missing in the payload, 
            # because the hook process is a child of the Claude Code process handling this session.
            # This avoids the catastrophic silent failure of a shared 'default_session' latch.
            # Using os.getppid() (Parent Process ID) is a reliable per-session proxy in this environment.
            session_id = f"ppid_{os.getppid()}"
            print("Warning: session_id not found in stdin payload. Using PPID as fallback.", file=sys.stderr)

        latch_file = os.path.join(memory_dir, f'.snapshot_latch_{session_id}')
        
        if os.path.exists(latch_file):
            # Already snapshotted in this session
            return 0
            
        # AI-2: Snapshot
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        snapshot_name = f'memory.jsonl.{timestamp}.bak'
        snapshot_path = os.path.join(memory_dir, snapshot_name)
        
        # HC-1: Copy file (snapshot)
        shutil.copy2(memory_file, snapshot_path)
        
        # Touch latch file
        with open(latch_file, 'w') as f:
            f.write(timestamp)
            
        # AI-4: Snapshot rotation (keep max 5)
        bak_files = []
        for f in os.listdir(memory_dir):
            if f.startswith('memory.jsonl.') and f.endswith('.bak'):
                bak_files.append(os.path.join(memory_dir, f))
                
        if len(bak_files) > 5:
            # Sort by mtime ascending (oldest first)
            bak_files.sort(key=lambda x: os.path.getmtime(x))
            # Delete oldest until only 5 remain
            while len(bak_files) > 5:
                oldest = bak_files.pop(0)
                os.remove(oldest)
                
    except Exception as e:
        # AI-5: Fail-open (print warning to stderr, but exit 0)
        print(f"Warning: Cueshot-memory snapshot failed: {e}", file=sys.stderr)
        
    return 0

if __name__ == '__main__':
    sys.exit(main())
```

---
**End of CUE-SNAP-01-FIX-A**
