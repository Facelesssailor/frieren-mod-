package dev.pete.frierenarcana;

import dev.pete.frierenarcana.client.ArcanaClient;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ArcanaNetwork {
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("4").playToServer(ArcanaNetwork.ModeRequest.TYPE, ArcanaNetwork.ModeRequest.CODEC, (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer p) {
                    ArcanaModes.request(p, payload.action);
                }
            }));
        event.registrar("4")
            .playToClient(
                ArcanaNetwork.Payload.TYPE, ArcanaNetwork.Payload.CODEC, (payload, context) -> context.enqueueWork(() -> ArcanaClient.receive(payload.data))
            );
    }

    private static CompoundTag message(String kind) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", kind);
        return tag;
    }

    private static void vector(CompoundTag tag, String key, Vec3 p) {
        tag.putDouble(key + "X", p.x);
        tag.putDouble(key + "Y", p.y);
        tag.putDouble(key + "Z", p.z);
    }

    public static Vec3 vector(CompoundTag tag, String key) {
        return new Vec3(tag.getDouble(key + "X"), tag.getDouble(key + "Y"), tag.getDouble(key + "Z"));
    }

    public static CompoundTag field(BarrierData.Field field) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", field.id);
        tag.putUUID("owner", field.owner);
        vector(tag, "center", field.center);
        tag.putInt("radius", field.radius);
        tag.putBoolean("defensive", field.defensive);
        return tag;
    }

    public static void syncFields(ServerPlayer p) {
        CompoundTag tag = message("fields");
        ListTag list = new ListTag();

        for (BarrierData.Field f : BarrierData.get(p.serverLevel()).fields()) {
            list.add(field(f));
        }

        tag.put("fields", list);
        PacketDistributor.sendToPlayer(p, new ArcanaNetwork.Payload(tag));
    }

    public static void syncFields(ServerLevel level) {
        for (ServerPlayer p : level.players()) {
            syncFields(p);
        }
    }

    public static void shatter(ServerLevel level, BarrierData.Field field) {
        shatter(level, field, field.center.add((double)field.radius, 0.0, 0.0));
    }

    public static void shatter(ServerLevel level, BarrierData.Field field, Vec3 impact) {
        RainHold.broken(level, field);
        CompoundTag tag = message("shatter");
        tag.put("field", field(field));
        vector(tag, "impact", impact);
        sendNearby(level, field.center, (double)(field.radius + 128), tag);
    }

    public static void flight(ServerPlayer p) {
        CompoundTag tag = message("flight");
        tag.putUUID("player", p.getUUID());
        tag.putBoolean("active", ArcanaEvents.flags(p).getBoolean("flight") && p.getAbilities().flying && ArcanaModes.hasStaff(p));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p, new ArcanaNetwork.Payload(tag));
    }

    public static void magicBeam(ServerLevel level, Vec3 start, Vec3 end, int style) {
        CompoundTag tag = message("beam");
        vector(tag, "start", start);
        vector(tag, "end", end);
        tag.putInt("style", style);
        sendNearby(level, start, 192.0, tag);
    }

    public static void effect(ServerLevel level, Vec3 center, int style, int strength) {
        CompoundTag tag = message("effect");
        vector(tag, "center", center);
        tag.putInt("style", style);
        tag.putInt("strength", strength);
        sendNearby(level, center, 96.0, tag);
    }

    public static void beam(ServerLevel level, Vec3 start, Vec3 end, boolean breaker) {
        magicBeam(level, start, end, breaker ? 3 : 0);
    }

    public static void charge(ServerPlayer p, boolean active) {
        CompoundTag tag = message("charge");
        tag.putUUID("player", p.getUUID());
        tag.putBoolean("active", active);
        tag.putBoolean("breaker", MagicData.getPlayerMagicData(p).getCastingSpellId().equals(FrierenArcana.id("barrier_breaker").toString()));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p, new ArcanaNetwork.Payload(tag));
    }

    private static void sendNearby(ServerLevel level, Vec3 center, double range, CompoundTag tag) {
        for (ServerPlayer p : level.players()) {
            if (ShipSpace.world(p).distanceToSqr(center) < range * range) {
                PacketDistributor.sendToPlayer(p, new ArcanaNetwork.Payload(tag));
            }
        }
    }

    public static void syncSight(ServerPlayer observer) {
        CompoundTag tag = message("sight");
        ListTag list = new ListTag();
        boolean active = ArcanaEvents.flags(observer).getBoolean("mana_sight");
        tag.putBoolean("active", active);
        if (active) {
            double range = ArcanaConfig.SIGHT_RANGE.get();
            Vec3 from = ShipSpace.world(observer);

            for (ServerPlayer target : observer.serverLevel().players()) {
                if (target != observer
                    && !target.isSpectator()
                    && ShipSpace.world(target).distanceToSqr(from) <= range * range
                    && !BarrierData.get(observer.serverLevel()).crosses(from, ShipSpace.world(target), false)) {
                    double factor = ArcanaEvents.flags(target).getBoolean("mana_concealment") ? ArcanaConfig.CONCEAL_RATIO.get() : 1.0;
                    CompoundTag entry = new CompoundTag();
                    entry.putUUID("player", target.getUUID());
                    entry.putFloat("mana", (float)((double)MagicData.getPlayerMagicData(target).getMana() * factor));
                    entry.putFloat("capacity", (float)(target.getAttributeValue(AttributeRegistry.MAX_MANA) * factor));
                    list.add(entry);
                }
            }
        }

        tag.put("players", list);
        PacketDistributor.sendToPlayer(observer, new ArcanaNetwork.Payload(tag));
    }

    public static record ModeRequest(int action) implements CustomPacketPayload {
        public static final Type<ArcanaNetwork.ModeRequest> TYPE = new Type<>(FrierenArcana.id("zoltraak_mode"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ArcanaNetwork.ModeRequest> CODEC = StreamCodec.of(
            (b, p) -> b.writeVarInt(p.action), b -> new ArcanaNetwork.ModeRequest(b.readVarInt())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static record Payload(CompoundTag data) implements CustomPacketPayload {
        public static final Type<ArcanaNetwork.Payload> TYPE = new Type<>(FrierenArcana.id("visual_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ArcanaNetwork.Payload> CODEC = StreamCodec.of(
            (b, p) -> b.writeNbt(p.data), b -> new ArcanaNetwork.Payload(Objects.requireNonNull(b.readNbt()))
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
