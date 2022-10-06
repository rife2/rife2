/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;
import java.net.URL;

public class CantRetrieveResourceContentException extends ResourceFinderErrorException {
    @Serial
    private static final long serialVersionUID = 5514414842991049686L;

    private final URL resource_;
    private final String encoding_;

    public CantRetrieveResourceContentException(URL resource, String encoding, Throwable e) {
        super("Error while retrieving the content of resource '" + resource.toString() + "' with encoding '" + encoding + "'.", e);

        resource_ = resource;
        encoding_ = encoding;
    }

    public URL getResource() {
        return resource_;
    }

    public String getEncoding() {
        return encoding_;
    }
}
