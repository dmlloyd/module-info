package io.github.dmlloyd.moduleinfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ModuleVisitor;

/**
 * A module visitor that adds the given list of packages.
 */
public class PackageAddingModuleVisitor extends ModuleVisitor {
    private final Collection<String> packageNames;
    private final Set<String> addedPackages = new HashSet<>();

    public PackageAddingModuleVisitor(final ModuleVisitor mv, final Collection<String> packageNames) {
        super(ModuleInfoCreator.ASM_VERSION, mv);
        this.packageNames = packageNames;
    }

    public void visitPackage(final String packaze) {
        addedPackages.add(packaze.replace('.', '/'));
        super.visitPackage(packaze.replace('.', '/'));
    }

    public void visitEnd() {
        final Logger logger = Logger.getLogger();
        for (String packageName : packageNames) {
            if (!addedPackages.contains(packageName)) {
                logger.info("Automatically adding package \"%s\"", packageName.replace('/', '.'));
                super.visitPackage(packageName);
            }
        }
        super.visitEnd();
    }
}
