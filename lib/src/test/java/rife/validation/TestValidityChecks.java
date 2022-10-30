/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestValidityChecks {
    @Test
    public void testCheckNotNull() {
        assertFalse(ValidityChecks.checkNotNull(null));
        assertTrue(ValidityChecks.checkNotNull(new Object()));
    }

    @Test
    public void testCheckNotEmptyCharSequence() {
        assertTrue(ValidityChecks.checkNotEmpty((CharSequence) null));
        assertTrue(ValidityChecks.checkNotEmpty("ok"));
        assertFalse(ValidityChecks.checkNotEmpty(""));
    }

    @Test
    public void testCheckNotBlankCharSequence() {
        assertFalse(ValidityChecks.checkNotBlank((CharSequence)null));
        assertTrue(ValidityChecks.checkNotBlank("ok"));
        assertFalse(ValidityChecks.checkNotBlank(""));
    }

    @Test
    public void testCheckNotEmptyCharacterObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Character) null));
        assertTrue(ValidityChecks.checkNotEmpty('I'));
        assertFalse(ValidityChecks.checkNotEmpty((char) 0));
    }

    @Test
    public void testCheckNotEmptyChar() {
        assertTrue(ValidityChecks.checkNotEmpty('K'));
        assertFalse(ValidityChecks.checkNotEmpty((char) 0));
    }

    @Test
    public void testCheckNotEmptyByteObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Byte) null));
        assertTrue(ValidityChecks.checkNotEmpty((byte) 87));
        assertFalse(ValidityChecks.checkNotEmpty((byte) 0));
    }

    @Test
    public void testCheckNotEmptyByte() {
        assertTrue(ValidityChecks.checkNotEmpty((byte) 98));
        assertFalse(ValidityChecks.checkNotEmpty((byte) 0));
    }

    @Test
    public void testCheckNotEmptyShortObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Short) null));
        assertTrue(ValidityChecks.checkNotEmpty((short) 223));
        assertFalse(ValidityChecks.checkNotEmpty((short) 0));
    }

    @Test
    public void testCheckNotEmptyShort() {
        assertTrue(ValidityChecks.checkNotEmpty((short) 8982));
        assertFalse(ValidityChecks.checkNotEmpty((short) 0));
    }

    @Test
    public void testCheckNotEmptyIntegerObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Integer) null));
        assertTrue(ValidityChecks.checkNotEmpty(8796));
        assertFalse(ValidityChecks.checkNotEmpty(0));
    }

    @Test
    public void testCheckNotEmptyInteger() {
        assertTrue(ValidityChecks.checkNotEmpty(622));
        assertFalse(ValidityChecks.checkNotEmpty(0));
    }

    @Test
    public void testCheckNotEmptyLongObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Long) null));
        assertTrue(ValidityChecks.checkNotEmpty(6887232L));
        assertFalse(ValidityChecks.checkNotEmpty(0L));
    }

    @Test
    public void testCheckNotEmptyLong() {
        assertTrue(ValidityChecks.checkNotEmpty(2324338L));
        assertFalse(ValidityChecks.checkNotEmpty(0L));
    }

    @Test
    public void testCheckNotEmptyFloatObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Float) null));
        assertTrue(ValidityChecks.checkNotEmpty(923.78f));
        assertFalse(ValidityChecks.checkNotEmpty(0.0f));
    }

    @Test
    public void testCheckNotEmptyFloat() {
        assertTrue(ValidityChecks.checkNotEmpty(234.98f));
        assertFalse(ValidityChecks.checkNotEmpty(0.0f));
    }

    @Test
    public void testCheckNotEmptyDoubleObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Double) null));
        assertTrue(ValidityChecks.checkNotEmpty(98023.343d));
        assertFalse(ValidityChecks.checkNotEmpty(0.0d));
    }

    @Test
    public void testCheckNotEmptyDouble() {
        assertTrue(ValidityChecks.checkNotEmpty(8723.87901d));
        assertFalse(ValidityChecks.checkNotEmpty(0.0d));
    }

    @Test
    public void testCheckNotEmptyObject() {
        assertTrue(ValidityChecks.checkNotEmpty((Object) null));
        assertTrue(ValidityChecks.checkNotEmpty((Object) "ok"));
        assertFalse(ValidityChecks.checkNotEmpty((Object) ""));
        assertTrue(ValidityChecks.checkNotEmpty((Object) 'I'));
        assertFalse(ValidityChecks.checkNotEmpty((Object) (char) 0));
        assertTrue(ValidityChecks.checkNotEmpty((Object) (byte) 87));
        assertFalse(ValidityChecks.checkNotEmpty((Object) (byte) 0));
        assertTrue(ValidityChecks.checkNotEmpty((Object) (short) 223));
        assertFalse(ValidityChecks.checkNotEmpty((Object) (short) 0));
        assertTrue(ValidityChecks.checkNotEmpty((Object) 8796));
        assertFalse(ValidityChecks.checkNotEmpty((Object) 0));
        assertTrue(ValidityChecks.checkNotEmpty((Object) 6887232L));
        assertFalse(ValidityChecks.checkNotEmpty((Object) 0L));
        assertTrue(ValidityChecks.checkNotEmpty((Object) 923.78f));
        assertFalse(ValidityChecks.checkNotEmpty((Object) 0.0f));
        assertTrue(ValidityChecks.checkNotEmpty((Object) 98023.343d));
        assertFalse(ValidityChecks.checkNotEmpty((Object) 0.0d));
        assertTrue(ValidityChecks.checkNotEmpty((Object) new String[]{"one", "two", "three"}));
        assertFalse(ValidityChecks.checkNotEmpty((Object) new String[]{"one", "two", "  "}));
        assertTrue(ValidityChecks.checkNotEmpty((Object) new int[]{34, 9, 34}));
        assertFalse(ValidityChecks.checkNotEmpty((Object) new int[]{4, 0, 9}));

        assertTrue(ValidityChecks.checkNotEmpty(new Date()));
    }

    @Test
    public void testCheckNotEqualBoolean() {
        assertTrue(ValidityChecks.checkNotEqual(true, false));
        assertFalse(ValidityChecks.checkNotEqual(true, true));
    }

    @Test
    public void testCheckNotEqualChar() {
        assertTrue(ValidityChecks.checkNotEqual('K', 'L'));
        assertFalse(ValidityChecks.checkNotEqual('a', 'a'));
    }

    @Test
    public void testCheckNotEqualByte() {
        assertTrue(ValidityChecks.checkNotEqual((byte) 98, (byte) 76));
        assertFalse(ValidityChecks.checkNotEqual((byte) 17, (byte) 17));
    }

    @Test
    public void testCheckNotEqualShort() {
        assertTrue(ValidityChecks.checkNotEqual((short) 8982, (short) 237));
        assertFalse(ValidityChecks.checkNotEqual((short) 23, (short) 23));
    }

    @Test
    public void testCheckNotEqualInteger() {
        assertTrue(ValidityChecks.checkNotEqual(622, 766));
        assertFalse(ValidityChecks.checkNotEqual(234, 234));
    }

    @Test
    public void testCheckNotEqualLong() {
        assertTrue(ValidityChecks.checkNotEqual(2324338L, 343876L));
        assertFalse(ValidityChecks.checkNotEqual(343224L, 343224L));
    }

    @Test
    public void testCheckNotEqualFloat() {
        assertTrue(ValidityChecks.checkNotEqual(234.98f, 3487.22f));
        assertFalse(ValidityChecks.checkNotEqual(433.2f, 433.2f));
    }

    @Test
    public void testCheckNotEqualDouble() {
        assertTrue(ValidityChecks.checkNotEqual(8723.87901d, 34786783.232d));
        assertFalse(ValidityChecks.checkNotEqual(52982.2322d, 52982.2322d));
    }

    @Test
    public void testCheckNotEqualObject() {
        assertTrue(ValidityChecks.checkNotEqual(null, null));
        assertTrue(ValidityChecks.checkNotEqual(new Object(), null));
        assertTrue(ValidityChecks.checkNotEqual(null, new Object()));
        assertTrue(ValidityChecks.checkNotEqual("test", "test2"));
        assertFalse(ValidityChecks.checkNotEqual("test3", "test3"));
    }

    @Test
    public void testCheckNotEqualArray() {
        assertTrue(ValidityChecks.checkNotEqual((String[]) null, (String[]) null));
        assertTrue(ValidityChecks.checkNotEqual(new String[]{"one"}, null));
        assertTrue(ValidityChecks.checkNotEqual(new String[]{"test", "test2", "test3"}, "test4"));
        assertFalse(ValidityChecks.checkNotEqual(new String[]{"test", "test2", "test3"}, "test3"));
        assertTrue(ValidityChecks.checkNotEqual(new int[]{89, 8, 5}, 3));
        assertFalse(ValidityChecks.checkNotEqual(new int[]{89, 8, 3}, 3));
    }

    @Test
    public void testCheckEqualBoolean() {
        assertFalse(ValidityChecks.checkEqual(true, false));
        assertTrue(ValidityChecks.checkEqual(true, true));
    }

    @Test
    public void testCheckEqualChar() {
        assertFalse(ValidityChecks.checkEqual('K', 'L'));
        assertTrue(ValidityChecks.checkEqual('a', 'a'));
    }

    @Test
    public void testCheckEqualByte() {
        assertFalse(ValidityChecks.checkEqual((byte) 98, (byte) 76));
        assertTrue(ValidityChecks.checkEqual((byte) 17, (byte) 17));
    }

    @Test
    public void testCheckEqualShort() {
        assertFalse(ValidityChecks.checkEqual((short) 8982, (short) 237));
        assertTrue(ValidityChecks.checkEqual((short) 23, (short) 23));
    }

    @Test
    public void testCheckEqualInteger() {
        assertFalse(ValidityChecks.checkEqual(622, 766));
        assertTrue(ValidityChecks.checkEqual(234, 234));
    }

    @Test
    public void testCheckEqualLong() {
        assertFalse(ValidityChecks.checkEqual(2324338L, 343876L));
        assertTrue(ValidityChecks.checkEqual(343224L, 343224L));
    }

    @Test
    public void testCheckEqualFloat() {
        assertFalse(ValidityChecks.checkEqual(234.98f, 3487.22f));
        assertTrue(ValidityChecks.checkEqual(433.2f, 433.2f));
    }

    @Test
    public void testCheckEqualDouble() {
        assertFalse(ValidityChecks.checkEqual(8723.87901d, 34786783.232d));
        assertTrue(ValidityChecks.checkEqual(52982.2322d, 52982.2322d));
    }

    @Test
    public void testCheckEqualObject() {
        assertTrue(ValidityChecks.checkEqual(null, null));
        assertTrue(ValidityChecks.checkEqual(new Object(), null));
        assertTrue(ValidityChecks.checkEqual(null, new Object()));
        assertFalse(ValidityChecks.checkEqual("test", "test2"));
        assertTrue(ValidityChecks.checkEqual("test3", "test3"));
    }

    @Test
    public void testCheckEqualArray() {
        assertTrue(ValidityChecks.checkEqual((String[]) null, (String[]) null));
        assertTrue(ValidityChecks.checkEqual(new String[]{"one"}, null));
        assertTrue(ValidityChecks.checkEqual(new String[]{"test3", "test3", "test3"}, "test3"));
        assertFalse(ValidityChecks.checkEqual(new String[]{"test3", "test3", "test2"}, "test3"));
        assertTrue(ValidityChecks.checkEqual(new int[]{5, 5, 5}, 5));
        assertFalse(ValidityChecks.checkEqual(new int[]{5, 5, 3}, 5));
    }

    @Test
    public void testCheckLengthByte() {
        assertTrue(ValidityChecks.checkLength((byte) 23, 0, 2));
        assertFalse(ValidityChecks.checkLength((byte) 120, 0, 2));
        assertFalse(ValidityChecks.checkLength((byte) 2, 2, 3));
    }

    @Test
    public void testCheckLengthChar() {
        assertTrue(ValidityChecks.checkLength('O', 0, 2));
        assertTrue(ValidityChecks.checkLength('K', 1, 2));
        assertFalse(ValidityChecks.checkLength('f', 2, 3));
    }

    @Test
    public void testCheckLengthShort() {
        assertTrue(ValidityChecks.checkLength((short) 2341, 0, 4));
        assertFalse(ValidityChecks.checkLength((short) 23419, 0, 4));
        assertFalse(ValidityChecks.checkLength((short) 143, 4, 7));
    }

    @Test
    public void testCheckLengthInt() {
        assertTrue(ValidityChecks.checkLength(2341, 0, 4));
        assertFalse(ValidityChecks.checkLength(23419, 0, 4));
        assertFalse(ValidityChecks.checkLength(143, 4, 7));
    }

    @Test
    public void testCheckLengthLong() {
        assertTrue(ValidityChecks.checkLength(2341L, 0, 4));
        assertFalse(ValidityChecks.checkLength(23419L, 0, 4));
        assertFalse(ValidityChecks.checkLength(143L, 4, 7));
    }

    @Test
    public void testCheckLengthFloat() {
        assertTrue(ValidityChecks.checkLength(21.78f, 0, 5));
        assertFalse(ValidityChecks.checkLength(231.78f, 0, 5));
        assertFalse(ValidityChecks.checkLength(12.8f, 5, 7));
    }

    @Test
    public void testCheckLengthDouble() {
        assertTrue(ValidityChecks.checkLength(21.78d, 0, 5));
        assertFalse(ValidityChecks.checkLength(231.78d, 0, 5));
        assertFalse(ValidityChecks.checkLength(12.8d, 5, 7));
    }

    @Test
    public void testCheckLength() {
        assertTrue(ValidityChecks.checkLength(null, 0, 0));

        StringBuffer string = new StringBuffer("testing");
        assertTrue(ValidityChecks.checkLength(string, 0, 7));
        assertTrue(ValidityChecks.checkLength(string, 5, 7));
        assertFalse(ValidityChecks.checkLength(string, 8, 10));
        assertFalse(ValidityChecks.checkLength(string, 0, 6));
        assertTrue(ValidityChecks.checkLength(string, -10, -20));
    }

    @Test
    public void testCheckRegexp() {
        assertTrue(ValidityChecks.checkRegexp(null, null));
        assertTrue(ValidityChecks.checkRegexp(null, ""));
        assertTrue(ValidityChecks.checkRegexp(null, "\\d+"));
        assertTrue(ValidityChecks.checkRegexp("", "\\d+"));
        assertTrue(ValidityChecks.checkRegexp(Integer.toString(2343), "\\d+"));
        assertFalse(ValidityChecks.checkRegexp("jhiuh", "\\d+"));
        assertFalse(ValidityChecks.checkRegexp(Integer.toString(2343), "\\d{4,"));
    }

    @Test
    public void testCheckEmail() {
        assertTrue(ValidityChecks.checkEmail("john.doe@something.com"));
        assertTrue(ValidityChecks.checkEmail("john+doe@something.com"));
        assertFalse(ValidityChecks.checkEmail("john.doe@something."));
    }

    @Test
    public void testCheckUrl() {
        assertTrue(ValidityChecks.checkUrl(null));
        assertTrue(ValidityChecks.checkUrl(""));
        assertTrue(ValidityChecks.checkUrl("http://uwyn.com"));
        assertTrue(ValidityChecks.checkUrl("https://www.uwyn.com"));
        assertFalse(ValidityChecks.checkUrl("htt:uwyn"));
    }

    @Test
    public void testLaterThanNow() {
        assertTrue(ValidityChecks.checkLaterThanNow(null));
        assertTrue(ValidityChecks.checkLaterThanNow(new Date(2003, Calendar.APRIL, 1)));
    }

    @Test
    public void testLimitedDate() {
        assertTrue(ValidityChecks.checkLimitedDate(null, null, null));
        assertTrue(ValidityChecks.checkLimitedDate("test", null, null));
        assertTrue(ValidityChecks.checkLimitedDate(new Date(2003, 12, 11), null, null));
        assertTrue(ValidityChecks.checkLimitedDate(new Date(2003, 12, 11), new Date(2003, Calendar.APRIL, 1), new Date(2004, Calendar.APRIL, 1)));
        assertFalse(ValidityChecks.checkLimitedDate(new Date(2003, 12, 11), new Date(2004, Calendar.APRIL, 1), null));
        assertFalse(ValidityChecks.checkLimitedDate(new Date(2003, 12, 11), null, new Date(2003, Calendar.DECEMBER, 1)));
        assertFalse(ValidityChecks.checkLimitedDate(new Date(2003, 12, 11), new Date(2004, Calendar.APRIL, 1), new Date(2004, Calendar.MAY, 1)));
        assertFalse(ValidityChecks.checkLimitedDate(new Date(2003, 12, 11), new Date(2003, Calendar.APRIL, 1), new Date(2003, Calendar.MAY, 1)));
    }

    @Test
    public void testInList() {
        assertTrue(ValidityChecks.checkInList(null, null));
        assertTrue(ValidityChecks.checkInList(null, new String[0]));
        assertTrue(ValidityChecks.checkInList("test", new String[0]));
        assertTrue(ValidityChecks.checkInList("test", null));
        assertTrue(ValidityChecks.checkInList("test", new String[]{"one", "two", "test", "aaa"}));
        assertTrue(ValidityChecks.checkInList("", new String[]{"in", "ok"}));
        assertTrue(ValidityChecks.checkInList(new String[0], new String[]{"in", "ok"}));
        assertTrue(ValidityChecks.checkInList(new String[]{"test", "one"}, new String[]{"one", "two", "test", "aaa"}));
        assertFalse(ValidityChecks.checkInList(new String[]{"test", "three"}, new String[]{"one", "two", "test", "aaa"}));
        assertTrue(ValidityChecks.checkInList(new int[]{98, 17, 3}, new String[]{"3", "98", "4", "17"}));
        assertFalse(ValidityChecks.checkInList(new int[]{1, 98, 17, 3}, new String[]{"3", "98", "4", "17"}));
    }

    @Test
    public void testCheckRangeByte() {
        assertTrue(ValidityChecks.checkRange((byte) 0, (byte) 0, (byte) 23));
        assertTrue(ValidityChecks.checkRange((byte) 12, (byte) 0, (byte) 23));
        assertTrue(ValidityChecks.checkRange((byte) 23, (byte) 7, (byte) 23));
        assertFalse(ValidityChecks.checkRange((byte) 24, (byte) 7, (byte) 23));
        assertFalse(ValidityChecks.checkRange((byte) 6, (byte) 7, (byte) 23));
    }

    @Test
    public void testCheckRangeChar() {
        assertTrue(ValidityChecks.checkRange('b', 'b', 'y'));
        assertTrue(ValidityChecks.checkRange('g', 'b', 'y'));
        assertTrue(ValidityChecks.checkRange('y', 'b', 'y'));
        assertFalse(ValidityChecks.checkRange('a', 'b', 'y'));
        assertFalse(ValidityChecks.checkRange('z', 'b', 'y'));
    }

    @Test
    public void testCheckRangeShort() {
        assertTrue(ValidityChecks.checkRange((short) 0, (short) 0, (short) 23));
        assertTrue(ValidityChecks.checkRange((short) 8, (short) 0, (short) 23));
        assertTrue(ValidityChecks.checkRange((short) 23, (short) 7, (short) 23));
        assertFalse(ValidityChecks.checkRange((short) 24, (short) 7, (short) 23));
        assertFalse(ValidityChecks.checkRange((short) 6, (short) 7, (short) 23));
    }

    @Test
    public void testCheckRangeInt() {
        assertTrue(ValidityChecks.checkRange(0, 0, 23));
        assertTrue(ValidityChecks.checkRange(9, 0, 23));
        assertTrue(ValidityChecks.checkRange(23, 7, 23));
        assertFalse(ValidityChecks.checkRange(24, 7, 23));
        assertFalse(ValidityChecks.checkRange(6, 7, 23));
    }

    @Test
    public void testCheckRangeLong() {
        assertTrue(ValidityChecks.checkRange(0L, 0, 23));
        assertTrue(ValidityChecks.checkRange(12L, 0, 23));
        assertTrue(ValidityChecks.checkRange(23L, 7, 23));
        assertFalse(ValidityChecks.checkRange(24L, 7, 23));
        assertFalse(ValidityChecks.checkRange(6L, 7, 23));
    }

    @Test
    public void testCheckRangeFloat() {
        assertTrue(ValidityChecks.checkRange(0.7f, 0.7f, 23.9f));
        assertTrue(ValidityChecks.checkRange(15.9f, 0.7f, 23.9f));
        assertTrue(ValidityChecks.checkRange(23.9f, 0.7f, 23.9f));
        assertFalse(ValidityChecks.checkRange(0.699f, 0.7f, 23.9f));
        assertFalse(ValidityChecks.checkRange(23.901f, 0.7f, 23.9f));
    }

    @Test
    public void testCheckRangeDouble() {
        assertTrue(ValidityChecks.checkRange(0.7d, 0.7d, 23.9d));
        assertTrue(ValidityChecks.checkRange(19.23d, 0.7d, 23.9d));
        assertTrue(ValidityChecks.checkRange(23.9d, 0.7d, 23.9d));
        assertFalse(ValidityChecks.checkRange(0.699d, 0.7d, 23.9d));
        assertFalse(ValidityChecks.checkRange(23.901d, 0.7d, 23.9d));
    }

    @Test
    public void testCheckRangeObject() {
        assertTrue(ValidityChecks.checkRange(null, "abc", "zrr"));
        assertTrue(ValidityChecks.checkRange(new Object(), "aaa", "aaa"));
        assertTrue(ValidityChecks.checkRange("bgt", null, null));
        assertTrue(ValidityChecks.checkRange("abc", "abc", "zrr"));
        assertTrue(ValidityChecks.checkRange("bgt", "abc", "zrr"));
        assertTrue(ValidityChecks.checkRange("zrr", "abc", "zrr"));
        assertFalse(ValidityChecks.checkRange("abb", "abc", "zrr"));
        assertFalse(ValidityChecks.checkRange("zrs", "abc", "zrr"));
    }

    @Test
    public void testCheckRangeArray() {
        assertTrue(ValidityChecks.checkRange(new String[]{"abc", "ccc", "zrr"}, "abc", "zrr"));
        assertFalse(ValidityChecks.checkRange(new String[]{"abb", "ccc", "zrr"}, "abc", "zrr"));
        assertTrue(ValidityChecks.checkRange(new int[]{89, 7, 3}, 3, 89));
        assertFalse(ValidityChecks.checkRange(new int[]{89, 7, 3}, 4, 89));
    }

    @Test
    public void testCheckFormat() {
        assertTrue(ValidityChecks.checkFormat(null, null));
        assertTrue(ValidityChecks.checkFormat(new Object(), null));
        assertTrue(ValidityChecks.checkFormat(new Object(), new SimpleDateFormat("dd/MM/yyyy")));
        assertTrue(ValidityChecks.checkFormat("testing", null));
        assertTrue(ValidityChecks.checkFormat("20/02/2004", new SimpleDateFormat("dd/MM/yyyy")));
        assertFalse(ValidityChecks.checkFormat("2/2/2004", new SimpleDateFormat("dd/MM/yyyy")));
        assertFalse(ValidityChecks.checkFormat("31/02/2004", new SimpleDateFormat("dd/MM/yyyy")));
        assertFalse(ValidityChecks.checkFormat("testing", new SimpleDateFormat("dd/MM/yyyy")));
    }
}
