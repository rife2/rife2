/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.annotations;

import java.lang.annotation.*;

import static rife.engine.annotations.FlowDirection.IN;

/**
 * Declares a session attribute.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface SessionAttribute {
    /**
     * The name of the attribute.
     *
     * @since 2.0
     */
    String name() default "";

    /**
     * Determines what the direction of the flow is for processing this field
     *
     * @return the direction of the flow for field processing
     * @since 2.0
     */
    FlowDirection flow() default IN;
}
