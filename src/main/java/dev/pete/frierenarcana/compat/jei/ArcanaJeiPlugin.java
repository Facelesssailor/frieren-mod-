package dev.pete.frierenarcana.compat.jei;

import dev.pete.frierenarcana.BarrierDevice;
import dev.pete.frierenarcana.FrierenArcana;
import dev.pete.frierenarcana.SpellGuidePages;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

@JeiPlugin
public final class ArcanaJeiPlugin implements IModPlugin {
    public static final RecipeType<SpellGuidePages.Page> GUIDE = new RecipeType<>(FrierenArcana.id("spell_guide"), SpellGuidePages.Page.class);

    @Override
    public ResourceLocation getPluginUid() {
        return FrierenArcana.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ArcanaSpellCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        registration.addExtraItemStacks(SpellGuidePages.all().stream().map(SpellGuidePages.Page::scroll).toList());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<SpellGuidePages.Page> pages = SpellGuidePages.all();
        registration.addRecipes(GUIDE, pages);

        for (SpellGuidePages.Page page : pages) {
            registration.addItemStackInfo(
                page.scroll(),
                Component.translatable("spell.frieren_arcana." + page.spell().kind.path + ".guide"),
                Component.translatable("jei.frieren_arcana.inscription")
            );
        }

        registration.addIngredientInfo(FrierenArcana.STAFF.get(), Component.translatable("jei.frieren_arcana.staff"));
        registration.addIngredientInfo(FrierenArcana.RELEASE.get(), Component.translatable("jei.frieren_arcana.release"));

        for (DeferredItem<Item> item : FrierenArcana.DEVICES) {
            BarrierDevice device = (BarrierDevice)item.get();
            registration.addIngredientInfo(
                device,
                Component.translatable("info.frieren_arcana.device", device.radius(), device.manaCost()),
                Component.translatable("jei.frieren_arcana.device")
            );
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (Boolean.getBoolean("frieren_arcana.jeiClientSmoke")) {
            JeiClientSmoke.start(runtime);
        }
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        for (SpellGuidePages.Page page : SpellGuidePages.all()) {
            registration.addAlias(page.scroll(), "Frieren Arcana spell magic");
        }

        registration.addAlias(FrierenArcana.STAFF.get().getDefaultInstance(), "Frieren flight staff");

        for (DeferredItem<Item> item : FrierenArcana.DEVICES) {
            registration.addAlias(item.get().getDefaultInstance(), "Frieren examination barrier dome");
        }
    }
}
