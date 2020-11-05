package io.github.dmlloyd.moduleinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class EmptyModuleInfoGenerator implements ClassVisitable<Exception> {

    public EmptyModuleInfoGenerator() {
    }

    public void accept(final ClassVisitor classVisitor) throws RuntimeException {
        classVisitor.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
        classVisitor.visitSource(null, null);
        final ModuleVisitor moduleVisitor = classVisitor.visitModule(null, 0, null);
        if (moduleVisitor != null) {
            moduleVisitor.visitEnd();
        }
        classVisitor.visitEnd();
    }
}
