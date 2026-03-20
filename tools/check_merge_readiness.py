#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Merge Readiness Check Tool
用途：在執行 git merge 前檢查文檔同步狀態，確保遵循 MERGE_SOP.md
參考：devspec/MERGE_SOP.md, devspec/GIT_WORKFLOW_IMPROVEMENT_FINAL_RECOMMENDATION.md
"""

import argparse
import re
import sys
from pathlib import Path
from typing import List, Tuple, Optional

# ANSI 顏色碼
class Colors:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    BOLD = '\033[1m'
    END = '\033[0m'

# 視覺化符號
PASS = f"{Colors.GREEN}✅{Colors.END}"
WARN = f"{Colors.YELLOW}⚠️{Colors.END}"
FAIL = f"{Colors.RED}❌{Colors.END}"
INFO = f"{Colors.CYAN}ℹ️{Colors.END}"

def print_header(title: str):
    """列印標題區塊"""
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'='*60}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{title:^60}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'='*60}{Colors.END}\n")

def print_section(title: str):
    """列印小節標題"""
    print(f"\n{Colors.BOLD}📋 {title}{Colors.END}")
    print(f"{Colors.CYAN}{'─'*60}{Colors.END}")

def extract_version_from_gradle() -> Optional[str]:
    """
    從 app/build.gradle.kts 讀取版本號

    Returns:
        版本號字串 (如 "2.11.1") 或 None
    """
    gradle_file = Path("app/build.gradle.kts")

    if not gradle_file.exists():
        print(f"{FAIL} 找不到 app/build.gradle.kts")
        return None

    try:
        content = gradle_file.read_text(encoding='utf-8')
        # 匹配 versionName = "X.Y.Z"
        match = re.search(r'versionName\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"', content)
        if match:
            version = match.group(1)
            print(f"{INFO} 偵測到版本: {Colors.BOLD}v{version}{Colors.END}")
            return version
        else:
            print(f"{WARN} 無法解析版本號 (未找到 versionName)")
            return None
    except Exception as e:
        print(f"{FAIL} 讀取 build.gradle.kts 失敗: {e}")
        return None

def check_feature_docs(version: str) -> Tuple[bool, List[str]]:
    """
    檢查 docs/features/<version>/ 是否存在功能文檔

    Returns:
        (是否通過, 警告訊息列表)
    """
    feature_dir = Path(f"docs/features/v{version}")
    warnings = []

    if not feature_dir.exists():
        warnings.append(f"目錄 docs/features/v{version}/ 不存在")
        return False, warnings

    # 檢查是否有 L0/L1/L2/Traycer 文檔（支援 _Lx.md 和 -Lx.md 兩種命名）
    # L0: prompt_generator_gui.pyw 生成的探索文檔 (Step 0 Traycer)
    l0_files = list(feature_dir.glob("*_L0.md")) + list(feature_dir.glob("*-L0.md"))
    l1_files = list(feature_dir.glob("*_L1.md")) + list(feature_dir.glob("*-L1.md"))
    l2_files = list(feature_dir.glob("*_L2.md")) + list(feature_dir.glob("*-L2.md"))
    traycer_files = list(feature_dir.glob("*_Traycer.md")) + list(feature_dir.glob("*-Traycer.md"))

    total_files = len(l0_files) + len(l1_files) + len(l2_files) + len(traycer_files)

    if total_files == 0:
        warnings.append(f"docs/features/v{version}/ 目錄為空 (沒有 L0/L1/L2/Traycer 文檔)")
        return False, warnings

    print(f"{PASS} 功能文檔存在: {total_files} 個檔案")
    if l0_files:
        print(f"   - L0 探索: {len(l0_files)} 個")
    print(f"   - L1 規劃: {len(l1_files)} 個")
    print(f"   - L2 可行性: {len(l2_files)} 個")
    if traycer_files:
        print(f"   - Traycer 實作: {len(traycer_files)} 個")

    return True, []

def check_test_results(version: str) -> Tuple[bool, List[str]]:
    """
    檢查 docs/test_results/<version>/ 是否存在測試報告

    Returns:
        (是否通過, 警告訊息列表)
    """
    test_dir = Path(f"docs/test_results/v{version}")
    warnings = []

    if not test_dir.exists():
        warnings.append(f"目錄 docs/test_results/v{version}/ 不存在")
        # 檢查是否有變體 (如 v2.11.0-Phase5)
        parent_dir = Path("docs/test_results")
        variant_dirs = list(parent_dir.glob(f"v{version}*"))
        if variant_dirs:
            print(f"{WARN} 測試報告目錄不存在，但找到變體:")
            for d in variant_dirs:
                print(f"   - {d.name}")
            warnings.append("建議檢查變體目錄是否包含完整測試報告")
            return False, warnings
        return False, warnings

    # 檢查是否有測試報告檔案
    test_files = list(test_dir.glob("*.md"))

    if not test_files:
        warnings.append(f"docs/test_results/v{version}/ 目錄為空 (沒有測試報告)")
        return False, warnings

    # 檢查是否有 HANDOVER_SUMMARY 或 TEST_REPORT
    handover_files = list(test_dir.glob("*HANDOVER*.md"))
    report_files = list(test_dir.glob("*TEST_REPORT*.md"))

    print(f"{PASS} 測試報告存在: {len(test_files)} 個檔案")
    if handover_files:
        print(f"   - 交接摘要: {', '.join([f.name for f in handover_files])}")
    if report_files:
        print(f"   - 測試報告: {', '.join([f.name for f in report_files])}")

    return True, []

def check_branch_releases(version: str) -> Tuple[bool, List[str]]:
    """
    檢查 devspec/BRANCH_RELEASES.md 是否包含當前版本記錄

    Returns:
        (是否通過, 警告訊息列表)
    """
    releases_file = Path("devspec/BRANCH_RELEASES.md")
    warnings = []

    if not releases_file.exists():
        warnings.append("找不到 devspec/BRANCH_RELEASES.md")
        return False, warnings

    try:
        content = releases_file.read_text(encoding='utf-8')

        # 檢查是否包含當前版本 (v2.11.1 或 2.11.1)
        version_patterns = [
            f"### v{version}",  # 標題格式
            f"v{version}",      # 任何提及
            version             # 純數字
        ]

        found = any(pattern in content for pattern in version_patterns)

        if not found:
            warnings.append(f"BRANCH_RELEASES.md 不包含 v{version} 的記錄")
            return False, warnings

        print(f"{PASS} BRANCH_RELEASES.md 包含 v{version} 記錄")

        # 檢查狀態標示
        if "✅" in content and f"v{version}" in content:
            print(f"   - 發現穩定版本標記 ✅")

        return True, []

    except Exception as e:
        warnings.append(f"讀取 BRANCH_RELEASES.md 失敗: {e}")
        return False, warnings

def check_feature_record_backlog(version: str) -> Tuple[bool, List[str]]:
    """
    檢查 devspec/feature_record_Backlog.md 是否有相關更新

    Returns:
        (是否通過, 警告訊息列表)
    """
    backlog_file = Path("devspec/feature_record_Backlog.md")
    warnings = []

    if not backlog_file.exists():
        warnings.append("找不到 devspec/feature_record_Backlog.md")
        return False, warnings

    try:
        content = backlog_file.read_text(encoding='utf-8')

        # 檢查是否提及當前版本
        if f"v{version}" in content or version in content:
            print(f"{PASS} feature_record_Backlog.md 包含 v{version} 相關記錄")
            return True, []
        else:
            warnings.append(f"feature_record_Backlog.md 未提及 v{version}")
            warnings.append("建議檢查是否需要更新決議文檔")
            return False, warnings

    except Exception as e:
        warnings.append(f"讀取 feature_record_Backlog.md 失敗: {e}")
        return False, warnings

def check_docs_sync_log() -> Tuple[bool, List[str]]:
    """
    檢查是否存在 DOCS_SYNC_LOG_*.md (MERGE_SOP Phase 2 執行證據)

    Returns:
        (是否通過, 警告訊息列表)
    """
    devspec_dir = Path("devspec")
    warnings = []

    if not devspec_dir.exists():
        warnings.append("找不到 devspec/ 目錄")
        return False, warnings

    # 查找 DOCS_SYNC_LOG 檔案
    sync_logs = list(devspec_dir.glob("DOCS_SYNC_LOG_*.md"))

    if not sync_logs:
        warnings.append("找不到 DOCS_SYNC_LOG_*.md (未執行 MERGE_SOP Phase 2 文檔同步)")
        warnings.append("建議參考 devspec/MERGE_SOP.md Phase 2 執行文檔同步")
        return False, warnings

    # 找到最新的同步日誌
    latest_log = max(sync_logs, key=lambda p: p.stat().st_mtime)
    print(f"{PASS} 找到文檔同步日誌: {latest_log.name}")

    return True, []

def main():
    parser = argparse.ArgumentParser(
        description="檢查 Git Merge 前的準備狀態 (MERGE_SOP 合規性)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
範例:
  python tools/check_merge_readiness.py
  python tools/check_merge_readiness.py --bypass

詳細說明:
  - 此工具檢查是否已完成 MERGE_SOP.md 的 Phase 2 文檔同步
  - 確保 docs/features/ 和 docs/test_results/ 包含當前版本文檔
  - 驗證 BRANCH_RELEASES.md 已更新

參考文檔:
  - devspec/MERGE_SOP.md
  - devspec/GIT_WORKFLOW_IMPROVEMENT_FINAL_RECOMMENDATION.md
        """
    )
    parser.add_argument(
        '--bypass',
        action='store_true',
        help='繞過檢查 (僅顯示警告，不阻擋合併)'
    )
    parser.add_argument(
        '--verbose',
        '-v',
        action='store_true',
        help='顯示詳細資訊'
    )

    args = parser.parse_args()

    print_header("🔍 Git Merge Readiness Check")

    # Step 1: 偵測版本
    print_section("Step 1: 版本偵測")
    version = extract_version_from_gradle()
    if not version:
        print(f"\n{FAIL} 無法偵測版本號，中止檢查")
        sys.exit(1)

    # 收集所有檢查結果
    all_passed = True
    all_warnings = []

    # Step 2: 檢查功能文檔
    print_section(f"Step 2: 檢查功能文檔 (docs/features/v{version}/)")
    passed, warnings = check_feature_docs(version)
    if not passed:
        all_passed = False
        all_warnings.extend(warnings)
        for w in warnings:
            print(f"{WARN} {w}")

    # Step 3: 檢查測試報告
    print_section(f"Step 3: 檢查測試報告 (docs/test_results/v{version}/)")
    passed, warnings = check_test_results(version)
    if not passed:
        all_passed = False
        all_warnings.extend(warnings)
        for w in warnings:
            print(f"{WARN} {w}")

    # Step 4: 檢查 BRANCH_RELEASES.md
    print_section("Step 4: 檢查分支發布記錄")
    passed, warnings = check_branch_releases(version)
    if not passed:
        all_passed = False
        all_warnings.extend(warnings)
        for w in warnings:
            print(f"{WARN} {w}")

    # Step 5: 檢查 feature_record_Backlog.md
    print_section("Step 5: 檢查功能決議文檔")
    passed, warnings = check_feature_record_backlog(version)
    if not passed:
        all_passed = False
        all_warnings.extend(warnings)
        for w in warnings:
            print(f"{WARN} {w}")

    # Step 6: 檢查文檔同步日誌 (可選)
    print_section("Step 6: 檢查文檔同步日誌 (Optional)")
    passed, warnings = check_docs_sync_log()
    if not passed:
        # 這不是強制的，只顯示警告
        for w in warnings:
            print(f"{INFO} {w}")

    # 最終結果
    print_header("📊 檢查結果")

    if all_passed:
        print(f"\n{Colors.GREEN}{Colors.BOLD}✅ READY TO MERGE{Colors.END}\n")
        print(f"{Colors.GREEN}所有檢查通過，您可以安全執行 git merge{Colors.END}")
        print(f"\n{Colors.CYAN}請參考 devspec/MERGE_SOP.md 執行合併流程{Colors.END}")
        sys.exit(0)
    else:
        print(f"\n{Colors.YELLOW}{Colors.BOLD}⚠️  NOT READY - 發現 {len(all_warnings)} 個問題{Colors.END}\n")

        print(f"{Colors.BOLD}問題列表:{Colors.END}")
        for i, warning in enumerate(all_warnings, 1):
            print(f"  {i}. {warning}")

        print(f"\n{Colors.YELLOW}建議動作:{Colors.END}")
        print(f"  1. 執行 devspec/MERGE_SOP.md Phase 2 (文檔同步)")
        print(f"  2. 確保 docs/features/v{version}/ 包含功能文檔")
        print(f"  3. 確保 docs/test_results/v{version}/ 包含測試報告")
        print(f"  4. 更新 devspec/BRANCH_RELEASES.md")
        print(f"  5. 更新 devspec/feature_record_Backlog.md")

        if args.bypass:
            print(f"\n{Colors.YELLOW}⚠️  --bypass 模式: 允許繼續 (風險自負){Colors.END}")
            sys.exit(0)
        else:
            print(f"\n{Colors.RED}❌ 合併被阻擋{Colors.END}")
            print(f"{Colors.CYAN}若要強制繼續，使用: python tools/check_merge_readiness.py --bypass{Colors.END}")
            sys.exit(1)

if __name__ == "__main__":
    main()
