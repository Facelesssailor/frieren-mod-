"""Generates the Siebenkreis circle textures (original procedural art).

Usage: python3 gen_textures.py <assets/siebenkreis dir>
Circles are white on black: the renderer draws them additively and tints them.
"""
import math
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

SS = 2          # supersampling
SIZE = 512


def canvas(size=SIZE):
    return Image.new("L", (size * SS, size * SS), 0)


def ring(d, c, r, w, v=255):
    w *= SS
    d.ellipse([c - r, c - r, c + r, c + r], outline=v, width=int(w))


def polygon_star(d, c, r, points, step, w, rot=0.0, v=255):
    pts = []
    for k in range(points):
        a = rot + 2 * math.pi * k / points - math.pi / 2
        pts.append((c + r * math.cos(a), c + r * math.sin(a)))
    for k in range(points):
        a, b = pts[k], pts[(k + step) % points]
        d.line([a, b], fill=v, width=int(w * SS))


def glyph(d, cx, cy, s, ang, rng, w, v=255):
    """A small invented rune: a few strokes on a 3x3 lattice, rotated to the ring tangent."""
    lattice = [(x, y) for x in (-1, 0, 1) for y in (-1, 0, 1)]
    strokes = rng.randint(2, 4)
    ca, sa = math.cos(ang), math.sin(ang)

    def tr(p):
        x, y = p[0] * s, p[1] * s
        return (cx + x * ca - y * sa, cy + x * sa + y * ca)

    for _ in range(strokes):
        a = rng.choice(lattice)
        b = rng.choice([p for p in lattice if p != a])
        d.line([tr(a), tr(b)], fill=v, width=int(w * SS))
    if rng.random() < 0.4:
        p = tr(rng.choice(lattice))
        rr = s * 0.28
        d.ellipse([p[0] - rr, p[1] - rr, p[0] + rr, p[1] + rr], outline=v, width=int(w * SS))


def rune_band(d, c, r, count, size, rng, w=2.0, v=255):
    for k in range(count):
        a = 2 * math.pi * k / count
        glyph(d, c + r * math.cos(a), c + r * math.sin(a), size, a + math.pi / 2, rng, w, v)


def ticks(d, c, r0, r1, count, w=1.5, v=255):
    for k in range(count):
        a = 2 * math.pi * k / count
        d.line([(c + r0 * math.cos(a), c + r0 * math.sin(a)), (c + r1 * math.cos(a), c + r1 * math.sin(a))],
               fill=v, width=int(w * SS))


def dots(d, c, r, count, rr, v=255, filled=True):
    for k in range(count):
        a = 2 * math.pi * k / count
        x, y = c + r * math.cos(a), c + r * math.sin(a)
        if filled:
            d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=v)
        else:
            d.ellipse([x - rr, y - rr, x + rr, y + rr], outline=v, width=2 * SS)


def finish(img, glow=6, size=SIZE):
    import numpy as np
    img = img.resize((size, size), Image.LANCZOS)
    halo = img.filter(ImageFilter.GaussianBlur(glow))
    arr = np.asarray(img, dtype="float32") + np.asarray(halo, dtype="float32") * 0.7
    out = Image.fromarray(np.clip(arr, 0, 255).astype("uint8"), "L")
    return Image.merge("RGB", (out, out, out))


def ring_0(seed):
    rng = random.Random(seed)
    img = canvas(); d = ImageDraw.Draw(img); c = SIZE * SS / 2; R = c * 0.97
    ring(d, c, R, 5); ring(d, c, R * 0.95, 2)
    rune_band(d, c, R * 0.885, 28, R * 0.035, rng)
    ring(d, c, R * 0.82, 2.5); ring(d, c, R * 0.79, 1.5)
    polygon_star(d, c, R * 0.79, 7, 3, 2.5)
    polygon_star(d, c, R * 0.79, 7, 2, 1.5)
    dots(d, c, R * 0.79, 7, R * 0.03, filled=False)
    ring(d, c, R * 0.36, 2); ticks(d, c, R * 0.30, R * 0.36, 42)
    return finish(img)


def ring_1(seed):
    rng = random.Random(seed)
    img = canvas(); d = ImageDraw.Draw(img); c = SIZE * SS / 2; R = c * 0.97
    ring(d, c, R, 3); ticks(d, c, R * 0.93, R, 96, 1.5); ring(d, c, R * 0.93, 3)
    rune_band(d, c, R * 0.86, 22, R * 0.04, rng, 2.2)
    ring(d, c, R * 0.78, 2)
    polygon_star(d, c, R * 0.78, 6, 2, 2.5)
    polygon_star(d, c, R * 0.78, 6, 2, 1.5, rot=math.pi / 6)
    ring(d, c, R * 0.45, 2); ring(d, c, R * 0.42, 1)
    dots(d, c, R * 0.6, 12, R * 0.012)
    return finish(img)


def ring_2(seed):
    rng = random.Random(seed)
    img = canvas(); d = ImageDraw.Draw(img); c = SIZE * SS / 2; R = c * 0.97
    ring(d, c, R, 4)
    dots(d, c, R * 0.95, 64, R * 0.007)
    ring(d, c, R * 0.9, 2)
    rune_band(d, c, R * 0.83, 32, R * 0.03, rng, 1.8)
    ring(d, c, R * 0.76, 2.5)
    polygon_star(d, c, R * 0.76, 4, 1, 2.5)
    polygon_star(d, c, R * 0.76, 4, 1, 2.5, rot=math.pi / 4)
    polygon_star(d, c, R * 0.76, 8, 3, 1.2)
    ring(d, c, R * 0.3, 2)
    return finish(img)


def ring_3(seed):
    rng = random.Random(seed)
    img = canvas(); d = ImageDraw.Draw(img); c = SIZE * SS / 2; R = c * 0.97
    ring(d, c, R, 2.5); ring(d, c, R * 0.97, 6); ring(d, c, R * 0.92, 1.5)
    # crescents
    for k in range(9):
        a = 2 * math.pi * k / 9
        x, y = c + R * 0.84 * math.cos(a), c + R * 0.84 * math.sin(a)
        rr = R * 0.06
        d.arc([x - rr, y - rr, x + rr, y + rr], math.degrees(a) + 90, math.degrees(a) + 270, fill=255, width=3 * SS)
    rune_band(d, c, R * 0.72, 18, R * 0.04, rng, 2.0)
    ring(d, c, R * 0.64, 2)
    ticks(d, c, R * 0.12, R * 0.64, 9, 2)
    polygon_star(d, c, R * 0.64, 9, 4, 1.5)
    ring(d, c, R * 0.2, 2)
    return finish(img)


def inner(seed):
    rng = random.Random(seed)
    img = canvas(); d = ImageDraw.Draw(img); c = SIZE * SS / 2; R = c * 0.97
    ring(d, c, R, 3)
    rune_band(d, c, R * 0.88, 20, R * 0.06, rng, 2.4)
    ring(d, c, R * 0.76, 2)
    polygon_star(d, c, R * 0.76, 5, 2, 2.5)
    ring(d, c, R * 0.3, 2.5)
    rune_band(d, c, R * 0.2, 6, R * 0.05, rng, 2)
    return finish(img)


def scar(seed):
    rng = random.Random(seed)
    img = canvas(); d = ImageDraw.Draw(img); c = SIZE * SS / 2; R = c * 0.97
    ring(d, c, R * 0.98, 7); ring(d, c, R * 0.9, 3)
    rune_band(d, c, R * 0.82, 26, R * 0.035, rng, 3)
    polygon_star(d, c, R * 0.74, 7, 3, 4)
    ring(d, c, R * 0.74, 3)
    # cracks radiating from the strike point
    for _ in range(18):
        a = rng.random() * 2 * math.pi
        r = 0; x, y = c, c
        while r < R * rng.uniform(0.4, 0.95):
            step = R * 0.05
            a += rng.uniform(-0.35, 0.35)
            nx, ny = x + step * math.cos(a), y + step * math.sin(a)
            d.line([(x, y), (nx, ny)], fill=200, width=int(2 * SS))
            x, y = nx, ny; r = math.hypot(x - c, y - c)
    return finish(img, glow=10)


def beam():
    import numpy as np
    w, h = 64, 256
    xs = np.linspace(-1, 1, w)
    prof = np.exp(-(xs ** 2) * 5.0) * 0.85 + np.exp(-(xs ** 2) * 60.0) * 0.6
    rng = np.random.default_rng(7)
    streak = np.clip(rng.normal(0.9, 0.12, (h, 1)), 0.6, 1.2)
    streak = np.convolve(streak[:, 0], np.ones(9) / 9, mode="same")[:, None]
    arr = np.clip(prof[None, :] * streak * 255, 0, 255).astype("uint8")
    g = Image.fromarray(arr, "L")
    return Image.merge("RGB", (g, g, g))


def icon():
    import numpy as np
    s = 64
    base = ring_0(1).resize((s, s), Image.LANCZOS)
    a = np.asarray(base, dtype="float32") / 255.0
    yy, xx = np.mgrid[0:s, 0:s]
    bg = np.zeros((s, s, 3), dtype="float32")
    bg[..., 0], bg[..., 1], bg[..., 2] = 0.08, 0.07, 0.16
    gold = np.array([1.0, 0.86, 0.5]); blue = np.array([0.7, 0.9, 1.0])
    t = (yy / (s - 1))[..., None]
    col = gold * (1 - t) + blue * t
    out = bg + a * col * 1.4
    # pillar
    d = np.abs(xx - (s - 1) / 2) / 3.0
    out += (np.exp(-d ** 2) * 0.9)[..., None] * np.array([1.0, 0.97, 0.88])
    out = np.clip(out, 0, 1)
    img = Image.fromarray((out * 255).astype("uint8"), "RGB").convert("RGBA")
    # rounded mask
    m = Image.new("L", (s, s), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, s - 1, s - 1], 8, fill=255)
    img.putalpha(m)
    return img


def main():
    root = Path(sys.argv[1])
    ent = root / "textures/entity/siebenkreis"
    ent.mkdir(parents=True, exist_ok=True)
    ring_0(11).save(ent / "ring_0.png")
    ring_1(22).save(ent / "ring_1.png")
    ring_2(33).save(ent / "ring_2.png")
    ring_3(44).save(ent / "ring_3.png")
    inner(55).save(ent / "inner.png")
    scar(66).save(ent / "scar.png")
    beam().save(ent / "beam.png")
    ic = root / "textures/gui/spell_icons"
    ic.mkdir(parents=True, exist_ok=True)
    icon().save(ic / "siebenkreis.png")


if __name__ == "__main__":
    main()
