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

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class UsesAddingModuleVisitor extends ModuleVisitor {
    private final Set<String> usesNames;
    private final Set<String> addedNames = new HashSet<>();

    public UsesAddingModuleVisitor(final ModuleVisitor mv, final Set<String> usesNames) {
        super(Opcodes.ASM6, mv);
        this.usesNames = usesNames;
    }

    public void visitUse(final String service) {
        addedNames.add(service);
        super.visitUse(service);
    }

    public void visitEnd() {
        final Logger logger = Logger.getLogger();
        for (String usesName : usesNames) {
            if (! addedNames.contains(usesName)) {
                logger.info("Automatically adding use for service \"%s\"", usesName.replace('/', '.'));
                super.visitUse(usesName);
            }
        }
        super.visitEnd();
    }
}
