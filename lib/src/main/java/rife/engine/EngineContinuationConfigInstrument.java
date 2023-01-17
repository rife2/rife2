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
    public String getContinuableMarkerInterfaceName() {
        return Element.class.getName();
    }

    public String getContinuableSupportClassName() {
        return Context.class.getName();
    }

    public String getEntryMethodName() {
        return "process";
    }

    public Class getEntryMethodReturnType() {
        return void.class;
    }

    public Class[] getEntryMethodArgumentTypes() {
        return new Class[]{Context.class};
    }

    public String getPauseMethodName() {
        return "pause";
    }
}
