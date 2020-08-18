package io.smallrye.moduleinfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;

/**
 */
@Parameters(separators = "=")
public class ModuleInfoCreator {
    @Parameter(names = { "--module-info-yml", "-i" }, converter = PathConverter.class)
    private Path moduleInfoYml;
    @Parameter(names = { "--output-dir", "-o" }, required = true, converter = PathConverter.class)
    private Path outputDirectory;
    @Parameter(names = { "--class-path" }, listConverter = PathConverter.class)
    private List<Path> classesPaths = new ArrayList<>();
    @Parameter(names = { "--module-name", "-n" })
    private String moduleName;
    @Parameter(names = { "--module-version", "-v" })
    private String moduleVersion;
    @Parameter(names = "--add-mandatory", arity = 1)
    private boolean addMandatory = true;
    @Parameter(names = "--add-packages", arity = 1)
    private boolean addPackages = true;
    @Parameter(names = "--add-exports", arity = 1)
    private boolean addExports = true;
    @Parameter(names = "--detect-uses", arity = 1)
    private boolean detectUses = true;
    @Parameter(names = "--detect-provides", arity = 1)
    private boolean detectProvides = true;
    @Parameter(names = "--detect-version", arity = 1)
    private boolean detectVersion = false;
    @Parameter(names = { "--help", "-h" }, help = true)
    private boolean help = false;

    private String defaultModuleName;

    public ModuleInfoCreator() {
    }

    public static void main(String[] args) throws IOException, XMLStreamException {
        ModuleInfoCreator creator = new ModuleInfoCreator();
        final JCommander commander = JCommander.newBuilder()
                .addObject(creator)
                .acceptUnknownOptions(false)
                .allowParameterOverwriting(true)
                .build();
        commander.parse(args);
        if (creator.help) {
            commander.usage();
            return;
        }
        creator.run();
    }

    public Path getModuleInfoYml() {
        return moduleInfoYml;
    }

    public ModuleInfoCreator setModuleInfoYml(final Path moduleInfoYml) {
        this.moduleInfoYml = moduleInfoYml;
        return this;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public ModuleInfoCreator setOutputDirectory(final Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public List<Path> getClassesPaths() {
        return classesPaths;
    }

    public ModuleInfoCreator setClassesPaths(final List<Path> classesPaths) {
        this.classesPaths = classesPaths;
        return this;
    }

    public boolean isAddPackages() {
        return addPackages;
    }

    public ModuleInfoCreator setAddPackages(final boolean addPackages) {
        this.addPackages = addPackages;
        return this;
    }

    public boolean isAddMandatory() {
        return addMandatory;
    }

    public ModuleInfoCreator setAddMandatory(final boolean addMandatory) {
        this.addMandatory = addMandatory;
        return this;
    }

    public boolean isAddExports() {
        return addExports;
    }

    public ModuleInfoCreator setAddExports(final boolean addExports) {
        this.addExports = addExports;
        return this;
    }

    public boolean isDetectUses() {
        return detectUses;
    }

    public ModuleInfoCreator setDetectUses(final boolean detectUses) {
        this.detectUses = detectUses;
        return this;
    }

    public boolean isDetectProvides() {
        return detectProvides;
    }

    public ModuleInfoCreator setDetectProvides(final boolean detectProvides) {
        this.detectProvides = detectProvides;
        return this;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public ModuleInfoCreator setModuleVersion(final String moduleVersion) {
        this.moduleVersion = moduleVersion;
        return this;
    }

    public String getModuleName() {
        return moduleName;
    }

    public ModuleInfoCreator setModuleName(final String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getDefaultModuleName() {
        return defaultModuleName;
    }

    public void setDefaultModuleName(final String moduleName) {
        defaultModuleName = moduleName;
    }

    public boolean isDetectVersion() {
        return detectVersion;
    }

    public ModuleInfoCreator setDetectVersion(final boolean detectVersion) {
        this.detectVersion = detectVersion;
        return this;
    }

    public void run() throws IOException, XMLStreamException {
        final Logger log = Logger.getLogger();
        final ClassVisitable<Exception> parser;
        if (moduleInfoYml == null) {
            log.debug("No module-info.yml given");
            parser = new EmptyModuleInfoGenerator();
        } else {
            log.info("Using source file: %s", moduleInfoYml);
            parser = new ModuleInfoYmlParser(moduleInfoYml);
        }
        final ClassWriter classWriter = new ClassWriter(0);

        ClassVisitor miWriter = new LogClassVisitor(classWriter);

        ClassPathsVisitor cpv = new ClassPathsVisitor();

        // phase 0: setup

        if (detectProvides) {
            log.debug("Detecting provides enabled");
            final Map<String, List<String>> providesNames = new TreeMap<>();
            cpv = DelegatingServiceClassPathsVisitor.of(cpv, ProvidesServiceVisitor::new, providesNames);
            miWriter = DelegatingModuleClassVisitor.of(miWriter, ProvidesAddingModuleVisitor::new, providesNames);
        }
        if (detectUses) {
            log.debug("Detecting uses enabled");
            final Set<String> usesNames = new TreeSet<>();
            cpv = DelegatingClassClassPathsVisitor.of(cpv, UsesClassVisitor::new, usesNames);
            miWriter = DelegatingModuleClassVisitor.of(miWriter, UsesAddingModuleVisitor::new, usesNames);
        }
        if (addMandatory) {
            log.debug("Adding mandatory enabled");
            miWriter = DelegatingModuleClassVisitor.of(miWriter, MandatoryAddingModuleVisitor::new);
        }
        if (addExports) {
            log.debug("Adding exports enabled");
            miWriter = DelegatingModuleClassVisitor.of(miWriter, ExportAddingModuleVisitor::new,
                    new HashSet<>(Arrays.asList("_private", "internal")));
        }
        if (moduleVersion != null) {
            log.debug("Overriding module version to \"%s\"", moduleVersion);
            miWriter = new ModuleVersionClassVisitor(miWriter, moduleVersion);
        }
        if (detectVersion) {
            log.debug("Detecting version");
            final Item<String> versionItem = new Item<>();
            cpv = DelegatingClassPathClassPathsVisitor.of(cpv, DetectVersionClassPathVisitor::new, versionItem);
            miWriter = new ModuleVersionClassVisitor(miWriter, versionItem);
        }
        if (defaultModuleName != null) {
            log.debug("Setting default module name to \"%s\"", defaultModuleName);
            miWriter = new DefaultModuleNameClassVisitor(miWriter, defaultModuleName);
        }
        if (moduleName != null) {
            log.debug("Overriding module name to \"%s\"", moduleName);
            miWriter = new ModuleNameClassVisitor(miWriter, moduleName);
        }
        if (addPackages) {
            log.debug("Adding packages enabled");
            final Set<String> packageNames = new TreeSet<>();
            cpv = DelegatingPackageClassPathsVisitor.of(cpv, PackageNamePackageVisitor::new, packageNames);
            miWriter = DelegatingModuleClassVisitor.of(miWriter, PackageAddingModuleVisitor::new, packageNames);
        }

        // phase 1: analysis

        for (Path classesPath : classesPaths) {
            log.debug("Processing class path root: %s", classesPath);
            if (Files.isRegularFile(classesPath)) {
                try (FileSystem fs = FileSystems.newFileSystem(new URI("jar:" + classesPath.toUri().toString() + "!/"),
                        Collections.emptyMap())) {
                    classesPath = fs.getRootDirectories().iterator().next();
                    final ClassPathReader reader = new ClassPathReader(classesPath);
                    reader.accept(cpv.visitClassPath());
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            } else {
                final ClassPathReader reader = new ClassPathReader(classesPath);
                reader.accept(cpv.visitClassPath());
            }
        }

        // phase 2: emission

        try {
            parser.accept(miWriter);
        } catch (IOException | XMLStreamException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Files.createDirectories(outputDirectory);
        final Path mic = outputDirectory.resolve("module-info.class");
        log.debug("Attempting to write \"%s\"", mic);
        try (OutputStream outputStream = Files.newOutputStream(mic)) {
            outputStream.write(classWriter.toByteArray());
        } catch (Exception e) {
            try {
                Files.deleteIfExists(mic);
            } catch (IOException e2) {
                log.debug(e2, "Failed to delete \"%s\" on failure", mic);
            }
        }
        log.info("Wrote module descriptor \"%s\"", mic);
    }
}
