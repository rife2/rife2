/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import rife.continuations.instrument.ContinuationsTransformer;
import rife.engine.EngineContinuationConfigInstrument;

/**
 * This is a bytecode transformer that will modify classes so that they
 * receive the functionalities that are required to support the continuations
 * functionalities as they are provided by RIFE2's web engine.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class EngineContinuationsTransformer extends ContinuationsTransformer {
    public static final String AGENT_ACTIVE_PROPERTY = "rife.agent.engine.continuations";

    public EngineContinuationsTransformer() {
        super(new EngineContinuationConfigInstrument(), AGENT_ACTIVE_PROPERTY);
    }
}

