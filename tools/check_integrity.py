#!/usr/bin/env python3
"""Static integrity checks for Frieren Arcana. Exit code 1 on any problem.

Checks that everything the code and data refer to exists:
  * lang keys: spells (name + .guide) for both spell enums, items, error/status messages built in code, subtitles
  * sounds: every sounds.json entry's .ogg exists, every breaker cue the code plays is in sounds.json
  * item models and the textures they use, player animations the spells ask for
  * shader programs and the .vsh/.fsh files they name
  * every method's pushPose()/popPose() calls balance (an unbalanced pop crashes the game)
  * client cutscene code reads the cutscene clock (ArcanaClock), not System.nanoTime(), so captures stay exact

Run from the repository root: python3 tools/check_integrity.py
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, 'src/main/java/dev/pete/frierenarcana')
ASSETS = os.path.join(ROOT, 'src/main/resources/assets/frieren_arcana')
NS = 'frieren_arcana'

problems = []


def problem(msg):
    problems.append(msg)


def java_files():
    for d, _, fs in os.walk(JAVA):
        for f in fs:
            if f.endswith('.java'):
                p = os.path.join(d, f)
                yield p, open(p, encoding='utf-8').read()


SOURCES = dict(java_files())
lang = json.load(open(os.path.join(ASSETS, 'lang/en_us.json'), encoding='utf-8'))


def rel(p):
    return os.path.relpath(p, ROOT)


# ---------------------------------------------------------------------------------------------------------- lang
def enum_paths(src, enum_name):
    m = re.search(r'enum\s+' + enum_name + r'\s*\{(.*?);', src, re.S)
    return re.findall(r'[A-Z_]+\(\s*"([a-z0-9_]+)"', m.group(1)) if m else []


spell_src = SOURCES.get(os.path.join(JAVA, 'ArcanaSpell.java'), '')
new_src = SOURCES.get(os.path.join(JAVA, 'NewSpell.java'), '')
spells = enum_paths(spell_src, 'Kind') + enum_paths(new_src, 'K')
if len(spells) < 10:
    problem('could not read the spell enums (found %d spells)' % len(spells))
for s in spells:
    for k in ('spell.%s.%s' % (NS, s), 'spell.%s.%s.guide' % (NS, s)):
        if k not in lang:
            problem('missing lang key %s' % k)

for p, src in SOURCES.items():
    for k in re.findall(r'"((?:message|info|hud|cinematic|key|mode|jei|death|itemGroup|subtitles|item|spell)\.' + NS + r'\.[a-z0-9_.]*[a-z0-9_])"', src):
        if k not in lang and not any(x.startswith(k + '.') for x in lang):
            problem('%s uses lang key %s, which en_us.json lacks' % (rel(p), k))
    # keys built as "prefix." + code: the prefix must exist for at least one key
    for k in re.findall(r'"((?:message|info|death|mode)\.' + NS + r'\.[a-z0-9_.]*\.)"\s*\+', src):
        if not any(x.startswith(k) for x in lang):
            problem('%s builds lang keys under %s but none exist' % (rel(p), k))

# error codes for "message.frieren_arcana.error." + code
codes = set(re.findall(r'var4 = "([a-z_]+)"', spell_src))
codes |= set(re.findall(r'return "([a-z_]+)";', SOURCES.get(os.path.join(JAVA, 'BarrierData.java'), '')))
for c in sorted(codes):
    if 'message.%s.error.%s' % (NS, c) not in lang:
        problem('missing lang key message.%s.error.%s' % (NS, c))

items = set(re.findall(r'ITEMS\.register\(\s*"([a-z0-9_]+)"\s*,', '\n'.join(SOURCES.values())))
items |= {'barrier_device_%d' % i for i in range(1, 6)}
for it in sorted(items):
    if 'item.%s.%s' % (NS, it) not in lang:
        problem('missing lang key item.%s.%s' % (NS, it))
    if not os.path.exists(os.path.join(ASSETS, 'models/item/%s.json' % it)):
        problem('item %s has no model' % it)

# ---------------------------------------------------------------------------------------------------------- models
for f in sorted(os.listdir(os.path.join(ASSETS, 'models/item'))):
    m = json.load(open(os.path.join(ASSETS, 'models/item', f), encoding='utf-8'))
    for tex in (m.get('textures') or {}).values():
        if tex.startswith('#'):
            continue
        ns, _, path = tex.partition(':') if ':' in tex else ('minecraft', '', tex)
        if ns == NS and not os.path.exists(os.path.join(ASSETS, 'textures', path + '.png')):
            problem('model %s uses missing texture %s' % (f, tex))
    for key in ('model',):
        mp = (m.get('loader') and m.get(key)) or None
        if mp and mp.startswith(NS + ':') and not os.path.exists(os.path.join(ASSETS, 'models', mp.split(':', 1)[1])):
            problem('model %s uses missing %s %s' % (f, key, mp))

# ---------------------------------------------------------------------------------------------------------- sounds
sounds = json.load(open(os.path.join(ASSETS, 'sounds.json'), encoding='utf-8'))
for name, entry in sounds.items():
    for snd in entry.get('sounds', []):
        sid = snd if isinstance(snd, str) else snd.get('name')
        ns, _, path = sid.partition(':') if ':' in sid else (NS, '', sid)
        if ns == NS and not os.path.exists(os.path.join(ASSETS, 'sounds', path + '.ogg')):
            problem('sound %s: missing file sounds/%s.ogg' % (name, path))
    sub = entry.get('subtitle')
    if sub and sub not in lang:
        problem('sound %s: missing subtitle key %s' % (name, sub))
all_src = '\n'.join(SOURCES.values())
for cue in set(re.findall(r'(?:BreakerSound|BreakerAudio)\.\w+\(\s*"([a-z_]+)"', all_src)):
    if 'breaker.' + cue not in sounds:
        problem('breaker cue %s is played but not in sounds.json' % cue)

# ---------------------------------------------------------------------------------------------------------- animations
anims = {}
for d in ('player_animations', 'animations'):
    folder = os.path.join(ASSETS, d)
    if os.path.isdir(folder):
        for f in os.listdir(folder):
            anims.update(json.load(open(os.path.join(folder, f), encoding='utf-8')).get('animations', {}))
for a in set(re.findall(r'FrierenArcana\.id\("([a-z_]+)"\), true, true\)', all_src)):
    if a not in anims:
        problem('spell animation %s is not defined in player_animations' % a)

# ---------------------------------------------------------------------------------------------------------- shaders
shader_dir = os.path.join(ASSETS, 'shaders/core')
for f in sorted(os.listdir(shader_dir)):
    if f.endswith('.json'):
        prog = json.load(open(os.path.join(shader_dir, f), encoding='utf-8'))
        for stage in ('vertex', 'fragment'):
            ref = prog.get(stage)
            if not ref:
                continue
            ns, _, path = ref.partition(':') if ':' in ref else ('minecraft', '', ref)
            ext = '.vsh' if stage == 'vertex' else '.fsh'
            if ns == NS and not os.path.exists(os.path.join(shader_dir, path + ext)):
                problem('shader %s: missing %s%s' % (f, path, ext))
for p, src in SOURCES.items():
    for sh in re.findall(r'FrierenArcana\.id\("(arcana_[a-z_]+)"\),\s*DefaultVertexFormat', src):
        if not os.path.exists(os.path.join(shader_dir, sh + '.json')):
            problem('%s registers shader %s, which has no json' % (rel(p), sh))


# ---------------------------------------------------------------------------------------------------------- pose balance
def methods(src):
    """Yield (name, body) for every method-like block: a header ending in ')' followed by a brace block."""
    clean = re.sub(r'//[^\n]*', '', src)
    clean = re.sub(r'/\*.*?\*/', '', clean, flags=re.S)
    clean = re.sub(r'"(?:\\.|[^"\\])*"', '""', clean)
    for m in re.finditer(r'(\w+)\s*\([^;{}()]*(?:\([^()]*\)[^;{}()]*)*\)\s*(?:throws [\w., ]+)?\{', clean):
        name = m.group(1)
        if name in ('if', 'for', 'while', 'switch', 'catch', 'synchronized', 'try'):
            continue
        i = m.end()
        depth = 1
        while depth and i < len(clean):
            depth += {'{': 1, '}': -1}.get(clean[i], 0)
            i += 1
        yield name, clean[m.end():i]


for p, src in SOURCES.items():
    seen = set()
    for name, body in methods(src):
        # only count the method's own pushes/pops, not those of methods nested inside it (anonymous classes)
        pushes = len(re.findall(r'\.pushPose\(\)', body))
        pops = len(re.findall(r'\.popPose\(\)', body))
        if pushes != pops and (name, pushes, pops) not in seen:
            seen.add((name, pushes, pops))
            problem('%s: %s() has %d pushPose and %d popPose' % (rel(p), name, pushes, pops))

# ---------------------------------------------------------------------------------------------------------- cutscene clock
for p, src in SOURCES.items():
    if '/client/' in p and not p.endswith(('ArcanaClock.java', 'CinemaCapture.java')) and 'System.nanoTime()' in src:
        problem('%s reads System.nanoTime(); cutscene timing must go through ArcanaClock.nanos()' % rel(p))

if problems:
    print('INTEGRITY: %d problem(s)' % len(problems))
    for x in problems:
        print('  - ' + x)
    sys.exit(1)
print('INTEGRITY: OK (%d spells, %d items, %d sounds, %d animations, %d Java files)' % (len(spells), len(items), len(sounds), len(anims), len(SOURCES)))
