/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.exceptions;

import java.io.Serial;

public class MissingMarkingBlockException extends ValidationBuilderException {
    @Serial private static final long serialVersionUID = 6014580362473175137L;

    private final String blockId_;

    public MissingMarkingBlockException(String blockId) {
        super("The template requires the '" + blockId + "' block to be able to generate the validation markings.");

        blockId_ = blockId;
    }

    public String getBlockId() {
        return blockId_;
    }
}
