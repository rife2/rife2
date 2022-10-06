/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.scheduler.exceptions.FrequencyException;
import rife.tools.ExceptionUtils;
import rife.tools.Localization;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public class TestFrequency {
    private static final byte[] ALL_MINUTES = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59};
    private static final byte[] ALL_HOURS = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    private static final byte[] ALL_DATES = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
    private static final byte[] ALL_MONTHS = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private static final byte[] ALL_WEEKDAYS = new byte[]{1, 2, 3, 4, 5, 6, 7};

    @BeforeEach
    public void setUp() {
        RifeConfig.tools().setDefaultTimeZone(TimeZone.getTimeZone("CET"));
    }

    @BeforeEach
    public void tearDown() {
        RifeConfig.tools().setDefaultTimeZone(RifeConfig.tools().DEFAULT_DEFAULT_TIMEZONE);
    }

    @Test
    public void testAllWildcards() {
        try {
            Frequency frequency = new Frequency("* * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testInvalidSpacing() {
        try {
            Frequency frequency = new Frequency("*  *  * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testTooManyParts() {
        try {
            Frequency frequency = new Frequency("* * * * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testTooLittleParts() {
        try {
            Frequency frequency = new Frequency("* * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidNumberMinute() {
        try {
            Frequency frequency = new Frequency("d * * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidDividerMinute() {
        try {
            Frequency frequency = new Frequency("2/4 * * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidEmptyPartMinute() {
        try {
            Frequency frequency = new Frequency("2, * * * *");
            assertNotNull(frequency);
            fail();
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSingleMinute() {
        try {
            Frequency frequency = new Frequency("2 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "2 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{-1, -1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedMinutes() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("10-22 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "10-22 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("56-59 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "56-59 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 56, 57, 58, 59});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("12-12 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "12-12 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedMinutes() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("57-5 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "57-5 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{0, 1, 2, 3, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 57, 58, 59});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("58-0 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "58-0 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 58, 59});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testDividerMinutes() {
        try {
            Frequency frequency = new Frequency("*/17 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "*/17 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 17, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 34, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 51, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedDividerMinutes() {
        try {
            Frequency frequency = new Frequency("5-40/17 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "5-40/17 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{-1, -1, -1, -1, -1, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 22, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 39, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedDividerMinutes() {
        try {
            Frequency frequency = new Frequency("31-20/13 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "31-20/13 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 44, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 57, -1, -1});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testMixedMinutes() {
        try {
            Frequency frequency = new Frequency("10,12-18/2,30-40,45,48-5/3 * * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "10,12-18/2,30-40,45,48-5/3 * * * *");
            assertArrayEquals(frequency.getMinutes(), new byte[]{0, -1, -1, 3, -1, -1, -1, -1, -1, -1, 10, -1, 12, -1, 14, -1, 16, -1, 18, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, -1, -1, -1, -1, 45, -1, -1, 48, -1, -1, 51, -1, -1, 54, -1, -1, 57, -1, -1});
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testInvalidNumberHour() {
        try {
            Frequency frequency = new Frequency("* d * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidDividerHour() {
        try {
            Frequency frequency = new Frequency("* 2/4 * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidEmptyPartHour() {
        try {
            Frequency frequency = new Frequency("* 2, * * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSingleHour() {
        try {
            Frequency frequency = new Frequency("* 12 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 12 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedHours() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* 7-13 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 7-13 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{-1, -1, -1, -1, -1, -1, -1, 7, 8, 9, 10, 11, 12, 13, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* 22-23 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 22-23 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 22, 23});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* 19-19 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 19-19 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 19, -1, -1, -1, -1});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedHours() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* 17-3 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 17-3 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{0, 1, 2, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 17, 18, 19, 20, 21, 22, 23});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* 21-0 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 21-0 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 21, 22, 23});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testDividerHours() {
        try {
            Frequency frequency = new Frequency("* */6 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* */6 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{0, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, 12, -1, -1, -1, -1, -1, 18, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedDividerHours() {
        try {
            Frequency frequency = new Frequency("* 4-17/5 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 4-17/5 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{-1, -1, -1, -1, 4, -1, -1, -1, -1, 9, -1, -1, -1, -1, 14, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedDividerHours() {
        try {
            Frequency frequency = new Frequency("* 18-5/4 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 18-5/4 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{-1, -1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 18, -1, -1, -1, 22, -1});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testMixedHours() {
        try {
            Frequency frequency = new Frequency("* 4,8-10/3,12,15-18,20-3/3 * * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* 4,8-10/3,12,15-18,20-3/3 * * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), new byte[]{-1, -1, 2, -1, 4, -1, -1, -1, 8, -1, -1, -1, 12, -1, -1, 15, 16, 17, 18, -1, 20, -1, -1, 23});
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testInvalidNumberDate() {
        try {
            Frequency frequency = new Frequency("* * d * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidDividerDate() {
        try {
            Frequency frequency = new Frequency("* * 2/4 * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidEmptyPartDate() {
        try {
            Frequency frequency = new Frequency("* * 2, * *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSingleDate() {
        try {
            Frequency frequency = new Frequency("* * 14 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 14 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 14, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedDates() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* * 13-17 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 13-17 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 13, 14, 15, 16, 17, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 29-31 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 29-31 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 29, 30, 31});
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 7-7 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 7-7 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedDates() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* * 26-4 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 26-4 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 4, 4, 4, 4, 4});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{4, 4, 4, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 26-2,30-5 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 26-2,30-5 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 5, 5});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 26-5,30-2 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 26-5,30-2 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 26-2,31-30 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 26-2,31-30 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2, 30});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 27-1 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 27-1 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 27, 28, 29, 30, 31});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testDividerDates() {
        try {
            Frequency frequency = new Frequency("* * */8 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * */8 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{1, -1, -1, -1, -1, -1, -1, -1, 9, -1, -1, -1, -1, -1, -1, -1, 17, -1, -1, -1, -1, -1, -1, -1, 25, -1, -1, -1, -1, -1, -1});
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedDividerDates() {
        try {
            Frequency frequency = new Frequency("* * 7-23/9 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 7-23/9 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedDividerDates() {
        try {
            Frequency frequency = new Frequency("* * 11-7/13 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 11-7/13 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 24, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{-1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 30-29/5,18-17/8 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 30-29/5,18-17/8 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 18, -1, -1, -1, -1, -1, -1, -1, 26, -1, -1, -1, 30, -1});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 17, -1, -1, -1, -1, -1, -1, -1, 17, -1, -1, -1, 29, -1});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{-1, -1, 17, 29, -1, -1, -1, -1, 29, -1, 17, -1, -1, 29, -1, -1, -1, -1, 29, -1, -1, -1, -1, 29, -1, -1, -1, -1, 29, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * 27-26/3 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 27-26/3 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 27, -1, -1, 30, -1});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, -1, -1, 26, -1});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{-1, 26, -1, -1, 26, -1, -1, 26, -1, -1, 26, -1, -1, 26, -1, -1, 26, -1, -1, 26, -1, -1, 26, -1, -1, 26, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testMixedDates() {
        try {
            Frequency frequency = new Frequency("* * 3,5,8-18/4,19-23,27-2/2 * *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * 3,5,8-18/4,19-23,27-2/2 * *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), new byte[]{-1, -1, 3, -1, 5, -1, -1, 8, -1, -1, -1, 12, -1, -1, -1, 16, -1, -1, 19, 20, 21, 22, 23, -1, -1, -1, 27, -1, 29, -1, 31});
            assertArrayEquals(frequency.getDatesUnderflow(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, -1, 2, -1, 2});
            assertArrayEquals(frequency.getDatesOverflow(), new byte[]{-1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testInvalidNumberMonth() {
        try {
            Frequency frequency = new Frequency("* * * d *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidDividerMonth() {
        try {
            Frequency frequency = new Frequency("* * * 2/4 *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidEmptyPartMonth() {
        try {
            Frequency frequency = new Frequency("* * * 2, *");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSingleMonth() {
        try {
            Frequency frequency = new Frequency("* * * 3 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 3 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{-1, -1, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedMonths() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* * * 7-9 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 7-9 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{-1, -1, -1, -1, -1, -1, 7, 8, 9, -1, -1, -1});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * * 10-12 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 10-12 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * * 5-5 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 5-5 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{-1, -1, -1, -1, 5, -1, -1, -1, -1, -1, -1, -1});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedMonths() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* * * 9-2 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 9-2 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{1, 2, -1, -1, -1, -1, -1, -1, 9, 10, 11, 12});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);

            frequency = new Frequency("* * * 11-1 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 11-1 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 11, 12});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testDividerMonths() {
        try {
            Frequency frequency = new Frequency("* * * */4 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * */4 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{1, -1, -1, -1, 5, -1, -1, -1, 9, -1, -1, -1});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedDividerMonths() {
        try {
            Frequency frequency = new Frequency("* * * 3-11/3 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 3-11/3 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{-1, -1, 3, -1, -1, 6, -1, -1, 9, -1, -1, -1});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedDividerMonths() {
        try {
            Frequency frequency = new Frequency("* * * 5-3/2 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 5-3/2 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{1, -1, 3, -1, 5, -1, 7, -1, 9, -1, 11, -1});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testMixedMonths() {
        try {
            Frequency frequency = new Frequency("* * * 4,5-6,8-11/2,12-3/3 *");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * 4,5-6,8-11/2,12-3/3 *");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), new byte[]{-1, -1, 3, 4, 5, 6, -1, 8, -1, 10, -1, 12});
            assertArrayEquals(frequency.getWeekdays(), ALL_WEEKDAYS);
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testInvalidNumberWeekday() {
        try {
            Frequency frequency = new Frequency("* * * * d");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidDividerWeekday() {
        try {
            Frequency frequency = new Frequency("* * * * 2/4");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidEmptyPartWeekday() {
        try {
            Frequency frequency = new Frequency("* * * * 2,");
            fail();
            assertNotNull(frequency);
        } catch (FrequencyException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSingleWeekday() {
        try {
            Frequency frequency = new Frequency("* * * * 7");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 7");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{-1, -1, -1, -1, -1, -1, 7});
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedWeekdays() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* * * * 3-5");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 3-5");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{-1, -1, 3, 4, 5, -1, -1});

            frequency = new Frequency("* * * * 6-7");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 6-7");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{-1, -1, -1, -1, -1, 6, 7});

            frequency = new Frequency("* * * * 2-2");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 2-2");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{-1, 2, -1, -1, -1, -1, -1});
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedWeekdays() {
        try {
            Frequency frequency = null;

            frequency = new Frequency("* * * * 5-2");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 5-2");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{1, 2, -1, -1, 5, 6, 7});

            frequency = new Frequency("* * * * 6-1");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 6-1");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{1, -1, -1, -1, -1, 6, 7});
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testDividerWeekdays() {
        try {
            Frequency frequency = new Frequency("* * * * */3");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * */3");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{1, -1, -1, 4, -1, -1, 7});
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRangedDividerWeekdays() {
        try {
            Frequency frequency = new Frequency("* * * * 2-6/3");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 2-6/3");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{-1, 2, -1, -1, 5, -1, -1});
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReverseRangedDividerWeekdays() {
        try {
            Frequency frequency = new Frequency("* * * * 4-2/2");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 4-2/2");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{1, -1, -1, 4, -1, 6, -1});
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testMixedWeekdays() {
        try {
            Frequency frequency = new Frequency("* * * * 2,3-4,4-1/2");
            assertNotNull(frequency);

            assertTrue(frequency.isParsed());
            assertEquals(frequency.getFrequency(), "* * * * 2,3-4,4-1/2");
            assertArrayEquals(frequency.getMinutes(), ALL_MINUTES);
            assertArrayEquals(frequency.getHours(), ALL_HOURS);
            assertArrayEquals(frequency.getDates(), ALL_DATES);
            assertNull(frequency.getDatesUnderflow());
            assertNull(frequency.getDatesOverflow());
            assertArrayEquals(frequency.getMonths(), ALL_MONTHS);
            assertArrayEquals(frequency.getWeekdays(), new byte[]{1, 2, 3, 4, -1, 6, -1});
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testNextDateMinutes() {
        try {
            Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
            calendar.set(2002, Calendar.SEPTEMBER, 1, 10, 29);
            long calendar_time = calendar.getTimeInMillis();
            long previous = 0;
            long next = 0;
            Frequency frequency = null;

            int minute = 60 * 1000;
            int hour = 60 * minute;

            frequency = new Frequency("* * * * *");
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);

            frequency = new Frequency("28 * * * *");        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(59 * minute, next - previous);            // 2002/09/01 11:28
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(hour, next - previous);            // 2002/09/01 12:28
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(hour, next - previous);            // 2002/09/01 13:28

            frequency = new Frequency("*/3 * * * *");        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);            // 2002/09/01 10:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * minute, next - previous);            // 2002/09/01 10:33
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * minute, next - previous);            // 2002/09/01 10:36

            frequency = new Frequency("28-56/7 * * * *");    // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(6 * minute, next - previous);            // 2002/09/01 10:35
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * minute, next - previous);            // 2002/09/01 10:42
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * minute, next - previous);            // 2002/09/01 10:49
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * minute, next - previous);            // 2002/09/01 10:56
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(32 * minute, next - previous);            // 2002/09/01 11:28
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * minute, next - previous);            // 2002/09/01 11:35

            frequency = new Frequency("56-40/13 * * * *");    // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(6 * minute, next - previous);            // 2002/09/01 10:35
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(21 * minute, next - previous);            // 2002/09/01 10:56
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(13 * minute, next - previous);            // 2002/09/01 11:09
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(13 * minute, next - previous);            // 2002/09/01 11:22
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(13 * minute, next - previous);            // 2002/09/01 11:35
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(21 * minute, next - previous);            // 2002/09/01 11:56

            frequency = new Frequency("31,37-57/4,59-4,7 * * * *");    // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(2 * minute, next - previous);                    // 2002/09/01 10:31
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(6 * minute, next - previous);                    // 2002/09/01 10:37
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * minute, next - previous);                    // 2002/09/01 10:41
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * minute, next - previous);                    // 2002/09/01 10:45
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * minute, next - previous);                    // 2002/09/01 10:49
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * minute, next - previous);                    // 2002/09/01 10:53
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * minute, next - previous);                    // 2002/09/01 10:57
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(2 * minute, next - previous);                    // 2002/09/01 10:59
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);                    // 2002/09/01 11:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);                    // 2002/09/01 11:01
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);                    // 2002/09/01 11:02
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);                    // 2002/09/01 11:03
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(minute, next - previous);                    // 2002/09/01 11:04
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * minute, next - previous);                    // 2002/09/01 11:07
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(24 * minute, next - previous);                    // 2002/09/01 11:31
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testNextDateHours() {
        try {
            Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
            calendar.set(2002, Calendar.SEPTEMBER, 1, 10, 29);
            long calendar_time = calendar.getTimeInMillis();
            long previous = 0;
            long next = 0;
            Frequency frequency = null;

            int minute = 60 * 1000;
            int hour = 60 * minute;
            int day = 24 * hour;

            frequency = new Frequency("*/30 13 * * *");        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(2 * hour + 31 * minute, next - previous);    // 2002/09/01 13:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);            // 2002/09/01 13:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day - (30 * minute), next - previous);    // 2002/09/02 13:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);            // 2002/09/02 13:30

            frequency = new Frequency("10 13 * * *");        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(2 * hour + 41 * minute, next - previous);    // 2002/09/01 13:10
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                // 2002/09/02 13:10
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                // 2002/09/03 13:10

            frequency = new Frequency("34 */5 * * *");        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(5 * minute, next - previous);            // 2002/09/01 10:34
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * hour, next - previous);            // 2002/09/02 15:34
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * hour, next - previous);            // 2002/09/02 20:34
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * hour, next - previous);            // 2002/09/03 00:34
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * hour, next - previous);            // 2002/09/03 05:34
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * hour, next - previous);            // 2002/09/03 10:34

            frequency = new Frequency("13 7-23/7 * * *");                // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + 3 * hour + 13 * minute, next - previous);    // 2002/09/01 14:13
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * hour, next - previous);                        // 2002/09/02 21:13
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(10 * hour, next - previous);                        // 2002/09/03 07:13
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * hour, next - previous);                        // 2002/09/03 14:13

            frequency = new Frequency("48 18-7/3 * * *");                // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + 7 * hour + 48 * minute, next - previous);    // 2002/09/01 18:48
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * hour, next - previous);                        // 2002/09/01 21:48
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * hour, next - previous);                        // 2002/09/02 00:48
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * hour, next - previous);                        // 2002/09/02 03:48
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * hour, next - previous);                        // 2002/09/02 06:48
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(12 * hour, next - previous);                        // 2002/09/02 18:48

            frequency = new Frequency("14 2,4-7,10-18/3,21-0/3 * * *");    // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + 2 * hour + 14 * minute, next - previous);    // 2002/09/01 13:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * hour, next - previous);                        // 2002/09/01 16:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * hour, next - previous);                        // 2002/09/01 21:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * hour, next - previous);                        // 2002/09/02 00:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(2 * hour, next - previous);                        // 2002/09/02 02:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(2 * hour, next - previous);                        // 2002/09/02 04:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(hour, next - previous);                        // 2002/09/02 05:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(hour, next - previous);                        // 2002/09/02 06:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(hour, next - previous);                        // 2002/09/02 07:14
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * hour, next - previous);                        // 2002/09/02 10:14
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testNextDateDates() {
        try {
            Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
            calendar.set(2002, Calendar.SEPTEMBER, 1, 10, 29);
            long calendar_time = calendar.getTimeInMillis();
            long previous = 0;
            long next = 0;
            Frequency frequency = null;

            long minute = 60 * 1000;
            long hour = 60 * minute;
            long day = 24 * hour;

            frequency = new Frequency("*/30 */12 6 * *");            // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + 13 * hour + 4 * day, next - previous);    // 2002/09/06 00:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);                    // 2002/09/06 00:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute + 11 * hour, next - previous);            // 2002/09/06 12:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);                    // 2002/09/06 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute + 11 * hour + 29 * day, next - previous);    // 2002/10/06 00:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);                    // 2002/10/06 00:30

            frequency = new Frequency("19 10 */11 * *");            // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(11 * day - 10 * minute, next - previous);            // 2002/09/12 10:19
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(11 * day, next - previous);                    // 2002/09/23 10:19
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(8 * day, next - previous);                        // 2002/10/01 10:19
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(11 * day, next - previous);                    // 2002/10/12 10:19

            frequency = new Frequency("57 10 6-18/5 * *");            // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(28 * minute + 5 * day, next - previous);            // 2002/09/06 10:57
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * day, next - previous);                        // 2002/09/11 10:57
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * day, next - previous);                        // 2002/09/16 10:57
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(20 * day, next - previous);                    // 2002/10/06 10:57

            frequency = new Frequency("24 19 27-9/4 * *");            // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(9 * hour - 5 * minute + 3 * day, next - previous);        // 2002/09/04 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/09/08 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(19 * day, next - previous);                    // 2002/09/27 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/10/01 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/10/05 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/10/09 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(18 * day + hour, next - previous);                // 2002/10/27 19:24 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/10/31 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/11/04 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/11/08 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(19 * day, next - previous);                    // 2002/11/27 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/12/01 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/12/05 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/12/09 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(18 * day, next - previous);                    // 2002/12/27 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2002/12/31 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2003/01/04 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                        // 2003/01/08 19:24
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(19 * day, next - previous);                    // 2003/01/27 19:24

            calendar.set(2002, Calendar.APRIL, 28, 8, 15);
            calendar_time = calendar.getTimeInMillis();

            frequency = new Frequency("30 9 29-4/3 * *");            // 2002/04/28 08:15
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(15 * minute + hour + day, next - previous);    // 2002/04/29 09:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                        // 2002/04/02 09:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(27 * day, next - previous);                    // 2002/04/29 09:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                        // 2002/05/01 09:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                        // 2002/05/04 09:30

            calendar.set(2002, Calendar.FEBRUARY, 1, 8, 15);
            calendar_time = calendar.getTimeInMillis();

            frequency = new Frequency("30 8 18-10/9 * *");            // 2002/02/01 08:15
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(15 * minute + 4 * day, next - previous);            // 2002/02/05 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(13 * day, next - previous);                    // 2002/02/18 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day, next - previous);                        // 2002/02/27 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day, next - previous);                        // 2002/03/08 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(10 * day, next - previous);                    // 2002/03/18 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day, next - previous);                        // 2002/03/27 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day - hour, next - previous);                // 2002/04/05 08:30 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(13 * day, next - previous);                    // 2002/04/18 08:30

            calendar.set(2004, Calendar.FEBRUARY, 1, 8, 15);
            calendar_time = calendar.getTimeInMillis();

            frequency = new Frequency("30 8 18-10/9 * *");            // 2004/02/01 08:15
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(15 * minute + 4 * day, next - previous);            // 2004/02/05 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(13 * day, next - previous);                    // 2004/02/18 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day, next - previous);                        // 2004/02/27 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day, next - previous);                        // 2004/03/07 08:30 (leap year)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(11 * day, next - previous);                    // 2004/03/18 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day, next - previous);                        // 2004/03/27 08:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(9 * day - hour, next - previous);                // 2004/04/05 08:30 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(13 * day, next - previous);                    // 2004/04/18 08:30

            frequency = new Frequency("15 7 6,9-12,15-27/4,26-4/3 * *");    // 2004/02/01 08:15
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day - hour, next - previous);                        // 2004/02/04 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(2 * day, next - previous);                                // 2004/02/06 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/02/09 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/02/10 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/02/11 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/02/12 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/02/15 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                                // 2004/02/19 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                                // 2004/02/23 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/02/26 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/02/27 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(2 * day, next - previous);                                // 2004/02/29 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/03/03 07:15 (leap year)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/03/06 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/03/09 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/03/10 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/03/11 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/03/12 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/03/15 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                                // 2004/03/19 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(4 * day, next - previous);                                // 2004/03/23 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/03/26 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(day, next - previous);                                // 2004/03/27 07:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(2 * day - hour, next - previous);                        // 2004/03/29 07:15 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(3 * day, next - previous);                                // 2004/04/01 07:15

            calendar.set(2003, Calendar.DECEMBER, 20, 17, 10);
            calendar_time = calendar.getTimeInMillis();

            frequency = new Frequency("20 19 20-10/5 * *");            // 2003/12/20 17:10
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(10 * minute + 2 * hour, next - previous);            // 2003/12/20 19:20
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * day, next - previous);                        // 2003/12/25 19:20
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * day, next - previous);                        // 2003/12/30 19:20
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * day, next - previous);                        // 2004/01/04 19:20
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * day, next - previous);                        // 2004/01/09 19:20
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(11 * day, next - previous);                    // 2004/01/20 19:20
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(5 * day, next - previous);                        // 2004/01/25 19:20
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testNextDateMonths() {
        try {
            Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
            calendar.set(2002, Calendar.SEPTEMBER, 1, 10, 29);
            long calendar_time = calendar.getTimeInMillis();
            long previous = 0;
            long next = 0;
            Frequency frequency = null;

            long minute = 60 * 1000;
            long hour = 60 * minute;
            long day = 24 * hour;

            frequency = new Frequency("*/30 */12 */20 10 *");            // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + 13 * hour + 29 * day, next - previous);        // 2002/10/01 00:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);                        // 2002/10/01 00:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute + 11 * hour, next - previous);                // 2002/10/01 12:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);                        // 2002/10/01 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute + 11 * hour + 19 * day, next - previous);        // 2002/10/21 00:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);                        // 2002/10/21 00:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute + 11 * hour, next - previous);                // 2002/10/21 12:00
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute, next - previous);                        // 2002/10/21 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(30 * minute + 11 * hour + 344 * day, next - previous);        // 2003/10/01 00:00

            frequency = new Frequency("10 19 7 */4 *");                        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + 8 * hour + 6 * day + 10 * minute, next - previous);    // 2002/09/07 19:10
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((30 + 31 + 30 + 31) * day + hour, next - previous);            // 2003/01/07 19:10 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 28 + 31 + 30) * day - hour, next - previous);            // 2003/05/07 19:10 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 30 + 31 + 31) * day, next - previous);                    // 2003/09/07 19:10
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((30 + 31 + 30 + 31) * day + hour, next - previous);            // 2004/01/07 19:10 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 29 + 31 + 30) * day - hour, next - previous);            // 2004/05/07 19:10 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 30 + 31 + 31) * day, next - previous);                    // 2003/09/07 19:10

            frequency = new Frequency("50 06 18 4-11/3 *");                                    // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + 13 * hour + (29 + 17) * day + 6 * hour + 50 * minute, next - previous);    // 2002/10/18 06:50
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 30 + 31 + 31 + 28 + 31) * day, next - previous);                            // 2003/04/18 06:50
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((30 + 31 + 30) * day, next - previous);                                    // 2003/07/18 06:50
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 31 + 30) * day, next - previous);                                    // 2003/10/18 06:50
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 30 + 31 + 31 + 29 + 31) * day, next - previous);                            // 2004/04/18 06:50

            frequency = new Frequency("15 12 06 8-4/3 *");                                        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(31 * minute + (5 + 30 + 31) * day + hour + 15 * minute + hour, next - previous);        // 2002/11/06 12:15 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((30 + 31 + 31) * day, next - previous);                                        // 2003/02/06 12:15
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((28 + 31 + 30 + 31 + 30 + 31) * day - hour, next - previous);                        // 2003/08/06 12:15 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 30 + 31) * day + hour, next - previous);                                    // 2003/11/06 12:15 (daylight savings)

            frequency = new Frequency("40 11 27 2,5-6,11-4/2 *");                        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(11 * minute + hour + (26 + 30 + 31) * day + hour, next - previous);        // 2002/11/27 11:40
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((30 + 31) * day, next - previous);                                    // 2003/01/27 11:40
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(31 * day, next - previous);                                        // 2003/02/27 11:40
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(28 * day, next - previous);                                        // 2003/03/27 11:40
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((31 + 30) * day - hour, next - previous);                            // 2003/05/27 11:40 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(31 * day, next - previous);                                        // 2003/06/27 11:40
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((30 + 31 + 31 + 30 + 31) * day + hour, next - previous);                    // 2003/11/27 11:40
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals((30 + 31) * day, next - previous);                                    // 2004/01/27 11:40
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(31 * day, next - previous);                                        // 2004/02/27 11:40
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testNextDateWeekdays() {
        try {
            Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
            calendar.set(2002, Calendar.SEPTEMBER, 1, 10, 29);
            long calendar_time = calendar.getTimeInMillis();
            long previous = 0;
            long next = 0;
            Frequency frequency = null;

            long minute = 60 * 1000;
            long hour = 60 * minute;
            long day = 24 * hour;

            frequency = new Frequency("30 12 * 10 1");                        // 2002/09/01 10:29
            previous = calendar_time;
            next = frequency.getNextDate(previous);
            assertEquals(minute + 30 * day + 6 * day + 2 * hour, next - previous);        // 2002/10/07 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * day, next - previous);                                // 2002/10/14 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * day, next - previous);                                // 2002/10/21 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * day + hour, next - previous);                        // 2002/10/28 12:30 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(365 * day - 22 * day - hour, next - previous);                // 2003/10/06 12:30 (daylight savings)
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * day, next - previous);                                // 2003/10/13 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * day, next - previous);                                // 2003/10/20 12:30
            previous = next;
            next = frequency.getNextDate(previous);
            assertEquals(7 * day + hour, next - previous);                        // 2003/10/27 12:30 (daylight savings)
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testInvalidNextDate() {
        try {
            Calendar calendar = Calendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
            calendar.set(2002, Calendar.SEPTEMBER, 1, 10, 0);
            long calendar_time = calendar.getTimeInMillis();
            Frequency frequency = null;

            frequency = new Frequency("* * 31 2 *");
            try {
                frequency.getNextDate(calendar_time);
                fail();
            } catch (FrequencyException e) {
                assertTrue(true);
            }
        } catch (FrequencyException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
