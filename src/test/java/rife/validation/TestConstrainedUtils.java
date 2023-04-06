/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.database.ConstrainedClass;
import rife.database.NotConstrainedClass;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestConstrainedUtils {
    public void testGetConstrainedInstance() {
        assertNotNull(ConstrainedUtils.getConstrainedInstance(ConstrainedClass.class));
        assertNull(ConstrainedUtils.getConstrainedInstance(NotConstrainedClass.class));
        assertNotNull(ConstrainedUtils.getConstrainedInstance(ConstrainedStaticInnerClass.class));
        assertNull(ConstrainedUtils.getConstrainedInstance(NotConstrainedStaticInnerClass.class));
        assertNull(ConstrainedUtils.getConstrainedInstance(ConstrainedInnerClass.class));
        assertNull(ConstrainedUtils.getConstrainedInstance(NotConstrainedInnerClass.class));
    }

    public static class ConstrainedStaticInnerClass extends Validation {
    }

    public static class NotConstrainedStaticInnerClass {
    }

    public class ConstrainedInnerClass extends Validation {
    }

    public class NotConstrainedInnerClass {
    }
}
