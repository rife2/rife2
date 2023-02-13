/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow.config;

import rife.continuations.ContinuationConfigInstrument;

/**
 * Byte-code instrumentation configuration that is needed for continuations to
 * work for the workflow engine. This class should not be used directly.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContinuationInstrument implements ContinuationConfigInstrument {
    public String getContinuableMarkerInterfaceName() {
        return "rife.continuations.CloneableContinuable";
    }

    public String getEntryMethodName() {
        return "execute";
    }

    public String getEntryMethodDescriptor() {
        return "(Lrife/workflow/Workflow;)V";
    }

    public String getCallMethodName() {
        return "pauseForEvent";
    }

    public String getCallMethodDescriptor() {
        return "(Ljava/lang/Object;)Lrife/workflow/Event;";
    }

    public String getCallMethodReturnTypeName() {
        return "rife/workflow/Event";
    }
}