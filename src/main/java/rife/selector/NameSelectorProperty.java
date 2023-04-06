/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.selector;

import java.util.Objects;

/**
 * Selects a name according to the {@code rife.application}
 * application property.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see NameSelector
 * @since 1.0
 */
public class NameSelectorProperty implements NameSelector {
    public String getActiveName() {
        return Objects.toString(System.getProperties().get("rife.application"), "");
    }
}
