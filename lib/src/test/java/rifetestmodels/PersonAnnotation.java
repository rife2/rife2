/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.annotations.MetaDataClass;

@MetaDataClass(PersonMetaData.class)
public class PersonAnnotation {
    private String firstname_;
    private String lastname_;

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