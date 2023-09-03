/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.ClassVisitor;
import rife.asm.MethodVisitor;
import rife.continuations.ContinuationConfigInstrument;

import static rife.asm.Opcodes.ASM9;

class TypesClassVisitor extends ClassVisitor {
    private final ContinuationConfigInstrument config_;
    private final MetricsClassVisitor metrics_;
    private final String className_;
    private final String entryMethodDesc_;
    private final String entryMethodName_;

    private TypesContext[] pauseContexts_ = null;
    private TypesContext[] labelContexts_ = null;
    private int pauseContextCounter_ = 0;
    private int labelContextCounter_ = 0;

    TypesClassVisitor(ContinuationConfigInstrument config, MetricsClassVisitor metrics, String className) {
        super(ASM9);

        config_ = config;
        metrics_ = metrics;
        className_ = className;
        entryMethodName_ = config.getEntryMethodName();
        entryMethodDesc_ = config.getEntryMethodDescriptor();
    }

    MetricsClassVisitor getMetrics() {
        return metrics_;
    }

    void setPauseContexts(TypesContext[] pauseContexts) {
        pauseContexts_ = pauseContexts;
    }

    TypesContext nextPauseContext() {
        return pauseContexts_[pauseContextCounter_++];
    }

    void setLabelContexts(TypesContext[] labelContexts) {
        labelContexts_ = labelContexts;
    }

    TypesContext nextLabelTypes() {
        return labelContexts_[labelContextCounter_++];
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (entryMethodName_.equals(name) &&
            entryMethodDesc_.equals(desc)) {
            return new TypesMethodVisitor(config_, this, className_);
        }

        return null;
    }
}
