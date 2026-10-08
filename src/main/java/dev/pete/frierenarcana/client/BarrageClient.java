package dev.pete.frierenarcana.client;

import dev.pete.frierenarcana.ArcanaModes;
import dev.pete.frierenarcana.ArcanaNetwork;
import java.util.ArrayList;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class BarrageClient {
    private static boolean active;
    private static long since;
    private static boolean holdMode;
    private static boolean[] wasDown = new boolean[0];
    private static KeyMapping[] castKeys;

    private BarrageClient() {
    }

    public static void state(boolean var0) {
        active = var0;
        since = System.nanoTime();
        holdMode = false;
    }

    private static KeyMapping[] castKeys(Minecraft var0) {
        if (castKeys == null) {
            ArrayList var1 = new ArrayList();

            for (KeyMapping var5 : var0.options.keyMappings) {
                String var6 = var5.getName();
                if (var6 != null && var6.contains("irons_spellbooks") && var6.contains("cast")) {
                    var1.add(var5);
                }
            }

            castKeys = var1.toArray(new KeyMapping[0]);
            wasDown = new boolean[castKeys.length];
        }

        return castKeys;
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        Minecraft var1 = Minecraft.getInstance();
        LocalPlayer var2 = var1.player;
        if (var2 == null) {
            active = false;
        } else {
            KeyMapping[] var3 = castKeys(var1);
            boolean var4 = false;

            for (int var5 = 0; var5 < var3.length; var5++) {
                boolean var6 = var3[var5].isDown();
                if (var6 && !wasDown[var5]) {
                    var4 = true;
                }

                wasDown[var5] = var6;
            }

            if (active && var1.screen == null) {
                double var10 = (double)(System.nanoTime() - since) / 1.0E9;
                boolean var7 = ArcanaModes.isStaff(var2.getMainHandItem()) || ArcanaModes.isStaff(var2.getOffhandItem());
                boolean var8 = var1.options.keyUse.isDown();
                if (var7 && var8) {
                    holdMode = true;
                }

                boolean var9 = var4 && var10 > 0.25 || holdMode && (!var8 || !var7);
                if (var9) {
                    PacketDistributor.sendToServer(new ArcanaNetwork.ModeRequest(3));
                    active = false;
                    holdMode = false;
                }
            }
        }
    }
}
