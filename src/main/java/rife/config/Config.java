/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import rife.config.exceptions.*;
import rife.ioc.HierarchicalProperties;
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderDirectories;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import rife.resources.ResourceFinder;
import rife.tools.exceptions.ConversionException;
import rife.tools.exceptions.FileUtilsErrorException;
import rife.tools.exceptions.SerializationUtilsErrorException;
import rife.xml.exceptions.XmlErrorException;

import java.io.File;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The {@code Config} class manages the configuration of parameters and lists.
 * <p>
 * It is capable of being loaded from and stored into XML files or the JDK {@link Preferences}
 * mechanism.
 * <p>
 * The XML schema includes additional tags that can be used when manually writing config XML files.
 * These are all the supported XML tags:
 * <ul>
 *     <li><code>&lt;param name="param.name"&gt;value&lt;/param&gt;</code><br>sets a parameter to the provided value, optionally takes a <code>final</code> attribute</li>
 *     <li><code>&lt;list name="list.name"&gt;&lt;/list&gt;</code><br>creates a named list with <code>item</code> tags, optionally takes a <code>final</code> attribute</li>
 *     <li><code>&lt;item&gt;value&lt;/item&gt;</code><br>adds an item to the <code>list</code> it's declared in</li>
 *     <li><code>&lt;property name="prop.name"/&gt;</code><br>will be replaced with the property value from the hierarchical properties</li>
 *     <li><code>&lt;value name="param.name"/&gt;</code><br>will be replaced with the previously declared parameter value of that name</li>
 *     <li><code>&lt;selector class="package.name"/&gt;</code><br>will be replaced with result of the provided <code>NameSelector</code> class</li>
 *     <li><code>&lt;include&gt;config/name.xml&lt;/include&gt;</code><br>includes another configuration that's looked up through the same resource finder</li>
 * </ul>
 * <p>
 * Here's an example of a complete config XML file:
 * <pre> &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 * &lt;!DOCTYPE config SYSTEM &quot;/dtd/config.dtd&quot;&gt;
 * &lt;config&gt;
 *     &lt;list name=&quot;list1&quot;&gt;
 *         &lt;item&gt;item1&lt;/item&gt;
 *         &lt;item&gt;item2:&lt;property name=&quot;prop.name&quot;/&gt;&lt;/item&gt;
 *     &lt;/list&gt;
 *     &lt;list name=&quot;list2&quot; final=&quot;true&quot;&gt;
 *         &lt;item&gt;item3&lt;/item&gt;
 *         &lt;item&gt;item4&lt;/item&gt;
 *     &lt;/list&gt;
 *     &lt;param name=&quot;param1&quot;&gt;5133&lt;/param&gt;
 *     &lt;param name=&quot;param2&quot;&gt;astring&lt;value name=&quot;param1&quot;/&gt;&lt;/param&gt;
 *     &lt;param name=&quot;param3&quot; final=&quot;true&quot;&gt;value&lt;/param&gt;
 *     &lt;include&gt;xml/config-&lt;selector class=&quot;NameSelectorHostname&quot;/&gt;.xml&lt;/include&gt;
 * &lt;/config&gt;</pre>
 * <p>
 * Two special configuration parameters can be used in the XML to indicate where config
 * should be stored to {@link Preferences}.
 * <p>
 * If both of these are specified, then the user one will take precedence:
 * <ul>
 *     <li><code>config.preferences.user</code></li>
 *     <li><code>config.preferences.system</code></li>
 * </ul>
 * <p>
 * For example:
 * <pre> &lt;config&gt;
 *   &lt;param name=&quot;config.preferences.user&quot;&gt;/myapplication&lt;/param&gt;
 *   &lt;param name=&quot;default.language&quot;&gt;en&lt;/param&gt;
 * &lt;/config&gt;</pre>
 * <p>
 * When loading this config file from a resource that's not writable, changes can then still
 * be saved and stored in {@code Preferences} using the {@link #storeToPreferences()} method.
 * <p>
 * As long as the special preferences parameter is part of the configuration,
 * parameters and lists will be retrieved from the preferences node instead
 * of from the static configuration file. If the preferences node doesn't exist
 * or the parameter or list doesn't exist in the preferences node, then the
 * static config value will be returned instead.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.6.0
 */
public class Config implements Cloneable {
    public final static String PARAMETER_PREFERENCES_USER = "config.preferences.user";
    public final static String PARAMETER_PREFERENCES_SYSTEM = "config.preferences.system";

    private final ReadWriteLock lock_ = new ReentrantReadWriteLock();
    private final Lock readLock_ = lock_.readLock();
    private final Lock writeLock_ = lock_.writeLock();

    private File xmlFile_;

    private Map<String, String> parameters_;
    private List<String> finalParameters_;
    private Map<String, List<String>> lists_;
    private List<String> finalLists_;

    /**
     * Creates a new {@code Config} instance.
     *
     * @since 1.6.0
     */
    public Config() {
        xmlFile_ = null;
        parameters_ = new HashMap<>();
        finalParameters_ = new ArrayList<>();
        lists_ = new HashMap<>();
        finalLists_ = new ArrayList<>();
    }

    private Config(File xmlFile, Map<String, String> parameters, List<String> finalParameters, Map<String, List<String>> lists, List<String> finalLists) {
        parameters_ = parameters;
        finalParameters_ = finalParameters;
        lists_ = lists;
        finalLists_ = finalLists;
        xmlFile_ = xmlFile;
    }

    /**
     * Creates a new {@code Config} instance that is parsed from an XML file.
     * <p>
     * The {@link HierarchicalProperties#createSystemInstance() system hierarchical properties} will be used.
     *
     * @param file the XML file to parse
     * @return the resulting config instance
     * @throws ConfigErrorException when an error occurred during parsing the XML file
     * @since 1.6.0
     */
    public static Config fromXmlFile(File file)
    throws ConfigErrorException {
        return fromXmlFile(file, null);
    }

    /**
     * Creates a new {@code Config} instance that is parsed from an XML file.
     *
     * @param file       the XML file to parse
     * @param properties the hierarchical properties to use
     * @return the resulting config instance
     * @throws ConfigErrorException when an error occurred during parsing the XML file
     * @since 1.6.0
     */
    public static Config fromXmlFile(File file, HierarchicalProperties properties)
    throws ConfigErrorException {
        if (null == file) throw new IllegalArgumentException("file can't be null.");
        return fromXmlResource(file.getAbsolutePath(), new ResourceFinderDirectories(new File(System.getProperty("user.dir")), new File("/")), properties);
    }

    /**
     * Creates a new {@code Config} instance that is parsed from an XML resource.
     * <p>
     * The {@link ResourceFinderClasspath#instance() class path resource finder} will be used.
     *
     * @param resourceName the name of the resource to parse
     * @param properties   the hierarchical properties to use
     * @return the resulting config instance
     * @throws ConfigErrorException when an error occurred during parsing the XML resource
     * @since 1.6.0
     */
    public static Config fromXmlResource(String resourceName, HierarchicalProperties properties)
    throws ConfigErrorException {
        return fromXmlResource(resourceName, ResourceFinderClasspath.instance(), properties);
    }

    /**
     * Creates a new {@code Config} instance that is parsed from an XML resource.
     * <p>
     * The {@link HierarchicalProperties#createSystemInstance() system hierarchical properties} will be used.
     *
     * @param resourceName   the name of the resource to parse
     * @param resourceFinder the resource finder to use
     * @return the resulting config instance
     * @throws ConfigErrorException when an error occurred during parsing the XML resource
     * @since 1.6.0
     */
    public static Config fromXmlResource(String resourceName, ResourceFinder resourceFinder)
    throws ConfigErrorException {
        return fromXmlResource(resourceName, resourceFinder, null);
    }

    /**
     * Creates a new {@code Config} instance that is parsed from an XML resource.
     *
     * @param resourceName   the name of the resource to parse
     * @param properties     the hierarchical properties to use
     * @param resourceFinder the resource finder to use
     * @return the resulting config instance
     * @throws ConfigErrorException when an error occurred during parsing the XML resource
     * @since 1.6.0
     */
    public static Config fromXmlResource(String resourceName, ResourceFinder resourceFinder, HierarchicalProperties properties)
    throws ConfigErrorException {
        return fromXmlResource(resourceName, resourceFinder, properties, null, null, null, null);
    }

    static Config fromXmlResource(String resourceName, ResourceFinder resourceFinder, HierarchicalProperties properties, Map<String, String> parameters, List<String> finalParameters, Map<String, List<String>> lists, List<String> finalLists)
    throws ConfigErrorException {
        if (null == resourceName) throw new IllegalArgumentException("resourceName can't be null.");
        if (0 == resourceName.length()) throw new IllegalArgumentException("resourceName can't be empty.");
        if (null == resourceFinder) throw new IllegalArgumentException("resourceFinder can't be null.");

        var config_resource = resourceFinder.getResource(resourceName);
        if (null == config_resource) {
            throw new ConfigResourceNotFoundException(resourceName);
        }

        var xml_config = new Xml2Config(properties, parameters, finalParameters, lists, finalLists);
        try {
            var content = resourceFinder.getContent(resourceName, StandardCharsets.UTF_8.name());
            xml_config.processXml(content, resourceFinder);

            return new Config(new File(URLDecoder.decode(config_resource.getPath(), StandardCharsets.UTF_8)),
                xml_config.getParameters(), xml_config.getFinalParameters(),
                xml_config.getLists(), xml_config.getFinalLists());
        } catch (ResourceFinderErrorException | XmlErrorException e) {
            throw new InitializationErrorException(resourceName, e);
        }
    }

    /**
     * Retrieves the XML file that is used for this config instance.
     * <p>
     * This can be {@code null} if the config instance wasn't parsed from an
     * XML file and the file wasn't explicitly set.
     *
     * @return the XML file used for this config instance; or
     * {@code null} if no XML file is used
     * @since 1.6.0
     */
    public File getXmlFile() {
        readLock_.lock();
        try {
            return xmlFile_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Set the XML file that should be used for this config instance.
     * <p>
     * This method simply changes the XML file, it doesn't perform storage
     * or any other file-related operations.
     *
     * @param file the XML file to use
     * @return this config instance
     * @since 1.6.0
     */
    public Config xmlFile(File file) {
        writeLock_.lock();
        try {
            xmlFile_ = file;
        } finally {
            writeLock_.unlock();
        }
        return this;
    }

    private boolean isPreferencesParameter(String parameter) {
        if (null == parameter) {
            return false;
        }

        return parameter.equals(PARAMETER_PREFERENCES_SYSTEM) ||
            parameter.equals(PARAMETER_PREFERENCES_USER);
    }

    /**
     * Sets the {@link Preferences} node that should be used for storing
     * this config data.
     *
     * @param node the preferences node to use
     * @return this config instance
     * @since 1.6.0
     */
    public Config preferencesNode(Preferences node) {
        if (null == node) throw new IllegalArgumentException("node can't be null.");

        writeLock_.lock();
        try {
            if (node.isUserNode()) {
                parameter(PARAMETER_PREFERENCES_USER, node.absolutePath());
                removeParameter(PARAMETER_PREFERENCES_SYSTEM);
            } else {
                removeParameter(PARAMETER_PREFERENCES_USER);
                parameter(PARAMETER_PREFERENCES_SYSTEM, node.absolutePath());
            }
        } finally {
            writeLock_.unlock();
        }

        return this;
    }

    /**
     * Indicates whether this config instance is using a {@link Preferences} node.
     *
     * @return {@code true} if this config instance is using a preferences node; or
     * {@code false} otherwise
     * @since 1.6.0
     */
    public boolean hasPreferencesNode() {
        readLock_.lock();
        try {
            return parameters_.containsKey(PARAMETER_PREFERENCES_USER) ||
                parameters_.containsKey(PARAMETER_PREFERENCES_SYSTEM);
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Retrieves the {@link Preferences} node that is used by this config instance.
     *
     * @return the used preferences node; or {@code null} if no preferences node is used
     * @since 1.6.0
     */
    public Preferences getPreferencesNode() {
        readLock_.lock();
        try {
            if (parameters_.containsKey(PARAMETER_PREFERENCES_USER)) {
                return Preferences.userRoot().node(getString(PARAMETER_PREFERENCES_USER));
            }

            if (parameters_.containsKey(PARAMETER_PREFERENCES_SYSTEM)) {
                return Preferences.systemRoot().node(getString(PARAMETER_PREFERENCES_SYSTEM));
            }
        } finally {
            readLock_.unlock();
        }

        return null;
    }

    /**
     * Returns whether a parameter exists.
     *
     * @param parameter the name of the parameter
     * @return {@code true} of the parameter exists; or {@code false} otherwise
     * @since 1.6.0
     */
    public boolean hasParameter(String parameter) {
        if (null == parameter || 0 == parameter.length()) {
            return false;
        }

        readLock_.lock();
        try {
            if (parameters_.containsKey(parameter)) {
                return true;
            }

            if (!isPreferencesParameter(parameter) &&
                hasPreferencesNode()) {
                var preferences = getPreferencesNode();

                return preferences != null &&
                    preferences.get(parameter, null) != null;
            }
        } finally {
            readLock_.unlock();
        }

        return false;
    }

    /**
     * Indicates whether a parameter is final.
     * <p>
     * A final parameter can not be changed.
     *
     * @param parameter the name of the parameter
     * @return {@code true} if the parameter is final; or {@code false} otherwise
     * @since 1.6.0
     */
    public boolean isFinalParameter(String parameter) {
        if (null == parameter || 0 == parameter.length()) {
            return false;
        }

        readLock_.lock();
        try {
            return finalParameters_.contains(parameter);
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Returns the number of parameters that are present.
     *
     * @return the number of parameters
     * @since 1.6.0
     */
    public int countParameters() {
        readLock_.lock();
        try {
            var result = parameters_.size();

            if (hasPreferencesNode()) {
                var preferences = getPreferencesNode();

                if (preferences != null) {
                    try {
                        String[] keys = preferences.keys();

                        for (var key : keys) {
                            if (!parameters_.containsKey(key)) {
                                result++;
                            }
                        }
                    } catch (BackingStoreException e) {
                        // that's ok, don't handle the preferences node
                    }
                }
            }

            return result;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Retrieves the string value of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the string value; or {@code null} if the parameter doesn't exist
     * @since 1.6.0
     */
    public String getString(String parameter) {
        return getString(parameter, null);
    }

    /**
     * Retrieves the string value of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the string value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public String getString(String parameter, String defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        String result = null;

        readLock_.lock();
        try {
            if (!finalParameters_.contains(parameter) &&
                !isPreferencesParameter(parameter) &&
                hasPreferencesNode()) {
                var preferences = getPreferencesNode();

                if (preferences != null) {
                    result = preferences.get(parameter, null);
                }
            }

            if (null == result &&
                null != parameters_) {
                result = parameters_.get(parameter);
            }
        } finally {
            readLock_.unlock();
        }

        if (null == result) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * Retrieves the boolean value of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the boolean value; or {@code false} if the parameter doesn't exist
     * @since 1.6.0
     */
    public boolean getBool(String parameter) {
        return getBool(parameter, false);
    }

    /**
     * Retrieves the boolean value of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the boolean value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public boolean getBool(String parameter, boolean defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        return Convert.toBoolean(getString(parameter), defaultValue);
    }

    /**
     * Retrieves the char value of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the char value; or {@code '0'} if the parameter doesn't exist
     * @since 1.6.0
     */
    public char getChar(String parameter) {
        return getChar(parameter, (char) 0);
    }

    /**
     * Retrieves the char value of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the char value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public char getChar(String parameter, char defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        return Convert.toChar(getString(parameter), defaultValue);
    }

    /**
     * Retrieves the int value of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the int value; or {@code 0} if the parameter doesn't exist
     * @since 1.6.0
     */
    public int getInt(String parameter) {
        return getInt(parameter, 0);
    }

    /**
     * Retrieves the int value of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the int value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public int getInt(String parameter, int defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        return Convert.toInt(getString(parameter), defaultValue);
    }

    /**
     * Retrieves the long value of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the long value; or {@code 0L} if the parameter doesn't exist
     * @since 1.6.0
     */
    public long getLong(String parameter) {
        return getLong(parameter, 0L);
    }

    /**
     * Retrieves the long value of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the long value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public long getLong(String parameter, long defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        return Convert.toLong(getString(parameter), defaultValue);
    }

    /**
     * Retrieves the float value of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the float value; or {@code 0F} if the parameter doesn't exist
     * @since 1.6.0
     */
    public float getFloat(String parameter) {
        return getFloat(parameter, 0F);
    }

    /**
     * Retrieves the float value of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the float value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public float getFloat(String parameter, float defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        return Convert.toFloat(getString(parameter), defaultValue);
    }

    /**
     * Retrieves the double value of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the double value; or {@code 0D} if the parameter doesn't exist
     * @since 1.6.0
     */
    public double getDouble(String parameter) {
        return getDouble(parameter, 0D);
    }

    /**
     * Retrieves the double value of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the double value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public double getDouble(String parameter, double defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        return Convert.toDouble(getString(parameter), defaultValue);
    }

    /**
     * Retrieves a serialized instance of a parameter.
     *
     * @param parameter the name of the parameter
     * @return the instance of the serialized value; or {@code null} if the parameter doesn't exist or can't be deserialized
     * @since 1.6.0
     */
    public <TargetType extends Serializable> TargetType getSerializable(String parameter) {
        return getSerializable(parameter, null);
    }

    /**
     * Retrieves a serialized instance of a parameter.
     *
     * @param parameter    the name of the parameter
     * @param defaultValue the default value to use when the parameter doesn't exist
     * @return the double value; or the provided default value if the parameter doesn't exist
     * @since 1.6.0
     */
    public <TargetType extends Serializable> TargetType getSerializable(String parameter, TargetType defaultValue) {
        if (null == parameter || 0 == parameter.length()) {
            return defaultValue;
        }

        var value = getString(parameter);
        if (null != value) {
            try {
                return SerializationUtils.deserializeFromString(value);
            } catch (SerializationUtilsErrorException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Sets whether a parameter is final or not.
     *
     * @param parameter the name of the parameter
     * @param isFinal   {@code true} to make the parameter final; or {@code false} otherwise
     * @return this config instance
     * @since 1.6.0
     */
    public Config finalParameter(String parameter, boolean isFinal) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        writeLock_.lock();
        try {
            if (isFinal &&
                !finalParameters_.contains(parameter)) {
                finalParameters_.add(parameter);
            } else {
                finalParameters_.remove(parameter);
            }
        } finally {
            writeLock_.unlock();
        }
        return this;
    }

    /**
     * Sets the string value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, String value) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");
        if (null == value) throw new IllegalArgumentException("value can't be null.");

        writeLock_.lock();
        try {
            if (!finalParameters_.contains(parameter)) {
                parameters_.put(parameter, value);

                if (!isPreferencesParameter(parameter) &&
                    hasPreferencesNode()) {
                    var preferences = getPreferencesNode();

                    if (preferences != null &&
                        preferences.get(parameter, null) != null) {
                        preferences.put(parameter, value);
                    }
                }
            }
        } finally {
            writeLock_.unlock();
        }

        return this;
    }

    /**
     * Sets the boolean value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, boolean value) {
        return parameter(parameter, String.valueOf(value));
    }

    /**
     * Sets the char value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, char value) {
        return parameter(parameter, String.valueOf(value));
    }

    /**
     * Sets the int value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, int value) {
        return parameter(parameter, String.valueOf(value));
    }

    /**
     * Sets the long value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, long value) {
        return parameter(parameter, String.valueOf(value));
    }

    /**
     * Sets the float value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, float value) {
        return parameter(parameter, String.valueOf(value));
    }

    /**
     * Sets the double value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, double value) {
        return parameter(parameter, String.valueOf(value));
    }

    /**
     * Sets the serializable value of a parameter.
     *
     * @param parameter the name of the parameter
     * @param value     the value of the parameter
     * @return this config instance
     * @since 1.6.0
     */
    public Config parameter(String parameter, Serializable value)
    throws ConfigErrorException {
        try {
            return parameter(parameter, SerializationUtils.serializeToString(value));
        } catch (SerializationUtilsErrorException e) {
            throw new ConfigErrorException(e);
        }
    }

    /**
     * Removes a parameter.
     *
     * @param parameter the parameter name
     * @since 1.6.0
     */
    public void removeParameter(String parameter) {
        if (null == parameter ||
            0 == parameter.length()) {
            return;
        }

        writeLock_.lock();
        try {
            if (!finalParameters_.contains(parameter)) {
                parameters_.remove(parameter);

                if (!isPreferencesParameter(parameter) &&
                    hasPreferencesNode()) {
                    var preferences = getPreferencesNode();

                    if (preferences != null) {
                        preferences.remove(parameter);
                    }
                }
            }
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Indicates whether a list is final.
     * <p>
     * A final list can not be changed.
     *
     * @param list the name of the list
     * @return {@code true} if the list is final; or {@code false} otherwise
     * @since 1.6.0
     */
    public boolean isFinalList(String list) {
        if (null == list || 0 == list.length()) {
            return false;
        }

        readLock_.lock();
        try {
            return finalLists_.contains(list);
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Retrieves a list as string items.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public List<String> getStringItems(String list) {
        if (null == list || 0 == list.length()) {
            return null;
        }

        readLock_.lock();
        try {
            List<String> list_items = null;

            if (!finalLists_.contains(list) &&
                hasPreferencesNode()) {
                var preferences = getPreferencesNode();

                if (preferences != null) {
                    var list_preferences = preferences.node(list);

                    if (list_preferences != null) {
                        try {
                            var string_array = list_preferences.keys();
                            if (string_array != null &&
                                string_array.length > 0) {
                                var int_array = new int[string_array.length];
                                var counter = 0;
                                for (var item_string : string_array) {
                                    int_array[counter++] = Integer.parseInt(item_string);
                                }
                                Arrays.sort(int_array);

                                list_items = new ArrayList<>(int_array.length);
                                for (var item_int : int_array) {
                                    list_items.add(list_preferences.get(String.valueOf(item_int), null));
                                }
                            }
                        } catch (BackingStoreException e) {
                            // that's ok, don't handle the preferences node
                        }
                    }
                }
            }

            if (null == list_items) {
                list_items = lists_.get(list);
            }

            return list_items;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Retrieves a list as boolean items.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public List<Boolean> getBoolItems(String list) {
        var list_items = getStringItems(list);
        if (list_items == null) {
            return null;
        }

        var result = new ArrayList<Boolean>(list_items.size());
        for (var item : list_items) {
            result.add(Convert.toBoolean(item, false));
        }

        return result;
    }

    /**
     * Retrieves a list as char items.
     * <p>
     * Any item that couldn't be converted to a char will be excluded.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public List<Character> getCharItems(String list) {
        var list_items = getStringItems(list);
        if (list_items == null) {
            return null;
        }

        var result = new ArrayList<Character>(list_items.size());
        for (var item : list_items) {
            try {
                result.add(Convert.toChar(item));
            } catch (ConversionException ignored) {
            }
        }

        return result;
    }

    /**
     * Retrieves a list as int items.
     * <p>
     * Any item that couldn't be converted to an int will be excluded.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public List<Integer> getIntItems(String list) {
        var list_items = getStringItems(list);
        if (list_items == null) {
            return null;
        }

        var result = new ArrayList<Integer>(list_items.size());
        for (var item : list_items) {
            if (null != item) {
                try {
                    result.add(Convert.toInt(item));
                } catch (ConversionException ignored) {
                }
            }
        }

        return result;
    }

    /**
     * Retrieves a list as long items.
     * <p>
     * Any item that couldn't be converted to a long will be excluded.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public List<Long> getLongItems(String list) {
        var list_items = getStringItems(list);
        if (list_items == null) {
            return null;
        }

        var result = new ArrayList<Long>(list_items.size());
        for (var item : list_items) {
            if (null != item) {
                try {
                    result.add(Convert.toLong(item));
                } catch (ConversionException ignored) {
                }
            }
        }

        return result;
    }

    /**
     * Retrieves a list as float items.
     * <p>
     * Any item that couldn't be converted to a float will be excluded.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public List<Float> getFloatItems(String list) {
        var list_items = getStringItems(list);
        if (list_items == null) {
            return null;
        }

        var result = new ArrayList<Float>(list_items.size());
        for (var item : list_items) {
            if (null != item) {
                try {
                    result.add(Convert.toFloat(item));
                } catch (ConversionException ignored) {
                }
            }
        }

        return result;
    }

    /**
     * Retrieves a list as double items.
     * <p>
     * Any item that couldn't be converted to a double will be excluded.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public List<Double> getDoubleItems(String list) {
        var list_items = getStringItems(list);
        if (list_items == null) {
            return null;
        }

        var result = new ArrayList<Double>(list_items.size());
        for (var item : list_items) {
            if (null != item) {
                try {
                    result.add(Convert.toDouble(item));
                } catch (ConversionException ignored) {
                }
            }
        }

        return result;
    }

    /**
     * Retrieves a list as serializable items.
     * <p>
     * Any item that couldn't be deserialized will be excluded.
     *
     * @param list the name of the list
     * @return the requested list; or {@code null} of the list couldn't be found
     * @since 1.6.0
     */
    public <TargetType extends Serializable> List<TargetType> getSerializableItems(String list) {
        var list_items = getStringItems(list);
        if (list_items == null) {
            return null;
        }

        var result = new ArrayList<TargetType>(list_items.size());
        for (var item : list_items) {
            try {
                result.add(SerializationUtils.deserializeFromString(item));
            } catch (SerializationUtilsErrorException ignored) {
            }
        }

        return result;
    }

    /**
     * Returns whether a list exists.
     *
     * @param list the name of the list
     * @return {@code true} if the list exists; or {@code false} otherwise
     * @since 1.6.0
     */
    public boolean hasList(String list) {
        if (null == list || 0 == list.length()) {
            return false;
        }

        readLock_.lock();
        try {
            if (lists_.containsKey(list)) {
                return true;
            }

            if (hasPreferencesNode()) {
                var preferences = getPreferencesNode();

                if (preferences != null) {
                    try {
                        var list_names_array = preferences.childrenNames();

                        if (list_names_array != null) {
                            var list_names = Arrays.asList(list_names_array);

                            return list_names.contains(list);
                        }
                    } catch (BackingStoreException e) {
                        // that's ok, don't handle the preferences node
                    }
                }
            }
        } finally {
            readLock_.unlock();
        }

        return false;
    }

    /**
     * Returns the number of lists that are present.
     *
     * @return the number of lists
     * @since 1.6.0
     */
    public int countLists() {
        readLock_.lock();
        try {
            var result = lists_.size();

            if (hasPreferencesNode()) {
                var preferences = getPreferencesNode();

                if (preferences != null) {
                    try {
                        var list_names = preferences.childrenNames();

                        if (list_names != null) {
                            for (var list_name : list_names) {
                                if (!lists_.containsKey(list_name)) {
                                    result++;
                                }
                            }
                        }
                    } catch (BackingStoreException e) {
                        // that's ok, don't handle the preferences node
                    }
                }
            }

            return result;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Adds a new string item to a list.
     *
     * @param list the name of the list
     * @param item the value of the item
     * @return this config instance
     * @since 1.6.0
     */
    public Config listItem(String list, String item) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");
        if (null == item) throw new IllegalArgumentException("item can't be null.");

        writeLock_.lock();
        try {
            if (!finalLists_.contains(list)) {
                List<String> list_items = null;

                if (hasPreferencesNode()) {
                    var preferences = getPreferencesNode();

                    if (preferences != null) {
                        var list_preferences = preferences.node(list);

                        if (list_preferences != null) {
                            try {
                                var string_array = list_preferences.keys();
                                if (string_array != null) {
                                    var int_array = new int[string_array.length];
                                    var counter = 0;
                                    for (var item_string : string_array) {
                                        int_array[counter++] = Integer.parseInt(item_string);
                                    }
                                    Arrays.sort(int_array);

                                    list_items = new ArrayList<>(int_array.length);
                                    for (var item_int : int_array) {
                                        list_items.add(list_preferences.get(String.valueOf(item_int), null));
                                    }

                                    list_preferences.put(String.valueOf(string_array.length), item);
                                    list_items.add(item);
                                }
                            } catch (BackingStoreException e) {
                                // that's ok, don't handle the preferences node
                            }
                        }
                    }
                }

                if (list_items != null) {
                    lists_.put(list, list_items);
                } else {
                    if (lists_.containsKey(list)) {
                        list_items = lists_.get(list);
                        list_items.add(item);
                    } else {
                        list_items = new ArrayList<>();
                        lists_.put(list, list_items);

                        list_items.add(item);
                    }
                }
            }
        } finally {
            writeLock_.unlock();
        }

        return this;
    }

    /**
     * Adds a new boolean item to a list.
     *
     * @param list the name of the list
     * @param item the value of the item
     * @return this config instance
     * @since 1.6.0
     */
    public Config listItem(String list, boolean item) {
        return listItem(list, String.valueOf(item));
    }

    /**
     * Adds a new char item to a list.
     *
     * @param list the name of the list
     * @param item the value of the item
     * @return this config instance
     * @since 1.6.0
     */
    public Config listItem(String list, char item) {
        return listItem(list, String.valueOf(item));
    }

    /**
     * Adds a new long item to a list.
     *
     * @param list the name of the list
     * @param item the value of the item
     * @return this config instance
     * @since 1.6.0
     */
    public Config listItem(String list, int item) {
        return listItem(list, String.valueOf(item));
    }

    public Config listItem(String list, long item) {
        return listItem(list, String.valueOf(item));
    }

    /**
     * Adds a new float item to a list.
     *
     * @param list the name of the list
     * @param item the value of the item
     * @return this config instance
     * @since 1.6.0
     */
    public Config listItem(String list, float item) {
        return listItem(list, String.valueOf(item));
    }

    /**
     * Adds a new double item to a list.
     *
     * @param list the name of the list
     * @param item the value of the item
     * @return this config instance
     * @since 1.6.0
     */
    public Config listItem(String list, double item) {
        return listItem(list, String.valueOf(item));
    }

    /**
     * Adds a new serializable item to a list.
     *
     * @param list the name of the list
     * @param item the value of the item
     * @return this config instance
     * @since 1.6.0
     */
    public Config listItem(String list, Serializable item)
    throws ConfigErrorException {
        try {
            return listItem(list, SerializationUtils.serializeToString(item));
        } catch (SerializationUtilsErrorException e) {
            throw new ConfigErrorException(e);
        }
    }

    /**
     * Clears all the items from a list.
     * <p>
     * If the list doesn't exist, nothing will happen.
     *
     * @param list the name of the list
     * @since 1.6.0
     */
    public void clearList(String list) {
        if (null == list || 0 == list.length()) {
            return;
        }

        writeLock_.lock();
        try {
            if (!finalLists_.contains(list)) {
                if (hasPreferencesNode()) {
                    var preferences = getPreferencesNode();

                    if (preferences != null) {
                        var list_preferences = preferences.node(list);

                        if (list_preferences != null) {
                            try {
                                list_preferences.clear();
                            } catch (BackingStoreException e) {
                                // that's ok, don't handle the preferences node
                            }
                        }
                    }
                }

                if (lists_.containsKey(list)) {
                    lists_.put(list, new ArrayList<>());
                }
            }
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Removes a list.
     * <p>
     * If the list doesn't exist, nothing will happen.
     *
     * @param list the name of the list
     * @since 1.6.0
     */
    public void removeList(String list) {
        if (null == list || 0 == list.length()) {
            return;
        }

        writeLock_.lock();
        try {
            if (!finalLists_.contains(list)) {
                if (hasPreferencesNode()) {
                    var preferences = getPreferencesNode();

                    if (preferences != null) {
                        var list_preferences = preferences.node(list);

                        if (list_preferences != null) {
                            try {
                                list_preferences.removeNode();
                            } catch (BackingStoreException e) {
                                // that's ok, don't handle the preferences node
                            }
                        }
                    }
                }

                lists_.remove(list);
            }
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Sets whether a list is final or not.
     *
     * @param list    the name of the parameter
     * @param isFinal {@code true} to make the list final; or {@code false} otherwise
     * @return this config instance
     * @since 1.6.0
     */
    public Config finalList(String list, boolean isFinal) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        writeLock_.lock();
        try {
            if (isFinal &&
                !finalLists_.contains(list)) {
                finalLists_.add(list);
            } else {
                finalLists_.remove(list);
            }
        } finally {
            writeLock_.unlock();
        }

        return this;
    }

    /**
     * Generates an XML presentation of the current state of this config instance.
     *
     * @return the generates XML
     * @since 1.6.0
     */
    public String toXml() {
        readLock_.lock();
        try {
            var xml_output = new StringBuilder();
            xml_output.append("<config>\n");

            var list_keys_arraylist = new ArrayList<>(lists_.keySet());
            list_keys_arraylist.sort(String::compareTo);

            for (var list_key : list_keys_arraylist) {
                xml_output.append("\t<list name=\"");
                xml_output.append(StringUtils.encodeXml(list_key));
                if (finalLists_.contains(list_key)) {
                    xml_output.append("\" final=\"true");
                }
                xml_output.append("\">\n");

                var list_items = lists_.get(list_key);
                for (var list_item : list_items) {
                    xml_output.append("\t\t<item>").append(StringUtils.encodeXml(list_item)).append("</item>\n");
                }

                xml_output.append("\t</list>\n");
            }

            var parameter_keys_arraylist = new ArrayList<>(parameters_.keySet());
            parameter_keys_arraylist.sort(String::compareTo);

            for (var parameter_key : parameter_keys_arraylist) {
                xml_output.append("\t<param name=\"");
                xml_output.append(StringUtils.encodeXml(parameter_key));
                if (finalParameters_.contains(parameter_key)) {
                    xml_output.append("\" final=\"true");
                }
                xml_output.append("\">");
                xml_output.append(StringUtils.encodeXml(parameters_.get(parameter_key)));
                xml_output.append("</param>\n");
            }

            xml_output.append("</config>\n");

            return xml_output.toString();
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Store this config instance to its XML file.
     *
     * @throws ConfigErrorException when an error occurred during the storage of the XML file or
     *                              when no XML file was set
     * @see #getXmlFile()
     * @see #xmlFile
     * @since 1.6.0
     */
    public void storeToXml()
    throws ConfigErrorException {
        var file = getXmlFile();
        if (null == file) {
            throw new MissingXmlFileException();
        }

        storeToXml(file);
    }

    /**
     * Store this config instance to a specific XML file.
     *
     * @param destination the file to store this config instance to
     * @throws ConfigErrorException when an error occurred during the storage of the XML file
     * @since 1.6.0
     */
    public synchronized void storeToXml(File destination)
    throws ConfigErrorException {
        if (null == destination) throw new IllegalArgumentException("destination can't be null");

        if (destination.exists() &&
            !destination.canWrite()) {
            throw new CantWriteToDestinationException(destination);
        }

        var content = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<!DOCTYPE config SYSTEM \"/dtd/config.dtd\">\n");
        content.append(toXml());
        try {
            FileUtils.writeString(content.toString(), destination);
        } catch (FileUtilsErrorException e) {
            throw new StoreXmlErrorException(destination, e);
        }
    }

    /**
     * Store this config instance to its assigned {@link Preferences} node.
     *
     * @throws ConfigErrorException when an error occurred during the storage or
     *                              when no preferences node was assigned
     * @since 1.6.0
     */
    public void storeToPreferences()
    throws ConfigErrorException {
        if (!hasPreferencesNode()) {
            throw new MissingPreferencesUserNodeException();
        }

        storeToPreferences(getPreferencesNode());
    }

    /**
     * Store this config instance to a specific {@link Preferences} node.
     *
     * @throws ConfigErrorException when an error occurred during the storage
     * @since 1.6.0
     */
    public synchronized void storeToPreferences(Preferences preferences)
    throws ConfigErrorException {
        if (null == preferences) throw new IllegalArgumentException("destination can't be null");

        readLock_.lock();
        try {
            Preferences list_node = null;

            for (var list_key : lists_.keySet()) {
                if (finalLists_.contains(list_key)) {
                    continue;
                }

                list_node = preferences.node(list_key);

                var counter = 0;
                var list_items = lists_.get(list_key);
                for (var list_item : list_items) {
                    list_node.put(String.valueOf(counter++), list_item);
                }
            }

            for (var parameter_key : parameters_.keySet()) {
                if (parameter_key.equals(PARAMETER_PREFERENCES_SYSTEM) ||
                    parameter_key.equals(PARAMETER_PREFERENCES_USER) ||
                    finalParameters_.contains(parameter_key)) {
                    continue;
                }

                preferences.put(parameter_key, parameters_.get(parameter_key));
            }

            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                throw new StorePreferencesErrorException(preferences, e);
            }
        } finally {
            readLock_.unlock();
        }
    }

    public Config clone() {
        Config new_config = null;
        readLock_.lock();
        try {
            new_config = (Config) super.clone();
            new_config.parameters_ = ObjectUtils.deepClone(parameters_);
            new_config.finalParameters_ = ObjectUtils.deepClone(finalParameters_);
            new_config.lists_ = ObjectUtils.deepClone(lists_);
            new_config.finalLists_ = ObjectUtils.deepClone(finalLists_);
        } catch (CloneNotSupportedException ignored) {
        } finally {
            readLock_.unlock();
        }

        return new_config;
    }
}
