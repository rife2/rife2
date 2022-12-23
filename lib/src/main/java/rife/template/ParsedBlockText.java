/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.asm.ClassVisitor;
import rife.asm.MethodVisitor;

import static rife.template.ParsedBlockPart.Type.TEXT;

class ParsedBlockText extends ParsedBlockPart {
    private String text_ = null;

    ParsedBlockText(String text) {
        assert text != null;
        assert text.length() > 0;

        text_ = text;
    }

    void setData(String text) {
        text_ = text;
    }

    String getData() {
        return text_;
    }

    Type getType() {
        return TEXT;
    }

    void visitByteCodeExternalForm(MethodVisitor visitor, String className, String staticIdentifier) {
        visitor.visitVarInsn    (ALOAD, 2);
        visitor.visitFieldInsn  (GETSTATIC, className, staticIdentifier, "Lrife/template/InternalString;");
        visitor.visitMethodInsn (INVOKEVIRTUAL, "rife/template/ExternalValue", "append", "(Ljava/lang/CharSequence;)V", false);
    }

    void visitByteCodeInternalForm(MethodVisitor visitor, String className, String staticIdentifier) {
        visitor.visitVarInsn    (ALOAD, 0);
        visitor.visitVarInsn    (ALOAD, 2);
        visitor.visitFieldInsn  (GETSTATIC, className, staticIdentifier, "Lrife/template/InternalString;");
        visitor.visitMethodInsn (INVOKEVIRTUAL, className, "appendTextInternal", "(Lrife/template/InternalValue;Ljava/lang/CharSequence;)V", false);
    }

    void visitByteCodeStaticDeclaration(ClassVisitor visitor, String staticIdentifier) {
        visitor.visitField      (ACC_PRIVATE | ACC_STATIC, staticIdentifier, "Lrife/template/InternalString;", null, null);
    }

    void visitByteCodeStaticDefinition(MethodVisitor visitor, String className, String staticIdentifier) {
        visitor.visitTypeInsn   (NEW, "rife/template/InternalString");
        visitor.visitInsn       (DUP);
        visitor.visitLdcInsn    (text_);
        visitor.visitMethodInsn (INVOKESPECIAL, "rife/template/InternalString", "<init>", "(Ljava/lang/String;)V", false);
        visitor.visitFieldInsn  (PUTSTATIC, className, staticIdentifier, "Lrife/template/InternalString;");
    }
}
