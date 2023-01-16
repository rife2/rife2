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
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestInsertHsqldb extends TestInsert {
    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInstantiationHsqldb() {
        var query = new Insert(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Insert");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testIncompleteQueryHsqldb() {
        var query = new Insert(HSQLDB);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Insert");
        }
        query.into("tablename");
        try {
            query.getSql();
            fail();
        } catch (FieldsRequiredException e) {
            assertEquals(e.getQueryName(), "Insert");
        }
        query.field("col1", "val1");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testClearHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .field("col1", "val1");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Insert");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testHintHsqldb() {
        var query = new Insert(HSQLDB)
            .hint("NO_INDEX")
            .into("tablename")
            .fieldParameter("col1");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testParameterHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldParameter("col1");
        assertEquals(query.getSql(), "INSERT INTO tablename (col1) VALUES (?)");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldHsqldb() {
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        var query = new Insert(HSQLDB);
        query.into("tablename")
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
        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (NULL, 98347.876438637, true, 16, '2002-08-19 12:17:52.462', 'M', '2002-08-19 12:17:52.462', 12.3, 13.4, 34, 45, 12, '2002-08-19', 'string''value', 'stringbuffer''value', '12:17:52', '2002-08-19 12:17:52.462')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldCustomHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldCustom("propertySqlDate", "now()");
        assertEquals(query.getSql(), "INSERT INTO tablename (propertySqlDate) VALUES (now())");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsHsqldb() {
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        var query = new Insert(HSQLDB);
        query.into("tablename")
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
                "propertyString", "string'value",
                "propertyStringBuffer", new StringBuffer("stringbuffer'value"),
                "propertyTime", Convert.toSqlTime(cal),
                "propertyTimestamp", Convert.toSqlTimestamp(cal)
            });
        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (NULL, 98347.876438637, true, 16, '2002-08-19 12:17:52.462', 'M', '2002-08-19 12:17:52.462', 12.3, 13.4, 34, 45, 12, '2002-08-19', 'string''value', 'stringbuffer''value', '12:17:52', '2002-08-19 12:17:52.462')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldParametersHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename");

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
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{
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
            "propertyTimestamp"});

        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldParametersMixedHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename");

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
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{
            "nullColumn",
            "propertyBoolean",
            "propertyByte",
            "propertyChar",
            "propertyFloat",
            "propertyInt",
            "propertySqlDate",
            "propertyString",
            "propertyTimestamp"});

        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (?, 98347.876438637, ?, ?, '2002-08-19 12:17:52.462', ?, '2002-08-19 12:17:52.462', 12.3, ?, ?, 45, 12, ?, ?, 'stringbuffer''value', '12:17:52', ?)");

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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsBeanHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fields(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShort, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (219038743.392874, true, false, 89, 34, '2002-06-18 15:26:14.167', 'v', 'r', '2002-06-18 15:26:14.167', 53348.34, 143298.692, 'VALUE_THREE', 98634.2, 8734.7, '2002-06-18 15:26:14.167', 545, 968, '2002-06-18', '2002-06-18 15:26:14.167', '15:26:14', 34563, 66875, 43, 68, '2002-06-18', 'someotherstring', 'someotherstringbuff', '15:26:14', '2002-06-18 15:26:14.167')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsBeanConstrainedHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fields(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLongObject, propertyShort, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (219038743.392874, true, false, 34, '2002-06-18 15:26:14.167', 'v', 'r', '2002-06-18 15:26:14.167', 53348.34, 143298.692, 98634.2, 8734.7, '2002-06-18 15:26:14.167', 545, 968, '2002-06-18', '2002-06-18 15:26:14.167', '15:26:14', 66875, 43, '2002-06-18', 'someotherstring', 'someotherstringbuff', '15:26:14', '2002-06-18 15:26:14.167')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsBeanNullValuesHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fields(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyDouble, propertyDoubleObject, propertyFloat, propertyFloatObject, propertyInt, propertyIntegerObject, propertyLong, propertyLongObject, propertyShort, propertyShortObject) VALUES (false, false, 0, 0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 0)");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsBeanIncludedHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldsIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyByte, propertyDouble, propertyShort, propertyStringBuffer, propertyTime) VALUES (89, 53348.34, 43, 'someotherstringbuff', '15:26:14')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsBeanExcludedHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldsExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShortObject, propertySqlDate, propertyString, propertyTimestamp) VALUES (219038743.392874, true, false, 34, '2002-06-18 15:26:14.167', 'v', 'r', '2002-06-18 15:26:14.167', 143298.692, 'VALUE_THREE', 98634.2, 8734.7, '2002-06-18 15:26:14.167', 545, 968, '2002-06-18', '2002-06-18 15:26:14.167', '15:26:14', 34563, 66875, 68, '2002-06-18', 'someotherstring', '2002-06-18 15:26:14.167')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsBeanFilteredHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldsFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyDouble, propertyStringBuffer) VALUES (53348.34, 'someotherstringbuff')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testMultipleRowsHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .field("propertyChar", 'M')
            .field("propertyDouble", 12.3d)
            .field("propertyFloat", 13.4f)
            .field("propertyInt", 34);
        query.field("propertyChar", 'S')
            .field("propertyDouble", 45.1d)
            .field("propertyFloat", 27.9f);
        query.field("propertyChar", 'T');
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsParametersBeanHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldsParameters(BeanImpl.class);
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLong, propertyLongObject, propertyShort, propertyShortObject, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsParametersBeanConstrainedHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldsParameters(BeanImplConstrained.class);
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyFloat, propertyFloatObject, propertyInstant, propertyInt, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLongObject, propertyShort, propertySqlDate, propertyString, propertyStringBuffer, propertyTime, propertyTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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
                cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 167);
                statement
                    .setBigDecimal(1, new BigDecimal("219038743.392874"))
                    .setBoolean(2, true)
                    .setBoolean(3, false)
                    .setByte(4, (byte) 34)
                    .setTimestamp(5, Convert.toSqlTimestamp(cal))
                    .setString(6, "v")
                    .setString(7, "r")
                    .setTimestamp(8, Convert.toSqlTimestamp(cal))
                    .setDouble(9, 53348.34d)
                    .setDouble(10, 143298.692d)
                    .setDouble(11, 98634.2d)
                    .setDouble(12, 8734.7d)
                    .setTimestamp(13, Convert.toSqlTimestamp(cal))
                    .setInt(14, 545)
                    .setInt(15, 968)
                    .setDate(16, Convert.toSqlDate(cal))
                    .setTimestamp(17, Convert.toSqlTimestamp(cal))
                    .setTime(18, Convert.toSqlTime(cal))
                    .setLong(19, 66875L)
                    .setShort(20, (short) 43)
                    .setDate(21, Convert.toSqlDate(cal))
                    .setString(22, "someotherstring")
                    .setString(23, "someotherstringbuff")
                    .setTime(24, Convert.toSqlTime(cal))
                    .setTimestamp(25, Convert.toSqlTimestamp(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testFieldsParametersBeanExcludedHsqldb() {
        var query = new Insert(HSQLDB);
        query.into("tablename")
            .fieldsParametersExcluded(BeanImpl.class,
                new String[]{"propertyBoolean", "propertyByte", "propertyChar",
                    "propertyDouble", "propertyInt", "propertyLong",
                    "propertySqlDate", "propertyStringBuffer", "propertyTimestamp"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyCharacterObject, propertyDate, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInstant, propertyIntegerObject, propertyLocalDate, propertyLocalDateTime, propertyLocalTime, propertyLongObject, propertyShort, propertyShortObject, propertyString, propertyTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInsertSubselectParamsHsqldb() {
        var fieldquery = new Select(HSQLDB);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");

        // Manual subselect creation
        var query = new Insert(HSQLDB);
        // shuffled the structure around a bit to test the correct order usage
        query
            .into("tablename")
            .fieldParameter("propertyString")
            .fieldCustom("propertyLong", "(" + fieldquery + ")")
            .fieldSubselect(fieldquery);
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyString, propertyLong) VALUES (?, (SELECT max(propertyLong) FROM table2 WHERE propertyInt > ?))");
        var parameters = query.getParameters().getOrderedNamesArray();
        assertEquals(2, parameters.length);
        assertEquals(parameters[0], "propertyString");
        assertEquals(parameters[1], "propertyInt");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "thestring")
                    .setLong("propertyInt", 90);
            }
        }));

        // Automated subselect creation
        query = new Insert(HSQLDB);
        // shuffled the structure around a bit to test the correct order usage
        query
            .into("tablename")
            .fieldParameter("propertyString")
            .field("propertyLong", fieldquery);
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyString, propertyLong) VALUES (?, (SELECT max(propertyLong) FROM table2 WHERE propertyInt > ?))");
        parameters = query.getParameters().getOrderedNamesArray();
        assertEquals(2, parameters.length);
        assertEquals(parameters[0], "propertyString");
        assertEquals(parameters[1], "propertyInt");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "thestring")
                    .setLong("propertyInt", 90);
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCloneHsqldb() {
        var fieldquery = new Select(HSQLDB);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");

        final var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        var query = new Insert(HSQLDB);
        query.into("tablename")
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
            .fieldSubselect(fieldquery);

        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        assertTrue(execute(query, new DbPreparedStatementHandler() {
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
                    .setTimestamp("propertyTimestamp", Convert.toSqlTimestamp(cal));
            }
        }));
    }
}

