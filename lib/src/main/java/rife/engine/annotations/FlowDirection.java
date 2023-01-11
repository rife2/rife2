/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.annotations;

/**
 * The flow direction that will be used by a field annotation that supports it.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public enum FlowDirection {
    /**
     * Data will only flow inwards to the field.
     */
    IN,

    /**
     * Data will only flow outwards from the field.
     */
    OUT,

    /**
     * Data will flow both inwards and onwards from the field.
     */
    IN_OUT
}
