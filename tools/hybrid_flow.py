#!/usr/bin/env python3
"""
Hybrid workflow MVP generator

⚠️  CRITICAL FILE - DO NOT DELETE ⚠️
This file is integrated with Claude Code workflow and VSCode Tasks.
Deletion will break the development planning process.

Generates:
- Level 1 plan (ChatGPT-side planning summary)
- Level 2 feasibility memo (Claude Code-side feasibility input)
- PR notes (verification + risk/rollback draft)

Output directory convention:
  --version vX.Y.Z  →  docs/features/vX.Y.Z/{vX.Y.Z}_{topic}_{level}.md  (preferred)
  --out <dir>       →  <dir>/{topic}_{level}.md                            (legacy fallback)
  (auto-detect)     →  reads versionName from app/build.gradle.kts

Usage examples:
  python tools/hybrid_flow.py --level L1 --topic "camera_static_mode" --version v2.11.3
  python tools/hybrid_flow.py --level L2 --topic "camera_static_mode" --version v2.11.3
  python tools/hybrid_flow.py --level PR --topic "camera_static_mode" --version v2.11.3
  python tools/hybrid_flow.py --level L1 --topic "camera_control_not_available" --input "實體機切換 Live 後操作 zoom 出現錯誤"
  python tools/hybrid_flow.py --level L2 --topic "camera_control_not_available" --input_file logs/logcat.txt
  adb logcat -d | python tools/hybrid_flow.py --level L1 --topic "camera_control_not_available" --stdin

Environment:
  OPENAI_API_KEY  (required)
Optional:
  OPENAI_MODEL    (default: gpt-5.2)
"""
from __future__ import annotations

import argparse
import os
import subprocess
from pathlib import Path
from datetime import datetime
from typing import List, Optional

def _run(cmd: List[str], cwd: Optional[Path] = None) -> str:
    try:
        p = subprocess.run(cmd, cwd=str(cwd) if cwd else None, capture_output=True, text=True, check=False)
        out = (p.stdout or "") + ("\n" + p.stderr if p.stderr else "")
        return out.strip()
    except Exception as e:
        return f"[command failed] {cmd}: {e}"

def _read_if_exists(path: Path, max_chars: int = 60_000) -> str:
    if not path.exists():
        return ""
    try:
        txt = path.read_text(encoding="utf-8", errors="replace")
        if len(txt) > max_chars:
            return txt[:max_chars] + "\n\n[...truncated...]"
        return txt
    except Exception as e:
        return f"[failed to read {path}] {e}"

def _slugify(s: str) -> str:
    s = s.strip()
    s = "".join(ch if ch.isalnum() or ch in "-_." else "-" for ch in s)
    s = "-".join(filter(None, s.split("-")))
    return s[:80] if s else "task"

def _detect_version(repo: Path) -> str:
    """Auto-detect versionName from app/build.gradle.kts.
    Returns 'vX.Y.Z' string, or empty string if not found.
    """
    gradle_path = repo / "app" / "build.gradle.kts"
    if not gradle_path.exists():
        return ""
    try:
        import re
        text = gradle_path.read_text(encoding="utf-8", errors="replace")
        m = re.search(r'versionName\s*=\s*"([^"]+)"', text)
        if m:
            ver = m.group(1).strip()
            return f"v{ver}" if not ver.startswith("v") else ver
    except Exception:
        pass
    return ""

def _compose_prompt(level: str, topic: str, issue_text: str, additional_text: str, context_blocks: List[str], git_block: str) -> str:
    header = "你是資深 Android / CameraX / AI 即時影像專案的技術 PM 與工程顧問。請用繁體中文產出以下文件。\n\n"

    common_context = ""
    if issue_text.strip():
        common_context += "【Issue / 任務描述】\n" + issue_text.strip() + "\n\n"
    if additional_text.strip():
        common_context += "【使用者補充描述 / 即時觀察】\n" + additional_text.strip() + "\n\n"
    if git_block.strip():
        common_context += "【Git 現況（供參考）】\n" + git_block.strip() + "\n\n"
    for b in context_blocks:
        if b.strip():
            common_context += b.strip() + "\n\n"

    if level == "L1":
        instruction = f"""【輸出文件類型：Level 1 規劃摘要】
主題：{topic}

請嚴格依下列章節輸出：
1. 目標（Goal）
2. 背景與問題（Why / What）
3. 影響範圍（Scope：模組/檔案/功能層級；若未知請以推測標註）
4. 風險清單（Risks：含相機管線/模式切換/效能回歸）
5. 建議方案（Plan A / Plan B：最小差異優先）
6. 驗證方式（How to verify：模擬器/實機/效能指標）
7. 回滾策略（Rollback）
8. 是否需要 Level 2 可行性檢視（Yes/No + 理由）

要求：可直接貼到 Git Issue / docs/plan 的格式。
"""
    elif level == "L2":
        instruction = f"""【輸出文件類型：Level 2 可行性檢視備忘錄（給 Claude Code 檢視 repo 用）】
主題：{topic}

請嚴格依下列章節輸出：
1. 可行性結論（可行 / 不可行 / 需調整）
2. 關鍵假設（Assumptions：哪些需由 repo 或 log 來驗證）
3. 高風險點（High-risk areas：CameraX Preview/ImageAnalysis/模式切換/Thread/權限）
4. 最小差異建議（Minimal diff approach：步驟化）
5. 需要檢視的檔案/關鍵函式清單（請用待辦清單列出）
6. 驗證清單（Build/Test/Device/Perf）
7. 回滾與降風險策略（Rollback/Feature flag）

要求：請以「可交付給 Claude Code 直接檢視與落地」的風格撰寫。
"""
    else:  # PR
        instruction = f"""【輸出文件類型：PR Notes（草稿，用於 pull_request_template.md）】
主題：{topic}

請輸出三段：
A) What changed（條列）
B) How verified（條列：含指令、裝置、觀察結果；若缺資訊請列出需補齊項）
C) Risk & rollback（風險與回滾：條列）

要求：內容可直接貼到 PR Template 對應欄位。
"""
    return header + common_context + instruction

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--level", required=True, choices=["L1", "L2", "PR"], help="Which artifact to generate")
    ap.add_argument("--topic", required=True, help="Short topic name, used for filename")
    ap.add_argument("--version", default="", help="Version tag (e.g. v2.11.3). Output goes to docs/features/{version}/. Auto-detected from build.gradle.kts if omitted.")
    ap.add_argument("--out", default="docs/plan", help="Output directory (legacy fallback, used only when --version is absent and auto-detect fails)")
    ap.add_argument("--issue", default="", help="Optional path to Issue markdown, or raw text")
    ap.add_argument("--input", default="", help="Optional inline description (requirements / problem statement)")
    ap.add_argument("--input_file", default="", help="Optional path to a text/markdown/log file to include as additional context")
    ap.add_argument("--stdin", action="store_true", help="Read additional context from STDIN (e.g., piped logcat)")
    ap.add_argument("--repo", default=".", help="Repository root path")
    ap.add_argument("--model", default=os.environ.get("OPENAI_MODEL", "gpt-5.2"))
    ap.add_argument("--no_git", action="store_true", help="Disable git context collection")
    args = ap.parse_args()

    repo = Path(args.repo).resolve()

    # Resolve version: explicit arg > auto-detect from build.gradle.kts > fallback to --out
    version = args.version.strip()
    if not version:
        version = _detect_version(repo)

    if version:
        # New convention: docs/features/{version}/{version}_{slug}_{level}.md
        out_dir = (repo / "docs" / "features" / version).resolve()
    else:
        # Legacy fallback: --out directory
        out_dir = (repo / args.out).resolve() if not Path(args.out).is_absolute() else Path(args.out).resolve()

    out_dir.mkdir(parents=True, exist_ok=True)

    # Issue text (file or inline)
    issue_text = ""
    if args.issue:
        # Normalize path separators (handle both Windows \ and Unix /)
        issue_path_str = args.issue.replace("\\", "/")
        ip = Path(issue_path_str)
        if ip.exists():
            issue_text = _read_if_exists(ip)
        else:
            # Try relative to repo root
            ip_rel = repo / issue_path_str
            if ip_rel.exists():
                issue_text = _read_if_exists(ip_rel)
            else:
                issue_text = args.issue

    # Additional input text (inline/file/stdin). Priority: --input > --input_file > --stdin
    additional_text = ""
    if args.input:
        additional_text = args.input
    elif args.input_file:
        # Normalize path separators (handle both Windows \ and Unix /)
        input_file_str = args.input_file.replace("\\", "/")
        fp = Path(input_file_str)
        if fp.exists():
            additional_text = _read_if_exists(fp)
        else:
            # Try relative to repo root
            fp_rel = repo / input_file_str
            if fp_rel.exists():
                additional_text = _read_if_exists(fp_rel)
            else:
                additional_text = f"[input_file not found] {args.input_file}"
    elif args.stdin:
        try:
            import sys
            stdin_data = sys.stdin.read()
            additional_text = stdin_data.strip()
        except Exception as e:
            additional_text = f"[failed to read stdin] {e}"
    if additional_text and len(additional_text) > 60_000:
        additional_text = additional_text[:60_000] + "\n\n[...truncated...]"

    # Context blocks: try to include workflow docs if present
    ctx = []
    for p in [
        repo / "docs" / "workflow" / "hybrid_workflow_checklist.md",
        repo / "docs" / "workflow" / "claude_prompts.md",
        repo / "DAILY_DEVELOPMENT_SOP.md",
    ]:
        t = _read_if_exists(p)
        if t.strip():
            ctx.append(f"【參考文件：{p.as_posix()}】\n{t}")

    # Git context
    git_block = ""
    if not args.no_git:
        status = _run(["git", "status", "--porcelain"], cwd=repo)
        diff = _run(["git", "diff", "--stat"], cwd=repo)
        diff_u = _run(["git", "diff", "--unified=3"], cwd=repo)
        if len(diff_u) > 30_000:
            diff_u = diff_u[:30_000] + "\n\n[...diff truncated...]"
        if status or diff:
            git_block = f"git status --porcelain:\n{status}\n\ngit diff --stat:\n{diff}\n\ngit diff (u=3):\n{diff_u}"

    prompt = _compose_prompt(args.level, args.topic, issue_text, additional_text, ctx, git_block)

    api_key = os.environ.get("OPENAI_API_KEY", "").strip()
    if not api_key:
        raise SystemExit("Missing OPENAI_API_KEY. Set it in your environment.")

    # OpenAI Responses API (recommended)
    from openai import OpenAI  # type: ignore
    client = OpenAI()

    resp = client.responses.create(
        model=args.model,
        input=prompt,
    )
    text = getattr(resp, "output_text", "") or ""
    if not text.strip():
        text = str(resp)

    stamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    slug = _slugify(args.topic)
    if version:
        out_name = f"{version}_{slug}_{args.level}.md"
    else:
        out_name = f"{slug}_{args.level}.md"
    out_path = out_dir / out_name

    out_path.write_text(f"<!-- generated: {stamp} -->\n\n{text.strip()}\n", encoding="utf-8")
    print(out_path.as_posix())
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
