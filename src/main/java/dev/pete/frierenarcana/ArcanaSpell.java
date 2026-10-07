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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ArcanaSpell extends AbstractSpell {
    public final ArcanaSpell.Kind kind;

    public ArcanaSpell(ArcanaSpell.Kind kind) {
        this.kind = kind;
        this.baseManaCost = kind.cost;
        this.manaCostPerLevel = kind.levels > 1 && kind != ArcanaSpell.Kind.LIFT ? 10 : 0;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = kind == ArcanaSpell.Kind.LIFT
            ? 140
            : (
                kind == ArcanaSpell.Kind.PIERCE
                    ? 100
                    : (
                        kind == ArcanaSpell.Kind.HEAVY
                            ? 160
                            : (
                                kind == ArcanaSpell.Kind.GOLD
                                    ? 100
                                    : (
                                        kind != ArcanaSpell.Kind.EXAM && kind != ArcanaSpell.Kind.INFERNO
                                            ? (kind == ArcanaSpell.Kind.THUNDER ? 30 : (kind == ArcanaSpell.Kind.JUDGMENT ? 20 : 0))
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
    public int getCastTime(int level) {
        return this.kind == ArcanaSpell.Kind.LIFT ? 140 + 20 * (level - 1) : super.getCastTime(level);
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
    public int getManaCost(int level) {
        return this.kind == ArcanaSpell.Kind.HEAVY ? ArcanaConfig.HEAVY_COST.get() : super.getManaCost(level);
    }

    @Override
    public int getEffectiveCastTime(int level, LivingEntity caster) {
        return this.kind == ArcanaSpell.Kind.HEAVY
            ? Math.max(ArcanaConfig.HEAVY_CHARGE.get() * 20, super.getEffectiveCastTime(level, caster))
            : super.getEffectiveCastTime(level, caster);
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return this.kind == ArcanaSpell.Kind.PIERCE
            ? new AnimationHolder(FrierenArcana.id("barrier_breaker_release"), true, true)
            : super.getCastFinishAnimation();
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData data) {
        if (this.kind == ArcanaSpell.Kind.PIERCE && entity instanceof ServerPlayer p) {
            CompoundTag f = ArcanaEvents.flags(p);
            f.putBoolean("hovering", true);
            f.putBoolean("hoverOldGravity", p.isNoGravity());
            p.setNoGravity(true);
            ArcanaNetwork.charge(p, true);
        }

        if (this.kind == ArcanaSpell.Kind.HEAVY && entity instanceof ServerPlayer p) {
            ArcanaModes.stopBarrage(p);
            ArcanaNetwork.charge(p, true);
        }

        super.onServerPreCast(level, spellLevel, entity, data);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData data) {
        if (this.kind == ArcanaSpell.Kind.PIERCE && entity instanceof ServerPlayer p) {
            p.setDeltaMovement(0.0, 0.015, 0.0);
            p.hurtMarked = true;
            p.fallDistance = 0.0F;
        }

        if (this.kind == ArcanaSpell.Kind.LIFT && entity instanceof ServerPlayer p) {
            AdvancedMagic.liftTick(p, spellLevel, data);
        }
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData data, boolean cancelled) {
        if (this.kind == ArcanaSpell.Kind.PIERCE && entity instanceof ServerPlayer p) {
            ArcanaEvents.endHover(p);
        }

        if (this.kind == ArcanaSpell.Kind.HEAVY && entity instanceof ServerPlayer p) {
            ArcanaNetwork.charge(p, false);
        }

        if (this.kind == ArcanaSpell.Kind.LIFT && entity instanceof ServerPlayer p) {
            AdvancedMagic.releaseLift(p);
        }

        super.onServerCastComplete(level, spellLevel, entity, data, cancelled);
    }

    @Override
    public int getSpellCooldown() {
        return this.kind == ArcanaSpell.Kind.PIERCE ? ArcanaConfig.PIERCE_COOLDOWN.get() * 20 : super.getSpellCooldown();
    }

    public int radius(int level) {
        return this.kind == ArcanaSpell.Kind.EXAM
            ? Math.min(56, ArcanaConfig.EXAM_RADIUS.get() + 2 * (level - 1))
            : ArcanaConfig.DEFENSE_RADIUS.get() + level - 1;
    }

    public boolean active(ServerPlayer p) {
        return switch (this.kind) {
            case EXAM, DEFENSE -> BarrierData.owns(p, this.kind == ArcanaSpell.Kind.DEFENSE);
            default -> false;
            case SIGHT, FLIGHT, CONCEAL -> ArcanaEvents.flags(p).getBoolean(this.kind.path);
            case BARRAGE -> ArcanaEvents.flags(p).getBoolean("barrage");
        };
    }

    public int cost(int level, ServerPlayer p) {
        if (this.active(p)) {
            return 0;
        } else {
            return this.kind == ArcanaSpell.Kind.PIERCE ? (int)Math.ceil(p.getAttributeValue(AttributeRegistry.MAX_MANA)) : this.getManaCost(level);
        }
    }

    @Override
    public CastResult canBeCastedBy(int level, CastSource source, MagicData magic, Player p) {
        if (p instanceof ServerPlayer sp && this.active(sp)) {
            return new CastResult(Type.SUCCESS);
        }

        return super.canBeCastedBy(level, source, magic, p);
    }

    public boolean ready(ServerPlayer p, int level, boolean feedback) {
        if (this.active(p)) {
            return true;
        } else {
            String error = null;
            float mana = MagicData.getPlayerMagicData(p).getMana();
            if ((double)mana + 0.01 < (this.kind == ArcanaSpell.Kind.PIERCE ? p.getAttributeValue(AttributeRegistry.MAX_MANA) : (double)this.cost(level, p))) {
                error = this.kind == ArcanaSpell.Kind.PIERCE ? "full_mana" : "mana";
            }

            if (ExpandedMagic.held(p)) {
                error = "bound";
            }

            if (this.kind == ArcanaSpell.Kind.FLIGHT && !ArcanaModes.hasStaff(p)) {
                error = "staff";
            }

            if (this.kind == ArcanaSpell.Kind.BARRAGE && ArcanaModes.equipped(p, ArcanaSpell.Kind.BARRAGE) == null) {
                error = "book";
            }

            if ((this.kind == ArcanaSpell.Kind.BIND || this.kind == ArcanaSpell.Kind.GOLD) && ExpandedMagic.target(p, 24.0) == null) {
                error = "living_target";
            }

            if (this.kind == ArcanaSpell.Kind.HEAVY && p.getAttributeValue(AttributeRegistry.MAX_MANA) < (double)ArcanaConfig.HEAVY_CAPACITY.get().intValue()) {
                error = "endgame";
            }

            if (this.kind == ArcanaSpell.Kind.WATER && !ExpandedMagic.nearWater(p)) {
                error = "water";
            }

            if (this.kind == ArcanaSpell.Kind.PETALS && !AdvancedMagic.hasFlowers(p)) {
                error = "flowers";
            }

            if (this.kind == ArcanaSpell.Kind.FIREWIND && !AdvancedMagic.hasWind(p)) {
                error = "wind";
            }

            if (this.kind == ArcanaSpell.Kind.LIFT
                && (!MagicData.getPlayerMagicData(p).isCasting() || !MagicData.getPlayerMagicData(p).getCastingSpellId().equals(this.getSpellId()))
                && AdvancedMagic.liftTarget(p, level) == null) {
                error = "item_target";
            }

            if (this.kind == ArcanaSpell.Kind.HEAVY && ArcanaEvents.flags(p).getLong("heavyReadyTick") > p.server.overworld().getGameTime()) {
                error = "cooldown";
            }

            if (this.kind == ArcanaSpell.Kind.PIERCE) {
                if (ArcanaEvents.flags(p).getLong("breakerReadyTick") > p.server.overworld().getGameTime()) {
                    error = "cooldown";
                } else if (targetBarrier(p) == null) {
                    error = "target";
                }
            }

            if (this.kind == ArcanaSpell.Kind.EXAM || this.kind == ArcanaSpell.Kind.DEFENSE) {
                String barrierError = BarrierData.get(p.serverLevel())
                    .validate(p.serverLevel(), BlockPos.containing(ShipSpace.world(p)), this.radius(level), this.kind == ArcanaSpell.Kind.DEFENSE);
                if (barrierError != null) {
                    error = barrierError;
                }
            }

            if (error != null && feedback) {
                p.displayClientMessage(Component.translatable("message.frieren_arcana.error." + error), true);
            }

            return error == null;
        }
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData magic) {
        if (entity instanceof ServerPlayer p && this.ready(p, spellLevel, true)) {
            return this.kind != ArcanaSpell.Kind.LIFT || AdvancedMagic.prepareLift(p, spellLevel, magic);
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource source, MagicData magic) {
        if (entity instanceof ServerPlayer p) {
            CompoundTag flags = ArcanaEvents.flags(p);
            if (flags.getBoolean("castApproved")) {
                flags.remove("castApproved");
                switch (this.kind) {
                    case EXAM:
                    case DEFENSE:
                        if (this.active(p)) {
                            BarrierData.releaseOwned(p, this.kind == ArcanaSpell.Kind.DEFENSE);
                        } else {
                            BarrierData.get(p.serverLevel()).create(p.serverLevel(), p, this.radius(spellLevel), this.kind == ArcanaSpell.Kind.DEFENSE);
                        }
                        break;
                    case PIERCE:
                        BarrierData.Field field = targetBarrier(p);
                        if (field == null) {
                            return;
                        }

                        magic.setMana(0.0F);
                        ArcanaEvents.syncMana(p);
                        ArcanaCooldowns.begin(p, this, source);
                        Vec3 start = ShipSpace.world(level, p.getEyePosition());
                        Vec3 direction = p.getLookAngle();
                        Vec3 relative = start.subtract(field.center);
                        double t = BarrierGeometry.firstHit(
                            relative.x, relative.y, relative.z, direction.x * 128.0, direction.y * 128.0, direction.z * 128.0, (double)field.radius
                        );
                        Vec3 impact = start.add(direction.scale(Double.isFinite(t) ? 128.0 * t : 8.0));
                        ArcanaNetwork.beam(p.serverLevel(), start, impact, true);
                        ArcanaNetwork.shatter(p.serverLevel(), field, impact);
                        BarrierData.get(p.serverLevel()).remove(p.serverLevel(), field.id, false);
                        p.serverLevel().playSound(null, BlockPos.containing(impact), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 4.0F, 0.55F);
                        break;
                    case SIGHT:
                    case FLIGHT:
                    case CONCEAL:
                        boolean enabled = !flags.getBoolean(this.kind.path);
                        flags.putBoolean(this.kind.path, enabled);
                        if (this.kind == ArcanaSpell.Kind.FLIGHT) {
                            ArcanaEvents.updateFlight(p);
                        }

                        p.displayClientMessage(
                            Component.translatable(enabled ? "message.frieren_arcana.enabled" : "message.frieren_arcana.disabled", this.getDisplayName(p)),
                            true
                        );
                        ArcanaNetwork.syncSight(p);
                        break;
                    case ZOLTRAAK:
                        this.fireZoltraak(p, spellLevel);
                        break;
                    case BARRAGE:
                        if (this.active(p)) {
                            ArcanaModes.stopBarrage(p);
                        } else {
                            ArcanaModes.startBarrage(p, spellLevel);
                        }
                        break;
                    case HEAVY:
                        ArcanaModes.fire(
                            p,
                            this,
                            spellLevel,
                            p.getLookAngle(),
                            128.0,
                            2.0F,
                            (float)ArcanaConfig.HEAVY_DAMAGE.get().doubleValue() * this.getEntityPowerMultiplier(p),
                            2,
                            true
                        );
                        flags.putLong("heavyReadyTick", p.server.overworld().getGameTime() + 1200L);
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
                        ExpandedMagic.cast(this, p, spellLevel);
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
                        AdvancedMagic.cast(this, p, spellLevel);
                }

                super.onCast(level, spellLevel, entity, source, magic);
            }
        }
    }

    public static BarrierData.Field targetBarrier(ServerPlayer p) {
        Vec3 start = ShipSpace.world(p.level(), p.getEyePosition());
        Vec3 end = start.add(p.getLookAngle().scale(128.0));
        BarrierData data = BarrierData.get(p.serverLevel());
        BarrierData.Field best = null;
        double bestT = Double.POSITIVE_INFINITY;
        Vec3 delta = end.subtract(start);

        for (BarrierData.Field f : data.fields()) {
            Vec3 r = start.subtract(f.center);
            double t = BarrierGeometry.firstHit(r.x, r.y, r.z, delta.x, delta.y, delta.z, (double)f.radius);
            if (t < bestT) {
                bestT = t;
                best = f;
            }
        }

        if (best == null) {
            return null;
        } else {
            HitResult solid = p.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, p));
            if (solid.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && solid instanceof BlockHitResult block
                && solid.getLocation().distanceTo(start) + 2.0 < bestT * 128.0) {
                return null;
            }

            return best;
        }
    }

    private void fireZoltraak(ServerPlayer p, int level) {
        ArcanaModes.stopBarrage(p);
        ArcanaModes.fire(p, this, level, p.getLookAngle(), 64.0, 0.0F, this.getSpellPower(level, p), 0, false);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        List<MutableComponent> info = new ArrayList<>();
        info.add(Component.translatable("spell.frieren_arcana." + this.kind.path + ".summary"));
        if (this.kind == ArcanaSpell.Kind.EXAM || this.kind == ArcanaSpell.Kind.DEFENSE) {
            info.add(Component.translatable("ui.irons_spellbooks.radius", this.radius(level)));
        }

        if (this.kind == ArcanaSpell.Kind.PIERCE) {
            info.add(Component.translatable("info.frieren_arcana.breaker_cost"));
            info.add(Component.translatable("info.frieren_arcana.breaker_skip"));
        } else {
            info.add(Component.translatable("info.frieren_arcana.cost", this.getManaCost(level)));
        }

        if (this.kind == ArcanaSpell.Kind.BARRAGE) {
            info.add(Component.translatable("info.frieren_arcana.barrage_drain", ArcanaConfig.BARRAGE_DRAIN.get()));
        }

        if (this.kind == ArcanaSpell.Kind.HEAVY) {
            info.add(Component.translatable("info.frieren_arcana.heavy", ArcanaConfig.HEAVY_CAPACITY.get(), ArcanaConfig.HEAVY_CHARGE.get()));
        }

        return info;
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
        CUT("reelseiden", 45, 5, 5, SpellRarity.EPIC),
        FLOWERS("flower_field", 10, 3, 3, SpellRarity.COMMON),
        HEAL("goddess_healing", 50, 3, 15, SpellRarity.RARE),
        CLEANSE("goddess_cleansing", 40, 3, 12, SpellRarity.RARE),
        GOLD("golden_transmutation", 350, 1, 60, SpellRarity.LEGENDARY),
        INFERNO("vollzanbel", 120, 3, 15, SpellRarity.EPIC),
        THUNDER("judradjim", 100, 3, 18, SpellRarity.EPIC),
        TORNADO("waldgose", 60, 5, 10, SpellRarity.RARE),
        FIREWIND("daosdorg", 80, 5, 12, SpellRarity.EPIC),
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
