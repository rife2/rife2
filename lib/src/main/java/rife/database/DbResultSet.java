/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import java.sql.*;

import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.MissingResultsException;
import rife.database.exceptions.RowIndexOutOfBoundsException;
import rife.tools.ExceptionUtils;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;

public class DbResultSet implements ResultSet, Cloneable {
    protected DbStatement statement_;
    protected ResultSet resultSet_;
    protected boolean firstRowSkew_ = false;
    protected boolean hasResultRows_ = false;

    DbResultSet(DbStatement statement, ResultSet resultSet) {
        assert statement != null;
        assert resultSet != null;

        statement_ = statement;
        resultSet_ = resultSet;
    }

    public final boolean next()
    throws SQLException {
        if (firstRowSkew_) {
            firstRowSkew_ = false;
            return true;
        } else if (resultSet_.next()) {
            hasResultRows_ = true;
            firstRowSkew_ = false;
            return true;
        }

        return false;
    }

    public final boolean previous()
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        if (resultSet_.previous()) {
            hasResultRows_ = true;
            firstRowSkew_ = false;
            return true;
        }

        return false;
    }

    public final boolean absolute(int row)
    throws SQLException {
        if (resultSet_.absolute(row)) {
            hasResultRows_ = true;
            firstRowSkew_ = false;
            return true;
        }

        return false;
    }

    public final boolean relative(int rows)
    throws SQLException {
        if (firstRowSkew_) {
            if (resultSet_.relative(rows - 1)) {
                firstRowSkew_ = false;
            }
        } else if (resultSet_.relative(rows)) {
            hasResultRows_ = true;
            firstRowSkew_ = false;
            return true;
        }

        return false;
    }

    public final void beforeFirst()
    throws SQLException {
        firstRowSkew_ = false;
        resultSet_.beforeFirst();
    }

    public final boolean first()
    throws SQLException {
        if (resultSet_.first()) {
            hasResultRows_ = true;
            firstRowSkew_ = false;
            return true;
        }

        return false;
    }

    public final boolean last()
    throws SQLException {
        if (resultSet_.last()) {
            hasResultRows_ = true;
            firstRowSkew_ = false;
            return true;
        }

        return false;
    }

    public final void afterLast()
    throws SQLException {
        resultSet_.afterLast();
    }

    public final void moveToInsertRow()
    throws SQLException {
        resultSet_.moveToInsertRow();
    }

    public final void moveToCurrentRow()
    throws SQLException {
        resultSet_.moveToCurrentRow();
    }

    public final boolean isBeforeFirst()
    throws SQLException {
        if (firstRowSkew_) {
            return true;
        }

        return resultSet_.isBeforeFirst();
    }

    public final boolean isFirst()
    throws SQLException {
        if (firstRowSkew_) {
            return false;
        }

        return resultSet_.isFirst();
    }

    public final boolean isLast()
    throws SQLException {
        if (firstRowSkew_) {
            return false;
        }

        return resultSet_.isLast();
    }

    public final boolean isAfterLast()
    throws SQLException {
        if (firstRowSkew_) {
            return false;
        }

        return resultSet_.isAfterLast();
    }

    public final int getRow()
    throws SQLException {
        if (firstRowSkew_) {
            return resultSet_.getRow() - 1;
        } else {
            return resultSet_.getRow();
        }
    }

    public final void refreshRow()
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.refreshRow();
    }

    public final void insertRow()
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.insertRow();
    }

    public final void updateRow()
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateRow();
    }

    public final void deleteRow()
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.deleteRow();
    }

    public final boolean rowInserted()
    throws SQLException {
        return resultSet_.rowInserted();
    }

    public final boolean rowUpdated()
    throws SQLException {
        return resultSet_.rowUpdated();
    }

    public final boolean rowDeleted()
    throws SQLException {
        return resultSet_.rowDeleted();
    }

    public final void close()
    throws SQLException {
        statement_ = null;
        if (resultSet_ != null) {
            resultSet_.close();
        }
        firstRowSkew_ = false;
        hasResultRows_ = false;
    }

    public final boolean wasNull()
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.wasNull();
    }

    public final void setFetchDirection(int direction)
    throws SQLException {
        resultSet_.setFetchDirection(direction);
    }

    public final void setFetchSize(int rows)
    throws SQLException {
        resultSet_.setFetchSize(rows);
    }

    public final void cancelRowUpdates()
    throws SQLException {
        resultSet_.cancelRowUpdates();
    }

    public final ResultSetMetaData getMetaData()
    throws SQLException {
        return resultSet_.getMetaData();
    }

    public final int getConcurrency()
    throws SQLException {
        return resultSet_.getConcurrency();
    }

    public final int getFetchDirection()
    throws SQLException {
        return resultSet_.getFetchDirection();
    }

    public final int getFetchSize()
    throws SQLException {
        return resultSet_.getFetchSize();
    }

    public final void clearWarnings()
    throws SQLException {
        resultSet_.clearWarnings();
    }

    public final SQLWarning getWarnings()
    throws SQLException {
        return resultSet_.getWarnings();
    }

    public final String getCursorName()
    throws SQLException {
        return resultSet_.getCursorName();
    }

    public final Statement getStatement()
    throws SQLException {
        return resultSet_.getStatement();
    }

    public final int getType()
    throws SQLException {
        return resultSet_.getType();
    }

    public final int findColumn(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.findColumn(columnName);
    }

    /**
     * Determines if there are rows available in the <code>ResultSet</code>
     * object that was returned by an <code>execute</code> method.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return <code>true</code> if there are result rows available; or
     * <p>
     * <code>false</code> if no <code>ResultSet</code> object was available or
     * it didn't have any result rows.
     * @throws DatabaseException if a database access error occurs
     * @since 1.0
     */
    public boolean hasResultRows()
    throws DatabaseException {
        try {
            if (resultSet_ != null) {
                if (hasResultRows_) {
                    return true;
                }

                if (firstRowSkew_) {
                    return true;
                }

                if (next()) {
                    firstRowSkew_ = true;
                    hasResultRows_ = true;
                    return true;
                }
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        return false;
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a string. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>String</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public String getFirstString()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getString(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a boolean. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>boolean</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public boolean getFirstBoolean()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getBoolean(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a byte. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>byte</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public byte getFirstByte()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getByte(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a short. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>short</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public short getFirstShort()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getShort(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as an integer. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>int</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public int getFirstInt()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getInt(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a long. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>long</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public long getFirstLong()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getLong(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a float. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>float</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public float getFirstFloat()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getFloat(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a double. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>String</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public double getFirstDouble()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getDouble(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a big decimal. This method works both when
     * the <code>next</code> method has never been called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>BigDecimal</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public BigDecimal getFirstBigDecimal()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getBigDecimal(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as an array of bytes. This method works
     * both when the <code>next</code> method has never been called or once been
     * called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>byte[]</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public byte[] getFirstBytes()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getBytes(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a sql date. This method works both
     * when the <code>next</code> method has never been called or once been
     * called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>java.sql.Date</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public java.sql.Date getFirstDate()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getDate(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a sql date. This method uses the given
     * calendar to construct an appropriate millisecond value for the date if
     * the underlying database does not store timezone information.
     * This method works both when the <code>next</code> method has never been
     * called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param cal the <code>java.util.Calendar</code> object
     *            to use in constructing the date
     * @return the first <code>java.sql.Date</code> object in the resultsn;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming language
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @since 1.0
     */
    public java.sql.Date getFirstDate(Calendar cal)
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getDate(1, cal);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a sql time. This method works both
     * when the <code>next</code> method has never been called or once been
     * called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>java.sql.Time</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public java.sql.Time getFirstTime()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getTime(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a sql time. This method works both
     * when the <code>next</code> method has never been called or once been
     * called. This method uses the given calendar to construct an appropriate
     * millisecond value for the time if the underlying database does not store
     * timezone information.
     * This method works both when the <code>next</code> method has never been
     * called or once been called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param cal the <code>java.util.Calendar</code> object to use in
     *            constructing the time
     * @return the first <code>java.sql.Time</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public java.sql.Time getFirstTime(Calendar cal)
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getTime(1, cal);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a sql timestamo. This method works both
     * when the <code>next</code> method has never been called or once been
     * called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>java.sql.Timestamp</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public java.sql.Timestamp getFirstTimestamp()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getTimestamp(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a sql timestamp. This method uses the
     * given calendar to construct an appropriate millisecond value for the
     * timestamp if the underlying database does not store timezone information.
     * This method works both when the <code>next</code> method has never been
     * called or once been called.
     * <p>
     * It is perfectly usable after the <code>hasResultRows</code> method or
     * alone where catching the <code>MissingResultsException</code> is used to
     * indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @param cal the <code>java.util.Calendar</code> object to use in
     *            constructing the date
     * @return the first <code>java.sql.Timestamp</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public java.sql.Timestamp getFirstTimestamp(Calendar cal)
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getTimestamp(1, cal);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as an ascii stream. This method works both
     * when the <code>next</code> method has never been called or once been
     * called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>java.io.InputStream</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public InputStream getFirstAsciiStream()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getAsciiStream(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a character stream. This method works
     * both when the <code>next</code> method has never been called or once been
     * called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>java.io.Reader</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public Reader getFirstCharacterStream()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getCharacterStream(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    /**
     * Retrieves the first field of the first row of this
     * <code>DbResultSet</code> object as a binary stream. This method works hiboth
     * when the <code>next</code> method has never been called or once been
     * called.
     * <p>
     * Therefore, it's thus perfectly usable after the <code>hasResultRows</code>
     * method or alone where catching the <code>MissingResultsException</code>
     * is used to indicate the absence of results.
     * <p>
     * If an exception is thrown, the related <code>DbStatement</code> is
     * automatically closed and an ongoing transaction will be automatically
     * rolled back if it belongs to the executing thread.
     *
     * @return the first <code>java.io.InputStream</code> object in the results.
     * @throws DatabaseException if a database access error occurs. If there
     *                           are no results available the thrown exception is
     *                           {@link MissingResultsException}.
     * @see #hasResultRows
     * @since 1.0
     */
    public InputStream getFirstBinaryStream()
    throws DatabaseException {
        try {
            if (resultSet_ != null &&
                (isFirst() || (isBeforeFirst() && next()))) {
                return getBinaryStream(1);
            }
        } catch (SQLException e) {
            statement_.handleException();
            throw new DatabaseException(e);
        }

        throw new MissingResultsException(statement_.getConnection().getDatasource());
    }

    public final String getString(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getString(columnIndex);
    }

    public final String getString(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getString(columnName);
    }

    public final boolean getBoolean(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBoolean(columnIndex);
    }

    public final boolean getBoolean(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBoolean(columnName);
    }

    public final byte getByte(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getByte(columnIndex);
    }

    public final byte getByte(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getByte(columnName);
    }

    public final short getShort(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getShort(columnIndex);
    }

    public final short getShort(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getShort(columnName);
    }

    public final int getInt(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getInt(columnIndex);
    }

    public final int getInt(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getInt(columnName);
    }

    public final long getLong(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getLong(columnIndex);
    }

    public final long getLong(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getLong(columnName);
    }

    public final float getFloat(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getFloat(columnIndex);
    }

    public final float getFloat(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getFloat(columnName);
    }

    public final double getDouble(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getDouble(columnIndex);
    }

    public final double getDouble(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getDouble(columnName);
    }

    public final BigDecimal getBigDecimal(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBigDecimal(columnIndex);
    }

    public final BigDecimal getBigDecimal(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBigDecimal(columnName);
    }

    public final BigDecimal getBigDecimal(int columnIndex, int scale)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBigDecimal(columnIndex, scale);
    }

    public final BigDecimal getBigDecimal(String columnName, int scale)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBigDecimal(columnName, scale);
    }

    public final byte[] getBytes(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBytes(columnIndex);
    }

    public final byte[] getBytes(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBytes(columnName);
    }

    public final Date getDate(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getDate(columnIndex);
    }

    public final Date getDate(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getDate(columnName);
    }

    public final Date getDate(int columnIndex, Calendar cal)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getDate(columnIndex, cal);
    }

    public final Date getDate(String columnName, Calendar cal)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getDate(columnName, cal);
    }

    public final Time getTime(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTime(columnIndex);
    }

    public final Time getTime(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTime(columnName);
    }

    public final Time getTime(int columnIndex, Calendar cal)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTime(columnIndex, cal);
    }

    public final Time getTime(String columnName, Calendar cal)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTime(columnName, cal);
    }

    public final Timestamp getTimestamp(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTimestamp(columnIndex);
    }

    public final Timestamp getTimestamp(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTimestamp(columnName);
    }

    public final Timestamp getTimestamp(int columnIndex, Calendar cal)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTimestamp(columnIndex, cal);
    }

    public final Timestamp getTimestamp(String columnName, Calendar cal)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getTimestamp(columnName, cal);
    }

    public final InputStream getAsciiStream(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getAsciiStream(columnIndex);
    }

    public final InputStream getAsciiStream(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getAsciiStream(columnName);
    }

    public final InputStream getUnicodeStream(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getUnicodeStream(columnIndex);
    }

    public final InputStream getUnicodeStream(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getUnicodeStream(columnName);
    }

    public final Reader getCharacterStream(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getCharacterStream(columnIndex);
    }

    public final Reader getCharacterStream(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getCharacterStream(columnName);
    }

    public final InputStream getBinaryStream(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBinaryStream(columnIndex);
    }

    public final InputStream getBinaryStream(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBinaryStream(columnName);
    }

    public final Ref getRef(String colName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getRef(colName);
    }

    public final Ref getRef(int i)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getRef(i);
    }

    public final Object getObject(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getObject(columnIndex);
    }

    public final Object getObject(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getObject(columnName);
    }

    public final Object getObject(int i, Map map)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getObject(i, map);
    }

    public final Object getObject(String colName, Map map)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getObject(colName, map);
    }

    public final <T> T getObject(int columnIndex, Class<T> type)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getObject(columnIndex, type);
    }

    public final <T> T getObject(String columnLabel, Class<T> type)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getObject(columnLabel, type);
    }

    public final Blob getBlob(int i)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBlob(i);
    }

    public final Blob getBlob(String colName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getBlob(colName);
    }

    public final Clob getClob(int i)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getClob(i);
    }

    public final Clob getClob(String colName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getClob(colName);
    }

    public final Array getArray(String colName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getArray(colName);
    }

    public final Array getArray(int i)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getArray(i);
    }

    public final URL getURL(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getURL(columnIndex);
    }

    public final URL getURL(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getURL(columnName);
    }

    public final void updateNull(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNull(columnIndex);
    }

    public final void updateNull(String columnName)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNull(columnName);
    }

    public final void updateString(int columnIndex, String x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateString(columnIndex, x);
    }

    public final void updateString(String columnName, String x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateString(columnName, x);
    }

    public final void updateBoolean(int columnIndex, boolean x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBoolean(columnIndex, x);
    }

    public final void updateBoolean(String columnName, boolean x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBoolean(columnName, x);
    }

    public final void updateByte(int columnIndex, byte x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateByte(columnIndex, x);
    }

    public final void updateByte(String columnName, byte x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateByte(columnName, x);
    }

    public final void updateShort(int columnIndex, short x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateShort(columnIndex, x);
    }

    public final void updateShort(String columnName, short x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateShort(columnName, x);
    }

    public final void updateInt(int columnIndex, int x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateInt(columnIndex, x);
    }

    public final void updateInt(String columnName, int x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateInt(columnName, x);
    }

    public final void updateLong(int columnIndex, long x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateLong(columnIndex, x);
    }

    public final void updateLong(String columnName, long x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateLong(columnName, x);
    }

    public final void updateFloat(int columnIndex, float x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateFloat(columnIndex, x);
    }

    public final void updateFloat(String columnName, float x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateFloat(columnName, x);
    }

    public final void updateDouble(int columnIndex, double x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateDouble(columnIndex, x);
    }

    public final void updateDouble(String columnName, double x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateDouble(columnName, x);
    }

    public final void updateBigDecimal(int columnIndex, BigDecimal x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBigDecimal(columnIndex, x);
    }

    public final void updateBigDecimal(String columnName, BigDecimal x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBigDecimal(columnName, x);
    }

    public final void updateBytes(int columnIndex, byte[] x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBytes(columnIndex, x);
    }

    public final void updateBytes(String columnName, byte[] x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBytes(columnName, x);
    }

    public final void updateDate(int columnIndex, Date x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateDate(columnIndex, x);
    }

    public final void updateDate(String columnName, Date x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateDate(columnName, x);
    }

    public final void updateTime(int columnIndex, Time x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateTime(columnIndex, x);
    }

    public final void updateTime(String columnName, Time x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateTime(columnName, x);
    }

    public final void updateTimestamp(int columnIndex, Timestamp x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateTimestamp(columnIndex, x);
    }

    public final void updateTimestamp(String columnName, Timestamp x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateTimestamp(columnName, x);
    }

    public final void updateAsciiStream(int columnIndex, InputStream x, int length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateAsciiStream(columnIndex, x, length);
    }

    public final void updateAsciiStream(String columnName, InputStream x, int length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateAsciiStream(columnName, x, length);
    }

    public final void updateCharacterStream(int columnIndex, Reader x, int length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateCharacterStream(columnIndex, x, length);
    }

    public final void updateCharacterStream(String columnName, Reader reader, int length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateCharacterStream(columnName, reader, length);
    }

    public final void updateBinaryStream(int columnIndex, InputStream x, int length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBinaryStream(columnIndex, x, length);
    }

    public final void updateBinaryStream(String columnName, InputStream x, int length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBinaryStream(columnName, x, length);
    }

    public final void updateRef(int columnIndex, Ref x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateRef(columnIndex, x);
    }

    public final void updateRef(String columnName, Ref x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateRef(columnName, x);
    }

    public final void updateObject(int columnIndex, Object x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnIndex, x);
    }

    public final void updateObject(String columnName, Object x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnName, x);
    }

    public final void updateObject(int columnIndex, Object x, int scale)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnIndex, x, scale);
    }

    public final void updateObject(String columnName, Object x, int scale)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnName, x, scale);
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnLabel, x, targetSqlType, scaleOrLength);
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnIndex, x, targetSqlType);
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateObject(columnLabel, x, targetSqlType);
    }

    public final void updateBlob(int columnIndex, Blob x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBlob(columnIndex, x);
    }

    public final void updateBlob(String columnName, Blob x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBlob(columnName, x);
    }

    public final void updateClob(int columnIndex, Clob x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateClob(columnIndex, x);
    }

    public final void updateClob(String columnName, Clob x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateClob(columnName, x);
    }

    public final void updateArray(int columnIndex, Array x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateArray(columnIndex, x);
    }

    public final void updateArray(String columnName, Array x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateArray(columnName, x);
    }

    public RowId getRowId(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getRowId(columnIndex);
    }

    public RowId getRowId(String columnLabel)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getRowId(columnLabel);
    }

    public void updateRowId(int columnIndex, RowId x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateRowId(columnIndex, x);
    }

    public void updateRowId(String columnLabel, RowId x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateRowId(columnLabel, x);
    }

    public int getHoldability()
    throws SQLException {
        return resultSet_.getHoldability();
    }

    public boolean isClosed()
    throws SQLException {
        return resultSet_.isClosed();
    }

    public void updateNString(int columnIndex, String nString)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNString(columnIndex, nString);
    }

    public void updateNString(String columnLabel, String nString)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNString(columnLabel, nString);
    }

    public void updateNClob(int columnIndex, NClob nClob)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNClob(columnIndex, nClob);
    }

    public void updateNClob(String columnLabel, NClob nClob)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNClob(columnLabel, nClob);
    }

    public NClob getNClob(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getNClob(columnIndex);
    }

    public NClob getNClob(String columnLabel)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getNClob(columnLabel);
    }

    public SQLXML getSQLXML(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getSQLXML(columnIndex);
    }

    public SQLXML getSQLXML(String columnLabel)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getSQLXML(columnLabel);
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateSQLXML(columnIndex, xmlObject);
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateSQLXML(columnLabel, xmlObject);
    }

    public String getNString(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getNString(columnIndex);
    }

    public String getNString(String columnLabel)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getNString(columnLabel);
    }

    public Reader getNCharacterStream(int columnIndex)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getNCharacterStream(columnIndex);
    }

    public Reader getNCharacterStream(String columnLabel)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        return resultSet_.getNCharacterStream(columnLabel);
    }

    public void updateNCharacterStream(int columnIndex, Reader x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNCharacterStream(columnIndex, x);
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNCharacterStream(columnIndex, x, length);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNCharacterStream(columnLabel, reader);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNCharacterStream(columnLabel, reader, length);
    }

    public void updateAsciiStream(int columnIndex, InputStream x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateAsciiStream(columnIndex, x);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateAsciiStream(columnIndex, x, length);
    }

    public void updateBinaryStream(int columnIndex, InputStream x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBinaryStream(columnIndex, x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBinaryStream(columnIndex, x, length);
    }

    public void updateCharacterStream(int columnIndex, Reader x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateCharacterStream(columnIndex, x);
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateCharacterStream(columnIndex, x, length);
    }

    public void updateAsciiStream(String columnLabel, InputStream x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateAsciiStream(columnLabel, x);
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateAsciiStream(columnLabel, x, length);
    }

    public void updateBinaryStream(String columnLabel, InputStream x)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBinaryStream(columnLabel, x);
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBinaryStream(columnLabel, x, length);
    }

    public void updateCharacterStream(String columnLabel, Reader reader)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateCharacterStream(columnLabel, reader);
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateCharacterStream(columnLabel, reader, length);
    }

    public void updateBlob(int columnIndex, InputStream inputStream)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBlob(columnIndex, inputStream);
    }

    public void updateBlob(String columnLabel, InputStream inputStream)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBlob(columnLabel, inputStream);
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBlob(columnIndex, inputStream, length);
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateBlob(columnLabel, inputStream, length);
    }

    public void updateClob(int columnIndex, Reader reader)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateClob(columnIndex, reader);
    }

    public void updateClob(String columnLabel, Reader reader)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateClob(columnLabel, reader);
    }

    public void updateClob(int columnIndex, Reader reader, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateClob(columnIndex, reader, length);
    }

    public void updateClob(String columnLabel, Reader reader, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateClob(columnLabel, reader, length);
    }

    public void updateNClob(String columnLabel, Reader reader)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNClob(columnLabel, reader);
    }

    public void updateNClob(int columnIndex, Reader reader)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNClob(columnIndex, reader);
    }

    public void updateNClob(int columnIndex, Reader reader, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNClob(columnIndex, reader, length);
    }

    public void updateNClob(String columnLabel, Reader reader, long length)
    throws SQLException {
        if (firstRowSkew_) {
            throw new RowIndexOutOfBoundsException();
        }

        resultSet_.updateNClob(columnLabel, reader, length);
    }

    public <T extends Object> T unwrap(Class<T> iface)
    throws SQLException {
        return resultSet_.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface)
    throws SQLException {
        return resultSet_.isWrapperFor(iface);
    }

    /**
     * Simply clones the instance with the default clone method. This creates a
     * shallow copy of all fields and the clone will in fact just be another
     * reference to the same underlying data. The independence of each cloned
     * instance is consciously not respected since they rely on resources
     * that can't be cloned.
     *
     * @since 1.0
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.database").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }
}
