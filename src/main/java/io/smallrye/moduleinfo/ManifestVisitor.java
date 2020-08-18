package io.smallrye.moduleinfo;

import java.util.jar.Manifest;

/**
 */
public class ManifestVisitor {
    protected final ManifestVisitor sv;

    public ManifestVisitor(final ManifestVisitor sv) {
        this.sv = sv;
    }

    public ManifestVisitor() {
        this(null);
    }

    public void visit(Manifest manifest) {
        if (sv != null) {
            sv.visit(manifest);
        }
    }
}
