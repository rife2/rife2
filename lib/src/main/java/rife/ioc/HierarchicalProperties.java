/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import java.util.*;

import rife.ioc.exceptions.IncompatiblePropertyValueTypeException;
import rife.ioc.exceptions.PropertyValueException;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * This class allows the creation of a hierarchical tree of named {@link
 * PropertyValue} instances.
 * <p>When a property is looked up in a child
 * {@code HierarchicalProperties} instance, the lookup will be propagated
 * to its parent when it couldn't be found in the child. A single hierarchical
 * line is thus considered to be one collection that groups all involved
 * {@code HierarchicalProperties} instances. Retrieving the names and the
 * size will recursively take all the properties of the parents into account
 * and return the consolidated result. To offer these features, intelligent
 * caching has been implemented to ensure optimal performance.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class HierarchicalProperties {
    private LinkedHashMap<String, PropertyValue> properties_;

    private HierarchicalProperties parent_;
    private LinkedHashSet<HierarchicalProperties> children_;
    private Set<String> cachedNames_;
    private Set<String> cachedInjectableNames_;

    public HierarchicalProperties() {
    }

    private HierarchicalProperties(HierarchicalProperties shadow) {
        properties_ = shadow.properties_;
    }

    /**
     * Creates a copy of this {@code HierarchicalProperties} hierarchy
     * until a certain instance is reached.
     * <p>
     * Each copied instance will share the datastructure in which the
     * properties are stored with the original. Creating a shadow is for
     * changing the hierarchical structure but maintaining a centralized
     * management of the properties.
     *
     * @param limit the {@code HierarchicalProperties} instance that will
     *              not be part of the shadow copy and interrupt the copying process; or
     *              {@code null} if the entire hierachy should be copied.
     * @return the shadow copy of this {@code HierarchicalProperties}
     * hierarchy
     * @since 1.0
     */
    public HierarchicalProperties createShadow(HierarchicalProperties limit) {
        var result = new HierarchicalProperties(this);

        var original = this;
        var shadow = result;
        while (original.getParent() != null &&
               original.getParent() != limit) {
            shadow.setParent(new HierarchicalProperties(original.getParent()));
            original = original.getParent();
            shadow = shadow.getParent();
        }
        return result;
    }

    /**
     * Retrieves the first parent of this {@code HierarchicalProperties}
     * hierarchy.
     *
     * @return the root of this {@code HierarchicalProperties}
     * hierarchy
     * @since 1.0
     */
    public HierarchicalProperties getRoot() {
        var root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }

        return root;
    }

    /**
     * Retrieves the {@code Map} with only the properties that are
     * locally present in this {@code HierarchicalProperties} instance.
     *
     * @return the local {@code Map} of this
     * {@code HierarchicalProperties} instance
     * @since 1.0
     */
    public Map<String, PropertyValue> getLocalMap() {
        if (null == properties_) {
            return Collections.EMPTY_MAP;
        }

        return properties_;
    }

    /**
     * Sets the parent of this {@code HierarchicalProperties} instance.
     *
     * @param parent the parent of this instance; or {@code null} if this
     *               instance should be isolated
     * @see #getParent
     * @since 1.0
     */
    public void setParent(HierarchicalProperties parent) {
        clearCaches();

        if (parent_ != null) {
            parent_.removeChild(this);
        }

        parent_ = parent;

        if (parent_ != null) {
            parent_.addChild(this);
        }
    }

    /**
     * Sets the parent of this {@code HierarchicalProperties} instance.
     *
     * @param parent the parent of this instance; or {@code null} if this
     *               instance should be isolated
     * @return this {@code HierarchicalProperties} instance
     * @see #getParent
     * @since 1.0
     */
    public HierarchicalProperties parent(HierarchicalProperties parent) {
        setParent(parent);

        return this;
    }

    /**
     * Retrieves the parent of this {@code HierarchicalProperties}
     * instance.
     *
     * @return the parent of this {@code HierarchicalProperties}
     * instance; or
     * <p>{@code null} if this instance is isolated
     * @see #parent
     * @since 1.0
     */
    public HierarchicalProperties getParent() {
        return parent_;
    }

    /**
     * Associates the specified value with the specified name in this
     * {@code HierarchicalProperties} instance. If it previously
     * contained a mapping for this name, the old value is replaced by the
     * specified value.
     *
     * @param name  the name that will be associated with the property
     * @param value the property value that will be associated with the
     *              specified name
     * @return this {@code HierarchicalProperties} instance
     * @see #put(String, Object)
     * @see #putAll
     * @since 1.0
     */
    public HierarchicalProperties put(String name, PropertyValue value) {
        clearCaches();

        if (null == properties_) {
            properties_ = new LinkedHashMap<>();
        }

        properties_.put(name, value);

        return this;
    }

    /**
     * Associates the specified fixed object value with the specified name
     * in this {@code HierarchicalProperties} instance. If it previously
     * contained a mapping for this name, the old value is replaced by the
     * specified value.
     *
     * @param name  the name that will be associated with the property
     * @param value the property value that will be associated with the
     *              specified name, note that this method will create a {@link PropertyValueObject}
     *              instance that will contain the value in a fixed manner
     * @return this {@code HierarchicalProperties} instance
     * @see #put(String, PropertyValue)
     * @see #putAll
     * @since 1.0
     */
    public HierarchicalProperties put(String name, Object value) {
        put(name, new PropertyValueObject(value));

        return this;
    }

    /**
     * Removes the mapping for this name from this
     * {@code HierarchicalProperties} instance, if it is present.
     *
     * @param name the name that will be removed
     * @return the previously associated value; or
     * <p>{@code null} if the name wasn't found in this
     * {@code HierarchicalProperties} instance
     * @since 1.0
     */
    public PropertyValue remove(String name) {
        if (null == properties_) {
            return null;
        }

        clearCaches();

        return properties_.remove(name);
    }

    /**
     * Copies all the named properties from the specified
     * {@code HierarchicalProperties} instance to this
     * {@code HierarchicalProperties} instance. The effect of this call
     * is equivalent to that of calling {@link #put} on this
     * {@code HierarchicalProperties} once for each mapping from the
     * specified {@code HierarchicalProperties} instance.
     *
     * @param source the properties that will be stored in this
     *               {@code HierarchicalProperties} instance
     * @return this {@code HierarchicalProperties} instance
     * @see #put
     * @since 1.0
     */
    public HierarchicalProperties putAll(HierarchicalProperties source) {
        clearCaches();

        if (source.properties_ != null) {
            if (null == properties_) {
                properties_ = new LinkedHashMap<>();
            }

            properties_.putAll(source.properties_);
        }

        return this;
    }

    /**
     * Copies all the named properties from the specified
     * {@code HierarchicalProperties} instance to this
     * {@code HierarchicalProperties} instance, without replacing existing
     * properties. The effect of this call
     * is equivalent to that of calling {@link #put} on this
     * {@code HierarchicalProperties} once for each mapping from the
     * specified {@code HierarchicalProperties} instance that doesn't
     * have a key in this instance yet.
     *
     * @param source the properties that will be stored in this
     *               {@code HierarchicalProperties} instance
     * @return this {@code HierarchicalProperties} instance
     * @see #put
     * @since 1.0
     */
    public HierarchicalProperties putAllWithoutReplacing(HierarchicalProperties source) {
        clearCaches();

        if (source.properties_ != null) {
            if (null == properties_) {
                properties_ = new LinkedHashMap<>();
            }

            for (var entry : source.properties_.entrySet()) {
                if (!properties_.containsKey(entry.getKey())) {
                    properties_.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return this;
    }

    /**
     * Copies all the entries for a {@code Map} instance to this
     * {@code HierarchicalProperties} instance.
     *
     * @param source the map entries that will be stored in this
     *               {@code HierarchicalProperties} instance
     * @return this {@code HierarchicalProperties} instance
     * @since 1.0
     */
    public HierarchicalProperties putAll(Map source) {
        if (null == source) {
            return this;
        }

        clearCaches();

        if (null == properties_) {
            properties_ = new LinkedHashMap<>();
        }

        for (var entry : ((Set<Map.Entry>) source.entrySet())) {
            properties_.put(String.valueOf(entry.getKey()), new PropertyValueObject(entry.getValue()));
        }

        return this;
    }

    /**
     * Checks the {@code HierarchicalProperties} hierarchy for the
     * presence of the specified name.
     *
     * @param name the name whose presence will be checked
     * @return {@code true} if the name was found; or
     * <p>{@code false} otherwise
     * @see #get
     * @since 1.0
     */
    public boolean contains(String name) {
        var current = this;

        LinkedHashMap<String, PropertyValue> properties = null;
        while (true) {
            properties = current.properties_;

            if (properties != null) {
                if (properties.containsKey(name)) {
                    return true;
                }
            }

            if (null == current.parent_) {
                break;
            }

            current = current.parent_;
        }

        return false;
    }

    /**
     * Retrieves the {@code PropertyValue} for a specific name from the
     * {@code HierarchicalProperties} hierarchy.
     *
     * @param name the name whose associated value will be returned
     * @return the associated {@code PropertyValue}; or
     * <p>{@code null} if the name could not be found
     * @see #contains
     * @since 1.0
     */
    public PropertyValue get(String name) {
        var current = this;
        PropertyValue result;

        LinkedHashMap<String, PropertyValue> properties = null;
        while (true) {
            properties = current.properties_;

            if (properties != null) {
                result = properties.get(name);
                if (result != null) {
                    return result;
                }
            }

            if (null == current.parent_) {
                break;
            }

            current = current.parent_;
        }

        return null;
    }

    /**
     * Retrieves the value of {@code PropertyValue} for a specific name from
     * the {@code HierarchicalProperties} hierarchy.
     *
     * @param name the name whose associated value will be returned
     * @return the associated {@code PropertyValue}; or
     * <p>{@code null} if the name could not be found
     * @throws PropertyValueException when an error occurred while retrieving the
     *                                property value
     * @see #get
     * @see #getValue(String, Object)
     * @since 1.0
     */
    public Object getValue(String name)
    throws PropertyValueException {
        return getValue(name, null);
    }

    /**
     * Retrieves the value of {@code PropertyValue} for a specific name from
     * the {@code HierarchicalProperties} hierarchy. If the property couldn't
     * be found or if the value was {@code null}, the default value will be
     * returned.
     *
     * @param name         the name whose associated value will be returned
     * @param defaultValue the value that should be used as a fallback
     * @return the associated {@code PropertyValue}; or
     * <p>the {@code defaultValue} if the property couldn't be found or if
     * the value was {@code null}
     * @throws PropertyValueException when an error occurred while retrieving the
     *                                property value
     * @see #get
     * @see #getValue(String)
     * @since 1.0
     */
    public Object getValue(String name, Object defaultValue)
    throws PropertyValueException {
        Object result = null;

        var property = get(name);
        if (property != null) {
            result = property.getValue();
        }

        if (null == result) {
            return defaultValue;
        }

        return result;
    }

    /**
     * Retrieves the string value of {@code PropertyValue} for a specific name from
     * the {@code HierarchicalProperties} hierarchy.
     *
     * @param name the name whose associated value will be returned
     * @return the string value of the retrieved {@code PropertyValue}; or
     * <p>{@code null} if the name could not be found
     * @throws PropertyValueException when an error occurred while retrieving the
     *                                property value
     * @see #get
     * @see #getValueString(String, String)
     * @see #getValueTyped
     * @since 1.0
     */
    public String getValueString(String name)
    throws PropertyValueException {
        return getValueString(name, null);
    }

    /**
     * Retrieves the string value of {@code PropertyValue} for a specific name from
     * the {@code HierarchicalProperties} hierarchy. If the property couldn't
     * be found, if the value was {@code null} or if the value was empty, the
     * default value will be returned.
     *
     * @param name         the name whose associated value will be returned
     * @param defaultValue the value that should be used as a fallback
     * @return the string value of the retrieved {@code PropertyValue}; or
     * <p>the {@code defaultValue} if the property couldn't be found or if
     * the value was {@code null} or an empty string
     * @throws PropertyValueException when an error occurred while retrieving the
     *                                property value
     * @see #get
     * @see #getValueString(String)
     * @see #getValueTyped
     * @since 1.0
     */
    public String getValueString(String name, String defaultValue)
    throws PropertyValueException {
        String result = null;

        var property = get(name);
        if (property != null) {
            result = property.getValueString();
        }

        if (null == result ||
            0 == result.length()) {
            return defaultValue;
        }

        return result;
    }

    /**
     * Retrieves the typed value of {@code PropertyValue} for a specific name from
     * the {@code HierarchicalProperties} hierarchy.
     * <p>
     * Note that no conversion will occurr, the value is simple verified to be
     * assignable to the requested type and then cast to it.
     *
     * @param name the name whose associated value will be returned
     * @param type the class that the value has to be retrieved as
     * @return the associated {@code PropertyValue} as an instance of the
     * provided type;  or
     * <p>{@code null} if the name could not be found
     * @throws IncompatiblePropertyValueTypeException when the type of the property
     *                                                value wasn't compatible with the requested type
     * @throws PropertyValueException                 when an error occurred while retrieving the
     *                                                property value
     * @see #get
     * @see #getValueString
     * @see #getValueTyped(String, Class)
     * @since 1.0
     */
    public <T> T getValueTyped(String name, Class<T> type)
    throws PropertyValueException {
        return (T) getValueTyped(name, type, null);
    }

    /**
     * Retrieves the typed value of {@code PropertyValue} for a specific name from
     * the {@code HierarchicalProperties} hierarchy.
     * <p>
     * Note that no conversion will occur, the value is simple verified to be
     * assignable to the requested type and then cast to it.
     *
     * @param name         the name whose associated value will be returned
     * @param type         the class that the value has to be retrieved as
     * @param defaultValue the value that should be used as a fallback
     * @return the associated {@code PropertyValue} as an instance of the
     * provided type;  or
     * <p>the {@code defaultValue} if the property couldn't be found or if
     * the value was {@code null}
     * @throws IncompatiblePropertyValueTypeException when the type of the property
     *                                                value wasn't compatible with the requested type
     * @throws PropertyValueException                 when an error occurred while retrieving the
     *                                                property value
     * @see #get
     * @see #getValueString
     * @see #getValueTyped(String, Class)
     * @since 1.0
     */
    public <T> T getValueTyped(String name, Class<T> type, T defaultValue)
    throws PropertyValueException {
        if (null == name ||
            null == type ||
            0 == name.length()) {
            return defaultValue;
        }

        Object result = null;

        var property = get(name);
        if (property != null) {
            result = property.getValue();
        }

        if (null == result) {
            return defaultValue;
        }

        if (!type.isAssignableFrom(result.getClass())) {
            throw new IncompatiblePropertyValueTypeException(name, type, result.getClass(), null);
        }

        return (T) result;
    }

    /**
     * Retrieves the number of unique names in the
     * {@code HierarchicalProperties} hierarchy.
     *
     * @return the amount of unique names
     * @since 1.0
     */
    public int size() {
        return getNames().size();
    }

    /**
     * Retrieves a {@code Set} with the unique names that are present in
     * the {@code HierarchicalProperties} hierarchy.
     *
     * @return a collection with the unique names
     * @see #getInjectableNames
     * @since 1.0
     */
    public Collection<String> getNames() {
        if (cachedNames_ != null) {
            return cachedNames_;
        }

        var current = this;
        Set<String> names = new LinkedHashSet<>();

        LinkedHashMap<String, PropertyValue> properties = null;
        while (true) {
            properties = current.properties_;

            if (properties != null) {
                names.addAll(properties.keySet());
            }

            if (null == current.parent_) {
                break;
            }

            current = current.parent_;
        }

        cachedNames_ = names;

        return names;
    }

    /**
     * Retrieves a {@code Set} with the unique names that are present in
     * the {@code HierarchicalProperties} hierarchy and that conform to
     * the <a
     * href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.8">Java
     * rules for valid identifiers</a>. The names in this set are thus usable
     * for injection through bean setters.
     *
     * @return a {@code Set} with the unique injectable names
     * @see #getNames
     * @since 1.0
     */
    public Collection<String> getInjectableNames() {
        if (cachedInjectableNames_ != null) {
            return cachedInjectableNames_;
        }

        Set<String> injectable_names = new LinkedHashSet<>();

        var names = getNames();
        for (var name : names) {
            var injectable = true;
            CharacterIterator it = new StringCharacterIterator(name);
            for (var c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
                if (!Character.isJavaIdentifierPart(c)) {
                    injectable = false;
                    break;
                }
            }

            if (injectable) {
                injectable_names.add(name);
            }
        }

        cachedInjectableNames_ = injectable_names;

        return injectable_names;
    }

    private void clearCaches() {
        cachedNames_ = null;
        cachedInjectableNames_ = null;

        if (null == children_) {
            return;
        }

        for (var child : children_) {
            child.clearCaches();
        }
    }

    private void addChild(HierarchicalProperties child) {
        if (null == children_) {
            children_ = new LinkedHashSet<>();
        }
        children_.add(child);
    }

    private void removeChild(HierarchicalProperties child) {
        if (null == children_) {
            return;
        }
        children_.remove(child);
    }
}
