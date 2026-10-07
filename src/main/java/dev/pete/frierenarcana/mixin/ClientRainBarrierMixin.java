package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.client.ArcanaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientLevel.class})
public abstract class ClientRainBarrierMixin {
    @Inject(
        method = {"addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$splash(ParticleOptions type, double x, double y, double z, double dx, double dy, double dz, CallbackInfo ci) {
        if (type.getType() == ParticleTypes.RAIN && ArcanaClient.rainBlocked(new Vec3(x, y, z))) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$rainSound(double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean delay, CallbackInfo ci) {
        if ((sound == SoundEvents.WEATHER_RAIN || sound == SoundEvents.WEATHER_RAIN_ABOVE)
            && ArcanaClient.rainBlocked(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())) {
            ci.cancel();
        }
    }
}
