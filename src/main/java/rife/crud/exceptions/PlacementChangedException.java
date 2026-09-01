/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class PlacementChangedException extends CrudException {
    @Serial private static final long serialVersionUID = 1214600092670590740L;

    private final Class beanClass_;
    private final int id_;
    private final long restriction_;
    private final int ordinal_;

    public PlacementChangedException(Class beanClass, int id, long restriction, int ordinal) {
        super("The list or the ordinal of instance '" + id + "' of bean '" + beanClass.getName() +
              "' was changed while it was being stored, which the ordinals of the '" + restriction +
              "' list that was locked aren't protected against.");

        beanClass_ = beanClass;
        id_ = id;
        restriction_ = restriction;
        ordinal_ = ordinal;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public int getId() {
        return id_;
    }

    public long getRestriction() {
        return restriction_;
    }

    public int getOrdinal() {
        return ordinal_;
    }
}
