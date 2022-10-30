/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class UnsupportedResourceProtocolException extends ResourceFinderErrorException {
    @Serial
    private static final long serialVersionUID = 3763519896520670663L;

    private final String fileName_;
    private final String protocol_;

    public UnsupportedResourceProtocolException(String fileName, String protocol) {
        super("The resource '" + fileName + "'  has the '" + protocol + "' protocol, which isn't supported.");

        fileName_ = fileName;
        protocol_ = protocol;
    }

    public String getFileName() {
        return fileName_;
    }

    public String getProtocol() {
        return protocol_;
    }
}
