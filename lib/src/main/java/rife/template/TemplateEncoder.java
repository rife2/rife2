/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

/**
 * Encodes <code>String</code>s into a corresponding template output format,
 * so they will be displayed correctly in the resulting file. For example, a
 * <code>TemplateEncoder</code> for a template file will {@link #encode encode}
 * <code>&gt;</code> as <code>&amp;gt</code>.
 *
 * @author Keith Lea (keith[remove] at cs dot oswego dot edu)
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.0
 */
public interface TemplateEncoder {
    /**
     * Encodes the given value, returning a string which contains only valid
     * characters and represents the given <code>value</code> correctly in the
     * output format.
     * <p>For example, an HTML template's encoder will encode
     * <code>&gt;</code> as <code>&amp;gt;</code>.
     *
     * @param value a string
     * @return an encoded version of the given string
     * @since 1.0
     */
    public String encode(String value);

    /**
     * Encodes the given value in a looser fashion than {@link #encode}'s,
     * only converting patterns which are explicitly not allowed by the output
     * format, but not guaranteeing that the output value exactly represents
     * the given <code>value</code> in the output format.
     * <p>For example, an HTML template's encoder will encode some Unicode
     * characters to corresponding XML entities (such as
     * <code>&amp;eacute;</code>) when this method is called but not encode
     * <code>&lt;</code> or <code>&amp;</code>.
     *
     * @param value a string
     * @return a loosely encoded version of the given <code>value</code>
     * @since 1.0
     */
    public String encodeDefensive(String value);
}


