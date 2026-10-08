package dev.pete.frierenarcana.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.pete.frierenarcana.client.ArcanaCinematic;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({MouseHandler.class})
public abstract class CinematicMouseMixin {
    @WrapOperation(
        method = {"turnPlayer"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
        )},
        require = 0
    )
    private void frierenArcana$turn(LocalPlayer var1, double var2, double var4, Operation<Void> var6) {
        if (!ArcanaCinematic.active()) {
            var6.call(var1, var2, var4);
        }
    }
}
