package io.smallrye.moduleinfo;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class UsesAddingModuleVisitor extends ModuleVisitor {
    private final Set<String> usesNames;
    private final Set<String> addedNames = new HashSet<>();

    public UsesAddingModuleVisitor(final ModuleVisitor mv, final Set<String> usesNames) {
        super(Opcodes.ASM7, mv);
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
