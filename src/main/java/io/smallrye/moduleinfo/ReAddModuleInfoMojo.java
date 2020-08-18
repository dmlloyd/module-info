package io.smallrye.moduleinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A mojo to re-add the deleted {@code module-info.class} file.
 */
@Mojo(name = "re-add", defaultPhase = LifecyclePhase.PACKAGE)
public class ReAddModuleInfoMojo extends AbstractMojo {
    @Parameter(property = "module-info.jar-file", defaultValue = "${project.build.directory}/${project.build.finalName}.jar", required = true)
    private File jarFile;

    @Parameter(defaultValue = "${project.build.outputDirectory}/module-info.class", required = true)
    private File moduleInfoClass;

    public void execute() throws MojoExecutionException, MojoFailureException {
        byte[] buffer = new byte[16384];
        final Logger logger = new MojoLogger(getLog());
        Logger.setLogger(logger);
        try (JarFile input = new JarFile(jarFile)) {
            File tmpFile = new File(jarFile.getParentFile(), jarFile.getName() + ".tmp");
            try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
                try (JarOutputStream output = new JarOutputStream(fos)) {
                    Enumeration<JarEntry> e = input.entries();
                    while (e.hasMoreElements()) {
                        JarEntry entry = e.nextElement();
                        if (entry.getName().equals("module-info.class")) {
                            output.close();
                            fos.close();
                            tmpFile.delete();
                            input.close();
                            // nothing to do
                            logger.info("Skipping re-add of module-info.class (JAR already contains file)");
                            return;
                        }
                        output.putNextEntry(entry);
                        try (InputStream eis = input.getInputStream(entry)) {
                            int res = eis.read(buffer);
                            while (res != -1) {
                                output.write(buffer, 0, res);
                                res = eis.read(buffer);
                            }
                        }
                        output.closeEntry();
                    }
                    // add the missing file
                    try (FileInputStream eis = new FileInputStream(moduleInfoClass)) {
                        output.putNextEntry(new JarEntry("module-info.class"));
                        int res = eis.read(buffer);
                        while (res != -1) {
                            output.write(buffer, 0, res);
                            res = eis.read(buffer);
                        }
                    }
                }
            }
            if (!jarFile.delete()) {
                throw new MojoExecutionException("Failed to delete original JAR file");
            }
            if (!tmpFile.renameTo(jarFile)) {
                throw new MojoExecutionException("Failed to rename new JAR file to original name");
            }
            logger.info("Re-added module-info.class to \"%s\".", jarFile);
        } catch (FileNotFoundException | NoSuchFileException e) {
            throw new MojoFailureException("JAR file \"" + jarFile + "\" not found", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to modify JAR file", e);
        }
    }
}
