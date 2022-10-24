/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class ResourceRemovalErrorException extends ResourceWriterErrorException {
    @Serial private static final long serialVersionUID = -2804786350448231032L;

    private final String name_;

    public ResourceRemovalErrorException(String name, Throwable e) {
        super("Error while removing the resource with the name '" + name + "'.", e);

        name_ = name;
    }

    public String getName() {
        return name_;
    }
}
