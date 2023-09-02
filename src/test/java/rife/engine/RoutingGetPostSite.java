/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingGetPostSite extends Site {
    public static class GetPostElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class GetPostElement");
        }
    }

    public static class GetPostPathInfoElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class GetPostPathInfoElement:" + c.pathInfo());
        }
    }

    @Override
    public void setup() {
        getPost(GetPostElement.class);
        getPost(PathInfoHandling.CAPTURE, GetPostPathInfoElement.class);
        getPost("/getPost3", GetPostElement.class);
        getPost("/getPost4", PathInfoHandling.CAPTURE, GetPostPathInfoElement.class);
        getPost("/getPost5", c -> c.print("getPost element"));
        getPost("/getPost6", PathInfoHandling.CAPTURE, c -> c.print("getPost element path info:" + c.pathInfo()));
        group("/supplier", new Router() {
                @Override
                public void setup() {
                    getPost(GetPostElement::new);
                    getPost(PathInfoHandling.CAPTURE, GetPostPathInfoElement::new);
                    getPost("/getPost3", GetPostElement::new);
                    getPost("/getPost4", PathInfoHandling.CAPTURE, GetPostPathInfoElement::new);
                }
            }
        );
    }
}
