/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a SQL "DROP TABLE" query.
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
public class DropTable extends AbstractQuery implements Cloneable {
    private List<String> tables_ = null;

    public DropTable(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public List<String> getTables() {
        return tables_;
    }

    public void clear() {
        super.clear();

        tables_ = new ArrayList<String>();

        assert 0 == tables_.size();
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (0 == tables_.size()) {
                throw new TableNameRequiredException("DropTable");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".drop_table");

                if (1 == tables_.size()) {
                    template.setValue("EXPRESSION", tables_.get(0));
                } else {
                    if (template.hasValueId("TABLES")) {
                        template.setValue("TABLES", StringUtils.join(tables_, template.getBlock("SEPARATOR")));
                    }

                    var block = template.getBlock("TABLES");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("MULTIPLE TABLE DROP", datasource_.getAliasedDriver());
                    }

                    template.setValue("EXPRESSION", block);
                }

                sql_ = template.getBlock("QUERY");

                if (template.hasValueId("TABLES")) {
                    template.removeValue("TABLES");
                }
                template.removeValue("EXPRESSION");

                assert sql_ != null;
                assert sql_.length() > 0;
            }
        }

        return sql_;
    }

    public DropTable table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (0 == table.length()) throw new IllegalArgumentException("table can't be empty.");

        tables_.add(table);
        clearGenerated();

        return this;
    }

    public DropTable clone() {
        var new_instance = (DropTable) super.clone();
        if (new_instance != null) {
            if (tables_ != null) {
                new_instance.tables_ = new ArrayList<String>();
                new_instance.tables_.addAll(tables_);
            }
        }

        return new_instance;
    }
}
