/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class ResourceStructureRemovalException extends ResourceWriterErrorException {
    @Serial private static final long serialVersionUID = 8328850970373591003L;

    public ResourceStructureRemovalException(Throwable e) {
        super("Error while removing the resource structure.", e);
    }
}
