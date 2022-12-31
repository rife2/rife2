/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import org.junit.jupiter.api.Test;
import rife.config.exceptions.DateFormatInitializationException;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestRifeConfig {
    @Test
    void testShortDateFormat() {
        try {
            switchLocale("US", "EN");

            var sf = RifeConfig.tools().getDefaultShortDateFormat();
            var formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "8/31/04");

            switchLocale("BE", "NL");

            sf = RifeConfig.tools().getDefaultShortDateFormat();
            formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "31/08/2004");

            switchLocale("ES", "ES");

            sf = RifeConfig.tools().getDefaultShortDateFormat();
            formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "31/8/04");

            switchDates("EEE, MMM d, yyyy", "EEE, d MMM yyyy HH:mm:ss");

            sf = RifeConfig.tools().getDefaultShortDateFormat();
            formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "mar, ago 31, 2004");

            try {
                switchDates("vvvv 999 uuuu", "vvvv, 82.2 cccc");

                sf = RifeConfig.tools().getDefaultShortDateFormat();
                formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());
                fail();
            } catch (DateFormatInitializationException e) {
                assertTrue(true);
            }
        } finally {
            switchLocale(null, "EN");
            switchDates(null, null);
        }
    }

    @Test
    void testLongDateFormat() {
        try {
            switchLocale("US", "EN");

            var sf = RifeConfig.tools().getDefaultLongDateFormat();
            var formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "Aug 31, 2004, 3:53 PM");

            switchLocale("BE", "NL");

            sf = RifeConfig.tools().getDefaultLongDateFormat();
            formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "31 aug. 2004 15:53");

            switchLocale("ES", "ES");

            sf = RifeConfig.tools().getDefaultLongDateFormat();
            formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "31 ago 2004 15:53");

            switchDates("EEE, MMM d, yyyy", "EEE, d MMM yyyy HH:mm:ss");

            sf = RifeConfig.tools().getDefaultLongDateFormat();
            formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());

            assertEquals(formatted, "mar, 31 ago 2004 14:53:00");

            try {
                switchDates("wwww 999 uuuu", "vvvv, 82.2 cccc");

                sf = RifeConfig.tools().getDefaultLongDateFormat();
                formatted = sf.format(new GregorianCalendar(2004, Calendar.AUGUST, 31, 15, 53).getTime());
                fail();
            } catch (DateFormatInitializationException e) {
                assertTrue(true);
            }
        } finally {
            switchLocale(null, "en");
            switchDates(null, null);
        }
    }

    public static void switchLocale(String country, String language) {
        RifeConfig.tools().setDefaultCountry(country);
        if (language != null) {
            RifeConfig.tools().setDefaultLanguage(language);
        }
    }

    public static void switchDates(String shortDate, String longDate) {
        RifeConfig.tools().setDefaultShortDateFormat(shortDate);
        RifeConfig.tools().setDefaultLongDateFormat(longDate);
    }
}
