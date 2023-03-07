/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli;

import rife.tools.exceptions.FileUtilsErrorException;

public interface CliCommand {
    boolean execute()
    throws Exception;

    default String getHelp() {
        return "";
    }
}
