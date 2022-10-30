/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.capabilities;

import rife.datastructures.*;

public class Capability extends EnumClass<String> {
    public static final Capability LIMIT = new Capability("LIMIT");
    public static final Capability LIMIT_PARAMETER = new Capability("LIMIT_PARAMETER");
    public static final Capability OFFSET = new Capability("OFFSET");
    public static final Capability OFFSET_PARAMETER = new Capability("OFFSET_PARAMETER");

    Capability(String identifier) {
        super(identifier);
    }

    public static Capability getMethod(String name) {
        return getMember(Capability.class, name);
    }
}

