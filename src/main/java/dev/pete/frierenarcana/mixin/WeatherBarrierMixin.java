package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.client.ArcanaClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({LevelRenderer.class})
public abstract class WeatherBarrierMixin {
    @Redirect(
        method = {"renderSnowAndRain"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"
        )
    )
    private int arcana$rainRoof(Level level, Types type, int x, int z) {
        return ArcanaClient.rainHeight(x, z, level.getHeight(type, x, z));
    }

    @Redirect(
        method = {"tickRain"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelReader;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
        )
    )
    private BlockPos arcana$splashRoof(LevelReader level, Types type, BlockPos pos) {
        BlockPos terrain = level.getHeightmapPos(type, pos);
        return new BlockPos(terrain.getX(), ArcanaClient.rainHeight(terrain.getX(), terrain.getZ(), terrain.getY()), terrain.getZ());
    }
}
