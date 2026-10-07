package dev.pete.frierenarcana;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellCooldownAddedEvent.Post;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent.Detonate;
import net.neoforged.neoforge.event.level.PistonEvent.Pre;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ArcanaEvents {
    public static CompoundTag flags(ServerPlayer p) {
        CompoundTag persistent = p.getPersistentData();
        if (!persistent.contains("frieren_arcana")) {
            persistent.put("frieren_arcana", new CompoundTag());
        }

        return persistent.getCompound("frieren_arcana");
    }

    public static void syncMana(ServerPlayer p) {
        PacketDistributor.sendToPlayer(p, new SyncManaPacket(MagicData.getPlayerMagicData(p)));
    }

    @SubscribeEvent
    public static void preventBoundCasting(SpellPreCastEvent event) {
        if (ExpandedMagic.held(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void spellCost(SpellOnCastEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            if (SpellRegistry.getSpell(event.getSpellId()) instanceof ArcanaSpell spell) {
                boolean var6 = spell.ready(p, event.getSpellLevel(), true);
                flags(p).putBoolean("castApproved", var6);
                event.setManaCost(var6 ? spell.cost(event.getSpellLevel(), p) : 0);
                if (var6 && !event.getCastSource().consumesMana()) {
                    MagicData data = MagicData.getPlayerMagicData(p);
                    data.setMana(Math.max(0.0F, data.getMana() - (float)spell.cost(event.getSpellLevel(), p)));
                    syncMana(p);
                }
            }
        }
    }

    @SubscribeEvent
    public static void cooldown(Post event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getSpell() instanceof ArcanaSpell spell && spell.kind == ArcanaSpell.Kind.PIERCE) {
            ArcanaCooldowns.record(p, event.getEffectiveCooldown());
        }
    }

    @SubscribeEvent
    public static void levelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AdvancedMagic.tick(level);
        }
    }

    @SubscribeEvent
    public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            CompoundTag var5 = flags(p);
            ArcanaModes.tick(p);
            ExpandedMagic.tick(p);
            if (var5.getBoolean("flight") && !ArcanaModes.hasStaff(p)) {
                var5.putBoolean("flight", false);
                updateFlight(p);
                p.displayClientMessage(Component.translatable("message.frieren_arcana.error.staff"), true);
            }

            if (var5.getBoolean("hovering") && !MagicData.getPlayerMagicData(p).isCasting()) {
                endHover(p);
            }

            if (var5.getBoolean("flight")) {
                if (!p.getAbilities().mayfly) {
                    updateFlight(p);
                }

                if (p.getAbilities().flying) {
                    MagicData magic = MagicData.getPlayerMagicData(p);
                    float drain = ArcanaConfig.FLIGHT_DRAIN.get().floatValue() / 20.0F;
                    if (magic.getMana() < drain) {
                        var5.putBoolean("flight", false);
                        updateFlight(p);
                        p.displayClientMessage(Component.translatable("message.frieren_arcana.flight_empty"), true);
                    } else {
                        magic.setMana(magic.getMana() - drain);
                        if (p.tickCount % 10 == 0) {
                            syncMana(p);
                        }
                    }

                    p.fallDistance = 0.0F;
                }
            }

            if (p.tickCount % 10 == 0) {
                ArcanaNetwork.syncSight(p);
                ArcanaNetwork.flight(p);
            }
        }
    }

    public static void updateFlight(ServerPlayer p) {
        CompoundTag f = flags(p);
        Abilities abilities = p.getAbilities();
        boolean enabled = f.getBoolean("flight");
        if (enabled) {
            if (!f.getBoolean("flightGranted")) {
                f.putBoolean("oldMayfly", abilities.mayfly);
                f.putBoolean("flightGranted", true);
            }

            abilities.mayfly = true;
        } else if (f.getBoolean("flightGranted")) {
            if (!p.isCreative() && !p.isSpectator() && !f.getBoolean("oldMayfly")) {
                abilities.mayfly = false;
                abilities.flying = false;
                p.fallDistance = 0.0F;
            }

            f.putBoolean("flightGranted", false);
        }

        p.onUpdateAbilities();
        ArcanaNetwork.flight(p);
        if (!enabled) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
        }
    }

    public static void endHover(ServerPlayer p) {
        CompoundTag f = flags(p);
        ArcanaModes.tick(p);
        ExpandedMagic.tick(p);
        if (f.getBoolean("flight") && !ArcanaModes.hasStaff(p)) {
            f.putBoolean("flight", false);
            updateFlight(p);
            p.displayClientMessage(Component.translatable("message.frieren_arcana.error.staff"), true);
        }

        if (f.getBoolean("hovering")) {
            p.setNoGravity(f.getBoolean("hoverOldGravity"));
            f.putBoolean("hovering", false);
            p.fallDistance = 0.0F;
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
            ArcanaNetwork.charge(p, false);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            endHover(p);
            ArcanaModes.stopBarrage(p);
            ExpandedMagic.release(p);
        }
    }

    @SubscribeEvent
    public static void login(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            endHover(p);
            ArcanaModes.stopBarrage(p);
            ExpandedMagic.release(p);
            if (!ArcanaModes.hasStaff(p)) {
                flags(p).putBoolean("flight", false);
            }

            updateFlight(p);
            ArcanaCooldowns.restore(p);
            ArcanaNetwork.syncFields(p);
            ArcanaNetwork.syncSight(p);
        }
    }

    @SubscribeEvent
    public static void dimension(PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            ArcanaNetwork.syncFields(p);
            ArcanaNetwork.syncSight(p);
        }
    }

    @SubscribeEvent
    public static void clone(Clone event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getOriginal() instanceof ServerPlayer old) {
            CompoundTag copy = flags(old).copy();
            if (event.isWasDeath()) {
                copy.putBoolean("barrage", false);
                copy.remove("bindingTarget");
                copy.putBoolean("flight", false);
                copy.putBoolean("flightGranted", false);
                copy.putBoolean("hovering", false);
                copy.putBoolean("mana_sight", false);
                copy.putBoolean("mana_concealment", false);
            }

            p.getPersistentData().put("frieren_arcana", copy);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            updateFlight(p);
            ArcanaCooldowns.restore(p);
            ArcanaNetwork.syncFields(p);
        }
    }

    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            Vec3 var5 = event.getSource().getSourcePosition();
            if (event.getSource().getDirectEntity() != null) {
                var5 = ShipSpace.world(event.getSource().getDirectEntity());
            } else if (var5 != null) {
                var5 = ShipSpace.world(level, var5);
            }

            Vec3 target = ShipSpace.world(level, event.getEntity().getBoundingBox().getCenter());
            Entity owner = event.getSource().getEntity();
            if (var5 != null && BarrierData.get(level).crosses(var5, target, true)
                || owner != null && owner.level() == level && BarrierData.get(level).crosses(ShipSpace.world(owner), target, true)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void dimensionTravel(EntityTravelToDimensionEvent event) {
        if (ExpandedMagic.held(event.getEntity())) {
            event.setCanceled(true);
        } else {
            if (event.getEntity().level() instanceof ServerLevel level && BarrierData.get(level).containsConfining(ShipSpace.world(event.getEntity()))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void teleport(EntityTeleportEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level
            && BarrierData.get(level)
                .crosses(
                    ShipSpace.world(event.getEntity()), ShipSpace.world(level, new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ())), false
                )) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void spawned(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel && event.getEntity() instanceof ItemEntity item) {
            AdvancedMagic.recover(item);
        }

        if (event.getLevel() instanceof ServerLevel level
            && event.getEntity() instanceof LightningBolt
            && BarrierData.get(level).containsConfining(ShipSpace.world(event.getEntity()))) {
            event.setCanceled(true);
            return;
        }

        if (event.getLevel() instanceof ServerLevel level
            && event.getEntity() instanceof Projectile p
            && p.getOwner() != null
            && BarrierData.get(level).crosses(ShipSpace.world(p.getOwner()), ShipSpace.world(p), true)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockBreak(BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && BarrierData.get(level).protects(event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void place(EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && BarrierData.get(level).protects(event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void explosion(Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Vec3 var4 = ShipSpace.world(level, event.getExplosion().center());
            BarrierData fields = BarrierData.get(level);
            event.getAffectedBlocks().removeIf(pos -> fields.crosses(var4, ShipSpace.world(level, Vec3.atCenterOf(pos)), true));
            event.getAffectedEntities().removeIf(e -> fields.crosses(var4, ShipSpace.world(e), true));
        }
    }

    @SubscribeEvent
    public static void piston(Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PistonStructureResolver resolver = event.getStructureHelper();
            if (resolver.resolve()) {
                for (BlockPos pos : resolver.getToPush()) {
                    Vec3 a = ShipSpace.world(level, Vec3.atCenterOf(pos));
                    Vec3 b = ShipSpace.world(level, Vec3.atCenterOf(pos.relative(event.getDirection())));
                    if (BarrierData.get(level).crosses(a, b, false)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("frierenarcana");
        root.then(Commands.literal("uncast").executes(c -> {
            BarrierData.releaseOwned(c.getSource().getPlayerOrException(), null);
            return 1;
        }).then(Commands.literal("examination").executes(c -> {
            BarrierData.releaseOwned(c.getSource().getPlayerOrException(), false);
            return 1;
        })).then(Commands.literal("defensive").executes(c -> {
            BarrierData.releaseOwned(c.getSource().getPlayerOrException(), true);
            return 1;
        })));
        root.then(
            Commands.literal("list")
                .executes(
                    c -> {
                        ServerPlayer p = c.getSource().getPlayerOrException();
                        int n = 0;

                        for (ServerLevel level : p.server.getAllLevels()) {
                            for (BarrierData.Field f : BarrierData.get(level).fields()) {
                                if (f.owner.equals(p.getUUID()) || c.getSource().hasPermission(2)) {
                                    c.getSource()
                                        .sendSuccess(
                                            () -> Component.literal(
                                                    f.id
                                                        + " "
                                                        + (f.defensive ? "defensive" : "examination")
                                                        + " "
                                                        + level.dimension().location()
                                                        + " "
                                                        + f.center
                                                        + " owner="
                                                        + f.owner
                                                ),
                                            false
                                        );
                                    n++;
                                }
                            }
                        }

                        return n;
                    }
                )
        );
        root.then(Commands.literal("admin_clear_dimension").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerLevel level = c.getSource().getLevel();
            BarrierData data = BarrierData.get(level);
            int count = data.fields().size();

            for (BarrierData.Field f : List.copyOf(data.fields())) {
                data.remove(level, f.id, false);
            }

            c.getSource().sendSuccess(() -> Component.literal("Removed " + count + " barriers in this dimension."), true);
            return count;
        }));
        event.getDispatcher().register(root);
    }
}
