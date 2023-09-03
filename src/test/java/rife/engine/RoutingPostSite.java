/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingPostSite extends Site {
    public static class PostElement implements Element {
        public void process(Context c) {
            c.print("class PostElement");
        }
    }

    public static class PostPathInfoElement implements Element {
        public void process(Context c) {
            c.print("class PostPathInfoElement:" + c.pathInfo());
        }
    }

    public void setup() {
        post(PostElement.class);
        post(PathInfoHandling.CAPTURE, PostPathInfoElement.class);
        post("/post3", PostElement.class);
        post("/post4", PathInfoHandling.CAPTURE, PostPathInfoElement.class);
        post("/post5", c -> c.print("post element"));
        post("/post6", PathInfoHandling.CAPTURE, c -> c.print("post element path info:" + c.pathInfo()));
        group("/supplier", new Router() {
                public void setup() {
                    post(PostElement::new);
                    post(PathInfoHandling.CAPTURE, PostPathInfoElement::new);
                    post("/post3", PostElement::new);
                    post("/post4", PathInfoHandling.CAPTURE, PostPathInfoElement::new);
                }
            }
        );
    }
}
