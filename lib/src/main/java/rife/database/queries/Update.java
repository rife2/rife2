/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.FieldsRequiredException;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.database.types.SqlNull;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;
import rife.validation.Constrained;
import rife.validation.ConstrainedUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Object representation of a SQL "UPDATE" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * {@link rife.database.DbQueryManager#executeUpdate(Query)
 * DbQueryManager.executeUpdate()}.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @since 1.0
 */
public class Update extends AbstractWhereQuery<Update> implements Cloneable {
    private String hint_ = null;
    private String table_ = null;
    private Map<String, Object> fields_ = null;

    public Update(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        hint_ = null;
        fields_ = new LinkedHashMap<String, Object>();
        table_ = null;

        assert 0 == fields_.size();
    }

    public String getHint() {
        return hint_;
    }

    public String getTable() {
        return table_;
    }

    public Map<String, Object> getFields() {
        return fields_;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getSql() {
        if (null == sql_) {
            if (null == table_) {
                throw new TableNameRequiredException("Update");
            } else if (0 == fields_.size()) {
                throw new FieldsRequiredException("Update");
            } else {
                Template template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".update");

                if (hint_ != null) {
                    if (!template.hasValueId("HINT")) {
                        throw new UnsupportedSqlFeatureException("HINT", datasource_.getAliasedDriver());
                    }
                    template.setValue("EXPRESSION", hint_);
                    template.setBlock("HINT", "HINT");
                }

                template.setValue("TABLE", table_);

                if (fields_.size() > 0) {
                    ArrayList<String> set_list = new ArrayList<String>();

                    for (String field : fields_.keySet()) {
                        template.setValue("NAME", field);
                        template.setValue("V", fields_.get(field).toString());
                        set_list.add(template.getBlock("SET"));
                    }
                    template.setValue("SET", StringUtils.join(set_list, template.getBlock("SEPARATOR")));
                }

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

    public Update hint(String hint) {
        clearGenerated();
        hint_ = hint;

        return this;
    }

    public Update table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (0 == table.length()) throw new IllegalArgumentException("table can't be empty.");

        clearGenerated();
        table_ = table;

        return this;
    }

    public Update fieldSubselect(Select query) {
        _fieldSubselect(query);

        return this;
    }

    protected Update _field(String field, Object value) {
        assert field != null;
        assert field.length() > 0;

        clearGenerated();
        if (null == value) {
            fields_.put(field, SqlNull.NULL);
        } else {
            fields_.put(field, value);
        }

        return this;
    }

    public Update fieldParameter(String field) {
        return fieldParameter(field, field);
    }

    public Update fieldParameter(String field, String alias) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");

        clearGenerated();

        addFieldParameter(alias);

        return _field(field, "?");
    }

    public Update field(String field, boolean value) {
        return field(field, Boolean.valueOf(value));
    }

    public Update field(String field, Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        StringBuilder buffer = new StringBuilder();

        buffer.append("(");
        buffer.append(query.toString());
        buffer.append(")");

        fieldCustom(field, buffer.toString());

        _fieldSubselect(query);

        return this;
    }

    public Update fieldCustom(String field, String expression) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");

        if (null == expression) {
            return _field(field, null);
        } else {
            return _field(field, expression);
        }
    }

    public Update field(String field, Object value) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");

        if (null == value) {
            return _field(field, null);
        } else {
            return _field(field, datasource_.getSqlConversion().getSqlValue(value));
        }
    }

    public Update fields(Object[] keyValues) {
        if (null == keyValues) throw new IllegalArgumentException("keyValues can't be null.");
        if (0 == keyValues.length) throw new IllegalArgumentException("keyValues can't be empty.");

        for (int i = 0; i < keyValues.length; i += 2) {
            if (null != keyValues[i]) {
                field(keyValues[i].toString(), keyValues[i + 1]);
            }
        }

        return this;
    }

    public Update fields(Object bean)
    throws DbQueryException {
        return fieldsFiltered(bean, null, null);
    }

    public Update fieldsIncluded(Object bean, String[] includedFields)
    throws DbQueryException {
        return fieldsFiltered(bean, includedFields, null);
    }

    public Update fieldsExcluded(Object bean, String[] excludedFields)
    throws DbQueryException {
        return fieldsFiltered(bean, null, excludedFields);
    }

    public Update fieldsFiltered(Object bean, String[] includedFields, String[] excludedFields)
    throws DbQueryException {
        if (null == bean) throw new IllegalArgumentException("bean can't be null.");

        Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        Map<String, String> property_values = QueryHelper.getBeanPropertyValues(bean, includedFields, excludedFields, getDatasource());

        for (String property_name : property_values.keySet()) {
            if (!ConstrainedUtils.saveConstrainedProperty(constrained, property_name, null)) {
                continue;
            }

            _field(property_name, property_values.get(property_name));
        }

        return this;
    }

    public Update fieldsParameters(Class beanClass)
    throws DbQueryException {
        return fieldsParametersExcluded(beanClass, null);
    }

    public Update fieldsParametersExcluded(Class beanClass, String[] excludedFields)
    throws DbQueryException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        clearGenerated();

        Constrained constrained = ConstrainedUtils.getConstrainedInstance(beanClass);
        Set<String> property_names = QueryHelper.getBeanPropertyNames(beanClass, excludedFields);

        for (String property_name : property_names) {
            if (!ConstrainedUtils.saveConstrainedProperty(constrained, property_name, null)) {
                continue;
            }

            addFieldParameter(property_name);
            _field(property_name, "?");
        }

        return this;
    }

    public Update clone() {
        Update new_instance = super.clone();
        if (new_instance != null) {
            if (fields_ != null) {
                new_instance.fields_ = new LinkedHashMap<String, Object>();
                new_instance.fields_.putAll(fields_);
            }
        }

        return new_instance;
    }
}
