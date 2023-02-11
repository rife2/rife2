/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.apis.ServiceProvider;
import rife.engine.*;
import rife.engine.annotations.ActiveSite;
import rife.services.HelloService;

public class HelloDependencyInjection extends Site {
    public static class ServiceUser implements Element {
        final ServiceProvider service_;

        public ServiceUser(ServiceProvider service) {
            service_ = service;
        }

        public void process(Context c) {
            c.print(service_.serviceApi());
        }
    }

    public static class ServiceUser2 implements Element {
        @ActiveSite HelloDependencyInjection site_;

        public void process(Context c) {
            c.print(site_.service_.serviceApi());
        }
    }

    final ServiceProvider service_;

    public HelloDependencyInjection(ServiceProvider service) {
        this.service_ = service;
    }

    public void setup() {
        get("/service1", () -> new ServiceUser(service_));
        get("/service2", ServiceUser2::new);
    }

    public static void main(String[] args) {
        new Server().start(new HelloDependencyInjection(new HelloService()));
    }
}
