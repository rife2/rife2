/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.*;

import java.sql.*;

import rife.database.queries.Query;
import rife.tools.ArrayUtils;
import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.Constrained;
import rife.validation.ConstrainedUtils;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Provides a wrapper around the regular JDBC {@code PreparedStatement}
 * class. It can only be instantiated by calling the
 * {@code getPreparedStatement} method on an existing
 * {@code DbConnection} instance.
 * <p>This class hooks into the database connection pool and cleans up as much
 * as possible in case of errors. The thrown {@code DatabaseException}
 * exceptions should thus only be used for error reporting and not for
 * releasing resources used by the framework.
 * <p>The {@code executeQuery} method stores its resultset in the
 * executing {@code DbPreparedStatement} instance. It's recommended to
 * use the {@code DbQueryManager}'s {@code fetch} method to process
 * the result set. If needed, one can also use the {@code getResultSet}
 * method to manually process the results through plain JDBC. However, when
 * exceptions are thrown during this procedure, it's also the responsability
 * of the user to correctly clean up all resources.
 * <p>Additional methods have been implemented to take advantage of
 * information that is present when one uses query builders to construct the
 * database queries. In this case, parameter values can be set by using column
 * names instead of column numbers and automatic population of a statement
 * from bean property values is also supported.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see #executeQuery()
 * @see #getResultSet()
 * @see rife.database.DbConnection#getPreparedStatement(String)
 * @see rife.database.DbQueryManager#fetch(ResultSet, DbRowProcessor)
 * @see rife.database.queries.Delete
 * @see rife.database.queries.Insert
 * @see rife.database.queries.Select
 * @see rife.database.queries.Update
 * @see java.sql.PreparedStatement
 * @see java.sql.ResultSet
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class DbPreparedStatement extends DbStatement {
    private String sql_;
    private Query query_;

    private List<String> parameterNames_ = null;
    private VirtualParameters virtualParameters_ = null;

    /**
     * Constructs a new {@code DbStatement} from a SQL query string,
     * a {@code DbConnection} and a {@code PreparedStatement}.
     * This constructor will never be called by a user of the api. The
     * {@code getPreparedStatement} of an existing
     * {@code DbConnection} instance should be used instead.
     *
     * @param connection        a {@code DbConnection} instance
     * @param sql               a {@code String} with the sql statement
     * @param preparedStatement a JDBC {@code PreparedStatement}
     *                          instance
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    DbPreparedStatement(DbConnection connection, String sql, PreparedStatement preparedStatement)
    throws DatabaseException {
        super(connection, preparedStatement);

        assert sql != null;
        assert sql.length() > 0;
        assert preparedStatement != null;

        sql_ = sql;
        query_ = null;
    }

    /**
     * Constructs a new {@code DbStatement} from a
     * {@code ParametrizedQuery}, a {@code DbConnection} and a
     * {@code PreparedStatement}. This constructor will never be
     * called by a user of the api. The {@code getPreparedStatement}
     * of an existing {@code DbConnection} instance should be used
     * instead.
     *
     * @param connection        a {@code DbConnection} instance
     * @param query             a {@code Query} with the sql statement
     * @param preparedStatement a JDBC {@code PreparedStatement}
     *                          instance
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    DbPreparedStatement(DbConnection connection, Query query, PreparedStatement preparedStatement)
    throws DatabaseException {
        super(connection, preparedStatement);

        String sql = query.getSql();

        assert sql != null;
        assert sql.length() > 0;
        assert preparedStatement != null;

        sql_ = sql;
        query_ = query;
    }

    /**
     * Returns the SQL query that will be executed by this prepared
     * statement.
     *
     * @return a {@code String} with the SQL query of this prepared
     * statement
     * @since 1.0
     */
    public String getSql() {
        return sql_;
    }

    /**
     * Returns the query builder that provides the SQL query that will be
     * executed by this prepared statement.
     *
     * @return a {@code Query} object with the query builder
     * instance; or
     * <p>{@code null} if the prepared statement was initialized from
     * a string SQL query
     * @since 1.0
     */
    public Query getQuery() {
        return query_;
    }

    /**
     * Executes the SQL query in this {@code DbPreparedStatement}
     * object. The {@code ResultSet} object generated by the query is
     * stored and can be retrieved with the {@code getResultSet}
     * method.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @throws DatabaseException if a database access error occurs or
     *                           the SQL statement does not return a {@code ResultSet} object
     * @see #getResultSet()
     * @since 1.0
     */
    public void executeQuery()
    throws DatabaseException {
        try {
            waitForConnection();

            cleanResultSet();

            long start = startTrace();
            if (virtualParameters_ != null) {
                virtualParameters_.callHandler(this);
            }
            ResultSet resultset = ((PreparedStatement) statement_).executeQuery();
            outputTrace(start, getSql());

            setResultset(resultset);
            return;
        } catch (SQLException e) {
            handleException();
            throw new ExecutionErrorException(sql_, connection_.getDatasource(), e);
        }
    }

    /**
     * Executes the SQL statement in this {@code DbPreparedStatement}
     * object, which must be an SQL {@code INSERT},
     * {@code UPDATE} or {@code DELETE} statement; or a SQL
     * statement that returns nothing, such as a DDL statement.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @return the row count for {@code INSERT}, {@code UPDATE},
     * or {@code DELETE} statements; or
     * <p>0 for SQL statements that return nothing
     * @throws DatabaseException if a database access error occurs or
     *                           the SQL statement returns a {@code ResultSet} object
     * @since 1.0
     */
    public int executeUpdate()
    throws DatabaseException {
        try {
            waitForConnection();

            long start = startTrace();
            if (virtualParameters_ != null) {
                virtualParameters_.callHandler(this);
            }
            int result = ((PreparedStatement) statement_).executeUpdate();
            outputTrace(start, getSql());

            return result;
        } catch (SQLException e) {
            handleException();
            throw new ExecutionErrorException(sql_, connection_.getDatasource(), e);
        }
    }

    /**
     * Adds a set of parameters to this {@code DbPreparedStatement}
     * object's batch of commands.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @throws DatabaseException if a database access error occurs
     * @see rife.database.DbStatement#addBatch
     * @since 1.0
     */
    public void addBatch()
    throws DatabaseException {
        try {
            ((PreparedStatement) statement_).addBatch();
            traceBatch(sql_);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Releases this {@code DbPreparedStatement} object's database
     * and JDBC resources immediately instead of waiting for this to
     * happen when it is automatically closed. It is generally good
     * practice to release resources as soon as you are finished with them
     * to avoid tying up database resources.
     * <p>Calling the method {@code close} on a
     * {@code DbPreparedStatement} object that is already closed has
     * no effect.
     * <p><b>Note:</b> A {@code DbPreparedStatement} object is
     * automatically closed when it is garbage collected. When a
     * {@code DbPreparedStatement} object is closed, its current
     * {@code ResultSet} object, if one exists, is also closed.
     *
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public void close()
    throws DatabaseException {
        super.close();
    }

    /**
     * Retrieves a {@code ResultSetMetaData} object that contains
     * information about the columns of the {@code ResultSet} object
     * that will be returned when this {@code PDbreparedStatement}
     * object is executed.
     * <p>Because a {@code DbPreparedStatement} object is
     * precompiled, it is possible to know about the
     * {@code ResultSet} object that it will return without having to
     * execute it. Consequently, it is possible to invoke the method
     * {@code getMetaData} on a {@code DbPreparedStatement}
     * object rather than waiting to execute it and then invoking the
     * {@code ResultSet.getMetaData} method on the
     * {@code ResultSet} object that is returned.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p><b>NOTE:</b> Using this method may be expensive for some drivers
     * due to the lack of underlying DBMS support.
     *
     * @return the description of a {@code ResultSet} object's
     * columns; or
     * <p>{@code null} if the driver cannot return a
     * {@code ResultSetMetaData} object
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public ResultSetMetaData getMetaData()
    throws DatabaseException {
        try {
            return ((PreparedStatement) statement_).getMetaData();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Retrieves the number, types and properties of this
     * {@code DbPreparedStatement} object's parameters.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @return a {@code ParameterMetaData} object that contains
     * information about the number, types and properties of this
     * {@code DbPreparedStatement} object's parameters.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.ParameterMetaData
     * @since 1.0
     */
    public ParameterMetaData getParameterMetaData()
    throws DatabaseException {
        try {
            return ((PreparedStatement) statement_).getParameterMetaData();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }

    /**
     * Ensures that this {@code DbPrepareStatement} instance has been
     * defined by a valid {@code ParametrizedQuery} and initializes
     * all parameter-related instance variables.
     *
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters.
     * @since 1.0
     */
    private void validateParametrizedQuery()
    throws DatabaseException {
        if (null == query_) {
            throw new NoParametrizedQueryException(this);
        }

        if (null == query_.getParameters() ||
            0 == query_.getParameters().getOrderedNames().size()) {
            throw new NoParametersException(this);
        }

        if (null == parameterNames_) {
            parameterNames_ = query_.getParameters().getOrderedNames();
            if (virtualParameters_ != null) {
                virtualParameters_.setup(query_);
            }
        }

    }

    /**
     * Get the value of a specific virtual parameter.
     *
     * @param name the name of the parameter whose value should be
     *             retrieved
     * @return the requested value
     * @throws DatabaseException when an error occurred during the
     *                           retrieval of the parameter's value
     * @since 1.0
     */
    public Object getVirtualParameterValue(String name)
    throws UndefinedVirtualParameterException {
        validateParametrizedQuery();

        int[] virtual_indices = getParameterIndices(name);

        if (!virtualParameters_.hasValue(virtual_indices[0])) {
            throw new UndefinedVirtualParameterException(this, name);
        }

        return virtualParameters_.getValue(virtual_indices[0]);
    }

    /**
     * Get the value of a specific virtual parameter.
     *
     * @param parameterIndex the index of the parameter whose value should
     *                       be retrieved
     * @return the requested value
     * @throws DatabaseException when an error occurred during the
     *                           retrieval of the parameter's value
     * @since 1.0
     */
    public Object getVirtualParameterValue(int parameterIndex)
    throws DatabaseException {
        validateParametrizedQuery();

        if (!virtualParameters_.hasValue(parameterIndex)) {
            throw new UndefinedVirtualParameterException(this, parameterIndex);
        }

        return virtualParameters_.getValue(parameterIndex);
    }

    /**
     * Automatically retrieves all the values of a bean's properties and
     * sets them for the parameters that have been defined by the
     * {@code ParametrizedQuery} object of this
     * {@code DbPrepareStatement} instance.
     *
     * @param bean the bean whose properties should be assigned to the
     *             query's parameters.
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if an error occurred during the manipulation of the bean's
     *                           properties.
     * @since 1.0
     */
    public DbPreparedStatement setBean(Object bean)
    throws DatabaseException {
        if (null == bean) throw new IllegalArgumentException("bean can't be null.");

        try {
            validateParametrizedQuery();

            String[] parameters_array = query_.getParameters().getOrderedNamesArray();

            Map<String, Class> property_types = BeanUtils.getPropertyTypes(bean.getClass(), parameters_array, null, null);
            Map<String, Object> property_values = BeanUtils.getPropertyValues(bean, parameters_array, null, null);
            Class property_type = null;
            Object property_value = null;
            int parameter_counter = 1;
			Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);
            for (String parameter_name : parameterNames_) {
                if (property_types.containsKey(parameter_name)) {
                    property_type = property_types.get(parameter_name);
                    property_value = property_values.get(parameter_name);

                    getConnection().getDatasource().getSqlConversion().setTypedParameter(this, parameter_counter, property_type, parameter_name, property_value, constrained);
                }

                parameter_counter++;
            }
        } catch (BeanUtilsException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the parameters that should be handled as virtual parameters.
     * These parameters are not sent to the backend, but their values will
     * be stored in this {@code DbPreparedStatement} instance for
     * retrieval by other functionalities like capabilities.
     *
     * @param parameters the {@code VirtualParameters} instance that
     *                   will determine the virtual parameters
     * @since 1.0
     */
    public void setVirtualParameters(VirtualParameters parameters) {
        parameterNames_ = null;
        virtualParameters_ = parameters;
    }

    /**
     * Retrieves all the parameter indices that correspond to the name of
     * a parameter of the {@code ParametrizedQuery} object that is
     * used by this {@code DbPreparedStatement} instance.
     *
     * @param parameterName the name of the parameter that should be
     *                      looked up
     * @return an {@code int} array with all the corresponding
     * indices
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found.
     * @since 1.0
     */
    public int[] getParameterIndices(String parameterName)
    throws DatabaseException {
        if (null == parameterName) throw new IllegalArgumentException("parameterName can't be null.");
        if (0 == parameterName.length()) throw new IllegalArgumentException("parameterName can't be empty.");

        validateParametrizedQuery();

        int parameter_index = 1;
        int[] parameter_indices = new int[0];
        for (String parameter_name : parameterNames_) {
            if (parameter_name.equals(parameterName)) {
                parameter_indices = ArrayUtils.join(parameter_indices, parameter_index);
            }

            parameter_index++;
        }

        if (0 == parameter_indices.length) {
            throw new ParameterDoesntExistException(this, parameterName);
        }

        return parameter_indices;
    }

    /**
     * Sets the named parameters to the given Java {@code double}
     * value. The driver converts this to a SQL {@code DOUBLE} value
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setDoubles(int[], double)
     * @see #setDouble(int, double)
     * @since 1.0
     */
    public DbPreparedStatement setDouble(String parameterName, double x)
    throws DatabaseException {
        setDoubles(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java
     * {@code double} value. The driver converts this to a SQL
     * {@code DOUBLE} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setDouble(String, double)
     * @see #setDouble(int, double)
     * @since 1.0
     */
    public DbPreparedStatement setDoubles(int[] parameterIndices, double x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setDouble(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java {@code double}
     * value. The driver converts this to a SQL {@code DOUBLE} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setDouble(String, double)
     * @see #setDoubles(int[], double)
     * @since 1.0
     */
    public DbPreparedStatement setDouble(int parameterIndex, double x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setDouble(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given Java {@code short}
     * value. The driver converts this to a SQL {@code SMALLINT}
     * value when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setShorts(int[], short)
     * @see #setShort(int, short)
     * @since 1.0
     */
    public DbPreparedStatement setShort(String parameterName, short x)
    throws DatabaseException {
        setShorts(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java {@code short}
     * value. The driver converts this to a SQL {@code SMALLINT}
     * value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setShort(String, short)
     * @see #setShort(int, short)
     * @since 1.0
     */
    public DbPreparedStatement setShorts(int[] parameterIndices, short x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setShort(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java {@code short}
     * value. The driver converts this to a SQL {@code SMALLINT}
     * value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setShort(String, short)
     * @see #setShorts(int[], short)
     * @since 1.0
     */
    public DbPreparedStatement setShort(int parameterIndex, short x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setShort(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to SQL {@code NULL}.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     * <p><b>Note:</b> You must specify the parameter's SQL type.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param sqlType       the SQL type code defined in
     *                      {@code java.sql.Types}
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see java.sql.Types
     * @see #setNulls(int[], int)
     * @see #setNull(int, int)
     * @since 1.0
     */
    public DbPreparedStatement setNull(String parameterName, int sqlType)
    throws DatabaseException {
        setNulls(getParameterIndices(parameterName), sqlType);

        return this;
    }

    /**
     * Sets the designated parameters to SQL {@code NULL}.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p><b>Note:</b> You must specify the parameter's SQL type.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param sqlType          the SQL type code defined in
     *                         {@code java.sql.Types}
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setNull(String, int)
     * @see #setNull(int, int)
     * @since 1.0
     */
    public DbPreparedStatement setNulls(int[] parameterIndices, int sqlType)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setNull(parameter_index, sqlType);
        }

        return this;
    }

    /**
     * Sets the designated parameter to SQL {@code NULL}.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p><b>Note:</b> You must specify the parameter's SQL type.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param sqlType        the SQL type code defined in
     *                       {@code java.sql.Types}
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setNull(String, int)
     * @see #setNulls(int[], int)
     * @since 1.0
     */
    public DbPreparedStatement setNull(int parameterIndex, int sqlType)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, null);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setNull(parameterIndex, sqlType);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to SQL {@code NULL}. This version of
     * the method {@code setNull} should be used for user-defined
     * types and REF type parameters. Examples of user-defined types
     * include: STRUCT, DISTINCT, JAVA_OBJECT, and named array types.
     * <p><b>Note:</b> To be portable, applications must give the SQL type
     * code and the fully-qualified SQL type name when specifying a NULL
     * user-defined or REF parameter. In the case of a user-defined type
     * the name is the type name of the parameter itself. For a REF
     * parameter, the name is the type name of the referenced type. If a
     * JDBC driver does not need the type code or type name information,
     * it may ignore it.
     * <p>Although it is intended for user-defined and Ref parameters,
     * this method may be used to set a null parameter of any JDBC type.
     * If the parameter does not have a user-defined or REF type, the
     * given typeName is ignored.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param sqlType       a value from {@code java.sql.Types}
     * @param typeName      the fully-qualified name of an SQL user-defined
     *                      type; ignored if the parameter is not a user-defined type or REF
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see java.sql.Types
     * @see #setNulls(int[], int, String)
     * @see #setNull(int, int, String)
     * @since 1.0
     */
    public DbPreparedStatement setNull(String parameterName, int sqlType, String typeName)
    throws DatabaseException {
        setNulls(getParameterIndices(parameterName), sqlType, typeName);

        return this;
    }

    /**
     * Sets the designated parameters to SQL {@code NULL}. This
     * version of the method {@code setNull} should be used for
     * user-defined types and REF type parameters. Examples of
     * user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types.
     * <p><b>Note:</b> To be portable, applications must give the SQL type
     * code and the fully-qualified SQL type name when specifying a NULL
     * user-defined or REF parameter. In the case of a user-defined type
     * the name is the type name of the parameter itself. For a REF
     * parameter, the name is the type name of the referenced type. If a
     * JDBC driver does not need the type code or type name information,
     * it may ignore it.
     * <p>Although it is intended for user-defined and Ref parameters,
     * this method may be used to set a null parameter of any JDBC type.
     * If the parameter does not have a user-defined or REF type, the
     * given typeName is ignored.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param sqlType          a value from {@code java.sql.Types}
     * @param typeName         the fully-qualified name of an SQL user-defined
     *                         type; ignored if the parameter is not a user-defined type or REF
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setNull(String, int, String)
     * @see #setNull(int, int, String)
     * @since 1.0
     */
    public DbPreparedStatement setNulls(int[] parameterIndices, int sqlType, String typeName)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setNull(parameter_index, sqlType, typeName);
        }

        return this;
    }

    /**
     * Sets the designated parameter to SQL {@code NULL}. This
     * version of the method {@code setNull} should be used for
     * user-defined types and REF type parameters. Examples of
     * user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types.
     * <p><b>Note:</b> To be portable, applications must give the SQL type
     * code and the fully-qualified SQL type name when specifying a NULL
     * user-defined or REF parameter. In the case of a user-defined type
     * the name is the type name of the parameter itself. For a REF
     * parameter, the name is the type name of the referenced type. If a
     * JDBC driver does not need the type code or type name information,
     * it may ignore it.
     * <p>Although it is intended for user-defined and Ref parameters,
     * this method may be used to set a null parameter of any JDBC type.
     * If the parameter does not have a user-defined or REF type, the
     * given typeName is ignored.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param sqlType        a value from {@code java.sql.Types}
     * @param typeName       the fully-qualified name of an SQL user-defined
     *                       type; ignored if the parameter is not a user-defined type or REF
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setNull(String, int, String)
     * @see #setNulls(int[], int, String)
     * @since 1.0
     */
    public DbPreparedStatement setNull(int parameterIndex, int sqlType, String typeName)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, null);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setNull(parameterIndex, sqlType, typeName);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given Java {@code boolean}
     * value. The driver converts this to a SQL {@code BIT} value
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setBoolean(String, boolean)
     * @see #setBooleans(int[], boolean)
     * @since 1.0
     */
    public DbPreparedStatement setBoolean(String parameterName, boolean x)
    throws DatabaseException {
        setBooleans(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java
     * {@code boolean} value. The driver converts this to a SQL
     * {@code BIT} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBoolean(String, boolean)
     * @see #setBoolean(int, boolean)
     * @since 1.0
     */
    public DbPreparedStatement setBooleans(int[] parameterIndices, boolean x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setBoolean(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java
     * {@code boolean} value. The driver converts this to a SQL
     * {@code BIT} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBoolean(String, boolean)
     * @see #setBooleans(int[], boolean)
     * @since 1.0
     */
    public DbPreparedStatement setBoolean(int parameterIndex, boolean x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setBoolean(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given Java {@code byte}
     * value. The driver converts this to a SQL {@code TINYINT} value
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setByte(int, byte)
     * @see #setBytes(int[], byte)
     * @since 1.0
     */
    public DbPreparedStatement setByte(String parameterName, byte x)
    throws DatabaseException {
        setBytes(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java {@code byte}
     * value. The driver converts this to a SQL {@code TINYINT} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setByte(String, byte)
     * @see #setByte(int, byte)
     * @since 1.0
     */
    public DbPreparedStatement setBytes(int[] parameterIndices, byte x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setByte(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java {@code byte}
     * value. The driver converts this to a SQL {@code TINYINT} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setByte(String, byte)
     * @see #setBytes(int[], byte)
     * @since 1.0
     */
    public DbPreparedStatement setByte(int parameterIndex, byte x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setByte(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given {@code java.sql.Date}
     * value. The driver converts this to a SQL {@code DATE} value
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setDates(int[], Date)
     * @see #setDate(int, Date)
     * @since 1.0
     */
    public DbPreparedStatement setDate(String parameterName, Date x)
    throws DatabaseException {
        setDates(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.sql.Date} value. The driver converts this to a SQL
     * {@code DATE} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setDate(String, Date)
     * @see #setDate(int, Date)
     * @since 1.0
     */
    public DbPreparedStatement setDates(int[] parameterIndices, Date x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setDate(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.sql.Date} value. The driver converts this to a SQL
     * {@code DATE} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setDate(String, Date)
     * @see #setDates(int[], Date)
     * @since 1.0
     */
    public DbPreparedStatement setDate(int parameterIndex, Date x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setDate(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given {@code java.sql.Date}
     * value, using the given {@code Calendar} object. The driver
     * uses the {@code Calendar} object to construct an SQL
     * {@code DATE} value, which the driver then sends to the
     * database. With a {@code Calendar} object, the driver can
     * calculate the date taking into account a custom timezone. If no
     * {@code Calendar} object is specified, the driver uses the
     * default timezone, which is that of the virtual machine running the
     * application.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @param cal           the {@code Calendar} object the driver will use to
     *                      construct the date
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setDates(int[], Date, Calendar)
     * @see #setDate(int, Date, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setDate(String parameterName, Date x, Calendar cal)
    throws DatabaseException {
        setDates(getParameterIndices(parameterName), x, cal);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.sql.Date} value, using the given
     * {@code Calendar} object. The driver uses the
     * {@code Calendar} object to construct an SQL {@code DATE}
     * value, which the driver then sends to the database. With a
     * {@code Calendar} object, the driver can calculate the date
     * taking into account a custom timezone. If no {@code Calendar}
     * object is specified, the driver uses the default timezone, which is
     * that of the virtual machine running the application.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @param cal              the {@code Calendar} object the driver will use to
     *                         construct the date
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setDate(String, Date, Calendar)
     * @see #setDate(int, Date, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setDates(int[] parameterIndices, Date x, Calendar cal)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setDate(parameter_index, x, cal);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.sql.Date} value, using the given
     * {@code Calendar} object. The driver uses the
     * {@code Calendar} object to construct an SQL {@code DATE}
     * value, which the driver then sends to the database. With a
     * {@code Calendar} object, the driver can calculate the date
     * taking into account a custom timezone. If no {@code Calendar}
     * object is specified, the driver uses the default timezone, which is
     * that of the virtual machine running the application.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @param cal            the {@code Calendar} object the driver will use to
     *                       construct the date
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setDate(String, Date, Calendar)
     * @see #setDates(int[], Date, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setDate(int parameterIndex, Date x, Calendar cal)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " with " + cal.getClass().getName());
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setDate(parameterIndex, x, cal);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameter to the given Java {@code int} value.
     * The driver converts this to a SQL {@code INTEGER} value when
     * it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setInt(int, int)
     * @see #setInts(int[], int)
     * @since 1.0
     */
    public DbPreparedStatement setInt(String parameterName, int x)
    throws DatabaseException {
        setInts(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java {@code int}
     * value. The driver converts this to a SQL {@code INTEGER} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setInt(String, int)
     * @see #setInt(int, int)
     * @since 1.0
     */
    public DbPreparedStatement setInts(int[] parameterIndices, int x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setInt(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java {@code int}
     * value. The driver converts this to a SQL {@code INTEGER} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setInt(String, int)
     * @see #setInts(int[], int)
     * @since 1.0
     */
    public DbPreparedStatement setInt(int parameterIndex, int x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setInt(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given Java {@code long}
     * value. The driver converts this to a SQL {@code BIGINT} value
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setLongs(int[], long)
     * @see #setLong(int, long)
     * @since 1.0
     */
    public DbPreparedStatement setLong(String parameterName, long x)
    throws DatabaseException {
        setLongs(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java {@code long}
     * value. The driver converts this to a SQL {@code BIGINT} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setLong(String, long)
     * @see #setLong(int, long)
     * @since 1.0
     */
    public DbPreparedStatement setLongs(int[] parameterIndices, long x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setLong(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java {@code long}
     * value. The driver converts this to a SQL {@code BIGINT} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setLong(String, long)
     * @see #setLongs(int[], long)
     * @since 1.0
     */
    public DbPreparedStatement setLong(int parameterIndex, long x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setLong(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given Java {@code float}
     * value. The driver converts this to a SQL {@code FLOAT} value
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setFloats(int[], float)
     * @see #setFloat(int, float)
     * @since 1.0
     */
    public DbPreparedStatement setFloat(String parameterName, float x)
    throws DatabaseException {
        setFloats(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java {@code float}
     * value. The driver converts this to a SQL {@code FLOAT} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setFloat(String, float)
     * @see #setFloat(int, float)
     * @since 1.0
     */
    public DbPreparedStatement setFloats(int[] parameterIndices, float x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setFloat(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java {@code float}
     * value. The driver converts this to a SQL {@code FLOAT} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setFloat(String, float)
     * @see #setFloats(int[], float)
     * @since 1.0
     */
    public DbPreparedStatement setFloat(int parameterIndex, float x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setFloat(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given
     * {@code java.math.BigDecimal} value. The driver converts this
     * to a SQL {@code NUMERIC} value when it sends it to the
     * database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setBigDecimal(int, BigDecimal)
     * @see #setBigDecimals(int[], BigDecimal)
     * @since 1.0
     */
    public DbPreparedStatement setBigDecimal(String parameterName, BigDecimal x)
    throws DatabaseException {
        setBigDecimals(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.math.BigDecimal} value. The driver converts this
     * to a SQL {@code NUMERIC} value when it sends it to the
     * database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBigDecimal(String, BigDecimal)
     * @see #setBigDecimal(int, BigDecimal)
     * @since 1.0
     */
    public DbPreparedStatement setBigDecimals(int[] parameterIndices, BigDecimal x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setBigDecimal(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.math.BigDecimal} value. The driver converts this
     * to a SQL {@code NUMERIC} value when it sends it to the
     * database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBigDecimal(String, BigDecimal)
     * @see #setBigDecimals(int[], BigDecimal)
     * @since 1.0
     */
    public DbPreparedStatement setBigDecimal(int parameterIndex, BigDecimal x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setBigDecimal(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given Java {@code String}
     * value. The driver converts this to a SQL {@code VARCHAR} or
     * {@code LONGVARCHAR} value (depending on the argument's size
     * relative to the driver's limits on {@code VARCHAR} values)
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setString(int, String)
     * @see #setStrings(int[], String)
     * @since 1.0
     */
    public DbPreparedStatement setString(String parameterName, String x)
    throws DatabaseException {
        setStrings(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java
     * {@code String} value. The driver converts this to a SQL
     * {@code VARCHAR} or {@code LONGVARCHAR} value (depending
     * on the argument's size relative to the driver's limits on
     * {@code VARCHAR} values) when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setString(String, String)
     * @see #setString(int, String)
     * @since 1.0
     */
    public DbPreparedStatement setStrings(int[] parameterIndices, String x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setString(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java {@code String}
     * value. The driver converts this to a SQL {@code VARCHAR} or
     * {@code LONGVARCHAR} value (depending on the argument's size
     * relative to the driver's limits on {@code VARCHAR} values)
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setString(String, String)
     * @see #setStrings(int[], String)
     * @since 1.0
     */
    public DbPreparedStatement setString(int parameterIndex, String x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setString(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given Java array of bytes. The
     * driver converts this to a SQL {@code VARBINARY} or
     * {@code LONGVARBINARY} (depending on the argument's size
     * relative to the driver's limits on {@code VARBINARY} values)
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setBytes(int[], byte[])
     * @see #setBytes(int, byte[])
     * @since 1.0
     */
    public DbPreparedStatement setBytes(String parameterName, byte x[])
    throws DatabaseException {
        setBytes(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given Java array of bytes.
     * The driver converts this to a SQL {@code VARBINARY} or
     * {@code LONGVARBINARY} (depending on the argument's size
     * relative to the driver's limits on {@code VARBINARY} values)
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBytes(String, byte[])
     * @see #setBytes(int, byte[])
     * @since 1.0
     */
    public DbPreparedStatement setBytes(int[] parameterIndices, byte x[])
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setBytes(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given Java array of bytes. The
     * driver converts this to a SQL {@code VARBINARY} or
     * {@code LONGVARBINARY} (depending on the argument's size
     * relative to the driver's limits on {@code VARBINARY} values)
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBytes(String, byte[])
     * @see #setBytes(int[], byte[])
     * @since 1.0
     */
    public DbPreparedStatement setBytes(int parameterIndex, byte x[])
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setBytes(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the value of the named parameters with the given object. The
     * second argument must be an object type; for integral values, the
     * {@code java.lang} equivalent objects should be used.
     * <p>The given Java object will be converted to the given
     * targetSqlType before being sent to the database.
     * <p>If the object has a custom mapping (is of a class implementing
     * the interface {@code SQLData}), the JDBC driver should call
     * the method {@code SQLData.writeSQL} to write it to the SQL
     * data stream. If, on the other hand, the object is of a class
     * implementing {@code Ref}, {@code Blob}, {@code Clob},
     * {@code Struct}, or {@code Array}, the driver should pass
     * it to the database as a value of the corresponding SQL type.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     * <p>Note that this method may be used to pass database-specific
     * abstract data types.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to
     *                      be sent to the database. The scale argument may further qualify
     *                      this type.
     * @param scale         for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC
     *                      types, this is the number of digits after the decimal point. For
     *                      all other types, this value will be ignored.
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see java.sql.Types
     * @see #setObjects(int[], Object, int, int)
     * @see #setObject(int, Object, int, int)
     * @since 1.0
     */
    public DbPreparedStatement setObject(String parameterName, Object x, int targetSqlType, int scale)
    throws DatabaseException {
        setObjects(getParameterIndices(parameterName), x, targetSqlType, scale);

        return this;
    }

    /**
     * Sets the value of the designated parameters with the given object.
     * The second argument must be an object type; for integral values,
     * the {@code java.lang} equivalent objects should be used.
     * <p>The given Java object will be converted to the given
     * targetSqlType before being sent to the database.
     * <p>If the object has a custom mapping (is of a class implementing
     * the interface {@code SQLData}), the JDBC driver should call
     * the method {@code SQLData.writeSQL} to write it to the SQL
     * data stream. If, on the other hand, the object is of a class
     * implementing {@code Ref}, {@code Blob}, {@code Clob},
     * {@code Struct}, or {@code Array}, the driver should pass
     * it to the database as a value of the corresponding SQL type.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p>Note that this method may be used to pass database-specific
     * abstract data types.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the object containing the input parameter value
     * @param targetSqlType    the SQL type (as defined in java.sql.Types) to
     *                         be sent to the database. The scale argument may further qualify
     *                         this type.
     * @param scale            for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC
     *                         types, this is the number of digits after the decimal point. For
     *                         all other types, this value will be ignored.
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject(String, Object, int, int)
     * @see #setObject(int, Object, int, int)
     * @since 1.0
     */
    public DbPreparedStatement setObjects(int[] parameterIndices, Object x, int targetSqlType, int scale)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setObject(parameter_index, x, targetSqlType, scale);
        }

        return this;
    }

    /**
     * Sets the value of the designated parameter with the given object.
     * The second argument must be an object type; for integral values,
     * the {@code java.lang} equivalent objects should be used.
     * <p>The given Java object will be converted to the given
     * targetSqlType before being sent to the database.
     * <p>If the object has a custom mapping (is of a class implementing
     * the interface {@code SQLData}), the JDBC driver should call
     * the method {@code SQLData.writeSQL} to write it to the SQL
     * data stream. If, on the other hand, the object is of a class
     * implementing {@code Ref}, {@code Blob}, {@code Clob},
     * {@code Struct}, or {@code Array}, the driver should pass
     * it to the database as a value of the corresponding SQL type.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p>Note that this method may be used to pass database-specific
     * abstract data types.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the object containing the input parameter value
     * @param targetSqlType  the SQL type (as defined in java.sql.Types) to
     *                       be sent to the database. The scale argument may further qualify
     *                       this type.
     * @param scale          for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC
     *                       types, this is the number of digits after the decimal point. For
     *                       all other types, this value will be ignored.
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject(String, Object, int, int)
     * @see #setObjects(int[], Object, int, int)
     * @since 1.0
     */
    public DbPreparedStatement setObject(int parameterIndex, Object x, int targetSqlType, int scale)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " targetSqlType:" + targetSqlType + " scale:" + scale);
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setObject(parameterIndex, x, targetSqlType, scale);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the value of the named parameters with the given object. This
     * method is like the method {@code setObject} above, except that
     * it assumes a scale of zero.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to
     *                      be sent to the database
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see java.sql.Types
     * @see #setObjects(int[], Object, int)
     * @see #setObject(int, Object, int)
     * @since 1.0
     */
    public DbPreparedStatement setObject(String parameterName, Object x, int targetSqlType)
    throws DatabaseException {
        setObjects(getParameterIndices(parameterName), x, targetSqlType);

        return this;
    }

    /**
     * Sets the value of the designated parameters with the given object.
     * This method is like the method {@code setObject} above, except
     * that it assumes a scale of zero.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the object containing the input parameter value
     * @param targetSqlType    the SQL type (as defined in java.sql.Types) to
     *                         be sent to the database
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject(String, Object, int)
     * @see #setObject(int, Object, int)
     * @since 1.0
     */
    public DbPreparedStatement setObjects(int[] parameterIndices, Object x, int targetSqlType)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setObject(parameter_index, x, targetSqlType);
        }

        return this;
    }

    /**
     * Sets the value of the designated parameter with the given object.
     * This method is like the method {@code setObject} above, except
     * that it assumes a scale of zero.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the object containing the input parameter value
     * @param targetSqlType  the SQL type (as defined in java.sql.Types) to
     *                       be sent to the database
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject(String, Object, int)
     * @see #setObjects(int[], Object, int)
     * @since 1.0
     */
    public DbPreparedStatement setObject(int parameterIndex, Object x, int targetSqlType)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " targetSqlType:" + targetSqlType);
            } else {
                parameterIndex = real_index;
            }
        }

        try {
            ((PreparedStatement) statement_).setObject(parameterIndex, x, targetSqlType);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given {@code java.sql.Time}
     * value. The driver converts this to a SQL {@code TIME} value
     * when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setTimes(int[], Time)
     * @see #setTime(int, Time)
     * @since 1.0
     */
    public DbPreparedStatement setTime(String parameterName, Time x)
    throws DatabaseException {
        setTimes(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.sql.Time} value. The driver converts this to a SQL
     * {@code TIME} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTime(String, Time)
     * @see #setTime(int, Time)
     * @since 1.0
     */
    public DbPreparedStatement setTimes(int[] parameterIndices, Time x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setTime(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.sql.Time} value. The driver converts this to a SQL
     * {@code TIME} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTime(String, Time)
     * @see #setTimes(int[], Time)
     * @since 1.0
     */
    public DbPreparedStatement setTime(int parameterIndex, Time x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setTime(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given {@code java.sql.Time}
     * value, using the given {@code Calendar} object. The driver
     * uses the {@code Calendar} object to construct an SQL
     * {@code TIME} value, which the driver then sends to the
     * database. With a {@code Calendar} object, the driver can
     * calculate the time taking into account a custom timezone. If no
     * {@code Calendar} object is specified, the driver uses the
     * default timezone, which is that of the virtual machine running the
     * application.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @param cal           the {@code Calendar} object the driver will use to
     *                      construct the time
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setTimes(int[], Time, Calendar)
     * @see #setTime(int, Time, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setTime(String parameterName, Time x, Calendar cal)
    throws DatabaseException {
        setTimes(getParameterIndices(parameterName), x, cal);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.sql.Time} value, using the given
     * {@code Calendar} object. The driver uses the
     * {@code Calendar} object to construct an SQL {@code TIME}
     * value, which the driver then sends to the database. With a
     * {@code Calendar} object, the driver can calculate the time
     * taking into account a custom timezone. If no {@code Calendar}
     * object is specified, the driver uses the default timezone, which is
     * that of the virtual machine running the application.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @param cal              the {@code Calendar} object the driver will use to
     *                         construct the time
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTime(String, Time, Calendar)
     * @see #setTime(int, Time, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setTimes(int[] parameterIndices, Time x, Calendar cal)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setTime(parameter_index, x, cal);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.sql.Time} value, using the given
     * {@code Calendar} object. The driver uses the
     * {@code Calendar} object to construct an SQL {@code TIME}
     * value, which the driver then sends to the database. With a
     * {@code Calendar} object, the driver can calculate the time
     * taking into account a custom timezone. If no {@code Calendar}
     * object is specified, the driver uses the default timezone, which is
     * that of the virtual machine running the application.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @param cal            the {@code Calendar} object the driver will use to
     *                       construct the time
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTime(String, Time, Calendar)
     * @see #setTimes(int[], Time, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setTime(int parameterIndex, Time x, Calendar cal)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " with " + cal.getClass().getName());
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setTime(parameterIndex, x, cal);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given
     * {@code java.sql.Timestamp} value. The driver converts this to
     * a SQL {@code TIMESTAMP} value when it sends it to the
     * database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setTimestamps(int[], Timestamp)
     * @see #setTimestamp(int, Timestamp)
     * @since 1.0
     */
    public DbPreparedStatement setTimestamp(String parameterName, Timestamp x)
    throws DatabaseException {
        setTimestamps(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.sql.Timestamp} value. The driver converts this to
     * a SQL {@code TIMESTAMP} value when it sends it to the
     * database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTimestamp(String, Timestamp)
     * @see #setTimestamp(int, Timestamp)
     * @since 1.0
     */
    public DbPreparedStatement setTimestamps(int[] parameterIndices, Timestamp x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setTimestamp(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.sql.Timestamp} value. The driver converts this to
     * a SQL {@code TIMESTAMP} value when it sends it to the
     * database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTimestamp(String, Timestamp)
     * @see #setTimestamps(int[], Timestamp)
     * @since 1.0
     */
    public DbPreparedStatement setTimestamp(int parameterIndex, Timestamp x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setTimestamp(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given
     * {@code java.sql.Timestamp} value, using the given
     * {@code Calendar} object. The driver uses the
     * {@code Calendar} object to construct an SQL
     * {@code TIMESTAMP} value, which the driver then sends to the
     * database. With a {@code Calendar} object, the driver can
     * calculate the timestamp taking into account a custom timezone. If
     * no {@code Calendar} object is specified, the driver uses the
     * default timezone, which is that of the virtual machine running the
     * application.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the parameter value
     * @param cal           the {@code Calendar} object the driver will use to
     *                      construct the timestamp
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setTimestamps(int[], Timestamp, Calendar)
     * @see #setTimestamp(int, Timestamp, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setTimestamp(String parameterName, Timestamp x, Calendar cal)
    throws DatabaseException {
        setTimestamps(getParameterIndices(parameterName), x, cal);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.sql.Timestamp} value, using the given
     * {@code Calendar} object. The driver uses the
     * {@code Calendar} object to construct an SQL
     * {@code TIMESTAMP} value, which the driver then sends to the
     * database. With a {@code Calendar} object, the driver can
     * calculate the timestamp taking into account a custom timezone. If
     * no {@code Calendar} object is specified, the driver uses the
     * default timezone, which is that of the virtual machine running the
     * application.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the parameter value
     * @param cal              the {@code Calendar} object the driver will use to
     *                         construct the timestamp
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTimestamp(String, Timestamp, Calendar)
     * @see #setTimestamp(int, Timestamp, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setTimestamps(int[] parameterIndices, Timestamp x, Calendar cal)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setTimestamp(parameter_index, x, cal);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.sql.Timestamp} value, using the given
     * {@code Calendar} object. The driver uses the
     * {@code Calendar} object to construct an SQL
     * {@code TIMESTAMP} value, which the driver then sends to the
     * database. With a {@code Calendar} object, the driver can
     * calculate the timestamp taking into account a custom timezone. If
     * no {@code Calendar} object is specified, the driver uses the
     * default timezone, which is that of the virtual machine running the
     * application.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the parameter value
     * @param cal            the {@code Calendar} object the driver will use to
     *                       construct the timestamp
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setTimestamp(String, Timestamp, Calendar)
     * @see #setTimestamps(int[], Timestamp, Calendar)
     * @since 1.0
     */
    public DbPreparedStatement setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " with " + cal.getClass().getName());
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setTimestamp(parameterIndex, x, cal);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameter to the given input stream, which will have
     * the specified number of bytes. When a very large ASCII value is
     * input to a {@code LONGVARCHAR} parameter, it may be more
     * practical to send it via a {@code java.io.InputStream}. Data
     * will be read from the stream as needed until end-of-file is
     * reached. The JDBC driver will do any necessary conversion from
     * ASCII to the database char format.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     * <p><b>Note:</b> This stream object can either be a standard Java
     * stream object or your own subclass that implements the standard
     * interface.
     *
     * @param parameterName the name of the parameter that will be set
     *                      (the first parameter with the name will be used)
     * @param x             the Java input stream that contains the ASCII parameter
     *                      value
     * @param length        the number of bytes in the stream
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setAsciiStream(int, InputStream, int)
     * @since 1.0
     */
    public DbPreparedStatement setAsciiStream(String parameterName, InputStream x, int length)
    throws DatabaseException {
        setAsciiStream(getParameterIndices(parameterName)[0], x, length);

        return this;
    }

    /**
     * Sets the designated parameter to the given input stream, which will
     * have the specified number of bytes. When a very large ASCII value
     * is input to a {@code LONGVARCHAR} parameter, it may be more
     * practical to send it via a {@code java.io.InputStream}. Data
     * will be read from the stream as needed until end-of-file is
     * reached. The JDBC driver will do any necessary conversion from
     * ASCII to the database char format.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p><b>Note:</b> This stream object can either be a standard Java
     * stream object or your own subclass that implements the standard
     * interface.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the Java input stream that contains the ASCII parameter
     *                       value
     * @param length         the number of bytes in the stream
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setAsciiStream(String, InputStream, int)
     * @since 1.0
     */
    public DbPreparedStatement setAsciiStream(int parameterIndex, InputStream x, int length)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " length:" + length);
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setAsciiStream(parameterIndex, x, length);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameter to the given {@code Reader} object,
     * which is the given number of characters long. When a very large
     * UNICODE value is input to a {@code LONGVARCHAR} parameter, it
     * may be more practical to send it via a {@code java.io.Reader}
     * object. The data will be read from the stream as needed until
     * end-of-file is reached. The JDBC driver will do any necessary
     * conversion from UNICODE to the database char format.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     * <p><b>Note:</b> This stream object can either be a standard Java
     * stream object or your own subclass that implements the standard
     * interface.
     *
     * @param parameterName the name of the parameter that will be set
     *                      (the first parameter with the name will be used)
     * @param x             the {@code java.io.Reader} object that contains
     *                      the Unicode data
     * @param length        the number of characters in the stream
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setCharacterStream(int, Reader, int)
     * @since 1.0
     */
    public DbPreparedStatement setCharacterStream(String parameterName, Reader x, int length)
    throws DatabaseException {
        setCharacterStream(getParameterIndices(parameterName)[0], x, length);

        return this;
    }

    /**
     * Sets the designated parameter to the given {@code Reader}
     * object, which is the given number of characters long. When a very
     * large UNICODE value is input to a {@code LONGVARCHAR}
     * parameter, it may be more practical to send it via a
     * {@code java.io.Reader} object. The data will be read from the
     * stream as needed until end-of-file is reached. The JDBC driver will
     * do any necessary conversion from UNICODE to the database char
     * format.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p><b>Note:</b> This stream object can either be a standard Java
     * stream object or your own subclass that implements the standard
     * interface.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the {@code java.io.Reader} object that contains
     *                       the Unicode data
     * @param length         the number of characters in the stream
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setCharacterStream(String, Reader, int)
     * @since 1.0
     */
    public DbPreparedStatement setCharacterStream(int parameterIndex, Reader x, int length)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " length:" + length);
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setCharacterStream(parameterIndex, x, length);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameter to the given input stream, which will have
     * the specified number of bytes. When a very large binary value is
     * input to a {@code LONGVARBINARY} parameter, it may be more
     * practical to send it via a {@code java.io.InputStream} object.
     * The data will be read from the stream as needed until end-of-file
     * is reached.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     * <p><b>Note:</b> This stream object can either be a standard Java
     * stream object or your own subclass that implements the standard
     * interface.
     *
     * @param parameterName the name of the parameter that will be set
     *                      (the first parameter with the name will be used)
     * @param x             the java input stream which contains the binary parameter
     *                      value
     * @param length        the number of bytes in the stream
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setBinaryStream(int, InputStream, int)
     * @since 1.0
     */
    public DbPreparedStatement setBinaryStream(String parameterName, InputStream x, int length)
    throws DatabaseException {
        setBinaryStream(getParameterIndices(parameterName)[0], x, length);

        return this;
    }

    /**
     * Sets the designated parameter to the given input stream, which will
     * have the specified number of bytes. When a very large binary value
     * is input to a {@code LONGVARBINARY} parameter, it may be more
     * practical to send it via a {@code java.io.InputStream} object.
     * The data will be read from the stream as needed until end-of-file
     * is reached.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     * <p><b>Note:</b> This stream object can either be a standard Java
     * stream object or your own subclass that implements the standard
     * interface.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the java input stream which contains the binary parameter
     *                       value
     * @param length         the number of bytes in the stream
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBinaryStream(String, InputStream, int)
     * @since 1.0
     */
    public DbPreparedStatement setBinaryStream(int parameterIndex, InputStream x, int length)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName() + " length:" + length);
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setBinaryStream(parameterIndex, x, length);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given {@code Array} object.
     * The driver converts this to a SQL {@code ARRAY} value when it
     * sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameter that will be set
     * @param x             an {@code Array} object that maps an SQL
     *                      {@code ARRAY} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setArray(int, Array)
     * @since 1.0
     */
    public DbPreparedStatement setArray(String parameterName, Array x)
    throws DatabaseException {
        setArray(getParameterIndices(parameterName)[0], x);

        return this;
    }

    /**
     * Sets the designated parameter to the given {@code Array}
     * object. The driver converts this to a SQL {@code ARRAY} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              an {@code Array} object that maps an SQL
     *                       {@code ARRAY} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setArray(String, Array)
     * @since 1.0
     */
    public DbPreparedStatement setArray(int parameterIndex, Array x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName());
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setArray(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the value of the named parameters using the given object. The
     * second parameter must be of type {@code Object}; therefore,
     * the {@code java.lang} equivalent objects should be used for
     * built-in types.
     * <p>The JDBC specification specifies a standard mapping from Java
     * {@code Object} types to SQL types. The given argument will be
     * converted to the corresponding SQL type before being sent to the
     * database.
     * <p>Note that this method may be used to pass datatabase-specific
     * abstract data types, by using a driver-specific Java type.
     * <p>If the object is of a class implementing the interface
     * {@code SQLData}, the JDBC driver should call the method
     * {@code SQLData.writeSQL} to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * {@code Ref}, {@code Blob}, {@code Clob},
     * {@code Struct}, or {@code Array}, the driver should pass
     * it to the database as a value of the corresponding SQL type.
     * <p>This method throws an exception if there is an ambiguity, for
     * example, if the object is of a class implementing more than one of
     * the interfaces named above.
     * <p>If such an ambiquity exception is thrown or if a database access
     * error occurs, this {@code DbPreparedStatement} instance is
     * automatically closed.
     *
     * @param parameterName the name of the parameter that will be set
     * @param x             the object containing the input parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs, or if the type of the given object is
     *                           ambiguous.
     * @see #setObjects(int[], Object)
     * @see #setObject(int, Object)
     * @since 1.0
     */
    public DbPreparedStatement setObject(String parameterName, Object x)
    throws DatabaseException {
        setObjects(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the value of the designated parameters using the given object.
     * The second parameter must be of type {@code Object};
     * therefore, the {@code java.lang} equivalent objects should be
     * used for built-in types.
     * <p>The JDBC specification specifies a standard mapping from Java
     * {@code Object} types to SQL types. The given argument will be
     * converted to the corresponding SQL type before being sent to the
     * database.
     * <p>Note that this method may be used to pass datatabase-specific
     * abstract data types, by using a driver-specific Java type.
     * <p>If the object is of a class implementing the interface
     * {@code SQLData}, the JDBC driver should call the method
     * {@code SQLData.writeSQL} to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * {@code Ref}, {@code Blob}, {@code Clob},
     * {@code Struct}, or {@code Array}, the driver should pass
     * it to the database as a value of the corresponding SQL type.
     * <p>This method throws an exception if there is an ambiguity, for
     * example, if the object is of a class implementing more than one of
     * the interfaces named above.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the object containing the input parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs or
     *                           the type of the given object is ambiguous
     * @see #setObject(String, Object)
     * @see #setObject(int, Object)
     * @since 1.0
     */
    public DbPreparedStatement setObjects(int[] parameterIndices, Object x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setObject(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the value of the designated parameter using the given object.
     * The second parameter must be of type {@code Object};
     * therefore, the {@code java.lang} equivalent objects should be
     * used for built-in types.
     * <p>The JDBC specification specifies a standard mapping from Java
     * {@code Object} types to SQL types. The given argument will be
     * converted to the corresponding SQL type before being sent to the
     * database.
     * <p>Note that this method may be used to pass datatabase-specific
     * abstract data types, by using a driver-specific Java type.
     * <p>If the object is of a class implementing the interface
     * {@code SQLData}, the JDBC driver should call the method
     * {@code SQLData.writeSQL} to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * {@code Ref}, {@code Blob}, {@code Clob},
     * {@code Struct}, or {@code Array}, the driver should pass
     * it to the database as a value of the corresponding SQL type.
     * <p>This method throws an exception if there is an ambiguity, for
     * example, if the object is of a class implementing more than one of
     * the interfaces named above.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              the object containing the input parameter value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs or
     *                           the type of the given object is ambiguous
     * @see #setObject(String, Object)
     * @see #setObjects(int[], Object)
     * @since 1.0
     */
    public DbPreparedStatement setObject(int parameterIndex, Object x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setObject(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameter to the given
     * {@code REF(&lt;structured-type&gt;)} value. The driver
     * converts this to a SQL {@code REF} value when it sends it to
     * the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameter that will be set
     *                      (the first parameter with the name will be used)
     * @param x             an SQL {@code REF} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setRef(int, Ref)
     * @since 1.0
     */
    public DbPreparedStatement setRef(String parameterName, Ref x)
    throws DatabaseException {
        setRef(getParameterIndices(parameterName)[0], x);

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code REF(&lt;structured-type&gt;)} value. The driver
     * converts this to a SQL {@code REF} value when it sends it to
     * the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              an SQL {@code REF} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setRef(String, Ref)
     * @since 1.0
     */
    public DbPreparedStatement setRef(int parameterIndex, Ref x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName());
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setRef(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameter to the given {@code Blob} object. The
     * driver converts this to a SQL {@code BLOB} value when it sends
     * it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameter that will be set
     *                      (the first parameter with the name will be used)
     * @param x             a {@code Blob} object that maps an SQL
     *                      {@code BLOB} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setBlob(String, Blob)
     * @since 1.0
     */
    public DbPreparedStatement setBlob(String parameterName, Blob x)
    throws DatabaseException {
        setBlob(getParameterIndices(parameterName)[0], x);

        return this;
    }

    /**
     * Sets the designated parameter to the given {@code Blob}
     * object. The driver converts this to a SQL {@code BLOB} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              a {@code Blob} object that maps an SQL
     *                       {@code BLOB} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setBlob(String, Blob)
     * @since 1.0
     */
    public DbPreparedStatement setBlob(int parameterIndex, Blob x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName());
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setBlob(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameter to the given {@code Clob} object. The
     * driver converts this to a SQL {@code CLOB} value when it sends
     * it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterName the name of the parameter that will be set
     *                      (the first parameter with the name will be used)
     * @param x             a {@code Clob} object that maps an SQL
     *                      {@code CLOB} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setClob(int, Clob)
     * @since 1.0
     */
    public DbPreparedStatement setClob(String parameterName, Clob x)
    throws DatabaseException {
        setClob(getParameterIndices(parameterName)[0], x);

        return this;
    }

    /**
     * Sets the designated parameter to the given {@code Clob}
     * object. The driver converts this to a SQL {@code CLOB} value
     * when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *                       ...
     * @param x              a {@code Clob} object that maps an SQL
     *                       {@code CLOB} value
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setClob(String, Clob)
     * @since 1.0
     */
    public DbPreparedStatement setClob(int parameterIndex, Clob x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                throw new UnsupportedVirtualParameterTypeException(this, parameterIndex, x.getClass().getName());
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setClob(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Sets the named parameters to the given {@code java.net.URL}
     * value. The driver converts this to a SQL {@code DATALINK}
     * value when it sends it to the database.
     * <p>If a database access error occurs, this
     * {@code DbPreparedStatement} instance is automatically closed.
     *
     * @param parameterName the name of the parameters that have to be set
     * @param x             the {@code java.net.URL} object to be set
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException when this
     *                           {@code DbPrepareStatement} instance wasn't defined by a
     *                           {@code ParametrizedQuery} but by a regular sql string, or if
     *                           the {@code ParametrizedQuery} doesn't contain any parameters,
     *                           or if no parameters with this name could be found, or if a database
     *                           access error occurs.
     * @see #setURLs(int[], URL)
     * @see #setURL(int, URL)
     * @since 1.0
     */
    public DbPreparedStatement setURL(String parameterName, URL x)
    throws DatabaseException {
        setURLs(getParameterIndices(parameterName), x);

        return this;
    }

    /**
     * Sets the designated parameters to the given
     * {@code java.net.URL} value. The driver converts this to a SQL
     * {@code DATALINK} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndices the first parameter is 1, the second is 2,
     *                         ...
     * @param x                the {@code java.net.URL} object to be set
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setURL(String, URL)
     * @see #setURL(int, URL)
     * @since 1.0
     */
    public DbPreparedStatement setURLs(int[] parameterIndices, URL x)
    throws DatabaseException {
        if (null == parameterIndices) throw new IllegalArgumentException("parameterIndices can't be null.");

        for (int parameter_index : parameterIndices) {
            setURL(parameter_index, x);
        }

        return this;
    }

    /**
     * Sets the designated parameter to the given
     * {@code java.net.URL} value. The driver converts this to a SQL
     * {@code DATALINK} value when it sends it to the database.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x              the {@code java.net.URL} object to be set
     * @return this {@code DbPreparedStatement} instance.
     * @throws DatabaseException if a database access error occurs
     * @see #setURL(String, URL)
     * @see #setURLs(int[], URL)
     * @since 1.0
     */
    public DbPreparedStatement setURL(int parameterIndex, URL x)
    throws DatabaseException {
        // handle virtual parameters
        if (virtualParameters_ != null &&
            virtualParameters_.hasParameter(parameterIndex)) {
            int real_index = virtualParameters_.getRealIndex(parameterIndex);
            if (-1 == real_index) {
                virtualParameters_.putValue(parameterIndex, x);
                return this;
            } else {
                parameterIndex = real_index;
            }
        }

        // set the real parameter
        try {
            ((PreparedStatement) statement_).setURL(parameterIndex, x);
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }

        return this;
    }

    /**
     * Clears the current parameter values immediately.
     * <p>In general, parameter values remain in force for repeated use of
     * a statement. Setting a parameter value automatically clears its
     * previous value. However, in some cases it is useful to immediately
     * release the resources used by the current parameter values; this
     * can be done by calling the method {@code clearParameters}.
     * <p>If an exception is thrown, this {@code DbPreparedStatement}
     * is automatically closed and an ongoing transaction will be
     * automatically rolled back if it belongs to the executing thread.
     *
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public void clearParameters()
    throws DatabaseException {
        try {
            ((PreparedStatement) statement_).clearParameters();
        } catch (SQLException e) {
            handleException();
            throw new DatabaseException(e);
        }
    }
}
