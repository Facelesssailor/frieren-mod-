import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Bytecode verifier: runs ASM's SimpleVerifier (type-checking data flow) over every method of every class in the target jar,
 *  resolving types against the real classpath without initialising anything.  args: target.jar cp... */
public class V {
  public static void main(String[] a) throws Exception {
    List<URL> urls = new ArrayList<>();
    for (String s : a) urls.add(new File(s).toURI().toURL());
    URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    int classes = 0, methods = 0, bad = 0;
    try (ZipFile z = new ZipFile(a[0])) {
      for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements();) {
        ZipEntry en = e.nextElement();
        if (!en.getName().endsWith(".class") || en.getName().startsWith("META-INF")) continue;
        ClassNode cn = new ClassNode();
        new ClassReader(z.getInputStream(en).readAllBytes()).accept(cn, 0);
        classes++;
        Type owner = Type.getObjectType(cn.name);
        Type sup = cn.superName == null ? null : Type.getObjectType(cn.superName);
        List<Type> itfs = new ArrayList<>(); for (String i : cn.interfaces) itfs.add(Type.getObjectType(i));
        boolean isItf = (cn.access & Opcodes.ACC_INTERFACE) != 0;
        for (MethodNode mn : cn.methods) {
          if ((mn.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) continue;
          methods++;
          SimpleVerifier sv = new SimpleVerifier(Opcodes.ASM9, owner, sup, itfs, isItf) {};
          sv.setClassLoader(cl);
          try { new Analyzer<>(sv).analyze(cn.name, mn); }
          catch (Throwable t) { bad++; System.out.println("VERIFY FAIL " + cn.name + "." + mn.name + mn.desc + ": " + t); }
        }
      }
    }
    System.out.println("verified classes=" + classes + " methods=" + methods + " failures=" + bad);
  }
}
