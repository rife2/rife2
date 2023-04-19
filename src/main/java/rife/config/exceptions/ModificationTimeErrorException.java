/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import rife.resources.exceptions.ResourceFinderErrorException;

import java.io.Serial;

public class ModificationTimeErrorException extends ConfigErrorException {
    @Serial
    private static final long serialVersionUID = 7732505426871416111L;

    private String xmlPath_ = null;

    public ModificationTimeErrorException(String xmlPath, ResourceFinderErrorException cause) {
        super("Error while retrieving the modification time of '" + xmlPath + "'", cause);

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
