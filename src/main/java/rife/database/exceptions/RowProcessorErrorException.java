/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class RowProcessorErrorException extends DatabaseException
{
	@Serial private static final long serialVersionUID = -5597696130038426852L;

	public RowProcessorErrorException(Throwable cause)
	{
		super("An error occurred while processing a resultset row.", cause);
	}
}
