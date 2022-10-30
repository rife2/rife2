/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class Person extends MetaData {
    private String firstname_;
    private String lastname_;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("firstname")
            .maxLength(10)
            .notNull(true));
        addConstraint(new ConstrainedProperty("lastname")
            .inList("Smith", "Jones", "Ronda"));
    }

    public void setFirstname(String firstname) {
        firstname_ = firstname;
    }

    public String getFirstname() {
        return firstname_;
    }

    public void setLastname(String lastname) {
        lastname_ = lastname;
    }

    public String getLastname() {
        return lastname_;
    }
}
