/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

/**
 * Object representation of a SQL "DELETE" query.
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
public class Delete extends AbstractWhereQuery<Delete> implements Cloneable {
    private String hint_ = null;
    private String from_ = null;

    public Delete(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public String getHint() {
        return hint_;
    }

    public void clear() {
        super.clear();

        hint_ = null;
        from_ = null;
    }

    public String getFrom() {
        return from_;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getSql() {
        if (null == sql_) {
            if (null == from_) {
                throw new TableNameRequiredException("Delete");
            } else {
                Template template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".delete");

                if (hint_ != null) {
                    if (!template.hasValueId("HINT")) {
                        throw new UnsupportedSqlFeatureException("HINT", datasource_.getAliasedDriver());
                    }
                    template.setValue("EXPRESSION", hint_);
                    template.setBlock("HINT", "HINT");
                }

                template.setValue("TABLE", from_);

                if (where_ != null &&
                    where_.length() > 0) {
                    template.setValue("CONDITION", where_);
                    template.setValue("WHERE", template.getBlock("WHERE"));
                }

                sql_ = template.getBlock("QUERY");

                assert sql_ != null;
                assert sql_.length() > 0;
            }
        }

        return sql_;
    }

    public Delete hint(String hint) {
        clearGenerated();
        hint_ = hint;

        return this;
    }

    public Delete from(String from) {
        if (null == from) throw new IllegalArgumentException("from can't be null.");
        if (0 == from.length()) throw new IllegalArgumentException("from can't be empty.");

        clearGenerated();
        from_ = from;

        return this;
    }

    public Delete clone() {
        return super.clone();
    }
}
