package dev.pete.frierenarcana;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(
    modid = "frieren_arcana",
    bus = Bus.MOD
)
public final class GuideBook {
    public static Item ITEM;

    private GuideBook() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent var0) {
        var0.register(Registries.ITEM, var0x -> {
            ITEM = new GuideBookItem(new Properties().stacksTo(1));
            var0x.register(ResourceLocation.fromNamespaceAndPath("frieren_arcana", "guide_book"), ITEM);
        });
    }

    @SubscribeEvent
    public static void tab(BuildCreativeModeTabContentsEvent var0) {
        if (ITEM != null && var0.getTabKey() != null && var0.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("frieren_arcana", "arcana"))) {
            var0.accept(ITEM);
        }
    }
}
