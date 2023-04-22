/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.cmf.dam.ContentQueryManager;
import rife.cmf.elements.ServeContent;
import rife.database.Datasource;
import rife.engine.*;
import rife.models.*;

public class HelloContentManagement extends Site {
    Datasource datasource = new Datasource("org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    ContentQueryManager<NewsItem> manager = new ContentQueryManager<>(datasource, NewsItem.class);

    public static class AddNews implements Element {
        public void process(Context c) {
            var t = c.template("HelloContentManagement");
            c.generateEmptyForm(t, NewsItem.class);
            t.setBlock("content", "form");
            c.print(t);
            c.pause();

            var news = c.parametersBean(NewsItem.class);
            while (!news.validate()) {
                c.generateForm(t, news);
                t.setBlock("content", "form");
                c.print(t);
                c.pause();

                news = c.parametersBean(NewsItem.class);
            }

            var router = (HelloContentManagement) c.router();
            router.manager.save(news);

            t.setBean(news);
            t.setValue("imageSmall", router.manager.getContentForHtml(news, "imageSmall", c, router.serve));
            t.setValue("imageMedium", router.manager.getContentForHtml(news, "imageMedium", c, router.serve));

            t.setBlock("content", "display");
            t.appendBlock("content", "list");
            t.appendBlock("content", "add");
            c.print(t);
        }
    }

    Route serve = get("/serve", PathInfoHandling.CAPTURE, new ServeContent(datasource));
    Route install = get("/install", c -> {
        manager.getContentManager().install();
        manager.install();
        c.print("Installed");
    });
    Route remove = get("/remove", c -> {
        manager.remove();
        manager.getContentManager().remove();
        c.print("Removed");
    });
    Route list = getPost("/list", c -> {
        var t = c.template("HelloContentManagement");
        for (var news : manager.restore(manager.getRestoreQuery().limit(10))) {
            t.setBean(news);
            t.setValue("imageSmall", manager.getContentForHtml(news, "imageSmall", c, serve));

            t.appendBlock("content", "display");
            t.appendBlock("content", "spacer");
        }
        t.appendBlock("content", "add");
        c.print(t);
    });
    Route add = getPost("/add", AddNews::new);

    public void destroy() {
        datasource.close();
    }

    public static void main(String[] args) {
        new Server().start(new HelloContentManagement());
    }
}