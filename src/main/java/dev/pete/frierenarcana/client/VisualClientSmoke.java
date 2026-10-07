package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.pete.frierenarcana.ArcanaCooldowns;
import dev.pete.frierenarcana.ArcanaNetwork;
import dev.pete.frierenarcana.ArcanaSpell;
import dev.pete.frierenarcana.BarrierData;
import dev.pete.frierenarcana.FrierenArcana;
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
import java.util.List;
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
    public static void tick(Post event) {
        if (Boolean.getBoolean("frieren_arcana.visualClientSmoke")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null && mc.getSingleplayerServer() != null) {
                int t = ++tick;
                if (t == 5) {
                    mc.setScreen(new VisualClientSmoke.Gallery());
                    setup(mc);
                }

                if (t == 35) {
                    capture(mc, "01-3d-items.png");
                }

                if (t == 45) {
                    mc.setScreen(null);
                }

                if (t == 60) {
                    scene(mc, new Vec3(0.0, 78.0, -38.0), 0.0F, 8.0F, false);
                }

                if (t == 90) {
                    capture(mc, "02-round-exam-dome.png");
                }

                if (t == 100) {
                    scene(mc, new Vec3(0.0, 76.0, -2.0), 0.0F, 0.0F, true);
                }

                if (t == 125) {
                    if (!ArcanaClient.rainBlocked(CENTER) || ArcanaClient.rainHeight(0, 0, 65) < 90) {
                        throw new IllegalStateException("Rain roof absent");
                    }

                    if (ArcanaClient.rainBlocked(new Vec3(30.0, 75.0, 0.0))) {
                        throw new IllegalStateException("Outside rain incorrectly blocked");
                    }

                    capture(mc, "03-dry-interior.png");
                    System.out.println("FRIEREN_VISUAL_SMOKE Rain roof and inside/outside containment passed");
                }

                if (t == 140) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("kind", "charge");
                    tag.putUUID("player", mc.player.getUUID());
                    tag.putBoolean("active", true);
                    tag.putBoolean("breaker", true);
                    ArcanaClient.receive(tag);
                }

                if (t == 180) {
                    capture(mc, "04-skippable-cutscene.png");
                }

                if (t == 185) {
                    if (!ArcanaCinematic.active()) {
                        throw new IllegalStateException("Cutscene did not start");
                    }

                    ArcanaCinematic.skip();
                    if (ArcanaCinematic.active() || mc.getCameraEntity() != mc.player) {
                        throw new IllegalStateException("Skip failed to restore player camera");
                    }

                    System.out.println("FRIEREN_VISUAL_SMOKE Cutscene start, skip and camera restoration passed");
                    CompoundTag tag = new CompoundTag();
                    tag.putString("kind", "charge");
                    tag.putUUID("player", mc.player.getUUID());
                    tag.putBoolean("active", false);
                    ArcanaClient.receive(tag);
                }

                if (t == 195) {
                    scene(mc, new Vec3(0.0, 78.0, -38.0), 0.0F, 8.0F, false);
                }

                if (t == 210) {
                    CompoundTag beam = new CompoundTag();
                    beam.putString("kind", "beam");
                    vector(beam, "start", CENTER.add(0.0, 0.0, -5.0));
                    vector(beam, "end", CENTER.add(0.0, 16.0, 0.0));
                    beam.putInt("style", 3);
                    ArcanaClient.receive(beam);
                    CompoundTag shatter = new CompoundTag();
                    shatter.putString("kind", "shatter");
                    shatter.put("field", field(false));
                    vector(shatter, "impact", CENTER.add(0.0, 16.0, 0.0));
                    ArcanaClient.receive(shatter);
                    mc.getSingleplayerServer()
                        .execute(() -> BarrierData.get(mc.getSingleplayerServer().overworld()).remove(mc.getSingleplayerServer().overworld(), fieldId, false));
                }

                if (t == 212) {
                    capture(mc, "05-green-breaker-fracture.png");
                }

                if (t == 220) {
                    fields(mc, true);
                    scene(mc, new Vec3(0.0, 78.0, -38.0), 0.0F, 8.0F, false);
                }

                if (t == 245) {
                    capture(mc, "06-defensive-honeycomb.png");
                }

                if (t == 255) {
                    scene(mc, new Vec3(0.0, 77.0, -9.0), 0.0F, 6.0F, false);
                    mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    CompoundTag f = new CompoundTag();
                    f.putString("kind", "flight");
                    f.putUUID("player", mc.player.getUUID());
                    f.putBoolean("active", true);
                    ArcanaClient.receive(f);
                }

                if (t >= 255 && t <= 285 && t % 5 == 0) {
                    CompoundTag f = new CompoundTag();
                    f.putString("kind", "flight");
                    f.putUUID("player", mc.player.getUUID());
                    f.putBoolean("active", true);
                    ArcanaClient.receive(f);
                }

                if (t == 277) {
                    capture(mc, "07-flight-staff.png");
                }

                if (t == 290) {
                    scene(mc, new Vec3(0.0, 78.0, -14.0), 0.0F, 9.0F, false);

                    for (int style : new int[]{11, 13, 14, 17, 8, 9}) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("kind", "effect");
                        vector(tag, "center", CENTER.add((double)((style % 3 - 1) * 3), 0.0, (double)(style % 2 * 4)));
                        tag.putInt("style", style);
                        tag.putInt("strength", 3);
                        ArcanaClient.receive(tag);
                    }
                }

                if (t == 295) {
                    capture(mc, "08-volumetric-spells.png");
                }

                if (t == 310) {
                    mc.setScreen(null);
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                    fields(mc, false);
                }

                if (t == 315) {
                    mc.getSingleplayerServer().execute(() -> {
                        ServerPlayer p = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                        ArcanaSpell spell = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
                        ItemStack book = new ItemStack(ItemRegistry.WIMPY_SPELL_BOOK.get());
                        ISpellContainerMutable container = ISpellContainer.create(1, true, true).mutableCopy();
                        container.addSpell(spell, 1, false);
                        ISpellContainer.set(book, container.toImmutable());
                        CuriosApi.getCuriosInventory(p).orElseThrow().getStacksHandler("spellbook").orElseThrow().getStacks().setStackInSlot(0, book);
                        p.getInventory().setItem(0, ItemStack.EMPTY);
                        p.containerMenu.broadcastChanges();
                        ArcanaCooldowns.begin(p, spell, CastSource.SPELLBOOK);
                    });
                }

                if (t == 345) {
                    ClientMagicData.updateSpellSelectionManager();
                    if (ClientMagicData.getSpellSelectionManager().getAllSpells().isEmpty()) {
                        throw new IllegalStateException("Equipped spellbook not synchronized to native selector");
                    }
                }

                if (t == 350) {
                    ArcanaSpell spell = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
                    CooldownInstance cd = ClientMagicData.getCooldowns().getSpellCooldowns().get(spell.getSpellId());
                    if (cd == null || cd.getCooldownRemaining() <= 0) {
                        throw new IllegalStateException("Native client cooldown not received");
                    }

                    capture(mc, "09-irons-breaker-cooldown.png");
                    System.out.println("FRIEREN_VISUAL_SMOKE Native Iron client cooldown received: " + cd.getCooldownRemaining());
                }

                if (t == 352) {
                    SpellWheelOverlay.instance.open();
                }

                if (t == 357) {
                    capture(mc, "10-irons-cooldown-wheel.png");
                }

                if (t == 362) {
                    SpellWheelOverlay.instance.close();
                }

                if (t == 365) {
                    try {
                        Files.writeString(
                            mc.gameDirectory.toPath().resolve("visual-smoke/result.txt"),
                            "PASS: actual client rendered ten screenshots; rain boundary classification, skippable camera restoration and native Iron cooldown reception passed.\n"
                        );
                    } catch (Exception var8) {
                        throw new IllegalStateException(var8);
                    }

                    System.out.println("FRIEREN_VISUAL_SMOKE completed models, rain, dome, fracture, flight, effects and skip");
                    mc.stop();
                }
            }
        }
    }

    private static void vector(CompoundTag root, String key, Vec3 v) {
        root.putDouble(key + "X", v.x);
        root.putDouble(key + "Y", v.y);
        root.putDouble(key + "Z", v.z);
    }

    private static CompoundTag field(boolean defensive) {
        CompoundTag f = new CompoundTag();
        f.putUUID("id", fieldId);
        f.putUUID("owner", UUID.nameUUIDFromBytes("visual-smoke-owner".getBytes()));
        vector(f, "center", CENTER);
        f.putDouble("x", CENTER.x);
        f.putDouble("y", CENTER.y);
        f.putDouble("z", CENTER.z);
        f.putInt("radius", 16);
        f.putBoolean("defensive", defensive);
        return f;
    }

    private static void fields(Minecraft mc, boolean defensive) {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        list.add(field(defensive));
        root.put("fields", list);
        mc.getSingleplayerServer().execute(() -> {
            ServerLevel level = mc.getSingleplayerServer().overworld();
            level.getDataStorage().set("frieren_arcana_barriers", BarrierData.load(root, level.registryAccess()));
            ArcanaNetwork.syncFields(level);
        });
    }

    private static void setup(Minecraft mc) {
        mc.getSingleplayerServer().execute(() -> {
            IntegratedServer server = mc.getSingleplayerServer();
            ServerLevel level = server.overworld();
            level.getDataStorage().set("frieren_arcana_barriers", new BarrierData());

            for (int x = -45; x <= 45; x++) {
                for (int z = -45; z <= 45; z++) {
                    level.setBlock(new BlockPos(x, 65, z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                }
            }

            level.setDayTime(7000L);
            level.setWeatherParameters(0, 12000, true, false);
            ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (p != null) {
                p.getAbilities().mayfly = true;
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
                p.teleportTo(0.0, 77.0, -2.0);
                p.getInventory().setItem(0, FrierenArcana.STAFF.get().getDefaultInstance());
                CuriosApi.getCuriosInventory(p).orElseThrow().getStacksHandler("spellbook").orElseThrow().getStacks().setStackInSlot(0, ItemStack.EMPTY);
            }
        });
        fields(mc, false);
    }

    private static void scene(Minecraft mc, Vec3 position, float yaw, float pitch, boolean rain) {
        mc.player.setPos(position.x, position.y, position.z);
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        mc.level.setRainLevel(rain ? 1.0F : 0.0F);
        mc.level.setThunderLevel(0.0F);
        mc.getSingleplayerServer().execute(() -> {
            IntegratedServer server = mc.getSingleplayerServer();
            ServerLevel level = server.overworld();
            BarrierData data = BarrierData.get(level);
            level.getDataStorage().set("frieren_arcana_barriers", new BarrierData());
            ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (p != null) {
                p.teleportTo(position.x, position.y, position.z);
                p.setYRot(yaw);
                p.setXRot(pitch);
            }

            level.getDataStorage().set("frieren_arcana_barriers", data);
            ArcanaNetwork.syncFields(level);
        });
    }

    private static void capture(Minecraft mc, String name) {
        pendingCapture = name;
    }

    @SubscribeEvent
    public static void rendered(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
        if (pendingCapture != null) {
            Minecraft mc = Minecraft.getInstance();
            String name = pendingCapture;
            pendingCapture = null;
            Path dir = mc.gameDirectory.toPath().resolve("visual-smoke");

            try {
                Files.createDirectories(dir);

                try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                    image.writeToFile(dir.resolve(name));
                }

                System.out.println("FRIEREN_VISUAL_SMOKE screenshot " + name);
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
        public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
            g.fill(0, 0, this.width, this.height, -15722457);
            g.drawString(this.font, this.title, 20, 18, 14871295);
            g.drawString(this.font, Component.literal("Actual Minecraft OBJ item rendering • faceted ruby, wrapped grip, armillary devices"), 20, 34, 9676740);
            List<ItemStack> items = new ArrayList<>();
            items.add(FrierenArcana.STAFF.get().getDefaultInstance());

            for (DeferredItem<Item> item : FrierenArcana.DEVICES) {
                items.add(item.get().getDefaultInstance());
            }

            items.add(FrierenArcana.RELEASE.get().getDefaultInstance());

            for (int i = 0; i < items.size(); i++) {
                int x = 28 + i % 4 * (this.width - 56) / 4;
                int y = 65 + i / 4 * 135;
                int cell = (this.width - 56) / 4;
                g.fill(x - 4, y - 4, x + cell - 12, y + 121, -15195079);
                g.pose().pushPose();
                g.pose().translate((float)(x + 7), (float)(y + 5), 0.0F);
                g.pose().scale(6.0F, 6.0F, 6.0F);
                g.renderItem(items.get(i), 0, 0);
                g.pose().popPose();
                g.drawString(this.font, Component.literal(i == 0 ? "Flight Staff" : (i == 6 ? "Release Sigil" : "Barrier Device " + i)), x, y + 105, 13885943);
            }
        }
    }
}
