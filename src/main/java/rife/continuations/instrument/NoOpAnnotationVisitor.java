/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.AnnotationVisitor;

import static rife.asm.Opcodes.ASM9;

class NoOpAnnotationVisitor extends AnnotationVisitor {
    protected NoOpAnnotationVisitor() {
        super(ASM9);
    }

    @Override
    public void visit(String name, Object value) {
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        return this;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return this;
    }

    @Override
    public void visitEnd() {
    }
}
