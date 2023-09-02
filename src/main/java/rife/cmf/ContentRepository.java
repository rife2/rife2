/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

/**
 * Contains the information that's required to describe a content repository.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContentRepository extends Validation {
    public final static String DEFAULT = "default";

    private String name_ = null;

    /**
     * Instantiates a new {@code ContentRepository} instance.
     *
     * @since 1.0
     */
    public ContentRepository() {
    }

    @Override
    public void activateValidation() {
        addConstraint(new ConstrainedProperty("name")
            .notNull(true)
            .notEmpty(true)
            .maxLength(100)
            .unique(true));
    }

    /**
     * Sets the name of the content repository.
     *
     * @param name the name
     * @return the current {@code ContentRepository} instance
     * @see #setName(String)
     * @see #getName()
     * @since 1.0
     */
    public ContentRepository name(String name) {
        setName(name);

        return this;
    }

    /**
     * Sets the name of the content repository.
     *
     * @param name the name
     * @see #name(String)
     * @see #getName()
     * @since 1.0
     */
    public void setName(String name) {
        name_ = name;
    }

    /**
     * Retrieves the name of the content repository.
     *
     * @return {@code null} if the stored {@code Content} instance
     * has no name; or
     * <p>the name of the content
     * @see #name(String)
     * @see #setName(String)
     * @since 1.0
     */
    public String getName() {
        return name_;
    }
}
