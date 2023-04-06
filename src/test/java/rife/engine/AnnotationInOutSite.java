/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.*;

import static rife.engine.annotations.FlowDirection.IN_OUT;

public class AnnotationInOutSite extends Site {
    public static class BaseElement implements Element {
        @Body(flow = IN_OUT) int intBody = -1;
        @Cookie(flow = IN_OUT) int intCookie = -2;
        @RequestAttribute(value = "requestAttr2", flow = IN_OUT) String stringRequestAttribute2 = "defaultRequestAttribute2";
        @SessionAttribute(value = "sessionAttr2", flow = IN_OUT) String stringSessionAttribute2 = "defaultSessionAttribute2";
        @Header(flow = IN_OUT) int intHeader = -8;

        public void process(Context c) {
        }
    }

    public static class AnnotatedElement extends BaseElement {
        @Body(flow = IN_OUT) String stringBody = "defaultBody";

        @Cookie(flow = IN_OUT) String stringCookie = "defaultCookie";
        @Cookie(value = "cookie2", flow = IN_OUT) String stringCookie2 = "defaultCookie2";
        @Cookie(value = "cookie3", flow = IN_OUT) int intCookie2 = -3;

        @RequestAttribute(flow = IN_OUT) String stringRequestAttribute = "defaultRequestAttribute";
        @RequestAttribute(flow = IN_OUT) int intRequestAttribute = -4;
        @RequestAttribute(value = "requestAttr3", flow = IN_OUT) int intRequestAttribute2 = -5;

        @SessionAttribute(flow = IN_OUT) String stringSessionAttribute = "defaultSessionAttribute";
        @SessionAttribute(flow = IN_OUT) int intSessionAttribute = -6;
        @SessionAttribute(value = "sessionAttr3", flow = IN_OUT) int intSessionAttribute2 = -7;

        @Header(flow = IN_OUT) String stringHeader = "defaultHeader";
        @Header(value = "header2", flow = IN_OUT) String stringHeader2 = "defaultHeader2";
        @Header(value = "header3", flow = IN_OUT) int intHeader2 = -9;

        public void process(Context c) {
            super.process(c);

            c.print(stringBody + "\n");
            c.print(intBody + "\n");

            c.print(stringCookie + "\n");
            c.print(intCookie + "\n");
            c.print(stringCookie2 + "\n");
            c.print(intCookie2 + "\n");

            c.print(stringRequestAttribute + "\n");
            c.print(intRequestAttribute + "\n");
            c.print(stringRequestAttribute2 + "\n");
            c.print(intRequestAttribute2 + "\n");

            c.print(stringSessionAttribute + "\n");
            c.print(intSessionAttribute + "\n");
            c.print(stringSessionAttribute2 + "\n");
            c.print(intSessionAttribute2 + "\n");

            c.print(stringHeader + "\n");
            c.print(intHeader + "\n");
            c.print(stringHeader2 + "\n");
            c.print(intHeader2 + "\n");

            if (c.parameterBoolean("generateOut")) {
                stringBody = "outValue1";
                intBody = 2002;
                stringCookie = "outValue3";
                intCookie = 2004;
                stringCookie2 = "outValue5";
                intCookie2 = 2006;
                stringRequestAttribute = "outValue7";
                intRequestAttribute = 2008;
                stringRequestAttribute2 = "outValue9";
                intRequestAttribute2 = 2010;
                stringSessionAttribute = "outValue11";
                intSessionAttribute = 2012;
                stringSessionAttribute2 = "outValue13";
                intSessionAttribute2 = 2014;
                stringHeader = "outValue15";
                intHeader = 2016;
                stringHeader2 = "outValue17";
                intHeader2 = 2018;
            }
        }
    }

    public void setup() {
        before(c -> {
            if (c.parameterBoolean("generateIn")) {
                c.setAttribute("stringRequestAttribute", "inValue7");
                c.setAttribute("intRequestAttribute", "1008");
                c.setAttribute("requestAttr2", "inValue11");
                c.setAttribute("requestAttr3", "1012");
                c.session().setAttribute("stringSessionAttribute", "inValue13");
                c.session().setAttribute("intSessionAttribute", "1014");
                c.session().setAttribute("sessionAttr2", "inValue15");
                c.session().setAttribute("sessionAttr3", "1016");
            }
        });
        get("/get", AnnotatedElement.class);
        post("/post", AnnotatedElement.class);
        after(c -> {
            c.print(c.attribute("stringRequestAttribute") + "\n");
            c.print(c.attribute("intRequestAttribute") + "\n");
            c.print(c.attribute("requestAttr2") + "\n");
            c.print(c.attribute("requestAttr3") + "\n");
            c.print(c.session(false).attribute("stringSessionAttribute") + "\n");
            c.print(c.session(false).attribute("intSessionAttribute") + "\n");
            c.print(c.session(false).attribute("sessionAttr2") + "\n");
            c.print(c.session(false).attribute("sessionAttr3") + "\n");
        });
    }
}
