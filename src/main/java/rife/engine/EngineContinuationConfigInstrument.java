/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.continuations.ContinuationConfigInstrument;

/**
 * Configures the continuations system for the web engine.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class EngineContinuationConfigInstrument implements ContinuationConfigInstrument {
    @Override
    public String getContinuableMarkerInterfaceName() {
        return "rife.engine.Element";
    }

    @Override
    public String getContinuableSupportClassName() {
        return "rife.engine.Context";
    }

    @Override
    public String getEntryMethodName() {
        return "process";
    }

    @Override
    public String getEntryMethodDescriptor() {
        return "(Lrife/engine/Context;)V";
    }

    @Override
    public String getPauseMethodName() {
        return "pause";
    }
}
