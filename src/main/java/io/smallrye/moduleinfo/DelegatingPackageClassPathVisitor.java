package io.smallrye.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 */
public class DelegatingPackageClassPathVisitor<T> extends ClassPathVisitor {
    private final BiFunction<PackageVisitor, T, PackageVisitor> wrapper;
    private final T arg;

    private DelegatingPackageClassPathVisitor(final ClassPathVisitor delegate,
            final BiFunction<PackageVisitor, T, PackageVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingPackageClassPathVisitor(final ClassPathVisitor delegate,
            final Function<PackageVisitor, PackageVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingPackageClassPathVisitor<T> of(final ClassPathVisitor delegate,
            final BiFunction<PackageVisitor, T, PackageVisitor> wrapper, T arg) {
        return new DelegatingPackageClassPathVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingPackageClassPathVisitor<Void> of(final ClassPathVisitor delegate,
            final Function<PackageVisitor, PackageVisitor> wrapper) {
        return new DelegatingPackageClassPathVisitor<>(delegate, wrapper);
    }

    public PackageVisitor visitPackage() {
        return wrapper.apply(super.visitPackage(), arg);
    }
}
