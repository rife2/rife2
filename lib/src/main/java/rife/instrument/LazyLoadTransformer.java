/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import rife.database.querymanagers.generic.instrument.LazyLoadAccessorsBytecodeTransformer;
import rife.tools.ClassBytesLoader;
import rife.validation.instrument.ConstrainedDetector;

import java.security.ProtectionDomain;

/**
 * This is a bytecode transformer that will modify classes so that they
 * receive the functionalities that are required to support lazy-loading
 * of relationships when the {@code GenericQueryManager} is being used.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class LazyLoadTransformer extends RifeTransformer {
    protected byte[] transformRife(ClassLoader loader, String classNameInternal, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        String classname_dotted_interned = classNameInternal.replace('/', '.').intern();

        boolean is_constrained = false;

        try {
            is_constrained = new ConstrainedDetector(new ClassBytesLoader(loader)).isConstrained(classname_dotted_interned, classfileBuffer);

            if (is_constrained) {
                return LazyLoadAccessorsBytecodeTransformer.addLazyLoadToBytes(classfileBuffer);
            }
        } catch (Throwable ignored) {
        }

        return classfileBuffer;
    }
}
