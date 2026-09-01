/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class HookFailedException extends CrudException {
    @Serial private static final long serialVersionUID = 6802477425583991226L;

    private final Class beanClass_;
    private final String hook_;

    public HookFailedException(Class beanClass, String hook, Throwable cause) {
        super("The '" + hook + "' hook of bean '" + beanClass.getName() + "' failed.", cause);

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
