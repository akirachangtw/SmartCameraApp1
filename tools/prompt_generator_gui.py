#!/usr/bin/env python3

import os
import json
import requests
import tkinter as tk
from tkinter import scrolledtext, messagebox

# --- 配置區 ---
# 支援的模型名稱
MODEL_NAME = "gemini-2.5-flash"
API_URL = f"https://generativelanguage.googleapis.com/v1beta/models/{MODEL_NAME}:generateContent"

class WorkflowApp:
    def __init__(self, root):
        self.root = root
        self.root.title("AI 智能攝影師 - Hybrid Workflow 提示詞生成器")
        self.root.geometry("900x700")
        
        # 讀取環境變數
        self.api_key = os.getenv("GEMINI_API_KEY", "")
        
        self.setup_ui()

    def setup_ui(self):
        # 輸入區標籤
        tk.Label(self.root, text="請輸入問題或功能敘述 (例如：長按 AF/AE Lock 後沒有對焦動作):", font=("Arial", 12, "bold")).pack(pady=10)
        
        # 輸入文本框
        self.input_text = scrolledtext.ScrolledText(self.root, height=8, font=("Arial", 11))
        self.input_text.pack(padx=20, pady=5, fill=tk.X)
        
        # 生成按鈕
        self.gen_button = tk.Button(
            self.root, 
            text="自動生成各 Step 提示詞", 
            command=self.generate_prompts,
            bg="#00F2FF", 
            fg="black",
            font=("Arial", 11, "bold"),
            height=2
        )
        self.gen_button.pack(pady=15)
        
        # 輸出區標籤
        tk.Label(self.root, text="產出的工作流提示詞 (Step 0 - Step 3):", font=("Arial", 12, "bold")).pack(pady=5)
        
        # 輸出文本框
        self.output_text = scrolledtext.ScrolledText(self.root, height=20, font=("Consolas", 10), bg="#f4f4f4")
        self.output_text.pack(padx=20, pady=5, fill=tk.BOTH, expand=True)

    def generate_prompts(self):
        user_query = self.input_text.get("1.0", tk.END).strip()
        
        if not self.api_key:
            messagebox.showerror("錯誤", "找不到環境變數 GEMINI_API_KEY，請先設定。")
            return
        
        if not user_query:
            messagebox.showwarning("警告", "請輸入問題敘述。")
            return

        self.gen_button.config(state=tk.DISABLED, text="生成中...")
        self.output_text.delete("1.0", tk.END)
        self.output_text.insert(tk.END, "正在調用 Gemini API 分析並生成提示詞...\n")
        self.root.update()

        # 設定 System Instruction 與 Prompt
        system_instruction = """
        你是一位開發工作流架構師。根據使用者的問題，生成 4 個步驟的 Hybrid Workflow 提示詞。
        格式要求如下：
        
        Step 0: 探索與定位 (Traycer)
        目的：說明此步驟的目的。
        建議提示詞：提供給 Traycer 的具體英文提示詞。
        
        Step 1: L1 規劃摘要 (OpenAI/Gemini)
        目的：說明此步驟的目的。
        建議提示詞：具體的 L1 生成提示詞。
        
        Step 2: L2 可行性檢視 (Gemini)
        目的：說明此步驟的目的。
        建議提示詞：具體的 L2 檢視提示詞。
        
        Step 3: Claude Code 終端落地 (Claude Code)
        目的：說明此步驟的目的。
        建議提示詞：具體的 CLI 執行指令。
        """

        payload = {
            "contents": [{
                "parts": [{
                    "text": f"問題敘述：{user_query}\n\n請根據以上問題生成優化後的工作流提示詞。"
                }]
            }],
            "systemInstruction": {
                "parts": [{
                    "text": system_instruction
                }]
            }
        }

        try:
            # 實作指數退避重試邏輯
            response_text = self.call_gemini_with_retry(payload)
            self.output_text.delete("1.0", tk.END)
            self.output_text.insert(tk.END, response_text)
        except Exception as e:
            messagebox.showerror("API 錯誤", f"發生錯誤: {str(e)}")
            self.output_text.delete("1.0", tk.END)
        finally:
            self.gen_button.config(state=tk.NORMAL, text="自動生成各 Step 提示詞")

    def call_gemini_with_retry(self, payload):
        import time
        retries = 5
        for i in range(retries):
            try:
                url_with_key = f"{API_URL}?key={self.api_key}"
                response = requests.post(url_with_key, json=payload, timeout=30)
                result = response.json()
                
                if response.status_code == 200:
                    return result['candidates'][0]['content']['parts'][0]['text']
                else:
                    error_msg = result.get('error', {}).get('message', '未知錯誤')
                    if response.status_code == 429: # Rate limit
                        time.sleep(2 ** i)
                        continue
                    raise Exception(f"API 回傳錯誤 ({response.status_code}): {error_msg}")
            except requests.exceptions.RequestException as e:
                if i == retries - 1: raise e
                time.sleep(2 ** i)
        return "無法取得回應。"

if __name__ == "__main__":
    root = tk.Tk()
    app = WorkflowApp(root)
    root.mainloop()