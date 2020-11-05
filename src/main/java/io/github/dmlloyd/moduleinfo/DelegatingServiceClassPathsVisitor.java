package io.github.dmlloyd.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 */
public class DelegatingServiceClassPathsVisitor<T> extends ClassPathsVisitor {
    private final BiFunction<ServiceVisitor, T, ServiceVisitor> wrapper;
    private final T arg;

    private DelegatingServiceClassPathsVisitor(final ClassPathsVisitor delegate,
            final BiFunction<ServiceVisitor, T, ServiceVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingServiceClassPathsVisitor(final ClassPathsVisitor delegate,
            final Function<ServiceVisitor, ServiceVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingServiceClassPathsVisitor<T> of(final ClassPathsVisitor delegate,
            final BiFunction<ServiceVisitor, T, ServiceVisitor> wrapper, T arg) {
        return new DelegatingServiceClassPathsVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingServiceClassPathsVisitor<Void> of(final ClassPathsVisitor delegate,
            final Function<ServiceVisitor, ServiceVisitor> wrapper) {
        return new DelegatingServiceClassPathsVisitor<>(delegate, wrapper);
    }

    public ClassPathVisitor visitClassPath() {
        return DelegatingServiceClassPathVisitor.of(super.visitClassPath(), wrapper, arg);
    }
}
