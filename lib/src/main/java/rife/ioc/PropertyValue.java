/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import rife.ioc.exceptions.PropertyValueException;

/**
 * This interface defines the methods that need to be implemented by classes
 * that are able to provide values to properties.
 * <p>These classes should make all value retrieval as lazy as possible and
 * store only the parameters that are required to obtain the actual data
 * dynamically at runtime.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface PropertyValue {
    /**
     * Retrieves a property value.
     *
     * @return the requested property value; or
     * <p><code>null</code> if the property value couldn't be found
     * @throws PropertyValueException When something went wrong during the
     *                                retrieval of the property value.
     * @since 1.0
     */
    Object getValue()
    throws PropertyValueException;

    /**
     * Retrieves a string representation of the property value.
     *
     * @return the requested string representation of the property value; or
     * <p><code>null</code> if the property value couldn't be found
     * @throws PropertyValueException When something went wrong during the
     *                                retrieval of the property value.
     * @since 1.0
     */
    String getValueString()
    throws PropertyValueException;

    /**
     * Indicates whether the value provided by this instance is negligible in
     * a textual context. This is for instance applicable to pure whitespace
     * values that when trimmed, have zero length. The property construction
     * logic will check this state to determine if it has to concatenate
     * several property values together as one text result of only use one and
     * discard all other negligible ones.
     *
     * @return <code>true</code> if the value is negligible in a textual
     * context; or
     * <p><code>false</code> otherwise
     * @since 1.0
     */
    boolean isNegligible();

    /**
     * Indicates whether the value is statically fixed an not dynamically
     * retrieved at runtime.
     *
     * @return <code>true</code> if the value is static; or
     * <p><code>false</code> if the value is dynamically retrieved at runtime
     * @since 1.0
     */
    boolean isStatic();
}
