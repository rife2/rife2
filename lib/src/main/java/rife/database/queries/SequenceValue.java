/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.SequenceOperationRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.datastructures.EnumClass;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

public class SequenceValue extends AbstractQuery implements Cloneable, ReadQuery {
    private String name_ = null;
    private Operation operation_ = null;

    public static final Operation NEXT = Operation.NEXT;
    public static final Operation CURRENT = Operation.CURRENT;

    public SequenceValue(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        name_ = null;
        operation_ = null;
    }

    public String getName() {
        return name_;
    }

    public Operation getOperation() {
        return operation_;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == name_) {
                throw new SequenceNameRequiredException("SequenceValue");
            } else if (null == operation_) {
                throw new SequenceOperationRequiredException("SequenceValue");
            } else {
                Template template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".sequence_value");

                if (template.hasValueId("NAME")) {
                    template.setValue("NAME", name_);
                }

                sql_ = template.getBlock("OPERATION_" + operation_);
                if (0 == sql_.length()) {
                    throw new UnsupportedSqlFeatureException("SEQUENCE VALUE " + operation_, datasource_.getAliasedDriver());
                }

                assert sql_ != null;
                assert sql_.length() > 0;
            }
        }

        return sql_;
    }

    public SequenceValue name(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        clearGenerated();
        name_ = name;

        return this;
    }

    public SequenceValue operation(Operation operation) {
        clearGenerated();
        operation_ = operation;

        return this;
    }

    public SequenceValue next() {
        return operation(NEXT);
    }

    public SequenceValue current() {
        return operation(CURRENT);
    }

    public SequenceValue clone() {
        return (SequenceValue) super.clone();
    }

    public static class Operation extends EnumClass<String> {
        public static final Operation NEXT = new Operation("NEXT");
        public static final Operation CURRENT = new Operation("CURRENT");

        Operation(String identifier) {
            super(identifier);
        }
    }
}
