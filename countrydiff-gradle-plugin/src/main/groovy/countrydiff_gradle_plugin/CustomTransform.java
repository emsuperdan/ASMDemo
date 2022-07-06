package countrydiff_gradle_plugin;

import com.android.build.api.transform.*;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class CustomTransform extends Transform {

    Project project;
    public static ArrayList<ScanSetting> registerList;
    public static File fileContainsInitClass;

    public CustomTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return ScanSetting.PLUGIN_NAME;
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(Context context, Collection<TransformInput> inputs,
                          Collection<TransformInput> referencedInputs,
                          TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {

        //删除旧的输出
        if (outputProvider != null) {
            outputProvider.deleteAll();
        }

        long startTime = System.currentTimeMillis();
        boolean leftSlash = "/".equals(File.separator);

        for (TransformInput input : inputs) {

            // scan all files
            for (JarInput jarInput : input.getJarInputs()) {
                String destName = jarInput.getName();
                // rename jar files
                String hexName = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }

                File src = jarInput.getFile();

                File dest = outputProvider.getContentLocation(destName + "_ " + hexName,
                        jarInput.getContentTypes(),
                        jarInput.getScopes(), Format.JAR
                );

                if (ScanUtil.shouldProcessPreDexJar(src.getAbsolutePath())) {
                    ScanUtil.scanJar(src, dest);
                }

                FileUtils.copyFile(src, dest);
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(), Format.DIRECTORY);
                String root = directoryInput.getFile().getAbsolutePath();
                if (!root.endsWith(File.separator)) {
                    root += File.separator;
                }

                for (File file : FileUtils.getAllFiles(directoryInput.getFile())) {
                    String path = file.getAbsolutePath().replace(root, "");
                    if (!leftSlash) {
                        path = path.replaceAll("\\\\", "/");
                    }

                    System.out.println("getDirectoryInputsPath:-----:" + path);
                    if (file.isFile() && ScanUtil.shouldProcessClass(path)) {
                        ScanUtil.scanClass(file);
                    }
                }

                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }
        }

        System.out.println("Scan finish, current cost time " + (System.currentTimeMillis() - startTime) + "ms");

        if (fileContainsInitClass != null) {
            for (ScanSetting ext : registerList) {
                System.out.println("Input register code to File " + fileContainsInitClass.getAbsolutePath());

                if (ext.classList.isEmpty()) {
                    System.out.println("implements found for interface:" + ext.interfaceName);
                } else {
                    for (String it : ext.classList) {
                        System.out.println(it);
                    }

                    CodeGenerator.insertInitCodeTo(ext);
                }
            }
        }

        System.out.println("Generate code finish, current cost time " + (System.currentTimeMillis() - startTime));
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
    }
}
