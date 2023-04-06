/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.exceptions;

import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class MissingManyToOneMainPropertyException extends DatabaseException {
    @Serial private static final long serialVersionUID = -860044159844481242L;

    private final Class associationClass_;
    private final String associationProperty_;
    private final Class mainClass_;

    public MissingManyToOneMainPropertyException(Class associationClass, String associationProperty, Class mainClass) {
        super("The bean '" + associationClass.getName() + "' declares a many-to-one association relationship on property '" + associationProperty + "', however no matching manyToOne constraint can be find on any property in the main bean '" + mainClass.getName() + "'.");

        associationClass_ = associationClass;
        associationProperty_ = associationProperty;
        mainClass_ = mainClass;
    }

    public Class getAssociationClass() {
        return associationClass_;
    }

    public String getAssociationProperty() {
        return associationProperty_;
    }

    public Class getMainClass() {
        return mainClass_;
    }
}
