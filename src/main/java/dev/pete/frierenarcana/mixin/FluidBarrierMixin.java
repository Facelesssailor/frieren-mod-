package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierData;
import dev.pete.frierenarcana.ShipSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FlowingFluid.class})
public abstract class FluidBarrierMixin {
    @Inject(
        method = {"canSpreadTo"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$fluid(
        BlockGetter getter,
        BlockPos from,
        BlockState fromState,
        Direction direction,
        BlockPos to,
        BlockState toState,
        FluidState state,
        Fluid fluid,
        CallbackInfoReturnable<Boolean> ci
    ) {
        if (getter instanceof ServerLevel level
            && BarrierData.get(level).crosses(ShipSpace.world(level, Vec3.atCenterOf(from)), ShipSpace.world(level, Vec3.atCenterOf(to)), false)) {
            ci.setReturnValue(false);
        }
    }
}
