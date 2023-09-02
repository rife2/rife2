/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingGetSite extends Site {
    public static class GetElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class GetElement");
        }
    }

    public static class GetPathInfoElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class GetPathInfoElement:" + c.pathInfo());
        }
    }

    @Override
    public void setup() {
        get(GetElement.class);
        get(PathInfoHandling.CAPTURE, GetPathInfoElement.class);
        get("/get3", GetElement.class);
        get("/get4", PathInfoHandling.CAPTURE, GetPathInfoElement.class);
        get("/get5", c -> c.print("get element"));
        get("/get6", PathInfoHandling.CAPTURE, c -> c.print("get element path info:" + c.pathInfo()));
        group("/supplier", new Router() {
                @Override
                public void setup() {
                    get(GetElement::new);
                    get(PathInfoHandling.CAPTURE, GetPathInfoElement::new);
                    get("/get3", GetElement::new);
                    get("/get4", PathInfoHandling.CAPTURE, GetPathInfoElement::new);
                }
            }
        );
    }
}
