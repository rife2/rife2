/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;
import rife.engine.annotations.Parameter;
import rife.models.Article;
import rife.models.Person;
import rife.validation.Validated;

public class HelloFormGeneration extends Site {
    public static class MyForm implements Element {
        public void process(Context c) {
            var t = c.template("HelloFormGeneration");
            c.generateEmptyForm(t, Article.class);
            t.setBlock("content", "form");
            c.print(t);
            c.pause();

            var article = c.parametersBean(Article.class);
            while (!((Validated)article).validate()) {
                c.generateForm(t, article);
                t.setBlock("content", "form");
                c.print(t);
                c.pause();

                article = c.parametersBean(Article.class);
            }

            t.setBean(article);
            t.setBlock("content", "display");
            c.print(t);
        }
    }
    Route form = getPost("/form", MyForm::new);

    public static void main(String[] args) {
        new Server().start(new HelloFormGeneration());
    }
}
