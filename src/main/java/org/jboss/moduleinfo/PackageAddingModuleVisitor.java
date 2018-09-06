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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A module visitor that adds the given list of packages.
 */
public class PackageAddingModuleVisitor extends ModuleVisitor {
    private final Collection<String> packageNames;
    private final Set<String> addedPackages = new HashSet<>();

    public PackageAddingModuleVisitor(final ModuleVisitor mv, final Collection<String> packageNames) {
        super(Opcodes.ASM6, mv);
        this.packageNames = packageNames;
    }

    public void visitPackage(final String packaze) {
        addedPackages.add(packaze.replace('.', '/'));
        super.visitPackage(packaze);
    }

    public void visitEnd() {
        final Logger logger = Logger.getLogger();
        for (String packageName : packageNames) {
            if (! addedPackages.contains(packageName)) {
                logger.info("Automatically adding package \"%s\"", packageName.replace('/', '.'));
                super.visitPackage(packageName);
            }
        }
        super.visitEnd();
    }
}
