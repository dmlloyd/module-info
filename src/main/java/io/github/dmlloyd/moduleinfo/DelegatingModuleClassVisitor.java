package io.github.dmlloyd.moduleinfo;

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

    private DelegatingModuleClassVisitor(final ClassVisitor cv, final BiFunction<ModuleVisitor, T, ModuleVisitor> wrapper,
            T arg) {
        super(Opcodes.ASM7, cv);
        this.wrapper = wrapper;
        this.arg = arg;
    }

    public static DelegatingModuleClassVisitor<?> of(final ClassVisitor cv,
            final Function<ModuleVisitor, ModuleVisitor> wrapper) {
        return new DelegatingModuleClassVisitor<Function<ModuleVisitor, ModuleVisitor>>(cv, (v, t) -> t.apply(v), wrapper);
    }

    public static <T> DelegatingModuleClassVisitor<T> of(final ClassVisitor cv,
            final BiFunction<ModuleVisitor, T, ModuleVisitor> wrapper, T arg) {
        return new DelegatingModuleClassVisitor<>(cv, wrapper, arg);
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        return wrapper.apply(super.visitModule(name, access, version), arg);
    }
}
