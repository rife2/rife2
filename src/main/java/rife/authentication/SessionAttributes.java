/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication;

/**
 * This interface defines the methods that classes with
 * {@code SessionAttributes} functionalities have to implement.
 * <p>
 * A {@code SessionAttributes} class defines the context in which a session
 * is being validated. The presence and the value of certain attributes can
 * influence whether the access to a secured resource is authorized or
 * prohibited for a particular user.
 * <p>
 * For example, regular users have access to everything besides in the
 * administration interface. Resources that are restricted to administrators can
 * for example have a {@code role} attribute that is set to
 * {@code admin}. A {@code SessionValidator} instance can detect the
 * presence of this attribute and act accordingly to verify if the user has the
 * authorities of the required role (in this case, administrator rights).
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.authentication.SessionValidator
 * @since 1.0
 */
public interface SessionAttributes {
    /**
     * Checks if a certain attribute is present.
     *
     * @param key The key that identifies the attribute.
     * @return {@code true} if the attribute was present; or
     * <p>
     * {@code false} otherwise.
     * @see #getAttribute(String)
     * @since 1.0
     */
    boolean hasAttribute(String key);

    /**
     * Retrieves the value of a particular attribute.
     *
     * @param key The key that identifies the attribute.
     * @return A {@code String} instance with the value of the requested
     * attribute; or
     * <p>
     * {@code null} if the attribute couldn't be found.
     * @see #hasAttribute(String)
     * @since 1.0
     */
    String getAttribute(String key);
}

