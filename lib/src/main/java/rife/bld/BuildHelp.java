/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

public interface BuildHelp {
    default String getDescription() {
        return "";
    }

    default String getHelp(String topic) {
        return "";
    }
}
