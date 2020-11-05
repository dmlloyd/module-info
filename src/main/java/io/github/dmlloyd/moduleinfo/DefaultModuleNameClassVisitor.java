package io.github.dmlloyd.moduleinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class DefaultModuleNameClassVisitor extends ClassVisitor {
    private final String moduleName;

    public DefaultModuleNameClassVisitor(final ClassVisitor cv, final String moduleName) {
        super(Opcodes.ASM7, cv);
        this.moduleName = moduleName;
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        return super.visitModule(name == null ? moduleName : name, access, version);
    }
}
