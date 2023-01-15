/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class GetBeanSite extends Site {
    private final String prefix_;

    public GetBeanSite() {
        this("");
    }
    public GetBeanSite(String prefix) {
        prefix_ = prefix;
    }

    public void setup() {
        route("/bean/get", c -> {
            switch (c.method()) {
                case GET -> {
                    c.print("<form name=\"submissionform\" action=\"/bean/get\" method=\"post\" enctype=\"multipart/form-data\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "enum\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "string\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "stringbuffer\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "int\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "integer\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "char\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "character\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "boolean\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "booleanObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "byte\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "byteObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "double\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "doubleObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "float\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "floatObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "long\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "longObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "short\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "shortObject\">");
                    c.print("<input type=\"file\" name=\"" + prefix_ + "stringFile\"/>");
                    c.print("<input type=\"file\" name=\"" + prefix_ + "bytesFile\"/>");
                    c.print("<input type=\"file\" name=\"" + prefix_ + "streamFile\"/>");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "date\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "dateFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "datesFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "datesFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instant\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instantFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instantsFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instantsFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParam\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParams\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParams\">");
                    c.print("<input id=\"beanSubmit\" type=\"submit\">");
                    c.print("</form");
                }
                case POST -> {
                    BeanImpl bean;
                    if (prefix_.isEmpty()) {
                        bean = c.parametersBean(BeanImpl.class);
                    } else {
                        bean = c.parametersBean(BeanImpl.class, prefix_);
                    }

                    bean.printToContext(c);
                }
            }
        });
    }
}
