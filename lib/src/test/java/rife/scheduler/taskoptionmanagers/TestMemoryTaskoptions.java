/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers;

import org.junit.jupiter.api.Test;
import rife.scheduler.*;

import rife.scheduler.exceptions.SchedulerException;
import rife.scheduler.exceptions.TaskoptionManagerException;
import rife.scheduler.taskmanagers.MemoryTasks;
import rife.scheduler.taskoptionmanagers.exceptions.DuplicateTaskoptionException;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;
import rife.tools.ExceptionUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestMemoryTaskoptions {
    @Test
    public void testInstantiateTaskoptionManager() {
        TaskoptionManager manager = new MemoryTaskoptions();
        assertNotNull(manager);
    }

    @Test
    public void testAddTaskoptionWithInexistentTaskId() {
        Taskoption taskoption = new Taskoption();
        taskoption.setTaskId(0);
        taskoption.setName("name");
        taskoption.setValue("value");

        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskoptionManager manager = scheduler.getTaskoptionManager();
        try {
            manager.addTaskoption(taskoption);
            fail();
        } catch (InexistentTaskIdException e) {
            assertTrue(true);
        } catch (TaskoptionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testAddTaskoption() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id = 0;
        Task task = new Task();
        String taskoption_name = "name";
        String taskoption_value = "value";

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(taskoption_value);

            assertTrue(taskoption_manager.addTaskoption(taskoption));
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testAddDuplicateTaskoption() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id = 0;
        Task task = new Task();
        String taskoption_name = "name";
        String taskoption_value = "value";

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);

            Taskoption taskoption1 = new Taskoption();
            taskoption1.setTaskId(task_id);
            taskoption1.setName(taskoption_name);
            taskoption1.setValue(taskoption_value);

            assertTrue(taskoption_manager.addTaskoption(taskoption1));
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            Taskoption taskoption2 = new Taskoption();
            taskoption2.setTaskId(task_id);
            taskoption2.setName(taskoption_name);
            taskoption2.setValue(taskoption_value);

            taskoption_manager.addTaskoption(taskoption2);
            fail();
        } catch (DuplicateTaskoptionException e) {
            assertTrue(true);
        } catch (TaskoptionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetTaskoption() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id = 0;
        Task task = new Task();
        String taskoption_name = "name";
        String taskoption_value = "value";

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);

            Taskoption taskoption1 = new Taskoption();
            taskoption1.setTaskId(task_id);
            taskoption1.setName(taskoption_name);
            taskoption1.setValue(taskoption_value);

            assertTrue(taskoption_manager.addTaskoption(taskoption1));

            task = task_manager.getTask(task_id);
            assertEquals(task.getTaskoptionValue(taskoption_name), taskoption_value);

            Taskoption taskoption = taskoption_manager.getTaskoption(task_id, taskoption_name);
            assertNotNull(taskoption);

            assertEquals(taskoption.getTaskId(), task_id);
            assertEquals(taskoption.getName(), taskoption_name);
            assertEquals(taskoption.getValue(), taskoption_value);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testUpdateTaskoption() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id = 0;
        Task task = new Task();
        String taskoption_name = "name";
        String taskoption_value = "value";

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);

            Taskoption taskoption1 = new Taskoption();
            taskoption1.setTaskId(task_id);
            taskoption1.setName(taskoption_name);
            taskoption1.setValue(taskoption_value);

            assertTrue(taskoption_manager.addTaskoption(taskoption1));

            taskoption_value = "new_taskoption_value";

            Taskoption taskoption2 = new Taskoption();
            taskoption2.setTaskId(task_id);
            taskoption2.setName(taskoption_name);
            taskoption2.setValue(taskoption_value);

            assertTrue(taskoption_manager.updateTaskoption(taskoption2));

            taskoption2 = taskoption_manager.getTaskoption(task_id, taskoption_name);
            assertNotNull(taskoption2);

            assertEquals(taskoption2.getTaskId(), task_id);
            assertEquals(taskoption2.getName(), taskoption_name);
            assertEquals(taskoption2.getValue(), taskoption_value);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetTaskoptions() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id1 = -1;
        int task_id2 = -1;
        Task task = new Task();
        String taskoption_name = "name";
        String taskoption_name2 = "name2";
        String taskoption_value = "value";

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id1 = task_manager.addTask(task);

            Taskoption taskoption1 = new Taskoption();
            taskoption1.setTaskId(task_id1);
            taskoption1.setName(taskoption_name);
            taskoption1.setValue(taskoption_value);

            assertTrue(taskoption_manager.addTaskoption(taskoption1));

            Taskoption taskoption2 = new Taskoption();
            taskoption2.setTaskId(task_id1);
            taskoption2.setName(taskoption_name2);
            taskoption2.setValue(taskoption_value);

            assertTrue(taskoption_manager.addTaskoption(taskoption2));

            task_id2 = task_manager.addTask(task);

            taskoption1.setTaskId(task_id2);

            assertTrue(taskoption_manager.addTaskoption(taskoption1));

            Collection<Taskoption> taskoptions = taskoption_manager.getTaskoptions(task_id1);
            assertEquals(2, taskoptions.size());

            Iterator<Taskoption> taskoptions_it = taskoptions.iterator();

            assertTrue(taskoptions_it.hasNext());

            Taskoption taskoption = taskoptions_it.next();
            assertEquals(taskoption.getTaskId(), task_id1);
            assertEquals(taskoption.getName(), taskoption_name);
            assertEquals(taskoption.getValue(), "value");

            assertTrue(taskoptions_it.hasNext());

            taskoption = taskoptions_it.next();
            assertEquals(taskoption.getTaskId(), task_id1);
            assertEquals(taskoption.getName(), taskoption_name2);
            assertEquals(taskoption.getValue(), "value");

            assertFalse(taskoptions_it.hasNext());
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveTaskoption() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id = 0;
        Task task = new Task();
        String taskoption_name = "name";
        String taskoption_value = "value";

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);

            Taskoption taskoption1 = new Taskoption();
            taskoption1.setTaskId(task_id);
            taskoption1.setName(taskoption_name);
            taskoption1.setValue(taskoption_value);

            assertTrue(taskoption_manager.addTaskoption(taskoption1));

            assertTrue(taskoption_manager.removeTaskoption(task_id, taskoption_name));
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetNonExistingTaskoption() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id = 0;
        Task task = new Task();

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);

            assertNull(taskoption_manager.getTaskoption(task_id, "name"));
            assertNull(taskoption_manager.getTaskoption(task_id + 1, "name"));
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveNonExistingTaskoption() {
        Scheduler scheduler = new Scheduler(new MemoryTasks(), new MemoryTaskoptions());
        TaskManager task_manager = scheduler.getTaskManager();
        TaskoptionManager taskoption_manager = scheduler.getTaskoptionManager();
        int task_id = 0;
        Task task = new Task();

        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);

            assertFalse(taskoption_manager.removeTaskoption(task_id, "name"));
            assertFalse(taskoption_manager.removeTaskoption(task_id + 1, "name"));
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
