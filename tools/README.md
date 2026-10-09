# Build tools (used from 1.5.11)

These are the scripts the 1.5.11 jar was made with. Paths inside them point at the workspace they ran in
(`/home/claude/w`), so adjust them before reuse.

- `build.sh` - compiles the changed sources against the real Minecraft/NeoForge/Iron's jars, applies `patch/Patch.java`
  (ASM bytecode edits to classes that are only kept as bytecode), copies shaders and sounds, and packs the jar with `pack.py`.
- `check/Link.java` - resolves every class/method/field reference in the jar against the game and mod jars.
- `check/V.java` - runs a type-checking bytecode verifier over every method.
- `check/glcheck.py` - compiles every shader through WebGL2 (headless Chromium) to catch GLSL errors.
- `stubs/` - stub generator used before the real game jars were available, plus the compile-view helper.
- `audio.py` - cuts the cutscene sounds out of ep. 21 (voices removed with a side-channel swap) into the `.ogg` files.
- `preview/` - offline renderer: `CineHarness` runs the mod's own drawing code headless and `render.html` draws it in
  WebGL; `run.py` drives it frame by frame, `compare.py` / `sheet.py` build side-by-side sheets against the episode.
