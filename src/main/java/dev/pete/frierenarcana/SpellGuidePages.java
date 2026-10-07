package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class SpellGuidePages {
    public static List<SpellGuidePages.Page> all() {
        List<SpellGuidePages.Page> pages = new ArrayList<>();

        for (DeferredHolder<AbstractSpell, ArcanaSpell> holder : FrierenArcana.SPELL_MAP.values()) {
            ArcanaSpell spell = holder.get();
            if (spell.isEnabled()) {
                for (int level = spell.getMinLevel(); level <= spell.getMaxLevel(); level++) {
                    ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
                    ISpellContainer.createScrollContainer(spell, level, scroll);
                    pages.add(new SpellGuidePages.Page(spell, level, scroll));
                }
            }
        }

        return List.copyOf(pages);
    }

    public static record Page(ArcanaSpell spell, int level, ItemStack scroll) {
    }
}
