/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import rife.config.exceptions.ConfigErrorException;
import rife.ioc.HierarchicalProperties;
import rife.ioc.exceptions.PropertyValueException;
import rife.selector.NameSelector;
import rife.tools.Convert;
import rife.xml.Xml2Data;
import rife.xml.exceptions.XmlErrorException;

import java.util.*;

import org.xml.sax.Attributes;

class Xml2Config extends Xml2Data {
    private final HierarchicalProperties properties_;
    private final Map<String, String> parameters_;
    private final List<String> finalParameters_;
    private final Map<String, List<String>> lists_;
    private final List<String> finalLists_;

    private StringBuilder characterDataStack_ = null;

    private String currentListName_ = null;
    private String currentParameterName_ = null;

    Xml2Config(HierarchicalProperties properties, Map<String, String> parameters, List<String> finalParameters, Map<String, List<String>> lists, List<String> finalLists) {
        if (properties == null) {
            properties = HierarchicalProperties.createSystemInstance();
        }
        properties_ = properties;
        parameters_ = Objects.requireNonNullElseGet(parameters, HashMap::new);
        finalParameters_ = Objects.requireNonNullElseGet(finalParameters, ArrayList::new);
        lists_ = Objects.requireNonNullElseGet(lists, HashMap::new);
        finalLists_ = Objects.requireNonNullElseGet(finalLists, ArrayList::new);
    }

    public Map<String, String> getParameters() {
        return parameters_;
    }

    public List<String> getFinalParameters() {
        return finalParameters_;
    }

    public Map<String, List<String>> getLists() {
        return lists_;
    }

    public List<String> getFinalLists() {
        return finalLists_;
    }

    public void startDocument() {
        characterDataStack_ = null;
        currentListName_ = null;
        currentParameterName_ = null;
    }

    public void endDocument() {
        characterDataStack_ = null;
        currentListName_ = null;
        currentParameterName_ = null;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        if (qName.equals("param") ||
            qName.equals("item") ||
            qName.equals("include")) {
            characterDataStack_ = new StringBuilder();
        }

        switch (qName) {
            case "config", "item", "include" -> {
                // do nothing
            }
            case "list" -> {
                var name = atts.getValue("name");

                if (!finalLists_.contains(name)) {
                    currentListName_ = name;
                    lists_.put(name, new ArrayList<>());

                    var final_attribute = atts.getValue("final");
                    if (final_attribute != null &&
                        (final_attribute.equals("1") ||
                            final_attribute.equals("t") ||
                            final_attribute.equals("true"))) {
                        finalLists_.add(name);
                    }
                }
            }
            case "param" -> {
                var name = atts.getValue("name");

                if (!finalParameters_.contains(name)) {
                    currentParameterName_ = name;

                    var final_attribute = atts.getValue("final");
                    if (final_attribute != null &&
                        Convert.toBoolean(final_attribute, false)) {
                        finalParameters_.add(name);
                    }
                }
            }
            case "value" -> {
                var parameter_name = atts.getValue("name");

                if (parameters_.containsKey(parameter_name)) {
                    characterDataStack_.append(parameters_.get(parameter_name));
                }
            }
            case "property" -> {
                var property_name = atts.getValue("name");

                if (properties_ != null && properties_.contains(property_name)) {
                    try {
                        characterDataStack_.append(properties_.get(property_name).getValueString());
                    } catch (PropertyValueException e) {
                        throw new XmlErrorException("Error while obtain the String value of property '" + property_name + "'.", e);
                    }
                }
            }
            case "selector" -> {
                var selector_name = atts.getValue("class");

                Class<?> klass = null;
                try {
                    // try a complete classname
                    klass = Class.forName(selector_name);
                } catch (ClassNotFoundException ignored) {
                }
                // try this package's prefix if the class couldn't be found
                if (null == klass) {
                    try {
                        klass = Class.forName(NameSelector.class.getPackage().getName() + "." + selector_name);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
                // if the class was found, create an instance
                if (klass != null) {
                    try {
                        Object instance = klass.getDeclaredConstructor().newInstance();
                        if (instance instanceof NameSelector selector) {
                            characterDataStack_.append(selector.getActiveName());
                        }
                    } catch (Exception e) {
                        throw new XmlErrorException("Error while obtain the name of selector '" + selector_name + "'.", e);
                    }
                }
            }
            default -> throw new XmlErrorException("Unsupported element name '" + qName + "'.");
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        switch (qName) {
            case "config", "value" -> {
                // do nothing
            }
            case "param" -> {
                if (currentParameterName_ != null) {
                    var parameter_name = currentParameterName_;
                    parameters_.put(parameter_name, characterDataStack_.toString());
                    currentParameterName_ = null;
                }
                characterDataStack_ = null;
            }
            case "list" -> {
                currentListName_ = null;
            }
            case "item" -> {
                if (currentListName_ != null) {
                    lists_.get(currentListName_).add(characterDataStack_.toString());
                    characterDataStack_ = null;
                }
            }
            case "include" -> {
                var included_file = characterDataStack_.toString();

                try {
                    Config.fromXmlResource(included_file, getResourceFinder(), properties_, parameters_, finalParameters_, lists_, finalLists_);
                } catch (ConfigErrorException e) {
                    throw new XmlErrorException("Error while processing the included config file '" + included_file + "'.", e);
                }
            }
        }
    }

    public void characters(char[] ch, int start, int length) {
        if (characterDataStack_ != null &&
            length > 0) {
            characterDataStack_.append(String.copyValueOf(ch, start, length));
        }
    }
}