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
CLAUDE_MODEL_OPTIONS = ("sonnet", "opus", "haiku")
CLAUDE_EFFORT_OPTIONS = ("low", "medium", "high")
DEFAULT_CLAUDE_MODEL = "sonnet"
DEFAULT_CLAUDE_EFFORT = "medium"

DEFAULT_PROMPT_TEMPLATE = """請讀取 @docs/session_handoff/[交接文檔], @.claude/PROGRESS.md 並回報下列事項:
1. 上次完成到哪裡？（最後一個 ✅ DONE 的 Ticket）
2. 今天的 Tickets 是什麼？ [Session名稱]
3. 有沒有未解決的 Blocking Issues？
4. 下一個 KAIROS 節點是哪天？」
5. **確認指令**: 你已讀取 CLAUDE.md。 請執行 Boot Sequence 並輸出 Boot Report。"""


class Colors:
    # 完全套用 mcp_ticket_platform_manager.pyw 淺色 palette（flatui 系）。
    PRIMARY = "#2980b9"
    SUCCESS = "#27ae60"
    WARNING = "#f39c12"
    DANGER = "#c0392b"
    BACKGROUND = "#ecf0f1"
    CARD_BG = "#ffffff"
    TEXT_DARK = "#2c3e50"
    TEXT_LIGHT = "#ffffff"
    MUTED = "#7f8c8d"
    TABLE_HEAD = "#f8f9fa"
    LOG_BG = "#f8f9fa"
    # launcher 專屬：manager 無表單欄位故未定義；Entry/Combobox 需邊框對比，
    # 取 flatui 同系淺灰 Silver 作為欄位邊框色。
    BORDER = "#bdc3c7"


UI_FONT = "Microsoft JhengHei UI"
MONO_FONT = "Consolas"


class ClaudeSessionLauncher:
    def __init__(self, root: tk.Tk):
        self.root = root
        self.root.title("Claude Session Launcher")
        # 預設尺寸貼合筆電螢幕（1366×768 扣工作列後可用高度約 700px）。
        self.root.geometry("940x680")
        self.root.minsize(780, 560)
        self.root.configure(bg=Colors.BACKGROUND)

        self._configure_styles()

        self.project_path_var = tk.StringVar(value=str(DEFAULT_PROJECT_PATH))
        self.handoff_var = tk.StringVar()
        self.session_name_var = tk.StringVar()
        self.claude_model_var = tk.StringVar(value=DEFAULT_CLAUDE_MODEL)
        self.claude_effort_var = tk.StringVar(value=DEFAULT_CLAUDE_EFFORT)
        self.status_var = tk.StringVar(value="Ready")
        self.handoff_files: list[Path] = []

        self._build_ui()
        self.refresh_handoff_files()

    def _configure_styles(self) -> None:
        # ttk 僅用於 Combobox 下拉（純 tk 無等效 readonly 下拉元件）；
        # 其餘結構全部走 manager 的純 tk 語彙。淺色 palette。
        style = ttk.Style(self.root)
        try:
            style.theme_use("clam")
        except tk.TclError:
            pass
        style.configure(
            "TCombobox",
            padding=(8, 6),
            font=(UI_FONT, 11),
            fieldbackground=Colors.CARD_BG,
            background=Colors.BORDER,
            foreground=Colors.TEXT_DARK,
            bordercolor=Colors.BORDER,
            lightcolor=Colors.BORDER,
            darkcolor=Colors.BORDER,
            arrowcolor=Colors.TEXT_DARK,
        )
        style.map(
            "TCombobox",
            fieldbackground=[("readonly", Colors.CARD_BG), ("focus", Colors.CARD_BG)],
            foreground=[("readonly", Colors.TEXT_DARK), ("focus", Colors.TEXT_DARK)],
            bordercolor=[("focus", Colors.PRIMARY)],
            arrowcolor=[("focus", Colors.PRIMARY)],
        )
        # Apply standard option database styling for listbox popup of ttk.Combobox
        self.root.option_add("*TCombobox*Listbox.background", Colors.CARD_BG)
        self.root.option_add("*TCombobox*Listbox.foreground", Colors.TEXT_DARK)
        self.root.option_add("*TCombobox*Listbox.selectBackground", Colors.PRIMARY)
        self.root.option_add("*TCombobox*Listbox.selectForeground", Colors.TEXT_LIGHT)
        self.root.option_add("*TCombobox*Listbox.font", (UI_FONT, 11))

    def _build_ui(self) -> None:
        # ── Header（manager 樣式：bg=TEXT_DARK，height=58，標題左對齊）──
        header = tk.Frame(self.root, bg=Colors.TEXT_DARK, height=58)
        header.pack(side=tk.TOP, fill=tk.X)
        header.pack_propagate(False)

        tk.Label(
            header,
            text="Claude Session Launcher",
            bg=Colors.TEXT_DARK,
            fg=Colors.TEXT_LIGHT,
            font=(UI_FONT, 17, "bold"),
        ).pack(side=tk.LEFT, padx=18)

        # ── Status bar：在 body 之前先以 side=BOTTOM 預留（修復按鈕/狀態列被裁切）──
        self.status_bar = tk.Label(
            self.root,
            textvariable=self.status_var,
            anchor=tk.W,
            bg=Colors.TEXT_DARK,
            fg=Colors.TEXT_LIGHT,
            padx=10,
            pady=5,
        )
        self.status_bar.pack(side=tk.BOTTOM, fill=tk.X)

        body = tk.Frame(self.root, bg=Colors.BACKGROUND)
        body.pack(side=tk.TOP, fill=tk.BOTH, expand=True, padx=14, pady=14)

        def field_label(parent: tk.Misc, text: str) -> tk.Label:
            return tk.Label(
                parent,
                text=text,
                bg=Colors.CARD_BG,
                fg=Colors.TEXT_DARK,
                font=(UI_FONT, 10, "bold"),
                anchor=tk.W,
            )

        def flat_button(
            parent: tk.Misc, text: str, command, color: str
        ) -> tk.Button:
            # manager 按鈕樣式：flat、activebackground=color、padx=16/pady=7。
            # 不套 width=9（manager 短英文標籤才適用；長中文標籤會裁切）。
            # 不加 hover 動畫 / cursor / 字體覆寫，維持 manager 純樣式。
            return tk.Button(
                parent,
                text=text,
                command=command,
                bg=color,
                fg=Colors.TEXT_LIGHT,
                activebackground=color,
                activeforeground=Colors.TEXT_LIGHT,
                relief=tk.FLAT,
                padx=16,
                pady=7,
            )

        # ── Session Setup 卡片（manager 純 LabelFrame）──
        settings = tk.LabelFrame(
            body,
            text="Session Setup",
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            padx=12,
            pady=10,
        )
        settings.pack(side=tk.TOP, fill=tk.X)
        settings.columnconfigure(1, weight=1)
        settings.columnconfigure(2, weight=0)

        field_label(settings, "Project 工作路徑").grid(
            row=0, column=0, sticky=tk.W, padx=(0, 10), pady=6
        )
        tk.Entry(
            settings,
            textvariable=self.project_path_var,
            font=(UI_FONT, 11),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            insertbackground=Colors.TEXT_DARK,
            relief=tk.FLAT,
            highlightthickness=1,
            highlightbackground=Colors.BORDER,
            highlightcolor=Colors.PRIMARY,
        ).grid(row=0, column=1, sticky=tk.EW, padx=(0, 10), pady=6, ipady=3)
        flat_button(settings, "瀏覽資料夾", self.browse_project_path, Colors.PRIMARY).grid(
            row=0, column=2, sticky=tk.E, pady=6
        )

        field_label(settings, "交接文檔").grid(
            row=1, column=0, sticky=tk.W, padx=(0, 10), pady=6
        )
        self.handoff_combo = ttk.Combobox(
            settings,
            textvariable=self.handoff_var,
            state="readonly",
        )
        self.handoff_combo.grid(row=1, column=1, sticky=tk.EW, padx=(0, 10), pady=6)
        flat_button(settings, "重新載入", self.refresh_handoff_files, Colors.PRIMARY).grid(
            row=1, column=2, sticky=tk.E, pady=6
        )

        field_label(settings, "Session 名稱").grid(
            row=2, column=0, sticky=tk.W, padx=(0, 10), pady=6
        )
        tk.Entry(
            settings,
            textvariable=self.session_name_var,
            font=(UI_FONT, 11),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            insertbackground=Colors.TEXT_DARK,
            relief=tk.FLAT,
            highlightthickness=1,
            highlightbackground=Colors.BORDER,
            highlightcolor=Colors.PRIMARY,
        ).grid(row=2, column=1, columnspan=2, sticky=tk.EW, pady=6, ipady=3)

        field_label(settings, "Claude 啟動參數").grid(
            row=3, column=0, sticky=tk.W, padx=(0, 10), pady=6
        )
        claude_options = tk.Frame(settings, bg=Colors.CARD_BG)
        claude_options.grid(row=3, column=1, columnspan=2, sticky=tk.EW, pady=6)
        claude_options.columnconfigure(1, weight=1)
        claude_options.columnconfigure(3, weight=1)

        tk.Label(
            claude_options,
            text="--model",
            bg=Colors.CARD_BG,
            fg=Colors.MUTED,
            font=(MONO_FONT, 10),
        ).grid(row=0, column=0, sticky=tk.W)
        self.claude_model_combo = ttk.Combobox(
            claude_options,
            textvariable=self.claude_model_var,
            values=CLAUDE_MODEL_OPTIONS,
            state="readonly",
            width=16,
        )
        self.claude_model_combo.grid(row=0, column=1, sticky=tk.EW, padx=(8, 18))

        tk.Label(
            claude_options,
            text="--effort",
            bg=Colors.CARD_BG,
            fg=Colors.MUTED,
            font=(MONO_FONT, 10),
        ).grid(row=0, column=2, sticky=tk.W)
        self.claude_effort_combo = ttk.Combobox(
            claude_options,
            textvariable=self.claude_effort_var,
            values=CLAUDE_EFFORT_OPTIONS,
            state="readonly",
            width=16,
        )
        self.claude_effort_combo.grid(row=0, column=3, sticky=tk.EW, padx=(8, 0))

        # ── Button bar：在 expand frames 之前先以 side=BOTTOM 預留 ──
        button_bar = tk.Frame(body, bg=Colors.BACKGROUND)
        button_bar.pack(side=tk.BOTTOM, fill=tk.X, pady=(12, 0))

        tk.Label(
            button_bar,
            text="確認 Project、handoff 與 Session 名稱後再執行。",
            bg=Colors.BACKGROUND,
            fg=Colors.MUTED,
            font=(UI_FONT, 10),
        ).pack(side=tk.LEFT)

        flat_button(button_bar, "執行 Claude Session", self.run_claude_session, Colors.SUCCESS).pack(
            side=tk.RIGHT, padx=4
        )
        flat_button(button_bar, "複製 Prompt", self.copy_final_prompt, Colors.PRIMARY).pack(
            side=tk.RIGHT, padx=4
        )

        # ── Prompt 樣板卡片（expand）──
        prompt_frame = tk.LabelFrame(
            body,
            text="Prompt 樣板",
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            padx=12,
            pady=10,
        )
        prompt_frame.pack(side=tk.TOP, fill=tk.BOTH, expand=True, pady=(12, 0))

        tk.Label(
            prompt_frame,
            text="提示：使用 [交接文檔] 與 [Session名稱] 作為自動替換欄位。",
            bg=Colors.CARD_BG,
            fg=Colors.MUTED,
            font=(UI_FONT, 10),
            anchor=tk.W,
        ).pack(fill=tk.X, pady=(0, 8))

        self.prompt_text = scrolledtext.ScrolledText(
            prompt_frame,
            height=7,
            wrap=tk.WORD,
            font=(UI_FONT, 12),
            relief=tk.FLAT,
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            insertbackground=Colors.TEXT_DARK,
            highlightthickness=1,
            highlightbackground=Colors.BORDER,
            highlightcolor=Colors.PRIMARY,
            padx=10,
            pady=8,
        )
        self.prompt_text.pack(fill=tk.BOTH, expand=True)
        self.prompt_text.insert("1.0", DEFAULT_PROMPT_TEMPLATE)

        # ── Command Preview 卡片（expand）──
        preview_frame = tk.LabelFrame(
            body,
            text="Command Preview",
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            padx=12,
            pady=10,
        )
        preview_frame.pack(side=tk.TOP, fill=tk.BOTH, expand=True, pady=(12, 0))

        self.preview_text = tk.Text(
            preview_frame,
            height=6,
            wrap=tk.WORD,
            font=(MONO_FONT, 11),
            bg=Colors.LOG_BG,
            fg=Colors.TEXT_DARK,
            insertbackground=Colors.TEXT_DARK,
            relief=tk.FLAT,
            padx=10,
            pady=8,
        )
        self.preview_text.pack(fill=tk.BOTH, expand=True)
        self.preview_text.configure(state=tk.DISABLED)

        self.project_path_var.trace_add("write", lambda *_: self.update_preview())
        self.handoff_var.trace_add("write", lambda *_: self.update_preview())
        self.session_name_var.trace_add("write", lambda *_: self.update_preview())
        self.claude_model_var.trace_add("write", lambda *_: self.update_preview())
        self.claude_effort_var.trace_add("write", lambda *_: self.update_preview())
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
        claude_model = self.claude_model_var.get().strip() or DEFAULT_CLAUDE_MODEL
        claude_effort = self.claude_effort_var.get().strip() or DEFAULT_CLAUDE_EFFORT
        final_prompt = self.build_final_prompt()

        launcher_path = self.write_temp_launcher(
            project_path,
            session_name,
            claude_model,
            claude_effort,
            final_prompt,
        )
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

    def write_temp_launcher(
        self,
        project_path: Path,
        session_name: str,
        claude_model: str,
        claude_effort: str,
        final_prompt: str,
    ) -> Path:
        temp_dir = Path(tempfile.gettempdir()) / "smartcamera_claude_launcher"
        temp_dir.mkdir(parents=True, exist_ok=True)
        launcher_path = temp_dir / "run_claude_session.py"

        payload = {
            "project_path": str(project_path),
            "session_name": session_name,
            "claude_model": claude_model,
            "claude_effort": claude_effort,
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
claude_model = payload["claude_model"]
claude_effort = payload["claude_effort"]
prompt = payload["prompt"]
claude = shutil.which("claude") or shutil.which("claude.cmd")

print("cd " + project_path)
print('claude --model "' + claude_model + '" --effort "' + claude_effort + '" -n "' + session_name + '" "' + prompt.replace(chr(10), "\\\\n") + '"')
print()

if not claude:
    print("ERROR: 找不到 claude CLI。請確認 claude 已安裝且在 PATH 中。")
    input("Press Enter to close...")
    sys.exit(1)

try:
    exit_code = subprocess.call(
        [claude, "--model", claude_model, "--effort", claude_effort, "-n", session_name, prompt],
        cwd=project_path,
    )
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
        claude_model = self.claude_model_var.get().strip() or DEFAULT_CLAUDE_MODEL
        claude_effort = self.claude_effort_var.get().strip() or DEFAULT_CLAUDE_EFFORT
        prompt = self.build_final_prompt() if self.handoff_var.get() else self.get_template_text()
        one_line_prompt = prompt.replace("\n", "\\n")
        preview = (
            f'cd /d "{project_path}"\n'
            f'claude --model "{claude_model}" --effort "{claude_effort}" '
            f'-n "{session_name}" "{one_line_prompt}"'
        )

        self.preview_text.configure(state=tk.NORMAL)
        self.preview_text.delete("1.0", tk.END)
        self.preview_text.insert("1.0", preview)
        self.preview_text.configure(state=tk.DISABLED)

    def set_status(self, message: str, error: bool = False) -> None:
        self.status_var.set(message)
        self.status_bar.configure(bg=Colors.DANGER if error else Colors.TEXT_DARK)
        if error:
            self.root.bell()


def main() -> None:
    root = tk.Tk()
    ClaudeSessionLauncher(root)
    root.mainloop()


if __name__ == "__main__":
    main()
