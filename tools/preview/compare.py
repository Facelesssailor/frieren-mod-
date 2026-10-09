import sys
from PIL import Image, ImageDraw
pv, start, fps, out = sys.argv[1], float(sys.argv[2]), float(sys.argv[3]), sys.argv[4]
us = [0.25, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.75, 5.25, 6.0, 7.0, 8.0, 8.75, 10.0, 12.0, 13.5, 14.5, 15.25, 16.0, 18.0, 21.0, 24.0, 28.0, 31.0]
w, h = 480, 270
cols = 4
rows = (len(us) + cols - 1) // cols
sh = Image.new('RGB', (cols * w * 2, rows * h), 'black')
d = ImageDraw.Draw(sh)
for k, u in enumerate(us):
    t = 903.0 + u
    n = int(round((t - 885.0) / 0.25)) + 1
    try:
        a = Image.open('/home/claude/w/ep/f_%03d.jpg' % n).convert('RGB').resize((w, h))
    except Exception:
        a = Image.new('RGB', (w, h))
    i = int(round((u - start) * fps))
    try:
        b = Image.open('%s/%05d.png' % (pv, i)).convert('RGB').resize((w, h))
    except Exception:
        b = Image.new('RGB', (w, h))
    x, y = (k % cols) * w * 2, (k // cols) * h
    sh.paste(a, (x, y)); sh.paste(b, (x + w, y))
    m = int(t // 60); s = t - 60 * m
    d.rectangle([x, y, x + 150, y + 16], fill='black'); d.text((x + 4, y + 3), 'ep21 %d:%05.2f' % (m, s), fill='yellow')
    d.rectangle([x + w, y, x + w + 120, y + 16], fill='black'); d.text((x + w + 4, y + 3), 'mod u=%.2f' % u, fill='cyan')
sh.save(out, quality=88)
print(out, sh.size)
