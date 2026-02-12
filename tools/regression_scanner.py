#!/usr/bin/env python3
"""
Regression Scanner for SmartCameraApp1

This tool automatically scans the codebase for known anti-patterns and regression issues.
It helps prevent re-introducing bugs that were previously fixed.

Usage:
    python tools/regression_scanner.py [options]

Options:
    --path PATH         Path to scan (default: current directory)
    --format FORMAT     Output format: text, json, markdown (default: text)
    --output FILE       Output file (default: stdout)
    --verbose           Show detailed information
    --ci                CI mode (exit with non-zero code if issues found)

Examples:
    # Scan current directory
    python tools/regression_scanner.py

    # Scan specific directory with JSON output
    python tools/regression_scanner.py --path app/src --format json --output report.json

    # CI mode (for GitHub Actions)
    python tools/regression_scanner.py --ci

Author: Claude Sonnet 4.5
Version: 1.0.0 (2026-02-10)
"""

import os
import re
import sys
import json
import argparse
from pathlib import Path
from typing import List, Dict, Tuple
from dataclasses import dataclass, asdict
from datetime import datetime


@dataclass
class AntiPattern:
    """Represents a known anti-pattern to detect"""
    id: str
    name: str
    description: str
    file_pattern: str  # Regex pattern for file names
    code_pattern: str  # Regex pattern for code content
    severity: str      # "critical", "warning", "info"
    reference: str     # Reference to documentation
    fixed_in: str      # Version where it was originally fixed


@dataclass
class ScanResult:
    """Result of a single anti-pattern detection"""
    anti_pattern_id: str
    file_path: str
    line_number: int
    line_content: str
    severity: str
    message: str


# Define known anti-patterns based on v2.10.2 Regression Prevention
ANTI_PATTERNS = [
    AntiPattern(
        id="AP001",
        name="Snackbar Photo Saved Message",
        description='Snackbar with "照片已儲存" or "Photo saved" message',
        file_pattern=r"CameraScreen\.kt$",
        code_pattern=r'(照片已儲存|Photo saved|showSnackbar.*saved)',
        severity="critical",
        reference="devspec/feature_record_Backlog.md - Photo Capture Feedback",
        fixed_in="v2.9.8"
    ),
    AntiPattern(
        id="AP002",
        name="Photo Info Button",
        description='Photo info button in CameraScreen (non-standard UX)',
        file_pattern=r"CameraScreen\.kt$",
        code_pattern=r'(Icons\.Default\.Info|IconButton.*Info|photo.*info.*button)',
        severity="critical",
        reference="devspec/feature_record_Backlog.md - Photo Capture Feedback",
        fixed_in="v2.10.2"
    ),
    AntiPattern(
        id="AP003",
        name="AspectRatioMode .first() Usage",
        description='Using .first() instead of .collect() for aspectRatioMode monitoring',
        file_pattern=r"CameraViewModel\.kt$",
        code_pattern=r'aspectRatioMode\s*\.\s*first\(\)',
        severity="critical",
        reference="devspec/feature_record_Backlog.md - Aspect Ratio Settings",
        fixed_in="v2.10.2"
    ),
    AntiPattern(
        id="AP004",
        name="PhotoInfoBottomSheet Auto-Display",
        description='Automatically showing PhotoInfoBottomSheet after photo capture',
        file_pattern=r"CameraScreen\.kt$",
        code_pattern=r'(showPhotoInfoSheet\s*=\s*true|PhotoInfoBottomSheet.*visible)',
        severity="warning",
        reference="devspec/feature_record_Backlog.md - Photo Capture Feedback",
        fixed_in="v2.10.2"
    ),
    AntiPattern(
        id="AP005",
        name="Gradual Permission Request",
        description='Context-aware permission requests instead of upfront request',
        file_pattern=r"MainActivity\.kt$",
        code_pattern=r'(rememberPermissionState\((?!.*rememberMultiplePermissionsState))',
        severity="warning",
        reference="devspec/feature_record_Backlog.md - Permission UX",
        fixed_in="v2.10.2"
    ),
    AntiPattern(
        id="AP006",
        name="Optical Flow Fade Guard",
        description='Incorrect fade guard preventing fade during optical flow tracking',
        file_pattern=r"CameraViewModel\.kt$",
        code_pattern=r'&&\s*!trackedPointActive.*alpha',
        severity="critical",
        reference="C:/Users/Administrator/.claude/projects/.../memory/MEMORY.md - Optical Flow Fade-out",
        fixed_in="v2.9.9 Phase 9B-fix"
    ),
]


class RegressionScanner:
    """Main scanner class"""

    def __init__(self, root_path: Path, verbose: bool = False):
        self.root_path = root_path
        self.verbose = verbose
        self.results: List[ScanResult] = []

    def scan(self) -> List[ScanResult]:
        """Scan the codebase for anti-patterns"""
        print(f"🔍 Scanning {self.root_path} for regression issues...")
        print("")

        kotlin_files = self._find_kotlin_files()

        if self.verbose:
            print(f"Found {len(kotlin_files)} Kotlin files")
            print("")

        for file_path in kotlin_files:
            self._scan_file(file_path)

        return self.results

    def _find_kotlin_files(self) -> List[Path]:
        """Find all Kotlin files in the project"""
        kotlin_files = []

        # Scan app/src directory
        app_src = self.root_path / "app" / "src"
        if app_src.exists():
            kotlin_files.extend(app_src.rglob("*.kt"))

        return kotlin_files

    def _scan_file(self, file_path: Path):
        """Scan a single file for anti-patterns"""
        rel_path = file_path.relative_to(self.root_path)

        # Check if this file matches any anti-pattern file pattern
        matching_patterns = [
            ap for ap in ANTI_PATTERNS
            if re.search(ap.file_pattern, str(rel_path))
        ]

        if not matching_patterns:
            return

        if self.verbose:
            print(f"  Checking: {rel_path}")

        # Read file content
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
        except Exception as e:
            print(f"  ⚠️  Error reading {rel_path}: {e}")
            return

        # Check each line for matching patterns
        for pattern in matching_patterns:
            for line_num, line in enumerate(lines, start=1):
                # Skip comment lines (single-line comments)
                stripped_line = line.strip()
                if stripped_line.startswith('//') or stripped_line.startswith('*'):
                    continue

                # Skip if pattern is found in comment part of the line
                comment_pos = line.find('//')
                if comment_pos != -1:
                    code_part = line[:comment_pos]
                else:
                    code_part = line

                if re.search(pattern.code_pattern, code_part, re.IGNORECASE):
                    result = ScanResult(
                        anti_pattern_id=pattern.id,
                        file_path=str(rel_path),
                        line_number=line_num,
                        line_content=line.strip(),
                        severity=pattern.severity,
                        message=f"{pattern.name}: {pattern.description}\n"
                                f"    Fixed in: {pattern.fixed_in}\n"
                                f"    Reference: {pattern.reference}"
                    )
                    self.results.append(result)

    def generate_report(self, format: str = "text") -> str:
        """Generate scan report in specified format"""
        if format == "json":
            return self._generate_json_report()
        elif format == "markdown":
            return self._generate_markdown_report()
        else:
            return self._generate_text_report()

    def _generate_text_report(self) -> str:
        """Generate text report"""
        lines = []
        lines.append("=" * 80)
        lines.append("REGRESSION SCANNER REPORT")
        lines.append(f"Scan Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        lines.append(f"Root Path: {self.root_path}")
        lines.append("=" * 80)
        lines.append("")

        if not self.results:
            lines.append("✅ No regression issues found!")
            lines.append("")
            return "\n".join(lines)

        # Group by severity
        critical = [r for r in self.results if r.severity == "critical"]
        warnings = [r for r in self.results if r.severity == "warning"]
        infos = [r for r in self.results if r.severity == "info"]

        lines.append(f"🔴 Critical Issues: {len(critical)}")
        lines.append(f"⚠️  Warnings: {len(warnings)}")
        lines.append(f"ℹ️  Info: {len(infos)}")
        lines.append("")

        # Report critical issues
        if critical:
            lines.append("-" * 80)
            lines.append("🔴 CRITICAL ISSUES")
            lines.append("-" * 80)
            for result in critical:
                lines.append("")
                lines.append(f"[{result.anti_pattern_id}] {result.file_path}:{result.line_number}")
                lines.append(f"    {result.message}")
                lines.append(f"    Code: {result.line_content}")
                lines.append("")

        # Report warnings
        if warnings:
            lines.append("-" * 80)
            lines.append("⚠️  WARNINGS")
            lines.append("-" * 80)
            for result in warnings:
                lines.append("")
                lines.append(f"[{result.anti_pattern_id}] {result.file_path}:{result.line_number}")
                lines.append(f"    {result.message}")
                lines.append(f"    Code: {result.line_content}")
                lines.append("")

        lines.append("=" * 80)
        lines.append(f"Total Issues: {len(self.results)}")
        lines.append("=" * 80)
        lines.append("")

        return "\n".join(lines)

    def _generate_json_report(self) -> str:
        """Generate JSON report"""
        report = {
            "scan_date": datetime.now().isoformat(),
            "root_path": str(self.root_path),
            "total_issues": len(self.results),
            "issues_by_severity": {
                "critical": len([r for r in self.results if r.severity == "critical"]),
                "warning": len([r for r in self.results if r.severity == "warning"]),
                "info": len([r for r in self.results if r.severity == "info"]),
            },
            "results": [asdict(r) for r in self.results]
        }
        return json.dumps(report, indent=2)

    def _generate_markdown_report(self) -> str:
        """Generate Markdown report"""
        lines = []
        lines.append("# Regression Scanner Report")
        lines.append("")
        lines.append(f"**Scan Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        lines.append(f"**Root Path**: `{self.root_path}`")
        lines.append("")

        if not self.results:
            lines.append("✅ **No regression issues found!**")
            lines.append("")
            return "\n".join(lines)

        # Summary
        critical = [r for r in self.results if r.severity == "critical"]
        warnings = [r for r in self.results if r.severity == "warning"]
        infos = [r for r in self.results if r.severity == "info"]

        lines.append("## Summary")
        lines.append("")
        lines.append(f"- 🔴 **Critical Issues**: {len(critical)}")
        lines.append(f"- ⚠️ **Warnings**: {len(warnings)}")
        lines.append(f"- ℹ️ **Info**: {len(infos)}")
        lines.append("")

        # Critical issues
        if critical:
            lines.append("## 🔴 Critical Issues")
            lines.append("")
            for result in critical:
                lines.append(f"### [{result.anti_pattern_id}] {result.file_path}:{result.line_number}")
                lines.append("")
                lines.append(f"**Message**: {result.message.split(chr(10))[0]}")
                lines.append("")
                lines.append("```kotlin")
                lines.append(result.line_content)
                lines.append("```")
                lines.append("")

        # Warnings
        if warnings:
            lines.append("## ⚠️ Warnings")
            lines.append("")
            for result in warnings:
                lines.append(f"### [{result.anti_pattern_id}] {result.file_path}:{result.line_number}")
                lines.append("")
                lines.append(f"**Message**: {result.message.split(chr(10))[0]}")
                lines.append("")
                lines.append("```kotlin")
                lines.append(result.line_content)
                lines.append("```")
                lines.append("")

        lines.append("---")
        lines.append(f"**Total Issues**: {len(self.results)}")
        lines.append("")

        return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(
        description="Scan SmartCameraApp1 codebase for known regression issues"
    )
    parser.add_argument(
        "--path",
        type=str,
        default=".",
        help="Path to scan (default: current directory)"
    )
    parser.add_argument(
        "--format",
        type=str,
        choices=["text", "json", "markdown"],
        default="text",
        help="Output format (default: text)"
    )
    parser.add_argument(
        "--output",
        type=str,
        help="Output file (default: stdout)"
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Show detailed information"
    )
    parser.add_argument(
        "--ci",
        action="store_true",
        help="CI mode (exit with non-zero code if issues found)"
    )

    args = parser.parse_args()

    # Resolve path
    root_path = Path(args.path).resolve()
    if not root_path.exists():
        print(f"❌ Error: Path does not exist: {root_path}")
        sys.exit(1)

    # Run scanner
    scanner = RegressionScanner(root_path, verbose=args.verbose)
    results = scanner.scan()

    # Generate report
    report = scanner.generate_report(format=args.format)

    # Output report
    if args.output:
        output_path = Path(args.output)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(report)
        print(f"📝 Report saved to: {output_path}")
    else:
        print(report)

    # CI mode exit code
    if args.ci:
        critical_count = len([r for r in results if r.severity == "critical"])
        if critical_count > 0:
            print(f"❌ CI Mode: Found {critical_count} critical issues")
            sys.exit(1)
        else:
            print("✅ CI Mode: No critical issues found")
            sys.exit(0)


if __name__ == "__main__":
    main()
