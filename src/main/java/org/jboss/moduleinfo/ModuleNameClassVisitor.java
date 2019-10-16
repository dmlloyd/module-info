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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class ModuleNameClassVisitor extends ClassVisitor {
    private final Item<String> moduleNameItem;

    public ModuleNameClassVisitor(final ClassVisitor cv, final Item<String> moduleNameItem) {
        super(Opcodes.ASM7, cv);
        this.moduleNameItem = moduleNameItem;
    }

    public ModuleNameClassVisitor(final ClassVisitor cv, final String moduleName) {
        this(cv, new Item<>(moduleName));
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        final String moduleName = moduleNameItem.getOrDefault(name);
        return super.visitModule(moduleName, access, version);
    }
}
