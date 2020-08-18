package io.smallrye.moduleinfo;

import java.util.Set;

/**
 */
public class PackageNamePackageVisitor extends PackageVisitor {
    private final Set<String> packageNames;

    public PackageNamePackageVisitor(final PackageVisitor pv, final Set<String> packageNames) {
        super(pv);
        this.packageNames = packageNames;
    }

    public void visit(final String name) {
        packageNames.add(name);
        super.visit(name);
    }
}
