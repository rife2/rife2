/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleUrl {
    @Test
    void testInstantiation() {
        Bean bean = new Bean("http://www.test.com");
        ValidationRuleUrl rule = new ValidationRuleUrl("property").setBean(bean);
        assertNotNull(rule);
    }

    @Test
    void testValid() {
        Bean bean = new Bean("http://www.test.com");
        ValidationRuleUrl rule = new ValidationRuleUrl("property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testValidArray() {
        Bean bean = new Bean(new String[]{"http://www.test.com", "http://www.uwyn.com", "http://www.rifers.org"});
        ValidationRuleUrl rule = new ValidationRuleUrl("arrayProperty").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testInvalid() {
        Bean bean = new Bean("htt:www/.test.com");
        ValidationRuleUrl rule = new ValidationRuleUrl("property").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    void testInvalidArray() {
        Bean bean = new Bean(new String[]{"http://www.test.com", "http://www.uwyn.com", "htt:www/.rifersorg"});
        ValidationRuleUrl rule = new ValidationRuleUrl("arrayProperty").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    void testUnknownProperty() {
        Bean bean = new Bean("http://www.test.com");
        ValidationRuleUrl rule = new ValidationRuleUrl("unknown_property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testGetError() {
        Bean bean = new Bean("http://www.test.com");
        ValidationRuleUrl rule = new ValidationRuleUrl("property").setBean(bean);
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_INVALID, error.getIdentifier());
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
