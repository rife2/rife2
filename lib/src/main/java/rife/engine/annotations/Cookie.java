/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.annotations;

import java.lang.annotation.*;

import static rife.engine.annotations.FlowDirection.IN;

/**
 * Declares a request cookie.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Cookie {
    /**
     * The name of the cookie.
     *
     * @since 2.0
     */
    String value() default "";

    /**
     * Determines what the direction of the flow is for processing this field
     *
     * @return the direction of the flow for field processing
     * @since 2.0
     */
    FlowDirection flow() default IN;
}
