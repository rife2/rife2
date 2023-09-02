/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingPatchSite extends Site {
    public static class PatchElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class PatchElement");
        }
    }

    public static class PatchPathInfoElement implements Element {
        @Override
        public void process(Context c) {
            c.print("class PatchPathInfoElement:" + c.pathInfo());
        }
    }

    @Override
    public void setup() {
        patch(PatchElement.class);
        patch(PathInfoHandling.CAPTURE, PatchPathInfoElement.class);
        patch("/patch3", PatchElement.class);
        patch("/patch4", PathInfoHandling.CAPTURE, PatchPathInfoElement.class);
        patch("/patch5", c -> c.print("patch element"));
        patch("/patch6", PathInfoHandling.CAPTURE, c -> c.print("patch element path info:" + c.pathInfo()));
        group("/supplier", new Router() {
                @Override
                public void setup() {
                    patch(PatchElement::new);
                    patch(PathInfoHandling.CAPTURE, PatchPathInfoElement::new);
                    patch("/patch3", PatchElement::new);
                    patch("/patch4", PathInfoHandling.CAPTURE, PatchPathInfoElement::new);
                }
            }
        );
    }
}
