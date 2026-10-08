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
import java.util.List;
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

    public static boolean hasStaff(LivingEntity player) {
        return isStaff(player.getMainHandItem()) || isStaff(player.getOffhandItem());
    }

    public static boolean isStaff(ItemStack stack) {
        return stack.getItem() instanceof StaffItem || stack.is(FLIGHT_STAVES);
    }

    public static void request(ServerPlayer p, int action) {
        if (action >= 0 && action <= 4) {
            if (action == 4) {
                AdvancedMagic.throwLift(p);
            } else if (action == 3) {
                stopBarrage(p);
                if (MagicData.getPlayerMagicData(p).isCasting()
                    && MagicData.getPlayerMagicData(p).getCastingSpellId().equals(FrierenArcana.id("zoltraak_heavy").toString())) {
                    Utils.serverSideCancelCast(p);
                }
            } else {
                CompoundTag flags = ArcanaEvents.flags(p);
                long now = p.server.overworld().getGameTime();
                if (now >= flags.getLong("nextModeRequest")) {
                    flags.putLong("nextModeRequest", now + 5L);
                    if (p.isAlive() && !p.isSpectator()) {
                        if (action == 1 && flags.getBoolean("barrage")) {
                            stopBarrage(p);
                        } else {
                            ArcanaSpell.Kind kind = action == 0 ? ArcanaSpell.Kind.ZOLTRAAK : (action == 1 ? ArcanaSpell.Kind.BARRAGE : ArcanaSpell.Kind.HEAVY);
                            SelectionOption choice = equipped(p, kind);
                            if (choice == null) {
                                p.displayClientMessage(Component.translatable("message.frieren_arcana.error.book"), true);
                            } else {
                                int level = choice.spellData.getLevel();
                                if (kind == ArcanaSpell.Kind.HEAVY
                                    && choice.spellData.getSpell().equals(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.ZOLTRAAK).get())
                                    && level < 5) {
                                    p.displayClientMessage(Component.translatable("message.frieren_arcana.error.mastery"), true);
                                } else {
                                    ArcanaSpell spell = FrierenArcana.SPELL_MAP.get(kind).get();
                                    if (kind == ArcanaSpell.Kind.HEAVY) {
                                        level = 1;
                                    }

                                    spell.attemptInitiateCast(
                                        ItemStack.EMPTY, Math.min(level, spell.getMaxLevel()), p.level(), p, CastSource.SPELLBOOK, true, choice.slot
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static SelectionOption equipped(ServerPlayer p, ArcanaSpell.Kind kind) {
        SelectionOption best = null;

        for (SelectionOption option : new SpellSelectionManager(p).getAllSpells()) {
            AbstractSpell spell = option.spellData.getSpell();
            boolean matches = spell.equals(FrierenArcana.SPELL_MAP.get(kind).get())
                || (kind == ArcanaSpell.Kind.BARRAGE || kind == ArcanaSpell.Kind.HEAVY)
                    && spell.equals(FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.ZOLTRAAK).get());
            if (matches && (best == null || option.spellData.getLevel() > best.spellData.getLevel())) {
                best = option;
            }
        }

        return best;
    }

    public static void startBarrage(ServerPlayer p, int level) {
        CompoundTag f = ArcanaEvents.flags(p);
        f.putBoolean("barrage", true);
        f.putInt("barrageLevel", level);
        f.putFloat("barrageMana", MagicData.getPlayerMagicData(p).getMana());
        p.displayClientMessage(Component.translatable("message.frieren_arcana.barrage_on"), true);
    }

    public static void stopBarrage(ServerPlayer p) {
        ArcanaEvents.flags(p).putBoolean("barrage", false);
    }

    public static void tick(ServerPlayer p) {
        CompoundTag f = ArcanaEvents.flags(p);
        if (f.getBoolean("barrage")) {
            MagicData magic = MagicData.getPlayerMagicData(p);
            if (p.isAlive() && !p.isSpectator() && !magic.isCasting() && equipped(p, ArcanaSpell.Kind.BARRAGE) != null) {
                float drain = ArcanaConfig.BARRAGE_DRAIN.get().floatValue() / 20.0F;
                float before = Math.min(magic.getMana(), f.getFloat("barrageMana"));
                if (before < drain) {
                    magic.setMana(0.0F);
                    stopBarrage(p);
                    ArcanaEvents.syncMana(p);
                    p.displayClientMessage(Component.translatable("message.frieren_arcana.barrage_empty"), true);
                } else {
                    magic.setMana(before - drain);
                    f.putFloat("barrageMana", magic.getMana());
                    if (p.tickCount % 3 == 0) {
                        ArcanaSpell spell = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.BARRAGE).get();
                        int level = Math.min(
                            Math.min(f.getInt("barrageLevel"), spell.getMaxLevel()), equipped(p, ArcanaSpell.Kind.BARRAGE).spellData.getLevel()
                        );
                        double yaw = (p.getRandom().nextDouble() - 0.5) * 0.025;
                        double pitch = (p.getRandom().nextDouble() - 0.5) * 0.025;
                        Vec3 d = p.getLookAngle().add(yaw, pitch, -yaw).normalize();
                        BarrageFern.volley(p, spell, level, d, 64.0, 0.12F, spell.getSpellPower(level, p) * 0.55F, 1, false);
                        ArcanaEvents.syncMana(p);
                    }
                }
            } else {
                stopBarrage(p);
            }
        }
    }

    public static Vec3 fire(
        ServerPlayer p, ArcanaSpell spell, int level, Vec3 direction, double range, float radius, float damage, int visual, boolean multiple
    ) {
        Vec3 start = ShipSpace.world(p.level(), p.getEyePosition());
        Vec3 end = start.add(direction.scale(range));
        BlockHitResult hit = p.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, p));
        if (hit.getType() == Type.BLOCK) {
            end = hit.getLocation();
        }

        BarrierData.Field field = BarrierData.get(p.serverLevel()).firstBoundary(start, end, true);
        if (field != null) {
            Vec3 r = start.subtract(field.center);
            Vec3 d = end.subtract(start);
            double t = BarrierGeometry.firstHit(r.x, r.y, r.z, d.x, d.y, d.z, (double)field.radius);
            end = start.add(d.scale(t));
        }

        double reach = start.distanceTo(end);
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity e : p.serverLevel().getAllEntities()) {
            if (e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)e;
                if (e != p && living.isAlive() && !DamageSources.isFriendlyFireBetween(p, e)) {
                    Vec3 c = ShipSpace.world(p.level(), living.getBoundingBox().getCenter());
                    Vec3 rel = c.subtract(start);
                    double along = rel.dot(direction);
                    double width = (double)radius + Math.max(0.45, (double)living.getBbWidth() / 2.0 + 0.15);
                    if (along >= 0.0
                        && along < reach
                        && rel.subtract(direction.scale(along)).lengthSqr() <= width * width
                        && !BarrierData.get(p.serverLevel()).crosses(start, c, true)) {
                        targets.add(living);
                    }
                }
            }
        }

        targets.sort(Comparator.comparingDouble(ex -> ShipSpace.world(p.level(), ex.getBoundingBox().getCenter()).distanceToSqr(start)));
        if (!multiple && !targets.isEmpty()) {
            LivingEntity target = targets.getFirst();
            end = start.add(direction.scale(ShipSpace.world(p.level(), target.getBoundingBox().getCenter()).subtract(start).dot(direction)));
            DamageSources.applyDamage(target, damage, SpellDamageSource.source(p, spell));
        } else {
            for (LivingEntity target : targets) {
                DamageSources.applyDamage(target, damage, SpellDamageSource.source(p, spell));
            }
        }

        BarrageFern.beam(p.serverLevel(), start, end, visual);
        return end;
    }
}
