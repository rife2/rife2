/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */

package rife.cmf.dam.contentmanagers;

import rife.cmf.ContentInfo;
import rife.validation.ConstrainedProperty;

/**
 * This class adds additional properties to the <code>ContentInfo</code> class
 * to be able to store the data in a database.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class DatabaseContentInfo extends ContentInfo {
    private int contentId_ = -1;

    /**
     * Instantiates a new <code>DatabaseContentInfo</code> instance.
     */
    public DatabaseContentInfo() {
    }

    public void activateValidation() {
        super.activateValidation();

        addConstraint(new ConstrainedProperty("contentId")
            .notNull(true)
            .rangeBegin(0)
            .identifier(true));
    }

    /**
     * Sets the ID of the stored <code>Content</code> instance.
     * <p>This ID will not be used to refer to the Content instance from
     * outside the backend. The path and the version should be used for this
     * instead.
     *
     * @param contentId the ID of the <code>Content</code> instance
     * @see #getContentId()
     */
    public void setContentId(int contentId) {
        contentId_ = contentId;
    }

    /**
     * Retrieves the ID of the stored <code>Content</code> instance.
     *
     * @return the <code>Content</code>'s ID
     * @see #setContentId(int)
     */
    public int getContentId() {
        return contentId_;
    }
}
