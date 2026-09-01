/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class CallbackChangedIdentifierException extends CrudException {
    @Serial private static final long serialVersionUID = 8186182667981796410L;

    private final Class beanClass_;
    private final int requestedId_;
    private final int storedId_;

    public CallbackChangedIdentifierException(Class beanClass, int requestedId, int storedId) {
        super("A callback of bean '" + beanClass.getName() + "' changed the identifier of an instance while it was being stored, " +
              "which stored it as '" + storedId + "' instead of as the '" + requestedId + "' that was asked for.");

        beanClass_ = beanClass;
        requestedId_ = requestedId;
        storedId_ = storedId;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public int getRequestedId() {
        return requestedId_;
    }

    public int getStoredId() {
        return storedId_;
    }
}
