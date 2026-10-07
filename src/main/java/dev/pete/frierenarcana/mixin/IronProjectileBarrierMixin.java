package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierHooks;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractMagicProjectile.class})
public abstract class IronProjectileBarrierMixin {
    @Unique
    private boolean arcana$surfaceHit;

    @Inject(
        method = {"handleHitDetection"},
        at = {@At("HEAD")}
    )
    private void arcana$clip(CallbackInfo ci) {
        this.arcana$surfaceHit = BarrierHooks.clipProjectile((AbstractMagicProjectile)this);
    }

    @Inject(
        method = {"handleHitDetection"},
        at = {@At("RETURN")}
    )
    private void arcana$consume(CallbackInfo ci) {
        if (this.arcana$surfaceHit) {
            ((AbstractMagicProjectile)this).discard();
        }
    }
}
