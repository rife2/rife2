/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.tools.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;

public class InternalString implements CharSequence {
    private CharSequence stringValue_;

    private transient SoftReference<byte[]> bytesValue_US_ASCII_ = null;
    private transient SoftReference<byte[]> bytesValue_ISO_8859_1_ = null;
    private transient SoftReference<byte[]> bytesValue_UTF_8_ = null;
    private transient SoftReference<byte[]> bytesValue_UTF_16_ = null;
    private transient SoftReference<byte[]> bytesValue_UTF_16BE_ = null;
    private transient SoftReference<byte[]> bytesValue_UTF_16LE_ = null;

    public InternalString(String value) {
        stringValue_ = value;
    }

    public InternalString(CharSequence value) {
        stringValue_ = value;
    }

    public String toString() {
        return stringValue_.toString();
    }

    public byte[] getBytes(String charsetName)
    throws UnsupportedEncodingException {
        byte[] bytes = null;

        if (StringUtils.ENCODING_ISO_8859_1.equals(charsetName)) {
            if (bytesValue_ISO_8859_1_ != null) {
                bytes = bytesValue_ISO_8859_1_.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == bytesValue_ISO_8859_1_) {
                    bytesValue_ISO_8859_1_ = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_8.equals(charsetName)) {
            if (bytesValue_UTF_8_ != null) {
                bytes = bytesValue_UTF_8_.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == bytesValue_UTF_8_) {
                    bytesValue_UTF_8_ = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_US_ASCII.equals(charsetName)) {
            if (bytesValue_US_ASCII_ != null) {
                bytes = bytesValue_US_ASCII_.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == bytesValue_US_ASCII_) {
                    bytesValue_US_ASCII_ = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_16.equals(charsetName)) {
            if (bytesValue_UTF_16_ != null) {
                bytes = bytesValue_UTF_16_.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == bytesValue_UTF_16_) {
                    bytesValue_UTF_16_ = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_16BE.equals(charsetName)) {
            if (bytesValue_UTF_16BE_ != null) {
                bytes = bytesValue_UTF_16BE_.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == bytesValue_UTF_16BE_) {
                    bytesValue_UTF_16BE_ = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_16LE.equals(charsetName)) {
            if (bytesValue_UTF_16LE_ != null) {
                bytes = bytesValue_UTF_16LE_.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == bytesValue_UTF_16LE_) {
                    bytesValue_UTF_16LE_ = new SoftReference<>(bytes);
                }
            }
        } else {
            bytes = toString().getBytes(charsetName);
        }

        return bytes;
    }

    public int length() {
        return stringValue_.length();
    }

    public void append(String value) {
        stringValue_ = stringValue_ + value;
        if (bytesValue_ISO_8859_1_ != null) {
            SoftReference<byte[]> reference = bytesValue_ISO_8859_1_;
            bytesValue_ISO_8859_1_ = null;
            reference.clear();
        }
        if (bytesValue_UTF_8_ != null) {
            SoftReference<byte[]> reference = bytesValue_UTF_8_;
            bytesValue_UTF_8_ = null;
            reference.clear();
        }
        if (bytesValue_UTF_16_ != null) {
            SoftReference<byte[]> reference = bytesValue_UTF_16_;
            bytesValue_UTF_16_ = null;
            reference.clear();
        }
        if (bytesValue_UTF_16BE_ != null) {
            SoftReference<byte[]> reference = bytesValue_UTF_16BE_;
            bytesValue_UTF_16BE_ = null;
            reference.clear();
        }
        if (bytesValue_UTF_16LE_ != null) {
            SoftReference<byte[]> reference = bytesValue_UTF_16LE_;
            bytesValue_UTF_16LE_ = null;
            reference.clear();
        }
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return stringValue_.subSequence(beginIndex, endIndex);
    }

    public char charAt(int index) {
        return stringValue_.charAt(index);
    }
}

