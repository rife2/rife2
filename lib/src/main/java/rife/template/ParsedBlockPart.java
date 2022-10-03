/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

abstract class ParsedBlockPart implements Opcodes {
    public static final int TEXT = 0;
    public static final int VALUE = 1;

    abstract String getData();

    abstract int getType();

    abstract void visitByteCodeExternalForm(MethodVisitor visitor, String className, String staticIdentifier);

    abstract void visitByteCodeInternalForm(MethodVisitor visitor, String className, String staticIdentifier);

    abstract void visitByteCodeStaticDeclaration(ClassVisitor visitor, String staticIdentifier);

    abstract void visitByteCodeStaticDefinition(MethodVisitor visitor, String className, String staticIdentifier);
}
