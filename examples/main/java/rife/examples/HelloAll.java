/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.database.Datasource;
import rife.engine.Router;
import rife.engine.Server;
import rife.engine.Site;
import rife.examples.apis.MyService;
import rife.examples.apis.MyServiceProvider;
import rife.examples.services.HelloService;

public class HelloAll extends Site implements MyServiceProvider {
    Router authentication;
    Router cmf;
    Router cookieFlow;
    HelloDependencyInjection dependencyInjection;
    Router generation;
    Router continuations;
    Router gqm;
    Router group;
    Router resources;

    public MyService getMyService() {
        return dependencyInjection.getMyService();
    }

    public void setup() {
        group(authentication = new HelloAuthentication());
        group("/content", cmf = new HelloContentManagement());
        group(new HelloContinuations());
        group("/cookie", cookieFlow = new HelloCookieFlow());
        group(new HelloCounterContinuations());
        group(new HelloDatabase());
        group(dependencyInjection = new HelloDependencyInjection(new HelloService()));
        group(new HelloErrors());
        group(new HelloForm());
        group("/generation", generation = new HelloFormGeneration());
        group("/continuations", continuations = new HelloFormContinuations());
        group("/generic", gqm = new HelloGenericQueryManager());
        group(group = new HelloGroup());
        group(new HelloLink());
        group(new HelloPathInfoMapping());
        group("/resources", resources = new HelloResources());
        group("/scheduler", new HelloScheduler());
        group(new HelloSvg());
        group(new HelloTemplate());
        group("/validation", new HelloValidation());
        group(new HelloWorkflow());
        group(new HelloWorld());

        get("/", c -> c.print(c.template("HelloAll")));
    }

    public static void main(String[] args) {
        new Server().start(new HelloAll())
            .properties().put("datasource",
                new Datasource("org.apache.derby.jdbc.EmbeddedDriver",
                    "jdbc:derby:./embedded_dbs/hello;create=true", "", "", 5));
    }
}
