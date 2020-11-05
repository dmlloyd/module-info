package io.github.dmlloyd.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.objectweb.asm.ClassVisitor;

/**
 */
public class DelegatingClassClassPathVisitor<T> extends ClassPathVisitor {
    private final BiFunction<ClassVisitor, T, ClassVisitor> wrapper;
    private final T arg;

    private DelegatingClassClassPathVisitor(final ClassPathVisitor delegate,
            final BiFunction<ClassVisitor, T, ClassVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingClassClassPathVisitor(final ClassPathVisitor delegate,
            final Function<ClassVisitor, ClassVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingClassClassPathVisitor<T> of(final ClassPathVisitor delegate,
            final BiFunction<ClassVisitor, T, ClassVisitor> wrapper, T arg) {
        return new DelegatingClassClassPathVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingClassClassPathVisitor<Void> of(final ClassPathVisitor delegate,
            final Function<ClassVisitor, ClassVisitor> wrapper) {
        return new DelegatingClassClassPathVisitor<>(delegate, wrapper);
    }

    public PackageVisitor visitPackage() {
        return DelegatingClassPackageVisitor.of(super.visitPackage(), wrapper, arg);
    }
}
