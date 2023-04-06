/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import java.io.Serializable;
import java.util.*;

import rife.asm.*;
import rife.validation.MetaDataBeanAware;
import rife.validation.MetaDataMerged;

class MetaDataClassAdapter extends ClassVisitor implements Opcodes {
    static final String DELEGATE_VAR_NAME = "$Rife$Meta$Data$Delegate$";

    private Map<String, List<String>> existingMethods_ = null;

    private Class metaData_ = null;
    private String metaDataInternalName_ = null;
    private String baseInternalName_ = null;

    MetaDataClassAdapter(Map<String, List<String>> existingMethods, Class metaData, ClassVisitor writer) {
        super(ASM9, writer);

        existingMethods_ = existingMethods;
        metaData_ = metaData;
        metaDataInternalName_ = Type.getInternalName(metaData_);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (baseInternalName_ != null) {
            if (name.equals("<init>")) {
                return new MetaDataDefaultConstructorAdapter(baseInternalName_, metaDataInternalName_, super.visitMethod(access, name, desc, signature, exceptions));
            } else if (name.equals("clone")) {
                return new MetaDataCloneableAdapter(baseInternalName_, metaDataInternalName_, super.visitMethod(access, name, desc, signature, exceptions));
            }
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // go over all the interfaces of the metadata class and its parents
        List<Class> meta_interfaces = new ArrayList<>();
        var meta_classes = new Stack<Class>();
        meta_classes.push(metaData_);
        while (meta_classes.size() > 0) {
            var current = meta_classes.pop();
            if (current == Object.class) {
                continue;
            }

            var super_class = current.getSuperclass();
            if (super_class != null) {
                meta_classes.push(super_class);
            }

            var current_interfaces = current.getInterfaces();
            for (var i : current_interfaces) {
                if (i == Cloneable.class ||
                    i == Serializable.class ||
                    i == MetaDataMerged.class ||
                    i == MetaDataBeanAware.class) {
                    continue;
                }

                if (!meta_interfaces.contains(i)) {
                    meta_interfaces.add(i);
                }

                meta_classes.push(i);
            }
        }

        // only instrument this class when the metadata class actually implements interfaces
        if (meta_interfaces.size() > 0) {
            baseInternalName_ = name;

            // add a member variable that will be used to delegate the interface method calls to
            cv.visitField(ACC_PRIVATE | ACC_SYNTHETIC | ACC_TRANSIENT, DELEGATE_VAR_NAME, Type.getDescriptor(metaData_), null, null);

            // obtain the already existing interfaces
            List<String> interfaces_merged = new ArrayList<>(Arrays.asList(interfaces));

            // process all interfaces and add those that are not yet implemented
            // to the base class all the interface methods will be delegated to the
            // member variable delegate
            String internal_name;
            for (var interface_class : meta_interfaces) {
                internal_name = Type.getInternalName(interface_class);
                if (!interfaces_merged.contains(internal_name)) {
                    // implement and delegate all the methods of the interface
                    List<String> descriptors;
                    for (var method : interface_class.getDeclaredMethods()) {
                        // check if the class already has an implementation for this method
                        // and if it's the case, don't implement it automatically
                        descriptors = existingMethods_.get(method.getName());
                        if (descriptors != null &&
                            descriptors.contains(Type.getMethodDescriptor(method))) {
                            continue;
                        }

                        // convert the exceptions into internal types
                        String[] exceptions_types = null;
                        if (method.getExceptionTypes().length > 0) {
                            List<String> exceptions_lists = new ArrayList<>(method.getExceptionTypes().length);
                            for (var exception : method.getExceptionTypes()) {
                                exceptions_lists.add(Type.getInternalName(exception));
                            }

                            exceptions_types = new String[exceptions_lists.size()];
                            exceptions_lists.toArray(exceptions_types);
                        }

                        // implement the interface method to delegate the call to the synthetic member variable
                        var method_descriptor = Type.getMethodDescriptor(method);
                        var method_param_count = method.getParameterTypes().length;

                        var mv = cv.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, method.getName(), method_descriptor, null, exceptions_types);
                        mv.visitCode();
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, baseInternalName_, DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");

                        // handle the method parameters correctly
                        var param_count = 1;
                        for (var param : method.getParameterTypes()) {
                            switch (Type.getType(param).getSort()) {
                                case Type.INT, Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT -> mv.visitVarInsn(ILOAD, param_count);
                                case Type.LONG -> mv.visitVarInsn(LLOAD, param_count);
                                case Type.FLOAT -> mv.visitVarInsn(FLOAD, param_count);
                                case Type.DOUBLE -> mv.visitVarInsn(DLOAD, param_count);
                                default -> mv.visitVarInsn(ALOAD, param_count);
                            }
                            param_count++;
                        }

                        mv.visitMethodInsn(INVOKEVIRTUAL, metaDataInternalName_, method.getName(), method_descriptor, false);
                        // handle the return type correctly
                        switch (Type.getReturnType(method).getSort()) {
                            case Type.VOID -> mv.visitInsn(RETURN);
                            case Type.INT, Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT -> mv.visitInsn(IRETURN);
                            case Type.LONG -> mv.visitInsn(LRETURN);
                            case Type.FLOAT -> mv.visitInsn(FRETURN);
                            case Type.DOUBLE -> mv.visitInsn(DRETURN);
                            default -> mv.visitInsn(ARETURN);
                        }
                        mv.visitMaxs(method_param_count + 1, method_param_count + 2);
                        mv.visitEnd();
                    }

                    interfaces_merged.add(internal_name);
                }
            }

            // handle clonability correctly when the metadata class is tied to one
            // particular instance of the base class
            if (MetaDataBeanAware.class.isAssignableFrom(metaData_)) {
                // implement the Cloneable interface in case this hasn't been done yet
                var cloneable_internal = Type.getInternalName(Cloneable.class);
                if (!interfaces_merged.contains(cloneable_internal)) {
                    interfaces_merged.add(cloneable_internal);
                }

                // check if the clone method has to be added from scratch
                var clone_method_descriptors = existingMethods_.get("clone");
                if (null == clone_method_descriptors ||
                    (!clone_method_descriptors.contains("()Ljava/lang/Object;") &&
                     !clone_method_descriptors.contains("()L" + baseInternalName_ + ";"))) {
                    var mv = cv.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "clone", "()Ljava/lang/Object;", null, new String[]{"java/lang/CloneNotSupportedException"});
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "clone", "()Ljava/lang/Object;", false);
                    mv.visitTypeInsn(CHECKCAST, baseInternalName_);
                    mv.visitVarInsn(ASTORE, 1);

                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, baseInternalName_, DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");
                    mv.visitMethodInsn(INVOKEVIRTUAL, metaDataInternalName_, "clone", "()Ljava/lang/Object;", false);
                    mv.visitTypeInsn(CHECKCAST, metaDataInternalName_);
                    mv.visitFieldInsn(PUTFIELD, baseInternalName_, DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitFieldInsn(GETFIELD, baseInternalName_, DELEGATE_VAR_NAME, "L" + metaDataInternalName_ + ";");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKEVIRTUAL, metaDataInternalName_, "setMetaDataBean", "(Ljava/lang/Object;)V", false);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitInsn(ARETURN);
                    var l0 = new Label();
                    mv.visitLabel(l0);
                    mv.visitJumpInsn(GOTO, l0);
                    mv.visitMaxs(2, 3);
                    mv.visitEnd();
                }
            }

            // use the new collection of interfaces for the class
            interfaces = new String[interfaces_merged.size()];
            interfaces_merged.toArray(interfaces);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }
}
