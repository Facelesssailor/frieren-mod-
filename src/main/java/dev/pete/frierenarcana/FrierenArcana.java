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

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("frieren_arcana", path);
    }

    public FrierenArcana(IEventBus bus, ModContainer container) {
        ITEMS.register(bus);
        SPELLS.register(bus);
        TABS.register(bus);
        container.registerConfig(Type.SERVER, ArcanaConfig.SPEC);
        bus.addListener(ArcanaNetwork::register);
        bus.addListener(event -> event.enqueueWork(SablePhysicsCompat::register));
        NeoForge.EVENT_BUS.register(ArcanaEvents.class);
    }

    static {
        for (int tier = 1; tier <= 5; tier++) {
            int t = tier;
            DEVICES.add(
                ITEMS.register("barrier_device_" + tier, () -> new BarrierDevice(new Properties().stacksTo(1).rarity(t < 4 ? Rarity.RARE : Rarity.EPIC), t))
            );
        }

        SPELL_MAP = new LinkedHashMap<>();

        for (ArcanaSpell.Kind kind : ArcanaSpell.Kind.values()) {
            SPELL_MAP.put(kind, SPELLS.register(kind.path, () -> new ArcanaSpell(kind)));
        }

        TAB = TABS.register(
            "arcana",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.frieren_arcana"))
                    .icon(() -> RELEASE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(RELEASE.get());
                        output.accept(STAFF.get());

                        for (DeferredItem<Item> device : DEVICES) {
                            output.accept(device.get());
                        }

                        for (DeferredHolder<AbstractSpell, ArcanaSpell> holder : SPELL_MAP.values()) {
                            AbstractSpell spell = holder.get();

                            for (int level = 1; level <= spell.getMaxLevel(); level++) {
                                ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
                                ISpellContainer.createScrollContainer(spell, level, scroll);
                                output.accept(scroll);
                            }
                        }
                    })
                    .build()
        );
    }
}
