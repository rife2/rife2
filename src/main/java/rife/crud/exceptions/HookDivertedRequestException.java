/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class HookDivertedRequestException extends CrudException {
    @Serial private static final long serialVersionUID = 3355821759090283719L;

    private final Class beanClass_;
    private final String hook_;

    public HookDivertedRequestException(Class beanClass, String hook) {
        super("The '" + hook + "' hook of bean '" + beanClass.getName() + "' tried to send the request somewhere else, " +
              "which an after hook can't do since it runs inside the transaction of the operation and taking that " +
              "transaction back is what sending the request elsewhere would do. Send it from a before hook or from " +
              "the operation instead.");

        beanClass_ = beanClass;
        hook_ = hook;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getHook() {
        return hook_;
    }
}
