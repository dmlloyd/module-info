package io.github.dmlloyd.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.objectweb.asm.ClassVisitor;

/**
 */
public class DelegatingClassClassPathsVisitor<T> extends ClassPathsVisitor {
    private final BiFunction<ClassVisitor, T, ClassVisitor> wrapper;
    private final T arg;

    private DelegatingClassClassPathsVisitor(final ClassPathsVisitor delegate,
            final BiFunction<ClassVisitor, T, ClassVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingClassClassPathsVisitor(final ClassPathsVisitor delegate,
            final Function<ClassVisitor, ClassVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingClassClassPathsVisitor<T> of(final ClassPathsVisitor delegate,
            final BiFunction<ClassVisitor, T, ClassVisitor> wrapper, T arg) {
        return new DelegatingClassClassPathsVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingClassClassPathsVisitor<Void> of(final ClassPathsVisitor delegate,
            final Function<ClassVisitor, ClassVisitor> wrapper) {
        return new DelegatingClassClassPathsVisitor<>(delegate, wrapper);
    }

    public ClassPathVisitor visitClassPath() {
        return DelegatingClassClassPathVisitor.of(super.visitClassPath(), wrapper, arg);
    }
}
