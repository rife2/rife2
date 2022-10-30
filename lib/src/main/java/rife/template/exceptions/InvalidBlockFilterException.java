/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class InvalidBlockFilterException extends TemplateException {
    @Serial private static final long serialVersionUID = -4317974249211817030L;

    private String blockFilter_ = null;

    public InvalidBlockFilterException(String blockFilter) {
        super("The block filter " + blockFilter + " is an invalid regular expression.");
        blockFilter_ = blockFilter;
    }

    public String getBlockFilter() {
        return blockFilter_;
    }
}
