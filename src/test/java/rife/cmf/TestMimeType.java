/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import org.junit.jupiter.api.Test;
import rife.validation.ConstrainedProperty;

import static org.junit.jupiter.api.Assertions.*;

public class TestMimeType {
    @Test
    void testMimeTypeIdentifiers() {
        assertEquals("application/xhtml+xml", MimeType.APPLICATION_XHTML.toString());
        assertEquals("text/plain", MimeType.TEXT_PLAIN.toString());
        assertEquals("image/gif", MimeType.IMAGE_GIF.toString());
        assertEquals("image/jpeg", MimeType.IMAGE_JPEG.toString());
        assertEquals("image/png", MimeType.IMAGE_PNG.toString());
    }

    @Test
    void testGetMimeType() {
        assertSame(MimeType.getMimeType("application/xhtml+xml"), MimeType.APPLICATION_XHTML);
        assertSame(MimeType.getMimeType("text/plain"), MimeType.TEXT_PLAIN);
        assertSame(MimeType.getMimeType("image/gif"), MimeType.IMAGE_GIF);
        assertSame(MimeType.getMimeType("image/jpeg"), MimeType.IMAGE_JPEG);
        assertSame(MimeType.getMimeType("image/png"), MimeType.IMAGE_PNG);
    }

    @Test
    void testGetUnsupportedMimeType() {
        assertNull(MimeType.getMimeType("uwynsspecial/type"));
    }

    @Test
    void testFormatters() {
        assertNotNull(MimeTypeFormatter.getFormatter(MimeType.APPLICATION_XHTML));
        assertNotNull(MimeTypeFormatter.getFormatter(MimeType.TEXT_PLAIN));
        assertNotNull(MimeTypeFormatter.getFormatter(MimeType.IMAGE_GIF));
        assertNotNull(MimeTypeFormatter.getFormatter(MimeType.IMAGE_JPEG));
        assertNotNull(MimeTypeFormatter.getFormatter(MimeType.IMAGE_PNG));
    }

    @Test
    void testValidationRuleApplicationXhtml() {
        var rule = MimeTypeValidation.getValidationRule(MimeType.APPLICATION_XHTML, new ConstrainedProperty("xhtml"));
        assertNotNull(rule);
        rule.setBean(new TestBean(false));
        assertTrue(rule.validate());
        rule.setBean(new TestBean(true));
        assertFalse(rule.validate());
    }

    @Test
    void testValidationRuleTextPlain() {
        var rule = MimeTypeValidation.getValidationRule(MimeType.TEXT_PLAIN, new ConstrainedProperty("textplain"));
        assertNull(rule);
    }

    @Test
    void testValidationRuleImageGif() {
        var rule = MimeTypeValidation.getValidationRule(MimeType.IMAGE_GIF, new ConstrainedProperty("gif"));
        assertNotNull(rule);
        rule.setBean(new TestBean(false));
        assertTrue(rule.validate());
        rule.setBean(new TestBean(true));
        assertFalse(rule.validate());
    }

    @Test
    void testValidationRuleImageJpeg() {
        var rule = MimeTypeValidation.getValidationRule(MimeType.IMAGE_JPEG, new ConstrainedProperty("jpeg"));
        assertNotNull(rule);
        rule.setBean(new TestBean(false));
        assertTrue(rule.validate());
        rule.setBean(new TestBean(true));
        assertFalse(rule.validate());
    }

    @Test
    void testValidationRuleImagePng() {
        var rule = MimeTypeValidation.getValidationRule(MimeType.IMAGE_PNG, new ConstrainedProperty("png"));
        assertNotNull(rule);
        rule.setBean(new TestBean(false));
        assertTrue(rule.validate());
        rule.setBean(new TestBean(true));
        assertFalse(rule.validate());
    }

    public static class TestBean {
        private String xhtml_ = null;
        private byte[] gif_ = null;
        private byte[] jpeg_ = null;
        private byte[] png_ = null;

        public TestBean(boolean invalid) {
            if (invalid) {
                xhtml_ = "invalid<sometag>";
                gif_ = "invalid".getBytes();
                jpeg_ = "invalid".getBytes();
                png_ = "invalid".getBytes();
            }
        }

        public String getXhtml() {
            return xhtml_;
        }

        public void setXhtml(String xhtml) {
            xhtml_ = xhtml;
        }

        public byte[] getGif() {
            return gif_;
        }

        public void setGif(byte[] gif) {
            gif_ = gif;
        }

        public byte[] getJpeg() {
            return jpeg_;
        }

        public void setJpeg(byte[] jpeg) {
            jpeg_ = jpeg;
        }

        public byte[] getPng() {
            return png_;
        }

        public void setPng(byte[] png) {
            png_ = png;
        }
    }
}
