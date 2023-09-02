/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import org.junit.jupiter.api.Test;
import rife.cmf.Content;
import rife.cmf.MimeType;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.format.exceptions.InvalidContentDataTypeException;
import rife.cmf.format.exceptions.UnreadableDataFormatException;
import rife.cmf.format.exceptions.UnsupportedTargetMimeTypeException;
import rife.cmf.loader.ImageContentLoader;
import rife.cmf.transform.ImageContentTransformer;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestImageFormatter {
    @Test
    void testFormatBasic()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatInvalidDataType()
    throws Exception {
        var content = new Content(MimeType.IMAGE_PNG, new Object());
        var formatter = new ImageFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (InvalidContentDataTypeException e) {
            assertSame(byte[].class, e.getExpectedType());
            assertSame(formatter, e.getFormatter());
            assertSame(MimeType.IMAGE_PNG, e.getMimeType());
            assertSame(Object.class, e.getReceivedType());
        }
    }

    @Test
    void testFormatCachedLoadedData()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        var image = new ImageContentLoader().load(data_image_gif, false, null);
        content.setCachedLoadedData(image);

        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatUnreadableData()
    throws Exception {
        var content = new Content(MimeType.IMAGE_PNG, new byte[]{34, 9, 12, 5, 92}); // random invalid bytes
        var formatter = new ImageFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (UnreadableDataFormatException e) {
            assertSame(MimeType.IMAGE_PNG, e.getMimeType());
            // TODO : check why no errors are added
//            assertTrue(e.getErrors().size() > 0);
        }
    }

    @Test
    void testFormatUnsupportedMimetype()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.APPLICATION_XHTML, data_image_gif);
        var formatter = new ImageFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (UnsupportedTargetMimeTypeException e) {
            assertSame(MimeType.APPLICATION_XHTML, e.getMimeType());
        }
    }

    @Test
    void testFormatAttributeWidth()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("width", 20);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_20.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatInvalidAttributeWidth()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("width", "notanumber");
        var formatter = new ImageFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (FormatException e) {
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    void testFormatAttributeLongestEdgeLengthHorizontal()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("longestEdgeLength", 20);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_20.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatAttributeLongestEdgeLengthVertical()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn-rotated_90_cw.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("longestEdgeLength", 20);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn-rotated_90_cw_resized-width_20.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatInvalidAttributeLongestEdgeLength()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("longestEdgeLength", "notanumber");
        var formatter = new ImageFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (FormatException e) {
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    void testFormatNegativeAttributeLongestEdgeLength()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("longestEdgeLength", "-20");
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatAttributeHeight()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("height", 15);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn_resized-height_15.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatInvalidAttributeHeight()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("height", "notanumber");
        var formatter = new ImageFormatter();
        try {
            formatter.format(content, null);
            fail();
        } catch (FormatException e) {
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    void testFormatPassThrough()
    throws Exception {
        var image_resource = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_300-height_70.jpg");
        var data_image = FileUtils.readBytes(image_resource);

        var content = new Content(MimeType.IMAGE_JPEG, data_image);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_jpg = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_300-height_70.jpg");
        var data_image_jpg = FileUtils.readBytes(image_resource_jpg);

        assertArrayEquals(data_image_jpg, result);
    }

    @Test
    void testFormatAttributeWidthHeight()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_JPEG, data_image_gif);
        content
            .attribute("width", 30)
            .attribute("height", 70);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_jpg = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_30-height_70.jpg");
        var data_image_jpg = FileUtils.readBytes(image_resource_jpg);

        assertArrayEquals(data_image_jpg, result);
    }

    @Test
    void testFormatAttributeWidthHeight2()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_JPEG, data_image_gif);
        content
            .attribute("width", 300)
            .attribute("height", 70);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_jpg = ResourceFinderClasspath.instance().getResource("uwyn_resized-width_300-height_70.jpg");
        var data_image_jpg = FileUtils.readBytes(image_resource_jpg);

        assertArrayEquals(data_image_jpg, result);
    }

    @Test
    void testFormatNegativeAttributeWidthHeight()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("width", -12)
            .attribute("height", -5);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatUnsupportedAttributes()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn.gif");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        content
            .attribute("unsupported", "blah");
        var formatter = new ImageFormatter();
        var result = formatter.format(content, null);

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    @Test
    void testFormatTransformer()
    throws Exception {
        var image_resource_gif = ResourceFinderClasspath.instance().getResource("uwyn_small.png");
        var data_image_gif = FileUtils.readBytes(image_resource_gif);

        var content = new Content(MimeType.IMAGE_PNG, data_image_gif);
        var formatter = new ImageFormatter();
        var result = formatter.format(content, new TransparentImageTransformer());

        assertNotNull(result);

        var image_resource_png = ResourceFinderClasspath.instance().getResource("uwyn-transparent.png");
        var data_image_png = FileUtils.readBytes(image_resource_png);

        assertArrayEquals(data_image_png, result);
    }

    static class TransparentImageTransformer implements ImageContentTransformer {
        @Override
        public Image transform(Image data, Map<String, String> attributes)
        throws ContentManagerException {
            // retrieve the rife logo to stamp on top of it
            var rife_url = ResourceFinderClasspath.instance().getResource("rife-logo_small.png");
            var rife_image = new ImageIcon(rife_url).getImage();

            // create a new drawing buffer
            var width = data.getWidth(null);
            var height = data.getHeight(null);
            var buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            var g2 = buffer.createGraphics();

            // make the background white
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);

            // draw a transparent image on it
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.drawImage(rife_image, 0, 0, null);

            // draw a transparent image on it
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.drawImage(data, 0, 0, null);

            // clean up
            g2.dispose();

            return buffer;
        }
    }
}
