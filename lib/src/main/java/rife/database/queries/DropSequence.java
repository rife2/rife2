/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

/**
 * Object representation of a SQL "DROP SEQUENCE" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @since 1.0
 */
public class DropSequence extends AbstractQuery implements Cloneable {
    private String name_ = null;

    public DropSequence(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        name_ = null;
    }

    public String getName() {
        return name_;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == name_) {
                throw new SequenceNameRequiredException("DropSequence");
            } else {
                Template template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".drop_sequence");

                if (template.hasValueId("NAME")) {
                    template.setValue("NAME", name_);
                }

                sql_ = template.getBlock("QUERY");
                if (0 == sql_.length()) {
                    throw new UnsupportedSqlFeatureException("DROP SEQUENCE", datasource_.getAliasedDriver());
                }

                assert sql_ != null;
                assert sql_.length() > 0;
            }
        }

        return sql_;
    }

    public DropSequence name(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        clearGenerated();
        name_ = name;

        return this;
    }

    public DropSequence clone() {
        return (DropSequence) super.clone();
    }
}
