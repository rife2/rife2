/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.validation.ConstrainedProperty;

public class UniqueBeanNotNull extends UniqueBean {
    protected void activateValidation() {
        super.activateValidation();

        addConstraint(new ConstrainedProperty("thirdString").maxLength(50).notNull(true).defaultValue(""));
    }
}

