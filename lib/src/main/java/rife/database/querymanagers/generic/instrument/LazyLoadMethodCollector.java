/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.instrument;

import rife.asm.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static rife.asm.Opcodes.V1_4;

class LazyLoadMethodCollector extends ClassVisitor {
    private Map<String, String> methods_ = null;
    private final AnnotationVisitor annotationVisitor_ = new LazyLoadNoOpAnnotationVisitor();

    LazyLoadMethodCollector() {
        super(V1_4);
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
                        methods_ = new HashMap<String, String>();
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

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    }

    public void visitSource(String source, String debug) {
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return annotationVisitor_;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    public void visitEnd() {
    }

    private static class LazyLoadNoOpAnnotationVisitor extends AnnotationVisitor {
        protected LazyLoadNoOpAnnotationVisitor() {
            super(V1_4);
        }

        public void visit(String name, Object value) {
        }

        public void visitEnum(String name, String desc, String value) {
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return this;
        }

        public AnnotationVisitor visitArray(String name) {
            return this;
        }

        public void visitEnd() {
        }
    }
}
