# CUE-SNAP-01-FIX-B: 封閉 CUESHOT_MEMORY_DIR 相對路徑 side door（absolute-only 契約：isabs＋driveless 補判守門 + fail-safe 回退）

**簽發日期**: 2026-08-27
**簽發者**: Claude
**來源議題**: Cueshot
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: execute_review
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（§4.d 凍結已於 2026-08-27 依外審回流＋存廢三案裁決落檔解除，非票間依賴）
- `evidence_dependency`: CUE-SNAP-01-FIX-A（判準/錨點來源）＋ Traycer T2 2026-08-27 首次功能面審查（T0 finding）＋ 決策檔 `Cueshot_Snapshot_Clause4_Disposition_Continue_Repair`（KD-2 absolute-only 契約、KD-5 driveless 補判強制）
- `coordination_dependency`: CUE-SNAP-01-FIX-A（本票通過後 FIX-A 重跑 execute_review，同批收束）

## 檔案治理/來源說明
- T0 來源：Traycer T2 判定 AI-2 未閉環——env override 直回 raw string，CWD 耦合以 side door 形式殘留
- 修正路線：外審（`Smarter-Project/deliver/CUESHOT_MEMORY_DIR_CWD-Perplexity.md`）確認不採「abspath/Path.resolve 轉換」（其基準即 CWD），採 isabs 守門＋拒用回退；並依 KD-5 追加 rooted-but-driveless 補判（Python < 3.13 之 Windows `ntpath.isabs('\\memory')` 回 True，沙盒 3.12.3 實測取證，僅 isabs 會漏接）
- 契約定義（KD-2）：`CUESHOT_MEMORY_DIR` 僅接受 OS-native absolute pathname；相對值、drive-relative（`D:foo`）、rooted-but-driveless（`\foo`）均為無效 override，禁以 CWD 為基準轉換之
- 票號/topic 註記：依票號議題前綴律（v3.6）新票 topic 與前綴 CUE 同源＝Cueshot；FIX-A 冊面 topic=Memory 為前法遺留（不追溯），家族跨 topic 之收斂另案裁決

## Verify Config
TICKET_ID=CUE-SNAP-01-FIX-B
CREATED=2026-08-27
PRESET=bugfix
ALLOWED=.claude/hooks/snapshot_cueshot_memory.py
FORBIDDEN=.claude/settings.json,.mcp-memory/memory.jsonl
MAX_LINES=N/A（安全票 R3：守門類安全不變量，失敗形狀枚舉為審查發現式，不可先驗估算）
DIFF_REF=4ad49b0
ANCHOR_PROBED=2026-08-27 23:33
BASE_COMMIT=4ad49b0d51b07bb8fcd5adb2b54cce9a88f8a93e
TARGET_BLOB=96ae34a（Claude Code 端有 git hash-object 通道，補上票面已知縫隙；`git show 4ad49b0:.claude/hooks/snapshot_cueshot_memory.py | git hash-object --stdin`）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/hooks/snapshot_cueshot_memory.py`
- **動作**: 逐字確認 AI-1 現值三行於檔內恰 1 次命中（開票時 live grep 實測：L28/L29/L30 各恰 1 次；`isabs` 全檔 0 命中；`import sys` 已在 L1）；不符依基線漂移三分流停止分類回報。
- [x] 完成

### AI-1: env override 加 absolute-only 守門（isabs ＋ Windows driveless 補判）
- **檔案**: `.claude/hooks/snapshot_cueshot_memory.py`
- **現值**（各恰 1 次，L28–L30）:
  `env_dir = os.environ.get('CUESHOT_MEMORY_DIR')`
  `    if env_dir:`
  `        return env_dir`
- **目標值**:
  `env_dir = os.environ.get('CUESHOT_MEMORY_DIR')`
  `    if env_dir:`
  `        if os.path.isabs(env_dir) and (os.name != 'nt' or os.path.splitdrive(env_dir)[0]):`
  `            return env_dir`
  `        print(f"Warning: CUESHOT_MEMORY_DIR must be an OS-native absolute path (drive/UNC-rooted on Windows); ignoring override: {env_dir}", file=sys.stderr)`
  （拒用後 fall through 至既有 `__file__` 推導之預設絕對路徑分支，L31 起，不改動）
- **設計依據**: `splitdrive` 補判封閉 Python < 3.13 之 `\rooted-but-driveless` 漏接（該版本 `ntpath.isabs` 回 True）；`os.name != 'nt'` 短路使補判僅於 Windows 生效，避免 POSIX 上 splitdrive 恆空致全拒
- [x] 完成

## Hard Constraints
1. 禁以 `os.path.abspath()` / `Path.resolve()` / 任何以 CWD 為基準之轉換對相對 env 值「絕對化」——該轉換本身即 CWD 耦合，係本票要消滅之根因換皮（外審引 Python 官方文檔獨立佐證：`abspath(p)` ≡ `normpath(join(getcwd(), p))`）
2. 禁以自寫字串前綴規則替代 `ntpath` 語義（isabs/splitdrive 之外不得另造路徑判定）
3. 禁觸碰 default 推導、latch、fallback 標記、輪替邏輯（單點修補；其餘行為以 FIX-A 既有 AC 為凍結基線）
4. fail-open 總則維持：本守門僅拒 override，不得使 hook 以非零 exit 阻斷工具呼叫

## Error Handling
- AI-1 現值錨點命中數 ≠ 1 → 停止回報（依基線漂移三分流分類：先 `git diff --name-only 4ad49b0..HEAD -- .claude/hooks/snapshot_cueshot_memory.py` 判範圍相交，再依 commit 前綴歸因）
- **[標配]** `git check-ignore -v .claude/hooks/snapshot_cueshot_memory.py` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** 腳本內無守門之 raw 回傳（`^\s*return env_dir$` 於 isabs 判定外）0 命中；`os.path.isabs` 命中 1 次；`splitdrive` 命中 1 次；`abspath(env_dir)`／`resolve(` 0 命中 — 實測：grep `^\s*return env_dir$` 恰 1 次且位於 isabs 判定內（12 空格縮排，非裸回傳）；`os.path.isabs` 1 次；`splitdrive` 1 次；`abspath(env_dir)`/`resolve(` 0 次
- [x] **[行為性]** given `CUESHOT_MEMORY_DIR=相對值`（如 `relative\memory`），於兩個不同 CWD 各觸發一次，expect 兩次 `resolve_memory_dir()` 定位結果**相同**且均等於既有 `__file__` 推導之預設絕對路徑，stderr 均含拒用警告 — 實測：`tmp/CUE-SNAP-01-FIX-B/cwd1`、`cwd2` 兩個實際不同 CWD 各執行一次，兩次結果均為 `D:\DevSources\AndroidProj\SmartCameraApp1\.mcp-memory`，stderr 均輸出拒用警告
- [x] **[行為性]** given `CUESHOT_MEMORY_DIR=D:memory`（drive-relative），expect 拒用＋警告＋回退預設路徑 — 實測：stderr 警告 + 回退至 `D:\DevSources\AndroidProj\SmartCameraApp1\.mcp-memory`
- [x] **[行為性]** given `CUESHOT_MEMORY_DIR=\memory`（rooted-but-driveless），expect 拒用＋警告＋回退預設路徑；**執行環境 Python 版本 MUST 隨證據回填**（`Python 3.14.5`；≥ 3.13，本機 `ntpath.isabs('\memory')` 已回 `False`，isabs 本身即可攔截，splitdrive 補判為防禦性冗餘，符合票面「仍不得移除」要求，未移除）— 實測：stderr 警告 + 回退至預設路徑
- [x] **[行為性]** given `CUESHOT_MEMORY_DIR=合法絕對路徑`（drive-rooted），expect 快照落於該路徑（override 正常用例不回歸，不得回退）— 實測：`resolve_memory_dir()` 直接回傳該合法絕對路徑，無警告、無回退
- [x] **[可達性]** 經真實 Claude Code session 之 Cueshot 寫類 MCP 呼叫觸發驗證。**producer 證據欄**（回填）：觸發時戳＝`20260827_234938`、`.bak` 檔名＝`memory.jsonl.20260827_234938.bak`（`mcp__cueshot-memory__create_entities` 觸發，session_id=`31def4fb-0e2c-4687-a0f3-cc005cefa60e`，於 AI-1 patch 落地後觸發，證明修補後 pipeline 正常運作；探測實體事後以 `delete_entities` 撤回，latch 生效未再新增 `.bak`）
- [x] `assembleDebug` PASS — bugfix preset，見 /qa Step 1
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 — 僅 `.claude/hooks/snapshot_cueshot_memory.py`
