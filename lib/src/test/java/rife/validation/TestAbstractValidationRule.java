/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestAbstractValidationRule {
    private String value_ = null;

    @Test
    void testInstantiation() {
        MyRule rule = new MyRule();
        assertNotNull(rule);
    }

    @Test
    void testGetSubject() {
        MyRule rule = new MyRule();
        String subject = rule.getSubject();
        assertEquals("the value", subject);
        assertSame(subject, rule.getSubject());
    }

    @Test
    void testValid() {
        MyRule rule = new MyRule();
        value_ = "ok";
        assertTrue(rule.validate());
    }

    @Test
    void testInvalid() {
        MyRule rule = new MyRule();
        value_ = null;
        assertFalse(rule.validate());
    }

    @Test
    void testGetError() {
        MyRule rule = new MyRule();
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
        assertEquals("the value", error.getSubject());
        assertEquals(rule.getSubject(), error.getSubject());
    }

    class MyRule extends AbstractValidationRule {
        public ValidationError getError() {
            return new ValidationError.MANDATORY("the value");
        }

        public boolean validate() {
            return value_ != null;
        }
    }
}
