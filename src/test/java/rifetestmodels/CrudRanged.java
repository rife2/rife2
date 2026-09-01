/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

/**
 * A bean whose identifier carries constraints of its own, which an addition
 * can't hold it to since it says that it isn't stored yet until the database
 * assigns it one.
 */
public class CrudRanged extends MetaData {
    private int id_ = -1;
    private String title_ = null;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false).subjectName("Number").rangeBegin(0).rangeEnd(99999));
        addConstraint(new ConstrainedProperty("title").notNull(true).maxLength(60).listed(true));
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
}
