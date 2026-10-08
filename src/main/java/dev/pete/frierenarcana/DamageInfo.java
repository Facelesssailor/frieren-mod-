package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

public final class DamageInfo {
    private DamageInfo() {
    }

    private static String n(double var0) {
        return String.format(Locale.ROOT, var0 >= 100.0 ? "%.0f" : "%.1f", var0);
    }

    private static MutableComponent line(String var0, Object... var1) {
        return Component.translatable("info.frieren_arcana." + var0, var1);
    }

    public static void add(List var0, AbstractSpell var1, int var2, LivingEntity var3) {
        if (var0 != null && var1 != null) {
            MutableComponent var4;
            try {
                var4 = describe(var1, var2, var3);
            } catch (Throwable var6) {
                return;
            }

            if (var4 != null) {
                var0.add(Math.min(1, var0.size()), var4);
            }
        }
    }

    private static MutableComponent describe(AbstractSpell var0, int var1, LivingEntity var2) {
        String var3 = var0.getSpellResource().getPath();
        double var4 = (double)var0.getSpellPower(var1, var2);
        switch (var3) {
            case "zoltraak":
                return line("damage_first", n(var4));
            case "zoltraak_barrage":
                return line("damage_per_shot", n(var4 * 0.55));
            case "zoltraak_heavy":
                double var8 = 100.0;

                try {
                    var8 = ArcanaConfig.HEAVY_DAMAGE.get();
                } catch (Throwable var12) {
                }

                double var10 = var0.getSpellPower(var1, null) <= 0.0F ? 1.0 : var4 / (double)var0.getSpellPower(var1, null);
                return line("damage_line", n(var8 * var10));
            case "nephtear":
                return line("damage_each", n(var4 * 0.6), 2 + var1, line("unit_spears"));
            case "reamstroha":
                return line("damage_line", n(var4 * 1.4));
            case "balgrant":
                return line("damage_area", n(var4 * 1.3), 3 + var1);
            case "reelseiden":
                return line("damage_line", n(var4 * 2.4));
            case "vollzanbel":
                return line("damage_area_burn", n(var4 * 2.0), 2 + var1, 3 + var1);
            case "judradjim":
                return line("damage_area", n(var4 * 2.2), n(1.5 + (double)var1));
            case "waldgose":
                return line("damage_area", n(var4 * 0.8), 2 + var1);
            case "daosdorg":
                return line("damage_area_burn", n(var4 * 1.8), 3 + var1, 3 + var1);
            case "catastrovia":
                return line("damage_line", n(var4 * 1.8));
            case "jubelade":
                return line("damage_line", n(var4 * 1.3));
            case "dragate":
                return line("damage_each", n(var4 * 0.65), 2 + var1, line("unit_rocks"));
            case "goddess_three_spears":
                return line("damage_each", n(var4 * 0.9), 3, line("unit_spears"));
            case "goddess_healing":
                return line("heals", n((double)(4 + 4 * var1)));
            case "black_hole":
                return line("damage_black_hole", n(var4 * 0.35), n(var4 * 1.6));
            case "height_of_magic":
                return line("damage_cone", n(var4 * 0.5));
            case "golem_fist":
                return line("damage_area", n(var4 * 1.5), n(2.2 + 0.5 * (double)var1));
            case "sorganeil":
                return line("no_damage_hold", 4 + 2 * var1);
            case "golden_transmutation":
                return line("no_damage_hold", 10);
            default:
                return null;
        }
    }
}
