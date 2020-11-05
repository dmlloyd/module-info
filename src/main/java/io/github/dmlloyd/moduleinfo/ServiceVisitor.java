package io.github.dmlloyd.moduleinfo;

/**
 */
public class ServiceVisitor {
    protected final ServiceVisitor sv;

    public ServiceVisitor(final ServiceVisitor sv) {
        this.sv = sv;
    }

    public ServiceVisitor() {
        this(null);
    }

    public void visit(String serviceName) {
        if (sv != null) {
            sv.visit(serviceName);
        }
    }

    public void visitImplementation(String implName) {
        if (sv != null) {
            sv.visitImplementation(implName);
        }
    }
}
