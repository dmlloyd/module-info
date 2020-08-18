package io.smallrye.moduleinfo;

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class MandatoryAddingModuleVisitor extends ModuleVisitor {
    private boolean gotIt = false;

    public MandatoryAddingModuleVisitor(final int api, final ModuleVisitor mv) {
        super(api, mv);
    }

    public MandatoryAddingModuleVisitor(final ModuleVisitor mv) {
        this(Opcodes.ASM7, mv);
    }

    public void visitRequire(final String module, final int access, final String version) {
        if (module.equals("java.base"))
            gotIt = true;
        super.visitRequire(module, access, version);
    }

    public void visitEnd() {
        if (!gotIt) {
            Logger.getLogger().info("Automatically adding mandatory \"java.base\" dependency");
            super.visitRequire("java.base", Opcodes.ACC_MANDATED, null);
        }
        super.visitEnd();
    }
}
