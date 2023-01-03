/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.databasedrivers;

import rife.database.*;
import rife.database.queries.*;
import rife.database.querymanagers.generic.*;

import rife.database.exceptions.DatabaseException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class generic<BeanType> extends AbstractGenericQueryManager<BeanType> implements GenericQueryManager<BeanType> {
    private CreateTable createTable_ = null;
    private CreateSequence createSequence_ = null;
    private DropTable dropTable_ = null;
    private DropSequence dropSequence_ = null;

    private Select restore_ = null;
    private SequenceValue getNextId_ = null;
    private Delete delete_ = null;
    private Delete deleteNoId_ = null;
    private Update saveUpdate_ = null;
    private Select restoreQuery_ = null;
    private Insert save_ = null;
    private Select count_ = null;

    protected String tableName_;
    protected boolean hasIdentifier_;

    public generic(Datasource datasource, String tableName, String primaryKey, Class<BeanType> beanClass, boolean hasIdentifier)
    throws DatabaseException {
        super(datasource, beanClass, primaryKey);

        baseClass_ = beanClass;
        tableName_ = tableName;
        hasIdentifier_ = hasIdentifier;
    }

    protected CreateTable getInternalCreateTableQuery() {
        if (null == createTable_) {
            final CreateTable query = new CreateTable(getDatasource())
                .table(tableName_)
                .columns(baseClass_);
            if (!hasIdentifier_) {
                query.primaryKey(primaryKey_);
            }

            addCreateTableManyToOneColumns(query);

            createTable_ = query;
        }

        return createTable_;
    }

    protected void addCreateTableManyToOneColumns(final CreateTable query) {
        final Map<String, CreateTable.Column> columns = query.getColumnMapping();
        GenericQueryManagerRelationalUtils.processManyToOneJoinColumns(this, new ManyToOneJoinColumnProcessor() {
            public boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration) {
                if (!columns.containsKey(columnName)) {
                    query
                        .column(columnName, int.class, CreateTable.NULL)
                        .foreignKey(declaration.getAssociationTable(), columnName, declaration.getAssociationColumn());
                }
                return true;
            }
        });
    }

    protected CreateSequence getInternalCreateSequenceQuery() {
        if (null == createSequence_) {
            CreateSequence query = new CreateSequence(getDatasource())
                .name(getSequenceName());
            createSequence_ = query;
        }

        return createSequence_;
    }

    protected String getSequenceName() {
        return "SEQ_" + tableName_;
    }

    protected DropTable getInternalDropTableQuery() {
        if (null == dropTable_) {
            DropTable query = new DropTable(getDatasource())
                .table(tableName_);
            dropTable_ = query;
        }

        return dropTable_;
    }

    protected DropSequence getInternalDropSequenceQuery() {
        if (null == dropSequence_) {
            DropSequence query = new DropSequence(getDatasource())
                .name(getSequenceName());
            dropSequence_ = query;
        }

        return dropSequence_;
    }

    protected Select getInternalRestoreByIdQuery() {
        if (null == restore_) {
            Select query = new Select(getDatasource())
                .from(tableName_)
                .whereParameter(primaryKey_, "=");
            restore_ = query;
        }

        return restore_;
    }

    protected SequenceValue getInternalGetNextIdQuery() {
        if (null == getNextId_) {
            SequenceValue query = new SequenceValue(getDatasource())
                .name(getSequenceName())
                .next();
            getNextId_ = query;
        }

        return getNextId_;
    }

    protected Delete getInternalDeleteQuery() {
        if (null == delete_) {
            Delete query = new Delete(getDatasource())
                .from(tableName_)
                .whereParameter(primaryKey_, "=");
            delete_ = query;
        }

        return delete_;
    }

    protected Delete getInternalDeleteNoIdQuery() {
        if (null == deleteNoId_) {
            Delete query = new Delete(getDatasource())
                .from(tableName_);
            deleteNoId_ = query;
        }

        return deleteNoId_;
    }

    protected Update getInternalSaveUpdateQuery() {
        if (null == saveUpdate_) {
            final Update query = new Update(getDatasource())
                .table(tableName_)
                .fieldsParametersExcluded(baseClass_, new String[]{primaryKey_})
                .whereParameter(primaryKey_, "=");

            addSaveUpdateManyToOneFields(query);

            saveUpdate_ = query;
        }

        return saveUpdate_;
    }

    protected void addSaveUpdateManyToOneFields(final Update query) {
        final Set<String> columns = query.getFields().keySet();
        GenericQueryManagerRelationalUtils.processManyToOneJoinColumns(this, new ManyToOneJoinColumnProcessor() {
            public boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration) {
                if (!columns.contains(columnName)) {
                    query.fieldParameter(columnName);
                }

                return true;
            }
        });
    }

    protected Select getInternalRestoreListQuery() {
        if (null == restoreQuery_) {
            Select query = new Select(getDatasource(), getBaseClass())
                .from(tableName_);
            restoreQuery_ = query;
        }

        return restoreQuery_;
    }

    protected Insert getInternalSaveQuery() {
        if (null == save_) {
            final Insert query = new Insert(getDatasource())
                .into(tableName_)
                .fieldsParameters(getBaseClass());
            if (!query.getFields().containsKey(primaryKey_)) {
                query.fieldParameter(primaryKey_);
            }

            addSaveManyToOneFields(query);

            save_ = query;
        }

        return save_;
    }

    protected void addSaveManyToOneFields(final Insert query) {
        final Set<String> columns = query.getFields().keySet();
        GenericQueryManagerRelationalUtils.processManyToOneJoinColumns(this, new ManyToOneJoinColumnProcessor() {
            public boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration) {
                if (!columns.contains(columnName)) {
                    query.fieldParameter(columnName);
                }

                return true;
            }
        });
    }

    protected Select getInternalCountQuery() {
        if (null == count_) {
            Select query = new Select(getDatasource())
                .from(tableName_)
                .field("count(*)");
            count_ = query;
        }

        return count_;
    }

    public void install()
    throws DatabaseException {
        install_(getInternalCreateSequenceQuery(), getInternalCreateTableQuery());
    }

    public void install(CreateTable query)
    throws DatabaseException {
        install_(getInternalCreateSequenceQuery(), query);
    }

    public int save(BeanType bean)
    throws DatabaseException {
        return _save(getInternalGetNextIdQuery(), getInternalSaveQuery(), getInternalSaveUpdateQuery(), bean);
    }

    public int insert(BeanType bean)
    throws DatabaseException {
        return _insert(getInternalGetNextIdQuery(), getInternalSaveQuery(), bean);
    }

    public int update(BeanType bean)
    throws DatabaseException {
        return _update(getInternalSaveUpdateQuery(), bean);
    }

    public boolean delete(DeleteQuery query)
    throws DatabaseException {
        return _delete(query.getDelegate());
    }

    public boolean delete(int objectId)
    throws DatabaseException {
        return _delete(getInternalDeleteQuery(), objectId);
    }

    public int count()
    throws DatabaseException {
        return _count(getInternalCountQuery());
    }

    public int count(CountQuery query)
    throws DatabaseException {
        return _count(query.getDelegate());
    }

    public BeanType restore(int objectId)
    throws DatabaseException {
        return _restore(getInternalRestoreByIdQuery(), objectId);
    }

    public List<BeanType> restore()
    throws DatabaseException {
        return _restore(getInternalRestoreListQuery());
    }

    public boolean restore(DbRowProcessor rowProcessor)
    throws DatabaseException {
        return _restore(getInternalRestoreListQuery(), rowProcessor);
    }

    public boolean restore(BeanFetcher<BeanType> beanFetcher)
    throws DatabaseException {
        return _restore(getInternalRestoreListQuery(), new DbBeanFetcher<>(getDatasource(), baseClass_) {
            public boolean gotBeanInstance(BeanType instance) {
                beanFetcher.gotBeanInstance(instance);
                return true;
            }
        });
    }

    public List<BeanType> restore(RestoreQuery query)
    throws DatabaseException {
        return _restore(query.getDelegate());
    }

    public boolean restore(RestoreQuery query, DbRowProcessor rowProcessor)
    throws DatabaseException {
        return _restore(query.getDelegate(), rowProcessor);
    }

    public boolean restore(RestoreQuery query, BeanFetcher<BeanType> beanFetcher)
    throws DatabaseException {
        return _restore(query.getDelegate(), new DbBeanFetcher<>(getDatasource(), baseClass_) {
            public boolean gotBeanInstance(BeanType instance) {
                beanFetcher.gotBeanInstance(instance);
                return true;
            }
        });
    }

    public BeanType restoreFirst(RestoreQuery query)
    throws DatabaseException {
        return _restoreFirst(query.getDelegate());
    }

    public void remove()
    throws DatabaseException {
        remove_(getInternalDropSequenceQuery(), getInternalDropTableQuery());
    }

    public CreateTable getInstallTableQuery() {
        return getInternalCreateTableQuery().clone();
    }

    public RestoreQuery getRestoreQuery() {
        return new RestoreQuery(getInternalRestoreListQuery());
    }

    public RestoreQuery getRestoreQuery(int objectId) {
        return new RestoreQuery(getInternalRestoreListQuery()).where(primaryKey_, "=", objectId);
    }

    public CountQuery getCountQuery() {
        return new CountQuery(getInternalCountQuery());
    }

    public DeleteQuery getDeleteQuery() {
        return new DeleteQuery(getInternalDeleteNoIdQuery());
    }

    public DeleteQuery getDeleteQuery(int objectId) {
        return new DeleteQuery(getInternalDeleteNoIdQuery()).where(primaryKey_, "=", objectId);
    }

    public String getTable() {
        return tableName_;
    }
}
