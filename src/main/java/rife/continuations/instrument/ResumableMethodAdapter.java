/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.*;
import rife.continuations.ContinuationConfigInstrument;

import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Level;

import static rife.continuations.instrument.ContinuationDebug.*;

class ResumableMethodAdapter extends MethodVisitor implements Opcodes {
    private final ContinuationConfigInstrument config_;

    private final TypesClassVisitor types_;

    private final MethodVisitor methodVisitor_;
    private final String className_;
    private final boolean visit_;
    private final boolean adapt_;

    private int contextIndex_ = -1;
    private int callTargetIndex_ = -1;
    private int answerIndex_ = -1;
    private int tempIndex_ = -1;

    private Label rethrowLabel_ = null;
    private boolean visitRethrowLabel_ = false;
    private Label[] labels_ = null;
    private int labelIndex_ = 0;

    private int maxLocalIndex_ = 0;

    private TypesContext labelContext_ = null;

    private final NoOpAnnotationVisitor annotationVisitor_ = new NoOpAnnotationVisitor();

    private void debugMessage(String message) {
        if (sTrace && ContinuationDebug.LOGGER.isLoggable(Level.FINEST)) {
            methodVisitor_.visitFieldInsn(GETSTATIC, "rife/continuations/instrument/ContinuationDebug", "LOGGER", "Ljava/util/logging/Logger;");
            methodVisitor_.visitLdcInsn(message);
            methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "java/util/logging/Logger", "finest", "(Ljava/lang/String;)V", false);
        }
    }

    ResumableMethodAdapter(ContinuationConfigInstrument config, TypesClassVisitor types, MethodVisitor methodVisitor, String className, boolean adapt, int maxLocals, int pauseCount) {
        super(ASM9);

        config_ = config;
        types_ = types;
        methodVisitor_ = methodVisitor;
        className_ = className;
        visit_ = (methodVisitor_ != null);
        adapt_ = adapt;
        contextIndex_ = maxLocals;
        callTargetIndex_ = contextIndex_ + 1;
        answerIndex_ = callTargetIndex_ + 1;
        tempIndex_ = answerIndex_ + 1;

        if (adapt_) {
            // create all the labels beforehand
            Label default_label = new Label();
            if (pauseCount > 0) {
                labels_ = new Label[pauseCount];
                for (var i = 0; i < pauseCount; i++) {
                    labels_[i] = new Label();
                }
            }

            debugMessage("CONT: context initializing");
            // get the current context for the current method and register it
            // after the last local variable
            methodVisitor_.visitVarInsn(ALOAD, 0);
            methodVisitor_.visitMethodInsn(INVOKESTATIC, "rife/continuations/ContinuationContext", "createOrResetContext", "(Ljava/lang/Object;)Lrife/continuations/ContinuationContext;", false);
            methodVisitor_.visitVarInsn(ASTORE, contextIndex_);
            debugMessage("CONT: context set up");

            if (pauseCount > 0) {
                debugMessage("CONT: context obtain label");
                // get a reference to the context object
                methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                // retrieve the current label index
                methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLabel", "()I", false);
                debugMessage("CONT: evaluate tableswitch");
                methodVisitor_.visitTableSwitchInsn(0, pauseCount - 1, default_label, labels_);
            }

            // set the default label to the start of the code
            methodVisitor_.visitLabel(default_label);
            debugMessage("CONT: begin of code");
        }
    }

    // store an integer on the stack
    private void addIntegerConst(int value) {
        switch (value) {
            case 0 -> methodVisitor_.visitInsn(ICONST_0);
            case 1 -> methodVisitor_.visitInsn(ICONST_1);
            case 2 -> methodVisitor_.visitInsn(ICONST_2);
            case 3 -> methodVisitor_.visitInsn(ICONST_3);
            case 4 -> methodVisitor_.visitInsn(ICONST_4);
            case 5 -> methodVisitor_.visitInsn(ICONST_5);
            default -> methodVisitor_.visitLdcInsn(value);
        }
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
            ContinuationDebug.LOGGER.finest(" Code:visitVarInsn            (" + OPCODES[opcode] + ", " + var + ")");

        if (adapt_) {
            // execute the original opcode
            methodVisitor_.visitVarInsn(opcode, var);

            // if this is an exception block, check if the caught exception is a
            // pause exception, if it is, just throw it further again.
            if (labelContext_ != null &&
                TypesNode.EXCEPTION == labelContext_.getSort() &&
                ASTORE == opcode) {
                if (null == rethrowLabel_) {
                    rethrowLabel_ = new Label();
                    visitRethrowLabel_ = true;
                }

                var label = new Label();
                methodVisitor_.visitVarInsn(ALOAD, var);
                methodVisitor_.visitTypeInsn(INSTANCEOF, "rife/tools/exceptions/ControlFlowRuntimeException");
                methodVisitor_.visitJumpInsn(IFEQ, label);
                methodVisitor_.visitVarInsn(ALOAD, var);
                methodVisitor_.visitJumpInsn(GOTO, rethrowLabel_);
                methodVisitor_.visitLabel(label);
            }

            // catch local variable store opcodes so that they can also be
            // stored in the context object
            if (opcode == ISTORE ||
                opcode == LSTORE ||
                opcode == FSTORE ||
                opcode == DSTORE ||
                opcode == ASTORE) {
                // retain the maximum index of the local var storage
                if (var > maxLocalIndex_) {
                    maxLocalIndex_ = var;
                }

                // prepare the arguments of the context storage method

                // get a reference to the context object
                methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                // get a reference to the local variable stack
                methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalVars", "()Lrife/continuations/ContinuationStack;", false);

                // push the index of local var that has to be stored on the
                // stack
                addIntegerConst(var);

                // detect the opcode and handle the different local variable
                // types correctly
                switch (opcode) {
                    // store ints
                    case ISTORE -> {
                        methodVisitor_.visitVarInsn(ILOAD, var);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "storeInt", "(II)V", false);
                    }
                    // store longs
                    case LSTORE -> {
                        methodVisitor_.visitVarInsn(LLOAD, var);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "storeLong", "(IJ)V", false);
                    }
                    // store floats
                    case FSTORE -> {
                        methodVisitor_.visitVarInsn(FLOAD, var);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "storeFloat", "(IF)V", false);
                    }
                    // store doubles
                    case DSTORE -> {
                        methodVisitor_.visitVarInsn(DLOAD, var);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "storeDouble", "(ID)V", false);
                    }
                    // store references
                    case ASTORE -> {
                        methodVisitor_.visitVarInsn(ALOAD, var);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "storeReference", "(ILjava/lang/Object;)V", false);
                    }
                }
            }

            // if this was the first ASTORE of an exception block, restore
            // the local types and change the type of the block so that this
            // isn't executed anymore
            if (labelContext_ != null &&
                TypesNode.EXCEPTION == labelContext_.getSort() &&
                ASTORE == opcode) {
                // restore the local variable stack
                restoreLocalStack(labelContext_);

                labelContext_.setSort(TypesNode.REGULAR);
            }
        } else if (visit_) {
            methodVisitor_.visitVarInsn(opcode, var);
        }
    }

    /**
     * Visits a method instruction. A method instruction is an instruction that
     * invokes a method.
     *
     * @param opcode      the opcode of the type instruction to be visited. This opcode
     *                    is either INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or
     *                    INVOKEINTERFACE.
     * @param owner       the internal name of the method's owner class (see {@link
     *                    Type#getInternalName getInternalName}).
     * @param name        the method's name.
     * @param desc        the method's descriptor (see {@link Type Type}).
     * @param isInterface if the method's owner class is an interface.
     */
    public void visitMethodInsn(int opcode, String owner, String name, String desc, final boolean isInterface) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitMethodInsn         (" + OPCODES[opcode] + ", \"" + owner + "\", \"" + name + "\", \"" + desc + "\")");

        if (adapt_) {
            var owner_classname = owner.replace('/', '.');

            if (owner_classname.equals(config_.getContinuableSupportClassName()) || className_.equals(owner_classname)) {
                if (config_.getPauseMethodName() != null && !config_.getPauseMethodName().isEmpty() &&
                    config_.getPauseMethodName().equals(name) && "()V".equals(desc)) {
                    debugMessage("CONT: pause : undoing method call");

                    // pop the ALOAD opcode off the stack
                    methodVisitor_.visitInsn(POP);

                    var context = types_.nextPauseContext();
                    var stack = context.getStackClone();
                    debugMessage("CONT: pause : saving operand stack");
                    saveOperandStack(stack);

                    debugMessage("CONT: pause : storing resume label");
                    // get a reference to the context object
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    // set the index of the current label
                    addIntegerConst(labelIndex_);
                    // set the new label index
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "setLabel", "(I)V", false);

                    // generate the pause exception
                    debugMessage("CONT: pause : throwing pause exception");
                    methodVisitor_.visitTypeInsn(NEW, "rife/continuations/exceptions/PauseException");
                    methodVisitor_.visitInsn(DUP);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKESPECIAL, "rife/continuations/exceptions/PauseException", "<init>", "(Lrife/continuations/ContinuationContext;)V", false);
                    methodVisitor_.visitInsn(ATHROW);

                    // add label for skipping over resumed code
                    methodVisitor_.visitLabel(labels_[labelIndex_]);
                    debugMessage("CONT: pause : resumed execution");

                    // get a reference to the context object
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    // clear the label
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "clearLabel", "()V", false);

                    // restore the local variable stack
                    debugMessage("CONT: pause : restoring local stack");
                    restoreLocalStack(context);

                    // restore the local operand stack
                    debugMessage("CONT: pause : restoring operand stack");
                    restoreOperandStack(stack);

                    labelIndex_++;

                    return;
                } else if (config_.getStepBackMethodName() != null && !config_.getStepBackMethodName().isEmpty() &&
                           config_.getStepBackMethodName().equals(name) && "()V".equals(desc)) {
                    debugMessage("CONT: stepBack : undoing method call");

                    // pop the ALOAD opcode off the stack
                    methodVisitor_.visitInsn(POP);

                    var context = types_.nextPauseContext();
                    var stack = context.getStackClone();
                    debugMessage("CONT: stepBack : saving operand stack");
                    saveOperandStack(stack);

                    // generate the stepBack exception
                    debugMessage("CONT: stepBack : throwing step-back exception");
                    methodVisitor_.visitTypeInsn(NEW, "rife/continuations/exceptions/StepBackException");
                    methodVisitor_.visitInsn(DUP);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKESPECIAL, "rife/continuations/exceptions/StepBackException", "<init>", "(Lrife/continuations/ContinuationContext;)V", false);
                    methodVisitor_.visitInsn(ATHROW);

                    // add label for skipping over resumed code
                    methodVisitor_.visitLabel(labels_[labelIndex_]);
                    debugMessage("CONT: stepBack : resumed execution");

                    // restore the local variable stack
                    debugMessage("CONT: stepBack : restoring local stack");
                    restoreLocalStack(context);

                    // restore the local operand stack
                    debugMessage("CONT: stepBack : restoring operand stack");
                    restoreOperandStack(stack);

                    labelIndex_++;

                    return;
                } else if (config_.getCallMethodName() != null && !config_.getCallMethodName().isEmpty() &&
                           config_.getCallMethodName().equals(name) && config_.getCallMethodDescriptor().equals(desc)) {
                    // store the call target
                    debugMessage("CONT: call : storing call target");
                    methodVisitor_.visitVarInsn(ASTORE, callTargetIndex_);

                    // pop the ALOAD opcode off the stack
                    debugMessage("CONT: call : undoing method call");
                    methodVisitor_.visitInsn(POP);

                    var context = types_.nextPauseContext();
                    var stack = context.getStackClone();
                    stack.pop();
                    debugMessage("CONT: call : saving operand stack");
                    saveOperandStack(stack);

                    debugMessage("CONT: call : storing resume label");
                    // get a reference to the context object
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    // set the index of the current label
                    addIntegerConst(labelIndex_);
                    // set the new label index
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "setLabel", "(I)V", false);

                    // generate the pause exception
                    debugMessage("CONT: call : throwing call exception");
                    methodVisitor_.visitTypeInsn(NEW, "rife/continuations/exceptions/CallException");
                    methodVisitor_.visitInsn(DUP);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitVarInsn(ALOAD, callTargetIndex_);
                    methodVisitor_.visitMethodInsn(INVOKESPECIAL, "rife/continuations/exceptions/CallException", "<init>", "(Lrife/continuations/ContinuationContext;Ljava/lang/Object;)V", false);
                    methodVisitor_.visitInsn(ATHROW);

                    // add label for skipping over resumed code
                    methodVisitor_.visitLabel(labels_[labelIndex_]);
                    debugMessage("CONT: call : resumed execution");

                    // get a reference to the context object
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    // clear the label
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "clearLabel", "()V", false);

                    // restore the local variable stack
                    debugMessage("CONT: call : restoring local stack");
                    restoreLocalStack(context);
                    // restore the local operand stack
                    debugMessage("CONT: call : restoring operand stack");
                    restoreOperandStack(stack);

                    debugMessage("CONT: call : retrieving call answer");
                    // get a reference to the context object
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    // get the call answer
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getCallAnswer", "()Ljava/lang/Object;", false);
                    methodVisitor_.visitTypeInsn(CHECKCAST, config_.getCallMethodReturnTypeName());

                    labelIndex_++;

                    return;
                } else if (config_.getAnswerMethodName() != null && !config_.getAnswerMethodName().isEmpty() &&
                           config_.getAnswerMethodName().equals(name) && ("()V".equals(desc) || "(Ljava/lang/Object;)V".equals(desc))) {
                    if ("()V".equals(desc)) {
                        methodVisitor_.visitInsn(ACONST_NULL);
                    }

                    // store the answer
                    debugMessage("CONT: call : storing answer");
                    methodVisitor_.visitVarInsn(ASTORE, answerIndex_);

                    // pop the ALOAD opcode off the stack
                    debugMessage("CONT: call : undoing method call");
                    methodVisitor_.visitInsn(POP);

                    // generate the answer exception
                    debugMessage("CONT: answer : throwing answer exception");
                    methodVisitor_.visitTypeInsn(NEW, "rife/continuations/exceptions/AnswerException");
                    methodVisitor_.visitInsn(DUP);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitVarInsn(ALOAD, answerIndex_);
                    methodVisitor_.visitMethodInsn(INVOKESPECIAL, "rife/continuations/exceptions/AnswerException", "<init>", "(Lrife/continuations/ContinuationContext;Ljava/lang/Object;)V", false);
                    methodVisitor_.visitInsn(ATHROW);

                    return;
                }
            }
        }

        if (visit_) {
            methodVisitor_.visitMethodInsn(opcode, owner, name, desc, isInterface);
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

        if (visit_) {
            methodVisitor_.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }
    }

    /**
     * Restore the local variable stack, first the computation
     * types of category 1 and afterward those of category 2
     */
    private void restoreLocalStack(TypesContext context) {
        for (var i = 1; i <= maxLocalIndex_; i++) {
            if (!context.hasVar(i)) {
                continue;
            }

            switch (context.getVarType(i)) {
                case Type.INT -> {
                    debugMessage("CONT: restore local : " + i + ", int");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalVars", "()Lrife/continuations/ContinuationStack;", false);
                    addIntegerConst(i);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "getInt", "(I)I", false);
                    methodVisitor_.visitVarInsn(ISTORE, i);
                }
                case Type.FLOAT -> {
                    debugMessage("CONT: restore local : " + i + ", float");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalVars", "()Lrife/continuations/ContinuationStack;", false);
                    addIntegerConst(i);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "getFloat", "(I)F", false);
                    methodVisitor_.visitVarInsn(FSTORE, i);
                }
                case Type.OBJECT -> {
                    debugMessage("CONT: restore local : " + i + ", " + context.getVar(i));
                    var type = context.getVar(i);
                    if (TypesContext.TYPE_NULL.equals(type)) {
                        methodVisitor_.visitInsn(ACONST_NULL);
                        methodVisitor_.visitVarInsn(ASTORE, i);
                    } else {
                        methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalVars", "()Lrife/continuations/ContinuationStack;", false);
                        addIntegerConst(i);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "getReference", "(I)Ljava/lang/Object;", false);
                        methodVisitor_.visitTypeInsn(CHECKCAST, type);
                        methodVisitor_.visitVarInsn(ASTORE, i);
                    }
                }
            }
        }
        for (var i = 1; i <= maxLocalIndex_; i++) {
            if (!context.hasVar(i)) {
                continue;
            }

            switch (context.getVarType(i)) {
                case Type.LONG -> {
                    debugMessage("CONT: restore local : " + i + ", long");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalVars", "()Lrife/continuations/ContinuationStack;", false);
                    addIntegerConst(i);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "getLong", "(I)J", false);
                    methodVisitor_.visitVarInsn(LSTORE, i);
                }
                case Type.DOUBLE -> {
                    debugMessage("CONT: restore local : " + i + ", double");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalVars", "()Lrife/continuations/ContinuationStack;", false);
                    addIntegerConst(i);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "getDouble", "(I)D", false);
                    methodVisitor_.visitVarInsn(DSTORE, i);
                }
            }
        }
    }

    /**
     * Save the operand stack
     */
    private void saveOperandStack(Stack<String> stack) {
        String type = null;

        // save all stack entries besides the last one pushed, it's the
        // element's object reference that is used for the stub continuation
        // methods
        for (int i = stack.size() - 1; i >= 0; i--) {
            type = stack.get(i);

            switch (type) {
                case TypesContext.CAT1_BOOLEAN, TypesContext.CAT1_CHAR, TypesContext.CAT1_BYTE, TypesContext.CAT1_SHORT, TypesContext.CAT1_INT -> {
                    methodVisitor_.visitVarInsn(ISTORE, tempIndex_);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitVarInsn(ILOAD, tempIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "pushInt", "(I)V", false);
                }
                case TypesContext.CAT1_FLOAT -> {
                    methodVisitor_.visitVarInsn(FSTORE, tempIndex_);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitVarInsn(FLOAD, tempIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "pushFloat", "(F)V", false);
                }
                case TypesContext.CAT2_DOUBLE -> {
                    methodVisitor_.visitVarInsn(DSTORE, tempIndex_);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitVarInsn(DLOAD, tempIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "pushDouble", "(D)V", false);
                }
                case TypesContext.CAT2_LONG -> {
                    methodVisitor_.visitVarInsn(LSTORE, tempIndex_);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitVarInsn(LLOAD, tempIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "pushLong", "(J)V", false);
                }
                case TypesContext.CAT1_ADDRESS ->
                    // this should never happen
                    throw new RuntimeException("Invalid local stack type");
                default -> {
                    methodVisitor_.visitVarInsn(ASTORE, tempIndex_);
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitVarInsn(ALOAD, tempIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "pushReference", "(Ljava/lang/Object;)V", false);
                }
            }
        }
    }

    /**
     * Restore the operand stack
     */
    private void restoreOperandStack(Stack<String> stack) {
        String type = null;

        // restore all stack entries besides the last one pushed, it's the
        // element's object reference that is used for the stub continuation
        // methods
        for (var i = 0; i < stack.size(); i++) {
            type = stack.get(i);

            switch (type) {
                case TypesContext.CAT1_BOOLEAN, TypesContext.CAT1_CHAR, TypesContext.CAT1_BYTE, TypesContext.CAT1_SHORT, TypesContext.CAT1_INT -> {
                    debugMessage("CONT: restore operand : " + i + ", int");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "popInt", "()I", false);
                }
                case TypesContext.CAT1_FLOAT -> {
                    debugMessage("CONT: restore operand : " + i + ", float");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "popFloat", "()F", false);
                }
                case TypesContext.CAT2_DOUBLE -> {
                    debugMessage("CONT: restore operand : " + i + ", double");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "popDouble", "()D", false);
                }
                case TypesContext.CAT2_LONG -> {
                    debugMessage("CONT: restore operand : " + i + ", long");
                    methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                    methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "popLong", "()J", false);
                }
                case TypesContext.CAT1_ADDRESS ->
                    // this should never happen
                    throw new RuntimeException("Invalid local stack type");
                default -> {
                    debugMessage("CONT: restore operand : " + i + ", " + type);
                    if (TypesContext.TYPE_NULL.equals(type)) {
                        methodVisitor_.visitInsn(ACONST_NULL);
                        methodVisitor_.visitVarInsn(ASTORE, i);
                    } else {
                        methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalStack", "()Lrife/continuations/ContinuationStack;", false);
                        methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "popReference", "()Ljava/lang/Object;", false);
                        methodVisitor_.visitTypeInsn(CHECKCAST, type);
                    }
                }
            }
        }
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
            ContinuationDebug.LOGGER.finest(" Code:visitTypeInsn           (" + OPCODES[opcode] + ", \"" + desc + "\")");

        if (visit_) {
            methodVisitor_.visitTypeInsn(opcode, desc);
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
            ContinuationDebug.LOGGER.finest(" Code:visitLdcInsn            (" + cst + ")");

        if (visit_) {
            methodVisitor_.visitLdcInsn(cst);
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
            ContinuationDebug.LOGGER.finest(" Code:visitMultiANewArrayInsn (\"" + desc + "\", " + dims + ")");

        if (visit_) {
            methodVisitor_.visitMultiANewArrayInsn(desc, dims);
        }
    }

    /**
     * Visits a zero operand instruction.
     *
     * @param opcode the opcode of the instruction to be visited. This opcode is
     *               either NOP, ACONST_NULL, ICONST_1, ICONST_0, ICONST_1, ICONST_2,
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
            ContinuationDebug.LOGGER.finest(" Code:visitInsn               (" + OPCODES[opcode] + ")");

        if (adapt_ &&
            RETURN == opcode) {
            debugMessage("CONT: context deactivation");

            // get a reference to the context object
            methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
            // remove the context from the manager
            methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "registerContext", "()V", false);

            // get a reference to the context object
            methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
            // remove the context from the manager
            methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "deactivate", "()V", false);

            methodVisitor_.visitInsn(opcode);

            if (rethrowLabel_ != null &&
                visitRethrowLabel_) {
                methodVisitor_.visitLabel(rethrowLabel_);
                debugMessage("CONT: rethrowing exception");
                methodVisitor_.visitInsn(ATHROW);
                visitRethrowLabel_ = false;
            }
        } else if (visit_) {
            methodVisitor_.visitInsn(opcode);
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
            ContinuationDebug.LOGGER.finest(" Code:visitIincInsn           (" + var + ", " + increment + ")");

        if (adapt_) {
            // execute the original opcode
            methodVisitor_.visitIincInsn(var, increment);

            // retain the maximum index of the local var storage
            if (var > maxLocalIndex_) {
                maxLocalIndex_ = var;
            }

            // prepare the arguments of the context storage method

            // get a reference to the context object
            methodVisitor_.visitVarInsn(ALOAD, contextIndex_);
            // get a reference to the local variable stack
            methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationContext", "getLocalVars", "()Lrife/continuations/ContinuationStack;", false);

            // push the index of local var that has to be stored on the
            // stack and put the increment amount on it also
            addIntegerConst(var);
            addIntegerConst(increment);
            methodVisitor_.visitMethodInsn(INVOKEVIRTUAL, "rife/continuations/ContinuationStack", "incrementInt", "(II)V", false);
        } else if (visit_) {
            methodVisitor_.visitIincInsn(var, increment);
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
            ContinuationDebug.LOGGER.finest(" Code:visitFieldInsn          (" + OPCODES[opcode] + ", \"" + owner + "\", \"" + name + "\", \"" + desc + "\")");

        if (visit_) {
            methodVisitor_.visitFieldInsn(opcode, owner, name, desc);
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
            ContinuationDebug.LOGGER.finest(" Code:visitIntInsn            (" + OPCODES[opcode] + ", " + operand + ")");

        if (visit_) {
            methodVisitor_.visitIntInsn(opcode, operand);
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

        if (visit_) {
            methodVisitor_.visitTryCatchBlock(start, end, handler, type);
        }
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
            ContinuationDebug.LOGGER.finest(" Code:visitLookupSwitchInsn   (" + dflt + ", " + (null == keys ? null : join(keys, ",")) + ", " + (null == labels ? null : join(labels, ",")) + ")");

        if (visit_) {
            methodVisitor_.visitLookupSwitchInsn(dflt, keys, labels);
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
            ContinuationDebug.LOGGER.finest(" Code:visitJumpInsn           (" + OPCODES[opcode] + ", " + label + ")");

        if (visit_) {
            methodVisitor_.visitJumpInsn(opcode, label);
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
            ContinuationDebug.LOGGER.finest(" Code:visitLabel              (" + label + ")");

        if (visit_) {
            methodVisitor_.visitLabel(label);
        }

        if (adapt_) {
            labelContext_ = types_.nextLabelTypes();
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

        if (visit_) {
            methodVisitor_.visitTableSwitchInsn(min, max, dflt, labels);
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
            ContinuationDebug.LOGGER.finest(" Code:visitMaxs               (" + maxStack + ", " + maxLocals + ")");

        if (visit_) {
            methodVisitor_.visitMaxs(maxStack, maxLocals);
        }
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
            ContinuationDebug.LOGGER.finest(" Code:visitLocalVariable      (\"" + name + "\", \"" + desc + ", \"" + signature + "\", " + start + ", " + end + ", " + index + ")");

        if (visit_) {
            methodVisitor_.visitLocalVariable(name, desc, signature, start, end, index);
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
            ContinuationDebug.LOGGER.finest(" Code:visitLineNumber         (" + line + ", " + start + ")");

        if (visit_) {
            methodVisitor_.visitLineNumber(line, start);
        }
    }

    /**
     * Visits a non-standard attribute of the code. This method must visit only
     * the first attribute in the given attribute list.
     *
     * @param attr a non-standard code attribute. Must not be <tt>null</tt>.
     */
    public void visitAttribute(Attribute attr) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitAttribute          (" + attr + ")");

        if (visit_) {
            methodVisitor_.visitAttribute(attr);
        }
    }

    public void visitCode() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitCode               ()");

        if (visit_) {
            methodVisitor_.visitCode();
        }
    }

    public AnnotationVisitor visitAnnotationDefault() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitAnnotationDefault  ()");

        if (visit_) {
            return methodVisitor_.visitAnnotationDefault();
        }

        return annotationVisitor_;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitAnnotation         (\"" + desc + "\", " + visible + ")");

        if (visit_) {
            return methodVisitor_.visitAnnotation(desc, visible);
        }

        return annotationVisitor_;
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitAnnotation         (" + parameter + ", \"" + desc + "\", " + visible + ")");

        if (visit_) {
            return methodVisitor_.visitParameterAnnotation(parameter, desc, visible);
        }

        return annotationVisitor_;
    }

    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitFrame              (" + type + ", " + nLocal + ", " + Arrays.toString(local) + ", " + nStack + ", " + Arrays.toString(stack) + ")");

        if (visit_) {
            methodVisitor_.visitFrame(type, nLocal, local, nStack, stack);
        }
    }

    public void visitEnd() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest(" Code:visitEnd                ()");

        if (visit_) {
            methodVisitor_.visitEnd();
        }
    }

}
