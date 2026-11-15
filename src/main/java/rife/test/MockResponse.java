/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import rife.engine.Response;
import rife.engine.SiteResponse;
import rife.engine.exceptions.EngineException;
import rife.template.Template;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides a {@link Response} implementation that is suitable for testing a
 * web application outside a servlet container.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockResponse extends SiteResponse {
    private static final Pattern STRIP_XHTML_XMLNS = Pattern.compile("html\\s+xmlns=\"[^\"]*\"");

    private final MockConversation mockConversation_;
    private Template template_;
    private final List<Template> eventTemplates_ = Collections.synchronizedList(new ArrayList<>());

    MockResponse(MockConversation conversation, MockRequest request) {
        super(request);

        mockConversation_ = conversation;
    }

    MockConversation getMockConversation() {
        return mockConversation_;
    }

    /**
     * Retrieves the template instance that was printed to the response.
     * <p>This is also the template when only one of its blocks was printed,
     * as {@link rife.engine.Context#printBlock} and {@link
     * rife.engine.Context#printHtmxFragment} do for an htmx request.
     *
     * @return the template instance that was printed to the response; or
     * <p>{@code null} if no template was printed to the response
     * @see #getBytes
     * @see #getText
     * @see #getParsedHtml
     * @since 1.0
     */
    public Template getTemplate() {
        return template_;
    }

    /**
     * Retrieves the content of this response as parsed HTML.
     *
     * @return the response content as parsed HTML
     * @see #getBytes
     * @see #getText
     * @see #getTemplate
     * @since 1.0
     */
    public ParsedHtml getParsedHtml() {
        return ParsedHtml.parse(this);
    }

    /**
     * Retrieves the content of this response as parsed server-sent events
     * (SSE).
     * <p>The text of the response will be interpreted according to the
     * {@code text/event-stream} wire format. Events are separated by blank
     * lines, the multiple {@code data} fields of a single event are joined
     * by line feeds, and the comment lines are captured as well, so that
     * the events that only consist of comments, like keep-alive
     * heartbeats, are represented too.
     *
     * @return the list of events that were sent to this response
     * @see MockEvent
     * @see #getText
     * @since 1.10
     */
    public List<MockEvent> getEvents() {
        var events = new ArrayList<MockEvent>();

        var pending = new MockEvent();
        for (var line : getText().split("\r\n|\r|\n", -1)) {
            if (line.isEmpty()) {
                if (!pending.isEmpty()) {
                    events.add(pending);
                    pending = new MockEvent();
                }
                continue;
            }

            String field;
            String value;
            var colon = line.indexOf(':');
            if (0 == colon) {
                pending.addComment(line.substring(1).startsWith(" ") ? line.substring(2) : line.substring(1));
                continue;
            } else if (colon > 0) {
                field = line.substring(0, colon);
                value = line.substring(colon + 1);
                if (value.startsWith(" ")) {
                    value = value.substring(1);
                }
            } else {
                field = line;
                value = "";
            }

            switch (field) {
                case "event" -> pending.setName(value);
                case "id" -> pending.setId(value);
                case "data" -> pending.addDataLine(value);
                case "retry" -> {
                    try {
                        pending.setRetry(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        // ignore invalid retry values, just like browsers do
                    }
                }
                default -> {
                    // unknown fields are ignored, as prescribed by the SSE spec
                }
            }
        }

        // associate the captured template instances with the parsed events,
        // every sent event corresponds to exactly one event on the wire
        synchronized (eventTemplates_) {
            for (var i = 0; i < events.size() && i < eventTemplates_.size(); i++) {
                events.get(i).setTemplate(eventTemplates_.get(i));
            }
        }

        return events;
    }

    @Override
    protected void sseEventSent(Template template) {
        eventTemplates_.add(template == null ? null : (Template) template.clone());
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a list of DOM nodes.
     * <p>More information about XPath can be found in the <a
     * href="http://www.w3.org/TR/xpath">original specification</a> or in this
     * <a href="http://zvon.org/xxl/XMLTutorial/General/book.html">tutorial</a>.
     *
     * @return the result as a {@code NodeList}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNode(String)
     * @see #xpathString(String)
     * @see #xpathBoolean(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public NodeList xpathNodeSet(String expression)
    throws XPathExpressionException {
        return (NodeList) xpath(expression, XPathConstants.NODESET);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a DOM node.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathString(String)
     * @see #xpathBoolean(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public Node xpathNode(String expression)
    throws XPathExpressionException {
        return (Node) xpath(expression, XPathConstants.NODE);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a string.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathNode(String)
     * @see #xpathBoolean(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public String xpathString(String expression)
    throws XPathExpressionException {
        return (String) xpath(expression, XPathConstants.STRING);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a boolean.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathNode(String)
     * @see #xpathString(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public Boolean xpathBoolean(String expression)
    throws XPathExpressionException {
        return (Boolean) xpath(expression, XPathConstants.BOOLEAN);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a number.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathNode(String)
     * @see #xpathString(String)
     * @see #xpathBoolean(String)
     * @since 1.0
     */
    public Double xpathNumber(String expression)
    throws XPathExpressionException {
        return (Double) xpath(expression, XPathConstants.NUMBER);
    }

    private Object xpath(String expression, QName returnType)
    throws XPathExpressionException {
        var matcher = STRIP_XHTML_XMLNS.matcher(getText());
        var text = matcher.replaceAll("html xmlns=\"\"");
        Reader reader = new StringReader(text);

        var input_source = new InputSource(reader);
        var xpath = XPathFactory.newInstance().newXPath();
        return xpath.evaluate(expression, input_source, returnType);
    }


    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a list of DOM nodes.
     * <p>More information about XPath can be found in the <a
     * href="http://www.w3.org/TR/xpath">original specification</a> or in this
     * <a href="http://zvon.org/xxl/XMLTutorial/General/book.html">tutorial</a>.
     *
     * @return the result as a {@code NodeList}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNode(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathBoolean(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public NodeList xpathNodeSet(String expression, Object context)
    throws XPathExpressionException {
        return (NodeList) xpath(expression, context, XPathConstants.NODESET);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a DOM node.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathBoolean(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public Node xpathNode(String expression, Object context)
    throws XPathExpressionException {
        return (Node) xpath(expression, context, XPathConstants.NODE);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a string.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathNode(String, Object)
     * @see #xpathBoolean(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public String xpathString(String expression, Object context)
    throws XPathExpressionException {
        return (String) xpath(expression, context, XPathConstants.STRING);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a boolean.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathNode(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public Boolean xpathBoolean(String expression, Object context)
    throws XPathExpressionException {
        return (Boolean) xpath(expression, context, XPathConstants.BOOLEAN);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a number.
     *
     * @return the result as a {@code Node}
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathNode(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathBoolean(String, Object)
     * @since 1.0
     */
    public Double xpathNumber(String expression, Object context)
    throws XPathExpressionException {
        return (Double) xpath(expression, context, XPathConstants.NUMBER);
    }

    private Object xpath(String expression, Object context, QName returnType)
    throws XPathExpressionException {
        var xpath = XPathFactory.newInstance().newXPath();
        return xpath.evaluate(expression, context, returnType);
    }

    public void print(Template template)
    throws EngineException {
        template_ = template;

        super.print(template);
    }

    public void printBlock(Template template, String blockId)
    throws EngineException {
        template_ = template;

        super.printBlock(template, blockId);
    }

    public void addCookie(Cookie cookie) {
        super.addCookie(cookie);
        mockConversation_.addCookie(cookie);
    }

    /**
     * Retrieves the list of cookies that have been added in this response.
     *
     * @return the list of added cookies; or
     * <p>an empty list if no cookies have been added
     * @since 1.0
     */
    public List<String> getNewCookieNames() {
        var names = new ArrayList<String>();
        for (var cookie : getNewCookies()) {
            if (!names.contains(cookie.getName())) {
                names.add(cookie.getName());
            }
        }

        return names;
    }
}
