package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

@Mod("frieren_arcana")
public final class FrierenArcana {
    public static final String ID = "frieren_arcana";
    public static final Items ITEMS = DeferredRegister.createItems("frieren_arcana");
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, "frieren_arcana");
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "frieren_arcana");
    public static final DeferredItem<Item> RELEASE = ITEMS.register("release_sigil", () -> new ReleaseSigil(new Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> STAFF = ITEMS.register("flight_staff", () -> new Item(new Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final List<DeferredItem<Item>> DEVICES = new ArrayList<>();
    public static final Map<ArcanaSpell.Kind, DeferredHolder<AbstractSpell, ArcanaSpell>> SPELL_MAP;
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB;

    public static ResourceLocation id(String var0) {
        return ResourceLocation.fromNamespaceAndPath("frieren_arcana", var0);
    }

    public FrierenArcana(IEventBus var1, ModContainer var2) {
        NewMagic.init();
        ITEMS.register(var1);
        SPELLS.register(var1);
        TABS.register(var1);
        var2.registerConfig(Type.SERVER, ArcanaConfig.SPEC);
        var1.addListener(ArcanaNetwork::register);
        var1.addListener(var0 -> var0.enqueueWork(SablePhysicsCompat::register));
        NeoForge.EVENT_BUS.register(ArcanaEvents.class);
    }

    static {
        for (int var0 = 1; var0 <= 5; var0++) {
            int var1 = var0;
            DEVICES.add(
                ITEMS.register(
                    "barrier_device_" + var0, () -> new BarrierDevice(new Properties().stacksTo(1).rarity(var1 < 4 ? Rarity.RARE : Rarity.EPIC), var1)
                )
            );
        }

        SPELL_MAP = new LinkedHashMap<>();

        for (ArcanaSpell.Kind var3 : ArcanaSpell.Kind.values()) {
            SPELL_MAP.put(var3, SPELLS.register(var3.path, () -> new ArcanaSpell(var3)));
        }

        TAB = TABS.register(
            "arcana",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.frieren_arcana"))
                    .icon(() -> RELEASE.get().getDefaultInstance())
                    .displayItems((var0x, var1x) -> {
                        var1x.accept(RELEASE.get());
                        var1x.accept(STAFF.get());

                        for (DeferredItem var3x : DEVICES) {
                            var1x.accept((ItemLike)var3x.get());
                        }

                        for (DeferredHolder var8 : SPELL_MAP.values()) {
                            AbstractSpell var4 = (AbstractSpell)var8.get();

                            for (int var5 = 1; var5 <= var4.getMaxLevel(); var5++) {
                                ItemStack var6 = new ItemStack(ItemRegistry.SCROLL.get());
                                ISpellContainer.createScrollContainer(var4, var5, var6);
                                var1x.accept(var6);
                            }
                        }
                    })
                    .build()
        );
    }
}
