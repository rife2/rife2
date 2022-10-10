/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.tools.exceptions.BeanUtilsException;
import rife.tools.exceptions.SerializationUtilsErrorException;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestBeanUtils {
    private BeanImpl getPopulatedBean() {
        BeanImpl bean = new BeanImpl();
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
        cal.set(Calendar.MILLISECOND, 153);
        bean.setPropertyString("thisisastring");
        bean.setPropertyStringBuffer(new StringBuffer("butthisisastringbuffer"));
        bean.setPropertyDate(cal.getTime());
        bean.setPropertyCalendar(cal);
        bean.setPropertySqlDate(new java.sql.Date(cal.getTime().getTime()));
        bean.setPropertyTime(new Time(cal.getTime().getTime()));
        bean.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));
        bean.setPropertyChar('g');
        bean.setPropertyBoolean(false);
        bean.setPropertyByte((byte) 53);
        bean.setPropertyDouble(84578.42d);
        bean.setPropertyFloat(35523.967f);
        bean.setPropertyInt(978);
        bean.setPropertyLong(87346L);
        bean.setPropertyShort((short) 31);
        bean.setPropertyBigDecimal(new BigDecimal("8347365990.387437894678"));

        return bean;
    }

    @Test
    public void testSetUppercaseBeanPropertyIllegalArguments()
    throws BeanUtilsException {
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        try {
            BeanUtils.setUppercasedBeanProperty(null, null, null, bean_properties, new BeanImpl2(), null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BeanUtils.setUppercasedBeanProperty("propertyString", null, null, null, new BeanImpl2(), null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BeanUtils.setUppercasedBeanProperty("propertyString", null, null, bean_properties, null, null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            BeanUtils.setUppercasedBeanProperty("propertyString", null, null, bean_properties, new BeanImpl2(), null);
        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException not expected.");
        }
    }

    @Test
    public void testSetUppercaseBeanPropertyNoOpArguments()
    throws BeanUtilsException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", null, null, bean_properties, bean, null);
        assertNull(bean.getPropertyString());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", new String[0], null, bean_properties, bean, null);
        assertNull(bean.getPropertyString());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", new String[]{"one", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyString(), "one");
    }

    @Test
    public void testSetUppercaseBeanPropertyNoSetter()
    throws BeanUtilsException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        assertEquals(bean.getPropertyReadonly(), 23L);
        BeanUtils.setUppercasedBeanProperty("propertyReadonly", new String[]{"42131"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyReadonly(), 23L);
    }

    @Test
    public void testSetUppercaseBeanProperty()
    throws BeanUtilsException, ParseException, SerializationUtilsErrorException {
        BeanImpl2 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl2.class);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyString", new String[]{"one", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyString(), "one");

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyInt", new String[]{"438", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyInt(), 438);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyChar", new String[]{"E", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyChar(), 'E');

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBoolean", new String[]{"true", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertTrue(bean.isPropertyBoolean());

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByte", new String[]{"27", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyByte(), 27);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDouble", new String[]{"80756.6287", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyDouble(), 80756.6287d);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloat", new String[]{"435.557", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyFloat(), 435.557f);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLong", new String[]{"122875", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyLong(), 122875);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShort", new String[]{"3285", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyShort(), 3285);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimal", new String[]{"983743.343", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyBigDecimal(), new BigDecimal("983743.343"));

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObject", new String[]{"438", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyIntegerObject(), 438);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharacterObject", new String[]{"E", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyCharacterObject(), 'E');

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanObject", new String[]{"true", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyBooleanObject(), Boolean.TRUE);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteObject", new String[]{"27", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyByteObject(), (byte) 27);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObject", new String[]{"80756.6287", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyDoubleObject(), 80756.6287d);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObject", new String[]{"435.557", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyFloatObject(), 435.557f);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongObject", new String[]{"122875", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyLongObject(), 122875);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortObject", new String[]{"3285", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyShortObject(), (short) 3285);

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuffer", new String[]{"one1", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyStringBuffer().toString(), "one1");

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuilder", new String[]{"one2", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyStringBuilder().toString(), "one2");

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDate", new String[]{"2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertyDate(), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"));

        bean = new BeanImpl2();
        BeanImpl2.SerializableType serializable = new BeanImpl2.SerializableType(5686, "Testing");
        BeanUtils.setUppercasedBeanProperty("propertySerializableType", new String[]{SerializationUtils.serializeToString(serializable), "two"}, null, bean_properties, bean, new BeanImpl2());
        assertEquals(bean.getPropertySerializableType(), serializable);


        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringArray", new String[]{"one", "two"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyStringArray(), new String[]{"one", "two"});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntArray", new String[]{"438", "98455", "711"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyIntArray(), new int[]{438, 98455, 711});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharArray", new String[]{"E", "a", "x"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyCharArray(), new char[]{'E', 'a', 'x'});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanArray", new String[]{"true", "0", "t", "1"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyBooleanArray(), new boolean[]{true, false, true, true});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteArray", new String[]{"27", "78"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyByteArray(), new byte[]{27, 78});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleArray", new String[]{"80756.6287", "3214.75", "85796.6237"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyDoubleArray(), new double[]{80756.6287d, 3214.75d, 85796.6237d});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatArray", new String[]{"435.557", "589.5"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyFloatArray(), new float[]{435.557f, 589.5f});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongArray", new String[]{"122875", "8526780", "3826589"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyLongArray(), new long[]{122875, 8526780, 3826589});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortArray", new String[]{"3285", "58"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyShortArray(), new short[]{3285, 58});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObjectArray", new String[]{"438", "7865", "475"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyIntegerObjectArray(), new Integer[]{438, 7865, 475});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyCharacterObjectArray", new String[]{"E", "z"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyCharacterObjectArray(), new Character[]{'E', 'z'});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBooleanObjectArray", new String[]{"fslse", "1", "true"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyBooleanObjectArray(), new Boolean[]{false, true, true});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyByteObjectArray", new String[]{"27", "78"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyByteObjectArray(), new Byte[]{(byte) 27, (byte) 78});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObjectArray", new String[]{"80756.6287", "5876.14", "3268.57"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyDoubleObjectArray(), new Double[]{80756.6287d, 5876.14d, 3268.57d});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObjectArray", new String[]{"435.557", "7865.66"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyFloatObjectArray(), new Float[]{435.557f, 7865.66f});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyLongObjectArray", new String[]{"122875", "5687621", "66578"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyLongObjectArray(), new Long[]{122875L, 5687621L, 66578L});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyShortObjectArray", new String[]{"3285", "6588"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyShortObjectArray(), new Short[]{(short) 3285, (short) 6588});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimalArray", new String[]{"32859837434343983.83749837498373434", "65884343.343"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyBigDecimalArray(), new BigDecimal[]{new BigDecimal("32859837434343983.83749837498373434"), new BigDecimal("65884343343E-3")});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBufferArray", new String[]{"one1", "two2"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(ArrayUtils.createStringArray(bean.getPropertyStringBufferArray()), new String[]{"one1", "two2"});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyStringBuilderArray", new String[]{"three3", "four4"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(ArrayUtils.createStringArray(bean.getPropertyStringBuilderArray()), new String[]{"three3", "four4"});

        bean = new BeanImpl2();
        BeanUtils.setUppercasedBeanProperty("propertyDateArray", new String[]{"2006-08-04 10:45", "2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertyDateArray(), new Date[]{RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05")});

        bean = new BeanImpl2();
        BeanImpl2.SerializableType serializable1 = new BeanImpl2.SerializableType(5682, "AnotherTest");
        BeanImpl2.SerializableType serializable2 = new BeanImpl2.SerializableType(850, "WhatTest");
        BeanUtils.setUppercasedBeanProperty("propertySerializableTypeArray", new String[]{SerializationUtils.serializeToString(serializable1), SerializationUtils.serializeToString(serializable2)}, null, bean_properties, bean, new BeanImpl2());
        assertArrayEquals(bean.getPropertySerializableTypeArray(), new BeanImpl2.SerializableType[]{serializable1, serializable2});
    }

    @Test
    public void testSetUppercaseBeanPropertyConstrained()
    throws BeanUtilsException, ParseException, SerializationUtilsErrorException {
        BeanImpl3 bean;
        Map<String, PropertyDescriptor> bean_properties = BeanUtils.getUppercasedBeanProperties(BeanImpl3.class);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDate", new String[]{"custom format 2006-08-04 10:45", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyDate(), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyInt", new String[]{"$438", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyInt(), 438);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByte", new String[]{"2,700%", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyByte(), 27);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDouble", new String[]{"80,756.6287", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyDouble(), 80756.6287d);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloat", new String[]{"435,557", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyFloat(), 435.557f);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLong", new String[]{"$122,875.00", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyLong(), 122875);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShort", new String[]{"¤3285", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyShort(), 3285);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimal", new String[]{"4353344987349830948394893,55709384093", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyBigDecimal(), new BigDecimal("435334498734983094839489355709384093E-11"));

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObject", new String[]{"$438", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyIntegerObject(), 438);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteObject", new String[]{"2,700%", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyByteObject(), (byte) 27);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObject", new String[]{"80,756.6287", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyDoubleObject(), 80756.6287d);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObject", new String[]{"435,557", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyFloatObject(), 435.557f);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongObject", new String[]{"$122,875.00", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyLongObject(), 122875);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortObject", new String[]{"¤3285", "two"}, null, bean_properties, bean, new BeanImpl3());
        assertEquals(bean.getPropertyShortObject(), (short) 3285);

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDateArray", new String[]{"custom format 2006-08-04 10:45", "custom format 2006-07-08 11:05"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyDateArray(), new Date[]{RifeConfig.tools().getDefaultInputDateFormat().parse("2006-08-04 10:45"), RifeConfig.tools().getDefaultInputDateFormat().parse("2006-07-08 11:05")});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntArray", new String[]{"$438", "$98455", "$711"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyIntArray(), new int[]{438, 98455, 711});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteArray", new String[]{"2,700%", "7,800%"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyByteArray(), new byte[]{27, 78});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleArray", new String[]{"80,756.6287", "3,214.75", "85,796.6237"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyDoubleArray(), new double[]{80756.6287d, 3214.75d, 85796.6237d});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatArray", new String[]{"435,557", "589,5"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyFloatArray(), new float[]{435.557f, 589.5f});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongArray", new String[]{"$122,875.00", "$8,526,780.00", "$3,826,589.00"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyLongArray(), new long[]{122875, 8526780, 3826589});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortArray", new String[]{"¤3285", "¤58"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyShortArray(), new short[]{3285, 58});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyIntegerObjectArray", new String[]{"$438", "$7865", "$475"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyIntegerObjectArray(), new Integer[]{438, 7865, 475});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyByteObjectArray", new String[]{"2,700%", "7,800%"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyByteObjectArray(), new Byte[]{(byte) 27, (byte) 78});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyDoubleObjectArray", new String[]{"80,756.6287", "5,876.14", "3,268.57"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyDoubleObjectArray(), new Double[]{80756.6287d, 5876.14d, 3268.57d});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyFloatObjectArray", new String[]{"435,557", "7865,66"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyFloatObjectArray(), new Float[]{435.557f, 7865.66f});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyLongObjectArray", new String[]{"$122,875.00", "$5,687,621.00", "$66,578.00"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyLongObjectArray(), new Long[]{122875L, 5687621L, 66578L});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyShortObjectArray", new String[]{"¤3285", "¤6588"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyShortObjectArray(), new Short[]{(short) 3285, (short) 6588});

        bean = new BeanImpl3();
        BeanUtils.setUppercasedBeanProperty("propertyBigDecimalArray", new String[]{"97687687998978673545669789,0000000000001", "34353"}, null, bean_properties, bean, new BeanImpl3());
        assertArrayEquals(bean.getPropertyBigDecimalArray(), new BigDecimal[]{new BigDecimal("976876879989786735456697890000000000001E-13"), new BigDecimal("3.4353E4")});
    }

    @Test
    public void testPropertyNamesIllegal() {
        try {
            assertEquals(0, BeanUtils.getPropertyNames(null, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesEmpty() {
        try {
            assertEquals(0, BeanUtils.getPropertyNames(Object.class, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNames() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class, null, null, null);
            assertEquals(property_names.size(), 16);
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyShort"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(property_names.size(), 17);
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyShort"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(property_names.size(), 17);
            assertTrue(property_names.contains("propertyWriteonly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyShort"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_names.size(), 16);
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_names.size(), 17);
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_names.size(), 17);
            assertTrue(property_names.contains("PREFIX:propertyWriteonly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesIncluded() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null);
            assertEquals(property_names.size(), 7);
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesIncludedGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null);
            assertEquals(property_names.size(), 8);
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesIncludedSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null);
            assertEquals(property_names.size(), 8);
            assertTrue(property_names.contains("propertyWriteonly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertySqlDate"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyByte"));
            assertTrue(property_names.contains("propertyDouble"));
            assertTrue(property_names.contains("propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesIncludedPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_names.size(), 7);
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesIncludedPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_names.size(), 8);
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesIncludedPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_names.size(), 8);
            assertTrue(property_names.contains("PREFIX:propertyWriteonly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertySqlDate"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyByte"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
            assertTrue(property_names.contains("PREFIX:propertyShort"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesExcluded() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(property_names.size(), 9);
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesExcludedGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(property_names.size(), 10);
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesExcludedSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(property_names.size(), 10);
            assertTrue(property_names.contains("propertyWriteonly"));
            assertTrue(property_names.contains("propertyString"));
            assertTrue(property_names.contains("propertyDate"));
            assertTrue(property_names.contains("propertyTime"));
            assertTrue(property_names.contains("propertyTimestamp"));
            assertTrue(property_names.contains("propertyBoolean"));
            assertTrue(property_names.contains("propertyFloat"));
            assertTrue(property_names.contains("propertyInt"));
            assertTrue(property_names.contains("propertyLong"));
            assertTrue(property_names.contains("propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesExcludedPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(property_names.size(), 9);
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesExcludedPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(property_names.size(), 10);
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesExcludedPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(property_names.size(), 10);
            assertTrue(property_names.contains("PREFIX:propertyWriteonly"));
            assertTrue(property_names.contains("PREFIX:propertyString"));
            assertTrue(property_names.contains("PREFIX:propertyDate"));
            assertTrue(property_names.contains("PREFIX:propertyTime"));
            assertTrue(property_names.contains("PREFIX:propertyTimestamp"));
            assertTrue(property_names.contains("PREFIX:propertyBoolean"));
            assertTrue(property_names.contains("PREFIX:propertyFloat"));
            assertTrue(property_names.contains("PREFIX:propertyInt"));
            assertTrue(property_names.contains("PREFIX:propertyLong"));
            assertTrue(property_names.contains("PREFIX:propertyBigDecimal"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesFiltered() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertySqlDate", "propertyByte", "propertyShort"},
                null);
            assertEquals(property_names.size(), 4);
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesFilteredGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertySqlDate", "propertyByte", "propertyShort"},
                null);
            assertEquals(property_names.size(), 5);
            assertTrue(property_names.contains("propertyReadonly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesFilteredSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertySqlDate", "propertyByte", "propertyShort"},
                null);
            assertEquals(property_names.size(), 5);
            assertTrue(property_names.contains("propertyWriteonly"));
            assertTrue(property_names.contains("propertyStringBuffer"));
            assertTrue(property_names.contains("propertyCalendar"));
            assertTrue(property_names.contains("propertyChar"));
            assertTrue(property_names.contains("propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesFilteredPrefix() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertySqlDate", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(property_names.size(), 4);
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesFilteredPrefixGetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertySqlDate", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(property_names.size(), 5);
            assertTrue(property_names.contains("PREFIX:propertyReadonly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyNamesFilteredPrefixSetters() {
        try {
            Set<String> property_names = BeanUtils.getPropertyNames(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertySqlDate", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(property_names.size(), 5);
            assertTrue(property_names.contains("PREFIX:propertyWriteonly"));
            assertTrue(property_names.contains("PREFIX:propertyStringBuffer"));
            assertTrue(property_names.contains("PREFIX:propertyCalendar"));
            assertTrue(property_names.contains("PREFIX:propertyChar"));
            assertTrue(property_names.contains("PREFIX:propertyDouble"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesIllegal() {
        try {
            assertEquals(0, BeanUtils.countProperties(null, null, null, null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountProperties() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class, null, null, null);
            assertEquals(count, 16);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(count, 17);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(count, 17);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesPrefix() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(count, 16);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesPrefixGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(count, 17);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesPrefixSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(count, 17);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesIncluded() {
        try {
            assertEquals(7, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesIncludedGetters() {
        try {
            assertEquals(8, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesIncludedSetters() {
        try {
            assertEquals(8, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null,
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesIncludedPrefix() {
        try {
            assertEquals(7, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesIncludedPrefixGetters() {
        try {
            assertEquals(8, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesIncludedPrefixSetters() {
        try {
            assertEquals(8, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                null,
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesExcluded() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(count, 9);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesExcludedGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(count, 10);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesExcludedSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                null);
            assertEquals(count, 10);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesExcludedPrefix() {
        try {
            int count = BeanUtils.countProperties(BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(count, 9);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesExcludedPrefixGetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(count, 10);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesExcludedPrefixSetters() {
        try {
            int count = BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                "PREFIX:");
            assertEquals(count, 10);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesFiltered() {
        try {
            assertEquals(3, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertyStringBuffer", "propertyChar", "propertyByte", "propertyShort"},
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesFilteredGetters() {
        try {
            assertEquals(4, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertyStringBuffer", "propertyChar", "propertyByte", "propertyShort"},
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesFilteredSetters() {
        try {
            assertEquals(4, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyStringBuffer", "propertyCalendar", "propertySqlDate",
                    "propertyChar", "propertyByte", "propertyDouble", "propertyShort"},
                new String[]{"propertyStringBuffer", "propertyChar", "propertyByte", "propertyShort"},
                null));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesFilteredPrefix() {
        try {
            assertEquals(3, BeanUtils.countProperties(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesFilteredPrefixGetters() {
        try {
            assertEquals(4, BeanUtils.countProperties(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCountPropertiesFilteredPrefixSetters() {
        try {
            assertEquals(4, BeanUtils.countProperties(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyStringBuffer", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate",
                    "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyDouble", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyStringBuffer", "PREFIX:propertyChar", "PREFIX:propertyByte", "PREFIX:propertyShort"},
                "PREFIX:"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypeIllegal() {
        try {
            BeanUtils.getPropertyType(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyType(Object.class, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyType(Object.class, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyType() {
        try {
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyString"), String.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyStringBuffer"), StringBuffer.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyDate"), java.util.Date.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyCalendar"), java.util.Calendar.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertySqlDate"), java.sql.Date.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyTime"), java.sql.Time.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyChar"), char.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyBoolean"), boolean.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyByte"), byte.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyDouble"), double.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyFloat"), float.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyInt"), int.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyLong"), long.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyShort"), short.class);
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            assertSame(BeanUtils.getPropertyType(BeanImpl.class, "unknown"), String.class);
            fail();
        } catch (BeanUtilsException e) {
            assertSame(e.getBeanClass(), BeanImpl.class);
        }
    }

    @Test
    public void testPropertyTypesIllegal() {
        try {
            assertEquals(0, BeanUtils.getPropertyTypes(null, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypes() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class, null, null, null);
            assertEquals(property_types.size(), 16);
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyCalendar"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyBoolean"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertTrue(property_types.containsKey("propertyBigDecimal"));
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyBoolean"), boolean.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
            assertSame(property_types.get("propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, null);
            assertEquals(property_types.size(), 17);
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyCalendar"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyBoolean"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertTrue(property_types.containsKey("propertyBigDecimal"));
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyBoolean"), boolean.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
            assertSame(property_types.get("propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, null);
            assertEquals(property_types.size(), 17);
            assertTrue(property_types.containsKey("propertyWriteonly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyCalendar"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyBoolean"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertTrue(property_types.containsKey("propertyBigDecimal"));
            assertSame(property_types.get("propertyWriteonly"), long.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyBoolean"), boolean.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
            assertSame(property_types.get("propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_types.size(), 16);
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertTrue(property_types.containsKey("PREFIX:propertyBigDecimal"));
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyBoolean"), boolean.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
            assertSame(property_types.get("PREFIX:propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_types.size(), 17);
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertTrue(property_types.containsKey("PREFIX:propertyBigDecimal"));
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyBoolean"), boolean.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
            assertSame(property_types.get("PREFIX:propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class, null, null, "PREFIX:");
            assertEquals(property_types.size(), 17);
            assertTrue(property_types.containsKey("PREFIX:propertyWriteonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertTrue(property_types.containsKey("PREFIX:propertyBigDecimal"));
            assertSame(property_types.get("PREFIX:propertyWriteonly"), long.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyCalendar"), java.util.Calendar.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyBoolean"), boolean.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
            assertSame(property_types.get("PREFIX:propertyBigDecimal"), BigDecimal.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesIncluded() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(property_types.size(), 7);
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesIncludedGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(property_types.size(), 8);
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesIncludedSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly",
                    "propertyString", "propertyDate", "propertySqlDate", "propertyTime",
                    "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(property_types.size(), 8);
            assertTrue(property_types.containsKey("propertyWriteonly"));
            assertTrue(property_types.containsKey("propertyString"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertySqlDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyFloat"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyWriteonly"), long.class);
            assertSame(property_types.get("propertyString"), String.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyFloat"), float.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesIncludedPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_types.size(), 7);
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesIncludedPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_types.size(), 8);
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesIncludedPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly",
                    "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate",
                    "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_types.size(), 8);
            assertTrue(property_types.containsKey("PREFIX:propertyWriteonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyString"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyWriteonly"), long.class);
            assertSame(property_types.get("PREFIX:propertyString"), String.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertySqlDate"), java.sql.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyFloat"), float.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesExcluded() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_types.size(), 10);
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesExcludedGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_types.size(), 11);
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesExcludedSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_types.size(), 11);
            assertTrue(property_types.containsKey("propertyWriteonly"));
            assertTrue(property_types.containsKey("propertyStringBuffer"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyTimestamp"));
            assertTrue(property_types.containsKey("propertyChar"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyDouble"));
            assertTrue(property_types.containsKey("propertyInt"));
            assertTrue(property_types.containsKey("propertyLong"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyWriteonly"), long.class);
            assertSame(property_types.get("propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("propertyChar"), char.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyDouble"), double.class);
            assertSame(property_types.get("propertyInt"), int.class);
            assertSame(property_types.get("propertyLong"), long.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesExcludedPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_types.size(), 10);
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesExcludedPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_types.size(), 11);
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesExcludedPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_types.size(), 11);
            assertTrue(property_types.containsKey("PREFIX:propertyWriteonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_types.containsKey("PREFIX:propertyChar"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_types.containsKey("PREFIX:propertyInt"));
            assertTrue(property_types.containsKey("PREFIX:propertyLong"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyWriteonly"), long.class);
            assertSame(property_types.get("PREFIX:propertyStringBuffer"), StringBuffer.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyTimestamp"), java.sql.Timestamp.class);
            assertSame(property_types.get("PREFIX:propertyChar"), char.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyDouble"), double.class);
            assertSame(property_types.get("PREFIX:propertyInt"), int.class);
            assertSame(property_types.get("PREFIX:propertyLong"), long.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesFiltered() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_types.size(), 4);
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesFilteredGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_types.size(), 5);
            assertTrue(property_types.containsKey("propertyReadonly"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyReadonly"), int.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesFilteredSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_types.size(), 5);
            assertTrue(property_types.containsKey("propertyWriteonly"));
            assertTrue(property_types.containsKey("propertyDate"));
            assertTrue(property_types.containsKey("propertyTime"));
            assertTrue(property_types.containsKey("propertyByte"));
            assertTrue(property_types.containsKey("propertyShort"));
            assertSame(property_types.get("propertyWriteonly"), long.class);
            assertSame(property_types.get("propertyDate"), java.util.Date.class);
            assertSame(property_types.get("propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("propertyByte"), byte.class);
            assertSame(property_types.get("propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesFilteredPrefix() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_types.size(), 4);
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesFilteredPrefixGetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.GETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_types.size(), 5);
            assertTrue(property_types.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyReadonly"), int.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPropertyTypesFilteredPrefixSetters() {
        try {
            var property_types = BeanUtils.getPropertyTypes(BeanUtils.Accessors.SETTERS, BeanImpl.class,
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_types.size(), 5);
            assertTrue(property_types.containsKey("PREFIX:propertyWriteonly"));
            assertTrue(property_types.containsKey("PREFIX:propertyDate"));
            assertTrue(property_types.containsKey("PREFIX:propertyTime"));
            assertTrue(property_types.containsKey("PREFIX:propertyByte"));
            assertTrue(property_types.containsKey("PREFIX:propertyShort"));
            assertSame(property_types.get("PREFIX:propertyWriteonly"), long.class);
            assertSame(property_types.get("PREFIX:propertyDate"), java.util.Date.class);
            assertSame(property_types.get("PREFIX:propertyTime"), java.sql.Time.class);
            assertSame(property_types.get("PREFIX:propertyByte"), byte.class);
            assertSame(property_types.get("PREFIX:propertyShort"), short.class);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValueIllegal() {
        try {
            BeanUtils.getPropertyValue(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(Object.class, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(new Object(), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(new Object(), "");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValue() {
        Object bean = getPopulatedBean();
        try {
            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyString"), "thisisastring");
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyDate"), cal.getTime());
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyCalendar"), cal);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyChar"), 'g');
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyBoolean"), Boolean.FALSE);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyByte"), (byte) 53);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyDouble"), 84578.42d);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyFloat"), 35523.967f);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyInt"), 978);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyLong"), 87346L);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyShort"), (short) 31);
            assertEquals(BeanUtils.getPropertyValue(bean, "propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValue(bean, "unknown");
            fail();
        } catch (BeanUtilsException e) {
            assertSame(e.getBeanClass(), bean.getClass());
        }
    }

    @Test
    public void testSetPropertyValue() {
        BeanImpl bean = new BeanImpl();
        try {
            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            BeanUtils.setPropertyValue(bean, "propertyString", "thisisastring");
            BeanUtils.setPropertyValue(bean, "propertyStringBuffer", new StringBuffer("butthisisastringbuffer"));
            BeanUtils.setPropertyValue(bean, "propertyDate", cal.getTime());
            BeanUtils.setPropertyValue(bean, "propertyCalendar", cal);
            BeanUtils.setPropertyValue(bean, "propertySqlDate", new java.sql.Date(cal.getTime().getTime()));
            BeanUtils.setPropertyValue(bean, "propertyTime", new Time(cal.getTime().getTime()));
            BeanUtils.setPropertyValue(bean, "propertyTimestamp", new Timestamp(cal.getTime().getTime()));
            BeanUtils.setPropertyValue(bean, "propertyChar", 'g');
            BeanUtils.setPropertyValue(bean, "propertyBoolean", Boolean.FALSE);
            BeanUtils.setPropertyValue(bean, "propertyByte", (byte) 53);
            BeanUtils.setPropertyValue(bean, "propertyDouble", 84578.42d);
            BeanUtils.setPropertyValue(bean, "propertyFloat", 35523.967f);
            BeanUtils.setPropertyValue(bean, "propertyInt", 978);
            BeanUtils.setPropertyValue(bean, "propertyLong", 87346L);
            BeanUtils.setPropertyValue(bean, "propertyShort", (short) 31);
            BeanUtils.setPropertyValue(bean, "propertyBigDecimal", new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        BeanImpl populated = getPopulatedBean();
        assertEquals(bean.getPropertyString(), populated.getPropertyString());
        assertEquals(bean.getPropertyStringBuffer().toString(), populated.getPropertyStringBuffer().toString());
        assertEquals(bean.getPropertyDate(), populated.getPropertyDate());
        assertEquals(bean.getPropertyCalendar(), populated.getPropertyCalendar());
        assertEquals(bean.getPropertySqlDate(), populated.getPropertySqlDate());
        assertEquals(bean.getPropertyTime(), populated.getPropertyTime());
        assertEquals(bean.getPropertyTimestamp(), populated.getPropertyTimestamp());
        assertEquals(bean.getPropertyChar(), populated.getPropertyChar());
        assertEquals(bean.isPropertyBoolean(), populated.isPropertyBoolean());
        assertEquals(bean.getPropertyByte(), populated.getPropertyByte());
        assertEquals(bean.getPropertyDouble(), populated.getPropertyDouble());
        assertEquals(bean.getPropertyFloat(), populated.getPropertyFloat());
        assertEquals(bean.getPropertyInt(), populated.getPropertyInt());
        assertEquals(bean.getPropertyLong(), populated.getPropertyLong());
        assertEquals(bean.getPropertyShort(), populated.getPropertyShort());
        assertEquals(bean.getPropertyBigDecimal(), populated.getPropertyBigDecimal());

        try {
            BeanUtils.setPropertyValue(bean, "unknown", "ok");
            fail();
        } catch (BeanUtilsException e) {
            assertSame(e.getBeanClass(), bean.getClass());
        }
    }

    @Test
    public void testGetPropertyValuesIllegal() {
        try {
            assertEquals(0, BeanUtils.getPropertyValues(null, null, null, null).size());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            BeanUtils.getPropertyValues(Object.class, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValues() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(), null, null, null);
            assertEquals(property_values.size(), 16);
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyCalendar"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyBoolean"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));
            assertTrue(property_values.containsKey("propertyBigDecimal"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
            assertEquals(property_values.get("propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(), null, null, null);
            assertEquals(property_values.size(), 17);
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyCalendar"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyBoolean"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));
            assertTrue(property_values.containsKey("propertyBigDecimal"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
            assertEquals(property_values.get("propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(), null, null, null);
            assertEquals(property_values.size(), 16);
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyCalendar"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyBoolean"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));
            assertTrue(property_values.containsKey("propertyBigDecimal"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyCalendar"), cal);
            assertEquals(property_values.get("propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
            assertEquals(property_values.get("propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(property_values.size(), 16);
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));
            assertTrue(property_values.containsKey("PREFIX:propertyBigDecimal"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
            assertEquals(property_values.get("PREFIX:propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(property_values.size(), 17);
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));
            assertTrue(property_values.containsKey("PREFIX:propertyBigDecimal"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
            assertEquals(property_values.get("PREFIX:propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(), null, null, "PREFIX:");
            assertEquals(property_values.size(), 16);
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyCalendar"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyBoolean"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));
            assertTrue(property_values.containsKey("PREFIX:propertyBigDecimal"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyCalendar"), cal);
            assertEquals(property_values.get("PREFIX:propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyBoolean"), Boolean.FALSE);
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
            assertEquals(property_values.get("PREFIX:propertyBigDecimal"), new BigDecimal("8347365990.387437894678"));
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesIncluded() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(property_values.size(), 7);
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesIncludedGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(property_values.size(), 8);
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesIncludedSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                null,
                null);
            assertEquals(property_values.size(), 7);
            assertTrue(property_values.containsKey("propertyString"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertySqlDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyFloat"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyString"), "thisisastring");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyFloat"), 35523.967f);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesIncludedPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_values.size(), 7);
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesIncludedPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_values.size(), 8);
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesIncludedPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                null,
                "PREFIX:");
            assertEquals(property_values.size(), 7);
            assertTrue(property_values.containsKey("PREFIX:propertyString"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertySqlDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyFloat"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyString"), "thisisastring");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertySqlDate"), new java.sql.Date(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyFloat"), 35523.967f);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesExcluded() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_values.size(), 10);
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesExcludedGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_values.size(), 11);
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesExcludedSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                null,
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_values.size(), 10);
            assertTrue(property_values.containsKey("propertyStringBuffer"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyTimestamp"));
            assertTrue(property_values.containsKey("propertyChar"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyDouble"));
            assertTrue(property_values.containsKey("propertyInt"));
            assertTrue(property_values.containsKey("propertyLong"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyChar"), 'g');
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyDouble"), 84578.42d);
            assertEquals(property_values.get("propertyInt"), 978);
            assertEquals(property_values.get("propertyLong"), 87346L);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesExcludedPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_values.size(), 10);
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesExcludedPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_values.size(), 11);
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesExcludedPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                null,
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_values.size(), 10);
            assertTrue(property_values.containsKey("PREFIX:propertyStringBuffer"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyTimestamp"));
            assertTrue(property_values.containsKey("PREFIX:propertyChar"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyDouble"));
            assertTrue(property_values.containsKey("PREFIX:propertyInt"));
            assertTrue(property_values.containsKey("PREFIX:propertyLong"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyStringBuffer").toString(), "butthisisastringbuffer");
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyTimestamp"), new Timestamp(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyChar"), 'g');
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyDouble"), 84578.42d);
            assertEquals(property_values.get("PREFIX:propertyInt"), 978);
            assertEquals(property_values.get("PREFIX:propertyLong"), 87346L);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesFiltered() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_values.size(), 4);
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesFilteredGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_values.size(), 5);
            assertTrue(property_values.containsKey("propertyReadonly"));
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyReadonly"), 23);
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesFilteredSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"propertyReadonly", "propertyWriteonly", "propertyString", "propertyDate", "propertySqlDate", "propertyTime", "propertyByte", "propertyFloat", "propertyShort"},
                new String[]{"propertyString", "propertyCalendar", "propertySqlDate", "propertyBoolean", "propertyFloat", "propertyBigDecimal"},
                null);
            assertEquals(property_values.size(), 4);
            assertTrue(property_values.containsKey("propertyDate"));
            assertTrue(property_values.containsKey("propertyTime"));
            assertTrue(property_values.containsKey("propertyByte"));
            assertTrue(property_values.containsKey("propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("propertyDate"), cal.getTime());
            assertEquals(property_values.get("propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("propertyByte"), (byte) 53);
            assertEquals(property_values.get("propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesFilteredPrefix() {
        try {
            var property_values = BeanUtils.getPropertyValues(getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_values.size(), 4);
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesFilteredPrefixGetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.GETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_values.size(), 5);
            assertTrue(property_values.containsKey("PREFIX:propertyReadonly"));
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyReadonly"), 23);
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGetPropertyValuesFilteredPrefixSetters() {
        try {
            var property_values = BeanUtils.getPropertyValues(BeanUtils.Accessors.SETTERS, getPopulatedBean(),
                new String[]{"PREFIX:propertyReadonly", "PREFIX:propertyWriteonly", "PREFIX:propertyString", "PREFIX:propertyDate", "PREFIX:propertySqlDate", "PREFIX:propertyTime", "PREFIX:propertyByte", "PREFIX:propertyFloat", "PREFIX:propertyShort"},
                new String[]{"PREFIX:propertyString", "PREFIX:propertyCalendar", "PREFIX:propertySqlDate", "PREFIX:propertyBoolean", "PREFIX:propertyFloat", "PREFIX:propertyBigDecimal"},
                "PREFIX:");
            assertEquals(property_values.size(), 4);
            assertTrue(property_values.containsKey("PREFIX:propertyDate"));
            assertTrue(property_values.containsKey("PREFIX:propertyTime"));
            assertTrue(property_values.containsKey("PREFIX:propertyByte"));
            assertTrue(property_values.containsKey("PREFIX:propertyShort"));

            var cal = Calendar.getInstance();
            cal.set(2002, Calendar.DECEMBER, 26, 22, 52, 31);
            cal.set(Calendar.MILLISECOND, 153);
            assertEquals(property_values.get("PREFIX:propertyDate"), cal.getTime());
            assertEquals(property_values.get("PREFIX:propertyTime"), new Time(cal.getTime().getTime()));
            assertEquals(property_values.get("PREFIX:propertyByte"), (byte) 53);
            assertEquals(property_values.get("PREFIX:propertyShort"), (short) 31);
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
