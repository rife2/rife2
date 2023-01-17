/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class TemplateNotFoundException extends ProcessingException {
    @Serial private static final long serialVersionUID = -2850049322836906728L;
    private String mName = null;

    public TemplateNotFoundException(String name, Throwable cause) {
        super("Couldn't find template '" + name + "'.", cause);
        mName = name;
    }

    public String getName() {
        return mName;
    }
}
