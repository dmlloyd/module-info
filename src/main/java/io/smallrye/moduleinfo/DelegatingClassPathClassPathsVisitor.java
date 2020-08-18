package io.smallrye.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 */
public class DelegatingClassPathClassPathsVisitor<T> extends ClassPathsVisitor {
    private final BiFunction<ClassPathVisitor, T, ClassPathVisitor> wrapper;
    private final T arg;

    private DelegatingClassPathClassPathsVisitor(final ClassPathsVisitor delegate,
            final BiFunction<ClassPathVisitor, T, ClassPathVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingClassPathClassPathsVisitor(final ClassPathsVisitor delegate,
            final Function<ClassPathVisitor, ClassPathVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingClassPathClassPathsVisitor<T> of(final ClassPathsVisitor delegate,
            final BiFunction<ClassPathVisitor, T, ClassPathVisitor> wrapper, T arg) {
        return new DelegatingClassPathClassPathsVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingClassPathClassPathsVisitor<Void> of(final ClassPathsVisitor delegate,
            final Function<ClassPathVisitor, ClassPathVisitor> wrapper) {
        return new DelegatingClassPathClassPathsVisitor<>(delegate, wrapper);
    }

    public ClassPathVisitor visitClassPath() {
        return wrapper.apply(super.visitClassPath(), arg);
    }
}
