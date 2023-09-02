/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingRouteSite extends Site {
    public static class RouteElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class RouteElement");
        }
    }

    public static class RoutePathInfoElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class RoutePathInfoElement:" + c.pathInfo());
        }
    }

    @Override
    public void setup() {
        route(RouteElement.class);
        route(PathInfoHandling.CAPTURE, RoutePathInfoElement.class);
        route("/route3", RouteElement.class);
        route("/route4", PathInfoHandling.CAPTURE, RoutePathInfoElement.class);
        route("/route5", c -> c.print("route element"));
        route("/route6", PathInfoHandling.CAPTURE, c -> c.print("route element path info:" + c.pathInfo()));
        group("/supplier", new Router() {
                @Override
                public void setup() {
                    route(RouteElement::new);
                    route(PathInfoHandling.CAPTURE, RoutePathInfoElement::new);
                    route("/route3", RouteElement::new);
                    route("/route4", PathInfoHandling.CAPTURE, RoutePathInfoElement::new);
                }
            }
        );
    }
}
