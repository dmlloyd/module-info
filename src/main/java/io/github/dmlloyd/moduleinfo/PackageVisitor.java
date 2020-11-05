package io.github.dmlloyd.moduleinfo;

import org.objectweb.asm.ClassVisitor;

/**
 * A visitor for packages which follows the general ASM ethos.
 */
public class PackageVisitor {
    protected final PackageVisitor pv;

    public PackageVisitor(final PackageVisitor pv) {
        this.pv = pv;
    }

    public PackageVisitor() {
        this(null);
    }

    public void visit(String name) {
        if (pv != null) {
            pv.visit(name);
        }
    }

    public ClassVisitor visitClass() {
        if (pv != null) {
            return pv.visitClass();
        }
        return null;
    }

    public void visitEnd() {
        if (pv != null) {
            pv.visitEnd();
        }
    }
}
