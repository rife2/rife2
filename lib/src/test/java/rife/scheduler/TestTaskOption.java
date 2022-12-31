/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import org.junit.jupiter.api.Test;
import rife.tools.ExceptionUtils;
import rife.validation.ValidationError;

import java.util.Iterator;

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
        int taskid = 1;
        String name = "name";
        String value = "value";

        TaskOption taskoption = new TaskOption();
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
            int taskid = 1;
            String name = "name";
            String value = "value";

            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(taskid);
            taskoption.setName(name);
            taskoption.setValue(value);

            TaskOption taskoption_clone = taskoption.clone();
            assertNotSame(taskoption, taskoption_clone);
            assertNotNull(taskoption_clone);
            assertEquals(taskoption, taskoption_clone);
        } catch (CloneNotSupportedException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testTaskOptionValidation() {
        TaskOption taskoption = new TaskOption();
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
