package dev.pete.frierenarcana.compat.jei;

import dev.pete.frierenarcana.ArcanaSpell;
import dev.pete.frierenarcana.FrierenArcana;
import dev.pete.frierenarcana.SpellGuidePages;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class ArcanaSpellCategory implements IRecipeCategory<SpellGuidePages.Page> {
    private final IDrawable icon;

    public ArcanaSpellCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, FrierenArcana.RELEASE.get().getDefaultInstance());
    }

    @Override
    public RecipeType<SpellGuidePages.Page> getRecipeType() {
        return ArcanaJeiPlugin.GUIDE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.frieren_arcana.category");
    }

    @Override
    public int getWidth() {
        return 216;
    }

    @Override
    public int getHeight() {
        return 154;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, SpellGuidePages.Page page, IFocusGroup focus) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 8, 28).addItemStack(page.scroll());
    }

    public void draw(SpellGuidePages.Page page, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.fill(0, 0, 216, 154, -15130058);
        graphics.fill(0, 0, 216, 2, -2505853);
        List<FormattedCharSequence> titles = font.split(page.spell().getDisplayName(Minecraft.getInstance().player), 200);

        for (int i = 0; i < Math.min(2, titles.size()); i++) {
            graphics.drawString(font, titles.get(i), 8, 5 + i * 9, -6992, false);
        }

        graphics.drawString(font, Component.translatable("jei.frieren_arcana.level", page.level(), page.spell().getMaxLevel()), 32, 28, -1577480, false);
        Component cost = page.spell().kind == ArcanaSpell.Kind.PIERCE
            ? Component.translatable("jei.frieren_arcana.full_mana")
            : Component.translatable("info.frieren_arcana.cost", page.spell().getManaCost(page.level()));
        graphics.drawString(font, cost, 32, 39, -6432513, false);
        List<FormattedCharSequence> lines = font.split(Component.translatable("spell.frieren_arcana." + page.spell().kind.path + ".guide"), 200);

        for (int i = 0; i < Math.min(8, lines.size()); i++) {
            graphics.drawString(font, lines.get(i), 8, 57 + i * 9, -1577480, false);
        }

        if (lines.size() > 8) {
            graphics.drawString(font, "…", 198, 120, -1577480, false);
        }

        graphics.drawString(font, Component.translatable("jei.frieren_arcana.cooldown", page.spell().getSpellCooldown() / 20), 8, 140, -2505853, false);
    }

    public void getTooltip(ITooltipBuilder tooltip, SpellGuidePages.Page page, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseY >= 52.0 && mouseY <= 137.0) {
            tooltip.addAll(page.spell().getUniqueInfo(page.level(), Minecraft.getInstance().player));
            tooltip.add(Component.translatable("jei.frieren_arcana.reference"));
        }
    }
}
