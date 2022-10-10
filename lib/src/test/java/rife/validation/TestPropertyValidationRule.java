/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPropertyValidationRule {
    @Test
    public void testInstantiation() {
        Rule rule = new Rule("property");
        assertNotNull(rule);
        assertEquals("property", rule.getPropertyName());
        assertNull(rule.getBean());
        assertEquals("property", rule.getSubject());
    }

    @Test
    public void testPropertyName() {
        Rule rule = new Rule("property");
        assertSame(rule, rule.setPropertyName("property2"));
        assertEquals("property2", rule.getPropertyName());
        assertEquals("property", rule.getSubject());
    }

    @Test
    public void testBean() {
        Rule rule = new Rule("property");
        assertSame(rule, rule.setBean(this));
        assertSame(this, rule.getBean());
    }

    @Test
    public void testSubjectName() {
        Rule rule = new Rule("property");
        assertSame(rule, rule.setSubject("property2"));
        assertEquals("property", rule.getPropertyName());
        assertEquals("property2", rule.getSubject());
        assertSame(rule, rule.setSubject(null));
        assertEquals("property", rule.getSubject());
    }

    public class Rule extends PropertyValidationRule {
        Rule(String property) {
            super(property);
        }

        public boolean validate() {
            return false;
        }

        public ValidationError getError() {
            return null;
        }
    }
}
