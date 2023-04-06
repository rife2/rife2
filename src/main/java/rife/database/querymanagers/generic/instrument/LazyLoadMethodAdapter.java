/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.instrument;

import static rife.database.querymanagers.generic.instrument.LazyLoadAccessorsBytecodeTransformer.GQM_VAR_NAME;
import static rife.database.querymanagers.generic.instrument.LazyLoadAccessorsBytecodeTransformer.LAZY_LOADED_VAR_NAME;

import java.util.Map;

import rife.asm.*;

class LazyLoadMethodAdapter extends MethodVisitor implements Opcodes {
    private final String className_;
    private final String methodDescriptor_;
    private final String propertyName_;
    private final boolean isGetter_;
    private final boolean isSetter_;

    private static final int GET_SET_ACCESSOR_PREFIX_LENGTH = 3;

    LazyLoadMethodAdapter(String className, String methodName, String methodDescriptor, MethodVisitor visitor) {
        super(ASM9, visitor);

        className_ = className;
        methodDescriptor_ = methodDescriptor;
        propertyName_ = uncapitalize(methodName.substring(GET_SET_ACCESSOR_PREFIX_LENGTH));

        isGetter_ = methodName.startsWith("get");
        isSetter_ = methodName.startsWith("set");
    }

    private static String uncapitalize(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }

        if (source.length() > 1 &&
            Character.isLowerCase(source.charAt(0))) {
            return source;
        }

        var chars = source.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public void visitInsn(int opcode) {
        if (ARETURN == opcode &&
            isGetter_) {
            var label_begin = new Label();
            var label_retrieve = new Label();
            var label_return = new Label();
            var label_end = new Label();

            var type_object = Type.getType(Object.class);

            mv.visitLabel(label_begin);

            // don't fetch a value if the current returned value is not null
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNONNULL, label_end);

            // don't fetch a value if there's no generic query manager stored in the bean
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className_, GQM_VAR_NAME, "Lrife/database/querymanagers/generic/GenericQueryManager;");
            mv.visitJumpInsn(IFNULL, label_end);

            // don't fetch a value if there's already one stored in the map
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className_, LAZY_LOADED_VAR_NAME, Type.getDescriptor(Map.class));
            mv.visitLdcInsn(propertyName_);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Map.class), "containsKey", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, type_object), true);
            mv.visitJumpInsn(IFNE, label_retrieve);

            // load the value from the database
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className_, GQM_VAR_NAME, "Lrife/database/querymanagers/generic/GenericQueryManager;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitLdcInsn(propertyName_);
            mv.visitLdcInsn(Type.getReturnType(methodDescriptor_).getClassName());
            mv.visitMethodInsn(INVOKESTATIC, "rife/database/querymanagers/generic/GenericQueryManagerRelationalUtils", "restoreLazyManyToOneProperty", "(Lrife/database/querymanagers/generic/GenericQueryManager;Lrife/validation/Constrained;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", false);
            mv.visitInsn(DUP);

            // store the lazily loaded value in the hash map
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className_, LAZY_LOADED_VAR_NAME, Type.getDescriptor(Map.class));
            mv.visitInsn(SWAP);
            mv.visitLdcInsn(propertyName_);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Map.class), "put", Type.getMethodDescriptor(type_object, type_object, type_object), true);
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, label_return);

            // fetch the existing value from the map
            mv.visitLabel(label_retrieve);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className_, LAZY_LOADED_VAR_NAME, Type.getDescriptor(Map.class));
            mv.visitLdcInsn(propertyName_);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Map.class), "get", Type.getMethodDescriptor(type_object, type_object), true);

            // return the value
            mv.visitLabel(label_return);
            mv.visitTypeInsn(CHECKCAST, Type.getReturnType(methodDescriptor_).getInternalName());
            mv.visitInsn(ARETURN);

            mv.visitLabel(label_end);
        }

        super.visitInsn(opcode);
    }

    public void visitCode() {
        if (isSetter_) {
            var type_object = Type.getType(Object.class);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className_, LAZY_LOADED_VAR_NAME, Type.getDescriptor(Map.class));
            var l1 = new Label();
            mv.visitJumpInsn(IFNULL, l1);
            var l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(55, l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className_, LAZY_LOADED_VAR_NAME, Type.getDescriptor(Map.class));
            mv.visitLdcInsn(propertyName_);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Map.class), "remove", Type.getMethodDescriptor(type_object, type_object), true);
            mv.visitInsn(POP);
            mv.visitLabel(l1);
        }
        super.visitCode();
    }
}
