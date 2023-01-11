/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

import org.junit.jupiter.api.Test;
import rife.database.SomeEnum;
import rife.database.types.databasedrivers.org_postgresql_Driver;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Test_org_apache_derby_jdbc_EmbeddedDriver {
    @Test
    void testGetSqlValue() {
        SqlConversion dbtypes;
        dbtypes = new org_postgresql_Driver();

        assertNotNull(dbtypes);
        assertEquals(dbtypes.getSqlValue(null), "NULL");
        assertEquals(dbtypes.getSqlValue("fdjh'kjhsdf'"), "'fdjh''kjhsdf'''");
        assertEquals(dbtypes.getSqlValue(new StringBuffer("kkdfkj'jfoodf")), "'kkdfkj''jfoodf'");
        assertEquals(dbtypes.getSqlValue('K'), "'K'");
        assertEquals(dbtypes.getSqlValue('\''), "''''");
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 05, 18, 18, 45, 40);
        cal.set(Calendar.MILLISECOND, 132);
        assertEquals(dbtypes.getSqlValue(new Time(cal.getTime().getTime())), "'18:45:40'");
        assertEquals(dbtypes.getSqlValue(new Timestamp(cal.getTime().getTime())), "'2002-06-18 18:45:40.132'");
        assertEquals(dbtypes.getSqlValue(new java.sql.Date(cal.getTime().getTime())), "'2002-06-18'");
        assertEquals(dbtypes.getSqlValue(new Date(cal.getTime().getTime())), "'2002-06-18 18:45:40.132'");
        assertEquals(dbtypes.getSqlValue(cal), "'2002-06-18 18:45:40.132'");
        assertEquals(dbtypes.getSqlValue(new String[]{"kjkdf", "fdfdf", "ljkldfd"}), "{'kjkdf','fdfdf','ljkldfd'}");
        assertEquals(dbtypes.getSqlValue(Boolean.TRUE), "true");
        assertEquals(dbtypes.getSqlValue(SomeEnum.VALUE_TWO), "'VALUE_TWO'");
    }
}
