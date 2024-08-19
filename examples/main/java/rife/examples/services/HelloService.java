/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples.services;

import rife.examples.apis.MyService;

public class HelloService implements MyService {
    @Override
    public String serviceApi() {
        return "Hello World";
    }
}
