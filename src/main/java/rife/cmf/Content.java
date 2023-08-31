/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import rife.tools.ExceptionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Contains the information required to store new content data together with
 * additional meta-data.
 * <p>All content is determined by its mime type and the raw data that will be
 * used to load the content. The type of the data is dependent on the mime
 * type.
 * <p>For example, images can be loaded from byte arrays and texts can be
 * loaded from strings. If an unsupported data type is used or the format is
 * incorrect, suitable exceptions will be thrown when the content is stored in
 * the back-end.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Content implements Cloneable {
    private final MimeType mimeType_;
    private Object data_ = null;
    private boolean fragment_ = false;
    private String name_ = null;
    private Map<String, String> attributes_ = null;
    private Map<String, String> properties_ = null;
    private Object cachedLoadedData_ = null;

    /**
     * Creates a new {@code Content} instance with the minimal required
     * arguments.
     *
     * @param mimeType the mime type as which the content will be stored in
     *                 the back-end, note that this doesn't necessarily have to correspond to
     *                 the mime type of the provided data. Image formats can for example be
     *                 automatically detected and converted to the target mime type by image
     *                 loading and conversion libraries.
     * @param data     the data of the content, if this is {@code null},
     *                 empty content should be stored
     * @since 1.0
     */
    public Content(MimeType mimeType, Object data) {
        if (null == mimeType) throw new IllegalArgumentException("mimeType can't be null");

        mimeType_ = mimeType;
        data_ = data;
    }

    /**
     * Retrieves the mime type of the content.
     *
     * @return the mime type of the content
     * @since 1.0
     */
    public MimeType getMimeType() {
        return mimeType_;
    }

    /**
     * Retrieves the data of the content.
     *
     * @return the data of the content
     * @since 1.0
     */
    public Object getData() {
        return data_;
    }

    /**
     * Sets the data of the content.
     *
     * @since 1.4
     */
    public void setData(Object data) {
        data_ = data;
    }

    /**
     * Sets whether the content data is a fragment. A fragment means that it's
     * not a complete document or a file, but rather a small part that is
     * intended to be used within a larger document. For example an HTML
     * snippet. This information is for example important when validating the
     * data.
     *
     * @param fragment {@code true} if the content is a fragment; or
     *                 <p>{@code false} otherwise
     * @return the current {@code Content} instance
     * @see #setFragment(boolean)
     * @see #isFragment()
     * @since 1.0
     */
    public Content fragment(boolean fragment) {
        setFragment(fragment);

        return this;
    }

    /**
     * Sets whether the content data is a fragment.
     *
     * @param fragment {@code true} if the content is a fragment; or
     *                 <p>{@code false} otherwise
     * @see #fragment(boolean)
     * @see #isFragment()
     * @since 1.0
     */
    public void setFragment(boolean fragment) {
        fragment_ = fragment;
    }

    /**
     * Indicates whether the content data is a fragment.
     *
     * @return {@code true} if the content is a fragment; or
     * <p>{@code false} otherwise
     * @see #fragment(boolean)
     * @see #setFragment(boolean)
     * @since 1.0
     */
    public boolean isFragment() {
        return fragment_;
    }

    /**
     * Sets the name of the content.
     *
     * @param name the name
     * @return the current {@code Content} instance
     * @see #setName(String)
     * @see #getName()
     * @see #hasName()
     * @since 1.0
     */
    public Content name(String name) {
        setName(name);

        return this;
    }

    /**
     * Sets the name of the content.
     *
     * @param name the name
     * @see #name(String)
     * @see #getName()
     * @see #hasName()
     * @since 1.0
     */
    public void setName(String name) {
        name_ = name;
    }

    /**
     * Retrieves the name of the content.
     *
     * @return {@code null} if the content has no name; or
     * <p>the name of the content
     * @see #name(String)
     * @see #setName(String)
     * @see #hasName()
     * @since 1.0
     */
    public String getName() {
        return name_;
    }

    /**
     * Indicates whether the content data has a name.
     *
     * @return {@code true} if the content has a name; or
     * <p>{@code false} otherwise
     * @see #name(String)
     * @see #setName(String)
     * @see #getName()
     * @since 1.0
     */
    public boolean hasName() {
        return name_ != null;
    }

    /**
     * Replaces the map of named content attributes.
     * <p>Note that attributes provide information about how to load, convert
     * and transform content into its stored data form. If you want to provide
     * meta information about the content, you should provide it through
     * properties instead.
     *
     * @param attributes the map of named content attributes
     * @return the current {@code Content} instance
     * @see #setAttributes(Map)
     * @see #getAttributes()
     * @see #hasAttributes()
     * @since 1.0
     */
    public Content attributes(Map<String, String> attributes) {
        setAttributes(attributes);

        return this;
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, boolean value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, char value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, byte value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, short value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, int value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, long value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, float value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, double value) {
        return attribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current {@code Content} instance
     * @see #getAttribute(String)
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public Content attribute(String name, String value) {
        if (null == attributes_) {
            attributes_ = new HashMap<>();
        }

        attributes_.put(name, value);

        return this;
    }

    /**
     * Replaces the map of named content attributes.
     *
     * @param attributes the map of named content attributes
     * @see #attributes(Map)
     * @see #getAttributes()
     * @see #hasAttributes()
     * @since 1.0
     */
    public void setAttributes(Map<String, String> attributes) {
        if (null == attributes) {
            attributes_ = null;
            return;
        }

        attributes_ = new HashMap<>(attributes);
    }

    /**
     * Retrieves the map of named content attributes.
     *
     * @return the map of named content attributes; or
     * <p>{@code null} if no attributes are present
     * @see #attributes(Map)
     * @see #setAttributes(Map)
     * @see #hasAttributes()
     * @since 1.0
     */
    public Map<String, String> getAttributes() {
        return attributes_;
    }

    /**
     * Indicates whether named content attributes are present.
     *
     * @return {@code true} if named content attributes are present; or
     * <p>{@code false} otherwise
     * @see #attributes(Map)
     * @see #setAttributes(Map)
     * @see #getAttributes()
     * @since 1.0
     */
    public boolean hasAttributes() {
        return attributes_ != null && attributes_.size() > 0;
    }

    /**
     * Indicates whether a specific named content attribute is present.
     *
     * @param name the name of the attribute
     * @return {@code true} if the name content attribute is present; or
     * <p>{@code false} otherwise
     * @see #getAttribute(String)
     * @since 1.0
     */
    public boolean hasAttribute(String name) {
        if (null == attributes_) {
            return false;
        }

        return attributes_.containsKey(name);
    }

    /**
     * Retrieves the value of a named content attribute.
     *
     * @param name the name of the attribute
     * @return the value of the named content attribute; or
     * <p>{@code null} if no such attribute could be found
     * @see #hasAttribute(String)
     * @since 1.0
     */
    public String getAttribute(String name) {
        if (null == attributes_) {
            return null;
        }

        return attributes_.get(name);
    }

    /**
     * Replaces the content properties.
     * <p>This is also internally used by content formatters to provide
     * additional information about the content that's stored after formatting
     * and transformation. Note that this is not the same as content
     * attributes, who provide infomration about how to format and transform
     * the provided data before storage. The content properties describe the
     * result as it's stored in the back-end.
     *
     * @param properties the content properties
     * @return the current {@code Content} instance
     * @see #setProperties(Map)
     * @see #hasProperties()
     * @see #getProperties()
     * @since 1.0
     */
    public Content properties(Map<String, String> properties) {
        setProperties(properties);

        return this;
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, boolean value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, char value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, byte value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, short value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, int value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, long value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, float value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property that will be converted internally to a
     * {@code String} value.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, double value) {
        return property(name, String.valueOf(value));
    }

    /**
     * Sets a named content property.
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return the current {@code Content} instance
     * @see #getProperty(String)
     * @see #hasProperty(String)
     * @since 1.0
     */
    public Content property(String name, String value) {
        if (null == properties_) {
            properties_ = new HashMap<>();
        }

        properties_.put(name, value);

        return this;
    }

    /**
     * Replaces the content properties.
     *
     * @param properties the content properties
     * @see #properties(Map)
     * @see #hasProperties()
     * @see #getProperties()
     * @since 1.0
     */
    public void setProperties(Map<String, String> properties) {
        if (null == properties) {
            properties_ = null;
            return;
        }

        properties_ = new HashMap<>(properties);
    }

    /**
     * Indicates whether content properties are present
     *
     * @return {@code true} if properties are present; or
     * <p>{@code false} otherwise
     * @see #properties(Map)
     * @see #setProperties(Map)
     * @see #getProperties()
     * @since 1.0
     */
    public boolean hasProperties() {
        return properties_ != null && properties_.size() > 0;
    }

    /**
     * Indicates whether a specific named content property is present.
     *
     * @param name the name of the property
     * @return {@code true} if the name content property is present; or
     * <p>{@code false} otherwise
     * @see #getProperty(String)
     * @since 1.0
     */
    public boolean hasProperty(String name) {
        if (null == properties_) {
            return false;
        }

        return properties_.containsKey(name);
    }

    /**
     * Retrieves the value of a named content property.
     *
     * @param name the name of the property
     * @return the value of the named content property; or
     * <p>{@code null} if no such property could be found
     * @see #hasProperty(String)
     * @since 1.0
     */
    public String getProperty(String name) {
        if (null == properties_) {
            return null;
        }

        return properties_.get(name);
    }

    /**
     * Retrieves the content properties.
     *
     * @return the content properties; or
     * <p>{@code null} if no content properties are present
     * @see #properties(Map)
     * @see #setProperties(Map)
     * @see #hasProperties()
     * @since 1.0
     */
    public Map<String, String> getProperties() {
        return properties_;
    }

    /**
     * Sets the cached loaded data.
     * <p>This is <b>internally</b> used by content loaders to prevent having
     * to load and convert data to the specified mime type several times for
     * the same content. It is for instance very resource intensive to detect
     * an image format, validate the provided raw data and create a generic
     * image instance for further processing. These operations are however
     * required in several different locations in the content handling logic.
     * Storing the result after the first successful loading and simply
     * retrieving it later enhances the speed considerably.
     *
     * @param data the loaded data
     * @return the current {@code Content} instance
     * @see #setCachedLoadedData(Object)
     * @see #hasCachedLoadedData()
     * @see #getCachedLoadedData()
     * @since 1.0
     */
    public Content cachedLoadedData(Object data) {
        setCachedLoadedData(data);

        return this;
    }

    /**
     * Sets the cached loaded data.
     *
     * @param data the loaded data
     * @see #cachedLoadedData(Object)
     * @see #hasCachedLoadedData()
     * @see #getCachedLoadedData()
     * @since 1.0
     */
    public void setCachedLoadedData(Object data) {
        cachedLoadedData_ = data;
    }

    /**
     * Indicates whether cached loaded content data is present.
     *
     * @return {@code true} if cached loaded content data is present; or
     * <p>{@code false} otherwise
     * @see #cachedLoadedData(Object)
     * @see #setCachedLoadedData(Object)
     * @see #getCachedLoadedData()
     * @since 1.0
     */
    public boolean hasCachedLoadedData() {
        return null != cachedLoadedData_;

    }

    /**
     * Retrieves the cached loaded content data.
     *
     * @return the cached loaded content data; or
     * <p>{@code null} if no loaded content data has been cached
     * @see #cachedLoadedData(Object)
     * @see #setCachedLoadedData(Object)
     * @see #hasCachedLoadedData()
     * @since 1.0
     */
    public Object getCachedLoadedData() {
        return cachedLoadedData_;
    }

    /**
     * Simply clones the instance with the default clone method since we
     * want to create a shallow copy
     *
     * @since 1.0
     */
    public Content clone() {
        try {
            return (Content) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.cmf").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }
}
