/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.continuations.ContinuationConfigInstrument;

public class EngineContinuationConfigInstrument implements ContinuationConfigInstrument {
    public String getContinuableMarkerInterfaceName() {
        return Element.class.getName();
    }

    public String getContinuableSupportClassName() {
        return Context.class.getName();
    }

    public String getEntryMethodName() {
        return "process";
    }

    public Class getEntryMethodReturnType() {
        return void.class;
    }

    public Class[] getEntryMethodArgumentTypes() {
        return new Class[]{Context.class};
    }

    public String getPauseMethodName() {
        return "pause";
    }

    public String getStepBackMethodName() {
        return "stepBack";
    }

    public String getCallMethodName() {
        return "call";
    }

    public Class getCallMethodReturnType() {
        return Object.class;
    }

    public Class[] getCallMethodArgumentTypes() {
        return new Class[]{String.class};
    }

    public String getAnswerMethodName() {
        return "answer";
    }
}
