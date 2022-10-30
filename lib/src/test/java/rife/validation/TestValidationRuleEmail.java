/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleEmail {
    @Test
    public void testInstantiation() {
        Bean bean = new Bean("email@domain.com");
        ValidationRuleEmail rule = new ValidationRuleEmail("property").setBean(bean);
        assertNotNull(rule);
    }

    @Test
    public void testValid() {
        Bean bean = new Bean("email@domain.com");
        ValidationRuleEmail rule = new ValidationRuleEmail("property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testValidArray() {
        Bean bean = new Bean(new String[]{"email@domain.com", "you@mymail.org"});
        ValidationRuleEmail rule = new ValidationRuleEmail("arrayProperty").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testInvalid() {
        Bean bean = new Bean("email@dom@ain.com");
        ValidationRuleEmail rule = new ValidationRuleEmail("property").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInvalidArray() {
        Bean bean = new Bean(new String[]{"you@mymail.org", "email@dom@ain.com", "email@domain.com"});
        ValidationRuleEmail rule = new ValidationRuleEmail("arrayProperty").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInvalid2() {
        Bean bean = new Bean("someone@hotmail..com");
        ValidationRuleEmail rule = new ValidationRuleEmail("property").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testUnknownProperty() {
        Bean bean = new Bean("email@domain.com");
        ValidationRuleEmail rule = new ValidationRuleEmail("unknown_property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testGetError() {
        Bean bean = new Bean("email@domain.com");
        ValidationRuleEmail rule = new ValidationRuleEmail("property").setBean(bean);
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
