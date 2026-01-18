#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
UsbDevTool_Enhanced.pyw
ADB Device Management Tool - Python Version
美化版 ADB USB 偵錯 / 螢幕常亮控制工具
"""

import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox
import subprocess
import threading
from datetime import datetime
from typing import Dict, List, Optional


class Colors:
    """Color scheme for the application"""
    PRIMARY = "#2980b9"
    PRIMARY_DARK = "#1e6091"
    SUCCESS = "#27ae60"
    WARNING = "#f39c12"
    DANGER = "#e74c3c"
    BACKGROUND = "#ecf0f1"
    CARD_BG = "#ffffff"
    TEXT_DARK = "#2c3e50"
    TEXT_LIGHT = "#ffffff"
    BORDER = "#bdc3c7"


class ADBManager:
    """ADB Command Manager"""

    def __init__(self, adb_path: str = "adb"):
        self.adb_path = adb_path

    def execute(self, args: str, serial: Optional[str] = None) -> Dict[str, any]:
        """
        Execute ADB command and return result

        Args:
            args: ADB command arguments
            serial: Device serial number (optional)

        Returns:
            Dict with 'success', 'output', and 'error' keys
        """
        try:
            if serial and serial.strip():
                command = f"{self.adb_path} -s {serial} {args}"
            else:
                command = f"{self.adb_path} {args}"

            result = subprocess.run(
                command,
                shell=True,
                capture_output=True,
                text=True,
                encoding='utf-8',
                timeout=10
            )

            if result.returncode == 0:
                return {
                    'success': True,
                    'output': result.stdout,
                    'error': ''
                }
            else:
                return {
                    'success': False,
                    'output': result.stdout,
                    'error': result.stderr or 'Unknown error'
                }
        except subprocess.TimeoutExpired:
            return {
                'success': False,
                'output': '',
                'error': 'Command timeout (> 10s)'
            }
        except Exception as e:
            return {
                'success': False,
                'output': '',
                'error': f'Exception: {str(e)}'
            }


class ADBDeviceToolGUI:
    """ADB Device Management Tool GUI"""

    def __init__(self, root: tk.Tk):
        self.root = root
        self.adb = ADBManager()

        # Configure root window
        self.root.title("ADB 裝置管理工具")
        self.root.geometry("800x600")
        self.root.resizable(False, False)
        self.root.configure(bg=Colors.BACKGROUND)

        # Setup GUI
        self.setup_ui()

        # Initialize device list
        self.root.after(100, self.update_device_list)

    def setup_ui(self):
        """Setup the user interface"""
        # Header
        self.create_header()

        # Device selection area
        self.create_device_section()

        # Screen control area
        self.create_control_section()

        # App permissions area
        self.create_permissions_section()

        # Log area
        self.create_log_section()

        # Bottom buttons
        self.create_bottom_buttons()

        # Status bar
        self.create_statusbar()

    def create_header(self):
        """Create header panel"""
        header = tk.Frame(self.root, bg=Colors.PRIMARY, height=60)
        header.pack(fill=tk.X)
        header.pack_propagate(False)

        title_label = tk.Label(
            header,
            text="ADB 裝置管理工具",
            font=("Microsoft JhengHei UI", 16, "bold"),
            fg=Colors.TEXT_LIGHT,
            bg=Colors.PRIMARY
        )
        title_label.pack(side=tk.LEFT, padx=20, pady=15)

    def create_device_section(self):
        """Create device selection section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 裝置連線 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=20, pady=(15, 5), fill=tk.X)

        # Device label and combobox
        info_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        info_frame.pack(pady=(10, 5), padx=15, fill=tk.X)

        tk.Label(
            info_frame,
            text="已連線裝置：",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG
        ).pack(side=tk.LEFT)

        self.device_combo = ttk.Combobox(
            info_frame,
            state="readonly",
            font=("Consolas", 9),
            width=35
        )
        self.device_combo.pack(side=tk.LEFT, padx=(5, 10))
        self.device_combo.bind("<<ComboboxSelected>>", self.on_device_selected)

        # Buttons
        btn_frame = tk.Frame(info_frame, bg=Colors.CARD_BG)
        btn_frame.pack(side=tk.LEFT, padx=5)

        self.create_button(
            btn_frame,
            "🔄 重新整理",
            Colors.PRIMARY,
            self.update_device_list
        ).pack(side=tk.LEFT, padx=2)

        self.create_button(
            btn_frame,
            "🔧 重啟 ADB",
            Colors.WARNING,
            self.restart_adb
        ).pack(side=tk.LEFT, padx=2)

        # Device info label
        self.device_info_label = tk.Label(
            frame,
            text="裝置資訊：未選擇裝置",
            font=("Microsoft JhengHei UI", 8),
            fg="gray",
            bg=Colors.CARD_BG,
            anchor=tk.W
        )
        self.device_info_label.pack(pady=(5, 10), padx=15, fill=tk.X)

    def create_control_section(self):
        """Create screen control section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 螢幕控制 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=20, pady=5, fill=tk.X)

        tk.Label(
            frame,
            text="控制手機螢幕常亮設定與休眠（需先啟用 USB 偵錯）",
            font=("Microsoft JhengHei UI", 8),
            fg="gray",
            bg=Colors.CARD_BG
        ).pack(pady=(10, 5), padx=15)

        btn_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        btn_frame.pack(pady=(5, 10))

        self.create_button(
            btn_frame,
            "✓ 啟用常亮",
            Colors.SUCCESS,
            self.enable_stay_on,
            width=15
        ).pack(side=tk.LEFT, padx=5)

        self.create_button(
            btn_frame,
            "✗ 停用常亮",
            Colors.DANGER,
            self.disable_stay_on,
            width=15
        ).pack(side=tk.LEFT, padx=5)

        self.create_button(
            btn_frame,
            "💤 螢幕休眠",
            Colors.WARNING,
            self.sleep_screen,
            width=15,
            fg=Colors.TEXT_DARK
        ).pack(side=tk.LEFT, padx=5)

    def create_permissions_section(self):
        """Create app permissions section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 應用程式權限控制 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=20, pady=5, fill=tk.X)

        tk.Label(
            frame,
            text="授予特定應用程式的系統權限（需先啟用 USB 偵錯）",
            font=("Microsoft JhengHei UI", 8),
            fg="gray",
            bg=Colors.CARD_BG
        ).pack(pady=(10, 5), padx=15)

        btn_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        btn_frame.pack(pady=(5, 10))

        self.create_button(
            btn_frame,
            "📱 授予 TeamViewer 媒體權限",
            Colors.PRIMARY,
            self.grant_teamviewer_permission,
            width=28
        ).pack(padx=10)

    def create_log_section(self):
        """Create log section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 操作日誌 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=20, pady=5, fill=tk.BOTH, expand=True)

        self.log_text = scrolledtext.ScrolledText(
            frame,
            font=("Consolas", 9),
            bg="#fafafa",
            relief=tk.FLAT,
            height=8
        )
        self.log_text.pack(padx=10, pady=10, fill=tk.BOTH, expand=True)
        self.log_text.config(state=tk.DISABLED)

    def create_bottom_buttons(self):
        """Create bottom buttons"""
        btn_frame = tk.Frame(self.root, bg=Colors.BACKGROUND)
        btn_frame.pack(pady=10, padx=20, fill=tk.X)

        self.create_button(
            btn_frame,
            "清除日誌",
            Colors.BORDER,
            self.clear_log,
            width=12,
            fg=Colors.TEXT_DARK
        ).pack(side=tk.RIGHT, padx=5)

        self.create_button(
            btn_frame,
            "關閉",
            Colors.DANGER,
            self.root.quit,
            width=10
        ).pack(side=tk.RIGHT, padx=5)

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

    def create_button(self, parent, text, bg_color, command, width=15, fg=Colors.TEXT_LIGHT):
        """Create a styled button"""
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
            width=width
        )

        # Hover effects
        def on_enter(e):
            btn['bg'] = self.darken_color(bg_color)

        def on_leave(e):
            btn['bg'] = bg_color

        btn.bind("<Enter>", on_enter)
        btn.bind("<Leave>", on_leave)

        return btn

    @staticmethod
    def darken_color(hex_color):
        """Darken a hex color by 10%"""
        hex_color = hex_color.lstrip('#')
        r, g, b = tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))
        r, g, b = int(r * 0.9), int(g * 0.9), int(b * 0.9)
        return f'#{r:02x}{g:02x}{b:02x}'

    def write_log(self, message: str, log_type: str = "Info"):
        """Write colored log message"""
        timestamp = datetime.now().strftime("%H:%M:%S")

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

    def clear_log(self):
        """Clear log text"""
        self.log_text.config(state=tk.NORMAL)
        self.log_text.delete(1.0, tk.END)
        self.log_text.config(state=tk.DISABLED)
        self.write_log("日誌已清除", "Info")

    def get_selected_device(self) -> Optional[str]:
        """Get currently selected device serial"""
        if not self.device_combo.get():
            messagebox.showinfo("提示", "請先選擇一台裝置。")
            return None
        return self.device_combo.get()

    def run_async(self, func, *args):
        """Run function in background thread"""
        thread = threading.Thread(target=func, args=args, daemon=True)
        thread.start()

    def update_device_list(self):
        """Update device list"""
        def task():
            self.write_log("重新整理裝置列表...", "Info")
            self.status_var.set("正在掃描裝置...")

            result = self.adb.execute("devices")

            self.device_combo['values'] = []
            self.device_info_label.config(text="裝置資訊：未選擇裝置")

            if not result['success']:
                self.write_log(f"錯誤：{result['error']}", "Error")
                self.status_var.set("裝置掃描失敗")
                return

            # Parse device list
            devices = []
            lines = result['output'].strip().split('\n')

            for line in lines:
                if '\tdevice' in line and 'List of devices' not in line:
                    serial = line.split('\t')[0].strip()
                    if serial:
                        devices.append(serial)

            if devices:
                self.device_combo['values'] = devices
                self.device_combo.current(0)
                self.get_device_info(devices[0])
                self.write_log(f"找到 {len(devices)} 台裝置", "Success")
                self.status_var.set(f"已連線 {len(devices)} 台裝置")
            else:
                self.write_log("未找到任何裝置，請確認 USB 偵錯已啟用", "Warning")
                self.status_var.set("未找到裝置")

        self.run_async(task)

    def on_device_selected(self, event):
        """Handle device selection"""
        serial = self.device_combo.get()
        if serial:
            self.run_async(self.get_device_info, serial)

    def get_device_info(self, serial: str):
        """Get device information"""
        self.status_var.set("正在取得裝置資訊...")

        # Get device model
        model_result = self.adb.execute("shell getprop ro.product.model", serial)
        model = model_result['output'].strip() if model_result['success'] else "未知"

        # Get Android version
        version_result = self.adb.execute("shell getprop ro.build.version.release", serial)
        version = version_result['output'].strip() if version_result['success'] else "未知"

        info_text = f"裝置資訊：{model} | Android {version} | 序號：{serial}"
        self.device_info_label.config(text=info_text)
        self.write_log(f"裝置：{model} (Android {version})", "Info")

        self.status_var.set("就緒")

    def restart_adb(self):
        """Restart ADB server"""
        def task():
            self.write_log("正在重啟 ADB 伺服器...", "Info")
            self.status_var.set("重啟 ADB 伺服器中...")

            self.adb.execute("kill-server")
            self.root.after(500, lambda: None)  # Wait 500ms
            result = self.adb.execute("start-server")

            if result['success']:
                self.write_log("ADB 伺服器重啟成功", "Success")
            else:
                self.write_log(f"ADB 伺服器重啟失敗：{result['error']}", "Error")

            self.root.after(100, self.update_device_list)

        self.run_async(task)

    def enable_stay_on(self):
        """Enable screen stay on"""
        serial = self.get_selected_device()
        if not serial:
            return

        def task():
            self.write_log("正在啟用 USB 充電時螢幕常亮...", "Info")
            self.status_var.set("執行中...")

            result = self.adb.execute("shell svc power stayon usb", serial)

            if result['success']:
                self.write_log("已成功啟用螢幕常亮功能", "Success")
                self.status_var.set("螢幕常亮已啟用")
            else:
                self.write_log(f"啟用失敗：{result['error']}", "Error")
                self.status_var.set("操作失敗")

        self.run_async(task)

    def disable_stay_on(self):
        """Disable screen stay on"""
        serial = self.get_selected_device()
        if not serial:
            return

        def task():
            self.write_log("正在停用螢幕常亮功能...", "Info")
            self.status_var.set("執行中...")

            result = self.adb.execute("shell svc power stayon false", serial)

            if result['success']:
                self.write_log("已成功停用螢幕常亮功能", "Success")
                self.status_var.set("螢幕常亮已停用")
            else:
                self.write_log(f"停用失敗：{result['error']}", "Error")
                self.status_var.set("操作失敗")

        self.run_async(task)

    def sleep_screen(self):
        """Put screen to sleep (lock screen)"""
        serial = self.get_selected_device()
        if not serial:
            return

        def task():
            self.write_log("正在執行螢幕休眠...", "Info")
            self.status_var.set("執行中...")

            # Use keyevent 26 (KEYCODE_POWER) to simulate power button press
            result = self.adb.execute("shell input keyevent 26", serial)

            if result['success']:
                self.write_log("已成功執行螢幕休眠（模擬電源鍵）", "Success")
                self.status_var.set("螢幕已休眠")
            else:
                self.write_log(f"休眠失敗：{result['error']}", "Error")
                self.status_var.set("操作失敗")

        self.run_async(task)

    def grant_teamviewer_permission(self):
        """Grant TeamViewer media projection permission"""
        serial = self.get_selected_device()
        if not serial:
            return

        def task():
            self.write_log("正在授予 TeamViewer 媒體投影權限...", "Info")
            self.status_var.set("執行中...")

            result = self.adb.execute(
                "shell appops set com.teamviewer.host.market PROJECT_MEDIA allow",
                serial
            )

            if result['success']:
                self.write_log("已成功授予 TeamViewer 媒體投影權限 (PROJECT_MEDIA)", "Success")
                self.status_var.set("權限已授予")
            else:
                self.write_log(f"授予權限失敗：{result['error']}", "Error")
                self.status_var.set("操作失敗")

        self.run_async(task)


def main():
    """Main entry point"""
    root = tk.Tk()
    app = ADBDeviceToolGUI(root)

    # Welcome message
    app.write_log("ADB 裝置管理工具已啟動", "Success")
    app.write_log("提示：請確保手機已開啟 USB 偵錯功能", "Info")

    root.mainloop()


if __name__ == "__main__":
    main()
