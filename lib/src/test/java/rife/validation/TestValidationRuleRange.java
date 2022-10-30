/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleRange {
    @Test
    public void testInstantiation() {
        Bean bean = new Bean(21);
        ValidationRuleRange rule = new ValidationRuleRange("property", 12, 30).setBean(bean);
        assertNotNull(rule);
    }

    @Test
    public void testValid() {
        Bean bean = new Bean(21);
        ValidationRuleRange rule = new ValidationRuleRange("property", 12, 30).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testValidArray() {
        Bean bean = new Bean(new int[]{21, 24, 30});
        ValidationRuleRange rule = new ValidationRuleRange("arrayproperty", 12, 30).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testInvalid() {
        Bean bean = new Bean(21);
        ValidationRuleRange rule = new ValidationRuleRange("property", 12, 20).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInvalidArray() {
        Bean bean = new Bean(new int[]{21, 24, 30});
        ValidationRuleRange rule = new ValidationRuleRange("arrayproperty", 12, 29).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testUnknownProperty() {
        Bean bean = new Bean(21);
        ValidationRuleRange rule = new ValidationRuleRange("unknown_property", 12, 30).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testGetError() {
        Bean bean = new Bean(21);
        ValidationRuleRange rule = new ValidationRuleRange("property", 12, 20).setBean(bean);
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_INVALID, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertEquals(rule.getSubject(), error.getSubject());
    }

    public class Bean {
        private int property_;
        private int[] arrayproperty_;

        public Bean(int property) {
            property_ = property;
        }

        public Bean(int[] arrayproperty) {
            arrayproperty_ = arrayproperty;
        }

        public void setProperty(int property) {
            property_ = property;
        }

        public int getProperty() {
            return property_;
        }

        public void setArrayproperty(int[] arrayproperty) {
            arrayproperty_ = arrayproperty;
        }

        public int[] getArrayproperty() {
            return arrayproperty_;
        }
    }
}
