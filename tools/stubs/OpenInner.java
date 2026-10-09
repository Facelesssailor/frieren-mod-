import jdk.internal.org.objectweb.asm.*;
import java.nio.file.*;
/** args: inDir outDir className... : compile view: private nested classes made package-visible, generic signatures on accessors */
public class OpenInner {
  public static void main(String[] a) throws Exception {
    Path in = Paths.get(a[0]), out = Paths.get(a[1]);
    for (int i = 2; i < a.length; i++) {
      Path p = in.resolve(a[i] + ".class");
      ClassReader cr = new ClassReader(Files.readAllBytes(p));
      ClassWriter cw = new ClassWriter(0);
      cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
        public void visitInnerClass(String n, String o, String s, int acc) { super.visitInnerClass(n, o, s, acc & ~Opcodes.ACC_PRIVATE); }
        public MethodVisitor visitMethod(int acc, String n, String d, String s, String[] ex) {
          if (s == null && d.equals("()Ljava/util/List;")) {
            if (n.equals("fractures")) s = "()Ljava/util/List<Ldev/pete/frierenarcana/client/ArcanaClient$Fracture;>;";
            if (n.equals("fields")) s = "()Ljava/util/List<Ldev/pete/frierenarcana/client/ArcanaClient$VisualField;>;";
          }
          return super.visitMethod(acc, n, d, s, ex);
        }
      }, 0);
      Path q = out.resolve(a[i] + ".class"); Files.createDirectories(q.getParent()); Files.write(q, cw.toByteArray());
    }
  }
}
