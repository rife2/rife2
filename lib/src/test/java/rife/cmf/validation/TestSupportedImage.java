/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.validation;

import org.junit.jupiter.api.Test;
import rife.cmf.MimeType;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import static org.junit.jupiter.api.Assertions.*;

public class TestSupportedImage {
    @Test
    void testValidateNull()
    throws Exception {
        var rule = new SupportedImage("image");
        var bean = new ImageBean();
        var property = new ConstrainedProperty("image").mimeType(MimeType.IMAGE_PNG);
        bean.addConstraint(property);
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
        assertNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateSupported()
    throws Exception {
        var rule = new SupportedImage("image");
        var bean = new ImageBean();
        var property = new ConstrainedProperty("image").mimeType(MimeType.IMAGE_PNG);
        bean.addConstraint(property);
        var image_resource = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var image_bytes = FileUtils.readBytes(image_resource);
        bean.setImage(image_bytes);
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
        assertNotNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateUnsupported()
    throws Exception {
        var rule = new SupportedImage("image");
        var bean = new ImageBean();
        var property = new ConstrainedProperty("image").mimeType(MimeType.IMAGE_PNG);
        bean.addConstraint(property);
        var image_bytes = new byte[]{2, 9, 7, 12, 45}; // just random values
        bean.setImage(image_bytes);
        rule.setBean(bean);
        assertFalse(rule.validate());
        // TODO
//        assertTrue(rule.getLoadingErrors().size() > 0);
        assertNull(property.getCachedLoadedData());
    }

    @Test
    void testValidateNotConstrained()
    throws Exception {
        var rule = new SupportedImage("image");
        var bean = new ImageBeanNotConstrained();
        var image_resource = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var image_bytes = FileUtils.readBytes(image_resource);
        bean.setImage(image_bytes);
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
    }

    @Test
    void testValidateNotCmfProperty()
    throws Exception {
        var rule = new SupportedImage("image");
        var bean = new ImageBeanValidation();
        var property = new ConstrainedProperty("image");
        bean.addConstraint(property);
        var image_resource = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var image_bytes = FileUtils.readBytes(image_resource);
        bean.setImage(image_bytes);
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
    }

    @Test
    void testValidateUnknownProperty()
    throws Exception {
        var rule = new SupportedImage("image_unknown");
        var bean = new ImageBean();
        rule.setBean(bean);
        assertTrue(rule.validate());
        assertNull(rule.getLoadingErrors());
    }

    @Test
    void testGetError()
    throws Exception {
        var rule = new SupportedImage("image");
        assertEquals("image", rule.getError().getSubject());
        assertEquals("invalid", rule.getError().getIdentifier());
    }

    public static class ImageBean extends Validation {
        private byte[] image_ = null;

        public ImageBean() {
        }

        public byte[] getImage() {
            return image_;
        }

        public void setImage(byte[] image) {
            image_ = image;
        }
    }

    public static class ImageBeanNotConstrained {
        private byte[] image_ = null;

        public ImageBeanNotConstrained() {
        }

        public byte[] getImage() {
            return image_;
        }

        public void setImage(byte[] image) {
            image_ = image;
        }
    }

    public static class ImageBeanValidation extends Validation {
        private byte[] image_ = null;

        public ImageBeanValidation() {
        }

        public byte[] getImage() {
            return image_;
        }

        public void setImage(byte[] image) {
            image_ = image;
        }
    }
}
