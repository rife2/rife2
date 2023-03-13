/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

public class RendererImpl2 implements ValueRenderer {
    public String render(Template template, String valueId, String differentiator) {
        if (differentiator != null && template.hasValueId(differentiator)) {
            return template.getValue(differentiator).substring(0, 13) + "...";
        } else {
            return "";
        }
    }
}