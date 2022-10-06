/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.tools.ExceptionUtils;

import java.io.File;
import java.util.logging.Logger;

/**
 * An <code>UploadedFile</code> instance is created by the web engine when
 * files are uploaded through a multi-part request.
 * <p>The uploaded files can be retrieved through the
 * <code>ElementSupport#getUploadedFile</code> method and its siblings. The
 * web engine does its best to dispose of the temporary file at a convenient
 * time, but the file is not guaranteed to persist after the request. If you
 * want to make sure that the file is deleted, you should call {@link
 * File#delete} yourself when you're finished with the uploaded file.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.0
 */
public class UploadedFile implements Cloneable, AutoCloseable {
    private File tempFile_ = null;
    private String filename_ = null;
    private String type_ = null;
    private boolean sizeExceeded_ = false;

    public UploadedFile(String filename, String type) {
        filename_ = filename;
        type_ = type;
    }

    @Override
    public void close()
    throws Exception {
        if (tempFile_ != null) {
            tempFile_.delete();
        }
    }

    public void setTempFile(File tempFile) {
        assert tempFile != null;
        assert tempFile.exists();
        assert tempFile.isFile();
        assert tempFile.canRead();

        tempFile_ = tempFile;
        tempFile_.deleteOnExit();
    }

    public void setSizeExceeded(boolean exceeded) {
        sizeExceeded_ = exceeded;
    }

    /**
     * Retrieves the content type of the file.
     *
     * @return the content type of the uploaded file
     * @since 1.0
     */
    public String getType() {
        return type_;
    }

    /**
     * Retrieves the name of the file that was selected on the client when
     * uploading.
     *
     * @return the name of the original file that was uploaded
     * @since 1.0
     */
    public String getName() {
        return filename_;
    }

    /**
     * Retrieves the temporary file on the server that was created for the
     * upload.
     *
     * @return the temporary uploaded file
     * @since 1.0
     */
    public File getFile() {
        return tempFile_;
    }

    /**
     * Indicates whether the uploaded file exceeded the file {@link
     * RifeConfig.EngineConfig#getFileuploadSizeLimit upload
     * size limit}.
     * <p>If the limit was exceeded, the temporary file will be
     * <code>null</code> and deleted from the server.
     *
     * @return <code>true</code> if the upload file size limit was exceeded;
     * or
     * <p><code>false</code> otherwise
     * @since 1.0
     */
    public boolean wasSizeExceeded() {
        return sizeExceeded_;
    }

    public UploadedFile clone() {
        try {
            return (UploadedFile) super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.getLogger("rife.engine").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }
}

