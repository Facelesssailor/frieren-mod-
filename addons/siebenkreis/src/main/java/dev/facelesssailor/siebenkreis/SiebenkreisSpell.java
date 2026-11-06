package dev.facelesssailor.siebenkreis;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Siebenkreis — Spell of the Seven Heavens.
 * <p>
 * A long cast that stacks spell circles over the target area one slow layer at a time.
 * Every finished layer adds an effect. On completion the stack aligns, collapses and
 * fires a pillar of light; if the cast is interrupted it fires with the layers built so far.
 */
public class SiebenkreisSpell extends AbstractSpell {
    /** Wind-up before the first circle forms (ticks). */
    public static final int LEAD_TICKS = 20;
    /** Time each layer takes to form (ticks) — the "longer sequence" pacing: 1.4 s per layer. */
    public static final int LAYER_TICKS = 28;
    public static final int MAX_LAYERS = 7;
    public static final double RANGE = 40.0;

    private static final Map<UUID, SiebenkreisEntity> ACTIVE = new HashMap<>();

    private final ResourceLocation spellId = SiebenkreisMod.id("siebenkreis");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(45)
            .build();

    public SiebenkreisSpell() {
        this.baseManaCost = 120;
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 24;
        this.spellPowerPerLevel = 4;
        this.castTime = LEAD_TICKS + MAX_LAYERS * LAYER_TICKS;
    }

    public static int layersFor(int spellLevel) {
        return Math.max(1, Math.min(MAX_LAYERS, 3 + spellLevel));
    }

    public static float radiusFor(int spellLevel) {
        return 6.0f + spellLevel;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public int getCastTime(int spellLevel) {
        return LEAD_TICKS + layersFor(spellLevel) * LAYER_TICKS;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float power = getSpellPower(spellLevel, caster);
        int layers = layersFor(spellLevel);
        float full = SiebenkreisEntity.strikeDamage(power, layers);
        return List.of(
                Component.translatable("ui.siebenkreis.layers", layers, MAX_LAYERS),
                Component.translatable("ui.irons_spellbooks.damage", String.format("%.1f", full)),
                Component.translatable("ui.irons_spellbooks.radius", String.format("%.1f", radiusFor(spellLevel)))
        );
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BEACON_ACTIVATE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BEACON_POWER_SELECT);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerPreCast(level, spellLevel, entity, playerMagicData);
        if (level.isClientSide) return;
        discardActive(entity);

        int duration = playerMagicData != null && playerMagicData.getCastDuration() > 0
                ? playerMagicData.getCastDuration()
                : getEffectiveCastTime(spellLevel, entity);
        SiebenkreisEntity circle = SiebenkreisEntity.create(level, entity, findTarget(level, entity),
                spellLevel, layersFor(spellLevel), radiusFor(spellLevel), duration);
        level.addFreshEntity(circle);
        ACTIVE.put(entity.getUUID(), circle);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            SiebenkreisEntity circle = ACTIVE.remove(entity.getUUID());
            if (circle == null || !circle.isAlive() || circle.isReleased()) {
                // No charge phase happened (e.g. cast from a source that skips the wind-up): open it instantly.
                circle = SiebenkreisEntity.create(level, entity, findTarget(level, entity),
                        spellLevel, layersFor(spellLevel), radiusFor(spellLevel), 1);
                level.addFreshEntity(circle);
            }
            circle.release(circle.getMaxLayers());
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        SiebenkreisEntity circle = ACTIVE.remove(entity.getUUID());
        if (circle != null && circle.isAlive() && !circle.isReleased()) {
            // Interrupted: fire with whatever layers have finished forming.
            circle.release(circle.getLayersBuilt());
        }
        super.onServerCastComplete(level, spellLevel, entity, playerMagicData, cancelled);
    }

    private static void discardActive(LivingEntity caster) {
        SiebenkreisEntity old = ACTIVE.remove(caster.getUUID());
        if (old != null && old.isAlive() && !old.isReleased()) old.discard();
    }

    /** Ground point under the caster's crosshair, up to {@link #RANGE} blocks away. */
    private static Vec3 findTarget(Level level, LivingEntity caster) {
        HitResult hit = caster.pick(RANGE, 1.0f, false);
        Vec3 point = hit.getLocation();
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
            point = Vec3.atBottomCenterOf(bhr.getBlockPos().relative(bhr.getDirection()));
            if (bhr.getDirection() != net.minecraft.core.Direction.UP) {
                point = dropToGround(level, point);
            }
        } else {
            point = dropToGround(level, point);
        }
        return point;
    }

    private static Vec3 dropToGround(Level level, Vec3 start) {
        net.minecraft.core.BlockPos.MutableBlockPos pos = net.minecraft.core.BlockPos.containing(start).mutable();
        for (int i = 0; i < 24 && pos.getY() > level.getMinBuildHeight(); i++) {
            BlockState below = level.getBlockState(pos.below());
            if (!below.getCollisionShape(level, pos.below()).isEmpty()) break;
            pos.move(0, -1, 0);
        }
        return new Vec3(start.x, pos.getY(), start.z);
    }
}
