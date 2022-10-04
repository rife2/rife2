/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.datastructures;

import java.util.Collection;
import java.util.HashMap;

/**
 * The purpose of this abstract base class is to allow the creation of
 * type-safe enumerations.
 * <p>Only the derived class is allowed to create instances and should do so
 * as <code>public static final</code> objects.
 * <p>Each instance of a <code>EnumClass</code> class needs an identifier to
 * its constructor. This identifier is used to uniquely differentiate
 * enumeration members amongst each-other.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class EnumClass<IdentifierType> {
    private static HashMap<String, HashMap<Object, EnumClass>> sTypes = new HashMap<>();

    protected IdentifierType identifier_ = null;

    protected EnumClass(IdentifierType identifier) {
        registerType(this.getClass(), identifier);
    }

    protected EnumClass(Class klass, IdentifierType identifier) {
        registerType(klass, identifier);
    }

    protected final void registerType(Class klass, IdentifierType identifier) {
        assert klass != null;
        assert identifier != null;

        var class_name = klass.getName();
        HashMap<Object, EnumClass> instances;

        if (!sTypes.containsKey(class_name)) {
            instances = new HashMap<>();
            sTypes.put(class_name, instances);
        } else {
            instances = sTypes.get(class_name);
        }
        identifier_ = identifier;
        instances.put(identifier_, this);
    }

    protected static Collection<?> getIdentifiers(Class<? extends EnumClass> type) {
        return sTypes.get(type.getName()).keySet();
    }

    protected static Collection<? extends EnumClass> getMembers(Class<? extends EnumClass> type) {
        return sTypes.get(type.getName()).values();
    }

    protected static <MemberType extends EnumClass> MemberType getMember(Class<MemberType> type, Object identifier) {
        return (MemberType) sTypes.get(type.getName()).get(identifier);
    }

    public IdentifierType getIdentifier() {
        return identifier_;
    }

    public String toString() {
        return identifier_.toString();
    }

    public int hashCode() {
        return identifier_.hashCode();
    }

    public boolean equals(Object object) {
        if (null == object) {
            return false;
        }

        if (object instanceof EnumClass) {
            EnumClass other_enumclass = (EnumClass) object;
            if (null != other_enumclass &&
                other_enumclass.identifier_.equals(identifier_)) {
                return true;
            } else {
                return false;
            }
        }

        return object.equals(identifier_);
    }
}
