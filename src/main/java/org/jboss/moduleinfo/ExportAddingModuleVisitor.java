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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A module visitor that adds the given list of packages as exports.
 */
public class ExportAddingModuleVisitor extends ModuleVisitor {
    private final Set<String> nonExportParts;
    private final Set<String> unexportedPackages = new TreeSet<>();
    private final Set<String> exportedPackages = new HashSet<>();

    public ExportAddingModuleVisitor(final ModuleVisitor mv, final Set<String> nonExportParts) {
        super(Opcodes.ASM6, mv);
        this.nonExportParts = nonExportParts;
    }

    public void visitExport(final String packaze, final int access, final String... modules) {
        exportedPackages.add(packaze.replace('.', '/'));
        super.visitExport(packaze, access, modules);
    }

    public void visitPackage(final String packaze) {
        unexportedPackages.add(packaze.replace('.', '/'));
        super.visitPackage(packaze);
    }

    public void visitEnd() {
        final Logger logger = Logger.getLogger();
        outer: for (String packageName : unexportedPackages) {
            if (! exportedPackages.contains(packageName)) {
                final List<String> list = Arrays.asList(packageName.split("[./]"));
                for (String part : list) {
                    if (nonExportParts.contains(part)) {
                        logger.info("Not adding export for private package \"%s\"", packageName.replace('/', '.'));
                        continue outer;
                    }
                }
                logger.info("Automatically adding export for package \"%s\"", packageName.replace('/', '.'));
                super.visitExport(packageName.replace('.', '/'), 0, (String[]) null);
            }
        }
        super.visitEnd();
    }
}
