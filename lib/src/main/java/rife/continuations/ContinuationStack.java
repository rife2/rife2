/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import rife.continuations.instrument.ContinuationDebug;
import rife.tools.ObjectUtils;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * [PRIVATE AND UNSUPPORTED] Contains the local state of a continuation.
 * <p>This needs to be publicly accessible for the instrumented code to be
 * able to interact with it, but it's not supposed to be used directly.
 *
 * @since 1.0
 */
public class ContinuationStack {
    static final int NONE = 0;
    static final int INTEGER = 1;
    static final int LONG = 2;
    static final int FLOAT = 3;
    static final int DOUBLE = 4;
    static final int REFERENCE = 5;

    private int[] positionMapping_ = null;
    private int[] typeMapping_ = null;
    private int stackHeight_ = 0;

    private int[] intStack_ = null;
    private long[] longStack_ = null;
    private float[] floatStack_ = null;
    private double[] doubleStack_ = null;
    private Object[] referenceStack_ = null;

    private int intTop_ = 0;
    private int longTop_ = 0;
    private int doubleTop_ = 0;
    private int floatTop_ = 0;
    private int referenceTop_ = 0;

    ContinuationStack() {
    }

    ContinuationStack initialize() {
        intStack_ = new int[10];
        longStack_ = new long[5];
        floatStack_ = new float[5];
        doubleStack_ = new double[5];
        referenceStack_ = new Object[5];

        positionMapping_ = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        typeMapping_ = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

        return this;
    }

    public synchronized int getType(int index) {
        if (index <= typeMapping_.length - 1) {
            return typeMapping_[index];
        }
        return NONE;
    }

    public synchronized int popInt() {
        return getInt(--stackHeight_);
    }

    public synchronized long popLong() {
        return getLong(--stackHeight_);
    }

    public synchronized float popFloat() {
        return getFloat(--stackHeight_);
    }

    public synchronized double popDouble() {
        return getDouble(--stackHeight_);
    }

    public synchronized Object popReference() {
        return getReference(--stackHeight_);
    }

    public synchronized int getInt(int index) {
        var position = positionMapping_[index];
        if (-1 == position ||
            position >= intStack_.length) {
            return 0;
        }
        return intStack_[position];
    }

    public synchronized long getLong(int index) {
        var position = positionMapping_[index];
        if (-1 == position ||
            position >= longStack_.length) {
            return 0L;
        }
        return longStack_[position];
    }

    public synchronized float getFloat(int index) {
        var position = positionMapping_[index];
        if (-1 == position ||
            position >= floatStack_.length) {
            return 0f;
        }
        return floatStack_[position];
    }

    public synchronized double getDouble(int index) {
        var position = positionMapping_[index];
        if (-1 == position ||
            position >= doubleStack_.length) {
            return 0d;
        }
        return doubleStack_[position];
    }

    public synchronized Object getReference(int index) {
        var position = positionMapping_[index];
        if (-1 == position ||
            position >= referenceStack_.length) {
            return null;
        }
        return referenceStack_[position];
    }

    public synchronized int getReferenceStackSize() {
        return referenceStack_.length;
    }

    private synchronized void storeIndex(int index, int position, int type) {
        if (index > positionMapping_.length - 1) {
            var size = (((index + 1) / 10) + 1) * 10;
            var new_position_mapping = new int[size];
            var new_type_mapping = new int[size];
            Arrays.fill(new_position_mapping, positionMapping_.length, new_position_mapping.length, -1);
            Arrays.fill(new_type_mapping, typeMapping_.length, new_type_mapping.length, -1);
            System.arraycopy(positionMapping_, 0, new_position_mapping, 0, positionMapping_.length);
            System.arraycopy(typeMapping_, 0, new_type_mapping, 0, typeMapping_.length);
            positionMapping_ = new_position_mapping;
            typeMapping_ = new_type_mapping;
        }
        positionMapping_[index] = position;
        typeMapping_[index] = type;
    }

    public synchronized void incrementInt(int index, int increment) {
        var position = -1;

        position = positionMapping_[index];
        intStack_[position] += increment;
    }

    public synchronized void pushInt(int value) {
        storeInt(stackHeight_++, value);
    }

    public synchronized void pushLong(long value) {
        storeLong(stackHeight_++, value);
    }

    public synchronized void pushFloat(float value) {
        storeFloat(stackHeight_++, value);
    }

    public synchronized void pushDouble(double value) {
        storeDouble(stackHeight_++, value);
    }

    public synchronized void pushReference(Object value) {
        storeReference(stackHeight_++, value);
    }

    public synchronized void storeInt(int index, int value) {
        var position = -1;

        if (getType(index) != INTEGER) {
            position = intTop_++;

            storeIndex(index, position, INTEGER);

            if (position > intStack_.length - 1) {
                var size = (((position + 1) / 10) + 1) * 10;
                var new_stack = new int[size];
                System.arraycopy(intStack_, 0, new_stack, 0, intStack_.length);
                intStack_ = new_stack;
            }
        } else {
            position = positionMapping_[index];
        }

        intStack_[position] = value;
    }

    public synchronized void storeLong(int index, long value) {
        var position = -1;

        if (getType(index) != LONG) {
            position = longTop_++;

            storeIndex(index, position, LONG);

            if (position > longStack_.length - 1) {
                var size = (((position + 1) / 10) + 1) * 10;
                var new_stack = new long[size];
                System.arraycopy(longStack_, 0, new_stack, 0, longStack_.length);
                longStack_ = new_stack;
            }
        } else {
            position = positionMapping_[index];
        }

        longStack_[position] = value;
    }

    public synchronized void storeFloat(int index, float value) {
        var position = -1;

        if (getType(index) != FLOAT) {
            position = floatTop_++;

            storeIndex(index, position, FLOAT);

            if (position > floatStack_.length - 1) {
                var size = (((position + 1) / 10) + 1) * 10;
                var new_stack = new float[size];
                System.arraycopy(floatStack_, 0, new_stack, 0, floatStack_.length);
                floatStack_ = new_stack;
            }
        } else {
            position = positionMapping_[index];
        }

        floatStack_[position] = value;
    }

    public synchronized void storeDouble(int index, double value) {
        var position = -1;

        if (getType(index) != DOUBLE) {
            position = doubleTop_++;

            storeIndex(index, position, DOUBLE);

            if (position > doubleStack_.length - 1) {
                var size = (((position + 1) / 10) + 1) * 10;
                var new_stack = new double[size];
                System.arraycopy(doubleStack_, 0, new_stack, 0, doubleStack_.length);
                doubleStack_ = new_stack;
            }
        } else {
            position = positionMapping_[index];
        }

        doubleStack_[position] = value;
    }

    public synchronized void storeReference(int index, Object value) {
        var position = -1;

        if (getType(index) != REFERENCE) {
            position = referenceTop_++;

            storeIndex(index, position, REFERENCE);

            if (position > referenceStack_.length - 1) {
                var size = (((position + 1) / 10) + 1) * 10;
                var new_stack = new Object[size];
                System.arraycopy(referenceStack_, 0, new_stack, 0, referenceStack_.length);
                referenceStack_ = new_stack;
            }
        } else {
            position = positionMapping_[index];
        }

        referenceStack_[position] = value;
    }

    public synchronized void outputState() {
        ContinuationDebug.LOGGER.finest("");
        ContinuationDebug.LOGGER.finest("STACK : " + this);
        ContinuationDebug.LOGGER.finest("mPositionMapping[" + positionMapping_.length + "] = " + join(positionMapping_, ","));
        ContinuationDebug.LOGGER.finest("mTypeMapping[" + typeMapping_.length + "]     = " + join(typeMapping_, ","));
        ContinuationDebug.LOGGER.finest("mIntStack[" + intStack_.length + "]        = " + join(intStack_, ","));
        ContinuationDebug.LOGGER.finest("mLongStack[" + longStack_.length + "]        = " + join(longStack_, ","));
        ContinuationDebug.LOGGER.finest("mFloatStack[" + floatStack_.length + "]       = " + join(floatStack_, ","));
        ContinuationDebug.LOGGER.finest("mDoubleStack[" + doubleStack_.length + "]      = " + join(doubleStack_, ","));
        ContinuationDebug.LOGGER.finest("mReferenceStack[" + referenceStack_.length + "]   = " + join(referenceStack_, ","));
    }

    // adding a join method here to remove a viral dependency on the StringUtils class
    private static String join(Object array, String separator) {
        if (null == array) {
            return "";
        }

        if (!array.getClass().isArray()) {
            return String.valueOf(array);
        }

        var result = new StringBuilder();
        for (var i = 0; i < Array.getLength(array); i++) {
            if (result.length() > 0) {
                result.append(separator);
            }

            result.append(Array.get(array, i));
        }

        return result.toString();
    }

    public synchronized ContinuationStack clone(Object continuableInstance)
    throws CloneNotSupportedException {
        var new_stack = new ContinuationStack();

        new_stack.positionMapping_ = positionMapping_.clone();
        new_stack.typeMapping_ = typeMapping_.clone();
        new_stack.stackHeight_ = stackHeight_;

        new_stack.intStack_ = intStack_.clone();
        new_stack.longStack_ = longStack_.clone();
        new_stack.floatStack_ = floatStack_.clone();
        new_stack.doubleStack_ = doubleStack_.clone();
        new_stack.referenceStack_ = new Object[referenceStack_.length];
        for (var i = 0; i < referenceStack_.length; i++) {
            if (referenceStack_[i] != null &&
                referenceStack_[i].getClass() == continuableInstance.getClass()) {
                new_stack.referenceStack_[i] = continuableInstance;
            } else {
                new_stack.referenceStack_[i] = ObjectUtils.deepClone(referenceStack_[i]);
            }
        }

        new_stack.intTop_ = intTop_;
        new_stack.longTop_ = longTop_;
        new_stack.doubleTop_ = doubleTop_;
        new_stack.floatTop_ = floatTop_;
        new_stack.referenceTop_ = referenceTop_;

        return new_stack;
    }
}
