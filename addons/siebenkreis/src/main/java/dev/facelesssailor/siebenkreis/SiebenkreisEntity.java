package dev.facelesssailor.siebenkreis;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The spell circle tower. Server side it runs the timeline and applies effects;
 * client side it records when each layer and phase began so the renderer can animate them.
 */
public class SiebenkreisEntity extends Entity {
    public static final int PHASE_CHARGE = 0;
    public static final int PHASE_ALIGN = 1;
    public static final int PHASE_COLLAPSE = 2;
    public static final int PHASE_PILLAR = 3;
    public static final int PHASE_SCAR = 4;

    // Release sequence lengths (ticks). Each is deliberately long so every stage reads on screen.
    public static final int ALIGN_TICKS = 40;          // circles spin up and lock runes
    public static final int COLLAPSE_TICKS = 34;       // full stack telescopes down
    public static final int COLLAPSE_PARTIAL_TICKS = 22;
    public static final int PILLAR_TICKS = 60;         // pillar of light
    public static final int ECHO_DELAY = 22;           // second hit (layer 5)
    public static final int SCAR_TICKS = 120;          // rune scar fades from the ground

    private static final EntityDataAccessor<Integer> DATA_MAX =
            SynchedEntityData.defineId(SiebenkreisEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BUILT =
            SynchedEntityData.defineId(SiebenkreisEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(SiebenkreisEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(SiebenkreisEntity.class, EntityDataSerializers.FLOAT);

    // server state
    @Nullable private UUID ownerId;
    @Nullable private LivingEntity ownerCache;
    private int spellLevel = 1;
    private int chargeDuration = 200;
    private int phaseAge;
    private boolean released;

    // client state (tickCount when each layer / the current phase appeared)
    public final int[] layerBornTick = new int[SiebenkreisSpell.MAX_LAYERS];
    public int clientPhaseStart;

    public SiebenkreisEntity(EntityType<? extends SiebenkreisEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        Arrays.fill(layerBornTick, -1);
    }

    public static SiebenkreisEntity create(Level level, LivingEntity owner, Vec3 pos, int spellLevel,
                                           int maxLayers, float radius, int chargeDuration) {
        SiebenkreisEntity e = new SiebenkreisEntity(SiebenkreisMod.CIRCLE.get(), level);
        e.setPos(pos.x, pos.y, pos.z);
        e.ownerId = owner.getUUID();
        e.ownerCache = owner;
        e.spellLevel = spellLevel;
        e.chargeDuration = Math.max(1, chargeDuration);
        e.entityData.set(DATA_MAX, Mth.clamp(maxLayers, 1, SiebenkreisSpell.MAX_LAYERS));
        e.entityData.set(DATA_RADIUS, radius);
        return e;
    }

    /** Damage of the main strike for a given spell power and layer count. */
    public static float strikeDamage(float power, int layers) {
        float dmg = power * (0.3f + 0.7f * layers / (float) SiebenkreisSpell.MAX_LAYERS);
        if (layers >= 4) dmg *= 1.3f; // Lens
        return dmg;
    }

    // ---------------------------------------------------------------- data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_MAX, SiebenkreisSpell.MAX_LAYERS);
        builder.define(DATA_BUILT, 0);
        builder.define(DATA_PHASE, PHASE_CHARGE);
        builder.define(DATA_RADIUS, 8.0f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (!level().isClientSide) return;
        if (DATA_BUILT.equals(key)) {
            int n = getLayersBuilt();
            for (int i = 0; i < n && i < layerBornTick.length; i++) {
                if (layerBornTick[i] < 0) layerBornTick[i] = tickCount;
            }
        } else if (DATA_PHASE.equals(key)) {
            clientPhaseStart = tickCount;
        }
    }

    public int getMaxLayers() { return entityData.get(DATA_MAX); }
    public int getLayersBuilt() { return entityData.get(DATA_BUILT); }
    public int getPhase() { return entityData.get(DATA_PHASE); }
    public float getRadius() { return entityData.get(DATA_RADIUS); }
    public boolean isReleased() { return released; }
    public boolean isFullRelease() { return getLayersBuilt() >= getMaxLayers(); }

    public int collapseTicks() {
        return isFullRelease() ? COLLAPSE_TICKS : COLLAPSE_PARTIAL_TICKS;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // never persisted (type is noSave)
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSqr) {
        return distSqr < 192.0 * 192.0;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerCache != null && !ownerCache.isRemoved()) return ownerCache;
        if (ownerId != null && level() instanceof ServerLevel sl && sl.getEntity(ownerId) instanceof LivingEntity le) {
            ownerCache = le;
            return le;
        }
        return null;
    }

    // ---------------------------------------------------------------- timeline

    /** Tick (from charge start) at which layer {@code index} (0-based) finishes forming. */
    private int layerTime(int index) {
        int max = getMaxLayers();
        float total = SiebenkreisSpell.LEAD_TICKS + max * SiebenkreisSpell.LAYER_TICKS;
        float at = SiebenkreisSpell.LEAD_TICKS + index * SiebenkreisSpell.LAYER_TICKS;
        return Math.round(chargeDuration * at / total);
    }

    /** Stop charging and fire with {@code layers} layers. */
    public void release(int layers) {
        if (released || level().isClientSide) return;
        released = true;
        int n = Mth.clamp(layers, 0, getMaxLayers());
        if (n <= 0) {
            fizzle();
            return;
        }
        // Layers that finished forming late still count on a full release.
        while (getLayersBuilt() < n) {
            entityData.set(DATA_BUILT, getLayersBuilt() + 1);
            onLayerFormed(getLayersBuilt() - 1);
        }
        entityData.set(DATA_BUILT, n);
        setPhase(n >= getMaxLayers() ? PHASE_ALIGN : PHASE_COLLAPSE);
    }

    private void setPhase(int phase) {
        entityData.set(DATA_PHASE, phase);
        phaseAge = 0;
        switch (phase) {
            case PHASE_ALIGN -> sound(SoundEvents.BEACON_AMBIENT, 4.0f, 0.6f);
            case PHASE_COLLAPSE -> sound(SoundEvents.BEACON_DEACTIVATE, 4.0f, 0.5f);
            default -> { }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        phaseAge++;
        int phase = getPhase();
        switch (phase) {
            case PHASE_CHARGE -> tickCharge();
            case PHASE_ALIGN -> {
                if (phaseAge % 6 == 0) ringParticles(ParticleTypes.END_ROD, getLayersBuilt() - 1, 14);
                if (phaseAge >= ALIGN_TICKS) setPhase(PHASE_COLLAPSE);
            }
            case PHASE_COLLAPSE -> {
                if (phaseAge >= collapseTicks()) {
                    setPhase(PHASE_PILLAR);
                    strike(1.0f, true);
                }
            }
            case PHASE_PILLAR -> {
                if (getLayersBuilt() >= 5 && phaseAge == ECHO_DELAY) strike(0.6f, false);
                if (phaseAge % 3 == 0) pillarParticles();
                if (phaseAge >= PILLAR_TICKS) setPhase(PHASE_SCAR);
            }
            case PHASE_SCAR -> {
                if (phaseAge % 10 == 0) ringParticles(ParticleTypes.SMALL_FLAME, 0, 10);
                if (phaseAge >= SCAR_TICKS) discard();
            }
            default -> discard();
        }
    }

    private void tickCharge() {
        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive() || phaseAge > chargeDuration + 100) {
            release(getLayersBuilt());
            return;
        }
        int built = getLayersBuilt();
        while (built < getMaxLayers() && phaseAge >= layerTime(built)) {
            built++;
            entityData.set(DATA_BUILT, built);
            onLayerFormed(built - 1);
        }

        // Layer 1 — Anchor: slows enemies standing in the circle.
        if (built >= 1 && phaseAge % 10 == 0) {
            for (LivingEntity t : targets(getRadius())) {
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, true));
            }
        }
        // Layer 2 — Binding: drags them toward the center.
        if (built >= 2) {
            for (LivingEntity t : targets(getRadius() * 1.15f)) {
                Vec3 to = position().subtract(t.position());
                Vec3 flat = new Vec3(to.x, 0, to.z);
                if (flat.lengthSqr() > 1.0) {
                    t.setDeltaMovement(t.getDeltaMovement().add(flat.normalize().scale(0.045)));
                    t.hurtMarked = true;
                }
            }
        }
        if (built >= 1 && phaseAge % 4 == 0) ringParticles(ParticleTypes.END_ROD, random.nextInt(built), 3);
    }

    private void onLayerFormed(int index) {
        sound(SoundEvents.BEACON_POWER_SELECT, 3.0f, 0.55f + index * 0.12f);
        sound(SoundEvents.AMETHYST_BLOCK_CHIME, 3.0f, 0.5f + index * 0.1f);
        ringParticles(ParticleTypes.END_ROD, index, 40);
        // Layer 6 — Ward: the caster gains a barrier while channeling.
        if (index == 5) {
            LivingEntity owner = getOwner();
            if (owner != null) owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 2, false, true, true));
        }
    }

    private void fizzle() {
        sound(SoundEvents.BEACON_DEACTIVATE, 2.0f, 1.4f);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.2, getZ(), 30, getRadius() * 0.4, 0.1, getRadius() * 0.4, 0.01);
        }
        discard();
    }

    // ---------------------------------------------------------------- effects

    private void strike(float multiplier, boolean primary) {
        LivingEntity owner = getOwner();
        int n = getLayersBuilt();
        float radius = getRadius();
        float power = SiebenkreisMod.SIEBENKREIS.get().getSpellPower(spellLevel, owner);
        float damage = strikeDamage(power, n) * multiplier;
        DamageSource source = SiebenkreisMod.SIEBENKREIS.get().getDamageSource(this, owner);

        for (LivingEntity t : targets(radius)) {
            t.hurt(source, damage);
            // Layer 3 — Severance: strips beneficial effects.
            if (n >= 3) {
                List<Holder<MobEffect>> remove = new ArrayList<>();
                for (MobEffectInstance inst : t.getActiveEffects()) {
                    if (inst.getEffect().value().isBeneficial()) remove.add(inst.getEffect());
                }
                for (Holder<MobEffect> h : remove) t.removeEffect(h);
            }
        }

        if (primary && n >= 7) {
            // Layer 7 — Crown: shockwave beyond the circle.
            for (LivingEntity t : targets(radius * 1.8f)) {
                Vec3 away = t.position().subtract(position());
                Vec3 flat = new Vec3(away.x, 0, away.z);
                double dist = flat.length();
                if (dist > radius) t.hurt(source, damage * 0.4f);
                Vec3 push = (dist < 0.01 ? new Vec3(1, 0, 0) : flat.normalize()).scale(1.4).add(0, 0.5, 0);
                t.setDeltaMovement(t.getDeltaMovement().add(push));
                t.hurtMarked = true;
            }
        }

        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1, getZ(), 3, 0.5, 0.5, 0.5, 0);
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1, getZ(), 120, radius * 0.35, 6, radius * 0.35, 0.25);
            if (primary && n >= 7) {
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 1, 0, 0, 0, 0);
            } else {
                sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 6, radius * 0.3, 0.5, radius * 0.3, 0);
            }
        }
        sound(SoundEvents.WARDEN_SONIC_BOOM, 6.0f, primary ? 0.6f : 0.9f);
        if (primary) sound(SoundEvents.BEACON_ACTIVATE, 6.0f, 1.6f);
    }

    private List<LivingEntity> targets(float radius) {
        LivingEntity owner = getOwner();
        AABB box = new AABB(getX() - radius, getY() - 2, getZ() - radius, getX() + radius, getY() + 14, getZ() + radius);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity t : level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (!t.isAlive() || t.isSpectator() || t instanceof ArmorStand) continue;
            if (owner != null) {
                if (t == owner || t.isAlliedTo(owner) || owner.isAlliedTo(t)) continue;
                if (t instanceof TamableAnimal pet && pet.isOwnedBy(owner)) continue;
            }
            double dx = t.getX() - getX(), dz = t.getZ() - getZ();
            if (dx * dx + dz * dz <= radius * radius) out.add(t);
        }
        return out;
    }

    // ---------------------------------------------------------------- fx helpers

    /** Height of layer {@code i} above the ground circle; shared with the renderer. */
    public static float layerHeight(int i) {
        return i == 0 ? 0.06f : 1.8f + i * 3.1f;
    }

    /** Radius of layer {@code i}; each layer above is wider than the one below. */
    public static float layerRadius(int i, float base) {
        return base * (0.78f + 0.1f * i);
    }

    private void ringParticles(ParticleOptions type, int layer, int count) {
        if (!(level() instanceof ServerLevel sl) || layer < 0) return;
        float r = layerRadius(layer, getRadius());
        float y = layerHeight(layer);
        for (int k = 0; k < count; k++) {
            double a = random.nextDouble() * Math.PI * 2;
            sl.sendParticles(type, getX() + Math.cos(a) * r, getY() + y + 0.1, getZ() + Math.sin(a) * r,
                    1, 0, 0.02, 0, 0.01);
        }
    }

    private void pillarParticles() {
        if (!(level() instanceof ServerLevel sl)) return;
        float r = getRadius() * 0.3f;
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 4, getZ(), 12, r, 4, r, 0.08);
    }

    private void sound(SoundEvent event, float volume, float pitch) {
        level().playSound(null, getX(), getY() + 2, getZ(), event, SoundSource.PLAYERS, volume, pitch);
    }
}
