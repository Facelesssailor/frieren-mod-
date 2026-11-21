package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import dev.pete.frierenarcana.ArcanaEvents;
import dev.pete.frierenarcana.ArcanaSpell;
import dev.pete.frierenarcana.BarrierData;
import dev.pete.frierenarcana.FrierenArcana;
import dev.pete.frierenarcana.StaffView;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slf4j.Logger;

/**
 * Films the Barrier Breaker cutscene inside the real game so it can be checked frame by frame against Frieren ep. 21.
 *
 * <p>Enabled only with {@code -Dfrieren_arcana.cinemaCapture=true} (the {@code runCapture} Gradle task, run headless on
 * CI under Xvfb + Mesa). It creates a fresh world, walks to a forest at dusk in the rain (the episode's setting), raises an
 * examination barrier around the player and casts Barrier Breaker through Iron's own casting path. From the moment the
 * cast starts the cutscene clock ({@link ArcanaClock}) runs virtually: every rendered frame is exactly one film frame
 * (1/24 s) later than the last, and the server tick rate is matched to it, so the frames land on the episode's timestamps
 * however slowly a software renderer draws. Every frame is written as a JPEG with its timing in {@code frames.csv}.
 */
@EventBusSubscriber(modid = "frieren_arcana", value = Dist.CLIENT)
public final class CinemaCapture {
    private static final Logger LOG = LogUtils.getLogger();
    private static final boolean ENABLED = Boolean.getBoolean("frieren_arcana.cinemaCapture");
    private static final long FRAME_NANOS = 1_000_000_000L / 24L;
    private static final String WORLD = "arcana-capture";
    private static final long SEED = 2120L;
    private static final int RADIUS = 40;
    /** Seconds after release (episode 15:03 + u) that also go to the stills folder for the side-by-side sheet. */
    private static final double[] STILL_U = {0.25, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.75, 5.25, 6.0, 7.0, 8.0, 8.75, 10.0, 12.0, 13.5, 14.5,
        15.25, 16.0, 18.0, 21.0, 24.0, 28.0, 31.0};
    /** Seconds after the charge began that go to the stills folder. */
    private static final double[] STILL_S = {0.5, 1.5, 2.5, 4.0, 5.5, 7.0, 8.5, 9.5};

    private enum Phase { TITLE, LOADING, SETUP, SETTLE, CAST, FILM, AFTER, DONE }

    private static Phase phase = Phase.TITLE;
    private static int ticks;
    private static int phaseTicks;
    private static long phaseReal;
    private static boolean worldRequested;
    private static volatile boolean setupDone;
    private static volatile String setupNote = "";
    private static boolean simulated;
    private static long castNanos;
    private static long releaseNanos;
    private static int frame;
    private static int afterFrames;
    private static Path dir;
    private static Writer csv;
    private static ExecutorService writer;
    private static final Semaphore QUEUE = new Semaphore(12);
    private static long rateWindowReal;
    private static int rateWindowFrames;
    private static float tickRate = 20.0F;
    private static long startedReal;
    private static int settleFrames;
    private static long settleReal;

    private CinemaCapture() {
    }

    // ------------------------------------------------------------------------------------------------ world and stage

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ENABLED || phase == Phase.DONE) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        phaseTicks++;
        if (startedReal == 0L) {
            startedReal = System.nanoTime();
        }
        if ((System.nanoTime() - startedReal) / 1_000_000_000L > 30L * 60L) {
            fail(mc, "watchdog: capture took longer than 30 minutes in phase " + phase);
            return;
        }
        switch (phase) {
            case TITLE -> {
                boolean title = mc.screen instanceof TitleScreen && mc.getOverlay() == null;
                if (!worldRequested && (title || phaseTicks > 600 && mc.level == null && mc.getOverlay() == null)) {
                    worldRequested = true;
                    log("creating world " + WORLD + " (seed " + SEED + ") from " + (mc.screen == null ? "no screen" : mc.screen.getClass().getSimpleName()));
                    GameRules rules = new GameRules();
                    LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT);
                    mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(SEED, false, false),
                        WorldPresets::createNormalWorldDimensions, mc.screen);
                    next(Phase.LOADING);
                }
            }
            case LOADING -> {
                if (mc.level != null && mc.player != null && mc.getSingleplayerServer() != null && mc.screen == null && phaseTicks > 40) {
                    log("world loaded; staging the forest");
                    stage(mc);
                    next(Phase.SETUP);
                }
            }
            case SETUP -> {
                // wait for the server to finish the stage, then for the chunks around the player to be meshed
                boolean meshed = mc.levelRenderer.hasRenderedAllSections();
                long realSeconds = (System.nanoTime() - phaseReal) / 1_000_000_000L;
                if (setupDone && (meshed && realSeconds > 20L || realSeconds > 150L)) {
                    log("stage ready after " + realSeconds + " s (all sections meshed: " + meshed + "); " + setupNote
                        + "; " + mc.levelRenderer.getSectionStatistics());
                    next(Phase.SETTLE);
                }
            }
            case SETTLE -> {
                mc.player.setYRot(0.0F);
                mc.player.setXRot(0.0F);
                if (phaseTicks > 60 && settleFrames > 20) {
                    // start the cast with the tick rate already matched to the renderer, so the 10 s charge is 10 film seconds
                    double fps = settleFrames / ((System.nanoTime() - settleReal) / 1.0E9);
                    tickRate = (float)Math.max(1.0, Math.min(20.0, 20.0 * fps / 24.0));
                    log(String.format(Locale.ROOT, "render %.2f fps before the cast -> tick rate %.2f", fps, tickRate));
                    open(mc);
                    ArcanaClock.startVirtual();
                    rateWindowReal = System.nanoTime();
                    rateWindowFrames = 0;
                    castNanos = ArcanaClock.nanos();
                    cast(mc, tickRate);
                    next(Phase.CAST);
                }
            }
            case CAST, FILM, AFTER -> {
            }
            default -> {
            }
        }
    }

    private static void next(Phase p) {
        phase = p;
        phaseTicks = 0;
        phaseReal = System.nanoTime();
    }

    /** Server side: forest at dusk, rain, a small clearing, the staff, full mana and an examination barrier. */
    private static void stage(Minecraft mc) {
        IntegratedServer server = mc.getSingleplayerServer();
        java.util.UUID id = mc.player.getUUID();
        server.execute(() -> {
            try {
                ServerLevel level = server.overworld();
                ServerPlayer sp = server.getPlayerList().getPlayer(id);
                GameRules rules = level.getGameRules();
                rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
                rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                rules.getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(false, server);
                // the episode: last light of the evening, rain outside the barrier
                level.setDayTime(13600L);
                level.setWeatherParameters(0, 1_000_000, true, false);
                BlockPos origin = sp.blockPosition();
                com.mojang.datafixers.util.Pair<BlockPos, Holder<Biome>> found =
                    level.findClosestBiome3d(h -> h.is(Biomes.FOREST), origin, 6400, 32, 64);
                if (found == null) {
                    found = level.findClosestBiome3d(h -> h.is(BiomeTags.IS_FOREST), origin, 6400, 32, 64);
                }
                BlockPos at = found == null ? origin : found.getFirst();
                // walk a little way into the biome sample until the ground is dry land under trees
                BlockPos ground = groundAt(level, at.getX(), at.getZ());
                for (int tries = 0; tries < 24 && !level.getFluidState(ground.below()).isEmpty(); tries++) {
                    at = at.offset(12, 0, 7);
                    ground = groundAt(level, at.getX(), at.getZ());
                }
                // a narrow clearing so the close shots are not filled with leaves; the forest stays all around
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        if (dx * dx + dz * dz > 10) {
                            continue;
                        }
                        int gy = groundAt(level, ground.getX() + dx, ground.getZ() + dz).getY();
                        for (int y = gy; y < ground.getY() + 40; y++) {
                            BlockPos p = new BlockPos(ground.getX() + dx, y, ground.getZ() + dz);
                            BlockState s = level.getBlockState(p);
                            if (!s.isAir() && (s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES) || s.canBeReplaced())) {
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }
                }
                sp.setGameMode(GameType.SURVIVAL);
                sp.getAbilities().flying = false;
                sp.getAbilities().mayfly = false;
                sp.onUpdateAbilities();
                sp.teleportTo(level, ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5, 0.0F, 0.0F);
                sp.getInventory().setItem(0, StaffView.demo());
                sp.getInventory().selected = 0;
                sp.containerMenu.broadcastChanges();
                setupNote = "forest " + (found == null ? "NOT FOUND, used spawn" : found.getSecond().unwrapKey().map(k -> k.location().toString()).orElse("?"))
                    + " at " + ground.toShortString();
                setupDone = true;
            } catch (Throwable t) {
                LOG.error("FRIEREN_CAPTURE stage failed", t);
                setupNote = "stage failed: " + t;
                setupDone = true;
            }
        });
    }

    /** The first free block above the soil: below any canopy, trunk or undergrowth (heightmaps count trunks as ground). */
    private static BlockPos groundAt(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        while (y > level.getMinBuildHeight() + 1) {
            BlockState below = level.getBlockState(new BlockPos(x, y - 1, z));
            if (below.isAir() || below.is(BlockTags.LOGS) || below.is(BlockTags.LEAVES) || below.canBeReplaced() && below.getFluidState().isEmpty()) {
                y--;
            } else {
                break;
            }
        }
        return new BlockPos(x, y, z);
    }

    /** Raise the barrier around the player and cast Barrier Breaker the way Iron's /cast does; simulate it if that is refused. */
    private static void cast(Minecraft mc, float rate) {
        IntegratedServer server = mc.getSingleplayerServer();
        java.util.UUID id = mc.player.getUUID();
        server.execute(() -> {
            server.tickRateManager().setTickRate(rate);
            ServerLevel level = server.overworld();
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            boolean made = BarrierData.get(level).create(level, sp, RADIUS, false);
            MagicData magic = MagicData.getPlayerMagicData(sp);
            magic.setMana((float)sp.getAttributeValue(AttributeRegistry.MAX_MANA));
            ArcanaEvents.syncMana(sp);
            ArcanaSpell spell = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
            boolean ready = spell.ready(sp, 1, false);
            boolean started = ready && spell.attemptInitiateCast(ItemStack.EMPTY, 1, level, sp, CastSource.COMMAND, false, "command");
            log("barrier created: " + made + ", radius " + RADIUS + ", breaker ready: " + ready + ", cast started: " + started
                + ", casting: " + magic.isCasting() + " " + magic.getCastingSpellId());
            if (!started) {
                mc.execute(CinemaCapture::simulateCharge);
            }
        });
    }

    private static void simulateCharge() {
        Minecraft mc = Minecraft.getInstance();
        simulated = true;
        log("SIMULATED: Iron's refused the cast; driving the cutscene with the mod's own packets");
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", "charge");
        tag.putUUID("player", mc.player.getUUID());
        tag.putBoolean("active", true);
        tag.putBoolean("breaker", true);
        ArcanaClient.receive(tag);
    }

    private static void simulateRelease(Minecraft mc) {
        IntegratedServer server = mc.getSingleplayerServer();
        java.util.UUID id = mc.player.getUUID();
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", "charge");
        tag.putUUID("player", id);
        tag.putBoolean("active", false);
        ArcanaClient.receive(tag);
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            ArcanaSpell spell = FrierenArcana.SPELL_MAP.get(ArcanaSpell.Kind.PIERCE).get();
            ArcanaEvents.flags(sp).putBoolean("castApproved", true);
            spell.onCast(sp.serverLevel(), 1, sp, CastSource.COMMAND, MagicData.getPlayerMagicData(sp));
        });
    }

    // ------------------------------------------------------------------------------------------------ film clock

    @SubscribeEvent
    public static void frameStart(RenderFrameEvent.Pre event) {
        if (ENABLED && phase == Phase.SETTLE) {
            if (settleFrames++ == 0) {
                settleReal = System.nanoTime();
            }
        }
        if (!ENABLED || !ArcanaClock.isVirtual() || phase == Phase.DONE) {
            return;
        }
        ArcanaClock.advance(FRAME_NANOS);
        // keep 20 game ticks per film second: the tick rate follows how fast the software renderer is drawing
        if (++rateWindowFrames >= 12) {
            long now = System.nanoTime();
            double fps = rateWindowFrames / ((now - rateWindowReal) / 1.0E9);
            float want = (float)Math.max(1.0, Math.min(20.0, 20.0 * fps / 24.0));
            if (Math.abs(want - tickRate) / tickRate > 0.08F) {
                tickRate = want;
                IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
                if (server != null) {
                    float rate = want;
                    server.execute(() -> server.tickRateManager().setTickRate(rate));
                }
                log(String.format(Locale.ROOT, "render %.2f fps -> tick rate %.2f", fps, want));
            }
            rateWindowReal = now;
            rateWindowFrames = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void frameEnd(RenderGuiEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (phase == Phase.CAST || phase == Phase.FILM || phase == Phase.AFTER) {
            film(mc);
        }
    }

    private static void film(Minecraft mc) {
        long now = ArcanaClock.nanos();
        double s = (now - castNanos) / 1.0E9;
        if (phase == Phase.CAST) {
            if (ArcanaCinematic.active()) {
                log(String.format(Locale.ROOT, "cutscene started %.3f s after the cast (breaker cutscene: %s)", s, ArcanaCinematic.breaker));
                next(Phase.FILM);
            } else if (s > 4.0) {
                fail(mc, "the cutscene never started");
                return;
            }
        }
        if (phase == Phase.FILM && releaseNanos == 0L) {
            if (ArcanaCinematic.release != 0L) {
                releaseNanos = ArcanaCinematic.release;
                log(String.format(Locale.ROOT, "released %.3f s after the cast (charge %.3f s)", (releaseNanos - castNanos) / 1.0E9,
                    (releaseNanos - ArcanaCinematic.started) / 1.0E9));
            } else if (simulated && (now - ArcanaCinematic.started) / 1.0E9 >= 10.0) {
                simulateRelease(mc);
            } else if (s > 25.0) {
                log("no release after 25 s; forcing it");
                simulateRelease(mc);
            }
        }
        double u = releaseNanos == 0L ? Double.NaN : (now - releaseNanos) / 1.0E9;
        if (phase == Phase.FILM && !Double.isNaN(u) && (!ArcanaCinematic.active() && u > 1.0 || u > 36.0)) {
            log(String.format(Locale.ROOT, "cutscene ended at u = %.3f", u));
            next(Phase.AFTER);
        }
        if (phase == Phase.AFTER && ++afterFrames > 72) {
            finish(mc);
            return;
        }
        double charge = ArcanaCinematic.started == 0L ? Double.NaN : (now - ArcanaCinematic.started) / 1.0E9;
        String still = null;
        if (Double.isNaN(u)) {
            for (double t : STILL_S) {
                if (!Double.isNaN(charge) && Math.round(charge * 24.0) == Math.round(t * 24.0)) {
                    still = String.format(Locale.ROOT, "s_%05.2f", t);
                }
            }
        } else {
            for (double t : STILL_U) {
                if (Math.round(u * 24.0) == Math.round(t * 24.0)) {
                    still = String.format(Locale.ROOT, "u_%05.2f", t);
                }
            }
        }
        grab(mc, frame, still);
        try {
            csv.write(String.format(Locale.ROOT, "%d,%.4f,%s,%s,%d,%s,%.2f%n", frame, s, Double.isNaN(charge) ? "" : String.format(Locale.ROOT, "%.4f", charge),
                Double.isNaN(u) ? "" : String.format(Locale.ROOT, "%.4f", u), CinemaDirector.currentShot(), still == null ? "" : still, tickRate));
        } catch (IOException e) {
            LOG.error("FRIEREN_CAPTURE csv", e);
        }
        frame++;
    }

    // ------------------------------------------------------------------------------------------------ output

    private static void open(Minecraft mc) {
        try {
            dir = mc.gameDirectory.toPath().resolve("arcana-capture");
            Files.createDirectories(dir.resolve("frames"));
            Files.createDirectories(dir.resolve("stills"));
            csv = Files.newBufferedWriter(dir.resolve("frames.csv"), StandardCharsets.UTF_8);
            csv.write("frame,seconds_since_cast,seconds_since_charge,u_since_release,shot,still,tick_rate\n");
            writer = Executors.newFixedThreadPool(3, r -> {
                Thread t = new Thread(r, "frieren-capture-writer");
                t.setDaemon(true);
                return t;
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void grab(Minecraft mc, int index, String still) {
        int w;
        int h;
        int[] rgb;
        try (NativeImage img = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            w = img.getWidth();
            h = img.getHeight();
            rgb = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = img.getPixelRGBA(x, y);
                    rgb[y * w + x] = (c & 0xFF) << 16 | (c >> 8 & 0xFF) << 8 | c >> 16 & 0xFF;
                }
            }
        }
        if (index == 0) {
            log("frame size " + w + "x" + h);
        }
        QUEUE.acquireUninterruptibly();
        int fw = w;
        int fh = h;
        int[] px = rgb;
        writer.execute(() -> {
            try {
                BufferedImage image = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_RGB);
                image.setRGB(0, 0, fw, fh, px, 0, fw);
                jpeg(image, dir.resolve("frames").resolve(String.format(Locale.ROOT, "%05d.jpg", index)), 0.88F);
                if (still != null) {
                    jpeg(image, dir.resolve("stills").resolve(still + ".jpg"), 0.93F);
                }
            } catch (Throwable t) {
                LOG.error("FRIEREN_CAPTURE frame " + index, t);
            } finally {
                QUEUE.release();
            }
        });
    }

    private static void jpeg(BufferedImage image, Path file, float quality) throws IOException {
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter jw = it.next();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(file.toFile())) {
            jw.setOutput(out);
            ImageWriteParam param = jw.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            jw.write(null, new IIOImage(image, null, null), param);
        } finally {
            jw.dispose();
        }
    }

    private static void finish(Minecraft mc) {
        phase = Phase.DONE;
        try {
            writer.shutdown();
            writer.awaitTermination(5, TimeUnit.MINUTES);
            csv.close();
            Files.writeString(dir.resolve("result.txt"), "PASS: filmed " + frame + " frames of the Barrier Breaker cutscene"
                + (simulated ? " (SIMULATED cast)" : " (real Iron's cast)") + "; " + setupNote + "\n");
        } catch (Exception e) {
            LOG.error("FRIEREN_CAPTURE finish", e);
        }
        log("done: " + frame + " frames");
        mc.stop();
    }

    private static void fail(Minecraft mc, String why) {
        phase = Phase.DONE;
        log("FAIL " + why);
        try {
            Path d = mc.gameDirectory.toPath().resolve("arcana-capture");
            Files.createDirectories(d);
            Files.writeString(d.resolve("result.txt"), "FAIL: " + why + "; " + setupNote + "\n");
            if (csv != null) {
                csv.close();
            }
            if (writer != null) {
                writer.shutdown();
                writer.awaitTermination(1, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            LOG.error("FRIEREN_CAPTURE fail", e);
        }
        mc.stop();
    }

    private static void log(String message) {
        LOG.info("FRIEREN_CAPTURE {}", message);
    }
}
