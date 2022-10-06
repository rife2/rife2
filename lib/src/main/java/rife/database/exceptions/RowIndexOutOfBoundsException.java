/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;
import java.sql.SQLException;

public class RowIndexOutOfBoundsException extends SQLException
{
	@Serial private static final long serialVersionUID = 3132609745592263804L;

	public RowIndexOutOfBoundsException()
	{
		super("Row index out of bounds.");
	}
}
