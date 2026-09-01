/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class CrudSubscriber extends MetaData {
    private int id_ = -1;
    private String email_ = null;
    private String name_ = null;
    private boolean active_ = true;
    private int visits_ = 0;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        addConstraint(new ConstrainedProperty("email").notNull(true).email(true).maxLength(120).unique(true).listed(true).position(0));
        addConstraint(new ConstrainedProperty("name").maxLength(50).listed(true).position(1));
        addConstraint(new ConstrainedProperty("active").listed(true).position(2));
        addConstraint(new ConstrainedProperty("visits").position(3));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setEmail(String email) {
        email_ = email;
    }

    public String getEmail() {
        return email_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public void setActive(boolean active) {
        active_ = active;
    }

    public boolean isActive() {
        return active_;
    }

    public void setVisits(int visits) {
        visits_ = visits;
    }

    public int getVisits() {
        return visits_;
    }
}
