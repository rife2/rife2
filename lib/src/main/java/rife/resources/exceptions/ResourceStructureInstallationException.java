/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class ResourceStructureInstallationException extends ResourceWriterErrorException {
    @Serial private static final long serialVersionUID = -4700139708043242285L;

    public ResourceStructureInstallationException(Throwable e) {
        super("Error while installing the resource structure.", e);
    }
}
