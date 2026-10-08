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
    public static void graphicalBarrierContract(GameTestHelper var0) {
        ServerLevel var1 = var0.getLevel();
        BarrierData var2 = BarrierData.get(var1);
        CompoundTag var3 = var2.save(new CompoundTag(), var1.registryAccess());
        Vec3 var4 = Vec3.atCenterOf(var0.absolutePos(new BlockPos(0, 20, 0)));
        UUID var5 = UUID.randomUUID();
        UUID var6 = UUID.randomUUID();
        CompoundTag var7 = new CompoundTag();
        var7.putUUID("id", var6);
        var7.putUUID("owner", var5);
        var7.putDouble("x", var4.x);
        var7.putDouble("y", var4.y);
        var7.putDouble("z", var4.z);
        var7.putInt("radius", 6);
        var7.putBoolean("defensive", false);
        ListTag var8 = new ListTag();
        var8.add(var7);
        CompoundTag var9 = new CompoundTag();
        var9.put("fields", var8);
        BarrierData var10 = BarrierData.load(var9, var1.registryAccess());
        var1.getDataStorage().set("frieren_arcana_barriers", var10);
        ArmorStand var11 = new ArmorStand(var1, var4.x, var4.y - 1.0, var4.z);
        ArmorStand var12 = new ArmorStand(var1, var4.x + 12.0, var4.y - 1.0, var4.z);

        try {
            var0.assertTrue(var10.owned(var5, false) != null, "Saved barrier owner must survive loading");
            var0.assertTrue(var10.owned(UUID.randomUUID(), false) == null, "Different player must not own the barrier");
            var11.setNoGravity(true);
            var12.setNoGravity(true);
            var1.addFreshEntity(var11);
            var1.addFreshEntity(var12);
            var11.move(MoverType.SELF, new Vec3(20.0, 0.0, 0.0));
            var0.assertTrue(var11.getX() > var4.x + 0.1, "Test entity must actually move before reaching the field");
            var0.assertTrue(var11.getX() < var4.x + 6.01, "Entity inside must not cross outward through graphical field");
            var12.move(MoverType.SELF, new Vec3(-24.0, 0.0, 0.0));
            var0.assertTrue(var12.getX() < var4.x + 11.9, "Incoming entity must actually move before reaching the field");
            var0.assertTrue(var12.getX() > var4.x + 5.99, "Fast entity outside must not tunnel inward through graphical field");
            var1.setWeatherParameters(0, 1000, true, false);
            var0.assertTrue(!var1.isRainingAt(BlockPos.containing(var4)), "Confining field must suppress rain inside");
            var0.assertTrue(var10.crosses(var4, var4.add(12.0, 0.0, 0.0), false), "Teleport boundary must detect outgoing crossing");
            BarrierData var13 = BarrierData.load(var10.save(new CompoundTag(), var1.registryAccess()), var1.registryAccess());
            var0.assertTrue(var13.owned(var5, false).id.equals(var6), "Save/load must retain barrier identity");
            var0.assertTrue(
                var10.validate(var1, BlockPos.containing(var4), 2, true) == null, "A defensive field must be allowed fully inside an examination sphere"
            );
            var0.assertTrue(var10.remove(var1, var6, false), "Barrier must release successfully");
            var11.move(MoverType.SELF, new Vec3(20.0, 0.0, 0.0));
            var0.assertTrue(var11.getX() > var4.x + 6.0, "Released field must stop blocking movement");
            var0.succeed();
        } finally {
            var11.discard();
            var12.discard();
            var1.setWeatherParameters(6000, 0, false, false);
            var1.getDataStorage().set("frieren_arcana_barriers", BarrierData.load(var3, var1.registryAccess()));
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void projectileAndRayBarrierContract(GameTestHelper var0) {
        ServerLevel var1 = var0.getLevel();
        BarrierData var2 = BarrierData.get(var1);
        Vec3 var3 = Vec3.atCenterOf(var0.absolutePos(new BlockPos(0, 35, 0)));
        UUID var4 = UUID.randomUUID();
        ArrayList var5 = new ArrayList();

        try {
            for (boolean var9 : new boolean[]{false, true}) {
                CompoundTag var10 = new CompoundTag();
                var10.putUUID("id", UUID.randomUUID());
                var10.putUUID("owner", var4);
                var10.putDouble("x", var3.x);
                var10.putDouble("y", var3.y);
                var10.putDouble("z", var3.z);
                var10.putInt("radius", 6);
                var10.putBoolean("defensive", var9);
                ListTag var11 = new ListTag();
                var11.add(var10);
                CompoundTag var12 = new CompoundTag();
                var12.put("fields", var11);
                var1.getDataStorage().set("frieren_arcana_barriers", BarrierData.load(var12, var1.registryAccess()));
                Snowball var13 = new Snowball(var1, var3.x + 12.0, var3.y, var3.z);
                var5.add(var13);
                var1.addFreshEntity(var13);
                var13.setPos(var3.x, var3.y, var3.z);
                var0.assertTrue(var13.isRemoved(), "Direct-position incoming projectile must be consumed for both barrier types");
                Snowball var14 = new Snowball(var1, var3.x + 12.0, var3.y, var3.z);
                var5.add(var14);
                var1.addFreshEntity(var14);
                var14.setPosRaw(var3.x, var3.y, var3.z);
                var0.assertTrue(var14.isRemoved(), "Raw-position incoming projectile must be consumed");
                FakePlayer var15 = FakePlayerFactory.get(var1, new GameProfile(UUID.randomUUID(), "ray-test"));
                var15.setPos(var3.x + 12.0, var3.y - 1.0, var3.z);
                var15.setYRot(90.0F);
                var15.setYHeadRot(90.0F);
                var15.setXRot(0.0F);
                FireboltProjectile var16 = new FireboltProjectile(var1, var15);
                var16.setPos(var3.x + 12.0, var3.y, var3.z);
                var16.setDeltaMovement(-24.0, 0.0, 0.0);
                var5.add(var16);
                var1.addFreshEntity(var16);
                var16.handleHitDetection();
                var0.assertTrue(var16.isRemoved(), "Iron projectile pre-movement hit ray must stop at the barrier");
                var0.assertTrue(var16.getDeltaMovement().length() <= 6.01, "Iron collision ray must be shortened before damage detection");
                BlockHitResult var17 = var1.clip(new ClipContext(var3.add(12.0, 0.0, 0.0), var3.add(-12.0, 0.0, 0.0), Block.COLLIDER, Fluid.NONE, var15));
                var0.assertTrue(
                    var17.getType() == Type.BLOCK && Math.abs(var17.getLocation().x - (var3.x + 6.0)) < 0.02, "Hitscan must return the first graphical surface"
                );
                Vec3 var18 = BarrierHooks.clip(var1, var3, var3.add(12.0, 0.0, 0.0));
                var0.assertTrue(var9 ? var18.x > var3.x + 11.99 : var18.x < var3.x + 6.01, "Only defensive magic may pass outgoing attacks");
                ArmorStand var19 = new ArmorStand(var1, var3.x, var3.y, var3.z);
                var5.add(var19);
                var1.addFreshEntity(var19);
                var19.moveTo(var3.x + 12.0, var3.y, var3.z);
                var0.assertTrue(
                    var9 ? var19.getX() > var3.x + 11.99 : var19.getX() < var3.x + 6.0, "moveTo must obey confinement while defensive barriers permit walking"
                );
                if (ModList.get().isLoaded("zoltraak_cinematic")) {
                    try {
                        Class var20 = Class.forName("com.frierenflight.zoltraakcinematic.entity.ZoltraakCinematicBeamEntity");
                        Entity var21 = (Entity)var20.getConstructor(Level.class, LivingEntity.class, float.class, float.class)
                            .newInstance(var1, var15, 10.0F, 24.0F);
                        var21.setPos(var3.x + 12.0, var3.y, var3.z);
                        var21.setYRot(90.0F);
                        var21.setXRot(0.0F);
                        var5.add(var21);
                        float var22 = (Float)var20.getMethod("getBeamLength").invoke(var21);
                        var0.assertTrue(
                            var22 < 7.1F && var22 > 4.5F,
                            "Supplied cinematic beam must stop at surface; actual length="
                                + var22
                                + " origin="
                                + var20.getMethod("visualOrigin", float.class).invoke(var21, 1.0F)
                        );
                    } catch (ReflectiveOperationException var26) {
                        throw new IllegalStateException("Cinematic beam integration failed", var26);
                    }
                }
            }

            var0.succeed();
        } finally {
            var5.forEach(Entity::discard);
            var1.getDataStorage().set("frieren_arcana_barriers", var2);
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void levitationAndVisibleCooldownContract(GameTestHelper var0) {
        ServerLevel var1 = var0.getLevel();
        FakePlayer var2 = FakePlayerFactory.get(var1, new GameProfile(UUID.randomUUID(), "arcana-channel-test"));
        Vec3 var3 = Vec3.atCenterOf(var0.absolutePos(new BlockPos(0, 35, 0)));
        var2.setPos(var3.x, var3.y, var3.z);
        var2.setYRot(0.0F);
        var2.setYHeadRot(0.0F);
        var2.setXRot(0.0F);
        MagicData var4 = MagicData.getPlayerMagicData(var2);
        var4.setMana(600.0F);
        ArcanaSpell var5 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.LIFT).get();
        ArcanaSpell var6 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
        ItemEntity var7 = new ItemEntity(var1, var3.x, var3.y + 1.5, var3.z + 5.0, new ItemStack(Items.DIAMOND));
        var1.addFreshEntity(var7);
        Cow var8 = EntityType.COW.create(var1);

        try {
            var0.assertTrue(var5.getCastType() == CastType.CONTINUOUS, "Levitation must use Iron's continuous casting lifecycle");
            ItemStack var9 = new ItemStack(Items.STICK);
            ISpellContainer.createImbuedContainer(var5, 1, var9);
            var2.setItemSlot(EquipmentSlot.MAINHAND, var9);
            SelectionOption var10 = ArcanaModes.equipped(var2, ArcanaSpell.Kind.LIFT);
            var0.assertTrue(
                var5.attemptInitiateCast(ItemStack.EMPTY, 1, var1, var2, CastSource.SPELLBOOK, true, var10.slot),
                "Aimed item must start a real continuous cast"
            );
            var0.assertTrue(var4.isCasting() && var7.isNoGravity(), "Live item channel must suspend gravity");
            var2.setYRot(-30.0F);
            var2.setYHeadRot(-30.0F);
            ArcanaModes.request(var2, 4);
            var0.assertTrue(
                !var4.isCasting() && !var7.isNoGravity() && var7.getDeltaMovement().length() > 1.5,
                "Throw must end channel, restore gravity and launch held item"
            );
            var7.discard();
            var2.setYRot(0.0F);
            var2.setYHeadRot(0.0F);
            var4.setMana(600.0F);
            var4.getPlayerCooldowns().clearCooldowns();
            var8.setPos(var3.x, var3.y + 0.5, var3.z + 5.0);
            var8.setNoAi(true);
            var1.addFreshEntity(var8);
            var0.assertTrue(
                var5.attemptInitiateCast(ItemStack.EMPTY, 1, var1, var2, CastSource.SPELLBOOK, true, var10.slot),
                "Living target must start telekinetic channel"
            );
            var0.assertTrue(var4.getAdditionalCastData() instanceof TelekinesisData, "Living channel must use installed Iron Telekinesis cast data");
            var2.setYRot(-30.0F);
            var2.setYHeadRot(-30.0F);
            AdvancedMagic.liftTick(var2, 1, var4);
            var0.assertTrue(var8.getDeltaMovement().lengthSqr() > 0.0, "Installed Iron force must move the grabbed creature");
            ArcanaModes.request(var2, 4);
            var0.assertTrue(!var4.isCasting() && var8.getDeltaMovement().length() > 1.5, "Living target must throw and end channel");
            var2.getAttribute(AttributeRegistry.COOLDOWN_REDUCTION).setBaseValue(1.25);
            ArcanaCooldowns.begin(var2, var6, CastSource.SCROLL);
            CooldownInstance var11 = var4.getPlayerCooldowns().getSpellCooldowns().get(var6.getSpellId());
            var0.assertTrue(
                var11 != null && var11.getCooldownRemaining() > 0 && var11.getCooldownRemaining() <= var6.getSpellCooldown(),
                "Scroll breaker cooldown must exist in native Iron cooldown manager"
            );
            long var12 = ArcanaEvents.flags(var2).getLong("breakerReadyTick") - var2.server.overworld().getGameTime();
            var0.assertTrue(var12 == (long)var11.getCooldownRemaining(), "Saved enforcement and native Iron display must agree");
            var4.getPlayerCooldowns().clearCooldowns();
            ArcanaCooldowns.restore(var2);
            var0.assertTrue(
                (long)var4.getPlayerCooldowns().getSpellCooldowns().get(var6.getSpellId()).getCooldownRemaining() == var12,
                "Relog restoration must recover visible cooldown without resetting its deadline"
            );
            var0.succeed();
        } finally {
            AdvancedMagic.release(var2);
            var4.resetCastingState();
            var4.getPlayerCooldowns().clearCooldowns();
            var2.getInventory().clearContent();
            var7.discard();
            var8.discard();
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void expandedMagicContract(GameTestHelper var0) {
        ServerLevel var1 = var0.getLevel();
        FakePlayer var2 = FakePlayerFactory.get(var1, new GameProfile(UUID.randomUUID(), "arcana-test"));
        Vec3 var3 = Vec3.atCenterOf(var0.absolutePos(new BlockPos(0, 20, 0)));
        var2.setPos(var3.x, var3.y, var3.z);
        var2.setYRot(0.0F);
        var2.setXRot(0.0F);
        MagicData var4 = MagicData.getPlayerMagicData(var2);
        ArcanaSpell var5 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.HEAVY).get();
        ArcanaSpell var6 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.BARRAGE).get();
        ArcanaSpell var7 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.FLIGHT).get();

        try {
            var4.setMana(600.0F);
            var2.getAttribute(AttributeRegistry.MAX_MANA).setBaseValue(100.0);
            var0.assertTrue(!var5.ready(var2, 1, false), "Starter mana capacity must not unlock heavy blast");
            var2.getAttribute(AttributeRegistry.MAX_MANA).setBaseValue(600.0);
            var4.setMana(600.0F);
            var0.assertTrue(var5.ready(var2, 1, false), "Endgame capacity with enough mana must unlock heavy blast");
            var0.assertTrue(var5.getEffectiveCastTime(1, var2) >= ArcanaConfig.HEAVY_CHARGE.get() * 20, "Heavy charge must retain its minimum");
            var4.setMana(10.0F);
            var0.assertTrue(!var5.ready(var2, 1, false), "Heavy blast must reject insufficient current mana");
            var4.setMana(600.0F);
            var2.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            var0.assertTrue(!var7.ready(var2, 1, false), "Flight must reject no staff");
            var2.setItemSlot(EquipmentSlot.OFFHAND, StaffView.demo());
            var0.assertTrue(var7.ready(var2, 1, false), "Offhand staff must permit flight");
            ArcanaEvents.flags(var2).putBoolean("flight", true);
            ArcanaEvents.updateFlight(var2);
            var0.assertTrue(var2.getAbilities().mayfly, "Staff flight must grant ordinary flight permission");
            var2.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            ArcanaEvents.tick(new Post(var2));
            var0.assertTrue(!ArcanaEvents.flags(var2).getBoolean("flight") && !var2.getAbilities().mayfly, "Removing staff must end survival flight");
            ArcanaModes.request(var2, 2);
            var0.assertTrue(!var4.isCasting(), "Key requests without an equipped spell must be rejected");
            ItemStack var8 = new ItemStack(Items.STICK);
            ISpellContainer.createImbuedContainer(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.ZOLTRAAK).get(), 5, var8);
            var2.setItemSlot(EquipmentSlot.MAINHAND, var8);
            ArcanaEvents.flags(var2).remove("nextModeRequest");
            ArcanaModes.request(var2, 2);
            var0.assertTrue(
                var4.isCasting() && var4.getCastingSpellId().equals(var5.getSpellId()), "Mastered equipped Zoltraak must begin a real Iron heavy cast"
            );
            var5.onServerCastComplete(var1, 1, var2, var4, true);
            var4.setMana(100.0F);
            ArcanaModes.startBarrage(var2, 5);
            var4.setMana(150.0F);
            ArcanaModes.tick(var2);
            var0.assertTrue(var4.getMana() < 100.0F, "Regeneration must not replenish the active barrage budget");
            var4.setMana(0.01F);
            ArcanaModes.tick(var2);
            var0.assertTrue(!ArcanaEvents.flags(var2).getBoolean("barrage") && var4.getMana() == 0.0F, "Exhaustion must end barrage without negative mana");
            int var9 = 0;

            for (DeferredItem var11 : FrierenArcana.DEVICES) {
                BarrierDevice var12 = (BarrierDevice)var11.get();
                var0.assertTrue(var12.radius() > var9, "Device tiers must increase radius");
                var9 = var12.radius();
            }

            var0.assertTrue(var9 == ArcanaConfig.DEVICE_RADIUS.get(), "Top device must use configured grand radius");
            var0.succeed();
        } finally {
            ArcanaModes.stopBarrage(var2);
            ExpandedMagic.release(var2);
            ArcanaEvents.flags(var2).putBoolean("flight", false);
            ArcanaEvents.updateFlight(var2);
            var4.resetCastingState();
            var2.getInventory().clearContent();
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void combatAndSupportContract(GameTestHelper var0) {
        ServerLevel var1 = var0.getLevel();
        FakePlayer var2 = FakePlayerFactory.get(var1, new GameProfile(UUID.randomUUID(), "arcana-combat-test"));
        Vec3 var3 = Vec3.atCenterOf(var0.absolutePos(new BlockPos(0, 20, 0)));
        var2.setPos(var3.x, var3.y, var3.z);
        var2.setYRot(0.0F);
        var2.setXRot(0.0F);
        Cow var4 = EntityType.COW.create(var1);
        Cow var5 = EntityType.COW.create(var1);
        var4.setPos(var3.x, var3.y, var3.z + 5.0);
        var5.setPos(var3.x, var3.y, var3.z + 10.0);
        var4.setNoAi(true);
        var5.setNoAi(true);
        var4.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        var5.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        var4.setHealth(200.0F);
        var5.setHealth(200.0F);
        var1.addFreshEntity(var4);
        var1.addFreshEntity(var5);

        try {
            ArcanaSpell var6 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.HEAVY).get();
            ArcanaModes.fire(var2, var6, 1, var2.getLookAngle(), 30.0, 2.0F, 100.0F, 2, true);
            var0.assertTrue(var4.getHealth() < 200.0F && var5.getHealth() < 200.0F, "Wide heavy beam must damage multiple aligned living targets");
            var2.setXRot(10.0F);
            ArcanaSpell var7 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.BIND).get();
            ExpandedMagic.cast(var7, var2, 1);
            var0.assertTrue(ExpandedMagic.held(var4), "Sight binding must restrain the aimed living target");
            double var8 = var4.getX();
            var4.move(MoverType.SELF, new Vec3(1.0, 0.0, 0.0));
            var0.assertTrue(var4.getX() == var8, "Bound target must not move through ordinary movement");
            var2.setYRot(180.0F);
            ExpandedMagic.tick(var2);
            var0.assertTrue(!ExpandedMagic.held(var4), "Looking away must release sight binding");
            var2.setHealth(5.0F);
            ExpandedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.HEAL).get(), var2, 1);
            var0.assertTrue(var2.getHealth() > 5.0F, "Goddess healing must restore health");
            var2.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
            var2.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
            ExpandedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.CLEANSE).get(), var2, 1);
            var0.assertTrue(
                !var2.hasEffect(MobEffects.POISON) && var2.hasEffect(MobEffects.BLINDNESS), "Level I cleanse must remove poison but retain blindness"
            );
            ExpandedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.CLEANSE).get(), var2, 3);
            var0.assertTrue(!var2.hasEffect(MobEffects.BLINDNESS), "Level III cleanse must unlock blindness removal");
            var0.succeed();
        } finally {
            ExpandedMagic.release(var2);
            var4.discard();
            var5.discard();
            var2.removeAllEffects();
        }
    }

    @GameTest(
        template = "empty",
        timeoutTicks = 100
    )
    public static void spellGuideAndAdvancedMagicContract(GameTestHelper var0) {
        ServerLevel var1 = var0.getLevel();
        FakePlayer var2 = FakePlayerFactory.get(var1, new GameProfile(UUID.randomUUID(), "arcana-guide-test"));
        Vec3 var3 = Vec3.atCenterOf(var0.absolutePos(new BlockPos(0, 20, 0)));
        var2.setPos(var3.x, var3.y, var3.z);
        var2.setYRot(0.0F);
        var2.setXRot(0.0F);
        List var4 = SpellGuidePages.all();
        int var5 = FrierenArcana.SPELL_MAP.values().stream().mapToInt(var0x -> var0x.get().getMaxLevel() - var0x.get().getMinLevel() + 1).sum();
        var0.assertTrue(var4.size() == var5, "JEI data must contain a page for every spell level");
        HashSet var6 = new HashSet();

        for (SpellGuidePages.Page var8 : var4) {
            SpellData var9 = ISpellContainer.get(var8.scroll()).getSpellAtIndex(0);
            var0.assertTrue(var9.getSpell().equals(var8.spell()) && var9.getLevel() == var8.level(), "JEI guide scroll must encode its exact spell and level");
            var0.assertTrue(var6.add(var8.spell().getSpellId() + ":" + var8.level()), "JEI guide entries must be unique");
        }

        ItemEntity var13 = new ItemEntity(var1, var3.x, var3.y + 1.2, var3.z + 4.0, new ItemStack(Items.DIAMOND));
        var13.setNoGravity(false);
        var1.addFreshEntity(var13);

        try {
            ArcanaSpell var14 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.FIREWIND).get();
            ArcanaSpell var15 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PETALS).get();
            var2.getAttribute(AttributeRegistry.MAX_MANA).setBaseValue(600.0);
            MagicData.getPlayerMagicData(var2).setMana(600.0F);
            var0.assertTrue(!var14.ready(var2, 1, false), "Fire-Wind must require a preceding wind cast");
            AdvancedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.TORNADO).get(), var2, 1);
            var0.assertTrue(AdvancedMagic.hasWind(var2) && var14.ready(var2, 1, false), "Tornado must open the Fire-Wind combo window");
            AdvancedMagic.cast(var14, var2, 1);
            var0.assertTrue(!AdvancedMagic.hasWind(var2), "Fire-Wind must consume its setup window");
            var0.assertTrue(!var15.ready(var2, 1, false), "Steel Petals must require flowers");
            AdvancedMagic.markFlowers(var2);
            var0.assertTrue(var15.ready(var2, 1, false), "Cosmetic flower magic must unlock Steel Petals nearby");
            var0.assertTrue(AdvancedMagic.itemTarget(var2) == var13, "Levitation must resolve an aimed dropped item");
            AdvancedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.LIFT).get(), var2, 1);
            var0.assertTrue(var13.isNoGravity(), "Object levitation must suspend item gravity");
            AdvancedMagic.release(var2);
            var0.assertTrue(!var13.isNoGravity(), "Releasing personal magic must restore original item gravity");
            AdvancedMagic.cast(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.LIFT).get(), var2, 1);
            AdvancedMagic.recover(var13);
            var0.assertTrue(!var13.isNoGravity(), "Recovered item markers must prevent permanent floating after reload");
            var0.succeed();
        } finally {
            AdvancedMagic.release(var2);
            var13.discard();
        }
    }
}
