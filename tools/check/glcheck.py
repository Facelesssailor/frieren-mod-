import sys, json, os, re
from playwright.sync_api import sync_playwright
d = sys.argv[1]
pairs = []
for f in sorted(os.listdir(d)):
    if f.endswith('.json'):
        j = json.load(open(os.path.join(d, f)))
        def src(ref, ext):
            ns, _, n = ref.rpartition(':')
            base = d if ns == 'frieren_arcana' else '/home/claude/w/vanilla/assets/minecraft/shaders/core'
            return open(os.path.join(base, n + ext)).read()
        pairs.append((f, src(j['vertex'], '.vsh'), src(j['fragment'], '.fsh')))
INC = '/home/claude/w/vanilla/assets/minecraft/shaders/include/'
def es(src):
    src = re.sub(r'#moj_import <([^>]+)>', lambda m: open(INC + m.group(1)).read().replace('#version 150', ''), src)
    return re.sub(r'#version 150', '#version 300 es\nprecision highp float;\nprecision highp int;\nprecision highp sampler2D;', src)
with sync_playwright() as p:
    b = p.chromium.launch(args=['--use-gl=swiftshader', '--enable-unsafe-swiftshader', '--ignore-gpu-blocklist'])
    pg = b.new_page()
    pg.set_content('<canvas id=c></canvas>')
    ok = True
    for name, vs, fs in pairs:
        r = pg.evaluate('''([vs, fs]) => {
          const gl = document.getElementById('c').getContext('webgl2');
          if (!gl) return 'no webgl2';
          const out = [];
          const mk = (t, s) => { const sh = gl.createShader(t); gl.shaderSource(sh, s); gl.compileShader(sh);
              if (!gl.getShaderParameter(sh, gl.COMPILE_STATUS)) out.push((t == gl.VERTEX_SHADER ? 'VS: ' : 'FS: ') + gl.getShaderInfoLog(sh)); return sh; };
          const p = gl.createProgram(); gl.attachShader(p, mk(gl.VERTEX_SHADER, vs)); gl.attachShader(p, mk(gl.FRAGMENT_SHADER, fs)); gl.linkProgram(p);
          if (!gl.getProgramParameter(p, gl.LINK_STATUS)) out.push('LINK: ' + gl.getProgramInfoLog(p));
          return out.join('\\n') || 'ok';
        }''', [es(vs), es(fs)])
        print(name, '->', r)
        ok = ok and r == 'ok'
    b.close()
print('ALL OK' if ok else 'FAILURES')
