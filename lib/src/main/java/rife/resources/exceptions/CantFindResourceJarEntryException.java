/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.exceptions;

import java.io.Serial;

public class CantFindResourceJarEntryException extends ResourceFinderErrorException {
    @Serial
    private static final long serialVersionUID = 5184216632613011807L;

    private final String jarFileName_;
    private final String entryFileName_;

    public CantFindResourceJarEntryException(String jarFileName, String entryFileName, Throwable cause) {
        super("The jar file '" + jarFileName + "' couldn't be found to read the '" + entryFileName + "' entry from.", cause);

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
