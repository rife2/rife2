/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class PropertyInMultipleSectionsException extends CrudException {
    @Serial private static final long serialVersionUID = 5127403618945292417L;

    private final String property_;
    private final String section_;

    public PropertyInMultipleSectionsException(String property, String section) {
        super("The property '" + property + "' is already part of section '" + section + "', a property belongs to a single section.");

        property_ = property;
        section_ = section;
    }

    public String getProperty() {
        return property_;
    }

    public String getSection() {
        return section_;
    }
}
