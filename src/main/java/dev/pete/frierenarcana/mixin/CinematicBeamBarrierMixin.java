package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
    targets = {"com.frierenflight.zoltraakcinematic.entity.ZoltraakCinematicBeamEntity"},
    remap = false
)
public abstract class CinematicBeamBarrierMixin {
    @Shadow
    public abstract Vec3 visualOrigin(float var1);

    @Shadow
    public abstract Vec3 visualDirection(float var1);

    @Inject(
        method = {"getBeamLength"},
        at = {@At("RETURN")},
        cancellable = true,
        remap = false
    )
    private void arcana$length(CallbackInfoReturnable<Float> ci) {
        Entity entity = (Entity)this;
        Vec3 from = this.visualOrigin(1.0F);
        Vec3 to = from.add(this.visualDirection(1.0F).normalize().scale((double)ci.getReturnValueF()));
        ci.setReturnValue((float)from.distanceTo(BarrierHooks.clip(entity.level(), from, to)));
    }
}
