/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.capabilities.Capabilities;
import rife.template.Template;

/**
 * An instance of <code>ReadQueryTemplate</code> will obtain a SQL from a
 * {@link Template} block. If the template is provided but no block name,
 * the entire content of the template will be used as the SQL query.
 *
 * <p>This allows you to write your custom SQL queries in dedicated templates,
 * to name them, and to use them together with the functionalities that are
 * provided by {@link rife.database.DbQueryManager}
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.6
 */
public class ReadQueryTemplate implements ReadQuery {
    private Template template_ = null;
    private String block_ = null;

    /**
     * Creates a new empty instance of <code>ReadQueryTemplate</code>.
     *
     * @since 1.6
     */
    public ReadQueryTemplate() {
    }

    /**
     * Creates a new instance of <code>ReadQueryTemplate</code> with the
     * template instance whose content provides the SQL query that will be
     * executed.
     *
     * @param template the template instance
     * @since 1.6
     */
    public ReadQueryTemplate(Template template) {
        setTemplate(template);
    }

    /**
     * Creates a new instance of <code>ReadQueryTemplate</code> with the
     * template instance and block name that provide the SQL that will
     * be executed.
     *
     * @param template the template instance
     * @param block    the name of the template block
     * @since 1.6
     */
    public ReadQueryTemplate(Template template, String block) {
        setTemplate(template);
        setBlock(block);
    }

    /**
     * Sets the template instance.
     *
     * @param template the template instance
     * @return this <code>ReadQueryTemplate</code> instance.
     * @see #setTemplate
     * @see #getTemplate
     * @since 1.6
     */
    public ReadQueryTemplate template(Template template) {
        setTemplate(template);
        return this;
    }

    /**
     * Sets the template instance.
     *
     * @param template the template instance
     * @see #template
     * @see #getTemplate
     * @since 1.6
     */
    public void setTemplate(Template template) {
        template_ = template;
    }

    /**
     * Retrieves the template instance.
     *
     * @return the template instance; or
     * <p><code>null</code> if no template instance was provided
     * @see #template
     * @see #setTemplate
     * @since 1.6
     */
    public Template getTemplate() {
        return template_;
    }

    /**
     * Sets the name of the template block.
     *
     * @param block the name of the template block
     * @return this <code>ReadQueryTemplate</code> instance.
     * @see #setBlock
     * @see #getBlock
     * @since 1.6
     */
    public ReadQueryTemplate block(String block) {
        setBlock(block);
        return this;
    }

    /**
     * Sets the name of the template block.
     *
     * @param block the name of the template block
     * @see #block
     * @see #getBlock
     * @since 1.6
     */
    public void setBlock(String block) {
        block_ = block;
    }

    /**
     * Retrieves the name of the template block.
     *
     * @return the name of the template block; or
     * <p><code>null</code> if no block name was provided
     * @see #block
     * @see #setBlock
     * @since 1.6
     */
    public String getBlock() {
        return block_;
    }

    public void clear() {
        template_ = null;
        block_ = null;
    }

    public String getSql() {
        if (null == template_) {
            return null;
        }

        if (null == block_) {
            return template_.getContent();
        }

        return template_.getBlock(block_);
    }

    public QueryParameters getParameters() {
        return null;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public void setExcludeUnsupportedCapabilities(boolean flag) {
    }
}
