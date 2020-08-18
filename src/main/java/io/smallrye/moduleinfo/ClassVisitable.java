package io.smallrye.moduleinfo;

import org.objectweb.asm.ClassVisitor;

/**
 */
public interface ClassVisitable<E extends Exception> {
    void accept(ClassVisitor classVisitor) throws E;
}
