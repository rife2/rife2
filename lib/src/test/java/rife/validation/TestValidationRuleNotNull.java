/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleNotNull {
    @Test
    void testInstantiation() {
        Bean bean = new Bean("not null");
        ValidationRuleNotNull rule = new ValidationRuleNotNull("property").setBean(bean);
        assertNotNull(rule);
    }

    @Test
    void testValid() {
        Bean bean = new Bean("not null");
        ValidationRuleNotNull rule = new ValidationRuleNotNull("property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testInvalid() {
        Bean bean = new Bean(null);
        ValidationRuleNotNull rule = new ValidationRuleNotNull("property").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    void testUnknownProperty() {
        Bean bean = new Bean("not null");
        ValidationRuleNotNull rule = new ValidationRuleNotNull("unknown_property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    void testGetError() {
        Bean bean = new Bean("");
        ValidationRuleNotNull rule = new ValidationRuleNotNull("property").setBean(bean);
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertEquals(rule.getSubject(), error.getSubject());
    }

    public class Bean {
        private String property_ = null;

        public Bean(String property) {
            property_ = property;
        }

        public void setProperty(String property) {
            property_ = property;
        }

        public String getProperty() {
            return property_;
        }
    }
}
