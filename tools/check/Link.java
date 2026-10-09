import jdk.internal.org.objectweb.asm.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Resolve every class/method/field reference made by the classes in a jar (or dir) against a classpath.
 *  args: target(jar|dir) classpathEntries... */
public class Link {
  static Map<String, byte[]> cp = new HashMap<>();
  static Map<String, ClassInfo> infos = new HashMap<>();
  static class ClassInfo { String name, sup; String[] itfs; int access; Map<String,Integer> methods = new HashMap<>(), fields = new HashMap<>(); }

  static void addJar(String p, boolean override) throws IOException {
    File f = new File(p);
    if (f.isDirectory()) {
      Files.walk(f.toPath()).filter(q -> q.toString().endsWith(".class")).forEach(q -> {
        String n = f.toPath().relativize(q).toString().replace('\\','/'); n = n.substring(0, n.length() - 6);
        try { if (override || !cp.containsKey(n)) cp.put(n, Files.readAllBytes(q)); } catch (IOException e) { throw new UncheckedIOException(e); }
      });
      return;
    }
    try (ZipFile z = new ZipFile(f)) {
      for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements();) {
        ZipEntry en = e.nextElement(); String n = en.getName();
        if (!n.endsWith(".class") || n.startsWith("META-INF/")) continue;
        n = n.substring(0, n.length() - 6);
        if (override || !cp.containsKey(n)) cp.put(n, z.getInputStream(en).readAllBytes());
      }
    }
  }
  static ClassInfo info(String n) {
    if (infos.containsKey(n)) return infos.get(n);
    byte[] b = cp.get(n);
    if (b == null) {
      try (InputStream in = ClassLoader.getSystemResourceAsStream(n + ".class")) { if (in != null) b = in.readAllBytes(); } catch (IOException e) {}
      if (b == null) { try { b = jrt(n); } catch (Exception e) {} }
    }
    if (b == null) { infos.put(n, null); return null; }
    ClassInfo ci = new ClassInfo();
    new ClassReader(b).accept(new ClassVisitor(Opcodes.ASM9) {
      public void visit(int v, int acc, String name, String s, String sup, String[] itfs) { ci.name = name; ci.sup = sup; ci.itfs = itfs; ci.access = acc; }
      public FieldVisitor visitField(int acc, String name, String d, String s, Object v) { ci.fields.put(name + ":" + d, acc); return null; }
      public MethodVisitor visitMethod(int acc, String name, String d, String s, String[] ex) { ci.methods.put(name + d, acc); return null; }
    }, ClassReader.SKIP_CODE);
    infos.put(n, ci); return ci;
  }
  static byte[] jrt(String n) throws Exception {
    java.nio.file.FileSystem fs = FileSystems.getFileSystem(java.net.URI.create("jrt:/"));
    for (Path mod : Files.newDirectoryStream(fs.getPath("/modules"))) { Path p = mod.resolve(n + ".class"); if (Files.exists(p)) return Files.readAllBytes(p); }
    return null;
  }
  static Integer findMethod(String owner, String nd, Set<String> seen) {
    if (owner == null || !seen.add(owner)) return null;
    ClassInfo ci = info(owner); if (ci == null) return null;
    Integer a = ci.methods.get(nd); if (a != null) return a;
    Integer r = findMethod(ci.sup, nd, seen); if (r != null) return r;
    if (ci.itfs != null) for (String i : ci.itfs) { r = findMethod(i, nd, seen); if (r != null) return r; }
    return null;
  }
  static Integer findField(String owner, String nd, Set<String> seen) {
    if (owner == null || !seen.add(owner)) return null;
    ClassInfo ci = info(owner); if (ci == null) return null;
    Integer a = ci.fields.get(nd); if (a != null) return a;
    if (ci.itfs != null) for (String i : ci.itfs) { Integer r = findField(i, nd, seen); if (r != null) return r; }
    return findField(ci.sup, nd, seen);
  }
  static boolean polymorphic(String owner, String name) { return (owner.equals("java/lang/invoke/MethodHandle") || owner.equals("java/lang/invoke/VarHandle")); }

  public static void main(String[] a) throws Exception {
    List<String> targets = new ArrayList<>();
    Map<String, byte[]> tgt = new LinkedHashMap<>();
    File t = new File(a[0]);
    Map<String, byte[]> save = cp; cp = new HashMap<>(); addJar(a[0], true); tgt.putAll(cp); cp = save;
    for (int i = 1; i < a.length; i++) addJar(a[i], false);
    cp.putAll(tgt);
    Set<String> problems = new TreeSet<>();
    int refs = 0;
    for (Map.Entry<String, byte[]> e : tgt.entrySet()) {
      String self = e.getKey();
      boolean mixin = self.contains("/mixin/");
      ClassReader cr = new ClassReader(e.getValue());
      final int[] cnt = {0};
      cr.accept(new ClassVisitor(Opcodes.ASM9) {
        void cls(String n, String where) { if (n == null) return; if (n.startsWith("[")) { Type ty = Type.getType(n); while (ty.getSort()==Type.ARRAY) ty = ty.getElementType(); if (ty.getSort()!=Type.OBJECT) return; n = ty.getInternalName(); } if (info(n) == null) problems.add("MISSING CLASS " + n + "  <- " + where); }
        void desc(String d, String where) { Type ty = Type.getType(d); if (ty.getSort()==Type.METHOD) { for (Type x : ty.getArgumentTypes()) desc(x.getDescriptor(), where); desc(ty.getReturnType().getDescriptor(), where); } else { while (ty.getSort()==Type.ARRAY) ty = ty.getElementType(); if (ty.getSort()==Type.OBJECT) cls(ty.getInternalName(), where); } }
        public void visit(int v, int acc, String name, String s, String sup, String[] itfs) { cls(sup, self); if (itfs != null) for (String i : itfs) cls(i, self); }
        public MethodVisitor visitMethod(int acc, String mname, String md, String s, String[] ex) {
          String where = self + "." + mname;
          desc(md, where);
          return new MethodVisitor(Opcodes.ASM9) {
            public void visitTypeInsn(int op, String ty) { cls(ty, where); }
            public void visitFieldInsn(int op, String o, String n, String d) {
              cnt[0]++;
              if (o.startsWith("[")) return;
              cls(o, where); desc(d, where);
              if (info(o) == null) return;
              Integer acc2 = findField(o, n + ":" + d, new HashSet<>());
              if (acc2 == null) { if (!(mixin && o.equals(self))) problems.add("MISSING FIELD " + o + "." + n + ":" + d + "  <- " + where); return; }
              boolean st = (acc2 & Opcodes.ACC_STATIC) != 0; boolean wantSt = op == Opcodes.GETSTATIC || op == Opcodes.PUTSTATIC;
              if (st != wantSt) problems.add("STATIC MISMATCH field " + o + "." + n + "  <- " + where);
            }
            public void visitMethodInsn(int op, String o, String n, String d, boolean itf) {
              cnt[0]++;
              desc(d, where);
              if (o.startsWith("[")) return;
              cls(o, where);
              ClassInfo ci = info(o); if (ci == null) return;
              if (polymorphic(o, n)) return;
              boolean isItf = (ci.access & Opcodes.ACC_INTERFACE) != 0;
              if (isItf != itf) problems.add("ITF MISMATCH " + o + "." + n + d + " (owner itf=" + isItf + ")  <- " + where);
              Integer acc2 = findMethod(o, n + d, new HashSet<>());
              if (acc2 == null) { if (!(mixin && o.equals(self))) problems.add("MISSING METHOD " + o + "." + n + d + "  <- " + where); return; }
              boolean st = (acc2 & Opcodes.ACC_STATIC) != 0;
              if (st != (op == Opcodes.INVOKESTATIC)) problems.add("STATIC MISMATCH " + o + "." + n + d + "  <- " + where);
              if ((acc2 & Opcodes.ACC_PRIVATE) != 0 && !o.equals(self) && !o.startsWith(self.split("\\$")[0])) problems.add("PRIVATE ACCESS " + o + "." + n + d + "  <- " + where);
            }
            public void visitInvokeDynamicInsn(String n, String d, Handle h, Object... args) {
              for (Object x : args) if (x instanceof Handle hh) {
                cnt[0]++;
                ClassInfo ci = info(hh.getOwner()); if (ci == null) { cls(hh.getOwner(), where); continue; }
                if (hh.getTag() >= Opcodes.H_INVOKEVIRTUAL) { if (findMethod(hh.getOwner(), hh.getName() + hh.getDesc(), new HashSet<>()) == null) problems.add("MISSING HANDLE " + hh.getOwner() + "." + hh.getName() + hh.getDesc() + "  <- " + where); }
              }
            }
            public void visitLdcInsn(Object v) { if (v instanceof Type ty && ty.getSort() == Type.OBJECT) cls(ty.getInternalName(), where); }
          };
        }
      }, 0);
      refs += cnt[0];
    }
    for (String p : problems) System.out.println(p);
    System.out.println("classes=" + tgt.size() + " refs=" + refs + " problems=" + problems.size());
  }
}
