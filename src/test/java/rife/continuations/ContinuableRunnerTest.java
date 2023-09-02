/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import rife.continuations.basic.BasicContinuableClassLoader;
import rife.continuations.basic.BasicContinuableRunner;

public class ContinuableRunnerTest extends BasicContinuableRunner {
    private static final ContinuationConfigInstrument CONFIG_INSTRUMENT = new ContinuationConfigInstrumentTests();
    private static final ContinuableSupport CONTINUABLE_SUPPORT_DUMMY = new ContinuableSupport();

    public ContinuableRunnerTest()
    throws ClassNotFoundException {
        super(CONFIG_INSTRUMENT, null, new BasicContinuableClassLoader(CONFIG_INSTRUMENT));
        Class.forName(ContinuableSupport.class.getName());
    }

    @Override
    public void beforeExecuteEntryMethodHook(Object object) {
        if (object instanceof ContinuableSupportAware) {
            ((ContinuableSupportAware) object).setContinuableSupport(CONTINUABLE_SUPPORT_DUMMY);
        }
    }
}
