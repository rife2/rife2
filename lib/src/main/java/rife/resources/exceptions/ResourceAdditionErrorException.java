/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class ResourceAdditionErrorException extends ResourceWriterErrorException {
    @Serial private static final long serialVersionUID = 5975240130712585022L;

    private final String name_;
    private final String content_;

    public ResourceAdditionErrorException(String name, String content, Throwable e) {
        super("Error while adding the resource with name '" + name + "' and content '" + content + "'.", e);

        name_ = name;
        content_ = content;
    }

    public String getName() {
        return name_;
    }

    public String getContent() {
        return content_;
    }
}
