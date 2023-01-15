/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class BeanErrorImpl {
    public BeanErrorImpl() {
    }

    public int getPropertyInt()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyInt(int propertyInt) {
    }

    public String getPropertyString()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyString(String propertyString) {
    }

    public double getPropertyDouble()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyDouble(double propertyDouble) {
    }

    public StringBuffer getPropertyStringBuffer()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyStringBuffer(StringBuffer propertyStringBuffer) {
    }

    public java.util.Date getPropertyDate()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyDate(java.util.Date propertyDate) {
    }

    public java.util.Calendar getPropertyCalendar()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyCalendar(java.util.Calendar propertyCalendar) {
    }

    public java.sql.Date getPropertySqlDate()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertySqlDate(java.sql.Date propertySqlDate) {
    }

    public java.sql.Time getPropertyTime()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyTime(java.sql.Time propertyTime) {
    }

    public java.sql.Timestamp getPropertyTimestamp()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyTimestamp(java.sql.Timestamp propertyTimestamp) {
    }

    public boolean isPropertyBoolean()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyBoolean(boolean propertyBoolean) {
    }

    public byte getPropertyByte()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyByte(byte propertyByte) {
    }

    public float getPropertyFloat()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyFloat(float propertyFloat) {
    }

    public long getPropertyLong()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyLong(long propertyLong) {
    }

    public short getPropertyShort()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyShort(short propertyShort) {
    }

    public char getPropertyChar()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyChar(char propertyChar) {
    }

    public BigDecimal getPropertyBigDecimal()
    throws Exception {
        throw new Exception("an error");
    }

    public void setPropertyBigDecimal(BigDecimal propertyBigDecimal) {
    }

    public static BeanErrorImpl getPopulatedBean() {
        BeanErrorImpl bean = new BeanErrorImpl();
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 5, 18, 15, 26, 14);
        cal.set(Calendar.MILLISECOND, 167);
        bean.setPropertyString("someotherstring");
        bean.setPropertyStringBuffer(new StringBuffer("someotherstringbuff"));
        bean.setPropertyDate(cal.getTime());
        bean.setPropertyCalendar(cal);
        bean.setPropertySqlDate(new java.sql.Date(cal.getTime().getTime()));
        bean.setPropertyTime(new Time(cal.getTime().getTime()));
        bean.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));
        bean.setPropertyChar('v');
        bean.setPropertyBoolean(true);
        bean.setPropertyByte((byte) 89);
        bean.setPropertyDouble(53348.34d);
        bean.setPropertyFloat(98634.2f);
        bean.setPropertyInt(545);
        bean.setPropertyLong(34563L);
        bean.setPropertyShort((short) 43);
        bean.setPropertyBigDecimal(new BigDecimal("219038743.392874"));

        return bean;
    }

    public static BeanErrorImpl getNullBean() {
        BeanErrorImpl bean = new BeanErrorImpl();
        bean.setPropertyString(null);
        bean.setPropertyStringBuffer(null);
        bean.setPropertyDate(null);
        bean.setPropertyCalendar(null);
        bean.setPropertySqlDate(null);
        bean.setPropertyTime(null);
        bean.setPropertyTimestamp(null);
        bean.setPropertyChar((char) 0);
        bean.setPropertyBoolean(false);
        bean.setPropertyByte((byte) 0);
        bean.setPropertyDouble(0d);
        bean.setPropertyFloat(0f);
        bean.setPropertyInt(0);
        bean.setPropertyLong(0L);
        bean.setPropertyShort((short) 0);
        bean.setPropertyBigDecimal(null);

        return bean;
    }
}
