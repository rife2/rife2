/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.*;
import rife.database.exceptions.MissingManyToOneColumnException;
import rife.database.exceptions.MissingManyToOneTableException;
import rife.database.queries.*;

import java.lang.reflect.*;
import java.util.*;

import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.ExecutionErrorException;
import rife.database.querymanagers.generic.exceptions.IncompatibleValidationTypeException;
import rife.database.querymanagers.generic.exceptions.UnsupportedManyToManyValueTypeException;
import rife.database.querymanagers.generic.instrument.LazyLoadAccessorsBytecodeTransformer;
import rife.tools.BeanUtils;
import rife.tools.ClassUtils;
import rife.tools.InnerClassException;
import rife.tools.StringUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.*;

import static rife.database.querymanagers.generic.GenericQueryManagerRelationalUtils.*;

public abstract class AbstractGenericQueryManager<BeanType> extends DbQueryManager implements GenericQueryManager<BeanType> {
    protected Class<BeanType> baseClass_ = null;
    protected String primaryKey_ = null;
    protected Method getPrimaryKeyMethod_ = null;
    protected Method setPrimaryKeyMethod_ = null;
    protected boolean sparseIdentifier_ = false;

    protected List<GenericQueryManagerListener<BeanType>> listeners_ = null;

    public AbstractGenericQueryManager(Datasource datasource, Class<BeanType> beanClass, String primaryKey)
    throws DatabaseException {
        super(datasource);

        baseClass_ = beanClass;
        primaryKey_ = primaryKey;
        try {
            var capitalized_primary_key = StringUtils.capitalize(primaryKey_);
            getPrimaryKeyMethod_ = baseClass_.getMethod("get" + capitalized_primary_key, (Class[]) null);
            try {
                setPrimaryKeyMethod_ = baseClass_.getMethod("set" + capitalized_primary_key, int.class);
            } catch (NoSuchMethodException e) {
                try {
                    setPrimaryKeyMethod_ = baseClass_.getMethod("set" + capitalized_primary_key, Integer.class);
                } catch (NoSuchMethodException e2) {
                    throw e;
                }
            }
        } catch (Throwable e) {
            throw new DatabaseException(e);
        }

        var constrained_bean = ConstrainedUtils.getConstrainedInstance(getBaseClass());
        if (constrained_bean != null) {
            var constrained_property = constrained_bean.getConstrainedProperty(primaryKey);
            if (constrained_property != null) {
                sparseIdentifier_ = constrained_property.isSparse();
            }
        }
    }

    public Class getBaseClass() {
        return baseClass_;
    }

    public String getIdentifierName() {
        return primaryKey_;
    }

    public int getIdentifierValue(BeanType bean)
    throws DatabaseException {
        try {
            var id = (Integer) getPrimaryKeyMethod_.invoke(bean, (Object[]) null);
            if (null == id) {
                return -1;
            }

            return id;
        } catch (Throwable e) {
            throw new DatabaseException(e);
        }
    }

    public boolean isIdentifierSparse() {
        return sparseIdentifier_;
    }

    public void validate(Validated validated) {
        // perform validation
        if (validated != null &&
            !(validated.getClass() == baseClass_)) {
            throw new IncompatibleValidationTypeException(validated.getClass(), baseClass_);
        }

        var bean = (BeanType) validated;

        // handle before callback
        Callbacks callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeValidate(bean)) {
            return;
        }
        _validateWithoutCallbacks(validated);

        // handle after callback
        if (callbacks != null) {
            callbacks.afterValidate(bean);
        }
    }

    private static int getIdentifierValue(Object bean, String propertyName)
    throws DatabaseException {
        try {
            var id = (Integer) BeanUtils.getPropertyValue(bean, propertyName);
            if (null == id) {
                return -1;
            }

            return id;
        } catch (Throwable e) {
            throw new DatabaseException(e);
        }
    }

    protected void _validateWithoutCallbacks(final Validated validated) {
        if (null == validated) {
            return;
        }

        // handle constrained beans
        final var constrained = ConstrainedUtils.makeConstrainedInstance(validated);

        if (constrained != null) {
            // check if the identifier exists or is still undefined (existing or
            // new entry)
            var identifier_exists = false;
            var identifier_value = getIdentifierValue((BeanType) validated);
            if (identifier_value >= 0) {
                identifier_exists = true;
            }

            // validate the many-to-one entities
            processManyToOneJoinColumns(this, new ManyToOneJoinColumnProcessor() {
                public boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration) {
                    Object property_value = null;
                    try {
                        property_value = BeanUtils.getPropertyValue(constrained, propertyName);
                    } catch (BeanUtilsException e) {
                        throw new DatabaseException(e);
                    }

                    if (property_value != null) {
                        var identifier_value = getIdentifierValue(property_value, declaration.getAssociationColumn());
                        if (identifier_value >= 0) {
                            if (!executeHasResultRows(declaration.getAssociationManager().getRestoreQuery(identifier_value))) {
                                validated.addValidationError(new ValidationError.INVALID(propertyName));
                            }
                        }
                    }

                    return true;
                }
            });

            Map<String, ManyToOneAssociationDeclaration> manytoone_association_declarations = null;
            Map<String, Object> manytoone_association_property_values = null;
            var obtained_manytoone_association_declarations = false;

            Map<String, ManyToManyDeclaration> manytomany_declarations = null;
            Map<String, Object> manytomany_property_values = null;
            var obtained_manytomany_declarations = false;

            // handle individual properties
            for (var property : constrained.getConstrainedProperties()) {
                // handle the uniqueness of individual properties
                if (property.isUnique()) {
                    Object property_value = null;
                    try {
                        property_value = BeanUtils.getPropertyValue(constrained, property.getPropertyName());
                    } catch (BeanUtilsException e) {
                        throw new DatabaseException(e);
                    }

                    if (property_value != null) {
                        var count_query = getCountQuery()
                            .where(property.getPropertyName(), "=", property_value);
                        if (identifier_exists) {
                            count_query.whereAnd(primaryKey_, "!=", identifier_value);
                        }

                        if (count(count_query) > 0) {
                            validated.addValidationError(new ValidationError.UNIQUENESS(property.getPropertyName()));
                        }
                    }
                }

                // handle the manyToOne constraint that contain the identifier values
                if (property.hasManyToOne()) {
                    Object property_value = null;
                    try {
                        property_value = BeanUtils.getPropertyValue(constrained, property.getPropertyName());
                    } catch (BeanUtilsException e) {
                        throw new DatabaseException(e);
                    }

                    if (property_value != null &&
                        ClassUtils.isBasic(property_value.getClass())) {
                        var many_to_one = property.getManyToOne();

                        if (null == many_to_one.getDerivedTable()) {
                            throw new MissingManyToOneTableException(constrained.getClass(), property.getPropertyName());
                        }

                        if (null == many_to_one.getColumn()) {
                            throw new MissingManyToOneColumnException(constrained.getClass(), property.getPropertyName());
                        }

                        if (!executeHasResultRows(new Select(getDatasource())
                            .from(many_to_one.getDerivedTable())
                            .where(many_to_one.getColumn(), "=", property_value))) {
                            validated.addValidationError(new ValidationError.INVALID(property.getPropertyName()));
                        }
                    }
                }

                // handle the manyToOneAssociation constraint
                if (property.hasManyToOneAssociation()) {
                    // make sure that the many-to-one association declarations have been obtained
                    if (!obtained_manytoone_association_declarations) {
                        manytoone_association_declarations = obtainManyToOneAssociationDeclarations(this, constrained);

                        if (manytoone_association_declarations != null) {
                            // get the property values of those that contain many-to-one association relationships
                            var manytoone_association_property_names = new String[manytoone_association_declarations.size()];
                            manytoone_association_declarations.keySet().toArray(manytoone_association_property_names);
                            try {
                                manytoone_association_property_values = BeanUtils.getPropertyValues(constrained, manytoone_association_property_names, null, null);
                            } catch (BeanUtilsException e) {
                                throw new DatabaseException(e);
                            }
                        }

                        obtained_manytoone_association_declarations = true;
                    }

                    var declaration = manytoone_association_declarations.get(property.getPropertyName());
                    if (declaration != null) {
                        var value = manytoone_association_property_values.get(property.getPropertyName());
                        var type = declaration.getMainType();
                        try {
                            checkCollectionRelationshipValidity(validated, property, value, type);
                        } catch (ClassCastException e) {
                            throw new UnsupportedManyToManyValueTypeException(validated.getClass(), property.getName(), e);
                        }
                    }
                }

                // handle the manyToMany constraint
                if (property.hasManyToMany() ||
                    property.hasManyToManyAssociation()) {
                    // make sure that the many-to-many declarations have been obtained
                    if (!obtained_manytomany_declarations) {
                        manytomany_declarations = obtainManyToManyDeclarations(this, constrained, true);

                        if (manytomany_declarations != null) {
                            // get the property values of those that contain many-to-many relationships
                            var manytomany_property_names = new String[manytomany_declarations.size()];
                            manytomany_declarations.keySet().toArray(manytomany_property_names);
                            try {
                                manytomany_property_values = BeanUtils.getPropertyValues(constrained, manytomany_property_names, null, null);
                            } catch (BeanUtilsException e) {
                                throw new DatabaseException(e);
                            }
                        }

                        obtained_manytomany_declarations = true;
                    }

                    var declaration = manytomany_declarations.get(property.getPropertyName());
                    if (declaration != null) {
                        var value = manytomany_property_values.get(property.getPropertyName());
                        try {
                            checkCollectionRelationshipValidity(validated, property, value, declaration.getAssociationType());
                        } catch (ClassCastException e) {
                            throw new UnsupportedManyToManyValueTypeException(validated.getClass(), property.getName(), e);
                        }
                    }

                }
            }

            // handle the bean-wide uniqueness
            var constrained_bean = constrained.getConstrainedBean();
            if (constrained_bean != null &&
                constrained_bean.hasUniques()) {
                for (var uniques : constrained_bean.getUniques()) {
                    var count_query = getCountQuery();
                    if (identifier_exists) {
                        count_query.where(primaryKey_, "!=", identifier_value);
                    }

                    for (var unique : uniques) {
                        Object property_value = null;
                        try {
                            property_value = BeanUtils.getPropertyValue(constrained, unique);
                        } catch (BeanUtilsException e) {
                            throw new DatabaseException(e);
                        }

                        if (null == property_value) {
                            count_query = null;
                            break;
                        } else {
                            count_query.where(unique, "=", property_value);
                        }
                    }

                    if (count_query != null &&
                        count(count_query) > 0) {
                        for (var unique : uniques) {
                            validated.addValidationError(new ValidationError.UNIQUENESS(unique));
                        }
                    }
                }
            }
        }
    }

    private void checkCollectionRelationshipValidity(final Validated validated, final ConstrainedProperty property, final Object propertyValue, final Class elementType)
    throws ClassCastException {
        if (elementType != null &&
            propertyValue != null) {
            // cast the property value to a collection
            var value_collection = (Collection) propertyValue;

            // iterate over all the collection elements to obtain the identifier values
            Set<Integer> identifiers = new HashSet<>();
            var element_manager = createNewManager(elementType);
            for (var entity : value_collection) {
                var identifier_value = element_manager.getIdentifierValue(entity);
                // only add the identifiers that have a value
                if (identifier_value != -1) {
                    identifiers.add(identifier_value);
                }
            }

            // check if the many-to-one associations exist
            var count_query = element_manager.getCountQuery()
                .where(element_manager.getIdentifierName() + " IN (" + StringUtils.join(identifiers, ",") + ")");
            var count = element_manager.count(count_query);
            if (count != identifiers.size()) {
                validated.addValidationError(new ValidationError.INVALID(property.getPropertyName()));
            }
        }
    }

    protected Callbacks<BeanType> getCallbacks(BeanType bean) {
        if (null == bean) return null;

        Callbacks<BeanType> callbacks = null;
        if (bean instanceof CallbacksProvider) {
            callbacks = ((CallbacksProvider<BeanType>) bean).getCallbacks();
        } else if (bean instanceof Callbacks) {
            callbacks = (Callbacks<BeanType>) bean;
        }
        return callbacks;
    }

    protected int _update(final Update saveUpdate, final BeanType bean) {
        // handle before callback
        var callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeUpdate(bean)) {
            return -1;
        }

        // perform update
        var result = _updateWithoutCallbacks(saveUpdate, bean);

        // handle after callback
        if (callbacks != null) {
            callbacks.afterUpdate(bean, result != -1);
        }

        return result;
    }

    protected int _updateWithoutCallbacks(final Update saveUpdate, final BeanType bean) {
        assert saveUpdate != null;

        final var identifier_value = getIdentifierValue(bean);
        int result = inTransaction(new DbTransactionUser<>() {
            public Integer useTransaction()
            throws InnerClassException {
                var result = identifier_value;

                storeManyToOne(bean);

                if (0 == executeUpdate(saveUpdate, statement -> {
                    statement
                        .setBean(bean);

                    setManyToOneJoinParameters(statement, bean);
                })) {
                    result = -1;
                } else {
                    storeManyToOneAssociations(bean, identifier_value);
                    storeManyToMany(bean, identifier_value);
                }

                return result;
            }
        });

        // handle listeners
        if (result != -1) {
            fireUpdated(bean);
        }

        return result;
    }

    protected int _insert(final SequenceValue nextId, final Insert save, final BeanType bean) {
        // handle before callback
        var callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeInsert(bean)) {
            return -1;
        }

        // perform insert
        var result = _insertWithoutCallbacks(nextId, save, bean);

        // handle after callback
        if (callbacks != null) {
            callbacks.afterInsert(bean, result != -1);
        }

        return result;
    }

    protected int _insertWithoutCallbacks(final SequenceValue nextId, final Insert save, final BeanType bean) {
        assert nextId != null;
        assert save != null;

        var value = -1;

        value = inTransaction(new DbTransactionUser<>() {
            public Integer useTransaction()
            throws InnerClassException {
                storeManyToOne(bean);

                var result = getIdentifierValue(bean);
                if (!isIdentifierSparse()) {
                    result = executeGetFirstInt(nextId);
                }

                final var primary_key_id = result;
                executeUpdate(save, statement -> {
                    statement
                        .setBean(bean)
                        .setInt(primaryKey_, primary_key_id);

                    setManyToOneJoinParameters(statement, bean);
                });

                storeManyToOneAssociations(bean, primary_key_id);
                storeManyToMany(bean, primary_key_id);

                return result;
            }
        });

        try {
            setPrimaryKeyMethod_.invoke(bean, value);
        } catch (Throwable e) {
            throw new DatabaseException(e);
        }

        // handle listeners
        if (value != -1) {
            fireInserted(bean);
        }

        return value;
    }

    protected void setManyToOneJoinParameters(final DbPreparedStatement statement, final BeanType bean) {
        final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);

        // handle many-to-one join column parameters
        processManyToOneJoinColumns(this, (columnName, propertyName, declaration) -> {
            try {
                var join_column_property = BeanUtils.getPropertyValue(bean, propertyName);
                Object identifier_value = null;
                if (join_column_property != null) {
                    identifier_value = BeanUtils.getPropertyValue(join_column_property, declaration.getAssociationColumn());
                }
                var identifier_type = BeanUtils.getPropertyType(declaration.getAssociationType(), declaration.getAssociationColumn());

                var indices = statement.getParameterIndices(columnName);
                for (var index : indices) {
                    getDatasource().getSqlConversion().setTypedParameter(statement, index, identifier_type, columnName, identifier_value, constrained);
                }
            } catch (BeanUtilsException e) {
                throw new DatabaseException(e);
            }

            return true;
        });
    }

    protected void storeManyToOne(final BeanType bean) {
        final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        final var declarations = obtainManyToOneDeclarations(this, constrained, null, null);
        if (declarations != null) {
            // get the property values of those that contain many-to-one relationships
            var property_names = new String[declarations.size()];
            declarations.keySet().toArray(property_names);
            final Map<String, Object> values;
            try {
                values = BeanUtils.getPropertyValues(bean, property_names, null, null);
            } catch (BeanUtilsException e) {
                throw new DatabaseException(e);
            }

            // iterate over all the many-to-one relationships that have associated classes and instance values
            for (var entry : declarations.entrySet()) {
                var declaration = entry.getValue();
                if (!declaration.isBasic()) {
                    var association_manager = declaration.getAssociationManager();
                    var property_name = entry.getKey();

                    // obtain the property value
                    var value = values.get(property_name);
                    if (value != null) {
                        var identifier_value = association_manager.getIdentifierValue(value);
                        // insert the collection entries that have no identifier value
                        if (identifier_value < 0) {
                            identifier_value = association_manager.insert(value);
                        }
                    }
                }
            }
        }
    }

    protected void storeManyToOneAssociations(final BeanType bean, final int objectId) {
        final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        final var declarations = obtainManyToOneAssociationDeclarations(this, constrained);
        if (declarations != null) {
            // get the property values of those that contain many-to-one relationships
            var property_names = new String[declarations.size()];
            declarations.keySet().toArray(property_names);
            final Map<String, Object> values;
            try {
                values = BeanUtils.getPropertyValues(bean, property_names, null, null);
            } catch (BeanUtilsException e) {
                throw new DatabaseException(e);
            }

            // iterate over all the many-to-one relationships that have associated classes and instance values
            for (var entry : declarations.entrySet()) {
                var declaration = entry.getValue();
                var main_manager = createNewManager(declaration.getMainType());
                var property_name = entry.getKey();

                // obtain the property value
                var value = values.get(property_name);

                final var main_table = main_manager.getTable();
                final var main_identify_column = main_manager.getIdentifierName();
                final var main_join_column = generateManyToOneJoinColumnName(declaration.getMainProperty(), declaration.getMainDeclaration());

                var clear_previous_mappings = new Update(getDatasource())
                    .table(main_table)
                    .fieldCustom(main_join_column, "NULL")
                    .where(main_join_column, "=", objectId);
                executeUpdate(clear_previous_mappings);

                if (value != null) {
                    ensureSupportedManyToOneAssociationPropertyValueType(baseClass_, property_name, value);

                    var value_collection = (Collection) value;

                    var update_mapping = new Update(getDatasource())
                        .table(main_table)
                        .fieldParameter(main_join_column)
                        .whereParameter(main_identify_column, "=");

                    // store the collection entries
                    for (var many_to_one_entity : value_collection) {
                        var identifier_value = main_manager.getIdentifierValue(many_to_one_entity);
                        // insert the collection entries that have no identifier value
                        if (identifier_value < 0) {
                            identifier_value = main_manager.insert(many_to_one_entity);
                        }

                        // store the many-to-one mappings
                        executeUpdate(update_mapping, new DbPreparedStatementHandler<>(identifier_value) {
                            public void setParameters(DbPreparedStatement statement) {
                                statement
                                    .setInt(main_join_column, objectId)
                                    .setInt(main_identify_column, getData());
                            }
                        });
                    }
                }
            }
        }
    }

    protected void storeManyToMany(final BeanType bean, final int objectId) {
        final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        final var declarations = obtainManyToManyDeclarations(this, constrained, true);
        if (declarations != null) {
            // get the property values of those that contain many-to-many relationships
            var property_names = new String[declarations.size()];
            declarations.keySet().toArray(property_names);
            final Map<String, Object> values;
            try {
                values = BeanUtils.getPropertyValues(bean, property_names, null, null);
            } catch (BeanUtilsException e) {
                throw new DatabaseException(e);
            }

            // iterate over all the many-to-many relationships
            final var column1_name = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
            for (var entry : declarations.entrySet()) {
                var declaration = entry.getValue();
                var association_manager = createNewManager(declaration.getAssociationType());
                var join_table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);
                final var column2_name = generateManyToManyJoinColumnName(association_manager);
                var property_name = entry.getKey();

                // obtain the property value
                var value = values.get(property_name);

                // create the delete statement to remove all the possible previous
                // mappings in the join table for this primary key ID
                var delete_previous_mappings = new Delete(getDatasource())
                    .from(join_table)
                    .where(column1_name, "=", objectId);
                executeUpdate(delete_previous_mappings);

                if (value != null) {
                    ensureSupportedManyToManyPropertyValueType(baseClass_, property_name, value);

                    var value_collection = (Collection) value;

                    var insert_mapping = new Insert(getDatasource())
                        .into(join_table)
                        .fieldParameter(column1_name)
                        .fieldParameter(column2_name);
                    // store the collection entries
                    for (var many_to_many_entity : value_collection) {
                        var identifier_value = association_manager.getIdentifierValue(many_to_many_entity);
                        // insert the collection entries that have no identifier value
                        if (identifier_value < 0) {
                            identifier_value = association_manager.insert(many_to_many_entity);
                        }
                        // store the many-to_many mappings
                        executeUpdate(insert_mapping, new DbPreparedStatementHandler<>(identifier_value) {
                            public void setParameters(DbPreparedStatement statement) {
                                statement
                                    .setInt(column1_name, objectId)
                                    .setInt(column2_name, getData());
                            }
                        });
                    }
                }
            }
        }
    }

    protected int _save(final SequenceValue nextId, final Insert save, final Update saveUpdate, final BeanType bean)
    throws DatabaseException {
        assert nextId != null;
        assert save != null;
        assert saveUpdate != null;

        var value = -1;

        // handle before callback
        final var callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeSave(bean)) {
            return -1;
        }

        // cancel indicator
        final var is_cancelled = new boolean[]{false};

        // perform save
        value = inTransaction(new DbTransactionUser<>() {
            public Integer useTransaction()
            throws InnerClassException {
                var result = getIdentifierValue(bean);
                if (isIdentifierSparse()) {
                    // handle before callback
                    if (callbacks != null &&
                        !callbacks.beforeInsert(bean)) {
                        is_cancelled[0] = true;
                        return -1;
                    }

                    // try to perform the insert
                    try {
                        result = _insertWithoutCallbacks(nextId, save, bean);
                    } catch (ExecutionErrorException e) {
                        result = -1;
                    }

                    // handle after callback
                    if (callbacks != null &&
                        !callbacks.afterInsert(bean, result != -1)) {
                        is_cancelled[0] = true;
                        return result;
                    }

                    // perform update if insert failed
                    if (-1 == result) {
                        // handle before callback
                        if (callbacks != null &&
                            !callbacks.beforeUpdate(bean)) {
                            is_cancelled[0] = true;
                            return -1;
                        }

                        result = _updateWithoutCallbacks(saveUpdate, bean);

                        // handle after callback
                        if (callbacks != null &&
                            !callbacks.afterUpdate(bean, result != -1)) {
                            is_cancelled[0] = true;
                            return result;
                        }
                    }
                } else {
                    // try to update
                    if (result >= 0) {
                        // handle before callback
                        if (callbacks != null &&
                            !callbacks.beforeUpdate(bean)) {
                            is_cancelled[0] = true;
                            return -1;
                        }

                        result = _updateWithoutCallbacks(saveUpdate, bean);

                        // handle after callback
                        if (callbacks != null &&
                            !callbacks.afterUpdate(bean, result != -1)) {
                            is_cancelled[0] = true;
                            return result;
                        }
                    }

                    // perform insert if update failed or wasn't appropriate
                    if (-1 == result) {
                        // handle before callback
                        if (callbacks != null &&
                            !callbacks.beforeInsert(bean)) {
                            is_cancelled[0] = true;
                            return -1;
                        }

                        result = _insertWithoutCallbacks(nextId, save, bean);

                        // handle after callback
                        if (callbacks != null &&
                            !callbacks.afterInsert(bean, result != -1)) {
                            is_cancelled[0] = true;
                            return result;
                        }
                    }
                }

                return result;
            }
        });

        // handle after callback
        if (!is_cancelled[0] &&
            callbacks != null) {
            callbacks.afterSave(bean, value != -1);
        }

        return value;
    }

    protected boolean _delete(Delete delete)
    throws DatabaseException {
        assert delete != null;

        var result = true;

        if (0 == executeUpdate(delete)) {
            result = false;
        }

        return result;
    }

    protected boolean _delete(final Delete delete, final int objectId)
    throws DatabaseException {
        assert delete != null;

        // handle before callback
        Callbacks<BeanType> callbacks = null;
        if (CallbacksProvider.class.isAssignableFrom(baseClass_)) {
            try {
                callbacks = ((CallbacksProvider<BeanType>) baseClass_.getDeclaredConstructor().newInstance()).getCallbacks();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                callbacks = null;
            }
        } else if (Callbacks.class.isAssignableFrom(baseClass_)) {
            try {
                callbacks = (Callbacks<BeanType>) baseClass_.getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                callbacks = null;
            }
        }
        if (callbacks != null &&
            !callbacks.beforeDelete(objectId)) {
            return false;
        }

        // perform delete
        Boolean result = inTransaction(new DbTransactionUser<>() {
            public Boolean useTransaction()
            throws InnerClassException {
                // remove all many-to-one mappings for this object ID
                deleteManyToOne(objectId);

                // remove all many-to-many mappings for this object ID
                deleteManyToMany(objectId);

                // perform the actual deletion of the object from the database
                return 0 != executeUpdate(delete, s -> s.setInt(primaryKey_, objectId));
            }
        });

        // handle listeners
        if (result) {
            fireDeleted(objectId);
        }

        // handle after callback
        if (callbacks != null) {
            callbacks.afterDelete(objectId, result);
        }

        return result;
    }

    protected void deleteManyToOne(final int objectId) {
        final var constrained = ConstrainedUtils.getConstrainedInstance(getBaseClass());
        final var declarations = obtainManyToOneAssociationDeclarations(this, constrained);
        if (declarations != null) {
            // iterate over all the many-to-one association relationships
            for (var entry : declarations.entrySet()) {
                var declaration = entry.getValue();
                var column_name = generateManyToOneJoinColumnName(declaration.getMainProperty(), declaration.getMainDeclaration());
                var main_manager = createNewManager(declaration.getMainType());

                // create an update statement that will set all the columns that
                // point to the deleted entity to NULL
                var clear_references = new Update(getDatasource())
                    .table(main_manager.getTable())
                    .fieldCustom(column_name, "NULL")
                    .where(column_name, "=", objectId);
                executeUpdate(clear_references);
            }
        }
    }

    protected void deleteManyToMany(final int objectId) {
        final var constrained = ConstrainedUtils.getConstrainedInstance(getBaseClass());
        final var declarations = obtainManyToManyDeclarations(this, constrained, true);
        if (declarations != null) {
            // iterate over all the many-to-many relationships
            final var column1_name = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
            for (var entry : declarations.entrySet()) {
                var declaration = entry.getValue();
                var association_manager = createNewManager(declaration.getAssociationType());
                var join_table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);

                // create the delete statement to remove all the possible previous
                // mappings in the join table for this primary key ID
                var delete_previous_mappings = new Delete(getDatasource())
                    .from(join_table)
                    .where(column1_name, "=", objectId);
                executeUpdate(delete_previous_mappings);
            }
        }
    }

    private boolean processFetchedBean(BeanType instance) {
        // handle listeners
        if (instance != null) {
            restoreManyToOne(instance);
            var identifier_value = getIdentifierValue(instance);
            restoreManyToOneAssociations(instance, identifier_value);
            restoreManyToMany(instance, identifier_value);

            fireRestored(instance);
        }

        // handle after callback
        var callbacks = getCallbacks(instance);
        return !(callbacks != null && !callbacks.afterRestore(instance));
    }

    protected BeanType _restore(Select restore, final int objectId)
    throws DatabaseException {
        assert restore != null;

        BeanType result = null;

        result = executeFetchFirstBean(restore, baseClass_, s -> s.setInt(primaryKey_, objectId));
        if (!processFetchedBean(result)) {
            return null;
        }

        return result;
    }

    protected BeanType _restoreFirst(Select restore)
    throws DatabaseException {
        assert restore != null;

        var result = executeFetchFirstBean(restore, baseClass_);
        if (!processFetchedBean(result)) {
            return null;
        }

        return result;
    }

    protected List<BeanType> _restore(Select restore)
    throws DatabaseException {
        assert restore != null;

        var bean_fetcher = new DbBeanFetcher<>(getDatasource(), baseClass_, true) {
            public boolean gotBeanInstance(BeanType instance) {
                return processFetchedBean(instance);
            }
        };

        executeFetchAll(restore, bean_fetcher, null);

        return bean_fetcher.getCollectedInstances();
    }

    protected boolean _restore(Select restore, DbRowProcessor rowProcessor)
    throws DatabaseException {
        assert restore != null;

        return executeFetchAll(restore, rowProcessor);
    }

    protected boolean _restore(Select restore, BeanFetcher<BeanType> beanFetcher)
    throws DatabaseException {
        assert restore != null;

        var bean_fetcher = new DbBeanFetcher<>(getDatasource(), baseClass_, true) {
            public boolean gotBeanInstance(BeanType instance) {
                var result = processFetchedBean(instance);
                beanFetcher.gotBeanInstance(instance);
                return result;
            }
        };

        return executeFetchAll(restore, bean_fetcher);
    }


    protected void restoreManyToMany(final BeanType bean, final int objectId) {
        // handle many-to-many associations
        final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        final var declarations = obtainManyToManyDeclarations(this, constrained, true);
        if (declarations != null) {
            // iterate over all the many-to-many relationships
            final var column1_name = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
            for (var entry : declarations.entrySet()) {
                var declaration = entry.getValue();

                // create the associations collection
                Object association_collection;
                if (Set.class == declaration.getCollectionType()) {
                    association_collection = new ManyToManySet<>(AbstractGenericQueryManager.this, column1_name, objectId, declaration);
                } else {
                    association_collection = new ManyToManyList<>(AbstractGenericQueryManager.this, column1_name, objectId, declaration);
                }

                // set the restore mappings as the property value
                try {
                    BeanUtils.setPropertyValue(bean, entry.getKey(), association_collection);
                } catch (BeanUtilsException e) {
                    throw new DatabaseException(e);
                }
            }
        }
    }

    protected void restoreManyToOne(final BeanType bean) {
        Field gqm_field = null;
        Field lazyloaded_field = null;
        try {
            gqm_field = bean.getClass().getDeclaredField(LazyLoadAccessorsBytecodeTransformer.GQM_VAR_NAME);
            lazyloaded_field = bean.getClass().getDeclaredField(LazyLoadAccessorsBytecodeTransformer.LAZY_LOADED_VAR_NAME);
        } catch (Exception e) {
            // if the synthetic fields don't exist in the class, just set them to null
            // since this means that bytecode enhancement hasn't been performed to provide
            // lazy-load functionalities
            gqm_field = null;
            lazyloaded_field = null;
        }

        // if the class has been enhanced for lazy loading capabilities, add a reference
        // to this GQM and create a new map for storing the already loaded property values
        if (gqm_field != null &&
            lazyloaded_field != null) {
            gqm_field.setAccessible(true);
            lazyloaded_field.setAccessible(true);
            try {
                gqm_field.set(bean, this);
                lazyloaded_field.set(bean, new WeakHashMap<>());
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
        // otherwise eagerly load all many-to-one properties
        else {
            processManyToOneJoinColumns(this, (columnName, propertyName, declaration) -> {
                var property_value = restoreManyToOneProperty(AbstractGenericQueryManager.this, bean, declaration.getAssociationManager(), columnName, declaration.getAssociationType());

                // set the many-to-one mapping as the property value
                try {
                    BeanUtils.setPropertyValue(bean, propertyName, property_value);
                } catch (BeanUtilsException e) {
                    throw new DatabaseException(e);
                }

                return true;
            });
        }
    }

    protected void restoreManyToOneAssociations(final BeanType bean, final int objectId) {
        // handle many-to-one associations
        final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);
        final var declarations = obtainManyToOneAssociationDeclarations(this, constrained);
        if (declarations != null) {
            // iterate over all the many-to-one association relationships
            for (var entry : declarations.entrySet()) {
                var declaration = entry.getValue();

                // create the associations collection
                Object association_collection;
                if (Set.class == declaration.getCollectionType()) {
                    association_collection = new ManyToOneAssociationSet(AbstractGenericQueryManager.this, objectId, declaration);
                } else {
                    association_collection = new ManyToOneAssociationList(AbstractGenericQueryManager.this, objectId, declaration);
                }

                // set the restore mappings as the property value
                try {
                    BeanUtils.setPropertyValue(bean, entry.getKey(), association_collection);
                } catch (BeanUtilsException e) {
                    throw new DatabaseException(e);
                }
            }
        }
    }

    protected int _count(Select count)
    throws DatabaseException {
        return executeGetFirstInt(count);
    }

    protected void install_(final CreateSequence createSequence, final CreateTable createTable)
    throws DatabaseException {
        assert createSequence != null;
        assert createTable != null;

        inTransaction(() -> {
            if (!isIdentifierSparse()) {
                executeUpdate(createSequence);
            }
            executeUpdate(createTable);

            installManyToMany();
        });
        fireInstalled();
    }

    protected void installManyToMany() {
        // create many-to-many join tables
        var constrained = ConstrainedUtils.getConstrainedInstance(baseClass_);
        var manytomany_declarations = obtainManyToManyDeclarations(this, constrained, false);
        if (manytomany_declarations != null) {
            var column1 = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
            for (var entry : manytomany_declarations.entrySet()) {
                var declaration = entry.getValue();
                var association_manager = createNewManager(declaration.getAssociationType());
                var table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);
                var column2 = generateManyToManyJoinColumnName(association_manager);

                // obtain the violation actions
                CreateTable.ViolationAction onupdate = null;
                CreateTable.ViolationAction ondelete = null;
                var property = constrained.getConstrainedProperty(entry.getKey());
                if (property != null &&
                    property.hasManyToMany()) {
                    onupdate = property.getManyToMany().getOnUpdate();
                    ondelete = property.getManyToMany().getOnDelete();
                }

                // build the table creation query
                var create_join_table = new CreateTable(getDatasource())
                    .table(table)
                    .column(column1, int.class, CreateTable.NOTNULL)
                    .column(column2, int.class, CreateTable.NOTNULL)
                    .foreignKey(getTable(), column1, getIdentifierName(), onupdate, ondelete)
                    .foreignKey(association_manager.getTable(), column2, association_manager.getIdentifierName(), onupdate, ondelete);
                executeUpdate(create_join_table);
            }
        }
    }

    protected void remove_(final DropSequence dropSequence, final DropTable dropTable)
    throws DatabaseException {
        assert dropTable != null;
        assert dropSequence != null;

        inTransaction(() -> {
            removeManyToMany();

            // drop the table itself and optionally the sequence
            executeUpdate(dropTable);
            if (!isIdentifierSparse()) {
                executeUpdate(dropSequence);
            }
        });
        fireRemoved();
    }

    protected void removeManyToMany() {
        // drop many-to-many join tables
        var constrained = ConstrainedUtils.getConstrainedInstance(baseClass_);
        var manytomany_declarations = obtainManyToManyDeclarations(this, constrained, false);
        if (manytomany_declarations != null) {
            for (var entry : manytomany_declarations.entrySet()) {
                var declaration = entry.getValue();
                var association_manager = createNewManager(declaration.getAssociationType());
                var table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);

                // build the table removal query
                var drop_join_table = new DropTable(getDatasource())
                    .table(table);
                executeUpdate(drop_join_table);
            }
        }
    }

    public void addListener(GenericQueryManagerListener<BeanType> listener) {
        if (null == listener) {
            return;
        }

        if (null == listeners_) {
            listeners_ = new ArrayList<>();
        }

        listeners_.add(listener);
    }

    public void removeListeners() {
        if (null == listeners_) {
            return;
        }

        listeners_.clear();
    }

    public <OtherBeanType> GenericQueryManager<OtherBeanType> createNewManager(Class<OtherBeanType> beanClass) {
        return GenericQueryManagerFactory.instance(getDatasource(), beanClass);
    }

    protected void fireInstalled() {
        if (null == listeners_) {
            return;
        }

        for (var listener : listeners_) {
            listener.installed();
        }
    }

    protected void fireRemoved() {
        if (null == listeners_) {
            return;
        }

        for (var listener : listeners_) {
            listener.removed();
        }
    }

    protected void fireInserted(BeanType bean) {
        if (null == listeners_) {
            return;
        }

        for (var listener : listeners_) {
            listener.inserted(bean);
        }
    }

    protected void fireUpdated(BeanType bean) {
        if (null == listeners_) {
            return;
        }

        for (var listener : listeners_) {
            listener.updated(bean);
        }
    }

    protected void fireRestored(BeanType bean) {
        if (null == listeners_) {
            return;
        }

        for (var listener : listeners_) {
            listener.restored(bean);
        }
    }

    protected void fireDeleted(int objectId) {
        if (null == listeners_) {
            return;
        }

        for (var listener : listeners_) {
            listener.deleted(objectId);
        }
    }
}

