/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

/**
 * This class describes a single cell of the browse table, as a
 * {@link CrudColumnRenderer} renders it instead of the formatted value.
 * <p>A cell is one of three things: text, which is displayed like the
 * formatted value would have been; a badge, which wraps the text in a small
 * marker that can be styled by name; or markup, which is written into the
 * cell as-is.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CrudEntityOptions#column(String, CrudColumnRenderer)
 * @since 1.10
 */
public final class CrudCell {
    enum Kind {TEXT, BADGE, HTML}

    private final Kind kind_;
    private final String content_;
    private final String style_;

    private CrudCell(Kind kind, String content, String style) {
        kind_ = kind;
        content_ = content;
        style_ = style;
    }

    /**
     * Creates a cell that displays text.
     * <p>The text is encoded like the formatted value would have been,
     * which makes this the way to show a computed value instead of a
     * stored one.
     *
     * @param text the text to display
     * @return the cell
     * @since 1.10
     */
    public static CrudCell text(String text) {
        if (null == text) throw new IllegalArgumentException("text can't be null.");

        return new CrudCell(Kind.TEXT, text, null);
    }

    /**
     * Creates a cell that displays its text as a badge.
     *
     * @param text the text of the badge
     * @return the cell
     * @see #badge(String, String)
     * @since 1.10
     */
    public static CrudCell badge(String text) {
        return badge(text, null);
    }

    /**
     * Creates a cell that displays its text as a badge with its own
     * style.
     * <p>The style becomes part of the class of the badge, so a status
     * {@code draft} renders as
     * {@code <span class="crud-badge crud-badge-draft">}, which your own
     * stylesheet gives a color through {@link CrudAdmin#head(String)}:
     * <pre>.column("status", (article, value) -&gt; CrudCell.badge(value, article.getStatus()))</pre>
     *
     * @param text  the text of the badge
     * @param style the name that the badge can be styled by; or {@code null}
     *              for a badge without a style of its own
     * @return the cell
     * @since 1.10
     */
    public static CrudCell badge(String text, String style) {
        if (null == text) throw new IllegalArgumentException("text can't be null.");
        // the style ends up inside the class attribute of the badge, where
        // anything else would leak into the markup around it
        if (style != null && !style.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("The badge style '" + style + "' isn't usable, use letters, digits, hyphens and underscores.");
        }

        return new CrudCell(Kind.BADGE, text, style);
    }

    /**
     * Creates a cell that writes markup into the browse table as-is.
     * <p>Nothing is encoded, so you have to encode any instance data that
     * you put into the markup yourself, for instance through
     * {@link rife.tools.StringUtils#encodeHtml}. A value that is written
     * without encoding renders whatever it happens to contain.
     *
     * @param html the markup of the cell
     * @return the cell
     * @since 1.10
     */
    public static CrudCell html(String html) {
        if (null == html) throw new IllegalArgumentException("html can't be null.");

        return new CrudCell(Kind.HTML, html, null);
    }

    Kind kind() {
        return kind_;
    }

    String content() {
        return content_;
    }

    String style() {
        return style_;
    }
}
