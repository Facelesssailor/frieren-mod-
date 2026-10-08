package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager.SelectionOption;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ArcanaModes {
    public static final TagKey<Item> FLIGHT_STAVES = TagKey.create(Registries.ITEM, FrierenArcana.id("flight_staves"));

    public static boolean hasStaff(LivingEntity var0) {
        return isStaff(var0.getMainHandItem()) || isStaff(var0.getOffhandItem());
    }

    public static boolean isStaff(ItemStack var0) {
        return var0.getItem() instanceof StaffItem || var0.is(FLIGHT_STAVES);
    }

    public static void request(ServerPlayer var0, int var1) {
        if (var1 >= 0 && var1 <= 4) {
            if (var1 == 4) {
                AdvancedMagic.throwLift(var0);
            } else if (var1 == 3) {
                stopBarrage(var0);
                if (MagicData.getPlayerMagicData(var0).isCasting()
                    && MagicData.getPlayerMagicData(var0).getCastingSpellId().equals(FrierenArcana.id("zoltraak_heavy").toString())) {
                    Utils.serverSideCancelCast(var0);
                }
            } else {
                CompoundTag var2 = ArcanaEvents.flags(var0);
                long var3 = var0.server.overworld().getGameTime();
                if (var3 >= var2.getLong("nextModeRequest")) {
                    var2.putLong("nextModeRequest", var3 + 5L);
                    if (var0.isAlive() && !var0.isSpectator()) {
                        if (var1 == 1 && var2.getBoolean("barrage")) {
                            stopBarrage(var0);
                        } else {
                            ArcanaSpell.Kind var5 = var1 == 0 ? ArcanaSpell.Kind.ZOLTRAAK : (var1 == 1 ? ArcanaSpell.Kind.BARRAGE : ArcanaSpell.Kind.HEAVY);
                            SelectionOption var6 = equipped(var0, var5);
                            if (var6 == null) {
                                var0.displayClientMessage(Component.translatable("message.frieren_arcana.error.book"), true);
                            } else {
                                int var7 = var6.spellData.getLevel();
                                if (var5 == ArcanaSpell.Kind.HEAVY
                                    && var6.spellData.getSpell().equals(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.ZOLTRAAK).get())
                                    && var7 < 5) {
                                    var0.displayClientMessage(Component.translatable("message.frieren_arcana.error.mastery"), true);
                                } else {
                                    ArcanaSpell var8 = FrierenArcana.SPELL_MAP.get(var5).get();
                                    if (var5 == ArcanaSpell.Kind.HEAVY) {
                                        var7 = 1;
                                    }

                                    var8.attemptInitiateCast(
                                        ItemStack.EMPTY, Math.min(var7, var8.getMaxLevel()), var0.level(), var0, CastSource.SPELLBOOK, true, var6.slot
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static SelectionOption equipped(ServerPlayer var0, ArcanaSpell.Kind var1) {
        SelectionOption var2 = null;

        for (SelectionOption var4 : new SpellSelectionManager(var0).getAllSpells()) {
            AbstractSpell var5 = var4.spellData.getSpell();
            boolean var6 = var5.equals(FrierenArcana.SPELL_MAP.get(var1).get())
                || (var1 == ArcanaSpell.Kind.BARRAGE || var1 == ArcanaSpell.Kind.HEAVY)
                    && var5.equals(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.ZOLTRAAK).get());
            if (var6 && (var2 == null || var4.spellData.getLevel() > var2.spellData.getLevel())) {
                var2 = var4;
            }
        }

        return var2;
    }

    public static void startBarrage(ServerPlayer var0, int var1) {
        CompoundTag var2 = ArcanaEvents.flags(var0);
        var2.putBoolean("barrage", true);
        var2.putInt("barrageLevel", var1);
        var2.putFloat("barrageMana", MagicData.getPlayerMagicData(var0).getMana());
        var0.displayClientMessage(Component.translatable("message.frieren_arcana.barrage_on"), true);
    }

    public static void stopBarrage(ServerPlayer var0) {
        ArcanaEvents.flags(var0).putBoolean("barrage", false);
    }

    public static void tick(ServerPlayer var0) {
        CompoundTag var1 = ArcanaEvents.flags(var0);
        if (var1.getBoolean("barrage")) {
            MagicData var2 = MagicData.getPlayerMagicData(var0);
            if (var0.isAlive() && !var0.isSpectator() && !var2.isCasting() && equipped(var0, ArcanaSpell.Kind.BARRAGE) != null) {
                float var3 = ArcanaConfig.BARRAGE_DRAIN.get().floatValue() / 20.0F;
                float var4 = Math.min(var2.getMana(), var1.getFloat("barrageMana"));
                if (var4 < var3) {
                    var2.setMana(0.0F);
                    stopBarrage(var0);
                    ArcanaEvents.syncMana(var0);
                    var0.displayClientMessage(Component.translatable("message.frieren_arcana.barrage_empty"), true);
                } else {
                    var2.setMana(var4 - var3);
                    var1.putFloat("barrageMana", var2.getMana());
                    if (var0.tickCount % 3 == 0) {
                        ArcanaSpell var5 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.BARRAGE).get();
                        int var6 = Math.min(
                            Math.min(var1.getInt("barrageLevel"), var5.getMaxLevel()), equipped(var0, ArcanaSpell.Kind.BARRAGE).spellData.getLevel()
                        );
                        double var7 = (var0.getRandom().nextDouble() - 0.5) * 0.025;
                        double var9 = (var0.getRandom().nextDouble() - 0.5) * 0.025;
                        Vec3 var11 = var0.getLookAngle().add(var7, var9, -var7).normalize();
                        BarrageFern.volley(var0, var5, var6, var11, 64.0, 0.12F, var5.getSpellPower(var6, var0) * 0.55F, 1, false);
                        ArcanaEvents.syncMana(var0);
                    }
                }
            } else {
                stopBarrage(var0);
            }
        }
    }

    public static Vec3 fire(ServerPlayer var0, ArcanaSpell var1, int var2, Vec3 var3, double var4, float var6, float var7, int var8, boolean var9) {
        Vec3 var10 = ShipSpace.world(var0.level(), var0.getEyePosition());
        Vec3 var11 = var10.add(var3.scale(var4));
        BlockHitResult var12 = var0.level().clip(new ClipContext(var10, var11, Block.COLLIDER, Fluid.NONE, var0));
        if (var12.getType() == Type.BLOCK) {
            var11 = var12.getLocation();
        }

        BarrierData.Field var13 = BarrierData.get(var0.serverLevel()).firstBoundary(var10, var11, true);
        if (var13 != null) {
            Vec3 var14 = var10.subtract(var13.center);
            Vec3 var15 = var11.subtract(var10);
            double var16 = BarrierGeometry.firstHit(var14.x, var14.y, var14.z, var15.x, var15.y, var15.z, (double)var13.radius);
            var11 = var10.add(var15.scale(var16));
        }

        double var26 = var10.distanceTo(var11);
        ArrayList var27 = new ArrayList();

        for (Entity var18 : var0.serverLevel().getAllEntities()) {
            if (var18 instanceof LivingEntity) {
                LivingEntity var19 = (LivingEntity)var18;
                if (var18 != var0 && var19.isAlive() && !DamageSources.isFriendlyFireBetween(var0, var18)) {
                    Vec3 var20 = ShipSpace.world(var0.level(), var19.getBoundingBox().getCenter());
                    Vec3 var21 = var20.subtract(var10);
                    double var22 = var21.dot(var3);
                    double var24 = (double)var6 + Math.max(0.45, (double)var19.getBbWidth() / 2.0 + 0.15);
                    if (var22 >= 0.0
                        && var22 < var26
                        && var21.subtract(var3.scale(var22)).lengthSqr() <= var24 * var24
                        && !BarrierData.get(var0.serverLevel()).crosses(var10, var20, true)) {
                        var27.add(var19);
                    }
                }
            }
        }

        var27.sort(Comparator.comparingDouble(var2x -> ShipSpace.world(var0.level(), var2x.getBoundingBox().getCenter()).distanceToSqr(var10)));
        if (!var9 && !var27.isEmpty()) {
            LivingEntity var29 = (LivingEntity)var27.getFirst();
            var11 = var10.add(var3.scale(ShipSpace.world(var0.level(), var29.getBoundingBox().getCenter()).subtract(var10).dot(var3)));
            DamageSources.applyDamage(var29, var7, SpellDamageSource.source(var0, var1));
        } else {
            for (LivingEntity var30 : var27) {
                DamageSources.applyDamage(var30, var7, SpellDamageSource.source(var0, var1));
            }
        }

        BarrageFern.beam(var0.serverLevel(), var10, var11, var8);
        return var11;
    }
}
