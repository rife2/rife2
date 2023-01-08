/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.continuations.*;

class EngineContinuationConfigRuntime extends ContinuationConfigRuntime {
    private final Site site_;

    EngineContinuationConfigRuntime(Site site) {
        site_ = site;
    }

    public ContinuationManager getContinuationManager(Object executingContinuable) {
        return site_.continuationManager_;
    }

    public boolean cloneContinuations(Object executingContinuable) {
        return false;
    }

    public long getContinuationDuration() {
        return RifeConfig.engine().getContinuationDuration();
    }

    public int getContinuationPurgeFrequency() {
        return RifeConfig.engine().getContinuationPurgeFrequency();
    }

    public int getContinuationPurgeScale() {
        return RifeConfig.engine().getContinuationPurgeScale();
    }
}
