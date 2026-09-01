/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.database.Datasource;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.Context;
import rife.engine.Site;
import rifetestmodels.CrudArticle;


/**
 * The same administration as {@link CrudAdminSite}, with every piece of
 * logic extracted into its own class instead of being written inline, which
 * is how an application keeps its site declaration readable once the rules
 * grow.
 */
public class CrudExtractedSite extends Site {
    private final Datasource datasource_;

    public CrudExtractedSite(Datasource datasource) {
        datasource_ = datasource;
    }

    public void setup() {
        group("/admin", new CrudAdmin(datasource_)
            .title("Extracted Administration")
            .entity(CrudArticle.class, new ArticleOptions(datasource_)));
    }

    /**
     * Configures how the articles are administered.
     */
    public static class ArticleOptions extends CrudEntityOptions<CrudArticle> {
        public ArticleOptions(Datasource datasource) {
            label("Article");
            slug("articles");
            pageSize(5);
            deletable(new Unpublished());
            action(new PublishAction(datasource));
        }
    }

    /**
     * Publishes an article that hasn't been published yet.
     */
    public static class PublishAction extends CrudAction<CrudArticle> {
        public PublishAction(Datasource datasource) {
            super("publish", "Publish", new PublishArticle(datasource));
            visibleWhen(new Unpublished());
        }
    }

    /**
     * Accepts the articles that haven't been published yet.
     */
    public static class Unpublished implements CrudCondition<CrudArticle> {
        public boolean accepts(CrudArticle article) {
            return !"published".equals(article.getStatus());
        }
    }

    /**
     * Publishes an article.
     */
    public static class PublishArticle implements CrudActionHandler<CrudArticle> {
        private final Datasource datasource_;

        public PublishArticle(Datasource datasource) {
            datasource_ = datasource;
        }

        public String perform(Context c, CrudArticle article) {
            article.setStatus("published");
            GenericQueryManagerFactory.instance(datasource_, CrudArticle.class).save(article);
            return "The article has been published.";
        }
    }
}
