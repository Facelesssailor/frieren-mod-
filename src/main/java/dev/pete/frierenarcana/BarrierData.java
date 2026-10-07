package dev.pete.frierenarcana;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.minecraft.world.phys.Vec3;

public final class BarrierData extends SavedData {
    private final Map<UUID, BarrierData.Field> fields = new LinkedHashMap<>();

    public static BarrierData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(BarrierData::new, BarrierData::load, null), "frieren_arcana_barriers");
    }

    public Collection<BarrierData.Field> fields() {
        return Collections.unmodifiableCollection(this.fields.values());
    }

    public boolean protects(BlockPos pos) {
        Vec3 p = Vec3.atCenterOf(pos);
        return this.fields.values().stream().anyMatch(f -> !f.defensive && Math.abs(p.distanceTo(f.center) - (double)f.radius) < 1.0);
    }

    public BarrierData.Field owned(UUID owner, boolean defensive) {
        return this.fields.values().stream().filter(f -> f.owner.equals(owner) && f.defensive == defensive).findFirst().orElse(null);
    }

    public static boolean owns(ServerPlayer player, boolean defensive) {
        for (ServerLevel level : player.server.getAllLevels()) {
            if (get(level).owned(player.getUUID(), defensive) != null) {
                return true;
            }
        }

        return false;
    }

    public static int releaseOwned(ServerPlayer player, Boolean defensive) {
        int count = 0;

        for (ServerLevel level : player.server.getAllLevels()) {
            BarrierData data = get(level);

            for (BarrierData.Field field : List.copyOf(data.fields.values())) {
                if (field.owner.equals(player.getUUID()) && (defensive == null || field.defensive == defensive)) {
                    data.remove(level, field.id, false);
                    count++;
                }
            }
        }

        player.displayClientMessage(Component.translatable("message.frieren_arcana.released", count), true);
        return count;
    }

    public String validate(ServerLevel level, BlockPos center, int radius, boolean defensive) {
        if (this.fields.size() >= ArcanaConfig.MAX_FIELDS.get()) {
            return "limit";
        } else if (center.getY() - radius >= level.getMinBuildHeight() && center.getY() + radius < level.getMaxBuildHeight()) {
            if (level.getWorldBorder().isWithinBounds(center.offset(radius, 0, radius))
                && level.getWorldBorder().isWithinBounds(center.offset(-radius, 0, -radius))) {
                Vec3 c = Vec3.atCenterOf(center);

                for (BarrierData.Field field : this.fields.values()) {
                    double distance = field.center.distanceTo(c);
                    boolean separate = distance >= (double)(field.radius + radius + 2);
                    boolean nested = distance + (double)Math.min(field.radius, radius) + 2.0 <= (double)Math.max(field.radius, radius);
                    if (!separate && !nested) {
                        return "overlap";
                    }
                }

                return null;
            } else {
                return "border";
            }
        } else {
            return "height";
        }
    }

    public boolean create(ServerLevel level, ServerPlayer owner, int radius, boolean defensive) {
        BlockPos center = BlockPos.containing(ShipSpace.world(owner));
        String error = this.validate(level, center, radius, defensive);
        if (error != null) {
            owner.displayClientMessage(Component.translatable("message.frieren_arcana.error." + error), true);
            return false;
        } else {
            BarrierData.Field field = new BarrierData.Field(UUID.randomUUID(), owner.getUUID(), Vec3.atCenterOf(center), radius, defensive);
            this.fields.put(field.id, field);
            this.setDirty();
            ArcanaNetwork.syncFields(level);
            owner.displayClientMessage(Component.translatable("message.frieren_arcana.created", radius), true);
            return true;
        }
    }

    public boolean remove(ServerLevel level, UUID id, boolean shattered) {
        BarrierData.Field field = this.fields.remove(id);
        if (field == null) {
            return false;
        } else {
            this.setDirty();
            ArcanaNetwork.syncFields(level);
            if (shattered) {
                ArcanaNetwork.shatter(level, field);
            }

            return true;
        }
    }

    public BarrierData.Field firstBoundary(Vec3 start, Vec3 end, boolean projectile) {
        BarrierData.Field closest = null;
        double best = Double.POSITIVE_INFINITY;
        Vec3 delta = end.subtract(start);

        for (BarrierData.Field f : this.fields.values()) {
            if (!f.defensive || projectile && !f.contains(start)) {
                Vec3 rel = start.subtract(f.center);
                double hit = BarrierGeometry.firstHit(rel.x, rel.y, rel.z, delta.x, delta.y, delta.z, (double)f.radius);
                if (hit < best) {
                    best = hit;
                    closest = f;
                }
            }
        }

        return closest;
    }

    public boolean crosses(Vec3 start, Vec3 end, boolean projectile) {
        return this.firstBoundary(start, end, projectile) != null;
    }

    public boolean containsConfining(Vec3 pos) {
        return this.fields.values().stream().anyMatch(f -> !f.defensive && f.contains(pos));
    }

    @Override
    public CompoundTag save(CompoundTag root, Provider lookup) {
        ListTag list = new ListTag();

        for (BarrierData.Field f : this.fields.values()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", f.id);
            tag.putUUID("owner", f.owner);
            tag.putDouble("x", f.center.x);
            tag.putDouble("y", f.center.y);
            tag.putDouble("z", f.center.z);
            tag.putInt("radius", f.radius);
            tag.putBoolean("defensive", f.defensive);
            list.add(tag);
        }

        root.put("fields", list);
        return root;
    }

    public static BarrierData load(CompoundTag root, Provider lookup) {
        BarrierData data = new BarrierData();

        for (Tag raw : root.getList("fields", 10)) {
            CompoundTag tag = (CompoundTag)raw;
            BarrierData.Field f = new BarrierData.Field(
                tag.getUUID("id"),
                tag.getUUID("owner"),
                new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")),
                tag.getInt("radius"),
                tag.getBoolean("defensive")
            );
            data.fields.put(f.id, f);
        }

        return data;
    }

    public static final class Field {
        public final UUID id;
        public final UUID owner;
        public final Vec3 center;
        public final int radius;
        public final boolean defensive;

        public Field(UUID id, UUID owner, Vec3 center, int radius, boolean defensive) {
            this.id = id;
            this.owner = owner;
            this.center = center;
            this.radius = radius;
            this.defensive = defensive;
        }

        public boolean contains(Vec3 p) {
            return p.distanceToSqr(this.center) < (double)(this.radius * this.radius);
        }
    }
}
