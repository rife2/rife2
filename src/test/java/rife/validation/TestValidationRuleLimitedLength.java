/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleLimitedLength {
    @Test
    void testInstantiation() {
        Bean bean = new Bean("123456");
        ValidationRuleLimitedLength rule = new ValidationRuleLimitedLength("property", 1, 6).setBean(bean);
        assertNotNull(rule);
    }

    @Test
    void testValid() {
        Bean bean = new Bean("123456");
        ValidationRuleLimitedLength rule = new ValidationRuleLimitedLength("property", 1, 6).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testValidArray() {
        Bean bean = new Bean(new String[]{"123456", "FDF3", "9J"});
        ValidationRuleLimitedLength rule = new ValidationRuleLimitedLength("arrayProperty", 1, 6).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testInvalid() {
        Bean bean = new Bean("123456");
        ValidationRuleLimitedLength rule = new ValidationRuleLimitedLength("property", 1, 4).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    void testInvalidArray() {
        Bean bean = new Bean(new String[]{"123456", "FDF3", "9J"});
        ValidationRuleLimitedLength rule = new ValidationRuleLimitedLength("arrayProperty", 3, 6).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    void testUnknownProperty() {
        Bean bean = new Bean("123456");
        ValidationRuleLimitedLength rule = new ValidationRuleLimitedLength("unknown_property", 1, 6).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testGetError() {
        Bean bean = new Bean("123456");
        ValidationRuleLimitedLength rule = new ValidationRuleLimitedLength("property", 1, 4).setBean(bean);
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_WRONG_LENGTH, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertEquals(rule.getSubject(), error.getSubject());
    }

    public class Bean {
        private String property_ = null;
        private String[] arrayProperty_ = null;

        public Bean(String property) {
            property_ = property;
        }

        public Bean(String[] arrayProperty) {
            arrayProperty_ = arrayProperty;
        }

        public void setArrayProperty(String[] arrayProperty) {
            arrayProperty_ = arrayProperty;
        }

        public String[] getArrayProperty() {
            return arrayProperty_;
        }

        public void setProperty(String property) {
            property_ = property;
        }

        public String getProperty() {
            return property_;
        }
    }
}
