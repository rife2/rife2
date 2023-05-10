/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.format;

import rife.cmf.Content;
import rife.cmf.MimeType;
import rife.cmf.format.exceptions.FormatException;
import rife.cmf.format.exceptions.InvalidContentDataTypeException;
import rife.cmf.format.exceptions.UnexpectedConversionErrorException;
import rife.cmf.format.exceptions.UnreadableDataFormatException;
import rife.cmf.format.exceptions.UnsupportedTargetMimeTypeException;
import rife.cmf.loader.ImageContentLoader;
import rife.cmf.loader.LoadedContent;
import rife.cmf.transform.ContentTransformer;
import rife.tools.Convert;
import rife.tools.exceptions.ConversionException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.*;

/**
 * Formats raw {@code Content} data as an image.
 *
 * <p>The following content attributes are supported:
 * <table>
 * <caption>Content Attributes</caption>
 * <tr>
 * <td>{@code width}</td>
 * <td>Changes the width of the image. If no height is provided, the image
 * will be proportionally scaled.</td>
 * </tr>
 * <tr>
 * <td>{@code height}</td>
 * <td>Changes the height of the image. If no width is provided, the image
 * will be proportionally scaled.</td>
 * </tr>
 * <tr>
 * <td>{@code longest-edge-length}</td>
 * <td>Changes the longest edge of the image.  Aspect ratio is preserved.  The &quot;width&quot; or
 * &quot;height&quot; attributes take precedence if set, and this attribute will be ignored.
 * </td>
 * </tr>
 * </table>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Formatter
 * @since 1.0
 */
public class ImageFormatter implements Formatter<byte[], Image> {
    public static final class ContentAttribute {
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String LONGEST_EDGE_LENGTH = "longestEdgeLength";
        public static final String HIDPI = "hidpi";
    }

    public static final class CmfProperty {
        public static final String WIDTH = "cmf:width";
        public static final String HEIGHT = "cmf:height";
        public static final String HIDPI = "cmf:hidpi";
    }

    public byte[] format(Content content, ContentTransformer<Image> transformer)
    throws FormatException {
        byte[] content_bytes;
        // we only support byte arrays as input
        if (!(content.getData() instanceof byte[] bytes)) {
            throw new InvalidContentDataTypeException(this, content.getMimeType(), byte[].class, content.getData().getClass());
        }
        content_bytes = bytes;

        LoadedContent<Image> loaded = null;
        Image data = null;

        // check if the content contains a cached value of the loaded data
        if (content.hasCachedLoadedData()) {
            var cached = content.getCachedLoadedData();
            if (cached instanceof LoadedContent<?> cached_loaded) {
                loaded = (LoadedContent<Image>) cached_loaded;
                data = loaded.data();
            } else {
                data = (Image)cached;
            }
        }

        if (null == data) {
            // get an image
            Set<String> errors = new HashSet<>();
            loaded = new ImageContentLoader().load(content_bytes, false, errors);
            if (null == loaded) {
                throw new UnreadableDataFormatException(content.getMimeType(), errors);
            }

            data = loaded.data();
        }

        boolean was_transformed = false;

        // determine image type
        var image_type = BufferedImage.TYPE_INT_ARGB;
        if (content.getMimeType() == MimeType.IMAGE_JPEG) {
            image_type = BufferedImage.TYPE_INT_RGB;
        }

        // perform additional conversions according to the provided attributes
        boolean hidpi = true;
        if (content.hasAttributes()) {
            if (content.hasAttribute(ContentAttribute.HIDPI)) {
                try {
                    hidpi = Convert.toBoolean(content.getAttribute(ContentAttribute.HIDPI));
                } catch (ConversionException e) {
                    throw new RuntimeException(e);
                }
            }

            var width = -1;
            var height = -1;
            if (content.hasAttribute(ContentAttribute.WIDTH) ||
                content.hasAttribute(ContentAttribute.HEIGHT)) {
                var width_value = content.getAttribute(ContentAttribute.WIDTH);
                var height_value = content.getAttribute(ContentAttribute.HEIGHT);
                // retrieve the width and the height values
                if (width_value != null) {
                    try {
                        width = Integer.parseInt(width_value);
                    } catch (NumberFormatException e) {
                        throw new FormatException(e);
                    }
                }
                if (height_value != null) {
                    try {
                        height = Integer.parseInt(height_value);
                    } catch (NumberFormatException e) {
                        throw new FormatException(e);
                    }
                }
            } else if (content.hasAttribute(ContentAttribute.LONGEST_EDGE_LENGTH)) {
                // retrieve the attributes
                var lel_value = content.getAttribute(ContentAttribute.LONGEST_EDGE_LENGTH);
                var lel = -1;
                if (lel_value != null) {
                    try {
                        lel = Integer.parseInt(lel_value);
                    } catch (NumberFormatException e) {
                        throw new FormatException(e);
                    }
                }

                if (lel >= 0) {
                    var orig_width = data.getWidth(null);
                    var orig_height = data.getHeight(null);

                    // If the width is the longer side, set it to the lEL.
                    if (orig_width >= orig_height) {
                        if (lel >= 0) {
                            width = lel;
                        }
                    } else {
                        if (lel >= 0) {
                            height = lel;
                        }
                    }
                }
            }

            if (width >= 0 || height >= 0) {
                var orig_width = data.getWidth(null);
                var orig_height = data.getHeight(null);

                // ensure that the aspect is preserved at all times
                if (width >= 0 && height >= 0) {
                    var width_ratio = ((double) orig_width) / width;
                    var height_ratio = ((double) orig_height) / height;
                    if (width_ratio > height_ratio) {
                        height = -1;
                    } else if (width_ratio < height_ratio) {
                        width = -1;
                    }
                }
                if (height == -1) {
                    height = (int) (((double) (orig_height * width) / orig_width) + 0.5);
                }
                if (width == -1) {
                    width = (int) ((((double) orig_width * height) / orig_height) + 0.5);
                }

                // only rescale when the dimensions are actually different
                if ((width >= 0 && width != orig_width) ||
                    (height >= 0 && height != orig_height)) {
                    data = progressiveScaling(data, Math.max(width, height), image_type);
                    was_transformed = true;
                }
            }
        }

        // transform the content, if needed
        if (transformer != null) {
            data = transformer.transform(data, content.getAttributes());
            was_transformed = true;
        }

        // if no transformation was applied to the data and the provided data already
        // has the requested mime-type, simply pass the data on
        if (!was_transformed && loaded != null && loaded.originalMimeType() == content.getMimeType()) {
            return content_bytes;
        }

        // draw it on a new buffer
        var buffer = new BufferedImage(data.getWidth(null), data.getHeight(null), image_type);
        var g2 = buffer.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.drawImage(data, 0, 0, null);
        g2.dispose();

        // set the content data properties
        content
            .property(CmfProperty.WIDTH, String.valueOf(buffer.getWidth()))
            .property(CmfProperty.HEIGHT, String.valueOf(buffer.getHeight()))
            .property(CmfProperty.HIDPI, hidpi);

        // write it out as the correct mimetype
        var bytes_out = new ByteArrayOutputStream();
        var buffered_out = new BufferedOutputStream(bytes_out);

        try {
            // retrieve a supported writer
            var writers = ImageIO.getImageWritersByMIMEType(content.getMimeType().getIdentifier());
            ImageWriter writer = null;
            ImageWriteParam write_param = null;
            if (writers.hasNext()) {
                writer = writers.next();
            }
            if (null == writer) {
                throw new UnsupportedTargetMimeTypeException(content.getMimeType());
            }
            var image_out = ImageIO.createImageOutputStream(buffered_out);
            writer.setOutput(image_out);
            if (content.getMimeType() == MimeType.IMAGE_JPEG) {
                write_param = writer.getDefaultWriteParam();
                write_param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                write_param.setCompressionQuality(0.85f);
                write_param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            }
            writer.write(null, new IIOImage(buffer, null, null), write_param);
            writer.dispose();
            bytes_out.flush();
            bytes_out.close();
        } catch (IOException e) {
            throw new UnexpectedConversionErrorException(e);
        }

        return bytes_out.toByteArray();
    }

    private static BufferedImage progressiveScaling(Image before, double longestSideLength, int imageType) {
        if (before == null) {
            return null;
        }

        var w = before.getWidth(null);
        var h = before.getHeight(null);
        var ratio = h > w ? longestSideLength / h : longestSideLength / w;

        while (ratio < 0.5) {
            before = scaleImage(before, 0.5, imageType);
            w = before.getWidth(null);
            h = before.getHeight(null);
            ratio = h > w ? longestSideLength / h : longestSideLength / w;
        }
        return scaleImage(before, ratio, imageType);
    }

    private static BufferedImage scaleImage(Image image, double ratio, int imageType) {
        var scaled_width = (int) (image.getWidth(null) * ratio + 0.5);
        var scaled_height = (int) (image.getHeight(null) * ratio + 0.5);
        var scaled_image = new BufferedImage(scaled_width, scaled_height, imageType);
        var g2 = scaled_image.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.drawImage(image, 0, 0, scaled_width, scaled_height, null);
        g2.dispose();
        return scaled_image;
    }
}

