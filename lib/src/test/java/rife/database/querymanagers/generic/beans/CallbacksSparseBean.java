/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import rife.database.querymanagers.generic.Callbacks;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import java.util.ArrayList;
import java.util.List;

public class CallbacksSparseBean extends Validation implements Callbacks<CallbacksSparseBean> {
    private int id_ = -1;
    private String testString_ = null;

    private List<String> executedCallbacks_ = new ArrayList<>();

    private boolean beforeValidateReturn_ = true;
    private boolean beforeInsertReturn_ = true;
    private boolean beforeDeleteReturn_ = true;
    private boolean beforeSaveReturn_ = true;
    private boolean beforeUpdateReturn_ = true;
    private boolean afterValidateReturn_ = true;
    private boolean afterInsertReturn_ = true;
    private boolean afterDeleteReturn_ = true;
    private boolean afterSaveReturn_ = true;
    private boolean afterUpdateReturn_ = true;
    private boolean afterRestoreReturn_ = true;

    public void activateValidation() {
        addConstraint(new ConstrainedProperty("id").identifier(true).sparse(true));
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

    public List<String> getExecutedCallbacks() {
        return executedCallbacks_;
    }

    public void clearExecuteCallbacks() {
        executedCallbacks_ = new ArrayList<String>();
    }

    public void setBeforeValidateReturn(boolean beforeValidateReturn) {
        beforeValidateReturn_ = beforeValidateReturn;
    }

    public void setBeforeInsertReturn(boolean beforeInsertReturn) {
        beforeInsertReturn_ = beforeInsertReturn;
    }

    public void setBeforeDeleteReturn(boolean beforeDeleteReturn) {
        beforeDeleteReturn_ = beforeDeleteReturn;
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

    public void setAfterDeleteReturn(boolean afterDeleteReturn) {
        afterDeleteReturn_ = afterDeleteReturn;
    }

    public void setAfterSaveReturn(boolean afterSaveReturn) {
        afterSaveReturn_ = afterSaveReturn;
    }

    public void setAfterUpdateReturn(boolean afterUpdateReturn) {
        afterUpdateReturn_ = afterUpdateReturn;
    }

    public void setAfterRestoreReturn(boolean afterRestoreReturn) {
        afterRestoreReturn_ = afterRestoreReturn;
    }

    public boolean beforeValidate(CallbacksSparseBean object) {
        executedCallbacks_.add("beforeValidate " + object.toString());
        return beforeValidateReturn_;
    }

    public boolean beforeInsert(CallbacksSparseBean object) {
        executedCallbacks_.add("beforeInsert " + object.toString());
        return beforeInsertReturn_;
    }

    public boolean beforeDelete(int objectId) {
        executedCallbacks_.add("beforeDelete " + objectId);
        return beforeDeleteReturn_;
    }

    public boolean beforeSave(CallbacksSparseBean object) {
        executedCallbacks_.add("beforeSave " + object.toString());
        return beforeSaveReturn_;
    }

    public boolean beforeUpdate(CallbacksSparseBean object) {
        executedCallbacks_.add("beforeUpdate " + object.toString());
        return beforeUpdateReturn_;
    }

    public boolean afterValidate(CallbacksSparseBean object) {
        executedCallbacks_.add("afterValidate " + object.toString());
        return afterValidateReturn_;
    }

    public boolean afterInsert(CallbacksSparseBean object, boolean success) {
        executedCallbacks_.add("afterInsert " + success + " " + object.toString());
        return afterInsertReturn_;
    }

    public boolean afterDelete(int objectId, boolean success) {
        executedCallbacks_.add("afterDelete " + success + " " + objectId);
        return afterDeleteReturn_;
    }

    public boolean afterSave(CallbacksSparseBean object, boolean success) {
        executedCallbacks_.add("afterSave " + success + " " + object.toString());
        return afterSaveReturn_;
    }

    public boolean afterUpdate(CallbacksSparseBean object, boolean success) {
        executedCallbacks_.add("afterUpdate " + success + " " + object.toString());
        return afterUpdateReturn_;
    }

    public boolean afterRestore(CallbacksSparseBean object) {
        executedCallbacks_.add("afterRestore " + object.toString());
        return afterRestoreReturn_;
    }
}

