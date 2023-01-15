/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.tools.Convert;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestDeletePgsql extends TestDelete {
    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testInstantiationPgsql() {
        var query = new Delete(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Delete");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testIncompleteQueryPgsql() {
        var query = new Delete(PGSQL);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Delete");
        }
        query.where("this = that");
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Delete");
        }
        query.from("tablename");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClearPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .where("this = that");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Delete");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testFromPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename");
        assertEquals(query.getSql(), "DELETE FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testHintPgsql() {
        var query = new Delete(PGSQL)
            .hint("NO_INDEX")
            .from("tablename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWherePgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .where("propertyByte = 89");
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyByte = 89");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereTypedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename");

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
            .whereOr("propertySqlDate", "=", new java.sql.Date(cal.getTime().getTime()))
            .whereAnd("propertyString", "LIKE", "someotherstring%")
            .whereAnd("propertyStringBuffer", "=", new StringBuffer("someotherstringbuff"))
            .whereOr("propertyTime", "=", Convert.toTime(cal))
            .whereAnd("propertyTimestamp", "<=", new Timestamp(cal.getTime().getTime()));

        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = false OR propertyByte = 54 AND propertyCalendar <= '2003-03-03 10:01:28.154' OR propertyChar = 'f' AND propertyDate = '2003-03-03 10:01:28.154' AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = '2003-03-03' AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = '10:01:28' AND propertyTimestamp <= '2003-03-03 10:01:28.154'");
        assertFalse(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereTypedMixedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename");

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
            .whereOr("propertyTime", "=", Convert.toTime(cal))
            .whereAnd("propertyTimestamp", "<=", new Timestamp(cal.getTime().getTime()));

        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = false OR propertyByte = 54 AND propertyCalendar <= '2003-03-03 10:01:28.154' OR propertyChar = 'f' AND propertyDate = '2003-03-03 10:01:28.154' AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = ? AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = '10:01:28' AND propertyTimestamp <= '2003-03-03 10:01:28.154'");

        assertFalse(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setDate("propertySqlDate", new java.sql.Date(cal.getTime().getTime()));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename");

        assertNull(query.getParameters());

        query.whereParameter("propertyInt", "=")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().size(), 3);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyChar");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyInt", "propertyLong", "propertyChar"}));

        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyInt = ? AND propertyLong < ? OR propertyChar = ?");
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
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersMixedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .where("propertyInt = 545")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyChar");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyLong", "propertyChar"}));

        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyInt = 545 AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setLong(1, 50000)
                    .setString(2, "v");
            }
        }));

        query.where("propertyInt = 545");

        assertNull(query.getParameters());
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereConstructionPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .where("propertyInt = 545")
            .whereAnd("propertyLong < 50000")
            .whereOr("propertyChar = 'v'");
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyInt = 545 AND propertyLong < 50000 OR propertyChar = 'v'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereConstructionGroupPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
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

        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyInt = 545 AND propertyLong < 50000 OR (propertyString = ? AND propertyByte <= 0 AND (propertyBoolean != true OR propertyStringBuffer LIKE ?)) OR propertyChar = 'v'");

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "someotherstring")
                    .setString("propertyStringBuffer", "stringbuff");
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .where(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = true AND propertyBooleanObject = false AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.167' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.167' AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.167' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.167' AND propertyLocalTime = '15:26:14' AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShort = 43 AND propertyShortObject = 68 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14' AND propertyTimestamp = '2002-06-18 15:26:14.167'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanConstrainedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .where(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = true AND propertyBooleanObject = false AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.167' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.167' AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.167' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.167' AND propertyLocalTime = '15:26:14' AND propertyLongObject = 66875 AND propertyShort = 43 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14' AND propertyTimestamp = '2002-06-18 15:26:14.167'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanNullValuesPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .where(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBoolean = false AND propertyBooleanObject = false AND propertyByte = 0 AND propertyByteObject = 0 AND propertyDouble = 0.0 AND propertyDoubleObject = 0.0 AND propertyFloat = 0.0 AND propertyFloatObject = 0.0 AND propertyInt = 0 AND propertyIntegerObject = 0 AND propertyLong = 0 AND propertyLongObject = 0 AND propertyShort = 0 AND propertyShortObject = 0");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanIncludedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .whereIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyByte = 89 AND propertyDouble = 53348.34 AND propertyShort = 43 AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanExcludedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .whereExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = true AND propertyBooleanObject = false AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.167' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.167' AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.167' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.167' AND propertyLocalTime = '15:26:14' AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShortObject = 68 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyTimestamp = '2002-06-18 15:26:14.167'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereBeanFilteredPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .whereFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyDouble = 53348.34 AND propertyStringBuffer = 'someotherstringbuff'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersBeanPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .whereParameters(BeanImpl.class);
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLong = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

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
                    .setTimestamp(6, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setString(7, "v")
                    .setString(8, "r")
                    .setTimestamp(9, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setDouble(10, 53348.34d)
                    .setDouble(11, 143298.692d)
                    .setString(12, "VALUE_THREE")
                    .setDouble(13, 98634.2d)
                    .setDouble(14, 8734.7d)
                    .setTimestamp(15, Timestamp.from(cal.toInstant()))
                    .setInt(16, 545)
                    .setInt(17, 968)
                    .setDate(18, new java.sql.Date(cal.getTime().getTime()))
                    .setTimestamp(19, Timestamp.from(cal.toInstant()))
                    .setTime(20, Convert.toTime(cal))
                    .setLong(21, 34563L)
                    .setLong(22, 66875L)
                    .setShort(23, (short) 43)
                    .setShort(24, (short) 68)
                    .setDate(25, new java.sql.Date(cal.getTime().getTime()))
                    .setString(26, "someotherstring")
                    .setString(27, "someotherstringbuff")
                    .setTime(28, Convert.toTime(cal))
                    .setTimestamp(29, new Timestamp(cal.getTime().getTime()));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersBeanConstrainedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .whereParameters(BeanImplConstrained.class);
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

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
                    .setTimestamp(6, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setString(7, "v")
                    .setString(8, "r")
                    .setTimestamp(9, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setDouble(10, 53348.34d)
                    .setDouble(11, 143298.692d)
                    .setFloat(12, 98634.2f)
                    .setFloat(13, 8734.7f)
                    .setTimestamp(14, Timestamp.from(cal.toInstant()))
                    .setInt(15, 545)
                    .setInt(16, 968)
                    .setDate(17, new java.sql.Date(cal.getTime().getTime()))
                    .setTimestamp(18, Timestamp.from(cal.toInstant()))
                    .setTime(19, Convert.toTime(cal))
                    .setLong(20, 66875L)
                    .setShort(21, (short) 43)
                    .setDate(22, new java.sql.Date(cal.getTime().getTime()))
                    .setString(23, "someotherstring")
                    .setString(24, "someotherstringbuff")
                    .setTime(25, Convert.toTime(cal))
                    .setTimestamp(26, new Timestamp(cal.getTime().getTime()));
            }
        });
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testWhereParametersBeanExcludedPgsql() {
        var query = new Delete(PGSQL);
        query.from("tablename")
            .whereParametersExcluded(BeanImpl.class,
                new String[]{"propertyBoolean", "propertyByte", "propertyChar",
                    "propertyDouble", "propertyInt", "propertyLong", "propertySqlDate",
                    "propertyStringBuffer", "propertyTimestamp"});
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyBigDecimal = ? AND propertyBooleanObject = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertyString = ? AND propertyTime = ?");

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
                    .setTimestamp(4, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setString(5, "r")
                    .setTimestamp(6, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setDouble(7, 143298.692d)
                    .setString(8, "VALUE_THREE")
                    .setDouble(9, 98634.2d)
                    .setDouble(10, 8734.7d)
                    .setTimestamp(11, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setInt(12, 968)
                    .setDate(13, new java.sql.Date(cal.getTime().getTime()))
                    .setTimestamp(14, Timestamp.from(cal.toInstant()))
                    .setTime(15, Convert.toTime(cal))
                    .setLong(16, 66875L)
                    .setShort(17, (short) 43)
                    .setShort(18, (short) 68)
                    .setString(19, "someotherstring")
                    .setTime(20, Convert.toTime(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testDeleteSubselectParamsPgsql() {
        var wherequery = new Select(PGSQL);
        wherequery
            .from("table2")
            .field("max(propertyShort)")
            .whereParameter("propertyShort", "!=");

        // Manual subselect creation
        var query = new Delete(PGSQL);
        query
            .where("propertyShort >= (" + wherequery + ")")
            .whereSubselect(wherequery)
            .whereParameterOr("propertyString", "=")
            .from("tablename");
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyShort >= (SELECT max(propertyShort) FROM table2 WHERE propertyShort != ?) OR propertyString = ?");
        var parameters = query.getParameters().getOrderedNamesArray();
        assertEquals(2, parameters.length);
        assertEquals(parameters[0], "propertyShort");
        assertEquals(parameters[1], "propertyString");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setShort("propertyShort", (short) 5)
                    .setString("propertyString", "thestring");
            }
        }));

        // Automated subselect creation
        query = new Delete(PGSQL);
        query
            .where("propertyShort", ">=", wherequery)
            .whereParameterOr("propertyString", "=")
            .whereOr("tablename.propertyFloat", ">", new Select(PGSQL)
                .from("table2")
                .field("max(propertyLong)")
                .whereParameter("propertyLong", "!="))
            .whereAnd("tablename.propertyDouble", "<=", new Select(PGSQL)
                .from("table2")
                .field("max(propertyFloat)")
                .whereParameter("propertyFloat", "!="))
            .from("tablename");
        assertEquals(query.getSql(), "DELETE FROM tablename WHERE propertyShort >= (SELECT max(propertyShort) FROM table2 WHERE propertyShort != ?) OR propertyString = ? OR tablename.propertyFloat > (SELECT max(propertyLong) FROM table2 WHERE propertyLong != ?) AND tablename.propertyDouble <= (SELECT max(propertyFloat) FROM table2 WHERE propertyFloat != ?)");
        parameters = query.getParameters().getOrderedNamesArray();
        assertEquals(4, parameters.length);
        assertEquals(parameters[0], "propertyShort");
        assertEquals(parameters[1], "propertyString");
        assertEquals(parameters[2], "propertyLong");
        assertEquals(parameters[3], "propertyFloat");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setShort("propertyShort", (short) 5)
                    .setString("propertyString", "thestring")
                    .setLong("propertyLong", 99999999)
                    .setFloat("propertyFloat", -1f);
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClonePgsql() {
        var wherequery = new Select(PGSQL);
        wherequery
            .from("table2")
            .field("max(propertyShort)")
            .whereParameter("propertyShort", "!=");

        var query = new Delete(PGSQL);
        query.from("tablename")
            .where("propertyShort >= (" + wherequery + ")")
            .whereSubselect(wherequery)
            .whereParameterOr("propertyString", "=")
            .whereOr("propertyByte", "=", (byte) 54)
            .whereAnd("propertyDouble", "!=", 73453.71d)
            .whereParameterOr("propertyInt", "=")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyChar", "M")
                    .setInt("propertyInt", 34)
                    .setString("propertyString", "string'value")
                    .setLong("propertyLong", 34543)
                    .setShort("propertyShort", (short) 4);
            }
        });
    }
}
