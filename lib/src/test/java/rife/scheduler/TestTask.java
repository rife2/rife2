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
        var id = 1;
        var type = TestTasktypes.UPLOAD_GROUPS;
        var planned = System.currentTimeMillis();
        var frequency = Frequency.MINUTELY;
        var busy = true;

        try {
            var task = new Task();
            task.setId(id);
            task.setType(type);
            task.setPlanned(planned);
            task.setFrequency(frequency);
            task.setBusy(busy);

            assertEquals(id, task.getId());
            assertEquals(type, task.getType());
            var calendar = Calendar.getInstance();
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
    void testCreateTaskOption() {
        var id = 4;
        var task = new Task();
        task.setId(id);
        assertEquals(task.getId(), task.createTaskOption().getTaskId());
    }

    @Test
    void testCloneTask() {
        var id = 1;
        var type = TestTasktypes.UPLOAD_GROUPS;
        var planned = System.currentTimeMillis();
        var busy = true;

        try {
            var task = new Task();
            task.setId(id);
            task.setType(type);
            task.setPlanned(planned);
            task.setFrequency(Frequency.MINUTELY);
            task.setBusy(busy);

            var task_clone = task.clone();
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
            var task = new Task();
            assertFalse(task.validate());
            assertEquals(2, task.countValidationErrors());
            ValidationError error = null;
            var it = task.getValidationErrors().iterator();
            error = it.next();
            assertEquals(error.getIdentifier(), "mandatory");
            assertEquals(error.getSubject(), "type");
            error = it.next();
            assertEquals(error.getIdentifier(), "mandatory");
            assertEquals(error.getSubject(), "planned");

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(System.currentTimeMillis() + 2000);
            task.setFrequency(Frequency.MINUTELY);
            task.setBusy(false);
            task.resetValidation();
            assertTrue(task.validate());

            var cal = Calendar.getInstance();
            cal.set(1970, Calendar.NOVEMBER, 25);
            task.setPlanned(cal.getTime());
            task.resetValidation();
            assertFalse(task.validate());
            assertEquals(1, task.countValidationErrors());
            var set = task.getValidationErrors();
            error = set.iterator().next();
            assertEquals(error.getIdentifier(), "invalid");
            assertEquals(error.getSubject(), "planned");
            task.setPlanned(System.currentTimeMillis() + 2000);

            task.setFrequency(null);
            task.resetValidation();
            assertTrue(task.validate());
            task.setFrequency(Frequency.MINUTELY);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
