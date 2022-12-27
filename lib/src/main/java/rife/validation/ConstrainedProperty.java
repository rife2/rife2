/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.*;

import rife.cmf.MimeType;
import rife.cmf.transform.ContentTransformer;
import rife.database.queries.CreateTable;
import rife.tools.ClassUtils;
import rife.tools.Convert;

import java.text.Format;

/**
 * A <code>ConstrainedProperty</code> object makes it possible to easily
 * define all constraints for a named property of a bean.
 * <p>The property name refers to the actual name of the bean property.
 * However, this sometimes doesn't correspond to its conceptual usage. It can
 * be handy to receive constraint violation reports with another conceptual
 * name: the subject name. Notice that this corresponds to the subject that is
 * used in a {@link ValidationError}. If no subject name is specified, the
 * property name will be used instead.
 * <p>It's possible to add constraints to a ConstrainedProperty instance
 * through regular setters, but chainable setters are also available to make
 * it possible to easily define a series of constraints, for example:
 * <pre>ConstrainedProperty constrained = new ConstrainedProperty("password")
 *    .maxLength(8)
 *    .notNull(true);</pre>
 * <p>
 * <p>Constrained properties are typically added to a {@link Constrained} bean
 * in its constructor. These are the static constraints that will be set for
 * each and every instance of the bean. You'll however most of the time use
 * the {@link MetaData} class that provides the {@link
 * MetaData#activateMetaData activateMetaData} method which initializes
 * the constraints on a need-to-have basis. This dramatically reduces memory
 * usage since otherwise all constraints will be initialized for every bean
 * instance, even though you don't use them, for example:
 * <pre>public class Credentials extends MetaData
 * {
 *    private String login_ = null;
 *    private String password_ = null;
 *    private String language_ = null;
 *
 *    public Credentials() {
 *    }
 *
 *    public activateMetaData() {
 *        addConstraint(new ConstrainedProperty("login").maxLength(6).notNull(true));
 *        addConstraint(new ConstrainedProperty("password").maxLength(8).notNull(true));
 *        addConstraint(new ConstrainedProperty("language").notNull(true));
 *    }
 *
 *    public void setLogin(String login) { login_ = login; }
 *    public String getLogin() { return login_; }
 *    public void setPassword(String password) { password_ = password; }
 *    public String getPassword() { return password_; }
 *    public void setLanguage(String language) { language_ = language; }
 *    public String getLanguage() { return language_; }
 * }</pre>
 * <p>
 * <p>It's however also possible to add constraints to a single bean instance
 * whenever they can't be determined beforehand. These are then dynamic
 * constraints than can be populated at runtime, for example:
 * <pre>Credentials credentials = new Credentials();
 * credentials.addConstraint(new ConstrainedProperty("language").inList(new String[] {"nl", "fr", "en"}));
 * </pre>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Constrained
 * @see ConstrainedBean
 * @since 1.0
 */
public class ConstrainedProperty implements Cloneable {
    // standard constraint identifiers
    public static final String NOT_NULL = "NOT_NULL";
    public static final String NOT_EMPTY = "NOT_EMPTY";
    public static final String NOT_EQUAL = "NOT_EQUAL";
    public static final String UNIQUE = "UNIQUE";
    public static final String IDENTIFIER = "IDENTIFIER";
    public static final String EDITABLE = "EDITABLE";
    public static final String PERSISTENT = "PERSISTENT";
    public static final String SAVED = "SAVED";
    public static final String DISPLAYED_RAW = "DISPLAYED_RAW";
    public static final String MIN_LENGTH = "MIN_LENGTH";
    public static final String MAX_LENGTH = "MAX_LENGTH";
    public static final String SCALE = "SCALE";
    public static final String REGEXP = "REGEXP";
    public static final String EMAIL = "EMAIL";
    public static final String URL = "URL";
    public static final String MIN_DATE = "MIN_DATE";
    public static final String MAX_DATE = "MAX_DATE";
    public static final String IN_LIST = "IN_LIST";
    public static final String RANGE_BEGIN = "RANGE_BEGIN";
    public static final String RANGE_END = "RANGE_END";
    public static final String DEFAULT_VALUE = "DEFAULT_VALUE";
    public static final String SAME_AS = "SAME_AS";
    public static final String MANY_TO_ONE = "MANY_TO_ONE";
    public static final String MANY_TO_ONE_ASSOCIATION = "MANY_TO_ONE_ASSOCIATION";
    public static final String MANY_TO_MANY = "MANY_TO_MANY";
    public static final String MANY_TO_MANY_ASSOCIATION = "MANY_TO_MANY_ASSOCIATION";
    public static final String FORMAT = "FORMAT";
    public static final String FILE = "FILE";
    public static final String SPARSE = "SPARSE";

    // standard CMF constraint identifiers
    public static final String LISTED = "LISTED";
    public static final String POSITION = "POSITION";
    public static final String MIMETYPE = "MIMETYPE";
    public static final String AUTO_RETRIEVED = "AUTO_RETRIEVED";
    public static final String FRAGMENT = "FRAGMENT";
    public static final String NAME = "NAME";
    public static final String REPOSITORY = "REPOSITORY";
    public static final String ORDINAL = "ORDINAL";
    public static final String ORDINAL_RESTRICTION = "ORDINAL_RESTRICTION";
    public static final String CONTENT_ATTRIBUTES = "CONTENT_ATTRIBUTES";
    public static final String TRANSFORMER = "TRANSFORMER";
    public static final String CACHED_LOADED_DATA = "CACHED_LOADED_DATA";


    // required member variables
    private String propertyName_ = null;
    private String subjectName_ = null;

    // constraints
    protected Map<String, Object> constraints_ = new LinkedHashMap<>();

    // listeners
    protected List<ConstrainedPropertyListener> listeners_;

    /**
     * Adds a new listener.
     * <p>
     * Listeners will be notified when events occur that are specified in the
     * {@code ConstrainedPropertyListener} interface.
     *
     * @param listener the listener instance that will be added
     * @since 1.0
     */
    public void addListener(ConstrainedPropertyListener listener) {
        if (null == listener) {
            return;
        }

        if (null == listeners_) {
            listeners_ = new ArrayList<>();
        }

        synchronized (this) {
            if (!listeners_.contains(listener)) {
                listeners_.add(listener);
            }
        }
    }

    /**
     * Removes a listener.
     * <p>
     * Once the listener has been removed, it will not receive any events anymore.
     *
     * @param listener the listener instance that will be removed
     * @return {@code true} when the listener could be found and has been removed; or
     * <p>{@code false} when the listener wasn't registered before
     * @since 1.0
     */
    public boolean removeListener(ConstrainedPropertyListener listener) {
        if (null == listeners_) {
            return false;
        }

        synchronized (this) {
            return listeners_.remove(listener);
        }
    }

    private void fireConstraintSet(String name, Object constraintData) {
        if (null == listeners_) {
            return;
        }

        synchronized (this) {
            for (ConstrainedPropertyListener listener : listeners_) {
                listener.constraintSet(this, name, constraintData);
            }
        }
    }

    /**
     * Creates a new <code>ConstrainedProperty</code> for the specified
     * property name.
     *
     * @param propertyName the name of the property that has to be
     *                     constrained
     * @since 1.0
     */
    public ConstrainedProperty(String propertyName) {
        if (null == propertyName) throw new IllegalArgumentException("propertyName can't be null.");
        if (0 == propertyName.length()) throw new IllegalArgumentException("propertyName can't be empty.");

        propertyName_ = propertyName;
    }

    /**
     * Sets the subject name.
     *
     * @param name the subject name
     * @return this <code>ConstrainedProperty</code>
     * @since 1.0
     */
    public ConstrainedProperty subjectName(String name) {
        setSubjectName(name);

        return this;
    }

    /**
     * Sets the subject name.
     *
     * @param name the subject name
     * @since 1.0
     */
    public void setSubjectName(String name) {
        subjectName_ = name;
    }

    /**
     * Retrieves the subject name.
     *
     * @return the subject name; or
     * <p>the property name if no subject was specified.
     * @since 1.0
     */
    public String getSubjectName() {
        if (null == subjectName_) {
            return propertyName_;
        }

        return subjectName_;
    }

    /**
     * Retrieves the property name.
     *
     * @return the property name
     * @since 1.0
     */
    public String getPropertyName() {
        return propertyName_;
    }

    /**
     * Set whether the property value can be <code>null</code>.
     * <p>Note that this has different meanings in different contexts:
     * <ul>
     * <li>for values in java this is only applicable to object references
     * as primitive values are never <code>null</code>,
     * <li>for values that are stored in a database, it's applicable to
     * every column.
     * </ul>
     *
     * @param notNull <code>true</code> when the value can't be
     *                <code>null</code>; or <code>false</code> when the value can be
     *                <code>null</code>.
     * @return this <code>ConstrainedProperty</code>
     * @see #isNotNull()
     * @since 1.0
     */
    public ConstrainedProperty notNull(boolean notNull) {
        setNotNull(notNull);

        return this;
    }

    /**
     * Set whether the property value can be <code>null</code>.
     *
     * @see #notNull(boolean)
     * @since 1.0
     */
    public void setNotNull(boolean notNull) {
        setConstraint(NOT_NULL, notNull);
    }

    /**
     * Retrieves whether the property value can be <code>null</code>.
     *
     * @return <code>true</code> when the value can't be <code>null</code>;
     * or
     * <p><code>false</code> when the value can be <code>null</code>.
     * @see #notNull(boolean)
     * @since 1.0
     */
    public boolean isNotNull() {
        return Convert.toBoolean(constraints_.get(NOT_NULL), false);
    }

    /**
     * Set whether the property value can be empty.
     * <p>Note that this has different meanings for different datatypes
     * <ul>
     * <li>for textual types this is an empty string, ie. "",
     * <li>for numeric types this is 0 (zero).
     * </ul>
     *
     * @param notEmpty <code>true</code> when the value can't be empty; or
     *                 <code>false</code> when the value can be empty.
     * @return this <code>ConstrainedProperty</code>
     * @see #isNotEmpty()
     * @since 1.0
     */
    public ConstrainedProperty notEmpty(boolean notEmpty) {
        setNotEmpty(notEmpty);

        return this;
    }

    /**
     * Set whether the property value can be empty.
     *
     * @see #notEmpty(boolean)
     * @since 1.0
     */
    public void setNotEmpty(boolean notEmpty) {
        setConstraint(NOT_EMPTY, notEmpty);
    }

    /**
     * Retrieves whether the property value can be empty.
     *
     * @return <code>true</code> when the value can't be empty; or
     * <p><code>false</code> when the value can be empty.
     * @see #notEmpty(boolean)
     * @since 1.0
     */
    public boolean isNotEmpty() {
        return Convert.toBoolean(constraints_.get(NOT_EMPTY), false);
    }

    /**
     * Set that the property value can't be equal to a specified
     * <code>boolean</code> reference value.
     *
     * @param reference the reference value it will be checked against
     * @return this <code>ConstrainedProperty</code>
     * @see #isNotEqual()
     * @since 1.0
     */
    public ConstrainedProperty notEqual(boolean reference) {
        setNotEqual(reference);

        return this;
    }

    /**
     * Set that the property value can't be equal to a specified
     * <code>Object</code> reference value.
     *
     * @see #notEqual(boolean)
     * @since 1.0
     */
    public ConstrainedProperty notEqual(Object reference) {
        setNotEqual(reference);

        return this;
    }

    /**
     * Set that the property value can't be equal to a specified
     * <code>boolean</code> reference value.
     *
     * @see #notEqual(boolean)
     * @since 1.0
     */
    public void setNotEqual(boolean reference) {
        setNotEqual(Boolean.valueOf(reference));
    }

    /**
     * Set that the property value can't be equal to a specified
     * <code>Object</code> reference value.
     *
     * @see #notEqual(boolean)
     * @since 1.0
     */
    public void setNotEqual(Object reference) {
        if (null == reference) {
            constraints_.remove(NOT_EQUAL);
        } else {
            setConstraint(NOT_EQUAL, reference);
        }
    }

    /**
     * Retrieves whether the property can't be equal to a specific
     * reference value.
     *
     * @return <code>true</code> when the value can't be equal; or
     * <p><code>false</code> when the value can be equal.
     * @see #notEqual(boolean)
     * @since 1.0
     */
    public boolean isNotEqual() {
        return constraints_.containsKey(NOT_EQUAL);
    }

    /**
     * Retrieves the reference object to which the property value can't be
     * equal.
     *
     * @return the requested reference object instance; or
     * <p><code>null</code> when the property has no notEqual constraint.
     * @see #notEqual(boolean)
     * @since 1.0
     */
    public Object getNotEqual() {
        return constraints_.get(NOT_EQUAL);
    }

    /**
     * Set whether the property value has to be unique.
     * <p>Note that this is only applicable to contexts where a collection
     * of the data is stored and that uniqueness can apply against the
     * other entries. In a singular context, uniqueness is always
     * guaranteed.
     *
     * @param unique <code>true</code> when the value has to be unique; or
     *               <code>false</code> when it doesn't have to be.
     * @return this <code>ConstrainedProperty</code>
     * @see #isUnique()
     * @since 1.0
     */
    public ConstrainedProperty unique(boolean unique) {
        setUnique(unique);

        return this;
    }

    /**
     * Set whether the property value has to be unique.
     *
     * @see #unique(boolean)
     * @since 1.0
     */
    public void setUnique(boolean unique) {
        setConstraint(UNIQUE, unique);
    }

    /**
     * Retrieves whether the property value has to be unique.
     *
     * @return <code>true</code> when the value has to be unique; or
     * <p><code>false</code> it doesn't have to be.
     * @see #unique(boolean)
     * @since 1.0
     */
    public boolean isUnique() {
        return Convert.toBoolean(constraints_.get(UNIQUE), false);
    }

    /**
     * Set whether the property value is an identifier.
     * <p>Note that this is only applicable to contexts where a collection
     * of the data is stored and that identification can apply against the
     * other entries. In a singular context, identification is
     * meaningless.
     *
     * @param identifier <code>true</code> when the value is an
     *                   identifier; or <code>false</code> when it isn't.
     * @return this <code>ConstrainedProperty</code>
     * @see #isIdentifier()
     * @since 1.0
     */
    public ConstrainedProperty identifier(boolean identifier) {
        setIdentifier(identifier);

        return this;
    }

    /**
     * Set whether the property value is an identifier.
     *
     * @see #identifier(boolean)
     * @since 1.0
     */
    public void setIdentifier(boolean identifier) {
        setConstraint(IDENTIFIER, identifier);
    }

    /**
     * Retrieves whether the property is an identifier.
     *
     * @return <code>true</code> when the property is an identifier; or
     * <p><code>false</code> it isn't.
     * @see #identifier(boolean)
     * @since 1.0
     */
    public boolean isIdentifier() {
        return Convert.toBoolean(constraints_.get(IDENTIFIER), false);
    }

    public ConstrainedProperty editable(boolean editable) {
        setEditable(editable);

        return this;
    }

    public void setEditable(boolean editable) {
        setConstraint(EDITABLE, editable);
    }

    public boolean isEditable() {
        return Convert.toBoolean(constraints_.get(EDITABLE), true);
    }

    public ConstrainedProperty persistent(boolean persistent) {
        setPersistent(persistent);

        return this;
    }

    public void setPersistent(boolean persistent) {
        if (hasMimeType() && persistent) {
            throw new IllegalArgumentException("Can't make a property persistent that has a content mime type assigned to it.");
        }

        setConstraint(PERSISTENT, persistent);
    }

    public boolean isPersistent() {
        return Convert.toBoolean(constraints_.get(PERSISTENT), true);
    }

    public ConstrainedProperty saved(boolean saved) {
        setSaved(saved);

        return this;
    }

    public void setSaved(boolean saved) {
        setConstraint(SAVED, saved);
    }

    public boolean isSaved() {
        return Convert.toBoolean(constraints_.get(SAVED), true);
    }

    public ConstrainedProperty displayedRaw(boolean displayedRaw) {
        setDisplayedRaw(displayedRaw);

        return this;
    }

    public void setDisplayedRaw(boolean displayedRaw) {
        if (hasMimeType() && !displayedRaw) {
            throw new IllegalArgumentException("Can't make a property not being displayed raw that has a content mime type assigned to it.");
        }

        setConstraint(DISPLAYED_RAW, displayedRaw);
    }

    public boolean isDisplayedRaw() {
        return Convert.toBoolean(constraints_.get(DISPLAYED_RAW), false);
    }

    public boolean hasLimitedLength() {
        return constraints_.containsKey(MIN_LENGTH) || constraints_.containsKey(MAX_LENGTH);
    }

    public boolean hasMixLength() {
        return constraints_.containsKey(MAX_LENGTH);
    }

    public boolean hasMaxLength() {
        return constraints_.containsKey(MAX_LENGTH);
    }

    public ConstrainedProperty minLength(int minLength) {
        setMinLength(minLength);

        return this;
    }

    public void setMinLength(int minLength) {
        if (minLength <= 0) {
            constraints_.remove(MIN_LENGTH);
        } else {
            setConstraint(MIN_LENGTH, minLength);
        }
    }

    public int getMinLength() {
        return Convert.toInt(constraints_.get(MIN_LENGTH), -1);
    }

    public ConstrainedProperty maxLength(int maxLength) {
        setMaxLength(maxLength);

        return this;
    }

    public void setMaxLength(int maxLength) {
        if (maxLength < 0) {
            constraints_.remove(MAX_LENGTH);
        } else {
            setConstraint(MAX_LENGTH, maxLength);
        }
    }

    public int getMaxLength() {
        return Convert.toInt(constraints_.get(MAX_LENGTH), -1);
    }

    public boolean hasPrecision() {
        return constraints_.containsKey(MAX_LENGTH);
    }

    public ConstrainedProperty precision(int precision) {
        setPrecision(precision);

        return this;
    }

    public void setPrecision(int precision) {
        setMaxLength(precision);
    }

    public int getPrecision() {
        return getMaxLength();
    }

    public boolean hasScale() {
        return constraints_.containsKey(SCALE);
    }

    public ConstrainedProperty scale(int scale) {
        setScale(scale);

        return this;
    }

    public void setScale(int scale) {
        if (scale < 0) {
            constraints_.remove(SCALE);
        } else {
            setConstraint(SCALE, scale);
        }
    }

    public int getScale() {
        return Convert.toInt(constraints_.get(SCALE), -1);
    }

    public ConstrainedProperty regexp(String regexp) {
        setRegexp(regexp);

        return this;
    }

    public void setRegexp(String regexp) {
        if (null == regexp) {
            constraints_.remove(REGEXP);
        } else {
            setConstraint(REGEXP, regexp);
        }
    }

    public String getRegexp() {
        return (String) constraints_.get(REGEXP);
    }

    public boolean matchesRegexp() {
        return constraints_.containsKey(REGEXP);
    }

    public ConstrainedProperty email(boolean email) {
        setEmail(email);

        return this;
    }

    public void setEmail(boolean email) {
        setConstraint(EMAIL, email);
    }

    public boolean isEmail() {
        return Convert.toBoolean(constraints_.get(EMAIL), false);
    }

    public ConstrainedProperty url(boolean url) {
        setUrl(url);

        return this;
    }

    public void setUrl(boolean url) {
        setConstraint(URL, url);
    }

    public boolean isUrl() {
        return Convert.toBoolean(constraints_.get(URL), false);
    }

    public ConstrainedProperty minDate(Date minDate) {
        setMinDate(minDate);

        return this;
    }

    public void setMinDate(Date minDate) {
        if (null == minDate) {
            constraints_.remove(MIN_DATE);
        } else {
            setConstraint(MIN_DATE, minDate);
        }
    }

    public Date getMinDate() {
        return (Date) constraints_.get(MIN_DATE);
    }

    public ConstrainedProperty maxDate(Date maxDate) {
        setMaxDate(maxDate);

        return this;
    }

    public void setMaxDate(Date maxDate) {
        if (null == maxDate) {
            constraints_.remove(MAX_DATE);
        } else {
            setConstraint(MAX_DATE, maxDate);
        }
    }

    public Date getMaxDate() {
        return (Date) constraints_.get(MAX_DATE);
    }

    public boolean isLimitedDate() {
        return constraints_.containsKey(MIN_DATE) || constraints_.containsKey(MAX_DATE);
    }

    public ConstrainedProperty inList(String... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(String... inList) {
        if (null == inList) {
            constraints_.remove(IN_LIST);
        } else {
            setConstraint(IN_LIST, inList);
        }
    }

    public ConstrainedProperty inList(int... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(int... inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.length];
            for (int i = 0; i < inList.length; i++) {
                list[i] = String.valueOf(inList[i]);
            }
        }

        setInList(list);
    }

    public ConstrainedProperty inList(byte... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(byte... inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.length];
            for (int i = 0; i < inList.length; i++) {
                list[i] = String.valueOf(inList[i]);
            }
        }

        setInList(list);
    }

    public ConstrainedProperty inList(char... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(char... inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.length];
            for (int i = 0; i < inList.length; i++) {
                list[i] = String.valueOf(inList[i]);
            }
        }

        setInList(list);
    }

    public ConstrainedProperty inList(short... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(short... inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.length];
            for (int i = 0; i < inList.length; i++) {
                list[i] = String.valueOf(inList[i]);
            }
        }

        setInList(list);
    }

    public ConstrainedProperty inList(long... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(long... inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.length];
            for (int i = 0; i < inList.length; i++) {
                list[i] = String.valueOf(inList[i]);
            }
        }

        setInList(list);
    }

    public ConstrainedProperty inList(float... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(float... inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.length];
            for (int i = 0; i < inList.length; i++) {
                list[i] = String.valueOf(inList[i]);
            }
        }

        setInList(list);
    }

    public ConstrainedProperty inList(double... inList) {
        setInList(inList);

        return this;
    }

    public void setInList(double... inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.length];
            for (int i = 0; i < inList.length; i++) {
                list[i] = String.valueOf(inList[i]);
            }
        }

        setInList(list);
    }

    public ConstrainedProperty inList(Collection inList) {
        setInList(inList);

        return this;
    }

    public void setInList(Collection inList) {
        String[] list = null;
        if (inList != null) {
            list = new String[inList.size()];
            int i = 0;
            for (Object entry : inList) {
                list[i++] = String.valueOf(entry);
            }
        }

        setInList(list);
    }

    public String[] getInList() {
        return (String[]) constraints_.get(IN_LIST);
    }

    public boolean isInList() {
        return constraints_.containsKey(IN_LIST) && ((String[]) constraints_.get(IN_LIST)).length > 0;
    }

    public ConstrainedProperty rangeBegin(byte value) {
        setRangeBegin(value);

        return this;
    }

    public ConstrainedProperty rangeBegin(char value) {
        setRangeBegin(value);

        return this;
    }

    public ConstrainedProperty rangeBegin(short value) {
        setRangeBegin(value);

        return this;
    }

    public ConstrainedProperty rangeBegin(int value) {
        setRangeBegin(value);

        return this;
    }

    public ConstrainedProperty rangeBegin(long value) {
        setRangeBegin(value);

        return this;
    }

    public ConstrainedProperty rangeBegin(float value) {
        setRangeBegin(value);

        return this;
    }

    public ConstrainedProperty rangeBegin(double value) {
        setRangeBegin(value);

        return this;
    }

    public ConstrainedProperty rangeBegin(Comparable value) {
        setRangeBegin(value);

        return this;
    }

    public void setRangeBegin(Comparable rangeBegin) {
        if (null == rangeBegin) {
            constraints_.remove(RANGE_BEGIN);
        } else {
            setConstraint(RANGE_BEGIN, rangeBegin);
        }
    }

    public Comparable getRangeBegin() {
        return (Comparable) constraints_.get(RANGE_BEGIN);
    }

    public ConstrainedProperty rangeEnd(char value) {
        setRangeEnd(value);

        return this;
    }

    public ConstrainedProperty rangeEnd(byte value) {
        setRangeEnd(value);

        return this;
    }

    public ConstrainedProperty rangeEnd(double value) {
        setRangeEnd(value);

        return this;
    }

    public ConstrainedProperty rangeEnd(float value) {
        setRangeEnd(value);

        return this;
    }

    public ConstrainedProperty rangeEnd(int value) {
        setRangeEnd(value);

        return this;
    }

    public ConstrainedProperty rangeEnd(long value) {
        setRangeEnd(value);

        return this;
    }

    public ConstrainedProperty rangeEnd(short value) {
        setRangeEnd(value);

        return this;
    }

    public ConstrainedProperty rangeEnd(Comparable value) {
        setRangeEnd(value);

        return this;
    }

    public void setRangeEnd(Comparable rangeEnd) {
        if (null == rangeEnd) {
            constraints_.remove(RANGE_END);
        } else {
            setConstraint(RANGE_END, rangeEnd);
        }
    }

    public Comparable getRangeEnd() {
        return (Comparable) constraints_.get(RANGE_END);
    }

    public boolean isRange() {
        return constraints_.containsKey(RANGE_BEGIN) || constraints_.containsKey(RANGE_END);
    }

    public ConstrainedProperty defaultValue(boolean value) {
        return defaultValue(Boolean.valueOf(value));
    }

    public ConstrainedProperty defaultValue(Object value) {
        setDefaultValue(value);

        return this;
    }

    public void setDefaultValue(Object value) {
        if (null == value) {
            constraints_.remove(DEFAULT_VALUE);
        } else {
            setConstraint(DEFAULT_VALUE, value);
        }
    }

    public Object getDefaultValue() {
        return constraints_.get(DEFAULT_VALUE);
    }

    public boolean hasDefaultValue() {
        return constraints_.containsKey(DEFAULT_VALUE);
    }

    public ConstrainedProperty sameAs(String reference) {
        setSameAs(reference);

        return this;
    }

    public void setSameAs(String reference) {
        if (null == reference) {
            constraints_.remove(SAME_AS);
        } else {
            setConstraint(SAME_AS, reference);
        }
    }

    public String getSameAs() {
        return (String) constraints_.get(SAME_AS);
    }

    public boolean isSameAs() {
        return constraints_.containsKey(SAME_AS);
    }

    public void setManyToOne() {
        setConstraint(MANY_TO_ONE, new ManyToOne());
    }

    public void setManyToOne(Class klass) {
        setConstraint(MANY_TO_ONE, new ManyToOne(klass));
    }

    public void setManyToOne(Class klass, String columnReference) {
        setConstraint(MANY_TO_ONE, new ManyToOne(klass, columnReference, null, null));
    }

    public void setManyToOne(String table, String columnReference) {
        setConstraint(MANY_TO_ONE, new ManyToOne(table, columnReference, null, null));
    }

    public void setManyToOne(Class klass, String columnReference, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setConstraint(MANY_TO_ONE, new ManyToOne(klass, columnReference, onUpdate, onDelete));
    }

    public void setManyToOne(String table, String columnReference, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setConstraint(MANY_TO_ONE, new ManyToOne(table, columnReference, onUpdate, onDelete));
    }

    public ManyToOne getManyToOne() {
        return (ManyToOne) constraints_.get(MANY_TO_ONE);
    }

    public ConstrainedProperty manyToOne() {
        setManyToOne();

        return this;
    }

    public ConstrainedProperty manyToOne(Class klass) {
        setManyToOne(klass);

        return this;
    }

    public ConstrainedProperty manyToOne(Class klass, String columnReference) {
        setManyToOne(klass, columnReference);

        return this;
    }

    public ConstrainedProperty manyToOne(String table, String columnReference) {
        setManyToOne(table, columnReference);

        return this;
    }

    public ConstrainedProperty manyToOne(Class klass, String columnReference, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToOne(klass, columnReference, onUpdate, onDelete);

        return this;
    }

    public ConstrainedProperty manyToOne(String table, String columnReference, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToOne(table, columnReference, onUpdate, onDelete);

        return this;
    }

    public boolean hasManyToOne() {
        return constraints_.containsKey(MANY_TO_ONE);
    }

    public void setManyToOneAssociation() {
        setConstraint(MANY_TO_ONE_ASSOCIATION, new ManyToOneAssociation());
    }

    public void setManyToOneAssociation(String property) {
        setConstraint(MANY_TO_ONE_ASSOCIATION, new ManyToOneAssociation(property));
    }

    public void setManyToOneAssociation(Class klass, String property) {
        setConstraint(MANY_TO_ONE_ASSOCIATION, new ManyToOneAssociation(klass, property));
    }

    public ManyToOneAssociation getManyToOneAssociation() {
        return (ManyToOneAssociation) constraints_.get(MANY_TO_ONE_ASSOCIATION);
    }

    public ConstrainedProperty manyToOneAssociation() {
        setManyToOneAssociation();

        return this;
    }

    public ConstrainedProperty manyToOneAssociation(String property) {
        setManyToOneAssociation(property);

        return this;
    }

    public ConstrainedProperty manyToOneAssociation(Class klass, String property) {
        setManyToOneAssociation(klass, property);

        return this;
    }

    public boolean hasManyToOneAssociation() {
        return constraints_.containsKey(MANY_TO_ONE_ASSOCIATION);
    }

    public void setManyToMany() {
        setConstraint(MANY_TO_MANY, new ManyToMany());
    }

    public void setManyToMany(Class klass) {
        setConstraint(MANY_TO_MANY, new ManyToMany(klass));
    }

    public void setManyToMany(CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setConstraint(MANY_TO_MANY, new ManyToMany(onUpdate, onDelete));
    }

    public void setManyToMany(Class klass, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setConstraint(MANY_TO_MANY, new ManyToMany(klass, onUpdate, onDelete));
    }

    public ManyToMany getManyToMany() {
        return (ManyToMany) constraints_.get(MANY_TO_MANY);
    }

    public ConstrainedProperty manyToMany() {
        setManyToMany();

        return this;
    }

    public ConstrainedProperty manyToMany(Class klass) {
        setManyToMany(klass);

        return this;
    }

    public ConstrainedProperty manyToMany(CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToMany(onUpdate, onDelete);

        return this;
    }

    public ConstrainedProperty manyToMany(Class klass, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToMany(klass, onUpdate, onDelete);

        return this;
    }

    public boolean hasManyToMany() {
        return constraints_.containsKey(MANY_TO_MANY);
    }

    public void setManyToManyAssociation() {
        setConstraint(MANY_TO_MANY_ASSOCIATION, new ManyToManyAssociation());
    }

    public void setManyToManyAssociation(String property) {
        setConstraint(MANY_TO_MANY_ASSOCIATION, new ManyToManyAssociation(property));
    }

    public void setManyToManyAssociation(Class klass, String property) {
        setConstraint(MANY_TO_MANY_ASSOCIATION, new ManyToManyAssociation(klass, property));
    }

    public ManyToManyAssociation getManyToManyAssociation() {
        return (ManyToManyAssociation) constraints_.get(MANY_TO_MANY_ASSOCIATION);
    }

    public ConstrainedProperty manyToManyAssociation() {
        setManyToManyAssociation();

        return this;
    }

    public ConstrainedProperty manyToManyAssociation(String property) {
        setManyToManyAssociation(property);

        return this;
    }

    public ConstrainedProperty manyToManyAssociation(Class klass, String property) {
        setManyToManyAssociation(klass, property);

        return this;
    }

    public boolean hasManyToManyAssociation() {
        return constraints_.containsKey(MANY_TO_MANY_ASSOCIATION);
    }

    public ConstrainedProperty format(Format format) {
        setFormat(format);

        return this;
    }

    public void setFormat(Format format) {
        if (null == format) {
            constraints_.remove(FORMAT);
        } else {
            setConstraint(FORMAT, format);
        }
    }

    public Format getFormat() {
        return (Format) constraints_.get(FORMAT);
    }

    public boolean isFormatted() {
        return constraints_.containsKey(FORMAT);
    }

    public ConstrainedProperty file(boolean file) {
        setFile(file);

        return this;
    }

    public void setFile(boolean file) {
        setConstraint(FILE, file);
    }

    public boolean isFile() {
        return Convert.toBoolean(constraints_.get(FILE), false);
    }

    public ConstrainedProperty sparse(boolean sparse) {
        setSparse(sparse);

        return this;
    }

    public void setSparse(boolean sparse) {
        setConstraint(SPARSE, sparse);
    }

    public boolean isSparse() {
        return Convert.toBoolean(constraints_.get(SPARSE), false);
    }

    /**
     * Sets whether the property should be included in data lists.
     * <p>This is not actually used by the CMF itself, but is very useful when
     * integrating with automatic user interface generation libraries.
     *
     * @param listed <code>true</code> if the property should be listed; or
     *               <p><code>false</code> if it shouldn't
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #setListed(boolean)
     * @see #isListed()
     * @since 1.0
     */
    public ConstrainedProperty listed(boolean listed) {
        setListed(listed);

        return this;
    }

    /**
     * Sets whether the property should be included in data lists.
     *
     * @param listed <code>true</code> if the property should be listed; or
     *               <p><code>false</code> if it shouldn't
     * @see #listed(boolean)
     * @see #isListed()
     * @since 1.0
     */
    public void setListed(boolean listed) {
        setConstraint(LISTED, listed);
    }

    /**
     * Retrieves whether the property should be included in data lists.
     *
     * @return <code>true</code> if the property should be listed; or
     * <p><code>false</code> if it shouldn't
     * @see #listed(boolean)
     * @see #setListed(boolean)
     * @since 1.0
     */
    public boolean isListed() {
        return Convert.toBoolean(constraints_.get(LISTED), false);
    }

    /**
     * Sets the position in which the property should be displayed.
     * <p>This is not actually used by the CMF itself, but is very useful when
     * integrating with automatic user interface generation libraries.
     *
     * @param position an integer value with the position; or
     *                 <p><code>-1</code> if the property shouldn't be positioned
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #setPosition(int)
     * @see #hasPosition()
     * @see #getPosition()
     * @since 1.0
     */
    public ConstrainedProperty position(int position) {
        setPosition(position);

        return this;
    }

    /**
     * Sets the position in which the property should be displayed.
     *
     * @param position an integer value with the position; or
     *                 <p><code>-1</code> if the property shouldn't be positioned
     * @see #position(int)
     * @see #hasPosition()
     * @see #getPosition()
     * @since 1.0
     */
    public void setPosition(int position) {
        if (position < 0) {
            constraints_.remove(POSITION);
        } else {
            setConstraint(POSITION, position);
        }
    }

    /**
     * Indicates whether the position of the property is set.
     *
     * @return <code>true</code> if the property has a position; or
     * <p><code>false</code> if it hasn't
     * @see #position(int)
     * @see #setPosition(int)
     * @see #getPosition()
     * @since 1.0
     */
    public boolean hasPosition() {
        return constraints_.containsKey(POSITION);
    }

    /**
     * Retrieves the position in which the property should be displayed.
     *
     * @return an integer value with the position; or
     * <p><code>-1</code> if the property shouldn't be positioned
     * @see #position(int)
     * @see #setPosition(int)
     * @see #hasPosition()
     * @since 1.0
     */
    public int getPosition() {
        return Convert.toInt(constraints_.get(POSITION), -1);
    }

    /**
     * Sets the mime type of the property.
     * <p>Setting this constraint will make the {@link
     * rife.cmf.dam.ContentQueryManager ContentQueryManager}
     * automatically store the data in this property in the content management
     * back-end. This column will not be stored in a regular database table.
     * All this is handled transparently and automatically.
     *
     * @param mimeType the <code>MimeType</code> of the property
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #setMimeType(MimeType)
     * @see #hasMimeType()
     * @see #getMimeType()
     * @since 1.0
     */
    public ConstrainedProperty mimeType(MimeType mimeType) {
        setMimeType(mimeType);

        return this;
    }

    /**
     * Sets the mime type of the property.
     *
     * @param mimeType the <code>MimeType</code> of the property
     * @see #mimeType(MimeType)
     * @see #hasMimeType()
     * @see #getMimeType()
     * @since 1.0
     */
    public void setMimeType(MimeType mimeType) {
        if (null == mimeType) {
            constraints_.remove(MIMETYPE);
        } else {
            setConstraint(MIMETYPE, mimeType);
            persistent(false);
            displayedRaw(true);
        }
    }

    /**
     * Indicates whether the property has a mime type.
     *
     * @return <code>true</code> if the property has a mime type; or
     * <p><code>false</code> if it hasn't
     * @see #mimeType(MimeType)
     * @see #setMimeType(MimeType)
     * @see #getMimeType()
     * @since 1.0
     */
    public boolean hasMimeType() {
        return constraints_.containsKey(MIMETYPE);
    }

    /**
     * Retrieves the mime type of the property.
     *
     * @return the mime type of the property; or
     * <p><code>null</code> if the property has no mime type
     * @see #mimeType(MimeType)
     * @see #setMimeType(MimeType)
     * @see #hasMimeType()
     * @since 1.0
     */
    public MimeType getMimeType() {
        return (MimeType) constraints_.get(MIMETYPE);
    }

    /**
     * Sets whether the content data of this property should be retrieved
     * automatically from the back-end.
     * <p>This is only useful when the property also has a mime type
     * constraint.
     * <p>It's not recommended to enable this constraint for large data since
     * everything will be stored in memory, only use this for text snippets or
     * something relatively small.
     *
     * @param autoRetrieved <code>true</code> if the data should be
     *                      automatically retrieved; or
     *                      <p><code>false</code> otherwise
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #mimeType(MimeType)
     * @see #setAutoRetrieved(boolean)
     * @see #isAutoRetrieved()
     * @since 1.0
     */
    public ConstrainedProperty autoRetrieved(boolean autoRetrieved) {
        setAutoRetrieved(autoRetrieved);

        return this;
    }

    /**
     * Sets whether the content data of this property should be retrieved
     * automatically from the back-end.
     *
     * @param autoRetrieved <code>true</code> if the data should be
     *                      automatically retrieved; or
     *                      <p><code>false</code> otherwise
     * @see #autoRetrieved(boolean)
     * @see #isAutoRetrieved()
     * @since 1.0
     */
    public void setAutoRetrieved(boolean autoRetrieved) {
        setConstraint(AUTO_RETRIEVED, autoRetrieved);
    }

    /**
     * Indicates whether the content data of this property is automatically
     * retrieved from the back-end.
     *
     * @return <code>true</code> if the data should be automatically
     * retrieved; or
     * <p><code>false</code> otherwise
     * @see #autoRetrieved(boolean)
     * @see #setAutoRetrieved(boolean)
     * @since 1.0
     */
    public boolean isAutoRetrieved() {
        return Convert.toBoolean(constraints_.get(AUTO_RETRIEVED), false);
    }

    /**
     * Sets whether the content data of this property is a fragment.
     * <p>This is only useful when the property also has a mime type
     * constraint. A fragment means that it's not a complete document or a
     * file, but rather a small part that is intended to be used within a
     * larger document. For example a HTML snippet. This information is for
     * example important when validating the data.
     *
     * @param fragment <code>true</code> if the content is a fragment; or
     *                 <p><code>false</code> otherwise
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #mimeType(MimeType)
     * @see #setFragment(boolean)
     * @see #isFragment()
     * @since 1.0
     */
    public ConstrainedProperty fragment(boolean fragment) {
        setFragment(fragment);

        return this;
    }

    /**
     * Sets whether the content data of this property is a fragment.
     *
     * @param fragment <code>true</code> if the content is a fragment; or
     *                 <p><code>false</code> otherwise
     * @see #fragment(boolean)
     * @see #isFragment()
     * @since 1.0
     */
    public void setFragment(boolean fragment) {
        setConstraint(FRAGMENT, fragment);
    }

    /**
     * Indicates whether the content data of this property is a fragment.
     *
     * @return <code>true</code> if the content is a fragment; or
     * <p><code>false</code> otherwise
     * @see #fragment(boolean)
     * @see #setFragment(boolean)
     * @since 1.0
     */
    public boolean isFragment() {
        return Convert.toBoolean(constraints_.get(FRAGMENT), false);
    }

    /**
     * Sets the name of the content data of this property.
     * <p>This is only useful when the property also has a mime type
     * constraint.
     *
     * @param name the name
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #setName(String)
     * @see #getName()
     * @see #hasName()
     * @since 1.0
     */
    public ConstrainedProperty name(String name) {
        setName(name);

        return this;
    }

    /**
     * Sets the name of the content data of this property.
     *
     * @param name the name
     * @see #name(String)
     * @see #getName()
     * @see #hasName()
     * @since 1.0
     */
    public void setName(String name) {
        if (null == name) {
            constraints_.remove(NAME);
        } else {
            setConstraint(NAME, name);
        }
    }

    /**
     * Retrieves the name of this property.
     *
     * @return <code>null</code> if the content data has no name; or
     * <p>the name of the content
     * @see #name(String)
     * @see #setName(String)
     * @see #hasName()
     * @since 1.0
     */
    public String getName() {
        return (String) constraints_.get(NAME);
    }

    /**
     * Indicates whether this property has a name.
     *
     * @return <code>true</code> if the property has a name; or
     * <p><code>false</code> otherwise
     * @see #name(String)
     * @see #setName(String)
     * @see #getName()
     * @since 1.0
     */
    public boolean hasName() {
        return constraints_.containsKey(NAME);
    }

    /**
     * Sets the repository where the content data of this property will be
     * stored.
     * <p>This is only useful when the property also has a mime type
     * constraint.
     *
     * @param repository the repository
     * @return the current <code>CmrProperty</code> instance
     * @see #mimeType(MimeType)
     * @see #setRepository(String)
     * @see #getRepository()
     * @see #hasRepository()
     * @since 1.0
     */
    public ConstrainedProperty repository(String repository) {
        setRepository(repository);

        return this;
    }

    /**
     * Sets the repository where the content data of this property will be
     * stored.
     *
     * @param repository the repository
     * @see #repository(String)
     * @see #getRepository()
     * @see #hasRepository()
     * @since 1.0
     */
    public void setRepository(String repository) {
        if (null == repository) {
            constraints_.remove(REPOSITORY);
        } else {
            setConstraint(REPOSITORY, repository);
        }
    }

    /**
     * Retrieves the repository where the content data of this property will
     * be stored.
     *
     * @return <code>null</code> if no repository has been specified; or
     * <p>the name of the repository
     * @see #repository(String)
     * @see #setRepository(String)
     * @see #hasRepository()
     * @since 1.0
     */
    public String getRepository() {
        return (String) constraints_.get(REPOSITORY);
    }

    /**
     * Indicates whether this property will be stored in another repository
     * than the default repository.
     *
     * @return <code>true</code> if the property will be stored in another
     * repository; or
     * <p><code>false</code> otherwise
     * @see #repository(String)
     * @see #setRepository(String)
     * @see #getRepository()
     * @since 1.0
     */
    public boolean hasRepository() {
        return constraints_.containsKey(REPOSITORY);
    }

    /**
     * Sets whether this property has to be used as an ordinal.
     * <p>The value of this property will be handled in the back-end by an
     * {@link rife.cmf.dam.OrdinalManager OrdinalManager}. It will
     * also enable the {@link
     * rife.cmf.dam.ContentQueryManager#move(Constrained, String, rife.cmf.dam.OrdinalManager.Direction)
     * move}, {@link
     * rife.cmf.dam.ContentQueryManager#up(Constrained, String) up}
     * and {@link
     * rife.cmf.dam.ContentQueryManager#down(Constrained, String)
     * down} methods in the {@link rife.cmf.dam.ContentQueryManager
     * ContentQueryManager} to easily reorder data rows in the back-end.
     *
     * @param ordinal <code>true</code> if this property is an ordinal; or
     *                <p><code>false</code> otherwise
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #ordinal(boolean, String)
     * @see #setOrdinal(boolean)
     * @see #setOrdinal(boolean, String)
     * @see #isOrdinal()
     * @since 1.0
     */
    public ConstrainedProperty ordinal(boolean ordinal) {
        setOrdinal(ordinal);

        return this;
    }

    /**
     * Sets whether this property has to be used as an ordinal with a
     * restricting column.
     *
     * @param ordinal     <code>true</code> if this property is an ordinal; or
     *                    <p><code>false</code> otherwise
     * @param restriction the name of the restricting column
     * @return the current <code>ConstrainedProperty</code> instance
     * @see #ordinal(boolean)
     * @see #setOrdinal(boolean)
     * @see #setOrdinal(boolean, String)
     * @see #isOrdinal()
     * @see #hasOrdinalRestriction()
     * @see #getOrdinalRestriction()
     * @since 1.0
     */
    public ConstrainedProperty ordinal(boolean ordinal, String restriction) {
        setOrdinal(ordinal, restriction);

        return this;
    }


    /**
     * Sets whether this property has to be used as an ordinal.
     *
     * @param ordinal <code>true</code> if this property is an ordinal; or
     *                <p><code>false</code> otherwise
     * @see #ordinal(boolean)
     * @see #ordinal(boolean, String)
     * @see #setOrdinal(boolean, String)
     * @see #isOrdinal()
     * @since 1.0
     */
    public void setOrdinal(boolean ordinal) {
        setConstraint(ORDINAL, ordinal);
        constraints_.remove(ORDINAL_RESTRICTION);
    }

    /**
     * Sets whether this property has to be used as an ordinal with a
     * restricting column.
     *
     * @param ordinal     <code>true</code> if this property is an ordinal; or
     *                    <p><code>false</code> otherwise
     * @param restriction the name of the restricting column
     * @see #ordinal(boolean)
     * @see #ordinal(boolean, String)
     * @see #setOrdinal(boolean)
     * @see #isOrdinal()
     * @see #hasOrdinalRestriction()
     * @see #getOrdinalRestriction()
     * @since 1.0
     */
    public void setOrdinal(boolean ordinal, String restriction) {
        setConstraint(ORDINAL, ordinal);

        if (ordinal) {
            if (null == restriction) {
                constraints_.remove(ORDINAL_RESTRICTION);
            } else {
                setConstraint(ORDINAL_RESTRICTION, restriction);
            }
        } else {
            constraints_.remove(ORDINAL_RESTRICTION);
        }
    }

    /**
     * Indicates whether this property has to be used as an ordinal.
     *
     * @return <code>true</code> if this property is an ordinal; or
     * <p><code>false</code> otherwise
     * @see #ordinal(boolean)
     * @see #ordinal(boolean, String)
     * @see #setOrdinal(boolean)
     * @see #setOrdinal(boolean, String)
     * @since 1.0
     */
    public boolean isOrdinal() {
        return Convert.toBoolean(constraints_.get(ORDINAL), false);
    }

    /**
     * Indicates whether this property has an ordinal restricting column.
     *
     * @return <code>true</code> if this property has an ordinal restricting
     * column; or
     * <p><code>false</code> otherwise
     * @see #ordinal(boolean, String)
     * @see #setOrdinal(boolean, String)
     * @see #getOrdinalRestriction()
     * @since 1.0
     */
    public boolean hasOrdinalRestriction() {
        return constraints_.containsKey(ORDINAL_RESTRICTION);
    }

    /**
     * Retrieves the ordinal restriction of this property.
     *
     * @return the name of the ordinal restricting column; or
     * <p><code>null</code> if no ordinal restricting column has been defined
     * @see #ordinal(boolean, String)
     * @see #setOrdinal(boolean, String)
     * @see #hasOrdinalRestriction()
     * @since 1.0
     */
    public String getOrdinalRestriction() {
        return (String) constraints_.get(ORDINAL_RESTRICTION);
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, boolean value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, char value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, byte value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, short value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, int value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, long value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, float value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property that will be converted
     * internally to a <code>String</code> value.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #contentAttribute(String, String)
     * @see #getContentAttributes()
     * @since 1.0
     */
    public ConstrainedProperty contentAttribute(String name, double value) {
        return contentAttribute(name, String.valueOf(value));
    }

    /**
     * Sets a named content attribute for this property.
     * <p>This is only useful when the property also has a mime type
     * constraint.
     * <p>A content attribute provides additional meta data about how you want
     * to store the content data after loading, this can for example be image
     * dimensions.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current <code>Content</code> instance
     * @see #mimeType(MimeType)
     * @see #getContentAttributes()
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public ConstrainedProperty contentAttribute(String name, String value) {
        HashMap<String, String> content_attributes = (HashMap<String, String>) constraints_.get(CONTENT_ATTRIBUTES);
        if (null == content_attributes) {
            content_attributes = new HashMap<>();
            setConstraint(CONTENT_ATTRIBUTES, content_attributes);
        }
        content_attributes.put(name, value);

        return this;
    }

    /**
     * Retrieves the map of named content attributes for this property.
     *
     * @return the map of named content attributes; or
     * <p><code>null</code> if no attributes are present
     * @see #contentAttribute(String, String)
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getContentAttributes() {
        return (HashMap<String, String>) constraints_.get(CONTENT_ATTRIBUTES);
    }

    /**
     * Sets a content transformer for this property.
     * <p>This is only useful when the property also has a mime type
     * constraint.
     *
     * @param transformer the content transformer
     * @return the current <code>Content</code> instance
     * @see #mimeType(MimeType)
     * @see #setTransformer(ContentTransformer)
     * @see #hasTransformer()
     * @see #getTransformer()
     * @since 1.0
     */
    public ConstrainedProperty transformer(ContentTransformer<?> transformer) {
        setTransformer(transformer);

        return this;
    }

    /**
     * Sets a content transformer for this property.
     *
     * @param transformer the content transformer
     * @see #mimeType(MimeType)
     * @see #transformer(ContentTransformer)
     * @see #hasTransformer()
     * @see #getTransformer()
     * @since 1.0
     */
    public void setTransformer(ContentTransformer<?> transformer) {
        if (null == transformer) {
            constraints_.remove(TRANSFORMER);
        } else {
            setConstraint(TRANSFORMER, transformer);
        }
    }

    /**
     * Indicates whether this property has a content transformer.
     *
     * @return <code>true</code> if this property has a content transformer;
     * or
     * <p><code>false</code> otherwise
     * @see #transformer(ContentTransformer)
     * @see #setTransformer(ContentTransformer)
     * @see #getTransformer()
     * @since 1.0
     */
    public boolean hasTransformer() {
        return constraints_.containsKey(TRANSFORMER);
    }

    /**
     * Retrieves the content transformer of this property.
     *
     * @return the requested content transformer; or
     * <p><code>null</code> if no content transformer has been defined
     * @see #transformer(ContentTransformer)
     * @see #setTransformer(ContentTransformer)
     * @see #hasTransformer()
     * @since 1.0
     */
    public ContentTransformer<?> getTransformer() {
        return (ContentTransformer<?>) constraints_.get(TRANSFORMER);
    }

    /**
     * Sets the cached loaded data.
     * <p>This is used internally and should never be used explicitly by a
     * developer, see {@link
     * rife.cmf.Content#cachedLoadedData(Object)
     * Content.cachedLoadedData(Object)} for more information.
     *
     * @param data the loaded data
     * @see #getCachedLoadedData()
     * @since 1.0
     */
    public void setCachedLoadedData(Object data) {
        if (null == data) {
            synchronized (constraints_) {
                constraints_.remove(CACHED_LOADED_DATA);
            }
        } else {
            setConstraint(CACHED_LOADED_DATA, data);
        }
    }

    /**
     * Retrieves the cached loaded content data.
     *
     * @return the cached loaded content data; or
     * <p><code>null</code> if no loaded content data has been cached
     * @see #setCachedLoadedData(Object)
     * @since 1.0
     */
    public Object getCachedLoadedData() {
        return constraints_.get(CACHED_LOADED_DATA);
    }

    /**
     * Sets the data of a particular constraint in a generic
     * fashion.
     * <p>Note that it's not recommended to use this to set any of
     * the standard constraints since none of the additional logic
     * and checks are executed.
     *
     * @see #constraint
     * @see #getConstraint
     * @see #getConstraints
     * @since 1.0
     */
    public void setConstraint(String name, Object constraintData) {
        synchronized (constraints_) {
            constraints_.put(name, constraintData);
        }
        fireConstraintSet(name, constraintData);
    }

    /**
     * Sets the data of a particular constraint in a generic
     * fashion.
     * <p> Note that it's not recommended to use this to set any of
     * the standard constraints since none of the additional logic
     * and checks are executed.
     *
     * @return the current <code>Content</code> instance
     * @see #setConstraint
     * @see #getConstraint
     * @see #getConstraints
     * @since 1.0
     */
    public ConstrainedProperty constraint(String name, Object constraintData) {
        setConstraint(name, constraintData);

        return this;
    }

    /**
     * Retrieves the value of a particular constraint in a
     * generic fashion
     *
     * @return the data of a particular constraint; or
     * <p><code>null</code> if nothing has been registered for
     * that constraint
     * @see #setConstraint
     * @see #constraint
     * @see #getConstraints
     * @since 1.0
     */
    public Object getConstraint(String name) {
        return constraints_.get(name);
    }

    /**
     * Retrieves the map of all the constraints.
     *
     * @return the map with all the registered constraints
     * @see #setConstraint
     * @see #constraint
     * @see #getConstraint
     * @since 1.0
     */
    public Map<String, Object> getConstraints() {
        return constraints_;
    }

    public ConstrainedProperty clone() {
        ConstrainedProperty new_instance = null;
        try {
            new_instance = (ConstrainedProperty) super.clone();

            new_instance.constraints_ = new LinkedHashMap<>(constraints_);

            if (listeners_ != null) {
                new_instance.listeners_ = new ArrayList<>(listeners_);
            }
        } catch (CloneNotSupportedException e) {
            new_instance = null;
        }

        return new_instance;
    }

    public class ManyToOne implements Cloneable {
        private String column_ = null;
        private String table_ = null;
        private String derivedTable_ = null;
        private Class class_ = null;
        private CreateTable.ViolationAction onUpdate_ = null;
        private CreateTable.ViolationAction onDelete_ = null;

        public ManyToOne() {
            this((Class) null, null, null, null);
        }

        public ManyToOne(Class klass) {
            this(klass, null, null, null);
        }

        public ManyToOne(String table, String column, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            column_ = column;
            table_ = table;
            onUpdate_ = onUpdate;
            onDelete_ = onDelete;
        }

        public ManyToOne(Class klass, String column, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            this((String) null, column, onUpdate, onDelete);
            class_ = klass;
        }

        public String getDerivedTable() {
            if (null == derivedTable_) {
                if (table_ != null) {
                    derivedTable_ = table_;
                }

                if (class_ != null) {
                    derivedTable_ = ClassUtils.shortenClassName(class_);
                }

            }

            return derivedTable_;
        }

        public void setColumn(String column) {
            column_ = column;
        }

        public String getColumn() {
            return column_;
        }

        public void setTable(String table) {
            derivedTable_ = null;
            table_ = table;
        }

        public String getTable() {
            return table_;
        }

        public void setAssociatedClass(Class klass) {
            derivedTable_ = null;
            class_ = klass;
        }

        public Class getAssociatedClass() {
            return class_;
        }

        public void setOnUpdate(CreateTable.ViolationAction onUpdate) {
            onUpdate_ = onUpdate;
        }

        public CreateTable.ViolationAction getOnUpdate() {
            return onUpdate_;
        }

        public void setOnDelete(CreateTable.ViolationAction onDelete) {
            onDelete_ = onDelete;
        }

        public CreateTable.ViolationAction getOnDelete() {
            return onDelete_;
        }

        public ManyToOne clone() {
            ManyToOne new_instance = null;
            try {
                new_instance = (ManyToOne) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public class ManyToOneAssociation implements Cloneable {
        private Class class_ = null;
        private String property_ = null;

        public ManyToOneAssociation() {
        }

        public ManyToOneAssociation(String property) {
            property_ = property;
        }

        public ManyToOneAssociation(Class klass, String property) {
            this(property);
            class_ = klass;
        }

        public void setMainClass(Class klass) {
            class_ = klass;
        }

        public Class getMainClass() {
            return class_;
        }

        public void setMainProperty(String property) {
            property_ = property;
        }

        public String getMainProperty() {
            return property_;
        }

        public ManyToOneAssociation clone() {
            ManyToOneAssociation new_instance = null;
            try {
                new_instance = (ManyToOneAssociation) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public class ManyToMany implements Cloneable {
        private Class class_ = null;
        private CreateTable.ViolationAction onUpdate_ = null;
        private CreateTable.ViolationAction onDelete_ = null;

        public ManyToMany() {
            this(null, null, null);
        }

        public ManyToMany(Class klass) {
            this(klass, null, null);
        }

        public ManyToMany(CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            this(null, onUpdate, onDelete);
            onUpdate_ = onUpdate;
            onDelete_ = onDelete;
        }

        public ManyToMany(Class klass, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            onUpdate_ = onUpdate;
            onDelete_ = onDelete;
            class_ = klass;
        }

        public void setAssociatedClass(Class klass) {
            class_ = klass;
        }

        public Class getAssociatedClass() {
            return class_;
        }

        public void setOnUpdate(CreateTable.ViolationAction onUpdate) {
            onUpdate_ = onUpdate;
        }

        public CreateTable.ViolationAction getOnUpdate() {
            return onUpdate_;
        }

        public void setOnDelete(CreateTable.ViolationAction onDelete) {
            onDelete_ = onDelete;
        }

        public CreateTable.ViolationAction getOnDelete() {
            return onDelete_;
        }

        public ManyToMany clone() {
            ManyToMany new_instance = null;
            try {
                new_instance = (ManyToMany) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public class ManyToManyAssociation implements Cloneable {
        private Class class_ = null;
        private String property_ = null;

        public ManyToManyAssociation() {
        }

        public ManyToManyAssociation(String property) {
            property_ = property;
        }

        public ManyToManyAssociation(Class klass, String property) {
            this(property);
            class_ = klass;
        }

        public void setAssociatedClass(Class klass) {
            class_ = klass;
        }

        public Class getAssociatedClass() {
            return class_;
        }

        public void setAssociatedProperty(String property) {
            property_ = property;
        }

        public String getAssociatedProperty() {
            return property_;
        }

        public ManyToManyAssociation clone() {
            ManyToManyAssociation new_instance = null;
            try {
                new_instance = (ManyToManyAssociation) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }
}

