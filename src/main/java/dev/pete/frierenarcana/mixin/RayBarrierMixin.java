package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BlockGetter.class})
public interface RayBarrierMixin {
    @Inject(
        method = {"clip"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void arcana$ray(ClipContext context, CallbackInfoReturnable<BlockHitResult> ci) {
        if (this instanceof Level level) {
            Vec3 from = context.getFrom();
            Vec3 to = ci.getReturnValue().getLocation();
            Vec3 end = BarrierHooks.clip(level, from, to);
            if (end.distanceToSqr(to) > 1.0E-10) {
                Vec3 normal = from.subtract(end);
                ci.setReturnValue(new BlockHitResult(end, Direction.getNearest(normal.x, normal.y, normal.z), BlockPos.containing(end), false));
            }
        }
    }
}
