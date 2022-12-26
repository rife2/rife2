/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import java.io.Serial;

public class CantFindEntityException extends XmlErrorException {
    @Serial private static final long serialVersionUID = -2964903002673180847L;

    private final String entity_;

    public CantFindEntityException(String entity, Throwable e) {
        super("Can't find the entity '" + entity + "'.", e);

        entity_ = entity;
    }

    public String getEntity() {
        return entity_;
    }
}
