/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.scheduler.Task;
import rife.scheduler.TaskManager;
import rife.scheduler.Taskoption;
import rife.scheduler.TaskoptionManager;
import rife.scheduler.TestTasktypes;
import rife.scheduler.exceptions.SchedulerException;
import rife.scheduler.exceptions.SchedulerManagerException;
import rife.scheduler.exceptions.TaskoptionManagerException;
import rife.scheduler.schedulermanagers.DatabaseScheduler;
import rife.scheduler.schedulermanagers.DatabaseSchedulerFactory;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;
import rife.tools.ExceptionUtils;

import java.util.Calendar;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseTaskoptions {
    public void setUp(Datasource datasource) {
        DatabaseScheduler manager = DatabaseSchedulerFactory.getInstance(datasource);

        try {
            assertTrue(manager.install());
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    public void tearDown(Datasource datasource) {
        DatabaseScheduler manager = DatabaseSchedulerFactory.getInstance(datasource);

        try {
            assertTrue(manager.remove());
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstantiateTaskoptionManager(Datasource datasource) {
        setUp(datasource);
        try {
            TaskoptionManager manager = DatabaseTaskoptionsFactory.getInstance(datasource);
            assertNotNull(manager);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testAddTaskoptionWithInexistentTaskId(Datasource datasource) {
        setUp(datasource);
        try {
            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(0);
            taskoption.setName("name");
            taskoption.setValue("value");

            TaskoptionManager manager = DatabaseTaskoptionsFactory.getInstance(datasource);
            try {
                manager.addTaskoption(taskoption);
                fail();
            } catch (InexistentTaskIdException e) {
                assertTrue(true);
            } catch (TaskoptionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testAddTaskoption(Datasource datasource) {
        setUp(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskoptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);
            assertTrue(task_id >= 0);

            String taskoption_name = "name";
            String value = "value";

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskoption(taskoption));
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testAddDuplicateTaskoption(Datasource datasource) {
        setUp(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskoptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);
            assertTrue(task_id >= 0);

            String taskoption_name = "name";
            String value = "value";

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskoption(taskoption));

            taskoption_manager.addTaskoption(taskoption);
            fail();
        } catch (SchedulerException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetTaskoption(Datasource datasource) {
        setUp(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskoptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);
            assertTrue(task_id >= 0);

            String taskoption_name = "name";
            String value = "value";

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskoption(taskoption));

            taskoption = taskoption_manager.getTaskoption(task_id, taskoption_name);
            assertNotNull(taskoption);

            assertEquals(taskoption.getTaskId(), task_id);
            assertEquals(taskoption.getName(), taskoption_name);
            assertEquals(taskoption.getValue(), value);

            task = task_manager.getTask(task_id);
            assertEquals(task.getTaskoptionValue(taskoption_name), value);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUpdateTaskoption(Datasource datasource) {
        setUp(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskoptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);
            assertTrue(task_id >= 0);

            String taskoption_name = "name";
            String value = "value";

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskoption(taskoption));

            value = "new_value";

            taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.updateTaskoption(taskoption));

            taskoption = taskoption_manager.getTaskoption(task_id, taskoption_name);
            assertNotNull(taskoption);

            assertEquals(taskoption.getTaskId(), task_id);
            assertEquals(taskoption.getName(), taskoption_name);
            assertEquals(taskoption.getValue(), value);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetTaskoptions(Datasource datasource) {
        setUp(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskoptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);
            assertTrue(task_id >= 0);

            String taskoption_name = "name";
            String value = "some_value";

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskoption(taskoption));

            Collection<Taskoption> taskoptions = taskoption_manager.getTaskoptions(task_id);
            assertEquals(1, taskoptions.size());

            taskoption = taskoptions.iterator().next();
            assertEquals(taskoption.getTaskId(), task_id);
            assertEquals(taskoption.getName(), taskoption_name);
            assertEquals(taskoption.getValue(), "some_value");
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRemoveTaskoption(Datasource datasource) {
        setUp(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskoptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(cal.getTime());
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);
            assertTrue(task_id >= 0);

            String taskoption_name = "name";
            String value = "value";

            Taskoption taskoption = new Taskoption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskoption(taskoption));

            assertTrue(taskoption_manager.removeTaskoption(task_id, taskoption_name));

            assertTrue(task_manager.removeTask(task_id));
            task_id = 0;
            taskoption_name = null;
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetNonExistingTaskoption(Datasource datasource) {
        setUp(datasource);

        TaskoptionManager manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
        int task_nonexisting_id = 340;
        try {
            assertNull(manager.getTaskoption(task_nonexisting_id, "unknownname"));
        } catch (TaskoptionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRemoveNonExistingTaskoption(Datasource datasource) {
        setUp(datasource);

        try {
            TaskoptionManager manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskoptionManager();
            int task_nonexisting_id = 120;
            try {
                assertFalse(manager.removeTaskoption(task_nonexisting_id, "unknownname"));
            } catch (TaskoptionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        } finally {
            tearDown(datasource);
        }
    }
}
