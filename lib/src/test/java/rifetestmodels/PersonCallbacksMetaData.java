/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.database.querymanagers.generic.*;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class PersonCallbacksMetaData extends MetaData implements CallbacksProvider {
    public Callbacks getCallbacks() {
        return new AbstractCallbacks<PersonCallbacks>() {
            public boolean beforeSave(PersonCallbacks object) {
                object.setFirstname("beforeSave");
                return true;
            }
        };
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("firstname")
            .maxLength(10)
            .notNull(true));
        addConstraint(new ConstrainedProperty("lastname")
            .inList("Smith", "Jones", "Ronda"));
    }
}
