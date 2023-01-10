/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers;

import org.junit.jupiter.api.Test;
import rife.scheduler.*;
import rife.scheduler.exceptions.FrequencyException;
import rife.scheduler.exceptions.NoExecutorForTasktypeException;
import rife.scheduler.exceptions.SchedulerException;
import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.exceptions.UnableToRetrieveTasksToProcessException;
import rife.tools.ExceptionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestMemoryScheduler {
    @Test
    void testInstantiateScheduler() {
        var scheduler = new MemoryScheduler().getScheduler();
        assertNotNull(scheduler);
    }

    @Test
    void testStartStopScheduler() {
        var scheduler = new MemoryScheduler().getScheduler();
        try {
            scheduler.start();
            synchronized (scheduler) {
                scheduler.stop();

                try {
                    scheduler.wait();
                } catch (InterruptedException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        } catch (NoExecutorForTasktypeException | UnableToRetrieveTasksToProcessException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testAddExecutor() {
        var scheduler = new MemoryScheduler().getScheduler();
        Executor executor = new TestExecutor();

        assertNull(scheduler.getExecutor(executor.getHandledTaskType()));
        try {
            scheduler.addExecutor(executor);
        } catch (SchedulerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        assertEquals(executor, scheduler.getExecutor(executor.getHandledTaskType()));
        assertTrue(scheduler.removeExecutor(executor));
    }

    @Test
    void testOneshotTaskExecution() {
        var sleep_time = 2 * 1000;
        var scheduler = new MemoryScheduler().getScheduler();
        var executor = new TestExecutor();
        var taskmanager = scheduler.getTaskManager();
        var task = executor.createTask();

        try {
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
                scheduler.stop();

                try {
                    scheduler.wait();
                } catch (InterruptedException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }

            var executed_tasks = executor.getExecutedTasks();
            assertEquals(1, executed_tasks.size());
            var executed_task = executed_tasks.iterator().next();
            assertEquals(task, executed_task);
            assertSame(executed_task.getTaskManager(), taskmanager);
        } catch (NoExecutorForTasktypeException | UnableToRetrieveTasksToProcessException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testRepeatingTaskExecution() {
        var scheduler_sleep_time = 10 * 1000;                // 10 seconds
        var task_frequency = 20 * 1000;                      // 20 seconds
        var thread_sleep_time = scheduler_sleep_time * 3;    // 30 seconds
        var scheduler = new MemoryScheduler().getScheduler();
        var executor = new TestExecutor();
        var taskmanager = scheduler.getTaskManager();
        var task = executor.createTask();

        try {
            // set back a while in the past to test the catch-up rescheduling
            task.setPlanned(System.currentTimeMillis() - (scheduler_sleep_time * 10));
            task.setFrequency(Frequency.MINUTELY);
            task.setBusy(false);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        scheduler.setSleepTime(scheduler_sleep_time);

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
        var executed_tasks_size = -1;
        try {
            scheduler.start();
            try {
                Thread.sleep(thread_sleep_time);
                executed_tasks = executor.getExecutedTasks();
                executed_tasks_size = executed_tasks.size();
            } catch (InterruptedException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            synchronized (scheduler) {
                scheduler.stop();

                try {
                    scheduler.wait();
                } catch (InterruptedException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }

            // task frequency fits in the thread sleep time
            long number_of_executions = (thread_sleep_time / task_frequency) + 1;

//			System.out.println("\nMemory\n"+executor.getFirstExecution().getTime().getTime()+" : "+executor.getFirstExecution().getTime().toGMTString()+"\n"+now.getTime()+" : "+now.toGMTString()+"\ntask_frequency = "+task_frequency+"\nnumber_of_executions = "+number_of_executions+"\nexecuted_tasks_size = "+executed_tasks_size);
            var now = new Date();
            assertTrue(number_of_executions == executed_tasks_size || number_of_executions == executed_tasks_size + 1, "\nFAILED Memory\n" + executor.getFirstExecution().getTime().getTime() + " : " + executor.getFirstExecution().getTime().toGMTString() + "\n" + now.getTime() + " : " + now.toGMTString() + "\ntask_frequency = " + task_frequency + "\nnumber_of_executions = " + number_of_executions + "\nexecuted_tasks_size = " + executed_tasks_size);
            for (var executed_task : executed_tasks) {
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
        }
    }

    static class TestExecutor extends Executor {
        private Calendar firstExecution_ = null;
        private ArrayList<Task> executedTasks_ = null;

        public TestExecutor() {
            executedTasks_ = new ArrayList<Task>();
        }

        public boolean executeTask(Task task) {
            synchronized (this) {
                if (null == firstExecution_) {
                    firstExecution_ = Calendar.getInstance();
                    firstExecution_.setTimeInMillis(System.currentTimeMillis());
                }
                executedTasks_.add(task);
            }

            return true;
        }

        public Collection<Task> getExecutedTasks() {
            synchronized (this) {
                return executedTasks_;
            }
        }

        public Calendar getFirstExecution() {
            synchronized (this) {
                return firstExecution_;
            }
        }

        public String getHandledTaskType() {
            return TestTasktypes.UPLOAD_GROUPS;
        }

        protected long getRescheduleDelay() {
            return 100;
        }
    }
}
