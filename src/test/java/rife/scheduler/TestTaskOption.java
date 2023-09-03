/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import org.junit.jupiter.api.Test;
import rife.tools.ExceptionUtils;
import rife.validation.ValidationError;

import static org.junit.jupiter.api.Assertions.*;

public class TestTaskOption {
    @Test
    void testInstantiateTaskOption() {
        TaskOption taskoption = null;
        assertNull(taskoption);
        taskoption = new TaskOption();
        assertNotNull(taskoption);
    }

    @Test
    void testPopulateTaskOption() {
        var taskid = 1;
        var name = "name";
        var value = "value";

        var taskoption = new TaskOption();
        taskoption.setTaskId(taskid);
        taskoption.setName(name);
        taskoption.setValue(value);

        assertEquals(taskid, taskoption.getTaskId());
        assertEquals(name, taskoption.getName());
        assertEquals(value, taskoption.getValue());
    }

    @Test
    void testCloneTaskOption() {
        try {
            var taskid = 1;
            var name = "name";
            var value = "value";

            var taskoption = new TaskOption();
            taskoption.setTaskId(taskid);
            taskoption.setName(name);
            taskoption.setValue(value);

            var taskoption_clone = taskoption.clone();
            assertNotSame(taskoption, taskoption_clone);
            assertNotNull(taskoption_clone);
            assertEquals(taskoption, taskoption_clone);
        } catch (CloneNotSupportedException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testTaskOptionValidation() {
        var taskoption = new TaskOption();
        assertFalse(taskoption.validate());
        assertEquals(3, taskoption.countValidationErrors());
        ValidationError error = null;
        var it = taskoption.getValidationErrors().iterator();
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
