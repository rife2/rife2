/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import java.util.*;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;
import rife.database.capabilities.Capability;
import rife.database.exceptions.DbQueryException;
import rife.database.exceptions.TableNameOrFieldsRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.datastructures.EnumClass;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;
import rife.validation.Constrained;
import rife.validation.ConstrainedBean;
import rife.validation.ConstrainedUtils;

/**
 * Object representation of a SQL "SELECT" query.
 *
 * <p>This object may be used to dynamically construct a SQL statement in a
 * database-independent fashion. After it is finished, it may be executed using
 * one of the query methods on {@link rife.database.DbQueryManager
 * DbQueryManager}.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @since 1.0
 */
public class Select extends AbstractWhereQuery<Select> implements Cloneable, ReadQuery {
    private String hint_ = null;
    private List<String> fields_ = null;
    private String from_ = null;
    private List<Join> joins_ = null;
    private List<String> groupBy_ = null;
    private List<String> having_ = null;
    private boolean distinct_ = false;
    private List<String> distinctOn_ = null;
    private List<Union> unions_ = null;
    private List<OrderBy> orderBy_ = null;
    private int limit_ = -1;
    private int offset_ = -1;

    private Capabilities capabilities_ = null;

    private Class constrainedClass_ = null;

    public static final JoinCondition NATURAL = new JoinCondition("NATURAL");
    public static final JoinCondition ON = new JoinCondition("ON");
    public static final JoinCondition USING = new JoinCondition("USING");

    public static final JoinType LEFT = new JoinType("LEFT");
    public static final JoinType RIGHT = new JoinType("RIGHT");
    public static final JoinType FULL = new JoinType("FULL");

    public static final OrderByDirection ASC = new OrderByDirection("ASC");
    public static final OrderByDirection DESC = new OrderByDirection("DESC");

    public Select(Datasource datasource) {
        this(datasource, null);
    }

    public Select(Datasource datasource, Class constrainedClass) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        constrainedClass_ = constrainedClass;

        clear();
    }

    public void clear() {
        super.clear();

        hint_ = null;
        fields_ = new ArrayList<>();
        from_ = null;
        joins_ = new ArrayList<>();
        groupBy_ = new ArrayList<>();
        having_ = new ArrayList<>();
        distinct_ = false;
        distinctOn_ = new ArrayList<>();
        unions_ = new ArrayList<>();
        orderBy_ = new ArrayList<>();
        limit_ = -1;
        offset_ = -1;
        capabilities_ = null;
    }

    public void clearGenerated() {
        super.clearGenerated();

        capabilities_ = null;
    }

    public String getHint() {
        return hint_;
    }

    public Collection<String> getFields() {
        return fields_;
    }

    public boolean isDistinct() {
        return distinct_;
    }

    public Collection<String> getDistinctOn() {
        return distinctOn_;
    }

    public String getFrom() {
        return from_;
    }

    public Collection<Join> getJoins() {
        return joins_;
    }

    public Collection<String> getGroupBy() {
        return groupBy_;
    }

    public Collection<String> getHaving() {
        return having_;
    }

    public Collection<Union> getUnions() {
        return unions_;
    }

    public Collection<OrderBy> getOrderBy() {
        return orderBy_;
    }

    public int getLimit() {
        return limit_;
    }

    public int getOffset() {
        return offset_;
    }

    protected Template getTemplate() {
        return TemplateFactory.SQL.get("sql." + StringUtils.encodeClassname(datasource_.getAliasedDriver()) + ".select");
    }

    public Capabilities getCapabilities() {
        if (null == capabilities_) {
            Capabilities capabilities = null;

            if (getLimit() != -1) {
                if (null == capabilities) {
                    capabilities = new Capabilities();
                }

                capabilities.put(Capability.LIMIT, getLimit());
            }

            if (getLimitParameter() != null) {
                if (null == capabilities) {
                    capabilities = new Capabilities();
                }

                capabilities.put(Capability.LIMIT_PARAMETER, getLimitParameter());
            }

            if (getOffset() != -1) {
                if (null == capabilities) {
                    capabilities = new Capabilities();
                }

                capabilities.put(Capability.OFFSET, getOffset());
            }

            if (getOffsetParameter() != null) {
                if (null == capabilities) {
                    capabilities = new Capabilities();
                }

                capabilities.put(Capability.OFFSET_PARAMETER, getOffsetParameter());
            }

            capabilities_ = capabilities;
        }

        return capabilities_;
    }

    public String getSql()
    throws DbQueryException {
        Constrained constrained = ConstrainedUtils.getConstrainedInstance(constrainedClass_);

        // handle constrained beans meta-data that needs to be handled after all the
        // rest
        if (constrained != null) {
            ConstrainedBean constrained_bean = constrained.getConstrainedBean();
            if (constrained_bean != null) {
                // handle default ordering if no order statements have been
                // defined yet
                if (constrained_bean.hasDefaultOrdering() &&
                    0 == orderBy_.size()) {
                    Iterator<ConstrainedBean.Order> ordering_it = constrained_bean.getDefaultOrdering().iterator();
                    ConstrainedBean.Order order = null;
                    while (ordering_it.hasNext()) {
                        order = ordering_it.next();
                        orderBy(order.getPropertyName(), OrderByDirection.getDirection(order.getDirection().toString()));
                    }
                }
            }
        }

        if (null == from_ &&
            0 == fields_.size()) {
            throw new TableNameOrFieldsRequiredException("Select");
        } else {
            if (null == sql_) {
                Template template = getTemplate();
                String block = null;

                if (hint_ != null) {
                    if (!template.hasValueId("HINT")) {
                        throw new UnsupportedSqlFeatureException("HINT", datasource_.getAliasedDriver());
                    }
                    template.setValue("EXPRESSION", hint_);
                    template.setBlock("HINT", "HINT");
                }

                if (distinct_) {
                    if (0 == distinctOn_.size()) {
                        block = template.getBlock("DISTINCT");
                        if (0 == block.length()) {
                            throw new UnsupportedSqlFeatureException("DISTINCT", datasource_.getAliasedDriver());
                        }
                        template.setValue("DISTINCT", block);
                    } else {
                        if (template.hasValueId("COLUMNS")) {
                            template.setValue("COLUMNS", StringUtils.join(distinctOn_, template.getBlock("SEPERATOR")));
                        }
                        block = template.getBlock("DISTINCTON");
                        if (0 == block.length()) {
                            throw new UnsupportedSqlFeatureException("DISTINCT ON", datasource_.getAliasedDriver());
                        }
                        template.setValue("DISTINCT", block);
                    }
                }

                if (0 == fields_.size()) {
                    template.setValue("FIELDS", template.getBlock("ALLFIELDS"));
                } else {
                    template.setValue("FIELDS", StringUtils.join(fields_, template.getBlock("SEPERATOR")));
                }

                if (null != from_) {
                    template.setValue("TABLE", from_);
                    block = template.getBlock("FROM");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("FROM", datasource_.getAliasedDriver());
                    }
                    template.setValue("FROM", block);
                }

                if (joins_.size() > 0) {
                    ArrayList<String> join_list = new ArrayList<String>();
                    for (Join join : joins_) {
                        join_list.add(join.getSql(template));
                    }
                    template.setValue("JOINS", StringUtils.join(join_list, ""));
                }

                if (where_.length() > 0) {
                    template.setValue("CONDITION", where_);
                    block = template.getBlock("WHERE");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("WHERE", datasource_.getAliasedDriver());
                    }
                    template.setValue("WHERE", block);
                }

                if (groupBy_.size() > 0) {
                    template.setValue("EXPRESSION", StringUtils.join(groupBy_, template.getBlock("SEPERATOR")));
                    block = template.getBlock("GROUPBY");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("GROUP BY", datasource_.getAliasedDriver());
                    }
                    template.setValue("GROUPBY", block);
                }

                if (having_.size() > 0) {
                    template.setValue("EXPRESSION", StringUtils.join(having_, template.getBlock("SEPERATOR")));
                    block = template.getBlock("HAVING");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("HAVING", datasource_.getAliasedDriver());
                    }
                    template.setValue("HAVING", block);
                }

                if (unions_ != null) {
                    for (Union union : unions_) {
                        template.setValue("EXPRESSION", union.getExpression());
                        if (union.isAll()) {
                            block = template.getBlock("UNION_ALL");
                            if (0 == block.length()) {
                                throw new UnsupportedSqlFeatureException("UNION_ALL", datasource_.getAliasedDriver());
                            }
                            template.appendBlock("UNION", "UNION_ALL");
                        } else {
                            block = template.getBlock("UNION");
                            if (0 == block.length()) {
                                throw new UnsupportedSqlFeatureException("UNION", datasource_.getAliasedDriver());
                            }
                            template.appendBlock("UNION", "UNION");
                        }
                    }
                }

                if (orderBy_.size() > 0) {
                    ArrayList<String> orderby_list = new ArrayList<String>();
                    for (OrderBy order_by : orderBy_) {
                        orderby_list.add(order_by.getSql(template));
                    }
                    template.setValue("ORDERBY_PARTS", StringUtils.join(orderby_list, template.getBlock("SEPERATOR")));
                    block = template.getBlock("ORDERBY");
                    if (0 == block.length()) {
                        throw new UnsupportedSqlFeatureException("ORDER BY", datasource_.getAliasedDriver());
                    }
                    template.setValue("ORDERBY", block);
                }

                if (limit_ != -1 ||
                    getLimitParameter() != null) {
                    // integrate a default value for offset if that has been provided
                    // by the template
                    if (-1 == offset_ &&
                        template.hasValueId("OFFSET_VALUE")) {
                        String offset_value = template.getValue("OFFSET_VALUE");
                        if (offset_value != null &&
                            offset_value.trim().length() > 0) {
                            offset_ = Integer.parseInt(offset_value);
                        }
                    }

                    if (offset_ > -1 ||
                        getOffsetParameter() != null) {
                        if (template.hasValueId("OFFSET_VALUE")) {
                            if (getOffsetParameter() != null) {
                                template.setValue("OFFSET_VALUE", "?");
                            } else {
                                template.setValue("OFFSET_VALUE", offset_);
                            }
                        }

                        block = template.getBlock("OFFSET");
                        if (0 == block.length()) {
                            if (!excludeUnsupportedCapabilities_) {
                                throw new UnsupportedSqlFeatureException("OFFSET", datasource_.getAliasedDriver());
                            }
                        } else {
                            template.setValue("OFFSET", block);
                        }
                    }

                    if (template.hasValueId("LIMIT_VALUE")) {
                        if (getLimitParameter() != null) {
                            template.setValue("LIMIT_VALUE", "?");
                        } else {
                            template.setValue("LIMIT_VALUE", limit_);
                        }
                    }

                    block = template.getBlock("LIMIT");
                    if (0 == block.length()) {
                        if (!excludeUnsupportedCapabilities_) {
                            throw new UnsupportedSqlFeatureException("LIMIT", datasource_.getAliasedDriver());
                        }
                    } else {
                        template.setValue("LIMIT", block);
                    }
                }

                sql_ = template.getBlock("QUERY");

                assert sql_ != null;
                assert sql_.length() > 0;
            }
        }

        return sql_;
    }

    public Select hint(String hint) {
        clearGenerated();
        hint_ = hint;

        return this;
    }

    public Select field(String field) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");

        clearGenerated();
        fields_.add(field);

        return this;
    }

    public Select field(String alias, Select query) {
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        StringBuilder buffer = new StringBuilder();

        buffer.append("(");
        buffer.append(query.toString());
        buffer.append(") AS ");
        buffer.append(alias);

        field(buffer.toString());

        fieldSubselect(query);

        return this;
    }

    public Select fields(Class beanClass)
    throws DbQueryException {
        return fieldsExcluded(null, beanClass, (String[]) null);
    }

    public Select fieldsExcluded(Class beanClass, String... excludedFields)
    throws DbQueryException {
        return fieldsExcluded(null, beanClass, excludedFields);
    }

    public Select fields(String table, Class beanClass)
    throws DbQueryException {
        return fieldsExcluded(table, beanClass, (String[]) null);
    }

    public Select fieldsExcluded(String table, Class beanClass, String... excludedFields)
    throws DbQueryException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        Set<String> property_names = QueryHelper.getBeanPropertyNames(beanClass, excludedFields);

        Constrained constrained = ConstrainedUtils.getConstrainedInstance(beanClass);

        // handle the properties
        for (String property_name : property_names) {
            if (!ConstrainedUtils.persistConstrainedProperty(constrained, property_name, null)) {
                continue;
            }

            if (null == table) {
                field(property_name);
            } else {
                field(table + "." + property_name);
            }
        }

        return this;
    }

    public Select fields(String... fields) {
        if (null == fields) throw new IllegalArgumentException("fields can't be null.");

        if (fields.length > 0) {
            clearGenerated();
            fields_.addAll(Arrays.asList(fields));
        }

        return this;
    }

    public Select distinct() {
        clearGenerated();
        distinct_ = true;

        return this;
    }

    public Select distinctOn(String column) {
        if (null == column) throw new IllegalArgumentException("column can't be null.");
        if (0 == column.length()) throw new IllegalArgumentException("column can't be empty.");

        clearGenerated();
        distinct_ = true;
        distinctOn_.add(column);

        return this;
    }

    public Select distinctOn(String... columns) {
        if (null == columns) throw new IllegalArgumentException("columns can't be null.");

        if (columns.length > 0) {
            clearGenerated();
            distinct_ = true;
            distinctOn_.addAll(Arrays.asList(columns));
        }

        return this;
    }

    public Select from(String from) {
        if (null == from) throw new IllegalArgumentException("from can't be null.");
        if (0 == from.length()) throw new IllegalArgumentException("from can't be empty.");

        clearGenerated();
        from_ = from;

        return this;
    }

    public Select from(Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        StringBuilder buffer = new StringBuilder();

        buffer.append("(");
        buffer.append(query.toString());
        buffer.append(")");

        from(buffer.toString());

        _tableSubselect(query);

        return this;
    }

    public Select from(String alias, Select query) {
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        StringBuilder buffer = new StringBuilder();

        buffer.append("(");
        buffer.append(query.toString());
        buffer.append(") ");
        buffer.append(alias);

        from(buffer.toString());

        _tableSubselect(query);

        return this;
    }

    public Select join(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (0 == table.length()) throw new IllegalArgumentException("table can't be empty.");

        clearGenerated();
        joins_.add(new JoinDefault(table));

        return this;
    }

    public Select join(String alias, Select query) {
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        StringBuilder buffer = new StringBuilder();
        buffer.append("(");
        buffer.append(query.toString());
        buffer.append(") ");
        buffer.append(alias);

        join(buffer.toString());

        tableSubselect(query);

        return this;
    }

    public Select joinCustom(String customJoin) {
        if (null == customJoin) throw new IllegalArgumentException("customJoin can't be null.");
        if (0 == customJoin.length()) throw new IllegalArgumentException("customJoin can't be empty.");

        clearGenerated();
        joins_.add(new JoinCustom(customJoin));

        return this;
    }

    public Select joinCross(String table) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (0 == table.length()) throw new IllegalArgumentException("table can't be empty.");

        clearGenerated();
        joins_.add(new JoinCross(table));

        return this;
    }

    public Select joinInner(String table, JoinCondition condition, String conditionExpression) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (0 == table.length()) throw new IllegalArgumentException("table can't be empty.");
        if (null == condition) throw new IllegalArgumentException("condition can't be null.");
        if (NATURAL == condition &&
            conditionExpression != null)
            throw new IllegalArgumentException("a NATURAL join condition can't have a join expression.");
        if (NATURAL != condition &&
            null == conditionExpression) throw new IllegalArgumentException("conditionExpression can't be null.");
        if (NATURAL != condition &&
            0 == conditionExpression.length())
            throw new IllegalArgumentException("conditionExpression can't be empty.");

        clearGenerated();
        joins_.add(new JoinInner(table, condition, conditionExpression));

        return this;
    }

    public Select joinOuter(String table, JoinType type, JoinCondition condition, String conditionExpression) {
        if (null == table) throw new IllegalArgumentException("table can't be null.");
        if (0 == table.length()) throw new IllegalArgumentException("table can't be empty.");
        if (null == type) throw new IllegalArgumentException("type can't be null.");
        if (null == condition) throw new IllegalArgumentException("condition can't be null.");
        if (NATURAL == condition &&
            conditionExpression != null)
            throw new IllegalArgumentException("a NATURAL join condition can't have a join expression.");
        if (NATURAL != condition &&
            null == conditionExpression) throw new IllegalArgumentException("conditionExpression can't be null.");
        if (NATURAL != condition &&
            0 == conditionExpression.length())
            throw new IllegalArgumentException("conditionExpression can't be empty.");

        clearGenerated();
        joins_.add(new JoinOuter(table, type, condition, conditionExpression));

        return this;
    }

    public Select fieldSubselect(Select query) {
        _fieldSubselect(query);

        return this;
    }

    public Select tableSubselect(Select query) {
        _tableSubselect(query);

        return this;
    }

    public Select groupBy(String groupBy) {
        if (null == groupBy) throw new IllegalArgumentException("groupBy can't be null.");
        if (0 == groupBy.length()) throw new IllegalArgumentException("groupBy can't be empty.");

        clearGenerated();
        groupBy_.add(groupBy);

        return this;
    }

    public Select groupBy(Class beanClass)
    throws DbQueryException {
        return groupByExcluded(beanClass, (String[]) null);
    }

    public Select groupByExcluded(Class beanClass, String... excludedFields)
    throws DbQueryException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        Set<String> property_names = QueryHelper.getBeanPropertyNames(beanClass, excludedFields);

        clearGenerated();

        for (String property_name : property_names) {
            groupBy_.add(property_name);
        }

        return this;
    }

    public Select having(String having) {
        if (null == having) throw new IllegalArgumentException("having can't be null.");
        if (0 == having.length()) throw new IllegalArgumentException("having can't be empty.");

        clearGenerated();
        having_.add(having);

        return this;
    }

    public Select union(String union) {
        if (null == union) throw new IllegalArgumentException("union can't be null.");
        if (0 == union.length()) throw new IllegalArgumentException("union can't be empty.");

        clearGenerated();
        unions_.add(new Union(union, false));

        return this;
    }

    public Select union(Select union)
    throws DbQueryException {
        if (null == union) throw new IllegalArgumentException("union can't be null.");

        union(union.getSql());
        _unionSubselect(union);

        return this;
    }

    public Select unionAll(String union) {
        if (null == union) throw new IllegalArgumentException("union can't be null.");
        if (0 == union.length()) throw new IllegalArgumentException("union can't be empty.");

        clearGenerated();
        unions_.add(new Union(union, true));

        return this;
    }

    public Select unionAll(Select union)
    throws DbQueryException {
        if (null == union) throw new IllegalArgumentException("union can't be null.");

        unionAll(union.getSql());
        _unionSubselect(union);

        return this;
    }

    public Select orderBy(String column) {
        clearGenerated();
        return orderBy(column, ASC);
    }

    public Select orderBy(String column, OrderByDirection direction) {
        if (null == column) throw new IllegalArgumentException("column can't be null.");
        if (0 == column.length()) throw new IllegalArgumentException("column can't be empty.");
        if (null == direction) throw new IllegalArgumentException("direction can't be null.");

        OrderBy orderby = new OrderBy(column, direction);
        clearGenerated();
        orderBy_.add(orderby);

        return this;
    }

    public Select limit(int limit) {
        if (limit < 1) throw new IllegalArgumentException("limit must be at least 1.");

        clearGenerated();
        limit_ = limit;
        setLimitParameter(null);

        return this;
    }

    public Select limitParameter(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        clearGenerated();
        limit_ = -1;
        setLimitParameter(name);

        return this;
    }

    public Select offset(int offset) {
        if (offset < 0) throw new IllegalArgumentException("offset must be at least 0.");

        clearGenerated();
        offset_ = offset;
        setOffsetParameter(null);

        return this;
    }

    protected boolean isLimitBeforeOffset() {
        Template template = getTemplate();
        if (!template.hasValueId("OFFSET") ||
            !template.hasValueId("LIMIT_VALUE")
        ) {
            return super.isLimitBeforeOffset();
        }

        String offset = template.getValue("OFFSET");
        String limit_value = template.getValue("LIMIT_VALUE");
        template.setValue("OFFSET", "offset");
        template.setValue("LIMIT_VALUE", "limit");
        String limit = template.getBlock("LIMIT");

        template.setValue("OFFSET", offset);
        template.setValue("LIMIT_VALUE", limit_value);

        return limit.indexOf("offset") >= limit.indexOf("limit");
    }

    public Select offsetParameter(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        clearGenerated();
        offset_ = -1;
        setOffsetParameter(name);

        return this;
    }

    public Select clone() {
        Select new_instance = super.clone();
        if (new_instance != null) {
            if (fields_ != null) {
                new_instance.fields_ = new ArrayList<String>();
                new_instance.fields_.addAll(fields_);
            }

            if (joins_ != null) {
                new_instance.joins_ = new ArrayList<Join>();

                for (Join join : joins_) {
                    new_instance.joins_.add(join.clone());
                }
            }

            if (unions_ != null) {
                new_instance.unions_ = new ArrayList<Union>();
                for (Union union : unions_) {
                    new_instance.unions_.add(union.clone());
                }
            }

            if (groupBy_ != null) {
                new_instance.groupBy_ = new ArrayList<String>();
                new_instance.groupBy_.addAll(groupBy_);
            }

            if (having_ != null) {
                new_instance.having_ = new ArrayList<String>();
                new_instance.having_.addAll(having_);
            }

            if (distinctOn_ != null) {
                new_instance.distinctOn_ = new ArrayList<String>();
                new_instance.distinctOn_.addAll(distinctOn_);
            }

            if (orderBy_ != null) {
                new_instance.orderBy_ = new ArrayList<OrderBy>();
                for (OrderBy order_by : orderBy_) {
                    new_instance.orderBy_.add(order_by.clone());
                }
            }
        }

        return new_instance;
    }

    public static class JoinCondition extends EnumClass<String> {
        JoinCondition(String identifier) {
            super(identifier);
        }
    }

    public static class JoinType extends EnumClass<String> {
        JoinType(String identifier) {
            super(identifier);
        }
    }

    public static class OrderByDirection extends EnumClass<String> {
        OrderByDirection(String identifier) {
            super(identifier);
        }

        public static OrderByDirection getDirection(String identifier) {
            return getMember(OrderByDirection.class, identifier);
        }
    }

    public class JoinCustom extends Join {
        JoinCustom(String customJoin) {
            super(customJoin);
        }

        String getSql(Template template) {
            return " " + getData();
        }
    }

    public class JoinDefault extends Join implements Cloneable {
        JoinDefault(String table) {
            super(table);
        }

        String getSql(Template template) {
            assert template != null;

            String result = null;

            template.setValue("TABLE", getData());
            result = template.getBlock("JOIN_DEFAULT");
            template.removeValue("TABLE");

            assert result != null;
            assert result.length() > 0;

            return result;
        }

        public JoinDefault clone() {
            return (JoinDefault) super.clone();
        }
    }

    public class JoinCross extends Join implements Cloneable {
        JoinCross(String table) {
            super(table);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String result = null;

            template.setValue("TABLE", getData());
            result = template.getBlock("JOIN_CROSS");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("CROSS JOIN", datasource_.getAliasedDriver());
            }
            template.removeValue("TABLE");

            assert result != null;
            assert result.length() > 0;

            return result;
        }

        public JoinCross clone() {
            return (JoinCross) super.clone();
        }
    }

    public class JoinInner extends Join implements Cloneable {
        private JoinCondition mCondition = null;
        private String mExpression = null;

        JoinInner(String table, JoinCondition condition, String expression) {
            super(table);

            assert condition != null;
            assert condition == Select.NATURAL || (expression != null && expression.length() > 0);

            setCondition(condition);
            setExpression(expression);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String condition = null;
            String result = null;

            template.setValue("TABLE", getData());
            if (getExpression() != null) {
                template.setValue("EXPRESSION", getExpression());
            }
            condition = template.getBlock("JOIN_INNER_" + getCondition().toString());
            if (0 == condition.length()) {
                throw new UnsupportedSqlFeatureException(getCondition().toString() + " for INNER JOIN", datasource_.getAliasedDriver());
            }
            template.setValue("JOIN_INNER_" + getCondition().toString(), condition);
            result = template.getBlock("JOIN_INNER");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("INNER JOIN", datasource_.getAliasedDriver());
            }
            template.removeValue("TABLE");
            template.removeValue("EXPRESSION");
            template.removeValue("JOIN_INNER_" + getCondition().toString());

            assert result != null;
            assert result.length() > 0;

            return result;
        }

        public JoinCondition getCondition() {
            return mCondition;
        }

        void setCondition(JoinCondition condition) {
            assert condition != null;

            mCondition = condition;
        }

        public String getExpression() {
            return mExpression;
        }

        void setExpression(String expression) {
            mExpression = expression;
        }

        public JoinInner clone() {
            return (JoinInner) super.clone();
        }
    }

    public class JoinOuter extends Join implements Cloneable {
        private JoinType mType = null;
        private JoinCondition mCondition = null;
        private String mExpression = null;

        JoinOuter(String table, JoinType type, JoinCondition condition, String expression) {
            super(table);

            assert type != null;
            assert condition != null;
            assert condition == Select.NATURAL || (expression != null && expression.length() > 0);

            setType(type);
            setCondition(condition);
            setExpression(expression);
        }

        String getSql(Template template)
        throws DbQueryException {
            assert template != null;

            String type = null;
            String condition = null;
            String result = null;

            template.setValue("TABLE", getData());
            if (getType() != null) {
                type = template.getBlock("JOIN_OUTER_" + getType().toString());
                if (0 == type.length()) {
                    throw new UnsupportedSqlFeatureException(getType().toString() + " for OUTER JOIN", datasource_.getAliasedDriver());
                }
                template.setValue("JOIN_OUTER_TYPE", type);
            }
            if (getExpression() != null) {
                template.setValue("EXPRESSION", getExpression());
            }
            condition = template.getBlock("JOIN_OUTER_" + getCondition().toString());
            if (0 == condition.length()) {
                throw new UnsupportedSqlFeatureException(getCondition().toString() + " for OUTER JOIN", datasource_.getAliasedDriver());
            }
            template.setValue("JOIN_OUTER_" + getCondition().toString(), condition);
            result = template.getBlock("JOIN_OUTER");
            if (0 == result.length()) {
                throw new UnsupportedSqlFeatureException("OUTER JOIN", datasource_.getAliasedDriver());
            }
            template.removeValue("TABLE");
            template.removeValue("EXPRESSION");
            template.removeValue("JOIN_OUTER_" + getCondition().toString());
            template.removeValue("JOIN_OUTER_TYPE");

            assert result != null;
            assert result.length() > 0;

            return result;
        }

        public JoinType getType() {
            return mType;
        }

        void setType(JoinType type) {
            assert type != null;

            mType = type;
        }

        public JoinCondition getCondition() {
            return mCondition;
        }

        void setCondition(JoinCondition condition) {
            assert condition != null;

            mCondition = condition;
        }

        public String getExpression() {
            return mExpression;
        }

        void setExpression(String expression) {
            mExpression = expression;
        }

        public JoinOuter clone() {
            return (JoinOuter) super.clone();
        }
    }

    public abstract class Join implements Cloneable {
        private String mData = null;

        Join(String data) {
            assert data != null;
            assert data.length() > 0;

            setData(data);
        }

        abstract String getSql(Template template)
        throws DbQueryException;

        public String getData() {
            return mData;
        }

        void setData(String data) {
            assert data != null;
            assert data.length() > 0;

            mData = data;
        }

        public Join clone() {
            Join new_instance = null;
            try {
                new_instance = (Join) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public class OrderBy implements Cloneable {
        private String mColumn = null;
        private OrderByDirection mDirection = null;

        OrderBy(String column, OrderByDirection direction) {
            assert column != null;
            assert column.length() > 0;
            assert direction != null;

            setColumn(column);
            setDirection(direction);
        }

        String getSql(Template template) {
            assert template != null;

            String result = null;

            template.setValue("COLUMN", getColumn());
            template.setValue("DIRECTION", template.getBlock("ORDERBY_" + getDirection().toString()));
            result = template.getBlock("ORDERBY_PART");
            template.removeValue("COLUMN");
            template.removeValue("DIRECTION");

            assert result != null;
            assert result.length() > 0;

            return result;
        }

        public String getColumn() {
            return mColumn;
        }

        void setColumn(String column) {
            assert column != null;
            assert column.length() > 0;

            mColumn = column;
        }

        public OrderByDirection getDirection() {
            return mDirection;
        }

        void setDirection(OrderByDirection direction) {
            assert direction != null;

            mDirection = direction;
        }

        public OrderBy clone() {
            OrderBy new_instance = null;
            try {
                new_instance = (OrderBy) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }

    public class Union implements Cloneable {
        private String mExpression = null;
        private boolean mAll = false;

        Union(String expression, boolean all) {
            setExpression(expression);
            setAll(all);
        }

        void setExpression(String expression) {
            assert expression != null;
            assert expression.length() > 0;

            mExpression = expression;
        }

        public String getExpression() {
            return mExpression;
        }

        void setAll(boolean all) {
            mAll = all;
        }

        public boolean isAll() {
            return mAll;
        }

        public Union clone() {
            Union new_instance = null;
            try {
                new_instance = (Union) super.clone();
            } catch (CloneNotSupportedException e) {
                new_instance = null;
            }

            return new_instance;
        }
    }
}
