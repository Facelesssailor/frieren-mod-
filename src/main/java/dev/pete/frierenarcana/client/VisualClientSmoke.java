package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.pete.frierenarcana.ArcanaCooldowns;
import dev.pete.frierenarcana.ArcanaNetwork;
import dev.pete.frierenarcana.ArcanaSpell;
import dev.pete.frierenarcana.BarrierData;
import dev.pete.frierenarcana.FrierenArcana;
import dev.pete.frierenarcana.StaffView;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import io.redspace.ironsspellbooks.gui.overlays.SpellWheelOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.registries.DeferredItem;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class VisualClientSmoke {
    private static int tick;
    private static String pendingCapture;
    private static UUID fieldId = UUID.randomUUID();
    private static final Vec3 CENTER = new Vec3(0.0, 75.0, 0.0);

    @SubscribeEvent
    public static void tick(Post var0) {
        if (Boolean.getBoolean("frieren_arcana.visualClientSmoke")) {
            Minecraft var1 = Minecraft.getInstance();
            if (var1.level != null && var1.player != null && var1.getSingleplayerServer() != null) {
                int var2 = ++tick;
                if (var2 == 5) {
                    var1.setScreen(new VisualClientSmoke.Gallery());
                    setup(var1);
                }

                if (var2 == 35) {
                    capture(var1, "01-3d-items.png");
                }

                if (var2 == 45) {
                    var1.setScreen(null);
                }

                if (var2 == 60) {
                    scene(var1, new Vec3(0.0, 78.0, -38.0), 0.0F, 8.0F, false);
                }

                if (var2 == 90) {
                    capture(var1, "02-round-exam-dome.png");
                }

                if (var2 == 100) {
                    scene(var1, new Vec3(0.0, 76.0, -2.0), 0.0F, 0.0F, true);
                }

                if (var2 == 125) {
                    if (!ArcanaClient.rainBlocked(CENTER) || ArcanaClient.rainHeight(0, 0, 65) < 90) {
                        throw new IllegalStateException("Rain roof absent");
                    }

                    if (ArcanaClient.rainBlocked(new Vec3(30.0, 75.0, 0.0))) {
                        throw new IllegalStateException("Outside rain incorrectly blocked");
                    }

                    capture(var1, "03-dry-interior.png");
                    System.out.println("FRIEREN_VISUAL_SMOKE Rain roof and inside/outside containment passed");
                }

                if (var2 == 140) {
                    CompoundTag var3 = new CompoundTag();
                    var3.putString("kind", "charge");
                    var3.putUUID("player", var1.player.getUUID());
                    var3.putBoolean("active", true);
                    var3.putBoolean("breaker", true);
                    ArcanaClient.receive(var3);
                }

                if (var2 == 180) {
                    capture(var1, "04-skippable-cutscene.png");
                }

                if (var2 == 185) {
                    if (!ArcanaCinematic.active()) {
                        throw new IllegalStateException("Cutscene did not start");
                    }

                    ArcanaCinematic.skip();
                    if (ArcanaCinematic.active() || var1.getCameraEntity() != var1.player) {
                        throw new IllegalStateException("Skip failed to restore player camera");
                    }

                    System.out.println("FRIEREN_VISUAL_SMOKE Cutscene start, skip and camera restoration passed");
                    CompoundTag var9 = new CompoundTag();
                    var9.putString("kind", "charge");
                    var9.putUUID("player", var1.player.getUUID());
                    var9.putBoolean("active", false);
                    ArcanaClient.receive(var9);
                }

                if (var2 == 195) {
                    scene(var1, new Vec3(0.0, 78.0, -38.0), 0.0F, 8.0F, false);
                }

                if (var2 == 210) {
                    CompoundTag var10 = new CompoundTag();
                    var10.putString("kind", "beam");
                    vector(var10, "start", CENTER.add(0.0, 0.0, -5.0));
                    vector(var10, "end", CENTER.add(0.0, 16.0, 0.0));
                    var10.putInt("style", 3);
                    ArcanaClient.receive(var10);
                    CompoundTag var4 = new CompoundTag();
                    var4.putString("kind", "shatter");
                    var4.put("field", field(false));
                    vector(var4, "impact", CENTER.add(0.0, 16.0, 0.0));
                    ArcanaClient.receive(var4);
                    var1.getSingleplayerServer()
                        .execute(
                            () -> BarrierData.get(var1.getSingleplayerServer().overworld()).remove(var1.getSingleplayerServer().overworld(), fieldId, false)
                        );
                }

                if (var2 == 212) {
                    capture(var1, "05-green-breaker-fracture.png");
                }

                if (var2 == 220) {
                    fields(var1, true);
                    scene(var1, new Vec3(0.0, 78.0, -38.0), 0.0F, 8.0F, false);
                }

                if (var2 == 245) {
                    capture(var1, "06-defensive-honeycomb.png");
                }

                if (var2 == 255) {
                    scene(var1, new Vec3(0.0, 77.0, -9.0), 0.0F, 6.0F, false);
                    var1.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    CompoundTag var11 = new CompoundTag();
                    var11.putString("kind", "flight");
                    var11.putUUID("player", var1.player.getUUID());
                    var11.putBoolean("active", true);
                    ArcanaClient.receive(var11);
                }

                if (var2 >= 255 && var2 <= 285 && var2 % 5 == 0) {
                    CompoundTag var12 = new CompoundTag();
                    var12.putString("kind", "flight");
                    var12.putUUID("player", var1.player.getUUID());
                    var12.putBoolean("active", true);
                    ArcanaClient.receive(var12);
                }

                if (var2 == 277) {
                    capture(var1, "07-flight-staff.png");
                }

                if (var2 == 290) {
                    scene(var1, new Vec3(0.0, 78.0, -14.0), 0.0F, 9.0F, false);

                    for (int var6 : new int[]{11, 13, 14, 17, 8, 9}) {
                        CompoundTag var7 = new CompoundTag();
                        var7.putString("kind", "effect");
                        vector(var7, "center", CENTER.add((double)((var6 % 3 - 1) * 3), 0.0, (double)(var6 % 2 * 4)));
                        var7.putInt("style", var6);
                        var7.putInt("strength", 3);
                        ArcanaClient.receive(var7);
                    }
                }

                if (var2 == 295) {
                    capture(var1, "08-volumetric-spells.png");
                }

                if (var2 == 310) {
                    var1.setScreen(null);
                    var1.options.setCameraType(CameraType.FIRST_PERSON);
                    fields(var1, false);
                }

                if (var2 == 315) {
                    var1.getSingleplayerServer().execute(() -> {
                        ServerPlayer var1x = var1.getSingleplayerServer().getPlayerList().getPlayer(var1.player.getUUID());
                        ArcanaSpell var2x = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
                        ItemStack var3x = new ItemStack(ItemRegistry.WIMPY_SPELL_BOOK.get());
                        ISpellContainerMutable var4x = ISpellContainer.create(1, true, true).mutableCopy();
                        var4x.addSpell(var2x, 1, false);
                        ISpellContainer.set(var3x, var4x.toImmutable());
                        CuriosApi.getCuriosInventory(var1x).orElseThrow().getStacksHandler("spellbook").orElseThrow().getStacks().setStackInSlot(0, var3x);
                        var1x.getInventory().setItem(0, ItemStack.EMPTY);
                        var1x.containerMenu.broadcastChanges();
                        ArcanaCooldowns.begin(var1x, var2x, CastSource.SPELLBOOK);
                    });
                }

                if (var2 == 345) {
                    ClientMagicData.updateSpellSelectionManager();
                    if (ClientMagicData.getSpellSelectionManager().getAllSpells().isEmpty()) {
                        throw new IllegalStateException("Equipped spellbook not synchronized to native selector");
                    }
                }

                if (var2 == 350) {
                    ArcanaSpell var14 = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
                    CooldownInstance var16 = ClientMagicData.getCooldowns().getSpellCooldowns().get(var14.getSpellId());
                    if (var16 == null || var16.getCooldownRemaining() <= 0) {
                        throw new IllegalStateException("Native client cooldown not received");
                    }

                    capture(var1, "09-irons-breaker-cooldown.png");
                    System.out.println("FRIEREN_VISUAL_SMOKE Native Iron client cooldown received: " + var16.getCooldownRemaining());
                }

                if (var2 == 352) {
                    SpellWheelOverlay.instance.open();
                }

                if (var2 == 357) {
                    capture(var1, "10-irons-cooldown-wheel.png");
                }

                if (var2 == 362) {
                    SpellWheelOverlay.instance.close();
                }

                if (var2 == 365) {
                    try {
                        Files.writeString(
                            var1.gameDirectory.toPath().resolve("visual-smoke/result.txt"),
                            "PASS: actual client rendered ten screenshots; rain boundary classification, skippable camera restoration and native Iron cooldown reception passed.\n"
                        );
                    } catch (Exception var8) {
                        throw new IllegalStateException(var8);
                    }

                    System.out.println("FRIEREN_VISUAL_SMOKE completed models, rain, dome, fracture, flight, effects and skip");
                    var1.stop();
                }
            }
        }
    }

    private static void vector(CompoundTag var0, String var1, Vec3 var2) {
        var0.putDouble(var1 + "X", var2.x);
        var0.putDouble(var1 + "Y", var2.y);
        var0.putDouble(var1 + "Z", var2.z);
    }

    private static CompoundTag field(boolean var0) {
        CompoundTag var1 = new CompoundTag();
        var1.putUUID("id", fieldId);
        var1.putUUID("owner", UUID.nameUUIDFromBytes("visual-smoke-owner".getBytes()));
        vector(var1, "center", CENTER);
        var1.putDouble("x", CENTER.x);
        var1.putDouble("y", CENTER.y);
        var1.putDouble("z", CENTER.z);
        var1.putInt("radius", 16);
        var1.putBoolean("defensive", var0);
        return var1;
    }

    private static void fields(Minecraft var0, boolean var1) {
        CompoundTag var2 = new CompoundTag();
        ListTag var3 = new ListTag();
        var3.add(field(var1));
        var2.put("fields", var3);
        var0.getSingleplayerServer().execute(() -> {
            ServerLevel var2x = var0.getSingleplayerServer().overworld();
            var2x.getDataStorage().set("frieren_arcana_barriers", BarrierData.load(var2, var2x.registryAccess()));
            ArcanaNetwork.syncFields(var2x);
        });
    }

    private static void setup(Minecraft var0) {
        var0.getSingleplayerServer().execute(() -> {
            IntegratedServer var1 = var0.getSingleplayerServer();
            ServerLevel var2 = var1.overworld();
            var2.getDataStorage().set("frieren_arcana_barriers", new BarrierData());

            for (int var3 = -45; var3 <= 45; var3++) {
                for (int var4 = -45; var4 <= 45; var4++) {
                    var2.setBlock(new BlockPos(var3, 65, var4), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }
            }

            var2.setDayTime(7000L);
            var2.setWeatherParameters(0, 12000, true, false);
            ServerPlayer var5 = var1.getPlayerList().getPlayer(var0.player.getUUID());
            if (var5 != null) {
                var5.getAbilities().mayfly = true;
                var5.getAbilities().flying = true;
                var5.onUpdateAbilities();
                var5.teleportTo(0.0, 77.0, -2.0);
                var5.getInventory().setItem(0, StaffView.demo());
                CuriosApi.getCuriosInventory(var5).orElseThrow().getStacksHandler("spellbook").orElseThrow().getStacks().setStackInSlot(0, ItemStack.EMPTY);
            }
        });
        fields(var0, false);
    }

    private static void scene(Minecraft var0, Vec3 var1, float var2, float var3, boolean var4) {
        var0.player.setPos(var1.x, var1.y, var1.z);
        var0.player.setYRot(var2);
        var0.player.setXRot(var3);
        var0.options.setCameraType(CameraType.FIRST_PERSON);
        var0.level.setRainLevel(var4 ? 1.0F : 0.0F);
        var0.level.setThunderLevel(0.0F);
        var0.getSingleplayerServer().execute(() -> {
            IntegratedServer var4x = var0.getSingleplayerServer();
            ServerLevel var5 = var4x.overworld();
            BarrierData var6 = BarrierData.get(var5);
            var5.getDataStorage().set("frieren_arcana_barriers", new BarrierData());
            ServerPlayer var7 = var4x.getPlayerList().getPlayer(var0.player.getUUID());
            if (var7 != null) {
                var7.teleportTo(var1.x, var1.y, var1.z);
                var7.setYRot(var2);
                var7.setXRot(var3);
            }

            var5.getDataStorage().set("frieren_arcana_barriers", var6);
            ArcanaNetwork.syncFields(var5);
        });
    }

    private static void capture(Minecraft var0, String var1) {
        pendingCapture = var1;
    }

    @SubscribeEvent
    public static void rendered(net.neoforged.neoforge.client.event.RenderGuiEvent.Post var0) {
        if (pendingCapture != null) {
            Minecraft var1 = Minecraft.getInstance();
            String var2 = pendingCapture;
            pendingCapture = null;
            Path var3 = var1.gameDirectory.toPath().resolve("visual-smoke");

            try {
                Files.createDirectories(var3);

                try (NativeImage var4 = Screenshot.takeScreenshot(var1.getMainRenderTarget())) {
                    var4.writeToFile(var3.resolve(var2));
                }

                System.out.println("FRIEREN_VISUAL_SMOKE screenshot " + var2);
            } catch (Exception var9) {
                throw new IllegalStateException(var9);
            }
        }
    }

    private static final class Gallery extends Screen {
        Gallery() {
            super(Component.literal("Frieren Arcana 1.3 — native 3D assets"));
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public void render(GuiGraphics var1, int var2, int var3, float var4) {
            var1.fill(0, 0, this.width, this.height, -15722457);
            var1.drawString(this.font, this.title, 20, 18, 14871295);
            var1.drawString(
                this.font, Component.literal("Actual Minecraft OBJ item rendering • faceted ruby, wrapped grip, armillary devices"), 20, 34, 9676740
            );
            ArrayList var5 = new ArrayList();
            var5.add(StaffView.demo());

            for (DeferredItem var7 : FrierenArcana.DEVICES) {
                var5.add(((Item)var7.get()).getDefaultInstance());
            }

            var5.add(FrierenArcana.RELEASE.get().getDefaultInstance());

            for (int var10 = 0; var10 < var5.size(); var10++) {
                int var11 = 28 + var10 % 4 * (this.width - 56) / 4;
                int var8 = 65 + var10 / 4 * 135;
                int var9 = (this.width - 56) / 4;
                var1.fill(var11 - 4, var8 - 4, var11 + var9 - 12, var8 + 121, -15195079);
                var1.pose().pushPose();
                var1.pose().translate((float)(var11 + 7), (float)(var8 + 5), 0.0F);
                var1.pose().scale(6.0F, 6.0F, 6.0F);
                var1.renderItem((ItemStack)var5.get(var10), 0, 0);
                var1.pose().popPose();
                var1.drawString(
                    this.font,
                    Component.literal(var10 == 0 ? "Flight Staff" : (var10 == 6 ? "Release Sigil" : "Barrier Device " + var10)),
                    var11,
                    var8 + 105,
                    13885943
                );
            }
        }
    }
}
