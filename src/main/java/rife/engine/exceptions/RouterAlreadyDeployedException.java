/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import rife.engine.Router;

import java.io.Serial;

public class RouterAlreadyDeployedException extends EngineException {
	@Serial private static final long serialVersionUID = 8335356303557183071L;

    private final Router router_;

    public RouterAlreadyDeployedException(Router router) {
        super("The router is already deployed, changes to the routing are prohibited.");

        router_ = router;
    }

    public Router getRouter() {
        return router_;
    }
}
