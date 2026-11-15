/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;

public class HelloSiteRunner extends Site {
    Route count = getPost("/count", HelloCounterContinuations.Counter::new);
    Route greet = post("/greet", c -> c.print("hello " + c.parameter("name")));

    public static void main(String[] args) {
        try (var runner = new SiteRunner().scheme("hello").serverName("app").start(new HelloSiteRunner())) {
            System.out.println(runner.handle("/greet", new SiteRequest(RequestMethod.POST)
                .contentType("application/x-www-form-urlencoded")
                .body("name=runner")).getText());

            // the counter continues with the link that it printed
            var first = runner.handle("/count").getText();
            System.out.println(first);
            var link = first.substring(first.indexOf("href='") + 6, first.lastIndexOf("'"));
            System.out.println(runner.handle(link).getText());

            System.out.println("/missing handled: " + runner.handle("/missing").isHandled());
        }
    }
}
