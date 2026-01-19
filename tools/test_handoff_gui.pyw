#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
test_handoff_gui.pyw
Test Handoff Manager with Modern GUI
測試交接管理員 - 雙機測試交接流程自動化工具
"""

import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext
import subprocess
import os
import sys
import datetime
import json
from pathlib import Path
from typing import List, Optional, Tuple, Dict

# 設定預設編碼為 UTF-8
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')


class Colors:
    """Modern color scheme"""
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
    """Manage configuration for Test Handoff Tool"""

    DEFAULT_CONFIG = {
        "git_remotes": {
            "home_push": "github",
            "work_pull": "origin",
            "work_push": "origin",
            "home_pull": "origin"
        },
        "defaults": {
            "topic": "v2.9.6_phase1",
            "device": "Pixel 9a",
            "objective": "Verify OrientationEventListener fix and photo EXIF orientation"
        },
        "paths": {
            "test_handoff": "docs/test_handoff/",
            "test_results": "docs/test_results/"
        }
    }

    def __init__(self, config_path=".vscode/test_handoff_config.json"):
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


class GitRemoteDetector:
    """Detect and manage git remotes for intelligent remote selection"""

    def __init__(self):
        self.remotes = self.detect_remotes()

    def detect_remotes(self) -> Dict[str, str]:
        """Detect all available git remotes"""
        try:
            result = subprocess.run(
                ['git', 'remote', '-v'],
                capture_output=True,
                text=True,
                encoding='utf-8',
                errors='replace',
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            remotes = {}
            for line in result.stdout.strip().split('\n'):
                if line and '(push)' in line:
                    parts = line.split()
                    if len(parts) >= 2:
                        remotes[parts[0]] = parts[1]
            return remotes
        except Exception as e:
            print(f"Warning: Failed to detect git remotes: {e}")
            return {}

    def get_remote_for(self, operation: str) -> str:
        """Get the best remote for a given operation"""
        preference_map = {
            'home_push': ['github', 'origin'],
            'work_pull': ['origin', 'github'],
            'work_push': ['origin', 'github'],
            'home_pull': ['origin', 'github']
        }

        preferences = preference_map.get(operation, ['origin', 'github'])
        for remote in preferences:
            if remote in self.remotes:
                return remote

        if self.remotes:
            return list(self.remotes.keys())[0]
        return 'origin'


class GitErrorHandler:
    """Handle Git operation errors with friendly messages"""
    ERROR_PATTERNS = {
        'nothing to commit': {
            'user_message': '❌ No changes to commit',
            'suggestion': 'Please modify files before committing.',
            'recovery': 'skip'
        },
        'no changes added to commit': {
            'user_message': '❌ No changes staged for commit',
            'suggestion': 'Please stage files using git add before committing.',
            'recovery': 'skip'
        },
        'Authentication failed': {
            'user_message': '❌ Git authentication failed',
            'suggestion': 'Run: gh auth login\nOr check your Personal Access Token.',
            'recovery': 'retry'
        },
        'Connection refused': {
            'user_message': '❌ Network connection failed',
            'suggestion': 'Check your internet connection and try again.',
            'recovery': 'retry'
        },
        'CONFLICT': {
            'user_message': '❌ Merge conflict detected',
            'suggestion': 'Please resolve conflicts manually.',
            'recovery': 'manual'
        },
        'would be overwritten by merge': {
            'user_message': '❌ Local changes conflict with remote updates',
            'suggestion': (
                'Local files conflict with remote updates.\n\n'
                '【Quick Fix - Recommended】\n'
                '  git stash && git pull origin main\n\n'
                '【Or Manual Resolution】\n'
                '1. Check conflicting files in error message\n'
                '2. For .vscode/tasks.json: git restore .vscode/tasks.json\n'
                '3. For untracked files: Move or remove them\n'
                '4. Then retry pull\n\n'
                '【Nuclear Option - Use with Caution】\n'
                '  git reset --hard origin/main\n'
                '  (This discards ALL local changes!)'
            ),
            'recovery': 'manual'
        }
    }

    @staticmethod
    def handle_error(stderr: str, command: str = '') -> Dict[str, str]:
        """Analyze error message and return structured error information"""
        stderr_lower = stderr.lower()
        for pattern, info in GitErrorHandler.ERROR_PATTERNS.items():
            if pattern.lower() in stderr_lower:
                return info

        return {
            'user_message': '❌ Git operation failed',
            'suggestion': f'Error: {stderr}',
            'recovery': 'manual'
        }


class TestHandoffApp:
    """Test Handoff Manager GUI Application"""

    def __init__(self, root: tk.Tk):
        self.root = root
        self.config = ConfigManager()
        self.remote_detector = GitRemoteDetector()

        # Configure root window
        self.root.title("測試交接管理員")
        self.root.geometry("920x650")
        self.root.resizable(True, True)
        self.root.minsize(800, 550)
        self.root.configure(bg=Colors.BACKGROUND)

        # Setup GUI
        self.setup_ui()

        # Log startup info
        self.write_log("🚀 測試交接管理員已啟動", "Success")
        if self.remote_detector.remotes:
            for name, url in self.remote_detector.remotes.items():
                self.write_log(f"📡 Git Remote: {name} → {url}", "Info")

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
            text="🚀  測試交接管理員",
            font=("Microsoft JhengHei UI", 14, "bold"),
            fg=Colors.TEXT_LIGHT,
            bg=Colors.PRIMARY
        )
        title_label.pack(side=tk.LEFT, padx=20, pady=12)

        subtitle_label = tk.Label(
            header,
            text="Home NB ↔ Work NB  •  自動化測試交接流程",
            font=("Microsoft JhengHei UI", 9),
            fg=Colors.TEXT_LIGHT,
            bg=Colors.PRIMARY
        )
        subtitle_label.pack(side=tk.LEFT, padx=(0, 20), pady=12)

    def create_input_section(self):
        """Create input section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 📝 任務資訊 ",
            font=("Microsoft JhengHei UI", 10, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=15, pady=(8, 5), fill=tk.X)

        # Topic
        topic_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        topic_frame.pack(pady=(5, 3), padx=10, fill=tk.X)

        tk.Label(
            topic_frame,
            text="任務主題:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            width=12,
            anchor=tk.W
        ).pack(side=tk.LEFT)

        self.topic_var = tk.StringVar(value=self.config.get('defaults.topic', 'v2.9.6_phase1'))
        tk.Entry(
            topic_frame,
            textvariable=self.topic_var,
            font=("Consolas", 9),
            width=50
        ).pack(side=tk.LEFT, fill=tk.X, expand=True)

        # Device
        device_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        device_frame.pack(pady=3, padx=10, fill=tk.X)

        tk.Label(
            device_frame,
            text="測試裝置:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            width=12,
            anchor=tk.W
        ).pack(side=tk.LEFT)

        self.device_var = tk.StringVar(value=self.config.get('defaults.device', 'Pixel 9a'))
        device_combo = ttk.Combobox(
            device_frame,
            textvariable=self.device_var,
            values=('Pixel 9a', 'Sony XQ-BT52', 'Emulator', 'Generic Device'),
            width=48
        )
        device_combo.pack(side=tk.LEFT, fill=tk.X, expand=True)

        # Objective
        obj_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        obj_frame.pack(pady=(3, 8), padx=10, fill=tk.X)

        tk.Label(
            obj_frame,
            text="測試目標:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            width=12,
            anchor=tk.NW
        ).pack(side=tk.LEFT)

        self.objective_text = tk.Text(
            obj_frame,
            height=2,
            width=50,
            font=("Microsoft JhengHei UI", 9),
            relief=tk.SOLID,
            bd=1
        )
        default_obj = self.config.get('defaults.objective', 'Verify feature')
        self.objective_text.insert("1.0", default_obj)
        self.objective_text.pack(side=tk.LEFT, fill=tk.X, expand=True)

    def create_tabs_section(self):
        """Create tabs section"""
        self.notebook = ttk.Notebook(self.root)
        self.notebook.pack(padx=15, pady=5, fill=tk.BOTH, expand=True)

        self.create_step1_tab()
        self.create_step2_tab()
        self.create_step3_tab()
        self.create_step4_tab()
        self.create_settings_tab()

    def create_step1_tab(self):
        """Step 1: Home NB Push"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="1. 📤 Home (Push)")

        tk.Label(
            frame,
            text="步驟 1: 開發機推送測試計畫",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="將本地撰寫的測試文檔推送到 GitHub 供測試機下載。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        self.create_button(
            frame,
            "🔍 檢查 Git 狀態",
            Colors.BORDER,
            self.run_git_status,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "🚀 執行推送 (Commit & Push)",
            Colors.SUCCESS,
            self.run_step1
        ).pack(fill=tk.X, pady=5)

    def create_step2_tab(self):
        """Step 2: Work NB Pull"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="2. 📥 Work (Pull)")

        tk.Label(
            frame,
            text="步驟 2: 測試機拉取測試計畫",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="在測試機上下載最新的測試文檔並準備執行測試。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        self.create_button(
            frame,
            "⬇️ 拉取更新 (Git Pull)",
            Colors.PRIMARY,
            self.run_step2
        ).pack(fill=tk.X, pady=5)

    def create_step3_tab(self):
        """Step 3: Work NB Push"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="3. 📤 Work (Push)")

        tk.Label(
            frame,
            text="步驟 3: 測試機推送測試報告",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="測試完成後，將測試結果和報告推送到 GitHub。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        result_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        result_frame.pack(fill=tk.X, pady=(0, 10))

        tk.Label(
            result_frame,
            text="測試結果:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG
        ).pack(side=tk.LEFT, padx=(0, 10))

        self.result_var = tk.StringVar(value="PASS")
        ttk.Combobox(
            result_frame,
            textvariable=self.result_var,
            values=('PASS', 'FAIL', 'PARTIAL'),
            width=12
        ).pack(side=tk.LEFT)

        self.create_button(
            frame,
            "🔍 檢查 Git 狀態",
            Colors.BORDER,
            self.run_git_status,
            fg=Colors.TEXT_DARK
        ).pack(fill=tk.X, pady=5)

        self.create_button(
            frame,
            "🚀 推送報告 (Commit & Push)",
            Colors.SUCCESS,
            self.run_step3
        ).pack(fill=tk.X, pady=5)

    def create_step4_tab(self):
        """Step 4: Home NB Pull"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="4. 📥 Home (Pull)")

        tk.Label(
            frame,
            text="步驟 4: 開發機查看報告",
            font=("Microsoft JhengHei UI", 11, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK
        ).pack(anchor=tk.W, pady=(0, 5))

        tk.Label(
            frame,
            text="拉取測試結果並自動開啟最新的測試報告。",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack(anchor=tk.W, pady=(0, 15))

        self.create_button(
            frame,
            "⬇️ 拉取並開啟報告",
            Colors.PRIMARY,
            self.run_step4
        ).pack(fill=tk.X, pady=5)

    def create_settings_tab(self):
        """Settings tab"""
        frame = tk.Frame(self.notebook, bg=Colors.CARD_BG, padx=20, pady=15)
        self.notebook.add(frame, text="⚙️ Settings")

        # Git Remotes info
        remote_frame = tk.LabelFrame(
            frame,
            text=" Git Remote 資訊 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        remote_frame.pack(fill=tk.X, pady=(0, 10))

        if self.remote_detector.remotes:
            for name, url in self.remote_detector.remotes.items():
                tk.Label(
                    remote_frame,
                    text=f"  • {name}: {url}",
                    font=("Consolas", 8),
                    bg=Colors.CARD_BG,
                    fg=Colors.TEXT_DARK,
                    anchor=tk.W
                ).pack(padx=10, pady=2, fill=tk.X)
        else:
            tk.Label(
                remote_frame,
                text="  ⚠️ No git remotes detected",
                font=("Microsoft JhengHei UI", 9),
                bg=Colors.CARD_BG,
                fg=Colors.WARNING
            ).pack(padx=10, pady=5)

        # Default values
        defaults_frame = tk.LabelFrame(
            frame,
            text=" 預設值設定 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        defaults_frame.pack(fill=tk.X, pady=(0, 10))

        # Topic
        topic_row = tk.Frame(defaults_frame, bg=Colors.CARD_BG)
        topic_row.pack(fill=tk.X, padx=10, pady=5)
        tk.Label(topic_row, text="Topic:", bg=Colors.CARD_BG, width=10, anchor=tk.W).pack(side=tk.LEFT)
        self.settings_topic = tk.StringVar(value=self.config.get('defaults.topic', ''))
        tk.Entry(topic_row, textvariable=self.settings_topic, width=40).pack(side=tk.LEFT, fill=tk.X, expand=True)

        # Device
        device_row = tk.Frame(defaults_frame, bg=Colors.CARD_BG)
        device_row.pack(fill=tk.X, padx=10, pady=5)
        tk.Label(device_row, text="Device:", bg=Colors.CARD_BG, width=10, anchor=tk.W).pack(side=tk.LEFT)
        self.settings_device = tk.StringVar(value=self.config.get('defaults.device', ''))
        tk.Entry(device_row, textvariable=self.settings_device, width=40).pack(side=tk.LEFT, fill=tk.X, expand=True)

        # Objective
        tk.Label(defaults_frame, text="Objective:", bg=Colors.CARD_BG, anchor=tk.W).pack(padx=10, pady=(5, 0))
        self.settings_objective = tk.Text(defaults_frame, height=2, font=("Microsoft JhengHei UI", 9), relief=tk.SOLID, bd=1)
        self.settings_objective.insert("1.0", self.config.get('defaults.objective', ''))
        self.settings_objective.pack(padx=10, pady=(0, 10), fill=tk.X)

        # Buttons
        btn_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        btn_frame.pack(fill=tk.X, pady=10)

        self.create_button(
            btn_frame,
            "💾 儲存配置",
            Colors.SUCCESS,
            self.save_configuration,
            width=15
        ).pack(side=tk.LEFT, padx=5)

        self.create_button(
            btn_frame,
            "🔄 恢復預設值",
            Colors.WARNING,
            self.reset_configuration,
            width=15
        ).pack(side=tk.LEFT, padx=5)

    def create_log_section(self):
        """Create log section"""
        log_frame = tk.LabelFrame(
            self.root,
            text=" 📜 執行日誌 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        log_frame.pack(padx=15, pady=(0, 5), fill=tk.BOTH, expand=True)

        # Log controls
        controls = tk.Frame(log_frame, bg=Colors.CARD_BG)
        controls.pack(fill=tk.X, padx=10, pady=(5, 5))

        self.create_button(
            controls,
            "🗑️ 清除",
            Colors.BORDER,
            self.clear_log,
            width=10,
            fg=Colors.TEXT_DARK
        ).pack(side=tk.LEFT, padx=2)

        self.create_button(
            controls,
            "💾 儲存",
            Colors.BORDER,
            self.save_log,
            width=10,
            fg=Colors.TEXT_DARK
        ).pack(side=tk.LEFT, padx=2)

        # Log area
        self.log_text = scrolledtext.ScrolledText(
            log_frame,
            font=("Consolas", 9),
            bg="#fafafa",
            relief=tk.FLAT,
            height=8
        )
        self.log_text.pack(padx=10, pady=(0, 10), fill=tk.BOTH, expand=True)
        self.log_text.config(state=tk.DISABLED)

    def create_statusbar(self):
        """Create status bar"""
        self.status_var = tk.StringVar(value="就緒")
        statusbar = tk.Label(
            self.root,
            textvariable=self.status_var,
            font=("Microsoft JhengHei UI", 8),
            bg=Colors.BACKGROUND,
            fg=Colors.TEXT_DARK,
            anchor=tk.W,
            relief=tk.SUNKEN,
            bd=1
        )
        statusbar.pack(side=tk.BOTTOM, fill=tk.X)

    def create_button(self, parent, text, bg_color, command, width=None, fg=Colors.TEXT_LIGHT):
        """Create styled button with hover effect"""
        btn = tk.Button(
            parent,
            text=text,
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=bg_color,
            fg=fg,
            activebackground=bg_color,
            activeforeground=fg,
            relief=tk.FLAT,
            cursor="hand2",
            command=command,
            width=width if width else 30,
            height=1
        )

        def on_enter(e):
            btn['bg'] = self.darken_color(bg_color)

        def on_leave(e):
            btn['bg'] = bg_color

        btn.bind("<Enter>", on_enter)
        btn.bind("<Leave>", on_leave)

        return btn

    @staticmethod
    def darken_color(hex_color):
        """Darken hex color by 10%"""
        hex_color = hex_color.lstrip('#')
        r, g, b = tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))
        r, g, b = int(r * 0.85), int(g * 0.85), int(b * 0.85)
        return f'#{r:02x}{g:02x}{b:02x}'

    def write_log(self, message: str, log_type: str = "Info"):
        """Write log message"""
        timestamp = datetime.datetime.now().strftime("%H:%M:%S")

        prefix = {
            "Success": "[✓]",
            "Warning": "[!]",
            "Error": "[✗]",
            "Info": "[i]"
        }.get(log_type, "[i]")

        log_message = f"[{timestamp}] {prefix} {message}\n"

        self.log_text.config(state=tk.NORMAL)
        self.log_text.insert(tk.END, log_message)
        self.log_text.see(tk.END)
        self.log_text.config(state=tk.DISABLED)
        self.root.update_idletasks()

    def clear_log(self):
        """Clear log"""
        self.log_text.config(state=tk.NORMAL)
        self.log_text.delete(1.0, tk.END)
        self.log_text.config(state=tk.DISABLED)
        self.write_log("日誌已清除", "Info")

    def save_log(self):
        """Save log to file"""
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")

        # Create tmp directory if it doesn't exist
        tmp_dir = Path("tmp")
        tmp_dir.mkdir(exist_ok=True)

        log_filename = tmp_dir / f"test_handoff_log_{timestamp}.txt"

        try:
            with open(log_filename, 'w', encoding='utf-8') as f:
                log_content = self.log_text.get('1.0', tk.END)
                f.write(log_content)
            messagebox.showinfo("成功", f"日誌已儲存至:\n{log_filename}")
            self.write_log(f"日誌已儲存: {log_filename}", "Success")
        except Exception as e:
            messagebox.showerror("錯誤", f"儲存日誌失敗:\n{str(e)}")

    def save_configuration(self):
        """Save configuration"""
        self.config.set('defaults.topic', self.settings_topic.get())
        self.config.set('defaults.device', self.settings_device.get())
        self.config.set('defaults.objective', self.settings_objective.get("1.0", tk.END).strip())

        if self.config.save():
            messagebox.showinfo("成功", f"配置已儲存至:\n{self.config.config_path}\n\n下次啟動時將使用這些預設值。")
            self.topic_var.set(self.settings_topic.get())
            self.device_var.set(self.settings_device.get())
            self.objective_text.delete("1.0", tk.END)
            self.objective_text.insert("1.0", self.settings_objective.get("1.0", tk.END).strip())
        else:
            messagebox.showerror("錯誤", "儲存配置失敗，請檢查檔案權限。")

    def reset_configuration(self):
        """Reset configuration to defaults"""
        if messagebox.askyesno("確認", "確定要恢復預設值嗎？"):
            self.config.config = ConfigManager.DEFAULT_CONFIG.copy()
            self.settings_topic.set(self.config.get('defaults.topic', ''))
            self.settings_device.set(self.config.get('defaults.device', ''))
            self.settings_objective.delete("1.0", tk.END)
            self.settings_objective.insert("1.0", self.config.get('defaults.objective', ''))
            messagebox.showinfo("成功", "已恢復預設值。\n點擊「儲存配置」以保存變更。")

    def run_command(self, commands: List[str], show_output: bool = True) -> Tuple[int, str, str]:
        """Execute shell command and capture output"""
        try:
            process = subprocess.Popen(
                commands,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                encoding='utf-8',
                errors='replace',
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )

            stdout_lines = []
            stderr_lines = []

            while True:
                output = process.stdout.readline()
                if output == '' and process.poll() is not None:
                    break
                if output:
                    line = output.strip()
                    stdout_lines.append(line)
                    if show_output:
                        self.write_log(line, "Info")

            stderr_output = process.stderr.read()
            if stderr_output:
                stderr_lines = stderr_output.strip().split('\n')

            returncode = process.poll()
            stdout = '\n'.join(stdout_lines)
            stderr = '\n'.join(stderr_lines)

            return returncode, stdout, stderr

        except Exception as e:
            self.write_log(f"執行錯誤: {str(e)}", "Error")
            return -1, '', str(e)

    def handle_git_error(self, command: str, returncode: int, stderr: str) -> bool:
        """Handle git command errors"""
        if returncode == 0:
            return False

        error_info = GitErrorHandler.handle_error(stderr, command)
        self.write_log(error_info['user_message'], "Error")
        self.write_log(error_info['suggestion'], "Warning")

        if error_info['recovery'] in ['abort', 'manual']:
            messagebox.showerror('操作失敗', error_info['user_message'] + '\n\n' + error_info['suggestion'])
        elif error_info['recovery'] == 'skip':
            messagebox.showwarning('注意', error_info['user_message'] + '\n\n' + error_info['suggestion'])

        return True

    def run_git_status(self):
        """Run git status"""
        self.write_log("Checking Git Status...", "Info")
        self.status_var.set("執行中...")
        returncode, stdout, stderr = self.run_command(["git", "status", "--short"])
        self.handle_git_error("git status", returncode, stderr)
        self.status_var.set("就緒")

    def run_step1(self):
        """Step 1: Home NB Push"""
        if not messagebox.askyesno("確認", "確定要推送到 GitHub (main) 嗎？"):
            return

        topic = self.topic_var.get()
        device = self.device_var.get()
        objective = self.objective_text.get("1.0", tk.END).strip()

        self.write_log("=== Task 1: Home NB (Push) ===", "Info")
        self.status_var.set("推送中...")

        # Git Add (use -A to add all changes from repo root)
        self.write_log("Adding files...", "Info")
        returncode, stdout, stderr = self.run_command(["git", "add", "-A"])
        if self.handle_git_error("git add", returncode, stderr):
            self.status_var.set("失敗")
            return

        # Git Commit
        self.write_log("Committing...", "Info")
        commit_msg = [
            "git", "commit",
            "-m", f"[Docs] Test handoff: {topic}",
            "-m", f"Test Device: {device}",
            "-m", f"Test Objective: {objective}",
            "-m", "",
            "-m", "Co-Authored-By: Claude Sonnet 4.5 (noreply@anthropic.com)"
        ]
        returncode, stdout, stderr = self.run_command(commit_msg)
        if self.handle_git_error("git commit", returncode, stderr):
            # Skip if no changes - allow both error message variants
            if 'nothing to commit' not in stderr.lower() and 'no changes added to commit' not in stderr.lower():
                self.status_var.set("失敗")
                return

        # Git Push
        remote = self.remote_detector.get_remote_for('home_push')
        self.write_log(f"Pushing to {remote}...", "Info")
        returncode, stdout, stderr = self.run_command(["git", "push", remote, "main"])

        if self.handle_git_error("git push", returncode, stderr):
            self.status_var.set("失敗")
        else:
            self.write_log("推送成功！", "Success")
            self.status_var.set("完成")
            messagebox.showinfo("成功", "推送成功！\n\n下一步：切換到 Work NB 執行步驟 2")

    def run_step2(self):
        """Step 2: Work NB Pull"""
        self.write_log("=== Task 2: Work NB (Pull) ===", "Info")
        self.status_var.set("拉取中...")

        remote = self.remote_detector.get_remote_for('work_pull')
        self.write_log(f"Pulling from {remote}...", "Info")
        returncode, stdout, stderr = self.run_command(["git", "pull", remote, "main"])

        if self.handle_git_error("git pull", returncode, stderr):
            self.status_var.set("失敗")
        else:
            self.write_log("同步成功！", "Success")
            self.status_var.set("完成")
            messagebox.showinfo("成功", "同步完成！\n\n下一步：執行測試")

    def run_step3(self):
        """Step 3: Work NB Push"""
        if not messagebox.askyesno("確認", "確定要推送測試結果嗎？"):
            return

        topic = self.topic_var.get()
        device = self.device_var.get()
        result = self.result_var.get()
        date_str = datetime.datetime.now().strftime("%Y-%m-%d")

        self.write_log("=== Task 3: Work NB (Push) ===", "Info")
        self.status_var.set("推送中...")

        # Git Add (use -A to add all changes from repo root)
        self.write_log("Adding test results...", "Info")
        returncode, stdout, stderr = self.run_command(["git", "add", "-A"])
        if self.handle_git_error("git add", returncode, stderr):
            self.status_var.set("失敗")
            return

        # Git Commit
        self.write_log("Committing...", "Info")
        commit_msg = [
            "git", "commit",
            "-m", f"[Test] {topic}: Test results - {result}",
            "-m", f"Device: {device}",
            "-m", "Tester: Claude Code (Work NB)",
            "-m", f"Date: {date_str}",
            "-m", "",
            "-m", "Co-Authored-By: Claude Sonnet 4.5 (noreply@anthropic.com)"
        ]
        returncode, stdout, stderr = self.run_command(commit_msg)
        if self.handle_git_error("git commit", returncode, stderr):
            # Skip if no changes - allow both error message variants
            if 'nothing to commit' not in stderr.lower() and 'no changes added to commit' not in stderr.lower():
                self.status_var.set("失敗")
                return

        # Git Push
        remote = self.remote_detector.get_remote_for('work_push')
        self.write_log(f"Pushing to {remote}...", "Info")
        returncode, stdout, stderr = self.run_command(["git", "push", remote, "main"])

        if self.handle_git_error("git push", returncode, stderr):
            self.status_var.set("失敗")
        else:
            self.write_log("報告推送成功！", "Success")
            self.status_var.set("完成")
            messagebox.showinfo("成功", "報告已上傳！\n\n下一步：切換到 Home NB 執行步驟 4")

    def run_step4(self):
        """Step 4: Home NB Pull"""
        self.write_log("=== Task 4: Home NB (Pull) ===", "Info")
        self.status_var.set("拉取中...")

        remote = self.remote_detector.get_remote_for('home_pull')
        self.write_log(f"Pulling from {remote}...", "Info")
        returncode, stdout, stderr = self.run_command(["git", "pull", remote, "main"])

        if self.handle_git_error("git pull", returncode, stderr):
            self.status_var.set("失敗")
            return

        self.write_log("Opening latest report...", "Info")
        cmd = "Get-ChildItem docs/test_results/*.md -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1 | ForEach-Object { Write-Host 'Opening: ' $_.FullName; code $_.FullName }"
        self.run_command(["powershell", "-Command", cmd])

        self.write_log("測試交接循環完成！", "Success")
        self.status_var.set("完成")


def main():
    """Main entry point"""
    root = tk.Tk()
    app = TestHandoffApp(root)
    root.mainloop()


if __name__ == "__main__":
    main()
