package io.smallrye.moduleinfo;

/**
 */
public class ClassPathsVisitor {
    protected final ClassPathsVisitor cpv;

    public ClassPathsVisitor(final ClassPathsVisitor cpv) {
        this.cpv = cpv;
    }

    public ClassPathsVisitor() {
        this(null);
    }

    public ClassPathVisitor visitClassPath() {
        if (cpv != null) {
            return cpv.visitClassPath();
        }
        return null;
    }
}
