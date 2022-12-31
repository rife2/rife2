/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import org.junit.jupiter.api.Test;
import rife.scheduler.exceptions.FrequencyException;
import rife.tools.ExceptionUtils;
import rife.validation.ValidationError;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestTask {
    @Test
    void testInstantiateTask() {
        Task task = null;

        assertNull(task);
        task = new Task();
        assertNotNull(task);
    }

    @Test
    void testPopulateTask() {
        int id = 1;
        String type = TestTasktypes.UPLOAD_GROUPS;
        long planned = System.currentTimeMillis();
        String frequency = "* * * * *";
        boolean busy = true;

        try {
            Task task = new Task();
            task.setId(id);
            task.setType(type);
            task.setPlanned(planned);
            task.setFrequency(frequency);
            task.setBusy(busy);

            assertEquals(id, task.getId());
            assertEquals(type, task.getType());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(planned);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            assertEquals(calendar.getTimeInMillis(), task.getPlanned());
            assertEquals(frequency, task.getFrequency());
            assertEquals(busy, task.isBusy());
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCloneTask() {
        int id = 1;
        String type = TestTasktypes.UPLOAD_GROUPS;
        long planned = System.currentTimeMillis();
        String frequency = "* * * * *";
        boolean busy = true;

        try {
            Task task = new Task();
            task.setId(id);
            task.setType(type);
            task.setPlanned(planned);
            task.setFrequency(frequency);
            task.setBusy(busy);

            Task task_clone = task.clone();
            assertNotSame(task, task_clone);
            assertNotNull(task_clone);
            assertEquals(task, task_clone);
        } catch (FrequencyException | CloneNotSupportedException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testTaskValidation() {
        try {
            Task task = new Task();
            assertTrue(false == task.validate());
            assertTrue(2 == task.countValidationErrors());
            ValidationError error = null;
            Iterator<ValidationError> it = task.getValidationErrors().iterator();
            error = it.next();
            assertEquals(error.getIdentifier(), "mandatory");
            assertEquals(error.getSubject(), "type");
            error = it.next();
            assertEquals(error.getIdentifier(), "mandatory");
            assertEquals(error.getSubject(), "planned");

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(System.currentTimeMillis() + 2000);
            task.setFrequency("* * * * *");
            task.setBusy(false);
            task.resetValidation();
            assertTrue(true == task.validate());

            Calendar cal = Calendar.getInstance();
            cal.set(1970, 10, 25);
            task.setPlanned(cal.getTime());
            task.resetValidation();
            assertTrue(false == task.validate());
            assertTrue(1 == task.countValidationErrors());
            Set<ValidationError> set = task.getValidationErrors();
            error = set.iterator().next();
            assertEquals(error.getIdentifier(), "invalid");
            assertEquals(error.getSubject(), "planned");
            task.setPlanned(System.currentTimeMillis() + 2000);

            task.setFrequency(null);
            task.resetValidation();
            assertTrue(true == task.validate());
            task.setFrequency("* * * * *");
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
