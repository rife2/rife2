/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestValidationError {
    @Test
    public void testMandatory() {
        ValidationError error = new ValidationError.MANDATORY("subject1");
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
        assertEquals("subject1", error.getSubject());
    }

    @Test
    public void testUnicity() {
        ValidationError error = new ValidationError.UNIQUENESS("subject3");
        assertEquals(ValidationError.IDENTIFIER_UNIQUENESS, error.getIdentifier());
        assertEquals("subject3", error.getSubject());
    }

    @Test
    public void testWrongLength() {
        ValidationError error = new ValidationError.WRONG_LENGTH("subject4");
        assertEquals(ValidationError.IDENTIFIER_WRONG_LENGTH, error.getIdentifier());
        assertEquals("subject4", error.getSubject());
    }

    @Test
    public void testWrongFormat() {
        ValidationError error = new ValidationError.WRONG_FORMAT("subject5");
        assertEquals(ValidationError.IDENTIFIER_WRONG_FORMAT, error.getIdentifier());
        assertEquals("subject5", error.getSubject());
    }

    @Test
    public void testNotNumeric() {
        ValidationError error = new ValidationError.NOT_NUMERIC("subject6");
        assertEquals(ValidationError.IDENTIFIER_NOT_NUMERIC, error.getIdentifier());
        assertEquals("subject6", error.getSubject());
    }

    @Test
    public void testUnexpected() {
        ValidationError error = new ValidationError.UNEXPECTED("subject7");
        assertEquals(ValidationError.IDENTIFIER_UNEXPECTED, error.getIdentifier());
        assertEquals("subject7", error.getSubject());
    }

    @Test
    public void testIncomplete() {
        ValidationError error = new ValidationError.INCOMPLETE("subject8");
        assertEquals(ValidationError.IDENTIFIER_INCOMPLETE, error.getIdentifier());
        assertEquals("subject8", error.getSubject());
    }

    @Test
    public void testInvalid() {
        ValidationError error = new ValidationError.INVALID("subject9");
        assertEquals(ValidationError.IDENTIFIER_INVALID, error.getIdentifier());
        assertEquals("subject9", error.getSubject());
    }

    @Test
    public void testCustom() {
        ValidationError error = new CustomError();
        assertEquals("CUSTOM", error.getIdentifier());
        assertEquals("customsubject", error.getSubject());
    }

    class CustomError extends ValidationError {
        CustomError() {
            super("CUSTOM", "customsubject");
        }
    }
}
