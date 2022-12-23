/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import rife.asm.*;

import static rife.asm.Opcodes.ASM9;

/**
 * Detects whether a class has the {@code MetaDataClass} class annotation
 * by analyzing its byte code.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class MetaDataClassAnnotationDetector {
    private static final String META_DATA_ANNOTATION_INTERNAL_TYPE = "Lrife/validation/annotations/MetaDataClass;";

    /**
     * Retrieves the class name of the metadata class that is associated
     * through the {@code MetaDataClass} class annotation.
     *
     * @param bytes the array of bytes that defines the class that needs to be
     *              analyzed
     * @return the name of the associated metadata class; or
     * <p>
     * {@code null} if no metadata class was specified through an annotation
     * @since 1.0
     */
    public static String getMetaDataClassName(final byte[] bytes) {
        var visitor = new DetectionClassVisitor();
        var detection_reader = new ClassReader(bytes);
        detection_reader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        return visitor.getMetaDataClassName();
    }

    private static class DetectionClassVisitor extends ClassVisitor {
        private String metaDataClassName_ = null;

        protected DetectionClassVisitor() {
            super(ASM9);
        }

        private String getMetaDataClassName() {
            return metaDataClassName_;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (META_DATA_ANNOTATION_INTERNAL_TYPE.equals(desc)) {
                return new AnnotationVisitor(ASM9) {
                    public void visit(String name, Object value) {
                        if ("value".equals(name) &&
                            value != null) {
                            metaDataClassName_ = ((Type) value).getClassName();
                        }
                    }
                };
            }

            return null;
        }
    }
}