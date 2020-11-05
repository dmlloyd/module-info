package io.github.dmlloyd.moduleinfo;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 */
public class DelegatingServiceClassPathVisitor<T> extends ClassPathVisitor {
    private final BiFunction<ServiceVisitor, T, ServiceVisitor> wrapper;
    private final T arg;

    private DelegatingServiceClassPathVisitor(final ClassPathVisitor delegate,
            final BiFunction<ServiceVisitor, T, ServiceVisitor> wrapper, T arg) {
        super(delegate);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    private DelegatingServiceClassPathVisitor(final ClassPathVisitor delegate,
            final Function<ServiceVisitor, ServiceVisitor> wrapper) {
        this(delegate, (packageVisitor, t) -> wrapper.apply(packageVisitor), null);
    }

    public static <T> DelegatingServiceClassPathVisitor<T> of(final ClassPathVisitor delegate,
            final BiFunction<ServiceVisitor, T, ServiceVisitor> wrapper, T arg) {
        return new DelegatingServiceClassPathVisitor<>(delegate, wrapper, arg);
    }

    public static DelegatingServiceClassPathVisitor<Void> of(final ClassPathVisitor delegate,
            final Function<ServiceVisitor, ServiceVisitor> wrapper) {
        return new DelegatingServiceClassPathVisitor<>(delegate, wrapper);
    }

    public ServiceVisitor visitService() {
        return wrapper.apply(super.visitService(), arg);
    }
}
