package dev.pete.frierenarcana;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class CircleNet {
    private CircleNet() {
    }

    public static void cast(LivingEntity var0, ArcanaSpell var1, int var2) {
        if (var0 instanceof ServerPlayer var3) {
            int var4 = Math.max(24, var1.getEffectiveCastTime(var2, var0) + 14);
            refresh(var3, var1, var4);
        }
    }

    public static void refresh(ServerPlayer var0, ArcanaSpell var1, int var2) {
        String var3 = var1.getSpellResource().getPath();
        CompoundTag var4 = new CompoundTag();
        var4.putString("kind", "circle");
        var4.putUUID("player", var0.getUUID());
        var4.putString("spell", var3);
        var4.putInt("ticks", var2);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(var0, new ArcanaNetwork.Payload(var4));
    }
}
