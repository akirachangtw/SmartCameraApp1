#!/usr/bin/env python3
"""Safely remove expired content from approved project tmp and .tmp folders."""

from __future__ import annotations

import argparse
import json
import os
import shutil
import sys
from datetime import datetime, timedelta
from pathlib import Path
from typing import Iterable


RETENTION_DAYS = 7
SCRIPT_ROOT = Path(__file__).resolve().parent.parent
APP_ROOT = SCRIPT_ROOT
MCP_ROOT = Path(r"D:\DevSources\workflow\mcp-ticket-platform")
TARGETS = {
    "smartcamera": APP_ROOT / "tmp",
    "smartcamera-.tmp": APP_ROOT / ".tmp",
    "mcp-ticket-platform": MCP_ROOT / "tmp",
    "mcp-ticket-platform-.tmp": MCP_ROOT / ".tmp",
}


def is_older_than(path: Path, cutoff: datetime) -> bool:
    return datetime.fromtimestamp(path.stat(follow_symlinks=False).st_mtime) < cutoff


def has_symlink_below(directory: Path) -> bool:
    """Return true without ever descending through a symbolic-link directory."""
    for current, directories, files in os.walk(directory, followlinks=False):
        for name in [*directories, *files]:
            if (Path(current) / name).is_symlink():
                return True
    return False


def audit_record(target: str, phase: str, dry_run: bool, removed_files: int, removed_dirs: int,
                 skipped_links: int, failures: int) -> bool:
    """Append only aggregate counters; paths and exception messages may contain secrets."""
    audit_path = TARGETS[target] / "cleanup-audit.jsonl"
    record = {
        "timestamp_local": datetime.now().astimezone().isoformat(timespec="seconds"),
        "target": target,
        "phase": phase,
        "dry_run": dry_run,
        "retention_days": RETENTION_DAYS,
        "removed_files": removed_files,
        "removed_directories": removed_dirs,
        "skipped_symbolic_links": skipped_links,
        "failures": failures,
    }
    try:
        with audit_path.open("a", encoding="utf-8") as stream:
            stream.write(json.dumps(record, separators=(",", ":")) + "\n")
        return True
    except OSError:
        # Fail closed: no cleanup occurs when its aggregate audit trail cannot be written.
        print(f"{target}: audit write failed", file=sys.stderr)
        return False


def clean_directory(directory: Path, cutoff: datetime, dry_run: bool,
                    counters: dict[str, int], is_root: bool = False) -> None:
    """Process old files individually and old directories as complete, verified trees."""
    try:
        entries: Iterable[os.DirEntry[str]] = list(os.scandir(directory))
    except OSError:
        counters["failures"] += 1
        return

    for entry in entries:
        path = Path(entry.path)
        try:
            if entry.is_symlink():
                counters["skipped_links"] += 1
                continue
            if entry.is_dir(follow_symlinks=False):
                if is_older_than(path, cutoff):
                    # rmtree does not follow links, but reject a tree containing one as a
                    # stronger boundary: no entry in it is ever acted on by this tool.
                    if has_symlink_below(path):
                        counters["skipped_links"] += 1
                    elif dry_run:
                        counters["removed_dirs"] += 1
                    else:
                        shutil.rmtree(path)
                        counters["removed_dirs"] += 1
                else:
                    clean_directory(path, cutoff, dry_run, counters)
            elif entry.is_file(follow_symlinks=False) and is_older_than(path, cutoff):
                if dry_run:
                    counters["removed_files"] += 1
                else:
                    path.unlink()
                    counters["removed_files"] += 1
        except OSError:
            counters["failures"] += 1


def clean_target(target: str, dry_run: bool) -> int:
    configured = TARGETS[target]
    try:
        root = configured.resolve(strict=True)
        project_root = configured.parent.resolve(strict=True)
    except OSError:
        print(f"{target}: tmp folder unavailable", file=sys.stderr)
        return 1

    # A changed constant, junction, or symlink must fail closed before any scan.
    if root != configured or root.parent != project_root or root.is_symlink():
        print(f"{target}: unsafe tmp folder boundary", file=sys.stderr)
        return 1

    cutoff = datetime.now().astimezone().replace(tzinfo=None) - timedelta(days=RETENTION_DAYS)
    counters = {"removed_files": 0, "removed_dirs": 0, "skipped_links": 0, "failures": 0}
    if not audit_record(target, "started", dry_run, **counters):
        return 1
    clean_directory(root, cutoff, dry_run, counters, is_root=True)
    audit_completed = audit_record(target, "completed", dry_run, **counters)
    action = "would remove" if dry_run else "removed"
    print(
        f"{target}: {action} {counters['removed_files']} files and "
        f"{counters['removed_dirs']} directories; skipped {counters['skipped_links']} links; "
        f"failures {counters['failures']}"
    )
    return 1 if counters["failures"] or not audit_completed else 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--target", choices=[*TARGETS, "all"], default="all")
    parser.add_argument("--dry-run", action="store_true", help="Report eligible items without deleting them.")
    args = parser.parse_args()
    targets = TARGETS if args.target == "all" else {args.target: TARGETS[args.target]}
    return max(clean_target(target, args.dry_run) for target in targets)


if __name__ == "__main__":
    raise SystemExit(main())
