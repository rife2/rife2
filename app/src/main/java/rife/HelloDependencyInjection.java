/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.apis.MyService;
import rife.apis.ServiceProvider;
import rife.database.Datasource;
import rife.engine.*;
import rife.engine.annotations.ActiveSite;
import rife.engine.annotations.Property;
import rife.services.HelloService;

public class HelloDependencyInjection extends Site implements ServiceProvider {
    final MyService service;

    public HelloDependencyInjection(MyService service) {
        this.service = service;
    }

    public MyService getMyService() {
        return service;
    }

    // Element implementations

    public static class ServiceUser implements Element {
        final MyService service;
        @Property Datasource datasource;

        public ServiceUser(MyService service) {
            this.service = service;
        }

        public void process(Context c) {
            c.print(service.serviceApi());
            c.print("<br>");
            c.print(datasource.getDriver());
        }
    }

    public static class ServiceUser2 implements Element {
        @ActiveSite ServiceProvider provider;
        @Property Datasource datasource;

        public void process(Context c) {
            c.print(provider.getMyService().serviceApi());
            c.print("<br>");
            c.print(datasource.getDriver());
        }
    }

    // Site setup

    public void setup() {
        get("/service1", () -> new ServiceUser(service));
        group(new Router() {
            public void setup() {
                get("/service2", ServiceUser2.class);
            }
        }).properties().put("datasource",
            new Datasource("org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:.", "sa", "", 5));
    }

    // Main

    public static void main(String[] args) {
        new Server()
            .start(new HelloDependencyInjection(new HelloService()))
            .properties().put("datasource",
                new Datasource("org.h2.Driver",
                    "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5));
    }
}
