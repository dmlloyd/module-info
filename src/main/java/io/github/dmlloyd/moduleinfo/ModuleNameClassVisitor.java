package io.github.dmlloyd.moduleinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;

/**
 */
public class ModuleNameClassVisitor extends ClassVisitor {
    private final Item<String> moduleNameItem;

    public ModuleNameClassVisitor(final ClassVisitor cv, final Item<String> moduleNameItem) {
        super(ModuleInfoCreator.ASM_VERSION, cv);
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
