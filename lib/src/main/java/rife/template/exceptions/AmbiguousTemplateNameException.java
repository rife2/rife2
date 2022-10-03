/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class AmbiguousTemplateNameException extends ProcessingException {
    @Serial private static final long serialVersionUID = -1908700295942542497L;

    private String name_ = null;

    public AmbiguousTemplateNameException(String name) {
        super("The template '" + name + "' can't be looked up in a predictable way. You can either provide template names as class names or as file names and the correct one will be picked up. However by asking for '" + name + "' this can't be done for certain. To access this template, look it up as a class name. This prevents you from having to provide the file extension.");
        name_ = name;
    }

    public String getName() {
        return name_;
    }
}
