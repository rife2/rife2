/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.config.RifeConfig;
import rife.scheduler.exceptions.FrequencyException;
import rife.scheduler.exceptions.SchedulerException;
import rife.tools.Localization;
import rife.validation.*;

import java.util.Calendar;
import java.util.Date;

public class Task extends Validation implements Cloneable {
    private int id_ = -1;
    private String type_ = null;
    private long planned_ = 0;
    private Frequency frequency_ = null;
    private boolean busy_ = false;

    private TaskManager taskManager_ = null;

    public Task() {
    }

    protected void activateValidation() {
        addRule(new ValidationRuleNotNull("type"));
        addRule(new ValidationRuleNotEmpty("planned"));
        addRule(new InvalidPlanned());
    }

    public void setTaskManager(TaskManager taskManager) {
        taskManager_ = taskManager;
    }

    public TaskManager getTaskManager() {
        return taskManager_;
    }

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

    public long getNextDate()
    throws FrequencyException {
        // lower towards the minute, remove seconds and milliseconds
        var current_calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
        current_calendar.set(Calendar.SECOND, 0);
        current_calendar.set(Calendar.MILLISECOND, 0);
        var current_time = current_calendar.getTimeInMillis();
        if (planned_ <= current_time) {
            return getNextDate(current_time);
        }

        return -1;
    }

    public long getNextDate(long start)
    throws FrequencyException {
        if (null == frequency_) {
            return -1;
        } else {
            return frequency_.getNextDate(start);
        }
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setType(String type) {
        type_ = type;
    }

    public Task type(String type) {
        setType(type);
        return this;
    }

    public String getType() {
        return type_;
    }

    public void setPlanned(Date planned) {
        setPlanned(planned.getTime());
    }

    public void setPlanned(long planned) {
        // lower towards the minute, remove seconds and milliseconds
        var planned_calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
        planned_calendar.setTimeInMillis(planned);
        planned_calendar.set(Calendar.SECOND, 0);
        planned_calendar.set(Calendar.MILLISECOND, 0);

        planned_ = planned_calendar.getTimeInMillis();
    }

    public Task planned(Date planned) {
        setPlanned(planned);
        return this;
    }

    public Task planned(long planned) {
        setPlanned(planned);
        return this;
    }

    public long getPlanned() {
        return planned_;
    }

    public void setFrequency(String frequency)
    throws FrequencyException {
        if (null == frequency) {
            frequency_ = null;
        } else {
            frequency_ = new Frequency(frequency);
        }
    }

    public Task frequency(String frequency)
    throws FrequencyException {
        setFrequency(frequency);
        return this;
    }

    public String getFrequency() {
        if (null == frequency_) {
            return null;
        }
        return frequency_.getFrequency();
    }

    public void setBusy(boolean busy) {
        busy_ = busy;
    }

    public Task busy(boolean busy) {
        setBusy(busy);
        return this;
    }

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

            var current_calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
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
