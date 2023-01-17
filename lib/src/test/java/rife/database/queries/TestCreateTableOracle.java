/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.*;

import java.math.BigDecimal;
import java.sql.Blob;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateTableOracle extends TestCreateTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testInstantiationOracle() {
        var query = new CreateTable(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testIncompleteQueryOracle() {
        var query = new CreateTable(ORACLE);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateTable");
        }
        query.table("tablename");
        try {
            query.getSql();
            fail();
        } catch (ColumnsRequiredException e) {
            assertEquals(e.getQueryName(), "CreateTable");
        }
        query.table("tablename")
            .column("string", String.class);
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testClearOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("string", String.class);
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename1")
            .column("string", String.class)
            .column("stringbuffer", StringBuffer.class)
            .column("characterobject", Character.class)
            .column("booleanobject", Boolean.class)
            .column("byteobject", Byte.class)
            .column("doubleobject", Double.class)
            .column("floatobject", Float.class)
            .column("integerobject", Integer.class)
            .column("longobject", Long.class)
            .column("shortobject", Short.class)
            .column("bigdecimal", BigDecimal.class)
            .column("charcolumn", char.class)
            .column("booleancolumn", boolean.class)
            .column("bytecolumn", byte.class)
            .column("doublecolumn", double.class)
            .column("floatcolumn", float.class)
            .column("intcolumn", int.class)
            .column("longcolumn", long.class)
            .column("shortcolumn", short.class)
            .column("blobcolumn", Blob.class);
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string VARCHAR2(4000), stringbuffer VARCHAR2(4000), characterobject CHAR, booleanobject NUMBER(1), byteobject NUMBER(3), doubleobject FLOAT, floatobject FLOAT, integerobject NUMBER(10), longobject NUMBER(19), shortobject NUMBER(5), bigdecimal NUMERIC, charcolumn CHAR, booleancolumn NUMBER(1), bytecolumn NUMBER(3), doublecolumn FLOAT, floatcolumn FLOAT, intcolumn NUMBER(10), longcolumn NUMBER(19), shortcolumn NUMBER(5), blobcolumn BLOB)");
        // this is invalid to execute with Oracle
        // VARCHAR2 and CHAR need size specification
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnPrecisionOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename1")
            .column("string", String.class, 255)
            .column("stringbuffer", StringBuffer.class, 100)
            .column("characterobject", Character.class, 20)
            .column("booleanobject", Boolean.class, 7)
            .column("byteobject", Byte.class, 9)
            .column("doubleobject", Double.class, 30, 2)
            .column("floatobject", Float.class, 20, 2)
            .column("integerobject", Integer.class, 10)
            .column("longobject", Long.class, 8)
            .column("shortobject", Short.class, 8)
            .column("bigdecimal", BigDecimal.class, 19, 9)
            .column("charcolumn", char.class, 10)
            .column("booleancolumn", boolean.class, 4)
            .column("bytecolumn", byte.class, 8)
            .column("doublecolumn", double.class, 12, 3)
            .column("floatcolumn", float.class, 13, 2)
            .column("intcolumn", int.class, 10)
            .column("longcolumn", long.class, 12)
            .column("shortcolumn", short.class, 9)
            .column("blobcolumn", Blob.class, 20);
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string VARCHAR2(255), stringbuffer VARCHAR2(100), characterobject CHAR(20), booleanobject NUMBER(1), byteobject NUMBER(3), doubleobject FLOAT, floatobject FLOAT, integerobject NUMBER(10), longobject NUMBER(19), shortobject NUMBER(5), bigdecimal NUMERIC(19,9), charcolumn CHAR(10), booleancolumn NUMBER(1), bytecolumn NUMBER(3), doublecolumn FLOAT, floatcolumn FLOAT, intcolumn NUMBER(10), longcolumn NUMBER(19), shortcolumn NUMBER(5), blobcolumn BLOB)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnsBeanOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .columns(BeanImpl.class);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyBoolean NUMBER(1), propertyBooleanObject NUMBER(1), propertyByte NUMBER(3), propertyByteObject NUMBER(3), propertyCalendar DATE, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate DATE, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyEnum VARCHAR(255), propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInstant DATE, propertyInt NUMBER(10), propertyIntegerObject NUMBER(10), propertyLocalDate DATE, propertyLocalDateTime DATE, propertyLocalTime DATE, propertyLong NUMBER(19), propertyLongObject NUMBER(19), propertyShort NUMBER(5), propertyShortObject NUMBER(5), propertySqlDate DATE, propertyString VARCHAR2(4000), propertyStringBuffer VARCHAR2(4000), propertyTime DATE, propertyTimestamp DATE, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        // this is invalid to execute with Oracle
        // VARCHAR2 and CHAR need size specification
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnsBeanIncludedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .columnsIncluded(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyByte NUMBER(3), propertyFloat FLOAT, propertyStringBuffer VARCHAR2(4000), propertyTime DATE)");
        // this is invalid to execute with Oracle
        // VARCHAR2 and CHAR need size specification
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnsBeanExcludedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .columnsExcluded(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringBuffer", "propertyTime"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBoolean NUMBER(1), propertyBooleanObject NUMBER(1), propertyByteObject NUMBER(3), propertyCalendar DATE, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate DATE, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyEnum VARCHAR(255), propertyFloatObject FLOAT, propertyInstant DATE, propertyInt NUMBER(10), propertyIntegerObject NUMBER(10), propertyLocalDate DATE, propertyLocalDateTime DATE, propertyLocalTime DATE, propertyLong NUMBER(19), propertyLongObject NUMBER(19), propertyShort NUMBER(5), propertyShortObject NUMBER(5), propertySqlDate DATE, propertyString VARCHAR2(4000), propertyTimestamp DATE, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        // this is invalid to execute with Oracle
        // VARCHAR2 and CHAR need size specification
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnsBeanFilteredOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .columnsFiltered(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringBuffer", "propertyTime"}, new String[]{"propertyByte", "propertyStringBuffer"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyFloat FLOAT, propertyTime DATE)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnsBeanPrecisionOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .columns(BeanImpl.class)
            .precision("propertyBigDecimal", 19, 9)
            .precision("propertyBoolean", 4)
            .precision("propertyBooleanObject", 7)
            .precision("propertyByte", 8)
            .precision("propertyByteObject", 9)
            .precision("propertyCalendar", 20)
            .precision("propertyChar", 10)
            .precision("propertyCharacterObject", 12)
            .precision("propertyDate", 7)
            .precision("propertyDouble", 12, 3)
            .precision("propertyDoubleObject", 14, 4)
            .precision("propertyFloat", 13, 2)
            .precision("propertyFloatObject", 12, 1)
            .precision("propertyInstant", 7)
            .precision("propertyInt", 10)
            .precision("propertyIntegerObject", 8)
            .precision("propertyLocalDateTime", 5)
            .precision("propertyLocalDate", 32)
            .precision("propertyLocalTime", 10)
            .precision("propertyLong", 12)
            .precision("propertyLongObject", 11)
            .precision("propertyShort", 9)
            .precision("propertyShortObject", 6)
            .precision("propertySqlDate", 8)
            .precision("propertyString", 255)
            .precision("propertyStringBuffer", 100)
            .precision("propertyTime", 9)
            .precision("propertyTimestamp", 30, 2)
            .precision("propertyEnum", 14);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC(19,9), propertyBoolean NUMBER(1), propertyBooleanObject NUMBER(1), propertyByte NUMBER(3), propertyByteObject NUMBER(3), propertyCalendar DATE, propertyChar CHAR(10), propertyCharacterObject CHAR(12), propertyDate DATE, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyEnum VARCHAR(255), propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInstant DATE, propertyInt NUMBER(10), propertyIntegerObject NUMBER(10), propertyLocalDate DATE, propertyLocalDateTime DATE, propertyLocalTime DATE, propertyLong NUMBER(19), propertyLongObject NUMBER(19), propertyShort NUMBER(5), propertyShortObject NUMBER(5), propertySqlDate DATE, propertyString VARCHAR2(255), propertyStringBuffer VARCHAR2(100), propertyTime DATE, propertyTimestamp DATE, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testColumnsBeanConstrainedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .columns(BeanImplConstrained.class);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC(17,6), propertyBoolean NUMBER(1), propertyBooleanObject NUMBER(1), propertyByte NUMBER(3), propertyByteObject NUMBER(3) NOT NULL, propertyCalendar DATE, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate DATE, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInstant DATE, propertyInt NUMBER(10) DEFAULT 23, propertyIntegerObject NUMBER(10), propertyLocalDate DATE, propertyLocalDateTime DATE, propertyLocalTime DATE, propertyLongObject NUMBER(19), propertyShort NUMBER(5), propertySqlDate DATE, propertyString VARCHAR2(30) DEFAULT 'one' NOT NULL, propertyStringBuffer VARCHAR2(20) NOT NULL, propertyTime DATE, propertyTimestamp DATE, PRIMARY KEY (propertyString), UNIQUE (propertyStringBuffer, propertyByteObject), UNIQUE (propertyStringBuffer), CHECK (propertyByteObject != -1), CHECK (propertyInt != 0), CHECK (propertyLongObject IS NULL OR propertyLongObject IN (89,1221,66875,878)), CHECK (propertyString IS NULL OR propertyString IN ('one','tw''''o','someotherstring')), CHECK (propertyStringBuffer != ''), CHECK (propertyStringBuffer != 'some''blurp'))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testNullableOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn1", int.class, CreateTable.NULL)
            .column("stringColumn", String.class, 12, CreateTable.NOTNULL)
            .column("intColumn2", int.class)
            .column("intColumn3", int.class)
            .column("floatColumn", float.class, 13, 6, CreateTable.NOTNULL)
            .nullable("intColumn2", CreateTable.NULL)
            .nullable("intColumn3", CreateTable.NOTNULL)
            .nullable("floatColumn", null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn1 NUMBER(10) NULL, stringColumn VARCHAR2(12) NOT NULL, intColumn2 NUMBER(10) NULL, intColumn3 NUMBER(10) NOT NULL, floatColumn FLOAT)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDefaultOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename1")
            .column("string", String.class, 255)
            .column("stringbuffer", StringBuffer.class, 100)
            .column("characterobject", Character.class, 22)
            .column("booleanobject", Boolean.class, 7)
            .column("byteobject", Byte.class, 9)
            .column("doubleobject", Double.class, 30, 2)
            .column("floatobject", Float.class, 20, 2)
            .column("integerobject", Integer.class, 10)
            .column("longobject", Long.class, 8)
            .column("shortobject", Short.class, 8)
            .column("bigdecimal", BigDecimal.class, 19, 9)
            .column("charcolumn", char.class, 10)
            .column("booleancolumn", boolean.class, 4)
            .column("bytecolumn", byte.class, 8)
            .column("doublecolumn", double.class, 12, 3)
            .column("floatcolumn", float.class, 13, 2)
            .column("intcolumn", int.class, 10)
            .column("longcolumn", long.class, 12)
            .column("shortcolumn", short.class, 9)
            .defaultValue("string", "stringDefault")
            .defaultValue("stringbuffer", "stringbufferDefault")
            .defaultValue("characterobject", "characterobjectDefault")
            .defaultValue("booleanobject", true)
            .defaultValue("byteobject", (byte) 34)
            .defaultValue("doubleobject", 234.87d)
            .defaultValue("floatobject", 834.43f)
            .defaultValue("integerobject", 463)
            .defaultValue("longobject", 34876L)
            .defaultValue("shortobject", (short) 98)
            .defaultValue("bigdecimal", new BigDecimal("347.14"))
            .defaultValue("charcolumn", "OSJFDZ")
            .defaultValue("booleancolumn", false)
            .defaultValue("bytecolumn", (byte) 27)
            .defaultValue("doublecolumn", 934.5d)
            .defaultValue("floatcolumn", 35.87f)
            .defaultValue("intcolumn", 983734)
            .defaultValue("longcolumn", 2343345L)
            .defaultValue("shortcolumn", 12);
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string VARCHAR2(255) DEFAULT 'stringDefault', stringbuffer VARCHAR2(100) DEFAULT 'stringbufferDefault', characterobject CHAR(22) DEFAULT 'characterobjectDefault', booleanobject NUMBER(1) DEFAULT 1, byteobject NUMBER(3) DEFAULT 34, doubleobject FLOAT DEFAULT 234.87, floatobject FLOAT DEFAULT 834.43, integerobject NUMBER(10) DEFAULT 463, longobject NUMBER(19) DEFAULT 34876, shortobject NUMBER(5) DEFAULT 98, bigdecimal NUMERIC(19,9) DEFAULT 347.14, charcolumn CHAR(10) DEFAULT 'OSJFDZ', booleancolumn NUMBER(1) DEFAULT 0, bytecolumn NUMBER(3) DEFAULT 27, doublecolumn FLOAT DEFAULT 934.5, floatcolumn FLOAT DEFAULT 35.87, intcolumn NUMBER(10) DEFAULT 983734, longcolumn NUMBER(19) DEFAULT 2343345, shortcolumn NUMBER(5) DEFAULT 12)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testDefaultFunctionOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename1")
            .column("intcolumn", int.class)
            .defaultFunction("intcolumn", "6+1");
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (intcolumn NUMBER(10) DEFAULT 6+1)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCustomAttributeOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename1")
            .column("intColumn", Integer.class)
            .customAttribute("intColumn", "CHECK (intColumn > 0)");
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (intColumn NUMBER(10) CHECK (intColumn > 0))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testTemporaryOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .temporary(true)
            .column("boolColumn", boolean.class);
        assertEquals(query.getSql(), "CREATE GLOBAL TEMPORARY TABLE tablename (boolColumn NUMBER(1))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testPrimaryKeySimpleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .primaryKey("intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10) NOT NULL, PRIMARY KEY (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testPrimaryKeyMultipleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .primaryKey(new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10) NOT NULL, stringColumn VARCHAR2(50) NOT NULL, PRIMARY KEY (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testPrimaryKeyNamedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .primaryKey("constraint_name", "intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10) NOT NULL, CONSTRAINT constraint_name PRIMARY KEY (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testPrimaryKeyMultipleNamedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .primaryKey("constraint_name", new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10) NOT NULL, stringColumn VARCHAR2(50) NOT NULL, CONSTRAINT constraint_name PRIMARY KEY (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testUniqueSimpleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .unique("intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), UNIQUE (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testUniqueMultipleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .unique(new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), stringColumn VARCHAR2(50), UNIQUE (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testUniqueNamedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .unique("constraint_name", "intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), CONSTRAINT constraint_name UNIQUE (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testUniqueMultipleNamedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .unique("constraint_name", new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), stringColumn VARCHAR2(50), CONSTRAINT constraint_name UNIQUE (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testForeignKeySimpleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testForeignKeyMultipleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), stringColumn VARCHAR2(50), FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testForeignKeySimpleNamedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("constraint_name", "foreigntable", "intColumn", "foreignIntColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), CONSTRAINT constraint_name FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testForeignKeyMultipleNamedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("constraint_name", "foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), stringColumn VARCHAR2(50), CONSTRAINT constraint_name FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testForeignKeyViolationsSingleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.CASCADE, null);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.NOACTION, null);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.RESTRICT, null);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.SETDEFAULT, null);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.SETNULL, null);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.CASCADE);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE CASCADE)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.NOACTION);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.RESTRICT);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.SETDEFAULT);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.SETNULL);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE SET NULL)");
        execute(query);
        query.clear();
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testForeignKeyViolationsOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.CASCADE, CreateTable.NOACTION);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testForeignKeyMultipleViolationsOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"}, CreateTable.RESTRICT, CreateTable.CASCADE);
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCheckSimpleOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .check("intColumn > 0");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), CHECK (intColumn > 0))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCheckNamedOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .column("intColumn", int.class)
            .check("NAME_CK", "intColumn > 0");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn NUMBER(10), CONSTRAINT NAME_CK CHECK (intColumn > 0))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCloneOracle() {
        var query = new CreateTable(ORACLE);
        query.table("tablename")
            .columns(BeanImpl.class)
            .precision("propertyBigDecimal", 19, 9)
            .precision("propertyBoolean", 4)
            .precision("propertyByte", 8)
            .precision("propertyCalendar", 20)
            .precision("propertyChar", 10)
            .precision("propertyDate", 7)
            .precision("propertyDouble", 12, 3)
            .precision("propertyFloat", 13, 2)
            .precision("propertyInt", 10)
            .precision("propertyLong", 12)
            .precision("propertyShort", 9)
            .precision("propertySqlDate", 8)
            .precision("propertyString", 255)
            .precision("propertyStringBuffer", 100)
            .precision("propertyTime", 9)
            .precision("propertyTimestamp", 30, 2)
            .nullable("propertyString", CreateTable.NULL)
            .nullable("propertyInt", CreateTable.NOTNULL)
            .defaultValue("propertyStringBuffer", "stringDefault")
            .defaultFunction("propertyLong", "6+1")
            .customAttribute("propertyInt", "CHECK (propertyInt > 0)")
            .primaryKey("constraint_name1", new String[]{"propertyInt", "propertyString"})
            .unique("constraint_name2", new String[]{"propertyLong", "propertyString"})
            .foreignKey("foreigntable", new String[]{"propertyInt", "foreignIntColumn", "propertyString", "foreignStringColumn"}, null, CreateTable.CASCADE)
            .check("NAME_CK", "propertyInt > 0");
        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(query_clone);
    }
}
