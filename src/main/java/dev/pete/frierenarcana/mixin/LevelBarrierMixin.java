package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierData;
import dev.pete.frierenarcana.BarrierHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Level.class})
public abstract class LevelBarrierMixin {
    @Inject(
        method = {"setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$protect(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> ci) {
        if (this instanceof ServerLevel level && BarrierData.get(level).protects(pos)) {
            ci.setReturnValue(false);
        }
    }

    @Inject(
        method = {"isRainingAt"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$weather(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (BarrierHooks.rainBlocked((Level)this, pos)) {
            ci.setReturnValue(false);
        }
    }
}
