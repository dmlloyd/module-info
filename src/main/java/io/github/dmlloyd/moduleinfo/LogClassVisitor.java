package io.github.dmlloyd.moduleinfo;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class LogClassVisitor extends ClassVisitor {
    public LogClassVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM7, cv);
    }

    public LogClassVisitor() {
        this(null);
    }

    public ModuleVisitor visitModule(final String name, final int access, final String version) {
        if (name == null)
            throw new IllegalArgumentException("No module name given");
        final Logger logger = Logger.getLogger();
        logger.info("Using module name \"%s\"", name);
        if (version != null) {
            logger.info("Using module version \"%s\"", version);
        }
        return new ModuleVisitor(Opcodes.ASM7, super.visitModule(name, access, version)) {
            public void visitMainClass(final String mainClass) {
                logger.debug("Using main-class \"%s\"", mainClass);
                super.visitMainClass(mainClass);
            }

            public void visitPackage(final String packaze) {
                logger.debug("Added package \"%s\"", packaze);
                super.visitPackage(packaze.replace('.', '/'));
            }

            public void visitRequire(final String module, final int access, final String version) {
                logger.debug("Added require \"%s\"", module);
                super.visitRequire(module, access, version);
            }

            public void visitExport(final String packaze, final int access, final String... modules) {
                logger.debug("Added export \"%s\"", packaze);
                super.visitExport(packaze, access, modules);
            }

            public void visitOpen(final String packaze, final int access, final String... modules) {
                logger.debug("Added open \"%s\"", packaze);
                super.visitOpen(packaze, access, modules);
            }

            public void visitUse(final String service) {
                logger.debug("Added use \"%s\"", service);
                super.visitUse(service);
            }

            public void visitProvide(final String service, final String... providers) {
                logger.debug("Added provide \"%s\"", service);
                super.visitProvide(service, providers);
            }

            public void visitEnd() {
                super.visitEnd();
            }
        };
    }
}
