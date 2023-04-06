/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import rife.asm.*;
import rife.validation.MetaDataBeanAware;

class MetaDataDefaultConstructorAdapter extends MethodVisitor implements Opcodes {
    private String baseInternalName_ = null;
    private String metaDataInternalName_ = null;

    MetaDataDefaultConstructorAdapter(String baseInternalName, String metaDataInternalName, MethodVisitor visitor) {
        super(ASM9, visitor);

        baseInternalName_ = baseInternalName;
        metaDataInternalName_ = metaDataInternalName;
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, final boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, desc, isInterface);

        if (INVOKESPECIAL == opcode &&
            "<init>".equals(name) &&
            "()V".equals(desc)) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, metaDataInternalName_);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, metaDataInternalName_, "<init>", "()V", false);
            mv.visitFieldInsn(PUTFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");
            mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(MetaDataBeanAware.class));
            var not_aware = new Label();
            mv.visitJumpInsn(IFEQ, not_aware);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, baseInternalName_, MetaDataClassAdapter.DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MetaDataBeanAware.class));
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(MetaDataBeanAware.class), "setMetaDataBean", "(Ljava/lang/Object;)V", true);
            mv.visitLabel(not_aware);
        }
    }
}
