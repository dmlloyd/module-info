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
public class ModuleVersionClassVisitor extends ClassVisitor {
    private final Item<String> versionItem;

    public ModuleVersionClassVisitor(final ClassVisitor cv, final Item<String> versionItem) {
        super(Opcodes.ASM6, cv);
        this.versionItem = versionItem;
    }

    public ModuleVersionClassVisitor(final ClassVisitor cv, final String version) {
        this(cv, new Item<>(version));
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        final String ourVersion = versionItem.getOrDefault(version);
        return super.visitModule(name, access, ourVersion);
    }
}
