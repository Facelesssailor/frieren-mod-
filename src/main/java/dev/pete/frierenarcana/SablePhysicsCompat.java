package dev.pete.frierenarcana;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class SablePhysicsCompat {
    private static final Map<Object, SablePhysicsCompat.Snapshot> BEFORE = Collections.synchronizedMap(new IdentityHashMap<>());
    private static Method containerGetter;
    private static Method allShips;
    private static Method getLevel;
    private static Method getHandle;
    private static Method teleport;
    private static Method getLinear;
    private static Method getAngular;
    private static Method addVelocity;
    private static boolean disabled;

    public static void register() {
        if (ModList.get().isLoaded("sable")) {
            try {
                Class<?> platform = Class.forName("dev.ryanhcode.sable.platform.SableEventPlatform");
                Object events = platform.getField("INSTANCE").get(null);
                Class<?> pre = Class.forName("dev.ryanhcode.sable.api.event.SablePrePhysicsTickEvent");
                Class<?> post = Class.forName("dev.ryanhcode.sable.api.event.SablePostPhysicsTickEvent");
                Class<?> system = Class.forName("dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem");
                Class<?> ship = Class.forName("dev.ryanhcode.sable.sublevel.ServerSubLevel");
                Class<?> container = Class.forName("dev.ryanhcode.sable.api.sublevel.SubLevelContainer");
                Class<?> handle = Class.forName("dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle");
                containerGetter = container.getMethod("getContainer", ServerLevel.class);
                allShips = container.getMethod("getAllSubLevels");
                getLevel = system.getMethod("getLevel");
                getHandle = system.getMethod("getPhysicsHandle", ship);
                teleport = handle.getMethod("teleport", Vector3dc.class, Quaterniondc.class);
                getLinear = handle.getMethod("getLinearVelocity", Vector3d.class);
                getAngular = handle.getMethod("getAngularVelocity", Vector3d.class);
                addVelocity = handle.getMethod("addLinearAndAngularVelocity", Vector3dc.class, Vector3dc.class);
                platform.getMethod("onPhysicsTick", pre).invoke(events, callback(pre, true));
                platform.getMethod("onPostPhysicsTick", post).invoke(events, callback(post, false));
                LogUtils.getLogger().info("Frieren Arcana: Sable ship boundary callbacks registered");
            } catch (ReflectiveOperationException var8) {
                disabled = true;
                LogUtils.getLogger().error("Frieren Arcana: Sable physics API mismatch; ship barrier collision unavailable", (Throwable)var8);
            }
        }
    }

    private static Object callback(Class<?> type, boolean before) {
        return Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class[]{type},
            (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return switch (method.getName()) {
                        case "toString" -> "FrierenArcanaPhysicsBoundary";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> null;
                    };
                }
                if (!disabled) {
                    try {
                        step(args[0], before);
                    } catch (ReflectiveOperationException e) {
                        disabled = true;
                        LogUtils.getLogger().error("Frieren Arcana: disabled Sable boundary callback after API error", e);
                    }
                }
                return null;
            }
        );
    }

    private static void step(Object system, boolean before) throws ReflectiveOperationException {
        ServerLevel level = (ServerLevel)getLevel.invoke(system);
        List<BarrierData.Field> fields = List.copyOf(BarrierData.get(level).fields());
        if (!fields.stream().noneMatch(f -> !f.defensive)) {
            Object container = containerGetter.invoke(null, level);
            if (container != null) {
                for (Object raw : (Iterable)allShips.invoke(container)) {
                    SubLevelAccess ship = (SubLevelAccess)raw;
                    if (before) {
                        BEFORE.put(
                            raw,
                            new SablePhysicsCompat.Snapshot(
                                new Vector3d(ship.logicalPose().position()), new Quaterniond(ship.logicalPose().orientation()), corners(ship)
                            )
                        );
                    } else {
                        SablePhysicsCompat.Snapshot previous = BEFORE.remove(raw);
                        if (previous != null) {
                            List<Vec3> next = corners(ship);
                            boolean blocked = false;

                            for (BarrierData.Field f : fields) {
                                if (!f.defensive) {
                                    for (int i = 0; i < 8 && !blocked; i++) {
                                        Vec3 a = previous.corners.get(i).subtract(f.center);
                                        Vec3 d = next.get(i).subtract(previous.corners.get(i));
                                        blocked = Double.isFinite(BarrierGeometry.firstHit(a.x, a.y, a.z, d.x, d.y, d.z, (double)f.radius));
                                    }
                                }
                            }

                            if (blocked) {
                                Object handle = getHandle.invoke(system, raw);
                                teleport.invoke(handle, previous.position, previous.orientation);
                                Vector3d linear = (Vector3d)getLinear.invoke(handle, new Vector3d());
                                Vector3d angular = (Vector3d)getAngular.invoke(handle, new Vector3d());
                                addVelocity.invoke(handle, linear.negate(), angular.negate());
                            }
                        }
                    }
                }
            }
        }
    }

    private static List<Vec3> corners(SubLevelAccess ship) {
        BoundingBox3dc box = ship.boundingBox();
        ArrayList<Vec3> result = new ArrayList<>(8);

        for (int i = 0; i < 8; i++) {
            result.add(new Vec3((i & 1) == 0 ? box.minX() : box.maxX(), (i & 2) == 0 ? box.minY() : box.maxY(), (i & 4) == 0 ? box.minZ() : box.maxZ()));
        }

        return result;
    }

    private SablePhysicsCompat() {
    }

    private static record Snapshot(Vector3d position, Quaterniond orientation, List<Vec3> corners) {
    }
}
