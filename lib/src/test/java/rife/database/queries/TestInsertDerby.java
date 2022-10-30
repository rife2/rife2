/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.BeanImpl;
import rife.database.BeanImplConstrained;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.exceptions.FieldsRequiredException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.database.types.SqlNull;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestInsertDerby extends TestInsert {
    @Test
    public void testInstantiationDerby() {
        Insert query = new Insert(DERBY);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Insert");
        }
    }

    @Test
    public void testIncompleteQueryDerby() {
        Insert query = new Insert(DERBY);
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

    @Test
    public void testClearDerby() {
        Insert query = new Insert(DERBY);
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

    @Test
    public void testHintDerby() {
        Insert query = new Insert(DERBY)
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

    @Test
    public void testParameterDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldParameter("col1");
        assertEquals(query.getSql(), "INSERT INTO tablename (col1) VALUES (?)");
    }

    @Test
    public void testFieldDerby() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 7, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        Insert query = new Insert(DERBY);
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
            .field("propertySqlDate", new java.sql.Date(cal.getTime().getTime()))
            .field("propertyString", "string'value")
            .field("propertyStringbuffer", new StringBuffer("stringbuffer'value"))
            .field("propertyTime", new Time(cal.getTime().getTime()))
            .field("propertyTimestamp", new Timestamp(cal.getTime().getTime()));
        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (NULL, 98347.876438637, 1, 16, '2002-08-19 12:17:52.462', 'M', '2002-08-19 12:17:52.462', 12.3, 13.4, 34, 45, 12, '2002-08-19', 'string''value', 'stringbuffer''value', '12:17:52', '2002-08-19 12:17:52.462')");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldCustomDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldCustom("propertySqlDate", "CURRENT_DATE");
        assertEquals(query.getSql(), "INSERT INTO tablename (propertySqlDate) VALUES (CURRENT_DATE)");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldsDerby() {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 7, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        Insert query = new Insert(DERBY);
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
                "propertySqlDate", new java.sql.Date(cal.getTime().getTime()),
                "propertyString", new String("string'value"),
                "propertyStringbuffer", new StringBuffer("stringbuffer'value"),
                "propertyTime", new Time(cal.getTime().getTime()),
                "propertyTimestamp", new Timestamp(cal.getTime().getTime())
            });
        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (NULL, 98347.876438637, 1, 16, '2002-08-19 12:17:52.462', 'M', '2002-08-19 12:17:52.462', 12.3, 13.4, 34, 45, 12, '2002-08-19', 'string''value', 'stringbuffer''value', '12:17:52', '2002-08-19 12:17:52.462')");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldParametersDerby() {
        Insert query = new Insert(DERBY);
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
            .fieldParameter("propertyStringbuffer")
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
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyStringbuffer");
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
            "propertyStringbuffer",
            "propertyTime",
            "propertyTimestamp"});

        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                Calendar cal = Calendar.getInstance();
                cal.set(2002, 7, 19, 12, 17, 52);
                cal.set(Calendar.MILLISECOND, 462);
                statement
                    .setString(1, null)
                    .setBigDecimal(2, new BigDecimal("98347.876438637"))
                    .setBoolean(3, true)
                    .setByte(4, (byte) 16)
                    .setDate(5, new java.sql.Date(cal.getTime().getTime()))
                    .setString(6, "M")
                    .setDate(7, new java.sql.Date(cal.getTime().getTime()))
                    .setDouble(8, 12.3d)
                    .setFloat(9, 13.4f)
                    .setInt(10, 34)
                    .setLong(11, 45L)
                    .setShort(12, (short) 12)
                    .setDate(13, new java.sql.Date(cal.getTime().getTime()))
                    .setString(14, "string'value")
                    .setString(15, "string'value2")
                    .setTime(16, new Time(cal.getTime().getTime()))
                    .setTimestamp(17, new Timestamp(cal.getTime().getTime()));
            }
        }));
    }

    @Test
    public void testFieldParametersMixedDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename");

        assertNull(query.getParameters());

        final Calendar cal = Calendar.getInstance();
        cal.set(2002, 7, 19, 12, 17, 52);
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
            .field("propertyStringbuffer", new StringBuffer("stringbuffer'value"))
            .field("propertyTime", new Time(cal.getTime().getTime()))
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

        assertEquals(query.getSql(), "INSERT INTO tablename (nullColumn, propertyBigDecimal, propertyBoolean, propertyByte, propertyCalendar, propertyChar, propertyDate, propertyDouble, propertyFloat, propertyInt, propertyLong, propertyShort, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (?, 98347.876438637, ?, ?, '2002-08-19 12:17:52.462', ?, '2002-08-19 12:17:52.462', 12.3, ?, ?, 45, 12, ?, ?, 'stringbuffer''value', '12:17:52', ?)");
        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString(1, null)
                    .setBoolean(2, true)
                    .setByte(3, (byte) 16)
                    .setString(4, "M")
                    .setFloat(5, 13.4f)
                    .setInt(6, 34)
                    .setDate(7, new java.sql.Date(cal.getTime().getTime()))
                    .setString(8, "string'value")
                    .setTimestamp(9, new Timestamp(cal.getTime().getTime()));
            }
        }));
    }

    @Test
    public void testFieldsBeanDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fields(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInt, propertyIntegerObject, propertyLong, propertyLongObject, propertyShort, propertyShortObject, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (219038743.392874, 1, 0, 89, 34, '2002-06-18 15:26:14.764', 'v', 'r', '2002-06-18 15:26:14.764', 53348.34, 143298.692, 'VALUE_THREE', 98634.2, 8734.7, 545, 968, 34563, 66875, 43, 68, '2002-06-18', 'someotherstring', 'someotherstringbuff', '15:26:14', '2002-06-18 15:26:14.764')");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldsBeanConstrainedDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fields(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyFloat, propertyFloatObject, propertyInt, propertyIntegerObject, propertyLongObject, propertyShort, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (219038743.392874, 1, 0, 34, '2002-06-18 15:26:14.764', 'v', 'r', '2002-06-18 15:26:14.764', 53348.34, 143298.692, 98634.2, 8734.7, 545, 968, 66875, 43, '2002-06-18', 'someotherstring', 'someotherstringbuff', '15:26:14', '2002-06-18 15:26:14.764')");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldsBeanNullValuesDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fields(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyDouble, propertyDoubleObject, propertyFloat, propertyFloatObject, propertyInt, propertyIntegerObject, propertyLong, propertyLongObject, propertyShort, propertyShortObject) VALUES (0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 0)");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldsBeanIncludedDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldsIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringbuffer", "propertyTime"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyByte, propertyDouble, propertyShort, propertyStringbuffer, propertyTime) VALUES (89, 53348.34, 43, 'someotherstringbuff', '15:26:14')");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldsBeanExcludedDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldsExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringbuffer", "propertyTime"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInt, propertyIntegerObject, propertyLong, propertyLongObject, propertyShortObject, propertySqlDate, propertyString, propertyTimestamp) VALUES (219038743.392874, 1, 0, 34, '2002-06-18 15:26:14.764', 'v', 'r', '2002-06-18 15:26:14.764', 143298.692, 'VALUE_THREE', 98634.2, 8734.7, 545, 968, 34563, 66875, 68, '2002-06-18', 'someotherstring', '2002-06-18 15:26:14.764')");
        assertTrue(execute(query));
    }

    @Test
    public void testFieldsBeanFilteredDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldsFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringbuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyDouble, propertyStringbuffer) VALUES (53348.34, 'someotherstringbuff')");
        assertTrue(execute(query));
    }

    @Test
    public void testMultipleRowsDerby() {
        Insert query = new Insert(DERBY);
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

    @Test
    public void testFieldsParametersBeanDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldsParameters(BeanImpl.class);
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByte, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyInt, propertyIntegerObject, propertyLong, propertyLongObject, propertyShort, propertyShortObject, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        assertEquals(query.getParameters().getOrderedNames().size(), 25);
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
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyShortObject");
        assertEquals(query.getParameters().getOrderedNames().get(20), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(21), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(22), "propertyStringbuffer");
        assertEquals(query.getParameters().getOrderedNames().get(23), "propertyTime");
        assertEquals(query.getParameters().getOrderedNames().get(24), "propertyTimestamp");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBoolean", "propertyBooleanObject", "propertyByte", "propertyByteObject", "propertyCalendar", "propertyChar", "propertyCharacterObject", "propertyDate", "propertyDouble", "propertyDoubleObject", "propertyEnum", "propertyFloat", "propertyFloatObject", "propertyInt", "propertyIntegerObject", "propertyLong", "propertyLongObject", "propertyShort", "propertyShortObject", "propertySqlDate", "propertyString", "propertyStringbuffer", "propertyTime", "propertyTimestamp"});

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                Calendar cal = Calendar.getInstance();
                cal.set(2002, 7, 19, 12, 17, 52);
                cal.set(Calendar.MILLISECOND, 462);
                statement
                    .setBigDecimal(1, new BigDecimal("98347.876438637"))
                    .setBoolean(2, false)
                    .setBoolean(3, true)
                    .setByte(4, (byte) 16)
                    .setByte(5, (byte) 72)
                    .setTimestamp(6, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setString(7, "M")
                    .setString(8, "p")
                    .setTimestamp(9, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setDouble(10, 12.3d)
                    .setDouble(11, 68.7d)
                    .setString(12, "VALUE_THREE")
                    .setFloat(13, 13.4f)
                    .setFloat(14, 42.1f)
                    .setInt(15, 92)
                    .setInt(16, 34)
                    .setLong(17, 687L)
                    .setLong(18, 92)
                    .setShort(19, (short) 7)
                    .setShort(20, (short) 12)
                    .setDate(21, new java.sql.Date(cal.getTime().getTime()))
                    .setString(22, "string'value")
                    .setString(23, "string'value2")
                    .setTime(24, new Time(cal.getTime().getTime()))
                    .setTimestamp(25, new Timestamp(cal.getTime().getTime()));
            }
        }));
    }

    @Test
    public void testFieldsParametersBeanConstrainedDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldsParameters(BeanImplConstrained.class);
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBoolean, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyChar, propertyCharacterObject, propertyDate, propertyDouble, propertyDoubleObject, propertyFloat, propertyFloatObject, propertyInt, propertyIntegerObject, propertyLongObject, propertyShort, propertySqlDate, propertyString, propertyStringbuffer, propertyTime, propertyTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        assertEquals(query.getParameters().getOrderedNames().size(), 21);
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
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(16), "propertySqlDate");
        assertEquals(query.getParameters().getOrderedNames().get(17), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(18), "propertyStringbuffer");
        assertEquals(query.getParameters().getOrderedNames().get(19), "propertyTime");
        assertEquals(query.getParameters().getOrderedNames().get(20), "propertyTimestamp");
        assertTrue(Arrays.equals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBoolean", "propertyBooleanObject", "propertyByteObject", "propertyCalendar", "propertyChar", "propertyCharacterObject", "propertyDate", "propertyDouble", "propertyDoubleObject", "propertyFloat", "propertyFloatObject", "propertyInt", "propertyIntegerObject", "propertyLongObject", "propertyShort", "propertySqlDate", "propertyString", "propertyStringbuffer", "propertyTime", "propertyTimestamp"}));

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                Calendar cal = Calendar.getInstance();
                cal.set(2002, 5, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 764);
                statement
                    .setBigDecimal(1, new BigDecimal("219038743.392874"))
                    .setBoolean(2, true)
                    .setBoolean(3, false)
                    .setByte(4, (byte) 34)
                    .setTimestamp(5, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setString(6, "v")
                    .setString(7, "r")
                    .setTimestamp(8, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setDouble(9, 53348.34d)
                    .setDouble(10, 143298.692d)
                    .setFloat(11, 98634.2f)
                    .setFloat(12, 8734.7f)
                    .setInt(13, 545)
                    .setInt(14, 968)
                    .setLong(15, 66875L)
                    .setShort(16, (short) 43)
                    .setDate(17, new java.sql.Date(cal.getTime().getTime()))
                    .setString(18, "someotherstring")
                    .setString(19, "someotherstringbuff")
                    .setTime(20, new Time(cal.getTime().getTime()))
                    .setTimestamp(21, new Timestamp(cal.getTime().getTime()));
            }
        }));
    }

    @Test
    public void testFieldsParametersBeanExcludedDerby() {
        Insert query = new Insert(DERBY);
        query.into("tablename")
            .fieldsParametersExcluded(BeanImpl.class,
                new String[]{"propertyBoolean", "propertyByte", "propertyChar",
                    "propertyDouble", "propertyInt", "propertyLong",
                    "propertySqlDate", "propertyStringbuffer", "propertyTimestamp"});
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyBigDecimal, propertyBooleanObject, propertyByteObject, propertyCalendar, propertyCharacterObject, propertyDate, propertyDoubleObject, propertyEnum, propertyFloat, propertyFloatObject, propertyIntegerObject, propertyLongObject, propertyShort, propertyShortObject, propertyString, propertyTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        assertEquals(query.getParameters().getOrderedNames().size(), 16);
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
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyShortObject");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(15), "propertyTime");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBooleanObject", "propertyByteObject", "propertyCalendar", "propertyCharacterObject", "propertyDate", "propertyDoubleObject", "propertyEnum", "propertyFloat", "propertyFloatObject", "propertyIntegerObject", "propertyLongObject", "propertyShort", "propertyShortObject", "propertyString", "propertyTime"});

        assertTrue(execute(query, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                Calendar cal = Calendar.getInstance();
                cal.set(2002, 7, 19, 12, 17, 52);
                cal.set(Calendar.MILLISECOND, 462);
                statement
                    .setBigDecimal(1, new BigDecimal("98347.876438637"))
                    .setBoolean(2, true)
                    .setByte(3, (byte) 72)
                    .setTimestamp(4, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setString(5, "o")
                    .setTimestamp(6, new java.sql.Timestamp(cal.getTime().getTime()))
                    .setDouble(7, 86.7d)
                    .setString(8, "VALUE_THREE")
                    .setFloat(9, 13.4f)
                    .setFloat(10, 32.8f)
                    .setInt(11, 358)
                    .setLong(12, 9680L)
                    .setShort(13, (short) 12)
                    .setShort(14, (short) 78)
                    .setString(15, "string'value")
                    .setTime(16, new Time(cal.getTime().getTime()));
            }
        }));
    }

    @Test
    public void testInsertSubselectParamsDerby() {
        Select fieldquery = new Select(DERBY);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");

        // Manual subselect creation
        Insert query = new Insert(DERBY);
        // shuffled the structure around a bit to test the correct order usage
        query
            .into("tablename")
            .fieldParameter("propertyString")
            .fieldCustom("propertyLong", "(" + fieldquery + ")")
            .fieldSubselect(fieldquery);
        assertEquals(query.getSql(), "INSERT INTO tablename (propertyString, propertyLong) VALUES (?, (SELECT max(propertyLong) FROM table2 WHERE propertyInt > ?))");
        String[] parameters = query.getParameters().getOrderedNamesArray();
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
        query = new Insert(DERBY);
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

    @Test
    public void testCloneDerby() {
        Select fieldquery = new Select(DERBY);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");

        final Calendar cal = Calendar.getInstance();
        cal.set(2002, 7, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        Insert query = new Insert(DERBY);
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
            .field("propertyStringbuffer", new StringBuffer("stringbuffer'value"))
            .field("propertyTime", new Time(cal.getTime().getTime()))
            .fieldParameter("propertyTimestamp")
            .fieldCustom("propertyLong", "(" + fieldquery + ")")
            .fieldSubselect(fieldquery);

        Insert query_clone = query.clone();
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
                    .setDate("propertySqlDate", new java.sql.Date(cal.getTime().getTime()))
                    .setString("propertyString", "string'value")
                    .setTimestamp("propertyTimestamp", new Timestamp(cal.getTime().getTime()));
            }
        }));
    }
}

