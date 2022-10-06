/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

/**
 * General purpose class containing common <code>Object</code> manipulation
 * methods.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @author Thomas McGlynn
 * @since 1.0
 */

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class ObjectUtils {
    /**
     * Clone an Object if possible.
     * <p>
     * This method returns an Object which is a clone of the
     * input object.  It checks if the method implements the
     * Cloneable interface and then uses reflection to invoke
     * the clone method.
     *
     * @param object The object to be cloned.
     * @return <code>null</code> if the cloning failed; or
     * <p>
     * the cloned <code>Object</code> instance.
     */
    public static <T> T genericClone(T object) {
        if (object == null) {
            return null;
        }

        // if this is a pure object and not an extending class, just don't clone
        // it since it's most probably a thread monitor lock
        if (Object.class == object.getClass()) {
            return object;
        }
        // strings can't be cloned, but they are immutable, so skip over it
        if (String.class == object.getClass()) {
            return object;
        }
        // exceptions can't be cloned, but they are simply indicative, so skip over it
        if (object instanceof Throwable) {
            return object;
        }
        // stringbuffers can't be cloned, so just create a new one
        if (StringBuffer.class == object.getClass()) {
            return (T) new StringBuffer(object.toString());
        }
        // stringbuilders can't be cloned, so just create a new one
        if (StringBuilder.class == object.getClass()) {
            return (T) new StringBuilder(object.toString());
        }
        // handle the reference counterparts of the primitives that are
        // not cloneable in the jdk, they are immutable
        if (object instanceof Number ||
            object instanceof Boolean ||
            object instanceof Character) {
            return object;
        }

        if (!(object instanceof Cloneable)) {
            return null;
        }

        try {
            Method method = object.getClass().getMethod("clone", (Class[]) null);
            method.setAccessible(true);
            return (T) method.invoke(object, (Object[]) null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Try to create a deep clone of the provides object. This handles arrays,
     * collections and maps. If the class in not a supported standard JDK
     * collection type the <code>genericClone</code> will be used instead.
     *
     * @param object The object to be copied.
     */
    public static <T> T deepClone(T object)
    throws CloneNotSupportedException {
        if (null == object) {
            return null;
        }

        String classname = object.getClass().getName();

        // check if it's an array
        if ('[' == classname.charAt(0)) {
            // handle 1 dimensional primitive arrays
            if (classname.charAt(1) != '[' && classname.charAt(1) != 'L') {
                switch (classname.charAt(1)) {
                    case 'B':
                        return (T) ((byte[]) object).clone();
                    case 'Z':
                        return (T) ((boolean[]) object).clone();
                    case 'C':
                        return (T) ((char[]) object).clone();
                    case 'S':
                        return (T) ((short[]) object).clone();
                    case 'I':
                        return (T) ((int[]) object).clone();
                    case 'J':
                        return (T) ((long[]) object).clone();
                    case 'F':
                        return (T) ((float[]) object).clone();
                    case 'D':
                        return (T) ((double[]) object).clone();
                    ///CLOVER:OFF
                    default:
                        Logger.getLogger("rife.tools").severe("Unknown primitive array class: " + classname);
                        return null;
                    ///CLOVER:ON
                }
            }

            // get the base type and the dimension count of the array
            int dimension_count = 1;
            while (classname.charAt(dimension_count) == '[') {
                dimension_count += 1;
            }
            Class baseClass = null;
            if (classname.charAt(dimension_count) != 'L') {
                baseClass = getBaseClass(object);
            } else {
                try {
                    baseClass = Class.forName(classname.substring(dimension_count + 1, classname.length() - 1));
                }
                ///CLOVER:OFF
                catch (ClassNotFoundException e) {
                    Logger.getLogger("rife.tools").severe("Internal error: class definition inconsistency: " + classname);
                    return null;
                }
                ///CLOVER:ON
            }

            // instantiate the array but make all but the first dimension 0.
            int[] dimensions = new int[dimension_count];
            dimensions[0] = Array.getLength(object);
            for (int i = 1; i < dimension_count; i += 1) {
                dimensions[i] = 0;
            }
            T copy = (T) Array.newInstance(baseClass, dimensions);

            // now fill in the next level down by recursion.
            for (int i = 0; i < dimensions[0]; i += 1) {
                Array.set(copy, i, deepClone(Array.get(object, i)));
            }

            return copy;
        }
        // handle cloneable collections
        else if (object instanceof Collection collection &&
            object instanceof Cloneable) {

            // instantiate the new collection and clear it
            Collection copy = (Collection) ObjectUtils.genericClone(object);
            copy.clear();

            // clone all the values in the collection individually
            for (Object element : collection) {
                copy.add(deepClone(element));
            }

            return (T) copy;
        }
        // handle cloneable maps
        else if (object instanceof Map map &&
            object instanceof Cloneable) {

            // instantiate the new map and clear it
            Map copy = (Map) ObjectUtils.genericClone(object);
            copy.clear();

            // now clone all the keys and values of the entries
            Iterator collection_it = map.entrySet().iterator();
            Map.Entry entry = null;
            while (collection_it.hasNext()) {
                entry = (Map.Entry) collection_it.next();
                copy.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
            }

            return (T) copy;
        }
        // use the generic clone method
        else {
            T copy = ObjectUtils.genericClone(object);
            if (null == copy) {
                throw new CloneNotSupportedException(object.getClass().getName());
            }
            return copy;
        }
    }

    /**
     * This routine returns the base class of an object.  This is just
     * the class of the object for non-arrays.
     *
     * @param object The object whose base class you want to retrieve.
     */
    public static Class getBaseClass(Object object) {
        if (object == null) {
            return Void.TYPE;
        }

        String className = object.getClass().getName();

        // skip forward over the array dimensions
        int dims = 0;
        while (className.charAt(dims) == '[') {
            dims += 1;
        }

        // if there were no array dimensions, just return the class of the
        // provided object
        if (dims == 0) {
            return object.getClass();
        }

        switch (className.charAt(dims)) {
            // handle the boxed primitives
            case 'Z':
                return Boolean.TYPE;
            case 'B':
                return Byte.TYPE;
            case 'S':
                return Short.TYPE;
            case 'C':
                return Character.TYPE;
            case 'I':
                return Integer.TYPE;
            case 'J':
                return Long.TYPE;
            case 'F':
                return Float.TYPE;
            case 'D':
                return Double.TYPE;
            // look up the class of another reference type
            case 'L':
                try {
                    return Class.forName(className.substring(dims + 1, className.length() - 1));
                } catch (ClassNotFoundException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}

