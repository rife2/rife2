/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.exceptions.*;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.datastructures.EnumClass;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.ClassUtils;
import rife.tools.StringUtils;
import rife.validation.Constrained;
import rife.validation.ConstrainedBean;
import rife.validation.ConstrainedProperty;
import rife.validation.ConstrainedUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Object representation of a SQL "CREATE TABLE" query.
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
public class CreateTable extends AbstractQuery implements Cloneable {
    private String table_ = null;
    private boolean temporary_ = false;
    private Map<String, Column> columnMapping_ = null;
    private List<PrimaryKey> primaryKeys_ = null;
    private List<ForeignKey> foreignKeys_ = null;
    private List<UniqueConstraint> uniqueConstraints_ = null;
    private List<CheckConstraint> checkConstraints_ = null;

    public static final Nullable NULL = new Nullable("NULL");
    public static final Nullable NOTNULL = new Nullable("NOTNULL");

    public static final ViolationAction NOACTION = new ViolationAction("NOACTION");
    public static final ViolationAction RESTRICT = new ViolationAction("RESTRICT");
    public static final ViolationAction CASCADE = new ViolationAction("CASCADE");
    public static final ViolationAction SETNULL = new ViolationAction("SETNULL");
    public static final ViolationAction SETDEFAULT = new ViolationAction("SETDEFAULT");

    public CreateTable(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        table_ = null;
        temporary_ = false;
        columnMapping_ = new LinkedHashMap<>();
        primaryKeys_ = new ArrayList<>();
        foreignKeys_ = new ArrayList<>();
        uniqueConstraints_ = new ArrayList<>();
        checkConstraints_ = new ArrayList<>();
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public String getTable() {
        return table_;
    }

    public boolean isTemporary() {
        return temporary_;
    }

    public Map<String, Column> getColumnMapping() {
        return columnMapping_;
    }

    public List<PrimaryKey> getPrimaryKeys() {
        return primaryKeys_;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys_;
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints_;
    }

    public List<CheckConstraint> getCheckConstraints() {
        return checkConstraints_;
    }

    public String getSql()
    throws DbQueryException {
        if (null == sql_) {
            if (null == getTable()) {
                throw new TableNameRequiredException("CreateTable");
            } else if (0 == columnMapping_.size()) {
                throw new ColumnsRequiredException("CreateTable");
            } else {
                Template template = TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".create_table");
                String block = null;
                String sql = null;

                String columns = null;
                ArrayList<String> column_list = new ArrayList<String>();
                for (Column column : columnMapping_.values()) {
                    column_list.add(column.getSql(template));
                }
                columns = StringUtils.join(column_list, template.getBlock("SEPERATOR"));

                if (temporary_) {
                    block = template.getBlock("TEMPORARY");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("TEMPORARY", datasource_.getAliasedDriver());
                    }
                    template.setValue("TEMPORARY", block);
                }

                String primary = "";
                if (primaryKeys_.size() > 0) {
                    ArrayList<String> constraints = new ArrayList<String>();
                    for (PrimaryKey primary_key : primaryKeys_) {
                        sql = primary_key.getSql(template);
                        if (sql.length() > 0) {
                            constraints.add(sql);
                        }
                    }
                    if (constraints.size() > 0) {
                        primary = template.getBlock("SEPERATOR") + StringUtils.join(constraints, template.getBlock("SEPERATOR"));
                    }
                }

                String foreign = "";
                if (foreignKeys_.size() > 0) {
                    ArrayList<String> constraints = new ArrayList<String>();
                    for (ForeignKey foreign_key : foreignKeys_) {
                        sql = foreign_key.getSql(template);
                        if (sql.length() > 0) {
                            constraints.add(sql);
                        }
                    }
                    if (constraints.size() > 0) {
                        foreign = template.getBlock("SEPERATOR") + StringUtils.join(constraints, template.getBlock("SEPERATOR"));
                    }
                }

                String unique = "";
                if (uniqueConstraints_.size() > 0) {
                    ArrayList<String> constraints = new ArrayList<String>();
                    for (UniqueConstraint unique_constraint : uniqueConstraints_) {
                        sql = unique_constraint.getSql(template);
                        if (sql.length() > 0) {
                            constraints.add(sql);
                        }
                    }
                    if (constraints.size() > 0) {
                        unique = template.getBlock("SEPERATOR") + StringUtils.join(constraints, template.getBlock("SEPERATOR"));
                    }
                }

                String check = "";
                if (checkConstraints_.size() > 0) {
                    ArrayList<String> constraints = new ArrayList<String>();
                    for (CheckConstraint check_constraint : checkConstraints_) {
                        sql = check_constraint.getSql(template);
                        if (sql.length() > 0) {
                            constraints.add(sql);
                        }
                    }
                    if (constraints.size() > 0) {
                        check = template.getBlock("SEPERATOR") + StringUtils.join(constraints, template.getBlock("SEPERATOR"));
                    }
                }

                template.setValue("TABLE", table_);
                template.setValue("COLUMNS", columns);
                template.setValue("PRIMARY_KEYS", primary);
                template.setValue("FOREIGN_KEYS", foreign);
                template.setValue("UNIQUE_CONSTRAINTS", unique);
                template.setValue("CHECKS", check);

                sql_ = template.getBlock("QUERY");

                assert sql_ != null;
                assert sql_.length() > 0;
            }
        }

        return sql_;
    }

    public CreateTable table(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (0 == table.length()) throw new IllegalArgumentException("table can't be empty.");

        table_ = table;
        clearGenerated();

        return this;
    }

    public CreateTable temporary(boolean temporary) {
        temporary_ = temporary;
        clearGenerated();

        return this;
    }

    public CreateTable column(String name, Class type) {
        return column(name, type, -1, -1, null, null);
    }

    public CreateTable column(String name, Class type, String typeAttribute) {
        return column(name, type, -1, -1, typeAttribute, null);
    }

    public CreateTable column(String name, Class type, int precision) {
        return column(name, type, precision, -1, null, null);
    }

    public CreateTable column(String name, Class type, int precision, String typeAttribute) {
        return column(name, type, precision, -1, typeAttribute, null);
    }

    public CreateTable column(String name, Class type, int precision, int scale) {
        return column(name, type, precision, scale, null, null);
    }

    public CreateTable column(String name, Class type, int precision, int scale, String typeAttribute) {
        return column(name, type, precision, scale, typeAttribute, null);
    }

    public CreateTable column(String name, Class type, Nullable nullable) {
        return column(name, type, -1, -1, null, nullable);
    }

    public CreateTable column(String name, Class type, String typeAttribute, Nullable nullable) {
        return column(name, type, -1, -1, typeAttribute, nullable);
    }

    public CreateTable column(String name, Class type, int precision, Nullable nullable) {
        return column(name, type, precision, -1, null, nullable);
    }

    public CreateTable column(String name, Class type, int precision, String typeAttribute, Nullable nullable) {
        return column(name, type, precision, -1, typeAttribute, nullable);
    }

    public CreateTable column(String name, Class type, int precision, int scale, Nullable nullable) {
        return column(name, type, precision, scale, null, nullable);
    }

    public CreateTable column(String name, Class type, int precision, int scale, String typeAttribute, Nullable nullable) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == type) throw new IllegalArgumentException("type can't be null.");

        columnMapping_.put(name, new Column(name, type, precision, scale, typeAttribute, nullable));
        clearGenerated();
        return this;
    }

    public CreateTable column(String name, String customType) {
        return column(name, customType, null);
    }

    public CreateTable column(String name, String customType, Nullable nullable) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == customType) throw new IllegalArgumentException("customType can't be null.");

        columnMapping_.put(name, new Column(name, customType, nullable));
        clearGenerated();
        return this;
    }

    public CreateTable columns(Object[] keyValues) {
        if (null == keyValues) throw new IllegalArgumentException("keyValues can't be null.");

        for (int i = 0; i < keyValues.length; i += 2) {
            if (null != keyValues[i]) {
                column(keyValues[i].toString(), (Class) keyValues[i + 1]);
            }
        }

        return this;
    }

    public CreateTable precision(String name, int precision) {
        return precision(name, precision, -1);
    }

    public CreateTable precision(String name, int precision, int scale) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (!columnMapping_.containsKey(name))
            throw new IllegalArgumentException("the '" + name + "' column hasn't been defined.");

        Column column = columnMapping_.get(name);
        column.setPrecision(precision);
        column.setScale(scale);

        return this;
    }

    public CreateTable nullable(String name, Nullable nullable) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (!columnMapping_.containsKey(name))
            throw new IllegalArgumentException("the '" + name + "' column hasn't been defined.");

        Column column = columnMapping_.get(name);
        column.setNullable(nullable);

        return this;
    }

    public CreateTable defaultValue(String name, boolean value) {
        return defaultValue(name, Boolean.valueOf(value));
    }

    public CreateTable defaultValue(String name, Object value) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (!columnMapping_.containsKey(name))
            throw new IllegalArgumentException("the '" + name + "' column hasn't been defined.");

        Column column = columnMapping_.get(name);
        column.setDefault(datasource_.getSqlConversion().getSqlValue(value));

        return this;
    }

    public CreateTable defaultFunction(String name, String defaultFunction) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (!columnMapping_.containsKey(name))
            throw new IllegalArgumentException("the '" + name + "' column hasn't been defined.");
        if (null == defaultFunction) throw new IllegalArgumentException("defaultFunction can't be null.");
        if (0 == defaultFunction.length()) throw new IllegalArgumentException("defaultFunction can't be empty.");

        Column column = columnMapping_.get(name);
        column.setDefault(defaultFunction);

        return this;
    }

    public CreateTable customAttribute(String name, String attribute) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (!columnMapping_.containsKey(name))
            throw new IllegalArgumentException("the '" + name + "' column hasn't been defined.");

        Column column = columnMapping_.get(name);
        column.addCustomAttribute(attribute);

        return this;
    }

    public CreateTable columns(Class beanClass)
    throws DbQueryException {
        return columnsFiltered(beanClass, null, null);
    }

    public CreateTable columnsIncluded(Class beanClass, String[] includedFields)
    throws DbQueryException {
        return columnsFiltered(beanClass, includedFields, null);
    }

    public CreateTable columnsExcluded(Class beanClass, String[] excludedFields)
    throws DbQueryException {
        return columnsFiltered(beanClass, null, excludedFields);
    }

    public CreateTable columnsFiltered(Class beanClass, String[] includedFields, String[] excludedFields)
    throws DbQueryException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        Constrained constrained = ConstrainedUtils.getConstrainedInstance(beanClass);

        // handle constrained bean
        if (constrained != null) {
            ConstrainedBean constrained_bean = constrained.getConstrainedBean();
            if (constrained_bean != null) {
                // handle multi-column uniques
                if (constrained_bean.hasUniques()) {
                    for (String[] o : (List<String[]>) constrained_bean.getUniques()) {
                        unique(o);
                    }
                }
            }
        }

        // handle properties
        ConstrainedProperty constrained_property = null;
        Map<String, Class> column_types = QueryHelper.getBeanPropertyTypes(beanClass, includedFields, excludedFields);
        Class column_type = null;
        Column column = null;
        for (String column_name : column_types.keySet()) {
            if (!ConstrainedUtils.persistConstrainedProperty(constrained, column_name, null)) {
                continue;
            }

            column_type = column_types.get(column_name);
            column = new Column(column_name, column_type);
            columnMapping_.put(column_name, column);

            String[] in_list_values = null;

            in_list_values = ClassUtils.getEnumClassValues(column_type);

            if (constrained != null) {
                constrained_property = constrained.getConstrainedProperty(column_name);
                if (constrained_property != null) {
                    if (constrained_property.isNotNull()) {
                        nullable(column_name, NOTNULL);
                    }

                    if (constrained_property.isIdentifier()) {
                        primaryKey(column_name);
                    }

                    if (constrained_property.isUnique()) {
                        unique(column_name);
                    }

                    if (constrained_property.isNotEmpty()) {
                        if (ClassUtils.isNumeric(column_type)) {
                            check(column_name + " != 0");
                        } else if (ClassUtils.isText(column_type)) {
                            check(column_name + " != ''");
                        }
                    }

                    if (constrained_property.isNotEqual()) {
                        if (ClassUtils.isNumeric(column_type)) {
                            check(column_name + " != " + constrained_property.getNotEqual());
                        } else if (ClassUtils.isText(column_type)) {
                            check(column_name + " != '" + StringUtils.encodeSql(constrained_property.getNotEqual().toString()) + "'");
                        }
                    }

                    if (constrained_property.hasPrecision()) {
                        if (constrained_property.hasScale()) {
                            precision(column_name, constrained_property.getPrecision(), constrained_property.getScale());
                        } else {
                            precision(column_name, constrained_property.getPrecision());
                        }
                    }

                    if (constrained_property.isInList()) {
                        in_list_values = constrained_property.getInList().clone();
                    }

                    if (constrained_property.hasDefaultValue()) {
                        defaultValue(column_name, constrained_property.getDefaultValue());
                    }

                    if (constrained_property.hasManyToOne() &&
                        ClassUtils.isBasic(column_type)) {
                        ConstrainedProperty.ManyToOne many_to_one = constrained_property.getManyToOne();

                        if (null == many_to_one.getDerivedTable()) {
                            throw new MissingManyToOneTableException(beanClass, constrained_property.getPropertyName());
                        }

                        if (null == many_to_one.getColumn()) {
                            throw new MissingManyToOneColumnException(beanClass, constrained_property.getPropertyName());
                        }

                        foreignKey(many_to_one.getDerivedTable(), constrained_property.getPropertyName(), many_to_one.getColumn(), many_to_one.getOnUpdate(), many_to_one.getOnDelete());
                    }
                }
            }

            // handle in list constraints
            if (in_list_values != null) {
                for (int i = 0; i < in_list_values.length; i++) {
                    in_list_values[i] = StringUtils.encodeSql(in_list_values[i]);
                }

                StringBuilder check_constraint = new StringBuilder();
                String seperator = "";
                if (ClassUtils.isText(column_type) || column_type.isEnum()) {
                    seperator = "'";
                }
                check_constraint.append(column_name);
                check_constraint.append(" IS NULL OR ");
                check_constraint.append(column_name);
                check_constraint.append(" IN (");
                check_constraint.append(seperator);
                check_constraint.append(StringUtils.join(in_list_values, seperator + "," + seperator));
                check_constraint.append(seperator);
                check_constraint.append(")");
                check(check_constraint.toString());
            }
        }
        clearGenerated();

        return this;
    }

    public CreateTable primaryKey(String column) {
        return primaryKey(null, column);
    }

    public CreateTable primaryKey(String[] columns) {
        return primaryKey(null, columns);
    }

    public CreateTable primaryKey(String name, String column) {
        return primaryKey(name, new String[]{column});
    }

    public CreateTable primaryKey(String name, String[] columns) {
        if (name != null && 0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == columns) throw new IllegalArgumentException("columns array can't be null.");
        if (0 == columns.length) throw new IllegalArgumentException("columns array can't be empty.");

        for (String column : columns) {
            nullable(column, CreateTable.NOTNULL);
        }
        primaryKeys_.add(new PrimaryKey(name, columns));
        clearGenerated();

        return this;
    }

    public CreateTable foreignKey(String foreignTable, String localColumn, String foreignColumn) {
        return foreignKey(null, foreignTable, localColumn, foreignColumn, null, null);
    }

    public CreateTable foreignKey(String foreignTable, String localColumn, String foreignColumn, ViolationAction onUpdate, ViolationAction onDelete) {
        return foreignKey(null, foreignTable, new String[]{localColumn, foreignColumn}, onUpdate, onDelete);
    }

    public CreateTable foreignKey(String foreignTable, String[] columnsMapping) {
        return foreignKey(null, foreignTable, columnsMapping, null, null);
    }

    public CreateTable foreignKey(String foreignTable, String[] columnsMapping, ViolationAction onUpdate, ViolationAction onDelete) {
        return foreignKey(null, foreignTable, columnsMapping, onUpdate, onDelete);
    }

    public CreateTable foreignKey(String name, String foreignTable, String localColumn, String foreignColumn) {
        return foreignKey(name, foreignTable, localColumn, foreignColumn, null, null);
    }

    public CreateTable foreignKey(String name, String foreignTable, String localColumn, String foreignColumn, ViolationAction onUpdate, ViolationAction onDelete) {
        return foreignKey(name, foreignTable, new String[]{localColumn, foreignColumn}, onUpdate, onDelete);
    }

    public CreateTable foreignKey(String name, String foreignTable, String[] columnsMapping) {
        return foreignKey(name, foreignTable, columnsMapping, null, null);
    }

    public CreateTable foreignKey(String name, String foreignTable, String[] columnsMapping, ViolationAction onUpdate, ViolationAction onDelete) {
        if (name != null && 0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == foreignTable) throw new IllegalArgumentException("foreignTable can't be null.");
        if (0 == foreignTable.length()) throw new IllegalArgumentException("foreignTable can't be empty.");
        if (null == columnsMapping) throw new IllegalArgumentException("columnsMapping array can't be null.");
        if (0 == columnsMapping.length) throw new IllegalArgumentException("columnsMapping array can't be empty.");
        if (columnsMapping.length % 2 != 0)
            throw new IllegalArgumentException("columnsMapping array isn't valid, each local column should be mapped to a foreign one.");

        foreignKeys_.add(new ForeignKey(name, foreignTable, columnsMapping, onUpdate, onDelete));

        clearGenerated();
        return this;
    }

    public CreateTable unique(String column) {
        return unique(null, column);
    }

    public CreateTable unique(String[] columns) {
        return unique(null, columns);
    }

    public CreateTable unique(String name, String column) {
        return unique(name, new String[]{column});
    }

    public CreateTable unique(String name, String[] columns) {
        if (name != null && 0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == columns) throw new IllegalArgumentException("columns array can't be null.");
        if (0 == columns.length) throw new IllegalArgumentException("columns array can't be empty.");

        uniqueConstraints_.add(new UniqueConstraint(name, columns));

        clearGenerated();
        return this;
    }

    public CreateTable check(String expression) {
        return check(null, expression);
    }

    public CreateTable check(String name, String expression) {
        if (name != null && 0 == name.length()) throw new IllegalArgumentException("name can't be empty.");
        if (null == expression) throw new IllegalArgumentException("expression can't be null.");
        if (0 == expression.length()) throw new IllegalArgumentException("expression can't be empty.");

        checkConstraints_.add(new CheckConstraint(name, expression));

        clearGenerated();
        return this;
    }

    public CreateTable clone() {
        CreateTable new_instance = (CreateTable) super.clone();
        if (new_instance != null) {
            if (columnMapping_ != null) {
                new_instance.columnMapping_ = new LinkedHashMap<String, Column>();

                Column column = null;
                for (String name : columnMapping_.keySet()) {
                    column = columnMapping_.get(name);
                    new_instance.columnMapping_.put(name, column.clone());
                }
            }

            if (primaryKeys_ != null) {
                new_instance.primaryKeys_ = new ArrayList<PrimaryKey>();

                for (PrimaryKey primary_key : primaryKeys_) {
                    new_instance.primaryKeys_.add(primary_key.clone());
                }
            }

            if (foreignKeys_ != null) {
                new_instance.foreignKeys_ = new ArrayList<ForeignKey>();

                for (ForeignKey foreign_key : foreignKeys_) {
                    new_instance.foreignKeys_.add(foreign_key.clone());
                }
            }

            if (uniqueConstraints_ != null) {
                new_instance.uniqueConstraints_ = new ArrayList<UniqueConstraint>();

                for (UniqueConstraint unique_constraint : uniqueConstraints_) {
                    new_instance.uniqueConstraints_.add(unique_constraint.clone());
                }
            }

            if (checkConstraints_ != null) {
                new_instance.checkConstraints_ = new ArrayList<CheckConstraint>();

                for (CheckConstraint check_constraint : checkConstraints_) {
                    new_instance.checkConstraints_.add(check_constraint.clone());
                }
            }
        }

        return new_instance;
    }

    public class PrimaryKey extends ColumnsConstraint implements Cloneable {
        PrimaryKey(String name, String[] columns) {
            super(name, columns);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String result = null;

            if (getName() != null &&
                template.hasValueId("PRIMARY_KEY_NAME")) {
                template.setValue("NAME", getName());
                template.setValue("PRIMARY_KEY_NAME", template.getBlock("PRIMARY_KEY_NAME"));
            }

            template.setValue("COLUMN_NAMES", StringUtils.join(getColumns(), template.getBlock("SEPERATOR")));

            result = template.getBlock("PRIMARY_KEY");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("PRIMARY KEY", datasource_.getAliasedDriver());
            }

            assert result != null;

            return result;
        }

        public PrimaryKey clone() {
            return (PrimaryKey) super.clone();
        }
    }

    public class ForeignKey extends ColumnsConstraint implements Cloneable {
        private String foreignTable_ = null;
        private ViolationAction onUpdate_ = null;
        private ViolationAction onDelete_ = null;

        ForeignKey(String name, String foreignTable, String[] columnsMapping, ViolationAction onUpdate, ViolationAction onDelete) {
            super(name, columnsMapping);
            setForeignTable(foreignTable);
            setOnUpdate(onUpdate);
            setOnDelete(onDelete);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String block = null;
            String result = null;

            if (getName() != null) {
                template.setValue("NAME", getName());
                template.setValue("FOREIGN_KEY_NAME", template.getBlock("FOREIGN_KEY_NAME"));
            }

            template.setValue("FOREIGN_TABLE", getForeignTable());

            String violations_actions = "";
            if (getOnUpdate() != null) {
                block = template.getBlock("ON_UPDATE_" + getOnUpdate().toString());
                if (0 == block.length()) {
                    throw new UnsupportedSqlFeatureException("ON UPDATE " + getOnUpdate().toString(), datasource_.getAliasedDriver());
                }
                template.setValue("ON_UPDATE_ACTION", block);
                block = template.getBlock("ON_UPDATE");
                if (0 == block.length()) {
                    throw new UnsupportedSqlFeatureException("ON UPDATE", datasource_.getAliasedDriver());
                }
                violations_actions += block;
            }

            if (getOnDelete() != null) {
                block = template.getBlock("ON_DELETE_" + getOnDelete().toString());
                if (0 == block.length()) {
                    throw new UnsupportedSqlFeatureException("ON DELETE " + getOnDelete().toString(), datasource_.getAliasedDriver());
                }
                template.setValue("ON_DELETE_ACTION", block);
                block = template.getBlock("ON_DELETE");
                if (0 == block.length()) {
                    throw new UnsupportedSqlFeatureException("ON DELETE", datasource_.getAliasedDriver());
                }
                violations_actions += block;
            }
            template.setValue("VIOLATION_ACTIONS", violations_actions);

            String[] local_columns = new String[getColumns().length / 2];
            String[] foreign_columns = new String[getColumns().length / 2];
            for (int i = 0; i < getColumns().length; i += 2) {
                local_columns[i / 2] = getColumns()[i];
                foreign_columns[i / 2] = getColumns()[i + 1];
            }
            template.setValue("LOCAL_COLUMN_NAMES", StringUtils.join(local_columns, template.getBlock("SEPERATOR")));
            template.setValue("FOREIGN_COLUMN_NAMES", StringUtils.join(foreign_columns, template.getBlock("SEPERATOR")));

            result = template.getBlock("FOREIGN_KEY");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("FOREIGN KEY", datasource_.getAliasedDriver());
            }

            assert result != null;

            return result;
        }

        public String getForeignTable() {
            return foreignTable_;
        }

        void setForeignTable(String foreignTable) {
            assert foreignTable != null;
            assert foreignTable.length() > 0;

            foreignTable_ = foreignTable;
        }

        public ViolationAction getOnUpdate() {
            return onUpdate_;
        }

        void setOnUpdate(ViolationAction onUpdate) {
            onUpdate_ = onUpdate;
        }

        public ViolationAction getOnDelete() {
            return onDelete_;
        }

        void setOnDelete(ViolationAction onDelete) {
            onDelete_ = onDelete;
        }

        public ForeignKey clone() {
            return (ForeignKey) super.clone();
        }
    }

    public class UniqueConstraint extends ColumnsConstraint implements Cloneable {
        UniqueConstraint(String name, String[] columns) {
            super(name, columns);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String result = null;

            if (getName() != null) {
                template.setValue("NAME", getName());
                template.setValue("UNIQUE_CONSTRAINT_NAME", template.getBlock("UNIQUE_CONSTRAINT_NAME"));
            }

            template.setValue("COLUMN_NAMES", StringUtils.join(getColumns(), template.getBlock("SEPERATOR")));

            result = template.getBlock("UNIQUE_CONSTRAINT");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("UNIQUE", datasource_.getAliasedDriver());
            }

            return result;
        }

        public UniqueConstraint clone() {
            return (UniqueConstraint) super.clone();
        }
    }

    public class CheckConstraint extends Constraint implements Cloneable {
        private String rxpression_ = null;

        CheckConstraint(String name, String expression) {
            super(name);
            setExpression(expression);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String result = null;

            if (getName() != null &&
                template.hasValueId("CHECK_NAME")) {
                template.setValue("NAME", getName());
                template.setValue("CHECK_NAME", template.getBlock("CHECK_NAME"));
            }

            if (template.hasValueId("EXPRESSION")) {
                template.setValue("EXPRESSION", getExpression());
            }

            result = template.getBlock("CHECK");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("CHECK", datasource_.getAliasedDriver());
            }

            return result;
        }

        public String getExpression() {
            return rxpression_;
        }

        void setExpression(String expression) {
            rxpression_ = expression;
        }

        public CheckConstraint clone() {
            return (CheckConstraint) super.clone();
        }
    }

    public abstract class ColumnsConstraint extends Constraint implements Cloneable {
        private String[] columns_ = null;

        ColumnsConstraint(String name, String[] columns) {
            super(name);

            setColumns(columns);
        }

        public String[] getColumns() {
            return columns_;
        }

        void setColumns(String[] columns) {
            assert columns != null;
            assert columns.length > 0;

            columns_ = columns;
        }

        public ColumnsConstraint clone() {
            return (ColumnsConstraint) super.clone();
        }
    }

    public abstract class Constraint implements Cloneable {
        private String name_ = null;

        Constraint(String name) {
            setName(name);
        }

        abstract String getSql(Template template)
        throws DbQueryException;

        public String getName() {
            return name_;
        }

        void setName(String name) {
            assert null == name || name.length() > 0;

            name_ = name;
        }

        public Constraint clone() {
            Constraint new_instance = null;
            try {
                new_instance = (Constraint) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public class Column implements Cloneable {
        private String name_ = null;
        private Class type_ = null;
        private int precision_ = -1;
        private int scale_ = -1;
        private String typeAttribute_ = null;
        private Nullable nullable_ = null;
        private String default_ = null;
        private String customType_ = null;
        private ArrayList<String> customAttributes_ = new ArrayList<String>();

        Column(String name, Class type) {
            setName(name);
            setType(type);
        }

        Column(String name, Class type, int precision, int scale, String typeAttribute, Nullable nullable) {
            setName(name);
            setType(type);
            setPrecision(precision);
            setScale(scale);
            setTypeAttribute(typeAttribute);
            setNullable(nullable);
        }

        Column(String name, String customType, Nullable nullable) {
            setName(name);
            setCustomType(customType);
            setNullable(nullable);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String block = null;
            String result = null;

            template.setValue("NAME", getName());
            String type = customType_;
            if (type == null) {
                type = datasource_.getSqlConversion().getSqlType(getType(), getPrecision(), getScale());
            }
            template.setValue("TYPE", type);
            if (typeAttribute_ != null) {
                template.appendValue("TYPE", " ");
                template.appendValue("TYPE", typeAttribute_);
            }

            if (getNullable() != null) {
                block = template.getBlock(getNullable().toString());
                if (0 == block.length()) {
                    throw new UnsupportedSqlFeatureException("NULLABLE " + getNullable().toString(), datasource_.getAliasedDriver());
                }
                template.setValue("NULLABLE", block);
            }
            if (getDefault() != null) {
                template.setValue("V", getDefault());
                block = template.getBlock("DEFAULT");
                if (0 == block.length()) {
                    throw new UnsupportedSqlFeatureException("DEFAULT", datasource_.getAliasedDriver());
                }
                template.setValue("DEFAULT", block);
                template.removeValue("V");
            }
            if (getCustomAttributes().size() > 0) {
                template.setValue("V", StringUtils.join(getCustomAttributes(), " "));
                block = template.getBlock("CUSTOM_ATTRIBUTES");
                if (0 == block.length()) {
                    throw new UnsupportedSqlFeatureException("CUSTOM_ATTRIBUTES", datasource_.getAliasedDriver());
                }
                template.setValue("CUSTOM_ATTRIBUTES", block);
                template.removeValue("V");
            }
            result = template.getBlock("COLUMN");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("COLUMN", datasource_.getAliasedDriver());
            }
            template.removeValue("NAME");
            template.removeValue("TYPE");
            template.removeValue("NULLABLE");
            template.removeValue("DEFAULT");
            template.removeValue("CUSTOM_ATTRIBUTES");

            assert result.length() > 0;

            return result;
        }

        public String getName() {
            return name_;
        }

        void setName(String name) {
            assert name != null;
            assert name.length() > 0;

            name_ = name;
        }

        public Class getType() {
            return type_;
        }

        void setType(Class type) {
            assert type != null;

            type_ = type;
        }

        public String getCustomType() {
            return customType_;
        }

        void setCustomType(String type) {
            assert type != null;

            customType_ = type;
        }

        public int getPrecision() {
            return precision_;
        }

        void setPrecision(int precision) {
            assert precision >= -1;

            precision_ = precision;
        }

        public int getScale() {
            return scale_;
        }

        void setScale(int scale) {
            assert scale >= -1;

            scale_ = scale;
        }

        public String getTypeAttribute() {
            return typeAttribute_;
        }

        void setTypeAttribute(String typeAttribute) {
            typeAttribute_ = typeAttribute;
        }

        public Nullable getNullable() {
            return nullable_;
        }

        void setNullable(Nullable nullable) {
            nullable_ = nullable;
        }

        public String getDefault() {
            return default_;
        }

        void setDefault(String defaultStatement) {
            default_ = defaultStatement;
        }

        void addCustomAttribute(String attribute) {
            assert attribute != null;
            assert attribute.length() > 0;

            customAttributes_.add(attribute);
        }

        public ArrayList<String> getCustomAttributes() {
            return customAttributes_;
        }

        public Column clone() {
            Column new_instance = null;
            try {
                new_instance = (Column) super.clone();

                if (customAttributes_ != null) {
                    new_instance.customAttributes_ = new ArrayList<String>();
                    new_instance.customAttributes_.addAll(customAttributes_);
                }
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public static class ViolationAction extends EnumClass<String> {
        ViolationAction(String identifier) {
            super(identifier);
        }
    }

    public static class Nullable extends EnumClass<String> {
        Nullable(String identifier) {
            super(identifier);
        }
    }
}
