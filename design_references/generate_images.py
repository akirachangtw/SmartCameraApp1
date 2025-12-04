"""
生成 DirectionalIndicator 重新設計的視覺參考圖片
使用 Pillow (PIL) 繪製靜態參考圖
"""

from PIL import Image, ImageDraw, ImageFont
import math

# 手機畫面尺寸
WIDTH = 360
HEIGHT = 640

# 顏色定義
BG_COLOR = (42, 42, 42)  # #2a2a2a
GRID_COLOR = (255, 255, 255, 51)  # rgba(255, 255, 255, 0.2)
FACE_BOX_COLOR = (76, 175, 80)  # #4CAF50 Material Green
TARGET_OUTER = (76, 175, 80, 153)  # rgba(76, 175, 80, 0.6)
TARGET_INNER_MIN = (76, 175, 80, 102)  # rgba(76, 175, 80, 0.4)
TARGET_INNER_MAX = (76, 175, 80, 255)  # rgba(76, 175, 80, 1.0)
GUIDELINE_COLOR = (76, 175, 80, 128)  # rgba(76, 175, 80, 0.5)
HINT_COLOR = (255, 193, 7, 230)  # rgba(255, 193, 7, 0.9)
LEVEL_COLOR = (255, 167, 38, 204)  # rgba(255, 167, 38, 0.8)

# 三分點位置
THIRD_X_LEFT = WIDTH // 3  # 120
THIRD_X_RIGHT = WIDTH * 2 // 3  # 240
THIRD_Y_TOP = HEIGHT // 3  # 213
THIRD_Y_BOTTOM = HEIGHT * 2 // 3  # 427

# 臉部位置（右側，需要移動到左上三分點）
FACE_X = 300
FACE_Y = 330
FACE_WIDTH = 80
FACE_HEIGHT = 100


def draw_grid(draw):
    """繪製九宮格線（淡化版）"""
    # 垂直線
    draw.line([(THIRD_X_LEFT, 0), (THIRD_X_LEFT, HEIGHT)], fill=GRID_COLOR, width=1)
    draw.line([(THIRD_X_RIGHT, 0), (THIRD_X_RIGHT, HEIGHT)], fill=GRID_COLOR, width=1)

    # 水平線
    draw.line([(0, THIRD_Y_TOP), (WIDTH, THIRD_Y_TOP)], fill=GRID_COLOR, width=1)
    draw.line([(0, THIRD_Y_BOTTOM), (WIDTH, THIRD_Y_BOTTOM)], fill=GRID_COLOR, width=1)


def draw_face_box(draw):
    """繪製臉部偵測框"""
    x1 = FACE_X - FACE_WIDTH // 2
    y1 = FACE_Y - FACE_HEIGHT // 2
    x2 = FACE_X + FACE_WIDTH // 2
    y2 = FACE_Y + FACE_HEIGHT // 2

    draw.rectangle([x1, y1, x2, y2], outline=FACE_BOX_COLOR, width=2)

    # 臉部中心點
    draw.ellipse([FACE_X - 3, FACE_Y - 3, FACE_X + 3, FACE_Y + 3], fill=FACE_BOX_COLOR)


def draw_top_ui(draw):
    """繪製頂部 UI 元素"""
    try:
        # 嘗試使用系統字體
        font = ImageFont.truetype("arial.ttf", 16)
        font_small = ImageFont.truetype("arial.ttf", 12)
    except:
        # 使用預設字體
        font = ImageFont.load_default()
        font_small = ImageFont.load_default()

    # 構圖提示文字
    draw.text((20, 20), "需要調整", fill=HINT_COLOR[:3], font=font)

    # 水平儀（簡化版 - 傾斜線條）
    level_x = 320
    level_y = 40
    angle = -7  # 傾斜角度

    # 繪製傾斜的水平線
    rad = math.radians(angle)
    length = 30
    x1 = level_x - length * math.cos(rad)
    y1 = level_y + length * math.sin(rad)
    x2 = level_x + length * math.cos(rad)
    y2 = level_y - length * math.sin(rad)

    draw.line([(x1, y1), (x2, y2)], fill=LEVEL_COLOR[:3], width=2)
    draw.text((295, 48), "-7.0°", fill=LEVEL_COLOR[:3], font=font_small)


def draw_target_circle(draw, x, y, pulse_phase=0.5):
    """
    繪製目標圓圈
    pulse_phase: 0.0 到 1.0，控制脈衝動畫階段
    """
    # 外圈（靜態）
    outer_radius = 40
    draw.ellipse(
        [x - outer_radius, y - outer_radius, x + outer_radius, y + outer_radius],
        outline=TARGET_OUTER[:3],
        width=3
    )

    # 內圈（脈衝）
    # pulse_phase = 0.5 代表動畫中間階段（最亮/最大）
    pulse_alpha = 0.4 + 0.6 * abs(math.sin(pulse_phase * math.pi))
    pulse_scale = 1.0 + 0.3 * abs(math.sin(pulse_phase * math.pi))

    inner_radius = int(20 * pulse_scale)
    alpha_value = int(pulse_alpha * 255)
    inner_color = (76, 175, 80, alpha_value)

    # 創建臨時圖層以支援 alpha 混合
    overlay = Image.new('RGBA', (WIDTH, HEIGHT), (0, 0, 0, 0))
    overlay_draw = ImageDraw.Draw(overlay)

    overlay_draw.ellipse(
        [x - inner_radius, y - inner_radius, x + inner_radius, y + inner_radius],
        fill=inner_color
    )

    # 中心點
    overlay_draw.ellipse([x - 4, y - 4, x + 4, y + 4], fill=FACE_BOX_COLOR)

    return overlay


def draw_guideline(draw, x1, y1, x2, y2):
    """繪製引導虛線"""
    # PIL 不支援虛線，用多段短線模擬
    dx = x2 - x1
    dy = y2 - y1
    length = math.sqrt(dx * dx + dy * dy)

    dash_length = 10
    gap_length = 10
    num_dashes = int(length / (dash_length + gap_length))

    for i in range(num_dashes):
        t1 = i * (dash_length + gap_length) / length
        t2 = (i * (dash_length + gap_length) + dash_length) / length

        if t2 > 1.0:
            t2 = 1.0

        px1 = x1 + dx * t1
        py1 = y1 + dy * t1
        px2 = x1 + dx * t2
        py2 = y1 + dy * t2

        draw.line([(px1, py1), (px2, py2)], fill=GUIDELINE_COLOR[:3], width=2)


def generate_option1():
    """選項 1：目標圓圈（簡潔版）"""
    img = Image.new('RGB', (WIDTH, HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(img)

    draw_grid(draw)
    draw_face_box(draw)
    draw_top_ui(draw)

    # 目標圓圈（中間階段，最明顯）
    target_x = THIRD_X_LEFT
    target_y = THIRD_Y_TOP
    overlay = draw_target_circle(draw, target_x, target_y, pulse_phase=0.5)

    # 合併圖層
    img = Image.alpha_composite(img.convert('RGBA'), overlay)

    return img.convert('RGB')


def generate_option2():
    """選項 2：目標圓圈 + 引導線"""
    img = Image.new('RGB', (WIDTH, HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(img)

    draw_grid(draw)

    # 引導線（在臉部框之前繪製）
    target_x = THIRD_X_LEFT
    target_y = THIRD_Y_TOP
    draw_guideline(draw, FACE_X, FACE_Y, target_x, target_y)

    draw_face_box(draw)
    draw_top_ui(draw)

    overlay = draw_target_circle(draw, target_x, target_y, pulse_phase=0.5)
    img = Image.alpha_composite(img.convert('RGBA'), overlay)

    return img.convert('RGB')


def generate_option3():
    """選項 3：顯示全部四個三分點"""
    img = Image.new('RGBA', (WIDTH, HEIGHT), BG_COLOR + (255,))
    draw = ImageDraw.Draw(img)

    draw_grid(draw)
    draw_face_box(draw)
    draw_top_ui(draw)

    # 四個三分點
    points = [
        (THIRD_X_LEFT, THIRD_Y_TOP, True),      # 左上（最近，高亮）
        (THIRD_X_RIGHT, THIRD_Y_TOP, False),    # 右上
        (THIRD_X_LEFT, THIRD_Y_BOTTOM, False),  # 左下
        (THIRD_X_RIGHT, THIRD_Y_BOTTOM, False)  # 右下
    ]

    for x, y, is_nearest in points:
        if is_nearest:
            # 最近的點：大圓圈 + 脈衝
            overlay = draw_target_circle(draw, x, y, pulse_phase=0.5)
            img = Image.alpha_composite(img, overlay)
        else:
            # 其他點：小圓圈，半透明
            radius = 30
            draw.ellipse(
                [x - radius, y - radius, x + radius, y + radius],
                outline=(76, 175, 80, 77),  # 0.3 alpha
                width=2
            )
            # 中心點
            draw.ellipse([x - 3, y - 3, x + 3, y + 3], fill=(76, 175, 80, 102))

    return img.convert('RGB')


def generate_animation_timeline():
    """動畫時間軸（三個關鍵幀）"""
    frame_size = 200
    total_width = frame_size * 3 + 60  # 3 frames + spacing
    total_height = frame_size + 100  # frame + label space

    img = Image.new('RGB', (total_width, total_height), (26, 26, 26))

    phases = [0.0, 0.5, 1.0]  # 開始、中間、結束
    labels = [
        "開始 (0.0s)\nAlpha: 0.4 | Scale: 1.0",
        "中間 (0.5s)\nAlpha: 1.0 | Scale: 1.3",
        "結束 (1.0s)\nAlpha: 0.4 | Scale: 1.0"
    ]

    for i, (phase, label) in enumerate(zip(phases, labels)):
        # 創建單個幀
        frame = Image.new('RGBA', (frame_size, frame_size), BG_COLOR + (255,))
        frame_draw = ImageDraw.Draw(frame)

        center_x = frame_size // 2
        center_y = frame_size // 2

        # 繪製參考網格
        frame_draw.line([(center_x, 0), (center_x, frame_size)], fill=(255, 255, 255, 26), width=1)
        frame_draw.line([(0, center_y), (frame_size, center_y)], fill=(255, 255, 255, 26), width=1)

        # 繪製目標圓圈
        overlay = draw_target_circle(frame_draw, center_x, center_y, pulse_phase=phase)

        # 合併到主圖
        x_offset = i * (frame_size + 30) + 15
        y_offset = 15

        img.paste(frame.convert('RGB'), (x_offset, y_offset))

        # 添加標籤
        try:
            font = ImageFont.truetype("arial.ttf", 14)
        except:
            font = ImageFont.load_default()

        draw = ImageDraw.Draw(img)
        text_y = y_offset + frame_size + 10

        # 多行文字
        for line_i, line in enumerate(label.split('\n')):
            draw.text((x_offset + 10, text_y + line_i * 20), line, fill=(170, 170, 170), font=font)

    return img


def main():
    """生成所有設計參考圖片"""
    print("正在生成設計參考圖片...")

    # 選項 1
    print("  [1/4] 生成選項 1：目標圓圈...")
    img1 = generate_option1()
    img1.save("option1_target_circle.png")

    # 選項 2
    print("  [2/4] 生成選項 2：目標圓圈 + 引導線...")
    img2 = generate_option2()
    img2.save("option2_with_guideline.png")

    # 選項 3
    print("  [3/4] 生成選項 3：顯示全部四個三分點...")
    img3 = generate_option3()
    img3.save("option3_all_points.png")

    # 動畫時間軸
    print("  [4/4] 生成動畫時間軸...")
    img4 = generate_animation_timeline()
    img4.save("animation_timeline.png")

    print("\n✅ 完成！已生成 4 張參考圖片：")
    print("   - option1_target_circle.png")
    print("   - option2_with_guideline.png")
    print("   - option3_all_points.png")
    print("   - animation_timeline.png")


if __name__ == "__main__":
    main()
