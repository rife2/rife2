/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.tools.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;

public class InternalString implements CharSequence {
    private CharSequence stringValue_;

    private transient SoftReference<byte[]> mBytesValue_US_ASCII = null;
    private transient SoftReference<byte[]> mBytesValue_ISO_8859_1 = null;
    private transient SoftReference<byte[]> mBytesValue_UTF_8 = null;
    private transient SoftReference<byte[]> mBytesValue_UTF_16 = null;
    private transient SoftReference<byte[]> mBytesValue_UTF_16BE = null;
    private transient SoftReference<byte[]> mBytesValue_UTF_16LE = null;

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
            if (mBytesValue_ISO_8859_1 != null) {
                bytes = mBytesValue_ISO_8859_1.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == mBytesValue_ISO_8859_1) {
                    mBytesValue_ISO_8859_1 = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_8.equals(charsetName)) {
            if (mBytesValue_UTF_8 != null) {
                bytes = mBytesValue_UTF_8.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == mBytesValue_UTF_8) {
                    mBytesValue_UTF_8 = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_US_ASCII.equals(charsetName)) {
            if (mBytesValue_US_ASCII != null) {
                bytes = mBytesValue_US_ASCII.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == mBytesValue_US_ASCII) {
                    mBytesValue_US_ASCII = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_16.equals(charsetName)) {
            if (mBytesValue_UTF_16 != null) {
                bytes = mBytesValue_UTF_16.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == mBytesValue_UTF_16) {
                    mBytesValue_UTF_16 = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_16BE.equals(charsetName)) {
            if (mBytesValue_UTF_16BE != null) {
                bytes = mBytesValue_UTF_16BE.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == mBytesValue_UTF_16BE) {
                    mBytesValue_UTF_16BE = new SoftReference<>(bytes);
                }
            }
        } else if (StringUtils.ENCODING_UTF_16LE.equals(charsetName)) {
            if (mBytesValue_UTF_16LE != null) {
                bytes = mBytesValue_UTF_16LE.get();
            }
            if (null == bytes) {
                bytes = toString().getBytes(charsetName);
                if (null == mBytesValue_UTF_16LE) {
                    mBytesValue_UTF_16LE = new SoftReference<>(bytes);
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
        if (mBytesValue_ISO_8859_1 != null) {
            SoftReference<byte[]> reference = mBytesValue_ISO_8859_1;
            mBytesValue_ISO_8859_1 = null;
            reference.clear();
        }
        if (mBytesValue_UTF_8 != null) {
            SoftReference<byte[]> reference = mBytesValue_UTF_8;
            mBytesValue_UTF_8 = null;
            reference.clear();
        }
        if (mBytesValue_UTF_16 != null) {
            SoftReference<byte[]> reference = mBytesValue_UTF_16;
            mBytesValue_UTF_16 = null;
            reference.clear();
        }
        if (mBytesValue_UTF_16BE != null) {
            SoftReference<byte[]> reference = mBytesValue_UTF_16BE;
            mBytesValue_UTF_16BE = null;
            reference.clear();
        }
        if (mBytesValue_UTF_16LE != null) {
            SoftReference<byte[]> reference = mBytesValue_UTF_16LE;
            mBytesValue_UTF_16LE = null;
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

