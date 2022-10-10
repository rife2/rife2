/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.*;

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
public class ConstrainedProperty<T extends ConstrainedProperty> implements Cloneable {
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
    protected Map<String, Object> mConstraints = new HashMap<>();

    // listeners
    protected List<ConstrainedPropertyListener> mListeners;

    /**
     * Adds a new listener.
     * <p>
     * Listeners will be notified when events occur that are specified in the
     * {@code ConstrainedPropertyListener} interface.
     *
     * @param listener the listener instance that will be added
     * @since 1.6
     */
    public void addListener(ConstrainedPropertyListener listener) {
        if (null == listener) {
            return;
        }

        if (null == mListeners) {
            mListeners = new ArrayList<ConstrainedPropertyListener>();
        }

        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
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
     * @since 1.6
     */
    public boolean removeListener(ConstrainedPropertyListener listener) {
        if (null == mListeners) {
            return false;
        }

        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    private void fireConstraintSet(String name, Object constraintData) {
        if (null == mListeners) {
            return;
        }

        synchronized (mListeners) {
            for (ConstrainedPropertyListener listener : mListeners) {
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
    public T subjectName(String name) {
        setSubjectName(name);

        return (T) this;
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
    public T notNull(boolean notNull) {
        setNotNull(notNull);

        return (T) this;
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
        return Convert.toBoolean(mConstraints.get(NOT_NULL), false);
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
    public T notEmpty(boolean notEmpty) {
        setNotEmpty(notEmpty);

        return (T) this;
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
        return Convert.toBoolean(mConstraints.get(NOT_EMPTY), false);
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
    public T notEqual(boolean reference) {
        setNotEqual(reference);

        return (T) this;
    }

    /**
     * Set that the property value can't be equal to a specified
     * <code>Object</code> reference value.
     *
     * @see #notEqual(boolean)
     * @since 1.0
     */
    public T notEqual(Object reference) {
        setNotEqual(reference);

        return (T) this;
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
            mConstraints.remove(NOT_EQUAL);
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
        return mConstraints.containsKey(NOT_EQUAL);
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
        return mConstraints.get(NOT_EQUAL);
    }

    /**
     * Set whether the property value has to be unique.
     * <p>Note that this is only applicable to contexts where a collection
     * of the data is stored an that uniqueness can apply against the
     * other entries. In a singular context, uniqueness is always
     * guaranteed.
     *
     * @param unique <code>true</code> when the value has to be unique; or
     *               <code>false</code> when it doesn't have to be.
     * @return this <code>ConstrainedProperty</code>
     * @see #isUnique()
     * @since 1.0
     */
    public T unique(boolean unique) {
        setUnique(unique);

        return (T) this;
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
        return Convert.toBoolean(mConstraints.get(UNIQUE), false);
    }

    /**
     * Set whether the property value is an identifier.
     * <p>Note that this is only applicable to contexts where a collection
     * of the data is stored an that identification can apply against the
     * other entries. In a singular context, identification is
     * meaningless.
     *
     * @param identifier <code>true</code> when the value is an
     *                   identifier; or <code>false</code> when it isn't.
     * @return this <code>ConstrainedProperty</code>
     * @see #isIdentifier()
     * @since 1.0
     */
    public T identifier(boolean identifier) {
        setIdentifier(identifier);

        return (T) this;
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
        return Convert.toBoolean(mConstraints.get(IDENTIFIER), false);
    }

    public T editable(boolean editable) {
        setEditable(editable);

        return (T) this;
    }

    public void setEditable(boolean editable) {
        setConstraint(EDITABLE, editable);
    }

    public boolean isEditable() {
        return Convert.toBoolean(mConstraints.get(EDITABLE), true);
    }

    public T persistent(boolean persistent) {
        setPersistent(persistent);

        return (T) this;
    }

    public void setPersistent(boolean persistent) {
        // TODO : cmf
//        if (hasMimeType() && persistent) {
//            throw new IllegalArgumentException("Can't make a property persistent that has a content mime type assigned to it.");
//        }

        setConstraint(PERSISTENT, persistent);
    }

    public boolean isPersistent() {
        return Convert.toBoolean(mConstraints.get(PERSISTENT), true);
    }

    public T saved(boolean saved) {
        setSaved(saved);

        return (T) this;
    }

    public void setSaved(boolean saved) {
        setConstraint(SAVED, saved);
    }

    public boolean isSaved() {
        return Convert.toBoolean(mConstraints.get(SAVED), true);
    }

    public T displayedRaw(boolean displayedRaw) {
        setDisplayedRaw(displayedRaw);

        return (T) this;
    }

    public void setDisplayedRaw(boolean displayedRaw) {
        // TODO : cmf
//        if (hasMimeType() && !displayedRaw) {
//            throw new IllegalArgumentException("Can't make a property not being displayed raw that has a content mime type assigned to it.");
//        }

        setConstraint(DISPLAYED_RAW, displayedRaw);
    }

    public boolean isDisplayedRaw() {
        return Convert.toBoolean(mConstraints.get(DISPLAYED_RAW), false);
    }

    public boolean hasLimitedLength() {
        return mConstraints.containsKey(MIN_LENGTH) || mConstraints.containsKey(MAX_LENGTH);
    }

    public boolean hasMixLength() {
        return mConstraints.containsKey(MAX_LENGTH);
    }

    public boolean hasMaxLength() {
        return mConstraints.containsKey(MAX_LENGTH);
    }

    public T minLength(int minLength) {
        setMinLength(minLength);

        return (T) this;
    }

    public void setMinLength(int minLength) {
        if (minLength <= 0) {
            mConstraints.remove(MIN_LENGTH);
        } else {
            setConstraint(MIN_LENGTH, minLength);
        }
    }

    public int getMinLength() {
        return Convert.toInt(mConstraints.get(MIN_LENGTH), -1);
    }

    public T maxLength(int maxLength) {
        setMaxLength(maxLength);

        return (T) this;
    }

    public void setMaxLength(int maxLength) {
        if (maxLength < 0) {
            mConstraints.remove(MAX_LENGTH);
        } else {
            setConstraint(MAX_LENGTH, maxLength);
        }
    }

    public int getMaxLength() {
        return Convert.toInt(mConstraints.get(MAX_LENGTH), -1);
    }

    public boolean hasPrecision() {
        return mConstraints.containsKey(MAX_LENGTH);
    }

    public T precision(int precision) {
        setPrecision(precision);

        return (T) this;
    }

    public void setPrecision(int precision) {
        setMaxLength(precision);
    }

    public int getPrecision() {
        return getMaxLength();
    }

    public boolean hasScale() {
        return mConstraints.containsKey(SCALE);
    }

    public T scale(int scale) {
        setScale(scale);

        return (T) this;
    }

    public void setScale(int scale) {
        if (scale < 0) {
            mConstraints.remove(SCALE);
        } else {
            setConstraint(SCALE, scale);
        }
    }

    public int getScale() {
        return Convert.toInt(mConstraints.get(SCALE), -1);
    }

    public T regexp(String regexp) {
        setRegexp(regexp);

        return (T) this;
    }

    public void setRegexp(String regexp) {
        if (null == regexp) {
            mConstraints.remove(REGEXP);
        } else {
            setConstraint(REGEXP, regexp);
        }
    }

    public String getRegexp() {
        return (String) mConstraints.get(REGEXP);
    }

    public boolean matchesRegexp() {
        return mConstraints.containsKey(REGEXP);
    }

    public T email(boolean email) {
        setEmail(email);

        return (T) this;
    }

    public void setEmail(boolean email) {
        setConstraint(EMAIL, email);
    }

    public boolean isEmail() {
        return Convert.toBoolean(mConstraints.get(EMAIL), false);
    }

    public T url(boolean url) {
        setUrl(url);

        return (T) this;
    }

    public void setUrl(boolean url) {
        setConstraint(URL, url);
    }

    public boolean isUrl() {
        return Convert.toBoolean(mConstraints.get(URL), false);
    }

    public T minDate(Date minDate) {
        setMinDate(minDate);

        return (T) this;
    }

    public void setMinDate(Date minDate) {
        if (null == minDate) {
            mConstraints.remove(MIN_DATE);
        } else {
            setConstraint(MIN_DATE, minDate);
        }
    }

    public Date getMinDate() {
        return (Date) mConstraints.get(MIN_DATE);
    }

    public T maxDate(Date maxDate) {
        setMaxDate(maxDate);

        return (T) this;
    }

    public void setMaxDate(Date maxDate) {
        if (null == maxDate) {
            mConstraints.remove(MAX_DATE);
        } else {
            setConstraint(MAX_DATE, maxDate);
        }
    }

    public Date getMaxDate() {
        return (Date) mConstraints.get(MAX_DATE);
    }

    public boolean isLimitedDate() {
        return mConstraints.containsKey(MIN_DATE) || mConstraints.containsKey(MAX_DATE);
    }

    public T inList(String... inList) {
        setInList(inList);

        return (T) this;
    }

    public void setInList(String... inList) {
        if (null == inList) {
            mConstraints.remove(IN_LIST);
        } else {
            setConstraint(IN_LIST, inList);
        }
    }

    public T inList(int... inList) {
        setInList(inList);

        return (T) this;
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

    public T inList(byte... inList) {
        setInList(inList);

        return (T) this;
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

    public T inList(char... inList) {
        setInList(inList);

        return (T) this;
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

    public T inList(short... inList) {
        setInList(inList);

        return (T) this;
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

    public T inList(long... inList) {
        setInList(inList);

        return (T) this;
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

    public T inList(float... inList) {
        setInList(inList);

        return (T) this;
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

    public T inList(double... inList) {
        setInList(inList);

        return (T) this;
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

    public T inList(Collection inList) {
        setInList(inList);

        return (T) this;
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
        return (String[]) mConstraints.get(IN_LIST);
    }

    public boolean isInList() {
        return mConstraints.containsKey(IN_LIST) && ((String[]) mConstraints.get(IN_LIST)).length > 0;
    }

    public T rangeBegin(byte value) {
        setRangeBegin(value);

        return (T) this;
    }

    public T rangeBegin(char value) {
        setRangeBegin(value);

        return (T) this;
    }

    public T rangeBegin(short value) {
        setRangeBegin(value);

        return (T) this;
    }

    public T rangeBegin(int value) {
        setRangeBegin(value);

        return (T) this;
    }

    public T rangeBegin(long value) {
        setRangeBegin(value);

        return (T) this;
    }

    public T rangeBegin(float value) {
        setRangeBegin(value);

        return (T) this;
    }

    public T rangeBegin(double value) {
        setRangeBegin(value);

        return (T) this;
    }

    public T rangeBegin(Comparable value) {
        setRangeBegin(value);

        return (T) this;
    }

    public void setRangeBegin(Comparable rangeBegin) {
        if (null == rangeBegin) {
            mConstraints.remove(RANGE_BEGIN);
        } else {
            setConstraint(RANGE_BEGIN, rangeBegin);
        }
    }

    public Comparable getRangeBegin() {
        return (Comparable) mConstraints.get(RANGE_BEGIN);
    }

    public T rangeEnd(char value) {
        setRangeEnd(value);

        return (T) this;
    }

    public T rangeEnd(byte value) {
        setRangeEnd(value);

        return (T) this;
    }

    public T rangeEnd(double value) {
        setRangeEnd(value);

        return (T) this;
    }

    public T rangeEnd(float value) {
        setRangeEnd(value);

        return (T) this;
    }

    public T rangeEnd(int value) {
        setRangeEnd(value);

        return (T) this;
    }

    public T rangeEnd(long value) {
        setRangeEnd(value);

        return (T) this;
    }

    public T rangeEnd(short value) {
        setRangeEnd(value);

        return (T) this;
    }

    public T rangeEnd(Comparable value) {
        setRangeEnd(value);

        return (T) this;
    }

    public void setRangeEnd(Comparable rangeEnd) {
        if (null == rangeEnd) {
            mConstraints.remove(RANGE_END);
        } else {
            setConstraint(RANGE_END, rangeEnd);
        }
    }

    public Comparable getRangeEnd() {
        return (Comparable) mConstraints.get(RANGE_END);
    }

    public boolean isRange() {
        return mConstraints.containsKey(RANGE_BEGIN) || mConstraints.containsKey(RANGE_END);
    }

    public T defaultValue(boolean value) {
        return defaultValue(Boolean.valueOf(value));
    }

    public T defaultValue(Object value) {
        setDefaultValue(value);

        return (T) this;
    }

    public void setDefaultValue(Object value) {
        if (null == value) {
            mConstraints.remove(DEFAULT_VALUE);
        } else {
            setConstraint(DEFAULT_VALUE, value);
        }
    }

    public Object getDefaultValue() {
        return mConstraints.get(DEFAULT_VALUE);
    }

    public boolean hasDefaultValue() {
        return mConstraints.containsKey(DEFAULT_VALUE);
    }

    public T sameAs(String reference) {
        setSameAs(reference);

        return (T) this;
    }

    public void setSameAs(String reference) {
        if (null == reference) {
            mConstraints.remove(SAME_AS);
        } else {
            setConstraint(SAME_AS, reference);
        }
    }

    public String getSameAs() {
        return (String) mConstraints.get(SAME_AS);
    }

    public boolean isSameAs() {
        return mConstraints.containsKey(SAME_AS);
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
        return (ManyToOne) mConstraints.get(MANY_TO_ONE);
    }

    public T manyToOne() {
        setManyToOne();

        return (T) this;
    }

    public T manyToOne(Class klass) {
        setManyToOne(klass);

        return (T) this;
    }

    public T manyToOne(Class klass, String columnReference) {
        setManyToOne(klass, columnReference);

        return (T) this;
    }

    public T manyToOne(String table, String columnReference) {
        setManyToOne(table, columnReference);

        return (T) this;
    }

    public T manyToOne(Class klass, String columnReference, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToOne(klass, columnReference, onUpdate, onDelete);

        return (T) this;
    }

    public T manyToOne(String table, String columnReference, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToOne(table, columnReference, onUpdate, onDelete);

        return (T) this;
    }

    public boolean hasManyToOne() {
        return mConstraints.containsKey(MANY_TO_ONE);
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
        return (ManyToOneAssociation) mConstraints.get(MANY_TO_ONE_ASSOCIATION);
    }

    public T manyToOneAssociation() {
        setManyToOneAssociation();

        return (T) this;
    }

    public T manyToOneAssociation(String property) {
        setManyToOneAssociation(property);

        return (T) this;
    }

    public T manyToOneAssociation(Class klass, String property) {
        setManyToOneAssociation(klass, property);

        return (T) this;
    }

    public boolean hasManyToOneAssociation() {
        return mConstraints.containsKey(MANY_TO_ONE_ASSOCIATION);
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
        return (ManyToMany) mConstraints.get(MANY_TO_MANY);
    }

    public T manyToMany() {
        setManyToMany();

        return (T) this;
    }

    public T manyToMany(Class klass) {
        setManyToMany(klass);

        return (T) this;
    }

    public T manyToMany(CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToMany(onUpdate, onDelete);

        return (T) this;
    }

    public T manyToMany(Class klass, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
        setManyToMany(klass, onUpdate, onDelete);

        return (T) this;
    }

    public boolean hasManyToMany() {
        return mConstraints.containsKey(MANY_TO_MANY);
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
        return (ManyToManyAssociation) mConstraints.get(MANY_TO_MANY_ASSOCIATION);
    }

    public T manyToManyAssociation() {
        setManyToManyAssociation();

        return (T) this;
    }

    public T manyToManyAssociation(String property) {
        setManyToManyAssociation(property);

        return (T) this;
    }

    public T manyToManyAssociation(Class klass, String property) {
        setManyToManyAssociation(klass, property);

        return (T) this;
    }

    public boolean hasManyToManyAssociation() {
        return mConstraints.containsKey(MANY_TO_MANY_ASSOCIATION);
    }

    public T format(Format format) {
        setFormat(format);

        return (T) this;
    }

    public void setFormat(Format format) {
        if (null == format) {
            mConstraints.remove(FORMAT);
        } else {
            setConstraint(FORMAT, format);
        }
    }

    public Format getFormat() {
        return (Format) mConstraints.get(FORMAT);
    }

    public boolean isFormatted() {
        return mConstraints.containsKey(FORMAT);
    }

    public T file(boolean file) {
        setFile(file);

        return (T) this;
    }

    public void setFile(boolean file) {
        setConstraint(FILE, file);
    }

    public boolean isFile() {
        return Convert.toBoolean(mConstraints.get(FILE), false);
    }

    public T sparse(boolean sparse) {
        setSparse(sparse);

        return (T) this;
    }

    public void setSparse(boolean sparse) {
        setConstraint(SPARSE, sparse);
    }

    public boolean isSparse() {
        return Convert.toBoolean(mConstraints.get(SPARSE), false);
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
    public T listed(boolean listed) {
        setListed(listed);

        return (T) this;
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
        return Convert.toBoolean(mConstraints.get(LISTED), false);
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
    public T position(int position) {
        setPosition(position);

        return (T) this;
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
            mConstraints.remove(POSITION);
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
        return mConstraints.containsKey(POSITION);
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
        return Convert.toInt(mConstraints.get(POSITION), -1);
    }

    // TODO : cmf
//    /**
//     * Sets the mime type of the property.
//     * <p>Setting this constraint will make the {@link
//     * rife.cmf.dam.ContentQueryManager ContentQueryManager}
//     * automatically store the data in this property in the content management
//     * back-end. This column will not be stored in a regular database table.
//     * All this is handled transparently and automatically.
//     *
//     * @param mimeType the <code>MimeType</code> of the property
//     * @return the current <code>ConstrainedProperty</code> instance
//     * @see #setMimeType(MimeType)
//     * @see #hasMimeType()
//     * @see #getMimeType()
//     * @since 1.0
//     */
//    public T mimeType(MimeType mimeType) {
//        setMimeType(mimeType);
//
//        return (T) this;
//    }
//
//    /**
//     * Sets the mime type of the property.
//     *
//     * @param mimeType the <code>MimeType</code> of the property
//     * @see #mimeType(MimeType)
//     * @see #hasMimeType()
//     * @see #getMimeType()
//     * @since 1.0
//     */
//    public void setMimeType(MimeType mimeType) {
//        if (null == mimeType) {
//            mConstraints.remove(MIMETYPE);
//        } else {
//            setConstraint(MIMETYPE, mimeType);
//            persistent(false);
//            displayedRaw(true);
//        }
//    }
//
//    /**
//     * Indicates whether the property has a mime type.
//     *
//     * @return <code>true</code> if the property has a mime type; or
//     * <p><code>false</code> if it hasn't
//     * @see #mimeType(MimeType)
//     * @see #setMimeType(MimeType)
//     * @see #getMimeType()
//     * @since 1.0
//     */
//    public boolean hasMimeType() {
//        return mConstraints.containsKey(MIMETYPE);
//    }
//
//    /**
//     * Retrieves the mime type of the property.
//     *
//     * @return the mime type of the property; or
//     * <p><code>null</code> if the property has no mime type
//     * @see #mimeType(MimeType)
//     * @see #setMimeType(MimeType)
//     * @see #hasMimeType()
//     * @since 1.0
//     */
//    public MimeType getMimeType() {
//        return (MimeType) mConstraints.get(MIMETYPE);
//    }
//
//    /**
//     * Sets whether the content data of this property should be retrieved
//     * automatically from the back-end.
//     * <p>This is only useful when the property also has a mime type
//     * constraint.
//     * <p>It's not recommended to enable this constraint for large data since
//     * everything will be stored in memory, only use this for text snippets or
//     * something relatively small.
//     *
//     * @param autoRetrieved <code>true</code> if the data should be
//     *                      automatically retrieved; or
//     *                      <p><code>false</code> otherwise
//     * @return the current <code>ConstrainedProperty</code> instance
//     * @see #mimeType(MimeType)
//     * @see #setAutoRetrieved(boolean)
//     * @see #isAutoRetrieved()
//     * @since 1.0
//     */
//    public T autoRetrieved(boolean autoRetrieved) {
//        setAutoRetrieved(autoRetrieved);
//
//        return (T) this;
//    }
//
//    /**
//     * Sets whether the content data of this property should be retrieved
//     * automatically from the back-end.
//     *
//     * @param autoRetrieved <code>true</code> if the data should be
//     *                      automatically retrieved; or
//     *                      <p><code>false</code> otherwise
//     * @see #autoRetrieved(boolean)
//     * @see #isAutoRetrieved()
//     * @since 1.0
//     */
//    public void setAutoRetrieved(boolean autoRetrieved) {
//        setConstraint(AUTO_RETRIEVED, autoRetrieved);
//    }
//
//    /**
//     * Indicates whether the content data of this property is automatically
//     * retrieved from the back-end.
//     *
//     * @return <code>true</code> if the data should be automatically
//     * retrieved; or
//     * <p><code>false</code> otherwise
//     * @see #autoRetrieved(boolean)
//     * @see #setAutoRetrieved(boolean)
//     * @since 1.0
//     */
//    public boolean isAutoRetrieved() {
//        return Convert.toBoolean(mConstraints.get(AUTO_RETRIEVED), false);
//    }
//
//    /**
//     * Sets whether the content data of this property is a fragment.
//     * <p>This is only useful when the property also has a mime type
//     * constraint. A fragment means that it's not a complete document or a
//     * file, but rather a small part that is intended to be used within a
//     * larger document. For example a HTML snippet. This information is for
//     * example important when validating the data.
//     *
//     * @param fragment <code>true</code> if the content is a fragment; or
//     *                 <p><code>false</code> otherwise
//     * @return the current <code>ConstrainedProperty</code> instance
//     * @see #mimeType(MimeType)
//     * @see #setFragment(boolean)
//     * @see #isFragment()
//     * @since 1.0
//     */
//    public T fragment(boolean fragment) {
//        setFragment(fragment);
//
//        return (T) this;
//    }
//
//    /**
//     * Sets whether the content data of this property is a fragment.
//     *
//     * @param fragment <code>true</code> if the content is a fragment; or
//     *                 <p><code>false</code> otherwise
//     * @see #fragment(boolean)
//     * @see #isFragment()
//     * @since 1.0
//     */
//    public void setFragment(boolean fragment) {
//        setConstraint(FRAGMENT, fragment);
//    }
//
//    /**
//     * Indicates whether the content data of this property is a fragment.
//     *
//     * @return <code>true</code> if the content is a fragment; or
//     * <p><code>false</code> otherwise
//     * @see #fragment(boolean)
//     * @see #setFragment(boolean)
//     * @since 1.0
//     */
//    public boolean isFragment() {
//        return Convert.toBoolean(mConstraints.get(FRAGMENT), false);
//    }
//
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
    public T name(String name) {
        setName(name);

        return (T) this;
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
            mConstraints.remove(NAME);
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
        return (String) mConstraints.get(NAME);
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
        return mConstraints.containsKey(NAME);
    }

//    /**
//     * Sets the repository where the content data of this property will be
//     * stored.
//     * <p>This is only useful when the property also has a mime type
//     * constraint.
//     *
//     * @param repository the repository
//     * @return the current <code>CmrProperty</code> instance
//     * @see #mimeType(MimeType)
//     * @see #setRepository(String)
//     * @see #getRepository()
//     * @see #hasRepository()
//     * @since 1.0
//     */
//    public T repository(String repository) {
//        setRepository(repository);
//
//        return (T) this;
//    }
//
//    /**
//     * Sets the repository where the content data of this property will be
//     * stored.
//     *
//     * @param repository the repository
//     * @see #repository(String)
//     * @see #getRepository()
//     * @see #hasRepository()
//     * @since 1.0
//     */
//    public void setRepository(String repository) {
//        if (null == repository) {
//            mConstraints.remove(REPOSITORY);
//        } else {
//            setConstraint(REPOSITORY, repository);
//        }
//    }
//
//    /**
//     * Retrieves the repository where the content data of this property will
//     * be stored.
//     *
//     * @return <code>null</code> if no repository has been specified; or
//     * <p>the name of the repository
//     * @see #repository(String)
//     * @see #setRepository(String)
//     * @see #hasRepository()
//     * @since 1.0
//     */
//    public String getRepository() {
//        return (String) mConstraints.get(REPOSITORY);
//    }
//
//    /**
//     * Indicates whether this property will be stored in another repository
//     * than the default repository.
//     *
//     * @return <code>true</code> if the property will be stored in another
//     * repository; or
//     * <p><code>false</code> otherwise
//     * @see #repository(String)
//     * @see #setRepository(String)
//     * @see #getRepository()
//     * @since 1.0
//     */
//    public boolean hasRepository() {
//        return mConstraints.containsKey(REPOSITORY);
//    }
//
//    /**
//     * Sets whether this property has to be used as an ordinal.
//     * <p>The value of this property will be handled in the back-end by an
//     * {@link rife.cmf.dam.OrdinalManager OrdinalManager}. It will
//     * also enable the {@link
//     * rife.cmf.dam.ContentQueryManager#move(Constrained, String, rife.cmf.dam.OrdinalManager.Direction)
//     * move}, {@link
//     * rife.cmf.dam.ContentQueryManager#up(Constrained, String) up}
//     * and {@link
//     * rife.cmf.dam.ContentQueryManager#down(Constrained, String)
//     * down} methods in the {@link rife.cmf.dam.ContentQueryManager
//     * ContentQueryManager} to easily reorder data rows in the back-end.
//     *
//     * @param ordinal <code>true</code> if this property is an ordinal; or
//     *                <p><code>false</code> otherwise
//     * @return the current <code>ConstrainedProperty</code> instance
//     * @see #ordinal(boolean, String)
//     * @see #setOrdinal(boolean)
//     * @see #setOrdinal(boolean, String)
//     * @see #isOrdinal()
//     * @since 1.0
//     */
//    public T ordinal(boolean ordinal) {
//        setOrdinal(ordinal);
//
//        return (T) this;
//    }
//
//    /**
//     * Sets whether this property has to be used as an ordinal with a
//     * restricting column.
//     *
//     * @param ordinal     <code>true</code> if this property is an ordinal; or
//     *                    <p><code>false</code> otherwise
//     * @param restriction the name of the restricting column
//     * @return the current <code>ConstrainedProperty</code> instance
//     * @see #ordinal(boolean)
//     * @see #setOrdinal(boolean)
//     * @see #setOrdinal(boolean, String)
//     * @see #isOrdinal()
//     * @see #hasOrdinalRestriction()
//     * @see #getOrdinalRestriction()
//     * @since 1.0
//     */
//    public T ordinal(boolean ordinal, String restriction) {
//        setOrdinal(ordinal, restriction);
//
//        return (T) this;
//    }
//
//
//    /**
//     * Sets whether this property has to be used as an ordinal.
//     *
//     * @param ordinal <code>true</code> if this property is an ordinal; or
//     *                <p><code>false</code> otherwise
//     * @see #ordinal(boolean)
//     * @see #ordinal(boolean, String)
//     * @see #setOrdinal(boolean, String)
//     * @see #isOrdinal()
//     * @since 1.0
//     */
//    public void setOrdinal(boolean ordinal) {
//        setConstraint(ORDINAL, ordinal);
//        mConstraints.remove(ORDINAL_RESTRICTION);
//    }
//
//    /**
//     * Sets whether this property has to be used as an ordinal with a
//     * restricting column.
//     *
//     * @param ordinal     <code>true</code> if this property is an ordinal; or
//     *                    <p><code>false</code> otherwise
//     * @param restriction the name of the restricting column
//     * @see #ordinal(boolean)
//     * @see #ordinal(boolean, String)
//     * @see #setOrdinal(boolean)
//     * @see #isOrdinal()
//     * @see #hasOrdinalRestriction()
//     * @see #getOrdinalRestriction()
//     * @since 1.0
//     */
//    public void setOrdinal(boolean ordinal, String restriction) {
//        setConstraint(ORDINAL, ordinal);
//
//        if (ordinal) {
//            if (null == restriction) {
//                mConstraints.remove(ORDINAL_RESTRICTION);
//            } else {
//                setConstraint(ORDINAL_RESTRICTION, restriction);
//            }
//        } else {
//            mConstraints.remove(ORDINAL_RESTRICTION);
//        }
//    }
//
//    /**
//     * Indicates whether this property has to be used as an ordinal.
//     *
//     * @return <code>true</code> if this property is an ordinal; or
//     * <p><code>false</code> otherwise
//     * @see #ordinal(boolean)
//     * @see #ordinal(boolean, String)
//     * @see #setOrdinal(boolean)
//     * @see #setOrdinal(boolean, String)
//     * @since 1.0
//     */
//    public boolean isOrdinal() {
//        return Convert.toBoolean(mConstraints.get(ORDINAL), false);
//    }
//
//    /**
//     * Indicates whether this property has an ordinal restricting column.
//     *
//     * @return <code>true</code> if this property has an ordinal restricting
//     * column; or
//     * <p><code>false</code> otherwise
//     * @see #ordinal(boolean, String)
//     * @see #setOrdinal(boolean, String)
//     * @see #getOrdinalRestriction()
//     * @since 1.0
//     */
//    public boolean hasOrdinalRestriction() {
//        return mConstraints.containsKey(ORDINAL_RESTRICTION);
//    }
//
//    /**
//     * Retrieves the ordinal restriction of this property.
//     *
//     * @return the name of the ordinal restricting column; or
//     * <p><code>null</code> if no ordinal restricting column has been defined
//     * @see #ordinal(boolean, String)
//     * @see #setOrdinal(boolean, String)
//     * @see #hasOrdinalRestriction()
//     * @since 1.0
//     */
//    public String getOrdinalRestriction() {
//        return (String) mConstraints.get(ORDINAL_RESTRICTION);
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, boolean value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, char value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, byte value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, short value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, int value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, long value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, float value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property that will be converted
//     * internally to a <code>String</code> value.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #contentAttribute(String, String)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    public T contentAttribute(String name, double value) {
//        return contentAttribute(name, String.valueOf(value));
//    }
//
//    /**
//     * Sets a named content attribute for this property.
//     * <p>This is only useful when the property also has a mime type
//     * constraint.
//     * <p>A content attribute provides additional meta data about how you want
//     * to store the content data after loading, this can for example be image
//     * dimensions.
//     *
//     * @param name  the name of the attribute
//     * @param value the value of the attribute
//     * @return the current <code>Content</code> instance
//     * @see #mimeType(MimeType)
//     * @see #getContentAttributes()
//     * @since 1.0
//     */
//    @SuppressWarnings("unchecked")
//    public T contentAttribute(String name, String value) {
//        HashMap<String, String> content_attributes = (HashMap<String, String>) mConstraints.get(CONTENT_ATTRIBUTES);
//        if (null == content_attributes) {
//            content_attributes = new HashMap<String, String>();
//            setConstraint(CONTENT_ATTRIBUTES, content_attributes);
//        }
//        content_attributes.put(name, value);
//
//        return (T) this;
//    }
//
//    /**
//     * Retrieves the map of named content attributes for this property.
//     *
//     * @return the map of named content attributes; or
//     * <p><code>null</code> if no attributes are present
//     * @see #contentAttribute(String, String)
//     * @since 1.0
//     */
//    @SuppressWarnings("unchecked")
//    public Map<String, String> getContentAttributes() {
//        return (HashMap<String, String>) mConstraints.get(CONTENT_ATTRIBUTES);
//    }
//
//    /**
//     * Sets a content transformer for this property.
//     * <p>This is only useful when the property also has a mime type
//     * constraint.
//     *
//     * @param transformer the content transformer
//     * @return the current <code>Content</code> instance
//     * @see #mimeType(MimeType)
//     * @see #setTransformer(ContentTransformer)
//     * @see #hasTransformer()
//     * @see #getTransformer()
//     * @since 1.0
//     */
//    public T transformer(ContentTransformer transformer) {
//        setTransformer(transformer);
//
//        return (T) this;
//    }
//
//    /**
//     * Sets a content transformer for this property.
//     *
//     * @param transformer the content transformer
//     * @see #mimeType(MimeType)
//     * @see #transformer(ContentTransformer)
//     * @see #hasTransformer()
//     * @see #getTransformer()
//     * @since 1.0
//     */
//    public void setTransformer(ContentTransformer transformer) {
//        if (null == transformer) {
//            mConstraints.remove(TRANSFORMER);
//        } else {
//            setConstraint(TRANSFORMER, transformer);
//        }
//    }
//
//    /**
//     * Indicates whether this property has a content transformer.
//     *
//     * @return <code>true</code> if this property has a content transformer;
//     * or
//     * <p><code>false</code> otherwise
//     * @see #transformer(ContentTransformer)
//     * @see #setTransformer(ContentTransformer)
//     * @see #getTransformer()
//     * @since 1.0
//     */
//    public boolean hasTransformer() {
//        return mConstraints.containsKey(TRANSFORMER);
//    }
//
//    /**
//     * Retrieves the content transformer of this property.
//     *
//     * @return the requested content transformer; or
//     * <p><code>null</code> if no content transformer has been defined
//     * @see #transformer(ContentTransformer)
//     * @see #setTransformer(ContentTransformer)
//     * @see #hasTransformer()
//     * @since 1.0
//     */
//    public ContentTransformer getTransformer() {
//        return (ContentTransformer) mConstraints.get(TRANSFORMER);
//    }
//
//    /**
//     * Sets the cached loaded data.
//     * <p>This is used internally and should never be used explicitly by a
//     * developer, see {@link
//     * rife.cmf.Content#cachedLoadedData(Object)
//     * Content.cachedLoadedData(Object)} for more information.
//     *
//     * @param data the loaded data
//     * @see #getCachedLoadedData()
//     * @since 1.0
//     */
//    public void setCachedLoadedData(Object data) {
//        if (null == data) {
//            synchronized (mConstraints) {
//                mConstraints.remove(CACHED_LOADED_DATA);
//            }
//        } else {
//            setConstraint(CACHED_LOADED_DATA, data);
//        }
//    }
//
//    /**
//     * Retrieves the cached loaded content data.
//     *
//     * @return the cached loaded content data; or
//     * <p><code>null</code> if no loaded content data has been cached
//     * @see #setCachedLoadedData(Object)
//     * @since 1.0
//     */
//    public Object getCachedLoadedData() {
//        return mConstraints.get(CACHED_LOADED_DATA);
//    }

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
     * @since 1.4
     */
    public void setConstraint(String name, Object constraintData) {
        synchronized (mConstraints) {
            mConstraints.put(name, constraintData);
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
     * @since 1.4
     */
    public T constraint(String name, Object constraintData) {
        setConstraint(name, constraintData);

        return (T) this;
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
     * @since 1.4
     */
    public Object getConstraint(String name) {
        return mConstraints.get(name);
    }

    /**
     * Retrieves the map of all the constraints.
     *
     * @return the map with all the registered constraints
     * @see #setConstraint
     * @see #constraint
     * @see #getConstraint
     * @since 1.4
     */
    public Map<String, Object> getConstraints() {
        return mConstraints;
    }

    public ConstrainedProperty clone() {
        ConstrainedProperty new_instance = null;
        try {
            new_instance = (ConstrainedProperty) super.clone();

            new_instance.mConstraints = new HashMap<String, Object>(mConstraints);

            if (mListeners != null) {
                new_instance.mListeners = new ArrayList<ConstrainedPropertyListener>(mListeners);
            }
        } catch (CloneNotSupportedException e) {
            new_instance = null;
        }

        return new_instance;
    }

    public class ManyToOne implements Cloneable {
        private String mColumn = null;
        private String mTable = null;
        private String mDerivedTable = null;
        private Class mClass = null;
        private CreateTable.ViolationAction mOnUpdate = null;
        private CreateTable.ViolationAction mOnDelete = null;

        public ManyToOne() {
            this((Class) null, null, null, null);
        }

        public ManyToOne(Class klass) {
            this(klass, null, null, null);
        }

        public ManyToOne(String table, String column, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            mColumn = column;
            mTable = table;
            mOnUpdate = onUpdate;
            mOnDelete = onDelete;
        }

        public ManyToOne(Class klass, String column, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            this((String) null, column, onUpdate, onDelete);
            mClass = klass;
        }

        public String getDerivedTable() {
            if (null == mDerivedTable) {
                if (mTable != null) {
                    mDerivedTable = mTable;
                }

                if (mClass != null) {
                    mDerivedTable = ClassUtils.shortenClassName(mClass);
                }

            }

            return mDerivedTable;
        }

        public void setColumn(String column) {
            mColumn = column;
        }

        public String getColumn() {
            return mColumn;
        }

        public void setTable(String table) {
            mDerivedTable = null;
            mTable = table;
        }

        public String getTable() {
            return mTable;
        }

        public void setAssociatedClass(Class klass) {
            mDerivedTable = null;
            mClass = klass;
        }

        public Class getAssociatedClass() {
            return mClass;
        }

        public void setOnUpdate(CreateTable.ViolationAction onUpdate) {
            mOnUpdate = onUpdate;
        }

        public CreateTable.ViolationAction getOnUpdate() {
            return mOnUpdate;
        }

        public void setOnDelete(CreateTable.ViolationAction onDelete) {
            mOnDelete = onDelete;
        }

        public CreateTable.ViolationAction getOnDelete() {
            return mOnDelete;
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
        private Class mClass = null;
        private String mProperty = null;

        public ManyToOneAssociation() {
        }

        public ManyToOneAssociation(String property) {
            mProperty = property;
        }

        public ManyToOneAssociation(Class klass, String property) {
            this(property);
            mClass = klass;
        }

        public void setMainClass(Class klass) {
            mClass = klass;
        }

        public Class getMainClass() {
            return mClass;
        }

        public void setMainProperty(String property) {
            mProperty = property;
        }

        public String getMainProperty() {
            return mProperty;
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
        private Class mClass = null;
        private CreateTable.ViolationAction mOnUpdate = null;
        private CreateTable.ViolationAction mOnDelete = null;

        public ManyToMany() {
            this(null, null, null);
        }

        public ManyToMany(Class klass) {
            this(klass, null, null);
        }

        public ManyToMany(CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            this(null, onUpdate, onDelete);
            mOnUpdate = onUpdate;
            mOnDelete = onDelete;
        }

        public ManyToMany(Class klass, CreateTable.ViolationAction onUpdate, CreateTable.ViolationAction onDelete) {
            mOnUpdate = onUpdate;
            mOnDelete = onDelete;
            mClass = klass;
        }

        public void setAssociatedClass(Class klass) {
            mClass = klass;
        }

        public Class getAssociatedClass() {
            return mClass;
        }

        public void setOnUpdate(CreateTable.ViolationAction onUpdate) {
            mOnUpdate = onUpdate;
        }

        public CreateTable.ViolationAction getOnUpdate() {
            return mOnUpdate;
        }

        public void setOnDelete(CreateTable.ViolationAction onDelete) {
            mOnDelete = onDelete;
        }

        public CreateTable.ViolationAction getOnDelete() {
            return mOnDelete;
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
        private Class mClass = null;
        private String mProperty = null;

        public ManyToManyAssociation() {
        }

        public ManyToManyAssociation(String property) {
            mProperty = property;
        }

        public ManyToManyAssociation(Class klass, String property) {
            this(property);
            mClass = klass;
        }

        public void setAssociatedClass(Class klass) {
            mClass = klass;
        }

        public Class getAssociatedClass() {
            return mClass;
        }

        public void setAssociatedProperty(String property) {
            mProperty = property;
        }

        public String getAssociatedProperty() {
            return mProperty;
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

