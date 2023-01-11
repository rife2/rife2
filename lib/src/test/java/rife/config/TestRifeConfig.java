/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import org.junit.jupiter.api.Test;
import rife.config.exceptions.DateFormatInitializationException;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestRifeConfig {
    private static ZonedDateTime makeDate(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        return ZonedDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute, 0, 0, RifeConfig.tools().getDefaultTimeZone().toZoneId());
    }
    
    @Test
    void testShortDateFormat() {
        try {
            switchLocale("US", "EN");

            var sf = RifeConfig.tools().getDefaultShortDateFormat();
            var formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "8/31/04");

            switchLocale("BE", "NL");

            sf = RifeConfig.tools().getDefaultShortDateFormat();
            formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "31/08/2004");

            switchLocale("ES", "ES");

            sf = RifeConfig.tools().getDefaultShortDateFormat();
            formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "31/8/04");

            switchDates("EEE, MMM d, yyyy", "EEE, d MMM yyyy HH:mm:ss");

            sf = RifeConfig.tools().getDefaultShortDateFormat();
            formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "mar, ago 31, 2004");

            try {
                switchDates("VvVv 999 uuuu", "WwWw, 82.2 cccc");

                sf = RifeConfig.tools().getDefaultShortDateFormat();
                formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));
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

            var formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "Aug 31, 2004, 3:53:00 PM");

            switchLocale("BE", "NL");

            sf = RifeConfig.tools().getDefaultLongDateFormat();
            formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "31 aug. 2004 15:53:00");

            switchLocale("ES", "ES");

            sf = RifeConfig.tools().getDefaultLongDateFormat();
            formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "31 ago 2004 15:53:00");

            switchDates("EEE, MMM d, yyyy", "EEE, d MMM yyyy HH:mm:ss");

            sf = RifeConfig.tools().getDefaultLongDateFormat();
            formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));

            assertEquals(formatted, "mar, 31 ago 2004 15:53:00");

            try {
                switchDates("WwWw 999 uuuu", "VvVv, 82.2 cccc");

                sf = RifeConfig.tools().getDefaultLongDateFormat();
                formatted = sf.format(makeDate(2004, Calendar.AUGUST, 31, 15, 53));
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
