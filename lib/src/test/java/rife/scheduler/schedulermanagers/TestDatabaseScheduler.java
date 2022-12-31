/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.TestDatasources;
import rife.scheduler.exceptions.*;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.scheduler.Executor;
import rife.scheduler.Scheduler;
import rife.scheduler.Task;
import rife.scheduler.TaskManager;
import rife.scheduler.TestTasktypes;
import rife.tools.ExceptionUtils;
import rife.tools.Localization;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseScheduler {
    public void setup(Datasource datasource) {
        DatabaseScheduler scheduler_manager = DatabaseSchedulerFactory.instance(datasource);
        try {
            scheduler_manager.install();
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    public void tearDown(Datasource datasource) {
        DatabaseScheduler scheduler_manager = DatabaseSchedulerFactory.instance(datasource);
        try {
            scheduler_manager.remove();
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiateScheduler(Datasource datasource) {
        setup(datasource);

        try {
            Scheduler scheduler = DatabaseSchedulerFactory.instance(datasource).getScheduler();
            assertNotNull(scheduler);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStartStopScheduler(Datasource datasource) {
        setup(datasource);

        Scheduler scheduler = DatabaseSchedulerFactory.instance(datasource).getScheduler();
        try {
            scheduler.start();
            synchronized (scheduler) {
                scheduler.interrupt();

                try {
                    scheduler.wait();
                } catch (InterruptedException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        } catch (NoExecutorForTasktypeException | UnableToRetrieveTasksToProcessException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAddExecutor(Datasource datasource) {
        setup(datasource);
        try {
            Scheduler scheduler = DatabaseSchedulerFactory.instance(datasource).getScheduler();
            Executor executor = new TestExecutor();

            assertNull(scheduler.getExecutor(executor.getHandledTasktype()));
            try {
                scheduler.addExecutor(executor);
            } catch (SchedulerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            assertEquals(executor, scheduler.getExecutor(executor.getHandledTasktype()));
            assertTrue(scheduler.removeExecutor(executor));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testOneshotTaskExecution(Datasource datasource) {
        setup(datasource);

        int sleep_time = 60 * 1000;
        Scheduler scheduler = DatabaseSchedulerFactory.instance(datasource).getScheduler();
        TestExecutor executor = new TestExecutor();
        TaskManager taskmanager = scheduler.getTaskManager();
        Task task = new Task();

        try {
            task.setType(TestTasktypes.UPLOAD_GROUPS);
            task.setPlanned(System.currentTimeMillis());
            task.setFrequency(null);
            task.setBusy(false);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            scheduler.addExecutor(executor);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        scheduler.setSleepTime(sleep_time);
        try {
            task.setId(taskmanager.addTask(task));
            task = taskmanager.getTask(task.getId());
        } catch (TaskManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            scheduler.start();
            try {
                Thread.sleep(sleep_time * 2);
            } catch (InterruptedException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            synchronized (scheduler) {
                scheduler.interrupt();

                try {
                    scheduler.wait();
                } catch (InterruptedException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }

            Collection<Task> executed_tasks = executor.getExecutedTasks();
            assertEquals(1, executed_tasks.size());
            Task executed_task = executed_tasks.iterator().next();
            assertEquals(task, executed_task);
            assertSame(executed_task.getTaskManager(), taskmanager);
        } catch (NoExecutorForTasktypeException | UnableToRetrieveTasksToProcessException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRepeatingTaskExecution(Datasource datasource) {
        setup(datasource);

        int scheduler_sleeptime = 30 * 1000;                // 30 seconds
        int task_frequency = 60 * 1000;                    // 1 minute
        int thread_sleeptime = scheduler_sleeptime * 6;    // 3 minutes
        Scheduler scheduler = DatabaseSchedulerFactory.instance(datasource).getScheduler();
        TestExecutor executor = new TestExecutor();
        TaskManager taskmanager = scheduler.getTaskManager();
        Task task = new Task();

        try {
            task.setType(TestTasktypes.UPLOAD_GROUPS);
            // set back a while in the past to test the catch up rescheduling
            task.setPlanned(System.currentTimeMillis() - (scheduler_sleeptime * 10));
            task.setFrequency("* * * * *");
            task.setBusy(false);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        scheduler.setSleepTime(scheduler_sleeptime);

        try {
            scheduler.addExecutor(executor);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            task.setId(taskmanager.addTask(task));
            task = taskmanager.getTask(task.getId());
        } catch (TaskManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        Collection<Task> executed_tasks = null;
        int executed_tasks_size = -1;
        try {
            scheduler.start();
            try {
                Thread.sleep(thread_sleeptime);
                executed_tasks = executor.getExecutedTasks();
                executed_tasks_size = executed_tasks.size();
            } catch (InterruptedException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            synchronized (scheduler) {
                scheduler.interrupt();

                try {
                    scheduler.wait();
                } catch (InterruptedException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }

            // task frequency fits in the thread sleep time
            long number_of_executions = (thread_sleeptime / task_frequency) + 1;

//			System.out.println("\n"+mDatasource.getDriver()+"\n"+executor.getFirstExecution().getTime().getTime()+" : "+executor.getFirstExecution().getTime().toGMTString()+"\n"+now.getTime()+" : "+now.toGMTString()+"\ntask_frequency = "+task_frequency+"\nnumber_of_executions = "+number_of_executions+"\nexecuted_tasks_size = "+executed_tasks_size);
            Date now = new Date();
            assertTrue(number_of_executions == executed_tasks_size || number_of_executions == executed_tasks_size + 1, "\nFAILED " + datasource.getDriver() + " \n" + executor.getFirstExecution().getTime().getTime() + " : " + executor.getFirstExecution().getTime().toGMTString() + "\n" + now.getTime() + " : " + now.toGMTString() + "\ntask_frequency = " + task_frequency + "\nnumber_of_executions = " + number_of_executions + "\nexecuted_tasks_size = " + executed_tasks_size);
            for (Task executed_task : executed_tasks) {
                assertEquals(task.getId(), executed_task.getId());
                assertEquals(task.getType(), executed_task.getType());
                assertEquals(task.getFrequency(), executed_task.getFrequency());
                assertTrue(task.getPlanned() <= executed_task.getPlanned());
                assertSame(executed_task.getTaskManager(), taskmanager);
            }

            try {
                taskmanager.removeTask(task.getId());
            } catch (TaskManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        } catch (NoExecutorForTasktypeException | UnableToRetrieveTasksToProcessException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    static class TestExecutor extends Executor {
        private Calendar mFirstExecution = null;
        private ArrayList<Task> mExecutedTasks = null;

        public TestExecutor() {
            mExecutedTasks = new ArrayList<Task>();
        }

        public boolean executeTask(Task task) {
            synchronized (this) {
                if (null == mFirstExecution) {
                    mFirstExecution = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
                    mFirstExecution.setTimeInMillis(System.currentTimeMillis());
                }
                mExecutedTasks.add(task);
            }

            return true;
        }

        public Collection<Task> getExecutedTasks() {
            synchronized (this) {
                return mExecutedTasks;
            }
        }

        public Calendar getFirstExecution() {
            synchronized (this) {
                return mFirstExecution;
            }
        }

        public String getHandledTasktype() {
            return TestTasktypes.UPLOAD_GROUPS;
        }

        protected long getRescheduleDelay() {
            return 100;
        }
    }
}
