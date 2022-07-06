package countrydiff_gradle_plugin;

import org.apache.http.util.TextUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScanUtil {

    /**
     * scan jar file
     *
     * @param jarFile  All jar files that are compiled into apk
     * @param destFile dest file after this transform
     */
    public static void scanJar(File jarFile, File destFile) {
        if (jarFile != null) {
            try {
                JarFile file = new JarFile(jarFile);
                Enumeration<JarEntry> enumeration = file.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                    String entryName = jarEntry.getName();
                    if (entryName.startsWith(ScanSetting.COUNTRY_DIFF_CLASS_PACKAGE_NAME)) {
                        InputStream inputStream = file.getInputStream(jarEntry);
                        scanClass(inputStream);
                        inputStream.close();
                    } else if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME.equals(entryName)) {
                        CustomTransform.fileContainsInitClass = destFile;
                    }
                }

                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    public static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith(ScanSetting.COUNTRY_DIFF_CLASS_PACKAGE_NAME);
    }

    /**
     * scan class file
     */
    public static void scanClass(File file) {
        try {
            scanClass(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void scanClass(InputStream inputStream) throws IOException {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        inputStream.close();
    }

    static class ScanClassVisitor extends ClassVisitor {

        ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }


        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);

            for (ScanSetting ext : CustomTransform.registerList) {
                if (ext.interfaceName != null && !TextUtils.isEmpty(ext.interfaceName)
                        && interfaces != null) {
                    for (String itName : interfaces) {
                        if (itName.equals(ext.interfaceName)) {
                            //fix repeated inject init code when Multi-channel packaging
                            if (!ext.classList.contains(name)) {
                                ext.classList.add(name);
                            }
                        }
                    }
                }
            }
        }
    }
}