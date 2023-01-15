/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.database.SomeEnum;
import rife.tools.Convert;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;

public class BeanImpl extends Validation {
    private int id_ = -1;

    private String propertyString_ = null;
    private StringBuffer propertyStringBuffer_ = null;
    private java.util.Date propertyDate_ = null;
    private java.util.Calendar propertyCalendar_ = null;
    private java.sql.Date propertySqlDate_ = null;
    private java.sql.Time propertyTime_ = null;
    private java.sql.Timestamp propertyTimestamp_ = null;
    private Instant propertyInstant_ = null;
    private LocalDateTime propertyLocalDateTime_ = null;
    private LocalDate propertyLocalDate_ = null;
    private LocalTime propertyLocalTime_ = null;
    private char propertyChar_ = 0;
    private Character propertyCharacterObject_ = null;
    private boolean propertyBoolean_ = false;
    private Boolean propertyBooleanObject_ = null;
    private byte propertyByte_ = 0;
    private Byte propertyByteObject_ = null;
    private double propertyDouble_ = 0.0d;
    private Double propertyDoubleObject_ = null;
    private float propertyFloat_ = 0.0f;
    private Float propertyFloatObject_ = null;
    private int propertyInt_ = 0;
    private Integer propertyIntegerObject_ = null;
    private long propertyLong_ = 0;
    private Long propertyLongObject_ = null;
    private short propertyShort_ = 0;
    private Short propertyShortObject_ = null;
    private BigDecimal propertyBigDecimal_ = null;

    public BeanImpl() {
    }

    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("propertyBigDecimal").precision(18).scale(5));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public String getPropertyString() {
        return propertyString_;
    }

    public void setPropertyString(String propertyString) {
        this.propertyString_ = propertyString;
    }

    public StringBuffer getPropertyStringBuffer() {
        return propertyStringBuffer_;
    }

    public void setPropertyStringBuffer(StringBuffer propertyStringBuffer) {
        propertyStringBuffer_ = propertyStringBuffer;
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

    public Instant getPropertyInstant() {
        return propertyInstant_;
    }

    public void setPropertyInstant(Instant propertyInstant_) {
        this.propertyInstant_ = propertyInstant_;
    }

    public LocalDateTime getPropertyLocalDateTime() {
        return propertyLocalDateTime_;
    }

    public void setPropertyLocalDateTime(LocalDateTime propertyLocalDateTime) {
        this.propertyLocalDateTime_ = propertyLocalDateTime;
    }

    public LocalDate getPropertyLocalDate() {
        return propertyLocalDate_;
    }

    public void setPropertyLocalDate(LocalDate propertyLocalDate) {
        this.propertyLocalDate_ = propertyLocalDate;
    }

    public LocalTime getPropertyLocalTime() {
        return propertyLocalTime_;
    }

    public void setPropertyLocalTime(LocalTime propertyLocalTime) {
        this.propertyLocalTime_ = propertyLocalTime;
    }

    public boolean isPropertyBoolean() {
        return propertyBoolean_;
    }

    public void setPropertyBoolean(boolean propertyBoolean) {
        propertyBoolean_ = propertyBoolean;
    }

    public Boolean getPropertyBooleanObject() {
        return propertyBooleanObject_;
    }

    public void setPropertyBooleanObject(Boolean propertyBooleanObject) {
        propertyBooleanObject_ = propertyBooleanObject;
    }

    public byte getPropertyByte() {
        return propertyByte_;
    }

    public Byte getPropertyByteObject() {
        return propertyByteObject_;
    }

    public void setPropertyByte(byte propertyByte) {
        propertyByte_ = propertyByte;
    }

    public void setPropertyByteObject(Byte propertyByteObject) {
        propertyByteObject_ = propertyByteObject;
    }

    public double getPropertyDouble() {
        return propertyDouble_;
    }

    public void setPropertyDouble(double propertyDouble) {
        propertyDouble_ = propertyDouble;
    }

    public void setPropertyDoubleObject(Double propertyDoubleObject) {
        propertyDoubleObject_ = propertyDoubleObject;
    }

    public Double getPropertyDoubleObject() {
        return propertyDoubleObject_;
    }

    public float getPropertyFloat() {
        return propertyFloat_;
    }

    public void setPropertyFloat(float propertyFloat) {
        propertyFloat_ = propertyFloat;
    }

    public void setPropertyFloatObject(Float propertyFloatObject) {
        propertyFloatObject_ = propertyFloatObject;
    }

    public Float getPropertyFloatObject() {
        return propertyFloatObject_;
    }

    public int getPropertyInt() {
        return propertyInt_;
    }

    public void setPropertyInt(int propertyInt) {
        propertyInt_ = propertyInt;
    }

    public Integer getPropertyIntegerObject() {
        return propertyIntegerObject_;
    }

    public void setPropertyIntegerObject(Integer propertyIntegerObject) {
        propertyIntegerObject_ = propertyIntegerObject;
    }

    public long getPropertyLong() {
        return propertyLong_;
    }

    public void setPropertyLong(long propertyLong) {
        propertyLong_ = propertyLong;
    }

    public Long getPropertyLongObject() {
        return propertyLongObject_;
    }

    public void setPropertyLongObject(Long propertyLongObject) {
        propertyLongObject_ = propertyLongObject;
    }

    public short getPropertyShort() {
        return propertyShort_;
    }

    public void setPropertyShort(short propertyShort) {
        propertyShort_ = propertyShort;
    }

    public Short getPropertyShortObject() {
        return propertyShortObject_;
    }

    public void setPropertyShortObject(Short propertyShortObject) {
        propertyShortObject_ = propertyShortObject;
    }

    public char getPropertyChar() {
        return propertyChar_;
    }

    public void setPropertyChar(char propertyChar) {
        propertyChar_ = propertyChar;
    }

    public Character getPropertyCharacterObject() {
        return propertyCharacterObject_;
    }

    public void setPropertyCharacterObject(Character propertyCharacterObject) {
        propertyCharacterObject_ = propertyCharacterObject;
    }

    public BigDecimal getPropertyBigDecimal() {
        return propertyBigDecimal_;
    }

    public void setPropertyBigDecimal(BigDecimal propertyBigDecimal) {
        propertyBigDecimal_ = propertyBigDecimal;
    }

    public static BeanImpl getPopulatedBean() {
        var bean = new BeanImpl();
        var cal = Calendar.getInstance();
        cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
        cal.set(Calendar.MILLISECOND, 167);
        var local_date_time = LocalDateTime.of(2002, Month.JUNE, 18, 15, 26, 14, 167000000);
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
        bean.setPropertyStringBuffer(new StringBuffer("someotherstringbuff"));
        bean.setPropertyTime(Convert.toTime(cal));
        bean.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));
        bean.setPropertyInstant(cal.toInstant());
        bean.setPropertyLocalDateTime(local_date_time);
        bean.setPropertyLocalDate(local_date_time.toLocalDate());
        bean.setPropertyLocalTime(local_date_time.toLocalTime());

        return bean;
    }

    public static BeanImpl getNullBean() {
        var bean = new BeanImpl();
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
        bean.setPropertyStringBuffer(null);
        bean.setPropertyTime(null);
        bean.setPropertyTimestamp(null);
        bean.setPropertyInstant(null);
        bean.setPropertyLocalDateTime(null);
        bean.setPropertyLocalDate(null);
        bean.setPropertyLocalTime(null);

        return bean;
    }
}