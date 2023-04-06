/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

/**
 * By extending this class it's possible to easily customize the behaviour of
 * a large number of methods in the {@link DbQueryManager} class.
 * <p>You're able to set the parameters of a {@link DbPreparedStatement}
 * before the actual execution of any logic by overriding the {@link
 * #setParameters(DbPreparedStatement) setParameters} method.
 * <p>If you need to customize the entire query execution, you can override
 * the {@link #performUpdate(DbPreparedStatement) performUpdate} and {@link
 * #performQuery(DbPreparedStatement) performQuery} methods. Note that these
 * methods are actually responsible for calling the {@link
 * #setParameters(DbPreparedStatement) setParameters} method, so if you
 * override them you either have to call this method yourself or include the
 * code in the overridden method.
 * <p>This class has both a default constructor and one that can take a data
 * object. This can be handy when using it as an extending anonymous inner
 * class when you need to use variables inside the inner class that are
 * cumbersome to change to {@code final} in the enclosing class.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DbPreparedStatement
 * @see DbQueryManager
 * @since 1.0
 */
public abstract class DbPreparedStatementHandler<DataType> extends DbResultSetHandler implements PreparedStatementHandler {
    protected DataType data_ = null;

    public DbPreparedStatementHandler() {
    }

    public DbPreparedStatementHandler(DataType data) {
        data_ = data;
    }

    public DataType getData() {
        return data_;
    }

    public void setParameters(DbPreparedStatement statement) {
    }

    public int performUpdate(DbPreparedStatement statement) {
        setParameters(statement);
        return statement.executeUpdate();
    }

    public void performQuery(DbPreparedStatement statement) {
        setParameters(statement);
        statement.executeQuery();
    }
}
