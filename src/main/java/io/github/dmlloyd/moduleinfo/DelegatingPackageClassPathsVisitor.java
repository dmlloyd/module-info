package io.github.dmlloyd.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 */
public class DelegatingPackageClassPathsVisitor<T> extends ClassPathsVisitor {
    private final BiFunction<PackageVisitor, T, PackageVisitor> wrapper;
    private final T arg;

    private DelegatingPackageClassPathsVisitor(final ClassPathsVisitor delegate,
            final BiFunction<PackageVisitor, T, PackageVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingPackageClassPathsVisitor(final ClassPathsVisitor delegate,
            final Function<PackageVisitor, PackageVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingPackageClassPathsVisitor<T> of(final ClassPathsVisitor delegate,
            final BiFunction<PackageVisitor, T, PackageVisitor> wrapper, T arg) {
        return new DelegatingPackageClassPathsVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingPackageClassPathsVisitor<Void> of(final ClassPathsVisitor delegate,
            final Function<PackageVisitor, PackageVisitor> wrapper) {
        return new DelegatingPackageClassPathsVisitor<>(delegate, wrapper);
    }

    public ClassPathVisitor visitClassPath() {
        return DelegatingPackageClassPathVisitor.of(super.visitClassPath(), wrapper, arg);
    }
}
