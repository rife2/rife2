/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class GetContentErrorException extends ProcessingException {
    @Serial private static final long serialVersionUID = 6372141337279582707L;

    private String path_ = null;

    public GetContentErrorException(String path, Throwable cause) {
        super("Error while obtaining the content of resource '" + path + "'.", cause);

        path_ = path;
    }

    public String getPath() {
        return path_;
    }
}
