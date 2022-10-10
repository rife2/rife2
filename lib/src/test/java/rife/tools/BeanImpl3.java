/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;
import rife.validation.ConstrainedBean;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BeanImpl3 extends MetaData<ConstrainedBean, ConstrainedProperty> {
    private Date propertyDate_ = null;
    private byte propertyByte_ = 0;
    private double propertyDouble_ = 0.0d;
    private float propertyFloat_ = 0.0f;
    private int propertyInt_ = 0;
    private long propertyLong_ = 0;
    private short propertyShort_ = 0;
    private Byte propertyByteObject_ = null;
    private Double propertyDoubleObject_ = null;
    private Float propertyFloatObject_ = null;
    private Integer propertyIntegerObject_ = null;
    private Long propertyLongObject_ = null;
    private Short propertyShortObject_ = null;
    private BigDecimal propertyBigDecimal_ = null;
    private Date[] propertyDateArray_ = null;
    private byte[] propertyByteArray_ = null;
    private double[] propertyDoubleArray_ = null;
    private float[] propertyFloatArray_ = null;
    private int[] propertyIntArray_ = null;
    private long[] propertyLongArray_ = null;
    private short[] propertyShortArray_ = null;
    private Byte[] propertyByteObjectArray_ = null;
    private Double[] propertyDoubleObjectArray_ = null;
    private Float[] propertyFloatObjectArray_ = null;
    private Integer[] propertyIntegerObjectArray_ = null;
    private Long[] propertyLongObjectArray_ = null;
    private Short[] propertyShortObjectArray_ = null;
    private BigDecimal[] propertyBigDecimalArray_ = null;

    public void activateMetaData() {
        DateFormat date_format = new SimpleDateFormat("'custom format' yyyy-MM-dd HH:mm");
        date_format.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
        NumberFormat int_format = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat double_format = NumberFormat.getNumberInstance(Locale.US);
        NumberFormat byte_format = NumberFormat.getPercentInstance(Locale.US);
        NumberFormat float_format = NumberFormat.getNumberInstance(Locale.FRANCE);
        NumberFormat long_format = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        NumberFormat short_format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        DecimalFormat bigdecimal_format = (DecimalFormat) NumberFormat.getNumberInstance(Locale.FRANCE);
        bigdecimal_format.setParseBigDecimal(true);

        addConstraint(new ConstrainedProperty("propertyDate").format(date_format));
        addConstraint(new ConstrainedProperty("propertyInt").format(int_format));
        addConstraint(new ConstrainedProperty("propertyIntegerObject").format(int_format));
        addConstraint(new ConstrainedProperty("propertyDouble").format(double_format));
        addConstraint(new ConstrainedProperty("propertyDoubleObject").format(double_format));
        addConstraint(new ConstrainedProperty("propertyByte").format(byte_format));
        addConstraint(new ConstrainedProperty("propertyByteObject").format(byte_format));
        addConstraint(new ConstrainedProperty("propertyFloat").format(float_format));
        addConstraint(new ConstrainedProperty("propertyFloatObject").format(float_format));
        addConstraint(new ConstrainedProperty("propertyLong").format(long_format));
        addConstraint(new ConstrainedProperty("propertyLongObject").format(long_format));
        addConstraint(new ConstrainedProperty("propertyShort").format(short_format));
        addConstraint(new ConstrainedProperty("propertyShortObject").format(short_format));
        addConstraint(new ConstrainedProperty("propertyBigDecimal").format(bigdecimal_format));

        addConstraint(new ConstrainedProperty("propertyDateArray").format(date_format));
        addConstraint(new ConstrainedProperty("propertyIntArray").format(int_format));
        addConstraint(new ConstrainedProperty("propertyIntegerObjectArray").format(int_format));
        addConstraint(new ConstrainedProperty("propertyDoubleArray").format(double_format));
        addConstraint(new ConstrainedProperty("propertyDoubleObjectArray").format(double_format));
        addConstraint(new ConstrainedProperty("propertyByteArray").format(byte_format));
        addConstraint(new ConstrainedProperty("propertyByteObjectArray").format(byte_format));
        addConstraint(new ConstrainedProperty("propertyFloatArray").format(float_format));
        addConstraint(new ConstrainedProperty("propertyFloatObjectArray").format(float_format));
        addConstraint(new ConstrainedProperty("propertyLongArray").format(long_format));
        addConstraint(new ConstrainedProperty("propertyLongObjectArray").format(long_format));
        addConstraint(new ConstrainedProperty("propertyShortArray").format(short_format));
        addConstraint(new ConstrainedProperty("propertyShortObjectArray").format(short_format));
        addConstraint(new ConstrainedProperty("propertyBigDecimalArray").format(bigdecimal_format));
    }

    public BeanImpl3() {
    }

    public int getPropertyInt() {
        return propertyInt_;
    }

    public void setPropertyInt(int propertyInt) {
        propertyInt_ = propertyInt;
    }

    public double getPropertyDouble() {
        return propertyDouble_;
    }

    public void setPropertyDouble(double propertyDouble) {
        propertyDouble_ = propertyDouble;
    }

    public Date getPropertyDate() {
        return propertyDate_;
    }

    public void setPropertyDate(Date propertyDate) {
        propertyDate_ = propertyDate;
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

    public Short getPropertyShortObject() {
        return propertyShortObject_;
    }

    public void setPropertyShortObject(Short propertyShortObject) {
        propertyShortObject_ = propertyShortObject;
    }

    public void setPropertyBigDecimal(BigDecimal propertyBigDecimal) {
        propertyBigDecimal_ = propertyBigDecimal;
    }

    public BigDecimal getPropertyBigDecimal() {
        return propertyBigDecimal_;
    }

    public Byte getPropertyByteObject() {
        return propertyByteObject_;
    }

    public void setPropertyByteObject(Byte propertyByteObject) {
        propertyByteObject_ = propertyByteObject;
    }

    public Double getPropertyDoubleObject() {
        return propertyDoubleObject_;
    }

    public void setPropertyDoubleObject(Double propertyDoubleObject) {
        propertyDoubleObject_ = propertyDoubleObject;
    }

    public Float getPropertyFloatObject() {
        return propertyFloatObject_;
    }

    public void setPropertyFloatObject(Float propertyFloatObject) {
        propertyFloatObject_ = propertyFloatObject;
    }

    public Integer getPropertyIntegerObject() {
        return propertyIntegerObject_;
    }

    public void setPropertyIntegerObject(Integer propertyIntegerObject) {
        propertyIntegerObject_ = propertyIntegerObject;
    }

    public Long getPropertyLongObject() {
        return propertyLongObject_;
    }

    public void setPropertyLongObject(Long propertyLongObject) {
        propertyLongObject_ = propertyLongObject;
    }

    public Date[] getPropertyDateArray() {
        return propertyDateArray_;
    }

    public void setPropertyDateArray(Date[] propertyDateArray) {
        propertyDateArray_ = propertyDateArray;
    }

    public byte[] getPropertyByteArray() {
        return propertyByteArray_;
    }

    public void setPropertyByteArray(byte[] propertyByteArray) {
        propertyByteArray_ = propertyByteArray;
    }

    public double[] getPropertyDoubleArray() {
        return propertyDoubleArray_;
    }

    public void setPropertyDoubleArray(double[] propertyDoubleArray) {
        propertyDoubleArray_ = propertyDoubleArray;
    }

    public float[] getPropertyFloatArray() {
        return propertyFloatArray_;
    }

    public void setPropertyFloatArray(float[] propertyFloatArray) {
        propertyFloatArray_ = propertyFloatArray;
    }

    public int[] getPropertyIntArray() {
        return propertyIntArray_;
    }

    public void setPropertyIntArray(int[] propertyIntArray) {
        propertyIntArray_ = propertyIntArray;
    }

    public long[] getPropertyLongArray() {
        return propertyLongArray_;
    }

    public void setPropertyLongArray(long[] propertyLongArray) {
        propertyLongArray_ = propertyLongArray;
    }

    public short[] getPropertyShortArray() {
        return propertyShortArray_;
    }

    public void setPropertyShortArray(short[] propertyShortArray) {
        propertyShortArray_ = propertyShortArray;
    }

    public void setPropertyBigDecimalArray(BigDecimal[] propertyBigDecimalArray) {
        propertyBigDecimalArray_ = propertyBigDecimalArray;
    }

    public BigDecimal[] getPropertyBigDecimalArray() {
        return propertyBigDecimalArray_;
    }

    public Byte[] getPropertyByteObjectArray() {
        return propertyByteObjectArray_;
    }

    public void setPropertyByteObjectArray(Byte[] propertyByteObjectArray) {
        propertyByteObjectArray_ = propertyByteObjectArray;
    }

    public Double[] getPropertyDoubleObjectArray() {
        return propertyDoubleObjectArray_;
    }

    public void setPropertyDoubleObjectArray(Double[] propertyDoubleObjectArray) {
        propertyDoubleObjectArray_ = propertyDoubleObjectArray;
    }

    public Float[] getPropertyFloatObjectArray() {
        return propertyFloatObjectArray_;
    }

    public void setPropertyFloatObjectArray(Float[] propertyFloatObjectArray) {
        propertyFloatObjectArray_ = propertyFloatObjectArray;
    }

    public Integer[] getPropertyIntegerObjectArray() {
        return propertyIntegerObjectArray_;
    }

    public void setPropertyIntegerObjectArray(Integer[] propertyIntegerObjectArray) {
        propertyIntegerObjectArray_ = propertyIntegerObjectArray;
    }

    public Long[] getPropertyLongObjectArray() {
        return propertyLongObjectArray_;
    }

    public void setPropertyLongObjectArray(Long[] propertyLongObjectArray) {
        propertyLongObjectArray_ = propertyLongObjectArray;
    }

    public Short[] getPropertyShortObjectArray() {
        return propertyShortObjectArray_;
    }

    public void setPropertyShortObjectArray(Short[] propertyShortObjectArray) {
        propertyShortObjectArray_ = propertyShortObjectArray;
    }
}
