/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;

public class HelloAll extends Site {
    Router authentication;
    Router cmf;
    Router generation;
    Router continuations;
    Router gqm;
    Router group;

    public void setup() {
        group(authentication = new HelloAuthentication());
        group("/content", cmf = new HelloContentManagement());
        group(new HelloContinuations());
        group(new HelloCounterContinuations());
        group(new HelloDatabase());
        group(new HelloErrors());
        group(new HelloForm());
        group("/generation", generation = new HelloFormGeneration());
        group("/continuations", continuations = new HelloFormContinuations());
        group("/generic", gqm = new HelloGenericQueryManager());
        group(group = new HelloGroup());
        group(new HelloLink());
        group(new HelloPathInfoMapping());
        group("/scheduler", new HelloScheduler());
        group(new HelloSvg());
        group(new HelloTemplate());
        group("/validation", new HelloValidation());
        group(new HelloWorld());

        get("/", c -> c.print(c.template("HelloAll")));
    }

    public static void main(String[] args) {
        new Server().start(new HelloAll());
    }
}
