package dev.pete.frierenarcana.compat.jei;

import com.mojang.blaze3d.platform.NativeImage;
import dev.pete.frierenarcana.ArcanaSpell;
import dev.pete.frierenarcana.FrierenArcana;
import dev.pete.frierenarcana.SpellGuidePages;
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

    static void start(IJeiRuntime runtime) {
        if (!started) {
            started = true;
            int expected = SpellGuidePages.all().size();
            long actual = runtime.getRecipeManager().createRecipeLookup(ArcanaJeiPlugin.GUIDE).includeHidden().get().count();
            if (actual != (long)expected) {
                throw new IllegalStateException("JEI guide expected " + expected + " pages, registered " + actual);
            } else {
                System.out.println("FRIEREN_JEI_SMOKE registered " + actual + " spell-level pages");
                int[] tick = new int[]{0};
                NeoForge.EVENT_BUS
                    .addListener(
                        event -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.level != null && mc.player != null) {
                                int t = ++tick[0];
                                if (t == 5) {
                                    runtime.getRecipesGui().showTypes(List.of(ArcanaJeiPlugin.GUIDE));
                                }

                                if (t == 25) {
                                    capture(mc, "jei-spell-guide.png");
                                }

                                if (t == 35) {
                                    runtime.getRecipesGui()
                                        .show(
                                            runtime.getJeiHelpers()
                                                .getFocusFactory()
                                                .createFocus(
                                                    RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, FrierenArcana.STAFF.get().getDefaultInstance()
                                                )
                                        );
                                }

                                if (t == 55) {
                                    capture(mc, "jei-staff-crafting.png");
                                }

                                if (t == 65) {
                                    runtime.getRecipesGui()
                                        .showRecipes(
                                            runtime.getRecipeManager().getRecipeCategory(ArcanaJeiPlugin.GUIDE),
                                            SpellGuidePages.all().stream().filter(p -> p.spell().kind == ArcanaSpell.Kind.FIREWIND && p.level() == 5).toList(),
                                            List.of()
                                        );
                                }

                                if (t == 85) {
                                    capture(mc, "jei-fire-wind-guide.png");
                                }

                                if (t == 95) {
                                    runtime.getRecipesGui()
                                        .show(
                                            runtime.getJeiHelpers()
                                                .getFocusFactory()
                                                .createFocus(
                                                    RecipeIngredientRole.OUTPUT,
                                                    VanillaTypes.ITEM_STACK,
                                                    FrierenArcana.DEVICES.get(4).get().getDefaultInstance()
                                                )
                                        );
                                }

                                if (t == 115) {
                                    capture(mc, "jei-barrier-upgrade.png");
                                }

                                if (t == 125) {
                                    mc.setScreen(null);
                                }

                                if (t == 135) {
                                    System.out.println("FRIEREN_JEI_SMOKE completed guide and crafting screens");
                                    mc.stop();
                                }
                            }
                        }
                    );
            }
        }
    }

    private static void capture(Minecraft mc, String name) {
        Path dir = mc.gameDirectory.toPath().resolve("jei-smoke");

        try {
            Files.createDirectories(dir);

            try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                image.writeToFile(dir.resolve(name));
            }

            System.out.println("FRIEREN_JEI_SMOKE screenshot " + name);
        } catch (Exception var8) {
            throw new IllegalStateException("JEI screenshot failed", var8);
        }
    }
}
