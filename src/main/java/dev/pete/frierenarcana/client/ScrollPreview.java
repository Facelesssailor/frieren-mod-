package dev.pete.frierenarcana.client;

import dev.pete.frierenarcana.ArcanaSpell;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class ScrollPreview {
    private ScrollPreview() {
    }

    static float multiplier(ArcanaSpell.Kind var0) {
        switch (var0) {
            case ZOLTRAAK:
                return 1.0F;
            case INFERNO:
                return 2.0F;
            case THUNDER:
                return 2.2F;
            case TORNADO:
                return 0.8F;
            case FIREWIND:
                return 1.8F;
            case EARTH:
                return 1.3F;
            default:
                return 0.0F;
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        try {
            ItemStack var1 = var0.getItemStack();
            if (var1 == null || var1.isEmpty() || !"Scroll".equals(var1.getItem().getClass().getSimpleName())) {
                return;
            }

            ISpellContainer var2 = ISpellContainer.get(var1);
            if (var2 == null) {
                return;
            }

            SpellData var3 = var2.getSpellAtIndex(0);
            if (var3 == null) {
                return;
            }

            AbstractSpell var4 = var3.getSpell();
            if (!(var4 instanceof ArcanaSpell var5)) {
                return;
            }

            float var6 = multiplier(var5.kind);
            if (var6 <= 0.0F) {
                return;
            }

            int var7 = var3.getLevel();
            float var8 = var4.getSpellPower(var7, var0.getEntity());
            float var9 = var8 * var6;
            var0.getToolTip().add(Component.translatable("info.frieren_arcana.preview.damage", String.format("%.1f", var9)).withStyle(ChatFormatting.RED));
            if (var5.kind == ArcanaSpell.Kind.TORNADO) {
                var0.getToolTip().add(Component.translatable("info.frieren_arcana.preview.repeating").withStyle(ChatFormatting.GRAY));
            }

            if (var5.kind == ArcanaSpell.Kind.INFERNO) {
                var0.getToolTip().add(Component.translatable("info.frieren_arcana.preview.ignite", 3 + var7).withStyle(ChatFormatting.GOLD));
            }

            if (var5.kind == ArcanaSpell.Kind.ZOLTRAAK) {
                var0.getToolTip().add(Component.translatable("info.frieren_arcana.preview.pierce").withStyle(ChatFormatting.GRAY));
            }
        } catch (Throwable var10) {
        }
    }
}
