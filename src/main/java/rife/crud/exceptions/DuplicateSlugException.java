/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class DuplicateSlugException extends CrudException {
    @Serial private static final long serialVersionUID = 5059264649255944855L;

    private final String slug_;
    private final Class beanClass_;
    private final Class registeredBeanClass_;

    public DuplicateSlugException(String slug, Class beanClass, Class registeredBeanClass) {
        super("The slug '" + slug + "' of bean '" + beanClass.getName() + "' is already used by bean '" +
              registeredBeanClass.getName() + "', provide another one with slug().");

        slug_ = slug;
        beanClass_ = beanClass;
        registeredBeanClass_ = registeredBeanClass;
    }

    public String getSlug() {
        return slug_;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public Class getRegisteredBeanClass() {
        return registeredBeanClass_;
    }
}
