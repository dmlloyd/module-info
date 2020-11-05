package io.github.dmlloyd.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.objectweb.asm.ClassVisitor;

/**
 */
public class DelegatingClassPackageVisitor<T> extends PackageVisitor {
    private final BiFunction<ClassVisitor, T, ClassVisitor> wrapper;
    private final T arg;

    private DelegatingClassPackageVisitor(final PackageVisitor delegate,
            final BiFunction<ClassVisitor, T, ClassVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingClassPackageVisitor(final PackageVisitor delegate, final Function<ClassVisitor, ClassVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingClassPackageVisitor<T> of(final PackageVisitor delegate,
            final BiFunction<ClassVisitor, T, ClassVisitor> wrapper, T arg) {
        return new DelegatingClassPackageVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingClassPackageVisitor<Void> of(final PackageVisitor delegate,
            final Function<ClassVisitor, ClassVisitor> wrapper) {
        return new DelegatingClassPackageVisitor<>(delegate, wrapper);
    }

    public ClassVisitor visitClass() {
        return wrapper.apply(super.visitClass(), arg);
    }
}
