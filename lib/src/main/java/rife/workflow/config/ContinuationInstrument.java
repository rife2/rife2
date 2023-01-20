/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow.config;

import rife.continuations.CloneableContinuable;
import rife.continuations.ContinuationConfigInstrument;
import rife.workflow.Event;
import rife.workflow.run.TaskRunner;

/**
 * Byte-code instrumentation configuration that is needed for continuations to
 * work for the workflow engine. This class should not be used directly.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContinuationInstrument implements ContinuationConfigInstrument {
    public String getContinuableMarkerInterfaceName() {
        return CloneableContinuable.class.getName();
    }

    public String getEntryMethodName() {
        return "execute";
    }

    public Class getEntryMethodReturnType() {
        return void.class;
    }

    public Class[] getEntryMethodArgumentTypes() {
        return new Class[]{TaskRunner.class};
    }

    public String getCallMethodName() {
        return "waitForEvent";
    }

    public Class getCallMethodReturnType() {
        return Event.class;
    }

    public Class[] getCallMethodArgumentTypes() {
        return new Class[]{Object.class};
    }
}