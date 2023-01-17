/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.querymanagers.generic.beans.CallbacksBean;

import java.util.LinkedHashMap;
import java.util.Map;

public class AggregatingCallbacksBeanListener implements GenericQueryManagerListener<CallbacksBean> {
    private Map<String, Object> mHistory = new LinkedHashMap<String, Object>();

    public void clearHistory() {
        mHistory.clear();
    }

    public Map<String, Object> getHistory() {
        return mHistory;
    }

    public void installed() {
        mHistory.put("installed" + mHistory.size(), null);
    }

    public void removed() {
        mHistory.put("removed" + mHistory.size(), null);
    }

    public void inserted(CallbacksBean bean) {
        mHistory.put("inserted" + mHistory.size(), bean);
        bean.setTestString("listener inserted");
    }

    public void updated(CallbacksBean bean) {
        mHistory.put("updated" + mHistory.size(), bean);
        bean.setTestString("listener updated");
    }

    public void restored(CallbacksBean bean) {
        mHistory.put("restored" + mHistory.size(), bean);
        bean.setTestString("listener restored");
    }

    public void deleted(int objectId) {
        mHistory.put("deleted" + mHistory.size(), objectId);
    }
}

