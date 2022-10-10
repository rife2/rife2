/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleSameAs {
    @Test
    public void testInstantiation() {
        Bean bean = new Bean("value", "value");
        ValidationRuleSameAs rule = new ValidationRuleSameAs("other", "property").setBean(bean);
        assertNotNull(rule);
    }

    @Test
    public void testValid() {
        Bean bean = new Bean("value", "value");
        ValidationRuleSameAs rule = new ValidationRuleSameAs("other", "property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testValidArray() {
        Bean bean = new Bean(new String[]{"value", "value"}, "value");
        ValidationRuleSameAs rule = new ValidationRuleSameAs("propertyArray", "other").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testInvalid() {
        Bean bean = new Bean("value", "value2");
        ValidationRuleSameAs rule = new ValidationRuleSameAs("other", "property").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInalidArray() {
        Bean bean = new Bean(new String[]{"value", "value2"}, "value");
        ValidationRuleSameAs rule = new ValidationRuleSameAs("propertyArray", "other").setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testUnknownProperty() {
        Bean bean = new Bean("value", "value2");
        ValidationRuleSameAs rule = new ValidationRuleSameAs("other", "unknown_property").setBean(bean);
        assertTrue(rule.validate());
        rule = new ValidationRuleSameAs("unknown_other", "property").setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testGetError() {
        Bean bean = new Bean("value", "value2");
        ValidationRuleSameAs rule = new ValidationRuleSameAs("other", "property").setBean(bean);
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_NOTSAME, error.getIdentifier());
        assertEquals("other", error.getSubject());
        assertEquals(rule.getSubject(), error.getSubject());
    }

    public class Bean {
        private String property_ = null;
        private String[] propertyArray_ = null;
        private String other_ = null;

        public Bean(String property, String other) {
            property_ = property;
            other_ = other;
        }

        public Bean(String[] propertyArray, String other) {
            propertyArray_ = propertyArray;
            other_ = other;
        }

        public void setProperty(String property) {
            property_ = property;
        }

        public String getProperty() {
            return property_;
        }

        public void setPropertyArray(String[] propertyArray) {
            propertyArray_ = propertyArray;
        }

        public String[] getPropertyArray() {
            return propertyArray_;
        }

        public void setOther(String other) {
            other_ = other;
        }

        public String getOther() {
            return other_;
        }
    }
}
