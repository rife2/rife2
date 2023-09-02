/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import rife.tools.InstrumentationUtils;

import java.security.ProtectionDomain;

/**
 * This is a no-op transformer that is just used to output the initial
 * bytecode of classes that will be instrumented when the {@value rife.tools.InstrumentationUtils#PROPERTY_RIFE_INSTRUMENTATION_DUMP}
 * system property is set.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class InitialTransformer extends RifeTransformer {
    @Override
    protected byte[] transformRife(ClassLoader loader, String classNameInternal, Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        InstrumentationUtils.dumpClassBytes("initial", classNameInternal, classfileBuffer);
        return classfileBuffer;
    }
}
