package io.github.dmlloyd.moduleinfo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class ModuleInfoMojo extends AbstractMojo {

    @Parameter(defaultValue = "false", property = "module-info.skip")
    private boolean skip;

    /**
     * The directory where the module-info.class file should be installed.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File outputDirectory;

    /**
     * The directory where class files can be read from.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;

    /**
     * The path to the module-info.yml file.
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}/module-info.yml")
    private File moduleInfoYml;

    @Parameter(defaultValue = "true", property = "module-info.add-packages")
    private boolean addPackages;

    @Parameter(defaultValue = "true", property = "module-info.add-exports")
    private boolean addExports;

    @Parameter(defaultValue = "^.*\\b(internal|_private|private_)\\b.*$", property = "module-info.export-excludes")
    private String exportExcludes;

    @Parameter(defaultValue = "${project.artifactId}", required = true)
    private String moduleArtifactId;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String moduleGroupId;

    @Parameter(property = "module-info.module-name")
    private String moduleName;

    @Parameter(defaultValue = "${project.version}", property = "module-info.module-version")
    private String moduleVersion;

    @Parameter(defaultValue = "true", property = "module-info.add-mandatory")
    private boolean addMandatory;

    @Parameter(defaultValue = "true", property = "module-info.detect-uses")
    private boolean detectUses;

    @Parameter(defaultValue = "true", property = "module-info.detect-provides")
    private boolean detectProvides;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Logger logger = new MojoLogger(getLog());
        Logger.setLogger(logger);
        if (skip) {
            logger.info("Skipping module-info generation");
            return;
        }
        final ModuleInfoCreator creator = new ModuleInfoCreator();
        creator.setOutputDirectory(outputDirectory.toPath());
        creator.setClassesPaths(Collections.singletonList(classesDirectory.toPath()));
        if (moduleInfoYml != null && !moduleInfoYml.toString().isEmpty() && moduleInfoYml.exists()) {
            creator.setModuleInfoYml(moduleInfoYml.toPath());
        }
        creator.setAddPackages(addPackages);
        creator.setAddExports(addExports);
        creator.setExportExcludes(exportExcludes);
        creator.setAddMandatory(addMandatory);
        creator.setDetectUses(detectUses);
        creator.setDetectProvides(detectProvides);
        if (moduleName != null) {
            creator.setModuleName(moduleName);
        } else {
            String artifact = moduleArtifactId.replace('-', '.');
            String group = moduleGroupId.replace('-', '.');
            // find any common part from the end of the group and the start of the artifact
            int i = 0;
            for (;;) {
                if (artifact.startsWith(group.substring(i))) {
                    moduleName = i == 0 ? artifact : group.substring(0, i - 1) + "." + artifact;
                    break;
                }
                i = group.indexOf('.', i);
                if (i == -1) {
                    moduleName = group + "." + artifact;
                    break;
                }
                i++;
            }
            creator.setDefaultModuleName(moduleName);
        }
        if (moduleVersion != null) {
            creator.setModuleVersion(moduleVersion);
        }
        try {
            creator.run();
        } catch (IOException e) {
            throw new MojoExecutionException("module-info.class generation failed", e);
        } catch (XMLStreamException e) {
            throw new MojoFailureException("module-info.class generation failed", e);
        }
    }
}
