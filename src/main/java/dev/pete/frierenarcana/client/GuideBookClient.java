package dev.pete.frierenarcana.client;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen.BookAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.fml.ModList;

public final class GuideBookClient {
    private static List<Component> cached;
    private static String cachedLang;

    private GuideBookClient() {
    }

    public static void open() {
        try {
            Minecraft var0 = Minecraft.getInstance();
            if (openPatchouli()) {
                return;
            }

            List var1 = pages(var0);
            if (var1.isEmpty()) {
                return;
            }

            var0.setScreen(new BookViewScreen(new BookAccess(var1)));
        } catch (Throwable var2) {
        }
    }

    private static boolean openPatchouli() {
        try {
            if (!ModList.get().isLoaded("patchouli")) {
                return false;
            } else {
                Object var0 = Class.forName("vazkii.patchouli.api.PatchouliAPI").getMethod("get").invoke(null);
                Method var1 = find(var0.getClass(), "openBookGUI");
                if (var1 == null) {
                    return false;
                } else {
                    var1.invoke(var0, ResourceLocation.fromNamespaceAndPath("frieren_arcana", "guide"));
                    return true;
                }
            }
        } catch (Throwable var2) {
            return false;
        }
    }

    private static Method find(Class<?> var0, String var1) {
        for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
            for (Class var6 : var2.getInterfaces()) {
                for (Method var10 : var6.getMethods()) {
                    if (var10.getName().equals(var1) && var10.getParameterCount() == 1 && var10.getParameterTypes()[0] == ResourceLocation.class) {
                        return var10;
                    }
                }
            }
        }

        for (Method var14 : var0.getMethods()) {
            if (var14.getName().equals(var1) && var14.getParameterCount() == 1 && var14.getParameterTypes()[0] == ResourceLocation.class) {
                return var14;
            }
        }

        return null;
    }

    private static List<Component> pages(Minecraft var0) {
        String var1 = var0.options != null && var0.options.languageCode != null ? var0.options.languageCode : "en_us";
        if (cached != null && var1.equals(cachedLang)) {
            return cached;
        } else {
            String var2 = read(var0, var1);
            if (var2 == null && !var1.equals("en_us")) {
                var2 = read(var0, "en_us");
            }

            ArrayList var3 = new ArrayList();
            if (var2 == null) {
                var3.add(Component.literal("The guide could not be loaded."));
                return var3;
            } else {
                for (String var7 : var2.replace("\r", "").split("\n@@page\n")) {
                    var3.add(page(var7.endsWith("\n") ? var7.substring(0, var7.length() - 1) : var7));
                }

                cached = var3;
                cachedLang = var1;
                return var3;
            }
        }
    }

    private static String read(Minecraft var0, String var1) {
        try {
            Optional var2 = var0.getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("frieren_arcana", "guide/" + var1 + ".txt"));
            if (var2.isEmpty()) {
                return null;
            } else {
                String var10;
                try (BufferedReader var3 = ((Resource)var2.get()).openAsReader()) {
                    StringBuilder var4 = new StringBuilder();
                    char[] var5 = new char[8192];

                    while ((var6 = var3.read(var5)) > 0) {
                        var4.append(var5, 0, var6);
                    }

                    var10 = var4.toString();
                }

                return var10;
            }
        } catch (Throwable var9) {
            return null;
        }
    }

    private static Component page(String var0) {
        MutableComponent var1 = Component.empty();
        int var2 = 0;

        while (var2 < var0.length()) {
            int var3 = var0.indexOf("[[", var2);
            if (var3 < 0) {
                var1.append(Component.literal(var0.substring(var2)));
                break;
            }

            int var4 = var0.indexOf("]]", var3);
            int var5 = var4 < 0 ? -1 : var0.indexOf(58, var3);
            if (var4 >= 0 && var5 >= 0 && var5 <= var4) {
                if (var3 > var2) {
                    var1.append(Component.literal(var0.substring(var2, var3)));
                }

                String var6 = var0.substring(var3 + 2, var5).trim();
                String var7 = var0.substring(var5 + 1, var4);
                MutableComponent var8 = Component.literal(var7);

                try {
                    int var9 = Integer.parseInt(var6);
                    var8 = var8.withStyle(
                        var1x -> var1x.withColor(ChatFormatting.DARK_BLUE)
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent(Action.CHANGE_PAGE, Integer.toString(var9)))
                    );
                } catch (NumberFormatException var10) {
                }

                var1.append(var8);
                var2 = var4 + 2;
            } else {
                var1.append(Component.literal(var0.substring(var2, var3 + 2)));
                var2 = var3 + 2;
            }
        }

        return var1;
    }
}
