/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.FieldsRequiredException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.types.SqlNull;
import rife.tools.Convert;

import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestUpdateOracle extends TestUpdate {
    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testInstantiationOracle() {
        var query = new Update(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "Update");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testIncompleteQueryOracle() {
        var query = new Update(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testClearOracle() {
        var query = new Update(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testHintOracle() {
        var query = new Update(ORACLE)
            .hint("NO_INDEX")
            .table("tablename")
            .field("propertyInt", 34);
        assertEquals(query.getSql(), "UPDATE /*+ NO_INDEX */ tablename SET propertyInt = 34");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldOracle() {
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        var query = new Update(ORACLE);
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
        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = NULL, propertyBigDecimal = 98347.876438637, propertyBoolean = 1, propertyByte = 16, propertyCalendar = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS'), propertyChar = 'M', propertyDate = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS'), propertyDouble = 12.3, propertyFloat = 13.4, propertyInt = 34, propertyLong = 45, propertyShort = 12, propertySqlDate = TO_DATE('2002/08/19 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyString = 'string''value', propertyStringBuffer = 'stringbuffer''value', propertyTime = TO_DATE('12:17:52', 'HH24:MI:SS'), propertyTimestamp = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS') WHERE propertyByte = 89");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldCustomOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .fieldCustom("propertySqlDate", "(SELECT sysdate FROM dual)");
        assertEquals(query.getSql(), "UPDATE tablename SET propertySqlDate = (SELECT sysdate FROM dual)");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldParametersOracle() {
        var query = new Update(ORACLE);
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

        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = ?, propertyBigDecimal = ?, propertyBoolean = ?, propertyByte = ?, propertyCalendar = ?, propertyChar = ?, propertyDate = ?, propertyDouble = ?, propertyFloat = ?, propertyInt = ?, propertyLong = ?, propertyShort = ?, propertySqlDate = ?, propertyString = ?, propertyStringBuffer = ?, propertyTime = ?, propertyTimestamp = ?");

        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldParametersMixedOracle() {
        var query = new Update(ORACLE);
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

        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = ?, propertyBigDecimal = 98347.876438637, propertyBoolean = ?, propertyByte = ?, propertyCalendar = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS'), propertyChar = ?, propertyDate = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS'), propertyDouble = 12.3, propertyFloat = ?, propertyInt = ?, propertyLong = 45, propertyShort = 12, propertySqlDate = ?, propertyString = ?, propertyStringBuffer = 'stringbuffer''value', propertyTime = TO_DATE('12:17:52', 'HH24:MI:SS'), propertyTimestamp = ?");

        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsOracle() {
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        var query = new Update(ORACLE);
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
        assertEquals(query.getSql(), "UPDATE tablename SET nullColumn = NULL, propertyBigDecimal = 98347.876438637, propertyBoolean = 1, propertyByte = 16, propertyCalendar = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS'), propertyChar = 'M', propertyDate = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS'), propertyDouble = 12.3, propertyFloat = 13.4, propertyInt = 34, propertyLong = 45, propertyShort = 12, propertySqlDate = TO_DATE('2002/08/19 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyString = 'string''value', propertyStringBuffer = 'stringbuffer''value', propertyTime = TO_DATE('12:17:52', 'HH24:MI:SS'), propertyTimestamp = TO_DATE('2002/08/19 12:17:52', 'YYYY/MM/DD HH24:MI:SS') WHERE propertyByte = 89");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereConstructionOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where("propertyInt = 545")
            .whereAnd("propertyLong < 50000")
            .whereOr("propertyChar = 'v'");
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyInt = 545 AND propertyLong < 50000 OR propertyChar = 'v'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereConstructionGroupOracle() {
        var query = new Update(ORACLE);
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
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyString", "propertyStringBuffer"});

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyInt = 545 AND propertyLong < 50000 OR (propertyString = ? AND propertyByte <= 0 AND (propertyBoolean != 1 OR propertyStringBuffer LIKE ?)) OR propertyChar = 'v'");

        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "someotherstring")
                    .setString("propertyStringBuffer", "stringbuff");
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereTypedOracle() {
        var query = new Update(ORACLE);
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

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = 0 OR propertyByte = 54 AND propertyCalendar <= TO_DATE('2003/03/03 10:01:28', 'YYYY/MM/DD HH24:MI:SS') OR propertyChar = 'f' AND propertyDate = TO_DATE('2003/03/03 10:01:28', 'YYYY/MM/DD HH24:MI:SS') AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = TO_DATE('2003/03/03 00:00:00', 'YYYY/MM/DD HH24:MI:SS') AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = TO_DATE('10:01:28', 'HH24:MI:SS') AND propertyTimestamp <= TO_DATE('2003/03/03 10:01:28', 'YYYY/MM/DD HH24:MI:SS')");
        assertFalse(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereTypedMixedOracle() {
        var query = new Update(ORACLE);
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

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal >= 53443433.9784567 AND propertyBoolean = 0 OR propertyByte = 54 AND propertyCalendar <= TO_DATE('2003/03/03 10:01:28', 'YYYY/MM/DD HH24:MI:SS') OR propertyChar = 'f' AND propertyDate = TO_DATE('2003/03/03 10:01:28', 'YYYY/MM/DD HH24:MI:SS') AND propertyDouble != 73453.71 OR propertyFloat >= 1987.14 AND propertyInt = 973 AND propertyLong < 347678 AND propertyShort = 78 OR propertySqlDate = ? AND propertyString LIKE 'someotherstring%' AND propertyStringBuffer = 'someotherstringbuff' OR propertyTime = TO_DATE('10:01:28', 'HH24:MI:SS') AND propertyTimestamp <= TO_DATE('2003/03/03 10:01:28', 'YYYY/MM/DD HH24:MI:SS')");

        assertFalse(execute(query, new DbPreparedStatementHandler<>() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setDate("propertySqlDate", Convert.toSqlDate(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereParametersOracle() {
        var query = new Update(ORACLE);
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
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyInt", "propertyLong", "propertyChar"});

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyInt = ? AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setInt(1, 545)
                    .setLong(2, 50000)
                    .setString(3, "v");
            }
        }));

        query.where("propertyInt = 545");

        assertNull(query.getParameters());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereParametersMixedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where("propertyInt = 545")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyChar");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyLong", "propertyChar"});

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyInt = 545 AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setLong(1, 50000)
                    .setString(2, "v");
            }
        }));

        query.where("propertyInt = 545");

        assertNull(query.getParameters());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldWhereParametersOracle() {
        var query = new Update(ORACLE);
        query.table("tablename");

        assertNull(query.getParameters());

        query.fieldParameter("propertyBoolean")
            .fieldParameter("propertyByte");

        assertEquals(query.getParameters().getOrderedNames().size(), 2);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyByte");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBoolean", "propertyByte"});

        query.whereParameter("propertyInt", "=")
            .whereParameterAnd("propertyLong", "<")
            .whereParameterOr("propertyChar", "=");

        assertEquals(query.getParameters().getOrderedNames().size(), 5);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBoolean");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyByte");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyInt");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyLong");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyChar");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBoolean", "propertyByte", "propertyInt", "propertyLong", "propertyChar"});

        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = ?, propertyByte = ? WHERE propertyInt = ? AND propertyLong < ? OR propertyChar = ?");
        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
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
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBoolean", "propertyByte"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = ?, propertyByte = ? WHERE propertyInt = 545");
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsBeanOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .where("propertyInt = 545")
            .fields(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = 219038743.392874, propertyBoolean = 1, propertyBooleanObject = 0, propertyByte = 89, propertyByteObject = 34, propertyCalendar = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyChar = 'v', propertyCharacterObject = 'r', propertyDate = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyDouble = 53348.34, propertyDoubleObject = 143298.692, propertyEnum = 'VALUE_THREE', propertyFloat = 98634.2, propertyFloatObject = 8734.7, propertyInstant = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyInt = 545, propertyIntegerObject = 968, propertyLocalDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyLocalDateTime = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyLocalTime = TO_DATE('15:26:14', 'HH24:MI:SS'), propertyLong = 34563, propertyLongObject = 66875, propertyShort = 43, propertyShortObject = 68, propertySqlDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyString = 'someotherstring', propertyStringBuffer = 'someotherstringbuff', propertyTime = TO_DATE('15:26:14', 'HH24:MI:SS'), propertyTimestamp = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsBeanConstrainedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .where("propertyInt = 545")
            .fields(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = 219038743.392874, propertyBoolean = 1, propertyBooleanObject = 0, propertyByteObject = 34, propertyCalendar = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyChar = 'v', propertyCharacterObject = 'r', propertyDate = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyDouble = 53348.34, propertyDoubleObject = 143298.692, propertyFloat = 98634.2, propertyFloatObject = 8734.7, propertyInstant = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyInt = 545, propertyIntegerObject = 968, propertyLocalDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyLocalDateTime = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyLocalTime = TO_DATE('15:26:14', 'HH24:MI:SS'), propertyLongObject = 66875, propertyShort = 43, propertySqlDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyString = 'someotherstring', propertyStringBuffer = 'someotherstringbuff', propertyTime = TO_DATE('15:26:14', 'HH24:MI:SS'), propertyTimestamp = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsBeanNullValuesOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .where("propertyInt = 545")
            .fields(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 0, propertyBooleanObject = 0, propertyByte = 0, propertyByteObject = 0, propertyDouble = 0.0, propertyDoubleObject = 0.0, propertyFloat = 0.0, propertyFloatObject = 0.0, propertyInt = 0, propertyIntegerObject = 0, propertyLong = 0, propertyLongObject = 0, propertyShort = 0, propertyShortObject = 0 WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsBeanIncludedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .where("propertyInt = 545")
            .fieldsIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyByte = 89, propertyDouble = 53348.34, propertyShort = 43, propertyStringBuffer = 'someotherstringbuff', propertyTime = TO_DATE('15:26:14', 'HH24:MI:SS') WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsBeanExcludedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .where("propertyInt = 545")
            .fieldsExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBigDecimal = 219038743.392874, propertyBoolean = 1, propertyBooleanObject = 0, propertyByteObject = 34, propertyCalendar = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyChar = 'v', propertyCharacterObject = 'r', propertyDate = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyDoubleObject = 143298.692, propertyEnum = 'VALUE_THREE', propertyFloat = 98634.2, propertyFloatObject = 8734.7, propertyInstant = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyInt = 545, propertyIntegerObject = 968, propertyLocalDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyLocalDateTime = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS'), propertyLocalTime = TO_DATE('15:26:14', 'HH24:MI:SS'), propertyLong = 34563, propertyLongObject = 66875, propertyShortObject = 68, propertySqlDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS'), propertyString = 'someotherstring', propertyTimestamp = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsBeanFilteredOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .where("propertyInt = 545")
            .fieldsFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyDouble = 53348.34, propertyStringBuffer = 'someotherstringbuff' WHERE propertyInt = 545");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsParametersBeanOracle() {
        var query = new Update(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsParametersBeanConstrainedOracle() {
        var query = new Update(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testFieldsParametersBeanExcludedOracle() {
        var query = new Update(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereBeanOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where(BeanImpl.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = 1 AND propertyBooleanObject = 0 AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS') AND propertyLocalDateTime = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyLocalTime = TO_DATE('15:26:14', 'HH24:MI:SS') AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShort = 43 AND propertyShortObject = 68 AND propertySqlDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS') AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = TO_DATE('15:26:14', 'HH24:MI:SS') AND propertyTimestamp = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereBeanConstrainedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where(BeanImplConstrained.getPopulatedBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = 1 AND propertyBooleanObject = 0 AND propertyByte = 89 AND propertyByteObject = 34 AND propertyCalendar = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyDouble = 53348.34 AND propertyDoubleObject = 143298.692 AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS') AND propertyLocalDateTime = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyLocalTime = TO_DATE('15:26:14', 'HH24:MI:SS') AND propertyLongObject = 66875 AND propertyShort = 43 AND propertySqlDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS') AND propertyString = 'someotherstring' AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = TO_DATE('15:26:14', 'HH24:MI:SS') AND propertyTimestamp = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereBeanNullValuesOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .where(BeanImpl.getNullBean());
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBoolean = 0 AND propertyBooleanObject = 0 AND propertyByte = 0 AND propertyByteObject = 0 AND propertyDouble = 0.0 AND propertyDoubleObject = 0.0 AND propertyFloat = 0.0 AND propertyFloatObject = 0.0 AND propertyInt = 0 AND propertyIntegerObject = 0 AND propertyLong = 0 AND propertyLongObject = 0 AND propertyShort = 0 AND propertyShortObject = 0");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereBeanIncludedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereIncluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyByte = 89 AND propertyDouble = 53348.34 AND propertyShort = 43 AND propertyStringBuffer = 'someotherstringbuff' AND propertyTime = TO_DATE('15:26:14', 'HH24:MI:SS')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereBeanExcludedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereExcluded(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal = 219038743.392874 AND propertyBoolean = 1 AND propertyBooleanObject = 0 AND propertyByteObject = 34 AND propertyCalendar = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyChar = 'v' AND propertyCharacterObject = 'r' AND propertyDate = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyDoubleObject = 143298.692 AND propertyEnum = 'VALUE_THREE' AND propertyFloat = 98634.2 AND propertyFloatObject = 8734.7 AND propertyInstant = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyInt = 545 AND propertyIntegerObject = 968 AND propertyLocalDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS') AND propertyLocalDateTime = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS') AND propertyLocalTime = TO_DATE('15:26:14', 'HH24:MI:SS') AND propertyLong = 34563 AND propertyLongObject = 66875 AND propertyShortObject = 68 AND propertySqlDate = TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS') AND propertyString = 'someotherstring' AND propertyTimestamp = TO_DATE('2002/06/18 15:26:14', 'YYYY/MM/DD HH24:MI:SS')");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereBeanFilteredOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereFiltered(BeanImpl.getPopulatedBean(), new String[]{"propertyByte", "propertyDouble", "propertyShort", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyShort", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyDouble = 53348.34 AND propertyStringBuffer = 'someotherstringbuff'");
        assertTrue(execute(query));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereParametersBeanOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereParameters(BeanImpl.class);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLong = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

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

        // don't check if actual rows were returned, since Oracle doesn't
        // match on the date types
        execute(query, new DbPreparedStatementHandler<>() {
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
        });
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereParametersBeanConstrainedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereParameters(BeanImplConstrained.class);
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal = ? AND propertyBoolean = ? AND propertyBooleanObject = ? AND propertyByte = ? AND propertyByteObject = ? AND propertyCalendar = ? AND propertyChar = ? AND propertyCharacterObject = ? AND propertyDate = ? AND propertyDouble = ? AND propertyDoubleObject = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyInstant = ? AND propertyInt = ? AND propertyIntegerObject = ? AND propertyLocalDate = ? AND propertyLocalDateTime = ? AND propertyLocalTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertySqlDate = ? AND propertyString = ? AND propertyStringBuffer = ? AND propertyTime = ? AND propertyTimestamp = ?");

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

        // don't check if actual rows were returned, since Oracle doesn't
        // match on the date types
        execute(query, new DbPreparedStatementHandler<>() {
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testWhereParametersBeanExcludedOracle() {
        var query = new Update(ORACLE);
        query.table("tablename")
            .field("propertyBoolean", true)
            .field("propertyByte", (byte) 16)
            .whereParametersExcluded(BeanImpl.class,
                new String[]{"propertyBoolean", "propertyByte", "propertyChar",
                    "propertyDouble", "propertyInstant", "propertyInt", "propertyLong",
                    "propertyLocalDate", "propertyLocalTime", "propertySqlDate", "propertyStringBuffer",
                    "propertyCalendar", "propertyDate", "propertyTime"});
        assertEquals(query.getSql(), "UPDATE tablename SET propertyBoolean = 1, propertyByte = 16 WHERE propertyBigDecimal = ? AND propertyBooleanObject = ? AND propertyByteObject = ? AND propertyCharacterObject = ? AND propertyDoubleObject = ? AND propertyEnum = ? AND propertyFloat = ? AND propertyFloatObject = ? AND propertyIntegerObject = ? AND propertyLocalDateTime = ? AND propertyLongObject = ? AND propertyShort = ? AND propertyShortObject = ? AND propertyString = ? AND propertyTimestamp = ?");

        assertEquals(query.getParameters().getOrderedNames().size(), 15);
        assertEquals(query.getParameters().getOrderedNames().get(0), "propertyBigDecimal");
        assertEquals(query.getParameters().getOrderedNames().get(1), "propertyBooleanObject");
        assertEquals(query.getParameters().getOrderedNames().get(2), "propertyByteObject");
        assertEquals(query.getParameters().getOrderedNames().get(3), "propertyCharacterObject");
        assertEquals(query.getParameters().getOrderedNames().get(4), "propertyDoubleObject");
        assertEquals(query.getParameters().getOrderedNames().get(5), "propertyEnum");
        assertEquals(query.getParameters().getOrderedNames().get(6), "propertyFloat");
        assertEquals(query.getParameters().getOrderedNames().get(7), "propertyFloatObject");
        assertEquals(query.getParameters().getOrderedNames().get(8), "propertyIntegerObject");
        assertEquals(query.getParameters().getOrderedNames().get(9), "propertyLocalDateTime");
        assertEquals(query.getParameters().getOrderedNames().get(10), "propertyLongObject");
        assertEquals(query.getParameters().getOrderedNames().get(11), "propertyShort");
        assertEquals(query.getParameters().getOrderedNames().get(12), "propertyShortObject");
        assertEquals(query.getParameters().getOrderedNames().get(13), "propertyString");
        assertEquals(query.getParameters().getOrderedNames().get(14), "propertyTimestamp");
        assertArrayEquals(query.getParameters().getOrderedNamesArray(), new String[]{"propertyBigDecimal", "propertyBooleanObject", "propertyByteObject", "propertyCharacterObject", "propertyDoubleObject", "propertyEnum", "propertyFloat", "propertyFloatObject", "propertyIntegerObject", "propertyLocalDateTime", "propertyLongObject", "propertyShort", "propertyShortObject", "propertyString", "propertyTimestamp"});

        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
            public void setParameters(DbPreparedStatement statement) {
                var cal = Calendar.getInstance();
                cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 0);
                statement
                    .setBigDecimal(1, new BigDecimal("219038743.392874"))
                    .setBoolean(2, false)
                    .setByte(3, (byte) 34)
                    .setString(4, "r")
                    .setDouble(5, 143298.692d)
                    .setString(6, "VALUE_THREE")
                    .setFloat(7, 98634.2f)
                    .setFloat(8, 8734.7f)
                    .setInt(9, 968)
                    .setTimestamp(10, Convert.toSqlTimestamp(cal))
                    .setLong(11, 66875L)
                    .setShort(12, (short) 43)
                    .setShort(13, (short) 68)
                    .setString(14, "someotherstring")
                    .setTimestamp(15, Convert.toSqlTimestamp(cal));
            }
        }));
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testSubselectParamsOracle() {
        var fieldquery = new Select(ORACLE);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");
        var wherequery = new Select(ORACLE);
        wherequery
            .from("table2")
            .field("max(propertyShort)")
            .whereParameter("propertyShort", "!=");
        var query = new Update(ORACLE);
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

        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setString("propertyString", "thestring")
                    .setInt("propertyInt", 1)
                    .setShort("propertyShort", (short) 5)
                    .setString("propertyString2", "anotherstring");
            }
        }));

        // Automated subselect creation
        query = new Update(ORACLE);
        // shuffled the structure around a bit to test the correct order usage
        query
            .where("propertyShort", ">=", wherequery)
            .whereParameterOr("propertyString", "propertyString2", "=")
            .whereOr("tablename.propertyFloat", ">", new Select(ORACLE)
                .from("table2")
                .field("max(propertyLong)")
                .whereParameter("propertyLong", "!="))
            .whereAnd("tablename.propertyDouble", "<=", new Select(ORACLE)
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

        assertTrue(execute(query, new DbPreparedStatementHandler<>() {
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCloneOracle() {
        var fieldquery = new Select(ORACLE);
        fieldquery
            .from("table2")
            .field("max(propertyLong)")
            .whereParameter("propertyInt", ">");
        var wherequery = new Select(ORACLE);
        wherequery
            .from("table2")
            .field("max(propertyShort)")
            .whereParameter("propertyShort", "!=");

        final var cal = Calendar.getInstance();
        cal.set(2002, Calendar.AUGUST, 19, 12, 17, 52);
        cal.set(Calendar.MILLISECOND, 462);
        var query = new Update(ORACLE);
        query.table("tablename")
            .hint("NO_INDEX")
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
        assertNotSame(query, query_clone);
        execute(query, new DbPreparedStatementHandler<>() {
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
