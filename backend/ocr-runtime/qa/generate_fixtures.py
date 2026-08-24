from __future__ import annotations

import json
import random
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).parent
CASES = json.loads((ROOT / "expected-transcriptions.json").read_text(encoding="utf-8"))
OUTPUT = ROOT / "generated"
OUTPUT.mkdir(exist_ok=True)
FONT = ImageFont.truetype("arial.ttf", 32)

for case in CASES:
    dark = case["variant"] == "dark"
    image = Image.new("RGB", (1200, 520), "#171a22" if dark else "white")
    draw = ImageDraw.Draw(image)
    draw.multiline_text((48, 42), case["text"], fill="white" if dark else "#151922", font=FONT, spacing=18)
    if case["variant"] == "low_resolution":
        image = image.resize((360, 156)).resize((1200, 520), Image.Resampling.BILINEAR)
    elif case["variant"] == "noisy":
        pixels = image.load()
        for _ in range(22000):
            x, y = random.randrange(image.width), random.randrange(image.height)
            shade = random.randrange(130, 235)
            pixels[x, y] = (shade, shade, shade)
        image = image.filter(ImageFilter.GaussianBlur(0.45))
    elif case["variant"] == "mixed_layout":
        image = image.rotate(1.4, expand=False, fillcolor="white")
    output = OUTPUT / f"{case['id']}.jpg"
    image.save(output, quality=38 if case["variant"] == "compressed" else 88, optimize=True)
