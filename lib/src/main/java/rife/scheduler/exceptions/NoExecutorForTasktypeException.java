/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class NoExecutorForTasktypeException extends SchedulerExecutionException
{
	@Serial private static final long serialVersionUID = -9088897866704438084L;
	
	private final String tasktype_;

	public NoExecutorForTasktypeException(String tasktype)
	{
		super("The scheduler didn't have an executor registered for the execution of a task with type '"+tasktype+"'.");

		tasktype_ = tasktype;
	}

	public String getTasktype()
	{
		return tasktype_;
	}
}
