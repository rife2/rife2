/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.exceptions;

import rife.cmf.dam.exceptions.ContentManagerException;

import java.io.Serial;

public class UnknownContentRepositoryException extends ContentManagerException {
    @Serial private static final long serialVersionUID = -7745441411433107874L;

    private final String repositoryName_;

    public UnknownContentRepositoryException(String repositoryName) {
        super("The repository '" + repositoryName + "' doesn't exist.");

        repositoryName_ = repositoryName;
    }

    public String getRepositoryName() {
        return repositoryName_;
    }
}
