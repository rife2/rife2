/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import org.xml.sax.Attributes;
import rife.tools.StringUtils;

import java.util.*;

public class Xml2DataTest extends Xml2Data {
    private List<String> elements_;
    private Stack<String> elementStack_;
    private Stack<StringBuilder> characterStack_;

    public List<String> getElements() {
        return elements_;
    }

    public void startDocument() {
        elements_ = new ArrayList<>();
        elementStack_ = new Stack<>();
        characterStack_ = new Stack<>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        elementStack_.push(qName);
        characterStack_.push(new StringBuilder());
    }

    public void endElement(String uri, String localName, String qName) {
        var element_id = StringUtils.join(elementStack_, "-");
        elementStack_.pop();
        var chars = characterStack_.pop().toString().trim();
        elements_.add(element_id + " : " + chars);
    }

    public void characters(char[] ch, int start, int length) {
        var character_data = characterStack_.peek();
        if (character_data != null) {
            character_data.append(String.copyValueOf(ch, start, length));
        }
    }
}
