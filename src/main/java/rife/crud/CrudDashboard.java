/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.engine.Context;

/**
 * Shows the entities that are registered with an administration.
 * <p>Each entity is shown with the number of instances that it currently
 * holds, so that the dashboard also tells you what's in the database.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class CrudDashboard extends CrudElement<Object> {
    CrudDashboard(CrudAdmin admin) {
        super(admin, null);
    }

    protected String pageKind() {
        return "dashboard";
    }

    protected void perform(Context c) {
        var t = page(c);

        for (var entity : admin_.getEntities()) {
            if (!isVisible(c, entity)) {
                continue;
            }
            t.setValueEncoded("crud:cardLabel", entity.getLabelPlural());
            t.setValueEncoded("crud:cardUrl", c.urlFor(admin_.routes(entity).browse));
            t.setValue("crud:cardCount", entity.createManager(admin_.datasource()).count());
            t.appendBlock("crud:cards", "crud:card");
        }

        t.setBlock(CONTENT_VALUE, "crud:dashboard");
        c.print(t);
    }
}
