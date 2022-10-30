/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleInList {
    @Test
    public void testInstantiation() {
        Bean bean = new Bean("entry");
        ValidationRuleInList rule = new ValidationRuleInList("property", new String[]{"one", "two", "entry", "three"}).setBean(bean);
        assertNotNull(rule);
    }

    @Test
    public void testValid() {
        Bean bean = new Bean("entry");
        ValidationRuleInList rule = new ValidationRuleInList("property", new String[]{"one", "two", "entry", "three"}).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testValidArray() {
        Bean bean = new Bean(new String[]{"entry", "two"});
        ValidationRuleInList rule = new ValidationRuleInList("arrayProperty", new String[]{"one", "two", "entry", "three"}).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testInvalid() {
        Bean bean = new Bean("entrykolo");
        ValidationRuleInList rule = new ValidationRuleInList("property", new String[]{"one", "two", "entry", "three"}).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInvalidArray() {
        Bean bean = new Bean(new String[]{"two", "one", "entrykolo"});
        ValidationRuleInList rule = new ValidationRuleInList("arrayProperty", new String[]{"one", "two", "entry", "three"}).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testUnknownProperty() {
        Bean bean = new Bean("entry");
        ValidationRuleInList rule = new ValidationRuleInList("unknown_property", new String[]{"one", "two", "entry", "three"}).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testGetError() {
        Bean bean = new Bean("entry");
        ValidationRuleInList rule = new ValidationRuleInList("property", new String[]{"one", "two", "entry", "three"}).setBean(bean);
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
