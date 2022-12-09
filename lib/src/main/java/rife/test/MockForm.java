/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import java.util.*;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rife.engine.RequestMethod;
import rife.tools.ArrayUtils;

/**
 * Corresponds to a form in a HTML document after it has been parsed with
 * {@link ParsedHtml#parse}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockForm {
    private final MockResponse response_;
    private final Element element_;
    private final Map<String, String[]> parameters_ = new LinkedHashMap<>();
    private final Map<String, MockFileUpload[]> files_ = new LinkedHashMap<>();

    MockForm(MockResponse response, Element element) {
        assert element != null;

        response_ = response;
        element_ = element;

        var elements_stack = new Stack<Elements>();

        Elements child_elements = element_.children();
        if (child_elements.size() > 0) {
            elements_stack.push(child_elements);
        }

        while (elements_stack.size() > 0) {
            child_elements = elements_stack.pop();

            for (Element child_element : child_elements) {
                var node_name = child_element.nodeName();
                var node_name_attribute = ParsedHtml.getElementAttribute(child_element, "name", null);
                if (node_name_attribute != null) {
                    switch (node_name) {
                        case "input" -> {
                            var input_type = ParsedHtml.getElementAttribute(child_element, "type", null);
                            if (input_type != null) {
                                var input_value = ParsedHtml.getElementAttribute(child_element, "value", null);
                                if ("text".equals(input_type) ||
                                    "password".equals(input_type) ||
                                    "hidden".equals(input_type)) {
                                    addParameterValue(node_name_attribute, input_value);
                                } else {
                                    var input_checked = ParsedHtml.getElementAttribute(child_element, "checked", null) != null;
                                    if (null == input_value) {
                                        input_value = "on";
                                    }

                                    if ("checkbox".equals(input_type)) {
                                        if (input_checked) {
                                            addParameterValue(node_name_attribute, input_value);
                                        }
                                    } else if ("radio".equals(input_type)) {
                                        if (input_checked) {
                                            setParameter(node_name_attribute, input_value);
                                        }
                                    }
                                }
                            }
                        }
                        case "textarea" -> {
                            var value = child_element.text();
                            addParameterValue(node_name_attribute, value);
                        }
                        case "select" -> {
                            List<String> selected_options = new ArrayList<>();
                            var select_multiple = ParsedHtml.getElementAttribute(child_element, "multiple", null) != null;
                            String first_option_value = null;

                            // go over all the select child elements to find the options tags
                            var select_child_elements = child_element.children();
                            for (Element select_child_element : select_child_elements) {
                                var select_element_name = select_child_element.nodeName();

                                // process each select option
                                if ("option".equals(select_element_name)) {
                                    // obtain the option value
                                    var option_value = ParsedHtml.getElementAttribute(select_child_element, "value", null);
                                    if (null == option_value) {
                                        option_value = select_child_element.text();
                                    }

                                    // remember the first option value
                                    if (null == first_option_value) {
                                        first_option_value = option_value;
                                    }

                                    // select the indicated options
                                    var option_selected = ParsedHtml.getElementAttribute(select_child_element, "selected", null) != null;
                                    if (option_selected) {
                                        if (!select_multiple) {
                                            selected_options.clear();
                                        }

                                        selected_options.add(option_value);
                                    }
                                }
                            }

                            // if no options were selected and the select is not multiple,
                            // the first option should be automatically selected
                            if (0 == selected_options.size() &&
                                !select_multiple &&
                                select_child_elements.size() > 0) {
                                selected_options.add(first_option_value);
                            }

                            // add the selected values to the parameters
                            for (var value : selected_options) {
                                addParameterValue(node_name_attribute, value);
                            }
                        }
                    }
                }

                var planned_child_nodes = child_element.children();
                if (planned_child_nodes.size() > 0) {
                    elements_stack.push(planned_child_nodes);
                }
            }
        }
    }

    /**
     * Retrieves the JSoup element that this form corresponds to.
     *
     * @return the corresponding JSoup element
     * @since 1.0
     */
    public Element getElement() {
        return element_;
    }

    /**
     * Creates a new {@link MockRequest} that contains the method, the
     * parameters and the files of this form.
     *
     * @return the created <code>MockRequest</code>
     * @since 1.0
     */
    public MockRequest getRequest() {
        return new MockRequest()
            .method(RequestMethod.valueOf(getMethod()))
            .parameters(parameters_)
            .files(files_);
    }

    /**
     * Submit this form with its current parameters and files; and returns the
     * response.
     *
     * @return the resulting {@link MockResponse}
     * @since 1.0
     */
    public MockResponse submit() {
        return response_.getMockConversation().doRequest(getAction(), getRequest());
    }

    private void addParameterValue(String name, String value) {
        if (null == value) {
            value = "";
        }

        var values = parameters_.get(name);
        if (null == values) {
            values = new String[]{value};
        } else {
            values = ArrayUtils.join(values, value);
        }

        parameters_.put(name, values);
    }

    /**
     * Retrieves all the parameters of this form.
     *
     * @return a <code>Map</code> of the parameters with the names as the keys
     * and their value arrays as the values
     * @see #getParameterNames
     * @see #hasParameter
     * @see #getParameterValue
     * @see #getParameterValues
     * @see #setParameter(String, String[])
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public Map<String, String[]> getParameters() {
        return parameters_;
    }

    /**
     * Retrieves all the parameter names of this form.
     *
     * @return a <code>Collection</code> of the parameter names
     * @see #getParameters
     * @see #hasParameter
     * @see #getParameterValue
     * @see #getParameterValues
     * @see #setParameter(String, String[])
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public Collection<String> getParameterNames() {
        return parameters_.keySet();
    }

    /**
     * Checks whether a named parameter is present in this form.
     *
     * @param name the name of the parameter to check
     * @return <code>true</code> if the parameter is present; or
     * <p><code>false</code> otherwise
     * @see #getParameters
     * @see #getParameterNames
     * @see #getParameterValue
     * @see #getParameterValues
     * @see #setParameter(String, String[])
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public boolean hasParameter(String name) {
        return parameters_.containsKey(name);
    }

    /**
     * Retrieves the first value of a parameter in this form.
     *
     * @param name the name of the parameter
     * @return the first value of the parameter; or
     * <p><code>null</code> if no such parameter could be found
     * @see #getParameters
     * @see #getParameterNames
     * @see #hasParameter
     * @see #getParameterValues
     * @see #setParameter(String, String[])
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public String getParameterValue(String name) {
        var values = getParameterValues(name);
        if (null == values ||
            0 == values.length) {
            return null;
        }

        return values[0];
    }

    /**
     * Retrieves the values of a parameter in this form.
     *
     * @param name the name of the parameter
     * @return the values of the parameter; or
     * <p><code>null</code> if no such parameter could be found
     * @see #getParameters
     * @see #getParameterNames
     * @see #hasParameter
     * @see #getParameterValue
     * @see #setParameter(String, String[])
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public String[] getParameterValues(String name) {
        return parameters_.get(name);
    }

    /**
     * Sets a parameter in this form.
     *
     * @param name  the name of the parameter
     * @param value the value of the parameter
     * @see #getParameters
     * @see #getParameterNames
     * @see #hasParameter
     * @see #getParameterValue
     * @see #getParameterValues
     * @see #setParameter(String, String[])
     * @since 1.0
     */
    public void setParameter(String name, String value) {
        if (null == name ||
            null == value) {
            return;
        }

        parameters_.put(name, new String[]{value});
    }

    /**
     * Sets a parameter in this form.
     *
     * @param name  the name of the parameter
     * @param value the value of the parameter
     * @return this <code>MockForm</code> instance
     * @see #getParameters
     * @see #getParameterNames
     * @see #hasParameter
     * @see #getParameterValue
     * @see #getParameterValues
     * @see #setParameter(String, String[])
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public MockForm parameter(String name, String value) {
        setParameter(name, value);

        return this;
    }

    /**
     * Sets a parameter in this form.
     *
     * @param name   the name of the parameter
     * @param values the value array of the parameter
     * @see #getParameters
     * @see #getParameterNames
     * @see #hasParameter
     * @see #getParameterValue
     * @see #getParameterValues
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public void setParameter(String name, String[] values) {
        if (null == name) {
            return;
        }

        if (null == values) {
            parameters_.remove(name);
        } else {
            parameters_.put(name, values);
        }
    }

    /**
     * Sets a parameter in this form.
     *
     * @param name   the name of the parameter
     * @param values the value array of the parameter
     * @return this <code>MockForm</code> instance
     * @see #getParameters
     * @see #getParameterNames
     * @see #hasParameter
     * @see #getParameterValue
     * @see #getParameterValues
     * @see #setParameter(String, String[])
     * @see #setParameter(String, String)
     * @since 1.0
     */
    public MockForm parameter(String name, String[] values) {
        setParameter(name, values);

        return this;
    }

    /**
     * Sets a file in this form.
     *
     * @param name the parameter name of the file
     * @param file the file specification that will be uploaded
     * @see #setFiles(String, MockFileUpload[])
     * @since 1.0
     */
    public void setFile(String name, MockFileUpload file) {
        if (null == name ||
            null == file) {
            return;
        }

        files_.put(name, new MockFileUpload[]{file});
    }

    /**
     * Sets a file in this form.
     *
     * @param name the parameter name of the file
     * @param file the file specification that will be uploaded
     * @return this <code>MockForm</code> instance
     * @see #setFiles(String, MockFileUpload[])
     * @since 1.0
     */
    public MockForm file(String name, MockFileUpload file) {
        setFile(name, file);

        return this;
    }

    /**
     * Sets files in this request.
     *
     * @param name  the parameter name of the file
     * @param files the file specifications that will be uploaded
     * @see #setFile(String, MockFileUpload)
     * @since 1.0
     */
    public void setFiles(String name, MockFileUpload[] files) {
        if (null == name) {
            return;
        }

        if (null == files) {
            files_.remove(name);
        } else {
            files_.put(name, files);
        }
    }

    /**
     * Sets files in this request.
     *
     * @param name  the parameter name of the file
     * @param files the file specifications that will be uploaded
     * @return this <code>MockForm</code> instance
     * @see #setFile(String, MockFileUpload)
     * @since 1.0
     */
    public MockForm files(String name, MockFileUpload[] files) {
        setFiles(name, files);

        return this;
    }

    /**
     * Retrieves the content of this form's <code>id</code> attribute.
     *
     * @return the content of the <code>id</code> attribute; or
     * <p>null if no such attribute could be found
     * @since 1.0
     */
    public String getId() {
        return getAttribute("id");
    }

    /**
     * Retrieves the content of this form's <code>class</code> attribute.
     *
     * @return the content of the <code>class</code> attribute; or
     * <p>null if no such attribute could be found
     * @since 1.0
     */
    public String getClassName() {
        return getAttribute("class");
    }

    /**
     * Retrieves the content of this form's <code>title</code> attribute.
     *
     * @return the content of the <code>title</code> attribute; or
     * <p>null if no such attribute could be found
     * @since 1.0
     */
    public String getTitle() {
        return getAttribute("title");
    }

    /**
     * Retrieves the content of this form's <code>action</code> attribute.
     *
     * @return the content of the <code>action</code> attribute; or
     * <p>null if no such attribute could be found
     * @since 1.0
     */
    public String getAction() {
        return getAttribute("action");
    }

    /**
     * Retrieves the content of this form's <code>method</code> attribute.
     *
     * @return the content of the <code>method</code> attribute; or
     * <p>null if no such attribute could be found
     * @since 1.0
     */
    public String getMethod() {
        return getAttribute("method").toUpperCase();
    }

    /**
     * Retrieves the content of this form's <code>name</code> attribute.
     *
     * @return the content of the <code>name</code> attribute; or
     * <p>null if no such attribute could be found
     * @since 1.0
     */
    public String getName() {
        return getAttribute("name");
    }

    private String getAttribute(String attributeName) {
        return ParsedHtml.getElementAttribute(element_, attributeName, null);
    }
}

