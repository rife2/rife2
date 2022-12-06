/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.*;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;

public class AnnotationParametersSite extends Site {
    Route routeOut;
    Route routeIn;
    Route routePathInfo;

    public static class AnnotatedInElement implements Element {
        @Parameter String stringParam = "defaultParam";
        @Parameter int intParam = -4;
        @Parameter(name = "param2") String stringParam2 = "defaultParam2";
        @Parameter(name = "param3") int intParam2 = -5;
        @Parameter(name = "param6") String stringParam3 = "defaultParam3";

        public void process(Context c)
        throws FileUtilsErrorException {
            c.print(stringParam + "\n");
            c.print(intParam + "\n");
            c.print(stringParam2 + "\n");
            c.print(intParam2 + "\n");
        }
    }

    public static class AnnotatedOutElement implements Element {
        @Parameter(flow = FlowDirection.OUT) String stringParam = "value1";
        @Parameter(flow = FlowDirection.OUT) int intParam = 222;
        @Parameter(name = "param2", flow = FlowDirection.OUT) String stringParam2 = "value3";
        @Parameter(name = "param3", flow = FlowDirection.OUT) int intParam2 = 444;
        @Parameter(name = "param5", flow = FlowDirection.OUT) String stringParam3 = "value5";
        @Parameter int switchRoute = 0;

        public void process(Context c)
        throws FileUtilsErrorException {
            switch (switchRoute) {
                case 1 -> c.print(c.urlFor(((AnnotationParametersSite)c.site()).routeIn));
                case 2 -> c.print(c.urlFor(((AnnotationParametersSite)c.site()).routePathInfo));
            }
        }
    }

    public void setup() {
        routeOut = get("/out", AnnotatedOutElement.class);
        routeIn = get("/in", AnnotatedInElement.class);
        routePathInfo = get("/pathinfo", PathInfoHandling.MAP(m -> m.t("some").s().p("intParam", "\\d+").s().p("param3")), AnnotatedInElement.class);
    }
}
