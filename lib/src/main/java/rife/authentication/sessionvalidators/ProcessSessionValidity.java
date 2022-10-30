/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import rife.database.DbRowProcessor;

public abstract class ProcessSessionValidity extends DbRowProcessor {
    public abstract int getValidity();
}

