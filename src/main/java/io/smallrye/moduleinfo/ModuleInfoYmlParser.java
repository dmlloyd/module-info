package io.smallrye.moduleinfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.yaml.snakeyaml.Yaml;

/**
 * A {@code module-info.yml} parser.
 */
public class ModuleInfoYmlParser implements ClassVisitable<Exception> {
    private static final String[] NO_STRINGS = new String[0];
    private final Path moduleInfoYml;

    /**
     * Construct a new instance.
     *
     * @param moduleInfoYml the path to the {@code module-info.yml} file
     */
    public ModuleInfoYmlParser(final Path moduleInfoYml) {
        Objects.requireNonNull(moduleInfoYml, "moduleInfoYaml");
        this.moduleInfoYml = moduleInfoYml;
    }

    /**
     * Read the {@code module-info.yml} file into the given class visitor.
     *
     * @param classVisitor the class visitor (must not be {@code null})
     * @throws IOException if an I/O exception occurred
     */
    public void accept(final ClassVisitor classVisitor) throws IOException {
        ModuleInfoYml moduleInfo;
        try (InputStream inputStream = Files.newInputStream(moduleInfoYml, StandardOpenOption.READ)) {
            Yaml yaml = new Yaml();
            moduleInfo = yaml.loadAs(inputStream, ModuleInfoYml.class);
        }
        parseRoot(moduleInfo, classVisitor);
    }

    private void parseRoot(ModuleInfoYml moduleInfo, ClassVisitor cv) {
        cv.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
        String name = moduleInfo.getName();
        String version = moduleInfo.getVersion();
        boolean open = moduleInfo.isOpen();
        boolean synthetic = moduleInfo.isSynthetic();
        boolean mandated = moduleInfo.isMandated();
        if (name == null) {
            throw new IllegalArgumentException("No module name given");
        }
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
        String sourceFile = moduleInfo.getSourceFile();
        if (sourceFile == null) {
            sourceFile = moduleInfoYml.getFileName().toString();
        }
        if (sourceFile != null) {
            cv.visitSource(sourceFile, null);
        }
        final ModuleVisitor moduleVisitor = cv.visitModule(name, flags, version);
        String mainClass = moduleInfo.getMainClass();
        if (mainClass != null) {
            moduleVisitor.visitMainClass(mainClass.replace('.', '/'));
        }
        List<String> packages = moduleInfo.getPackages();
        if (packages != null) {
            for (String packageName : packages) {
                moduleVisitor.visitPackage(packageName.replace('.', '/'));
            }
        }
        List<ModuleInfoYml.Export> exports = moduleInfo.getExports();
        if (exports != null) {
            for (ModuleInfoYml.Export export : exports) {
                flags = 0;
                if (export.isSynthetic()) {
                    flags |= Opcodes.ACC_SYNTHETIC;
                }
                if (export.isMandated()) {
                    flags |= Opcodes.ACC_MANDATED;
                }
                List<String> exportTo = export.getTo();
                moduleVisitor.visitExport(export.getPackage().replace('.', '/'), flags,
                        exportTo == null ? null : exportTo.toArray(NO_STRINGS));
            }
        }
        List<ModuleInfoYml.Export> opens = moduleInfo.getOpens();
        if (opens != null) {
            for (ModuleInfoYml.Export export : opens) {
                flags = 0;
                if (export.isSynthetic()) {
                    flags |= Opcodes.ACC_SYNTHETIC;
                }
                if (export.isMandated()) {
                    flags |= Opcodes.ACC_MANDATED;
                }
                List<String> exportTo = export.getTo();
                moduleVisitor.visitOpen(export.getPackage().replace('.', '/'), flags,
                        exportTo == null ? null : exportTo.toArray(NO_STRINGS));
            }
        }
        List<String> uses = moduleInfo.getUses();
        if (uses != null) {
            for (String use : uses) {
                moduleVisitor.visitUse(use.replace('.', '/'));
            }
        }
        List<ModuleInfoYml.Require> requires = moduleInfo.getRequires();
        if (requires != null) {
            for (ModuleInfoYml.Require require : requires) {
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
        List<ModuleInfoYml.Provide> provides = moduleInfo.getProvides();
        if (provides != null) {
            for (ModuleInfoYml.Provide provide : provides) {
                List<String> with = provide.getWith();
                if (with != null) {
                    moduleVisitor.visitProvide(provide.getServiceType().replace('.', '/'),
                            with.stream().map(i -> i.replace('.', '/')).collect(Collectors.toList()).toArray(NO_STRINGS));
                }
            }
        }
        List<ModuleInfoYml.Annotation> annotations = moduleInfo.getAnnotations();
        if (annotations != null) {
            for (ModuleInfoYml.Annotation annotation : annotations) {
                processAnnotation(cv, annotation);
            }
        }
        moduleVisitor.visitEnd();
        cv.visitEnd();
    }

    private void processAnnotation(final ClassVisitor cv, final ModuleInfoYml.Annotation annotation) {
        AnnotationVisitor annotationVisitor = cv.visitAnnotation(annotation.getType().replace('.', '/'),
                annotation.isVisible());
        processAnnotation(annotation, annotationVisitor);
    }

    private void processAnnotation(final ModuleInfoYml.Annotation annotation, final AnnotationVisitor annotationVisitor) {
        Map<String, Object> values = annotation.getValues();
        if (values != null) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                processAnnotationValue(annotation, annotationVisitor, entry.getKey(), entry.getValue());
            }
        }
    }

    private void processAnnotationValue(final ModuleInfoYml.Annotation annotation, final AnnotationVisitor visitor,
            final String name, final Object value) {
        if (value instanceof Number || value instanceof String) {
            visitor.visit(name, value);
        } else if (value instanceof List) {
            AnnotationVisitor arrayVisitor = visitor.visitArray(name);
            processAnnotationValues(annotation, arrayVisitor, (List<?>) value);
        } else if (value instanceof ModuleInfoYml.Annotation) {
            ModuleInfoYml.Annotation ann = (ModuleInfoYml.Annotation) value;
            processAnnotation(ann, visitor.visitAnnotation(name, "L" + ann.getType().replace('.', '/') + ";"));
        } else {
            throw new IllegalArgumentException("Unsupported annotation value type: " + value.getClass());
        }
    }

    private void processAnnotationValues(final ModuleInfoYml.Annotation annotation, final AnnotationVisitor visitor,
            final List<?> list) {
        for (Object value : list) {
            processAnnotationValue(annotation, visitor, null, value);
        }
    }
}
