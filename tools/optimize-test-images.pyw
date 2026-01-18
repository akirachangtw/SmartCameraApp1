#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
optimize-test-images.pyw
Image Optimization Tool with GUI
優化測試圖片 - 調整尺寸並壓縮為 JPEG (品質 85)
"""

import tkinter as tk
from tkinter import ttk, scrolledtext, filedialog, messagebox
from PIL import Image, ImageTk
import os
import threading
from datetime import datetime
from pathlib import Path


class Colors:
    """Color scheme"""
    PRIMARY = "#2980b9"
    SUCCESS = "#27ae60"
    WARNING = "#f39c12"
    DANGER = "#e74c3c"
    BACKGROUND = "#ecf0f1"
    CARD_BG = "#ffffff"
    TEXT_DARK = "#2c3e50"
    TEXT_LIGHT = "#ffffff"
    BORDER = "#bdc3c7"


class ImageOptimizer:
    """Image optimization core logic"""

    def __init__(self):
        self.portrait_max = (1080, 1920)  # Portrait max dimensions
        self.landscape_max = (1920, 1080)  # Landscape max dimensions
        self.jpeg_quality = 85

    def optimize_image(self, source_path: str, target_path: str, callback=None) -> dict:
        """
        Optimize single image

        Args:
            source_path: Source image path
            target_path: Target image path
            callback: Progress callback function

        Returns:
            Dict with success status and info
        """
        try:
            # Open image
            img = Image.open(source_path)
            original_size = img.size
            original_width, original_height = original_size

            if callback:
                callback(f"原始尺寸: {original_width}x{original_height}")

            # Determine orientation and calculate target size
            aspect_ratio = original_width / original_height

            if original_height > original_width:
                # Portrait
                max_width, max_height = self.portrait_max
            else:
                # Landscape
                max_width, max_height = self.landscape_max

            # Calculate target dimensions maintaining aspect ratio
            if original_width > max_width or original_height > max_height:
                # Need to resize
                width_ratio = max_width / original_width
                height_ratio = max_height / original_height
                scale_ratio = min(width_ratio, height_ratio)

                target_width = int(original_width * scale_ratio)
                target_height = int(original_height * scale_ratio)
            else:
                # No resize needed, but still compress
                target_width = original_width
                target_height = original_height

            # Resize with high quality
            if (target_width, target_height) != original_size:
                img = img.resize(
                    (target_width, target_height),
                    Image.Resampling.LANCZOS  # High quality resampling
                )

            # Convert to RGB if necessary (for PNG with transparency)
            if img.mode in ('RGBA', 'LA', 'P'):
                # Create white background
                background = Image.new('RGB', img.size, (255, 255, 255))
                if img.mode == 'P':
                    img = img.convert('RGBA')
                if img.mode in ('RGBA', 'LA'):
                    background.paste(img, mask=img.split()[-1])  # Use alpha channel as mask
                    img = background
                else:
                    img = img.convert('RGB')
            elif img.mode != 'RGB':
                img = img.convert('RGB')

            # Save as JPEG with quality 85
            img.save(target_path, 'JPEG', quality=self.jpeg_quality, optimize=True)

            # Get file sizes
            original_file_size = os.path.getsize(source_path)
            target_file_size = os.path.getsize(target_path)
            compression_ratio = (1 - target_file_size / original_file_size) * 100

            return {
                'success': True,
                'original_size': original_size,
                'target_size': (target_width, target_height),
                'original_file_size': original_file_size,
                'target_file_size': target_file_size,
                'compression_ratio': compression_ratio
            }

        except Exception as e:
            return {
                'success': False,
                'error': str(e)
            }


class ImageOptimizerGUI:
    """Image Optimizer GUI Application"""

    def __init__(self, root: tk.Tk):
        self.root = root
        self.optimizer = ImageOptimizer()
        self.source_dir = None
        self.target_dir = None
        self.is_processing = False

        # Configure root window
        self.root.title("圖片優化工具")
        self.root.geometry("850x650")
        self.root.resizable(False, False)
        self.root.configure(bg=Colors.BACKGROUND)

        # Setup GUI
        self.setup_ui()

    def setup_ui(self):
        """Setup user interface"""
        self.create_header()
        self.create_config_section()
        self.create_settings_section()
        self.create_log_section()
        self.create_bottom_buttons()
        self.create_statusbar()

    def create_header(self):
        """Create header panel"""
        header = tk.Frame(self.root, bg=Colors.PRIMARY, height=60)
        header.pack(fill=tk.X)
        header.pack_propagate(False)

        title_label = tk.Label(
            header,
            text="🖼️  圖片優化工具",
            font=("Microsoft JhengHei UI", 16, "bold"),
            fg=Colors.TEXT_LIGHT,
            bg=Colors.PRIMARY
        )
        title_label.pack(side=tk.LEFT, padx=20, pady=15)

        subtitle_label = tk.Label(
            header,
            text="調整尺寸 • 壓縮檔案 • 批次處理",
            font=("Microsoft JhengHei UI", 9),
            fg=Colors.TEXT_LIGHT,
            bg=Colors.PRIMARY
        )
        subtitle_label.pack(side=tk.LEFT, padx=(0, 20), pady=15)

    def create_config_section(self):
        """Create directory configuration section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 目錄設定 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=20, pady=(15, 5), fill=tk.X)

        # Source directory
        src_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        src_frame.pack(pady=(10, 5), padx=15, fill=tk.X)

        tk.Label(
            src_frame,
            text="來源目錄：",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            width=10,
            anchor=tk.W
        ).pack(side=tk.LEFT)

        self.source_dir_var = tk.StringVar(value="未選擇")
        tk.Label(
            src_frame,
            textvariable=self.source_dir_var,
            font=("Consolas", 9),
            bg=Colors.CARD_BG,
            fg="gray",
            anchor=tk.W
        ).pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5)

        self.create_button(
            src_frame,
            "📁 選擇目錄",
            Colors.PRIMARY,
            self.select_source_directory,
            width=12
        ).pack(side=tk.RIGHT)

        # Target directory (auto)
        tgt_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        tgt_frame.pack(pady=(5, 10), padx=15, fill=tk.X)

        tk.Label(
            tgt_frame,
            text="輸出目錄：",
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            width=10,
            anchor=tk.W
        ).pack(side=tk.LEFT)

        self.target_dir_var = tk.StringVar(value="[來源目錄]/optimize/")
        tk.Label(
            tgt_frame,
            textvariable=self.target_dir_var,
            font=("Consolas", 9),
            bg=Colors.CARD_BG,
            fg="#27ae60",
            anchor=tk.W
        ).pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5)

    def create_settings_section(self):
        """Create settings section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 優化設定 ",
            font=("Microsoft JhengHei UI", 9, "bold"),
            bg=Colors.CARD_BG,
            fg=Colors.TEXT_DARK,
            relief=tk.RIDGE,
            bd=1
        )
        frame.pack(padx=20, pady=5, fill=tk.X)

        settings_frame = tk.Frame(frame, bg=Colors.CARD_BG)
        settings_frame.pack(pady=10, padx=15)

        # Settings info
        info_text = (
            "📐 直向圖片：最大 1080×1920  |  "
            "📐 橫向圖片：最大 1920×1080  |  "
            "🎨 JPEG 品質：85"
        )
        tk.Label(
            settings_frame,
            text=info_text,
            font=("Microsoft JhengHei UI", 9),
            bg=Colors.CARD_BG,
            fg="gray"
        ).pack()

    def create_log_section(self):
        """Create log section"""
        frame = tk.LabelFrame(
            self.root,
            text=" 處理日誌 ",
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
            height=15
        )
        self.log_text.pack(padx=10, pady=10, fill=tk.BOTH, expand=True)
        self.log_text.config(state=tk.DISABLED)

    def create_bottom_buttons(self):
        """Create bottom buttons"""
        btn_frame = tk.Frame(self.root, bg=Colors.BACKGROUND)
        btn_frame.pack(pady=10, padx=20, fill=tk.X)

        self.start_button = self.create_button(
            btn_frame,
            "▶ 開始處理",
            Colors.SUCCESS,
            self.start_processing,
            width=15
        )
        self.start_button.pack(side=tk.LEFT)

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
        """Create styled button"""
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
        r, g, b = int(r * 0.9), int(g * 0.9), int(b * 0.9)
        return f'#{r:02x}{g:02x}{b:02x}'

    def write_log(self, message: str, log_type: str = "Info"):
        """Write log message"""
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
        """Clear log"""
        self.log_text.config(state=tk.NORMAL)
        self.log_text.delete(1.0, tk.END)
        self.log_text.config(state=tk.DISABLED)
        self.write_log("日誌已清除", "Info")

    def select_source_directory(self):
        """Select source directory"""
        directory = filedialog.askdirectory(
            title="選擇來源圖片目錄",
            initialdir=os.getcwd()
        )

        if directory:
            self.source_dir = directory
            self.source_dir_var.set(directory)

            # Auto set target directory
            self.target_dir = os.path.join(directory, "optimize")
            self.target_dir_var.set(f"{directory}\\optimize\\")

            self.write_log(f"已選擇來源目錄: {directory}", "Success")
            self.write_log(f"輸出目錄將建立於: {self.target_dir}", "Info")

            # Count images
            image_count = self.count_images(directory)
            if image_count > 0:
                self.write_log(f"找到 {image_count} 個圖片檔案", "Info")
            else:
                self.write_log("警告: 目錄中未找到圖片檔案", "Warning")

    def count_images(self, directory: str) -> int:
        """Count image files in directory"""
        image_extensions = {'.jpg', '.jpeg', '.png', '.bmp', '.gif', '.webp'}
        count = 0
        for file in os.listdir(directory):
            if os.path.splitext(file.lower())[1] in image_extensions:
                count += 1
        return count

    def start_processing(self):
        """Start image processing"""
        if not self.source_dir:
            messagebox.showwarning("提示", "請先選擇來源目錄")
            return

        if self.is_processing:
            messagebox.showinfo("提示", "正在處理中，請稍候")
            return

        # Confirm start
        image_count = self.count_images(self.source_dir)
        if image_count == 0:
            messagebox.showerror("錯誤", "來源目錄中沒有圖片檔案")
            return

        response = messagebox.askyesno(
            "確認處理",
            f"將處理 {image_count} 個圖片檔案\n"
            f"來源目錄: {self.source_dir}\n"
            f"輸出目錄: {self.target_dir}\n\n"
            f"是否開始處理？"
        )

        if response:
            self.run_processing()

    def run_processing(self):
        """Run processing in background thread"""
        self.is_processing = True
        self.start_button.config(state=tk.DISABLED)
        self.status_var.set("處理中...")

        thread = threading.Thread(target=self.process_images, daemon=True)
        thread.start()

    def process_images(self):
        """Process all images"""
        try:
            # Create target directory
            os.makedirs(self.target_dir, exist_ok=True)
            self.write_log(f"建立輸出目錄: {self.target_dir}", "Success")
            self.write_log("=" * 60, "Info")

            # Get image files
            image_extensions = {'.jpg', '.jpeg', '.png', '.bmp', '.gif', '.webp'}
            image_files = [
                f for f in os.listdir(self.source_dir)
                if os.path.splitext(f.lower())[1] in image_extensions
            ]

            total = len(image_files)
            success_count = 0
            error_count = 0

            for idx, filename in enumerate(image_files, 1):
                source_path = os.path.join(self.source_dir, filename)

                # Change extension to .jpg and add 'opt-' prefix
                base_name = os.path.splitext(filename.lower())[0]
                target_filename = f"opt-{base_name}.jpg"
                target_path = os.path.join(self.target_dir, target_filename)

                self.write_log(f"[{idx}/{total}] 處理: {filename}", "Info")

                result = self.optimizer.optimize_image(source_path, target_path, self.write_log)

                if result['success']:
                    orig_w, orig_h = result['original_size']
                    tgt_w, tgt_h = result['target_size']
                    orig_size_kb = result['original_file_size'] / 1024
                    tgt_size_kb = result['target_file_size'] / 1024
                    ratio = result['compression_ratio']

                    self.write_log(
                        f"  ✓ {orig_w}×{orig_h} → {tgt_w}×{tgt_h} | "
                        f"{orig_size_kb:.1f}KB → {tgt_size_kb:.1f}KB (-{ratio:.1f}%)",
                        "Success"
                    )
                    self.write_log(f"  → 儲存為: {target_filename}", "Info")
                    success_count += 1
                else:
                    self.write_log(f"  ✗ 錯誤: {result['error']}", "Error")
                    error_count += 1

                self.write_log("", "Info")

                # Update status
                self.status_var.set(f"處理中... ({idx}/{total})")

            # Summary
            self.write_log("=" * 60, "Info")
            self.write_log(
                f"處理完成！成功: {success_count} / 失敗: {error_count} / 總計: {total}",
                "Success" if error_count == 0 else "Warning"
            )
            self.write_log(f"輸出目錄: {self.target_dir}", "Info")

            self.status_var.set(f"處理完成 ({success_count}/{total})")

        except Exception as e:
            self.write_log(f"處理失敗: {str(e)}", "Error")
            self.status_var.set("處理失敗")

        finally:
            self.is_processing = False
            self.start_button.config(state=tk.NORMAL)


def main():
    """Main entry point"""
    root = tk.Tk()
    app = ImageOptimizerGUI(root)

    # Welcome message
    app.write_log("圖片優化工具已啟動", "Success")
    app.write_log("請選擇來源目錄以開始處理", "Info")

    root.mainloop()


if __name__ == "__main__":
    main()
