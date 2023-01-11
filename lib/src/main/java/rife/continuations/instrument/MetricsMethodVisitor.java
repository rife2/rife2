/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.*;

import rife.continuations.ContinuationConfigInstrument;

import java.util.*;

class MetricsMethodVisitor extends MethodVisitor implements Opcodes {
    private final ContinuationConfigInstrument config_;
    private final MetricsClassVisitor classVisitor_;
    private final String className_;
    private final List<Label> labelsOrder_ = new ArrayList<>();
    private final Map<Label, String> exceptionTypes_ = new HashMap<>();

    private int pauseCount_ = 0;
    private int answerCount_ = 0;

    MetricsMethodVisitor(ContinuationConfigInstrument config, MetricsClassVisitor classVisitor, String className) {
        super(ASM9);

        config_ = config;
        classVisitor_ = classVisitor;
        className_ = className;
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        // go over all the labels in their order of appearance and check if
        // they are exception labels with thus an initial exception
        // type
        var exception_labels_types = new ArrayList<String>(labelsOrder_.size());
        for (var label : labelsOrder_) {
            exception_labels_types.add(exceptionTypes_.get(label));
        }

        // store all the metrics in the class visitor
        classVisitor_.setMaxLocals(maxLocals);
        classVisitor_.setPauseCount(pauseCount_);
        classVisitor_.setAnswerCount(answerCount_);
        classVisitor_.setExceptionTypes(exception_labels_types);
    }

    public void visitMethodInsn(int opcode, String owner, String methodName, String desc, final boolean isInterface) {
        var owner_classname = owner.replace('/', '.');

        if ((owner_classname.equals(config_.getContinuableSupportClassName()) || className_.equals(owner_classname)) &&
            ((config_.getPauseMethodName() != null && !config_.getPauseMethodName().isEmpty() && config_.getPauseMethodName().equals(methodName) && "()V".equals(desc)) ||
             (config_.getStepBackMethodName() != null && !config_.getStepBackMethodName().isEmpty() && config_.getStepBackMethodName().equals(methodName) && "()V".equals(desc)) ||
             (config_.getCallMethodName() != null && !config_.getCallMethodName().isEmpty() && config_.getCallMethodName().equals(methodName) && config_.getCallMethodDescriptor().equals(desc)))) {
            pauseCount_++;
        } else if (config_.getAnswerMethodName() != null && !config_.getAnswerMethodName().isEmpty() && config_.getAnswerMethodName().equals(methodName) && ("()V".equals(desc) || "(Ljava/lang/Object;)V".equals(desc))) {
            answerCount_++;
        }
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // store the types of the exception labels so that the exception
        // instance can be cast to it when restoring the local
        // variable stack
        if (null == type) {
            type = "java/lang/Throwable";
        }

        exceptionTypes_.put(handler, type);
    }

    public void visitLabel(Label label) {
        // remember the order of the labels
        labelsOrder_.add(label);
    }
}

