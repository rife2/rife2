/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml.exceptions;

import rife.tools.StringUtils;

import java.io.Serial;
import java.util.Collection;

public class FatalParsingErrorsException extends XmlErrorException {
    @Serial private static final long serialVersionUID = 1286210792114678095L;

    private final String xmlPath_;
    private final Collection<String> fatalErrors_;

    public FatalParsingErrorsException(String xmlPath, Collection<String> fatalErrors) {
        super("The following fatal XML errors occurred during the parsing of " + xmlPath + "'\n" + StringUtils.join(fatalErrors, "\n"));

        xmlPath_ = xmlPath;
        fatalErrors_ = fatalErrors;
    }

    public String getXmlPath() {
        return xmlPath_;
    }

    public Collection<String> getFatalErrors() {
        return fatalErrors_;
    }
}
