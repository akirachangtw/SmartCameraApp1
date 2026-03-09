#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
prompt_generator_gui.pyw
Ultimate Defensive Hybrid Workflow Prompt Generator (Custom Workflow Edition)
使用 Gemini API 自動生成「終極防禦型混合工作流」四步驟提示詞，
並完美對接 Traycer Custom Workflow 的 5 大 Commands。
"""

import os
import re
import sys
import datetime
import threading
import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox
from pathlib import Path

try:
    import requests
except ImportError:
    requests = None

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')


# ── Color scheme ─────────────────────────────────────────────────────────────
class Colors:
    PRIMARY    = "#2980b9"
    SUCCESS    = "#27ae60"
    WARNING    = "#f39c12"  # Gemini
    DANGER     = "#e74c3c"
    BACKGROUND = "#ecf0f1"
    CARD_BG    = "#ffffff"
    TEXT_DARK  = "#2c3e50"
    TEXT_LIGHT = "#ffffff"
    BORDER     = "#bdc3c7"
    ACCENT     = "#8e44ad"  # Claude
    TRAYCER    = "#16a085"  # Traycer Trigger
    PLANNER    = "#34495e"  # Traycer Planner
    VERIFY     = "#1abc9c"  # Traycer Verify


# ── Step metadata ────────────────────────────────────────────────────────────
STEPS = [
    {"key": "Step 1", "label": "Step 1 — 宏觀防禦 (Traycer)",    "tool": "Traycer Trigger", "color": Colors.TRAYCER},
    {"key": "Step 2", "label": "Step 2 — 微觀防禦 (Gemini)",     "tool": "Gemini Architect", "color": Colors.WARNING},
    {"key": "Step 3", "label": "Step 3 — 匯入與排程 (Traycer)",  "tool": "Traycer Planner", "color": Colors.PLANNER},
    {"key": "Step 4", "label": "Step 4 — 實作與驗收 (雙重指令)", "tool": "Claude + Traycer", "color": Colors.VERIFY},
]

MODEL_NAME   = "gemini-2.5-flash"
API_URL      = f"https://generativelanguage.googleapis.com/v1beta/models/{MODEL_NAME}:generateContent"
PROJECT_ROOT = Path(__file__).parent.parent   # tools/../ → project root

SYSTEM_INSTRUCTION = """\
你是一位「終極防禦型混合工作流」的系統架構導師。根據使用者的問題，生成 4 個步驟的防禦性開發提示詞。
嚴格按照以下格式輸出，首先輸出 FileName 行，再依序輸出 Step 1-4：

FileName: [英文 snake_case，最多 30 字元，精確描述問題核心，例如 aeaf_lock_focus_delay]

Step 1: 宏觀防禦 (Traycer)
目的：說明此步驟的目的（釐清現況、驗證問題、確立邊界）。
建議提示詞：提供給 Traycer 的指令。內容必須包含：「我需要開發/重構 [問題相關功能]。請啟動 Ultimate Defensive Workflow 並執行 `trigger-workflow`。在規劃代碼前，請先探索專案現有邏輯，並進行 Spec-Driven 的釐清。請與我確認：1. 我的需求是否符合現實？ 2. 這次修改的 Scope IN 與 Scope OUT 邊界？ 確認後，請產出 L1 現況與需求報告。」

Step 2: 微觀防禦 (Gemini)
目的：說明此步驟的目的（嚴格架構審查、防禦邊界條件）。
建議提示詞：提供給 Gemini 的指令。內容必須包含：「你現在是一位擁有 10 年經驗的頂尖 Android 架構師。這是 Traycer 產出的 L1 需求：[貼上 L1]。請對此進行『嚴格的技術設計審查』。請根據：1. 軟硬體能力矩陣 2. Sealed Class 狀態機防禦 3. UDF 解耦 4. 破壞性邊界條件，來拷問這個需求。審查完畢後，產出一份給 Claude Code 參考的英文 `Claude_Implementation_Guide_L2.md` 防禦架構指南。」

Step 3: 匯入與排程 (Traycer Planner)
目的：說明此步驟的目的（讓 Traycer 吸收 L2 架構，並進入 Planner Mode 拆解票據與產生交接指令）。
建議提示詞：提供給 Traycer 的指令。內容必須包含：「請執行 `architectural_review`。這是我準備好的 L2 防禦架構指南：[貼上 Gemini 產出的 L2]。請吸收這份指南並產出正式的 Tech Plan Spec。確認無誤後，請接續執行 `ticket_breakdown` 將其拆解為 Scoped Tickets，最後啟動 `execute` 為我產出第一批要交接給 Claude Code 的【精確交接指令 (Handoff Instructions)】。」

Step 4: 實作落地與驗收閉環 (Claude & Traycer)
目的：說明此步驟的目的（交接給 Claude 實作，完成後用 Traycer 的 Reviewer Mode 驗收）。
建議提示詞：提供實作與驗收的雙重指令。內容必須包含：
「【第一步：貼給 Claude Code】
請根據以下 Traycer 的交接指令執行任務：[貼上 Traycer 產出的 Handoff Instructions]。請嚴格遵守 Ticket 範圍與 Acceptance Criteria，絕不可進行大重寫。實作並編譯無誤後，請立即暫停並回報。
【第二步：Claude 完成後，貼回給 Traycer】
Claude Code 已完成實作。請執行 `strict_verification`。請嚴格比對 Tech Plan 與實際代碼，檢查狀態機制是否如實建立？是否有 Implementation drift (代碼偏移)？請給出最終裁決：Path A (批准), Path B (開立 Fix Tickets) 或 Path C (升級重新規劃)。」
"""


# ── Main Application ─────────────────────────────────────────────────────────
class PromptGeneratorApp:

    def __init__(self, root: tk.Tk):
        self.root = root
        self.root.title("Ultimate Defensive Hybrid Workflow — 提示詞生成器")
        self.root.geometry("1050x820")
        self.root.minsize(850, 640)
        self.root.configure(bg=Colors.BACKGROUND)
        self.root.resizable(True, True)

        self._setup_ui()
        self.write_log("🤖 終極防禦工作流 Generator (自訂工作流版) 已啟動", "Success")

        if requests is None:
            self.write_log("❌ 缺少 requests 套件，請執行：pip install requests", "Error")
        if not os.getenv("GEMINI_API_KEY", ""):
            self.write_log("⚠️ 未偵測到 GEMINI_API_KEY 環境變數，請在 API Key 欄位手動輸入", "Warning")
        else:
            self.write_log("✅ 已從環境變數載入 GEMINI_API_KEY", "Success")
            self.api_key_var.set(os.getenv("GEMINI_API_KEY"))

    # ── UI Setup ─────────────────────────────────────────────────────────────

    def _setup_ui(self):
        self._create_header()
        self._create_input_section()
        self._create_output_tabs()
        self._create_log_section()
        self._create_statusbar()

    def _create_header(self):
        header = tk.Frame(self.root, bg=Colors.TEXT_DARK, height=52)
        header.pack(fill=tk.X)
        header.pack_propagate(False)

        tk.Label(
            header,
            text="🛡️  終極防禦型混合工作流 — 提示詞生成器 (Custom Workflow 版)",
            font=("Microsoft JhengHei UI", 14, "bold"),
            fg=Colors.TEXT_LIGHT, bg=Colors.TEXT_DARK
        ).pack(side=tk.LEFT, padx=20, pady=13)

        tk.Label(
            header,
            text=f"Model: {MODEL_NAME}  •  Trigger → Design → Planner → Code/Verify",
            font=("Microsoft JhengHei UI", 9),
            fg="#bdc3c7", bg=Colors.TEXT_DARK
        ).pack(side=tk.LEFT, padx=(0, 20), pady=13)

    def _create_input_section(self):
        frame = tk.LabelFrame(
            self.root, text=" 📝 輸入問題或重構需求 ",
            font=("Microsoft JhengHei UI", 10, "bold"),
            bg=Colors.CARD_BG, fg=Colors.TEXT_DARK,
            relief=tk.RIDGE, bd=1
        )
        frame.pack(padx=15, pady=(8, 4), fill=tk.X)

        # API Key row
        key_row = tk.Frame(frame, bg=Colors.CARD_BG)
        key_row.pack(padx=10, pady=(8, 2), fill=tk.X)

        tk.Label(
            key_row, text="API Key:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG, width=9, anchor=tk.W
        ).pack(side=tk.LEFT)

        self.api_key_var = tk.StringVar()
        self._key_entry = tk.Entry(
            key_row, textvariable=self.api_key_var,
            font=("Consolas", 9), show="*", width=50
        )
        self._key_entry.pack(side=tk.LEFT, padx=(0, 5))

        self._show_key = tk.BooleanVar(value=False)
        tk.Checkbutton(
            key_row, text="顯示",
            variable=self._show_key,
            command=self._toggle_key_visibility,
            font=("Microsoft JhengHei UI", 8),
            bg=Colors.CARD_BG
        ).pack(side=tk.LEFT)

        tk.Label(
            key_row,
            text="  (留空則使用 GEMINI_API_KEY 環境變數)",
            font=("Microsoft JhengHei UI", 8),
            fg="gray", bg=Colors.CARD_BG
        ).pack(side=tk.LEFT)

        # Version + Topic row
        ver_row = tk.Frame(frame, bg=Colors.CARD_BG)
        ver_row.pack(padx=10, pady=(2, 2), fill=tk.X)

        tk.Label(
            ver_row, text="版本:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG, width=9, anchor=tk.W
        ).pack(side=tk.LEFT)

        self.version_var = tk.StringVar(value="v2.12.0")
        tk.Entry(
            ver_row, textvariable=self.version_var,
            font=("Consolas", 9), width=12
        ).pack(side=tk.LEFT, padx=(0, 12))

        tk.Label(
            ver_row, text="檔名 (自動):",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG
        ).pack(side=tk.LEFT)

        self.topic_var = tk.StringVar()
        tk.Entry(
            ver_row, textvariable=self.topic_var,
            font=("Consolas", 9), width=32
        ).pack(side=tk.LEFT, padx=(0, 5))

        tk.Label(
            ver_row,
            text="  (由 AI 依需求自動生成，可手動覆蓋)",
            font=("Microsoft JhengHei UI", 8),
            fg="gray", bg=Colors.CARD_BG
        ).pack(side=tk.LEFT)

        # Problem description
        desc_row = tk.Frame(frame, bg=Colors.CARD_BG)
        desc_row.pack(padx=10, pady=(4, 4), fill=tk.X)

        tk.Label(
            desc_row, text="需求描述:",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG, width=9, anchor=tk.NW
        ).pack(side=tk.LEFT, anchor=tk.N)

        self.input_text = scrolledtext.ScrolledText(
            desc_row, height=4,
            font=("Microsoft JhengHei UI", 10),
            wrap=tk.WORD
        )
        self.input_text.pack(side=tk.LEFT, fill=tk.X, expand=True)
        self.input_text.insert(tk.END, "例如：重構相機對焦模組，解決前鏡頭 Fixed-Focus 對焦框不動的 Bug A，以及後鏡頭切換目標的 Bug B，並導入 Sealed Class 防禦。")

        # Buttons
        btn_row = tk.Frame(frame, bg=Colors.CARD_BG)
        btn_row.pack(padx=10, pady=(4, 10), fill=tk.X)

        self._gen_btn = self._create_button(
            btn_row, "🚀 生成閉環防禦提示詞",
            Colors.PRIMARY, self._on_generate
        )
        self._gen_btn.pack(side=tk.LEFT, padx=(0, 8))

        self._create_button(
            btn_row, "🗑️ 清除輸入",
            Colors.BORDER, self._clear_input,
            fg=Colors.TEXT_DARK
        ).pack(side=tk.LEFT, padx=(0, 8))

        self._create_button(
            btn_row, "🧹 清除所有輸出",
            Colors.BORDER, self._clear_outputs,
            fg=Colors.TEXT_DARK
        ).pack(side=tk.LEFT)

    def _create_output_tabs(self):
        outer = tk.LabelFrame(
            self.root, text=" 📋 四部曲提示詞 (自訂工作流適配版) ",
            font=("Microsoft JhengHei UI", 10, "bold"),
            bg=Colors.CARD_BG, fg=Colors.TEXT_DARK,
            relief=tk.RIDGE, bd=1
        )
        outer.pack(padx=15, pady=4, fill=tk.BOTH, expand=True)

        self.notebook = ttk.Notebook(outer)
        self.notebook.pack(padx=5, pady=5, fill=tk.BOTH, expand=True)

        self._step_texts = {}

        for step in STEPS:
            tab_frame = tk.Frame(self.notebook, bg=Colors.CARD_BG)
            self.notebook.add(tab_frame, text=f"  {step['label']}  ")

            # Header row inside tab
            info_row = tk.Frame(tab_frame, bg=step["color"], height=30)
            info_row.pack(fill=tk.X)
            info_row.pack_propagate(False)

            tk.Label(
                info_row,
                text=f"  🛡️ {step['label']}  │  Tool: {step['tool']}",
                font=("Microsoft JhengHei UI", 9, "bold"),
                fg=Colors.TEXT_LIGHT, bg=step["color"]
            ).pack(side=tk.LEFT, padx=8, pady=5)

            # Copy button
            step_key = step["key"]
            copy_btn = tk.Button(
                info_row,
                text="📋 複製",
                font=("Microsoft JhengHei UI", 8),
                bg=Colors.TEXT_LIGHT, fg=step["color"],
                relief=tk.FLAT, cursor="hand2",
                padx=6, pady=2,
                command=lambda k=step_key: self._copy_step(k)
            )
            copy_btn.pack(side=tk.RIGHT, padx=8, pady=4)

            # Content text area
            txt = scrolledtext.ScrolledText(
                tab_frame,
                font=("Consolas", 10),
                bg="#fafafa",
                fg=Colors.TEXT_DARK,
                wrap=tk.WORD,
                state=tk.DISABLED
            )
            txt.pack(fill=tk.BOTH, expand=True, padx=4, pady=4)
            txt.tag_config("header", font=("Microsoft JhengHei UI", 10, "bold"), foreground=step["color"])

            self._step_texts[step_key] = txt

    def _create_log_section(self):
        log_frame = tk.LabelFrame(
            self.root, text=" 🖥️ 執行日誌 ",
            font=("Microsoft JhengHei UI", 10, "bold"),
            bg=Colors.CARD_BG, fg=Colors.TEXT_DARK,
            relief=tk.RIDGE, bd=1
        )
        log_frame.pack(padx=15, pady=(4, 4), fill=tk.X)

        self.log_text = scrolledtext.ScrolledText(
            log_frame, height=5,
            font=("Consolas", 9),
            bg="#2c3e50", fg="#ecf0f1",
            insertbackground="white",
            wrap=tk.WORD
        )
        self.log_text.pack(padx=5, pady=5, fill=tk.X)
        self.log_text.tag_config("Success", foreground="#2ecc71")
        self.log_text.tag_config("Error",   foreground="#e74c3c")
        self.log_text.tag_config("Warning", foreground="#f39c12")
        self.log_text.tag_config("Info",    foreground="#3498db")

    def _create_statusbar(self):
        self._statusbar = tk.Label(
            self.root, text="就緒",
            bd=1, relief=tk.SUNKEN, anchor=tk.W,
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG, fg=Colors.TEXT_DARK
        )
        self._statusbar.pack(side=tk.BOTTOM, fill=tk.X)

    # ── Helpers ───────────────────────────────────────────────────────────────

    def _create_button(self, parent, text, bg_color, command, fg=Colors.TEXT_LIGHT, width=None):
        return tk.Button(
            parent, text=text,
            font=("Microsoft JhengHei UI", 9),
            bg=bg_color, fg=fg,
            activebackground=bg_color, activeforeground=fg,
            relief=tk.FLAT, cursor="hand2", command=command,
            padx=10, pady=7, width=width
        )

    def write_log(self, message: str, level: str = "Info"):
        ts = datetime.datetime.now().strftime("%H:%M:%S")
        self.log_text.insert(tk.END, f"[{ts}] {message}\n", level)
        self.log_text.see(tk.END)
        self.log_text.update()

    def _update_status(self, message: str):
        self._statusbar.config(text=message)
        self._statusbar.update()

    def _toggle_key_visibility(self):
        self._key_entry.config(show="" if self._show_key.get() else "*")

    def _clear_input(self):
        self.input_text.delete("1.0", tk.END)

    def _clear_outputs(self):
        for txt in self._step_texts.values():
            txt.config(state=tk.NORMAL)
            txt.delete("1.0", tk.END)
            txt.config(state=tk.DISABLED)
        self.write_log("🗑️ 已清除所有輸出", "Info")

    def _copy_step(self, step_key: str):
        txt = self._step_texts.get(step_key)
        if txt:
            content = txt.get("1.0", tk.END).strip()
            if content:
                self.root.clipboard_clear()
                self.root.clipboard_append(content)
                self.write_log(f"📋 已複製 {step_key} 內容到剪貼簿", "Success")

    def _set_step_content(self, step_key: str, content: str):
        txt = self._step_texts.get(step_key)
        if not txt:
            return
        txt.config(state=tk.NORMAL)
        txt.delete("1.0", tk.END)
        if content:
            txt.insert(tk.END, content)
        txt.config(state=tk.DISABLED)

    # ── Path Computation & File Creation ─────────────────────────────────────

    def _compute_paths(self) -> dict:
        version     = self.version_var.get().strip() or "v0.0.0"
        dir_version = re.sub(r'-\d+$', '', version)
        topic       = self.topic_var.get().strip()[:30]
        topic       = re.sub(r'[\\/*?:"<>|]', '_', topic).replace(' ', '_')
        base        = f"{version}_{topic}" if topic else version
        rel_dir     = Path("docs") / "features" / dir_version
        return {
            "abs_dir": PROJECT_ROOT / rel_dir,
            "L1": rel_dir / f"{base}-L1_Context.md",
            "L2": rel_dir / f"Claude_Implementation_Guide_{base}_L2.md",
        }

    def _create_doc_files(self, paths: dict):
        try:
            paths["abs_dir"].mkdir(parents=True, exist_ok=True)
            for key in ("L1", "L2"):
                abs_fp = PROJECT_ROOT / paths[key]
                if not abs_fp.exists():
                    abs_fp.write_text("", encoding="utf-8")
            rel = paths["abs_dir"].relative_to(PROJECT_ROOT).as_posix()
            self.write_log(f"📁 已確保規劃書目錄存在：{rel}", "Success")
        except Exception as exc:
            self.write_log(f"⚠️ 確保規劃書檔案失敗：{exc}", "Warning")

    def _postprocess_step(self, step_key: str, content: str, paths: dict) -> str:
        content = content.replace("建議提示詞：", "").replace("建議提示詞:", "")
        fwd = lambda p: p.as_posix()

        if step_key == "Step 1":
            content = content.rstrip() + f"\n\n[💡 系統建議] 請要求 Traycer 將輸出的 L1 現況報告儲存為：\n@{fwd(paths['L1'])}"
        elif step_key == "Step 2":
            content = content.rstrip() + f"\n\n[💡 系統建議] 請提供 @{fwd(paths['L1'])} 讓 Gemini 審查，並要求其產出：\n@{fwd(paths['L2'])}"
        elif step_key == "Step 3":
            content = content.rstrip() + f"\n\n[💡 系統建議] 將 @{fwd(paths['L2'])} 餵給 Traycer，讓其自動執行審查 (`architectural_review`)、拆解票據 (`ticket_breakdown`)，並產出給 Claude 的交接指令 (`execute`)。"
        elif step_key == "Step 4":
            content = content.rstrip() + f"\n\n[💡 系統建議] 第一步讓 Claude 依循 Handoff 指令實作；第二步在 Traycer 啟動 `strict_verification` 驗收。若通過 (Path A)，再寫入 feature_record_Backlog.md。"
        return content

    # ── API Call & Parsing ────────────────────────────────────────────────────

    def _on_generate(self):
        query = self.input_text.get("1.0", tk.END).strip()
        if not query:
            messagebox.showwarning("警告", "請輸入需求描述。")
            return

        api_key = self.api_key_var.get().strip() or os.getenv("GEMINI_API_KEY", "")
        if not api_key:
            messagebox.showerror("錯誤", "未設定 API Key。")
            return

        if requests is None:
            messagebox.showerror("錯誤", "缺少 requests 套件。")
            return

        self._gen_btn.config(state=tk.DISABLED, text="⏳ 生成防禦提示詞中…")
        self._update_status("呼叫 Gemini API 進行架構推理中…")
        self._clear_outputs()

        threading.Thread(target=self._call_api, args=(query, api_key), daemon=True).start()

    def _call_api(self, query: str, api_key: str):
        payload = {
            "contents": [{"parts": [{"text": f"開發需求描述：{query}\n\n請根據以上需求，生成終極防禦型混合工作流 (適配 Traycer Custom Workflow 版) 的四步驟提示詞。"}]}],
            "systemInstruction": {"parts": [{"text": SYSTEM_INSTRUCTION}]}
        }
        try:
            response_text = self._call_with_retry(payload, api_key)
            steps, filename = self._parse_steps(response_text)
            self.root.after(0, self._on_api_success, steps, response_text, filename)
        except Exception as exc:
            self.root.after(0, self._on_api_error, str(exc))

    def _call_with_retry(self, payload: dict, api_key: str, retries: int = 5) -> str:
        import time
        for attempt in range(retries):
            try:
                url = f"{API_URL}?key={api_key}"
                resp = requests.post(url, json=payload, timeout=45)
                data = resp.json()
                if resp.status_code == 200:
                    return data["candidates"][0]["content"]["parts"][0]["text"]
                if resp.status_code == 429:
                    time.sleep(2 ** attempt)
                    continue
                raise Exception(f"API 錯誤 ({resp.status_code})")
            except requests.exceptions.RequestException as exc:
                if attempt == retries - 1: raise exc
                time.sleep(2 ** attempt)
        raise Exception("重試失敗")

    def _parse_steps(self, text: str) -> tuple:
        filename = ""
        fn_match = re.search(r'^FileName:\s*(\S+)', text, re.IGNORECASE | re.MULTILINE)
        if fn_match:
            filename = re.sub(r'[\\/*?:"<>|]', '_', fn_match.group(1).strip())[:30]

        result = {}
        parts = re.split(r'(?=Step\s+\d+\s*:)', text, flags=re.IGNORECASE)
        for part in parts:
            match = re.match(r'Step\s+(\d+)\s*:', part.strip(), re.IGNORECASE)
            if match:
                result[f"Step {match.group(1)}"] = part.strip()
        return result, filename

    def _on_api_success(self, steps: dict, raw: str, filename: str = ""):
        if filename: self.topic_var.set(filename)
        paths = self._compute_paths()
        self._create_doc_files(paths)

        for step in STEPS:
            key = step["key"]
            content = steps.get(key, f"（解析失敗）\n{raw}")
            self._set_step_content(key, self._postprocess_step(key, content, paths))

        self.write_log("✅ 生成完成", "Success")
        self._update_status("✅ 完成")
        self._gen_btn.config(state=tk.NORMAL, text="🚀 生成閉環防禦提示詞")
        self._save_log(paths)

        for idx, step in enumerate(STEPS):
            if steps.get(step["key"]):
                self.notebook.select(idx)
                break

    def _save_log(self, paths: dict):
        try:
            log_path = PROJECT_ROOT / "tmp" / "prompt_generator_gui.log"
            log_path.parent.mkdir(parents=True, exist_ok=True)
            lines = [f"# Workflow Log\n# Topic: {self.topic_var.get()}"]
            for step in STEPS:
                txt = self._step_texts.get(step["key"])
                lines += [f"## {step['label']}", txt.get("1.0", tk.END).strip() if txt else "", ""]
            log_path.write_text("\n".join(lines), encoding="utf-8")
        except Exception:
            pass

    def _on_api_error(self, error_msg: str):
        self.write_log(f"❌ 失敗：{error_msg}", "Error")
        self._update_status("❌ 生成失敗")
        self._gen_btn.config(state=tk.NORMAL, text="🚀 生成閉環防禦提示詞")
        messagebox.showerror("錯誤", error_msg)

if __name__ == "__main__":
    root = tk.Tk()
    PromptGeneratorApp(root)
    root.mainloop()
