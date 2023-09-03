/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.*;
import rife.continuations.ContinuationConfigInstrument;

import java.util.logging.Level;

import static rife.asm.Opcodes.ASM9;
import static rife.continuations.instrument.ContinuationDebug.join;

class ResumableClassAdapter extends ClassVisitor {
    private final ContinuationConfigInstrument config_;
    private final MetricsClassVisitor metrics_;
    private final TypesClassVisitor types_;

    private final String className_;
    private final String entryMethodName_;
    private final String entryMethodDesc_;
    private final ClassVisitor classVisitor_;
    private final boolean adapt_;
    private final NoOpAnnotationVisitor annotationVisitor_ = new NoOpAnnotationVisitor();

    ResumableClassAdapter(ContinuationConfigInstrument config, MetricsClassVisitor metrics, TypesClassVisitor types, String className, ClassVisitor classVisitor) {
        super(ASM9);
        config_ = config;
        metrics_ = metrics;
        types_ = types;

        className_ = className;
        entryMethodName_ = config.getEntryMethodName();
        entryMethodDesc_ = config.getEntryMethodDescriptor();
        classVisitor_ = classVisitor;
        adapt_ = (classVisitor != null);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visit                   (" + access + ", \"" + name + "\", \"" + signature + "\", \"" + superName + "\", " + (null == interfaces ? null : join(interfaces, ",")) + ")");

        if (adapt_) {
            classVisitor_.visit(version, access, name, signature, superName, interfaces);
        }
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitField              (" + access + ", \"" + name + "\", \"" + desc + "\", " + signature + ", " + value + ")");

        if (adapt_) {
            return classVisitor_.visitField(access, name, desc, signature, value);
        }

        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitMethod             (" + access + ", \"" + name + "\", \"" + desc + "\", \"" + signature + "\", " + (null == exceptions ? null : join(exceptions, ",")) + ")");

        // only adapt the entry method
        if (entryMethodName_.equals(name) &&
            entryMethodDesc_.equals(desc)) {
            if (adapt_) {
                if (!metrics_.makeResumable()) {
                    return new ResumableMethodAdapter(config_, null, classVisitor_.visitMethod(access, name, desc, signature, exceptions), className_, false, -1, 0);
                } else {
                    return new ResumableMethodAdapter(config_, types_, classVisitor_.visitMethod(access, name, desc, signature, exceptions), className_, true, metrics_.getMaxLocals(), metrics_.getPauseCount());
                }
            } else {
                return new ResumableMethodAdapter(config_, null, null, null, false, -1, 0);
            }
        }

        if (null == classVisitor_) {
            return null;
        } else {
            return classVisitor_.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitInnerClass         (\"" + name + "\", \"" + outerName + "\", \"" + innerName + ", " + access + ")");

        if (adapt_) {
            classVisitor_.visitInnerClass(name, outerName, innerName, access);
        }
    }

    public void visitAttribute(Attribute attr) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitAttribute          (" + attr + ")");

        if (adapt_) {
            classVisitor_.visitAttribute(attr);
        }
    }

    public void visitSource(String source, String debug) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitSource             (\"" + source + "\", \"" + debug + "\")");

        if (adapt_) {
            classVisitor_.visitSource(source, debug);
        }
    }

    public void visitOuterClass(String owner, String name, String desc) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitOuterClass         (\"" + owner + "\", \"" + name + "\", \"" + desc + "\")");

        if (adapt_) {
            classVisitor_.visitOuterClass(owner, name, desc);
        }
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitAnnotation         (\"" + desc + "\", " + visible + ")");

        if (adapt_) {
            return classVisitor_.visitAnnotation(desc, visible);
        }

        return annotationVisitor_;
    }

    public void visitEnd() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST))
            ContinuationDebug.LOGGER.finest("Class:visitEnd                ()");

        if (adapt_) {
            classVisitor_.visitEnd();
        }
    }
}
