/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

public class SqlNull {
    public static final SqlNull NULL = new SqlNull();

    private SqlNull() {
    }

    public String toString() {
        return "NULL";
    }
}
