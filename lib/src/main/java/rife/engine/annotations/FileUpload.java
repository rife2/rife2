/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.annotations;

import java.lang.annotation.*;

/**
 * Declares a file that was uploaded in this request.
 *
 * This can either be an <code>UploadedFile</code> instance with all the details,
 * a <code>File</code> instance which will then directly
 * point to the temporary uploaded file, or a <code>String</code> which will
 * contain the path of the temporary uploaded file.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface FileUpload {
    /**
     * The name of the uploaded file.
     *
     * @since 2.0
     */
    String name() default "";
}
