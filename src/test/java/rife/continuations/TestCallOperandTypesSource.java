/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallOperandTypesSource extends AbstractContinuableObject {
    private String result_;

    public void execute() {
        Object candidate = "candidate";
        result_ = combine(
            (String) null,
            100_000,
            10_000_000_000L,
            100_000.5F,
            100_000.25D,
            candidate instanceof String,
            "constant",
            String.class,
            call(TestCallOperandTypesTarget.class));
    }

    private static String combine(String nullValue, int intValue, long longValue, float floatValue,
                                  double doubleValue, boolean instanceOf, String stringValue,
                                  Class<?> classValue, Object answer) {
        return nullValue + ":" + intValue + ":" + longValue + ":" + floatValue + ":" +
               doubleValue + ":" + instanceOf + ":" + stringValue + ":" +
               classValue.getName() + ":" + answer;
    }

    public String getResult() {
        return result_;
    }
}
