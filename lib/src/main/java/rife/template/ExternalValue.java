/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.tools.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.ArrayList;

public class ExternalValue extends ArrayList<CharSequence> implements CharSequence {
    @Serial private static final long serialVersionUID = -7361025452353622788L;

    private int size_ = 0;

    public ExternalValue() {
        super();
    }

    public int length() {
        return toString().length();
    }

    public char charAt(int index) {
        return toString().charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    public void append(CharSequence value) {
        size_ += value.length();
        add(value);
    }

    public String toString() {
        var result = new StringBuilder(size_);

        for (CharSequence charsequence : this) {
            // force JDK 1.4 compatibility by preventing that append(CharSequence) is used
            result.append((Object) charsequence);
        }

        return result.toString();
    }

    public void write(OutputStream out, String charsetName)
    throws IOException {
        if (null == charsetName) {
            charsetName = StringUtils.ENCODING_UTF_8;
        }

        for (var char_sequence : this) {
            if (char_sequence instanceof rife.template.InternalString) {
                out.write(((InternalString) char_sequence).getBytes(charsetName));
            } else if (char_sequence instanceof java.lang.String) {
                out.write(((String) char_sequence).getBytes(charsetName));
            }
        }
    }
}

