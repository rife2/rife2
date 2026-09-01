/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CrudScheduled extends MetaData {
    private int id_ = -1;
    private String title_ = null;
    private Date publishAt_ = null;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        addConstraint(new ConstrainedProperty("title").notNull(true).maxLength(60).listed(true));
        addConstraint(new ConstrainedProperty("publishAt").format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm")));
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

    public void setPublishAt(Date publishAt) {
        publishAt_ = publishAt;
    }

    public Date getPublishAt() {
        return publishAt_;
    }
}
