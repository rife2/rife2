/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class ModificationTimeErrorException extends ProcessingException {
    @Serial private static final long serialVersionUID = -6892522819661669305L;

    private String path_ = null;

    public ModificationTimeErrorException(String path, Throwable cause) {
        super("Error while obtaining the modification time of resource '" + path + "'.", cause);
        path_ = path;
    }

    public String getPath() {
        return path_;
    }
}
