package dev.pete.frierenarcana.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.pete.frierenarcana.client.ArcanaClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({LevelRenderer.class})
public abstract class WeatherBarrierMixin {
    @WrapOperation(
        method = {"renderSnowAndRain"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"
        )},
        require = 0
    )
    private int arcana$rainRoof(Level var1, Types var2, int var3, int var4, Operation<Integer> var5) {
        return ArcanaClient.rainHeight(var3, var4, (Integer)var5.call(var1, var2, var3, var4));
    }

    @WrapOperation(
        method = {"tickRain"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelReader;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
        )},
        require = 0
    )
    private BlockPos arcana$splashRoof(LevelReader var1, Types var2, BlockPos var3, Operation<BlockPos> var4) {
        BlockPos var5 = (BlockPos)var4.call(var1, var2, var3);
        return new BlockPos(var5.getX(), ArcanaClient.rainHeight(var5.getX(), var5.getZ(), var5.getY()), var5.getZ());
    }
}
