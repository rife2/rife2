/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationRuleLimitedDate {
    @Test
    public void testInstantiation() {
        Bean bean = new Bean(new Date(2003, 12, 11));
        ValidationRuleLimitedDate rule = new ValidationRuleLimitedDate("property", new Date(2003, 3, 1), new Date(2004, 3, 1)).setBean(bean);
        assertNotNull(rule);
    }

    @Test
    public void testValid() {
        Bean bean = new Bean(new Date(2003, 12, 11));
        ValidationRuleLimitedDate rule = new ValidationRuleLimitedDate("property", new Date(2003, 3, 1), new Date(2004, 3, 1)).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testValidArray() {
        Bean bean = new Bean(new Date[]{new Date(2003, 12, 11), new Date(2005, 3, 7)});
        ValidationRuleLimitedDate rule = new ValidationRuleLimitedDate("arrayProperty", new Date(2003, 3, 1), new Date(2006, 3, 1)).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testInvalid() {
        Bean bean = new Bean(new Date(2003, 12, 11));
        ValidationRuleLimitedDate rule = new ValidationRuleLimitedDate("property", new Date(2004, 3, 1), new Date(2004, 4, 1)).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testInvalidArray() {
        Bean bean = new Bean(new Date[]{new Date(2003, 12, 11), new Date(2006, 3, 7)});
        ValidationRuleLimitedDate rule = new ValidationRuleLimitedDate("arrayProperty", new Date(2003, 3, 1), new Date(2006, 3, 1)).setBean(bean);
        assertFalse(rule.validate());
    }

    @Test
    public void testUnknownProperty() {
        Bean bean = new Bean(new Date(2003, 12, 11));
        ValidationRuleLimitedDate rule = new ValidationRuleLimitedDate("unknown_property", new Date(2003, 3, 1), new Date(2004, 3, 1)).setBean(bean);
        assertTrue(rule.validate());
    }

    @Test
    public void testGetError() {
        Bean bean = new Bean(new Date(2003, 12, 11));
        ValidationRuleLimitedDate rule = new ValidationRuleLimitedDate("property", new Date(2004, 3, 1), new Date(2004, 4, 1)).setBean(bean);
        ValidationError error = rule.getError();
        assertEquals(ValidationError.IDENTIFIER_INVALID, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertEquals(rule.getSubject(), error.getSubject());
    }

    public class Bean {
        private Date property_ = null;
        private Date[] arrayProperty_ = null;

        public Bean(Date property) {
            property_ = property;
        }

        public Bean(Date[] arrayProperty) {
            arrayProperty_ = arrayProperty;
        }

        public void setProperty(Date property) {
            property_ = property;
        }

        public Date getProperty() {
            return property_;
        }

        public void setArrayProperty(Date[] arrayProperty) {
            arrayProperty_ = arrayProperty;
        }

        public Date[] getArrayProperty() {
            return arrayProperty_;
        }
    }
}
