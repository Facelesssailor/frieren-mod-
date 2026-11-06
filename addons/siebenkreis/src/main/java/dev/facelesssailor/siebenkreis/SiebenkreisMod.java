package dev.facelesssailor.siebenkreis;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(SiebenkreisMod.MODID)
public class SiebenkreisMod {
    public static final String MODID = "siebenkreis";

    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, MODID);
    public static final DeferredHolder<AbstractSpell, SiebenkreisSpell> SIEBENKREIS =
            SPELLS.register("siebenkreis", SiebenkreisSpell::new);

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<SiebenkreisEntity>> CIRCLE =
            ENTITIES.register("siebenkreis_circle", () -> EntityType.Builder
                    .<SiebenkreisEntity>of(SiebenkreisEntity::new, MobCategory.MISC)
                    .sized(2.0f, 2.0f)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .noSave()
                    .fireImmune()
                    .build("siebenkreis_circle"));

    public SiebenkreisMod(IEventBus modBus) {
        SPELLS.register(modBus);
        ENTITIES.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
