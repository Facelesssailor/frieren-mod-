package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TelekinesisData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AdvancedMagic {
    private static final Map<UUID, AdvancedMagic.Lift> LIFTS = new HashMap<>();
    private static final String MARKER = "frieren_arcana_lift";

    public static void markFlowers(ServerPlayer p) {
        CompoundTag f = ArcanaEvents.flags(p);
        f.putLong("flowersUntil", p.serverLevel().getGameTime() + 300L);
        f.putString("flowersDimension", p.serverLevel().dimension().location().toString());
        point(f, "flowers", ShipSpace.world(p));
    }

    public static boolean hasFlowers(ServerPlayer p) {
        CompoundTag f = ArcanaEvents.flags(p);
        Vec3 pos = ShipSpace.world(p);
        if (f.getLong("flowersUntil") > p.serverLevel().getGameTime()
            && f.getString("flowersDimension").equals(p.serverLevel().dimension().location().toString())
            && read(f, "flowers").distanceToSqr(pos) <= 25.0) {
            return true;
        } else {
            BlockPos origin = BlockPos.containing(pos);

            for (BlockPos at : BlockPos.betweenClosed(origin.offset(-4, -2, -4), origin.offset(4, 2, 4))) {
                if (p.level().getBlockState(at).is(BlockTags.FLOWERS) && !BarrierData.get(p.serverLevel()).crosses(pos, Vec3.atCenterOf(at), false)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean hasWind(ServerPlayer p) {
        CompoundTag f = ArcanaEvents.flags(p);
        return f.getLong("windUntil") > p.serverLevel().getGameTime()
            && f.getString("windDimension").equals(p.serverLevel().dimension().location().toString())
            && read(f, "wind").distanceToSqr(ShipSpace.world(p)) < 1024.0
            && !BarrierData.get(p.serverLevel()).crosses(ShipSpace.world(p), read(f, "wind"), false);
    }

    public static ItemEntity itemTarget(ServerPlayer p) {
        Vec3 from = ShipSpace.world(p.level(), p.getEyePosition());
        Vec3 dir = p.getLookAngle();
        Vec3 end = from.add(dir.scale(12.0));
        BlockHitResult solid = p.level().clip(new ClipContext(from, end, Block.COLLIDER, Fluid.NONE, p));
        double reach = solid.getType() == Type.MISS ? 12.0 : from.distanceTo(solid.getLocation());
        ItemEntity best = null;
        double nearest = reach;

        for (Entity e : p.serverLevel().getAllEntities()) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity)e;
                if (item.isAlive()) {
                    Vec3 r = ShipSpace.world(p.level(), item.getBoundingBox().getCenter()).subtract(from);
                    double along = r.dot(dir);
                    if (along >= 0.0
                        && along < nearest
                        && r.subtract(dir.scale(along)).lengthSqr() < 0.42250000000000004
                        && !BarrierData.get(p.serverLevel()).crosses(from, ShipSpace.world(item), false)) {
                        best = item;
                        nearest = along;
                    }
                }
            }
        }

        return best;
    }

    public static Entity liftTarget(ServerPlayer p, int level) {
        Vec3 from = ShipSpace.world(p.level(), p.getEyePosition());
        Vec3 d = p.getLookAngle();
        Vec3 to = from.add(d.scale((double)(12 + 2 * (level - 1))));
        BlockHitResult hit = p.level().clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, p));
        double reach = from.distanceTo(hit.getLocation());
        double nearest = reach;
        Entity best = null;

        for (Entity e : p.serverLevel().getAllEntities()) {
            if (e != p
                && e.isAlive()
                && (e instanceof ItemEntity || e instanceof LivingEntity)
                && !DamageSources.isFriendlyFireBetween(p, e)
                && !ExpandedMagic.held(e)) {
                Vec3 r = ShipSpace.world(p.level(), e.getBoundingBox().getCenter()).subtract(from);
                double along = r.dot(d);
                if (along >= 0.0
                    && along < nearest
                    && r.subtract(d.scale(along)).lengthSqr() < Math.pow(0.35 + (double)(e.getBbWidth() / 2.0F), 2.0)
                    && !BarrierData.get(p.serverLevel()).crosses(from, ShipSpace.world(e), true)) {
                    best = e;
                    nearest = along;
                }
            }
        }

        return best;
    }

    public static boolean prepareLift(ServerPlayer p, int level, MagicData magic) {
        Entity target = liftTarget(p, level);
        if (target == null) {
            return false;
        } else {
            if (target instanceof LivingEntity living) {
                magic.setAdditionalCastData(new TelekinesisData(p.distanceTo(living), living, 6));
            } else {
                startItemLift(p, (ItemEntity)target, level);
            }

            return true;
        }
    }

    private static void startItemLift(ServerPlayer p, ItemEntity item, int level) {
        releaseLift(p);
        boolean old = item.isNoGravity();
        CompoundTag marker = new CompoundTag();
        marker.putBoolean("oldGravity", old);
        item.getPersistentData().put("frieren_arcana_lift", marker);
        item.setNoGravity(true);
        item.setPickUpDelay(20);
        double distance = Math.max(2.0, Math.min((double)(12 + 2 * (level - 1)), ShipSpace.world(p).distanceTo(ShipSpace.world(item))));
        LIFTS.put(item.getUUID(), new AdvancedMagic.Lift(p.getUUID(), item, old, p.serverLevel().getGameTime() + 220L, distance));
    }

    public static void releaseLift(ServerPlayer p) {
        for (Entry<UUID, AdvancedMagic.Lift> entry : List.copyOf(LIFTS.entrySet())) {
            if (entry.getValue().owner.equals(p.getUUID())) {
                end(entry.getValue());
                LIFTS.remove(entry.getKey());
            }
        }
    }

    public static void throwLift(ServerPlayer p) {
        MagicData magic = MagicData.getPlayerMagicData(p);
        if (magic.isCasting() && magic.getCastingSpellId().equals(FrierenArcana.id("object_levitation").toString())) {
            Entity target = null;
            if (magic.getAdditionalCastData() instanceof TelekinesisData data) {
                target = data.getTarget(p.serverLevel());
            } else {
                for (AdvancedMagic.Lift lift : LIFTS.values()) {
                    if (lift.owner.equals(p.getUUID())) {
                        target = lift.item;
                        break;
                    }
                }
            }

            Utils.serverSideCancelCast(p);
            releaseLift(p);
            if (target != null && target.isAlive() && !BarrierData.get(p.serverLevel()).crosses(ShipSpace.world(p), ShipSpace.world(target), true)) {
                target.setDeltaMovement(p.getLookAngle().scale(1.6).add(0.0, 0.12, 0.0));
                target.hurtMarked = true;
            }
        }
    }

    public static void liftTick(ServerPlayer p, int level, MagicData magic) {
        if (magic.getAdditionalCastData() instanceof TelekinesisData data) {
            LivingEntity target = data.getTarget(p.serverLevel());
            if (target == null || !target.isAlive() || BarrierData.get(p.serverLevel()).crosses(ShipSpace.world(p), ShipSpace.world(target), true)) {
                Utils.serverSideCancelCast(p);
                return;
            }

            SpellRegistry.TELEKINESIS_SPELL.get().onServerCastTick(p.level(), level, p, magic);
        }
    }

    public static void recover(ItemEntity item) {
        if (item.getPersistentData().contains("frieren_arcana_lift")) {
            item.setNoGravity(item.getPersistentData().getCompound("frieren_arcana_lift").getBoolean("oldGravity"));
            item.getPersistentData().remove("frieren_arcana_lift");
        }
    }

    private static void end(AdvancedMagic.Lift lift) {
        lift.item.setNoGravity(lift.oldGravity);
        lift.item.getPersistentData().remove("frieren_arcana_lift");
    }

    public static void release(ServerPlayer p) {
        releaseLift(p);
        CompoundTag f = ArcanaEvents.flags(p);
        f.remove("windUntil");
        f.remove("flowersUntil");
    }

    public static void tick(ServerLevel level) {
        for (Entry<UUID, AdvancedMagic.Lift> entry : List.copyOf(LIFTS.entrySet())) {
            AdvancedMagic.Lift lift = entry.getValue();
            ItemEntity item = lift.item;
            if (item.level() == level) {
                ServerPlayer owner = level.getServer().getPlayerList().getPlayer(lift.owner);
                if (item.isAlive()
                    && level.getGameTime() < lift.expires
                    && owner != null
                    && owner.isAlive()
                    && owner.level() == level
                    && MagicData.getPlayerMagicData(owner).isCasting()
                    && !BarrierData.get(level).crosses(ShipSpace.world(owner), ShipSpace.world(item), false)) {
                    Vec3 desired = ShipSpace.local(item, ShipSpace.world(level, owner.getEyePosition()).add(owner.getLookAngle().scale(lift.distance)));
                    Vec3 from = owner.getEyePosition();
                    Vec3 clipped = BarrierHooks.clip(level, from, ShipSpace.local(item, ShipSpace.world(level, desired)));
                    BlockHitResult obstruction = level.clip(new ClipContext(from, clipped, Block.COLLIDER, Fluid.NONE, owner));
                    if (obstruction.getType() != Type.MISS) {
                        desired = obstruction.getLocation().lerp(from, 0.08);
                    }

                    Vec3 velocity = item.getDeltaMovement().lerp(desired.subtract(item.position()).scale(0.32), 0.35);
                    if (velocity.length() > 0.8) {
                        velocity = velocity.normalize().scale(0.8);
                    }

                    item.setDeltaMovement(velocity);
                    item.hurtMarked = true;
                    if (level.getGameTime() % 10L == 0L) {
                        ArcanaNetwork.effect(level, ShipSpace.world(item), 20, 1);
                    }
                } else {
                    end(lift);
                    LIFTS.remove(entry.getKey());
                }
            }
        }
    }

    private static void point(CompoundTag f, String key, Vec3 pos) {
        f.putDouble(key + "X", pos.x);
        f.putDouble(key + "Y", pos.y);
        f.putDouble(key + "Z", pos.z);
    }

    private static Vec3 read(CompoundTag f, String key) {
        return new Vec3(f.getDouble(key + "X"), f.getDouble(key + "Y"), f.getDouble(key + "Z"));
    }

    private static Vec3 aim(ServerPlayer p) {
        LivingEntity target = ExpandedMagic.target(p, 24.0);
        if (target != null) {
            return ShipSpace.world(p.level(), target.getBoundingBox().getCenter());
        } else {
            Vec3 start = ShipSpace.world(p.level(), p.getEyePosition());
            Vec3 end = start.add(p.getLookAngle().scale(12.0));
            BlockHitResult solid = p.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, p));
            if (solid.getType() != Type.MISS) {
                end = solid.getLocation();
            }

            BarrierData.Field field = BarrierData.get(p.serverLevel()).firstBoundary(start, end, true);
            if (field != null) {
                Vec3 r = start.subtract(field.center);
                Vec3 d = end.subtract(start);
                double t = BarrierGeometry.firstHit(r.x, r.y, r.z, d.x, d.y, d.z, (double)field.radius);
                end = start.add(d.scale(Math.max(0.0, t - 0.01)));
            }

            return end;
        }
    }

    private static List<LivingEntity> victims(ServerPlayer p, Vec3 center, double radius) {
        List<LivingEntity> result = new ArrayList<>();
        Vec3 from = ShipSpace.world(p);

        for (Entity e : p.serverLevel().getAllEntities()) {
            if (e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)e;
                if (e != p
                    && living.isAlive()
                    && !DamageSources.isFriendlyFireBetween(p, e)
                    && ShipSpace.world(e).distanceToSqr(center) <= radius * radius
                    && p.hasLineOfSight(e)
                    && !BarrierData.get(p.serverLevel()).crosses(from, ShipSpace.world(e), true)) {
                    result.add(living);
                }
            }
        }

        return result;
    }

    public static void cast(ArcanaSpell spell, ServerPlayer p, int level) {
        ServerLevel world = p.serverLevel();
        float power = spell.getSpellPower(level, p);
        Vec3 center = aim(p);
        switch (spell.kind) {
            case INFERNO:
                for (LivingEntity e : victims(p, center, (double)(2 + level))) {
                    DamageSources.applyDamage(e, power * 2.0F, SpellDamageSource.source(p, spell));
                    e.igniteForSeconds((float)(3 + level));
                }

                ArcanaNetwork.effect(world, center, 13, level);
                world.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 60, (double)(1 + level), 0.8, (double)(1 + level), 0.04);
                break;
            case THUNDER:
                for (LivingEntity e : victims(p, center, 1.5 + (double)level)) {
                    DamageSources.applyDamage(e, power * 2.2F, SpellDamageSource.source(p, spell));
                    ArcanaNetwork.magicBeam(world, ShipSpace.world(world, p.getEyePosition()), ShipSpace.world(world, e.getBoundingBox().getCenter()), 15);
                }

                ArcanaNetwork.effect(world, center, 15, level);
                break;
            case TORNADO:
                for (LivingEntity e : victims(p, center, (double)(2 + level))) {
                    DamageSources.applyDamage(e, power * 0.8F, SpellDamageSource.source(p, spell));
                    e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 + 10 * level, 0));
                    e.push(0.0, 0.2, 0.0);
                    e.hurtMarked = true;
                }

                CompoundTag f = ArcanaEvents.flags(p);
                f.putLong("windUntil", world.getGameTime() + 80L);
                f.putString("windDimension", world.dimension().location().toString());
                point(f, "wind", center);
                ArcanaNetwork.effect(world, center, 14, level);
                break;
            case FIREWIND:
                center = read(ArcanaEvents.flags(p), "wind");
                ArcanaEvents.flags(p).remove("windUntil");

                for (LivingEntity e : victims(p, center, (double)(3 + level))) {
                    DamageSources.applyDamage(e, power * 1.8F, SpellDamageSource.source(p, spell));
                    e.igniteForSeconds((float)(3 + level));
                }

                ArcanaNetwork.effect(world, center, 21, level);
                break;
            case JUDGMENT:
                ArcanaModes.fire(p, spell, level, p.getLookAngle(), 56.0, 0.65F, power * 1.8F, 16, true);
                break;
            case PETALS:
                ArcanaModes.fire(p, spell, level, p.getLookAngle(), 24.0, 0.6F, power * 1.3F, 17, true);
                ArcanaNetwork.effect(world, center, 17, level);
                break;
            case STONE:
                Vec3 side = p.getLookAngle().cross(new Vec3(0.0, 1.0, 0.0)).normalize();

                for (int i = 0; i < 2 + level; i++) {
                    ArcanaModes.fire(
                        p,
                        spell,
                        level,
                        p.getLookAngle().add(side.scale(((double)i - (double)(1 + level) / 2.0) * 0.025)).normalize(),
                        32.0,
                        0.15F,
                        power * 0.65F,
                        18,
                        false
                    );
                }
                break;
            case LIFT:
                if (!MagicData.getPlayerMagicData(p).isCasting()) {
                    ItemEntity item = itemTarget(p);
                    if (item != null) {
                        startItemLift(p, item, level);
                    }
                }
                break;
            case SPEARS:
                Vec3 side = p.getLookAngle().cross(new Vec3(0.0, 1.0, 0.0)).normalize();

                for (int i = -1; i <= 1; i++) {
                    ArcanaModes.fire(p, spell, level, p.getLookAngle().add(side.scale((double)i * 0.06)).normalize(), 32.0, 0.1F, power * 0.9F, 19, false);
                }
        }
    }

    private static record Lift(UUID owner, ItemEntity item, boolean oldGravity, long expires, double distance) {
    }
}
