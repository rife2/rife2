/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingPatchSite extends Site {
    public static class PatchElement implements Element {
        public void process(Context c) {
            c.print("class PatchElement");
        }
    }

    public static class PatchPathInfoElement implements Element {
        public void process(Context c) {
            c.print("class PatchPathInfoElement:" + c.pathInfo());
        }
    }

    public void setup() {
        patch(PatchElement.class);
        patch(PathInfoHandling.CAPTURE, PatchPathInfoElement.class);
        patch("/patch3", PatchElement.class);
        patch("/patch4", PathInfoHandling.CAPTURE, PatchPathInfoElement.class);
        patch("/patch5", c -> c.print("patch element"));
        patch("/patch6", PathInfoHandling.CAPTURE, c -> c.print("patch element path info:" + c.pathInfo()));
    }
}
