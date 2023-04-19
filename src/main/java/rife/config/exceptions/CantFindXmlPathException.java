/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import java.io.Serial;

public class CantFindXmlPathException extends ConfigErrorException {
    @Serial
    private static final long serialVersionUID = 379094904515534057L;

    private String xmlPath_ = null;

    public CantFindXmlPathException(String xmlPath) {
        super("The xml path '" + xmlPath + "' can't be found.");

        xmlPath_ = xmlPath;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
