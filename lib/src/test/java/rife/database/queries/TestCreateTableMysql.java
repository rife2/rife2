/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.BeanImpl;
import rife.database.BeanImplConstrained;
import rife.database.exceptions.ColumnsRequiredException;
import rife.database.exceptions.TableNameRequiredException;

import java.math.BigDecimal;
import java.sql.Blob;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateTableMysql extends TestCreateTable {
    @Test
    void testInstantiationMysql() {
        CreateTable query = new CreateTable(MYSQL);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateTable");
        }
    }

    @Test
    void testIncompleteQueryMysql() {
        CreateTable query = new CreateTable(MYSQL);
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

    @Test
    void testClearMysql() {
        CreateTable query = new CreateTable(MYSQL);
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

    @Test
    void testColumnMysql() {
        CreateTable query = new CreateTable(MYSQL);
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
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string LONGTEXT, stringbuffer LONGTEXT, characterobject CHAR, booleanobject BIT, byteobject TINYINT, doubleobject DOUBLE, floatobject FLOAT, integerobject INT, longobject BIGINT, shortobject SMALLINT, bigdecimal NUMERIC, charcolumn CHAR, booleancolumn BIT, bytecolumn TINYINT, doublecolumn DOUBLE, floatcolumn FLOAT, intcolumn INT, longcolumn BIGINT, shortcolumn SMALLINT, blobcolumn LONGBLOB)");
        // this is invalid to execute with Mysql
        // VARCHAR and CHAR need size specification
    }

    @Test
    void testColumnPrecisionMysql() {
        CreateTable query = new CreateTable(MYSQL);
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
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string VARCHAR(255), stringbuffer VARCHAR(100), characterobject CHAR(20), booleanobject BIT, byteobject TINYINT, doubleobject DOUBLE(30,2), floatobject FLOAT(20,2), integerobject INT, longobject BIGINT, shortobject SMALLINT, bigdecimal NUMERIC(19,9), charcolumn CHAR(10), booleancolumn BIT, bytecolumn TINYINT, doublecolumn DOUBLE(12,3), floatcolumn FLOAT(13,2), intcolumn INT, longcolumn BIGINT, shortcolumn SMALLINT, blobcolumn LONGBLOB)");
        execute(query);
    }

    @Test
    void testColumnsBeanMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .columns(BeanImpl.class);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyBoolean BIT, propertyBooleanObject BIT, propertyByte TINYINT, propertyByteObject TINYINT, propertyCalendar DATETIME, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate DATETIME, propertyDouble DOUBLE, propertyDoubleObject DOUBLE, propertyEnum VARCHAR(255), propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInt INT, propertyIntegerObject INT, propertyLong BIGINT, propertyLongObject BIGINT, propertyShort SMALLINT, propertyShortObject SMALLINT, propertySqlDate DATE, propertyString LONGTEXT, propertyStringbuffer LONGTEXT, propertyTime TIME, propertyTimestamp DATETIME, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        // this is invalid to execute with Mysql
        // CHAR needs size specification
    }

    @Test
    void testColumnsBeanIncludedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .columnsIncluded(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringbuffer", "propertyTime"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyByte TINYINT, propertyFloat FLOAT, propertyStringbuffer LONGTEXT, propertyTime TIME)");
        execute(query);
    }

    @Test
    void testColumnsBeanExcludedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .columnsExcluded(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringbuffer", "propertyTime"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBoolean BIT, propertyBooleanObject BIT, propertyByteObject TINYINT, propertyCalendar DATETIME, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate DATETIME, propertyDouble DOUBLE, propertyDoubleObject DOUBLE, propertyEnum VARCHAR(255), propertyFloatObject FLOAT, propertyInt INT, propertyIntegerObject INT, propertyLong BIGINT, propertyLongObject BIGINT, propertyShort SMALLINT, propertyShortObject SMALLINT, propertySqlDate DATE, propertyString LONGTEXT, propertyTimestamp DATETIME, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        // this is invalid to execute with Mysql
        // VARCHAR and CHAR need size specification
    }

    @Test
    void testColumnsBeanFilteredMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .columnsFiltered(BeanImpl.class, new String[]{"propertyBigDecimal", "propertyByte", "propertyFloat", "propertyStringbuffer", "propertyTime"}, new String[]{"propertyByte", "propertyStringbuffer"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC, propertyFloat FLOAT, propertyTime TIME)");
        execute(query);
    }

    @Test
    void testColumnsBeanPrecisionMysql() {
        CreateTable query = new CreateTable(MYSQL);
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
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC(19,9), propertyBoolean BIT, propertyBooleanObject BIT, propertyByte TINYINT, propertyByteObject TINYINT, propertyCalendar DATETIME, propertyChar CHAR(10), propertyCharacterObject CHAR(12), propertyDate DATETIME, propertyDouble DOUBLE(12,3), propertyDoubleObject DOUBLE(14,4), propertyEnum VARCHAR(255), propertyFloat FLOAT(13,2), propertyFloatObject FLOAT(12,1), propertyInt INT, propertyIntegerObject INT, propertyLong BIGINT, propertyLongObject BIGINT, propertyShort SMALLINT, propertyShortObject SMALLINT, propertySqlDate DATE, propertyString VARCHAR(255), propertyStringbuffer VARCHAR(100), propertyTime TIME, propertyTimestamp DATETIME, CHECK (propertyEnum IS NULL OR propertyEnum IN ('VALUE_ONE','VALUE_TWO','VALUE_THREE')))");
        execute(query);
    }

    @Test
    void testColumnsBeanConstrainedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .columns(BeanImplConstrained.class);
        assertEquals(query.getSql(), "CREATE TABLE tablename (propertyBigDecimal NUMERIC(17,6), propertyBoolean BIT, propertyBooleanObject BIT, propertyByte TINYINT, propertyByteObject TINYINT NOT NULL, propertyCalendar DATETIME, propertyChar CHAR, propertyCharacterObject CHAR, propertyDate DATETIME, propertyDouble DOUBLE, propertyDoubleObject DOUBLE, propertyFloat FLOAT, propertyFloatObject FLOAT, propertyInt INT DEFAULT 23, propertyIntegerObject INT, propertyLongObject BIGINT, propertyShort SMALLINT, propertySqlDate DATE, propertyString VARCHAR(30) DEFAULT 'one' NOT NULL, propertyStringbuffer VARCHAR(20) NOT NULL, propertyTime TIME, propertyTimestamp DATETIME, PRIMARY KEY (propertyString), UNIQUE (propertyStringbuffer, propertyByteObject), UNIQUE (propertyStringbuffer), CHECK (propertyByteObject != -1), CHECK (propertyInt != 0), CHECK (propertyLongObject IS NULL OR propertyLongObject IN (89,1221,66875,878)), CHECK (propertyString IS NULL OR propertyString IN ('one','tw''''o','someotherstring')), CHECK (propertyStringbuffer != ''), CHECK (propertyStringbuffer != 'some''blurp'))");
        execute(query);
    }

    @Test
    void testNullableMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn1", int.class, CreateTable.NULL)
            .column("stringColumn", String.class, 12, CreateTable.NOTNULL)
            .column("intColumn2", int.class)
            .column("intColumn3", int.class)
            .column("floatColumn", float.class, 13, 6, CreateTable.NOTNULL)
            .nullable("intColumn2", CreateTable.NULL)
            .nullable("intColumn3", CreateTable.NOTNULL)
            .nullable("floatColumn", null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn1 INT NULL, stringColumn VARCHAR(12) NOT NULL, intColumn2 INT NULL, intColumn3 INT NOT NULL, floatColumn FLOAT(13,6))");
        execute(query);
    }

    @Test
    void testDefaultMysql() {
        CreateTable query = new CreateTable(MYSQL);
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
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (string VARCHAR(255) DEFAULT 'stringDefault', stringbuffer VARCHAR(100) DEFAULT 'stringbufferDefault', characterobject CHAR(22) DEFAULT 'characterobjectDefault', booleanobject BIT DEFAULT 1, byteobject TINYINT DEFAULT 34, doubleobject DOUBLE(30,2) DEFAULT 234.87, floatobject FLOAT(20,2) DEFAULT 834.43, integerobject INT DEFAULT 463, longobject BIGINT DEFAULT 34876, shortobject SMALLINT DEFAULT 98, bigdecimal NUMERIC(19,9) DEFAULT 347.14, charcolumn CHAR(10) DEFAULT 'OSJFDZ', booleancolumn BIT DEFAULT 0, bytecolumn TINYINT DEFAULT 27, doublecolumn DOUBLE(12,3) DEFAULT 934.5, floatcolumn FLOAT(13,2) DEFAULT 35.87, intcolumn INT DEFAULT 983734, longcolumn BIGINT DEFAULT 2343345, shortcolumn SMALLINT DEFAULT 12)");
        execute(query);
    }

    @Test
    void testDefaultFunctionMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename1")
            .column("dateobject", java.sql.Date.class)
            .defaultFunction("dateobject", "now()");
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (dateobject DATE DEFAULT now())");
        // this is invalid to execute with Mysql
        // it doesn't support columns with default functions
    }

    @Test
    void testCustomAttributeMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename1")
            .column("intColumn", Integer.class)
            .customAttribute("intColumn", "AUTO_INCREMENT")
            .primaryKey("intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename1 (intColumn INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (intColumn))");
        execute(query);
    }

    @Test
    void testTemporaryMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .temporary(true)
            .column("boolColumn", boolean.class);
        assertEquals(query.getSql(), "CREATE TEMPORARY TABLE tablename (boolColumn BIT)");
        execute(query);
    }

    @Test
    void testPrimaryKeySimpleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .primaryKey("intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT NOT NULL, PRIMARY KEY (intColumn))");
        execute(query);
    }

    @Test
    void testPrimaryKeyMultipleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .primaryKey(new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT NOT NULL, stringColumn VARCHAR(50) NOT NULL, PRIMARY KEY (intColumn, stringColumn))");
        execute(query);
    }

    @Test
    void testPrimaryKeyNamedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .primaryKey("constraint_name", "intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT NOT NULL, PRIMARY KEY (intColumn))");
        execute(query);
    }

    @Test
    void testPrimaryKeyMultipleNamedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .primaryKey("constraint_name", new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT NOT NULL, stringColumn VARCHAR(50) NOT NULL, PRIMARY KEY (intColumn, stringColumn))");
        execute(query);
    }

    @Test
    void testUniqueSimpleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .unique("intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, UNIQUE (intColumn))");
        execute(query);
    }

    @Test
    void testUniqueMultipleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .unique(new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, stringColumn VARCHAR(50), UNIQUE (intColumn, stringColumn))");
        execute(query);
    }

    @Test
    void testUniqueNamedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .unique("constraint_name", "intColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, UNIQUE constraint_name (intColumn))");
        execute(query);
    }

    @Test
    void testUniqueMultipleNamedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .unique("constraint_name", new String[]{"intColumn", "stringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, stringColumn VARCHAR(50), UNIQUE constraint_name (intColumn, stringColumn))");
        execute(query);
    }

    @Test
    void testForeignKeySimpleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn))");
        execute(query);
    }

    @Test
    void testForeignKeyMultipleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, stringColumn VARCHAR(50), FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn))");
        execute(query);
    }

    @Test
    void testForeignKeySimpleNamedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("constraint_name", "foreigntable", "intColumn", "foreignIntColumn");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, CONSTRAINT constraint_name FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn))");
        execute(query);
    }

    @Test
    void testForeignKeyMultipleNamedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("constraint_name", "foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"});
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, stringColumn VARCHAR(50), CONSTRAINT constraint_name FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn))");
        execute(query);
    }

    @Test
    void testForeignKeyViolationsSingleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.CASCADE, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE CASCADE)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.NOACTION, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE NO ACTION)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.RESTRICT, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE RESTRICT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.SETDEFAULT, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE SET DEFAULT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.SETNULL, null);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE SET NULL)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.CASCADE);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE CASCADE)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.NOACTION);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE NO ACTION)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.RESTRICT);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE RESTRICT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.SETDEFAULT);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE SET DEFAULT)");
        execute(query);
        query.clear();

        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", null, CreateTable.SETNULL);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON DELETE SET NULL)");
        execute(query);
        query.clear();
    }

    @Test
    void testForeignKeyViolationsMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .foreignKey("foreigntable", "intColumn", "foreignIntColumn", CreateTable.CASCADE, CreateTable.NOACTION);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, FOREIGN KEY (intColumn) REFERENCES foreigntable (foreignIntColumn) ON UPDATE CASCADE ON DELETE NO ACTION)");
        execute(query);
    }

    @Test
    void testForeignKeyMultipleViolationsMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .column("stringColumn", String.class, 50)
            .foreignKey("foreigntable", new String[]{"intColumn", "foreignIntColumn", "stringColumn", "foreignStringColumn"}, CreateTable.RESTRICT, CreateTable.SETDEFAULT);
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, stringColumn VARCHAR(50), FOREIGN KEY (intColumn, stringColumn) REFERENCES foreigntable (foreignIntColumn, foreignStringColumn) ON UPDATE RESTRICT ON DELETE SET DEFAULT)");
        execute(query);
    }

    @Test
    void testCheckSimpleMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .check("intColumn > 0");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, CHECK (intColumn > 0))");
        execute(query);
    }

    @Test
    void testCheckNamedMysql() {
        CreateTable query = new CreateTable(MYSQL);
        query.table("tablename")
            .column("intColumn", int.class)
            .check("NAME_CK", "intColumn > 0");
        assertEquals(query.getSql(), "CREATE TABLE tablename (intColumn INT, CHECK (intColumn > 0))");
        execute(query);
    }

    // TODO : test fails
//    @Test
//    public void testCloneMysql() {
//        CreateTable query = new CreateTable(MYSQL);
//        query.table("tablename")
//            .columns(BeanImpl.class)
//            .precision("propertyBigDecimal", 19, 9)
//            .precision("propertyBoolean", 4)
//            .precision("propertyByte", 8)
//            .precision("propertyCalendar", 20)
//            .precision("propertyChar", 10)
//            .precision("propertyDate", 7)
//            .precision("propertyDouble", 12, 3)
//            .precision("propertyFloat", 13, 2)
//            .precision("propertyInt", 10)
//            .precision("propertyLong", 12)
//            .precision("propertyShort", 9)
//            .precision("propertySqlDate", 8)
//            .precision("propertyString", 255)
//            .precision("propertyStringbuffer", 100)
//            .precision("propertyTime", 9)
//            .precision("propertyTimestamp", 30, 2)
//            .nullable("propertyString", CreateTable.NULL)
//            .nullable("propertyInt", CreateTable.NOTNULL)
//            .defaultValue("propertyString", "stringDefault")
//            .customAttribute("propertyInt", "AUTO_INCREMENT")
//            .temporary(true)
//            .primaryKey("constraint_name1", new String[]{"propertyInt", "propertyString"})
//            .unique("constraint_name2", new String[]{"propertyLong", "propertyString"})
//            .foreignKey("foreigntable", new String[]{"propertyInt", "foreignIntColumn", "propertyString", "foreignStringColumn"}, CreateTable.RESTRICT, CreateTable.SETDEFAULT);
//        CreateTable query_clone = query.clone();
//        assertEquals(query.getSql(), query_clone.getSql());
//        assertNotSame(query, query_clone);
//        execute(query_clone);
//    }
}
