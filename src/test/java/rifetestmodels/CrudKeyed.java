/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.util.UUID;

/**
 * A bean with a property that the database stores and a form can show, while
 * nothing lists it among the types that a submission is read into.
 */
public class CrudKeyed extends MetaData {
    private int id_ = -1;
    private String name_ = null;
    private UUID reference_ = null;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        addConstraint(new ConstrainedProperty("name").notNull(true).maxLength(60).listed(true).position(0));
        addConstraint(new ConstrainedProperty("reference").listed(true).position(1));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public void setReference(UUID reference) {
        reference_ = reference;
    }

    public UUID getReference() {
        return reference_;
    }
}
