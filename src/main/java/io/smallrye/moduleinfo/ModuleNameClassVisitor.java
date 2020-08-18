package io.smallrye.moduleinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class ModuleNameClassVisitor extends ClassVisitor {
    private final Item<String> moduleNameItem;

    public ModuleNameClassVisitor(final ClassVisitor cv, final Item<String> moduleNameItem) {
        super(Opcodes.ASM7, cv);
        this.moduleNameItem = moduleNameItem;
    }

    public ModuleNameClassVisitor(final ClassVisitor cv, final String moduleName) {
        this(cv, new Item<>(moduleName));
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        final String moduleName = moduleNameItem.getOrDefault(name);
        return super.visitModule(moduleName, access, version);
    }
}
