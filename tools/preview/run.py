"""Drive the offline cutscene preview: CineHarness (the mod's own Java drawing code) -> WebGL renderer -> PNG frames.
usage: run.py OUTDIR START END FPS [W H]   (times in seconds after release; negative = still charging)"""
import sys, os, subprocess, threading, http.server, functools, socketserver, time
from playwright.sync_api import sync_playwright

W_ROOT = '/home/claude/w'
out = sys.argv[1]; t0 = float(sys.argv[2]); t1 = float(sys.argv[3]); fps = float(sys.argv[4])
times = sys.argv[5].split(',') if len(sys.argv) > 5 and ',' in sys.argv[5] else None
os.makedirs(out, exist_ok=True)
tmp = '/tmp/claude-0/pv' + os.environ.get('MODE', '')
os.makedirs(tmp, exist_ok=True)

class Quiet(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *a):
        pass
srv = socketserver.ThreadingTCPServer(('127.0.0.1', 0), functools.partial(Quiet, directory=W_ROOT))
port = srv.server_address[1]
threading.Thread(target=srv.serve_forever, daemon=True).start()
link = W_ROOT + '/pvtmp' + os.environ.get('MODE', '')
os.symlink(tmp, link) if not os.path.exists(link) else None

cp = open(W_ROOT + '/hcp.txt').read().strip()
if os.environ.get('HCLS'):
    cp = cp.replace('prev/cls:', os.environ['HCLS'] + ':', 1)
h = subprocess.Popen(['java', '-Dheight=720', '-Dmode=' + os.environ.get('MODE', 'barrier'), '-cp', cp, 'dev.pete.frierenarcana.client.CineHarness'], stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                     stderr=subprocess.DEVNULL, text=True, bufsize=1)
if times:
    ts = [float(x) for x in times]
else:
    n = int(round((t1 - t0) * fps)) + 1
    ts = [t0 + i / fps for i in range(n)]
with sync_playwright() as p:
    b = p.chromium.launch(args=['--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--ignore-gpu-blocklist'])
    pg = b.new_page(viewport={'width': 1280, 'height': 720})
    pg.on('console', lambda m: print('console:', m.text) if m.type in ('error', 'warning') else None)
    pg.goto(f'http://127.0.0.1:{port}/prev/render.html')
    print(pg.evaluate('initRenderer()'))
    start = time.time()
    for i, u in enumerate(ts):
        f = f'{tmp}/f{i:05d}.bin'
        h.stdin.write(f'f {i} {u:.5f} {f}\n'); h.stdin.flush()
        line = h.stdout.readline().strip()
        if not line.startswith('ok'):
            print('harness:', line); break
        info = pg.evaluate(f"renderFrame('/pvtmp{os.environ.get('MODE', '')}/f{i:05d}.bin')")
        pg.locator('#c').screenshot(path=f'{out}/{i:05d}.png')
        os.remove(f)
        if i % 10 == 0:
            print(f'{i}/{len(ts)} u={u:.2f} {line} {time.time() - start:.0f}s', flush=True)
    b.close()
h.stdin.close(); h.wait(timeout=10)
srv.shutdown()
