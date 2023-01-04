/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers;

import rife.authentication.RememberManager;
import rife.authentication.exceptions.RememberManagerException;
import rife.config.RifeConfig;

import java.util.Random;

public class PurgingRememberManager implements RememberManager {
    private int rememberPurgeFrequency_ = RifeConfig.authentication().getRememberPurgeFrequency();
    private int rememberPurgeScale_ = RifeConfig.authentication().getRememberPurgeScale();

    private final Random mRandom = new Random();

    private RememberManager mRememberManager = null;

    public PurgingRememberManager(RememberManager rememberManager) {
        if (null == rememberManager) throw new IllegalArgumentException("rememberManager can't be null");

        mRememberManager = rememberManager;
    }

    public RememberManager getRememberManager() {
        return mRememberManager;
    }

    public int getRememberPurgeFrequency() {
        return rememberPurgeFrequency_;
    }

    public void setRememberPurgeFrequency(int frequency) {
        rememberPurgeFrequency_ = frequency;
    }

    public int getRememberPurgeScale() {
        return rememberPurgeScale_;
    }

    public void setRememberPurgeScale(int scale) {
        rememberPurgeScale_ = scale;
    }

    public long getRememberDuration() {
        return mRememberManager.getRememberDuration();
    }

    public void setRememberDuration(long milliseconds) {
        mRememberManager.setRememberDuration(milliseconds);
    }

    public String createRememberId(long userId, String authData)
    throws RememberManagerException {
        int purge_decision = -1;
        synchronized (mRandom) {
            purge_decision = mRandom.nextInt(rememberPurgeScale_);
        }
        if (purge_decision <= rememberPurgeFrequency_) {
            purgeRememberIds();
        }

        return mRememberManager.createRememberId(userId, authData);
    }

    public boolean eraseRememberId(String rememberId)
    throws RememberManagerException {
        return mRememberManager.eraseRememberId(rememberId);
    }

    public boolean eraseUserRememberIds(long userId)
    throws RememberManagerException {
        return mRememberManager.eraseUserRememberIds(userId);
    }

    public void eraseAllRememberIds()
    throws RememberManagerException {
        mRememberManager.eraseAllRememberIds();
    }

    public long getRememberedUserId(String rememberId)
    throws RememberManagerException {
        return mRememberManager.getRememberedUserId(rememberId);
    }

    public void purgeRememberIds()
    throws RememberManagerException {
        mRememberManager.purgeRememberIds();
    }
}

