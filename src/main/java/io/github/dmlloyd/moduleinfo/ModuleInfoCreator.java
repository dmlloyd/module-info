package io.github.dmlloyd.moduleinfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.yaml.snakeyaml.Yaml;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;

/**
 */
@Parameters(separators = "=")
public class ModuleInfoCreator {
    static final int ASM_VERSION = Opcodes.ASM9;
    private static final String[] NO_STRINGS = new String[0];

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
    @Parameter(names = "--export-excludes", arity = 1)
    private String exportExcludes = "^.*\\b(internal|_private|private_)\\b.*$";
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

    public String getExportExcludes() {
        return exportExcludes;
    }

    public ModuleInfoCreator setExportExcludes(String exportExcludes) {
        this.exportExcludes = exportExcludes;
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
        if (moduleInfoYml == null) {
            log.debug("No module-info.yml given");
        } else {
            log.info("Using source file: %s", moduleInfoYml);
        }
        final ClassWriter classWriter = new ClassWriter(0);

        ClassVisitor miWriter = new LogClassVisitor(classWriter);

        // phase 0: setup

        if (addMandatory) {
            log.debug("Adding mandatory enabled");
        }
        if (addExports) {
            log.debug("Adding exports enabled");
        }
        if (moduleVersion != null) {
            log.debug("Overriding module version to \"%s\"", moduleVersion);
        }
        if (detectVersion) {
            log.debug("Detecting version");
        }
        if (defaultModuleName != null) {
            log.debug("Setting default module name to \"%s\"", defaultModuleName);
        }
        if (moduleName != null) {
            log.debug("Overriding module name to \"%s\"", moduleName);
        }
        if (addPackages) {
            log.debug("Adding packages enabled");
        }

        // phase 1: analysis

        for (Path classesPath : classesPaths) {
            log.debug("Processing class path root: %s", classesPath);
            if (Files.isRegularFile(classesPath)) {
                try (FileSystem fs = FileSystems.newFileSystem(new URI("jar:" + classesPath.toUri().toString() + "!/"),
                        Collections.emptyMap())) {
                    classesPath = fs.getRootDirectories().iterator().next();
                    readClassPathItem(classesPath, Paths.get(""));
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            } else {
                readClassPathItem(classesPath, Paths.get(""));
            }
        }

        ModuleInfoYml moduleInfo;
        if (moduleInfoYml != null) {
            try (InputStream inputStream = Files.newInputStream(moduleInfoYml, StandardOpenOption.READ)) {
                Yaml yaml = new Yaml();
                moduleInfo = yaml.loadAs(inputStream, ModuleInfoYml.class);
            }
        } else {
            moduleInfo = null;
        }

        // phase 2: emission

        Files.createDirectories(outputDirectory);
        final Path mic = outputDirectory.resolve("module-info.class");
        log.debug("Attempting to write \"%s\"", mic);
        try (OutputStream outputStream = Files.newOutputStream(mic)) {
            miWriter.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
            String sourceName = moduleInfoYml == null ? null : moduleInfoYml.getFileName().toString();
            miWriter.visitSource(sourceName, null);
            String name = moduleName;
            if (name == null && moduleInfo != null) {
                name = moduleInfo.getName();
            }
            if (name == null) {
                name = defaultModuleName;
            }
            if (name == null) {
                name = detectedClassPathAutoModuleName;
            }
            if (name == null) {
                throw new IllegalArgumentException("No module name given or detected");
            }
            String version = this.moduleVersion;
            if (version == null && moduleInfo != null) {
                version = moduleInfo.getVersion();
            }
            if (version == null && detectVersion) {
                version = detectedClassPathVersion;
            }
            boolean open = moduleInfo != null && moduleInfo.isOpen();
            boolean synthetic = moduleInfo != null && moduleInfo.isSynthetic();
            boolean mandated = moduleInfo != null && moduleInfo.isMandated();
            int flags = 0;
            if (open) {
                flags |= Opcodes.ACC_OPEN;
            }
            if (synthetic) {
                flags |= Opcodes.ACC_SYNTHETIC;
            }
            if (mandated) {
                flags |= Opcodes.ACC_MANDATED;
            }
            final ModuleVisitor moduleVisitor = miWriter.visitModule(name, flags, version);
            if (moduleVisitor != null) {
                String mainClass = moduleInfo == null ? null : moduleInfo.getMainClass();
                if (mainClass != null) {
                    moduleVisitor.visitMainClass(mainClass.replace('.', '/'));
                }
                Set<String> packages = new HashSet<>();
                if (moduleInfo != null) {
                    List<String> list = moduleInfo.getPackages();
                    if (list != null) {
                        packages.addAll(list);
                    }
                }
                if (addPackages) {
                    packages.addAll(detectedClassPathPackages);
                }
                if (!packages.isEmpty()) {
                    List<String> sortedPackages = new ArrayList<>(packages);
                    Collections.sort(sortedPackages);
                    for (String packageName : sortedPackages) {
                        moduleVisitor.visitPackage(packageName.replace('.', '/'));
                    }
                }
                Map<String, ModuleExport> exports = new TreeMap<>();
                if (addExports) {
                    Pattern excludedPackages = Pattern.compile(exportExcludes);
                    for (String package_ : packages) {
                        if (!exports.containsKey(package_)) {
                            String normalizedPackageName = package_.replace('/', '.');
                            if (excludedPackages.matcher(normalizedPackageName).matches()) {
                                log.info("Not adding export for private package \"%s\"", normalizedPackageName);
                            } else {
                                log.info("Automatically adding export for package \"%s\"", normalizedPackageName);
                                exports.put(package_, new ModuleExport(package_, Collections.emptyList(), false, false));
                            }
                        }
                    }
                }
                if (moduleInfo != null) {
                    List<ModuleExport> moduleInfoExports = moduleInfo.getExports();
                    if (moduleInfoExports != null) {
                        for (ModuleExport export : moduleInfoExports) {
                            if (export.isPattern()) {
                                Pattern packagePattern = Pattern.compile(export.getPackage());
                                for (String package_ : packages) {
                                    if (packagePattern.matcher(package_).matches()) {
                                        addExport(export.withPackageName(package_), exports);
                                    }
                                }
                            } else {
                                addExport(export, exports);
                            }
                        }
                    }
                }
                for (ModuleExport export : exports.values()) {
                    List<String> to = export.getTo();
                    String[] array;
                    if (to == null) {
                        array = null;
                    } else {
                        array = new HashSet<>(to).toArray(NO_STRINGS);
                        Arrays.sort(array);
                    }
                    flags = 0;
                    if (export.isSynthetic()) {
                        flags |= Opcodes.ACC_SYNTHETIC;
                    }
                    if (export.isMandated()) {
                        flags |= Opcodes.ACC_MANDATED;
                    }
                    moduleVisitor.visitExport(export.getPackage().replace('.', '/'), flags, array);
                }
                Map<String, ModuleExport> opens = new TreeMap<>();
                if (moduleInfo != null) {
                    List<ModuleExport> moduleInfoOpens = moduleInfo.getOpens();
                    if (moduleInfoOpens != null) {
                        for (ModuleExport openInfo : moduleInfoOpens) {
                            if (openInfo.isPattern()) {
                                Pattern packagePattern = Pattern.compile(openInfo.getPackage());
                                for (String package_ : packages) {
                                    if (packagePattern.matcher(package_).matches()) {
                                        addExport(openInfo.withPackageName(package_), opens);
                                    }
                                }
                            } else {
                                addExport(openInfo, opens);
                            }
                        }
                    }
                }
                for (ModuleExport export : opens.values()) {
                    List<String> to = export.getTo();
                    String[] array;
                    if (to == null) {
                        array = null;
                    } else {
                        array = new HashSet<>(to).toArray(NO_STRINGS);
                        Arrays.sort(array);
                    }
                    flags = 0;
                    if (export.isSynthetic()) {
                        flags |= Opcodes.ACC_SYNTHETIC;
                    }
                    if (export.isMandated()) {
                        flags |= Opcodes.ACC_MANDATED;
                    }
                    moduleVisitor.visitOpen(export.getPackage().replace('.', '/'), flags, array);
                }
                Set<String> uses = new HashSet<>();
                if (detectUses) {
                    uses.addAll(detectedClassPathUsesNames);
                }
                if (moduleInfo != null) {
                    List<String> list = moduleInfo.getUses();
                    if (list != null) {
                        uses.addAll(list);
                    }
                }
                if (!uses.isEmpty()) {
                    ArrayList<String> usesList = new ArrayList<>(uses);
                    Collections.sort(usesList);
                    for (String use : usesList) {
                        moduleVisitor.visitUse(use.replace('.', '/'));
                    }
                }
                // todo: detect requires
                Map<String, ModuleRequire> requires = new HashMap<>();
                if (addMandatory) {
                    log.info("Automatically adding mandatory \"java.base\" dependency");
                    // We want the `java.base` to be added as non-synthetic
                    // so that there are no problems to use it with JDK21+ (see https://bugs.openjdk.org/browse/JDK-8299769)
                    requires.put("java.base", new ModuleRequire("java.base", null, false, false, true, false));
                }
                if (moduleInfo != null) {
                    List<ModuleRequire> list = moduleInfo.getRequires();
                    if (list != null) {
                        for (ModuleRequire require : list) {
                            ModuleRequire existing = requires.putIfAbsent(require.getModule(), require);
                            if (existing != null) {
                                // never weaken, only strengthen
                                if (require.isMandated())
                                    existing.setMandated(true);
                                if (!require.isStatic())
                                    existing.setStatic(false);
                                if (require.isSynthetic())
                                    existing.setSynthetic(true);
                                if (require.isTransitive())
                                    existing.setTransitive(true);
                                // override dep version
                                String ymlVersion = require.getVersion();
                                if (ymlVersion != null) {
                                    existing.setVersion(ymlVersion);
                                }
                            }
                        }
                    }
                }
                if (!requires.isEmpty()) {
                    List<ModuleRequire> list = new ArrayList<>(requires.values());
                    list.sort(((Comparator<ModuleRequire>) (a, b) -> Boolean.compare(b.isMandated(), a.isMandated()))
                            .thenComparing(ModuleRequire::getModule));
                    for (ModuleRequire require : list) {
                        flags = 0;
                        if (require.isSynthetic()) {
                            flags |= Opcodes.ACC_SYNTHETIC;
                        }
                        if (require.isMandated()) {
                            flags |= Opcodes.ACC_MANDATED;
                        }
                        if (require.isStatic()) {
                            flags |= Opcodes.ACC_STATIC_PHASE;
                        }
                        if (require.isTransitive()) {
                            flags |= Opcodes.ACC_TRANSITIVE;
                        }
                        moduleVisitor.visitRequire(require.getModule(), flags, require.getVersion());
                    }
                }
                Map<String, List<String>> provides = new HashMap<>();
                if (detectProvides) {
                    for (Map.Entry<String, List<String>> entry : detectedClassPathProvides.entrySet()) {
                        String serviceName = entry.getKey();
                        List<String> set = provides.computeIfAbsent(serviceName, ModuleInfoCreator::newList);
                        set.addAll(entry.getValue());
                    }
                }
                if (moduleInfo != null) {
                    List<ModuleProvide> list = moduleInfo.getProvides();
                    if (list != null) {
                        for (ModuleProvide provide : list) {
                            if (provide.getWith() != null) {
                                String serviceName = provide.getServiceType();
                                List<String> set = provides.computeIfAbsent(serviceName, ModuleInfoCreator::newList);
                                set.addAll(provide.getWith());
                            }
                        }
                    }
                }
                for (Map.Entry<String, List<String>> provide : provides.entrySet()) {
                    moduleVisitor.visitProvide(
                            provide.getKey().replace('.', '/'),
                            provide.getValue().stream().map(i -> i.replace('.', '/')).collect(Collectors.toList())
                                    .toArray(NO_STRINGS));
                }
                if (moduleInfo != null) {
                    List<ModuleAnnotation> annotations = moduleInfo.getAnnotations();
                    if (annotations != null) {
                        for (ModuleAnnotation annotation : annotations) {
                            processAnnotation(miWriter, annotation);
                        }
                    }
                }
                moduleVisitor.visitEnd();
            }
            miWriter.visitEnd();
            outputStream.write(classWriter.toByteArray());
        } catch (Exception e) {
            try {
                Files.deleteIfExists(mic);
            } catch (IOException e2) {
                log.debug(e2, "Failed to delete \"%s\" on failure", mic);
            }
            throw e;
        }
        log.info("Wrote module descriptor \"%s\"", mic);
    }

    private static void addExport(ModuleExport export, Map<String, ModuleExport> exports) {
        ModuleExport existing = exports.putIfAbsent(export.getPackage(), export);
        if (existing != null) {
            if (export.isSynthetic()) {
                existing.setSynthetic(true);
            }
            if (export.isMandated()) {
                existing.setMandated(true);
            }
            List<String> to = existing.getTo();
            if (to == null || to.isEmpty()) {
                existing.setTo(export.getTo());
            } else {
                Set<String> set = new HashSet<>();
                set.addAll(to);
                set.addAll(export.getTo());
                existing.setTo(new ArrayList<>(set));
            }
        }
    }

    private void processAnnotation(final ClassVisitor cv, final ModuleAnnotation annotation) {
        AnnotationVisitor annotationVisitor = cv.visitAnnotation(annotation.getType().replace('.', '/'),
                annotation.isVisible());
        processAnnotation(annotation, annotationVisitor);
    }

    private void processAnnotation(final ModuleAnnotation annotation, final AnnotationVisitor annotationVisitor) {
        Map<String, Object> values = annotation.getValues();
        if (values != null) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                processAnnotationValue(annotation, annotationVisitor, entry.getKey(), entry.getValue());
            }
        }
    }

    private void processAnnotationValue(final ModuleAnnotation annotation, final AnnotationVisitor visitor,
            final String name, final Object value) {
        if (value instanceof Number || value instanceof String) {
            visitor.visit(name, value);
        } else if (value instanceof List) {
            AnnotationVisitor arrayVisitor = visitor.visitArray(name);
            processAnnotationValues(annotation, arrayVisitor, (List<?>) value);
        } else if (value instanceof ModuleAnnotation) {
            ModuleAnnotation ann = (ModuleAnnotation) value;
            processAnnotation(ann, visitor.visitAnnotation(name, "L" + ann.getType().replace('.', '/') + ";"));
        } else {
            throw new IllegalArgumentException("Unsupported annotation value type: " + value.getClass());
        }
    }

    private void processAnnotationValues(final ModuleAnnotation annotation, final AnnotationVisitor visitor,
            final List<?> list) {
        for (Object value : list) {
            processAnnotationValue(annotation, visitor, null, value);
        }
    }

    // Path name queries

    private boolean isClassFile(final Path path) {
        int cnt = path.getNameCount();
        return cnt >= 1 && path.getName(cnt - 1).toString().endsWith(".class");
    }

    private boolean isMetaInf(final Path path) {
        return path.getNameCount() >= 1 && path.getName(0).toString().equals("META-INF");
    }

    private boolean isVersionDirectory(final Path path) {
        int cnt = path.getNameCount();
        return isMetaInf(path) && cnt == 3 && path.getName(1).toString().equals("versions")
                && isInt(path.getName(2).toString());
    }

    private boolean isInt(final String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }

    private boolean isManifest(final Path path) {
        int cnt = path.getNameCount();
        return isMetaInf(path) && cnt == 2 && path.getName(1).toString().equals("MANIFEST.MF");
    }

    private boolean isMetaInfServices(final Path path) {
        int cnt = path.getNameCount();
        return isMetaInf(path) && cnt == 3 && path.getName(1).toString().equals("services");
    }

    private boolean isMetaInfProviders(final Path path) {
        int cnt = path.getNameCount();
        return isMetaInf(path) && cnt == 3 && path.getName(1).toString().equals("providers");
    }

    // analyze

    final Map<String, List<String>> detectedClassPathProvides = new HashMap<>();
    final Set<String> detectedClassPathUsesNames = new HashSet<>();
    final Set<String> detectedClassPathPackages = new HashSet<>();
    String detectedClassPathVersion = null;
    String detectedClassPathAutoModuleName = null;

    void readClassPathItem(final Path basePath, final Path path) throws IOException {
        Path resolved = basePath.resolve(path);
        if (Files.isDirectory(resolved)) {
            if (isVersionDirectory(path)) {
                readClassPathItem(resolved, Paths.get(""));
            } else
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(resolved)) {
                    for (Path child : ds) {
                        readClassPathItem(basePath, basePath.relativize(child));
                    }
                }
        } else {
            readClassPathFile(basePath, path);
        }
    }

    void readClassPathFile(final Path basePath, final Path path) throws IOException {
        if (detectUses && isClassFile(path)) {
            readClassPathClassFile(basePath, path);
        } else if (detectProvides && isMetaInfServices(path)) {
            readClassPathServicesFile(basePath, path);
        } else if (detectProvides && isMetaInfProviders(path)) {
            readClassPathProvidersFile(basePath, path);
        } else if (isManifest(path)) {
            readClassPathManifest(basePath, path);
        }
    }

    private void readClassPathServicesFile(final Path basePath, final Path path) throws IOException {
        String serviceName = path.getName(2).toString();
        List<String> list = detectedClassPathProvides.computeIfAbsent(serviceName, ModuleInfoCreator::newList);
        try (BufferedReader reader = Files.newBufferedReader(basePath.resolve(path), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String implName = line.replaceAll("#.*", "").trim();
                if (!implName.isEmpty()) {
                    list.add(implName);
                }
            }
        }
    }

    private void readClassPathProvidersFile(final Path basePath, final Path path) throws IOException {
        String implName = path.getName(2).toString();
        List<String> list = detectedClassPathProvides.computeIfAbsent(implName, ModuleInfoCreator::newList);
        try (BufferedReader reader = Files.newBufferedReader(basePath.resolve(path), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String serviceName = line.replaceAll("#.*", "").trim();
                if (!serviceName.isEmpty()) {
                    list.add(serviceName);
                }
            }
        }
    }

    private void readClassPathManifest(final Path basePath, final Path path) throws IOException {
        Manifest manifest;
        try (InputStream is = Files.newInputStream(basePath.resolve(path))) {
            try (BufferedInputStream bis = new BufferedInputStream(is)) {
                manifest = new Manifest(bis);
            }
        }
        final String version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        if (version != null) {
            detectedClassPathVersion = version;
        }
        final String autoName = manifest.getMainAttributes().getValue("Automatic-Module-Name");
        if (autoName != null) {
            detectedClassPathAutoModuleName = autoName;
        }
    }

    private void readClassPathClassFile(final Path basePath, final Path path) throws IOException {
        Path parent = path.getParent();
        int cnt = parent == null ? 0 : parent.getNameCount();
        if (cnt > 0) {
            StringBuilder b = new StringBuilder(64);
            b.append(parent.getName(0));
            for (int i = 1; i < cnt; i++) {
                b.append('.');
                b.append(parent.getName(i));
            }
            detectedClassPathPackages.add(b.toString());
        }
        ClassReader cr;
        try (InputStream is = Files.newInputStream(basePath.resolve(path))) {
            cr = new ClassReader(is);
        }
        cr.accept(new UsesClassVisitor(null, detectedClassPathUsesNames), 0);
    }

    private static <T> List<T> newList(final Object ignored) {
        return new ArrayList<>();
    }

    private static <E> Set<E> newLinkedHashSet(final Object ignored) {
        return new LinkedHashSet<>();
    }
}
