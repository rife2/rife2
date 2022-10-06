/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.math.BigDecimal;

public class BeanImpl {
    private int propertyReadonly_ = 23;
    private String propertyString_ = null;
    private StringBuffer propertyStringBuffer_ = null;
    private java.util.Date propertyDate_ = null;
    private java.util.Calendar propertyCalendar_ = null;
    private java.sql.Date propertySqlDate_ = null;
    private java.sql.Time propertyTime_ = null;
    private java.sql.Timestamp propertyTimestamp_ = null;
    private char propertyChar_ = 0;
    private boolean propertyBoolean_ = false;
    private byte propertyByte_ = 0;
    private double propertyDouble_ = 0.0d;
    private float propertyFloat_ = 0.0f;
    private int propertyInt_ = 0;
    private long propertyLong_ = 0;
    private short propertyShort_ = 0;
    private BigDecimal propertyBigDecimal_ = null;

    public BeanImpl() {
    }

    public int getPropertyReadonly() {
        return propertyReadonly_;
    }

    public void setPropertyWriteonly(long propertyWriteonly) {
    }

    public int getPropertyInt() {
        return propertyInt_;
    }

    public void setPropertyInt(int propertyInt) {
        propertyInt_ = propertyInt;
    }

    public String getPropertyString() {
        return propertyString_;
    }

    public void setPropertyString(String propertyString) {
        propertyString_ = propertyString;
    }

    public double getPropertyDouble() {
        return propertyDouble_;
    }

    public void setPropertyDouble(double propertyDouble) {
        propertyDouble_ = propertyDouble;
    }

    public StringBuffer getPropertyStringBuffer() {
        return propertyStringBuffer_;
    }

    public void setPropertyStringBuffer(StringBuffer propertyStringbuffer) {
        propertyStringBuffer_ = propertyStringbuffer;
    }

    public java.util.Date getPropertyDate() {
        return propertyDate_;
    }

    public void setPropertyDate(java.util.Date propertyDate) {
        propertyDate_ = propertyDate;
    }

    public java.util.Calendar getPropertyCalendar() {
        return propertyCalendar_;
    }

    public void setPropertyCalendar(java.util.Calendar propertyCalendar) {
        propertyCalendar_ = propertyCalendar;
    }

    public java.sql.Date getPropertySqlDate() {
        return propertySqlDate_;
    }

    public void setPropertySqlDate(java.sql.Date propertySqlDate) {
        propertySqlDate_ = propertySqlDate;
    }

    public java.sql.Time getPropertyTime() {
        return propertyTime_;
    }

    public void setPropertyTime(java.sql.Time propertyTime) {
        propertyTime_ = propertyTime;
    }

    public java.sql.Timestamp getPropertyTimestamp() {
        return propertyTimestamp_;
    }

    public void setPropertyTimestamp(java.sql.Timestamp propertyTimestamp) {
        propertyTimestamp_ = propertyTimestamp;
    }

    public boolean isPropertyBoolean() {
        return propertyBoolean_;
    }

    public void setPropertyBoolean(boolean propertyBoolean) {
        propertyBoolean_ = propertyBoolean;
    }

    public byte getPropertyByte() {
        return propertyByte_;
    }

    public void setPropertyByte(byte propertyByte) {
        propertyByte_ = propertyByte;
    }

    public float getPropertyFloat() {
        return propertyFloat_;
    }

    public void setPropertyFloat(float propertyFloat) {
        propertyFloat_ = propertyFloat;
    }

    public long getPropertyLong() {
        return propertyLong_;
    }

    public void setPropertyLong(long propertyLong) {
        propertyLong_ = propertyLong;
    }

    public short getPropertyShort() {
        return propertyShort_;
    }

    public void setPropertyShort(short propertyShort) {
        propertyShort_ = propertyShort;
    }

    public char getPropertyChar() {
        return propertyChar_;
    }

    public void setPropertyChar(char propertyChar) {
        propertyChar_ = propertyChar;
    }

    public BigDecimal getPropertyBigDecimal() {
        return propertyBigDecimal_;
    }

    public void setPropertyBigDecimal(BigDecimal propertyBigDecimal) {
        propertyBigDecimal_ = propertyBigDecimal;
    }
}
