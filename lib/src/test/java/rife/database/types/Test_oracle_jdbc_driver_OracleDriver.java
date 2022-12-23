/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

import org.junit.jupiter.api.Test;
import rife.database.SomeEnum;
import rife.database.types.databasedrivers.oracle_jdbc_driver_OracleDriver;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Test_oracle_jdbc_driver_OracleDriver {
    @Test
    public void testGetSqlValue() {
        SqlConversion dbtypes;
        dbtypes = new oracle_jdbc_driver_OracleDriver();

        assertNotNull(dbtypes);
        assertEquals(dbtypes.getSqlValue(null), "NULL");
        assertEquals(dbtypes.getSqlValue("fdjh'kjhsdf'"), "'fdjh''kjhsdf'''");
        assertEquals(dbtypes.getSqlValue(new StringBuffer("kkdfkj'jfoodf")), "'kkdfkj''jfoodf'");
        assertEquals(dbtypes.getSqlValue('K'), "'K'");
        assertEquals(dbtypes.getSqlValue('\''), "''''");
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 05, 18, 18, 45, 40);
        assertEquals(dbtypes.getSqlValue(new Time(cal.getTime().getTime())), "TO_DATE('18:45:40', 'HH24:MI:SS')");
        assertEquals(dbtypes.getSqlValue(new Timestamp(cal.getTime().getTime())), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(dbtypes.getSqlValue(new java.sql.Date(cal.getTime().getTime())), "TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(dbtypes.getSqlValue(new Date(cal.getTime().getTime())), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(dbtypes.getSqlValue(cal), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(dbtypes.getSqlValue(new String[]{"kjkdf", "fdfdf", "ljkldfd"}), "{'kjkdf','fdfdf','ljkldfd'}");
        assertEquals(dbtypes.getSqlValue(Boolean.TRUE), "1");
        assertEquals(dbtypes.getSqlValue(SomeEnum.VALUE_TWO), "'VALUE_TWO'");
    }
}
