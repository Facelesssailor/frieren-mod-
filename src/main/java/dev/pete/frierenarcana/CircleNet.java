package dev.pete.frierenarcana;

import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class CircleNet {
    private static final Set<String> WITH_CIRCLE = Set.of(
        "zoltraak",
        "zoltraak_barrage",
        "zoltraak_heavy",
        "nephtear",
        "reamstroha",
        "balgrant",
        "sorganeil",
        "vollzanbel",
        "judradjim",
        "waldgose",
        "daosdorg",
        "catastrovia",
        "goddess_healing",
        "goddess_cleansing",
        "goddess_three_spears",
        "golden_transmutation",
        "dragate",
        "reelseiden",
        "examination_barrier",
        "defensive_barrier",
        "jubelade"
    );

    private CircleNet() {
    }

    public static void cast(LivingEntity var0, ArcanaSpell var1, int var2) {
        if (var0 instanceof ServerPlayer var3) {
            int var4 = Math.max(24, var1.getCastTime(var2) + 14);
            refresh(var3, var1, var4);
        }
    }

    public static void refresh(ServerPlayer var0, ArcanaSpell var1, int var2) {
        String var3 = "";
        if (WITH_CIRCLE.contains(var3)) {
            CompoundTag var4 = new CompoundTag();
            var4.putString("kind", "circle");
            var4.putUUID("player", var0.getUUID());
            var4.putString("spell", var3);
            var4.putInt("ticks", var2);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(var0, new ArcanaNetwork.Payload(var4));
        }
    }
}
