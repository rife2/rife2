/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.BeanUtils;
import rife.tools.ClassUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.util.Iterator;

public class ConstrainedUtils {
    public static Constrained getConstrainedInstance(Class beanClass) {
        if (null == beanClass) {
            return null;
        }

        Constrained constrained = null;
        if (Constrained.class.isAssignableFrom(beanClass)) {
            try {
                constrained = (Constrained) beanClass.newInstance();
            } catch (Throwable e) {
                return null;
            }
        }

        return constrained;
    }

    public static Constrained makeConstrainedInstance(Object bean) {
        if (null == bean) {
            return null;
        }

        Constrained constrained = null;
        if (bean instanceof Constrained) {
            constrained = (Constrained) bean;
        }

        return constrained;
    }

    public static ConstrainedProperty getConstrainedProperty(Object bean, String name) {
        if (null == bean) {
            return null;
        }

        Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        ConstrainedProperty contrained_property = null;
        if (constrained != null) {
            contrained_property = constrained.getConstrainedProperty(name);
        }

        return contrained_property;
    }

    public static String getIdentityProperty(Class beanClass) {
        String identity_property = null;

        Constrained constrained_bean = getConstrainedInstance(beanClass);
        if (constrained_bean != null) {
            Iterator<ConstrainedProperty> properties_it = constrained_bean.getConstrainedProperties().iterator();
            ConstrainedProperty property;
            while (properties_it.hasNext()) {
                property = properties_it.next();
                if (property.isIdentifier()) {
                    identity_property = property.getPropertyName();
                    break;
                }
            }
        }

        if (null == identity_property) {
            identity_property = "id";
        }

        return identity_property;
    }

    public static boolean editConstrainedProperty(Constrained bean, String propertyName, String prefix) {
        if (null == bean) {
            return true;
        }

        if (prefix != null &&
            propertyName.startsWith(prefix)) {
            propertyName = propertyName.substring(prefix.length());
        }

        ConstrainedProperty constrained_property = bean.getConstrainedProperty(propertyName);

        return !(constrained_property != null &&
            !constrained_property.isEditable());
    }

    public static boolean persistConstrainedProperty(Constrained bean, String propertyName, String prefix) {
        if (null == bean) {
            return true;
        }

        if (prefix != null &&
            propertyName.startsWith(prefix)) {
            propertyName = propertyName.substring(prefix.length());
        }

        ConstrainedProperty constrained_property = bean.getConstrainedProperty(propertyName);
        if (constrained_property != null) {
            if (!constrained_property.isPersistent() ||
                constrained_property.isSameAs() ||
                constrained_property.hasManyToOneAssociation() ||
                constrained_property.hasManyToMany() ||
                constrained_property.hasManyToManyAssociation()) {
                return false;
            }

            if (constrained_property.hasManyToOne() &&
                !isManyToOneJoinProperty(bean.getClass(), propertyName)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isManyToOneJoinProperty(Class beanClass, String propertyName) {
        try {
            Class property_type = BeanUtils.getPropertyType(beanClass, propertyName);
            if (ClassUtils.isBasic(property_type)) {
                return true;
            }
        } catch (BeanUtilsException e) {
            return true;
        }

        return false;
    }

    public static boolean saveConstrainedProperty(Constrained bean, String propertyName, String prefix) {
        if (null == bean) {
            return true;
        }

        if (prefix != null &&
            propertyName.startsWith(prefix)) {
            propertyName = propertyName.substring(prefix.length());
        }

        ConstrainedProperty constrained_property = bean.getConstrainedProperty(propertyName);
        if (constrained_property != null) {
            if (!constrained_property.isPersistent() ||
                !constrained_property.isSaved() ||
                constrained_property.isSameAs() ||
                constrained_property.hasManyToOneAssociation() ||
                constrained_property.hasManyToMany() ||
                constrained_property.hasManyToManyAssociation()) {
                return false;
            }

            if (constrained_property.hasManyToOne() &&
                !isManyToOneJoinProperty(bean.getClass(), propertyName)) {
                return false;
            }
        }

        return true;
    }

    public static boolean fileConstrainedProperty(Constrained bean, String propertyName, String prefix) {
        if (null == bean) {
            return false;
        }

        if (prefix != null &&
            propertyName.startsWith(prefix)) {
            propertyName = propertyName.substring(prefix.length());
        }

        ConstrainedProperty constrained_property = bean.getConstrainedProperty(propertyName);

        return constrained_property != null && constrained_property.isFile();
    }
}
