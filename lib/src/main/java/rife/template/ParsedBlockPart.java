/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.asm.ClassVisitor;
import rife.asm.MethodVisitor;
import rife.asm.Opcodes;

abstract class ParsedBlockPart implements Opcodes {
    public enum Type {
        TEXT, VALUE
    }

    abstract String getData();

    abstract Type getType();

    abstract void visitByteCodeExternalForm(MethodVisitor visitor, String className, String staticIdentifier);

    abstract void visitByteCodeInternalForm(MethodVisitor visitor, String className, String staticIdentifier);

    abstract void visitByteCodeStaticDeclaration(ClassVisitor visitor, String staticIdentifier);

    abstract void visitByteCodeStaticDefinition(MethodVisitor visitor, String className, String staticIdentifier);
}
