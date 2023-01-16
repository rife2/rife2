/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.*;
import rife.database.types.SqlNull;
import rife.tools.Convert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestUpdatePgsql extends TestUpdate {
    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testInstantiationPgsql() {
        var query = new Update(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Update");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testIncompleteQueryPgsql() {
        var query = new Update(PGSQL);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Update");
        }
        query.table("tablename4");
        try {
            query.getSql();
            fail();
        } catch (FieldsRequiredException e) {
            assertEquals(e.getQueryName(), "Update");
        }
        query.field("col1", "val1");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClearPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename4")
            .field("col1", "val1");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Update");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testHintPgsql() {
        var query = new Update(PGSQL)
            .hint("NO_INDEX")
            .table("tablename")
            .field("propertyByte", (byte) 16);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldPgsql() {
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyByte = 89")
            .field("nullColumn", SqlNull.NULL)
            .field("propertyBigDecimal", new BigDecimal("98347.876438637"))
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .field("propertyCalendar", cal.getTime())
            .field("propertyChar", 'M')
            .field("propertyDate", cal)
            .field("propertyDouble", 12.3d)
            .field("propertyFloat", 13.4f)
            .field("propertyInt", 34)
            .field("propertyLong", 45L)
            .field("propertyShort", (short) 12)
            .field("propertySqlDate", Convert.toSqlDate(cal))
            .field("propertyString", "string'value")
            .field("propertyStringBuffer", new StringBuffer("stringbuffer'value"))
            .field("propertyTime", Convert.toSqlTime(cal))
            .field("propertyTimestamp", Convert.toSqlTimestamp(cal));
        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = NULL, propertyBigDecimal = 98347.876438637, propertyBoolean = true, propertyByte = 16, propertyCalendar = '2002-08-19 12:17:52.462', propertyChar = 'M', propertyDate = '2002-08-19 12:17:52.462', propertyDouble = 12.3, propertyFloat = 13.4, propertyInt = 34, propertyLong = 45, propertyShort = 12, propertySqlDate = '2002-08-19', propertyString = 'string''value', propertyStringBuffer = 'stringbuffer''value', propertyTime = '12:17:52', propertyTimestamp = '2002-08-19 12:17:52.462' WHERE propertyByte = 89");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldCustomPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .fieldCustom("propertySqlDate", "now()");
        assertEquals(query.getSql(), "UPDATE tablename SET propertySqlDate = now()");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldParametersPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename");

        assertNull(query.getParameters());

        query.fieldParameter("nullColumn")
            .fieldParameter("propertyBigDecimal")
            .fieldParameter("propertyBoolean")
            .fieldParameter("propertyByte")
            .fieldParameter("propertyCalendar")
            .fieldParameter("propertyChar")
            .fieldParameter("propertyDate")
            .fieldParameter("propertyDouble")
            .fieldParameter("propertyFloat")
            .fieldParameter("propertyInt")
            .fieldParameter("propertyLong")
            .fieldParameter("propertyShort")
            .fieldParameter("propertySqlDate")
            .fieldParameter("propertyString")
            .fieldParameter("propertyStringBuffer")
            .fieldParameter("propertyTime")
            .fieldParameter("propertyTimestamp");

        assertEquals(query.getParameters().getOrderedNames().size(), 17);
        assertEquals(query.getParameters().getOrderedNames().get(0), "nullColumn");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyByte");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyCalendar");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyChar");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyDate");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyDouble");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyStringBuffer");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyTime");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyTimestamp");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{
            "nullColumn",
            "propertyBigDecimal",
            "propertyBoolean",
            "propertyByte",
            "propertyCalendar",
            "propertyChar",
            "propertyDate",
            "propertyDouble",
            "propertyFloat",
            "propertyInt",
            "propertyLong",
            "propertyShort",
            "propertySqlDate",
            "propertyString",
            "propertyStringBuffer",
            "propertyTime",
            "propertyTimestamp"}));

        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = ?, propertyBigDecimal = ?, propertyBoolean = ?, propertyByte = ?, propertyCalendar = ?, propertyChar = ?, propertyDate = ?, propertyDouble = ?, propertyFloat = ?, propertyInt = ?, propertyLong = ?, propertyShort = ?, propertySqlDate = ?, propertyString = ?, propertyStringBuffer = ?, propertyTime = ?, propertyTimestamp = ?");

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
                cal.set(Calendar.MILLISECOND, 462);
                statement
                    .setString(1, null)
                    .setBigDecimal(2, new BigDecimal("98347.876438637"))
                    .setBoolean(3, true)
                    .setByte(4, (byte) 16)
                    .setDate(5, Convert.toSqlDate(cal))
                    .setString(6, "M")
                    .setDate(7, Convert.toSqlDate(cal))
                    .setDouble(8, 12.3d)
                    .setFloat(9, 13.4f)
                    .setInt(10, 34)
                    .setLong(11, 45L)
                    .setShort(12, (short) 12)
                    .setDate(13, Convert.toSqlDate(cal))
                    .setString(14, "string'value")
                    .setString(15, "string'value2")
                    .setTime(16, Convert.toSqlTime(cal))
                    .setTimestamp(17, Convert.toSqlTimestamp(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldParametersMixedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename");

        assertNull(query.getParameters());

        final var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        query.fieldParameter("nullColumn")
            .field("propertyBigDecimal", new BigDecimal("98347.876438637"))
            .fieldParameter("propertyBoolean")
            .fieldParameter("propertyByte")
            .field("propertyCalendar", cal.getTime())
            .fieldParameter("propertyChar")
            .field("propertyDate", cal)
            .field("propertyDouble", 12.3d)
            .fieldParameter("propertyFloat")
            .fieldParameter("propertyInt")
            .field("propertyLong", 45L)
            .field("propertyShort", (short) 12)
            .fieldParameter("propertySqlDate")
            .fieldParameter("propertyString")
            .field("propertyStringBuffer", new StringBuffer("stringbuffer'value"))
            .field("propertyTime", Convert.toSqlTime(cal))
            .fieldParameter("propertyTimestamp");

        assertEquals(query.getParameters().getOrderedNames().size(), 9);
        assertEquals(query.getParameters().getOrderedNames().get(0), "nullColumn");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyByte");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyChar");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyTimestamp");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{
            "nullColumn",
            "propertyBoolean",
            "propertyByte",
            "propertyChar",
            "propertyFloat",
            "propertyInt",
            "propertySqlDate",
            "propertyString",
            "propertyTimestamp"}));

        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = ?, propertyBigDecimal = 98347.876438637, propertyBoolean = ?, propertyByte = ?, propertyCalendar = '2002-08-19 12:17:52.462', propertyChar = ?, propertyDate = '2002-08-19 12:17:52.462', propertyDouble = 12.3, propertyFloat = ?, propertyInt = ?, propertyLong = 45, propertyShort = 12, propertySqlDate = ?, propertyString = ?, propertyStringBuffer = 'stringbuffer''value', propertyTime = '12:17:52', propertyTimestamp = ?");

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString(1, null)
                    .setBoolean(2, true)
                    .setByte(3, (byte) 16)
                    .setString(4, "M")
                    .setFloat(5, 13.4f)
                    .setInt(6, 34)
                    .setDate(7, Convert.toSqlDate(cal))
                    .setString(8, "string'value")
                    .setTimestamp(9, Convert.toSqlTimestamp(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsPgsql() {
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyByte = 89")
            .fields(new Object[]{
                "nullColumn", SqlNull.NULL,
                "propertyBigDecimal", new BigDecimal("98347.876438637"),
                "propertyBoolean", true,
                "propertyByte", (byte) 16,
                "propertyCalendar", cal.getTime(),
                "propertyChar", 'M',
                "propertyDate", cal,
                "propertyDouble", 12.3d,
                "propertyFloat", 13.4f,
                "propertyInt", 34,
                "propertyLong", 45L,
                "propertyShort", (short) 12,
                "propertySqlDate", Convert.toSqlDate(cal),
                "propertyString", new String("string'value"),
                "propertyStringBuffer", new StringBuffer("stringbuffer'value"),
                "propertyTime", Convert.toSqlTime(cal),
                "propertyTimestamp", Convert.toSqlTimestamp(cal)
            });
        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = NULL, propertyBigDecimal = 98347.876438637, propertyBoolean = true, propertyByte = 16, propertyCalendar = '2002-08-19 12:17:52.462', propertyChar = 'M', propertyDate = '2002-08-19 12:17:52.462', propertyDouble = 12.3, propertyFloat = 13.4, propertyInt = 34, propertyLong = 45, propertyShort = 12, propertySqlDate = '2002-08-19', propertyString = 'string''value', propertyStringBuffer = 'stringbuffer''value', propertyTime = '12:17:52', propertyTimestamp = '2002-08-19 12:17:52.462' WHERE propertyByte = 89");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereConstructionPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where("propertyInt = 545")
            .whereAnd("propertyLong < 50000")
            .whereOr("propertyChar = 'v'");
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyInt = 545 AND propertyLong < 50000 OR propertyChar = 'v'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereConstructionGroupPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where("propertyInt = 545")
            .whereAnd("propertyLong < 50000")
            .startWhereOr()
            .whereParameter("propertyString", "=")
            .whereAnd("propertyByte", "<=", (byte) 0)
            .startWhereAnd()
            .where("propertyBoolean", "!=", true)
            .whereParameterOr("propertyStringBuffer", "LIKE")
            .end()
            .end()
            .whereOr("propertyChar = 'v'");

        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyStringBuffer");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyString", "propertyStringBuffer"}));

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyInt = 545 AND propertyLong < 50000 OR (propertyString = ? AND propertyByte <= 0 AND (propertyBoolean != true OR propertyStringBuffer LIKE ?)) OR propertyChar = 'v'");

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "someotherstring")
                    .setString("propertyStringBuffer", "stringbuff");
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereTypedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16);

        var cal = Calendar.getInstance();
        cal.set(2003, Calendar.MARCH, 3, 10, 1, 28);
        cal.set(Calendar.MILLISECOND, 154);

        query
            .where("propertyBigDecimal", ">=", new BigDecimal("53443433.9784567"))
            .whereAnd("propertyBoolean", "=", false)
            .whereOr("propertyByte", "=", (byte) 54)
            .whereAnd("propertyCalendar", "<=", cal)
            .whereOr("propertyChar", "=", 'f')
            .whereAnd("propertyDate", "=", cal.getTime())
            .whereAnd("propertyDouble", "!=", 73453.71d)
            .whereOr("propertyFloat", ">=", 1987.14f)
            .whereAnd("propertyInt", "=", 973)
            .whereAnd("propertyLong", "<", 347678L)
            .whereAnd("propertyShort", "=", (short) 78)
            .whereOr("propertySqlDate", "=", Convert.toSqlDate(cal))
            .whereAnd("propertyString", "LIKE", "someotherstring%")
            .whereAnd("propertyStringBuffer", "=", new StringBuffer("someotherstringbuff"))
            .whereOr("propertyTime", "=", Convert.toSqlTime(cal))
            .whereAnd("propertyTimestamp", "<=", Convert.toSqlTimestamp(cal));

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = false OR propertyByte = 54 AND propertyCalendar <= '2003-03-03 10:01:28.154' OR propertyChar = 'f' AND propertyDate = '2003-03-03 10:01:28.154' AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = '2003-03-03' AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = '10:01:28' AND propertyTimestamp <= '2003-03-03 10:01:28.154'");
        assertFalse(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereTypedMixedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16);

        final var cal = Calendar.getInstance();
        cal.set(2003, Calendar.MARCH, 3, 10, 1, 28);
        cal.set(Calendar.MILLISECOND, 154);

        query
            .where("propertyBigDecimal", ">=", new BigDecimal("53443433.9784567"))
            .whereAnd("propertyBoolean", "=", false)
            .whereOr("propertyByte = 54")
            .whereAnd("propertyCalendar", "<=", cal)
            .whereOr("propertyChar", "=", 'f')
            .whereAnd("propertyDate", "=", cal.getTime())
            .whereAnd("propertyDouble", "!=", 73453.71d)
            .whereOr("propertyFloat >= 1987.14")
            .whereAnd("propertyInt", "=", 973)
            .whereAnd("propertyLong", "<", 347678L)
            .whereAnd("propertyShort", "=", (short) 78)
            .whereParameterOr("propertySqlDate", "=")
            .whereAnd("propertyString", "LIKE", "someotherstring%")
            .whereAnd("propertyStringBuffer", "=", new StringBuffer("someotherstringbuff"))
            .whereOr("propertyTime", "=", Convert.toSqlTime(cal))
            .whereAnd("propertyTimestamp", "<=", Convert.toSqlTimestamp(cal));

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = false OR propertyByte = 54 AND propertyCalendar <= '2003-03-03 10:01:28.154' OR propertyChar = 'f' AND propertyDate = '2003-03-03 10:01:28.154' AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = ? AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = '10:01:28' AND propertyTimestamp <= '2003-03-03 10:01:28.154'");

        assertFalse(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setDate("propertySqlDate", Convert.toSqlDate(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16);

        assertNull(query.getParameters());

        query.whereParameter("propertyInt", "=")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().size(), 3);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyChar");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyInt", "propertyLong", "propertyChar"}));

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyInt = ? AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setInt(1, 545)
                    .setLong(2, 50000)
                    .setString(3, "v");
            }
        }));

        query.where("propertyInt = 545");
        assertNull(query.getParameters());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersMixedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where("propertyInt = 545")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyChar");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyLong", "propertyChar"}));

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyInt = 545 AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setLong(1, 50000)
                    .setString(2, "v");
            }
        }));

        query.where("propertyInt = 545");

        assertNull(query.getParameters());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldWhereParametersPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename");

        assertNull(query.getParameters());

        query.fieldParameter("propertyBoolean")
            .fieldParameter("propertyByte");

        assertEquals(query.getParameters().getOrderedNames().size(), 2);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyByte");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBoolean", "propertyByte"}));

        query.whereParameter("propertyInt", "=")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().size(), 5);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyByte");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyChar");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBoolean", "propertyByte", "propertyInt", "propertyLong", "propertyChar"}));

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = ?, propertyByte = ? WHERE propertyInt = ? AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setBoolean(1, true)
                    .setByte(2, (byte) 16)
                    .setInt(3, 545)
                    .setLong(4, 50000)
                    .setString(5, "v");
            }
        }));

        query.where("propertyInt = 545");

        assertEquals(query.getParameters().getOrderedNames().size(), 2);
        assertEquals(query.getParameters().getOrderedNamesArray().length, 2);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyByte");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBoolean", "propertyByte"}));
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = ?, propertyByte = ? WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsBeanPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyInt = 545")
            .fields(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = 219038743.392874, propertyBoolean = true, propertyBooleanObject = false, propertyByte = 89, propertyByteObject = 34, propertyCalendar = '2002-06-18 15:26:14.167', propertyChar = 'v', propertyCharacterObject = 'r', propertyDate = '2002-06-18 15:26:14.167', propertyDouble = 53348.34, propertyDoubleObject = 143298.692, propertyEnum = 'VALUE_THREE', propertyFloat = 98634.2, propertyFloatObject = 8734.7, propertyInstant = '2002-06-18 15:26:14.167', propertyInt = 545, propertyIntegerObject = 968, propertyLocalDate = '2002-06-18', propertyLocalDateTime = '2002-06-18 15:26:14.167', propertyLocalTime = '15:26:14', propertyLong = 34563, propertyLongObject = 66875, propertyShort = 43, propertyShortObject = 68, propertySqlDate = '2002-06-18', propertyString = 'someotherstring', propertyStringBuffer = 'someotherstringbuff', propertyTime = '15:26:14', propertyTimestamp = '2002-06-18 15:26:14.167' WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsBeanConstrainedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyInt = 545")
            .fields(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = 219038743.392874, propertyBoolean = true, propertyBooleanObject = false, propertyByteObject = 34, propertyCalendar = '2002-06-18 15:26:14.167', propertyChar = 'v', propertyCharacterObject = 'r', propertyDate = '2002-06-18 15:26:14.167', propertyDouble = 53348.34, propertyDoubleObject = 143298.692, propertyFloat = 98634.2, propertyFloatObject = 8734.7, propertyInstant = '2002-06-18 15:26:14.167', propertyInt = 545, propertyIntegerObject = 968, propertyLocalDate = '2002-06-18', propertyLocalDateTime = '2002-06-18 15:26:14.167', propertyLocalTime = '15:26:14', propertyLongObject = 66875, propertyShort = 43, propertySqlDate = '2002-06-18', propertyString = 'someotherstring', propertyStringBuffer = 'someotherstringbuff', propertyTime = '15:26:14', propertyTimestamp = '2002-06-18 15:26:14.167' WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsBeanNullValuesPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyInt = 545")
            .fields(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = false, propertyBooleanObject = false, propertyByte = 0, propertyByteObject = 0, propertyDouble = 0.0, propertyDoubleObject = 0.0, propertyFloat = 0.0, propertyFloatObject = 0.0, propertyInt = 0, propertyIntegerObject = 0, propertyLong = 0, propertyLongObject = 0, propertyShort = 0, propertyShortObject = 0 WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsBeanIncludedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyInt = 545")
            .fieldsIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyByte = 89, propertyDouble = 53348.34, propertyShort = 43, propertyStringBuffer = 'someotherstringbuff', propertyTime = '15:26:14' WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsBeanExcludedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyInt = 545")
            .fieldsExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = 219038743.392874, propertyBoolean = true, propertyBooleanObject = false, propertyByteObject = 34, propertyCalendar = '2002-06-18 15:26:14.167', propertyChar = 'v', propertyCharacterObject = 'r', propertyDate = '2002-06-18 15:26:14.167', propertyDoubleObject = 143298.692, propertyEnum = 'VALUE_THREE', propertyFloat = 98634.2, propertyFloatObject = 8734.7, propertyInstant = '2002-06-18 15:26:14.167', propertyInt = 545, propertyIntegerObject = 968, propertyLocalDate = '2002-06-18', propertyLocalDateTime = '2002-06-18 15:26:14.167', propertyLocalTime = '15:26:14', propertyLong = 34563, propertyLongObject = 66875, propertyShortObject = 68, propertySqlDate = '2002-06-18', propertyString = 'someotherstring', propertyTimestamp = '2002-06-18 15:26:14.167' WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsBeanFilteredPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .where("propertyInt = 545")
            .fieldsFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyDouble = 53348.34, propertyStringBuffer = 'someotherstringbuff' WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsParametersBeanPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .fieldsParameters(BeanImpl.class);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = ?, propertyBoolean = ?, propertyBooleanObject = ?, propertyByte = ?, propertyByteObject = ?, propertyCalendar = ?, propertyChar = ?, propertyCharacterObject = ?, propertyDate = ?, propertyDouble = ?, propertyDoubleObject = ?, propertyEnum = ?, propertyFloat = ?, propertyFloatObject = ?, propertyInstant = ?, propertyInt = ?, propertyIntegerObject = ?, propertyLocalDate = ?, propertyLocalDateTime = ?, propertyLocalTime = ?, propertyLong = ?, propertyLongObject = ?, propertyShort = ?, propertyShortObject = ?, propertySqlDate = ?, propertyString = ?, propertyStringBuffer = ?, propertyTime = ?, propertyTimestamp = ?");

        assertEquals(query.getParameters().getOrderedNames().size(), 29);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyBooleanObject");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyByte");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyByteObject");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyCalendar");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyChar");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyCharacterObject");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyDate");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyDouble");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyDoubleObject");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyEnum");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyFloatObject");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyInstant");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyLocalDate");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyLocalDateTime");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyLocalTime");
        assertEquals(query.getParameters().getOrderedNames().get(20), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(21), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(22), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(23), "propertyShortObject");
        assertEquals(query.getParameters().getOrderedNames().get(24), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(25), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(26), "propertyStringBuffer");
        assertEquals(query.getParameters().getOrderedNames().get(27), "propertyTime");
        assertEquals(query.getParameters().getOrderedNames().get(28), "propertyTimestamp");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBoolean", "propertyBooleanObject", "propertyByte", "propertyByteObject", "propertyCalendar", "propertyChar", "propertyCharacterObject", "propertyDate", "propertyDouble", "propertyDoubleObject", "propertyEnum", "propertyFloat", "propertyFloatObject", "propertyInstant", "propertyInt", "propertyIntegerObject", "propertyLocalDate", "propertyLocalDateTime", "propertyLocalTime", "propertyLong", "propertyLongObject", "propertyShort", "propertyShortObject", "propertySqlDate", "propertyString", "propertyStringBuffer", "propertyTime", "propertyTimestamp"});

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
                cal.set(Calendar.MILLISECOND, 462);
                statement
                    .setBigDecimal(1, new BigDecimal("98347.876438637"))
                    .setBoolean(2, false)
                    .setBoolean(3, true)
                    .setByte(4, (byte) 16)
                    .setByte(5, (byte) 72)
                    .setTimestamp(6, Convert.toSqlTimestamp(cal))
                    .setString(7, "M")
                    .setString(8, "p")
                    .setTimestamp(9, Convert.toSqlTimestamp(cal))
                    .setDouble(10, 12.3d)
                    .setDouble(11, 68.7d)
                    .setString(12, "VALUE_THREE")
                    .setFloat(13, 13.4f)
                    .setFloat(14, 42.1f)
                    .setTimestamp(15, Convert.toSqlTimestamp(cal))
                    .setInt(16, 92)
                    .setInt(17, 34)
                    .setDate(18, Convert.toSqlDate(cal))
                    .setTimestamp(19, Convert.toSqlTimestamp(cal))
                    .setTime(20, Convert.toSqlTime(cal))
                    .setLong(21, 687L)
                    .setLong(22, 92)
                    .setShort(23, (short) 7)
                    .setShort(24, (short) 12)
                    .setDate(25, Convert.toSqlDate(cal))
                    .setString(26, "string'value")
                    .setString(27, "string'value2")
                    .setTime(28, Convert.toSqlTime(cal))
                    .setTimestamp(29, Convert.toSqlTimestamp(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsParametersBeanConstrainedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .fieldsParameters(BeanImplConstrained.class);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = ?, propertyBoolean = ?, propertyBooleanObject = ?, propertyByteObject = ?, propertyCalendar = ?, propertyChar = ?, propertyCharacterObject = ?, propertyDate = ?, propertyDouble = ?, propertyDoubleObject = ?, propertyFloat = ?, propertyFloatObject = ?, propertyInstant = ?, propertyInt = ?, propertyIntegerObject = ?, propertyLocalDate = ?, propertyLocalDateTime = ?, propertyLocalTime = ?, propertyLongObject = ?, propertyShort = ?, propertySqlDate = ?, propertyString = ?, propertyStringBuffer = ?, propertyTime = ?, propertyTimestamp = ?");

        assertEquals(query.getParameters().getOrderedNames().size(), 25);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyBooleanObject");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyByteObject");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyCalendar");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyChar");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyCharacterObject");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyDate");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyDouble");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyDoubleObject");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyFloatObject");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyInstant");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyLocalDate");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyLocalDateTime");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyLocalTime");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(20), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(21), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(22), "propertyStringBuffer");
        assertEquals(query.getParameters().getOrderedNames().get(23), "propertyTime");
        assertEquals(query.getParameters().getOrderedNames().get(24), "propertyTimestamp");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBoolean", "propertyBooleanObject", "propertyByteObject", "propertyCalendar", "propertyChar", "propertyCharacterObject", "propertyDate", "propertyDouble", "propertyDoubleObject", "propertyFloat", "propertyFloatObject", "propertyInstant", "propertyInt", "propertyIntegerObject", "propertyLocalDate", "propertyLocalDateTime", "propertyLocalTime", "propertyLongObject", "propertyShort", "propertySqlDate", "propertyString", "propertyStringBuffer", "propertyTime", "propertyTimestamp"});

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
                cal.set(Calendar.MILLISECOND, 462);
                statement
                    .setBigDecimal(1, new BigDecimal("98347.876438637"))
                    .setBoolean(2, false)
                    .setBoolean(3, true)
                    .setByte(4, (byte) 72)
                    .setTimestamp(5, Convert.toSqlTimestamp(cal))
                    .setString(6, "M")
                    .setString(7, "p")
                    .setTimestamp(8, Convert.toSqlTimestamp(cal))
                    .setDouble(9, 12.3d)
                    .setDouble(10, 68.7d)
                    .setFloat(11, 13.4f)
                    .setFloat(12, 42.1f)
                    .setTimestamp(13, Convert.toSqlTimestamp(cal))
                    .setInt(14, 92)
                    .setInt(15, 34)
                    .setDate(16, Convert.toSqlDate(cal))
                    .setTimestamp(17, Convert.toSqlTimestamp(cal))
                    .setTime(18, Convert.toSqlTime(cal))
                    .setLong(19, 92)
                    .setShort(20, (short) 7)
                    .setDate(21, Convert.toSqlDate(cal))
                    .setString(22, "string'value")
                    .setString(23, "string'value2")
                    .setTime(24, Convert.toSqlTime(cal))
                    .setTimestamp(25, Convert.toSqlTimestamp(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFieldsParametersBeanExcludedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .fieldsParametersExcluded(BeanImpl.class,
                new String[]{"propertyBoolean", "propertyByte", "propertyChar",
                    "propertyDouble", "propertyInt", "propertyLong",
                    "propertySqlDate", "propertyStringBuffer", "propertyTimestamp"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = ?, propertyBooleanObject = ?, propertyByteObject = ?, propertyCalendar = ?, propertyCharacterObject = ?, propertyDate = ?, propertyDoubleObject = ?, propertyEnum = ?, propertyFloat = ?, propertyFloatObject = ?, propertyInstant = ?, propertyIntegerObject = ?, propertyLocalDate = ?, propertyLocalDateTime = ?, propertyLocalTime = ?, propertyLongObject = ?, propertyShort = ?, propertyShortObject = ?, propertyString = ?, propertyTime = ?");

        assertEquals(query.getParameters().getOrderedNames().size(), 20);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBooleanObject");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyByteObject");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyCalendar");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyCharacterObject");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyDate");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyDoubleObject");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyEnum");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyFloatObject");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyInstant");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyLocalDate");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyLocalDateTime");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyLocalTime");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyShortObject");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyTime");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBooleanObject", "propertyByteObject", "propertyCalendar", "propertyCharacterObject", "propertyDate", "propertyDoubleObject", "propertyEnum", "propertyFloat", "propertyFloatObject", "propertyInstant", "propertyIntegerObject", "propertyLocalDate", "propertyLocalDateTime", "propertyLocalTime", "propertyLongObject", "propertyShort", "propertyShortObject", "propertyString", "propertyTime"});

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
                cal.set(Calendar.MILLISECOND, 462);
                statement
                    .setBigDecimal(1, new BigDecimal("98347.876438637"))
                    .setBoolean(2, true)
                    .setByte(3, (byte) 72)
                    .setTimestamp(4, Convert.toSqlTimestamp(cal))
                    .setString(5, "o")
                    .setTimestamp(6, Convert.toSqlTimestamp(cal))
                    .setDouble(7, 86.7d)
                    .setString(8, "VALUE_THREE")
                    .setFloat(9, 13.4f)
                    .setFloat(10, 32.8f)
                    .setTimestamp(11, Convert.toSqlTimestamp(cal))
                    .setInt(12, 358)
                    .setDate(13, Convert.toSqlDate(cal))
                    .setTimestamp(14, Convert.toSqlTimestamp(cal))
                    .setTime(15, Convert.toSqlTime(cal))
                    .setLong(16, 9680L)
                    .setShort(17, (short) 12)
                    .setShort(18, (short) 78)
                    .setString(19, "string'value")
                    .setTime(20, Convert.toSqlTime(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = true AND propertyBooleanObject = false AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.167' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.167' AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.167' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.167' AND propertyLocalTime = '15:26:14' AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShort = 43 AND propertyShortObject = 68 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14' AND propertyTimestamp = '2002-06-18 15:26:14.167'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanConstrainedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = true AND propertyBooleanObject = false AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.167' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.167' AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.167' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.167' AND propertyLocalTime = '15:26:14' AND propertyLongObject = 66875 AND propertyShort = 43 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14' AND propertyTimestamp = '2002-06-18 15:26:14.167'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanNullValuesPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBoolean = false AND propertyBooleanObject = false AND propertyByte = 0 AND propertyByteObject = 0 AND propertyDouble = 0.0 AND propertyDoubleObject = 0.0 AND propertyFloat = 0.0 AND propertyFloatObject = 0.0 AND propertyInt = 0 AND propertyIntegerObject = 0 AND propertyLong = 0 AND propertyLongObject = 0 AND propertyShort = 0 AND propertyShortObject = 0");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanIncludedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyByte = 89 AND propertyDouble = 53348.34 AND propertyShort = 43 AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanExcludedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = true AND propertyBooleanObject = false AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.167' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.167' AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.167' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.167' AND propertyLocalTime = '15:26:14' AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShortObject = 68 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyTimestamp = '2002-06-18 15:26:14.167'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanFilteredPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyDouble = 53348.34 AND propertyStringBuffer = 'someotherstringbuff'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersBeanPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereParameters(BeanImpl.class);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLong = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

        assertEquals(query.getParameters().getOrderedNames().size(), 29);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyBooleanObject");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyByte");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyByteObject");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyCalendar");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyChar");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyCharacterObject");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyDate");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyDouble");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyDoubleObject");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyEnum");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyFloatObject");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyInstant");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyLocalDate");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyLocalDateTime");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyLocalTime");
        assertEquals(query.getParameters().getOrderedNames().get(20), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(21), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(22), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(23), "propertyShortObject");
        assertEquals(query.getParameters().getOrderedNames().get(24), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(25), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(26), "propertyStringBuffer");
        assertEquals(query.getParameters().getOrderedNames().get(27), "propertyTime");
        assertEquals(query.getParameters().getOrderedNames().get(28), "propertyTimestamp");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBoolean", "propertyBooleanObject", "propertyByte", "propertyByteObject", "propertyCalendar", "propertyChar", "propertyCharacterObject", "propertyDate", "propertyDouble", "propertyDoubleObject", "propertyEnum", "propertyFloat", "propertyFloatObject", "propertyInstant", "propertyInt", "propertyIntegerObject", "propertyLocalDate", "propertyLocalDateTime", "propertyLocalTime", "propertyLong", "propertyLongObject", "propertyShort", "propertyShortObject", "propertySqlDate", "propertyString", "propertyStringBuffer", "propertyTime", "propertyTimestamp"});

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 167);
                statement
                    .setBigDecimal(1, new BigDecimal("219038743.392874"))
                    .setBoolean(2, true)
                    .setBoolean(3, false)
                    .setByte(4, (byte) 89)
                    .setByte(5, (byte) 34)
                    .setTimestamp(6, Convert.toSqlTimestamp(cal))
                    .setString(7, "v")
                    .setString(8, "r")
                    .setTimestamp(9, Convert.toSqlTimestamp(cal))
                    .setDouble(10, 53348.34d)
                    .setDouble(11, 143298.692d)
                    .setString(12, "VALUE_THREE")
                    .setDouble(13, 98634.2d)
                    .setDouble(14, 8734.7d)
                    .setTimestamp(15, Convert.toSqlTimestamp(cal))
                    .setInt(16, 545)
                    .setInt(17, 968)
                    .setDate(18, Convert.toSqlDate(cal))
                    .setTimestamp(19, Convert.toSqlTimestamp(cal))
                    .setTime(20, Convert.toSqlTime(cal))
                    .setLong(21, 34563L)
                    .setLong(22, 66875L)
                    .setShort(23, (short) 43)
                    .setShort(24, (short) 68)
                    .setDate(25, Convert.toSqlDate(cal))
                    .setString(26, "someotherstring")
                    .setString(27, "someotherstringbuff")
                    .setTime(28, Convert.toSqlTime(cal))
                    .setTimestamp(29, Convert.toSqlTimestamp(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersBeanConstrainedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereParameters(BeanImplConstrained.class);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

        assertEquals(query.getParameters().getOrderedNames().size(), 26);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyBooleanObject");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyByte");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyByteObject");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyCalendar");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyChar");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyCharacterObject");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyDate");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyDouble");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyDoubleObject");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyFloatObject");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyInstant");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyLocalDate");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyLocalDateTime");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyLocalTime");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(20), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(21), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(22), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(23), "propertyStringBuffer");
        assertEquals(query.getParameters().getOrderedNames().get(24), "propertyTime");
        assertEquals(query.getParameters().getOrderedNames().get(25), "propertyTimestamp");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBoolean", "propertyBooleanObject", "propertyByte", "propertyByteObject", "propertyCalendar", "propertyChar", "propertyCharacterObject", "propertyDate", "propertyDouble", "propertyDoubleObject", "propertyFloat", "propertyFloatObject", "propertyInstant", "propertyInt", "propertyIntegerObject", "propertyLocalDate", "propertyLocalDateTime", "propertyLocalTime", "propertyLongObject", "propertyShort", "propertySqlDate", "propertyString", "propertyStringBuffer", "propertyTime", "propertyTimestamp"});

        // don't check if actual rows were returned, since Derby doesn't
        // match on the float
        execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 167);
                statement
                    .setBigDecimal(1, new BigDecimal("219038743.392874"))
                    .setBoolean(2, true)
                    .setBoolean(3, false)
                    .setByte(4, (byte) 89)
                    .setByte(5, (byte) 34)
                    .setTimestamp(6, Convert.toSqlTimestamp(cal))
                    .setString(7, "v")
                    .setString(8, "r")
                    .setTimestamp(9, Convert.toSqlTimestamp(cal))
                    .setDouble(10, 53348.34d)
                    .setDouble(11, 143298.692d)
                    .setFloat(12, 98634.2f)
                    .setFloat(13, 8734.7f)
                    .setTimestamp(14, Convert.toSqlTimestamp(cal))
                    .setInt(15, 545)
                    .setInt(16, 968)
                    .setDate(17, Convert.toSqlDate(cal))
                    .setTimestamp(18, Convert.toSqlTimestamp(cal))
                    .setTime(19, Convert.toSqlTime(cal))
                    .setLong(20, 66875L)
                    .setShort(21, (short) 43)
                    .setDate(22, Convert.toSqlDate(cal))
                    .setString(23, "someotherstring")
                    .setString(24, "someotherstringbuff")
                    .setTime(25, Convert.toSqlTime(cal))
                    .setTimestamp(26, Convert.toSqlTimestamp(cal));
            }
        });
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersBeanExcludedPgsql() {
        var query = new Update(PGSQL);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereParametersExcluded(BeanImpl.class,
                new String[]{"propertyBoolean", "propertyByte", "propertyChar",
                    "propertyDouble", "propertyInt", "propertyLong",
                    "propertySqlDate", "propertyStringBuffer", "propertyTimestamp"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = true, propertyByte = 16 WHERE propertyBigDecimal = ? AND propertyBooleanObject = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertyString = ? AND propertyTime = ?");

        assertEquals(query.getParameters().getOrderedNames().size(), 20);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBooleanObject");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyByteObject");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyCalendar");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyCharacterObject");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyDate");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyDoubleObject");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyEnum");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyFloatObject");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyInstant");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyLocalDate");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyLocalDateTime");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyLocalTime");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyShortObject");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyTime");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBooleanObject", "propertyByteObject", "propertyCalendar", "propertyCharacterObject", "propertyDate", "propertyDoubleObject", "propertyEnum", "propertyFloat", "propertyFloatObject", "propertyInstant", "propertyIntegerObject", "propertyLocalDate", "propertyLocalDateTime", "propertyLocalTime", "propertyLongObject", "propertyShort", "propertyShortObject", "propertyString", "propertyTime"});

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 167);
                statement
                    .setBigDecimal(1, new BigDecimal("219038743.392874"))
                    .setBoolean(2, false)
                    .setByte(3, (byte) 34)
                    .setTimestamp(4, Convert.toSqlTimestamp(cal))
                    .setString(5, "r")
                    .setTimestamp(6, Convert.toSqlTimestamp(cal))
                    .setDouble(7, 143298.692d)
                    .setString(8, "VALUE_THREE")
                    .setDouble(9, 98634.2d)
                    .setDouble(10, 8734.7d)
                    .setTimestamp(11, Convert.toSqlTimestamp(cal))
                    .setInt(12, 968)
                    .setDate(13, Convert.toSqlDate(cal))
                    .setTimestamp(14, Convert.toSqlTimestamp(cal))
                    .setTime(15, Convert.toSqlTime(cal))
                    .setLong(16, 66875L)
                    .setShort(17, (short) 43)
                    .setShort(18, (short) 68)
                    .setString(19, "someotherstring")
                    .setTime(20, Convert.toSqlTime(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testSubselectParamsPgsql() {
        var fieldquery = new Select(PGSQL);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");
        var wherequery = new Select(PGSQL);
        wherequery
            .from("table2")
            .field("max(propertyShort)")
            .whereParameter("propertyShort", "!=");

        // Manual subselect creation
        var query = new Update(PGSQL);
        // shuffled the structure around a bit to test the correct order usage
        query
            .where("propertyShort >= (" + wherequery + ")")
            .whereSubselect(wherequery)
            .whereParameterOr("propertyString", "propertyString2", "=")
            .table("tablename")
            .fieldParameter("propertyString")
            .fieldCustom("propertyLong", "(" + fieldquery + ")")
            .fieldSubselect(fieldquery);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyString = ?, propertyLong = (SELECT max(propertyLong) FROM table2 WHERE propertyInt > ?) WHERE propertyShort >= (SELECT max(propertyShort) FROM table2 WHERE propertyShort != ?) OR propertyString = ?");
        var parameters = query.getParameters().getOrderedNamesArray();
        assertEquals(4, parameters.length);
        assertEquals(parameters[0], "propertyString");
        assertEquals(parameters[1], "propertyInt");
        assertEquals(parameters[2], "propertyShort");
        assertEquals(parameters[3], "propertyString2");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "thestring")
                    .setInt("propertyInt", 1)
                    .setShort("propertyShort", (short) 5)
                    .setString("propertyString2", "anotherstring");
            }
        }));

        // Automated subselect creation
        query = new Update(PGSQL);
        // shuffled the structure around a bit to test the correct order usage
        query
            .where("propertyShort", ">=", wherequery)
            .whereParameterOr("propertyString", "propertyString2", "=")
            .whereOr("tablename.propertyFloat", ">", new Select(PGSQL)
                .from("table2")
                .field("max(propertyLong)")
                .whereParameter("propertyLong", "!="))
            .whereAnd("tablename.propertyDouble", "<=", new Select(PGSQL)
                .from("table2")
                .field("max(propertyFloat)")
                .whereParameter("propertyFloat", "!="))
            .table("tablename")
            .fieldParameter("propertyString")
            .field("propertyLong", fieldquery);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyString = ?, propertyLong = (SELECT max(propertyLong) FROM table2 WHERE propertyInt > ?) WHERE propertyShort >= (SELECT max(propertyShort) FROM table2 WHERE propertyShort != ?) OR propertyString = ? OR tablename.propertyFloat > (SELECT max(propertyLong) FROM table2 WHERE propertyLong != ?) AND tablename.propertyDouble <= (SELECT max(propertyFloat) FROM table2 WHERE propertyFloat != ?)");
        parameters = query.getParameters().getOrderedNamesArray();
        assertEquals(6, parameters.length);
        assertEquals(parameters[0], "propertyString");
        assertEquals(parameters[1], "propertyInt");
        assertEquals(parameters[2], "propertyShort");
        assertEquals(parameters[3], "propertyString2");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "thestring")
                    .setInt("propertyInt", 1)
                    .setShort("propertyShort", (short) 5)
                    .setString("propertyString2", "anotherstring")
                    .setLong("propertyLong", 99999999)
                    .setFloat("propertyFloat", -1f);
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClonePgsql() {
        var fieldquery = new Select(PGSQL);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");
        var wherequery = new Select(PGSQL);
        wherequery
            .from("table2")
            .field("max(propertyShort)")
            .whereParameter("propertyShort", "!=");

        final var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        var query = new Update(PGSQL);
        query.table("tablename")
            .fieldParameter("nullColumn")
            .field("propertyBigDecimal", new BigDecimal("98347.876438637"))
            .fieldParameter("propertyBoolean")
            .fieldParameter("propertyByte")
            .field("propertyCalendar", cal.getTime())
            .fieldParameter("propertyChar")
            .field("propertyDate", cal)
            .field("propertyDouble", 12.3d)
            .fieldParameter("propertyFloat")
            .fieldParameter("propertyInt")
            .field("propertyShort", (short) 12)
            .fieldParameter("propertySqlDate")
            .fieldParameter("propertyString")
            .field("propertyStringBuffer", new StringBuffer("stringbuffer'value"))
            .field("propertyTime", Convert.toSqlTime(cal))
            .fieldParameter("propertyTimestamp")
            .fieldCustom("propertyLong", "(" + fieldquery + ")")
            .fieldSubselect(fieldquery)
            .where("tablename.propertyShort >= (" + wherequery + ")")
            .whereSubselect(wherequery)
            .whereParameterOr("tablename.propertyString", "propertyString", "=")
            .whereOr("tablename.propertyByte", "=", (byte) 54)
            .whereAnd("tablename.propertyDouble", "!=", 73453.71d)
            .whereParameterOr("tablename.propertyInt", "propertyInt", "=")
            .whereParameterAnd("tablename.propertyLong", "propertyLong", "<")
            .whereParameterOr("tablename.propertyChar", "propertyChar", "=");

        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("nullColumn", null)
                    .setBoolean("propertyBoolean", true)
                    .setByte("propertyByte", (byte) 16)
                    .setString("propertyChar", "M")
                    .setFloat("propertyFloat", 13.4f)
                    .setInt("propertyInt", 34)
                    .setDate("propertySqlDate", Convert.toSqlDate(cal))
                    .setString("propertyString", "string'value")
                    .setTimestamp("propertyTimestamp", Convert.toSqlTimestamp(cal))
                    .setShort("propertyShort", (short) 4)
                    .setLong("propertyLong", 34543);
            }
        });
    }
}
