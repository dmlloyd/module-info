/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.moduleinfo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
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
     * The path to the module-info.xml file.
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}/module-info.xml", required = true)
    private File moduleXml;

    @Parameter(defaultValue = "true", property = "module-info.add-packages")
    private boolean addPackages;

    @Parameter(defaultValue = "true", property = "module-info.add-exports")
    private boolean addExports;

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
        final Logger logger = new Logger() {
            private final Log log = getLog();

            public void error(final Throwable cause, final String format, final Object... args) {
                log.error(String.format(format, args), cause);
            }

            public void warn(final Throwable cause, final String format, final Object... args) {
                log.warn(String.format(format, args), cause);
            }

            public void info(final Throwable cause, final String format, final Object... args) {
                log.info(String.format(format, args), cause);
            }

            public void debug(final Throwable cause, final String format, final Object... args) {
                log.debug(String.format(format, args), cause);
            }
        };
        Logger.setLogger(logger);
        if (skip) {
            logger.info("Skipping module-info generation");
            return;
        }
        final ModuleInfoCreator creator = new ModuleInfoCreator();
        creator.setOutputDirectory(outputDirectory.toPath());
        creator.setClassesPaths(Collections.singletonList(classesDirectory.toPath()));
        if (! moduleXml.toString().isEmpty()) creator.setModuleInfoXml(moduleXml.toPath());
        creator.setAddPackages(addPackages);
        creator.setAddExports(addExports);
        creator.setAddMandatory(addMandatory);
        creator.setDetectUses(detectUses);
        creator.setDetectProvides(detectProvides);
        creator.setModuleName(moduleName);
        creator.setModuleVersion(moduleVersion);
        try {
            creator.run();
        } catch (IOException e) {
            throw new MojoExecutionException("module-info.class generation failed", e);
        } catch (XMLStreamException e) {
            throw new MojoFailureException("module-info.class generation failed", e);
        }
    }
}
