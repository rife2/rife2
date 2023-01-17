/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtasks;

import rife.workflow.EventType;

public abstract class TestEventTypes {
    public static final EventType TYPE1 = new EventType() {
        public String getType() {
            return "TYPE1";
        }
    };

    public static final EventType TYPE2 = new EventType() {
        public String getType() {
            return "TYPE2";
        }
    };

    public static final EventType BEGIN = new EventType() {
        public String getType() {
            return "BEGIN";
        }
    };

    public static final EventType END = new EventType() {
        public String getType() {
            return "END";
        }
    };
}