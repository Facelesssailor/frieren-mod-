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
    public void registerCategories(IRecipeCategoryRegistration var1) {
        var1.addRecipeCategories(new ArcanaSpellCategory(var1.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration var1) {
        var1.addExtraItemStacks(SpellGuidePages.all().stream().map(SpellGuidePages.Page::scroll).toList());
    }

    @Override
    public void registerRecipes(IRecipeRegistration var1) {
        List var2 = SpellGuidePages.all();
        var1.addRecipes(GUIDE, var2);

        for (SpellGuidePages.Page var4 : var2) {
            var1.addItemStackInfo(
                var4.scroll(),
                Component.translatable("spell.frieren_arcana." + var4.spell().kind.path + ".guide"),
                Component.translatable("jei.frieren_arcana.inscription")
            );
        }

        var1.addIngredientInfo(FrierenArcana.RELEASE.get(), Component.translatable("jei.frieren_arcana.release"));

        for (DeferredItem var7 : FrierenArcana.DEVICES) {
            BarrierDevice var5 = (BarrierDevice)var7.get();
            var1.addIngredientInfo(
                var5, Component.translatable("info.frieren_arcana.device", var5.radius(), var5.manaCost()), Component.translatable("jei.frieren_arcana.device")
            );
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime var1) {
        if (Boolean.getBoolean("frieren_arcana.jeiClientSmoke")) {
            JeiClientSmoke.start(var1);
        }
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration var1) {
        for (SpellGuidePages.Page var3 : SpellGuidePages.all()) {
            var1.addAlias(var3.scroll(), "Frieren Arcana spell magic");
        }

        for (DeferredItem var5 : FrierenArcana.DEVICES) {
            var1.addAlias(((Item)var5.get()).getDefaultInstance(), "Frieren examination barrier dome");
        }
    }
}
