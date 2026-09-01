/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class CrudModerated extends MetaData {
    private int id_ = -1;
    private String title_ = null;
    private String moderation_ = null;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        addConstraint(new ConstrainedProperty("title").notNull(true).maxLength(60).listed(true));
        // stored and shown, but changed through moderation rather than
        // through the edit form
        addConstraint(new ConstrainedProperty("moderation").editable(false).listed(true));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setTitle(String title) {
        title_ = title;
    }

    public String getTitle() {
        return title_;
    }

    public void setModeration(String moderation) {
        moderation_ = moderation;
    }

    public String getModeration() {
        return moderation_;
    }
}
