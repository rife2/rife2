/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;
import rife.engine.annotations.Parameter;
import rife.server.Server;
import rife.template.Template;

public class TestSite extends Site {
    public static void main(String[] args) {
        new Server().port(4242).start(new TestSite());
    }

    public class AuthenticatedRoutes extends Router {
        public Route test = get("/", c -> {

        });
    }

    public void setup() {
        before(BeforeElement.class, AfterElement.class);
        before(c -> {

        }, c -> {

        });
        after(AfterElement.class);

        get("/inlined", c -> {
            c.print("test");
        });
        get("/parameters", Checkout.class);

        group(new Router() {
            public void setup() {

            }
        });
        g.test.path();

    }

    AuthenticatedRoutes g = group(new AuthenticatedRoutes());

    public static class Checkout implements Element {
        @Parameter String one;
        @Parameter int two;

        @Override
        public void process(Context context) {
            Template template = context.templateHtml("Example");
            template.setValue("one", one);
            template.setValue("two", two);
            context.print(template);
        }
    }

    public static class BeforeElement implements Element {
        public void process(Context c)
        throws Exception {

        }
    }

    public static class AfterElement implements Element {
        public void process(Context c)
        throws Exception {

        }
    }
}
