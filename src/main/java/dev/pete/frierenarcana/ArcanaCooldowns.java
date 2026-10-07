package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import io.redspace.ironsspellbooks.network.casting.SyncCooldownPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ArcanaCooldowns {
    public static void begin(ServerPlayer p, ArcanaSpell spell, CastSource source) {
        int duration = MagicManager.getEffectiveSpellCooldown(spell, p, source);
        record(p, duration);
        MagicData.getPlayerMagicData(p).getPlayerCooldowns().addCooldown(spell, duration);
        PacketDistributor.sendToPlayer(p, new SyncCooldownPacket(spell.getSpellId(), duration));
    }

    public static void record(ServerPlayer p, int duration) {
        CompoundTag f = ArcanaEvents.flags(p);
        f.putLong("breakerReadyTick", p.server.overworld().getGameTime() + (long)duration);
        f.putInt("breakerCooldownTotal", duration);
        f.putInt("breakerCooldownVersion", 2);
    }

    public static void restore(ServerPlayer p) {
        CompoundTag f = ArcanaEvents.flags(p);
        ArcanaSpell spell = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
        long now = p.server.overworld().getGameTime();
        long remaining = Math.max(0L, f.getLong("breakerReadyTick") - now);
        if (f.getInt("breakerCooldownVersion") < 2) {
            remaining = Math.min(remaining, (long)spell.getSpellCooldown());
            f.putLong("breakerReadyTick", now + remaining);
            f.putInt("breakerCooldownTotal", spell.getSpellCooldown());
            f.putInt("breakerCooldownVersion", 2);
        }

        PlayerCooldowns cooldowns = MagicData.getPlayerMagicData(p).getPlayerCooldowns();
        cooldowns.removeCooldown(spell.getSpellId());
        if (remaining > 0L) {
            cooldowns.addCooldown(spell, Math.max((int)remaining, f.getInt("breakerCooldownTotal")), (int)Math.min(2147483647L, remaining));
        }

        cooldowns.syncToPlayer(p);
    }
}
