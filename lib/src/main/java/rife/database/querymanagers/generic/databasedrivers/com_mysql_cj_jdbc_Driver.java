/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.databasedrivers;

import rife.database.*;

import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.ExecutionErrorException;
import rife.database.queries.CreateTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.database.queries.Update;
import rife.database.querymanagers.generic.Callbacks;
import rife.database.querymanagers.generic.GenericQueryManager;

import java.sql.Types;

public class com_mysql_cj_jdbc_Driver<BeanType> extends generic<BeanType> implements GenericQueryManager<BeanType> {
    private CreateTable createTableMysql_ = null;
    private Insert saveMysql_ = null;
    private Select lastIdMysql_ = null;

    public com_mysql_cj_jdbc_Driver(Datasource datasource, String tableName, String primaryKey, Class<BeanType> beanClass, boolean hasIdentifier)
    throws DatabaseException {
        super(datasource, tableName, primaryKey, beanClass, hasIdentifier);
    }

    protected CreateTable getInternalCreateTableQuery() {
        if (null == createTableMysql_) {
            CreateTable query = new CreateTable(getDatasource())
                .table(tableName_)
                .columns(baseClass_);
            if (!isIdentifierSparse()) {
                query
                    .customAttribute(primaryKey_, "AUTO_INCREMENT");
            }
            if (!hasIdentifier_) {
                query
                    .primaryKey(primaryKey_);
            }

            addCreateTableManyToOneColumns(query);

            createTableMysql_ = query;
        }

        return createTableMysql_;
    }

    protected Insert getInternalSaveQuery() {
        if (null == saveMysql_) {
            Insert query = new Insert(getDatasource())
                .into(tableName_);
            if (!isIdentifierSparse()) {
                query
                    .fieldsParametersExcluded(baseClass_, new String[]{primaryKey_});
            } else {
                query
                    .fieldsParameters(baseClass_);
            }

            addSaveManyToOneFields(query);

            saveMysql_ = query;
        }

        return saveMysql_;
    }

    protected Select getInternalLastIdQuery() {
        if (null == lastIdMysql_) {
            Select query = new Select(getDatasource())
                .from(tableName_)
                .field("LAST_INSERT_ID()");
            lastIdMysql_ = query;
        }

        return lastIdMysql_;
    }

    public void install()
    throws DatabaseException {
        executeUpdate(getInternalCreateTableQuery());
        installManyToMany();

        fireInstalled();
    }

    public void install(CreateTable query)
    throws DatabaseException {
        executeUpdate(query);
        installManyToMany();

        fireInstalled();
    }

    public void remove()
    throws DatabaseException {
        removeManyToMany();
        executeUpdate(getInternalDropTableQuery());

        fireRemoved();
    }

    public int save(BeanType bean)
    throws DatabaseException {
        return _save(getInternalLastIdQuery(), getInternalSaveQuery(), getInternalSaveUpdateQuery(), bean);
    }

    public int insert(BeanType bean)
    throws DatabaseException {
        return _insert(getInternalLastIdQuery(), getInternalSaveQuery(), bean);
    }

    protected int _insert(final Select lastId, final Insert save, final BeanType bean) {
        // handle before callback
        Callbacks callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeInsert(bean)) {
            return -1;
        }

        // perform insert
        int result = _insertWithoutCallbacks(lastId, save, bean);

        // handle after callback
        if (callbacks != null) {
            callbacks.afterInsert(bean, result != -1);
        }

        return result;
    }

    protected int _insertWithoutCallbacks(final Select lastId, final Insert save, final BeanType bean)
    throws DatabaseException {
        int result = -1;

        result = inTransaction(new DbTransactionUser() {
            public Integer useTransaction() {
                // reserving the connection inside the transaction user
                // since there are versions of MySQL that don't support tranactions
                // and in that case the thread connection isn't reserved to obtain
                // the generated primary key
                return reserveConnection(new DbConnectionUser() {
                    public Integer useConnection(DbConnection connection) {
                        storeManyToOne(bean);

                        executeUpdate(save, new DbPreparedStatementHandler() {
                            public void setParameters(DbPreparedStatement statement) {
                                statement.setBean(bean);

                                if (!isIdentifierSparse() &&
                                    save.getFields().containsKey(getIdentifierName())) {
                                    statement.setNull(getIdentifierName(), Types.INTEGER);
                                }

                                setManyToOneJoinParameters(statement, bean);
                            }
                        });

                        int primary_key_id;
                        if (isIdentifierSparse()) {
                            primary_key_id = getIdentifierValue(bean);
                        } else {
                            primary_key_id = executeGetFirstInt(lastId);
                        }

                        storeManyToOneAssociations(bean, primary_key_id);
                        storeManyToMany(bean, primary_key_id);

                        return primary_key_id;
                    }
                });
            }
        });

        if (result != -1) {
            try {
                setPrimaryKeyMethod_.invoke(bean, result);
            } catch (Throwable e) {
                throw new DatabaseException(e);
            }
        }

        // handle listeners
        if (result != -1) {
            fireInserted(bean);
        }

        return result;
    }

    protected int _save(final Select lastId, final Insert save, final Update saveUpdate, final BeanType bean)
    throws DatabaseException {
        int value = -1;

        // handle before callback
        final Callbacks callbacks = getCallbacks(bean);
        if (callbacks != null &&
            !callbacks.beforeSave(bean)) {
            return -1;
        }

        boolean update = false;
        try {
            int id = getIdentifierValue(bean);
            if (id >= 0) {
                value = id;
                update = true;
            }
        } catch (Throwable e) {
            throw new DatabaseException(e);
        }

        if (isIdentifierSparse()) {
            // handle before callback
            if (callbacks != null &&
                !callbacks.beforeInsert(bean)) {
                return -1;
            }

            // try to perform the insert
            try {
                value = _insertWithoutCallbacks(lastId, save, bean);
            } catch (ExecutionErrorException e) {
                value = -1;
            }

            // handle after callback
            if (callbacks != null &&
                !callbacks.afterInsert(bean, value != -1)) {
                return value;
            }

            if (-1 == value) {
                // handle before callback
                if (callbacks != null &&
                    !callbacks.beforeUpdate(bean)) {
                    return -1;
                }

                value = _updateWithoutCallbacks(saveUpdate, bean);

                // handle after callback
                if (callbacks != null &&
                    !callbacks.afterUpdate(bean, value != -1)) {
                    return value;
                }
            }
        } else {
            if (update) {
                // handle before callback
                if (callbacks != null &&
                    !callbacks.beforeUpdate(bean)) {
                    return -1;
                }

                value = _updateWithoutCallbacks(saveUpdate, bean);

                // handle after callback
                if (callbacks != null &&
                    !callbacks.afterUpdate(bean, value != -1)) {
                    return value;
                }
            }

            if (-1 == value) {
                // handle before callback
                if (callbacks != null &&
                    !callbacks.beforeInsert(bean)) {
                    return -1;
                }

                value = _insertWithoutCallbacks(lastId, save, bean);

                // handle after callback
                if (callbacks != null &&
                    !callbacks.afterInsert(bean, value != -1)) {
                    return value;
                }
            }
        }

        // handle after callback
        if (callbacks != null) {
            callbacks.afterSave(bean, value != -1);
        }

        return value;
    }
}
