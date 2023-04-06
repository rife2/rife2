/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;
import java.net.URL;

public class CantOpenResourceStreamException extends ResourceFinderErrorException {
    @Serial
    private static final long serialVersionUID = -8647491507074658157L;

    private final URL resource_;

    public CantOpenResourceStreamException(URL resource, Throwable e) {
        super("Error while opening a stream to resource '" + resource.toString() + "'.", e);

        resource_ = resource;
    }

    public URL getResource() {
        return resource_;
    }
}
