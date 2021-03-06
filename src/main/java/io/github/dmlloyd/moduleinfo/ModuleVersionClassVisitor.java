package io.github.dmlloyd.moduleinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class ModuleVersionClassVisitor extends ClassVisitor {
    private final Item<String> versionItem;

    public ModuleVersionClassVisitor(final ClassVisitor cv, final Item<String> versionItem) {
        super(Opcodes.ASM7, cv);
        this.versionItem = versionItem;
    }

    public ModuleVersionClassVisitor(final ClassVisitor cv, final String version) {
        this(cv, new Item<>(version));
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        final String ourVersion = versionItem.getOrDefault(version);
        return super.visitModule(name, access, ourVersion);
    }
}
