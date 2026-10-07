package dev.pete.frierenarcana;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ShipSpace {
    private ShipSpace() {
    }

    public static Vec3 world(Level level, Vec3 position) {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, position);
    }

    public static Vec3 world(Entity entity) {
        return world(entity.level(), entity.position());
    }

    public static boolean inPlot(Entity entity) {
        return SableCompanion.INSTANCE.isInPlotGrid(entity.level(), entity.position());
    }

    public static Vec3 local(Entity entity, Vec3 worldPosition) {
        SubLevelAccess ship = SableCompanion.INSTANCE.getContaining(entity);
        return ship == null ? worldPosition : ship.logicalPose().transformPositionInverse(worldPosition);
    }
}
