import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/** Bytecode patches applied to original (not recompiled) classes of the mod.
 *  args: classesDir(in/out) classpath...  */
public class Patch {
  static URLClassLoader cl;
  static Path dir;
  static ClassNode read(String n) throws IOException { ClassNode cn = new ClassNode(); new ClassReader(Files.readAllBytes(dir.resolve(n + ".class"))).accept(cn, 0); return cn; }
  static void write(ClassNode cn) throws IOException {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
      protected ClassLoader getClassLoader() { return cl; }
    };
    cn.accept(cw);
    Files.write(dir.resolve(cn.name + ".class"), cw.toByteArray());
  }
  static MethodNode m(ClassNode cn, String name, String desc) {
    for (MethodNode mn : cn.methods) if (mn.name.equals(name) && mn.desc.equals(desc)) return mn;
    throw new IllegalStateException("no method " + cn.name + "." + name + desc);
  }
  static int ldc(ClassNode cn, String methodPrefix, double from, double to) {
    int n = 0;
    for (MethodNode mn : cn.methods) {
      if (!mn.name.startsWith(methodPrefix)) continue;
      for (AbstractInsnNode in : mn.instructions) if (in instanceof LdcInsnNode l && l.cst instanceof Double d && Math.abs(d - from) < 1e-9) { l.cst = to; n++; }
    }
    return n;
  }
  public static void main(String[] a) throws Exception {
    dir = Paths.get(a[0]);
    List<URL> urls = new ArrayList<>(); urls.add(dir.toUri().toURL());
    for (int i = 1; i < a.length; i++) urls.add(new File(a[i]).toURI().toURL());
    cl = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    double life = Double.parseDouble(System.getProperty("life")), beamLife = Double.parseDouble(System.getProperty("beamLife"));

    // ArcanaClient: rain hold + fracture lifetime
    ClassNode ac = read("dev/pete/frierenarcana/client/ArcanaClient");
    {
      MethodNode mn = m(ac, "rainBlocked", "(Lnet/minecraft/world/phys/Vec3;)Z");
      InsnList il = new InsnList(); LabelNode go = new LabelNode();
      il.add(new VarInsnNode(Opcodes.ALOAD, 0));
      il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/pete/frierenarcana/client/ClientRainHold", "blocked", "(Lnet/minecraft/world/phys/Vec3;)Z", false));
      il.add(new JumpInsnNode(Opcodes.IFEQ, go)); il.add(new InsnNode(Opcodes.ICONST_1)); il.add(new InsnNode(Opcodes.IRETURN)); il.add(go);
      mn.instructions.insert(il);
      MethodNode rh = m(ac, "rainHeight", "(III)I");
      InsnList h = new InsnList();
      h.add(new VarInsnNode(Opcodes.ILOAD, 0)); h.add(new VarInsnNode(Opcodes.ILOAD, 1)); h.add(new VarInsnNode(Opcodes.ILOAD, 2));
      h.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/pete/frierenarcana/client/ClientRainHold", "height", "(III)I", false));
      h.add(new VarInsnNode(Opcodes.ISTORE, 2));
      rh.instructions.insert(h);
      int n = ldc(ac, "lambda$render$", 23.7, life);
      if (n != 1) throw new IllegalStateException("fracture life literal count " + n);
      write(ac);
    }
    // SpellFx: breaker beam lifetime
    ClassNode sf = read("dev/pete/frierenarcana/client/SpellFx");
    {
      int n = ldc(sf, "beamLife", 11.5, beamLife); if (n != 1) throw new IllegalStateException("beam life literal count " + n);
      // style 21: one curved Zoltraak barrage shot, drawn by FernBarrageFx
      MethodNode lifeM = m(sf, "beamLife", "(I)D");
      InsnList l = new InsnList(); LabelNode skip = new LabelNode();
      l.add(new VarInsnNode(Opcodes.ILOAD, 0)); l.add(new IntInsnNode(Opcodes.BIPUSH, 21)); l.add(new JumpInsnNode(Opcodes.IF_ICMPNE, skip));
      l.add(new LdcInsnNode(0.62)); l.add(new InsnNode(Opcodes.DRETURN)); l.add(skip);
      lifeM.instructions.insert(l);
      MethodNode beam = m(sf, "beam", "(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;ILnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;D)Z");
      InsnList b = new InsnList(); LabelNode other = new LabelNode();
      b.add(new VarInsnNode(Opcodes.ILOAD, 2)); b.add(new IntInsnNode(Opcodes.BIPUSH, 21)); b.add(new JumpInsnNode(Opcodes.IF_ICMPNE, other));
      b.add(new VarInsnNode(Opcodes.ALOAD, 0)); b.add(new VarInsnNode(Opcodes.ALOAD, 1)); b.add(new VarInsnNode(Opcodes.ALOAD, 3)); b.add(new VarInsnNode(Opcodes.ALOAD, 4)); b.add(new VarInsnNode(Opcodes.DLOAD, 5));
      b.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/pete/frierenarcana/client/FernBarrageFx", "draw", "(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;D)V", false));
      b.add(new InsnNode(Opcodes.ICONST_1)); b.add(new InsnNode(Opcodes.IRETURN)); b.add(other);
      beam.instructions.insert(b);
      write(sf);
    }
    // ArcanaNetwork.shatter: remember the broken barrier on the server
    ClassNode an = read("dev/pete/frierenarcana/ArcanaNetwork");
    {
      MethodNode mn = m(an, "shatter", "(Lnet/minecraft/server/level/ServerLevel;Ldev/pete/frierenarcana/BarrierData$Field;Lnet/minecraft/world/phys/Vec3;)V");
      InsnList il = new InsnList();
      il.add(new VarInsnNode(Opcodes.ALOAD, 0)); il.add(new VarInsnNode(Opcodes.ALOAD, 1));
      il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/pete/frierenarcana/RainHold", "broken", "(Lnet/minecraft/server/level/ServerLevel;Ldev/pete/frierenarcana/BarrierData$Field;)V", false));
      mn.instructions.insert(il);
      write(an);
    }
    // BarrierHooks.rainBlocked: server side hold
    ClassNode bh = read("dev/pete/frierenarcana/BarrierHooks");
    {
      MethodNode mn = m(bh, "rainBlocked", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z");
      InsnList il = new InsnList(); LabelNode go = new LabelNode();
      il.add(new VarInsnNode(Opcodes.ALOAD, 0));
      il.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/server/level/ServerLevel"));
      il.add(new JumpInsnNode(Opcodes.IFEQ, go));
      il.add(new VarInsnNode(Opcodes.ALOAD, 0));
      il.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/server/level/ServerLevel"));
      il.add(new VarInsnNode(Opcodes.ALOAD, 1));
      il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/pete/frierenarcana/RainHold", "blocked", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Z", false));
      il.add(new JumpInsnNode(Opcodes.IFEQ, go)); il.add(new InsnNode(Opcodes.ICONST_1)); il.add(new InsnNode(Opcodes.IRETURN)); il.add(go);
      mn.instructions.insert(il);
      write(bh);
    }
    System.out.println("patched ArcanaClient, SpellFx, ArcanaNetwork, BarrierHooks");
  }
}
