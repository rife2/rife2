/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleNotEmpty {
    @Test
    public void testInstantiation() {
        Bean bean = new Bean("not empty");
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("property").setBean(bean);
        assertNotNull(rule);
    }

    @Test
    public void testValid() {
        Bean bean = new Bean("not empty");
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testValidArray() {
        Bean bean = new Bean(new String[]{"not empty", "not empty either"});
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("arrayProperty").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testInvalid() {
        Bean bean = new Bean("");
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("property").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInvalidArray() {
        Bean bean = new Bean(new String[]{"not empty", ""});
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("arrayProperty").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInvalidTrim() {
        Bean bean = new Bean("      ");
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("property").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testUnknownProperty() {
        Bean bean = new Bean("not empty");
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("unknown_property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testGetError() {
        Bean bean = new Bean("");
        ValidationRuleNotEmpty rule = new ValidationRuleNotEmpty("property").setBean(bean);
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
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
