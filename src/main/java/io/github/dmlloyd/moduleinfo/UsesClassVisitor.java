package io.github.dmlloyd.moduleinfo;

import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

/**
 */
public class UsesClassVisitor extends ClassVisitor {
    private final Set<String> usesNames;

    public UsesClassVisitor(final ClassVisitor cv, final Set<String> usesNames) {
        super(ModuleInfoCreator.ASM_VERSION, cv);
        this.usesNames = usesNames;
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName,
            final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final MethodVisitor omv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodNode(ModuleInfoCreator.ASM_VERSION, access, name, desc, signature, exceptions) {
            public void visitEnd() {
                if (instructions.size() > 0)
                    try {
                        Analyzer<BasicValue> a = new Analyzer<>(new BasicInterpreter(ModuleInfoCreator.ASM_VERSION) {
                            public BasicValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
                                if (insn instanceof LdcInsnNode) {
                                    final Object cst = ((LdcInsnNode) insn).cst;
                                    if (cst instanceof Type) {
                                        final Type type = (Type) cst;
                                        if (type.getSort() == Type.OBJECT) {
                                            return new ClassRefValue(type);
                                        }
                                    }
                                }
                                return super.newOperation(insn);
                            }
                        });
                        a.analyze(name, this);
                        final ListIterator<AbstractInsnNode> it = instructions.iterator();
                        while (it.hasNext()) {
                            final AbstractInsnNode insnNode = it.next();
                            if (insnNode instanceof MethodInsnNode) {
                                final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                                if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
                                    if (methodInsnNode.name.equals("load")
                                            && methodInsnNode.owner.equals("java/util/ServiceLoader")) {
                                        final String desc = methodInsnNode.desc;
                                        final Type methodType = Type.getMethodType(desc);
                                        final Type[] argumentTypes = methodType.getArgumentTypes();
                                        final int classArg = argumentTypes.length == 2
                                                && argumentTypes[1].getInternalName().equals("java/lang/Class") ? 1 : 0;
                                        final Frame<BasicValue> frame = a.getFrames()[it.previousIndex()];
                                        final int stackSize = frame.getStackSize();
                                        if (stackSize >= classArg + 1) {
                                            final BasicValue value = frame.getStack(classArg);
                                            if (value instanceof ClassRefValue) {
                                                usesNames.add(((ClassRefValue) value).getRefType().getInternalName());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (omv != null)
                            accept(omv);
                    } catch (AnalyzerException e) {
                        // ignore and move on
                    }
            }
        };
    }

    static final class ClassRefValue extends BasicValue {
        private static final Type clazzType = Type.getType(Class.class);
        private final Type refType;

        ClassRefValue(final Type type) {
            super(clazzType);
            this.refType = type;
        }

        Type getRefType() {
            return refType;
        }
    }
}
