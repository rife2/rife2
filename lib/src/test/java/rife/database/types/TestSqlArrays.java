/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSqlArrays
{
	@Test
	public void testConvertArray()
	{
		assertEquals(SqlArrays.convertArray(new Object[] {
			"string'value", new StringBuffer("stringbuffer'value"), null, 12, new Object[] {"value1", "value2"}
		}), "{'string''value','stringbuffer''value',NULL,12,{'value1','value2'}}");

		assertEquals(SqlArrays.convertArray(new boolean[] {false, true, false}), "{false,true,false}");
		assertEquals(SqlArrays.convertArray(new byte[] {9, 8, 7}), "{9,8,7}");
		assertEquals(SqlArrays.convertArray(new double[] {32.9d, 87.3434d}), "{32.9,87.3434}");
		assertEquals(SqlArrays.convertArray(new float[] {12.3f, 9.4342f, 87.232f}), "{12.3,9.4342,87.232}");
		assertEquals(SqlArrays.convertArray(new int[] {9, 87, 3, 2}), "{9,87,3,2}");
		assertEquals(SqlArrays.convertArray(new long[] {2394L, 322L, 342L, 98394L}), "{2394,322,342,98394}");
		assertEquals(SqlArrays.convertArray(new short[] {78, 254, 23}), "{78,254,23}");
		assertEquals(SqlArrays.convertArray(new char[] {'g', 'M', 'q'}), "{'g','M','q'}");
	}
}
