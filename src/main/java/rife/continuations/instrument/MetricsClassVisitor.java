/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.ClassVisitor;
import rife.asm.MethodVisitor;
import rife.continuations.ContinuationConfigInstrument;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static rife.asm.Opcodes.ASM9;

class MetricsClassVisitor extends ClassVisitor {
    private final ContinuationConfigInstrument config_;
    private final String className_;
    private final String entryMethodName_;
    private final String entryMethodDesc_;
    private int maxLocals_ = -1;
    private int pauseCount_ = -1;
    private int answerCount_ = -1;
    private List<String> exceptionTypes_ = null;

    MetricsClassVisitor(ContinuationConfigInstrument config, String className) {
        super(ASM9);

        config_ = config;
        className_ = className;
        entryMethodName_ = config.getEntryMethodName();
        entryMethodDesc_ = config.getEntryMethodDescriptor();
    }

    void setMaxLocals(int maxLocals) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("maxLocals = " + maxLocals);

        maxLocals_ = maxLocals;
    }

    int getMaxLocals() {
        return maxLocals_;
    }

    void setPauseCount(int pauseCount) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("pauseCount = " + pauseCount);

        pauseCount_ = pauseCount;
    }

    void setAnswerCount(int answerCount) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("answerCount = " + answerCount);

        answerCount_ = answerCount;
    }

    void setExceptionTypes(ArrayList<String> exceptionTypes) {
        exceptionTypes_ = exceptionTypes;
    }

    String nextExceptionType() {
        return exceptionTypes_.remove(0);
    }

    int getPauseCount() {
        return pauseCount_;
    }

    int getAnswerCount() {
        return answerCount_;
    }

    boolean makeResumable() {
        return pauseCount_ > 0 || answerCount_ > 0;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (entryMethodName_.equals(name) &&
            entryMethodDesc_.equals(desc)) {
            return new MetricsMethodVisitor(config_, this, className_);
        }

        return null;
    }
}
