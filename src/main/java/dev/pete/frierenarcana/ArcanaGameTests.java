package dev.pete.frierenarcana;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager.SelectionOption;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import io.redspace.ironsspellbooks.capabilities.magic.TelekinesisData;
import io.redspace.ironsspellbooks.entity.spells.firebolt.FireboltProjectile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredItem;

@GameTestHolder("frieren_arcana")
@PrefixGameTestTemplate(false)
public final class ArcanaGameTests {
    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void graphicalBarrierContract(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BarrierData data = BarrierData.get(level);
        CompoundTag before = data.save(new CompoundTag(), level.registryAccess());
        Vec3 center = Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 20, 0)));
        UUID owner = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putUUID("owner", owner);
        tag.putDouble("x", center.x);
        tag.putDouble("y", center.y);
        tag.putDouble("z", center.z);
        tag.putInt("radius", 6);
        tag.putBoolean("defensive", false);
        ListTag fields = new ListTag();
        fields.add(tag);
        CompoundTag root = new CompoundTag();
        root.put("fields", fields);
        BarrierData loaded = BarrierData.load(root, level.registryAccess());
        level.getDataStorage().set("frieren_arcana_barriers", loaded);
        ArmorStand inside = new ArmorStand(level, center.x, center.y - 1.0, center.z);
        ArmorStand outside = new ArmorStand(level, center.x + 12.0, center.y - 1.0, center.z);

        try {
            helper.assertTrue(loaded.owned(owner, false) != null, "Saved barrier owner must survive loading");
            helper.assertTrue(loaded.owned(UUID.randomUUID(), false) == null, "Different player must not own the barrier");
            inside.setNoGravity(true);
            outside.setNoGravity(true);
            level.addFreshEntity(inside);
            level.addFreshEntity(outside);
            inside.move(MoverType.SELF, new Vec3(20.0, 0.0, 0.0));
            helper.assertTrue(inside.getX() > center.x + 0.1, "Test entity must actually move before reaching the field");
            helper.assertTrue(inside.getX() < center.x + 6.01, "Entity inside must not cross outward through graphical field");
            outside.move(MoverType.SELF, new Vec3(-24.0, 0.0, 0.0));
            helper.assertTrue(outside.getX() < center.x + 11.9, "Incoming entity must actually move before reaching the field");
            helper.assertTrue(outside.getX() > center.x + 5.99, "Fast entity outside must not tunnel inward through graphical field");
            level.setWeatherParameters(0, 1000, true, false);
            helper.assertTrue(!level.isRainingAt(BlockPos.containing(center)), "Confining field must suppress rain inside");
            helper.assertTrue(loaded.crosses(center, center.add(12.0, 0.0, 0.0), false), "Teleport boundary must detect outgoing crossing");
            BarrierData persisted = BarrierData.load(loaded.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
            helper.assertTrue(persisted.owned(owner, false).id.equals(id), "Save/load must retain barrier identity");
            helper.assertTrue(
                loaded.validate(level, BlockPos.containing(center), 2, true) == null, "A defensive field must be allowed fully inside an examination sphere"
            );
            helper.assertTrue(loaded.remove(level, id, false), "Barrier must release successfully");
            inside.move(MoverType.SELF, new Vec3(20.0, 0.0, 0.0));
            helper.assertTrue(inside.getX() > center.x + 6.0, "Released field must stop blocking movement");
            helper.succeed();
        } finally {
            inside.discard();
            outside.discard();
            level.setWeatherParameters(6000, 0, false, false);
            level.getDataStorage().set("frieren_arcana_barriers", BarrierData.load(before, level.registryAccess()));
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void projectileAndRayBarrierContract(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BarrierData before = BarrierData.get(level);
        Vec3 c = Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 35, 0)));
        UUID owner = UUID.randomUUID();
        ArrayList<Entity> entities = new ArrayList<>();

        try {
            for (boolean defensive : new boolean[]{false, true}) {
                CompoundTag f = new CompoundTag();
                f.putUUID("id", UUID.randomUUID());
                f.putUUID("owner", owner);
                f.putDouble("x", c.x);
                f.putDouble("y", c.y);
                f.putDouble("z", c.z);
                f.putInt("radius", 6);
                f.putBoolean("defensive", defensive);
                ListTag list = new ListTag();
                list.add(f);
                CompoundTag root = new CompoundTag();
                root.put("fields", list);
                level.getDataStorage().set("frieren_arcana_barriers", BarrierData.load(root, level.registryAccess()));
                Snowball arrow = new Snowball(level, c.x + 12.0, c.y, c.z);
                entities.add(arrow);
                level.addFreshEntity(arrow);
                arrow.setPos(c.x, c.y, c.z);
                helper.assertTrue(arrow.isRemoved(), "Direct-position incoming projectile must be consumed for both barrier types");
                Snowball raw = new Snowball(level, c.x + 12.0, c.y, c.z);
                entities.add(raw);
                level.addFreshEntity(raw);
                raw.setPosRaw(c.x, c.y, c.z);
                helper.assertTrue(raw.isRemoved(), "Raw-position incoming projectile must be consumed");
                FakePlayer caster = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ray-test"));
                caster.setPos(c.x + 12.0, c.y - 1.0, c.z);
                caster.setYRot(90.0F);
                caster.setYHeadRot(90.0F);
                caster.setXRot(0.0F);
                FireboltProjectile bolt = new FireboltProjectile(level, caster);
                bolt.setPos(c.x + 12.0, c.y, c.z);
                bolt.setDeltaMovement(-24.0, 0.0, 0.0);
                entities.add(bolt);
                level.addFreshEntity(bolt);
                bolt.handleHitDetection();
                helper.assertTrue(bolt.isRemoved(), "Iron projectile pre-movement hit ray must stop at the barrier");
                helper.assertTrue(bolt.getDeltaMovement().length() <= 6.01, "Iron collision ray must be shortened before damage detection");
                BlockHitResult hit = level.clip(new ClipContext(c.add(12.0, 0.0, 0.0), c.add(-12.0, 0.0, 0.0), Block.COLLIDER, Fluid.NONE, caster));
                helper.assertTrue(
                    hit.getType() == Type.BLOCK && Math.abs(hit.getLocation().x - (c.x + 6.0)) < 0.02, "Hitscan must return the first graphical surface"
                );
                Vec3 outgoing = BarrierHooks.clip(level, c, c.add(12.0, 0.0, 0.0));
                helper.assertTrue(defensive ? outgoing.x > c.x + 11.99 : outgoing.x < c.x + 6.01, "Only defensive magic may pass outgoing attacks");
                ArmorStand walker = new ArmorStand(level, c.x, c.y, c.z);
                entities.add(walker);
                level.addFreshEntity(walker);
                walker.moveTo(c.x + 12.0, c.y, c.z);
                helper.assertTrue(
                    defensive ? walker.getX() > c.x + 11.99 : walker.getX() < c.x + 6.0, "moveTo must obey confinement while defensive barriers permit walking"
                );
                if (ModList.get().isLoaded("zoltraak_cinematic")) {
                    try {
                        Class<?> type = Class.forName("com.frierenflight.zoltraakcinematic.entity.ZoltraakCinematicBeamEntity");
                        Entity beam = (Entity)type.getConstructor(Level.class, LivingEntity.class, float.class, float.class)
                            .newInstance(level, caster, 10.0F, 24.0F);
                        beam.setPos(c.x + 12.0, c.y, c.z);
                        beam.setYRot(90.0F);
                        beam.setXRot(0.0F);
                        entities.add(beam);
                        float length = (Float)type.getMethod("getBeamLength").invoke(beam);
                        helper.assertTrue(
                            length < 7.1F && length > 4.5F,
                            "Supplied cinematic beam must stop at surface; actual length="
                                + length
                                + " origin="
                                + type.getMethod("visualOrigin", float.class).invoke(beam, 1.0F)
                        );
                    } catch (ReflectiveOperationException var26) {
                        throw new IllegalStateException("Cinematic beam integration failed", var26);
                    }
                }
            }

            helper.succeed();
        } finally {
            entities.forEach(Entity::discard);
            level.getDataStorage().set("frieren_arcana_barriers", before);
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void levitationAndVisibleCooldownContract(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FakePlayer p = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "arcana-channel-test"));
        Vec3 from = Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 35, 0)));
        p.setPos(from.x, from.y, from.z);
        p.setYRot(0.0F);
        p.setYHeadRot(0.0F);
        p.setXRot(0.0F);
        MagicData magic = MagicData.getPlayerMagicData(p);
        magic.setMana(600.0F);
        ArcanaSpell lift = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.LIFT).get();
        ArcanaSpell breaker = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
        ItemEntity item = new ItemEntity(level, from.x, from.y + 1.5, from.z + 5.0, new ItemStack(Items.DIAMOND));
        level.addFreshEntity(item);
        Cow cow = EntityType.COW.create(level);

        try {
            helper.assertTrue(lift.getCastType() == CastType.CONTINUOUS, "Levitation must use Iron's continuous casting lifecycle");
            ItemStack book = new ItemStack(Items.STICK);
            ISpellContainer.createImbuedContainer(lift, 1, book);
            p.setItemSlot(EquipmentSlot.MAINHAND, book);
            SelectionOption selection = ArcanaModes.equipped(p, ArcanaSpell.Kind.LIFT);
            helper.assertTrue(
                lift.attemptInitiateCast(ItemStack.EMPTY, 1, level, p, CastSource.SPELLBOOK, true, selection.slot),
                "Aimed item must start a real continuous cast"
            );
            helper.assertTrue(magic.isCasting() && item.isNoGravity(), "Live item channel must suspend gravity");
            p.setYRot(-30.0F);
            p.setYHeadRot(-30.0F);
            ArcanaModes.request(p, 4);
            helper.assertTrue(
                !magic.isCasting() && !item.isNoGravity() && item.getDeltaMovement().length() > 1.5,
                "Throw must end channel, restore gravity and launch held item"
            );
            item.discard();
            p.setYRot(0.0F);
            p.setYHeadRot(0.0F);
            magic.setMana(600.0F);
            magic.getPlayerCooldowns().clearCooldowns();
            cow.setPos(from.x, from.y + 0.5, from.z + 5.0);
            cow.setNoAi(true);
            level.addFreshEntity(cow);
            helper.assertTrue(
                lift.attemptInitiateCast(ItemStack.EMPTY, 1, level, p, CastSource.SPELLBOOK, true, selection.slot),
                "Living target must start telekinetic channel"
            );
            helper.assertTrue(magic.getAdditionalCastData() instanceof TelekinesisData, "Living channel must use installed Iron Telekinesis cast data");
            p.setYRot(-30.0F);
            p.setYHeadRot(-30.0F);
            AdvancedMagic.liftTick(p, 1, magic);
            helper.assertTrue(cow.getDeltaMovement().lengthSqr() > 0.0, "Installed Iron force must move the grabbed creature");
            ArcanaModes.request(p, 4);
            helper.assertTrue(!magic.isCasting() && cow.getDeltaMovement().length() > 1.5, "Living target must throw and end channel");
            p.getAttribute(AttributeRegistry.COOLDOWN_REDUCTION).setBaseValue(1.25);
            ArcanaCooldowns.begin(p, breaker, CastSource.SCROLL);
            CooldownInstance cooldown = magic.getPlayerCooldowns().getSpellCooldowns().get(breaker.getSpellId());
            helper.assertTrue(
                cooldown != null && cooldown.getCooldownRemaining() > 0 && cooldown.getCooldownRemaining() <= breaker.getSpellCooldown(),
                "Scroll breaker cooldown must exist in native Iron cooldown manager"
            );
            long remaining = ArcanaEvents.flags(p).getLong("breakerReadyTick") - p.server.overworld().getGameTime();
            helper.assertTrue(remaining == (long)cooldown.getCooldownRemaining(), "Saved enforcement and native Iron display must agree");
            magic.getPlayerCooldowns().clearCooldowns();
            ArcanaCooldowns.restore(p);
            helper.assertTrue(
                (long)magic.getPlayerCooldowns().getSpellCooldowns().get(breaker.getSpellId()).getCooldownRemaining() == remaining,
                "Relog restoration must recover visible cooldown without resetting its deadline"
            );
            helper.succeed();
        } finally {
            AdvancedMagic.release(p);
            magic.resetCastingState();
            magic.getPlayerCooldowns().clearCooldowns();
            p.getInventory().clearContent();
            item.discard();
            cow.discard();
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void expandedMagicContract(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FakePlayer p = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "arcana-test"));
        Vec3 center = Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 20, 0)));
        p.setPos(center.x, center.y, center.z);
        p.setYRot(0.0F);
        p.setXRot(0.0F);
        MagicData magic = MagicData.getPlayerMagicData(p);
        ArcanaSpell heavy = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.HEAVY).get();
        ArcanaSpell barrage = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.BARRAGE).get();
        ArcanaSpell flight = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.FLIGHT).get();

        try {
            magic.setMana(600.0F);
            p.getAttribute(AttributeRegistry.MAX_MANA).setBaseValue(100.0);
            helper.assertTrue(!heavy.ready(p, 1, false), "Starter mana capacity must not unlock heavy blast");
            p.getAttribute(AttributeRegistry.MAX_MANA).setBaseValue(600.0);
            magic.setMana(600.0F);
            helper.assertTrue(heavy.ready(p, 1, false), "Endgame capacity with enough mana must unlock heavy blast");
            helper.assertTrue(heavy.getEffectiveCastTime(1, p) >= ArcanaConfig.HEAVY_CHARGE.get() * 20, "Heavy charge must retain its minimum");
            magic.setMana(10.0F);
            helper.assertTrue(!heavy.ready(p, 1, false), "Heavy blast must reject insufficient current mana");
            magic.setMana(600.0F);
            p.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            helper.assertTrue(!flight.ready(p, 1, false), "Flight must reject no staff");
            p.setItemSlot(EquipmentSlot.OFFHAND, FrierenArcana.STAFF.get().getDefaultInstance());
            helper.assertTrue(flight.ready(p, 1, false), "Offhand staff must permit flight");
            ArcanaEvents.flags(p).putBoolean("flight", true);
            ArcanaEvents.updateFlight(p);
            helper.assertTrue(p.getAbilities().mayfly, "Staff flight must grant ordinary flight permission");
            p.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            ArcanaEvents.tick(new Post(p));
            helper.assertTrue(!ArcanaEvents.flags(p).getBoolean("flight") && !p.getAbilities().mayfly, "Removing staff must end survival flight");
            ArcanaModes.request(p, 2);
            helper.assertTrue(!magic.isCasting(), "Key requests without an equipped spell must be rejected");
            ItemStack book = new ItemStack(Items.STICK);
            ISpellContainer.createImbuedContainer(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.ZOLTRAAK).get(), 5, book);
            p.setItemSlot(EquipmentSlot.MAINHAND, book);
            ArcanaEvents.flags(p).remove("nextModeRequest");
            ArcanaModes.request(p, 2);
            helper.assertTrue(
                magic.isCasting() && magic.getCastingSpellId().equals(heavy.getSpellId()), "Mastered equipped Zoltraak must begin a real Iron heavy cast"
            );
            heavy.onServerCastComplete(level, 1, p, magic, true);
            magic.setMana(100.0F);
            ArcanaModes.startBarrage(p, 5);
            magic.setMana(150.0F);
            ArcanaModes.tick(p);
            helper.assertTrue(magic.getMana() < 100.0F, "Regeneration must not replenish the active barrage budget");
            magic.setMana(0.01F);
            ArcanaModes.tick(p);
            helper.assertTrue(!ArcanaEvents.flags(p).getBoolean("barrage") && magic.getMana() == 0.0F, "Exhaustion must end barrage without negative mana");
            int previous = 0;

            for (DeferredItem<Item> item : FrierenArcana.DEVICES) {
                BarrierDevice device = (BarrierDevice)item.get();
                helper.assertTrue(device.radius() > previous, "Device tiers must increase radius");
                previous = device.radius();
            }

            helper.assertTrue(previous == ArcanaConfig.DEVICE_RADIUS.get(), "Top device must use configured grand radius");
            helper.succeed();
        } finally {
            ArcanaModes.stopBarrage(p);
            ExpandedMagic.release(p);
            ArcanaEvents.flags(p).putBoolean("flight", false);
            ArcanaEvents.updateFlight(p);
            magic.resetCastingState();
            p.getInventory().clearContent();
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void combatAndSupportContract(GameTestHelper helper) {
        ServerLevel world = helper.getLevel();
        FakePlayer p = FakePlayerFactory.get(world, new GameProfile(UUID.randomUUID(), "arcana-combat-test"));
        Vec3 origin = Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 20, 0)));
        p.setPos(origin.x, origin.y, origin.z);
        p.setYRot(0.0F);
        p.setXRot(0.0F);
        Cow a = EntityType.COW.create(world);
        Cow b = EntityType.COW.create(world);
        a.setPos(origin.x, origin.y, origin.z + 5.0);
        b.setPos(origin.x, origin.y, origin.z + 10.0);
        a.setNoAi(true);
        b.setNoAi(true);
        a.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        b.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        a.setHealth(200.0F);
        b.setHealth(200.0F);
        world.addFreshEntity(a);
        world.addFreshEntity(b);

        try {
            ArcanaSpell heavy = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.HEAVY).get();
            ArcanaModes.fire(p, heavy, 1, p.getLookAngle(), 30.0, 2.0F, 100.0F, 2, true);
            helper.assertTrue(a.getHealth() < 200.0F && b.getHealth() < 200.0F, "Wide heavy beam must damage multiple aligned living targets");
            p.setXRot(10.0F);
            ArcanaSpell bind = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.BIND).get();
            ExpandedMagic.cast(bind, p, 1);
            helper.assertTrue(ExpandedMagic.held(a), "Sight binding must restrain the aimed living target");
            double x = a.getX();
            a.move(MoverType.SELF, new Vec3(1.0, 0.0, 0.0));
            helper.assertTrue(a.getX() == x, "Bound target must not move through ordinary movement");
            p.setYRot(180.0F);
            ExpandedMagic.tick(p);
            helper.assertTrue(!ExpandedMagic.held(a), "Looking away must release sight binding");
            p.setHealth(5.0F);
            ExpandedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.HEAL).get(), p, 1);
            helper.assertTrue(p.getHealth() > 5.0F, "Goddess healing must restore health");
            p.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
            ExpandedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.CLEANSE).get(), p, 1);
            helper.assertTrue(!p.hasEffect(MobEffects.POISON) && p.hasEffect(MobEffects.BLINDNESS), "Level I cleanse must remove poison but retain blindness");
            ExpandedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.CLEANSE).get(), p, 3);
            helper.assertTrue(!p.hasEffect(MobEffects.BLINDNESS), "Level III cleanse must unlock blindness removal");
            helper.succeed();
        } finally {
            ExpandedMagic.release(p);
            a.discard();
            b.discard();
            p.removeAllEffects();
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void spellGuideAndAdvancedMagicContract(GameTestHelper helper) {
        ServerLevel world = helper.getLevel();
        FakePlayer p = FakePlayerFactory.get(world, new GameProfile(UUID.randomUUID(), "arcana-guide-test"));
        Vec3 origin = Vec3.atCenterOf(helper.absolutePos(new BlockPos(0, 20, 0)));
        p.setPos(origin.x, origin.y, origin.z);
        p.setYRot(0.0F);
        p.setXRot(0.0F);
        List<SpellGuidePages.Page> pages = SpellGuidePages.all();
        int expected = FrierenArcana.SPELL_MAP.values().stream().mapToInt(h -> h.get().getMaxLevel() - h.get().getMinLevel() + 1).sum();
        helper.assertTrue(pages.size() == expected, "JEI data must contain a page for every spell level");
        Set<String> seen = new HashSet<>();

        for (SpellGuidePages.Page page : pages) {
            SpellData contained = ISpellContainer.get(page.scroll()).getSpellAtIndex(0);
            helper.assertTrue(
                contained.getSpell().equals(page.spell()) && contained.getLevel() == page.level(), "JEI guide scroll must encode its exact spell and level"
            );
            helper.assertTrue(seen.add(page.spell().getSpellId() + ":" + page.level()), "JEI guide entries must be unique");
        }

        ItemEntity item = new ItemEntity(world, origin.x, origin.y + 1.2, origin.z + 4.0, new ItemStack(Items.DIAMOND));
        item.setNoGravity(false);
        world.addFreshEntity(item);

        try {
            ArcanaSpell firewind = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.FIREWIND).get();
            ArcanaSpell petals = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PETALS).get();
            p.getAttribute(AttributeRegistry.MAX_MANA).setBaseValue(600.0);
            MagicData.getPlayerMagicData(p).setMana(600.0F);
            helper.assertTrue(!firewind.ready(p, 1, false), "Fire-Wind must require a preceding wind cast");
            AdvancedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.TORNADO).get(), p, 1);
            helper.assertTrue(AdvancedMagic.hasWind(p) && firewind.ready(p, 1, false), "Tornado must open the Fire-Wind combo window");
            AdvancedMagic.cast(firewind, p, 1);
            helper.assertTrue(!AdvancedMagic.hasWind(p), "Fire-Wind must consume its setup window");
            helper.assertTrue(!petals.ready(p, 1, false), "Steel Petals must require flowers");
            AdvancedMagic.markFlowers(p);
            helper.assertTrue(petals.ready(p, 1, false), "Cosmetic flower magic must unlock Steel Petals nearby");
            helper.assertTrue(AdvancedMagic.itemTarget(p) == item, "Levitation must resolve an aimed dropped item");
            AdvancedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.LIFT).get(), p, 1);
            helper.assertTrue(item.isNoGravity(), "Object levitation must suspend item gravity");
            AdvancedMagic.release(p);
            helper.assertTrue(!item.isNoGravity(), "Releasing personal magic must restore original item gravity");
            AdvancedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.LIFT).get(), p, 1);
            AdvancedMagic.recover(item);
            helper.assertTrue(!item.isNoGravity(), "Recovered item markers must prevent permanent floating after reload");
            helper.succeed();
        } finally {
            AdvancedMagic.release(p);
            item.discard();
        }
    }
}
