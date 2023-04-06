/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

import org.junit.jupiter.api.Test;
import rife.database.SomeEnum;
import rife.database.types.databasedrivers.org_postgresql_Driver;
import rife.tools.Convert;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Test_org_postgresql_Driver {
    @Test
    void testGetSqlValue() {
        var db_types = new org_postgresql_Driver();

        assertNotNull(db_types);
        assertEquals(db_types.getSqlValue(null), "NULL");
        assertEquals(db_types.getSqlValue("fdjh'kjhsdf'"), "'fdjh''kjhsdf'''");
        assertEquals(db_types.getSqlValue(new StringBuffer("kkdfkj'jfoodf")), "'kkdfkj''jfoodf'");
        assertEquals(db_types.getSqlValue('K'), "'K'");
        assertEquals(db_types.getSqlValue('\''), "''''");
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.JUNE, 18, 18, 45, 40);
        cal.set(Calendar.MILLISECOND, 132);
        assertEquals(db_types.getSqlValue(Convert.toSqlTime(cal)), "'18:45:40'");
        assertEquals(db_types.getSqlValue(Convert.toSqlTimestamp(cal)), "'2002-06-18 18:45:40.132'");
        assertEquals(db_types.getSqlValue(Convert.toSqlDate(cal)), "'2002-06-18'");
        assertEquals(db_types.getSqlValue(Convert.toDate(cal)), "'2002-06-18 18:45:40.132'");
        assertEquals(db_types.getSqlValue(cal), "'2002-06-18 18:45:40.132'");
        assertEquals(db_types.getSqlValue(cal.toInstant()), "'2002-06-18 18:45:40.132'");
        var local_date_time = LocalDateTime.of(2002, Month.JUNE, 18, 18, 45, 40, 132000000);
        assertEquals(db_types.getSqlValue(local_date_time), "'2002-06-18 18:45:40.132'");
        assertEquals(db_types.getSqlValue(local_date_time.toLocalDate()), "'2002-06-18'");
        assertEquals(db_types.getSqlValue(local_date_time.toLocalTime()), "'18:45:40'");
        assertEquals(db_types.getSqlValue(new String[]{"kjkdf", "fdfdf", "ljkldfd"}), "{'kjkdf','fdfdf','ljkldfd'}");
        assertEquals(db_types.getSqlValue(true), "true");
        assertEquals(db_types.getSqlValue(SomeEnum.VALUE_TWO), "'VALUE_TWO'");
    }
}
