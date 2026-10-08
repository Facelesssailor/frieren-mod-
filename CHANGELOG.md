# Changelog

Each version is a tagged commit (`v<version>`). Code/resource lines are derived from the released jars.

## 1.5.10

- Cutscene shots matched to ep. 21; the whitish-green spear replaces the speed-line flash; pre-release ground glow removed.

Code: changed client.BreakerFx, client.CinemaDirector, client.CinemaOverlay, client.SpellCircleFx.

## 1.5.9

- Flight Staff removed: any of Iron's staffs works for flight.
- Barrier break: no green barrier left during or after the shatter; crystal shards rebuilt.
- Fixed a crash when skipping the cutscene; guide book updates.

Code: added StaffView; changed ArcanaGameTests, FrierenArcana, client.ArcanaClient, client.BarrierLook, client.BreakerFx, client.ChargeFx, client.CinemaDirector, client.CinemaOverlay, client.SpellCircleFx, client.SpellFx, client.VisualClientSmoke, compat.jei.ArcanaJeiPlugin, compat.jei.JeiClientSmoke.

Resources: added patchouli_books/guide/en_us/entries/items/compendium.json; changed guide/en_us.txt, patchouli_books/guide/en_us/categories/basics.json, patchouli_books/guide/en_us/categories/items.json, patchouli_books/guide/en_us/entries/barriers/barrier_breaker.json, patchouli_books/guide/en_us/entries/basics/compat.json, patchouli_books/guide/en_us/entries/basics/cutscenes.json, patchouli_books/guide/en_us/entries/support/flight.json, shaders/core/arcana_energy.fsh, shaders/core/arcana_refraction.fsh, data/tags/item/flight_staves.json; removed models/item/flight_staff.json, patchouli_books/guide/en_us/entries/items/flight_staff.json, textures/item/flight_staff.png, textures/item/flight_staff_3d.png, data/recipe/flight_staff.json.

## 1.5.8

- Built-in guide book, the Magic Compendium, that works without Patchouli.
- Barrier shatter sound; barrier break visual fixes.

Code: added GuideBook, GuideBookItem, client.BreakerSound, client.GuideBookClient; changed ArcanaSpell, client.ArcanaClient, client.BarrierLook, client.BreakerFx, client.CinemaDirector, client.CinemaOverlay, client.SpellCircleFx, client.SpellFx.

Resources: added guide/en_us.txt, models/item/guide_book.json, textures/item/guide_book.png, data/advancement/grant_compendium.json; changed shaders/core/arcana_energy.fsh, shaders/core/arcana_refraction.fsh, data/loot_table/grant/guide_book.json, data/patchouli_books/guide/book.json, data/recipe/guide_book.json; removed data/advancement/grant_guide_book.json.

## 1.5.7

- Patchouli guide book content.
- Keybindings no longer block Iron's cast key (KeyCompat); new key to throw a levitated target.

Code: added client.KeyCompat; changed CircleNet, DamageInfo, NewMagic, client.BlackHoleCinema, client.CinemaDirector, client.CircleArt, client.SpellCircleFx, client.SpellFx.

Resources: added patchouli_books/guide/en_us/categories/barriers.json, patchouli_books/guide/en_us/categories/basics.json, patchouli_books/guide/en_us/categories/items.json, patchouli_books/guide/en_us/categories/offense.json, patchouli_books/guide/en_us/categories/support.json, patchouli_books/guide/en_us/entries/barriers/barrier_breaker.json, patchouli_books/guide/en_us/entries/barriers/defensive_barrier.json, patchouli_books/guide/en_us/entries/barriers/examination_barrier.json, patchouli_books/guide/en_us/entries/barriers/shield_breaking.json, patchouli_books/guide/en_us/entries/basics/casting.json, patchouli_books/guide/en_us/entries/basics/circles.json, patchouli_books/guide/en_us/entries/basics/compat.json (+38 more).

## 1.5.6

- Rebuild with no code changes.

## 1.5.5

- Damage lines in every spell description (DamageInfo); cutscene camera placement (CinemaFrame); spell guide text rewritten.

Code: added DamageInfo, client.CinemaFrame; changed ArcanaSpell, NewSpell, client.ArcanaKeys, client.BlackHoleCinema, client.CinemaDirector, client.SpellFx, mixin.CinematicMouseMixin, mixin.WeatherBarrierMixin.

## 1.5.4

- Black Hole shape; effect fixes; seated flight tweak.

Code: added BlackHoleShape; changed BreakerAim, CircleNet, NewMagic, ShieldBreak, client.BlackHoleCinema, client.PixelFx, client.SeatedFlight, client.SpellFx.

## 1.5.3

- Hand-drawn magic circle designs (CircleArt).

Code: added client.CircleArt; changed CircleNet, client.BreakerFx, client.CastCircles, client.CinemaDirector, client.SpellCircleFx, client.SpellFx.

## 1.5.2

- Spell circle fix.

Code: changed client.SpellCircleFx.

## 1.5.1

- Black Hole cutscene; Barrier Breaker and Black Hole descriptions.

Code: added client.BlackHoleCinema; changed NewMagic, client.CastCircles, client.CinemaDirector, client.SpellCircleFx, client.SpellFx.

## 1.5.0

- New spells: Black Hole, The Height of Magic, Golem Magic - Stone Fist.
- Creative tab renamed to "Frieren: Magic"; spell descriptions rewritten.

Code: added NewMagic, NewSpell; changed ArcanaSpell, BarrageFern, BreakerAim, FrierenArcana, ShieldBreak, client.ArcanaClient, client.BarrierLook, client.BreakerFx, client.CinemaDirector, client.CinemaOverlay, client.SpellCircleFx, client.SpellFx.

Resources: added textures/gui/spell_icons/black_hole.png, textures/gui/spell_icons/golem_fist.png, textures/gui/spell_icons/height_of_magic.png; changed shaders/core/arcana_energy.fsh.

## 1.4.6

- Barrage tuning.

Code: changed BarrageFern.

## 1.4.5

- Barrage tuning.

Code: changed BarrageFern, client.SpellFx.

## 1.4.4

- Barrage tuning.

Code: changed BarrageFern, client.SpellFx.

## 1.4.3

- Barrage networking and aim assist (BarrageNet, ZoltraakAim, BarrageClient); barrage guide text.

Code: added BarrageNet, ZoltraakAim, client.BarrageClient; changed ArcanaModes, ArcanaSpell, client.CastCircles, client.SpellFx.

## 1.4.2

- Defensive barriers break with their own effect (ShieldBreak).

Code: added ShieldBreak; changed ArcanaModes, client.BreakerFx, client.SpellFx.

## 1.4.1

- Pixel-art effect sprites (PixelFx); Barrier Breaker aiming (BreakerAim).

Code: added BreakerAim, client.PixelFx; changed ArcanaSpell, client.BreakerFx, client.CinemaDirector, client.SpellCircleFx, client.SpellFx.

Resources: added textures/fx/pixel_fx.png.

## 1.4.0

- New spell effects renderer (SpellFx).

Code: added client.SpellFx; changed ArcanaSpell, client.ArcanaClient, client.BreakerFx, client.CinemaDirector, client.CinemaOverlay, client.SpellCircleFx.

## 1.3.9

- Barrier break effects rebuilt (BreakerFx, BarrierLook, ChargeFx) with a full-screen cutscene overlay.
- Mouse locked during cutscenes; 3D textures for the barrier devices, release sigil and flight staff.

Code: added client.BarrierLook, client.BreakerFx, client.ChargeFx, client.CinemaOverlay, mixin.CinematicMouseMixin; changed ArcanaSpell, client.ArcanaClient, client.CinemaDirector, client.SeatedFlight, client.SpellCircleFx.

Resources: added textures/item/barrier_device_1_3d.png, textures/item/barrier_device_2_3d.png, textures/item/barrier_device_3_3d.png, textures/item/barrier_device_4_3d.png, textures/item/barrier_device_5_3d.png, textures/item/flight_staff_3d.png, textures/item/release_sigil_3d.png; changed models/item/barrier_device_1.json, models/item/barrier_device_2.json, models/item/barrier_device_3.json, models/item/barrier_device_4.json, models/item/barrier_device_5.json, models/item/flight_staff.json, models/item/release_sigil.json, shaders/core/arcana_energy.fsh, shaders/core/arcana_refraction.fsh, frieren_arcana.mixins.json.

## 1.3.8

- Spell, barrier-hook and cutscene fixes.

Code: changed ArcanaModes, ArcanaSpell, BarrierHooks, client.ArcanaCinematic, client.ArcanaClient, client.CinemaDirector.

## 1.3.7

- Cutscene and dome look tuning.

Code: changed client.ArcanaClient, client.CinemaDirector, client.SpellCircleFx.

Resources: changed shaders/core/arcana_energy.fsh.

## 1.3.6

- Cutscene camera and spell-circle tuning; shader updates.

Code: changed client.CinemaDirector, client.SpellCircleFx.

Resources: changed shaders/core/arcana_energy.fsh, shaders/core/arcana_refraction.fsh.

## 1.3.5

- Fern-style Zoltraak barrage (BarrageFern).
- Casting circles shown to other players (CircleNet, CastCircles).

Code: added BarrageFern, CircleNet, client.CastCircles; changed ArcanaModes, ArcanaSpell, client.ArcanaClient, client.SpellCircleFx.

## 1.3.4

- First cutscene director for the Barrier Breaker (CinemaDirector); dome shader update.

Code: added client.CinemaDirector; changed client.ArcanaCinematic, client.ArcanaClient.

Resources: changed shaders/core/arcana_energy.fsh.

## 1.3.3

- Spell circles drawn around the caster while casting (SpellCircleFx).
- Damage/effect previews on scrolls; sliding along barrier walls (BarrierSlide).
- Item models moved from OBJ meshes to JSON models.

Code: added BarrierSlide, client.ScrollPreview, client.SpellCircleFx; changed ArcanaSpell, BarrierHooks, client.ArcanaCinematic, client.ArcanaClient, client.SeatedFlight.

Resources: changed models/item/barrier_device_1.json, models/item/barrier_device_2.json, models/item/barrier_device_3.json, models/item/barrier_device_4.json, models/item/barrier_device_5.json, models/item/flight_staff.json, models/item/release_sigil.json, shaders/core/arcana_refraction.fsh, textures/item/barrier_device_1.png, textures/item/barrier_device_2.png, textures/item/barrier_device_3.png, textures/item/barrier_device_4.png (+3 more); removed models/item/arcana_materials.mtl, models/item/barrier_device_1.obj, models/item/barrier_device_2.obj, models/item/barrier_device_3.obj, models/item/barrier_device_4.obj, models/item/barrier_device_5.obj, models/item/flight_staff.obj, models/item/release_sigil.obj, textures/item/arcana_materials.png.

## 1.3.2

- Seated flight: the caster sits on the staff while flying (new pose and held-staff mixins).
- Keybinding changes; item model files updated.

Code: added client.SeatedFlight, mixin.SeatedHandMixin, mixin.SeatedPoseMixin; changed ArcanaModes, client.ArcanaClient, client.ArcanaKeys.

Resources: changed models/item/arcana_materials.mtl, models/item/barrier_device_1.obj, models/item/barrier_device_2.obj, models/item/barrier_device_3.obj, models/item/barrier_device_4.obj, models/item/barrier_device_5.obj, models/item/flight_staff.obj, models/item/release_sigil.obj, textures/item/arcana_materials.png, frieren_arcana.mixins.json.

## 1.3.1

- Fixes to spell casting, the Barrier Breaker cutscene and the client renderer.

Code: changed ArcanaSpell, client.ArcanaCinematic, client.ArcanaClient.

## 1.3.0

- Original release: a Frieren-inspired add-on for Iron's Spells 'n Spellbooks 3.16 with 28 spells, examination and defensive barriers, Zoltraak modes, staff flight and the grand barrier devices.

