/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.Collection;

/**
 * This interface defines methods for bean-centric constraining of data
 * entities.
 * <p>A constraint describes additional information about a data entity. Its
 * main purpose is to alter the default behaviour of a data type and to
 * clearly set the accepted limits. The meta-data that's provided through
 * constraints can be used elsewhere to gather more information about how to
 * correctly integrate the indicated data limits.
 * <p>For example, a constraint specifies that a certain text's length is
 * limited to 30 characters, parts of the system can query this information
 * and act accordingly:
 * <ul>
 * <li>a HTML form builder can create a field that doesn't allow the entry of
 * longer text,
 * <li>a SQL query builder can limit the size of the column in which the text
 * will stored when the table creation SQL is generated,
 * <li>a validation system can check if the text isn't longer than 30
 * characters and provide appropriate information when the length is exceeded.
 * </ul>
 * <p>There are two types of constraints:
 * <ul>
 * <li>those that are related to the entire bean (<code>ConstrainedBean</code>
 * constraints)
 * <li>those that only apply to a single property (<code>ConstrainedProperty</code>
 * constraints)
 * </ul>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ConstrainedBean
 * @see ConstrainedProperty
 * @since 1.0
 */
public interface Constrained<B extends ConstrainedBean, P extends ConstrainedProperty> {
    /**
     * Add a new constrained bean.
     * <p>
     * When several constrained beans are added, they are merged at
     * constraint-level. This means for instance that all previous unique
     * constraints will be replaced by those of the new constrained bean, they
     * will not be combined.
     *
     * @param constrainedBean the <code>ConstrainedBean</code> instance
     *                        that has to be added
     * @see ConstrainedBean
     * @since 1.0
     */
    void addConstraint(B constrainedBean);

    /**
     * Add a new constrained property.
     * <p>
     * When several of the same constrained properties are added, they are
     * merged at constraint-level. This means for instance that a previous
     * inList constraint will be replaced by the one of the new constrained
     * bean, they will not be combined.
     *
     * @param constrainedProperty the <code>ConstrainedProperty</code>
     *                            instance that has to be added
     * @see ConstrainedProperty
     * @since 1.0
     */
    void addConstraint(P constrainedProperty);

    /**
     * Retrieves the constrained bean that has been set for this
     * <code>Constrained</code> instance.
     *
     * @return the requested <code>ConstrainedBean; or </code>
     * <p><code>null</code> if no <code>ConstrainedBean</code> is
     * available.
     * @see ConstrainedProperty
     * @since 1.0
     */
    B getConstrainedBean();

    /**
     * Returns a collection with all the constrained properties that have
     * been registered.
     *
     * @return A <code>Collection</code> with all the
     * <code>ConstrainedProperty</code> objects that are registered. If no
     * constrained properties are available, an empty collection will be
     * returned, not <code>null</code>.
     * @see ConstrainedProperty
     * @since 1.0
     */
    Collection<P> getConstrainedProperties();

    /**
     * Indicates whether this constrained bean contains a particular constraint
     * on at least one of its properties.
     *
     * @return <code>true</code> if this constraint is present on at least one
     * of the properties; or
     * <p><code>false</code> otherwise
     * @see ConstrainedProperty
     * @since 1.6
     */
    boolean hasPropertyConstraint(String name);

    /**
     * Retrieve a registered <code>ConstrainedProperty</code> according to
     * its name.
     *
     * @param propertyName the name of the
     *                     <code>ConstrainedProperty</code> that has to be retrieved
     * @return the requested <code>ConstrainedProperty; or </code>
     * <p><code>null</code> if no such <code>ConstrainedProperty</code> is
     * available.
     * @see ConstrainedProperty
     * @since 1.0
     */
    P getConstrainedProperty(String propertyName);
}

