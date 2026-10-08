package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class KeyCompat {
    private static boolean done;
    private static final String FLAG = "frieren_arcana-keyfix.txt";
    private static final int[] FREE = new int[]{
        90, 77, 73, 79, 44, 46, 59, 39, 91, 93, 92, 45, 61, 321, 322, 323, 324, 325, 326, 327, 328, 329, 295, 296, 297, 298, 299, 301
    };
    private static final String[] VANILLA_CATEGORIES = new String[]{
        "key.categories.movement",
        "key.categories.gameplay",
        "key.categories.inventory",
        "key.categories.creative",
        "key.categories.multiplayer",
        "key.categories.ui",
        "key.categories.misc"
    };

    private KeyCompat() {
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        if (!done) {
            Minecraft var1 = Minecraft.getInstance();
            if (var1.options != null && var1.player != null && var1.options.keyMappings != null) {
                done = true;

                try {
                    Path var2 = FMLPaths.CONFIGDIR.get().resolve("frieren_arcana-keyfix.txt");
                    if (Files.exists(var2)) {
                        return;
                    }

                    ArrayList var3 = new ArrayList();
                    ArrayList var4 = new ArrayList();
                    fix(var1.options.keyMappings, var3, var4);
                    if (!var3.isEmpty()) {
                        KeyMapping.resetMapping();
                        var1.options.save();
                    }

                    Files.writeString(
                        var2,
                        "Frieren Arcana keybind fix ran once. Delete this file to run it again.\n" + String.join("\n", var3) + "\n",
                        StandardCharsets.UTF_8
                    );

                    for (MutableComponent var6 : var4) {
                        var1.player.displayClientMessage(var6, false);
                    }
                } catch (Throwable var7) {
                }
            }
        }
    }

    private static boolean ours(KeyMapping var0) {
        return var0.getName() != null && var0.getName().startsWith("key.frieren_arcana.");
    }

    private static boolean ironsCasting(KeyMapping var0) {
        String var1 = var0.getName();
        return var1 != null
            && (
                var1.equals("key.irons_spellbooks.spellbook_cast")
                    || var1.equals("key.irons_spellbooks.spell_wheel")
                    || var1.equals("key.irons_spellbooks.spell_wheel_toggle")
                    || var1.startsWith("key.irons_spellbooks.spell_quick_cast")
            );
    }

    private static boolean vanilla(KeyMapping var0) {
        String var1 = var0.getCategory();
        if (var1 == null) {
            return false;
        } else {
            for (String var5 : VANILLA_CATEGORIES) {
                if (var5.equals(var1)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean clash(KeyMapping var0, KeyMapping var1) {
        if (var0 != var1 && !var0.isUnbound() && !var1.isUnbound()) {
            if (var0.getKey() == null || !var0.getKey().equals(var1.getKey())) {
                return false;
            } else if (var0.getKeyModifier() != var1.getKeyModifier()) {
                return false;
            } else {
                IKeyConflictContext var2 = var0.getKeyConflictContext();
                IKeyConflictContext var3 = var1.getKeyConflictContext();
                return var2 == null || var3 == null || var2.conflicts(var3) || var3.conflicts(var2);
            }
        } else {
            return false;
        }
    }

    private static void fix(KeyMapping[] var0, List<String> var1, List<MutableComponent> var2) {
        for (KeyMapping var6 : var0) {
            boolean var7 = ironsCasting(var6);
            boolean var8 = ours(var6);
            if (var7 || var8) {
                for (KeyMapping var12 : var0) {
                    if (clash(var6, var12)) {
                        KeyMapping var13;
                        if (var8 && !ironsCasting(var12)) {
                            var13 = var6;
                        } else if (var7 && !ours(var12) && !ironsCasting(var12)) {
                            var13 = var12;
                        } else {
                            if (!var7 || !ours(var12)) {
                                continue;
                            }

                            var13 = var12;
                        }

                        if (!vanilla(var13) && var13.isDefault()) {
                            Key var14 = freeKey(var0);
                            if (var14 == null) {
                                return;
                            }

                            Key var15 = var13.getKey();
                            var13.setKeyModifierAndCode(KeyModifier.NONE, var14);
                            var1.add(var13.getName() + ": " + var15.getValue() + " -> " + var14.getValue());
                            var2.add(
                                Component.literal("[Frieren Arcana] ")
                                    .append(Component.translatable(var13.getName()))
                                    .append(Component.literal(": "))
                                    .append(var15.getDisplayName())
                                    .append(Component.literal(" → "))
                                    .append(var14.getDisplayName())
                                    .append(Component.literal(var13 == var12 && var7 ? " (it blocked " : " (it clashed with "))
                                    .append(Component.translatable(var6 == var13 ? var12.getName() : var6.getName()))
                                    .append(Component.literal("). Change it any time in Controls."))
                            );
                            if (var13 == var6) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static Key freeKey(KeyMapping[] var0) {
        for (int var4 : FREE) {
            Key var5 = Type.KEYSYM.getOrCreate(var4);
            boolean var6 = false;

            for (KeyMapping var10 : var0) {
                if (var5.equals(var10.getKey())) {
                    var6 = true;
                    break;
                }
            }

            if (!var6) {
                return var5;
            }
        }

        return null;
    }
}
