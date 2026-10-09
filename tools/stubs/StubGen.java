import jdk.internal.org.objectweb.asm.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Generates bytecode stubs for every class referenced by the scanned jars but not provided by them or the JDK.
 *  args: outDir hierarchyFile scanJar... -- providedJar...  (scan jars are also provided) */
public class StubGen {
  static class M { String name, desc; boolean stat; }
  static class C {
    String name; boolean itf, ann, en; String sup; Set<String> itfs = new LinkedHashSet<>();
    Map<String,M> methods = new LinkedHashMap<>(); Map<String,M> fields = new LinkedHashMap<>();
    int arity; String sam, outer, simple; int innerAcc; Map<String,String> annElems = new LinkedHashMap<>();
  }
  static Map<String,C> missing = new TreeMap<>();
  static Set<String> provided = new HashSet<>();
  static Map<String,String> hierSup = new HashMap<>();
  static Map<String,List<String>> hierItf = new HashMap<>();
  static Set<String> forceItf = new HashSet<>(), forceEnum = new HashSet<>();

  static boolean isProvided(String n) {
    if (provided.contains(n)) return true;
    if (n.startsWith("java/") || n.startsWith("javax/") || n.startsWith("jdk/") || n.startsWith("sun/")) return true;
    return ClassLoader.getSystemResource(n + ".class") != null;
  }
  static C c(String n) {
    if (n == null || n.startsWith("[")) { if (n != null) { Type t = Type.getType(n); while (t.getSort()==Type.ARRAY) t=t.getElementType(); if (t.getSort()==Type.OBJECT) return c(t.getInternalName()); } return null; }
    if (isProvided(n)) return null;
    return missing.computeIfAbsent(n, k -> { C x = new C(); x.name = k; return x; });
  }
  static void type(Type t) { if (t == null) return; if (t.getSort()==Type.ARRAY) t=t.getElementType(); if (t.getSort()==Type.OBJECT) c(t.getInternalName()); else if (t.getSort()==Type.METHOD) { for (Type a: t.getArgumentTypes()) type(a); type(t.getReturnType()); } }
  static void desc(String d) { if (d != null) type(Type.getType(d)); }

  static void sig(String s) {
    if (s == null) return;
    // find L...<  and count top-level args
    int i = 0;
    while ((i = s.indexOf('L', i)) >= 0) {
      int j = i + 1; StringBuilder name = new StringBuilder();
      // read class name until ; or <
      int k = j; while (k < s.length() && s.charAt(k) != ';' && s.charAt(k) != '<' && s.charAt(k) != '.') k++;
      if (k >= s.length()) break;
      // validate preceding char is a type-start position
      char prev = i == 0 ? '(' : s.charAt(i-1);
      if (!(prev=='('||prev==';'||prev=='<'||prev=='['||prev=='+'||prev=='-'||prev==')'||prev=='>'||prev==':'||prev=='^'||prev=='*')) { i++; continue; }
      String cn = s.substring(j, k);
      String cur = cn;
      while (k < s.length() && (s.charAt(k) == '<' || s.charAt(k) == '.')) {
        if (s.charAt(k) == '<') {
          int depth = 0, count = 0; int m = k;
          for (; m < s.length(); m++) {
            char ch = s.charAt(m);
            if (ch == '<') { depth++; if (depth == 1) { count++; } continue; }
            if (ch == '>') { depth--; if (depth == 0) break; continue; }
          }
          // count args properly: parse sequence of type args at depth 1
          count = countArgs(s, k + 1);
          C cc = c(cur); if (cc != null) cc.arity = Math.max(cc.arity, count);
          k = m + 1;
        } else { // inner .Name
          int e = k + 1; while (e < s.length() && s.charAt(e) != ';' && s.charAt(e) != '<' && s.charAt(e) != '.') e++;
          cur = cur + "$" + s.substring(k + 1, e); c(cur); k = e;
        }
      }
      i = j;
    }
  }
  static int countArgs(String s, int p) { int n = 0; while (s.charAt(p) != '>') { p = skipType(s, p); n++; } return n; }
  static int skipType(String s, int p) {
    char ch = s.charAt(p);
    if (ch == '*') return p + 1;
    if (ch == '+' || ch == '-') return skipType(s, p + 1);
    if (ch == '[') return skipType(s, p + 1);
    if (ch == 'T') return s.indexOf(';', p) + 1;
    if (ch == 'L') { int d = 0; for (int i = p; ; i++) { char c2 = s.charAt(i); if (c2 == '<') d++; else if (c2 == '>') d--; else if (c2 == ';' && d == 0) return i + 1; } }
    return p + 1;
  }

  static void scanClass(byte[] b) {
    ClassReader cr = new ClassReader(b);
    cr.accept(new ClassVisitor(Opcodes.ASM9) {
      String self;
      public void visit(int v, int acc, String name, String s, String sup, String[] itfs) {
        self = name; C sc = c(sup); sig(s);
        if (itfs != null) for (String i : itfs) { C ic = c(i); if (ic != null) ic.itf = true; }
      }
      public AnnotationVisitor visitAnnotation(String d, boolean vis) { return ann(d); }
      public FieldVisitor visitField(int acc, String n, String d, String s, Object v) { desc(d); sig(s);
        return new FieldVisitor(Opcodes.ASM9) { public AnnotationVisitor visitAnnotation(String d2, boolean v2) { return ann(d2); } }; }
      public MethodVisitor visitMethod(int acc, String n, String d, String s, String[] ex) {
        desc(d); sig(s); if (ex != null) for (String e : ex) c(e);
        return new MethodVisitor(Opcodes.ASM9) {
          public AnnotationVisitor visitAnnotation(String d2, boolean v2) { return ann(d2); }
          public AnnotationVisitor visitParameterAnnotation(int p, String d2, boolean v2) { return ann(d2); }
          public void visitTypeInsn(int op, String t) { c(t); }
          public void visitFieldInsn(int op, String o, String n2, String d2) {
            desc(d2); C oc = c(o); if (oc == null) return;
            M m = new M(); m.name = n2; m.desc = d2; m.stat = op == Opcodes.GETSTATIC || op == Opcodes.PUTSTATIC;
            oc.fields.putIfAbsent(n2, m);
          }
          public void visitMethodInsn(int op, String o, String n2, String d2, boolean itf) {
            desc(d2); C oc = c(o); if (oc == null) return;
            if (itf) oc.itf = true;
            if (n2.equals("ordinal") && d2.equals("()I")) oc.en = true;
            M m = new M(); m.name = n2; m.desc = d2; m.stat = op == Opcodes.INVOKESTATIC;
            oc.methods.putIfAbsent(n2 + d2, m);
          }
          public void visitInvokeDynamicInsn(String n2, String d2, Handle h, Object... args) {
            desc(d2);
            for (Object a : args) {
              if (a instanceof Handle hh) {
                C oc = c(hh.getOwner()); desc(hh.getDesc());
                if (oc != null) { M m = new M(); m.name = hh.getName(); m.desc = hh.getDesc(); m.stat = hh.getTag() == Opcodes.H_INVOKESTATIC;
                  if (hh.isInterface()) oc.itf = true;
                  if (hh.getTag()==Opcodes.H_GETFIELD||hh.getTag()==Opcodes.H_GETSTATIC) { oc.fields.putIfAbsent(m.name, m);} else oc.methods.putIfAbsent(m.name + m.desc, m); }
              } else if (a instanceof Type t) type(t);
            }
            // functional interface returned by indy (e.g. LambdaMetafactory) -> interface
            Type rt = Type.getReturnType(d2);
            if (rt.getSort() == Type.OBJECT && args.length >= 3 && h.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
              C fc = c(rt.getInternalName()); if (fc != null) { fc.itf = true;
                Type sam = (Type) args[0]; M m = new M(); m.name = n2; m.desc = sam.getDescriptor(); m.stat = false; fc.methods.putIfAbsent(n2 + m.desc, m); fc.sam = n2 + m.desc; }
            }
          }
          public void visitLdcInsn(Object v) { if (v instanceof Type t) type(t); }
          public void visitMultiANewArrayInsn(String d2, int dims) { desc(d2); }
          public void visitLocalVariable(String n2, String d2, String s2, Label a, Label b2, int i) { desc(d2); sig(s2); }
          public void visitTryCatchBlock(Label a, Label b2, Label h2, String t) { if (t != null) c(t); }
        };
      }
      public void visitInnerClass(String n, String outer, String inner, int acc) {
        C ic = c(n); if (ic != null) { if ((acc & Opcodes.ACC_INTERFACE) != 0) ic.itf = true; if ((acc & Opcodes.ACC_ENUM) != 0) ic.en = true; if ((acc & Opcodes.ACC_ANNOTATION)!=0) ic.ann = true; ic.innerAcc = acc; ic.outer = outer; ic.simple = inner; }
      }
    }, 0);
  }
  static AnnotationVisitor ann(String d) {
    C a = c(Type.getType(d).getInternalName());
    if (a == null) return null;
    a.ann = true; a.itf = true;
    return annV(a);
  }
  static AnnotationVisitor annV(C a) {
    return new AnnotationVisitor(Opcodes.ASM9) {
      public void visit(String n, Object v) { if (a != null && n != null) a.annElems.putIfAbsent(n, prim(v)); }
      public void visitEnum(String n, String d, String v) { C e = c(Type.getType(d).getInternalName()); if (e != null) { e.en = true; M m = new M(); m.name = v; m.desc = d; m.stat = true; e.fields.putIfAbsent(v, m);} if (a != null && n != null) a.annElems.putIfAbsent(n, d); }
      public AnnotationVisitor visitAnnotation(String n, String d) { if (a != null && n != null) a.annElems.putIfAbsent(n, d); return ann(d); }
      public AnnotationVisitor visitArray(String n) {
        return new AnnotationVisitor(Opcodes.ASM9) {
          public void visit(String n2, Object v) { if (a != null) a.annElems.put(n, "[" + prim(v)); }
          public void visitEnum(String n2, String d, String v) { C e = c(Type.getType(d).getInternalName()); if (e != null) { e.en = true; M m = new M(); m.name = v; m.desc = d; m.stat = true; e.fields.putIfAbsent(v, m);} if (a != null) a.annElems.put(n, "[" + d); }
          public AnnotationVisitor visitAnnotation(String n2, String d) { if (a != null) a.annElems.put(n, "[" + d); return ann(d); }
        };
      }
    };
  }
  static String prim(Object v) {
    if (v instanceof String) return "Ljava/lang/String;"; if (v instanceof Type) return "Ljava/lang/Class;";
    if (v instanceof Integer) return "I"; if (v instanceof Boolean) return "Z"; if (v instanceof Float) return "F"; if (v instanceof Double) return "D"; if (v instanceof Long) return "J"; return "Ljava/lang/String;";
  }

  static void eachClass(String jar, java.util.function.Consumer<byte[]> f, boolean recordProvided) throws IOException {
    try (ZipFile z = new ZipFile(jar)) {
      for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements();) {
        ZipEntry en = e.nextElement();
        if (en.getName().endsWith(".class") && !en.getName().startsWith("META-INF/versions") && !en.getName().equals("module-info.class")) {
          byte[] b = z.getInputStream(en).readAllBytes();
          if (recordProvided) provided.add(en.getName().substring(0, en.getName().length() - 6));
          else if (f != null) f.accept(b);
        }
      }
    }
  }

  public static void main(String[] a) throws Exception {
    Path out = Paths.get(a[0]);
    for (String line : Files.readAllLines(Paths.get(a[1]))) {
      line = line.trim(); if (line.isEmpty() || line.startsWith("#")) continue;
      String[] p = line.split("\\s+");
      // forms:  Child > Super [, Itf...]   |  itf Name  | enum Name
      if (p[0].equals("itf")) { forceItf.add(p[1]); continue; }
      if (p[0].equals("enum")) { forceEnum.add(p[1]); continue; }
      if (p.length >= 3 && p[1].equals(">")) { if (!p[2].equals("-")) hierSup.put(p[0], p[2]); List<String> l = new ArrayList<>(); for (int i = 3; i < p.length; i++) l.add(p[i]); hierItf.put(p[0], l); }
    }
    List<String> scan = new ArrayList<>(), prov = new ArrayList<>();
    boolean pr = false;
    for (int i = 2; i < a.length; i++) { if (a[i].equals("--")) { pr = true; continue; } (pr ? prov : scan).add(a[i]); }
    for (String j : scan) eachClass(j, null, true);
    for (String j : prov) eachClass(j, null, true);
    for (String j : scan) eachClass(j, StubGen::scanClass, false);
    // make sure hierarchy-named classes exist
    for (String k : new ArrayList<>(hierSup.keySet())) { c(k); c(hierSup.get(k)); }
    for (List<String> l : new ArrayList<>(hierItf.values())) for (String s : l) { C ic = c(s); if (ic != null) ic.itf = true; }
    for (String s : forceItf) { C x = c(s); if (x != null) x.itf = true; }
    for (String s : forceEnum) { C x = c(s); if (x != null) x.en = true; }
    // outer classes of inner stubs
    for (String k : new ArrayList<>(missing.keySet())) { int d = k.lastIndexOf('$'); if (d > 0) c(k.substring(0, d)); }
    int n = 0;
    for (C x : missing.values()) { write(out, x); n++; }
    System.out.println("stubs: " + n);
  }

  static void write(Path out, C x) throws IOException {
    ClassWriter cw = new ClassWriter(0);
    boolean itf = x.itf && !x.en;
    int acc = Opcodes.ACC_PUBLIC | (itf ? Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT : 0) | (x.ann ? Opcodes.ACC_ANNOTATION : 0) | (x.en ? Opcodes.ACC_ENUM | Opcodes.ACC_FINAL : 0);
    String sup = x.en ? "java/lang/Enum" : itf ? "java/lang/Object" : hierSup.getOrDefault(x.name, "java/lang/Object");
    List<String> itfs = new ArrayList<>(hierItf.getOrDefault(x.name, List.of()));
    if (x.ann) itfs.add("java/lang/annotation/Annotation");
    if (itf && hierSup.containsKey(x.name)) itfs.add(0, hierSup.get(x.name));
    String sig = null;
    StringBuilder tp = new StringBuilder();
    if (x.arity > 0) { tp.append('<'); for (int i = 0; i < x.arity; i++) tp.append("T").append(i).append(":Ljava/lang/Object;"); tp.append('>'); }
    if (x.en) sig = tp + "Ljava/lang/Enum<L" + x.name + ";>;" + itfSig(itfs);
    else if (x.arity > 0) sig = tp + "L" + sup + ";" + itfSig(itfs);
    cw.visit(Opcodes.V17, acc, x.name, sig, sup, itfs.toArray(new String[0]));
    // inner class attributes
    int d = x.name.lastIndexOf('$');
    if (d > 0) {
      String outer = x.name.substring(0, d), simple = x.name.substring(d + 1);
      boolean anon = simple.chars().allMatch(Character::isDigit);
      if (!anon) cw.visitInnerClass(x.name, outer, simple, (acc & ~Opcodes.ACC_ABSTRACT & ~Opcodes.ACC_FINAL) | Opcodes.ACC_STATIC | (itf ? Opcodes.ACC_ABSTRACT : 0) | (x.en ? Opcodes.ACC_FINAL : 0));
    }
    for (String k : missing.keySet()) { int dd = k.lastIndexOf('$'); if (dd > 0 && k.substring(0, dd).equals(x.name)) { String simple = k.substring(dd + 1); if (simple.chars().allMatch(Character::isDigit)) continue; C ic = missing.get(k); boolean ii = ic.itf && !ic.en; cw.visitInnerClass(k, x.name, simple, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | (ii ? Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT : 0) | (ic.ann ? Opcodes.ACC_ANNOTATION : 0) | (ic.en ? Opcodes.ACC_ENUM | Opcodes.ACC_FINAL : 0)); } }
    if (!itf) {
      MethodVisitor mv = cw.visitMethod(x.en ? Opcodes.ACC_PRIVATE : Opcodes.ACC_PROTECTED, "<init>", "()V", null, null);
      mv.visitCode(); mv.visitInsn(Opcodes.ACONST_NULL); mv.visitInsn(Opcodes.ATHROW); mv.visitMaxs(1, 1); mv.visitEnd();
    }
    if (x.en) {
      boolean hasValues = false;
      for (M m : x.methods.values()) if (m.name.equals("values")) hasValues = true;
      if (!hasValues) { M m = new M(); m.name = "values"; m.desc = "()[L" + x.name + ";"; m.stat = true; x.methods.put(m.name + m.desc, m); }
      M vo = new M(); vo.name = "valueOf"; vo.desc = "(Ljava/lang/String;)L" + x.name + ";"; vo.stat = true; x.methods.putIfAbsent(vo.name + vo.desc, vo);
    }
    for (M f : x.fields.values()) {
      boolean enumConst = x.en && f.stat && f.desc.equals("L" + x.name + ";");
      int fa = Opcodes.ACC_PUBLIC | (f.stat ? Opcodes.ACC_STATIC : 0) | (enumConst ? Opcodes.ACC_ENUM | Opcodes.ACC_FINAL : 0) | (itf ? Opcodes.ACC_STATIC | Opcodes.ACC_FINAL : 0);
      cw.visitField(fa, f.name, f.desc, null, null).visitEnd();
    }
    for (M m : x.methods.values()) {
      if (m.name.equals("<clinit>")) continue;
      if (m.name.equals("<init>")) {
        if (itf) continue;
        if (m.desc.equals("()V")) continue;
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", m.desc, null, null);
        mv.visitCode(); mv.visitInsn(Opcodes.ACONST_NULL); mv.visitInsn(Opcodes.ATHROW); mv.visitMaxs(1, 20); mv.visitEnd();
        continue;
      }
      if (x.en && (m.name.equals("ordinal") || m.name.equals("name") || m.name.equals("compareTo") || m.name.equals("getDeclaringClass") || m.name.equals("equals") || m.name.equals("hashCode") || m.name.equals("toString"))) continue;
      if (!x.en && (m.name.equals("equals")&&m.desc.equals("(Ljava/lang/Object;)Z") || m.name.equals("hashCode")&&m.desc.equals("()I") || m.name.equals("toString")&&m.desc.equals("()Ljava/lang/String;") || m.name.equals("getClass"))) { if (!itf) continue; }
      boolean isSam = itf && (m.name + m.desc).equals(x.sam);
      int ma = Opcodes.ACC_PUBLIC | (m.stat ? Opcodes.ACC_STATIC : 0) | (x.ann || isSam ? Opcodes.ACC_ABSTRACT : 0);
      if (itf && !m.stat && !x.ann && !isSam && (m.name.equals("equals") || m.name.equals("hashCode") || m.name.equals("toString"))) ma |= Opcodes.ACC_ABSTRACT;
      MethodVisitor mv = cw.visitMethod(ma, m.name, m.desc, null, null);
      if ((ma & Opcodes.ACC_ABSTRACT) == 0) {
        mv.visitCode(); mv.visitInsn(Opcodes.ACONST_NULL); mv.visitInsn(Opcodes.ATHROW); mv.visitMaxs(1, 30);
      }
      mv.visitEnd();
    }
    if (x.ann) {
      for (Map.Entry<String,String> e : x.annElems.entrySet()) {
        if (x.methods.containsKey(e.getKey() + "()" + e.getValue())) continue;
        String dsc = e.getValue();
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, e.getKey(), "()" + dsc, null, null);
        if (dsc.startsWith("L") && !dsc.equals("Ljava/lang/String;") && !dsc.equals("Ljava/lang/Class;")) { C ec0 = missing.get(Type.getType(dsc).getInternalName()); if (ec0 == null || !((ec0.en && !ec0.fields.isEmpty()) || ec0.ann)) { mv.visitEnd(); continue; } }
        AnnotationVisitor av = mv.visitAnnotationDefault();
        if (dsc.startsWith("[")) { av.visitArray(null).visitEnd(); }
        else if (dsc.equals("Ljava/lang/String;")) av.visit(null, "");
        else if (dsc.equals("Ljava/lang/Class;")) av.visit(null, Type.getType("Ljava/lang/Object;"));
        else if (dsc.equals("I")) av.visit(null, 0); else if (dsc.equals("Z")) av.visit(null, false);
        else if (dsc.equals("F")) av.visit(null, 0f); else if (dsc.equals("D")) av.visit(null, 0d); else if (dsc.equals("J")) av.visit(null, 0L);
        else if (dsc.startsWith("L")) { C ec = missing.get(Type.getType(dsc).getInternalName()); if (ec != null && ec.en && !ec.fields.isEmpty()) av.visitEnum(null, dsc, ec.fields.keySet().iterator().next()); else if (ec != null && ec.ann) av.visitAnnotation(null, dsc).visitEnd(); }
        av.visitEnd();
        mv.visitEnd();
      }
    }
    cw.visitEnd();
    Path p = out.resolve(x.name + ".class");
    Files.createDirectories(p.getParent());
    Files.write(p, cw.toByteArray());
  }
  static String itfSig(List<String> l) { StringBuilder b = new StringBuilder(); for (String s : l) b.append('L').append(s).append(';'); return b.toString(); }
}
