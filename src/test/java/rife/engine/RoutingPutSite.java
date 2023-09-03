/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingPutSite extends Site {
    public static class PutElement implements Element {
        public void process(Context c) {
            c.print("class PutElement");
        }
    }

    public static class PutPathInfoElement implements Element {
        public void process(Context c) {
            c.print("class PutPathInfoElement:" + c.pathInfo());
        }
    }

    public void setup() {
        put(PutElement.class);
        put(PathInfoHandling.CAPTURE, PutPathInfoElement.class);
        put("/put3", PutElement.class);
        put("/put4", PathInfoHandling.CAPTURE, PutPathInfoElement.class);
        put("/put5", c -> c.print("put element"));
        put("/put6", PathInfoHandling.CAPTURE, c -> c.print("put element path info:" + c.pathInfo()));
        group("/supplier", new Router() {
                public void setup() {
                    put(PutElement::new);
                    put(PathInfoHandling.CAPTURE, PutPathInfoElement::new);
                    put("/put3", PutElement::new);
                    put("/put4", PathInfoHandling.CAPTURE, PutPathInfoElement::new);
                }
            }
        );
    }
}
