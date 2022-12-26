/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class CantFindResourceException extends XmlErrorException {
    @Serial private static final long serialVersionUID = -3540210087656613629L;

    private final String resourcePath_;

    public CantFindResourceException(String resourcePath, Throwable e) {
        super("Can't find the resource '" + resourcePath + "'.", e);

        resourcePath_ = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath_;
    }
}
