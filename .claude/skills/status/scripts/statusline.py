#!/usr/bin/env python3
"""statusLine renderer + /status cache writer (single source).

Wired at .claude/settings.json -> statusLine.command. Claude Code pipes the
session payload on stdin; stdout is rendered as the status line.

Two responsibilities are deliberately merged here (INFRA-SKILLS-STATUSLINE-01):
  1. Render the status line.
  2. Persist the raw payload to tmp/claude_status_cache.json -- the ONLY
     real-data source for the /status skill. Splitting these across two
     scripts silently degrades /status to estimation mode.

Context is reported as an ABSOLUTE token count, never as a percentage or bar:
context_window_size is not a trustworthy denominator (Opus 4.8 reports 1M while
the practical wall is 200K), so a percentage would read green while at the wall.
This matches the /status skill's "absolute only" contract.

Rate limits come from the API, not the context window, so their percentages are
trustworthy and ARE colored. They are shown as REMAINING budget (100 - used),
matching the /status skill's "5h Rate Limit 剩餘" output contract.
"""
import json
import os
import sys

RESET = "\033[0m"
DIM = "\033[2m"
CYAN = "\033[36m"
GREEN = "\033[32m"
YELLOW = "\033[33m"
RED = "\033[31m"
MAGENTA = "\033[35m"

CACHE_REL_PATH = os.path.join("tmp", "claude_status_cache.json")


def get(data, path, default=None):
    cur = data
    for key in path.split("."):
        if not isinstance(cur, dict) or key not in cur:
            return default
        cur = cur[key]
    return cur if cur is not None else default


def safe_int(value, default=0):
    try:
        return int(float(value))
    except (TypeError, ValueError):
        return default


def format_duration(ms):
    total_sec = safe_int(ms) // 1000
    mins = total_sec // 60
    hours = mins // 60
    mins = mins % 60
    if hours > 0:
        return f"{hours}h {mins}m"
    return f"{mins}m"


def format_tokens(count):
    count = safe_int(count)
    if count >= 1000:
        return f"{count / 1000:.1f}K"
    return str(count)


def color_by_remaining(percent):
    """Green >=50% left, yellow 21-49%, red <=20%."""
    if percent <= 20:
        return RED
    if percent <= 49:
        return YELLOW
    return GREEN


def git_branch(path):
    """Read .git/HEAD directly -- the statusLine refreshes often and spawning a
    git subprocess costs ~50-100ms per render on Windows."""
    if not path:
        return None
    try:
        git_path = os.path.join(path, ".git")
        if os.path.isfile(git_path):  # linked worktree: "gitdir: <path>"
            with open(git_path, encoding="utf-8") as f:
                git_path = f.read().split(":", 1)[1].strip()
        head_path = os.path.join(git_path, "HEAD")
        with open(head_path, encoding="utf-8") as f:
            head = f.read().strip()
        if head.startswith("ref:"):
            return head.split("refs/heads/", 1)[-1]
        return head[:7] or None  # detached HEAD
    except (OSError, IndexError, ValueError):
        return None


def write_cache(raw, data, project_dir):
    """Persist the raw payload for the /status skill. Only payloads carrying
    context_window are cached, so a leaner caller can never blank out rich data."""
    if not get(data, "context_window"):
        return
    try:
        cache_path = os.path.join(project_dir or os.getcwd(), CACHE_REL_PATH)
        os.makedirs(os.path.dirname(cache_path), exist_ok=True)
        tmp_path = cache_path + ".tmp"
        with open(tmp_path, "w", encoding="utf-8") as f:
            f.write(raw)
        os.replace(tmp_path, cache_path)  # atomic: /status never reads a partial file
    except OSError:
        pass  # a cache miss degrades /status to estimation; it must not blank the status line


def render(data):
    model = get(data, "model.display_name", "?")
    current_dir = get(data, "workspace.current_dir", get(data, "cwd", ""))
    repo_name = os.path.basename(current_dir) if current_dir else "unknown"
    branch = git_branch(current_dir)
    session_name = get(data, "session_name")
    agent_name = get(data, "agent.name")
    pr_number = get(data, "pr.number")
    pr_state = get(data, "pr.review_state")

    line1 = [f"{CYAN}[{model}]{RESET}", f"{MAGENTA}{repo_name}{RESET}"]
    if branch:
        line1.append(f"🌿 {branch}")
    if session_name:
        line1.append(f"S:{session_name}")
    if agent_name:
        line1.append(f"A:{agent_name}")
    if pr_number:
        line1.append(f"PR#{pr_number}({pr_state})" if pr_state else f"PR#{pr_number}")

    used_tokens = get(data, "context_window.total_input_tokens")
    cost = float(get(data, "cost.total_cost_usd", 0) or 0)

    line2 = []
    if used_tokens is not None:
        line2.append(f"ctx {format_tokens(used_tokens)}")
    line2.append(f"${cost:.2f}")
    line2.append(format_duration(get(data, "cost.total_duration_ms")))

    for label, path in (("5h", "rate_limits.five_hour.used_percentage"),
                        ("7d", "rate_limits.seven_day.used_percentage")):
        used = get(data, path)
        if used is None:
            continue
        left = 100 - safe_int(used)
        line2.append(f"{color_by_remaining(left)}{label} {left}%{RESET}")

    sep = f"{DIM} | {RESET}"
    return sep.join(line1) + "\n" + sep.join(line2)


def main():
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    raw = sys.stdin.read()
    try:
        data = json.loads(raw)
    except ValueError:
        return  # a blank status line beats a traceback in the bar
    write_cache(raw, data, get(data, "workspace.project_dir"))
    print(render(data))


if __name__ == "__main__":
    main()
