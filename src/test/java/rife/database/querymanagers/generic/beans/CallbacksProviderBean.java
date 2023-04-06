/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

import java.util.ArrayList;
import java.util.List;

import rife.database.querymanagers.generic.Callbacks;
import rife.database.querymanagers.generic.CallbacksProvider;

public class CallbacksProviderBean implements CallbacksProvider<CallbacksProviderBean> {
    private int id_ = -1;
    private String testString_ = null;

    static TheCallbacks callbacks_ = new TheCallbacks();

    public CallbacksProviderBean() {
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public Callbacks<CallbacksProviderBean> getCallbacks() {
        return callbacks_;
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
        return callbacks_.getExecutedCallbacks();
    }

    public static void clearExecuteCallbacks() {
        callbacks_.clearExecuteCallbacks();
    }

    public static class TheCallbacks implements Callbacks<CallbacksProviderBean> {
        private List<String> mExecutedCallbacks = new ArrayList<String>();

        public List<String> getExecutedCallbacks() {
            return mExecutedCallbacks;
        }

        public void clearExecuteCallbacks() {
            mExecutedCallbacks = new ArrayList<String>();
        }

        public boolean beforeValidate(CallbacksProviderBean object) {
            mExecutedCallbacks.add("beforeValidate " + object.toString());
            return true;
        }

        public boolean beforeInsert(CallbacksProviderBean object) {
            mExecutedCallbacks.add("beforeInsert " + object.toString());
            return true;
        }

        public boolean beforeDelete(int objectId) {
            mExecutedCallbacks.add("beforeDelete " + objectId);
            return true;
        }

        public boolean beforeSave(CallbacksProviderBean object) {
            mExecutedCallbacks.add("beforeSave " + object.toString());
            return true;
        }

        public boolean beforeUpdate(CallbacksProviderBean object) {
            mExecutedCallbacks.add("beforeUpdate " + object.toString());
            return true;
        }

        public boolean afterValidate(CallbacksProviderBean object) {
            mExecutedCallbacks.add("afterValidate " + object.toString());
            return true;
        }

        public boolean afterInsert(CallbacksProviderBean object, boolean success) {
            mExecutedCallbacks.add("afterInsert " + success + " " + object.toString());
            return true;
        }

        public boolean afterDelete(int objectId, boolean success) {
            mExecutedCallbacks.add("afterDelete " + success + " " + objectId);
            return true;
        }

        public boolean afterSave(CallbacksProviderBean object, boolean success) {
            mExecutedCallbacks.add("afterSave " + success + " " + object.toString());
            return true;
        }

        public boolean afterUpdate(CallbacksProviderBean object, boolean success) {
            mExecutedCallbacks.add("afterUpdate " + success + " " + object.toString());
            return true;
        }

        public boolean afterRestore(CallbacksProviderBean object) {
            mExecutedCallbacks.add("afterRestore " + object.toString());
            return true;
        }
    }
}

