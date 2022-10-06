/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com> and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.*;
import rife.database.queries.*;

import java.util.*;

import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.ExecutionErrorException;
import rife.tools.BeanUtils;
import rife.tools.InnerClassException;
import rife.tools.StringUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.lang.reflect.Method;

import static rife.database.querymanagers.generic.GenericQueryManagerRelationalUtils.processManyToOneJoinColumns;

public abstract class AbstractGenericQueryManager<BeanType> extends DbQueryManager implements GenericQueryManager<BeanType> {
    protected Class<BeanType> baseClass_ = null;
    protected String primaryKey_ = null;
    protected Method getPrimaryKeyMethod_ = null;
    protected Method setPrimaryKeyMethod_ = null;
    protected boolean sparseIdentifier_ = false;

    protected List<GenericQueryManagerListener> mListeners = null;

    public AbstractGenericQueryManager(Datasource datasource, Class<BeanType> beanClass, String primaryKey)
    throws DatabaseException {
        super(datasource);

        baseClass_ = beanClass;
        primaryKey_ = primaryKey;
        try {
            String capitalized_primary_key = StringUtils.capitalize(primaryKey_);
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

        // TODO
//		Constrained constrained_bean = ConstrainedUtils.getConstrainedInstance(getBaseClass());
//		if (constrained_bean != null)
//		{
//			ConstrainedProperty constrained_property = constrained_bean.getConstrainedProperty(primaryKey);
//			if (constrained_property != null)
//			{
//				mSparseIdentifier = constrained_property.isSparse();
//			}
//		}
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
            Integer id = (Integer) getPrimaryKeyMethod_.invoke(bean, (Object[]) null);
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

    // TODO
//	public void validate(Validated validated)
//	{
//		// perform validation
//		if (validated != null &&
//			!(validated.getClass() == mBaseClass))
//		{
//			throw new IncompatibleValidationTypeException(validated.getClass(), mBaseClass);
//		}
//
//		BeanType bean = (BeanType)validated;
//
//		// handle before callback
//		Callbacks callbacks = getCallbacks(bean);
//		if (callbacks != null &&
//			!callbacks.beforeValidate(bean))
//		{
//			return;
//		}
//		_validateWithoutCallbacks(validated);
//
//		// handle after callback
//		if (callbacks != null)
//		{
//			callbacks.afterValidate(bean);
//		}
//	}

    private static int getIdentifierValue(Object bean, String propertyName)
    throws DatabaseException {
        try {
            Integer id = (Integer) BeanUtils.getPropertyValue(bean, propertyName);
            if (null == id) {
                return -1;
            }

            return id;
        } catch (Throwable e) {
            throw new DatabaseException(e);
        }
    }

    // TODO
//	protected void _validateWithoutCallbacks(final Validated validated)
//	{
//		if (null == validated)
//		{
//			return;
//		}
//
//		// handle constrained beans
//		final Constrained constrained = ConstrainedUtils.makeConstrainedInstance(validated);
//
//		if (constrained != null)
//		{
//			// check if the identifier exists or is still undefined (existing or
//			// new entry)
//			boolean identifier_exists = false;
//			int	identifier_value = getIdentifierValue((BeanType)validated);
//			if (identifier_value >= 0)
//			{
//				identifier_exists = true;
//			}
//
//			// validate the many-to-one entities
//			processManyToOneJoinColumns(this, new ManyToOneJoinColumnProcessor() {
//					public boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration)
//					{
//						Object property_value = null;
//						try
//						{
//							property_value = BeanUtils.getPropertyValue(constrained, propertyName);
//						}
//						catch (BeanUtilsException e)
//						{
//							throw new DatabaseException(e);
//						}
//
//						if (property_value != null)
//						{
//							int identifier_value = getIdentifierValue(property_value, declaration.getAssociationColumn());
//							if (identifier_value >= 0)
//							{
//								if (!executeHasResultRows(declaration.getAssociationManager().getRestoreQuery(identifier_value)))
//								{
//									validated.addValidationError(new ValidationError.INVALID(propertyName));
//								}
//							}
//						}
//
//						return true;
//					}
//				});
//
//			Map<String, ManyToOneAssociationDeclaration> manytoone_association_declarations = null;
//			Map<String, Object> manytoone_association_property_values = null;
//			boolean obtained_manytoone_association_declarations = false;
//
//			Map<String, ManyToManyDeclaration> manytomany_declarations = null;
//			Map<String, Object> manytomany_property_values = null;
//			boolean obtained_manytomany_declarations = false;
//
//			// handle invididual properties
//			for (ConstrainedProperty property : (Collection<ConstrainedProperty>)constrained.getConstrainedProperties())
//			{
//				// handle the uniqueness of invididual properties
//				if (property.isUnique())
//				{
//					Object property_value = null;
//					try
//					{
//						property_value = BeanUtils.getPropertyValue(constrained, property.getPropertyName());
//					}
//					catch (BeanUtilsException e)
//					{
//						throw new DatabaseException(e);
//					}
//
//					if (property_value != null)
//					{
//						CountQuery count_query = getCountQuery()
//							.where(property.getPropertyName(), "=", property_value);
//						if (identifier_exists)
//						{
//							count_query.whereAnd(mPrimaryKey, "!=", identifier_value);
//						}
//
//						if (count(count_query) > 0)
//						{
//							validated.addValidationError(new ValidationError.UNICITY(property.getPropertyName()));
//						}
//					}
//				}
//
//				// handle the manyToOne constraint that contain the identifier values
//				if (property.hasManyToOne())
//				{
//					Object property_value = null;
//					try
//					{
//						property_value = BeanUtils.getPropertyValue(constrained, property.getPropertyName());
//					}
//					catch (BeanUtilsException e)
//					{
//						throw new DatabaseException(e);
//					}
//
//					if (property_value != null &&
//						ClassUtils.isBasic(property_value.getClass()))
//					{
//						ConstrainedProperty.ManyToOne many_to_one = property.getManyToOne();
//
//						if (null == many_to_one.getDerivedTable())
//						{
//							throw new MissingManyToOneTableException(constrained.getClass(), property.getPropertyName());
//						}
//
//						if (null == many_to_one.getColumn())
//						{
//							throw new MissingManyToOneColumnException(constrained.getClass(), property.getPropertyName());
//						}
//
//						if (!executeHasResultRows(new Select(getDatasource())
//												  .from(many_to_one.getDerivedTable())
//												  .where(many_to_one.getColumn(), "=", property_value)))
//						{
//							validated.addValidationError(new ValidationError.INVALID(property.getPropertyName()));
//						}
//					}
//				}
//
//				// handle the manyToOneAssociation constraint
//				if (property.hasManyToOneAssociation())
//				{
//					// make sure that the many to one association declarations have been obtained
//					if (!obtained_manytoone_association_declarations)
//					{
//						manytoone_association_declarations = obtainManyToOneAssociationDeclarations(this, constrained);
//
//						if (manytoone_association_declarations != null)
//						{
//							// get the property values of those that contain many-to-one association relationships
//							String[] manytoone_association_property_names = new String[manytoone_association_declarations.size()];
//							manytoone_association_declarations.keySet().toArray(manytoone_association_property_names);
//							try
//							{
//								manytoone_association_property_values = BeanUtils.getPropertyValues(constrained, manytoone_association_property_names, null, null);
//							}
//							catch (BeanUtilsException e)
//							{
//								throw new DatabaseException(e);
//							}
//						}
//
//						obtained_manytoone_association_declarations = true;
//					}
//
//					ManyToOneAssociationDeclaration declaration = manytoone_association_declarations.get(property.getPropertyName());
//					if (declaration != null)
//					{
//						Object value = manytoone_association_property_values.get(property.getPropertyName());
//						Class type = declaration.getMainType();
//						try
//						{
//							checkCollectionRelationshipValidity(validated, property, value, type);
//						}
//						catch (ClassCastException e)
//						{
//							throw new UnsupportedManyToManyValueTypeException(validated.getClass(), property.getName(), e);
//						}
//					}
//				}
//
//				// handle the manyToMany constraint
//				if (property.hasManyToMany() ||
//					property.hasManyToManyAssociation())
//				{
//					// make sure that the many to many declarations have been obtained
//					if (!obtained_manytomany_declarations)
//					{
//						manytomany_declarations = obtainManyToManyDeclarations(this, constrained, true);
//
//						if (manytomany_declarations != null)
//						{
//							// get the property values of those that contain many-to-many relationships
//							String[] manytomany_property_names = new String[manytomany_declarations.size()];
//							manytomany_declarations.keySet().toArray(manytomany_property_names);
//							try
//							{
//								manytomany_property_values = BeanUtils.getPropertyValues(constrained, manytomany_property_names, null, null);
//							}
//							catch (BeanUtilsException e)
//							{
//								throw new DatabaseException(e);
//							}
//						}
//
//						obtained_manytomany_declarations = true;
//					}
//
//					ManyToManyDeclaration declaration = manytomany_declarations.get(property.getPropertyName());
//					if (declaration != null)
//					{
//						Object value = manytomany_property_values.get(property.getPropertyName());
//						try
//						{
//							checkCollectionRelationshipValidity(validated, property, value, declaration.getAssociationType());
//						}
//						catch (ClassCastException e)
//						{
//							throw new UnsupportedManyToManyValueTypeException(validated.getClass(), property.getName(), e);
//						}
//					}
//
//				}
//			}
//
//			// handle the bean-wide uniqueness
//			ConstrainedBean constrained_bean = constrained.getConstrainedBean();
//			if (constrained_bean != null &&
//				constrained_bean.hasUniques())
//			{
//				for (String[] uniques : (List<String[]>)constrained_bean.getUniques())
//				{
//					CountQuery count_query = getCountQuery();
//					if (identifier_exists)
//					{
//						count_query.where(mPrimaryKey, "!=", identifier_value);
//					}
//
//					for (String unique : uniques)
//					{
//						Object property_value = null;
//						try
//						{
//							property_value = BeanUtils.getPropertyValue(constrained, unique);
//						}
//						catch (BeanUtilsException e)
//						{
//							throw new DatabaseException(e);
//						}
//
//						if (null == property_value)
//						{
//							count_query = null;
//							break;
//						}
//						else
//						{
//							count_query.where(unique, "=", property_value);
//						}
//					}
//
//					if (count_query != null &&
//						count(count_query) > 0)
//					{
//						for (String unique : uniques)
//						{
//							validated.addValidationError(new ValidationError.UNICITY(unique));
//						}
//					}
//				}
//			}
//		}
//	}
//
//	private void checkCollectionRelationshipValidity(final Validated validated, final ConstrainedProperty property, final Object propertyValue, final Class elementType)
//	throws ClassCastException
//	{
//		if (elementType != null &&
//			propertyValue != null)
//		{
//			// cast the property value to a collection
//			Collection value_collection = (Collection)propertyValue;
//
//			// iterate over all the collection elements to obtain the identifier values
//			Set<Integer> identifiers = new HashSet<Integer>();
//			GenericQueryManager element_manager = createNewManager(elementType);
//			for (Object entity : value_collection)
//			{
//				int identifier_value = element_manager.getIdentifierValue(entity);
//				// only add the identifiers that have a value
//				if (identifier_value != -1)
//				{
//					identifiers.add(identifier_value);
//				}
//			}
//
//			// check if the many-to-one associations exist
//			CountQuery count_query = element_manager.getCountQuery()
//				.where(element_manager.getIdentifierName() + " IN (" + StringUtils.join(identifiers, ",") + ")");
//			int count = element_manager.count(count_query);
//			if (count != identifiers.size())
//			{
//				validated.addValidationError(new ValidationError.INVALID(property.getPropertyName()));
//			}
//		}
//	}

    protected Callbacks getCallbacks(BeanType bean) {
        if (null == bean) return null;

        Callbacks callbacks = null;
        if (bean instanceof CallbacksProvider) {
            callbacks = ((CallbacksProvider) bean).getCallbacks();
        } else if (bean instanceof Callbacks) {
            callbacks = (Callbacks) bean;
        }
        return callbacks;
    }

    protected int _update(final Update saveUpdate, final BeanType bean) {
        // handle before callback
        Callbacks callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeUpdate(bean)) {
            return -1;
        }

        // perform update
        int result = _updateWithoutCallbacks(saveUpdate, bean);

        // handle after callback
        if (callbacks != null) {
            callbacks.afterUpdate(bean, result != -1);
        }

        return result;
    }

    protected int _updateWithoutCallbacks(final Update saveUpdate, final BeanType bean) {
        assert saveUpdate != null;

        final int identifier_value = getIdentifierValue(bean);
        int result = (Integer) inTransaction(new DbTransactionUser() {
            public Integer useTransaction()
            throws InnerClassException {
                int result = identifier_value;

                storeManyToOne(bean);

                if (0 == executeUpdate(saveUpdate, new DbPreparedStatementHandler() {
                    public void setParameters(final DbPreparedStatement statement) {
                        statement
                            .setBean(bean);

                        setManyToOneJoinParameters(statement, bean);
                    }
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
        Callbacks callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeInsert(bean)) {
            return -1;
        }

        // perform insert
        int result = _insertWithoutCallbacks(nextId, save, bean);

        // handle after callback
        if (callbacks != null) {
            callbacks.afterInsert(bean, result != -1);
        }

        return result;
    }

    protected int _insertWithoutCallbacks(final SequenceValue nextId, final Insert save, final BeanType bean) {
        assert nextId != null;
        assert save != null;

        int value = -1;

        value = (Integer) inTransaction(new DbTransactionUser() {
            public Integer useTransaction()
            throws InnerClassException {
                storeManyToOne(bean);

                int result = getIdentifierValue(bean);
                if (!isIdentifierSparse()) {
                    result = executeGetFirstInt(nextId);
                }

                final int primary_key_id = result;
                executeUpdate(save, new DbPreparedStatementHandler() {
                    public void setParameters(final DbPreparedStatement statement) {
                        statement
                            .setBean(bean)
                            .setInt(primaryKey_, primary_key_id);

                        setManyToOneJoinParameters(statement, bean);
                    }
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
        // TODO
//		final Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);

        // handle many-to-one join column parameters
        processManyToOneJoinColumns(this, new ManyToOneJoinColumnProcessor() {
            public boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration) {
                try {
                    Object join_column_property = BeanUtils.getPropertyValue(bean, propertyName);
                    Object identifier_value = null;
                    if (join_column_property != null) {
                        identifier_value = BeanUtils.getPropertyValue(join_column_property, declaration.getAssociationColumn());
                    }
                    Class identifier_type = BeanUtils.getPropertyType(declaration.getAssociationType(), declaration.getAssociationColumn());

                    int[] indices = statement.getParameterIndices(columnName);
                    for (int index : indices) {
                        getDatasource().getSqlConversion().setTypedParameter(statement, index, identifier_type, columnName, identifier_value/*, constrained*/);
                    }
                } catch (BeanUtilsException e) {
                    throw new DatabaseException(e);
                }

                return true;
            }
        });
    }

    protected void storeManyToOne(final BeanType bean) {
        // TODO
//		final Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
//		final Map<String, ManyToOneDeclaration> declarations = obtainManyToOneDeclarations(this, constrained, null, null);
//		if (declarations != null)
//		{
//			// get the property values of those that contain many-to-one relationships
//			String[] property_names = new String[declarations.size()];
//			declarations.keySet().toArray(property_names);
//			final Map<String, Object> values;
//			try
//			{
//				values = BeanUtils.getPropertyValues(bean, property_names, null, null);
//			}
//			catch (BeanUtilsException e)
//			{
//				throw new DatabaseException(e);
//			}
//
//			// iterate over all the many-to-one relationships that have associated classes and instance values
//			for (Map.Entry<String, ManyToOneDeclaration> entry : declarations.entrySet())
//			{
//				ManyToOneDeclaration declaration = entry.getValue();
//				if (!declaration.isBasic())
//				{
//					GenericQueryManager association_manager = declaration.getAssociationManager();
//					String property_name = entry.getKey();
//
//					// obtain the property value
//					Object value = values.get(property_name);
//					if (value != null)
//					{
//						int identifier_value = association_manager.getIdentifierValue(value);
//						// insert the collection entries that have no identifier value
//						if (identifier_value < 0)
//						{
//							identifier_value = association_manager.insert(value);
//						}
//					}
//				}
//			}
//		}
    }

    protected void storeManyToOneAssociations(final BeanType bean, final int objectId) {
        // TODO
//		final Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
//		final Map<String, ManyToOneAssociationDeclaration> declarations = obtainManyToOneAssociationDeclarations(this, constrained);
//		if (declarations != null)
//		{
//			// get the property values of those that contain many-to-one relationships
//			String[] property_names = new String[declarations.size()];
//			declarations.keySet().toArray(property_names);
//			final Map<String, Object> values;
//			try
//			{
//				values = BeanUtils.getPropertyValues(bean, property_names, null, null);
//			}
//			catch (BeanUtilsException e)
//			{
//				throw new DatabaseException(e);
//			}
//
//			// iterate over all the many-to-one relationships that have associated classes and instance values
//			for (Map.Entry<String, ManyToOneAssociationDeclaration> entry : declarations.entrySet())
//			{
//				ManyToOneAssociationDeclaration declaration = entry.getValue();
//				GenericQueryManager main_manager = createNewManager(declaration.getMainType());
//				String property_name = entry.getKey();
//
//				// obtain the property value
//				Object value = values.get(property_name);
//
//				final String main_table = main_manager.getTable();
//				final String main_identify_column = main_manager.getIdentifierName();
//				final String main_join_column = generateManyToOneJoinColumnName(declaration.getMainProperty(), declaration.getMainDeclaration());
//
//				Update clear_previous_mappings = new Update(getDatasource())
//					.table(main_table)
//					.fieldCustom(main_join_column, "NULL")
//					.where(main_join_column, "=", objectId);
//				executeUpdate(clear_previous_mappings);
//
//				if (value != null)
//				{
//					ensureSupportedManyToOneAssociationPropertyValueType(mBaseClass, property_name, value);
//
//					Collection value_collection = (Collection)value;
//
//					Update update_mapping = new Update(getDatasource())
//						.table(main_table)
//						.fieldParameter(main_join_column)
//						.whereParameter(main_identify_column, "=");
//
//					// store the collection entries
//					for (Object many_to_one_entity : value_collection)
//					{
//						int identifier_value = main_manager.getIdentifierValue(many_to_one_entity);
//						// insert the collection entries that have no identifier value
//						if (identifier_value < 0)
//						{
//							identifier_value = main_manager.insert(many_to_one_entity);
//						}
//
//						// store the many-to-one mappings
//						executeUpdate(update_mapping, new DbPreparedStatementHandler<Integer>(identifier_value) {
//								public void setParameters(DbPreparedStatement statement)
//								{
//									statement
//										.setInt(main_join_column, objectId)
//										.setInt(main_identify_column, getData());
//								}
//							});
//					}
//				}
//			}
//		}
    }

    protected void storeManyToMany(final BeanType bean, final int objectId) {
        // TODO
//		final Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
//		final Map<String, ManyToManyDeclaration> declarations = obtainManyToManyDeclarations(this, constrained, true);
//		if (declarations != null)
//		{
//			// get the property values of those that contain many-to-many relationships
//			String[] property_names = new String[declarations.size()];
//			declarations.keySet().toArray(property_names);
//			final Map<String, Object> values;
//			try
//			{
//				values = BeanUtils.getPropertyValues(bean, property_names, null, null);
//			}
//			catch (BeanUtilsException e)
//			{
//				throw new DatabaseException(e);
//			}
//
//			// iterate over all the many to many relationships
//			final String column1_name = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
//			for (Map.Entry<String, ManyToManyDeclaration> entry : declarations.entrySet())
//			{
//				ManyToManyDeclaration declaration = entry.getValue();
//				GenericQueryManager association_manager = createNewManager(declaration.getAssociationType());
//				String join_table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);
//				final String column2_name = generateManyToManyJoinColumnName(association_manager);
//				String property_name = entry.getKey();
//
//				// obtain the property value
//				Object value = values.get(property_name);
//
//				// create the delete statement to remove all the possible previous
//				// mappings in the join table for this primary key ID
//				Delete delete_previous_mappings = new Delete(getDatasource())
//					.from(join_table)
//					.where(column1_name, "=", objectId);
//				executeUpdate(delete_previous_mappings);
//
//				if (value != null)
//				{
//					ensureSupportedManyToManyPropertyValueType(mBaseClass, property_name, value);
//
//					Collection value_collection = (Collection)value;
//
//					Insert insert_mapping = new Insert(getDatasource())
//						.into(join_table)
//						.fieldParameter(column1_name)
//						.fieldParameter(column2_name);
//					// store the collection entries
//					for (Object many_to_many_entity : value_collection)
//					{
//						int identifier_value = association_manager.getIdentifierValue(many_to_many_entity);
//						// insert the collection entries that have no identifier value
//						if (identifier_value < 0)
//						{
//							identifier_value = association_manager.insert(many_to_many_entity);
//						}
//						// store the many-to_many mappings
//						executeUpdate(insert_mapping, new DbPreparedStatementHandler<Integer>(identifier_value) {
//								public void setParameters(DbPreparedStatement statement)
//								{
//									statement
//										.setInt(column1_name, objectId)
//										.setInt(column2_name, getData());
//								}
//							});
//					}
//				}
//			}
//		}
    }

    protected int _save(final SequenceValue nextId, final Insert save, final Update saveUpdate, final BeanType bean)
    throws DatabaseException {
        assert nextId != null;
        assert save != null;
        assert saveUpdate != null;

        int value = -1;

        // handle before callback
        final Callbacks callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeSave(bean)) {
            return -1;
        }

        // cancel indicator
        final boolean[] is_cancelled = new boolean[]{false};

        // perform save
        value = (Integer) inTransaction(new DbTransactionUser() {
            public Integer useTransaction()
            throws InnerClassException {
                int result = getIdentifierValue(bean);
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

        boolean result = true;

        if (0 == executeUpdate(delete)) {
            result = false;
        }

        return result;
    }

    protected boolean _delete(final Delete delete, final int objectId)
    throws DatabaseException {
        assert delete != null;

        // handle before callback
        Callbacks callbacks = null;
        if (CallbacksProvider.class.isAssignableFrom(baseClass_)) {
            try {
                callbacks = ((CallbacksProvider) baseClass_.newInstance()).getCallbacks();
            } catch (IllegalAccessException | InstantiationException e) {
                callbacks = null;
            }
        } else if (Callbacks.class.isAssignableFrom(baseClass_)) {
            try {
                callbacks = (Callbacks) baseClass_.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                callbacks = null;
            }
        }
        if (callbacks != null &&
            !callbacks.beforeDelete(objectId)) {
            return false;
        }

        // perform delete
        Boolean result = inTransaction(new DbTransactionUser() {
            public Boolean useTransaction()
            throws InnerClassException {
                // remove all many-to-one mappings for this object ID
                deleteManyToOne(objectId);

                // remove all many-to-many mappings for this object ID
                deleteManyToMany(objectId);

                // perform the actual deletion of the object from the database
                if (0 == executeUpdate(delete, new DbPreparedStatementHandler() {
                    public void setParameters(DbPreparedStatement statement) {
                        statement
                            .setInt(primaryKey_, objectId);
                    }
                })) {
                    return false;
                }

                return true;
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
        // TODO
//		final Constrained constrained = ConstrainedUtils.getConstrainedInstance(getBaseClass());
//		final Map<String, ManyToOneAssociationDeclaration> declarations = obtainManyToOneAssociationDeclarations(this, constrained);
//		if (declarations != null)
//		{
//			// iterate over all the many to one assocation relationships
//			for (Map.Entry<String, ManyToOneAssociationDeclaration> entry : declarations.entrySet())
//			{
//				ManyToOneAssociationDeclaration declaration = entry.getValue();
//				String column_name = generateManyToOneJoinColumnName(declaration.getMainProperty(), declaration.getMainDeclaration());
//				GenericQueryManager main_manager = createNewManager(declaration.getMainType());
//
//				// create an update statement that will set all the columns that
//				// point to the deleted entity to NULL
//				Update clear_references = new Update(getDatasource())
//					.table(main_manager.getTable())
//					.fieldCustom(column_name, "NULL")
//					.where(column_name, "=", objectId);
//				executeUpdate(clear_references);
//			}
//		}
    }

    protected void deleteManyToMany(final int objectId) {
        // TODO
//		final Constrained constrained = ConstrainedUtils.getConstrainedInstance(getBaseClass());
//		final Map<String, ManyToManyDeclaration> declarations = obtainManyToManyDeclarations(this, constrained, true);
//		if (declarations != null)
//		{
//			// iterate over all the many to many relationships
//			final String column1_name = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
//			for (Map.Entry<String, ManyToManyDeclaration> entry : declarations.entrySet())
//			{
//				ManyToManyDeclaration declaration = entry.getValue();
//				GenericQueryManager association_manager = createNewManager(declaration.getAssociationType());
//				String join_table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);
//
//				// create the delete statement to remove all the possible previous
//				// mappings in the join table for this primary key ID
//				Delete delete_previous_mappings = new Delete(getDatasource())
//					.from(join_table)
//					.where(column1_name, "=", objectId);
//				executeUpdate(delete_previous_mappings);
//			}
//		}
    }

    protected BeanType _restore(Select restore, final int objectId)
    throws DatabaseException {
        assert restore != null;

        BeanType result = null;

        result = executeFetchFirstBean(restore, baseClass_, new DbPreparedStatementHandler() {
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setInt(primaryKey_, objectId);
            }
        });

        // handle listeners
        if (result != null) {
            restoreManyToOne(result);
            restoreManyToOneAssociations(result, objectId);
            restoreManyToMany(result, objectId);

            fireRestored(result);
        }

        // handle after callback
        Callbacks callbacks = getCallbacks(result);
        if (callbacks != null &&
            !callbacks.afterRestore(result)) {
            return null;
        }

        return result;
    }

    protected BeanType _restoreFirst(Select restore)
    throws DatabaseException {
        assert restore != null;

        BeanType result = executeFetchFirstBean(restore, baseClass_);

        // handle listeners
        if (result != null) {
            restoreManyToOne(result);
            int identifier_value = getIdentifierValue(result);
            restoreManyToOneAssociations(result, identifier_value);
            restoreManyToMany(result, identifier_value);

            fireRestored(result);
        }

        // handle after callback
        Callbacks callbacks = getCallbacks(result);
        if (callbacks != null &&
            !callbacks.afterRestore(result)) {
            return null;
        }

        return result;
    }

    protected List<BeanType> _restore(Select restore)
    throws DatabaseException {
        assert restore != null;

        DbBeanFetcher<BeanType> bean_fetcher = new DbBeanFetcher<BeanType>(getDatasource(), baseClass_, true) {
            public boolean gotBeanInstance(BeanType instance) {
                // handle listeners
                if (instance != null) {
                    restoreManyToOne(instance);
                    int identifier_value = getIdentifierValue(instance);
                    restoreManyToOneAssociations(instance, identifier_value);
                    restoreManyToMany(instance, identifier_value);

                    fireRestored(instance);
                }

                // handle after callback
                Callbacks callbacks = getCallbacks(instance);
                return !(callbacks != null && !callbacks.afterRestore(instance));
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

    protected void restoreManyToMany(final BeanType bean, final int objectId) {
        // TODO
//		// handle many-to-many associations
//		final Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
//		final Map<String, ManyToManyDeclaration> declarations = obtainManyToManyDeclarations(this, constrained, true);
//		if (declarations != null)
//		{
//			// iterate over all the many to many relationships
//			final String column1_name = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
//			for (Map.Entry<String, ManyToManyDeclaration> entry : declarations.entrySet())
//			{
//				ManyToManyDeclaration declaration = entry.getValue();
//
//				// create the associations collection
//				Object association_collection;
//				if (Set.class == declaration.getCollectionType())
//				{
//					association_collection = new ManyToManySet(AbstractGenericQueryManager.this, column1_name, objectId, declaration);
//				}
//				else
//				{
//					association_collection = new ManyToManyList(AbstractGenericQueryManager.this, column1_name, objectId, declaration);
//				}
//
//				// set the restore mappings as the property value
//				try
//				{
//					BeanUtils.setPropertyValue(bean, entry.getKey(), association_collection);
//				}
//				catch (BeanUtilsException e)
//				{
//					throw new DatabaseException(e);
//				}
//			}
//		}
    }

    protected void restoreManyToOne(final BeanType bean) {
        // TODO
//		Field gqm_field = null;
//		Field lazyloaded_field = null;
//		try
//		{
//			gqm_field = bean.getClass().getDeclaredField(LazyLoadAccessorsBytecodeTransformer.GQM_VAR_NAME);
//			lazyloaded_field = bean.getClass().getDeclaredField(LazyLoadAccessorsBytecodeTransformer.LAZYLOADED_VAR_NAME);
//		}
//		catch (Exception e)
//		{
//			// if the synthetic fields don't exist in the class, just set them to null
//			// since this means that bytecode enhancement hasn't been performed to provide
//			// lazy-load functionalities
//			gqm_field = null;
//			lazyloaded_field = null;
//		}
//
//		// if the class has been enhanced for lazy loading capabilities, add a reference
//		// to this GQM and create a new map for storing the already loaded property values
//		if (gqm_field != null &&
//			lazyloaded_field != null)
//		{
//			gqm_field.setAccessible(true);
//			lazyloaded_field.setAccessible(true);
//			try
//			{
//				gqm_field.set(bean, this);
//				if (TerracottaUtils.isTcPresent())
//				{
//					lazyloaded_field.set(bean, new HashMap());
//				}
//				else
//				{
//					lazyloaded_field.set(bean, new WeakHashMap());
//				}
//			}
//			catch (Exception e)
//			{
//				throw new DatabaseException(e);
//			}
//		}
//		// otherwise eargerly load all many-to-one properties
//		else
//		{
//			processManyToOneJoinColumns(this, new ManyToOneJoinColumnProcessor() {
//				public boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration)
//				{
//					Object property_value = restoreManyToOneProperty(AbstractGenericQueryManager.this, bean, declaration.getAssociationManager(), columnName, declaration.getAssociationType());
//
//					// set the many-to-one mapping as the property value
//					try
//					{
//						BeanUtils.setPropertyValue(bean, propertyName, property_value);
//					}
//					catch (BeanUtilsException e)
//					{
//						throw new DatabaseException(e);
//					}
//
//					return true;
//				}
//			});
//		}
    }

    protected void restoreManyToOneAssociations(final BeanType bean, final int objectId) {
        // TODO
//		// handle many-to-one associations
//		final Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
//		final Map<String, ManyToOneAssociationDeclaration> declarations = obtainManyToOneAssociationDeclarations(this, constrained);
//		if (declarations != null)
//		{
//			// iterate over all the many to one association relationships
//			for (Map.Entry<String, ManyToOneAssociationDeclaration> entry : declarations.entrySet())
//			{
//				ManyToOneAssociationDeclaration declaration = entry.getValue();
//
//				// create the associations collection
//				Object association_collection;
//				if (Set.class == declaration.getCollectionType())
//				{
//					association_collection = new ManyToOneAssociationSet(AbstractGenericQueryManager.this, objectId, declaration);
//				}
//				else
//				{
//					association_collection = new ManyToOneAssociationList(AbstractGenericQueryManager.this, objectId, declaration);
//				}
//
//				// set the restore mappings as the property value
//				try
//				{
//					BeanUtils.setPropertyValue(bean, entry.getKey(), association_collection);
//				}
//				catch (BeanUtilsException e)
//				{
//					throw new DatabaseException(e);
//				}
//			}
//		}
    }

    protected int _count(Select count)
    throws DatabaseException {
        return executeGetFirstInt(count);
    }

    protected void install_(final CreateSequence createSequence, final CreateTable createTable)
    throws DatabaseException {
        assert createSequence != null;
        assert createTable != null;

        inTransaction(new DbTransactionUserWithoutResult() {
            public void useTransactionWithoutResult()
            throws InnerClassException {
                if (!isIdentifierSparse()) {
                    executeUpdate(createSequence);
                }
                executeUpdate(createTable);

                installManyToMany();
            }
        });
        fireInstalled();
    }

    protected void installManyToMany() {
        // TODO
//		// create many-to-many join tables
//		Constrained constrained = ConstrainedUtils.getConstrainedInstance(mBaseClass);
//		Map<String, ManyToManyDeclaration> manytomany_declarations = obtainManyToManyDeclarations(this, constrained, false);
//		if (manytomany_declarations != null)
//		{
//			String column1 = generateManyToManyJoinColumnName(AbstractGenericQueryManager.this);
//			for (Map.Entry<String, ManyToManyDeclaration> entry : manytomany_declarations.entrySet())
//			{
//				ManyToManyDeclaration declaration = entry.getValue();
//				GenericQueryManager association_manager = createNewManager(declaration.getAssociationType());
//				String table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);
//				String column2 = generateManyToManyJoinColumnName(association_manager);
//
//				// obtain the violation actions
//				CreateTable.ViolationAction onupdate = null;
//				CreateTable.ViolationAction ondelete = null;
//				ConstrainedProperty property = constrained.getConstrainedProperty(entry.getKey());
//				if (property != null &&
//					property.hasManyToMany())
//				{
//					onupdate = property.getManyToMany().getOnUpdate();
//					ondelete = property.getManyToMany().getOnDelete();
//				}
//
//				// build the table creation query
//				CreateTable create_join_table = new CreateTable(getDatasource())
//					.table(table)
//					.column(column1, int.class, CreateTable.NOTNULL)
//					.column(column2, int.class, CreateTable.NOTNULL)
//					.foreignKey(getTable(), column1, getIdentifierName(), onupdate, ondelete)
//					.foreignKey(association_manager.getTable(), column2, association_manager.getIdentifierName(), onupdate, ondelete);
//				executeUpdate(create_join_table);
//			}
//		}
    }

    protected void remove_(final DropSequence dropSequence, final DropTable dropTable)
    throws DatabaseException {
        assert dropTable != null;
        assert dropSequence != null;

        inTransaction(new DbTransactionUserWithoutResult() {
            public void useTransactionWithoutResult()
            throws InnerClassException {
                removeManyToMany();

                // drop the table itself and optionally the sequence
                executeUpdate(dropTable);
                if (!isIdentifierSparse()) {
                    executeUpdate(dropSequence);
                }
            }
        });
        fireRemoved();
    }

    protected void removeManyToMany() {
        // TODO
//		// drop many-to-many join tables
//		Constrained constrained = ConstrainedUtils.getConstrainedInstance(mBaseClass);
//		Map<String, ManyToManyDeclaration> manytomany_declarations = obtainManyToManyDeclarations(this, constrained, false);
//		if (manytomany_declarations != null)
//		{
//			for (Map.Entry<String, ManyToManyDeclaration> entry : manytomany_declarations.entrySet())
//			{
//				ManyToManyDeclaration declaration = entry.getValue();
//				GenericQueryManager association_manager = createNewManager(declaration.getAssociationType());
//				String table = generateManyToManyJoinTableName(declaration, AbstractGenericQueryManager.this, association_manager);
//
//				// build the table removal query
//				DropTable drop_join_table = new DropTable(getDatasource())
//					.table(table);
//				executeUpdate(drop_join_table);
//			}
//		}
    }

    public void addListener(GenericQueryManagerListener listener) {
        if (null == listener) {
            return;
        }

        if (null == mListeners) {
            mListeners = new ArrayList<>();
        }

        mListeners.add(listener);
    }

    public void removeListeners() {
        if (null == mListeners) {
            return;
        }

        mListeners.clear();
    }

    public <OtherBeanType> GenericQueryManager<OtherBeanType> createNewManager(Class<OtherBeanType> beanClass) {
        return GenericQueryManagerFactory.getInstance(getDatasource(), beanClass);
    }

    protected void fireInstalled() {
        if (null == mListeners) {
            return;
        }

        for (GenericQueryManagerListener listener : mListeners) {
            listener.installed();
        }
    }

    protected void fireRemoved() {
        if (null == mListeners) {
            return;
        }

        for (GenericQueryManagerListener listener : mListeners) {
            listener.removed();
        }
    }

    protected void fireInserted(BeanType bean) {
        if (null == mListeners) {
            return;
        }

        for (GenericQueryManagerListener listener : mListeners) {
            listener.inserted(bean);
        }
    }

    protected void fireUpdated(BeanType bean) {
        if (null == mListeners) {
            return;
        }

        for (GenericQueryManagerListener listener : mListeners) {
            listener.updated(bean);
        }
    }

    protected void fireRestored(BeanType bean) {
        if (null == mListeners) {
            return;
        }

        for (GenericQueryManagerListener listener : mListeners) {
            listener.restored(bean);
        }
    }

    protected void fireDeleted(int objectId) {
        if (null == mListeners) {
            return;
        }

        for (GenericQueryManagerListener listener : mListeners) {
            listener.deleted(objectId);
        }
    }
}

