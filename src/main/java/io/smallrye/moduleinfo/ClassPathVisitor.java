package io.smallrye.moduleinfo;

import java.nio.file.Path;

/**
 */
public class ClassPathVisitor {
    protected final ClassPathVisitor cpv;

    public ClassPathVisitor(final ClassPathVisitor cpv) {
        this.cpv = cpv;
    }

    public ClassPathVisitor() {
        this(null);
    }

    public void visit(Path classPathRoot) {
        if (cpv != null) {
            cpv.visit(classPathRoot);
        }
    }

    public ManifestVisitor visitManifest() {
        if (cpv != null) {
            return cpv.visitManifest();
        }
        return null;
    }

    public PackageVisitor visitPackage() {
        if (cpv != null) {
            return cpv.visitPackage();
        }
        return null;
    }

    public ServiceVisitor visitService() {
        if (cpv != null) {
            return cpv.visitService();
        }
        return null;
    }
}
