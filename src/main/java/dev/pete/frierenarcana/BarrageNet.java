package dev.pete.frierenarcana;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class BarrageNet {
    private BarrageNet() {
    }

    public static boolean blocked(ServerPlayer var0) {
        CompoundTag var1 = ArcanaEvents.flags(var0);
        return var0.serverLevel().getGameTime() - var1.getLong("barrageStoppedAt") < 8L;
    }

    public static void started(ServerPlayer var0) {
        send(var0, true);
    }

    public static void stopping(ServerPlayer var0) {
        CompoundTag var1 = ArcanaEvents.flags(var0);
        if (var1.getBoolean("barrage")) {
            var1.putLong("barrageStoppedAt", var0.serverLevel().getGameTime());
            send(var0, false);
        }
    }

    private static void send(ServerPlayer var0, boolean var1) {
        CompoundTag var2 = new CompoundTag();
        var2.putString("kind", "barrage");
        var2.putBoolean("active", var1);
        PacketDistributor.sendToPlayer(var0, new ArcanaNetwork.Payload(var2));
    }
}
