import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext
import subprocess
import os
import sys
import datetime
from typing import List, Optional

# 設定預設編碼為 UTF-8
sys.stdout.reconfigure(encoding='utf-8')

class TestHandoffApp:
    def __init__(self, root):
        self.root = root
        self.root.title("測試交接管理員 (Test Handoff Manager)")
        self.root.geometry("800x700")
        
        # 設定樣式
        self.style = ttk.Style()
        self.style.configure("Title.TLabel", font=("Microsoft JhengHei", 14, "bold"))
        self.style.configure("Header.TLabel", font=("Microsoft JhengHei", 10, "bold"))
        self.style.configure("TButton", font=("Microsoft JhengHei", 10))
        self.style.configure("TLabel", font=("Microsoft JhengHei", 10))
        
        self.create_widgets()
        
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
        self.topic_var = tk.StringVar(value="v2.9.6_phase1")
        ttk.Entry(input_frame, textvariable=self.topic_var, width=40).grid(row=0, column=1, sticky=tk.W, padx=5, pady=5)

        # 測試裝置 (Device)
        ttk.Label(input_frame, text="測試裝置 (Device):").grid(row=1, column=0, sticky=tk.W, padx=5, pady=5)
        self.device_var = tk.StringVar(value="Pixel 9a")
        device_combo = ttk.Combobox(input_frame, textvariable=self.device_var, width=37)
        device_combo['values'] = ('Pixel 9a', 'Sony XQ-BT52', 'Emulator', 'Generic Device')
        device_combo.grid(row=1, column=1, sticky=tk.W, padx=5, pady=5)

        # 測試目標 (Objective)
        ttk.Label(input_frame, text="測試目標/結果說明:").grid(row=2, column=0, sticky=tk.NW, padx=5, pady=5)
        self.objective_text = tk.Text(input_frame, height=3, width=40, font=("Microsoft JhengHei", 9))
        self.objective_text.insert("1.0", "Verify OrientationEventListener fix and photo EXIF orientation")
        self.objective_text.grid(row=2, column=1, sticky=tk.W, padx=5, pady=5)

        # 頁籤區域 (主要操作)
        self.notebook = ttk.Notebook(main_frame)
        self.notebook.pack(fill=tk.BOTH, expand=True, pady=10)

        # 建立四個分頁
        self.create_step1_tab()
        self.create_step2_tab()
        self.create_step3_tab()
        self.create_step4_tab()

        # Log 區域
        log_frame = ttk.LabelFrame(main_frame, text="📜 執行日誌 (Logs)", padding="5")
        log_frame.pack(fill=tk.BOTH, expand=True)
        
        self.log_area = scrolledtext.ScrolledText(log_frame, height=10, state='disabled', font=("Consolas", 9))
        self.log_area.pack(fill=tk.BOTH, expand=True)

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

    def log(self, message):
        self.log_area.config(state='normal')
        self.log_area.insert(tk.END, f"{message}\n")
        self.log_area.see(tk.END)
        self.log_area.config(state='disabled')
        self.root.update_idletasks()

    def run_command(self, commands: List[str]):
        try:
            # 確保使用 UTF-8
            process = subprocess.Popen(
                commands,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                encoding='utf-8',
                errors='replace',
                creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
            )
            
            while True:
                output = process.stdout.readline()
                if output == '' and process.poll() is not None:
                    break
                if output:
                    self.log(output.strip())
            
            return process.poll()
        except Exception as e:
            self.log(f"❌ 執行錯誤: {str(e)}")
            return -1

    def run_git_status(self):
        self.log("\n--- 🔍 Checking Git Status ---")
        self.run_command(["git", "status", "--short"])

    def run_step1(self):
        if not messagebox.askyesno("確認", "確定要推送到 GitHub (main) 嗎？"):
            return

        topic = self.topic_var.get()
        device = self.device_var.get()
        objective = self.objective_text.get("1.0", tk.END).strip()

        self.log(f"\n--- 🚀 Starting Task 1: Home NB (Push) ---")
        
        # Git Add
        self.log("Step 1: Adding files...")
        self.run_command(["git", "add", "docs/test_handoff/"])
        
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
        self.run_command(commit_msg)
        
        # Git Push
        self.log("Step 3: Pushing to GitHub...")
        if self.run_command(["git", "push", "github", "main"]) == 0:
            self.log("\n✅ 成功！請切換到測試機執行步驟 2。")
            messagebox.showinfo("成功", "推送成功！\n\n下一步：請切換到 Work NB 執行步驟 2 (Pull)。")
        else:
            self.log("\n❌ 推送失敗，請檢查日誌。")

    def run_step2(self):
        self.log(f"\n--- 📥 Starting Task 2: Work NB (Pull) ---")
        
        if self.run_command(["git", "pull", "origin", "main"]) == 0:
            self.log("\n✅ 同步成功！可用測試文檔：")
            self.run_command(["powershell", "-Command", "Get-ChildItem docs/test_handoff/*.md | Select-Object Name"])
            messagebox.showinfo("成功", "同步完成！\n\n下一步：請叫出 Claude Code 執行測試。")
        else:
            self.log("\n❌ 同步失敗。")

    def run_step3(self):
        if not messagebox.askyesno("確認", "確定要推送測試結果嗎？"):
            return

        topic = self.topic_var.get()
        device = self.device_var.get()
        result = self.result_var.get()
        date_str = datetime.datetime.now().strftime("%Y-%m-%d")

        self.log(f"\n--- 🚀 Starting Task 3: Work NB (Push) ---")
        
        self.log("Step 1: Adding test results...")
        self.run_command(["git", "add", "docs/test_results/"])
        
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
        self.run_command(commit_msg)
        
        self.log("Step 3: Pushing to GitHub...")
        if self.run_command(["git", "push", "origin", "main"]) == 0:
            self.log("\n✅ 報告推送成功！")
            messagebox.showinfo("成功", "報告已上傳！\n\n下一步：請切換回 Home NB 執行步驟 4 (Pull)。")
        else:
            self.log("\n❌ 推送失敗。")

    def run_step4(self):
        self.log(f"\n--- 📥 Starting Task 4: Home NB (Pull) ---")
        
        if self.run_command(["git", "pull", "origin", "main"]) == 0:
            self.log("\n✅ 下載完成。正在尋找最新報告...")
            # 使用 PowerShell 尋找並打開最新報告
            cmd = "Get-ChildItem docs/test_results/*.md -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1 | ForEach-Object { Write-Host 'Opening: ' $_.FullName; code $_.FullName }"
            self.run_command(["powershell", "-Command", cmd])
        else:
            self.log("\n❌ 下載失敗。")

if __name__ == "__main__":
    root = tk.Tk()
    # 設定圖示 (如果有的話)
    # root.iconbitmap('icon.ico') 
    app = TestHandoffApp(root)
    root.mainloop()
