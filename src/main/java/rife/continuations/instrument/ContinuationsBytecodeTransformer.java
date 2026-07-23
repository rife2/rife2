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
        return transformIntoResumableBytes(configInstrument, rawBytes, classname, ContinuationsBytecodeTransformer.class.getClassLoader());
    }

    /**
     * Perform the class transformation using the provided class loader when
     * computing stack map frames.
     *
     * @param configInstrument the instrumentation configuration
     * @param rawBytes         the original class bytes
     * @param classname        the class name
     * @param classLoader      the loader that resolves application classes
     * @return the transformed bytes, or {@code null} when no transformation is needed
     * @throws ClassNotFoundException when an error occurs
     * @since 1.10
     */
    public static byte[] transformIntoResumableBytes(ContinuationConfigInstrument configInstrument, byte[] rawBytes, String classname, ClassLoader classLoader)
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
                // Expanded stack-map frames provide the verifier's merged local
                // and operand types at control-flow joins. The types pass uses
                // them as authoritative checkpoints and only simulates the
                // straight-line instructions between them.
                types_reader.accept(types_visitor, ClassReader.EXPAND_FRAMES);
                ContinuationDebug.LOGGER.finest("\n");

                ContinuationDebug.LOGGER.finest("SOURCE:");
                var resumable_reader = new ClassReader(rawBytes);
                var resumable_writer = new ContinuationClassWriter(
                    ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES,
                    rawBytes,
                    classLoader);
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
