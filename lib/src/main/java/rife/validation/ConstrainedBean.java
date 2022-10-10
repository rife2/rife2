/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rife.datastructures.EnumClass;


/**
 * A <code>ConstrainedBean</code> object makes it possible to define all
 * constraints for a bean instance that are not related to a single property.
 * The constraints here are global for the entire bean and either involve
 * several properties or are even totally unrelated to properties.
 * <p>It's possible to add constraints to a ConstrainedProperty instance
 * through regular setters, but chainable setters are also available to make
 * it possible to easily define a series of constraints, for example:
 * <pre>ConstrainedBean constrained = new ConstrainedBean()
 *    .unique("firstName", "lastName")
 *    .defaultOrder("city")
 *    .defaultOrder("lastName")
 *    .defaultOrder("firstName");</pre>
 * <p>
 * <p>A constrained bean is typically added to a {@link Constrained} bean in
 * its constructor. These are the static constraints that will be set for each
 * and every instance of the bean. You'll however most of the time use the
 * {@link Validation} abstract class that provides the {@link
 * Validation#activateValidation activateValidation} method which initializes
 * the constraints on a need-to-have basis. This dramatically reduces memory
 * usage since otherwise all constraints will be initialized for every bean
 * instance, even though you don't use them.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Constrained
 * @see ConstrainedProperty
 * @since 1.0
 */
public class ConstrainedBean<T extends ConstrainedBean> {
    public static final Direction ASC = new Direction("ASC");
    public static final Direction DESC = new Direction("DESC");

    // standard constraint identifiers
    public static final String ASSOCIATIONS = "ASSOCIATIONS";
    public static final String UNIQUE = "UNIQUE";
    public static final String TEXTUAL_IDENTIFIER = "TEXTUAL_IDENTIFIER";
    public static final String DEFAULT_ORDERING = "DEFAULT_ORDERING";

    // constraints
    protected HashMap<String, Object> constraints_ = new HashMap<>();

    /**
     * Creates a new <code>ConstrainedBean</code>.
     *
     * @since 1.0
     */
    public ConstrainedBean() {
    }

    public T associations(Class... associations) {
        setAssociations(associations);

        return (T) this;
    }

    public void setAssociations(Class... associations) {
        if (null == associations) {
            constraints_.remove(ASSOCIATIONS);
        } else {
            constraints_.put(ASSOCIATIONS, associations);
        }
    }

    public Class[] getAssociations() {
        return (Class[]) constraints_.get(ASSOCIATIONS);
    }

    public boolean hasAssociations() {
        return constraints_.containsKey(ASSOCIATIONS) && ((Class[]) constraints_.get(ASSOCIATIONS)).length > 0;
    }

    public T unique(String... unique) {
        if (unique != null) {
            List<String[]> unique_list = (List<String[]>) constraints_.get(UNIQUE);
            if (null == unique_list) {
                unique_list = new ArrayList<String[]>();
                constraints_.put(UNIQUE, unique_list);
            }

            unique_list.add(unique);
        }

        return (T) this;
    }

    public T uniques(List<String[]> unique) {
        setUniques(unique);

        return (T) this;
    }

    public void setUniques(List<String[]> unique) {
        if (null == unique) {
            constraints_.remove(UNIQUE);
        } else {
            constraints_.put(UNIQUE, unique);
        }
    }

    public List<String[]> getUniques() {
        return (List<String[]>) constraints_.get(UNIQUE);
    }

    public boolean hasUniques() {
        return constraints_.containsKey(UNIQUE) && ((List<String[]>) constraints_.get(UNIQUE)).size() > 0;
    }

    public T textualIdentifier(TextualIdentifierGenerator identifier) {
        setTextualIdentifier(identifier);

        return (T) this;
    }

    public void setTextualIdentifier(TextualIdentifierGenerator identifier) {
        if (null == identifier) {
            constraints_.remove(TEXTUAL_IDENTIFIER);
        } else {
            constraints_.put(TEXTUAL_IDENTIFIER, identifier);
        }
    }

    public TextualIdentifierGenerator getTextualIdentifier() {
        return (TextualIdentifierGenerator) constraints_.get(TEXTUAL_IDENTIFIER);
    }

    public boolean hasTextualIdentifier() {
        return constraints_.containsKey(TEXTUAL_IDENTIFIER);
    }

    public T defaultOrder(String propertyName) {
        return defaultOrder(propertyName, ASC);
    }

    public T defaultOrder(String propertyName, Direction direction) {
        return defaultOrder(new Order(propertyName, direction));
    }

    public T defaultOrder(Order order) {
        if (order != null) {
            List<Order> ordering_list = (List<Order>) constraints_.get(DEFAULT_ORDERING);
            if (null == ordering_list) {
                ordering_list = new ArrayList<Order>();
                constraints_.put(DEFAULT_ORDERING, ordering_list);
            }

            ordering_list.add(order);
        }

        return (T) this;
    }

    public T defaultOrdering(List<Order> ordering) {
        setDefaultOrdering(ordering);

        return (T) this;
    }

    public void setDefaultOrdering(List<Order> ordering) {
        if (null == ordering) {
            constraints_.remove(DEFAULT_ORDERING);
        } else {
            constraints_.put(DEFAULT_ORDERING, ordering);
        }
    }

    public List<Order> getDefaultOrdering() {
        return (List<Order>) constraints_.get(DEFAULT_ORDERING);
    }

    public boolean hasDefaultOrdering() {
        return constraints_.containsKey(DEFAULT_ORDERING) && ((List<Order>) constraints_.get(DEFAULT_ORDERING)).size() > 0;
    }

    HashMap<String, Object> getConstraints() {
        return constraints_;
    }

    public static class Order implements Cloneable {
        private String mPropertyName = null;
        private Direction mDirection = null;

        public Order(String property, Direction direction) {
            setPropertyName(property);
            setDirection(direction);
        }

        public String getPropertyName() {
            return mPropertyName;
        }

        void setPropertyName(String propertyName) {
            if (null == propertyName) throw new IllegalArgumentException("propertyName can't be null.");
            if (0 == propertyName.length()) throw new IllegalArgumentException("propertyName can't be empty.");

            mPropertyName = propertyName;
        }

        public Direction getDirection() {
            return mDirection;
        }

        void setDirection(Direction direction) {
            if (null == direction) throw new IllegalArgumentException("direction can't be null.");

            mDirection = direction;
        }

        public Order clone() {
            Order new_instance;
            try {
                new_instance = (Order) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public static class Direction extends EnumClass<String> {
        Direction(String identifier) {
            super(identifier);
        }
    }
}

