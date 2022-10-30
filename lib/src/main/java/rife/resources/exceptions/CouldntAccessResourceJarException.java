/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class CouldntAccessResourceJarException extends ResourceFinderErrorException {
    @Serial
    private static final long serialVersionUID = -7524303430166484247L;

    private final String jarFileName_;
    private final String entryFileName_;

    public CouldntAccessResourceJarException(String jarFileName, String entryFileName) {
        super("The jar file '" + jarFileName + "' couldn't be found to read the '" + entryFileName + "' entry from.");

        jarFileName_ = jarFileName;
        entryFileName_ = entryFileName;
    }

    public String getJarFileName() {
        return jarFileName_;
    }

    public String getEntryFileName() {
        return entryFileName_;
    }
}
