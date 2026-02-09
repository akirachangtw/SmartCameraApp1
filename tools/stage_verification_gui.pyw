#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
stage_verification_gui.pyw
Stage Verification Manager with Modern GUI
階段驗測管理員 - 自動化測試驗證工具
"""

import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext, filedialog
import subprocess
import os
import sys
import datetime
import json
import time
from pathlib import Path
from typing import List, Optional, Tuple, Dict

# 設定預設編碼為 UTF-8
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')


class Colors:
    """Modern color scheme (same as Test Handoff GUI)"""
    PRIMARY = "#2980b9"      # Primary blue
    SUCCESS = "#27ae60"      # Success green
    WARNING = "#f39c12"      # Warning orange
    DANGER = "#e74c3c"       # Danger red
    BACKGROUND = "#ecf0f1"   # Light gray background
    CARD_BG = "#ffffff"      # Card background (white)
    TEXT_DARK = "#2c3e50"    # Dark text
    TEXT_LIGHT = "#ffffff"   # Light text
    BORDER = "#bdc3c7"       # Border gray


class ConfigManager:
    """Manage configuration for Stage Verification Tool"""

    DEFAULT_CONFIG = {
        "defaults": {
            "version": "v2.11.1",
            "emulator": "Medium_Phone_API_36.1",
            "device": "Pixel 9a"
        },
        "paths": {
            "test_results": "docs/test_results/",
            "google_16kb_tool": "docs/test_results/v2.9.6-Phase2.6/Google-check_elf_alignment.sh",
            "apk_emulator": "app/build/outputs/apk/emulator/debug/app-emulator-debug.apk",
            "apk_device": "app/build/outputs/apk/device/debug/app-device-debug.apk"
        },
        "gradle": {
            "clean": "./gradlew clean",
            "build_emulator": "./gradlew assembleEmulatorDebug",
            "build_device": "./gradlew assembleDeviceDebug",
            "lint": "./gradlew lintEmulatorDebug"
        },
        "emulator": {
            "android_home": "",
            "wait_timeout": 60,
            "boot_check_interval": 2
        },
        "verification_results": {}
    }

    def __init__(self, config_path=".vscode/stage_verification_config.json"):
        self.config_path = Path(config_path)
        self.config = self.load()

    def load(self) -> dict:
        """Load configuration from file, or use defaults"""
        if self.config_path.exists():
            try:
                with open(self.config_path, 'r', encoding='utf-8') as f:
                    loaded_config = json.load(f)
                config = self.DEFAULT_CONFIG.copy()
                self._deep_update(config, loaded_config)
                return config
            except Exception as e:
                print(f"Warning: Failed to load config: {e}")
                return self.DEFAULT_CONFIG.copy()
        return self.DEFAULT_CONFIG.copy()

    def save(self):
        """Save current configuration to file"""
        try:
            self.config_path.parent.mkdir(parents=True, exist_ok=True)
            with open(self.config_path, 'w', encoding='utf-8') as f:
                json.dump(self.config, f, indent=2, ensure_ascii=False)
            return True
        except Exception as e:
            print(f"Failed to save configuration: {e}")
            return False

    def get(self, key_path: str, default=None):
        """Get configuration value using dot notation"""
        keys = key_path.split('.')
        value = self.config
        for key in keys:
            if isinstance(value, dict):
                value = value.get(key)
                if value is None:
                    return default
            else:
                return default
        return value

    def set(self, key_path: str, value):
        """Set configuration value using dot notation"""
        keys = key_path.split('.')
        config = self.config
        for key in keys[:-1]:
            if key not in config:
                config[key] = {}
            config = config[key]
        config[keys[-1]] = value

    def _deep_update(self, base_dict, update_dict):
        """Recursively update base_dict with values from update_dict"""
        for key, value in update_dict.items():
            if isinstance(value, dict) and key in base_dict and isinstance(base_dict[key], dict):
                self._deep_update(base_dict[key], value)
            else:
                base_dict[key] = value


class EmulatorManager:
    """Manage Android Emulator operations"""

    def __init__(self):
        self.android_home = self.detect_android_home()
        self.emulator_path = self.get_emulator_path()

    def detect_android_home(self) -> Optional[str]:
        """Auto-detect Android SDK path"""
        if 'ANDROID_HOME' in os.environ:
            return os.environ['ANDROID_HOME']

        default_path = os.path.join(os.environ.get('LOCALAPPDATA', ''), 'Android', 'Sdk')
        if os.path.exists(default_path):
            return default_path
        return None

    def get_emulator_path(self) -> Optional[str]:
        """Get emulator executable path"""
        if not self.android_home:
            return None
        emulator_exe = os.path.join(self.android_home, 'emulator', 'emulator.exe')
        if os.path.exists(emulator_exe):
            return emulator_exe
        return None

    def list_avds(self) -> List[str]:
        """List available AVDs"""
        if not self.emulator_path:
            return []

        try:
            result = subprocess.run(
                [self.emulator_path, '-list-avds'],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )
            avds = [line.strip() for line in result.stdout.strip().split('\n') if line.strip()]
            return avds if avds else []
        except Exception as e:
            print(f"Failed to list AVDs: {e}")
            return []

    def start_emulator(self, avd_name: str) -> bool:
        """Start emulator using PowerShell"""
        if not self.emulator_path:
            return False

        try:
            cmd = f'Start-Process "{self.emulator_path}" -ArgumentList "-avd", "{avd_name}", "-gpu", "host" -NoNewWindow'
            subprocess.run(['powershell', '-Command', cmd], check=True)
            return True
        except Exception as e:
            print(f"Failed to start emulator: {e}")
            return False

    def wait_for_boot(self, timeout=60) -> Tuple[bool, Optional[str]]:
        """Wait for emulator to boot completely"""
        elapsed = 0
        while elapsed < timeout:
            try:
                result = subprocess.run(['adb', 'devices'], capture_output=True, text=True)
                if 'emulator' in result.stdout and 'device' in result.stdout:
                    # Check boot completed
                    boot_result = subprocess.run(
                        ['adb', 'shell', 'getprop', 'sys.boot_completed'],
                        capture_output=True,
                        text=True
                    )
                    if '1' in boot_result.stdout:
                        device_id = self.get_device_id()
                        return True, device_id
            except Exception:
                pass
            time.sleep(2)
            elapsed += 2
        return False, None

    def get_device_id(self) -> Optional[str]:
        """Get emulator device ID"""
        try:
            result = subprocess.run(['adb', 'devices'], capture_output=True, text=True)
            for line in result.stdout.split('\n'):
                if 'emulator' in line and 'device' in line:
                    return line.split()[0]
        except Exception:
            pass
        return None

    def install_apk(self, device_id: str, apk_path: str) -> bool:
        """Install APK to emulator"""
        try:
            result = subprocess.run(
                ['adb', '-s', device_id, 'install', '-r', apk_path],
                capture_output=True,
                text=True
            )
            return result.returncode == 0
        except Exception:
            return False

    def start_logcat(self, device_id: str, output_file: str):
        """Start logcat monitoring"""
        try:
            subprocess.run(['adb', '-s', device_id, 'logcat', '-c'])
            logcat_process = subprocess.Popen(
                ['adb', '-s', device_id, 'logcat', '-v', 'time'],
                stdout=open(output_file, 'w', encoding='utf-8'),
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )
            return logcat_process
        except Exception as e:
            print(f"Failed to start logcat: {e}")
            return None


class StageVerificationApp:
    """Stage Verification Manager GUI Application"""

    def __init__(self, root: tk.Tk):
        self.root = root
        self.config = ConfigManager()
        self.emulator_manager = EmulatorManager()
        self.logcat_process = None
        self.phase_results = {}

        # Configure root window
        self.root.title("階段驗測管理員")
        self.root.geometry("920x750")
        self.root.resizable(True, True)
        self.root.minsize(800, 600)
        self.root.configure(bg=Colors.BACKGROUND)

        # Setup GUI
        self.setup_ui()

        # Log startup info
        self.write_log("🚀 階段驗測管理員已啟動", "Success")
        if self.emulator_manager.android_home:
            self.write_log(f"📱 Android SDK: {self.emulator_manager.android_home}", "Info")
        else:
            self.write_log("⚠️ 未找到 Android SDK，請設定 ANDROID_HOME 環境變數", "Warning")

    def setup_ui(self):
        """Setup user interface"""
        self.create_header()
        self.create_input_section()
        self.create_tabs_section()
        self.create_log_section()
        self.create_statusbar()

    def create_header(self):
        """Create header panel"""
        header = tk.Frame(self.root, bg=Colors.PRIMARY, height=50)
        header.pack(fill=tk.X)
        header.pack_propagate(False)

        title_label = tk.Label(
            header,
            text="🧪  階段驗測管理員",
            font=("Microsoft JhengHei UI", 14, "bold"),
            fg=Colors.TEXT_LIGHT,
            bg=Colors.PRIMARY
        )
        title_label.pack(side=tk.LEFT, padx=20, pady=12)

        subtitle_label = tk.Label(
            header,
            text="自動化測試驗證工具  •  Pre-Build → Build → 16KB → Emulator → Device",
            font=("Microsoft JhengHei UI", 9),
            fg=Colors.TEXT_LIGHT,
            bg=Colors.PRIMARY
        )
        subtitle_label.pack(side=tk.LEFT, padx=(0, 20), pady=12)

    def create_input_section(self):
        """Create version input section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 📝 驗測資訊 ",
            font=("Microsoft JhengHei UI", 10, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=15, pady=(8, 5), fill=tk.X)

        # Version
        version_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        version_frame.pack(pady=(5, 8), padx=10, fill=tk.X)

        tk.Label(
            version_frame,
            text="版本號:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            width=10,
            anchor=tk.W
        ).pack(side=tk.LEFT)

        self.version_var = tk.StringVar(value=self.config.get('defaults.version', 'v2.11.1'))
        tk.Entry(
            version_frame,
            textvariable=self.version_var,
            font=("Consolas", 9),
            width=20
        ).pack(side=tk.LEFT)

        tk.Label(
            version_frame,
            text="  測試結果將儲存至: docs/test_results/<version>/",
            font=("Microsoft JhengHei UI", 8),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(side=tk.LEFT, padx=(10, 0))

    def create_tabs_section(self):
        """Create tabs section"""
        self.notebook = ttk.Notebook(self.root)
        self.notebook.pack(padx=15, pady=5, fill=tk.BOTH, expand=True)

        self.create_phase1_tab()
        self.create_phase2_tab()
        self.create_phase3_tab()
        self.create_phase4_tab()
        self.create_phase5_tab()
        self.create_settings_tab()

    def create_phase1_tab(self):
        """Phase 1: Pre-Build Check"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="1. 📋 Pre-Build")

        tk.Label(
            frame,
            text="階段 1: Pre-Build 檢查",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="檢查 Git 狀態、代碼規範、變更範圍。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        self.create_button(
            frame,
            "🔍 檢查 Git 狀態",
            Colors.BORDER,
            self.check_git_status,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "📋 檢查代碼規範",
            Colors.BORDER,
            self.check_code_conventions,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        tk.Frame(frame, height=2, bg=Colors.BORDER).pack(fill=tk.X, pady=10)

        self.create_button(
            frame,
            "✅ 執行 Phase 1",
            Colors.SUCCESS,
            self.run_phase1
        ).pack(fill=tk.X, pady=5)

    def create_phase2_tab(self):
        """Phase 2: Build Verification"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="2. 🔨 Build")

        tk.Label(
            frame,
            text="階段 2: Build 驗證",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="Clean build → Build Emulator Debug → Lint 檢查。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        self.create_button(
            frame,
            "🧹 Clean Build",
            Colors.BORDER,
            self.run_clean_build,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "🔨 Build Emulator Debug",
            Colors.BORDER,
            self.run_emulator_build,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "🔍 Lint 檢查",
            Colors.BORDER,
            self.run_lint_check,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        tk.Frame(frame, height=2, bg=Colors.BORDER).pack(fill=tk.X, pady=10)

        self.create_button(
            frame,
            "✅ 執行 Phase 2",
            Colors.SUCCESS,
            self.run_phase2
        ).pack(fill=tk.X, pady=5)

    def create_phase3_tab(self):
        """Phase 3: 16KB Compatibility"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="3. 🧪 16KB")

        tk.Label(
            frame,
            text="階段 3: 16KB Compatibility",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="使用 Google 官方工具檢測 16KB 頁面大小兼容性。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        self.create_button(
            frame,
            "🔍 檢查 APK 路徑",
            Colors.BORDER,
            self.check_apk_path,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "🧪 執行 16KB 檢測 (Emulator APK)",
            Colors.WARNING,
            self.run_16kb_check
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "📄 查看檢測報告",
            Colors.BORDER,
            self.view_16kb_report,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

    def create_phase4_tab(self):
        """Phase 4: Emulator Testing"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="4. 🚀 Emulator")

        tk.Label(
            frame,
            text="階段 4: Emulator 測試",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="啟動模擬器 → 安裝 APK → Logcat 監控 → 功能測試。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        # Emulator selection
        emulator_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        emulator_frame.pack(fill=tk.X, pady=10)

        tk.Label(
            emulator_frame,
            text="選擇模擬器:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            width=12,
            anchor=tk.W
        ).pack(side=tk.LEFT)

        avds = self.emulator_manager.list_avds()
        default_emulator = self.config.get('defaults.emulator', '')

        self.emulator_var = tk.StringVar(value=default_emulator if default_emulator in avds else (avds[0] if avds else ''))
        self.emulator_combo = ttk.Combobox(
            emulator_frame,
            textvariable=self.emulator_var,
            values=avds,
            width=35
        )
        self.emulator_combo.pack(side=tk.LEFT, fill=tk.X, expand=True)

        self.create_button(
            emulator_frame,
            "🔄",
            Colors.BORDER,
            self.refresh_emulators,
            fg=Colors.TEXT_DARK,
            width=3
        ).pack(side=tk.LEFT, padx=(5, 0))

        # Buttons
        self.create_button(
            frame,
            "🚀 啟動模擬器",
            Colors.SUCCESS,
            self.start_emulator
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "📱 安裝 APK",
            Colors.PRIMARY,
            self.install_apk
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "📋 啟動 Logcat 監控",
            Colors.WARNING,
            self.start_logcat
        ).pack(fill=tk.X, pady=5)

        tk.Frame(frame, height=2, bg=Colors.BORDER).pack(fill=tk.X, pady=10)

        self.create_button(
            frame,
            "✅ 執行 Phase 4 (完整流程)",
            Colors.SUCCESS,
            self.run_phase4
        ).pack(fill=tk.X, pady=5)

    def create_phase5_tab(self):
        """Phase 5: Device Testing"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="5. 📱 Device")

        tk.Label(
            frame,
            text="階段 5: Device 測試",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="Build Device Debug → 16KB 檢測 → Test Handoff。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        self.create_button(
            frame,
            "🔨 Build Device Debug",
            Colors.PRIMARY,
            self.run_device_build
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "🧪 執行 16KB 檢測 (Device APK)",
            Colors.WARNING,
            self.run_16kb_check_device
        ).pack(fill=tk.X, pady=5)

        tk.Frame(frame, height=2, bg=Colors.BORDER).pack(fill=tk.X, pady=10)

        self.create_button(
            frame,
            "🚀 啟動 Test Handoff GUI",
            Colors.SUCCESS,
            self.launch_test_handoff
        ).pack(fill=tk.X, pady=5)

    def create_settings_tab(self):
        """Settings Tab"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="⚙️ Settings")

        tk.Label(
            frame,
            text="設定",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 15))

        # Android SDK Path
        sdk_frame = tk.LabelFrame(frame, text=" Android SDK ", bg=Colors.CARD_BG, font=("Microsoft JhengHei UI", 9, "bold"))
        sdk_frame.pack(fill=tk.X, pady=10)

        tk.Label(
            sdk_frame,
            text=f"Path: {self.emulator_manager.android_home or '未設定'}",
            font=("Consolas", 8),
            bg=Colors.CARD_BG
        ).pack(anchor=tk.W, padx=10, pady=5)

        # Config File
        config_frame = tk.LabelFrame(frame, text=" Configuration File ", bg=Colors.CARD_BG, font=("Microsoft JhengHei UI", 9, "bold"))
        config_frame.pack(fill=tk.X, pady=10)

        tk.Label(
            config_frame,
            text=f"Path: {self.config.config_path}",
            font=("Consolas", 8),
            bg=Colors.CARD_BG
        ).pack(anchor=tk.W, padx=10, pady=5)

        self.create_button(
            config_frame,
            "💾 儲存當前設定",
            Colors.PRIMARY,
            self.save_config
        ).pack(padx=10, pady=5, fill=tk.X)

        self.create_button(
            config_frame,
            "📄 開啟配置檔案",
            Colors.BORDER,
            self.open_config_file,
            fg=Colors.TEXT_DARK
        ).pack(padx=10, pady=5, fill=tk.X)

    def create_log_section(self):
        """Create log display section"""
        log_frame = tk.LabelFrame(
            self.root,
            text=" 📋 執行日誌 ",
            font=("Microsoft JhengHei UI", 10, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        log_frame.pack(padx=15, pady=5, fill=tk.BOTH, expand=True)

        self.log_text = scrolledtext.ScrolledText(
            log_frame,
            height=10,
            font=("Consolas", 9),
            bg="#2c3e50",
            fg="#ecf0f1",
            insertbackground="white",
            wrap=tk.WORD
        )
        self.log_text.pack(padx=5, pady=5, fill=tk.BOTH, expand=True)

        # Color tags
        self.log_text.tag_config("Success", foreground="#27ae60")
        self.log_text.tag_config("Error", foreground="#e74c3c")
        self.log_text.tag_config("Warning", foreground="#f39c12")
        self.log_text.tag_config("Info", foreground="#3498db")

    def create_statusbar(self):
        """Create status bar"""
        self.statusbar = tk.Label(
            self.root,
            text="就緒",
            bd=1,
            relief=tk.SUNKEN,
            anchor=tk.W,
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        )
        self.statusbar.pack(side=tk.BOTTOM, fill=tk.X)

    def create_button(self, parent, text, bg_color, command, fg=Colors.TEXT_LIGHT, width=None):
        """Create styled button"""
        button = tk.Button(
            parent,
            text=text,
            font=("Microsoft JhengHei UI", 9),
            bg=bg_color,
            fg=fg,
            activebackground=bg_color,
            activeforeground=fg,
            relief=tk.FLAT,
            cursor="hand2",
            command=command,
            padx=10,
            pady=8,
            width=width
        )
        return button

    def write_log(self, message: str, level: str = "Info"):
        """Write log message"""
        timestamp = datetime.datetime.now().strftime("%H:%M:%S")
        log_message = f"[{timestamp}] {message}\n"
        self.log_text.insert(tk.END, log_message, level)
        self.log_text.see(tk.END)
        self.log_text.update()

    def update_status(self, message: str):
        """Update status bar"""
        self.statusbar.config(text=message)
        self.statusbar.update()

    # ========== Phase 1: Pre-Build Check ==========
    def check_git_status(self):
        """Check Git status"""
        self.write_log("🔍 檢查 Git 狀態...", "Info")
        self.update_status("執行中: Git Status")

        try:
            result = subprocess.run(
                ['git', 'status', '--short'],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            if result.returncode == 0:
                lines = [line for line in result.stdout.strip().split('\n') if line.strip()]
                if lines:
                    self.write_log(f"✅ Git status:\n{result.stdout}", "Success")
                    self.write_log(f"📊 Modified/Untracked files: {len(lines)}", "Info")
                else:
                    self.write_log("✅ Git status clean - 無未追蹤的變更", "Success")
                self.update_status("完成: Git Status")
                return True
            else:
                self.write_log(f"❌ Git status 失敗: {result.stderr}", "Error")
                self.update_status("失敗: Git Status")
                return False
        except Exception as e:
            self.write_log(f"❌ 執行失敗: {str(e)}", "Error")
            self.update_status("失敗: Git Status")
            return False

    def check_code_conventions(self):
        """Check code conventions (placeholder)"""
        self.write_log("📋 檢查代碼規範...", "Info")
        self.update_status("執行中: Code Conventions")

        # Placeholder - actual implementation would check naming, hardcoded strings, etc.
        self.write_log("ℹ️ 代碼規範檢查功能待實作", "Warning")
        self.write_log("  建議檢查項目:", "Info")
        self.write_log("  - 變數名稱至少 3 個字元", "Info")
        self.write_log("  - 無硬編碼字串（使用 strings.xml）", "Info")
        self.write_log("  - 無硬編碼顏色（使用 colors.xml）", "Info")

        self.update_status("完成: Code Conventions")
        return True

    def run_phase1(self):
        """Execute Phase 1: Pre-Build Check"""
        self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Info")
        self.write_log("🚀 開始執行 Phase 1: Pre-Build 檢查", "Info")
        self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Info")
        self.update_status("執行中: Phase 1")

        results = []

        # 1. Git Status
        self.write_log("\n1. Git Status 檢查", "Info")
        results.append(self.check_git_status())

        # 2. Code Conventions
        self.write_log("\n2. 代碼規範檢查", "Info")
        results.append(self.check_code_conventions())

        # Summary
        if all(results):
            self.write_log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Success")
            self.write_log("✅ Phase 1: PASS", "Success")
            self.write_log("可以繼續執行 Phase 2 (Build 驗證)", "Success")
            self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Success")
            self.phase_results['phase1'] = 'PASS'
            self.update_status("✅ Phase 1 完成")
            messagebox.showinfo("Phase 1 完成", "Pre-Build 檢查已完成！\n\n可以繼續執行 Phase 2。")
        else:
            self.write_log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Error")
            self.write_log("❌ Phase 1: FAIL", "Error")
            self.write_log("請修正問題後重新執行", "Error")
            self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Error")
            self.phase_results['phase1'] = 'FAIL'
            self.update_status("❌ Phase 1 失敗")
            messagebox.showerror("Phase 1 失敗", "Pre-Build 檢查失敗，請修正問題後重試。")

    # ========== Phase 2: Build Verification ==========
    def run_clean_build(self):
        """Run clean build"""
        self.write_log("🧹 開始 Clean Build...", "Info")
        self.update_status("執行中: Clean Build")

        try:
            result = subprocess.run(
                ['./gradlew', 'clean'],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            if result.returncode == 0:
                self.write_log("✅ Clean build 成功", "Success")
                self.update_status("完成: Clean Build")
                return True
            else:
                self.write_log(f"❌ Clean build 失敗:\n{result.stderr}", "Error")
                self.update_status("失敗: Clean Build")
                return False
        except Exception as e:
            self.write_log(f"❌ 執行失敗: {str(e)}", "Error")
            self.update_status("失敗: Clean Build")
            return False

    def run_emulator_build(self):
        """Build Emulator Debug APK"""
        self.write_log("🔨 開始編譯 Emulator Debug APK...", "Info")
        self.update_status("執行中: Build Emulator Debug (可能需要 2-5 分鐘)")

        start_time = time.time()

        try:
            result = subprocess.run(
                ['./gradlew', 'assembleEmulatorDebug'],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            elapsed = time.time() - start_time

            if result.returncode == 0:
                apk_path = self.config.get('paths.apk_emulator')
                if os.path.exists(apk_path):
                    apk_size = os.path.getsize(apk_path) / (1024 * 1024)
                    self.write_log("✅ Emulator Debug APK 編譯成功", "Success")
                    self.write_log(f"   📦 APK 大小: {apk_size:.2f} MB", "Info")
                    self.write_log(f"   ⏱️  編譯時間: {elapsed:.1f} 秒", "Info")
                    self.write_log(f"   📁 路徑: {apk_path}", "Info")
                    self.update_status("完成: Build Emulator Debug")
                    return True
                else:
                    self.write_log("❌ APK 檔案未生成", "Error")
                    self.update_status("失敗: Build Emulator Debug")
                    return False
            else:
                self.write_log(f"❌ 編譯失敗:\n{result.stderr[:500]}", "Error")
                self.update_status("失敗: Build Emulator Debug")
                return False
        except Exception as e:
            self.write_log(f"❌ 執行失敗: {str(e)}", "Error")
            self.update_status("失敗: Build Emulator Debug")
            return False

    def run_lint_check(self):
        """Run Lint check"""
        self.write_log("🔍 開始 Lint 檢查...", "Info")
        self.update_status("執行中: Lint 檢查")

        try:
            result = subprocess.run(
                ['./gradlew', 'lintEmulatorDebug'],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            if result.returncode == 0:
                self.write_log("✅ Lint 檢查完成", "Success")
                self.write_log("   📄 報告: app/build/reports/lint-results-emulatorDebug.html", "Info")
                self.update_status("完成: Lint 檢查")
                return True
            else:
                self.write_log(f"⚠️ Lint 檢查發現問題 (錯誤或警告)", "Warning")
                self.write_log("   📄 詳細報告: app/build/reports/lint-results-emulatorDebug.html", "Info")
                self.update_status("完成: Lint 檢查 (有警告)")
                return True  # Allow to continue even with warnings
        except Exception as e:
            self.write_log(f"❌ 執行失敗: {str(e)}", "Error")
            self.update_status("失敗: Lint 檢查")
            return False

    def run_phase2(self):
        """Execute Phase 2: Build Verification"""
        self.write_log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Info")
        self.write_log("🚀 開始執行 Phase 2: Build 驗證", "Info")
        self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Info")
        self.update_status("執行中: Phase 2")

        # 1. Clean Build
        self.write_log("\n1. Clean Build", "Info")
        if not self.run_clean_build():
            self.write_log("❌ Phase 2: FAIL (Clean Build)", "Error")
            self.phase_results['phase2'] = 'FAIL'
            self.update_status("❌ Phase 2 失敗")
            messagebox.showerror("Phase 2 失敗", "Clean Build 失敗！")
            return

        # 2. Build Emulator Debug
        self.write_log("\n2. Build Emulator Debug APK", "Info")
        if not self.run_emulator_build():
            self.write_log("❌ Phase 2: FAIL (Build)", "Error")
            self.phase_results['phase2'] = 'FAIL'
            self.update_status("❌ Phase 2 失敗")
            messagebox.showerror("Phase 2 失敗", "Emulator Debug 編譯失敗！")
            return

        # 3. Lint Check
        self.write_log("\n3. Lint 檢查", "Info")
        self.run_lint_check()

        # Summary
        self.write_log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Success")
        self.write_log("✅ Phase 2: PASS", "Success")
        self.write_log("可以繼續執行 Phase 3 (16KB 檢測)", "Success")
        self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Success")
        self.phase_results['phase2'] = 'PASS'
        self.update_status("✅ Phase 2 完成")
        messagebox.showinfo("Phase 2 完成", "Build 驗證已完成！\n\nAPK 已生成，可以繼續執行 Phase 3 (16KB 檢測)。")

    # ========== Phase 3: 16KB Compatibility ==========
    def check_apk_path(self):
        """Check if APK exists"""
        apk_path = self.config.get('paths.apk_emulator')
        if os.path.exists(apk_path):
            size = os.path.getsize(apk_path) / (1024 * 1024)
            self.write_log(f"✅ APK 檔案存在", "Success")
            self.write_log(f"   📁 路徑: {apk_path}", "Info")
            self.write_log(f"   📦 大小: {size:.2f} MB", "Info")
        else:
            self.write_log(f"❌ APK 檔案不存在: {apk_path}", "Error")
            self.write_log("   請先執行 Phase 2 (Build 驗證)", "Warning")

    def run_16kb_check(self):
        """Run 16KB compatibility check"""
        self.write_log("🧪 開始執行 16KB 檢測...", "Info")
        self.update_status("執行中: 16KB 檢測")

        tool_path = self.config.get('paths.google_16kb_tool')
        apk_path = self.config.get('paths.apk_emulator')

        if not os.path.exists(tool_path):
            self.write_log(f"❌ 16KB 檢測工具不存在: {tool_path}", "Error")
            self.update_status("失敗: 16KB 檢測")
            return

        if not os.path.exists(apk_path):
            self.write_log(f"❌ APK 檔案不存在: {apk_path}", "Error")
            self.write_log("   請先執行 Phase 2 (Build 驗證)", "Warning")
            self.update_status("失敗: 16KB 檢測")
            return

        try:
            # Run bash script
            result = subprocess.run(
                ['bash', tool_path, apk_path],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            # Save output
            version = self.version_var.get()
            output_dir = Path(self.config.get('paths.test_results')) / version
            output_dir.mkdir(parents=True, exist_ok=True)
            output_file = output_dir / "16kb_check_emulator.log"

            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(result.stdout)

            # Check result
            if 'ELF Verification Successful' in result.stdout:
                self.write_log("✅ 16KB 兼容性檢測通過", "Success")
                self.write_log("   ELF Verification Successful", "Success")
                self.write_log(f"   📄 報告已儲存: {output_file}", "Info")
                self.phase_results['phase3'] = 'PASS'
                self.update_status("✅ 16KB 檢測通過")
                messagebox.showinfo("16KB 檢測通過", "16KB 兼容性檢測通過！\n\n可以繼續執行 Phase 4 (Emulator 測試)。")
            else:
                self.write_log("❌ 16KB 兼容性檢測失敗", "Error")
                self.write_log(f"   📄 詳細報告: {output_file}", "Info")
                self.write_log("\n檢測輸出:", "Warning")
                self.write_log(result.stdout, "Info")
                self.phase_results['phase3'] = 'FAIL'
                self.update_status("❌ 16KB 檢測失敗")
                messagebox.showerror("16KB 檢測失敗", "16KB 兼容性檢測失敗！\n\n請查看日誌了解詳情。")
        except Exception as e:
            self.write_log(f"❌ 執行失敗: {str(e)}", "Error")
            self.update_status("失敗: 16KB 檢測")

    def view_16kb_report(self):
        """View 16KB check report"""
        version = self.version_var.get()
        report_file = Path(self.config.get('paths.test_results')) / version / "16kb_check_emulator.log"

        if report_file.exists():
            try:
                os.startfile(str(report_file))
                self.write_log(f"📄 開啟報告: {report_file}", "Info")
            except Exception as e:
                self.write_log(f"❌ 無法開啟報告: {str(e)}", "Error")
        else:
            self.write_log(f"❌ 報告不存在: {report_file}", "Error")
            self.write_log("   請先執行 16KB 檢測", "Warning")

    # ========== Phase 4: Emulator Testing ==========
    def refresh_emulators(self):
        """Refresh emulator list"""
        self.write_log("🔄 刷新模擬器列表...", "Info")
        avds = self.emulator_manager.list_avds()
        self.emulator_combo['values'] = avds
        if avds:
            self.write_log(f"✅ 找到 {len(avds)} 個模擬器", "Success")
            for avd in avds:
                self.write_log(f"   - {avd}", "Info")
        else:
            self.write_log("⚠️ 未找到可用的模擬器", "Warning")

    def start_emulator(self):
        """Start emulator"""
        avd_name = self.emulator_var.get()
        if not avd_name:
            messagebox.showwarning("警告", "請先選擇模擬器！")
            return

        self.write_log(f"🚀 啟動模擬器: {avd_name}", "Info")
        self.update_status(f"啟動中: {avd_name}")

        if self.emulator_manager.start_emulator(avd_name):
            self.write_log("✅ 模擬器啟動指令已發送", "Success")
            self.write_log("⏳ 等待模擬器啟動完成 (最多 60 秒)...", "Info")

            # Wait for boot
            success, device_id = self.emulator_manager.wait_for_boot()

            if success:
                self.write_log(f"✅ 模擬器啟動成功: {device_id}", "Success")
                self.update_status(f"✅ 模擬器已啟動: {device_id}")
                messagebox.showinfo("模擬器已啟動", f"模擬器已成功啟動！\n\nDevice ID: {device_id}")
            else:
                self.write_log("⚠️ 模擬器啟動超時 (60 秒)", "Warning")
                self.write_log("   請手動檢查模擬器狀態", "Warning")
                self.update_status("⚠️ 模擬器啟動超時")
        else:
            self.write_log("❌ 模擬器啟動失敗", "Error")
            self.update_status("❌ 模擬器啟動失敗")

    def install_apk(self):
        """Install APK to emulator"""
        device_id = self.emulator_manager.get_device_id()
        if not device_id:
            messagebox.showwarning("警告", "未偵測到模擬器！\n\n請先啟動模擬器。")
            return

        apk_path = self.config.get('paths.apk_emulator')
        if not os.path.exists(apk_path):
            messagebox.showerror("錯誤", f"APK 檔案不存在！\n\n{apk_path}\n\n請先執行 Phase 2 (Build 驗證)。")
            return

        self.write_log(f"📱 安裝 APK 到 {device_id}...", "Info")
        self.update_status(f"安裝中: {device_id}")

        if self.emulator_manager.install_apk(device_id, apk_path):
            self.write_log("✅ APK 安裝成功", "Success")
            self.update_status("✅ APK 已安裝")
            messagebox.showinfo("APK 已安裝", f"APK 已成功安裝到模擬器！\n\nDevice: {device_id}")
        else:
            self.write_log("❌ APK 安裝失敗", "Error")
            self.update_status("❌ APK 安裝失敗")
            messagebox.showerror("安裝失敗", "APK 安裝失敗！")

    def start_logcat(self):
        """Start logcat monitoring"""
        device_id = self.emulator_manager.get_device_id()
        if not device_id:
            messagebox.showwarning("警告", "未偵測到模擬器！\n\n請先啟動模擬器。")
            return

        version = self.version_var.get()
        output_dir = Path(self.config.get('paths.test_results')) / version
        output_dir.mkdir(parents=True, exist_ok=True)
        output_file = output_dir / "emulator_test_logcat.log"

        self.write_log(f"📋 啟動 Logcat 監控: {device_id}", "Info")
        self.update_status(f"Logcat 監控中: {device_id}")

        self.logcat_process = self.emulator_manager.start_logcat(device_id, str(output_file))

        if self.logcat_process:
            self.write_log(f"✅ Logcat 已啟動 (背景執行)", "Success")
            self.write_log(f"   📁 輸出: {output_file}", "Info")
            self.write_log("   ℹ️  Logcat 將持續執行直到關閉 GUI", "Info")
            messagebox.showinfo("Logcat 已啟動", f"Logcat 監控已啟動！\n\n輸出: {output_file}\n\nLogcat 將持續執行直到關閉 GUI。")
        else:
            self.write_log("❌ Logcat 啟動失敗", "Error")
            self.update_status("❌ Logcat 啟動失敗")

    def run_phase4(self):
        """Execute Phase 4: Emulator Testing (Full Flow)"""
        avd_name = self.emulator_var.get()
        if not avd_name:
            messagebox.showwarning("警告", "請先選擇模擬器！")
            return

        self.write_log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Info")
        self.write_log("🚀 開始執行 Phase 4: Emulator 測試 (完整流程)", "Info")
        self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Info")
        self.update_status("執行中: Phase 4")

        # 1. Start emulator
        self.write_log(f"\n1. 啟動模擬器: {avd_name}", "Info")
        if not self.emulator_manager.start_emulator(avd_name):
            self.write_log("❌ Phase 4: FAIL (模擬器啟動)", "Error")
            self.phase_results['phase4'] = 'FAIL'
            self.update_status("❌ Phase 4 失敗")
            messagebox.showerror("Phase 4 失敗", "模擬器啟動失敗！")
            return

        self.write_log("✅ 模擬器啟動指令已發送", "Success")
        self.write_log("⏳ 等待模擬器啟動完成...", "Info")

        # 2. Wait for boot
        success, device_id = self.emulator_manager.wait_for_boot()
        if not success:
            self.write_log("❌ Phase 4: FAIL (模擬器啟動超時)", "Error")
            self.phase_results['phase4'] = 'FAIL'
            self.update_status("❌ Phase 4 失敗")
            messagebox.showerror("Phase 4 失敗", "模擬器啟動超時！")
            return

        self.write_log(f"✅ 模擬器已啟動: {device_id}", "Success")

        # 3. Install APK
        self.write_log(f"\n2. 安裝 APK 到 {device_id}", "Info")
        apk_path = self.config.get('paths.apk_emulator')
        if not self.emulator_manager.install_apk(device_id, apk_path):
            self.write_log("❌ Phase 4: FAIL (APK 安裝)", "Error")
            self.phase_results['phase4'] = 'FAIL'
            self.update_status("❌ Phase 4 失敗")
            messagebox.showerror("Phase 4 失敗", "APK 安裝失敗！")
            return

        self.write_log("✅ APK 安裝成功", "Success")

        # 4. Start Logcat
        self.write_log(f"\n3. 啟動 Logcat 監控", "Info")
        version = self.version_var.get()
        output_dir = Path(self.config.get('paths.test_results')) / version
        output_dir.mkdir(parents=True, exist_ok=True)
        output_file = output_dir / "emulator_test_logcat.log"

        self.logcat_process = self.emulator_manager.start_logcat(device_id, str(output_file))
        if self.logcat_process:
            self.write_log(f"✅ Logcat 已啟動: {output_file}", "Success")

        # Summary
        self.write_log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Success")
        self.write_log("✅ Phase 4: 自動化部分完成", "Success")
        self.write_log(f"   模擬器: {device_id}", "Success")
        self.write_log(f"   APK: 已安裝", "Success")
        self.write_log(f"   Logcat: 監控中", "Success")
        self.write_log("\n📱 請手動測試 App 功能:", "Info")
        self.write_log("   - App 啟動", "Info")
        self.write_log("   - 主要功能", "Info")
        self.write_log("   - UI 顯示", "Info")
        self.write_log("   - 穩定性測試", "Info")
        self.write_log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "Success")
        self.phase_results['phase4'] = 'PASS'
        self.update_status("✅ Phase 4 完成 (請手動測試)")
        messagebox.showinfo(
            "Phase 4 完成",
            f"Emulator 自動化部分已完成！\n\n"
            f"模擬器: {device_id}\n"
            f"APK: 已安裝\n"
            f"Logcat: 監控中\n\n"
            f"請手動測試 App 功能。"
        )

    # ========== Phase 5: Device Testing ==========
    def run_device_build(self):
        """Build Device Debug APK"""
        self.write_log("🔨 開始編譯 Device Debug APK...", "Info")
        self.update_status("執行中: Build Device Debug")

        try:
            result = subprocess.run(
                ['./gradlew', 'assembleDeviceDebug'],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            if result.returncode == 0:
                apk_path = self.config.get('paths.apk_device')
                if os.path.exists(apk_path):
                    apk_size = os.path.getsize(apk_path) / (1024 * 1024)
                    self.write_log("✅ Device Debug APK 編譯成功", "Success")
                    self.write_log(f"   📦 APK 大小: {apk_size:.2f} MB", "Info")
                    self.write_log(f"   📁 路徑: {apk_path}", "Info")
                    self.update_status("完成: Build Device Debug")
                    messagebox.showinfo("編譯完成", f"Device Debug APK 已編譯完成！\n\n大小: {apk_size:.2f} MB")
                else:
                    self.write_log("❌ APK 檔案未生成", "Error")
                    self.update_status("失敗: Build Device Debug")
            else:
                self.write_log(f"❌ 編譯失敗:\n{result.stderr[:500]}", "Error")
                self.update_status("失敗: Build Device Debug")
        except Exception as e:
            self.write_log(f"❌ 執行失敗: {str(e)}", "Error")
            self.update_status("失敗: Build Device Debug")

    def run_16kb_check_device(self):
        """Run 16KB check for Device APK"""
        self.write_log("🧪 開始執行 16KB 檢測 (Device APK)...", "Info")

        tool_path = self.config.get('paths.google_16kb_tool')
        apk_path = self.config.get('paths.apk_device')

        if not os.path.exists(apk_path):
            self.write_log(f"❌ APK 檔案不存在: {apk_path}", "Error")
            self.write_log("   請先執行 Build Device Debug", "Warning")
            return

        try:
            result = subprocess.run(
                ['bash', tool_path, apk_path],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            version = self.version_var.get()
            output_dir = Path(self.config.get('paths.test_results')) / version
            output_dir.mkdir(parents=True, exist_ok=True)
            output_file = output_dir / "16kb_check_device.log"

            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(result.stdout)

            if 'ELF Verification Successful' in result.stdout:
                self.write_log("✅ 16KB 兼容性檢測通過 (Device APK)", "Success")
                self.write_log(f"   📄 報告已儲存: {output_file}", "Info")
                messagebox.showinfo("16KB 檢測通過", "Device APK 16KB 兼容性檢測通過！")
            else:
                self.write_log("❌ 16KB 兼容性檢測失敗 (Device APK)", "Error")
                self.write_log(f"   📄 詳細報告: {output_file}", "Info")
                messagebox.showerror("16KB 檢測失敗", "Device APK 16KB 兼容性檢測失敗！")
        except Exception as e:
            self.write_log(f"❌ 執行失敗: {str(e)}", "Error")

    def launch_test_handoff(self):
        """Launch Test Handoff GUI"""
        self.write_log("🚀 啟動 Test Handoff GUI...", "Info")

        try:
            subprocess.Popen(
                ['python', 'tools/test_handoff_gui.pyw'],
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )
            self.write_log("✅ Test Handoff GUI 已啟動", "Success")
        except Exception as e:
            self.write_log(f"❌ 啟動失敗: {str(e)}", "Error")

    # ========== Settings ==========
    def save_config(self):
        """Save current configuration"""
        self.config.set('defaults.version', self.version_var.get())
        self.config.set('defaults.emulator', self.emulator_var.get())

        if self.config.save():
            self.write_log("💾 配置已儲存", "Success")
            messagebox.showinfo("儲存成功", "配置已儲存！")
        else:
            self.write_log("❌ 配置儲存失敗", "Error")
            messagebox.showerror("儲存失敗", "配置儲存失敗！")

    def open_config_file(self):
        """Open configuration file"""
        try:
            os.startfile(str(self.config.config_path))
            self.write_log(f"📄 開啟配置檔案: {self.config.config_path}", "Info")
        except Exception as e:
            self.write_log(f"❌ 無法開啟配置檔案: {str(e)}", "Error")

    def on_closing(self):
        """Handle window close event"""
        if self.logcat_process:
            self.logcat_process.terminate()
        self.root.destroy()


def main():
    """Main entry point"""
    root = tk.Tk()
    app = StageVerificationApp(root)
    root.protocol("WM_DELETE_WINDOW", app.on_closing)
    root.mainloop()


if __name__ == "__main__":
    main()
