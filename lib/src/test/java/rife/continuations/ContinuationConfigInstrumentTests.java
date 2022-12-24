/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class ContinuationConfigInstrumentTests implements ContinuationConfigInstrument {
    public String getContinuableMarkerInterfaceName() {
        return ContinuableObject.class.getName();
    }

    public String getContinuableSupportClassName() {
        return ContinuableSupport.class.getName();
    }

    public String getEntryMethodName() {
        return "execute";
    }

    public Class getEntryMethodReturnType() {
        return void.class;
    }

    public Class[] getEntryMethodArgumentTypes() {
        return null;
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
        return new Class[]{Class.class};
    }

    public String getAnswerMethodName() {
        return "answer";
    }
}
