/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import rife.asm.MethodVisitor;
import rife.asm.Opcodes;

class MetaDataCloneableAdapter extends MethodVisitor implements Opcodes {
    private String baseInternalName_ = null;
    private String metaDataInternalName_ = null;

    MetaDataCloneableAdapter(String baseInternalName, String metaDataInternalName, MethodVisitor visitor) {
        super(ASM9, visitor);

        baseInternalName_ = baseInternalName;
        metaDataInternalName_ = metaDataInternalName;
    }

    public void visitInsn(int opcode) {
        if (ARETURN == opcode) {
            mv.visitTypeInsn(CHECKCAST, baseInternalName_);
            mv.visitVarInsn(ASTORE, 1);

            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");
            mv.visitMethodInsn(INVOKEVIRTUAL, metaDataInternalName_, "clone", "()Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, metaDataInternalName_);
            mv.visitFieldInsn(PUTFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");

            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(GETFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");

            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, metaDataInternalName_, "setMetaDataBean", "(Ljava/lang/Object;)V", false);

            mv.visitVarInsn(ALOAD, 1);
        }

        super.visitInsn(opcode);
    }
}
