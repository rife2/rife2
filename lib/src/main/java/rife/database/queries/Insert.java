/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Object representation of a SQL "INSERT" query.
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
public class Insert extends AbstractParametrizedQuery implements Cloneable {
    private String hint_ = null;
    private String into_ = null;
    private Map<String, List<Object>> fields_ = null;

    public Insert(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        hint_ = null;
        into_ = null;
        fields_ = new LinkedHashMap<String, List<Object>>();

        assert 0 == fields_.size();
    }

    public String getHint() {
        return hint_;
    }

    public String getInto() {
        return into_;
    }

    public Map<String, List<Object>> getFields() {
        return fields_;
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == into_) {
                throw new TableNameRequiredException("Insert");
            } else if (0 == fields_.size()) {
                throw new FieldsRequiredException("Insert");
            } else {
                var template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".insert");

                if (hint_ != null) {
                    if (!template.hasValueId("HINT")) {
                        throw new UnsupportedSqlFeatureException("HINT", datasource_.getAliasedDriver());
                    }
                    template.setValue("EXPRESSION", hint_);
                    template.setBlock("HINT", "HINT");
                }

                template.setValue("INTO", into_);

                // obtain the maximum number of values that are present by counting those of each field
                var maximum_number_of_value_rows = 0;
                for (var values : fields_.values()) {
                    if (values.size() > maximum_number_of_value_rows) {
                        maximum_number_of_value_rows = values.size();
                    }
                }

                // create the different rows that will be inserted into the database
                var value_rows = new ArrayList<String>();
                ArrayList<String> value_row = null;
                var column_names = fields_.keySet().toArray();
                String column_name = null;
                for (var current_value_row = 0; current_value_row < maximum_number_of_value_rows; current_value_row++) {
                    value_row = new ArrayList<String>();
                    for (Object columnName : column_names) {
                        column_name = (String) columnName;
                        if (current_value_row <= fields_.get(column_name).size() - 1) {
                            value_row.add(fields_.get(column_name).get(current_value_row).toString());
                        } else {
                            value_row.add("NULL");
                        }
                    }
                    template.setValue("VALUES", StringUtils.join(value_row, template.getBlock("SEPARATOR")));
                    value_rows.add(template.getBlock("VALUE_ROW"));
                }

                // create the strings of the columns that values will be inserted into and which values they are
                template.setValue("COLUMNS", StringUtils.join(column_names, template.getBlock("SEPARATOR")));
                if (1 == value_rows.size()) {
                    template.setValue("DATA", value_rows.get(0));
                } else {
                    if (template.hasValueId("VALUE_ROWS")) {
                        template.setValue("VALUE_ROWS", StringUtils.join(value_rows, template.getBlock("SEPARATOR")));
                    }

                    var block = template.getBlock("VALUE_ROWS");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("MULTIPLE INSERT ROWS", datasource_.getAliasedDriver());
                    }
                    template.setValue("DATA", block);
                }

                sql_ = template.getBlock("QUERY");

                assert sql_ != null;
                assert sql_.length() > 0;
            }
        }

        return sql_;
    }

    public Insert hint(String hint) {
        clearGenerated();
        hint_ = hint;

        return this;
    }

    public Insert into(String into) {
        if (null == into) throw new IllegalArgumentException("into can't be null.");
        if (0 == into.length()) throw new IllegalArgumentException("into can't be empty.");

        clearGenerated();
        into_ = into;

        return this;
    }

    public Insert fieldSubselect(Select query) {
        _fieldSubselect(query);

        return this;
    }

    protected Insert _field(String field, Object value) {
        assert field != null;
        assert field.length() > 0;

        clearGenerated();
        if (!fields_.containsKey(field)) {
            fields_.put(field, new ArrayList<Object>());
        }
        if (null == value) {
            fields_.get(field).add(SqlNull.NULL);
        } else {
            fields_.get(field).add(value);
        }

        return this;
    }

    public Insert fieldParameter(String field) {
        return fieldParameter(field, field);
    }

    public Insert fieldParameter(String field, String alias) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");

        clearGenerated();

        addFieldParameter(alias);

        return _field(field, "?");
    }

    public Insert fieldParameterCustom(String field, String expression) {
        return fieldParameterCustom(field, field, expression);
    }

    public Insert fieldParameterCustom(String field, String alias, String expression) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");

        clearGenerated();

        addFieldParameter(alias);

        if (null == expression) {
            return _field(field, null);
        } else {
            return _field(field, expression);
        }
    }

    public Insert field(String field, boolean value) {
        return field(field, Boolean.valueOf(value));
    }

    public Insert field(String field, Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        var buffer = new StringBuilder();
        buffer.append("(");
        buffer.append(query.toString());
        buffer.append(")");

        fieldCustom(field, buffer.toString());
        _fieldSubselect(query);

        return this;
    }

    public Insert field(String field, Object value) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");

        if (null == value) {
            return _field(field, null);
        } else {
            return _field(field, datasource_.getSqlConversion().getSqlValue(value));
        }
    }

    public Insert fieldCustom(String field, String expression) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");

        if (null == expression) {
            return _field(field, null);
        } else {
            return _field(field, expression);
        }
    }

    public Insert fields(Object[] keyValues) {
        if (null == keyValues) throw new IllegalArgumentException("keyValues can't be null.");
        if (0 == keyValues.length) throw new IllegalArgumentException("keyValues can't be empty.");

        for (var i = 0; i < keyValues.length; i += 2) {
            if (null != keyValues[i]) {
                field(keyValues[i].toString(), keyValues[i + 1]);
            }
        }

        return this;
    }

    public Insert fields(Object bean)
    throws DbQueryException {
        return fieldsFiltered(bean, null, null);
    }

    public Insert fieldsIncluded(Object bean, String[] includedFields)
    throws DbQueryException {
        return fieldsFiltered(bean, includedFields, null);
    }

    public Insert fieldsExcluded(Object bean, String[] excludedFields)
    throws DbQueryException {
        return fieldsFiltered(bean, null, excludedFields);
    }

    public Insert fieldsFiltered(Object bean, String[] includedFields, String[] excludedFields)
    throws DbQueryException {
        if (null == bean) throw new IllegalArgumentException("bean can't be null.");

        var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        var property_values = QueryHelper.getBeanPropertyValues(bean, includedFields, excludedFields, getDatasource());
        for (var property_name : property_values.keySet()) {
            if (!ConstrainedUtils.saveConstrainedProperty(constrained, property_name, null)) {
                continue;
            }

            _field(property_name, property_values.get(property_name));
        }

        return this;
    }

    public Insert fieldsParameters(Class beanClass)
    throws DbQueryException {
        return fieldsParametersExcluded(beanClass, null);
    }

    public Insert fieldsParametersExcluded(Class beanClass, String[] excludedFields)
    throws DbQueryException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        clearGenerated();

        var constrained = ConstrainedUtils.getConstrainedInstance(beanClass);
        var property_names = QueryHelper.getBeanPropertyNames(beanClass, excludedFields);
        for (var property_name : property_names) {
            if (!ConstrainedUtils.saveConstrainedProperty(constrained, property_name, null)) {
                continue;
            }

            addFieldParameter(property_name);
            _field(property_name, "?");
        }

        return this;
    }

    public Insert clone() {
        var new_instance = (Insert) super.clone();
        if (new_instance != null) {
            if (fields_ != null) {
                new_instance.fields_ = new LinkedHashMap<>();

                List<Object> values = null;

                for (var field : fields_.keySet()) {
                    values = fields_.get(field);
                    if (values != null) {
                        values = new ArrayList<Object>(values);
                    }
                    new_instance.fields_.put(field, values);
                }
            }
        }

        return new_instance;
    }
}

