package dev.pete.frierenarcana.compat.jei;

import com.mojang.blaze3d.platform.NativeImage;
import dev.pete.frierenarcana.ArcanaSpell;
import dev.pete.frierenarcana.FrierenArcana;
import dev.pete.frierenarcana.SpellGuidePages;
import dev.pete.frierenarcana.StaffView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.neoforge.common.NeoForge;

final class JeiClientSmoke {
    private static boolean started;

    static void start(IJeiRuntime var0) {
        if (!started) {
            started = true;
            int var1 = SpellGuidePages.all().size();
            long var2 = var0.getRecipeManager().createRecipeLookup(ArcanaJeiPlugin.GUIDE).includeHidden().get().count();
            if (var2 != (long)var1) {
                throw new IllegalStateException("JEI guide expected " + var1 + " pages, registered " + var2);
            } else {
                System.out.println("FRIEREN_JEI_SMOKE registered " + var2 + " spell-level pages");
                int[] var4 = new int[]{0};
                NeoForge.EVENT_BUS
                    .addListener(
                        var2x -> {
                            Minecraft var3 = Minecraft.getInstance();
                            if (var3.level != null && var3.player != null) {
                                int var4x = ++var4[0];
                                if (var4x == 5) {
                                    var0.getRecipesGui().showTypes(List.of(ArcanaJeiPlugin.GUIDE));
                                }

                                if (var4x == 25) {
                                    capture(var3, "jei-spell-guide.png");
                                }

                                if (var4x == 35) {
                                    var0.getRecipesGui()
                                        .show(
                                            var0.getJeiHelpers()
                                                .getFocusFactory()
                                                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, StaffView.demo())
                                        );
                                }

                                if (var4x == 55) {
                                    capture(var3, "jei-staff-crafting.png");
                                }

                                if (var4x == 65) {
                                    var0.getRecipesGui()
                                        .showRecipes(
                                            var0.getRecipeManager().getRecipeCategory(ArcanaJeiPlugin.GUIDE),
                                            SpellGuidePages.all()
                                                .stream()
                                                .filter(var0xx -> var0xx.spell().kind == ArcanaSpell.Kind.FIREWIND && var0xx.level() == 5)
                                                .toList(),
                                            List.of()
                                        );
                                }

                                if (var4x == 85) {
                                    capture(var3, "jei-fire-wind-guide.png");
                                }

                                if (var4x == 95) {
                                    var0.getRecipesGui()
                                        .show(
                                            var0.getJeiHelpers()
                                                .getFocusFactory()
                                                .createFocus(
                                                    RecipeIngredientRole.OUTPUT,
                                                    VanillaTypes.ITEM_STACK,
                                                    FrierenArcana.DEVICES.get(4).get().getDefaultInstance()
                                                )
                                        );
                                }

                                if (var4x == 115) {
                                    capture(var3, "jei-barrier-upgrade.png");
                                }

                                if (var4x == 125) {
                                    var3.setScreen(null);
                                }

                                if (var4x == 135) {
                                    System.out.println("FRIEREN_JEI_SMOKE completed guide and crafting screens");
                                    var3.stop();
                                }
                            }
                        }
                    );
            }
        }
    }

    private static void capture(Minecraft var0, String var1) {
        Path var2 = var0.gameDirectory.toPath().resolve("jei-smoke");

        try {
            Files.createDirectories(var2);

            try (NativeImage var3 = Screenshot.takeScreenshot(var0.getMainRenderTarget())) {
                var3.writeToFile(var2.resolve(var1));
            }

            System.out.println("FRIEREN_JEI_SMOKE screenshot " + var1);
        } catch (Exception var8) {
            throw new IllegalStateException("JEI screenshot failed", var8);
        }
    }
}
