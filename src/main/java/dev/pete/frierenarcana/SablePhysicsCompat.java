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
                // $VF: Couldn't be decompiled
                // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
                // java.lang.RuntimeException: invalid constant type: Ljava/lang/Object; with value FrierenArcanaPhysicsBoundary
                //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent.toJava(ConstExprent.java:356)
                //   at org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchExprent.toJava(SwitchExprent.java:151)
                //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:1018)
                //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent.toJava(ExitExprent.java:86)
                //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:895)
                //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:90)
                //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
                //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
                //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
                //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:241)
                //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
                //   at org.jetbrains.java.decompiler.main.ClassWriter.methodLambdaToJava(ClassWriter.java:949)
                //
                // Bytecode:
                // 00: aload 2
                // 01: invokevirtual java/lang/reflect/Method.getDeclaringClass ()Ljava/lang/Class;
                // 04: ldc java/lang/Object
                // 06: if_acmpne a8
                // 09: aload 2
                // 0a: invokevirtual java/lang/reflect/Method.getName ()Ljava/lang/String;
                // 0d: astore 4
                // 0f: bipush -1
                // 10: istore 5
                // 12: aload 4
                // 14: invokevirtual java/lang/String.hashCode ()I
                // 17: lookupswitch 81 3 -1776922004 33 -1295482945 67 147696667 50
                // 38: aload 4
                // 3a: ldc_w "toString"
                // 3d: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
                // 40: ifeq 68
                // 43: bipush 0
                // 44: istore 5
                // 46: goto 68
                // 49: aload 4
                // 4b: ldc_w "hashCode"
                // 4e: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
                // 51: ifeq 68
                // 54: bipush 1
                // 55: istore 5
                // 57: goto 68
                // 5a: aload 4
                // 5c: ldc_w "equals"
                // 5f: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
                // 62: ifeq 68
                // 65: bipush 2
                // 66: istore 5
                // 68: iload 5
                // 6a: tableswitch 60 0 2 26 32 42
                // 84: ldc_w "FrierenArcanaPhysicsBoundary"
                // 87: goto a7
                // 8a: aload 1
                // 8b: invokestatic java/lang/System.identityHashCode (Ljava/lang/Object;)I
                // 8e: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
                // 91: goto a7
                // 94: aload 1
                // 95: aload 3
                // 96: bipush 0
                // 97: aaload
                // 98: if_acmpne 9f
                // 9b: bipush 1
                // 9c: goto a0
                // 9f: bipush 0
                // a0: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
                // a3: goto a7
                // a6: aconst_null
                // a7: areturn
                // a8: getstatic dev/pete/frierenarcana/SablePhysicsCompat.disabled Z
                // ab: ifne cb
                // ae: aload 3
                // af: bipush 0
                // b0: aaload
                // b1: iload 0
                // b2: invokestatic dev/pete/frierenarcana/SablePhysicsCompat.step (Ljava/lang/Object;Z)V
                // b5: goto cb
                // b8: astore 4
                // ba: bipush 1
                // bb: putstatic dev/pete/frierenarcana/SablePhysicsCompat.disabled Z
                // be: invokestatic com/mojang/logging/LogUtils.getLogger ()Lorg/slf4j/Logger;
                // c1: ldc_w "Frieren Arcana: disabled Sable boundary callback after API error"
                // c4: aload 4
                // c6: invokeinterface org/slf4j/Logger.error (Ljava/lang/String;Ljava/lang/Throwable;)V 3
                // cb: aconst_null
                // cc: areturn
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
