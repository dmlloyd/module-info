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

import java.util.function.BiFunction;
import java.util.function.Function;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class DelegatingModuleClassVisitor<T> extends ClassVisitor {
    private final BiFunction<ModuleVisitor, T, ModuleVisitor> wrapper;
    private final T arg;

    private DelegatingModuleClassVisitor(final ClassVisitor cv, final Function<ModuleVisitor, ModuleVisitor> wrapper) {
        this(cv, (moduleVisitor, t) -> wrapper.apply(moduleVisitor), null);
    }

    private DelegatingModuleClassVisitor(final ClassVisitor cv, final BiFunction<ModuleVisitor, T, ModuleVisitor> wrapper, T arg) {
        super(Opcodes.ASM7, cv);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    public static DelegatingModuleClassVisitor<Void> of(final ClassVisitor cv, final Function<ModuleVisitor, ModuleVisitor> wrapper) {
        return new DelegatingModuleClassVisitor<Void>(cv, wrapper);
    }

    public static <T> DelegatingModuleClassVisitor<T> of(final ClassVisitor cv, final BiFunction<ModuleVisitor, T, ModuleVisitor> wrapper, T arg) {
        return new DelegatingModuleClassVisitor<>(cv, wrapper, arg);
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        return wrapper.apply(super.visitModule(name, access, version), arg);
    }
}
