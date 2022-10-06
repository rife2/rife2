/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;
import java.net.URL;

public class TransformerErrorException extends ProcessingException {
    @Serial private static final long serialVersionUID = -7256768610167542980L;

    private URL resource_ = null;

    public TransformerErrorException(URL resource, Throwable cause) {
        super("Error while transforming resource '" + resource + "'.", cause);
        resource_ = resource;
    }

    public URL getResource() {
        return resource_;
    }
}
