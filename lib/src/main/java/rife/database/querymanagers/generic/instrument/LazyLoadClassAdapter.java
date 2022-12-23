/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.instrument;

import static rife.database.querymanagers.generic.instrument.LazyLoadAccessorsBytecodeTransformer.GQM_VAR_NAME;
import static rife.database.querymanagers.generic.instrument.LazyLoadAccessorsBytecodeTransformer.LAZY_LOADED_VAR_NAME;

import java.util.Map;

import rife.asm.*;

class LazyLoadClassAdapter extends ClassVisitor implements Opcodes {
    private String className_ = null;
    private final Map<String, String> lazyLoadingMethods_;

    LazyLoadClassAdapter(Map<String, String> lazyLoadingMethods, ClassVisitor writer) {
        super(ASM9, writer);

        lazyLoadingMethods_ = lazyLoadingMethods;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // retrieve the method from the previously detected lazy loading methods
        var stored_desc = lazyLoadingMethods_.get(name);

        // check if it's the same method by comparing the description
        if (stored_desc != null &&
            stored_desc.equals(desc)) {
            return new LazyLoadMethodAdapter(className_, name, desc, super.visitMethod(access, name, desc, signature, exceptions));
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className_ = name;

        // add a member variable that will be used to store the GenericQueryManager instance in when the bean instance is retrieved
        cv.visitField(ACC_PRIVATE | ACC_SYNTHETIC | ACC_TRANSIENT, GQM_VAR_NAME, "Lrife/database/querymanagers/generic/GenericQueryManager;", null, null);

        // add a member variable that will be used to store the lazily loaded values of many-to-one properties
        cv.visitField(ACC_PRIVATE | ACC_SYNTHETIC | ACC_TRANSIENT, LAZY_LOADED_VAR_NAME, Type.getDescriptor(Map.class), null, null);

        super.visit(version, access, name, signature, superName, interfaces);
    }
}
