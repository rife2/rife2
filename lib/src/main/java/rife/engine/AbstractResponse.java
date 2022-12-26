/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.engine.exceptions.EngineException;
import rife.engine.exceptions.ResponseOutputStreamRetrievalErrorException;
import rife.template.InternalString;
import rife.template.Template;
import rife.tools.HttpUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

/**
 * This abstract class implements parts of the {@link Response} interface to
 * provide behaviour that is specific to RIFE.
 * <p>Additional abstract methods have been provided to integrate with the
 * concrete back-end classes that extend <code>AbstractResponse</code>.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class AbstractResponse implements Response {
    private final Request request_;

    protected String contentType_ = null;
    protected Element lastElement_ = null;
    protected boolean textBufferEnabled_ = true;
    protected ArrayList<CharSequence> textBuffer_ = null;
    protected OutputStream responseOutputStream_ = null;
    protected ByteArrayOutputStream gzipByteOutputStream_ = null;
    protected GZIPOutputStream gzipOutputStream_ = null;
    protected OutputStream outputStream_ = null;

    /**
     * This method needs to be implemented by the extending back-end class and
     * will be called by <code>AbstractResponse</code> during the
     * RIFE-specific additional behaviour. It behaves exactly like its {@link
     * Response#setContentType(String) counter-part in the Response interface}.
     *
     * @see Response#setContentType(String)
     * @since 1.0
     */
    protected abstract void _setContentType(String contentType);

    /**
     * This method needs to be implemented by the extending back-end class and
     * will be called by <code>AbstractResponse</code> during the
     * RIFE-specific additional behaviour. It behaves exactly like its {@link
     * Response#getOutputStream() counter-part in the Request interface}.
     *
     * @see Response#getOutputStream()
     * @since 1.0
     */
    protected abstract OutputStream _getOutputStream()
    throws IOException;

    /**
     * Constructor that needs to be called by all the constructors of the
     * extending classes.
     *
     * @param request the {@link Request} that is associated with this
     *                response
     * @since 1.0
     */
    protected AbstractResponse(Request request) {
        request_ = request;
    }

    /**
     * Retrieves the request that is associated with this response.
     *
     * @return the associated request
     * @since 1.0
     */
    public Request getRequest() {
        return request_;
    }

    public void setLastElement(Element element) {
        lastElement_ = element;
    }

    public Element getLastElement() {
        return lastElement_;
    }

    public boolean isContentTypeSet() {
        return contentType_ != null;
    }

    public String getContentType() {
        return contentType_;
    }

    public void setContentType(String contentType) {
        if (null == contentType) {
            return;
        }

        if (!contentType.contains(HttpUtils.CHARSET)) {
            contentType = contentType + "; charset=UTF-8";
        }

        contentType_ = contentType;
        _setContentType(contentType);
    }

    public void enableTextBuffer(boolean enabled) {
        if (textBufferEnabled_ != enabled) {
            flush();
        }

        textBufferEnabled_ = enabled;
    }

    public boolean isTextBufferEnabled() {
        return textBufferEnabled_;
    }

    public void print(Template template)
    throws EngineException {
        if (null == template) return;

        print(template.getDeferredContent());
    }

    public void print(Collection<CharSequence> deferredContent)
    throws EngineException {
        if (!isContentTypeSet()) {
            setContentType(RifeConfig.engine().getDefaultContentType());
        }

        if (null == deferredContent ||
            0 == deferredContent.size()) {
            return;
        }

        if (textBufferEnabled_) {
            if (outputStream_ != null) {
                try {
                    outputStream_.flush();
                } catch (IOException e) {
                    throw new EngineException(e);
                }
            }

            if (null == textBuffer_) {
                textBuffer_ = new ArrayList<CharSequence>();
            }
            textBuffer_.addAll(deferredContent);
        } else {
            writeDeferredContent(deferredContent);
        }
    }

    public void print(Object value)
    throws EngineException {
        if (!isContentTypeSet()) {
            setContentType(RifeConfig.engine().getDefaultContentType());
        }

        if (null == value) {
            return;
        }

        var text = String.valueOf(value);

        if (textBufferEnabled_) {
            if (outputStream_ != null) {
                try {
                    outputStream_.flush();
                } catch (IOException e) {
                    throw new EngineException(e);
                }
            }

            if (null == textBuffer_) {
                textBuffer_ = new ArrayList<CharSequence>();
            }
            textBuffer_.add(text);
        } else {
            ensureOutputStream();

            try {
                outputStream_.write(text.getBytes(getCharacterEncoding()));
                outputStream_.flush();
            } catch (IOException e) {
                throw new EngineException(e);
            }
        }
    }

    private void writeDeferredContent(Collection<CharSequence> deferredContent)
    throws EngineException {
        // create a string version of each char sequence so that any state operation happens
        // before any content is actually being written
        for (CharSequence charsequence : deferredContent) {
            charsequence.toString();
        }

        ensureOutputStream();

        String encoding = getCharacterEncoding();
        try {
            outputStream_.flush();

            // write the content to the output stream
            for (CharSequence charsequence : deferredContent) {
                if (charsequence instanceof rife.template.InternalString) {
                    outputStream_.write(((InternalString) charsequence).getBytes(encoding));
                } else if (charsequence instanceof java.lang.String) {
                    outputStream_.write(((String) charsequence).getBytes(encoding));
                }
            }

            outputStream_.flush();
        } catch (IOException e) {
            // don't do anything since this exception is merely caused by someone that
            // stopped or closed his browsing request
        }
    }

    public void clearBuffer() {
        if (textBuffer_ != null &&
            textBuffer_.size() > 0) {
            textBuffer_.clear();
        }
    }

    public void flush()
    throws EngineException {
        if (textBuffer_ != null &&
            textBuffer_.size() > 0) {
            writeDeferredContent(textBuffer_);

            textBuffer_.clear();
        }

        if (outputStream_ != null) {
            try {
                outputStream_.flush();
            } catch (IOException e) {
                // don't do anything, the response stream has probably been
                // closed or reset
            }
        }
    }

    public void close()
    throws EngineException {
        flush();

        if (outputStream_ != null) {
            try {
                if (gzipOutputStream_ != null) {
                    gzipOutputStream_.flush();
                    gzipOutputStream_.finish();

                    byte[] bytes = gzipByteOutputStream_.toByteArray();

                    gzipOutputStream_ = null;
                    gzipByteOutputStream_ = null;

                    setContentLength(bytes.length);
                    addHeader("Content-Encoding", "gzip");
                    responseOutputStream_.write(bytes);
                    outputStream_ = responseOutputStream_;
                }

                try {
                    outputStream_.flush();
                    outputStream_.close();
                } catch (IOException e) {
                    // don't do anything, the response stream has probably been
                    // closed or reset
                }

                outputStream_ = null;
            } catch (IOException e) {
                // don't do anything, the response stream has probably been
                // closed or reset
            }
        }
    }

    public OutputStream getOutputStream()
    throws EngineException {
        ensureOutputStream();

        return outputStream_;
    }

    private void ensureOutputStream()
    throws EngineException {
        if (null == outputStream_) {
            if (null == responseOutputStream_) {
                try {
                    responseOutputStream_ = _getOutputStream();

                    if (contentType_ != null) {
                        String content_type = HttpUtils.extractMimeTypeFromContentType(contentType_);

                        // check if the content type should be gzip encoded
                        if (RifeConfig.engine().getGzipCompression() &&
                            RifeConfig.engine().getGzipCompressionTypes().contains(content_type)) {
                            String accept_encoding = request_.getHeader("Accept-Encoding");
                            if (accept_encoding != null &&
                                accept_encoding.contains("gzip")) {
                                gzipByteOutputStream_ = new ByteArrayOutputStream();
                                gzipOutputStream_ = new GZIPOutputStream(gzipByteOutputStream_);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new ResponseOutputStreamRetrievalErrorException(e);
                }
            }

            if (gzipOutputStream_ != null) {
                outputStream_ = gzipOutputStream_;
            } else {
                outputStream_ = responseOutputStream_;
            }
        }
    }
}
