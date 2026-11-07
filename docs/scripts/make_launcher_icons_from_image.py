# -*- coding: utf-8 -*-
"""简记账 CoinFlow 启动图标生成脚本（图片导入版）。

用法：python docs/scripts/make_launcher_icons_from_image.py <图片路径>
处理：裁掉透明边 → 主体等比缩放进自适应图标安全区 → 配浅蓝渐变背景层
     → 生成前景层 / legacy 圆角方 / legacy 正圆 全密度资源。
说明：3D 彩色插画不适合做单色主题图标层，monochrome 已从 adaptive-icon 中移除。
"""
from PIL import Image
import os
import sys

RES = os.path.join(os.path.dirname(__file__), "..", "..", "app", "src", "main", "res")
SRC = sys.argv[1] if len(sys.argv) > 1 else r"D:\Users\Admin\Downloads\img_20261107_173109_gend74bd_1.png"

# 主体在 108 单位画布中的对角线半径（自适应显示圆半径约 36，legacy 更宽松）
FG_RADIUS_U = 38.0
LEGACY_RADIUS_U = 45.0

BG_LIGHT = (255, 255, 255)   # 背景渐变浅端（顶部）
BG_DEEP = (214, 228, 248)    # 背景渐变深端（底部，浅蓝）


def load_subject():
    img = Image.open(SRC).convert("RGBA")
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    print(f"主体裁切后尺寸: {img.size}")
    return img


def diag_gradient(size, c_top, c_bottom):
    """垂直渐变（上浅下深）。"""
    small = 64
    img = Image.new("RGB", (small, small))
    px = img.load()
    for y in range(small):
        t = y / (small - 1.0)
        row = tuple(int(a + (b - a) * t) for a, b in zip(c_top, c_bottom))
        for x in range(small):
            px[x, y] = row
    return img.resize(size, Image.BILINEAR)


def make_foreground(subject, px):
    """自适应前景层：透明底 + 主体居中（对角半径 = FG_RADIUS_U 单位）。"""
    canvas = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    s = px / 108.0
    w, h = subject.size
    diag = (w * w + h * h) ** 0.5
    scale = (FG_RADIUS_U * 2.0) / diag * s
    new = (max(1, round(w * scale)), max(1, round(h * scale)))
    resized = subject.resize(new, Image.LANCZOS)
    canvas.paste(resized, ((px - new[0]) // 2, (px - new[1]) // 2), resized)
    return canvas


def make_legacy(subject, px, round_shape=False):
    """API 24/25 兜底：渐变底 + 主体（legacy 空间更宽松，主体更大）。"""
    img = diag_gradient((px, px), BG_LIGHT, BG_DEEP).convert("RGBA")
    from PIL import ImageDraw
    mask = Image.new("L", (px, px), 0)
    d = ImageDraw.Draw(mask)
    if round_shape:
        d.ellipse([0, 0, px - 1, px - 1], fill=255)
    else:
        d.rounded_rectangle([0, 0, px - 1, px - 1], radius=round(px * 0.22), fill=255)
    r, g, b, _ = img.split()
    img = Image.merge("RGBA", (r, g, b, mask))
    s = px / 108.0
    w, h = subject.size
    diag = (w * w + h * h) ** 0.5
    scale = (LEGACY_RADIUS_U * 2.0) / diag * s
    new = (max(1, round(w * scale)), max(1, round(h * scale)))
    resized = subject.resize(new, Image.LANCZOS)
    img.paste(resized, ((px - new[0]) // 2, (px - new[1]) // 2), resized)
    return img


def save(img, rel):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, "WEBP", quality=95)
    print(f"  wrote {rel}")


def main():
    subject = load_subject()
    densities = [("mdpi", 1), ("hdpi", 1.5), ("xhdpi", 2), ("xxhdpi", 3), ("xxxhdpi", 4)]
    print("生成自适应前景…")
    for name, m in densities:
        save(make_foreground(subject, round(108 * m)), f"mipmap-{name}/ic_launcher_foreground.webp")
    print("生成 legacy 启动图标…")
    for name, m in densities:
        size = round(48 * m)
        save(make_legacy(subject, size), f"mipmap-{name}/ic_launcher.webp")
        save(make_legacy(subject, size, round_shape=True), f"mipmap-{name}/ic_launcher_round.webp")
    # 清掉程序化版本的 monochrome 层（3D 插画不适合单色染色）
    for name, _ in densities:
        p = os.path.join(RES, f"mipmap-{name}", "ic_launcher_monochrome.webp")
        if os.path.exists(p):
            os.remove(p)
            print(f"  removed mipmap-{name}/ic_launcher_monochrome.webp")
    print("完成。")


if __name__ == "__main__":
    main()
