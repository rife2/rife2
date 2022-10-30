/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.annotations;

import java.lang.annotation.*;

/**
 * Declares a request parameter.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Parameter {
    /**
     * The name of the parameter.
     *
     * @since 2.0
     */
    String name() default "";
}
