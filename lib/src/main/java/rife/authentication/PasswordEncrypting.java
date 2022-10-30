/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication;

import rife.tools.StringEncryptor;

/**
 * Credentials managers that can encrypt passwords implement this interface.
 * The authentication element deployer will pass them a password encryptor
 * based on the element configuration.
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.6
 */
public interface PasswordEncrypting {
    /**
     * Sets the password encryptor to use to encrypt this credentials
     * manager's passwords.
     *
     * @param passwordEncryptor the password ecryptor that will be used
     * @since 1.6
     */
    void setPasswordEncryptor(StringEncryptor passwordEncryptor);
}
