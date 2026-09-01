/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class CrudArticle extends MetaData {
    private int id_ = -1;
    private String title_ = null;
    private String author_ = null;
    private String status_ = null;
    private String body_ = null;
    private boolean featured_ = false;
    private int ordinal_ = -1;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        addConstraint(new ConstrainedProperty("title").notNull(true).notEmpty(true).maxLength(60).listed(true).position(0));
        addConstraint(new ConstrainedProperty("author").maxLength(40).listed(true).position(1));
        addConstraint(new ConstrainedProperty("status").inList("draft", "published", "archived").defaultValue("draft").listed(true).position(2));
        addConstraint(new ConstrainedProperty("featured").listed(true).position(3));
        addConstraint(new ConstrainedProperty("body").maxLength(2000));
        // deliberately not marked as not editable, since being the ordinal
        // has to be what keeps it out of the forms
        addConstraint(new ConstrainedProperty("ordinal").ordinal(true));
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

    public void setAuthor(String author) {
        author_ = author;
    }

    public String getAuthor() {
        return author_;
    }

    public void setStatus(String status) {
        status_ = status;
    }

    public String getStatus() {
        return status_;
    }

    public void setBody(String body) {
        body_ = body;
    }

    public String getBody() {
        return body_;
    }

    public void setFeatured(boolean featured) {
        featured_ = featured;
    }

    public boolean isFeatured() {
        return featured_;
    }

    public void setOrdinal(int ordinal) {
        ordinal_ = ordinal;
    }

    public int getOrdinal() {
        return ordinal_;
    }
}
