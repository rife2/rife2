/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingDeleteSite extends Site {
    public static class DeleteElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class DeleteElement");
        }
    }

    public static class DeletePathInfoElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class DeletePathInfoElement:" + c.pathInfo());
        }
    }

    @Override
    public void setup() {
        delete(DeleteElement.class);
        delete(PathInfoHandling.CAPTURE, DeletePathInfoElement.class);
        delete("/delete3", DeleteElement.class);
        delete("/delete4", PathInfoHandling.CAPTURE, DeletePathInfoElement.class);
        delete("/delete5", c -> c.print("delete element"));
        delete("/delete6", PathInfoHandling.CAPTURE, c -> c.print("delete element path info:" + c.pathInfo()));
        group("/supplier", new Router() {
                @Override
                public void setup() {
                    delete(DeleteElement::new);
                    delete(PathInfoHandling.CAPTURE, DeletePathInfoElement::new);
                    delete("/delete3", DeleteElement::new);
                    delete("/delete4", PathInfoHandling.CAPTURE, DeletePathInfoElement::new);
                }
            }
        );
    }
}
