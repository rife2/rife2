/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

import org.junit.jupiter.api.Test;
import rife.database.SomeEnum;
import rife.database.types.databasedrivers.oracle_jdbc_driver_OracleDriver;
import rife.tools.Convert;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Test_oracle_jdbc_driver_OracleDriver {
    @Test
    void testGetSqlValue() {
        var db_types = new oracle_jdbc_driver_OracleDriver();

        assertNotNull(db_types);
        assertEquals(db_types.getSqlValue(null), "NULL");
        assertEquals(db_types.getSqlValue("fdjh'kjhsdf'"), "'fdjh''kjhsdf'''");
        assertEquals(db_types.getSqlValue(new StringBuffer("kkdfkj'jfoodf")), "'kkdfkj''jfoodf'");
        assertEquals(db_types.getSqlValue('K'), "'K'");
        assertEquals(db_types.getSqlValue('\''), "''''");
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.JUNE, 18, 18, 45, 40);
        assertEquals(db_types.getSqlValue(Convert.toSqlTime(cal)), "TO_DATE('18:45:40', 'HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(Convert.toSqlTimestamp(cal)), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(Convert.toSqlDate(cal)), "TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(Convert.toDate(cal)), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(cal), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(cal.toInstant()), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        var local_date_time = LocalDateTime.of(2002, Month.JUNE, 18, 18, 45, 40);
        assertEquals(db_types.getSqlValue(local_date_time), "TO_DATE('2002/06/18 18:45:40', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(local_date_time.toLocalDate()), "TO_DATE('2002/06/18 00:00:00', 'YYYY/MM/DD HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(local_date_time.toLocalTime()), "TO_DATE('18:45:40', 'HH24:MI:SS')");
        assertEquals(db_types.getSqlValue(new String[]{"kjkdf", "fdfdf", "ljkldfd"}), "{'kjkdf','fdfdf','ljkldfd'}");
        assertEquals(db_types.getSqlValue(Boolean.TRUE), "1");
        assertEquals(db_types.getSqlValue(SomeEnum.VALUE_TWO), "'VALUE_TWO'");
    }
}
