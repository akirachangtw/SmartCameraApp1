#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Claude_Session_open.pyw
Small GUI launcher for opening a Claude session with the latest handoff prompt.
"""

from __future__ import annotations

import json
import shutil
import subprocess
import sys
import tempfile
import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, scrolledtext, ttk


DEFAULT_PROJECT_PATH = Path("d:/DevSources/AndroidProj/SmartCameraApp1")
HANDOFF_DIR = Path("docs/session_handoff")

DEFAULT_PROMPT_TEMPLATE = """請讀取 @docs/session_handoff/[交接文檔], @.claude/PROGRESS.md 並回報下列事項:
1. 上次完成到哪裡？（最後一個 ✅ DONE 的 Ticket）
2. 今天的 Tickets 是什麼？ [Session名稱]
3. 有沒有未解決的 Blocking Issues？
4. 下一個 KAIROS 節點是哪天？」
5. **確認指令**: 你已讀取 CLAUDE.md。 請執行 Boot Sequence 並輸出 Boot Report。"""


class Colors:
    PRIMARY = "#2980b9"
    SUCCESS = "#27ae60"
    DANGER = "#c0392b"
    BACKGROUND = "#ecf0f1"
    CARD_BG = "#ffffff"
    TEXT_DARK = "#2c3e50"
    TEXT_LIGHT = "#ffffff"
    BORDER = "#bdc3c7"


class ClaudeSessionLauncher:
    def __init__(self, root: tk.Tk):
        self.root = root
        self.root.title("Claude Session Launcher")
        self.root.geometry("920x720")
        self.root.minsize(760, 560)
        self.root.configure(bg=Colors.BACKGROUND)

        self.project_path_var = tk.StringVar(value=str(DEFAULT_PROJECT_PATH))
        self.handoff_var = tk.StringVar()
        self.session_name_var = tk.StringVar()
        self.status_var = tk.StringVar(value="Ready")
        self.handoff_files: list[Path] = []

        self._build_ui()
        self.refresh_handoff_files()

    def _build_ui(self) -> None:
        header = tk.Frame(self.root, bg=Colors.TEXT_DARK, height=56)
        header.pack(fill=tk.X)
        header.pack_propagate(False)

        tk.Label(
            header,
            text="Claude Session Launcher",
            bg=Colors.TEXT_DARK,
            fg=Colors.TEXT_LIGHT,
            font=("Microsoft JhengHei UI", 17, "bold"),
        ).pack(side=tk.LEFT, padx=18)

        body = tk.Frame(self.root, bg=Colors.BACKGROUND)
        body.pack(fill=tk.BOTH, expand=True, padx=14, pady=14)

        settings = tk.LabelFrame(
            body,
            text="Session Setup",
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            padx=12,
            pady=10,
        )
        settings.pack(fill=tk.X)
        settings.columnconfigure(1, weight=1)

        tk.Label(settings, text="Project工作路徑", bg=Colors.CARD_BG).grid(
            row=0, column=0, sticky=tk.W, pady=6
        )
        ttk.Entry(settings, textvariable=self.project_path_var).grid(
            row=0, column=1, sticky=tk.EW, padx=8, pady=6
        )
        ttk.Button(settings, text="瀏覽資料夾", command=self.browse_project_path).grid(
            row=0, column=2, sticky=tk.E, pady=6
        )

        tk.Label(settings, text="交接文檔", bg=Colors.CARD_BG).grid(
            row=1, column=0, sticky=tk.W, pady=6
        )
        self.handoff_combo = ttk.Combobox(
            settings,
            textvariable=self.handoff_var,
            state="readonly",
        )
        self.handoff_combo.grid(row=1, column=1, sticky=tk.EW, padx=8, pady=6)
        ttk.Button(settings, text="重新載入", command=self.refresh_handoff_files).grid(
            row=1, column=2, sticky=tk.E, pady=6
        )

        tk.Label(settings, text="Session名稱", bg=Colors.CARD_BG).grid(
            row=2, column=0, sticky=tk.W, pady=6
        )
        ttk.Entry(settings, textvariable=self.session_name_var).grid(
            row=2, column=1, columnspan=2, sticky=tk.EW, padx=8, pady=6
        )

        prompt_frame = tk.LabelFrame(
            body,
            text="Prompt樣板",
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            padx=12,
            pady=10,
        )
        prompt_frame.pack(fill=tk.BOTH, expand=True, pady=(12, 0))

        self.prompt_text = scrolledtext.ScrolledText(
            prompt_frame,
            height=16,
            wrap=tk.WORD,
            font=("Microsoft JhengHei UI", 10),
        )
        self.prompt_text.pack(fill=tk.BOTH, expand=True)
        self.prompt_text.insert("1.0", DEFAULT_PROMPT_TEMPLATE)

        button_bar = tk.Frame(body, bg=Colors.BACKGROUND)
        button_bar.pack(fill=tk.X, pady=(12, 0))

        tk.Button(
            button_bar,
            text="複製最終 Prompt",
            command=self.copy_final_prompt,
            bg=Colors.PRIMARY,
            fg=Colors.TEXT_LIGHT,
            relief=tk.FLAT,
            padx=18,
            pady=8,
        ).pack(side=tk.LEFT)

        tk.Button(
            button_bar,
            text="執行 Claude Session",
            command=self.run_claude_session,
            bg=Colors.SUCCESS,
            fg=Colors.TEXT_LIGHT,
            relief=tk.FLAT,
            padx=22,
            pady=8,
        ).pack(side=tk.RIGHT)

        preview_frame = tk.LabelFrame(
            body,
            text="Command Preview",
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            padx=12,
            pady=8,
        )
        preview_frame.pack(fill=tk.X, pady=(12, 0))

        self.preview_text = tk.Text(
            preview_frame,
            height=4,
            wrap=tk.WORD,
            font=("Consolas", 9),
            bg="#f8f9fa",
            relief=tk.FLAT,
        )
        self.preview_text.pack(fill=tk.X)
        self.preview_text.configure(state=tk.DISABLED)

        status_bar = tk.Label(
            self.root,
            textvariable=self.status_var,
            anchor=tk.W,
            bg=Colors.TEXT_DARK,
            fg=Colors.TEXT_LIGHT,
            padx=10,
            pady=5,
        )
        status_bar.pack(fill=tk.X, side=tk.BOTTOM)

        self.project_path_var.trace_add("write", lambda *_: self.update_preview())
        self.handoff_var.trace_add("write", lambda *_: self.update_preview())
        self.session_name_var.trace_add("write", lambda *_: self.update_preview())
        self.prompt_text.bind("<KeyRelease>", lambda _event: self.update_preview())

    def browse_project_path(self) -> None:
        selected = filedialog.askdirectory(
            title="選擇 Project 工作路徑",
            initialdir=self.project_path_var.get() or str(DEFAULT_PROJECT_PATH),
        )
        if selected:
            self.project_path_var.set(selected)
            self.refresh_handoff_files()

    def refresh_handoff_files(self) -> None:
        project_path = self.get_project_path()
        handoff_dir = project_path / HANDOFF_DIR

        if not handoff_dir.exists():
            self.handoff_files = []
            self.handoff_combo["values"] = []
            self.handoff_var.set("")
            self.set_status(f"找不到交接文檔目錄: {handoff_dir}", error=True)
            self.update_preview()
            return

        self.handoff_files = sorted(
            handoff_dir.glob("*.md"),
            key=lambda path: path.stat().st_mtime,
            reverse=True,
        )
        values = [path.name for path in self.handoff_files]
        self.handoff_combo["values"] = values

        if values:
            current = self.handoff_var.get()
            self.handoff_var.set(current if current in values else values[0])
            self.set_status(f"已載入 {len(values)} 份交接文檔，預設最新檔: {self.handoff_var.get()}")
        else:
            self.handoff_var.set("")
            self.set_status(f"交接文檔目錄沒有 .md 檔案: {handoff_dir}", error=True)

        self.update_preview()

    def get_project_path(self) -> Path:
        return Path(self.project_path_var.get().strip()).expanduser()

    def get_template_text(self) -> str:
        return self.prompt_text.get("1.0", tk.END).strip()

    def build_final_prompt(self) -> str:
        handoff_name = self.handoff_var.get().strip()
        session_name = self.session_name_var.get().strip()
        return (
            self.get_template_text()
            .replace("[交接文檔]", handoff_name)
            .replace("[Session名稱]", session_name)
        )

    def validate_inputs(self) -> bool:
        project_path = self.get_project_path()
        if not project_path.exists() or not project_path.is_dir():
            messagebox.showerror("路徑錯誤", f"Project 工作路徑不存在:\n{project_path}")
            return False
        if not self.handoff_var.get().strip():
            messagebox.showerror("缺少交接文檔", "請先選取交接文檔。")
            return False
        if not self.session_name_var.get().strip():
            messagebox.showerror("缺少 Session 名稱", "請輸入 Session名稱。")
            return False
        if not self.get_template_text():
            messagebox.showerror("缺少 Prompt", "Prompt樣板不可為空。")
            return False
        return True

    def copy_final_prompt(self) -> None:
        if not self.validate_inputs():
            return
        final_prompt = self.build_final_prompt()
        self.root.clipboard_clear()
        self.root.clipboard_append(final_prompt)
        self.set_status("最終 Prompt 已複製到剪貼簿。")

    def run_claude_session(self) -> None:
        if not self.validate_inputs():
            return

        project_path = self.get_project_path()
        session_name = self.session_name_var.get().strip()
        final_prompt = self.build_final_prompt()

        launcher_path = self.write_temp_launcher(project_path, session_name, final_prompt)
        python_exe = self.find_console_python()
        creationflags = subprocess.CREATE_NEW_CONSOLE if sys.platform.startswith("win") else 0

        try:
            if sys.platform.startswith("win"):
                subprocess.Popen(
                    ["cmd.exe", "/k", python_exe, str(launcher_path)],
                    creationflags=creationflags,
                )
            else:
                subprocess.Popen([python_exe, str(launcher_path)])
        except Exception as exc:
            messagebox.showerror("執行失敗", f"無法啟動 Claude Session:\n{exc}")
            self.set_status(f"執行失敗: {exc}", error=True)
            return

        self.set_status(f"已啟動 Claude Session: {session_name}")

    def write_temp_launcher(self, project_path: Path, session_name: str, final_prompt: str) -> Path:
        temp_dir = Path(tempfile.gettempdir()) / "smartcamera_claude_launcher"
        temp_dir.mkdir(parents=True, exist_ok=True)
        launcher_path = temp_dir / "run_claude_session.py"

        payload = {
            "project_path": str(project_path),
            "session_name": session_name,
            "prompt": final_prompt,
        }
        script = f"""#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import shutil
import subprocess
import sys

payload = json.loads({json.dumps(json.dumps(payload, ensure_ascii=False))})
project_path = payload["project_path"]
session_name = payload["session_name"]
prompt = payload["prompt"]
claude = shutil.which("claude") or shutil.which("claude.cmd")

print("cd " + project_path)
print('claude -n "' + session_name + '" "' + prompt.replace(chr(10), "\\\\n") + '"')
print()

if not claude:
    print("ERROR: 找不到 claude CLI。請確認 claude 已安裝且在 PATH 中。")
    input("Press Enter to close...")
    sys.exit(1)

try:
    exit_code = subprocess.call([claude, "-n", session_name, prompt], cwd=project_path)
except Exception as exc:
    print("ERROR: Claude execution failed:", exc)
    input("Press Enter to close...")
    sys.exit(1)

print()
print("Claude process exited with code:", exit_code)
input("Press Enter to close...")
sys.exit(exit_code)
"""
        launcher_path.write_text(script, encoding="utf-8")
        return launcher_path

    def find_console_python(self) -> str:
        current = Path(sys.executable)
        if current.name.lower() == "pythonw.exe":
            python_exe = current.with_name("python.exe")
            if python_exe.exists():
                return str(python_exe)
        return shutil.which("python") or shutil.which("py") or str(current)

    def update_preview(self) -> None:
        project_path = self.project_path_var.get().strip() or str(DEFAULT_PROJECT_PATH)
        session_name = self.session_name_var.get().strip() or "[Session名稱]"
        prompt = self.build_final_prompt() if self.handoff_var.get() else self.get_template_text()
        one_line_prompt = prompt.replace("\n", "\\n")
        preview = f'cd /d "{project_path}"\nclaude -n "{session_name}" "{one_line_prompt}"'

        self.preview_text.configure(state=tk.NORMAL)
        self.preview_text.delete("1.0", tk.END)
        self.preview_text.insert("1.0", preview)
        self.preview_text.configure(state=tk.DISABLED)

    def set_status(self, message: str, error: bool = False) -> None:
        self.status_var.set(message)
        if error:
            self.root.bell()


def main() -> None:
    root = tk.Tk()
    ClaudeSessionLauncher(root)
    root.mainloop()


if __name__ == "__main__":
    main()
