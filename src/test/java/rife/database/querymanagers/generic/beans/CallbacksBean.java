/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.database.querymanagers.generic.Callbacks;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import java.util.ArrayList;
import java.util.List;

public class CallbacksBean extends Validation implements Callbacks<CallbacksBean> {
    private int id_ = -1;
    private String testString_ = null;

    private static List<String> sExecutedCallbacks = new ArrayList<>();

    private boolean beforeValidateReturn_ = true;
    private boolean beforeInsertReturn_ = true;
    private static boolean sBeforeDeleteReturn = true;
    private boolean beforeSaveReturn_ = true;
    private boolean beforeUpdateReturn_ = true;
    private boolean afterValidateReturn_ = true;
    private boolean afterInsertReturn_ = true;
    private static boolean sAfterDeleteReturn = true;
    private boolean afterSaveReturn_ = true;
    private boolean afterUpdateReturn_ = true;
    private static boolean sAfterRestoreReturn = true;

    public CallbacksBean() {
    }

    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("id").identifier(true).notNull(true));
        addConstraint(new ConstrainedProperty("testString").notNull(true));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setTestString(String testString) {
        this.testString_ = testString;
    }

    public String getTestString() {
        return testString_;
    }

    public String toString() {
        return id_ + ";" + testString_;
    }

    public static List<String> getExecutedCallbacks() {
        return sExecutedCallbacks;
    }

    public static void clearExecuteCallbacks() {
        sExecutedCallbacks = new ArrayList<String>();
    }

    public void setBeforeValidateReturn(boolean beforeValidateReturn) {
        beforeValidateReturn_ = beforeValidateReturn;
    }

    public void setBeforeInsertReturn(boolean beforeInsertReturn) {
        beforeInsertReturn_ = beforeInsertReturn;
    }

    public static void setBeforeDeleteReturn(boolean beforeDeleteReturn) {
        sBeforeDeleteReturn = beforeDeleteReturn;
    }

    public void setBeforeSaveReturn(boolean beforeSaveReturn) {
        beforeSaveReturn_ = beforeSaveReturn;
    }

    public void setBeforeUpdateReturn(boolean beforeUpdateReturn) {
        beforeUpdateReturn_ = beforeUpdateReturn;
    }

    public void setAfterValidateReturn(boolean afterValidateReturn) {
        afterValidateReturn_ = afterValidateReturn;
    }

    public void setAfterInsertReturn(boolean afterInsertReturn) {
        afterInsertReturn_ = afterInsertReturn;
    }

    public static void setAfterDeleteReturn(boolean afterDeleteReturn) {
        sAfterDeleteReturn = afterDeleteReturn;
    }

    public void setAfterSaveReturn(boolean afterSaveReturn) {
        afterSaveReturn_ = afterSaveReturn;
    }

    public void setAfterUpdateReturn(boolean afterUpdateReturn) {
        afterUpdateReturn_ = afterUpdateReturn;
    }

    public static void setAfterRestoreReturn(boolean afterRestoreReturn) {
        sAfterRestoreReturn = afterRestoreReturn;
    }

    public boolean beforeValidate(CallbacksBean object) {
        sExecutedCallbacks.add("beforeValidate " + object.toString());
        return beforeValidateReturn_;
    }

    public boolean beforeInsert(CallbacksBean object) {
        sExecutedCallbacks.add("beforeInsert " + object.toString());
        return beforeInsertReturn_;
    }

    public boolean beforeDelete(int objectId) {
        sExecutedCallbacks.add("beforeDelete " + objectId);
        return sBeforeDeleteReturn;
    }

    public boolean beforeSave(CallbacksBean object) {
        sExecutedCallbacks.add("beforeSave " + object.toString());
        return beforeSaveReturn_;
    }

    public boolean beforeUpdate(CallbacksBean object) {
        sExecutedCallbacks.add("beforeUpdate " + object.toString());
        return beforeUpdateReturn_;
    }

    public boolean afterValidate(CallbacksBean object) {
        sExecutedCallbacks.add("afterValidate " + object.toString());
        return afterValidateReturn_;
    }

    public boolean afterInsert(CallbacksBean object, boolean success) {
        sExecutedCallbacks.add("afterInsert " + success + " " + object.toString());
        return afterInsertReturn_;
    }

    public boolean afterDelete(int objectId, boolean success) {
        sExecutedCallbacks.add("afterDelete " + success + " " + objectId);
        return sAfterDeleteReturn;
    }

    public boolean afterSave(CallbacksBean object, boolean success) {
        sExecutedCallbacks.add("afterSave " + success + " " + object.toString());
        return afterSaveReturn_;
    }

    public boolean afterUpdate(CallbacksBean object, boolean success) {
        sExecutedCallbacks.add("afterUpdate " + success + " " + object.toString());
        return afterUpdateReturn_;
    }

    public boolean afterRestore(CallbacksBean object) {
        sExecutedCallbacks.add("afterRestore " + object.toString());
        return sAfterRestoreReturn;
    }
}

