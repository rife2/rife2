/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.continuations.ContinuationConfigInstrument;
import rife.instrument.RifeTransformer;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A bytecode transformer that will modify classes so that they
 * receive the functionalities that are required to support the continuations
 * functionalities as they are provided by RIFE2's web engine.
 * <p>This transformer is internally used by the {@link ContinuationsAgent}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContinuationsTransformer extends RifeTransformer {
    private static final Logger LOGGER = Logger.getLogger(ContinuationsTransformer.class.getName());

    private final ContinuationConfigInstrument configInstrument_;
    private final String property_;

    /**
     * Creates a new transformer.
     *
     * @param configInstrument the instance of the instrumentation
     *                         configuration that will be used for the transformation
     * @since 1.0
     */
    public ContinuationsTransformer(ContinuationConfigInstrument configInstrument, String property) {
        configInstrument_ = configInstrument;
        property_ = property;
    }

    protected byte[] transformRife(ClassLoader loader, String classNameInternal, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.getProperties().setProperty(property_, Boolean.TRUE.toString());

        try {
            var result = ContinuationsBytecodeTransformer.transformIntoResumableBytes(configInstrument_, classfileBuffer, classNameInternal.replace('/', '.'), loader);
            if (result != null) {
                return result;
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(
                Level.SEVERE,
                "Unable to instrument continuation class '" + classNameInternal.replace('/', '.') + "'. The original class bytes will be used.",
                e);
            return classfileBuffer;
        }

        return classfileBuffer;
    }
}
