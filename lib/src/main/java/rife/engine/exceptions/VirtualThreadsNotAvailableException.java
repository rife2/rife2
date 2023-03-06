/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import java.io.Serial;

public class VirtualThreadsNotAvailableException extends EngineException {
	@Serial private static final long serialVersionUID = 3586122234816173614L;

    public VirtualThreadsNotAvailableException(Throwable e) {
        super("Unable to create a virtual thread pool because virtual threads are not available on this system.", e);
    }
}
