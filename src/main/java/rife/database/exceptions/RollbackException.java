/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class RollbackException extends DatabaseException
{
	@Serial private static final long serialVersionUID = -8696265689207175989L;

	public RollbackException()
	{
		super("Causes a transaction user to trigger a rollback.");
	}
}
