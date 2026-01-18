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
sys.stdout.reconfigure(encoding='utf-8')


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
                # Merge with defaults to ensure all keys exist
                config = self.DEFAULT_CONFIG.copy()
                self._deep_update(config, loaded_config)
                return config
            except Exception as e:
                print(f"Warning: Failed to load config from {self.config_path}: {e}")
                print("Using default configuration")
                return self.DEFAULT_CONFIG.copy()
        else:
            print(f"No config file found at {self.config_path}, using defaults")
            return self.DEFAULT_CONFIG.copy()

    def save(self):
        """Save current configuration to file"""
        try:
            self.config_path.parent.mkdir(parents=True, exist_ok=True)
            with open(self.config_path, 'w', encoding='utf-8') as f:
                json.dump(self.config, f, indent=2, ensure_ascii=False)
            print(f"✅ Configuration saved to {self.config_path}")
            return True
        except Exception as e:
            print(f"❌ Failed to save configuration: {e}")
            return False

    def get(self, key_path: str, default=None):
        """
        Get configuration value using dot notation

        Args:
            key_path: Path to key (e.g., 'git_remotes.home_push' or 'defaults.topic')
            default: Default value if key not found

        Returns:
            Configuration value
        """
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
        """
        Set configuration value using dot notation

        Args:
            key_path: Path to key (e.g., 'defaults.topic')
            value: Value to set
        """
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
        """
        Detect all available git remotes

        Returns:
            Dictionary mapping remote names to URLs
            Example: {'github': 'https://...', 'origin': 'D:\\...'}
        """
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
        """
        Get the best remote for a given operation

        Args:
            operation: One of 'home_push', 'work_pull', 'work_push', 'home_pull'

        Returns:
            Remote name (e.g., 'github', 'origin')
        """
        # Preference map for each operation
        preference_map = {
            'home_push': ['github', 'origin'],    # Home prefers github for push
            'work_pull': ['origin', 'github'],    # Work prefers origin for pull
            'work_push': ['origin', 'github'],    # Work prefers origin for push
            'home_pull': ['origin', 'github']     # Home prefers origin for pull
        }

        # Get preferences for this operation
        preferences = preference_map.get(operation, ['origin', 'github'])

        # Return first available remote from preferences
        for remote in preferences:
            if remote in self.remotes:
                return remote

        # Fallback: return first available remote
        if self.remotes:
            return list(self.remotes.keys())[0]

        # Ultimate fallback
        return 'origin'

    def get_remote_url(self, remote_name: str) -> Optional[str]:
        """Get URL for a specific remote"""
        return self.remotes.get(remote_name)


class GitErrorHandler:
    """Handle Git operation errors with friendly messages and recovery suggestions"""
    ERROR_PATTERNS = {
        'nothing to commit': {
            'user_message': '❌ No changes to commit',
            'suggestion': 'Please modify files in the target directory before committing.',
            'recovery': 'skip'
        },
        'nothing added to commit': {
            'user_message': '❌ No files added to commit',
            'suggestion': 'Add files to the staging area before committing.',
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
        'Connection timed out': {
            'user_message': '❌ Network connection timed out',
            'suggestion': 'Check your internet connection and try again.',
            'recovery': 'retry'
        },
        'fatal: not a git repository': {
            'user_message': '❌ Not a git repository',
            'suggestion': 'Run this tool from within a git repository.',
            'recovery': 'abort'
        },
        'error: failed to push': {
            'user_message': '❌ Push failed',
            'suggestion': 'Check your network, authentication, and branch permissions.',
            'recovery': 'retry'
        },
        'CONFLICT': {
            'user_message': '❌ Merge conflict detected',
            'suggestion': 'Please resolve conflicts manually:\n1. Run: git status\n2. Edit conflicted files\n3. Run: git add .\n4. Run: git commit',
            'recovery': 'manual'
        },
        'Permission denied': {
            'user_message': '❌ Permission denied',
            'suggestion': 'Check file permissions or run with appropriate privileges.',
            'recovery': 'manual'
        }
    }

    @staticmethod
    def handle_error(stderr: str, command: str = '') -> Dict[str, str]:
        """
        Analyze error message and return structured error information

        Args:
            stderr: Error output from git command
            command: The git command that failed (for context)

        Returns:
            Dictionary with user_message, suggestion, and recovery strategy
        """
        stderr_lower = stderr.lower()

        for pattern, info in GitErrorHandler.ERROR_PATTERNS.items():
            if pattern.lower() in stderr_lower:
                return info

        # Default error handling
        return {
            'user_message': f'❌ Git operation failed',
            'suggestion': f'Error details:\n{stderr}\n\nCommand: {command}',
            'recovery': 'manual'
        }

class TestHandoffApp:
    def __init__(self, root):
        self.root = root
        self.root.title("測試交接管理員 (Test Handoff Manager)")
        self.root.geometry("900x750")

        # 初始化配置管理器
        self.config = ConfigManager()

        # 初始化 Git Remote 檢測器
        self.remote_detector = GitRemoteDetector()
        self.log_remote_info()

        # 設定樣式
        self.style = ttk.Style()
        self.style.configure("Title.TLabel", font=("Microsoft JhengHei", 14, "bold"))
        self.style.configure("Header.TLabel", font=("Microsoft JhengHei", 10, "bold"))
        self.style.configure("TButton", font=("Microsoft JhengHei", 10))
        self.style.configure("TLabel", font=("Microsoft JhengHei", 10))

        self.create_widgets()

    def log_remote_info(self):
        """Log detected git remotes at startup"""
        print(f"📁 Config file: {self.config.config_path}")
        if self.remote_detector.remotes:
            print("📡 Detected Git Remotes:")
            for name, url in self.remote_detector.remotes.items():
                print(f"  {name}: {url}")
        else:
            print("⚠️ Warning: No git remotes detected")
        
    def create_widgets(self):
        # 主容器
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.pack(fill=tk.BOTH, expand=True)

        # 標題
        title_label = ttk.Label(main_frame, text="🚀 雙機測試交接流程管理", style="Title.TLabel")
        title_label.pack(pady=(0, 15))

        # 輸入區域容器
        input_frame = ttk.LabelFrame(main_frame, text="📝 任務資訊", padding="10")
        input_frame.pack(fill=tk.X, pady=(0, 10))

        # 任務主題 (Topic)
        ttk.Label(input_frame, text="任務主題 (Topic):").grid(row=0, column=0, sticky=tk.W, padx=5, pady=5)
        self.topic_var = tk.StringVar(value=self.config.get('defaults.topic', 'v2.9.6_phase1'))
        ttk.Entry(input_frame, textvariable=self.topic_var, width=40).grid(row=0, column=1, sticky=tk.W, padx=5, pady=5)

        # 測試裝置 (Device)
        ttk.Label(input_frame, text="測試裝置 (Device):").grid(row=1, column=0, sticky=tk.W, padx=5, pady=5)
        self.device_var = tk.StringVar(value=self.config.get('defaults.device', 'Pixel 9a'))
        device_combo = ttk.Combobox(input_frame, textvariable=self.device_var, width=37)
        device_combo['values'] = ('Pixel 9a', 'Sony XQ-BT52', 'Emulator', 'Generic Device')
        device_combo.grid(row=1, column=1, sticky=tk.W, padx=5, pady=5)

        # 測試目標 (Objective)
        ttk.Label(input_frame, text="測試目標/結果說明:").grid(row=2, column=0, sticky=tk.NW, padx=5, pady=5)
        self.objective_text = tk.Text(input_frame, height=3, width=40, font=("Microsoft JhengHei", 9))
        default_objective = self.config.get('defaults.objective', 'Verify OrientationEventListener fix')
        self.objective_text.insert("1.0", default_objective)
        self.objective_text.grid(row=2, column=1, sticky=tk.W, padx=5, pady=5)

        # 頁籤區域 (主要操作)
        self.notebook = ttk.Notebook(main_frame)
        self.notebook.pack(fill=tk.BOTH, expand=True, pady=10)

        # 建立五個分頁
        self.create_step1_tab()
        self.create_step2_tab()
        self.create_step3_tab()
        self.create_step4_tab()
        self.create_settings_tab()

        # Log 區域
        log_frame = ttk.LabelFrame(main_frame, text="📜 執行日誌 (Logs)", padding="5")
        log_frame.pack(fill=tk.BOTH, expand=True)

        # Log 控制按鈕
        log_controls = ttk.Frame(log_frame)
        log_controls.pack(fill=tk.X, pady=(0, 5))

        btn_clear_log = ttk.Button(log_controls, text="🗑️ 清除日誌", command=self.clear_log)
        btn_clear_log.pack(side=tk.LEFT, padx=5)

        btn_save_log = ttk.Button(log_controls, text="💾 儲存日誌", command=self.save_log)
        btn_save_log.pack(side=tk.LEFT, padx=5)

        self.log_area = scrolledtext.ScrolledText(log_frame, height=10, state='disabled', font=("Consolas", 9))
        self.log_area.pack(fill=tk.BOTH, expand=True)

    def clear_log(self):
        """Clear the log area"""
        self.log_area.config(state='normal')
        self.log_area.delete('1.0', tk.END)
        self.log_area.config(state='disabled')
        self.log("📜 日誌已清除", add_timestamp=True)

    def save_log(self):
        """Save log to file"""
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        log_filename = f"test_handoff_log_{timestamp}.txt"

        try:
            with open(log_filename, 'w', encoding='utf-8') as f:
                log_content = self.log_area.get('1.0', tk.END)
                f.write(log_content)
            messagebox.showinfo("成功", f"日誌已儲存至:\n{log_filename}")
            self.log(f"✅ 日誌已儲存: {log_filename}", add_timestamp=True)
        except Exception as e:
            messagebox.showerror("錯誤", f"儲存日誌失敗:\n{str(e)}")

    def create_step1_tab(self):
        frame = ttk.Frame(self.notebook, padding="15")
        self.notebook.add(frame, text="1. 📤 Home NB (Push)")

        ttk.Label(frame, text="步驟 1: 開發機推送測試計畫", style="Header.TLabel").pack(anchor=tk.W)
        ttk.Label(frame, text="將本地撰寫的測試文檔推送到 GitHub 供測試機下載。").pack(anchor=tk.W, pady=(5, 15))

        btn_check = ttk.Button(frame, text="🔍 檢查 Git 狀態", command=lambda: self.run_git_status())
        btn_check.pack(fill=tk.X, pady=5)

        btn_push = ttk.Button(frame, text="🚀 執行推送 (Commit & Push)", command=self.run_step1)
        btn_push.pack(fill=tk.X, pady=5)

    def create_step2_tab(self):
        frame = ttk.Frame(self.notebook, padding="15")
        self.notebook.add(frame, text="2. 📥 Work NB (Pull)")

        ttk.Label(frame, text="步驟 2: 測試機拉取測試計畫", style="Header.TLabel").pack(anchor=tk.W)
        ttk.Label(frame, text="在測試機上下載最新的測試文檔並準備執行測試。").pack(anchor=tk.W, pady=(5, 15))

        btn_pull = ttk.Button(frame, text="⬇️ 拉取更新 (Git Pull)", command=self.run_step2)
        btn_pull.pack(fill=tk.X, pady=5)

    def create_step3_tab(self):
        frame = ttk.Frame(self.notebook, padding="15")
        self.notebook.add(frame, text="3. 📤 Work NB (Push)")

        ttk.Label(frame, text="步驟 3: 測試機推送測試報告", style="Header.TLabel").pack(anchor=tk.W)
        ttk.Label(frame, text="測試完成後，將測試結果和報告推送到 GitHub。").pack(anchor=tk.W, pady=(5, 15))

        ttk.Label(frame, text="測試結果 (PASS/FAIL):").pack(anchor=tk.W)
        self.result_var = tk.StringVar(value="PASS")
        ttk.Combobox(frame, textvariable=self.result_var, values=('PASS', 'FAIL', 'PARTIAL'), width=10).pack(anchor=tk.W, pady=(0, 10))

        btn_check = ttk.Button(frame, text="🔍 檢查 Git 狀態", command=lambda: self.run_git_status())
        btn_check.pack(fill=tk.X, pady=5)

        btn_push = ttk.Button(frame, text="🚀 推送報告 (Commit & Push)", command=self.run_step3)
        btn_push.pack(fill=tk.X, pady=5)

    def create_step4_tab(self):
        frame = ttk.Frame(self.notebook, padding="15")
        self.notebook.add(frame, text="4. 📥 Home NB (Pull)")

        ttk.Label(frame, text="步驟 4: 開發機查看報告", style="Header.TLabel").pack(anchor=tk.W)
        ttk.Label(frame, text="拉取測試結果並自動開啟最新的測試報告。").pack(anchor=tk.W, pady=(5, 15))

        btn_pull = ttk.Button(frame, text="⬇️ 拉取並開啟報告", command=self.run_step4)
        btn_pull.pack(fill=tk.X, pady=5)

    def create_settings_tab(self):
        frame = ttk.Frame(self.notebook, padding="15")
        self.notebook.add(frame, text="⚙️ Settings")

        ttk.Label(frame, text="配置設定", style="Header.TLabel").pack(anchor=tk.W, pady=(0, 10))

        # Git Remote Settings
        remote_frame = ttk.LabelFrame(frame, text="Git Remote 設定", padding="10")
        remote_frame.pack(fill=tk.X, pady=(0, 10))

        # Display detected remotes
        ttk.Label(remote_frame, text="已檢測到的 Remotes:").pack(anchor=tk.W, pady=(0, 5))
        for name, url in self.remote_detector.remotes.items():
            ttk.Label(remote_frame, text=f"  • {name}: {url}", font=("Consolas", 9)).pack(anchor=tk.W)

        # Default Values Settings
        defaults_frame = ttk.LabelFrame(frame, text="預設值設定", padding="10")
        defaults_frame.pack(fill=tk.X, pady=(0, 10))

        # Topic default
        topic_row = ttk.Frame(defaults_frame)
        topic_row.pack(fill=tk.X, pady=5)
        ttk.Label(topic_row, text="Topic:").pack(side=tk.LEFT, padx=(0, 10))
        self.settings_topic = tk.StringVar(value=self.config.get('defaults.topic', ''))
        ttk.Entry(topic_row, textvariable=self.settings_topic, width=30).pack(side=tk.LEFT)

        # Device default
        device_row = ttk.Frame(defaults_frame)
        device_row.pack(fill=tk.X, pady=5)
        ttk.Label(device_row, text="Device:").pack(side=tk.LEFT, padx=(0, 10))
        self.settings_device = tk.StringVar(value=self.config.get('defaults.device', ''))
        ttk.Entry(device_row, textvariable=self.settings_device, width=30).pack(side=tk.LEFT)

        # Objective default
        objective_row = ttk.Frame(defaults_frame)
        objective_row.pack(fill=tk.X, pady=5)
        ttk.Label(objective_row, text="Objective:").pack(anchor=tk.W)
        self.settings_objective = scrolledtext.ScrolledText(defaults_frame, height=3, width=50, font=("Microsoft JhengHei", 9))
        self.settings_objective.insert("1.0", self.config.get('defaults.objective', ''))
        self.settings_objective.pack(fill=tk.X, pady=(0, 5))

        # Save buttons
        button_frame = ttk.Frame(frame)
        button_frame.pack(fill=tk.X, pady=10)

        btn_save = ttk.Button(button_frame, text="💾 儲存配置", command=self.save_configuration)
        btn_save.pack(side=tk.LEFT, padx=(0, 10))

        btn_reset = ttk.Button(button_frame, text="🔄 恢復預設值", command=self.reset_configuration)
        btn_reset.pack(side=tk.LEFT)

        # Config file info
        info_frame = ttk.LabelFrame(frame, text="配置檔資訊", padding="10")
        info_frame.pack(fill=tk.X)
        ttk.Label(info_frame, text=f"配置檔位置: {self.config.config_path}").pack(anchor=tk.W)
        exists_text = "✅ 存在" if self.config.config_path.exists() else "❌ 不存在（使用預設值）"
        ttk.Label(info_frame, text=f"狀態: {exists_text}").pack(anchor=tk.W)

    def save_configuration(self):
        """Save current settings to configuration file"""
        # Update config with current values
        self.config.set('defaults.topic', self.settings_topic.get())
        self.config.set('defaults.device', self.settings_device.get())
        self.config.set('defaults.objective', self.settings_objective.get("1.0", tk.END).strip())

        # Save to file
        if self.config.save():
            messagebox.showinfo("成功", f"配置已儲存至:\n{self.config.config_path}\n\n下次啟動時將使用這些預設值。")
            # Update main form fields
            self.topic_var.set(self.settings_topic.get())
            self.device_var.set(self.settings_device.get())
            self.objective_text.delete("1.0", tk.END)
            self.objective_text.insert("1.0", self.settings_objective.get("1.0", tk.END).strip())
        else:
            messagebox.showerror("錯誤", "儲存配置失敗，請檢查檔案權限。")

    def reset_configuration(self):
        """Reset configuration to defaults"""
        if messagebox.askyesno("確認", "確定要恢復預設值嗎？\n這將清除所有自訂設定。"):
            self.config.config = ConfigManager.DEFAULT_CONFIG.copy()
            # Update settings fields
            self.settings_topic.set(self.config.get('defaults.topic', ''))
            self.settings_device.set(self.config.get('defaults.device', ''))
            self.settings_objective.delete("1.0", tk.END)
            self.settings_objective.insert("1.0", self.config.get('defaults.objective', ''))
            messagebox.showinfo("成功", "已恢復預設值。\n點擊「儲存配置」以保存變更。")

    def log(self, message, add_timestamp=False):
        """
        Add a log message to the log area

        Args:
            message: The message to log
            add_timestamp: Whether to prepend a timestamp
        """
        self.log_area.config(state='normal')

        if add_timestamp:
            timestamp = datetime.datetime.now().strftime("%H:%M:%S")
            formatted_message = f"[{timestamp}] {message}\n"
        else:
            formatted_message = f"{message}\n"

        self.log_area.insert(tk.END, formatted_message)
        self.log_area.see(tk.END)
        self.log_area.config(state='disabled')
        self.root.update_idletasks()

    def run_command(self, commands: List[str], show_output: bool = True) -> Tuple[int, str, str]:
        """
        Execute a shell command and capture output

        Args:
            commands: Command and arguments as list
            show_output: Whether to log output in real-time

        Returns:
            Tuple of (returncode, stdout, stderr)
        """
        try:
            # 確保使用 UTF-8
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

            # Read stdout in real-time
            while True:
                output = process.stdout.readline()
                if output == '' and process.poll() is not None:
                    break
                if output:
                    line = output.strip()
                    stdout_lines.append(line)
                    if show_output:
                        self.log(line)

            # Read any remaining stderr
            stderr_output = process.stderr.read()
            if stderr_output:
                stderr_lines = stderr_output.strip().split('\n')

            returncode = process.poll()
            stdout = '\n'.join(stdout_lines)
            stderr = '\n'.join(stderr_lines)

            return returncode, stdout, stderr

        except Exception as e:
            error_msg = f"❌ 執行錯誤: {str(e)}"
            self.log(error_msg)
            return -1, '', str(e)

    def handle_git_error(self, command: str, returncode: int, stderr: str) -> bool:
        """
        Handle git command errors with friendly messages

        Args:
            command: The git command that was executed
            returncode: Return code from the command
            stderr: Error output

        Returns:
            True if error was handled, False if operation succeeded
        """
        if returncode == 0:
            return False

        # Get error information from handler
        error_info = GitErrorHandler.handle_error(stderr, command)

        # Display error message
        self.log('')
        self.log(error_info['user_message'])
        self.log(error_info['suggestion'])

        # Show message box for critical errors
        if error_info['recovery'] in ['abort', 'manual']:
            messagebox.showerror('操作失敗', error_info['user_message'] + '\n\n' + error_info['suggestion'])
        elif error_info['recovery'] == 'skip':
            messagebox.showwarning('注意', error_info['user_message'] + '\n\n' + error_info['suggestion'])

        return True

    def run_git_status(self):
        self.log("\n--- 🔍 Checking Git Status ---", add_timestamp=True)
        returncode, stdout, stderr = self.run_command(["git", "status", "--short"])
        if self.handle_git_error("git status", returncode, stderr):
            return False
        return True

    def run_step1(self):
        if not messagebox.askyesno("確認", "確定要推送到 GitHub (main) 嗎？"):
            return

        topic = self.topic_var.get()
        device = self.device_var.get()
        objective = self.objective_text.get("1.0", tk.END).strip()

        self.log(f"\n--- 🚀 Starting Task 1: Home NB (Push) ---", add_timestamp=True)

        # Git Add
        self.log("Step 1: Adding files...")
        returncode, stdout, stderr = self.run_command(["git", "add", "docs/test_handoff/"])
        if self.handle_git_error("git add", returncode, stderr):
            self.log("\n❌ 添加檔案失敗，操作中止。")
            return

        # Git Commit
        self.log("Step 2: Committing...")
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
            # Check if it's just "nothing to commit" - this is not critical
            if 'nothing to commit' in stderr.lower():
                self.log("\n⚠️ 沒有變更可提交，跳過推送步驟。")
                return
            else:
                self.log("\n❌ 提交失敗，操作中止。")
                return

        # Git Push
        remote = self.remote_detector.get_remote_for('home_push')
        self.log(f"Step 3: Pushing to {remote} (main)...")
        returncode, stdout, stderr = self.run_command(["git", "push", remote, "main"])
        if self.handle_git_error("git push", returncode, stderr):
            self.log("\n❌ 推送失敗，請檢查日誌。")
        else:
            self.log("\n✅ 成功！請切換到測試機執行步驟 2。")
            messagebox.showinfo("成功", "推送成功！\n\n下一步：請切換到 Work NB 執行步驟 2 (Pull)。")

    def run_step2(self):
        self.log(f"\n--- 📥 Starting Task 2: Work NB (Pull) ---", add_timestamp=True)

        # Git Pull
        remote = self.remote_detector.get_remote_for('work_pull')
        self.log(f"Step 1: Pulling from {remote} (main)...")
        returncode, stdout, stderr = self.run_command(["git", "pull", remote, "main"])
        if self.handle_git_error("git pull", returncode, stderr):
            self.log("\n❌ 同步失敗。")
            return

        self.log("\n✅ 同步成功！可用測試文檔：")

        # List available test docs
        self.log("Step 2: Listing test handoff documents...")
        returncode, stdout, stderr = self.run_command(["powershell", "-Command", "Get-ChildItem docs/test_handoff/*.md | Select-Object Name"])

        messagebox.showinfo("成功", "同步完成！\n\n下一步：請叫出 Claude Code 執行測試。")

    def run_step3(self):
        if not messagebox.askyesno("確認", "確定要推送測試結果嗎？"):
            return

        topic = self.topic_var.get()
        device = self.device_var.get()
        result = self.result_var.get()
        date_str = datetime.datetime.now().strftime("%Y-%m-%d")

        self.log(f"\n--- 🚀 Starting Task 3: Work NB (Push) ---", add_timestamp=True)

        # Git Add
        self.log("Step 1: Adding test results...")
        returncode, stdout, stderr = self.run_command(["git", "add", "docs/test_results/"])
        if self.handle_git_error("git add", returncode, stderr):
            self.log("\n❌ 添加檔案失敗，操作中止。")
            return

        # Git Commit
        self.log("Step 2: Committing...")
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
            if 'nothing to commit' in stderr.lower():
                self.log("\n⚠️ 沒有測試報告可提交，請確保已生成測試報告。")
                return
            else:
                self.log("\n❌ 提交失敗，操作中止。")
                return

        # Git Push
        remote = self.remote_detector.get_remote_for('work_push')
        self.log(f"Step 3: Pushing to {remote} (main)...")
        returncode, stdout, stderr = self.run_command(["git", "push", remote, "main"])
        if self.handle_git_error("git push", returncode, stderr):
            self.log("\n❌ 推送失敗。")
        else:
            self.log("\n✅ 報告推送成功！")
            messagebox.showinfo("成功", "報告已上傳！\n\n下一步：請切換回 Home NB 執行步驟 4 (Pull)。")

    def run_step4(self):
        self.log(f"\n--- 📥 Starting Task 4: Home NB (Pull) ---", add_timestamp=True)

        # Git Pull
        remote = self.remote_detector.get_remote_for('home_pull')
        self.log(f"Step 1: Pulling from {remote} (main)...")
        returncode, stdout, stderr = self.run_command(["git", "pull", remote, "main"])
        if self.handle_git_error("git pull", returncode, stderr):
            self.log("\n❌ 下載失敗。")
            return

        self.log("\n✅ 下載完成。正在尋找最新報告...")

        # Find and open latest report
        self.log("Step 2: Opening latest test report...")
        cmd = "Get-ChildItem docs/test_results/*.md -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1 | ForEach-Object { Write-Host 'Opening: ' $_.FullName; code $_.FullName }"
        returncode, stdout, stderr = self.run_command(["powershell", "-Command", cmd])

        if stdout and 'Opening:' in stdout:
            self.log("\n✅ 測試交接循環完成！")
        else:
            self.log("\n⚠️ 未找到測試報告，請確認 docs/test_results/ 目錄中有測試報告檔案。")

if __name__ == "__main__":
    root = tk.Tk()
    # 設定圖示 (如果有的話)
    # root.iconbitmap('icon.ico') 
    app = TestHandoffApp(root)
    root.mainloop()
