# Git Commit & Remote Guide

> 本文件彙整本專案所有 Git Commit 規範、自動化機制與 Local/Remote 架構。
> 最後更新：2026-03-20

---

## 目錄

1. [Commit 訊息格式](#1-commit-訊息格式)
2. [原子化提交鐵律](#2-原子化提交鐵律)
3. [Pre-Commit Hook 防線](#3-pre-commit-hook-防線)
4. [手動 Pre-Commit 清單](#4-手動-pre-commit-清單)
5. [Local / Remote 架構](#5-local--remote-架構)
6. [Branch 結構與生命週期](#6-branch-結構與生命週期)
7. [Tags 版本標記](#7-tags-版本標記)
8. [合併前強制流程](#8-合併前強制流程)
9. [常用指令速查](#9-常用指令速查)
10. [輔助工具 & Skills](#10-輔助工具--skills)

---

## 1. Commit 訊息格式

```
[Type] 簡短說明（英文）

詳細變更內容:
- 變更項目 1
- 變更項目 2

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

**Type 清單**：

| Type | 用途 |
|------|------|
| `[Feature]` | 新增功能 |
| `[Fix]` | Bug 修復 |
| `[Refactor]` | 程式碼重構 |
| `[UI]` | UI/UX 調整 |
| `[Perf]` | 效能優化 |
| `[Docs]` | 文件更新 |
| `[Test]` | 測試相關 |
| `[Build]` | 建置相關 |
| `[Merge]` | 分支合併記錄 |

---

## 2. 原子化提交鐵律

> 來源：`.claude/CLAUDE.md` — 鐵律 2

```
✅ Commit 1: [Fix]  程式碼修復（.kt / .java / .xml）
✅ Commit 2: [Docs] 文件歸檔（.md / .log）
❌ 禁止: 兩類檔案混入同一 Commit
```

違反此規則時，**Pre-Commit Hook 會自動阻斷 Commit**（exit 1）。

---

## 3. Pre-Commit Hook 防線

**位置**：`.git/hooks/pre-commit`（執行 `git commit` 時自動觸發）

### 3.1 阻斷型檢查（exit 1，Commit 失敗）

| 檢查項目 | 觸發條件 |
|---------|---------|
| **Scope Drift** | 同一 Commit 同時包含 `.kt/.java/.xml` 與 `.md/.log` |
| **Anti-Pattern 1** | `CameraScreen.kt` 含 `"照片已儲存"` 或 `Photo saved`（v2.9.8 回歸防護）|
| **Anti-Pattern 2** | `CameraScreen.kt` 含 `Icons.Default.Info` 或 photo info button（v2.10.2 UX 規範）|
| **Anti-Pattern 3** | `CameraViewModel.kt` 含 `aspectRatioMode.first()`（應改用 `.collect()`）|

### 3.2 警告型檢查（exit 0，Commit 繼續）

| 檢查項目 | 說明 |
|---------|------|
| TODO / FIXME 偵測 | 違反零偷懶政策的提示 |
| `println()` / `System.out.print` | 應改用 `Log.d/i/e` |
| 高風險檔案修改 | `CameraScreen.kt`, `CameraViewModel.kt`, `LiveCameraSource.kt`, `MainActivity.kt` |
| 硬編碼中文字串 | 應移至 `strings.xml` |

### 3.3 Commit 格式提示

Hook 完成後顯示格式範例提醒，非阻斷。

---

## 4. 手動 Pre-Commit 清單

> 來源：`devspec/pre_commit_checklist.md`

Commit 前人工確認：

- [ ] 查閱 `devspec/feature_record_Backlog.md` — 確認無 ⚠️ Critical Warnings
- [ ] 變數命名 ≥ 3 個字元，有意義
- [ ] 無硬編碼字串（`strings.xml`）/ 顏色（`colors.xml`）
- [ ] 複雜邏輯有適當註解
- [ ] Log 使用正確級別（`Log.d/i/e`，非 `println`）
- [ ] 核心邏輯修改有對應單元測試
- [ ] Commit 訊息格式正確（含 `[Type]` 與 Co-Author）
- [ ] 無 commented-out 殘碼（應直接刪除）

---

## 5. Local / Remote 架構

### 5.1 Remote 設定

`.git/config` 中定義兩個 Remote，**指向同一個 GitHub Repo**：

```
github  → git@github.com:akirachangtw/SmartCameraApp1.git
```

> 歷史原因造成兩個別名並存。**日常操作統一使用 `github`** 作為 remote 前綴（feature branch tracking 均指向 `github`）。

認證方式：**SSH Key**（非 HTTPS），需確認 SSH key 已設定：
```bash
ssh -T git@github.com
```

### 5.2 目前同步狀態（2026-03-20）

| Branch | Local 狀態 | Remote 狀態 |
|--------|-----------|------------|
| `main` | 與 `github/main` 完全同步 | ✅ Up to date |
| `feature/camera-core-v2.12.0` ⭐ 目前 | 領先 Remote **13 commits** | Fast-forwardable（可直接 push）|
| `feature/aspect-ratio-usecasegroup-v2.10.0` | 與 remote 同步 | ✅ Up to date |
| `feature/optical-flow-phase5-6` | 有差異 | Fast-forwardable |
| `feature/camera-core-v2.11.0` | Local only | ⚠️ Remote 已 stale（需 prune）|
| `backup/before-package-rename-v2.5.0` | Local only | ❌ 未推上 remote |

> ⚠️ `feature/camera-core-v2.12.0` 的 `git pull` 無 upstream 設定；`git push` 已設定 fast-forwardable。執行 `git push -u github feature/camera-core-v2.12.0` 可同時推送並設定 pull upstream。

> ⚠️ `github/feature/camera-core-v2.11.0` 已標示 stale，可執行 `git remote prune github` 清除。

---

## 6. Branch 結構與生命週期

```
main（穩定基底）
  │
  ├── feature/camera-core-v2.12.0  ← 目前開發中
  ├── feature/camera-core-v2.11.0  ← 已 merge 至 main（v2.11.4 stable）
  ├── feature/aspect-ratio-usecasegroup-v2.10.0  ← 已 merge
  ├── feature/optical-flow-phase5-6  ← 歷史分支
  └── backup/before-package-rename-v2.5.0  ← 備份用，local only
```

**版本迭代命名規則**：
```
開發迭代:  v2.X.Y-01 → v2.X.Y-02 → ... → v2.X.Y-NN  （commit 標記）
穩定完成:  v2.X.Y                                      （merge 後標記）
下一功能:  v2.X.(Y+1)
```

**完整生命週期流程**：
```
git checkout -b feature/camera-core-v2.X.0
  │
  │  [開發迭代 commits v2.X.Y-01 ... -NN]
  │
  ├── python tools/check_merge_readiness.py   ← 必須 PASS
  ├── git fetch github --tags
  ├── git merge github/feature/<branch>        ← 用 Remote，非 Local
  └── git push github main
```

---

## 7. Tags 版本標記

目前存在的 tags（均為舊版本）：

```
v2.9.0-beta5
v2.6.2
v2.5.3
v2.1.0
v2.0.0
```

> ⚠️ v2.10.0 之後**尚未建立任何 tag**。`MERGE_SOP.md` 雖提及使用 `v<版本>-final` tag，但目前此規範尚未落實。合併時改用 `github/feature/<branch>` 指定遠端分支。

建立 tag 的正確方式：
```bash
git tag v2.12.0-final <commit-hash>
git push github v2.12.0-final
```

---

## 8. 合併前強制流程

> 來源：`devspec/MERGE_SOP_QUICK_REFERENCE.md`

```
Step 1: git fetch github --tags
Step 2: python tools/check_merge_readiness.py   ← 輸出必須為 ✅ READY TO MERGE
Step 3: git merge github/feature/<branch>        ← 用 Remote 分支，非 Local
Step 4: ./gradlew assembleDeviceDebug + 實機驗證
Step 5: 更新 devspec/BRANCH_RELEASES.md → 單獨 [Docs] commit
```

**`check_merge_readiness.py` 檢查項目**：

| 檢查項目 | 路徑 |
|---------|------|
| L1/L2 功能文件 | `docs/features/v<版本>/` |
| 測試報告 | `docs/test_results/v<版本>/HANDOVER_SUMMARY.md` |
| Branch 歷史記錄 | `devspec/BRANCH_RELEASES.md` |
| SSOT Backlog | `devspec/feature_record_Backlog.md` |

**禁止使用 Local Branch 合併**（常見錯誤）：
```bash
❌ git merge feature/camera-core-v2.12.0      # Local 可能過時
✅ git merge github/feature/camera-core-v2.12.0  # 遠端最新版
```

---

## 9. 常用指令速查

### Remote 操作

```bash
# 更新所有 remote 資訊（含 tags）
git fetch github --tags

# 查看 remote 詳細狀態
git remote show github

# 清除 stale remote tracking branches
git remote prune github

# 查看 local vs remote 差異（commit 數）
git rev-list --left-right --count HEAD...github/feature/camera-core-v2.12.0
```

### Push 操作

```bash
# 首次 push 並設定 upstream（含 pull tracking）
git push -u github feature/camera-core-v2.12.0

# 後續 push（upstream 已設定）
git push

# push main
git push github main

# push tag
git push github v2.12.0-final
```

### Branch 操作

```bash
# 查看所有 local + remote 分支
git branch -a

# 建立新 feature branch
git checkout -b feature/camera-core-v2.13.0

# 切換分支
git checkout main
```

### 合併操作

```bash
# 合併前必須執行
git fetch github --tags
python tools/check_merge_readiness.py

# 合併遠端分支（推薦）
git merge github/feature/camera-core-v2.12.0

# 合併 tag（有 -final tag 時）
git merge v2.12.0-final
```

### Commit 操作

```bash
# 加入特定檔案（避免 git add -A 誤加 .env）
git add app/src/main/java/...

# 提交（使用 HEREDOC 確保格式正確）
git commit -m "$(cat <<'EOF'
[Fix] v2.12.0-27: SP-C defense for Sony AF hunting loop

詳細變更內容:
- 加入 in-flight 鎖定防禦

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

---

## 10. 輔助工具 & Skills

### 10.1 `/sv` — Stage Verification（Commit 前必執行）

```
位置: .claude/skills/sv/SKILL.md
觸發: 人工觸發（disable-model-invocation: true）
用途: 殘碼掃描、Anti-Lazy 檢查、UI 互斥驗證
時機: 每個 Phase 完成後，Commit 前
```

### 10.2 `/qa` — QA 自動化

```
位置: .claude/skills/qa/SKILL.md
觸發: 人工或 Claude 自動觸發
用途: ./gradlew assembleDebug + 錯誤分析 + Warning 清理
時機: 程式碼修改後、Commit 前
```

### 10.3 `tools/check_merge_readiness.py`

```
用途: 合併前文件同步狀態驗證
執行: python tools/check_merge_readiness.py
必要: 輸出 ✅ READY TO MERGE 才能合併
```

### 10.4 `tools/hybrid_flow.py`

```
用途: 生成 L1/L2/PR 規劃文件
執行: python tools/hybrid_flow.py --level L1 --topic <topic> --version <version>
輸出: docs/features/v{version}/{version}_{topic}_{level}.md
```

### 10.5 標準執行順序（Phase 完成後）

```
/sv → /qa → python tools/check_merge_readiness.py → git push
```

---

## 相關文件

| 文件 | 用途 |
|------|------|
| `devspec/MERGE_SOP.md` | 完整合併 SOP（Phase 1–5）|
| `devspec/MERGE_SOP_QUICK_REFERENCE.md` | 5 步驟快速流程 |
| `devspec/pre_commit_checklist.md` | Commit 前人工清單 |
| `devspec/BRANCH_RELEASES.md` | Branch 歷史與 stable commit 記錄 |
| `devspec/feature_record_Backlog.md` | SSOT — Critical Warnings |
| `.github/pull_request_template.md` | PR 必填模板 |
| `plans/adb_screencap_guide.md` | ADB 截圖正確方式 |
