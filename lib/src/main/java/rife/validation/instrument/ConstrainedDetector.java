/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import rife.asm.*;
import rife.instrument.exceptions.ClassBytesNotFoundException;
import rife.instrument.exceptions.VisitInterruptionException;
import rife.tools.ClassBytesLoader;

import static rife.asm.Opcodes.ASM9;

/**
 * Detects whether a class implements the {@code Constrained} interface or not,
 * by analyzing its bytecode.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ConstrainedDetector {
    private static final String CONSTRAINED_INTERNAL_NAME = "rife/validation/Constrained";
    private static final String CONSTRAINED_NAME = "rife.validation.Constrained";
    private static final String OBJECT_INTERNAL_NAME = "java/lang/Object";

    private final ClassBytesLoader bytesLoader_;

    /**
     * Creates new instance by providing a loader that is able to retrieve the
     * bytes of any parent classes that are extended by the class that is
     * being analyzed.
     *
     * @param bytesLoader the loader that will be used to retrieve the bytes
     *                    of the additional classes
     * @since 1.0
     */
    public ConstrainedDetector(ClassBytesLoader bytesLoader) {
        bytesLoader_ = bytesLoader;
    }

    /**
     * Verifies if the Constrained interface is implemented by the class that
     * is defined by the bytes that are provided.
     *
     * @param internedClassname the class name as the previously interned
     *                          string
     * @param bytes             the array of bytes that defines the class that needs to be
     *                          analyzed
     * @return {@code true} if the analyzed class implements the constrained
     * interface; or
     * <p>{@code false} otherwise
     * @throws ClassBytesNotFoundException when the bytes of a parent class
     *                                     can be found
     * @since 1.0
     */
    public boolean isConstrained(String internedClassname, byte[] bytes)
    throws ClassBytesNotFoundException {
        if (CONSTRAINED_NAME == internedClassname) {
            return false;
        }

        ConstrainedDetectionClassVisitor visitor = new ConstrainedDetectionClassVisitor();
        ClassReader detection_reader = null;

        while (!visitor.isConstrained()) {
            detection_reader = new ClassReader(bytes);
            try {
                detection_reader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            } catch (VisitInterruptionException e) {
                // do nothing
            }

            if (null == visitor.getSuperNameInternal() ||
                OBJECT_INTERNAL_NAME == visitor.getSuperNameInternal()) {
                break;
            }

            // get the parent's class' bytecode
            if (!visitor.isConstrained()) {
                String filename = visitor.getSuperNameInternal() + ".class";
                try {
                    bytes = bytesLoader_.getClassBytes(filename);
                } catch (ClassNotFoundException e) {
                    throw new ClassBytesNotFoundException(filename, e);
                }
            }
        }

        return visitor.isConstrained();
    }

    private class ConstrainedDetectionClassVisitor extends ClassVisitor {
        private boolean isConstrained_ = false;
        private String superNameInternal_ = null;

        protected ConstrainedDetectionClassVisitor() {
            super(ASM9);
        }

        private boolean isConstrained() {
            return isConstrained_;
        }

        private String getSuperNameInternal() {
            return superNameInternal_;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            for (String interface_name : interfaces) {
                if (CONSTRAINED_INTERNAL_NAME == interface_name.intern()) {
                    isConstrained_ = true;
                    break;
                }
            }

            if (null == superName) {
                superNameInternal_ = null;
            } else {
                superNameInternal_ = superName.intern();
            }

            throw new VisitInterruptionException();
        }
    }
}
