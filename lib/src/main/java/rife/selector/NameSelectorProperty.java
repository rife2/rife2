/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.selector;

import java.util.Objects;

/**
 * Selects a name according to the <code>rife.application</code>
 * application property.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see NameSelector
 * @since 2.0
 */
public class NameSelectorProperty implements NameSelector {
    public String getActiveName() {
        return Objects.toString(System.getProperties().get("rife.application"), "");
    }
}
