package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.client.ArcanaClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WaterDropParticle.class})
public abstract class FallingRainBarrierMixin extends TextureSheetParticle {
    protected FallingRainBarrierMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Inject(
        method = {"tick"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$dry(CallbackInfo ci) {
        if (ArcanaClient.rainBlocked(new Vec3(this.x, this.y, this.z))) {
            this.remove();
            ci.cancel();
        }
    }
}
