package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ExpandedMagic {
    private static final Map<UUID, ExpandedMagic.Hold> HOLDS = new HashMap<>();

    public static boolean held(Entity e) {
        ExpandedMagic.Hold h = HOLDS.get(e.getUUID());
        if (h == null) {
            return false;
        } else if (e.level() == h.level && e.isAlive() && h.level.getGameTime() < h.expires) {
            return true;
        } else {
            HOLDS.remove(e.getUUID());
            return false;
        }
    }

    public static boolean blocksPosition(Entity e, Vec3 destination) {
        return held(e) && destination.distanceToSqr(e.position()) > 1.0E-4;
    }

    private static void releaseHolds(ServerPlayer p) {
        HOLDS.entrySet().removeIf(e -> e.getValue().caster.equals(p.getUUID()) || e.getKey().equals(p.getUUID()));
    }

    public static void release(ServerPlayer p) {
        AdvancedMagic.release(p);
        releaseHolds(p);
    }

    public static void tick(ServerPlayer p) {
        for (Entry<UUID, ExpandedMagic.Hold> entry : List.copyOf(HOLDS.entrySet())) {
            ExpandedMagic.Hold h = entry.getValue();
            if (h.caster.equals(p.getUUID())) {
                Entity target = h.level.getEntity(entry.getKey());
                if (target != null
                    && target.isAlive()
                    && p.level() == h.level
                    && p.isAlive()
                    && h.level.getGameTime() < h.expires
                    && (!h.sightRequired || inSight(p, target))) {
                    target.setDeltaMovement(Vec3.ZERO);
                    target.fallDistance = 0.0F;
                    target.hurtMarked = true;
                    if (p.tickCount % 10 == 0) {
                        h.level
                            .sendParticles(
                                h.sightRequired ? ParticleTypes.END_ROD : ParticleTypes.WAX_ON,
                                target.getX(),
                                target.getY() + 1.0,
                                target.getZ(),
                                8,
                                0.4,
                                0.7,
                                0.4,
                                0.02
                            );
                    }
                } else {
                    HOLDS.remove(entry.getKey());
                }
            }
        }
    }

    private static boolean inSight(ServerPlayer p, Entity e) {
        Vec3 rel = ShipSpace.world(p.level(), e.getBoundingBox().getCenter()).subtract(ShipSpace.world(p.level(), p.getEyePosition()));
        return rel.lengthSqr() <= 576.0
            && rel.normalize().dot(p.getLookAngle()) > 0.96
            && p.hasLineOfSight(e)
            && !BarrierData.get(p.serverLevel()).crosses(ShipSpace.world(p), ShipSpace.world(e), true);
    }

    public static boolean nearWater(ServerPlayer p) {
        BlockPos origin = BlockPos.containing(ShipSpace.world(p));

        for (BlockPos at : BlockPos.betweenClosed(origin.offset(-5, -3, -5), origin.offset(5, 3, 5))) {
            if (p.level().getFluidState(at).is(FluidTags.WATER) && !BarrierData.get(p.serverLevel()).crosses(ShipSpace.world(p), Vec3.atCenterOf(at), false)) {
                return true;
            }
        }

        return false;
    }

    public static LivingEntity target(ServerPlayer p, double reach) {
        Vec3 start = ShipSpace.world(p.level(), p.getEyePosition());
        Vec3 dir = p.getLookAngle();
        Vec3 end = start.add(dir.scale(reach));
        BlockHitResult solid = p.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, p));
        if (solid.getType() != Type.MISS) {
            reach = start.distanceTo(solid.getLocation());
        }

        LivingEntity best = null;
        double closest = reach;

        for (Entity e : p.serverLevel().getAllEntities()) {
            if (e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)e;
                if (e != p && living.isAlive() && !DamageSources.isFriendlyFireBetween(p, e)) {
                    Vec3 r = ShipSpace.world(p.level(), living.getBoundingBox().getCenter()).subtract(start);
                    double along = r.dot(dir);
                    if (along >= 0.0
                        && along < closest
                        && r.subtract(dir.scale(along)).lengthSqr() < Math.pow(0.6 + (double)(living.getBbWidth() / 2.0F), 2.0)
                        && !BarrierData.get(p.serverLevel()).crosses(start, ShipSpace.world(living), true)) {
                        best = living;
                        closest = along;
                    }
                }
            }
        }

        return best;
    }

    public static void cast(ArcanaSpell spell, ServerPlayer p, int level) {
        ServerLevel world = p.serverLevel();
        float power = spell.getSpellPower(level, p);
        Vec3 pos = ShipSpace.world(p);
        switch (spell.kind) {
            case ICE:
                Vec3 side = p.getLookAngle().cross(new Vec3(0.0, 1.0, 0.0)).normalize();

                for (int i = 0; i < 2 + level; i++) {
                    Vec3 d = p.getLookAngle().add(side.scale(((double)i - (double)(1 + level) / 2.0) * 0.022)).normalize();
                    ArcanaModes.fire(p, spell, level, d, 40.0, 0.1F, power * 0.6F, 4, false);
                }

                LivingEntity tx = target(p, 40.0);
                if (tx != null) {
                    tx.setTicksFrozen(Math.max(140, tx.getTicksFrozen()));
                    tx.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + 20 * level, 1));
                }
                break;
            case WATER:
                Vec3 end = ArcanaModes.fire(p, spell, level, p.getLookAngle(), 40.0, 0.8F, power * 1.4F, 5, true);
                ArcanaNetwork.effect(world, end, 5, level);
                break;
            case EARTH:
                Vec3 center = pos.add(p.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(3.0));
                ArcanaNetwork.effect(world, center, 6, level);

                for (Entity e : world.getAllEntities()) {
                    if (e instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity)e;
                        if (e != p
                            && !DamageSources.isFriendlyFireBetween(p, e)
                            && ShipSpace.world(e).distanceToSqr(center) < Math.pow((double)(3 + level), 2.0)
                            && !BarrierData.get(world).crosses(pos, ShipSpace.world(e), true)
                            && p.hasLineOfSight(e)) {
                            DamageSources.applyDamage(living, power * 1.3F, SpellDamageSource.source(p, spell));
                            living.push(0.0, 0.45 + 0.08 * (double)level, 0.0);
                            living.hurtMarked = true;
                        }
                    }
                }
                break;
            case SPEED:
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80 + 40 * level, 1 + level));
                ArcanaNetwork.effect(world, pos, 7, level);
                break;
            case BIND:
            case GOLD:
                LivingEntity t = target(p, 24.0);
                if (t == null) {
                    p.displayClientMessage(Component.translatable("message.frieren_arcana.error.living_target"), true);
                    return;
                }

                if (spell.kind == ArcanaSpell.Kind.BIND) {
                    releaseHolds(p);
                }

                HOLDS.put(
                    t.getUUID(),
                    new ExpandedMagic.Hold(
                        p.getUUID(),
                        world,
                        t.position(),
                        world.getGameTime() + (long)(spell.kind == ArcanaSpell.Kind.GOLD ? 200 : 80 + 40 * level),
                        spell.kind == ArcanaSpell.Kind.BIND
                    )
                );
                if (t instanceof ServerPlayer victim && MagicData.getPlayerMagicData(victim).isCasting()) {
                    Utils.serverSideCancelCast(victim);
                }

                ArcanaNetwork.effect(world, ShipSpace.world(t), spell.kind == ArcanaSpell.Kind.GOLD ? 9 : 8, level);
                break;
            case CUT:
                ArcanaModes.fire(p, spell, level, p.getLookAngle(), 5.0, 0.8F, power * 2.4F, 10, true);
                break;
            case FLOWERS:
                AdvancedMagic.markFlowers(p);
                ArcanaNetwork.effect(world, pos, 11, level);
                world.sendParticles(
                    ParticleTypes.CHERRY_LEAVES, p.getX(), p.getY() + 0.3, p.getZ(), 60 * level, (double)(2 + level), 0.3, (double)(2 + level), 0.015
                );
                break;
            case HEAL:
                p.heal((float)(4 + 4 * level));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, level - 1));
                ArcanaNetwork.effect(world, pos, 12, level);
                break;
            case CLEANSE:
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
                if (level >= 2) {
                    p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                }

                if (level >= 3) {
                    p.removeEffect(MobEffects.BLINDNESS);
                }

                ArcanaNetwork.effect(world, pos, 12, level);
        }
    }

    private static record Hold(UUID caster, ServerLevel level, Vec3 anchor, long expires, boolean sightRequired) {
    }
}
