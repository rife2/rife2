/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.instrument;

import rife.asm.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static rife.asm.Opcodes.ASM9;

class LazyLoadMethodCollector extends ClassVisitor {
    private Map<String, String> methods_ = null;

    LazyLoadMethodCollector() {
        super(ASM9);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // only consider public non-static and non-synthetic methods
        if ((access & Opcodes.ACC_PUBLIC) != 0 &&
            (access & Opcodes.ACC_STATIC) == 0 &&
            (access & Opcodes.ACC_SYNTHETIC) == 0) {
            if (name.length() > 3) {
                var return_type = Type.getReturnType(desc);
                var argument_types = Type.getArgumentTypes(desc);

                var add = false;

                // only consider getters without arguments and an object return type
                // that is not part of the JDK java.lang package
                if (name.startsWith("get") &&
                    0 == argument_types.length &&
                    Type.OBJECT == return_type.getSort() &&
                    !return_type.getClassName().startsWith("java.lang.")) {
                    add = true;
                }
                // only consider setters with one argument type that is not part of the
                // JDK java.lang package and a void return type
                else if (name.startsWith("set") &&
                         1 == argument_types.length &&
                         Type.VOID == return_type.getSort() &&
                         !argument_types[0].getClassName().startsWith("java.lang.")) {
                    add = true;
                }

                if (add) {
                    if (null == methods_) {
                        methods_ = new HashMap<>();
                    }

                    methods_.put(name, desc);
                }
            }
        }

        return null;
    }

    public Map<String, String> getMethods() {
        if (null == methods_) {
            return Collections.emptyMap();
        }

        return methods_;
    }
}
