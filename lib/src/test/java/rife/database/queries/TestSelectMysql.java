/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.TableNameOrFieldsRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.tools.Convert;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestSelectMysql extends TestSelect {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testInstantiationMysql() {
        var query = new Select(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameOrFieldsRequiredException e) {
            assertEquals(e.getQueryName(), "Select");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testIncompleteQueryMysql() {
        var query = new Select(MYSQL);
        try {
            query.getSql();
            fail();
        } catch (TableNameOrFieldsRequiredException e) {
            assertEquals(e.getQueryName(), "Select");
        }
        query.from("tablename");
        assertNotNull(query.getSql());

        query = new Select(MYSQL);
        try {
            query.getSql();
            fail();
        } catch (TableNameOrFieldsRequiredException e) {
            assertEquals(e.getQueryName(), "Select");
        }
        query.field("field");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testClearMysql() {
        var query = new Select(MYSQL);
        query.from("tablename");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (TableNameOrFieldsRequiredException e) {
            assertEquals(e.getQueryName(), "Select");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testBasicMysql() {
        var query = new Select(MYSQL);
        query.from("tablename");
        assertEquals(query.getSql(), "SELECT * FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testHintMysql() {
        var query = new Select(MYSQL);
        query
            .hint("SQL_NO_CACHE")
            .from("tablename");
        assertEquals(query.getSql(), "SELECT SQL_NO_CACHE * FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testOrderByAscendingMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .orderBy("propertyInt", Select.ASC);
        assertEquals(query.getSql(), "SELECT * FROM tablename ORDER BY propertyInt ASC");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testOrderByDescendingMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .orderBy("propertyInt", Select.DESC);
        assertEquals(query.getSql(), "SELECT * FROM tablename ORDER BY propertyInt DESC");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testBeanMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .fields(BeanImpl.class);
        assertEquals(query.getSql(), "SELECT propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShort, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testBeanConstrainedMysql() {
        var query = new Select(MYSQL, BeanImplConstrained.class);
        query.from("tablename");
        assertEquals(query.getSql(), "SELECT * FROM tablename ORDER BY propertyString ASC, propertyInt DESC");
        assertTrue(execute(query));

        query = new Select(MYSQL, BeanImplConstrained.class);
        query.from("tablename")
            .orderBy("propertyByte");
        assertEquals(query.getSql(), "SELECT * FROM tablename ORDER BY propertyByte ASC");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testBeanExcludedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .fieldsExcluded(BeanImpl.class, "propertyCalendar", "propertyFloat", "propertyShort");
        assertEquals(query.getSql(), "SELECT propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testBeanTableMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .fields("tablename", BeanImpl.class);
        assertEquals(query.getSql(), "SELECT tablename.propertyBigDecimal, tablename.propertyBoolean, tablename.propertyBooleanObject, tablename.propertyByte, tablename.propertyByteObject, tablename.propertyCalendar, tablename.propertyChar, tablename.propertyCharacterObject, tablename.propertyDate, tablename.propertyDouble, tablename.propertyDoubleObject, tablename.propertyEnum, tablename.propertyFloat, tablename.propertyFloatObject, tablename.propertyInstant, tablename.propertyInt, tablename.propertyIntegerObject, tablename.propertyLocalDate, tablename.propertyLocalDateTime, tablename.propertyLocalTime, tablename.propertyLong, tablename.propertyLongObject, tablename.propertyShort, tablename.propertyShortObject, tablename.propertySqlDate, tablename.propertyString, tablename.propertyStringBuffer, tablename.propertyTime, tablename.propertyTimestamp FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testBeanExcludedTableMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .fieldsExcluded("tablename", BeanImpl.class, "propertyCalendar", "propertyFloat", "propertyShort");
        assertEquals(query.getSql(), "SELECT tablename.propertyBigDecimal, tablename.propertyBoolean, tablename.propertyBooleanObject, tablename.propertyByte, tablename.propertyByteObject, tablename.propertyChar, tablename.propertyCharacterObject, tablename.propertyDate, tablename.propertyDouble, tablename.propertyDoubleObject, tablename.propertyEnum, tablename.propertyFloatObject, tablename.propertyInstant, tablename.propertyInt, tablename.propertyIntegerObject, tablename.propertyLocalDate, tablename.propertyLocalDateTime, tablename.propertyLocalTime, tablename.propertyLong, tablename.propertyLongObject, tablename.propertyShortObject, tablename.propertySqlDate, tablename.propertyString, tablename.propertyStringBuffer, tablename.propertyTime, tablename.propertyTimestamp FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereTypedMysql() {
        var query = new Select(MYSQL);
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
            .whereOr("propertyTime", "=", new Time(cal.getTime().getTime()))
            .whereAnd("propertyTimestamp", "<=", new Timestamp(cal.getTime().getTime()));

        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = 0 OR propertyByte = 54 AND propertyCalendar <= '2003-03-03 10:01:28.0' OR propertyChar = 'f' AND propertyDate = '2003-03-03 10:01:28.0' AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = '2003-03-03' AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = '10:01:28' AND propertyTimestamp <= '2003-03-03 10:01:28.0'");
        assertFalse(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereTypedMixedMysql() {
        var query = new Select(MYSQL);
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
            .whereOr("propertyTime", "=", new Time(cal.getTime().getTime()))
            .whereAnd("propertyTimestamp", "<=", new Timestamp(cal.getTime().getTime()));

        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = 0 OR propertyByte = 54 AND propertyCalendar <= '2003-03-03 10:01:28.0' OR propertyChar = 'f' AND propertyDate = '2003-03-03 10:01:28.0' AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = ? AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = '10:01:28' AND propertyTimestamp <= '2003-03-03 10:01:28.0'");

        assertFalse(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setDate("propertySqlDate", new java.sql.Date(cal.getTime().getTime()));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereParametersMysql() {
        var query = new Select(MYSQL);
        query.from("tablename");

        assertNull(query.getParameters());

        query.whereParameter("propertyInt", "=")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().size(), 3);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyChar");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyInt", "propertyLong", "propertyChar"});

        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyInt = ? AND propertyLong < ? OR propertyChar = ?");
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
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereParametersMixedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .where("propertyInt = 545")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyChar");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyLong", "propertyChar"});

        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyInt = 545 AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setLong(1, 50000)
                    .setString(2, "v");
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereConstructionMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .where("propertyInt = 545")
            .whereAnd("propertyLong < 50000")
            .whereOr("propertyChar = 'v'");
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyInt = 545 AND propertyLong < 50000 OR propertyChar = 'v'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereConstructionGroupMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .startWhere()
            .where("propertyInt", "=", 545)
            .whereAnd("propertyByte", "=", 89)
            .end()
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
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyString", "propertyStringBuffer"});

        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE (propertyInt = 545 AND propertyByte = 89) AND propertyLong < 50000 OR (propertyString = ? AND propertyByte <= 0 AND (propertyBoolean != 1 OR propertyStringBuffer LIKE ?)) OR propertyChar = 'v'");

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "someotherstring")
                    .setString("propertyStringBuffer", "stringbuff");
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereBeanMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .where(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = 1 AND propertyBooleanObject = 0 AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.0' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.0' AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.0' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.0' AND propertyLocalTime = '15:26:14' AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShort = 43 AND propertyShortObject = 68 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14' AND propertyTimestamp = '2002-06-18 15:26:14.0'");
        // mysql doesn't compare correctly on floats, thus don't check results
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereBeanConstrainedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .where(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = 1 AND propertyBooleanObject = 0 AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.0' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.0' AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.0' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.0' AND propertyLocalTime = '15:26:14' AND propertyLongObject = 66875 AND propertyShort = 43 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14' AND propertyTimestamp = '2002-06-18 15:26:14.0'");
        // mysql doesn't compare correctly on floats, thus don't check results
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereBeanNullValuesMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .where(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBoolean = 0 AND propertyBooleanObject = 0 AND propertyByte = 0 AND propertyByteObject = 0 AND propertyDouble = 0.0 AND propertyDoubleObject = 0.0 AND propertyFloat = 0.0 AND propertyFloatObject = 0.0 AND propertyInt = 0 AND propertyIntegerObject = 0 AND propertyLong = 0 AND propertyLongObject = 0 AND propertyShort = 0 AND propertyShortObject = 0");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereBeanIncludedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .whereIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyByte = 89 AND propertyDouble = 53348.34 AND propertyShort = 43 AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = '15:26:14'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereBeanExcludedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .whereExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = 1 AND propertyBooleanObject = 0 AND propertyByteObject = 34 AND propertyCalendar = '2002-06-18 15:26:14.0' AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = '2002-06-18 15:26:14.0' AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = '2002-06-18 15:26:14.0' AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = '2002-06-18' AND propertyLocalDateTime = '2002-06-18 15:26:14.0' AND propertyLocalTime = '15:26:14' AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShortObject = 68 AND propertySqlDate = '2002-06-18' AND propertyString = 'someotherstring' AND propertyTimestamp = '2002-06-18 15:26:14.0'");
        // mysql doesn't compare correctly on floats, thus don't check results
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereBeanFilteredMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .whereFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyDouble = 53348.34 AND propertyStringBuffer = 'someotherstringbuff'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    public void testWhereParametersBeanMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .whereParameters(BeanImpl.class);
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLong = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

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
                cal.set(Calendar.MILLISECOND, 0);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testWhereParametersBeanConstrainedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .whereParameters(BeanImplConstrained.class);
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

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

        // don't check if actual rows were returned, since Mysql doesn't
        // match on the float
        execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 0);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    public void testWhereParametersBeanExcludedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .whereParametersExcluded(BeanImpl.class,
                new String[]{"propertyBoolean", "propertyByte", "propertyChar",
                    "propertyDouble", "propertyInt", "propertyLong",
                    "propertySqlDate", "propertyStringBuffer", "propertyTimestamp"});
        assertEquals(query.getSql(), "SELECT * FROM tablename WHERE propertyBigDecimal = ? AND propertyBooleanObject = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertyString = ? AND propertyTime = ?");

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
                cal.set(Calendar.MILLISECOND, 0);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDistinctMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .distinct()
            .where("propertyByte = 89")
            .orderBy("propertyDouble")
            .orderBy("propertyShort")
            .orderBy("propertyTime");
        assertEquals(query.getSql(), "SELECT DISTINCT * FROM tablename WHERE propertyByte = 89 ORDER BY propertyDouble ASC, propertyShort ASC, propertyTime ASC");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testDistinctOnMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .distinctOn("propertyDouble")
            .distinctOn("propertyShort")
            .distinctOn("propertyTime")
            .where("propertyByte = 89")
            .orderBy("propertyDouble")
            .orderBy("propertyShort")
            .orderBy("propertyTime");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testComplexMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .field("field1")
            .field("field2")
            .field("field3")
            .join("table2")
            .joinCross("table3")
            .where("this = that")
            .groupBy("gexpr1")
            .groupBy("gexpr2")
            .having("hexpr1")
            .having("hexpr2")
            .distinct()
            .unionAll("uexpr1")
            .union("uexpr2")
            .limit(3)
            .offset(1);
        assertEquals(query.getSql(), "SELECT DISTINCT field1, field2, field3 FROM tablename, table2 CROSS JOIN table3 WHERE this = that GROUP BY gexpr1, gexpr2 HAVING hexpr1, hexpr2 UNION ALL uexpr1 UNION uexpr2 LIMIT 3 OFFSET 1");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testGroupByBeanMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .fields(BeanImpl.class)
            .groupBy(BeanImpl.class);
        assertEquals(query.getSql(), "SELECT propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShort, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp FROM tablename GROUP BY propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShort, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testGroupByBeanExcludedMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .fieldsExcluded(BeanImpl.class, "propertyCalendar", "propertyFloat", "propertyShort")
            .groupByExcluded(BeanImpl.class, "propertyCalendar", "propertyFloat", "propertyShort");
        assertEquals(query.getSql(), "SELECT propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp FROM tablename GROUP BY propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testJoinMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .join("table2")
            .join("table3");
        assertEquals(query.getSql(), "SELECT * FROM tablename, table2, table3");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testJoinCustomMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .joinCustom("INNER JOIN table3 USING (propertyInt)")
            .joinCustom("CROSS JOIN table2");
        assertEquals(query.getSql(), "SELECT * FROM tablename INNER JOIN table3 USING (propertyInt) CROSS JOIN table2");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testJoinCrossMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .joinCross("table2")
            .joinCross("table3");
        assertEquals(query.getSql(), "SELECT * FROM tablename CROSS JOIN table2 CROSS JOIN table3");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testJoinInnerMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .joinInner("table2", Select.NATURAL, null);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();
        query.from("tablename")
            .joinInner("table2", Select.ON, "tablename.propertyInt = table2.propertyInt");
        assertEquals(query.getSql(), "SELECT * FROM tablename INNER JOIN table2 ON (tablename.propertyInt = table2.propertyInt)");
        assertTrue(execute(query));
        query.clear();
        query.from("tablename")
            .joinInner("table2", Select.USING, "propertyInt");
        assertEquals(query.getSql(), "SELECT * FROM tablename INNER JOIN table2 USING (propertyInt)");
        assertTrue(execute(query));
    }


    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testJoinOuterMysql() {
        var query = new Select(MYSQL);

        query.from("tablename")
            .joinOuter("table2", Select.FULL, Select.NATURAL, null);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();
        query.from("tablename")
            .joinOuter("table2", Select.LEFT, Select.NATURAL, null);
        assertEquals(query.getSql(), "SELECT * FROM tablename NATURAL LEFT OUTER JOIN table2");
        assertTrue(execute(query));
        query.clear();
        query.from("tablename")
            .joinOuter("table2", Select.RIGHT, Select.NATURAL, null);
        assertEquals(query.getSql(), "SELECT * FROM tablename NATURAL RIGHT OUTER JOIN table2");
        assertTrue(execute(query));
        query.clear();

        query.from("tablename")
            .joinOuter("table2", Select.FULL, Select.ON, "tablename.propertyInt = table2.propertyInt");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();
        query.from("tablename")
            .joinOuter("table2", Select.LEFT, Select.ON, "tablename.propertyInt = table2.propertyInt");
        assertEquals(query.getSql(), "SELECT * FROM tablename LEFT OUTER JOIN table2 ON (tablename.propertyInt = table2.propertyInt)");
        assertTrue(execute(query));
        query.clear();
        query.from("tablename")
            .joinOuter("table2", Select.RIGHT, Select.ON, "tablename.propertyInt = table2.propertyInt");
        assertEquals(query.getSql(), "SELECT * FROM tablename RIGHT OUTER JOIN table2 ON (tablename.propertyInt = table2.propertyInt)");
        assertTrue(execute(query));
        query.clear();

        query.from("tablename")
            .joinOuter("table2", Select.FULL, Select.USING, "propertyInt");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();
        query.from("tablename")
            .joinOuter("table2", Select.LEFT, Select.USING, "propertyInt");
        assertEquals(query.getSql(), "SELECT * FROM tablename LEFT OUTER JOIN table2 USING (propertyInt)");
        assertTrue(execute(query));
        query.clear();
        query.from("tablename")
            .joinOuter("table2", Select.RIGHT, Select.USING, "propertyInt");
        assertEquals(query.getSql(), "SELECT * FROM tablename RIGHT OUTER JOIN table2 USING (propertyInt)");
        assertTrue(execute(query));
        query.clear();
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testLimitMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .limit(3);
        assertEquals(query.getSql(), "SELECT * FROM tablename LIMIT 3");
        assertTrue(execute(query));
        query.offset(1);
        assertEquals(query.getSql(), "SELECT * FROM tablename LIMIT 3 OFFSET 1");
        assertTrue(execute(query));
        query.clear();
        query.from("tablename")
            .offset(10);
        assertEquals(query.getSql(), "SELECT * FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testLimitParameterMysql() {
        var query = new Select(MYSQL);
        query.from("tablename")
            .limitParameter("limit");
        assertEquals(query.getSql(), "SELECT * FROM tablename LIMIT ?");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setInt("limit", 3);
            }
        }));

        query.offsetParameter("offset");
        assertEquals(query.getSql(), "SELECT * FROM tablename LIMIT ? OFFSET ?");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setInt("limit", 3)
                    .setInt("offset", 1);
            }
        }));

        query.clear();
        query.from("tablename")
            .offsetParameter("offset");
        assertEquals(query.getSql(), "SELECT * FROM tablename");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testSubselectParamsMysql() {
        // mysql doesn't do subqueries
    }

    // TODO : test fails
//    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
//    public void testCloneMysql() {
//        Select unionquery1 = new Select(MYSQL);
//        unionquery1
//            .from("table2")
//            .field("table2.propertyString")
//            .field("table2.propertyShort")
//            .field("table2.propertyLong")
//            .field("table2.propertyString")
//            .field("max(table2.propertyByte)")
//            .whereParameter("propertyByte", "=")
//            .groupBy("propertyString");
//        Select unionquery2 = new Select(MYSQL);
//        unionquery2
//            .from("table2")
//            .field("table2.propertyStringBuffer")
//            .field("table2.propertyShort")
//            .field("table2.propertyLong")
//            .field("table2.propertyString")
//            .field("min(table2.propertyByte)")
//            .whereParameter("propertyByte", ">")
//            .groupBy("propertyString");
//        Select query = new Select(MYSQL);
//        query
//            .hint("SQL_CACHE")
//            .from("tablename")
//            .joinCross("table3")
//            .joinOuter("table2", Select.RIGHT, Select.ON, "table3.propertyInt = table2.propertyInt")
//            .distinct()
//            .field("tablename.propertyString")
//            .field("tablename.propertyShort")
//            .field("tablename.propertyLong")
//            .field("tablename.propertyString")
//            .field("tablename.propertyByte")
//            .whereParameter("tablename.propertyString", "propertyString", "=")
//            .whereOr("tablename.propertyByte", "=", (byte) 54)
//            .whereAnd("tablename.propertyDouble", "!=", 73453.71d)
//            .whereParameterOr("tablename.propertyInt", "propertyInt", "=")
//            .whereParameterAnd("tablename.propertyLong", "propertyLong", "<")
//            .whereParameterOr("tablename.propertyChar", "propertyChar", "=")
//            .groupBy("tablename.propertyShort")
//            .groupBy("tablename.propertyLong")
//            .groupBy("tablename.propertyString")
//            .groupBy("tablename.propertyByte")
//            .having("tablename.propertyLong = 1")
//            .unionAll(unionquery1)
//            .union(unionquery2)
//            .limit(3)
//            .offset(1);
//        Select query_clone = query.clone();
//        assertEquals(query.getSql(), query_clone.getSql());
//        assertNotSame(query, query_clone);
//        execute(query, new DbPreparedStatementHandler() {
//            public void setParameters(DbPreparedStatement statement) {
//                statement
//                    .setInt("propertyInt", 1)
//                    .setLong("propertyLong", 99999999)
//                    .setString("propertyString", "thestring")
//                    .setByte("propertyByte", (byte) 4)
//                    .setString("propertyChar", "c");
//            }
//        });
//    }
}


