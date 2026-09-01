/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.database.Datasource;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.Site;
import rifetestmodels.CrudArticle;
import rifetestmodels.CrudSubscriber;

/**
 * The site that the CRUD tests exercise, which registers two entities that
 * together cover the constraint vocabulary that the administration reads.
 */
public class CrudAdminSite extends Site {
    private final Datasource datasource_;
    public CrudAdmin admin;

    public CrudAdminSite(Datasource datasource) {
        datasource_ = datasource;
    }

    public void setup() {
        admin = new CrudAdmin(datasource_)
            .title("Test Administration")
            .environment("development")
            .link("Website", "/")
            .entity(CrudArticle.class, e -> e
                .label("Article")
                .pageSize(3)
                .deletable(article -> !"published".equals(article.getStatus()))
                .action("publish", "Publish", (c, article) -> {
                    article.setStatus("published");
                    GenericQueryManagerFactory.instance(datasource_, CrudArticle.class).save(article);
                    return "The article has been published.";
                }, a -> a.visibleWhen(article -> !"published".equals(article.getStatus())))
                .action("archive", "Archive", (c, article) -> {
                    article.setStatus("archived");
                    GenericQueryManagerFactory.instance(datasource_, CrudArticle.class).save(article);
                    return "The article has been archived.";
                }, a -> a.confirm()))
            .entity(CrudSubscriber.class, e -> e
                .label("Subscriber")
                .pageSize(3)
                .slug("subscribers"));

        group("/admin", admin);
    }

}
