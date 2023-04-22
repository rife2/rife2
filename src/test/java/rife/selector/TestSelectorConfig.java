/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.selector;

public class TestSelectorConfig implements NameSelector {
    @Override
    public String getActiveName() {
        return "xml2.config selected";
    }
}
