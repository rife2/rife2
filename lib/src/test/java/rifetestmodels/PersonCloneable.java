/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

public class PersonCloneable extends Person implements Cloneable {
    public Object clone()
    throws CloneNotSupportedException {
        PersonCloneable new_person = (PersonCloneable) super.clone();

        if (null == getFirstname()) {
            new_person.setFirstname("autofirst");
            return new_person;
        }

        if (null == getLastname()) {
            new_person.setLastname("autolast");
            return new_person;
        }

        return new_person;
    }
}
