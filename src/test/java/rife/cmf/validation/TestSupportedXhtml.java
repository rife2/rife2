/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.validation;

import org.junit.jupiter.api.Test;
import rife.cmf.MimeType;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import static org.junit.jupiter.api.Assertions.*;

public class TestSupportedXhtml {
    @Test
    void testValidateNull() {
        var rule = new SupportedXhtml("xhtml", true);
        var bean = new XhtmlBean();
        var property = new ConstrainedProperty("xhtml").mimeType(MimeType.APPLICATION_XHTML);
        bean.addConstraint(property);
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
        assertNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateSupportedDocument() {
        var rule = new SupportedXhtml("xhtml", false);
        var bean = new XhtmlBean();
        var property = new ConstrainedProperty("xhtml").mimeType(MimeType.APPLICATION_XHTML);
        bean.addConstraint(property);
        bean.setXhtml("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml"><head><title></title></head><body>
            <p>body</p>
            </body></html>""");
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
        assertNotNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateUnsupportedDocument() {
        var rule = new SupportedXhtml("xhtml", false);
        var bean = new XhtmlBean();
        var property = new ConstrainedProperty("xhtml").mimeType(MimeType.APPLICATION_XHTML);
        bean.addConstraint(property);
        bean.setXhtml("<pp>some <b>html</b> here</p>");
        rule.setBean(bean);
        assertFalse(rule.validate());
        assertTrue(!rule.getLoadingErrors().isEmpty());
        assertNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateSupportedFragment() {
        var rule = new SupportedXhtml("xhtml", true);
        var bean = new XhtmlBean();
        var property = new ConstrainedProperty("xhtml").mimeType(MimeType.APPLICATION_XHTML);
        bean.addConstraint(property);
        bean.setXhtml("<p>some <b>html</b> here</p>");
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
        assertNotNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateUnsupportedFragment() {
        var rule = new SupportedXhtml("xhtml", true);
        var bean = new XhtmlBean();
        var property = new ConstrainedProperty("xhtml").mimeType(MimeType.APPLICATION_XHTML);
        bean.addConstraint(property);
        bean.setXhtml("<i><b>error</i>");
        rule.setBean(bean);
        assertFalse(rule.validate());
        assertTrue(!rule.getLoadingErrors().isEmpty());
        assertNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateNotConstrained() {
        var rule = new SupportedXhtml("xhtml", true);
        var bean = new XhtmlBeanNotConstrained();
        bean.setXhtml("<p>some <b>html</b> here</p>");
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
    }

    @Test
    void testValidateNotCmfProperty() {
        var rule = new SupportedXhtml("xhtml", true);
        var bean = new XhtmlBeanValidation();
        var property = new ConstrainedProperty("xhtml");
        bean.addConstraint(property);
        bean.setXhtml("<p>some <b>html</b> here</p>");
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
    }

    @Test
    void testValidateUnknownProperty() {
        var rule = new SupportedXhtml("xhtml_unknown", true);
        var bean = new XhtmlBean();
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
    }

    @Test
    void testGetError() {
        var rule = new SupportedXhtml("xhtml", true);
        assertEquals("xhtml", rule.getError().getSubject());
        assertEquals("invalid", rule.getError().getIdentifier());
    }

    public static class XhtmlBean extends Validation {
        private String xhtml_ = null;

        public XhtmlBean() {
        }

        public String getXhtml() {
            return xhtml_;
        }

        public void setXhtml(String xhtml) {
            xhtml_ = xhtml;
        }
    }

    public static class XhtmlBeanValidation extends Validation {
        private String xhtml_ = null;

        public XhtmlBeanValidation() {
        }

        public String getXhtml() {
            return xhtml_;
        }

        public void setXhtml(String xhtml) {
            xhtml_ = xhtml;
        }
    }

    public static class XhtmlBeanNotConstrained {
        private String xhtml_ = null;

        public XhtmlBeanNotConstrained() {
        }

        public String getXhtml() {
            return xhtml_;
        }

        public void setXhtml(String xhtml) {
            xhtml_ = xhtml;
        }
    }
}
