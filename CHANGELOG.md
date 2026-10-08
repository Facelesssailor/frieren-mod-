# Changelog

Each version is a tagged commit (`v<version>`). Code/resource lines are derived from the released jars.

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

