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

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class MandatoryAddingModuleVisitor extends ModuleVisitor {
    private boolean gotIt = false;

    public MandatoryAddingModuleVisitor(final int api, final ModuleVisitor mv) {
        super(api, mv);
    }

    public MandatoryAddingModuleVisitor(final ModuleVisitor mv) {
        this(Opcodes.ASM6, mv);
    }

    public void visitRequire(final String module, final int access, final String version) {
        if (module.equals("java.base")) gotIt = true;
        super.visitRequire(module, access, version);
    }

    public void visitEnd() {
        if (! gotIt) {
            Logger.getLogger().info("Automatically adding mandatory \"java.base\" dependency");
            super.visitRequire("java.base", Opcodes.ACC_MANDATED, null);
        }
        super.visitEnd();
    }
}
