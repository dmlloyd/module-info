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

/**
 */
public class DelegatingPackageClassPathVisitor<T> extends ClassPathVisitor {
    private final BiFunction<PackageVisitor, T, PackageVisitor> wrapper;
    private final T arg;

    private DelegatingPackageClassPathVisitor(final ClassPathVisitor delegate, final BiFunction<PackageVisitor, T, PackageVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingPackageClassPathVisitor(final ClassPathVisitor delegate, final Function<PackageVisitor, PackageVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingPackageClassPathVisitor<T> of(final ClassPathVisitor delegate, final BiFunction<PackageVisitor, T, PackageVisitor> wrapper, T arg) {
        return new DelegatingPackageClassPathVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingPackageClassPathVisitor<Void> of(final ClassPathVisitor delegate, final Function<PackageVisitor, PackageVisitor> wrapper) {
        return new DelegatingPackageClassPathVisitor<>(delegate, wrapper);
    }

    public PackageVisitor visitPackage() {
        return wrapper.apply(super.visitPackage(), arg);
    }
}
