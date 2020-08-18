package io.smallrye.moduleinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 *
 */
public class ClassPathReader {
    private final Path path;

    public ClassPathReader(Path path) {
        this.path = path;
    }

    public void accept(ClassPathVisitor classPathVisitor) throws IOException {
        if (classPathVisitor == null) {
            return;
        }
        classPathVisitor.visit(path);
        final Map<String, PackageVisitor> visitedPackages = new HashMap<>();
        final Set<String> skipPackages = new HashSet<>();
        final Path path = this.path;
        final Iterator<Path> iterator;
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(path)) {
            iterator = stream.iterator();
            while (iterator.hasNext()) {
                final Path item = iterator.next();
                final Path relative = path.relativize(item);
                final Path parent = relative.getParent();
                if (parent != null) {
                    final Path fileName = relative.getFileName();
                    if (fileName != null) {
                        final boolean isMetaInf = parent.getNameCount() > 0 && parent.getName(0).toString().equals("META-INF");
                        if (isMetaInf && parent.getNameCount() == 2 && parent.getName(1).toString().equals("services")) {
                            final String serviceName = fileName.toString();
                            final ServiceVisitor serviceVisitor = classPathVisitor.visitService();
                            if (serviceVisitor != null) {
                                serviceVisitor.visit(serviceName.replace('.', '/'));
                                try (BufferedReader reader = Files.newBufferedReader(item, StandardCharsets.UTF_8)) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        final String name = line.replaceAll("#.*", "").trim();
                                        if (!name.isEmpty()) {
                                            serviceVisitor.visitImplementation(name.replace('.', '/'));
                                        }
                                    }
                                }
                            }
                        } else if (isMetaInf && parent.getNameCount() == 2
                                && parent.getName(1).toString().equals("providers")) {
                            final String implName = fileName.toString();
                            try (BufferedReader reader = Files.newBufferedReader(item, StandardCharsets.UTF_8)) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    final String name = line.replaceAll("#.*", "").trim();
                                    if (!name.isEmpty()) {
                                        final ServiceVisitor serviceVisitor = classPathVisitor.visitService();
                                        serviceVisitor.visit(name.replace('.', '/'));
                                        serviceVisitor.visitImplementation(implName.replace('.', '/'));
                                    }
                                }
                            }
                        } else if (fileName.toString().endsWith(".class") && !isMetaInf) {
                            // it's a package
                            String packageName = parent.toString().replace('/', '.');
                            PackageVisitor packageVisitor;
                            if (!skipPackages.contains(packageName)) {
                                packageVisitor = visitedPackages.get(packageName);
                                if (packageVisitor == null) {
                                    packageVisitor = classPathVisitor.visitPackage();
                                    if (packageVisitor == null) {
                                        skipPackages.add(packageName);
                                        continue;
                                    }
                                    packageVisitor.visit(packageName);
                                    visitedPackages.put(packageName, packageVisitor);
                                }
                                final ClassVisitor classVisitor = packageVisitor.visitClass();
                                if (classVisitor != null) {
                                    ClassReader classReader;
                                    try (InputStream inputStream = Files.newInputStream(item)) {
                                        classReader = new ClassReader(inputStream);
                                    }
                                    classReader.accept(classVisitor, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
