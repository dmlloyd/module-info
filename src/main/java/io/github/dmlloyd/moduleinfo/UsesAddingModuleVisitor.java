package io.github.dmlloyd.moduleinfo;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ModuleVisitor;

/**
 */
public class UsesAddingModuleVisitor extends ModuleVisitor {
    private final Set<String> usesNames;
    private final Set<String> addedNames = new HashSet<>();

    public UsesAddingModuleVisitor(final ModuleVisitor mv, final Set<String> usesNames) {
        super(ModuleInfoCreator.ASM_VERSION, mv);
        this.usesNames = usesNames;
    }

    public void visitUse(final String service) {
        addedNames.add(service);
        super.visitUse(service);
    }

    public void visitEnd() {
        final Logger logger = Logger.getLogger();
        for (String usesName : usesNames) {
            if (!addedNames.contains(usesName)) {
                logger.info("Automatically adding use for service \"%s\"", usesName.replace('/', '.'));
                super.visitUse(usesName);
            }
        }
        super.visitEnd();
    }
}
