/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

public class RendererImpl implements ValueRenderer {
    public static int sCount = 0;

    public String render(Template template, String valueName, String differentiator) {
        return valueName.toUpperCase() + differentiator + ":" + (++sCount);
    }
}
