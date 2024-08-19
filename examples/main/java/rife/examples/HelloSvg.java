/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.Server;
import rife.engine.Site;

import java.util.Calendar;

public class HelloSvg extends Site {
    static final int DEGREES = 360;
    static final int H_UNIT = DEGREES / 12;
    static final int M_UNIT = DEGREES / 60;

    public void setup() {
        get("/clock", c -> {
            var t = c.templateSvg("HelloSvg");
            for (int h = 0; h < DEGREES; h += H_UNIT) {
                t.setValue("degree", h);
                t.appendBlock("marks", "hour-mark");

                for (int m = h + M_UNIT; m < h + H_UNIT; m += M_UNIT) {
                    t.setValue("degree", m);
                    t.appendBlock("marks", "minute-mark");
                }
            }

            var now = Calendar.getInstance();
            var hour = now.get(Calendar.HOUR_OF_DAY);
            var minute = now.get(Calendar.MINUTE);
            var second = now.get(Calendar.SECOND);
            t.setValue("degree", (hour + (minute / 60.0)) * H_UNIT);
            t.appendBlock("wands", "hour-wand");
            t.setValue("degree", (minute + (second / 60.0)) * M_UNIT);
            t.appendBlock("wands", "minute-wand");
            t.setValue("degree", second * M_UNIT);
            t.appendBlock("wands", "second-wand");

            c.print(t);
        });
    }

    public static void main(String[] args) {
        new Server().start(new HelloSvg());
    }
}
