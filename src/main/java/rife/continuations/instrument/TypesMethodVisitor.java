/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.*;

import rife.continuations.ContinuationConfigInstrument;

import java.util.*;
import java.util.logging.Level;

import static rife.continuations.instrument.ContinuationDebug.*;

class TypesMethodVisitor extends MethodVisitor implements Opcodes {
    private static final int T_BOOLEAN = 4;
    private static final int T_CHAR = 5;
    private static final int T_FLOAT = 6;
    private static final int T_DOUBLE = 7;
    private static final int T_BYTE = 8;
    private static final int T_SHORT = 9;
    private static final int T_INT = 10;
    private static final int T_LONG = 11;

    private final ContinuationConfigInstrument config_;
    private final TypesClassVisitor classVisitor_;
    private final String className_;
    private final String classNameInternal_;
    private final TypesNode rootNode_;
    private final HashMap<Label, TypesNode> labelMapping_;
    private final LinkedHashMap<Label, List<Label>> tryCatchHandlers_;
    private final NoOpAnnotationVisitor annotationVisitor_ = new NoOpAnnotationVisitor();
    private TypesNode currentNode_;
    private TypesContext[] pauseContexts_ = null;
    private TypesContext[] labelContexts_ = null;
    private int pauseCount_ = -1;
    private int labelCount_ = -1;

    TypesMethodVisitor(ContinuationConfigInstrument config, TypesClassVisitor classVisitor, String className) {
        super(ASM9);

        config_ = config;
        classVisitor_ = classVisitor;
        className_ = className;
        classNameInternal_ = className_.replace('.', '/');

        labelMapping_ = new HashMap<>();
        tryCatchHandlers_ = new LinkedHashMap<>();

        // pushes the first block onto the stack of blocks to be visited
        currentNode_ = new TypesNode();
        rootNode_ = currentNode_;
    }

    /**
     * Visits a local variable instruction. A local variable instruction is an
     * instruction that loads or stores the value of a local variable.
     *
     * @param opcode the opcode of the local variable instruction to be visited.
     *               This opcode is either ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE,
     *               LSTORE, FSTORE, DSTORE, ASTORE or RET.
     * @param var    the operand of the instruction to be visited. This operand is
     *               the index of a local variable.
     */
    public void visitVarInsn(int opcode, int var) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitVarInsn            (" + OPCODES[opcode] + ", " + var + ")");

        if (currentNode_ != null) {
            switch (opcode) {
                // store primitive variable
                case ISTORE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.SET, var, TypesContext.CAT1_INT));
                case FSTORE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.SET, var, TypesContext.CAT1_FLOAT));
                case LSTORE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.SET, var, TypesContext.CAT2_LONG));
                case DSTORE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.SET, var, TypesContext.CAT2_DOUBLE));

                // store reference var
                case ASTORE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.SET, var, null));

                // load primitive var
                case ILOAD -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.GET, var, TypesContext.CAT1_INT));
                case FLOAD -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.GET, var, TypesContext.CAT1_FLOAT));
                case LLOAD -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.GET, var, TypesContext.CAT2_LONG));
                case DLOAD -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.GET, var, TypesContext.CAT2_DOUBLE));

                // load reference var
                case ALOAD -> {
                    if (0 == var) {
                        currentNode_.addInstruction(new TypesInstruction(TypesOpcode.GET, var, classNameInternal_));
                    } else {
                        currentNode_.addInstruction(new TypesInstruction(TypesOpcode.GET, var, null));
                    }
                }
                // no stack change, but end of current block (no successor)
                case RET -> currentNode_ = null;
            }
        }
    }

    /**
     * Visits a method instruction. A method instruction is an instruction that
     * invokes a method.
     *
     * @param opcode the opcode of the type instruction to be visited. This opcode
     *               is either INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or
     *               INVOKEINTERFACE.
     * @param owner  the internal name of the method's owner class (see {@link
     *               Type#getInternalName getInternalName}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    public void visitMethodInsn(int opcode, String owner, String name, String desc, final boolean isInterface) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitMethodInsn         (" + OPCODES[opcode] + ", \"" + owner + "\", \"" + name + "\", \"" + desc + "\"," + isInterface + ")");

        if (currentNode_ != null) {
            var owner_classname = owner.replace('/', '.');

            if ((owner_classname.equals(config_.getContinuableSupportClassName()) || className_.equals(owner_classname)) &&
                ((config_.getPauseMethodName() != null && !config_.getPauseMethodName().isEmpty() && config_.getPauseMethodName().equals(name) && "()V".equals(desc)) ||
                    (config_.getStepBackMethodName() != null && !config_.getStepBackMethodName().isEmpty() && config_.getStepBackMethodName().equals(name) && "()V".equals(desc)) ||
                    (config_.getCallMethodName() != null && !config_.getCallMethodName().isEmpty() && config_.getCallMethodName().equals(name) && config_.getCallMethodDescriptor().equals(desc)))) {
                // pop the element instance reference from the stack
                currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));

                // remember the node in which the pause invocation is called
                currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PAUSE, ++pauseCount_));
            }
            // not the pause invocation
            else {
                handleMethodTypes(opcode, name, desc);
            }
        }
    }

    private void handleMethodTypes(int opcode, String name, String desc) {
        // pop off the argument types of the method
        var arguments = Type.getArgumentTypes(desc);
        for (var i = 0; i < arguments.length; i++) {
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
        }

        // pop the object reference from the stack if it'd not a static
        // method invocation
        if (INVOKESTATIC != opcode &&
            INVOKEDYNAMIC != opcode) {
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
        }

        // store the return type of the method
        if (!("<init>".equals(name) && INVOKESPECIAL == opcode)) {
            var type = Type.getReturnType(desc);
            switch (type.getSort()) {
                case Type.OBJECT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, type.getInternalName()));
                case Type.ARRAY -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, type.getDescriptor()));
                case Type.BOOLEAN -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_BOOLEAN));
                case Type.BYTE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_BYTE));
                case Type.CHAR -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_CHAR));
                case Type.FLOAT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_FLOAT));
                case Type.INT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_INT));
                case Type.SHORT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_SHORT));
                case Type.DOUBLE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_DOUBLE));
                case Type.LONG -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_LONG));
            }
        }
    }

    /**
     * Visits an invokedynamic instruction.
     *
     * @param name                     the method's name.
     * @param descriptor               the method's descriptor (see {@link Type}).
     * @param bootstrapMethodHandle    the bootstrap method.
     * @param bootstrapMethodArguments the bootstrap method constant arguments. Each argument must be
     *                                 an {@link Integer}, {@link Float}, {@link Long}, {@link Double}, {@link String}, {@link
     *                                 Type}, {@link Handle} or {@link ConstantDynamic} value. This method is allowed to modify
     *                                 the content of the array so a caller should expect that this array may change.
     */
    public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitInvokeDynamicInsn  (\"" + name + "\", \"" + descriptor + "\", " + bootstrapMethodHandle + ", " + Arrays.toString(bootstrapMethodArguments) + ")");

        handleMethodTypes(INVOKEDYNAMIC, name, descriptor);
    }

    /**
     * Visits a type instruction. A type instruction is an instruction that
     * takes a type descriptor as parameter.
     *
     * @param opcode the opcode of the type instruction to be visited. This opcode
     *               is either NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
     * @param desc   the operand of the instruction to be visited. This operand is
     *               must be a fully qualified class name in internal form, or the type
     *               descriptor of an array type (see {@link Type Type}).
     */
    public void visitTypeInsn(int opcode, String desc) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitTypeInsn           (" + OPCODES[opcode] + ", \"" + desc + "\")");

        if (currentNode_ != null) {
            switch (opcode) {
                case NEW -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, desc));
                case ANEWARRAY -> {
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    // multi dims new arrays without final dimension specification
                    // end up here as an array of arrays
                    if (desc.startsWith("[")) {
                        currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, "[" + desc));
                    } else {
                        currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, "[L" + desc + ";"));
                    }
                }
                case CHECKCAST, INSTANCEOF -> {
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, desc));
                }
            }
        }
    }

    /**
     * Visits a LDC instruction.
     *
     * @param cst the constant to be loaded on the stack. This parameter must be
     *            a non-null {@link java.lang.Integer Integer}, a {@link java.lang.Float
     *            Float}, a {@link java.lang.Long Long}, a {@link java.lang.Double
     *            Double} or a {@link String String}.
     */
    public void visitLdcInsn(Object cst) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitLdcInsn            (" + cst + ")");

        if (currentNode_ != null) {
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, Type.getInternalName(cst.getClass())));
        }
    }

    /**
     * Visits a MULTIANEWARRAY instruction.
     *
     * @param desc an array type descriptor (see {@link Type Type}).
     * @param dims number of dimensions of the array to allocate.
     */
    public void visitMultiANewArrayInsn(String desc, int dims) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitMultiANewArrayInsn (\"" + desc + "\", " + dims + ")");

        if (currentNode_ != null) {
            for (var i = 1; i <= dims; i++) {
                currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
            }
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, desc));
        }
    }

    /**
     * Visits a zero operand instruction.
     *
     * @param opcode the opcode of the instruction to be visited. This opcode is
     *               either NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2,
     *               ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1,
     *               FCONST_2, DCONST_0, DCONST_1,
     *               <p>
     *               IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD,
     *               IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE,
     *               SASTORE,
     *               <p>
     *               POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP,
     *               <p>
     *               IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL,
     *               DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG,
     *               FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR,
     *               LOR, IXOR, LXOR,
     *               <p>
     *               I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C,
     *               I2S,
     *               <p>
     *               LCMP, FCMPL, FCMPG, DCMPL, DCMPG,
     *               <p>
     *               IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
     *               <p>
     *               ARRAYLENGTH,
     *               <p>
     *               ATHROW,
     *               <p>
     *               MONITORENTER, or MONITOREXIT.
     */
    public void visitInsn(int opcode) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitInsn               (" + OPCODES[opcode] + ")");

        if (currentNode_ != null) {
            switch (opcode) {
                case RETURN:
                    currentNode_ = null;
                    break;
                case ATHROW:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    break;
                case ACONST_NULL:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.TYPE_NULL));
                    break;
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                case ICONST_M1:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_INT));
                    break;
                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_FLOAT));
                    break;
                case LCONST_0:
                case LCONST_1:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_LONG));
                    break;
                case DCONST_0:
                case DCONST_1:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_DOUBLE));
                    break;
                case AALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.AALOAD));
                    break;
                case IALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_INT));
                    break;
                case FALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_FLOAT));
                    break;
                case BALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_BYTE));
                    break;
                case CALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_CHAR));
                    break;
                case SALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_SHORT));
                    break;
                case LALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_LONG));
                    break;
                case DALOAD:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_DOUBLE));
                    break;
                case IASTORE:
                case LASTORE:
                case FASTORE:
                case DASTORE:
                case AASTORE:
                case BASTORE:
                case CASTORE:
                case SASTORE:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    break;
                case POP:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    break;
                case POP2:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP2));
                    break;
                case DUP:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.DUP));
                    break;
                case DUP_X1:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.DUPX1));
                    break;
                case DUP_X2:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.DUPX2));
                    break;
                case DUP2:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.DUP2));
                    break;
                case DUP2_X1:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.DUP2_X1));
                    break;
                case DUP2_X2:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.DUP2_X2));
                    break;
                case SWAP:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.SWAP));
                    break;
                case IADD:
                case LADD:
                case FADD:
                case DADD:
                case ISUB:
                case LSUB:
                case FSUB:
                case DSUB:
                case IMUL:
                case LMUL:
                case FMUL:
                case DMUL:
                case IDIV:
                case LDIV:
                case FDIV:
                case DDIV:
                case IREM:
                case LREM:
                case FREM:
                case DREM:
                case ISHL:
                case LSHL:
                case ISHR:
                case LSHR:
                case IUSHR:
                case LUSHR:
                case IAND:
                case LAND:
                case IOR:
                case LOR:
                case IXOR:
                case LXOR:
                    // just pop one type since the result is the same type as both operands
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    break;
                case INEG:
                case LNEG:
                case FNEG:
                case DNEG:
                    // do nothing, the type stack remains the same
                    break;
                case I2F:
                case I2B:
                case I2C:
                case I2S:
                case L2D:
                case F2I:
                case D2L:
                    // do nothing, the type stack remains the same
                    break;
                case I2L:
                case F2L:
                    // the type widens
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_LONG));
                    break;
                case I2D:
                case F2D:
                    // the type widens
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_DOUBLE));
                    break;
                case L2I:
                case D2I:
                    // the type shrinks
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_INT));
                    break;
                case L2F:
                case D2F:
                    // the type shrinks
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_FLOAT));
                    break;
                case LCMP:
                case FCMPL:
                case FCMPG:
                case DCMPL:
                case DCMPG:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_INT));
                    break;
                case ARRAYLENGTH:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_INT));
                    break;
                case MONITORENTER:
                case MONITOREXIT:
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    break;
            }
        }
    }

    /**
     * Visits an IINC instruction.
     *
     * @param var       index of the local variable to be incremented.
     * @param increment amount to increment the local variable by.
     */
    public void visitIincInsn(int var, int increment) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitIincInsn           (" + var + ", " + increment + ")");

        if (currentNode_ != null) {
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.IINC, var));
        }
    }

    /**
     * Visits a field instruction. A field instruction is an instruction that
     * loads or stores the value of a field of an object.
     *
     * @param opcode the opcode of the type instruction to be visited. This opcode
     *               is either GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
     * @param owner  the internal name of the field's owner class (see {@link
     *               Type#getInternalName getInternalName}).
     * @param name   the field's name.
     * @param desc   the field's descriptor (see {@link Type Type}).
     */
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitFieldInsn          (" + OPCODES[opcode] + ", \"" + owner + "\", \"" + name + "\", \"" + desc + "\")");

        if (currentNode_ != null) {
            switch (opcode) {
                case GETFIELD, GETSTATIC -> {
                    // pop the object reference from the stack if it'd not a static
                    // field access
                    if (GETSTATIC != opcode) {
                        currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    }

                    var type = Type.getType(desc);
                    switch (type.getSort()) {
                        case Type.OBJECT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, type.getInternalName()));
                        case Type.ARRAY -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, type.getDescriptor()));
                        case Type.BOOLEAN -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_BOOLEAN));
                        case Type.BYTE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_BYTE));
                        case Type.CHAR -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_CHAR));
                        case Type.FLOAT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_FLOAT));
                        case Type.INT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_INT));
                        case Type.SHORT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_SHORT));
                        case Type.DOUBLE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_DOUBLE));
                        case Type.LONG -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT2_LONG));
                    }
                }
                case PUTFIELD, PUTSTATIC -> {
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));

                    // pop the object reference from the stack if it'd not a static
                    // field access
                    if (PUTSTATIC != opcode) {
                        currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    }
                }
            }
        }
    }

    /**
     * Visits an instruction with a single int operand.
     *
     * @param opcode  the opcode of the instruction to be visited. This opcode is
     *                either BIPUSH, SIPUSH or NEWARRAY.
     * @param operand the operand of the instruction to be visited.
     */
    public void visitIntInsn(int opcode, int operand) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitIntInsn            (" + OPCODES[opcode] + ", " + operand + ")");

        if (currentNode_ != null) {
            switch (opcode) {
                case BIPUSH -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_BYTE));
                case SIPUSH -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_SHORT));
                case NEWARRAY -> {
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    switch (operand) {
                        case T_BOOLEAN -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_BOOLEAN));
                        case T_CHAR -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_CHAR));
                        case T_FLOAT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_FLOAT));
                        case T_DOUBLE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_DOUBLE));
                        case T_BYTE -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_BYTE));
                        case T_SHORT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_SHORT));
                        case T_INT -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_INT));
                        case T_LONG -> currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.ARRAY_LONG));
                    }
                }
            }
        }
    }

    /**
     * Visits a try catch block.
     *
     * @param start   beginning of the exception handler's scope (inclusive).
     * @param end     end of the exception handler's scope (exclusive).
     * @param handler beginning of the exception handler's code.
     * @param type    internal name of the type of exceptions handled by the handler,
     *                or <tt>null</tt> to catch any exceptions (for "finally" blocks).
     * @throws IllegalArgumentException if one of the labels has not already been
     *                                  visited by this visitor (by the {@link #visitLabel visitLabel}
     *                                  method).
     */
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitTryCatchBlock      (" + start + ", " + end + ", " + handler + ", \"" + type + "\")");

        var handlers = tryCatchHandlers_.computeIfAbsent(start, k -> new ArrayList<>());
        handlers.add(handler);
    }

    /**
     * Visits a LOOKUPSWITCH instruction.
     *
     * @param dflt   beginning of the default handler block.
     * @param keys   the values of the keys.
     * @param labels beginnings of the handler blocks. <tt>labels[i]</tt> is the
     *               beginning of the handler block for the <tt>keys[i]</tt> key.
     */
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitLookupSwitchInsn   (" + dflt + ", " + (null == keys ? null : join(keys, ",")) + ", " + (null == labels ? null : join(labels, ",")) + ")");

        if (currentNode_ != null) {
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));

            // add all the switch's successors
            currentNode_.addSuccessor(dflt);
            for (var label : labels) {
                currentNode_.addSuccessor(label);
            }

            // end the current node
            currentNode_ = null;
        }
    }

    /**
     * Visits a jump instruction. A jump instruction is an instruction that may
     * jump to another instruction.
     *
     * @param opcode the opcode of the type instruction to be visited. This opcode
     *               is either IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE,
     *               IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
     *               GOTO, JSR, IFNULL or IFNONNULL.
     * @param label  the operand of the instruction to be visited. This operand is
     *               a label that designates the instruction to which the jump instruction
     *               may jump.
     */
    public void visitJumpInsn(int opcode, Label label) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitJumpInsn           (" + OPCODES[opcode] + ", " + label + ")");

        if (currentNode_ != null) {
            switch (opcode) {
                case IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IFNULL, IFNONNULL -> {
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addSuccessor(label);
                }
                case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE -> {
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));
                    currentNode_.addSuccessor(label);
                }
                case JSR -> {
                    currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, TypesContext.CAT1_ADDRESS));
                    currentNode_.addSuccessor(label);
                }
                case GOTO -> {
                    currentNode_.addSuccessor(label);

                    // end the current node
                    currentNode_ = null;
                }
            }
        }
    }

    /**
     * Visits a label. A label designates the instruction that will be visited
     * just after it.
     *
     * @param label a {@link Label Label} object.
     */
    public void visitLabel(Label label) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitLabel              (" + label + ")");

        // begins a new current block
        var new_node = new TypesNode();
        labelMapping_.put(label, new_node);

        if (currentNode_ != null) {
            currentNode_.setFollowingNode(new_node);
        }
        currentNode_ = new_node;

        currentNode_.addInstruction(new TypesInstruction(TypesOpcode.LABEL, ++labelCount_));

        // if the label starts with an exception type, change the sort of
        // the created node and add the exception type as the first type
        // on the stack
        var exception_type = classVisitor_.getMetrics().nextExceptionType();
        if (exception_type != null) {
            currentNode_.setSort(TypesNode.EXCEPTION);
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.PUSH, exception_type));
        }
    }

    /**
     * Visits a TABLESWITCH instruction.
     *
     * @param min    the minimum key value.
     * @param max    the maximum key value.
     * @param dflt   beginning of the default handler block.
     * @param labels beginnings of the handler blocks. <tt>labels[i]</tt> is the
     *               beginning of the handler block for the <tt>min + i</tt> key.
     */
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitTableSwitchInsn    (" + min + ", " + max + ", " + dflt + ", " + (null == labels ? null : join(labels, ",")) + ")");

        if (currentNode_ != null) {
            currentNode_.addInstruction(new TypesInstruction(TypesOpcode.POP));

            // add all the switch's successors
            currentNode_.addSuccessor(dflt);
            for (var label : labels) {
                currentNode_.addSuccessor(label);
            }

            // end the current node
            currentNode_ = null;
        }
    }

    /**
     * Visits the maximum stack size and the maximum number of local variables of
     * the method.
     *
     * @param maxStack  maximum stack size of the method.
     * @param maxLocals maximum number of local variables for the method.
     */
    public void visitMaxs(int maxStack, int maxLocals) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("\nPROCESSING CONTROL FLOW");

        // process all try-catch instructions and map the start labels to their
        // handler successor
        for (var entry : tryCatchHandlers_.entrySet()) {
            var try_node = labelMapping_.get(entry.getKey());
            if (try_node != null) {
                for (var handler : entry.getValue()) {
                    try_node.addSuccessor(handler);
                }
            }
        }

        // start the control flow analysis
        pauseContexts_ = new TypesContext[pauseCount_ + 1];
        labelContexts_ = new TypesContext[labelCount_ + 1];

        TypesNode following_node = null;
        TypesNode successor_node = null;

        // control flow analysis algorithm
        var stack = rootNode_;
        while (stack != null) {
            // pops a block from the stack
            var node = stack;
            stack = stack.getNextToProcess();

            // process the node's instructions
            processInstructions(node);

            // analyzes the node's successors
            var successor = node.getSuccessors();
            while (successor != null) {
                successor_node = labelMapping_.get(successor.getLabel());

                if (!successor_node.isProcessed()) {
                    successor_node.setProcessed(true);
                    successor_node.setPredecessor(true, node);

                    // push the previous node on the stack
                    successor_node.setNextToProcess(stack);
                    stack = successor_node;
                }

                // iterate through the successors
                successor = successor.getNextSuccessor();
            }
            // handle a possible following node
            if (node.getFollowingNode() != null &&
                !node.getFollowingNode().isProcessed()) {
                following_node = node.getFollowingNode();

                following_node.setProcessed(true);
                following_node.setPredecessor(false, node);

                // push the previous node on the stack
                following_node.setNextToProcess(stack);
                stack = following_node;
            }
        }

        classVisitor_.setPauseContexts(pauseContexts_);
        classVisitor_.setLabelContexts(labelContexts_);
    }

    private void processInstructions(TypesNode node) {
        // set up the context for the node
        TypesContext context = null;
        // if it's the first node, create a new context
        if (null == node.getPredecessor()) {
            context = new TypesContext();
        }
        // otherwise retrieve the previous context
        else {
            var predecessor_context = node.getPredecessor().getContext();
            // always isolate the context for a successor
            if (node.getIsSuccessor()) {
                context = predecessor_context.clone();
            } else {
                context = new TypesContext(predecessor_context.getVars(), predecessor_context.getStackClone());
            }
        }
        node.setContext(context);

        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            context.setDebugIndent(repeat("  ", node.getLevel()));

        TypesContext exception_context = null;
        for (var instruction : node.getInstructions()) {
            switch (instruction.getOpcode()) {
                case TypesOpcode.SET: {
                    var type = context.peek();
                    if (instruction.getType() != null) {
                        type = instruction.getType();
                    }

                    // if the variables types in the scope change or if a new var is added,
                    // ensure that the context vars are isolated
                    var current_var_type = context.getVar(instruction.getArgument());
                    if (node.getPredecessor() != null &&
                        context.getVars() == node.getPredecessor().getContext().getVars() &&
                        (null == current_var_type ||
                            (!current_var_type.equals(TypesContext.TYPE_NULL) && !current_var_type.equals(type)))) {
                        context.cloneVars();
                    }

                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  SET  " + instruction.getArgument() + ", " + type);

                    context.pop();
                    context.setVar(instruction.getArgument(), type);
                    if (exception_context != null) {
                        exception_context.setVar(instruction.getArgument(), type);
                        exception_context = null;
                    }
                }
                break;
                case TypesOpcode.GET: {
                    var type = instruction.getType();
                    if (null == type) {
                        type = context.getVar(instruction.getArgument());
                    }

                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  GET  " + instruction.getArgument() + ", " + type);

                    context.push(type);
                }
                break;
                case TypesOpcode.IINC:
                    // do nothing
                    break;
                case TypesOpcode.POP: {
                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  POP " + context.peek());

                    context.pop();
                }
                break;
                case TypesOpcode.POP2: {
                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  POP2 " + context.peek());

                    var type = context.pop();
                    if (!type.startsWith("2")) {
                        context.pop();
                    }
                }
                break;
                case TypesOpcode.PUSH:
                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  PUSH " + instruction.getType());
                    context.push(instruction.getType());
                    break;
                case TypesOpcode.AALOAD: {
                    context.pop();
                    var array_desc = context.pop();
                    String element_desc = null;
                    if (array_desc.startsWith("[[")) {
                        element_desc = array_desc.substring(1);
                    } else {
                        var array_type = Type.getType(array_desc);
                        element_desc = array_type.getElementType().getInternalName();
                    }

                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  AALOAD " + element_desc);

                    context.push(element_desc);
                }
                break;
                case TypesOpcode.DUP:
                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP " + context.peek());
                    context.push(context.peek());
                    break;
                case TypesOpcode.DUPX1: {
                    var type1 = context.pop();
                    var type2 = context.pop();

                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUPX1 " + type1);

                    context.push(type1);
                    context.push(type2);
                    context.push(type1);
                }
                break;
                case TypesOpcode.DUPX2: {
                    var type1 = context.pop();
                    var type2 = context.pop();

                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUPX2 " + type1);

                    if (type2.startsWith("2")) {
                        context.push(type1);
                        context.push(type2);
                        context.push(type1);
                    } else {
                        var type3 = context.pop();
                        if (type3 != null) {
                            context.push(type1);
                            context.push(type3);
                            context.push(type2);
                            context.push(type1);
                        }
                    }
                }
                break;
                case TypesOpcode.DUP2: {
                    var type1 = context.pop();
                    if (type1.startsWith("2")) {
                        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                            ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2 " + type1);

                        context.push(type1);
                        context.push(type1);
                    } else {
                        var type2 = context.pop();

                        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                            ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2 " + type1 + ", " + type2);

                        context.push(type2);
                        context.push(type1);
                        context.push(type2);
                        context.push(type1);
                    }
                }
                break;
                case TypesOpcode.DUP2_X1: {
                    var type1 = context.pop();
                    var type2 = context.pop();
                    if (type1.startsWith("2")) {
                        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                            ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2_X1 " + type1);

                        context.push(type1);
                        context.push(type2);
                        context.push(type1);
                    } else {
                        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                            ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2_X1 " + type1 + ", " + type2);

                        var type3 = context.pop();
                        context.push(type2);
                        context.push(type1);
                        context.push(type3);
                        context.push(type2);
                        context.push(type1);
                    }
                }
                break;
                case TypesOpcode.DUP2_X2: {
                    var type1 = context.pop();
                    var type2 = context.pop();
                    if (type1.startsWith("2")) {
                        if (type2.startsWith("2"))    // form 4
                        {
                            if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                                ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2_X2 " + type1);

                            context.push(type1);
                            context.push(type2);
                            context.push(type1);
                        } else                        // form 2
                        {
                            if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                                ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2_X2 " + type1);

                            var type3 = context.pop();
                            context.push(type1);
                            context.push(type3);
                            context.push(type2);
                            context.push(type1);
                        }
                    } else if (!type2.startsWith("2")) {
                        var type3 = context.pop();
                        if (type3.startsWith("2"))    // form 3
                        {
                            if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                                ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2_X2 " + type1 + ", " + type2);

                            context.push(type2);
                            context.push(type1);
                            context.push(type3);
                            context.push(type2);
                            context.push(type1);
                        } else                        // form 1
                        {
                            if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                                ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  DUP2_X2 " + type1 + ", " + type2);

                            var type4 = context.pop();
                            context.push(type2);
                            context.push(type1);
                            context.push(type4);
                            context.push(type3);
                            context.push(type2);
                            context.push(type1);
                        }
                    }
                }
                break;
                case TypesOpcode.SWAP: {
                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "  SWAP");

                    var type1 = context.pop();
                    var type2 = context.pop();
                    context.push(type1);
                    context.push(type2);
                }
                break;
                case TypesOpcode.PAUSE:
                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "PAUSE " + instruction.getArgument());
                    pauseContexts_[instruction.getArgument()] = context.clone(node);
                    break;
                case TypesOpcode.LABEL: {
                    if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
                        ContinuationDebug.LOGGER.finest(repeat("  ", node.getLevel()) + "LABEL " + instruction.getArgument());

                    var label_context = context.clone(node);
                    labelContexts_[instruction.getArgument()] = label_context;
                    if (TypesNode.EXCEPTION == node.getSort()) {
                        exception_context = label_context;
                    }
                }
                break;
            }
        }
    }

    /**
     * Visits a line number declaration.
     *
     * @param line  a line number. This number refers to the source file
     *              from which the class was compiled.
     * @param start the first instruction corresponding to this line number.
     * @throws IllegalArgumentException if <tt>start</tt> has not already been
     *                                  visited by this visitor (by the {@link #visitLabel visitLabel}
     *                                  method).
     */
    public void visitLineNumber(int line, Label start) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitLineNumber         (" + line + ", " + start + ")");
    }

    /**
     * Visits a local variable declaration.
     *
     * @param name      the name of a local variable.
     * @param desc      the type descriptor of this local variable.
     * @param signature the type signature of this local variable. May be
     *                  <tt>null</tt> if the local variable type does not use generic types.
     * @param start     the first instruction corresponding to the scope of this
     *                  local variable (inclusive).
     * @param end       the last instruction corresponding to the scope of this
     *                  local variable (exclusive).
     * @param index     the local variable's index.
     * @throws IllegalArgumentException if one of the labels has not already been
     *                                  visited by this visitor (by the {@link #visitLabel visitLabel}
     *                                  method).
     */
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitLocalVariable      (\"" + name + "\", \"" + desc + "\", \"" + signature + "\", " + start + ", " + end + ", " + index + ")");
    }

    /**
     * Visits a non-standard attribute of the code. This method must visit only
     * the first attribute in the given attribute list.
     *
     * @param attr a non-standard code attribute. Must not be <tt>null</tt>.
     */
    public void visitAttribute(Attribute attr) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitAttribute          (" + attr + ")");
    }

    public void visitCode() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitCode               ()");
    }

    /**
     * Visits the default value of this annotation interface method.
     *
     * @return a visitor to the visit the actual default value of this annotation
     * interface method. The 'name' parameters passed to the methods of this
     * annotation visitor are ignored. Moreover, exacly one visit method
     * must be called on this annotation visitor, followed by visitEnd.
     */
    public AnnotationVisitor visitAnnotationDefault() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitAnnotationDefault  ()");

        return annotationVisitor_;
    }

    /**
     * Visits an annotation of this method.
     *
     * @param desc    the class descriptor of the annotation class.
     * @param visible <tt>true</tt> if the annotation is visible at runtime.
     * @return a visitor to visit the annotation values.
     */
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitAnnotation         (\"" + desc + "\", " + visible + ")");

        return annotationVisitor_;
    }

    /**
     * Visits an annotation of a parameter this method.
     *
     * @param parameter the parameter index.
     * @param desc      the class descriptor of the annotation class.
     * @param visible   <tt>true</tt> if the annotation is visible at runtime.
     * @return a visitor to visit the annotation values.
     */
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitParameterAnnotation(" + parameter + ", \"" + desc + "\", " + visible + ")");

        return annotationVisitor_;
    }

    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitFrame              (" + type + ", " + nLocal + ", " + Arrays.toString(local) + ", " + nStack + ", " + Arrays.toString(stack) + ")");
    }

    /**
     * Visits the end of the method. This method, which is the last one to be
     * called, is used to inform the visitor that all the annotations and
     * attributes of the method have been visited.
     */
    public void visitEnd() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Type:visitEnd                ()");
    }
}

