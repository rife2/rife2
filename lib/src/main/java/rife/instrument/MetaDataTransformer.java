/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import rife.validation.instrument.MetaDataInstrumenter;

import java.security.ProtectionDomain;

/**
 * This is a bytecode transformer that will modify classes so that they
 * receive the functionalities that are required to support meta-data
 * merging.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MetaDataTransformer extends RifeTransformer {
    protected byte[] transformRife(ClassLoader loader, String classNameInternal, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return MetaDataInstrumenter.instrument(loader, classNameInternal.replace('/', '.'), classfileBuffer);
    }
}