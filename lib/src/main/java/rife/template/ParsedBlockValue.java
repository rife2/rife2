/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ParsedBlockValue extends ParsedBlockPart {
    private String valueKey_ = null;
    private String valueTag_ = null;

    ParsedBlockValue(String valueKey, String valueTag) {
        assert valueKey != null;
        assert valueKey.length() > 0;
        assert valueTag != null;
        assert valueTag.length() > 0;

        valueKey_ = valueKey;
        valueTag_ = valueTag;
    }

    String getData() {
        return valueKey_;
    }

    String getTag() {
        return valueTag_;
    }

    int getType() {
        return VALUE;
    }

    void visitByteCodeExternalForm(MethodVisitor visitor, String className, String staticIdentifier) {
        visitor.visitVarInsn    (ALOAD, 0);
        visitor.visitFieldInsn  (GETSTATIC, className, staticIdentifier, "Ljava/lang/String;");
        visitor.visitFieldInsn  (GETSTATIC, className, staticIdentifier + "Tag", "Ljava/lang/String;");
        visitor.visitVarInsn    (ALOAD, 2);
        visitor.visitMethodInsn (INVOKEVIRTUAL, className, "appendValueExternalForm", "(Ljava/lang/String;Ljava/lang/String;Lrife/template/ExternalValue;)V", false);
    }

    void visitByteCodeInternalForm(MethodVisitor visitor, String className, String staticIdentifier) {
        visitor.visitVarInsn    (ALOAD, 0);
        visitor.visitFieldInsn  (GETSTATIC, className, staticIdentifier, "Ljava/lang/String;");
        visitor.visitFieldInsn  (GETSTATIC, className, staticIdentifier + "Tag", "Ljava/lang/String;");
        visitor.visitVarInsn    (ALOAD, 2);
        visitor.visitMethodInsn (INVOKEVIRTUAL, className, "appendValueInternalForm", "(Ljava/lang/String;Ljava/lang/String;Lrife/template/InternalValue;)V", false);
    }

    void visitByteCodeStaticDeclaration(ClassVisitor visitor, String staticIdentifier) {
        visitor.visitField      (ACC_PRIVATE | ACC_STATIC, staticIdentifier, "Ljava/lang/String;", null, null);
        visitor.visitField      (ACC_PRIVATE | ACC_STATIC, staticIdentifier + "Tag", "Ljava/lang/String;", null, null);
    }

    void visitByteCodeStaticDefinition(MethodVisitor visitor, String className, String staticIdentifier) {
        visitor.visitLdcInsn    (valueKey_);
        visitor.visitFieldInsn  (PUTSTATIC, className, staticIdentifier, "Ljava/lang/String;");
        visitor.visitLdcInsn    (valueTag_);
        visitor.visitFieldInsn  (PUTSTATIC, className, staticIdentifier + "Tag", "Ljava/lang/String;");
    }
}
