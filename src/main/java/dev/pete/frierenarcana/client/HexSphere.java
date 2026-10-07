package dev.pete.frierenarcana.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.Vec3;

public final class HexSphere {
    public static final List<HexSphere.Panel> PANELS = build();

    private static List<HexSphere.Panel> build() {
        double p = (1.0 + Math.sqrt(5.0)) / 2.0;
        ArrayList<Vec3> vertices = new ArrayList<>();
        double[][] seed = new double[][]{
            {-1.0, p, 0.0},
            {1.0, p, 0.0},
            {-1.0, -p, 0.0},
            {1.0, -p, 0.0},
            {0.0, -1.0, p},
            {0.0, 1.0, p},
            {0.0, -1.0, -p},
            {0.0, 1.0, -p},
            {p, 0.0, -1.0},
            {p, 0.0, 1.0},
            {-p, 0.0, -1.0},
            {-p, 0.0, 1.0}
        };

        for (double[] v : seed) {
            vertices.add(new Vec3(v[0], v[1], v[2]).normalize());
        }

        ArrayList<int[]> faces = new ArrayList<>();
        int[][] fs = new int[][]{
            {0, 11, 5},
            {0, 5, 1},
            {0, 1, 7},
            {0, 7, 10},
            {0, 10, 11},
            {1, 5, 9},
            {5, 11, 4},
            {11, 10, 2},
            {10, 7, 6},
            {7, 1, 8},
            {3, 9, 4},
            {3, 4, 2},
            {3, 2, 6},
            {3, 6, 8},
            {3, 8, 9},
            {4, 9, 5},
            {2, 4, 11},
            {6, 2, 10},
            {8, 6, 7},
            {9, 8, 1}
        };
        faces.addAll(Arrays.asList(fs));

        for (int iteration = 0; iteration < 2; iteration++) {
            HashMap<Long, Integer> cache = new HashMap<>();
            ArrayList<int[]> next = new ArrayList<>();

            for (int[] face : faces) {
                int a = midpoint(face[0], face[1], vertices, cache);
                int b = midpoint(face[1], face[2], vertices, cache);
                int c = midpoint(face[2], face[0], vertices, cache);
                next.add(new int[]{face[0], a, c});
                next.add(new int[]{face[1], b, a});
                next.add(new int[]{face[2], c, b});
                next.add(new int[]{a, b, c});
            }

            faces = next;
        }

        ArrayList<HexSphere.Panel> panels = new ArrayList<>();

        for (int i = 0; i < vertices.size(); i++) {
            int index = i;
            Vec3 normal = vertices.get(i);
            Vec3 u = normal.cross(Math.abs(normal.y) < 0.95 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
            Vec3 v = normal.cross(u);
            ArrayList<Vec3> corners = new ArrayList<>();

            for (int[] f : faces) {
                if (f[0] == index || f[1] == index || f[2] == index) {
                    corners.add(vertices.get(f[0]).add(vertices.get(f[1])).add(vertices.get(f[2])).normalize());
                }
            }

            corners.sort(Comparator.comparingDouble(cx -> Math.atan2(cx.dot(v), cx.dot(u))));
            panels.add(new HexSphere.Panel(normal, List.copyOf(corners)));
        }

        return List.copyOf(panels);
    }

    private static int midpoint(int a, int b, List<Vec3> vertices, Map<Long, Integer> cache) {
        long key = (long)Math.min(a, b) << 32 | (long)Math.max(a, b);
        return cache.computeIfAbsent(key, k -> {
            vertices.add(vertices.get(a).add(vertices.get(b)).normalize());
            return vertices.size() - 1;
        });
    }

    private HexSphere() {
    }

    public static record Panel(Vec3 normal, List<Vec3> corners) {
    }
}
