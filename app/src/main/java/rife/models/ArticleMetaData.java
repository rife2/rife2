/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.models;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class ArticleMetaData extends MetaData {
    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("title")
            .notNull(true)
            .notEmpty(true)
            .maxLength(120));
        addConstraint(new ConstrainedProperty("email")
            .notNull(true)
            .notEmpty(true)
            .maxLength(100)
            .email(true));
        addConstraint(new ConstrainedProperty("body")
            .notNull(true)
            .notEmpty(true));
    }
}