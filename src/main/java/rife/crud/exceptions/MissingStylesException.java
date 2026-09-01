/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

/**
 * Reports the styles of the administration not being where they travel.
 * <p>They ship in the jar of the framework, so this says that something took
 * them out of it rather than that anything is configured wrongly.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class MissingStylesException extends CrudException {
    @Serial private static final long serialVersionUID = 4611686018427387906L;

    public MissingStylesException() {
        this(null);
    }

    public MissingStylesException(Throwable cause) {
        super("The styles of the administration couldn't be read from 'rife/crud/style.css', which travels in the jar of the framework.", cause);
    }
}
