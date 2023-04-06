/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.config.RifeConfig;
import rife.scheduler.exceptions.FrequencyException;
import rife.scheduler.exceptions.SchedulerException;
import rife.validation.*;

import java.util.Calendar;
import java.util.Date;

/**
 * A task contains all the information for the scheduler to plan its execution.
 * <p>
 * For a task to be valid, it needs to have a type and either the one-shat planned execution
 * needs to be specified, or the repeating frequency.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Task extends MetaData implements Cloneable {
    private int id_ = -1;
    private String type_ = null;
    private long planned_ = 0;
    private Frequency frequency_ = null;
    private boolean busy_ = false;

    private TaskManager taskManager_ = null;

    /**
     * Create a new task.
     *
     * @since 1.0
     */
    public Task() {
    }

    public void activateMetaData() {
        addRule(new ValidationRuleNotNull("type"));
        addRule(new ValidationRuleNotEmpty("planned"));
        addRule(new InvalidPlanned());
    }

    /**
     * Sets this task's task manager.
     * <p>
     * This is not intended to be used by the user and will be set by RIFE2
     * when processing tasks.
     *
     * @param taskManager the task manager to set
     * @since 1.0
     */
    public void setTaskManager(TaskManager taskManager) {
        taskManager_ = taskManager;
    }

    /**
     * Retrieve this task's task manager.
     *
     * @return this task's task manager; or
     * {@code null} if no task manager has been assigned to this task
     * @since 1.0
     */
    public TaskManager getTaskManager() {
        return taskManager_;
    }

    /**
     * Retrieve the value of a named task option for this task.
     * <p>
     * This method relies on RIFE2 having set the task manager first,
     * without that, the option value will always be {@code null}.
     *
     * @param name the name of the task option to retrieve
     * @return the value for the named option; or {@code null} if the option
     * wasn't available or couldn't be found
     * @throws SchedulerException when an error occurs during the retrieval
     *                            of the task option
     * @since 1.0
     */
    public String getTaskOptionValue(String name)
    throws SchedulerException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        if (null == taskManager_) {
            return null;
        }

        var scheduler = taskManager_.getScheduler();
        if (null == scheduler) {
            return null;
        }

        var task_option_manager = scheduler.getTaskOptionManager();
        if (null == task_option_manager) {
            return null;
        }

        var taskoption = task_option_manager.getTaskOption(getId(), name);
        if (null == taskoption) {
            return null;
        }

        return taskoption.getValue();
    }

    /**
     * Calculate the next timestamp in milliseconds since epoch at which this
     * task should execute.
     *
     * @return the next execution timestamp; or
     * {@code -1} if there's no next scheduled time
     * @throws FrequencyException when an error occurred during the timestamp calculation
     * @since 1.0
     */
    public long getNextTimestamp()
    throws FrequencyException {
        // lower towards the minute, remove seconds and milliseconds
        var current_calendar = RifeConfig.tools().getCalendarInstance();
        current_calendar.set(Calendar.SECOND, 0);
        current_calendar.set(Calendar.MILLISECOND, 0);
        var current_time = current_calendar.getTimeInMillis();
        if (planned_ <= current_time) {
            return getNextTimestamp(current_time);
        }

        return -1;
    }

    /**
     * Calculate the next timestamp in milliseconds since epoch at which this
     * task should execute, starting from a provided timestamp.
     *
     * @param start the starting timestamp in milliseconds since epoch to look
     *              for the next execution timestamp
     * @return the next execution timestamp; or
     * {@code -1} if there's no next scheduled time
     * @throws FrequencyException when an error occurred during the timestamp calculation
     * @since 1.0
     */
    public long getNextTimestamp(long start)
    throws FrequencyException {
        if (null == frequency_) {
            return -1;
        } else {
            return frequency_.getNextTimestamp(start);
        }
    }

    /**
     * Sets the type of the task, which will be used to execute
     * this task with the {@link Executor} that handles the same task type.
     *
     * @param type this task's type
     * @since 1.0
     */
    public void setType(String type) {
        type_ = type;
    }

    /**
     * Sets the type of the task, which will be used to execute
     * this task with the {@link Executor} that handles the same task type.
     *
     * @param type this task's type
     * @return this task instance
     * @since 1.0
     */
    public Task type(String type) {
        setType(type);
        return this;
    }

    /**
     * Retrieves the type of this task
     *
     * @return this task's type; or
     * {@code null} if no type was set
     */
    public String getType() {
        return type_;
    }

    /**
     * Sets the planned timestamp at which this task should execute next.
     * <p>
     * This can be set manually for one-shot task execution, or when providing
     * a frequency, RIFE2 will automatically set the planned timestamp based
     * on the frequency specification.
     *
     * @param planned the date instance for one-shot task execution
     * @since 1.0
     */
    public void setPlanned(Date planned) {
        setPlanned(planned.getTime());
    }

    /**
     * Sets the planned timestamp at which this task should execute next.
     * <p>
     * This can be set manually for one-shot task execution, or when providing
     * a frequency, RIFE2 will automatically set the planned timestamp based
     * on the frequency specification.
     *
     * @param planned the timestamp in milliseconds since epoch for one-shot
     *                task execution
     * @since 1.0
     */
    public void setPlanned(long planned) {
        // lower towards the minute, remove seconds and milliseconds
        var planned_calendar = RifeConfig.tools().getCalendarInstance();
        planned_calendar.setTimeInMillis(planned);
        planned_calendar.set(Calendar.SECOND, 0);
        planned_calendar.set(Calendar.MILLISECOND, 0);

        planned_ = planned_calendar.getTimeInMillis();
    }

    /**
     * Sets the planned timestamp at which this task should execute next.
     * <p>
     * This can be set manually for one-shot task execution, or when providing
     * a frequency, RIFE2 will automatically set the planned timestamp based
     * on the frequency specification.
     *
     * @param planned the date instance for one-shot task execution
     * @return this task instance
     * @since 1.0
     */
    public Task planned(Date planned) {
        setPlanned(planned);
        return this;
    }

    /**
     * Sets the planned timestamp at which this task should execute next.
     *
     * @param planned the timestamp in milliseconds since epoch for one-shot
     *                task execution
     * @return this task instance
     * @since 1.0
     */
    public Task planned(long planned) {
        setPlanned(planned);
        return this;
    }

    /**
     * Retrieve the timestamp at which this task is scheduled for the next
     * execution.
     *
     * @return the next execution timestamp in milliseconds since epoch
     * @since 1.0
     */
    public long getPlanned() {
        return planned_;
    }

    /**
     * Sets the frequency at which this task should execute repeatedly.
     *
     * @param frequency this task's frequency
     * @since 1.0
     */
    public void setFrequency(Frequency frequency) {
        frequency_ = frequency;
    }

    /**
     * Sets the frequency at which this task should execute repeatedly.
     *
     * @param frequency this task's frequency
     * @return this task instance
     * @since 1.0
     */
    public Task frequency(Frequency frequency) {
        setFrequency(frequency);
        return this;
    }

    /**
     * Retrieves the frequency at which this task should execute repeatedly.
     *
     * @return this task's frequency; or
     * {@code null} if the frequency hasn't been set
     * @since 1.0
     */
    public Frequency getFrequency() {
        return frequency_;
    }

    /**
     * Sets the frequency crontab-like specification at which this task should
     * execute repeatedly.
     *
     * @param specification the frequency specification
     * @throws FrequencyException when an error occurred during the parsing
     *                            of the frequency specification
     * @since 1.0
     */
    public void setFrequencySpecification(String specification)
    throws FrequencyException {
        if (specification == null) {
            frequency_ = null;
        } else {
            setFrequency(new Frequency(specification));
        }
    }

    /**
     * Sets the frequency crontab-like specification at which this task should
     * execute repeatedly.
     *
     * @param specification the frequency specification
     * @return this task instance
     * @throws FrequencyException when an error occurred during the parsing
     *                            of the frequency specification
     * @since 1.0
     */
    public Task frequencySpecification(String specification)
    throws FrequencyException {
        setFrequencySpecification(specification);
        return this;
    }

    /**
     * Retrieves the frequency specification at which this task should execute
     * repeatedly.
     *
     * @return this task's frequency specification; or
     * {@code null} if the frequency hasn't been set
     * @since 1.0
     */
    public String getFrequencySpecification() {
        if (frequency_ == null) {
            return null;
        }
        return frequency_.toString();
    }

    /**
     * Create a new task option for this task.
     * <p>
     * Make sure to first add this task to a task manager or scheduler so that
     * it receives its unique ID. Without this, the task option will not be
     * properly associated with the task.
     *
     * @return the newly created task option.
     * @since 1.0
     */
    public TaskOption createTaskOption() {
        return new TaskOption().taskId(id_);
    }

    /**
     * Sets the unique ID of this task.
     * <p>
     * This is intended to be used internally by RIFE2.
     *
     * @param id the unique ID of this task.
     * @since 1.0
     */
    public void setId(int id) {
        id_ = id;
    }

    /**
     * Retrieves the unique ID of this task.
     *
     * @return this task's unique ID; or
     * {@code -1} if the ID hasn't been set yet
     * @since 1.0
     */
    public int getId() {
        return id_;
    }

    /**
     * Sets whether the task is currently busy being processed by an {@link Executor}.
     * <p>
     * This is intended to be used internally by RIFE2.
     *
     * @param busy {@code true} if the task is busy; or
     *             {@code false} otherwise
     * @since 1.0
     */
    public void setBusy(boolean busy) {
        busy_ = busy;
    }

    /**
     * Indicates whether this task is currently being processed by an {@link Executor}.
     *
     * @return {@code true} if the task is busy; or
     * {@code false} otherwise
     * @since 1.0
     */
    public boolean isBusy() {
        return busy_;
    }

    public Task clone()
    throws CloneNotSupportedException {
        return (Task) super.clone();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }

        var other_task = (Task) object;

        if (other_task.getId() == getId() &&
            other_task.getType().equals(getType()) &&
            other_task.getPlanned() == getPlanned() &&
            ((null == other_task.getFrequency() && null == getFrequency()) ||
             (other_task.getFrequency() != null && other_task.getFrequency().equals(getFrequency()))) &&
            other_task.getTaskManager() == getTaskManager()) {
            return true;
        }

        return false;
    }

    public class InvalidPlanned implements ValidationRule {
        public boolean validate() {
            if (0 == planned_) {
                return true;
            }

            var current_calendar = RifeConfig.tools().getCalendarInstance();
            current_calendar.set(Calendar.SECOND, 0);
            current_calendar.set(Calendar.MILLISECOND, 0);
            return planned_ >= current_calendar.getTimeInMillis();
        }

        public String getSubject() {
            return "planned";
        }

        public ValidationError getError() {
            return new ValidationError.INVALID(getSubject());
        }

        public Object getBean() {
            return null;
        }

        public <T extends ValidationRule> T setBean(Object bean) {
            return (T) this;
        }

        public Object clone() {
            return this;
        }
    }
}
