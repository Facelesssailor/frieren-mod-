package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.CastResult.Type;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ArcanaSpell extends AbstractSpell {
    public final ArcanaSpell.Kind kind;

    public ArcanaSpell(ArcanaSpell.Kind var1) {
        this.kind = var1;
        this.baseManaCost = var1.cost;
        this.manaCostPerLevel = var1.levels > 1 && var1 != ArcanaSpell.Kind.LIFT ? 10 : 0;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = var1 == ArcanaSpell.Kind.LIFT
            ? 140
            : (
                var1 == ArcanaSpell.Kind.PIERCE
                    ? 200
                    : (
                        var1 == ArcanaSpell.Kind.HEAVY
                            ? 160
                            : (
                                var1 == ArcanaSpell.Kind.GOLD
                                    ? 100
                                    : (
                                        var1 != ArcanaSpell.Kind.EXAM && var1 != ArcanaSpell.Kind.INFERNO
                                            ? (var1 == ArcanaSpell.Kind.THUNDER ? 30 : (var1 == ArcanaSpell.Kind.JUDGMENT ? 20 : 0))
                                            : 40
                                    )
                            )
                    )
            );
    }

    @Override
    public ResourceLocation getSpellResource() {
        return FrierenArcana.id(this.kind.path);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig().setMinRarity(this.kind.rarity).setSchoolResource(switch (this.kind) {
            case INFERNO, FIREWIND -> SchoolRegistry.FIRE_RESOURCE;
            case THUNDER -> SchoolRegistry.LIGHTNING_RESOURCE;
            default -> SchoolRegistry.EVOCATION_RESOURCE;
            case SPEARS -> SchoolRegistry.HOLY_RESOURCE;
        }).setMaxLevel(this.kind.levels).setCooldownSeconds((double)this.kind.cooldown).build();
    }

    @Override
    public int getCastTime(int var1) {
        return this.kind == ArcanaSpell.Kind.LIFT ? 140 + 20 * (var1 - 1) : super.getCastTime(var1);
    }

    @Override
    public CastType getCastType() {
        return this.kind == ArcanaSpell.Kind.LIFT
            ? CastType.CONTINUOUS
            : (
                this.kind != ArcanaSpell.Kind.PIERCE
                        && this.kind != ArcanaSpell.Kind.EXAM
                        && this.kind != ArcanaSpell.Kind.HEAVY
                        && this.kind != ArcanaSpell.Kind.GOLD
                        && this.kind != ArcanaSpell.Kind.INFERNO
                        && this.kind != ArcanaSpell.Kind.THUNDER
                        && this.kind != ArcanaSpell.Kind.JUDGMENT
                    ? CastType.INSTANT
                    : CastType.LONG
            );
    }

    @Override
    public int getManaCost(int var1) {
        return this.kind == ArcanaSpell.Kind.HEAVY ? ArcanaConfig.HEAVY_COST.get() : super.getManaCost(var1);
    }

    @Override
    public int getEffectiveCastTime(int var1, LivingEntity var2) {
        return this.kind == ArcanaSpell.Kind.HEAVY
            ? Math.max(ArcanaConfig.HEAVY_CHARGE.get() * 20, super.getEffectiveCastTime(var1, var2))
            : super.getEffectiveCastTime(var1, var2);
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return this.kind == ArcanaSpell.Kind.PIERCE
            ? new AnimationHolder(FrierenArcana.id("barrier_breaker_release"), true, true)
            : super.getCastFinishAnimation();
    }

    @Override
    public void onServerPreCast(Level var1, int var2, LivingEntity var3, MagicData var4) {
        CircleNet.cast(var3, this, var2);
        if (this.kind == ArcanaSpell.Kind.PIERCE && var3 instanceof ServerPlayer var5) {
            CompoundTag var6 = ArcanaEvents.flags(var5);
            var6.putBoolean("hovering", true);
            var6.putBoolean("hoverOldGravity", var5.isNoGravity());
            var5.setNoGravity(true);
            ArcanaNetwork.charge(var5, true);
        }

        if (this.kind == ArcanaSpell.Kind.HEAVY && var3 instanceof ServerPlayer var7) {
            ArcanaModes.stopBarrage(var7);
        }

        super.onServerPreCast(var1, var2, var3, var4);
    }

    @Override
    public void onServerCastTick(Level var1, int var2, LivingEntity var3, MagicData var4) {
        if (this.kind == ArcanaSpell.Kind.PIERCE && var3 instanceof ServerPlayer var5) {
            var5.setDeltaMovement(0.0, 0.015, 0.0);
            var5.hurtMarked = true;
            var5.fallDistance = 0.0F;
        }

        if (this.kind == ArcanaSpell.Kind.LIFT && var3 instanceof ServerPlayer var6) {
            AdvancedMagic.liftTick(var6, var2, var4);
        }
    }

    @Override
    public void onServerCastComplete(Level var1, int var2, LivingEntity var3, MagicData var4, boolean var5) {
        if (this.kind == ArcanaSpell.Kind.PIERCE && var3 instanceof ServerPlayer var6) {
            ArcanaEvents.endHover(var6);
        }

        if (this.kind == ArcanaSpell.Kind.HEAVY && var3 instanceof ServerPlayer var7) {
            ArcanaNetwork.charge(var7, false);
        }

        if (this.kind == ArcanaSpell.Kind.LIFT && var3 instanceof ServerPlayer var8) {
            AdvancedMagic.releaseLift(var8);
        }

        super.onServerCastComplete(var1, var2, var3, var4, var5);
    }

    @Override
    public int getSpellCooldown() {
        return this.kind == ArcanaSpell.Kind.PIERCE ? ArcanaConfig.PIERCE_COOLDOWN.get() * 20 : super.getSpellCooldown();
    }

    public int radius(int var1) {
        return this.kind == ArcanaSpell.Kind.EXAM
            ? Math.min(56, ArcanaConfig.EXAM_RADIUS.get() + 2 * (var1 - 1))
            : ArcanaConfig.DEFENSE_RADIUS.get() + var1 - 1;
    }

    public boolean active(ServerPlayer var1) {
        return switch (this.kind) {
            case EXAM, DEFENSE -> BarrierData.owns(var1, this.kind == ArcanaSpell.Kind.DEFENSE);
            default -> false;
            case SIGHT, FLIGHT, CONCEAL -> ArcanaEvents.flags(var1).getBoolean(this.kind.path);
            case BARRAGE -> ArcanaEvents.flags(var1).getBoolean("barrage");
        };
    }

    public int cost(int var1, ServerPlayer var2) {
        if (this.active(var2)) {
            return 0;
        } else {
            return this.kind == ArcanaSpell.Kind.PIERCE ? (int)Math.ceil(var2.getAttributeValue(AttributeRegistry.MAX_MANA)) : this.getManaCost(var1);
        }
    }

    @Override
    public CastResult canBeCastedBy(int var1, CastSource var2, MagicData var3, Player var4) {
        if (var4 instanceof ServerPlayer var5 && this.active(var5)) {
            return new CastResult(Type.SUCCESS);
        }

        return super.canBeCastedBy(var1, var2, var3, var4);
    }

    public boolean ready(ServerPlayer var1, int var2, boolean var3) {
        if (this.active(var1)) {
            return true;
        } else {
            String var4 = null;
            float var5 = MagicData.getPlayerMagicData(var1).getMana();
            if ((double)var5 + 0.01
                < (this.kind == ArcanaSpell.Kind.PIERCE ? var1.getAttributeValue(AttributeRegistry.MAX_MANA) : (double)this.cost(var2, var1))) {
                var4 = this.kind == ArcanaSpell.Kind.PIERCE ? "full_mana" : "mana";
            }

            if (ExpandedMagic.held(var1)) {
                var4 = "bound";
            }

            if (this.kind == ArcanaSpell.Kind.FLIGHT && !ArcanaModes.hasStaff(var1)) {
                var4 = "staff";
            }

            if (this.kind == ArcanaSpell.Kind.BARRAGE && ArcanaModes.equipped(var1, ArcanaSpell.Kind.BARRAGE) == null) {
                var4 = "book";
            }

            if ((this.kind == ArcanaSpell.Kind.BIND || this.kind == ArcanaSpell.Kind.GOLD) && ExpandedMagic.target(var1, 24.0) == null) {
                var4 = "living_target";
            }

            if (this.kind == ArcanaSpell.Kind.HEAVY
                && var1.getAttributeValue(AttributeRegistry.MAX_MANA) < (double)ArcanaConfig.HEAVY_CAPACITY.get().intValue()) {
                var4 = "endgame";
            }

            if (this.kind == ArcanaSpell.Kind.WATER && !ExpandedMagic.nearWater(var1)) {
                var4 = "water";
            }

            if (this.kind == ArcanaSpell.Kind.PETALS && !AdvancedMagic.hasFlowers(var1)) {
                var4 = "flowers";
            }

            if (this.kind == ArcanaSpell.Kind.FIREWIND && !AdvancedMagic.hasWind(var1)) {
                var4 = "wind";
            }

            if (this.kind == ArcanaSpell.Kind.LIFT
                && (!MagicData.getPlayerMagicData(var1).isCasting() || !MagicData.getPlayerMagicData(var1).getCastingSpellId().equals(this.getSpellId()))
                && AdvancedMagic.liftTarget(var1, var2) == null) {
                var4 = "item_target";
            }

            if (this.kind == ArcanaSpell.Kind.HEAVY) {
                long var7;
                int var10000 = (var7 = ArcanaEvents.flags(var1).getLong("heavyReadyTick") - var1.server.overworld().getGameTime()) == 0L
                    ? 0
                    : (var7 < 0L ? -1 : 1);
                if (0 > 0) {
                    var4 = "cooldown";
                }
            }

            if (this.kind == ArcanaSpell.Kind.PIERCE) {
                if (ArcanaEvents.flags(var1).getLong("breakerReadyTick") > var1.server.overworld().getGameTime()) {
                    var4 = "cooldown";
                } else if (targetBarrier(var1) == null) {
                    var4 = "target";
                }
            }

            if (this.kind == ArcanaSpell.Kind.EXAM || this.kind == ArcanaSpell.Kind.DEFENSE) {
                String var6 = BarrierData.get(var1.serverLevel())
                    .validate(var1.serverLevel(), BlockPos.containing(ShipSpace.world(var1)), this.radius(var2), this.kind == ArcanaSpell.Kind.DEFENSE);
                if (var6 != null) {
                    var4 = var6;
                }
            }

            if (var4 != null && var3) {
                var1.displayClientMessage(Component.translatable("message.frieren_arcana.error." + var4), true);
            }

            return var4 == null;
        }
    }

    @Override
    public boolean checkPreCastConditions(Level var1, int var2, LivingEntity var3, MagicData var4) {
        if (var3 instanceof ServerPlayer var5 && this.ready(var5, var2, true)) {
            return this.kind != ArcanaSpell.Kind.LIFT || AdvancedMagic.prepareLift(var5, var2, var4);
        }

        return false;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return this.kind == ArcanaSpell.Kind.LIFT
            ? SpellRegistry.TELEKINESIS_SPELL.get().getCastStartAnimation()
            : (
                this.kind == ArcanaSpell.Kind.PIERCE
                    ? new AnimationHolder(FrierenArcana.id("barrier_breaker_charge"), true, true)
                    : super.getCastStartAnimation()
            );
    }

    @Override
    public void onCast(Level var1, int var2, LivingEntity var3, CastSource var4, MagicData var5) {
        if (var3 instanceof ServerPlayer var6) {
            CompoundTag var7 = ArcanaEvents.flags(var6);
            if (var7.getBoolean("castApproved")) {
                var7.remove("castApproved");
                switch (this.kind) {
                    case EXAM:
                    case DEFENSE:
                        if (this.active(var6)) {
                            BarrierData.releaseOwned(var6, this.kind == ArcanaSpell.Kind.DEFENSE);
                        } else {
                            BarrierData.get(var6.serverLevel()).create(var6.serverLevel(), var6, this.radius(var2), this.kind == ArcanaSpell.Kind.DEFENSE);
                        }
                        break;
                    case PIERCE:
                        BarrierData.Field var15 = targetBarrier(var6);
                        if (var15 == null) {
                            return;
                        }

                        var5.setMana(0.0F);
                        ArcanaEvents.syncMana(var6);
                        ArcanaCooldowns.begin(var6, this, var4);
                        Vec3 var9 = ShipSpace.world(var1, var6.getEyePosition());
                        Vec3 var10 = BreakerAim.direction(var6);
                        Vec3 var11 = var9.subtract(var15.center);
                        double var12 = BarrierGeometry.firstHit(
                            var11.x, var11.y, var11.z, var10.x * 128.0, var10.y * 128.0, var10.z * 128.0, (double)var15.radius
                        );
                        Vec3 var14 = var9.add(var10.scale(Double.isFinite(var12) ? 128.0 * var12 : 8.0));
                        ArcanaNetwork.beam(var6.serverLevel(), var9, var14, true);
                        ArcanaNetwork.shatter(var6.serverLevel(), var15, var14);
                        BarrierData.get(var6.serverLevel()).remove(var6.serverLevel(), var15.id, false);
                        var6.serverLevel().playSound(null, BlockPos.containing(var14), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 4.0F, 0.55F);
                        break;
                    case SIGHT:
                    case FLIGHT:
                    case CONCEAL:
                        boolean var8 = !var7.getBoolean(this.kind.path);
                        var7.putBoolean(this.kind.path, var8);
                        if (this.kind == ArcanaSpell.Kind.FLIGHT) {
                            ArcanaEvents.updateFlight(var6);
                        }

                        var6.displayClientMessage(
                            Component.translatable(var8 ? "message.frieren_arcana.enabled" : "message.frieren_arcana.disabled", this.getDisplayName(var6)),
                            true
                        );
                        ArcanaNetwork.syncSight(var6);
                        break;
                    case ZOLTRAAK:
                        this.fireZoltraak(var6, var2);
                        break;
                    case BARRAGE:
                        if (this.active(var6)) {
                            ArcanaModes.stopBarrage(var6);
                        } else {
                            ArcanaModes.startBarrage(var6, var2);
                        }
                        break;
                    case HEAVY:
                        ArcanaModes.fire(
                            var6,
                            this,
                            var2,
                            var6.getLookAngle(),
                            128.0,
                            2.0F,
                            (float)ArcanaConfig.HEAVY_DAMAGE.get().doubleValue() * this.getEntityPowerMultiplier(var6),
                            2,
                            true
                        );
                        String var10001 = "heavyReadyTick";
                        long var10002 = var6.server.overworld().getGameTime() + 1200L;
                        break;
                    case ICE:
                    case WATER:
                    case EARTH:
                    case SPEED:
                    case BIND:
                    case CUT:
                    case FLOWERS:
                    case HEAL:
                    case CLEANSE:
                    case GOLD:
                        ExpandedMagic.cast(this, var6, var2);
                        break;
                    case INFERNO:
                    case THUNDER:
                    case TORNADO:
                    case FIREWIND:
                    case JUDGMENT:
                    case PETALS:
                    case STONE:
                    case LIFT:
                    case SPEARS:
                        AdvancedMagic.cast(this, var6, var2);
                }

                super.onCast(var1, var2, var3, var4, var5);
            }
        }
    }

    public static BarrierData.Field targetBarrierLook(ServerPlayer var0) {
        Vec3 var1 = ShipSpace.world(var0.level(), var0.getEyePosition());
        Vec3 var2 = var1.add(var0.getLookAngle().scale(128.0));
        BarrierData var3 = BarrierData.get(var0.serverLevel());
        BarrierData.Field var4 = null;
        double var5 = Double.POSITIVE_INFINITY;
        Vec3 var7 = var2.subtract(var1);

        for (BarrierData.Field var9 : var3.fields()) {
            Vec3 var10 = var1.subtract(var9.center);
            double var11 = BarrierGeometry.firstHit(var10.x, var10.y, var10.z, var7.x, var7.y, var7.z, (double)var9.radius);
            if (var11 < var5) {
                var5 = var11;
                var4 = var9;
            }
        }

        if (var4 == null) {
            return null;
        } else {
            BlockHitResult var13 = var0.level().clip(new ClipContext(var1, var2, Block.COLLIDER, Fluid.NONE, var0));
            if (var13.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && var13 instanceof BlockHitResult var14
                && var13.getLocation().distanceTo(var1) + 2.0 < var5 * 128.0) {
                return null;
            }

            return var4;
        }
    }

    private void fireZoltraak(ServerPlayer var1, int var2) {
        ArcanaModes.stopBarrage(var1);
        ArcanaModes.fire(var1, this, var2, var1.getLookAngle(), 64.0, 0.0F, this.getSpellPower(var2, var1), 0, false);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int var1, LivingEntity var2) {
        ArrayList var3 = new ArrayList();
        var3.add(Component.translatable("spell.frieren_arcana." + this.kind.path + ".summary"));
        if (this.kind == ArcanaSpell.Kind.EXAM || this.kind == ArcanaSpell.Kind.DEFENSE) {
            var3.add(Component.translatable("ui.irons_spellbooks.radius", this.radius(var1)));
        }

        if (this.kind == ArcanaSpell.Kind.PIERCE) {
            var3.add(Component.translatable("info.frieren_arcana.breaker_cost"));
            var3.add(Component.translatable("info.frieren_arcana.breaker_skip"));
        } else {
            var3.add(Component.translatable("info.frieren_arcana.cost", this.getManaCost(var1)));
        }

        if (this.kind == ArcanaSpell.Kind.BARRAGE) {
            var3.add(Component.translatable("info.frieren_arcana.barrage_drain", ArcanaConfig.BARRAGE_DRAIN.get()));
        }

        if (this.kind == ArcanaSpell.Kind.HEAVY) {
            var3.add(Component.translatable("info.frieren_arcana.heavy", ArcanaConfig.HEAVY_CAPACITY.get(), ArcanaConfig.HEAVY_CHARGE.get()));
        }

        return var3;
    }

    public static BarrierData.Field targetBarrier(ServerPlayer var0) {
        return BreakerAim.target(var0);
    }

    public static enum Kind {
        EXAM("examination_barrier", 120, 5, 30, SpellRarity.EPIC),
        DEFENSE("defensive_barrier", 60, 5, 10, SpellRarity.UNCOMMON),
        PIERCE("barrier_breaker", 0, 1, 90, SpellRarity.LEGENDARY),
        SIGHT("mana_sight", 20, 1, 2, SpellRarity.RARE),
        ZOLTRAAK("zoltraak", 25, 5, 3, SpellRarity.UNCOMMON),
        FLIGHT("flight", 30, 1, 2, SpellRarity.RARE),
        CONCEAL("mana_concealment", 20, 1, 2, SpellRarity.RARE),
        BARRAGE("zoltraak_barrage", 25, 5, 3, SpellRarity.RARE),
        HEAVY("zoltraak_heavy", 450, 1, 60, SpellRarity.LEGENDARY),
        ICE("nephtear", 35, 5, 5, SpellRarity.UNCOMMON),
        WATER("reamstroha", 45, 5, 8, SpellRarity.RARE),
        EARTH("balgrant", 50, 5, 10, SpellRarity.RARE),
        SPEED("jilwer", 30, 3, 8, SpellRarity.UNCOMMON),
        BIND("sorganeil", 40, 3, 12, SpellRarity.RARE),
        CUT("reelseiden", 60, 5, 6, SpellRarity.EPIC),
        FLOWERS("flower_field", 10, 3, 3, SpellRarity.COMMON),
        HEAL("goddess_healing", 50, 3, 15, SpellRarity.RARE),
        CLEANSE("goddess_cleansing", 40, 3, 12, SpellRarity.RARE),
        GOLD("golden_transmutation", 350, 1, 60, SpellRarity.LEGENDARY),
        INFERNO("vollzanbel", 120, 3, 15, SpellRarity.EPIC),
        THUNDER("judradjim", 130, 3, 18, SpellRarity.EPIC),
        TORNADO("waldgose", 60, 5, 10, SpellRarity.RARE),
        FIREWIND("daosdorg", 95, 5, 14, SpellRarity.EPIC),
        JUDGMENT("catastrovia", 90, 5, 10, SpellRarity.EPIC),
        PETALS("jubelade", 45, 5, 7, SpellRarity.RARE),
        STONE("dragate", 40, 5, 5, SpellRarity.UNCOMMON),
        LIFT("object_levitation", 25, 3, 4, SpellRarity.UNCOMMON),
        SPEARS("goddess_three_spears", 50, 3, 7, SpellRarity.RARE);

        public final String path;
        public final int cost;
        public final int levels;
        public final int cooldown;
        public final SpellRarity rarity;

        private Kind(String path, int cost, int levels, int cooldown, SpellRarity rarity) {
            this.path = path;
            this.cost = cost;
            this.levels = levels;
            this.cooldown = cooldown;
            this.rarity = rarity;
        }
    }
}
