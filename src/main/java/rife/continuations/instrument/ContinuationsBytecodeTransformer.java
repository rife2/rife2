/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.ClassReader;
import rife.asm.ClassVisitor;
import rife.asm.ClassWriter;
import rife.continuations.ContinuationConfigInstrument;

import java.util.logging.Level;

/**
 * Abstract class that transforms the bytecode of regular classes so that
 * they support continuations functionalities.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class ContinuationsBytecodeTransformer {
    /**
     * Perform the class transformation.
     * <p>If the class doesn't implement the marker interface that is set up
     * in the instrumentation config, the original bytes will be returned.
     *
     * @param configInstrument the configuration for the instrumentation
     * @param rawBytes         the raw bytes of the class to instrument
     * @param classname        the name of the class to instrument
     * @return a byte array with the instrumented bytecode; or
     * <p>the original raw byte array if the class didn't need to be
     * instrumented
     * @throws ClassNotFoundException when an error occurs during the
     *                                inspection or transformation
     * @since 1.0
     */
    public static byte[] transformIntoResumableBytes(ContinuationConfigInstrument configInstrument, byte[] rawBytes, String classname)
    throws ClassNotFoundException {
        // adapts the class on the fly
        byte[] resumable_bytes = null;
        var reader_flags = ClassReader.SKIP_FRAMES;
        try {
            ContinuationDebug.LOGGER.finest("METRICS:");
            var metrics_reader = new ClassReader(rawBytes);
            var metrics_visitor = new MetricsClassVisitor(configInstrument, classname);
            metrics_reader.accept(metrics_visitor, reader_flags);
            ContinuationDebug.LOGGER.finest("\n");

            if (metrics_visitor.makeResumable()) {
                ContinuationDebug.LOGGER.finest("TYPES:");
                var types_reader = new ClassReader(rawBytes);
                var types_visitor = new TypesClassVisitor(configInstrument, metrics_visitor, classname);
                types_reader.accept(types_visitor, reader_flags);
                ContinuationDebug.LOGGER.finest("\n");

                ContinuationDebug.LOGGER.finest("SOURCE:");
                var resumable_reader = new ClassReader(rawBytes);
                var resumable_writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                ClassVisitor resumable_visitor = new ResumableClassAdapter(configInstrument, metrics_visitor, types_visitor, classname, resumable_writer);
                resumable_reader.accept(resumable_visitor, reader_flags);
                resumable_bytes = resumable_writer.toByteArray();
                ContinuationDebug.LOGGER.finest("\n");

                if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST)) {
                    ContinuationDebug.LOGGER.finest("RESULT:");
                    var reporting_reader = new ClassReader(resumable_bytes);
                    ClassVisitor reporting_visitor = new ResumableClassAdapter(configInstrument, null, null, classname, null);
                    reporting_reader.accept(reporting_visitor, reader_flags);
                }
            }
        } catch (Exception e) {
            throw new ClassNotFoundException(classname, e);
        }

        return resumable_bytes;
    }
}
