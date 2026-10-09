"""Cut the Barrier Breaker cutscene audio out of ep. 21 (Japanese track) into the clips the mod plays.
Dialogue windows (from the episode's own subtitle timings) are replaced by the side channel (L-R), which keeps the
wide music and effects but drops the centre-panned voices, level-matched to the surrounding audio."""
import numpy as np, scipy.io.wavfile as wf, subprocess, sys, os, json
SR = 48000
BASE = 880.0
r, x = wf.read('/home/claude/w/aud/jp.wav')
assert r == SR
def at(t): return int(round((t - BASE) * SR))
DIALOGUE = [(885.12, 888.84), (898.25, 899.47), (900.05, 902.98), (903.70, 906.06), (913.39, 914.24),
            (921.50, 926.00), (926.87, 930.50), (930.93, 933.92), (934.43, 937.94), (938.50, 941.23)]
MARGIN = 0.12
XF = 0.08
y = x.copy()
mid = (x[:, 0] + x[:, 1]) * 0.5
side = (x[:, 0] - x[:, 1]) * 0.5
w = np.zeros(len(x))  # 1 where the voice-free side channel replaces the mix
for a, b in DIALOGUE:
    s, e = at(a - MARGIN), at(b + MARGIN)
    f = int(XF * SR)
    s0, e1 = max(0, s - f), min(len(x), e + f)
    ramp_in = np.linspace(0, 1, s - s0) if s > s0 else np.zeros(0)
    ramp_out = np.linspace(1, 0, e1 - e) if e1 > e else np.zeros(0)
    w[s0:s] = np.maximum(w[s0:s], ramp_in)
    w[s:e] = 1.0
    w[e:e1] = np.maximum(w[e:e1], ramp_out)
# level match the side channel to the full mix in a sliding 0.75 s window
win = int(0.75 * SR)
def rms(sig):
    c = np.cumsum(np.concatenate([[0.0], sig.astype(np.float64) ** 2]))
    out = np.empty(len(sig))
    for i in range(0, len(sig), 2400):
        lo, hi = max(0, i - win), min(len(sig), i + win)
        out[i:i + 2400] = np.sqrt((c[hi] - c[lo]) / max(1, hi - lo))
    return out
full_r = rms(mid * 0 + (x[:, 0] ** 2 + x[:, 1] ** 2) ** 0.5 / np.sqrt(2))
side_r = rms(side)
# use the mix level from just outside dialogue: smooth heavily so the gain does not follow the voice
gain = np.clip(full_r / np.maximum(side_r, 1e-4), 1.0, 3.0) * 0.8
k = np.ones(int(0.5 * SR)) / int(0.5 * SR)
gain = np.convolve(gain, k, mode='same')
s_l = side * gain
s_r = -side * gain * 0.85 + side * gain * 0.15   # mostly anti-phase for width, a little in phase so it survives mono
y[:, 0] = x[:, 0] * (1 - w) + s_l * w
y[:, 1] = x[:, 1] * (1 - w) + s_r * w

CLIPS = [  # name, start (episode seconds), end, fade-out seconds
    ('charge', 889.00, 903.00, 0.25),
    ('release', 903.00, 908.00, 0.02),
    ('rise', 908.00, 916.00, 0.02),
    ('flicker', 916.00, 918.03, 0.01),
    ('shatter', 918.03, 920.00, 0.02),
    ('after', 920.00, 935.50, 1.2),
]
peak = max(np.abs(y[at(a):at(b)]).max() for _, a, b, _ in CLIPS)
scale = 0.89 / peak
os.makedirs('/home/claude/w/snd/wav', exist_ok=True)
info = {}
for name, a, b, fo in CLIPS:
    seg = y[at(a):at(b)].copy() * scale
    fi = int(0.004 * SR)
    seg[:fi] *= np.linspace(0, 1, fi)[:, None]
    n = int(fo * SR)
    seg[-n:] *= np.linspace(1, 0, n)[:, None]
    p = '/home/claude/w/snd/wav/%s.wav' % name
    wf.write(p, SR, seg.astype(np.float32))
    info[name] = round(b - a, 3)
print(json.dumps(info), 'scale %.3f' % scale)
