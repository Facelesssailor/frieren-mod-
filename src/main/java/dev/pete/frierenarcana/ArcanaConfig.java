package dev.pete.frierenarcana;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class ArcanaConfig {
    public static final ModConfigSpec SPEC;
    public static final IntValue EXAM_RADIUS;
    public static final IntValue DEFENSE_RADIUS;
    public static final IntValue PIERCE_COOLDOWN;
    public static final IntValue MAX_FIELDS;
    public static final IntValue HEAVY_CAPACITY;
    public static final IntValue HEAVY_COST;
    public static final IntValue HEAVY_CHARGE;
    public static final IntValue DEVICE_RADIUS;
    public static final DoubleValue FLIGHT_DRAIN;
    public static final DoubleValue SIGHT_RANGE;
    public static final DoubleValue CONCEAL_RATIO;
    public static final DoubleValue BARRAGE_DRAIN;
    public static final DoubleValue HEAVY_DAMAGE;

    private ArcanaConfig() {
    }

    static {
        Builder b = new Builder();
        b.comment("Barriers remain until their owner releases them or Barrier Breaker destroys them. No lifetime timer.").push("barriers");
        EXAM_RADIUS = b.comment("Base examination radius in blocks. Spell levels add 2 blocks per level.").defineInRange("examinationRadius", 12, 6, 48);
        DEFENSE_RADIUS = b.defineInRange("defensiveRadius", 4, 2, 16);
        MAX_FIELDS = b.comment("Per-dimension performance limit. One of each barrier per caster.").defineInRange("maxActiveFields", 32, 1, 128);
        DEVICE_RADIUS = b.comment("Tier V device radius. Tiers use 25%, 37.5%, 50%, 75%, 100%. World height and borders still apply.")
            .defineInRange("grandBarrierRadius", 128, 32, 128);
        b.pop().push("magic");
        PIERCE_COOLDOWN = b.comment("Visible Iron cooldown in seconds; normal Iron gear reductions apply. Legacy 600-second setting is retired.")
            .defineInRange("barrierBreakerVisibleCooldownSeconds", 90, 30, 86400);
        FLIGHT_DRAIN = b.defineInRange("flightManaPerSecond", 1.0, 0.0, 100.0);
        BARRAGE_DRAIN = b.comment("Continuous barrage cost per second; regeneration is suppressed while active.")
            .defineInRange("barrageManaPerSecond", 40.0, 1.0, 500.0);
        HEAVY_CAPACITY = b.defineInRange("heavyBeamMinimumCapacity", 500, 100, 100000);
        HEAVY_COST = b.defineInRange("heavyBeamManaCost", 450, 100, 100000);
        HEAVY_CHARGE = b.comment("Minimum charge time in seconds; gear cannot shorten it.").defineInRange("heavyBeamChargeSeconds", 8, 3, 60);
        HEAVY_DAMAGE = b.defineInRange("heavyBeamBaseDamage", 100.0, 20.0, 10000.0);
        SIGHT_RANGE = b.defineInRange("manaSightRange", 48.0, 8.0, 128.0);
        CONCEAL_RATIO = b.comment("Fraction of mana visible to mana sight while concealed.").defineInRange("concealedVisibleRatio", 0.1, 0.0, 1.0);
        b.pop();
        SPEC = b.build();
    }
}
