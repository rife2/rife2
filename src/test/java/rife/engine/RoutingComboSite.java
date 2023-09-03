/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class RoutingComboSite extends Site {
    public void setup() {
        get("/combo1", RoutingGetSite.GetElement.class);
        get("/combo2", PathInfoHandling.CAPTURE, RoutingGetSite.GetPathInfoElement.class);
        get("/combo3", c -> c.print("get element"));
        get("/combo4", PathInfoHandling.CAPTURE, c -> c.print("get element path info:" + c.pathInfo()));
        get("/combo5", RoutingGetSite.GetElement::new);
        get("/combo6", PathInfoHandling.CAPTURE, RoutingGetSite.GetPathInfoElement::new);

        post("/combo1", RoutingPostSite.PostElement.class);
        post("/combo2", PathInfoHandling.CAPTURE, RoutingPostSite.PostPathInfoElement.class);
        post("/combo3", c -> c.print("post element"));
        post("/combo4", PathInfoHandling.CAPTURE, c -> c.print("post element path info:" + c.pathInfo()));
        post("/combo5", RoutingPostSite.PostElement::new);
        post("/combo6", PathInfoHandling.CAPTURE, RoutingPostSite.PostPathInfoElement::new);

        put("/combo1", RoutingPutSite.PutElement.class);
        put("/combo2", PathInfoHandling.CAPTURE, RoutingPutSite.PutPathInfoElement.class);
        put("/combo3", c -> c.print("put element"));
        put("/combo4", PathInfoHandling.CAPTURE, c -> c.print("put element path info:" + c.pathInfo()));
        put("/combo5", RoutingPutSite.PutElement::new);
        put("/combo6", PathInfoHandling.CAPTURE, RoutingPutSite.PutPathInfoElement::new);

        delete("/combo1", RoutingDeleteSite.DeleteElement.class);
        delete("/combo2", PathInfoHandling.CAPTURE, RoutingDeleteSite.DeletePathInfoElement.class);
        delete("/combo3", c -> c.print("delete element"));
        delete("/combo4", PathInfoHandling.CAPTURE, c -> c.print("delete element path info:" + c.pathInfo()));
        delete("/combo5", RoutingDeleteSite.DeleteElement::new);
        delete("/combo6", PathInfoHandling.CAPTURE, RoutingDeleteSite.DeletePathInfoElement::new);

        patch("/combo1", RoutingPatchSite.PatchElement.class);
        patch("/combo2", PathInfoHandling.CAPTURE, RoutingPatchSite.PatchPathInfoElement.class);
        patch("/combo3", c -> c.print("patch element"));
        patch("/combo4", PathInfoHandling.CAPTURE, c -> c.print("patch element path info:" + c.pathInfo()));
        patch("/combo5", RoutingPatchSite.PatchElement::new);
        patch("/combo6", PathInfoHandling.CAPTURE, RoutingPatchSite.PatchPathInfoElement::new);
    }
}
