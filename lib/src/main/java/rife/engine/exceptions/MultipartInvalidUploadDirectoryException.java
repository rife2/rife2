/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.File;
import java.io.Serial;

public class MultipartInvalidUploadDirectoryException extends MultipartRequestException {
    @Serial
    private static final long serialVersionUID = 2872851131466908315L;

    private File directory_ = null;

    public MultipartInvalidUploadDirectoryException(File directory) {
        super("Invalid upload directory '" + directory.getAbsolutePath() + "'.");

        directory_ = directory;
    }

    public File getDirectory() {
        return directory_;
    }
}
