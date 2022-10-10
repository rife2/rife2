/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.selector;

/**
 * Selects a name according to the current operating system.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @see NameSelector
 * @since 2.0
 */
public class NameSelectorOs implements NameSelector {
    public String getActiveName() {
        return System.getProperty("os.name").toLowerCase();
    }
}
