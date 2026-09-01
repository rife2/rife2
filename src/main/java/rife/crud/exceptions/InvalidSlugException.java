/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class InvalidSlugException extends CrudException {
    @Serial private static final long serialVersionUID = 7768023264686631332L;

    private final Class beanClass_;
    private final String slug_;

    public InvalidSlugException(String slug) {
        this(null, slug);
    }

    public InvalidSlugException(Class beanClass, String slug) {
        super("The slug '" + slug + "'" + (beanClass == null ? "" : " that was derived from bean '" + beanClass.getName() + "'") +
              " isn't usable in a URL, use letters, digits, underscores and dashes.");

        beanClass_ = beanClass;
        slug_ = slug;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getSlug() {
        return slug_;
    }
}
