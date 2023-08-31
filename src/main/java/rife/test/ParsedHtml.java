/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves the text content of a {@link MockResponse} and parses it as HTML.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ParsedHtml {
    private final MockResponse response_;
    private final Document document_;
    private final List<MockForm> forms_ = new ArrayList<>();
    private final List<MockLink> links_ = new ArrayList<>();

    private ParsedHtml(MockResponse response, Document document) {
        response_ = response;
        document_ = document;

        // get all the forms
        var forms = document_.select("form");
        for (var element : forms) {
            var form = new MockForm(response_, element);
            forms_.add(form);
        }

        // get all the links
        var links = document_.select("a[href]");
        for (var element : links) {
            var link = new MockLink(response_, element);
            links_.add(link);
        }
    }

    /**
     * Parses the text content of a {@link MockResponse} object as HTML and
     * returns the result as an instance of {@code ParsedHtml}.
     *
     * @param response the response whose text content will be parsed
     * @return the resulting instance of {@code ParsedHtml}
     * @since 1.0
     */
    public static ParsedHtml parse(MockResponse response) {
        return parse(response, response.getText());
    }

    static ParsedHtml parse(MockResponse response, String text) {
        return new ParsedHtml(response, Jsoup.parse(text));
    }

    /**
     * Retrieves the document that corresponds to the parsed HTML.
     *
     * @return the parsed document
     * @since 1.0
     */
    public Document getDocument() {
        return document_;
    }

    /**
     * Retrieves the text of the {@code title} tag.
     *
     * @return the title
     * @since 1.0
     */
    public String getTitle() {
        var elements = document_.select("title");
        if (0 == elements.size()) {
            return null;
        }

        return elements.get(0).text();
    }

    /**
     * Retrieves the list of all the forms in the HTML document.
     *
     * @return a list with {@link MockForm} instances
     * @see #getFormWithName
     * @see #getFormWithId
     * @since 1.0
     */
    public List<MockForm> getForms() {
        return forms_;
    }

    /**
     * Retrieves the first form in the HTML document with a particular
     * {@code name} attribute.
     *
     * @param name the content of the {@code name} attribute
     * @return the first {@link MockForm} whose {@code name} attribute
     * matches; or
     * <p>{@code null} if no such form could be found
     * @see #getForms
     * @see #getFormWithId
     * @since 1.0
     */
    public MockForm getFormWithName(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty");

        for (var form : forms_) {
            if (name.equals(form.getName())) {
                return form;
            }
        }
        return null;
    }

    /**
     * Retrieves the first form in the HTML document with a particular
     * {@code id} attribute.
     *
     * @param id the content of the {@code id} attribute
     * @return the first {@link MockForm} whose {@code id} attribute
     * matches; or
     * <p>{@code null} if no such form could be found
     * @see #getForms
     * @see #getFormWithName
     * @since 1.0
     */
    public MockForm getFormWithId(String id) {
        if (null == id) throw new IllegalArgumentException("id can't be null");
        if (0 == id.length()) throw new IllegalArgumentException("id can't be empty");

        for (var form : forms_) {
            if (form.getId().equals(id)) {
                return form;
            }
        }
        return null;
    }

    /**
     * Retrieves the list of all the links in the HTML document.
     *
     * @return a list with {@link MockLink} instances
     * @see #getLinkWithName
     * @see #getLinkWithId
     * @see #getLinkWithText
     * @see #getLinkWithImageAlt
     * @see #getLinkWithImageName
     * @since 1.0
     */
    public List<MockLink> getLinks() {
        return links_;
    }

    /**
     * Retrieves the first link in the HTML document with a particular
     * {@code name} attribute.
     *
     * @param name the content of the {@code name} attribute
     * @return the first {@link MockLink} whose {@code name} attribute
     * matches; or
     * <p>{@code null} if no such link could be found
     * @see #getLinks
     * @see #getLinkWithId
     * @see #getLinkWithText
     * @see #getLinkWithImageAlt
     * @see #getLinkWithImageName
     * @since 1.0
     */
    public MockLink getLinkWithName(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty");

        for (var link : links_) {
            if (link.getName().equals(name)) {
                return link;
            }
        }
        return null;
    }

    /**
     * Retrieves the first link in the HTML document with a particular
     * {@code id} attribute.
     *
     * @param id the content of the {@code id} attribute
     * @return the first {@link MockLink} whose {@code id} attribute
     * matches; or
     * <p>{@code null} if no such link could be found
     * @see #getLinks
     * @see #getLinkWithName
     * @see #getLinkWithText
     * @see #getLinkWithImageAlt
     * @see #getLinkWithImageName
     * @since 1.0
     */
    public MockLink getLinkWithId(String id) {
        if (null == id) throw new IllegalArgumentException("id can't be null");
        if (0 == id.length()) throw new IllegalArgumentException("id can't be empty");

        for (var link : links_) {
            if (id.equals(link.getId())) {
                return link;
            }
        }
        return null;
    }

    /**
     * Retrieves the first link in the HTML document that surrounds a particular
     * text.
     *
     * @param text the surrounded text
     * @return the first {@link MockLink} whose surrounded text matches; or
     * <p>{@code null} if no such link could be found
     * @see #getLinks
     * @see #getLinkWithName
     * @see #getLinkWithId
     * @see #getLinkWithText
     * @see #getLinkWithImageName
     * @since 1.0
     */
    public MockLink getLinkWithText(String text) {
        if (null == text) throw new IllegalArgumentException("text can't be null");

        for (var link : links_) {
            if (link.getText() != null &&
                link.getText().equals(text)) {
                return link;
            }
        }
        return null;
    }

    /**
     * Retrieves the first link in the HTML document that surrounds an
     * {@code img} tag with a certain {@code alt} attribute.
     *
     * @param alt the content of the {@code alt} attribute
     * @return the first {@link MockLink} that has an {@code img} tag
     * whose {@code alt} attribute matches; or
     * <p>{@code null} if no such link could be found
     * @see #getLinks
     * @see #getLinkWithName
     * @see #getLinkWithId
     * @see #getLinkWithText
     * @see #getLinkWithImageName
     * @since 1.0
     */
    public MockLink getLinkWithImageAlt(String alt) {
        if (null == alt) throw new IllegalArgumentException("alt can't be null");

        for (var link : links_) {
            var element = link.getElement();
            var children = element.children();
            if (children.size() > 0) {
                for (var child : children) {
                    if ("img".equals(child.nodeName())) {
                        var alt_text = getElementAttribute(child, "alt", null);
                        if (alt_text != null &&
                            alt_text.equals(alt)) {
                            return link;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the first link in the HTML document that surrounds an
     * {@code img} tag with a certain {@code name} attribute.
     *
     * @param name the content of the {@code name} attribute
     * @return the first {@link MockLink} that has an {@code img} tag
     * whose {@code name} attribute matches; or
     * <p>{@code null} if no such link could be found
     * @see #getLinks
     * @see #getLinkWithName
     * @see #getLinkWithId
     * @see #getLinkWithText
     * @see #getLinkWithImageAlt
     * @since 1.0
     */
    public MockLink getLinkWithImageName(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty");

        for (var link : links_) {
            var element = link.getElement();
            var children = element.children();
            if (children.size() > 0) {
                for (var child : children) {
                    if ("img".equals(child.nodeName())) {
                        var alt_text = getElementAttribute(child, "name", null);
                        if (alt_text != null &&
                            alt_text.equals(name)) {
                            return link;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the value of the attribute of an JSoup element.
     *
     * @param element       the element where the attribute should be obtained from
     * @param attributeName the name of the attribute
     * @return the value of the attribute; or
     * <p>{@code null} if no attribute could be found
     * @since 1.0
     */
    public static String getElementAttribute(Element element, String attributeName) {
        return getElementAttribute(element, attributeName, null);
    }

    static String getElementAttribute(Element element, String attributeName, String defaultValue) {
        var attribute = element.attr(attributeName);
        if (attribute.isEmpty()) {
            return defaultValue;
        }
        return attribute;
    }
}
