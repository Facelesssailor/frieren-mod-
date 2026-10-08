package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.client.ArcanaCinematic;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({MouseHandler.class})
public abstract class CinematicMouseMixin {
    @Redirect(
        method = {"turnPlayer"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
        ),
        require = 0
    )
    private void frierenArcana$turn(LocalPlayer var1, double var2, double var4) {
        if (!ArcanaCinematic.active()) {
            var1.turn(var2, var4);
        }
    }
}
