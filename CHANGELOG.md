# Changelog

Each version is a tagged commit (`v<version>`). Code/resource lines are derived from the released jars.

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

