/*
 * Copyright 2001-2008 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.instrument;

import rife.tools.InstrumentationUtils;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * This is a no-op transformer that is just used to output the instrumented
 * bytecode of classes when the {@value rife.tools.InstrumentationUtils#PROPERTY_RIFE_INSTRUMENTATION_DUMP}
 * system property is set.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @version $Revision$
 * @since 1.6
 */
public class FinalTransformer extends RifeTransformer {
    protected byte[] transformRife(ClassLoader loader, String classNameInternal, Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        InstrumentationUtils.dumpClassBytes("adapted", classNameInternal, classfileBuffer);
        return classfileBuffer;
    }
}
