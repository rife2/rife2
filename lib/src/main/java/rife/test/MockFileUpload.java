/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import rife.config.RifeConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An instance of this class provides all the data that is needed to simulate
 * a file upload.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockFileUpload {
    private File file_;
    private InputStream inputStream_;
    private String fileName_;
    private String contentType_ = "text/plain";

    /**
     * Creates a new file upload simulation based on a <code>File</code>
     * object.
     * <p>The content type will be guessed from the file extension. The
     * extension to mime-type mapping is retrieved from {@link
     * rife.config.RifeConfig.Mime}.
     *
     * @param file the file that will be uploaded
     * @since 1.0
     */
    public MockFileUpload(File file) {
        if (null == file) throw new IllegalArgumentException("file can't be null.");

        file_ = file;
        guessContentType();
    }

    /**
     * Creates a new file upload simulation based on a <code>File</code>
     * object.
     *
     * @param file        the file that will be uploaded
     * @param contentType the content type of the file
     * @since 1.0
     */
    public MockFileUpload(File file, String contentType) {
        if (null == file) throw new IllegalArgumentException("file can't be null.");

        file_ = file;
        if (null == contentType) {
            guessContentType();
        } else {
            contentType_ = contentType;
        }
    }

    /**
     * Creates a new file upload simulation based on an
     * <code>InputStream</code>.
     *
     * @param fileName    the name of file that will be uploaded
     * @param inputStream the input stream that will be read to provide the
     *                    content of the uploaded file
     * @param contentType the content type of the uploaded file
     * @since 1.0
     */
    public MockFileUpload(String fileName, InputStream inputStream, String contentType) {
        if (null == fileName) throw new IllegalArgumentException("fileName can't be null.");
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        fileName_ = fileName;
        inputStream_ = inputStream;
        if (null == contentType) {
            guessContentType();
        } else {
            contentType_ = contentType;
        }
    }

    InputStream getInputStream()
    throws IOException {
        if (null == inputStream_) {
            inputStream_ = new FileInputStream(file_);
        }
        return inputStream_;
    }

    String getFileName() {
        if (null == fileName_) {
            fileName_ = file_.getAbsolutePath();
        }
        return fileName_;
    }

    /**
     * Returns the content type associated with this file upload simulation.
     * <p>If no content type has been provided, and it could not be detected
     * automatically, then it defaults to <code>text/plain</code>.
     *
     * @return the content type
     * @since 1.0
     */
    public String getContentType() {
        return contentType_;
    }

    private void guessContentType() {
        var extension = getExtension(getFileName());
        if (null == extension) {
            return;
        }

        var content_type = RifeConfig.Mime.getMimeType(extension);
        if (content_type != null) {
            contentType_ = content_type;
        }
    }

    private String getExtension(String fileName) {
        var last_dot_index = fileName.lastIndexOf('.');
        if (-1 == last_dot_index) {
            return null;
        }
        return fileName.substring(last_dot_index + 1);
    }
}
