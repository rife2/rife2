/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.continuations.ContinuationConfigInstrument;
import rife.instrument.ClassBytesProvider;
import rife.instrument.ClassInterfaceDetector;

/**
 * Detects whether a class implements the continuable marker interface that is
 * set up in the provided instrumentation config.
 * <p>This is done without actually loading the class and by analyzing the
 * bytecode itself. It's important to not load the class because the class is
 * supposed to be instrumented before actually loading it, if it implements
 * the marker interface.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContinuableDetector extends ClassInterfaceDetector {
    /**
     * Creates a new instance of the detector.
     *
     * @param config        the instrumentation configuration
     * @param bytesProvider the bytecode provider that will be used to load
     *                      the bytes relevant classes and interfaces
     * @since 1.0
     */
    public ContinuableDetector(ContinuationConfigInstrument config, ClassBytesProvider bytesProvider) {
        super(bytesProvider, config.getContinuableMarkerInterfaceName().replace('.', '/').intern());
    }
}
