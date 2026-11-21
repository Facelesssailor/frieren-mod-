# frieren-mod-

## Frieren Arcana

A NeoForge 1.21.1 add-on for Iron's Spells 'n Spellbooks 3.16 built around *Frieren: Beyond Journey's End*:
examination and defensive barriers, Frieren's Barrier Breaker with its ep. 21 cutscene, Zoltraak and Fern's barrage,
flight on any of Iron's staffs, the Magic Compendium guide book, and more spells from the series.

**Requires:** Minecraft 1.21.1, NeoForge 21.1.235+, Iron's Spells 'n Spellbooks 3.16.x, GeckoLib, Curios, playerAnimator.

**Optional:** [Veil](https://modrinth.com/mod/veil) 4.x - real bloom and coloured light for the spells (off under shader packs).

### Versions

Every released version is one commit on `main`, oldest (1.3.0) first, and each version also has its own branch,
`release/<version>` (for example `release/1.5.11`), pointing at that version's commit. `CHANGELOG.md` lists what
changed in each.

### Layout

- `src/main/java` - the mod's code (`dev.pete.frierenarcana`)
- `src/main/resources` - assets, data, shaders, `META-INF/neoforge.mods.toml`, mixin config
- `tools/` (from 1.5.11) - the scripts used to build, patch, check and preview the mod;
  `tools/check_integrity.py` (from 1.6.0) checks resources and pose-stack balance
- `src/test/java` - `TimelineSweep`, run by `gradle check`: the Barrier Breaker camera and timeline at every film frame
- `ci/`, `.github/workflows/build.yml` - the CI build and the in-game capture

### Building and the in-game capture (from 1.6.0)

`gradle build` builds the jar (ModDevGradle). On GitHub Actions every push to a `rework/**` branch also runs
`gradle runCapture` under a virtual display: the game creates a forest world at dusk in the rain, raises an
examination barrier, casts Barrier Breaker through Iron's own casting path and films the cutscene one film frame
(1/24 s) per rendered frame (`CinemaCapture`, enabled by `-Dfrieren_arcana.cinemaCapture=true`). The jar, the video,
stills at the episode's timestamps and the log are published to the `rework-dist` branch; the same capture with Veil
installed goes to `rework-veil-dist`.

### About the sources

The mod was developed by editing and patching the released jars directly, so for most versions the Java here is
decompiled from that version's jar (Vineflower). It mirrors the shipped bytecode, with generated local variable names
and no original comments. In 1.5.11 the classes that were written or rewritten as source are included as written.
