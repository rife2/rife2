/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class ContinuationConfigInstrumentTests implements ContinuationConfigInstrument {
    public String getContinuableMarkerInterfaceName() {
        return "rife.continuations.CloneableContinuable";
    }

    public String getContinuableSupportClassName() {
        return "rife.continuations.ContinuableSupport";
    }

    public String getEntryMethodName() {
        return "execute";
    }

    public String getEntryMethodDescriptor() {
        return "()V";
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

    public String getCallMethodReturnTypeName() {
        return "java/lang/Object";
    }

    public String getCallMethodDescriptor() {
        return "(Ljava/lang/Class;)Ljava/lang/Object;";
    }

    public String getAnswerMethodName() {
        return "answer";
    }
}
