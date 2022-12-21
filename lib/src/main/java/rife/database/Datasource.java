/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.*;

import rife.database.capabilities.CapabilitiesCompensator;
import rife.database.types.SqlConversion;
import rife.tools.ExceptionUtils;
import rife.tools.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Contains all the information required to connect to a database and
 * centralizes the creation of connections to a database. These connections can
 * optionally be pooled.
 * <p>
 * The initial connection will only be made and the pool will only be
 * initialized when a connection is obtained for the first time. The
 * instantiation only stores the connection parameters.
 * <p>
 * A <code>Datasource</code> also defines the type of database that is used for
 * all database-independent logic such as sql to java and java to sql type
 * mappings, query builders, database-based authentication, database-based
 * scheduling, ... The key that identifies a supported type is the class name of
 * the jdbc driver.
 * <p>
 * A <code>Datasource</code> instance can be created through it's constructor,
 * but it's recommended to work with a <code>Datasources</code> collection
 * that is created and populated through XML. This can easily be achieved by
 * using a <code>ParticipantDatasources</code> which participates in the
 * application-wide repository.
 * <p>
 * Once a connection has been obtained from a pooled datasource, modifying its
 * connection parameters is not possible anymore, a new instance has to be
 * created to set the parameters to different values.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.database.Datasources
 * @since 1.0
 */
public class Datasource implements Cloneable {
    static HashMap<String, String> sDriverAliases = new HashMap<String, String>();
    static HashMap<String, String> sDriverNames = new HashMap<String, String>();

    static {
        sDriverAliases.put("org.gjt.mm.mysql.Driver", "com.mysql.cj.jdbc.Driver");
        sDriverAliases.put("com.mysql.jdbc.Driver", "com.mysql.cj.jdbc.Driver");
        sDriverAliases.put("oracle.jdbc.OracleDriver", "oracle.jdbc.driver.OracleDriver");
        sDriverAliases.put("org.apache.derby.jdbc.ClientDriver", "org.apache.derby.jdbc.EmbeddedDriver");

        sDriverNames.put("Apache Derby Embedded JDBC Driver", "org.apache.derby.jdbc.EmbeddedDriver");
        sDriverNames.put("Apache Derby Network Client JDBC Driver", "org.apache.derby.jdbc.EmbeddedDriver");
        sDriverNames.put("H2 JDBC Driver", "org.h2.Driver");
        sDriverNames.put("HSQL Database Engine Driver", "org.hsqldb.jdbcDriver");
        sDriverNames.put("MySQL-AB JDBC Driver", "com.mysql.cj.jdbc.Driver");
        sDriverNames.put("Oracle JDBC driver", "oracle.jdbc.driver.OracleDriver");
        sDriverNames.put("PostgreSQL Native Driver", "org.postgresql.Driver");
        sDriverNames.put("PostgreSQL JDBC Driver", "org.postgresql.Driver");
    }

    private String driver_ = null;
    private String url_ = null;
    private String user_ = null;
    private String password_ = null;
    private SqlConversion sqlConversion_ = null;
    private CapabilitiesCompensator capabilitiesCompensator_ = null;
    private ConnectionPool connectionPool_ = new ConnectionPool();
    private DataSource dataSource_ = null;

    /**
     * Instantiates a new <code>Datasource</code> object with no connection
     * information. The setters need to be used afterwards to provide each
     * required parameter before the <code>Datasource</code> can be used.
     *
     * @see #setDriver(String)
     * @see #setUrl(String)
     * @see #setUser(String)
     * @see #setPassword(String)
     * @see #setPoolSize(int)
     * @see #setDataSource(DataSource)
     * @since 1.0
     */
    public Datasource() {
    }

    /**
     * Instantiates a new <code>Datasource</code> object with all the
     * connection parameters that are required.
     *
     * @param driver   the fully-qualified classname of the jdbc driver that will
     *                 be used to connect to the database
     * @param url      the connection url which identifies the database to which the
     *                 connection will be made, this is entirely driver-dependent
     * @param user     the user that will be used to connect to the database
     * @param password the password that will be used to connect to the database
     * @param poolSize the size of the connection pool, <code>0</code> means
     *                 that the connections will not be pooled
     * @since 1.0
     */
    public Datasource(String driver, String url, String user, String password, int poolSize) {
        setDriver(driver);
        setUrl(url);
        setUser(user);
        setPassword(password);
        setPoolSize(poolSize);

        assert driver_ != null;
        assert driver_.length() > 0;
        assert url_ != null;
        assert url_.length() > 0;
    }

    /**
     * Instantiates a new <code>Datasource</code> object from a standard
     * <code>javax.sql.DataSource</code>.
     * <p>
     * The driver will be detected from the connection that is provided by this
     * <code>DataSource</code>. If the driver couldn't be detected, an exception
     * will be thrown upon connect.
     *
     * @param dataSource the standard datasource that will be used to obtain the
     *                   connection
     * @param poolSize   the size of the connection pool, <code>0</code> means
     *                   that the connections will not be pooled
     * @since 1.0
     */
    public Datasource(DataSource dataSource, int poolSize) {
        setDataSource(dataSource);
        setPoolSize(poolSize);

        assert dataSource != null;
    }

    /**
     * Instantiates a new <code>Datasource</code> object from a standard
     * <code>javax.sql.DataSource</code>.
     *
     * @param dataSource the standard datasource that will be used to obtain the
     *                   connection
     * @param driver     the fully-qualified classname of the jdbc driver that will
     *                   be used to provide an identifier for the database abstraction functionalities,
     *                   <code>null</code> will let RIFE try to figure it out by itself
     * @param user       the user that will be used to connect to the database
     * @param password   the password that will be used to connect to the database
     * @param poolSize   the size of the connection pool, <code>0</code> means
     *                   that the connections will not be pooled
     * @since 1.0
     */
    public Datasource(DataSource dataSource, String driver, String user, String password, int poolSize) {
        setDataSource(dataSource);
        driver_ = driver;
        sqlConversion_ = null;
        setUser(user);
        setPassword(password);
        setPoolSize(poolSize);

        assert dataSource != null;
    }

    /**
     * Creates a new connection by using all the parameters that have been
     * defined in the <code>Datasource</code>.
     *
     * @return the newly created <code>DbConnection</code> instance
     * @throws DatabaseException when an error occured during the creation of
     *                           the connection
     * @since 1.0
     */
    DbConnection createConnection()
    throws DatabaseException {
        Connection connection = null;

        if (this.dataSource_ != null) {
            // try to create a datasource connection
            if (null != user_ && null != password_) {
                try {
                    connection = this.dataSource_.getConnection(user_, password_);
                } catch (SQLException e) {
                    throw new ConnectionOpenErrorException(null, user_, password_, e);
                }
            } else {
                try {
                    connection = this.dataSource_.getConnection();
                } catch (SQLException e) {
                    throw new ConnectionOpenErrorException(null, e);
                }
            }

            if (null == driver_) {
                try {
                    String driver_name = connection.getMetaData().getDriverName();
                    driver_ = sDriverNames.get(driver_name);
                    if (null == driver_) {
                        throw new UnsupportedDriverNameException(driver_name);
                    }
                } catch (SQLException e) {
                    throw new DriverNameRetrievalErrorException(e);
                }
            }
        } else {

            // obtain the jdbc driver instance
            try {
                Class.forName(driver_).getDeclaredConstructor().newInstance();
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException |
                     NoSuchMethodException | InvocationTargetException e) {
                throw new DriverInstantiationErrorException(driver_, e);
            }

            // try to create a jdbc connection
            if (null != user_ &&
                null != password_) {
                try {
                    connection = DriverManager.getConnection(url_, user_, password_);
                } catch (SQLException e) {
                    throw new ConnectionOpenErrorException(url_, user_, password_, e);
                }
            } else {
                try {
                    connection = DriverManager.getConnection(url_);
                } catch (SQLException e) {
                    throw new ConnectionOpenErrorException(url_, e);
                }
            }
        }


        // returns a new DbConnection instance with contains the new jdbc
        // connection and is linked to this datasource
        return new DbConnection(connection, this);
    }

    /**
     * Retrieves a free database connection. If no connection pool is used, a
     * new <code>DbConnection</code> will always be created, otherwise the first
     * available connection in the pool will be returned.
     *
     * @return a free <code>DbConnection</code> instance which can be used to
     * create an execute statements
     * @throws DatabaseException when errors occured during the creation of a
     *                           new connection or during the obtainance of a connection from the pool
     * @since 1.0
     */
    public DbConnection getConnection()
    throws DatabaseException {
        return connectionPool_.getConnection(this);
    }

    /**
     * Retrieves the fully qualified class name of the jdbc driver that's used
     * by this <code>Datasource</code>.
     *
     * @return a <code>String</code> with the name of the jdbc driver; or
     * <p>
     * <code>null</code> if the driver hasn't been set
     * @see #setDriver(String)
     * @see #getAliasedDriver()
     * @since 1.0
     */
    public String getDriver() {
        // make sure that a JNDI connection has been made first, so that the database name can be looked up
        if (dataSource_ != null &&
            null == driver_) {
            getConnection();
        }

        return driver_;
    }

    /**
     * Retrieves the fully qualified class name of the jdbc driver that's used
     * by this <code>Datasource</code>. Instead of straight retrieval of the
     * internal value, it looks for jdbc driver aliases and changes the driver
     * classname if it's not supported by RIFE, but its alias is.
     *
     * @return a <code>String</code> with the name of the jdbc driver; or
     * <p>
     * <code>null</code> if the driver hasn't been set
     * @see #getDriver()
     * @see #setDriver(String)
     * @since 1.0
     */
    public String getAliasedDriver() {
        String driver = getDriver();
        if (null == driver) {
            return null;
        }

        String alias = sDriverAliases.get(driver);

        if (null == alias) {
            return driver;
        }

        return alias;
    }

    /**
     * Sets the jdbc driver that will be used to connect to the database. This
     * has to be a fully qualified class name and will be looked up through
     * reflection. It's not possible to change the driver after a connection
     * has been obtained from a pooled datasource.
     * <p>
     * If the class name can't be resolved, an exception is thrown during the
     * creation of the first connection.
     *
     * @param driver a <code>String</code> with the fully qualified class name
     *               of the jdbc driver that will be used
     * @see #getDriver()
     * @since 1.0
     */
    public void setDriver(String driver) {
        if (null == driver) throw new IllegalArgumentException("driver can't be null.");
        if (0 == driver.length()) throw new IllegalArgumentException("driver can't be empty.");
        if (connectionPool_.isInitialized())
            throw new IllegalArgumentException("driver can't be changed after the connection pool has been set up.");

        driver_ = driver;
        sqlConversion_ = null;
    }

    /**
     * Retrieves the standard datasource that is used by this RIFE datasource
     * to obtain a database connection.
     *
     * @return a standard <code>DataSource</code>; or
     * <p>
     * <code>null</code> if the standard datasource hasn't been set
     * @see #setDataSource(DataSource)
     * @since 1.0
     */
    public DataSource getDataSource() {
        return dataSource_;
    }

    /**
     * Sets the standard datasource that will be used to connect to the database.
     *
     * @param dataSource a standard <code>DataSource</code> that will be used
     *                   by this RIFE datasource to obtain a database connection.
     * @see #getDataSource()
     * @since 1.0
     */
    public void setDataSource(DataSource dataSource) {
        dataSource_ = dataSource;
    }

    /**
     * Retrieves the connection url that's used by this <code>Datasource</code>.
     *
     * @return a <code>String</code> with the connection url; or
     * <p>
     * <code>null</code> if the url hasn't been set
     * @see #setUrl(String)
     * @since 1.0
     */
    public String getUrl() {
        return url_;
    }

    /**
     * Sets the connection url that will be used to connect to the database.
     * It's not possible to change the url after a connection has been obtained
     * from a pooled datasource.
     *
     * @param url a <code>String</code> with the connection url that will be
     *            used
     * @see #getUrl()
     * @since 1.0
     */
    public void setUrl(String url) {
        if (null == url) throw new IllegalArgumentException("url can't be null.");
        if (0 == url.length()) throw new IllegalArgumentException("url can't be empty.");
        if (connectionPool_.isInitialized())
            throw new IllegalArgumentException("url can't be changed after the connection pool has been set up.");

        url_ = url;
    }

    /**
     * Retrieves the user that's used by this <code>Datasource</code>.
     *
     * @return a <code>String</code> with the user; or
     * <code>null</code> if the user hasn't been set
     * @see #setUser(String)
     * @since 1.0
     */
    public String getUser() {
        return user_;
    }

    /**
     * Sets the user that will be used to connect to the database.
     * It's not possible to change the user after a connection has been obtained
     * from a pooled datasource.
     *
     * @param user a <code>String</code> with the user that will be used
     * @see #getUser()
     * @since 1.0
     */
    public void setUser(String user) {
        if (connectionPool_.isInitialized())
            throw new IllegalArgumentException("user can't be changed after the connection pool has been set up.");

        user_ = user;
    }

    /**
     * Retrieves the password that's used by this <code>Datasource</code>.
     *
     * @return a <code>String</code> with the password; or
     * <code>null</code> if the password hasn't been set
     * @see #setPassword(String)
     * @since 1.0
     */
    public String getPassword() {
        return password_;
    }

    /**
     * Sets the password that will be used to connect to the database.
     * It's not possible to change the password after a connection has been
     * obtained from a pooled datasource.
     *
     * @param password a <code>String</code> with the password that will be used
     * @see #getPassword()
     * @since 1.0
     */
    public void setPassword(String password) {
        if (connectionPool_.isInitialized())
            throw new IllegalArgumentException("password can't be changed after the connection pool has been set up.");

        password_ = password;
    }

    /**
     * Retrieves the size of the pool that's used by this
     * <code>Datasource</code>.
     *
     * @return a positive <code>int</code> with the size of the pool; or
     * <code>0</code> if no pool is being used
     * @see #isPooled()
     * @see #setPoolSize(int)
     * @since 1.0
     */
    public int getPoolSize() {
        return connectionPool_.getPoolSize();
    }

    /**
     * Indicates whether the <code>Datasource</code> uses a connection pool or
     * not
     *
     * @return <code>true</code> if a pool is being used by this
     * <code>Datasource</code>; or
     * <code>false</code> otherwise
     * @see #getPoolSize()
     * @see #setPoolSize(int)
     * @since 1.0
     */
    public boolean isPooled() {
        return getPoolSize() > 0;
    }

    /**
     * Sets the size of the connection pool that will be used to connect to the
     * database.
     *
     * @param poolSize a positive <code>int</code> with the size of the pool,
     *                 providing <code>0</code> will disable the use of a connection pool for
     *                 this <code>Datasource</code>.
     * @see #getPoolSize()
     * @see #isPooled()
     * @since 1.0
     */
    public void setPoolSize(int poolSize) {
        if (poolSize < 0) throw new IllegalArgumentException("poolSize can't be negative.");

        connectionPool_.setPoolSize(poolSize);
    }

    /**
     * Retrieves the sql to java and java to sql type mapping logic that
     * corresponds to the provide driver class name.
     *
     * @return a <code>SqlConversion</code> instance that is able to perform
     * the required type conversions for the provided jdbc driver
     * @throws UnsupportedJdbcDriverException when the provided jdbc isn't
     *                                        supported
     * @since 1.0
     */
    public SqlConversion getSqlConversion()
    throws UnsupportedJdbcDriverException {
        String driver = getDriver();
        if (null == sqlConversion_ &&
            null != driver) {
            try {
                sqlConversion_ = (SqlConversion) Class.forName("rife.database.types.databasedrivers." + StringUtils.encodeClassname(getAliasedDriver())).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new UnsupportedJdbcDriverException(driver, e);
            }
        }

        return sqlConversion_;
    }

    /**
     * Retrieves a <code>CapabilitiesCompensator</code> instance that is able to
     * compensate for certain missing database features
     *
     * @return the requested <code>CapabilitiesCompensator</code> instance
     * @throws UnsupportedJdbcDriverException when the provided jdbc isn't
     *                                        supported
     * @since 1.0
     */
    public CapabilitiesCompensator getCapabilitiesCompensator()
    throws UnsupportedJdbcDriverException {
        String driver = getDriver();
        if (null == capabilitiesCompensator_ &&
            null != driver) {
            try {
                capabilitiesCompensator_ = (CapabilitiesCompensator) Class.forName("rife.database.capabilities." + StringUtils.encodeClassname(getAliasedDriver())).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new UnsupportedJdbcDriverException(driver, e);
            }
        }

        return capabilitiesCompensator_;
    }

    /**
     * Returns a hash code value for the <code>Datasource</code>. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     *
     * @return an <code>int</code> with the hash code value for this
     * <code>Datasource</code>.
     * @see #equals(Object)
     * @since 1.0
     */
    public int hashCode() {
        int dataSourceHash = dataSource_ == null ? 1 : dataSource_.hashCode();
        int driverHash = driver_ == null ? 1 : driver_.hashCode();
        int urlHash = url_ == null ? 1 : url_.hashCode();
        int userHash = user_ == null ? 1 : user_.hashCode();
        int passwordHash = password_ == null ? 1 : password_.hashCode();
        return dataSourceHash * driverHash * urlHash * userHash * passwordHash;
    }

    /**
     * Indicates whether some other object is "equal to" this one. Only the
     * real connection parameters will be taken into account. The size of the
     * pool is not used for the comparison.
     *
     * @param object the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the object
     * argument; or
     * <code>false</code> otherwise
     * @see #hashCode()
     * @since 1.0
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (null == object) {
            return false;
        }

        if (!(object instanceof Datasource other_datasource)) {
            return false;
        }

        if (!other_datasource.getDriver().equals(getDriver())) {
            return false;
        }
        if (other_datasource.getUrl() != null || getUrl() != null) {
            if (null == other_datasource.getUrl() || null == getUrl()) {
                return false;
            }
            if (!other_datasource.getUrl().equals(getUrl())) {
                return false;
            }
        }
        if (other_datasource.getDataSource() != null || getDataSource() != null) {
            if (null == other_datasource.getDataSource() || null == getDataSource()) {
                return false;
            }
            if (!other_datasource.getDataSource().equals(getDataSource())) {
                return false;
            }
        }
        if (other_datasource.getUser() != null || getUser() != null) {
            if (null == other_datasource.getUser() || null == getUser()) {
                return false;
            }
            if (!other_datasource.getUser().equals(getUser())) {
                return false;
            }
        }
        if (other_datasource.getPassword() != null || getPassword() != null) {
            if (null == other_datasource.getPassword() || null == getPassword()) {
                return false;
            }
            if (!other_datasource.getPassword().equals(getPassword())) {
                return false;
            }
        }

        return true;
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
    public Datasource clone() {
        Datasource other = null;
        try {
            other = (Datasource) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.database").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }

        other.sqlConversion_ = sqlConversion_;
        other.connectionPool_ = connectionPool_;

        return other;
    }

    /**
     * Cleans up all connections that have been reserved by this datasource.
     *
     * @throws DatabaseException when an error occured during the cleanup
     * @since 1.0
     */
    public void cleanup()
    throws DatabaseException {
        connectionPool_.cleanup();
    }


    /**
     * Retrieves the instance of the connection pool that is provided by this
     * dtaasource.
     *
     * @return the requested instance of <code>ConnectionPool</code>
     */
    public ConnectionPool getPool() {
        return connectionPool_;
    }
}

