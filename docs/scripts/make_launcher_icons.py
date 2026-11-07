# -*- coding: utf-8 -*-
"""简记账 CoinFlow 启动图标生成脚本。

设计：翡翠绿对角渐变底 + 白色账单票据（账目横线）+ 深绿圆形 ¥ 徽章。
产物：
  - app/src/main/res/mipmap-*/ic_launcher.webp          (legacy 圆角方, API 24/25)
  - app/src/main/res/mipmap-*/ic_launcher_round.webp    (legacy 正圆)
  - app/src/main/res/mipmap-*/ic_launcher_foreground.webp (自适应前景层)
  - app/src/main/res/mipmap-*/ic_launcher_monochrome.webp (Android 13 主题图标层)
运行：python docs/scripts/make_launcher_icons.py
"""
from PIL import Image, ImageDraw, ImageFont
import os

RES = os.path.join(os.path.dirname(__file__), "..", "..", "app", "src", "main", "res")
FONT = "C:/Windows/Fonts/msyhbd.ttc"  # 微软雅黑 Bold

# 品牌色（对齐 LiquidGlassTokens 翡翠绿系）
EMERALD_400 = (52, 211, 153)   # 34D399
EMERALD_600 = (5, 150, 105)    # 059669
EMERALD_700 = (4, 120, 87)     # 047857
LINE_GREEN = (167, 243, 208)   # A7F3D0
WHITE = (255, 255, 255, 255)

# 内容坐标系：108x108 单位（自适应图标前景规范），中心 (54,54)，安全区半径 33
RECEIPT = (37, 28, 71, 72)     # 票据圆角矩形
RECEIPT_R = 6                  # 票据圆角半径
LINES = [(37, 43, 65), (45, 43, 65), (53, 43, 57)]  # (y, x0, x1) 账目横线
BADGE_C, BADGE_R = (65, 63), 12.5                   # ¥ 徽章圆心/半径
YEN_FONT_U = 15                # ¥ 字号（单位）


def diag_gradient(size, c_light, c_dark):
    """左上浅 → 右下深的对角渐变。"""
    small = 64
    img = Image.new("RGB", (small, small))
    px = img.load()
    for y in range(small):
        for x in range(small):
            t = (x + y) / (2.0 * (small - 1))
            px[x, y] = tuple(int(a + (b - a) * t) for a, b in zip(c_light, c_dark))
    return img.resize(size, Image.BILINEAR)


def draw_receipt(img, s, ox=0.0, oy=0.0, line_color=LINE_GREEN, mono=False):
    """在 108 单位坐标系绘制票据 + 徽章。s = 每单位像素数。"""
    d = ImageDraw.Draw(img)
    x0, y0, x1, y1 = [v * s + o for v, o in zip(RECEIPT, (ox, oy, ox, oy))]
    if mono:
        d.rounded_rectangle([x0, y0, x1, y1], radius=RECEIPT_R * s,
                            outline=WHITE, width=max(2, round(3.2 * s)))
    else:
        d.rounded_rectangle([x0, y0, x1, y1], radius=RECEIPT_R * s, fill=WHITE)
    for (ly, lx0, lx1) in LINES:
        yy = ly * s + oy
        lw = max(1, round(2.6 * s))
        d.line([lx0 * s + ox, yy, lx1 * s + ox, yy],
               fill=WHITE if mono else line_color, width=lw)
        for ex in (lx0, lx1):  # 圆头
            r = lw / 2.0
            d.ellipse([ex * s + ox - r, yy - r, ex * s + ox + r, yy + r],
                      fill=WHITE if mono else line_color)
    if mono:
        font = ImageFont.truetype(FONT, int(YEN_FONT_U * s))
        d.text((54 * s + ox, 51 * s + oy), "￥", font=font, fill=WHITE, anchor="mm")
    else:
        cx, cy = BADGE_C[0] * s + ox, BADGE_C[1] * s + oy
        r = BADGE_R * s
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=EMERALD_700)
        font = ImageFont.truetype(FONT, int(YEN_FONT_U * s))
        d.text((cx, cy), "￥", font=font, fill=WHITE, anchor="mm")


def make_foreground(px):
    """自适应前景层：透明底 + 内容。"""
    img = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    draw_receipt(img, px / 108.0)
    return img


def make_monochrome(px):
    """Android 13 主题图标层：纯白描边（系统按 alpha 染色）。"""
    img = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    draw_receipt(img, px / 108.0, mono=True)
    return img


def make_legacy(px, round_shape=False):
    """API 24/25 兜底：完整合成图（渐变底 + 放大的内容）。"""
    img = diag_gradient((px, px), EMERALD_400, EMERALD_600).convert("RGBA")
    mask = Image.new("L", (px, px), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, px - 1, px - 1],
                                           radius=round(px * 0.22), fill=255)
    if round_shape:
        ImageDraw.Draw(mask).ellipse([0, 0, px - 1, px - 1], fill=255)
        img.putalpha(mask)
    else:
        r, g, b, _ = img.split()
        img = Image.merge("RGBA", (r, g, b, mask))
    # legacy 无裁切遮罩，内容放大 1.25 倍保持视觉占比
    draw_receipt(img, px / 108.0 * 1.25, ox=px * -0.0625, oy=px * -0.0625)
    return img


def save(img, rel, size):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, "WEBP", quality=95)
    print(f"  wrote {rel} ({size}px)")


DENSITIES = [("mdpi", 1), ("hdpi", 1.5), ("xhdpi", 2), ("xxhdpi", 3), ("xxxhdpi", 4)]

print("生成自适应前景 / 主题图标层…")
for name, m in DENSITIES:
    base = round(108 * m)
    save(make_foreground(base), f"mipmap-{name}/ic_launcher_foreground.webp", base)
    save(make_monochrome(base), f"mipmap-{name}/ic_launcher_monochrome.webp", base)

print("生成 legacy 启动图标…")
for name, m in DENSITIES:
    size = round(48 * m)
    save(make_legacy(size), f"mipmap-{name}/ic_launcher.webp", size)
    save(make_legacy(size, round_shape=True), f"mipmap-{name}/ic_launcher_round.webp", size)

print("完成。")
