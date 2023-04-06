/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.tools.BeanImpl;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestConstrainedProperty {
    @Test
    void testIllegalInstantiation() {
        try {
            new ConstrainedProperty(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new ConstrainedProperty("");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    void testInstantiation() {
        var property = new ConstrainedProperty("the_name");
        assertEquals("the_name", property.getPropertyName());
        assertEquals("the_name", property.getSubjectName());
        assertFalse(property.isNotNull());
        assertFalse(property.isNotEmpty());
        assertFalse(property.isNotEqual());
        assertFalse(property.isUnique());
        assertFalse(property.isIdentifier());
        assertTrue(property.isEditable());
        assertTrue(property.isSaved());
        assertFalse(property.isDisplayedRaw());
        assertTrue(property.isPersistent());
        assertFalse(property.hasLimitedLength());
        assertFalse(property.hasPrecision());
        assertFalse(property.hasScale());
        assertEquals(-1, property.getMinLength());
        assertEquals(-1, property.getMaxLength());
        assertNull(property.getRegexp());
        assertFalse(property.matchesRegexp());
        assertFalse(property.isEmail());
        assertFalse(property.isUrl());
        assertNull(property.getMinDate());
        assertNull(property.getMaxDate());
        assertFalse(property.isLimitedDate());
        assertNull(property.getFormat());
        assertFalse(property.isFormatted());
        assertNull(property.getInList());
        assertFalse(property.isInList());
        assertNull(property.getDefaultValue());
        assertFalse(property.hasDefaultValue());
        assertFalse(property.hasManyToOne());
        assertNull(property.getManyToOne());
        assertFalse(property.isEmail());
    }

    @Test
    void testSubjectName() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.subjectName("subject"));
        assertEquals("subject", property.getSubjectName());

        property.subjectName("subject2");
        assertEquals("subject2", property.getSubjectName());
    }

    @Test
    void testMandatory() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notNull(true));
        assertTrue(property.isNotNull());

        property.setNotNull(false);
        assertFalse(property.isNotNull());
    }

    @Test
    void testNotEmpty() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEmpty(true));
        assertTrue(property.isNotEmpty());

        assertSame(property, property.notEmpty(false));
        assertFalse(property.isNotEmpty());
    }

    @Test
    void testNotEqualBoolean() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual(true));
        assertTrue(property.isNotEqual());
        assertEquals(true, property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqualChar() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual('K'));
        assertTrue(property.isNotEqual());
        assertEquals('K', property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqualByte() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual((byte) 23));
        assertTrue(property.isNotEqual());
        assertEquals((byte) 23, property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqualShort() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual((short) 432));
        assertTrue(property.isNotEqual());
        assertEquals((short) 432, property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqualInt() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual(5452));
        assertTrue(property.isNotEqual());
        assertEquals(5452, property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqualLong() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual(34386434L));
        assertTrue(property.isNotEqual());
        assertEquals(34386434L, property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqualFloat() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual(435.23f));
        assertTrue(property.isNotEqual());
        assertEquals(435.23f, property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqualDouble() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual(3983498.234d));
        assertTrue(property.isNotEqual());
        assertEquals(3983498.234d, property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testNotEqual() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.notEqual("value"));
        assertTrue(property.isNotEqual());
        assertEquals("value", property.getNotEqual());

        assertSame(property, property.notEqual(null));
        assertFalse(property.isNotEqual());
        assertNull(property.getNotEqual());
    }

    @Test
    void testUnique() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.unique(true));
        assertTrue(property.isUnique());

        assertSame(property, property.unique(false));
        assertFalse(property.isUnique());
    }

    @Test
    void testIdentifier() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.identifier(true));
        assertTrue(property.isIdentifier());

        assertSame(property, property.identifier(false));
        assertFalse(property.isIdentifier());
    }

    @Test
    void testEditable() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.editable(false));
        assertFalse(property.isEditable());

        assertSame(property, property.editable(true));
        assertTrue(property.isEditable());
    }

    @Test
    void testSaved() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.saved(false));
        assertFalse(property.isSaved());

        assertSame(property, property.saved(true));
        assertTrue(property.isSaved());
    }

    @Test
    void testDisplayedRaw() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.displayedRaw(true));
        assertTrue(property.isDisplayedRaw());

        assertSame(property, property.displayedRaw(false));
        assertFalse(property.isDisplayedRaw());
    }

    @Test
    void testPersistent() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.persistent(false));
        assertFalse(property.isPersistent());

        assertSame(property, property.persistent(true));
        assertTrue(property.isPersistent());
    }

    @Test
    void testLimitedLength() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.minLength(5));
        assertTrue(property.hasLimitedLength());
        assertEquals(5, property.getMinLength());

        assertSame(property, property.minLength(0));
        assertFalse(property.hasLimitedLength());

        assertSame(property, property.maxLength(0));
        assertTrue(property.hasLimitedLength());
        assertTrue(property.hasPrecision());
        assertEquals(0, property.getMaxLength());
        assertEquals(0, property.getPrecision());

        assertSame(property, property.maxLength(10));
        assertTrue(property.hasLimitedLength());
        assertTrue(property.hasPrecision());
        assertEquals(10, property.getMaxLength());
        assertEquals(10, property.getPrecision());

        assertSame(property, property.maxLength(-1));
        assertFalse(property.hasLimitedLength());
        assertFalse(property.hasPrecision());
        assertEquals(-1, property.getMaxLength());
        assertEquals(-1, property.getPrecision());

        assertSame(property, property.minLength(7));
        assertSame(property, property.maxLength(27));
        assertTrue(property.hasLimitedLength());
        assertEquals(7, property.getMinLength());
        assertEquals(27, property.getMaxLength());
    }

    @Test
    void testPrecision() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.precision(0));
        assertTrue(property.hasPrecision());
        assertTrue(property.hasLimitedLength());
        assertEquals(0, property.getPrecision());
        assertEquals(0, property.getMaxLength());

        assertSame(property, property.precision(10));
        assertTrue(property.hasPrecision());
        assertTrue(property.hasLimitedLength());
        assertEquals(10, property.getPrecision());
        assertEquals(10, property.getMaxLength());

        assertSame(property, property.precision(-1));
        assertFalse(property.hasPrecision());
        assertFalse(property.hasLimitedLength());
        assertEquals(-1, property.getPrecision());
        assertEquals(-1, property.getMaxLength());
    }

    @Test
    void testScale() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.scale(0));
        assertTrue(property.hasScale());
        assertEquals(0, property.getScale());

        assertSame(property, property.scale(10));
        assertTrue(property.hasScale());
        assertEquals(10, property.getScale());

        assertSame(property, property.scale(-1));
        assertFalse(property.hasScale());
        assertEquals(-1, property.getScale());
    }

    @Test
    void testRegexp() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.regexp("/l/k/"));
        assertTrue(property.matchesRegexp());
        assertEquals("/l/k/", property.getRegexp());

        assertSame(property, property.regexp(null));
        assertFalse(property.matchesRegexp());
        assertNull(property.getRegexp());
    }

    @Test
    void testEmail() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.email(true));
        assertTrue(property.isEmail());

        assertSame(property, property.email(false));
        assertFalse(property.isEmail());
    }

    @Test
    void testUrl() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.url(true));
        assertTrue(property.isUrl());

        assertSame(property, property.url(false));
        assertFalse(property.isUrl());
    }

    @Test
    void testLimitedDate() {
        var date1 = new Date();
        var date2 = new Date();

        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.minDate(date1));
        assertTrue(property.isLimitedDate());
        assertSame(date1, property.getMinDate());

        assertSame(property, property.minDate(null));
        assertFalse(property.isLimitedDate());
        assertNull(property.getMinDate());

        assertSame(property, property.maxDate(date2));
        assertTrue(property.isLimitedDate());
        assertSame(date2, property.getMaxDate());

        assertSame(property, property.maxDate(null));
        assertFalse(property.isLimitedDate());
        assertNull(property.getMaxDate());

        assertSame(property, property.minDate(date2));
        assertSame(property, property.maxDate(date1));
        assertTrue(property.isLimitedDate());
        assertSame(date2, property.getMinDate());
        assertSame(date1, property.getMaxDate());
    }

    @Test
    void testFormat() {
        var format = RifeConfig.tools().getSimpleDateFormat("dd/MM/yyyy");

        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.format(format));
        assertTrue(property.isFormatted());
        assertSame(format, property.getFormat());

        assertSame(property, property.format(null));
        assertFalse(property.isFormatted());
        assertNull(property.getFormat());
    }

    @Test
    void testInList() {
        var list = new String[]{"one", "two"};

        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.inList(list));
        assertTrue(property.isInList());
        assertSame(list, property.getInList());

        assertSame(property, property.inList((String[]) null));
        assertFalse(property.isInList());
        assertNull(property.getInList());

        list = new String[0];
        assertSame(property, property.inList(list));
        assertFalse(property.isInList());
        assertSame(list, property.getInList());
    }

    @Test
    void testRangeByte() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin((byte) 5));
        assertTrue(property.isRange());
        assertEquals((byte) 5, property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd((byte) 78));
        assertTrue(property.isRange());
        assertEquals((byte) 78, property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin((byte) 7));
        assertSame(property, property.rangeEnd((byte) 27));
        assertTrue(property.isRange());
        assertEquals((byte) 7, property.getRangeBegin());
        assertEquals((byte) 27, property.getRangeEnd());
    }

    @Test
    void testRangeChar() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin('b'));
        assertTrue(property.isRange());
        assertEquals('b', property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd('z'));
        assertTrue(property.isRange());
        assertEquals('z', property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin('d'));
        assertSame(property, property.rangeEnd('g'));
        assertTrue(property.isRange());
        assertEquals('d', property.getRangeBegin());
        assertEquals('g', property.getRangeEnd());
    }

    @Test
    void testRangeShort() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin((short) 23));
        assertTrue(property.isRange());
        assertEquals((short) 23, property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd((short) 534));
        assertTrue(property.isRange());
        assertEquals((short) 534, property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin((short) 39));
        assertSame(property, property.rangeEnd((short) 3534));
        assertTrue(property.isRange());
        assertEquals((short) 39, property.getRangeBegin());
        assertEquals((short) 3534, property.getRangeEnd());
    }

    @Test
    void testRangeInt() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin(534));
        assertTrue(property.isRange());
        assertEquals(534, property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd(6344));
        assertTrue(property.isRange());
        assertEquals(6344, property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin(423));
        assertSame(property, property.rangeEnd(89724));
        assertTrue(property.isRange());
        assertEquals(423, property.getRangeBegin());
        assertEquals(89724, property.getRangeEnd());
    }

    @Test
    void testRangeLong() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin(242L));
        assertTrue(property.isRange());
        assertEquals(242L, property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd(783658L));
        assertTrue(property.isRange());
        assertEquals(783658L, property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin(2432L));
        assertSame(property, property.rangeEnd(9872949L));
        assertTrue(property.isRange());
        assertEquals(2432L, property.getRangeBegin());
        assertEquals(9872949L, property.getRangeEnd());
    }

    @Test
    void testRangeFloat() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin(234.32f));
        assertTrue(property.isRange());
        assertEquals(234.32f, property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd(9887.23f));
        assertTrue(property.isRange());
        assertEquals(9887.23f, property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin(12.8f));
        assertSame(property, property.rangeEnd(89736.25f));
        assertTrue(property.isRange());
        assertEquals(12.8f, property.getRangeBegin());
        assertEquals(89736.25f, property.getRangeEnd());
    }

    @Test
    void testRangeDouble() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin(290.523d));
        assertTrue(property.isRange());
        assertEquals(290.523d, property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd(7623847.3453d));
        assertTrue(property.isRange());
        assertEquals(7623847.3453d, property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin(24.2348d));
        assertSame(property, property.rangeEnd(423987.2345d));
        assertTrue(property.isRange());
        assertEquals(24.2348d, property.getRangeBegin());
        assertEquals(423987.2345d, property.getRangeEnd());
    }

    @Test
    void testRange() {
        var property = new ConstrainedProperty("the_name");

        assertSame(property, property.rangeBegin(5));
        assertTrue(property.isRange());
        assertEquals(5, property.getRangeBegin());

        assertSame(property, property.rangeBegin(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeBegin());

        assertSame(property, property.rangeEnd(78));
        assertTrue(property.isRange());
        assertEquals(78, property.getRangeEnd());

        assertSame(property, property.rangeEnd(null));
        assertFalse(property.isRange());
        assertNull(property.getRangeEnd());

        assertSame(property, property.rangeBegin(7));
        assertSame(property, property.rangeEnd(27));
        assertTrue(property.isRange());
        assertEquals(7, property.getRangeBegin());
        assertEquals(27, property.getRangeEnd());
    }

    @Test
    void testDefaultValueChar() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue('C'));
        assertTrue(property.hasDefaultValue());
        assertEquals('C', property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueBoolean() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue(false));
        assertTrue(property.hasDefaultValue());
        assertEquals(false, property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueByte() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue((byte) 89));
        assertTrue(property.hasDefaultValue());
        assertEquals((byte) 89, property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueDouble() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue(87923.878d));
        assertTrue(property.hasDefaultValue());
        assertEquals(87923.878d, property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueFloat() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue(61.9f));
        assertTrue(property.hasDefaultValue());
        assertEquals(61.9f, property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueInt() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue(9824));
        assertTrue(property.hasDefaultValue());
        assertEquals(9824, property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueLong() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue(2332979L));
        assertTrue(property.hasDefaultValue());
        assertEquals(2332979L, property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueShort() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue((short) 221));
        assertTrue(property.hasDefaultValue());
        assertEquals((short) 221, property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testDefaultValueObject() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.defaultValue("the default"));
        assertTrue(property.hasDefaultValue());
        assertEquals("the default", property.getDefaultValue());

        assertSame(property, property.defaultValue(null));
        assertFalse(property.hasDefaultValue());
        assertNull(property.getDefaultValue());
    }

    @Test
    void testManyToOne() {
        var property = new ConstrainedProperty("the_name");
        assertSame(property, property.manyToOne(BeanImpl.class, "column"));
        assertTrue(property.hasManyToOne());
        assertEquals("column", property.getManyToOne().getColumn());
        assertNull(property.getManyToOne().getTable());
        assertEquals("BeanImpl", property.getManyToOne().getDerivedTable());

        property.manyToOne("tablename", "column");
        assertTrue(property.hasManyToOne());
        assertEquals("column", property.getManyToOne().getColumn());
        assertEquals("tablename", property.getManyToOne().getTable());
        assertEquals("tablename", property.getManyToOne().getDerivedTable());
    }
}
