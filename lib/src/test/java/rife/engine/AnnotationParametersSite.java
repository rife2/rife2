/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.*;
import rife.tools.exceptions.FileUtilsErrorException;

public class AnnotationParametersSite extends Site {
    Route routeOut;
    Route routeIn;
    Route routePathInfo;

    public static class ParentInElement implements Element {
        @Parameter int intParam = -4;
        @Parameter("param3") int intParam2 = -5;

        public void process(Context c) {
        }
    }

    public static class AnnotatedInElement extends ParentInElement {
        @Parameter String stringParam = "defaultParam";
        @Parameter("param2") String stringParam2 = "defaultParam2";
        @Parameter("param6") String stringParam3 = "defaultParam3";

        public void process(Context c) {
            super.process(c);

            c.print(stringParam + "\n");
            c.print(intParam + "\n");
            c.print(stringParam2 + "\n");
            c.print(intParam2 + "\n");
        }
    }

    public static class ParentOutElement implements Element {
        @Parameter(value = "param2", flow = FlowDirection.OUT) String stringParam2 = "value3";
        @Parameter int switchRoute = 0;

        public void process(Context c) {
        }
    }

    public static class AnnotatedOutElement extends ParentOutElement {
        @Parameter(flow = FlowDirection.OUT) String stringParam = "value1";
        @Parameter(flow = FlowDirection.OUT) int intParam = 222;
        @Parameter(value = "param3", flow = FlowDirection.OUT) int intParam2 = 444;
        @Parameter(value = "param5", flow = FlowDirection.OUT) String stringParam3 = "value5";

        public void process(Context c) {
            super.process(c);

            switch (switchRoute) {
                case 1 -> c.print(c.urlFor(((AnnotationParametersSite) c.site()).routeIn));
                case 2 -> c.print(c.urlFor(((AnnotationParametersSite) c.site()).routePathInfo));
            }
        }
    }

    public void setup() {
        routeOut = get("/out", AnnotatedOutElement.class);
        routeIn = get("/in", AnnotatedInElement.class);
        routePathInfo = get("/pathinfo", PathInfoHandling.MAP(m -> m.t("some").s().p("intParam", "\\d+").s().p("param3")), AnnotatedInElement.class);
    }
}
