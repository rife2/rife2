/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.util.ListResourceBundle;

public class TestResourceBundleClass extends ListResourceBundle {
    public Object[][] getContents() {
        return new Object[][]{{"THE_CLASS_KEY", "list key class"}};
    }
}
