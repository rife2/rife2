/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class MissingManyToOneTableException extends DatabaseException {
    @Serial private static final long serialVersionUID = 9024147800617136452L;

    private Class constrainedClass_;
    private String propertyName_;

    public MissingManyToOneTableException(Class constrainedClass, String propertyName) {
        super("The property '" + propertyName + "' of '" + constrainedClass.getName() + "' has a manyToOne constraint, however the associated table name is missing. This can be provided by giving either the table name or the associated class when the constraint is declared.");

        constrainedClass_ = constrainedClass;
        propertyName_ = propertyName;
    }

    public Class getConstrainedClass() {
        return constrainedClass_;
    }

    public String getPropertyName() {
        return propertyName_;
    }
}
