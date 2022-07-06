package countrydiff_gradle_plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CodeGenerator {
    ScanSetting extension;

    private CodeGenerator(ScanSetting extension) {
        this.extension = extension;
    }

    static void insertInitCodeTo(ScanSetting registerSetting) {
        if (registerSetting != null && !registerSetting.classList.isEmpty()) {
            CodeGenerator processor = new CodeGenerator(registerSetting);
            File file = CustomTransform.fileContainsInitClass;
            if (file.getName().endsWith(".jar")) {
                try {
                    processor.insertInitCodeIntoJarFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * generate code into jar file
     *
     * @param jarFile the jar file which contains LogisticsCenter.class
     * @return
     */
    private File insertInitCodeIntoJarFile(File jarFile) throws IOException {
        System.out.println("insertInitCodeIntoJarFile init");
        if (jarFile != null) {
            File optJar = new File(jarFile.getParent(), jarFile.getName() + ".opt");
            if (optJar.exists()) {
                optJar.delete();
            }
            JarFile file = new JarFile(jarFile);
            Enumeration<JarEntry> enumeration = file.entries();
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = file.getInputStream(jarEntry);
                jarOutputStream.putNextEntry(zipEntry);
                System.out.println("insertInitCodeIntoJarFile enumeration " + entryName);
                if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME.equals(entryName)) {
                    System.out.println("Insert init code to class >> " + entryName);
                    byte[] bytes = referHackWhenInit(inputStream);
                    jarOutputStream.write(bytes);
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }

                inputStream.close();
                jarOutputStream.closeEntry();
            }

            jarOutputStream.close();
            file.close();

            if (jarFile.exists()) {
                jarFile.delete();
            }
            optJar.renameTo(jarFile);
        }
        System.out.println("insertInitCodeIntoJarFile success");
        return jarFile;
    }

    //refer hack class when object init
    private byte[] referHackWhenInit(InputStream inputStream) throws IOException {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    class MyClassVisitor extends ClassVisitor {

        MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            //generate code into this method
            if (name.equals(ScanSetting.GENERATE_TO_METHOD_NAME)) {
                mv = new DiffMethodVisitor(Opcodes.ASM5, mv);
            }
            return mv;
        }
    }

    class DiffMethodVisitor extends MethodVisitor {

        DiffMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            //generate code before return
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                for (String name : extension.classList) {
                    System.out.println("insert diff method");
                    name = name.replaceAll("/", ".");
                    mv.visitLdcInsn(name);//类名
                    // generate invoke register method into LogisticsCenter.loadRouterMap()
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC
                            , ScanSetting.GENERATE_TO_CLASS_NAME
                            , ScanSetting.REGISTER_METHOD_NAME
                            , "(Ljava/lang/String;)V"
                            , false);
                }
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals);
        }
    }
}