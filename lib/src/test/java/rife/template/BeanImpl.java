/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.config.RifeConfig;

import java.util.Calendar;
import java.util.Date;

public class BeanImpl {
    private int propertyInt_ = 34876;
    private String propertyString_ = "oifuigygti";
    private StringBuffer propertyStringBuffer_ = new StringBuffer("osduhbfezgb");
    private long propertyLong_ = 982787834L;
    private char propertyChar_ = 'O';
    private short propertyShort_ = 423;
    private byte propertyByte_ = (byte) 92;
    private Date propertyDate_;

    public BeanImpl() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
        cal.set(2005, 7, 18, 10, 36, 31);
        cal.set(Calendar.MILLISECOND, 874);
        propertyDate_ = cal.getTime();
    }

    public void setPropertyInt(int propertyInt) {
        this.propertyInt_ = propertyInt;
    }

    public int getPropertyInt() {
        return propertyInt_;
    }

    public void setPropertyString(String propertyString) {
        this.propertyString_ = propertyString;
    }

    public String getPropertyString() {
        return propertyString_;
    }

    public void setPropertyStringBuffer(StringBuffer propertyStringBuffer) {
        this.propertyStringBuffer_ = propertyStringBuffer;
    }

    public StringBuffer getPropertyStringBuffer() {
        return propertyStringBuffer_;
    }

    public void setPropertyLong(long propertyLong) {
        this.propertyLong_ = propertyLong;
    }

    public long getPropertyLong() {
        return propertyLong_;
    }

    public void setPropertyChar(char propertyChar) {
        this.propertyChar_ = propertyChar;
    }

    public char getPropertyChar() {
        return propertyChar_;
    }

    public void setPropertyShort(short propertyShort) {
        this.propertyShort_ = propertyShort;
    }

    public short getPropertyShort() {
        return propertyShort_;
    }

    public void setPropertyByte(byte propertyByte) {
        this.propertyByte_ = propertyByte;
    }

    public byte getPropertyByte() {
        return propertyByte_;
    }

    public void setPropertyDate(Date propertyDate) {
        propertyDate_ = propertyDate;
    }

    public Date getPropertyDate() {
        return propertyDate_;
    }
}

