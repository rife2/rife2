/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.databasedrivers;

import rife.database.Datasource;
import rife.database.DbConnection;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.DbTransactionUserWithoutResult;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Insert;
import rife.database.queries.Query;
import rife.database.queries.SequenceValue;
import rife.database.querymanagers.generic.GenericQueryManager;

import java.sql.Statement;

public class org_apache_derby_jdbc_EmbeddedDriver<BeanType> extends generic<BeanType> implements GenericQueryManager<BeanType> {
    private CreateTable createTableDerby_ = null;
    private Insert saveDerby_ = null;

    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource, String tableName, String primaryKey, Class<BeanType> beanClass, boolean hasIdentifier)
    throws DatabaseException {
        super(datasource, tableName, primaryKey, beanClass, hasIdentifier);
    }

    protected CreateTable getInternalCreateTableQuery() {
        if (null == createTableDerby_) {
            CreateTable query = new CreateTable(getDatasource())
                .table(mTableName)
                .columns(baseClass_);
            if (!isIdentifierSparse()) {
                query
                    .customAttribute(mPrimaryKey, "GENERATED ALWAYS AS IDENTITY");
            }
            if (!mHasIdentifier) {
                query
                    .primaryKey(mPrimaryKey);
            }

            addCreateTableManyToOneColumns(query);

            createTableDerby_ = query;
        }

        return createTableDerby_;
    }

    protected Insert getInternalSaveQuery() {
        if (null == saveDerby_) {
            Insert query = new Insert(getDatasource())
                .into(mTableName);
            if (!isIdentifierSparse()) {
                query
                    .fieldsParametersExcluded(baseClass_, new String[]{mPrimaryKey});
            } else {
                query
                    .fieldsParameters(baseClass_);
            }

            addSaveManyToOneFields(query);

            saveDerby_ = query;
        }

        return saveDerby_;
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

    protected int _insertWithoutCallbacks(final SequenceValue nextId, final Insert save, final BeanType bean)
    throws DatabaseException {
        final int[] result = new int[]{getIdentifierValue(bean)};

        inTransaction(new DbTransactionUserWithoutResult() {
            public void useTransactionWithoutResult() {
                storeManyToOne(bean);

                executeUpdate(save, new DbPreparedStatementHandler() {
                    public DbPreparedStatement getPreparedStatement(Query query, DbConnection connection) {
                        return connection.getPreparedStatement(query, Statement.RETURN_GENERATED_KEYS);
                    }

                    public void setParameters(DbPreparedStatement statement) {
                        statement
                            .setBean(bean);

                        setManyToOneJoinParameters(statement, bean);
                    }

                    public int performUpdate(DbPreparedStatement statement) {
                        setParameters(statement);
                        int query_result = statement.executeUpdate();
                        if (isIdentifierSparse()) {
                            result[0] = getIdentifierValue(bean);
                        } else {
                            result[0] = statement.getFirstGeneratedIntKey();
                        }

                        return query_result;
                    }
                });

                if (result[0] != -1) {
                    try {
                        setPrimaryKeyMethod_.invoke(bean, result[0]);
                    } catch (Throwable e) {
                        throw new DatabaseException(e);
                    }

                    storeManyToOneAssociations(bean, result[0]);
                    storeManyToMany(bean, result[0]);
                }
            }
        });

        // handle listeners
        if (result[0] != -1) {
            fireInserted(bean);
        }

        return result[0];
    }
}
