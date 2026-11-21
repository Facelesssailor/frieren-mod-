package dev.pete.frierenarcana.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/**
 * Sweeps the Barrier Breaker cutscene's timeline and camera through every film frame (1/24 s), for a small, a default and
 * a large barrier, and fails the build on NaN, a camera sitting on its own target, a shot order that differs from the
 * episode's, or timeline curves that run the wrong way. Runs as part of {@code gradle check} (task timelineCheck).
 */
public final class TimelineSweep {
    private static final List<String> FAIL = new ArrayList<>();
    /** The order of shots after the release, as cut in ep. 21 15:03 - 15:42. */
    private static final int[] ORDER = {5, 7, 8, 6, 1, 14, 15, 2, 3, 4, 9};

    public static void main(String[] args) {
        for (int radius : new int[]{12, 40, 56}) {
            sweep(radius);
        }
        curves();
        if (!FAIL.isEmpty()) {
            FAIL.stream().limit(40).forEach(f -> System.err.println("TIMELINE FAIL " + f));
            System.err.println("TIMELINE: " + FAIL.size() + " problem(s)");
            System.exit(1);
        }
        System.out.println("TIMELINE: OK (camera and curves sound at every film frame, radii 12/40/56)");
    }

    private static boolean finite(Vec3 v) {
        return v != null && Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }

    private static void sweep(int radius) {
        Vec3 anchor = new Vec3(0.5, 71.3, 0.5);
        Vec3 flat = new Vec3(0.0, 0.0, 1.0);
        Vec3 side = new Vec3(-flat.z, 0.0, flat.x);
        Vec3 center = new Vec3(0.5, 70.5, 0.5);
        Vec3 hit = center.add(0.0, radius, 0.0);
        int[] shot = new int[1];
        // before the release
        for (int f = 0; f <= 12 * 24; f++) {
            double s = f / 24.0;
            Vec3[] cam = CinemaDirector.shot(s, 0.0, false, false, anchor, flat, side, center, radius, null, shot);
            check(radius, "charge", s, cam, anchor, radius);
        }
        // after it
        List<Integer> seen = new ArrayList<>();
        for (int f = 0; f <= (int)(BreakTimeline.END * 24.0); f++) {
            double u = f / 24.0;
            Vec3[] cam = CinemaDirector.shot(u + 10.0, u, true, true, anchor, flat, side, center, radius, hit, shot);
            check(radius, "release", u, cam, anchor, radius);
            if (seen.isEmpty() || seen.get(seen.size() - 1) != shot[0]) {
                seen.add(shot[0]);
            }
            double fov = CinemaDirector.fov(shot[0], 70.0);
            if (!(fov >= 5.0 && fov <= 110.0)) {
                FAIL.add(String.format("r=%d u=%.3f shot %d fov %.2f", radius, u, shot[0], fov));
            }
            float[] post = CinemaPost.params(u, shot[0], 720, cam[0].distanceTo(anchor));
            for (float p : post) {
                if (!Float.isFinite(p) || p < 0.0F) {
                    FAIL.add(String.format("r=%d u=%.3f post params %s", radius, u, java.util.Arrays.toString(post)));
                    break;
                }
            }
            float pitch = CinemaDirector.pitch(0.0F, u, true);
            if (!(pitch >= -90.0F && pitch <= 90.0F)) {
                FAIL.add(String.format("r=%d u=%.3f head pitch %.2f", radius, u, pitch));
            }
        }
        int[] got = seen.stream().mapToInt(Integer::intValue).toArray();
        if (!java.util.Arrays.equals(got, ORDER)) {
            FAIL.add("r=" + radius + " shot order " + seen + " differs from the episode's " + java.util.Arrays.toString(ORDER));
        }
    }

    private static void check(int radius, String phase, double t, Vec3[] cam, Vec3 anchor, int r) {
        if (cam == null || cam.length != 2 || !finite(cam[0]) || !finite(cam[1])) {
            FAIL.add(String.format("r=%d %s t=%.3f camera not finite", radius, phase, t));
            return;
        }
        if (cam[0].distanceTo(cam[1]) < 0.2) {
            FAIL.add(String.format("r=%d %s t=%.3f camera on its target", radius, phase, t));
        }
        if (cam[0].distanceTo(anchor) > r * 4.0 + 220.0) {
            FAIL.add(String.format("r=%d %s t=%.3f camera %.1f blocks away", radius, phase, t, cam[0].distanceTo(anchor)));
        }
    }

    private static void curves() {
        double prevNeedle = 0.0;
        double prevFront = 2.0;
        int prevFlick = 0;
        int[] flickFrames = new int[7];
        for (int f = 0; f <= 34 * 240; f++) {
            double u = f / 240.0;
            double n = BreakTimeline.needle(u);
            double b = BreakTimeline.bandFront(u);
            double p = BreakTimeline.bandProgress(u);
            if (!(n >= 0.0 && n <= 1.0) || n + 1e-9 < prevNeedle) {
                FAIL.add(String.format("needle(%.3f) = %.4f", u, n));
            }
            if (!Double.isFinite(b) || b > prevFront + 1e-9) {
                FAIL.add(String.format("bandFront(%.3f) = %.4f rises", u, b));
            }
            if (!(p >= 0.0 && p <= 1.0)) {
                FAIL.add(String.format("bandProgress(%.3f) = %.4f", u, p));
            }
            int k = BreakTimeline.flicker(u);
            if (k != 0 && k != prevFlick && k != prevFlick + 1) {
                FAIL.add(String.format("flicker jumps %d -> %d at %.3f", prevFlick, k, u));
            }
            if (k > 0) {
                flickFrames[k]++;
            }
            prevNeedle = n;
            prevFront = b;
            prevFlick = k;
        }
        // the bands start descending while the needle is still on its way up (15:09 - 15:12)
        if (!(BreakTimeline.needle(BreakTimeline.BANDS_BEGIN) < 0.6)) {
            FAIL.add("the needle is past 60% when the bands begin");
        }
        if (!(BreakTimeline.needle(BreakTimeline.APEX) >= 0.999)) {
            FAIL.add("the needle never reaches the apex");
        }
        for (int i = 1; i <= 5; i++) {
            if (flickFrames[i] == 0) {
                FAIL.add("flicker frame " + i + " never shows");
            }
        }
    }
}
