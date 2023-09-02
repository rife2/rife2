/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class ContinuationConfigInstrumentTests implements ContinuationConfigInstrument {
    @Override
    public String getContinuableMarkerInterfaceName() {
        return "rife.continuations.CloneableContinuable";
    }

    @Override
    public String getContinuableSupportClassName() {
        return "rife.continuations.ContinuableSupport";
    }

    @Override
    public String getEntryMethodName() {
        return "execute";
    }

    @Override
    public String getEntryMethodDescriptor() {
        return "()V";
    }

    @Override
    public String getPauseMethodName() {
        return "pause";
    }

    @Override
    public String getStepBackMethodName() {
        return "stepBack";
    }

    @Override
    public String getCallMethodName() {
        return "call";
    }

    @Override
    public String getCallMethodReturnTypeName() {
        return "java/lang/Object";
    }

    @Override
    public String getCallMethodDescriptor() {
        return "(Ljava/lang/Class;)Ljava/lang/Object;";
    }

    @Override
    public String getAnswerMethodName() {
        return "answer";
    }
}
