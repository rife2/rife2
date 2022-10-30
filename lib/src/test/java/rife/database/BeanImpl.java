/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class BeanImpl {
    private String mPropertyString = null;
    private StringBuffer mPropertyStringbuffer = null;
    private java.util.Date mPropertyDate = null;
    private java.util.Calendar mPropertyCalendar = null;
    private java.sql.Date mPropertySqlDate = null;
    private java.sql.Time mPropertyTime = null;
    private java.sql.Timestamp mPropertyTimestamp = null;
    private char mPropertyChar = 0;
    private Character mPropertyCharacterObject = null;
    private boolean mPropertyBoolean = false;
    private Boolean mPropertyBooleanObject = null;
    private byte mPropertyByte = 0;
    private Byte mPropertyByteObject = null;
    private double mPropertyDouble = 0.0d;
    private Double mPropertyDoubleObject = null;
    private float mPropertyFloat = 0.0f;
    private Float mPropertyFloatObject = null;
    private int mPropertyInt = 0;
    private Integer mPropertyIntegerObject = null;
    private long mPropertyLong = 0;
    private Long mPropertyLongObject = null;
    private short mPropertyShort = 0;
    private Short mPropertyShortObject = null;
    private BigDecimal mPropertyBigDecimal = null;
    private SomeEnum mPropertyEnum = null;

    public BeanImpl() {
    }

    public String getPropertyString() {
        return mPropertyString;
    }

    public void setPropertyString(String propertyString) {
        mPropertyString = propertyString;
    }

    public StringBuffer getPropertyStringbuffer() {
        return mPropertyStringbuffer;
    }

    public void setPropertyStringbuffer(StringBuffer propertyStringbuffer) {
        mPropertyStringbuffer = propertyStringbuffer;
    }

    public java.util.Date getPropertyDate() {
        return mPropertyDate;
    }

    public void setPropertyDate(java.util.Date propertyDate) {
        mPropertyDate = propertyDate;
    }

    public java.util.Calendar getPropertyCalendar() {
        return mPropertyCalendar;
    }

    public void setPropertyCalendar(java.util.Calendar propertyCalendar) {
        mPropertyCalendar = propertyCalendar;
    }

    public java.sql.Date getPropertySqlDate() {
        return mPropertySqlDate;
    }

    public void setPropertySqlDate(java.sql.Date propertySqlDate) {
        mPropertySqlDate = propertySqlDate;
    }

    public java.sql.Time getPropertyTime() {
        return mPropertyTime;
    }

    public void setPropertyTime(java.sql.Time propertyTime) {
        mPropertyTime = propertyTime;
    }

    public java.sql.Timestamp getPropertyTimestamp() {
        return mPropertyTimestamp;
    }

    public void setPropertyTimestamp(java.sql.Timestamp propertyTimestamp) {
        mPropertyTimestamp = propertyTimestamp;
    }

    public boolean isPropertyBoolean() {
        return mPropertyBoolean;
    }

    public void setPropertyBoolean(boolean propertyBoolean) {
        mPropertyBoolean = propertyBoolean;
    }

    public Boolean getPropertyBooleanObject() {
        return mPropertyBooleanObject;
    }

    public void setPropertyBooleanObject(Boolean propertyBooleanObject) {
        mPropertyBooleanObject = propertyBooleanObject;
    }

    public byte getPropertyByte() {
        return mPropertyByte;
    }

    public Byte getPropertyByteObject() {
        return mPropertyByteObject;
    }

    public void setPropertyByte(byte propertyByte) {
        mPropertyByte = propertyByte;
    }

    public void setPropertyByteObject(Byte propertyByteObject) {
        mPropertyByteObject = propertyByteObject;
    }

    public double getPropertyDouble() {
        return mPropertyDouble;
    }

    public void setPropertyDouble(double propertyDouble) {
        mPropertyDouble = propertyDouble;
    }

    public void setPropertyDoubleObject(Double propertyDoubleObject) {
        mPropertyDoubleObject = propertyDoubleObject;
    }

    public Double getPropertyDoubleObject() {
        return mPropertyDoubleObject;
    }

    public float getPropertyFloat() {
        return mPropertyFloat;
    }

    public void setPropertyFloat(float propertyFloat) {
        mPropertyFloat = propertyFloat;
    }

    public void setPropertyFloatObject(Float propertyFloatObject) {
        mPropertyFloatObject = propertyFloatObject;
    }

    public Float getPropertyFloatObject() {
        return mPropertyFloatObject;
    }

    public int getPropertyInt() {
        return mPropertyInt;
    }

    public void setPropertyInt(int propertyInt) {
        mPropertyInt = propertyInt;
    }

    public Integer getPropertyIntegerObject() {
        return mPropertyIntegerObject;
    }

    public void setPropertyIntegerObject(Integer propertyIntegerObject) {
        mPropertyIntegerObject = propertyIntegerObject;
    }

    public long getPropertyLong() {
        return mPropertyLong;
    }

    public void setPropertyLong(long propertyLong) {
        mPropertyLong = propertyLong;
    }

    public Long getPropertyLongObject() {
        return mPropertyLongObject;
    }

    public void setPropertyLongObject(Long propertyLongObject) {
        mPropertyLongObject = propertyLongObject;
    }

    public short getPropertyShort() {
        return mPropertyShort;
    }

    public void setPropertyShort(short propertyShort) {
        mPropertyShort = propertyShort;
    }

    public Short getPropertyShortObject() {
        return mPropertyShortObject;
    }

    public void setPropertyShortObject(Short propertyShortObject) {
        mPropertyShortObject = propertyShortObject;
    }

    public char getPropertyChar() {
        return mPropertyChar;
    }

    public void setPropertyChar(char propertyChar) {
        mPropertyChar = propertyChar;
    }

    public Character getPropertyCharacterObject() {
        return mPropertyCharacterObject;
    }

    public void setPropertyCharacterObject(Character propertyCharacterObject) {
        mPropertyCharacterObject = propertyCharacterObject;
    }

    public BigDecimal getPropertyBigDecimal() {
        return mPropertyBigDecimal;
    }

    public void setPropertyBigDecimal(BigDecimal propertyBigDecimal) {
        mPropertyBigDecimal = propertyBigDecimal;
    }

    public void setPropertyEnum(SomeEnum propertyEnum) {
        mPropertyEnum = propertyEnum;
    }

    public SomeEnum getPropertyEnum() {
        return mPropertyEnum;
    }

    public static BeanImpl getPopulatedBean() {
        BeanImpl bean = new BeanImpl();
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 5, 18, 15, 26, 14);
        cal.set(Calendar.MILLISECOND, 764);
        bean.setPropertyBigDecimal(new BigDecimal("219038743.392874"));
        bean.setPropertyBoolean(true);
        bean.setPropertyBooleanObject(false);
        bean.setPropertyByte((byte) 89);
        bean.setPropertyByteObject((byte) 34);
        bean.setPropertyCalendar(cal);
        bean.setPropertyChar('v');
        bean.setPropertyCharacterObject('r');
        bean.setPropertyDate(cal.getTime());
        bean.setPropertyDouble(53348.34d);
        bean.setPropertyDoubleObject(143298.692d);
        bean.setPropertyFloat(98634.2f);
        bean.setPropertyFloatObject(8734.7f);
        bean.setPropertyInt(545);
        bean.setPropertyIntegerObject(968);
        bean.setPropertyLong(34563L);
        bean.setPropertyLongObject(66875L);
        bean.setPropertyShort((short) 43);
        bean.setPropertyShortObject((short) 68);
        bean.setPropertySqlDate(new java.sql.Date(cal.getTime().getTime()));
        bean.setPropertyString("someotherstring");
        bean.setPropertyStringbuffer(new StringBuffer("someotherstringbuff"));
        bean.setPropertyTime(new Time(cal.getTime().getTime()));
        bean.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));
        bean.setPropertyEnum(SomeEnum.VALUE_THREE);

        return bean;
    }

    public static BeanImpl getNullBean() {
        BeanImpl bean = new BeanImpl();
        bean.setPropertyBigDecimal(null);
        bean.setPropertyBoolean(false);
        bean.setPropertyBooleanObject(false);
        bean.setPropertyByte((byte) 0);
        bean.setPropertyByteObject((byte) 0);
        bean.setPropertyDate(null);
        bean.setPropertyCalendar(null);
        bean.setPropertyChar((char) 0);
        bean.setPropertyCharacterObject(null);
        bean.setPropertyDouble(0d);
        bean.setPropertyDoubleObject(0d);
        bean.setPropertyFloat(0f);
        bean.setPropertyFloatObject(0f);
        bean.setPropertyInt(0);
        bean.setPropertyIntegerObject(0);
        bean.setPropertyLong(0L);
        bean.setPropertyLongObject(0L);
        bean.setPropertyShort((short) 0);
        bean.setPropertyShortObject((short) 0);
        bean.setPropertySqlDate(null);
        bean.setPropertyString(null);
        bean.setPropertyStringbuffer(null);
        bean.setPropertyTime(null);
        bean.setPropertyTimestamp(null);
        bean.setPropertyEnum(null);

        return bean;
    }
}

