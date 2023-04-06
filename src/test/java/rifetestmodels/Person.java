/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

public class Person {
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
