/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.scheduler.Task;
import rife.scheduler.TaskManager;
import rife.scheduler.TaskOption;
import rife.scheduler.TaskOptionManager;
import rife.scheduler.TestTasktypes;
import rife.scheduler.exceptions.SchedulerException;
import rife.scheduler.exceptions.SchedulerManagerException;
import rife.scheduler.exceptions.TaskOptionManagerException;
import rife.scheduler.schedulermanagers.DatabaseScheduler;
import rife.scheduler.schedulermanagers.DatabaseSchedulerFactory;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;
import rife.tools.ExceptionUtils;

import java.util.Calendar;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseTaskOptions {
    public void setup(Datasource datasource) {
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
    public void testInstantiateTaskOptionManager(Datasource datasource) {
        setup(datasource);
        try {
            TaskOptionManager manager = DatabaseTaskOptionsFactory.getInstance(datasource);
            assertNotNull(manager);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testAddTaskOptionWithInexistentTaskId(Datasource datasource) {
        setup(datasource);
        try {
            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(0);
            taskoption.setName("name");
            taskoption.setValue("value");

            TaskOptionManager manager = DatabaseTaskOptionsFactory.getInstance(datasource);
            try {
                manager.addTaskOption(taskoption);
                fail();
            } catch (InexistentTaskIdException e) {
                assertTrue(true);
            } catch (TaskOptionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testAddTaskOption(Datasource datasource) {
        setup(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskOptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
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

            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskOption(taskoption));
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testAddDuplicateTaskOption(Datasource datasource) {
        setup(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskOptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
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

            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskOption(taskoption));

            taskoption_manager.addTaskOption(taskoption);
            fail();
        } catch (SchedulerException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetTaskOption(Datasource datasource) {
        setup(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskOptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2001, Calendar.NOVEMBER, 24, 0, 0, 0);

            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setFrequency("* * * * *");

            task_id = task_manager.addTask(task);
            assertTrue(task_id >= 0);

            String taskoption_name = "name";
            String value = "value";

            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskOption(taskoption));

            taskoption = taskoption_manager.getTaskOption(task_id, taskoption_name);
            assertNotNull(taskoption);

            assertEquals(taskoption.getTaskId(), task_id);
            assertEquals(taskoption.getName(), taskoption_name);
            assertEquals(taskoption.getValue(), value);

            task = task_manager.getTask(task_id);
            assertEquals(task.getTaskOptionValue(taskoption_name), value);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUpdateTaskOption(Datasource datasource) {
        setup(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskOptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
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

            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskOption(taskoption));

            value = "new_value";

            taskoption = new TaskOption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.updateTaskOption(taskoption));

            taskoption = taskoption_manager.getTaskOption(task_id, taskoption_name);
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
    public void testGetTaskOptions(Datasource datasource) {
        setup(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskOptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
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

            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskOption(taskoption));

            Collection<TaskOption> taskoptions = taskoption_manager.getTaskOptions(task_id);
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
    public void testRemoveTaskOption(Datasource datasource) {
        setup(datasource);

        int task_id = 0;
        Task task = new Task();
        TaskManager task_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskManager();
        TaskOptionManager taskoption_manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
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

            TaskOption taskoption = new TaskOption();
            taskoption.setTaskId(task_id);
            taskoption.setName(taskoption_name);
            taskoption.setValue(value);

            assertTrue(taskoption_manager.addTaskOption(taskoption));

            assertTrue(taskoption_manager.removeTaskOption(task_id, taskoption_name));

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
    public void testGetNonExistingTaskOption(Datasource datasource) {
        setup(datasource);

        TaskOptionManager manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
        int task_nonexisting_id = 340;
        try {
            assertNull(manager.getTaskOption(task_nonexisting_id, "unknownname"));
        } catch (TaskOptionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRemoveNonExistingTaskOption(Datasource datasource) {
        setup(datasource);

        try {
            TaskOptionManager manager = DatabaseSchedulerFactory.getInstance(datasource).getScheduler().getTaskOptionManager();
            int task_nonexisting_id = 120;
            try {
                assertFalse(manager.removeTaskOption(task_nonexisting_id, "unknownname"));
            } catch (TaskOptionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        } finally {
            tearDown(datasource);
        }
    }
}
