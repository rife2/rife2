/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.ColumnsRequiredException;
import rife.database.exceptions.TableNameRequiredException;

import java.math.BigDecimal;
import java.sql.Blob;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateTableH2 extends TestCreateTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testInstantiationH2() {
        CreateTable query = new CreateTable(H2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testIncompleteQueryH2() {
        CreateTable query = new CreateTable(H2);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testClearH2() {
        CreateTable query = new CreateTable(H2);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnH2() {
        CreateTable query = new CreateTable(H2);
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
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string LONGVARCHAR, stringbuffer LONGVARCHAR, characterobject CHAR, booleanobject BOOLEAN, byteobject SMALLINT, doubleobject FLOAT, floatobject FLOAT, integerobject INTEGER, longobject BIGINT, shortobject SMALLINT, bigdecimal NUMERIC, charcolumn CHAR, booleancolumn BOOLEAN, bytecolumn SMALLINT, doublecolumn FLOAT, floatcolumn FLOAT, intcolumn INTEGER, longcolumn BIGINT, shortcolumn SMALLINT, blobcolumn LONGVARBINARY)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnPrecisionH2() {
        CreateTable query = new CreateTable(H2);
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
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string VARCHAR(255), stringbuffer VARCHAR(100), characterobject CHAR(20), booleanobject BOOLEAN, byteobject SMALLINT, doubleobject FLOAT, floatobject FLOAT, integerobject INTEGER, longobject BIGINT, shortobject SMALLINT, bigdecimal NUMERIC(19,9), charcolumn CHAR(10), booleancolumn BOOLEAN, bytecolumn SMALLINT, doublecolumn FLOAT, floatcolumn FLOAT, intcolumn INTEGER, longcolumn BIGINT, shortcolumn SMALLINT, blobcolumn LONGVARBINARY)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnsBeanH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .columns(BeanImpl.class);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyBoolean BOOLEAN, propertyBooleanObject BOOLEAN, propertyByte SMALLINT, propertyByteObject SMALLINT, propertyCalendar TIMESTAMP, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate TIMESTAMP, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyEnum VARCHAR(255), propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInt INTEGER, propertyIntegerObject INTEGER, propertyLong BIGINT, propertyLongObject BIGINT, propertyShort SMALLINT, propertyShortObject SMALLINT, propertySqlDate DATE, propertyString LONGVARCHAR, propertyStringbuffer LONGVARCHAR, propertyTime TIME, propertyTimestamp TIMESTAMP, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnsBeanIncludedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .columnsIncluded(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringbuffer", "propertyTime"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyByte SMALLINT, propertyFloat FLOAT, propertyStringbuffer LONGVARCHAR, propertyTime TIME)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnsBeanExcludedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .columnsExcluded(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringbuffer", "propertyTime"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBoolean BOOLEAN, propertyBooleanObject BOOLEAN, propertyByteObject SMALLINT, propertyCalendar TIMESTAMP, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate TIMESTAMP, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyEnum VARCHAR(255), propertyFloatObject FLOAT, propertyInt INTEGER, propertyIntegerObject INTEGER, propertyLong BIGINT, propertyLongObject BIGINT, propertyShort SMALLINT, propertyShortObject SMALLINT, propertySqlDate DATE, propertyString LONGVARCHAR, propertyTimestamp TIMESTAMP, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnsBeanFilteredH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .columnsFiltered(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringbuffer", "propertyTime"}, new String[]{"propertyByte", "propertyStringbuffer"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyFloat FLOAT, propertyTime TIME)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnsBeanPrecisionH2() {
        CreateTable query = new CreateTable(H2);
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
            .precision("propertyInt", 10)
            .precision("propertyIntegerObject", 8)
            .precision("propertyLong", 12)
            .precision("propertyLongObject", 11)
            .precision("propertyShort", 9)
            .precision("propertyShortObject", 6)
            .precision("propertySqlDate", 8)
            .precision("propertyString", 255)
            .precision("propertyStringbuffer", 100)
            .precision("propertyTime", 9)
            .precision("propertyTimestamp", 30, 2)
            .precision("propertyEnum", 12);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC(19,9), propertyBoolean BOOLEAN, propertyBooleanObject BOOLEAN, propertyByte SMALLINT, propertyByteObject SMALLINT, propertyCalendar TIMESTAMP, propertyChar CHAR(10), propertyCharacterObject CHAR(12), propertyDate TIMESTAMP, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyEnum VARCHAR(255), propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInt INTEGER, propertyIntegerObject INTEGER, propertyLong BIGINT, propertyLongObject BIGINT, propertyShort SMALLINT, propertyShortObject SMALLINT, propertySqlDate DATE, propertyString VARCHAR(255), propertyStringbuffer VARCHAR(100), propertyTime TIME, propertyTimestamp TIMESTAMP, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testColumnsBeanConstrainedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .columns(BeanImplConstrained.class);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC(17,6), propertyBoolean BOOLEAN, propertyBooleanObject BOOLEAN, propertyByte SMALLINT, propertyByteObject SMALLINT NOT NULL, propertyCalendar TIMESTAMP, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate TIMESTAMP, propertyDouble FLOAT, propertyDoubleObject FLOAT, propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInt INTEGER DEFAULT 23, propertyIntegerObject INTEGER, propertyLongObject BIGINT, propertyShort SMALLINT, propertySqlDate DATE, propertyString VARCHAR(30) DEFAULT 'one' NOT NULL, propertyStringbuffer VARCHAR(20) NOT NULL, propertyTime TIME, propertyTimestamp TIMESTAMP, PRIMARY KEY (propertyString), UNIQUE (propertyStringbuffer, propertyByteObject), UNIQUE (propertyStringbuffer), CHECK (propertyByteObject != -1), CHECK (propertyInt != 0), CHECK (propertyLongObject IS NULL OR propertyLongObject IN (89,1221,66875,878)), CHECK (propertyString IS NULL OR propertyString IN ('one','tw''''o','someotherstring')), CHECK (propertyStringbuffer != ''), CHECK (propertyStringbuffer != 'some''blurp'))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testNullableH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn1", int.class, CreateTable.NULL)
            .column("stringColumn", String.class, 12, CreateTable.NOTNULL)
            .column("intColumn2", int.class)
            .column("intColumn3", int.class)
            .column("floatColumn", float.class, 13, 6, CreateTable.NOTNULL)
            .nullable("intColumn2", CreateTable.NULL)
            .nullable("intColumn3", CreateTable.NOTNULL)
            .nullable("floatColumn", null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn1 INTEGER NULL, stringColumn VARCHAR(12) NOT NULL, intColumn2 INTEGER NULL, intColumn3 INTEGER NOT NULL, floatColumn FLOAT)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDefaultH2() {
        CreateTable query = new CreateTable(H2);
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
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string VARCHAR(255) DEFAULT 'stringDefault', stringbuffer VARCHAR(100) DEFAULT 'stringbufferDefault', characterobject CHAR(22) DEFAULT 'characterobjectDefault', booleanobject BOOLEAN DEFAULT true, byteobject SMALLINT DEFAULT 34, doubleobject FLOAT DEFAULT 234.87, floatobject FLOAT DEFAULT 834.43, integerobject INTEGER DEFAULT 463, longobject BIGINT DEFAULT 34876, shortobject SMALLINT DEFAULT 98, bigdecimal NUMERIC(19,9) DEFAULT 347.14, charcolumn CHAR(10) DEFAULT 'OSJFDZ', booleancolumn BOOLEAN DEFAULT false, bytecolumn SMALLINT DEFAULT 27, doublecolumn FLOAT DEFAULT 934.5, floatcolumn FLOAT DEFAULT 35.87, intcolumn INTEGER DEFAULT 983734, longcolumn BIGINT DEFAULT 2343345, shortcolumn SMALLINT DEFAULT 12)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testDefaultFunctionH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename1")
            .column("dateobject", java.sql.Date.class)
            .defaultFunction("dateobject", "now()");
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (dateobject DATE DEFAULT now())");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCustomAttributeH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename1")
            .column("intColumn", Integer.class)
            .customAttribute("intColumn", "CHECK (intColumn > 0)");
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (intColumn INTEGER CHECK (intColumn > 0))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testTemporaryH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .temporary(true)
            .column("boolColumn", boolean.class);
        assertEquals(query.getSql(), "CREATE TEMPORARY TABLE tablename (boolColumn BOOLEAN)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testPrimaryKeySimpleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .primaryKey("intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER NOT NULL, PRIMARY KEY (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testPrimaryKeyMultipleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .primaryKey(new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER NOT NULL, stringColumn VARCHAR(50) NOT NULL, PRIMARY KEY (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testPrimaryKeyNamedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .primaryKey("constraint_name", "intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER NOT NULL, CONSTRAINT constraint_name PRIMARY KEY (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testPrimaryKeyMultipleNamedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .primaryKey("constraint_name", new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER NOT NULL, stringColumn VARCHAR(50) NOT NULL, CONSTRAINT constraint_name PRIMARY KEY (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testUniqueSimpleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .unique("intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, UNIQUE (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testUniqueMultipleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .unique(new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, stringColumn VARCHAR(50), UNIQUE (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testUniqueNamedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .unique("constraint_name", "intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, CONSTRAINT constraint_name UNIQUE (intColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testUniqueMultipleNamedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .unique("constraint_name", new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, stringColumn VARCHAR(50), CONSTRAINT constraint_name UNIQUE (intColumn, stringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testForeignKeySimpleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testForeignKeyMultipleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, stringColumn VARCHAR(50), FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testForeignKeySimpleNamedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("constraint_name", "foreigntable", "intColumn", "foreignIntColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, CONSTRAINT constraint_name FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testForeignKeyMultipleNamedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("constraint_name", "foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, stringColumn VARCHAR(50), CONSTRAINT constraint_name FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testForeignKeyViolationsSingleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.CASCADE, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE CASCADE)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.NOACTION, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE NO ACTION)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.RESTRICT, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE RESTRICT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.SETDEFAULT, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE SET DEFAULT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.SETNULL, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE SET NULL)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.CASCADE);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE CASCADE)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.NOACTION);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE NO ACTION)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.RESTRICT);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE RESTRICT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.SETDEFAULT);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE SET DEFAULT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.SETNULL);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE SET NULL)");
        execute(query);
        query.clear();
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testForeignKeyViolationsH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.CASCADE, CreateTable.NOACTION);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE CASCADE ON DELETE NO ACTION)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testForeignKeyMultipleViolationsH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"}, CreateTable.RESTRICT, CreateTable.SETDEFAULT);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, stringColumn VARCHAR(50), FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn) ON UPDATE RESTRICT ON DELETE SET DEFAULT)");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCheckSimpleH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .check("intColumn > 0");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, CHECK (intColumn > 0))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCheckNamedH2() {
        CreateTable query = new CreateTable(H2);
        query.table("tablename")
            .column("intColumn", int.class)
            .check("NAME_CK", "intColumn > 0");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INTEGER, CONSTRAINT NAME_CK CHECK (intColumn > 0))");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCloneH2() {
        CreateTable query = new CreateTable(H2);
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
            .precision("propertyStringbuffer", 100)
            .precision("propertyTime", 9)
            .precision("propertyTimestamp", 30, 2)
            .nullable("propertyString", CreateTable.NULL)
            .nullable("propertyInt", CreateTable.NOTNULL)
            .defaultValue("propertyString", "stringDefault")
            .defaultFunction("propertyDate", "now()")
            .customAttribute("propertyInt", "CHECK (propertyInt > 0)")
            .temporary(true)
            .primaryKey("constraint_name1", new String[]{"propertyInt", "propertyString"})
            .unique("constraint_name2", new String[]{"propertyLong", "propertyString"})
            .foreignKey("foreigntable", new String[]{"propertyInt", "foreignIntColumn", "propertyString", "foreignStringColumn"}, CreateTable.RESTRICT, CreateTable.SETDEFAULT)
            .check("NAME_CK", "propertyInt > 0");
        CreateTable query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(query_clone);
    }
}
