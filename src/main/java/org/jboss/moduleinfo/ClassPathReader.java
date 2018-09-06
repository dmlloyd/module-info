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
 */
public class ClassPathReader {
    private final Path path;

    public ClassPathReader(Path path) {
        this.path = path;
    }

    public void accept(ClassPathVisitor classPathVisitor) throws IOException {
        classPathVisitor.visit(path);
        final Map<String, PackageVisitor> visitedPackages = new HashMap<>();
        final Set<String> skipPackages = new HashSet<>();
        final Path path = this.path;
        final Iterator<Path> iterator;
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
                                        if (! name.isEmpty()) {
                                            serviceVisitor.visitImplementation(name.replace('.', '/'));
                                        }
                                    }
                                }
                            }
                        } else if (fileName.toString().endsWith(".class") && ! isMetaInf) {
                            // it's a package
                            String packageName = parent.toString().replace('/', '.');
                            PackageVisitor packageVisitor;
                            if (! skipPackages.contains(packageName)) {
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
