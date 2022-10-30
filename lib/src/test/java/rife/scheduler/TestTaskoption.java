/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import org.junit.jupiter.api.Test;
import rife.tools.ExceptionUtils;
import rife.validation.ValidationError;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestTaskoption {
    @Test
    public void testInstantiateTaskoption() {
        Taskoption taskoption = null;
        assertNull(taskoption);
        taskoption = new Taskoption();
        assertNotNull(taskoption);
    }

    @Test
    public void testPopulateTaskoption() {
        int taskid = 1;
        String name = "name";
        String value = "value";

        Taskoption taskoption = new Taskoption();
        taskoption.setTaskId(taskid);
        taskoption.setName(name);
        taskoption.setValue(value);

        assertEquals(taskid, taskoption.getTaskId());
        assertEquals(name, taskoption.getName());
        assertEquals(value, taskoption.getValue());
    }

    @Test
    public void testCloneTaskoption() {
        try {
            int taskid = 1;
            String name = "name";
            String value = "value";

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(taskid);
            taskoption.setName(name);
            taskoption.setValue(value);

            Taskoption taskoption_clone = taskoption.clone();
            assertNotSame(taskoption, taskoption_clone);
            assertNotNull(taskoption_clone);
            assertEquals(taskoption, taskoption_clone);
        } catch (CloneNotSupportedException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testTaskoptionValidation() {
        Taskoption taskoption = new Taskoption();
        assertEquals(false, taskoption.validate());
        assertEquals(3, taskoption.countValidationErrors());
        ValidationError error = null;
        Iterator<ValidationError> it = taskoption.getValidationErrors().iterator();
        error = it.next();
        assertEquals(error.getIdentifier(), "invalid");
        assertEquals(error.getSubject(), "taskId");
        error = it.next();
        assertEquals(error.getIdentifier(), "mandatory");
        assertEquals(error.getSubject(), "name");
        error = it.next();
        assertEquals(error.getIdentifier(), "mandatory");
        assertEquals(error.getSubject(), "value");
    }
}
